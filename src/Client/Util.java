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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * A miscellaneous utility class
 */
public class Util {
	
	/**
	 * Stores the world populations in the array indices corresponding to the world numbers
	 */
	static int[] worldPopArray;
	
	/**
	 * The last time the world populations were checked
	 */
	static long lastPopCheck = 0;
	
	private Util() {
		// Empty private constructor to prevent instantiation.
	}
	
	/**
	 * Gets the URL to the RSC jav_config.ws file for a given world.
	 * 
	 * @param world the world
	 * @return the URL to the jav_config.ws file
	 */
	public static String makeWorldURL(int world) {
		return "http://classic" + world + ".runescape.com/jav_config.ws";
	}
	
	/**
	 * Creates a directory relative to codebase, which is typically either the jar or location of the package folders.
	 * 
	 * @param name the name of the folder to create
	 */
	public static void makeDirectory(String name) {
		File dir = new File(name);
		if (dir.isFile())
			dir.delete();
		if (!dir.exists())
			dir.mkdir();
	}
	
	/**
	 * Converts a byte array into a String of 2 digit hexadecimal numbers.
	 * 
	 * @param data a byte array to convert
	 * @return a String of hexadecimal numbers
	 * @see #hexStringByte
	 */
	public static String byteHexString(byte[] data) {
		String ret = "";
		for (int i = 0; i < data.length; i++)
			ret += String.format("%02x", data[i]);
		return ret;
	}
	
	/**
	 * Converts a String of 2 digit hexadecimal numbers into a byte array.
	 * 
	 * @param data a String to convert
	 * @return a byte array
	 * @see #byteHexString
	 */
	public static byte[] hexStringByte(String data) {
		byte[] bytes = new byte[data.length() / 2];
		int j;
		for (int i = 0; i < bytes.length; i++) {
			j = i * 2;
			String hex_pair = data.substring(j, j + 2);
			byte b = (byte)(Integer.parseInt(hex_pair, 16) & 0xFF);
			bytes[i] = b;
		}
		return bytes;
	}
	
	/**
	 * Gets the populations of the worlds.
	 * 
	 * @return an array containing the population of each world
	 */
	public static int[] getPop() {
		if (worldPopArray == null)
			worldPopArray = new int[6];
		
		if (System.currentTimeMillis() < lastPopCheck + 60000)
			return worldPopArray;
		
		lastPopCheck = System.currentTimeMillis();
		
		URL url;
		URLConnection con;
		InputStream is = null;
		BufferedReader br;
		
		try {
			url = new URL("http://www.runescape.com/classicapplet/playclassic.ws");
			con = url.openConnection();
			is = con.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
		} catch (IOException ioe) {
			Logger.Error("Network connection issues");
			ioe.printStackTrace();
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
			return worldPopArray;
		}
		
		String line = null;
		
		try {
			while ((line = br.readLine()) != null) {
				if (line.contains("<span class='classic-worlds__name'>Classic ")) {
					String[] worldNumLine = line.split(" ");
					int worldNum = Integer.parseInt(worldNumLine[2].split("<")[0]);
					br.readLine();
					String[] worldPopLine = br.readLine().split(" ");
					int worldPop = Integer.parseInt(worldPopLine[0].trim());
					worldPopArray[worldNum] = worldPop;
				}
			}
		} catch (IOException ioe) {
			Logger.Error("Error parsing");
			ioe.printStackTrace();
			return null;
		} catch (NumberFormatException nfe) {
			Logger.Error("Error parsing a number");
			nfe.printStackTrace();
			return null;
		}
		
		try {
			br.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return worldPopArray;
	}
	
}
