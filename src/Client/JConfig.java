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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.HashMap;
import Game.Game;

public class JConfig
{
	public boolean fetch(String url)
	{
		try
		{
			URL configURL = new URL(url);
			Logger.Debug("Config URL: " + configURL);

			// Open connection
			URLConnection connection = configURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String line;
			while((line = in.readLine()) != null)
			{
				// Skip empty lines
				if(line.length() <= 0)
					continue;

				String key = line.substring(0, line.indexOf("="));

				// Skip official client locale messages
				if(key.equals("msg"))
					continue;

				String value = line.substring(key.length() + 1);

				switch(key)
				{
				case "param":
					String paramKey = value.substring(0, value.indexOf("="));
					String paramValue = value.substring(paramKey.length() + 1);
					parameters.put(paramKey, paramValue);
					Logger.Debug("parameters[" + paramKey + "]: " + paramValue);
					break;

				default:
					Logger.Debug("data[" + key + "]: " + value);
					m_data.put(key, value);
					break;
				}
			}

			// Close connection
			in.close();
		}
		catch(Exception e)
		{
			return false;
		}

		return  true;
	}

	public boolean isSupported()
	{
		String version = m_data.get("viewerversion");
		if(version == null)
			version = "0";

		String subVersion = m_data.get("other_sub_version");
		if(subVersion == null)
			subVersion = "0";

		return (Integer.valueOf(version) <= VERSION && Integer.valueOf(subVersion) <= SUBVERSION);
	}

	public void changeWorld(int world)
	{
		// Clip world to 1 - 5
		if(world > 5)
			world = 5;
		else if(world < 1)
			world = 1;

		parameters.put("nodeid", "" + (5000 + world));
		if(world == 1)
			parameters.put("servertype", "" + 3);
		else
			parameters.put("servertype", "" + 1);
		m_data.put("codebase", "http://classic" + world + ".runescape.com/");

		// Update settings
		Settings.WORLD = world;
		Settings.Save();

		// TODO: This may not be the best way to handle this, but for now this works
		// If we start setting other title info, it will be broken by this
		Game.getInstance().setTitle("World " + (Settings.WORLD));
	}

	public String getString(String key)
	{
		return m_data.get(key);
	}

	public URL getURL(String key)
	{
		try
		{
			return new URL(m_data.get(key));
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public URL getJarURL()
	{
		String codebase = m_data.get("codebase");
		if(codebase == null)
			return null;

		String initial_jar = m_data.get("initial_jar");
		if(initial_jar == null)
			return null;

		try
		{
			return new URL(codebase + initial_jar);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public String getJarClass()
	{
		String initial_class = m_data.get("initial_class");
		if(initial_class == null)
			return null;

		return initial_class.substring(0, initial_class.indexOf(".class"));
	}

	public int getInteger(String key)
	{
		String string = m_data.get(key);
		if(string != null)
			return Integer.valueOf(string);
		else
			return 0;
	}

	// Official client version information, subversion uses 'other_sub_version'
	public static final int VERSION = 124;
	public static final int SUBVERSION = 2;

	public Map<String, String> parameters = new HashMap<String, String>();

	private Map<String, String> m_data = new HashMap<String, String>();
}
