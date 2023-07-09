package Client;

import Game.Client;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.text.Document;

public class ChatHistoryWindow {
  private JFrame frame;
  private GridBagLayout layout;
  private GridBagConstraints layoutConstraints;

  private Color bgColor = Color.decode("#282828");

  // Filter buttons
  private JPanel filterButtonsPanel;
  public JRadioButton allFilterButton;

  public JRadioButton chatFilterButton;
  public JRadioButton questFilterButton;
  public JRadioButton privateFilterButton;
  private ButtonGroup filterButtonGroup;
  private ChatWindowFilterListener filterButtonListener;

  private JScrollPane chatScrollPane;
  private JTextPane chatTextPane;

  private int selectedFilter = -1;

  private ArrayList<ChatMessage> allChatMessages;
  private ArrayList<ChatMessage> filteredChatMessages;

  public ChatHistoryWindow() {
    try {
      // Set System L&F as a fall-back option.
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          NimbusLookAndFeel laf = (NimbusLookAndFeel) UIManager.getLookAndFeel();
          laf.getDefaults().put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 11));
          break;
        }
      }
    } catch (UnsupportedLookAndFeelException e) {
      Logger.Error("Unable to set L&F: Unsupported look and feel");
    } catch (ClassNotFoundException e) {
      Logger.Error("Unable to set L&F: Class not found");
    } catch (InstantiationException e) {
      Logger.Error("Unable to set L&F: Class object cannot be instantiated");
    } catch (IllegalAccessException e) {
      Logger.Error("Unable to set L&F: Illegal access exception");
    }

    initialize();
  }

  public void showChatHistoryWindow() {
    frame.setVisible(true);
  }

  public void hideChatHistoryWindow() {
    frame.setVisible(false);
  }

  public void toggleChatHistoryWindow() {
    if (frame.isVisible()) {
      hideChatHistoryWindow();
    } else {
      showChatHistoryWindow();
    }
  }

  public void disposeJFrame() {
    frame.dispose();
  }

  public void setSelectedFilter(int filter) {
    if (filter == selectedFilter) return; // Do nothing if filter already selected

    selectedFilter = filter;

    if (selectedFilter == -1) {
      // All messages
      renderChatMessages(allChatMessages, true);
      return;
    }

    // Build list of filtered chat messages
    filteredChatMessages = new ArrayList<>();
    for (ChatMessage chatMessage : allChatMessages) {
      if (chatMessage.getType() == selectedFilter) {
        filteredChatMessages.add(chatMessage);
      }
    }

    renderChatMessages(filteredChatMessages, false);
  }

  public void registerChatMessage(String username, String message, int type) {
    // Filter out other types of messages
    if (type != Client.CHAT_CHAT && type != Client.CHAT_QUEST && type != Client.CHAT_PRIVATE) {
      return;
    }

    // Filter out RSC+ messages
    if (username == null
        && (message.contains("Welcome to")
            || message.contains("for a list of commands")
            || message.contains("Open the settings by"))) {
      return;
    }

    // Remove color tags
    //        message = ChatMessageFormatter.removeTagsFromMessage(message);

    // Handle npc chats
    if (username == null && type == Client.CHAT_QUEST) {
      int colonLoc = message.indexOf(":");
      if (colonLoc > -1) {
        username = message.substring(0, colonLoc - 1);
        message = message.substring(colonLoc + 2);
      }
    }

    // Handle null username
    if (username == null) {
      username = "";
    }

    // Handle null message
    if (message == null) {
      message = "";
    }

    ChatMessage chatMessage = new ChatMessage(username, message, type);

    // Add this message to the list of all messages.
    allChatMessages.add(chatMessage);

    if (selectedFilter == -1 || type == selectedFilter) {
      Document document = chatTextPane.getDocument();
      ChatMessageRenderer.renderChatMessage(document, chatMessage, selectedFilter == -1);
    }

    scrollToBottomIfNecessary();
  }

  private void renderChatMessages(ArrayList<ChatMessage> chatMessages, boolean showChatTypeLabel) {
    Document document = ChatMessageRenderer.renderChatMessages(chatMessages, showChatTypeLabel);
    chatTextPane.setDocument(document);
    chatTextPane.revalidate();

    scrollToBottom();
  }

  private void scrollToBottom() {
    SwingUtilities.invokeLater(
        () -> {
          JScrollBar verticalScrollBar = chatScrollPane.getVerticalScrollBar();
          verticalScrollBar.revalidate();
          int maxScrollPosition = verticalScrollBar.getMaximum();
          verticalScrollBar.setValue(maxScrollPosition);
        });
  }

  private void scrollToBottomIfNecessary() {
    SwingUtilities.invokeLater(
        () -> {
          JScrollBar verticalScrollBar = chatScrollPane.getVerticalScrollBar();
          int scrollPosition = verticalScrollBar.getValue();
          int margin = 300;
          int totalScrollPosition = scrollPosition + verticalScrollBar.getVisibleAmount();
          int maxScroll = verticalScrollBar.getMaximum();
          int threshold = maxScroll - margin;

          if (totalScrollPosition >= threshold) {
            verticalScrollBar.setValue(maxScroll);
          } else {
            verticalScrollBar.setValue(scrollPosition);
          }
        });
  }

  private void runInit() {
    // Setup the frame
    frame = new JFrame("Chat History");

    Container content = frame.getContentPane();
    content.setPreferredSize(new Dimension(512, 346));

    frame.pack();
    frame.setMinimumSize(new Dimension(325, 150));
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    URL iconURL = Launcher.getResource("/assets/icon.png");
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      frame.setIconImage(icon.getImage());
    }

    // Create the layout
    layout = new GridBagLayout();
    layoutConstraints = new GridBagConstraints();
    content.setLayout(layout);
    content.setBackground(bgColor);

    // Create filter radio buttons
    allFilterButton = new JRadioButton("All");
    chatFilterButton = new JRadioButton("Chat");
    questFilterButton = new JRadioButton("Quest");
    privateFilterButton = new JRadioButton("Private");

    // Add listener
    filterButtonListener = new ChatWindowFilterListener(this);
    allFilterButton.addItemListener(filterButtonListener);
    chatFilterButton.addItemListener(filterButtonListener);
    questFilterButton.addItemListener(filterButtonListener);
    privateFilterButton.addItemListener(filterButtonListener);

    // Add radio buttons to a button group
    filterButtonGroup = new ButtonGroup();
    filterButtonGroup.add(allFilterButton);
    filterButtonGroup.add(chatFilterButton);
    filterButtonGroup.add(questFilterButton);
    filterButtonGroup.add(privateFilterButton);

    // Select "All" by default
    filterButtonGroup.setSelected(allFilterButton.getModel(), true);

    // Add radio buttons to the frame
    filterButtonsPanel = new JPanel();

    layoutConstraints.anchor = GridBagConstraints.NORTH;
    layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
    layoutConstraints.weightx = 1;
    layoutConstraints.weighty = 0;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = 0;
    content.add(filterButtonsPanel, layoutConstraints);

    filterButtonsPanel.add(allFilterButton);
    filterButtonsPanel.add(chatFilterButton);
    filterButtonsPanel.add(questFilterButton);
    filterButtonsPanel.add(privateFilterButton);

    // Workaround for changing JTextPane background color
    //    UIManager.put(
    //        "TextPane[Enabled].backgroundPainter",
    //        (Painter<JComponent>)
    //            (g, comp, width, height) -> {
    //              g.setColor(bgColor);
    //              g.fillRect(0, 0, width, height);
    //            });

    // Add chat messages list
    chatTextPane = new JTextPane();
    chatTextPane.setEditable(false);
    chatTextPane.setOpaque(true);

    int padding = 2;
    EmptyBorder eb = new EmptyBorder(new Insets(padding, padding, padding, padding));

    chatScrollPane = new JScrollPane(chatTextPane);
    chatScrollPane.setBorder(eb);

    layoutConstraints.anchor = GridBagConstraints.NORTH;
    layoutConstraints.fill = GridBagConstraints.BOTH;
    layoutConstraints.weighty = 1;
    layoutConstraints.weightx = 1;
    layoutConstraints.gridx = 0;
    layoutConstraints.gridy = 1;
    content.add(chatScrollPane, layoutConstraints);

    allChatMessages = new ArrayList<>();
  }

  private void initialize() {
    Logger.Info("Creating chat history window");
    try {
      SwingUtilities.invokeAndWait(
          new Runnable() {

            @Override
            public void run() {
              runInit();
            }
          });
    } catch (InvocationTargetException e) {
      Logger.Error("There was a thread-related error while setting up the chat history window!");
      e.printStackTrace();
    } catch (InterruptedException e) {
      Logger.Error(
          "There was a thread-related error while setting up the chat history window! The window may not be initialized properly!");
      e.printStackTrace();
    }
  }
}

class ChatWindowFilterListener implements ItemListener {
  private final ChatHistoryWindow chatHistoryWindow;

  public ChatWindowFilterListener(ChatHistoryWindow chatHistoryWindow) {
    this.chatHistoryWindow = chatHistoryWindow;
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    int stateChange = e.getStateChange();
    if (stateChange == ItemEvent.SELECTED) {
      JRadioButton button = (JRadioButton) e.getItem();

      if (button == chatHistoryWindow.allFilterButton) {
        // All
        chatHistoryWindow.setSelectedFilter(-1);
      } else if (button == chatHistoryWindow.chatFilterButton) {
        // Chat
        chatHistoryWindow.setSelectedFilter(Client.CHAT_CHAT);
      } else if (button == chatHistoryWindow.questFilterButton) {
        // Quest
        chatHistoryWindow.setSelectedFilter(Client.CHAT_QUEST);
      } else if (button == chatHistoryWindow.privateFilterButton) {
        // Private
        chatHistoryWindow.setSelectedFilter(Client.CHAT_PRIVATE);
      }
    }
  }
}
