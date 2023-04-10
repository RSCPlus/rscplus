package Chat;


import Game.Client;
import org.apache.commons.compress.archivers.sevenz.CLI;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

public class ChatWindowHTMLChatRenderer {
  public static HashMap<String, Color> usernameColors = new HashMap<>();

  private static boolean showChatTypeCell = true;

  public static void setShowChatTypeCell(boolean showChatTypeCell) {
    ChatWindowHTMLChatRenderer.showChatTypeCell = showChatTypeCell;
  }

  public static void renderChatDocument(
      ChatWindowHTMLChatView chatView, ArrayList<ChatMessage> chatMessages) {
    String htmlTemplate = "<html>" + "<body>" + "%s" + "</body>" + "</html>";

    String tableTemplate =
        "<table id=\"chat-table\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">"
            + "%s"
            + "</table>";

    StringBuilder rowsBuilder = new StringBuilder();

    for (ChatMessage chatMessage : chatMessages) {
      String rowStr = createChatMessageRow(chatMessage);
      rowsBuilder.append(rowStr);
    }

    String rowsStr = rowsBuilder.toString();

    tableTemplate = String.format(tableTemplate, rowsStr);
    htmlTemplate = String.format(htmlTemplate, tableTemplate);

    chatView.setText(htmlTemplate);
  }

  public static void appendChatMessage(ChatWindowHTMLChatView chatView, ChatMessage chatMessage) {
    HTMLDocument document = (HTMLDocument) chatView.getDocument();
    Element chatTableBody = document.getElement("chat-table");

    if (chatTableBody != null) {
      String rowStr = createChatMessageRow(chatMessage);

      try {
        document.insertBeforeEnd(chatTableBody, rowStr);
      } catch (Exception e) {
        System.out.println("Failed to append chat message: " + chatMessage.getMessage());
      }
    }
  }

  private static String createChatMessageRow(ChatMessage chatMessage) {
    String message = chatMessage.getMessage();
    long timestamp = chatMessage.getTimestamp();
    int messageType = chatMessage.getType();

    message = parseChatMessage(message);
    DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
    String timestampStr = dateFormat.format(timestamp);

    String username = chatMessage.getUsername();
    String messageTypeStr = "";

    if(messageType == Client.CHAT_PRIVATE) {
      messageTypeStr = "PRIVATE";
    } else if(messageType == Client.CHAT_PRIVATE_OUTGOING) {
      messageTypeStr = "PRIVATE";
      username = Client.player_name;
    } else if(messageType == Client.CHAT_QUEST) {
      messageTypeStr = "QUEST";
    } else if(messageType == Client.CHAT_CHAT) {
      messageTypeStr = "CHAT";
    }

    String usernameRgbColor = getUsernameRgbColor(username);
    String usernameHTML = String.format("<span style=\"color: %s; font-weight: 600;\">%s</span>", usernameRgbColor, username);

    String rowStr =
      "<tr valign=\"top\">"
              + "<td><div class=\"timestamp-cell\">[%s]</div></td>";

    if(showChatTypeCell) {
      rowStr += "<td class=\"message-type-cell-container\"><div class=\"message-type-cell\">[%s]</div></td>";
    }

    rowStr += "<td><div class=\"username-cell\">%s:</div></td>"
      + "<td><div class=\"message-cell\">%s</div></td>"
      + "</tr>";

    if(showChatTypeCell) {
      return String.format(rowStr, timestampStr, messageTypeStr, usernameHTML, message);
    } else {
      return String.format(rowStr, timestampStr, usernameHTML, message);
    }
  }

  private static String getUsernameRgbColor(String username) {
    Color usernameColor = usernameColors.get(username);
    if (usernameColor == null) {
      usernameColor = createUsernameColor();
      usernameColors.put(username, usernameColor);
    }

    return getColorRGB(usernameColor);
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

  private static String parseChatMessage(String message) {
    try {
      Pattern pattern = Pattern.compile("@[a-zA-Z]{3}@", Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(message);

      ArrayList<Integer> tagStartIndices = new ArrayList<>();
      ArrayList<Integer> tagEndIndices = new ArrayList<>();
      ArrayList<Color> tagColors = new ArrayList<>();
      ArrayList<String> htmlMessageSegments = new ArrayList<>();

      while(matcher.find()) {
        String tag = matcher.group().toLowerCase();
        Color tagColor = ChatColors.getColorForTag(tag);

        tagColors.add(tagColor);
        tagStartIndices.add(matcher.start());
        tagEndIndices.add(matcher.end());
      }

      if(tagColors.size() > 0) {
        for(int i = 0; i < tagColors.size(); i++) {
          int tagEndIndex = tagEndIndices.get(i);
          Color tagColor = tagColors.get(i);

          // Look ahead
          int j = i + 1;

          if(j >= tagColors.size()) {
            // There isn't a next message segment so just get the whole message after the tag
            String messageSegment = message.substring(tagEndIndex);
            String htmlMessageSegment = createHTMLMessageSegment(messageSegment, tagColor);

            htmlMessageSegments.add(htmlMessageSegment);
          } else {
            int nextTagStartIndex = tagStartIndices.get(j);

            // Get the actual message segment
            String messageSegment = message.substring(tagEndIndex, nextTagStartIndex);
            String htmlMessageSegment = createHTMLMessageSegment(messageSegment, tagColor);

            htmlMessageSegments.add(htmlMessageSegment);
          }
        }
      } else {
        return message;
      }

      return String.join("", htmlMessageSegments);
    } catch (Exception e) {
      return message;
    }
  }

  private static String createHTMLMessageSegment(String messageSegment, Color color) {
    if(color == null) {
      return "<span>" + messageSegment + "</span>";
    }

    String colorRGB = getColorRGB(color);
    return "<span style=\"color: " + colorRGB + "\">" + messageSegment + "</span>";
  }

  private static String getColorRGB(Color color) {
    String rgbValues = new String(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
    return String.format("rgb(%s)", rgbValues);
  }
}
