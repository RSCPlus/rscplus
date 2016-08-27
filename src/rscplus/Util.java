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

package rscplus;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Util
{	
	public static String MakeWorldURL(int world)
	{
		return "http://classic" + world + ".runescape.com/jav_config.ws";
	}

	public static void MakeDirectory(String name)
	{
		File dir = new File(name);
		if(dir.isFile())
			dir.delete();
		if(!dir.exists())
			dir.mkdir();
	}

	public static boolean FileExists(String name)
	{
		File file = new File(name);
		return (file.isFile() && file.exists());
	}

	public static String byteHexString(byte[] data)
	{
		String ret = "";
		for(int i = 0; i < data.length; i++)
			ret += String.format("%02x", data[i]);
		return ret;
	}

	public static int[] getPop()
	{
		if(worldPopArray == null)
			worldPopArray = new int[6];

		if (!(System.currentTimeMillis() >= lastPopCheck + 60000))
			return worldPopArray;

		lastPopCheck = System.currentTimeMillis();

		URL url;
		URLConnection con;
		InputStream is = null;
		BufferedReader br;
    
		try
		{
			url = new URL("http://www.runescape.com/classicapplet/playclassic.ws");
			con = url.openConnection();
			is = con.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
		}
		catch(IOException ioe)
		{
			System.err.println("Network connection issues");
			ioe.printStackTrace();
			try
			{
				if(is != null)
					is.close();
			}
			catch(IOException e) {}
			return null;
		}

		String line = null;

		try
		{
			while ((line = br.readLine()) != null)
			{
				if(line.contains("<span class='classic-worlds__name'>Classic "))
				{
					String[] worldNumLine = line.split(" ");
					int worldNum = Integer.parseInt(worldNumLine[2].split("<")[0]);

					br.readLine();

					String[] worldPopLine = br.readLine().split(" ");
					int worldPop = Integer.parseInt(worldPopLine[0].trim());
					worldPopArray[worldNum] = worldPop;
				}
			}
		}
		catch(IOException ioe)
		{
			Logger.Error("Error parsing");
			ioe.printStackTrace();
			return null;
		}
		catch(NumberFormatException nfe)
		{
			Logger.Error("Error parsing a number");
			nfe.printStackTrace();
			return null;
		}

		try
		{
			br.close();
			is.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return worldPopArray;
	}

	static int[] worldPopArray;
	static long lastPopCheck = 0;
}
