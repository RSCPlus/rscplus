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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import Game.Client;
import Game.Game;

public class Settings
{
	public static void Load()
	{
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

		// Load settings
		try
		{
			Properties props = new Properties();
			FileInputStream in = new FileInputStream(Dir.JAR + "/config.ini");
			props.load(in);
			in.close();

			WORLD = getInt(props, "world", 2);
			HIDE_ROOFS = getBoolean(props, "hide_roofs", false);
			COMBAT_MENU = getBoolean(props, "combat_menu", false);
			SHOW_ITEMINFO = getBoolean(props, "show_iteminfo", false);
			SHOW_NPCINFO = getBoolean(props, "show_npcinfo", false);
			SHOW_PLAYERINFO = getBoolean(props, "show_playerinfo", false);
			SHOW_FRIENDINFO = getBoolean(props, "show_friendinfo", false);
			SHOW_HITBOX = getBoolean(props, "show_hitbox", false);
			SHOW_LOGINDETAILS = getBoolean(props, "show_logindetails", false);
			SHOW_XPDROPS = getBoolean(props, "show_xpdrops", true);
			SHOW_INVCOUNT = getBoolean(props, "show_invcount", true);
			SHOW_STATUSDISPLAY = getBoolean(props, "show_statusdisplay", true);
			SHOW_FATIGUEDROPS = getBoolean(props, "show_fatiguedrops", true);
			SOFTWARE_CURSOR = getBoolean(props, "software_cursor", false);
			DEBUG = getBoolean(props, "debug", false);
			VIEW_DISTANCE = getInt(props, "view_distance", 10000);
			COMBAT_STYLE = getInt(props, "combat_style", Client.COMBAT_AGGRESSIVE);
			FATIGUE_ALERT = getBoolean(props, "fatigue_alert", true);
			TWITCH_HIDE = getBoolean(props, "twitch_hide", false);
			TWITCH_USERNAME = getString(props, "twitch_username", "");
			TWITCH_OAUTH = getString(props, "twitch_oauth", "");
			TWITCH_CHANNEL = getString(props, "twitch_channel", "");
			NAME_PATCH_TYPE = getInt(props, "name_patch_type", 3);
			SAVE_LOGININFO = getBoolean(props, "save_logininfo", true);
			FATIGUE_FIGURES = getInt(props, "fatigue_figures", 2);

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
		}
		catch(Exception e) {}

		if(DEBUG)
		{
			Dir.DUMP = Dir.JAR + "/dump";
			Util.MakeDirectory(Dir.DUMP);
		}
	}

	public static void Save()
	{
		try
		{
			Properties props = new Properties();
			props.setProperty("world", "" + WORLD);
			props.setProperty("hide_roofs", "" + HIDE_ROOFS);
			props.setProperty("combat_menu", "" + COMBAT_MENU);
			props.setProperty("show_invcount", "" + SHOW_INVCOUNT);
			props.setProperty("show_iteminfo", "" + SHOW_ITEMINFO);
			props.setProperty("show_npcinfo", "" + SHOW_NPCINFO);
			props.setProperty("show_playerinfo", "" + SHOW_PLAYERINFO);
			props.setProperty("show_friendinfo", "" + SHOW_FRIENDINFO);
			props.setProperty("show_hitbox", "" + SHOW_HITBOX);
			props.setProperty("show_logindetails", "" + SHOW_LOGINDETAILS);
			props.setProperty("show_xpdrops", "" + SHOW_XPDROPS);
			props.setProperty("show_fatiguedrops", "" + SHOW_FATIGUEDROPS);
			props.setProperty("show_statusdisplay", "" + SHOW_STATUSDISPLAY);
			props.setProperty("software_cursor", "" + SOFTWARE_CURSOR);
			props.setProperty("debug", "" + DEBUG);
			props.setProperty("view_distance", "" + VIEW_DISTANCE);
			props.setProperty("combat_style", "" + COMBAT_STYLE);
			props.setProperty("fatigue_alert", "" + FATIGUE_ALERT);
			props.setProperty("twitch_hide", "" + TWITCH_HIDE);
			props.setProperty("twitch_username", "" + TWITCH_USERNAME);
			props.setProperty("twitch_oauth", "" + TWITCH_OAUTH);
			props.setProperty("twitch_channel", "" + TWITCH_CHANNEL);
			props.setProperty("name_patch_type", "" + NAME_PATCH_TYPE);
			props.setProperty("save_logininfo", "" + SAVE_LOGININFO);
			props.setProperty("fatigue_figures", "" + FATIGUE_FIGURES);

			FileOutputStream out = new FileOutputStream(Dir.JAR + "/config.ini");
			props.store(out, "---rscplus config---");
			out.close();
		}
		catch(Exception e)
		{
			Logger.Error("Unable to save settings");
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

	private static String getString(Properties props, String key, String def)
	{
		String value = props.getProperty(key);
		if(value == null)
			return def;

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

	public static int VIEW_DISTANCE = 10000;
	public static boolean FATIGUE_ALERT = true;
	public static boolean COMBAT_MENU = false;
	public static int COMBAT_STYLE = Client.COMBAT_AGGRESSIVE;
	public static boolean HIDE_ROOFS = false;
	public static int WORLD = 2;
	public static boolean SHOW_NPCINFO = false;
	public static boolean SHOW_PLAYERINFO = false;
	public static boolean SHOW_FRIENDINFO = false;
	public static boolean SHOW_INVCOUNT = true;
	public static boolean SHOW_ITEMINFO = false;
	public static boolean SHOW_HITBOX = false;
	public static boolean SHOW_LOGINDETAILS = false;
	public static boolean SHOW_STATUSDISPLAY = true;
	public static boolean SHOW_XPDROPS = true;
	public static boolean SHOW_FATIGUEDROPS = true;
	public static boolean SOFTWARE_CURSOR = false;
	public static boolean DEBUG = false;
	public static boolean TWITCH_HIDE = false;
	public static String TWITCH_USERNAME = "";
	public static String TWITCH_OAUTH = "";
	public static String TWITCH_CHANNEL = "";
	public static int NAME_PATCH_TYPE = 3;
	public static boolean SAVE_LOGININFO = true;
	public static int FATIGUE_FIGURES = 2;
}
