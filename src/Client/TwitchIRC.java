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
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import Game.Client;

/**
 * Handles communication with a Twitch chat channel.
 */
public class TwitchIRC implements Runnable {
	
	public static final String SERVER = "irc.twitch.tv";
	public static final int PORT = 6667;
	
	/**
	 * Boolean that dictates whether or not the twitch connection is initialized and active.
	 * 
	 * @see TwitchIRC#connect
	 */
	private boolean active = false;
	private BufferedReader m_reader = null;
	private BufferedWriter m_writer = null;
	private Socket m_socket = null;
	private Thread m_thread = null;
	
	/**
	 * Creates a socket and buffered reader/writer to irc.twitch.tv on port 6667 and attempts to log in using the OAUTH
	 * and name specified in {@link Settings} which can be configured via the config GUI.
	 * It then creates a new thread to run this instance's {@link TwitchIRC#run} method and starts it.
	 */
	public void connect() {
		try {
			m_socket = new Socket(SERVER, PORT);
			m_writer = new BufferedWriter(new OutputStreamWriter(m_socket.getOutputStream()));
			m_reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
			
			m_writer.write("PASS " + Settings.TWITCH_OAUTH + "\r\n");
			m_writer.write("NICK " + Settings.TWITCH_USERNAME.toLowerCase() + "\r\n");
			m_writer.flush();
		} catch (Exception e) {
			disconnect(); // Clean up if the connection fails
		}
		
		m_thread = new Thread(this);
		m_thread.start();
		active = true;
	}
	
	/**
	 * Attempts to close the instance's BufferedWriter, BufferedReader, and Socket and sets the instance's
	 * {@link TwitchIRC#active} status to false.
	 */
	public void disconnect() {
		try {
			m_writer.close();
			m_writer = null;
		} catch (Exception e2) {
		}
		try {
			m_reader.close();
			m_reader = null;
		} catch (Exception e2) {
		}
		try {
			m_socket.close();
			m_socket = null;
		} catch (Exception e2) {
		}
		active = false;
	}
	
	/**
	 * Returns true if the client is currently configured to use Twitch.
	 * 
	 * <p>
	 * This check is performed by determining if a Twitch channel is currently specified in the {@link Settings}
	 * which can be configured via the config GUI.
	 * </p>
	 * 
	 * @return If the client is currently configured to use Twitch
	 */
	public static boolean isUsing() {
		return Settings.TWITCH_CHANNEL.length() > 0; // TODO maybe check for an OAUTH/password also?
	}
	
	/**
	 * Handles the Twitch login response parsing and IRC BufferedReader reading.
	 * Passes messages received from twitch to the client's chat display.
	 */
	@Override
	public void run() {
		try {
			String line = null;
			
			while (active && (line = m_reader.readLine()) != null) {
				if (line.indexOf("004") >= 0) {
					m_writer.write("CAP REQ :twitch.tv/commands\r\n");
					m_writer.write("JOIN #" + Settings.TWITCH_CHANNEL.toLowerCase() + "\r\n");
					m_writer.flush();
					// FIXME: Consider thread safety for applet GUI updates outside of its thread?
					Client.displayMessage("@yel@Connected to @red@[" + Settings.TWITCH_CHANNEL + "]@yel@ Twitch chat", Client.CHAT_CHAT);
					Client.displayMessage("@lre@Messages starting with @whi@/@lre@ are sent to Twitch.", Client.CHAT_CHAT);
					break;
				} else if (line.contains("NOTICE")) {
					String message = line.substring(line.indexOf(':', 2) + 1, line.length());
					if ("Error logging in".equals(message)) {
						active = false;
						Client.displayMessage("@red@Unable to login to Twitch (username/oauth incorrect)", Client.CHAT_CHAT);
						break;
					}
				}
				
				Logger.Debug(line);
			}
			
			while (active && (line = m_reader.readLine()) != null) {
				String[] lineArray = line.split(" ");
				
				if ("PING".equals(lineArray[0])) {
					m_writer.write("PONG " + lineArray[1] + "\r\n");
					m_writer.flush();
				} else if ("PRIVMSG".equals(lineArray[1]) && !Settings.TWITCH_HIDE) {
					String username = line.substring(1, line.indexOf('!'));
					String message = line.substring(line.indexOf(':', 2) + 1, line.length());
					
					if (username.equalsIgnoreCase(Settings.TWITCH_CHANNEL))
						username = "@cya@" + username;
					else
						username = "@yel@" + username;
					
					String msgColor = "@yel@";
					if (message.toLowerCase().contains(Settings.TWITCH_USERNAME.toLowerCase()))
						msgColor = "@gre@";
					
					if (message.startsWith(Character.toString((char)1)) && message.endsWith(Character.toString((char)1))) {
						message = message.substring(7, message.length() - 1);
						Client.displayMessage("@red@[" + Settings.TWITCH_CHANNEL + "] " + username + " @lre@" + message, Client.CHAT_CHAT);
					} else {
						Client.displayMessage("@red@[" + Settings.TWITCH_CHANNEL + "] " + username + "@yel@: " + msgColor + message, Client.CHAT_CHAT);
					}
				} else if ("NOTICE".equals(lineArray[1]) && !Settings.TWITCH_HIDE) {
					String message = line.substring(line.indexOf(':', 2) + 1, line.length());
					Client.displayMessage("@red@[" + Settings.TWITCH_CHANNEL + "] " + message, Client.CHAT_CHAT);
				}
				
				Logger.Debug(line);
			}
		} catch (Exception e) {
		}
		
		// Reconnect on disconnect
		if (active) {
			Client.displayMessage("@yel@Disconnected from @red@[" + Settings.TWITCH_CHANNEL + "] @yel@, reconnecting in 10 seconds...", Client.CHAT_CHAT);
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
			}
			connect();
		}
	}
	
	/*public boolean processCommand(String username, String message) {
		// !stats
		if (message.toLowerCase().equals("!stats")) {
			String statString = "";
			for (int i = 0; i < 18; i++) {
				statString = statString + Client.skill_name[i] + ": " + Client.base_level[i];
				if (i != 17)
					statString += ", ";
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			sendMessage(statString, false);
			return true;
		}
		return false;
	}*/
	
	/**
	 * Sends a Twitch message to the server via the instance's BufferedWriter.
	 * This method is called when the message to be sent is <b>not</b> an emote (designated with /me).
	 * 
	 * @param message The message to be sent to the server
	 * @param show Whether or not to show the message to the client.
	 * @see TwitchIRC#sendEmote
	 */
	public void sendMessage(String message, boolean show) {
		try {
			m_writer.write("PRIVMSG #" + Settings.TWITCH_CHANNEL.toLowerCase() + " :" + message + "\r\n");
			m_writer.flush();
			if (show) {
				String username = Settings.TWITCH_USERNAME.toLowerCase();
				if (username.equalsIgnoreCase(Settings.TWITCH_CHANNEL))
					username = "@cya@" + username;
				else
					username = "@yel@" + username;
				Client.displayMessage("@red@[" + Settings.TWITCH_CHANNEL + "] " + username + "@yel@: " + message, Client.CHAT_CHAT);
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * Sends a Twitch message to the server via the instance's BufferedWriter.
	 * This method is called when the message to be sent is an emote (designated with /me).
	 * 
	 * @param message The message to be sent to the server
	 * @param show Whether or not to show the message to the client.
	 * @see TwitchIRC#sendMessage
	 */
	public void sendEmote(String message, boolean show) {
		try {
			m_writer.write("PRIVMSG #" + Settings.TWITCH_CHANNEL.toLowerCase() + " :" + Character.toString((char)1) + "ACTION " + message + Character.toString((char)1) + "\r\n");
			m_writer.flush();
			if (show) {
				String username = Settings.TWITCH_USERNAME.toLowerCase();
				if (username.equalsIgnoreCase(Settings.TWITCH_CHANNEL))
					username = "@cya@" + username;
				else
					username = "@yel@" + username;
				Client.displayMessage("@red@[" + Settings.TWITCH_CHANNEL + "] " + username + "@lre@ " + message, Client.CHAT_CHAT);
			}
		} catch (Exception e) {
		}
	}
	
}
