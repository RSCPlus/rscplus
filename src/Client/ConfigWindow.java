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

import static Client.Util.isDarkThemeFlatLAF;
import static Client.Util.isUsingFlatLAFTheme;
import static Client.Util.osScaleDiv;
import static Client.Util.osScaleMul;

import Client.KeybindSet.KeyModifier;
import Game.Bank;
import Game.Camera;
import Game.Client;
import Game.Game;
import Game.GameApplet;
import Game.Item;
import Game.JoystickHandler;
import Game.KeyboardHandler;
import Game.Renderer;
import Game.Replay;
import Game.SoundEffects;
import com.formdev.flatlaf.ui.FlatRoundBorder;
import java.awt.AWTEvent;
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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import javax.swing.JComponent;
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
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jsoup.Jsoup;

/**
 * GUI designed for the RSCPlus client that manages configuration options and keybind values from
 * within an interface.<br>
 * <br>
 *
 * <p>To add a new configuration option to the GUI:
 *
 * <ol>
 *   <li>Declare an instance variable to hold the gui element (eg checkbox) and add it to the GUI
 *       from {@link #initialize}. Multiple components belonging to the same setting should be
 *       grouped within a common parent JPanel, or tagged with {@link
 *       SearchUtils#setRelatedSearchComponent} <i>(see existing examples)</i>
 *   <li>If there is a helper method such as {@link #addCheckbox}, use that method to create and
 *       store the element that is returned in the {@link #initialize} method. <i>(see existing
 *       examples)</i>
 *   <li>Configure search-related properties as-needed, such as metadata. See methods in {@link
 *       SearchUtils} for more information.
 *   <li><sup>^</sup>Add an appropriate variable to the {@link Settings} class as a class variable,
 *       <i>and</i> as an assignment in the appropriate restore default method below.
 *   <li>Add an entry in the {@link #executeSynchronizeGuiValues} method that references the
 *       variable, as per the already-existing examples.
 *   <li>Add an entry in the {@link #saveSettings} method referencing the variable, as per the
 *       already-existing examples.
 *   <li><sup>^</sup>Add an entry in the {@link Settings#save(String)} method to save the option to
 *       file.
 *   <li><sup>^</sup>Add an entry in the {@link Settings#definePresets} method to load the option
 *       from file. Settings that should persist between changing presets should be defined using
 *       the defineStaticPreset methods, with sensible defaults.
 *   <li><i>(Optional)</i> If a method needs to be called to adjust settings other than the setting
 *       value itself, add it to the {@link #applySettings} method below.
 * </ol>
 *
 * <p><i>Entries marked with a <sup>^</sup> are steps used to add settings that are not included in
 * the GUI.</i><br>
 * <br>
 *
 * <p><b>To add a new keybind:</b>
 *
 * <ol>
 *   <li>Add a call in the {@link #initialize} method to {@link #addKeybindSet} with appropriate
 *       parameters.
 *   <li>Add an entry to the command switch statement in {@link Settings#processKeybindCommand} to
 *       process the command when its keybind is pressed.
 *   <li><i>(Optional, recommended)</i>: Separate the command from its functionality by making a
 *       "toggleBlah" method and calling it from the switch statement.
 * </ol>
 */
public class ConfigWindow {

  private JFrame frame;

  ClickListener clickListener = new ClickListener();
  RebindListener rebindListener = new RebindListener();

  ButtonFocusListener focusListener = new ButtonFocusListener();
  JTabbedPane tabbedPane;

  // Search-related components
  private JTextField searchTextField;
  private JButton goToSearchButton;
  private JButton clearSearchButton;

  // Tooltip-related components
  private final AWTEventListener eventQueueListener;
  private final String toolTipInitText =
      "Click here to display additional information about settings";
  private boolean isListeningForEventQueue = false;
  private JPanel toolTipPanel;
  private JLabel toolTipTextLabel;
  private String toolTipTextString;

  /*
   * JComponent variables which hold configuration data
   */

  //// General tab
  private JCheckBox generalPanelClientSizeCheckbox;
  private JSpinner generalPanelClientSizeXSpinner;
  private JSpinner generalPanelClientSizeYSpinner;
  private SpinnerNumberModel spinnerWinXModel;
  private SpinnerNumberModel spinnerWinYModel;
  private JCheckBox generalPanelScaleWindowCheckbox;
  private JRadioButton generalPanelIntegerScalingFocusButton;
  private JSpinner generalPanelIntegerScalingSpinner;
  private JRadioButton generalPanelBilinearScalingFocusButton;
  private JSpinner generalPanelBilinearScalingSpinner;
  private JRadioButton generalPanelBicubicScalingFocusButton;
  private JSpinner generalPanelBicubicScalingSpinner;
  private JCheckBox generalPanelCheckUpdates;
  private JCheckBox generalPanelAccountSecurityCheckbox;
  private JCheckBox generalPanelConfirmCancelRecoveryChangeCheckbox;
  private JCheckBox generalPanelShowSecurityTipsAtLoginCheckbox;
  private JCheckBox generalPanelWelcomeEnabled;
  private JCheckBox generalPanelChatHistoryCheckbox;
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
  private JCheckBox generalPanelDisableNatureRuneAlchCheckbox;
  private JCheckBox generalPanelCommandPatchQuestCheckbox;
  private JCheckBox generalPanelCommandPatchEdibleRaresCheckbox;
  private JCheckBox generalPanelCommandPatchDiskOfReturningCheckbox;
  private JCheckBox generalPanelBypassAttackCheckbox;
  private JCheckBox generalPanelNumberedDialogueOptionsCheckbox;
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
  private JCheckBox generalPanelCtrlScrollChatCheckbox;
  private JCheckBox generalPanelShiftScrollCameraRotationCheckbox;
  private JSlider generalPanelTrackpadRotationSlider;
  private JCheckBox generalPanelCustomRandomChatColourCheckbox;
  private JRadioButton generalPanelRanEntirelyDisableButton;
  private JRadioButton generalPanelRanReduceFrequencyButton;
  private JRadioButton generalPanelVanillaRanHiddenButton;
  private JRadioButton generalPanelRanRS2EffectButton;
  private JComboBox generalPanelRS2ChatEffectComboBox;
  private JRadioButton generalPanelRanSelectColourButton;
  private JPanel generalPanelRanStaticColourSubpanel;
  private JRadioButton generalPanelRanRGBRotationButton;
  private Color ranStaticColour =
      Util.intToColor(Settings.CUSTOM_RAN_STATIC_COLOUR.get(Settings.currentProfile));
  private JSlider generalPanelViewDistanceSlider;
  private JCheckBox generalPanelLimitFPSCheckbox;
  private JSpinner generalPanelLimitFPSSpinner;
  private JSpinner generalPanelLimitRanFPSSpinner;
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
  private JCheckBox generalPanelUseDarkModeCheckbox;
  private JCheckBox generalPanelUseNimbusThemeCheckbox;

  //// Overlays tab
  private JCheckBox overlayPanelStatusDisplayCheckbox;
  private JCheckBox overlayPanelStatusAlwaysTextCheckbox;
  private JCheckBox overlayPanelBuffsCheckbox;
  private JCheckBox overlayPanelKeptItemsCheckbox;
  private JCheckBox overlayPanelKeptItemsWildCheckbox;
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
  private JCheckBox overlayPanelItemNamesHighlightedOnlyCheckbox;
  private JCheckBox overlayPanelPlayerNamesCheckbox;
  private JCheckBox overlayPanelPvpNamesCheckbox;
  private JPanel overlayPanelPvpNamesColourSubpanel;
  private Color pvpNamesColour =
      Util.intToColor(Settings.PVP_NAMES_COLOUR.get(Settings.currentProfile));
  private JCheckBox overlayPanelOwnNameCheckbox;
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
  private JPanel overlayPanelItemHighlightColourSubpanel;
  private Color itemHighlightColour =
      Util.intToColor(Settings.ITEM_HIGHLIGHT_COLOUR.get(Settings.currentProfile));
  private JCheckBox overlayPanelHighlightRightClickCheckbox;
  //   private JRadioButton overlayPanelFontStyleJagexFocusButton;
  private JRadioButton overlayPanelFontStyleJagexBorderedFocusButton;
  private JRadioButton overlayPanelFontStyleLegacyFocusButton;

  //// Audio tab
  private JCheckBox audioPanelEnableMusicCheckbox;
  private JSlider audioPanelSfxVolumeSlider;
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
  private JLabel presetsPanelCurrentPresetLabel;
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

  // Search index caching and processing
  private HashMap<ConfigTab, List<SearchItem>> searchItemsMap;
  private List<Component> allSearchComponents;
  private int searchInitiatedTabIndex = -1;
  private boolean executedGoToSearch = false;
  private boolean reindexing = false;

  /** Defines all {@link ConfigWindow} tabs */
  protected enum ConfigTab {
    PRESETS("Presets"),
    GENERAL("General"),
    OVERLAYS("Overlays"),
    AUDIO("Audio"),
    BANK("Bank"),
    NOTIFICATIONS("Notifications"),
    STREAMING_PRIVACY("Streaming & Privacy"),
    KEYBINDS("Keybinds"),
    REPLAY("Replay"),
    WORLD_LIST("World List"),
    JOYSTICK("Joystick"),
    AUTHORS("Authors");

    public final String label;

    ConfigTab(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }
  }

  public ConfigWindow() {
    Util.setUITheme();
    eventQueueListener = createConfigWindowEventQueueListener();
    initialize();
  }

  public void showConfigWindow() {
    Client.displayMessage("Showing config window...", Client.CHAT_NONE);

    // Clear the search
    setSearchText("");

    this.synchronizeGuiValues();
    frame.setVisible(true);
    frame.toFront();
    frame.requestFocus();
    searchTextField.requestFocusInWindow();
  }

  public void hideConfigWindow() {
    Client.displayMessage("Hid the config window.", Client.CHAT_NONE);

    resetToolTipListener();

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
    frame.setBounds(osScaleDiv(100), osScaleDiv(100), osScaleMul(800), osScaleMul(650));
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout(0, 0));
    URL iconURL = Launcher.getResource("/assets/icon.png");
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      frame.setIconImage(icon.getImage());
    }
    frame.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            resetToolTipListener();
            super.windowClosed(e);
          }
        });

    // Container declarations

    /** The JPanel containing the search components */
    JPanel searchPanel = new JPanel();
    if (Util.isDarkThemeFlatLAF()) {
      searchPanel.setBackground(new Color(60, 63, 65));
    } else if (Util.isLightThemeFlatLAF()) {
      searchPanel.setBackground(new Color(225, 225, 225));
    }

    /** The tabbed pane holding the five configuration tabs */
    tabbedPane = new JTabbedPane();
    if (Util.isUsingFlatLAFTheme()) {
      tabbedPane.putClientProperty("JTabbedPane.tabType", "card");
    }
    tabbedPane.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (isListeningForEventQueue) {
              toolTipTextString = "Waiting for mouse hover...";
            } else {
              toolTipTextString = toolTipInitText;
            }
            toolTipTextLabel.setText(toolTipTextString);
          }
        });

    /* The JPanel containing the tooltip text components */
    toolTipPanel = new JPanel();
    resetToolTipBarPanelColors();

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

    if (Util.isUsingFlatLAFTheme()) {
      Color navigationPanelBackgroundColor = null;

      if (Util.isDarkThemeFlatLAF()) {
        navigationPanelBackgroundColor = new Color(60, 63, 65);
      } else if (Util.isLightThemeFlatLAF()) {
        navigationPanelBackgroundColor = new Color(225, 225, 225);
      }

      navigationPanel.setBackground(navigationPanelBackgroundColor);

      Color scrollPaneBorderColor = null;

      if (Util.isDarkThemeFlatLAF()) {
        scrollPaneBorderColor = new Color(82, 86, 87);
      } else if (Util.isLightThemeFlatLAF()) {
        scrollPaneBorderColor = new Color(194, 194, 194);
      }

      MatteBorder scrollPaneBorder =
          BorderFactory.createMatteBorder(
              0, osScaleMul(1), osScaleMul(1), osScaleMul(1), scrollPaneBorderColor);

      presetsScrollPane.setBorder(scrollPaneBorder);
      generalScrollPane.setBorder(scrollPaneBorder);
      overlayScrollPane.setBorder(scrollPaneBorder);
      audioScrollPane.setBorder(scrollPaneBorder);
      bankScrollPane.setBorder(scrollPaneBorder);
      notificationScrollPane.setBorder(scrollPaneBorder);
      streamingScrollPane.setBorder(scrollPaneBorder);
      keybindScrollPane.setBorder(scrollPaneBorder);
      replayScrollPane.setBorder(scrollPaneBorder);
      worldListScrollPane.setBorder(scrollPaneBorder);
      authorsScrollPane.setBorder(scrollPaneBorder);
      joystickScrollPane.setBorder(scrollPaneBorder);
    }

    JPanel presetsPanel = new JPanel();
    presetsPanel.setName(ConfigTab.PRESETS.name());
    JPanel generalPanel = new JPanel();
    generalPanel.setName(ConfigTab.GENERAL.name());
    JPanel overlayPanel = new JPanel();
    overlayPanel.setName(ConfigTab.OVERLAYS.name());
    JPanel audioPanel = new JPanel();
    audioPanel.setName(ConfigTab.AUDIO.name());
    JPanel bankPanel = new JPanel();
    bankPanel.setName(ConfigTab.BANK.name());
    JPanel notificationPanel = new JPanel();
    notificationPanel.setName(ConfigTab.NOTIFICATIONS.name());
    JPanel streamingPanel = new JPanel();
    streamingPanel.setName(ConfigTab.STREAMING_PRIVACY.name());
    JPanel keybindPanel = new JPanel();
    keybindPanel.setName(ConfigTab.KEYBINDS.name());
    JPanel replayPanel = new JPanel();
    replayPanel.setName(ConfigTab.REPLAY.name());
    joystickPanel = new JPanel();
    joystickPanel.setName(ConfigTab.JOYSTICK.name());
    worldListPanel = new JPanel();
    worldListPanel.setName(ConfigTab.WORLD_LIST.name());
    JPanel authorsPanel = new JPanel();
    authorsPanel.setName(ConfigTab.AUTHORS.name());

    frame.getContentPane().add(searchPanel, BorderLayout.PAGE_START);
    frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

    JPanel pageEndPanel = new JPanel();
    pageEndPanel.setLayout(new BoxLayout(pageEndPanel, BoxLayout.Y_AXIS));
    pageEndPanel.add(toolTipPanel);
    pageEndPanel.add(navigationPanel);
    frame.getContentPane().add(pageEndPanel, BorderLayout.PAGE_END);

    tabbedPane.addTab(ConfigTab.PRESETS.getLabel(), null, presetsScrollPane, null);
    tabbedPane.addTab(ConfigTab.GENERAL.getLabel(), null, generalScrollPane, null);
    tabbedPane.addTab(ConfigTab.OVERLAYS.getLabel(), null, overlayScrollPane, null);
    tabbedPane.addTab(ConfigTab.AUDIO.getLabel(), null, audioScrollPane, null);
    tabbedPane.addTab(ConfigTab.BANK.getLabel(), null, bankScrollPane, null);
    tabbedPane.addTab(ConfigTab.NOTIFICATIONS.getLabel(), null, notificationScrollPane, null);
    tabbedPane.addTab(ConfigTab.STREAMING_PRIVACY.getLabel(), null, streamingScrollPane, null);
    tabbedPane.addTab(ConfigTab.KEYBINDS.getLabel(), null, keybindScrollPane, null);
    tabbedPane.addTab(ConfigTab.REPLAY.getLabel(), null, replayScrollPane, null);
    tabbedPane.addTab(ConfigTab.WORLD_LIST.getLabel(), null, worldListScrollPane, null);
    tabbedPane.addTab(ConfigTab.JOYSTICK.getLabel(), null, joystickScrollPane, null);
    tabbedPane.addTab(ConfigTab.AUTHORS.getLabel(), null, authorsScrollPane, null);

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
    int border10 = osScaleMul(10);
    searchPanel.setBorder(
        BorderFactory.createEmptyBorder(border10, border10, osScaleMul(7), border10));
    if (Util.isUsingFlatLAFTheme()) {
      Color borderColor = isDarkThemeFlatLAF() ? new Color(82, 86, 87) : new Color(194, 194, 194);
      toolTipPanel.setBorder(
          BorderFactory.createMatteBorder(
              0, osScaleMul(1), osScaleMul(1), osScaleMul(1), borderColor));
    } else {
      toolTipPanel.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createEmptyBorder(0, osScaleMul(2), 0, osScaleMul(2)),
              BorderFactory.createLineBorder(new Color(146, 151, 161))));
    }
    navigationPanel.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), border10, border10, border10));
    presetsPanel.setBorder(BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    generalPanel.setBorder(BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    overlayPanel.setBorder(BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    audioPanel.setBorder(BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    bankPanel.setBorder(BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    notificationPanel.setBorder(
        BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    streamingPanel.setBorder(
        BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    keybindPanel.setBorder(BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    replayPanel.setBorder(BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    worldListPanel.setBorder(
        BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    authorsPanel.setBorder(BorderFactory.createEmptyBorder(border10, border10, border10, border10));
    joystickPanel.setBorder(
        BorderFactory.createEmptyBorder(border10, border10, border10, border10));

    int verticalSpeed = osScaleMul(20);
    int horizontalSpeed = osScaleMul(15);

    setScrollSpeed(presetsScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(generalScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(overlayScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(bankScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(audioScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(notificationScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(streamingScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(keybindScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(replayScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(worldListScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(authorsScrollPane, verticalSpeed, horizontalSpeed);
    setScrollSpeed(joystickScrollPane, verticalSpeed, horizontalSpeed);

    /*
     * Search components
     */
    searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));

    JLabel searchTitleLabel = new JLabel("Search settings: ");
    searchPanel.add(searchTitleLabel);
    searchTitleLabel.setAlignmentY(Util.isUsingFlatLAFTheme() ? 0.9f : 1.0f);

    searchTextField = new JTextField();
    if (Util.isDarkThemeFlatLAF()) {
      searchTextField.setBackground(new Color(69, 73, 75));
    }
    searchPanel.add(searchTextField);
    searchTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    searchTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    searchTextField.setAlignmentY(0.75f);
    searchTextField.setToolTipText(
        "Press ENTER to go to the first search result or ESCAPE to reset the search");

    searchTextField.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              goToSearchResult();
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
              setSearchText("");
            }
          }
        });

    if (Util.isUsingFlatLAFTheme()) {
      searchPanel.add(Box.createRigidArea(osScaleMul(new Dimension(4, 0))));
    }

    goToSearchButton = new JButton("Go To");
    if (Util.isDarkThemeFlatLAF()) {
      goToSearchButton.setBackground(new Color(42, 46, 48));
    }
    searchPanel.add(goToSearchButton);
    goToSearchButton.setToolTipText("Navigates to the first search result");
    goToSearchButton.setAlignmentY(0.75f);
    goToSearchButton.setFocusable(false);
    goToSearchButton.addActionListener(actionEvent -> goToSearchResult());

    if (Util.isUsingFlatLAFTheme()) {
      searchPanel.add(Box.createRigidArea(osScaleMul(new Dimension(4, 0))));
    }

    clearSearchButton = new JButton("Clear");
    if (Util.isDarkThemeFlatLAF()) {
      clearSearchButton.setBackground(new Color(42, 46, 48));
    }
    searchPanel.add(clearSearchButton);
    clearSearchButton.setToolTipText("Resets the current search");
    clearSearchButton.setAlignmentY(0.75f);
    clearSearchButton.setFocusable(false);
    clearSearchButton.addActionListener(actionEvent -> setSearchText(""));

    searchTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void changedUpdate(DocumentEvent e) {
                performSearch();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                performSearch();
              }

              @Override
              public void insertUpdate(DocumentEvent e) {
                performSearch();
              }

              private void performSearch() {
                searchComponents();
              }
            });

    /*
     Tooltip panel
    */
    toolTipPanel.setLayout(new BoxLayout(toolTipPanel, BoxLayout.X_AXIS));
    toolTipTextLabel = new JLabel(toolTipInitText);
    toolTipTextLabel.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(2), border10, osScaleMul(2), border10));
    toolTipTextLabel.setText(toolTipInitText);
    toolTipTextLabel.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    toolTipTextLabel.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    toolTipTextLabel.setAlignmentY(0.75f);
    toolTipPanel.add(toolTipTextLabel);

    toolTipPanel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (isListeningForEventQueue) {
              resetToolTipBarPanelColors();
              toolTipTextLabel.setText(toolTipInitText);
              removeConfigWindowEventQueueListener();
            } else {
              // Uses theme tooltip colors
              if (Util.isDarkThemeFlatLAF()) {
                toolTipPanel.setBackground(new Color(21, 23, 24));
              } else if (Util.isLightThemeFlatLAF()) {
                toolTipPanel.setBackground(new Color(250, 250, 250));
              } else {
                toolTipPanel.setBackground(new Color(242, 242, 189));
              }
              toolTipTextString = "Waiting for mouse hover...";
              toolTipTextLabel.setText(toolTipTextString);
              addConfigWindowEventQueueListener();
            }
          }
        });

    /*
     * Navigation buttons
     */

    navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.X_AXIS));

    JButton okButton = addButton("OK", navigationPanel, Component.LEFT_ALIGNMENT);

    if (Util.isDarkThemeFlatLAF()) {
      okButton.setBackground(new Color(42, 46, 48));
    }

    okButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Launcher.getConfigWindow().applySettings();
            setInitiatedTab(tabbedPane.getSelectedIndex());
            Launcher.getConfigWindow().hideConfigWindow();
          }
        });

    if (Util.isUsingFlatLAFTheme()) {
      navigationPanel.add(Box.createRigidArea(osScaleMul(new Dimension(4, 0))));
    }

    JButton cancelButton = addButton("Cancel", navigationPanel, Component.LEFT_ALIGNMENT);

    if (Util.isDarkThemeFlatLAF()) {
      cancelButton.setBackground(new Color(42, 46, 48));
    }

    cancelButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Launcher.getConfigWindow().hideConfigWindow();
          }
        });

    if (Util.isUsingFlatLAFTheme()) {
      navigationPanel.add(Box.createRigidArea(osScaleMul(new Dimension(4, 0))));
    }

    JButton applyButton = addButton("Apply", navigationPanel, Component.LEFT_ALIGNMENT);

    if (Util.isDarkThemeFlatLAF()) {
      applyButton.setBackground(new Color(42, 46, 48));
    }

    applyButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Launcher.getConfigWindow().applySettings();
            setInitiatedTab(tabbedPane.getSelectedIndex());
          }
        });

    navigationPanel.add(Box.createHorizontalGlue());

    JButton restoreDefaultsButton =
        addButton("Restore Defaults", navigationPanel, Component.RIGHT_ALIGNMENT);

    if (Util.isDarkThemeFlatLAF()) {
      restoreDefaultsButton.setBackground(new Color(42, 46, 48));
    }

    restoreDefaultsButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            JPanel confirmDefaultPanel =
                Util.createOptionMessagePanel(
                    "Are you sure you want to restore all settings to their defaults?<br/>"
                        + "<br/>"
                        + "You will need to restart the client after this has been completed.");
            int choice =
                JOptionPane.showConfirmDialog(
                    Launcher.getConfigWindow().frame,
                    confirmDefaultPanel,
                    "Confirm",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.CLOSED_OPTION || choice == JOptionPane.NO_OPTION) {
              return;
            }

            try {
              File configFile = new File(Settings.Dir.JAR + "/config.ini");
              if (configFile.exists()) {
                Files.delete(configFile.toPath());
              }
            } catch (Exception ex) {
              String restoreDefaultsFailureMessage =
                  "An error occurred while trying to restore the default settings.<br/>";
              JPanel restoreDefaultsFailurePanel =
                  Util.createOptionMessagePanel(restoreDefaultsFailureMessage);

              JOptionPane.showMessageDialog(
                  Launcher.getConfigWindow().frame,
                  restoreDefaultsFailurePanel,
                  "RSCPlus",
                  JOptionPane.ERROR_MESSAGE,
                  Launcher.scaled_icon_warn);

              return;
            }

            String restoreDefaultsSuccessMessage =
                "Default settings have been restored.<br/>"
                    + "<br/>"
                    + "The client needs to be restarted and will now shut down.";
            JPanel restoreDefaultsSuccessPanel =
                Util.createOptionMessagePanel(restoreDefaultsSuccessMessage);

            JOptionPane.showMessageDialog(
                Launcher.getConfigWindow().frame,
                restoreDefaultsSuccessPanel,
                "RSCPlus",
                JOptionPane.INFORMATION_MESSAGE,
                Launcher.scaled_option_icon);
            System.exit(0);

            // Restore defaults
            /* TODO: reimplement per-tab defaults? Will need to consider search re-indexing
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
    generalPanelClientSizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // TODO: Perhaps change to "Save client size on close"?
    generalPanelClientSizeCheckbox =
        addCheckbox("Client window dimensions:", generalPanelClientSizePanel);
    generalPanelClientSizeCheckbox.setToolTipText("Set the client size to the supplied dimensions");

    generalPanelClientSizeXSpinner = new JSpinner();
    generalPanelClientSizePanel.add(generalPanelClientSizeXSpinner);
    generalPanelClientSizeXSpinner.setMaximumSize(osScaleMul(new Dimension(70, 23)));
    generalPanelClientSizeXSpinner.setMinimumSize(osScaleMul(new Dimension(70, 23)));
    generalPanelClientSizeXSpinner.setAlignmentY(0.7f);
    generalPanelClientSizeXSpinner.setToolTipText("Default client width (512 minimum at 1x scale)");
    generalPanelClientSizeXSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    JLabel generalPanelClientSizeByLabel = new JLabel("x");
    generalPanelClientSizePanel.add(generalPanelClientSizeByLabel);
    generalPanelClientSizeByLabel.setAlignmentY(0.8f);

    int spinnerByMargin = isUsingFlatLAFTheme() ? 4 : 2;

    generalPanelClientSizeByLabel.setBorder(
        BorderFactory.createEmptyBorder(
            0, osScaleMul(spinnerByMargin), 0, osScaleMul(spinnerByMargin)));

    generalPanelClientSizeYSpinner = new JSpinner();
    generalPanelClientSizePanel.add(generalPanelClientSizeYSpinner);
    generalPanelClientSizeYSpinner.setMaximumSize(osScaleMul(new Dimension(70, 23)));
    generalPanelClientSizeYSpinner.setMinimumSize(osScaleMul(new Dimension(70, 23)));
    generalPanelClientSizeYSpinner.setAlignmentY(0.7f);
    generalPanelClientSizeYSpinner.setToolTipText(
        "Default client height (346 minimum at 1x scale)");
    generalPanelClientSizeYSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    // Sanitize JSpinner values
    spinnerWinXModel = new SpinnerNumberModel();
    spinnerWinXModel.setMinimum(512);
    spinnerWinXModel.setValue(512);
    spinnerWinXModel.setStepSize(10);
    generalPanelClientSizeXSpinner.setModel(spinnerWinXModel);
    spinnerWinYModel = new SpinnerNumberModel();
    spinnerWinYModel.setMinimum(346);
    spinnerWinYModel.setValue(346);
    spinnerWinYModel.setStepSize(10);
    generalPanelClientSizeYSpinner.setModel(spinnerWinYModel);

    if (Util.isUsingFlatLAFTheme()) {
      generalPanelClientSizePanel.add(Box.createRigidArea(osScaleMul(new Dimension(6, 0))));
    }

    JButton generalPanelClientSizeMaxButton =
        addButton("Max", generalPanelClientSizePanel, Component.RIGHT_ALIGNMENT);
    generalPanelClientSizeMaxButton.setAlignmentY(0.7f);
    generalPanelClientSizeMaxButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Dimension maximumWindowSize =
                ScaledWindow.getInstance().getMaximumEffectiveWindowSize();

            int windowWidth = maximumWindowSize.width;
            int windowHeight = maximumWindowSize.height;

            // This only changes the values in the boxes
            spinnerWinXModel.setValue(windowWidth);
            spinnerWinYModel.setValue(windowHeight);
          }
        });

    if (Util.isUsingFlatLAFTheme()) {
      generalPanelClientSizePanel.add(Box.createRigidArea(osScaleMul(new Dimension(6, 0))));
    }

    JButton generalPanelClientSizeResetButton =
        addButton("Reset", generalPanelClientSizePanel, Component.RIGHT_ALIGNMENT);
    generalPanelClientSizeResetButton.setAlignmentY(0.7f);
    generalPanelClientSizeResetButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            // This only changes the values in the boxes
            Dimension scaledMinimumWindowSize =
                ScaledWindow.getInstance().getMinimumViewportSizeForScalar();
            spinnerWinXModel.setValue(scaledMinimumWindowSize.width);
            spinnerWinYModel.setValue(scaledMinimumWindowSize.height);
          }
        });

    JLabel generalPanelClientSizeScaleWarning =
        new JLabel("(Will be reset if window scale changes)");
    generalPanelClientSizeScaleWarning.setAlignmentY(0.8f);
    generalPanelClientSizeScaleWarning.setBorder(
        BorderFactory.createEmptyBorder(0, osScaleMul(6), 0, 0));
    generalPanelClientSizePanel.add(generalPanelClientSizeScaleWarning);

    // Scaling options
    JPanel generalPanelScalePanel = new JPanel();
    generalPanel.add(generalPanelScalePanel);
    generalPanelScalePanel.setLayout(new BoxLayout(generalPanelScalePanel, BoxLayout.Y_AXIS));
    generalPanelScalePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelScaleWindowCheckbox = addCheckbox("Scale window:", generalPanelScalePanel);
    generalPanelScaleWindowCheckbox.setToolTipText("Enable to scale the game client");
    generalPanelScaleWindowCheckbox.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(5), 0));

    ButtonGroup generalPanelScaleWindowTypeButtonGroup = new ButtonGroup();
    String scaleLargerThanResolutionToolTip =
        "This scale value will produce a window bigger than your screen resolution";

    // Integer scaling
    JPanel generalPanelIntegerScalingPanel = new JPanel();
    generalPanelScalePanel.add(generalPanelIntegerScalingPanel);
    generalPanelIntegerScalingPanel.setLayout(
        new BoxLayout(generalPanelIntegerScalingPanel, BoxLayout.X_AXIS));
    generalPanelIntegerScalingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelIntegerScalingFocusButton =
        addRadioButton("Integer scaling", generalPanelIntegerScalingPanel, osScaleMul(20));
    generalPanelIntegerScalingFocusButton.setToolTipText(
        "Uses the nearest neighbor algorithm for pixel-perfect client scaling");
    generalPanelScaleWindowTypeButtonGroup.add(generalPanelIntegerScalingFocusButton);

    generalPanelIntegerScalingSpinner = new JSpinner();
    generalPanelIntegerScalingPanel.add(generalPanelIntegerScalingSpinner);
    String integerScalingSpinnerToolTip =
        "Integer scaling value " + (int) Renderer.minScalar + "-" + (int) Renderer.maxIntegerScalar;
    generalPanelIntegerScalingSpinner.setMaximumSize(osScaleMul(new Dimension(55, 26)));
    generalPanelIntegerScalingSpinner.setMinimumSize(osScaleMul(new Dimension(55, 26)));
    generalPanelIntegerScalingSpinner.setAlignmentY(0.625f);
    generalPanelIntegerScalingSpinner.setToolTipText(integerScalingSpinnerToolTip);
    generalPanelIntegerScalingSpinner.putClientProperty("JComponent.sizeVariant", "mini");
    if (Util.isUsingFlatLAFTheme()) {
      generalPanelIntegerScalingSpinner.setBorder(new FlatRoundBorder());
    } else {
      generalPanelIntegerScalingSpinner.setBorder(
          BorderFactory.createEmptyBorder(
              osScaleMul(2), osScaleMul(2), osScaleMul(2), osScaleMul(2)));
    }
    generalPanelIntegerScalingSpinner.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            Dimension maximumWindowSize = ScaledWindow.getInstance().getMaximumWindowSize();
            int scalar = (int) generalPanelIntegerScalingSpinner.getValue();

            if (((512 * scalar) + ScaledWindow.getInstance().getWindowWidthInsets()
                    > maximumWindowSize.getWidth())
                || ((346 * scalar) + ScaledWindow.getInstance().getWindowHeightInsets()
                    > maximumWindowSize.getHeight())) {
              generalPanelIntegerScalingSpinner.setBorder(
                  new LineBorder(Color.orange, osScaleMul(2)));
              generalPanelIntegerScalingSpinner.setToolTipText(scaleLargerThanResolutionToolTip);
            } else {
              if (Util.isUsingFlatLAFTheme()) {
                generalPanelIntegerScalingSpinner.setBorder(new FlatRoundBorder());
              } else {
                generalPanelIntegerScalingSpinner.setBorder(
                    BorderFactory.createEmptyBorder(
                        osScaleMul(2), osScaleMul(2), osScaleMul(2), osScaleMul(2)));
              }
              generalPanelIntegerScalingSpinner.setToolTipText(integerScalingSpinnerToolTip);
            }
          }
        });

    SpinnerNumberModel spinnerLimitIntegerScaling =
        new SpinnerNumberModel(2, (int) Renderer.minScalar, (int) Renderer.maxIntegerScalar, 1);
    generalPanelIntegerScalingSpinner.setModel(spinnerLimitIntegerScaling);

    // Bilinear scaling
    JPanel generalPanelBilinearScalingPanel = new JPanel();
    generalPanelScalePanel.add(generalPanelBilinearScalingPanel);
    generalPanelBilinearScalingPanel.setLayout(
        new BoxLayout(generalPanelBilinearScalingPanel, BoxLayout.X_AXIS));
    generalPanelBilinearScalingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelBilinearScalingFocusButton =
        addRadioButton("Bilinear interpolation", generalPanelBilinearScalingPanel, osScaleMul(20));
    generalPanelBilinearScalingFocusButton.setToolTipText(
        "Uses the bilinear interpolation algorithm for client scaling");
    generalPanelScaleWindowTypeButtonGroup.add(generalPanelBilinearScalingFocusButton);

    generalPanelBilinearScalingSpinner = new JSpinner();
    generalPanelBilinearScalingPanel.add(generalPanelBilinearScalingSpinner);
    String bilinearScalingSpinnerToolTip =
        "Bilinear scaling value " + Renderer.minScalar + "-" + Renderer.maxInterpolationScalar;
    generalPanelBilinearScalingSpinner.setMaximumSize(osScaleMul(new Dimension(55, 26)));
    generalPanelBilinearScalingSpinner.setMinimumSize(osScaleMul(new Dimension(55, 26)));
    generalPanelBilinearScalingSpinner.setAlignmentY(0.625f);
    generalPanelBilinearScalingSpinner.setToolTipText(bilinearScalingSpinnerToolTip);
    generalPanelBilinearScalingSpinner.putClientProperty("JComponent.sizeVariant", "mini");
    if (Util.isUsingFlatLAFTheme()) {
      generalPanelBilinearScalingSpinner.setBorder(new FlatRoundBorder());
    } else {
      generalPanelBilinearScalingSpinner.setBorder(
          BorderFactory.createEmptyBorder(
              osScaleMul(2), osScaleMul(2), osScaleMul(2), osScaleMul(2)));
    }
    generalPanelBilinearScalingSpinner.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            Dimension maximumWindowSize = ScaledWindow.getInstance().getMaximumWindowSize();
            float scalar = (float) generalPanelBilinearScalingSpinner.getValue();

            if (((512 * scalar) + ScaledWindow.getInstance().getWindowWidthInsets()
                    > maximumWindowSize.getWidth())
                || ((346 * scalar) + ScaledWindow.getInstance().getWindowHeightInsets()
                    > maximumWindowSize.getHeight())) {
              generalPanelBilinearScalingSpinner.setBorder(
                  new LineBorder(Color.orange, osScaleMul(2)));
              generalPanelBilinearScalingSpinner.setToolTipText(scaleLargerThanResolutionToolTip);
            } else {
              if (Util.isUsingFlatLAFTheme()) {
                generalPanelBilinearScalingSpinner.setBorder(new FlatRoundBorder());
              } else {
                generalPanelBilinearScalingSpinner.setBorder(
                    BorderFactory.createEmptyBorder(
                        osScaleMul(2), osScaleMul(2), osScaleMul(2), osScaleMul(2)));
              }
              generalPanelBilinearScalingSpinner.setToolTipText(bilinearScalingSpinnerToolTip);
            }
          }
        });

    SpinnerNumberModel spinnerLimitBilinearScaling =
        new SpinnerNumberModel(
            new Float(1.5f),
            new Float(Renderer.minScalar),
            new Float(Renderer.maxInterpolationScalar),
            new Float(0.1f));
    generalPanelBilinearScalingSpinner.setModel(spinnerLimitBilinearScaling);

    JLabel bilinearInterpolationScalingWarning =
        new JLabel("(May affect performance at high scaling values)");
    bilinearInterpolationScalingWarning.setAlignmentY(0.75f);
    int bilinearInterpolationScalingMargin = isUsingFlatLAFTheme() ? 6 : 2;
    bilinearInterpolationScalingWarning.setBorder(
        BorderFactory.createEmptyBorder(0, osScaleMul(bilinearInterpolationScalingMargin), 0, 0));
    generalPanelBilinearScalingPanel.add(bilinearInterpolationScalingWarning);

    // Bicubic scaling
    JPanel generalPanelBicubicScalingPanel = new JPanel();
    generalPanelScalePanel.add(generalPanelBicubicScalingPanel);
    generalPanelBicubicScalingPanel.setLayout(
        new BoxLayout(generalPanelBicubicScalingPanel, BoxLayout.X_AXIS));
    generalPanelBicubicScalingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelBicubicScalingFocusButton =
        addRadioButton("Bicubic interpolation", generalPanelBicubicScalingPanel, osScaleMul(20));
    generalPanelBicubicScalingFocusButton.setToolTipText(
        "Uses the bicubic interpolation algorithm for client scaling");
    generalPanelScaleWindowTypeButtonGroup.add(generalPanelBicubicScalingFocusButton);

    generalPanelBicubicScalingSpinner = new JSpinner();
    generalPanelBicubicScalingPanel.add(generalPanelBicubicScalingSpinner);
    String bicubicScalingSpinnerToolTip =
        "Bicubic scaling value " + Renderer.minScalar + "-" + Renderer.maxInterpolationScalar;
    generalPanelBicubicScalingSpinner.setMaximumSize(osScaleMul(new Dimension(55, 26)));
    generalPanelBicubicScalingSpinner.setMinimumSize(osScaleMul(new Dimension(55, 26)));
    generalPanelBicubicScalingSpinner.setAlignmentY(0.625f);
    generalPanelBicubicScalingSpinner.setToolTipText(bicubicScalingSpinnerToolTip);
    generalPanelBicubicScalingSpinner.putClientProperty("JComponent.sizeVariant", "mini");
    if (Util.isUsingFlatLAFTheme()) {
      generalPanelBicubicScalingSpinner.setBorder(new FlatRoundBorder());
    } else {
      generalPanelBicubicScalingSpinner.setBorder(
          BorderFactory.createEmptyBorder(
              osScaleMul(2), osScaleMul(2), osScaleMul(2), osScaleMul(2)));
    }
    generalPanelBicubicScalingSpinner.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            Dimension maximumWindowSize = ScaledWindow.getInstance().getMaximumWindowSize();
            float scalar = (float) generalPanelBicubicScalingSpinner.getValue();

            if (((512 * scalar) + ScaledWindow.getInstance().getWindowWidthInsets()
                    > maximumWindowSize.getWidth())
                || ((346 * scalar) + ScaledWindow.getInstance().getWindowHeightInsets()
                    > maximumWindowSize.getHeight())) {
              generalPanelBicubicScalingSpinner.setBorder(new LineBorder(Color.orange, 2));
              generalPanelBicubicScalingSpinner.setToolTipText(scaleLargerThanResolutionToolTip);
            } else {
              if (Util.isUsingFlatLAFTheme()) {
                generalPanelBicubicScalingSpinner.setBorder(new FlatRoundBorder());
              } else {
                generalPanelBicubicScalingSpinner.setBorder(
                    BorderFactory.createEmptyBorder(
                        osScaleMul(2), osScaleMul(2), osScaleMul(2), osScaleMul(2)));
              }
              generalPanelBicubicScalingSpinner.setToolTipText(bicubicScalingSpinnerToolTip);
            }
          }
        });

    SpinnerNumberModel spinnerLimitBicubicScaling =
        new SpinnerNumberModel(
            new Float(1.5f),
            new Float(Renderer.minScalar),
            new Float(Renderer.maxInterpolationScalar),
            new Float(0.1f));
    generalPanelBicubicScalingSpinner.setModel(spinnerLimitBicubicScaling);

    JLabel bicubicInterpolationScalingWarning =
        new JLabel("(May affect performance at high scaling values)");
    bicubicInterpolationScalingWarning.setAlignmentY(0.75f);
    int bicubicInterpolationScalingMargin = isUsingFlatLAFTheme() ? 6 : 2;
    bicubicInterpolationScalingWarning.setBorder(
        BorderFactory.createEmptyBorder(0, osScaleMul(bicubicInterpolationScalingMargin), 0, 0));
    generalPanelBicubicScalingPanel.add(bicubicInterpolationScalingWarning);

    if (isUsingFlatLAFTheme()) {
      generalPanelScalePanel.add(Box.createRigidArea(osScaleMul(new Dimension(0, 5))));
    }
    // End scaling options

    generalPanelCheckUpdates =
        addCheckbox("Check for RSCPlus updates from GitHub at launch", generalPanel);
    generalPanelCheckUpdates.setToolTipText(
        "When enabled, RSCPlus will check for client updates before launching the game and install them when prompted");

    generalPanelWelcomeEnabled =
        addCheckbox(
            "<html><head><style>span{color:red;}</style></head>Remind you how to open the Settings every time you log in <span>(!!! Disable this if you know how to open the settings)</span></html>",
            generalPanel);
    generalPanelWelcomeEnabled.setToolTipText(
        "When enabled, RSCPlus will insert a message telling the current keybinding to open the settings menu and remind you about the tray icon");

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

    generalPanelCtrlScrollChatCheckbox =
        addCheckbox("Hold ctrl to scroll through chat history from anywhere", generalPanel);
    generalPanelCtrlScrollChatCheckbox.setToolTipText(
        "Holding CTRL allows you to scroll through the currently-selected chat history from anywhere");

    generalPanelShiftScrollCameraRotationCheckbox =
        addCheckbox("Enable camera rotation with compatible trackpads", generalPanel);
    generalPanelShiftScrollCameraRotationCheckbox.setToolTipText(
        "Trackpads that send SHIFT-SCROLL WHEEL when swiping left or right with two fingers will be able to rotate the camera");

    JPanel generalPanelTrackPadRotationPanel = new JPanel();
    generalPanel.add(generalPanelTrackPadRotationPanel);
    generalPanelTrackPadRotationPanel.setLayout(
        new BoxLayout(generalPanelTrackPadRotationPanel, BoxLayout.Y_AXIS));
    generalPanelTrackPadRotationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel generalPanelTrackpadRotationLabel = new JLabel("Camera rotation trackpad sensitivity");
    generalPanelTrackpadRotationLabel.setToolTipText(
        "Sets the camera rotation trackpad sensitivity (Default: 8)");
    generalPanelTrackPadRotationPanel.add(generalPanelTrackpadRotationLabel);
    generalPanelTrackpadRotationLabel.setAlignmentY(1.0f);

    if (Util.isUsingFlatLAFTheme()) {
      generalPanelTrackPadRotationPanel.add(Box.createRigidArea(osScaleMul(new Dimension(0, 10))));
    }

    generalPanelTrackpadRotationSlider = new JSlider();

    generalPanelTrackPadRotationPanel.add(generalPanelTrackpadRotationSlider);
    generalPanelTrackpadRotationSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelTrackpadRotationSlider.setMaximumSize(osScaleMul(new Dimension(200, 55)));
    generalPanelTrackpadRotationSlider.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(10), 0));
    generalPanelTrackpadRotationSlider.setMajorTickSpacing(2);
    generalPanelTrackpadRotationSlider.setMinorTickSpacing(1);
    generalPanelTrackpadRotationSlider.setMinimum(0);
    generalPanelTrackpadRotationSlider.setMaximum(16);
    generalPanelTrackpadRotationSlider.setPaintTicks(true);

    Hashtable<Integer, JLabel> generalPanelTrackpadRotationLabelTable =
        new Hashtable<Integer, JLabel>();
    generalPanelTrackpadRotationLabelTable.put(new Integer(0), new JLabel("0"));
    generalPanelTrackpadRotationLabelTable.put(new Integer(4), new JLabel("4"));
    generalPanelTrackpadRotationLabelTable.put(new Integer(8), new JLabel("8"));
    generalPanelTrackpadRotationLabelTable.put(new Integer(12), new JLabel("12"));
    generalPanelTrackpadRotationLabelTable.put(new Integer(16), new JLabel("16"));
    generalPanelTrackpadRotationSlider.setLabelTable(generalPanelTrackpadRotationLabelTable);
    generalPanelTrackpadRotationSlider.setPaintLabels(true);

    generalPanelAutoScreenshotCheckbox =
        addCheckbox("Take a screenshot when you level up or complete a quest", generalPanel);
    generalPanelAutoScreenshotCheckbox.setToolTipText(
        "Takes a screenshot for you for level ups and quest completion");

    generalPanelUseJagexFontsCheckBox =
        addCheckbox("Override system font with Jagex fonts", generalPanel);
    generalPanelUseJagexFontsCheckBox.setToolTipText(
        "Make game fonts appear consistent by loading Jagex font files the same as prior to 2009.");

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

    generalPanelChatHistoryCheckbox =
        addCheckbox("Load chat history after relogging", generalPanel);
    generalPanelChatHistoryCheckbox.setToolTipText("Make chat history persist between logins");

    generalPanelCombatXPMenuCheckbox =
        addCheckbox("Combat style menu shown outside of combat", generalPanel);
    generalPanelCombatXPMenuCheckbox.setToolTipText(
        "Always show the combat style menu when out of combat");

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
    SearchUtils.addSearchMetadata(generalPanelRoofHidingCheckbox, "roofs", "rooves");

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

    addSettingsHeader(generalPanel, "Graphical effect changes");

    JPanel generalPanelViewDistancePanel = new JPanel();
    generalPanel.add(generalPanelViewDistancePanel);
    generalPanelViewDistancePanel.setLayout(
        new BoxLayout(generalPanelViewDistancePanel, BoxLayout.Y_AXIS));
    generalPanelViewDistancePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel generalPanelViewDistanceLabel = new JLabel("View distance (affects the black fog)");
    generalPanelViewDistanceLabel.setToolTipText(
        "Sets the max render distance of structures and landscape");
    generalPanelViewDistancePanel.add(generalPanelViewDistanceLabel);
    generalPanelViewDistanceLabel.setAlignmentY(1.0f);

    if (Util.isUsingFlatLAFTheme()) {
      generalPanelViewDistancePanel.add(Box.createRigidArea(osScaleMul(new Dimension(0, 5))));
    }

    generalPanelViewDistanceSlider = new JSlider();

    generalPanelViewDistancePanel.add(generalPanelViewDistanceSlider);
    generalPanelViewDistanceSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelViewDistanceSlider.setMaximumSize(osScaleMul(new Dimension(350, 55)));
    generalPanelViewDistanceSlider.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(10), 0));
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

    generalPanelRS2HDSkyCheckbox =
        addCheckbox("Use RS2: HD sky colours (overrides custom colours below)", generalPanel);
    generalPanelRS2HDSkyCheckbox.setToolTipText("Uses sky colours from RS2: HD");
    SearchUtils.addSearchMetadata(generalPanelRS2HDSkyCheckbox, CommonMetadata.COLOURS.getText());

    // colour choose overworld sub-panel
    JPanel generalPanelSkyOverworldColourPanel = new JPanel();
    generalPanel.add(generalPanelSkyOverworldColourPanel);
    generalPanelSkyOverworldColourPanel.setLayout(
        new BoxLayout(generalPanelSkyOverworldColourPanel, BoxLayout.X_AXIS));
    generalPanelSkyOverworldColourPanel.setPreferredSize(osScaleMul(new Dimension(0, 30)));
    generalPanelSkyOverworldColourPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelCustomSkyboxOverworldCheckbox =
        addCheckbox("Use a custom colour for the sky", generalPanelSkyOverworldColourPanel);
    generalPanelCustomSkyboxOverworldCheckbox.setToolTipText(
        "You can set your own preferred colour for what you think the sky should have");
    SearchUtils.addSearchMetadata(
        generalPanelCustomSkyboxOverworldCheckbox, CommonMetadata.COLOUR.getText());

    generalPanelSkyOverworldColourColourPanel = new JPanel();
    generalPanelSkyOverworldColourPanel.add(generalPanelSkyOverworldColourColourPanel);
    generalPanelSkyOverworldColourColourPanel.setAlignmentY(0.7f);
    generalPanelSkyOverworldColourColourPanel.setMinimumSize(osScaleMul(new Dimension(32, 20)));
    generalPanelSkyOverworldColourColourPanel.setPreferredSize(osScaleMul(new Dimension(32, 20)));
    generalPanelSkyOverworldColourColourPanel.setMaximumSize(osScaleMul(new Dimension(32, 20)));
    generalPanelSkyOverworldColourColourPanel.setBorder(
        BorderFactory.createLineBorder(Color.black));
    generalPanelSkyOverworldColourColourPanel.setBackground(overworldSkyColour);

    int overworldColourMargin = isUsingFlatLAFTheme() ? 8 : 4;
    generalPanelSkyOverworldColourPanel.add(
        Box.createRigidArea(osScaleMul(new Dimension(overworldColourMargin, 20))));

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
    overworldSkyColourChooserButton.setAlignmentY(0.7f);

    // choose colour for underground subpanel
    JPanel generalPanelSkyUndergroundColourPanel = new JPanel();
    generalPanel.add(generalPanelSkyUndergroundColourPanel);
    generalPanelSkyUndergroundColourPanel.setLayout(
        new BoxLayout(generalPanelSkyUndergroundColourPanel, BoxLayout.X_AXIS));
    generalPanelSkyUndergroundColourPanel.setPreferredSize(osScaleMul(new Dimension(0, 30)));
    generalPanelSkyUndergroundColourPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelCustomSkyboxUndergroundCheckbox =
        addCheckbox(
            "Use a custom colour for the sky when underground",
            generalPanelSkyUndergroundColourPanel);
    generalPanelCustomSkyboxUndergroundCheckbox.setToolTipText(
        "You can set your own preferred colour for what you think the sky should have (underground)");
    SearchUtils.addSearchMetadata(
        generalPanelCustomSkyboxUndergroundCheckbox, CommonMetadata.COLOUR.getText());

    generalPanelSkyUndergroundColourColourPanel = new JPanel();
    generalPanelSkyUndergroundColourPanel.add(generalPanelSkyUndergroundColourColourPanel);
    generalPanelSkyUndergroundColourColourPanel.setAlignmentY(0.7f);
    generalPanelSkyUndergroundColourColourPanel.setMinimumSize(osScaleMul(new Dimension(32, 20)));
    generalPanelSkyUndergroundColourColourPanel.setPreferredSize(osScaleMul(new Dimension(32, 20)));
    generalPanelSkyUndergroundColourColourPanel.setMaximumSize(osScaleMul(new Dimension(32, 20)));
    generalPanelSkyUndergroundColourColourPanel.setBorder(
        BorderFactory.createLineBorder(Color.black));
    generalPanelSkyUndergroundColourColourPanel.setBackground(undergroundSkyColour);

    int undergroundSkyColourMargin = isUsingFlatLAFTheme() ? 8 : 4;
    generalPanelSkyUndergroundColourPanel.add(
        Box.createRigidArea(osScaleMul(new Dimension(undergroundSkyColourMargin, 20))));

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
    undergroundSkyColourChooserButton.setAlignmentY(0.7f);
    /////

    // FOV slider
    JPanel generalPanelFoVPanel = new JPanel();
    generalPanel.add(generalPanelFoVPanel);
    generalPanelFoVPanel.setLayout(new BoxLayout(generalPanelFoVPanel, BoxLayout.Y_AXIS));
    generalPanelFoVPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel generalPanelFoVLabel =
        new JLabel("Field of view (Resets to default on client restart, can be set with ::fov)");
    generalPanelFoVLabel.setToolTipText(
        "Sets the field of view (Default 9, non-default values not recommended for general use)");
    generalPanelFoVPanel.add(generalPanelFoVLabel);
    generalPanelFoVLabel.setAlignmentY(1.0f);

    if (Util.isUsingFlatLAFTheme()) {
      generalPanelFoVPanel.add(Box.createRigidArea(osScaleMul(new Dimension(0, 5))));
    }

    generalPanelFoVSlider = new JSlider();

    generalPanelFoVPanel.add(generalPanelFoVSlider);
    generalPanelFoVSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelFoVSlider.setMaximumSize(osScaleMul(new Dimension(300, 55)));
    generalPanelFoVSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(10), 0));
    generalPanelFoVSlider.setMinimum(7);
    generalPanelFoVSlider.setMaximum(16);
    generalPanelFoVSlider.setMajorTickSpacing(1);
    generalPanelFoVSlider.setPaintTicks(true);
    generalPanelFoVSlider.setPaintLabels(true);
    //////

    generalPanelDisableUndergroundLightingCheckbox =
        addCheckbox("Disable underground lighting flicker", generalPanel);
    generalPanelDisableUndergroundLightingCheckbox.setToolTipText(
        "Underground lighting will no longer flicker");
    // TODO: should introduce lighting flicker interval reduction as an option

    ButtonGroup ranChatEffectButtonGroup = new ButtonGroup();

    JPanel generalPanelCustomRandomPanel = new JPanel();
    generalPanel.add(generalPanelCustomRandomPanel);
    generalPanelCustomRandomPanel.setLayout(
        new BoxLayout(generalPanelCustomRandomPanel, BoxLayout.Y_AXIS));
    generalPanelCustomRandomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelCustomRandomChatColourCheckbox =
        addCheckbox("Use custom \"@ran@\" chat colour effect", generalPanelCustomRandomPanel);
    generalPanelCustomRandomChatColourCheckbox.setToolTipText(
        "The random chat colour effect will be altered per the settings below");
    generalPanelCustomRandomChatColourCheckbox.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));

    // limit ran fps panel
    JPanel generalPanelLimitRanFPSPanel = new JPanel();
    generalPanelCustomRandomPanel.add(generalPanelLimitRanFPSPanel);
    generalPanelLimitRanFPSPanel.setLayout(
        new BoxLayout(generalPanelLimitRanFPSPanel, BoxLayout.X_AXIS));
    generalPanelLimitRanFPSPanel.setPreferredSize(osScaleMul(new Dimension(0, 26)));
    generalPanelLimitRanFPSPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelRanReduceFrequencyButton =
        addRadioButton(
            "Reduce frequency, similar to low FPS on an older computer",
            generalPanelLimitRanFPSPanel,
            osScaleMul(20));
    generalPanelRanReduceFrequencyButton.setToolTipText(
        "The random colour effect is exactly the same, but at a frequency closer to what Andrew would have seen when designing the chat effect.");
    SearchUtils.addSearchMetadata(
        generalPanelRanReduceFrequencyButton, CommonMetadata.FPS.getText());

    generalPanelLimitRanFPSSpinner = new JSpinner();
    generalPanelLimitRanFPSPanel.add(generalPanelLimitRanFPSSpinner);
    generalPanelLimitRanFPSSpinner.setMaximumSize(osScaleMul(new Dimension(48, 22)));
    generalPanelLimitRanFPSSpinner.setMinimumSize(osScaleMul(new Dimension(48, 22)));
    generalPanelLimitRanFPSSpinner.setAlignmentY(0.75f);
    generalPanelLimitRanFPSSpinner.setToolTipText("Target FPS");
    generalPanelLimitRanFPSSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    // Sanitize JSpinner value
    SpinnerNumberModel spinnerLimitRanFpsModel = new SpinnerNumberModel();
    spinnerLimitRanFpsModel.setMinimum(1);
    spinnerLimitRanFpsModel.setMaximum(50);
    spinnerLimitRanFpsModel.setValue(10);
    spinnerLimitRanFpsModel.setStepSize(1);
    generalPanelLimitRanFPSSpinner.setModel(spinnerLimitRanFpsModel);
    ///////

    // colour choose ran static color sub-panel
    JPanel generalPanelRanStaticColourPanel = new JPanel();
    generalPanelCustomRandomPanel.add(generalPanelRanStaticColourPanel);
    generalPanelRanStaticColourPanel.setLayout(
        new BoxLayout(generalPanelRanStaticColourPanel, BoxLayout.X_AXIS));
    generalPanelRanStaticColourPanel.setPreferredSize(osScaleMul(new Dimension(0, 26)));
    generalPanelRanStaticColourPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelRanSelectColourButton =
        addRadioButton(
            "Replace with a static colour", generalPanelRanStaticColourPanel, osScaleMul(20));
    generalPanelRanSelectColourButton.setToolTipText(
        "Sets a static color to replace the flashing colour effect.");

    generalPanelRanStaticColourSubpanel = new JPanel();
    generalPanelRanStaticColourPanel.add(generalPanelRanStaticColourSubpanel);
    generalPanelRanStaticColourSubpanel.setAlignmentY(0.7f);
    generalPanelRanStaticColourSubpanel.setMinimumSize(osScaleMul(new Dimension(32, 20)));
    generalPanelRanStaticColourSubpanel.setPreferredSize(osScaleMul(new Dimension(32, 20)));
    generalPanelRanStaticColourSubpanel.setMaximumSize(osScaleMul(new Dimension(32, 20)));
    generalPanelRanStaticColourSubpanel.setBorder(BorderFactory.createLineBorder(Color.black));
    generalPanelRanStaticColourSubpanel.setBackground(ranStaticColour);

    int ranStaticColourMargin = isUsingFlatLAFTheme() ? 8 : 4;
    generalPanelRanStaticColourPanel.add(
        Box.createRigidArea(osScaleMul(new Dimension(ranStaticColourMargin, 20))));

    JButton ranStaticColourChooserButton = new JButton("Choose colour");
    ranStaticColourChooserButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Color selected =
                JColorChooser.showDialog(null, "Choose @ran@ Static Colour", ranStaticColour);
            if (null != selected) {
              ranStaticColour = selected;
            }
            generalPanelRanStaticColourSubpanel.setBackground(ranStaticColour);
          }
        });
    generalPanelRanStaticColourPanel.add(ranStaticColourChooserButton);
    ranStaticColourChooserButton.setAlignmentY(0.7f);

    ////////////

    generalPanelRanRGBRotationButton =
        addRadioButton(
            "Replace with a continuous rainbow colour sweep",
            generalPanelCustomRandomPanel,
            osScaleMul(20));
    generalPanelRanRGBRotationButton.setToolTipText(
        "The effect is similar to RGB gamer PC lighting.");
    generalPanelRanRGBRotationButton.setBorder(
        BorderFactory.createEmptyBorder(
            osScaleMul(5), osScaleMul(20), osScaleMul(7), osScaleMul(5)));

    ////////////

    JPanel generalPanelRanRs2EffectPanel = new JPanel();
    generalPanelCustomRandomPanel.add(generalPanelRanRs2EffectPanel);
    generalPanelRanRs2EffectPanel.setLayout(
        new BoxLayout(generalPanelRanRs2EffectPanel, BoxLayout.X_AXIS));
    generalPanelRanRs2EffectPanel.setPreferredSize(osScaleMul(new Dimension(0, 28)));
    generalPanelRanRs2EffectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelRanRS2EffectButton =
        addRadioButton("Use an RS2 chat effect", generalPanelRanRs2EffectPanel, osScaleMul(20));
    generalPanelRanRS2EffectButton.setToolTipText("Selects an RS2 chat effect to display instead.");

    String[] rs2ChatEffectTypes = {"Flash1", "Flash2", "Flash3", "Glow1", "Glow2", "Glow3"};
    generalPanelRS2ChatEffectComboBox = new JComboBox(rs2ChatEffectTypes);

    generalPanelRS2ChatEffectComboBox.setMinimumSize(osScaleMul(new Dimension(80, 28)));
    generalPanelRS2ChatEffectComboBox.setMaximumSize(osScaleMul(new Dimension(80, 28)));
    generalPanelRS2ChatEffectComboBox.setPreferredSize(osScaleMul(new Dimension(80, 28)));
    generalPanelRS2ChatEffectComboBox.setAlignmentY(0.75f);
    generalPanelRS2ChatEffectComboBox.setSelectedIndex(3);
    generalPanelRanRs2EffectPanel.add(generalPanelRS2ChatEffectComboBox);

    //////

    generalPanelRanEntirelyDisableButton =
        addRadioButton("Entirely disable @ran@", generalPanelCustomRandomPanel, osScaleMul(20));
    generalPanelRanEntirelyDisableButton.setToolTipText(
        "Text occurring after the @ran@ tag will remain the same color as before.");
    generalPanelRanEntirelyDisableButton.setBorder(
        BorderFactory.createEmptyBorder(
            osScaleMul(5), osScaleMul(20), osScaleMul(7), osScaleMul(5)));

    generalPanelVanillaRanHiddenButton = new JRadioButton("vanilla");

    ranChatEffectButtonGroup.add(generalPanelVanillaRanHiddenButton);
    ranChatEffectButtonGroup.add(generalPanelRanReduceFrequencyButton);
    ranChatEffectButtonGroup.add(generalPanelRanRGBRotationButton);
    ranChatEffectButtonGroup.add(generalPanelRanSelectColourButton);
    ranChatEffectButtonGroup.add(generalPanelRanRS2EffectButton);
    ranChatEffectButtonGroup.add(generalPanelRanEntirelyDisableButton);

    ActionListener customRanSelectedActionListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            generalPanelCustomRandomChatColourCheckbox.setSelected(true);
          }
        };
    generalPanelRanReduceFrequencyButton.addActionListener(customRanSelectedActionListener);
    generalPanelRanRGBRotationButton.addActionListener(customRanSelectedActionListener);
    generalPanelRanSelectColourButton.addActionListener(customRanSelectedActionListener);
    generalPanelRanRS2EffectButton.addActionListener(customRanSelectedActionListener);
    generalPanelRanEntirelyDisableButton.addActionListener(customRanSelectedActionListener);

    generalPanelCustomRandomChatColourCheckbox.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (generalPanelCustomRandomChatColourCheckbox.isSelected()) {
              generalPanelRanReduceFrequencyButton.setSelected(true);
            } else {
              generalPanelVanillaRanHiddenButton.setSelected(true);
            }
          }
        });

    // FPS limit
    JPanel generalPanelLimitFPSPanel = new JPanel();
    generalPanel.add(generalPanelLimitFPSPanel);
    generalPanelLimitFPSPanel.setLayout(new BoxLayout(generalPanelLimitFPSPanel, BoxLayout.X_AXIS));
    generalPanelLimitFPSPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    generalPanelLimitFPSCheckbox =
        addCheckbox("FPS limit (doubled while F1 interlaced):", generalPanelLimitFPSPanel);
    generalPanelLimitFPSCheckbox.setToolTipText(
        "Limit FPS for a more 2001 feeling (or to save battery)");
    SearchUtils.addSearchMetadata(
        generalPanelLimitFPSCheckbox, CommonMetadata.FPS.getText(), "limiter");

    generalPanelLimitFPSSpinner = new JSpinner();
    generalPanelLimitFPSPanel.add(generalPanelLimitFPSSpinner);
    generalPanelLimitFPSSpinner.setMaximumSize(osScaleMul(new Dimension(50, 22)));
    generalPanelLimitFPSSpinner.setMinimumSize(osScaleMul(new Dimension(50, 22)));
    generalPanelLimitFPSSpinner.setAlignmentY(0.75f);
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

    addSettingsHeader(generalPanel, "Menu/Item patching");

    generalPanelBypassAttackCheckbox = addCheckbox("Always left click to attack", generalPanel);
    generalPanelBypassAttackCheckbox.setToolTipText(
        "Left click attack monsters regardless of level difference");

    generalPanelNumberedDialogueOptionsCheckbox =
        addCheckbox("Display numbers next to dialogue options", generalPanel);
    generalPanelNumberedDialogueOptionsCheckbox.setToolTipText(
        "Displays a number next to each option within a conversational menu");

    generalPanelDisableNatureRuneAlchCheckbox =
        addCheckbox("Disable the ability to cast alchemy spells on nature runes", generalPanel);
    generalPanelDisableNatureRuneAlchCheckbox.setToolTipText(
        "Protect yourself from a terrible fate");

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
    generalPanelNamePatchModePanel.setMaximumSize(osScaleMul(new Dimension(300, 85)));
    generalPanelNamePatchModePanel.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(5), 0));
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
    generalPanelNamePatchModeSlider.setPreferredSize(osScaleMul(new Dimension(40, 0)));
    generalPanelNamePatchModeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelNamePatchModeSlider.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(10), 0));
    generalPanelNamePatchModeSlider.setOrientation(SwingConstants.VERTICAL);
    generalPanelNamePatchModePanel.add(generalPanelNamePatchModeSlider);

    JPanel generalPanelNamePatchModeTextPanel = new JPanel();
    generalPanelNamePatchModeTextPanel.setPreferredSize(osScaleMul(new Dimension(255, 80)));
    generalPanelNamePatchModeTextPanel.setLayout(new BorderLayout());
    generalPanelNamePatchModeTextPanel.setBorder(
        BorderFactory.createEmptyBorder(0, osScaleMul(10), 0, 0));
    generalPanelNamePatchModePanel.add(generalPanelNamePatchModeTextPanel);

    JLabel generalPanelNamePatchModeTitle = new JLabel("<html><b>Item name patch mode</b></html>");
    generalPanelNamePatchModeTitle.setToolTipText(
        "Replace certain item names with improved versions");
    generalPanelNamePatchModeTextPanel.add(generalPanelNamePatchModeTitle, BorderLayout.PAGE_START);
    generalPanelNamePatchModeDesc = new JLabel("");

    String patchNameType0 = "No item name patching";
    String patchNameType1 =
        "Purely practical name changes (potion dosages, unidentified herbs, unfinished potions)";
    String patchNameType2 = "Capitalizations and fixed spellings on top of type 1 changes";
    String patchNameType3 =
        "Reworded vague stuff to be more descriptive on top of type 1 & 2 changes";

    // Add all possible display values to the search metadata, since only one is displayed at a time
    SearchUtils.addSearchMetadata(
        generalPanelNamePatchModeDesc,
        patchNameType0 + " " + patchNameType1 + " " + patchNameType2 + " " + patchNameType3);

    generalPanelNamePatchModeTextPanel.add(generalPanelNamePatchModeDesc, BorderLayout.CENTER);

    generalPanelNamePatchModeSlider.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            switch (generalPanelNamePatchModeSlider.getValue()) {
              case 3:
                generalPanelNamePatchModeDesc.setText("<html>" + patchNameType3 + "</html>");
                break;
              case 2:
                generalPanelNamePatchModeDesc.setText("<html>" + patchNameType2 + "</html>");
                break;
              case 1:
                generalPanelNamePatchModeDesc.setText("<html>" + patchNameType1 + "</html>");
                break;
              case 0:
                generalPanelNamePatchModeDesc.setText("<html>" + patchNameType0 + "</html>");
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

    // Logger settings

    addSettingsHeader(generalPanel, "Logging settings");

    JPanel generalPanelLogVerbosityPanel = new JPanel();
    generalPanelLogVerbosityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelLogVerbosityPanel.setMaximumSize(osScaleMul(new Dimension(350, 128)));
    generalPanelLogVerbosityPanel.setLayout(
        new BoxLayout(generalPanelLogVerbosityPanel, BoxLayout.Y_AXIS));
    generalPanel.add(generalPanelLogVerbosityPanel);

    JLabel generalPanelLogVerbosityTitle = new JLabel("Log verbosity maximum");
    generalPanelLogVerbosityTitle.setToolTipText(
        "What max level of log text will be shown in the RSCPlus log/console");
    generalPanelLogVerbosityPanel.add(generalPanelLogVerbosityTitle);
    generalPanelLogVerbosityTitle.setAlignmentY(1.0f);
    SearchUtils.addSearchMetadata(generalPanelLogVerbosityTitle, "logger", "logging");

    Hashtable<Integer, JLabel> generalPanelLogVerbosityLabelTable =
        new Hashtable<Integer, JLabel>();
    generalPanelLogVerbosityLabelTable.put(new Integer(0), new JLabel("Error"));
    generalPanelLogVerbosityLabelTable.put(new Integer(1), new JLabel("Warning"));
    generalPanelLogVerbosityLabelTable.put(new Integer(2), new JLabel("Game"));
    generalPanelLogVerbosityLabelTable.put(new Integer(3), new JLabel("Info"));
    generalPanelLogVerbosityLabelTable.put(new Integer(4), new JLabel("Debug"));
    generalPanelLogVerbosityLabelTable.put(new Integer(5), new JLabel("Opcode"));

    if (Util.isUsingFlatLAFTheme()) {
      generalPanelLogVerbosityPanel.add(Box.createRigidArea(osScaleMul(new Dimension(0, 5))));
    }

    generalPanelLogVerbositySlider = new JSlider();
    generalPanelLogVerbositySlider.setMajorTickSpacing(1);
    generalPanelLogVerbositySlider.setLabelTable(generalPanelLogVerbosityLabelTable);
    generalPanelLogVerbositySlider.setPaintLabels(true);
    generalPanelLogVerbositySlider.setPaintTicks(true);
    generalPanelLogVerbositySlider.setSnapToTicks(true);
    generalPanelLogVerbositySlider.setMinimum(0);
    generalPanelLogVerbositySlider.setMaximum(5);
    generalPanelLogVerbositySlider.setPreferredSize(osScaleMul(new Dimension(200, 55)));
    generalPanelLogVerbositySlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelLogVerbositySlider.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(5), 0));
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

    generalPanelColoredTextCheckbox = addCheckbox("Coloured console text", generalPanel);
    generalPanelColoredTextCheckbox.setToolTipText(
        "When running the client from a console, chat messages in the console will reflect the colours they are in game");
    SearchUtils.addSearchMetadata(
        generalPanelColoredTextCheckbox, CommonMetadata.COLOURS.getText());

    // UI Settings
    addSettingsHeader(generalPanel, "UI settings");

    generalPanelUseDarkModeCheckbox =
        addCheckbox(
            "Use dark mode for the interface (Requires restart & modern UI theme)", generalPanel);
    generalPanelUseDarkModeCheckbox.setToolTipText(
        "Uses the darker UI theme, unless the legacy theme is enabled");
    SearchUtils.addSearchMetadata(generalPanelUseDarkModeCheckbox, "light");

    generalPanelUseNimbusThemeCheckbox =
        addCheckbox("Use legacy RSC+ UI theme (Requires restart)", generalPanel);
    generalPanelUseNimbusThemeCheckbox.setToolTipText("Uses the legacy RSC+ Nimbus look-and-feel");

    if (Util.isModernWindowsOS() && Launcher.OSScalingFactor != 1.0) {
      generalPanelUseNimbusThemeCheckbox.setEnabled(false);
      generalPanelUseNimbusThemeCheckbox.setText(
          "<html><strike>Use legacy RSC+ UI theme</strike> You must disable OS level scaling in Windows to enable this option (Requires restart)</html>");
    }

    addPanelBottomGlue(generalPanel);

    /*
     * Overlays tab
     */

    overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));

    /// "Interface overlays" are overlays that have a constant position on
    /// the screen because they are designed to modify just the interface of RSC+
    addSettingsHeader(overlayPanel, "Interface overlays");
    overlayPanelStatusDisplayCheckbox = addCheckbox("Show HP/Prayer/Fatigue display", overlayPanel);
    SearchUtils.addSearchMetadata(overlayPanelStatusDisplayCheckbox, CommonMetadata.HP.getText());
    overlayPanelStatusDisplayCheckbox.setToolTipText("Toggle hits/prayer/fatigue display");

    overlayPanelStatusAlwaysTextCheckbox =
        addCheckbox("Always show HP/Prayer/Fatigue display in upper-left corner", overlayPanel);
    overlayPanelStatusAlwaysTextCheckbox.setToolTipText(
        "Always show the status display as text even at larger client sizes");
    SearchUtils.addSearchMetadata(
        overlayPanelStatusAlwaysTextCheckbox, CommonMetadata.HP.getText());

    overlayPanelBuffsCheckbox =
        addCheckbox("Show combat (de)buffs and cooldowns display", overlayPanel);
    overlayPanelBuffsCheckbox.setToolTipText("Toggle combat (de)buffs and cooldowns display");

    overlayPanelKeptItemsCheckbox = addCheckbox("Show items kept on death", overlayPanel);
    overlayPanelKeptItemsCheckbox.setToolTipText(
        "Displays a skull in your inventory next to the items that you will retain upon dying");

    overlayPanelKeptItemsWildCheckbox =
        addCheckbox("Only show items kept on death while in the wilderness", overlayPanel);
    overlayPanelKeptItemsWildCheckbox.setToolTipText(
        "Only displays the \"kept items\" skull when you are in the wilderness");

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
    SearchUtils.addSearchMetadata(
        overlayPanelInvCountColoursCheckbox, CommonMetadata.COLOURS.getText());

    overlayPanelRemoveReportAbuseButtonHbarCheckbox =
        addCheckbox("Remove Report Abuse Button (Similar to prior to 2002-09-11)", overlayPanel);
    overlayPanelRemoveReportAbuseButtonHbarCheckbox.setToolTipText(
        "mudclient149 added the Report Abuse button. You will still be able to report players with right click menu if this option is enabled.");

    overlayPanelPositionCheckbox = addCheckbox("Display position", overlayPanel);
    overlayPanelPositionCheckbox.setToolTipText("Shows the player's global position");

    overlayPanelRetroFpsCheckbox = addCheckbox("Display FPS like early RSC", overlayPanel);
    overlayPanelRetroFpsCheckbox.setToolTipText(
        "Shows the FPS like it used to be displayed in RSC");
    SearchUtils.addSearchMetadata(overlayPanelRetroFpsCheckbox, CommonMetadata.FPS.getText());

    overlayPanelShowCombatInfoCheckbox = addCheckbox("Show NPC HP info", overlayPanel);
    overlayPanelShowCombatInfoCheckbox.setToolTipText(
        "Shows the HP info for the NPC you're in combat with");
    SearchUtils.addSearchMetadata(overlayPanelShowCombatInfoCheckbox, CommonMetadata.HP.getText());

    overlayPanelUsePercentageCheckbox = addCheckbox("Use percentage for NPC HP info", overlayPanel);
    overlayPanelUsePercentageCheckbox.setToolTipText(
        "Uses percentage for NPC HP info instead of actual HP");
    SearchUtils.addSearchMetadata(overlayPanelUsePercentageCheckbox, CommonMetadata.HP.getText());

    overlayPanelLagIndicatorCheckbox = addCheckbox("Lag indicator", overlayPanel);
    overlayPanelLagIndicatorCheckbox.setToolTipText(
        "When there's a problem with your connection, RSCPlus will tell you in the bottom right");

    overlayPanelFoodHealingCheckbox =
        addCheckbox("Show food healing overlay (Not implemented yet)", overlayPanel);
    overlayPanelFoodHealingCheckbox.setToolTipText(
        "When hovering on food, shows the HP a consumable recovers");
    SearchUtils.addSearchMetadata(overlayPanelFoodHealingCheckbox, CommonMetadata.HP.getText());
    // TODO: Remove this line when food healing overlay is implemented
    overlayPanelFoodHealingCheckbox.setEnabled(false);

    overlayPanelHPRegenTimerCheckbox =
        addCheckbox("Display time until next HP regeneration (Not implemented yet)", overlayPanel);
    overlayPanelHPRegenTimerCheckbox.setToolTipText(
        "Shows the seconds until your HP will naturally regenerate");
    SearchUtils.addSearchMetadata(overlayPanelHPRegenTimerCheckbox, CommonMetadata.HP.getText());
    // TODO: Remove this line when the HP regen timer is implemented
    overlayPanelHPRegenTimerCheckbox.setEnabled(false);

    /// In-game buttons
    addSettingsHeader(overlayPanel, "In-game buttons");

    overlayPanelRscPlusButtonsCheckbox =
        addCheckbox("Display + indicators over the activated in-game buttons", overlayPanel);
    overlayPanelRscPlusButtonsCheckbox.setToolTipText("Display + indicators over in-game buttons");
    SearchUtils.addSearchMetadata(overlayPanelRscPlusButtonsCheckbox, "plus");

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

    JPanel overlayPanelXPDropsPanel = new JPanel();
    overlayPanel.add(overlayPanelXPDropsPanel);
    overlayPanelXPDropsPanel.setLayout(new BoxLayout(overlayPanelXPDropsPanel, BoxLayout.Y_AXIS));
    overlayPanelXPDropsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    overlayPanelXPDropsCheckbox = addCheckbox("Show XP drops", overlayPanelXPDropsPanel);
    overlayPanelXPDropsCheckbox.setToolTipText(
        "Show the XP gained as an overlay each time XP is received");
    overlayPanelXPDropsCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));

    ButtonGroup XPAlignButtonGroup = new ButtonGroup();
    overlayPanelXPRightAlignFocusButton =
        addRadioButton("Display on the right", overlayPanelXPDropsPanel, osScaleMul(20));
    overlayPanelXPRightAlignFocusButton.setToolTipText(
        "The XP bar and XP drops will be shown just left of the Settings menu.");
    overlayPanelXPCenterAlignFocusButton =
        addRadioButton("Display in the center", overlayPanelXPDropsPanel, osScaleMul(20));
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
    generalPanelFatigueFigsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    generalPanelFatigueFigsPanel.setLayout(
        new BoxLayout(generalPanelFatigueFigsPanel, BoxLayout.X_AXIS));

    JLabel generalPanelFatigueFigsLabel = new JLabel("Fatigue figures:");
    generalPanelFatigueFigsPanel.add(generalPanelFatigueFigsLabel);
    generalPanelFatigueFigsLabel.setAlignmentY(0.9f);
    generalPanelFatigueFigsLabel.setToolTipText(
        "Number of significant figures past the decimal point to display on fatigue drops");

    if (Util.isUsingFlatLAFTheme()) {
      generalPanelFatigueFigsPanel.add(Box.createRigidArea(osScaleMul(new Dimension(4, 0))));
    }

    overlayPanelFatigueFigSpinner = new JSpinner();
    generalPanelFatigueFigsPanel.add(overlayPanelFatigueFigSpinner);
    overlayPanelFatigueFigSpinner.setMaximumSize(osScaleMul(new Dimension(48, 22)));
    overlayPanelFatigueFigSpinner.setMinimumSize(osScaleMul(new Dimension(48, 22)));
    overlayPanelFatigueFigSpinner.setAlignmentY(0.7f);
    generalPanelFatigueFigsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(4), 0));
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

    overlayPanelPlayerNamesCheckbox =
        addCheckbox("Show player names over their heads", overlayPanel);
    overlayPanelPlayerNamesCheckbox.setToolTipText(
        "Shows players' display names over their character");

    // pvp names sub-panel
    JPanel overlayPanelPvpNamesPanel = new JPanel();
    overlayPanel.add(overlayPanelPvpNamesPanel);
    overlayPanelPvpNamesPanel.setLayout(new BoxLayout(overlayPanelPvpNamesPanel, BoxLayout.X_AXIS));
    overlayPanelPvpNamesPanel.setPreferredSize(osScaleMul(new Dimension(0, 28)));
    overlayPanelPvpNamesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    overlayPanelPvpNamesCheckbox =
        addCheckbox(
            "Show attackable players' names in a separate colour", overlayPanelPvpNamesPanel);
    overlayPanelPvpNamesCheckbox.setToolTipText(
        "Changes the colour of players' names when they are within attacking range in the wilderness");
    SearchUtils.addSearchMetadata(overlayPanelPvpNamesCheckbox, CommonMetadata.COLOUR.getText());

    overlayPanelPvpNamesColourSubpanel = new JPanel();
    overlayPanelPvpNamesPanel.add(overlayPanelPvpNamesColourSubpanel);
    overlayPanelPvpNamesColourSubpanel.setAlignmentY(0.65f);
    overlayPanelPvpNamesColourSubpanel.setMinimumSize(osScaleMul(new Dimension(32, 20)));
    overlayPanelPvpNamesColourSubpanel.setPreferredSize(osScaleMul(new Dimension(32, 20)));
    overlayPanelPvpNamesColourSubpanel.setMaximumSize(osScaleMul(new Dimension(32, 20)));
    overlayPanelPvpNamesColourSubpanel.setBorder(BorderFactory.createLineBorder(Color.black));
    overlayPanelPvpNamesColourSubpanel.setBackground(pvpNamesColour);

    int pvpNamesColourMargin = isUsingFlatLAFTheme() ? 8 : 4;
    overlayPanelPvpNamesPanel.add(
        Box.createRigidArea(osScaleMul(new Dimension(pvpNamesColourMargin, 20))));

    JButton pvpNamesColourChooserButton = new JButton("Choose colour");
    pvpNamesColourChooserButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Color selected =
                JColorChooser.showDialog(null, "Choose PVP display name colour", pvpNamesColour);
            if (null != selected) {
              pvpNamesColour = selected;
            }
            overlayPanelPvpNamesColourSubpanel.setBackground(pvpNamesColour);
          }
        });
    overlayPanelPvpNamesPanel.add(pvpNamesColourChooserButton);
    pvpNamesColourChooserButton.setAlignmentY(0.7f);
    ////////////

    overlayPanelOwnNameCheckbox =
        addCheckbox(
            "Show your own name over your head when player names are enabled", overlayPanel);
    overlayPanelOwnNameCheckbox.setToolTipText(
        "Shows your own display name over your character when player names are shown");

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

    JPanel overlayPanelGroundItemsPanel = new JPanel();
    overlayPanel.add(overlayPanelGroundItemsPanel);
    overlayPanelGroundItemsPanel.setLayout(
        new BoxLayout(overlayPanelGroundItemsPanel, BoxLayout.Y_AXIS));
    overlayPanelGroundItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    overlayPanelItemNamesCheckbox =
        addCheckbox("Display the names of items on the ground", overlayPanelGroundItemsPanel);
    overlayPanelItemNamesCheckbox.setToolTipText("Shows the names of dropped items");

    overlayPanelItemNamesHighlightedOnlyCheckbox =
        addCheckbox("Only display highlighted items", overlayPanelGroundItemsPanel);
    overlayPanelItemNamesHighlightedOnlyCheckbox.setToolTipText(
        "Will only show items in the highlighted list below");

    String itemInputToolTip =
        "Surround with \" \" for exact matches (not case-sensitive). Block list takes priority over highlight list.";

    int itemsTextHeight = isUsingFlatLAFTheme() ? 32 : 37;

    // Blocked Items
    JPanel blockedItemsPanel = new JPanel();
    overlayPanelGroundItemsPanel.add(blockedItemsPanel);
    blockedItemsPanel.setLayout(new BoxLayout(blockedItemsPanel, BoxLayout.X_AXIS));
    blockedItemsPanel.setPreferredSize(osScaleMul(new Dimension(0, itemsTextHeight)));
    blockedItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    blockedItemsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(9), 0));
    blockedItemsPanel.setToolTipText(itemInputToolTip);

    JLabel blockedItemsPanelNameLabel = new JLabel("Blocked items: ");
    blockedItemsPanel.add(blockedItemsPanelNameLabel);
    blockedItemsPanelNameLabel.setAlignmentY(0.9f);

    blockedItemsTextField = new JTextField();
    blockedItemsPanel.add(blockedItemsTextField);
    blockedItemsTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    blockedItemsTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    blockedItemsTextField.setAlignmentY(0.75f);
    blockedItemsTextField.setToolTipText(itemInputToolTip);

    // Highlighted Items
    JPanel highlightedItemsPanel = new JPanel();
    overlayPanelGroundItemsPanel.add(highlightedItemsPanel);
    highlightedItemsPanel.setLayout(new BoxLayout(highlightedItemsPanel, BoxLayout.X_AXIS));
    highlightedItemsPanel.setPreferredSize(osScaleMul(new Dimension(0, itemsTextHeight)));
    highlightedItemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    highlightedItemsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(9), 0));
    highlightedItemsPanel.setToolTipText(itemInputToolTip);

    JLabel highlightedItemsPanelNameLabel = new JLabel("Highlighted items: ");
    highlightedItemsPanel.add(highlightedItemsPanelNameLabel);
    highlightedItemsPanelNameLabel.setAlignmentY(0.9f);

    highlightedItemsTextField = new JTextField();
    highlightedItemsPanel.add(highlightedItemsTextField);
    highlightedItemsTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    highlightedItemsTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    highlightedItemsTextField.setAlignmentY(0.75f);
    highlightedItemsTextField.setToolTipText(itemInputToolTip);

    // Highlight colour panel
    JPanel overlayPanelItemHighlightColourPanel = new JPanel();
    overlayPanel.add(overlayPanelItemHighlightColourPanel);
    overlayPanelItemHighlightColourPanel.setLayout(
        new BoxLayout(overlayPanelItemHighlightColourPanel, BoxLayout.X_AXIS));
    overlayPanelItemHighlightColourPanel.setPreferredSize(osScaleMul(new Dimension(0, 26)));
    overlayPanelItemHighlightColourPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel highlightedItemColourPanelNameLabel = new JLabel("Highlight colour ");
    overlayPanelItemHighlightColourPanel.add(highlightedItemColourPanelNameLabel);
    highlightedItemColourPanelNameLabel.setAlignmentY(0.9f);
    SearchUtils.addSearchMetadata(
        highlightedItemColourPanelNameLabel, CommonMetadata.COLOUR.getText());

    overlayPanelItemHighlightColourSubpanel = new JPanel();
    overlayPanelItemHighlightColourPanel.add(overlayPanelItemHighlightColourSubpanel);
    overlayPanelItemHighlightColourSubpanel.setAlignmentY(0.7f);
    overlayPanelItemHighlightColourSubpanel.setMinimumSize(osScaleMul(new Dimension(32, 20)));
    overlayPanelItemHighlightColourSubpanel.setPreferredSize(osScaleMul(new Dimension(32, 20)));
    overlayPanelItemHighlightColourSubpanel.setMaximumSize(osScaleMul(new Dimension(32, 20)));
    overlayPanelItemHighlightColourSubpanel.setBorder(BorderFactory.createLineBorder(Color.black));
    overlayPanelItemHighlightColourSubpanel.setBackground(itemHighlightColour);

    int itemHighlightColourMargin = isUsingFlatLAFTheme() ? 8 : 4;
    overlayPanelItemHighlightColourPanel.add(
        Box.createRigidArea(osScaleMul(new Dimension(itemHighlightColourMargin, 20))));

    JButton rightClickHighlightColourChooserButton = new JButton("Choose colour");
    rightClickHighlightColourChooserButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Color selected =
                JColorChooser.showDialog(null, "Choose item highlight colour", itemHighlightColour);
            if (null != selected) {
              itemHighlightColour = selected;
            }
            overlayPanelItemHighlightColourSubpanel.setBackground(itemHighlightColour);
          }
        });
    overlayPanelItemHighlightColourPanel.add(rightClickHighlightColourChooserButton);
    rightClickHighlightColourChooserButton.setAlignmentY(0.7f);
    ////////////

    overlayPanelHighlightRightClickCheckbox =
        addCheckbox("Highlight items in the right-click menu", overlayPanel);
    overlayPanelHighlightRightClickCheckbox.setToolTipText(
        "Highlights items from the above list in the right-click menu");
    overlayPanelHighlightRightClickCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(9), 0, osScaleMul(10), 0));

    addSettingsHeader(overlayPanel, "General overlay settings");

    JPanel overlayPanelFontStylePanel = new JPanel();
    overlayPanel.add(overlayPanelFontStylePanel);
    overlayPanelFontStylePanel.setLayout(
        new BoxLayout(overlayPanelFontStylePanel, BoxLayout.Y_AXIS));
    overlayPanelFontStylePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel overlayFontStyleNameLabel = new JLabel("Overlay font style:");
    overlayFontStyleNameLabel.setAlignmentY(0.9f);
    overlayFontStyleNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));
    overlayPanelFontStylePanel.add(overlayFontStyleNameLabel);

    ButtonGroup overlayPanelFontStyleButtonGroup = new ButtonGroup();
    // overlayPanelFontStyleJagexFocusButton = addRadioButton("Use the Jagex font", overlayPanel,
    // osScaleMul(20));
    // overlayPanelFontStyleJagexFocusButton.setToolTipText(
    //         "Use the standard Jagex font for all custom overlay text");
    overlayPanelFontStyleJagexBorderedFocusButton =
        addRadioButton("Use bordered Jagex fonts", overlayPanelFontStylePanel, osScaleMul(20));
    overlayPanelFontStyleJagexBorderedFocusButton.setToolTipText(
        "Use a bordered version of the standard Jagex font for custom overlay text");
    overlayPanelFontStyleLegacyFocusButton =
        addRadioButton("Use legacy RSC+ font", overlayPanelFontStylePanel, osScaleMul(20));
    overlayPanelFontStyleLegacyFocusButton.setToolTipText(
        "Use the legacy RSC+ font for custom overlay text");
    // overlayPanelFontStyleButtonGroup.add(overlayPanelFontStyleJagexFocusButton);
    overlayPanelFontStyleButtonGroup.add(overlayPanelFontStyleJagexBorderedFocusButton);
    overlayPanelFontStyleButtonGroup.add(overlayPanelFontStyleLegacyFocusButton);

    addPanelBottomGlue(overlayPanel);

    /*
     * Audio tab
     */

    audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(audioPanel, "Audio settings");

    audioPanelEnableMusicCheckbox =
        addCheckbox("Enable Music (Must have music pack installed)", audioPanel);
    audioPanelEnableMusicCheckbox.setToolTipText("Enable Music (Must have music pack installed)");

    JPanel audioPanelSfxVolumePanel = new JPanel();
    audioPanel.add(audioPanelSfxVolumePanel);
    audioPanelSfxVolumePanel.setLayout(new BoxLayout(audioPanelSfxVolumePanel, BoxLayout.Y_AXIS));
    audioPanelSfxVolumePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    audioPanelSfxVolumePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(10), 0));

    JLabel audioPanelSfxVolumeLabel = new JLabel("Sound effects volume");
    audioPanelSfxVolumeLabel.setToolTipText("Sets the volume for game sound effects");
    audioPanelSfxVolumePanel.add(audioPanelSfxVolumeLabel);
    audioPanelSfxVolumeLabel.setAlignmentY(1.0f);
    SearchUtils.addSearchMetadata(audioPanelSfxVolumeLabel, CommonMetadata.SFX.getText());

    audioPanelSfxVolumePanel.add(Box.createRigidArea(osScaleMul(new Dimension(0, 5))));

    audioPanelSfxVolumeSlider = new JSlider();

    audioPanelSfxVolumePanel.add(audioPanelSfxVolumeSlider);
    audioPanelSfxVolumeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    audioPanelSfxVolumeSlider.setMaximumSize(osScaleMul(new Dimension(350, 55)));
    audioPanelSfxVolumeSlider.setMinorTickSpacing(5);
    audioPanelSfxVolumeSlider.setMajorTickSpacing(10);
    audioPanelSfxVolumeSlider.setMinimum(0);
    audioPanelSfxVolumeSlider.setMaximum(100);
    audioPanelSfxVolumeSlider.setPaintTicks(true);

    Hashtable<Integer, JLabel> audioPanelSfxVolumeTable = new Hashtable<Integer, JLabel>();
    audioPanelSfxVolumeTable.put(new Integer(0), new JLabel("0"));
    audioPanelSfxVolumeTable.put(new Integer(25), new JLabel("25"));
    audioPanelSfxVolumeTable.put(new Integer(50), new JLabel("50"));
    audioPanelSfxVolumeTable.put(new Integer(75), new JLabel("75"));
    audioPanelSfxVolumeTable.put(new Integer(100), new JLabel("100"));
    audioPanelSfxVolumeSlider.setLabelTable(audioPanelSfxVolumeTable);
    audioPanelSfxVolumeSlider.setPaintLabels(true);

    audioPanelLouderSoundEffectsCheckbox = addCheckbox("Louder sound effects", audioPanel);
    audioPanelLouderSoundEffectsCheckbox.setToolTipText(
        "Doubles the current volume for all sound effects.");
    SearchUtils.addSearchMetadata(
        audioPanelLouderSoundEffectsCheckbox, CommonMetadata.SFX.getText());

    JPanel audioPanelOverrideAudioSettingsPanel = new JPanel();
    audioPanel.add(audioPanelOverrideAudioSettingsPanel);
    audioPanelOverrideAudioSettingsPanel.setLayout(
        new BoxLayout(audioPanelOverrideAudioSettingsPanel, BoxLayout.Y_AXIS));
    audioPanelOverrideAudioSettingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    audioPanelOverrideAudioSettingCheckbox =
        addCheckbox(
            "Override server's remembered audio on/off setting",
            audioPanelOverrideAudioSettingsPanel);
    audioPanelOverrideAudioSettingCheckbox.setToolTipText(
        "Let RSC+ control whether or not sound effects are played (useful for watching replays)");
    audioPanelOverrideAudioSettingCheckbox.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));
    SearchUtils.addSearchMetadata(
        audioPanelOverrideAudioSettingCheckbox, CommonMetadata.SFX.getText());

    ButtonGroup overrideAudioSettingGroup = new ButtonGroup();
    audioPanelOverrideAudioSettingOnButton =
        addRadioButton(
            "Sound effects always on", audioPanelOverrideAudioSettingsPanel, osScaleMul(20));
    audioPanelOverrideAudioSettingOnButton.setToolTipText(
        "Even if the server remembers that the user's audio should be off, RSC+ will play sound effects.");
    SearchUtils.addSearchMetadata(
        audioPanelOverrideAudioSettingOnButton, CommonMetadata.SFX.getText());
    audioPanelOverrideAudioSettingOffButton =
        addRadioButton(
            "Sound effects always off", audioPanelOverrideAudioSettingsPanel, osScaleMul(20));
    audioPanelOverrideAudioSettingOffButton.setToolTipText(
        "Even if the server remembers that the user's audio should be on, RSC+ will NOT play sound effects.");
    SearchUtils.addSearchMetadata(
        audioPanelOverrideAudioSettingOffButton, CommonMetadata.SFX.getText());
    overrideAudioSettingGroup.add(audioPanelOverrideAudioSettingOnButton);
    overrideAudioSettingGroup.add(audioPanelOverrideAudioSettingOffButton);

    audioPanelFixSpiderWebDummySoundCheckbox =
        addCheckbox("Fix web slicing & dummy hitting sound effect", audioPanel);
    audioPanelFixSpiderWebDummySoundCheckbox.setToolTipText(
        "The RSC server authentically tells your client to play a sound effect when slicing a web or hitting a dummy, but that sound effect doesn't exist in an unmodified client cache.");
    SearchUtils.addSearchMetadata(
        audioPanelFixSpiderWebDummySoundCheckbox, CommonMetadata.SFX.getText());

    addSettingsHeader(audioPanel, "Toggle individual sound effects");

    JPanel audioPanelEnableAllSfxPanel = new JPanel();
    audioPanel.add(audioPanelEnableAllSfxPanel);
    audioPanelEnableAllSfxPanel.setLayout(
        new BoxLayout(audioPanelEnableAllSfxPanel, BoxLayout.Y_AXIS));
    audioPanelEnableAllSfxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel audioPanelSoundEffectsToggleExplanation =
        new JLabel(
            "<html><p>"
                + "There are 37 sound effects in RS-Classic.<br/>"
                + "Some are great, and some can be grating. It's up to you to decide which are which."
                + "</p></html>");
    audioPanelEnableAllSfxPanel.add(audioPanelSoundEffectsToggleExplanation);
    audioPanelSoundEffectsToggleExplanation.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));
    SearchUtils.skipSearchText(audioPanelSoundEffectsToggleExplanation);

    JPanel audioPanelToggleAllPanel = new JPanel();
    audioPanelToggleAllPanel.setLayout(new BoxLayout(audioPanelToggleAllPanel, BoxLayout.X_AXIS));
    audioPanelToggleAllPanel.setPreferredSize(osScaleMul(new Dimension(0, 28)));
    audioPanelToggleAllPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    audioPanelToggleAllPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    JButton audioPanelEnableAllSfxButton =
        addButton("Enable All Sound Effects", audioPanelToggleAllPanel, Component.LEFT_ALIGNMENT);
    audioPanelEnableAllSfxButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            setAllSoundeffects(true);
          }
        });
    SearchUtils.addSearchMetadata(audioPanelEnableAllSfxButton, CommonMetadata.SFX.getText());

    audioPanelToggleAllPanel.add(Box.createRigidArea(osScaleMul(new Dimension(6, 20))));

    JButton audioPanelDisableAllSfxButton =
        addButton("Disable All Sound Effects", audioPanelToggleAllPanel, Component.LEFT_ALIGNMENT);
    audioPanelDisableAllSfxButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            setAllSoundeffects(false);
          }
        });
    SearchUtils.addSearchMetadata(audioPanelDisableAllSfxButton, CommonMetadata.SFX.getText());

    audioPanelEnableAllSfxPanel.add(audioPanelToggleAllPanel);

    JPanel advancePanel = makeSoundEffectPanel("advance");
    soundEffectAdvanceCheckbox = addCheckbox("advance", advancePanel);
    soundEffectAdvanceCheckbox.setToolTipText("Plays when advancing a level.");
    soundEffectAdvanceCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(advancePanel);

    JPanel anvilPanel = makeSoundEffectPanel("anvil");
    soundEffectAnvilCheckbox = addCheckbox("anvil", anvilPanel);
    soundEffectAnvilCheckbox.setToolTipText("Plays when hammering on an anvil.");
    soundEffectAnvilCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(anvilPanel);

    JPanel chiselPanel = makeSoundEffectPanel("chisel");
    soundEffectChiselCheckbox = addCheckbox("chisel", chiselPanel);
    soundEffectChiselCheckbox.setToolTipText("Plays when cutting a gemstone.");
    soundEffectChiselCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(chiselPanel);

    JPanel clickPanel = makeSoundEffectPanel("click");
    soundEffectClickCheckbox = addCheckbox("click", clickPanel);
    soundEffectClickCheckbox.setToolTipText("Plays when equipping or unequipping your equipment.");
    soundEffectClickCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(clickPanel);

    JPanel closedoorPanel = makeSoundEffectPanel("closedoor");
    soundEffectClosedoorCheckbox = addCheckbox("closedoor", closedoorPanel);
    soundEffectClosedoorCheckbox.setToolTipText("Plays when a door opens or closes.");
    soundEffectClosedoorCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(closedoorPanel);

    JPanel coinsPanel = makeSoundEffectPanel("coins");
    soundEffectCoinsCheckbox = addCheckbox("coins", coinsPanel);
    soundEffectCoinsCheckbox.setToolTipText("Plays when buying or selling to a shop.");
    soundEffectCoinsCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(coinsPanel);

    JPanel combat1aPanel = makeSoundEffectPanel("combat1a");
    soundEffectCombat1aCheckbox = addCheckbox("combat1a", combat1aPanel);
    soundEffectCombat1aCheckbox.setToolTipText(
        "Plays when no damage is done without a weapon wielded.");
    soundEffectCombat1aCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(combat1aPanel);

    JPanel combat1bPanel = makeSoundEffectPanel("combat1b");
    soundEffectCombat1bCheckbox = addCheckbox("combat1b", combat1bPanel);
    soundEffectCombat1bCheckbox.setToolTipText(
        "Plays when damage is done in combat without a weapon wielded.");
    soundEffectCombat1bCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(combat1bPanel);

    JPanel combat2aPanel = makeSoundEffectPanel("combat2a");
    soundEffectCombat2aCheckbox = addCheckbox("combat2a", combat2aPanel);
    soundEffectCombat2aCheckbox.setToolTipText(
        "Plays when no damage is done with a sharp weapon wielded.");
    soundEffectCombat2aCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(combat2aPanel);

    JPanel combat2bPanel = makeSoundEffectPanel("combat2b");
    soundEffectCombat2bCheckbox = addCheckbox("combat2b", combat2bPanel);
    soundEffectCombat2bCheckbox.setToolTipText(
        "Plays when damage is done with a sharp weapon wielded.");
    soundEffectCombat2bCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(combat2bPanel);

    JPanel combat3aPanel = makeSoundEffectPanel("combat3a");
    soundEffectCombat3aCheckbox = addCheckbox("combat3a", combat3aPanel);
    soundEffectCombat3aCheckbox.setToolTipText(
        "Plays when no damage is done against an undead opponent.");
    soundEffectCombat3aCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(combat3aPanel);

    JPanel combat3bPanel = makeSoundEffectPanel("combat3b");
    soundEffectCombat3bCheckbox = addCheckbox("combat3b", combat3bPanel);
    soundEffectCombat3bCheckbox.setToolTipText(
        "Plays when damage is done against an undead opponent.");
    soundEffectCombat3bCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(combat3bPanel);

    JPanel cookingPanel = makeSoundEffectPanel("cooking");
    soundEffectCookingCheckbox = addCheckbox("cooking", cookingPanel);
    soundEffectCookingCheckbox.setToolTipText("Plays when cooking food on a range or fire.");
    soundEffectCookingCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(cookingPanel);

    JPanel deathPanel = makeSoundEffectPanel("death");
    soundEffectDeathCheckbox = addCheckbox("death", deathPanel);
    soundEffectDeathCheckbox.setToolTipText("Plays when the player dies.");
    soundEffectDeathCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(deathPanel);

    JPanel dropobjectPanel = makeSoundEffectPanel("dropobject");
    soundEffectDropobjectCheckbox = addCheckbox("dropobject", dropobjectPanel);
    soundEffectDropobjectCheckbox.setToolTipText("Plays when you drop an item.");
    soundEffectDropobjectCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(dropobjectPanel);

    JPanel eatPanel = makeSoundEffectPanel("eat");
    soundEffectEatCheckbox = addCheckbox("eat", eatPanel);
    soundEffectEatCheckbox.setToolTipText("Plays when you eat food.");
    soundEffectEatCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(eatPanel);

    JPanel filljugPanel = makeSoundEffectPanel("filljug");
    soundEffectFilljugCheckbox = addCheckbox("filljug", filljugPanel);
    soundEffectFilljugCheckbox.setToolTipText("Plays when filling things with water.");
    soundEffectFilljugCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(filljugPanel);

    JPanel fishPanel = makeSoundEffectPanel("fish");
    soundEffectFishCheckbox = addCheckbox("fish", fishPanel);
    soundEffectFishCheckbox.setToolTipText("Plays when fishing.");
    soundEffectFishCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(fishPanel);

    JPanel foundgemPanel = makeSoundEffectPanel("foundgem");
    soundEffectFoundgemCheckbox = addCheckbox("foundgem", foundgemPanel);
    soundEffectFoundgemCheckbox.setToolTipText("Plays when you find a gem while fishing.");
    soundEffectFoundgemCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(foundgemPanel);

    JPanel mechanicalPanel = makeSoundEffectPanel("mechanical");
    soundEffectMechanicalCheckbox = addCheckbox("mechanical", mechanicalPanel);
    soundEffectMechanicalCheckbox.setToolTipText(
        "Plays when using a hopper, spinning wheel, making pottery.");
    soundEffectMechanicalCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(mechanicalPanel);

    JPanel minePanel = makeSoundEffectPanel("mine");
    soundEffectMineCheckbox = addCheckbox("mine", minePanel);
    soundEffectMineCheckbox.setToolTipText("Plays when mining.");
    soundEffectMineCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(minePanel);

    JPanel mixPanel = makeSoundEffectPanel("mix");
    soundEffectMixCheckbox = addCheckbox("mix", mixPanel);
    soundEffectMixCheckbox.setToolTipText(
        "Plays when mixing ingredients, particularly in Herblaw.");
    soundEffectMixCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(mixPanel);

    JPanel opendoorPanel = makeSoundEffectPanel("opendoor");
    soundEffectOpendoorCheckbox = addCheckbox("opendoor", opendoorPanel);
    soundEffectOpendoorCheckbox.setToolTipText("The sound of a door opening.");
    soundEffectOpendoorCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(opendoorPanel);

    JPanel outofammoPanel = makeSoundEffectPanel("outofammo");
    soundEffectOutofammoCheckbox = addCheckbox("outofammo", outofammoPanel);
    soundEffectOutofammoCheckbox.setToolTipText("Plays when you run out of ammo while ranging.");
    soundEffectOutofammoCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(outofammoPanel);

    JPanel potatoPanel = makeSoundEffectPanel("potato");
    soundEffectPotatoCheckbox = addCheckbox("potato", potatoPanel);
    soundEffectPotatoCheckbox.setToolTipText("Plays when harvesting crops from a field.");
    soundEffectPotatoCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(potatoPanel);

    JPanel prayeroffPanel = makeSoundEffectPanel("prayeroff");
    soundEffectPrayeroffCheckbox = addCheckbox("prayeroff", prayeroffPanel);
    soundEffectPrayeroffCheckbox.setToolTipText("Plays when disabling a prayer.");
    soundEffectPrayeroffCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    soundEffectPrayeroffCheckbox.setEnabled(
        false); // TODO: would need to either reimplement opcode 206 or go disable it in there
    // (preferred)
    audioPanel.add(prayeroffPanel);

    JPanel prayeronPanel = makeSoundEffectPanel("prayeron");
    soundEffectPrayeronCheckbox = addCheckbox("prayeron", prayeronPanel);
    soundEffectPrayeronCheckbox.setToolTipText("Plays when enabling a prayer.");
    soundEffectPrayeronCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    soundEffectPrayeronCheckbox.setEnabled(
        false); // TODO: would need to either reimplement opcode 206 or go disable it in there
    // (preferred)
    audioPanel.add(prayeronPanel);

    JPanel prospectPanel = makeSoundEffectPanel("prospect");
    soundEffectProspectCheckbox = addCheckbox("prospect", prospectPanel);
    soundEffectProspectCheckbox.setToolTipText("Plays when prospecting a mining resource.");
    soundEffectProspectCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(prospectPanel);

    JPanel rechargePanel = makeSoundEffectPanel("recharge");
    soundEffectRechargeCheckbox = addCheckbox("recharge", rechargePanel);
    soundEffectRechargeCheckbox.setToolTipText("Plays when praying at an altar.");
    soundEffectRechargeCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(rechargePanel);

    JPanel retreatPanel = makeSoundEffectPanel("retreat");
    soundEffectRetreatCheckbox = addCheckbox("retreat", retreatPanel);
    soundEffectRetreatCheckbox.setToolTipText("Plays when you or your opponent flee from combat.");
    soundEffectRetreatCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(retreatPanel);

    JPanel secretdoorPanel = makeSoundEffectPanel("secretdoor");
    soundEffectSecretdoorCheckbox = addCheckbox("secretdoor", secretdoorPanel);
    soundEffectSecretdoorCheckbox.setToolTipText(
        "Plays when passing through a secret door (e.g. in Karamja dungeon)");
    soundEffectSecretdoorCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(secretdoorPanel);

    JPanel shootPanel = makeSoundEffectPanel("shoot");
    soundEffectShootCheckbox = addCheckbox("shoot", shootPanel);
    soundEffectShootCheckbox.setToolTipText("Plays when using the ranged skill.");
    soundEffectShootCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(shootPanel);

    JPanel spellfailPanel = makeSoundEffectPanel("spellfail");
    soundEffectSpellfailCheckbox = addCheckbox("spellfail", spellfailPanel);
    soundEffectSpellfailCheckbox.setToolTipText(
        "Plays when you fail to cast a spell successfully.");
    soundEffectSpellfailCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(spellfailPanel);

    JPanel spellokPanel = makeSoundEffectPanel("spellok");
    soundEffectSpellokCheckbox = addCheckbox("spellok", spellokPanel);
    soundEffectSpellokCheckbox.setToolTipText("Plays when you successfully cast a spell.");
    soundEffectSpellokCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(spellokPanel);

    JPanel takeobjectPanel = makeSoundEffectPanel("takeobject");
    soundEffectTakeobjectCheckbox = addCheckbox("takeobject", takeobjectPanel);
    soundEffectTakeobjectCheckbox.setToolTipText("Plays when you pick up an item.");
    soundEffectTakeobjectCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(takeobjectPanel);

    JPanel underattackPanel = makeSoundEffectPanel("underattack");
    soundEffectUnderattackCheckbox = addCheckbox("underattack", underattackPanel);
    soundEffectUnderattackCheckbox.setToolTipText("Plays when you are attacked.");
    soundEffectUnderattackCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(underattackPanel);

    JPanel victoryPanel = makeSoundEffectPanel("victory");
    soundEffectVictoryCheckbox = addCheckbox("victory", victoryPanel);
    soundEffectVictoryCheckbox.setToolTipText("Plays when you have won a fight.");
    soundEffectVictoryCheckbox.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(4), osScaleMul(6), 0));
    audioPanel.add(victoryPanel);

    addPanelBottomGlue(audioPanel);

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

    bankPanelSortFilterAugmentCheckbox = addCheckbox("Sort or Filter your Bank!!", bankPanel);
    bankPanelSortFilterAugmentCheckbox.setToolTipText(
        "Displays the RSC+ Sort and Filtering interface! Authentic!");

    bankPanelStartSearchedBankCheckbox = addCheckbox("Remember Filter/Sort", bankPanel);
    bankPanelStartSearchedBankCheckbox.setToolTipText(
        "Always start with your last filtered/sorted bank settings");

    JPanel searchBankPanel = new JPanel();
    bankPanel.add(searchBankPanel);
    searchBankPanel.setLayout(new BoxLayout(searchBankPanel, BoxLayout.X_AXIS));
    searchBankPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    searchBankPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    JLabel searchBankPanelLabel = new JLabel("Item Search (supports CSV): ");
    searchBankPanelLabel.setToolTipText(
        "List of phrases that occur in item names that you would like to see in your bank");
    searchBankPanel.add(searchBankPanelLabel);
    searchBankPanelLabel.setAlignmentY(0.9f);

    bankPanelSearchBankWordTextField = new JTextField();
    searchBankPanel.add(bankPanelSearchBankWordTextField);
    bankPanelSearchBankWordTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    bankPanelSearchBankWordTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    bankPanelSearchBankWordTextField.setAlignmentY(0.75f);

    JLabel banksearchExplanation =
        new JLabel(
            "<html><p>"
                + "<strong>Note:</strong> <em>Right Clicking</em> the magnifying glass is also a way to set the \"Item Search\""
                + "</p></html>");
    bankPanel.add(banksearchExplanation);
    banksearchExplanation.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(4), 0, osScaleMul(12), 0));
    SearchUtils.skipSearchText(banksearchExplanation);

    addSettingsHeader(bankPanel, "Custom bank order");
    JLabel exportExplanation =
        new JLabel(
            String.format(
                "<html><head><style>p{font-size:%dpx;}</style></head><p>"
                    + "You can define your own preferred bank sort order here!<br/>"
                    + "This is where the User button at the bottom left of the bank is defined when \"Sort or Filter your Bank!!\" is enabled.<br/>"
                    + "The sort order is applied per-username.<br/>"
                    + "<br/><strong>Instructions:</strong><br/>Open your bank, click some buttons (or don't) then click <strong>Export Current Bank</strong> below.<br/>"
                    + "Your bank as it is currently displayed will be exported to a file."
                    + "</p><br/></html>",
                osScaleMul(10)));
    bankPanel.add(exportExplanation);
    SearchUtils.skipSearchText(exportExplanation);

    JPanel exportPanel = new JPanel();
    JButton bankExportButton = new JButton("Export Current Bank");
    bankExportButton.setAlignmentY(0.80f);
    bankExportButton.setPreferredSize(osScaleMul(new Dimension(200, 28)));
    bankExportButton.setMinimumSize(osScaleMul(new Dimension(200, 28)));
    bankExportButton.setMaximumSize(osScaleMul(new Dimension(200, 28)));
    bankExportButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            String result = Bank.exportBank();
            Logger.Info(result);
            bankPanelExportLabel.setText(
                String.format(
                    "<html><head><style>p{font-size:%dpx;padding-left:%dpx;}</style></head><p><strong>Status:</strong>&nbsp;"
                        + result.replace(" ", "&nbsp;") // non-breaking space prevents newline
                        + "</p></html>",
                    osScaleMul(10),
                    osScaleMul(7)));
          }
        });

    bankPanel.add(exportPanel);
    exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.X_AXIS));
    exportPanel.setPreferredSize(osScaleMul(new Dimension(0, 37)));
    exportPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    exportPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    exportPanel.add(bankExportButton);
    bankPanelExportLabel = new JLabel(" ");
    bankPanelExportLabel.setAlignmentY(0.7f);
    bankPanelExportLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));
    exportPanel.add(bankPanelExportLabel);

    JComponent exportButtonSpacer =
        (JComponent) Box.createRigidArea(osScaleMul(new Dimension(0, 11)));
    // Make spacer related when using FlatLAF to account for extra spacing
    if (Util.isUsingFlatLAFTheme()) {
      SearchUtils.setRelatedSearchComponent(exportButtonSpacer, bankExportButton);
    }
    bankPanel.add(exportButtonSpacer);

    JLabel importExplanation =
        new JLabel(
            String.format(
                "<html><head><style>p{font-size:%dpx;}</style></head><p>"
                    + "Once you have that file, go to <strong>https://rsc.plus/bank-organizer</strong><br/>"
                    + "From there, you can import the file. Click \"Import from RSC+ .csv file\" under the Controls section.<br/>"
                    + "It's a really nice HTML bank organizer which you can use to rearrange your bank<br/>"
                    + "or add new item \"Place Holders\" for when you acquire an item later.<br/>"
                    + "When you're done, click the \"Save to RSC+ .csv file\" button.<br/><br/>"
                    + "You can either use the <strong>Import</strong> button below,<br/>or simply drag-and-drop your downloaded file<br/>"
                    + "onto the main RSC+ window while you are logged in and have the bank open."
                    + "</p><br/></html>",
                osScaleMul(10)));
    importExplanation.setBorder(BorderFactory.createEmptyBorder(osScaleMul(2), 0, 0, 0));
    bankPanel.add(importExplanation);
    SearchUtils.skipSearchText(importExplanation);

    JButton bankImportButton = new JButton("Import Bank Order");
    bankImportButton.setAlignmentY(0.80f);
    bankImportButton.setPreferredSize(osScaleMul(new Dimension(200, 28)));
    bankImportButton.setMinimumSize(osScaleMul(new Dimension(200, 28)));
    bankImportButton.setMaximumSize(osScaleMul(new Dimension(200, 28)));
    bankImportButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            String result = Bank.importBank();
            bankPanelImportLabel.setText(
                String.format(
                    "<html><head><style>p{font-size:%dpx;padding-left:%dpx;}</style></head><p><strong>Status:</strong>&nbsp;"
                        + result.replace(" ", "&nbsp;") // non-breaking space prevents newline
                        + "</p></html>",
                    osScaleMul(10),
                    osScaleMul(7)));
          }
        });

    bankPanel.add(bankImportButton);

    JPanel importPanel = new JPanel();
    bankPanel.add(importPanel);
    importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.X_AXIS));
    importPanel.setPreferredSize(osScaleMul(new Dimension(0, 37)));
    importPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    importPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(10), 0));

    importPanel.add(bankImportButton);
    bankPanelImportLabel = new JLabel(" ");
    bankPanelImportLabel.setAlignmentY(0.7f);
    bankPanelImportLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));
    importPanel.add(bankPanelImportLabel);

    addPanelBottomGlue(bankPanel);

    /*
     * Notifications tab
     */

    notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(notificationPanel, "Notification settings");

    JPanel notificationPanelTrayPopupsPanel = new JPanel();
    notificationPanel.add(notificationPanelTrayPopupsPanel);
    notificationPanelTrayPopupsPanel.setLayout(
        new BoxLayout(notificationPanelTrayPopupsPanel, BoxLayout.Y_AXIS));
    notificationPanelTrayPopupsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    notificationPanelTrayPopupCheckbox =
        addCheckbox("Enable notification tray popups", notificationPanelTrayPopupsPanel);
    notificationPanelTrayPopupCheckbox.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));
    notificationPanelTrayPopupCheckbox.setToolTipText(
        "Shows a system notification when a notification is triggered");

    ButtonGroup trayPopupButtonGroup = new ButtonGroup();
    notificationPanelTrayPopupClientFocusButton =
        addRadioButton(
            "Only when client is not focused", notificationPanelTrayPopupsPanel, osScaleMul(20));
    notificationPanelTrayPopupAnyFocusButton =
        addRadioButton(
            "Regardless of client focus", notificationPanelTrayPopupsPanel, osScaleMul(20));
    trayPopupButtonGroup.add(notificationPanelTrayPopupClientFocusButton);
    trayPopupButtonGroup.add(notificationPanelTrayPopupAnyFocusButton);

    JPanel notificationPanelNotifSoundsPanel = new JPanel();
    notificationPanel.add(notificationPanelNotifSoundsPanel);
    notificationPanelNotifSoundsPanel.setLayout(
        new BoxLayout(notificationPanelNotifSoundsPanel, BoxLayout.Y_AXIS));
    notificationPanelNotifSoundsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    notificationPanelNotifSoundsCheckbox =
        addCheckbox("Enable notification sounds", notificationPanelNotifSoundsPanel);
    notificationPanelNotifSoundsCheckbox.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));
    notificationPanelNotifSoundsCheckbox.setToolTipText(
        "Plays a sound when a notification is triggered");

    ButtonGroup notifSoundButtonGroup = new ButtonGroup();
    notificationPanelNotifSoundClientFocusButton =
        addRadioButton(
            "Only when client is not focused", notificationPanelNotifSoundsPanel, osScaleMul(20));
    notificationPanelNotifSoundAnyFocusButton =
        addRadioButton(
            "Regardless of client focus", notificationPanelNotifSoundsPanel, osScaleMul(20));
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
    notificationPanelPMNotifsCheckbox.setToolTipText(
        "Shows a system notification when a PM is received");

    // PM notifications denylist
    JPanel pmDenylistPanel = new JPanel();
    notificationPanel.add(pmDenylistPanel);
    pmDenylistPanel.setLayout(new BoxLayout(pmDenylistPanel, BoxLayout.X_AXIS));
    pmDenylistPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    pmDenylistPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(9), 0));

    JLabel pmDenyListNameLabel = new JLabel("Disable PM notifications from: ");
    pmDenylistPanel.add(pmDenyListNameLabel);
    pmDenyListNameLabel.setAlignmentY(0.9f);

    notificationPanelPMDenyListTextField = new JTextField();
    pmDenylistPanel.add(notificationPanelPMDenyListTextField);
    notificationPanelPMDenyListTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    notificationPanelPMDenyListTextField.setMaximumSize(
        new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    notificationPanelPMDenyListTextField.setAlignmentY(0.75f);

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
    notificationPanelLowHPNotifsPanel.setPreferredSize(osScaleMul(new Dimension(0, 28)));
    notificationPanelLowHPNotifsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    notificationPanelLowHPNotifsCheckbox =
        addCheckbox("Enable low HP notification at", notificationPanelLowHPNotifsPanel);
    notificationPanelLowHPNotifsCheckbox.setToolTipText(
        "Shows a system notification when your HP drops below the specified value");
    SearchUtils.addSearchMetadata(
        notificationPanelLowHPNotifsCheckbox, CommonMetadata.HP.getText());

    notificationPanelLowHPNotifsSpinner = new JSpinner();
    notificationPanelLowHPNotifsSpinner.setMaximumSize(osScaleMul(new Dimension(55, 22)));
    notificationPanelLowHPNotifsSpinner.setMinimumSize(osScaleMul(new Dimension(55, 22)));
    notificationPanelLowHPNotifsSpinner.setAlignmentY(0.75f);
    notificationPanelLowHPNotifsPanel.add(notificationPanelLowHPNotifsSpinner);
    notificationPanelLowHPNotifsSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    JLabel notificationPanelLowHPNotifsEndLabel = new JLabel("% HP");
    notificationPanelLowHPNotifsPanel.add(notificationPanelLowHPNotifsEndLabel);
    notificationPanelLowHPNotifsEndLabel.setAlignmentY(0.8f);
    notificationPanelLowHPNotifsEndLabel.setBorder(
        BorderFactory.createEmptyBorder(0, osScaleMul(2), 0, 0));

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
    notificationPanelFatigueNotifsPanel.setPreferredSize(osScaleMul(new Dimension(0, 28)));
    notificationPanelFatigueNotifsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    notificationPanelFatigueNotifsCheckbox =
        addCheckbox("Enable high fatigue notifications at", notificationPanelFatigueNotifsPanel);
    notificationPanelFatigueNotifsCheckbox.setToolTipText(
        "Shows a system notification when your fatigue gets past the specified value");

    notificationPanelFatigueNotifsSpinner = new JSpinner();
    notificationPanelFatigueNotifsSpinner.setMaximumSize(osScaleMul(new Dimension(55, 22)));
    notificationPanelFatigueNotifsSpinner.setMinimumSize(osScaleMul(new Dimension(55, 22)));
    notificationPanelFatigueNotifsSpinner.setAlignmentY(0.75f);
    notificationPanelFatigueNotifsPanel.add(notificationPanelFatigueNotifsSpinner);
    notificationPanelFatigueNotifsSpinner.putClientProperty("JComponent.sizeVariant", "mini");

    JLabel notificationPanelFatigueNotifsEndLabel = new JLabel("% fatigue");
    notificationPanelFatigueNotifsPanel.add(notificationPanelFatigueNotifsEndLabel);
    notificationPanelFatigueNotifsEndLabel.setAlignmentY(0.8f);
    notificationPanelFatigueNotifsEndLabel.setBorder(
        BorderFactory.createEmptyBorder(0, osScaleMul(2), 0, 0));

    // Sanitize JSpinner values
    SpinnerNumberModel spinnerFatigueNumModel = new SpinnerNumberModel();
    spinnerFatigueNumModel.setMinimum(1);
    spinnerFatigueNumModel.setMaximum(100);
    spinnerFatigueNumModel.setValue(98);
    notificationPanelFatigueNotifsSpinner.setModel(spinnerFatigueNumModel);

    JPanel notificationPanelGroundItemPanel = new JPanel();
    notificationPanel.add(notificationPanelGroundItemPanel);
    notificationPanelGroundItemPanel.setLayout(
        new BoxLayout(notificationPanelGroundItemPanel, BoxLayout.Y_AXIS));
    notificationPanelGroundItemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel warnHighlightedOnGroundPanel = new JPanel();
    notificationPanelGroundItemPanel.add(warnHighlightedOnGroundPanel);
    warnHighlightedOnGroundPanel.setLayout(
        new BoxLayout(warnHighlightedOnGroundPanel, BoxLayout.X_AXIS));
    warnHighlightedOnGroundPanel.setPreferredSize(osScaleMul(new Dimension(0, 28)));
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
    notificationPanelGroundItemPanel.add(highlightedItemsSuggestionJLabel);
    highlightedItemsSuggestionJLabel.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(8), 0));

    notificationPanelHighlightedItemTimerSpinner = new JSpinner();
    notificationPanelHighlightedItemTimerSpinner.setMaximumSize(osScaleMul(new Dimension(65, 22)));
    notificationPanelHighlightedItemTimerSpinner.setMinimumSize(osScaleMul(new Dimension(65, 22)));
    notificationPanelHighlightedItemTimerSpinner.setAlignmentY(0.75f);
    warnHighlightedOnGroundPanel.add(notificationPanelHighlightedItemTimerSpinner);
    notificationPanelHighlightedItemTimerSpinner.putClientProperty(
        "JComponent.sizeVariant", "mini");

    JLabel notificationPanelHighlightedItemEndLabel = new JLabel("seconds");
    warnHighlightedOnGroundPanel.add(notificationPanelHighlightedItemEndLabel);
    notificationPanelHighlightedItemEndLabel.setAlignmentY(0.8f);
    int secondsMargin = isUsingFlatLAFTheme() ? 4 : 2;
    notificationPanelHighlightedItemEndLabel.setBorder(
        BorderFactory.createEmptyBorder(0, osScaleMul(secondsMargin), 0, 0));

    // Sanitize JSpinner values
    SpinnerNumberModel highlightedItemSecondsModel = new SpinnerNumberModel();
    highlightedItemSecondsModel.setMinimum(0);
    highlightedItemSecondsModel.setMaximum(630); // 10.5 minutes max
    highlightedItemSecondsModel.setValue(100);
    notificationPanelHighlightedItemTimerSpinner.setModel(highlightedItemSecondsModel);

    int messagesTextHeight = isUsingFlatLAFTheme() ? 32 : 37;

    // Important messages
    JPanel importantMessagesPanel = new JPanel();
    notificationPanel.add(importantMessagesPanel);
    importantMessagesPanel.setLayout(new BoxLayout(importantMessagesPanel, BoxLayout.X_AXIS));
    importantMessagesPanel.setPreferredSize(osScaleMul(new Dimension(0, messagesTextHeight)));
    importantMessagesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    importantMessagesPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(9), 0));

    JLabel importantMessagesNameLabel = new JLabel("Important Messages: ");
    importantMessagesPanel.add(importantMessagesNameLabel);
    importantMessagesNameLabel.setAlignmentY(0.9f);

    importantMessagesTextField = new JTextField();
    importantMessagesPanel.add(importantMessagesTextField);
    importantMessagesTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    importantMessagesTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    importantMessagesTextField.setAlignmentY(0.75f);

    // Important sad messages
    JPanel importantSadMessagesPanel = new JPanel();
    notificationPanel.add(importantSadMessagesPanel);
    importantSadMessagesPanel.setLayout(new BoxLayout(importantSadMessagesPanel, BoxLayout.X_AXIS));
    importantSadMessagesPanel.setPreferredSize(osScaleMul(new Dimension(0, messagesTextHeight)));
    importantSadMessagesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    importantSadMessagesPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(9), 0));

    JLabel importantSadMessagesNameLabel = new JLabel("Important Messages (sad noise): ");
    importantSadMessagesPanel.add(importantSadMessagesNameLabel);
    importantSadMessagesNameLabel.setAlignmentY(0.9f);

    importantSadMessagesTextField = new JTextField();
    importantSadMessagesPanel.add(importantSadMessagesTextField);
    importantSadMessagesTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    importantSadMessagesTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    importantSadMessagesTextField.setAlignmentY(0.75f);

    notificationPanelMuteImportantMessageSoundsCheckbox =
        addCheckbox("Mute the alert sound even if it's an important message", notificationPanel);
    notificationPanelMuteImportantMessageSoundsCheckbox.setToolTipText(
        "Muting for Important Messages (defined in text fields above)");

    addPanelBottomGlue(notificationPanel);

    /*
     * Streaming & Privacy tab
     */

    streamingPanel.setLayout(new BoxLayout(streamingPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(streamingPanel, "Streaming & Privacy");

    streamingPanelTwitchChatIntegrationEnabledCheckbox =
        addCheckbox("Enable Twitch chat integration", streamingPanel);
    streamingPanelTwitchChatIntegrationEnabledCheckbox.setToolTipText(
        "If this box is checked, and the 3 relevant text fields are filled out, you will connect to a chat channel on login.");

    streamingPanelTwitchChatCheckbox = addCheckbox("Hide incoming Twitch chat", streamingPanel);
    streamingPanelTwitchChatCheckbox.setToolTipText(
        "Don't show chat from other Twitch users, but still be able to send Twitch chat");

    int twitchTextHeight = isUsingFlatLAFTheme() ? 32 : 37;

    JPanel streamingPanelTwitchChannelNamePanel = new JPanel();
    streamingPanel.add(streamingPanelTwitchChannelNamePanel);
    streamingPanelTwitchChannelNamePanel.setLayout(
        new BoxLayout(streamingPanelTwitchChannelNamePanel, BoxLayout.X_AXIS));
    streamingPanelTwitchChannelNamePanel.setPreferredSize(
        osScaleMul(new Dimension(0, twitchTextHeight)));
    streamingPanelTwitchChannelNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    streamingPanelTwitchChannelNamePanel.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));

    JLabel streamingPanelTwitchChannelNameLabel = new JLabel("Twitch channel to chat in: ");
    streamingPanelTwitchChannelNameLabel.setToolTipText("The Twitch channel you want to chat in");
    streamingPanelTwitchChannelNamePanel.add(streamingPanelTwitchChannelNameLabel);
    streamingPanelTwitchChannelNameLabel.setAlignmentY(0.9f);

    streamingPanelTwitchChannelNameTextField = new JTextField();
    streamingPanelTwitchChannelNamePanel.add(streamingPanelTwitchChannelNameTextField);
    streamingPanelTwitchChannelNameTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    streamingPanelTwitchChannelNameTextField.setMaximumSize(
        new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    streamingPanelTwitchChannelNameTextField.setAlignmentY(0.75f);

    JPanel streamingPanelTwitchUserPanel = new JPanel();
    streamingPanel.add(streamingPanelTwitchUserPanel);
    streamingPanelTwitchUserPanel.setLayout(
        new BoxLayout(streamingPanelTwitchUserPanel, BoxLayout.X_AXIS));
    streamingPanelTwitchUserPanel.setPreferredSize(osScaleMul(new Dimension(0, twitchTextHeight)));
    streamingPanelTwitchUserPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    streamingPanelTwitchUserPanel.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));

    JLabel streamingPanelTwitchUserLabel = new JLabel("Your Twitch username: ");
    streamingPanelTwitchUserLabel.setToolTipText("The Twitch username you log into Twitch with");
    streamingPanelTwitchUserPanel.add(streamingPanelTwitchUserLabel);
    streamingPanelTwitchUserLabel.setAlignmentY(0.9f);

    streamingPanelTwitchUserTextField = new JTextField();
    streamingPanelTwitchUserPanel.add(streamingPanelTwitchUserTextField);
    streamingPanelTwitchUserTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    streamingPanelTwitchUserTextField.setMaximumSize(
        new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    streamingPanelTwitchUserTextField.setAlignmentY(0.75f);

    JPanel streamingPanelTwitchOAuthPanel = new JPanel();
    streamingPanel.add(streamingPanelTwitchOAuthPanel);
    streamingPanelTwitchOAuthPanel.setLayout(
        new BoxLayout(streamingPanelTwitchOAuthPanel, BoxLayout.X_AXIS));
    streamingPanelTwitchOAuthPanel.setPreferredSize(osScaleMul(new Dimension(0, twitchTextHeight)));
    streamingPanelTwitchOAuthPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    streamingPanelTwitchOAuthPanel.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));

    JLabel streamingPanelTwitchOAuthLabel =
        new JLabel("Your Twitch OAuth token (not your Stream key): ");
    streamingPanelTwitchOAuthLabel.setToolTipText("Your Twitch OAuth token (not your Stream Key)");
    streamingPanelTwitchOAuthPanel.add(streamingPanelTwitchOAuthLabel);
    streamingPanelTwitchOAuthLabel.setAlignmentY(0.9f);

    streamingPanelTwitchOAuthTextField = new JPasswordField();
    streamingPanelTwitchOAuthPanel.add(streamingPanelTwitchOAuthTextField);
    streamingPanelTwitchOAuthTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    streamingPanelTwitchOAuthTextField.setMaximumSize(
        new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    streamingPanelTwitchOAuthTextField.setAlignmentY(0.75f);

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
        new JLabel(
            String.format(
                "<html><head><style>p{font-size:%dpx;}</style></head><p>&nbsp;</p>",
                osScaleMul(5)));
    streamingPanel.add(spacerLabel);

    addSettingsHeader(streamingPanel, "Speedrunner Mode");

    JPanel streamingPanelSpeedrunPanel = new JPanel();
    streamingPanel.add(streamingPanelSpeedrunPanel);
    streamingPanelSpeedrunPanel.setLayout(
        new BoxLayout(streamingPanelSpeedrunPanel, BoxLayout.Y_AXIS));
    streamingPanelSpeedrunPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel speedrunnerModeExplanation =
        new JLabel(
            String.format(
                "<html><head><style>p{font-size:%dpx;}ul{list-style-type:none;padding-left:0px;margin-left:0px;}</style></head>"
                    + "<p>Speedrunner mode keeps track of your precise time spent in game <br/>"
                    + "between the first player update packet received and either logout or<br/>"
                    + "upon completing any of the following goals:<br/><ul>"
                    + "<li>● Completion of Tutorial Island</li>"
                    + "<li>● Completion of Black Knight's Fortress</li>"
                    + "<li>● Entrance to the Champion's Guild</li>"
                    + "<li>● Completion of Dragon Slayer</li></ul></p>"
                    + "<p>Speedrunner mode also overrides the following RSC+ options:<ul>"
                    + "<li>● You will always be recording a replay</li>"
                    + "<li>● You will not be able to desync the camera position from the player position (too weird)</li>"
                    + "<li>● Keyboard shortcut to trigger sleeping bag is disabled</li>"
                    + "<li>● Prayer & Magic scrollbars will reset to the top when switching between those tabs</li>"
                    + "<li>● Menu item swapping (e.g. \"Always left click to attack\") is disabled "
                    + "<ul style=\"padding:0px; margin: %dpx 0 0 %dpx;\">"
                    + "<li style=\"padding:0px; margin:0px;\">○ REQUIRES RESTART IF NOT ALREADY DISABLED</li></ul></li></ul></p>"
                    + "<p>The below box should be manually clicked before logging in to a new character.<br/> "
                    + "The apply button must be clicked for it to take effect.</p><br/></html>",
                osScaleMul(10), osScaleMul(2), osScaleMul(10)));
    streamingPanelSpeedrunPanel.add(speedrunnerModeExplanation);
    SearchUtils.skipSearchText(speedrunnerModeExplanation);

    streamingPanelSpeedrunnerCheckbox =
        addCheckbox("Activate Speedrunner Mode", streamingPanelSpeedrunPanel);
    streamingPanelSpeedrunnerCheckbox.setToolTipText("Speedrunner Mode, see above explanation");

    JLabel speedrunnerHowToSTOPSPEEDRUNNINGGGGExplanation =
        new JLabel(
            String.format(
                "<html><head><style>p{font-size:%dpx; padding-bottom: %dpx;}</style></head><p>When you are satisfied that your run is over, end the speedrun<br/> by sending the command <font face=\"courier\"><strong>::endrun</strong></font> or press the configurable keybind <strong>&lt;CTRL-END&gt;</strong>.</p></html>",
                osScaleMul(10), osScaleMul(5)));
    streamingPanelSpeedrunPanel.add(speedrunnerHowToSTOPSPEEDRUNNINGGGGExplanation);
    SearchUtils.skipSearchText(speedrunnerHowToSTOPSPEEDRUNNINGGGGExplanation);

    /* shame to write all this code and then not need it...
    JPanel streamingPanelSpeedRunnerNamePanel = new JPanel();
    streamingPanel.add(streamingPanelSpeedRunnerNamePanel);
    streamingPanelSpeedRunnerNamePanel.setLayout(
        new BoxLayout(streamingPanelSpeedRunnerNamePanel, BoxLayout.X_AXIS));
    streamingPanelSpeedRunnerNamePanel.setPreferredSize(osScaleMul(new Dimension(0, 37)));
    streamingPanelSpeedRunnerNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    streamingPanelSpeedRunnerNamePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(9), 0));

    JLabel streamingPanelSpeedRunnerUsernameLabel = new JLabel("Speedrunner username: ");
    streamingPanelSpeedRunnerUsernameLabel.setToolTipText("Only apply speedrunner restrictions/enhancements if the username in this field matches.");
    streamingPanelSpeedRunnerNamePanel.add(streamingPanelSpeedRunnerUsernameLabel);
    streamingPanelSpeedRunnerUsernameLabel.setAlignmentY(0.9f);

    streamingPanelSpeedrunnerUsernameTextField = new JTextField();
    streamingPanelSpeedRunnerNamePanel.add(streamingPanelSpeedrunnerUsernameTextField);
    streamingPanelSpeedrunnerUsernameTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    streamingPanelSpeedrunnerUsernameTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    streamingPanelSpeedrunnerUsernameTextField.setAlignmentY(0.75f);
    */

    addPanelBottomGlue(streamingPanel);

    /*
     * Keybind tab
     */
    JPanel keybindContainerPanel = new JPanel(new GridBagLayout());
    // Since keybind labels and buttons aren't in panels, skip the grouping
    SearchUtils.bypassPanelGrouping(keybindContainerPanel);

    JPanel keybindContainerContainerPanel = new JPanel(new GridBagLayout());
    SearchUtils.bypassPanelGrouping(
        keybindContainerContainerPanel); // Same as above, for the inner grouping

    GridBagConstraints con = new GridBagConstraints();
    con.gridy = 0;
    con.gridx = 0;
    con.fill = GridBagConstraints.HORIZONTAL;

    GridBagConstraints gbl_constraints = new GridBagConstraints();
    gbl_constraints.fill = GridBagConstraints.HORIZONTAL;
    gbl_constraints.anchor = GridBagConstraints.FIRST_LINE_START;
    gbl_constraints.weightx = 1;
    gbl_constraints.ipadx = 20;
    gbl_constraints.gridy = 0;
    gbl_constraints.gridwidth = 3;

    // Note: CTRL + every single letter on the keyboard is now used
    // consider using ALT instead.

    addKeybindCategory(keybindContainerPanel, "General");
    addKeybindSet(
        keybindContainerPanel,
        "Sleep",
        "sleep",
        KeyModifier.CTRL,
        KeyEvent.VK_SPACE,
        "sleeping bag");
    addKeybindSet(keybindContainerPanel, "Logout", "logout", KeyModifier.CTRL, KeyEvent.VK_L);
    addKeybindSet(
        keybindContainerPanel, "Take screenshot", "screenshot", KeyModifier.CTRL, KeyEvent.VK_S);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle scaling",
        "toggle_scaling",
        KeyModifier.ALT,
        KeyEvent.VK_S,
        "scale");
    addKeybindSet(
        keybindContainerPanel,
        "Increase scale",
        "increase_scale",
        KeyModifier.ALT,
        KeyEvent.VK_UP,
        "scaling");
    addKeybindSet(
        keybindContainerPanel,
        "Decrease scale",
        "decrease_scale",
        KeyModifier.ALT,
        KeyEvent.VK_DOWN,
        "scaling");
    addKeybindSet(
        keybindContainerPanel,
        "Show settings window",
        "show_config_window",
        KeyModifier.CTRL,
        KeyEvent.VK_O);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle numbered dialogue",
        "toggle_numbered_dialogue",
        KeyModifier.ALT,
        KeyEvent.VK_B);
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
        "Toggle colour coded text",
        "toggle_colorize",
        KeyModifier.CTRL,
        KeyEvent.VK_Z,
        "color");
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
    addKeybindSet(
        keybindContainerPanel,
        "Toggle trackpad camera rotation",
        "toggle_trackpad_camera_rotation",
        KeyModifier.ALT,
        KeyEvent.VK_D);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle ctrl to scroll chat history",
        "toggle_ctrl_scroll",
        KeyModifier.ALT,
        KeyEvent.VK_H);
    addKeybindCategory(keybindContainerPanel, "Overlays");
    addKeybindSet(
        keybindContainerPanel,
        "Toggle HP/prayer/fatigue display",
        "toggle_hpprayerfatigue_display",
        KeyModifier.CTRL,
        KeyEvent.VK_U,
        CommonMetadata.HP.getText());
    addKeybindSet(
        keybindContainerPanel,
        "Toggle combat buffs and cooldowns display         ", // TODO: remove this spacing
        "toggle_buffs_display",
        KeyModifier.CTRL,
        KeyEvent.VK_Y);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle items kept on death display",
        "toggle_death_items",
        KeyModifier.CTRL,
        KeyEvent.VK_D);
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
        KeyEvent.VK_T,
        "colors");
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
        KeyEvent.VK_F,
        "frames per second");
    addKeybindSet(
        keybindContainerPanel,
        "Toggle item name overlay",
        "toggle_item_name_overlay",
        KeyModifier.CTRL,
        KeyEvent.VK_G);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle item name overlay (highlighted only)",
        "toggle_item_name_overlay_highlight",
        KeyModifier.ALT,
        KeyEvent.VK_G);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle player name overlay",
        "toggle_player_name_overlay",
        KeyModifier.CTRL,
        KeyEvent.VK_P);
    addKeybindSet(
        keybindContainerPanel,
        "Toggle own name overlay",
        "toggle_own_name_overlay",
        KeyModifier.ALT,
        KeyEvent.VK_J);
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
        "Toggle time until health regen",
        "toggle_health_regen_timer",
        KeyModifier.CTRL,
        KeyEvent.VK_X,
        CommonMetadata.HP.getText());
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
        KeyEvent.VK_C);

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

    addPanelBottomGlue(keybindPanel);

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

    int replayTextHeight = isUsingFlatLAFTheme() ? 32 : 37;

    JPanel replayPanelReplayFolderBasePathTextFieldPanel = new JPanel();
    replayPanel.add(replayPanelReplayFolderBasePathTextFieldPanel);
    replayPanelReplayFolderBasePathTextFieldPanel.setLayout(
        new BoxLayout(replayPanelReplayFolderBasePathTextFieldPanel, BoxLayout.X_AXIS));
    replayPanelReplayFolderBasePathTextFieldPanel.setPreferredSize(
        osScaleMul(new Dimension(0, replayTextHeight)));
    replayPanelReplayFolderBasePathTextFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    replayPanelReplayFolderBasePathTextFieldPanel.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(9), 0));

    JLabel replayPanelReplayFolderBasePathTextFieldLabel = new JLabel("Replay Folder Location: ");
    replayPanelReplayFolderBasePathTextFieldLabel.setToolTipText(
        "Any string of characters you enter into this field will be removed from the Folder Path column in the Replay Queue window.");
    replayPanelReplayFolderBasePathTextFieldPanel.add(
        replayPanelReplayFolderBasePathTextFieldLabel);
    replayPanelReplayFolderBasePathTextFieldLabel.setAlignmentY(0.9f);

    replayPanelReplayFolderBasePathTextField = new JTextField();
    replayPanelReplayFolderBasePathTextFieldPanel.add(replayPanelReplayFolderBasePathTextField);
    replayPanelReplayFolderBasePathTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    replayPanelReplayFolderBasePathTextField.setMaximumSize(
        new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    replayPanelReplayFolderBasePathTextField.setAlignmentY(0.75f);

    JPanel replayPanelDateFormatTextFieldPanel = new JPanel();
    replayPanel.add(replayPanelDateFormatTextFieldPanel);
    replayPanelDateFormatTextFieldPanel.setLayout(
        new BoxLayout(replayPanelDateFormatTextFieldPanel, BoxLayout.X_AXIS));
    replayPanelDateFormatTextFieldPanel.setPreferredSize(
        osScaleMul(new Dimension(0, replayTextHeight)));
    replayPanelDateFormatTextFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    replayPanelDateFormatTextFieldPanel.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(9), 0));

    JLabel replayPanelDateFormatTextFieldLabel = new JLabel("Preferred Date Format: ");
    replayPanelDateFormatTextFieldLabel.setToolTipText(
        "This is the date string pattern that you personally prefer. If you're not sure what your options are, check https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html");
    replayPanelDateFormatTextFieldPanel.add(replayPanelDateFormatTextFieldLabel);
    replayPanelDateFormatTextFieldLabel.setAlignmentY(0.9f);

    replayPanelDateFormatTextField = new JTextField();
    replayPanelDateFormatTextFieldPanel.add(replayPanelDateFormatTextField);
    replayPanelDateFormatTextField.setMinimumSize(osScaleMul(new Dimension(100, 28)));
    replayPanelDateFormatTextField.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(28)));
    replayPanelDateFormatTextField.setAlignmentY(0.75f);

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

    addPanelBottomGlue(replayPanel);

    /*
     * Presets tab
     */
    presetsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    presetsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    presetsPanel.setLayout(new BoxLayout(presetsPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(presetsPanel, "Presets");
    JPanel presetsPanelPresetSelectionPanel = new JPanel();
    presetsPanel.add(presetsPanelPresetSelectionPanel);
    presetsPanelPresetSelectionPanel.setLayout(
        new BoxLayout(presetsPanelPresetSelectionPanel, BoxLayout.Y_AXIS));
    presetsPanelPresetSelectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    presetsPanelCustomSettingsCheckbox =
        addCheckbox("Use saved settings", presetsPanelPresetSelectionPanel);
    presetsPanelCustomSettingsCheckbox.setToolTipText(
        "Loads settings stored in the config.ini file");

    JPanel presetsPanelPresetSliderPanel = new JPanel();
    presetsPanelPresetSliderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    presetsPanelPresetSliderPanel.setMaximumSize(osScaleMul(new Dimension(300, 175)));
    presetsPanelPresetSliderPanel.setLayout(
        new BoxLayout(presetsPanelPresetSliderPanel, BoxLayout.X_AXIS));
    presetsPanelPresetSelectionPanel.add(presetsPanelPresetSliderPanel);

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
    presetsPanelPresetSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
    presetsPanelPresetSlider.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(5), osScaleMul(70)));
    presetsPanelPresetSlider.setOrientation(SwingConstants.VERTICAL);
    presetsPanelPresetSlider.setToolTipText(
        "A preset will only apply until the client has been closed - save it permanently with the button below");

    presetsPanelPresetSlider.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) { // When the slider has finished moving
              // Only allow 'reset current presets' on the current preset to avoid edge case issues
              if (Settings.presetModified && !presetsPanelCustomSettingsCheckbox.isSelected()) {
                resetPresetsButton.setEnabled(
                    Settings.presetTable.get(source.getValue()).equals(Settings.currentProfile));
              }
            }
          }
        });

    if (Util.isUsingFlatLAFTheme()) {
      presetsPanelPresetSliderPanel.add(Box.createHorizontalStrut(osScaleMul(35)));
    }
    presetsPanelPresetSliderPanel.add(presetsPanelPresetSlider);

    presetsPanelCurrentPresetLabel = new JLabel(" ");
    presetsPanelCurrentPresetLabel.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(5), osScaleMul(20), osScaleMul(5), 0));
    presetsPanelCurrentPresetLabel.setToolTipText("Displays the currently-active preset");
    presetsPanelPresetSelectionPanel.add(presetsPanelCurrentPresetLabel);

    JPanel presetsButtonPanel = new JPanel();
    presetsButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    presetsButtonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(50)));
    presetsButtonPanel.setBorder(
        BorderFactory.createEmptyBorder(osScaleMul(7), osScaleMul(10), osScaleMul(10), 0));
    presetsButtonPanel.setLayout(new BoxLayout(presetsButtonPanel, BoxLayout.X_AXIS));

    replaceConfigButton =
        addButton("Save Current Preset to settings", presetsButtonPanel, Component.LEFT_ALIGNMENT);
    replaceConfigButton.setToolTipText(
        "Replaces your saved settings with the currently-active preset, including any customizations");
    replaceConfigButton.setEnabled(false);
    replaceConfigButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            String confirmPresetDefaultMessage =
                "<b>Warning</b>: this will delete your old settings!<br/>"
                    + "<br/>"
                    + "Are you sure you want to delete your old settings?";
            JPanel confirmPresetDefaultPanel =
                Util.createOptionMessagePanel(confirmPresetDefaultMessage);

            int choice =
                JOptionPane.showConfirmDialog(
                    Launcher.getConfigWindow().frame,
                    confirmPresetDefaultPanel,
                    "Confirm",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.CLOSED_OPTION || choice == JOptionPane.NO_OPTION) {
              return;
            }

            Settings.save(Settings.currentProfile);
            Settings.initSettings();
            Settings.presetModified = false;
            synchronizeGuiValues();
          }
        });

    if (Util.isUsingFlatLAFTheme()) {
      presetsButtonPanel.add(Box.createRigidArea(osScaleMul(new Dimension(10, 0))));
    }

    resetPresetsButton =
        addButton("Reset Current Preset", presetsButtonPanel, Component.RIGHT_ALIGNMENT);
    resetPresetsButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Logger.Info("Try saying that 10 times fast...");
            Settings.initSettings();
            applySettings();
          }
        });
    resetPresetsButton.setToolTipText("Resets the currently-active preset to its default settings");
    resetPresetsButton.setEnabled(false);
    presetsButtonPanel.add(Box.createHorizontalGlue());
    presetsPanelPresetSelectionPanel.add(presetsButtonPanel);

    presetsPanelCustomSettingsCheckbox.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            synchronizePresetOptions();
          }
        });

    addPanelBottomGlue(presetsPanel);

    // World List Tab
    worldListPanel.setLayout(new BoxLayout(worldListPanel, BoxLayout.Y_AXIS));
    worldListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    worldListPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    addSettingsHeaderCentered(worldListPanel, "World List");

    JLabel spacingLabel = new JLabel("");
    spacingLabel.setBorder(BorderFactory.createEmptyBorder(osScaleMul(15), 0, 0, 0));
    worldListPanel.add(spacingLabel);
    SearchUtils.setUnsearchable(spacingLabel);

    for (int i = 1; i <= Settings.WORLDS_TO_DISPLAY; i++) {
      addWorldFields(i);
    }
    addAddWorldButton();

    addPanelBottomGlue(worldListPanel);

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
          new JLabel(
              new ImageIcon(
                  rscplusLogo.getScaledInstance(
                      osScaleMul(215), osScaleMul(215), Image.SCALE_SMOOTH)));
      rscplusLogoJLabel.setBorder(
          BorderFactory.createEmptyBorder(0, osScaleMul(10), osScaleMul(20), osScaleMul(40)));
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
                "<html><div style=\"font-size:%dpx; padding-bottom:%dpx;\"<b>RSC</b>Plus</div><div style=\"font-size:%dpx;\">v%8.6f </div></html>",
                osScaleMul(45), osScaleMul(10), osScaleMul(20), Settings.VERSION_NUMBER));

    rightPane.add(RSCPlusText);

    cR.gridy = 1;

    JLabel aboutText =
        new JLabel(
            String.format(
                "<html><head><style>p{font-size:%dpx; padding-top:%dpx;}ul{list-style-type:none;padding-left:0px;margin-left:0px;}</style></head>"
                    + "<p><b>RSC</b>Plus is a RuneLite-like client "
                    + "based on the 234 RSC client.<br/> Learn more at https://rsc.plus.<br/><br/>"
                    + "Thanks to the authors who made this software possible:<br/><ul>"
                    + "<li><b>● Ornox</b>, for creating the client & most of its features</li>"
                    + "<li><b>● Logg</b>, currently maintains RSC+, new interfaces & improvements</li>"
                    + "<li><b>● Brian</b>, who laid a lot of the groundwork for the user interface</li>"
                    + "<li><b>● conker</b>, client scaling, fonts, general UX, & many other improvements</li>"
                    + "<li><b>● Luis</b>, who found a lot of important hooks & fixed a lot of bugs</li>"
                    + "<li><b>● Talkarcabbage</b>, generic notifications, UI backend, & keybind overhaul</li>"
                    + "<li><b>● nickzuber</b>, fixed some bugs, highlight/block items</li>"
                    + "<li><b>● Ryan</b>, keybind to reset camera zoom, toggle statuses in corner always</li>"
                    + "<li><b>● Yumeko</b>, fixed Twitch chat integration in 2023</li>"
                    + "<li><b>● sammy123k</b>, added an option to center the XP progress bar</li>"
                    + "<li><b>● The Jagex team of 2000 to 2004</b></li>"
                    + "</ul></p></html>",
                osScaleMul(10), osScaleMul(15)));

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

    addPanelBottomGlue(authorsPanel);

    // Joystick Tab

    joystickPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    joystickPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    joystickPanel.setLayout(new BoxLayout(joystickPanel, BoxLayout.Y_AXIS));

    addSettingsHeader(joystickPanel, "Explanation");

    JLabel joystickExplanation =
        new JLabel(
            String.format(
                "<html><head><style>p{font-size:%dpx;}</style></head><p>"
                    + "Currently, RSC+ is compatible with only the 3DConnexion Space Navigator 3D Mouse.<br/>"
                    + "It is used to enable a 5 degree of freedom camera (roll left/right is omitted).<br/><br/>"
                    + "This setting does not allow you to move the player with a joystick or perform in-game actions.<br/><br/>"
                    + "If you do not have a 3DConnexion Space Navigator 3D Mouse, you should not enable this setting."
                    + "</p><br/></html>",
                osScaleMul(10)));
    joystickPanel.add(joystickExplanation);
    SearchUtils.skipSearchText(joystickExplanation);

    addSettingsHeader(joystickPanel, "Joystick");
    joystickPanelJoystickEnabledCheckbox =
        addCheckbox(
            "Enable Joystick polling (Performance decreased if not using joystick)", joystickPanel);
    joystickPanelJoystickEnabledCheckbox.setToolTipText("Enable Joystick polling once every frame");
    joystickPanelJoystickEnabledCheckbox.setBorder(
        BorderFactory.createEmptyBorder(0, 0, osScaleMul(7), 0));

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
          JLabel joystickInputspacerLabel = new JLabel("<html><br/></html>");
          joystickPanel.add(joystickInputspacerLabel);

          // Don't index any of these for searching
          SearchUtils.skipSearchText(value);
          SearchUtils.skipSearchText(joystickInputValueJlabels.get(key));
          SearchUtils.skipSearchText(joystickInputspacerLabel);
        });

    addPanelBottomGlue(joystickPanel);

    //// End component creation ////

    // As a final step, index all components for searching
    indexSearch();
  }

  /** Resets the tooltip listener state */
  private void resetToolTipListener() {
    toolTipTextString = " ";
    toolTipTextLabel.setText(toolTipInitText);
    resetToolTipBarPanelColors();
    removeConfigWindowEventQueueListener();
  }

  /** Resets the tooltip bar panel colors */
  private void resetToolTipBarPanelColors() {
    if (Util.isDarkThemeFlatLAF()) {
      toolTipPanel.setBackground(new Color(52, 56, 58));
    } else if (Util.isLightThemeFlatLAF()) {
      toolTipPanel.setBackground(new Color(235, 235, 235));
    } else {
      toolTipPanel.setBackground(new Color(233, 236, 242));
    }
  }

  /** Collection of frequently-used metadata values */
  private enum CommonMetadata {
    HP("health", "hits", "hp"),
    SFX("sfx", "sound effects"),
    FPS("fps", "frames per second"),
    COLOUR("colour", "color"),
    COLOURS("colours", "colors");

    public final String text;

    CommonMetadata(String... text) {
      this.text = String.join(" ", text);
    }

    public String getText() {
      return text;
    }
  }

  /**
   * Given a {@link ConfigTab} object, get the tab index
   *
   * @param configTab {@link ConfigTab} for which to get the index
   * @return Tab index for the ConfigTab
   */
  private int getTabIndex(ConfigTab configTab) {
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      if (tabbedPane.getTitleAt(i).equals(configTab.getLabel())) {
        return i;
      }
    }

    return -1;
  }

  /**
   * Sets the current active tab, as if the user clicked on it. Primarily used for tab focusing
   * logic during searches.
   *
   * @param tabIndex {@link ConfigTab} index
   */
  protected void setInitiatedTab(int tabIndex) {
    searchInitiatedTabIndex = tabIndex;
  }

  /**
   * Indexes all swing components within the ConfigWindow for searching.
   *
   * <p>The search indexing process works by scanning all swing components, constructing a {@link
   * SearchItem} for each individual or logically-grouped set of components it finds, and finally
   * caching each SearchItem into the {@link #searchItemsMap}.
   *
   * <p>A SearchItem object holds search-related data found during the indexing process, notably a
   * list of the actual swing components belonging to it, their visibility states, and the compiled
   * searchable text. When a search is performed, the user's input is tested against the searchable
   * text within each SearchItem, hiding all components that do not match and restoring visibility
   * to components that do, if they were visible prior to the search. Config tabs which no longer
   * contain any visible components will be temporarily disabled.
   *
   * <p>The searchable text for each SearchItem is a composite of all displayed, tooltip, metadata,
   * and user-entered text for all components belonging to the SearchItem, as well as any related
   * components that a developer may choose to assign. Metadata text is only used for searching and
   * is never displayed to the user. It exists primarily as a means to enhance search usability and
   * may be added to a component via {@link SearchUtils#addSearchMetadata(JComponent, String...)}.
   * By contrast, there may be situations where it is not desirable to include text from a
   * particular component within a SearchItem, such as long description labels. This may be
   * accomplished via {@link SearchUtils#skipSearchText(JComponent)}.
   *
   * <p>During indexing, components are automatically grouped together within a SearchItem when they
   * belong to the same parent JPanel within a panel tab. As such, JPanels <i>should</i> be used to
   * group together swing components that all belong to the same logical "setting". However, this
   * may not always be possible due to various layout-related constraints; for this reason, separate
   * components may alternatively be grouped together via {@link
   * SearchUtils#setRelatedSearchComponent(JComponent, Component)}. On the other hand, there may be
   * cases where it is not desired to group items within a JPanel into a single SearchItem at all;
   * this can be accomplished via {@link SearchUtils#bypassPanelGrouping(JPanel)}.
   *
   * <p>Certain components such as section headers are excluded from the entire search process by
   * design. This has a net effect of preventing search text matching and the ability to become
   * hidden during a search, as well as not counting towards the config tab disablement
   * calculations. Components needing to follow this behavior may be marked via {@link
   * SearchUtils#setUnsearchable(JComponent)}.
   *
   * <p>Although initial indexing occurs during application startup, there are conditions under
   * which it must be repeated in order for a search to reflect the most up-to-date ConfigWindow GUI
   * state. These primarily include times when searchable text has to be updated, such as when a
   * user modifies the value for a TextField element, or when the GUI layout itself has been altered
   * due to changes in component visibility states or the addition of new components such as world
   * fields. In order to properly reindex components, the {@link #reindexSearch(UIChangeMethod)}
   * method <b>must</b> be invoked, as it appropriately handles logic surrounding the current search
   * state. As such, any code which results in the need for reindexing must be passed to this method
   * as a lambda expression.
   */
  private void indexSearch() {
    // Reset search indexes
    searchItemsMap = new HashMap<>();
    allSearchComponents = new ArrayList<>();

    HashMap<ConfigTab, JPanel> configTabMap = new HashMap<>();

    // Build a map of all tab panel components
    Arrays.stream(tabbedPane.getComponents())
        .forEach(
            tab -> {
              JPanel tabPanel = ((JPanel) ((JScrollPane) tab).getViewport().getComponent(0));
              configTabMap.put(ConfigTab.valueOf(tabPanel.getName()), tabPanel);
            });

    // Index components within each tab panel, creating SearchItems per component and grouping
    // all components within JPanels into a single SearchItem with combined search terms
    configTabMap
        .keySet()
        .forEach(
            configTab -> {
              searchItemsMap.put(configTab, new ArrayList<>());

              getAllComponents(configTabMap.get(configTab))
                  .forEach(
                      component -> {
                        if (component.getClass().equals(JPanel.class)) {
                          SearchItem panelSearchItem = new SearchItem(configTab);
                          JPanel panelComponent = (JPanel) component;

                          // Add the panel component itself to the components list
                          panelSearchItem.addComponent(panelComponent, panelComponent.isVisible());

                          // Get and add all the panel's children to the components list
                          getAllComponents(panelComponent)
                              .forEach(
                                  childComponent ->
                                      panelSearchItem.addComponent(
                                          childComponent, childComponent.isVisible()));

                          // Index the panel component
                          indexPanelComponent(configTab, panelSearchItem, panelComponent);
                        } else {
                          indexComponent(configTab, component);
                        }
                      });
            });

    reindexing = false;
  }

  /**
   * Indexes a found {@link JPanel} component for searching.
   *
   * <p>The panel's children will be grouped together for the purpose of searching; all search text
   * will be combined such that a match on any of the child components will result in the visibility
   * of the entire panel.
   *
   * @param configTab {@link ConfigTab} which owns the panel component
   * @param panelSearchItem {@link SearchItem} instance for the panel
   * @param panelComponent The {@link JPanel} instance itself
   */
  private void indexPanelComponent(
      ConfigTab configTab, SearchItem panelSearchItem, JPanel panelComponent) {
    // When the "bypass panel grouping" flag has been set on a parent panel, components within the
    // panel will not be grouped and will be found later in the component hierarchy and indexed
    // separately.
    if (SearchUtils.shouldBypassPanelGrouping(panelComponent)) {
      return;
    }

    // Already indexed by a parent panel
    if (allSearchComponents.contains(panelComponent)) {
      return;
    }

    // Combine all child search text into the panel SearchItem (including the panel component
    // itself)
    if (panelComponent
        .isVisible()) { // Don't add search text for components that are not currently displayed
      panelSearchItem
          .getComponents()
          .forEach(
              childComponent -> SearchUtils.addSearchProperties(panelSearchItem, childComponent));
    }

    searchItemsMap.get(configTab).add(panelSearchItem);

    allSearchComponents.addAll(panelSearchItem.getComponents());
  }

  /**
   * Indexes a non-{@link JPanel} Swing component, caching the constructed {@link SearchItem} for
   * searching.
   *
   * @param configTab {@link ConfigTab} which owns the component
   * @param component The {@link Component} to index
   */
  private void indexComponent(ConfigTab configTab, Component component) {
    // Previously indexed by a parent panel
    if (allSearchComponents.contains(component)) {
      return;
    }

    SearchItem searchItem = new SearchItem(configTab, component);

    SearchUtils.addSearchProperties(searchItem, component);

    searchItemsMap.get(configTab).add(searchItem);

    allSearchComponents.add(component);
  }

  /**
   * Re-indexes search components by first resetting the current search, such that all component
   * visibility states are restored prior to execution of uiChangeMethod(), in case it has to alter
   * component visibility. This order of operations is important as component visibility is cached
   * during indexing, such that when a searched component becomes visible again, its original
   * pre-search visibility will be restored. Reapplies search upon completion.
   *
   * @param uiChangeMethod Lambda expression to execute during reindexing
   */
  private void reindexSearch(UIChangeMethod uiChangeMethod) {
    final String searchText = searchTextField.getText();

    clearSearchForIndexing();

    uiChangeMethod.execute();

    indexSearch();

    setSearchText(searchText);
  }

  /**
   * Searches the {@link #searchItemsMap} cache for items which match the current {@link
   * #searchTextField} value, actively hiding and showing components belonging to each matched
   * {@link SearchItem}
   */
  private void searchComponents() {
    final String searchText = searchTextField.getText().toLowerCase();

    // Reset scroll position to the top
    final JScrollPane currentScrollPane =
        (JScrollPane) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
    javax.swing.SwingUtilities.invokeLater(
        () -> currentScrollPane.getVerticalScrollBar().setValue(0));

    if (searchInitiatedTabIndex == -1) {
      setInitiatedTab(tabbedPane.getSelectedIndex());
    }

    if (executedGoToSearch) {
      setInitiatedTab(tabbedPane.getSelectedIndex());
      executedGoToSearch = false;
    }

    // When search was cleared, return to the tab where search was originally initiated from,
    // unless search was cleared for reindexing purposes
    if (!reindexing && searchText.equals("")) {
      tabbedPane.setSelectedIndex(searchInitiatedTabIndex);
      setInitiatedTab(-1);
    }

    List<SearchItem> hideList = new ArrayList<>();
    List<SearchItem> showList = new ArrayList<>();

    // Iterate through the searchItemsMap, finding components to hide or show based on search text
    // matching
    for (ConfigTab configTab : ConfigTab.values()) {
      for (SearchItem searchItem : searchItemsMap.get(configTab)) {
        if (searchItem.isSearchable()
            && !tokenizedStringContains(searchItem.getSearchText().toLowerCase(), searchText)) {
          if (searchItem.isShown()) {
            hideList.add(searchItem);
          }
        } else {
          if (!searchItem.isShown()) {
            showList.add(searchItem);
          }
        }
      }
    }

    // Hide components that no longer match the search text
    hideList.forEach(
        searchItem -> {
          searchItem.setShown(false);
          searchItem.getComponents().forEach(component -> component.setVisible(false));
        });

    // Show components that now match the search text
    showList.forEach(
        searchItem -> {
          searchItem.setShown(true);

          // Only make things visible that were visible prior to the search
          for (int i = 0; i < searchItem.getComponents().size(); i++) {
            boolean componentVisible = searchItem.getComponentVisibility().get(i);
            if (componentVisible) {
              searchItem.getComponents().get(i).setVisible(true);
            }
          }
        });

    // Disable tabs as needed
    for (ConfigTab configTab : ConfigTab.values()) {
      long visibleSearchItemsCount =
          searchItemsMap
              .get(configTab)
              .stream()
              .filter(SearchItem::isShown)
              .filter(SearchItem::isSearchable)
              .count();

      int tabIndex = getTabIndex(configTab);

      // This would only happen if a developer forgets to set a tab title when creating a new tab
      if (tabIndex != -1) {
        tabbedPane.setEnabledAt(tabIndex, visibleSearchItemsCount != 0);
      }
    }

    // Find the first enabled tab
    int firstEnabledTab = -1;
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      if (tabbedPane.isEnabledAt(i)) {
        firstEnabledTab = i;
        break;
      }
    }

    // Go to first enabled tab
    if (!searchText.equals("")
        && firstEnabledTab != -1
        && !tabbedPane.isEnabledAt(tabbedPane.getSelectedIndex())) {
      tabbedPane.setSelectedIndex(firstEnabledTab);
    }

    // Revalidate hierarchy after filtering
    frame.revalidate();
    frame.repaint();
  }

  /**
   * Space-delimits a provided search string and performs a {@link String#contains(CharSequence)} on
   * the provided input string
   *
   * @param inputText The string being searched
   * @param searchText The text to search for
   * @return {@code boolean} value indicating match status
   */
  public static boolean tokenizedStringContains(String inputText, String searchText) {
    return Arrays.stream(searchText.split(" ")).allMatch(inputText::contains);
  }

  /**
   * Sets the current search text, which ultimately triggers the {@link #searchTextField}
   * DocumentListener event and executes {@link #searchComponents()}
   *
   * @param text Search text to set
   */
  private void setSearchText(String text) {
    searchTextField.setText(text);
  }

  /**
   * Clears the current search text in prepartion for reindexing, setting proper {@link #reindexing}
   * flag state
   */
  private void clearSearchForIndexing() {
    reindexing = true;
    setSearchText("");
  }

  /**
   * Finds the first component matching the current search text and scrolls to it. When no results
   * are found, the search is reset.
   */
  private void goToSearchResult() {
    if (searchTextField.getText().equals("")) {
      return;
    }

    // Look for first search match
    Optional<SearchItem> firstVisibleSearchItemOptional =
        searchItemsMap
            .get(ConfigTab.values()[tabbedPane.getSelectedIndex()])
            .stream()
            .filter(SearchItem::isShown)
            .filter(SearchItem::isSearchable)
            .findFirst();

    int previousTabIndex = searchInitiatedTabIndex;

    // Clear search
    executedGoToSearch = true;
    setSearchText("");

    // Scroll to the matched item if found, otherwise reset the search
    if (firstVisibleSearchItemOptional.isPresent()) {
      SearchItem firstVisibleSearchItem = firstVisibleSearchItemOptional.get();

      int firstVisibleSearchItemTabIndex = getTabIndex(firstVisibleSearchItem.getConfigTab());

      // This would only happen if a developer forgets to set a tab title when creating a new tab
      // Even so, we don't want the game to crash
      if (firstVisibleSearchItemTabIndex == -1) {
        executedGoToSearch = false;
        tabbedPane.setSelectedIndex(previousTabIndex);
        setSearchText("");
        return;
      }

      final JScrollPane searchItemScrollPane =
          (JScrollPane) tabbedPane.getComponentAt(firstVisibleSearchItemTabIndex);

      // Scroll a little bit above the matched item
      Rectangle panelBounds = searchItemScrollPane.getViewport().getComponent(0).getBounds();
      Rectangle offsetBounds =
          new Rectangle(panelBounds.x, panelBounds.y - 3, panelBounds.width, panelBounds.height);

      SwingUtilities.invokeLater(
          () ->
              ((JComponent) firstVisibleSearchItem.getComponents().get(0))
                  .scrollRectToVisible(offsetBounds));
    } else {
      executedGoToSearch = false;
      tabbedPane.setSelectedIndex(previousTabIndex);
      setSearchText("");
    }
  }

  /**
   * Adds vertical glue to a settings panel to ensure that components do not shrink / grow when an
   * active search removes the scrollbar. This must be the very last component added to a settings
   * panel.
   *
   * @param panel The panel to which glue should be added
   */
  private static void addPanelBottomGlue(JPanel panel) {
    JComponent panelGlue = (JComponent) Box.createVerticalGlue();
    // These are named such that they can be identified in cases where the panel
    // layout dynamically changes, such as during search.
    panelGlue.setName(panel.getName().toLowerCase() + "PanelBottomGlue");
    SearchUtils.setUnsearchable(panelGlue);
    panel.add(panelGlue);
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

    formatPlayButton("▶", "Play");
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

  private void formatPlayButton(String text, String fallbackText) {
    if (Launcher.controlsFont != null) {
      QueueWindow.button = new JButton(text);
      Font smaller_font = Launcher.controlsFont.deriveFont(Font.PLAIN, osScaleMul(12.0f));
      QueueWindow.button.setFont(smaller_font);
    } else {
      QueueWindow.button = new JButton(fallbackText);
    }
    if (Util.isUsingFlatLAFTheme()) {
      QueueWindow.button.setMargin(new Insets(0, 0, 0, 0));
    } else {
      QueueWindow.button.setMargin(
          new Insets(osScaleMul(-3), osScaleMul(-6), osScaleMul(-3), osScaleMul(-7)));
    }
  }
  /**
   * Alias for {@link #addKeybindSet(JPanel, String, String, KeyModifier, int, String)}, without
   * search metadata
   */
  private void addKeybindSet(
      JPanel panel,
      String labelText,
      String commandID,
      KeyModifier defaultModifier,
      int defaultKeyValue) {
    addKeybindSet(panel, labelText, commandID, defaultModifier, defaultKeyValue, null);
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
   * @param searchMetadata Additional search metadata to add
   */
  private void addKeybindSet(
      JPanel panel,
      String labelText,
      String commandID,
      KeyModifier defaultModifier,
      int defaultKeyValue,
      String searchMetadata) {
    JLabel l = addKeybindLabel(panel, labelText);
    if (searchMetadata != null) {
      SearchUtils.addSearchMetadata(l, searchMetadata);
    }
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

    // Relate label and button for searching, since they are not within a panel
    SearchUtils.setRelatedSearchComponent(l, b);
    SearchUtils.setRelatedSearchComponent(b, l);

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
    gbc.insets = new Insets(0, 0, osScaleMul(5), 0);
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
    gbc.insets = new Insets(0, 0, osScaleMul(5), 0);
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

    JComponent spacer1 = (JComponent) Box.createVerticalStrut(osScaleMul(7));
    panel.add(spacer1, gbc);
    SearchUtils.setUnsearchable(spacer1);
    JSeparator jsep = new JSeparator(SwingConstants.HORIZONTAL);
    SearchUtils.setUnsearchable(jsep);
    panel.add(jsep, gbc);
    JComponent spacer2 = (JComponent) Box.createVerticalStrut(osScaleMul(7));
    SearchUtils.setUnsearchable(spacer2);
    panel.add(spacer2, gbc);
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
    else gbc.insets = new Insets(osScaleMul(7), 0, 0, 0);
    gbc.gridy = keybindLabelGridYCounter++;
    keybindButtonGridYCounter++;
    gbc.weightx = 20;
    gbc.gridwidth = 2;

    JLabel jlbl = new JLabel(categoryName);
    SearchUtils.setUnsearchable(jlbl);
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
    addSettingsHeaderLabel(panel, "<html><b>" + categoryName + "</b></html>", false);
    addSettingsHeaderSeparator(panel);
  }

  /**
   * Adds a new category title to the notifications list, displayed in the center.
   *
   * @param panel Panel to add the title to.
   * @param categoryName Name of the category to add.
   */
  private void addSettingsHeaderCentered(JPanel panel, String categoryName) {
    addSettingsHeaderLabel(panel, "<html><b>" + categoryName + "</b></html>", true);
    addSettingsHeaderSeparator(panel);
  }

  /**
   * Adds a new horizontal separator to the notifications list.
   *
   * @param panel Panel to add the separator to.
   */
  private void addSettingsHeaderSeparator(JPanel panel) {
    JSeparator jsep = new JSeparator(SwingConstants.HORIZONTAL);
    jsep.setPreferredSize(new Dimension(0, osScaleMul(7)));
    jsep.setMaximumSize(new Dimension(Short.MAX_VALUE, osScaleMul(7)));
    SearchUtils.setUnsearchable(jsep);
    panel.add(jsep);
  }

  /**
   * Adds a new category label to the notifications list.
   *
   * @param panel Panel to add the label to.
   * @param categoryName Name of the category to add.
   * @param center Whether to center the jLabel
   * @return The label that was added.
   */
  private JLabel addSettingsHeaderLabel(JPanel panel, String categoryName, boolean center) {
    JLabel jlbl = center ? new JLabel(categoryName, JLabel.CENTER) : new JLabel(categoryName);
    if (center) {
      jlbl.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    }
    SearchUtils.setUnsearchable(jlbl);
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
    checkbox.setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(10), osScaleMul(5)));
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
    radioButton.setBorder(
        BorderFactory.createEmptyBorder(0, leftIndent, osScaleMul(7), osScaleMul(5)));
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
   * Calls {@link #executeSynchronizeGuiValues()}, reindexing components and ensuring it is run from
   * the EDT
   */
  public void synchronizeGuiValues() {
    // Always invoke from the EDT, since this method can be called outside the AWT thread
    if (SwingUtilities.isEventDispatchThread()) {
      reindexSearch(this::executeSynchronizeGuiValues);
    } else {
      try {
        SwingUtilities.invokeAndWait(() -> reindexSearch(this::executeSynchronizeGuiValues));
      } catch (InterruptedException | InvocationTargetException e) {
        Logger.Error("Please screenshot and report this [synchronizeGuiValues] error!");
        e.printStackTrace();
      }
    }
  }

  /** Synchronizes all relevant values in the gui's elements to match those in Settings.java */
  private void executeSynchronizeGuiValues() {
    // Presets tab (has to go first to properly synchronizeGui)
    presetsPanelCustomSettingsCheckbox.setSelected(Settings.currentProfile.equals("custom"));

    String currentPreset;
    int currentPresetIndex = Settings.presetTable.indexOf(Settings.currentProfile);
    if (currentPresetIndex == -1) {
      currentPreset = "None";
    } else {
      currentPreset =
          ((JLabel) presetsPanelPresetSlider.getLabelTable().get(currentPresetIndex)).getText();
    }

    String currentPresetText;
    if (Settings.presetModified) {
      currentPresetText = "<html>Current Preset: <b>" + currentPreset + "</b> (modified)</html>";
    } else {
      currentPresetText = "<html>Current Preset: <b>" + currentPreset + "</b></html>";
    }

    presetsPanelCurrentPresetLabel.setText(currentPresetText);

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
    generalPanelScaleWindowCheckbox.setSelected(
        Settings.SCALED_CLIENT_WINDOW.get(Settings.currentProfile));
    if (Settings.SCALING_ALGORITHM.get(Settings.currentProfile)
        == AffineTransformOp.TYPE_NEAREST_NEIGHBOR) {
      generalPanelIntegerScalingFocusButton.setSelected(true);
    } else if (Settings.SCALING_ALGORITHM.get(Settings.currentProfile)
        == AffineTransformOp.TYPE_BILINEAR) {
      generalPanelBilinearScalingFocusButton.setSelected(true);
    } else if (Settings.SCALING_ALGORITHM.get(Settings.currentProfile)
        == AffineTransformOp.TYPE_BICUBIC) {
      generalPanelBicubicScalingFocusButton.setSelected(true);
    }
    generalPanelIntegerScalingSpinner.setValue(
        Settings.INTEGER_SCALING_FACTOR.get(Settings.currentProfile));
    generalPanelBilinearScalingSpinner.setValue(
        Settings.BILINEAR_SCALING_FACTOR.get(Settings.currentProfile));
    generalPanelBicubicScalingSpinner.setValue(
        Settings.BICUBIC_SCALING_FACTOR.get(Settings.currentProfile));
    generalPanelUseDarkModeCheckbox.setSelected(
        Settings.USE_DARK_FLATLAF.get(Settings.currentProfile));
    generalPanelUseNimbusThemeCheckbox.setSelected(
        Settings.USE_NIMBUS_THEME.get(Settings.currentProfile));
    generalPanelCheckUpdates.setSelected(Settings.CHECK_UPDATES.get(Settings.currentProfile));
    generalPanelAccountSecurityCheckbox.setSelected(
        Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile));
    generalPanelConfirmCancelRecoveryChangeCheckbox.setSelected(
        Settings.CONFIRM_CANCEL_RECOVERY_CHANGE.get(Settings.currentProfile));
    generalPanelShowSecurityTipsAtLoginCheckbox.setSelected(
        Settings.SHOW_SECURITY_TIP_DAY.get(Settings.currentProfile));
    generalPanelWelcomeEnabled.setSelected(
        Settings.REMIND_HOW_TO_OPEN_SETTINGS.get(Settings.currentProfile));
    generalPanelChatHistoryCheckbox.setSelected(
        Settings.LOAD_CHAT_HISTORY.get(Settings.currentProfile));
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
    generalPanelDisableNatureRuneAlchCheckbox.setSelected(
        Settings.DISABLE_NAT_RUNE_ALCH.get(Settings.currentProfile));
    generalPanelCommandPatchQuestCheckbox.setSelected(
        Settings.COMMAND_PATCH_QUEST.get(Settings.currentProfile));
    generalPanelCommandPatchEdibleRaresCheckbox.setSelected(
        Settings.COMMAND_PATCH_EDIBLE_RARES.get(Settings.currentProfile));
    generalPanelCommandPatchDiskOfReturningCheckbox.setSelected(
        Settings.COMMAND_PATCH_DISK.get(Settings.currentProfile));
    generalPanelBypassAttackCheckbox.setSelected(
        Settings.ATTACK_ALWAYS_LEFT_CLICK.get(Settings.currentProfile));
    generalPanelNumberedDialogueOptionsCheckbox.setSelected(
        Settings.NUMBERED_DIALOGUE_OPTIONS.get(Settings.currentProfile));
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
    generalPanelLimitRanFPSSpinner.setValue(
        Settings.RAN_EFFECT_TARGET_FPS.get(Settings.currentProfile));
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
    generalPanelCtrlScrollChatCheckbox.setSelected(
        Settings.CTRL_SCROLL_CHAT.get(Settings.currentProfile));
    generalPanelShiftScrollCameraRotationCheckbox.setSelected(
        Settings.SHIFT_SCROLL_CAMERA_ROTATION.get(Settings.currentProfile));
    generalPanelTrackpadRotationSlider.setValue(
        Settings.TRACKPAD_ROTATION_SENSITIVITY.get(Settings.currentProfile));
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

    switch (Settings.CUSTOM_RAN_CHAT_EFFECT.get(Settings.currentProfile)) {
      case DISABLED:
        generalPanelRanEntirelyDisableButton.setSelected(true);
        generalPanelCustomRandomChatColourCheckbox.setSelected(true);
        break;
      case VANILLA:
        generalPanelVanillaRanHiddenButton.setSelected(true);
        generalPanelCustomRandomChatColourCheckbox.setSelected(false);
        break;
      case SLOWER:
        generalPanelRanReduceFrequencyButton.setSelected(true);
        generalPanelCustomRandomChatColourCheckbox.setSelected(true);
        break;
      case RGB_WAVE:
        generalPanelRanRGBRotationButton.setSelected(true);
        generalPanelCustomRandomChatColourCheckbox.setSelected(true);
        break;
      case STATIC:
        generalPanelRanSelectColourButton.setSelected(true);
        generalPanelCustomRandomChatColourCheckbox.setSelected(true);
        break;
      case FLASH1:
      case FLASH2:
      case FLASH3:
      case GLOW1:
      case GLOW2:
      case GLOW3:
        generalPanelRanRS2EffectButton.setSelected(true);
        generalPanelCustomRandomChatColourCheckbox.setSelected(true);
        generalPanelRS2ChatEffectComboBox.setSelectedIndex(
            Settings.CUSTOM_RAN_CHAT_EFFECT.get(Settings.currentProfile).id() - 6);
        break;
    }

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
    overlayPanelStatusAlwaysTextCheckbox.setSelected(
        Settings.ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.get(Settings.currentProfile));
    overlayPanelBuffsCheckbox.setSelected(Settings.SHOW_BUFFS.get(Settings.currentProfile));
    overlayPanelKeptItemsCheckbox.setSelected(Settings.DEATH_ITEMS.get(Settings.currentProfile));
    overlayPanelKeptItemsWildCheckbox.setSelected(
        Settings.DEATH_ITEMS_WILD.get(Settings.currentProfile));
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
    overlayPanelItemNamesHighlightedOnlyCheckbox.setSelected(
        Settings.SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.get(Settings.currentProfile));
    overlayPanelPlayerNamesCheckbox.setSelected(
        Settings.SHOW_PLAYER_NAME_OVERLAY.get(Settings.currentProfile));
    overlayPanelPvpNamesCheckbox.setSelected(
        Settings.SHOW_PVP_NAME_OVERLAY.get(Settings.currentProfile));
    pvpNamesColour = Util.intToColor(Settings.PVP_NAMES_COLOUR.get(Settings.currentProfile));
    overlayPanelOwnNameCheckbox.setSelected(
        Settings.SHOW_OWN_NAME_OVERLAY.get(Settings.currentProfile));
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
    itemHighlightColour =
        Util.intToColor(Settings.ITEM_HIGHLIGHT_COLOUR.get(Settings.currentProfile));
    overlayPanelHighlightRightClickCheckbox.setSelected(
        Settings.HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.get(Settings.currentProfile));
    // if (Settings.OVERLAY_FONT_STYLE.get(Settings.currentProfile)
    //         == Renderer.OverlayFontStyle.JAGEX.getValue()) {
    //   overlayPanelFontStyleJagexFocusButton.setSelected(true);
    if (Settings.OVERLAY_FONT_STYLE.get(Settings.currentProfile)
        == Renderer.OverlayFontStyle.JAGEX_BORDERED.getValue()) {
      overlayPanelFontStyleJagexBorderedFocusButton.setSelected(true);
    } else if (Settings.OVERLAY_FONT_STYLE.get(Settings.currentProfile)
        == Renderer.OverlayFontStyle.LEGACY.getValue()) {
      overlayPanelFontStyleLegacyFocusButton.setSelected(true);
    }

    // Audio tab
    audioPanelEnableMusicCheckbox.setSelected(Settings.CUSTOM_MUSIC.get(Settings.currentProfile));
    audioPanelSfxVolumeSlider.setValue(Settings.SFX_VOLUME.get(Settings.currentProfile));
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
  private void saveSettings() {
    // General options
    Settings.CUSTOM_CLIENT_SIZE.put(
        Settings.currentProfile, generalPanelClientSizeCheckbox.isSelected());
    Settings.CUSTOM_CLIENT_SIZE_X.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (generalPanelClientSizeXSpinner.getModel())).getNumber().intValue());
    Settings.CUSTOM_CLIENT_SIZE_Y.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (generalPanelClientSizeYSpinner.getModel())).getNumber().intValue());
    Settings.SCALED_CLIENT_WINDOW.put(
        Settings.currentProfile, generalPanelScaleWindowCheckbox.isSelected());
    Settings.SCALING_ALGORITHM.put(
        Settings.currentProfile,
        generalPanelIntegerScalingFocusButton.isSelected()
            ? AffineTransformOp.TYPE_NEAREST_NEIGHBOR
            : generalPanelBilinearScalingFocusButton.isSelected()
                ? AffineTransformOp.TYPE_BILINEAR
                : AffineTransformOp.TYPE_BICUBIC);
    Settings.INTEGER_SCALING_FACTOR.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (generalPanelIntegerScalingSpinner.getModel()))
            .getNumber()
            .intValue());
    Settings.BILINEAR_SCALING_FACTOR.put(
        Settings.currentProfile,
        BigDecimal.valueOf(
                ((SpinnerNumberModel) (generalPanelBilinearScalingSpinner.getModel()))
                    .getNumber()
                    .floatValue())
            .setScale(1, RoundingMode.HALF_DOWN)
            .floatValue());
    Settings.BICUBIC_SCALING_FACTOR.put(
        Settings.currentProfile,
        BigDecimal.valueOf(
                ((SpinnerNumberModel) (generalPanelBicubicScalingSpinner.getModel()))
                    .getNumber()
                    .floatValue())
            .setScale(1, RoundingMode.HALF_DOWN)
            .floatValue());
    Settings.USE_DARK_FLATLAF.put(
        Settings.currentProfile, generalPanelUseDarkModeCheckbox.isSelected());
    Settings.USE_NIMBUS_THEME.put(
        Settings.currentProfile, generalPanelUseNimbusThemeCheckbox.isSelected());
    Settings.CHECK_UPDATES.put(Settings.currentProfile, generalPanelCheckUpdates.isSelected());
    Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.put(
        Settings.currentProfile, generalPanelAccountSecurityCheckbox.isSelected());
    Settings.CONFIRM_CANCEL_RECOVERY_CHANGE.put(
        Settings.currentProfile, generalPanelConfirmCancelRecoveryChangeCheckbox.isSelected());
    Settings.SHOW_SECURITY_TIP_DAY.put(
        Settings.currentProfile, generalPanelShowSecurityTipsAtLoginCheckbox.isSelected());
    Settings.REMIND_HOW_TO_OPEN_SETTINGS.put(
        Settings.currentProfile, generalPanelWelcomeEnabled.isSelected());
    Settings.LOAD_CHAT_HISTORY.put(
        Settings.currentProfile, generalPanelChatHistoryCheckbox.isSelected());
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

    if (generalPanelRanEntirelyDisableButton.isSelected()) {
      Settings.CUSTOM_RAN_CHAT_EFFECT.put(Settings.currentProfile, RanOverrideEffectType.DISABLED);
    } else if (generalPanelRanReduceFrequencyButton.isSelected()) {
      Settings.CUSTOM_RAN_CHAT_EFFECT.put(Settings.currentProfile, RanOverrideEffectType.SLOWER);
    } else if (generalPanelRanRGBRotationButton.isSelected()) {
      Settings.CUSTOM_RAN_CHAT_EFFECT.put(Settings.currentProfile, RanOverrideEffectType.RGB_WAVE);
    } else if (generalPanelRanSelectColourButton.isSelected()) {
      Settings.CUSTOM_RAN_CHAT_EFFECT.put(Settings.currentProfile, RanOverrideEffectType.STATIC);
    } else if (generalPanelRanRS2EffectButton.isSelected()) {
      Settings.CUSTOM_RAN_CHAT_EFFECT.put(
          Settings.currentProfile,
          RanOverrideEffectType.getById(generalPanelRS2ChatEffectComboBox.getSelectedIndex() + 6));
    } else {
      Settings.CUSTOM_RAN_CHAT_EFFECT.put(Settings.currentProfile, RanOverrideEffectType.VANILLA);
    }

    Settings.COMMAND_PATCH_DISK.put(
        Settings.currentProfile, generalPanelCommandPatchDiskOfReturningCheckbox.isSelected());
    Settings.COMMAND_PATCH_EDIBLE_RARES.put(
        Settings.currentProfile, generalPanelCommandPatchEdibleRaresCheckbox.isSelected());
    Settings.COMMAND_PATCH_QUEST.put(
        Settings.currentProfile, generalPanelCommandPatchQuestCheckbox.isSelected());
    Settings.DISABLE_NAT_RUNE_ALCH.put(
        Settings.currentProfile, generalPanelDisableNatureRuneAlchCheckbox.isSelected());
    Settings.ATTACK_ALWAYS_LEFT_CLICK.put(
        Settings.currentProfile, generalPanelBypassAttackCheckbox.isSelected());
    Settings.NUMBERED_DIALOGUE_OPTIONS.put(
        Settings.currentProfile, generalPanelNumberedDialogueOptionsCheckbox.isSelected());
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
    Settings.CTRL_SCROLL_CHAT.put(
        Settings.currentProfile, generalPanelCtrlScrollChatCheckbox.isSelected());
    Settings.SHIFT_SCROLL_CAMERA_ROTATION.put(
        Settings.currentProfile, generalPanelShiftScrollCameraRotationCheckbox.isSelected());
    Settings.TRACKPAD_ROTATION_SENSITIVITY.put(
        Settings.currentProfile, generalPanelTrackpadRotationSlider.getValue());
    Settings.AUTO_SCREENSHOT.put(
        Settings.currentProfile, generalPanelAutoScreenshotCheckbox.isSelected());
    Settings.RS2HD_SKY.put(Settings.currentProfile, generalPanelRS2HDSkyCheckbox.isSelected());
    Settings.CUSTOM_SKYBOX_OVERWORLD_ENABLED.put(
        Settings.currentProfile, generalPanelCustomSkyboxOverworldCheckbox.isSelected());
    Settings.CUSTOM_SKYBOX_UNDERGROUND_ENABLED.put(
        Settings.currentProfile, generalPanelCustomSkyboxUndergroundCheckbox.isSelected());
    Settings.CUSTOM_RAN_STATIC_COLOUR.put(
        Settings.currentProfile, Util.colorToInt(ranStaticColour));
    Settings.CUSTOM_SKYBOX_OVERWORLD_COLOUR.put(
        Settings.currentProfile, Util.colorToInt(overworldSkyColour));
    Settings.CUSTOM_SKYBOX_UNDERGROUND_COLOUR.put(
        Settings.currentProfile, Util.colorToInt(undergroundSkyColour));
    Settings.VIEW_DISTANCE.put(Settings.currentProfile, generalPanelViewDistanceSlider.getValue());
    Settings.FPS_LIMIT_ENABLED.put(
        Settings.currentProfile, generalPanelLimitFPSCheckbox.isSelected());
    Settings.RAN_EFFECT_TARGET_FPS.put(
        Settings.currentProfile,
        ((SpinnerNumberModel) (generalPanelLimitRanFPSSpinner.getModel())).getNumber().intValue());
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
    Settings.ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.put(
        Settings.currentProfile, overlayPanelStatusAlwaysTextCheckbox.isSelected());
    Settings.SHOW_BUFFS.put(Settings.currentProfile, overlayPanelBuffsCheckbox.isSelected());
    Settings.DEATH_ITEMS.put(Settings.currentProfile, overlayPanelKeptItemsCheckbox.isSelected());
    Settings.DEATH_ITEMS_WILD.put(
        Settings.currentProfile, overlayPanelKeptItemsWildCheckbox.isSelected());
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
    Settings.SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.put(
        Settings.currentProfile, overlayPanelItemNamesHighlightedOnlyCheckbox.isSelected());
    Settings.SHOW_PLAYER_NAME_OVERLAY.put(
        Settings.currentProfile, overlayPanelPlayerNamesCheckbox.isSelected());
    Settings.SHOW_PVP_NAME_OVERLAY.put(
        Settings.currentProfile, overlayPanelPvpNamesCheckbox.isSelected());
    Settings.PVP_NAMES_COLOUR.put(Settings.currentProfile, Util.colorToInt(pvpNamesColour));
    Settings.SHOW_OWN_NAME_OVERLAY.put(
        Settings.currentProfile, overlayPanelOwnNameCheckbox.isSelected());
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
    Settings.ITEM_HIGHLIGHT_COLOUR.put(
        Settings.currentProfile, Util.colorToInt(itemHighlightColour));
    Settings.HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.put(
        Settings.currentProfile, overlayPanelHighlightRightClickCheckbox.isSelected());
    Settings.OVERLAY_FONT_STYLE.put(
        Settings.currentProfile,
        // overlayPanelFontStyleJagexFocusButton.isSelected()
        //         ? Renderer.OverlayFontStyle.JAGEX.getValue() :
        overlayPanelFontStyleJagexBorderedFocusButton.isSelected()
            ? Renderer.OverlayFontStyle.JAGEX_BORDERED.getValue()
            : Renderer.OverlayFontStyle.LEGACY.getValue());

    // Audio options
    Settings.CUSTOM_MUSIC.put(Settings.currentProfile, audioPanelEnableMusicCheckbox.isSelected());
    Settings.SFX_VOLUME.put(Settings.currentProfile, audioPanelSfxVolumeSlider.getValue());
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
        Settings.presetModified = false;
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
        Settings.presetModified = false;
        resetPresetsButton.setEnabled(false);
        Logger.Info("Changed to " + Settings.currentProfile + " preset");
      } else {
        Settings.presetModified = true;
        resetPresetsButton.setEnabled(true);
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
    Settings.saveNoPresetModification();
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
  public void setKeybindButtonText(KeybindSet kbs) {
    kbs.button.setText(kbs.getFormattedKeybindText());
  }

  /**
   * Applies the settings in the Config GUI to the Settings class variables. <br>
   *
   * <p>Note that this method should be used to apply any additional settings that are not applied
   * automatically, such as those already present. Also note that thread-unsafe operations affecting
   * the applet should not be done in this method, as this method is invoked by the AWT event queue.
   */
  private void applySettings() {
    // Wrap entire function to ensure search is cleared prior to any other executions that may
    // involve reading from or updating GUI values
    reindexSearch(
        () -> {
          saveSettings();
          // Tell the Renderer to update the scale from its thread to avoid thread-safety issues.
          Settings.renderingScalarUpdateRequired = true;
          // Tell the Renderer to update the FoV from its thread to avoid thread-safety issues.
          Settings.fovUpdateRequired = true;
          Settings.checkSoftwareCursor();
          Camera.setDistance(Settings.VIEW_DISTANCE.get(Settings.currentProfile));
          executeSynchronizeGuiValues();
          QueueWindow.syncColumnsWithSettings();
          QueueWindow.playlistTable.repaint();
          Item.patchItemNames();
          Item.patchItemCommands();
          GameApplet.syncFontSetting();
          SoundEffects.adjustMudClientSfxVolume();
        });
  }

  private void synchronizePresetOptions() {
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
      if (Settings.presetTable.contains(Settings.currentProfile)) {
        replaceConfigButton.setEnabled(true);
      }
      // Only enable when applicable
      if (Settings.presetModified) {
        resetPresetsButton.setEnabled(
            Settings.presetTable
                .get(presetsPanelPresetSlider.getValue())
                .equals(Settings.currentProfile));
      }
    }
  }

  private void addWorldFields(int i) {
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
    worldNumberJLabel.setAlignmentY(0.75f);
    worldListTitleTextFieldContainers.get(i).add(worldNumberJLabel, cR);

    cR.weightx = 0.5;
    cR.gridwidth = 5;

    if (Util.isUsingFlatLAFTheme()) {
      cR.insets = new Insets(0, 0, 0, osScaleMul(4));
    }

    worldNamesJTextFields.put(i, new HintTextField("Name of World"));
    worldNamesJTextFields.get(i).setMinimumSize(osScaleMul(new Dimension(80, 28)));
    worldNamesJTextFields.get(i).setMaximumSize(osScaleMul(new Dimension(300, 28)));
    worldNamesJTextFields.get(i).setPreferredSize(osScaleMul(new Dimension(200, 28)));
    worldNamesJTextFields.get(i).setAlignmentY(0.75f);
    worldListTitleTextFieldContainers.get(i).add(worldNamesJTextFields.get(i), cR);

    cR.weightx = 0.1;
    cR.gridwidth = 1;
    cR.anchor = GridBagConstraints.LINE_END;

    /*
          JLabel spacingJLabel = new JLabel("");
          worldNumberJLabel.setAlignmentY(0.75f);
          worldListTitleTextFieldContainers.get(i).add(spacingJLabel, cR);
    */

    String[] worldTypes = {"Free", "Members", "Free (Veteran)", "Members (Veteran)"};
    JComboBox worldTypeComboBox = new JComboBox(worldTypes);

    worldTypesJComboBoxes.put(i, worldTypeComboBox);
    worldTypesJComboBoxes.get(i).setMinimumSize(osScaleMul(new Dimension(120, 28)));
    worldTypesJComboBoxes.get(i).setMaximumSize(osScaleMul(new Dimension(120, 28)));
    worldTypesJComboBoxes.get(i).setPreferredSize(osScaleMul(new Dimension(120, 28)));
    worldTypesJComboBoxes.get(i).setAlignmentY(0.75f);
    worldListTitleTextFieldContainers.get(i).add(worldTypesJComboBoxes.get(i), cR);

    cR.weightx = 0.3;
    cR.gridwidth = 1;
    if (Util.isUsingFlatLAFTheme()) {
      cR.insets = new Insets(0, 0, 0, 0);
    }

    worldDeleteJButtons.put(i, new JButton("Delete World"));
    worldDeleteJButtons.get(i).setAlignmentY(0.80f);
    worldDeleteJButtons.get(i).setPreferredSize(osScaleMul(new Dimension(50, 28)));
    worldTypesJComboBoxes.get(i).setMinimumSize(osScaleMul(new Dimension(50, 28)));
    worldTypesJComboBoxes.get(i).setMaximumSize(osScaleMul(new Dimension(50, 28)));
    worldDeleteJButtons.get(i).setActionCommand(String.format("%d", i));
    worldDeleteJButtons
        .get(i)
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                String actionCommandWorld = e.getActionCommand();

                JPanel confirmDeleteWorldPanel =
                    Util.createOptionMessagePanel(
                        "<b>Warning</b>: Are you sure you want to <b>DELETE</b> World %s?",
                        actionCommandWorld);

                int choice =
                    JOptionPane.showConfirmDialog(
                        Launcher.getConfigWindow().frame,
                        confirmDeleteWorldPanel,
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.CLOSED_OPTION || choice == JOptionPane.NO_OPTION) {
                  return;
                }

                Logger.Info("Deleting World " + actionCommandWorld);
                // Reindex search to account for the altered UI
                reindexSearch(() -> Settings.removeWorld(Integer.parseInt(actionCommandWorld)));
              }
            });

    worldListTitleTextFieldContainers.get(i).add(worldDeleteJButtons.get(i), cR);

    worldListTitleTextFieldContainers.get(i).setMaximumSize(osScaleMul(new Dimension(680, 40)));
    worldListPanel.add(worldListTitleTextFieldContainers.get(i));

    //// URL/Ports line
    worldUrlsJTextFields.put(i, new HintTextField(String.format("World %d URL", i)));
    worldPortsJTextFields.put(
        i, new HintTextField(String.format("World %d Port (default: 43594)", i)));

    worldUrlsJTextFields.get(i).setMinimumSize(osScaleMul(new Dimension(100, 28)));
    worldUrlsJTextFields.get(i).setMaximumSize(osScaleMul(new Dimension(500, 28)));
    worldUrlsJTextFields.get(i).setPreferredSize(osScaleMul(new Dimension(500, 28)));
    worldUrlsJTextFields.get(i).setAlignmentY(0.75f);

    int portOffset = Util.isUsingFlatLAFTheme() ? 4 : 0;
    worldPortsJTextFields.get(i).setMinimumSize(osScaleMul(new Dimension(100 - portOffset, 28)));
    worldPortsJTextFields.get(i).setMaximumSize(osScaleMul(new Dimension(180 - portOffset, 28)));
    worldPortsJTextFields.get(i).setAlignmentY(0.75f);

    worldListURLPortTextFieldContainers.put(i, new JPanel());

    worldListURLPortTextFieldContainers
        .get(i)
        .setLayout(new BoxLayout(worldListURLPortTextFieldContainers.get(i), BoxLayout.X_AXIS));

    worldListURLPortTextFieldContainers.get(i).add(worldUrlsJTextFields.get(i));
    if (Util.isUsingFlatLAFTheme()) {
      JLabel spacingLabel = new JLabel("");
      spacingLabel.setBorder(BorderFactory.createEmptyBorder(0, osScaleMul(4), 0, 0));
      worldListURLPortTextFieldContainers.get(i).add(spacingLabel);
      worldListURLPortTextFieldContainers
          .get(i)
          .setBorder(BorderFactory.createEmptyBorder(osScaleMul(4), 0, osScaleMul(4), 0));
    }
    worldListURLPortTextFieldContainers.get(i).add(worldPortsJTextFields.get(i));
    worldListPanel.add(worldListURLPortTextFieldContainers.get(i));

    //// RSA Pubkey/Exponent line
    worldRSAPubKeyJTextFields.put(
        i, new HintTextField(String.format("World %d RSA Public Key", i)));
    worldRSAExponentsJTextFields.put(
        i, new HintTextField(String.format("World %d RSA Exponent", i)));

    worldRSAPubKeyJTextFields.get(i).setMinimumSize(osScaleMul(new Dimension(100, 28)));
    worldRSAPubKeyJTextFields.get(i).setMaximumSize(osScaleMul(new Dimension(500, 28)));
    worldRSAPubKeyJTextFields.get(i).setPreferredSize(osScaleMul(new Dimension(500, 28)));
    worldRSAPubKeyJTextFields.get(i).setAlignmentY(0.75f);

    int exponentOffset = Util.isUsingFlatLAFTheme() ? 4 : 0;
    worldRSAExponentsJTextFields
        .get(i)
        .setMinimumSize(osScaleMul(new Dimension(100 - exponentOffset, 28)));
    worldRSAExponentsJTextFields
        .get(i)
        .setMaximumSize(osScaleMul(new Dimension(180 - exponentOffset, 28)));
    worldRSAExponentsJTextFields.get(i).setAlignmentY(0.75f);

    worldListRSATextFieldContainers.put(i, new JPanel());

    worldListRSATextFieldContainers
        .get(i)
        .setLayout(new BoxLayout(worldListRSATextFieldContainers.get(i), BoxLayout.X_AXIS));

    worldListRSATextFieldContainers.get(i).add(worldRSAPubKeyJTextFields.get(i));
    if (Util.isUsingFlatLAFTheme()) {
      JLabel spacingLabel = new JLabel("");
      spacingLabel.setBorder(BorderFactory.createEmptyBorder(0, osScaleMul(4), 0, 0));
      worldListRSATextFieldContainers.get(i).add(spacingLabel);
      worldListRSATextFieldContainers
          .get(i)
          .setBorder(BorderFactory.createEmptyBorder(0, 0, osScaleMul(4), 0));
    }
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
    hiscoresURLJLabel.setAlignmentY(1.0f);
    worldListHiscoresTextFieldContainers.get(i).add(hiscoresURLJLabel, cR);

    worldListHiscoresURLTextFieldContainers.put(
        i, new HintTextField(String.format("World %d Hiscores URL", i)));

    worldListHiscoresURLTextFieldContainers
        .get(i)
        .setMinimumSize(osScaleMul(new Dimension(100, 28)));
    worldListHiscoresURLTextFieldContainers
        .get(i)
        .setMaximumSize(osScaleMul(new Dimension(580, 28)));
    worldListHiscoresURLTextFieldContainers
        .get(i)
        .setPreferredSize(osScaleMul(new Dimension(580, 28)));
    worldListHiscoresURLTextFieldContainers.get(i).setAlignmentY(0.75f);

    worldListHiscoresTextFieldContainers
        .get(i)
        .setLayout(new BoxLayout(worldListHiscoresTextFieldContainers.get(i), BoxLayout.X_AXIS));

    worldListHiscoresTextFieldContainers.get(i).add(worldListHiscoresURLTextFieldContainers.get(i));
    worldListHiscoresTextFieldContainers.get(i).setMaximumSize(osScaleMul(new Dimension(680, 28)));
    worldListPanel.add(worldListHiscoresTextFieldContainers.get(i));

    //// spacing between worlds
    worldListSpacingLabels.put(i, new JLabel(""));
    worldListSpacingLabels
        .get(i)
        .setBorder(BorderFactory.createEmptyBorder(osScaleMul(30), 0, 0, 0));
    worldListPanel.add(worldListSpacingLabels.get(i));

    //// create world
    if (i > Settings.WORLD_NAMES.size()) {
      Settings.createNewWorld(i);
    }
  }

  private void addAddWorldButton() {
    JButton addWorldButton = new JButton("Add New World");
    addWorldButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
    addWorldButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            worldListPanel.remove(addWorldButton);
            Component verticalGlue =
                Arrays.stream(worldListPanel.getComponents())
                    .filter(
                        c -> c.getName() != null && c.getName().equals("world_listPanelBottomGlue"))
                    .findFirst()
                    .orElse(null);
            if (verticalGlue != null) {
              worldListPanel.remove(verticalGlue);
            }
            ++Settings.WORLDS_TO_DISPLAY;
            // Reindex search to account for the altered UI
            reindexSearch(
                () -> {
                  synchronizeWorldTab();
                  addAddWorldButton();
                  if (verticalGlue != null) {
                    worldListPanel.add(verticalGlue);
                  }
                });
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

        // Group all world fields for searching
        List<Component> worldComponents = new ArrayList<>();
        worldComponents.addAll(
            Arrays.asList(worldListTitleTextFieldContainers.get(i).getComponents()));
        worldComponents.addAll(
            Arrays.asList(worldListURLPortTextFieldContainers.get(i).getComponents()));
        worldComponents.addAll(
            Arrays.asList(worldListRSATextFieldContainers.get(i).getComponents()));
        worldComponents.addAll(Arrays.asList(worldListSpacingLabels.get(i).getComponents()));
        if (Settings.HISCORES_LOOKUP_BUTTON.get(Settings.currentProfile)) {
          worldComponents.addAll(
              Arrays.asList(worldListHiscoresTextFieldContainers.get(i).getComponents()));
        }

        // Add the group to each panel's related search lists
        SearchUtils.setRelatedSearchComponents(
            worldListTitleTextFieldContainers.get(i), worldComponents);
        SearchUtils.setRelatedSearchComponents(
            worldListURLPortTextFieldContainers.get(i), worldComponents);
        SearchUtils.setRelatedSearchComponents(
            worldListRSATextFieldContainers.get(i), worldComponents);
        SearchUtils.setRelatedSearchComponents(worldListSpacingLabels.get(i), worldComponents);
        if (Settings.HISCORES_LOOKUP_BUTTON.get(Settings.currentProfile)) {
          SearchUtils.setRelatedSearchComponents(
              worldListHiscoresTextFieldContainers.get(i), worldComponents);
        }
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
    // Note: the joystick input JLabels do not have their search text indexed, as that would be
    // overkill. As such, we don't need to be worried about reindexing when these get updated.
    joystickInputValueJlabels
        .get(compName)
        .setText(
            String.format(
                "%d", (int) Math.floor(JoystickHandler.joystickInputReports.get(compName))));
    joystickPanel.revalidate();
    joystickPanel.repaint();
  }

  public void updateCustomClientSizeMinValues(Dimension updatedMinimumWindowSize) {
    spinnerWinXModel.setMinimum(updatedMinimumWindowSize.width);
    spinnerWinYModel.setMinimum(updatedMinimumWindowSize.height);
  }

  /**
   * Scans a {@link Container} to recursively find all of its child components
   *
   * @param container {@link Container} to scan
   * @return {@link List} of all found {@link Component}s
   */
  public static List<Component> getAllComponents(final Container container) {
    final Component[] components = container.getComponents();
    final List<Component> componentList = new ArrayList<>();

    for (Component component : components) {
      componentList.add(component);
      if (component instanceof Container)
        componentList.addAll(getAllComponents((Container) component));
    }

    return componentList;
  }

  /**
   * Creates an EventQueue listener, used to capture {@link MouseEvent#MOUSE_ENTERED} events for
   * display tooltip text within the {@link #toolTipPanel}
   *
   * @return The constructed {@link AWTEventListener} instance
   */
  private AWTEventListener createConfigWindowEventQueueListener() {
    return new AWTEventListener() {
      @Override
      public void eventDispatched(AWTEvent e) {
        try {
          // Exit early if the label hasn't been initialized
          if (toolTipTextLabel == null) {
            return;
          }

          // Exit early for things that aren't MOUSE_ENTERED events
          if (e.getID() != MouseEvent.MOUSE_ENTERED) {
            return;
          }

          // Exit early for events that aren't on a JComponent
          if (!(e.getSource() instanceof JComponent)) {
            return;
          }

          // Exit early for events that did not originate from the ConfigWindow
          if (SwingUtilities.getWindowAncestor((JComponent) e.getSource()) != frame) {
            return;
          }

          String componentToolTipText = ((JComponent) e.getSource()).getToolTipText();

          if (componentToolTipText != null && !componentToolTipText.equals(toolTipTextString)) {
            toolTipTextString = componentToolTipText;
            toolTipTextLabel.setText(toolTipTextString);
          }
        } catch (Exception ex) {
          Logger.Error(
              "There was an error with processing the MOUSE_ENTERED event listener."
                  + "Please screenshot and report this error if possible.");
          ex.printStackTrace();
        }
      }
    };
  }

  /** Attaches the EventQueue listener */
  private void addConfigWindowEventQueueListener() {
    if (isListeningForEventQueue) {
      return;
    }

    // Disable tooltips
    ToolTipManager.sharedInstance().setEnabled(false);

    // Add listener
    Toolkit.getDefaultToolkit().addAWTEventListener(eventQueueListener, AWTEvent.MOUSE_EVENT_MASK);

    isListeningForEventQueue = true;
  }

  /** Detaches the EventQueue listener */
  private void removeConfigWindowEventQueueListener() {
    if (!isListeningForEventQueue) {
      return;
    }

    // Enable tooltips
    ToolTipManager.sharedInstance().setEnabled(true);

    // Remove listener
    Toolkit.getDefaultToolkit().removeAWTEventListener(eventQueueListener);

    isListeningForEventQueue = false;
  }

  /** Searchable objects cached within the {@link #searchItemsMap} */
  private static class SearchItem {
    private final ConfigTab configTab;
    private final List<Component> components;
    private final List<Boolean> componentVisibility;
    private String componentText;
    private String metadataText;
    private boolean searchable;
    private boolean shown;

    /**
     * Constructor used when indexing a found {@link JPanel} component
     *
     * @param configTab {@link ConfigTab} which owns this object
     */
    public SearchItem(ConfigTab configTab) {
      this.configTab = configTab;
      this.components = new ArrayList<>();
      this.componentVisibility = new ArrayList<>();
      this.componentText = "";
      this.metadataText = "";
      this.searchable = true;
      this.shown = true;
    }

    /**
     * Constructor used when indexing all other swing components
     *
     * @param configTab {@link ConfigTab} which owns this object
     * @param component The swing component tied to this object
     */
    public SearchItem(ConfigTab configTab, Component component) {
      this.configTab = configTab;
      this.components = new ArrayList<>(Collections.singleton(component));
      this.componentVisibility = new ArrayList<>(Collections.singleton(component.isVisible()));
      this.componentText = "";
      this.metadataText = "";
      this.searchable = true;
      this.shown = true;
    }

    /** @return {@link ConfigTab} which owns this object */
    public ConfigTab getConfigTab() {
      return this.configTab;
    }

    /**
     * Adds a component to the SearchItem, storing its original visibility state
     *
     * @param component The swing component to add
     * @param isVisible {@code boolean} flag indicating original visibility state
     */
    public void addComponent(Component component, boolean isVisible) {
      this.components.add(component);
      this.componentVisibility.add(isVisible);
    }

    /** @return a {@link List} of owned swing components */
    public List<Component> getComponents() {
      return this.components;
    }

    /** @return a {@link List} of visibility states tied to each {@link #components} object */
    public List<Boolean> getComponentVisibility() {
      return this.componentVisibility;
    }

    /**
     * Adds primary searchable text to the SearchItem, derived from component indexing by {@link
     * SearchUtils#addSearchText}
     *
     * @param text {@code String} to add for searching
     */
    public void addComponentText(String text) {
      if (this.componentText.equals("")) {
        this.componentText = text;
      } else {
        this.componentText += " " + text;
      }
    }

    /** @return the primary searchable text */
    public String getComponentText() {
      return this.componentText;
    }

    /**
     * Adds secondary searchable text to the SearchItem which is not derived from normal component
     * indexing
     *
     * @param text {@code String} to add for searching
     */
    public void addMetadataText(String text) {
      if (this.metadataText.equals("")) {
        this.metadataText = text;
      } else {
        this.metadataText += " " + text;
      }
    }

    /** @return the secondary searchable text */
    public String getMetadataText() {
      return this.metadataText;
    }

    /**
     * @return a combination of the primary and secondary searchable text, used for the actual
     *     searching
     */
    public String getSearchText() {
      return this.componentText + " " + this.metadataText;
    }

    /**
     * Sets whether this object should be searchable and hideable
     *
     * @param searchable {@code boolean} flag indicating searchability status
     */
    public void setSearchable(boolean searchable) {
      this.searchable = searchable;
    }

    /** @return searchability state for this SearchItem */
    public boolean isSearchable() {
      return this.searchable;
    }

    /**
     * Sets whether this object is currently being shown due to search filtering
     *
     * @param shown {@code boolean} flag indicating shown status
     */
    public void setShown(boolean shown) {
      this.shown = shown;
    }

    /** @return {@code boolean} flag indicating shown status */
    public boolean isShown() {
      return this.shown;
    }
  }

  /** Utility class for performing various search-related operations */
  private static class SearchUtils {
    // Constants used for applying swing ClientProperties, name-spaced to prevent conflict with
    // other libraries
    private static final String SEARCH_METADATA = "rsc+searchMetaData";
    private static final String BYPASS_PANEL_GROUPING = "rsc+bypassPanelGrouping";
    private static final String SKIP_SEARCH_TEXT = "rsc+skipSearchText";
    private static final String RELATED_SEARCH_COMPONENTS = "rsc+relatedSearchComponent";
    private static final String UNSEARCHABLE = "rsc+unsearchable";

    /**
     * Scans a provided {@link Component} object to derive and set various search properties
     *
     * @param searchItem {@link SearchItem} instance to set properties on
     * @param component The swing {@link Component} in context
     */
    private static void addSearchProperties(SearchItem searchItem, Component component) {
      if (isUnsearchable(component)) {
        searchItem.setSearchable(false);
      }

      // Don't add search text when it was explicitly excluded
      if (!shouldSkipSearchText(component)) {
        addSearchText(searchItem, component, false);
      }
    }

    /**
     * Scans a provided {@link Component} object to derive its searchable text
     *
     * @param searchItem {@link SearchItem} instance to set search text on
     * @param component The swing {@link Component} in context
     * @param isRelatedComponent {@code boolean} flag indicating whether it's a component related to
     *     the {@link SearchItem}
     */
    private static void addSearchText(
        SearchItem searchItem, Component component, boolean isRelatedComponent) {
      if (!component.isVisible()) {
        return;
      }

      final String text;
      final String toolTipText;

      // Add search metadata text
      String metaText = getSearchMetadata(component);

      // Combine text from all related components for searching
      if (!isRelatedComponent) { // Prevent infinite recursion when multiple components refer to
        // each other
        List<Component> relatedComponents = getRelatedSearchComponents(component);

        if (relatedComponents != null) {
          relatedComponents.forEach(
              relatedComponent -> {
                if (!shouldSkipSearchText(relatedComponent)) {
                  addSearchText(searchItem, relatedComponent, true);
                }
              });
        }
      }

      Object componentClass = component.getClass();

      // Extract text from different types of components
      if (componentClass.equals(JButton.class)) { // JButton
        text = ((JButton) component).getText();
        toolTipText = ((JButton) component).getToolTipText();
      } else if (componentClass.equals(JCheckBox.class)) { // JCheckBox
        text = ((JCheckBox) component).getText();
        toolTipText = ((JCheckBox) component).getToolTipText();
      } else if (componentClass.equals(JComboBox.class)) { // JComboBox
        StringBuilder textBuilder = new StringBuilder();
        JComboBox<?> jcb = (JComboBox<?>) component;

        int size = jcb.getItemCount();
        for (int i = 0; i < size; i++) {
          textBuilder.append(jcb.getItemAt(i)).append(" ");
        }

        text = textBuilder.toString();
        toolTipText = jcb.getToolTipText();
      } else if (componentClass.equals(JLabel.class)) { // JLabel
        text = ((JLabel) component).getText();
        toolTipText = ((JLabel) component).getToolTipText();
      } else if (componentClass.equals(JPanel.class)) { // JPanel
        text = null;
        toolTipText = ((JPanel) component).getToolTipText();
      } else if (componentClass.equals(JRadioButton.class)) { // JRadioButton
        text = ((JRadioButton) component).getText();
        toolTipText = ((JRadioButton) component).getToolTipText();
      } else if (componentClass.equals(JSlider.class)) { // JSlider
        StringBuilder textBuilder = new StringBuilder();
        Enumeration<?> values = ((JSlider) component).getLabelTable().elements();

        while (values.hasMoreElements()) {
          Object element = values.nextElement();
          if (element instanceof JLabel) {
            textBuilder.append(((JLabel) element).getText()).append(" ");
          }
        }

        text = textBuilder.toString();
        toolTipText = ((JSlider) component).getToolTipText();
      } else if (componentClass.equals(JSpinner.class)) { // JSpinner
        Object value = ((JSpinner) component).getValue();
        text = String.valueOf(value);
        toolTipText = ((JSpinner) component).getToolTipText();
      } else if (component instanceof JTextField) { // JTextField
        StringBuilder textBuilder = new StringBuilder();
        if (componentClass.equals(HintTextField.class)) {
          textBuilder.append(((HintTextField) component).getHint()).append(" ");
        }

        // Don't index user-entered values from JPassword fields
        if (!component.getClass().equals(JPasswordField.class)) {
          textBuilder.append(((JTextField) component).getText());
        }

        text = textBuilder.toString();
        toolTipText = ((JTextField) component).getToolTipText();
      } else {
        return;
      }

      // Remove extraneous text
      if (text != null && !text.equals("") && !text.trim().equalsIgnoreCase("null")) {
        searchItem.addComponentText(sanitizeHtml(text));
      }

      if (toolTipText != null
          && !toolTipText.equals("")
          && !toolTipText.trim().equalsIgnoreCase("null")) {
        searchItem.addComponentText(sanitizeHtml(toolTipText));
      }

      if (metaText != null && !metaText.equals("") && !metaText.trim().equalsIgnoreCase("null")) {
        searchItem.addMetadataText(sanitizeHtml(metaText));
      }
    }

    /**
     * Removes all HTML and inline CSS from a provided {@code String}
     *
     * @param text The {@code String} to sanitize
     * @return The resulting sanitized {@code String}
     */
    private static String sanitizeHtml(String text) {
      return Jsoup.parse(text).text();
    }

    /**
     * Adds secondary searchable text to a {@link JComponent}, which is ultimately scanned by {@link
     * #addSearchText(SearchItem, Component, boolean)}.
     *
     * <p>This is useful in a couple scenarios:
     *
     * <ol>
     *   <li>When there is a desire to have additional search text on a component than what can be
     *       automatically scanned or what will be visible to the user, such as different spellings,
     *       synonyms, or logically-applicable verbiage.
     *   <li>When automatic scanning cannot pick up on all possible text values, such as
     *       conditionally-rendered labels.
     * </ol>
     *
     * @param component The {@link JComponent} in context
     * @param text The {@code String}s of text to add
     */
    private static void addSearchMetadata(JComponent component, String... text) {
      component.putClientProperty(SEARCH_METADATA, String.join(" ", text));
    }

    /**
     * Retrieves secondary searchable text from a {@link Component}
     *
     * @param component The {@link Component} in context
     * @return The metadata text
     */
    private static String getSearchMetadata(Component component) {
      try {
        return (String) ((JComponent) component).getClientProperty(SEARCH_METADATA);
      } catch (ClassCastException cce) {
        // All Components are scanned, but only JComponents contain clientProperties
        return null;
      }
    }

    /**
     * When applied to a {@link JPanel}, this causes all of its children to be indexed individually
     * rather than grouped together into a single SearchItem. This is useful and important to
     * consider when you cannot easily group items within a JPanel due to layout restrictions.
     *
     * <p>Note: this must be used on a top-level panel within a tab due to the use of JPanels for
     * grouping components. If needed to be used on a sub-panel, its parent panel must also be
     * bypassed.
     *
     * <p>See the "keybindContainerPanel" for an example.
     *
     * @param panel {@link JPanel} component to bypass component grouping for
     */
    private static void bypassPanelGrouping(JPanel panel) {
      panel.putClientProperty(BYPASS_PANEL_GROUPING, true);
    }

    /**
     * Retrieves the "bypass panel grouping" status for a provided {@link JPanel}
     *
     * @param panel The {@link JPanel} in context
     * @return {@code boolean} flag indicating whether panel grouping should be bypassed
     */
    private static boolean shouldBypassPanelGrouping(JPanel panel) {
      Boolean bypassPanelGrouping = (Boolean) panel.getClientProperty(BYPASS_PANEL_GROUPING);

      if (bypassPanelGrouping != null) {
        return bypassPanelGrouping;
      }

      return false;
    }

    /**
     * When applied to a {@link JComponent}, this causes all of its searchable text to be excluded
     * during indexing, though the component will still be shown when applicable. This is useful,
     * for example, when long blocks of text in JLabels will result in a vast number of matches,
     * rendering the dynamic tab-disabling and component hiding less useful.
     *
     * <p>Note: if used on a JPanel, it will not automatically apply to all of its children
     *
     * @param component The {@link JComponent} in context
     */
    private static void skipSearchText(JComponent component) {
      component.putClientProperty(SKIP_SEARCH_TEXT, true);
    }

    /**
     * Retrieves the "search text skipping" status for a provided {@link Component}
     *
     * @param component The {@link Component} in context
     * @return {@code boolean} flag indicating whether search text adding should be skipped
     */
    private static boolean shouldSkipSearchText(Component component) {
      try {
        Object skipIndexFlag = ((JComponent) component).getClientProperty(SKIP_SEARCH_TEXT);

        if (skipIndexFlag != null) {
          return (Boolean) skipIndexFlag;
        }

        return false;
      } catch (ClassCastException cce) {
        // All Components are scanned, but only JComponents contain clientProperties
        return false;
      }
    }

    /**
     * When applied to a {@link JComponent}, this causes the provided {@link Component} to become
     * "related" during indexing / component scanning, resulting in the addition of its searchable
     * text to the resulting {@link SearchItem}. This is primarily useful as a workaround for when
     * multiple components cannot be nicely grouped within a {@link JPanel} due to layout-related
     * restrictions.
     *
     * <p>Note: When adding circular links between multiple components, it is important to do so
     * after all objects have actually been instantiated.
     *
     * @param component The {@link JComponent} in context
     * @param relatedComponent The related {@link Component} to add
     */
    private static void setRelatedSearchComponent(
        JComponent component, Component relatedComponent) {
      if (relatedComponent == null) {
        // The error thrown here is to prevent developer error; it will appear on launch
        throw new IllegalArgumentException("Must pass an instantiated object");
      }

      component.putClientProperty(
          RELATED_SEARCH_COMPONENTS, Collections.singletonList(relatedComponent));
    }

    /**
     * Same functionality as {@link #setRelatedSearchComponent(JComponent, Component)}, but
     * accepting a {@link List} of {@link Component}s to add all at once.
     *
     * @param component The {@link JComponent} in context
     * @param relatedComponentList The {@link List} of related {@link Component}s
     */
    private static void setRelatedSearchComponents(
        JComponent component, List<Component> relatedComponentList) {
      if (relatedComponentList.stream().anyMatch(Objects::isNull)) {
        // The error thrown here is to prevent developer error; it will appear on launch
        throw new IllegalArgumentException("All objects passed must be instantiated");
      }

      component.putClientProperty(RELATED_SEARCH_COMPONENTS, relatedComponentList);
    }

    /**
     * Retrieves the {@link List} of related search components
     *
     * @param component The {@link Component} in context
     * @return the {@link List} of related search components
     */
    private static List<Component> getRelatedSearchComponents(Component component) {
      try {
        Object relatedComponents =
            ((JComponent) component).getClientProperty(RELATED_SEARCH_COMPONENTS);

        if (relatedComponents != null) {
          return (List<Component>) relatedComponents;
        } else {
          return null;
        }
      } catch (ClassCastException cce) {
        // All Components are scanned, but only JComponents contain clientProperties
        return null;
      }
    }

    /**
     * When applied to a {@link JComponent}, the resulting {@link SearchItem} constructed during
     * indexing will neither be searchable nor hideable. This is useful when there is a desire to
     * permanently retain certain components within the tab panels during the course of searching,
     * such as section headers.
     *
     * @param component The {@link JComponent} in context
     */
    private static void setUnsearchable(JComponent component) {
      component.putClientProperty(UNSEARCHABLE, true);
    }

    /**
     * Retrieves the "unsearchable" status for a provided {@link Component}
     *
     * @param component The {@link Component} in context
     * @return {@code boolean} flag indicating whether the Component should not be searchable
     */
    private static boolean isUnsearchable(Component component) {
      try {
        Object unsearchableFlag = ((JComponent) component).getClientProperty(UNSEARCHABLE);

        if (unsearchableFlag != null) {
          return (Boolean) unsearchableFlag;
        }

        return false;
      } catch (ClassCastException cce) {
        // All Components are scanned, but only JComponents contain clientProperties
        return false;
      }
    }
  }

  /**
   * Functional interface for UI-altering method calls to be made during the course of re-indexing.
   * See {@link #reindexSearch(UIChangeMethod)} for more details.
   */
  public interface UIChangeMethod {
    void execute();
  }

  /** Implements ActionListener; to be used for the buttons in the keybinds tab. */
  private static class ClickListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      JButton button = (JButton) e.getSource();
      button.setText("...");
      button.setFocusable(true);
      button.requestFocusInWindow();
    }
  }

  private class ButtonFocusListener implements FocusListener {

    @Override
    public void focusGained(FocusEvent arg0) {}

    @Override
    public void focusLost(FocusEvent arg0) {
      JButton button = (JButton) arg0.getSource();

      for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
        if (button == kbs.button) {
          setKeybindButtonText(kbs);
          kbs.button.setFocusable(false);
        }
      }
    }
  }

  private class RebindListener implements KeyListener {

    @Override
    public void keyPressed(KeyEvent arg0) {
      KeyModifier modifier = KeyModifier.NONE;

      if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
        for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
          if (arg0.getSource() == kbs.button) {
            reindexSearch(
                () -> {
                  kbs.modifier = KeyModifier.NONE;
                  kbs.key = -1;
                  setKeybindButtonText(kbs);
                  kbs.button.setFocusable(false);
                });
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

      final KeyModifier reindexModifier = modifier;

      for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
        if (jbtn == kbs.button) {
          reindexSearch(
              () -> {
                kbs.modifier = reindexModifier;
                kbs.key = key;
                setKeybindButtonText(kbs);
                kbs.button.setFocusable(false);
              });
        }
      }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {}

    @Override
    public void keyTyped(KeyEvent arg0) {}
  }

  private static class HintTextField extends JTextField {
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

    public String getHint() {
      return _hint;
    }
  }
}
