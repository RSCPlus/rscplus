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

import java.io.ByteArrayInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class BZip2 {
  public static byte[] decompress(byte data[], int offset, int length, int uncompressedLength) {
    byte uncompressedData[] = new byte[uncompressedLength];
    try {
      BZip2CompressorInputStream in =
          new BZip2CompressorInputStream(new ByteArrayInputStream(data, 2, length + 4));
      in.read(uncompressedData);
      in.close();
    } catch (Exception e) {
      return null;
    }
    return uncompressedData;
  }
}
