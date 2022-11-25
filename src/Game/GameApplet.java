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

import java.io.IOException;
import java.io.InputStream;
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

  public static void loadFontHook() {
    try {
      InputStream h11pInputStream = Launcher.getResourceAsStream("/assets/jf/h11p.jf");
      InputStream h12bInputStream = Launcher.getResourceAsStream("/assets/jf/h12b.jf");
      InputStream h12pInputStream = Launcher.getResourceAsStream("/assets/jf/h12p.jf");
      InputStream h13bInputStream = Launcher.getResourceAsStream("/assets/jf/h13b.jf");
      InputStream h14bInputStream = Launcher.getResourceAsStream("/assets/jf/h14b.jf");
      InputStream h16bInputStream = Launcher.getResourceAsStream("/assets/jf/h16b.jf");
      InputStream h20bInputStream = Launcher.getResourceAsStream("/assets/jf/h20b.jf");
      InputStream h24bInputStream = Launcher.getResourceAsStream("/assets/jf/h24b.jf");

      byte[] h11p = new byte[h11pInputStream.available()];
      h11pInputStream.read(h11p);
      gameFonts[0] = h11p;

      byte[] h12b = new byte[h12bInputStream.available()];
      h12bInputStream.read(h12b);
      gameFonts[1] = h12b;

      byte[] h12p = new byte[h12pInputStream.available()];
      h12pInputStream.read(h12p);
      gameFonts[2] = h12p;

      byte[] h13b = new byte[h13bInputStream.available()];
      h13bInputStream.read(h13b);
      gameFonts[3] = h13b;

      byte[] h14b = new byte[h14bInputStream.available()];
      h14bInputStream.read(h14b);
      gameFonts[4] = h14b;

      byte[] h16b = new byte[h16bInputStream.available()];
      h16bInputStream.read(h16b);
      gameFonts[5] = h16b;

      byte[] h20b = new byte[h20bInputStream.available()];
      h20bInputStream.read(h20b);
      gameFonts[6] = h20b;

      byte[] h24b = new byte[h24bInputStream.available()];
      h24bInputStream.read(h24b);
      gameFonts[7] = h24b;
    } catch (IOException e) {
      Logger.Error("Error loading jagex font files");
      throw new RuntimeException(e);
    }
  }
}
