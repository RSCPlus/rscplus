package Client;

import Game.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ChatWindow {
    private final JFrame frame;
    private final GridBagLayout layout;
    private final GridBagConstraints layoutConstraints;

    // Filter buttons
    private final JPanel filterButtonsPanel;
    public final JRadioButton allFilterButton;
    public final JRadioButton chatFilterButton;
    public final JRadioButton questFilterButton;
    public final JRadioButton privateFilterButton;
    private final ButtonGroup filterButtonGroup;
    private final ChatWindowFilterListener filterButtonListener;

    private final JScrollPane chatScrollPane;
    private final JTextPane chatTextPane;

    private int selectedFilter = -1;

    private final int MAX_CHAT_SIZE = 256;
    private final ArrayList<ChatMessage> chatMessages;
    private final HashMap<String, Color> usernameColors;

    public ChatWindow() {
        // Init the frame
        frame = new JFrame("Chat");
        frame.pack();
        frame.setAlwaysOnTop(false);
        frame.setSize(new Dimension(512, 512));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the layout
        layout = new GridBagLayout();
        layoutConstraints = new GridBagConstraints();
        Container content = frame.getContentPane();
        content.setLayout(layout);
        content.setBackground(Color.WHITE);

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

        // Add chat messages list
        chatTextPane = new JTextPane();
        chatTextPane.setEditable(false);

        int padding = 2;
        EmptyBorder eb = new EmptyBorder(new Insets(padding, padding, padding, padding));

        chatScrollPane = new JScrollPane(chatTextPane);
        chatScrollPane.getViewport().setBackground(Color.WHITE);
        chatScrollPane.setBorder(eb);

        layoutConstraints.anchor = GridBagConstraints.NORTH;
        layoutConstraints.fill = GridBagConstraints.BOTH;
        layoutConstraints.weighty = 1;
        layoutConstraints.weightx = 1;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 1;
        content.add(chatScrollPane, layoutConstraints);

        chatMessages = new ArrayList<ChatMessage>();
        usernameColors = new HashMap<>();
    }

    public void show() {
        frame.setVisible(true);
    }

    public void hide() {
        frame.setVisible(false);
    }

    public void setSelectedFilter(int selectedFilter) {
        this.selectedFilter = selectedFilter;

        System.out.println("selected filter: " + this.selectedFilter);
    }

    public void registerChatMessage(String username, String message, int type) {
        if (type != Client.CHAT_CHAT && type != Client.CHAT_QUEST && type != Client.CHAT_PRIVATE) return;

        ChatMessage chatMessage = new ChatMessage(username, message, type);

        if (chatMessages.size() == MAX_CHAT_SIZE) {
            int lastIndex = chatMessages.size() - 1;
            chatMessages.remove(lastIndex);
        }

        chatMessages.add(chatMessage);
        appendMessageToChat(chatMessage);

        System.out.println(String.format("GOT MESSAGE - [%s]: %s", username, message));
    }

    private void appendMessageToChat(ChatMessage chatMessage) {
        String username = chatMessage.getUsername();
        String message = chatMessage.getMessage();
        Document doc = chatTextPane.getDocument();

        if (username == null && (message.contains("Welcome to") || message.contains("for a list of commands") || message.contains("Open the settings by"))) {
            return;
        }

        if (username != null) {
            // Add username
            Color usernameColor = usernameColors.get(username);
            if (usernameColor == null) {
                usernameColor = generateUsernameColor();
                usernameColors.put(username, usernameColor);
            }

            SimpleAttributeSet usernameAttribSet = new SimpleAttributeSet();
            usernameAttribSet.addAttribute(StyleConstants.CharacterConstants.Bold, true);
            usernameAttribSet.addAttribute(StyleConstants.Foreground, usernameColor);

            int len = doc.getLength();
            System.out.println("Chat text pane document length: " + len);

            try {
                doc.insertString(len, username + ": ", usernameAttribSet);
            } catch (Exception ex) {
                System.out.println("Failed to print username: \"" + username + "\"");
//                throw new RuntimeException(ex);
            }


            System.out.println("Added username to message list. Color:" + usernameColor.toString());
        }


        if (message != null) {
            SimpleAttributeSet messageAttribSet = new SimpleAttributeSet();
            int len = doc.getLength();

            try {
                doc.insertString(len, message + "\n", messageAttribSet);
            } catch (Exception ex) {
                System.out.println("Failed to print message: \"" + message + "\"");
            }
        }
    }

    private Color generateUsernameColor() {
        Random random = new Random();

        final float hue = random.nextFloat();
        // Saturation between 0.1 and 0.3
        final float saturation = (random.nextInt(2000) + 2000) / 10000f;
        final float luminance = 0.65f;
        final Color color = Color.getHSBColor(hue, saturation, luminance);

        return color;
    }
}

class ChatMessage {
    private final String username;
    private final String message;
    private final int type;

    public ChatMessage(String username, String message, int type) {
        this.username = username;
        this.message = message;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }
}

class ChatWindowFilterListener implements ItemListener {
    private final ChatWindow chatWindow;

    public ChatWindowFilterListener(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        int stateChange = e.getStateChange();
        if (stateChange == ItemEvent.SELECTED) {
            JRadioButton button = (JRadioButton) e.getItem();

            if (button == chatWindow.allFilterButton) {
                chatWindow.setSelectedFilter(-1);
            } else if (button == chatWindow.chatFilterButton) {
                chatWindow.setSelectedFilter(Client.CHAT_CHAT);
            } else if (button == chatWindow.questFilterButton) {
                chatWindow.setSelectedFilter(Client.CHAT_QUEST);
            } else if (button == chatWindow.privateFilterButton) {
                chatWindow.setSelectedFilter(Client.CHAT_PRIVATE);
            }
        }
    }
}
