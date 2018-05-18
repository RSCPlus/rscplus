/**
 *	rscplus
 *
 *	This file is part of rscplus.
 *
 *	rscplus is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	rscplus is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with rscplus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Authors: see <https://github.com/OrN/rscplus>
 */

package Client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import Client.KeybindSet.KeyModifier;
import Game.Camera;
import Game.Game;
import Game.KeyboardHandler;

/**
 * GUI designed for the RSCPlus client that manages configuration options and keybind values from within an interface.
 * <p>
 * <b>To add a new configuration option to the GUI,</b> <br>
 * 1.) Declare an instance variable to hold the gui element (eg checkbox) and add it to the GUI from
 * ConfigWindow.initialize() (see existing examples) <br>
 * 1.5.) If there is a helper method such as addCheckbox, use that method to create and store the element that is
 * returned in the ConfigWindow.initialize() method. See existing code for examples.<br>
 * 2.) ^Add an appropriate variable to the Settings class as a class variable, <i>and</i> as an assignment in the
 * appropriate restore default method below. <br>
 * 3.) Add an entry in the ConfigWindow.synchronizeGuiValues() method that references the variable, as per the
 * already-existing examples.<br>
 * 4.) Add an entry in the ConfigWindow.saveSettings() method referencing the variable, as per the already-existing
 * examples.<br>
 * 5.) ^Add an entry in the Settings.Save() class save method to save the option to file.<br>
 * 6.) ^Add an entry in the Settings.Load() class load method to load the option from file.<br>
 * 7.) (Optional) If a method needs to be called to adjust settings other than the setting value itself, add it to the
 * ConfigWindow.applySettings() method below.<br>
 * <br>
 * <i>Entries marked with a ^ are steps used to add settings that are not included in the GUI.</i> <br>
 * <br>
 * <b>To add a new keybind,</b><br>
 * 1.) Add a call in the initialize method to addKeybind with appropriate parameters.<br>
 * 2.) Add an entry to the command switch statement in Settings to process the command when its keybind is pressed.<br>
 * 3.) Optional, recommended: Separate the command from its functionality by making a toggleBlah method and calling it
 * from the switch statement.<br>
 * </p>
 */
public class ConfigWindow {
	
	private JFrame frame;
	
	private JLabel generalPanelNamePatchModeDesc;
	private JLabel notificationPanelLowHPNotifsEndLabel;
	private JLabel notificationPanelFatigueNotifsEndLabel;
	
	ClickListener clickListener = new ClickListener();
	RebindListener rebindListener = new RebindListener();
	
	ButtonFocusListener focusListener = new ButtonFocusListener();
	JTabbedPane tabbedPane;
	
	/*
	 * JComponent variables which hold configuration data
	 */
	
	// General tab
	private JCheckBox generalPanelClientSizeCheckbox;
	private JSpinner generalPanelClientSizeXSpinner;
	private JSpinner generalPanelClientSizeYSpinner;
	private JCheckBox generalPanelChatHistoryCheckbox;
	private JCheckBox generalPanelCombatXPMenuCheckbox;
	private JCheckBox generalPanelXPDropsCheckbox;
	private JRadioButton generalPanelXPCenterAlignFocusButton;
	private JRadioButton generalPanelXPRightAlignFocusButton;
	private JCheckBox generalPanelFatigueDropsCheckbox;
	private JSpinner generalPanelFatigueFigSpinner;
	private JCheckBox generalPanelFatigueAlertCheckbox;
	private JCheckBox generalPanelInventoryFullAlertCheckbox;
	private JSlider generalPanelNamePatchModeSlider;
	private JCheckBox generalPanelRoofHidingCheckbox;
	private JCheckBox generalPanelColoredTextCheckbox;
	private JSlider generalPanelFoVSlider;
	private JCheckBox generalPanelCustomCursorCheckbox;
	private JSlider generalPanelViewDistanceSlider;
	private JCheckBox generalPanelStartSearchedBankCheckbox;
	private JTextField generalPanelSearchBankWordTextfield;
	
	// Overlays tab
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
	private JTextField blockedItemsTextField;
	private JTextField highlightedItemsTextField;
	
	// Notifications tab
	private JCheckBox notificationPanelPMNotifsCheckbox;
	private JCheckBox notificationPanelTradeNotifsCheckbox;
	private JCheckBox notificationPanelDuelNotifsCheckbox;
	private JCheckBox notificationPanelLogoutNotifsCheckbox;
	private JCheckBox notificationPanelLowHPNotifsCheckbox;
	private JSpinner notificationPanelLowHPNotifsSpinner;
	private JCheckBox notificationPanelFatigueNotifsCheckbox;
	private JSpinner notificationPanelFatigueNotifsSpinner;
	private JCheckBox notificationPanelNotifSoundsCheckbox;
	private JCheckBox notificationPanelUseSystemNotifsCheckbox;
	private JCheckBox notificationPanelTrayPopupCheckbox;
	private JRadioButton notificationPanelTrayPopupClientFocusButton;
	private JRadioButton notificationPanelNotifSoundClientFocusButton;
	private JRadioButton notificationPanelTrayPopupAnyFocusButton;
	private JRadioButton notificationPanelNotifSoundAnyFocusButton;
	
	// Streaming & Privacy tab
	private JCheckBox streamingPanelTwitchChatCheckbox;
	private JTextField streamingPanelTwitchChannelNameTextField;
	private JTextField streamingPanelTwitchOAuthTextField;
	private JTextField streamingPanelTwitchUserTextField;
	private JCheckBox streamingPanelIPAtLoginCheckbox;
	private JCheckBox streamingPanelSaveLoginCheckbox;
	
	public ConfigWindow() {
		try {
			// Set System L&F as a fall-back option.
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					NimbusLookAndFeel laf = (NimbusLookAndFeel)UIManager.getLookAndFeel();
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
		Logger.Info("Creating configuration window");
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					runInit();
				}
			});
		} catch (InvocationTargetException e) {
			Logger.Error("There was a thread-related error while setting up the config window!");
			e.printStackTrace();
		} catch (InterruptedException e) {
			Logger.Error("There was a thread-related error while setting up the config window! The window may not be initialized properly!");
			e.printStackTrace();
		}
	}
	
	private void runInit() {
		frame = new JFrame();
		frame.setTitle("Settings");
		frame.setBounds(100, 100, 444, 650);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		URL iconURL = Settings.getResource("/assets/icon.png");
		if (iconURL != null) {
			ImageIcon icon = new ImageIcon(iconURL);
			frame.setIconImage(icon.getImage());
		}
		
		// Container declarations
		/** The tabbed pane holding the five configuration tabs */
		tabbedPane = new JTabbedPane();
		/** The JPanel containing the OK, Cancel, Apply, and Restore Defaults buttons at the bottom of the window */
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
		
		// Adding padding for aesthetics
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
				Launcher.getConfigWindow().applySettings();
				Launcher.getConfigWindow().hideConfigWindow();
			}
		});
		
		addButton("Apply", navigationPanel, Component.LEFT_ALIGNMENT).addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Launcher.getConfigWindow().applySettings();
			}
		});
		
		navigationPanel.add(Box.createHorizontalGlue());
		addButton("Restore Defaults", navigationPanel, Component.RIGHT_ALIGNMENT).addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int choice = JOptionPane.showConfirmDialog(
						Launcher.getConfigWindow().frame,
						"Are you sure you want to restore this tab's settings to their defaults?",
						"Confirm",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (choice == JOptionPane.CLOSED_OPTION || choice == JOptionPane.NO_OPTION) {
					return;
				}
				
				// Restore defaults
				switch (tabbedPane.getSelectedIndex()) {
				case 0:
					Settings.restoreDefaultGeneral();
					Game.getInstance().resizeFrameWithContents();
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
		generalPanelClientSizePanel.setPreferredSize(new Dimension(0, 37));
		generalPanelClientSizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		// TODO: Perhaps change to "Save client size on close"?
		generalPanelClientSizeCheckbox = addCheckbox("Default client size:", generalPanelClientSizePanel);
		generalPanelClientSizeCheckbox.setToolTipText("Start the client with the supplied window size");
		
		generalPanelClientSizeXSpinner = new JSpinner();
		generalPanelClientSizePanel.add(generalPanelClientSizeXSpinner);
		generalPanelClientSizeXSpinner.setMaximumSize(new Dimension(58, 22));
		generalPanelClientSizeXSpinner.setMinimumSize(new Dimension(58, 22));
		generalPanelClientSizeXSpinner.setAlignmentY((float)0.75);
		generalPanelClientSizeXSpinner.setToolTipText("Default client width (512 minimum)");
		generalPanelClientSizeXSpinner.putClientProperty("JComponent.sizeVariant", "mini");
		
		JLabel generalPanelClientSizeByLabel = new JLabel("x");
		generalPanelClientSizePanel.add(generalPanelClientSizeByLabel);
		generalPanelClientSizeByLabel.setAlignmentY((float)0.9);
		generalPanelClientSizeByLabel.setBorder(new EmptyBorder(0, 2, 0, 2));
		
		generalPanelClientSizeYSpinner = new JSpinner();
		generalPanelClientSizePanel.add(generalPanelClientSizeYSpinner);
		generalPanelClientSizeYSpinner.setMaximumSize(new Dimension(58, 22));
		generalPanelClientSizeYSpinner.setMinimumSize(new Dimension(58, 22));
		generalPanelClientSizeYSpinner.setAlignmentY((float)0.75);
		generalPanelClientSizeYSpinner.setToolTipText("Default client height (346 minimum)");
		generalPanelClientSizeYSpinner.putClientProperty("JComponent.sizeVariant", "mini");
		
		// Sanitize JSpinner values
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
		
		generalPanelChatHistoryCheckbox = addCheckbox("Load chat history after relogging (Not implemented yet)", generalPanel);
		generalPanelChatHistoryCheckbox.setToolTipText("Make chat history persist between logins");
		generalPanelChatHistoryCheckbox.setEnabled(false); // TODO: Remove this line when chat history is implemented
		
		generalPanelCombatXPMenuCheckbox = addCheckbox("Combat XP menu always on", generalPanel);
		generalPanelCombatXPMenuCheckbox.setToolTipText("Always show the combat XP menu, even when out of combat");
		
		generalPanelXPDropsCheckbox = addCheckbox("XP drops", generalPanel);
		generalPanelXPDropsCheckbox.setToolTipText("Show the XP gained as an overlay each time XP is received");
		
		ButtonGroup XPAlignButtonGroup = new ButtonGroup();
		generalPanelXPRightAlignFocusButton = addRadioButton("Display on the right", generalPanel, 20);
		generalPanelXPRightAlignFocusButton.setToolTipText("The XP bar and XP drops will be shown just left of the Settings menu.");
		generalPanelXPCenterAlignFocusButton = addRadioButton("Display in the center", generalPanel, 20);
		generalPanelXPCenterAlignFocusButton.setToolTipText("The XP bar and XP drops will be shown at the top-middle of the screen.");
		XPAlignButtonGroup.add(generalPanelXPRightAlignFocusButton);
		XPAlignButtonGroup.add(generalPanelXPCenterAlignFocusButton);
		
		generalPanelFatigueDropsCheckbox = addCheckbox("Fatigue drops", generalPanel);
		generalPanelFatigueDropsCheckbox.setToolTipText("Show the fatigue gained as an overlay each time fatigue is received");
		
		JPanel generalPanelFatigueFigsPanel = new JPanel();
		generalPanel.add(generalPanelFatigueFigsPanel);
		generalPanelFatigueFigsPanel.setLayout(new BoxLayout(generalPanelFatigueFigsPanel, BoxLayout.X_AXIS));
		generalPanelFatigueFigsPanel.setPreferredSize(new Dimension(0, 37));
		generalPanelFatigueFigsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelFatigueFigsPanel.setLayout(new BoxLayout(generalPanelFatigueFigsPanel, BoxLayout.X_AXIS));
		
		JLabel generalPanelFatigueFigsLabel = new JLabel("Fatigue figures:");
		generalPanelFatigueFigsPanel.add(generalPanelFatigueFigsLabel);
		generalPanelFatigueFigsLabel.setAlignmentY((float)0.9);
		generalPanelFatigueFigsLabel.setToolTipText("Number of significant figures past the decimal point to display on fatigue drops");
		
		generalPanelFatigueFigSpinner = new JSpinner();
		generalPanelFatigueFigsPanel.add(generalPanelFatigueFigSpinner);
		generalPanelFatigueFigSpinner.setMaximumSize(new Dimension(40, 22));
		generalPanelFatigueFigSpinner.setAlignmentY((float)0.7);
		generalPanelFatigueFigsPanel.setBorder(new EmptyBorder(0, 0, 7, 0));
		generalPanelFatigueFigSpinner.putClientProperty("JComponent.sizeVariant", "mini");
		
		// Sanitize JSpinner values
		SpinnerNumberModel spinnerNumModel = new SpinnerNumberModel();
		spinnerNumModel.setMinimum(1);
		spinnerNumModel.setMaximum(7);
		spinnerNumModel.setValue(2);
		generalPanelFatigueFigSpinner.setModel(spinnerNumModel);
		
		generalPanelFatigueAlertCheckbox = addCheckbox("Fatigue alert", generalPanel);
		generalPanelFatigueAlertCheckbox.setToolTipText("Displays a large notice when fatigue approaches 100%");
		
		generalPanelInventoryFullAlertCheckbox = addCheckbox("Inventory full alert", generalPanel);
		generalPanelInventoryFullAlertCheckbox.setToolTipText("Displays a large notice when the inventory is full");
		
		JPanel generalPanelNamePatchModePanel = new JPanel();
		generalPanelNamePatchModePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelNamePatchModePanel.setMaximumSize(new Dimension(300, 60));
		generalPanelNamePatchModePanel.setLayout(new BoxLayout(generalPanelNamePatchModePanel, BoxLayout.X_AXIS));
		generalPanel.add(generalPanelNamePatchModePanel);
		
		generalPanelNamePatchModeSlider = new JSlider();
		generalPanelNamePatchModeSlider.setMajorTickSpacing(1);
		generalPanelNamePatchModeSlider.setPaintLabels(true);
		generalPanelNamePatchModeSlider.setPaintTicks(true);
		generalPanelNamePatchModeSlider.setSnapToTicks(true);
		generalPanelNamePatchModeSlider.setMinimum(0);
		generalPanelNamePatchModeSlider.setMaximum(3);
		generalPanelNamePatchModeSlider.setPreferredSize(new Dimension(33, 0));
		generalPanelNamePatchModeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelNamePatchModeSlider.setBorder(new EmptyBorder(0, 0, 5, 0));
		generalPanelNamePatchModeSlider.setOrientation(SwingConstants.VERTICAL);
		generalPanelNamePatchModePanel.add(generalPanelNamePatchModeSlider);
		
		JPanel generalPanelNamePatchModeTextPanel = new JPanel();
		generalPanelNamePatchModeTextPanel.setPreferredSize(new Dimension(255, 55));
		generalPanelNamePatchModeTextPanel.setLayout(new BorderLayout());
		generalPanelNamePatchModeTextPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		generalPanelNamePatchModePanel.add(generalPanelNamePatchModeTextPanel);
		
		JLabel generalPanelNamePatchModeTitle = new JLabel("<html><b>Item name patch mode</b> (Requires restart)</html>");
		generalPanelNamePatchModeTitle.setToolTipText("Replace certain item names with improved versions");
		generalPanelNamePatchModeTextPanel.add(generalPanelNamePatchModeTitle, BorderLayout.PAGE_START);
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
		generalPanelRoofHidingCheckbox.setToolTipText("Always hide rooftops");
		
		generalPanelColoredTextCheckbox = addCheckbox("Colored console text", generalPanel);
		generalPanelColoredTextCheckbox.setToolTipText("When running the client from a console, chat messages in the console will reflect the colors they are in game");
		
		generalPanelCustomCursorCheckbox = addCheckbox("Use custom mouse cursor", generalPanel);
		generalPanelCustomCursorCheckbox.setToolTipText("Switch to using a custom mouse cursor instead of the system default");
		
		JLabel generalPanelFoVLabel = new JLabel("Field of view (Default 9)");
		generalPanelFoVLabel.setToolTipText("Sets the field of view (not recommended past 10)");
		generalPanel.add(generalPanelFoVLabel);
		generalPanelFoVLabel.setAlignmentY((float)0.9);
		
		generalPanelFoVSlider = new JSlider();
		
		generalPanel.add(generalPanelFoVSlider);
		generalPanelFoVSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelFoVSlider.setMaximumSize(new Dimension(200, 55));
		generalPanelFoVSlider.setBorder(new EmptyBorder(0, 0, 5, 0));
		generalPanelFoVSlider.setMinimum(7);
		generalPanelFoVSlider.setMaximum(16);
		generalPanelFoVSlider.setMajorTickSpacing(1);
		generalPanelFoVSlider.setPaintTicks(true);
		generalPanelFoVSlider.setPaintLabels(true);
		
		JLabel generalPanelViewDistanceLabel = new JLabel("View distance");
		generalPanelViewDistanceLabel.setToolTipText("Sets the max render distance of structures and landscape");
		generalPanel.add(generalPanelViewDistanceLabel);
		generalPanelViewDistanceLabel.setAlignmentY((float)0.9);
		
		generalPanelViewDistanceSlider = new JSlider();
		
		generalPanel.add(generalPanelViewDistanceSlider);
		generalPanelViewDistanceSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
		generalPanelViewDistanceSlider.setMaximumSize(new Dimension(200, 55));
		generalPanelViewDistanceSlider.setBorder(new EmptyBorder(0, 0, 0, 0));
		generalPanelViewDistanceSlider.setMinorTickSpacing(500);
		generalPanelViewDistanceSlider.setMajorTickSpacing(1000);
		generalPanelViewDistanceSlider.setMinimum(2300);
		generalPanelViewDistanceSlider.setMaximum(10000);
		generalPanelViewDistanceSlider.setPaintTicks(true);
		
		Hashtable<Integer, JLabel> generalPanelViewDistanceLabelTable = new Hashtable<Integer, JLabel>();
		generalPanelViewDistanceLabelTable.put(new Integer(2300), new JLabel("2,300"));
		generalPanelViewDistanceLabelTable.put(new Integer(10000), new JLabel("10,000"));
		generalPanelViewDistanceSlider.setLabelTable(generalPanelViewDistanceLabelTable);
		generalPanelViewDistanceSlider.setPaintLabels(true);
		
		generalPanelStartSearchedBankCheckbox = addCheckbox("Start with Searched Bank", generalPanel);
		generalPanelStartSearchedBankCheckbox.setToolTipText("Always start with a searched bank");

		JPanel searchBankPanel = new JPanel();
		generalPanel.add(searchBankPanel);
		searchBankPanel.setLayout(new BoxLayout(searchBankPanel, BoxLayout.X_AXIS));
		searchBankPanel.setPreferredSize(new Dimension(0, 37));
		searchBankPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		searchBankPanel.setBorder(new EmptyBorder(0, 0, 9, 0));

		JLabel searchBankPanelLabel = new JLabel("Search term used on bank: ");
		searchBankPanelLabel.setToolTipText("The search term that will be used on bank start");
		searchBankPanel.add(searchBankPanelLabel);
		searchBankPanelLabel.setAlignmentY((float)0.9);
		
		generalPanelSearchBankWordTextfield = new JTextField();
		searchBankPanel.add(generalPanelSearchBankWordTextfield);
		generalPanelSearchBankWordTextfield.setMinimumSize(new Dimension(100, 28));
		generalPanelSearchBankWordTextfield.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
		generalPanelSearchBankWordTextfield.setAlignmentY((float)0.75);
		
		/*
		 * Overlays tab
		 */
		
		overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
		
		overlayPanelStatusDisplayCheckbox = addCheckbox("Show HP/Prayer/Fatigue display", overlayPanel);
		overlayPanelStatusDisplayCheckbox.setToolTipText("Toggle hits/prayer/fatigue display");
		
		overlayPanelInvCountCheckbox = addCheckbox("Display inventory count", overlayPanel);
		overlayPanelInvCountCheckbox.setToolTipText("Shows the number of items in your inventory");
		
		overlayPanelItemNamesCheckbox = addCheckbox("Display item name overlay", overlayPanel);
		overlayPanelItemNamesCheckbox.setToolTipText("Shows the names of dropped items");
		
		overlayPanelPlayerNamesCheckbox = addCheckbox("Show player name overlay", overlayPanel);
		overlayPanelPlayerNamesCheckbox.setToolTipText("Shows players' display names over their character");
		
		overlayPanelFriendNamesCheckbox = addCheckbox("Show nearby friend name overlay", overlayPanel);
		overlayPanelFriendNamesCheckbox.setToolTipText("Shows your friends' display names over their character");
		
		overlayPanelNPCNamesCheckbox = addCheckbox("Display NPC name overlay", overlayPanel);
		overlayPanelNPCNamesCheckbox.setToolTipText("Shows NPC names over the NPC");
		
		overlayPanelNPCHitboxCheckbox = addCheckbox("Show character hitboxes", overlayPanel);
		overlayPanelNPCHitboxCheckbox.setToolTipText("Shows the clickable areas on NPCs and players");
		
		overlayPanelFoodHealingCheckbox = addCheckbox("Show food healing overlay (Not implemented yet)", overlayPanel);
		overlayPanelFoodHealingCheckbox.setToolTipText("When hovering on food, shows the HP a consumable recovers");
		// TODO: Remove this line when food healing overlay is implemented
		overlayPanelFoodHealingCheckbox.setEnabled(false);
		
		overlayPanelHPRegenTimerCheckbox = addCheckbox("Display time until next HP regeneration (Not implemented yet)", overlayPanel);
		overlayPanelHPRegenTimerCheckbox.setToolTipText("Shows the seconds until your HP will naturally regenerate");
		// TODO: Remove this line when the HP regen timer is implemented
		overlayPanelHPRegenTimerCheckbox.setEnabled(false);
		
		overlayPanelDebugModeCheckbox = addCheckbox("Enable debug mode", overlayPanel);
		overlayPanelDebugModeCheckbox.setToolTipText("Shows debug overlays and enables debug text in the console");

		// Blocked Items
		JPanel blockedItemsPanel = new JPanel();
		overlayPanel.add(blockedItemsPanel);
		blockedItemsPanel.setLayout(new BoxLayout(blockedItemsPanel, BoxLayout.X_AXIS));
		blockedItemsPanel.setPreferredSize(new Dimension(0,37));
		blockedItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		blockedItemsPanel.setBorder(new EmptyBorder(0,0,9,0));

		JLabel blockedItemsPanelNameLabel = new JLabel("Blocked items: ");
		blockedItemsPanel.add(blockedItemsPanelNameLabel);
		blockedItemsPanelNameLabel.setAlignmentY((float) 0.9);

		blockedItemsTextField = new JTextField();
		blockedItemsPanel.add(blockedItemsTextField);
		blockedItemsTextField.setMinimumSize(new Dimension(100,28));
		blockedItemsTextField.setMaximumSize(new Dimension(Short.MAX_VALUE,28));
		blockedItemsTextField.setAlignmentY((float) 0.75);

		// Highlighted Items
		JPanel highlightedItemsPanel = new JPanel();
		overlayPanel.add(highlightedItemsPanel);
		highlightedItemsPanel.setLayout(new BoxLayout(highlightedItemsPanel, BoxLayout.X_AXIS));
		highlightedItemsPanel.setPreferredSize(new Dimension(0,37));
		highlightedItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		highlightedItemsPanel.setBorder(new EmptyBorder(0,0,9,0));

		JLabel highlightedItemsPanelNameLabel = new JLabel("Highlighted items: ");
		highlightedItemsPanel.add(highlightedItemsPanelNameLabel);
		highlightedItemsPanelNameLabel.setAlignmentY((float) 0.9);

		highlightedItemsTextField = new JTextField();
		highlightedItemsPanel.add(highlightedItemsTextField);
		highlightedItemsTextField.setMinimumSize(new Dimension(100,28));
		highlightedItemsTextField.setMaximumSize(new Dimension(Short.MAX_VALUE,28));
		highlightedItemsTextField.setAlignmentY((float) 0.75);
		
		/*
		 * Notifications tab
		 */
		
		notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
		
		addNotificationCategory(notificationPanel, "Notification Settings");
		
		notificationPanelTrayPopupCheckbox = addCheckbox("Enable notification tray popups", notificationPanel);
		notificationPanelTrayPopupCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
		notificationPanelTrayPopupCheckbox.setToolTipText("Shows a system notification when a notification is triggered");
		
		ButtonGroup trayPopupButtonGroup = new ButtonGroup();
		notificationPanelTrayPopupClientFocusButton = addRadioButton("Only when client is not focused", notificationPanel, 20);
		notificationPanelTrayPopupAnyFocusButton = addRadioButton("Regardless of client focus", notificationPanel, 20);
		trayPopupButtonGroup.add(notificationPanelTrayPopupClientFocusButton);
		trayPopupButtonGroup.add(notificationPanelTrayPopupAnyFocusButton);
		
		notificationPanelNotifSoundsCheckbox = addCheckbox("Enable notification sounds", notificationPanel);
		notificationPanelNotifSoundsCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
		notificationPanelNotifSoundsCheckbox.setToolTipText("Plays a sound when a notification is triggered");
		
		ButtonGroup notifSoundButtonGroup = new ButtonGroup();
		notificationPanelNotifSoundClientFocusButton = addRadioButton("Only when client is not focused", notificationPanel, 20);
		notificationPanelNotifSoundAnyFocusButton = addRadioButton("Regardless of client focus", notificationPanel, 20);
		notifSoundButtonGroup.add(notificationPanelNotifSoundClientFocusButton);
		notifSoundButtonGroup.add(notificationPanelNotifSoundAnyFocusButton);
		
		if (SystemTray.isSupported())
			notificationPanelUseSystemNotifsCheckbox = addCheckbox("Use system notifications if available", notificationPanel);
		else {
			notificationPanelUseSystemNotifsCheckbox = addCheckbox("Use system notifications if available (INCOMPATIBLE OS)", notificationPanel);
			notificationPanelUseSystemNotifsCheckbox.setEnabled(false);
		}
		notificationPanelUseSystemNotifsCheckbox
				.setToolTipText("Uses built-in system notifications. Enable this to attempt to use your operating system's notification system instead of the built-in pop-up");
		
		addNotificationCategory(notificationPanel, "Notifications");
		
		notificationPanelPMNotifsCheckbox = addCheckbox("Enable PM notifications", notificationPanel);
		notificationPanelPMNotifsCheckbox.setToolTipText("Shows a system notification when a PM is received");
		
		notificationPanelTradeNotifsCheckbox = addCheckbox("Enable trade notifications", notificationPanel);
		notificationPanelTradeNotifsCheckbox.setToolTipText("Shows a system notification when a trade request is received");
		
		notificationPanelDuelNotifsCheckbox = addCheckbox("Enable duel notifications", notificationPanel);
		notificationPanelDuelNotifsCheckbox.setToolTipText("Shows a system notification when a duel request is received");
		
		notificationPanelLogoutNotifsCheckbox = addCheckbox("Enable logout notification", notificationPanel);
		notificationPanelLogoutNotifsCheckbox.setToolTipText("Shows a system notification when about to idle out");
		
		JPanel notificationPanelLowHPNotifsPanel = new JPanel();
		notificationPanel.add(notificationPanelLowHPNotifsPanel);
		notificationPanelLowHPNotifsPanel.setLayout(new BoxLayout(notificationPanelLowHPNotifsPanel, BoxLayout.X_AXIS));
		notificationPanelLowHPNotifsPanel.setPreferredSize(new Dimension(0, 37));
		notificationPanelLowHPNotifsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		notificationPanelLowHPNotifsCheckbox = addCheckbox("Enable low HP notification at", notificationPanelLowHPNotifsPanel);
		notificationPanelLowHPNotifsCheckbox.setToolTipText("Shows a system notification when your HP drops below the specified value");
		
		notificationPanelLowHPNotifsSpinner = new JSpinner();
		notificationPanelLowHPNotifsSpinner.setMaximumSize(new Dimension(45, 22));
		notificationPanelLowHPNotifsSpinner.setMinimumSize(new Dimension(45, 22));
		notificationPanelLowHPNotifsSpinner.setAlignmentY((float)0.75);
		notificationPanelLowHPNotifsPanel.add(notificationPanelLowHPNotifsSpinner);
		notificationPanelLowHPNotifsSpinner.putClientProperty("JComponent.sizeVariant", "mini");
		
		notificationPanelLowHPNotifsEndLabel = new JLabel("% HP");
		notificationPanelLowHPNotifsPanel.add(notificationPanelLowHPNotifsEndLabel);
		notificationPanelLowHPNotifsEndLabel.setAlignmentY((float)0.9);
		notificationPanelLowHPNotifsEndLabel.setBorder(new EmptyBorder(0, 2, 0, 0));
		
		// Sanitize JSpinner values
		SpinnerNumberModel spinnerHPNumModel = new SpinnerNumberModel();
		spinnerHPNumModel.setMinimum(1);
		spinnerHPNumModel.setMaximum(99);
		spinnerHPNumModel.setValue(25);
		notificationPanelLowHPNotifsSpinner.setModel(spinnerHPNumModel);
		
		JPanel notificationPanelFatigueNotifsPanel = new JPanel();
		notificationPanel.add(notificationPanelFatigueNotifsPanel);
		notificationPanelFatigueNotifsPanel.setLayout(new BoxLayout(notificationPanelFatigueNotifsPanel, BoxLayout.X_AXIS));
		notificationPanelFatigueNotifsPanel.setPreferredSize(new Dimension(0, 37));
		notificationPanelFatigueNotifsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		notificationPanelFatigueNotifsCheckbox = addCheckbox("Enable high fatigue notifications at", notificationPanelFatigueNotifsPanel);
		notificationPanelFatigueNotifsCheckbox.setToolTipText("Shows a system notification when your fatigue gets past the specified value");
		
		notificationPanelFatigueNotifsSpinner = new JSpinner();
		notificationPanelFatigueNotifsSpinner.setMaximumSize(new Dimension(45, 22));
		notificationPanelFatigueNotifsSpinner.setMinimumSize(new Dimension(45, 22));
		notificationPanelFatigueNotifsSpinner.setAlignmentY((float)0.75);
		notificationPanelFatigueNotifsPanel.add(notificationPanelFatigueNotifsSpinner);
		notificationPanelFatigueNotifsSpinner.putClientProperty("JComponent.sizeVariant", "mini");
		
		notificationPanelFatigueNotifsEndLabel = new JLabel("% fatigue");
		notificationPanelFatigueNotifsPanel.add(notificationPanelFatigueNotifsEndLabel);
		notificationPanelFatigueNotifsEndLabel.setAlignmentY((float)0.9);
		notificationPanelFatigueNotifsEndLabel.setBorder(new EmptyBorder(0, 2, 0, 0));
		
		// Sanitize JSpinner values
		SpinnerNumberModel spinnerFatigueNumModel = new SpinnerNumberModel();
		spinnerFatigueNumModel.setMinimum(1);
		spinnerFatigueNumModel.setMaximum(100);
		spinnerFatigueNumModel.setValue(98);
		notificationPanelFatigueNotifsSpinner.setModel(spinnerFatigueNumModel);
		
		/*
		 * Streaming & Privacy tab
		 */
		
		streamingPanel.setLayout(new BoxLayout(streamingPanel, BoxLayout.Y_AXIS));
		
		streamingPanelTwitchChatCheckbox = addCheckbox("Hide incoming Twitch chat", streamingPanel);
		streamingPanelTwitchChatCheckbox.setToolTipText("Don't show chat from other Twitch users, but still be able to send Twitch chat");
		
		JPanel streamingPanelTwitchChannelNamePanel = new JPanel();
		streamingPanel.add(streamingPanelTwitchChannelNamePanel);
		streamingPanelTwitchChannelNamePanel.setLayout(new BoxLayout(streamingPanelTwitchChannelNamePanel, BoxLayout.X_AXIS));
		streamingPanelTwitchChannelNamePanel.setPreferredSize(new Dimension(0, 37));
		streamingPanelTwitchChannelNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		streamingPanelTwitchChannelNamePanel.setBorder(new EmptyBorder(0, 0, 9, 0));
		
		JLabel streamingPanelTwitchChannelNameLabel = new JLabel("Twitch channel name: ");
		streamingPanelTwitchChannelNameLabel.setToolTipText("The Twitch channel you want to chat in (leave empty to stop trying to connect to Twitch)");
		streamingPanelTwitchChannelNamePanel.add(streamingPanelTwitchChannelNameLabel);
		streamingPanelTwitchChannelNameLabel.setAlignmentY((float)0.9);
		
		streamingPanelTwitchChannelNameTextField = new JTextField();
		streamingPanelTwitchChannelNamePanel.add(streamingPanelTwitchChannelNameTextField);
		streamingPanelTwitchChannelNameTextField.setMinimumSize(new Dimension(100, 28));
		streamingPanelTwitchChannelNameTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
		streamingPanelTwitchChannelNameTextField.setAlignmentY((float)0.75);
		
		JPanel streamingPanelTwitchUserPanel = new JPanel();
		streamingPanel.add(streamingPanelTwitchUserPanel);
		streamingPanelTwitchUserPanel.setLayout(new BoxLayout(streamingPanelTwitchUserPanel, BoxLayout.X_AXIS));
		streamingPanelTwitchUserPanel.setPreferredSize(new Dimension(0, 37));
		streamingPanelTwitchUserPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		streamingPanelTwitchUserPanel.setBorder(new EmptyBorder(0, 0, 9, 0));
		
		JLabel streamingPanelTwitchUserLabel = new JLabel("Twitch username: ");
		streamingPanelTwitchUserLabel.setToolTipText("The Twitch username you log into Twitch with");
		streamingPanelTwitchUserPanel.add(streamingPanelTwitchUserLabel);
		streamingPanelTwitchUserLabel.setAlignmentY((float)0.9);
		
		streamingPanelTwitchUserTextField = new JTextField();
		streamingPanelTwitchUserPanel.add(streamingPanelTwitchUserTextField);
		streamingPanelTwitchUserTextField.setMinimumSize(new Dimension(100, 28));
		streamingPanelTwitchUserTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
		streamingPanelTwitchUserTextField.setAlignmentY((float)0.75);
		
		JPanel streamingPanelTwitchOAuthPanel = new JPanel();
		streamingPanel.add(streamingPanelTwitchOAuthPanel);
		streamingPanelTwitchOAuthPanel.setLayout(new BoxLayout(streamingPanelTwitchOAuthPanel, BoxLayout.X_AXIS));
		streamingPanelTwitchOAuthPanel.setPreferredSize(new Dimension(0, 37));
		streamingPanelTwitchOAuthPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		streamingPanelTwitchOAuthPanel.setBorder(new EmptyBorder(0, 0, 9, 0));
		
		JLabel streamingPanelTwitchOAuthLabel = new JLabel("Twitch OAuth token: ");
		streamingPanelTwitchOAuthLabel.setToolTipText("Your Twitch OAuth token (not your Stream Key)");
		streamingPanelTwitchOAuthPanel.add(streamingPanelTwitchOAuthLabel);
		streamingPanelTwitchOAuthLabel.setAlignmentY((float)0.9);
		
		streamingPanelTwitchOAuthTextField = new JPasswordField();
		streamingPanelTwitchOAuthPanel.add(streamingPanelTwitchOAuthTextField);
		streamingPanelTwitchOAuthTextField.setMinimumSize(new Dimension(100, 28));
		streamingPanelTwitchOAuthTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
		streamingPanelTwitchOAuthTextField.setAlignmentY((float)0.75);
		
		streamingPanelIPAtLoginCheckbox = addCheckbox("Enable IP/DNS details at login welcome screen", streamingPanel);
		streamingPanelIPAtLoginCheckbox.setToolTipText("Shows the last IP/DNS you last logged in from when you log in (Disable this if you're streaming)");
		
		streamingPanelSaveLoginCheckbox = addCheckbox("Save login information between logins (Requires restart)", streamingPanel);
		streamingPanelSaveLoginCheckbox.setToolTipText("Preserves login details between logins (Disable this if you're streaming)");
		
		/*
		 * Keybind tab
		 */
		
		// TODO: Make the contents top aligned
		keybindPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		keybindPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		GridBagLayout gbl_panel = new GridBagLayout();
		
		keybindPanel.setLayout(gbl_panel);
		
		addKeybindCategory(keybindPanel, "General");
		addKeybindSet(keybindPanel, "Sleep", "sleep", KeyModifier.CTRL, KeyEvent.VK_X);
		addKeybindSet(keybindPanel, "Logout", "logout", KeyModifier.CTRL, KeyEvent.VK_L);
		addKeybindSet(keybindPanel, "Take screenshot", "screenshot", KeyModifier.CTRL, KeyEvent.VK_S);
		addKeybindSet(keybindPanel, "Show settings window", "show_config_window", KeyModifier.CTRL, KeyEvent.VK_O);
		addKeybindSet(keybindPanel, "Toggle combat XP menu persistence", "toggle_combat_xp_menu", KeyModifier.CTRL, KeyEvent.VK_C);
		addKeybindSet(keybindPanel, "Toggle XP drops", "toggle_xp_drops", KeyModifier.CTRL, KeyEvent.VK_OPEN_BRACKET);
		addKeybindSet(keybindPanel, "Toggle fatigue drops", "toggle_fatigue_drops", KeyModifier.CTRL, KeyEvent.VK_CLOSE_BRACKET);
		addKeybindSet(keybindPanel, "Toggle fatigue alert", "toggle_fatigue_alert", KeyModifier.CTRL, KeyEvent.VK_F);
		addKeybindSet(keybindPanel, "Toggle inventory full alert", "toggle_inventory_full_alert", KeyModifier.CTRL, KeyEvent.VK_V);
		addKeybindSet(keybindPanel, "Toggle roof hiding", "toggle_roof_hiding", KeyModifier.CTRL, KeyEvent.VK_R);
		addKeybindSet(keybindPanel, "Toggle color coded text", "toggle_colorize", KeyModifier.CTRL, KeyEvent.VK_Z);
		addKeybindSet(keybindPanel, "Toggle start with searched bank", "toggle_start_searched_bank", KeyModifier.CTRL, KeyEvent.VK_Q);
		
		addKeybindCategory(keybindPanel, "Overlays");
		addKeybindSet(keybindPanel, "Toggle HP/prayer/fatigue display", "toggle_hpprayerfatigue_display", KeyModifier.CTRL, KeyEvent.VK_U);
		addKeybindSet(keybindPanel, "Toggle inventory count overlay", "toggle_inven_count_overlay", KeyModifier.CTRL, KeyEvent.VK_E);
		addKeybindSet(keybindPanel, "Toggle item name overlay", "toggle_item_overlay", KeyModifier.CTRL, KeyEvent.VK_I);
		addKeybindSet(keybindPanel, "Toggle player name overlay", "toggle_player_name_overlay", KeyModifier.CTRL, KeyEvent.VK_P);
		addKeybindSet(keybindPanel, "Toggle friend name overlay", "toggle_friend_name_overlay", KeyModifier.NONE, -1);
		addKeybindSet(keybindPanel, "Toggle NPC name overlay", "toggle_npc_name_overlay", KeyModifier.CTRL, KeyEvent.VK_N);
		addKeybindSet(keybindPanel, "Toggle hitboxes", "toggle_hitboxes", KeyModifier.CTRL, KeyEvent.VK_H);
		addKeybindSet(keybindPanel, "Toggle food heal overlay", "toggle_food_heal_overlay", KeyModifier.CTRL, KeyEvent.VK_G);
		addKeybindSet(keybindPanel, "Toggle time until health regen", "toggle_health_regen_timer", KeyModifier.NONE, -1);
		addKeybindSet(keybindPanel, "Toggle debug mode", "toggle_debug", KeyModifier.CTRL, KeyEvent.VK_D);
		
		addKeybindCategory(keybindPanel, "Streaming & Privacy");
		addKeybindSet(keybindPanel, "Toggle Twitch chat", "toggle_twitch_chat", KeyModifier.CTRL, KeyEvent.VK_T);
		addKeybindSet(keybindPanel, "Toggle IP/DNS shown at login screen", "toggle_ipdns", KeyModifier.NONE, -1);
		// TODO: Uncomment the following line if this feature no longer requires a restart
		// addKeybindSet(keybindPanel, "Toggle save login information", "toggle_save_login_info", KeyModifier.NONE, -1);
		
		addKeybindCategory(keybindPanel, "Miscellaneous");
		addKeybindSet(keybindPanel, "Switch to world 1 at login screen", "world_1", KeyModifier.CTRL, KeyEvent.VK_1);
		addKeybindSet(keybindPanel, "Switch to world 2 at login screen", "world_2", KeyModifier.CTRL, KeyEvent.VK_2);
		addKeybindSet(keybindPanel, "Switch to world 3 at login screen", "world_3", KeyModifier.CTRL, KeyEvent.VK_3);
		addKeybindSet(keybindPanel, "Switch to world 4 at login screen", "world_4", KeyModifier.CTRL, KeyEvent.VK_4);
		addKeybindSet(keybindPanel, "Switch to world 5 at login screen", "world_5", KeyModifier.CTRL, KeyEvent.VK_5);
	}
	
	/**
	 * Adds a new keybind to the GUI and settings and registers it to be checked when keypresses are sent to the applet.
	 * 
	 * @param panel Panel to add the keybind label and button to
	 * @param labelText Text describing the keybind's function as shown to the user on the config window.
	 * @param commandID Unique String matching an entry in the processKeybindCommand switch statement.
	 * @param defaultModifier Default modifier value. This can be one of the enum values of KeybindSet.KeyModifier, eg
	 * KeyModifier.CTRL
	 * @param defaultKeyValue Default key value. This should match up with a KeyEvent.VK_ value. Set to -1 to set the
	 * default as NONE
	 */
	private void addKeybindSet(JPanel panel, String labelText, String commandID, KeyModifier defaultModifier, int defaultKeyValue) {
		addKeybindLabel(panel, labelText);
		String buttonText = defaultModifier.toString() + " + " + KeyEvent.getKeyText(defaultKeyValue);
		if (defaultKeyValue == -1)
			buttonText = "NONE";
		JButton b = addKeybindButton(panel, buttonText);
		KeybindSet kbs = new KeybindSet(b, commandID, defaultModifier, defaultKeyValue);
		KeyboardHandler.keybindSetList.add(kbs);
		setKeybindButtonText(kbs); // Set the text of the keybind button now that it has been initialized properly
		b.addActionListener(this.clickListener);
		b.addKeyListener(this.rebindListener);
		b.addFocusListener(focusListener);
		b.setFocusable(false);
		
		// Default KeybindSet
		KeyboardHandler.defaultKeybindSetList.put(commandID, new KeybindSet(null, commandID, defaultModifier, defaultKeyValue));
	}
	
	/**
	 * Tracks the number of keybind labels added to the keybind panel. Used to determine the gbc.gridy and panel
	 * preferred height.
	 */
	private int keybindLabelGridYCounter = 0;
	
	/**
	 * Adds a new label to the keybinds list. This should be used in conjunction with adding a button in a 1:1 ratio.
	 * The new label will be added below the existing ones.
	 * 
	 * @param panel Panel to add the label to.
	 * @param labelText Text of the label to add.
	 * @return The label that was added.
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
		return jlbl;
	}
	
	/**
	 * Tracks the number of keybind buttons added to the keybind panel. Used to determine the gbc.gridy.
	 */
	private int keybindButtonGridYCounter = 0;
	
	/**
	 * Adds a new button to the keybinds list. This should be used in conjunction with adding a label in a 1:1 ratio.
	 * The new button will be added below the existing ones.
	 * 
	 * @param panel Panel to add the button to.
	 * @param buttonText Text of the label to add.
	 * @return The label that was added.
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
	 * Adds a new category title to the keybinds list.
	 * 
	 * @param panel Panel to add the title to.
	 * @param categoryName Name of the category to add.
	 */
	private void addKeybindCategory(JPanel panel, String categoryName) {
		addKeybindCategoryLabel(panel, "<html><b>" + categoryName + "</b></html>");
		addKeybindCategorySeparator(panel);
		keybindButtonGridYCounter++;
		keybindLabelGridYCounter++;
	}
	
	/**
	 * Adds a new horizontal separator to the keybinds list. The JSeparator spans 2 columns.
	 * 
	 * @param panel Panel to add the separator to.
	 */
	private void addKeybindCategorySeparator(JPanel panel) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.gridx = 0;
		gbc.gridy = keybindButtonGridYCounter++;
		keybindLabelGridYCounter++;
		gbc.gridwidth = 2;
		
		panel.add(Box.createVerticalStrut(7), gbc);
		JSeparator jsep = new JSeparator(SwingConstants.HORIZONTAL);
		panel.add(jsep, gbc);
		panel.add(Box.createVerticalStrut(7), gbc);
	}
	
	/**
	 * Adds a new category label to the keybinds list. The JLabel spans 2 columns.
	 * 
	 * @param panel Panel to add the label to.
	 * @param categoryName Name of the category to add.
	 * @return The label that was added.
	 */
	private JLabel addKeybindCategoryLabel(JPanel panel, String categoryName) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		if (keybindLabelGridYCounter == 0)
			gbc.insets = new Insets(0, 0, 0, 0);
		else
			gbc.insets = new Insets(7, 0, 0, 0);
		gbc.gridy = keybindLabelGridYCounter++;
		keybindButtonGridYCounter++;
		gbc.weightx = 20;
		gbc.gridwidth = 2;
		
		JLabel jlbl = new JLabel(categoryName);
		panel.add(jlbl, gbc);
		return jlbl;
	}
	
	/**
	 * Adds a new category title to the notifications list.
	 * 
	 * @param panel Panel to add the title to.
	 * @param categoryName Name of the category to add.
	 */
	private void addNotificationCategory(JPanel panel, String categoryName) {
		addNotificationCategoryLabel(panel, "<html><b>" + categoryName + "</b></html>");
		addNotificationCategorySeparator(panel);
	}
	
	/**
	 * Adds a new horizontal separator to the notifications list.
	 * 
	 * @param panel Panel to add the separator to.
	 */
	private void addNotificationCategorySeparator(JPanel panel) {
		JSeparator jsep = new JSeparator(SwingConstants.HORIZONTAL);
		jsep.setMaximumSize(new Dimension(Short.MAX_VALUE, 7));
		panel.add(jsep);
	}
	
	/**
	 * Adds a new category label to the notifications list.
	 * 
	 * @param panel Panel to add the label to.
	 * @param categoryName Name of the category to add.
	 * @return The label that was added.
	 */
	private JLabel addNotificationCategoryLabel(JPanel panel, String categoryName) {
		JLabel jlbl = new JLabel(categoryName);
		panel.add(jlbl);
		return jlbl;
	}
	
	/**
	 * Adds a preconfigured JCheckbox to the specified container, setting its alignment constraint to left and adding an
	 * empty padding border.
	 * 
	 * @param text The text of the checkbox
	 * @param container The container to add the checkbox to.
	 * @return The newly created JCheckBox.
	 */
	private JCheckBox addCheckbox(String text, Container container) {
		JCheckBox checkbox = new JCheckBox(text);
		checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
		checkbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 5));
		container.add(checkbox);
		return checkbox;
	}
	
	/**
	 * Adds a preconfigured JButton to the specified container using the specified alignment constraint. Does not modify
	 * the button's border.
	 * 
	 * @param text The text of the button
	 * @param container The container to add the button to
	 * @param alignment The alignment of the button.
	 * @return The newly created JButton.
	 */
	private JButton addButton(String text, Container container, float alignment) {
		JButton button = new JButton(text);
		button.setAlignmentX(alignment);
		container.add(button);
		return button;
	}
	
	/**
	 * Adds a preconfigured radio button to the specified container. Does not currently assign the radio button to a
	 * group.
	 * 
	 * @param text The text of the radio button
	 * @param container The container to add the button to
	 * @param leftIndent The amount of padding to add to the left of the radio button as an empty border argument.
	 * @return The newly created JRadioButton
	 */
	private JRadioButton addRadioButton(String text, Container container, int leftIndent) {
		JRadioButton radioButton = new JRadioButton(text);
		radioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		radioButton.setBorder(BorderFactory.createEmptyBorder(0, leftIndent, 7, 5));
		container.add(radioButton);
		return radioButton;
	}
	
	/**
	 * Sets the scroll speed of a JScrollPane
	 * 
	 * @param scrollPane The JScrollPane to modify
	 * @param horizontalInc The horizontal increment value
	 * @param verticalInc The vertical increment value
	 */
	private void setScrollSpeed(JScrollPane scrollPane, int horizontalInc, int verticalInc) {
		scrollPane.getVerticalScrollBar().setUnitIncrement(verticalInc);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(horizontalInc);
	}
	
	/**
	 * Synchronizes all relevant values in the gui's elements to match those in Settings.java
	 */
	public void synchronizeGuiValues() {
		// General tab
		generalPanelClientSizeCheckbox.setSelected(Settings.CUSTOM_CLIENT_SIZE);
		generalPanelClientSizeXSpinner.setValue(Settings.CUSTOM_CLIENT_SIZE_X);
		generalPanelClientSizeYSpinner.setValue(Settings.CUSTOM_CLIENT_SIZE_Y);
		generalPanelChatHistoryCheckbox.setSelected(Settings.LOAD_CHAT_HISTORY); // TODO: Implement this feature
		generalPanelCombatXPMenuCheckbox.setSelected(Settings.COMBAT_MENU);
		generalPanelXPDropsCheckbox.setSelected(Settings.SHOW_XPDROPS);
		generalPanelXPCenterAlignFocusButton.setSelected(Settings.CENTER_XPDROPS);
		generalPanelXPRightAlignFocusButton.setSelected(!Settings.CENTER_XPDROPS);
		notificationPanelTrayPopupClientFocusButton.setSelected(!Settings.TRAY_NOTIFS_ALWAYS);
		notificationPanelTrayPopupAnyFocusButton.setSelected(Settings.TRAY_NOTIFS_ALWAYS);
		generalPanelFatigueDropsCheckbox.setSelected(Settings.SHOW_FATIGUEDROPS);
		generalPanelFatigueFigSpinner.setValue(new Integer(Settings.FATIGUE_FIGURES));
		generalPanelFatigueAlertCheckbox.setSelected(Settings.FATIGUE_ALERT);
		generalPanelInventoryFullAlertCheckbox.setSelected(Settings.INVENTORY_FULL_ALERT);
		generalPanelNamePatchModeSlider.setValue(Settings.NAME_PATCH_TYPE);
		generalPanelRoofHidingCheckbox.setSelected(Settings.HIDE_ROOFS);
		generalPanelColoredTextCheckbox.setSelected(Settings.COLORIZE);
		generalPanelFoVSlider.setValue(Settings.FOV);
		generalPanelCustomCursorCheckbox.setSelected(Settings.SOFTWARE_CURSOR);
		generalPanelViewDistanceSlider.setValue(Settings.VIEW_DISTANCE);
		generalPanelStartSearchedBankCheckbox.setSelected(Settings.START_SEARCHEDBANK);
		generalPanelSearchBankWordTextfield.setText(Settings.SEARCH_BANK_WORD);
		
		// Sets the text associated with the name patch slider.
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
		
		// Overlays tab
		overlayPanelStatusDisplayCheckbox.setSelected(Settings.SHOW_STATUSDISPLAY);
		overlayPanelInvCountCheckbox.setSelected(Settings.SHOW_INVCOUNT);
		overlayPanelItemNamesCheckbox.setSelected(Settings.SHOW_ITEMINFO);
		overlayPanelPlayerNamesCheckbox.setSelected(Settings.SHOW_PLAYERINFO);
		overlayPanelFriendNamesCheckbox.setSelected(Settings.SHOW_FRIENDINFO);
		overlayPanelNPCNamesCheckbox.setSelected(Settings.SHOW_NPCINFO);
		overlayPanelNPCHitboxCheckbox.setSelected(Settings.SHOW_HITBOX);
		overlayPanelFoodHealingCheckbox.setSelected(Settings.SHOW_FOOD_HEAL_OVERLAY); // TODO: Implement this feature
		overlayPanelHPRegenTimerCheckbox.setSelected(Settings.SHOW_TIME_UNTIL_HP_REGEN); // TODO: Implement this feature
		overlayPanelDebugModeCheckbox.setSelected(Settings.DEBUG);
		highlightedItemsTextField.setText(String.join(",", Settings.HIGHLIGHTED_ITEMS));
		blockedItemsTextField.setText(String.join(",", Settings.BLOCKED_ITEMS));
		
		// Notifications tab
		notificationPanelPMNotifsCheckbox.setSelected(Settings.PM_NOTIFICATIONS);
		notificationPanelTradeNotifsCheckbox.setSelected(Settings.TRADE_NOTIFICATIONS);
		notificationPanelDuelNotifsCheckbox.setSelected(Settings.DUEL_NOTIFICATIONS);
		notificationPanelLogoutNotifsCheckbox.setSelected(Settings.LOGOUT_NOTIFICATIONS);
		notificationPanelLowHPNotifsCheckbox.setSelected(Settings.LOW_HP_NOTIFICATIONS);
		notificationPanelLowHPNotifsSpinner.setValue(Settings.LOW_HP_NOTIF_VALUE);
		notificationPanelFatigueNotifsCheckbox.setSelected(Settings.FATIGUE_NOTIFICATIONS);
		notificationPanelFatigueNotifsSpinner.setValue(Settings.FATIGUE_NOTIF_VALUE);
		notificationPanelNotifSoundsCheckbox.setSelected(Settings.NOTIFICATION_SOUNDS);
		notificationPanelUseSystemNotifsCheckbox.setSelected(Settings.USE_SYSTEM_NOTIFICATIONS);
		notificationPanelTrayPopupCheckbox.setSelected(Settings.TRAY_NOTIFS);
		notificationPanelTrayPopupClientFocusButton.setSelected(!Settings.TRAY_NOTIFS_ALWAYS);
		notificationPanelTrayPopupAnyFocusButton.setSelected(Settings.TRAY_NOTIFS_ALWAYS);
		notificationPanelNotifSoundClientFocusButton.setSelected(!Settings.SOUND_NOTIFS_ALWAYS);
		notificationPanelNotifSoundAnyFocusButton.setSelected(Settings.SOUND_NOTIFS_ALWAYS);
		
		// Streaming & Privacy tab
		streamingPanelTwitchChatCheckbox.setSelected(Settings.TWITCH_HIDE);
		streamingPanelTwitchChannelNameTextField.setText(Settings.TWITCH_CHANNEL);
		streamingPanelTwitchOAuthTextField.setText(Settings.TWITCH_OAUTH);
		streamingPanelTwitchUserTextField.setText(Settings.TWITCH_USERNAME);
		streamingPanelIPAtLoginCheckbox.setSelected(Settings.SHOW_LOGINDETAILS);
		streamingPanelSaveLoginCheckbox.setSelected(Settings.SAVE_LOGININFO);
		
		for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
			setKeybindButtonText(kbs);
		}
	}
	
	/**
	 * Saves the settings from the GUI values to the settings class variables
	 */
	public void saveSettings() {
		// General options
		Settings.CUSTOM_CLIENT_SIZE = generalPanelClientSizeCheckbox.isSelected();
		Settings.CUSTOM_CLIENT_SIZE_X = ((SpinnerNumberModel)(generalPanelClientSizeXSpinner.getModel())).getNumber().intValue();
		Settings.CUSTOM_CLIENT_SIZE_Y = ((SpinnerNumberModel)(generalPanelClientSizeYSpinner.getModel())).getNumber().intValue();
		Settings.LOAD_CHAT_HISTORY = generalPanelChatHistoryCheckbox.isSelected();
		Settings.COMBAT_MENU = generalPanelCombatXPMenuCheckbox.isSelected();
		Settings.SHOW_XPDROPS = generalPanelXPDropsCheckbox.isSelected();
		Settings.CENTER_XPDROPS = generalPanelXPCenterAlignFocusButton.isSelected();
		Settings.SHOW_FATIGUEDROPS = generalPanelFatigueDropsCheckbox.isSelected();
		Settings.FATIGUE_FIGURES = ((SpinnerNumberModel)(generalPanelFatigueFigSpinner.getModel())).getNumber().intValue();
		Settings.FATIGUE_ALERT = generalPanelFatigueAlertCheckbox.isSelected();
		Settings.INVENTORY_FULL_ALERT = generalPanelInventoryFullAlertCheckbox.isSelected();
		Settings.NAME_PATCH_TYPE = generalPanelNamePatchModeSlider.getValue();
		Settings.HIDE_ROOFS = generalPanelRoofHidingCheckbox.isSelected();
		Settings.COLORIZE = generalPanelColoredTextCheckbox.isSelected();
		Settings.FOV = generalPanelFoVSlider.getValue();
		Settings.SOFTWARE_CURSOR = generalPanelCustomCursorCheckbox.isSelected();
		Settings.VIEW_DISTANCE = generalPanelViewDistanceSlider.getValue();
		Settings.START_SEARCHEDBANK = generalPanelStartSearchedBankCheckbox.isSelected();
		Settings.SEARCH_BANK_WORD = generalPanelSearchBankWordTextfield.getText().trim().toLowerCase();
		
		// Overlays options
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
		Settings.HIGHLIGHTED_ITEMS = new ArrayList<>(Arrays.asList(highlightedItemsTextField.getText().split(",")));
		Settings.BLOCKED_ITEMS = new ArrayList<>(Arrays.asList(blockedItemsTextField.getText().split(",")));

		// Notifications options
		Settings.PM_NOTIFICATIONS = notificationPanelPMNotifsCheckbox.isSelected();
		Settings.TRADE_NOTIFICATIONS = notificationPanelTradeNotifsCheckbox.isSelected();
		Settings.DUEL_NOTIFICATIONS = notificationPanelDuelNotifsCheckbox.isSelected();
		Settings.LOGOUT_NOTIFICATIONS = notificationPanelLogoutNotifsCheckbox.isSelected();
		Settings.LOW_HP_NOTIFICATIONS = notificationPanelLowHPNotifsCheckbox.isSelected();
		Settings.LOW_HP_NOTIF_VALUE = ((SpinnerNumberModel)(notificationPanelLowHPNotifsSpinner.getModel())).getNumber().intValue();
		Settings.FATIGUE_NOTIFICATIONS = notificationPanelFatigueNotifsCheckbox.isSelected();
		Settings.FATIGUE_NOTIF_VALUE = ((SpinnerNumberModel)(notificationPanelFatigueNotifsSpinner.getModel())).getNumber().intValue();
		Settings.NOTIFICATION_SOUNDS = notificationPanelNotifSoundsCheckbox.isSelected();
		Settings.USE_SYSTEM_NOTIFICATIONS = notificationPanelUseSystemNotifsCheckbox.isSelected();
		Settings.TRAY_NOTIFS = notificationPanelTrayPopupCheckbox.isSelected();
		Settings.TRAY_NOTIFS_ALWAYS = notificationPanelTrayPopupAnyFocusButton.isSelected();
		Settings.SOUND_NOTIFS_ALWAYS = notificationPanelNotifSoundAnyFocusButton.isSelected();
		
		// Streaming & Privacy
		Settings.TWITCH_HIDE = streamingPanelTwitchChatCheckbox.isSelected();
		Settings.TWITCH_CHANNEL = streamingPanelTwitchChannelNameTextField.getText();
		Settings.TWITCH_OAUTH = streamingPanelTwitchOAuthTextField.getText();
		Settings.TWITCH_USERNAME = streamingPanelTwitchUserTextField.getText();
		Settings.SHOW_LOGINDETAILS = streamingPanelIPAtLoginCheckbox.isSelected();
		Settings.SAVE_LOGININFO = streamingPanelSaveLoginCheckbox.isSelected();
		
		Settings.save();
	}
	
	public void disposeJFrame() {
		frame.dispose();
	}
	
	/**
	 * Sets the text of the button to its keybind.
	 * 
	 * @param kbs The KeybindSet object to set the button text of.
	 */
	public static void setKeybindButtonText(KeybindSet kbs) {
		kbs.button.setText(kbs.getFormattedKeybindText());
	}
	
	/**
	 * Applies the settings in the Config GUI to the Settings class variables. <br>
	 * <p>
	 * Note that this method should be used to apply any additional settings that are not applied automatically, such as
	 * those already present. Also note that thread-unsafe operations affecting the applet should not be done in this
	 * method, as this method is invoked by the AWT event queue.
	 * </p>
	 */
	public void applySettings() {
		saveSettings();
		if (Settings.CUSTOM_CLIENT_SIZE)
			Game.getInstance().resizeFrameWithContents();
		// Tell the Renderer to update the FoV from its thread to avoid thread-safety issues.
		Settings.fovUpdateRequired = true;
		Settings.checkSoftwareCursor();
		Camera.setDistance(Settings.VIEW_DISTANCE);
		
	}
}

/**
 * Implements ActionListener; to be used for the buttons in the keybinds tab.
 */
class ClickListener implements ActionListener {
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton)e.getSource();
		button.setText("...");
		button.setFocusable(true);
		button.requestFocusInWindow();
	}
}

class ButtonFocusListener implements FocusListener {
	
	@Override
	public void focusGained(FocusEvent arg0) {
	}
	
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
				if ((jbtn != kbs.button) && kbs.isDuplicateKeybindSet(modifier, key)) {
					jbtn.setText("DUPLICATE!");
					return;
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
	public void keyReleased(KeyEvent arg0) {
	}
	
	@Override
	public void keyTyped(KeyEvent arg0) {
	}
	
}
