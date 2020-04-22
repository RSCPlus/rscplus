/**
 * rscminus
 *
 * <p>This file is part of rscminus.
 *
 * <p>rscminus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscminus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscminus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/RSCPlus/rscminus>
 */
package Replay.scraper;

import Replay.common.ISAACCipher;
import Replay.game.PacketBuilder;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class ReplayReader {
  private byte[] m_data;
  private Queue<Integer> m_timestamps = new LinkedList<Integer>();
  LinkedList<Integer> m_disconnectOffsets = new LinkedList<Integer>();

  private static final byte[] m_inputDisconnectPattern = {
    0x40, 0x05, 0x00, 0x6E, 0x00, 0x00, 0x00, 0x1A, 0x00, 0x6C, 0x03, 0x00, 0x00, 0x57, 0x65, 0x6C,
    0x63
  };

  private static final byte[] m_outputDisconnectPattern = {
    0x00, 0x01, 0x00, 0x00, 0x00, (byte) 0xEB
  };

  // Reader state
  private boolean m_loggedIn;
  private boolean m_forceQuit;
  private boolean m_outgoing;
  private int m_position;
  private LinkedList<ReplayKeyPair> m_keys;
  private int m_keyIndex;
  private ISAACCipher isaac = new ISAACCipher();

  public static final int TIMESTAMP_EOF = -1;

  public byte[] getData() {
    return m_data;
  }

  public int getDataPosition() {
    return m_position;
  }

  public int getDataSize() {
    return m_data.length;
  }

  public boolean open(
      File f,
      ReplayVersion replayVersion,
      ReplayMetadata replayMetadata,
      LinkedList<ReplayKeyPair> keys,
      byte[] fileMetadata,
      byte[] metadata,
      byte[] checksum,
      boolean outgoing)
      throws IOException, NoSuchAlgorithmException {
    int size = calculateSize(f);

    if (size == 0) return false;

    // Calculate checksum
    if (replayVersion.version >= 3) {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      DataInputStream in =
          new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(f))));
      m_data = new byte[calculateRealSize(f)];
      in.read(m_data);

      int length = in.read(fileMetadata);
      if (length < fileMetadata.length) {
        System.arraycopy(fileMetadata, 0, metadata, 0, metadata.length);
        for (int i = 0; i < fileMetadata.length; i++) fileMetadata[i] = 0x00;
      } else {
        in.read(metadata);
      }

      in.close();
      System.arraycopy(messageDigest.digest(m_data), 0, checksum, 0, checksum.length);
    }

    // Allocate space for data without replay headers
    m_data = new byte[size];
    m_outgoing = outgoing;

    // Read replay data
    DataInputStream in =
        new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(f))));
    int timestamp = 0;
    int lastTimestamp = timestamp;
    int offset = 0;
    int lastOffset = offset;
    LinkedHashMap<Integer, Integer> timestamps = new LinkedHashMap<Integer, Integer>();
    while ((timestamp = in.readInt()) != TIMESTAMP_EOF) {
      timestamps.put(offset, timestamp);
      int length = in.readInt();

      // Detect extreme disconnects that may be missed
      // int timestampDiff = timestamp - lastTimestamp;
      // if (timestampDiff >= 500) {
      //    m_disconnectOffsets.add(offset);
      //    System.out.println("WARNING: Using potential extreme disconnect point at offset " +
      // offset);
      //    Sleep.sleep(10000);
      // }

      if (length > 0) {
        in.read(m_data, offset, length);
        offset += length;
      }

      // We have reached the end of readable data
      if (offset >= size) break;

      // Update replay length
      replayMetadata.replayLength = timestamp;

      lastTimestamp = timestamp;
    }

    in.close();

    m_loggedIn = false;
    m_position = 0;
    m_keys = keys;
    m_keyIndex = -1;

    // Build disconnect map for in.bin
    // Our initial recording implementation had problems with this
    if (!m_outgoing) {
      // Skip first login
      m_position = 1;
      while (!isEOF()) {
        if (loginBinarySearch()) {
          m_disconnectOffsets.add(m_position);
          // System.out.println("Disconnect found at offset " + m_position);
        }
        skip(1);
      }
      m_position = 0;
    }

    // Map timestamps for faster import
    Iterator<Map.Entry<Integer, Integer>> iterator = timestamps.entrySet().iterator();
    Map.Entry<Integer, Integer> entry = iterator.next();
    int timestampOffset = 0;
    while (!isEOF()) {
      // Handle disconnect
      if (m_disconnectOffsets.contains(m_position)) {
        m_loggedIn = false;
      } else if (m_disconnectOffsets.contains(m_position + 1)) {
        m_loggedIn = false;
        m_position++;
      } else if (m_disconnectOffsets.contains(m_position + 2)) {
        m_loggedIn = false;
        m_position += 2;
      }

      if (m_loggedIn || m_outgoing) {
        int length = readPacketLength();
        skip(length);
        timestampOffset = m_position - 1;
      } else {
        timestampOffset = m_position;
        skip(1);
        m_loggedIn = true;
      }

      while (timestampOffset >= entry.getKey()) {
        timestamp = entry.getValue();
        if (!iterator.hasNext()) {
          break;
        }
        entry = iterator.next();
      }

      // Add timestamp to FIFO
      m_timestamps.add(timestamp);
    }
    m_position = 0;
    m_loggedIn = false;

    // Build disconnect map for out.bin because we didn't handle it
    // We detect the login information packet
    if (outgoing) {
      while (!isEOF()) {
        if (binarySearch(m_outputDisconnectPattern)) {
          m_disconnectOffsets.add(m_position);
        }
        skip(1);
      }
      m_position = 0;
    }

    return true;
  }

  private boolean loginBinarySearch() {
    if (m_data.length - m_position < m_inputDisconnectPattern.length) return false;

    for (int i = 0; i < m_inputDisconnectPattern.length; i++) {
      if (i == 0 || i == 3 || i == 9) continue;

      int offset = m_position + i;
      int searchValue = m_inputDisconnectPattern[i];
      if (searchValue == 0x00 && i <= 6) {
        if (!(m_data[offset] == 0x00 || m_data[offset] == 0x01)) return false;
      } else {
        if (m_data[offset] != searchValue) return false;
      }
    }
    return true;
  }

  private boolean binarySearch(byte[] pattern) {
    for (int i = 0; i < pattern.length; i++) {
      int offset = m_position + i;
      if (offset >= m_data.length || m_data[offset] != pattern[i]) return false;
    }
    return true;
  }

  private boolean verifyLogin() {
    boolean success = true;
    int originalPosition = m_position;
    ReplayPacket packet;
    packet = readPacket(true);
    if (packet == null || packet.opcode != PacketBuilder.OPCODE_PRIVACY_SETTINGS) success = false;
    packet = readPacket(true);
    if (packet == null
        || (packet.opcode != PacketBuilder.OPCODE_SEND_MESSAGE
            && packet.opcode != PacketBuilder.OPCODE_SHOW_APPEARANCE_CHANGE)) success = false;
    m_position = originalPosition;
    isaac.reset();
    isaac.setKeys(m_keys.get(m_keyIndex).keys);
    return success;
  }

  public ReplayPacket readPacket(boolean peek) {
    if (isEOF() || m_forceQuit) return null;

    int packetTimestamp;
    if (peek) packetTimestamp = 0; // m_timestamps.peek();
    else packetTimestamp = m_timestamps.poll();

    // Check for disconnect for outgoing (workaround)
    if (m_outgoing) {
      int oldPosition = m_position;
      readPacketLength();
      if (m_disconnectOffsets.contains(m_position)) m_loggedIn = false;
      m_position = oldPosition;
    } else if (!peek) {
      // System.out.println("Checking disconnect at " + m_position);
      // Handle disconnect
      if (m_disconnectOffsets.contains(m_position)) {
        m_loggedIn = false;
      } else if (m_disconnectOffsets.contains(m_position + 1)) {
        // This is safe because no packet can ever be 1 byte long
        m_loggedIn = false;
        m_position++;
      } else if (m_disconnectOffsets.contains(m_position + 2)) {
        // This is unsafe because a 1 byte packet can be skipped, but since we are disconnecting
        // it may not matter much anyway since the packet is likely fragmented from the way
        // the server sends packets.
        m_loggedIn = false;
        m_position += 2;
      }
    }

    try {
      ReplayPacket replayPacket = new ReplayPacket();
      if (!m_loggedIn) {
        if (m_outgoing) {
          int length = readPacketLength();
          if (length > 1) {
            int dataLength = length - 1;
            replayPacket.data = new byte[dataLength];
            if (length < 160) {
              replayPacket.data[dataLength - 1] = readByte();
              replayPacket.opcode = readUnsignedByte();
              if (dataLength > 1) read(replayPacket.data, 0, dataLength - 1);
            } else {
              replayPacket.opcode = readUnsignedByte();
              read(replayPacket.data, 0, dataLength);
            }
          } else {
            replayPacket.data = null;
            replayPacket.opcode = readUnsignedByte();
          }
          replayPacket.timestamp = packetTimestamp;

          if (replayPacket.opcode != 0) {
            System.out.println("ERROR: Invalid outgoing login packet: " + replayPacket.opcode);
            return null;
          }

          // Set isaac keys
          isaac.reset();
          isaac.setKeys(m_keys.get(++m_keyIndex).keys);

          replayPacket.opcode = ReplayEditor.VIRTUAL_OPCODE_CONNECT;

          m_loggedIn = true;
        } else {
          // Handle login response
          int loginResponse = readUnsignedByte();
          int skipKeys = 0;
          if ((loginResponse & 64) != 0) {
            // Find working key
            for (; ; ) {
              // Set isaac keys
              m_keyIndex++;
              if (m_keyIndex >= m_keys.size()) {
                System.out.println("ERROR: Replay is trying to use non-existing keys");
                return null;
              }
              isaac.reset();
              isaac.setKeys(m_keys.get(m_keyIndex).keys);
              m_loggedIn = true;

              boolean success = verifyLogin();
              if (!success) skipKeys++;
              else break;
            }
          } else {
            m_forceQuit = true;
          }

          if (skipKeys > 0) System.out.println("WARNING: Skipping " + skipKeys + " keys");

          // Create virtual connect packet
          replayPacket.opcode = ReplayEditor.VIRTUAL_OPCODE_CONNECT;
          replayPacket.data = new byte[1];
          replayPacket.data[0] = (byte) loginResponse;

          // Set timestamp
          replayPacket.timestamp = packetTimestamp;
        }
      } else {
        try {
          int length = readPacketLength();
          if (length > 1) {
            int dataLength = length - 1;
            replayPacket.data = new byte[dataLength];
            if (length < 160) {
              replayPacket.data[dataLength - 1] = readByte();
              replayPacket.opcode = readUnsignedByte();
              if (dataLength > 1) read(replayPacket.data, 0, dataLength - 1);
            } else {
              replayPacket.opcode = readUnsignedByte();
              read(replayPacket.data, 0, dataLength);
            }
          } else {
            replayPacket.data = null;
            replayPacket.opcode = readUnsignedByte();
          }

          replayPacket.opcode = (replayPacket.opcode - isaac.getNextValue()) & 0xFF;
          replayPacket.timestamp = packetTimestamp;
        } catch (Exception e) {
          System.out.println("WARNING: Invalid packet found, trimming replay");
          return null;
        }
      }
      return replayPacket;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private boolean isEOF() {
    return (m_position >= m_data.length);
  }

  private void read(byte[] data, int offset, int length) {
    int maxLength = Math.min(m_data.length - m_position, length);
    if (maxLength != length) System.out.println("WARNING: Copy is out of bounds");
    System.arraycopy(m_data, m_position, data, offset, length);
    m_position += length;
  }

  private void skip(int size) {
    m_position += size;
  }

  private byte readByte() {
    return m_data[m_position++];
  }

  private int readUnsignedByte() {
    return readByte() & 0xFF;
  }

  public int readPacketLength() {
    int length = readUnsignedByte();
    if (length >= 160) length = 256 * length - (40960 - readUnsignedByte());
    return length;
  }

  private int calculateRealSize(File f) {
    int size = 0;
    try {
      DataInputStream in =
          new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(f))));
      while (in.readInt() != TIMESTAMP_EOF) {
        int length = in.readInt();
        if (length > 0) {
          size += length;
          in.skipBytes(length);
        }
        size += 8;
      }
      in.close();
    } catch (Exception e) {
    }
    size += 4;
    return size;
  }

  private int calculateSize(File f) {
    int size = 0;
    try {
      DataInputStream in =
          new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(f))));
      while (in.readInt() != TIMESTAMP_EOF) {
        int length = in.readInt();
        if (length > 0) {
          size += length;
          in.skipBytes(length);
        }
      }
      in.close();
    } catch (Exception e) {
    }
    return size;
  }
}
