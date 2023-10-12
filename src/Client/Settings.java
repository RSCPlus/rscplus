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
import Game.KeyboardHandler;
import Game.Renderer;
import Game.Replay;
import Game.SoundEffects;
import Game.XPBar;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** Manages storing, loading, and changing settings. */
public class Settings {

  // Internally used variables
  public static boolean fovUpdateRequired;
  public static boolean renderingScalarUpdateRequired;
  public static boolean versionCheckRequired = true;
  public static int javaVersion = 0;
  public static final double VERSION_NUMBER = 20230818.180806;

  static String CONFIG_FILE = null;
  static boolean successfullyInitted = false;
  static boolean presetModified = false;

  /**
   * A time stamp corresponding to the current version of this source code. Used as a sophisticated
   * versioning system.
   *
   * <p>This variable follows ISO 8601 yyyyMMdd.HHmmss format. The version number will actually be
   * read from this source file, so please don't change the name of this variable and keep the
   * assignment near the top for scanning.
   *
   * <p>This variable can be set automatically by ant by issuing `ant setversion` before you push
   * your changes, so there's no need to update it manually.
   */

  /*
   * Settings Variables
   *
   * These have been ordered according to their order on the GUI, for convenience.
   */

  //// general
  public static HashMap<String, Boolean> CUSTOM_CLIENT_SIZE = new HashMap<String, Boolean>();

  public static HashMap<String, Integer> CUSTOM_CLIENT_SIZE_X = new HashMap<String, Integer>();
  public static HashMap<String, Integer> CUSTOM_CLIENT_SIZE_Y = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> SCALED_CLIENT_WINDOW = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> SCALING_ALGORITHM = new HashMap<String, Integer>();
  public static HashMap<String, Integer> INTEGER_SCALING_FACTOR = new HashMap<String, Integer>();
  public static HashMap<String, Float> BILINEAR_SCALING_FACTOR = new HashMap<String, Float>();
  public static HashMap<String, Float> BICUBIC_SCALING_FACTOR = new HashMap<String, Float>();
  public static HashMap<String, Boolean> CHECK_UPDATES = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_ACCOUNT_SECURITY_SETTINGS =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> CONFIRM_CANCEL_RECOVERY_CHANGE =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_SECURITY_TIP_DAY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> REMIND_HOW_TO_OPEN_SETTINGS =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> LOAD_CHAT_HISTORY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> COMBAT_MENU_SHOWN = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> COMBAT_MENU_HIDDEN = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_XPDROPS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> CENTER_XPDROPS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_FATIGUEDROPS = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> FATIGUE_FIGURES = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> SHOW_FATIGUEUNITS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> FATIGUE_ALERT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> INVENTORY_FULL_ALERT = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> NAME_PATCH_TYPE = new HashMap<String, Integer>();
  public static HashMap<String, Integer> COMMAND_PATCH_LEGACY = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> DISABLE_NAT_RUNE_ALCH = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> COMMAND_PATCH_QUEST = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> COMMAND_PATCH_EDIBLE_RARES =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> COMMAND_PATCH_DISK = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> ATTACK_ALWAYS_LEFT_CLICK = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> NUMBERED_DIALOGUE_OPTIONS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> ENABLE_MOUSEWHEEL_SCROLLING =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> KEEP_SCROLLBAR_POS_MAGIC_PRAYER =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> HIDE_ROOFS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> DISABLE_UNDERGROUND_LIGHTING =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> DISABLE_MINIMAP_ROTATION = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> CAMERA_ZOOMABLE = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> CAMERA_ROTATABLE = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> CAMERA_MOVABLE = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> CAMERA_MOVABLE_RELATIVE = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> COLORIZE_CONSOLE_TEXT = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> FOV = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> FPS_LIMIT_ENABLED = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> FPS_LIMIT = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> SOFTWARE_CURSOR = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> CTRL_SCROLL_CHAT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHIFT_SCROLL_CAMERA_ROTATION =
      new HashMap<String, Boolean>();
  public static HashMap<String, Integer> TRACKPAD_ROTATION_SENSITIVITY =
      new HashMap<String, Integer>();
  public static HashMap<String, RanOverrideEffectType> CUSTOM_RAN_CHAT_EFFECT =
      new HashMap<String, RanOverrideEffectType>();
  public static HashMap<String, Integer> RAN_EFFECT_TARGET_FPS = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> AUTO_SCREENSHOT = new HashMap<String, Boolean>();
  public static HashMap<String, String> SCREENSHOTS_STORAGE_PATH = new HashMap<String, String>();
  public static HashMap<String, Boolean> RS2HD_SKY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> CUSTOM_SKYBOX_OVERWORLD_ENABLED =
      new HashMap<String, Boolean>();
  public static HashMap<String, Integer> CUSTOM_SKYBOX_OVERWORLD_COLOUR =
      new HashMap<String, Integer>();
  public static HashMap<String, Boolean> CUSTOM_SKYBOX_UNDERGROUND_ENABLED =
      new HashMap<String, Boolean>();
  public static HashMap<String, Integer> CUSTOM_SKYBOX_UNDERGROUND_COLOUR =
      new HashMap<String, Integer>();
  public static HashMap<String, Integer> CUSTOM_RAN_STATIC_COLOUR = new HashMap<String, Integer>();
  public static HashMap<String, Integer> VIEW_DISTANCE = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> PATCH_GENDER = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> PATCH_HBAR_512_LAST_PIXEL = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> USE_JAGEX_FONTS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> PATCH_WRENCH_MENU_SPACING = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> LOG_VERBOSITY = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> LOG_SHOW_TIMESTAMPS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> LOG_SHOW_LEVEL = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> LOG_FORCE_TIMESTAMPS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> LOG_FORCE_LEVEL = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> PREFERS_XDG_OPEN = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> USE_DARK_FLATLAF = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> USE_NIMBUS_THEME = new HashMap<String, Boolean>();

  //// audio settings
  public static HashMap<String, String> CUSTOM_MUSIC_PATH = new HashMap<String, String>();
  public static final double MIDI_VOLUME = 0.01;
  public static HashMap<String, Boolean> CUSTOM_MUSIC = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> SFX_VOLUME = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> LOUDER_SOUND_EFFECTS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> OVERRIDE_AUDIO_SETTING = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> OVERRIDE_AUDIO_SETTING_SETTING_ON =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_COMBAT1 = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_ADVANCE = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_ANVIL = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_CHISEL = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_CLICK = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_CLOSEDOOR = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_COINS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_COMBAT1A = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_COMBAT1B = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_COMBAT2A = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_COMBAT2B = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_COMBAT3A = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_COMBAT3B = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_COOKING = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_DEATH = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_DROPOBJECT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_EAT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_FILLJUG = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_FISH = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_FOUNDGEM = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_MECHANICAL = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_MINE = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_MIX = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_OPENDOOR = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_OUTOFAMMO = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_POTATO = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_PRAYEROFF = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_PRAYERON = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_PROSPECT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_RECHARGE = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_RETREAT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_SECRETDOOR = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_SHOOT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_SPELLFAIL = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_SPELLOK = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_TAKEOBJECT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_UNDERATTACK = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_EFFECT_VICTORY = new HashMap<String, Boolean>();

  //// overlays
  public static HashMap<String, Boolean> SHOW_HP_PRAYER_FATIGUE_OVERLAY =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_MOUSE_TOOLTIP = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_EXTENDED_TOOLTIP = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_BUFFS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> DEATH_ITEMS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> DEATH_ITEMS_WILD = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_LAST_MENU_ACTION = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_INVCOUNT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_INVCOUNT_COLOURS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_RSCPLUS_BUTTONS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> RSCPLUS_BUTTONS_FUNCTIONAL =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> WIKI_LOOKUP_ON_MAGIC_BOOK = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> MOTIVATIONAL_QUOTES_BUTTON =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> TOGGLE_XP_BAR_ON_STATS_BUTTON =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> HISCORES_LOOKUP_BUTTON = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> WIKI_LOOKUP_ON_HBAR = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> REMOVE_REPORT_ABUSE_BUTTON_HBAR =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_ITEM_GROUND_OVERLAY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_PLAYER_NAME_OVERLAY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_PVP_NAME_OVERLAY = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> PVP_NAMES_COLOUR = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> SHOW_OWN_NAME_OVERLAY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_FRIEND_NAME_OVERLAY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_NPC_NAME_OVERLAY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> EXTEND_IDS_OVERLAY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> TRACE_OBJECT_INFO = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_COMBAT_INFO = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_PLAYER_POSITION = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_RETRO_FPS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> NPC_HEALTH_SHOW_PERCENTAGE =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_HITBOX = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_XP_BAR = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> LAG_INDICATOR = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_FOOD_HEAL_OVERLAY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_TIME_UNTIL_HP_REGEN = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> DEBUG = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> EXCEPTION_HANDLER = new HashMap<String, Boolean>();
  public static HashMap<String, ArrayList<String>> HIGHLIGHTED_ITEMS =
      new HashMap<String, ArrayList<String>>();
  public static HashMap<String, ArrayList<String>> SPECIAL_HIGHLIGHTED_ITEMS =
      new HashMap<String, ArrayList<String>>();
  public static HashMap<String, ArrayList<String>> BLOCKED_ITEMS =
      new HashMap<String, ArrayList<String>>();
  public static HashMap<String, Integer> ITEM_HIGHLIGHT_COLOUR = new HashMap<String, Integer>();
  public static HashMap<String, Integer> ITEM_SPECIAL_HIGHLIGHT_COLOUR =
      new HashMap<String, Integer>();
  public static HashMap<String, Boolean> HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU =
      new HashMap<String, Boolean>();
  public static HashMap<String, Integer> OVERLAY_FONT_STYLE = new HashMap<>();

  //// bank
  public static HashMap<String, Boolean> START_REMEMBERED_FILTER_SORT =
      new HashMap<String, Boolean>();
  public static HashMap<String, String> SEARCH_BANK_WORD = new HashMap<String, String>();
  public static HashMap<String, Boolean> SORT_FILTER_BANK = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_BANK_VALUE = new HashMap<String, Boolean>();
  public static HashMap<String, String> SORT_BANK_REMEMBER = new HashMap<String, String>();

  //// notifications
  public static HashMap<String, Boolean> TRAY_NOTIFS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> TRAY_NOTIFS_ALWAYS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> NOTIFICATION_SOUNDS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SOUND_NOTIFS_ALWAYS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> USE_SYSTEM_NOTIFICATIONS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> PM_NOTIFICATIONS = new HashMap<String, Boolean>();
  public static HashMap<String, ArrayList<String>> PM_DENYLIST =
      new HashMap<String, ArrayList<String>>();
  public static HashMap<String, Boolean> TRADE_NOTIFICATIONS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> DUEL_NOTIFICATIONS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> LOGOUT_NOTIFICATIONS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> LOW_HP_NOTIFICATIONS = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> LOW_HP_NOTIF_VALUE = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> FATIGUE_NOTIFICATIONS = new HashMap<String, Boolean>();
  public static HashMap<String, Integer> FATIGUE_NOTIF_VALUE = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> HIGHLIGHTED_ITEM_NOTIFICATIONS =
      new HashMap<String, Boolean>();
  public static HashMap<String, Integer> HIGHLIGHTED_ITEM_NOTIF_VALUE =
      new HashMap<String, Integer>();
  public static HashMap<String, ArrayList<String>> IMPORTANT_MESSAGES =
      new HashMap<String, ArrayList<String>>();
  public static HashMap<String, ArrayList<String>> IMPORTANT_SAD_MESSAGES =
      new HashMap<String, ArrayList<String>>();
  public static HashMap<String, Boolean> MUTE_IMPORTANT_MESSAGE_SOUNDS =
      new HashMap<String, Boolean>();

  //// streaming
  public static HashMap<String, Boolean> TWITCH_CHAT_ENABLED = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> TWITCH_HIDE_CHAT = new HashMap<String, Boolean>();
  public static HashMap<String, String> TWITCH_CHANNEL = new HashMap<String, String>();
  public static HashMap<String, String> TWITCH_OAUTH = new HashMap<String, String>();
  public static HashMap<String, String> TWITCH_USERNAME = new HashMap<String, String>();
  public static HashMap<String, Boolean> SHOW_LOGIN_IP_ADDRESS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SAVE_LOGININFO = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> START_LOGINSCREEN = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SPEEDRUNNER_MODE_ACTIVE = new HashMap<String, Boolean>();
  // public static HashMap<String, String> SPEEDRUNNER_USERNAME = new HashMap<String, String>();

  //// replay
  public static HashMap<String, String> REPLAY_STORAGE_PATH = new HashMap<String, String>();
  public static HashMap<String, Boolean> RECORD_KB_MOUSE = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> PARSE_OPCODES = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> FAST_DISCONNECT = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> RECORD_AUTOMATICALLY = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> HIDE_PRIVATE_MSGS_REPLAY // only hides, still in data
      = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean>
      SHOW_SEEK_BAR // TODO: maybe should implement a "hover in area" toggle for this
      = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_PLAYER_CONTROLS = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> TRIGGER_ALERTS_REPLAY = new HashMap<String, Boolean>();
  public static HashMap<String, String> REPLAY_BASE_PATH = new HashMap<String, String>();
  public static HashMap<String, String> PREFERRED_DATE_FORMAT = new HashMap<String, String>();
  public static HashMap<String, Boolean> SHOW_WORLD_COLUMN = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_CONVERSION_COLUMN = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> SHOW_USERFIELD_COLUMN = new HashMap<String, Boolean>();

  //// world list
  public static HashMap<Integer, String> WORLD_URLS = new HashMap<Integer, String>();
  public static HashMap<Integer, String> WORLD_NAMES = new HashMap<Integer, String>();
  public static HashMap<Integer, Integer> WORLD_PORTS = new HashMap<Integer, Integer>();
  public static HashMap<Integer, Integer> WORLD_SERVER_TYPES = new HashMap<Integer, Integer>();
  public static HashMap<Integer, String> WORLD_RSA_PUB_KEYS = new HashMap<Integer, String>();
  public static HashMap<Integer, String> WORLD_RSA_EXPONENTS = new HashMap<Integer, String>();
  public static HashMap<Integer, String> WORLD_HISCORES_URL = new HashMap<Integer, String>();
  public static HashMap<Integer, String> WORLD_FILE_PATHS = new HashMap<Integer, String>();
  public static int WORLDS_TO_DISPLAY = 5;
  public static boolean noWorldsConfigured = true;

  //// joystick
  public static HashMap<String, Boolean> JOYSTICK_ENABLED = new HashMap<String, Boolean>();

  //// no gui
  public static HashMap<String, Integer> LAST_KNOWN_COMBAT_STYLE = new HashMap<String, Integer>();
  public static HashMap<String, Integer> WORLD = new HashMap<String, Integer>();
  public static HashMap<String, Boolean> FIRST_TIME = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> UPDATE_CONFIRMATION = new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> RECORD_AUTOMATICALLY_FIRST_TIME =
      new HashMap<String, Boolean>();
  public static HashMap<String, Boolean> DISASSEMBLE = new HashMap<String, Boolean>();
  public static HashMap<String, String> DISASSEMBLE_DIRECTORY = new HashMap<String, String>();
  public static HashMap<String, Integer[]> USER_BANK_SORT = new HashMap<String, Integer[]>();
  public static boolean takingSceneryScreenshots = false;

  // these are variables that are injected with JClassPatcher
  public static int COMBAT_STYLE_INT = Client.COMBAT_AGGRESSIVE;
  public static boolean HIDE_ROOFS_BOOL = false;
  public static boolean DISABLE_UNDERGROUND_LIGHTING_BOOL = false;
  public static boolean DISABLE_MINIMAP_ROTATION_BOOL = false;
  public static boolean COMBAT_MENU_SHOWN_BOOL = false;
  public static boolean COMBAT_MENU_HIDDEN_BOOL = false;
  public static boolean CAMERA_ZOOMABLE_BOOL = false;
  public static boolean CAMERA_ROTATABLE_BOOL = false;
  public static boolean CAMERA_MOVABLE_BOOL = false;
  public static boolean VIEW_DISTANCE_BOOL = false;
  public static boolean FOV_BOOL = false;
  public static boolean USE_JAGEX_FONTS_BOOL = false;
  public static boolean PROTECT_NAT_RUNE_ALCH_BOOL = false;
  public static boolean LOAD_CHAT_HISTORY_BOOL = false;
  public static boolean HIGHLIGHT_ITEMS_MENU_BOOL = false;

  // determines which preset to load, or your custom settings :-)
  public static String currentProfile = "custom";

  public static ArrayList<String> presetTable = new ArrayList<String>();

  private Settings() {
    // Empty private constructor to prevent instantiation.
  }

  /** This is storage for all the presets */
  public static void definePresets(Properties props) {
    if (presetTable.size() == 0) {
      presetTable.add("all"); // 0
      presetTable.add("heavy"); // 1
      presetTable.add("default"); // 2
      presetTable.add("lite"); // 3
      presetTable.add("vanilla_resizable"); // 4
      presetTable.add("vanilla"); // 5
    }

    //// general
    CUSTOM_CLIENT_SIZE.put("vanilla", true);
    CUSTOM_CLIENT_SIZE.put("vanilla_resizable", false);
    CUSTOM_CLIENT_SIZE.put("lite", false);
    CUSTOM_CLIENT_SIZE.put("default", false);
    CUSTOM_CLIENT_SIZE.put("heavy", false);
    CUSTOM_CLIENT_SIZE.put("all", false);
    CUSTOM_CLIENT_SIZE.put(
        "custom", getPropBoolean(props, "custom_client_size", CUSTOM_CLIENT_SIZE.get("default")));

    CUSTOM_CLIENT_SIZE_X.put("vanilla", 512);
    CUSTOM_CLIENT_SIZE_X.put("vanilla_resizable", 1024);
    CUSTOM_CLIENT_SIZE_X.put("lite", 1024);
    CUSTOM_CLIENT_SIZE_X.put("default", 1024);
    CUSTOM_CLIENT_SIZE_X.put("heavy", 1024);
    CUSTOM_CLIENT_SIZE_X.put("all", 1024);
    CUSTOM_CLIENT_SIZE_X.put(
        "custom", getPropInt(props, "custom_client_size_x", CUSTOM_CLIENT_SIZE_X.get("default")));

    CUSTOM_CLIENT_SIZE_Y.put("vanilla", 346);
    CUSTOM_CLIENT_SIZE_Y.put("vanilla_resizable", 692);
    CUSTOM_CLIENT_SIZE_Y.put("lite", 692);
    CUSTOM_CLIENT_SIZE_Y.put("default", 692);
    CUSTOM_CLIENT_SIZE_Y.put("heavy", 692);
    CUSTOM_CLIENT_SIZE_Y.put("all", 692);
    CUSTOM_CLIENT_SIZE_Y.put(
        "custom", getPropInt(props, "custom_client_size_y", CUSTOM_CLIENT_SIZE_Y.get("default")));

    SCALED_CLIENT_WINDOW.put("vanilla", false);
    SCALED_CLIENT_WINDOW.put("vanilla_resizable", true);
    SCALED_CLIENT_WINDOW.put("lite", true);
    SCALED_CLIENT_WINDOW.put("default", true);
    SCALED_CLIENT_WINDOW.put("heavy", true);
    SCALED_CLIENT_WINDOW.put("all", true);
    SCALED_CLIENT_WINDOW.put(
        "custom",
        getPropBoolean(props, "enable_window_scaling", SCALED_CLIENT_WINDOW.get("default")));

    SCALING_ALGORITHM.put("vanilla", AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    SCALING_ALGORITHM.put("vanilla_resizable", AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    SCALING_ALGORITHM.put("lite", AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    SCALING_ALGORITHM.put("default", AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    SCALING_ALGORITHM.put("heavy", AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    SCALING_ALGORITHM.put("all", AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    SCALING_ALGORITHM.put(
        "custom", getPropInt(props, "scaling_algorithm", SCALING_ALGORITHM.get("default")));

    INTEGER_SCALING_FACTOR.put("vanilla", 2);
    INTEGER_SCALING_FACTOR.put("vanilla_resizable", 2);
    INTEGER_SCALING_FACTOR.put("lite", 2);
    INTEGER_SCALING_FACTOR.put("default", 2);
    INTEGER_SCALING_FACTOR.put("heavy", 2);
    INTEGER_SCALING_FACTOR.put("all", 2);
    INTEGER_SCALING_FACTOR.put(
        "custom",
        getPropInt(props, "integer_scaling_factor", INTEGER_SCALING_FACTOR.get("default")));

    BILINEAR_SCALING_FACTOR.put("vanilla", 1.5f);
    BILINEAR_SCALING_FACTOR.put("vanilla_resizable", 1.5f);
    BILINEAR_SCALING_FACTOR.put("lite", 1.5f);
    BILINEAR_SCALING_FACTOR.put("default", 1.5f);
    BILINEAR_SCALING_FACTOR.put("heavy", 1.5f);
    BILINEAR_SCALING_FACTOR.put("all", 1.5f);
    BILINEAR_SCALING_FACTOR.put(
        "custom",
        getPropFloat(props, "bilinear_scaling_factor", BILINEAR_SCALING_FACTOR.get("default")));

    BICUBIC_SCALING_FACTOR.put("vanilla", 1.5f);
    BICUBIC_SCALING_FACTOR.put("vanilla_resizable", 1.5f);
    BICUBIC_SCALING_FACTOR.put("lite", 1.5f);
    BICUBIC_SCALING_FACTOR.put("default", 1.5f);
    BICUBIC_SCALING_FACTOR.put("heavy", 1.5f);
    BICUBIC_SCALING_FACTOR.put("all", 1.5f);
    BICUBIC_SCALING_FACTOR.put(
        "custom",
        getPropFloat(props, "bicubic_scaling_factor", BICUBIC_SCALING_FACTOR.get("default")));

    defineStaticPreset(CHECK_UPDATES, getPropBoolean(props, "check_updates", true));

    SHOW_ACCOUNT_SECURITY_SETTINGS.put("vanilla", false);
    SHOW_ACCOUNT_SECURITY_SETTINGS.put("vanilla_resizable", false);
    SHOW_ACCOUNT_SECURITY_SETTINGS.put("lite", true);
    SHOW_ACCOUNT_SECURITY_SETTINGS.put("default", true);
    SHOW_ACCOUNT_SECURITY_SETTINGS.put("heavy", true);
    SHOW_ACCOUNT_SECURITY_SETTINGS.put("all", true);
    SHOW_ACCOUNT_SECURITY_SETTINGS.put(
        "custom",
        getPropBoolean(
            props,
            "show_account_security_settings",
            SHOW_ACCOUNT_SECURITY_SETTINGS.get("default")));

    CONFIRM_CANCEL_RECOVERY_CHANGE.put("vanilla", false);
    CONFIRM_CANCEL_RECOVERY_CHANGE.put("vanilla_resizable", false);
    CONFIRM_CANCEL_RECOVERY_CHANGE.put("lite", true);
    CONFIRM_CANCEL_RECOVERY_CHANGE.put("default", true);
    CONFIRM_CANCEL_RECOVERY_CHANGE.put("heavy", true);
    CONFIRM_CANCEL_RECOVERY_CHANGE.put("all", true);
    CONFIRM_CANCEL_RECOVERY_CHANGE.put(
        "custom",
        getPropBoolean(
            props,
            "confirm_cancel_recovery_change",
            CONFIRM_CANCEL_RECOVERY_CHANGE.get("default")));

    SHOW_SECURITY_TIP_DAY.put("vanilla", false);
    SHOW_SECURITY_TIP_DAY.put("vanilla_resizable", false);
    SHOW_SECURITY_TIP_DAY.put("lite", true);
    SHOW_SECURITY_TIP_DAY.put("default", true);
    SHOW_SECURITY_TIP_DAY.put("heavy", true);
    SHOW_SECURITY_TIP_DAY.put("all", true);
    SHOW_SECURITY_TIP_DAY.put(
        "custom",
        getPropBoolean(props, "show_security_tip_day", SHOW_SECURITY_TIP_DAY.get("default")));

    REMIND_HOW_TO_OPEN_SETTINGS.put("vanilla", false);
    REMIND_HOW_TO_OPEN_SETTINGS.put("vanilla_resizable", false);
    REMIND_HOW_TO_OPEN_SETTINGS.put("lite", false);
    REMIND_HOW_TO_OPEN_SETTINGS.put("default", true);
    REMIND_HOW_TO_OPEN_SETTINGS.put("heavy", true);
    REMIND_HOW_TO_OPEN_SETTINGS.put("all", true);
    REMIND_HOW_TO_OPEN_SETTINGS.put(
        "custom",
        getPropBoolean(props, "welcome_enabled", REMIND_HOW_TO_OPEN_SETTINGS.get("default")));

    LOAD_CHAT_HISTORY.put("vanilla", false);
    LOAD_CHAT_HISTORY.put("vanilla_resizable", false);
    LOAD_CHAT_HISTORY.put("lite", false);
    LOAD_CHAT_HISTORY.put("default", true);
    LOAD_CHAT_HISTORY.put("heavy", true);
    LOAD_CHAT_HISTORY.put("all", true);
    LOAD_CHAT_HISTORY.put(
        "custom", getPropBoolean(props, "load_chat_history", LOAD_CHAT_HISTORY.get("default")));

    COMBAT_MENU_SHOWN.put("vanilla", false);
    COMBAT_MENU_SHOWN.put("vanilla_resizable", false);
    COMBAT_MENU_SHOWN.put("lite", false);
    COMBAT_MENU_SHOWN.put("default", false);
    COMBAT_MENU_SHOWN.put("heavy", true);
    COMBAT_MENU_SHOWN.put("all", true);
    COMBAT_MENU_SHOWN.put(
        "custom", getPropBoolean(props, "combat_menu", COMBAT_MENU_SHOWN.get("default")));

    COMBAT_MENU_HIDDEN.put("vanilla", false);
    COMBAT_MENU_HIDDEN.put("vanilla_resizable", false);
    COMBAT_MENU_HIDDEN.put("lite", false);
    COMBAT_MENU_HIDDEN.put("default", false);
    COMBAT_MENU_HIDDEN.put("heavy", false);
    COMBAT_MENU_HIDDEN.put("all", true);
    COMBAT_MENU_HIDDEN.put(
        "custom", getPropBoolean(props, "combat_menu_hidden", COMBAT_MENU_HIDDEN.get("default")));

    SHOW_XPDROPS.put("vanilla", false);
    SHOW_XPDROPS.put("vanilla_resizable", false);
    SHOW_XPDROPS.put("lite", false);
    SHOW_XPDROPS.put("default", true);
    SHOW_XPDROPS.put("heavy", true);
    SHOW_XPDROPS.put("all", true);
    SHOW_XPDROPS.put("custom", getPropBoolean(props, "show_xpdrops", SHOW_XPDROPS.get("default")));

    CENTER_XPDROPS.put("vanilla", false);
    CENTER_XPDROPS.put("vanilla_resizable", false);
    CENTER_XPDROPS.put("lite", false);
    CENTER_XPDROPS.put("default", false);
    CENTER_XPDROPS.put("heavy", true);
    CENTER_XPDROPS.put("all", true);
    CENTER_XPDROPS.put(
        "custom", getPropBoolean(props, "center_xpdrops", CENTER_XPDROPS.get("default")));

    SHOW_FATIGUEDROPS.put("vanilla", false);
    SHOW_FATIGUEDROPS.put("vanilla_resizable", false);
    SHOW_FATIGUEDROPS.put("lite", false);
    SHOW_FATIGUEDROPS.put("default", true);
    SHOW_FATIGUEDROPS.put("heavy", true);
    SHOW_FATIGUEDROPS.put("all", true);
    SHOW_FATIGUEDROPS.put(
        "custom", getPropBoolean(props, "show_fatiguedrops", SHOW_FATIGUEDROPS.get("default")));

    FATIGUE_FIGURES.put("vanilla", 2);
    FATIGUE_FIGURES.put("vanilla_resizable", 2);
    FATIGUE_FIGURES.put("lite", 2);
    FATIGUE_FIGURES.put("default", 2);
    FATIGUE_FIGURES.put("heavy", 2);
    FATIGUE_FIGURES.put("all", 2);
    FATIGUE_FIGURES.put(
        "custom", getPropInt(props, "fatigue_figures", FATIGUE_FIGURES.get("default")));

    SHOW_FATIGUEUNITS.put("vanilla", false);
    SHOW_FATIGUEUNITS.put("vanilla_resizable", false);
    SHOW_FATIGUEUNITS.put("lite", false);
    SHOW_FATIGUEUNITS.put("default", false);
    SHOW_FATIGUEUNITS.put("heavy", true);
    SHOW_FATIGUEUNITS.put("all", true);
    SHOW_FATIGUEUNITS.put(
        "custom", getPropBoolean(props, "show_fatigueunits", SHOW_FATIGUEUNITS.get("default")));

    FATIGUE_ALERT.put("vanilla", false);
    FATIGUE_ALERT.put("vanilla_resizable", false);
    FATIGUE_ALERT.put("lite", true);
    FATIGUE_ALERT.put("default", true);
    FATIGUE_ALERT.put("heavy", true);
    FATIGUE_ALERT.put("all", true);
    FATIGUE_ALERT.put(
        "custom", getPropBoolean(props, "fatigue_alert", FATIGUE_ALERT.get("default")));

    INVENTORY_FULL_ALERT.put("vanilla", false);
    INVENTORY_FULL_ALERT.put("vanilla_resizable", false);
    INVENTORY_FULL_ALERT.put("lite", false);
    INVENTORY_FULL_ALERT.put("default", false);
    INVENTORY_FULL_ALERT.put("heavy", false);
    INVENTORY_FULL_ALERT.put("all", true);
    INVENTORY_FULL_ALERT.put(
        "custom",
        getPropBoolean(props, "inventory_full_alert", INVENTORY_FULL_ALERT.get("default")));

    /**
     * Defines to what extent the item names should be patched. 0 - No item name patching 1 - Purely
     * practical name changes (potion dosages, unidentified herbs, unfinished potions) 2 -
     * Capitalizations and fixed spellings on top of type 1 changes 3 - Reworded vague stuff to be
     * more descriptive on top of type 1 & 2 changes
     */
    NAME_PATCH_TYPE.put("vanilla", 0);
    NAME_PATCH_TYPE.put("vanilla_resizable", 0);
    NAME_PATCH_TYPE.put("lite", 1);
    NAME_PATCH_TYPE.put("default", 1);
    NAME_PATCH_TYPE.put("heavy", 3);
    NAME_PATCH_TYPE.put("all", 3);
    NAME_PATCH_TYPE.put(
        "custom", getPropInt(props, "name_patch_type", NAME_PATCH_TYPE.get("default")));

    DISABLE_NAT_RUNE_ALCH.put("vanilla", false);
    DISABLE_NAT_RUNE_ALCH.put("vanilla_resizable", false);
    DISABLE_NAT_RUNE_ALCH.put("lite", true);
    DISABLE_NAT_RUNE_ALCH.put("default", true);
    DISABLE_NAT_RUNE_ALCH.put("heavy", true);
    DISABLE_NAT_RUNE_ALCH.put("all", true);
    DISABLE_NAT_RUNE_ALCH.put(
        "custom",
        getPropBoolean(props, "disable_nat_rune_alch", DISABLE_NAT_RUNE_ALCH.get("default")));

    /**
     * LEGACY, NOT USED EXCEPT TO MIGRATE SETTINGS Defines to what extent fix the item commands
     * should be patched. 0 - No item command patching 1 - Disable item consumption on discontinued
     * rares 2 - Swap item command, i.e. use instead of consuming on quest-only items 3 - Apply both
     * fixes 1 and 2
     */
    COMMAND_PATCH_LEGACY.put("vanilla", 0);
    COMMAND_PATCH_LEGACY.put("vanilla_resizable", 0);
    COMMAND_PATCH_LEGACY.put("lite", 0);
    COMMAND_PATCH_LEGACY.put("default", 0);
    COMMAND_PATCH_LEGACY.put("heavy", 0);
    COMMAND_PATCH_LEGACY.put("all", 0);
    COMMAND_PATCH_LEGACY.put(
        "custom", getPropInt(props, "command_patch_type", COMMAND_PATCH_LEGACY.get("default")));

    COMMAND_PATCH_QUEST.put("vanilla", false);
    COMMAND_PATCH_QUEST.put("vanilla_resizable", false);
    COMMAND_PATCH_QUEST.put("lite", false);
    COMMAND_PATCH_QUEST.put("default", false);
    COMMAND_PATCH_QUEST.put("heavy", false);
    COMMAND_PATCH_QUEST.put("all", true);
    COMMAND_PATCH_QUEST.put(
        "custom",
        getPropBoolean(
            props,
            "command_patch_quest",
            COMMAND_PATCH_QUEST.get("default") || COMMAND_PATCH_LEGACY.get("default") >= 2));

    COMMAND_PATCH_EDIBLE_RARES.put("vanilla", false);
    COMMAND_PATCH_EDIBLE_RARES.put("vanilla_resizable", false);
    COMMAND_PATCH_EDIBLE_RARES.put("lite", false);
    COMMAND_PATCH_EDIBLE_RARES.put("default", true);
    COMMAND_PATCH_EDIBLE_RARES.put("heavy", true);
    COMMAND_PATCH_EDIBLE_RARES.put("all", true);
    COMMAND_PATCH_EDIBLE_RARES.put(
        "custom",
        getPropBoolean(
            props,
            "command_patch_edible_rares",
            COMMAND_PATCH_EDIBLE_RARES.get("default")
                || COMMAND_PATCH_LEGACY.get("default") == 1
                || COMMAND_PATCH_LEGACY.get("default") == 3));

    COMMAND_PATCH_DISK.put("vanilla", false);
    COMMAND_PATCH_DISK.put("vanilla_resizable", false);
    COMMAND_PATCH_DISK.put("lite", false);
    COMMAND_PATCH_DISK.put("default", false);
    COMMAND_PATCH_DISK.put("heavy", false);
    COMMAND_PATCH_DISK.put("all", true);
    COMMAND_PATCH_DISK.put(
        "custom", getPropBoolean(props, "command_patch_disk", COMMAND_PATCH_DISK.get("default")));

    ATTACK_ALWAYS_LEFT_CLICK.put("vanilla", false);
    ATTACK_ALWAYS_LEFT_CLICK.put("vanilla_resizable", false);
    ATTACK_ALWAYS_LEFT_CLICK.put("lite", false);
    ATTACK_ALWAYS_LEFT_CLICK.put("default", false);
    ATTACK_ALWAYS_LEFT_CLICK.put("heavy", true);
    ATTACK_ALWAYS_LEFT_CLICK.put("all", true);
    ATTACK_ALWAYS_LEFT_CLICK.put(
        "custom", getPropBoolean(props, "bypass_attack", ATTACK_ALWAYS_LEFT_CLICK.get("default")));

    NUMBERED_DIALOGUE_OPTIONS.put("vanilla", false);
    NUMBERED_DIALOGUE_OPTIONS.put("vanilla_resizable", false);
    NUMBERED_DIALOGUE_OPTIONS.put("lite", false);
    NUMBERED_DIALOGUE_OPTIONS.put("default", false);
    NUMBERED_DIALOGUE_OPTIONS.put("heavy", false);
    NUMBERED_DIALOGUE_OPTIONS.put("all", true);
    NUMBERED_DIALOGUE_OPTIONS.put(
        "custom",
        getPropBoolean(
            props, "numbered_dialogue_options", NUMBERED_DIALOGUE_OPTIONS.get("default")));

    ENABLE_MOUSEWHEEL_SCROLLING.put("vanilla", false);
    ENABLE_MOUSEWHEEL_SCROLLING.put("vanilla_resizable", false);
    ENABLE_MOUSEWHEEL_SCROLLING.put("lite", true);
    ENABLE_MOUSEWHEEL_SCROLLING.put("default", true);
    ENABLE_MOUSEWHEEL_SCROLLING.put("heavy", true);
    ENABLE_MOUSEWHEEL_SCROLLING.put("all", true);
    ENABLE_MOUSEWHEEL_SCROLLING.put(
        "custom",
        getPropBoolean(
            props, "enable_mousewheel_scrolling", ENABLE_MOUSEWHEEL_SCROLLING.get("default")));

    KEEP_SCROLLBAR_POS_MAGIC_PRAYER.put("vanilla", false);
    KEEP_SCROLLBAR_POS_MAGIC_PRAYER.put("vanilla_resizable", false);
    KEEP_SCROLLBAR_POS_MAGIC_PRAYER.put("lite", true);
    KEEP_SCROLLBAR_POS_MAGIC_PRAYER.put("default", true);
    KEEP_SCROLLBAR_POS_MAGIC_PRAYER.put("heavy", true);
    KEEP_SCROLLBAR_POS_MAGIC_PRAYER.put("all", true);
    KEEP_SCROLLBAR_POS_MAGIC_PRAYER.put(
        "custom",
        getPropBoolean(
            props,
            "keep_scrollbar_pos_magic_prayer",
            KEEP_SCROLLBAR_POS_MAGIC_PRAYER.get("default")));

    HIDE_ROOFS.put("vanilla", false);
    HIDE_ROOFS.put("vanilla_resizable", false);
    HIDE_ROOFS.put("lite", false);
    HIDE_ROOFS.put("default", false);
    HIDE_ROOFS.put("heavy", true);
    HIDE_ROOFS.put("all", true);
    HIDE_ROOFS.put("custom", getPropBoolean(props, "hide_roofs", HIDE_ROOFS.get("default")));

    DISABLE_UNDERGROUND_LIGHTING.put("vanilla", false);
    DISABLE_UNDERGROUND_LIGHTING.put("vanilla_resizable", false);
    DISABLE_UNDERGROUND_LIGHTING.put("lite", false);
    DISABLE_UNDERGROUND_LIGHTING.put("default", false);
    DISABLE_UNDERGROUND_LIGHTING.put("heavy", true);
    DISABLE_UNDERGROUND_LIGHTING.put("all", true);
    DISABLE_UNDERGROUND_LIGHTING.put(
        "custom",
        getPropBoolean(
            props, "disable_underground_lighting", DISABLE_UNDERGROUND_LIGHTING.get("default")));

    DISABLE_MINIMAP_ROTATION.put("vanilla", false);
    DISABLE_MINIMAP_ROTATION.put("vanilla_resizable", false);
    DISABLE_MINIMAP_ROTATION.put("lite", true);
    DISABLE_MINIMAP_ROTATION.put("default", true);
    DISABLE_MINIMAP_ROTATION.put("heavy", true);
    DISABLE_MINIMAP_ROTATION.put("all", true);
    DISABLE_MINIMAP_ROTATION.put(
        "custom",
        getPropBoolean(props, "disable_minimap_rotation", DISABLE_MINIMAP_ROTATION.get("default")));

    CAMERA_ZOOMABLE.put("vanilla", false);
    CAMERA_ZOOMABLE.put("vanilla_resizable", false);
    CAMERA_ZOOMABLE.put("lite", true);
    CAMERA_ZOOMABLE.put("default", true);
    CAMERA_ZOOMABLE.put("heavy", true);
    CAMERA_ZOOMABLE.put("all", true);
    CAMERA_ZOOMABLE.put(
        "custom", getPropBoolean(props, "camera_zoomable", CAMERA_ZOOMABLE.get("default")));

    CAMERA_ROTATABLE.put("vanilla", false);
    CAMERA_ROTATABLE.put("vanilla_resizable", false);
    CAMERA_ROTATABLE.put("lite", true);
    CAMERA_ROTATABLE.put("default", true);
    CAMERA_ROTATABLE.put("heavy", true);
    CAMERA_ROTATABLE.put("all", true);
    CAMERA_ROTATABLE.put(
        "custom", getPropBoolean(props, "camera_rotatable", CAMERA_ROTATABLE.get("default")));

    CAMERA_MOVABLE.put("vanilla", false);
    CAMERA_MOVABLE.put("vanilla_resizable", false);
    CAMERA_MOVABLE.put("lite", true);
    CAMERA_MOVABLE.put("default", true);
    CAMERA_MOVABLE.put("heavy", true);
    CAMERA_MOVABLE.put("all", true);
    CAMERA_MOVABLE.put(
        "custom", getPropBoolean(props, "camera_movable", CAMERA_MOVABLE.get("default")));

    CAMERA_MOVABLE_RELATIVE.put("vanilla", false);
    CAMERA_MOVABLE_RELATIVE.put("vanilla_resizable", false);
    CAMERA_MOVABLE_RELATIVE.put("lite", false);
    CAMERA_MOVABLE_RELATIVE.put("default", false);
    CAMERA_MOVABLE_RELATIVE.put("heavy", false);
    CAMERA_MOVABLE_RELATIVE.put("all", false);
    CAMERA_MOVABLE_RELATIVE.put(
        "custom",
        getPropBoolean(props, "camera_movable_relative", CAMERA_MOVABLE_RELATIVE.get("default")));

    COLORIZE_CONSOLE_TEXT.put("vanilla", true);
    COLORIZE_CONSOLE_TEXT.put("vanilla_resizable", true);
    COLORIZE_CONSOLE_TEXT.put("lite", true);
    COLORIZE_CONSOLE_TEXT.put("default", true);
    COLORIZE_CONSOLE_TEXT.put("heavy", true);
    COLORIZE_CONSOLE_TEXT.put("all", true);
    COLORIZE_CONSOLE_TEXT.put(
        "custom", getPropBoolean(props, "colorize", COLORIZE_CONSOLE_TEXT.get("default")));

    FOV.put("vanilla", 9);
    FOV.put("vanilla_resizable", 9);
    FOV.put("lite", 9);
    FOV.put("default", 9);
    FOV.put("heavy", 9);
    FOV.put("all", 9);
    FOV.put("custom", 9);
    // We don't actually want to load this from the settings for usability
    // FOV.put("custom", getPropInt(props, "fov", FOV.get("default")));

    FPS_LIMIT_ENABLED.put("vanilla", false);
    FPS_LIMIT_ENABLED.put("vanilla_resizable", false);
    FPS_LIMIT_ENABLED.put("lite", false);
    FPS_LIMIT_ENABLED.put("default", false);
    FPS_LIMIT_ENABLED.put("heavy", false);
    FPS_LIMIT_ENABLED.put("all", true);
    FPS_LIMIT_ENABLED.put(
        "custom", getPropBoolean(props, "fps_limit_enabled", FPS_LIMIT_ENABLED.get("default")));

    FPS_LIMIT.put("vanilla", 10);
    FPS_LIMIT.put("vanilla_resizable", 10);
    FPS_LIMIT.put("lite", 10);
    FPS_LIMIT.put("default", 10);
    FPS_LIMIT.put("heavy", 10);
    FPS_LIMIT.put("all", 10);
    FPS_LIMIT.put("custom", getPropInt(props, "fps_limit", FPS_LIMIT.get("default")));

    SOFTWARE_CURSOR.put("vanilla", false);
    SOFTWARE_CURSOR.put("vanilla_resizable", false);
    SOFTWARE_CURSOR.put("lite", false);
    SOFTWARE_CURSOR.put("default", false);
    SOFTWARE_CURSOR.put("heavy", false);
    SOFTWARE_CURSOR.put("all", true);
    SOFTWARE_CURSOR.put(
        "custom", getPropBoolean(props, "software_cursor", SOFTWARE_CURSOR.get("default")));

    CTRL_SCROLL_CHAT.put("vanilla", false);
    CTRL_SCROLL_CHAT.put("vanilla_resizable", false);
    CTRL_SCROLL_CHAT.put("lite", false);
    CTRL_SCROLL_CHAT.put("default", true);
    CTRL_SCROLL_CHAT.put("heavy", true);
    CTRL_SCROLL_CHAT.put("all", true);
    CTRL_SCROLL_CHAT.put(
        "custom", getPropBoolean(props, "ctrl_scroll_chat", CTRL_SCROLL_CHAT.get("default")));

    SHIFT_SCROLL_CAMERA_ROTATION.put("vanilla", false);
    SHIFT_SCROLL_CAMERA_ROTATION.put("vanilla_resizable", false);
    SHIFT_SCROLL_CAMERA_ROTATION.put("lite", false);
    SHIFT_SCROLL_CAMERA_ROTATION.put("default", true);
    SHIFT_SCROLL_CAMERA_ROTATION.put("heavy", true);
    SHIFT_SCROLL_CAMERA_ROTATION.put("all", true);
    SHIFT_SCROLL_CAMERA_ROTATION.put(
        "custom",
        getPropBoolean(
            props, "shift_scroll_camera_rotation", SHIFT_SCROLL_CAMERA_ROTATION.get("default")));

    TRACKPAD_ROTATION_SENSITIVITY.put("vanilla", 8);
    TRACKPAD_ROTATION_SENSITIVITY.put("vanilla_resizable", 8);
    TRACKPAD_ROTATION_SENSITIVITY.put("lite", 8);
    TRACKPAD_ROTATION_SENSITIVITY.put("default", 8);
    TRACKPAD_ROTATION_SENSITIVITY.put("heavy", 8);
    TRACKPAD_ROTATION_SENSITIVITY.put("all", 8);
    TRACKPAD_ROTATION_SENSITIVITY.put(
        "custom",
        getPropInt(
            props, "trackpad_rotation_sensitivity", TRACKPAD_ROTATION_SENSITIVITY.get("default")));

    CUSTOM_RAN_CHAT_EFFECT.put("vanilla", RanOverrideEffectType.VANILLA);
    CUSTOM_RAN_CHAT_EFFECT.put("vanilla_resizable", RanOverrideEffectType.VANILLA);
    CUSTOM_RAN_CHAT_EFFECT.put("lite", RanOverrideEffectType.SLOWER);
    CUSTOM_RAN_CHAT_EFFECT.put("default", RanOverrideEffectType.SLOWER);
    CUSTOM_RAN_CHAT_EFFECT.put("heavy", RanOverrideEffectType.GLOW1);
    CUSTOM_RAN_CHAT_EFFECT.put("all", RanOverrideEffectType.GLOW1);
    CUSTOM_RAN_CHAT_EFFECT.put(
        "custom",
        RanOverrideEffectType.getById(
            getPropInt(
                props, "custom_ran_chat_effect", CUSTOM_RAN_CHAT_EFFECT.get("default").id())));

    RAN_EFFECT_TARGET_FPS.put("vanilla", 50);
    RAN_EFFECT_TARGET_FPS.put("vanilla_resizable", 50);
    RAN_EFFECT_TARGET_FPS.put("lite", 10);
    RAN_EFFECT_TARGET_FPS.put("default", 10);
    RAN_EFFECT_TARGET_FPS.put("heavy", 10);
    RAN_EFFECT_TARGET_FPS.put("all", 10);
    RAN_EFFECT_TARGET_FPS.put(
        "custom", getPropInt(props, "ran_effect_target_fps", RAN_EFFECT_TARGET_FPS.get("default")));

    VIEW_DISTANCE.put("vanilla", 2300);
    VIEW_DISTANCE.put("vanilla_resizable", 3000);
    VIEW_DISTANCE.put("lite", 10000);
    VIEW_DISTANCE.put("default", 10000);
    VIEW_DISTANCE.put("heavy", 20000);
    VIEW_DISTANCE.put("all", 20000);
    VIEW_DISTANCE.put("custom", getPropInt(props, "view_distance", VIEW_DISTANCE.get("default")));

    AUTO_SCREENSHOT.put("vanilla", true);
    AUTO_SCREENSHOT.put("vanilla_resizable", true);
    AUTO_SCREENSHOT.put("lite", true);
    AUTO_SCREENSHOT.put("default", true);
    AUTO_SCREENSHOT.put("heavy", true);
    AUTO_SCREENSHOT.put("all", true);
    AUTO_SCREENSHOT.put(
        "custom", getPropBoolean(props, "auto_screenshot", AUTO_SCREENSHOT.get("default")));

    defineStaticPreset(
        SCREENSHOTS_STORAGE_PATH,
        getPropString(props, "screenshots_storage_path", sanitizeDirTextValue(Dir.SCREENSHOT)));

    RS2HD_SKY.put("vanilla", false);
    RS2HD_SKY.put("vanilla_resizable", false);
    RS2HD_SKY.put("lite", false);
    RS2HD_SKY.put("default", false);
    RS2HD_SKY.put("heavy", true);
    RS2HD_SKY.put("all", true);
    RS2HD_SKY.put("custom", getPropBoolean(props, "rs2hd_sky", RS2HD_SKY.get("default")));

    CUSTOM_SKYBOX_OVERWORLD_ENABLED.put("vanilla", false);
    CUSTOM_SKYBOX_OVERWORLD_ENABLED.put("vanilla_resizable", false);
    CUSTOM_SKYBOX_OVERWORLD_ENABLED.put("lite", false);
    CUSTOM_SKYBOX_OVERWORLD_ENABLED.put("default", false);
    CUSTOM_SKYBOX_OVERWORLD_ENABLED.put("heavy", true);
    CUSTOM_SKYBOX_OVERWORLD_ENABLED.put("all", true);
    CUSTOM_SKYBOX_OVERWORLD_ENABLED.put(
        "custom",
        getPropBoolean(
            props,
            "custom_skybox_overworld_enabled",
            CUSTOM_SKYBOX_OVERWORLD_ENABLED.get("default")));

    CUSTOM_SKYBOX_OVERWORLD_COLOUR.put("vanilla", 0);
    CUSTOM_SKYBOX_OVERWORLD_COLOUR.put("vanilla_resizable", 0);
    CUSTOM_SKYBOX_OVERWORLD_COLOUR.put("lite", 0);
    CUSTOM_SKYBOX_OVERWORLD_COLOUR.put("default", 0xBEEF); // moooo
    CUSTOM_SKYBOX_OVERWORLD_COLOUR.put("heavy", 0xBEEF); // it's actually a nice colour
    CUSTOM_SKYBOX_OVERWORLD_COLOUR.put("all", 0xFFB8D5FF); // 117hd sky colour
    CUSTOM_SKYBOX_OVERWORLD_COLOUR.put(
        "custom",
        getPropInt(
            props,
            "custom_skybox_overworld_colour",
            CUSTOM_SKYBOX_OVERWORLD_COLOUR.get("default")));

    CUSTOM_RAN_STATIC_COLOUR.put("vanilla", 0xFFFFFF);
    CUSTOM_RAN_STATIC_COLOUR.put("vanilla_resizable", 0xFFFFFF);
    CUSTOM_RAN_STATIC_COLOUR.put("lite", 0x9933FF);
    CUSTOM_RAN_STATIC_COLOUR.put("default", 0x9933FF); // classic purple
    CUSTOM_RAN_STATIC_COLOUR.put("heavy", 0x9933FF);
    CUSTOM_RAN_STATIC_COLOUR.put("all", 0x68478D); // royal purple alternative
    CUSTOM_RAN_STATIC_COLOUR.put(
        "custom",
        getPropInt(props, "custom_ran_static_colour", CUSTOM_RAN_STATIC_COLOUR.get("default")));

    CUSTOM_SKYBOX_UNDERGROUND_ENABLED.put("vanilla", false);
    CUSTOM_SKYBOX_UNDERGROUND_ENABLED.put("vanilla_resizable", false);
    CUSTOM_SKYBOX_UNDERGROUND_ENABLED.put("lite", false);
    CUSTOM_SKYBOX_UNDERGROUND_ENABLED.put("default", false);
    CUSTOM_SKYBOX_UNDERGROUND_ENABLED.put("heavy", true);
    CUSTOM_SKYBOX_UNDERGROUND_ENABLED.put("all", true);
    CUSTOM_SKYBOX_UNDERGROUND_ENABLED.put(
        "custom",
        getPropBoolean(
            props,
            "custom_skybox_underground_enabled",
            CUSTOM_SKYBOX_UNDERGROUND_ENABLED.get("default")));

    CUSTOM_SKYBOX_UNDERGROUND_COLOUR.put("vanilla", 0);
    CUSTOM_SKYBOX_UNDERGROUND_COLOUR.put("vanilla_resizable", 0);
    CUSTOM_SKYBOX_UNDERGROUND_COLOUR.put("lite", 0);
    CUSTOM_SKYBOX_UNDERGROUND_COLOUR.put("default", 0x101010);
    CUSTOM_SKYBOX_UNDERGROUND_COLOUR.put("heavy", 0x101010);
    CUSTOM_SKYBOX_UNDERGROUND_COLOUR.put("all", 0xFF1D150E);
    CUSTOM_SKYBOX_UNDERGROUND_COLOUR.put(
        "custom",
        getPropInt(
            props,
            "custom_skybox_underground_colour",
            CUSTOM_SKYBOX_UNDERGROUND_COLOUR.get("default")));

    PATCH_GENDER.put("vanilla", false);
    PATCH_GENDER.put("vanilla_resizable", false);
    PATCH_GENDER.put("lite", false);
    PATCH_GENDER.put("default", true);
    PATCH_GENDER.put("heavy", true);
    PATCH_GENDER.put("all", true);
    PATCH_GENDER.put("custom", getPropBoolean(props, "patch_gender", PATCH_GENDER.get("default")));

    PATCH_WRENCH_MENU_SPACING.put("vanilla", false);
    PATCH_WRENCH_MENU_SPACING.put("vanilla_resizable", false);
    PATCH_WRENCH_MENU_SPACING.put("lite", true);
    PATCH_WRENCH_MENU_SPACING.put("default", true);
    PATCH_WRENCH_MENU_SPACING.put("heavy", true);
    PATCH_WRENCH_MENU_SPACING.put("all", true);
    PATCH_WRENCH_MENU_SPACING.put(
        "custom",
        getPropBoolean(
            props, "patch_wrench_menu_spacing", PATCH_WRENCH_MENU_SPACING.get("default")));

    PATCH_HBAR_512_LAST_PIXEL.put("vanilla", false);
    PATCH_HBAR_512_LAST_PIXEL.put("vanilla_resizable", false);
    PATCH_HBAR_512_LAST_PIXEL.put("lite", false);
    PATCH_HBAR_512_LAST_PIXEL.put("default", false);
    PATCH_HBAR_512_LAST_PIXEL.put("heavy", true);
    PATCH_HBAR_512_LAST_PIXEL.put("all", true);
    PATCH_HBAR_512_LAST_PIXEL.put(
        "custom",
        getPropBoolean(
            props, "patch_hbar_512_last_pixel", PATCH_HBAR_512_LAST_PIXEL.get("default")));

    USE_JAGEX_FONTS.put("vanilla", false);
    USE_JAGEX_FONTS.put("vanilla_resizable", false);
    USE_JAGEX_FONTS.put("lite", true);
    USE_JAGEX_FONTS.put("default", true);
    USE_JAGEX_FONTS.put("heavy", true);
    USE_JAGEX_FONTS.put("all", true);
    USE_JAGEX_FONTS.put(
        "custom", getPropBoolean(props, "use_jagex_fonts", USE_JAGEX_FONTS.get("default")));

    LOG_VERBOSITY.put("vanilla", Logger.Type.GAME.id);
    LOG_VERBOSITY.put("vanilla_resizable", Logger.Type.GAME.id);
    LOG_VERBOSITY.put("lite", Logger.Type.WARN.id);
    LOG_VERBOSITY.put("default", Logger.Type.INFO.id);
    LOG_VERBOSITY.put("heavy", Logger.Type.INFO.id);
    LOG_VERBOSITY.put("all", Logger.Type.DEBUG.id);
    LOG_VERBOSITY.put("custom", getPropInt(props, "log_verbosity", LOG_VERBOSITY.get("default")));

    LOG_SHOW_TIMESTAMPS.put("vanilla", true);
    LOG_SHOW_TIMESTAMPS.put("vanilla_resizable", true);
    LOG_SHOW_TIMESTAMPS.put("lite", true);
    LOG_SHOW_TIMESTAMPS.put("default", true);
    LOG_SHOW_TIMESTAMPS.put("heavy", true);
    LOG_SHOW_TIMESTAMPS.put("all", true);
    LOG_SHOW_TIMESTAMPS.put(
        "custom", getPropBoolean(props, "log_show_timestamps", LOG_SHOW_TIMESTAMPS.get("default")));

    LOG_SHOW_LEVEL.put("vanilla", true);
    LOG_SHOW_LEVEL.put("vanilla_resizable", true);
    LOG_SHOW_LEVEL.put("lite", true);
    LOG_SHOW_LEVEL.put("default", true);
    LOG_SHOW_LEVEL.put("heavy", true);
    LOG_SHOW_LEVEL.put("all", true);
    LOG_SHOW_LEVEL.put(
        "custom", getPropBoolean(props, "log_show_level", LOG_SHOW_LEVEL.get("default")));

    LOG_FORCE_TIMESTAMPS.put("vanilla", false);
    LOG_FORCE_TIMESTAMPS.put("vanilla_resizable", false);
    LOG_FORCE_TIMESTAMPS.put("lite", false);
    LOG_FORCE_TIMESTAMPS.put("default", false);
    LOG_FORCE_TIMESTAMPS.put("heavy", false);
    LOG_FORCE_TIMESTAMPS.put("all", true);
    LOG_FORCE_TIMESTAMPS.put(
        "custom",
        getPropBoolean(props, "log_force_timestamps", LOG_FORCE_TIMESTAMPS.get("default")));

    LOG_FORCE_LEVEL.put("vanilla", false);
    LOG_FORCE_LEVEL.put("vanilla_resizable", false);
    LOG_FORCE_LEVEL.put("lite", false);
    LOG_FORCE_LEVEL.put("default", false);
    LOG_FORCE_LEVEL.put("heavy", false);
    LOG_FORCE_LEVEL.put("all", true);
    LOG_FORCE_LEVEL.put(
        "custom", getPropBoolean(props, "log_force_level", LOG_FORCE_LEVEL.get("default")));

    if (!successfullyInitted) {
      Util.hasXdgOpen = Util.detectBinaryAvailable("xdg-open", "URL opening");
    }

    defineStaticPreset(
        PREFERS_XDG_OPEN, getPropBoolean(props, "prefers_xdg_open", Util.hasXdgOpen));

    boolean defaultDarkMode = shouldDefaultDarkMode();
    USE_DARK_FLATLAF.put("vanilla", defaultDarkMode);
    USE_DARK_FLATLAF.put("vanilla_resizable", defaultDarkMode);
    USE_DARK_FLATLAF.put("lite", defaultDarkMode);
    USE_DARK_FLATLAF.put("default", defaultDarkMode);
    USE_DARK_FLATLAF.put("heavy", defaultDarkMode);
    USE_DARK_FLATLAF.put("all", defaultDarkMode);
    USE_DARK_FLATLAF.put(
        "custom", getPropBoolean(props, "use_dark_flatlaf", USE_DARK_FLATLAF.get("default")));

    USE_NIMBUS_THEME.put("vanilla", false);
    USE_NIMBUS_THEME.put("vanilla_resizable", false);
    USE_NIMBUS_THEME.put("lite", false);
    USE_NIMBUS_THEME.put("default", false);
    USE_NIMBUS_THEME.put("heavy", false);
    USE_NIMBUS_THEME.put("all", false);
    USE_NIMBUS_THEME.put(
        "custom", getPropBoolean(props, "use_nimbus_theme", USE_NIMBUS_THEME.get("default")));

    //// audio settings
    CUSTOM_MUSIC.put("vanilla", false);
    CUSTOM_MUSIC.put("vanilla_resizable", false);
    CUSTOM_MUSIC.put("lite", false);
    CUSTOM_MUSIC.put("default", true);
    CUSTOM_MUSIC.put("heavy", true);
    CUSTOM_MUSIC.put("all", true);
    CUSTOM_MUSIC.put("custom", getPropBoolean(props, "custom_music", CUSTOM_MUSIC.get("default")));

    defineStaticPreset(
        CUSTOM_MUSIC_PATH, getPropString(props, "custom_music_path", "mods/music.zip"));

    SFX_VOLUME.put("vanilla", 100);
    SFX_VOLUME.put("vanilla_resizable", 100);
    SFX_VOLUME.put("lite", 100);
    SFX_VOLUME.put("default", 100);
    SFX_VOLUME.put("heavy", 100);
    SFX_VOLUME.put("all", 100);
    SFX_VOLUME.put("custom", 100);
    SFX_VOLUME.put("custom", getPropInt(props, "sfx_volume", SFX_VOLUME.get("default")));

    LOUDER_SOUND_EFFECTS.put("vanilla", false);
    LOUDER_SOUND_EFFECTS.put("vanilla_resizable", false);
    LOUDER_SOUND_EFFECTS.put("lite", false);
    LOUDER_SOUND_EFFECTS.put("default", false);
    LOUDER_SOUND_EFFECTS.put("heavy", true);
    LOUDER_SOUND_EFFECTS.put("all", true);
    LOUDER_SOUND_EFFECTS.put(
        "custom",
        getPropBoolean(props, "louder_sound_effects", LOUDER_SOUND_EFFECTS.get("default")));

    OVERRIDE_AUDIO_SETTING.put("vanilla", false);
    OVERRIDE_AUDIO_SETTING.put("vanilla_resizable", false);
    OVERRIDE_AUDIO_SETTING.put("lite", false);
    OVERRIDE_AUDIO_SETTING.put("default", false);
    OVERRIDE_AUDIO_SETTING.put("heavy", true);
    OVERRIDE_AUDIO_SETTING.put("all", true);
    OVERRIDE_AUDIO_SETTING.put(
        "custom",
        getPropBoolean(props, "override_audio_setting", OVERRIDE_AUDIO_SETTING.get("default")));

    OVERRIDE_AUDIO_SETTING_SETTING_ON.put("vanilla", false);
    OVERRIDE_AUDIO_SETTING_SETTING_ON.put("vanilla_resizable", false);
    OVERRIDE_AUDIO_SETTING_SETTING_ON.put("lite", false);
    OVERRIDE_AUDIO_SETTING_SETTING_ON.put("default", false);
    OVERRIDE_AUDIO_SETTING_SETTING_ON.put("heavy", false);
    OVERRIDE_AUDIO_SETTING_SETTING_ON.put("all", true);
    OVERRIDE_AUDIO_SETTING_SETTING_ON.put(
        "custom",
        getPropBoolean(
            props,
            "override_audio_setting_setting_on",
            OVERRIDE_AUDIO_SETTING_SETTING_ON.get("default")));

    SOUND_EFFECT_COMBAT1.put("vanilla", false);
    SOUND_EFFECT_COMBAT1.put("vanilla_resizable", false);
    SOUND_EFFECT_COMBAT1.put("lite", false);
    SOUND_EFFECT_COMBAT1.put("default", false);
    SOUND_EFFECT_COMBAT1.put("heavy", true);
    SOUND_EFFECT_COMBAT1.put("all", true);
    SOUND_EFFECT_COMBAT1.put(
        "custom",
        getPropBoolean(props, "sound_effect_combat1", SOUND_EFFECT_COMBAT1.get("default")));

    SOUND_EFFECT_ADVANCE.put("vanilla", true);
    SOUND_EFFECT_ADVANCE.put("vanilla_resizable", true);
    SOUND_EFFECT_ADVANCE.put("lite", true);
    SOUND_EFFECT_ADVANCE.put("default", true);
    SOUND_EFFECT_ADVANCE.put("heavy", true);
    SOUND_EFFECT_ADVANCE.put("all", true);
    SOUND_EFFECT_ADVANCE.put(
        "custom",
        getPropBoolean(props, "sound_effect_advance", SOUND_EFFECT_ADVANCE.get("default")));

    SOUND_EFFECT_ANVIL.put("vanilla", true);
    SOUND_EFFECT_ANVIL.put("vanilla_resizable", true);
    SOUND_EFFECT_ANVIL.put("lite", true);
    SOUND_EFFECT_ANVIL.put("default", true);
    SOUND_EFFECT_ANVIL.put("heavy", true);
    SOUND_EFFECT_ANVIL.put("all", true);
    SOUND_EFFECT_ANVIL.put(
        "custom", getPropBoolean(props, "sound_effect_anvil", SOUND_EFFECT_ANVIL.get("default")));

    SOUND_EFFECT_CHISEL.put("vanilla", true);
    SOUND_EFFECT_CHISEL.put("vanilla_resizable", true);
    SOUND_EFFECT_CHISEL.put("lite", true);
    SOUND_EFFECT_CHISEL.put("default", true);
    SOUND_EFFECT_CHISEL.put("heavy", true);
    SOUND_EFFECT_CHISEL.put("all", true);
    SOUND_EFFECT_CHISEL.put(
        "custom", getPropBoolean(props, "sound_effect_chisel", SOUND_EFFECT_CHISEL.get("default")));

    SOUND_EFFECT_CLICK.put("vanilla", true);
    SOUND_EFFECT_CLICK.put("vanilla_resizable", true);
    SOUND_EFFECT_CLICK.put("lite", true);
    SOUND_EFFECT_CLICK.put("default", true);
    SOUND_EFFECT_CLICK.put("heavy", true);
    SOUND_EFFECT_CLICK.put("all", true);
    SOUND_EFFECT_CLICK.put(
        "custom", getPropBoolean(props, "sound_effect_click", SOUND_EFFECT_CLICK.get("default")));

    SOUND_EFFECT_CLOSEDOOR.put("vanilla", true);
    SOUND_EFFECT_CLOSEDOOR.put("vanilla_resizable", true);
    SOUND_EFFECT_CLOSEDOOR.put("lite", true);
    SOUND_EFFECT_CLOSEDOOR.put("default", true);
    SOUND_EFFECT_CLOSEDOOR.put("heavy", true);
    SOUND_EFFECT_CLOSEDOOR.put("all", true);
    SOUND_EFFECT_CLOSEDOOR.put(
        "custom",
        getPropBoolean(props, "sound_effect_closedoor", SOUND_EFFECT_CLOSEDOOR.get("default")));

    SOUND_EFFECT_COINS.put("vanilla", true);
    SOUND_EFFECT_COINS.put("vanilla_resizable", true);
    SOUND_EFFECT_COINS.put("lite", true);
    SOUND_EFFECT_COINS.put("default", true);
    SOUND_EFFECT_COINS.put("heavy", true);
    SOUND_EFFECT_COINS.put("all", true);
    SOUND_EFFECT_COINS.put(
        "custom", getPropBoolean(props, "sound_effect_coins", SOUND_EFFECT_COINS.get("default")));

    SOUND_EFFECT_COMBAT1A.put("vanilla", true);
    SOUND_EFFECT_COMBAT1A.put("vanilla_resizable", true);
    SOUND_EFFECT_COMBAT1A.put("lite", true);
    SOUND_EFFECT_COMBAT1A.put("default", true);
    SOUND_EFFECT_COMBAT1A.put("heavy", true);
    SOUND_EFFECT_COMBAT1A.put("all", true);
    SOUND_EFFECT_COMBAT1A.put(
        "custom",
        getPropBoolean(props, "sound_effect_combat1a", SOUND_EFFECT_COMBAT1A.get("default")));

    SOUND_EFFECT_COMBAT1B.put("vanilla", true);
    SOUND_EFFECT_COMBAT1B.put("vanilla_resizable", true);
    SOUND_EFFECT_COMBAT1B.put("lite", true);
    SOUND_EFFECT_COMBAT1B.put("default", true);
    SOUND_EFFECT_COMBAT1B.put("heavy", true);
    SOUND_EFFECT_COMBAT1B.put("all", true);
    SOUND_EFFECT_COMBAT1B.put(
        "custom",
        getPropBoolean(props, "sound_effect_combat1b", SOUND_EFFECT_COMBAT1B.get("default")));

    SOUND_EFFECT_COMBAT2A.put("vanilla", true);
    SOUND_EFFECT_COMBAT2A.put("vanilla_resizable", true);
    SOUND_EFFECT_COMBAT2A.put("lite", true);
    SOUND_EFFECT_COMBAT2A.put("default", true);
    SOUND_EFFECT_COMBAT2A.put("heavy", true);
    SOUND_EFFECT_COMBAT2A.put("all", true);
    SOUND_EFFECT_COMBAT2A.put(
        "custom",
        getPropBoolean(props, "sound_effect_combat2a", SOUND_EFFECT_COMBAT2A.get("default")));

    SOUND_EFFECT_COMBAT2B.put("vanilla", true);
    SOUND_EFFECT_COMBAT2B.put("vanilla_resizable", true);
    SOUND_EFFECT_COMBAT2B.put("lite", true);
    SOUND_EFFECT_COMBAT2B.put("default", true);
    SOUND_EFFECT_COMBAT2B.put("heavy", true);
    SOUND_EFFECT_COMBAT2B.put("all", true);
    SOUND_EFFECT_COMBAT2B.put(
        "custom",
        getPropBoolean(props, "sound_effect_combat2b", SOUND_EFFECT_COMBAT2B.get("default")));

    SOUND_EFFECT_COMBAT3A.put("vanilla", true);
    SOUND_EFFECT_COMBAT3A.put("vanilla_resizable", true);
    SOUND_EFFECT_COMBAT3A.put("lite", true);
    SOUND_EFFECT_COMBAT3A.put("default", true);
    SOUND_EFFECT_COMBAT3A.put("heavy", true);
    SOUND_EFFECT_COMBAT3A.put("all", true);
    SOUND_EFFECT_COMBAT3A.put(
        "custom",
        getPropBoolean(props, "sound_effect_combat3a", SOUND_EFFECT_COMBAT3A.get("default")));

    SOUND_EFFECT_COMBAT3B.put("vanilla", true);
    SOUND_EFFECT_COMBAT3B.put("vanilla_resizable", true);
    SOUND_EFFECT_COMBAT3B.put("lite", true);
    SOUND_EFFECT_COMBAT3B.put("default", true);
    SOUND_EFFECT_COMBAT3B.put("heavy", true);
    SOUND_EFFECT_COMBAT3B.put("all", true);
    SOUND_EFFECT_COMBAT3B.put(
        "custom",
        getPropBoolean(props, "sound_effect_combat3b", SOUND_EFFECT_COMBAT3B.get("default")));

    SOUND_EFFECT_COOKING.put("vanilla", true);
    SOUND_EFFECT_COOKING.put("vanilla_resizable", true);
    SOUND_EFFECT_COOKING.put("lite", true);
    SOUND_EFFECT_COOKING.put("default", true);
    SOUND_EFFECT_COOKING.put("heavy", true);
    SOUND_EFFECT_COOKING.put("all", true);
    SOUND_EFFECT_COOKING.put(
        "custom",
        getPropBoolean(props, "sound_effect_cooking", SOUND_EFFECT_COOKING.get("default")));

    SOUND_EFFECT_DEATH.put("vanilla", true);
    SOUND_EFFECT_DEATH.put("vanilla_resizable", true);
    SOUND_EFFECT_DEATH.put("lite", true);
    SOUND_EFFECT_DEATH.put("default", true);
    SOUND_EFFECT_DEATH.put("heavy", true);
    SOUND_EFFECT_DEATH.put("all", true);
    SOUND_EFFECT_DEATH.put(
        "custom", getPropBoolean(props, "sound_effect_death", SOUND_EFFECT_DEATH.get("default")));

    SOUND_EFFECT_DROPOBJECT.put("vanilla", true);
    SOUND_EFFECT_DROPOBJECT.put("vanilla_resizable", true);
    SOUND_EFFECT_DROPOBJECT.put("lite", true);
    SOUND_EFFECT_DROPOBJECT.put("default", true);
    SOUND_EFFECT_DROPOBJECT.put("heavy", true);
    SOUND_EFFECT_DROPOBJECT.put("all", true);
    SOUND_EFFECT_DROPOBJECT.put(
        "custom",
        getPropBoolean(props, "sound_effect_dropobject", SOUND_EFFECT_DROPOBJECT.get("default")));

    SOUND_EFFECT_EAT.put("vanilla", true);
    SOUND_EFFECT_EAT.put("vanilla_resizable", true);
    SOUND_EFFECT_EAT.put("lite", true);
    SOUND_EFFECT_EAT.put("default", true);
    SOUND_EFFECT_EAT.put("heavy", true);
    SOUND_EFFECT_EAT.put("all", true);
    SOUND_EFFECT_EAT.put(
        "custom", getPropBoolean(props, "sound_effect_eat", SOUND_EFFECT_EAT.get("default")));

    SOUND_EFFECT_FILLJUG.put("vanilla", true);
    SOUND_EFFECT_FILLJUG.put("vanilla_resizable", true);
    SOUND_EFFECT_FILLJUG.put("lite", true);
    SOUND_EFFECT_FILLJUG.put("default", true);
    SOUND_EFFECT_FILLJUG.put("heavy", true);
    SOUND_EFFECT_FILLJUG.put("all", true);
    SOUND_EFFECT_FILLJUG.put(
        "custom",
        getPropBoolean(props, "sound_effect_filljug", SOUND_EFFECT_FILLJUG.get("default")));

    SOUND_EFFECT_FISH.put("vanilla", true);
    SOUND_EFFECT_FISH.put("vanilla_resizable", true);
    SOUND_EFFECT_FISH.put("lite", true);
    SOUND_EFFECT_FISH.put("default", true);
    SOUND_EFFECT_FISH.put("heavy", true);
    SOUND_EFFECT_FISH.put("all", true);
    SOUND_EFFECT_FISH.put(
        "custom", getPropBoolean(props, "sound_effect_fish", SOUND_EFFECT_FISH.get("default")));

    SOUND_EFFECT_FOUNDGEM.put("vanilla", true);
    SOUND_EFFECT_FOUNDGEM.put("vanilla_resizable", true);
    SOUND_EFFECT_FOUNDGEM.put("lite", true);
    SOUND_EFFECT_FOUNDGEM.put("default", true);
    SOUND_EFFECT_FOUNDGEM.put("heavy", true);
    SOUND_EFFECT_FOUNDGEM.put("all", true);
    SOUND_EFFECT_FOUNDGEM.put(
        "custom",
        getPropBoolean(props, "sound_effect_foundgem", SOUND_EFFECT_FOUNDGEM.get("default")));

    SOUND_EFFECT_MECHANICAL.put("vanilla", true);
    SOUND_EFFECT_MECHANICAL.put("vanilla_resizable", true);
    SOUND_EFFECT_MECHANICAL.put("lite", true);
    SOUND_EFFECT_MECHANICAL.put("default", true);
    SOUND_EFFECT_MECHANICAL.put("heavy", true);
    SOUND_EFFECT_MECHANICAL.put("all", true);
    SOUND_EFFECT_MECHANICAL.put(
        "custom",
        getPropBoolean(props, "sound_effect_mechanical", SOUND_EFFECT_MECHANICAL.get("default")));

    SOUND_EFFECT_MINE.put("vanilla", true);
    SOUND_EFFECT_MINE.put("vanilla_resizable", true);
    SOUND_EFFECT_MINE.put("lite", true);
    SOUND_EFFECT_MINE.put("default", true);
    SOUND_EFFECT_MINE.put("heavy", true);
    SOUND_EFFECT_MINE.put("all", true);
    SOUND_EFFECT_MINE.put(
        "custom", getPropBoolean(props, "sound_effect_mine", SOUND_EFFECT_MINE.get("default")));

    SOUND_EFFECT_MIX.put("vanilla", true);
    SOUND_EFFECT_MIX.put("vanilla_resizable", true);
    SOUND_EFFECT_MIX.put("lite", true);
    SOUND_EFFECT_MIX.put("default", true);
    SOUND_EFFECT_MIX.put("heavy", true);
    SOUND_EFFECT_MIX.put("all", true);
    SOUND_EFFECT_MIX.put(
        "custom", getPropBoolean(props, "sound_effect_mix", SOUND_EFFECT_MIX.get("default")));

    SOUND_EFFECT_OPENDOOR.put("vanilla", true);
    SOUND_EFFECT_OPENDOOR.put("vanilla_resizable", true);
    SOUND_EFFECT_OPENDOOR.put("lite", true);
    SOUND_EFFECT_OPENDOOR.put("default", true);
    SOUND_EFFECT_OPENDOOR.put("heavy", true);
    SOUND_EFFECT_OPENDOOR.put("all", true);
    SOUND_EFFECT_OPENDOOR.put(
        "custom",
        getPropBoolean(props, "sound_effect_opendoor", SOUND_EFFECT_OPENDOOR.get("default")));

    SOUND_EFFECT_OUTOFAMMO.put("vanilla", true);
    SOUND_EFFECT_OUTOFAMMO.put("vanilla_resizable", true);
    SOUND_EFFECT_OUTOFAMMO.put("lite", true);
    SOUND_EFFECT_OUTOFAMMO.put("default", true);
    SOUND_EFFECT_OUTOFAMMO.put("heavy", true);
    SOUND_EFFECT_OUTOFAMMO.put("all", true);
    SOUND_EFFECT_OUTOFAMMO.put(
        "custom",
        getPropBoolean(props, "sound_effect_outofammo", SOUND_EFFECT_OUTOFAMMO.get("default")));

    SOUND_EFFECT_POTATO.put("vanilla", true);
    SOUND_EFFECT_POTATO.put("vanilla_resizable", true);
    SOUND_EFFECT_POTATO.put("lite", true);
    SOUND_EFFECT_POTATO.put("default", true);
    SOUND_EFFECT_POTATO.put("heavy", true);
    SOUND_EFFECT_POTATO.put("all", true);
    SOUND_EFFECT_POTATO.put(
        "custom", getPropBoolean(props, "sound_effect_potato", SOUND_EFFECT_POTATO.get("default")));

    SOUND_EFFECT_PRAYEROFF.put("vanilla", true);
    SOUND_EFFECT_PRAYEROFF.put("vanilla_resizable", true);
    SOUND_EFFECT_PRAYEROFF.put("lite", true);
    SOUND_EFFECT_PRAYEROFF.put("default", true);
    SOUND_EFFECT_PRAYEROFF.put("heavy", true);
    SOUND_EFFECT_PRAYEROFF.put("all", true);
    SOUND_EFFECT_PRAYEROFF.put(
        "custom",
        getPropBoolean(props, "sound_effect_prayeroff", SOUND_EFFECT_PRAYEROFF.get("default")));

    SOUND_EFFECT_PRAYERON.put("vanilla", true);
    SOUND_EFFECT_PRAYERON.put("vanilla_resizable", true);
    SOUND_EFFECT_PRAYERON.put("lite", true);
    SOUND_EFFECT_PRAYERON.put("default", true);
    SOUND_EFFECT_PRAYERON.put("heavy", true);
    SOUND_EFFECT_PRAYERON.put("all", true);
    SOUND_EFFECT_PRAYERON.put(
        "custom",
        getPropBoolean(props, "sound_effect_prayeron", SOUND_EFFECT_PRAYERON.get("default")));

    SOUND_EFFECT_PROSPECT.put("vanilla", true);
    SOUND_EFFECT_PROSPECT.put("vanilla_resizable", true);
    SOUND_EFFECT_PROSPECT.put("lite", true);
    SOUND_EFFECT_PROSPECT.put("default", true);
    SOUND_EFFECT_PROSPECT.put("heavy", true);
    SOUND_EFFECT_PROSPECT.put("all", true);
    SOUND_EFFECT_PROSPECT.put(
        "custom",
        getPropBoolean(props, "sound_effect_prospect", SOUND_EFFECT_PROSPECT.get("default")));

    SOUND_EFFECT_RECHARGE.put("vanilla", true);
    SOUND_EFFECT_RECHARGE.put("vanilla_resizable", true);
    SOUND_EFFECT_RECHARGE.put("lite", true);
    SOUND_EFFECT_RECHARGE.put("default", true);
    SOUND_EFFECT_RECHARGE.put("heavy", true);
    SOUND_EFFECT_RECHARGE.put("all", true);
    SOUND_EFFECT_RECHARGE.put(
        "custom",
        getPropBoolean(props, "sound_effect_recharge", SOUND_EFFECT_RECHARGE.get("default")));

    SOUND_EFFECT_RETREAT.put("vanilla", true);
    SOUND_EFFECT_RETREAT.put("vanilla_resizable", true);
    SOUND_EFFECT_RETREAT.put("lite", true);
    SOUND_EFFECT_RETREAT.put("default", true);
    SOUND_EFFECT_RETREAT.put("heavy", true);
    SOUND_EFFECT_RETREAT.put("all", true);
    SOUND_EFFECT_RETREAT.put(
        "custom",
        getPropBoolean(props, "sound_effect_retreat", SOUND_EFFECT_RETREAT.get("default")));

    SOUND_EFFECT_SECRETDOOR.put("vanilla", true);
    SOUND_EFFECT_SECRETDOOR.put("vanilla_resizable", true);
    SOUND_EFFECT_SECRETDOOR.put("lite", true);
    SOUND_EFFECT_SECRETDOOR.put("default", true);
    SOUND_EFFECT_SECRETDOOR.put("heavy", true);
    SOUND_EFFECT_SECRETDOOR.put("all", true);
    SOUND_EFFECT_SECRETDOOR.put(
        "custom",
        getPropBoolean(props, "sound_effect_secretdoor", SOUND_EFFECT_SECRETDOOR.get("default")));

    SOUND_EFFECT_SHOOT.put("vanilla", true);
    SOUND_EFFECT_SHOOT.put("vanilla_resizable", true);
    SOUND_EFFECT_SHOOT.put("lite", true);
    SOUND_EFFECT_SHOOT.put("default", true);
    SOUND_EFFECT_SHOOT.put("heavy", true);
    SOUND_EFFECT_SHOOT.put("all", true);
    SOUND_EFFECT_SHOOT.put(
        "custom", getPropBoolean(props, "sound_effect_shoot", SOUND_EFFECT_SHOOT.get("default")));

    SOUND_EFFECT_SPELLFAIL.put("vanilla", true);
    SOUND_EFFECT_SPELLFAIL.put("vanilla_resizable", true);
    SOUND_EFFECT_SPELLFAIL.put("lite", true);
    SOUND_EFFECT_SPELLFAIL.put("default", true);
    SOUND_EFFECT_SPELLFAIL.put("heavy", true);
    SOUND_EFFECT_SPELLFAIL.put("all", true);
    SOUND_EFFECT_SPELLFAIL.put(
        "custom",
        getPropBoolean(props, "sound_effect_spellfail", SOUND_EFFECT_SPELLFAIL.get("default")));

    SOUND_EFFECT_SPELLOK.put("vanilla", true);
    SOUND_EFFECT_SPELLOK.put("vanilla_resizable", true);
    SOUND_EFFECT_SPELLOK.put("lite", true);
    SOUND_EFFECT_SPELLOK.put("default", true);
    SOUND_EFFECT_SPELLOK.put("heavy", true);
    SOUND_EFFECT_SPELLOK.put("all", true);
    SOUND_EFFECT_SPELLOK.put(
        "custom",
        getPropBoolean(props, "sound_effect_spellok", SOUND_EFFECT_SPELLOK.get("default")));

    SOUND_EFFECT_TAKEOBJECT.put("vanilla", true);
    SOUND_EFFECT_TAKEOBJECT.put("vanilla_resizable", true);
    SOUND_EFFECT_TAKEOBJECT.put("lite", true);
    SOUND_EFFECT_TAKEOBJECT.put("default", true);
    SOUND_EFFECT_TAKEOBJECT.put("heavy", true);
    SOUND_EFFECT_TAKEOBJECT.put("all", true);
    SOUND_EFFECT_TAKEOBJECT.put(
        "custom",
        getPropBoolean(props, "sound_effect_takeobject", SOUND_EFFECT_TAKEOBJECT.get("default")));

    SOUND_EFFECT_UNDERATTACK.put("vanilla", true);
    SOUND_EFFECT_UNDERATTACK.put("vanilla_resizable", true);
    SOUND_EFFECT_UNDERATTACK.put("lite", true);
    SOUND_EFFECT_UNDERATTACK.put("default", true);
    SOUND_EFFECT_UNDERATTACK.put("heavy", true);
    SOUND_EFFECT_UNDERATTACK.put("all", true);
    SOUND_EFFECT_UNDERATTACK.put(
        "custom",
        getPropBoolean(props, "sound_effect_underattack", SOUND_EFFECT_UNDERATTACK.get("default")));

    SOUND_EFFECT_VICTORY.put("vanilla", true);
    SOUND_EFFECT_VICTORY.put("vanilla_resizable", true);
    SOUND_EFFECT_VICTORY.put("lite", true);
    SOUND_EFFECT_VICTORY.put("default", true);
    SOUND_EFFECT_VICTORY.put("heavy", true);
    SOUND_EFFECT_VICTORY.put("all", true);
    SOUND_EFFECT_VICTORY.put(
        "custom",
        getPropBoolean(props, "sound_effect_victory", SOUND_EFFECT_VICTORY.get("default")));

    //// overlays
    SHOW_HP_PRAYER_FATIGUE_OVERLAY.put("vanilla", false);
    SHOW_HP_PRAYER_FATIGUE_OVERLAY.put("vanilla_resizable", false);
    SHOW_HP_PRAYER_FATIGUE_OVERLAY.put("lite", true);
    SHOW_HP_PRAYER_FATIGUE_OVERLAY.put("default", true);
    SHOW_HP_PRAYER_FATIGUE_OVERLAY.put("heavy", true);
    SHOW_HP_PRAYER_FATIGUE_OVERLAY.put("all", true);
    SHOW_HP_PRAYER_FATIGUE_OVERLAY.put(
        "custom",
        getPropBoolean(props, "show_statusdisplay", SHOW_HP_PRAYER_FATIGUE_OVERLAY.get("default")));

    ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.put("vanilla", false);
    ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.put("vanilla_resizable", false);
    ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.put("lite", false);
    ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.put("default", false);
    ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.put("heavy", false);
    ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.put("all", false);
    ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.put(
        "custom",
        getPropBoolean(
            props,
            "always_show_statusdisplay_text",
            ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.get("default")));

    SHOW_BUFFS.put("vanilla", false);
    SHOW_BUFFS.put("vanilla_resizable", false);
    SHOW_BUFFS.put("lite", true);
    SHOW_BUFFS.put("default", true);
    SHOW_BUFFS.put("heavy", true);
    SHOW_BUFFS.put("all", true);
    SHOW_BUFFS.put("custom", getPropBoolean(props, "show_buffs", SHOW_BUFFS.get("default")));

    DEATH_ITEMS.put("vanilla", false);
    DEATH_ITEMS.put("vanilla_resizable", false);
    DEATH_ITEMS.put("lite", false);
    DEATH_ITEMS.put("default", false);
    DEATH_ITEMS.put("heavy", true);
    DEATH_ITEMS.put("all", true);
    DEATH_ITEMS.put("custom", getPropBoolean(props, "death_items", DEATH_ITEMS.get("default")));

    DEATH_ITEMS_WILD.put("vanilla", false);
    DEATH_ITEMS_WILD.put("vanilla_resizable", false);
    DEATH_ITEMS_WILD.put("lite", false);
    DEATH_ITEMS_WILD.put("default", false);
    DEATH_ITEMS_WILD.put("heavy", false);
    DEATH_ITEMS_WILD.put("all", false);
    DEATH_ITEMS_WILD.put(
        "custom", getPropBoolean(props, "death_items_wild", DEATH_ITEMS_WILD.get("default")));

    SHOW_LAST_MENU_ACTION.put("vanilla", false);
    SHOW_LAST_MENU_ACTION.put("vanilla_resizable", false);
    SHOW_LAST_MENU_ACTION.put("lite", false);
    SHOW_LAST_MENU_ACTION.put("default", false);
    SHOW_LAST_MENU_ACTION.put("heavy", true);
    SHOW_LAST_MENU_ACTION.put("all", true);
    SHOW_LAST_MENU_ACTION.put(
        "custom",
        getPropBoolean(props, "show_last_menu_action", SHOW_LAST_MENU_ACTION.get("default")));

    SHOW_MOUSE_TOOLTIP.put("vanilla", false);
    SHOW_MOUSE_TOOLTIP.put("vanilla_resizable", false);
    SHOW_MOUSE_TOOLTIP.put("lite", false);
    SHOW_MOUSE_TOOLTIP.put("default", false);
    SHOW_MOUSE_TOOLTIP.put("heavy", true);
    SHOW_MOUSE_TOOLTIP.put("all", true);
    SHOW_MOUSE_TOOLTIP.put(
        "custom", getPropBoolean(props, "show_mouse_tooltip", SHOW_MOUSE_TOOLTIP.get("default")));

    SHOW_EXTENDED_TOOLTIP.put("vanilla", false);
    SHOW_EXTENDED_TOOLTIP.put("vanilla_resizable", false);
    SHOW_EXTENDED_TOOLTIP.put("lite", false);
    SHOW_EXTENDED_TOOLTIP.put("default", true);
    SHOW_EXTENDED_TOOLTIP.put("heavy", true);
    SHOW_EXTENDED_TOOLTIP.put("all", true);
    SHOW_EXTENDED_TOOLTIP.put(
        "custom",
        getPropBoolean(props, "show_extended_tooltip", SHOW_EXTENDED_TOOLTIP.get("default")));

    SHOW_INVCOUNT.put("vanilla", false);
    SHOW_INVCOUNT.put("vanilla_resizable", false);
    SHOW_INVCOUNT.put("lite", true);
    SHOW_INVCOUNT.put("default", true);
    SHOW_INVCOUNT.put("heavy", true);
    SHOW_INVCOUNT.put("all", true);
    SHOW_INVCOUNT.put(
        "custom", getPropBoolean(props, "show_invcount", SHOW_INVCOUNT.get("default")));

    SHOW_INVCOUNT_COLOURS.put("vanilla", false);
    SHOW_INVCOUNT_COLOURS.put("vanilla_resizable", false);
    SHOW_INVCOUNT_COLOURS.put("lite", false);
    SHOW_INVCOUNT_COLOURS.put("default", false);
    SHOW_INVCOUNT_COLOURS.put("heavy", true);
    SHOW_INVCOUNT_COLOURS.put("all", true);
    SHOW_INVCOUNT_COLOURS.put(
        "custom",
        getPropBoolean(props, "show_invcount_colours", SHOW_INVCOUNT_COLOURS.get("default")));

    SHOW_RSCPLUS_BUTTONS.put("vanilla", false);
    SHOW_RSCPLUS_BUTTONS.put("vanilla_resizable", false);
    SHOW_RSCPLUS_BUTTONS.put("lite", true);
    SHOW_RSCPLUS_BUTTONS.put("default", true);
    SHOW_RSCPLUS_BUTTONS.put("heavy", true);
    SHOW_RSCPLUS_BUTTONS.put("all", true);
    SHOW_RSCPLUS_BUTTONS.put(
        "custom",
        getPropBoolean(props, "show_rscplus_buttons", SHOW_RSCPLUS_BUTTONS.get("default")));

    RSCPLUS_BUTTONS_FUNCTIONAL.put("vanilla", false);
    RSCPLUS_BUTTONS_FUNCTIONAL.put("vanilla_resizable", false);
    RSCPLUS_BUTTONS_FUNCTIONAL.put("lite", true);
    RSCPLUS_BUTTONS_FUNCTIONAL.put("default", true);
    RSCPLUS_BUTTONS_FUNCTIONAL.put("heavy", true);
    RSCPLUS_BUTTONS_FUNCTIONAL.put("all", true);
    RSCPLUS_BUTTONS_FUNCTIONAL.put(
        "custom",
        getPropBoolean(
            props, "rscplus_buttons_functional", RSCPLUS_BUTTONS_FUNCTIONAL.get("default")));

    WIKI_LOOKUP_ON_MAGIC_BOOK.put("vanilla", false);
    WIKI_LOOKUP_ON_MAGIC_BOOK.put("vanilla_resizable", false);
    WIKI_LOOKUP_ON_MAGIC_BOOK.put("lite", false);
    WIKI_LOOKUP_ON_MAGIC_BOOK.put("default", false);
    WIKI_LOOKUP_ON_MAGIC_BOOK.put("heavy", true);
    WIKI_LOOKUP_ON_MAGIC_BOOK.put("all", true);
    WIKI_LOOKUP_ON_MAGIC_BOOK.put(
        "custom",
        getPropBoolean(
            props, "wiki_lookup_on_magic_book", WIKI_LOOKUP_ON_MAGIC_BOOK.get("default")));

    MOTIVATIONAL_QUOTES_BUTTON.put("vanilla", false);
    MOTIVATIONAL_QUOTES_BUTTON.put("vanilla_resizable", false);
    MOTIVATIONAL_QUOTES_BUTTON.put("lite", false);
    MOTIVATIONAL_QUOTES_BUTTON.put("default", false);
    MOTIVATIONAL_QUOTES_BUTTON.put("heavy", true);
    MOTIVATIONAL_QUOTES_BUTTON.put("all", true);
    MOTIVATIONAL_QUOTES_BUTTON.put(
        "custom",
        getPropBoolean(
            props, "motivational_quotes_button", MOTIVATIONAL_QUOTES_BUTTON.get("default")));

    TOGGLE_XP_BAR_ON_STATS_BUTTON.put("vanilla", false);
    TOGGLE_XP_BAR_ON_STATS_BUTTON.put("vanilla_resizable", false);
    TOGGLE_XP_BAR_ON_STATS_BUTTON.put("lite", false);
    TOGGLE_XP_BAR_ON_STATS_BUTTON.put("default", true);
    TOGGLE_XP_BAR_ON_STATS_BUTTON.put("heavy", true);
    TOGGLE_XP_BAR_ON_STATS_BUTTON.put("all", true);
    TOGGLE_XP_BAR_ON_STATS_BUTTON.put(
        "custom",
        getPropBoolean(
            props, "toggle_xp_bar_on_stats_button", TOGGLE_XP_BAR_ON_STATS_BUTTON.get("default")));

    HISCORES_LOOKUP_BUTTON.put("vanilla", false);
    HISCORES_LOOKUP_BUTTON.put("vanilla_resizable", false);
    HISCORES_LOOKUP_BUTTON.put("lite", false);
    HISCORES_LOOKUP_BUTTON.put("default", false);
    HISCORES_LOOKUP_BUTTON.put("heavy", false);
    HISCORES_LOOKUP_BUTTON.put("all", true);
    HISCORES_LOOKUP_BUTTON.put(
        "custom",
        getPropBoolean(props, "hiscores_lookup_button", HISCORES_LOOKUP_BUTTON.get("default")));

    WIKI_LOOKUP_ON_HBAR.put("vanilla", false);
    WIKI_LOOKUP_ON_HBAR.put("vanilla_resizable", false);
    WIKI_LOOKUP_ON_HBAR.put("lite", false);
    WIKI_LOOKUP_ON_HBAR.put("default", true);
    WIKI_LOOKUP_ON_HBAR.put("heavy", true);
    WIKI_LOOKUP_ON_HBAR.put("all", true);
    WIKI_LOOKUP_ON_HBAR.put(
        "custom", getPropBoolean(props, "wiki_lookup_on_hbar", WIKI_LOOKUP_ON_HBAR.get("default")));

    REMOVE_REPORT_ABUSE_BUTTON_HBAR.put("vanilla", false);
    REMOVE_REPORT_ABUSE_BUTTON_HBAR.put("vanilla_resizable", false);
    REMOVE_REPORT_ABUSE_BUTTON_HBAR.put("lite", false);
    REMOVE_REPORT_ABUSE_BUTTON_HBAR.put("default", false);
    REMOVE_REPORT_ABUSE_BUTTON_HBAR.put("heavy", false);
    REMOVE_REPORT_ABUSE_BUTTON_HBAR.put("all", true);
    REMOVE_REPORT_ABUSE_BUTTON_HBAR.put(
        "custom",
        getPropBoolean(
            props,
            "remove_report_abuse_button_hbar",
            REMOVE_REPORT_ABUSE_BUTTON_HBAR.get("default")));

    SHOW_ITEM_GROUND_OVERLAY.put("vanilla", false);
    SHOW_ITEM_GROUND_OVERLAY.put("vanilla_resizable", false);
    SHOW_ITEM_GROUND_OVERLAY.put("lite", false);
    SHOW_ITEM_GROUND_OVERLAY.put("default", true);
    SHOW_ITEM_GROUND_OVERLAY.put("heavy", true);
    SHOW_ITEM_GROUND_OVERLAY.put("all", true);
    SHOW_ITEM_GROUND_OVERLAY.put(
        "custom", getPropBoolean(props, "show_iteminfo", SHOW_ITEM_GROUND_OVERLAY.get("default")));

    SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.put("vanilla", false);
    SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.put("vanilla_resizable", false);
    SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.put("lite", false);
    SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.put("default", false);
    SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.put("heavy", false);
    SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.put("all", false);
    SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.put(
        "custom",
        getPropBoolean(
            props,
            "show_iteminfo_highlighted",
            SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.get("default")));

    SHOW_PLAYER_NAME_OVERLAY.put("vanilla", false);
    SHOW_PLAYER_NAME_OVERLAY.put("vanilla_resizable", false);
    SHOW_PLAYER_NAME_OVERLAY.put("lite", false);
    SHOW_PLAYER_NAME_OVERLAY.put("default", false);
    SHOW_PLAYER_NAME_OVERLAY.put("heavy", false);
    SHOW_PLAYER_NAME_OVERLAY.put("all", true);
    SHOW_PLAYER_NAME_OVERLAY.put(
        "custom",
        getPropBoolean(props, "show_playerinfo", SHOW_PLAYER_NAME_OVERLAY.get("default")));

    SHOW_PVP_NAME_OVERLAY.put("vanilla", false);
    SHOW_PVP_NAME_OVERLAY.put("vanilla_resizable", false);
    SHOW_PVP_NAME_OVERLAY.put("lite", false);
    SHOW_PVP_NAME_OVERLAY.put("default", false);
    SHOW_PVP_NAME_OVERLAY.put("heavy", false);
    SHOW_PVP_NAME_OVERLAY.put("all", true);
    SHOW_PVP_NAME_OVERLAY.put(
        "custom", getPropBoolean(props, "show_pvpinfo", SHOW_PVP_NAME_OVERLAY.get("default")));

    PVP_NAMES_COLOUR.put("vanilla", 0x990000); // red berry
    PVP_NAMES_COLOUR.put("vanilla_resizable", 0x990000);
    PVP_NAMES_COLOUR.put("lite", 0x990000);
    PVP_NAMES_COLOUR.put("default", 0x990000);
    PVP_NAMES_COLOUR.put("heavy", 0x990000);
    PVP_NAMES_COLOUR.put("all", 0x990000);
    PVP_NAMES_COLOUR.put(
        "custom", getPropInt(props, "pvp_names_colour", PVP_NAMES_COLOUR.get("default")));

    SHOW_OWN_NAME_OVERLAY.put("vanilla", false);
    SHOW_OWN_NAME_OVERLAY.put("vanilla_resizable", false);
    SHOW_OWN_NAME_OVERLAY.put("lite", false);
    SHOW_OWN_NAME_OVERLAY.put("default", false);
    SHOW_OWN_NAME_OVERLAY.put("heavy", false);
    SHOW_OWN_NAME_OVERLAY.put("all", true);
    SHOW_OWN_NAME_OVERLAY.put(
        "custom", getPropBoolean(props, "show_owninfo", SHOW_OWN_NAME_OVERLAY.get("default")));

    SHOW_FRIEND_NAME_OVERLAY.put("vanilla", false);
    SHOW_FRIEND_NAME_OVERLAY.put("vanilla_resizable", false);
    SHOW_FRIEND_NAME_OVERLAY.put("lite", false);
    SHOW_FRIEND_NAME_OVERLAY.put("default", false);
    SHOW_FRIEND_NAME_OVERLAY.put("heavy", true);
    SHOW_FRIEND_NAME_OVERLAY.put("all", true);
    SHOW_FRIEND_NAME_OVERLAY.put(
        "custom",
        getPropBoolean(props, "show_friendinfo", SHOW_FRIEND_NAME_OVERLAY.get("default")));

    SHOW_NPC_NAME_OVERLAY.put("vanilla", false);
    SHOW_NPC_NAME_OVERLAY.put("vanilla_resizable", false);
    SHOW_NPC_NAME_OVERLAY.put("lite", false);
    SHOW_NPC_NAME_OVERLAY.put("default", false);
    SHOW_NPC_NAME_OVERLAY.put("heavy", false);
    SHOW_NPC_NAME_OVERLAY.put("all", true);
    SHOW_NPC_NAME_OVERLAY.put(
        "custom", getPropBoolean(props, "show_npcinfo", SHOW_NPC_NAME_OVERLAY.get("default")));

    EXTEND_IDS_OVERLAY.put("vanilla", false);
    EXTEND_IDS_OVERLAY.put("vanilla_resizable", false);
    EXTEND_IDS_OVERLAY.put("lite", false);
    EXTEND_IDS_OVERLAY.put("default", false);
    EXTEND_IDS_OVERLAY.put("heavy", false);
    EXTEND_IDS_OVERLAY.put("all", true);
    EXTEND_IDS_OVERLAY.put(
        "custom", getPropBoolean(props, "extend_idsinfo", EXTEND_IDS_OVERLAY.get("default")));

    TRACE_OBJECT_INFO.put("vanilla", false);
    TRACE_OBJECT_INFO.put("vanilla_resizable", false);
    TRACE_OBJECT_INFO.put("lite", false);
    TRACE_OBJECT_INFO.put("default", false);
    TRACE_OBJECT_INFO.put("heavy", false);
    TRACE_OBJECT_INFO.put("all", true);
    TRACE_OBJECT_INFO.put(
        "custom", getPropBoolean(props, "trace_objectinfo", TRACE_OBJECT_INFO.get("default")));

    SHOW_COMBAT_INFO.put("vanilla", false);
    SHOW_COMBAT_INFO.put("vanilla_resizable", false);
    SHOW_COMBAT_INFO.put("lite", false);
    SHOW_COMBAT_INFO.put("default", false);
    SHOW_COMBAT_INFO.put("heavy", true);
    SHOW_COMBAT_INFO.put("all", true);
    SHOW_COMBAT_INFO.put(
        "custom", getPropBoolean(props, "show_combat_info", SHOW_COMBAT_INFO.get("default")));

    LAG_INDICATOR.put("vanilla", false);
    LAG_INDICATOR.put("vanilla_resizable", false);
    LAG_INDICATOR.put("lite", false);
    LAG_INDICATOR.put("default", false);
    LAG_INDICATOR.put("heavy", true);
    LAG_INDICATOR.put("all", true);
    LAG_INDICATOR.put("custom", getPropBoolean(props, "indicators", LAG_INDICATOR.get("default")));

    SHOW_PLAYER_POSITION.put("vanilla", false);
    SHOW_PLAYER_POSITION.put("vanilla_resizable", false);
    SHOW_PLAYER_POSITION.put("lite", false);
    SHOW_PLAYER_POSITION.put("default", false);
    SHOW_PLAYER_POSITION.put("heavy", true);
    SHOW_PLAYER_POSITION.put("all", true);
    SHOW_PLAYER_POSITION.put(
        "custom",
        getPropBoolean(props, "show_player_position", SHOW_PLAYER_POSITION.get("default")));

    SHOW_RETRO_FPS.put("vanilla", false);
    SHOW_RETRO_FPS.put("vanilla_resizable", false);
    SHOW_RETRO_FPS.put("lite", false);
    SHOW_RETRO_FPS.put("default", false);
    SHOW_RETRO_FPS.put("heavy", true);
    SHOW_RETRO_FPS.put("all", true);
    SHOW_RETRO_FPS.put(
        "custom", getPropBoolean(props, "show_retro_fps", SHOW_RETRO_FPS.get("default")));

    SHOW_XP_BAR.put("vanilla", false);
    SHOW_XP_BAR.put("vanilla_resizable", false);
    SHOW_XP_BAR.put("lite", false);
    SHOW_XP_BAR.put("default", true);
    SHOW_XP_BAR.put("heavy", true);
    SHOW_XP_BAR.put("all", true);
    SHOW_XP_BAR.put("custom", getPropBoolean(props, "show_xp_bar", SHOW_XP_BAR.get("default")));

    NPC_HEALTH_SHOW_PERCENTAGE.put("vanilla", false);
    NPC_HEALTH_SHOW_PERCENTAGE.put("vanilla_resizable", false);
    NPC_HEALTH_SHOW_PERCENTAGE.put("lite", false);
    NPC_HEALTH_SHOW_PERCENTAGE.put("default", false);
    NPC_HEALTH_SHOW_PERCENTAGE.put("heavy", false);
    NPC_HEALTH_SHOW_PERCENTAGE.put("all", true);
    NPC_HEALTH_SHOW_PERCENTAGE.put(
        "custom",
        getPropBoolean(props, "use_percentage", NPC_HEALTH_SHOW_PERCENTAGE.get("default")));

    SHOW_HITBOX.put("vanilla", false);
    SHOW_HITBOX.put("vanilla_resizable", false);
    SHOW_HITBOX.put("lite", false);
    SHOW_HITBOX.put("default", false);
    SHOW_HITBOX.put("heavy", false);
    SHOW_HITBOX.put("all", true);
    SHOW_HITBOX.put("custom", getPropBoolean(props, "show_hitbox", SHOW_HITBOX.get("default")));

    SHOW_FOOD_HEAL_OVERLAY.put("vanilla", false);
    SHOW_FOOD_HEAL_OVERLAY.put("vanilla_resizable", false);
    SHOW_FOOD_HEAL_OVERLAY.put("lite", false);
    SHOW_FOOD_HEAL_OVERLAY.put("default", false);
    SHOW_FOOD_HEAL_OVERLAY.put("heavy", true);
    SHOW_FOOD_HEAL_OVERLAY.put("all", true);
    SHOW_FOOD_HEAL_OVERLAY.put(
        "custom",
        getPropBoolean(props, "show_food_heal_overlay", SHOW_FOOD_HEAL_OVERLAY.get("default")));

    SHOW_TIME_UNTIL_HP_REGEN.put("vanilla", false);
    SHOW_TIME_UNTIL_HP_REGEN.put("vanilla_resizable", false);
    SHOW_TIME_UNTIL_HP_REGEN.put("lite", false);
    SHOW_TIME_UNTIL_HP_REGEN.put("default", false);
    SHOW_TIME_UNTIL_HP_REGEN.put("heavy", true);
    SHOW_TIME_UNTIL_HP_REGEN.put("all", true);
    SHOW_TIME_UNTIL_HP_REGEN.put(
        "custom",
        getPropBoolean(props, "show_time_until_hp_regen", SHOW_TIME_UNTIL_HP_REGEN.get("default")));

    DEBUG.put("vanilla", false);
    DEBUG.put("vanilla_resizable", false);
    DEBUG.put("lite", false);
    DEBUG.put("default", false);
    DEBUG.put("heavy", false);
    DEBUG.put("all", false);
    DEBUG.put("custom", getPropBoolean(props, "debug", DEBUG.get("default")));

    EXCEPTION_HANDLER.put("vanilla", false);
    EXCEPTION_HANDLER.put("vanilla_resizable", false);
    EXCEPTION_HANDLER.put("lite", false);
    EXCEPTION_HANDLER.put("default", false);
    EXCEPTION_HANDLER.put("heavy", false);
    EXCEPTION_HANDLER.put("all", false);
    EXCEPTION_HANDLER.put(
        "custom", getPropBoolean(props, "exception_handler", EXCEPTION_HANDLER.get("default")));

    defineStaticPreset(
        HIGHLIGHTED_ITEMS, getPropArrayListString(props, "highlighted_items", new ArrayList<>()));

    defineStaticPreset(
        SPECIAL_HIGHLIGHTED_ITEMS,
        getPropArrayListString(props, "special_highlighted_items", new ArrayList<>()));

    defineStaticPreset(
        BLOCKED_ITEMS, getPropArrayListString(props, "blocked_items", new ArrayList<>()));

    ITEM_HIGHLIGHT_COLOUR.put("vanilla", 0xFFD700);
    ITEM_HIGHLIGHT_COLOUR.put("vanilla_resizable", 0xFFD700);
    ITEM_HIGHLIGHT_COLOUR.put("lite", 0xFFD700);
    ITEM_HIGHLIGHT_COLOUR.put("default", 0xFFD700);
    ITEM_HIGHLIGHT_COLOUR.put("heavy", 0xFFD700);
    ITEM_HIGHLIGHT_COLOUR.put("all", 0xFFD700);
    ITEM_HIGHLIGHT_COLOUR.put(
        "custom", getPropInt(props, "item_highlight_colour", ITEM_HIGHLIGHT_COLOUR.get("default")));

    ITEM_SPECIAL_HIGHLIGHT_COLOUR.put("vanilla", 0xAC49F0);
    ITEM_SPECIAL_HIGHLIGHT_COLOUR.put("vanilla_resizable", 0xAC49F0);
    ITEM_SPECIAL_HIGHLIGHT_COLOUR.put("lite", 0xAC49F0);
    ITEM_SPECIAL_HIGHLIGHT_COLOUR.put("default", 0xAC49F0);
    ITEM_SPECIAL_HIGHLIGHT_COLOUR.put("heavy", 0xAC49F0);
    ITEM_SPECIAL_HIGHLIGHT_COLOUR.put("all", 0xAC49F0);
    ITEM_SPECIAL_HIGHLIGHT_COLOUR.put(
        "custom",
        getPropInt(
            props, "item_special_highlight_colour", ITEM_SPECIAL_HIGHLIGHT_COLOUR.get("default")));

    HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.put("vanilla", false);
    HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.put("vanilla_resizable", false);
    HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.put("lite", false);
    HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.put("default", true);
    HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.put("heavy", true);
    HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.put("all", true);
    HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.put(
        "custom",
        getPropBoolean(
            props,
            "highlight_items_right_click_menu",
            HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.get("default")));

    OVERLAY_FONT_STYLE.put("vanilla", Renderer.OverlayFontStyle.JAGEX_BORDERED.getValue());
    OVERLAY_FONT_STYLE.put(
        "vanilla_resizable", Renderer.OverlayFontStyle.JAGEX_BORDERED.getValue());
    OVERLAY_FONT_STYLE.put("lite", Renderer.OverlayFontStyle.JAGEX_BORDERED.getValue());
    OVERLAY_FONT_STYLE.put("default", Renderer.OverlayFontStyle.JAGEX_BORDERED.getValue());
    OVERLAY_FONT_STYLE.put("heavy", Renderer.OverlayFontStyle.JAGEX_BORDERED.getValue());
    OVERLAY_FONT_STYLE.put("all", Renderer.OverlayFontStyle.JAGEX_BORDERED.getValue());
    OVERLAY_FONT_STYLE.put(
        "custom", getPropInt(props, "overlay_font_style", OVERLAY_FONT_STYLE.get("default")));

    //// bank
    START_REMEMBERED_FILTER_SORT.put("vanilla", false);
    START_REMEMBERED_FILTER_SORT.put("vanilla_resizable", false);
    START_REMEMBERED_FILTER_SORT.put("lite", false);
    START_REMEMBERED_FILTER_SORT.put("default", true);
    START_REMEMBERED_FILTER_SORT.put("heavy", true);
    START_REMEMBERED_FILTER_SORT.put("all", true);
    START_REMEMBERED_FILTER_SORT.put(
        "custom",
        getPropBoolean(props, "start_searched_bank", START_REMEMBERED_FILTER_SORT.get("default")));

    defineStaticPreset(SEARCH_BANK_WORD, getPropString(props, "search_bank_word", ""));

    SHOW_BANK_VALUE.put("vanilla", false);
    SHOW_BANK_VALUE.put("vanilla_resizable", false);
    SHOW_BANK_VALUE.put("lite", false);
    SHOW_BANK_VALUE.put("default", true);
    SHOW_BANK_VALUE.put("heavy", true);
    SHOW_BANK_VALUE.put("all", true);
    SHOW_BANK_VALUE.put(
        "custom", getPropBoolean(props, "show_bank_value", SHOW_BANK_VALUE.get("default")));

    SORT_FILTER_BANK.put("vanilla", false);
    SORT_FILTER_BANK.put("vanilla_resizable", false);
    SORT_FILTER_BANK.put("lite", false);
    SORT_FILTER_BANK.put("default", true);
    SORT_FILTER_BANK.put("heavy", true);
    SORT_FILTER_BANK.put("all", true);
    SORT_FILTER_BANK.put(
        "custom", getPropBoolean(props, "sort_filter_bank", SORT_FILTER_BANK.get("default")));

    SORT_BANK_REMEMBER.put("vanilla", "000000000000");
    SORT_BANK_REMEMBER.put("vanilla_resizable", "000000000000");
    SORT_BANK_REMEMBER.put("lite", "000000000000");
    SORT_BANK_REMEMBER.put("default", "000000000000");
    SORT_BANK_REMEMBER.put("heavy", "000000000000");
    SORT_BANK_REMEMBER.put("all", "000000000000");
    SORT_BANK_REMEMBER.put(
        "custom", getPropString(props, "sort_bank", SORT_BANK_REMEMBER.get("default")));

    if (SORT_FILTER_BANK.get(currentProfile)) {
      Bank.loadButtonMode(SORT_BANK_REMEMBER.get(currentProfile));
    }

    //// notifications
    TRAY_NOTIFS.put("vanilla", false);
    TRAY_NOTIFS.put("vanilla_resizable", false);
    TRAY_NOTIFS.put("lite", true);
    TRAY_NOTIFS.put("default", true);
    TRAY_NOTIFS.put("heavy", true);
    TRAY_NOTIFS.put("all", true);
    TRAY_NOTIFS.put("custom", getPropBoolean(props, "tray_notifs", TRAY_NOTIFS.get("default")));

    TRAY_NOTIFS_ALWAYS.put("vanilla", false);
    TRAY_NOTIFS_ALWAYS.put("vanilla_resizable", false);
    TRAY_NOTIFS_ALWAYS.put("lite", false);
    TRAY_NOTIFS_ALWAYS.put("default", false);
    TRAY_NOTIFS_ALWAYS.put("heavy", false);
    TRAY_NOTIFS_ALWAYS.put("all", true);
    TRAY_NOTIFS_ALWAYS.put(
        "custom", getPropBoolean(props, "tray_notifs_always", TRAY_NOTIFS_ALWAYS.get("default")));

    NOTIFICATION_SOUNDS.put("vanilla", false);
    NOTIFICATION_SOUNDS.put("vanilla_resizable", false);
    NOTIFICATION_SOUNDS.put("lite", !Settings.isRecommendedToUseSystemNotifs());
    NOTIFICATION_SOUNDS.put("default", !Settings.isRecommendedToUseSystemNotifs());
    NOTIFICATION_SOUNDS.put("heavy", !Settings.isRecommendedToUseSystemNotifs());
    NOTIFICATION_SOUNDS.put("all", true);
    NOTIFICATION_SOUNDS.put(
        "custom", getPropBoolean(props, "notification_sounds", NOTIFICATION_SOUNDS.get("default")));

    SOUND_NOTIFS_ALWAYS.put("vanilla", false);
    SOUND_NOTIFS_ALWAYS.put("vanilla_resizable", false);
    SOUND_NOTIFS_ALWAYS.put("lite", false);
    SOUND_NOTIFS_ALWAYS.put("default", false);
    SOUND_NOTIFS_ALWAYS.put("heavy", false);
    SOUND_NOTIFS_ALWAYS.put("all", true);
    SOUND_NOTIFS_ALWAYS.put(
        "custom", getPropBoolean(props, "sound_notifs_always", SOUND_NOTIFS_ALWAYS.get("default")));

    USE_SYSTEM_NOTIFICATIONS.put("vanilla", false);
    USE_SYSTEM_NOTIFICATIONS.put("vanilla_resizable", false);
    USE_SYSTEM_NOTIFICATIONS.put("lite", Settings.isRecommendedToUseSystemNotifs());
    USE_SYSTEM_NOTIFICATIONS.put("default", Settings.isRecommendedToUseSystemNotifs());
    USE_SYSTEM_NOTIFICATIONS.put("heavy", Settings.isRecommendedToUseSystemNotifs());
    USE_SYSTEM_NOTIFICATIONS.put("all", true);
    USE_SYSTEM_NOTIFICATIONS.put(
        "custom",
        getPropBoolean(props, "use_system_notifications", USE_SYSTEM_NOTIFICATIONS.get("default")));

    PM_NOTIFICATIONS.put("vanilla", false);
    PM_NOTIFICATIONS.put("vanilla_resizable", false);
    PM_NOTIFICATIONS.put("lite", false);
    PM_NOTIFICATIONS.put("default", true);
    PM_NOTIFICATIONS.put("heavy", true);
    PM_NOTIFICATIONS.put("all", true);
    PM_NOTIFICATIONS.put(
        "custom", getPropBoolean(props, "pm_notifications", PM_NOTIFICATIONS.get("default")));

    defineStaticPreset(
        PM_DENYLIST, getPropArrayListString(props, "pm_denylist", new ArrayList<>()));

    TRADE_NOTIFICATIONS.put("vanilla", false);
    TRADE_NOTIFICATIONS.put("vanilla_resizable", false);
    TRADE_NOTIFICATIONS.put("lite", false);
    TRADE_NOTIFICATIONS.put("default", true);
    TRADE_NOTIFICATIONS.put("heavy", true);
    TRADE_NOTIFICATIONS.put("all", true);
    TRADE_NOTIFICATIONS.put(
        "custom", getPropBoolean(props, "trade_notifications", TRADE_NOTIFICATIONS.get("default")));

    DUEL_NOTIFICATIONS.put("vanilla", false);
    DUEL_NOTIFICATIONS.put("vanilla_resizable", false);
    DUEL_NOTIFICATIONS.put("lite", false);
    DUEL_NOTIFICATIONS.put("default", true);
    DUEL_NOTIFICATIONS.put("heavy", true);
    DUEL_NOTIFICATIONS.put("all", true);
    DUEL_NOTIFICATIONS.put(
        "custom", getPropBoolean(props, "duel_notifications", DUEL_NOTIFICATIONS.get("default")));

    LOGOUT_NOTIFICATIONS.put("vanilla", false);
    LOGOUT_NOTIFICATIONS.put("vanilla_resizable", false);
    LOGOUT_NOTIFICATIONS.put("lite", false);
    LOGOUT_NOTIFICATIONS.put("default", true);
    LOGOUT_NOTIFICATIONS.put("heavy", true);
    LOGOUT_NOTIFICATIONS.put("all", true);
    LOGOUT_NOTIFICATIONS.put(
        "custom",
        getPropBoolean(props, "logout_notifications", LOGOUT_NOTIFICATIONS.get("default")));

    LOW_HP_NOTIFICATIONS.put("vanilla", false);
    LOW_HP_NOTIFICATIONS.put("vanilla_resizable", false);
    LOW_HP_NOTIFICATIONS.put("lite", false);
    LOW_HP_NOTIFICATIONS.put("default", true);
    LOW_HP_NOTIFICATIONS.put("heavy", true);
    LOW_HP_NOTIFICATIONS.put("all", true);
    LOW_HP_NOTIFICATIONS.put(
        "custom",
        getPropBoolean(props, "low_hp_notifications", LOW_HP_NOTIFICATIONS.get("default")));

    LOW_HP_NOTIF_VALUE.put("vanilla", 0);
    LOW_HP_NOTIF_VALUE.put("vanilla_resizable", 0);
    LOW_HP_NOTIF_VALUE.put("lite", 25);
    LOW_HP_NOTIF_VALUE.put("default", 25);
    LOW_HP_NOTIF_VALUE.put("heavy", 25);
    LOW_HP_NOTIF_VALUE.put("all", 30);
    LOW_HP_NOTIF_VALUE.put(
        "custom", getPropInt(props, "low_hp_notif_value", LOW_HP_NOTIF_VALUE.get("default")));

    FATIGUE_NOTIFICATIONS.put("vanilla", false);
    FATIGUE_NOTIFICATIONS.put("vanilla_resizable", false);
    FATIGUE_NOTIFICATIONS.put("lite", false);
    FATIGUE_NOTIFICATIONS.put("default", true);
    FATIGUE_NOTIFICATIONS.put("heavy", true);
    FATIGUE_NOTIFICATIONS.put("all", true);
    FATIGUE_NOTIFICATIONS.put(
        "custom",
        getPropBoolean(props, "fatigue_notifications", FATIGUE_NOTIFICATIONS.get("default")));

    FATIGUE_NOTIF_VALUE.put("vanilla", 101);
    FATIGUE_NOTIF_VALUE.put("vanilla_resizable", 101);
    FATIGUE_NOTIF_VALUE.put("lite", 98);
    FATIGUE_NOTIF_VALUE.put("default", 98);
    FATIGUE_NOTIF_VALUE.put("heavy", 98);
    FATIGUE_NOTIF_VALUE.put("all", 80);
    FATIGUE_NOTIF_VALUE.put(
        "custom", getPropInt(props, "fatigue_notif_value", FATIGUE_NOTIF_VALUE.get("default")));

    HIGHLIGHTED_ITEM_NOTIFICATIONS.put("vanilla", false);
    HIGHLIGHTED_ITEM_NOTIFICATIONS.put("vanilla_resizable", false);
    HIGHLIGHTED_ITEM_NOTIFICATIONS.put("lite", false);
    HIGHLIGHTED_ITEM_NOTIFICATIONS.put("default", true);
    HIGHLIGHTED_ITEM_NOTIFICATIONS.put("heavy", true);
    HIGHLIGHTED_ITEM_NOTIFICATIONS.put("all", true);
    HIGHLIGHTED_ITEM_NOTIFICATIONS.put(
        "custom",
        getPropBoolean(
            props,
            "highlighted_item_notifications",
            HIGHLIGHTED_ITEM_NOTIFICATIONS.get("default")));

    HIGHLIGHTED_ITEM_NOTIF_VALUE.put("vanilla", 11000);
    HIGHLIGHTED_ITEM_NOTIF_VALUE.put("vanilla_resizable", 11000);
    HIGHLIGHTED_ITEM_NOTIF_VALUE.put("lite", 100);
    HIGHLIGHTED_ITEM_NOTIF_VALUE.put("default", 100);
    HIGHLIGHTED_ITEM_NOTIF_VALUE.put("heavy", 100);
    HIGHLIGHTED_ITEM_NOTIF_VALUE.put("all", 0);
    HIGHLIGHTED_ITEM_NOTIF_VALUE.put(
        "custom",
        getPropInt(
            props, "highlighted_item_notif_value", HIGHLIGHTED_ITEM_NOTIF_VALUE.get("default")));

    defineStaticPreset(
        IMPORTANT_MESSAGES, getPropArrayListString(props, "important_messages", new ArrayList<>()));

    defineStaticPreset(
        IMPORTANT_SAD_MESSAGES,
        getPropArrayListString(props, "important_sad_messages", new ArrayList<>()));

    MUTE_IMPORTANT_MESSAGE_SOUNDS.put("vanilla", false);
    MUTE_IMPORTANT_MESSAGE_SOUNDS.put("vanilla_resizable", false);
    MUTE_IMPORTANT_MESSAGE_SOUNDS.put("lite", false);
    MUTE_IMPORTANT_MESSAGE_SOUNDS.put("default", false);
    MUTE_IMPORTANT_MESSAGE_SOUNDS.put("heavy", false);
    MUTE_IMPORTANT_MESSAGE_SOUNDS.put("all", true);
    MUTE_IMPORTANT_MESSAGE_SOUNDS.put(
        "custom",
        getPropBoolean(
            props, "mute_important_message_sounds", MUTE_IMPORTANT_MESSAGE_SOUNDS.get("default")));

    //// streaming
    TWITCH_CHAT_ENABLED.put("vanilla", false);
    TWITCH_CHAT_ENABLED.put("vanilla_resizable", false);
    TWITCH_CHAT_ENABLED.put("lite", true);
    TWITCH_CHAT_ENABLED.put("default", true);
    TWITCH_CHAT_ENABLED.put("heavy", true);
    TWITCH_CHAT_ENABLED.put("all", true);
    TWITCH_CHAT_ENABLED.put(
        "custom", getPropBoolean(props, "twitch_enabled", TWITCH_CHAT_ENABLED.get("default")));

    TWITCH_HIDE_CHAT.put("vanilla", true);
    TWITCH_HIDE_CHAT.put("vanilla_resizable", true);
    TWITCH_HIDE_CHAT.put("lite", false);
    TWITCH_HIDE_CHAT.put("default", false);
    TWITCH_HIDE_CHAT.put("heavy", false);
    TWITCH_HIDE_CHAT.put("all", false);
    TWITCH_HIDE_CHAT.put(
        "custom", getPropBoolean(props, "twitch_hide", TWITCH_HIDE_CHAT.get("default")));

    defineStaticPreset(TWITCH_CHANNEL, getPropString(props, "twitch_channel", ""));

    defineStaticPreset(TWITCH_OAUTH, getPropString(props, "twitch_oauth", ""));

    defineStaticPreset(TWITCH_USERNAME, getPropString(props, "twitch_username", ""));

    SHOW_LOGIN_IP_ADDRESS.put("vanilla", true);
    SHOW_LOGIN_IP_ADDRESS.put("vanilla_resizable", true);
    SHOW_LOGIN_IP_ADDRESS.put("lite", true);
    SHOW_LOGIN_IP_ADDRESS.put("default", true);
    SHOW_LOGIN_IP_ADDRESS.put("heavy", true);
    SHOW_LOGIN_IP_ADDRESS.put("all", false);
    SHOW_LOGIN_IP_ADDRESS.put(
        "custom", getPropBoolean(props, "show_logindetails", SHOW_LOGIN_IP_ADDRESS.get("default")));

    SAVE_LOGININFO.put("vanilla", false);
    SAVE_LOGININFO.put("vanilla_resizable", false);
    SAVE_LOGININFO.put("lite", true);
    SAVE_LOGININFO.put("default", true);
    SAVE_LOGININFO.put("heavy", true);
    SAVE_LOGININFO.put("all", true);
    SAVE_LOGININFO.put(
        "custom", getPropBoolean(props, "save_logininfo", SAVE_LOGININFO.get("default")));

    START_LOGINSCREEN.put("vanilla", false);
    START_LOGINSCREEN.put("vanilla_resizable", false);
    START_LOGINSCREEN.put("lite", true);
    START_LOGINSCREEN.put("default", true);
    START_LOGINSCREEN.put("heavy", true);
    START_LOGINSCREEN.put("all", true);
    START_LOGINSCREEN.put(
        "custom", getPropBoolean(props, "start_loginscreen", START_LOGINSCREEN.get("default")));

    SPEEDRUNNER_MODE_ACTIVE.put("vanilla", false);
    SPEEDRUNNER_MODE_ACTIVE.put("vanilla_resizable", false);
    SPEEDRUNNER_MODE_ACTIVE.put("lite", false);
    SPEEDRUNNER_MODE_ACTIVE.put("default", false);
    SPEEDRUNNER_MODE_ACTIVE.put("heavy", false);
    SPEEDRUNNER_MODE_ACTIVE.put("all", true);
    SPEEDRUNNER_MODE_ACTIVE.put(
        "custom", getPropBoolean(props, "speedrun_active", SPEEDRUNNER_MODE_ACTIVE.get("default")));

    /*
    SPEEDRUNNER_USERNAME.put("vanilla", "");
    SPEEDRUNNER_USERNAME.put("vanilla_resizable", "");
    SPEEDRUNNER_USERNAME.put("lite", "");
    SPEEDRUNNER_USERNAME.put("default", "");
    SPEEDRUNNER_USERNAME.put("heavy", "");
    SPEEDRUNNER_USERNAME.put("all", "");
    SPEEDRUNNER_USERNAME.put(
        "custom", getPropString(props, "speedrun_username", SPEEDRUNNER_USERNAME.get("default")));
        */

    //// replay
    defineStaticPreset(
        REPLAY_STORAGE_PATH,
        getPropString(props, "replay_storage_path", sanitizeDirTextValue(Dir.REPLAY)));

    RECORD_KB_MOUSE.put("vanilla", false);
    RECORD_KB_MOUSE.put("vanilla_resizable", false);
    RECORD_KB_MOUSE.put("lite", false);
    RECORD_KB_MOUSE.put("default", false);
    RECORD_KB_MOUSE.put("heavy", true);
    RECORD_KB_MOUSE.put("all", true);
    RECORD_KB_MOUSE.put(
        "custom", getPropBoolean(props, "record_kb_mouse", RECORD_KB_MOUSE.get("default")));

    PARSE_OPCODES.put("vanilla", true);
    PARSE_OPCODES.put("vanilla_resizable", true);
    PARSE_OPCODES.put("lite", true);
    PARSE_OPCODES.put("default", true);
    PARSE_OPCODES.put("heavy", true);
    PARSE_OPCODES.put("all", true);
    PARSE_OPCODES.put(
        "custom", getPropBoolean(props, "parse_opcodes", PARSE_OPCODES.get("default")));

    FAST_DISCONNECT.put("vanilla", false);
    FAST_DISCONNECT.put("vanilla_resizable", false);
    FAST_DISCONNECT.put("lite", false);
    FAST_DISCONNECT.put("default", false);
    FAST_DISCONNECT.put("heavy", true);
    FAST_DISCONNECT.put("all", true);
    FAST_DISCONNECT.put(
        "custom", getPropBoolean(props, "fast_disconnect", FAST_DISCONNECT.get("default")));

    RECORD_AUTOMATICALLY.put("vanilla", false);
    RECORD_AUTOMATICALLY.put("vanilla_resizable", false);
    RECORD_AUTOMATICALLY.put("lite", false);
    RECORD_AUTOMATICALLY.put("default", false);
    RECORD_AUTOMATICALLY.put("heavy", true);
    RECORD_AUTOMATICALLY.put("all", true);
    RECORD_AUTOMATICALLY.put(
        "custom",
        getPropBoolean(props, "record_automatically", RECORD_AUTOMATICALLY.get("default")));

    HIDE_PRIVATE_MSGS_REPLAY.put("vanilla", false);
    HIDE_PRIVATE_MSGS_REPLAY.put("vanilla_resizable", false);
    HIDE_PRIVATE_MSGS_REPLAY.put("lite", false);
    HIDE_PRIVATE_MSGS_REPLAY.put("default", false);
    HIDE_PRIVATE_MSGS_REPLAY.put("heavy", false);
    HIDE_PRIVATE_MSGS_REPLAY.put("all", true);
    HIDE_PRIVATE_MSGS_REPLAY.put(
        "custom",
        getPropBoolean(props, "hide_private_msgs_replay", HIDE_PRIVATE_MSGS_REPLAY.get("default")));

    SHOW_SEEK_BAR.put("vanilla", true);
    SHOW_SEEK_BAR.put("vanilla_resizable", true);
    SHOW_SEEK_BAR.put("lite", true);
    SHOW_SEEK_BAR.put("default", true);
    SHOW_SEEK_BAR.put("heavy", true);
    SHOW_SEEK_BAR.put("all", true);
    SHOW_SEEK_BAR.put(
        "custom", getPropBoolean(props, "show_seek_bar", SHOW_SEEK_BAR.get("default")));

    SHOW_PLAYER_CONTROLS.put("vanilla", true);
    SHOW_PLAYER_CONTROLS.put("vanilla_resizable", true);
    SHOW_PLAYER_CONTROLS.put("lite", true);
    SHOW_PLAYER_CONTROLS.put("default", true);
    SHOW_PLAYER_CONTROLS.put("heavy", true);
    SHOW_PLAYER_CONTROLS.put("all", true);
    SHOW_PLAYER_CONTROLS.put(
        "custom",
        getPropBoolean(props, "show_player_controls", SHOW_PLAYER_CONTROLS.get("default")));

    TRIGGER_ALERTS_REPLAY.put("vanilla", false);
    TRIGGER_ALERTS_REPLAY.put("vanilla_resizable", false);
    TRIGGER_ALERTS_REPLAY.put("lite", false);
    TRIGGER_ALERTS_REPLAY.put("default", false);
    TRIGGER_ALERTS_REPLAY.put("heavy", true);
    TRIGGER_ALERTS_REPLAY.put("all", true);
    TRIGGER_ALERTS_REPLAY.put(
        "custom",
        getPropBoolean(props, "trigger_alerts_replay", TRIGGER_ALERTS_REPLAY.get("default")));

    defineStaticPreset(REPLAY_BASE_PATH, getPropString(props, "replay_base_path", ""));

    PREFERRED_DATE_FORMAT.put(
        "vanilla",
        getPropString(
            props,
            "preferred_date_format",
            "dd MMMMMMMMM yyyy - HH:mm:ss")); // jagex is british so this is vanilla
    PREFERRED_DATE_FORMAT.put(
        "vanilla_resizable",
        getPropString(props, "preferred_date_format", "dd MMMMMMMMM yyyy - HH:mm:ss"));
    PREFERRED_DATE_FORMAT.put(
        "lite", getPropString(props, "preferred_date_format", "dd MMMMMMMMM yyyy - HH:mm:ss"));
    PREFERRED_DATE_FORMAT.put(
        "default",
        getPropString(
            props,
            "preferred_date_format",
            "yyyy-MM-dd HH:mm:ss")); // ISO 8601, same as default folder name format
    PREFERRED_DATE_FORMAT.put(
        "heavy",
        getPropString(
            props,
            "preferred_date_format",
            "MMMMMMMMM dd, yyyy, hh:mm:ss aa")); // american date format for some reason
    PREFERRED_DATE_FORMAT.put(
        "all",
        getPropString(
            props,
            "preferred_date_format",
            "EEEEEEE, MMMMMMMMM dd, yyyy GG; hh:mm:ss aa")); // american format + era + day of week
    PREFERRED_DATE_FORMAT.put(
        "custom",
        getPropString(
            props,
            "preferred_date_format",
            PREFERRED_DATE_FORMAT.get("default"))); // ISO 8601, same as default folder name format

    SHOW_WORLD_COLUMN.put("vanilla", false);
    SHOW_WORLD_COLUMN.put("vanilla_resizable", false);
    SHOW_WORLD_COLUMN.put("lite", false);
    SHOW_WORLD_COLUMN.put("default", true);
    SHOW_WORLD_COLUMN.put("heavy", true);
    SHOW_WORLD_COLUMN.put("all", true);
    SHOW_WORLD_COLUMN.put(
        "custom", getPropBoolean(props, "show_world_column", SHOW_WORLD_COLUMN.get("default")));

    SHOW_CONVERSION_COLUMN.put("vanilla", false);
    SHOW_CONVERSION_COLUMN.put("vanilla_resizable", false);
    SHOW_CONVERSION_COLUMN.put("lite", false);
    SHOW_CONVERSION_COLUMN.put("default", false);
    SHOW_CONVERSION_COLUMN.put("heavy", true);
    SHOW_CONVERSION_COLUMN.put("all", true);
    SHOW_CONVERSION_COLUMN.put(
        "custom",
        getPropBoolean(props, "show_conversion_column", SHOW_CONVERSION_COLUMN.get("default")));

    SHOW_USERFIELD_COLUMN.put("vanilla", false);
    SHOW_USERFIELD_COLUMN.put("vanilla_resizable", false);
    SHOW_USERFIELD_COLUMN.put("lite", false);
    SHOW_USERFIELD_COLUMN.put("default", false);
    SHOW_USERFIELD_COLUMN.put("heavy", false);
    SHOW_USERFIELD_COLUMN.put("all", true);
    SHOW_USERFIELD_COLUMN.put(
        "custom",
        getPropBoolean(props, "show_userfield_column", SHOW_USERFIELD_COLUMN.get("default")));

    //// world list
    initWorlds();

    //// joystick
    JOYSTICK_ENABLED.put("vanilla", false);
    JOYSTICK_ENABLED.put("vanilla_resizable", false);
    JOYSTICK_ENABLED.put("lite", false);
    JOYSTICK_ENABLED.put("default", false);
    JOYSTICK_ENABLED.put("heavy", false);
    JOYSTICK_ENABLED.put("all", true);
    JOYSTICK_ENABLED.put(
        "custom", getPropBoolean(props, "joystick_enabled", JOYSTICK_ENABLED.get("default")));

    //// no gui
    defineStaticPreset(
        LAST_KNOWN_COMBAT_STYLE,
        getPropInt(props, "last_known_combat_style", Client.COMBAT_AGGRESSIVE));

    defineStaticPreset(WORLD, getPropInt(props, "world", 1));

    defineStaticPreset(FIRST_TIME, getPropBoolean(props, "first_time", true));

    defineStaticPreset(UPDATE_CONFIRMATION, getPropBoolean(props, "update_confirmation", true));

    defineStaticPreset(
        RECORD_AUTOMATICALLY_FIRST_TIME,
        getPropBoolean(props, "record_automatically_first_time", true));

    defineStaticPreset(DISASSEMBLE, getPropBoolean(props, "disassemble", false));

    defineStaticPreset(
        DISASSEMBLE_DIRECTORY, getPropString(props, "disassemble_directory", "dump"));

    // Ensure current profile is set to custom whenever this is invoked
    if (!currentProfile.equals("custom")) {
      currentProfile = "custom";
    }
  }

  /* Convenience methods to define the same preset for all preset levels */

  public static void defineStaticPreset(
      HashMap<String, ArrayList<String>> hashMap, ArrayList<String> item) {
    hashMap.put("vanilla", item);
    hashMap.put("vanilla_resizable", item);
    hashMap.put("lite", item);
    hashMap.put("default", item);
    hashMap.put("heavy", item);
    hashMap.put("all", item);
    hashMap.put("custom", item);
  }

  public static void defineStaticPreset(HashMap<String, String> hashMap, String item) {
    hashMap.put("vanilla", item);
    hashMap.put("vanilla_resizable", item);
    hashMap.put("lite", item);
    hashMap.put("default", item);
    hashMap.put("heavy", item);
    hashMap.put("all", item);
    hashMap.put("custom", item);
  }

  public static void defineStaticPreset(HashMap<String, Boolean> hashMap, Boolean item) {
    hashMap.put("vanilla", item);
    hashMap.put("vanilla_resizable", item);
    hashMap.put("lite", item);
    hashMap.put("default", item);
    hashMap.put("heavy", item);
    hashMap.put("all", item);
    hashMap.put("custom", item);
  }

  public static void defineStaticPreset(HashMap<String, Integer> hashMap, Integer item) {
    hashMap.put("vanilla", item);
    hashMap.put("vanilla_resizable", item);
    hashMap.put("lite", item);
    hashMap.put("default", item);
    hashMap.put("heavy", item);
    hashMap.put("all", item);
    hashMap.put("custom", item);
  }

  /* * * * * * */

  /** Detects and fixes invalid values for critically-important settings */
  public static void sanitizeSettings() {
    boolean foundInvalidSetting = false;

    /* Check for migrated props */

    Properties props = loadProps();

    // Migrating users from their old global combat style to the
    // new per-character combat style introduced in Oct 2023
    if (props.get("combat_style") != null) {
      LAST_KNOWN_COMBAT_STYLE.put("custom", Integer.parseInt((String) props.get("combat_style")));
      foundInvalidSetting = true;
    }

    /* Check for invalid setting values */

    String screenshotsStorageDir = SCREENSHOTS_STORAGE_PATH.get("custom");
    String validatedScreenshotsStorageDir =
        validateCustomDir(screenshotsStorageDir, Dir.SCREENSHOT);
    if (!screenshotsStorageDir.equals(validatedScreenshotsStorageDir)) {
      SCREENSHOTS_STORAGE_PATH.put("custom", validatedScreenshotsStorageDir);
      foundInvalidSetting = true;
    }

    String replayStorageDir = REPLAY_STORAGE_PATH.get("custom");
    String validatedReplayStorageDir = validateCustomDir(replayStorageDir, Dir.REPLAY);
    if (!replayStorageDir.equals(validatedReplayStorageDir)) {
      REPLAY_STORAGE_PATH.put("custom", validatedReplayStorageDir);
      foundInvalidSetting = true;
    }

    String replayBaseDir = Settings.REPLAY_BASE_PATH.get("custom");
    String validatedReplayBaseDir = validateCustomDir(replayBaseDir, "");
    if (!replayBaseDir.equals(validatedReplayBaseDir)) {
      REPLAY_BASE_PATH.put("custom", validatedReplayBaseDir);
      foundInvalidSetting = true;
    }

    if (CUSTOM_CLIENT_SIZE_X.get("custom") < 512) {
      CUSTOM_CLIENT_SIZE_X.put("custom", 512);
      foundInvalidSetting = true;
    }

    if (CUSTOM_CLIENT_SIZE_Y.get("custom") < 346) {
      CUSTOM_CLIENT_SIZE_Y.put("custom", 346);
      foundInvalidSetting = true;
    }

    if (INTEGER_SCALING_FACTOR.get("custom") < (int) Renderer.minScalar) {
      INTEGER_SCALING_FACTOR.put("custom", (int) Renderer.minScalar);
      foundInvalidSetting = true;
    } else if (INTEGER_SCALING_FACTOR.get("custom") > (int) Renderer.maxIntegerScalar) {
      INTEGER_SCALING_FACTOR.put("custom", (int) Renderer.maxIntegerScalar);
      foundInvalidSetting = true;
    }

    if (BILINEAR_SCALING_FACTOR.get("custom") < Renderer.minScalar) {
      BILINEAR_SCALING_FACTOR.put("custom", Renderer.minScalar);
      foundInvalidSetting = true;
    } else if (BILINEAR_SCALING_FACTOR.get("custom") > Renderer.maxInterpolationScalar) {
      BILINEAR_SCALING_FACTOR.put("custom", Renderer.maxInterpolationScalar);
      foundInvalidSetting = true;
    }

    if (BICUBIC_SCALING_FACTOR.get("custom") < Renderer.minScalar) {
      BICUBIC_SCALING_FACTOR.put("custom", Renderer.minScalar);
      foundInvalidSetting = true;
    } else if (BICUBIC_SCALING_FACTOR.get("custom") > Renderer.maxInterpolationScalar) {
      BICUBIC_SCALING_FACTOR.put("custom", Renderer.maxInterpolationScalar);
      foundInvalidSetting = true;
    }

    if (WORLD.get("custom") < 0) {
      WORLD.put("custom", 0);
      foundInvalidSetting = true;
    } else if (WORLD.get("custom") > Settings.WORLDS_TO_DISPLAY) {
      WORLD.put("custom", Settings.WORLDS_TO_DISPLAY);
      foundInvalidSetting = true;
    }

    if (SFX_VOLUME.get("custom") < 0) {
      SFX_VOLUME.put("custom", 0);
      foundInvalidSetting = true;
    } else if (SFX_VOLUME.get("custom") > 100) {
      SFX_VOLUME.put("custom", 100);
      foundInvalidSetting = true;
    }

    if (VIEW_DISTANCE.get("custom") < 2300) {
      VIEW_DISTANCE.put("custom", 2300);
      foundInvalidSetting = true;
    } else if (VIEW_DISTANCE.get("custom") > 20000) {
      VIEW_DISTANCE.put("custom", 20000);
      foundInvalidSetting = true;
    }

    if (TRACKPAD_ROTATION_SENSITIVITY.get("custom") < 0) {
      TRACKPAD_ROTATION_SENSITIVITY.put("custom", 0);
      foundInvalidSetting = true;
    } else if (TRACKPAD_ROTATION_SENSITIVITY.get("custom") > 16) {
      TRACKPAD_ROTATION_SENSITIVITY.put("custom", 16);
      foundInvalidSetting = true;
    }

    if (LAST_KNOWN_COMBAT_STYLE.get("custom") < Client.COMBAT_CONTROLLED) {
      LAST_KNOWN_COMBAT_STYLE.put("custom", Client.COMBAT_CONTROLLED);
      foundInvalidSetting = true;
    } else if (LAST_KNOWN_COMBAT_STYLE.get("custom") > Client.COMBAT_DEFENSIVE) {
      LAST_KNOWN_COMBAT_STYLE.put("custom", Client.COMBAT_DEFENSIVE);
      foundInvalidSetting = true;
    }

    if (NAME_PATCH_TYPE.get("custom") < 0) {
      NAME_PATCH_TYPE.put("custom", 0);
      foundInvalidSetting = true;
    } else if (NAME_PATCH_TYPE.get("custom") > 3) {
      NAME_PATCH_TYPE.put("custom", 3);
      foundInvalidSetting = true;
    }

    if (FATIGUE_FIGURES.get("custom") < 1) {
      FATIGUE_FIGURES.put("custom", 1);
      foundInvalidSetting = true;
    } else if (FATIGUE_FIGURES.get("custom") > 7) {
      FATIGUE_FIGURES.put("custom", 7);
      foundInvalidSetting = true;
    }

    if (foundInvalidSetting) {
      save("custom");
    }
  }

  /** @return OS-agnostic string value of a path, unless the provided string is empty */
  public static String sanitizeDirTextValue(String path) {
    if ("".equals(path)) {
      return path;
    }

    if (Util.isWindowsOS()) {
      String windowsPath = new File(path).getAbsolutePath();
      return (windowsPath + (windowsPath.endsWith("\\") ? "" : "\\")).replaceAll("\\+", "\\");
    }

    return (path + (path.endsWith("/") ? "" : "/")).replaceAll("/+", "/");
  }

  /**
   * Determine whether we should default to dark mode for the app interface
   *
   * @return {@code boolean} indicating whether dark mode should be used
   */
  public static boolean shouldDefaultDarkMode() {
    // Detect via JNA/registry for Windows
    if (Util.isWindowsOS()) {
      return Util.isWindowsOSDarkTheme();
    }

    // Default to dark mode for other OS's
    return true;
  }

  /**
   * Creates necessary folders relative to the codebase, which is typically either the jar or
   * location of the package folders
   *
   * @see java.security.CodeSource
   */
  public static void initDir() { // TODO: Consider moving to a more relevant place
    // Find JAR directory
    // TODO: Consider utilizing Util.makeDirectory()
    Dir.JAR = ".";
    try {
      Dir.JAR =
          Settings.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      int indexFileSep1 = Dir.JAR.lastIndexOf('/');
      int indexFileSep2 = Dir.JAR.lastIndexOf('\\');
      int index = (indexFileSep1 > indexFileSep2) ? indexFileSep1 : indexFileSep2;
      if (index != -1) Dir.JAR = Dir.JAR.substring(0, index);
    } catch (Exception e) {
    }

    // Check to see if RSC+ has permissions to write to the current dir
    try {
      if (!Files.isWritable(new File(Settings.Dir.JAR).toPath())) {
        String filePermissionsErrorMessage =
            "<b>Error attempting to launch RSCPlus</b><br/>"
                + "<br/>"
                + "RSCPlus is unable to create configuration files in the following directory:"
                + "<br/><br/>"
                + new File(Dir.JAR).getAbsolutePath()
                + "<br/><br/>"
                + "You must either grant the appropriate permissions to this directory OR<br/>"
                + "move the application to a different location.";
        JPanel filePermissionsErrorPanel =
            Util.createOptionMessagePanel(filePermissionsErrorMessage);

        JOptionPane.showConfirmDialog(
            Launcher.getInstance(),
            filePermissionsErrorPanel,
            "RSCPlus",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE,
            Launcher.scaled_icon_warn);

        System.exit(0);
      }
    } catch (Exception e) {
      Logger.Error("There was an error on startup checking for directory permissions.");
      e.printStackTrace();
    }

    CONFIG_FILE = Dir.JAR + "/config.ini";

    // Load other directories
    Dir.LOGS = Dir.JAR + "/logs";
    Util.makeDirectory(Dir.LOGS);
    Dir.SCREENSHOT = Dir.JAR + "/screenshots";
    Util.makeDirectory(Dir.SCREENSHOT);
    Dir.MODS = Dir.JAR + "/mods";
    Util.makeDirectory(Dir.MODS);
    Dir.REPLAY = Dir.JAR + "/replay";
    Util.makeDirectory(Dir.REPLAY);
    Dir.WORLDS = Dir.JAR + "/worlds";
    Util.makeDirectory(Dir.WORLDS);
    Dir.SPEEDRUN = Dir.JAR + "/speedrun";
    Util.makeDirectory(Dir.SPEEDRUN);
    Dir.BANK = Dir.JAR + "/bank";
    Util.makeDirectory(Dir.BANK);
    Dir.LIB = Dir.JAR + "/lib";
    Util.makeDirectory(Dir.LIB);
    Dir.JINPUTNATIVELIB = Dir.LIB + "/jinput-natives";
    Util.makeDirectory(Dir.JINPUTNATIVELIB);
    Dir.VIDEO = Dir.SCREENSHOT + "/rapid-screenshots";
    Util.makeDirectory(Dir.VIDEO);
  }

  /** Loads properties from config.ini for use with definePresets */
  public static Properties initSettings() {
    // Load settings
    try {
      String versionText = System.getProperty("java.version");
      if (versionText.startsWith("1.")) {
        versionText = versionText.substring(2);
      }
      javaVersion = Integer.parseInt(versionText.substring(0, versionText.indexOf(".")));
    } catch (Exception e) {
      javaVersion = -1;
    }

    try {
      Properties props = loadProps();

      currentProfile = "custom";

      definePresets(props);

      // World Map
      WorldMapWindow.showIcons = getPropBoolean(props, "worldmap_show_icons", true);
      WorldMapWindow.showLabels = getPropBoolean(props, "worldmap_show_labels", true);
      WorldMapWindow.showScenery = getPropBoolean(props, "worldmap_show_scenery", true);
      WorldMapWindow.renderChunkGrid = getPropBoolean(props, "worldmap_show_chunk_grid", false);
      WorldMapWindow.renderChunkLabelling =
          getPropBoolean(props, "worldmap_show_chunk_labelling", false);
      WorldMapWindow.showOtherFloors = getPropBoolean(props, "worldmap_show_other_floors", true);

      updateInjectedVariables(); // TODO remove this function

      // Keybinds
      if (KeyboardHandler.keybindSetList.size() == 0) {
        Logger.Debug("No keybinds defined yet, config window not initialized");
      } else {
        loadKeybinds(props);
      }

      XPBar.pinnedBar = getPropBoolean(props, "pinXPBar", false);
      XPBar.pinnedSkill = getPropInt(props, "pinnedSkill", -1);
      XPBar.showActionCount = getPropBoolean(props, "showActionCount", true);
      XPBar.showTimeCount = getPropBoolean(props, "showTimeCount", true);
      XPBar.skillClickPinning = getPropBoolean(props, "skillClickPinning", true);

      Logger.Info("Loaded settings");
      return props;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Properties loadProps() {
    Properties props = new Properties();

    try {
      File configFile = new File(Settings.CONFIG_FILE);
      if (!configFile.isDirectory()) {
        if (!configFile.exists()) {
          definePresets(props);
          successfullyInitted = true;
          save("custom");
        }
      }

      FileInputStream in = new FileInputStream(Settings.CONFIG_FILE);
      props.load(in);
      in.close();
    } catch (Exception e) {
      Logger.Warn("Error loading config.ini");
      e.printStackTrace();
    }
    return props;
  }

  /**
   * Reads character-specific settings that MUST be re-read from the config.ini file every time the
   * user logs in, loading them into the static variables for this client instance. Doing so is
   * necessary to accommodate multi-client use cases.
   *
   * @param reloadSelf Whether to update settings for the current character
   */
  public static void loadCharacterSpecificSettings(boolean reloadSelf) {
    final Properties props = loadProps();
    final String currentPlayerName = Util.formatPlayerName(Client.player_name);

    // Combat styles
    int numberOfCombatants = getPropInt(props, "numberOfCombatants", 0);
    String[] combatantUsernames = new String[numberOfCombatants];
    for (int usernameID = 0; usernameID < numberOfCombatants; usernameID++) {
      combatantUsernames[usernameID] = getPropString(props, "cmbUsername" + usernameID, "");

      if (!reloadSelf
          && (combatantUsernames[usernameID].equals(currentPlayerName)
              || combatantUsernames[usernameID].equals(Client.playerAlias))) {
        continue;
      }

      int loadedCombatStyle =
          getPropInt(props, String.format("cmbStyle%03d", usernameID), Client.COMBAT_AGGRESSIVE);
      // Sanitize the value if it somehow got corrupted
      if (loadedCombatStyle < Client.COMBAT_CONTROLLED) {
        loadedCombatStyle = Client.COMBAT_CONTROLLED;
      } else if (loadedCombatStyle > Client.COMBAT_DEFENSIVE) {
        loadedCombatStyle = Client.COMBAT_DEFENSIVE;
      }
      Client.playerCombatStyles.put(combatantUsernames[usernameID], loadedCombatStyle);
    }

    // Only read from props on client initialization and logins
    if (reloadSelf) {
      LAST_KNOWN_COMBAT_STYLE.put(
          "custom", Integer.parseInt((String) props.get("last_known_combat_style")));
    }

    // XP and level goals
    int numberOfGoalers = getPropInt(props, "numberOfGoalers", 0);
    String[] goalerUsernames = new String[numberOfGoalers];
    for (int usernameID = 0; usernameID < numberOfGoalers; usernameID++) {
      goalerUsernames[usernameID] = getPropString(props, "username" + usernameID, "");

      if (!reloadSelf
          && (goalerUsernames[usernameID].equals(currentPlayerName)
              || goalerUsernames[usernameID].equals(Client.playerAlias))) {
        continue;
      }

      Client.xpGoals.put(goalerUsernames[usernameID], new Integer[Client.NUM_SKILLS]);
      Client.lvlGoals.put(goalerUsernames[usernameID], new Float[Client.NUM_SKILLS]);
      for (int skill = 0; skill < Client.NUM_SKILLS; skill++) {
        Client.xpGoals.get(goalerUsernames[usernameID])[skill] =
            getPropInt(props, String.format("xpGoal%02d%03d", skill, usernameID), 0);
        try {
          Client.lvlGoals.get(goalerUsernames[usernameID])[skill] =
              Float.parseFloat(
                  getPropString(props, String.format("lvlGoal%02d%03d", skill, usernameID), "0"));
        } catch (Exception e1) {
          Client.lvlGoals.get(goalerUsernames[usernameID])[skill] = new Float(0);
          Logger.Warn(
              "Couldn't parse settings key " + String.format("lvlGoal%02d%03d", skill, usernameID));
        }
      }
    }
  }

  public static void loadKeybinds(Properties props) {
    if (props == null) {
      props = loadProps();
    }

    for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
      String keybindCombo =
          getPropString(
              props, "key_" + kbs.commandName, "" + getPropIntForKeyModifier(kbs) + "*" + kbs.key);
      kbs.modifier = getKeyModifierFromString(keybindCombo);
      kbs.key = Integer.parseInt(keybindCombo.substring(2));
    }

    resolveNewDefaults();
  }

  /**
   * Validates a provided directory path by checking for its existence and write-ability
   *
   * @param dir Directory to validate
   * @param fallback Fallback directory to return if validation fails
   * @return String value of the provided directory if valid, or sanitized fallback if not
   */
  public static String validateCustomDir(String dir, String fallback) {
    boolean returnFallback = false;

    if ("".equals(dir)) {
      returnFallback = true;
    }

    File dirFile = new File(dir);
    if (!(dirFile.exists())) {
      returnFallback = true;
    }

    if (!Files.isWritable(dirFile.toPath())) {
      returnFallback = true;
    }

    if (returnFallback) {
      return sanitizeDirTextValue(fallback);
    }

    return sanitizeDirTextValue(dir);
  }

  /**
   * Resolves conflicting keybindings when a new merge has decided to steal and replace a
   * pre-existing keybinding.
   */
  private static void resolveNewDefaults() {
    for (KeybindSet keybind : KeyboardHandler.keybindSetList) {
      // 02/12/23 - show_player_controls was rebound from alt-up to alt-c
      if (keybind.getCommandName().equals("show_player_controls")) {
        if (keybind.getModifier().equals(KeyModifier.ALT) && keybind.getKey() == KeyEvent.VK_UP) {
          keybind.setModifier(KeyModifier.ALT);
          keybind.setKey(KeyEvent.VK_C);

          break;
        }
      }

      // 08/16/2023 - toggle_food_heal_overlay was removed
      if (keybind.getCommandName().equals("toggle_food_heal_overlay")) {
        if (keybind.getModifier().equals(KeyModifier.CTRL) && keybind.getKey() == KeyEvent.VK_G) {
          keybind.setModifier(null);
          keybind.setKey(-1);

          break;
        }
      }
    }
  }

  public static void initWorlds() {
    File[] fList = new File(Dir.WORLDS).listFiles();

    // Sorts alphabetically
    Arrays.sort(
        fList,
        new Comparator<File>() {
          @Override
          public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
          }
        });

    int i = 1;
    if (fList != null) {
      for (File worldFile : fList) {
        if (!worldFile.getName().equals(".DS_Store")) {
          Properties worldProps = validateWorldFile(worldFile);

          if (worldProps == null) continue;

          WORLD_FILE_PATHS.put(i, worldFile.getAbsolutePath());
          WORLD_NAMES.put(i, worldProps.getProperty("name"));
          WORLD_URLS.put(i, worldProps.getProperty("url"));
          WORLD_PORTS.put(i, Integer.parseInt(worldProps.getProperty("port")));
          WORLD_SERVER_TYPES.put(
              i, Integer.parseInt((String) worldProps.getOrDefault("servertype", "1")));
          WORLD_RSA_PUB_KEYS.put(i, worldProps.getProperty("rsa_pub_key"));
          WORLD_RSA_EXPONENTS.put(i, worldProps.getProperty("rsa_exponent"));
          WORLD_HISCORES_URL.put(i, (String) worldProps.getOrDefault("hiscores_url", ""));

          i++;
        }
      }
    }

    if (i > 1) {
      noWorldsConfigured = false;
      WORLDS_TO_DISPLAY = i - 1;
    } else {
      createNewWorld(1);
      WORLDS_TO_DISPLAY = 1;
    }
  }

  /**
   * Ensures that a world file can be properly parsed into a properties file
   *
   * @param worldFile {@link File} reference for the world ini
   * @return parsed world file {@link Properties} or {@code null} if invalid
   */
  public static Properties validateWorldFile(File worldFile) {
    if (!worldFile.isDirectory() && worldFile.getName().endsWith(".ini")) {
      try (FileInputStream in = new FileInputStream(worldFile)) {
        Properties worldProps = new Properties();
        worldProps.load(in);
        return worldProps;
      } catch (Exception e) {
        // Will fall through below
      }
    }

    Logger.Warn("Error loading World config for " + worldFile.getAbsolutePath());
    return null;
  }

  public static void saveWorlds() {
    // TODO: it would be nice if we only saved a new file if information is different
    for (int i = 1; i <= WORLD_NAMES.size(); i++) {
      String worldFileName =
          String.format("%s%d_%s%s", i < 10 ? "0" : "", i, WORLD_NAMES.get(i), ".ini");
      Properties worldProps = new Properties();

      worldProps.setProperty("name", WORLD_NAMES.get(i));
      worldProps.setProperty("url", WORLD_URLS.get(i));
      worldProps.setProperty("port", WORLD_PORTS.get(i).toString());
      worldProps.setProperty("servertype", WORLD_SERVER_TYPES.get(i).toString());
      worldProps.setProperty("rsa_pub_key", WORLD_RSA_PUB_KEYS.get(i));
      worldProps.setProperty("rsa_exponent", WORLD_RSA_EXPONENTS.get(i));
      worldProps.setProperty("hiscores_url", WORLD_HISCORES_URL.get(i));

      try {
        FileOutputStream out = new FileOutputStream(new File(Dir.WORLDS, worldFileName));
        worldProps.store(out, "---rscplus world config---");
        out.close();
      } catch (Exception e) {
        Logger.Warn("Error saving World config for " + worldFileName);
      }
      try {
        File oldFile = new File(WORLD_FILE_PATHS.get(i));
        if (!worldFileName.equals(oldFile.getName())) {
          if (!oldFile.delete()) {
            Logger.Warn(
                String.format("Error deleting old file %d: %s", i, oldFile.getAbsolutePath()));
          }
          WORLD_FILE_PATHS.put(i, new File(Dir.WORLDS, worldFileName).getAbsolutePath());
        }
      } catch (Exception e) {
        Logger.Warn(String.format("Error deleting old file %d: %s", i, WORLD_FILE_PATHS.get(i)));
      }
    }
  }

  public static void createNewWorld(int worldNum) {
    WORLD_NAMES.put(worldNum, String.format("World %d", worldNum));
    WORLD_URLS.put(worldNum, "");
    WORLD_PORTS.put(worldNum, Replay.DEFAULT_PORT);
    WORLD_SERVER_TYPES.put(worldNum, 1);
    WORLD_RSA_PUB_KEYS.put(worldNum, "");
    WORLD_RSA_EXPONENTS.put(worldNum, "");
    WORLD_HISCORES_URL.put(worldNum, "");

    String worldFileName =
        String.format(
            "%s%d_%s%s", worldNum < 10 ? "0" : "", worldNum, WORLD_NAMES.get(worldNum), ".ini");
    Properties worldProps = new Properties();

    worldProps.setProperty("name", WORLD_NAMES.get(worldNum));
    worldProps.setProperty("url", WORLD_URLS.get(worldNum));
    worldProps.setProperty("port", WORLD_PORTS.get(worldNum).toString());
    worldProps.setProperty("servertype", WORLD_SERVER_TYPES.get(worldNum).toString());
    worldProps.setProperty("rsa_pub_key", WORLD_RSA_PUB_KEYS.get(worldNum));
    worldProps.setProperty("rsa_exponent", WORLD_RSA_EXPONENTS.get(worldNum));
    worldProps.setProperty("hiscores_url", WORLD_HISCORES_URL.get(worldNum));

    try {
      FileOutputStream out = new FileOutputStream(new File(Dir.WORLDS, worldFileName));
      worldProps.store(out, "---rscplus world config---");
      out.close();
    } catch (Exception e) {
      Logger.Warn("Error saving World config for " + worldFileName);
    }

    WORLD_FILE_PATHS.put(worldNum, new File(Dir.WORLDS, worldFileName).getAbsolutePath());
  }

  public static void removeWorld(int worldNum) {
    try {
      File oldFile = new File(WORLD_FILE_PATHS.get(worldNum));
      Logger.Info("Removed old file: " + oldFile.getName());
      oldFile.delete();
    } catch (Exception e) {
      Logger.Warn("Error deleting old file: " + WORLD_FILE_PATHS.get(worldNum));
    }

    int initialSize = WORLD_NAMES.size();
    for (int i = worldNum + 1; i <= initialSize; i++) {
      WORLD_NAMES.put(i - 1, WORLD_NAMES.remove(i));
      WORLD_URLS.put(i - 1, WORLD_URLS.remove(i));
      WORLD_PORTS.put(i - 1, WORLD_PORTS.remove(i));
      WORLD_SERVER_TYPES.put(i - 1, WORLD_SERVER_TYPES.remove(i));
      WORLD_RSA_PUB_KEYS.put(i - 1, WORLD_RSA_PUB_KEYS.remove(i));
      WORLD_RSA_EXPONENTS.put(i - 1, WORLD_RSA_EXPONENTS.remove(i));
      WORLD_FILE_PATHS.put(i - 1, WORLD_FILE_PATHS.remove(i));
      WORLD_HISCORES_URL.put(i - 1, WORLD_HISCORES_URL.remove(i));
    }
    WORLD_NAMES.remove(initialSize);
    WORLD_URLS.remove(initialSize);
    WORLD_PORTS.remove(initialSize);
    WORLD_SERVER_TYPES.remove(initialSize);
    WORLD_RSA_PUB_KEYS.remove(initialSize);
    WORLD_RSA_EXPONENTS.remove(initialSize);
    WORLD_FILE_PATHS.remove(initialSize);
    WORLD_HISCORES_URL.remove(initialSize);
    Settings.WORLDS_TO_DISPLAY--;
    Launcher.getConfigWindow().synchronizeWorldTab();
    saveWorlds();
  }

  private static KeyModifier getKeyModifierFromString(String savedKeybindSet) {
    switch (Integer.parseInt(savedKeybindSet.substring(0, 1))) {
      case 0:
        return KeyModifier.NONE;
      case 1:
        return KeyModifier.CTRL;
      case 2:
        return KeyModifier.ALT;
      case 3:
        return KeyModifier.SHIFT;
      default:
        Logger.Error("Unrecognized KeyModifier code");
        return KeyModifier.NONE;
    }
  }

  /**
   * Writes all setting variables to config.ini, without setting the preset modification flags. Only
   * used within ConfigWindow.applySettings and JConfig.changeWorld.
   */
  public static void saveNoPresetModification() {
    updateInjectedVariables(); // TODO remove this function
    if (currentProfile.equals("custom")) {
      save("custom");
    }
  }

  /** Writes all setting variables to config.ini. */
  public static void save() {
    updateInjectedVariables(); // TODO remove this function
    if (currentProfile.equals("custom")) {
      save("custom");
    } else {
      if (!Settings.presetModified) {
        Settings.presetModified = true;
      }
    }
  }

  public static void save(String preset) {
    if (!successfullyInitted) {
      Logger.Warn(
          "Prevented erroneous save, please report this along with the RSC+ log file, set to debug logging mode");
      return;
    }
    try {
      Properties props = new Properties();

      updateInjectedVariables(); // TODO remove this function

      //// general
      props.setProperty("custom_client_size", Boolean.toString(CUSTOM_CLIENT_SIZE.get(preset)));
      props.setProperty("custom_client_size_x", Integer.toString(CUSTOM_CLIENT_SIZE_X.get(preset)));
      props.setProperty("custom_client_size_y", Integer.toString(CUSTOM_CLIENT_SIZE_Y.get(preset)));
      props.setProperty(
          "enable_window_scaling", Boolean.toString(SCALED_CLIENT_WINDOW.get(preset)));
      props.setProperty("scaling_algorithm", Integer.toString(SCALING_ALGORITHM.get(preset)));
      props.setProperty(
          "integer_scaling_factor", Integer.toString(INTEGER_SCALING_FACTOR.get(preset)));
      props.setProperty(
          "bilinear_scaling_factor", Float.toString(BILINEAR_SCALING_FACTOR.get(preset)));
      props.setProperty(
          "bicubic_scaling_factor", Float.toString(BICUBIC_SCALING_FACTOR.get(preset)));
      props.setProperty("use_dark_flatlaf", Boolean.toString(USE_DARK_FLATLAF.get(preset)));
      props.setProperty("use_nimbus_theme", Boolean.toString(USE_NIMBUS_THEME.get(preset)));
      props.setProperty("check_updates", Boolean.toString(CHECK_UPDATES.get(preset)));
      props.setProperty(
          "show_account_security_settings",
          Boolean.toString(SHOW_ACCOUNT_SECURITY_SETTINGS.get(preset)));
      props.setProperty(
          "confirm_cancel_recovery_change",
          Boolean.toString(CONFIRM_CANCEL_RECOVERY_CHANGE.get(preset)));
      props.setProperty(
          "show_security_tip_day", Boolean.toString(SHOW_SECURITY_TIP_DAY.get(preset)));
      props.setProperty(
          "welcome_enabled", Boolean.toString(REMIND_HOW_TO_OPEN_SETTINGS.get(preset)));
      props.setProperty("load_chat_history", Boolean.toString(LOAD_CHAT_HISTORY.get(preset)));
      props.setProperty("combat_menu", Boolean.toString(COMBAT_MENU_SHOWN.get(preset)));
      props.setProperty("combat_menu_hidden", Boolean.toString(COMBAT_MENU_HIDDEN.get(preset)));
      props.setProperty("show_xpdrops", Boolean.toString(SHOW_XPDROPS.get(preset)));
      props.setProperty("center_xpdrops", Boolean.toString(CENTER_XPDROPS.get(preset)));
      props.setProperty("show_fatiguedrops", Boolean.toString(SHOW_FATIGUEDROPS.get(preset)));
      props.setProperty("fatigue_figures", Integer.toString(FATIGUE_FIGURES.get(preset)));
      props.setProperty("show_fatigueunits", Boolean.toString(SHOW_FATIGUEUNITS.get(preset)));
      props.setProperty("fatigue_alert", Boolean.toString(FATIGUE_ALERT.get(preset)));
      props.setProperty("inventory_full_alert", Boolean.toString(INVENTORY_FULL_ALERT.get(preset)));
      props.setProperty("name_patch_type", Integer.toString(NAME_PATCH_TYPE.get(preset)));
      props.setProperty(
          "disable_nat_rune_alch", Boolean.toString(DISABLE_NAT_RUNE_ALCH.get(preset)));
      props.setProperty("command_patch_quest", Boolean.toString(COMMAND_PATCH_QUEST.get(preset)));
      props.setProperty(
          "command_patch_edible_rares", Boolean.toString(COMMAND_PATCH_EDIBLE_RARES.get(preset)));
      props.setProperty("command_patch_disk", Boolean.toString(COMMAND_PATCH_DISK.get(preset)));
      props.setProperty("bypass_attack", Boolean.toString(ATTACK_ALWAYS_LEFT_CLICK.get(preset)));
      props.setProperty(
          "numbered_dialogue_options", Boolean.toString(NUMBERED_DIALOGUE_OPTIONS.get(preset)));
      props.setProperty(
          "enable_mousewheel_scrolling", Boolean.toString(ENABLE_MOUSEWHEEL_SCROLLING.get(preset)));
      props.setProperty(
          "keep_scrollbar_pos_magic_prayer",
          Boolean.toString(KEEP_SCROLLBAR_POS_MAGIC_PRAYER.get(preset)));
      props.setProperty("hide_roofs", Boolean.toString(HIDE_ROOFS.get(preset)));
      props.setProperty(
          "disable_underground_lighting",
          Boolean.toString(DISABLE_UNDERGROUND_LIGHTING.get(preset)));
      props.setProperty(
          "disable_minimap_rotation", Boolean.toString(DISABLE_MINIMAP_ROTATION.get(preset)));
      props.setProperty("camera_zoomable", Boolean.toString(CAMERA_ZOOMABLE.get(preset)));
      props.setProperty("camera_rotatable", Boolean.toString(CAMERA_ROTATABLE.get(preset)));
      props.setProperty("camera_movable", Boolean.toString(CAMERA_MOVABLE.get(preset)));
      props.setProperty(
          "camera_movable_relative", Boolean.toString(CAMERA_MOVABLE_RELATIVE.get(preset)));
      props.setProperty("colorize", Boolean.toString(COLORIZE_CONSOLE_TEXT.get(preset)));
      props.setProperty("fov", Integer.toString(FOV.get(preset)));
      props.setProperty("fps_limit_enabled", Boolean.toString(FPS_LIMIT_ENABLED.get(preset)));
      props.setProperty("fps_limit", Integer.toString(FPS_LIMIT.get(preset)));
      props.setProperty("software_cursor", Boolean.toString(SOFTWARE_CURSOR.get(preset)));
      props.setProperty("ctrl_scroll_chat", Boolean.toString(CTRL_SCROLL_CHAT.get(preset)));
      props.setProperty(
          "shift_scroll_camera_rotation",
          Boolean.toString(SHIFT_SCROLL_CAMERA_ROTATION.get(preset)));
      props.setProperty(
          "trackpad_rotation_sensitivity",
          Integer.toString(TRACKPAD_ROTATION_SENSITIVITY.get(preset)));
      props.setProperty(
          "custom_ran_chat_effect", Integer.toString(CUSTOM_RAN_CHAT_EFFECT.get(preset).id()));
      props.setProperty(
          "ran_effect_target_fps", Integer.toString(RAN_EFFECT_TARGET_FPS.get(preset)));
      props.setProperty("auto_screenshot", Boolean.toString(AUTO_SCREENSHOT.get(preset)));
      props.setProperty("screenshots_storage_path", SCREENSHOTS_STORAGE_PATH.get(preset));
      props.setProperty("rs2hd_sky", Boolean.toString(RS2HD_SKY.get(preset)));
      props.setProperty(
          "custom_ran_static_colour", Integer.toString(CUSTOM_RAN_STATIC_COLOUR.get(preset)));
      props.setProperty(
          "custom_skybox_overworld_enabled",
          Boolean.toString(CUSTOM_SKYBOX_OVERWORLD_ENABLED.get(preset)));
      props.setProperty(
          "custom_skybox_overworld_colour",
          Integer.toString(CUSTOM_SKYBOX_OVERWORLD_COLOUR.get(preset)));
      props.setProperty(
          "custom_skybox_underground_enabled",
          Boolean.toString(CUSTOM_SKYBOX_UNDERGROUND_ENABLED.get(preset)));
      props.setProperty(
          "custom_skybox_underground_colour",
          Integer.toString(CUSTOM_SKYBOX_UNDERGROUND_COLOUR.get(preset)));
      props.setProperty("view_distance", Integer.toString(VIEW_DISTANCE.get(preset)));
      props.setProperty("patch_gender", Boolean.toString(PATCH_GENDER.get(preset)));
      props.setProperty(
          "patch_hbar_512_last_pixel", Boolean.toString(PATCH_HBAR_512_LAST_PIXEL.get(preset)));
      props.setProperty("use_jagex_fonts", Boolean.toString(USE_JAGEX_FONTS.get(preset)));
      props.setProperty(
          "patch_wrench_menu_spacing", Boolean.toString(PATCH_WRENCH_MENU_SPACING.get(preset)));
      props.setProperty("log_verbosity", Integer.toString(LOG_VERBOSITY.get(preset)));
      props.setProperty("log_show_timestamps", Boolean.toString(LOG_SHOW_TIMESTAMPS.get(preset)));
      props.setProperty("log_show_level", Boolean.toString(LOG_SHOW_LEVEL.get(preset)));
      props.setProperty("log_force_timestamps", Boolean.toString(LOG_FORCE_TIMESTAMPS.get(preset)));
      props.setProperty("log_force_level", Boolean.toString(LOG_FORCE_LEVEL.get(preset)));
      props.setProperty("prefers_xdg_open", Boolean.toString(PREFERS_XDG_OPEN.get(preset)));

      //// audio settings
      props.setProperty("custom_music", Boolean.toString(CUSTOM_MUSIC.get(preset)));
      props.setProperty("custom_music_path", CUSTOM_MUSIC_PATH.get(preset));
      props.setProperty("sfx_volume", Integer.toString(SFX_VOLUME.get(preset)));
      props.setProperty("louder_sound_effects", Boolean.toString(LOUDER_SOUND_EFFECTS.get(preset)));
      props.setProperty(
          "override_audio_setting", Boolean.toString(OVERRIDE_AUDIO_SETTING.get(preset)));
      props.setProperty(
          "override_audio_setting_setting_on",
          Boolean.toString(OVERRIDE_AUDIO_SETTING_SETTING_ON.get(preset)));
      props.setProperty("sound_effect_combat1", Boolean.toString(SOUND_EFFECT_COMBAT1.get(preset)));
      props.setProperty("sound_effect_advance", Boolean.toString(SOUND_EFFECT_ADVANCE.get(preset)));
      props.setProperty("sound_effect_anvil", Boolean.toString(SOUND_EFFECT_ANVIL.get(preset)));
      props.setProperty("sound_effect_chisel", Boolean.toString(SOUND_EFFECT_CHISEL.get(preset)));
      props.setProperty("sound_effect_click", Boolean.toString(SOUND_EFFECT_CLICK.get(preset)));
      props.setProperty(
          "sound_effect_closedoor", Boolean.toString(SOUND_EFFECT_CLOSEDOOR.get(preset)));
      props.setProperty("sound_effect_coins", Boolean.toString(SOUND_EFFECT_COINS.get(preset)));
      props.setProperty(
          "sound_effect_combat1a", Boolean.toString(SOUND_EFFECT_COMBAT1A.get(preset)));
      props.setProperty(
          "sound_effect_combat1b", Boolean.toString(SOUND_EFFECT_COMBAT1B.get(preset)));
      props.setProperty(
          "sound_effect_combat2a", Boolean.toString(SOUND_EFFECT_COMBAT2A.get(preset)));
      props.setProperty(
          "sound_effect_combat2b", Boolean.toString(SOUND_EFFECT_COMBAT2B.get(preset)));
      props.setProperty(
          "sound_effect_combat3a", Boolean.toString(SOUND_EFFECT_COMBAT3A.get(preset)));
      props.setProperty(
          "sound_effect_combat3b", Boolean.toString(SOUND_EFFECT_COMBAT3B.get(preset)));
      props.setProperty("sound_effect_cooking", Boolean.toString(SOUND_EFFECT_COOKING.get(preset)));
      props.setProperty("sound_effect_death", Boolean.toString(SOUND_EFFECT_DEATH.get(preset)));
      props.setProperty(
          "sound_effect_dropobject", Boolean.toString(SOUND_EFFECT_DROPOBJECT.get(preset)));
      props.setProperty("sound_effect_eat", Boolean.toString(SOUND_EFFECT_EAT.get(preset)));
      props.setProperty("sound_effect_filljug", Boolean.toString(SOUND_EFFECT_FILLJUG.get(preset)));
      props.setProperty("sound_effect_fish", Boolean.toString(SOUND_EFFECT_FISH.get(preset)));
      props.setProperty(
          "sound_effect_foundgem", Boolean.toString(SOUND_EFFECT_FOUNDGEM.get(preset)));
      props.setProperty(
          "sound_effect_mechanical", Boolean.toString(SOUND_EFFECT_MECHANICAL.get(preset)));
      props.setProperty("sound_effect_mine", Boolean.toString(SOUND_EFFECT_MINE.get(preset)));
      props.setProperty("sound_effect_mix", Boolean.toString(SOUND_EFFECT_MIX.get(preset)));
      props.setProperty(
          "sound_effect_opendoor", Boolean.toString(SOUND_EFFECT_OPENDOOR.get(preset)));
      props.setProperty(
          "sound_effect_outofammo", Boolean.toString(SOUND_EFFECT_OUTOFAMMO.get(preset)));
      props.setProperty("sound_effect_potato", Boolean.toString(SOUND_EFFECT_POTATO.get(preset)));
      props.setProperty(
          "sound_effect_prayeroff", Boolean.toString(SOUND_EFFECT_PRAYEROFF.get(preset)));
      props.setProperty(
          "sound_effect_prayeron", Boolean.toString(SOUND_EFFECT_PRAYERON.get(preset)));
      props.setProperty(
          "sound_effect_prospect", Boolean.toString(SOUND_EFFECT_PROSPECT.get(preset)));
      props.setProperty(
          "sound_effect_recharge", Boolean.toString(SOUND_EFFECT_RECHARGE.get(preset)));
      props.setProperty("sound_effect_retreat", Boolean.toString(SOUND_EFFECT_RETREAT.get(preset)));
      props.setProperty(
          "sound_effect_secretdoor", Boolean.toString(SOUND_EFFECT_SECRETDOOR.get(preset)));
      props.setProperty("sound_effect_shoot", Boolean.toString(SOUND_EFFECT_SHOOT.get(preset)));
      props.setProperty(
          "sound_effect_spellfail", Boolean.toString(SOUND_EFFECT_SPELLFAIL.get(preset)));
      props.setProperty("sound_effect_spellok", Boolean.toString(SOUND_EFFECT_SPELLOK.get(preset)));
      props.setProperty(
          "sound_effect_takeobject", Boolean.toString(SOUND_EFFECT_TAKEOBJECT.get(preset)));
      props.setProperty(
          "sound_effect_underattack", Boolean.toString(SOUND_EFFECT_UNDERATTACK.get(preset)));
      props.setProperty("sound_effect_victory", Boolean.toString(SOUND_EFFECT_VICTORY.get(preset)));

      //// overlays
      props.setProperty(
          "show_statusdisplay", Boolean.toString(SHOW_HP_PRAYER_FATIGUE_OVERLAY.get(preset)));
      props.setProperty(
          "always_show_statusdisplay_text",
          Boolean.toString(ALWAYS_SHOW_HP_PRAYER_FATIGUE_AS_TEXT.get(preset)));
      props.setProperty("show_buffs", Boolean.toString(SHOW_BUFFS.get(preset)));
      props.setProperty("death_items", Boolean.toString(DEATH_ITEMS.get(preset)));
      props.setProperty("death_items_wild", Boolean.toString(DEATH_ITEMS_WILD.get(preset)));
      props.setProperty(
          "show_last_menu_action", Boolean.toString(SHOW_LAST_MENU_ACTION.get(preset)));
      props.setProperty("show_mouse_tooltip", Boolean.toString(SHOW_MOUSE_TOOLTIP.get(preset)));
      props.setProperty(
          "show_extended_tooltip", Boolean.toString(SHOW_EXTENDED_TOOLTIP.get(preset)));
      props.setProperty("show_invcount", Boolean.toString(SHOW_INVCOUNT.get(preset)));
      props.setProperty(
          "show_invcount_colours", Boolean.toString(SHOW_INVCOUNT_COLOURS.get(preset)));
      props.setProperty("show_rscplus_buttons", Boolean.toString(SHOW_RSCPLUS_BUTTONS.get(preset)));
      props.setProperty(
          "rscplus_buttons_functional", Boolean.toString(RSCPLUS_BUTTONS_FUNCTIONAL.get(preset)));
      props.setProperty(
          "wiki_lookup_on_magic_book", Boolean.toString(WIKI_LOOKUP_ON_MAGIC_BOOK.get(preset)));
      props.setProperty("wiki_lookup_on_hbar", Boolean.toString(WIKI_LOOKUP_ON_HBAR.get(preset)));
      props.setProperty(
          "remove_report_abuse_button_hbar",
          Boolean.toString(REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(preset)));
      props.setProperty(
          "hiscores_lookup_button", Boolean.toString(HISCORES_LOOKUP_BUTTON.get(preset)));
      props.setProperty(
          "motivational_quotes_button", Boolean.toString(MOTIVATIONAL_QUOTES_BUTTON.get(preset)));
      props.setProperty(
          "toggle_xp_bar_on_stats_button",
          Boolean.toString(TOGGLE_XP_BAR_ON_STATS_BUTTON.get(preset)));
      props.setProperty("show_iteminfo", Boolean.toString(SHOW_ITEM_GROUND_OVERLAY.get(preset)));
      props.setProperty(
          "show_iteminfo_highlighted",
          Boolean.toString(SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.get(preset)));
      props.setProperty("show_playerinfo", Boolean.toString(SHOW_PLAYER_NAME_OVERLAY.get(preset)));
      props.setProperty("show_pvpinfo", Boolean.toString(SHOW_PVP_NAME_OVERLAY.get(preset)));
      props.setProperty("pvp_names_colour", Integer.toString(PVP_NAMES_COLOUR.get(preset)));
      props.setProperty("show_owninfo", Boolean.toString(SHOW_OWN_NAME_OVERLAY.get(preset)));
      props.setProperty("show_friendinfo", Boolean.toString(SHOW_FRIEND_NAME_OVERLAY.get(preset)));
      props.setProperty("show_npcinfo", Boolean.toString(SHOW_NPC_NAME_OVERLAY.get(preset)));
      props.setProperty("extend_idsinfo", Boolean.toString(EXTEND_IDS_OVERLAY.get(preset)));
      props.setProperty("trace_objectinfo", Boolean.toString(TRACE_OBJECT_INFO.get(preset)));
      props.setProperty("show_combat_info", Boolean.toString(SHOW_COMBAT_INFO.get(preset)));
      props.setProperty("show_player_position", Boolean.toString(SHOW_PLAYER_POSITION.get(preset)));
      props.setProperty("show_retro_fps", Boolean.toString(SHOW_RETRO_FPS.get(preset)));
      props.setProperty("use_percentage", Boolean.toString(NPC_HEALTH_SHOW_PERCENTAGE.get(preset)));
      props.setProperty("show_hitbox", Boolean.toString(SHOW_HITBOX.get(preset)));
      props.setProperty(
          "show_food_heal_overlay", Boolean.toString(SHOW_FOOD_HEAL_OVERLAY.get(preset)));
      props.setProperty(
          "show_time_until_hp_regen", Boolean.toString(SHOW_TIME_UNTIL_HP_REGEN.get(preset)));
      props.setProperty("indicators", Boolean.toString(LAG_INDICATOR.get(preset)));
      props.setProperty("show_xp_bar", Boolean.toString(SHOW_XP_BAR.get(preset)));
      props.setProperty("debug", Boolean.toString(DEBUG.get(preset)));
      props.setProperty("exception_handler", Boolean.toString(EXCEPTION_HANDLER.get(preset)));
      props.setProperty("highlighted_items", String.join(",", HIGHLIGHTED_ITEMS.get(preset)));
      props.setProperty(
          "special_highlighted_items", String.join(",", SPECIAL_HIGHLIGHTED_ITEMS.get(preset)));
      props.setProperty("blocked_items", String.join(",", BLOCKED_ITEMS.get(preset)));
      props.setProperty(
          "item_highlight_colour", Integer.toString(ITEM_HIGHLIGHT_COLOUR.get(preset)));
      props.setProperty(
          "item_special_highlight_colour",
          Integer.toString(ITEM_SPECIAL_HIGHLIGHT_COLOUR.get(preset)));
      props.setProperty(
          "highlight_items_right_click_menu",
          Boolean.toString(HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.get(preset)));
      props.setProperty("overlay_font_style", Integer.toString(OVERLAY_FONT_STYLE.get(preset)));

      //// bank
      props.setProperty("show_bank_value", Boolean.toString(SHOW_BANK_VALUE.get(preset)));
      props.setProperty("sort_filter_bank", Boolean.toString(SORT_FILTER_BANK.get(preset)));
      props.setProperty(
          "start_searched_bank", Boolean.toString(START_REMEMBERED_FILTER_SORT.get(preset)));
      props.setProperty("search_bank_word", SEARCH_BANK_WORD.get(preset));
      props.setProperty("sort_bank", Bank.getButtonModeString());

      //// notifications
      props.setProperty("tray_notifs", Boolean.toString(TRAY_NOTIFS.get(preset)));
      props.setProperty("tray_notifs_always", Boolean.toString(TRAY_NOTIFS_ALWAYS.get(preset)));
      props.setProperty("notification_sounds", Boolean.toString(NOTIFICATION_SOUNDS.get(preset)));
      props.setProperty("sound_notifs_always", Boolean.toString(SOUND_NOTIFS_ALWAYS.get(preset)));
      props.setProperty(
          "use_system_notifications", Boolean.toString(USE_SYSTEM_NOTIFICATIONS.get(preset)));
      props.setProperty("pm_notifications", Boolean.toString(PM_NOTIFICATIONS.get(preset)));
      props.setProperty("pm_denylist", String.join(",", PM_DENYLIST.get(preset)));
      props.setProperty("trade_notifications", Boolean.toString(TRADE_NOTIFICATIONS.get(preset)));
      props.setProperty("duel_notifications", Boolean.toString(DUEL_NOTIFICATIONS.get(preset)));
      props.setProperty("logout_notifications", Boolean.toString(LOGOUT_NOTIFICATIONS.get(preset)));
      props.setProperty("low_hp_notifications", Boolean.toString(LOW_HP_NOTIFICATIONS.get(preset)));
      props.setProperty("low_hp_notif_value", Integer.toString(LOW_HP_NOTIF_VALUE.get(preset)));
      props.setProperty(
          "fatigue_notifications", Boolean.toString(FATIGUE_NOTIFICATIONS.get(preset)));
      props.setProperty("fatigue_notif_value", Integer.toString(FATIGUE_NOTIF_VALUE.get(preset)));
      props.setProperty(
          "highlighted_item_notifications",
          Boolean.toString(HIGHLIGHTED_ITEM_NOTIFICATIONS.get(preset)));
      props.setProperty(
          "highlighted_item_notif_value",
          Integer.toString(HIGHLIGHTED_ITEM_NOTIF_VALUE.get(preset)));
      props.setProperty("important_messages", String.join(",", IMPORTANT_MESSAGES.get(preset)));
      props.setProperty(
          "important_sad_messages", String.join(",", IMPORTANT_SAD_MESSAGES.get(preset)));
      props.setProperty(
          "mute_important_message_sounds",
          Boolean.toString(MUTE_IMPORTANT_MESSAGE_SOUNDS.get(preset)));

      //// streaming
      props.setProperty("twitch_enabled", Boolean.toString(TWITCH_CHAT_ENABLED.get(preset)));
      props.setProperty("twitch_hide", Boolean.toString(TWITCH_HIDE_CHAT.get(preset)));
      props.setProperty("twitch_channel", TWITCH_CHANNEL.get(preset));
      props.setProperty("twitch_oauth", TWITCH_OAUTH.get(preset));
      props.setProperty("twitch_username", TWITCH_USERNAME.get(preset));
      props.setProperty("show_logindetails", Boolean.toString(SHOW_LOGIN_IP_ADDRESS.get(preset)));
      props.setProperty("save_logininfo", Boolean.toString(SAVE_LOGININFO.get(preset)));
      props.setProperty("start_loginscreen", Boolean.toString(START_LOGINSCREEN.get(preset)));
      props.setProperty("speedrun_active", Boolean.toString(SPEEDRUNNER_MODE_ACTIVE.get(preset)));
      // props.setProperty("speedrun_username", Settings.SPEEDRUNNER_USERNAME.get(preset));

      //// replay
      props.setProperty("replay_storage_path", REPLAY_STORAGE_PATH.get(preset));
      props.setProperty("record_kb_mouse", Boolean.toString(RECORD_KB_MOUSE.get(preset)));
      props.setProperty("parse_opcodes", Boolean.toString(PARSE_OPCODES.get(preset)));
      props.setProperty("fast_disconnect", Boolean.toString(FAST_DISCONNECT.get(preset)));
      props.setProperty("record_automatically", Boolean.toString(RECORD_AUTOMATICALLY.get(preset)));
      props.setProperty(
          "hide_private_msgs_replay", Boolean.toString(HIDE_PRIVATE_MSGS_REPLAY.get(preset)));
      props.setProperty("show_seek_bar", Boolean.toString(SHOW_SEEK_BAR.get(preset)));
      props.setProperty("show_player_controls", Boolean.toString(SHOW_PLAYER_CONTROLS.get(preset)));
      props.setProperty(
          "trigger_alerts_replay", Boolean.toString(TRIGGER_ALERTS_REPLAY.get(preset)));
      props.setProperty("replay_base_path", REPLAY_BASE_PATH.get(preset));
      props.setProperty("preferred_date_format", PREFERRED_DATE_FORMAT.get(preset));
      props.setProperty("show_world_column", Boolean.toString(SHOW_WORLD_COLUMN.get(preset)));
      props.setProperty(
          "show_conversion_column", Boolean.toString(SHOW_CONVERSION_COLUMN.get(preset)));
      props.setProperty(
          "show_userfield_column", Boolean.toString(SHOW_USERFIELD_COLUMN.get(preset)));

      //// world urls
      saveWorlds();

      //// joystick
      props.setProperty("joystick_enabled", Boolean.toString(JOYSTICK_ENABLED.get(preset)));

      //// no gui
      props.setProperty(
          "last_known_combat_style", Integer.toString(LAST_KNOWN_COMBAT_STYLE.get(preset)));
      props.setProperty("world", Integer.toString(WORLD.get(preset)));
      // This is set to false, as logically, saving the config would imply this is not a first-run.
      props.setProperty("first_time", Boolean.toString(false));
      props.setProperty("update_confirmation", Boolean.toString(UPDATE_CONFIRMATION.get(preset)));
      props.setProperty(
          "record_automatically_first_time",
          Boolean.toString(RECORD_AUTOMATICALLY_FIRST_TIME.get(preset)));
      props.setProperty("disassemble", Boolean.toString(DISASSEMBLE.get(preset)));
      props.setProperty("disassemble_directory", DISASSEMBLE_DIRECTORY.get(preset));

      // Keybinds
      for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
        props.setProperty(
            "key_" + kbs.commandName,
            Integer.toString(getPropIntForKeyModifier(kbs)) + "*" + kbs.key);
      }

      // Reload other char settings before re-saving to resolve differences when multi-clienting
      // Note: Skip on 1st client load, as the config file won't exist yet
      if (new File(Settings.CONFIG_FILE).exists()) {
        loadCharacterSpecificSettings(false);
      }

      // Combat styles
      int combatUsernameID = 0;
      for (String username : Client.playerCombatStyles.keySet()) {
        if (username.equals(Replay.excludeUsername)) continue;
        int combatStyle = Client.playerCombatStyles.get(username);
        props.setProperty(
            String.format("cmbStyle%03d", combatUsernameID), Integer.toString(combatStyle));
        props.setProperty(String.format("cmbUsername%d", combatUsernameID), username);
        combatUsernameID++;
      }
      props.setProperty("numberOfCombatants", String.format("%d", combatUsernameID));

      // XP Goals
      int xpUsernameID = 0;
      for (String username : Client.xpGoals.keySet()) {
        if (username.equals(Replay.excludeUsername)) continue;
        for (int skill = 0; skill < Client.NUM_SKILLS; skill++) {
          int skillgoal = 0;
          try {
            skillgoal = Client.xpGoals.get(username)[skill];
          } catch (Exception noGoal) {
          }

          float lvlgoal = (float) 0;
          try {
            lvlgoal = Client.lvlGoals.get(username)[skill];
          } catch (Exception noGoal) {
          }

          props.setProperty(
              String.format("xpGoal%02d%03d", skill, xpUsernameID), Integer.toString(skillgoal));
          props.setProperty(
              String.format("lvlGoal%02d%03d", skill, xpUsernameID), Float.toString(lvlgoal));
        }
        props.setProperty(String.format("username%d", xpUsernameID), username);
        xpUsernameID++;
      }
      props.setProperty("numberOfGoalers", String.format("%d", xpUsernameID));

      props.setProperty("pinXPBar", Boolean.toString(XPBar.pinnedBar));
      props.setProperty("pinnedSkill", String.format("%d", XPBar.pinnedSkill));
      props.setProperty("showActionCount", Boolean.toString(XPBar.showActionCount));
      props.setProperty("showTimeCount", Boolean.toString(XPBar.showTimeCount));
      props.setProperty("skillClickPinning", Boolean.toString(XPBar.skillClickPinning));

      // World Map
      props.setProperty("worldmap_show_icons", Boolean.toString(WorldMapWindow.showIcons));
      props.setProperty("worldmap_show_labels", Boolean.toString(WorldMapWindow.showLabels));
      props.setProperty("worldmap_show_scenery", Boolean.toString(WorldMapWindow.showScenery));
      props.setProperty(
          "worldmap_show_chunk_grid", Boolean.toString(WorldMapWindow.renderChunkGrid));
      props.setProperty(
          "worldmap_show_chunk_labelling", Boolean.toString(WorldMapWindow.renderChunkLabelling));
      props.setProperty(
          "worldmap_show_other_floors", Boolean.toString(WorldMapWindow.showOtherFloors));

      FileOutputStream out = new FileOutputStream(Settings.CONFIG_FILE);
      props.store(out, "---rscplus config---");
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
      Logger.Error("Unable to save settings");
    }
  }

  private static int getPropIntForKeyModifier(KeybindSet kbs) {
    switch (kbs.modifier) {
      case NONE:
        return 0;
      case CTRL:
        return 1;
      case ALT:
        return 2;
      case SHIFT:
        return 3;
      default:
        Logger.Error("Tried to save a keybind with an invalid modifier!");
        return 0;
    }
  }

  /*
   * Setting toggle methods
   */

  public static void toggleWindowScaling() {
    if (ScaledWindow.getInstance().isViewportLoaded()) {
      SCALED_CLIENT_WINDOW.put(
          currentProfile, new Boolean(!SCALED_CLIENT_WINDOW.get(currentProfile)));

      if (SCALED_CLIENT_WINDOW.get(currentProfile)) {
        Client.displayMessage("@cya@Client scaling is now enabled", Client.CHAT_NONE);
      } else {
        Client.displayMessage("@cya@Client scaling is now disabled", Client.CHAT_NONE);
      }

      Settings.renderingScalarUpdateRequired = true;

      save();
    }
  }

  public static void increaseScale() {
    if (ScaledWindow.getInstance().isViewportLoaded()) {
      float scalingDelta = 0f;

      if (!SCALED_CLIENT_WINDOW.get(currentProfile)) {
        Client.displayMessage(
            "@cya@Enable client scaling before attempting to increase the scale value",
            Client.CHAT_NONE);
        return;
      }

      String scaleLimitReached = "@cya@Cannot increase the scale further";

      if (SCALING_ALGORITHM.get(currentProfile).equals(AffineTransformOp.TYPE_NEAREST_NEIGHBOR)) {
        int currentIntegerScalingFactor = INTEGER_SCALING_FACTOR.get(currentProfile);
        scalingDelta = 1.0f;

        if (currentIntegerScalingFactor < (int) Renderer.maxIntegerScalar) {
          int newScale = currentIntegerScalingFactor + (int) scalingDelta;

          INTEGER_SCALING_FACTOR.put(currentProfile, newScale);
          Client.displayMessage("@cya@Increased scale to " + newScale + "x", Client.CHAT_NONE);
        } else {
          Client.displayMessage(scaleLimitReached, Client.CHAT_NONE);
          return;
        }
      } else if (SCALING_ALGORITHM.get(currentProfile).equals(AffineTransformOp.TYPE_BILINEAR)) {
        float currentBilinearScalingFactor = BILINEAR_SCALING_FACTOR.get(currentProfile);
        scalingDelta = 0.1f;

        if (currentBilinearScalingFactor < Renderer.maxInterpolationScalar) {
          float newScale =
              BigDecimal.valueOf(currentBilinearScalingFactor + scalingDelta)
                  .setScale(1, RoundingMode.HALF_DOWN)
                  .floatValue();
          BILINEAR_SCALING_FACTOR.put(currentProfile, newScale);
          Client.displayMessage("@cya@Increased scale to " + newScale + "x", Client.CHAT_NONE);
        } else {
          Client.displayMessage(scaleLimitReached, Client.CHAT_NONE);
          return;
        }
      } else if (SCALING_ALGORITHM.get(currentProfile).equals(AffineTransformOp.TYPE_BICUBIC)) {
        float currentBicubicScalingFactor = BICUBIC_SCALING_FACTOR.get(currentProfile);
        scalingDelta = 0.1f;

        if (currentBicubicScalingFactor < Renderer.maxInterpolationScalar) {
          float newScale =
              BigDecimal.valueOf(currentBicubicScalingFactor + scalingDelta)
                  .setScale(1, RoundingMode.HALF_DOWN)
                  .floatValue();
          BICUBIC_SCALING_FACTOR.put(currentProfile, newScale);
          Client.displayMessage("@cya@Increased scale to " + newScale + "x", Client.CHAT_NONE);
        } else {
          Client.displayMessage(scaleLimitReached, Client.CHAT_NONE);
          return;
        }
      }

      Settings.renderingScalarUpdateRequired = true;

      save();
    }
  }

  public static void decreaseScale() {
    if (ScaledWindow.getInstance().isViewportLoaded()) {
      float scalingDelta = 0;

      if (!SCALED_CLIENT_WINDOW.get(currentProfile)) {
        Client.displayMessage(
            "@cya@Enable client scaling before attempting to decrease the scale value",
            Client.CHAT_NONE);
        return;
      }

      String scaleLimitReached = "@cya@Cannot decrease the scale further";

      if (SCALING_ALGORITHM.get(currentProfile).equals(AffineTransformOp.TYPE_NEAREST_NEIGHBOR)) {
        int currentIntegerScalingFactor = INTEGER_SCALING_FACTOR.get(currentProfile);
        scalingDelta = 1.0f;

        if (currentIntegerScalingFactor > (int) Renderer.minScalar) {
          int newScale = currentIntegerScalingFactor - (int) scalingDelta;
          INTEGER_SCALING_FACTOR.put(currentProfile, newScale);
          Client.displayMessage("@cya@Decreased scale to " + newScale + "x", Client.CHAT_NONE);
        } else {
          Client.displayMessage(scaleLimitReached, Client.CHAT_NONE);
          return;
        }
      } else if (SCALING_ALGORITHM.get(currentProfile).equals(AffineTransformOp.TYPE_BILINEAR)) {
        float currentBilinearScalingFactor = BILINEAR_SCALING_FACTOR.get(currentProfile);
        scalingDelta = 0.1f;

        if (currentBilinearScalingFactor > Renderer.minScalar) {
          float newScale =
              BigDecimal.valueOf(currentBilinearScalingFactor - scalingDelta)
                  .setScale(1, RoundingMode.HALF_DOWN)
                  .floatValue();
          BILINEAR_SCALING_FACTOR.put(currentProfile, newScale);
          Client.displayMessage("@cya@Decreased scale to " + newScale + "x", Client.CHAT_NONE);
        } else {
          Client.displayMessage(scaleLimitReached, Client.CHAT_NONE);
          return;
        }
      } else if (SCALING_ALGORITHM.get(currentProfile).equals(AffineTransformOp.TYPE_BICUBIC)) {
        float currentBicubicScalingFactor = BICUBIC_SCALING_FACTOR.get(currentProfile);
        scalingDelta = 0.1f;

        if (currentBicubicScalingFactor > Renderer.minScalar) {
          float newScale =
              BigDecimal.valueOf(currentBicubicScalingFactor - scalingDelta)
                  .setScale(1, RoundingMode.HALF_DOWN)
                  .floatValue();
          BICUBIC_SCALING_FACTOR.put(currentProfile, newScale);
          Client.displayMessage("@cya@Decreased scale to " + newScale + "x", Client.CHAT_NONE);
        } else {
          Client.displayMessage(scaleLimitReached, Client.CHAT_NONE);
          return;
        }
      }

      Settings.renderingScalarUpdateRequired = true;

      save();
    }
  }

  public static void toggleAttackAlwaysLeftClick() {
    ATTACK_ALWAYS_LEFT_CLICK.put(
        currentProfile, new Boolean(!ATTACK_ALWAYS_LEFT_CLICK.get(currentProfile)));

    if (ATTACK_ALWAYS_LEFT_CLICK.get(currentProfile)) {
      Client.displayMessage(
          "@cya@You are now able to left click attack all monsters", Client.CHAT_NONE);
    } else {
      Client.displayMessage(
          "@cya@You are no longer able to left click attack all monsters", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleNumberedDialogue() {
    NUMBERED_DIALOGUE_OPTIONS.put(
        currentProfile, new Boolean(!NUMBERED_DIALOGUE_OPTIONS.get(currentProfile)));

    if (NUMBERED_DIALOGUE_OPTIONS.get(currentProfile)) {
      Client.displayMessage("@cya@Displaying numbered dialogue options", Client.CHAT_NONE);
    } else {
      Client.displayMessage(
          "@cya@No longer displaying numbered dialogue options", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleHideRoofs() {
    HIDE_ROOFS.put(currentProfile, !HIDE_ROOFS.get(currentProfile));

    if (HIDE_ROOFS.get(currentProfile)) {
      Client.displayMessage("@cya@Roofs are now hidden", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Roofs are now shown", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleCombatMenuShown() {
    COMBAT_MENU_SHOWN.put(currentProfile, !COMBAT_MENU_SHOWN.get(currentProfile));

    if (COMBAT_MENU_SHOWN.get(currentProfile)) {
      Client.displayMessage("@cya@Combat style is now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Combat style is now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleShowFriendNameOverlay() {
    SHOW_FRIEND_NAME_OVERLAY.put(currentProfile, !SHOW_FRIEND_NAME_OVERLAY.get(currentProfile));

    if (SHOW_FRIEND_NAME_OVERLAY.get(currentProfile)) {
      Client.displayMessage("@cya@Friend Names overlay now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Friend Names overlay now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void togglePosition() {
    SHOW_PLAYER_POSITION.put(currentProfile, !SHOW_PLAYER_POSITION.get(currentProfile));
    if (SHOW_PLAYER_POSITION.get(currentProfile))
      Client.displayMessage("@cya@Global Position is now shown", Client.CHAT_NONE);
    else Client.displayMessage("@cya@Global Position is now hidden", Client.CHAT_NONE);
    save();
  }

  public static void toggleRetroFPS() {
    SHOW_RETRO_FPS.put(currentProfile, !SHOW_RETRO_FPS.get(currentProfile));
    if (SHOW_RETRO_FPS.get(currentProfile))
      Client.displayMessage("@cya@Retro FPS is now shown", Client.CHAT_NONE);
    else Client.displayMessage("@cya@Retro FPS is now hidden", Client.CHAT_NONE);
    save();
  }

  public static void toggleXPBar() {
    SHOW_XP_BAR.put(currentProfile, !SHOW_XP_BAR.get(currentProfile));
    if (SHOW_XP_BAR.get(currentProfile))
      Client.displayMessage("@cya@XP Bar is now shown", Client.CHAT_NONE);
    else Client.displayMessage("@cya@XP Bar is now hidden", Client.CHAT_NONE);
    save();
  }

  public static void toggleSkillClickPinning() {
    XPBar.skillClickPinning = !XPBar.skillClickPinning;
    save();
  }

  public static void toggleXPBarPin() {
    SHOW_XP_BAR.put(currentProfile, true);
    if (!XPBar.pinnedBar) Client.displayMessage("@cya@XP Bar is now pinned", Client.CHAT_NONE);
    else Client.displayMessage("@cya@XP Bar is now unpinned", Client.CHAT_NONE);
    XPBar.pinnedBar = !XPBar.pinnedBar;
    save();
  }

  public static void toggleActionCount() {
    XPBar.showActionCount = !XPBar.showActionCount;
    save();
  }

  public static void toggleTimeCount() {
    XPBar.showTimeCount = !XPBar.showTimeCount;
    save();
  }

  public static void toggleShowSeekBar() {
    SHOW_SEEK_BAR.put(currentProfile, !SHOW_SEEK_BAR.get(currentProfile));
    if (SHOW_SEEK_BAR.get(currentProfile))
      Client.displayMessage("@cya@Seek bar is now shown", Client.CHAT_NONE);
    else Client.displayMessage("@cya@Seek bar is now hidden", Client.CHAT_NONE);
    save();
  }

  public static void toggleShowPlayerControls() {
    SHOW_PLAYER_CONTROLS.put(currentProfile, !SHOW_PLAYER_CONTROLS.get(currentProfile));
    if (SHOW_PLAYER_CONTROLS.get(currentProfile))
      Client.displayMessage("@cya@Player controls are now shown", Client.CHAT_NONE);
    else Client.displayMessage("@cya@Player controls are now hidden", Client.CHAT_NONE);
    save();
  }

  public static void toggleInvCount() {
    SHOW_INVCOUNT.put(currentProfile, !SHOW_INVCOUNT.get(currentProfile));

    if (SHOW_INVCOUNT.get(currentProfile)) {
      Client.displayMessage("@cya@Inventory count is now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Inventory count is now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleInvCountColours() {
    SHOW_INVCOUNT_COLOURS.put(currentProfile, !SHOW_INVCOUNT_COLOURS.get(currentProfile));

    if (SHOW_INVCOUNT_COLOURS.get(currentProfile)) {
      Client.displayMessage(
          "@cya@Additional inventory count colours are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage(
          "@cya@Additional inventory count colours are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleBuffs() {
    SHOW_BUFFS.put(currentProfile, !SHOW_BUFFS.get(currentProfile));

    if (SHOW_BUFFS.get(currentProfile)) {
      Client.displayMessage("@cya@Combat (de)buffs and cooldowns are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Combat (de)buffs and cooldowns are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleDeathItems() {
    DEATH_ITEMS.put(currentProfile, !DEATH_ITEMS.get(currentProfile));

    if (DEATH_ITEMS.get(currentProfile)) {
      Client.displayMessage("@cya@Items kept on death are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Items kept on death are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleHpPrayerFatigueOverlay() {
    SHOW_HP_PRAYER_FATIGUE_OVERLAY.put(
        currentProfile, !SHOW_HP_PRAYER_FATIGUE_OVERLAY.get(currentProfile));

    if (SHOW_HP_PRAYER_FATIGUE_OVERLAY.get(currentProfile)) {
      Client.displayMessage("@cya@HP/Prayer/Fatigue are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@HP/Prayer/Fatigue are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleShowHitbox() {
    SHOW_HITBOX.put(currentProfile, !SHOW_HITBOX.get(currentProfile));

    if (SHOW_HITBOX.get(currentProfile)) {
      Client.displayMessage("@cya@Hitboxes are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Hitboxes are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleShowItemGroundOverlay() {
    SHOW_ITEM_GROUND_OVERLAY.put(currentProfile, !SHOW_ITEM_GROUND_OVERLAY.get(currentProfile));

    String baseMessageOn = "@cya@Ground item names are now shown";
    String highlightOnlyMessage = " (Highlighted only)";

    boolean highlightOnlyOff =
        !Settings.SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.get(currentProfile);

    if (SHOW_ITEM_GROUND_OVERLAY.get(currentProfile)) {
      Client.displayMessage(
          highlightOnlyOff ? baseMessageOn : baseMessageOn + highlightOnlyMessage,
          Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Ground item names are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleShowItemGroundHighlightOnlyOverlay() {
    SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.put(
        currentProfile, !SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.get(currentProfile));

    if (SHOW_ITEM_GROUND_OVERLAY_HIGHLIGHTED_ONLY.get(currentProfile)) {
      Client.displayMessage(
          "@cya@Only highlighted ground item names will be shown when enabled", Client.CHAT_NONE);
    } else {
      Client.displayMessage(
          "@cya@All ground item names will be shown when enabled", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleShowNPCNameOverlay() {
    SHOW_NPC_NAME_OVERLAY.put(currentProfile, !SHOW_NPC_NAME_OVERLAY.get(currentProfile));
    if (SHOW_NPC_NAME_OVERLAY.get(currentProfile)) {
      Client.displayMessage("@cya@NPC names are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@NPC names are is now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleShowPlayerNameOverlay() {
    SHOW_PLAYER_NAME_OVERLAY.put(currentProfile, !SHOW_PLAYER_NAME_OVERLAY.get(currentProfile));

    if (SHOW_PLAYER_NAME_OVERLAY.get(currentProfile)) {
      Client.displayMessage("@cya@Player names are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Player names are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleShowOwnNameOverlay() {
    SHOW_OWN_NAME_OVERLAY.put(currentProfile, !SHOW_OWN_NAME_OVERLAY.get(currentProfile));

    if (SHOW_OWN_NAME_OVERLAY.get(currentProfile)) {
      Client.displayMessage(
          "@cya@Your own name is now shown when player names are enabled", Client.CHAT_NONE);
    } else {
      Client.displayMessage(
          "@cya@Your own name is no longer shown when player names are enabled", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleExtendIdsOverlay() {
    EXTEND_IDS_OVERLAY.put(currentProfile, !EXTEND_IDS_OVERLAY.get(currentProfile));

    if (EXTEND_IDS_OVERLAY.get(currentProfile)) {
      Client.displayMessage("@cya@IDs are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@IDs are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleTraceObjectInfo() {
    TRACE_OBJECT_INFO.put(currentProfile, !TRACE_OBJECT_INFO.get(currentProfile));

    if (TRACE_OBJECT_INFO.get(currentProfile)) {
      Client.displayMessage("@cya@Object info now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Object info now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void endSpeedrun() {
    if (!SPEEDRUNNER_MODE_ACTIVE.get(currentProfile)) return;
    Speedrun.endTheRun();
    save();
  }

  public static void toggleShowLoginIpAddress() {
    SHOW_LOGIN_IP_ADDRESS.put(currentProfile, !SHOW_LOGIN_IP_ADDRESS.get(currentProfile));

    if (SHOW_LOGIN_IP_ADDRESS.get(currentProfile)) {
      Client.displayMessage("@cya@IP address will appear next login", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@IP address will not appear next login", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleStartSearchedBank(String searchWord, boolean replaceSavedWord) {
    // Settings.SEARCH_BANK_WORD should be trimmed
    if (SEARCH_BANK_WORD.get("custom").trim().equals("") && searchWord.trim().equals("")) {
      if (START_REMEMBERED_FILTER_SORT.get(currentProfile)) {
        START_REMEMBERED_FILTER_SORT.put(
            currentProfile, !START_REMEMBERED_FILTER_SORT.get(currentProfile));
      }
    } else {
      START_REMEMBERED_FILTER_SORT.put(
          currentProfile, !START_REMEMBERED_FILTER_SORT.get(currentProfile));
      // check if search word should be updated
      if (replaceSavedWord
          && !searchWord.trim().equals("")
          && !searchWord.trim().toLowerCase().equals(SEARCH_BANK_WORD.get("custom"))) {
        SEARCH_BANK_WORD.put("custom", searchWord.trim().toLowerCase());
      }
      if (START_REMEMBERED_FILTER_SORT.get(currentProfile)) {
        Client.displayMessage(
            "@cya@Your bank will start searched with keyword '"
                + SEARCH_BANK_WORD.get("custom")
                + "' next time",
            Client.CHAT_NONE);
      } else {
        Client.displayMessage("@cya@Your bank will start as normal next time", Client.CHAT_NONE);
      }
    }
    save("custom");
  }

  public static void toggleDebug() {
    DEBUG.put(currentProfile, !DEBUG.get(currentProfile));

    if (DEBUG.get(currentProfile)) {
      Client.displayMessage("@cya@Debug mode is on", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Debug mode is off", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleFatigueAlert() {
    FATIGUE_ALERT.put(currentProfile, !FATIGUE_ALERT.get(currentProfile));

    if (FATIGUE_ALERT.get(currentProfile)) {
      Client.displayMessage("@cya@Fatigue alert is now on", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Fatigue alert is now off", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleInventoryFullAlert() {
    INVENTORY_FULL_ALERT.put(currentProfile, !INVENTORY_FULL_ALERT.get(currentProfile));

    if (INVENTORY_FULL_ALERT.get(currentProfile)) {
      Client.displayMessage("@cya@Inventory full alert is now on", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Inventory full alert is now off", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleTwitchHide() {
    TWITCH_HIDE_CHAT.put(currentProfile, !TWITCH_HIDE_CHAT.get(currentProfile));

    if (TWITCH_HIDE_CHAT.get(currentProfile)) {
      Client.displayMessage("@cya@Twitch chat is now hidden", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Twitch chat is now shown", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleXpDrops() {
    SHOW_XPDROPS.put(currentProfile, !SHOW_XPDROPS.get(currentProfile));

    if (SHOW_XPDROPS.get(currentProfile)) {
      Client.displayMessage("@cya@XP drops are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@XP drops are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleFatigueDrops() {
    SHOW_FATIGUEDROPS.put(currentProfile, !SHOW_FATIGUEDROPS.get(currentProfile));

    if (SHOW_FATIGUEDROPS.get(currentProfile)) {
      Client.displayMessage("@cya@Fatigue drops are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Fatigue drops are now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleTrackpadRotation() {
    SHIFT_SCROLL_CAMERA_ROTATION.put(
        currentProfile, !SHIFT_SCROLL_CAMERA_ROTATION.get(currentProfile));

    if (SHIFT_SCROLL_CAMERA_ROTATION.get(currentProfile)) {
      Client.displayMessage("@cya@Trackpad Camera Rotation is now enabled", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Trackpad Camera Rotation is now disabled", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleCtrlScroll() {
    CTRL_SCROLL_CHAT.put(currentProfile, !CTRL_SCROLL_CHAT.get(currentProfile));

    if (CTRL_SCROLL_CHAT.get(currentProfile)) {
      Client.displayMessage(
          "@cya@Hold CTRL to scroll through chat history from anywhere is now enabled",
          Client.CHAT_NONE);
    } else {
      Client.displayMessage(
          "@cya@Hold CTRL to scroll through chat history from anywhere is now disabled",
          Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleColorTerminal() {
    COLORIZE_CONSOLE_TEXT.put(currentProfile, !COLORIZE_CONSOLE_TEXT.get(currentProfile));

    if (COLORIZE_CONSOLE_TEXT.get(currentProfile)) {
      Client.displayMessage("@cya@Colors are now shown in terminal", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Colors are now ignored in terminal", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleLagIndicator() {
    LAG_INDICATOR.put(currentProfile, !LAG_INDICATOR.get(currentProfile));

    if (LAG_INDICATOR.get(currentProfile)) {
      Client.displayMessage("@cya@Connection indicators are now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Connection indicators are now ignored", Client.CHAT_NONE);
    }

    save();
  }

  public static void checkSoftwareCursor() {
    // Only load the software cursor if setting is enabled and
    // the game has begun rendering post-loading
    if (SOFTWARE_CURSOR.get(currentProfile) && ScaledWindow.getInstance().isViewportLoaded()) {
      ScaledWindow.getInstance()
          .setCursor(
              ScaledWindow.getInstance()
                  .getToolkit()
                  .createCustomCursor(
                      new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB),
                      new Point(0, 0),
                      "null"));
    } else {
      ScaledWindow.getInstance().setCursor(Cursor.getDefaultCursor());
    }
  }

  private static void toggleFoodOverlay() {
    // TODO: This toggles the variable but does nothing yet
    SHOW_FOOD_HEAL_OVERLAY.put(currentProfile, !SHOW_FOOD_HEAL_OVERLAY.get(currentProfile));

    if (SHOW_FOOD_HEAL_OVERLAY.get(currentProfile)) {
      Client.displayMessage("@cya@Not yet implemented, sorry!", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Not yet implemented, sorry!", Client.CHAT_NONE);
    }

    save();
  }

  private static void toggleSaveLoginInfo() {
    SAVE_LOGININFO.put(currentProfile, !SAVE_LOGININFO.get(currentProfile));

    if (SAVE_LOGININFO.get(currentProfile)) {
      Client.displayMessage("@cya@Saving login info enabled.", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Saving login info disabled.", Client.CHAT_NONE);
    }

    save();
  }

  private static void toggleHealthRegenTimer() {
    // TODO: This toggles the variable but does nothing yet
    SHOW_TIME_UNTIL_HP_REGEN.put(currentProfile, !SHOW_TIME_UNTIL_HP_REGEN.get(currentProfile));

    if (SHOW_TIME_UNTIL_HP_REGEN.get(currentProfile)) {
      Client.displayMessage("@cya@Not yet implemented, sorry!", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Not yet implemented, sorry!", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleWikiHbar() {
    WIKI_LOOKUP_ON_HBAR.put(currentProfile, !WIKI_LOOKUP_ON_HBAR.get(currentProfile));

    if (WIKI_LOOKUP_ON_HBAR.get(currentProfile)) {
      Client.displayMessage("@cya@Wiki button in Hbar now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Wiki button in Hbar now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void toggleReportAbuse() {
    REMOVE_REPORT_ABUSE_BUTTON_HBAR.put(
        currentProfile, !REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(currentProfile));

    if (!REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(currentProfile)) {
      Client.displayMessage("@cya@Report Abuse button is now shown", Client.CHAT_NONE);
    } else {
      Client.displayMessage("@cya@Report Abuse button is now hidden", Client.CHAT_NONE);
    }

    save();
  }

  public static void setClientFoV(String fovValue) {
    try {
      FOV.put(currentProfile, Integer.parseInt(fovValue));
      Camera.setFoV(FOV.get(currentProfile));
      // If stupid FoV, warn user how to get back
      if (FOV.get(currentProfile) > 10 || FOV.get(currentProfile) < 8) {
        Client.displayMessage(
            "@whi@This is fun, but if you want to go back to normal, use @yel@::fov 9",
            Client.CHAT_QUEST);
      }
    } catch (Exception e) {
      // More sane limitation would be 8 to 10, but it's fun to play with
      Client.displayMessage(
          "@whi@Please use an @lre@integer@whi@ between 7 and 16 (default = 9)", Client.CHAT_QUEST);
    }
    save();
  }

  public static void setSfxVolume(String volumeLevel) {
    String outOfBoundsMessage =
        "@whi@Please use an @lre@integer@whi@ between 0 and 100 (default = 100)";

    try {
      int newVolume = Integer.parseInt(volumeLevel);

      // Warn if value out of bounds
      if (newVolume < 0 || newVolume > 100) {
        Client.displayMessage(outOfBoundsMessage, Client.CHAT_QUEST);
        return;
      }

      SFX_VOLUME.put(currentProfile, newVolume);
      SoundEffects.adjustMudClientSfxVolume();
      Client.displayMessage(
          "@cya@Volume of sound effects was changed to " + volumeLevel + "%", Client.CHAT_NONE);

      Launcher.getConfigWindow().synchronizeGuiValues();
    } catch (Exception e) {
      Client.displayMessage(outOfBoundsMessage, Client.CHAT_QUEST);
    }

    save();
  }

  public static void setOverlayFontStyle(String styleString) {
    String invalidValueMessage = "@whi@Please select a value of 1 or 2";

    try {
      int style = Integer.parseInt(styleString);

      if (style < 1 || style > 2) {
        Client.displayMessage(invalidValueMessage, Client.CHAT_QUEST);
        return;
      }

      OVERLAY_FONT_STYLE.put(currentProfile, style);
      Client.displayMessage(
          "@cya@Overlay font style set to: " + Renderer.OverlayFontStyle.from(style).name(),
          Client.CHAT_NONE);
      Launcher.getConfigWindow().synchronizeGuiValues();
    } catch (Exception e) {
      Client.displayMessage(invalidValueMessage, Client.CHAT_QUEST);
    }

    save();
  }

  /**
   * Gets the String value of a Properties object for the specified key. If no value is defined for
   * that key, it returns the specified default value.
   *
   * @param props the Properties object to read
   * @param key the name of the property to lookup
   * @param defaultProp the default String value of the specified property
   * @return a String value corresponding to the specified property
   */
  private static String getPropString(Properties props, String key, String defaultProp) {
    String value = props.getProperty(key);
    if (value == null) {
      return defaultProp;
    }

    return value;
  }

  /**
   * Gets the ArrayList<String> value of a Properties object for the specified key. If no value is
   * defined for that key, it returns the specified default value.
   *
   * @param props the Properties object to read
   * @param key the name of the property to lookup
   * @param defaultProp the default ArrayList<String> value of the specified property
   * @return an ArrayList<String> value corresponding to the specified property
   */
  private static ArrayList<String> getPropArrayListString(
      Properties props, String key, ArrayList<String> defaultProp) {
    String valueString = props.getProperty(key);
    if (valueString == null) {
      return defaultProp;
    }

    return new ArrayList<>(Arrays.asList(valueString.split(",")));
  }

  /**
   * Gets the Integer value of a Properties object for the specified key. If no value is defined for
   * that key, it returns the specified default value.
   *
   * @param props the Properties object to read
   * @param key the name of the property to lookup
   * @param defaultProp the default Integer value of the specified property
   * @return a Integer value corresponding to the specified property
   */
  private static int getPropInt(Properties props, String key, int defaultProp) {
    String value = props.getProperty(key);
    if (value == null) return defaultProp;

    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      return defaultProp;
    }
  }

  /**
   * Gets the Float value of a Properties object for the specified key. If no value is defined for
   * that key, it returns the specified default value.
   *
   * @param props the Properties object to read
   * @param key the name of the property to lookup
   * @param defaultProp the default Float value of the specified property
   * @return a Float value corresponding to the specified property
   */
  private static float getPropFloat(Properties props, String key, float defaultProp) {
    String value = props.getProperty(key);
    if (value == null) return defaultProp;

    try {
      return Float.parseFloat(value);
    } catch (Exception e) {
      return defaultProp;
    }
  }

  /**
   * Gets the Boolean value of a Properties object for the specified key. If no value is defined for
   * that key, it returns the specified default value.
   *
   * @param props the Properties object to read
   * @param key the name of the property to lookup
   * @param defaultProp the default Boolean value of the specified property
   * @return a Boolean value corresponding to the specified property
   */
  private static boolean getPropBoolean(Properties props, String key, boolean defaultProp) {
    String value = props.getProperty(key);
    if (value == null) return defaultProp;

    try {
      return Boolean.parseBoolean(value);
    } catch (Exception e) {
      return defaultProp;
    }
  }

  /** Contains variables which store folder paths. */
  public static class Dir {

    public static String JAR;
    public static String DUMP;
    public static String LOGS;
    public static String SCREENSHOT;
    public static String VIDEO;
    public static String MODS;
    public static String REPLAY;
    public static String WORLDS;
    public static String SPEEDRUN;
    public static String BANK;
    public static String LIB;
    public static String JINPUTNATIVELIB;
  }

  /**
   * Processes the commands triggered by pressing keybinds
   *
   * @param commandName the name of a keybind command as defined by ConfigWindow.addKeybindSet
   */
  public static boolean processKeybindCommand(String commandName) {
    switch (commandName) {
      case "sleep":
        if (Client.state != Client.STATE_LOGIN && !Replay.isPlaying) Client.sleep();
        return true;
      case "logout":
        if (Client.state != Client.STATE_LOGIN) Client.logout();
        return true;
      case "screenshot":
        Renderer.takeScreenshot(false);
        return true;
      case "toggle_scaling":
        Settings.toggleWindowScaling();
        return true;
      case "increase_scale":
        Settings.increaseScale();
        return true;
      case "decrease_scale":
        Settings.decreaseScale();
        return true;
      case "toggle_indicators":
        Settings.toggleLagIndicator();
        return true;
      case "reset_zoom":
        Camera.resetZoom();
        return true;
      case "reset_rotation":
        Camera.resetRotation();
        return true;
      case "toggle_trackpad_camera_rotation":
        Settings.toggleTrackpadRotation();
        return true;
      case "toggle_ctrl_scroll":
        Settings.toggleCtrlScroll();
        return true;
      case "toggle_colorize":
        Settings.toggleColorTerminal();
        return true;
      case "toggle_combat_xp_menu":
        Settings.toggleCombatMenuShown();
        return true;
      case "toggle_fatigue_alert":
        Settings.toggleFatigueAlert();
        return true;
      case "toggle_inventory_full_alert":
        Settings.toggleInventoryFullAlert();
        return true;
      case "toggle_fatigue_drops":
        Settings.toggleFatigueDrops();
        return true;
      case "toggle_food_heal_overlay":
        Settings.toggleFoodOverlay();
        return true;
      case "toggle_friend_name_overlay":
        Settings.toggleShowFriendNameOverlay();
        return true;
      case "toggle_buffs_display":
        Settings.toggleBuffs();
        return true;
      case "toggle_death_items":
        Settings.toggleDeathItems();
        return true;
      case "toggle_hpprayerfatigue_display":
        Settings.toggleHpPrayerFatigueOverlay();
        return true;
      case "toggle_position_overlay":
        Settings.togglePosition();
        return true;
      case "toggle_retro_fps_overlay":
        Settings.toggleRetroFPS();
        return true;
      case "toggle_inven_count_overlay":
        Settings.toggleInvCount();
        return true;
      case "toggle_inven_count_colours":
        Settings.toggleInvCountColours();
        return true;
      case "toggle_ipdns":
        Settings.toggleShowLoginIpAddress();
        return true;
      case "endrun":
        Settings.endSpeedrun();
        return true;
      case "toggle_item_name_overlay":
        Settings.toggleShowItemGroundOverlay();
        return true;
      case "toggle_item_name_overlay_highlight":
        Settings.toggleShowItemGroundHighlightOnlyOverlay();
        return true;
      case "toggle_hitboxes":
        Settings.toggleShowHitbox();
        return true;
      case "toggle_npc_name_overlay":
        Settings.toggleShowNPCNameOverlay();
        return true;
      case "toggle_ids_overlay":
        Settings.toggleExtendIdsOverlay();
        return true;
      case "toggle_trace_object_info":
        Settings.toggleTraceObjectInfo();
        return true;
      case "toggle_player_name_overlay":
        Settings.toggleShowPlayerNameOverlay();
        return true;
      case "toggle_own_name_overlay":
        Settings.toggleShowOwnNameOverlay();
        return true;
      case "toggle_bypass_attack":
        Settings.toggleAttackAlwaysLeftClick();
        return true;
      case "toggle_numbered_dialogue":
        Settings.toggleNumberedDialogue();
        return true;
      case "toggle_roof_hiding":
        Settings.toggleHideRoofs();
        return true;
      case "toggle_save_login_info":
        Settings.toggleSaveLoginInfo();
        return true;
      case "toggle_health_regen_timer":
        Settings.toggleHealthRegenTimer();
        return true;
      case "toggle_twitch_chat":
        Settings.toggleTwitchHide();
        return true;
      case "toggle_xp_drops":
        Settings.toggleXpDrops();
        return true;
      case "toggle_start_searched_bank":
        Settings.toggleStartSearchedBank("", false);
        return true;
      case "show_config_window":
        Launcher.getConfigWindow().toggleConfigWindow();
        return true;
      case "show_worldmap_window":
        Launcher.getWorldMapWindow().toggleWorldMapWindow();
        return true;
      case "show_queue_window":
        // Try to not allow Replay window to appear while logged into the game :-)
        // (can still open while on login screen, then login to the game)
        if (Replay.isPlaying
            || Replay.isSeeking
            || Replay.isRestarting
            || Client.state == Client.STATE_LOGIN) Launcher.getQueueWindow().showQueueWindow();
        return true;
      case "world_1":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(1);
        return true;
      case "world_2":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(2);
        return true;
      case "world_3":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(3);
        return true;
      case "world_4":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(4);
        return true;
      case "world_5":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(5);
        return true;
      case "world_6":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(6);
        return true;
      case "world_7":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(7);
        return true;
      case "world_8":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(8);
        return true;
      case "world_9":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(9);
        return true;
      case "world_10":
        if (Client.state == Client.STATE_LOGIN) Game.getInstance().getJConfig().changeWorld(10);
        return true;
      case "toggle_report_abuse_button":
        Settings.toggleReportAbuse();
        return true;
      case "toggle_wiki_hbar_button":
        Settings.toggleWikiHbar();
        return true;
      case "stop":
      case "restart":
      case "pause":
      case "ff_plus":
      case "ff_minus":
      case "ff_reset":
      case "next":
      case "prev":
        Replay.controlPlayback(commandName);
        return Replay.isPlaying;
      case "toggle_xp_bar":
        Settings.toggleXPBar();
        return true;
      case "show_seek_bar":
        Settings.toggleShowSeekBar();
        return true;
      case "show_player_controls":
        Settings.toggleShowPlayerControls();
        return true;
      default:
        Logger.Error("An unrecognized command was sent to processCommand: " + commandName);
        break;
    }
    return false;
  }

  /** Restores all settings on the 'General' tab to default values */
  public static void restoreDefaultGeneral() {
    /*
    CUSTOM_CLIENT_SIZE = false;
    CUSTOM_CLIENT_SIZE_X = 512;
    CUSTOM_CLIENT_SIZE_Y = 346;
    LOAD_CHAT_HISTORY = false;
    COMBAT_MENU = false;
    SHOW_XPDROPS = true;
    INDICATORS = true;
    SHOW_FATIGUEDROPS = true;
    FATIGUE_FIGURES = 2;
    FATIGUE_ALERT = true;
    INVENTORY_FULL_ALERT = false;
    NAME_PATCH_TYPE = 3;
    COMMAND_PATCH_LEGACY = 3;
    BYPASS_ATTACK = false;
    HIDE_ROOFS = true;
    COLORIZE = true;
    FOV = 9;
    SOFTWARE_CURSOR = false;
    VIEW_DISTANCE = 10000;
    START_REMEMBERED_FILTER_SORT = false;
    SEARCH_BANK_WORD = "";
          */
    // TODO
    Launcher.getConfigWindow().synchronizeGuiValues();
  }

  /** Restores all settings on the 'Overlays' tab to default values */
  public static void restoreDefaultOverlays() {
    /*
    SHOW_STATUSDISPLAY = true;
    SHOW_BUFFS = true;
    SHOW_INVCOUNT = true;
    SHOW_RETRO_FPS = false;
    SHOW_ITEMINFO = false;
    SHOW_PLAYERINFO = false;
    SHOW_FRIENDINFO = false;
    SHOW_NPCINFO = false;
    SHOW_COMBAT_INFO = false;
    USE_PERCENTAGE = false;
    SHOW_HITBOX = false;
    SHOW_FOOD_HEAL_OVERLAY = false;
    SHOW_TIME_UNTIL_HP_REGEN = false;
    DEBUG = false;
    HIGHLIGHTED_ITEMS = new ArrayList<String>();
    BLOCKED_ITEMS = new ArrayList<String>();
          */
    // TODO
    Launcher.getConfigWindow().synchronizeGuiValues();
  }

  /** Restores all settings on the 'Notifications' tab to default values */
  public static void restoreDefaultNotifications() {
    /*
    PM_NOTIFICATIONS = true;
    TRADE_NOTIFICATIONS = true;
    DUEL_NOTIFICATIONS = true;
    LOGOUT_NOTIFICATIONS = true;
    LOW_HP_NOTIFICATIONS = true;
    LOW_HP_NOTIF_VALUE = 25;
    FATIGUE_NOTIFICATIONS = true;
    FATIGUE_NOTIF_VALUE = 98;
    NOTIFICATION_SOUNDS = !isRecommendedToUseSystemNotifs();
    USE_SYSTEM_NOTIFICATIONS = isRecommendedToUseSystemNotifs();
    TRAY_NOTIFS = true;
    TRAY_NOTIFS_ALWAYS = false;
          */
    // TODO
    Launcher.getConfigWindow().synchronizeGuiValues();
  }

  /** Restores all settings on the 'Streaming &#38; Privacy' tab to default values */
  public static void restoreDefaultPrivacy() {
    /*
    TWITCH_HIDE = false;
    TWITCH_CHANNEL = "";
    TWITCH_OAUTH = "";
    TWITCH_USERNAME = "";
    SHOW_LOGINDETAILS = true;
    SAVE_LOGININFO = true;
          */
    // TODO
    Launcher.getConfigWindow().synchronizeGuiValues();
  }

  /** Restores all keybinds to the default values */
  public static void restoreDefaultKeybinds() {
    try {
      for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
        KeybindSet defaultKBS = KeyboardHandler.defaultKeybindSetList.get(kbs.commandName);
        kbs.key = defaultKBS.key;
        kbs.modifier = defaultKBS.modifier;
      }
    } catch (NullPointerException npe) {
      Logger.Error("Null Pointer while attempting to restore default keybind values!");
    }
    Launcher.getConfigWindow().synchronizeGuiValues();
  }

  /** Note: This gets invoked nonstop during gameplay */
  public static void updateInjectedVariables() {
    // TODO: get rid of these variables and this function if possible
    COMBAT_STYLE_INT = Client.combat_style;
    HIDE_ROOFS_BOOL = HIDE_ROOFS.get(currentProfile);
    DISABLE_UNDERGROUND_LIGHTING_BOOL = DISABLE_UNDERGROUND_LIGHTING.get(currentProfile);
    DISABLE_MINIMAP_ROTATION_BOOL = DISABLE_MINIMAP_ROTATION.get(currentProfile);
    COMBAT_MENU_SHOWN_BOOL = COMBAT_MENU_SHOWN.get(currentProfile);
    COMBAT_MENU_HIDDEN_BOOL = COMBAT_MENU_HIDDEN.get(currentProfile);
    CAMERA_ZOOMABLE_BOOL = CAMERA_ZOOMABLE.get(currentProfile);
    CAMERA_ROTATABLE_BOOL = CAMERA_ROTATABLE.get(currentProfile);
    CAMERA_MOVABLE_BOOL = CAMERA_MOVABLE.get(currentProfile);
    USE_JAGEX_FONTS_BOOL = USE_JAGEX_FONTS.get(currentProfile);
    PROTECT_NAT_RUNE_ALCH_BOOL = DISABLE_NAT_RUNE_ALCH.get(currentProfile);
    LOAD_CHAT_HISTORY_BOOL = LOAD_CHAT_HISTORY.get(currentProfile);
    HIGHLIGHT_ITEMS_MENU_BOOL = HIGHLIGHT_ITEMS_RIGHT_CLICK_MENU.get(currentProfile);
  }

  /** Invoked when combat style changes */
  public static void outputInjectedVariables() {
    // TODO: get rid of these variables and this function if possible

    if (Client.player_name.isEmpty() || Client.username_login.equals(Replay.excludeUsername)) {
      return;
    }

    // Save character-specific combat style
    Client.playerCombatStyles.put(Util.formatPlayerName(Client.player_name), COMBAT_STYLE_INT);

    // If an alias exists, save to it as well
    if (!Client.playerAlias.isEmpty()) {
      Client.playerCombatStyles.put(Client.playerAlias, COMBAT_STYLE_INT);
    }

    // Save to the global previously-known combat style as a fallback
    LAST_KNOWN_COMBAT_STYLE.put("custom", COMBAT_STYLE_INT);
  }

  /**
   * Returns if it is recommended for the OS to use system notifications.
   *
   * @return if it is recommended to use system notifications
   */
  public static boolean isRecommendedToUseSystemNotifs() {
    // Users on Windows 8.1+ are recommend to set USE_SYSTEM_NOTIFICATIONS = true
    if (Util.isWindowsOS()) {
      return Util.isModernWindowsOS();
    } else { // Linux, macOS, etc.
      return NotificationsHandler.hasNotifySend;
    }
  }
}
