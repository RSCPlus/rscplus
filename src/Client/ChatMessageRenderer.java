package Client;

import Game.Client;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.text.*;

public class ChatMessageRenderer {
  public static HashMap<String, Color> usernameColors = new HashMap<>();

  public static StyledDocument renderChatMessages(
      ArrayList<ChatMessage> chatMessages, boolean showChatTypeLabel) {
    DefaultStyledDocument document = new DefaultStyledDocument(new StyleContext());

    for (ChatMessage chatMessage : chatMessages) {
      renderChatMessage(document, chatMessage, showChatTypeLabel);
    }

    return document;
  }

  public static void renderChatMessage(
      Document document, ChatMessage chatMessage, boolean showChatTypeLabel) {
    String username = chatMessage.getUsername();
    String message = chatMessage.getMessage();
    int type = chatMessage.getType();

    SimpleAttributeSet usernameStyle = createUsernameStyle(username);

    // Get the document length
    int docLength = document.getLength();

    try {
      // Attempt to write the username
      String chatTypeLabel = getChatTypeLabel(type);
      if (showChatTypeLabel && chatTypeLabel != null) {
        username = String.format("[%s] %s", getChatTypeLabel(type), username);
      }

      document.insertString(docLength, username + ": ", usernameStyle);
    } catch (BadLocationException ex) {
      Logger.Error("Failed to render username:" + username);
    }

    // Get new document length
    docLength = document.getLength();
    try {
      // Attempt to write the message
      document.insertString(docLength, message + "\n", null);
    } catch (BadLocationException ex) {
      Logger.Error("Failed to render message: " + message);
    }
  }

  private static SimpleAttributeSet createUsernameStyle(String username) {
    SimpleAttributeSet attributes = new SimpleAttributeSet();

    // Get the color for this username
    Color usernameColor = usernameColors.get(username);
    if (usernameColor == null) {
      // If we didn't find any existing color, generate one now
      usernameColor = createUsernameColor();
      usernameColors.put(username, usernameColor);
    }

    // Make username bold
    StyleConstants.setBold(attributes, true);
    StyleConstants.setForeground(attributes, usernameColor);

    return attributes;
  }

  private static Color createUsernameColor() {
    Random random = new Random();

    final float hue = random.nextFloat();
    // Saturation between 0.1 and 0.3
    final float saturation = (random.nextInt(2000) + 2000) / 10000f;
    final float luminance = 0.65f;
    final Color color = Color.getHSBColor(hue, saturation, luminance);

    return color;
  }

  private static String getChatTypeLabel(int type) {
    switch (type) {
      case Client.CHAT_GLOBAL:
        return "Global";
      case Client.CHAT_CHAT:
        return "Chat";
      case Client.CHAT_QUEST:
        return "Quest";
      case Client.CHAT_PRIVATE:
        return "Private";
      default:
        return null;
    }
  }
}
