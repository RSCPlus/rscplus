package Client;

import Game.MouseHandler;

public class WikiURL {
  public static boolean nextClickIsLookup = false;
  public static long lastLookupTime = 0;
  public static long cooldownTimer = 3000; // milliseconds required between queries
  private static final String wikiPrefix = "https://classic.runescape.wiki/?search=";
  private static final String wikiSuffix =
      "&utm_source=rscplus"; // https://en.wikipedia.org/wiki/UTM_parameters
  // also used by OSRS & RS3 clients' wiki integration

  public static String translateNameToUrl(String name) throws IllegalArgumentException {
    if (name.equals("Walk here") || name.contains("Choose a target")) {
      return "INVALID";
    }
    return wikiPrefix + name + wikiSuffix;
  }

  public static boolean shouldConsume() {
    return nextClickIsLookup && !MouseHandler.rightClick;
  }
}
