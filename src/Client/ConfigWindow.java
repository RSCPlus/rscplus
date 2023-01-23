/**
 * rscplus
 *
 * <p>This file is part of rscplus.
 *
 * <p>rscplus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscplus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscplus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/RSCPlus/rscplus>
 */
package Client;

import Client.KeybindSet.KeyModifier;
import Game.Bank;
import Game.Camera;
import Game.Client;
import Game.Game;
import Game.GameApplet;
import Game.Item;
import Game.JoystickHandler;
import Game.KeyboardHandler;
import Game.Replay;
import Game.SoundEffects;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
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

/**
 * GUI designed for the RSCPlus client that manages configuration options and keybind values from
 * within an interface.
 *
 * <p><b>To add a new configuration option to the GUI,</b> <br>
 * 1.) Declare an instance variable to hold the gui element (eg checkbox) and add it to the GUI from
 * ConfigWindow.initialize() (see existing examples) <br>
 * 1.5.) If there is a helper method such as addCheckbox, use that method to create and store the
 * element that is returned in the ConfigWindow.initialize() method. See existing code for examples.
 * <br>
 * 2.) ^Add an appropriate variable to the Settings class as a class variable, <i>and</i> as an
 * assignment in the appropriate restore default method below. <br>
 * 3.) Add an entry in the ConfigWindow.synchronizeGuiValues() method that references the variable,
 * as per the already-existing examples.<br>
 * 4.) Add an entry in the ConfigWindow.saveSettings() method referencing the variable, as per the
 * already-existing examples.<br>
 * 5.) ^Add an entry in the Settings.Save() class save method to save the option to file.<br>
 * 6.) ^Add an entry in the Settings.Load() class load method to load the option from file.<br>
 * 7.) (Optional) If a method needs to be called to adjust settings other than the setting value
 * itself, add it to the ConfigWindow.applySettings() method below.<br>
 * <br>
 * <i>Entries marked with a ^ are steps used to add settings that are not included in the GUI.</i>
 * <br>
 * <br>
 * <b>To add a new keybind,</b><br>
 * 1.) Add a call in the initialize method to addKeybind with appropriate parameters.<br>
 * 2.) Add an entry to the command switch statement in Settings to process the command when its
 * keybind is pressed.<br>
 * 3.) Optional, recommended: Separate the command from its functionality by making a toggleBlah
 * method and calling it from the switch statement.<br>
 */
public class ConfigWindow {

  private JFrame frame;

  ClickListener clickListener = new ClickListener();
  RebindListener rebindListener = new RebindListener();

  ButtonFocusListener focusListener = new ButtonFocusListener();
  JTabbedPane tabbedPane;

  /*
   * JComponent variables which hold configuration data
   */

  //// General tab
  private JCheckBox generalPanelClientSizeCheckbox;
  private JSpinner generalPanelClientSizeXSpinner;
  private JSpinner generalPanelClientSizeYSpinner;
  private JCheckBox generalPanelCheckUpdates;
  private JCheckBox generalPanelAccountSecurityCheckbox;
  private JCheckBox generalPanelConfirmCancelRecoveryChangeCheckbox;
  private JCheckBox generalPanelShowSecurityTipsAtLoginCheckbox;
  private JCheckBox generalPanelWelcomeEnabled;
  // private JCheckBox generalPanelChatHistoryCheckbox;
  private JCheckBox generalPanelCombatXPMenuCheckbox;
  private JCheckBox generalPanelCombatXPMenuHiddenCheckbox;
  private JCheckBox generalPanelFatigueAlertCheckbox;
  private JCheckBox generalPanelInventoryFullAlertCheckbox;
  private JSlider generalPanelNamePatchModeSlider;
  private JSlider generalPanelLogVerbositySlider;
  private JCheckBox generalPanelLogLevelCheckbox;
  private JCheckBox generalPanelLogTimestampsCheckbox;
  private JCheckBox generalPanelLogForceLevelCheckbox;
  private JCheckBox generalPanelPrefersXdgOpenCheckbox;
  private JCheckBox generalPanelLogForceTimestampsCheckbox;
  private JCheckBox generalPanelCommandPatchQuestCheckbox;
  private JCheckBox generalPanelCommandPatchEdibleRaresCheckbox;
  private JCheckBox generalPanelCommandPatchDiskOfReturningCheckbox;
  private JCheckBox generalPanelBypassAttackCheckbox;
  private JCheckBox generalPanelEnableMouseWheelScrollingCheckbox;
  private JCheckBox generalPanelKeepScrollbarPosMagicPrayerCheckbox;
  private JCheckBox generalPanelRoofHidingCheckbox;
  private JCheckBox generalPanelDisableUndergroundLightingCheckbox;
  private JCheckBox generalPanelDisableMinimapRotationCheckbox;
  private JCheckBox generalPanelCameraZoomableCheckbox;
  private JCheckBox generalPanelCameraRotatableCheckbox;
  private JCheckBox generalPanelCameraMovableCheckbox;
  private JCheckBox generalPanelCameraMovableRelativeCheckbox;
  private JCheckBox generalPanelColoredTextCheckbox;
  private JSlider generalPanelFoVSlider;
  private JCheckBox generalPanelCustomCursorCheckbox;
  private JCheckBox generalPanelDisableRandomChatColourCheckbox;
  private JSlider generalPanelViewDistanceSlider;
  private JCheckBox generalPanelLimitFPSCheckbox;
  private JSpinner generalPanelLimitFPSSpinner;
  private JCheckBox generalPanelAutoScreenshotCheckbox;
  private JCheckBox generalPanelRS2HDSkyCheckbox;
  private JCheckBox generalPanelCustomSkyboxOverworldCheckbox;
  private JPanel generalPanelSkyOverworldColourColourPanel;
  private JCheckBox generalPanelCustomSkyboxUndergroundCheckbox;
  private JPanel generalPanelSkyUndergroundColourColourPanel;
  private Color overworldSkyColour =
      Util.intToColor(Settings.CUSTOM_SKYBOX_OVERWORLD_COLOUR.get(Settings.currentProfile));
  private Color undergroundSkyColour =
      Util.intToColor(Settings.CUSTOM_SKYBOX_UNDERGROUND_COLOUR.get(Settings.currentProfile));
  private JCheckBox generalPanelPatchGenderCheckbox;
  private JCheckBox generalPanelPatchHbar512LastPixelCheckbox;
  private JCheckBox generalPanelUseJagexFontsCheckBox;
  private JCheckBox generalPanelPatchWrenchMenuSpacingCheckbox;
  private JCheckBox generalPanelDebugModeCheckbox;
  private JCheckBox generalPanelExceptionHandlerCheckbox;
  private JLabel generalPanelNamePatchModeDesc;

  //// Overlays tab
  private JCheckBox overlayPanelStatusDisplayCheckbox;
  private JCheckBox overlayPanelBuffsCheckbox;
  private JCheckBox overlayPanelLastMenuActionCheckbox;
  private JCheckBox overlayPanelMouseTooltipCheckbox;
  private JCheckBox overlayPanelExtendedTooltipCheckbox;
  private JCheckBox overlayPanelInvCountCheckbox;
  private JCheckBox overlayPanelInvCountColoursCheckbox;
  private JCheckBox overlayPanelRscPlusButtonsCheckbox;
  private JCheckBox overlayPanelRscPlusButtonsFunctionalCheckbox;
  private JCheckBox overlayPanelWikiLookupOnMagicBookCheckbox;
  private JCheckBox overlayPanelWikiLookupOnHbarCheckbox;
  private JCheckBox overlayPanelToggleXPBarOnStatsButtonCheckbox;
  private JCheckBox overlayPaneHiscoresLookupButtonCheckbox;
  private JCheckBox overlayPanelToggleMotivationalQuotesCheckbox;
  private JCheckBox overlayPanelRemoveReportAbuseButtonHbarCheckbox;
  private JCheckBox overlayPanelPositionCheckbox;
  private JCheckBox overlayPanelRetroFpsCheckbox;
  private JCheckBox overlayPanelItemNamesCheckbox;
  private JCheckBox overlayPanelPlayerNamesCheckbox;
  private JCheckBox overlayPanelFriendNamesCheckbox;
  private JCheckBox overlayPanelNPCNamesCheckbox;
  private JCheckBox overlayPanelIDsCheckbox;
  private JCheckBox overlayPanelObjectInfoCheckbox;
  private JCheckBox overlayPanelHitboxCheckbox;
  private JCheckBox overlayPanelXPBarCheckbox;
  private JCheckBox overlayPanelXPDropsCheckbox;
  private JRadioButton overlayPanelXPCenterAlignFocusButton;
  private JRadioButton overlayPanelXPRightAlignFocusButton;
  private JCheckBox overlayPanelFatigueDropsCheckbox;
  private JSpinner overlayPanelFatigueFigSpinner;
  private JCheckBox overlayPanelFatigueUnitsCheckbox;
  private JCheckBox overlayPanelShowCombatInfoCheckbox;
  private JCheckBox overlayPanelUsePercentageCheckbox;
  private JCheckBox overlayPanelFoodHealingCheckbox;
  private JCheckBox overlayPanelHPRegenTimerCheckbox;
  private JCheckBox overlayPanelLagIndicatorCheckbox;
  private JTextField blockedItemsTextField;
  private JTextField highlightedItemsTextField;

  //// Audio tab
  private JCheckBox audioPanelEnableMusicCheckbox;
  private JCheckBox audioPanelLouderSoundEffectsCheckbox;
  private JCheckBox audioPanelOverrideAudioSettingCheckbox;
  private JRadioButton audioPanelOverrideAudioSettingOnButton;
  private JRadioButton audioPanelOverrideAudioSettingOffButton;
  private JCheckBox audioPanelFixSpiderWebDummySoundCheckbox;
  private JCheckBox soundEffectAdvanceCheckbox;
  private JCheckBox soundEffectAnvilCheckbox;
  private JCheckBox soundEffectChiselCheckbox;
  private JCheckBox soundEffectClickCheckbox;
  private JCheckBox soundEffectClosedoorCheckbox;
  private JCheckBox soundEffectCoinsCheckbox;
  private JCheckBox soundEffectCombat1aCheckbox;
  private JCheckBox soundEffectCombat1bCheckbox;
  private JCheckBox soundEffectCombat2aCheckbox;
  private JCheckBox soundEffectCombat2bCheckbox;
  private JCheckBox soundEffectCombat3aCheckbox;
  private JCheckBox soundEffectCombat3bCheckbox;
  private JCheckBox soundEffectCookingCheckbox;
  private JCheckBox soundEffectDeathCheckbox;
  private JCheckBox soundEffectDropobjectCheckbox;
  private JCheckBox soundEffectEatCheckbox;
  private JCheckBox soundEffectFilljugCheckbox;
  private JCheckBox soundEffectFishCheckbox;
  private JCheckBox soundEffectFoundgemCheckbox;
  private JCheckBox soundEffectMechanicalCheckbox;
  private JCheckBox soundEffectMineCheckbox;
  private JCheckBox soundEffectMixCheckbox;
  private JCheckBox soundEffectOpendoorCheckbox;
  private JCheckBox soundEffectOutofammoCheckbox;
  private JCheckBox soundEffectPotatoCheckbox;
  private JCheckBox soundEffectPrayeroffCheckbox;
  private JCheckBox soundEffectPrayeronCheckbox;
  private JCheckBox soundEffectProspectCheckbox;
  private JCheckBox soundEffectRechargeCheckbox;
  private JCheckBox soundEffectRetreatCheckbox;
  private JCheckBox soundEffectSecretdoorCheckbox;
  private JCheckBox soundEffectShootCheckbox;
  private JCheckBox soundEffectSpellfailCheckbox;
  private JCheckBox soundEffectSpellokCheckbox;
  private JCheckBox soundEffectTakeobjectCheckbox;
  private JCheckBox soundEffectUnderattackCheckbox;
  private JCheckBox soundEffectVictoryCheckbox;

  //// Bank tab
  private JCheckBox bankPanelStartSearchedBankCheckbox;
  private JTextField bankPanelSearchBankWordTextField;
  private JCheckBox bankPanelCalculateBankValueCheckbox;
  private JCheckBox bankPanelSortFilterAugmentCheckbox;
  private JLabel bankPanelImportLabel;
  private JLabel bankPanelExportLabel;

  //// Notifications tab
  private JCheckBox notificationPanelPMNotifsCheckbox;
  private JTextField notificationPanelPMDenyListTextField;
  private JCheckBox notificationPanelTradeNotifsCheckbox;
  private JCheckBox notificationPanelDuelNotifsCheckbox;
  private JCheckBox notificationPanelLogoutNotifsCheckbox;
  private JCheckBox notificationPanelLowHPNotifsCheckbox;
  private JSpinner notificationPanelLowHPNotifsSpinner;
  private JCheckBox notificationPanelFatigueNotifsCheckbox;
  private JSpinner notificationPanelFatigueNotifsSpinner;
  private JCheckBox notificationPanelHighlightedItemTimerCheckbox;
  private JSpinner notificationPanelHighlightedItemTimerSpinner;
  private JCheckBox notificationPanelNotifSoundsCheckbox;
  private JCheckBox notificationPanelUseSystemNotifsCheckbox;
  private JCheckBox notificationPanelTrayPopupCheckbox;
  private JRadioButton notificationPanelTrayPopupClientFocusButton;
  private JRadioButton notificationPanelNotifSoundClientFocusButton;
  private JRadioButton notificationPanelTrayPopupAnyFocusButton;
  private JRadioButton notificationPanelNotifSoundAnyFocusButton;
  private JTextField importantMessagesTextField;
  private JTextField importantSadMessagesTextField;
  private JCheckBox notificationPanelMuteImportantMessageSoundsCheckbox;

  //// Streaming & Privacy tab
  private JCheckBox streamingPanelTwitchChatCheckbox;
  private JCheckBox streamingPanelTwitchChatIntegrationEnabledCheckbox;
  private JTextField streamingPanelTwitchChannelNameTextField;
  private JTextField streamingPanelTwitchOAuthTextField;
  private JTextField streamingPanelTwitchUserTextField;
  private JCheckBox streamingPanelIPAtLoginCheckbox;
  private JCheckBox streamingPanelSaveLoginCheckbox;
  private JCheckBox streamingPanelStartLoginCheckbox;
  private JCheckBox streamingPanelSpeedrunnerCheckbox;
  // private JTextField streamingPanelSpeedrunnerUsernameTextField;

  //// Replay tab
  private JCheckBox replayPanelRecordKBMouseCheckbox;
  private JCheckBox replayPanelParseOpcodesCheckbox;
  private JCheckBox replayPanelFastDisconnectCheckbox;
  private JCheckBox replayPanelRecordAutomaticallyCheckbox;
  private JCheckBox replayPanelHidePrivateMessagesCheckbox;
  private JCheckBox replayPanelShowSeekBarCheckbox;
  private JCheckBox replayPanelShowPlayerControlsCheckbox;
  private JCheckBox replayPanelTriggerAlertsReplayCheckbox;
  private JTextField replayPanelDateFormatTextField;
  private JTextField replayPanelReplayFolderBasePathTextField;
  private JCheckBox replayPanelShowWorldColumnCheckbox;
  private JCheckBox replayPanelShowConversionSettingsCheckbox;
  private JCheckBox replayPanelShowUserFieldCheckbox;

  //// Presets tab
  private JCheckBox presetsPanelCustomSettingsCheckbox;
  private JSlider presetsPanelPresetSlider;
  private JButton replaceConfigButton;
  private JButton resetPresetsButton;
  private int sliderValue = -1;

  //// World List tab
  private HashMap<Integer, JTextField> worldNamesJTextFields = new HashMap<Integer, JTextField>();
  private HashMap<Integer, JComboBox> worldTypesJComboBoxes = new HashMap<Integer, JComboBox>();
  private HashMap<Integer, JButton> worldDeleteJButtons = new HashMap<Integer, JButton>();
  private HashMap<Integer, JTextField> worldUrlsJTextFields = new HashMap<Integer, JTextField>();
  private HashMap<Integer, JTextField> worldPortsJTextFields = new HashMap<Integer, JTextField>();
  private HashMap<Integer, JTextField> worldRSAPubKeyJTextFields =
      new HashMap<Integer, JTextField>();
  private HashMap<Integer, JTextField> worldRSAExponentsJTextFields =
      new HashMap<Integer, JTextField>();
  private HashMap<Integer, JPanel> worldListTitleTextFieldContainers =
      new HashMap<Integer, JPanel>();
  private HashMap<Integer, JPanel> worldListURLPortTextFieldContainers =
      new HashMap<Integer, JPanel>();
  private HashMap<Integer, JPanel> worldListRSATextFieldContainers = new HashMap<Integer, JPanel>();
  private HashMap<Integer, JPanel> worldListHiscoresTextFieldContainers =
      new HashMap<Integer, JPanel>();
  private HashMap<Integer, JTextField> worldListHiscoresURLTextFieldContainers =
      new HashMap<Integer, JTextField>();
  private HashMap<Integer, JLabel> worldListSpacingLabels = new HashMap<Integer, JLabel>();
  private JPanel worldListPanel = new JPanel();

  //// Joystick tab
  private JCheckBox joystickPanelJoystickEnabledCheckbox;
  private HashMap<String, JLabel> joystickInputJlabels = new LinkedHashMap<String, JLabel>();
  private HashMap<String, JLabel> joystickInputValueJlabels = new HashMap<String, JLabel>();
  private JPanel joystickPanel = new JPanel();

  public ConfigWindow() {
    try {
      // Set System L&F as a fall-back option.
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
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

  public void showConfigWindow() {
    Client.displayMessage("Showing config window...", Client.CHAT_NONE);

    this.synchronizeGuiValues();
    frame.setVisible(true);
  }

  public void hideConfigWindow() {
    Client.displayMessage("Hid the config window.", Client.CHAT_NONE);

    frame.setVisible(false);
  }

  public void toggleConfigWindow() {
    if (this.isShown()) {
      this.hideConfigWindow();
    } else {
      this.showConfigWindow();
    }
  }

  public boolean isShown() {
    return frame.isVisible();
  }

  /** Initialize the contents of the frame. */
  private void initialize() {
    Logger.Info("Creating configuration window");
    try {
      SwingUtilities.invokeAndWait(
          new Runnable() {

            @Override
            public void run() {
              runInit();
            }
          });
    } catch (InvocationTargetException e) {
      Logger.Error("There was a thread-related error while setting up the config window!");
      e.printStackTrace();
    } catch (InterruptedException e) {
      Logger.Error(
          "There was a thread-related error while setting up the config window! The window may not be initialized properly!");
      e.printStackTrace();
    }
  }

  private void runInit() {
    frame = new JFrame();
    frame.setTitle("Settings");
    frame.setBounds(100, 100, 800, 650);
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout(0, 0));
    URL iconURL = Launcher.getResource("/assets/icon.png");
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      frame.setIconImage(icon.getImage());
    }

    // Container declarations
    /** The tabbed pane holding the five configuration tabs */
    tabbedPane = new JTabbedPane();
    /**
     * The JPanel containing the OK, Cancel, Apply, and Restore Defaults buttons at the bottom of
     * the window
     */
    JPanel navigationPanel = new JPanel();

    JScrollPane presetsScrollPane = new JScrollPane();
    JScrollPane generalScrollPane = new JScrollPane();
    JScrollPane overlayScrollPane = new JScrollPane();
    JScrollPane audioScrollPane = new JScrollPane();
    JScrollPane bankScrollPane = new JScrollPane();
    JScrollPane notificationScrollPane = new JScrollPane();
    JScrollPane streamingScrollPane = new JScrollPane();
    JScrollPane keybindScrollPane = new JScrollPane();
    JScrollPane replayScrollPane = new JScrollPane();
    JScrollPane worldListScrollPane = new JScrollPane();
    JScrollPane authorsScrollPane = new JScrollPane();
    JScrollPane joystickScrollPane = new JScrollPane();

    JPanel presetsPanel = new JPanel();
    JPanel generalPanel = new JPanel();
    JPanel overlayPanel = new JPanel();
    JPanel audioPanel = new JPanel();
    JPanel bankPanel = new JPanel();
    JPanel notificationPanel = new JPanel();
    JPanel streamingPanel = new JPanel();
    JPanel keybindPanel = new JPanel();
    JPanel replayPanel = new JPanel();
    joystickPanel = new JPanel();
    worldListPanel = new JPanel();
    JPanel authorsPanel = new JPanel();

    frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
    frame.getContentPane().add(navigationPanel, BorderLayout.PAGE_END);

    tabbedPane.addTab("Presets", null, presetsScrollPane, null);
    tabbedPane.addTab("General", null, generalScrollPane, null);
    tabbedPane.addTab("Overlays", null, overlayScrollPane, null);
    tabbedPane.addTab("Audio", null, audioScrollPane, null);
    tabbedPane.addTab("Bank", null, bankScrollPane, null);
    tabbedPane.addTab("Notifications", null, notificationScrollPane, null);
    tabbedPane.addTab("Streaming & Privacy", null, streamingScrollPane, null);
    tabbedPane.addTab("Keybinds", null, keybindScrollPane, null);
    tabbedPane.addTab("Replay", null, replayScrollPane, null);
    tabbedPane.addTab("World List", null, worldListScrollPane, null);
    tabbedPane.addTab("Joystick", null, joystickScrollPane, null);
    tabbedPane.addTab("Authors", null, authorsScrollPane, null);

    presetsScrollPane.setViewportView(presetsPanel);
    generalScrollPane.setViewportView(generalPanel);
    overlayScrollPane.setViewportView(overlayPanel);
    audioScrollPane.setViewportView(audioPanel);
    bankScrollPane.setViewportView(bankPanel);
    notificationScrollPane.setViewportView(notificationPanel);
    streamingScrollPane.setViewportView(streamingPanel);
    keybindScrollPane.setViewportView(keybindPanel);
    replayScrollPane.setViewportView(replayPanel);
    worldListScrollPane.setViewportView(worldListPanel);
    authorsScrollPane.setViewportView(authorsPanel);
    joystickScrollPane.setViewportView(joystickPanel);

    // Adding padding for aesthetics
    navigationPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 10, 10));
    presetsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    generalPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    overlayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    audioPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    bankPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    notificationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    streamingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    keybindPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    replayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    worldListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    authorsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    joystickPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    setScrollSpeed(presetsScrollPane, 20, 15);
    setScrollSpeed(generalScrollPane, 20, 15);
    setScrollSpeed(overlayScrollPane, 20, 15);
    setScrollSpeed(bankScrollPane, 20, 15);
    setScrollSpeed(audioScrollPane, 20, 15);
    setScrollSpeed(notificationScrollPane, 20, 15);
    setScrollSpeed(streamingScrollPane, 20, 15);
    setScrollSpeed(keybindScrollPane, 20, 15);
    setScrollSpeed(replayScrollPane, 20, 15);
    setScrollSpeed(worldListScrollPane, 20, 15);
    setScrollSpeed(authorsScrollPane, 20, 15);
    setScrollSpeed(joystickScrollPane, 20, 15);

    /*
     * Navigation buttons
     */

    navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.X_AXIS));

    addButton("OK", navigationPanel, Component.LEFT_ALIGNMENT)
        .addActionListener(
            new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent e) {
                Launcher.getConfigWindow().saveSettings();
                Launcher.getConfigWindow().hideConfigWindow();
              }
            });

    addButton("Cancel", navigationPanel, Component.LEFT_ALIGNMENT)
        .addActionListener(
            new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent e) {
                Launcher.getConfigWindow().applySettings();
                Launcher.getConfigWindow().hideConfigWindow();
              }
            });

    addButton("Apply", navigationPanel, Component.LEFT_ALIGNMENT)
        .addActionListener(
            new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent e) {
                Launcher.getConfigWindow().applySettings();
              }
            });

    navigationPanel.add(Box.createHorizontalGlue());
    addButton("Restore Defaults", navigationPanel, Component.RIGHT_ALIGNMENT)
        .addActionListener(
            new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent e) {
                int choice =
                    JOptionPane.showConfirmDialog(
                        Launcher.getConfigWindow().frame,
                        "Are you sure you want to restore all settings to their defaults?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.CLOSED_OPTION || choice == JOptionPane.NO_OPTION) {
                  return;
                }

                Settings.initSettings(); // make sure "default" is really default
                Settings.save("default");
                synchronizeGuiValues();

                // Restore defaults
                /* TODO: reimplement per-tab defaults?
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
                            //TODO more pages
                default:
                	Logger.Error("Restore defaults attempted to operate on a non-existent tab!");
                }
                            */
              }
            });

    /*
     * General tab
     */

    generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));

    /// "Client settings" are settings related to just setting up how the client behaves
    /// Not really anything related to gameplay, just being able to set up the client
    /// the way the user wants it
    addSettingsHeader(generalPanel, "Client settings");

    JPanel generalPanelClientSizePanel = new JPanel();
    generalPanel.add(generalPanelClientSizePanel);
    generalPanelClientSizePanel.setLayout(
        new BoxLayout(generalPanelClientSizePanel, BoxLayout.X_AXIS));
    generalPanelClientSizePanel.setPreferredSize(new Dimension(0, 37));
    generalPanelClientSizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // TODO: Perhaps change to "Save client size on close"?
    generalPanelClientSizeCheckbox =
        addCheckbox("Default client size:", generalPanelClientSizePanel);
    generalPanelClientSizeCheckbox.setToolTipText("Start the client with the supplied window size");

    generalPanelClientSizeXSpinner = new JSpinner();
    generalPanelClientSizePanel.add(generalPanelClientSizeXSpinner);
    generalPanelClientSizeXSpinner.setMaximumSize(new Dimension(58, 22));
    generalPanelClientSizeXSpinner.setMinimumSize(new Dimension(58, 22));
    generalPanelClientSizeXSpinner.setAlignmentY((float) 0.75);
    generalPanelClientSizeXSpinner.setToolTipText("Default client width (512 minimum)");
    generalPanelClientSizeXSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    JLabel generalPanelClientSizeByLabel = new JLabel("x");
    generalPanelClientSizePanel.add(generalPanelClientSizeByLabel);
    generalPanelClientSizeByLabel.setAlignmentY((float) 0.9);
    generalPanelClientSizeByLabel.setBorder(new EmptyBorder(0, 2, 0, 2));

    generalPanelClientSizeYSpinner = new JSpinner();
    generalPanelClientSizePanel.add(generalPanelClientSizeYSpinner);
    generalPanelClientSizeYSpinner.setMaximumSize(new Dimension(58, 22));
    generalPanelClientSizeYSpinner.setMinimumSize(new Dimension(58, 22));
    generalPanelClientSizeYSpinner.setAlignmentY((float) 0.75);
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

    generalPanelCheckUpdates =
        addCheckbox("Check for rscplus updates from GitHub at launch", generalPanel);
    generalPanelCheckUpdates.setToolTipText(
        "When enabled, rscplus will check for client updates before launching the game and install them when prompted");

    generalPanelWelcomeEnabled =
        addCheckbox("Remind you how to open the Settings every time you log in", generalPanel);
    generalPanelWelcomeEnabled.setToolTipText(
        "When enabled, rscplus will insert a message telling the current keybinding to open the settings menu and remind you about the tray icon");

    generalPanelAccountSecurityCheckbox =
        addCheckbox(
            "Show Account Creation and Security Settings (Requires restart for Account Creation and Recovery)",
            generalPanel);
    generalPanelAccountSecurityCheckbox.setToolTipText(
        "Makes old RSC account creation, password recovery and in-game security settings");

    generalPanelConfirmCancelRecoveryChangeCheckbox =
        addCheckbox("Show Cancel Recovery Change Confirmation Box", generalPanel);
    generalPanelConfirmCancelRecoveryChangeCheckbox.setToolTipText(
        "Displays a confirmation dialog box when clicking cancel recovery question change of welcome screen");

    generalPanelShowSecurityTipsAtLoginCheckbox =
        addCheckbox("Show Security tip of the day at login welcome screen", generalPanel);
    generalPanelShowSecurityTipsAtLoginCheckbox.setToolTipText(
        "Displays old RSC Security tip of the day at welcome screen if player has recovery questions permanently set");

    generalPanelCustomCursorCheckbox = addCheckbox("Use custom mouse cursor", generalPanel);
    generalPanelCustomCursorCheckbox.setToolTipText(
        "Switch to using a custom mouse cursor instead of the system default");

    generalPanelDisableRandomChatColourCheckbox =
        addCheckbox("Disable \"@ran@\" chat colour effect", generalPanel);
    generalPanelDisableRandomChatColourCheckbox.setToolTipText(
        "The random chat colour effect will be no longer be displayed");

    generalPanelAutoScreenshotCheckbox =
        addCheckbox("Take a screenshot when you level up or complete a quest", generalPanel);
    generalPanelAutoScreenshotCheckbox.setToolTipText(
        "Takes a screenshot for you for level ups and quest completion");

    generalPanelRS2HDSkyCheckbox =
        addCheckbox("Use RS2: HD sky colours (overrides custom colours below)", generalPanel);
    generalPanelRS2HDSkyCheckbox.setToolTipText("Uses sky colours from RS2: HD");

    // colour choose overworld sub-panel
    JPanel generalPanelSkyOverworldColourPanel = new JPanel();
    generalPanel.add(generalPanelSkyOverworldColourPanel);
    generalPanelSkyOverworldColourPanel.setLayout(
        new BoxLayout(generalPanelSkyOverworldColourPanel, BoxLayout.X_AXIS));
    generalPanelSkyOverworldColourPanel.setPreferredSize(new Dimension(0, 30));
    generalPanelSkyOverworldColourPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelCustomSkyboxOverworldCheckbox =
        addCheckbox("Use a custom colour for the sky", generalPanelSkyOverworldColourPanel);
    generalPanelCustomSkyboxOverworldCheckbox.setToolTipText(
        "You can set your own preferred colour for what you think the sky should have");

    generalPanelSkyOverworldColourColourPanel = new JPanel();
    generalPanelSkyOverworldColourPanel.add(generalPanelSkyOverworldColourColourPanel);
    generalPanelSkyOverworldColourColourPanel.setAlignmentY((float) 0.7);
    generalPanelSkyOverworldColourColourPanel.setMinimumSize(new Dimension(32, 20));
    generalPanelSkyOverworldColourColourPanel.setPreferredSize(new Dimension(32, 20));
    generalPanelSkyOverworldColourColourPanel.setMaximumSize(new Dimension(32, 20));
    generalPanelSkyOverworldColourColourPanel.setBorder(
        BorderFactory.createLineBorder(Color.black));
    generalPanelSkyOverworldColourColourPanel.setBackground(overworldSkyColour);

    JPanel generalPanelSkyOverworldColourColourSpacingPanel = new JPanel();
    generalPanelSkyOverworldColourPanel.add(generalPanelSkyOverworldColourColourSpacingPanel);
    generalPanelSkyOverworldColourColourSpacingPanel.setMinimumSize(new Dimension(4, 20));
    generalPanelSkyOverworldColourColourSpacingPanel.setPreferredSize(new Dimension(4, 20));
    generalPanelSkyOverworldColourColourSpacingPanel.setMaximumSize(new Dimension(4, 20));

    JButton overworldSkyColourChooserButton = new JButton("Choose colour");
    overworldSkyColourChooserButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Color selected =
                JColorChooser.showDialog(null, "Choose Overworld Sky Colour", overworldSkyColour);
            if (null != selected) {
              overworldSkyColour = selected;
            }
            generalPanelSkyOverworldColourColourPanel.setBackground(overworldSkyColour);
          }
        });
    generalPanelSkyOverworldColourPanel.add(overworldSkyColourChooserButton);
    overworldSkyColourChooserButton.setAlignmentY(.7f);

    // choose colour for underground subpanel
    JPanel generalPanelSkyUndergroundColourPanel = new JPanel();
    generalPanel.add(generalPanelSkyUndergroundColourPanel);
    generalPanelSkyUndergroundColourPanel.setLayout(
        new BoxLayout(generalPanelSkyUndergroundColourPanel, BoxLayout.X_AXIS));
    generalPanelSkyUndergroundColourPanel.setPreferredSize(new Dimension(0, 30));
    generalPanelSkyUndergroundColourPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelCustomSkyboxUndergroundCheckbox =
        addCheckbox(
            "Use a custom colour for the sky when underground",
            generalPanelSkyUndergroundColourPanel);
    generalPanelCustomSkyboxUndergroundCheckbox.setToolTipText(
        "You can set your own preferred colour for what you think the sky should have (underground)");

    generalPanelSkyUndergroundColourColourPanel = new JPanel();
    generalPanelSkyUndergroundColourPanel.add(generalPanelSkyUndergroundColourColourPanel);
    generalPanelSkyUndergroundColourColourPanel.setAlignmentY((float) 0.7);
    generalPanelSkyUndergroundColourColourPanel.setMinimumSize(new Dimension(32, 20));
    generalPanelSkyUndergroundColourColourPanel.setPreferredSize(new Dimension(32, 20));
    generalPanelSkyUndergroundColourColourPanel.setMaximumSize(new Dimension(32, 20));
    generalPanelSkyUndergroundColourColourPanel.setBorder(
        BorderFactory.createLineBorder(Color.black));
    generalPanelSkyUndergroundColourColourPanel.setBackground(undergroundSkyColour);

    JPanel generalPanelSkyUndergroundColourColourSpacingPanel = new JPanel();
    generalPanelSkyUndergroundColourPanel.add(generalPanelSkyUndergroundColourColourSpacingPanel);
    generalPanelSkyUndergroundColourColourSpacingPanel.setMinimumSize(new Dimension(4, 20));
    generalPanelSkyUndergroundColourColourSpacingPanel.setPreferredSize(new Dimension(4, 20));
    generalPanelSkyUndergroundColourColourSpacingPanel.setMaximumSize(new Dimension(4, 20));

    JButton undergroundSkyColourChooserButton = new JButton("Choose colour");
    undergroundSkyColourChooserButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Color selected =
                JColorChooser.showDialog(
                    null, "Choose Underground Sky Colour", undergroundSkyColour);
            if (null != selected) {
              undergroundSkyColour = selected;
            }
            generalPanelSkyUndergroundColourColourPanel.setBackground(undergroundSkyColour);
          }
        });
    generalPanelSkyUndergroundColourPanel.add(undergroundSkyColourChooserButton);
    undergroundSkyColourChooserButton.setAlignmentY(.7f);

    // sliders
    JLabel generalPanelFoVLabel = new JLabel("Field of view (Default 9)");
    generalPanelFoVLabel.setToolTipText("Sets the field of view (not recommended past 10)");
    generalPanel.add(generalPanelFoVLabel);
    generalPanelFoVLabel.setAlignmentY((float) 0.9);

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
    generalPanelViewDistanceLabel.setToolTipText(
        "Sets the max render distance of structures and landscape");
    generalPanel.add(generalPanelViewDistanceLabel);
    generalPanelViewDistanceLabel.setAlignmentY((float) 0.9);

    generalPanelViewDistanceSlider = new JSlider();

    generalPanel.add(generalPanelViewDistanceSlider);
    generalPanelViewDistanceSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelViewDistanceSlider.setMaximumSize(new Dimension(200, 55));
    generalPanelViewDistanceSlider.setBorder(new EmptyBorder(0, 0, 5, 0));
    generalPanelViewDistanceSlider.setMinorTickSpacing(500);
    generalPanelViewDistanceSlider.setMajorTickSpacing(1000);
    generalPanelViewDistanceSlider.setMinimum(2300);
    generalPanelViewDistanceSlider.setMaximum(20000);
    generalPanelViewDistanceSlider.setPaintTicks(true);

    Hashtable<Integer, JLabel> generalPanelViewDistanceLabelTable =
        new Hashtable<Integer, JLabel>();
    generalPanelViewDistanceLabelTable.put(new Integer(2300), new JLabel("2,300"));
    generalPanelViewDistanceLabelTable.put(new Integer(10000), new JLabel("10,000"));
    generalPanelViewDistanceLabelTable.put(new Integer(20000), new JLabel("20,000"));
    generalPanelViewDistanceSlider.setLabelTable(generalPanelViewDistanceLabelTable);
    generalPanelViewDistanceSlider.setPaintLabels(true);

    //////
    JPanel generalPanelLimitFPSPanel = new JPanel();
    generalPanel.add(generalPanelLimitFPSPanel);
    generalPanelLimitFPSPanel.setLayout(new BoxLayout(generalPanelLimitFPSPanel, BoxLayout.X_AXIS));
    generalPanelLimitFPSPanel.setPreferredSize(new Dimension(0, 37));
    generalPanelLimitFPSPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelLimitFPSCheckbox =
        addCheckbox("FPS limit (doubled while F1 interlaced):", generalPanelLimitFPSPanel);
    generalPanelLimitFPSCheckbox.setToolTipText(
        "Limit FPS for a more 2001 feeling (or to save battery)");

    generalPanelLimitFPSSpinner = new JSpinner();
    generalPanelLimitFPSPanel.add(generalPanelLimitFPSSpinner);
    generalPanelLimitFPSSpinner.setMaximumSize(new Dimension(45, 22));
    generalPanelLimitFPSSpinner.setMinimumSize(new Dimension(45, 22));
    generalPanelLimitFPSSpinner.setAlignmentY((float) 0.75);
    generalPanelLimitFPSSpinner.setToolTipText("Target FPS");
    generalPanelLimitFPSSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    // Sanitize JSpinner value
    SpinnerNumberModel spinnerLimitFpsModel = new SpinnerNumberModel();
    spinnerLimitFpsModel.setMinimum(1);
    spinnerLimitFpsModel.setMaximum(50);
    spinnerLimitFpsModel.setValue(10);
    spinnerLimitFpsModel.setStepSize(1);
    generalPanelLimitFPSSpinner.setModel(spinnerLimitFpsModel);
    //////

    JPanel generalPanelLogVerbosityPanel = new JPanel();
    generalPanelLogVerbosityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelLogVerbosityPanel.setMaximumSize(new Dimension(350, 128));
    generalPanelLogVerbosityPanel.setLayout(
        new BoxLayout(generalPanelLogVerbosityPanel, BoxLayout.Y_AXIS));
    generalPanel.add(generalPanelLogVerbosityPanel);

    JLabel generalPanelLogVerbosityTitle = new JLabel("Log verbosity maximum");
    generalPanelLogVerbosityTitle.setToolTipText(
        "What max level of log text will be shown in the rscplus log/console");
    generalPanelLogVerbosityPanel.add(generalPanelLogVerbosityTitle);
    generalPanelLogVerbosityTitle.setAlignmentY((float) 0.9);

    Hashtable<Integer, JLabel> generalPanelLogVerbosityLabelTable =
        new Hashtable<Integer, JLabel>();
    generalPanelLogVerbosityLabelTable.put(new Integer(0), new JLabel("Error"));
    generalPanelLogVerbosityLabelTable.put(new Integer(1), new JLabel("Warning"));
    generalPanelLogVerbosityLabelTable.put(new Integer(2), new JLabel("Game"));
    generalPanelLogVerbosityLabelTable.put(new Integer(3), new JLabel("Info"));
    generalPanelLogVerbosityLabelTable.put(new Integer(4), new JLabel("Debug"));
    generalPanelLogVerbosityLabelTable.put(new Integer(5), new JLabel("Opcode"));

    generalPanelLogVerbositySlider = new JSlider();
    generalPanelLogVerbositySlider.setMajorTickSpacing(1);
    generalPanelLogVerbositySlider.setLabelTable(generalPanelLogVerbosityLabelTable);
    generalPanelLogVerbositySlider.setPaintLabels(true);
    generalPanelLogVerbositySlider.setPaintTicks(true);
    generalPanelLogVerbositySlider.setSnapToTicks(true);
    generalPanelLogVerbositySlider.setMinimum(0);
    generalPanelLogVerbositySlider.setMaximum(5);
    generalPanelLogVerbositySlider.setPreferredSize(new Dimension(200, 55));
    generalPanelLogVerbositySlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelLogVerbositySlider.setBorder(new EmptyBorder(0, 0, 5, 0));
    generalPanelLogVerbositySlider.setOrientation(SwingConstants.HORIZONTAL);
    generalPanelLogVerbosityPanel.add(generalPanelLogVerbositySlider);

    generalPanelLogTimestampsCheckbox = addCheckbox("Show timestamps in log", generalPanel);
    generalPanelLogTimestampsCheckbox.setToolTipText(
        "Displays the time text was output to the log");

    generalPanelLogLevelCheckbox = addCheckbox("Show log level in log", generalPanel);
    generalPanelLogLevelCheckbox.setToolTipText("Displays the log level of output in the log");

    generalPanelLogForceTimestampsCheckbox = addCheckbox("Force timestamps in log", generalPanel);
    generalPanelLogForceTimestampsCheckbox.setToolTipText(
        "Forces display of the time text was output to the log");

    generalPanelLogForceLevelCheckbox = addCheckbox("Force log level in log", generalPanel);
    generalPanelLogForceLevelCheckbox.setToolTipText(
        "Forces display of the log level of output in the log");

    generalPanelColoredTextCheckbox = addCheckbox("Colored console text", generalPanel);
    generalPanelColoredTextCheckbox.setToolTipText(
        "When running the client from a console, chat messages in the console will reflect the colors they are in game");

    generalPanelDebugModeCheckbox = addCheckbox("Enable debug mode", generalPanel);
    generalPanelDebugModeCheckbox.setToolTipText(
        "Shows debug overlays and enables debug text in the console");

    generalPanelExceptionHandlerCheckbox = addCheckbox("Enable exception handler", generalPanel);
    generalPanelExceptionHandlerCheckbox.setToolTipText(
        "Show's all of RSC's thrown exceptions in the log. (ADVANCED USERS)");

    generalPanelPrefersXdgOpenCheckbox =
        addCheckbox("Use xdg-open to open URLs on Linux", generalPanel);
    generalPanelPrefersXdgOpenCheckbox.setToolTipText(
        "Does nothing on Windows or Mac, may improve URL opening experience on Linux");

    /// "Gameplay settings" are settings that can be seen inside the game
    addSettingsHeader(generalPanel, "Gameplay settings");

    // Commented out b/c probably no one will ever implement this
    // generalPanelChatHistoryCheckbox = addCheckbox("Load chat history after relogging (Not
    // implemented yet)", generalPanel);
    // generalPanelChatHistoryCheckbox.setToolTipText("Make chat history persist between logins");
    // generalPanelChatHistoryCheckbox.setEnabled(false); // TODO: Remove this line when chat
    // history is implemented

    generalPanelCombatXPMenuCheckbox =
        addCheckbox("Combat style menu shown outside of combat", generalPanel);
    generalPanelCombatXPMenuCheckbox.setToolTipText(
        "Always show the combat style menu when out of combat");
    generalPanelCombatXPMenuCheckbox.setBorder(new EmptyBorder(7, 0, 10, 0));

    generalPanelCombatXPMenuHiddenCheckbox =
        addCheckbox("Combat style menu hidden when in combat", generalPanel);
    generalPanelCombatXPMenuHiddenCheckbox.setToolTipText("Hide combat style menu when in combat");

    generalPanelFatigueAlertCheckbox = addCheckbox("Fatigue alert", generalPanel);
    generalPanelFatigueAlertCheckbox.setToolTipText(
        "Displays a large notice when fatigue approaches 100%");

    generalPanelInventoryFullAlertCheckbox = addCheckbox("Inventory full alert", generalPanel);
    generalPanelInventoryFullAlertCheckbox.setToolTipText(
        "Displays a large notice when the inventory is full");

    generalPanelEnableMouseWheelScrollingCheckbox =
        addCheckbox("Enable menu list mouse wheel scrolling", generalPanel);
    generalPanelEnableMouseWheelScrollingCheckbox.setToolTipText(
        "Enables mouse wheel scrolling through menu lists");

    generalPanelKeepScrollbarPosMagicPrayerCheckbox =
        addCheckbox("Keep Magic & Prayer scrollbar position", generalPanel);
    generalPanelKeepScrollbarPosMagicPrayerCheckbox.setToolTipText(
        "Keeps the magic & prayers scrollbar position when switching between tabs");

    generalPanelRoofHidingCheckbox = addCheckbox("Roof hiding", generalPanel);
    generalPanelRoofHidingCheckbox.setToolTipText("Always hide rooftops");

    generalPanelDisableUndergroundLightingCheckbox =
        addCheckbox("Disable underground lighting flicker", generalPanel);
    generalPanelDisableUndergroundLightingCheckbox.setToolTipText(
        "Underground lighting will no longer flicker");

    generalPanelDisableMinimapRotationCheckbox =
        addCheckbox("Disable random minimap rotation", generalPanel);
    generalPanelDisableMinimapRotationCheckbox.setToolTipText(
        "The random minimap rotation when opening minimap will no longer be applied");

    generalPanelCameraZoomableCheckbox = addCheckbox("Camera zoom enhancement", generalPanel);
    generalPanelCameraZoomableCheckbox.setToolTipText(
        "Zoom the camera in and out with the mouse wheel, and no longer zooms in inside buildings");

    generalPanelCameraRotatableCheckbox = addCheckbox("Camera rotation enhancement", generalPanel);
    generalPanelCameraRotatableCheckbox.setToolTipText(
        "Rotate the camera with middle mouse click, among other things");

    generalPanelCameraMovableCheckbox = addCheckbox("Camera movement enhancement", generalPanel);
    generalPanelCameraMovableCheckbox.setToolTipText(
        "Makes the camera follow the player more closely, and allow camera movement while holding shift, and pressing arrow keys");

    generalPanelCameraMovableRelativeCheckbox =
        addCheckbox("Camera movement is relative to player", generalPanel);
    generalPanelCameraMovableRelativeCheckbox.setToolTipText(
        "Camera movement will follow the player position");

    addSettingsHeader(generalPanel, "Menu/Item patching");

    generalPanelBypassAttackCheckbox = addCheckbox("Always left click to attack", generalPanel);
    generalPanelBypassAttackCheckbox.setToolTipText(
        "Left click attack monsters regardless of level difference");
    generalPanelBypassAttackCheckbox.setBorder(new EmptyBorder(7, 0, 10, 0));

    generalPanelCommandPatchEdibleRaresCheckbox =
        addCheckbox("Disable the ability to ingest holiday items or rares", generalPanel);
    generalPanelCommandPatchEdibleRaresCheckbox.setToolTipText(
        "Applies to the Easter Egg, Pumpkin, and the Half Wine.");

    generalPanelCommandPatchQuestCheckbox =
        addCheckbox("Swap eat & use options on Quest Items", generalPanel);
    generalPanelCommandPatchQuestCheckbox.setToolTipText(
        "Applies to giant Carp, Draynor Malt Whisky, Rock cake, and Nightshade.");

    generalPanelCommandPatchDiskOfReturningCheckbox =
        addCheckbox("Remove the spin option from the Disk of Returning", generalPanel);
    generalPanelCommandPatchDiskOfReturningCheckbox.setToolTipText(
        "There is no reason to want to do this. Kept in RSC+ as a historic option.");

    JPanel generalPanelNamePatchModePanel = new JPanel();
    generalPanelNamePatchModePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelNamePatchModePanel.setMaximumSize(new Dimension(300, 60));
    generalPanelNamePatchModePanel.setLayout(
        new BoxLayout(generalPanelNamePatchModePanel, BoxLayout.X_AXIS));
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
    generalPanelNamePatchModeSlider.setBorder(new EmptyBorder(0, 0, 10, 0));
    generalPanelNamePatchModeSlider.setOrientation(SwingConstants.VERTICAL);
    generalPanelNamePatchModePanel.add(generalPanelNamePatchModeSlider);

    JPanel generalPanelNamePatchModeTextPanel = new JPanel();
    generalPanelNamePatchModeTextPanel.setPreferredSize(new Dimension(255, 80));
    generalPanelNamePatchModeTextPanel.setLayout(new BorderLayout());
    generalPanelNamePatchModeTextPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
    generalPanelNamePatchModePanel.add(generalPanelNamePatchModeTextPanel);

    JLabel generalPanelNamePatchModeTitle = new JLabel("<html><b>Item name patch mode</b></html>");
    generalPanelNamePatchModeTitle.setToolTipText(
        "Replace certain item names with improved versions");
    generalPanelNamePatchModeTextPanel.add(generalPanelNamePatchModeTitle, BorderLayout.PAGE_START);
    generalPanelNamePatchModeDesc = new JLabel("");
    generalPanelNamePatchModeTextPanel.add(generalPanelNamePatchModeDesc, BorderLayout.CENTER);

    generalPanelNamePatchModeSlider.addChangeListener(
        new ChangeListener() {

          @Override
          public void stateChanged(ChangeEvent e) {
            switch (generalPanelNamePatchModeSlider.getValue()) {
              case 3:
                generalPanelNamePatchModeDesc.setText(
                    "<html>Reworded vague stuff to be more descriptive on top of type 1 & 2 changes</html>");
                break;
              case 2:
                generalPanelNamePatchModeDesc.setText(
                    "<html>Capitalizations and fixed spellings on top of type 1 changes</html>");
                break;
              case 1:
                generalPanelNamePatchModeDesc.setText(
                    "<html>Purely practical name changes (potion dosages, unidentified herbs, unfinished potions)</html>");
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

    generalPanelPatchGenderCheckbox =
        addCheckbox(
            "Correct \"Gender\" to \"Body Type\" on the appearance screen (Requires restart)",
            generalPanel);
    generalPanelPatchGenderCheckbox.setToolTipText(
        "When selected, says \"Body Type\" instead of \"Gender\" on the character creation/appearance screen");

    generalPanelPatchWrenchMenuSpacingCheckbox =
        addCheckbox("Fix 5 pixel vertical spacing bug in wrench menu", generalPanel);
    generalPanelPatchWrenchMenuSpacingCheckbox.setToolTipText(
        "When the \"Security settings\" section was removed from the wrench menu, Jagex also deleted 5 pixels of vertical space needed to properly align the next section.");

    generalPanelPatchHbar512LastPixelCheckbox =
        addCheckbox("Fix bottom bar's last pixel at 512 width", generalPanel);
    generalPanelPatchHbar512LastPixelCheckbox.setToolTipText(
        "Even since very early versions of the client, the horizontal blue bar at the bottom has been misaligned so that 1 pixel shines through at the end");

    generalPanelUseJagexFontsCheckBox =
        addCheckbox("Override system font with Jagex fonts", generalPanel);
    generalPanelUseJagexFontsCheckBox.setToolTipText(
        "Make game fonts appear consistent by loading Jagex font files the same as prior to 2009.");

    /*
     * Overlays tab
     */

    overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));

    /// "Interface overlays" are overlays that have a constant position on
    /// the screen because they are designed to modify just the interface of RSC+
    addSettingsHeader(overlayPanel, "Interface overlays");
    overlayPanelStatusDisplayCheckbox = addCheckbox("Show HP/Prayer/Fatigue display", overlayPanel);
    overlayPanelStatusDisplayCheckbox.setToolTipText("Toggle hits/prayer/fatigue display");
    overlayPanelStatusDisplayCheckbox.setBorder(new EmptyBorder(7, 0, 10, 0));

    overlayPanelBuffsCheckbox =
        addCheckbox("Show combat (de)buffs and cooldowns display", overlayPanel);
    overlayPanelBuffsCheckbox.setToolTipText("Toggle combat (de)buffs and cooldowns display");

    overlayPanelLastMenuActionCheckbox = addCheckbox("Show last menu action display", overlayPanel);
    overlayPanelLastMenuActionCheckbox.setToolTipText("Toggle last menu action used display");

    overlayPanelMouseTooltipCheckbox =
        addCheckbox("Show mouse hover action at mouse cursor", overlayPanel);
    overlayPanelMouseTooltipCheckbox.setToolTipText(
        "Shows important actions from the text at the top left of the game near the mouse cursor");

    overlayPanelExtendedTooltipCheckbox =
        addCheckbox("Extend mouse hover action at mouse cursor", overlayPanel);
    overlayPanelExtendedTooltipCheckbox.setToolTipText(
        "Shows the text at the top left of the game near the mouse cursor");

    overlayPanelInvCountCheckbox = addCheckbox("Display inventory count", overlayPanel);
    overlayPanelInvCountCheckbox.setToolTipText("Shows the number of items in your inventory");

    overlayPanelInvCountColoursCheckbox =
        addCheckbox("Additional inventory count colours", overlayPanel);
    overlayPanelInvCountColoursCheckbox.setToolTipText(
        "Adds additional colours to the inventory count to indicate fullness levels");

    overlayPanelRemoveReportAbuseButtonHbarCheckbox =
        addCheckbox("Remove Report Abuse Button (Similar to prior to 2002-09-11)", overlayPanel);
    overlayPanelRemoveReportAbuseButtonHbarCheckbox.setToolTipText(
        "mudclient149 added the Report Abuse button. You will still be able to report players with right click menu if this option is enabled.");

    overlayPanelPositionCheckbox = addCheckbox("Display position", overlayPanel);
    overlayPanelPositionCheckbox.setToolTipText("Shows the player's global position");

    overlayPanelRetroFpsCheckbox = addCheckbox("Display FPS like early RSC", overlayPanel);
    overlayPanelRetroFpsCheckbox.setToolTipText(
        "Shows the FPS like it used to be displayed in RSC");

    overlayPanelShowCombatInfoCheckbox = addCheckbox("Show NPC HP info", overlayPanel);
    overlayPanelShowCombatInfoCheckbox.setToolTipText(
        "Shows the HP info for the NPC you're in combat with");

    overlayPanelUsePercentageCheckbox = addCheckbox("Use percentage for NPC HP info", overlayPanel);
    overlayPanelUsePercentageCheckbox.setToolTipText(
        "Uses percentage for NPC HP info instead of actual HP");

    overlayPanelLagIndicatorCheckbox = addCheckbox("Lag indicator", overlayPanel);
    overlayPanelLagIndicatorCheckbox.setToolTipText(
        "When there's a problem with your connection, rscplus will tell you in the bottom right");

    overlayPanelFoodHealingCheckbox =
        addCheckbox("Show food healing overlay (Not implemented yet)", overlayPanel);
    overlayPanelFoodHealingCheckbox.setToolTipText(
        "When hovering on food, shows the HP a consumable recovers");
    // TODO: Remove this line when food healing overlay is implemented
    overlayPanelFoodHealingCheckbox.setEnabled(false);

    overlayPanelHPRegenTimerCheckbox =
        addCheckbox("Display time until next HP regeneration (Not implemented yet)", overlayPanel);
    overlayPanelHPRegenTimerCheckbox.setToolTipText(
        "Shows the seconds until your HP will naturally regenerate");
    // TODO: Remove this line when the HP regen timer is implemented
    overlayPanelHPRegenTimerCheckbox.setEnabled(false);

    /// In-game buttons
    addSettingsHeader(overlayPanel, "In-game buttons");

    overlayPanelRscPlusButtonsCheckbox =
        addCheckbox("Display + indicators over the activated in-game buttons", overlayPanel);
    overlayPanelRscPlusButtonsCheckbox.setToolTipText("Display + indicators over in-game buttons");
    overlayPanelRscPlusButtonsCheckbox.setBorder(new EmptyBorder(7, 0, 10, 0));

    overlayPanelRscPlusButtonsFunctionalCheckbox =
        addCheckbox(
            "Enable opening the World Map window and the Settings window with in-game buttons",
            overlayPanel);
    overlayPanelRscPlusButtonsFunctionalCheckbox.setToolTipText(
        "Able to click in-game Wrench & Map buttons to activate RSC+ features");

    overlayPanelWikiLookupOnMagicBookCheckbox =
        addCheckbox("Search the RSC Wiki by first clicking on the Magic Book", overlayPanel);
    overlayPanelWikiLookupOnMagicBookCheckbox.setToolTipText(
        "Click the spell book, then click on anything else, and it will look it up on the RSC wiki.");

    overlayPanelWikiLookupOnHbarCheckbox =
        addCheckbox(
            "Search the RSC Wiki with a button in the bottom blue bar (replaces Report Abuse button at low width)",
            overlayPanel);
    overlayPanelWikiLookupOnHbarCheckbox.setToolTipText(
        "Click the button on the bottom bar, then click on anything else, and it will look it up on the RSC wiki.");

    overlayPanelToggleXPBarOnStatsButtonCheckbox =
        addCheckbox(
            "Add left and right click options to the Stats button to control the XP bar",
            overlayPanel);
    overlayPanelToggleXPBarOnStatsButtonCheckbox.setToolTipText(
        "Left click pins/unpins the XP Bar, Right click enables/disables the XP Bar");

    overlayPaneHiscoresLookupButtonCheckbox =
        addCheckbox(
            "Look up players in hiscores by left clicking the Friends button", overlayPanel);
    overlayPaneHiscoresLookupButtonCheckbox.setToolTipText(
        "Must be a hiscores that supports looking up player by username. Hiscores URL defined per-world.");

    overlayPanelToggleMotivationalQuotesCheckbox =
        addCheckbox(
            "Right click on the Friends button to display a motivational quote", overlayPanel);
    overlayPanelToggleMotivationalQuotesCheckbox.setToolTipText(
        "Motivational quotes are displayed when you need motivation.");

    /// XP Bar
    addSettingsHeader(overlayPanel, "XP bar");
    overlayPanelXPBarCheckbox = addCheckbox("Show an XP bar", overlayPanel);
    overlayPanelXPBarCheckbox.setToolTipText("Show an XP bar to the left of the wrench");
    overlayPanelXPBarCheckbox.setBorder(new EmptyBorder(7, 0, 10, 0));

    overlayPanelXPDropsCheckbox = addCheckbox("Show XP drops", overlayPanel);
    overlayPanelXPDropsCheckbox.setToolTipText(
        "Show the XP gained as an overlay each time XP is received");

    ButtonGroup XPAlignButtonGroup = new ButtonGroup();
    overlayPanelXPRightAlignFocusButton = addRadioButton("Display on the right", overlayPanel, 20);
    overlayPanelXPRightAlignFocusButton.setToolTipText(
        "The XP bar and XP drops will be shown just left of the Settings menu.");
    overlayPanelXPCenterAlignFocusButton =
        addRadioButton("Display in the center", overlayPanel, 20);
    overlayPanelXPCenterAlignFocusButton.setToolTipText(
        "The XP bar and XP drops will be shown at the top-middle of the screen.");
    XPAlignButtonGroup.add(overlayPanelXPRightAlignFocusButton);
    XPAlignButtonGroup.add(overlayPanelXPCenterAlignFocusButton);

    overlayPanelFatigueDropsCheckbox = addCheckbox("Show Fatigue drops", overlayPanel);
    overlayPanelFatigueDropsCheckbox.setToolTipText(
        "Show the fatigue gained as an overlay each time fatigue is received");

    JPanel generalPanelFatigueFigsPanel = new JPanel();
    overlayPanel.add(generalPanelFatigueFigsPanel);
    generalPanelFatigueFigsPanel.setLayout(
        new BoxLayout(generalPanelFatigueFigsPanel, BoxLayout.X_AXIS));
    generalPanelFatigueFigsPanel.setPreferredSize(new Dimension(0, 37));
    generalPanelFatigueFigsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelFatigueFigsPanel.setLayout(
        new BoxLayout(generalPanelFatigueFigsPanel, BoxLayout.X_AXIS));

    JLabel generalPanelFatigueFigsLabel = new JLabel("Fatigue figures:");
    generalPanelFatigueFigsPanel.add(generalPanelFatigueFigsLabel);
    generalPanelFatigueFigsLabel.setAlignmentY((float) 0.9);
    generalPanelFatigueFigsLabel.setToolTipText(
        "Number of significant figures past the decimal point to display on fatigue drops");

    overlayPanelFatigueFigSpinner = new JSpinner();
    generalPanelFatigueFigsPanel.add(overlayPanelFatigueFigSpinner);
    overlayPanelFatigueFigSpinner.setMaximumSize(new Dimension(40, 22));
    overlayPanelFatigueFigSpinner.setAlignmentY((float) 0.7);
    generalPanelFatigueFigsPanel.setBorder(new EmptyBorder(0, 0, 7, 0));
    overlayPanelFatigueFigSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    // Sanitize JSpinner values
    SpinnerNumberModel spinnerNumModel = new SpinnerNumberModel();
    spinnerNumModel.setMinimum(1);
    spinnerNumModel.setMaximum(7);
    spinnerNumModel.setValue(2);
    overlayPanelFatigueFigSpinner.setModel(spinnerNumModel);

    overlayPanelFatigueUnitsCheckbox = addCheckbox("Fatigue units", overlayPanel);
    overlayPanelFatigueUnitsCheckbox.setToolTipText(
        "Show the fatigue units gained additional to fatigue percentage");

    /// "In World" Overlays move with the camera, and modify objects that the are rendered in the
    // world
    addSettingsHeader(overlayPanel, "\"In World\" Overlays");
    overlayPanelHitboxCheckbox =
        addCheckbox("Show hitboxes around NPCs, players, and items", overlayPanel);
    overlayPanelHitboxCheckbox.setToolTipText(
        "Shows the clickable areas on NPCs, players, and items");
    overlayPanelHitboxCheckbox.setBorder(new EmptyBorder(7, 0, 10, 0));

    overlayPanelPlayerNamesCheckbox =
        addCheckbox("Show player names over their heads", overlayPanel);
    overlayPanelPlayerNamesCheckbox.setToolTipText(
        "Shows players' display names over their character");

    overlayPanelFriendNamesCheckbox =
        addCheckbox("Show nearby friend names over their heads", overlayPanel);
    overlayPanelFriendNamesCheckbox.setToolTipText(
        "Shows your friends' display names over their character");

    // even the animated axe has an "axe head". All NPCs have a head until proven otherwise
    overlayPanelNPCNamesCheckbox = addCheckbox("Show NPC names over their heads", overlayPanel);
    overlayPanelNPCNamesCheckbox.setToolTipText("Shows NPC names over the NPC");

    overlayPanelIDsCheckbox = addCheckbox("Extend names by showing IDs", overlayPanel);
    overlayPanelIDsCheckbox.setToolTipText(
        "Displays IDs of NPCs and Players if their name overlay is present");

    overlayPanelObjectInfoCheckbox = addCheckbox("Trace object info", overlayPanel);
    overlayPanelObjectInfoCheckbox.setToolTipText(
        "Displays object information after their name on the right click examine");

    overlayPanelItemNamesCheckbox =
        addCheckbox("Display the names of items on the ground", overlayPanel);
    overlayPanelItemNamesCheckbox.setToolTipText("Shows the names of dropped items");

    // Blocked Items
    JPanel blockedItemsPanel = new JPanel();
    overlayPanel.add(blockedItemsPanel);
    blockedItemsPanel.setLayout(new BoxLayout(blockedItemsPanel, BoxLayout.X_AXIS));
    blockedItemsPanel.setPreferredSize(new Dimension(0, 37));
    blockedItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    blockedItemsPanel.setBorder(new EmptyBorder(0, 0, 9, 0));

    JLabel blockedItemsPanelNameLabel = new JLabel("Blocked items: ");
    blockedItemsPanel.add(blockedItemsPanelNameLabel);
    blockedItemsPanelNameLabel.setAlignmentY((float) 0.9);

    blockedItemsTextField = new JTextField();
    blockedItemsPanel.add(blockedItemsTextField);
    blockedItemsTextField.setMinimumSize(new Dimension(100, 28));
    blockedItemsTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    blockedItemsTextField.setAlignmentY((float) 0.75);

    // Highlighted Items
    JPanel highlightedItemsPanel = new JPanel();
    overlayPanel.add(highlightedItemsPanel);
    highlightedItemsPanel.setLayout(new BoxLayout(highlightedItemsPanel, BoxLayout.X_AXIS));
    highlightedItemsPanel.setPreferredSize(new Dimension(0, 37));
    highlightedItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    highlightedItemsPanel.setBorder(new EmptyBorder(0, 0, 9, 0));

    JLabel highlightedItemsPanelNameLabel = new JLabel("Highlighted items: ");
    highlightedItemsPanel.add(highlightedItemsPanelNameLabel);
    highlightedItemsPanelNameLabel.setAlignmentY((float) 0.9);

    highlightedItemsTextField = new JTextField();
    highlightedItemsPanel.add(highlightedItemsTextField);
    highlightedItemsTextField.setMinimumSize(new Dimension(100, 28));
    highlightedItemsTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    highlightedItemsTextField.setAlignmentY((float) 0.75);

    /*
     * Audio tab
     */

    audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(audioPanel, "Audio settings");

    audioPanelEnableMusicCheckbox =
        addCheckbox("Enable Music (Must have music pack installed)", audioPanel);
    audioPanelEnableMusicCheckbox.setToolTipText("Enable Music (Must have music pack installed)");
    audioPanelEnableMusicCheckbox.setBorder(new EmptyBorder(7, 0, 10, 0));

    audioPanelLouderSoundEffectsCheckbox = addCheckbox("Louder sound effects", audioPanel);
    audioPanelLouderSoundEffectsCheckbox.setToolTipText(
        "Play sound effects twice at the same time so that it's louder.");

    audioPanelOverrideAudioSettingCheckbox =
        addCheckbox("Override server's remembered audio on/off setting", audioPanel);
    audioPanelOverrideAudioSettingCheckbox.setToolTipText(
        "Let RSC+ control whether or not sound effects are played (useful for watching replays)");

    ButtonGroup overrideAudioSettingGroup = new ButtonGroup();
    audioPanelOverrideAudioSettingOnButton =
        addRadioButton("Sound effects always on", audioPanel, 20);
    audioPanelOverrideAudioSettingOnButton.setToolTipText(
        "Even if the server remembers that the user's audio should be off, RSC+ will play sound effects.");
    audioPanelOverrideAudioSettingOffButton =
        addRadioButton("Sound effects always off", audioPanel, 20);
    audioPanelOverrideAudioSettingOffButton.setToolTipText(
        "Even if the server remembers that the user's audio should be on, RSC+ will NOT play sound effects.");
    overrideAudioSettingGroup.add(audioPanelOverrideAudioSettingOnButton);
    overrideAudioSettingGroup.add(audioPanelOverrideAudioSettingOffButton);

    audioPanelFixSpiderWebDummySoundCheckbox =
        addCheckbox("Fix web slicing & dummy hitting sound effect", audioPanel);
    audioPanelFixSpiderWebDummySoundCheckbox.setToolTipText(
        "The RSC server authentically tells your client to play a sound effect when slicing a web or hitting a dummy, but that sound effect doesn't exist in an unmodified client cache.");

    addSettingsHeader(audioPanel, "Toggle individual sound effects");

    JLabel audioPanelSoundEffectsToggleExplanation =
        new JLabel(
            "<html><p>"
                + "There are 37 sound effects in RS-Classic. Some are great, and some can be grating. It's up to you to decide which are which."
                + "</p></html>");
    audioPanel.add(audioPanelSoundEffectsToggleExplanation);
    audioPanelSoundEffectsToggleExplanation.setBorder(new EmptyBorder(7, 0, 7, 0));

    JPanel audioPanelToggleAllPanel = new JPanel();
    audioPanelToggleAllPanel.setLayout(new BoxLayout(audioPanelToggleAllPanel, BoxLayout.X_AXIS));
    audioPanelToggleAllPanel.setPreferredSize(new Dimension(0, 37));
    audioPanelToggleAllPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    audioPanelToggleAllPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

    addButton("Enable All Sound Effects", audioPanelToggleAllPanel, Component.LEFT_ALIGNMENT)
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                setAllSoundeffects(true);
              }
            });

    JPanel audioPanelToggleAllPanelSpacingPanel = new JPanel();
    audioPanelToggleAllPanel.add(audioPanelToggleAllPanelSpacingPanel);
    audioPanelToggleAllPanelSpacingPanel.setMinimumSize(new Dimension(6, 20));
    audioPanelToggleAllPanelSpacingPanel.setPreferredSize(new Dimension(6, 20));
    audioPanelToggleAllPanelSpacingPanel.setMaximumSize(new Dimension(6, 20));

    addButton("Disable All Sound Effects", audioPanelToggleAllPanel, Component.LEFT_ALIGNMENT)
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                setAllSoundeffects(false);
              }
            });

    audioPanel.add(audioPanelToggleAllPanel);

    JPanel advancePanel = makeSoundEffectPanel("advance");
    soundEffectAdvanceCheckbox = addCheckbox("advance", advancePanel);
    soundEffectAdvanceCheckbox.setToolTipText("Plays when advancing a level.");
    soundEffectAdvanceCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(advancePanel);

    JPanel anvilPanel = makeSoundEffectPanel("anvil");
    soundEffectAnvilCheckbox = addCheckbox("anvil", anvilPanel);
    soundEffectAnvilCheckbox.setToolTipText("Plays when hammering on an anvil.");
    soundEffectAnvilCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(anvilPanel);

    JPanel chiselPanel = makeSoundEffectPanel("chisel");
    soundEffectChiselCheckbox = addCheckbox("chisel", chiselPanel);
    soundEffectChiselCheckbox.setToolTipText("Plays when cutting a gemstone.");
    soundEffectChiselCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(chiselPanel);

    JPanel clickPanel = makeSoundEffectPanel("click");
    soundEffectClickCheckbox = addCheckbox("click", clickPanel);
    soundEffectClickCheckbox.setToolTipText("Plays when equipping or unequipping your equipment.");
    soundEffectClickCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(clickPanel);

    JPanel closedoorPanel = makeSoundEffectPanel("closedoor");
    soundEffectClosedoorCheckbox = addCheckbox("closedoor", closedoorPanel);
    soundEffectClosedoorCheckbox.setToolTipText("Plays when a door opens or closes.");
    soundEffectClosedoorCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(closedoorPanel);

    JPanel coinsPanel = makeSoundEffectPanel("coins");
    soundEffectCoinsCheckbox = addCheckbox("coins", coinsPanel);
    soundEffectCoinsCheckbox.setToolTipText("Plays when buying or selling to a shop.");
    soundEffectCoinsCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(coinsPanel);

    JPanel combat1aPanel = makeSoundEffectPanel("combat1a");
    soundEffectCombat1aCheckbox = addCheckbox("combat1a", combat1aPanel);
    soundEffectCombat1aCheckbox.setToolTipText(
        "Plays when no damage is done without a weapon wielded.");
    soundEffectCombat1aCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(combat1aPanel);

    JPanel combat1bPanel = makeSoundEffectPanel("combat1b");
    soundEffectCombat1bCheckbox = addCheckbox("combat1b", combat1bPanel);
    soundEffectCombat1bCheckbox.setToolTipText(
        "Plays when damage is done in combat without a weapon wielded.");
    soundEffectCombat1bCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(combat1bPanel);

    JPanel combat2aPanel = makeSoundEffectPanel("combat2a");
    soundEffectCombat2aCheckbox = addCheckbox("combat2a", combat2aPanel);
    soundEffectCombat2aCheckbox.setToolTipText(
        "Plays when no damage is done with a sharp weapon wielded.");
    soundEffectCombat2aCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(combat2aPanel);

    JPanel combat2bPanel = makeSoundEffectPanel("combat2b");
    soundEffectCombat2bCheckbox = addCheckbox("combat2b", combat2bPanel);
    soundEffectCombat2bCheckbox.setToolTipText(
        "Plays when damage is done with a sharp weapon wielded.");
    soundEffectCombat2bCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(combat2bPanel);

    JPanel combat3aPanel = makeSoundEffectPanel("combat3a");
    soundEffectCombat3aCheckbox = addCheckbox("combat3a", combat3aPanel);
    soundEffectCombat3aCheckbox.setToolTipText(
        "Plays when no damage is done against an undead opponent.");
    soundEffectCombat3aCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(combat3aPanel);

    JPanel combat3bPanel = makeSoundEffectPanel("combat3b");
    soundEffectCombat3bCheckbox = addCheckbox("combat3b", combat3bPanel);
    soundEffectCombat3bCheckbox.setToolTipText(
        "Plays when damage is done against an undead opponent.");
    soundEffectCombat3bCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(combat3bPanel);

    JPanel cookingPanel = makeSoundEffectPanel("cooking");
    soundEffectCookingCheckbox = addCheckbox("cooking", cookingPanel);
    soundEffectCookingCheckbox.setToolTipText("Plays when cooking food on a range or fire.");
    soundEffectCookingCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(cookingPanel);

    JPanel deathPanel = makeSoundEffectPanel("death");
    soundEffectDeathCheckbox = addCheckbox("death", deathPanel);
    soundEffectDeathCheckbox.setToolTipText("Plays when the player dies.");
    soundEffectDeathCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(deathPanel);

    JPanel dropobjectPanel = makeSoundEffectPanel("dropobject");
    soundEffectDropobjectCheckbox = addCheckbox("dropobject", dropobjectPanel);
    soundEffectDropobjectCheckbox.setToolTipText("Plays when you drop an item.");
    soundEffectDropobjectCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(dropobjectPanel);

    JPanel eatPanel = makeSoundEffectPanel("eat");
    soundEffectEatCheckbox = addCheckbox("eat", eatPanel);
    soundEffectEatCheckbox.setToolTipText("Plays when you eat food.");
    soundEffectEatCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(eatPanel);

    JPanel filljugPanel = makeSoundEffectPanel("filljug");
    soundEffectFilljugCheckbox = addCheckbox("filljug", filljugPanel);
    soundEffectFilljugCheckbox.setToolTipText("Plays when filling things with water.");
    soundEffectFilljugCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(filljugPanel);

    JPanel fishPanel = makeSoundEffectPanel("fish");
    soundEffectFishCheckbox = addCheckbox("fish", fishPanel);
    soundEffectFishCheckbox.setToolTipText("Plays when fishing.");
    soundEffectFishCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(fishPanel);

    JPanel foundgemPanel = makeSoundEffectPanel("foundgem");
    soundEffectFoundgemCheckbox = addCheckbox("foundgem", foundgemPanel);
    soundEffectFoundgemCheckbox.setToolTipText("Plays when you find a gem while fishing.");
    soundEffectFoundgemCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(foundgemPanel);

    JPanel mechanicalPanel = makeSoundEffectPanel("mechanical");
    soundEffectMechanicalCheckbox = addCheckbox("mechanical", mechanicalPanel);
    soundEffectMechanicalCheckbox.setToolTipText(
        "Plays when using a hopper, spinning wheel, making pottery.");
    soundEffectMechanicalCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(mechanicalPanel);

    JPanel minePanel = makeSoundEffectPanel("mine");
    soundEffectMineCheckbox = addCheckbox("mine", minePanel);
    soundEffectMineCheckbox.setToolTipText("Plays when mining.");
    soundEffectMineCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(minePanel);

    JPanel mixPanel = makeSoundEffectPanel("mix");
    soundEffectMixCheckbox = addCheckbox("mix", mixPanel);
    soundEffectMixCheckbox.setToolTipText(
        "Plays when mixing ingredients, particularly in Herblaw.");
    soundEffectMixCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(mixPanel);

    JPanel opendoorPanel = makeSoundEffectPanel("opendoor");
    soundEffectOpendoorCheckbox = addCheckbox("opendoor", opendoorPanel);
    soundEffectOpendoorCheckbox.setToolTipText("The sound of a door opening.");
    soundEffectOpendoorCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(opendoorPanel);

    JPanel outofammoPanel = makeSoundEffectPanel("outofammo");
    soundEffectOutofammoCheckbox = addCheckbox("outofammo", outofammoPanel);
    soundEffectOutofammoCheckbox.setToolTipText("Plays when you run out of ammo while ranging.");
    soundEffectOutofammoCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(outofammoPanel);

    JPanel potatoPanel = makeSoundEffectPanel("potato");
    soundEffectPotatoCheckbox = addCheckbox("potato", potatoPanel);
    soundEffectPotatoCheckbox.setToolTipText("Plays when harvesting crops from a field.");
    soundEffectPotatoCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(potatoPanel);

    JPanel prayeroffPanel = makeSoundEffectPanel("prayeroff");
    soundEffectPrayeroffCheckbox = addCheckbox("prayeroff", prayeroffPanel);
    soundEffectPrayeroffCheckbox.setToolTipText("Plays when disabling a prayer.");
    soundEffectPrayeroffCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    soundEffectPrayeroffCheckbox.setEnabled(
        false); // TODO: would need to either reimplement opcode 206 or go disable it in there
    // (preferred)
    audioPanel.add(prayeroffPanel);

    JPanel prayeronPanel = makeSoundEffectPanel("prayeron");
    soundEffectPrayeronCheckbox = addCheckbox("prayeron", prayeronPanel);
    soundEffectPrayeronCheckbox.setToolTipText("Plays when enabling a prayer.");
    soundEffectPrayeronCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    soundEffectPrayeronCheckbox.setEnabled(
        false); // TODO: would need to either reimplement opcode 206 or go disable it in there
    // (preferred)
    audioPanel.add(prayeronPanel);

    JPanel prospectPanel = makeSoundEffectPanel("prospect");
    soundEffectProspectCheckbox = addCheckbox("prospect", prospectPanel);
    soundEffectProspectCheckbox.setToolTipText("Plays when prospecting a mining resource.");
    soundEffectProspectCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(prospectPanel);

    JPanel rechargePanel = makeSoundEffectPanel("recharge");
    soundEffectRechargeCheckbox = addCheckbox("recharge", rechargePanel);
    soundEffectRechargeCheckbox.setToolTipText("Plays when praying at an altar.");
    soundEffectRechargeCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(rechargePanel);

    JPanel retreatPanel = makeSoundEffectPanel("retreat");
    soundEffectRetreatCheckbox = addCheckbox("retreat", retreatPanel);
    soundEffectRetreatCheckbox.setToolTipText("Plays when you or your opponent flee from combat.");
    soundEffectRetreatCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(retreatPanel);

    JPanel secretdoorPanel = makeSoundEffectPanel("secretdoor");
    soundEffectSecretdoorCheckbox = addCheckbox("secretdoor", secretdoorPanel);
    soundEffectSecretdoorCheckbox.setToolTipText(
        "Plays when passing through a secret door (e.g. in Karamja dungeon)");
    soundEffectSecretdoorCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(secretdoorPanel);

    JPanel shootPanel = makeSoundEffectPanel("shoot");
    soundEffectShootCheckbox = addCheckbox("shoot", shootPanel);
    soundEffectShootCheckbox.setToolTipText("Plays when using the ranged skill.");
    soundEffectShootCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(shootPanel);

    JPanel spellfailPanel = makeSoundEffectPanel("spellfail");
    soundEffectSpellfailCheckbox = addCheckbox("spellfail", spellfailPanel);
    soundEffectSpellfailCheckbox.setToolTipText(
        "Plays when you fail to cast a spell successfully.");
    soundEffectSpellfailCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(spellfailPanel);

    JPanel spellokPanel = makeSoundEffectPanel("spellok");
    soundEffectSpellokCheckbox = addCheckbox("spellok", spellokPanel);
    soundEffectSpellokCheckbox.setToolTipText("Plays when you successfully cast a spell.");
    soundEffectSpellokCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(spellokPanel);

    JPanel takeobjectPanel = makeSoundEffectPanel("takeobject");
    soundEffectTakeobjectCheckbox = addCheckbox("takeobject", takeobjectPanel);
    soundEffectTakeobjectCheckbox.setToolTipText("Plays when you pick up an item.");
    soundEffectTakeobjectCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(takeobjectPanel);

    JPanel underattackPanel = makeSoundEffectPanel("underattack");
    soundEffectUnderattackCheckbox = addCheckbox("underattack", underattackPanel);
    soundEffectUnderattackCheckbox.setToolTipText("Plays when you are attacked.");
    soundEffectUnderattackCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(underattackPanel);

    JPanel victoryPanel = makeSoundEffectPanel("victory");
    soundEffectVictoryCheckbox = addCheckbox("victory", victoryPanel);
    soundEffectVictoryCheckbox.setToolTipText("Plays when you have won a fight.");
    soundEffectVictoryCheckbox.setBorder(new EmptyBorder(7, 0, 6, 0));
    audioPanel.add(victoryPanel);

    /*
     * Bank tab
     */

    bankPanel.setLayout(new BoxLayout(bankPanel, BoxLayout.Y_AXIS));

    /// "Client settings" are settings related to just setting up how the client behaves
    /// Not really anything related to gameplay, just being able to set up the client
    /// the way the user wants it
    addSettingsHeader(bankPanel, "Bank settings");

    bankPanelCalculateBankValueCheckbox = addCheckbox("Show Bank Value", bankPanel);
    bankPanelCalculateBankValueCheckbox.setToolTipText(
        "Calculates the value of your bank and displays it in the bank interface");
    bankPanelCalculateBankValueCheckbox.setBorder(new EmptyBorder(0, 0, 10, 0));

    bankPanelSortFilterAugmentCheckbox = addCheckbox("Sort or Filter your Bank!!", bankPanel);
    bankPanelSortFilterAugmentCheckbox.setToolTipText(
        "Displays the RSC+ Sort and Filtering interface! Authentic!");

    bankPanelStartSearchedBankCheckbox = addCheckbox("Remember Filter/Sort", bankPanel);
    bankPanelStartSearchedBankCheckbox.setToolTipText(
        "Always start with your last filtered/sorted bank settings");

    JPanel searchBankPanel = new JPanel();
    bankPanel.add(searchBankPanel);
    searchBankPanel.setLayout(new BoxLayout(searchBankPanel, BoxLayout.X_AXIS));
    searchBankPanel.setPreferredSize(new Dimension(0, 37));
    searchBankPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    searchBankPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

    JLabel searchBankPanelLabel = new JLabel("Item Search (supports CSV): ");
    searchBankPanelLabel.setToolTipText(
        "List of phrases that occur in item names that you would like to see in your bank");
    searchBankPanel.add(searchBankPanelLabel);
    searchBankPanelLabel.setAlignmentY((float) 0.9);

    bankPanelSearchBankWordTextField = new JTextField();
    searchBankPanel.add(bankPanelSearchBankWordTextField);
    bankPanelSearchBankWordTextField.setMinimumSize(new Dimension(100, 28));
    bankPanelSearchBankWordTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    bankPanelSearchBankWordTextField.setAlignmentY((float) 0.75);

    JLabel banksearchExplanation =
        new JLabel(
            "<html><p>"
                + "<strong>Note:</strong> <em>Right Clicking</em> the magnifying glass is also a way to set the \"Item Search\""
                + "</p></html>");
    bankPanel.add(banksearchExplanation);
    banksearchExplanation.setBorder(new EmptyBorder(0, 0, 12, 0));

    addSettingsHeader(bankPanel, "Custom bank order");
    JLabel exportExplanation =
        new JLabel(
            "<html><head><style>p{font-size:10px;}</style></head><p>"
                + "You can define your own preferred bank sort order here!<br/>"
                + "This is where the User button at the bottom left of the bank is defined when \"Sort or Filter your Bank!!\" is enabled.<br/>"
                + "The sort order is applied per-username.<br/>"
                + "<br/><strong>Instructions:</strong><br/>Open your bank, click some buttons (or don't) then click <strong>Export Current Bank</strong> below.<br/>"
                + "Your bank as it is currently displayed will be exported to a file."
                + "</p><br/></html>");
    exportExplanation.setBorder(new EmptyBorder(0, 0, 0, 0));
    bankPanel.add(exportExplanation);

    JPanel exportPanel = new JPanel();
    JButton bankExportButton = new JButton("Export Current Bank");
    bankExportButton.setAlignmentY((float) 0.80);
    bankExportButton.setPreferredSize(new Dimension(200, 28));
    bankExportButton.setMinimumSize(new Dimension(200, 28));
    bankExportButton.setMaximumSize(new Dimension(200, 28));
    bankExportButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            String result = Bank.exportBank();
            Logger.Info(result);
            bankPanelExportLabel.setText(
                "<html><head><style>p{font-size:10px;padding-left:7px;}</style></head><p><strong>Status:</strong>&nbsp;"
                    + result.replace(" ", "&nbsp;") // non-breaking space prevents newline
                    + "</p></html>");
          }
        });

    bankPanel.add(exportPanel);
    exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.X_AXIS));
    exportPanel.setPreferredSize(new Dimension(0, 37));
    exportPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    exportPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

    exportPanel.add(bankExportButton);
    bankPanelExportLabel = new JLabel("");
    bankPanelExportLabel.setAlignmentY((float) 0.7);
    bankPanelExportLabel.setBorder(new EmptyBorder(0, 0, 7, 0));
    exportPanel.add(bankPanelExportLabel);

    JLabel importExplanation =
        new JLabel(
            "<html><head><style>p{font-size:10px;}</style></head><p>"
                + "<br/>Once you have that file, go to <strong>https://rsc.plus/bank-organizer</strong><br/>"
                + "From there, you can import the file. Click \"Import from RSC+ .csv file\" under the Controls section.<br/>"
                + "It's a really nice HTML bank organizer which you can use to rearrange your bank<br/>"
                + "or add new item \"Place Holders\" for when you acquire an item later.<br/>"
                + "When you're done, click the \"Save to RSC+ .csv file\" button.<br/><br/>"
                + "You can either use the <strong>Import</strong> button below,<br/>or simply drag-and-drop your downloaded file<br/>"
                + "onto the main RSC+ window while you are logged in and have the bank open."
                + "</p><br/></html>");
    importExplanation.setBorder(new EmptyBorder(2, 0, 0, 0));
    bankPanel.add(importExplanation);

    JButton bankImportButton = new JButton("Import Bank Order");
    bankImportButton.setAlignmentY((float) 0.80);
    bankImportButton.setPreferredSize(new Dimension(200, 28));
    bankImportButton.setMinimumSize(new Dimension(200, 28));
    bankImportButton.setMaximumSize(new Dimension(200, 28));
    bankImportButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            String result = Bank.importBank();
            bankPanelImportLabel.setText(
                "<html><head><style>p{font-size:10px;padding-left:7px;}</style></head><p><strong>Status:</strong>&nbsp;"
                    + result.replace(" ", "&nbsp;") // non-breaking space prevents newline
                    + "</p></html>");
          }
        });

    bankPanel.add(bankImportButton);

    JPanel importPanel = new JPanel();
    bankPanel.add(importPanel);
    importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.X_AXIS));
    importPanel.setPreferredSize(new Dimension(0, 37));
    importPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    importPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

    importPanel.add(bankImportButton);
    bankPanelImportLabel = new JLabel("");
    bankPanelImportLabel.setAlignmentY((float) 0.7);
    bankPanelImportLabel.setBorder(new EmptyBorder(0, 0, 7, 0));
    importPanel.add(bankPanelImportLabel);

    /*
     * Notifications tab
     */

    notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(notificationPanel, "Notification settings");

    notificationPanelTrayPopupCheckbox =
        addCheckbox("Enable notification tray popups", notificationPanel);
    notificationPanelTrayPopupCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
    notificationPanelTrayPopupCheckbox.setToolTipText(
        "Shows a system notification when a notification is triggered");

    ButtonGroup trayPopupButtonGroup = new ButtonGroup();
    notificationPanelTrayPopupClientFocusButton =
        addRadioButton("Only when client is not focused", notificationPanel, 20);
    notificationPanelTrayPopupAnyFocusButton =
        addRadioButton("Regardless of client focus", notificationPanel, 20);
    trayPopupButtonGroup.add(notificationPanelTrayPopupClientFocusButton);
    trayPopupButtonGroup.add(notificationPanelTrayPopupAnyFocusButton);

    notificationPanelNotifSoundsCheckbox =
        addCheckbox("Enable notification sounds", notificationPanel);
    notificationPanelNotifSoundsCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
    notificationPanelNotifSoundsCheckbox.setToolTipText(
        "Plays a sound when a notification is triggered");

    ButtonGroup notifSoundButtonGroup = new ButtonGroup();
    notificationPanelNotifSoundClientFocusButton =
        addRadioButton("Only when client is not focused", notificationPanel, 20);
    notificationPanelNotifSoundAnyFocusButton =
        addRadioButton("Regardless of client focus", notificationPanel, 20);
    notifSoundButtonGroup.add(notificationPanelNotifSoundClientFocusButton);
    notifSoundButtonGroup.add(notificationPanelNotifSoundAnyFocusButton);

    if (SystemTray.isSupported())
      notificationPanelUseSystemNotifsCheckbox =
          addCheckbox("Use system notifications if available", notificationPanel);
    else {
      notificationPanelUseSystemNotifsCheckbox =
          addCheckbox("Use system notifications if available (INCOMPATIBLE OS)", notificationPanel);
      notificationPanelUseSystemNotifsCheckbox.setEnabled(false);
    }
    notificationPanelUseSystemNotifsCheckbox.setToolTipText(
        "Uses built-in system notifications. Enable this to attempt to use your operating system's notification system instead of the built-in pop-up");

    addSettingsHeader(notificationPanel, "Notifications");

    notificationPanelPMNotifsCheckbox = addCheckbox("Enable PM notifications", notificationPanel);
    notificationPanelPMNotifsCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
    notificationPanelPMNotifsCheckbox.setToolTipText(
        "Shows a system notification when a PM is received");

    // PM notifications denylist
    JPanel pmDenylistPanel = new JPanel();
    notificationPanel.add(pmDenylistPanel);
    pmDenylistPanel.setLayout(new BoxLayout(pmDenylistPanel, BoxLayout.X_AXIS));
    pmDenylistPanel.setPreferredSize(new Dimension(0, 37));
    pmDenylistPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    pmDenylistPanel.setBorder(new EmptyBorder(0, 0, 9, 0));

    JLabel pmDenyListNameLabel = new JLabel("Disable PM notifications from: ");
    pmDenylistPanel.add(pmDenyListNameLabel);
    pmDenyListNameLabel.setAlignmentY((float) 0.9);

    notificationPanelPMDenyListTextField = new JTextField();
    pmDenylistPanel.add(notificationPanelPMDenyListTextField);
    notificationPanelPMDenyListTextField.setMinimumSize(new Dimension(100, 28));
    notificationPanelPMDenyListTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    notificationPanelPMDenyListTextField.setAlignmentY((float) 0.75);

    notificationPanelTradeNotifsCheckbox =
        addCheckbox("Enable trade notifications", notificationPanel);
    notificationPanelTradeNotifsCheckbox.setToolTipText(
        "Shows a system notification when a trade request is received");

    notificationPanelDuelNotifsCheckbox =
        addCheckbox("Enable duel notifications", notificationPanel);
    notificationPanelDuelNotifsCheckbox.setToolTipText(
        "Shows a system notification when a duel request is received");

    notificationPanelLogoutNotifsCheckbox =
        addCheckbox("Enable logout notification", notificationPanel);
    notificationPanelLogoutNotifsCheckbox.setToolTipText(
        "Shows a system notification when about to idle out");

    JPanel notificationPanelLowHPNotifsPanel = new JPanel();
    notificationPanel.add(notificationPanelLowHPNotifsPanel);
    notificationPanelLowHPNotifsPanel.setLayout(
        new BoxLayout(notificationPanelLowHPNotifsPanel, BoxLayout.X_AXIS));
    notificationPanelLowHPNotifsPanel.setPreferredSize(new Dimension(0, 37));
    notificationPanelLowHPNotifsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    notificationPanelLowHPNotifsCheckbox =
        addCheckbox("Enable low HP notification at", notificationPanelLowHPNotifsPanel);
    notificationPanelLowHPNotifsCheckbox.setToolTipText(
        "Shows a system notification when your HP drops below the specified value");

    notificationPanelLowHPNotifsSpinner = new JSpinner();
    notificationPanelLowHPNotifsSpinner.setMaximumSize(new Dimension(45, 22));
    notificationPanelLowHPNotifsSpinner.setMinimumSize(new Dimension(45, 22));
    notificationPanelLowHPNotifsSpinner.setAlignmentY((float) 0.75);
    notificationPanelLowHPNotifsPanel.add(notificationPanelLowHPNotifsSpinner);
    notificationPanelLowHPNotifsSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    JLabel notificationPanelLowHPNotifsEndLabel = new JLabel("% HP");
    notificationPanelLowHPNotifsPanel.add(notificationPanelLowHPNotifsEndLabel);
    notificationPanelLowHPNotifsEndLabel.setAlignmentY((float) 0.9);
    notificationPanelLowHPNotifsEndLabel.setBorder(new EmptyBorder(0, 2, 0, 0));

    // Sanitize JSpinner values
    SpinnerNumberModel spinnerHPNumModel = new SpinnerNumberModel();
    spinnerHPNumModel.setMinimum(1);
    spinnerHPNumModel.setMaximum(99);
    spinnerHPNumModel.setValue(25);
    notificationPanelLowHPNotifsSpinner.setModel(spinnerHPNumModel);

    JPanel notificationPanelFatigueNotifsPanel = new JPanel();
    notificationPanel.add(notificationPanelFatigueNotifsPanel);
    notificationPanelFatigueNotifsPanel.setLayout(
        new BoxLayout(notificationPanelFatigueNotifsPanel, BoxLayout.X_AXIS));
    notificationPanelFatigueNotifsPanel.setPreferredSize(new Dimension(0, 37));
    notificationPanelFatigueNotifsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    notificationPanelFatigueNotifsCheckbox =
        addCheckbox("Enable high fatigue notifications at", notificationPanelFatigueNotifsPanel);
    notificationPanelFatigueNotifsCheckbox.setToolTipText(
        "Shows a system notification when your fatigue gets past the specified value");

    notificationPanelFatigueNotifsSpinner = new JSpinner();
    notificationPanelFatigueNotifsSpinner.setMaximumSize(new Dimension(45, 22));
    notificationPanelFatigueNotifsSpinner.setMinimumSize(new Dimension(45, 22));
    notificationPanelFatigueNotifsSpinner.setAlignmentY((float) 0.75);
    notificationPanelFatigueNotifsPanel.add(notificationPanelFatigueNotifsSpinner);
    notificationPanelFatigueNotifsSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    JLabel notificationPanelFatigueNotifsEndLabel = new JLabel("% fatigue");
    notificationPanelFatigueNotifsPanel.add(notificationPanelFatigueNotifsEndLabel);
    notificationPanelFatigueNotifsEndLabel.setAlignmentY((float) 0.9);
    notificationPanelFatigueNotifsEndLabel.setBorder(new EmptyBorder(0, 2, 0, 0));

    // Sanitize JSpinner values
    SpinnerNumberModel spinnerFatigueNumModel = new SpinnerNumberModel();
    spinnerFatigueNumModel.setMinimum(1);
    spinnerFatigueNumModel.setMaximum(100);
    spinnerFatigueNumModel.setValue(98);
    notificationPanelFatigueNotifsSpinner.setModel(spinnerFatigueNumModel);

    JPanel warnHighlightedOnGroundPanel = new JPanel();
    notificationPanel.add(warnHighlightedOnGroundPanel);
    warnHighlightedOnGroundPanel.setLayout(
        new BoxLayout(warnHighlightedOnGroundPanel, BoxLayout.X_AXIS));
    warnHighlightedOnGroundPanel.setPreferredSize(new Dimension(0, 37));
    warnHighlightedOnGroundPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    notificationPanelHighlightedItemTimerCheckbox =
        addCheckbox(
            "Warn if one of your highlighted items has been on the ground for more than",
            warnHighlightedOnGroundPanel);
    notificationPanelHighlightedItemTimerCheckbox.setToolTipText(
        "Highlighted items can be configured in the Overlays tab");

    JLabel highlightedItemsSuggestionJLabel =
        new JLabel(
            "<html><p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                + "<strong>Note:</strong> Loot from kills despawns after about 2 minutes."
                + "</p></html>");
    notificationPanel.add(highlightedItemsSuggestionJLabel);
    highlightedItemsSuggestionJLabel.setBorder(new EmptyBorder(0, 0, 8, 0));

    notificationPanelHighlightedItemTimerSpinner = new JSpinner();
    notificationPanelHighlightedItemTimerSpinner.setMaximumSize(new Dimension(55, 22));
    notificationPanelHighlightedItemTimerSpinner.setMinimumSize(new Dimension(55, 22));
    notificationPanelHighlightedItemTimerSpinner.setAlignmentY((float) 0.75);
    warnHighlightedOnGroundPanel.add(notificationPanelHighlightedItemTimerSpinner);
    notificationPanelHighlightedItemTimerSpinner.putClientProperty(
        "JComponent.sizeVariant", "mini");

    JLabel notificationPanelHighlightedItemEndLabel = new JLabel("seconds");
    warnHighlightedOnGroundPanel.add(notificationPanelHighlightedItemEndLabel);
    notificationPanelHighlightedItemEndLabel.setAlignmentY((float) 0.9);
    notificationPanelHighlightedItemEndLabel.setBorder(new EmptyBorder(0, 2, 0, 0));

    // Sanitize JSpinner values
    SpinnerNumberModel highlightedItemSecondsModel = new SpinnerNumberModel();
    highlightedItemSecondsModel.setMinimum(0);
    highlightedItemSecondsModel.setMaximum(630); // 10.5 minutes max
    highlightedItemSecondsModel.setValue(100);
    notificationPanelHighlightedItemTimerSpinner.setModel(highlightedItemSecondsModel);

    // Important messages
    JPanel importantMessagesPanel = new JPanel();
    notificationPanel.add(importantMessagesPanel);
    importantMessagesPanel.setLayout(new BoxLayout(importantMessagesPanel, BoxLayout.X_AXIS));
    importantMessagesPanel.setPreferredSize(new Dimension(0, 37));
    importantMessagesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    importantMessagesPanel.setBorder(new EmptyBorder(0, 0, 9, 0));

    JLabel importantMessagesNameLabel = new JLabel("Important Messages: ");
    importantMessagesPanel.add(importantMessagesNameLabel);
    importantMessagesNameLabel.setAlignmentY((float) 0.9);

    importantMessagesTextField = new JTextField();
    importantMessagesPanel.add(importantMessagesTextField);
    importantMessagesTextField.setMinimumSize(new Dimension(100, 28));
    importantMessagesTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    importantMessagesTextField.setAlignmentY((float) 0.75);

    // Important sad messages
    JPanel importantSadMessagesPanel = new JPanel();
    notificationPanel.add(importantSadMessagesPanel);
    importantSadMessagesPanel.setLayout(new BoxLayout(importantSadMessagesPanel, BoxLayout.X_AXIS));
    importantSadMessagesPanel.setPreferredSize(new Dimension(0, 37));
    importantSadMessagesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    importantSadMessagesPanel.setBorder(new EmptyBorder(0, 0, 9, 0));

    JLabel importantSadMessagesNameLabel = new JLabel("Important Messages (sad noise): ");
    importantSadMessagesPanel.add(importantSadMessagesNameLabel);
    importantSadMessagesNameLabel.setAlignmentY((float) 0.9);

    importantSadMessagesTextField = new JTextField();
    importantSadMessagesPanel.add(importantSadMessagesTextField);
    importantSadMessagesTextField.setMinimumSize(new Dimension(100, 28));
    importantSadMessagesTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    importantSadMessagesTextField.setAlignmentY((float) 0.75);

    notificationPanelMuteImportantMessageSoundsCheckbox =
        addCheckbox("Mute the alert sound even if it's an important message", notificationPanel);
    notificationPanelMuteImportantMessageSoundsCheckbox.setToolTipText(
        "Muting for Important Messages (defined in text fields above)");

    /*
     * Streaming & Privacy tab
     */

    streamingPanel.setLayout(new BoxLayout(streamingPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(streamingPanel, "Streaming & Privacy");

    streamingPanelTwitchChatIntegrationEnabledCheckbox =
        addCheckbox("Enable Twitch chat integration", streamingPanel);
    streamingPanelTwitchChatIntegrationEnabledCheckbox.setToolTipText(
        "If this box is checked, and the 3 relevant text fields are filled out, you will connect to a chat channel on login.");
    streamingPanelTwitchChatIntegrationEnabledCheckbox.setBorder(new EmptyBorder(0, 0, 7, 0));

    streamingPanelTwitchChatCheckbox = addCheckbox("Hide incoming Twitch chat", streamingPanel);
    streamingPanelTwitchChatCheckbox.setToolTipText(
        "Don't show chat from other Twitch users, but still be able to send Twitch chat");

    JPanel streamingPanelTwitchChannelNamePanel = new JPanel();
    streamingPanel.add(streamingPanelTwitchChannelNamePanel);
    streamingPanelTwitchChannelNamePanel.setLayout(
        new BoxLayout(streamingPanelTwitchChannelNamePanel, BoxLayout.X_AXIS));
    streamingPanelTwitchChannelNamePanel.setPreferredSize(new Dimension(0, 37));
    streamingPanelTwitchChannelNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    streamingPanelTwitchChannelNamePanel.setBorder(new EmptyBorder(0, 0, 7, 0));

    JLabel streamingPanelTwitchChannelNameLabel = new JLabel("Twitch channel to chat in: ");
    streamingPanelTwitchChannelNameLabel.setToolTipText("The Twitch channel you want to chat in");
    streamingPanelTwitchChannelNamePanel.add(streamingPanelTwitchChannelNameLabel);
    streamingPanelTwitchChannelNameLabel.setAlignmentY((float) 0.9);

    streamingPanelTwitchChannelNameTextField = new JTextField();
    streamingPanelTwitchChannelNamePanel.add(streamingPanelTwitchChannelNameTextField);
    streamingPanelTwitchChannelNameTextField.setMinimumSize(new Dimension(100, 28));
    streamingPanelTwitchChannelNameTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    streamingPanelTwitchChannelNameTextField.setAlignmentY((float) 0.75);

    JPanel streamingPanelTwitchUserPanel = new JPanel();
    streamingPanel.add(streamingPanelTwitchUserPanel);
    streamingPanelTwitchUserPanel.setLayout(
        new BoxLayout(streamingPanelTwitchUserPanel, BoxLayout.X_AXIS));
    streamingPanelTwitchUserPanel.setPreferredSize(new Dimension(0, 37));
    streamingPanelTwitchUserPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    streamingPanelTwitchUserPanel.setBorder(new EmptyBorder(0, 0, 7, 0));

    JLabel streamingPanelTwitchUserLabel = new JLabel("Your Twitch username: ");
    streamingPanelTwitchUserLabel.setToolTipText("The Twitch username you log into Twitch with");
    streamingPanelTwitchUserPanel.add(streamingPanelTwitchUserLabel);
    streamingPanelTwitchUserLabel.setAlignmentY((float) 0.9);

    streamingPanelTwitchUserTextField = new JTextField();
    streamingPanelTwitchUserPanel.add(streamingPanelTwitchUserTextField);
    streamingPanelTwitchUserTextField.setMinimumSize(new Dimension(100, 28));
    streamingPanelTwitchUserTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    streamingPanelTwitchUserTextField.setAlignmentY((float) 0.75);

    JPanel streamingPanelTwitchOAuthPanel = new JPanel();
    streamingPanel.add(streamingPanelTwitchOAuthPanel);
    streamingPanelTwitchOAuthPanel.setLayout(
        new BoxLayout(streamingPanelTwitchOAuthPanel, BoxLayout.X_AXIS));
    streamingPanelTwitchOAuthPanel.setPreferredSize(new Dimension(0, 37));
    streamingPanelTwitchOAuthPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    streamingPanelTwitchOAuthPanel.setBorder(new EmptyBorder(0, 0, 7, 0));

    JLabel streamingPanelTwitchOAuthLabel =
        new JLabel("Your Twitch OAuth token (not your Stream key): ");
    streamingPanelTwitchOAuthLabel.setToolTipText("Your Twitch OAuth token (not your Stream Key)");
    streamingPanelTwitchOAuthPanel.add(streamingPanelTwitchOAuthLabel);
    streamingPanelTwitchOAuthLabel.setAlignmentY((float) 0.9);

    streamingPanelTwitchOAuthTextField = new JPasswordField();
    streamingPanelTwitchOAuthPanel.add(streamingPanelTwitchOAuthTextField);
    streamingPanelTwitchOAuthTextField.setMinimumSize(new Dimension(100, 28));
    streamingPanelTwitchOAuthTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    streamingPanelTwitchOAuthTextField.setAlignmentY((float) 0.75);

    streamingPanelIPAtLoginCheckbox =
        addCheckbox("Show IP details at login welcome screen", streamingPanel);
    streamingPanelIPAtLoginCheckbox.setToolTipText(
        "Shows the last IP you last logged in from when you log in (Disable this if you're streaming)");

    streamingPanelSaveLoginCheckbox =
        addCheckbox("Save login information between logins (Requires restart)", streamingPanel);
    streamingPanelSaveLoginCheckbox.setToolTipText(
        "Preserves login details between logins (Disable this if you're streaming)");

    streamingPanelStartLoginCheckbox = addCheckbox("Start game at login screen", streamingPanel);
    streamingPanelStartLoginCheckbox.setToolTipText(
        "Starts the game at the login screen and return to it on logout");

    JLabel spacerLabel =
        new JLabel("<html><head><style>p{font-size:10px;}</style></head><p>&nbsp;</p>");
    streamingPanel.add(spacerLabel);

    addSettingsHeader(streamingPanel, "Speedrunner Mode");
    JLabel speedrunnerModeExplanation =
        new JLabel(
            "<html><head><style>p{font-size:10px;}ul{padding-left:0px;margin-left:10px;}</style></head><p>Speedrunner mode keeps track of your precise time spent in game <br/> between the first player update packet received and either logout or<br/> upon completing any of the following goals:<br/><ul><li>Completion of Tutorial Island</li><li>Completion of Black Knight's Fortress</li><li>Entrance to the Champion's Guild</li><li>Completion of Dragon Slayer</li></ul></p><p>Speedrunner mode also overrides the following RSC+ options:<ul><li>You will always be recording a replay</li><li>You will not be able to desync the camera position from the player position (too weird)</li><li>Keyboard shortcut to trigger sleeping bag is disabled</li><li>Prayer & Magic scrollbars will reset to the top when switching between those tabs</li><li>Menu item swapping (e.g. \"Always left click to attack\") is disabled <ul style=\"padding:0px; margin: 2px 0 0 10px;\"><li style=\"padding:0px; margin:0px;\">REQUIRES RESTART IF NOT ALREADY DISABLED</li></ul></li></ul></p><p>The below box should be manually clicked before logging in to a new character.<br/> The apply button must be clicked for it to take effect.</p><br/></html>");
    speedrunnerModeExplanation.setBorder(new EmptyBorder(2, 0, 0, 0));
    streamingPanel.add(speedrunnerModeExplanation);

    streamingPanelSpeedrunnerCheckbox = addCheckbox("Activate Speedrunner Mode", streamingPanel);
    streamingPanelSpeedrunnerCheckbox.setToolTipText("Speedrunner Mode, see above explanation");

    JLabel speedrunnerHowToSTOPSPEEDRUNNINGGGGExplanation =
        new JLabel(
            "<html><head><style>p{font-size:10px; padding-bottom: 5px;}</style></head><p>When you are satisfied that your run is over, end the speedrun<br/> by sending the command <font face=\"courier\"><strong>::endrun</strong></font> or press the configurable keybind <strong>&lt;CTRL-END&gt;</strong>.</p></html>");
    streamingPanel.add(speedrunnerHowToSTOPSPEEDRUNNINGGGGExplanation);

    /* shame to write all this code and then not need it...
    JPanel streamingPanelSpeedRunnerNamePanel = new JPanel();
    streamingPanel.add(streamingPanelSpeedRunnerNamePanel);
    streamingPanelSpeedRunnerNamePanel.setLayout(
        new BoxLayout(streamingPanelSpeedRunnerNamePanel, BoxLayout.X_AXIS));
    streamingPanelSpeedRunnerNamePanel.setPreferredSize(new Dimension(0, 37));
    streamingPanelSpeedRunnerNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    streamingPanelSpeedRunnerNamePanel.setBorder(new EmptyBorder(0, 0, 9, 0));

    JLabel streamingPanelSpeedRunnerUsernameLabel = new JLabel("Speedrunner username: ");
    streamingPanelSpeedRunnerUsernameLabel.setToolTipText("Only apply speedrunner restrictions/enhancements if the username in this field matches.");
    streamingPanelSpeedRunnerNamePanel.add(streamingPanelSpeedRunnerUsernameLabel);
    streamingPanelSpeedRunnerUsernameLabel.setAlignmentY((float) 0.9);

    streamingPanelSpeedrunnerUsernameTextField = new JTextField();
    streamingPanelSpeedRunnerNamePanel.add(streamingPanelSpeedrunnerUsernameTextField);
    streamingPanelSpeedrunnerUsernameTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    streamingPanelSpeedrunnerUsernameTextField.setMinimumSize(new Dimension(100, 28));
    streamingPanelSpeedrunnerUsernameTextField.setAlignmentY((float) 0.75);
    */

    /*
     * Keybind tab
     */
    JPanel keybindContainerPanel = new JPanel(new GridBagLayout());

    JPanel keybindContainerContainerPanel = new JPanel(new GridBagLayout());
    GridBagConstraints con = new GridBagConstraints();
    con.gridy = 0;
    con.gridx = 0;
    con.fill = GridBagConstraints.HORIZONTAL;

    GridBagConstraints gbl_constraints = new GridBagConstraints();
    gbl_constraints.fill = GridBagConstraints.HORIZONTAL;
    gbl_constraints.anchor = GridBagConstraints.FIRST_LINE_START;
    gbl_constraints.weightx = 1;
    gbl_constraints.gridy = 0;
    gbl_constraints.gridwidth = 3;

    // Note: CTRL + every single letter on the keyboard is now used
    // consider using ALT instead.

    addKeybindCategory(keybindContainerPanel, "General");
    addKeybindSet(keybindContainerPanel, "Sleep", "sleep", KeyModifier.CTRL, KeyEvent.VK_SPACE);
    addKeybindSet(keybindContainerPanel, "Logout", "logout", KeyModifier.CTRL, KeyEvent.VK_L);
    addKeybindSet(
        keybindContainerPanel, "Take screenshot", "screenshot", KeyModifier.CTRL, KeyEvent.VK_S);
    addKeybindSet(
        keybindContainerPanel,
        "Show settings window",
        "show_config_window",
        KeyModifier.CTRL,
        KeyEvent.VK_O);
    addKeybindSet(
        keybindContainerPanel,
        "Show world map window",
        "show_worldmap_window",
        KeyModifier.ALT,
        KeyEvent.VK_M);
    addKeybindSet(
        keybindContainerPanel,
        "Show queue window",
        "show_queue_window",
        KeyModifier.CTRL,
        KeyEvent.VK_Q);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle combat XP menu persistence",
        "toggle_combat_xp_menu",
        KeyModifier.CTRL,
        KeyEvent.VK_C);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle XP drops",
        "toggle_xp_drops",
        KeyModifier.CTRL,
        KeyEvent.VK_OPEN_BRACKET);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle fatigue drops",
        "toggle_fatigue_drops",
        KeyModifier.CTRL,
        KeyEvent.VK_CLOSE_BRACKET);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle fatigue alert",
        "toggle_fatigue_alert",
        KeyModifier.CTRL,
        KeyEvent.VK_F);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle inventory full alert",
        "toggle_inventory_full_alert",
        KeyModifier.CTRL,
        KeyEvent.VK_V);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle bypass attack",
        "toggle_bypass_attack",
        KeyModifier.CTRL,
        KeyEvent.VK_A);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle roof hiding",
        "toggle_roof_hiding",
        KeyModifier.CTRL,
        KeyEvent.VK_R);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle color coded text",
        "toggle_colorize",
        KeyModifier.CTRL,
        KeyEvent.VK_Z);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle start with searched bank",
        "toggle_start_searched_bank",
        KeyModifier.ALT,
        KeyEvent.VK_Q);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle lag indicator",
        "toggle_indicators",
        KeyModifier.CTRL,
        KeyEvent.VK_W);
    addKeybindSet(
        keybindContainerPanel, "Reset camera zoom", "reset_zoom", KeyModifier.ALT, KeyEvent.VK_Z);
    addKeybindSet(
        keybindContainerPanel,
        "Reset camera rotation",
        "reset_rotation",
        KeyModifier.ALT,
        KeyEvent.VK_N);

    addKeybindCategory(keybindContainerPanel, "Overlays");
    addKeybindSet(
        keybindContainerPanel,
        "Toggle HP/prayer/fatigue display",
        "toggle_hpprayerfatigue_display",
        KeyModifier.CTRL,
        KeyEvent.VK_U);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle combat buffs and cooldowns display         ", // TODO: remove this spacing
        "toggle_buffs_display",
        KeyModifier.CTRL,
        KeyEvent.VK_Y);
    addKeybindSet(
        keybindContainerPanel, "Toggle XP bar", "toggle_xp_bar", KeyModifier.CTRL, KeyEvent.VK_K);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle inventory count overlay",
        "toggle_inven_count_overlay",
        KeyModifier.CTRL,
        KeyEvent.VK_E);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle additional inventory count colours",
        "toggle_inven_count_colours",
        KeyModifier.ALT,
        KeyEvent.VK_T);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle position overlay",
        "toggle_position_overlay",
        KeyModifier.ALT,
        KeyEvent.VK_P);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle retro fps overlay",
        "toggle_retro_fps_overlay",
        KeyModifier.ALT,
        KeyEvent.VK_F);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle item name overlay",
        "toggle_item_overlay",
        KeyModifier.CTRL,
        KeyEvent.VK_I);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle player name overlay",
        "toggle_player_name_overlay",
        KeyModifier.CTRL,
        KeyEvent.VK_P);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle friend name overlay",
        "toggle_friend_name_overlay",
        KeyModifier.CTRL,
        KeyEvent.VK_M);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle NPC name overlay",
        "toggle_npc_name_overlay",
        KeyModifier.CTRL,
        KeyEvent.VK_N);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle hitboxes",
        "toggle_hitboxes",
        KeyModifier.CTRL,
        KeyEvent.VK_H);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle food heal overlay",
        "toggle_food_heal_overlay",
        KeyModifier.CTRL,
        KeyEvent.VK_G);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle time until health regen",
        "toggle_health_regen_timer",
        KeyModifier.CTRL,
        KeyEvent.VK_X);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle debug mode",
        "toggle_debug",
        KeyModifier.CTRL,
        KeyEvent.VK_D);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle Wiki Hbar Button",
        "toggle_wiki_hbar_button",
        KeyModifier.ALT,
        KeyEvent.VK_W);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle report abuse button",
        "toggle_report_abuse_button",
        KeyModifier.ALT,
        KeyEvent.VK_R);

    addKeybindCategory(keybindContainerPanel, "Streaming & Privacy");
    addKeybindSet(
        keybindContainerPanel,
        "Toggle Twitch chat",
        "toggle_twitch_chat",
        KeyModifier.CTRL,
        KeyEvent.VK_T);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle IP shown at login screen",
        "toggle_ipdns",
        KeyModifier.CTRL,
        KeyEvent.VK_J);
    addKeybindSet(
        keybindContainerPanel,
        "End your current speedrun",
        "endrun",
        KeyModifier.CTRL,
        KeyEvent.VK_END);
    // TODO: Uncomment the following line if this feature no longer requires a restart
    // addKeybindSet(keybindContainerPanel, "Toggle save login information",
    // "toggle_save_login_info",
    // KeyModifier.NONE, -1);

    addKeybindCategory(
        keybindContainerPanel, "Replay (only used while a recording is played back)");
    addKeybindSet(keybindContainerPanel, "Stop", "stop", KeyModifier.CTRL, KeyEvent.VK_B);
    addKeybindSet(keybindContainerPanel, "Restart", "restart", KeyModifier.ALT, KeyEvent.VK_R);
    addKeybindSet(keybindContainerPanel, "Pause", "pause", KeyModifier.NONE, KeyEvent.VK_SPACE);
    addKeybindSet(
        keybindContainerPanel,
        "Increase playback speed",
        "ff_plus",
        KeyModifier.CTRL,
        KeyEvent.VK_RIGHT);
    addKeybindSet(
        keybindContainerPanel,
        "Decrease playback speed",
        "ff_minus",
        KeyModifier.CTRL,
        KeyEvent.VK_LEFT);
    addKeybindSet(
        keybindContainerPanel,
        "Reset playback speed",
        "ff_reset",
        KeyModifier.CTRL,
        KeyEvent.VK_DOWN);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle seek bar",
        "show_seek_bar",
        KeyModifier.CTRL,
        KeyEvent.VK_UP);
    addKeybindSet(
        keybindContainerPanel,
        "Show player controls",
        "show_player_controls",
        KeyModifier.ALT,
        KeyEvent.VK_UP);

    addKeybindCategory(keybindContainerPanel, "Miscellaneous");
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 1 at login screen",
        "world_1",
        KeyModifier.CTRL,
        KeyEvent.VK_1);
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 2 at login screen",
        "world_2",
        KeyModifier.CTRL,
        KeyEvent.VK_2);
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 3 at login screen",
        "world_3",
        KeyModifier.CTRL,
        KeyEvent.VK_3);
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 4 at login screen",
        "world_4",
        KeyModifier.CTRL,
        KeyEvent.VK_4);
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 5 at login screen",
        "world_5",
        KeyModifier.CTRL,
        KeyEvent.VK_5);
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 6 at login screen",
        "world_6",
        KeyModifier.CTRL,
        KeyEvent.VK_6);
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 7 at login screen",
        "world_7",
        KeyModifier.CTRL,
        KeyEvent.VK_7);
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 8 at login screen",
        "world_8",
        KeyModifier.CTRL,
        KeyEvent.VK_8);
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 9 at login screen",
        "world_9",
        KeyModifier.CTRL,
        KeyEvent.VK_9);
    addKeybindSet(
        keybindContainerPanel,
        "Switch to world 10 at login screen",
        "world_0",
        KeyModifier.CTRL,
        KeyEvent.VK_0);

    keybindContainerContainerPanel.add(keybindContainerPanel, gbl_constraints);
    keybindPanel.add(keybindContainerContainerPanel, con);

    /*
     *  Replay Settings tab
     */
    replayPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    replayPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    replayPanel.setLayout(new BoxLayout(replayPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(replayPanel, "Recording settings");

    replayPanelRecordAutomaticallyCheckbox =
        addCheckbox("Record your play sessions by default", replayPanel);
    replayPanelRecordAutomaticallyCheckbox.setToolTipText(
        "Record your play sessions without having to click the record button every time you log in");

    replayPanelRecordKBMouseCheckbox =
        addCheckbox("Record Keyboard and Mouse input in replay recordings", replayPanel);
    replayPanelRecordKBMouseCheckbox.setToolTipText(
        "Additionally record mouse and keyboard inputs when recording a session");

    addSettingsHeader(replayPanel, "Playback settings");

    replayPanelParseOpcodesCheckbox = addCheckbox("Use opcode parsing on playback", replayPanel);
    replayPanelParseOpcodesCheckbox.setToolTipText(
        "Uses opcode parsing for better playback & visual data of outgoing packets");

    replayPanelFastDisconnectCheckbox = addCheckbox("Fast reconnect (Hack)", replayPanel);
    replayPanelFastDisconnectCheckbox.setToolTipText(
        "When a disconnect happens in replay playback, it will reconnect as quick as it can");

    addSettingsHeader(replayPanel, "Interface modifications");

    replayPanelShowSeekBarCheckbox = addCheckbox("Show seek bar during replay", replayPanel);
    replayPanelShowSeekBarCheckbox.setToolTipText(
        "Displays an incredibly helpful seek bar that you can use to move your position in the replay");

    replayPanelShowPlayerControlsCheckbox =
        addCheckbox("Show control buttons under the seek bar", replayPanel);
    replayPanelShowPlayerControlsCheckbox.setToolTipText(
        "Buttons you can click on to increase speed, decrease speed, restart, play/pause");

    replayPanelHidePrivateMessagesCheckbox =
        addCheckbox(
            "Prevent private messages from being output to the console during replay", replayPanel);
    replayPanelHidePrivateMessagesCheckbox.setToolTipText(
        "Message types 1, 2, and 5 will not be output to the console when this is selected"); // TODO: possibly don't show in client either

    replayPanelTriggerAlertsReplayCheckbox =
        addCheckbox("Prevent system alerts from triggering during replay", replayPanel);
    replayPanelTriggerAlertsReplayCheckbox.setToolTipText(
        "Overrides the system alerts setting during replay");

    addSettingsHeader(replayPanel, "Replay Queue Window");

    JPanel replayPanelReplayFolderBasePathTextFieldPanel = new JPanel();
    replayPanel.add(replayPanelReplayFolderBasePathTextFieldPanel);
    replayPanelReplayFolderBasePathTextFieldPanel.setLayout(
        new BoxLayout(replayPanelReplayFolderBasePathTextFieldPanel, BoxLayout.X_AXIS));
    replayPanelReplayFolderBasePathTextFieldPanel.setPreferredSize(new Dimension(0, 37));
    replayPanelReplayFolderBasePathTextFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    replayPanelReplayFolderBasePathTextFieldPanel.setBorder(new EmptyBorder(0, 0, 9, 0));

    JLabel replayPanelReplayFolderBasePathTextFieldLabel = new JLabel("Replay Folder Location: ");
    replayPanelReplayFolderBasePathTextFieldLabel.setToolTipText(
        "Any string of characters you enter into this field will be removed from the Folder Path column in the Replay Queue window.");
    replayPanelReplayFolderBasePathTextFieldPanel.add(
        replayPanelReplayFolderBasePathTextFieldLabel);
    replayPanelReplayFolderBasePathTextFieldLabel.setAlignmentY((float) 0.9);

    replayPanelReplayFolderBasePathTextField = new JTextField();
    replayPanelReplayFolderBasePathTextFieldPanel.add(replayPanelReplayFolderBasePathTextField);
    replayPanelReplayFolderBasePathTextField.setMinimumSize(new Dimension(100, 28));
    replayPanelReplayFolderBasePathTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    replayPanelReplayFolderBasePathTextField.setAlignmentY((float) 0.75);

    JPanel replayPanelDateFormatTextFieldPanel = new JPanel();
    replayPanel.add(replayPanelDateFormatTextFieldPanel);
    replayPanelDateFormatTextFieldPanel.setLayout(
        new BoxLayout(replayPanelDateFormatTextFieldPanel, BoxLayout.X_AXIS));
    replayPanelDateFormatTextFieldPanel.setPreferredSize(new Dimension(0, 37));
    replayPanelDateFormatTextFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    replayPanelDateFormatTextFieldPanel.setBorder(new EmptyBorder(0, 0, 9, 0));

    JLabel replayPanelDateFormatTextFieldLabel = new JLabel("Preferred Date Format: ");
    replayPanelDateFormatTextFieldLabel.setToolTipText(
        "This is the date string pattern that you personally prefer. If you're not sure what your options are, check https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html");
    replayPanelDateFormatTextFieldPanel.add(replayPanelDateFormatTextFieldLabel);
    replayPanelDateFormatTextFieldLabel.setAlignmentY((float) 0.9);

    replayPanelDateFormatTextField = new JTextField();
    replayPanelDateFormatTextFieldPanel.add(replayPanelDateFormatTextField);
    replayPanelDateFormatTextField.setMinimumSize(new Dimension(100, 28));
    replayPanelDateFormatTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));
    replayPanelDateFormatTextField.setAlignmentY((float) 0.75);

    replayPanelShowWorldColumnCheckbox = addCheckbox("Show \"World\" Column", replayPanel);
    replayPanelShowWorldColumnCheckbox.setToolTipText(
        "Displays \"Friendly Name\" for IPs that RSC+ recognizes, and just the IP address otherwise.");
    replayPanelShowConversionSettingsCheckbox =
        addCheckbox(
            "Show RSCMinus \"Conversion Settings\" Column (Chat Stripping, etc)", replayPanel);
    replayPanelShowConversionSettingsCheckbox.setToolTipText(
        "Chat Stripping, Private Chat Stripping, Whether or not it has been converted, etc.");
    replayPanelShowUserFieldCheckbox =
        addCheckbox("Show \"User Field\" Column (1st bit used for F2P)", replayPanel);
    replayPanelShowUserFieldCheckbox.setToolTipText(
        "This int field when introduced did absolutely nothing and acts as \"Reserved Bits\" for the metadata.bin format. Users may feel free to use it for whatever they can think of.");

    /*
     * Presets tab
     */
    presetsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    presetsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    presetsPanel.setLayout(new BoxLayout(presetsPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(presetsPanel, "Presets");
    presetsPanelCustomSettingsCheckbox = addCheckbox("Custom Settings", presetsPanel);
    presetsPanelCustomSettingsCheckbox.setToolTipText(
        "Load settings from config.ini instead of using a preset");

    JPanel presetsPanelPresetSliderPanel = new JPanel();
    presetsPanelPresetSliderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    presetsPanelPresetSliderPanel.setMaximumSize(new Dimension(400, 175));
    presetsPanelPresetSliderPanel.setLayout(
        new BoxLayout(presetsPanelPresetSliderPanel, BoxLayout.X_AXIS));
    presetsPanel.add(presetsPanelPresetSliderPanel);

    // these JLabels are purposely mispelled to give it that authentic RS1 feel
    Hashtable<Integer, JLabel> presetsPanelPresetSliderLabelTable =
        new Hashtable<Integer, JLabel>();
    presetsPanelPresetSliderLabelTable.put(new Integer(0), new JLabel("All"));
    presetsPanelPresetSliderLabelTable.put(new Integer(1), new JLabel("Heavy"));
    presetsPanelPresetSliderLabelTable.put(new Integer(2), new JLabel("Recommended"));
    presetsPanelPresetSliderLabelTable.put(new Integer(3), new JLabel("Lite"));
    presetsPanelPresetSliderLabelTable.put(new Integer(4), new JLabel("Vanilla (Resizable)"));
    presetsPanelPresetSliderLabelTable.put(new Integer(5), new JLabel("Vanilla"));

    presetsPanelPresetSlider = new JSlider();
    presetsPanelPresetSlider.setMajorTickSpacing(1);
    presetsPanelPresetSlider.setLabelTable(presetsPanelPresetSliderLabelTable);
    presetsPanelPresetSlider.setPaintLabels(true);
    presetsPanelPresetSlider.setPaintTicks(true);
    presetsPanelPresetSlider.setSnapToTicks(true);
    presetsPanelPresetSlider.setMinimum(0);
    presetsPanelPresetSlider.setMaximum(5);
    presetsPanelPresetSlider.setPreferredSize(new Dimension(100, 0));
    presetsPanelPresetSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    presetsPanelPresetSlider.setBorder(new EmptyBorder(0, 0, 5, 70));
    presetsPanelPresetSlider.setOrientation(SwingConstants.VERTICAL);
    presetsPanelPresetSliderPanel.add(presetsPanelPresetSlider);

    JPanel presetsButtonPanel = new JPanel();
    presetsButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    presetsButtonPanel.setMaximumSize(new Dimension(300, 50));
    presetsButtonPanel.setBorder(BorderFactory.createEmptyBorder(7, 10, 10, 0));
    presetsButtonPanel.setLayout(new BoxLayout(presetsButtonPanel, BoxLayout.X_AXIS));

    replaceConfigButton =
        addButton("Replace Config with Preset", presetsButtonPanel, Component.LEFT_ALIGNMENT);
    replaceConfigButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            int choice =
                JOptionPane.showConfirmDialog(
                    Launcher.getConfigWindow().frame,
                    "Warning: this will delete your old settings! Are you sure you want to delete your old settings?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.CLOSED_OPTION || choice == JOptionPane.NO_OPTION) {
              return;
            }

            Settings.save(Settings.currentProfile);
          }
        });
    resetPresetsButton = addButton("Reset Presets", presetsButtonPanel, Component.RIGHT_ALIGNMENT);
    resetPresetsButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Logger.Info("Try saying that 10 times fast...");
            Settings.initSettings();
          }
        });
    presetsButtonPanel.add(Box.createHorizontalGlue());
    presetsPanel.add(presetsButtonPanel);

    presetsPanelCustomSettingsCheckbox.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            synchronizePresetOptions();
          }
        });

    // World List Tab
    worldListPanel.setLayout(new BoxLayout(worldListPanel, BoxLayout.Y_AXIS));
    worldListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    worldListPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    addSettingsHeader(worldListPanel, "World List");

    JLabel spacingLabel = new JLabel("");
    spacingLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    worldListPanel.add(spacingLabel);

    for (int i = 1; i <= Settings.WORLDS_TO_DISPLAY; i++) {
      addWorldFields(i);
    }
    addAddWorldButton();

    // Authors Tab
    JPanel logoPanel = new JPanel();

    JPanel thirdsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = 0;
    c.gridx = 0;
    c.fill = GridBagConstraints.HORIZONTAL;

    try {
      BufferedImage rscplusLogo = ImageIO.read(Launcher.getResource("/assets/icon-large.png"));
      JLabel rscplusLogoJLabel =
          new JLabel(new ImageIcon(rscplusLogo.getScaledInstance(250, 250, Image.SCALE_DEFAULT)));
      rscplusLogoJLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 40));
      logoPanel.add(rscplusLogoJLabel);
    } catch (Exception e) {
      e.printStackTrace();
    }

    thirdsPanel.add(logoPanel, c);

    JPanel rightPane = new JPanel(new GridBagLayout());
    GridBagConstraints cR = new GridBagConstraints();
    cR.fill = GridBagConstraints.VERTICAL;
    cR.anchor = GridBagConstraints.LINE_START;
    cR.weightx = 0.5;
    cR.gridy = 0;
    cR.gridwidth = 3;

    JLabel RSCPlusText =
        new JLabel(
            String.format(
                "<html><div style=\"font-size:45px; padding-bottom:10px;\"<b>RSC</b>Plus</div><div style=\"font-size:20px;\">v%8.6f </div></html>",
                Settings.VERSION_NUMBER));

    rightPane.add(RSCPlusText);

    cR.gridy = 1;

    JLabel aboutText =
        new JLabel(
            "<html><head><style>p{font-size:10px; padding-top:15px;}ul{padding-left:0px;margin-left:10px;}</style></head><p><b>RSC</b>Plus is a RuneLite-like client "
                + "based on the 234 RSC client.<br/> Learn more at https://rsc.plus.<br/><br/>"
                + "Thanks to the authors who made this software possible:<br/>"
                + "<ul><li><b>Ornox</b>, for creating the client & most of its features</li>"
                + "<li><b>Logg</b>, currently maintains RSC+, new interfaces & improvements</li>"
                + "<li><b>Brian</b>, who laid a lot of the groundwork for the user interface</li>"
                + "<li><b>Luis</b>, who found a lot of important hooks & fixed a lot of bugs</li>"
                + "<li><b>Talkarcabbage</b>, generic notifications, ui backend, & keybind overhaul</li>"
                + "<li><b>conker</b>, client scaling, font consistency, menu scrolling, & other improvements</li>"
                + "<li><b>nickzuber</b>, fixed some bugs</li>"
                + "<li><b>sammy123k</b>, added an option to center the XP progress bar</li>"
                + "<li><b>The Jagex team of 2000 to 2004</b></li></ul></p></html>");

    rightPane.add(aboutText, cR);
    c.gridx = 2;
    thirdsPanel.add(rightPane, c);

    JPanel bottomPane = new JPanel(new GridBagLayout());
    GridBagConstraints cB = new GridBagConstraints();
    cB = new GridBagConstraints();
    cB.fill = GridBagConstraints.HORIZONTAL;
    cB.anchor = GridBagConstraints.NORTH;

    cB.gridx = 0;
    cB.weightx = 0.33;
    cB.gridwidth = 1;

    JLabel licenseText =
        new JLabel(
            "        This software is licensed under GPLv3. Visit https://www.gnu.org/licenses/gpl-3.0.en.html for more information.");
    bottomPane.add(licenseText, cB);

    cB.gridx = 5;
    cB.weightx = 1;
    cB.gridwidth = 20;
    JLabel blank = new JLabel("");
    bottomPane.add(blank, cB);

    c.gridy = 10;
    c.gridx = 0;
    c.gridwidth = 10;
    thirdsPanel.add(bottomPane, c);

    authorsPanel.add(thirdsPanel);

    // Joystick Tab

    joystickPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    joystickPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    joystickPanel.setLayout(new BoxLayout(joystickPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(joystickPanel, "Explanation");

    JLabel joystickExplanation =
        new JLabel(
            "<html><head><style>p{font-size:10px;}</style></head><p>"
                + "Currently, RSC+ is compatible with only the 3DConnexion Space Navigator 3D Mouse.<br/>"
                + "It is used to enable a 5 degree of freedom camera (roll left/right is omitted).<br/><br/>"
                + "This setting does not allow you to move the player with a joystick or perform in-game actions.<br/><br/>"
                + "If you do not have a 3DConnexion Space Navigator 3D Mouse, you should not enable this setting."
                + "</p><br/></html>");
    joystickExplanation.setBorder(new EmptyBorder(7, 0, 0, 0));
    joystickPanel.add(joystickExplanation);

    addSettingsHeader(joystickPanel, "Joystick");
    joystickPanelJoystickEnabledCheckbox =
        addCheckbox(
            "Enable Joystick polling (Performance decreased if not using joystick)", joystickPanel);
    joystickPanelJoystickEnabledCheckbox.setToolTipText("Enable Joystick polling once every frame");
    joystickPanelJoystickEnabledCheckbox.setBorder(new EmptyBorder(7, 0, 7, 0));

    joystickInputJlabels.put("X Axis", new JLabel("X Axis"));
    joystickInputJlabels.put("Y Axis", new JLabel("Y Axis"));
    joystickInputJlabels.put("Z Axis", new JLabel("Z Axis"));
    joystickInputJlabels.put("X Rotation", new JLabel("X Rotate"));
    joystickInputJlabels.put("Y Rotation", new JLabel("Y Rotate"));
    joystickInputJlabels.put("Z Rotation", new JLabel("Z Rotate"));
    joystickInputJlabels.put("Button 0", new JLabel("Button 0"));
    joystickInputJlabels.put("Button 1", new JLabel("Button 1"));

    joystickInputValueJlabels.put("X Axis", new JLabel("No input"));
    joystickInputValueJlabels.put("Y Axis", new JLabel("No input"));
    joystickInputValueJlabels.put("Z Axis", new JLabel("No input"));
    joystickInputValueJlabels.put("X Rotation", new JLabel("No input"));
    joystickInputValueJlabels.put("Y Rotation", new JLabel("No input"));
    joystickInputValueJlabels.put("Z Rotation", new JLabel("No input"));
    joystickInputValueJlabels.put("Button 0", new JLabel("No input"));
    joystickInputValueJlabels.put("Button 1", new JLabel("No input"));

    joystickInputJlabels.forEach(
        (key, value) -> {
          joystickPanel.add(value);
          joystickPanel.add(joystickInputValueJlabels.get(key));
          joystickPanel.add(new JLabel("<html><br/></html>"));
        });
  }

  private void setAllSoundeffects(boolean setting) {
    soundEffectAdvanceCheckbox.setSelected(setting);
    soundEffectAnvilCheckbox.setSelected(setting);
    soundEffectChiselCheckbox.setSelected(setting);
    soundEffectClickCheckbox.setSelected(setting);
    soundEffectClosedoorCheckbox.setSelected(setting);
    soundEffectCoinsCheckbox.setSelected(setting);
    soundEffectCombat1aCheckbox.setSelected(setting);
    soundEffectCombat1bCheckbox.setSelected(setting);
    soundEffectCombat2aCheckbox.setSelected(setting);
    soundEffectCombat2bCheckbox.setSelected(setting);
    soundEffectCombat3aCheckbox.setSelected(setting);
    soundEffectCombat3bCheckbox.setSelected(setting);
    soundEffectCookingCheckbox.setSelected(setting);
    soundEffectDeathCheckbox.setSelected(setting);
    soundEffectDropobjectCheckbox.setSelected(setting);
    soundEffectEatCheckbox.setSelected(setting);
    soundEffectFilljugCheckbox.setSelected(setting);
    soundEffectFishCheckbox.setSelected(setting);
    soundEffectFoundgemCheckbox.setSelected(setting);
    soundEffectMechanicalCheckbox.setSelected(setting);
    soundEffectMineCheckbox.setSelected(setting);
    soundEffectMixCheckbox.setSelected(setting);
    soundEffectOpendoorCheckbox.setSelected(setting);
    soundEffectOutofammoCheckbox.setSelected(setting);
    soundEffectPotatoCheckbox.setSelected(setting);
    // soundEffectPrayeroffCheckbox.setSelected(setting); // TODO: unimplemented
    // soundEffectPrayeronCheckbox.setSelected(setting); // TODO: unimplemented
    soundEffectProspectCheckbox.setSelected(setting);
    soundEffectRechargeCheckbox.setSelected(setting);
    soundEffectRetreatCheckbox.setSelected(setting);
    soundEffectSecretdoorCheckbox.setSelected(setting);
    soundEffectShootCheckbox.setSelected(setting);
    soundEffectSpellfailCheckbox.setSelected(setting);
    soundEffectSpellokCheckbox.setSelected(setting);
    soundEffectTakeobjectCheckbox.setSelected(setting);
    soundEffectUnderattackCheckbox.setSelected(setting);
    soundEffectVictoryCheckbox.setSelected(setting);
  }

  private JPanel makeSoundEffectPanel(String soundEffectName) {
    JPanel soundEffectPanel = new JPanel();
    soundEffectPanel.setLayout(new BoxLayout(soundEffectPanel, BoxLayout.X_AXIS));
    soundEffectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    formatPlayButton("▶");
    QueueWindow.button.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            boolean disabledBefore = SoundEffects.sounds_disabled;
            if (disabledBefore) {
              SoundEffects.sounds_disabled = false;
            }
            SoundEffects.playSound(soundEffectName);
            if (disabledBefore) {
              SoundEffects.sounds_disabled = true;
            }
          }
        });
    soundEffectPanel.add(QueueWindow.button);

    return soundEffectPanel;
  }

  private void formatPlayButton(String text) {
    QueueWindow.button = new JButton(text);
    QueueWindow.button.setFont(QueueWindow.controlsFont);
    QueueWindow.button.setMargin(new Insets(-2, -7, -2, -7));
  }

  /**
   * Adds a new keybind to the GUI and settings and registers it to be checked when keypresses are
   * sent to the applet.
   *
   * @param panel Panel to add the keybind label and button to
   * @param labelText Text describing the keybind's function as shown to the user on the config
   *     window.
   * @param commandID Unique String matching an entry in the processKeybindCommand switch statement.
   * @param defaultModifier Default modifier value. This can be one of the enum values of
   *     KeybindSet.KeyModifier, eg KeyModifier.CTRL
   * @param defaultKeyValue Default key value. This should match up with a KeyEvent.VK_ value. Set
   *     to -1 to set the default as NONE
   */
  private void addKeybindSet(
      JPanel panel,
      String labelText,
      String commandID,
      KeyModifier defaultModifier,
      int defaultKeyValue) {
    addKeybindLabel(panel, labelText);
    String buttonText = defaultModifier.toString() + " + " + KeyEvent.getKeyText(defaultKeyValue);
    if (defaultKeyValue == -1) buttonText = "NONE";
    JButton b = addKeybindButton(panel, buttonText);
    KeybindSet kbs = new KeybindSet(b, commandID, defaultModifier, defaultKeyValue);
    KeyboardHandler.keybindSetList.add(kbs);
    setKeybindButtonText(
        kbs); // Set the text of the keybind button now that it has been initialized properly
    b.addActionListener(this.clickListener);
    b.addKeyListener(this.rebindListener);
    b.addFocusListener(focusListener);
    b.setFocusable(false);

    // Default KeybindSet
    KeyboardHandler.defaultKeybindSetList.put(
        commandID, new KeybindSet(null, commandID, defaultModifier, defaultKeyValue));
  }

  /**
   * Tracks the number of keybind labels added to the keybind panel. Used to determine the gbc.gridy
   * and panel preferred height.
   */
  private int keybindLabelGridYCounter = 0;

  /**
   * Adds a new label to the keybinds list. This should be used in conjunction with adding a button
   * in a 1:1 ratio. The new label will be added below the existing ones.
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
   * Tracks the number of keybind buttons added to the keybind panel. Used to determine the
   * gbc.gridy.
   */
  private int keybindButtonGridYCounter = 0;

  /**
   * Adds a new button to the keybinds list. This should be used in conjunction with adding a label
   * in a 1:1 ratio. The new button will be added below the existing ones.
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
    if (keybindLabelGridYCounter == 0) gbc.insets = new Insets(0, 0, 0, 0);
    else gbc.insets = new Insets(7, 0, 0, 0);
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
  private void addSettingsHeader(JPanel panel, String categoryName) {
    addSettingsHeaderLabel(panel, "<html><b>" + categoryName + "</b></html>");
    addSettingsHeaderSeparator(panel);
  }

  /**
   * Adds a new horizontal separator to the notifications list.
   *
   * @param panel Panel to add the separator to.
   */
  private void addSettingsHeaderSeparator(JPanel panel) {
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
  private JLabel addSettingsHeaderLabel(JPanel panel, String categoryName) {
    JLabel jlbl = new JLabel(categoryName);
    panel.add(jlbl);
    return jlbl;
  }

  /**
   * Adds a preconfigured JCheckbox to the specified container, setting its alignment constraint to
   * left and adding an empty padding border.
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
   * Adds a preconfigured JButton to the specified container using the specified alignment
   * constraint. Does not modify the button's border.
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
   * Adds a preconfigured radio button to the specified container. Does not currently assign the
   * radio button to a group.
   *
   * @param text The text of the radio button
   * @param container The container to add the button to
   * @param leftIndent The amount of padding to add to the left of the radio button as an empty
   *     border argument.
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

  /** Synchronizes all relevant values in the gui's elements to match those in Settings.java */
  public void synchronizeGuiValues() {

    // Presets tab (has to go first to properly synchronizeGui)
    presetsPanelCustomSettingsCheckbox.setSelected(Settings.currentProfile.equals("custom"));
    synchronizePresetOptions();

    if (!Settings.currentProfile.equals("custom")) {
      sliderValue = Settings.presetTable.indexOf(Settings.currentProfile);
    }
    if (sliderValue < 0 || sliderValue > Settings.presetTable.size()) {
      sliderValue = Settings.presetTable.indexOf("default");
    }
    presetsPanelPresetSlider.setValue(sliderValue);

    // General tab
    generalPanelClientSizeCheckbox.setSelected(
        Settings.CUSTOM_CLIENT_SIZE.get(Settings.currentProfile));
    generalPanelClientSizeXSpinner.setValue(
        Settings.CUSTOM_CLIENT_SIZE_X.get(Settings.currentProfile));
    generalPanelClientSizeYSpinner.setValue(
        Settings.CUSTOM_CLIENT_SIZE_Y.get(Settings.currentProfile));
    generalPanelCheckUpdates.setSelected(Settings.CHECK_UPDATES.get(Settings.currentProfile));
    generalPanelAccountSecurityCheckbox.setSelected(
        Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile));
    generalPanelConfirmCancelRecoveryChangeCheckbox.setSelected(
        Settings.CONFIRM_CANCEL_RECOVERY_CHANGE.get(Settings.currentProfile));
    generalPanelShowSecurityTipsAtLoginCheckbox.setSelected(
        Settings.SHOW_SECURITY_TIP_DAY.get(Settings.currentProfile));
    generalPanelWelcomeEnabled.setSelected(
        Settings.REMIND_HOW_TO_OPEN_SETTINGS.get(Settings.currentProfile));
    // generalPanelChatHistoryCheckbox.setSelected(Settings.LOAD_CHAT_HISTORY.get(Settings.currentProfile)); // TODO: Implement this feature
    generalPanelCombatXPMenuCheckbox.setSelected(
        Settings.COMBAT_MENU_SHOWN.get(Settings.currentProfile));
    generalPanelCombatXPMenuHiddenCheckbox.setSelected(
        Settings.COMBAT_MENU_HIDDEN.get(Settings.currentProfile));
    generalPanelFatigueAlertCheckbox.setSelected(
        Settings.FATIGUE_ALERT.get(Settings.currentProfile));
    generalPanelInventoryFullAlertCheckbox.setSelected(
        Settings.INVENTORY_FULL_ALERT.get(Settings.currentProfile));
    generalPanelNamePatchModeSlider.setValue(Settings.NAME_PATCH_TYPE.get(Settings.currentProfile));
    generalPanelLogVerbositySlider.setValue(Settings.LOG_VERBOSITY.get(Settings.currentProfile));
    generalPanelCommandPatchQuestCheckbox.setSelected(
        Settings.COMMAND_PATCH_QUEST.get(Settings.currentProfile));
    generalPanelCommandPatchEdibleRaresCheckbox.setSelected(
        Settings.COMMAND_PATCH_EDIBLE_RARES.get(Settings.currentProfile));
    generalPanelCommandPatchDiskOfReturningCheckbox.setSelected(
        Settings.COMMAND_PATCH_DISK.get(Settings.currentProfile));
    generalPanelBypassAttackCheckbox.setSelected(
        Settings.ATTACK_ALWAYS_LEFT_CLICK.get(Settings.currentProfile));
    generalPanelEnableMouseWheelScrollingCheckbox.setSelected(
        Settings.ENABLE_MOUSEWHEEL_SCROLLING.get(Settings.currentProfile));
    generalPanelKeepScrollbarPosMagicPrayerCheckbox.setSelected(
        Settings.KEEP_SCROLLBAR_POS_MAGIC_PRAYER.get(Settings.currentProfile));
    generalPanelRoofHidingCheckbox.setSelected(Settings.HIDE_ROOFS.get(Settings.currentProfile));
    generalPanelDisableUndergroundLightingCheckbox.setSelected(
        Settings.DISABLE_UNDERGROUND_LIGHTING.get(Settings.currentProfile));
    generalPanelDisableMinimapRotationCheckbox.setSelected(
        Settings.DISABLE_MINIMAP_ROTATION.get(Settings.currentProfile));
    generalPanelCameraZoomableCheckbox.setSelected(
        Settings.CAMERA_ZOOMABLE.get(Settings.currentProfile));
    generalPanelCameraRotatableCheckbox.setSelected(
        Settings.CAMERA_ROTATABLE.get(Settings.currentProfile));
    generalPanelCameraMovableCheckbox.setSelected(
        Settings.CAMERA_MOVABLE.get(Settings.currentProfile));
    generalPanelCameraMovableRelativeCheckbox.setSelected(
        Settings.CAMERA_MOVABLE_RELATIVE.get(Settings.currentProfile));
    generalPanelColoredTextCheckbox.setSelected(
        Settings.COLORIZE_CONSOLE_TEXT.get(Settings.currentProfile));
    generalPanelLogLevelCheckbox.setSelected(Settings.LOG_SHOW_LEVEL.get(Settings.currentProfile));
    generalPanelLogTimestampsCheckbox.setSelected(
        Settings.LOG_SHOW_TIMESTAMPS.get(Settings.currentProfile));
    generalPanelLogForceLevelCheckbox.setSelected(
        Settings.LOG_FORCE_LEVEL.get(Settings.currentProfile));
    generalPanelLogForceTimestampsCheckbox.setSelected(
        Settings.LOG_FORCE_TIMESTAMPS.get(Settings.currentProfile));
    generalPanelFoVSlider.setValue(Settings.FOV.get(Settings.currentProfile));
    generalPanelLimitFPSCheckbox.setSelected(
        Settings.FPS_LIMIT_ENABLED.get(Settings.currentProfile));
    generalPanelLimitFPSSpinner.setValue(Settings.FPS_LIMIT.get(Settings.currentProfile));
    generalPanelAutoScreenshotCheckbox.setSelected(
        Settings.AUTO_SCREENSHOT.get(Settings.currentProfile));
    generalPanelRS2HDSkyCheckbox.setSelected(Settings.RS2HD_SKY.get(Settings.currentProfile));
    generalPanelCustomSkyboxOverworldCheckbox.setSelected(
        Settings.CUSTOM_SKYBOX_OVERWORLD_ENABLED.get(Settings.currentProfile));
    generalPanelCustomSkyboxUndergroundCheckbox.setSelected(
        Settings.CUSTOM_SKYBOX_UNDERGROUND_ENABLED.get(Settings.currentProfile));
    overworldSkyColour =
        Util.intToColor(Settings.CUSTOM_SKYBOX_OVERWORLD_COLOUR.get(Settings.currentProfile));
    undergroundSkyColour =
        Util.intToColor(Settings.CUSTOM_SKYBOX_UNDERGROUND_COLOUR.get(Settings.currentProfile));
    generalPanelSkyOverworldColourColourPanel.setBackground(overworldSkyColour);
    generalPanelSkyUndergroundColourColourPanel.setBackground(undergroundSkyColour);
    generalPanelCustomCursorCheckbox.setSelected(
        Settings.SOFTWARE_CURSOR.get(Settings.currentProfile));
    generalPanelDisableRandomChatColourCheckbox.setSelected(
        Settings.DISABLE_RANDOM_CHAT_COLOUR.get(Settings.currentProfile));
    generalPanelViewDistanceSlider.setValue(Settings.VIEW_DISTANCE.get(Settings.currentProfile));
    generalPanelPatchGenderCheckbox.setSelected(Settings.PATCH_GENDER.get(Settings.currentProfile));
    generalPanelPatchHbar512LastPixelCheckbox.setSelected(
        Settings.PATCH_HBAR_512_LAST_PIXEL.get(Settings.currentProfile));
    generalPanelUseJagexFontsCheckBox.setSelected(
        Settings.USE_JAGEX_FONTS.get(Settings.currentProfile));
    generalPanelPatchWrenchMenuSpacingCheckbox.setSelected(
        Settings.PATCH_WRENCH_MENU_SPACING.get(Settings.currentProfile));
    generalPanelPrefersXdgOpenCheckbox.setSelected(
        Settings.PREFERS_XDG_OPEN.get(Settings.currentProfile));

    // Sets the text associated with the name patch slider.
    switch (generalPanelNamePatchModeSlider.getValue()) {
      case 3:
        generalPanelNamePatchModeDesc.setText(
            "<html>Reworded vague stuff to be more descriptive on top of type 1 & 2 changes</html>");
        break;
      case 2:
        generalPanelNamePatchModeDesc.setText(
            "<html>Capitalizations and fixed spellings on top of type 1 changes</html>");
        break;
      case 1:
        generalPanelNamePatchModeDesc.setText(
            "<html>Purely practical name changes (potion dosages, unidentified herbs, unfinished potions)</html>");
        break;
      case 0:
        generalPanelNamePatchModeDesc.setText("<html>No item name patching</html>");
        break;
      default:
        Logger.Error("Invalid name patch mode value");
        break;
    }

    // Overlays tab
    overlayPanelStatusDisplayCheckbox.setSelected(
        Settings.SHOW_HP_PRAYER_FATIGUE_OVERLAY.get(Settings.currentProfile));
    overlayPanelBuffsCheckbox.setSelected(Settings.SHOW_BUFFS.get(Settings.currentProfile));
    overlayPanelLastMenuActionCheckbox.setSelected(
        Settings.SHOW_LAST_MENU_ACTION.get(Settings.currentProfile));
    overlayPanelMouseTooltipCheckbox.setSelected(
        Settings.SHOW_MOUSE_TOOLTIP.get(Settings.currentProfile));
    overlayPanelExtendedTooltipCheckbox.setSelected(
        Settings.SHOW_EXTENDED_TOOLTIP.get(Settings.currentProfile));
    overlayPanelInvCountCheckbox.setSelected(Settings.SHOW_INVCOUNT.get(Settings.currentProfile));
    overlayPanelInvCountColoursCheckbox.setSelected(
        Settings.SHOW_INVCOUNT_COLOURS.get(Settings.currentProfile));
    overlayPanelRscPlusButtonsCheckbox.setSelected(
        Settings.SHOW_RSCPLUS_BUTTONS.get(Settings.currentProfile));
    overlayPanelRscPlusButtonsFunctionalCheckbox.setSelected(
        Settings.RSCPLUS_BUTTONS_FUNCTIONAL.get(Settings.currentProfile)
            || Settings.SHOW_RSCPLUS_BUTTONS.get(Settings.currentProfile));
    overlayPanelWikiLookupOnMagicBookCheckbox.setSelected(
        Settings.WIKI_LOOKUP_ON_MAGIC_BOOK.get(Settings.currentProfile));
    overlayPanelWikiLookupOnHbarCheckbox.setSelected(
        Settings.WIKI_LOOKUP_ON_HBAR.get(Settings.currentProfile));
    overlayPanelToggleXPBarOnStatsButtonCheckbox.setSelected(
        Settings.TOGGLE_XP_BAR_ON_STATS_BUTTON.get(Settings.currentProfile));
    overlayPaneHiscoresLookupButtonCheckbox.setSelected(
        Settings.HISCORES_LOOKUP_BUTTON.get(Settings.currentProfile));
    overlayPanelToggleMotivationalQuotesCheckbox.setSelected(
        Settings.MOTIVATIONAL_QUOTES_BUTTON.get(Settings.currentProfile));
    overlayPanelRemoveReportAbuseButtonHbarCheckbox.setSelected(
        Settings.REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(Settings.currentProfile));
    overlayPanelPositionCheckbox.setSelected(
        Settings.SHOW_PLAYER_POSITION.get(Settings.currentProfile));
    overlayPanelRetroFpsCheckbox.setSelected(Settings.SHOW_RETRO_FPS.get(Settings.currentProfile));
    overlayPanelItemNamesCheckbox.setSelected(
        Settings.SHOW_ITEM_GROUND_OVERLAY.get(Settings.currentProfile));
    overlayPanelPlayerNamesCheckbox.setSelected(
        Settings.SHOW_PLAYER_NAME_OVERLAY.get(Settings.currentProfile));
    overlayPanelFriendNamesCheckbox.setSelected(
        Settings.SHOW_FRIEND_NAME_OVERLAY.get(Settings.currentProfile));
    overlayPanelNPCNamesCheckbox.setSelected(
        Settings.SHOW_NPC_NAME_OVERLAY.get(Settings.currentProfile));
    overlayPanelIDsCheckbox.setSelected(Settings.EXTEND_IDS_OVERLAY.get(Settings.currentProfile));
    overlayPanelObjectInfoCheckbox.setSelected(
        Settings.TRACE_OBJECT_INFO.get(Settings.currentProfile));
    overlayPanelHitboxCheckbox.setSelected(Settings.SHOW_HITBOX.get(Settings.currentProfile));
    overlayPanelShowCombatInfoCheckbox.setSelected(
        Settings.SHOW_COMBAT_INFO.get(Settings.currentProfile));
    overlayPanelUsePercentageCheckbox.setSelected(
        Settings.NPC_HEALTH_SHOW_PERCENTAGE.get(Settings.currentProfile));
    overlayPanelXPBarCheckbox.setSelected(Settings.SHOW_XP_BAR.get(Settings.currentProfile));
    overlayPanelXPDropsCheckbox.setSelected(Settings.SHOW_XPDROPS.get(Settings.currentProfile));
    overlayPanelXPCenterAlignFocusButton.setSelected(
        Settings.CENTER_XPDROPS.get(Settings.currentProfile));
    overlayPanelXPRightAlignFocusButton.setSelected(
        !Settings.CENTER_XPDROPS.get(Settings.currentProfile));
    overlayPanelFatigueDropsCheckbox.setSelected(
        Settings.SHOW_FATIGUEDROPS.get(Settings.currentProfile));
    overlayPanelFatigueFigSpinner.setValue(
        new Integer(Settings.FATIGUE_FIGURES.get(Settings.currentProfile)));
    overlayPanelFatigueUnitsCheckbox.setSelected(
        Settings.SHOW_FATIGUEUNITS.get(Settings.currentProfile));
    overlayPanelLagIndicatorCheckbox.setSelected(
        Settings.LAG_INDICATOR.get(Settings.currentProfile));
    overlayPanelFoodHealingCheckbox.setSelected(
        Settings.SHOW_FOOD_HEAL_OVERLAY.get(
            Settings.currentProfile)); // TODO: Implement this feature
    overlayPanelHPRegenTimerCheckbox.setSelected(
        Settings.SHOW_TIME_UNTIL_HP_REGEN.get(
            Settings.currentProfile)); // TODO: Implement this feature
    generalPanelDebugModeCheckbox.setSelected(Settings.DEBUG.get(Settings.currentProfile));
    generalPanelExceptionHandlerCheckbox.setSelected(
        Settings.EXCEPTION_HANDLER.get(Settings.currentProfile));
    highlightedItemsTextField.setText(
        Util.joinAsString(",", Settings.HIGHLIGHTED_ITEMS.get("custom")));
    blockedItemsTextField.setText(Util.joinAsString(",", Settings.BLOCKED_ITEMS.get("custom")));

    // Audio tab
    audioPanelEnableMusicCheckbox.setSelected(Settings.CUSTOM_MUSIC.get(Settings.currentProfile));
    audioPanelLouderSoundEffectsCheckbox.setSelected(
        Settings.LOUDER_SOUND_EFFECTS.get(Settings.currentProfile));
    audioPanelOverrideAudioSettingCheckbox.setSelected(
        Settings.OVERRIDE_AUDIO_SETTING.get(Settings.currentProfile));
    audioPanelOverrideAudioSettingOnButton.setSelected(
        Settings.OVERRIDE_AUDIO_SETTING_SETTING_ON.get(Settings.currentProfile));
    audioPanelOverrideAudioSettingOffButton.setSelected(
        !Settings.OVERRIDE_AUDIO_SETTING_SETTING_ON.get(Settings.currentProfile));
    audioPanelFixSpiderWebDummySoundCheckbox.setSelected(
        Settings.SOUND_EFFECT_COMBAT1.get(Settings.currentProfile));
    soundEffectAdvanceCheckbox.setSelected(
        Settings.SOUND_EFFECT_ADVANCE.get(Settings.currentProfile));
    soundEffectAnvilCheckbox.setSelected(Settings.SOUND_EFFECT_ANVIL.get(Settings.currentProfile));
    soundEffectChiselCheckbox.setSelected(
        Settings.SOUND_EFFECT_CHISEL.get(Settings.currentProfile));
    soundEffectClickCheckbox.setSelected(Settings.SOUND_EFFECT_CLICK.get(Settings.currentProfile));
    soundEffectClosedoorCheckbox.setSelected(
        Settings.SOUND_EFFECT_CLOSEDOOR.get(Settings.currentProfile));
    soundEffectCoinsCheckbox.setSelected(Settings.SOUND_EFFECT_COINS.get(Settings.currentProfile));
    soundEffectCombat1aCheckbox.setSelected(
        Settings.SOUND_EFFECT_COMBAT1A.get(Settings.currentProfile));
    soundEffectCombat1bCheckbox.setSelected(
        Settings.SOUND_EFFECT_COMBAT1B.get(Settings.currentProfile));
    soundEffectCombat2aCheckbox.setSelected(
        Settings.SOUND_EFFECT_COMBAT2A.get(Settings.currentProfile));
    soundEffectCombat2bCheckbox.setSelected(
        Settings.SOUND_EFFECT_COMBAT2B.get(Settings.currentProfile));
    soundEffectCombat3aCheckbox.setSelected(
        Settings.SOUND_EFFECT_COMBAT3A.get(Settings.currentProfile));
    soundEffectCombat3bCheckbox.setSelected(
        Settings.SOUND_EFFECT_COMBAT3B.get(Settings.currentProfile));
    soundEffectCookingCheckbox.setSelected(
        Settings.SOUND_EFFECT_COOKING.get(Settings.currentProfile));
    soundEffectDeathCheckbox.setSelected(Settings.SOUND_EFFECT_DEATH.get(Settings.currentProfile));
    soundEffectDropobjectCheckbox.setSelected(
        Settings.SOUND_EFFECT_DROPOBJECT.get(Settings.currentProfile));
    soundEffectEatCheckbox.setSelected(Settings.SOUND_EFFECT_EAT.get(Settings.currentProfile));
    soundEffectFilljugCheckbox.setSelected(
        Settings.SOUND_EFFECT_FILLJUG.get(Settings.currentProfile));
    soundEffectFishCheckbox.setSelected(Settings.SOUND_EFFECT_FISH.get(Settings.currentProfile));
    soundEffectFoundgemCheckbox.setSelected(
        Settings.SOUND_EFFECT_FOUNDGEM.get(Settings.currentProfile));
    soundEffectMechanicalCheckbox.setSelected(
        Settings.SOUND_EFFECT_MECHANICAL.get(Settings.currentProfile));
    soundEffectMineCheckbox.setSelected(Settings.SOUND_EFFECT_MINE.get(Settings.currentProfile));
    soundEffectMixCheckbox.setSelected(Settings.SOUND_EFFECT_MIX.get(Settings.currentProfile));
    soundEffectOpendoorCheckbox.setSelected(
        Settings.SOUND_EFFECT_OPENDOOR.get(Settings.currentProfile));
    soundEffectOutofammoCheckbox.setSelected(
        Settings.SOUND_EFFECT_OUTOFAMMO.get(Settings.currentProfile));
    soundEffectPotatoCheckbox.setSelected(
        Settings.SOUND_EFFECT_POTATO.get(Settings.currentProfile));
    soundEffectPrayeroffCheckbox.setSelected(
        Settings.SOUND_EFFECT_PRAYEROFF.get(Settings.currentProfile));
    soundEffectPrayeronCheckbox.setSelected(
        Settings.SOUND_EFFECT_PRAYERON.get(Settings.currentProfile));
    soundEffectProspectCheckbox.setSelected(
        Settings.SOUND_EFFECT_PROSPECT.get(Settings.currentProfile));
    soundEffectRechargeCheckbox.setSelected(
        Settings.SOUND_EFFECT_RECHARGE.get(Settings.currentProfile));
    soundEffectRetreatCheckbox.setSelected(
        Settings.SOUND_EFFECT_RETREAT.get(Settings.currentProfile));
    soundEffectSecretdoorCheckbox.setSelected(
        Settings.SOUND_EFFECT_SECRETDOOR.get(Settings.currentProfile));
    soundEffectShootCheckbox.setSelected(Settings.SOUND_EFFECT_SHOOT.get(Settings.currentProfile));
    soundEffectSpellfailCheckbox.setSelected(
        Settings.SOUND_EFFECT_SPELLFAIL.get(Settings.currentProfile));
    soundEffectSpellokCheckbox.setSelected(
        Settings.SOUND_EFFECT_SPELLOK.get(Settings.currentProfile));
    soundEffectTakeobjectCheckbox.setSelected(
        Settings.SOUND_EFFECT_TAKEOBJECT.get(Settings.currentProfile));
    soundEffectUnderattackCheckbox.setSelected(
        Settings.SOUND_EFFECT_UNDERATTACK.get(Settings.currentProfile));
    soundEffectVictoryCheckbox.setSelected(
        Settings.SOUND_EFFECT_VICTORY.get(Settings.currentProfile));

    // Bank tab
    bankPanelCalculateBankValueCheckbox.setSelected(
        Settings.SHOW_BANK_VALUE.get(Settings.currentProfile));
    bankPanelSortFilterAugmentCheckbox.setSelected(
        Settings.SORT_FILTER_BANK.get(Settings.currentProfile));
    bankPanelStartSearchedBankCheckbox.setSelected(
        Settings.START_REMEMBERED_FILTER_SORT.get(Settings.currentProfile));
    bankPanelSearchBankWordTextField.setText(
        Settings.SEARCH_BANK_WORD.get(Settings.currentProfile));

    // Notifications tab
    notificationPanelPMNotifsCheckbox.setSelected(
        Settings.PM_NOTIFICATIONS.get(Settings.currentProfile));
    notificationPanelPMDenyListTextField.setText(
        Util.joinAsString(",", Settings.PM_DENYLIST.get("custom")));
    notificationPanelTradeNotifsCheckbox.setSelected(
        Settings.TRADE_NOTIFICATIONS.get(Settings.currentProfile));
    notificationPanelDuelNotifsCheckbox.setSelected(
        Settings.DUEL_NOTIFICATIONS.get(Settings.currentProfile));
    notificationPanelLogoutNotifsCheckbox.setSelected(
        Settings.LOGOUT_NOTIFICATIONS.get(Settings.currentProfile));
    notificationPanelLowHPNotifsCheckbox.setSelected(
        Settings.LOW_HP_NOTIFICATIONS.get(Settings.currentProfile));
    notificationPanelLowHPNotifsSpinner.setValue(
        Settings.LOW_HP_NOTIF_VALUE.get(Settings.currentProfile));
    notificationPanelFatigueNotifsCheckbox.setSelected(
        Settings.FATIGUE_NOTIFICATIONS.get(Settings.currentProfile));
    notificationPanelFatigueNotifsSpinner.setValue(
        Settings.FATIGUE_NOTIF_VALUE.get(Settings.currentProfile));
    notificationPanelHighlightedItemTimerCheckbox.setSelected(
        Settings.HIGHLIGHTED_ITEM_NOTIFICATIONS.get(Settings.currentProfile));
    notificationPanelHighlightedItemTimerSpinner.setValue(
        Settings.HIGHLIGHTED_ITEM_NOTIF_VALUE.get(Settings.currentProfile));
    notificationPanelNotifSoundsCheckbox.setSelected(
        Settings.NOTIFICATION_SOUNDS.get(Settings.currentProfile));
    notificationPanelUseSystemNotifsCheckbox.setSelected(
        Settings.USE_SYSTEM_NOTIFICATIONS.get(Settings.currentProfile));
    notificationPanelTrayPopupCheckbox.setSelected(
        Settings.TRAY_NOTIFS.get(Settings.currentProfile));
    notificationPanelTrayPopupClientFocusButton.setSelected(
        !Settings.TRAY_NOTIFS_ALWAYS.get(Settings.currentProfile));
    notificationPanelTrayPopupAnyFocusButton.setSelected(
        Settings.TRAY_NOTIFS_ALWAYS.get(Settings.currentProfile));
    notificationPanelNotifSoundClientFocusButton.setSelected(
        !Settings.SOUND_NOTIFS_ALWAYS.get(Settings.currentProfile));
    notificationPanelNotifSoundAnyFocusButton.setSelected(
        Settings.SOUND_NOTIFS_ALWAYS.get(Settings.currentProfile));
    importantMessagesTextField.setText(
        Util.joinAsString(",", Settings.IMPORTANT_MESSAGES.get("custom")));
    importantSadMessagesTextField.setText(
        Util.joinAsString(",", Settings.IMPORTANT_SAD_MESSAGES.get("custom")));
    notificationPanelMuteImportantMessageSoundsCheckbox.setSelected(
        Settings.MUTE_IMPORTANT_MESSAGE_SOUNDS.get(Settings.currentProfile));

    // Streaming & Privacy tab
    streamingPanelTwitchChatIntegrationEnabledCheckbox.setSelected(
        Settings.TWITCH_CHAT_ENABLED.get(Settings.currentProfile));
    streamingPanelTwitchChatCheckbox.setSelected(
        Settings.TWITCH_HIDE_CHAT.get(Settings.currentProfile));
    streamingPanelTwitchChannelNameTextField.setText(
        Settings.TWITCH_CHANNEL.get(Settings.currentProfile));
    streamingPanelTwitchOAuthTextField.setText(Settings.TWITCH_OAUTH.get(Settings.currentProfile));
    streamingPanelTwitchUserTextField.setText(
        Settings.TWITCH_USERNAME.get(Settings.currentProfile));
    streamingPanelIPAtLoginCheckbox.setSelected(
        Settings.SHOW_LOGIN_IP_ADDRESS.get(Settings.currentProfile));
    streamingPanelSaveLoginCheckbox.setSelected(
        Settings.SAVE_LOGININFO.get(Settings.currentProfile));
    streamingPanelStartLoginCheckbox.setSelected(
        Settings.START_LOGINSCREEN.get(Settings.currentProfile));
    streamingPanelSpeedrunnerCheckbox.setSelected(
        Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile));
    // streamingPanelSpeedrunnerUsernameTextField.setText(Settings.SPEEDRUNNER_USERNAME.get(Settings.currentProfile));

    // Replay tab
    replayPanelRecordAutomaticallyCheckbox.setSelected(
        Settings.RECORD_AUTOMATICALLY.get(Settings.currentProfile));
    replayPanelParseOpcodesCheckbox.setSelected(
        Settings.PARSE_OPCODES.get(Settings.currentProfile));
    replayPanelFastDisconnectCheckbox.setSelected(
        Settings.FAST_DISCONNECT.get(Settings.currentProfile));
    replayPanelRecordKBMouseCheckbox.setSelected(
        Settings.RECORD_KB_MOUSE.get(Settings.currentProfile));
    replayPanelHidePrivateMessagesCheckbox.setSelected(
        Settings.HIDE_PRIVATE_MSGS_REPLAY.get(Settings.currentProfile));
    replayPanelShowSeekBarCheckbox.setSelected(Settings.SHOW_SEEK_BAR.get(Settings.currentProfile));
    replayPanelShowPlayerControlsCheckbox.setSelected(
        Settings.SHOW_PLAYER_CONTROLS.get(Settings.currentProfile));
    replayPanelTriggerAlertsReplayCheckbox.setSelected(
        Settings.TRIGGER_ALERTS_REPLAY.get(Settings.currentProfile));
    replayPanelDateFormatTextField.setText(Settings.PREFERRED_DATE_FORMAT.get("custom"));
    replayPanelReplayFolderBasePathTextField.setText(Settings.REPLAY_BASE_PATH.get("custom"));
    replayPanelShowWorldColumnCheckbox.setSelected(
        Settings.SHOW_WORLD_COLUMN.get(Settings.currentProfile));
    replayPanelShowConversionSettingsCheckbox.setSelected(
        Settings.SHOW_CONVERSION_COLUMN.get(Settings.currentProfile));
    replayPanelShowUserFieldCheckbox.setSelected(
        Settings.SHOW_USERFIELD_COLUMN.get(Settings.currentProfile));

    // World List tab
    synchronizeWorldTab();

    // Joystick tab
    joystickPanelJoystickEnabledCheckbox.setSelected(
        Settings.JOYSTICK_ENABLED.get(Settings.currentProfile));

    for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
      setKeybindButtonText(kbs);
    }
  }

  /** Saves the settings from the GUI values to the settings class variables */
  public void saveSettings() {
    // General options
    Settings.CUSTOM_CLIENT_SIZE.put(
        Settings.currentProfile, generalPanelClientSizeCheckbox.isSelected());
    Settings.CUSTOM_CLIENT_SIZE_X.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (generalPanelClientSizeXSpinner.getModel())).getNumber().intValue());
    Settings.CUSTOM_CLIENT_SIZE_Y.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (generalPanelClientSizeYSpinner.getModel())).getNumber().intValue());
    Settings.CHECK_UPDATES.put(Settings.currentProfile, generalPanelCheckUpdates.isSelected());
    Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.put(
        Settings.currentProfile, generalPanelAccountSecurityCheckbox.isSelected());
    Settings.CONFIRM_CANCEL_RECOVERY_CHANGE.put(
        Settings.currentProfile, generalPanelConfirmCancelRecoveryChangeCheckbox.isSelected());
    Settings.SHOW_SECURITY_TIP_DAY.put(
        Settings.currentProfile, generalPanelShowSecurityTipsAtLoginCheckbox.isSelected());
    Settings.REMIND_HOW_TO_OPEN_SETTINGS.put(
        Settings.currentProfile, generalPanelWelcomeEnabled.isSelected());
    // Settings.LOAD_CHAT_HISTORY.put(Settings.currentProfile,
    // generalPanelChatHistoryCheckbox.isSelected());
    Settings.COMBAT_MENU_SHOWN.put(
        Settings.currentProfile, generalPanelCombatXPMenuCheckbox.isSelected());
    Settings.COMBAT_MENU_HIDDEN.put(
        Settings.currentProfile, generalPanelCombatXPMenuHiddenCheckbox.isSelected());
    Settings.SHOW_XPDROPS.put(Settings.currentProfile, overlayPanelXPDropsCheckbox.isSelected());
    Settings.CENTER_XPDROPS.put(
        Settings.currentProfile, overlayPanelXPCenterAlignFocusButton.isSelected());
    Settings.SHOW_FATIGUEDROPS.put(
        Settings.currentProfile, overlayPanelFatigueDropsCheckbox.isSelected());
    Settings.FATIGUE_FIGURES.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (overlayPanelFatigueFigSpinner.getModel())).getNumber().intValue());
    Settings.SHOW_FATIGUEUNITS.put(
        Settings.currentProfile, overlayPanelFatigueUnitsCheckbox.isSelected());
    Settings.FATIGUE_ALERT.put(
        Settings.currentProfile, generalPanelFatigueAlertCheckbox.isSelected());
    Settings.INVENTORY_FULL_ALERT.put(
        Settings.currentProfile, generalPanelInventoryFullAlertCheckbox.isSelected());
    Settings.NAME_PATCH_TYPE.put(
        Settings.currentProfile, generalPanelNamePatchModeSlider.getValue());
    Settings.LOG_VERBOSITY.put(Settings.currentProfile, generalPanelLogVerbositySlider.getValue());
    Settings.LOG_SHOW_TIMESTAMPS.put(
        Settings.currentProfile, generalPanelLogTimestampsCheckbox.isSelected());
    Settings.LOG_SHOW_LEVEL.put(Settings.currentProfile, generalPanelLogLevelCheckbox.isSelected());
    Settings.LOG_FORCE_TIMESTAMPS.put(
        Settings.currentProfile, generalPanelLogForceTimestampsCheckbox.isSelected());
    Settings.LOG_FORCE_LEVEL.put(
        Settings.currentProfile, generalPanelLogForceLevelCheckbox.isSelected());
    Settings.PREFERS_XDG_OPEN.put(
        Settings.currentProfile, generalPanelPrefersXdgOpenCheckbox.isSelected());

    Settings.COMMAND_PATCH_DISK.put(
        Settings.currentProfile, generalPanelCommandPatchDiskOfReturningCheckbox.isSelected());
    Settings.COMMAND_PATCH_EDIBLE_RARES.put(
        Settings.currentProfile, generalPanelCommandPatchEdibleRaresCheckbox.isSelected());
    Settings.COMMAND_PATCH_QUEST.put(
        Settings.currentProfile, generalPanelCommandPatchQuestCheckbox.isSelected());
    Settings.ATTACK_ALWAYS_LEFT_CLICK.put(
        Settings.currentProfile, generalPanelBypassAttackCheckbox.isSelected());
    Settings.ENABLE_MOUSEWHEEL_SCROLLING.put(
        Settings.currentProfile, generalPanelEnableMouseWheelScrollingCheckbox.isSelected());
    Settings.KEEP_SCROLLBAR_POS_MAGIC_PRAYER.put(
        Settings.currentProfile, generalPanelKeepScrollbarPosMagicPrayerCheckbox.isSelected());
    Settings.HIDE_ROOFS.put(Settings.currentProfile, generalPanelRoofHidingCheckbox.isSelected());
    Settings.DISABLE_UNDERGROUND_LIGHTING.put(
        Settings.currentProfile, generalPanelDisableUndergroundLightingCheckbox.isSelected());
    Settings.DISABLE_MINIMAP_ROTATION.put(
        Settings.currentProfile, generalPanelDisableMinimapRotationCheckbox.isSelected());
    Settings.CAMERA_ZOOMABLE.put(
        Settings.currentProfile, generalPanelCameraZoomableCheckbox.isSelected());
    Settings.CAMERA_ROTATABLE.put(
        Settings.currentProfile, generalPanelCameraRotatableCheckbox.isSelected());
    Settings.CAMERA_MOVABLE.put(
        Settings.currentProfile, generalPanelCameraMovableCheckbox.isSelected());
    Settings.CAMERA_MOVABLE_RELATIVE.put(
        Settings.currentProfile, generalPanelCameraMovableRelativeCheckbox.isSelected());
    Settings.COLORIZE_CONSOLE_TEXT.put(
        Settings.currentProfile, generalPanelColoredTextCheckbox.isSelected());
    Settings.FOV.put(Settings.currentProfile, generalPanelFoVSlider.getValue());
    Settings.SOFTWARE_CURSOR.put(
        Settings.currentProfile, generalPanelCustomCursorCheckbox.isSelected());
    Settings.DISABLE_RANDOM_CHAT_COLOUR.put(
        Settings.currentProfile, generalPanelDisableRandomChatColourCheckbox.isSelected());
    Settings.AUTO_SCREENSHOT.put(
        Settings.currentProfile, generalPanelAutoScreenshotCheckbox.isSelected());
    Settings.RS2HD_SKY.put(Settings.currentProfile, generalPanelRS2HDSkyCheckbox.isSelected());
    Settings.CUSTOM_SKYBOX_OVERWORLD_ENABLED.put(
        Settings.currentProfile, generalPanelCustomSkyboxOverworldCheckbox.isSelected());
    Settings.CUSTOM_SKYBOX_UNDERGROUND_ENABLED.put(
        Settings.currentProfile, generalPanelCustomSkyboxUndergroundCheckbox.isSelected());
    Settings.CUSTOM_SKYBOX_OVERWORLD_COLOUR.put(
        Settings.currentProfile, Util.colorToInt(overworldSkyColour));
    Settings.CUSTOM_SKYBOX_UNDERGROUND_COLOUR.put(
        Settings.currentProfile, Util.colorToInt(undergroundSkyColour));
    Settings.VIEW_DISTANCE.put(Settings.currentProfile, generalPanelViewDistanceSlider.getValue());
    Settings.FPS_LIMIT_ENABLED.put(
        Settings.currentProfile, generalPanelLimitFPSCheckbox.isSelected());
    Settings.FPS_LIMIT.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (generalPanelLimitFPSSpinner.getModel())).getNumber().intValue());
    Settings.PATCH_GENDER.put(
        Settings.currentProfile, generalPanelPatchGenderCheckbox.isSelected());
    Settings.PATCH_HBAR_512_LAST_PIXEL.put(
        Settings.currentProfile, generalPanelPatchHbar512LastPixelCheckbox.isSelected());
    Settings.USE_JAGEX_FONTS.put(
        Settings.currentProfile, generalPanelUseJagexFontsCheckBox.isSelected());
    Settings.PATCH_WRENCH_MENU_SPACING.put(
        Settings.currentProfile, generalPanelPatchWrenchMenuSpacingCheckbox.isSelected());

    // Overlays options
    Settings.SHOW_HP_PRAYER_FATIGUE_OVERLAY.put(
        Settings.currentProfile, overlayPanelStatusDisplayCheckbox.isSelected());
    Settings.SHOW_BUFFS.put(Settings.currentProfile, overlayPanelBuffsCheckbox.isSelected());
    Settings.SHOW_LAST_MENU_ACTION.put(
        Settings.currentProfile, overlayPanelLastMenuActionCheckbox.isSelected());
    Settings.SHOW_MOUSE_TOOLTIP.put(
        Settings.currentProfile, overlayPanelMouseTooltipCheckbox.isSelected());
    Settings.SHOW_EXTENDED_TOOLTIP.put(
        Settings.currentProfile, overlayPanelExtendedTooltipCheckbox.isSelected());
    Settings.SHOW_INVCOUNT.put(Settings.currentProfile, overlayPanelInvCountCheckbox.isSelected());
    Settings.SHOW_INVCOUNT_COLOURS.put(
        Settings.currentProfile, overlayPanelInvCountColoursCheckbox.isSelected());
    Settings.SHOW_RSCPLUS_BUTTONS.put(
        Settings.currentProfile, overlayPanelRscPlusButtonsCheckbox.isSelected());
    Settings.RSCPLUS_BUTTONS_FUNCTIONAL.put(
        Settings.currentProfile, overlayPanelRscPlusButtonsFunctionalCheckbox.isSelected());
    Settings.WIKI_LOOKUP_ON_MAGIC_BOOK.put(
        Settings.currentProfile, overlayPanelWikiLookupOnMagicBookCheckbox.isSelected());
    Settings.WIKI_LOOKUP_ON_HBAR.put(
        Settings.currentProfile, overlayPanelWikiLookupOnHbarCheckbox.isSelected());
    Settings.TOGGLE_XP_BAR_ON_STATS_BUTTON.put(
        Settings.currentProfile, overlayPanelToggleXPBarOnStatsButtonCheckbox.isSelected());
    Settings.HISCORES_LOOKUP_BUTTON.put(
        Settings.currentProfile, overlayPaneHiscoresLookupButtonCheckbox.isSelected());
    Settings.MOTIVATIONAL_QUOTES_BUTTON.put(
        Settings.currentProfile, overlayPanelToggleMotivationalQuotesCheckbox.isSelected());
    Settings.REMOVE_REPORT_ABUSE_BUTTON_HBAR.put(
        Settings.currentProfile, overlayPanelRemoveReportAbuseButtonHbarCheckbox.isSelected());
    Settings.SHOW_PLAYER_POSITION.put(
        Settings.currentProfile, overlayPanelPositionCheckbox.isSelected());
    Settings.SHOW_RETRO_FPS.put(Settings.currentProfile, overlayPanelRetroFpsCheckbox.isSelected());
    Settings.SHOW_ITEM_GROUND_OVERLAY.put(
        Settings.currentProfile, overlayPanelItemNamesCheckbox.isSelected());
    Settings.SHOW_PLAYER_NAME_OVERLAY.put(
        Settings.currentProfile, overlayPanelPlayerNamesCheckbox.isSelected());
    Settings.SHOW_FRIEND_NAME_OVERLAY.put(
        Settings.currentProfile, overlayPanelFriendNamesCheckbox.isSelected());
    Settings.SHOW_NPC_NAME_OVERLAY.put(
        Settings.currentProfile, overlayPanelNPCNamesCheckbox.isSelected());
    Settings.EXTEND_IDS_OVERLAY.put(Settings.currentProfile, overlayPanelIDsCheckbox.isSelected());
    Settings.TRACE_OBJECT_INFO.put(
        Settings.currentProfile, overlayPanelObjectInfoCheckbox.isSelected());
    Settings.SHOW_HITBOX.put(Settings.currentProfile, overlayPanelHitboxCheckbox.isSelected());
    Settings.SHOW_COMBAT_INFO.put(
        Settings.currentProfile, overlayPanelShowCombatInfoCheckbox.isSelected());
    Settings.NPC_HEALTH_SHOW_PERCENTAGE.put(
        Settings.currentProfile, overlayPanelUsePercentageCheckbox.isSelected());
    Settings.SHOW_XP_BAR.put(Settings.currentProfile, overlayPanelXPBarCheckbox.isSelected());
    Settings.SHOW_FOOD_HEAL_OVERLAY.put(
        Settings.currentProfile, overlayPanelFoodHealingCheckbox.isSelected());
    Settings.SHOW_TIME_UNTIL_HP_REGEN.put(
        Settings.currentProfile, overlayPanelHPRegenTimerCheckbox.isSelected());
    Settings.LAG_INDICATOR.put(
        Settings.currentProfile, overlayPanelLagIndicatorCheckbox.isSelected());
    Settings.DEBUG.put(Settings.currentProfile, generalPanelDebugModeCheckbox.isSelected());
    Settings.EXCEPTION_HANDLER.put(
        Settings.currentProfile, generalPanelExceptionHandlerCheckbox.isSelected());
    Settings.HIGHLIGHTED_ITEMS.put(
        "custom", new ArrayList<>(Arrays.asList(highlightedItemsTextField.getText().split(","))));
    Settings.BLOCKED_ITEMS.put(
        "custom", new ArrayList<>(Arrays.asList(blockedItemsTextField.getText().split(","))));

    // Audio options
    Settings.CUSTOM_MUSIC.put(Settings.currentProfile, audioPanelEnableMusicCheckbox.isSelected());
    Settings.LOUDER_SOUND_EFFECTS.put(
        Settings.currentProfile, audioPanelLouderSoundEffectsCheckbox.isSelected());
    Settings.OVERRIDE_AUDIO_SETTING.put(
        Settings.currentProfile, audioPanelOverrideAudioSettingCheckbox.isSelected());
    Settings.OVERRIDE_AUDIO_SETTING_SETTING_ON.put(
        Settings.currentProfile,
        audioPanelOverrideAudioSettingOnButton
            .isSelected()); // audioPanelOverrideAudioSettingOffButton ignored
    Settings.SOUND_EFFECT_COMBAT1.put(
        Settings.currentProfile, audioPanelFixSpiderWebDummySoundCheckbox.isSelected());
    Settings.SOUND_EFFECT_ADVANCE.put(
        Settings.currentProfile, soundEffectAdvanceCheckbox.isSelected());
    Settings.SOUND_EFFECT_ANVIL.put(Settings.currentProfile, soundEffectAnvilCheckbox.isSelected());
    Settings.SOUND_EFFECT_CHISEL.put(
        Settings.currentProfile, soundEffectChiselCheckbox.isSelected());
    Settings.SOUND_EFFECT_CLICK.put(Settings.currentProfile, soundEffectClickCheckbox.isSelected());
    Settings.SOUND_EFFECT_CLOSEDOOR.put(
        Settings.currentProfile, soundEffectClosedoorCheckbox.isSelected());
    Settings.SOUND_EFFECT_COINS.put(Settings.currentProfile, soundEffectCoinsCheckbox.isSelected());
    Settings.SOUND_EFFECT_COMBAT1A.put(
        Settings.currentProfile, soundEffectCombat1aCheckbox.isSelected());
    Settings.SOUND_EFFECT_COMBAT1B.put(
        Settings.currentProfile, soundEffectCombat1bCheckbox.isSelected());
    Settings.SOUND_EFFECT_COMBAT2A.put(
        Settings.currentProfile, soundEffectCombat2aCheckbox.isSelected());
    Settings.SOUND_EFFECT_COMBAT2B.put(
        Settings.currentProfile, soundEffectCombat2bCheckbox.isSelected());
    Settings.SOUND_EFFECT_COMBAT3A.put(
        Settings.currentProfile, soundEffectCombat3aCheckbox.isSelected());
    Settings.SOUND_EFFECT_COMBAT3B.put(
        Settings.currentProfile, soundEffectCombat3bCheckbox.isSelected());
    Settings.SOUND_EFFECT_COOKING.put(
        Settings.currentProfile, soundEffectCookingCheckbox.isSelected());
    Settings.SOUND_EFFECT_DEATH.put(Settings.currentProfile, soundEffectDeathCheckbox.isSelected());
    Settings.SOUND_EFFECT_DROPOBJECT.put(
        Settings.currentProfile, soundEffectDropobjectCheckbox.isSelected());
    Settings.SOUND_EFFECT_EAT.put(Settings.currentProfile, soundEffectEatCheckbox.isSelected());
    Settings.SOUND_EFFECT_FILLJUG.put(
        Settings.currentProfile, soundEffectFilljugCheckbox.isSelected());
    Settings.SOUND_EFFECT_FISH.put(Settings.currentProfile, soundEffectFishCheckbox.isSelected());
    Settings.SOUND_EFFECT_FOUNDGEM.put(
        Settings.currentProfile, soundEffectFoundgemCheckbox.isSelected());
    Settings.SOUND_EFFECT_MECHANICAL.put(
        Settings.currentProfile, soundEffectMechanicalCheckbox.isSelected());
    Settings.SOUND_EFFECT_MINE.put(Settings.currentProfile, soundEffectMineCheckbox.isSelected());
    Settings.SOUND_EFFECT_MIX.put(Settings.currentProfile, soundEffectMixCheckbox.isSelected());
    Settings.SOUND_EFFECT_OPENDOOR.put(
        Settings.currentProfile, soundEffectOpendoorCheckbox.isSelected());
    Settings.SOUND_EFFECT_OUTOFAMMO.put(
        Settings.currentProfile, soundEffectOutofammoCheckbox.isSelected());
    Settings.SOUND_EFFECT_POTATO.put(
        Settings.currentProfile, soundEffectPotatoCheckbox.isSelected());
    Settings.SOUND_EFFECT_PRAYEROFF.put(
        Settings.currentProfile, soundEffectPrayeroffCheckbox.isSelected());
    Settings.SOUND_EFFECT_PRAYERON.put(
        Settings.currentProfile, soundEffectPrayeronCheckbox.isSelected());
    Settings.SOUND_EFFECT_PROSPECT.put(
        Settings.currentProfile, soundEffectProspectCheckbox.isSelected());
    Settings.SOUND_EFFECT_RECHARGE.put(
        Settings.currentProfile, soundEffectRechargeCheckbox.isSelected());
    Settings.SOUND_EFFECT_RETREAT.put(
        Settings.currentProfile, soundEffectRetreatCheckbox.isSelected());
    Settings.SOUND_EFFECT_SECRETDOOR.put(
        Settings.currentProfile, soundEffectSecretdoorCheckbox.isSelected());
    Settings.SOUND_EFFECT_SHOOT.put(Settings.currentProfile, soundEffectShootCheckbox.isSelected());
    Settings.SOUND_EFFECT_SPELLFAIL.put(
        Settings.currentProfile, soundEffectSpellfailCheckbox.isSelected());
    Settings.SOUND_EFFECT_SPELLOK.put(
        Settings.currentProfile, soundEffectSpellokCheckbox.isSelected());
    Settings.SOUND_EFFECT_TAKEOBJECT.put(
        Settings.currentProfile, soundEffectTakeobjectCheckbox.isSelected());
    Settings.SOUND_EFFECT_UNDERATTACK.put(
        Settings.currentProfile, soundEffectUnderattackCheckbox.isSelected());
    Settings.SOUND_EFFECT_VICTORY.put(
        Settings.currentProfile, soundEffectVictoryCheckbox.isSelected());

    // Bank options
    Settings.SHOW_BANK_VALUE.put(
        Settings.currentProfile, bankPanelCalculateBankValueCheckbox.isSelected());
    Settings.SORT_FILTER_BANK.put(
        Settings.currentProfile, bankPanelSortFilterAugmentCheckbox.isSelected());
    Settings.START_REMEMBERED_FILTER_SORT.put(
        Settings.currentProfile, bankPanelStartSearchedBankCheckbox.isSelected());
    Settings.SEARCH_BANK_WORD.put(
        Settings.currentProfile, bankPanelSearchBankWordTextField.getText().trim().toLowerCase());

    // Notifications options
    Settings.PM_NOTIFICATIONS.put(
        Settings.currentProfile, notificationPanelPMNotifsCheckbox.isSelected());
    Settings.PM_DENYLIST.put(
        "custom",
        new ArrayList<>(Arrays.asList(notificationPanelPMDenyListTextField.getText().split(","))));
    Settings.TRADE_NOTIFICATIONS.put(
        Settings.currentProfile, notificationPanelTradeNotifsCheckbox.isSelected());
    Settings.DUEL_NOTIFICATIONS.put(
        Settings.currentProfile, notificationPanelDuelNotifsCheckbox.isSelected());
    Settings.LOGOUT_NOTIFICATIONS.put(
        Settings.currentProfile, notificationPanelLogoutNotifsCheckbox.isSelected());
    Settings.LOW_HP_NOTIFICATIONS.put(
        Settings.currentProfile, notificationPanelLowHPNotifsCheckbox.isSelected());
    Settings.LOW_HP_NOTIF_VALUE.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (notificationPanelLowHPNotifsSpinner.getModel()))
            .getNumber()
            .intValue());
    Settings.FATIGUE_NOTIFICATIONS.put(
        Settings.currentProfile, notificationPanelFatigueNotifsCheckbox.isSelected());
    Settings.FATIGUE_NOTIF_VALUE.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (notificationPanelFatigueNotifsSpinner.getModel()))
            .getNumber()
            .intValue());
    Settings.HIGHLIGHTED_ITEM_NOTIFICATIONS.put(
        Settings.currentProfile, notificationPanelHighlightedItemTimerCheckbox.isSelected());
    Settings.HIGHLIGHTED_ITEM_NOTIF_VALUE.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (notificationPanelHighlightedItemTimerSpinner.getModel()))
            .getNumber()
            .intValue());
    Settings.NOTIFICATION_SOUNDS.put(
        Settings.currentProfile, notificationPanelNotifSoundsCheckbox.isSelected());
    Settings.USE_SYSTEM_NOTIFICATIONS.put(
        Settings.currentProfile, notificationPanelUseSystemNotifsCheckbox.isSelected());
    Settings.TRAY_NOTIFS.put(
        Settings.currentProfile, notificationPanelTrayPopupCheckbox.isSelected());
    Settings.TRAY_NOTIFS_ALWAYS.put(
        Settings.currentProfile, notificationPanelTrayPopupAnyFocusButton.isSelected());
    Settings.SOUND_NOTIFS_ALWAYS.put(
        Settings.currentProfile, notificationPanelNotifSoundAnyFocusButton.isSelected());
    Settings.IMPORTANT_MESSAGES.put(
        "custom", new ArrayList<>(Arrays.asList(importantMessagesTextField.getText().split(","))));
    Settings.IMPORTANT_SAD_MESSAGES.put(
        "custom",
        new ArrayList<>(Arrays.asList(importantSadMessagesTextField.getText().split(","))));
    Settings.MUTE_IMPORTANT_MESSAGE_SOUNDS.put(
        Settings.currentProfile, notificationPanelMuteImportantMessageSoundsCheckbox.isSelected());

    // Streaming & Privacy
    Settings.TWITCH_CHAT_ENABLED.put(
        Settings.currentProfile, streamingPanelTwitchChatIntegrationEnabledCheckbox.isSelected());
    Settings.TWITCH_HIDE_CHAT.put(
        Settings.currentProfile, streamingPanelTwitchChatCheckbox.isSelected());
    Settings.TWITCH_CHANNEL.put(
        Settings.currentProfile, streamingPanelTwitchChannelNameTextField.getText());
    Settings.TWITCH_OAUTH.put(
        Settings.currentProfile, streamingPanelTwitchOAuthTextField.getText());
    Settings.TWITCH_USERNAME.put(
        Settings.currentProfile, streamingPanelTwitchUserTextField.getText());
    Settings.SHOW_LOGIN_IP_ADDRESS.put(
        Settings.currentProfile, streamingPanelIPAtLoginCheckbox.isSelected());
    Settings.SAVE_LOGININFO.put(
        Settings.currentProfile, streamingPanelSaveLoginCheckbox.isSelected());
    Settings.START_LOGINSCREEN.put(
        Settings.currentProfile, streamingPanelStartLoginCheckbox.isSelected());
    Settings.SPEEDRUNNER_MODE_ACTIVE.put(
        Settings.currentProfile, streamingPanelSpeedrunnerCheckbox.isSelected());
    // Settings.SPEEDRUNNER_USERNAME.put(
    //    Settings.currentProfile, streamingPanelSpeedrunnerUsernameTextField.getText());

    // Replay
    Settings.RECORD_AUTOMATICALLY.put(
        Settings.currentProfile, replayPanelRecordAutomaticallyCheckbox.isSelected());
    Settings.PARSE_OPCODES.put(
        Settings.currentProfile, replayPanelParseOpcodesCheckbox.isSelected());
    Settings.FAST_DISCONNECT.put(
        Settings.currentProfile, replayPanelFastDisconnectCheckbox.isSelected());
    Settings.RECORD_KB_MOUSE.put(
        Settings.currentProfile, replayPanelRecordKBMouseCheckbox.isSelected());
    Settings.HIDE_PRIVATE_MSGS_REPLAY.put(
        Settings.currentProfile, replayPanelHidePrivateMessagesCheckbox.isSelected());
    Settings.SHOW_SEEK_BAR.put(
        Settings.currentProfile, replayPanelShowSeekBarCheckbox.isSelected());
    Settings.SHOW_PLAYER_CONTROLS.put(
        Settings.currentProfile, replayPanelShowPlayerControlsCheckbox.isSelected());
    Settings.TRIGGER_ALERTS_REPLAY.put(
        Settings.currentProfile, replayPanelTriggerAlertsReplayCheckbox.isSelected());
    Settings.REPLAY_BASE_PATH.put(
        Settings.currentProfile, replayPanelReplayFolderBasePathTextField.getText());
    Settings.PREFERRED_DATE_FORMAT.put(
        Settings.currentProfile, replayPanelDateFormatTextField.getText());
    Settings.SHOW_WORLD_COLUMN.put(
        Settings.currentProfile, replayPanelShowWorldColumnCheckbox.isSelected());
    Settings.SHOW_CONVERSION_COLUMN.put(
        Settings.currentProfile, replayPanelShowConversionSettingsCheckbox.isSelected());
    Settings.SHOW_USERFIELD_COLUMN.put(
        Settings.currentProfile, replayPanelShowUserFieldCheckbox.isSelected());

    // Presets
    if (presetsPanelCustomSettingsCheckbox.isSelected()) {
      if (!Settings.currentProfile.equals("custom")) {
        Settings.currentProfile = "custom";
        Logger.Info("Changed to custom profile");
      }
    } else {
      String lastPresetValue = Settings.currentProfile;

      int presetValue = presetsPanelPresetSlider.getValue();
      if (presetValue >= 0 && presetValue <= Settings.presetTable.size()) {
        Settings.currentProfile = Settings.presetTable.get(presetValue);
      } else { // not custom, and also out of range for presetTable
        Logger.Error("presetsPanelPresetSlider out of range of Settings.presetTable");
      }

      if (!lastPresetValue.equals(Settings.currentProfile)) {
        String saveMe = Settings.currentProfile;
        Settings.initSettings(); // reset preset values to their defaults
        Settings.currentProfile = saveMe;
        Logger.Info("Changed to " + Settings.currentProfile + " preset");
      }
    }

    // World List
    for (int i = 1; i <= Settings.WORLDS_TO_DISPLAY; i++) {
      Settings.WORLD_NAMES.put(
          i, getTextWithDefault(worldNamesJTextFields, i, String.format("World %d", i)));
      Settings.WORLD_URLS.put(i, worldUrlsJTextFields.get(i).getText());

      Settings.WORLD_SERVER_TYPES.put(i, (Integer) worldTypesJComboBoxes.get(i).getSelectedIndex());

      String portString = worldPortsJTextFields.get(i).getText();
      if (portString.equals("")) {
        Settings.WORLD_PORTS.put(i, Replay.DEFAULT_PORT);
      } else {
        Settings.WORLD_PORTS.put(i, Integer.parseInt(portString));
      }
      Settings.WORLD_RSA_PUB_KEYS.put(i, worldRSAPubKeyJTextFields.get(i).getText());
      Settings.WORLD_RSA_EXPONENTS.put(i, worldRSAExponentsJTextFields.get(i).getText());
      Settings.WORLD_HISCORES_URL.put(i, worldListHiscoresURLTextFieldContainers.get(i).getText());
    }
    if (Client.state == Client.STATE_LOGIN)
      Game.getInstance().getJConfig().changeWorld(Settings.WORLD.get(Settings.currentProfile));

    //// joystick
    Settings.JOYSTICK_ENABLED.put(
        Settings.currentProfile, joystickPanelJoystickEnabledCheckbox.isSelected());

    // Save Settings
    Settings.save();
  }

  private String getTextWithDefault(
      HashMap<Integer, JTextField> textFields, int index, String defaultValue) {
    String value = textFields.get(index).getText();
    if (value.equals("")) return defaultValue;
    else return value;
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
   *
   * <p>Note that this method should be used to apply any additional settings that are not applied
   * automatically, such as those already present. Also note that thread-unsafe operations affecting
   * the applet should not be done in this method, as this method is invoked by the AWT event queue.
   */
  public void applySettings() {
    saveSettings();
    if (Settings.CUSTOM_CLIENT_SIZE.get(Settings.currentProfile))
      Game.getInstance().resizeFrameWithContents();
    // Tell the Renderer to update the FoV from its thread to avoid thread-safety issues.
    Settings.fovUpdateRequired = true;
    Settings.checkSoftwareCursor();
    Camera.setDistance(Settings.VIEW_DISTANCE.get(Settings.currentProfile));
    synchronizeGuiValues();
    QueueWindow.syncColumnsWithSettings();
    QueueWindow.playlistTable.repaint();
    Item.patchItemNames();
    Item.patchItemCommands();
    GameApplet.syncFontSetting();
  }

  public void synchronizePresetOptions() {
    if (presetsPanelCustomSettingsCheckbox.isSelected()) {
      if (sliderValue == -1) {
        presetsPanelPresetSlider.setValue(Settings.presetTable.indexOf("default"));
      } else {
        presetsPanelPresetSlider.setValue(sliderValue);
      }
      presetsPanelPresetSlider.setEnabled(false);
      replaceConfigButton.setEnabled(false);
      resetPresetsButton.setEnabled(false);
    } else {
      presetsPanelPresetSlider.setEnabled(true);
      replaceConfigButton.setEnabled(true);
      resetPresetsButton.setEnabled(true);
    }
  }

  public void addWorldFields(int i) {
    //// Name line
    worldListTitleTextFieldContainers.put(i, new JPanel());
    worldListTitleTextFieldContainers.get(i).setLayout(new GridBagLayout());
    GridBagConstraints cR = new GridBagConstraints();
    cR.fill = GridBagConstraints.HORIZONTAL;
    cR.anchor = GridBagConstraints.LINE_START;
    cR.weightx = 0.1;
    cR.gridy = 0;
    cR.gridwidth = 1;

    JLabel worldNumberJLabel = new JLabel(String.format("<html><b>World %d</b></html>", i));
    worldNumberJLabel.setAlignmentY((float) 0.75);
    worldListTitleTextFieldContainers.get(i).add(worldNumberJLabel, cR);

    cR.weightx = 0.5;
    cR.gridwidth = 5;

    worldNamesJTextFields.put(i, new HintTextField("Name of World"));
    worldNamesJTextFields.get(i).setMinimumSize(new Dimension(80, 28));
    worldNamesJTextFields.get(i).setMaximumSize(new Dimension(300, 28));
    worldNamesJTextFields.get(i).setPreferredSize(new Dimension(200, 28));
    worldNamesJTextFields.get(i).setAlignmentY((float) 0.75);
    worldListTitleTextFieldContainers.get(i).add(worldNamesJTextFields.get(i), cR);

    cR.weightx = 0.1;
    cR.gridwidth = 1;
    cR.anchor = GridBagConstraints.LINE_END;

    /*
          JLabel spacingJLabel = new JLabel("");
          worldNumberJLabel.setAlignmentY((float) 0.75);
          worldListTitleTextFieldContainers.get(i).add(spacingJLabel, cR);
    */

    String[] worldTypes = {"Free", "Members", "Free (Veteran)", "Members (Veteran)"};
    JComboBox worldTypeComboBox = new JComboBox(worldTypes);

    worldTypesJComboBoxes.put(i, worldTypeComboBox);
    worldTypesJComboBoxes.get(i).setMinimumSize(new Dimension(120, 28));
    worldTypesJComboBoxes.get(i).setMaximumSize(new Dimension(120, 28));
    worldTypesJComboBoxes.get(i).setPreferredSize(new Dimension(120, 28));
    worldTypesJComboBoxes.get(i).setAlignmentY((float) 0.75);
    worldListTitleTextFieldContainers.get(i).add(worldTypesJComboBoxes.get(i), cR);

    cR.weightx = 0.3;
    cR.gridwidth = 1;

    worldDeleteJButtons.put(i, new JButton("Delete World"));
    worldDeleteJButtons.get(i).setAlignmentY((float) 0.80);
    worldDeleteJButtons.get(i).setPreferredSize(new Dimension(50, 28));
    worldTypesJComboBoxes.get(i).setMinimumSize(new Dimension(50, 28));
    worldTypesJComboBoxes.get(i).setMaximumSize(new Dimension(50, 28));
    worldDeleteJButtons.get(i).setActionCommand(String.format("%d", i));
    worldDeleteJButtons
        .get(i)
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                String actionCommandWorld = e.getActionCommand();
                int choice =
                    JOptionPane.showConfirmDialog(
                        Launcher.getConfigWindow().frame,
                        String.format(
                            "Warning: Are you sure you want to DELETE World %s?",
                            actionCommandWorld),
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.CLOSED_OPTION || choice == JOptionPane.NO_OPTION) {
                  return;
                }

                Logger.Info("Deleting World " + actionCommandWorld);
                Settings.removeWorld(Integer.parseInt(actionCommandWorld));
              }
            });

    worldListTitleTextFieldContainers.get(i).add(worldDeleteJButtons.get(i), cR);

    worldListTitleTextFieldContainers.get(i).setMaximumSize(new Dimension(680, 40));
    worldListPanel.add(worldListTitleTextFieldContainers.get(i));

    //// URL/Ports line
    worldUrlsJTextFields.put(i, new HintTextField(String.format("World %d URL", i)));
    worldPortsJTextFields.put(
        i, new HintTextField(String.format("World %d Port (default: 43594)", i)));

    worldUrlsJTextFields.get(i).setMinimumSize(new Dimension(100, 28));
    worldUrlsJTextFields.get(i).setMaximumSize(new Dimension(500, 28));
    worldUrlsJTextFields.get(i).setPreferredSize(new Dimension(500, 28));
    worldUrlsJTextFields.get(i).setAlignmentY((float) 0.75);

    worldPortsJTextFields.get(i).setMinimumSize(new Dimension(100, 28));
    worldPortsJTextFields.get(i).setMaximumSize(new Dimension(180, 28));
    worldPortsJTextFields.get(i).setAlignmentY((float) 0.75);

    worldListURLPortTextFieldContainers.put(i, new JPanel());

    worldListURLPortTextFieldContainers
        .get(i)
        .setLayout(new BoxLayout(worldListURLPortTextFieldContainers.get(i), BoxLayout.X_AXIS));

    worldListURLPortTextFieldContainers.get(i).add(worldUrlsJTextFields.get(i));
    worldListURLPortTextFieldContainers.get(i).add(worldPortsJTextFields.get(i));
    worldListPanel.add(worldListURLPortTextFieldContainers.get(i));

    //// RSA Pubkey/Exponent line
    worldRSAPubKeyJTextFields.put(
        i, new HintTextField(String.format("World %d RSA Public Key", i)));
    worldRSAExponentsJTextFields.put(
        i, new HintTextField(String.format("World %d RSA Exponent", i)));

    worldRSAPubKeyJTextFields.get(i).setMinimumSize(new Dimension(100, 28));
    worldRSAPubKeyJTextFields.get(i).setMaximumSize(new Dimension(500, 28));
    worldRSAPubKeyJTextFields.get(i).setPreferredSize(new Dimension(500, 28));
    worldRSAPubKeyJTextFields.get(i).setAlignmentY((float) 0.75);

    worldRSAExponentsJTextFields.get(i).setMinimumSize(new Dimension(100, 28));
    worldRSAExponentsJTextFields.get(i).setMaximumSize(new Dimension(180, 28));
    worldRSAExponentsJTextFields.get(i).setAlignmentY((float) 0.75);

    worldListRSATextFieldContainers.put(i, new JPanel());

    worldListRSATextFieldContainers
        .get(i)
        .setLayout(new BoxLayout(worldListRSATextFieldContainers.get(i), BoxLayout.X_AXIS));

    worldListRSATextFieldContainers.get(i).add(worldRSAPubKeyJTextFields.get(i));
    worldListRSATextFieldContainers.get(i).add(worldRSAExponentsJTextFields.get(i));
    worldListPanel.add(worldListRSATextFieldContainers.get(i));

    //// Hiscores URL line
    worldListHiscoresTextFieldContainers.put(i, new JPanel());
    worldListHiscoresTextFieldContainers.get(i).setLayout(new GridBagLayout());
    cR.fill = GridBagConstraints.HORIZONTAL;
    cR.anchor = GridBagConstraints.LINE_START;
    cR.weightx = 0.1;
    cR.gridy = 0;
    cR.gridwidth = 1;

    JLabel hiscoresURLJLabel = new JLabel("<html><b>Hiscores URL</b></html>");
    hiscoresURLJLabel.setAlignmentY((float) 1);
    worldListHiscoresTextFieldContainers.get(i).add(hiscoresURLJLabel, cR);

    worldListHiscoresURLTextFieldContainers.put(
        i, new HintTextField(String.format("World %d Hiscores URL", i)));

    worldListHiscoresURLTextFieldContainers.get(i).setMinimumSize(new Dimension(100, 28));
    worldListHiscoresURLTextFieldContainers.get(i).setMaximumSize(new Dimension(580, 28));
    worldListHiscoresURLTextFieldContainers.get(i).setPreferredSize(new Dimension(580, 28));
    worldListHiscoresURLTextFieldContainers.get(i).setAlignmentY((float) 0.75);

    worldListHiscoresTextFieldContainers
        .get(i)
        .setLayout(new BoxLayout(worldListHiscoresTextFieldContainers.get(i), BoxLayout.X_AXIS));

    worldListHiscoresTextFieldContainers.get(i).add(worldListHiscoresURLTextFieldContainers.get(i));
    worldListHiscoresTextFieldContainers.get(i).setMaximumSize(new Dimension(680, 28));
    worldListHiscoresTextFieldContainers
        .get(i)
        .setVisible(Settings.HISCORES_LOOKUP_BUTTON.get(Settings.currentProfile));
    worldListPanel.add(worldListHiscoresTextFieldContainers.get(i));

    //// spacing between worlds
    worldListSpacingLabels.put(i, new JLabel(""));
    worldListSpacingLabels.get(i).setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
    worldListPanel.add(worldListSpacingLabels.get(i));

    //// create world
    if (i > Settings.WORLD_NAMES.size()) {
      Settings.createNewWorld(i);
    }
  }

  public void addAddWorldButton() {
    JButton addWorldButton = new JButton("Add New World");
    addWorldButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            worldListPanel.remove(addWorldButton);
            ++Settings.WORLDS_TO_DISPLAY;
            synchronizeWorldTab();
            addAddWorldButton();
          }
        });
    worldListPanel.add(addWorldButton);
    worldListPanel.revalidate();
    worldListPanel.repaint();
  }

  // adds or removes world list text fields & fills them with their values
  public void synchronizeWorldTab() {
    int numberOfWorldsEver = worldUrlsJTextFields.size();
    // sync values from Settings (read in from file) & hide worlds that have gotten deleted
    if (Settings.WORLDS_TO_DISPLAY > numberOfWorldsEver) {
      addWorldFields(Settings.WORLDS_TO_DISPLAY);
    }
    for (int i = 1; (i <= numberOfWorldsEver) || (i <= Settings.WORLDS_TO_DISPLAY); i++) {
      if (i <= Settings.WORLDS_TO_DISPLAY) {
        worldNamesJTextFields.get(i).setText(Settings.WORLD_NAMES.get(i));
        worldTypesJComboBoxes
            .get(i)
            .setSelectedIndex(Settings.WORLD_SERVER_TYPES.getOrDefault(i, 1));
        worldUrlsJTextFields.get(i).setText(Settings.WORLD_URLS.get(i));
        try {
          worldPortsJTextFields.get(i).setText(Settings.WORLD_PORTS.get(i).toString());
        } catch (Exception e) {
          worldNamesJTextFields.get(i).setText(String.format("World %d", i));
          Settings.createNewWorld(i);
        }
        worldRSAPubKeyJTextFields.get(i).setText(Settings.WORLD_RSA_PUB_KEYS.get(i));
        worldRSAExponentsJTextFields.get(i).setText(Settings.WORLD_RSA_EXPONENTS.get(i));
        worldListHiscoresURLTextFieldContainers.get(i).setText(Settings.WORLD_HISCORES_URL.get(i));
        worldListTitleTextFieldContainers.get(i).setVisible(true);
        worldListURLPortTextFieldContainers.get(i).setVisible(true);
        worldListRSATextFieldContainers.get(i).setVisible(true);
        worldListSpacingLabels.get(i).setVisible(true);
        worldListHiscoresTextFieldContainers
            .get(i)
            .setVisible(Settings.HISCORES_LOOKUP_BUTTON.get(Settings.currentProfile));
      } else {
        worldListTitleTextFieldContainers.get(i).setVisible(false);
        worldListURLPortTextFieldContainers.get(i).setVisible(false);
        worldListRSATextFieldContainers.get(i).setVisible(false);
        worldListSpacingLabels.get(i).setVisible(false);
        worldListHiscoresTextFieldContainers.get(i).setVisible(false);
      }
    }
  }

  public void updateJoystickInput(String compName) {
    joystickInputValueJlabels
        .get(compName)
        .setText(
            String.format(
                "%d", (int) Math.floor(JoystickHandler.joystickInputReports.get(compName))));
    joystickPanel.revalidate();
    joystickPanel.repaint();
  }
}

/** Implements ActionListener; to be used for the buttons in the keybinds tab. */
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
    JButton button = (JButton) arg0.getSource();

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

    if (arg0.getKeyCode() == KeyEvent.VK_CONTROL
        || arg0.getKeyCode() == KeyEvent.VK_SHIFT
        || arg0.getKeyCode() == KeyEvent.VK_ALT) {
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
    JButton jbtn = (JButton) arg0.getSource();

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
  public void keyReleased(KeyEvent arg0) {}

  @Override
  public void keyTyped(KeyEvent arg0) {}
}

class HintTextField extends JTextField {
  public HintTextField(String hint) {
    _hint = hint;
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    if (getText().length() == 0) {
      int h = getHeight();
      ((Graphics2D) g)
          .setRenderingHint(
              RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      Insets ins = getInsets();
      FontMetrics fm = g.getFontMetrics();
      int c0 = getBackground().getRGB();
      int c1 = getForeground().getRGB();
      int m = 0xfefefefe;
      int c2 = ((c0 & m) >>> 1) + ((c1 & m) >>> 1);
      g.setColor(new Color(c2, true));
      g.drawString(_hint, ins.left, h / 2 + fm.getAscent() / 2 - 2);
    }
  }

  private final String _hint;
}
