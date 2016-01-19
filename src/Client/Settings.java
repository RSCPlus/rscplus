/**
 *	rscplus, RuneScape Classic injection client to enhance the game
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

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

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

		// Load other directories
		Dir.CACHE = Dir.JAR + "/cache";
		Util.MakeDirectory(Dir.CACHE);
		Dir.SCREENSHOT = Dir.JAR + "/screenshots";
		Util.MakeDirectory(Dir.SCREENSHOT);

		if(DEBUG)
		{
			Dir.DUMP = Dir.JAR + "/dump";
			Util.MakeDirectory(Dir.DUMP);
		}

		// Load settings
		try
		{
			Properties props = new Properties();
			FileInputStream in = new FileInputStream(Dir.JAR + "/config.ini");
			props.load(in);
			in.close();

			WORLD = getInt(props, "world", 2);
			RESOLUTION.width = getInt(props, "width", 512);
			RESOLUTION.height = getInt(props, "height", 346);
			HIDE_ROOFS = getBoolean(props, "hide_roofs", false);
			DEBUG = getBoolean(props, "debug", false);
		}
		catch(Exception e) {}
	}

	public static void Save()
	{
		try
		{
			Properties props = new Properties();
			props.setProperty("width", "" + RESOLUTION.width);
			props.setProperty("height", "" + RESOLUTION.height);
			props.setProperty("world", "" + WORLD);
			props.setProperty("hide_roofs", "" + HIDE_ROOFS);
			props.setProperty("debug", "" + DEBUG);

			FileOutputStream out = new FileOutputStream(Dir.JAR + "/config.ini");
			props.store(out, "---rscplus config---");
			out.close();
		}
		catch(Exception e)
		{
			Logger.Error("Unable to save settings");
		}
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
		"1 (Veterans Only)",
		"2",
		"3",
		"4",
		"5"
	};

	public static boolean HIDE_ROOFS = true;
	public static Dimension RESOLUTION = new Dimension(512, 346);
	public static int WORLD = 2;
	public static boolean DEBUG = true;
}
