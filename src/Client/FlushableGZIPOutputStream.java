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
package Client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class FlushableGZIPOutputStream extends GZIPOutputStream {
  public FlushableGZIPOutputStream(OutputStream out) throws IOException {
    super(out);
  }

  @Override
  protected void deflate() throws IOException {
    // Use SYNC_FLUSH to sync immediately
    int len = def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH);
    if (len > 0) out.write(buf, 0, len);
  }
}
