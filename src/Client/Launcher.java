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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import Game.Client;
import Game.Game;

/**
 * Singleton main class which renders a loading window and the game client window.
 */
public class Launcher extends JFrame implements Runnable {
	
	// Singleton
	private static Launcher instance;
	private static ConfigWindow window;
	
	public static ImageIcon icon = null;
	public static ImageIcon icon_warn = null;
	
	private JProgressBar m_progressBar;
	private JClassLoader m_classLoader;
	
	private Launcher() {
		// Empty private constructor to prevent extra instances from being created.
	}
	
	/**
	 * Renders the launcher progress bar window, then calls {@link #run()}.
	 */
	public void init() {
		Logger.Info("Starting rscplus");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.BLACK);
		
		// Set window icon
		URL iconURL = Settings.getResource("/assets/icon.png");
		if (iconURL != null) {
			icon = new ImageIcon(iconURL);
			setIconImage(icon.getImage());
		}
		iconURL = Settings.getResource("/assets/icon_warn.png");
		if (iconURL != null) {
			icon_warn = new ImageIcon(iconURL);
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
	
	/**
	 * Generates a config file if needed and launches the main client window.
	 */
	@Override
	public void run() {
		// Generates a config file if needed
		Settings.save();
		
		if (Settings.UPDATE_CONFIRMATION.get(Settings.currentProfile)) {
			int response = JOptionPane.showConfirmDialog(this, "rscplus has an automatic update feature.\n" +
					"\n" +
					"When enabled, rscplus will prompt for and install updates when launching the client.\n" +
					"The updates are obtained from our 'Latest' release on GitHub.\n" +
					"\n" +
					"Would you like to enable this feature?\n" +
					"\n" +
					"NOTE: This option can be toggled in the Settings interface under the General tab.", "rscplus", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
			if (response == JOptionPane.YES_OPTION || response == JOptionPane.CLOSED_OPTION) {
				Settings.CHECK_UPDATES.put(Settings.currentProfile, true);
				JOptionPane.showMessageDialog(this, "rscplus is set to check for updates on GitHub at every launch!", "rscplus", JOptionPane.INFORMATION_MESSAGE, icon);
			}
			else if (response == JOptionPane.NO_OPTION) {
				Settings.CHECK_UPDATES.put(Settings.currentProfile, false);
				JOptionPane.showMessageDialog(this, "rscplus will not check for updates automatically.\n" +
						"\n" +
						"You will not get notified when new releases are available. To update your client, you\n" +
						"will need to do it manually by replacing 'rscplus.jar' in your rscplus directory.\n" +
						"\n" +
						"You can enable GitHub updates again in the Settings interface under the General tab.", "rscplus", JOptionPane.INFORMATION_MESSAGE, icon_warn);
			}
			Settings.UPDATE_CONFIRMATION.put(Settings.currentProfile, false);
			Settings.save();
		}
			
		if (Settings.CHECK_UPDATES.get(Settings.currentProfile)) {
			setStatus("Checking for rscplus update...");
			double latestVersion = Client.fetchLatestVersionNumber();
			if (Settings.VERSION_NUMBER < latestVersion) {
				setStatus("rscplus update is available");
				// TODO: before Y10K update this to %9.6f
				int response = JOptionPane.showConfirmDialog(this, "An rscplus client update is available!\n" +
						"\n" +
						"Latest: " + String.format("%8.6f", latestVersion) + "\n" +
						"Installed: " + String.format("%8.6f", Settings.VERSION_NUMBER) + "\n" +
						"\n" +
						"Would you like to update now?", "rscplus", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
				if (response == JOptionPane.YES_OPTION) {
					if (updateJar()) {
						JOptionPane.showMessageDialog(this, "rscplus has been updated successfully!\n" +
								"\n" +
								"The client requires a restart, and will now exit.", "rscplus", JOptionPane.INFORMATION_MESSAGE, icon);
						System.exit(0);
					}
					else {
						response = JOptionPane.showConfirmDialog(this, "rscplus has failed to update, please try again later.\n" +
								"\n" +
								"Would you like to continue without updating?", "rscplus", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, icon_warn);
						if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
							System.exit(0);
						}
					}
				}
			}
		}
		
		setStatus("Creating JConfig...");
		JConfig config = Game.getInstance().getJConfig();
		config.create(Settings.WORLD.get(Settings.currentProfile));
		
		m_classLoader = new JClassLoader();
		if (!m_classLoader.fetch("/assets/rsc.jar")) {
			error("Unable to fetch Jar");
		}
		
		setStatus("Launching game...");
		Game game = Game.getInstance();
		try {
			Class<?> client = m_classLoader.loadClass(config.getJarClass());
			game.setApplet((Applet)client.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
			error("Unable to launch game");
			return;
		}
		setVisible(false);
		dispose();
		game.start();
	}
	
	/**
	 * Changes the launcher progress bar text and pauses the thread for 5 seconds.
	 * 
	 * @param text the text to change the progress bar text to
	 */
	public void error(String text) {
		setStatus("Error: " + text);
		try {
			Thread.sleep(5000);
			System.exit(0);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Changes the launcher progress bar text.
	 * 
	 * @param text the text to change the progress bar text to
	 */
	public void setStatus(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_progressBar.setString(text);
			}
		});
	}
	
	public boolean updateJar() {
		boolean success = true;
		
		setStatus("Starting rscplus update...");
		setProgress(0, 1);
		
		try {
			URL url = new URL("https://github.com/OrN/rscplus/releases/download/Latest/rscplus.jar");
			
			// Open connection
			URLConnection connection = url.openConnection();
			
			int size = connection.getContentLength();
			int offset = 0;
			byte[] data = new byte[size];

			InputStream input = url.openStream();
			
			int readSize;
			while ((readSize = input.read(data, offset, size - offset)) != -1) {
				offset += readSize;
				setStatus("Updating rscplus (" + (offset / 1024) + "KiB / " + (size / 1024) + "KiB)");
				setProgress(offset, size);
			}
			
			if (offset != size) {
				success = false;
			} else {
				// TODO: Get the jar filename in Settings.initDir
				File file = new File(Settings.Dir.JAR + "/rscplus.jar");
				FileOutputStream output = new FileOutputStream(file);
				output.write(data);
				output.close();
				
				setStatus("rscplus update complete");
			}
		} catch (Exception e) {
			success = false;
		}
		
		return success;
	}
	
	/**
	 * Sets the progress value of the launcher progress bar.
	 * 
	 * @param value the number of tasks that have been completed
	 * @param total the total number of tasks to complete
	 */
	public void setProgress(final int value, final int total) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (total == 0) {
					m_progressBar.setValue(0);
					return;
				}
				
				m_progressBar.setValue(value * 100 / total);
			}
		});
	}
	
	public JClassLoader getClassLoader() {
		return m_classLoader;
	}
	
	public static void main(String[] args) {
		Settings.initDir();
		setConfigWindow(new ConfigWindow());
		Settings.initSettings();
		TrayHandler.initTrayIcon();
		NotificationsHandler.initialize();
		Launcher.getInstance().init();
	}
	
	public static Launcher getInstance() {
		if (instance == null) {
			synchronized (Launcher.class) {
				instance = new Launcher();
			}
		}
		return instance;
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
	
}
