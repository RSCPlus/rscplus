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
	}

	public static class Dir
	{
		public static String JAR;
		public static String CACHE;
		public static String DUMP;
		public static String SCREENSHOT;
	}

	public static Dimension RESOLUTION = new Dimension(1280, 720);
	public static int WORLD_DEFAULT = 5;
	public static boolean DEBUG = true;
}
