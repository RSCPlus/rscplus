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
package Replay.common;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class JContentFile {
  private byte m_data[];
  private int m_position;

  public JContentFile(byte data[]) {
    m_data = data;
    m_position = 0;
  }

  public void skip(int amount) {
    m_position += amount;
  }

  public byte readByte() {
    return m_data[m_position++];
  }

  public int readUnsignedByte() {
    return m_data[m_position++] & 0xFF;
  }

  public int readUnsignedShort() {
    return (readUnsignedByte() << 8) | readUnsignedByte();
  }

  public int readUnsignedInt() {
    return (readUnsignedByte() << 24)
        | (readUnsignedByte() << 16)
        | (readUnsignedByte() << 8)
        | readUnsignedByte();
  }

  public String readString() {
    int length = 0;
    while (m_data[m_position + length] != '\0') length++;
    String ret;
    ret = new String(m_data, m_position, length);
    m_position += length + 1;
    return ret;
  }

  public void dump(String fname) {
    File f = new File(fname);
    try {
      DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
      out.write(m_data, 0, m_data.length);
      out.close();
    } catch (Exception e) {
    }
  }

  public void close() {
    m_data = null;
  }
}
