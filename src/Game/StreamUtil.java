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

import java.math.BigInteger;
import java.net.Socket;

/** Util for Client Stream manipulation */
public class StreamUtil {

  public static void initializeStream(String address, int port) {
    if (Reflection.createSocket == null
        || Reflection.stream == null
        || Reflection.clientStreamField == null) return;

    try {
      Socket connectionSocket =
          (Socket) Reflection.createSocket.invoke(Client.instance, -12, port, address);
      Reflection.clientStreamField.set(
          Client.instance, Reflection.stream.newInstance(connectionSocket, Client.instance));
      Client.clientStream = Reflection.clientStreamField.get(Client.instance);
    } catch (Exception e) {
    }
  }

  public static void newPacket(int id) {
    if (Reflection.newPacket == null) return;

    try {
      Reflection.newPacket.invoke(Client.clientStream, id, -12 ^ -12);
    } catch (Exception e) {
    }
  }

  public static void flushPacket() {
    if (Reflection.flushPacket == null) return;

    try {
      Reflection.flushPacket.invoke(Client.clientStream, -6924);
    } catch (Exception e) {
    }
  }

  public static void sendPacket() {
    if (Reflection.sendPacket == null) return;

    try {
      Reflection.sendPacket.invoke(Client.clientStream, 21294);
    } catch (Exception e) {
    }
  }

  public static void initIsaac(int[] keys) {
    if (Reflection.initIsaac == null) return;

    try {
      Reflection.initIsaac.invoke(Client.clientStream, (byte) -119, keys);
    } catch (Exception e) {
    }
  }

  public static Object getStreamBuffer() {
    Object buffer = null;
    if (Reflection.bufferField == null) return null;

    try {
      buffer = Reflection.bufferField.get(Client.clientStream);
    } catch (Exception e) {
    }

    return buffer;
  }

  public static Object getNewBuffer(int capacity) {
    Object buffer = null;
    if (Reflection.buffer == null) return null;

    try {
      buffer = Reflection.buffer.newInstance(capacity);
    } catch (Exception e) {
    }

    return buffer;
  }

  public static void putRandom(Object buffer) {
    if (Reflection.putRandom == null) return;

    try {
      Reflection.putRandom.invoke(null, (int) 22607, buffer);
    } catch (Exception e) {
    }
  }

  public static int getBufferOffset(Object buffer) {
    if (Reflection.bufferOffset == null) return 0;
    int bufferOffset = 0;

    try {
      bufferOffset = (int) Reflection.bufferOffset.get(buffer);
    } catch (Exception e) {
    }

    return bufferOffset;
  }

  public static byte[] getBufferByteArray(Object buffer) {
    if (Reflection.bufferByteArray == null) return null;
    byte[] bufferArray = null;

    try {
      bufferArray = (byte[]) Reflection.bufferByteArray.get(buffer);
    } catch (Exception e) {
    }

    return bufferArray;
  }

  public static void setStreamMaxRetries(int maxRetries) {
    if (Reflection.maxRetriesField == null) return;

    try {
      Reflection.maxRetriesField.set(Client.clientStream, maxRetries);
    } catch (Exception e) {
    }
  }

  public static void setBufferLength(Object buffer, int xtea_start) {
    if (Reflection.setBlockLength == null) return;

    try {
      Reflection.setBlockLength.invoke(buffer, StreamUtil.getBufferOffset(buffer) - xtea_start, 1);
    } catch (Exception e) {
    }
  }

  public static int readStream() {
    if (Reflection.readResponse == null) return -1;
    int response = -1;

    try {
      response = (int) Reflection.readResponse.invoke(Client.clientStream, true);
    } catch (Exception e) {
    }

    return response;
  }

  public static int readByte() {
    if (Reflection.readResponse == null) return -1;
    return readStream();
  }

  public static int readShort() {
    if (Reflection.readResponse == null) return -1;
    int i = readByte();
    int j = readByte();
    return i * 256 + j;
  }

  public static int readInt() {
    if (Reflection.readResponse == null) return -1;
    int i = readShort();
    int j = readShort();
    return i * 65536 + j;
  }

  public static void readBytes(byte[] byteArr, int length) {
    readBytes(byteArr, 0, length);
  }

  public static void readBytes(byte[] byteArr, int offset, int length) {
    if (Reflection.readBytes == null) return;
    int response = -1;

    try {
      Reflection.readBytes.invoke(Client.clientStream, byteArr, length, offset, 123);
    } catch (Exception e) {
    }
  }

  public static void putBytesTo(Object buffer, byte[] block, int start, int offset) {
    if (Reflection.putBytes == null) return;

    try {
      Reflection.putBytes.invoke(buffer, start, -123, offset, block);
    } catch (Exception e) {
    }
  }

  public static void putByteTo(Object buffer, byte n) {
    if (Reflection.putByte == null) return;

    try {
      Reflection.putByte.invoke(buffer, (int) n, -117);
    } catch (Exception e) {
    }
  }

  public static void putShortTo(Object buffer, short s) {
    if (Reflection.putShort == null) return;

    try {
      Reflection.putShort.invoke(buffer, 393, (int) s);
    } catch (Exception e) {
    }
  }

  public static void putIntTo(Object buffer, int n) {
    if (Reflection.putInt == null) return;

    try {
      Reflection.putInt.invoke(buffer, -422797528, n);
    } catch (Exception e) {
    }
  }

  public static void putInt3ByteTo(Object buffer, int n) {
    if (Reflection.putInt3Byte == null) return;

    try {
      Reflection.putInt3Byte.invoke(buffer, n, (byte) -13);
    } catch (Exception e) {
    }
  }

  public static void putLongTo(Object buffer, long l) {
    putIntTo(buffer, (int) (l >> 32));
    putIntTo(buffer, (int) (l & -1L));
  }

  /**
   * Put a string terminated by '\0'
   *
   * @param buffer
   * @param st
   */
  public static void putStrTo(Object buffer, String st) {
    if (Reflection.putStr == null) return;

    try {
      Reflection.putStr.invoke(buffer, (byte) -39, st);
    } catch (Exception e) {
    }
  }

  /**
   * Put a regular string and not terminate it with '\0'
   *
   * @param buffer
   * @param st
   */
  public static void putRegStrTo(Object buffer, String st) {
    byte[] stringArray = st.getBytes();
    putBytesTo(buffer, stringArray, 0, stringArray.length);
  }

  public static void encrypt(Object buffer, BigInteger exponent, BigInteger modulus) {
    if (Reflection.encrypt == null) return;

    try {
      Reflection.encrypt.invoke(buffer, modulus, -118, exponent);
    } catch (Exception e) {
    }
  }

  public static void xteaEncrypt(Object buffer, int xtea_start, int[] keys) {
    if (Reflection.xteaEncrypt == null) return;

    try {
      Reflection.xteaEncrypt.invoke(
          buffer, (byte) 87, xtea_start, keys, StreamUtil.getBufferOffset(buffer));
    } catch (Exception e) {
    }
  }
}
