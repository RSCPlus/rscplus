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

package Game;

import static org.fusesource.jansi.Ansi.ansi;
import java.applet.Applet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.fusesource.jansi.AnsiConsole;
import Client.KeybindSet;
import Client.Logger;
import Client.NotificationsHandler;
import Client.NotificationsHandler.NotifType;
import Client.Settings;
import Client.TwitchIRC;

/**
 * This class prepares the client for login, handles chat messages, and performs player related calculations.
 */
public class Client {
	
	// Game's client instance
	public static Object instance;
	
	public static List<NPC> npc_list = new ArrayList<>();
	public static List<Item> item_list = new ArrayList<>();
	
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
	
	public static final int STATE_LOGIN = 1;
	public static final int STATE_GAME = 2;
	
	public static final int MENU_NONE = 0;
	public static final int MENU_INVENTORY = 1;
	public static final int MENU_MINIMAP = 2;
	public static final int MENU_STATS = 3;
	public static final int MENU_FRIENDS = 4;
	public static final int MENU_SETTINGS = 5;
	
	public static final int CHAT_NONE = 0;
	public static final int CHAT_PRIVATE = 1;
	public static final int CHAT_PRIVATE_OUTGOING = 2;
	public static final int CHAT_QUEST = 3;
	public static final int CHAT_CHAT = 4;
	public static final int CHAT_PRIVATE_LOG_IN_OUT = 5;
	public static final int CHAT_TRADE_REQUEST_RECEIVED = 6; //only used when another player sends you a trade request. (hopefully!)
	public static final int CHAT_OTHER = 7;	// used for when you send a player a duel/trade request, follow someone, or drop an item
	
	public static final int COMBAT_CONTROLLED = 0;
	public static final int COMBAT_AGGRESSIVE = 1;
	public static final int COMBAT_ACCURATE = 2;
	public static final int COMBAT_DEFENSIVE = 3;
	
	public static int state = STATE_LOGIN;
	
	public static int max_inventory;
	public static int inventory_count;
	public static long magic_timer = 0L;
	
	public static int combat_timer;
	public static boolean show_bank;
	public static boolean show_duel;
	public static boolean show_duelconfirm;
	public static int show_friends;
	public static int show_menu;
	public static boolean show_questionmenu;
	public static int show_report;
	public static boolean show_shop;
	public static boolean show_sleeping;
	public static boolean show_trade;
	public static boolean show_tradeconfirm;
	public static boolean show_welcome;
	
	public static int[] inventory_items;
	
	public static int fatigue;
	private static float currentFatigue;
	public static int[] current_level;
	public static int[] base_level;
	public static int[] xp;
	public static String[] skill_name;
	public static int combat_style;
	
	public static int friends_count;
	public static String[] friends;
	
	public static String pm_username;
	public static String pm_text;
	public static String pm_enteredText;
	public static String lastpm_username = null;
	
	public static int login_screen;
	public static String username_login;
	
	public static Object player_object;
	public static String player_name = null;
	public static int player_posX = -1;
	public static int player_posY = -1;
	public static int player_height = -1;
	public static int player_width = -1;
	
	public static int regionX = -1;
	public static int regionY = -1;
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
	
	// bank items and their count for each type, new bank items are first to get updated and indicate bank
	// excluding inventory types and bank items do include them (in regular mode), as bank operations are messy
	// they get excluded also in searchable bank
	public static int[] bank_items_count;
	public static int[] bank_items;
	public static int[] new_bank_items_count;
	public static int[] new_bank_items;
	
	// these two variables, they indicate distinct bank items count
	public static int new_count_items_bank;
	public static int count_items_bank;
	
	public static int selectedItem;
	public static int selectedItemSlot;
	
	/**
	 * An array of Strings that stores text used in the client
	 */
	public static String[] strings;
	
	public static XPDropHandler xpdrop_handler = new XPDropHandler();
	public static XPBar xpbar = new XPBar();
	
	private static TwitchIRC twitch = new TwitchIRC();
	private static MouseHandler handler_mouse;
	private static KeyboardHandler handler_keyboard;
	private static float[] xpdrop_state = new float[18];
	private static long updateTimer = 0;
	
	/**
	 * A boolean array that stores if the XP per hour should be shown for a given skill when hovering on the XP bar.
	 * <p>
	 * This should only be false for a skill if there has been less than 2 XP drops during the current tracking session,
	 * since there is not enough data to calculate the XP per
	 * hour.
	 * </p>
	 */
	private static boolean[] showXpPerHour = new boolean[18];
	
	/**
	 * An array to store the XP per hour for a given skill
	 */
	private static double[] xpPerHour = new double[18];
	
	/**
	 * A multi-dimensional array that stores the last time XP was gained for a given skill.
	 * <p>
	 * The first dimension stores the skill index corresponding to the constants defined in the client class (
	 * {@link #SKILL_ATTACK}, etc.)<br>
	 * The second dimension stores:<br>
	 * - [0] the total XP gained in a given skill within the sample period,<br>
	 * - [1] the time of the last XP drop in a given skill,<br>
	 * - [2] the time of the first XP drop in a given skill within the sample period,<br>
	 * - [3] and the total number of XP drops recorded within the sample period, plus 1.
	 * </p>
	 */
	private static double[][] lastXpGain = new double[18][4];
	
	/**
	 * The client version
	 */
	public static int version;
	
	/**
	 * Iterates through {@link #strings} array and checks if various conditions are met. Used for patching client text.
	 */
	public static void adaptStrings() {
		for (int i = 0; i < strings.length; i++) {
			if (!Settings.SHOW_LOGINDETAILS && strings[i].startsWith("from:")) {
				strings[i] = "@bla@from:";
			} else if (Settings.SHOW_LOGINDETAILS && strings[i].startsWith("@bla@from:")) {
				strings[i] = "from:";
			}
		}
	}
	
	public static void init() {
		
		adaptStrings();
		
		handler_mouse = new MouseHandler();
		handler_keyboard = new KeyboardHandler();
		
		Applet applet = Game.getInstance().getApplet();
		applet.addMouseListener(handler_mouse);
		applet.addMouseMotionListener(handler_mouse);
		applet.addMouseWheelListener(handler_mouse);
		applet.addKeyListener(handler_keyboard);
		applet.setFocusTraversalKeysEnabled(false);
		
		if (Settings.DISASSEMBLE)
			dumpStrings();
		
		// Initialize login
		init_login();
		
		// Skip first login screen and don't wipe user info
		login_screen = 2;
	}
	
	/**
	 * An updater that runs frequently to update calculations for XP/fatigue drops, the XP bar, etc.
	 * <p>
	 * This updater does not handle any rendering, for rendering see {@link Renderer#present}
	 * </p>
	 */
	public static void update() {
		// FIXME: This is a hack from a rsc client update (so we can skip updating the client this time)
		version = 235;
		
		if (state == STATE_GAME) {
			// Process XP drops
			boolean dropXP = xpdrop_state[SKILL_HP] > 0.0f; // TODO: Declare dropXP outside of the update method
			for (int i = 0; i < xpdrop_state.length; i++) {
				float xpGain = getXP(i) - xpdrop_state[i]; // TODO: Declare xpGain outside of the update method
				xpdrop_state[i] += xpGain;
				
				if (xpGain > 0.0f && dropXP) {
					if (Settings.SHOW_XPDROPS)
						xpdrop_handler.add("+" + xpGain + " (" + skill_name[i] + ")", Renderer.color_text);
					
					// XP/hr calculations
					// TODO: After 5-10 minutes of tracking XP, make it display a rolling average instead of a session
					// average
					if ((System.currentTimeMillis() - lastXpGain[i][1]) <= 180000) {
						// < 3 minutes since last XP drop
						lastXpGain[i][0] = xpGain + lastXpGain[i][0];
						xpPerHour[i] = 3600 * (lastXpGain[i][0]) / ((System.currentTimeMillis() - lastXpGain[i][2]) / 1000);
						lastXpGain[i][3]++;
						showXpPerHour[i] = true;
						lastXpGain[i][1] = System.currentTimeMillis();
					} else {
						lastXpGain[i][0] = xpGain;
						lastXpGain[i][1] = lastXpGain[i][2] = System.currentTimeMillis();
						lastXpGain[i][3] = 0;
						showXpPerHour[i] = false;
					}
					
					if (i == SKILL_HP && xpbar.current_skill != -1)
						continue;
					
					xpbar.setSkill(i);
				}
			}
			// Process fatigue drops
			if (Settings.SHOW_FATIGUEDROPS) {
				final float actualFatigue = getActualFatigue();
				final float fatigueGain = actualFatigue - currentFatigue;
				if (fatigueGain > 0.0f && !isWelcomeScreen()) {
					xpdrop_handler.add("+" + trimNumber(fatigueGain, Settings.FATIGUE_FIGURES) + "% (Fatigue)", Renderer.color_fatigue);
					currentFatigue = actualFatigue;
				}
			}
			// Prevents a fatigue drop upon first login during a session
			if (isWelcomeScreen() && currentFatigue != getActualFatigue())
				currentFatigue = getActualFatigue();
		}
	}
	
	public static void init_login() {
		for (int i = 0; i < xpdrop_state.length; i++)
			xpdrop_state[i] = 0.0f;
		
		Camera.init();
		state = STATE_LOGIN;
		
		twitch.disconnect();
		
		// After logging out, the login screen shows "Please wait... Connecting to server", so we'll overwrite this
		setLoginMessage("Please enter your username and password", "");
		adaptStrings();
		player_name = null;
	}
	
	public static void init_game() {
		Camera.init();
		combat_style = Settings.COMBAT_STYLE;
		state = STATE_GAME;
		
		if (TwitchIRC.isUsing())
			twitch.connect();
	}
	
	/**
	 * Stores the user's display name in {@link #player_name}.
	 */
	public static void getPlayerName() {
		try {
			player_name = (String)Reflection.characterName.get(player_object);
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Prints the coordinates of the player
	 */
	public static void getCoords() { // TODO: Use this method or remove it
		try {
			// int coordX = Reflection.characterPosX.getInt(player_object) / 128;
			// int coordY = Reflection.characterPosY.getInt(player_object) / 128;
			// System.out.println("("+coordX+","+coordY+")");
			
			System.out.println("Region x: " + regionX);
			System.out.println("Region y: " + regionY);
			
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		}
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
			return "::";
		}
		
		line = processClientChatCommand(line);
		line = processClientCommand(line);
		
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
			case "toggleroofs":
				Settings.toggleHideRoofs();
				break;
			case "togglecombat":
				Settings.toggleCombatMenu();
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
				Settings.toggleShowPlayerInfo();
				break;
			case "togglefriendinfo":
				Settings.toggleShowFriendInfo();
				break;
			case "togglenpcinfo":
				Settings.toggleShowNPCInfo();
				break;
			case "toggleiteminfo":
				Settings.toggleShowItemInfo();
				break;
			case "togglelogindetails":
				Settings.toggleShowLoginDetails();
				break;
			case "togglestartsearchedbank":
				if (commandArray.length > 1) {
					StringBuilder sb = new StringBuilder();
					for (int i = 1; i < commandArray.length; i++) {
						if (commandArray[i].trim().equals(""))
							continue;
						sb.append(commandArray[i].trim().toLowerCase());
						if (i < commandArray.length - 1)
							sb.append(" ");
					}
					Settings.toggleStartSearchedBank(sb.toString(), true);
				} else {
					Settings.toggleStartSearchedBank("", false);
				}
				
				break;
			case "banksearch":
				// enters searchable bank mode, to return normal mode player has to speak to the banker again
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
				Renderer.takeScreenshot();
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
			case "toggleinvcount":
				Settings.toggleInvCount();
				break;
			case "togglestatusdisplay":
				Settings.toggleStatusDisplay();
				break;
			case "help":
				try {
					Help.help(Integer.parseInt(commandArray[2]), commandArray[1]);
				} catch (Exception e) {
					Help.help(0, "help");
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
				// this command breaks character limits and might be bannable... would not recommend sending this
				// command over PM to rs2/rs3
				return "@whi@My Combat is Level "
						+ "@gre@" +
						// basic melee stats
						((base_level[SKILL_ATTACK] + base_level[SKILL_STRENGTH] + base_level[SKILL_DEFENSE] + base_level[SKILL_HP]) * 0.25
								// add ranged levels if ranger
								+ ((base_level[SKILL_ATTACK] + base_level[SKILL_STRENGTH]) < base_level[SKILL_RANGED] * 1.5 ? base_level[SKILL_RANGED] * 0.25 : 0)
								// prayer and mage
								+ (base_level[SKILL_PRAYER] + base_level[SKILL_MAGIC]) * 0.125)
						+ " @lre@A:@whi@ " + base_level[SKILL_ATTACK]
						+ " @lre@S:@whi@ " + base_level[SKILL_STRENGTH]
						+ " @lre@D:@whi@ " + base_level[SKILL_DEFENSE]
						+ " @lre@H:@whi@ " + base_level[SKILL_HP]
						+ " @lre@R:@whi@ " + base_level[SKILL_RANGED]
						+ " @lre@P:@whi@ " + base_level[SKILL_PRAYER]
						+ " @lre@M:@whi@ " + base_level[SKILL_MAGIC];
			} else if ("cmbnocolor".equals(command)) { // this command stays within character limits and is safe.
				return "My Combat is Level "
						// basic melee stats
						+ ((base_level[SKILL_ATTACK] + base_level[SKILL_STRENGTH] + base_level[SKILL_DEFENSE] + base_level[SKILL_HP]) * 0.25
								// add ranged levels if ranger
								+ ((base_level[SKILL_ATTACK] + base_level[SKILL_STRENGTH]) < base_level[SKILL_RANGED] * 1.5 ? base_level[SKILL_RANGED] * 0.25 : 0)
								// prayer and mage
								+ (base_level[SKILL_PRAYER] + base_level[SKILL_MAGIC]) * 0.125)
						+ " A:" + base_level[SKILL_ATTACK]
						+ " S:" + base_level[SKILL_STRENGTH]
						+ " D:" + base_level[SKILL_DEFENSE]
						+ " H:" + base_level[SKILL_HP]
						+ " R:" + base_level[SKILL_RANGED]
						+ " P:" + base_level[SKILL_PRAYER]
						+ " M:" + base_level[SKILL_MAGIC];
			} else if ("bank".equals(command)) {
				return "Hey, everyone, I just tried to do something very silly!";
			} else if ("update".equals(command)) {
				checkForUpdate(true);
			} else if (command.startsWith("xmas ")) {
				int randomStart = (int)System.currentTimeMillis();
				if (randomStart < 0) {
					randomStart *= -1; // casting to long to int sometimes results in a negative number
				}
				String subline = "";
				String[] colours = { "@red@", "@whi@", "@gre@", "@whi@" };
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
				int randomStart = (int)System.currentTimeMillis();
				if (randomStart < 0) {
					randomStart *= -1; // casting to long to int sometimes results in a negative number
				}
				String subline = "";
				String[] colours = { "@red@", "@ora@", "@yel@", "@gre@", "@cya@", "@mag@" };
				int spaceCounter = 0;
				for (int i = 0; i < line.length() - 10; i++) {
					if (" ".equals(line.substring(10 + i, 11 + i))) {
						spaceCounter += 1;
					}
					subline += colours[(i - spaceCounter + randomStart) % 6];
					subline += line.substring(10 + i, 11 + i);
				}
				return subline;
				
			} else if (command.startsWith("system ")) {// ~007~@bla@Username~007~ Msg
				if (Client.player_name != null) {
					return "~007~@bla@" + Client.player_name + ":~007~@yel@" + line.substring(9, line.length());
				} else {
					return line.substring(9, line.length()); // send the message anyway
				}
			} else if (command.startsWith("next_")) {
				for (int i = 0; i < 18; i++) {
					if (command.equals("next_" + skill_name[i].toLowerCase())) {
						final float neededXp = base_level[i] == 99 ? 0 : getXPforLevel(base_level[i] + 1) - getXP(i);
						return "I need " + neededXp + " more xp for " + (base_level[i] == 99 ? 99 : base_level[i] + 1) + " " + skill_name[i] + ".";
					}
				}
			}
			
			for (int i = 0; i < 18; i++) {
				if (command.equalsIgnoreCase(skill_name[i]))
					return "My " + skill_name[i] + " level is " + base_level[i] + " (" + getXP(i) + " xp).";
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
	public static void displayMessage(String message, int chat_type) {
		if (Client.state != Client.STATE_GAME || Reflection.displayMessage == null)
			return;
		
		try {
			Reflection.displayMessage.invoke(Client.instance, false, null, 0, message, chat_type, 0, null, null);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Sets the client text above the login information on the login screen.
	 * 
	 * @param line1 the bottom line of text
	 * @param line2 the top line of text
	 */
	public static void setLoginMessage(String line1, String line2) {
		if (Reflection.setLoginText == null)
			return;
		
		try {
			Reflection.setLoginText.invoke(Client.instance, (byte)-49, line2, line1);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Send over the instruction of sleep, if player has sleeping bag with them
	 */
	public static void sleep() {
		if (Reflection.itemClick == null)
			return;
		
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
	 * Logs the user out of the game.
	 */
	public static void logout() {
		if (Reflection.logout == null)
			return;
		
		try {
			Reflection.logout.invoke(Client.instance, 0);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Fetches the value of {@link Settings#VERSION_NUMBER} in the master branch on GitHub.
	 * <p>
	 * Used to check the newest version of the client.
	 * </p>
	 * 
	 * @return the current version number
	 */
	public static Double fetchLatestVersionNumber() {
		try {
			Double currentVersion = 0.0;
			URL updateURL = new URL("https://raw.githubusercontent.com/OrN/rscplus/master/src/Client/Settings.java");
			
			// Open connection
			URLConnection connection = updateURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			// in our current client version, we are looking at the source file of Settings.java in the main repository
			// in order to parse what the current version number is.
			String line;
			while ((line = in.readLine()) != null) {
				if (line.contains("VERSION_NUMBER")) {
					currentVersion = Double.parseDouble(line.substring(line.indexOf('=') + 1, line.indexOf(';')));
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
	 * Compares the local value of {@link Settings#VERSION_NUMBER} to the value on the GitHub master branch.
	 * <p>
	 * Used to check if there is a newer version of the client available.
	 * </p>
	 * 
	 * @param announceIfUpToDate if a message should be displayed in chat if the client is up-to-date
	 */
	public static void checkForUpdate(boolean announceIfUpToDate) {
		double latestVersion = fetchLatestVersionNumber();
		if (latestVersion > Settings.VERSION_NUMBER) {
			displayMessage("@gre@A new version of RSC+ is available!", CHAT_QUEST);
			// TODO: before Y10K update this to %9.6f
			displayMessage("The latest version is @gre@" + String.format("%8.6f", latestVersion), CHAT_QUEST);
			displayMessage("~034~ Your version is @red@" + String.format("%8.6f", Settings.VERSION_NUMBER), CHAT_QUEST);
			if (Settings.CHECK_UPDATES) {
				displayMessage("~034~ You will receive the update next time you restart rscplus", CHAT_QUEST);
			}
		} else if (announceIfUpToDate) {
			displayMessage("You're up to date: @gre@" + String.format("%8.6f", latestVersion), CHAT_QUEST);
		}
	}
	
	// combat packet received (testing only, the info is taken on function that draws hits)
	public static void inCombatHook(int damageTaken, int currentHealth, int maxHealth, int index, int hooknum, Object obj) {
		// discard if info seems to be for local player
		int n1, n2;
		String name;
		// object was found
		if (obj != null) {
			try {
				n1 = Reflection.attackingPlayerIdx.getInt(obj);
				n2 = Reflection.attackingNpcIdx.getInt(obj);
				name = (String)Reflection.characterDisplayName.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
			}
		}
	}
	
	/**
	 * This method hooks all chat messages.
	 * 
	 * @param username the username that the message originated from
	 * @param message the content of the message
	 * @param type the type of message being displayed
	 */
	public static void messageHook(String username, String message, int type) {
		if (username != null)
			// Prevents non-breaking space in colored usernames appearing as an accented 'a' in console
			username = username.replace("\u00A0", " ");
		if (message != null)
			// Prevents non-breaking space in colored usernames appearing as an accented 'a' in console
			message = message.replace("\u00A0", " ");
		if (type == CHAT_NONE) {
			if (username == null && message != null) {
				if (message.contains("The spell fails! You may try again in 20 seconds"))
					magic_timer = Renderer.time + 21000L;
				else if (Settings.TRAY_NOTIFS && message.contains("You have been standing here for 5 mins! Please move to a new area")) {
					NotificationsHandler.notify(NotifType.LOGOUT, "Logout Notification", "You're about to log out");
				}
			}
		} else if (type == CHAT_PRIVATE) {
			NotificationsHandler.notify(NotifType.PM, "PM from " + username, message);
		} else if (type == CHAT_TRADE_REQUEST_RECEIVED) {
			//as far as I know, this chat type is only used when receiving a trade request.
			// (message == "") is true for trade notifications... could be used if CHAT_TRADE_REQUEST_RECEIVED is used for anything else
			NotificationsHandler.notify(NotifType.TRADE, "Trade Request", username + " wishes to trade with you");
		} else if (type == CHAT_OTHER) {
			if (message.contains(" wishes to duel with you"))
				NotificationsHandler.notify(NotifType.DUEL, "Duel Request", message.replaceAll("@...@",""));
		}
		
		if (type == Client.CHAT_PRIVATE || type == Client.CHAT_PRIVATE_OUTGOING) {
			if (username != null)
				lastpm_username = username;
		}
		
		if (message.startsWith("Welcome to RuneScape!")) {
			// because this section of code is triggered when the "Welcome to RuneScape!" message first appears, we can
			// use it to do some first time set up
			if (Settings.FIRST_TIME) {
				Settings.FIRST_TIME = false;
				Settings.save();
			}
			
			// Get keybind to open the config window
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
			
			// TODO: possibly put this in welcome screen or at least _after_ "Welcome to RuneScape"
			displayMessage("@mag@Type @yel@::help@mag@ for a list of commands", CHAT_QUEST);
			displayMessage("@mag@Open the settings with @yel@" + configWindowShortcut + "@mag@ or @yel@right-click the tray icon", CHAT_QUEST);
			
			// Check for updates every login in hour intervals, so users are notified when an update is available
			long currentTime = System.currentTimeMillis();
			if (Settings.CHECK_UPDATES && currentTime >= updateTimer) {
				checkForUpdate(false);
				updateTimer = currentTime + (60 * 60 * 1000);
			}
		}
		
		if (Settings.COLORIZE) {
			AnsiConsole.systemInstall();
			System.out.println(ansi()
					.render("@|white (" + type + ")|@ " + ((username == null) ? "" : colorizeUsername(formatUsername(username, type), type)) + colorizeMessage(message, type)));
			AnsiConsole.systemUninstall();
		} else {
			System.out.println("(" + type + ") " + ((username == null) ? "" : formatUsername(username, type)) + message);
		}
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
			System.out.println("Username specified for unhandled chat type, please report this: " + type);
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
			// If username != null during CHAT_QUEST, then this is your player name, which is usually white
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
			System.out.println("Username specified for unhandled chat type, please report this: " + type);
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
		boolean blueMessage = (type == CHAT_NONE) && (colorMessage.contains("You have been standing here for 5 mins! Please move to a new area"));
		boolean yellowMessage = (type == CHAT_NONE) && (colorMessage.contains("Well Done")); //tourist trap completion
		boolean greenMessage = (type == CHAT_NONE) && (colorMessage.contains("You just advanced ") || colorMessage.contains("quest point") || colorMessage.contains("***") || colorMessage.contains("ou have completed")); //"***" is for Tourist Trap completion
		
		if (blueMessage) { // this is one of the messages which we must overwrite expected color for
			return "@|cyan,intensity_faint " + colorMessage + "|@";
		} else if (greenMessage) {
			return "@|green,intensity_bold " + colorMessage + "|@";
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
			if (colorMessage.contains(":")) {
				// this will be like "banker: would you like to access your bank account?" which should be yellow
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
		case CHAT_OTHER:
			colorMessage = "@|white " + colorReplace(colorMessage) + "|@";
			break;
		default: // this should never happen, only 8 Chat Types
			System.out.println("Unhandled chat type in colourizeMessage, please report this:" + type);
			colorMessage = "@|white,intensity_faint " + colorReplace(colorMessage) + "|@";
		}
		return colorMessage;
	}
	
	public static String colorReplace(String colorMessage) {
		final String[] colorDict = { // TODO: Make this a class variable
				// less common colors should go at the bottom b/c we can break search loop early
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
				"(?i)@or2@", "|@@|red,intensity_faint ", // these are all basically the same color, even in game
				"(?i)@or3@", "|@@|red ",
				"(?i)@blu@", "|@@|blue ",
				"(?i)@bla@", "|@@|black "
		};
		for (int i = 0; i + 1 < colorDict.length; i += 2) {
			if (!colorMessage.matches(".*@.{3}@.*")) { // if doesn't contain any color codes: break;
				break;
			}
			colorMessage = colorMessage.replaceAll(colorDict[i], colorDict[i + 1]);
		}
		
		// we could replace @.{3}@ with "" to remove "@@@@@" or "@dne@" (i.e. color code which does not exist) just like
		// in chat box, but I think it's more interesting to leave the misspelled stuff in terminal
		
		// could also respect ~xxx~ but not really useful.
		
		return colorMessage;
	}
	
	public static void drawNPC(int x, int y, int width, int height, String name, int currentHits, int maxHits) {
		// ILOAD 6 is index
		npc_list.add(new NPC(x, y, width, height, name, NPC.TYPE_MOB, currentHits, maxHits));
	}
	
	public static void drawPlayer(int x, int y, int width, int height, String name, int currentHits, int maxHits) {
		npc_list.add(new NPC(x, y, width, height, name, NPC.TYPE_PLAYER, currentHits, maxHits));
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
		// TODO: Consider using a final variable to store corresponding values since this is called a lot
		float xp = 0.0f;
		for (int x = 1; x < level; x++)
			xp += Math.floor(x + 300 * Math.pow(2, x / 7.0f)) / 4.0f;
		return xp;
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
	
	/**
	 * Returns the user's current level in a specified skill. This number is affected by skills boosts and debuffs.
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
		for (int i = 0; i < 18; i++)
			total += Client.base_level[i];
		return total;
	}
	
	/**
	 * Returns the sum of the XP of the user's skills.
	 * 
	 * @return the user's total XP
	 */
	public static float getTotalXP() {
		float xp = 0;
		for (int i = 0; i < 18; i++)
			xp += getXP(i);
		return xp;
	}
	
	/**
	 * Returns the user's base level in a specified skill. This number is <b>not</b> affected by skills boosts and
	 * debuffs.
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
		return (float)xp[skill] / 4.0f;
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
		return (float)(fatigue * 100.0 / 750);
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
			if (friends[i] != null && friends[i].equals(name))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns if the user is currently in combat. Recently being in combat does not count as in combat.
	 * 
	 * @return if the user is in combat
	 */
	public static boolean isInCombat() {
		return combat_timer == 499;
	}
	
	/**
	 * Returns if an in-game interface, window, menu, etc. is currently displayed.
	 * 
	 * @return if an interface is showing
	 */
	public static boolean isInterfaceOpen() {
		return show_bank || show_shop || show_welcome || show_trade || show_tradeconfirm || show_duel || show_duelconfirm || show_report != 0 || show_friends != 0
				|| show_sleeping;
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
	
	/**
	 * Writes {@link #strings} to a file.
	 */
	private static void dumpStrings() {
		BufferedWriter writer = null;
		
		try {
			File file = new File(Settings.Dir.DUMP + "/strings.dump");
			writer = new BufferedWriter(new FileWriter(file));
			
			writer.write("Client:\n\n");
			for (int i = 0; i < strings.length; i++)
				writer.write(i + ": " + strings[i] + "\n");
			
			writer.close();
		} catch (Exception e) {
			try {
				writer.close();
			} catch (Exception e2) {
			}
		}
	}
	
	public static boolean[] getShowXpPerHour() {
		return showXpPerHour;
	}
	
	public static double[] getXpPerHour() {
		return xpPerHour;
	}
	
	public static double[][] getLastXpGain() {
		return lastXpGain;
	}
	
}
