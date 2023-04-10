package Chat;

import Game.Client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.border.Border;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class ChatWindowHTMLChatView extends JEditorPane {

  private HTMLEditorKit kit = new HTMLEditorKit();
  private ArrayList<ChatMessage> chatMessages = new ArrayList<>();
  private ArrayList<ChatMessage> filteredChatMessages = new ArrayList<>();
  private HTMLDocument document;
  private StyleSheet styleSheet;
  private ArrayList<Integer> chatFilter = new ArrayList<Integer>() {
    {

      add(-1);
    }
  };

  private int fontSize = 12;

  public ChatWindowHTMLChatView() {
    super();

    Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    setBorder(emptyBorder);

    setEditable(false);
    setEditorKit(kit);

    styleSheet = kit.getStyleSheet();
    document = (HTMLDocument) kit.createDefaultDocument();

    // Setup stylesheet
    styleSheet.addRule("body { padding: 0; margin: 0; background: #282828; height: 100%; }");
    styleSheet.addRule("#chat-table { color: white; height: 100%; padding: 4px; }");

    //    styleSheet.addRule(
    //        ".chat-divider { margin-left: 134px; width: 20px; height: 1px; border-left: 2px solid
    // #74747c; }");
    styleSheet.addRule(".timestamp-cell { width: 60px; }");
    styleSheet.addRule(".message-type-cell { text-align: right; width: 50px }");
    styleSheet.addRule(".username-cell { text-align: right; width: 85px; }");
    styleSheet.addRule(".message-cell { padding-left: 4px; padding-bottom: 4px; }");

    //    ".message-cell { padding-left: 4px; padding-bottom: 4px; padding-right: 4px; border-left:
    // 2px solid #74747c; }");

    setDocument(document);
    setContentType("text/html");

    ChatWindowHTMLChatRenderer.renderChatDocument(this, chatMessages);

    setFontSize(12);
  }

  public void setChatFilter(int[] chatFilter) {
    this.chatFilter.clear();
    for(int filter : chatFilter) {
      this.chatFilter.add(filter);
    }

    if(this.chatFilter.indexOf(-1) == 0) {
      ChatWindowHTMLChatRenderer.setShowChatTypeCell(true);
      ChatWindowHTMLChatRenderer.renderChatDocument(this, chatMessages);
    } else {
      ChatWindowHTMLChatRenderer.setShowChatTypeCell(false);
      filteredChatMessages = (ArrayList<ChatMessage>) chatMessages.stream().filter(message -> {
        for(int filterType : this.chatFilter) {
          if(message.getType() == filterType) {
            return true;
          }

          return false;
        }

        return false;
      }).collect(Collectors.toList());

      ChatWindowHTMLChatRenderer.renderChatDocument(this, filteredChatMessages);
    }
  }

  public void setFontSize(int fontSize) {
    this.fontSize = fontSize;

    styleSheet.addRule(String.format("table { font-size: %spt; }", this.fontSize));
  }

  public void registerChatMessage(ChatMessage chatMessage) {
    chatMessages.add(chatMessage);

    if(this.chatFilter == null) return;

    if(this.chatFilter.indexOf(-1) == 0 || this.chatFilter.contains(chatMessage.getType())) {
      ChatWindowHTMLChatRenderer.appendChatMessage(this, chatMessage);
    }
  }

  private void registerTestMessages() {
    long timestamp = Calendar.getInstance().getTimeInMillis();
    for (int i = 0; i < 100; i++) {

      // Create a test message
      ChatMessage chatMessage =
          new ChatMessage(
              "Flowermate",
              "This is a message. This is a message. This is a message. This is a message. This is a message. This is a message. This is a message. This is a message.",
              timestamp,
              Client.CHAT_CHAT);

      // Register the message
      registerChatMessage(chatMessage);
    }

    ChatWindowHTMLChatRenderer.appendChatMessage(
        this, new ChatMessage("username1234", "new message", timestamp, 1));
    ChatWindowHTMLChatRenderer.appendChatMessage(
        this, new ChatMessage("username1234", "new message1235", timestamp, 1));
  }
}
