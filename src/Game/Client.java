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
package Game;

import static Replay.game.constants.Game.itemActionMap;

import Client.JClassPatcher;
import Client.JConfig;
import Client.KeybindSet;
import Client.Launcher;
import Client.Logger;
import Client.NotificationsHandler;
import Client.NotificationsHandler.NotifType;
import Client.Settings;
import Client.Speedrun;
import Client.TwitchIRC;
import Client.Util;
import Client.WorldMapWindow;
import Replay.game.constants.Game.ItemAction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.applet.Applet;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * This class prepares the client for login, handles chat messages, and performs player related
 * calculations.
 */
public class Client {

  // Game's client instance
  public static Object instance;

  public static Map<String, LinkedList<String>> tracerInstructions =
      new LinkedHashMap<String, LinkedList<String>>();

  public static List<NPC> npc_list = new ArrayList<>();
  public static List<NPC> npc_list_retained = new ArrayList<>();
  public static List<Item> item_list = new ArrayList<>();
  public static List<Item> item_list_retained = new ArrayList<>();

  public static final int SKILL_ATTACK = 0;
  public static final int SKILL_DEFENSE = 1;
  public static final int SKILL_STRENGTH = 2;
  public static final int SKILL_HP = 3;
  public static final int SKILL_RANGED = 4;
  public static final int SKILL_PRAYER = 5;
  public static final int SKILL_MAGIC = 6;
  public static final int SKILL_COOKING = 7;
  public static final int SKILL_WOODCUT = 8;
  public static final int SKILL_FLETCHING = 9;
  public static final int SKILL_FISHING = 10;
  public static final int SKILL_FIREMAKING = 11;
  public static final int SKILL_CRAFTING = 12;
  public static final int SKILL_SMITHING = 13;
  public static final int SKILL_MINING = 14;
  public static final int SKILL_HERBLAW = 15;
  public static final int SKILL_AGILITY = 16;
  public static final int SKILL_THIEVING = 17;

  public static final int STAT_PRAYER = 4;
  public static final int[] DRAIN_RATES = {15, 15, 15, 30, 30, 30, 5, 10, 10, 60, 60, 60, 60, 60};

  public static final int STATE_LOGIN = 1;
  public static final int STATE_GAME = 2;

  public static final int SCREEN_CLICK_TO_LOGIN = 0; // also is this value while logged in
  public static final int SCREEN_REGISTER_NEW_ACCOUNT = 1;
  public static final int SCREEN_USERNAME_PASSWORD_LOGIN = 2;
  public static final int SCREEN_PASSWORD_RECOVERY = 3;

  public static final int MENU_NONE = 0;
  public static final int MENU_INVENTORY = 1;
  public static final int MENU_MINIMAP = 2;
  public static final int MENU_STATS_QUESTS = 3;
  public static final int MENU_MAGIC_PRAYERS = 4;
  public static final int MENU_FRIENDS_IGNORE = 5;
  public static final int MENU_SETTINGS = 6;

  public static final int MENU_STATS = 0;
  public static final int MENU_QUESTS = 1;

  public static final int CHAT_NONE = 0;
  public static final int CHAT_PRIVATE = 1;
  public static final int CHAT_PRIVATE_OUTGOING = 2;
  public static final int CHAT_QUEST = 3;
  public static final int CHAT_CHAT = 4;
  public static final int CHAT_PRIVATE_LOG_IN_OUT = 5;
  public static final int CHAT_TRADE_REQUEST_RECEIVED =
      6; // only used when another player sends you a trade request. (hopefully!)
  public static final int CHAT_OTHER =
      7; // used for when you send a player a duel/trade request, follow someone, or drop an item

  public static final int CHAT_INCOMING_OPTION = 8;
  public static final int CHAT_CHOSEN_OPTION = 9;
  public static final int CHAT_WINDOWED_MSG = 10;

  public static final int COMBAT_CONTROLLED = 0;
  public static final int COMBAT_AGGRESSIVE = 1;
  public static final int COMBAT_ACCURATE = 2;
  public static final int COMBAT_DEFENSIVE = 3;

  public static int state = STATE_LOGIN;

  public static final int POPUP_CANCEL_RECOVERY = 10;
  public static final int POPUP_BANK_SEARCH = 11;

  public static final int WRENCH_ALIGNMENT_FIX = 5;
  public static final int WRENCH_ALIGNMENT_NO_FIX = 0;

  public static int max_inventory;
  public static int inventory_count;
  public static long magic_timer = 0L;

  public static int combat_timer;
  public static boolean isGameLoaded;
  public static boolean show_bank;
  public static boolean show_duel;
  public static boolean show_duelconfirm;
  public static int show_friends;
  public static int show_menu;
  public static int show_stats_or_quests;
  public static boolean show_questionmenu;
  public static int show_report;
  public static boolean show_shop;
  public static boolean show_sleeping;
  public static boolean show_trade;
  public static boolean show_tradeconfirm;
  public static boolean show_welcome;
  public static boolean show_appearance;

  public static boolean runReplayHook = false;
  public static boolean runReplayCloseHook = false;

  public static int[] inventory_items;

  public static long poison_timer = 0L;
  public static boolean is_poisoned = false;
  public static boolean is_in_wild;
  // fatigue units as sent by the server
  public static int fatigue;
  // fatigue in units
  public static int current_fatigue_units;
  // fatigue in percentage
  private static float currentFatigue;
  public static boolean[] prayers_on;
  // equipment stats (array position 4 holds prayer bonus to determine change drain rate)
  public static int[] current_equipment_stats;
  public static int[] current_level;
  public static int[] base_level;
  public static int[] xp;
  public static String[] skill_name;
  public static int combat_style;

  public static int friends_count;
  public static String[] friends;
  public static String[] friends_world;
  public static String[] friends_formerly;
  public static int[] friends_online;

  public static int ignores_count;
  public static String[] ignores;
  public static String[] ignores_formerly;
  public static String[] ignores_copy;
  public static String[] ignores_formerly_copy;

  public static String pm_username;
  public static String pm_text;
  public static String pm_enteredText;
  public static String lastpm_username = null;
  public static String modal_text;
  public static String modal_enteredText;

  public static int login_screen;
  public static String username_login;
  public static String password_login;
  public static int autologin_timeout;

  public static Object player_object;
  public static String player_name = "";
  public static String xpUsername = "";
  public static boolean knowWhoIAm = false;
  public static int player_posX = -1;
  public static int player_posY = -1;
  public static int player_height = -1;
  public static int player_width = -1;

  public static int regionX = -1;
  public static int regionY = -1;
  public static int worldX = -1;
  public static int worldY = -1;
  public static int localRegionX = -1;
  public static int localRegionY = -1;
  public static int planeWidth = -1;
  public static int planeHeight = -1;
  public static int planeIndex = -1;
  public static boolean loadingArea = false;

  public static boolean sleepCmdSent = false;
  public static int sleepBagIdx = -1;

  public static Object clientStream;
  public static Object writeBuffer;
  public static Object menuCommon;
  public static Object packetsIncoming;

  public static final int TRACER_LINES = 100;

  // bank items and their count for each type, new bank items are first to get updated and indicate
  // bank
  // excluding inventory types and bank items do include them (in regular mode), as bank operations
  // are messy
  // they get excluded also in searchable bank
  public static int[] bank_items_count;
  public static int[] bank_items;
  public static int[] new_bank_items_count;
  public static int[] new_bank_items;
  public static int bank_active_page;
  public static int bank_items_max;

  // these two variables, they indicate distinct bank items count
  public static int new_count_items_bank;
  public static int count_items_bank;

  public static int selectedItem;
  public static int selectedItemSlot;
  public static boolean is_hover;

  /** An array of Strings that stores text used in the client */
  public static String[] strings;

  public static XPDropHandler xpdrop_handler = new XPDropHandler();
  public static XPBar xpbar = new XPBar();

  private static TwitchIRC twitch = new TwitchIRC();
  public static MouseHandler handler_mouse;
  public static KeyboardHandler handler_keyboard;

  private static long updateTimer = 0;
  private static long last_time = 0;

  public static boolean showRecordAlwaysDialogue = false;

  public static long update_timer;
  public static long updates;
  public static long updatesPerSecond;

  public static String lastSoundEffect = "";

  public static BigInteger modulus;
  public static BigInteger exponent;
  public static int maxRetries;
  public static byte[] fontData;
  public static byte[] indexData;
  public static byte[] hbarOrigData;
  public static byte[] hbarRetroData;
  public static String lastServerMessage = "";
  public static int[] inputFilterCharFontAddr;

  public static byte[] lastIncomingBytes;

  public static Thread loginMessageHandlerThread;
  public static String loginMessageTop =
      "To connect to a server, please configure your World URLs.";
  public static String loginMessageBottom =
      "Hit @gre@Ctrl-O@whi@ to access Settings and select the \"World List\" tab.";

  public static final int NUM_SKILLS = 18;

  /**
   * A boolean array that stores if the XP per hour should be shown for a given skill when hovering
   * on the XP bar.
   *
   * <p>This should only be false for a skill if there has been less than 2 XP drops during the
   * current tracking session, since there is not enough data to calculate the XP per hour.
   */
  private static HashMap<String, Boolean[]> showXpPerHour = new HashMap<String, Boolean[]>();

  /** An array to store the XP per hour for a given skill */
  private static HashMap<String, Double[]> xpPerHour = new HashMap<String, Double[]>();

  // the total XP gained in a given skill within the sample period
  private static final int TOTAL_XP_GAIN = 0;
  // the time of the last XP drop in a given skill
  private static final int TIME_OF_LAST_XP_DROP = 1;
  // the time of the first XP drop in a given skill within the sample period
  private static final int TIME_OF_FIRST_XP_DROP = 2;
  // the total number of XP drops recorded within the sample period, plus 1
  private static final int TOTAL_XP_DROPS = 3;
  // the amount of XP gained since last processed
  private static final int LAST_XP_GAIN = 4;

  // first dimension of this array is skill ID.
  // second dimension is the constants in block above.
  private static HashMap<String, Double[][]> lastXpGain = new HashMap<String, Double[][]>();

  // holds players XP since last processing xp drops
  private static HashMap<String, Float[]> xpLast = new HashMap<String, Float[]>();

  public static HashMap<String, Integer[]> xpGoals = new HashMap<String, Integer[]>();
  public static HashMap<String, Float[]> lvlGoals = new HashMap<String, Float[]>();

  private static float[] xpGain = new float[18];

  /** The client version */
  public static int version;

  public static String[] menuOptions;

  public static int tileSize;
  public static long menu_timer;
  public static String lastAction;

  public static int login_delay;
  public static String server_address;
  public static int serverjag_port;
  public static int session_id;
  public static boolean failedRecovery = false;
  public static int recoveryChangeDays;
  public static int tipOfDay = -1;

  public static Object panelWelcome;
  public static Object panelLogin;
  public static Object panelRegister;
  public static Object panelRecovery;
  public static Object panelRecoveryQuestions;
  public static Object panelContactDetails;
  public static int controlServerType;
  public static int controlLoginTop;
  public static int controlLoginBottom;
  public static int loginUserInput;
  public static int loginPassInput;
  public static int loginLostPasswordButton;
  public static int registerButton;
  public static int controlRegister;
  public static int chooseUserInput;
  public static int choosePasswordInput;
  public static int chooseConfirmPassInput;
  public static int acceptTermsCheckbox;
  public static int chooseSubmitRegisterButton;
  public static int chooseCancelRegisterButton;
  public static int controlRecoveryTop;
  public static int controlRecoveryBottom;
  public static int controlRecoveryQuestion[] = new int[5];
  public static int controlRecoveryInput[] = new int[5];
  public static int recoverOldPassInput;
  public static int recoverNewPassInput;
  public static int recoverConfirmPassInput;
  public static int chooseSubmitRecoveryButton;
  public static int chooseCancelRecoveryButton;
  public static int controlRecoveryQuestions;
  public static String controlRecoveryText[] = new String[5];
  public static int controlRecoveryIns[] = new int[5];
  public static int controlAnswerInput[] = new int[5];
  public static int controlQuestion[] = new int[5];
  public static int controlCustomQuestion[] = new int[5];
  public static int chooseFinishSetRecoveryButton;
  public static int controlContactDetails;
  public static int fullNameInput;
  public static int zipCodeInput;
  public static int countryInput;
  public static int emailInput;
  public static int chooseSubmitContactDetailsButton;
  public static boolean showAppearanceChange;
  public static boolean showRecoveryQuestions;
  public static boolean showContactDetails;

  public static boolean firstTimeRunningRSCPlus = false;

  public static int mouse_click;
  public static boolean singleButtonMode;
  public static boolean firstTime = true;
  public static Object worldInstance;
  public static int lastHeightOffset;

  // BELOW VARIABLE USED IN THE REMOVE X RESIZABLE BUG-FIX WHEN TRADING
  public static String[] items_remove_message =
      new String[] {"Enter number of items to remove and press enter"};

  public static Boolean lastIsMembers = null;

  // used in original client
  public static boolean members;
  public static boolean veterans;
  // used to distinguish live world, in replay there are two similar variables
  public static boolean worldMembers;
  public static boolean worldVeterans;
  public static Object soundSub = null;
  public static Object gameContainer = null;

  public static boolean usingRetroTabs = false;

  public static AreaDefinition[][] areaDefinitions = new AreaDefinition[AreaDefinition.SIZE_X][AreaDefinition.SIZE_Y_ALL];

  public static final String[] colorDict = {
    // less common colors should go at the bottom b/c we can break search loop early
    // several of the orange & green colours are basically the same colour, even in-game
    "(?i)@cya@", "|@@|cyan ",
    "(?i)@whi@", "|@@|white ",
    "(?i)@red@", "|@@|red ",
    "(?i)@gre@", "|@@|green ",
    "(?i)@lre@", "|@@|red,intensity_faint ",
    "(?i)@dre@", "|@@|red,intensity_bold ",
    "(?i)@ran@", "|@@|red,blink_fast ", // TODO: consider handling this specially
    "(?i)@yel@", "|@@|yellow ",
    "(?i)@mag@", "|@@|magenta,intensity_bold ",
    "(?i)@gr1@", "|@@|green ",
    "(?i)@gr2@", "|@@|green ",
    "(?i)@gr3@", "|@@|green ",
    "(?i)@ora@", "|@@|red,intensity_faint ",
    "(?i)@or1@", "|@@|red,intensity_faint ",
    "(?i)@or2@", "|@@|red,intensity_faint ",
    "(?i)@or3@", "|@@|red ",
    "(?i)@blu@", "|@@|blue ",
    "(?i)@bla@", "|@@|black "
  };

  /**
   * Iterates through {@link #strings} array and checks if various conditions are met. Used for
   * patching client text.
   */
  public static void adaptStrings() {}

  // string 662 is the one in version 235 that contains the "from: " used in login welcome screen
  public static void adaptLoginInfo() {
    if (!Settings.SHOW_LOGIN_IP_ADDRESS.get(Settings.currentProfile)
        && strings[662].startsWith("from:")) {
      strings[662] = "@bla@from: ";
    } else if (Settings.SHOW_LOGIN_IP_ADDRESS.get(Settings.currentProfile)
        && strings[662].startsWith("@bla@from:")) {
      strings[662] = "from: ";
    }
  }

  public static int shadowSleepCount;

  /*
    This method replaces u.a (IJ)
    shadowSleepCount is unused in the client, but is hooked and incremented properly anyway.
    According to "Hixk": https://github.com/RSCPlus/rscplus/pull/16#issuecomment-648823713

    This function was recreated from the disassembly of the function, please leave the original
    code commented out in the function and explain why it was removed!

    TODO: Figure out what unknown is
  */
  public static final void shadowSleep(int unknown, long ms) {
    /* Removing this increases stability according to the Hixk issue linked above */
    // if (unknown != 0) return;

    try {
      Thread.sleep(ms);
    } catch (Exception e) {
    }

    shadowSleepCount += 1;
  }

  public static void CrashFixRoutine(Throwable e, int index) {
    Logger.Error("A crash was prevented, here is some information about it.");
    PrintException(e, index);
  }

  public static void PrintException(Throwable e, int index) {
    String printMessage = "Caller: " + JClassPatcher.ExceptionSignatures.get(index) + "\n\n";
    if (e.getMessage() != null) printMessage = "Message: " + e.getMessage() + "\n" + printMessage;
    StackTraceElement[] stacktrace = e.getStackTrace();
    for (int i = 0; i < stacktrace.length; i++) {
      StackTraceElement element = stacktrace[i];
      printMessage += element.getClassName() + "." + element.getMethodName() + "(UNKNOWN)";
      if (i != stacktrace.length - 1) printMessage += "\n";
    }

    // Add tracer information
    Iterator tracerIterator = tracerInstructions.entrySet().iterator();
    if (tracerIterator.hasNext()) {
      printMessage += "\n\n";
    }
    while (tracerIterator.hasNext()) {
      Map.Entry element = (Map.Entry) tracerIterator.next();
      String name = (String) element.getKey();
      String[] tracer = (String[]) ((LinkedList<String>) element.getValue()).toArray();
      printMessage += "[" + name + "]\n";
      for (int i = 0; i < tracer.length; i++) {
        String instruction = (String) tracer[i];
        if (instruction != null) {
          printMessage += instruction;
          if (i != tracer.length - 1) printMessage += "\n";
        }
      }

      if (tracerIterator.hasNext()) printMessage += "\n\n";
    }

    Logger.Error("EXCEPTION\n" + printMessage);
  }

  public static Throwable HandleException(Throwable e, int index) {
    if (!Settings.EXCEPTION_HANDLER.get(Settings.currentProfile)) return e;

    PrintException(e, index);

    return e;
  }

  public static void TracerHandler(int indexHigh, int indexLow) {
    // Convert index
    if (indexHigh < 0) indexHigh += Short.MAX_VALUE * 2;
    if (indexLow < 0) indexLow += Short.MAX_VALUE * 2;
    int index = (indexHigh << 16) | indexLow;

    Thread thread = Thread.currentThread();
    String threadName = thread.getName();

    LinkedList<String> instructions;
    if (tracerInstructions.containsKey(threadName)) {
      instructions = tracerInstructions.get(threadName);
    } else {
      instructions =
          new LinkedList<String>() {
            private Object threadLock = new Object();

            @Override
            public boolean add(String object) {
              boolean result;
              if (this.size() >= TRACER_LINES) removeFirst();
              synchronized (threadLock) {
                result = super.add(object);
              }
              return result;
            }

            @Override
            public String removeFirst() {
              String result;
              synchronized (threadLock) {
                result = super.removeFirst();
              }
              return result;
            }

            @Override
            public String[] toArray() {
              String[] result;
              synchronized (threadLock) {
                result = new String[size()];
                for (int i = 0; i < size(); i++) result[i] = get(i);
              }
              return result;
            }
          };
      tracerInstructions.put(threadName, instructions);
    }

    // Add decoded instruction to tracer
    String instruction = JClassPatcher.InstructionBytecode.get(index);
    instructions.add(instruction);
  }

  public static void loadAreaDefinitions()
  {
    // Set default region
    for (int x = 0; x < AreaDefinition.SIZE_X; x++)
      for (int y = 0; y < AreaDefinition.SIZE_Y_ALL; y++)
        areaDefinitions[x][y] = AreaDefinition.DEFAULT;

    try {
      String areaJson = Util.readString(Launcher.getResource("/assets/areas.json").openStream());
      JSONArray obj = new JSONArray(areaJson);
      for (int i = 0; i < obj.length(); i++) {
        JSONObject entry = obj.getJSONObject(i);

        int chunkX = entry.getInt("x");
        int chunkY = entry.getInt("y");
        int chunkX2 = chunkX;
        int chunkY2 = chunkY;
        try { chunkX2 = entry.getInt("x2"); } catch (Exception e) {}
        try { chunkY2 = entry.getInt("y2"); } catch (Exception e) {}
        String music = entry.getString("music");

        AreaDefinition definition = new AreaDefinition(music);

        for (int x = chunkX; x <= chunkX2; x++)
          for (int y = chunkY; y <= chunkY2; y++)
            areaDefinitions[x][y] = definition;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static AreaDefinition getCurrentAreaDefinition()
  {
    int chunkX = getChunkX();
    int chunkY = getChunkY();

    // Return default chunk if out of bounds
    if (chunkX >= AreaDefinition.SIZE_X || chunkY >= AreaDefinition.SIZE_Y_ALL ||
        chunkX < 0 || chunkY < 0)
      return AreaDefinition.DEFAULT;

    return areaDefinitions[chunkX][chunkY];
  }

  public static void init() {
    loadAreaDefinitions();
    adaptStrings();

    handler_mouse = new MouseHandler();
    handler_keyboard = new KeyboardHandler();

    Applet applet = Game.getInstance().getApplet();
    applet.addMouseListener(handler_mouse);
    applet.addMouseMotionListener(handler_mouse);
    applet.addMouseWheelListener(handler_mouse);
    applet.addKeyListener(handler_keyboard);
    applet.setFocusTraversalKeysEnabled(false);

    if (Settings.DISASSEMBLE.get(Settings.currentProfile)) dumpStrings();

    // Initialize login
    init_login();

    init_extra();
    init_chat_tab_assets();

    // check if "Gender" of appearance panel should be patched
    // first is of the string to "Body" then in
    // patch_gender_hook adds text "Type"
    if (Settings.PATCH_GENDER.get(Settings.currentProfile)) {
      strings[91] = "Body";
    }
  }

  public static boolean skipToLogin() {
    if (firstTimeRunningRSCPlus) {
      return false;
    }

    boolean skipToLogin = false;

    if (Settings.noWorldsConfigured
        || (Settings.WORLDS_TO_DISPLAY == 1 && Settings.WORLD.get(Settings.currentProfile) != 0)) {
      String curWorldURL = Settings.WORLD_URLS.get(1);
      try {
        String address = InetAddress.getByName(curWorldURL).toString();
        if (address.contains("localhost") || address.contains("127.0.0.1")) {
          // no configured world or localhost only
          skipToLogin = true;
        }
      } catch (UnknownHostException e) {
        skipToLogin = true;
      }
    }

    return skipToLogin || Settings.START_LOGINSCREEN.get(Settings.currentProfile);
  }

  /**
   * Decides if the text of "to change your contact details, etc" should be moved down to make it
   * well aligned
   *
   * @return the needed alignment or none
   */
  public static int wrenchFixHook() {
    return Settings.PATCH_WRENCH_MENU_SPACING.get(Settings.currentProfile)
        ? WRENCH_ALIGNMENT_FIX
        : WRENCH_ALIGNMENT_NO_FIX;
  }

  /**
   * Reference for JClassPatcher and any other required. Indicates if should show account and
   * security settings
   *
   * @return
   */
  public static boolean showSecuritySettings() {
    return Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile);
  }

  /**
   * Method that gets called when starting game, normally would go to Welcome screen but if no world
   * configured (using RSC+ for replay mode) skip directly to login for replays
   */
  public static void resetLoginHook() {
    if (skipToLogin()) {
      login_screen = SCREEN_USERNAME_PASSWORD_LOGIN;
    }
  }

  public static void init_extra() {
    InputStream is = Launcher.getResourceAsStream("/assets/fontdata.bin");
    try {
      fontData = new byte[is.available()];
      is.read(fontData);
    } catch (IOException e) {
      Logger.Warn("Could not load font data, will not log prettified windowed server messages");
      fontData = null;
    }

    String inputFilterChars =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\u00a3$%^&*()-_=+[{]};:\'@#~,<.>/?\\| ";
    inputFilterCharFontAddr = new int[256];
    for (int code = 0; code < 256; ++code) {
      int index = inputFilterChars.indexOf(code);
      if (index == -1) {
        index = 74;
      }

      inputFilterCharFontAddr[code] = index * 9;
    }
  }

  public static void init_chat_tab_assets() {
    InputStream is = Launcher.getResourceAsStream("/assets/hbar/hbar2_retro_compat.dat");
    try {
      hbarRetroData = new byte[is.available()];
      is.read(hbarRetroData);
    } catch (IOException e) {
      Logger.Warn("Could not load old hbar2 data, will not be able to draw old chat tabs");
      hbarRetroData = null;
    }

    is = Launcher.getResourceAsStream("/assets/hbar/hbar2_orig.dat");
    try {
      hbarOrigData = new byte[is.available()];
      is.read(hbarOrigData);
    } catch (IOException e) {
      Logger.Warn("Could not load hbar2 data, will not be able to go back to the modern chat tabs");
      hbarOrigData = null;
    }
  }

  public static boolean forceDisconnect = false;
  public static boolean forceReconnect = false;

  public static boolean isUnderground() {
    return planeIndex == 3;
  }



  /**
   * An updater that runs frequently to update calculations for XP/fatigue drops, the XP bar, etc.
   *
   * <p>This updater does not handle any rendering, for rendering see {@link Renderer#present}
   */
  public static void update() {
    // FIXME: This is a hack from a rsc client update (so we can skip updating the client this time)
    version = 235;
    if (!exponent.toString().equals(JConfig.SERVER_RSA_EXPONENT))
      exponent = new BigInteger(JConfig.SERVER_RSA_EXPONENT);
    if (!modulus.toString().equals(JConfig.SERVER_RSA_MODULUS))
      modulus = new BigInteger(JConfig.SERVER_RSA_MODULUS);

    long time = System.currentTimeMillis();
    long nanoTime = System.nanoTime();

    float delta_time = (float) (nanoTime - last_time) / 1000000000.0f;
    last_time = nanoTime;

    // Handdle area data
    if (state == STATE_GAME)
    {
      AreaDefinition area = getCurrentAreaDefinition();
      MusicPlayer.playTrack(area.music);
    }
    else if (state == STATE_LOGIN)
    {
      MusicPlayer.playTrack("scape_original");
    }

    Camera.setLookatTile(getPlayerWaypointX(), getPlayerWaypointY());
    Camera.update(delta_time);

    Renderer.setClearColor(0);
    if (Settings.RS2HD_SKY.get(Settings.currentProfile)) {
      if (isUnderground()) Renderer.setClearColor(Renderer.rs2hd_color_skyunderground);
      else Renderer.setClearColor(Renderer.rs2hd_color_skyoverworld);
    } else {
      if (Settings.CUSTOM_SKYBOX_OVERWORLD_ENABLED.get(Settings.currentProfile)) {
        Renderer.setClearColor(
            Settings.CUSTOM_SKYBOX_OVERWORLD_COLOUR.get(Settings.currentProfile));
      }
      if (Settings.CUSTOM_SKYBOX_UNDERGROUND_ENABLED.get(Settings.currentProfile)) {
        if (isUnderground())
          Renderer.setClearColor(
              Settings.CUSTOM_SKYBOX_UNDERGROUND_COLOUR.get(Settings.currentProfile));
      }
    }

    if (Settings.JOYSTICK_ENABLED.get(Settings.currentProfile)) {
      JoystickHandler.poll();
    }

    Replay.update();

    if (Settings.RECORD_AUTOMATICALLY_FIRST_TIME.get(Settings.currentProfile)
        && showRecordAlwaysDialogue) {
      int response =
          JOptionPane.showConfirmDialog(
              Game.getInstance().getApplet(),
              "If you'd like, you can record your session every time you play by default.\n"
                  + "\n"
                  + "These recordings do not leave your computer unless you manually do it on purpose.\n"
                  + "They also take up negligible space. You could fit about a 6 hour session on a floppy disk, depending on what you do.\n"
                  + "\n"
                  + "Recordings can be played back later, even offline, and capture the data the server sends and that you send the server.\n"
                  + "Your password is not in the capture.\n"
                  + "\n"
                  + "Would you like to record all your play sessions by default?\n"
                  + "\n"
                  + "NOTE: This option can be toggled in the Settings interface (ctrl-o by default) under the Replay tab.",
              "rscplus",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.INFORMATION_MESSAGE,
              Launcher.icon);
      if (response == JOptionPane.YES_OPTION || response == JOptionPane.CLOSED_OPTION) {
        Settings.RECORD_AUTOMATICALLY.put(Settings.currentProfile, true);
      } else if (response == JOptionPane.NO_OPTION) {
        Settings.RECORD_AUTOMATICALLY.put(Settings.currentProfile, false);
      }
      Settings.RECORD_AUTOMATICALLY_FIRST_TIME.put(Settings.currentProfile, false);
      Settings.save();
    }

    if (state == STATE_GAME) {
      Client.getPlayerName();
      Client.adaptLoginInfo();
    }

    Game.getInstance().updateTitle();

    if (forceDisconnect) {
      Client.closeConnection(false);
      forceDisconnect = false;
    }

    if (forceReconnect) {
      Client.loseConnection(false);
      forceReconnect = false;
    }

    // Handle skipping to next replay
    if (!Replay.isPlaying && Replay.replayServer != null && Replay.replayServer.isDone) {
      if (ReplayQueue.currentIndex < ReplayQueue.queue.size()) {
        if (!ReplayQueue.skipped) {
          ReplayQueue.nextReplay();
        }
      }
      ReplayQueue.skipped = false;
    }

    // Process playback actions for replays
    Replay.processPlaybackAction();

    // Process playback queue for replays
    ReplayQueue.processPlaybackQueue();

    // Close replay, order matters on these two!
    if (runReplayCloseHook) {
      Replay.handleReplayClosing();
      runReplayCloseHook = false;
    }

    // Login hook on this thread
    if (runReplayHook && state == STATE_LOGIN) {
      Renderer.replayOption = 2;
      runReplayHook = false;
      login_hook();
    }

    WorldMapWindow.UpdateView();
    if (Client.state == Client.STATE_GAME) {
      WorldMapWindow.Update();
    } else {
      WorldMapWindow.Reset();
    }

    updates++;
    time = System.currentTimeMillis();
    if (time >= update_timer) {
      updatesPerSecond = updates;
      update_timer = time + 1000;
      updates = 0;
    }
  }

  public static void processFatigueXPDrops() {
    if (xpUsername.equals("") || !knowWhoIAm) {
      return;
    }

    // Process XP drops
    for (int skill = 0; skill < NUM_SKILLS; skill++) {

      xpGain[skill] = getXP(skill) - xpLast.get(xpUsername)[skill];
      xpLast.get(xpUsername)[skill] += xpGain[skill];

      if (xpGain[skill] > 0.0f) {
        if (Settings.SHOW_XPDROPS.get(Settings.currentProfile))
          xpdrop_handler.add(
              "+" + xpGain[skill] + " (" + skill_name[skill] + ")", Renderer.color_text);

        // XP/hr calculations

        // calc last xp gain
        lastXpGain.get(xpUsername)[skill][LAST_XP_GAIN] = new Double(xpGain[skill]);

        // calc total xp gain
        lastXpGain.get(xpUsername)[skill][TOTAL_XP_GAIN] =
            xpGain[skill] + lastXpGain.get(xpUsername)[skill][TOTAL_XP_GAIN];

        // calc xp/hr
        xpPerHour.get(xpUsername)[skill] =
            3600
                * (lastXpGain.get(xpUsername)[skill][TOTAL_XP_GAIN])
                / ((System.currentTimeMillis()
                        - lastXpGain.get(xpUsername)[skill][TIME_OF_FIRST_XP_DROP])
                    / 1000);

        // increment xp drops
        lastXpGain.get(xpUsername)[skill][TOTAL_XP_DROPS]++;

        // display xp/hr or not
        if (lastXpGain.get(xpUsername)[skill][TOTAL_XP_DROPS] > 1) {
          showXpPerHour.get(xpUsername)[skill] = true;
        }

        // update time since last xp drop
        lastXpGain.get(xpUsername)[skill][TIME_OF_LAST_XP_DROP] =
            (double) System.currentTimeMillis();

        if (skill == SKILL_HP && xpbar.current_skill != -1) continue;

        xpbar.setSkill(skill);
      }
    }

    // Process fatigue drops
    if (Settings.SHOW_FATIGUEDROPS.get(Settings.currentProfile)) {
      final float actualFatigue = getActualFatigue();
      final float fatigueGain = actualFatigue - currentFatigue;
      final int fatigueUnitsGain = fatigue - current_fatigue_units;
      String gainText = "";
      if (fatigueGain > 0.0f && !isWelcomeScreen()) {
        gainText =
            "+"
                + trimNumber(fatigueGain, Settings.FATIGUE_FIGURES.get(Settings.currentProfile))
                + "% (Fatigue)";
        if (Settings.SHOW_FATIGUEUNITS.get(Settings.currentProfile))
          gainText += (" [" + fatigueUnitsGain + " U]");
        xpdrop_handler.add(gainText, Renderer.color_fatigue);
        currentFatigue = actualFatigue;
        current_fatigue_units = fatigue;
      }
    }
    // Prevents a fatigue drop upon first login during a session
    if (isWelcomeScreen() && currentFatigue != getActualFatigue()) {
      currentFatigue = getActualFatigue();
      current_fatigue_units = fatigue;
    }
  }

  public static void resetFatigueXPDrops(boolean resetSession) {
    if (username_login.equals("")) {
      return;
    }

    xpUsername = Util.formatString(username_login, 50);
    if (lastXpGain.get(xpUsername) == null) {
      lastXpGain.put(xpUsername, new Double[NUM_SKILLS][5]);
      showXpPerHour.put(xpUsername, new Boolean[NUM_SKILLS]);
      xpPerHour.put(xpUsername, new Double[NUM_SKILLS]);
      xpLast.put(xpUsername, new Float[NUM_SKILLS]);
      for (int skill = 0; skill < NUM_SKILLS; skill++) {
        lastXpGain.get(xpUsername)[skill][TOTAL_XP_GAIN] = new Double(0);
        lastXpGain.get(xpUsername)[skill][TIME_OF_FIRST_XP_DROP] =
            lastXpGain.get(xpUsername)[skill][TIME_OF_LAST_XP_DROP] =
                new Double(System.currentTimeMillis());
        lastXpGain.get(xpUsername)[skill][TOTAL_XP_DROPS] = new Double(0);

        showXpPerHour.get(xpUsername)[skill] = false;
        xpPerHour.get(xpUsername)[skill] = new Double(0);
      }
    }
    if (xpGoals.get(xpUsername) == null) {
      xpGoals.put(xpUsername, new Integer[NUM_SKILLS]);
      lvlGoals.put(xpUsername, new Float[NUM_SKILLS]);
    }

    for (int skill = 0; skill < NUM_SKILLS; skill++) {
      xpLast.get(xpUsername)[skill] = getXP(skill);

      if (resetSession) {

        lastXpGain.get(xpUsername)[skill][TOTAL_XP_GAIN] = new Double(0);
        lastXpGain.get(xpUsername)[skill][TIME_OF_FIRST_XP_DROP] =
            lastXpGain.get(xpUsername)[skill][TIME_OF_LAST_XP_DROP] =
                (double) System.currentTimeMillis();
        lastXpGain.get(xpUsername)[skill][TOTAL_XP_DROPS] = new Double(0);
        showXpPerHour.get(xpUsername)[skill] = false;
      }
    }
  }

  public static void init_login() {
    Camera.init();
    state = STATE_LOGIN;
    isGameLoaded = false;
    Renderer.replayOption = 0;

    twitch.disconnect();

    if (skipToLogin()) {
      login_screen = SCREEN_USERNAME_PASSWORD_LOGIN;
    }

    resetLoginMessage();
    Replay.closeReplayPlayback();
    Replay.closeReplayRecording();
    adaptStrings();
    player_name = "";
  }

  public static void init_game() {
    Camera.init();
    combat_style = Settings.COMBAT_STYLE.get(Settings.currentProfile);
    state = STATE_GAME;
    // bank_active_page = 0; // TODO: config option? don't think this is very important.
    // combat_timer = 0;
  }

  public static void login_hook() {
    // Order of comparison matters here
    Replay.init(ReplayQueue.currentReplayName);
    if (Renderer.replayOption == 2) {
      if (!Replay.initializeReplayPlayback()) Renderer.replayOption = 0;
    } else if (Renderer.replayOption == 1
        || Settings.RECORD_AUTOMATICALLY.get(Settings.currentProfile)
        || Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) {
      Replay.initializeReplayRecording();
    }

    if (Settings.noWorldsConfigured
        && Settings.WORLD.get(Settings.currentProfile) != 0
        && !Replay.isPlaying) {
      closeConnection(false);
      // make sure to set to login screen here
      Client.login_screen = SCREEN_USERNAME_PASSWORD_LOGIN;
      loginMessageHandlerThread = new Thread(new LoginMessageHandler());
      loginMessageHandlerThread.start();
    }
  }

  // triggered on receiving Welcome screen; opcode 182
  public static void allTheWayLoggedIn() {
    if (Settings.FIRST_TIME.get(Settings.currentProfile)) {
      Settings.FIRST_TIME.put(Settings.currentProfile, false);
      Settings.save();
    }

    // Get keybind to open the config window so that we can tell the player how to open it
    if (Settings.REMIND_HOW_TO_OPEN_SETTINGS.get(Settings.currentProfile)) {
      String configWindowShortcut = "";
      for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
        if ("show_config_window".equals(kbs.getCommandName())) {
          configWindowShortcut = kbs.getFormattedKeybindText();
          break;
        }
      }
      if ("".equals(configWindowShortcut)) {
        Logger.Error("Could not find the keybind for the config window!");
        configWindowShortcut = "<Keybind error>";
      }

      displayMessage("@mag@Type @yel@::help@mag@ for a list of commands", CHAT_QUEST);
      displayMessage(
          "@mag@Open the settings by @yel@clicking the wrench icon@mag@, pressing @yel@"
              + configWindowShortcut
              + "@mag@, or from the @yel@tray icon",
          CHAT_QUEST);
    }

    if (TwitchIRC.isUsing()) twitch.connect();

    // Check for updates every login at most once per hour,
    // so users are notified when an update is available
    long currentTime = System.currentTimeMillis();
    if (Settings.CHECK_UPDATES.get(Settings.currentProfile) && currentTime >= updateTimer) {
      checkForUpdate(false);
      updateTimer = currentTime + (60 * 60 * 1000);
    }

    resetFatigueXPDrops(false);
  }

  public static void disconnect_hook() {
    // ::lostcon or closeConnection
    Replay.closeReplayRecording();
    Speedrun.saveAndQuitSpeedrun();
    player_name = "";
    knowWhoIAm = false;
    Client.tipOfDay = -1;
  }

  // check if login attempt is not a valid login or reconnect, send to disconnect hook
  // response 1 i don't know exactly what's for might be trying to connect in combat or something
  public static void login_attempt_hook(int response, boolean reconnecting, int[] xtea_keys) {
    // at this stage just close it
    if (response == 1 || ((response & 64) == 0)) {
      disconnect_hook();
    }
  }

  public static void error_game_hook(String s) {
    // from here its error_game_ + s -> check if its error_game_crash, thats the finalizing one that
    // interrupts
    // things
    Logger.Error("Error game reported: " + s);
    if (s.toLowerCase().equals("crash")) {
      disconnect_hook();
    }
  }

  /**
   * Called if Profile SAVE_LOGIN_INFO set, to not clear login info when selecting click here to
   * login
   */
  public static void keep_login_info_hook() {
    Client.login_screen = SCREEN_USERNAME_PASSWORD_LOGIN;
    setResponseMessage("Please enter your username and password", "");
    Panel.setFocus(Client.panelLogin, Client.loginUserInput);
  }

  /**
   * Hooks the message that hovering over X thing gives in the client
   *
   * @param tooltipMessage - the message in raw color format
   */
  public static String mouse_action_hook(String tooltipMessage) {
    MouseText.mouseText = tooltipMessage;
    MouseText.regenerateCleanedMouseTexts();

    // Remove top-left action text in extended mode
    if (Settings.SHOW_MOUSE_TOOLTIP.get(Settings.currentProfile)
        && Settings.SHOW_EXTENDED_TOOLTIP.get(Settings.currentProfile)) return "";

    return tooltipMessage;
  }

  /**
   * If Profile PATCH_GENDER is set, changes the Appearance Panel text from "Gender" to "Body Type"
   *
   * @param panelAppearance
   * @param xPos
   * @param yPos
   */
  public static void patch_gender_hook(Object panelAppearance, int xPos, int yPos) {
    if (Settings.PATCH_GENDER.get(Settings.currentProfile)) {
      Panel.addCenterTextTo(panelAppearance, xPos, yPos + 8, "Type", 1, true);
    }
  }

  /**
   * Tells the client that the adjacent region is loading, so not to do spikes in position printing
   *
   * @param isLoading - the flag for loading
   */
  public static void isLoadingHook(boolean isLoading) {
    if (worldX == -1 && worldY == -1) {
      worldX = localRegionX + regionX;
      worldY = localRegionY + regionY;
    } else {
      if (isLoading) {
        Camera.reset_lookat();
      } else {
        worldX = localRegionX + regionX;
        worldY = localRegionY + regionY;
      }
    }
  }

  /**
   * General check for existing game received hooks, useful to reroute logic Send false if has
   * finished processing
   *
   * @param opcode - Packet opcode
   * @param psize - Packet size
   * @return false to indicate no more processing is needed
   */
  public static boolean gameOpcodeReceivedHook(int opcode, int psize) {
    boolean needsProcess = true;

    if (Bank.processPacket(opcode, psize)) {
      needsProcess = false;
    }

    return needsProcess;
  }

  /**
   * General extra check for new received opcodes Send false if has finished processing
   *
   * @param opcode - Packet opcode
   * @param psize - Packet size
   * @return false to indicate no more processing is needed
   */
  public static boolean newOpcodeReceivedHook(int opcode, int psize) {
    boolean needsProcess = true;

    if (AccountManagement.processPacket(opcode, psize)) {
      needsProcess = false;
    }

    return needsProcess;
  }

  /**
   * General in game input hook for new added elements return true to indicate continue checking
   * conditions in the original gameInput() method
   */
  public static boolean gameInputHook(int n1, int mouseY, int n3, int mouseX) {
    boolean continueFlow = true;

    if (Client.showRecoveryQuestions) {
      AccountManagement.recovery_questions_input(n1, mouseY, n3, mouseX);
      continueFlow = false;
    } else if (Client.showContactDetails) {
      AccountManagement.contact_details_input(n1, mouseY, n3, mouseX);
      continueFlow = false;
    }

    return continueFlow;
  }

  public static void loginOtherButtonCheckHook() {
    AccountManagement.processForgotPassword();
  }

  /**
   * General drawGame hook for new added panels return true to indicate continue checking conditions
   * in the original drawGame() method
   */
  public static boolean drawGameHook() {
    boolean continueFlow = true;

    if (AccountManagement.pending_render()) {
      continueFlow = false;
    }

    return continueFlow;
  }

  /**
   * Extensible method hooking to draw other dialog boxes and consuming mouse method. Branching
   * should match conditions inside showTextInputDialog()
   */
  public static void drawTextInputDialogMouseHook(int mouseX, int mouseY, int mouseButtonClick) {
    if (AccountManagement.shouldShowPassChange()) {
      AccountManagement.drawChangePassInput(mouseX, mouseY, mouseButtonClick);
    } else if (XPBar.shouldShowGoalInput()) {
      XPBar.drawGoalXPInput(mouseX, mouseY, mouseButtonClick);
    }
  }

  /**
   * Extensible method hooking to consume key press for panel, should return non-zero if we need to
   * stop doing future checks when returning
   */
  public static int gameKeyPressHook(int loggedIn, int key) {
    if (loggedIn != 1) {
      return 0;
    } else if (AccountManagement.shouldConsumeKey()) {
      return AccountManagement.keyHandler(key);
    } else if (XPBar.shouldConsumeKey()) {
      return XPBar.keyHandler();
    }
    return 0;
  }

  public static int welcome_screen_size(int oldSize) {
    int newSize = oldSize;
    if (Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile)) {
      if (Client.recoveryChangeDays == 200) { // RSC235 recovery not set
        newSize -= 15;
      } else if (Client.recoveryChangeDays
          < 200) { // Between 0 and 200 (normally 0 and 13 to allow cancel recovery)
        newSize += 15;
      }
    }
    if (Settings.SHOW_SECURITY_TIP_DAY.get(Settings.currentProfile)) {
      if (Client.recoveryChangeDays == 201) { // questions set, security tip of day
        newSize += 74;
      }
    }
    return newSize;
  }

  /** Determines if client should display "Click here to close window" at welcome screen */
  public static boolean showWelcomeClickToClose() {
    boolean shouldShow = true;

    if (Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile)) {
      // In RSC235 200 is questions not set, 201 is questions set
      // RecoveryChangeDays should normally between 0 and 13 to allow cancel recovery
      if (Client.recoveryChangeDays < 200) {
        shouldShow = false;
      }
    }

    return shouldShow;
  }

  /** Determines if client should display the security tip of day at welcome screen */
  public static boolean showSecurityTipOfDay() {
    boolean shouldShow = false;

    if (Settings.SHOW_SECURITY_TIP_DAY.get(Settings.currentProfile)) {
      if (Client.recoveryChangeDays == 201) {
        shouldShow = true;
      }
    }

    return shouldShow;
  }

  /** Return true if there is pending render to show the text-input-box dialog */
  public static boolean shouldShowTextInputDialog() {
    return AccountManagement.shouldShowPassChange() || XPBar.shouldShowGoalInput();
  }

  public static void drawInputPopupHook(int popupType, String popupInput) {
    boolean needsProcess = true;

    if (AccountManagement.processInputPopup(popupType, popupInput)) {
      needsProcess = false;
    } else if (Bank.processInputPopup(popupType, popupInput)) {
      needsProcess = false;
    }
  }

  public static void resetLoginMessage() {
    setResponseMessage("Please enter your username and password", "");
  }

  /**
   * Displays a native in-game input popup such as enter the items to deposit. For popup types 9 and
   * onwards it isn't restricted to digits only but if a future popup requires it, method may need
   * tweak and hooks added.
   *
   * @return true if the call to the native showInputPopup was possible
   */
  public static boolean showNativeInputPopup(int type, String[] text, boolean hasInput) {
    boolean displayed = true;
    try {
      Reflection.showInputPopup.invoke(Client.instance, text, 12, type, hasInput);
    } catch (Exception e) {
      displayed = false;
    }
    return displayed;
  }

  /** Stores the user's display name in {@link #player_name}. */
  public static void getPlayerName() {
    try {
      String name = (String) Reflection.characterName.get(player_object);
      if (name != null) {
        if (!name.equals(player_name)) {
          player_name = name;
          Camera.reset_lookat();
        }
      }
    } catch (IllegalArgumentException | IllegalAccessException e1) {
      e1.printStackTrace();
    }
  }

  public static int getPlayerWaypointX() {
    int x = 0;
    try {
      x = (int) Reflection.characterWaypointX.get(player_object);
    } catch (Exception e) {
    }
    return x;
  }

  public static int getPlayerWaypointY() {
    int y = 0;
    try {
      y = (int) Reflection.characterWaypointY.get(player_object);
    } catch (Exception e) {
    }
    return y;
  }

  /** Returns the coordinates of the player */
  public static String getCoords() {
    return "(" + worldX + "," + worldY + ")";
  }

  public static int getChunkX()
  {
    return worldX / 48;
  }

  public static int getChunkY()
  {
    int chunkY = (worldY / 48);
    int floor = chunkY / AreaDefinition.SIZE_Y;
    chunkY = chunkY % AreaDefinition.SIZE_Y;
    if (floor == 3)
      chunkY += AreaDefinition.SIZE_Y;
    return chunkY;
  }

  /**
   * Intercepts chat messages sent by the user and parses them for commands.
   *
   * @param line a chat message sent by the user
   * @return a modified chat message
   */
  public static String processChatCommand(String line) {
    // TODO: Move Twitch related checks to their own method to stay consistent
    if (TwitchIRC.isUsing() && line.startsWith("/")) {
      String message = line.substring(1, line.length());
      String[] messageArray = message.split(" ");

      message = processClientChatCommand(message);

      if (messageArray.length > 1 && "me".equals(messageArray[0])) {
        message = message.substring(3);
        twitch.sendEmote(message, true);
      } else {
        twitch.sendMessage(message, true);
      }
      return "::null";
    }

    line = processClientChatCommand(line);
    processClientCommand(line);

    return line;
  }

  // TODO: Use processClientChatCommand instead of this method
  public static String processPrivateCommand(String line) {
    return processClientChatCommand(line);
  }

  /**
   * Parses a chat message sent by the user for client related commands.
   *
   * @param line a chat message sent by the user
   * @return a modified chat message
   */
  private static String processClientCommand(String line) {
    if (line.startsWith("::")) {
      String[] commandArray = line.substring(2, line.length()).toLowerCase().split(" ");

      switch (commandArray[0]) {
        case "togglebypassattack":
          Settings.toggleAttackAlwaysLeftClick();
          break;
        case "toggleroofs":
          Settings.toggleHideRoofs();
          break;
        case "togglecombat":
          Settings.toggleCombatMenuShown();
          break;
        case "togglecolor":
          Settings.toggleColorTerminal();
          break;
        case "togglehitbox":
          Settings.toggleShowHitbox();
          break;
        case "togglefatigue":
          Settings.toggleFatigueAlert();
          break;
        case "toggletwitch":
          Settings.toggleTwitchHide();
          break;
        case "toggleplayerinfo":
          Settings.toggleShowPlayerNameOverlay();
          break;
        case "togglefriendinfo":
          Settings.toggleShowFriendNameOverlay();
          break;
        case "togglenpcinfo":
          Settings.toggleShowNPCNameOverlay();
          break;
        case "toggleidsinfo":
          Settings.toggleExtendIdsOverlay();
          break;
        case "toggleiteminfo":
          Settings.toggleShowItemGroundOverlay();
          break;
        case "togglelogindetails":
          Settings.toggleShowLoginIpAddress();
          break;
        case "togglestartsearchedbank":
          if (commandArray.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < commandArray.length; i++) {
              if (commandArray[i].trim().equals("")) continue;
              sb.append(commandArray[i].trim().toLowerCase());
              if (i < commandArray.length - 1) sb.append(" ");
            }
            Settings.toggleStartSearchedBank(sb.toString(), true);
          } else {
            Settings.toggleStartSearchedBank("", false);
          }

          break;
        case "banksearch":
          // enters searchable bank mode, to return normal mode player has to speak to the banker
          // again
          if (commandArray.length > 1) {
            Bank.search(commandArray, false);
          } else {
            Bank.search(commandArray, true);
          }
          break;
        case "sleep":
          Client.sleep();
          break;
        case "screenshot":
          Renderer.takeScreenshot(false);
          break;
        case "debug":
          Settings.toggleDebug();
          break;
        case "togglexpdrops":
          Settings.toggleXpDrops();
          break;
        case "togglefatiguedrops":
          Settings.toggleFatigueDrops();
          break;
        case "fov":
          if (commandArray.length > 1) {
            Settings.setClientFoV(commandArray[1]);
          }
          break;
        case "logout":
          Client.logout();
          break;
        case "toggleposition":
          Settings.togglePosition();
          break;
        case "toggleretrofps":
          Settings.toggleRetroFPS();
          break;
        case "toggleinvcount":
          Settings.toggleInvCount();
          break;
        case "togglebuffs":
          Settings.toggleBuffs();
          break;
        case "togglestatusdisplay":
          Settings.toggleHpPrayerFatigueOverlay();
          break;
        case "help":
          try {
            Help.help(Integer.parseInt(commandArray[2]), commandArray[1]);
          } catch (Exception e) {
            Help.help(0, "help");
          }
          break;
        case "endrun":
          Settings.endSpeedrun();
          break;
        case "cancelrecoveryrequest":
          AccountManagement.sendCancelRecoveryChange();
          break;
        case "set_pitch":
          try {
            Camera.pitch_rscplus = Integer.parseInt(commandArray[1]);
            if (Camera.pitch_rscplus < 0) Camera.pitch_rscplus = 0;
            if (Camera.pitch_rscplus > 1023) Camera.pitch_rscplus = 1023;
          } catch (ArrayIndexOutOfBoundsException ex) {
            displayMessage(
                "You must specify a number to set the pitch to. 112 is default.", CHAT_QUEST);
          } catch (NumberFormatException ex) {
            displayMessage("That is not a number.", CHAT_QUEST);
          }
          break;
        case "set_height":
          try {
            Camera.offset_height = Integer.parseInt(commandArray[1]);
          } catch (ArrayIndexOutOfBoundsException ex) {
            displayMessage(
                "You must specify a number to set the height offset to. 0 is default.", CHAT_QUEST);
          } catch (NumberFormatException ex) {
            displayMessage("That is not a number.", CHAT_QUEST);
          }
          break;
        default:
          if (commandArray[0] != null) {
            return "::";
          }
          break;
      }
    }

    return line;
  }

  /**
   * Parses a chat message sent by the user for chat related commands.
   *
   * @param line a chat message sent by the user
   * @return a modified chat message
   */
  private static String processClientChatCommand(String line) {
    if (line.startsWith("::")) {
      String command = line.substring(2, line.length()).toLowerCase();

      if ("total".equals(command)) {
        return "My Total Level is " + getTotalLevel() + " (" + getTotalXP() + " xp).";
      } else if ("fatigue".equals(command)) {
        return "My Fatigue is at " + currentFatigue + "%.";
      } else if ("cmb".equals(command)) {
        // this command breaks character limits and might be bannable... would not recommend sending
        // this command over PM to rs2/rs3
        return "@whi@My Combat is Level "
            + "@gre@"
            +
            // basic melee stats
            ((base_level[SKILL_ATTACK]
                        + base_level[SKILL_STRENGTH]
                        + base_level[SKILL_DEFENSE]
                        + base_level[SKILL_HP])
                    * 0.25
                // add ranged levels if ranger
                + ((base_level[SKILL_ATTACK] + base_level[SKILL_STRENGTH])
                        < base_level[SKILL_RANGED] * 1.5
                    ? base_level[SKILL_RANGED] * 0.25
                    : 0)
                // prayer and mage
                + (base_level[SKILL_PRAYER] + base_level[SKILL_MAGIC]) * 0.125)
            + " @lre@A:@whi@ "
            + base_level[SKILL_ATTACK]
            + " @lre@S:@whi@ "
            + base_level[SKILL_STRENGTH]
            + " @lre@D:@whi@ "
            + base_level[SKILL_DEFENSE]
            + " @lre@H:@whi@ "
            + base_level[SKILL_HP]
            + " @lre@R:@whi@ "
            + base_level[SKILL_RANGED]
            + " @lre@P:@whi@ "
            + base_level[SKILL_PRAYER]
            + " @lre@M:@whi@ "
            + base_level[SKILL_MAGIC];
      } else if ("cmbnocolor"
          .equals(command)) { // this command stays within character limits and is safe.
        return "My Combat is Level "
            // basic melee stats
            + ((base_level[SKILL_ATTACK]
                        + base_level[SKILL_STRENGTH]
                        + base_level[SKILL_DEFENSE]
                        + base_level[SKILL_HP])
                    * 0.25
                // add ranged levels if ranger
                + ((base_level[SKILL_ATTACK] + base_level[SKILL_STRENGTH])
                        < base_level[SKILL_RANGED] * 1.5
                    ? base_level[SKILL_RANGED] * 0.25
                    : 0)
                // prayer and mage
                + (base_level[SKILL_PRAYER] + base_level[SKILL_MAGIC]) * 0.125)
            + " A:"
            + base_level[SKILL_ATTACK]
            + " S:"
            + base_level[SKILL_STRENGTH]
            + " D:"
            + base_level[SKILL_DEFENSE]
            + " H:"
            + base_level[SKILL_HP]
            + " R:"
            + base_level[SKILL_RANGED]
            + " P:"
            + base_level[SKILL_PRAYER]
            + " M:"
            + base_level[SKILL_MAGIC];
      } else if ("bank".equals(command)) {
        return "Hey, everyone, I just tried to do something very silly!";
      } else if ("update".equals(command)) {
        checkForUpdate(true);
      } else if (command.startsWith("xmas ")) {
        int randomStart = (int) System.currentTimeMillis();
        if (randomStart < 0) {
          randomStart *= -1; // casting to long to int sometimes results in a negative number
        }
        String subline = "";
        String[] colours = {"@red@", "@whi@", "@gre@", "@whi@"};
        int spaceCounter = 0;
        for (int i = 0; i < line.length() - 7; i++) {
          if (" ".equals(line.substring(7 + i, 8 + i))) {
            spaceCounter += 1;
          }
          subline += colours[(i - spaceCounter + randomStart) % 4];
          subline += line.substring(7 + i, 8 + i);
        }
        return subline;
      } else if (command.startsWith("rainbow ")) { // @red@A@ora@B@yel@C etc
        int randomStart = (int) System.currentTimeMillis();
        if (randomStart < 0) {
          randomStart *= -1; // casting to long to int sometimes results in a negative number
        }
        String subline = "";
        String[] colours = {"@red@", "@ora@", "@yel@", "@gre@", "@cya@", "@mag@"};
        int spaceCounter = 0;
        for (int i = 0; i < line.length() - 10; i++) {
          if (" ".equals(line.substring(10 + i, 11 + i))) {
            spaceCounter += 1;
          }
          subline += colours[(i - spaceCounter + randomStart) % 6];
          subline += line.substring(10 + i, 11 + i);
        }
        return subline;

      } else if (command.startsWith("next_")) {
        for (int skill = 0; skill < NUM_SKILLS; skill++) {
          if (command.equals("next_" + skill_name[skill].toLowerCase())) {
            final float neededXp =
                base_level[skill] == 99 ? 0 : getXPforLevel(base_level[skill] + 1) - getXP(skill);
            return "I need "
                + neededXp
                + " more xp for "
                + (base_level[skill] == 99 ? 99 : base_level[skill] + 1)
                + " "
                + skill_name[skill]
                + ".";
          }
        }
      }

      for (int skill = 0; skill < NUM_SKILLS; skill++) {
        if (command.equalsIgnoreCase(skill_name[skill]))
          return "My "
              + skill_name[skill]
              + " level is "
              + base_level[skill]
              + " ("
              + getXP(skill)
              + " xp).";
      }
    }

    return line;
  }

  /**
   * Prints a client-side message in chat.
   *
   * @param message a message to print
   * @param chat_type the type of message to send
   */
  public static synchronized void displayMessage(String message, int chat_type) {
    if (Client.state != Client.STATE_GAME || Reflection.displayMessage == null) return;

    try {
      Reflection.displayMessage.invoke(
          Client.instance, false, null, 0, message, chat_type, 0, null, null);
    } catch (Exception e) {
    }
  }

  /**
   * Sets the client text response status. In the login screen this is the information shown above
   * the login controls In the register and recover screens is the replacement of control text in
   * the respective panels
   *
   * @param line1 the bottom line of text
   * @param line2 the top part of text
   */
  public static void setResponseMessage(String line1, String line2) {
    if (Reflection.setLoginText == null) return;

    try {
      if (Client.login_screen == Client.SCREEN_USERNAME_PASSWORD_LOGIN) {
        if (line1 == null || line1.length() == 0) {
          Panel.setControlText(Client.panelLogin, Client.controlLoginTop, "");
          Panel.setControlText(Client.panelLogin, Client.controlLoginBottom, line2);
        } else {
          Reflection.setLoginText.invoke(Client.instance, (byte) -49, line2, line1);
        }
      } else if (Client.login_screen == Client.SCREEN_PASSWORD_RECOVERY) {
        Panel.setControlText(Client.panelRecovery, Client.controlRecoveryTop, line2);
        Panel.setControlText(Client.panelRecovery, Client.controlRecoveryBottom, line1);
      } else if (Client.login_screen == Client.SCREEN_REGISTER_NEW_ACCOUNT) {
        Panel.setControlText(Client.panelRegister, Client.controlRegister, line2 + " " + line1);
      }
    } catch (Exception e) {
    }
  }

  /**
   * Sets the account type needed in the welcome screen, based on the server type 0 = Free 1 =
   * Members 2 = Free (Veterans) 3 = Members (Veterans)
   */
  public static void setServertype(int servertype, boolean switchingWorld) {
    try {
      if ((servertype & 1) != 0) { // members
        Client.members = true;
        if ((servertype & 2) != 0) { // members + veterans
          // "You need a veteran Classic members account to use this server"
          Panel.setControlText(Client.panelWelcome, Client.controlServerType, strings[233]);
          Client.veterans = true;
        } else {
          // "You need a members account to use this server"
          Panel.setControlText(Client.panelWelcome, Client.controlServerType, strings[230]);
          Client.veterans = false;
        }
      } else { // free
        Client.members = false;
        if ((servertype & 2) != 0) { // free + veterans
          // "You need a veteran Classic account to use this server"
          Panel.setControlText(Client.panelWelcome, Client.controlServerType, strings[238]);
          Client.veterans = true;
        } else {
          // "You need an account to use this server"
          Panel.setControlText(
              Client.panelWelcome,
              Client.controlServerType,
              "You need an account to use this server");
          Client.veterans = false;
        }
      }
      if (switchingWorld) {
        Client.worldMembers = Client.members;
        Client.worldVeterans = Client.veterans;
      }
    } catch (Exception e) {
    }
  }

  public static void softReloadCache(boolean members) {
    try {
      Reflection.memberMapPack.set(Client.worldInstance, null);
      Reflection.memberLandscapePack.set(Client.worldInstance, null);

      Reflection.loadGameConfig.invoke(Client.instance, false);
      Reflection.loadEntities.invoke(Client.instance, true);
      Reflection.loadMaps.invoke(Client.instance, 5359);

      Object soundBuf = Reflection.soundBuffer.get(Client.instance);
      if (soundSub == null && soundBuf != null) {
        // keep reference of buffer
        soundSub = soundBuf;
      }

      if (members) {
        // sound loading routine not done
        if (soundSub == null && soundBuf == null) {
          Reflection.loadSounds.invoke(Client.instance, -90);
        }
        // from free to memb and loading routine already done sometime before
        // is just needed to load the sound data and setting back the buffer
        else if (soundBuf == null) {
          // strings[345] = "Sound effects"
          byte[] soundData =
              (byte[]) Reflection.loadDataFile.invoke(Client.instance, strings[345], 90, 10, 66);
          Reflection.memberSoundPack.set(Client.instance, soundData);
          Reflection.soundBuffer.set(Client.instance, soundSub);
        }
      } else {
        // nullify the sound buffer disallow playing sound
        Reflection.soundBuffer.set(Client.instance, null);
        // and sound data to save some memory
        Reflection.memberSoundPack.set(Client.instance, null);
      }
      if (lastHeightOffset == planeIndex) {
        // force re-render of game world terrain
        lastHeightOffset = (planeIndex + 1) % 2;
      }
    } catch (Exception e) {
    }
  }

  public static void closeConnection(boolean sendPacket31) {
    if (Reflection.closeConnection == null) return;

    try {
      Reflection.closeConnection.invoke(Client.instance, sendPacket31, 31);
    } catch (Exception e) {
    }
  }

  public static void loseConnection(boolean close) {
    if (Reflection.loseConnection == null) return;

    try {
      Reflection.loseConnection.invoke(Client.clientStream, close);
    } catch (Exception e) {
    }
  }

  public static void setInactivityTimer(int val) {
    try {
      Reflection.lastMouseAction.set(Client.instance, val);
    } catch (Exception e) {
    }
  }

  /** Send over the instruction of sleep, if player has sleeping bag with them */
  public static void sleep() {
    if (Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) return;
    if (Reflection.itemClick == null) return;

    try {
      int idx = -1;
      // inventory_items contains ids of items
      for (int n = 0; n < max_inventory; n++) {
        // id of sleeping bag
        if (inventory_items[n] == 1263) {
          idx = n;
          break;
        }
      }
      if (idx != -1 && !Client.isInterfaceOpen() && !Client.isInCombat()) {
        // method to sleep here
        sleepCmdSent = true;
        sleepBagIdx = idx;
        Reflection.itemClick.invoke(Client.instance, false, 0);
      }
    } catch (Exception e) {

    }
  }

  /**
   * Switches direction of live game to replay. True - to change to replay. False - to change to
   * live
   */
  public static void switchLiveToReplay(boolean direction) {
    int currentType = ((Client.veterans ? 1 : 0) << 1) + (Client.members ? 1 : 0);
    int servertype;
    if (direction) {
      servertype = ((Replay.replayVeterans ? 1 : 0) << 1) + (Replay.replayMembers ? 1 : 0);
    } else {
      servertype = ((Client.worldVeterans ? 1 : 0) << 1) + (Client.worldMembers ? 1 : 0);
    }

    if (currentType != servertype) {
      Client.setServertype(servertype, !direction);
      if ((servertype & 1) != (currentType & 1)) {
        Client.softReloadCache((servertype & 1) != 0);
      }
    }
  }

  /**
   * Logs the user in
   *
   * @param reconnecting - is user reconnecting
   * @param user
   * @param pass
   */
  public static void login(boolean reconnecting, String user, String pass) {
    if (Reflection.login == null) return;

    try {
      Client.autologin_timeout = 2;
      Reflection.login.invoke(Client.instance, -12, pass, user, reconnecting);
    } catch (Exception e) {
    }
  }

  /** Logs the user out of the game. */
  public static void logout() {
    if (Reflection.logout == null) return;

    try {
      Reflection.logout.invoke(Client.instance, 0);
    } catch (Exception e) {
    }
  }

  /** Gets a parameter defined from world config */
  public static String getParameter(String parameter) {
    if (Reflection.getParameter == null) return null;
    String result = null;

    try {
      result = ((String) Reflection.getParameter.invoke(Client.instance, parameter));
    } catch (Exception e) {
    }
    return result;
  }

  public static void clearScreen() {
    if (Reflection.clearScreen == null) return;

    try {
      Reflection.clearScreen.invoke(Renderer.instance, true);
    } catch (Exception e) {
    }
  }

  public static void setInterlace(boolean value) {
    if (Reflection.interlace == null) return;

    try {
      Reflection.interlace.set(Renderer.instance, value);
    } catch (Exception e) {
    }
  }

  public static boolean getInterlace() {
    try {
      return (boolean) Reflection.interlace.get(Renderer.instance);
    } catch (Exception e) {
    }
    return false;
  }

  public static void drawGraphics() {
    if (Reflection.drawGraphics == null) return;

    try {
      Reflection.drawGraphics.invoke(Renderer.instance, Renderer.graphicsInstance, 0, 256, 0);
    } catch (Exception e) {
    }
  }

  public static void preGameDisplay() {
    if (Reflection.preGameDisplay == null) return;

    try {
      Reflection.preGameDisplay.invoke(Client.instance, 2540);
    } catch (Exception e) {
    }
  }

  public static void resetTimings() {
    if (Reflection.resetTimings == null) return;

    try {
      Reflection.resetTimings.invoke(Client.instance, -28492);
    } catch (Exception e) {
    }
  }

  /**
   * Fetches the value of {@link Settings#VERSION_NUMBER} in the master branch on GitHub.
   *
   * <p>Used to check the newest version of the client.
   *
   * @return the current version number
   */
  public static Double fetchLatestVersionNumber() {
    try {
      Double currentVersion = 0.0;
      URL updateURL =
          new URL(
              "https://raw.githubusercontent.com/RSCPlus/rscplus/master/src/Client/Settings.java");

      // Open connection
      URLConnection connection = updateURL.openConnection();
      connection.setConnectTimeout(3000);
      connection.setReadTimeout(3000);
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      // in our current client version, we are looking at the source file of Settings.java in the
      // main repository
      // in order to parse what the current version number is.
      String line;
      while ((line = in.readLine()) != null) {
        if (line.contains("VERSION_NUMBER")) {
          currentVersion =
              Double.parseDouble(line.substring(line.indexOf('=') + 1, line.indexOf(';')));
          Logger.Info(String.format("@|green Current Version: %f|@", currentVersion));
          break;
        }
      }

      // Close connection
      in.close();
      return currentVersion;
    } catch (Exception e) {
      displayMessage("@dre@Error checking latest version", 0);
      return Settings.VERSION_NUMBER;
    }
  }

  /**
   * Compares the local value of {@link Settings#VERSION_NUMBER} to the value on the GitHub master
   * branch.
   *
   * <p>Used to check if there is a newer version of the client available.
   *
   * @param announceIfUpToDate if a message should be displayed in chat if the client is up-to-date
   */
  public static void checkForUpdate(boolean announceIfUpToDate) {
    double latestVersion = fetchLatestVersionNumber();
    if (latestVersion > Settings.VERSION_NUMBER) {
      displayMessage("@gre@A new version of RSC+ is available!", CHAT_QUEST);
      // TODO: before Y10K update this to %9.6f
      displayMessage(
          "The latest version is @gre@" + String.format("%8.6f", latestVersion), CHAT_QUEST);
      displayMessage(
          "~034~ Your version is @red@" + String.format("%8.6f", Settings.VERSION_NUMBER),
          CHAT_QUEST);
      if (Settings.CHECK_UPDATES.get(Settings.currentProfile)) {
        displayMessage(
            "~034~ You will receive the update next time you restart rscplus", CHAT_QUEST);
      }
    } else if (announceIfUpToDate) {
      displayMessage(
          "You're up to date: @gre@" + String.format("%8.6f", latestVersion), CHAT_QUEST);
    }
  }

  // hook to display retro fps on the client, early 2001 style
  public static void drawNativeTextHook(Object surfaceInstance) {
    if (surfaceInstance != null) {
      if (Settings.SHOW_RETRO_FPS.get(Settings.currentProfile)) {
        int offset = 0;
        if (Client.is_in_wild) offset = 70;
        try {
          Reflection.drawString.invoke(
              surfaceInstance,
              "Fps: " + Renderer.fps,
              Renderer.width - 62 - offset,
              Renderer.height - 19,
              0xffff00,
              false,
              1);
        } catch (Exception e) {
        }
      }
    }
  }

  public static void initCreateExtraPanelsHook() {
    AccountManagement.create_account_recovery();
    AccountManagement.create_recovery_questions();
    AccountManagement.create_contact_details();
  }

  public static int attack_menu_hook(int cmpVar) {
    if (Settings.ATTACK_ALWAYS_LEFT_CLICK.get(Settings.currentProfile)
        && !Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) {
      return 10;
    } else {
      return cmpVar;
    }
  }

  public static String appendDetailsHook(int id, int dir, int x, int y, int type) {
    int fullX = x + regionX;
    int fullY = y + regionY;
    MouseText.lastObjectId = id;

    // TODO:
    // MouseText.lastObjectType = type;
    if (type == dir) {
      MouseText.lastObjectType = MouseText.BOUNDARY;
    } else if (type == y) {
      MouseText.lastObjectType = MouseText.SCENERY;
    }

    if (Settings.TRACE_OBJECT_INFO.get(Settings.currentProfile)) {
      return " @gre@(" + id + ";" + dir + ";" + fullX + "," + fullY + ")";
    } else {
      return "";
    }
  }

  /**
   * Index fix after menu swap of redrawMenuHook
   *
   * @param menuindex - the index of the menu
   * @return the fixed index
   */
  public static int swapUseMenuHook(int menuindex) {
    int newmenuindex = menuindex;
    if (newmenuindex == 635) {
      newmenuindex = 650;
    } else {
    }
    return newmenuindex;
  }

  /**
   * Redraw right click menu add item hook, only interested from the portion of items with special
   * commands 640 or none
   *
   * @param instance - the instance of common menu
   * @param n - some index sent over
   * @param index - the item index
   * @param itemCommand - item command
   * @param itemName - item name
   */
  public static void redrawMenuHook(
      Object instance, int n, int index, String itemCommand, String itemName) {

    if (instance != null) {
      try {
        // Client.strings[34] - @lre@
        if (!itemCommand.equals("")) {
          if (!Item.shouldPatch(index)) {
            // Edible item command
            Reflection.menuGen.invoke(
                instance, n, 640, false, itemCommand, Client.strings[34] + itemName);
            // Use
            Reflection.menuGen.invoke(
                instance, n, 650, false, Client.strings[71], Client.strings[34] + itemName);
          } else {
            // 635 is a synonym for 650 "Use", its lower than 640 since otherwise won't do the swap
            // Use
            Reflection.menuGen.invoke(
                instance, n, 635, false, Client.strings[71], Client.strings[34] + itemName);
            // Edible item command
            Reflection.menuGen.invoke(
                instance, n, 640, false, itemCommand, Client.strings[34] + itemName);
          }
        } else {
          // Use
          Reflection.menuGen.invoke(
              instance, n, 650, false, Client.strings[71], Client.strings[34] + itemName);
        }
        // Drop
        Reflection.menuGen.invoke(
            instance, n, 660, false, Client.strings[67], Client.strings[34] + itemName);
        // Examine
        Reflection.menuGen.invoke(
            instance, index, 3600, false, Client.strings[51], Client.strings[34] + itemName);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public static void gameClickHook(Integer lastMenuId, int idxOrDeltaX, int idxOrDeltaY) {
    ItemAction action = itemActionMap.get(lastMenuId);
    Object res;
    int mx, my;
    if (action != null) {
      if (action.containsWorldPoint == 1) {
        Client.displayMenuAction(action.name, idxOrDeltaX + regionX, idxOrDeltaY + regionY);
      } else if (action.containsWorldPoint == 2) {
        if (Reflection.getNpc == null) return;
        try {
          res = Reflection.getNpc.invoke(Client.instance, idxOrDeltaX, (byte) -123);
          mx = (Reflection.characterX.getInt(res) - 64) / Client.tileSize;
          my = (Reflection.characterY.getInt(res) - 64) / Client.tileSize;
          Client.displayMenuAction(action.name, mx + regionX, my + regionY);
        } catch (Exception e) {
        }
      } else if (action.containsWorldPoint == 3) {
        if (Reflection.getPlayer == null) return;
        try {
          res = Reflection.getPlayer.invoke(Client.instance, idxOrDeltaX, 220);
          mx = (Reflection.characterX.getInt(res) - 64) / Client.tileSize;
          my = (Reflection.characterY.getInt(res) - 64) / Client.tileSize;
          Client.displayMenuAction(action.name, mx + regionX, my + regionY);
        } catch (Exception e) {
        }
      }
    }
  }

  public static void walkSourceHook(int deltaX, int deltaY) {
    Client.displayWalkToSource(deltaX + regionX, deltaY + regionY);
  }

  public static void displayMenuAction(String action, int positionX, int positionY) {
    Client.printAndShowActionString(action + " @ (" + positionX + "," + positionY + ")");
  }

  public static void displayMenuAction(String action, int serverId) {
    Client.printAndShowActionString(action + " ID: " + serverId);
  }

  public static void displayWalkToSource(int positionX, int positionY) {
    Client.printAndShowActionString("WALK_TO_SOURCE @ (" + positionX + "," + positionY + ")");
  }

  public static void printAndShowActionString(String actionString) {
    if (Settings.SHOW_LAST_MENU_ACTION.get(Settings.currentProfile)) {
      menu_timer = System.currentTimeMillis() + 3500L;
      lastAction = actionString;
      Logger.Info(actionString);
    }
  }

  // combat packet received (testing only, the info is taken on function that draws hits)
  public static void inCombatHook(
      int damageTaken, int currentHealth, int maxHealth, int index, int hooknum, Object obj) {
    // discard if info seems to be for local player
    int n1, n2;
    String name;
    // object was found
    if (obj != null) {
      try {
        n1 = Reflection.attackingPlayerIdx.getInt(obj);
        n2 = Reflection.attackingNpcIdx.getInt(obj);
        name = (String) Reflection.characterDisplayName.get(obj);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        // TODO Auto-generated catch block
      }
    }
  }

  public static Component getAndSetSoundGameContainer(Component gameContainer) {
    if (Client.gameContainer == null) {
      Client.gameContainer = gameContainer;
    }
    return (Component) Client.gameContainer;
  }

  /**
   * Clones over to memory index.dat
   *
   * @param data
   */
  public static void cloneMediaIndex(byte[] data) {
    indexData = data.clone();
  }

  /**
   * This method determines if the old chat tabs (without report abuse button) can/should be shown
   * To hot load changes use checkChatTabs(). Requires asset file hbar2_retro_compat.dat
   *
   * @param init - to specify if should also initialize first time value
   * @return true when wanting to display the old chat tabs.
   */
  public static boolean drawOldChatTabs(boolean init) {
    boolean shouldDraw =
        Settings.REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(Settings.currentProfile)
            && hbarRetroData != null;
    if (init) {
      usingRetroTabs = shouldDraw;
    }
    return shouldDraw;
  }

  /**
   * This method returns over the old chat tabs data array. Checked at mudclient's loadMedia()
   * Requires asset file hbar2_retro_compat.dat
   *
   * @return
   */
  public static byte[] readDataOldChatTabs() {
    return hbarRetroData;
  }

  /**
   * This method checks if the hbar2 file should be changed (per settings) before rendering the chat
   * tabs
   */
  public static void checkChatTabs() {
    boolean shouldDrawOldTabs = drawOldChatTabs(false);
    if (!usingRetroTabs && shouldDrawOldTabs) {
      try {
        Reflection.parseSprite.invoke(
            Renderer.instance,
            Renderer.sprite_media + 23,
            1,
            Client.hbarRetroData,
            104,
            Client.indexData);
        usingRetroTabs = true;
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      }
    } else if (usingRetroTabs && !shouldDrawOldTabs && hbarOrigData != null) {
      try {
        Reflection.parseSprite.invoke(
            Renderer.instance,
            Renderer.sprite_media + 23,
            1,
            Client.hbarOrigData,
            104,
            Client.indexData);
        usingRetroTabs = false;
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      }
    }
  }

  /**
   * This method skips drawing original chat tabs to make room to display alternative one. PREFER
   * THE USE OF drawOldChatTabs() IF JUST WANTING TO HAVE OVER PRE-REPORT ABUSE TABS
   *
   * @return true if alternative chat tabs have already have been displayed
   */
  public static boolean displayAltTabsHook() {
    return false;
  }

  /**
   * This method hides the report abuse text on chat tabs
   *
   * @return true if something else has been drawn or left empty
   */
  public static boolean hideReportAbuseHook() {
    return Settings.REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(Settings.currentProfile);
  }

  /**
   * This method allows client to skip having default report abuse tab click behavior and instead
   * have some custom one defined
   *
   * @return true when it is desired to not have the default report abuse tab click behavior
   */
  public static boolean skipActionReportAbuseTabHook() {
    return Settings.REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(Settings.currentProfile)
        || wikiLookupReplacesReportAbuse();
  }

  /**
   * This method hooks all options received and adds them to console
   *
   * @param menuOptions The options received from server
   * @param count The count for the options
   */
  public static void receivedOptionsHook(String[] menuOptions, int count) {
    if (Settings.PARSE_OPCODES.get(Settings.currentProfile)
        && (Replay.isPlaying || Replay.isSeeking || Replay.isRestarting)) return;

    Client.printReceivedOptions(menuOptions, count);
  }

  public static void printReceivedOptions(String[] menuOptions, int count) {
    int type = CHAT_INCOMING_OPTION;

    String option = "";
    Client.menuOptions = menuOptions;
    for (int i = 0; i < count; i++) {
      option = menuOptions[i];

      String originalLog = "(" + formatChatType(type) + ") " + option;
      String colorizedLog =
          "@|white (" + formatChatType(type) + ")|@ " + colorizeMessage(option, type);
      Logger.Chat(colorizedLog, originalLog);
    }
  }

  /**
   * This method hooks into selected option from incoming options and adds the selection to console
   *
   * @param possibleOptions The possible options that the user saw from server
   * @param selection The chosen option
   */
  public static void selectedOptionHook(String[] possibleOptions, int selection) {
    // Do not run anything below here while seeking or playing as is handled separately
    if (Replay.isPlaying || Replay.isSeeking || Replay.isRestarting) return;

    Client.printSelectedOption(possibleOptions, selection);
  }

  public static void printSelectedOption(String[] possibleOptions, int selection) {
    int type = CHAT_CHOSEN_OPTION;
    if (selection < 0) return;

    int select =
        (KeyboardHandler.dialogue_option == -1) ? selection : KeyboardHandler.dialogue_option;
    String option = possibleOptions[select];

    String originalLog = "(" + formatChatType(type) + ") " + option;
    String colorizedLog =
        "@|white (" + formatChatType(type) + ")|@ " + colorizeMessage(option, type);
    Logger.Chat(colorizedLog, originalLog);
  }

  public static void serverMessageHook(String message) {
    int type = CHAT_WINDOWED_MSG;
    if (!message.equals(lastServerMessage) && !message.trim().equals("")) {
      lastServerMessage = message;
      String originalLog = "(" + formatChatType(type) + ") " + lastServerMessage;
      String colorizedLog =
          "@|white (" + formatChatType(type) + ")|@ " + colorizeMessage(lastServerMessage, type);
      Logger.Chat(colorizedLog, originalLog);
      if (fontData != null) windowedForm(lastServerMessage);
    }
  }

  // original games client method to determine how to format server message into lines
  // excludes centering as it is placed into simple text
  private static void windowedForm(String str) {
    int type = CHAT_WINDOWED_MSG;
    int wrapWidth = 360;
    boolean newLineOnPercent = true;

    try {
      int width = 0;

      int lastLineTerm = 0;
      int lastBreak = 0;
      String originalLog, colorizedLog;
      String stringLine;

      for (int i = 0; str.length() > i; ++i) {
        if (str.charAt(i) == '@' && 4 + i < str.length() && str.charAt(i + 4) == '@') {
          i += 4;
        } else if (str.charAt(i) == '~' && str.length() > 4 + i && str.charAt(4 + i) == '~') {
          i += 4;
        } else {
          char c = str.charAt(i);
          if (c < 0 || c >= inputFilterCharFontAddr.length) {
            c = ' ';
          }

          width += fontData[7 + inputFilterCharFontAddr[c]];
        }

        if (str.charAt(i) == ' ') {
          lastBreak = i;
        }

        if (str.charAt(i) == '%' && newLineOnPercent) {
          width = 1000;
          lastBreak = i;
        }

        if (width > wrapWidth) {
          if (lastBreak <= lastLineTerm) {
            lastBreak = i;
          }

          width = 0;
          stringLine = str.substring(lastLineTerm, lastBreak);
          originalLog = "(" + formatChatType(type) + ") " + stringLine;
          colorizedLog =
              "@|white (" + formatChatType(type) + ")|@ " + colorizeMessage(stringLine, type);
          Logger.Chat(colorizedLog, originalLog);
          lastLineTerm = i = 1 + lastBreak;
        }
      }

      if (width > 0) {
        stringLine = str.substring(lastLineTerm);
        originalLog = "(" + formatChatType(type) + ") " + stringLine;
        colorizedLog =
            "@|white (" + formatChatType(type) + ")|@ " + colorizeMessage(stringLine, type);
        Logger.Chat(colorizedLog, originalLog);
      }
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }

  /**
   * This method hooks all chat messages.
   *
   * @param username the username that the message originated from
   * @param message the content of the message
   * @param type the type of message being displayed
   */
  public static void messageHook(
      String username, String message, int type, String colorCodeOverride) {

    // notify if the user set the message as one they wanted to be alerted by
    if (Renderer.stringIsWithinList(message, Settings.IMPORTANT_MESSAGES.get("custom"))) {
      NotificationsHandler.notify(NotifType.IMPORTANT_MESSAGE, "Important message", message);
    }
    if (Renderer.stringIsWithinList(message, Settings.IMPORTANT_SAD_MESSAGES.get("custom"))) {
      NotificationsHandler.notify(NotifType.IMPORTANT_MESSAGE, "Important message", message, "sad");
    }

    if (colorCodeOverride != null) {
      if (!((type == CHAT_QUEST || type == CHAT_CHAT) && colorCodeOverride.equals("@yel@")))
        message = colorCodeOverride + message;
    }

    // Close dialogues when player says something in-game in quest chat
    if (Replay.isPlaying) {
      if (username != null && username.equals(Client.player_name) && type == CHAT_QUEST) {
        Replay.closeDialogue = true;
      }
    }

    if (username != null)
      // Prevents non-breaking space in colored usernames appearing as an accented 'a' in console
      username = username.replace("\u00A0", " ");
    if (message != null)
      // Prevents non-breaking space in colored usernames appearing as an accented 'a' in console
      message = message.replace("\u00A0", " ");

    if (message != null && username != null) {
      Speedrun.checkMessageCompletions(message);
    }

    if (type == CHAT_NONE) {
      if (username == null && message != null) {
        if (message.contains("The spell fails! You may try again in 20 seconds"))
          magic_timer = Renderer.time + 21000L;
        else if (Settings.TRAY_NOTIFS.get(Settings.currentProfile)
            && message.contains(
                "You have been standing here for 5 mins! Please move to a new area")) {
          NotificationsHandler.notify(
              NotifType.LOGOUT, "Logout Notification", "You're about to log out");
        }
        // while the message is really You @gr2@are @gr1@poisioned! @gr2@You @gr3@lose @gr2@3
        // @gr1@health.
        // it can be known looking for "poisioned!"
        else if (message.contains("poisioned!")) {
          is_poisoned = true;
          poison_timer = Renderer.time + 21000L;
        } else if (message.contains("You drink") && message.contains("poison")) {
          is_poisoned = false;
          poison_timer = Renderer.time;
        } else if (message.contains("You retain your skills. Your objects land where you died")
            && is_poisoned) {
          is_poisoned = false;
          poison_timer = Renderer.time;
        }
      }
    } else if (type == CHAT_PRIVATE) {
      NotificationsHandler.notify(NotifType.PM, "PM from " + username, message);
    } else if (type == CHAT_TRADE_REQUEST_RECEIVED) {
      // as far as I know, this chat type is only used when receiving a trade request.
      // (message == "") is true for trade notifications... could be used if
      // CHAT_TRADE_REQUEST_RECEIVED is used for anything else
      NotificationsHandler.notify(
          NotifType.TRADE, "Trade Request", username + " wishes to trade with you");
    } else if (type == CHAT_OTHER) {
      if (message.contains(" wishes to duel with you"))
        NotificationsHandler.notify(
            NotifType.DUEL, "Duel Request", message.replaceAll("@...@", ""));
    }

    if (type == Client.CHAT_PRIVATE || type == Client.CHAT_PRIVATE_OUTGOING) {
      if (username != null) lastpm_username = username;
    }

    // Don't output private messages if option is turned on and replaying
    if (Settings.HIDE_PRIVATE_MSGS_REPLAY.get(Settings.currentProfile) && Replay.isPlaying) {
      if (type == CHAT_PRIVATE_LOG_IN_OUT || type == CHAT_PRIVATE || type == CHAT_PRIVATE_OUTGOING)
        return;
    }

    String originalLog =
        "("
            + formatChatType(type)
            + ") "
            + ((username == null) ? "" : formatUsername(username, type))
            + message;
    String colorizedLog =
        "@|white ("
            + formatChatType(type)
            + ")|@ "
            + ((username == null) ? "" : colorizeUsername(formatUsername(username, type), type))
            + colorizeMessage(message, type);
    Logger.Chat(colorizedLog, originalLog);
  }

  private static String formatChatType(int type) {
    String chatType = getChatTypeName(type).toUpperCase();

    // Make text fixed width so it aligns properly
    final int fixedWidth = getChatTypeName(CHAT_INCOMING_OPTION).length();
    while (chatType.length() < fixedWidth) chatType = " " + chatType;

    return chatType;
  }

  private static String getChatTypeName(int type) {
    switch (type) {
      case CHAT_NONE:
        return "none";
      case CHAT_PRIVATE:
        return "pm_in";
      case CHAT_PRIVATE_OUTGOING:
        return "pm_out";
      case CHAT_QUEST:
        return "quest";
      case CHAT_CHAT:
        return "chat";
      case CHAT_PRIVATE_LOG_IN_OUT:
        return "pm_log";
      case CHAT_TRADE_REQUEST_RECEIVED:
        return "trade";
      case CHAT_OTHER:
        return "other";
      case CHAT_INCOMING_OPTION:
        return "option";
      case CHAT_CHOSEN_OPTION:
        return "select";
      case CHAT_WINDOWED_MSG:
        return "window";
      default:
        return Integer.toString(type);
    }
  }

  public static String formatText(String inputText, int length) {
    if (Reflection.resetTimings == null) return null;
    String outputText = null;

    try {
      outputText = (String) Reflection.formatText.invoke(null, length, (byte) -5, inputText);
    } catch (Exception e) {
    }
    return outputText;
  }

  /**
   * Formats the username clause preceding a chat message for use in the console.
   *
   * @param username the username associated with the message
   * @param type the type of message being displayed
   * @return the formatted username clause
   */
  private static String formatUsername(String username, int type) {
    switch (type) {
      case CHAT_PRIVATE:
        // Username tells you:
        username = username + " tells you: ";
        break;
      case CHAT_PRIVATE_OUTGOING:
        // You tell Username:
        username = "You tell " + username + ": ";
        break;
      case CHAT_QUEST:
        // If username != null during CHAT_QUEST, then this is your player name
        username = username + ": ";
        break;
      case CHAT_CHAT:
        username = username + ": ";
        break;
      case CHAT_TRADE_REQUEST_RECEIVED: // happens when player trades you
        username = username + " wishes to trade with you.";
        break;
        /* username will not appear in these chat types, but just to cover it I'm leaving code commented out here
        case CHAT_NONE:
        case CHAT_PRIVATE_LOG_IN_OUT:
        case CHAT_PLAYER_INTERRACT_OUT:
        */
      default:
        Logger.Info("Username specified for unhandled chat type, please report this: " + type);
        username = username + ": ";
    }

    return username;
  }

  /**
   * Adds color to the username clause preceding a chat message for use in the console.
   *
   * @param colorMessage the username clause to colorize
   * @param type the type of message being displayed
   * @return the colorized username clause
   */
  public static String colorizeUsername(String colorMessage, int type) {
    switch (type) {
      case CHAT_PRIVATE:
        // Username tells you:
        colorMessage = "@|cyan,intensity_bold " + colorMessage + "|@";
        break;
      case CHAT_PRIVATE_OUTGOING:
        // You tell Username:
        colorMessage = "@|cyan,intensity_bold " + colorMessage + "|@";
        break;
      case CHAT_QUEST:
        // If username != null during CHAT_QUEST, then this is your player name, which is usually
        // white
        colorMessage = "@|white,intensity_faint " + colorMessage + "|@";
        break;
      case CHAT_CHAT:
        // just bold username for chat
        colorMessage = "@|yellow,intensity_bold " + colorMessage + "|@";
        break;
      case CHAT_TRADE_REQUEST_RECEIVED: // happens when player trades you
        colorMessage = "@|white " + colorMessage + "|@";
        break;
        /* username will not appear in these chat types, but just to cover it I'm leaving code commented out here
        case CHAT_NONE:
        case CHAT_PRIVATE_LOG_IN_OUT:
        case CHAT_PLAYER_INTERRACT_OUT:
        */

      default:
        Logger.Info("Username specified for unhandled chat type, please report this: " + type);
        colorMessage = "@|white,intensity_bold " + colorMessage + "|@";
    }
    return colorMessage;
  }

  /**
   * Adds color to the contents of a chat message for use in the console.
   *
   * @param colorMessage the message to colorize
   * @param type the type of message being displayed
   * @return the colorized message
   */
  public static String colorizeMessage(String colorMessage, int type) {
    boolean whiteMessage = colorMessage.contains("Welcome to RuneScape!"); // want this to be bold
    boolean blueMessage =
        (type == CHAT_NONE)
            && (colorMessage.contains(
                "You have been standing here for 5 mins! Please move to a new area"));
    boolean yellowMessage =
        (type == CHAT_NONE) && (colorMessage.contains("Well Done")); // tourist trap completion
    boolean screenshotMessage =
        (type == CHAT_NONE)
            && (colorMessage.contains("You just advanced ")
                || (colorMessage.contains("quest point") && colorMessage.endsWith("!"))
                || colorMessage.contains("ou have completed"));
    boolean greenMessage =
        screenshotMessage
            || (type == CHAT_NONE
                && (colorMessage.contains("poisioned!")
                    || colorMessage.contains("***"))); // "***" is for Tourist Trap completion

    if (screenshotMessage
        && Settings.AUTO_SCREENSHOT.get(Settings.currentProfile)
        && !Replay.isPlaying) {
      Renderer.takeScreenshot(true);
    }

    if (blueMessage) { // this is one of the messages which we must overwrite expected color for
      return "@|cyan,intensity_faint " + colorReplace(colorMessage) + "|@";
    } else if (greenMessage) {
      return "@|green,intensity_bold " + colorReplace(colorMessage) + "|@";
    } else if (whiteMessage) {
      // if (colorMessage.contains("Welcome to RuneScape!")) {
      // this would be necessary if whiteMessage had more than one .contains()
      // }

      return "@|white,intensity_bold " + colorMessage + "|@";
    } else if (yellowMessage) {
      return "@|yellow,intensity_bold " + colorMessage + "|@";
    }

    switch (type) {
      case CHAT_PRIVATE:
      case CHAT_PRIVATE_OUTGOING:
        // message to/from PMs
        colorMessage = "@|cyan,intensity_faint " + colorReplace(colorMessage) + "|@";
        break;
      case CHAT_QUEST:
        if (colorMessage.contains(":") && !colorMessage.startsWith("***")) {
          // this will be like "banker: would you like to access your bank account?" which should be
          // yellow. Avoids yellow print the message of tourist trap
          colorMessage = "@|yellow,intensity_faint " + colorReplace(colorMessage) + "|@";
        } else {
          // this is usually skilling
          colorMessage = "@|white,intensity_faint " + colorReplace(colorMessage) + "|@";
        }
        break;
      case CHAT_CHAT:
        colorMessage = "@|yellow,intensity_faint " + colorReplace(colorMessage) + "|@";
        break;
      case CHAT_PRIVATE_LOG_IN_OUT:
        // don't need to colorReplace, this is just "username has logged in/out"
        colorMessage = "@|cyan,intensity_faint " + colorMessage + "|@";
        break;
      case CHAT_NONE: // have to replace b/c @cya@Screenshot saved...
      case CHAT_TRADE_REQUEST_RECEIVED:
      case CHAT_WINDOWED_MSG:
      case CHAT_OTHER:
        colorMessage = "@|white " + colorReplace(colorMessage) + "|@";
        break;
      case CHAT_INCOMING_OPTION:
        colorMessage = "@|cyan,intensity_faint " + colorReplace(colorMessage) + "|@";
        break;
        // faint red since it would have been a hover/selection over the item
      case CHAT_CHOSEN_OPTION:
        colorMessage = "@|red,intensity_faint " + colorReplace(colorMessage) + "|@";
        break;
      default: // this should never happen, only 10 Chat Types
        Logger.Info("Unhandled chat type in colourizeMessage, please report this:" + type);
        colorMessage = "@|white,intensity_faint " + colorReplace(colorMessage) + "|@";
    }
    return colorMessage;
  }

  public static String colorReplace(String colorMessage) {
    for (int i = 0; i + 1 < colorDict.length; i += 2) {
      if (!colorMessage.matches(".*@.{3}@.*")) { // if doesn't contain any color codes: break;
        break;
      }
      colorMessage = colorMessage.replaceAll(colorDict[i], colorDict[i + 1]);
    }

    // we could replace @.{3}@ with "" to remove "@@@@@" or "@dne@" (i.e. color code which does not
    // exist) just like
    // in chat box, but I think it's more interesting to leave the misspelled stuff in terminal

    // could also respect ~xxx~ but not really useful.

    return colorMessage;
  }

  public static void drawNPC(
      int x,
      int y,
      int width,
      int height,
      String name,
      int currentHits,
      int maxHits,
      int id,
      int id2) {
    // ILOAD 6 is index
    npc_list.add(new NPC(x, y, width, height, name, NPC.TYPE_MOB, currentHits, maxHits, id, id2));
  }

  public static void drawPlayer(
      int x, int y, int width, int height, String name, int currentHits, int maxHits, int id2) {
    npc_list.add(new NPC(x, y, width, height, name, NPC.TYPE_PLAYER, currentHits, maxHits, 0, id2));
  }

  public static void drawItem(int x, int y, int width, int height, int id) {
    item_list.add(new Item(x, y, width, height, id));
  }

  /**
   * Returns the minimum XP required to reach a specified level, starting from 0 XP.
   *
   * @param level the level
   * @return the minimum XP required to reach the specified level, starting from 0 XP
   */
  public static float getXPforLevel(int level) {
    if (level < 2) {
      return 0;
    }

    if (level > Util.xpLevelTable.length - 1) {
      // This probably doesn't ever happen since our lookup table already goes to virtual level 150.
      // levels 1 to 120 are from the official game, level 121 to 150 are from this formula below
      float xp = 0.0f;
      for (int x = 1; x < level; x++) xp += Math.floor(x + 300 * Math.pow(2, x / 7.0f)) / 4.0f;
      return (float) Math.floor(xp);
    }

    // speedier to use a lookup table than to always calculate
    return Util.xpLevelTable[level];
  }

  public static float getLevelFromXP(float xp) {

    // 136.53725 is the maximum level you can reach in RSC before XP rolls over negative
    int lvl = 1;
    while (lvl <= 137 && getXPforLevel(lvl) <= xp) {
      lvl++;
    }
    float xpToLevel = (float) Math.floor(getXPforLevel(lvl) - xp);
    if (xpToLevel > 0) {
      lvl--;
      float xpIntoLevel = (float) Math.floor(xp - getXPforLevel(lvl));
      float xpBetweenLevels = (getXPforLevel(lvl + 1) - getXPforLevel(lvl));
      return lvl + (xpIntoLevel / xpBetweenLevels);
    } else {
      return lvl;
    }
  }

  /**
   * Returns the minimum XP required until the user reaches the next level in a specified skill.
   *
   * @param skill an integer corresponding to a skill
   * @return the minimum XP required until the user reaches the next level in the specified skill
   */
  public static float getXPUntilLevel(int skill) {
    float xpNextLevel = getXPforLevel(base_level[skill] + 1);
    return xpNextLevel - getXP(skill);
  }

  public static float getXPUntilGoal(int skill) {
    return xpGoals.get(xpUsername)[skill] - getXP(skill);
  }

  /**
   * Returns the user's current level in a specified skill. This number is affected by skills boosts
   * and debuffs.
   *
   * @param skill an integer corresponding to a skill
   * @return the user's current level in the specified skill
   * @see #getBaseLevel(int)
   */
  public static int getCurrentLevel(int skill) {
    return current_level[skill];
  }

  /**
   * Returns the sum of the user's base skill levels.
   *
   * @return the user's total level
   */
  public static int getTotalLevel() {
    int total = 0;
    for (int skill = 0; skill < NUM_SKILLS; skill++) total += Client.base_level[skill];
    return total;
  }

  /**
   * Returns the sum of the XP of the user's skills.
   *
   * @return the user's total XP
   */
  public static float getTotalXP() {
    float xp = 0;
    for (int skill = 0; skill < NUM_SKILLS; skill++) xp += getXP(skill);
    return xp;
  }

  /**
   * Returns the user's base level in a specified skill. This number is <b>not</b> affected by
   * skills boosts and debuffs.
   *
   * @param skill an integer corresponding to a skill
   * @return the user's base level in the specified skill
   * @see #getCurrentLevel(int)
   */
  public static int getBaseLevel(int skill) {
    return base_level[skill];
  }

  /**
   * Returns the user's XP in a specified skill.
   *
   * @param skill an integer corresponding to a skill
   * @return the user's XP in the specified skill
   */
  public static float getXP(int skill) {
    return (float) xp[skill] / 4.0f;
  }

  /**
   * Returns the user's fatigue as a percent, rounded down to the nearest percent.
   *
   * @return the user's fatigue as a percent
   * @see #getActualFatigue()
   */
  public static int getFatigue() {
    return fatigue * 100 / 750;
  }

  /**
   * Returns the user's fatigue as a float percentage.
   *
   * @return the user's fatigue as a float percentage
   * @see #getFatigue()
   */
  public static float getActualFatigue() {
    return (float) (fatigue * 100.0 / 750);
  }

  /**
   * Rounds a number to a specified decimal place
   *
   * @param num the number to be rounded
   * @param figures the number of decimal places to round to
   * @return the rounded number
   */
  public static Double trimNumber(double num, int figures) {
    return Math.round(num * Math.pow(10, figures)) / Math.pow(10, figures);
  }

  public static void updateCurrentFatigue() {
    final float nextFatigue = getActualFatigue();
    if (currentFatigue != nextFatigue) {
      currentFatigue = nextFatigue;
      current_fatigue_units = fatigue;
    }
  }

  /**
   * Checks if a specified player is on the user's friend list.
   *
   * @param name the player's display name
   * @return if the player is the user's friend
   */
  public static boolean isFriend(String name) {
    for (int i = 0; i < friends_count; i++) {
      if (friends[i] != null && friends[i].equals(name)) return true;
    }

    return false;
  }

  /**
   * Returns if the user is currently in combat. Recently being in combat does not count as in
   * combat.
   *
   * @return if the user is in combat
   */
  public static boolean isInCombat() {
    return combat_timer == 499;
  }

  public static boolean isInCombatWithNPC(NPC npc) {
    if (npc == null) {
      return false;
    }

    int bottom_posY_npc = npc.y + npc.height;
    int bottom_posY_player = player_posY + player_height;

    // NPC's in combat with the player are always on the same bottom y coord, however
    // when moving the screen around they can be slightly off for a moment. To prevent
    // flickering, just give them a very small buffer of difference.
    boolean inCombatCandidate = (Math.abs(bottom_posY_npc - bottom_posY_player) < 5);

    // Hitboxes will intersect on the X axis from what I've tested, giving this a small
    // buffer as well just in case there are edge cases with very small monsters that
    // don't follow this pattern exactly.
    boolean hitboxesIntersectOnXAxis = (player_posX - 10) < (npc.x + npc.width);

    // The NPC you're fighting is always on the left side of the player.
    boolean isOnLeftOfPlayer = (player_posX + player_width) > npc.x;

    return isInCombat()
        && npc.currentHits != 0
        && npc.maxHits != 0
        && !player_name.equals(npc.name)
        && inCombatCandidate
        && isOnLeftOfPlayer
        && hitboxesIntersectOnXAxis;
  }

  /**
   * Returns if an in-game interface, window, menu, etc. is currently displayed.
   *
   * @return if an interface is showing
   */
  public static boolean isInterfaceOpen() {
    return show_bank
        || show_shop
        || show_welcome
        || show_trade
        || show_tradeconfirm
        || show_duel
        || show_duelconfirm
        || show_report != 0
        || show_friends != 0
        || show_sleeping
        || showAppearanceChange
        || showRecoveryQuestions
        || showContactDetails;
  }

  /**
   * Returns if the user is sleeping.
   *
   * @return if the user is sleeping
   */
  public static boolean isSleeping() {
    return show_sleeping;
  }

  /**
   * Returns if the welcome screen is currently displayed.
   *
   * @return if the welcome screen is currently displayed
   */
  public static boolean isWelcomeScreen() {
    return show_welcome;
  }

  /** Writes {@link #strings} to a file. */
  private static void dumpStrings() {
    BufferedWriter writer = null;

    try {
      File file = new File(Settings.Dir.DUMP + "/strings.dump");
      writer = new BufferedWriter(new FileWriter(file));

      writer.write("Client:\n\n");
      for (int i = 0; i < strings.length; i++) writer.write(i + ": " + strings[i] + "\n");

      writer.close();
    } catch (Exception e) {
      try {
        writer.close();
      } catch (Exception e2) {
      }
    }
  }

  public static Boolean[] getShowXpPerHour() {
    return showXpPerHour.get(xpUsername);
  }

  public static Double[] getXpPerHour() {
    return xpPerHour.get(xpUsername);
  }

  public static Double getLastXpGain(int skill) {
    return lastXpGain.get(xpUsername)[skill][LAST_XP_GAIN];
  }

  public static boolean wikiLookupReplacesReportAbuse() {
    if (Settings.WIKI_LOOKUP_ON_HBAR.get(Settings.currentProfile)) {
      if (Settings.REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(Settings.currentProfile)) {
        return true;
      }
      // both wiki lookup and report abuse button are enabled
      // want to make room for hp/prayer/fatigue overlay if possible
      if (Settings.SHOW_HP_PRAYER_FATIGUE_OVERLAY.get(Settings.currentProfile)) {
        // there is a 90 pixel region where HP/Prayer/Fatigue overlay can't be drawn, but both
        // Report Abuse & Wiki lookup can
        return (Renderer.width < 900 && Renderer.width >= 704) || Renderer.width < 512 + 90 + 12;
      } else {
        return Renderer.width < 512 + 90 + 12;
      }
    }
    return false;
  }

  public static void displayMotivationalQuote() {
    // TODO: more motivational quotes
    int colorIdx = ((int) (Math.random() * ((Client.colorDict.length / 2) - 1)) * 2);
    String color = colorDict[colorIdx].substring(4);
    // A coward dies a thousand deaths, but the valiant tastes death but once.
    displayMessage(color + "You are beautiful today, " + player_name + ".", CHAT_QUEST);
  }
}

// set Client.loginMessageBottom/Top before calling if you want something else to show up
class LoginMessageHandler implements Runnable {
  @Override
  public void run() {
    try {
      Thread.sleep(5);
      Client.setResponseMessage(Client.loginMessageBottom, Client.loginMessageTop);
    } catch (InterruptedException e) {
      Logger.Error(
          "The login message thread was interrupted unexpectedly! Perhaps the game crashed or was killed?");
      // End the thread
    }
  }
}
