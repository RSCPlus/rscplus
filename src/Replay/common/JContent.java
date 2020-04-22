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

public class JContent {
  private byte m_data[];

  public boolean open(String fname) {
    m_data = FileUtil.readFull(new File(fname));

    if (m_data == null) return false;

    int uncompressedLength =
        ((m_data[0] & 0xFF) << 16) | ((m_data[1] & 0xFF) << 8) | (m_data[2] & 0xFF);
    int compressedLength =
        ((m_data[3] & 0xFF) << 16) | ((m_data[4] & 0xFF) << 8) | (m_data[5] & 0xFF);

    if (uncompressedLength == compressedLength) {
      byte newData[] = new byte[uncompressedLength];
      System.arraycopy(m_data, 6, newData, 0, uncompressedLength);
      m_data = newData;
    } else {
      // Add BZIP2 header to content file before decompressing
      m_data[2] = 0x42;
      m_data[3] = 0x5A;
      m_data[4] = 0x68;
      m_data[5] = 0x31;
      m_data = BZip2.decompress(m_data, 2, compressedLength + 4, uncompressedLength);
      if (m_data == null) return false;
    }

    return true;
  }

  public JContentFile unpack(String filename) {
    int entryCount = ((m_data[0] & 0xFF) << 8) | (m_data[1] & 0xFF);
    filename = filename.toUpperCase();

    int hash = 0;
    for (int i = 0; i < filename.length(); i++) hash = 61 * hash + (filename.charAt(i) - 32);

    int offset = 2 + (10 * entryCount);
    for (int i = 0; i < entryCount; i++) {
      int entryOffset = i * 10;
      int entryHash =
          ((m_data[2 + entryOffset] & 0xFF) << 24)
              | ((m_data[3 + entryOffset] & 0xFF) << 16)
              | ((m_data[4 + entryOffset] & 0xFF) << 8)
              | (m_data[5 + entryOffset] & 0xFF);
      int uncompressedLength =
          ((m_data[6 + entryOffset] & 0xFF) << 16)
              | ((m_data[7 + entryOffset] & 0xFF) << 8)
              | (m_data[8 + entryOffset] & 0xFF);
      int compressedLength =
          ((m_data[9 + entryOffset] & 0xFF) << 16)
              | ((m_data[10 + entryOffset] & 0xFF) << 8)
              | (m_data[11 + entryOffset] & 0xFF);

      if (hash == entryHash) {
        byte data[] = new byte[uncompressedLength];
        if (uncompressedLength == compressedLength) {
          System.arraycopy(m_data, offset, data, 0, uncompressedLength);
        } else {
          // Add BZIP2 header to content file before decompressing
          m_data[8 + entryOffset] = 0x42;
          m_data[9 + entryOffset] = 0x5A;
          m_data[10 + entryOffset] = 0x68;
          m_data[11 + entryOffset] = 0x31;
          data =
              BZip2.decompress(m_data, 8 + entryOffset, compressedLength + 4, uncompressedLength);
          if (data == null) return null;
        }
        return new JContentFile(data);
      }
      offset += compressedLength;
    }

    return null;
  }

  public void close() {
    m_data = null;
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
}
