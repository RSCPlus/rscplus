package Game;

import Client.WikiURL;

public class MouseText {
  public static String mouseText = ""; // raw mouseText set by Client.mouse_action_hook()

  public static String colorlessText = "";
  public static String extraOptions = "";

  public static String name = "";
  public static String action = "";

  private static String mouseoverText = "";

  public static boolean isPlayer = false;
  public static boolean isNpc = false;
  public static boolean isItem = false;
  public static boolean isScenery = false;
  public static boolean isBoundary = false;

  public static int lastObjectId = -1;
  public static int lastObjectType = -1;

  public static final int SCENERY = 0;
  public static final int BOUNDARY = 1;

  public static void regenerateCleanedMouseTexts() {
    String cleanText = mouseText;
    extraOptions = "";
    colorlessText = cleanText;
    isPlayer = false;
    isNpc = false;
    isItem = false;
    isScenery = false;
    isBoundary = false;

    int indexExtraOptions = cleanText.indexOf('/');

    // Remove extra options text
    if (indexExtraOptions != -1) cleanText = cleanText.substring(0, indexExtraOptions).trim();

    // Remove color codes from string
    for (int i = 0; i < colorlessText.length(); i++) {
      if (colorlessText.charAt(i) == '@') {
        try {
          if (colorlessText.charAt(i + 4) == '@')
            colorlessText = colorlessText.substring(0, i) + colorlessText.substring(i + 5);
        } catch (Exception e) {
        }
      }
    }

    // Let's grab the extra options
    indexExtraOptions = colorlessText.indexOf('/');
    if (indexExtraOptions != -1) {
      extraOptions = colorlessText.substring(indexExtraOptions + 1).trim();
      colorlessText = colorlessText.substring(0, indexExtraOptions).trim();
    }

    if (extraOptions.length() > 0) extraOptions = "(" + extraOptions + ")";

    indexExtraOptions = cleanText.indexOf(":");
    if (indexExtraOptions != -1) {
      name = cleanText.substring(0, indexExtraOptions).trim();
      action = cleanText.substring(indexExtraOptions + 1).trim();
      mouseoverText = action + " " + name;
    } else {
      mouseoverText = cleanText;
    }

    if (name.contains("level-")) {
      if (name.startsWith("@whi@")) {
        // begins with @whi@ always
        isPlayer = true;
      } else {
        // begins with @yel@ always
        isNpc = true;
      }
    } else if (name.startsWith("@yel@")) {
      isNpc = true;
    } else if (name.startsWith("@lre@")) {
      isItem = true;
    } else if (name.startsWith("@cya@")) {
      if (lastObjectType == BOUNDARY) {
        isBoundary = true;
      } else if (lastObjectType == SCENERY) {
        isScenery = true;
      }
    }

    // Remove color codes from name
    for (int i = 0; i < name.length(); i++) {
      if (name.charAt(i) == '@') {
        try {
          if (i > 0) {
            // remove extended info, don't believe there's any legitimate use of colour changing
            // within name
            name = name.substring(0, i).trim();
            break;
          } else {
            if (name.charAt(i + 4) == '@') name = name.substring(0, i) + name.substring(i + 5);
          }
        } catch (Exception e) {
        }
      }
    }
  }

  public static String getMouseOverText() {
    return mouseoverText;
  }

  public static int getType() {
    if (isNpc) {
      return WikiURL.NPC;
    }
    if (isItem) {
      return WikiURL.ITEM;
    }
    if (isScenery) {
      return WikiURL.SCENERY;
    }
    if (isBoundary) {
      return WikiURL.BOUNDARY;
    }
    return -1;
  }

  public static int getNpcIdHover() {
    int x = MouseHandler.x;
    int y = MouseHandler.y;
    for (int i = Client.npc_list_retained.size() - 1; i >= 0; i--) {
      NPC npc = Client.npc_list_retained.get(i);
      if (npc.type == NPC.TYPE_MOB) {
        if (x >= npc.x && x <= npc.x + npc.width && y >= npc.y && y <= npc.y + npc.height) {
          return npc.id;
        }
      }
    }
    return -1;
  }

  public static int getItemIdHover() {
    int x = MouseHandler.x;
    int y = MouseHandler.y;

    if (Client.show_menu == Client.MENU_INVENTORY) {
      // item inside inventory
      int xTrans = x + 248 - Renderer.width;
      int yTrans = y - 36;
      if (xTrans >= 0 && yTrans >= 0 && xTrans < 248 && (30 / 5 * 34) > yTrans) {
        int idx = xTrans / 49 + (yTrans / 34 * 5);
        if (idx < Client.inventory_count) {
          return Client.inventory_items[idx];
        }
      }
    } else {
      // Ground item
      for (int i = Client.item_list_retained.size() - 1; i >= 0; i--) {
        Item item = Client.item_list_retained.get(i);
        if (x >= item.x && x <= item.x + item.width && y >= item.y && y <= item.y + item.height) {
          return item.id;
        }
      }
    }
    return -1;
  }

  public static int getLastObjectId() {
    return lastObjectId;
  }
}
