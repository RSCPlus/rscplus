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

import java.applet.Applet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.fusesource.jansi.AnsiConsole;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

import Client.Settings;
import Client.TwitchIRC;

public class Client {

	public static void adaptStrings() {
		for (int i = 0; i < strings.length; i++) {
			if (strings[i].contains("Oh dear! You are dead...")) {
				strings[i] = "YOU DIED";
			}
			if (strings[i].startsWith("from:") && !Settings.SHOW_LOGINDETAILS) {
				strings[i] = "@bla@from:";
			}
			if (strings[i].startsWith("@bla@from:") && Settings.SHOW_LOGINDETAILS) {
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

		if (Settings.DEBUG)
			dumpStrings();

		// Initialize login
		init_login();

		// Skip first login screen and don't wipe user info
		login_screen = 2;
	}

	public static void update() {
		if (state == STATE_GAME) {
			// Process XP drops
			boolean dropXP = (xpdrop_state[SKILL_HP] > 0.0f);
			for (int i = 0; i < xpdrop_state.length; i++) {
				float xpGain = getXP(i) - xpdrop_state[i];
				xpdrop_state[i] += xpGain;

				if (xpGain > 0.0f && dropXP) {
					if (Settings.SHOW_XPDROPS)
						xpdrop_handler.add("+" + xpGain + " (" + skill_name[i] + ")");

					// XP/hr calculations
					// TODO: After 5-10 minutes of tracking XP, make it display a rolling average instead of a session average
					if((System.currentTimeMillis() - lastXpGain[i][1]) <= 180000) {
					// < 3 minutes since last XP drop
						lastXpGain[i][0] = xpGain + lastXpGain[i][0];
						XpPerHour[i] = 3600 * (lastXpGain[i][0]) / ((System.currentTimeMillis() - lastXpGain[i][2]) / 1000);
						lastXpGain[i][3]++;
						showXpPerHour[i] = true;
						lastXpGain[i][1] = System.currentTimeMillis();
					} else {
						lastXpGain[i][0] = xpGain;
						lastXpGain[i][1] = lastXpGain[i][2] = System.currentTimeMillis();
						lastXpGain[i][3] = 0;
						showXpPerHour[i] = false;
					}
					
					if (i == SKILL_HP && xpbar.currentSkill != -1)
						continue;

					xpbar.setSkill(i);
				}
			}
			// + Fatigue drops
			if(Settings.SHOW_FATIGUEDROPS) {
				final float actualFatigue = getActualFatigue();
				final float fatigueGain = actualFatigue - currentFatigue;
				if (fatigueGain > 0.0f && !isWelcomeScreen()) {
					xpdrop_handler.add("+" + trimNumber(fatigueGain,Settings.FATIGUE_FIGURES) + "% (Fatigue)");
					currentFatigue = actualFatigue;
				}
			}
			// Prevents a fatigue drop upon first login during a session
			if(isWelcomeScreen() && currentFatigue != getActualFatigue())
				currentFatigue = getActualFatigue();
		}
	}

	public static void init_login() {
		for (int i = 0; i < xpdrop_state.length; i++)
			xpdrop_state[i] = 0.0f;

		Camera.init();
		state = STATE_LOGIN;

		twitch.disconnect();

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

	public static void getPlayerName() {

		try {
			player_name = (String) Reflection.characterName.get(player_object);
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	public static void getCoords() {
		try {
			//int coordX = Reflection.characterPosX.getInt(player_object) / 128;
			//int coordY = Reflection.characterPosY.getInt(player_object) / 128;
			// System.out.println("("+coordX+","+coordY+")");

			System.out.println("Region x: "+regionX);
			System.out.println("Region y: "+regionY);


		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static String processChatCommand(String line) {
		if (TwitchIRC.isUsing() && line.startsWith("/")) {
			String message = line.substring(1, line.length());
			String messageArray[] = message.split(" ");

			message = processClientChatCommand(message);

			if (messageArray[0] != null && messageArray[0].equals("me") && messageArray.length > 1) {
				message = message.substring(3, message.length());
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

	public static String processPrivateCommand(String line) {
		line = processClientChatCommand(line);
		return line;
	}

	private static String processClientCommand(String line) {
		if (line.startsWith("::")) {
			String commandArray[] = line.substring(2, line.length()).toLowerCase().split(" ");
			String command = commandArray[0];

			if (command.equals("toggleroofs"))
				Settings.toggleHideRoofs();
			else if (command.equals("togglecombat"))
				Settings.toggleCombatMenu();
			else if (command.equals("togglecolor"))
				Settings.toggleColorTerminal();
			else if (command.equals("togglehitbox"))
				Settings.toggleShowHitbox();
			else if (command.equals("togglefatigue"))
				Settings.toggleFatigueAlert();
			else if (command.equals("toggletwitch"))
				Settings.toggleTwitchHide();
			else if (command.equals("toggleplayerinfo"))
				Settings.toggleShowPlayerInfo();
			else if (command.equals("togglefriendinfo"))
				Settings.toggleShowFriendInfo();
			else if (command.equals("togglenpcinfo"))
				Settings.toggleShowNPCInfo();
			else if (command.equals("toggleiteminfo"))
				Settings.toggleShowItemInfo();
			else if (command.equals("togglelogindetails"))
				Settings.toggleShowLoginDetails();
			else if (command.equals("screenshot"))
				Renderer.takeScreenshot();
			else if (command.equals("debug"))
				Settings.toggleDebug();
			else if (command.equals("togglexpdrops"))
				Settings.toggleXpDrops();
			else if (command.equals("togglefatiguedrops"))
				Settings.toggleFatigueDrops();
			else if (command.equals("fov") && commandArray.length > 1)
				Camera.setFoV(Integer.parseInt(commandArray[1]));
			else if (command.equals("logout"))
				Client.logout();
			else if (command.equals("toggleinvcount"))
				Settings.toggleInvCount();
			else if (command.equals("togglestatusdisplay"))
				Settings.toggleStatusDisplay();

			if (commandArray[0] != null)
				return "::";
		}

		return line;
	}

	private static String processClientChatCommand(String line) {
		if (line.startsWith("::")) {
			String command = line.substring(2, line.length()).toLowerCase();

			if (command.equals("total"))
				return "My Total Level is " + getTotalLevel() + " (" + getTotalXP() + " xp).";
			else
			if (command.equals("fatigue")) {
				return "My Fatigue is at " + currentFatigue + "%.";
			}
			else
			
			if (command.equals("cmb")) { //this command breaks character limits and might be bannable... would not recommend sending this command over PM to rs2/rs3
				return "@whi@My Combat is Level " 
					    + "@gre@" +
					    		(
								(base_level[SKILL_ATTACK] + base_level[SKILL_STRENGTH] + base_level[SKILL_DEFENSE] + base_level[SKILL_HP])*0.25 //basic melee stats
								+ ((base_level[SKILL_ATTACK]+base_level[SKILL_STRENGTH]) < base_level[SKILL_RANGED]*1.5 ? base_level[SKILL_RANGED]*0.25 : 0) //add ranged levels if ranger
								+ (base_level[SKILL_PRAYER] + base_level[SKILL_MAGIC])*0.125 //prayer and mage
								)
					    + " @lre@A:@whi@ " + base_level[SKILL_ATTACK]
					    + " @lre@S:@whi@ " + base_level[SKILL_STRENGTH]
					    + " @lre@D:@whi@ " + base_level[SKILL_DEFENSE]
					    + " @lre@H:@whi@ " + base_level[SKILL_HP]
					    + " @lre@R:@whi@ " + base_level[SKILL_RANGED]
					    + " @lre@P:@whi@ " + base_level[SKILL_PRAYER]
					    + " @lre@M:@whi@ " + base_level[SKILL_MAGIC];
			}
			
			else
			if (command.equals("cmbnocolor")) { //this command stays within character limits and is safe.
				return "My Combat is Level "
						+  (
							(base_level[SKILL_ATTACK] + base_level[SKILL_STRENGTH] + base_level[SKILL_DEFENSE] + base_level[SKILL_HP])*0.25 //basic melee stats
							+ ((base_level[SKILL_ATTACK]+base_level[SKILL_STRENGTH]) < base_level[SKILL_RANGED]*1.5 ? base_level[SKILL_RANGED]*0.25 : 0) //add ranged levels if ranger
							+ (base_level[SKILL_PRAYER] + base_level[SKILL_MAGIC])*0.125 //prayer and mage
							)
						+ " A:" + base_level[SKILL_ATTACK]
						+ " S:" + base_level[SKILL_STRENGTH]
						+ " D:" + base_level[SKILL_DEFENSE]
						+ " H:" + base_level[SKILL_HP]
						+ " R:" + base_level[SKILL_RANGED]
						+ " P:" + base_level[SKILL_PRAYER]
						+ " M:" + base_level[SKILL_MAGIC];
			}
			else
			if (command.equals("bank")) { 
				return "Hey, everyone, I just tried to do something very silly!";
			}
			else
			if (command.startsWith("next_")) {
				for (int i = 0; i < 18; i++) {
					if (command.equals("next_" + skill_name[i].toLowerCase())) {
						final float neededXp = base_level[i] == 99 ? 0 : getXPforLevel(base_level[i] + 1) - getXP(i);
						return "I need " + neededXp + " more xp for " + (base_level[i] == 99 ? 99 : base_level[i] + 1)
								+ " " + skill_name[i] + ".";
					}
				}
			}

			for (int i = 0; i < 18; i++) {
				if (command.equals(skill_name[i].toLowerCase()))
					return "My " + skill_name[i] + " level is " + base_level[i] + " (" + getXP(i) + " xp).";
			}
		}

		return line;
	}

	public static void displayMessage(String message, int chat_type) {
		if (Client.state != Client.STATE_GAME || Reflection.displayMessage == null)
			return;

		try {
			Reflection.displayMessage.invoke(Client.instance, false, null, 0, message, chat_type, 0, null, null);
		} catch (Exception e) {
		}
	}

	public static void setLoginMessage(String line1, String line2) {
		if (Reflection.setLoginText == null)
			return;

		try {
			Reflection.setLoginText.invoke(Client.instance, (byte) -49, line2, line1);
		} catch (Exception e) {
		}
	}

	public static void logout() {
		if (Reflection.logout == null)
			return;

		try {
			Reflection.logout.invoke(Client.instance, 0);
		} catch (Exception e) {
		}
	}

	// All messages added to chat are routed here
	public static void messageHook(String username, String message, int type) {
		if (type == CHAT_NONE) {
			if (username == null && message != null
					&& message.contains("The spell fails! You may try again in 20 seconds"))
				magic_timer = Renderer.time + 21000L;
		}
		else
		if (type == Client.CHAT_PRIVATE || type == Client.CHAT_PRIVATE_OUTGOING) {
			if (username != null)
				lastpm_username = username;
		}
		
		if (Settings.COLORIZE) { //no nonsense for those who don't want it
			AnsiConsole.systemInstall();	
			System.out.println(ansi().render("@|white (" + type + ")|@ " + ((username == null) ? "" : colorizeUsername(username, type)) + colorizeMessage(message, type)));
			AnsiConsole.systemUninstall();
		} else {
			System.out.println("(" + type + ") " + ((username == null) ? "" : username + ": ") + message);
		}
	}
	
	public static String colorizeUsername(String colorMessage, int type) {
		switch (type) {
			case CHAT_PRIVATE:
				colorMessage = "@|cyan,intensity_bold "  + colorMessage + " tells you: |@"; //Username tells you:
				break;
			case CHAT_PRIVATE_OUTGOING:
				colorMessage = "@|cyan,intensity_bold You tell " + colorMessage + ": |@"; //You tell Username:
				break;
			case CHAT_QUEST:
				colorMessage = "@|white,intensity_faint " + colorMessage + ": |@"; //If username != null during CHAT_QUEST, then this is your player name, which is usually white
				break;
			case CHAT_CHAT:
				colorMessage = "@|yellow,intensity_bold " + colorMessage + ": |@"; //just bold username for chat
				break;
			case CHAT_PLAYER_INTERRACT_IN: //happens when player trades you
				colorMessage = "@|white " + colorMessage + " wishes to trade with you.|@";
				break;
			/*// username will not appear in these chat types, but just to cover it I'm leaving code commented out here
			case CHAT_NONE:
			case CHAT_PRIVATE_LOG_IN_OUT:
			case CHAT_PLAYER_INTERRACT_OUT:
			*/
				
			default:
				System.out.println("Username specified for unhandled chat type, please report this: " + type);
				colorMessage = "@|white,intensity_bold " + colorMessage + ": |@";
		}
		return colorMessage;
	}
	public static String colorizeMessage(String colorMessage, int type) {
		
		boolean whiteMessage = (colorMessage.contains("Welcome to RuneScape!")); //want this to be bold
		boolean blueMessage = (colorMessage.contains("You have been standing here for 5 mins! Please move to a new area"));
		//boolean yellowMessage = false;
		boolean greenMessage = (colorMessage.contains("You just advanced "));
		
		if (blueMessage) { //this is one of the messages which we must overwrite expected color for
			return "@|cyan,intensity_faint " + colorMessage + "|@";
		} else if (greenMessage) {
			return "@|green,intensity_bold " + colorMessage + "|@";
		} else if (whiteMessage) {
			return "@|white,intensity_bold " + colorMessage + "|@";
		}
		
		switch (type) {
			case CHAT_NONE:
				colorMessage = "@|white " + colorReplace(colorMessage) + "|@"; //have to replace b/c @cya@Screenshot saved...
				break;
			case CHAT_PRIVATE:
			case CHAT_PRIVATE_OUTGOING:
				colorMessage = "@|cyan,intensity_faint " + colorReplace(colorMessage) + "|@"; //message to/from PMs
				break;
			case CHAT_QUEST:
				if (colorMessage.contains(":")) {
					colorMessage = "@|yellow,intensity_faint " + colorMessage + "|@"; //this will be like "banker: would you like to access your bank account?" which should be yellow
				} else {
					colorMessage = "@|white,intensity_faint " + colorMessage + "|@"; //this is usually skilling
				}
				break;
			case CHAT_CHAT:
				colorMessage = "@|yellow,intensity_faint " + colorReplace(colorMessage) + "|@";
				break;
			case CHAT_PRIVATE_LOG_IN_OUT:
				colorMessage = "@|cyan,intensity_faint " + colorMessage + "|@"; //don't need to colorReplace, this is just "username has logged in/out"
				break;
			case CHAT_PLAYER_INTERRACT_IN:
			case CHAT_PLAYER_INTERRACT_OUT:
				colorMessage = "@|white " + colorReplace(colorMessage) + "|@";
				break;
			default: //this should never happen, only 8 Chat Types
				System.out.println("Unhandled chat type in colourizeMessage, please report this:" + type);
				colorMessage = "@|white,intensity_faint " + colorReplace(colorMessage) + "|@";
		}
		return colorMessage;
	}
	public static String colorReplace(String colorMessage) {
		String[] colorDict = {"(?i)@cya@","|@@|cyan ", //less common colors should go at the bottom b/c we can break search loop early
							  "(?i)@whi@","|@@|white ",
		                      "(?i)@red@","|@@|red ",
		                      "(?i)@gre@","|@@|green ",
		                      "(?i)@lre@","|@@|red,intensity_faint ",
		                      "(?i)@dre@","|@@|red,intensity_bold ",
		                      "(?i)@ran@","|@@|red,blink_fast ", //consider handling this specially
		                      "(?i)@yel@","|@@|yellow ",
		                      "(?i)@mag@","|@@|magenta,intensity_bold ",
		                      "(?i)@gr1@","|@@|green ",
		                      "(?i)@gr2@","|@@|green ",
		                      "(?i)@ora@","|@@|red,intensity_faint ",
		                      "(?i)@or1@","|@@|red,intensity_faint ",
		                      "(?i)@or2@","|@@|red,intensity_faint ", //these are all basically the same color, even in game
		                      "(?i)@blu@","|@@|blue ",
		                      "(?i)@bla@","|@@|black "
		                      };
		for (int i=0; i+1 < colorDict.length; i+=2)
		{
			if (!colorMessage.matches(".*@.{3}@.*")){ //if doesn't contain any color codes: break;
				break;
			}
			colorMessage = colorMessage.replaceAll(colorDict[i], colorDict[i+1]);
		}
		
		//we could replace @.{3}@ with "" to remove "@@@@@" or "@dne@" (i.e. color code which does not exist) just like in chat box,
		//but I think it's more interesting to leave the misspelled stuff in terminal 
		
		//could also respect ~xxx~ but not really useful.
		
		return colorMessage;
	}
	
	public static void drawNPC(int x, int y, int width, int height, String name) {
		// ILOAD 6 is index
		npc_list.add(new NPC(x, y, width, height, name, NPC.TYPE_MOB));
	}

	public static void drawPlayer(int x, int y, int width, int height, String name) {
		npc_list.add(new NPC(x, y, width, height, name, NPC.TYPE_PLAYER));
	}

	public static void drawItem(int x, int y, int width, int height, int id) {
		item_list.add(new Item(x, y, width, height, id));
	}

	public static float getXPforLevel(int level) {
		float xp = 0.0f;
		for (int x = 1; x < level; x++)
			xp += Math.floor(x + 300 * Math.pow(2, x / 7.0f)) / 4.0f;
		return xp;
	}

	public static float getXPUntilLevel(int skill) {
		float xpNextLevel = getXPforLevel(base_level[skill] + 1);
		return xpNextLevel - getXP(skill);
	}

	public static int getLevel(int i) {
		return current_level[i];
	}

	public static int getTotalLevel() {
		int total = 0;
		for (int i = 0; i < 18; i++)
			total += Client.base_level[i];
		return total;
	}

	public static float getTotalXP() {
		float xp = 0;
		for (int i = 0; i < 18; i++)
			xp += getXP(i);
		return xp;
	}

	public static int getBaseLevel(int i) {
		return base_level[i];
	}

	public static float getXP(int i) {
		return (float) xp[i] / 4.0f;
	}

	public static int getFatigue() {
		return (fatigue * 100 / 750);
	}

	public static float getActualFatigue() {
		return (float) (fatigue * 100.0 / 750);
	}
	
	public static Double trimNumber(double num, int figures) {
		return Math.round(num*Math.pow(10,figures))/Math.pow(10,figures);
	}

	public static void updateCurrentFatigue() {
		final float nextFatigue = getActualFatigue();
		if (currentFatigue != nextFatigue) {
			currentFatigue = nextFatigue;
		}
	}

	public static boolean isFriend(String name) {
		for (int i = 0; i < friends_count; i++) {
			if (friends[i] != null && friends[i].equals(name))
				return true;
		}

		return false;
	}

	public static boolean isInCombat()
	{
		return (combat_timer == 499);
	}

	public static boolean isInterfaceOpen() {
		return (show_bank || show_shop || show_welcome || show_trade || show_tradeconfirm || show_duel
				|| show_duelconfirm || show_report != 0 || show_friends != 0 || show_sleeping);
	}

	public static boolean isSleeping() {
		return show_sleeping;
	}

	public static boolean isWelcomeScreen() {
		return show_welcome;
	}

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

	public static List<NPC> npc_list = new ArrayList<NPC>();
	public static List<Item> item_list = new ArrayList<Item>();

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
	public static final int CHAT_PLAYER_INTERRACT_IN = 6;  //used for when player sends you a trade request. If player sends you a duel request it's type 7 for some reason...
	public static final int CHAT_PLAYER_INTERRACT_OUT = 7; //used for when you send a player a duel, trade request, or follow

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

	public static int inventory_items[];

	public static int fatigue;
	private static float currentFatigue;
	public static int current_level[];
	public static int base_level[];
	public static int xp[];
	public static String skill_name[];
	public static int combat_style;

	public static int friends_count;
	public static String friends[];

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

	public static int regionX = -1;
	public static int regionY = -1;

	public static String strings[];

	public static XPDropHandler xpdrop_handler = new XPDropHandler();
	public static XPBar xpbar = new XPBar();

	// Game's client instance
	public static Object instance;

	private static TwitchIRC twitch = new TwitchIRC();
	private static MouseHandler handler_mouse;
	private static KeyboardHandler handler_keyboard;
	private static float xpdrop_state[] = new float[18];
	
	public static boolean showXpPerHour[] = new boolean[18];
	public static double XpPerHour[] = new double[18];
	
	// [[skill1, skill2, skill3, ...], [totalXpGainInSample, mostRecentXpDropTime, initialTimeInSample, sampleSize]]
	// sampleSize + 1 is the actual sample size
	public static double lastXpGain[][] = new double[18][4];
}
