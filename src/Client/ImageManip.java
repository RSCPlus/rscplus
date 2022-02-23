package Client;


import java.awt.image.BufferedImage;

public class ImageManip {
  public static BufferedImage prepareSceneryImage(BufferedImage image) {
    BufferedImage outputImage =
        new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

    int curPix = 0;
    boolean[] rowHasPixels = new boolean[image.getHeight()];
    boolean[] columnHasPixels = new boolean[image.getWidth()];

    int transparentPixel = image.getRGB(0, 0);
    // must iterate over all pixels to find auto-crop, so also fix bgcolor transparency here
    for (int curY = 0; curY < outputImage.getHeight(); curY++) {
      for (int curX = 0; curX < outputImage.getWidth(); curX++) {
        curPix = image.getRGB(curX, curY);
        if (curPix == transparentPixel) {
          // transparent pixel
          outputImage.setRGB(curX, curY, 0);
        } else {
          // opaque pixel
          rowHasPixels[curY] = true;
          columnHasPixels[curX] = true;
          outputImage.setRGB(curX, curY, curPix);
        }
      }
    }

    int leftMostPixel = 0;
    int rightMostPixel = image.getWidth();
    int topMostPixel = 0;
    int bottomMostPixel = image.getHeight();

    for (int i = 0; i < columnHasPixels.length; i++) {
      if (columnHasPixels[i]) {
        leftMostPixel = i;
        break;
      }
    }
    for (int i = columnHasPixels.length - 1; i >= 0; i--) {
      if (columnHasPixels[i]) {
        rightMostPixel = i;
        break;
      }
    }
    for (int i = 0; i < rowHasPixels.length; i++) {
      if (rowHasPixels[i]) {
        topMostPixel = i;
        break;
      }
    }
    for (int i = rowHasPixels.length - 1; i >= 0; i--) {
      if (rowHasPixels[i]) {
        bottomMostPixel = i;
        break;
      }
    }

    return outputImage.getSubimage(
        leftMostPixel,
        topMostPixel,
        rightMostPixel - leftMostPixel,
        bottomMostPixel - topMostPixel);
  }
}
