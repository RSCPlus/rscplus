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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import Client.KeybindSet.KeyModifier;
import Game.Camera;
import Game.Client;
import Game.Game;
import Game.KeyboardHandler;
import Game.Renderer;

public class Settings
{
	//Internally used variables
	public static boolean fovUpdateRequired;
	public static boolean versionCheckRequired = true;
	public static double VERSION_NUMBER = 20170402.075627;
		//The version number ^^^^ follows ISO 8601 yyyyMMdd.HHmmss
		//The version number will actually be read from this source file, so please don't change the name of this variable and keep the assignment near the top for scanning.
		//This variable can be set automatically by ant by issuing `ant setversion`
		//	before you push your changes, so there's no need to update it manually.
	
	public static void initDir() {
		// Find JAR directory
		Dir.JAR = ".";
		try
		{
			Dir.JAR = Settings.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			int indexFileSep1 = Dir.JAR.lastIndexOf("/");
			int indexFileSep2 = Dir.JAR.lastIndexOf("\\");
			int index = (indexFileSep1>indexFileSep2)?indexFileSep1:indexFileSep2;
			if(index != -1)
				Dir.JAR = Dir.JAR.substring(0, index);
		}
		catch(Exception e) {}

		Logger.Info("Jar Location: " + Dir.JAR);

		// Load other directories
		Dir.CACHE = Dir.JAR + "/cache";
		Util.MakeDirectory(Dir.CACHE);
		Dir.SCREENSHOT = Dir.JAR + "/screenshots";
		Util.MakeDirectory(Dir.SCREENSHOT);
	}
	
	public static void Load()
	{
		// Load settings
		try
		{
			Properties props = new Properties();
			FileInputStream in = new FileInputStream(Dir.JAR + "/config.ini");
			props.load(in);
			in.close();
			
			//General options
			CUSTOM_CLIENT_SIZE = getBoolean(props, "custom_client_size", CUSTOM_CLIENT_SIZE);
			CUSTOM_CLIENT_SIZE_X = getInt(props, "custom_client_size_x", CUSTOM_CLIENT_SIZE_X);
			CUSTOM_CLIENT_SIZE_Y = getInt(props, "custom_client_size_y", CUSTOM_CLIENT_SIZE_Y);
			LOAD_CHAT_HISTORY = getBoolean(props, "load_chat_history", LOAD_CHAT_HISTORY);
			COMBAT_MENU = getBoolean(props, "combat_menu", COMBAT_MENU);
			SHOW_XPDROPS = getBoolean(props, "show_xpdrops", SHOW_XPDROPS);
			SHOW_FATIGUEDROPS = getBoolean(props, "show_fatiguedrops", SHOW_FATIGUEDROPS);
			FATIGUE_FIGURES = getInt(props, "fatigue_figures", FATIGUE_FIGURES);
			FATIGUE_ALERT = getBoolean(props, "fatigue_alert", FATIGUE_ALERT);
			INVENTORY_FULL_ALERT = getBoolean(props, "inventory_full_alert", INVENTORY_FULL_ALERT);
			NAME_PATCH_TYPE = getInt(props, "name_patch_type", NAME_PATCH_TYPE);
			HIDE_ROOFS = getBoolean(props, "hide_roofs", HIDE_ROOFS);
			COLORIZE = getBoolean(props, "colorize", COLORIZE);
			FOV = getInt(props, "fov", FOV);
			SOFTWARE_CURSOR = getBoolean(props, "software_cursor", SOFTWARE_CURSOR);
			VIEW_DISTANCE = getInt(props, "view_distance", VIEW_DISTANCE);

			//Overlays options
			SHOW_STATUSDISPLAY = getBoolean(props, "show_statusdisplay", SHOW_STATUSDISPLAY);
			SHOW_INVCOUNT = getBoolean(props, "show_invcount", SHOW_INVCOUNT);
			SHOW_ITEMINFO = getBoolean(props, "show_iteminfo", SHOW_ITEMINFO);
			SHOW_PLAYERINFO = getBoolean(props, "show_playerinfo", SHOW_PLAYERINFO);
			SHOW_FRIENDINFO = getBoolean(props, "show_friendinfo", SHOW_FRIENDINFO);
			SHOW_NPCINFO = getBoolean(props, "show_npcinfo", SHOW_NPCINFO);
			SHOW_HITBOX = getBoolean(props, "show_hitbox", SHOW_HITBOX);
			SHOW_FOOD_HEAL_OVERLAY = getBoolean(props, "show_food_heal_overlay", SHOW_FOOD_HEAL_OVERLAY);
			SHOW_TIME_UNTIL_HP_REGEN = getBoolean(props, "show_time_until_hp_regen", SHOW_TIME_UNTIL_HP_REGEN);
			DEBUG = getBoolean(props, "debug", DEBUG);

			//Notifications options
			TRAY_NOTIFS = getBoolean(props, "tray_notifs", TRAY_NOTIFS);
			TRAY_NOTIFS_ALWAYS = getBoolean(props, "tray_notifs_always", TRAY_NOTIFS_ALWAYS);
			NOTIFICATION_SOUNDS = getBoolean(props, "notification_sounds", NOTIFICATION_SOUNDS);
			SOUND_NOTIFS_ALWAYS = getBoolean(props, "sound_notifs_always", SOUND_NOTIFS_ALWAYS);
			USE_SYSTEM_NOTIFICATIONS = getBoolean(props, "use_system_notifications", USE_SYSTEM_NOTIFICATIONS);
			PM_NOTIFICATIONS = getBoolean(props, "pm_notifications", PM_NOTIFICATIONS);
			TRADE_NOTIFICATIONS = getBoolean(props, "trade_notifications", TRADE_NOTIFICATIONS);
			DUEL_NOTIFICATIONS = getBoolean(props, "duel_notifications", DUEL_NOTIFICATIONS);
			LOGOUT_NOTIFICATIONS = getBoolean(props, "logout_notifications", LOGOUT_NOTIFICATIONS);
			LOW_HP_NOTIFICATIONS = getBoolean(props, "low_hp_notifications", LOW_HP_NOTIFICATIONS);
			LOW_HP_NOTIF_VALUE = getInt(props, "low_hp_notif_value", LOW_HP_NOTIF_VALUE);
			FATIGUE_NOTIFICATIONS = getBoolean(props, "fatigue_notifications", FATIGUE_NOTIFICATIONS);
			FATIGUE_NOTIF_VALUE = getInt(props, "fatigue_notif_value", FATIGUE_NOTIF_VALUE);

			//Streaming & Privacy
			TWITCH_HIDE = getBoolean(props, "twitch_hide", TWITCH_HIDE);
			TWITCH_CHANNEL = getString(props, "twitch_channel", TWITCH_CHANNEL);
			TWITCH_OAUTH = getString(props, "twitch_oauth", TWITCH_OAUTH);
			TWITCH_USERNAME = getString(props, "twitch_username", TWITCH_USERNAME);
			SHOW_LOGINDETAILS = getBoolean(props, "show_logindetails", SHOW_LOGINDETAILS);
			SAVE_LOGININFO = getBoolean(props, "save_logininfo", SAVE_LOGININFO);

			//Miscellaneous settings (No GUI)
			WORLD = getInt(props, "world", 2);
			COMBAT_STYLE = getInt(props, "combat_style", Client.COMBAT_AGGRESSIVE);
			FIRST_TIME = getBoolean(props, "first_time", FIRST_TIME);
			DISASSEMBLE = getBoolean(props, "disassemble", false);
			DISASSEMBLE_DIRECTORY = getString(props, "disassemble_directory", "dump");
			
			if (CUSTOM_CLIENT_SIZE_X < 512) {
				CUSTOM_CLIENT_SIZE_X = 512;
			}
			if (CUSTOM_CLIENT_SIZE_Y < 346) {
				CUSTOM_CLIENT_SIZE_Y = 346;
			}

			if(WORLD < 1)
				WORLD = 1;
			else if(WORLD > 5)
				WORLD = 5;

			if(VIEW_DISTANCE < 2300)
			{
				VIEW_DISTANCE = 2300;
				Save();
			}
			else if(VIEW_DISTANCE > 10000)
			{
				VIEW_DISTANCE = 10000;
				Save();
			}

			if(COMBAT_STYLE < Client.COMBAT_CONTROLLED)
			{
				COMBAT_STYLE = Client.COMBAT_CONTROLLED;
				Save();
			}
			else if(COMBAT_STYLE > Client.COMBAT_DEFENSIVE)
			{
				COMBAT_STYLE = Client.COMBAT_DEFENSIVE;
				Save();
			}

			if(NAME_PATCH_TYPE < 0)
				NAME_PATCH_TYPE = 0;
			else if(NAME_PATCH_TYPE > 3)
				NAME_PATCH_TYPE = 3;
			
			if(FATIGUE_FIGURES < 1)
				FATIGUE_FIGURES = 1;
			else if(FATIGUE_FIGURES > 7)
				FATIGUE_FIGURES = 7;
			
			
			//Keybinds
			for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
				String keybindCombo = getString(props, "key_" + kbs.commandName, "" + kbs.modifier + "*" + kbs.key);
				kbs.modifier = getKeyModifierFromString(keybindCombo);
				kbs.key = Integer.parseInt(keybindCombo.substring(2));
			}
		}
		catch(Exception e) {}

		if(DISASSEMBLE)
		{
			Dir.DUMP = Dir.JAR + "/" + DISASSEMBLE_DIRECTORY;
			Util.MakeDirectory(Dir.DUMP);
		}
		
	}
	
	private static KeyModifier getKeyModifierFromString(String savedKeybindSet) {
		switch(Integer.parseInt(savedKeybindSet.substring(0,1))) {
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

	public static void Save()
	{
		try
		{
			Properties props = new Properties();

			//General
			props.setProperty("custom_client_size", "" + CUSTOM_CLIENT_SIZE);
			props.setProperty("custom_client_size_x", "" + CUSTOM_CLIENT_SIZE_X);
			props.setProperty("custom_client_size_y", "" + CUSTOM_CLIENT_SIZE_Y);
			props.setProperty("load_chat_history", "" + LOAD_CHAT_HISTORY);
			props.setProperty("combat_menu", "" + COMBAT_MENU);
			props.setProperty("show_xpdrops", "" + SHOW_XPDROPS);
			props.setProperty("show_fatiguedrops", "" + SHOW_FATIGUEDROPS);
			props.setProperty("fatigue_figures", "" + FATIGUE_FIGURES);
			props.setProperty("fatigue_alert", "" + FATIGUE_ALERT);
			props.setProperty("inventory_full_alert", "" + INVENTORY_FULL_ALERT);
			props.setProperty("name_patch_type", "" + NAME_PATCH_TYPE);
			props.setProperty("hide_roofs", "" + HIDE_ROOFS);
			props.setProperty("colorize", "" + COLORIZE);
			props.setProperty("fov", "" + FOV);
			props.setProperty("software_cursor", "" + SOFTWARE_CURSOR);
			props.setProperty("view_distance", "" + VIEW_DISTANCE);

			//Overlays
			props.setProperty("show_statusdisplay", "" + SHOW_STATUSDISPLAY);
			props.setProperty("show_invcount", "" + SHOW_INVCOUNT);
			props.setProperty("show_iteminfo", "" + SHOW_ITEMINFO);
			props.setProperty("show_playerinfo", "" + SHOW_PLAYERINFO);
			props.setProperty("show_friendinfo", "" + SHOW_FRIENDINFO);
			props.setProperty("show_npcinfo", "" + SHOW_NPCINFO);
			props.setProperty("show_hitbox", "" + SHOW_HITBOX);
			props.setProperty("show_food_heal_overlay", "" + SHOW_FOOD_HEAL_OVERLAY);
			props.setProperty("show_time_until_hp_regen", "" + SHOW_TIME_UNTIL_HP_REGEN);
			props.setProperty("debug", "" + DEBUG);

			//Notifications
			props.setProperty("tray_notifs", "" + TRAY_NOTIFS);
			props.setProperty("tray_notifs_always", "" + TRAY_NOTIFS_ALWAYS);
			props.setProperty("notification_sounds", "" + NOTIFICATION_SOUNDS);
			props.setProperty("sound_notifs_always", "" + SOUND_NOTIFS_ALWAYS);
			props.setProperty("use_system_notifications", "" + USE_SYSTEM_NOTIFICATIONS);
			props.setProperty("pm_notifications", "" + PM_NOTIFICATIONS);
			props.setProperty("trade_notifications", "" + TRADE_NOTIFICATIONS);
			props.setProperty("duel_notifications", "" + DUEL_NOTIFICATIONS);
			props.setProperty("logout_notifications", "" + LOGOUT_NOTIFICATIONS);
			props.setProperty("low_hp_notifications", "" + LOW_HP_NOTIFICATIONS);
			props.setProperty("low_hp_notif_value", "" + LOW_HP_NOTIF_VALUE);
			props.setProperty("fatigue_notifications", "" + FATIGUE_NOTIFICATIONS);
			props.setProperty("fatigue_notif_value", "" + FATIGUE_NOTIF_VALUE);

			//Streaming & Privacy
			props.setProperty("twitch_hide", "" + TWITCH_HIDE);
			props.setProperty("twitch_channel", "" + TWITCH_CHANNEL);
			props.setProperty("twitch_oauth", "" + TWITCH_OAUTH);
			props.setProperty("twitch_username", "" + TWITCH_USERNAME);
			props.setProperty("show_logindetails", "" + SHOW_LOGINDETAILS);
			props.setProperty("save_logininfo", "" + SAVE_LOGININFO);

			//Miscellaneous settings (No GUI);
			props.setProperty("world", "" + WORLD);
			props.setProperty("combat_style", "" + COMBAT_STYLE);
			props.setProperty("first_time", "" + false); //This is set to false, as logically, saving the config would imply this is not a first-run.
			props.setProperty("disassemble", "" + DISASSEMBLE);
			props.setProperty("disassemble_directory", "" + DISASSEMBLE_DIRECTORY);

			//Keybinds
			for (KeybindSet kbs : KeyboardHandler.keybindSetList) {
				props.setProperty("key_" + kbs.commandName, "" + getIntForKeyModifier(kbs) + "*" + kbs.key);
			}
			
			FileOutputStream out = new FileOutputStream(Dir.JAR + "/config.ini");
			props.store(out, "---rscplus config---");
			out.close();
		}
		catch(Exception e)
		{
			Logger.Error("Unable to save settings");
		}
	}
	
	private static int getIntForKeyModifier(KeybindSet kbs) {
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

	public static URL getResource(String fname)
	{
		URL url = null;
		try
		{
			url = Game.getInstance().getClass().getResource(fname);
		}
		catch(Exception e) {}

		if(url == null)
		{
			try
			{
				url = new URL("file://" + Dir.JAR + "/.." + fname);
			}
			catch(Exception e) { e.printStackTrace(); }
		}

		Logger.Info("Loading resource: " + fname);

		return url;
	}

	public static InputStream getResourceAsStream(String fname)
	{
		InputStream stream = null;
		try
		{
			stream = Game.getInstance().getClass().getResourceAsStream(fname);
		}
		catch(Exception e) {}

		if(stream == null)
		{
			try
			{
				stream = new FileInputStream(Dir.JAR + "/.." + fname);
			}
			catch(Exception e) { e.printStackTrace(); }
		}

		Logger.Info("Loading resource as stream: " + fname);

		return stream;
	}

	public static void toggleHideRoofs()
	{
		HIDE_ROOFS = !HIDE_ROOFS;
		if(HIDE_ROOFS)
			Client.displayMessage("@cya@Roofs are now hidden", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Roofs are now shown", Client.CHAT_NONE);
		Save();
	}

	public static void toggleCombatMenu()
	{
		COMBAT_MENU = !COMBAT_MENU;
		if(COMBAT_MENU)
			Client.displayMessage("@cya@Combat style is now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Combat style is now hidden", Client.CHAT_NONE);
		Save();
	}

	public static void toggleShowFriendInfo()
	{
		SHOW_FRIENDINFO = !SHOW_FRIENDINFO;
		if(Settings.SHOW_FRIENDINFO)
			Client.displayMessage("@cya@Friend info is now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Friend info is now hidden", Client.CHAT_NONE);
		Save();
	}
	
	public static void toggleInvCount()
	{
		SHOW_INVCOUNT = !SHOW_INVCOUNT;
		if(Settings.SHOW_INVCOUNT)
			Client.displayMessage("@cya@Inventory count is now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Inventory count is now hidden", Client.CHAT_NONE);
		Save();
	}
	
	public static void toggleStatusDisplay()
	{
		SHOW_STATUSDISPLAY= !SHOW_STATUSDISPLAY;
		if(Settings.SHOW_STATUSDISPLAY)
			Client.displayMessage("@cya@Status display is now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Status display is now hidden", Client.CHAT_NONE);
		Save();
	}

	public static void toggleShowHitbox()
	{
		SHOW_HITBOX = !SHOW_HITBOX;
		if(SHOW_HITBOX)
			Client.displayMessage("@cya@Hitboxes are now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Hitboxes are now hidden", Client.CHAT_NONE);
		Save();
	}

	public static void toggleShowItemInfo()
	{
		SHOW_ITEMINFO = !SHOW_ITEMINFO;
		if(Settings.SHOW_ITEMINFO)
			Client.displayMessage("@cya@Item info is now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Item info is now hidden", Client.CHAT_NONE);
		Save();
	}

	public static void toggleShowNPCInfo()
	{
		SHOW_NPCINFO = !SHOW_NPCINFO;
		if(Settings.SHOW_NPCINFO)
			Client.displayMessage("@cya@NPC info is now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@NPC info is now hidden", Client.CHAT_NONE);
		Save();
	}
	
	public static void toggleShowPlayerInfo()
	{
		SHOW_PLAYERINFO = !SHOW_PLAYERINFO;
		if(Settings.SHOW_PLAYERINFO)
			Client.displayMessage("@cya@Player info is now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Player info is now hidden", Client.CHAT_NONE);
		Save();
	}
	
	public static void toggleShowLoginDetails()
	{
		SHOW_LOGINDETAILS = !SHOW_LOGINDETAILS;
		if(Settings.SHOW_LOGINDETAILS)
			Client.displayMessage("@cya@Login details will appear next time", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Login details will not appear next time", Client.CHAT_NONE);
		Save();
	}

	public static void toggleDebug()
	{
		DEBUG = !DEBUG;
		if(DEBUG)
			Client.displayMessage("@cya@Debug mode is on", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Debug mode is off", Client.CHAT_NONE);
		Save();
	}

	public static void toggleFatigueAlert()
	{
		FATIGUE_ALERT = !FATIGUE_ALERT;
		if(FATIGUE_ALERT)
			Client.displayMessage("@cya@Fatigue alert is now on", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Fatigue alert is now off", Client.CHAT_NONE);
		Save();
	}

	public static void toggleInventoryFullAlert()
	{
		INVENTORY_FULL_ALERT = !INVENTORY_FULL_ALERT;
		if(INVENTORY_FULL_ALERT)
			Client.displayMessage("@cya@Inventory full alert is now on", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Inventory full alert is now off", Client.CHAT_NONE);
		Save();
	}
	
	public static void toggleTwitchHide()
	{
		TWITCH_HIDE = !TWITCH_HIDE;
		if(TWITCH_HIDE)
			Client.displayMessage("@cya@Twitch chat is now hidden", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Twitch chat is now shown", Client.CHAT_NONE);
		Save();
	}
	
	public static void toggleXpDrops()
	{
		SHOW_XPDROPS = !SHOW_XPDROPS;
		if(SHOW_XPDROPS)
			Client.displayMessage("@cya@XP drops are now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@XP drops are now hidden", Client.CHAT_NONE);
		Save();
	}
	
	public static void toggleFatigueDrops()
	{
		SHOW_FATIGUEDROPS = !SHOW_FATIGUEDROPS;
		if(SHOW_FATIGUEDROPS)
			Client.displayMessage("@cya@Fatigue drops are now shown", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Fatigue drops are now hidden", Client.CHAT_NONE);
		Save();
	}

	public static void toggleColorTerminal()
	{
		COLORIZE = !COLORIZE;
		if(COLORIZE)
			Client.displayMessage("@cya@Colors are now shown in terminal", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Colors are now ignored in terminal", Client.CHAT_NONE);
		Save();
	}
	
	public static void checkSoftwareCursor() {
		if(Settings.SOFTWARE_CURSOR)
		{
			Game.getInstance().setCursor(Game.getInstance().getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null"));
		} else {
			Game.getInstance().setCursor(Cursor.getDefaultCursor());
		}
	}
		
	private static void toggleFoodOverlay() {
		//TODO: this toggles the variable but does nothing yet
		SHOW_FOOD_HEAL_OVERLAY = !SHOW_FOOD_HEAL_OVERLAY;
		if(SHOW_FOOD_HEAL_OVERLAY)
			Client.displayMessage("@cya@Not yet implemented, sorry!", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Not yet implemented, sorry!", Client.CHAT_NONE);
		Save();
	}
	
	private static void toggleSaveLoginInfo() {
		SAVE_LOGININFO = !SAVE_LOGININFO;
		if(SAVE_LOGININFO)
			Client.displayMessage("@cya@Saving login info enabled.", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Saving login info disabled.", Client.CHAT_NONE);
		Save();
	}
	
	private static void toggleHealthRegenTimer() {
		//TODO: this toggles the variable but does nothing yet
		SHOW_TIME_UNTIL_HP_REGEN = !SHOW_TIME_UNTIL_HP_REGEN;
		if(SHOW_TIME_UNTIL_HP_REGEN)
			Client.displayMessage("@cya@Not yet implemented, sorry!", Client.CHAT_NONE);
		else
			Client.displayMessage("@cya@Not yet implemented, sorry!", Client.CHAT_NONE);
		Save();
	}

	public static void setClientFoV(String fovValue) {
		try {
			FOV = Integer.parseInt(fovValue);
			Camera.setFoV(FOV);
			if (FOV > 10 || FOV < 8) { //if stupid fov, warn user how to get back
				Client.displayMessage("@whi@This is fun, but if you want to go back to normal, use @yel@::fov 9", Client.CHAT_QUEST);
			}
		} catch (Exception e) {
			Client.displayMessage("@whi@Please use an @lre@integer@whi@ between 7 and 16 (default = 9)", Client.CHAT_QUEST); //more sane limitation would be 8 to 10, but it's fun to play with
		}
		Save();
	}
	
	private static String getString(Properties props, String key, String def)
	{
		String value = props.getProperty(key);
		if(value == null){
				return def;
			}
		
		return value;
	}

	private static int getInt(Properties props, String key, int def)
	{
		String value = props.getProperty(key);
		if(value == null)
			return def;

		try
		{
			int intValue = Integer.parseInt(value);
			return intValue;
		}
		catch(Exception e)
		{
			return def;
		}
	}

	private static boolean getBoolean(Properties props, String key, boolean def)
	{
		String value = props.getProperty(key);
		if(value == null)
			return def;

		try
		{
			boolean boolValue = Boolean.parseBoolean(value);
			return boolValue;
		}
		catch(Exception e)
		{
			return def;
		}
	}

	public static class Dir
	{
		public static String JAR;
		public static String CACHE;
		public static String DUMP;
		public static String SCREENSHOT;
	}

	public static String WORLD_LIST[] =
	{
		"1",
		"2",
		"3",
		"4",
		"5"
	};
	
	public static void processKeybindCommand(String commandName) {
		switch (commandName) { 
		case "logout":
			if(Client.state != Client.STATE_LOGIN)
				Client.logout();
			break;
		case "screenshot":
			Renderer.takeScreenshot();
			break;
		case "toggle_colorize":
			Settings.toggleColorTerminal();
			break;
		case "toggle_combat_xp_menu":
			Settings.toggleCombatMenu();
			break;
		case "toggle_debug":
			Settings.toggleDebug();
			break;
		case "toggle_fatigue_alert":
			Settings.toggleFatigueAlert();
			break;
		case "toggle_inventory_full_alert":
			Settings.toggleInventoryFullAlert();
			break;
		case "toggle_fatigue_drops":
			Settings.toggleFatigueDrops();
			break;
		case "toggle_food_heal_overlay":
			Settings.toggleFoodOverlay();
			break;
		case "toggle_friend_name_overlay":
			Settings.toggleShowFriendInfo();
			break;
		case "toggle_hpprayerfatigue_display":
			Settings.toggleStatusDisplay();
			break;
		case "toggle_inven_count_overlay":
			Settings.toggleInvCount();
			break;
		case "toggle_ipdns":
			Settings.toggleShowLoginDetails();
			break;
		case "toggle_item_overlay":
			Settings.toggleShowItemInfo();
			break;
		case "toggle_hitboxes":
			Settings.toggleShowHitbox();
			break;
		case "toggle_npc_name_overlay":
			Settings.toggleShowNPCInfo();
			break;
		case "toggle_player_name_overlay":
			Settings.toggleShowPlayerInfo();
			break;
		case "toggle_roof_hiding":
			Settings.toggleHideRoofs();
			break;
		case "toggle_save_login_info":
			Settings.toggleSaveLoginInfo();
			break;
		case "toggle_health_regen_timer":
			Settings.toggleHealthRegenTimer();
			break;
		case "toggle_twitch_chat":
			Settings.toggleTwitchHide();
			break;
		case "toggle_xp_drops":
			Settings.toggleXpDrops();
			break;
		case "show_config_window":
			Launcher.getConfigWindow().showConfigWindow();
			break;
		case "world_1":
			if(Client.state == Client.STATE_LOGIN)
				Game.getInstance().getJConfig().changeWorld(1);
			break;
		case "world_2":
			if(Client.state == Client.STATE_LOGIN)
				Game.getInstance().getJConfig().changeWorld(2);
			break;
		case "world_3":
			if(Client.state == Client.STATE_LOGIN)
				Game.getInstance().getJConfig().changeWorld(3);
			break;
		case "world_4":
			if(Client.state == Client.STATE_LOGIN)
				Game.getInstance().getJConfig().changeWorld(4);
			break;
		case "world_5":
			if(Client.state == Client.STATE_LOGIN)
				Game.getInstance().getJConfig().changeWorld(5);
			break;
		
		default:
			Logger.Error("An unrecognized command was sent to processCommand: " + commandName);
			break;
		}
	}
	

	/*
	 * Settings Variables
	 * Note that the settings defaults are those values listed here, as the Load method now references these values as defaults.
	 * These have been ordered according to their order on the GUI, for convenience.
	 */
	
	//General options
	public static boolean CUSTOM_CLIENT_SIZE = false;
	public static int CUSTOM_CLIENT_SIZE_X = 512;
	public static int CUSTOM_CLIENT_SIZE_Y = 346;
	public static boolean LOAD_CHAT_HISTORY = false;
	public static boolean COMBAT_MENU = false;
	public static boolean SHOW_XPDROPS = true;
	public static boolean CENTER_XPDROPS = false;
	public static boolean SHOW_FATIGUEDROPS = true;
	public static int FATIGUE_FIGURES = 2;
	public static boolean FATIGUE_ALERT = true;
	public static boolean INVENTORY_FULL_ALERT = false;
	public static int NAME_PATCH_TYPE = 3;
	public static boolean HIDE_ROOFS = true;
    public static boolean COLORIZE = true; //TODO: Vague, consider refactoring for clarity
	public static int FOV = 9;
	public static boolean SOFTWARE_CURSOR = false;
	public static int VIEW_DISTANCE = 10000;
	
	//Overlays options
	public static boolean SHOW_STATUSDISPLAY = true; //TODO: PLEASE refactor to a name that isn't uselessly vague. This is apparently the HP/Prayer/Fatigue display.
	public static boolean SHOW_INVCOUNT = true;
	public static boolean SHOW_ITEMINFO = false; //TODO: Refactor to add the word 'overlay' for clarity
	public static boolean SHOW_PLAYERINFO = false; //TODO: See above
	public static boolean SHOW_FRIENDINFO = false; //TODO ^
	public static boolean SHOW_NPCINFO = false; //TODO ^
	public static boolean SHOW_HITBOX = false; //TODO: Consider refactoring for clarity that this only affects NPCs
	public static boolean SHOW_FOOD_HEAL_OVERLAY = false;
	public static boolean SHOW_TIME_UNTIL_HP_REGEN = false;
	public static boolean DEBUG = false;
	
	//Notifications options
	public static boolean TRAY_NOTIFS = true;
	public static boolean TRAY_NOTIFS_ALWAYS = false; //If false, only when client is not focused. Based on radio button.
	public static boolean NOTIFICATION_SOUNDS = !isRecommendedToUseSystemNotifs();
	public static boolean SOUND_NOTIFS_ALWAYS = false; //If false, only when client focused. Also based on radio button. TODO
	public static boolean USE_SYSTEM_NOTIFICATIONS = isRecommendedToUseSystemNotifs();
	public static boolean PM_NOTIFICATIONS = true; 
	public static boolean TRADE_NOTIFICATIONS = true;
	public static boolean DUEL_NOTIFICATIONS = true;
	public static boolean LOGOUT_NOTIFICATIONS = true;
	public static boolean LOW_HP_NOTIFICATIONS = true;
	public static int LOW_HP_NOTIF_VALUE = 25;
	public static boolean FATIGUE_NOTIFICATIONS = true;
	public static int FATIGUE_NOTIF_VALUE = 98;
	
	//Streaming & Privacy
	public static boolean TWITCH_HIDE = false; //TODO: Refactor? Vague, if it manages chat visibility
	public static String TWITCH_CHANNEL = "";
	public static String TWITCH_OAUTH = "";
	public static String TWITCH_USERNAME = "";
	public static boolean SHOW_LOGINDETAILS = true; //TODO: Consider refactoring for clarity. This determines if IP/DNS details are shown at login welcome screen
	public static boolean SAVE_LOGININFO = true;
	
	//Miscellaneous settings (No GUI)
	public static int COMBAT_STYLE = Client.COMBAT_AGGRESSIVE;
	public static int WORLD = 2;
	public static boolean FIRST_TIME = true;
	public static boolean DISASSEMBLE = false;
	public static String DISASSEMBLE_DIRECTORY = "dump";
	
	
	
	public static void restoreDefaultGeneral() {
		CUSTOM_CLIENT_SIZE = false;
		CUSTOM_CLIENT_SIZE_X = 512;
		CUSTOM_CLIENT_SIZE_Y = 346;
		LOAD_CHAT_HISTORY = false;
		COMBAT_MENU = false;
		SHOW_XPDROPS = true;
		SHOW_FATIGUEDROPS = true;
		FATIGUE_FIGURES = 2;
		FATIGUE_ALERT = true;
		INVENTORY_FULL_ALERT = false;
		NAME_PATCH_TYPE = 3;
		HIDE_ROOFS = true;
		COLORIZE = true;
		FOV = 9;
		SOFTWARE_CURSOR = false;
		VIEW_DISTANCE = 10000;
		Launcher.getConfigWindow().synchronizeGuiValues();
	}
	
	public static void restoreDefaultOverlays() {
		SHOW_STATUSDISPLAY = true;
		SHOW_INVCOUNT = true;
		SHOW_ITEMINFO = false;
		SHOW_PLAYERINFO = false;
		SHOW_FRIENDINFO = false;
		SHOW_NPCINFO = false;
		SHOW_HITBOX = false;
		SHOW_FOOD_HEAL_OVERLAY = false;
		SHOW_TIME_UNTIL_HP_REGEN = false;
		DEBUG = false;
		Launcher.getConfigWindow().synchronizeGuiValues();
	}
	
	public static void restoreDefaultNotifications() {
		PM_NOTIFICATIONS = true; 
		TRADE_NOTIFICATIONS = true;
		DUEL_NOTIFICATIONS = true;
		LOGOUT_NOTIFICATIONS = true;
		LOW_HP_NOTIFICATIONS = true;
		LOW_HP_NOTIF_VALUE = 25;
		FATIGUE_NOTIFICATIONS = true;
		FATIGUE_NOTIF_VALUE = 98;
		NOTIFICATION_SOUNDS = false;
		USE_SYSTEM_NOTIFICATIONS = isRecommendedToUseSystemNotifs();
		TRAY_NOTIFS = true;
		TRAY_NOTIFS_ALWAYS = false;
		Launcher.getConfigWindow().synchronizeGuiValues();
	}
	
	public static void restoreDefaultPrivacy() {
		TWITCH_HIDE = false;
		TWITCH_CHANNEL = "";
		TWITCH_OAUTH = "";
		TWITCH_USERNAME = "";
		SHOW_LOGINDETAILS = true;
		SAVE_LOGININFO = true;
		Launcher.getConfigWindow().synchronizeGuiValues();
	}
	
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
	
	public static boolean isRecommendedToUseSystemNotifs() {
		// Users on Windows 8.1 or 10 are recommend to set USE_SYSTEM_NOTIFICATIONS = true
		return (System.getProperty("os.name").equals("Windows 10") || System.getProperty("os.name").equals("Windows 8.1"));
	}
}
