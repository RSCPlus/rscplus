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
import Client.Settings;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class GameApplet {

  public static int gameFontSize;
  public static byte[][] gameFonts;
  public static boolean[] gameFontStates;
  public static byte[] gameFontData;

  private static byte[] h11p;
  private static byte[] h12b;
  private static byte[] h12p;
  private static byte[] h13b;
  private static byte[] h14b;
  private static byte[] h16b;
  private static byte[] h20b;
  private static byte[] h24b;

  private static boolean systemFontLoaded = true;

  public static URL cacheURLHook(URL url) {
    String file = url.getFile();
    if (file.startsWith("/contentcrcs")) {
      file = "/contentcrcs";
    }

    URL urlFile = Launcher.getResource("/assets/content" + file);
    if (urlFile != null) return urlFile;

    return url;
  }

  public static void loadJagexFonts() {
    resetFontVariables();

    try {
      InputStream h11pInputStream = Launcher.getResourceAsStream("/assets/jf/h11p.jf");
      InputStream h12bInputStream = Launcher.getResourceAsStream("/assets/jf/h12b.jf");
      InputStream h12pInputStream = Launcher.getResourceAsStream("/assets/jf/h12p.jf");
      InputStream h13bInputStream = Launcher.getResourceAsStream("/assets/jf/h13b.jf");
      InputStream h14bInputStream = Launcher.getResourceAsStream("/assets/jf/h14b.jf");
      InputStream h16bInputStream = Launcher.getResourceAsStream("/assets/jf/h16b.jf");
      InputStream h20bInputStream = Launcher.getResourceAsStream("/assets/jf/h20b.jf");
      InputStream h24bInputStream = Launcher.getResourceAsStream("/assets/jf/h24b.jf");

      h11p = new byte[h11pInputStream.available()];
      h11pInputStream.read(h11p);
      h12b = new byte[h12bInputStream.available()];
      h12bInputStream.read(h12b);
      h12p = new byte[h12pInputStream.available()];
      h12pInputStream.read(h12p);
      h13b = new byte[h13bInputStream.available()];
      h13bInputStream.read(h13b);
      h14b = new byte[h14bInputStream.available()];
      h14bInputStream.read(h14b);
      h16b = new byte[h16bInputStream.available()];
      h16bInputStream.read(h16b);
      h20b = new byte[h20bInputStream.available()];
      h20bInputStream.read(h20b);
      h24b = new byte[h24bInputStream.available()];
      h24bInputStream.read(h24b);

    } catch (IOException e) {
      Logger.Error("Error loading jagex font files");
      throw new RuntimeException(e);
    }
  }

  // assigns system fonts authentic to after 2009 shenanigans
  public static boolean loadSystemFonts() {
    if (!loadSystemFont("h11p", 0)) {
      return false;
    }
    if (!loadSystemFont("h12b", 1)) {
      return false;
    }
    if (!loadSystemFont("h12p", 2)) {
      return false;
    }
    if (!loadSystemFont("h13b", 3)) {
      return false;
    }
    if (!loadSystemFont("h14b", 4)) {
      return false;
    }
    if (!loadSystemFont("h16b", 5)) {
      return false;
    }
    if (!loadSystemFont("h20b", 6)) {
      return false;
    }
    if (!loadSystemFont("h24b", 7)) {
      return false;
    }
    systemFontLoaded = true;
    return true;
  }

  // calls wonky 2009+ authentic system font generator
  private static boolean loadSystemFont(String fontName, int index) {
    if (Reflection.loadSystemFont == null) return false;

    try {
      return (boolean)
          Reflection.loadSystemFont.invoke(Client.instance, Client.instance, fontName, index, 0);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  // assigns jf fonts authentic to before 2009 shenanigans
  // used by hook; do not rename
  public static void loadJfFonts() {
    resetFontVariables();

    gameFonts[0] = h11p;
    gameFonts[1] = h12b;
    gameFonts[2] = h12p;
    gameFonts[3] = h13b;
    gameFonts[4] = h14b;
    gameFonts[5] = h16b;
    gameFonts[6] = h20b;
    gameFonts[7] = h24b;
    systemFontLoaded = false;
  }

  // resets all static variables related to font data
  private static void resetFontVariables() {
    gameFontSize = 0;
    gameFonts = new byte[50][];
    gameFontStates =
        new boolean[] {
          false, false, false, false, false, false, false, false, false, false, false, false
        };
    gameFontData = new byte[100000];
  }

  public static void syncFontSetting() {
    if (Settings.USE_JAGEX_FONTS.get(Settings.currentProfile)) {
      if (systemFontLoaded) {
        loadJfFonts();
      }
    } else {
      if (!systemFontLoaded) {
        loadSystemFonts();
      }
    }
  }
}
