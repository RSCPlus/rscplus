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

import Replay.common.MathUtil;
import Replay.scraper.client.Class11;

public class ReplayPacket {
  public int timestamp;
  public int opcode;
  public byte[] data;

  private static Class11 stringDecrypter =
      new Class11(
          new byte[] {
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 21, (byte) 22,
            (byte) 22, (byte) 20, (byte) 22, (byte) 22, (byte) 22, (byte) 21, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 3, (byte) 8, (byte) 22, (byte) 16, (byte) 22, (byte) 16, (byte) 17, (byte) 7,
            (byte) 13, (byte) 13, (byte) 13, (byte) 16, (byte) 7, (byte) 10, (byte) 6, (byte) 16,
            (byte) 10, (byte) 11, (byte) 12, (byte) 12, (byte) 12, (byte) 12, (byte) 13, (byte) 13,
            (byte) 14, (byte) 14, (byte) 11, (byte) 14, (byte) 19, (byte) 15, (byte) 17, (byte) 8,
            (byte) 11, (byte) 9, (byte) 10, (byte) 10, (byte) 10, (byte) 10, (byte) 11, (byte) 10,
            (byte) 9, (byte) 7, (byte) 12, (byte) 11, (byte) 10, (byte) 10, (byte) 9, (byte) 10,
            (byte) 10, (byte) 12, (byte) 10, (byte) 9, (byte) 8, (byte) 12, (byte) 12, (byte) 9,
            (byte) 14, (byte) 8, (byte) 12, (byte) 17, (byte) 16, (byte) 17, (byte) 22, (byte) 13,
            (byte) 21, (byte) 4, (byte) 7, (byte) 6, (byte) 5, (byte) 3, (byte) 6, (byte) 6,
            (byte) 5, (byte) 4, (byte) 10, (byte) 7, (byte) 5, (byte) 6, (byte) 4, (byte) 4,
            (byte) 6, (byte) 10, (byte) 5, (byte) 4, (byte) 4, (byte) 5, (byte) 7, (byte) 6,
            (byte) 10, (byte) 6, (byte) 10, (byte) 22, (byte) 19, (byte) 22, (byte) 14, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22,
            (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 21,
            (byte) 22, (byte) 21, (byte) 22, (byte) 22, (byte) 22, (byte) 21, (byte) 22, (byte) 22
          });
  private int m_position;
  private int m_bitmaskPosition;

  ReplayPacket() {
    m_position = 0;
    m_bitmaskPosition = 0;
  }

  public void startBitmask() {
    m_bitmaskPosition = m_position << 3;
  }

  public void endBitmask() {
    m_position = (m_bitmaskPosition + 7) >> 3;
  }

  public int readBitmask(int size) {
    int start = m_bitmaskPosition >> 3;
    int bitEnd = m_bitmaskPosition + size;
    int byteSize = ((bitEnd + 7) >> 3) - start;
    int offset = ((start + byteSize) << 3) - bitEnd;
    int bitmask = MathUtil.getBitmask(size);

    int ret = 0;
    for (int i = 0; i < byteSize; i++) {
      int dataOffset = start + (byteSize - i - 1);
      ret |= (data[dataOffset] & 0xFF) << (i << 3);
    }

    m_bitmaskPosition += size;
    return (ret >> offset) & bitmask;
  }

  public int tell() {
    return m_position;
  }

  public int tellBitmask() {
    return m_bitmaskPosition;
  }

  public void seek(int position) {
    m_position = position;
  }

  public void skip(int size) {
    m_position += size;
  }

  public void trim(int count) {
    int size = data.length - count;
    byte[] newData = new byte[size];
    System.arraycopy(data, 0, newData, 0, m_position);
    System.arraycopy(
        data, m_position + count, newData, m_position, data.length - m_position - count);
    data = newData;
  }

  public String readPaddedString() {
    skip(1);
    return readString();
  }

  public String readRSCString() {
    int length = readUnsignedByte();
    if (length >= 128) {
      m_position--;
      length = readUnsignedShort() - 32768;
    }
    byte[] byteData = new byte[length];
    int count = stringDecrypter.method240(data, 0, byteData, true, m_position, length);
    skip(count);
    return new String(byteData, 0, length);
  }

  public String readString() {
    int length = 0;
    while (data[m_position + length] != '\0') length++;
    String ret;
    ret = new String(data, m_position, length);
    m_position += length + 1;
    return ret;
  }

  public int readUnsignedInt() {
    return (readUnsignedByte() << 24)
        | (readUnsignedByte() << 16)
        | (readUnsignedByte() << 8)
        | readUnsignedByte();
  }

  public int readUnsignedShort() {
    return (readUnsignedByte() << 8) | readUnsignedByte();
  }

  public int readUnsignedShortLE() {
    int a = readUnsignedByte();
    int b = readUnsignedByte() << 8;
    return b | a;
  }

  public byte readByte() {
    return data[m_position++];
  }

  public int readUnsignedByte() {
    return readByte() & 0xFF;
  }

  public void writeUnsignedByte(int value) {
    data[m_position++] = (byte) (value & 0xFF);
  }

  public void writeUnsignedShort(int value) {
    writeUnsignedByte(value >> 8);
    writeUnsignedByte(value);
  }
}
