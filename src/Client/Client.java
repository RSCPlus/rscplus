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

import Game.Game;
import java.applet.Applet;

public class Client implements Runnable
{
	public Client()
	{
		m_thread = new Thread(this);
		m_thread.start();
	}

	public void run()
	{
		Logger.Info("Starting client");
		Settings.Load();

		JConfig config = new JConfig();
		config.fetch(Util.MakeWorldURL(Settings.WORLD_DEFAULT));

		m_classLoader = new JClassLoader();
		m_classLoader.fetch(config.getJarURL());

		// Launch game
		game = new Game();
		try
		{
			Class<?> client = m_classLoader.loadClass(config.getJarClass());
			game.applet = (Applet)client.newInstance();
			game.applet.setStub(game);
			game.config = config;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		game.start();
	}

	public static void main(String args[])
	{
		instance = new Client();
		return;
	}

	public Game game;

	private JClassLoader m_classLoader;
	private Thread m_thread;

	public static Client instance;
}
