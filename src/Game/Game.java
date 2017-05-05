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

package Game;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import Client.JConfig;
import Client.Launcher;
import Client.NotificationsHandler;
import Client.Settings;
import Client.TrayHandler;

/**
 * Singleton class that handles packaging the client into a JFrame and starting the applet.
 */
public class Game extends JFrame implements AppletStub, ComponentListener, WindowListener {
	
	// Singleton
	private static Game instance = null;
	
	private JConfig m_config = new JConfig();
	private Applet m_applet = null;
	
	private Game() {
		// Empty private constructor to prevent extra instances from being created.
	}
	
	public void setApplet(Applet applet) {
		m_applet = applet;
		m_applet.setStub(this);
	}
	
	public Applet getApplet() {
		return m_applet;
	}
	
	/**
	 * Builds the main game client window and adds the applet to it.
	 */
	public void start() {
		if (m_applet == null)
			return;
		
		// Set window icon
		URL iconURL = Settings.getResource("/assets/icon.png");
		if (iconURL != null) {
			ImageIcon icon = new ImageIcon(iconURL);
			setIconImage(icon.getImage());
		}
		
		// Set window properties
		setResizable(true);
		addWindowListener(this);
		
		// Add applet to window
		setContentPane(m_applet);
		getContentPane().setBackground(Color.BLACK);
		getContentPane().setPreferredSize(new Dimension(512, 346));
		pack();
		
		// Hide cursor if software cursor
		Settings.checkSoftwareCursor();
		
		// Position window and make it visible
		setLocationRelativeTo(null);
		setVisible(true);
		
		setMinimumSize(new Dimension(1, 1));
		addComponentListener(this);
		
		Reflection.Load();
		Renderer.init();
		
		if (Settings.CUSTOM_CLIENT_SIZE) {
			Game.getInstance().resizeFrameWithContents();
		}
	}
	
	public JConfig getJConfig() {
		return m_config;
	}
	
	/**
	 * Starts the game applet.
	 */
	public void launchGame() {
		m_config.changeWorld(Settings.WORLD);
		m_applet.init();
		m_applet.start();
	}
	
	@Override
	public void setTitle(String title) {
		String t = "rscplus";
		
		if (title != null)
			t = t + " (" + title + ")";
		
		super.setTitle(t);
	}
	
	/*
	 * AppletStub methods
	 */
	
	@Override
	public final URL getCodeBase() {
		return m_config.getURL("codebase");
	}
	
	@Override
	public final URL getDocumentBase() {
		return getCodeBase();
	}
	
	@Override
	public final String getParameter(String key) {
		return m_config.parameters.get(key);
	}
	
	@Override
	public final AppletContext getAppletContext() {
		return null;
	}
	
	@Override
	public final void appletResize(int width, int height) {
	}
	
	/*
	 * WindowListener methods
	 */
	
	@Override
	public final void windowClosed(WindowEvent e) {
		if (m_applet == null)
			return;
		
		m_applet.stop();
		m_applet.destroy();
	}
	
	@Override
	public final void windowClosing(WindowEvent e) {
		dispose();
		Launcher.getConfigWindow().disposeJFrame();
		TrayHandler.removeTrayIcon();
		NotificationsHandler.closeNotificationSoundClip();
		NotificationsHandler.disposeNotificationHandler();
	}
	
	@Override
	public final void windowOpened(WindowEvent e) {
	}
	
	@Override
	public final void windowDeactivated(WindowEvent e) {
	}
	
	@Override
	public final void windowActivated(WindowEvent e) {
	}
	
	@Override
	public final void windowDeiconified(WindowEvent e) {
	}
	
	@Override
	public final void windowIconified(WindowEvent e) {
	}
	
	/*
	 * ComponentListener methods
	 */
	
	@Override
	public final void componentHidden(ComponentEvent e) {
	}
	
	@Override
	public final void componentMoved(ComponentEvent e) {
	}
	
	@Override
	public final void componentResized(ComponentEvent e) {
		if (m_applet == null)
			return;
		
		// Handle minimum size and launch game
		// TODO: This is probably a bad spot and should be moved
		if (getMinimumSize().width == 1) {
			setMinimumSize(getSize());
			launchGame();
		}
		
		Renderer.resize(getContentPane().getWidth(), getContentPane().getHeight());
	}
	
	@Override
	public final void componentShown(ComponentEvent e) {
	}
	
	/**
	 * Gets the game client instance. It makes one if one doesn't exist.
	 * 
	 * @return The game client instance
	 */
	public static Game getInstance() {
		if (instance == null) {
			synchronized (Game.class) {
				instance = new Game();
			}
		}
		return instance;
	}
	
	/**
	 * Resizes the Game window to match the X and Y values stored in Settings. The applet's size will be recalculated on
	 * the next rendering tick.
	 */
	public void resizeFrameWithContents() {
		int windowWidth = Settings.CUSTOM_CLIENT_SIZE_X + getInsets().left + getInsets().right;
		int windowHeight = Settings.CUSTOM_CLIENT_SIZE_Y + getInsets().top + getInsets().bottom;
		setSize(windowWidth, windowHeight);
	}
	
}
