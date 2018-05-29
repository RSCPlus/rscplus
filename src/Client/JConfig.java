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
import java.util.HashMap;
import java.util.Map;

/**
 * Parses, stores, and retrieves values from a jav_config.ws file
 */
public class JConfig {
	
	// Official client version information, subversion uses 'other_sub_version'
	public static final int VERSION = 124;
	public static final int SUBVERSION = 2;
	
	/**
	 * Stores the jav_config.ws 'param' value sets
	 * 
	 * @see #m_data
	 */
	public Map<String, String> parameters = new HashMap<>();
	
	/**
	 * Stores the jav_config.ws value sets for everything but 'param' and 'msg'
	 * 
	 * @see JConfig#parameters
	 */
	private Map<String, String> m_data = new HashMap<>();
	
	/**
	 * Opens and parses a jav_config.ws file.
	 * 
	 * @param url the URL to a jav_config.ws file
	 * @return if no exceptions occurred
	 */
	public boolean fetch(String url) {
		try {
			URL configURL = new URL(url);
			Logger.Debug("Config URL: " + configURL);
			
			// Open connection
			URLConnection connection = configURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			String line;
			while ((line = in.readLine()) != null) {
				// Skip empty lines
				if (line.length() <= 0)
					continue;
				
				String key = line.substring(0, line.indexOf('='));
				
				// Skip official client locale messages
				if ("msg".equals(key))
					continue;
				
				String value = line.substring(key.length() + 1);
				
				switch (key) {
				case "param":
					String paramKey = value.substring(0, value.indexOf('='));
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
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * An attempt to check if the version and subversion listed in jav_config.ws are compatible.<br>
	 * However, as of 4/6/2017, 'viewerversion' and 'other_sub_version' do not appear in jav_config.ws
	 * 
	 * @return if the official version of the client is compatible with the RSC+ client
	 */
	public boolean isSupported() {
		// TODO: Since 'viewerversion' and 'other_sub_version' do not appear in jav_config.ws, this method doesn't do
		// anything meaningful
		String version = m_data.get("viewerversion");
		if (version == null)
			version = "0";
		
		String subVersion = m_data.get("other_sub_version");
		if (subVersion == null)
			subVersion = "0";
		
		return Integer.valueOf(version) <= VERSION && Integer.valueOf(subVersion) <= SUBVERSION;
	}
	
	/**
	 * Prepares the client to log into a given world and saves the choice in the config
	 * 
	 * @param world The desired world to log into
	 */
	public void changeWorld(int world) {
		if (world == 6) {
			// Replay playback "world"
			m_data.put("codebase", "http://127.0.0.1/");
			return;
		}
		
		// Clip world to 1 - 5
		if (world > 5)
			world = 5;
		else if (world < 1)
			world = 1;
		
		parameters.put("nodeid", "" + (5000 + world));
		if (world == 1)
			parameters.put("servertype", "" + 3);
		else
			parameters.put("servertype", "" + 1);
		
		m_data.put("codebase", "http://classic" + world + ".runescape.com/");
		
		// Update settings
		Settings.WORLD = world;
		Settings.save();
	}
	
	/**
	 * Gets the corresponding String from the {@link #m_data} HashMap, given the key
	 * 
	 * @param key A key for the {@link #m_data} HashMap
	 * @return A String stored in the {@link #m_data} HashMap
	 */
	public String getString(String key) {
		return m_data.get(key);
	}
	
	/**
	 * Attempts to return a URL from {@link #m_data}, given the key
	 * 
	 * @param key The key to a corresponding URL
	 * @return A URL
	 */
	public URL getURL(String key) {
		try {
			return new URL(m_data.get(key));
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Gets the URL that points to the vanilla RSC client jar.
	 * 
	 * @return The URL to the vanilla RSC client jar
	 */
	public URL getJarURL() {
		String codebase = m_data.get("codebase");
		if (codebase == null)
			return null;
		
		String initial_jar = m_data.get("initial_jar");
		if (initial_jar == null)
			return null;
		
		try {
			return new URL(codebase + initial_jar);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Gets the main class of the client jar from jav_config.ws.
	 * 
	 * @return The name of the main class of the client jar
	 */
	public String getJarClass() {
		String initial_class = m_data.get("initial_class");
		if (initial_class == null)
			return null;
		
		return initial_class.substring(0, initial_class.indexOf(".class"));
	}
	
	/**
	 * Gets values in the {@link #m_data} HashMap.
	 * 
	 * @param key The HashMap key
	 * @return The value of the {@link #m_data} HashMap for the supplied key
	 */
	public int getInteger(String key) {
		String string = m_data.get(key);
		if (string != null)
			return Integer.valueOf(string);
		else
			return 0;
	}
	
}
