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
import java.nio.file.Files;

public class FileUtil {
  public static void mkdir(String path) {
    new File(path).mkdirs();
  }

  public static boolean writeFull(String fname, byte[] data) {
    try {
      DataOutputStream os = new DataOutputStream(new FileOutputStream(fname));
      os.write(data);
      os.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static byte[] readFull(File f) {
    try {
      return Files.readAllBytes(f.toPath());
    } catch (Exception e) {
      return null;
    }
  }
}
