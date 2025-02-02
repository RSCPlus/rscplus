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

import static Game.Renderer.getStringBounds;

import Client.Launcher;
import Client.Settings;
import Client.Util;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;

/**
 * Responsible for storing data and performing rendering duties related to the special star that is
 * drawn next to highlighted ground item names
 */
public class SpecialStar {
  private static BufferedImage highlightStarImage = null;
  private static BufferedImage secondaryHighlightStarImage = null;

  public static BufferedImage image_highlighted_item;
  public static short[][] starGlimmerMask;
  public static boolean starImagesUpdateRequired = false;

  /**
   * Reads the base star image and glimmer mask data from disk and creates the special star {@link
   * BufferedImage}s
   */
  public static void initializeSpecialStars() throws IOException {
    BufferedImage in = ImageIO.read(Launcher.getResource("/assets/highlighted_item.png"));
    image_highlighted_item =
        new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image_highlighted_item.createGraphics();
    g.drawImage(in, 0, 0, in.getWidth(), in.getHeight(), null);
    g.dispose();

    starGlimmerMask = new short[12][11];
    try (InputStream inputStream =
            Launcher.getResource("/assets/starGlimmerMask.dat").openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      int row = 0;
      while ((line = br.readLine()) != null) {
        String[] values = line.split(",");
        for (int col = 0; col < values.length; col++) {
          starGlimmerMask[row][col] = Short.parseShort(values[col]);
        }
        row++;
      }
    }

    generateSpecialStarImages();
  }

  /** Creates the special star {@link BufferedImage}s */
  public static synchronized void generateSpecialStarImages() {
    highlightStarImage =
        applyColorMask(
            image_highlighted_item, Settings.ITEM_HIGHLIGHT_COLOUR.get(Settings.currentProfile));
    secondaryHighlightStarImage =
        applyColorMask(
            image_highlighted_item,
            Settings.ITEM_SPECIAL_HIGHLIGHT_COLOUR.get(Settings.currentProfile));
  }

  /**
   * Draws the special star image overlay, accounting for right-click menu overlaps
   *
   * @param g {@link Graphics2D} instances to draw with
   * @param isSecondary boolean value indicating whether to draw the secondary star image
   * @param text Item name text, used for positioning the star bounds
   * @param x x coordinate for drawing the star
   * @param y y coordinate for drawing the star
   */
  public static void drawSpecialStarImage(
      Graphics2D g, boolean isSecondary, String text, int x, int y) {
    int correctedX = x;
    int correctedY = y;

    // Adjust for centering
    Dimension bounds = getStringBounds(g, text);
    correctedX -= (bounds.width / 2);
    correctedY += (bounds.height / 2);

    int imageX = 15;
    int imageY = 11;

    if (Renderer.showingRightClickMenu) {
      Rectangle drawBounds =
          new Rectangle(
              correctedX - imageX - 2,
              correctedY - (bounds.height),
              bounds.width + imageX + 3,
              bounds.height + 4);
      Rectangle menuBounds =
          new Rectangle(
              Renderer.rightClickMenuX, Renderer.rightClickMenuY,
              Renderer.rightClickMenuWidth, Renderer.rightClickMenuHeight);

      if (drawBounds.intersects(menuBounds)) {
        return;
      }
    }

    BufferedImage starImage = isSecondary ? secondaryHighlightStarImage : highlightStarImage;

    g.drawImage(starImage, correctedX - imageX, correctedY - imageY, null);
  }

  /**
   * Applies the special star color mask to the base image
   *
   * @param image Base {@link BufferedImage} to colorize
   * @param colorMask Integer value of the color mask
   * @return Final colorized {@link BufferedImage}
   */
  private static BufferedImage applyColorMask(BufferedImage image, int colorMask) {
    BufferedImage maskedImage = Renderer.cloneBufferedImage(image);

    int width = maskedImage.getWidth();
    int height = maskedImage.getHeight();

    // Define base colors
    Color pointMaskColor = new Color(colorMask);
    Color baseMaskColor = darken(pointMaskColor, 0.05f);
    Color glimmerColor = getRedFacingAnalogousColor(baseMaskColor);
    Color glimmerShadowColor = getRedFacingAnalogousColor(baseMaskColor, 36);

    // Generate color mask data
    float[] pointColorMaskValues = generateMaskColors(pointMaskColor);
    float[] baseColorMaskValues = generateMaskColors(baseMaskColor);
    float[] glimmerColorValues = generateMaskColors(glimmerColor);
    float[] glimmerShadowColorValues = generateMaskColors(glimmerShadowColor);

    // Determine the mix and max gray values for the base image
    float maxImageGrayLevel = -1;
    float minImageGrayLevel = 256;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = maskedImage.getRGB(x, y);

        int ri = (argb >> 16) & 0xFF;
        int gi = (argb >> 8) & 0xFF;
        int bi = (argb & 0xFF);
        int grayLevelImg = Math.max(Math.max(ri, gi), bi);
        if (grayLevelImg > maxImageGrayLevel) {
          maxImageGrayLevel = grayLevelImg;
        }
        if (grayLevelImg < minImageGrayLevel) {
          minImageGrayLevel = grayLevelImg;
        }
      }
    }

    // Iterate through the image, setting the color for each pixel per the mask data
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int grayLevelColorPixel = -1;
        float colorBrPixel = -1f;
        float huePixel = -1f;
        float saturationPixel = -1f;

        int maskByte = starGlimmerMask[y][x];
        if (maskByte == 0) {
          grayLevelColorPixel = (int) pointColorMaskValues[0];
          colorBrPixel = pointColorMaskValues[1];
          huePixel = pointColorMaskValues[2];
          saturationPixel = pointColorMaskValues[3];
        } else if (maskByte == 1) {
          grayLevelColorPixel = (int) baseColorMaskValues[0];
          colorBrPixel = baseColorMaskValues[1];
          huePixel = baseColorMaskValues[2];
          saturationPixel = baseColorMaskValues[3];
        } else if (maskByte == 2) {
          grayLevelColorPixel = (int) glimmerColorValues[0];
          colorBrPixel = glimmerColorValues[1];
          huePixel = glimmerColorValues[2];
          saturationPixel = glimmerColorValues[3];
        } else if (maskByte == 3) {
          grayLevelColorPixel = (int) glimmerShadowColorValues[0];
          colorBrPixel = glimmerShadowColorValues[1];
          huePixel = glimmerShadowColorValues[2];
          saturationPixel = glimmerShadowColorValues[3];
        }

        int argb = maskedImage.getRGB(x, y);

        int alpha = (argb & 0xff000000);

        int ri = (argb >> 16) & 0xFF;
        int gi = (argb >> 8) & 0xFF;
        int bi = (argb & 0xFF);
        int grayLevelImg = Math.max(Math.max(ri, gi), bi);

        float colorDelta =
            grayLevelColorPixel > maxImageGrayLevel ? grayLevelColorPixel - maxImageGrayLevel : 0;

        float imageBr = (colorDelta + grayLevelImg) / 255f;

        float finalBr = imageBr * colorBrPixel;

        int rgb = Color.HSBtoRGB(huePixel, saturationPixel, finalBr);

        argb = (rgb & 0x00ffffff) | alpha;
        maskedImage.setRGB(x, y, argb);
      }
    }

    // Apply gamma filter to brighten the final image, matching it to the provided color
    RescaleOp bright = new RescaleOp(1.2f, 0, null);
    bright.filter(maskedImage, maskedImage);

    return maskedImage;
  }

  /**
   * Generates all colors used for masking the base image
   *
   * @param baseColor Base {@link Color} instance
   * @return {@link Float} array with data for the following indices:
   *     <ol>
   *       <li>Gray level
   *       <li>Brightness
   *       <li>Hue
   *       <li>Saturation
   *     </ol>
   */
  private static float[] generateMaskColors(Color baseColor) {
    float[] hsbMask =
        Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
    float hue = hsbMask[0];
    float saturation = hsbMask[1];

    int colorInt = Util.colorToInt(baseColor);
    int rG = (colorInt >> 16) & 0xFF;
    int gG = (colorInt >> 8) & 0xFF;
    int bG = (colorInt & 0xFF);
    int grayLevel = Math.max(Math.max(rG, gG), bG);
    float brightness = grayLevel / 255f;

    return new float[] {(float) grayLevel, brightness, hue, saturation};
  }

  /**
   * Darkens a provided {@link Color} instance by a percentage
   *
   * @param baseColor Provided {@link Color} instance
   * @param amount {@code float} value indicating the percent difference
   * @return Final darkened {@link Color} instance
   */
  private static Color darken(Color baseColor, float amount) {
    float[] hsbValues =
        Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);

    float hue = hsbValues[0];
    float saturation = hsbValues[1];
    float brightness = hsbValues[2];

    brightness -= amount;

    if (brightness < 0) {
      brightness = 0;
    }

    return new Color(Color.HSBtoRGB(hue, saturation, brightness));
  }

  /** @return The calculated red-facing analogous color for a given {@link Color} instance */
  private static Color getRedFacingAnalogousColor(Color mainColor) {
    return getRedFacingAnalogousColor(mainColor, 1);
  }

  /**
   * Calculates the fractional red-facing analogous color for a given {@link Color} instance
   *
   * @param mainColor The provided {@link Color} instance
   * @param divisor Fractional divisor used to determine the fractional analogous color
   * @return The calculated red-facing analogous color for a given color
   */
  private static Color getRedFacingAnalogousColor(Color mainColor, int divisor) {
    float angleRotation = 1 / (float) divisor;

    float[] hsbLeftColor =
        Color.RGBtoHSB(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), null);
    hsbLeftColor[0] -= angleRotation;
    Color leftColor = new Color(Color.HSBtoRGB(hsbLeftColor[0], hsbLeftColor[1], hsbLeftColor[2]));

    float[] hsbRightColor =
        Color.RGBtoHSB(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), null);
    hsbRightColor[0] += angleRotation;
    Color rightColor =
        new Color(Color.HSBtoRGB(hsbRightColor[0], hsbRightColor[1], hsbRightColor[2]));

    if (leftColor.getRed() == rightColor.getRed()) {
      // When red values are equal, use the bluer value
      if (leftColor.getGreen() < rightColor.getGreen()) {
        return leftColor;
      } else {
        return rightColor;
      }
    } else {
      if (leftColor.getRed() > rightColor.getRed()) {
        return leftColor;
      } else {
        return rightColor;
      }
    }
  }
}
