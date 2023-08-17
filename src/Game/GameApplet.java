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
import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GameApplet {

  public static int gameFontSize;
  public static byte[][] gameFonts;
  public static boolean[] gameFontStates;
  public static byte[] gameFontData;
  public static int[] characterWidth;

  // Official fonts
  private static byte[] h11p;
  private static byte[] h12b;
  private static byte[] h12p;
  private static byte[] h13b;
  private static byte[] h14b;
  private static byte[] h16b;
  private static byte[] h20b;
  private static byte[] h24b;

  // Custom fonts
  private static byte[] h11b;
  private static byte[] h22b;

  public static GlyphData h11bGlyphData;
  public static GlyphData h22bGlyphData;

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
      // Official fonts
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

      // Custom fonts
      InputStream h11bInputStream = Launcher.getResourceAsStream("/assets/jf/h11b.jf");
      InputStream h22bInputStream = Launcher.getResourceAsStream("/assets/jf/h22b.jf");

      h11b = new byte[h11bInputStream.available()];
      h11bInputStream.read(h11b);
      h22b = new byte[h22bInputStream.available()];
      h22bInputStream.read(h22b);
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
        syncWikiHbarImageWithFontSetting();
      }
    } else {
      if (!systemFontLoaded) {
        loadSystemFonts();
        syncWikiHbarImageWithFontSetting();
      }
    }
  }

  // TODO: Can likely render using h11p, will need additional drawing method for "no
  // shadows/borders"
  public static void syncWikiHbarImageWithFontSetting() {
    if (systemFontLoaded) {
      Renderer.image_wiki_hbar_active = Renderer.image_wiki_hbar_active_system;
      Renderer.image_wiki_hbar_inactive = Renderer.image_wiki_hbar_inactive_system;
    } else {
      Renderer.image_wiki_hbar_active = Renderer.image_wiki_hbar_active_jf;
      Renderer.image_wiki_hbar_inactive = Renderer.image_wiki_hbar_inactive_jf;
    }
  }

  /** Load {@link Glyph} data for Jagex fonts */
  public static void loadGlyphData() {
    h11bGlyphData = generateGlyphData(h11b);
    h22bGlyphData = generateGlyphData(h22b);
  }

  /**
   * Load {@link Glyph} data for a custom Jagex font Code adapted from <a
   * href="https://github.com/2003scape/rsc-fonts">rsc-fonts</a>
   */
  private static GlyphData generateGlyphData(byte[] fontData) {
    try {
      Map<Character, Glyph> glyphMap = new HashMap<>();

      int maxWidth = 0;
      int maxHeight = 0;
      int minY = 0;

      // Generate glyph data for each applicable rsc character
      for (int i = 0; i < Client.inputFilterChars.length(); i++) {
        char letter = Client.inputFilterChars.charAt(i);
        Glyph glyph = getGlyph(fontData, letter);

        if (glyph.boundingBox.width > maxWidth) {
          maxWidth = glyph.boundingBox.width;
        }

        if (glyph.boundingBox.height > maxHeight) {
          maxHeight = glyph.boundingBox.height;
        }

        if (glyph.boundingBox.y < minY) {
          minY = glyph.boundingBox.y;
        }

        glyphMap.put(letter, glyph);
      }

      Rectangle boundingBox = new Rectangle(0, minY, maxWidth, maxHeight - minY);
      int baseline = boundingBox.height + boundingBox.y;

      return new GlyphData(glyphMap, boundingBox, baseline);
    } catch (Exception e) {
      throw new RuntimeException("Error generating glyph data", e);
    }
  }

  /**
   * Creates a {@link Glyph}
   *
   * <p>code adapted from: <a href="https://github.com/2003scape/rsc-fonts">rsc-fonts</a>
   */
  private static Glyph getGlyph(byte[] fontData, char letter) {
    int characterOffset = characterWidth[letter];

    // baseline offsets
    byte xOffset = fontData[characterOffset + 5];
    byte yOffset = fontData[characterOffset + 6];

    byte width = fontData[characterOffset + 3];
    byte height = fontData[characterOffset + 4];

    // position of pixel data for the font (on/off)
    int fontPosition =
        fontData[characterOffset] * (128 * 128)
            + fontData[characterOffset + 1] * 128
            + fontData[characterOffset + 2];

    int bitLength = (int) Math.ceil(width / 8.0) * 8;

    byte[][] bitmap = new byte[height][bitLength];

    // construct glyph bitmap
    for (int y = 0; y < height; y++) {
      byte[] row = new byte[bitLength];

      for (int x = 0; x < width; x++) {
        if (fontData[fontPosition] != 0) {
          row[x] = 1;
        } else {
          row[x] = 0;
        }

        fontPosition++;
      }

      bitmap[y] = row;
    }

    Rectangle boundingBox = new Rectangle(xOffset, yOffset - height, width, height);

    return new Glyph(letter, bitmap, boundingBox, fontData[characterOffset + 7]);
  }

  /**
   * Generate a pixel map for a particular string, using {@link GlyphData} for the font
   *
   * <p>Code adapted and extended from: <a
   * href="https://github.com/2003scape/rsc-fonts">rsc-fonts</a> and <a
   * href="https://github.com/misterhat/bdf">misterhat/bdf</a>
   */
  public static int[][] getTextPixelMap(
      GlyphData glyphData, String text, Color colour, boolean bordered) {
    int numChars = text.length();
    final Glyph[] glyphs = new Glyph[numChars];

    int height = glyphData.getBoundingBox().height;
    int baseline = glyphData.getBaseLine();

    int borderOffset = bordered ? 1 : 0;

    int xPosition = borderOffset; // Begins at 0 when no border offset is needed

    int bitmapWidth = 0;
    int bitMapHeight = height;

    // Determine maximum pixel map bounds for the text string
    for (int i = 0; i < numChars; i++) {
      glyphs[i] = glyphData.getGlyphMap().get(text.charAt(i));

      // Replace missing characters with '?', mostly as a safety check
      if (glyphs[i] == null) {
        // nbsp workaround
        if (text.charAt(i) == 160) {
          glyphs[i] = glyphData.getGlyphMap().get(' ');
        } else {
          glyphs[i] = glyphData.getGlyphMap().get('?');
        }
      }

      bitmapWidth += glyphs[i].displayWidth;

      if (glyphs[i].boundingBox.height > bitMapHeight) {
        bitMapHeight = glyphs[i].boundingBox.height;
      }
    }

    // Construct pixel map
    int[][] pixelMap = new int[bitMapHeight + 1 + borderOffset][bitmapWidth + 1 + borderOffset];

    // Add fully-opaque alpha value
    int colorValue = colour.getRGB() | (0xFF << 24);

    for (int i = 0; i < numChars; i++) {
      final Glyph glyph = glyphs[i];

      // Index into the destination pixelMap for the top row of the font
      int rowStart = baseline - glyph.boundingBox.y - glyph.boundingBox.height;

      // Draw the glyph
      for (int y = 0; y < glyph.boundingBox.height; y++) {
        for (int x = 0; x < glyph.boundingBox.width; x++) {
          int row = rowStart + y;
          int column = xPosition + glyph.boundingBox.x + x;

          boolean hasBit = glyph.bitmap[y][x] == 1;
          boolean isShadowPixel = pixelMap[row][column] == 0xFF000000;

          // Set pixel color or transparency based on the glyph bitmap, unless a shadow pixel is
          // present
          pixelMap[row][column] = hasBit ? colorValue : isShadowPixel ? pixelMap[row][column] : 0x0;

          if (hasBit) {
            // Create bottom shadow
            if (row + 1 <= pixelMap.length) {
              pixelMap[row + 1][column] = 0xFF000000;
            }

            if (column + 1 <= pixelMap[0].length) {
              pixelMap[row][column + 1] = 0xFF000000;
            }

            // Create upper shadow when needed
            if (bordered) {
              if (row - 1 >= 0 && pixelMap[row - 1][column] != colorValue) {
                pixelMap[row - 1][column] = 0xFF000000;
              }

              if (column - 1 >= 0 && pixelMap[row][column - 1] != colorValue) {
                pixelMap[row][column - 1] = 0xFF000000;
              }
            }
          }
        }
      }

      xPosition += glyph.displayWidth;
    }

    return pixelMap;
  }

  /** Holds font data for all characters in a particular font */
  public static class GlyphData {
    private final Map<Character, Glyph> glyphMap;
    private final Rectangle boundingBox;
    private final int baseLine;

    public GlyphData(Map<Character, Glyph> glyphMap, Rectangle boundingBox, int baseLine) {
      this.glyphMap = glyphMap;
      this.boundingBox = boundingBox;
      this.baseLine = baseLine;
    }

    public Map<Character, Glyph> getGlyphMap() {
      return glyphMap;
    }

    public Rectangle getBoundingBox() {
      return boundingBox;
    }

    public int getBaseLine() {
      return baseLine;
    }
  }

  /** Holds font data about an individual glyph */
  private static class Glyph {
    private final char letter;
    private final byte[][] bitmap;
    private final Rectangle boundingBox;
    private final int displayWidth;

    public Glyph(char letter, byte[][] bitmap, Rectangle boundingBox, int displayWidth) {
      this.letter = letter;
      this.bitmap = bitmap;
      this.boundingBox = boundingBox;
      this.displayWidth = displayWidth;
    }
  }
}
