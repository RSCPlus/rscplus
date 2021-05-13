package Client;

import Game.MouseHandler;
import Game.MouseText;

public class WikiURL {
  public static boolean nextClickIsLookup = false;
  public static long lastLookupTime = 0;
  public static long cooldownTimer = 3000; // milliseconds required between queries
  private static final String wikiPrefix = "https://classic.runescape.wiki/w/Special:Lookup?name=";
  private static final String typePrefix = "&type=";
  private static final String idPrefix = "&id=";
  private static final String utm_source =
      "&utm_source=rscplus"; // https://en.wikipedia.org/wiki/UTM_parameters
  // also used by OSRS & RS3 clients' wiki integration

  public static final int ITEM = 0;
  public static final int NPC = 1;
  public static final int SCENERY = 2;
  public static final int BOUNDARY = 3;

  public static String getURL() throws IllegalArgumentException {
    String name = MouseText.name;
    if (name.equals("Walk here") || name.contains("Choose a target") || name.equals("")) {
      return "INVALID";
    }
    StringBuilder url = new StringBuilder(wikiPrefix);
    url.append(name);

    int id = -1;
    switch (MouseText.getType()) {
      case ITEM:
        url.append(typePrefix).append("item");
        id = MouseText.getItemIdHover();
        break;
      case NPC:
        url.append(typePrefix).append("npc");
        id = MouseText.getNpcIdHover();
        break;
      case SCENERY:
        url.append(typePrefix).append("object");
        id = MouseText.getLastObjectId();
        break;
      case BOUNDARY:
        url.append(typePrefix).append("wallobject");
        id = MouseText.getLastObjectId();
        break;
    }
    if (id != -1) {
      url.append(idPrefix).append(id);
    }

    return url.append(utm_source).toString();
  }

  public static boolean shouldConsume() {
    return nextClickIsLookup && !MouseHandler.rightClick;
  }
}
