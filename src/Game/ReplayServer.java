/**
 * rscplus
 *
 * <p>This file is part of rscplus.
 *
 * <p>rscplus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscplus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscplus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/RSCPlus/rscplus>
 */
package Game;

import static Replay.game.constants.Game.itemActionMap;
import static Replay.game.constants.Game.opcodeToItemActionId;
import static Replay.scraper.ReplayEditor.VIRTUAL_OPCODE_CONNECT;
import static Replay.scraper.ReplayEditor.VIRTUAL_OPCODE_NOP;
import static java.net.StandardSocketOptions.TCP_NODELAY;

import Client.Logger;
import Client.Settings;
import Client.Util;
import Replay.common.ISAACCipher;
import Replay.game.constants.Game.ItemAction;
import Replay.scraper.ReplayEditor;
import Replay.scraper.ReplayPacket;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

public class ReplayServer implements Runnable {
  String playbackDirectory;
  DataInputStream input = null;
  FileInputStream file_input = null;
  ServerSocketChannel sock = null;
  SocketChannel client = null;
  ByteBuffer readBuffer = null;
  long frame_timer = 0;
  int timestamp_new = Replay.TIMESTAMP_EOF;

  int keyIndex = 0;
  int[] keys = new int[] {0xDEADBEEF, 0xDEADBEEF, 0xDEADBEEF, 0xDEADBEEF};
  boolean firstConnection = true;

  public boolean isReady = false;
  public boolean isDone = false;
  public boolean isSeeking = false;
  public boolean restart = false;
  public long size = 0;
  public long available = 0;
  public int timestamp_end = 0;

  public int client_read = 0;
  public int client_write = 0;
  public int client_writePrev = 0;
  ISAACCipher isaac = new ISAACCipher();

  public LinkedList<ReplayPacket> incomingPackets;
  public LinkedList<ReplayPacket> outgoingPackets;
  public int incomingPacketsSizeCache = 0;
  public int outgoingPacketsSizeCache = 0;
  public int outgoingPacketsIndex = 0;
  public int incomingPacketsIndex = 0;
  public ReplayPacket nextOutgoingPacket;
  public ReplayPacket nextIncomingPacket;
  public AtomicReference<ArrayList<String>> lastMenu;
  public int lastErrorChosenOptStamp; // timestamp in which couldnt replay chosen option
  public int lastErrorChosenOpt; // chosen option which couldnt be replayed in that moment

  ReplayServer(String directory) {
    playbackDirectory = directory;
    readBuffer = ByteBuffer.allocate(1024);
  }

  private void sync_with_client(boolean parseOpcodes) {
    int diff = client_write - client_read;
    int threshold = 200;

    // Wait for client
    while (!isDone && diff > threshold) {
      try {
        Thread.sleep(1);
      } catch (Exception e) {
      }
      diff = client_write - client_read;
    }
  }

  public int getPercentRemaining() {
    try {
      return (int) (available * 100 / size);
    } catch (Exception e) {
    }
    return 0;
  }

  public void seek(int new_timestamp) {
    if (Replay.timestamp > new_timestamp) Replay.restartReplayPlayback();

    timestamp_new = new_timestamp;
    isSeeking = true;
  }

  @Override
  public void run() {
    sock = null;
    isDone = false;
    // this one will try to find open port
    int port = -1;
    int usePort;
    // attempt to find free port starting from default port
    for (int i = 0; i < 10; i++) {
      try {
        new ServerSocket(Replay.DEFAULT_PORT + i).close();
        port = Replay.DEFAULT_PORT + i;
        break;
      } catch (IOException e) {
        continue;
      }
    }

    try {
      // Load replay
      File file = new File(playbackDirectory + "/in.bin.gz");
      size = file.length();
      file_input = new FileInputStream(file);
      input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(file_input)));
      timestamp_end = Util.getReplayEnding(file);
      Logger.Debug("ReplayServer: Replay loaded, waiting for client; length=" + timestamp_end);

      boolean parseOpcodesPrev = Settings.PARSE_OPCODES.get(Settings.currentProfile);
      boolean parseOpcode = parseOpcodesPrev;

      // Load replay a second time but using the RSCMinus method
      if (parseOpcode) {
        initializeIncomingOutgoingPackets();
        initializeNextIncomingOutgoingPackets();
      }

      // Start the server
      sock = ServerSocketChannel.open();
      // last attempt 10 + default port
      usePort = port == -1 ? Replay.DEFAULT_PORT + 10 : port;
      Replay.changePort(usePort);
      sock.bind(new InetSocketAddress(usePort));

      // Let's connect our client
      Logger.Debug("ReplayServer: Syncing playback to client...");
      isReady = true;
      client = sock.accept(); // waiting for Replay.initializeReplayPlayback()
      client.setOption(TCP_NODELAY, new Boolean(true));

      Logger.Debug("ReplayServer: Starting playback; port=" + usePort);

      frame_timer = System.currentTimeMillis();

      while (!isDone) {
        // Check if settings were changed for parse opcode
        parseOpcode = Settings.PARSE_OPCODES.get(Settings.currentProfile);
        if (parseOpcodesPrev != parseOpcode) {
          Client.runReplayCloseHook = true;
          Client.runReplayHook = true;
          isDone = true;
          break;
        }

        // Restart the replay
        if (restart) {
          if (!parseOpcode) {
            // Sync on restart
            Client.forceReconnect = true;
            boolean wasPaused = Replay.paused;
            int oldTimeSlice = Replay.frame_time_slice;
            Replay.frame_time_slice = 1000 / 50;
            client.close();
            Replay.paused = false;
            client = sock.accept();
            client.setOption(TCP_NODELAY, new Boolean(true));
            client_write = 0;
            client_read = 0;
            client_writePrev = 0;
            if (Replay.isSeeking) Replay.paused = wasPaused;
            else Replay.paused = false;
            Replay.frame_time_slice = oldTimeSlice;
          }
          input.close();
          file_input = new FileInputStream(file);
          input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(file_input)));
          Replay.timestamp = 0;
          Replay.timestamp_client = 0;
          Replay.timestamp_server_last = 0;
          keyIndex = 0;
          frame_timer = System.currentTimeMillis() + Replay.getFrameTimeSlice();
          incomingPacketsIndex = 0;
          outgoingPacketsIndex = 0;

          if (parseOpcode) {
            if (incomingPackets == null) {
              initializeIncomingOutgoingPackets();
            }
            initializeNextIncomingOutgoingPackets();
          }

          restart = false;
          Replay.isRestarting = false;
        }

        if (timestamp_new != Replay.TIMESTAMP_EOF || !Replay.paused) {
          if (!parseOpcode) {
            if (!doTick(parseOpcode)) isDone = true;
          } else {
            if (!doEditorTick(parseOpcode)) isDone = true;
          }
        } else {
          // Update timestamp immediately on unpausing
          frame_timer = System.currentTimeMillis();
          Thread.sleep(1);
        }
      }

      client.close();
      sock.close();
      input.close();

      Logger.Debug("ReplayServer: Replay ended");
      Client.switchLiveToReplay(false);

      if (ReplayQueue.currentIndex >= ReplayQueue.queue.size())
        Logger.Info("ReplayServer: Playback has finished");
    } catch (Exception e) {
      if (sock != null) {
        try {
          sock.close();
          client.close();
          input.close();
        } catch (Exception e2) {
        }
      }

      isReady = true;
      e.printStackTrace();
      Logger.Error("ReplayServer: Failed to serve replay");
    }
  }

  public void replayOutput(ReplayPacket packet) {
    try {
      int menuAction = opcodeToItemActionId.getOrDefault(packet.opcode, -1);

      // DROP_ITEM
      if (packet.opcode == 246) {
        int item = -1;
        try {
          item = Byte.toUnsignedInt(packet.data[0]) << 8 | Byte.toUnsignedInt(packet.data[1]);
        } catch (Exception e) {
        }
        if (item < 0) return;
        Client.displayMessage(
            "Dropping " + Item.item_name[Client.inventory_items[item]], Client.CHAT_NONE);
      }
      // SELECT_DIALOGUE_OPTION
      else if (packet.opcode == 116) {
        int chosen = -1;
        try {
          chosen = packet.data[0];
        } catch (Exception e) {
        }
        String[] menuOptions =
            lastMenu != null && lastMenu.get() != null && lastMenu.get().size() > 0
                ? lastMenu.get().toArray(new String[0])
                : Client.menuOptions;
        if (chosen < 0) {
          return;
        } else if (menuOptions == null || menuOptions[chosen] == null) {
          lastErrorChosenOpt = chosen;
          lastErrorChosenOptStamp = packet.timestamp;
          return;
        }
        Client.printSelectedOption(menuOptions, chosen);
        lastMenu.set(new ArrayList<String>());
      }
      // WALK_TO_SOURCE
      else if (packet.opcode == 16 || packet.opcode == 187) {
        int posX, posY;
        posX = Byte.toUnsignedInt(packet.data[0]) << 8 | Byte.toUnsignedInt(packet.data[1]);
        posY = Byte.toUnsignedInt(packet.data[2]) << 8 | Byte.toUnsignedInt(packet.data[3]);
        Client.displayWalkToSource(posX, posY);
      }
      // MENU ACTIONS such as NPC TALK TO, OBJECT COMMAND, etc
      else if (menuAction != -1) {
        int posX, posY, id;
        ItemAction action = itemActionMap.get(menuAction);
        if (action != null) {
          if (action.containsWorldPoint == 1) {
            posX = Byte.toUnsignedInt(packet.data[0]) << 8 | Byte.toUnsignedInt(packet.data[1]);
            posY = Byte.toUnsignedInt(packet.data[2]) << 8 | Byte.toUnsignedInt(packet.data[3]);
            Client.displayMenuAction(action.name, posX, posY);
          } else if (action.containsWorldPoint == 2 || action.containsWorldPoint == 3) {
            id = Byte.toUnsignedInt(packet.data[0]) << 8 | Byte.toUnsignedInt(packet.data[1]);
            Client.displayMenuAction(action.name, id);
          }
        }
      }
    } catch (Exception e) {
      Logger.Warn("ReplayServer: Can not process corrupt output packet");
      e.printStackTrace();
    }
  }

  public void readInput(ReplayPacket packet) {
    try {
      // SHOW_DIALOGUE_MENU
      if (packet.opcode == 245) {
        ArrayList<String> menu = new ArrayList<String>();
        int numOpts = 0;
        boolean read = true;
        byte[] option;
        try {
          numOpts = packet.data[0];
          if (numOpts > 0) {
            read = true;
            int start = 1;
            int end = 1;
            int cur;
            while (read) {
              for (cur = start + 1; cur < packet.data.length; cur++) {
                if (packet.data[cur] == 0) {
                  end = cur;
                  break;
                }
              }
              // inclusive from, exclusive to
              option = Arrays.copyOfRange(packet.data, start + 1, end);
              menu.add(new String(option));
              start = ++end;
              if (cur >= packet.data.length || menu.size() == numOpts) {
                read = false;
              }
            }
          }
          lastMenu.set(menu);
        } catch (Exception e) {
        }
        if (menu.size() == 0) {
          return;
        }
        Client.printReceivedOptions(menu.toArray(new String[0]), menu.size());
        // replay back chosen option (if couldnt be played earlier bc some bad sync)
        if (lastMenu.get().size() > 0
            && Math.abs(packet.timestamp - lastErrorChosenOptStamp) < 100
            && lastErrorChosenOpt > 0
            && lastErrorChosenOpt < lastMenu.get().size()) {
          Client.printSelectedOption(lastMenu.get().toArray(new String[0]), lastErrorChosenOpt);
          lastErrorChosenOpt = -1;
          lastErrorChosenOptStamp = -1;
        }
      }
    } catch (Exception e) {
      Logger.Warn("ReplayServer: Can not process corrupt incoming packet");
      e.printStackTrace();
    }
  }

  public int getXTEAKey() {
    if (!Settings.PARSE_OPCODES.get(Settings.currentProfile) || isDone) return 0;

    // Wrap keys if they go out of bounds
    if (keyIndex >= keys.length) keyIndex = 0;

    return keys[keyIndex++];
  }

  public boolean doEditorTick(boolean parseOpcodes) {
    int timestamp_input = nextIncomingPacket.timestamp;

    // Handle outgoing packets
    while (outgoingPacketsIndex != (outgoingPacketsSizeCache - 1)
        && nextOutgoingPacket.timestamp <= timestamp_input) {
      Logger.Opcode(
          nextOutgoingPacket.timestamp, "OUT", nextOutgoingPacket.opcode, nextOutgoingPacket.data);
      replayOutput(nextOutgoingPacket);
      nextOutgoingPacket = outgoingPackets.get(++outgoingPacketsIndex);
    }

    // Handle seeking
    if (timestamp_new != Replay.TIMESTAMP_EOF) {
      Replay.timestamp = timestamp_input;
      if (Replay.timestamp >= timestamp_new) {
        Replay.isSeeking = false;
        timestamp_new = Replay.TIMESTAMP_EOF;
        Replay.updateFrameTimeSlice();
        frame_timer = System.currentTimeMillis();
        if (Replay.paused) Replay.resetFrameTimeSlice();
        isSeeking = false;
      }
    }

    // Synchronize the server to input
    while (Replay.timestamp < timestamp_input) {
      long time = System.currentTimeMillis();
      if (time >= frame_timer) {
        frame_timer += Replay.getFrameTimeSlice();
        Replay.incrementTimestamp();
      }

      // Don't hammer the cpu, unless we have to
      long sleepTime = frame_timer - System.currentTimeMillis() - 1;
      if (sleepTime > 0) {
        try {
          Thread.sleep(sleepTime);
        } catch (Exception e) {
        }
      }
    }

    while (nextIncomingPacket.timestamp == timestamp_input) {
      // Handle incoming packet logging
      Logger.Opcode(
          nextIncomingPacket.timestamp, " IN", nextIncomingPacket.opcode, nextIncomingPacket.data);
      readInput(nextIncomingPacket);

      // Do nothing
      if (nextIncomingPacket.opcode == VIRTUAL_OPCODE_NOP) {
        nextIncomingPacket = incomingPackets.get(++incomingPacketsIndex);
        return true;
      }

      ByteBuffer buffer = null;

      // Login response/disconnect
      if (nextIncomingPacket.opcode == VIRTUAL_OPCODE_CONNECT) {
        byte loginResponse = nextIncomingPacket.data[0];
        buffer = ByteBuffer.allocate(1);
        buffer.put(loginResponse);

        // Handle disconnecting
        if (!firstConnection) {
          try {
            Client.forceReconnect = true;
            int oldTimeSlice = Replay.frame_time_slice;
            boolean oldPaused = Replay.paused;
            Replay.frame_time_slice = 1000 / 50;
            Replay.paused = false;
            Logger.Info("ReplayServer: Killing client connection");
            client.close();
            Logger.Info("ReplayServer: Reconnecting client");
            client = sock.accept();
            client.setOption(TCP_NODELAY, new Boolean(true));
            client_write = 0;
            client_read = 0;
            client_writePrev = client_write;
            Logger.Info("ReplayServer: Client reconnected");
            Replay.frame_time_slice = oldTimeSlice;
            Replay.paused = oldPaused;
          } catch (Exception e) {
            Logger.Error("ReplayServer: Error reconnecting client");
            return false;
          }
        } else {
          firstConnection = false;
        }

        isaac.reset();
        isaac.setKeys(keys);
      } else {
        int packetLength = 1;
        if (nextIncomingPacket.data != null) packetLength += nextIncomingPacket.data.length;

        // Encode packet and send
        int encodedOpcode = (nextIncomingPacket.opcode + isaac.getNextValue()) & 0xFF;
        if (packetLength == 1) {
          buffer = ByteBuffer.allocate(2);
          buffer.put((byte) (packetLength));
          buffer.put((byte) (encodedOpcode));
        } else {
          if (packetLength < 160) {
            buffer = ByteBuffer.allocate(packetLength + 1);
            int dataSize = packetLength - 1;
            buffer.put((byte) (packetLength));
            buffer.put((byte) (nextIncomingPacket.data[dataSize - 1]));
            buffer.put((byte) (encodedOpcode));
            if (dataSize > 1) buffer.put(nextIncomingPacket.data, 0, dataSize - 1);
          } else {
            buffer = ByteBuffer.allocate(packetLength + 2);
            buffer.put((byte) (packetLength / 256 + 160));
            buffer.put((byte) (packetLength & 0xFF));
            buffer.put((byte) (encodedOpcode));
            buffer.put(nextIncomingPacket.data, 0, nextIncomingPacket.data.length);
          }
        }
      }

      if (buffer != null) {
        try {
          buffer.flip();
          int writeSize = client.write(buffer);
          if (writeSize > 0) {
            client_writePrev = client_write;
            client_write += writeSize;
            sync_with_client(parseOpcodes);
          }
        } catch (Exception e) {
          return false;
        }
      }

      // End of replay
      if (incomingPacketsIndex == (incomingPacketsSizeCache - 1)) return false;

      // Load next packet
      nextIncomingPacket = incomingPackets.get(++incomingPacketsIndex);
    }

    return true;
  }

  public boolean doTick(boolean parseOpcodes) {
    try {
      int timestamp_input = input.readInt();

      // We've reached the end of the replay
      if (timestamp_input == Replay.TIMESTAMP_EOF) return false;

      int length = input.readInt();
      ByteBuffer buffer = null;
      if (length > 0) {
        buffer = ByteBuffer.allocate(length);
        input.read(buffer.array());
        available = file_input.available();
      }

      if (timestamp_input < Replay.timestamp) {
        Logger.Debug(
            String.format(
                "timestamp_input: %d; Replay.timestamp: %d", timestamp_input, Replay.timestamp));
        Logger.Warn("ReplayServer: Input timestamp is in the past, skipping packet");
        return true;
      }

      boolean disconnected = false;

      // Handle disconnects in replay playback
      if (Replay.replay_version >= 1) {
        // v1+ Disconnect handler
        // If packet length is -1, it's a disconnection
        if (length == -1) {
          Client.forceReconnect = true;
          Logger.Info("ReplayServer: Killing client connection");
          client.close();
          Logger.Info("ReplayServer: Reconnecting client");
          client = sock.accept();
          client.setOption(TCP_NODELAY, new Boolean(true));
          client_write = 0;
          client_read = 0;
          client_writePrev = client_write;
          Logger.Info("ReplayServer: Client reconnected");

          if (Replay.isSeeking || Settings.FAST_DISCONNECT.get(Settings.currentProfile))
            Replay.timestamp = timestamp_input;

          disconnected = true;
        }
      } else {
        // v0 Disconnect handler
        int timestamp_diff = timestamp_input - Replay.timestamp;

        // If the timestamp is 400+ frames in the future, it's a client disconnection
        // So we disconnect and reconnect the client
        // NOTE: Versions older than v1 have no disconnection indication
        if (timestamp_diff > 400) {
          Client.forceReconnect = true;
          Logger.Info(
              "ReplayServer: Killing client connection; timestamp="
                  + Replay.timestamp
                  + ", timestamp_diff="
                  + timestamp_diff);
          client.close();
          client = sock.accept();
          client.setOption(TCP_NODELAY, new Boolean(true));
          client_write = 0;
          client_read = 0;
          client_writePrev = client_write;
          timestamp_diff -= 400;
          Replay.timestamp = timestamp_input - timestamp_diff;
          Logger.Info("ReplayServer: Reconnected client; timestamp=" + Replay.timestamp);

          if (Replay.isSeeking || Settings.FAST_DISCONNECT.get(Settings.currentProfile))
            Replay.timestamp = timestamp_input;

          disconnected = true;
        }
      }

      // Handle seeking
      if (timestamp_new != Replay.TIMESTAMP_EOF) {
        Replay.timestamp = timestamp_input;
        if (Replay.timestamp >= timestamp_new) {
          Replay.isSeeking = false;
          timestamp_new = Replay.TIMESTAMP_EOF;
          Replay.updateFrameTimeSlice();
          frame_timer = System.currentTimeMillis();
          if (Replay.paused) Replay.resetFrameTimeSlice();
          isSeeking = false;
        }
      }

      // Synchronize the server to input
      while (Replay.timestamp < timestamp_input) {
        long time = System.currentTimeMillis();
        if (time >= frame_timer) {
          frame_timer += Replay.getFrameTimeSlice();
          Replay.incrementTimestamp();
        }

        // Don't hammer the cpu, unless we have to
        long sleepTime = frame_timer - System.currentTimeMillis() - 1;
        if (sleepTime > 0) Thread.sleep(sleepTime);
      }

      // Write out replay data to the client
      try {
        if (buffer != null) {
          int writeSize = client.write(buffer);
          if (writeSize > 0) {
            client_writePrev = client_write;
            client_write += writeSize;
            sync_with_client(parseOpcodes);
          }
        }
      } catch (Exception e) {
      }

      return true;
    } catch (Exception e) {
      // e.printStackTrace();
    }

    return false;
  }

  public void initializeIncomingOutgoingPackets() {
    ReplayEditor editor = new ReplayEditor();
    boolean success = editor.importData(playbackDirectory);

    if (!success) {
      Logger.Warn("@|red Can't parse this as complete replay!|@");
    }

    Logger.Debug("client version: " + editor.getReplayVersion().clientVersion);
    Logger.Debug("replay version: " + editor.getReplayVersion().version);

    incomingPackets = editor.getIncomingPackets();
    outgoingPackets = editor.getOutgoingPackets();

    lastMenu = new AtomicReference<ArrayList<String>>();

    Logger.Info(String.format("Incoming packet length: %d", incomingPackets.size()));
    Logger.Info(String.format("Outgoing packet length: %d", outgoingPackets.size()));

    incomingPacketsIndex = 0;
    outgoingPacketsIndex = 0;
    incomingPacketsSizeCache = incomingPackets.size();
    outgoingPacketsSizeCache = outgoingPackets.size();
  }

  public void initializeNextIncomingOutgoingPackets() {
    if (incomingPacketsSizeCache > 0) {
      nextIncomingPacket = incomingPackets.getFirst();
    } else {
      // RSC+ won't be able to play this replay, so let's skip it.
      Logger.Warn("@|red No incoming packets in that Replay, moving on...|@");
      ReplayQueue.nextReplay();
      isDone = true;
    }
    if (outgoingPacketsSizeCache > 0) nextOutgoingPacket = outgoingPackets.getFirst();
  }
}
