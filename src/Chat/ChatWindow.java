package Chat;

import Client.Launcher;
import Client.Logger;
import Game.Client;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ChatWindow {

  private final Dimension frameDefaultSize = new Dimension(825, 650);
  private final Dimension frameMinimumSize = new Dimension(725, 400);
  private JFrame frame;
  private JSplitPane frameSplitPane;
  private JPanel panelLeft;
  private JPanel panelRight;
  private JSplitPane panelRightSplitPane;

  // Chat UI elements
  private JPanel chatPanel;
  private ChatWindowHTMLChatView chatView;
  private JPanel inputFieldPanel;
  private JLabel inputFieldLabel;
  private JTextField inputField;
  private JScrollPane chatScrollPane;
  private Color chatBgColor = Color.decode("#282828");

  // Channel list UI elements
  private JPanel channelPanel;
  private JScrollPane channelsScrollPane;

  private JTree channelsTree;

  // Friends list UI elements
  private JPanel friendsPanel;
  private JScrollPane friendsScrollPane;
  private JList friendsList;
  private DefaultListModel<String> friendsListModel = new DefaultListModel<>();

  public ChatWindow() {
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

  public void showChatWindow() {
    frame.setVisible(true);
  }

  public void hideChatWindow() {
    frame.setVisible(false);
  }

  public void toggleChatWindow() {
    if (frame.isVisible()) {
      hideChatWindow();
    } else {
      showChatWindow();
    }
  }

  public void disposeJFrame() {
    frame.dispose();
  }

  private void initialize() {
    Logger.Info("Creating chat window");
    try {
      SwingUtilities.invokeAndWait(this::runInit);
    } catch (InvocationTargetException e) {
      Logger.Error("There was a thread-related error while setting up the chat history window!");
      e.printStackTrace();
    } catch (InterruptedException e) {
      Logger.Error(
          "There was a thread-related error while setting up the chat history window! The window may not be initialized properly!");
      e.printStackTrace();
    }
  }

  private void runInit() {
    frame = new JFrame("Chat");
    frame.setPreferredSize(frameDefaultSize);
    frame.setMinimumSize(frameMinimumSize);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    URL iconURL = Launcher.getResource("/assets/icon.png");
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      frame.setIconImage(icon.getImage());
    }

    // Setup frame layout
    Container frameContent = frame.getContentPane();

    //    frameContent.setBackground(Color.PINK);

    // Setup all panels
    BorderLayout panelLeftLayout = new BorderLayout();

    panelLeft = new JPanel();
    panelLeft.setLayout(panelLeftLayout);
    panelLeft.setMinimumSize(new Dimension(450, 0));
    //    panelLeft.setBackground(Color.RED);
    panelRight = new JPanel();
    panelRight.setLayout(new GridLayout(0, 1));
    panelRight.setMinimumSize(new Dimension(225, 0));
    //    panelRight.setBackground(Color.ORANGE);

    chatPanel = new JPanel();
    chatPanel.setLayout(new GridLayout(0, 1));
    panelLeft.add(chatPanel, BorderLayout.CENTER);

    inputFieldPanel = new JPanel();
    //        inputFieldPanel.setBackground(Color.MAGENTA);
    inputFieldPanel.setLayout(new BorderLayout());

    panelLeft.add(inputFieldPanel, BorderLayout.SOUTH);

    Font inputFieldFont = createFontBold(12);
    inputFieldLabel = new JLabel("Not logged in");
    inputFieldLabel.setFont(inputFieldFont);
    inputField = new JTextField();

    inputFieldPanel.add(inputFieldLabel, BorderLayout.WEST);
    inputFieldPanel.add(inputField, BorderLayout.CENTER);

    // Setup frame split pane
    frameSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelLeft, panelRight);
    frameSplitPane.setResizeWeight(1);

    frameContent.add(frameSplitPane);

    // Test table data
    String col[] = {"Username", "Message"};

    DefaultTableModel dtm = new DefaultTableModel(0, 2);
    dtm.setColumnIdentifiers(col);

    for (int i = 0; i < 100; i++) {
      String message =
          String.format(
              "[%d] The quick brown fox jumps over a lazy dog is the most famous pangram in English, that is the most short sentence in which all the 26 letters of the alphabet are used. For this reason, it is useful to test the graphic aspect of the fonts, because all the letters are immediately on hand.",
              i);
      dtm.addRow(new Object[] {"Flowermate:", message});
    }

    chatView = new ChatWindowHTMLChatView();

    chatScrollPane =
        new JScrollPane(
            chatView,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    chatScrollPane.getViewport().setBackground(chatBgColor);
    Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    chatScrollPane.setBorder(emptyBorder);

    // Init channel list UI elements
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("__ROOT__");
    DefaultMutableTreeNode allMessagesNode = new DefaultMutableTreeNode("All Messages");
    DefaultMutableTreeNode chatMessagesNode = new DefaultMutableTreeNode("Chat");
    DefaultMutableTreeNode questMessagesNode = new DefaultMutableTreeNode("Quest");
    DefaultMutableTreeNode privateMessagesNode = new DefaultMutableTreeNode("Private");

    // TODO: generate these dynamically
    privateMessagesNode.add(new DefaultMutableTreeNode("TestFriend1"));
    privateMessagesNode.add(new DefaultMutableTreeNode("TestFriend2"));

    DefaultMutableTreeNode otherMessagesNode = new DefaultMutableTreeNode("Other");
    rootNode.add(allMessagesNode);
    rootNode.add(chatMessagesNode);
    rootNode.add(questMessagesNode);
    rootNode.add(privateMessagesNode);
    rootNode.add(otherMessagesNode);

    channelsTree = new JTree(rootNode);
    channelsTree.setRootVisible(false);

    // Override default renderer to hide leaf icons
    DefaultTreeCellRenderer channelsTreeCellRenderer =
        (DefaultTreeCellRenderer) channelsTree.getCellRenderer();
    channelsTreeCellRenderer.setLeafIcon(null);
    channelsTreeCellRenderer.setClosedIcon(null);
    channelsTreeCellRenderer.setOpenIcon(null);

    channelsTree.setCellRenderer(channelsTreeCellRenderer);

    channelsScrollPane = new JScrollPane(channelsTree);

    // Init friends list UI elements
    friendsList = new JList();
    friendsList.setLayoutOrientation(JList.VERTICAL);
    friendsList.setModel(friendsListModel);
    friendsScrollPane = new JScrollPane(friendsList);

    // -- Add UI elements to frame content --

    // Add chat
    chatPanel.add(chatScrollPane);

    int rightPanelsMinHeight = 125;

    channelPanel = new JPanel();
    channelPanel.setLayout(new BorderLayout());
    channelPanel.setMinimumSize(new Dimension(0, rightPanelsMinHeight));

    // Create padding border for labels
    EmptyBorder labelBorder = createPaddingBorder(4);

    Font headerFont = createFontBold(14);

    // Add channels label and make it bold
    JLabel channelPanelLabel = new JLabel("Channels");
    channelPanelLabel.setBorder(labelBorder);

    channelPanelLabel.setFont(headerFont);
    channelPanel.add(channelPanelLabel, BorderLayout.NORTH);

    friendsPanel = new JPanel();
    friendsPanel.setLayout(new BorderLayout());
    friendsPanel.setMinimumSize(new Dimension(0, rightPanelsMinHeight));

    // Add friends label and make it bold
    JLabel friendsPanelLabel = new JLabel("Friends");
    friendsPanelLabel.setBorder(labelBorder);
    friendsPanelLabel.setFont(headerFont);
    friendsPanel.add(friendsPanelLabel, BorderLayout.NORTH);

    panelRightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, channelPanel, friendsPanel);
    panelRightSplitPane.setResizeWeight(.5);
    panelRight.add(panelRightSplitPane);

    SwingUtilities.invokeLater(
        () -> {
          frameSplitPane.setDividerLocation(.65);
          panelRightSplitPane.setDividerLocation(.5);
        });

    // Add channels list
    channelPanel.add(channelsScrollPane);

    // Add friends list
    friendsPanel.add(friendsScrollPane);
  }

  public void registerChatMessage(String username, String message, int type) {
    if (username == null) {
      return;
    }

    long timestamp = Calendar.getInstance().getTimeInMillis();
    ChatMessage chatMessage = new ChatMessage(username, message, timestamp, type);

    chatView.registerChatMessage(chatMessage);
  }

  public void updatePlayerName() {
    String playerName = Client.player_name;
    if (playerName == null || playerName.equals("")) {
      inputFieldLabel.setText("Not logged in");
    } else {
      inputFieldLabel.setText(playerName);
    }
  }

  public void updateFriendsList() {
    clearFriendsList();

    for (int i = 0; i < Client.friends_count; i++) {
      String friendName = Client.friends[i];
      boolean isOnline = Client.friends_online[i] == 6; // online
      if (friendName == null || friendName.equals("Global$") || !isOnline) {
        continue;
      }

      friendsListModel.addElement(friendName);
    }
  }

  public void clearFriendsList() {
    friendsListModel.clear();
  }

  private Font createFont() {
    return new JLabel().getFont();
  }

  private Font createFont(float fontSize) {
    Font font = createFont();
    return font.deriveFont(font.getStyle(), fontSize);
  }

  private Font createFontBold(float fontSize) {
    Font font = createFont();
    return font.deriveFont(font.getStyle() | Font.BOLD, fontSize);
  }

  private EmptyBorder createPaddingBorder(int padding) {
    return new EmptyBorder(new Insets(padding, padding, padding, padding));
  }

  private EmptyBorder createPaddingBorder(
      int paddingTop, int paddingLeft, int paddingBottom, int paddingRight) {
    return new EmptyBorder(new Insets(paddingTop, paddingLeft, paddingBottom, paddingRight));
  }
}
