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

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import Game.Game;

public class Launcher extends JFrame implements Runnable
{
	public Launcher() {
	}
	public void init()
	{
		Logger.Info("Starting rscplus");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.BLACK);

		// Set window icon
		URL iconURL = Settings.getResource("/assets/icon.png");
		if(iconURL != null)
		{
			ImageIcon icon = new ImageIcon(iconURL);
			setIconImage(icon.getImage());
		}

		// Set size
		getContentPane().setPreferredSize(new Dimension(280, 32));
		setTitle("rscplus Launcher");
		setResizable(false);
		pack();
		setLocationRelativeTo(null);

		// Add progress bar
		m_progressBar = new JProgressBar();
		m_progressBar.setStringPainted(true);
		m_progressBar.setBorderPainted(true);
		m_progressBar.setForeground(Color.GRAY.brighter());
		m_progressBar.setBackground(Color.BLACK);
		m_progressBar.setString("Initializing");
		getContentPane().add(m_progressBar);

		setVisible(true);
		new Thread(this).start();
	}

	public void run()
	{
		Settings.Save();

		setStatus("Loading JConfig...");
		JConfig config = Game.getInstance().getJConfig();
		if(!config.fetch(Util.MakeWorldURL(Settings.WORLD)))
		{
			error("Unable to fetch JConfig");
			return;
		}

		if(!config.isSupported())
		{
			error("JConfig outdated");
			return;
		}

		m_classLoader = new JClassLoader();
		if(!m_classLoader.fetch("/assets/rsc.jar"))
		{
			error("Unable to fetch Jar");
		}

		setStatus("Launching game...");
		Game game = Game.getInstance();
		try
		{
			Class<?> client = m_classLoader.loadClass(config.getJarClass());
			game.setApplet((Applet)client.newInstance());
		}
		catch(Exception e)
		{
			error("Unable to launch game");
			e.printStackTrace();
			return;
		}
		setVisible(false);
		dispose();
		game.start();
	}

	public void error(String text)
	{
		setStatus("Error: " + text);
		try { Thread.sleep(5000); } catch(Exception e) {}
	}

	public void setStatus(final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				m_progressBar.setString(text);
			}
		});
	}

	public void setProgress(final int value, final int total)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if(total == 0)
				{
					m_progressBar.setValue(0);
					return;
				}

				m_progressBar.setValue(value * 100 / total);
			}
		});
	}

	public JClassLoader getClassLoader()
	{
		return m_classLoader;
	}

	public static void main(String args[])
	{
		Settings.initDir();
		setConfigWindow(new ConfigWindow());
		Settings.Load();
		TrayHandler.initTrayIcon();
		NotificationsHandler.initialize();
		Launcher.getInstance().init();
	}

	public static Launcher getInstance()
	{
		return (instance == null)?(instance = new Launcher()):instance;
	}

	/**
	 * @return the window
	 */
	public static ConfigWindow getConfigWindow() {
		return window;
	}
	/**
	 * @param window the window to set
	 */
	public static void setConfigWindow(ConfigWindow window) {
		Launcher.window = window;
	}

	private JProgressBar m_progressBar;
	private JClassLoader m_classLoader;
	
	private static ConfigWindow window;

	// Singleton
	private static Launcher instance;
}
