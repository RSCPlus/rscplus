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

package Game;

import Client.JConfig;
import Client.Launcher;
import Client.Settings;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Game extends JFrame implements AppletStub, WindowListener
{
	public Game()
	{
		// Set window icon
		URL iconURL = getClass().getResource("/assets/icon.png");
		if(iconURL != null)
		{
			ImageIcon icon = new ImageIcon(iconURL);
			setIconImage(icon.getImage());
		}

		// Set window properties
		addWindowListener(this);
		getContentPane().setBackground(Color.BLACK);
		getContentPane().setPreferredSize(new Dimension(Settings.RESOLUTION.width, Settings.RESOLUTION.height));
		setResizable(false);
		setTitle(null);
		pack();
		pack();
		setLocationRelativeTo(null);

		instance = this;
	}

	public void start()
	{
		if(applet == null)
			return;

		getContentPane().add(applet);
		setVisible(true);

		Renderer.init();
		applet.init();
		applet.start();
	}

	@Override
	public void setTitle(String title)
	{
		String t = "rscplus";

		if(title != null)
			t = t + " (" + title + ")";

		super.setTitle(t);
	}

	@Override
	public final URL getCodeBase()
	{
		return config.getURL("codebase");
	}


	@Override
	public final URL getDocumentBase()
	{
		return getCodeBase();
	}

	@Override
	public final String getParameter(String key)
	{
		return config.parameters.get(key);
	}

	@Override
	public final AppletContext getAppletContext()
	{
		return null;
	}

	@Override
	public final void appletResize(int width, int height)
	{
		if(applet != null)
			Renderer.resize(width, height);
	}

	@Override
	public final void windowClosed(WindowEvent e)
	{
		if(applet != null)
		{
			new Thread(new Runnable()
			{
				public void run()
				{
					applet.stop();
					applet.destroy();
				}
			}).start();
		}
	}

	@Override
	public final void windowClosing(WindowEvent e)
	{
		dispose();
		new Launcher();
	}

	@Override
	public final void windowOpened(WindowEvent e)
	{
	}

	@Override
	public final void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public final void windowActivated(WindowEvent e)
	{
	}

	@Override
	public final void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public final void windowIconified(WindowEvent e)
	{
	}

	public static Game instance;
	public JConfig config;
	public Applet applet = null;
}
