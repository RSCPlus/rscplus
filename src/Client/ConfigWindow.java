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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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

import Client.KeybindSet.KeyModifier;
import Game.KeyboardHandler;

/**
 * GUI designed for the RSCPlus client that manages 
 * configuration options and keybind values from within an interface.<br>
 * <br>
 * <b>To add a new configuration option,</b> <br>
 * 1.) Declare an instance variable to hold the gui element (eg checkbox) and add it to the GUI <br>
 * 1.5.) If there is a helper method such as addCheckbox, use that method to create and store the element that is returned in the initialize method. See existing code for examples.<br>
 * 2.) Add an appropriate variable to the Settings.java class as a class variable, <i>and</i> as an assignment in the appropriate restore default method below.<br>
 * 3.) Add an entry in the synchronizeGuiValues method that references the variable, as per the already-existing examples.<br>
 * 4.) Add an entry in the saveSettings method in ConfigWindow referencing the variable, as per the already-existing examples.<br>
 * 5.) Add an entry in the Settings class save method to save the option to file.<br>
 * 6.) Add an entry in the Settings class load method to load the option from file.<br>
 * <br>
 * <b>To add a new keybind,</b><br>
 * 1.) Add a call in the initialize method to addKeybind with appropriate parameters.<br>
 * 2.) Add an entry to the command switch statement in Settings to process the command when its keybind is pressed.<br>
 * 3.) Optional, recommended: Separate the command from its functionality by making a toggleBlah method and calling it from the switch statement.<br>
 */

//TODO: Check that all configurations entered through the GUI are properly sanitized and saved/loaded

public class ConfigWindow {

	private JFrame frame;
	
	private JLabel generalPanelNamePatchModeDesc;
	
	ClickListener clickListener = new ClickListener();
	RebindListener rebindListener = new RebindListener();
	
	ButtonFocusListener focusListener = new ButtonFocusListener();
	JTabbedPane tabbedPane;
	
	/*
	 * JComponent variables which hold configuration data
	 */

	//General tab
	private JCheckBox generalPanelClientSizeCheckbox;
		private JSpinner generalPanelClientSizeXSpinner;
		private JSpinner generalPanelClientSizeYSpinner;
	private JCheckBox generalPanelChatHistoryCheckbox;
	private JCheckBox generalPanelCombatXPMenuCheckbox;
	private JCheckBox generalPanelXPDropsCheckbox;
	private JCheckBox generalPanelFatigueDropsCheckbox;
	private JSpinner generalPanelFatigueFigSpinner;
	private JCheckBox generalPanelFatigueAlertCheckbox;
	private JSlider generalPanelNamePatchModeSlider;
	private JCheckBox generalPanelRoofHidingCheckbox;
	private JCheckBox generalPanelColoredTextCheckbox;
	private JCheckBox generalPanelIncreaseFoVCheckbox;
	private JCheckBox generalPanelCustomCursorCheckbox;
	private JSlider generalPanelViewDistanceSlider;
	
	//Overlays tab	
	private JCheckBox overlayPanelStatusDisplayCheckbox;
	private JCheckBox overlayPanelInvCountCheckbox;
	private JCheckBox overlayPanelItemNamesCheckbox;
	private JCheckBox overlayPanelPlayerNamesCheckbox;
	private JCheckBox overlayPanelFriendNamesCheckbox;
	private JCheckBox overlayPanelNPCNamesCheckbox;
	private JCheckBox overlayPanelNPCHitboxCheckbox;
	private JCheckBox overlayPanelFoodHealingCheckbox;
	private JCheckBox overlayPanelHPRegenTimerCheckbox;
	private JCheckBox overlayPanelDebugModeCheckbox;
	
	//Notifications tab
	private JCheckBox notificationPanelPMNotifsCheckbox;
	private JCheckBox notificationPanelTradeNotifsCheckbox;
	private JCheckBox notificationPanelDuelNotifsCheckbox;
	private JCheckBox notificationPanelLogoutNotifsCheckbox;
	private JCheckBox notificationPanelLowHPNotifsCheckbox;
		private JSpinner notificationPanelLowHPNotifsSpinner;
	private JCheckBox notificationPanelNotifSoundsCheckbox;	
	private JCheckBox notificationPanelTrayPopupCheckbox;
	private JRadioButton notificationPanelClientFocusButton;
	private JRadioButton notificationPanelAnyFocusButton;
	
	//Streaming & Privacy tab
	private JCheckBox streamingPanelTwitchChatCheckbox;
	private JTextField streamingPanelTwitchInGameNameTextField;
	private JTextField streamingPanelTwitchOAuthTextField;
	private JTextField streamingPanelTwitchUserTextField;
	private JCheckBox streamingPanelIPAtLoginCheckbox;
	private JCheckBox streamingPanelSaveLoginCheckbox;
	
	public ConfigWindow() {
		
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			Logger.Error("Unable to set L&F: Unsupported look and feel");
		} catch (ClassNotFoundException e) {
			Logger.Error("Unable to set L&F: Class not found");
		} catch (InstantiationException e) {
			Logger.Error("Unable to set L&F: Class object cannot be instantiated");
		} catch (IllegalAccessException e) {
			Logger.Error("Unable to set L&F: Illegal acess exception");
		}
	    
		initialize();
	}
	
	public void showConfigWindow() {
		this.synchronizeGuiValues();
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
		tabbedPane = new JTabbedPane();
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
				Launcher.getConfigWindow().saveSettings();
				Launcher.getConfigWindow().hideConfigWindow();
				
			}
		});
        
        addButton("Cancel", navigationPanel, Component.LEFT_ALIGNMENT).addActionListener(new ActionListener() {
        	
			@Override
			public void actionPerformed(ActionEvent e) {
				Launcher.getConfigWindow().hideConfigWindow();

			}
		});
        
        addButton("Apply", navigationPanel, Component.LEFT_ALIGNMENT).addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Launcher.getConfigWindow().saveSettings();
			}
		});
        
        navigationPanel.add(Box.createHorizontalGlue());
        addButton("Restore Defaults", navigationPanel, Component.RIGHT_ALIGNMENT).addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO: Restore defaults
				switch(tabbedPane.getSelectedIndex()) {
				case 0:
					Settings.restoreDefaultGeneral();
					break;
				case 1:
					Settings.restoreDefaultOverlays();
					break;
					
				case 2:
					Settings.restoreDefaultNotifications();
					break;
					
				case 3:
					Settings.restoreDefaultPrivacy();
					break;
					
				case 4:
					Settings.restoreDefaultKeybinds();
					break;
					
				default:
					Logger.Error("Restore defaults attempted to operate on a non-existent tab!");
				}
				
			}
		});
		
		/*
		 * General tab
		 */
		
		generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));
		
		JPanel generalPanelClientSizePanel = new JPanel();
		generalPanel.add(generalPanelClientSizePanel);
		generalPanelClientSizePanel.setLayout(new BoxLayout(generalPanelClientSizePanel, BoxLayout.X_AXIS));
		generalPanelClientSizePanel.setPreferredSize(new Dimension(0,37));
		generalPanelClientSizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
			//TODO: Make the client size change after applying this setting
			//TODO: Gray out the JSpinners if checkbox is unchecked?
			generalPanelClientSizeCheckbox = addCheckbox("Default client size:", generalPanelClientSizePanel);
			
			generalPanelClientSizeXSpinner = new JSpinner();
			generalPanelClientSizePanel.add(generalPanelClientSizeXSpinner);
			generalPanelClientSizeXSpinner.setMaximumSize(new Dimension(48,20));
			generalPanelClientSizeXSpinner.setMinimumSize(new Dimension(48,20));
			generalPanelClientSizeXSpinner.setAlignmentY((float) 0.75);
			
			JLabel generalPanelClientSizeByLabel = new JLabel("x");
			generalPanelClientSizePanel.add(generalPanelClientSizeByLabel);
			generalPanelClientSizeByLabel.setAlignmentY((float) 0.9);
			generalPanelClientSizeByLabel.setBorder(new EmptyBorder(0,2,0,2));
			
			generalPanelClientSizeYSpinner = new JSpinner();
			generalPanelClientSizePanel.add(generalPanelClientSizeYSpinner);
			generalPanelClientSizeYSpinner.setMaximumSize(new Dimension(48,20));
			generalPanelClientSizeYSpinner.setMinimumSize(new Dimension(48,20));
			generalPanelClientSizeYSpinner.setAlignmentY((float) 0.75);
			
			//Sanitize JSpinner values
			SpinnerNumberModel spinnerWinXModel = new SpinnerNumberModel();
			spinnerWinXModel.setMinimum(512);
			spinnerWinXModel.setValue(512);
			spinnerWinXModel.setStepSize(10);
			generalPanelClientSizeXSpinner.setModel(spinnerWinXModel);
			SpinnerNumberModel spinnerWinYModel = new SpinnerNumberModel();
			spinnerWinYModel.setMinimum(346);
			spinnerWinYModel.setValue(346);
			spinnerWinYModel.setStepSize(10);
			generalPanelClientSizeYSpinner.setModel(spinnerWinYModel);
		
		generalPanelChatHistoryCheckbox = addCheckbox("Load chat history after relogging", generalPanel);
		generalPanelCombatXPMenuCheckbox = addCheckbox("Combat XP menu always on", generalPanel);
		generalPanelXPDropsCheckbox = addCheckbox("XP drops", generalPanel);
		generalPanelFatigueDropsCheckbox = addCheckbox("Fatigue drops", generalPanel);
		
		JPanel generalPanelFatigueFigsPanel = new JPanel();
		generalPanel.add(generalPanelFatigueFigsPanel);
		generalPanelFatigueFigsPanel.setLayout(new BoxLayout(generalPanelFatigueFigsPanel, BoxLayout.X_AXIS));
		generalPanelFatigueFigsPanel.setPreferredSize(new Dimension(0,37));
		generalPanelFatigueFigsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelFatigueFigsPanel.setLayout(new BoxLayout(generalPanelFatigueFigsPanel, BoxLayout.X_AXIS));
		
			JLabel generalPanelFatigueFigsLabel = new JLabel("Fatigue figures:");
			generalPanelFatigueFigsPanel.add(generalPanelFatigueFigsLabel);
			generalPanelFatigueFigsLabel.setAlignmentY((float) 0.9);
			
			generalPanelFatigueFigSpinner = new JSpinner();
			generalPanelFatigueFigsPanel.add(generalPanelFatigueFigSpinner);
			generalPanelFatigueFigSpinner.setMaximumSize(new Dimension(35,20));
			generalPanelFatigueFigSpinner.setAlignmentY((float) 0.7);
			generalPanelFatigueFigsPanel.setBorder(new EmptyBorder(0,0,7,0));
			
			//Sanitize JSpinner values
			SpinnerNumberModel spinnerNumModel = new SpinnerNumberModel();
			spinnerNumModel.setMinimum(1);
			spinnerNumModel.setMaximum(7);
			spinnerNumModel.setValue(2);
			generalPanelFatigueFigSpinner.setModel(spinnerNumModel);
			
		generalPanelFatigueAlertCheckbox = addCheckbox("Fatigue alert", generalPanel);
		
		JPanel generalPanelNamePatchModePanel = new JPanel();
		generalPanelNamePatchModePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelNamePatchModePanel.setMaximumSize(new Dimension(300,60));
		generalPanelNamePatchModePanel.setLayout(new BoxLayout(generalPanelNamePatchModePanel, BoxLayout.X_AXIS));
		generalPanel.add(generalPanelNamePatchModePanel);
		
		generalPanelNamePatchModeSlider = new JSlider();
		generalPanelNamePatchModeSlider.setMajorTickSpacing(1);
		generalPanelNamePatchModeSlider.setPaintLabels(true);
		generalPanelNamePatchModeSlider.setPaintTicks(true);
		generalPanelNamePatchModeSlider.setSnapToTicks(true);
		generalPanelNamePatchModeSlider.setMinimum(0);
		generalPanelNamePatchModeSlider.setMaximum(3);
		generalPanelNamePatchModeSlider.setPreferredSize(new Dimension(33,0));
		generalPanelNamePatchModeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelNamePatchModeSlider.setBorder(new EmptyBorder(0, 0, 5, 0));
		generalPanelNamePatchModeSlider.setOrientation(SwingConstants.VERTICAL);
		generalPanelNamePatchModePanel.add(generalPanelNamePatchModeSlider);
		
		JPanel generalPanelNamePatchModeTextPanel = new JPanel();
		generalPanelNamePatchModeTextPanel.setPreferredSize(new Dimension(255,55));
		generalPanelNamePatchModeTextPanel.setLayout(new BorderLayout());
		generalPanelNamePatchModeTextPanel.setBorder(new EmptyBorder(0,10,0,0));
		generalPanelNamePatchModePanel.add(generalPanelNamePatchModeTextPanel);
		
		JLabel generalPanelNamePatchModeTitle = new JLabel("<html><b>Name patch mode</b> (Requires restart)</html>");
		generalPanelNamePatchModeTextPanel.add(generalPanelNamePatchModeTitle, BorderLayout.PAGE_START);
		//TODO: Remove hard-coded generalPanelPatchDesc text from JLabel
		generalPanelNamePatchModeDesc = new JLabel("");
		generalPanelNamePatchModeTextPanel.add(generalPanelNamePatchModeDesc, BorderLayout.CENTER);
		
		generalPanelNamePatchModeSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				switch (generalPanelNamePatchModeSlider.getValue()) {
				case 3:
					generalPanelNamePatchModeDesc.setText("<html>Reworded vague stuff to be more descriptive on top of type 1 & 2 changes</html>");
					break;
				case 2:
					generalPanelNamePatchModeDesc.setText("<html>Capitalizations and fixed spellings on top of type 1 changes</html>");
					break;
				case 1:
					generalPanelNamePatchModeDesc.setText("<html>Purely practical name changes (potion dosages, unidentified herbs, unfinished potions)</html>");
					break;
				case 0:
					generalPanelNamePatchModeDesc.setText("<html>No item name patching</html>");
					break;
				default:
					Logger.Error("Invalid name patch mode value");
					break;
				}
			}
		});
		
		generalPanelRoofHidingCheckbox = addCheckbox("Roof hiding", generalPanel);
		generalPanelColoredTextCheckbox = addCheckbox("Color coded text", generalPanel);
		generalPanelIncreaseFoVCheckbox = addCheckbox("Increase FoV", generalPanel);
		generalPanelCustomCursorCheckbox = addCheckbox("Use custom cursor", generalPanel);
        
		JLabel generalPanelViewDistanceLabel = new JLabel("View distance");
		generalPanel.add(generalPanelViewDistanceLabel);
		generalPanelViewDistanceLabel.setAlignmentY((float) 0.9);
		
		generalPanelViewDistanceSlider = new JSlider();
		
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
		 * Overlays tab
		 */
		
		overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
		
		overlayPanelStatusDisplayCheckbox = addCheckbox("Show HP/Prayer/Fatigue display", overlayPanel);
		overlayPanelInvCountCheckbox = addCheckbox("Display inventory count", overlayPanel);
		overlayPanelItemNamesCheckbox = addCheckbox("Display item name overlay", overlayPanel);
		overlayPanelPlayerNamesCheckbox = addCheckbox("Show player name overlay", overlayPanel);
		overlayPanelFriendNamesCheckbox = addCheckbox("Show nearby friend name overlay", overlayPanel);
		overlayPanelNPCNamesCheckbox = addCheckbox("Display NPC name overlay", overlayPanel);
		overlayPanelNPCHitboxCheckbox = addCheckbox("Show character hitboxes", overlayPanel);
		overlayPanelFoodHealingCheckbox = addCheckbox("Show food healing overlay", overlayPanel);
		overlayPanelHPRegenTimerCheckbox = addCheckbox("Display time until next HP regeneration", overlayPanel);
		overlayPanelDebugModeCheckbox = addCheckbox("Enable debug mode", overlayPanel);
		
		/*
		 * Notifications tab
		 */
		
		notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
		
		notificationPanelPMNotifsCheckbox = addCheckbox("Enable PM notifications", notificationPanel);
		notificationPanelTradeNotifsCheckbox = addCheckbox("Enable trade notifications", notificationPanel);
		notificationPanelDuelNotifsCheckbox = addCheckbox("Enable duel notifications", notificationPanel);
		notificationPanelLogoutNotifsCheckbox = addCheckbox("Enable logout notification", notificationPanel);
		
		JPanel notificationPanelLowHPNotifsPanel = new JPanel();
		notificationPanel.add(notificationPanelLowHPNotifsPanel);
		notificationPanelLowHPNotifsPanel.setLayout(new BoxLayout(notificationPanelLowHPNotifsPanel, BoxLayout.X_AXIS));
		notificationPanelLowHPNotifsPanel.setPreferredSize(new Dimension(0,37));
		notificationPanelLowHPNotifsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		notificationPanelLowHPNotifsCheckbox = addCheckbox("Enable low HP notification at", notificationPanelLowHPNotifsPanel);
		
			notificationPanelLowHPNotifsSpinner = new JSpinner();
			notificationPanelLowHPNotifsSpinner.setMaximumSize(new Dimension(35,20));
			notificationPanelLowHPNotifsSpinner.setMinimumSize(new Dimension(35,20));
			notificationPanelLowHPNotifsSpinner.setAlignmentY((float) 0.75);
			notificationPanelLowHPNotifsPanel.add(notificationPanelLowHPNotifsSpinner);
			
			JLabel notificationPanelLowHPNotifsEndLabel = new JLabel("% HP");
			notificationPanelLowHPNotifsPanel.add(notificationPanelLowHPNotifsEndLabel);
			notificationPanelLowHPNotifsEndLabel.setAlignmentY((float) 0.9);
			notificationPanelLowHPNotifsEndLabel.setBorder(new EmptyBorder(0,2,0,0));
			
			//Sanitize JSpinner values
			SpinnerNumberModel spinnerHPNumModel = new SpinnerNumberModel();
			spinnerHPNumModel.setMinimum(1);
			spinnerHPNumModel.setMaximum(99);
			spinnerHPNumModel.setValue(25);
			notificationPanelLowHPNotifsSpinner.setModel(spinnerHPNumModel);
			
		notificationPanelNotifSoundsCheckbox = addCheckbox("Enable notification sounds", notificationPanel);
		
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
		
		streamingPanelTwitchChatCheckbox = addCheckbox("Enable Twitch chat", streamingPanel);
		
		JPanel streamingPanelTwitchInGameNamePanel = new JPanel();
		streamingPanel.add(streamingPanelTwitchInGameNamePanel);
		streamingPanelTwitchInGameNamePanel.setLayout(new BoxLayout(streamingPanelTwitchInGameNamePanel, BoxLayout.X_AXIS));
		streamingPanelTwitchInGameNamePanel.setPreferredSize(new Dimension(0,37));
		streamingPanelTwitchInGameNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		streamingPanelTwitchInGameNamePanel.setBorder(new EmptyBorder(0,0,9,0));
		
			JLabel streamingPanelTwitchInGameLabel = new JLabel("Twitch name to display in game: ");
			streamingPanelTwitchInGameNamePanel.add(streamingPanelTwitchInGameLabel);
			streamingPanelTwitchInGameLabel.setAlignmentY((float) 0.9);
			
			streamingPanelTwitchInGameNameTextField = new JTextField();
			streamingPanelTwitchInGameNamePanel.add(streamingPanelTwitchInGameNameTextField);
			streamingPanelTwitchInGameNameTextField.setMinimumSize(new Dimension(100,20));
			streamingPanelTwitchInGameNameTextField.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
			streamingPanelTwitchInGameNameTextField.setAlignmentY((float) 0.75);
			
		JPanel streamingPanelTwitchOAuthPanel = new JPanel();
		streamingPanel.add(streamingPanelTwitchOAuthPanel);
		streamingPanelTwitchOAuthPanel.setLayout(new BoxLayout(streamingPanelTwitchOAuthPanel, BoxLayout.X_AXIS));
		streamingPanelTwitchOAuthPanel.setPreferredSize(new Dimension(0,37));
		streamingPanelTwitchOAuthPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		streamingPanelTwitchOAuthPanel.setBorder(new EmptyBorder(0,0,9,0));
		
			JLabel streamingPanelTwitchOAuthLabel = new JLabel("Twitch OAuth token: ");
			streamingPanelTwitchOAuthPanel.add(streamingPanelTwitchOAuthLabel);
			streamingPanelTwitchOAuthLabel.setAlignmentY((float) 0.9);
			
			streamingPanelTwitchOAuthTextField = new JPasswordField();
			streamingPanelTwitchOAuthPanel.add(streamingPanelTwitchOAuthTextField);
			streamingPanelTwitchOAuthTextField.setMinimumSize(new Dimension(100,20));
			streamingPanelTwitchOAuthTextField.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
			streamingPanelTwitchOAuthTextField.setAlignmentY((float) 0.75);
			
		JPanel streamingPanelTwitchUserPanel = new JPanel();
		streamingPanel.add(streamingPanelTwitchUserPanel);
		streamingPanelTwitchUserPanel.setLayout(new BoxLayout(streamingPanelTwitchUserPanel, BoxLayout.X_AXIS));
		streamingPanelTwitchUserPanel.setPreferredSize(new Dimension(0,37));
		streamingPanelTwitchUserPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		streamingPanelTwitchUserPanel.setBorder(new EmptyBorder(0,0,9,0));
		
			JLabel streamingPanelTwitchUserLabel = new JLabel("Twitch username: ");
			streamingPanelTwitchUserPanel.add(streamingPanelTwitchUserLabel);
			streamingPanelTwitchUserLabel.setAlignmentY((float) 0.9);
			
			streamingPanelTwitchUserTextField = new JTextField();
			streamingPanelTwitchUserPanel.add(streamingPanelTwitchUserTextField);
			streamingPanelTwitchUserTextField.setMinimumSize(new Dimension(100,20));
			streamingPanelTwitchUserTextField.setMaximumSize(new Dimension(Short.MAX_VALUE,20));
			streamingPanelTwitchUserTextField.setAlignmentY((float) 0.75);
		
		streamingPanelIPAtLoginCheckbox = addCheckbox("Enable IP/DNS details at login screen", streamingPanel);
		streamingPanelSaveLoginCheckbox = addCheckbox("Save login information between logins (Requires restart)", streamingPanel);
		
		/*
		 * Keybind tab
		 */
		
		//TODO: Make the contents top aligned
		keybindPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		keybindPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		GridBagLayout gbl_panel = new GridBagLayout();
		
		keybindPanel.setLayout(gbl_panel);

		addKeybindSet(keybindPanel, "Logout", "logout", KeyModifier.CTRL, KeyEvent.VK_L);
		addKeybindSet(keybindPanel, "Take screenshot", "screenshot", KeyModifier.CTRL, KeyEvent.VK_S);
		addKeybindSet(keybindPanel, "Toggle color coded text", "toggle_colorize", KeyModifier.CTRL, KeyEvent.VK_Z);
		addKeybindSet(keybindPanel, "Toggle combat XP menu persistence", "toggle_combat_xp_menu", KeyModifier.CTRL, KeyEvent.VK_C);
		addKeybindSet(keybindPanel, "Toggle debug mode", "toggle_debug", KeyModifier.CTRL, KeyEvent.VK_D);
		addKeybindSet(keybindPanel, "Toggle fatigue alert", "toggle_fatigue_alert", KeyModifier.CTRL, KeyEvent.VK_F);
		addKeybindSet(keybindPanel, "Toggle fatigue drops", "toggle_fatigue_drops", KeyModifier.CTRL, KeyEvent.VK_CLOSE_BRACKET);
		addKeybindSet(keybindPanel, "Toggle food heal overlay", "toggle_food_heal_overlay", KeyModifier.CTRL, KeyEvent.VK_G);
		addKeybindSet(keybindPanel, "Toggle friend name overlay", "toggle_friend_name_overlay", KeyModifier.NONE, -1);
		addKeybindSet(keybindPanel, "Toggle HP/prayer/fatigue display", "toggle_hpprayerfatigue_display", KeyModifier.CTRL, KeyEvent.VK_U);
		addKeybindSet(keybindPanel, "Toggle inventory count overlay", "toggle_inven_count_overlay", KeyModifier.CTRL, KeyEvent.VK_E);
		addKeybindSet(keybindPanel, "Toggle IP/DNS shown at login screen", "toggle_ipdns", KeyModifier.NONE, -1);
		addKeybindSet(keybindPanel, "Toggle item name overlay", "toggle_item_overlay", KeyModifier.CTRL, KeyEvent.VK_I);
		addKeybindSet(keybindPanel, "Toggle hitboxes", "toggle_hitboxes", KeyModifier.CTRL, KeyEvent.VK_H);
		addKeybindSet(keybindPanel, "Toggle NPC name overlay", "toggle_npc_name_overlay", KeyModifier.CTRL, KeyEvent.VK_N);
		addKeybindSet(keybindPanel, "Toggle player name overlay", "toggle_player_name_overlay", KeyModifier.CTRL, KeyEvent.VK_P);
		addKeybindSet(keybindPanel, "Toggle roof hiding", "toggle_roof_hiding", KeyModifier.CTRL, KeyEvent.VK_R);
		addKeybindSet(keybindPanel, "Toggle save login information", "toggle_save_login_info", KeyModifier.NONE, -1);
		addKeybindSet(keybindPanel, "Toggle time until health regen", "toggle_health_regen_timer", KeyModifier.NONE, -1);
		addKeybindSet(keybindPanel, "Toggle Twitch chat", "toggle_twitch_chat", KeyModifier.CTRL, KeyEvent.VK_T);
		addKeybindSet(keybindPanel, "Toggle XP drops", "toggle_xp_drops", KeyModifier.CTRL, KeyEvent.VK_OPEN_BRACKET);
		addKeybindSet(keybindPanel, "Show settings window", "show_config_window", KeyModifier.CTRL, KeyEvent.VK_O);
		//TODO: Add a horizontal line here?
		addKeybindSet(keybindPanel, "Switch to world 1 at login screen", "world_1", KeyModifier.CTRL, KeyEvent.VK_1);
		addKeybindSet(keybindPanel, "Switch to world 2 at login screen", "world_2", KeyModifier.CTRL, KeyEvent.VK_2);
		addKeybindSet(keybindPanel, "Switch to world 3 at login screen", "world_3", KeyModifier.CTRL, KeyEvent.VK_3);
		addKeybindSet(keybindPanel, "Switch to world 4 at login screen", "world_4", KeyModifier.CTRL, KeyEvent.VK_4);
		addKeybindSet(keybindPanel, "Switch to world 5 at login screen", "world_5", KeyModifier.CTRL, KeyEvent.VK_5);
		
		//this.synchronizeGuiValues();
	}
	
	/**
	 * Adds a new keybind to the GUI and settings and registers it to be checked when keypresses are sent to the applet.
	 * @param panel - Panel to add the keybind label and button to
	 * @param labelText - Text describing the keybind's function as shown to the user on the config window.
	 * @param commandID - Unique String matching an entry in the processKeybindCommand switch statement.
	 * @param defaultModifier - Default modifier value. This can be one of the enum values of KeybindSet.KeyModifier, eg KeyModifier.CTRL
	 * @param defaultKeyValue - Default key value. This should match up with a KeyEvent.VK_ value. Set to -1 to set the default as NONE
	 */
	private void addKeybindSet(JPanel panel, String labelText, String commandID, KeyModifier defaultModifier, int defaultKeyValue) {
		addKeybindLabel(panel, labelText);
		String buttonText = defaultModifier.toString() + " + " + KeyEvent.getKeyText(defaultKeyValue);
		if (defaultKeyValue == -1)
			buttonText = "NONE";
		JButton b = addKeybindButton(panel, buttonText);
		KeybindSet kbs = new KeybindSet(b, commandID, defaultModifier, defaultKeyValue);
		KeyboardHandler.keybindSetList.add(kbs);
		setKeybindButtonText(kbs); //Set the text of the keybind button now that it has been intialized properly
		b.addActionListener(this.clickListener);
		b.addKeyListener(this.rebindListener);
		b.addFocusListener(focusListener);
		b.setFocusable(false);
		
		//Default KeybindSet
		KeyboardHandler.defaultKeybindSetList.put(commandID, new KeybindSet(null, commandID, defaultModifier, defaultKeyValue));
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
	
	/**
	 * Synchronizes all relevant values in the gui's elements to match those in Settings.java
	 */
	public void synchronizeGuiValues() {
		//General tab
		generalPanelClientSizeCheckbox.setSelected(Settings.CUSTOM_CLIENT_SIZE);
		generalPanelClientSizeXSpinner.setValue(Settings.CUSTOM_CLIENT_SIZE_X);
		generalPanelClientSizeYSpinner.setValue(Settings.CUSTOM_CLIENT_SIZE_Y);
		generalPanelChatHistoryCheckbox.setSelected(Settings.LOAD_CHAT_HISTORY);
		generalPanelCombatXPMenuCheckbox.setSelected(Settings.COMBAT_MENU);
		generalPanelXPDropsCheckbox.setSelected(Settings.SHOW_XPDROPS);
		generalPanelFatigueDropsCheckbox.setSelected(Settings.SHOW_FATIGUEDROPS);
		generalPanelFatigueFigSpinner.setValue(new Integer(Settings.FATIGUE_FIGURES));
		generalPanelFatigueAlertCheckbox.setSelected(Settings.FATIGUE_ALERT);
		generalPanelNamePatchModeSlider.setValue(Settings.NAME_PATCH_TYPE);
		generalPanelRoofHidingCheckbox.setSelected(Settings.HIDE_ROOFS);
		generalPanelColoredTextCheckbox.setSelected(Settings.COLORIZE);
		generalPanelIncreaseFoVCheckbox.setSelected(Settings.INCREASE_FOV);
		generalPanelCustomCursorCheckbox.setSelected(Settings.SOFTWARE_CURSOR);
		generalPanelViewDistanceSlider.setValue(Settings.VIEW_DISTANCE);
		//Sets the text associated with the name patch slider.
		switch (generalPanelNamePatchModeSlider.getValue()) {
		case 3:
			generalPanelNamePatchModeDesc.setText("<html>Reworded vague stuff to be more descriptive on top of type 1 & 2 changes</html>");
			break;
		case 2:
			generalPanelNamePatchModeDesc.setText("<html>Capitalizations and fixed spellings on top of type 1 changes</html>");
			break;
		case 1:
			generalPanelNamePatchModeDesc.setText("<html>Purely practical name changes (potion dosages, unidentified herbs, unfinished potions)</html>");
			break;
		case 0:
			generalPanelNamePatchModeDesc.setText("<html>No item name patching</html>");
			break;
		default:
			System.err.println("Invalid name patch mode value");
			break;
		}

		//Overlays tab	
		overlayPanelStatusDisplayCheckbox.setSelected(Settings.SHOW_STATUSDISPLAY);
		overlayPanelInvCountCheckbox.setSelected(Settings.SHOW_INVCOUNT);
		overlayPanelItemNamesCheckbox.setSelected(Settings.SHOW_ITEMINFO);
		overlayPanelPlayerNamesCheckbox.setSelected(Settings.SHOW_PLAYERINFO);
		overlayPanelFriendNamesCheckbox.setSelected(Settings.SHOW_FRIENDINFO);
		overlayPanelNPCNamesCheckbox.setSelected(Settings.SHOW_NPCINFO);
		overlayPanelNPCHitboxCheckbox.setSelected(Settings.SHOW_HITBOX);
		overlayPanelFoodHealingCheckbox.setSelected(Settings.SHOW_FOOD_HEAL_OVERLAY);
		overlayPanelHPRegenTimerCheckbox.setSelected(Settings.SHOW_TIME_UNTIL_HP_REGEN);
		overlayPanelDebugModeCheckbox.setSelected(Settings.DEBUG);

		//Notifications tab
		notificationPanelPMNotifsCheckbox.setSelected(Settings.PM_NOTIFICATIONS );
		notificationPanelTradeNotifsCheckbox.setSelected(Settings.TRADE_NOTIFICATIONS);
		notificationPanelDuelNotifsCheckbox.setSelected(Settings.DUEL_NOTIFICATIONS);
		notificationPanelLogoutNotifsCheckbox.setSelected(Settings.LOGOUT_NOTIFICATIONS);
		notificationPanelLowHPNotifsCheckbox.setSelected(Settings.LOW_HP_NOTIFICATIONS);
		notificationPanelLowHPNotifsSpinner.setValue(Settings.LOW_HP_NOTIF_VALUE);
		notificationPanelNotifSoundsCheckbox.setSelected(Settings.NOTIFICATION_SOUNDS);	
		notificationPanelTrayPopupCheckbox.setSelected(Settings.TRAY_NOTIFS);
		notificationPanelClientFocusButton.setSelected(!Settings.TRAY_NOTIFS_ALWAYS);
		notificationPanelAnyFocusButton.setSelected(Settings.TRAY_NOTIFS_ALWAYS);
		notificationPanelClientFocusButton.setEnabled(Settings.TRAY_NOTIFS);
		notificationPanelAnyFocusButton.setEnabled(Settings.TRAY_NOTIFS);
		
		//Streaming & Privacy tab
		streamingPanelTwitchChatCheckbox.setSelected(Settings.TWITCH_HIDE);
		streamingPanelTwitchInGameNameTextField.setText(Settings.TWITCH_CHANNEL);
		streamingPanelTwitchOAuthTextField.setText(Settings.TWITCH_OAUTH);
		streamingPanelTwitchUserTextField.setText(Settings.TWITCH_USERNAME);
		streamingPanelIPAtLoginCheckbox.setSelected(Settings.SHOW_LOGINDETAILS);
		streamingPanelSaveLoginCheckbox.setSelected(Settings.SAVE_LOGININFO);
		
		for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
			setKeybindButtonText(kbs);
		}
	}
	
	/**
	 * Saves the settings from the gui values to the settings class variables
	 */
	public void saveSettings() {
		//General options
		Settings.CUSTOM_CLIENT_SIZE = generalPanelClientSizeCheckbox.isSelected();
		Settings.CUSTOM_CLIENT_SIZE_X = ((SpinnerNumberModel)(generalPanelClientSizeXSpinner.getModel())).getNumber().intValue();
		Settings.CUSTOM_CLIENT_SIZE_Y = ((SpinnerNumberModel)(generalPanelClientSizeYSpinner.getModel())).getNumber().intValue();
			//TODO: Resize client according to configuration preferences
//			Game.getInstance().setSize(new Dimension(Settings.CUSTOM_CLIENT_SIZE_X, Settings.CUSTOM_CLIENT_SIZE_Y));
//			Renderer.resize(Settings.CUSTOM_CLIENT_SIZE_X, Settings.CUSTOM_CLIENT_SIZE_Y);
		Settings.LOAD_CHAT_HISTORY = generalPanelChatHistoryCheckbox.isSelected();
		Settings.COMBAT_MENU = generalPanelCombatXPMenuCheckbox.isSelected();
		Settings.SHOW_XPDROPS = generalPanelXPDropsCheckbox.isSelected();
		Settings.SHOW_FATIGUEDROPS = generalPanelFatigueDropsCheckbox.isSelected();
		Settings.FATIGUE_FIGURES = ((SpinnerNumberModel)(generalPanelFatigueFigSpinner.getModel())).getNumber().intValue();
		Settings.FATIGUE_ALERT = generalPanelFatigueAlertCheckbox.isSelected();
		Settings.NAME_PATCH_TYPE = generalPanelNamePatchModeSlider.getValue();
		Settings.HIDE_ROOFS = generalPanelRoofHidingCheckbox.isSelected();
		Settings.COLORIZE = generalPanelColoredTextCheckbox.isSelected();
		Settings.INCREASE_FOV = generalPanelIncreaseFoVCheckbox.isSelected();
		Settings.SOFTWARE_CURSOR = generalPanelCustomCursorCheckbox.isSelected();
		Settings.VIEW_DISTANCE = generalPanelViewDistanceSlider.getValue();

		//Overlays options
		Settings.SHOW_STATUSDISPLAY = overlayPanelStatusDisplayCheckbox.isSelected();
		Settings.SHOW_INVCOUNT = overlayPanelInvCountCheckbox.isSelected();
		Settings.SHOW_ITEMINFO = overlayPanelItemNamesCheckbox.isSelected();
		Settings.SHOW_PLAYERINFO = overlayPanelPlayerNamesCheckbox.isSelected();
		Settings.SHOW_FRIENDINFO = overlayPanelFriendNamesCheckbox.isSelected();
		Settings.SHOW_NPCINFO = overlayPanelNPCNamesCheckbox.isSelected();
		Settings.SHOW_HITBOX = overlayPanelNPCHitboxCheckbox.isSelected();
		Settings.SHOW_FOOD_HEAL_OVERLAY = overlayPanelFoodHealingCheckbox.isSelected();
		Settings.SHOW_TIME_UNTIL_HP_REGEN = overlayPanelHPRegenTimerCheckbox.isSelected();
		Settings.DEBUG = overlayPanelDebugModeCheckbox.isSelected();

		//Notifications options
		Settings.PM_NOTIFICATIONS = notificationPanelPMNotifsCheckbox.isSelected();
		Settings.TRADE_NOTIFICATIONS = notificationPanelTradeNotifsCheckbox.isSelected();
		Settings.DUEL_NOTIFICATIONS = notificationPanelDuelNotifsCheckbox.isSelected();
		Settings.LOGOUT_NOTIFICATIONS = notificationPanelLogoutNotifsCheckbox.isSelected();
		Settings.LOW_HP_NOTIFICATIONS = notificationPanelLowHPNotifsCheckbox.isSelected();
		Settings.LOW_HP_NOTIF_VALUE = ((SpinnerNumberModel)(notificationPanelLowHPNotifsSpinner.getModel())).getNumber().intValue();
		Settings.NOTIFICATION_SOUNDS = notificationPanelNotifSoundsCheckbox.isSelected();
		Settings.TRAY_NOTIFS = notificationPanelTrayPopupCheckbox.isSelected();
		Settings.TRAY_NOTIFS_ALWAYS = notificationPanelAnyFocusButton.isSelected();

		//Streaming & Privacy
		Settings.TWITCH_HIDE = streamingPanelTwitchChatCheckbox.isSelected();
		Settings.TWITCH_CHANNEL = streamingPanelTwitchInGameNameTextField.getText();
		Settings.TWITCH_OAUTH = streamingPanelTwitchOAuthTextField.getText();
		Settings.TWITCH_USERNAME = streamingPanelTwitchUserTextField.getText();
		Settings.SHOW_LOGINDETAILS = streamingPanelIPAtLoginCheckbox.isSelected();
		Settings.SAVE_LOGININFO = streamingPanelSaveLoginCheckbox.isSelected();
		
		Settings.Save();
	}

	public void disposeJFrame() {
		frame.dispose();
		
	}
	
	/**
	 * Sets the text of the button to its keybind.
	 * @param kbs - The KeybindSet object to set the button text of.
	 */
	public static void setKeybindButtonText(KeybindSet kbs) {
		String modifierText = kbs.modifier.toString() + " + ";
		String keyText = KeyEvent.getKeyText(kbs.key);
		
		if (kbs.key == -1) 
			keyText = "NONE";
		
		if (kbs.modifier == KeyModifier.NONE)
			modifierText = "";
		
		if (keyText.equals("Open Bracket")) {
			keyText = "[";
		}
		if (keyText.equals("Close Bracket")) {
			keyText = "]";
		}
		if (keyText.equals("Unknown keyCode: 0x0")) {
			keyText = "???";
		}
		
		kbs.button.setText(modifierText + keyText);
	}
	
}

/**
 * Implements ActionListener; to be used for the buttons in the keybinds tab.
 *
 */
class ClickListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();
		button.setText("...");
		button.setFocusable(true);
		button.requestFocusInWindow();		
	}
	
}

class ButtonFocusListener implements FocusListener {

	@Override
	public void focusGained(FocusEvent arg0) {}

	@Override
	public void focusLost(FocusEvent arg0) {
		JButton button = (JButton)arg0.getSource();
		
		for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
			if (button == kbs.button) {
				ConfigWindow.setKeybindButtonText(kbs);
				kbs.button.setFocusable(false);
			}
		}
		
	}
	
}

class RebindListener implements KeyListener {

	@Override
	public void keyPressed(KeyEvent arg0) {
		KeyModifier modifier = KeyModifier.NONE;
		
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
				if (arg0.getSource() == kbs.button) {
					kbs.modifier = KeyModifier.NONE;
					kbs.key = -1;
					ConfigWindow.setKeybindButtonText(kbs);
					kbs.button.setFocusable(false);
				}
			}
			return;
		}
		
		if (arg0.getKeyCode() == KeyEvent.VK_CONTROL || arg0.getKeyCode() == KeyEvent.VK_SHIFT || arg0.getKeyCode() == KeyEvent.VK_ALT) {
			return;
		}
		if (arg0.isControlDown()) {
			modifier = KeyModifier.CTRL;
			
		} else if (arg0.isShiftDown()) {
			modifier = KeyModifier.SHIFT;
			
		} else if (arg0.isAltDown()) {
			modifier = KeyModifier.ALT;
			
		}
		
		int key = arg0.getKeyCode();
		JButton jbtn = (JButton)arg0.getSource();
		
		if (key != -1)
			for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
				if (jbtn != kbs.button) {
					if (kbs.isDuplicateKeybindSet(modifier, key)) {
						jbtn.setText("DUPLICATE!");
						return;
					}
				}
			}
		
		for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
			if (jbtn == kbs.button) {
				kbs.modifier = modifier;
				kbs.key = key;
				ConfigWindow.setKeybindButtonText(kbs);
				kbs.button.setFocusable(false);
			}
		}
		
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
	
	
}