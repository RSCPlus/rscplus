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

import Game.Reflection;
import Game.Renderer;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Graphics;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class rscplus extends JApplet implements ComponentListener, WindowListener, Runnable
{
	public rscplus()
	{
		m_jconfig = new JConfig();
		m_jclassloader = new JClassLoader();
		m_appletstub = new RSC_AppletStub();

		getContentPane().setLayout(null);
		getContentPane().setBackground(Color.BLACK);
		getContentPane().setPreferredSize(new Dimension(512, 346));
		addComponentListener(this);

		m_jprogressbar = new JProgressBar();
		m_jprogressbar.setStringPainted(true);
		m_jprogressbar.setBorderPainted(true);
		m_jprogressbar.setForeground(Color.GRAY.brighter());
		m_jprogressbar.setBackground(Color.BLACK);
		m_jprogressbar.setString("initializing");
		m_jprogressbar.setSize(300, 40);
		m_jprogressbar.setVisible(true);

		getContentPane().add(m_jprogressbar);
		getContentPane().revalidate();
	}

	public static rscplus getInstance()
	{
		return m_instance;
	}

	public JConfig getJConfig()
	{
		return m_jconfig;
	}

	public JClassLoader getJClassLoader()
	{
		return m_jclassloader;
	}

	public Applet getApplet()
	{
		return m_applet;
	}

	public boolean loadRSC()
	{
		Logger.Info("Loading rscplus config...");
		Settings.Load();

		Logger.Info("Loading JConfig...");
		if(!m_jconfig.fetch(Util.MakeWorldURL(Settings.WORLD)))
		{
			Logger.Error("Unable to fetch JConfig");
			return false;
		}

		Logger.Info("Checking if JConfig is supported...");
		if(!m_jconfig.isSupported())
		{
			Logger.Error("JConfig loader is outdated, please wait for an update");
			return false;
		}

		Logger.Info("Loading rsc JAR...");
		if(!m_jclassloader.fetch(m_jconfig.getJarURL()))
		{
			Logger.Error("JClassLoader is unable to obtain the rsc JAR");
			return false;
		}

		Logger.Info("Creating rsc instance...");
		try
		{
			Class<?> client = m_jclassloader.loadClass(m_jconfig.getJarClass());
			m_applet = (Applet)client.newInstance();
		}
		catch(Exception e)
		{
			Logger.Error("Unable to create rsc instance");
			e.printStackTrace();
			return false;
		}

		Logger.Info("Setting rsc client applet stub...");
		m_applet.setStub(m_appletstub);
		m_applet.setBackground(Color.BLACK);

		Logger.Info("Adding rsc applet to JApplet...");
		getContentPane().remove(m_jprogressbar);
		setContentPane(m_applet);
		revalidate();

		// Handle progress bar
		// TODO: Do we need to dispose of this somehow?
		m_jprogressbar = null;

		Logger.Info("Client loaded successfully");

		return true;
	}

	public void runRSC()
	{
		if(m_applet == null)
			return;

		Logger.Info("Finding reflection hooks...");
		Reflection.Load();

		Logger.Info("Initializing renderer...");
		Renderer.init();

		Logger.Info("Running rsc code, have fun :)");
		m_applet.init();
		m_applet.start();
	}

	public void setStatus(final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				m_jprogressbar.setString(text);
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
					m_jprogressbar.setValue(0);
					return;
				}

				m_jprogressbar.setValue(value * 100 / total);
			}
		});
	}

	public void run()
	{
		// Load rsc client
		if(!loadRSC())
		{
			// TODO: This is not safe to call in an applet
			System.exit(0);
			return;
		}

		// Run the client
		runRSC();
	}

	/*
	 *	Applet entry points
	 */
	@Override
	public void start()
	{
		Logger.Info("Starting rscplus in applet mode");

		// Create rscplus instance
		m_instance = this;

		new Thread(m_instance).start();
	}

	/*
	 *	Applet exit point
	 */
	@Override
	public void stop()
	{
		if(m_applet == null)
			return;

		m_applet.stop();
		m_applet.destroy();
	}

	/*
	 *	Application entry point
	 */
	public static void main(String args[])
	{
		Logger.Info("Starting rscplus in application mode");

		// Create rscplus instance
		m_instance = new rscplus();

		// Create rscplus window
		m_jframe = new JFrame();
		m_jframe.addWindowListener(m_instance);
		m_jframe.setResizable(false);
		m_jframe.setTitle("rscplus");
		m_jframe.setContentPane(m_instance);
		m_jframe.pack();
		m_jframe.setLocationRelativeTo(null);

		// Set window icon
		URL iconURL = Settings.getResource("/assets/icon.png");
		if(iconURL != null)
		{
			ImageIcon icon = new ImageIcon(iconURL);
			m_jframe.setIconImage(icon.getImage());
		}

		m_jframe.setVisible(true);

		new Thread(m_instance).start();
	}

	@Override
	public final void windowClosed(WindowEvent e)
	{
		stop();
	}

	@Override
	public final void windowClosing(WindowEvent e)
	{
		m_jframe.dispose();
	}

	@Override
	public final void windowOpened(WindowEvent e)
	{
		m_jframe.setResizable(true);
		m_jframe.pack();
		m_jframe.setMinimumSize(m_jframe.getSize());
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

	@Override
	public final void componentHidden(ComponentEvent e)
	{
	}

	@Override
	public final void componentMoved(ComponentEvent e)
	{
	}

	@Override
	public final void componentResized(ComponentEvent e)
	{
		Dimension size = new Dimension(getContentPane().getWidth(), getContentPane().getHeight());

		Logger.Debug("Resized to " + size.width + "x" + size.height);

		if(m_jprogressbar != null)
			m_jprogressbar.setLocation((size.width / 2) - (m_jprogressbar.getWidth() / 2), (size.height / 2) - (m_jprogressbar.getHeight() / 2));

		if(m_applet != null)
			Renderer.resize(size.width, size.height);
	}

	@Override
	public final void componentShown(ComponentEvent e)
	{
	}

	private JConfig m_jconfig;
	private JClassLoader m_jclassloader;
	private RSC_AppletStub m_appletstub;
	private Applet m_applet = null;
	private static JProgressBar m_jprogressbar = null;

	// This is only used in application mode
	private static JFrame m_jframe = null;

	// Singleton
	private static rscplus m_instance = null;
}
