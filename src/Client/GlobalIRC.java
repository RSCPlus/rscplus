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

import Game.Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class GlobalIRC implements Runnable
{
	class User
	{
		User(String username, boolean mod)
		{
			rscplus_username = username;
			rscplus_mod = mod;
		}

		public String rscplus_username;
		public boolean rscplus_mod;
	}

	public static final String SERVER = "irc.snoonet.org";
	public static final int PORT = 6667;
	public static final String CHANNEL = "#rscplus";
	public static final String RSC_CHANNEL = "rsc+";

	public void connect(String username)
	{
		if(m_active)
			disconnect(true);

		Logger.Info("Connecting to global chat...");
		try
		{
			m_socket = new Socket(SERVER, PORT);
			m_writer = new BufferedWriter(new OutputStreamWriter(m_socket.getOutputStream()));
			m_reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));

			m_username = username.replaceAll(" ", "_");

			m_writer.write("NICK " + m_username + "\r\n");
			m_writer.write("USER " + m_username + " rscplus/" + m_username + " rscplus :" + m_username + "\r\n");
			m_writer.flush();

			m_users.clear();
			m_active = true;

			m_thread = new Thread(this);
			m_thread.start();
		}
		catch(Exception e)
		{
			try { m_writer.close(); m_writer = null; } catch(Exception e2) {}
			try { m_reader.close(); m_reader = null; } catch(Exception e2) {}
			try { m_socket.close(); m_socket = null; } catch(Exception e2) {}
		}
	}

	public void disconnect(boolean wait)
	{
		try
		{
			m_writer.write("QUIT :Leaving\r\n");
			m_writer.flush();
			m_writer.close();
			m_writer = null;
		} catch(Exception e2) {}
		try { m_reader.close(); m_reader = null; } catch(Exception e2) {}
		try { m_socket.close(); m_socket = null; } catch(Exception e2) {}

		m_active = false;

		if(wait)
		{
			try
			{
				m_thread.join();
			} catch(Exception e) {}
		}
	}

	public void displayStatus()
	{
		Client.displayMessage("@yel@Messages starting with @whi@/@yel@ are sent to @lre@[" + RSC_CHANNEL + "]@yel@ chat.", Client.CHAT_CHAT);
		Client.displayMessage("@yel@Usernames in @lre@[" + RSC_CHANNEL + "]@yel@ may not be the same in RSC.", Client.CHAT_CHAT);
	}

	public void sendMessage(String message)
	{
		if(!m_active)
		{
			Client.displayMessage("@lre@[" + RSC_CHANNEL + "]@dre@ You are not connected to chat", Client.CHAT_CHAT);
			return;
		}

		if(m_users.size() <= 1)
		{
			Client.displayMessage("@lre@[" + RSC_CHANNEL + "]@dre@ There are no other players online", Client.CHAT_CHAT);
			return;
		}

		try
		{
			m_writer.write("PRIVMSG " + CHANNEL + " :" + message + "\r\n");
			m_writer.flush();
			Client.displayMessage("@lre@[" + RSC_CHANNEL + "]@yel@ " + m_username + ": @whi@" + message, Client.CHAT_CHAT);
		}
		catch(Exception e)
		{
			Client.displayMessage("@lre@[" + RSC_CHANNEL + "]@dre@ Failed to send message", Client.CHAT_CHAT);
		}
	}

	private boolean userIgnored(String user)
	{
		return (user.equals("ChanServ"));
	}

	private boolean charIgnored(String message, int index)
	{
		char value = ' ';
		try
		{
			value = message.charAt(index);
		}
		catch(Exception e) {}

		return (value == ' ' || value == ':' || value == ',' || value == '.' || value == '!' || value == '?' ||
			value == ';' || value == '/' || value == '\\' || value == '\'' || value == '"' || value == '@' || value == '~');
	}

	private String formatMessage(String message)
	{
		int mention = message.indexOf(m_username);
		if(mention != -1)
		{
			if(charIgnored(message, mention - 1) && charIgnored(message, mention + m_username.length()))
				message = "@gre@" + message;
		}

		return message;
	}

	public void run()
	{
		String line = null;
		boolean connected = false;

		while(m_active)
		{
			try
			{
				line = m_reader.readLine();
				if(line != null)
				{
					String line_split[] = line.split(" ");

					String command = line_split[1];

					if(line_split[0].equals("PING"))
					{
						m_writer.write("PONG " + line_split[1] + "\r\n");
						m_writer.flush();
					}
					else if(command.equals("NICK"))
					{
						// :nick!nick@50.45.ot.vj NICK :Guest26275
						String user_from = line_split[0].split("!")[0].substring(1);
						String user_to = line_split[2].substring(1);

						User user = m_users.get(user_from);
						m_users.remove(user_from);
						m_users.put(user_to, user);

						if(user_from.equals(m_username))
						{
							Client.displayMessage("@lre@[" + RSC_CHANNEL + "]@whi@ You are now known as @yel@" + user_to, Client.CHAT_CHAT);
							m_username = user_to;
						}
					}
					else if(command.equals("JOIN"))
					{
						// :ornox_!ornox@50.45.tg.sm JOIN #rscplus
						String user = line_split[0].split("!")[0].substring(1);
						String chan = line_split[2];

						if(!userIgnored(user))
						{
							User user_listing = new User(user, false);
							m_users.put(user, user_listing);
						}
					}
					else if(command.equals("QUIT"))
					{
						// :ornox_!ornox@50.45.tg.sm QUIT :Quit: Leaving
						String user = line_split[0].split("!")[0].substring(1);

						m_users.remove(user);
					}
					else if(command.equals("KICK"))
					{
						// :ornox_!ornox@50.45.tg.sm QUIT :Quit: Leaving
						String user = line_split[3];

						m_users.remove(user);

						if(user.equals(m_username))
						{
							disconnect(false);
							Client.displayMessage("@lre@[" + RSC_CHANNEL + "]@yel@ You have been kicked from chat", Client.CHAT_CHAT);
						}
					}
					else if(command.equals("MODE"))
					{
						// :ChanServ!chanserv@services.darkmyst.org MODE #rscplus +o ornox_
						String user = line_split[4];
						String mode = line_split[3];

						User user_listing = m_users.get(user);
						if(user_listing != null)
						{
							if(mode.startsWith("+"))
							{
								if(mode.contains("o"))
									user_listing.rscplus_mod = true;
							}
							else if(mode.startsWith("-"))
							{
								if(mode.contains("o"))
									user_listing.rscplus_mod = false;
							}
						}
					}
					else if(command.equals("353"))
					{
						// :prometheus.no.eu.darkmyst.org 353 ornox = #rscplus :ornox @ornox_ @ChanServ
						String users[] = line.substring(line_split[0].length() + line_split[1].length() + line_split[2].length() +
										line_split[3].length() + line_split[4].length() + 6).split(" ");

						for(String user : users)
						{
							boolean op = false;

							if(user.charAt(0) == '@')
							{
								op = true;
								user = user.substring(1);
							}

							if(!userIgnored(user))
							{
								User user_listing = new User(user, op);
								m_users.put(user, user_listing);
							}
						}
					}
					else if(command.equals("366"))
					{
						Client.displayMessage("@lre@[" + RSC_CHANNEL + "]@yel@ There are currently @whi@" + m_users.size() + "@yel@ players online", Client.CHAT_CHAT);
					}
					else if(command.equals("PRIVMSG"))
					{
						// :ornox_!ornox@50.45.ot.vj PRIVMSG #rscplus :l
						String chan = line_split[2];
						String user_from = line_split[0].split("!")[0].substring(1);
						String message = line.substring(line_split[0].length() + line_split[1].length() + line_split[2].length() + 4);

						User user = m_users.get(user_from);
						if(user != null)
						{
							if(user.rscplus_mod)
								user_from = "@gre@@@yel@" + user_from;
						}

						message = formatMessage(message);

						if(chan.equals(CHANNEL))
							Client.displayMessage("@lre@[" + RSC_CHANNEL + "]@yel@ " + user_from + ": @whi@" + message, Client.CHAT_CHAT);
					}
					else if(command.equals("433"))
					{
						// Add a '_' to the end of username everytime it's not available
						m_username = m_username + "_";
						m_writer.write("NICK " + m_username + "\r\n");
						m_writer.flush();
					}
					else if(command.equals("376") && !connected)
					{
						m_writer.write("JOIN " + CHANNEL + "\r\n");
						m_writer.flush();
						connected = true;
						Client.displayMessage("@lre@[" + RSC_CHANNEL + "]@whi@ You are known as @yel@" + m_username, Client.CHAT_CHAT);
					}

					Logger.Debug("[Global] " + line);
				}
			}
			catch(Exception e)
			{
			}

			try { Thread.sleep(10); } catch(Exception e) {}
		}
	}

	private Map<String,User> m_users = new HashMap<String,User>();
	private String m_username = "<null>";
	private boolean m_active = false;
	private Thread m_thread;
	private BufferedReader m_reader = null;
	private BufferedWriter m_writer = null;
	private Socket m_socket = null;
}
