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

import Client.FlushableGZIPOutputStream;
import Client.Launcher;
import Client.Logger;
import Client.QueueWindow;
import Client.Settings;
import Client.Speedrun;
import Client.Util;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Replay {
  // If we ever change replays in a way that breaks backwards compatibility,
  // we need to increment this
  public static int VERSION = 5;

  static DataOutputStream output = null;
  static DataOutputStream input = null;
  static DataOutputStream keys = null;
  static DataOutputStream keyboard = null;
  static DataOutputStream mouse = null;
  static DataOutputStream metadata = null;

  static DataInputStream play_keys = null;
  static DataInputStream play_keyboard = null;
  static DataInputStream play_mouse = null;

  static MessageDigest output_checksum = null;
  static MessageDigest input_checksum = null;

  public static final byte KEYBOARD_TYPED = 0;
  public static final byte KEYBOARD_PRESSED = 1;
  public static final byte KEYBOARD_RELEASED = 2;

  public static final byte MOUSE_CLICKED = 0;
  public static final byte MOUSE_ENTERED = 1;
  public static final byte MOUSE_EXITED = 2;
  public static final byte MOUSE_PRESSED = 3;
  public static final byte MOUSE_RELEASED = 4;
  public static final byte MOUSE_DRAGGED = 5;
  public static final byte MOUSE_MOVED = 6;
  public static final byte MOUSE_WHEEL_MOVED = 7;

  public static final int DEFAULT_PORT = 43594;

  public static final int TIMESTAMP_EOF = -1;

  public static boolean isSeeking = false;
  public static boolean isPlaying = false;
  public static boolean isRecording = false;
  public static boolean isRestarting = false;
  public static boolean paused = false;
  public static boolean closeDialogue = false;

  // Hack for player position
  public static boolean ignoreFirstMovement = true;

  public static int fps = 50;
  public static float fpsPlayMultiplier = 1.0f;
  public static int frame_time_slice;
  public static int connection_port;

  public static ReplayServer replayServer = null;
  public static Thread replayThread = null;
  public static String replayDirectory;

  public static int replay_version;
  public static int client_version;
  public static int prevPlayerX;
  public static int prevPlayerY;

  // when replay is initialized, client.username_login will be set to this
  public static final String excludeUsername = "excludemefromreplaytracking";

  // TODO: these will be needed to be added as metadata for existing replays
  // and stored for new ones
  public static boolean replayMembers = true;
  public static boolean replayVeterans = false;

  public static int timestamp;
  public static int timestamp_disconnect;
  public static int timestamp_client;
  public static int timestamp_server_last;
  public static int timestamp_kb_input;
  public static int timestamp_mouse_input;

  public static boolean started_record_kb_mouse = true;

  public static int enc_opcode;
  public static int retained_timestamp;
  public static byte[] retained_bytes = null;
  public static int retained_off;
  public static int retained_bread;

  public static byte[] ipAddressMetadata;

  public static int timestamp_lag = 0;

  public static void incrementTimestamp() {
    timestamp++;

    if (timestamp == TIMESTAMP_EOF) {
      timestamp = 0;
    }
  }

  public static void incrementTimestampClient() {
    timestamp_client++;

    if (timestamp_client == TIMESTAMP_EOF) {
      timestamp_client = 0;
    }
  }

  public static void init(String directory) {
    timestamp = 0;
    timestamp_client = 0;
    timestamp_server_last = 0;
    timestamp_disconnect = TIMESTAMP_EOF;
    isRestarting = false;
    isSeeking = false;
    isRecording = false;
    isPlaying = false;
    replayServer = null;
    paused = false;
    closeDialogue = false;
    replayDirectory = directory;
    replayMembers =
        ((int) Replay.readMetadata(directory)[4] & (1 << 31))
            == 0; // first bit of user settings is true if replay is F2P
  }

  public static int getServerLag() {
    return timestamp - timestamp_server_last;
  }

  public static int getSeekEnd() {
    if (replayServer == null) return 0;

    return replayServer.timestamp_new;
  }

  public static void seek(int new_timestamp) {
    isSeeking = true;
    frame_time_slice = 0;
    replayServer.seek(new_timestamp);
  }

  public static boolean initializeReplayPlayback() {
    try {
      // We read in this information to adjust our replay method based on versioning
      // No need to check if output matches until other revisions come out
      DataInputStream version =
          new DataInputStream(
              new BufferedInputStream(
                  new FileInputStream(new File(replayDirectory + "/version.bin"))));
      replay_version = version.readInt();
      client_version = version.readInt();
      version.close();

      if (replay_version > Replay.VERSION) {
        String replayVersionErrorMessage =
            "The replay you selected is for replay version "
                + replay_version
                + ".<br/>"
                + "You may need to update RSCPlus to run this replay.</br>";
        JPanel replayVersionErrorPanel = Util.createOptionMessagePanel(replayVersionErrorMessage);

        JOptionPane.showMessageDialog(
            Game.getInstance().getApplet(),
            replayVersionErrorPanel,
            "RSCPlus",
            JOptionPane.ERROR_MESSAGE,
            Launcher.scaled_icon_warn);
        return false;
      }

      /* TODO:
       * Should also write out endpoint, so we know what server the replay is for
      if (replay_version <= 3) {
        boolean claims_authentic_rsc = true;
      }
      */

      if (client_version < 234 || client_version > 235) {
        String replayClientVersionErrorMessage =
            "The replay you selected is for client version "
                + client_version
                + ".<br/>"
                + "RSCPlus currently only supports versions 234 and 235.<br/>";
        JPanel replayClientVersionErrorPanel =
            Util.createOptionMessagePanel(replayClientVersionErrorMessage);

        JOptionPane.showMessageDialog(
            Game.getInstance().getApplet(),
            replayClientVersionErrorPanel,
            "RSCPlus",
            JOptionPane.ERROR_MESSAGE,
            Launcher.scaled_icon_warn);
        return false;
      }

      play_keys =
          new DataInputStream(
              new BufferedInputStream(
                  new FileInputStream(new File(replayDirectory + "/keys.bin"))));
      if (Settings.RECORD_KB_MOUSE.get(Settings.currentProfile)) {
        File file = new File(replayDirectory + "/keyboard.bin.gz");
        if (file.exists()) {
          play_keyboard =
              new DataInputStream(
                  new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
          timestamp_kb_input = play_keyboard.readInt();
        }
        file = new File(replayDirectory + "/mouse.bin.gz");
        if (file.exists()) {
          play_mouse =
              new DataInputStream(
                  new BufferedInputStream(
                      new GZIPInputStream(
                          new FileInputStream(new File(replayDirectory + "/mouse.bin.gz")))));
          timestamp_mouse_input = play_mouse.readInt();
        }
        started_record_kb_mouse = true;
      } else {
        started_record_kb_mouse = false;
      }
    } catch (Exception e) {
      play_keys = null;
      play_keyboard = null;
      play_mouse = null;

      JPanel replayErrorPanel =
          Util.createOptionMessagePanel("An error has occurred while trying to open the replay.");

      JOptionPane.showMessageDialog(
          Game.getInstance().getApplet(),
          replayErrorPanel,
          "RSCPlus",
          JOptionPane.ERROR_MESSAGE,
          Launcher.scaled_icon_warn);
      return false;
    }
    Game.getInstance().getJConfig().changeWorld(Settings.WORLDS_TO_DISPLAY + 1);
    if (replayServer != null) replayServer.isDone = true;
    replayServer = new ReplayServer(replayDirectory);
    replayThread = new Thread(replayServer);
    replayThread.start();
    ignoreFirstMovement = true;

    // if (Client.strings[662].startsWith("from:")) {
    // Client.strings[662] = "@bla@from:";
    // }
    isPlaying = true;

    QueueWindow.updatePlaying();

    // Wait
    try {
      while (!replayServer.isReady) Thread.sleep(1);
    } catch (Exception e) {
    }
    Client.switchLiveToReplay(true);
    Client.login(false, excludeUsername, "");
    updateFrameTimeSlice();
    return true;
  }

  public static void restartReplayPlayback() {
    if (timestamp == 0 || isRestarting || play_keys == null) return;

    try {
      isRestarting = true;
      play_keys.close();
      play_keys =
          new DataInputStream(
              new BufferedInputStream(
                  new FileInputStream(new File(replayDirectory + "/keys.bin"))));
      replayServer.restart = true;
    } catch (Exception e) {
      e.printStackTrace();
      isRestarting = false;
      shutdown_error();
    }
  }

  public static void closeReplayPlayback() {
    Client.runReplayCloseHook = true;
  }

  public static void handleReplayClosing() {
    if (play_keys == null) return;

    replayServer.isDone = true;
    try {
      replayThread.join();
    } catch (Exception e) {
    }

    try {
      play_keys.close();
      play_keyboard.close();
      play_mouse.close();

      play_keys = null;
      play_keyboard = null;
      play_mouse = null;
    } catch (Exception e) {
      play_keys = null;
      play_keyboard = null;
      play_mouse = null;
    }

    Game.getInstance().getJConfig().changeWorld(Settings.WORLD.get(Settings.currentProfile));
    isSeeking = false;
    resetFrameTimeSlice();
    Client.closeConnection(false);
    Client.switchLiveToReplay(false);
    // resetPort();
    // fpsPlayMultiplier = 1.0f;
    resetPatchClient();
    isPlaying = false;
    QueueWindow.updatePlaying();
  }

  public static void initializeReplayRecording() {
    // No username specified, exit
    if (Client.username_login.length() == 0) return;

    String timeStamp = new SimpleDateFormat("MM-dd-yyyy HH.mm.ss").format(new Date());

    File replayStorageDir = new File(Settings.REPLAY_STORAGE_PATH.get("custom"));

    // Look for an existing character-specific dir using server-escaped names
    File existingPlayerDir = null;
    File[] replayStorageDirFiles = replayStorageDir.listFiles();
    if (replayStorageDirFiles != null) {
      for (File f : replayStorageDirFiles) {
        if (f.isDirectory()) {
          String sanitizedFolderName = Util.formatPlayerName(f.getName());
          String sanitizedLoginName = Util.formatPlayerName(Client.username_login);

          if (sanitizedFolderName.equals(sanitizedLoginName)) {
            existingPlayerDir = f;
            break;
          }
        }
      }
    }

    String recordingDirectory;

    if (existingPlayerDir != null) {
      recordingDirectory = existingPlayerDir.toString();
    } else {
      recordingDirectory = replayStorageDir + "/" + Client.username_login;
    }

    Util.makeDirectory(recordingDirectory);
    recordingDirectory = recordingDirectory + "/" + timeStamp;
    Util.makeDirectory(recordingDirectory);

    try {
      // Write out version information
      DataOutputStream version =
          new DataOutputStream(
              new BufferedOutputStream(
                  new FileOutputStream(new File(recordingDirectory + "/version.bin"))));
      version.writeInt(Replay.VERSION);
      version.writeInt(Client.version);
      version.close();

      output =
          new DataOutputStream(
              new BufferedOutputStream(
                  new FlushableGZIPOutputStream(
                      new FileOutputStream(new File(recordingDirectory + "/out.bin.gz")))));
      input =
          new DataOutputStream(
              new BufferedOutputStream(
                  new FlushableGZIPOutputStream(
                      new FileOutputStream(new File(recordingDirectory + "/in.bin.gz")))));
      keys =
          new DataOutputStream(
              new BufferedOutputStream(
                  new FileOutputStream(new File(recordingDirectory + "/keys.bin"))));
      if (Settings.RECORD_KB_MOUSE.get(Settings.currentProfile)) {
        keyboard =
            new DataOutputStream(
                new BufferedOutputStream(
                    new FlushableGZIPOutputStream(
                        new FileOutputStream(new File(recordingDirectory + "/keyboard.bin.gz")))));
        mouse =
            new DataOutputStream(
                new BufferedOutputStream(
                    new FlushableGZIPOutputStream(
                        new FileOutputStream(new File(recordingDirectory + "/mouse.bin.gz")))));
        started_record_kb_mouse =
            true; // need this to know whether or not to close the file if the user changes settings
        // mid-recording
      } else {
        started_record_kb_mouse = false;
      }
      metadata =
          new DataOutputStream(
              new BufferedOutputStream(
                  new FileOutputStream(new File(recordingDirectory + "/metadata.bin"))));

      output_checksum = MessageDigest.getInstance("SHA-256");
      input_checksum = MessageDigest.getInstance("SHA-256");

      Logger.Info("Replay recording started");
    } catch (Exception e) {
      output = null;
      input = null;
      keys = null;
      keyboard = null;
      mouse = null;
      Logger.Error("Unable to create replay files");
      return;
    }

    retained_timestamp = TIMESTAMP_EOF;
    retained_bytes = null;
    isRecording = true;
  }

  public static void closeReplayRecording() {
    if (input == null) return;

    try {
      // since we are working with packet retention, last packet on memory has not been written,
      // write it here
      if (retained_timestamp != TIMESTAMP_EOF && retained_bytes != null) {
        try {
          ByteBuffer buffer = ByteBuffer.allocate(retained_bread + 8);
          buffer.putInt(retained_timestamp);
          buffer.putInt(retained_bread);
          buffer.put(retained_bytes, retained_off, retained_bread);
          input_checksum.update(buffer.array());
          input.write(buffer.array());
          input.flush();
        } catch (Exception e) {
          e.printStackTrace();
          shutdown_error();
        }
      }

      // Write EOF values
      ByteBuffer buffer = ByteBuffer.allocate(4);
      buffer.putInt(TIMESTAMP_EOF);
      input_checksum.update(buffer.array());
      input.write(buffer.array());
      output_checksum.update(buffer.array());
      output.write(buffer.array());

      // Write Checksum
      input.write(input_checksum.digest());
      output.write(output_checksum.digest());

      Logger.Debug("Generating metadata");
      // generate new metadata
      try {
        metadata.writeInt(retained_timestamp);
        metadata.writeLong(System.currentTimeMillis());
        if (ipAddressMetadata.length == 4) { // ipv4, need padding in the ipv6 fields
          metadata.writeInt(0);
          metadata.writeInt(0);
          metadata.writeInt(0xFFFF);
        }
        for (int i = 0; i < ipAddressMetadata.length; i++) {
          metadata.writeByte(ipAddressMetadata[i]);
        }

        metadata.writeByte(0); // rscminus conversion settings, none used in this case

        int userSettings = 0;
        // "User settings", 1st bit is used to denote if replay was on F2P or Members world.
        // Rest of the 31 bits are not implemented for any purpose at this time
        if (!Client.members) {
          userSettings |= 1 << 31;
        }
        metadata.writeInt(userSettings);

        metadata.flush();
        metadata.close();
      } catch (IOException e) {
        Logger.Error("Couldn't write metadata.bin!");
      }

      output.close();
      input.close();
      keys.close();
      if (started_record_kb_mouse) {
        keyboard.write(buffer.array());
        mouse.write(buffer.array());
        keyboard.close();
        mouse.close();
      }

      output = null;
      input = null;
      keys = null;
      keyboard = null;
      mouse = null;

      output_checksum = null;
      input_checksum = null;

      retained_timestamp = TIMESTAMP_EOF;
      retained_bytes = null;

      Logger.Info("Replay recording stopped");
    } catch (Exception e) {
      output = null;
      input = null;
      keys = null;
      keyboard = null;
      mouse = null;
      Logger.Error("Unable to close replay files");
      return;
    }

    isRecording = false;
  }

  public static void update() {
    // If the replay is done playing, disable replay mode
    if (isPlaying) {
      // Reset inactivity timer, we're not the ones playing the game
      Client.setInactivityTimer(0);

      int playerX = prevPlayerX;
      int playerY = prevPlayerY;

      // Make sure we're loaded in
      if (Client.isGameLoaded) {
        playerX = Client.worldX;
        playerY = Client.worldY;
      }

      // If the player moves, we're going to run some events
      if (playerX != prevPlayerX || playerY != prevPlayerY) {
        if (!ignoreFirstMovement) {
          prevPlayerX = playerX;
          prevPlayerY = playerY;

          // Close dialogues
          closeDialogue = true;
        } else {
          ignoreFirstMovement = false;
          prevPlayerX = playerX;
          prevPlayerY = playerY;
        }
      }

      if (closeDialogue) {
        // Close welcome screen
        if (Client.isWelcomeScreen()) Client.show_welcome = false;

        KeyboardHandler.dialogue_option = 1;
        closeDialogue = false;
      }

      // Replay server is no longer running
      if (replayServer.isDone) closeReplayPlayback();
    }

    // Increment the replay timestamp
    if (!Replay.isPlaying) Replay.incrementTimestamp();
    else Replay.incrementTimestampClient();
  }

  public static int getPercentPlayed() {
    return 100 - replayServer.getPercentRemaining();
  }

  public static void playKeyboardInput() {
    try {
      while (timestamp >= timestamp_kb_input) {
        byte event = play_keyboard.readByte();
        char keychar = play_keyboard.readChar();
        int keycode = play_keyboard.readInt();
        int modifier = play_keyboard.readInt();
        KeyEvent keyEvent;
        switch (event) {
          case KEYBOARD_PRESSED:
            keyEvent =
                new KeyEvent(
                    Game.getInstance().getApplet(),
                    KeyEvent.KEY_PRESSED,
                    timestamp,
                    modifier,
                    keycode,
                    keychar);
            Client.handler_keyboard.keyPressed(keyEvent);
            break;
          case KEYBOARD_RELEASED:
            keyEvent =
                new KeyEvent(
                    Game.getInstance().getApplet(),
                    KeyEvent.KEY_RELEASED,
                    timestamp,
                    modifier,
                    keycode,
                    keychar);
            Client.handler_keyboard.keyReleased(keyEvent);
            break;
          case KEYBOARD_TYPED:
            keyEvent =
                new KeyEvent(
                    Game.getInstance().getApplet(),
                    KeyEvent.KEY_TYPED,
                    timestamp,
                    modifier,
                    keycode,
                    keychar);
            Client.handler_keyboard.keyTyped(keyEvent);
            break;
        }
        timestamp_kb_input = play_keyboard.readInt();
      }
    } catch (Exception e) {
    }
  }

  public static void playMouseInput() {
    try {
      while (timestamp >= timestamp_mouse_input) {
        byte event = play_mouse.readByte();
        int x = play_mouse.readInt();
        int y = play_mouse.readInt();
        int rotation = play_mouse.readInt();
        int modifier = play_mouse.readInt();
        int clickCount = play_mouse.readInt();
        int scrollType = play_mouse.readInt();
        int scrollAmount = play_mouse.readInt();
        boolean popupTrigger = play_mouse.readBoolean();
        int button = play_mouse.readInt();
        MouseEvent mouseEvent;
        switch (event) {
          case MOUSE_CLICKED:
            mouseEvent =
                new MouseEvent(
                    Game.getInstance().getApplet(),
                    MouseEvent.MOUSE_CLICKED,
                    timestamp,
                    modifier,
                    x,
                    y,
                    clickCount,
                    popupTrigger,
                    button);
            Client.handler_mouse.mouseClicked(mouseEvent);
            break;
          case MOUSE_ENTERED:
            mouseEvent =
                new MouseEvent(
                    Game.getInstance().getApplet(),
                    MouseEvent.MOUSE_ENTERED,
                    timestamp,
                    modifier,
                    x,
                    y,
                    clickCount,
                    popupTrigger,
                    button);
            Client.handler_mouse.mouseEntered(mouseEvent);
            break;
          case MOUSE_EXITED:
            mouseEvent =
                new MouseEvent(
                    Game.getInstance().getApplet(),
                    MouseEvent.MOUSE_EXITED,
                    timestamp,
                    modifier,
                    x,
                    y,
                    clickCount,
                    popupTrigger,
                    button);
            Client.handler_mouse.mouseExited(mouseEvent);
            break;
          case MOUSE_PRESSED:
            mouseEvent =
                new MouseEvent(
                    Game.getInstance().getApplet(),
                    MouseEvent.MOUSE_PRESSED,
                    timestamp,
                    modifier,
                    x,
                    y,
                    clickCount,
                    popupTrigger,
                    button);
            Client.handler_mouse.mousePressed(mouseEvent);
            break;
          case MOUSE_RELEASED:
            mouseEvent =
                new MouseEvent(
                    Game.getInstance().getApplet(),
                    MouseEvent.MOUSE_RELEASED,
                    timestamp,
                    modifier,
                    x,
                    y,
                    clickCount,
                    popupTrigger,
                    button);
            Client.handler_mouse.mouseReleased(mouseEvent);
            break;
          case MOUSE_DRAGGED:
            mouseEvent =
                new MouseEvent(
                    Game.getInstance().getApplet(),
                    MouseEvent.MOUSE_DRAGGED,
                    timestamp,
                    modifier,
                    x,
                    y,
                    clickCount,
                    popupTrigger,
                    button);
            Client.handler_mouse.mouseDragged(mouseEvent);
            break;
          case MOUSE_MOVED:
            mouseEvent =
                new MouseEvent(
                    Game.getInstance().getApplet(),
                    MouseEvent.MOUSE_MOVED,
                    timestamp,
                    modifier,
                    x,
                    y,
                    clickCount,
                    popupTrigger,
                    button);
            Client.handler_mouse.mouseMoved(mouseEvent);
            break;
          case MOUSE_WHEEL_MOVED:
            MouseWheelEvent wheelEvent =
                new MouseWheelEvent(
                    Game.getInstance().getApplet(),
                    MouseWheelEvent.MOUSE_WHEEL,
                    timestamp,
                    modifier,
                    x,
                    y,
                    clickCount,
                    popupTrigger,
                    scrollType,
                    scrollAmount,
                    rotation);
            Client.handler_mouse.mouseWheelMoved(wheelEvent);
            break;
        }
        timestamp_mouse_input = play_mouse.readInt();
      }
    } catch (Exception e) {
    }
  }

  public static void togglePause() {
    paused = !paused;

    if (paused) {
      resetFrameTimeSlice();
    } else {
      updateFrameTimeSlice();
    }
  }

  public static boolean isValid(String path) {
    File keys = new File(path + "/keys.bin");
    boolean filesExist =
        new File(path + "/in.bin.gz").exists()
            && keys.exists()
            && new File(path + "/version.bin").exists();
    if (filesExist) {
      return keys.length() > 0;
    }
    return false;
  }

  public static boolean isBroken(String path) {
    File keys = new File(path + "/keys.bin");
    if (keys.exists()) {
      return keys.length() <= 0;
    }
    return false;
  }

  public static void checkAndGenerateMetadata(String replayFolder) {
    if (new File(replayFolder + "/metadata.bin").exists()) {
      return;
    }
    Logger.Info("Generating metadata for " + replayFolder);
    // generate new metadata
    int replayLength = Util.getReplayEnding(new File(replayFolder + "/in.bin.gz"));
    long dateModified = new File(replayFolder + "/keys.bin").lastModified();

    try {
      DataOutputStream metadata =
          new DataOutputStream(
              new BufferedOutputStream(
                  new FileOutputStream(new File(replayFolder + "/metadata.bin"))));
      metadata.writeInt(replayLength);
      metadata.writeLong(dateModified);
      // TODO: implement attempting to find the IP address here, from rscminus
      if (ipAddressMetadata.length == 4) { // ipv4, need padding
        metadata.writeInt(0);
        metadata.writeInt(0);
        metadata.writeInt(0xFFFF);
      }
      for (int i = 0; i < ipAddressMetadata.length; i++) {
        metadata.writeByte(ipAddressMetadata[i]);
      }
      metadata.writeByte(0); // conversion settings, none used in this case
      metadata.writeInt(
          0); // "User settings", 1st bit is F2P or Members. Since metadata.bin doesn't exist,
      // probably members
      metadata.flush();
      metadata.close();
    } catch (IOException e) {
      Logger.Error("Couldn't write metadata.bin!");
    }
  }

  public static Object[] readMetadata(String replayFolder) {
    int replayLength = -1;
    long dateModified = -1;
    String world = "Unknown";
    byte conversionSettings = (byte) 128;
    int userField = 0;

    File metadataFile = new File(replayFolder + "/metadata.bin");
    if (metadataFile != null && metadataFile.exists()) {
      try {
        DataInputStream metadata =
            new DataInputStream(new BufferedInputStream(new FileInputStream(metadataFile)));
        replayLength = metadata.readInt();
        dateModified = metadata.readLong();
        if (metadataFile.length() > 12) {
          int ipAddress1 = metadata.readInt();
          int ipAddress2 = metadata.readInt();
          int ipAddress3 = metadata.readInt();
          int ipAddress4 = metadata.readInt();

          if (ipAddress1 == 0 && ipAddress2 == 0 && ipAddress3 == 0xFFFF) { // true if ipv4
            switch (ipAddress4) {
              case -643615488: // Unknown Jagex World IP: 217.163.53.0
                world = "Jagex";
                break;
              case -643615310: // World 1: IP address 217.163.53.178
                world = "Jagex 1";
                break;
              case -643615309: // World 2: IP address 217.163.53.179
                world = "Jagex 2";
                break;
              case -643615308: // World 3: IP address 217.163.53.180
                world = "Jagex 3";
                break;
              case -643615307: // World 4: IP address 217.163.53.181
                world = "Jagex 4";
                break;
              case -643615306: // World 5: IP address 217.163.53.182
                world = "Jagex 5";
                break;
              default:
                world =
                    String.format(
                        "%d.%d.%d.%d",
                        (ipAddress4 >> 24) & 0xFF,
                        (ipAddress4 >> 16) & 0xFF,
                        (ipAddress4 >> 8) & 0xFF,
                        (ipAddress4) & 0xFF);
                break;
            }
          } else { // ipv6
            // TODO: this is not a properly formatted ipv6 address
            world =
                String.format("ipv6: %d:%d:%d:%d", ipAddress1, ipAddress2, ipAddress3, ipAddress4);
          }

          conversionSettings = metadata.readByte();
          userField = metadata.readInt();
        }
        metadata.close();
      } catch (IOException e) {
        Logger.Error("Couldn't read metadata.bin!");
      }
    }
    return new Object[] {replayLength, dateModified, world, conversionSettings, userField};
  }

  public static void resetFrameTimeSlice() {
    if (isSeeking) return;

    frame_time_slice = 1000 / fps;
  }

  // adjusts frame time slice
  public static int getFrameTimeSlice() {
    return frame_time_slice;
  }

  // Returns video elapsed time in millis
  public static int elapsedTimeMillis() {
    int time_slice = 1000 / fps;
    return timestamp * time_slice;
  }

  // Returns video length in millis
  public static int endTimeMillis() {
    if (replayServer == null) return 0;

    int time_slice = 1000 / fps;
    return replayServer.timestamp_end * time_slice;
  }

  public static void updateFrameTimeSlice() {
    if (paused || isSeeking) return;

    if (isPlaying) {
      frame_time_slice = 1000 / ((int) (fps * fpsPlayMultiplier));
      return;
    }

    frame_time_slice = 1000 / fps;
  }

  public static int getFPS() {
    if (isPlaying) {
      return (int) (fps * fpsPlayMultiplier);
    }

    return fps;
  }

  public static int getReplayEnd() {
    if (replayServer == null) return 0;

    return replayServer.timestamp_end;
  }

  public static int getClientRead() {
    if (replayServer == null) return 0;

    return replayServer.client_read;
  }

  public static int getClientWrite() {
    if (replayServer == null) return 0;

    return replayServer.client_write;
  }

  // only change port in replay
  public static void changePort(int newPort) {
    if (isPlaying) {
      Logger.Info("Replay: Changing port to " + newPort);
      connection_port = newPort;
    }
  }

  public static void resetPort() {
    connection_port = Replay.DEFAULT_PORT;
  }

  public static String lastAction = null;

  public static void processPlaybackAction() {
    String action = lastAction;
    if (action == null) return;

    lastAction = null;

    if (isPlaying) {
      switch (action) {
        case "stop":
          closeReplayPlayback();
          break;
        case "restart":
          if (!replayServer.isSeeking) {
            restartReplayPlayback();
          }
          break;
        case "pause":
          togglePause();
          Client.displayMessage(
              paused ? "Playback paused." : "Playback unpaused.", Client.CHAT_QUEST);
          break;
        case "ff_plus":
          if (fpsPlayMultiplier < 1.0f) {
            fpsPlayMultiplier += 0.25f;
          } else if (fpsPlayMultiplier < 20.0f) {
            fpsPlayMultiplier += 1.0f;
          }
          updateFrameTimeSlice();
          Client.displayMessage(
              "Playback speed set to "
                  + new DecimalFormat("##.##").format(fpsPlayMultiplier)
                  + "x.",
              Client.CHAT_QUEST);
          break;
        case "ff_minus":
          if (fpsPlayMultiplier > 1.0f) {
            fpsPlayMultiplier -= 1.0f;
          } else if (fpsPlayMultiplier > 0.25f) {
            fpsPlayMultiplier -= 0.25f;
          }
          updateFrameTimeSlice();
          Client.displayMessage(
              "Playback speed set to "
                  + new DecimalFormat("##.##").format(fpsPlayMultiplier)
                  + "x.",
              Client.CHAT_QUEST);
          break;
        case "ff_reset":
          fpsPlayMultiplier = 1.0f;
          updateFrameTimeSlice();
          Client.displayMessage("Playback speed reset to 1x.", Client.CHAT_QUEST);
          break;
        case "prev":
          ReplayQueue.skipped = true;
          ReplayQueue.previousReplay();
          break;
        case "next":
          ReplayQueue.skipped = true;
          ReplayQueue.nextReplay();
          break;
        default:
          Logger.Error("An unrecognized command was sent to controlPlayback: " + action);
          break;
      }
    }
  }

  public static void controlPlayback(String action) {
    lastAction = action;
  }

  public static void shutdown_error() {
    closeReplayPlayback();
    closeReplayRecording();
    if (Client.state == Client.STATE_GAME) {
      Client.displayMessage("Recording has been stopped because of an error", Client.CHAT_QUEST);
      Client.displayMessage("Please log back in to start recording again", Client.CHAT_QUEST);
    }
  }

  public static void dumpKeyboardInput(int keycode, byte event, char keychar, int modifier) {
    if (keyboard == null) return;

    try {
      keyboard.writeInt(timestamp);
      keyboard.writeByte(event);
      keyboard.writeChar(keychar);
      keyboard.writeInt(keycode);
      keyboard.writeInt(modifier);
      keyboard.flush();
    } catch (Exception e) {
      e.printStackTrace();
      shutdown_error();
    }
  }

  public static void dumpMouseInput(
      byte event,
      int x,
      int y,
      int rotation,
      int modifier,
      int clickCount,
      int scrollType,
      int scrollAmount,
      boolean popupTrigger,
      int button) {
    if (mouse == null) return;

    try {
      mouse.writeInt(timestamp);
      mouse.writeByte(event);
      mouse.writeInt(x);
      mouse.writeInt(y);
      mouse.writeInt(rotation);
      mouse.writeInt(modifier);
      mouse.writeInt(clickCount);
      mouse.writeInt(scrollType);
      mouse.writeInt(scrollAmount);
      mouse.writeBoolean(popupTrigger);
      mouse.writeInt(button);
      mouse.flush();
    } catch (Exception e) {
      e.printStackTrace();
      shutdown_error();
    }
  }

  public static void dumpRawInputStream(byte[] b, int n, int n2, int n5, int bytesread) {
    Client.lastIncomingBytes = b.clone();

    // Save timestamp of last time we saw data from the server
    if (bytesread > 0) {
      int lag = timestamp - timestamp_server_last;
      if (lag > 10) timestamp_lag = lag;
      timestamp_server_last = timestamp;
      if (replayServer != null) replayServer.client_read += bytesread;
    }

    if (input == null) return;

    int off = n2 + n5;
    // when packet 182 is received retained_timestamp should be TIMESTAMP_EOF
    // to indicate not to dump previous packet
    if (retained_timestamp != TIMESTAMP_EOF) {
      // new set of packets arrived, dump previous ones
      try {
        // Handle disconnection
        if (timestamp_disconnect != TIMESTAMP_EOF && retained_timestamp >= timestamp_disconnect) {
          ByteBuffer buffer = ByteBuffer.allocate(8);
          buffer.putInt(timestamp_disconnect);
          buffer.putInt(-1);
          input_checksum.update(buffer.array());
          input.write(buffer.array());
          input.flush();
          timestamp_disconnect = TIMESTAMP_EOF;
        }

        ByteBuffer buffer = ByteBuffer.allocate(retained_bread + 8);
        buffer.putInt(retained_timestamp);
        buffer.putInt(retained_bread);
        buffer.put(retained_bytes, retained_off, retained_bread);
        input_checksum.update(buffer.array());
        input.write(buffer.array());

        /* Debug viewing entire input stream
        System.out.print("Writing Input Stream: ");
        for (byte h : buffer.array()) {
            System.out.print(String.format("%d ",  Byte.toUnsignedInt(h)));
        }
        System.out.println();
        */

        input.flush();
      } catch (Exception e) {
        e.printStackTrace();
        shutdown_error();
      }
    }
    retained_timestamp = timestamp;
    // Important! Cloned since it gets modified by decryption in game logic
    retained_bytes = b.clone();
    retained_off = off;
    retained_bread = bytesread;
  }

  public static void dumpRawOutputStream(byte[] b, int off, int len) {
    if (output == null) return;

    try {
      boolean isLogin = false;
      int pos = -1;
      byte[] out_b = null;
      // for the first bytes if byte == (byte)Client.version, 4 bytes before indicate if its
      // login or reconnect and 5 its what determines if its login-related
      for (int i = off + 5; i < off + Math.min(15, len); i++) {
        if (b[i] == (byte) Client.version && b[i - 5] == 0 && (b[i - 4] == 0 || b[i - 4] == 1)) {
          isLogin = true;
          pos = i + 1;
          out_b = b.clone();
          break;
        }
      }
      if (isLogin && pos != -1) {
        for (int i = pos; i < off + len; i++) {
          out_b[i] = 0x00;
        }

        Logger.Info("Replay: Removed login block from client output");

        ByteBuffer buffer = ByteBuffer.allocate(len + 8);
        buffer.putInt(timestamp);
        buffer.putInt(len);
        buffer.put(out_b, off, len);
        output_checksum.update(buffer.array());
        output.write(buffer.array());
        output.flush();

        /*
         // Debug viewing entire output stream
        System.out.print("Writing Output Stream: ");
        for (byte h : buffer.array()) {
            System.out.print(String.format("%d ",  Byte.toUnsignedInt(h)));
        }
        System.out.println();
        */

        return;
      }

      ByteBuffer buffer = ByteBuffer.allocate(len + 8);
      buffer.putInt(timestamp);
      buffer.putInt(len);
      buffer.put(b, off, len);
      output_checksum.update(buffer.array());
      output.write(buffer.array());
      output.flush();

      /*
      // Debug viewing entire output stream
      System.out.print("Writing Output Stream: ");
      for (byte h : buffer.array()) {
        System.out.print(String.format("%d ",  Byte.toUnsignedInt(h)));
      }
      System.out.println();
      */

    } catch (Exception e) {
      e.printStackTrace();
      shutdown_error();
    }
  }

  public static int hookXTEAKey(int key) {
    if (replayServer != null) {
      int serverXTEAKey = replayServer.getXTEAKey();
      if (serverXTEAKey != 0) return serverXTEAKey;
    }

    if (play_keys != null) {
      try {
        return play_keys.readInt();
      } catch (Exception e) {
        // e.printStackTrace();
        shutdown_error();
        return key;
      }
    }

    if (keys == null) return key;

    try {
      Logger.Debug(String.format("Writing XTEA key: %d", key));
      keys.writeInt(key); // data length
      keys.flush();
    } catch (Exception e) {
      // e.printStackTrace();
      shutdown_error();
    }

    return key;
  }

  public static void disconnect_hook() {
    timestamp_disconnect = timestamp;
  }

  // Not sure that enc_opcode ever gets used & this might be an unused function
  public static void saveEncOpcode(int inopcode) {
    if (isRecording) {
      // Logger.Debug(String.format("Encrypted Opcode: %d", inopcode)); // Debug Info
      enc_opcode = inopcode;
    }
  }

  public static void checkPoint(int opcode, int len) {
    // SERVER_OPCODE_SHOW_WELCOME
    if (opcode == 182) {

      // getting opcode 182, we can tell that player very recently managed to log in successfully.
      Client.allTheWayLoggedIn();

      // received packet 182 while recording, set flag, do not dump bytes
      if (input == null) return;
      if (isRecording) {
        // in here probably would need to check the position
        // don't care about the packet if 182, just rewrite it using the enc opcode
        try {
          ByteBuffer buffer = ByteBuffer.allocate(retained_bread + 8);
          buffer.putInt(retained_timestamp);
          buffer.putInt(retained_bread);
          retained_bytes[retained_off + 1] = (byte) 127;
          retained_bytes[retained_off + 2] = 0;
          retained_bytes[retained_off + 3] = 0;
          retained_bytes[retained_off + 4] = 1;
          buffer.put(retained_bytes, retained_off, retained_bread);
          input_checksum.update(buffer.array());
          input.write(buffer.array());
          input.flush();
          Logger.Debug("Replay: Removed host block from client input");
        } catch (Exception e) {
          e.printStackTrace();
          shutdown_error();
        }
        retained_timestamp = TIMESTAMP_EOF;
        // free memory
        retained_bytes = null;
      }
    } else if (opcode == 99) {
      Item.checkForNewItems(len);
    } else if (opcode == 191) {
      Item.checkForImminentlyDespawningCoolItem();
    }

    if (!isPlaying && !isSeeking) {
      // SERVER_OPCODE_PLAYER_UPDATE
      if (opcode == 234) {
        // Timing should only begin once the player actually exists in the world.
        // The first time they get a player update packet, we will consider them fully in the world.
        // This allows time for character creation on tutorial island without counting against
        // speedrun time.
        Speedrun.checkAndBeginSpeedrun();

        if (!Client.knowWhoIAm) {
          Client.knowWhoIAm = true;
          Client.resetFatigueXPDrops(true);
        }
      }
      // SERVER_OPCODE_PLAYER_COORDS
      if (opcode == 191) {
        Speedrun.incrementTicks();
        Speedrun.checkCoordinateCompletions();
      }
    } else {
      // in a replay, just make sure knowWhoIAm is set
      if (opcode == 234) {
        if (!Client.knowWhoIAm) {
          Client.knowWhoIAm = true;
          Client.resetFatigueXPDrops(true);
        }
      }
    }
  }

  public static void patchClient() {
    // This is called from the client to apply fixes specific to replay
    // We only run this while playing replays
    if (!isPlaying) return;

    // The client doesn't remove friends during replay because they're removed client-side
    // Instead, lets increase the array size so we can still see added friends and not crash the
    // client
    if (Client.friends_count == Client.friends.length) {
      int newLength = Client.friends.length + 200;
      Client.friends = Arrays.copyOf(Client.friends, newLength);
      Client.friends_world = Arrays.copyOf(Client.friends_world, newLength);
      Client.friends_formerly = Arrays.copyOf(Client.friends_formerly, newLength);
      Client.friends_online = Arrays.copyOf(Client.friends_online, newLength);
      Logger.Debug(
          "Replay.patchClient(): Applied friends list length patch to fix playback; newLength: "
              + newLength);
    }

    // The client doesn't remove ignores during replay because they're removed client-side
    // Instead, lets increase the array size so we can still see added ignores and not crash the
    // client
    if (Client.ignores_count == Client.ignores.length) {
      int newLength = Client.ignores.length + 100;
      Client.ignores = Arrays.copyOf(Client.ignores, newLength);
      Client.ignores_formerly = Arrays.copyOf(Client.ignores_formerly, newLength);
      Client.ignores_copy = Arrays.copyOf(Client.ignores_copy, newLength);
      Client.ignores_formerly_copy = Arrays.copyOf(Client.ignores_formerly_copy, newLength);
      Logger.Debug(
          "Replay.patchClient(): Applied ignores list length patch to fix playback; newLength: "
              + newLength);
    }
  }

  public static void resetPatchClient() {
    // Resets all replay patching
    Client.friends_count = 0;
    Client.friends = new String[200];
    Client.friends_world = new String[200];
    Client.friends_formerly = new String[200];
    Client.friends_online = new int[200];

    Client.ignores_count = 0;
    Client.ignores = new String[100];
    Client.ignores_formerly = new String[100];
    Client.ignores_copy = new String[100];
    Client.ignores_formerly_copy = new String[100];
  }
}
