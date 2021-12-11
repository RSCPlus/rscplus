package Client;

import Game.MouseHandler;
import Game.MouseText;

public class HiscoresURL {
  public static boolean nextClickIsLookup = false;
  public static long lastLookupTime = 0;
  public static long cooldownTimer = 3000; // milliseconds required between queries

  public static String getURL() {
    String name = MouseText.name;
    if (name.equals("Walk here") || name.contains("Choose a target") || name.equals("")) {
      return "INVALID";
    }
    if (Settings.WORLD_HISCORES_URL.get(Settings.WORLD.get(Settings.currentProfile)).equals("")) {
      return "NO_URL";
    }
    return Settings.WORLD_HISCORES_URL
        .get(Settings.WORLD.get(Settings.currentProfile))
        .replace("%USERNAME%", name);
  }

  public static boolean shouldConsume() {
    return nextClickIsLookup && !MouseHandler.rightClick;
  }
}
