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

import java.io.File;

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

	public static String byteHexString(byte[] data)
	{
		String ret = "";
		for(int i = 0; i < data.length; i++)
			ret += String.format("%02x", data[i]);
		return ret;
	}
}
