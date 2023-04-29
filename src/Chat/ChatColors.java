package Chat;

import java.awt.*;
import java.util.HashMap;

public class ChatColors {

  private static final HashMap<String, Color> rscColorMap =
      new HashMap() {
        {
          put("@red@", Color.decode("#FF0000"));
          put("@ora@", Color.decode("#FF9040"));
          put("@yel@", Color.decode("#FFFF00"));
          put("@gre@", Color.decode("#00FF00"));
          put("@blu@", Color.decode("#0000FF"));
          put("@cya@", Color.decode("#00FFFF"));
          put("@mag@", Color.decode("#FF00FF"));
          put("@whi@", Color.decode("#FFFFFF"));
          put("@bla@", Color.decode("#000000"));
          put("@dre@", Color.decode("#C00000"));
          put("@lre@", Color.decode("#FF9040"));
          put("@or1@", Color.decode("#FFB000"));
          put("@or2@", Color.decode("#FF7000"));
          put("@or3@", Color.decode("#FF3000"));
          put("@gr1@", Color.decode("#C0FF00"));
          put("@gr2@", Color.decode("#80FF00"));
          put("@gr3@", Color.decode("#40FF00"));
        }
      };

  public static Color getColorForTag(String tag) {
    return rscColorMap.get(tag);
  }
}
