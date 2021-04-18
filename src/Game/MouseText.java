package Game;

public class MouseText {
  public static String mouseText = ""; // raw mouseText set by Client.mouse_action_hook()

  public static String colorlessText = "";
  public static String extraOptions = "";

  public static String name = "";
  public static String action = "";

  private static String mouseoverText = "";

  public static boolean isPlayer = false;
  public static boolean isNpc = false;

  public static void regenerateCleanedMouseTexts() {
    String cleanText = mouseText;
    extraOptions = "";
    colorlessText = cleanText;
    isPlayer = false;
    isNpc = false;

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
}
