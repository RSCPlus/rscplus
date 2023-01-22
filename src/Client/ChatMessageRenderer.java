package Client;

import Game.Client;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
      // 1. Find all instances of color tags w/ Regex
      // 2. Create "groups" with start and end indices
      // 3. Use start and end indices to find message segments
      // 4. Render message segments one at a time until reaching the end

      // Build regex pattern of color tags and match with message
      ArrayList<String> colorTags = ChatColors.getColorTags();
      Pattern pattern = Pattern.compile(String.join("|", colorTags), Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(message);

      // Create list of message color tag/indices
      ArrayList<ChatMessageColorIndices> allColorIndices = new ArrayList<>();

      while (matcher.find()) {
        // For each match, add entry with color tag, start index and end index
        String colorTag = matcher.group().toLowerCase();
        allColorIndices.add(new ChatMessageColorIndices(colorTag, matcher.start(), matcher.end()));
      }

      if (allColorIndices.size() > 0) {
        // If we found color tags, have to split up the message
        for (int i = 0; i < allColorIndices.size(); i++) {
          // Get the current color indices group
          ChatMessageColorIndices colorIndices = allColorIndices.get(i);

          // We can create style at this point
          SimpleAttributeSet messageSegmentStyle = createColorMessageSegmentStyle(colorIndices);

          // Look ahead for next segment
          int j = i + 1;

          if (j >= allColorIndices.size()) {
            // The "next" segment doesn't exist so the rest of the message will be this color
            String messageSegment = message.substring(colorIndices.getEndIndex());

            document.insertString(docLength, messageSegment + "\n", messageSegmentStyle);
          } else {
            // Get the "next" color indices group
            ChatMessageColorIndices nextColorIndices = allColorIndices.get(j);

            // Get the message segment indices
            // This will be the end of the "current" color indices group and start of the "next"
            int segmentStartIndex = colorIndices.getEndIndex();
            int segmentEndIndex = nextColorIndices.getStartIndex();

            // Get the actual message segment
            String messageSegment = message.substring(segmentStartIndex, segmentEndIndex);

            if (i == allColorIndices.size() - 1) {
              // We're at the last message segment, have to break here
              document.insertString(docLength, messageSegment + "\n", messageSegmentStyle);
            } else {
              // Write message on same line
              document.insertString(docLength, messageSegment, messageSegmentStyle);
            }
          }

          // Update document length (offset)
          docLength = document.getLength();
        }
      } else {
        SimpleAttributeSet messageStyle = createNormalMessageStyle();

        // No color tags found so write the entire message
        document.insertString(docLength, message + "\n", messageStyle);
      }

      // Attempt to write the message
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

  private static SimpleAttributeSet createNormalMessageStyle() {
    SimpleAttributeSet attributes = new SimpleAttributeSet();
    StyleConstants.setForeground(attributes, Color.WHITE);

    return attributes;
  }

  private static SimpleAttributeSet createColorMessageSegmentStyle(
      ChatMessageColorIndices colorIndices) {
    Color color = colorIndices.getColor();
    SimpleAttributeSet attributes = new SimpleAttributeSet();
    StyleConstants.setForeground(attributes, color);

    return attributes;
  }

  private static Color createUsernameColor() {
    Random random = new Random();

    final float hue = random.nextFloat();
    // Saturation between 0.1 and 0.3
    final float saturation = (random.nextInt(2000) + 2000) / 10000f;
    final float luminance = .9f;
    final Color color = Color.getHSBColor(hue, saturation, luminance);

    return color;
  }

  private static String getChatTypeLabel(int type) {
    switch (type) {
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

class ChatMessageColorIndices {
  private String colorTag;
  private Color color;
  private int startIndex;
  private int endIndex;

  public ChatMessageColorIndices(String colorTag, int startIndex, int endIndex) {
    this.colorTag = colorTag;
    this.startIndex = startIndex;
    this.endIndex = endIndex;

    color = ChatColors.getColorForTag(colorTag);
  }

  public int getStartIndex() {
    return startIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public Color getColor() {
    return color;
  }
}
