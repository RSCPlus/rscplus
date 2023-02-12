package Client;

import java.awt.*;

public class RanOverrideEffect {
  private static int cachedRandomColor = 0x00BEEF;
  private static long lastColourChange = 0;

  /**
   * Hooks into the original {@code drawstring()} method to override the colour displayed for the
   * current frame, when the user has activated the "@ran@" chat effect and the override option has
   * been enabled. <br>
   * <br>
   * <b>Note:</b> For possible future implementations, the {@code getRGB()} method on a given {@link
   * Color} object will return its equivalent decimal value.
   *
   * @param defaultColour The original value passed into {@code drawString()}
   * @return {@code int} rgb value to display for the current frame
   */
  public static int getRanEffectOverrideColour(int defaultColour) {
    switch (Settings.CUSTOM_RAN_CHAT_EFFECT.get(Settings.currentProfile)) {
      case DISABLED:
        return defaultColour;
      default:
      case VANILLA:
        return (int) (Math.random() * 16777215D);
      case SLOWER:
        long now = System.currentTimeMillis();
        if (now - lastColourChange
            > (1000.0F / Settings.RAN_EFFECT_TARGET_FPS.get(Settings.currentProfile))) {
          cachedRandomColor = (int) (Math.random() * 16777215D);
          lastColourChange = now;
        }
        return cachedRandomColor;
      case STATIC:
        return Settings.CUSTOM_RAN_STATIC_COLOUR.get(Settings.currentProfile);
      case FLASH1:
        // RS2 flash1, red & yellow
        return (System.currentTimeMillis() / 20) % 20 >= 10 ? 0xFFFF00 : 0xFF0000;
      case FLASH2:
        // RS2 flash2, magenta & blue
        return (System.currentTimeMillis() / 20) % 20 >= 10 ? 0x00FFFF : 0x0000FF;
      case FLASH3:
        // RS2 flash3, green & green-white
        return (System.currentTimeMillis() / 20) % 20 >= 10 ? 0x80FF80 : 0x00B000;
      case RGB_WAVE:
      case GLOW1:
        {
          // RS2 glow1, primitive rainbow sweep
          int intCycle = (int) Math.abs((System.currentTimeMillis() / 20) % 300);
          if (intCycle < 50) {
            // red becoming yellow
            return 0xFF0000 + 0x000500 * intCycle;
          } else if (intCycle < 100) {
            // yellow becoming green
            return 0xFFFF00 - 0x050000 * (intCycle - 50);
          } else if (intCycle < 150) {
            // green becoming cyan
            return 0x00FF00 + 0x000005 * (intCycle - 100);
          } else if (intCycle < 200) {
            // extended to loop for RSC; cyan becoming blue
            return 0x00FFFF - 0x000500 * (intCycle - 150);
          } else if (intCycle < 250) {
            // extended to loop for RSC; blue becoming magenta
            return 0x0000FF + 0x050000 * (intCycle - 200);
          } else if (intCycle < 300) {
            // extended to loop for RSC; magenta becoming red
            return 0xFF00FF - 0x000005 * (intCycle - 250);
          }
        }
      case GLOW2:
        {
          // RS2 glow2, red & blue
          int intCycle = (int) Math.abs((System.currentTimeMillis() / 20) % 150);
          if (intCycle < 50) {
            // red becoming magenta
            return 0xFF0000 + 0x000005 * intCycle;
          } else if (intCycle < 100) {
            // magenta becoming blue
            return 0xFF00FF - 0x050000 * (intCycle - 50);
          } else if (intCycle < 150) {
            // blue becoming red
            return (0x0000FF + 0x050000 * (intCycle - 100)) - 0x000005 * (intCycle - 100);
          }
        }
      case GLOW3:
        {
          // RS2 glow3, green & cyan
          int intCycle = (int) Math.abs((System.currentTimeMillis() / 20) % 200);
          if (intCycle < 50) {
            // white becoming green
            return 0xFFFFFF - 0x050005 * intCycle;
          } else if (intCycle < 100) {
            // green becoming white
            return 0x00FF00 + 0x050005 * (intCycle - 50);
          } else if (intCycle < 150) {
            // white becoming cyan
            return 0xFFFFFF - 0x050000 * (intCycle - 100);
          } else if (intCycle < 200) {
            // extended to loop for RSC; cyan becoming white
            return 0x00FFFF + 0x050000 * (intCycle - 150);
          }
        }
    }
    return defaultColour;
  }
}
