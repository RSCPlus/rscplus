package Client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ConfigWindow {

	private JFrame frame;
	
	private JLabel generalPanelPatchDesc;
	private JSlider generalPanelPatchSlider;
	
	private JCheckBox notificationPanelTrayPopupCheckbox;
	private JRadioButton notificationPanelClientFocusButton;
	private JRadioButton notificationPanelAnyFocusButton;
	
	/**
	 * Launch the application.
	 */


	/**
	 * Create the application.
	 */
	public ConfigWindow() {
		
	    try {
            // Set System L&F
    		UIManager.setLookAndFeel(
			UIManager.getSystemLookAndFeelClassName());
	    	} 
	    	catch (UnsupportedLookAndFeelException e) {
	    		// handle exception
	    	}
	    	catch (ClassNotFoundException e) {
    			// handle exception
	    	}
	    	catch (InstantiationException e) {
	    		// handle exception
	    	}
	    	catch (IllegalAccessException e) {
	    		// handle exception
	    	}
	    
		initialize();
	}
	
	public void showConfigWindow() {
		frame.setVisible(true);
	}
	
	public void hideConfigWindow() {
		frame.setVisible(false);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Settings");
		frame.setBounds(100, 100, 410, 540);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		//Container declarations
		/** The tabbed pane holding the five configuration tabs*/
		JTabbedPane tabbedPane = new JTabbedPane();
		/**The JPanel containing the OK, Cancel, Apply, and Restore Defaults buttons at the bottom of the window*/
		JPanel navigationPanel = new JPanel();

		JScrollPane generalScrollPane = new JScrollPane();
		JScrollPane overlayScrollPane = new JScrollPane();
		JScrollPane notificationScrollPane = new JScrollPane();
		JScrollPane streamingScrollPane = new JScrollPane();
		JScrollPane keybindScrollPane = new JScrollPane();

		JPanel generalPanel = new JPanel();
		JPanel overlayPanel = new JPanel();
		JPanel notificationPanel = new JPanel();
		JPanel streamingPanel = new JPanel();
		JPanel keybindPanel = new JPanel();

		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		frame.getContentPane().add(navigationPanel, BorderLayout.PAGE_END);

		tabbedPane.addTab("General", null, generalScrollPane, null);
			generalScrollPane.setViewportView(generalPanel);
		tabbedPane.addTab("Overlays", null, overlayScrollPane, null);
			overlayScrollPane.setViewportView(overlayPanel);
		tabbedPane.addTab("Notifications", null, notificationScrollPane, null);
			notificationScrollPane.setViewportView(notificationPanel);
		tabbedPane.addTab("Streaming & Privacy", null, streamingScrollPane, null);
			streamingScrollPane.setViewportView(streamingPanel);
		tabbedPane.addTab("Keybinds", null, keybindScrollPane, null);
			keybindScrollPane.setViewportView(keybindPanel);
		
		//Adding padding for aesthetics
		navigationPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 10, 10));
		generalPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		overlayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		notificationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		streamingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		keybindPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
		setScrollSpeed(generalScrollPane, 20, 15);
		setScrollSpeed(overlayScrollPane, 20, 15);
		setScrollSpeed(notificationScrollPane, 20, 15);
		setScrollSpeed(streamingScrollPane, 20, 15);
		setScrollSpeed(keybindScrollPane, 20, 15);
			
		/*
		 * Navigation buttons
		 */
		
		navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.X_AXIS));
		
        addButton("OK", navigationPanel, Component.LEFT_ALIGNMENT).addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("OK");
				
			}
		});
        
        addButton("Cancel", navigationPanel, Component.LEFT_ALIGNMENT).addActionListener(new ActionListener() {
        	
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Cancel");
				
			}
		});
        
        addButton("Apply", navigationPanel, Component.LEFT_ALIGNMENT).addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Apply");
				
			}
		});
        
        navigationPanel.add(Box.createHorizontalGlue());
        addButton("Restore Defaults", navigationPanel, Component.RIGHT_ALIGNMENT).addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Restore Defaults");
				
			}
		});
		
		/*
		 * General tab
		 */
		
		generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));
		
		JPanel generalPanelRes = new JPanel();
		generalPanel.add(generalPanelRes);
		generalPanelRes.setLayout(new BoxLayout(generalPanelRes, BoxLayout.X_AXIS));
		generalPanelRes.setPreferredSize(new Dimension(0,37));
		generalPanelRes.setAlignmentX(Component.LEFT_ALIGNMENT);
			addCheckbox("Default client size:", generalPanelRes);
			JSpinner generalPanelResX = new JSpinner();
			generalPanelRes.add(generalPanelResX);
			generalPanelResX.setMaximumSize(new Dimension(48,20));
			generalPanelResX.setMinimumSize(new Dimension(48,20));
			generalPanelResX.setAlignmentY((float) 0.75);
			JLabel generalPanelResBy = new JLabel("x");
			generalPanelRes.add(generalPanelResBy);
			generalPanelResBy.setAlignmentY((float) 0.9);
			generalPanelResBy.setBorder(new EmptyBorder(0,2,0,2));
			JSpinner generalPanelResY = new JSpinner();
			generalPanelRes.add(generalPanelResY);
			generalPanelResY.setMaximumSize(new Dimension(48,20));
			generalPanelResY.setMinimumSize(new Dimension(48,20));
			generalPanelResY.setAlignmentY((float) 0.75);
			//Sanitize JSpinner values
			SpinnerNumberModel spinnerWinXModel = new SpinnerNumberModel();
			spinnerWinXModel.setMinimum(512);
			spinnerWinXModel.setValue(512);
			spinnerWinXModel.setStepSize(10);
			generalPanelResX.setModel(spinnerWinXModel);
			SpinnerNumberModel spinnerWinYModel = new SpinnerNumberModel();
			spinnerWinYModel.setMinimum(346);
			spinnerWinYModel.setValue(346);
			spinnerWinYModel.setStepSize(10);
			generalPanelResY.setModel(spinnerWinYModel);
		
		addCheckbox("Load chat history after relogging", generalPanel);
		addCheckbox("Combat XP menu always on", generalPanel);
		addCheckbox("XP drops", generalPanel);
		addCheckbox("Fatigue drops", generalPanel);
		
		JPanel generalPanelFatFigs = new JPanel();
		generalPanel.add(generalPanelFatFigs);
		generalPanelFatFigs.setLayout(new BoxLayout(generalPanelFatFigs, BoxLayout.X_AXIS));
		generalPanelFatFigs.setPreferredSize(new Dimension(0,37));
		generalPanelFatFigs.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelFatFigs.setLayout(new BoxLayout(generalPanelFatFigs, BoxLayout.X_AXIS));
			JLabel generalPanelFatFigsLabel = new JLabel("Fatigue figures:");
			generalPanelFatFigs.add(generalPanelFatFigsLabel);
			generalPanelFatFigsLabel.setAlignmentY((float) 0.9);
			JSpinner generalPanelFatFigsSpinner = new JSpinner();
			generalPanelFatFigs.add(generalPanelFatFigsSpinner);
			generalPanelFatFigsSpinner.setMaximumSize(new Dimension(35,20));
			generalPanelFatFigsSpinner.setAlignmentY((float) 0.7);
			generalPanelFatFigs.setBorder(new EmptyBorder(0,0,7,0));
			//Sanitize JSpinner values
			SpinnerNumberModel spinnerNumModel = new SpinnerNumberModel();
			spinnerNumModel.setMinimum(1);
			spinnerNumModel.setMaximum(7);
			spinnerNumModel.setValue(2);
			generalPanelFatFigsSpinner.setModel(spinnerNumModel);
			
		addCheckbox("Fatigue alert", generalPanel);
		
		JPanel generalPanelPatch = new JPanel();
		generalPanelPatch.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelPatch.setMaximumSize(new Dimension(300,60));
		generalPanelPatch.setLayout(new BoxLayout(generalPanelPatch, BoxLayout.X_AXIS));
		generalPanel.add(generalPanelPatch);
		
		generalPanelPatchSlider = new JSlider();
		generalPanelPatchSlider.setMajorTickSpacing(1);
		generalPanelPatchSlider.setPaintLabels(true);
		generalPanelPatchSlider.setPaintTicks(true);
		generalPanelPatchSlider.setSnapToTicks(true);
		generalPanelPatchSlider.setMinimum(0);
		generalPanelPatchSlider.setMaximum(3);
		generalPanelPatchSlider.setPreferredSize(new Dimension(33,0));
		generalPanelPatchSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelPatchSlider.setBorder(new EmptyBorder(0, 0, 5, 0));
		generalPanelPatchSlider.setOrientation(SwingConstants.VERTICAL);
		generalPanelPatch.add(generalPanelPatchSlider);
		
		JPanel generalPanelPatchTextPanel = new JPanel();
		generalPanelPatchTextPanel.setPreferredSize(new Dimension(255,55));
		generalPanelPatchTextPanel.setLayout(new BorderLayout());
		generalPanelPatchTextPanel.setBorder(new EmptyBorder(0,10,0,0));
		generalPanelPatch.add(generalPanelPatchTextPanel);
		
		JLabel generalPanelPatchTitle = new JLabel("<html><b>Name patch mode</b> (Requires restart)</html>");
		generalPanelPatchTextPanel.add(generalPanelPatchTitle, BorderLayout.PAGE_START);
		//TODO: Remove hard-coded generalPanelPatchDesc text from JLabel
		generalPanelPatchDesc = new JLabel("<html>Reworded vague stuff to be more descriptive on top of type 1 & 2 changes</html>");
		generalPanelPatchTextPanel.add(generalPanelPatchDesc, BorderLayout.CENTER);
		
		generalPanelPatchSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				switch (generalPanelPatchSlider.getValue()) {
				case 3:
					generalPanelPatchDesc.setText("<html>Reworded vague stuff to be more descriptive on top of type 1 & 2 changes</html>");
					break;
				case 2:
					generalPanelPatchDesc.setText("<html>Capitalizations and fixed spellings on top of type 1 changes</html>");
					break;
				case 1:
					generalPanelPatchDesc.setText("<html>Purely practical name changes (potion dosages, unidentified herbs, unfinished potions)</html>");
					break;
				case 0:
					generalPanelPatchDesc.setText("<html>No item name patching</html>");
					break;
				default:
					System.err.println("Invalid name patch mode value");
					break;
				}
			}
		});
		
		addCheckbox("Roof hiding", generalPanel);
		addCheckbox("Color coded text", generalPanel);
		addCheckbox("Increase FoV", generalPanel);
		addCheckbox("Use custom cursor (Requires restart)", generalPanel);
        
		JLabel generalPanelViewDistanceLabel = new JLabel("View distance");
		generalPanel.add(generalPanelViewDistanceLabel);
		generalPanelViewDistanceLabel.setAlignmentY((float) 0.9);
		JSlider generalPanelViewDistanceSlider = new JSlider();
		
		generalPanel.add(generalPanelViewDistanceSlider);
		generalPanelViewDistanceSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelViewDistanceSlider.setMaximumSize(new Dimension(200,30));
		generalPanelViewDistanceSlider.setBorder(new EmptyBorder(10, 0, 0, 0));
		generalPanelViewDistanceSlider.setMinorTickSpacing(500);
		generalPanelViewDistanceSlider.setMajorTickSpacing(1000);
		generalPanelViewDistanceSlider.setMinimum(2300);
		generalPanelViewDistanceSlider.setMaximum(10000);
		generalPanelViewDistanceSlider.setPaintTicks(true);
		
		/*
		 * Overlay tab
		 */
		overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
		
		addCheckbox("Show HP/Prayer/Fatigue display", overlayPanel);
		addCheckbox("Display inventory count", overlayPanel);
		addCheckbox("Display item name overlay", overlayPanel);
		addCheckbox("Show player name overlay", overlayPanel);
		addCheckbox("Show nearby friend name overlay", overlayPanel);
		addCheckbox("Display NPC name overlay", overlayPanel);
		addCheckbox("Show NPC hitboxes", overlayPanel);
		addCheckbox("Show food healing overlay", overlayPanel);
		addCheckbox("Display time until next HP regeneration", overlayPanel);
		addCheckbox("Enable debug mode", overlayPanel);
		
		/*
		 * Notification tab
		 */
		
		notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
		
		addCheckbox("Enable PM notifications", notificationPanel);
		addCheckbox("Enable trade notifications", notificationPanel);
		addCheckbox("Enable duel notifications", notificationPanel);
		addCheckbox("Enable logout notification", notificationPanel);
		
		JPanel notificationPanelLowHP = new JPanel();
		notificationPanel.add(notificationPanelLowHP);
		notificationPanelLowHP.setLayout(new BoxLayout(notificationPanelLowHP, BoxLayout.X_AXIS));
		notificationPanelLowHP.setPreferredSize(new Dimension(0,37));
		notificationPanelLowHP.setAlignmentX(Component.LEFT_ALIGNMENT);
		addCheckbox("Enable low HP notification at", notificationPanelLowHP);
			JSpinner lowHP = new JSpinner();
			lowHP.setMaximumSize(new Dimension(35,20));
			lowHP.setMinimumSize(new Dimension(35,20));
			lowHP.setAlignmentY((float) 0.75);
			notificationPanelLowHP.add(lowHP);
			//Sanitize JSpinner values
			SpinnerNumberModel spinnerHPNumModel = new SpinnerNumberModel();
			spinnerHPNumModel.setMinimum(1);
			spinnerHPNumModel.setMaximum(99);
			spinnerHPNumModel.setValue(25);
			lowHP.setModel(spinnerHPNumModel);
			JLabel lowHPEnd = new JLabel("% HP");
			notificationPanelLowHP.add(lowHPEnd);
			lowHPEnd.setAlignmentY((float) 0.9);
			lowHPEnd.setBorder(new EmptyBorder(0,2,0,0));
		
		addCheckbox("Enable notification sounds", notificationPanel);
		notificationPanelTrayPopupCheckbox = addCheckbox("Enable notification tray popups", notificationPanel);
		notificationPanelTrayPopupCheckbox.setBorder(BorderFactory.createEmptyBorder(0,0,7,0));
		
		ButtonGroup trayPopupButtonGroup = new ButtonGroup();
		notificationPanelClientFocusButton = addRadioButton("Only when client is not focused", notificationPanel, 20);
		notificationPanelAnyFocusButton = addRadioButton("Regardless of client focus", notificationPanel, 20);
		trayPopupButtonGroup.add(notificationPanelClientFocusButton);
		trayPopupButtonGroup.add(notificationPanelAnyFocusButton);

		notificationPanelTrayPopupCheckbox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(notificationPanelTrayPopupCheckbox.isSelected()) {
					notificationPanelClientFocusButton.setEnabled(true);
					notificationPanelAnyFocusButton.setEnabled(true);
				} else {
					notificationPanelClientFocusButton.setEnabled(false);
					notificationPanelAnyFocusButton.setEnabled(false);
				}
				
			}
		});
		
		/* 
		 * Streaming & Privacy tab
		 */
		
		streamingPanel.setLayout(new BoxLayout(streamingPanel, BoxLayout.Y_AXIS));
		
		addCheckbox("Enable Twitch chat", streamingPanel);
		
		JPanel twitchInGameName = new JPanel();
		streamingPanel.add(twitchInGameName);
		twitchInGameName.setLayout(new BoxLayout(twitchInGameName, BoxLayout.X_AXIS));
		twitchInGameName.setPreferredSize(new Dimension(0,37));
		twitchInGameName.setAlignmentX(Component.LEFT_ALIGNMENT);
		twitchInGameName.setBorder(new EmptyBorder(0,0,9,0));
			JLabel twitchInGameNameLabel = new JLabel("Twitch name to display in game: ");
			twitchInGameName.add(twitchInGameNameLabel);
			twitchInGameNameLabel.setAlignmentY((float) 0.9);
			JTextField twitchInGameNameTextField = new JTextField("");
			twitchInGameName.add(twitchInGameNameTextField);
			twitchInGameNameTextField.setMinimumSize(new Dimension(100,20));
			twitchInGameNameTextField.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
			twitchInGameNameTextField.setAlignmentY((float) 0.75);
			
		JPanel twitchOAuth = new JPanel();
		streamingPanel.add(twitchOAuth);
		twitchOAuth.setLayout(new BoxLayout(twitchOAuth, BoxLayout.X_AXIS));
		twitchOAuth.setPreferredSize(new Dimension(0,37));
		twitchOAuth.setAlignmentX(Component.LEFT_ALIGNMENT);
		twitchOAuth.setBorder(new EmptyBorder(0,0,9,0));
			JLabel twitchOAuthLabel = new JLabel("Twitch OAuth token: ");
			twitchOAuth.add(twitchOAuthLabel);
			twitchOAuthLabel.setAlignmentY((float) 0.9);
			JPasswordField twitchOAuthTextField = new JPasswordField("");
			twitchOAuth.add(twitchOAuthTextField);
			twitchOAuthTextField.setMinimumSize(new Dimension(100,20));
			twitchOAuthTextField.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
			twitchOAuthTextField.setAlignmentY((float) 0.75);
			
		JPanel twitchUser = new JPanel();
		streamingPanel.add(twitchUser);
		twitchUser.setLayout(new BoxLayout(twitchUser, BoxLayout.X_AXIS));
		twitchUser.setPreferredSize(new Dimension(0,37));
		twitchUser.setAlignmentX(Component.LEFT_ALIGNMENT);
		twitchUser.setBorder(new EmptyBorder(0,0,9,0));
			JLabel twitchUserLabel = new JLabel("Twitch username: ");
			twitchUser.add(twitchUserLabel);
			twitchUserLabel.setAlignmentY((float) 0.9);
			JTextField twitchUserTextField = new JTextField("");
			twitchUser.add(twitchUserTextField);
			twitchUserTextField.setMinimumSize(new Dimension(100,20));
			twitchUserTextField.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
			twitchUserTextField.setAlignmentY((float) 0.75);
		
		addCheckbox("Enable IP/DNS details at login screen", streamingPanel);
		addCheckbox("Save login information between logins", streamingPanel);
		
		/*
		 * Keybind tab
		 */
		
		//TODO: Make the contents top aligned
		keybindPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		keybindPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		GridBagLayout gbl_panel = new GridBagLayout();
		
		keybindPanel.setLayout(gbl_panel);

		addKeybindSet(keybindPanel, "Logout", "Ctrl + L");
		addKeybindSet(keybindPanel, "Take screenshot", "Ctrl + S");
		addKeybindSet(keybindPanel, "Toggle color coded text", "None");
		addKeybindSet(keybindPanel, "Toggle combat XP menu persistence", "Ctrl + C");
		addKeybindSet(keybindPanel, "Toggle debug mode", "Ctrl + D");
		addKeybindSet(keybindPanel, "Toggle fatigue alert", "Ctrl + F");
		addKeybindSet(keybindPanel, "Toggle fatigue drops", "Ctrl + ]");
		addKeybindSet(keybindPanel, "Toggle food heal overlay", "None");
		addKeybindSet(keybindPanel, "Toggle friend name overlay", "Ctrl + F");
		addKeybindSet(keybindPanel, "Toggle HP/prayer/fatigue display", "None");
		addKeybindSet(keybindPanel, "Toggle inventory count overlay", "None");
		addKeybindSet(keybindPanel, "Toggle IP/DNS shown at login screen", "None");
		addKeybindSet(keybindPanel, "Toggle item name overlay", "Ctrl + I");
		addKeybindSet(keybindPanel, "Toggle NPC hitboxes", "Ctrl + H");
		addKeybindSet(keybindPanel, "Toggle NPC name overlay", "Ctrl + N");
		addKeybindSet(keybindPanel, "Toggle player name overlay", "Ctrl + P");
		addKeybindSet(keybindPanel, "Toggle roof hiding", "Ctrl + R");
		addKeybindSet(keybindPanel, "Toggle save login information", "None");
		addKeybindSet(keybindPanel, "Toggle time until health regen", "None");
		addKeybindSet(keybindPanel, "Toggle Twitch chat", "Ctrl + T");
		addKeybindSet(keybindPanel, "Toggle XP drops", "Ctrl + [");
	}
	
	//TODO: Event registration.
	/**
	 * Adds a new label and button to the keybinds list. 
	 * @param labelText - The text of the label.
	 * @param buttonText - The text of the button.
	 */
	private void addKeybindSet(JPanel panel, String labelText, String buttonText) {
		addKeybindLabel(panel, labelText);
		addKeybindButton(panel, buttonText);
	}
	
	/**
	 * Tracks the number of keybind labels added to the keybind panel. Used to determine the gbc.gridy and panel preferred height.
	 */
	private int keybindLabelGridYCounter = 0;
	/**
	 * Adds a new label to the keybinds list. This should be used in conjunction with adding a button in a 1:1 ratio. The new label will be added below the existing ones.
	 * @param labelText - Text of the label to add.
	 * @return - The label that was added.
	 */
	private JLabel addKeybindLabel(JPanel panel, String labelText) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridx = 0;
		gbc.gridy = keybindLabelGridYCounter++;
		gbc.weightx = 20;
		JLabel jlbl = new JLabel(labelText);
		panel.add(jlbl, gbc);
		panel.setPreferredSize(new Dimension(200, 15+(28*keybindLabelGridYCounter)));
		return jlbl;
	}
	
	/**
	 * Tracks the number of keybind buttons added to the keybind panel. Used to determine the gbc.gridy.
	 */
	private int keybindButtonGridYCounter = 0;
	/**
	 * Adds a new button to the keybinds list. This should be used in conjunction with adding a label in a 1:1 ratio. The new button will be added below the existing ones.
	 * @param labelText - Text of the label to add.
	 * @return - The label that was added.
	 */
	private JButton addKeybindButton(JPanel panel, String buttonText) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridx = 1;
		gbc.gridy = keybindButtonGridYCounter++;
		JButton jbtn = new JButton(buttonText);
		panel.add(jbtn, gbc);
		return jbtn;
	}
	
	/**
	 * Adds a preconfigured JCheckbox to the specified container, setting its alignment constraint to left and adding an empty padding border.
	 * @param text - The text of the checkbox
	 * @param container - The container to add the checkbox to.
	 * @return The newly created JCheckBox.
	 */
	private JCheckBox addCheckbox(String text, Container container) {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkbox.setBorder(BorderFactory.createEmptyBorder(0,0,10,5));
        container.add(checkbox);
		return checkbox;
    }
	
	/**
	 * Adds a preconfigured JButton to the specified container using the specified alignment constraint. Does not modify the button's border.
	 * @param text - The text of the button
	 * @param container - The container to add the button to
	 * @param alignment - The alignment of the button.
	 * @return The newly created JButton.
	 */
    private JButton addButton(String text, Container container, float alignment) {
        JButton button = new JButton(text);
        button.setAlignmentX(alignment);
        container.add(button);
		return button;
    }
    
    //TODO: Determine if this method should provide grouping for the radio buttons, or if it should be done in main code.
    /**
     * Adds a preconfigured radio button to the specified container. Does not currently assign the radio button to a group.
     * @param text - The text of the radio button
     * @param container - The container to add the button to
     * @param leftIndent - The amount of padding to add to the left of the radio button as an empty border argument.
     * @return The newly created JRadioButton
     */
	private JRadioButton addRadioButton(String text, Container container, int leftIndent) {
		JRadioButton radioButton = new JRadioButton(text);
        radioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioButton.setBorder(BorderFactory.createEmptyBorder(0,leftIndent,7,5));
        container.add(radioButton);
		return radioButton;
    }
    
	/**
	 * Sets the scroll speed of a JScrollPane
	 * @param scrollPane - The JScrollPane to modify
	 * @param horizontalInc - The horizontal increment value
	 * @param verticalInc - The vertical increment value
	 */
	private void setScrollSpeed(JScrollPane scrollPane, int horizontalInc, int verticalInc) {
		scrollPane.getVerticalScrollBar().setUnitIncrement(verticalInc);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(horizontalInc);
	}
	
}

/**
 * Implements ActionListener; to be used for the buttons in the keybinds tab.
 *
 */
class KeybindListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("I'm a little keybind short and stout.");
		
	}
	
}