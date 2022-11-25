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

import Client.Launcher;
import Client.Logger;
import java.net.URL;

public class GameApplet {

  public static byte[][] gameFonts;

  public static URL cacheURLHook(URL url) {
    String file = url.getFile();
    if (file.startsWith("/contentcrcs")) {
      file = "/contentcrcs";
    }

    URL urlFile = Launcher.getResource("/assets/content" + file);
    if (urlFile != null) return urlFile;

    return url;
  }

  public static void loadFontHook(byte[] dataFile) {
    try {
      String jagexFileExtension = ".jf";

      String h11pFile = Renderer.shellStrings[29] + jagexFileExtension;
      String h12bFile = Renderer.shellStrings[39] + jagexFileExtension;
      String h12pFile = Renderer.shellStrings[31] + jagexFileExtension;
      String h13bFile = Renderer.shellStrings[36] + jagexFileExtension;
      String h14bFile = Renderer.shellStrings[38] + jagexFileExtension;
      String h16bFile = Renderer.shellStrings[34] + jagexFileExtension;
      String h20bFile = Renderer.shellStrings[35] + jagexFileExtension;
      String h24bFile = Renderer.shellStrings[37] + jagexFileExtension;

      byte[] h11p = (byte[]) Reflection.loadData.invoke(null, h11pFile, 0, dataFile, -120);
      gameFonts[0] = h11p;

      byte[] h12b = (byte[]) Reflection.loadData.invoke(null, h12bFile, 0, dataFile, -120);
      gameFonts[1] = h12b;

      byte[] h12p = (byte[]) Reflection.loadData.invoke(null, h12pFile, 0, dataFile, -120);
      gameFonts[2] = h12p;

      byte[] h13b = (byte[]) Reflection.loadData.invoke(null, h13bFile, 0, dataFile, -120);
      gameFonts[3] = h13b;

      byte[] h14b = (byte[]) Reflection.loadData.invoke(null, h14bFile, 0, dataFile, -120);
      gameFonts[4] = h14b;

      byte[] h16b = (byte[]) Reflection.loadData.invoke(null, h16bFile, 0, dataFile, -120);
      gameFonts[5] = h16b;

      byte[] h20b = (byte[]) Reflection.loadData.invoke(null, h20bFile, 0, dataFile, -120);
      gameFonts[6] = h20b;

      byte[] h24b = (byte[]) Reflection.loadData.invoke(null, h24bFile, 0, dataFile, -120);
      gameFonts[7] = h24b;
    } catch (Exception e) {
      Logger.Error("Error loading jagex font files");
    }
  }
}
