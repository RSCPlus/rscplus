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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.TrayIcon.MessageType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import Game.Game;

/**
 * Handles system and pseudo-system notifications
 */
public class NotificationsHandler {
	
	static JFrame notificationFrame;
	static JLabel iconLabel;
	static JLabel notificationTitle;
	static JTextArea notificationTextArea;
	static JPanel mainContentPanel;
	static Thread notifTimeoutThread;
	static long notifLastShownTime;
	
	public enum NotifType {
		PM, TRADE, DUEL, LOGOUT, LOWHP, FATIGUE
	}
	
	/**
	 * Initializes the Notification JFrame and prepares it to receive notifications
	 */
	public static void initialize() {
		Logger.Info("Creating notification window");
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					runInit();
				}
			});
		} catch (InvocationTargetException e) {
			Logger.Error("There was a thread-related error while setting up the notifications window!");
			e.printStackTrace();
		} catch (InterruptedException e) {
			Logger.Error("There was a thread-related error while setting up the notifications window! The window may not be initialized properly!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets up pseudo-system notifications.
	 */
	private static void runInit() {
		NotifsShowGameMouseListener mouseManager = new NotifsShowGameMouseListener();
		
		// Get Monitor size for GUI.
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		
		// 1
		notificationFrame = new JFrame();
		JPanel contentPanel = new JPanel();
		notificationFrame.setContentPane(contentPanel);
		
		notificationFrame.setUndecorated(true);
		notificationFrame.setAutoRequestFocus(false);
		notificationFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		// TODO: Will changing this for Linux affect whether it has a "taskbar" icon?
		notificationFrame.setType(Window.Type.UTILITY);
		notificationFrame.setAlwaysOnTop(true);
		contentPanel.setLayout(null);
		
		// 2
		mainContentPanel = new JPanel();
		mainContentPanel.setLayout(null);
		
		mainContentPanel.addMouseListener(mouseManager);
		
		// 3
		JPanel iconPanel = new JPanel();
		iconPanel.setBounds(0, 0, 79, 79);
		iconPanel.setLayout(new BorderLayout(0, 0));
		
		// 4
		iconLabel = new JLabel();
		iconLabel.setIcon(new ImageIcon(Settings.getResource("/assets/icon.png")));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		iconPanel.add(iconLabel, BorderLayout.CENTER);
		
		// 5
		notificationTitle = new JLabel();
		notificationTitle.setBounds(91, 3, 326, 26);
		notificationTitle.setForeground(new Color(0x1d, 0x1d, 0x1d));
		mainContentPanel.add(notificationTitle);
		
		// 6
		notificationTextArea = new JTextArea();
		notificationTextArea.setDisabledTextColor(new Color(0x3f, 0x3f, 0x3f));
		notificationTextArea.setFocusable(false);
		notificationTextArea.setEnabled(false);
		notificationTextArea.setEditable(false);
		notificationTextArea.setBorder(null);
		notificationTextArea.setLineWrap(true);
		notificationTextArea.setBounds(91, 30, 326, 43);
		notificationTextArea.addMouseListener(mouseManager);
		
		// 7
		JButton closeButton = new JButton("");
		closeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setNotificationWindowVisible(false);
			}
		});
		closeButton.setBounds(400, 5, 17, 17);
		closeButton.setIcon(new ImageIcon(Settings.getResource("/assets/notification_close.png")));
		closeButton.setSelectedIcon(new ImageIcon(Settings.getResource("/assets/notification_close_highlighted.png")));
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		closeButton.setContentAreaFilled(false);
		mainContentPanel.add(closeButton);
		
		// 8 (add the background image to the JPanel if on windows)
		
		/*
		 * So basically, if we're running windows, everything renders normally and looks great. If we aren't, we assume
		 * everything breaks and revert to a simpler but compatible look
		 */
		if (System.getProperty("os.name").contains("Windows")) {
			// 1
			// Configure the frame to have rounded corners and to be transparent
			notificationFrame.setShape(new RoundRectangle2D.Double(0, 0, notificationFrame.getWidth(), notificationFrame.getHeight(), 16, 16));
			
			notificationFrame.setBackground(new Color(0, 0, 0, 0)); // Make the JFrame itself transparent.
			contentPanel.setBackground(new Color(0, 0, 0, 0));
			notificationFrame.setBounds(width - 446, height - 154, 449, 104);
			notificationFrame.setMaximumSize(new Dimension(449, 104));
			notificationFrame.setMaximizedBounds(new Rectangle(width - 446, height - 154, 449, 104));
			
			// 2
			mainContentPanel.setBounds(13, 13, 423, 79);
			mainContentPanel.setBackground(new Color(249, 249, 247, 0));
			
			contentPanel.add(mainContentPanel); // To make sure it's added at a reasonable time
			
			// 3
			iconPanel.setBackground(new Color(232, 232, 230, 0));
			mainContentPanel.add(iconPanel);
			
			// 4 (nothing to do)
			
			// 5 (nothing to do)
			
			// 6
			notificationTextArea.setBackground(new Color(0, 0, 0, 0));
			notificationTextArea.setOpaque(false);
			mainContentPanel.add(notificationTextArea);
			
			// 7 (button, nothing to do yet)
			
			// 8 (Add the background image
			JLabel backgroundImage = new JLabel("");
			ImageIcon img = null;
			
			img = new ImageIcon(Settings.getResource("/assets/notification_background.png"));
			backgroundImage.setBounds(0, 0, 442, 104);
			
			backgroundImage.setIcon(img);
			backgroundImage.setBackground(new Color(0, 0, 0, 0));
			backgroundImage.setForeground(new Color(0, 0, 0, 0));
			backgroundImage.setOpaque(false);
			contentPanel.add(backgroundImage);
			
		} else {
			// TODO: Consider OS-dependent locations for the notification window
			// 1
			notificationFrame.setBounds(width - 446, height - 154, 425, 81);
			notificationFrame.setMaximumSize(new Dimension(425, 81));
			notificationFrame.setMaximizedBounds(new Rectangle(width - 446, height - 154, 425, 81));
			contentPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(172, 172, 172)));
			
			// 2
			mainContentPanel.setBounds(1, 1, 423, 79);
			mainContentPanel.setBackground(new Color(249, 249, 247));
			
			contentPanel.add(mainContentPanel); // To make sure it's added at a reasonable time
			
			// 3
			iconPanel.setBackground(new Color(232, 232, 230));
			iconPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(196, 196, 194)));
			mainContentPanel.add(iconPanel);
			
			// 4 (nothing to do)
			
			// 5 (nothing to do)
			
			// 6
			notificationTextArea.setBackground(new Color(249, 249, 247, 0));
			notificationTextArea.setOpaque(false);
			mainContentPanel.add(notificationTextArea);
			
			// 7 (button, nothing to do yet)
			
			// 8 (Add background image if windows for the shadow effect)
			
		}
		
		try {
			Font font = Font.createFont(Font.TRUETYPE_FONT, Settings.getResourceAsStream("/assets/OpenSans-Regular.ttf"));
			Font boldFont = Font.createFont(Font.TRUETYPE_FONT, Settings.getResourceAsStream("/assets/OpenSans-Bold.ttf"));
			
			notificationTitle.setFont(boldFont.deriveFont(Font.BOLD, 18f));
			notificationTextArea.setFont(font.deriveFont(Font.PLAIN, 16f));
		} catch (FontFormatException | IOException e) {
			Logger.Error("Error while setting up notifications font:" + e.getMessage());
			e.printStackTrace();
		}
		
		loadNotificationSound();
		notificationFrame.repaint();
		createNotifTimerThread();
		
	}
	
	/**
	 * Creates the notification timeout thread for closing the non-native notification after a few seconds
	 */
	private static void createNotifTimerThread() {
		setLastNotifTime(0);
		notifTimeoutThread = new Thread(new NotifTimeoutHandler());
		try {
			notifTimeoutThread.setName("Notifications Timeout");
		} catch (SecurityException e) {
			Logger.Error("Access denied attempting to set the name of the notifications thread. This is not fatal.");
		}
		// Make sure this thread doesn't keep the program alive if something breaks horribly
		notifTimeoutThread.setDaemon(true);
		notifTimeoutThread.start();
	}
	
	/**
	 * Thread for the notification timeout thread
	 */
	static class NotifTimeoutHandler implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(500);
					if (getLastNotifTime() == -1) {
						break;
					}
					if (notificationFrame.isVisible()) {
						if (System.currentTimeMillis() > (getLastNotifTime() + 8000L)) {
							NotificationsHandler.setNotificationWindowVisible(false);
						}
					}
				}
			} catch (InterruptedException e) {
				Logger.Error("The notifications timeout thread was interrupted unexpectedly! Perhaps the game crashed or was killed?");
				// End the thread
			}
		}
	}
	
	/**
	 * @param time Current system time, or -1 to terminate the timeout thread. If this has been set to -1, it cannot be
	 * reset; this should only be done on close.
	 */
	public static synchronized void setLastNotifTime(long time) {
		if (notifLastShownTime != -1)
			notifLastShownTime = time;
	}
	
	/**
	 * @return The last millis system time of a notification being shown.
	 */
	public static synchronized long getLastNotifTime() {
		return notifLastShownTime;
	}
	
	/**
	 * Displays/plays a notification popup or sound. This method checks whether each of the respective settings for that
	 * specific notification type.<br>
	 * This method does <i>not</i> check for values such as low HP or fatigue amounts, as the code that does so is local
	 * to the Render method.
	 * 
	 * @param type The NotifType to display. This can be one of SYSTEM, PM, TRADE, DUEL LOGOUT, LOWHP, or FATIGUE as of
	 * the writing of this documentation.
	 * @param title The title to use for the notification.
	 * @param text Text message of the notification.
	 * @return True if at least one type of notification (audio/popup) was attempted; false otherwise
	 */
	public static boolean notify(NotifType type, String title, String text) {
		boolean didNotify = false;
		
		switch (type) {
		case PM: {
			if (Settings.PM_NOTIFICATIONS) {
				if (Settings.NOTIFICATION_SOUNDS) {
					// If always notification sounds or if game isn't focused, play audio
					if (Settings.SOUND_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						playNotificationSound();
						didNotify = true;
					}
				}
				if (Settings.TRAY_NOTIFS) {
					// If always tray notifications or if game isn't focused, display tray notification
					if (Settings.TRAY_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						displayNotification(title, text);
						didNotify = true;
					}
				}
			}
			break;
		}
		case TRADE: {
			if (Settings.TRADE_NOTIFICATIONS) {
				if (Settings.NOTIFICATION_SOUNDS) {
					// If always notification sounds or if game isn't focused, play audio
					if (Settings.SOUND_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						playNotificationSound();
						didNotify = true;
					}
				}
				if (Settings.TRAY_NOTIFS) {
					// If always tray notifications or if game isn't focused, display tray notification
					if (Settings.TRAY_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						displayNotification(title, text);
						didNotify = true;
					}
				}
			}
			break;
		}
		case DUEL: {
			if (Settings.DUEL_NOTIFICATIONS) {
				if (Settings.NOTIFICATION_SOUNDS) {
					// If always notification sounds or if game isn't focused, play audio
					if (Settings.SOUND_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						playNotificationSound();
						didNotify = true;
					}
				}
				if (Settings.TRAY_NOTIFS) {
					// If always tray notifications or if game isn't focused, display tray notification
					if (Settings.TRAY_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						displayNotification(title, text);
						didNotify = true;
					}
				}
			}
			break;
		}
		case LOGOUT: {
			if (Settings.LOGOUT_NOTIFICATIONS) {
				if (Settings.NOTIFICATION_SOUNDS) {
					// If always notification sounds or if game isn't focused, play audio
					if (Settings.SOUND_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						playNotificationSound();
						didNotify = true;
					}
				}
				if (Settings.TRAY_NOTIFS) {
					// If always tray notifications or if game isn't focused, display tray notification
					if (Settings.TRAY_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						displayNotification(title, text);
						didNotify = true;
					}
				}
			}
			break;
		}
		case LOWHP: {
			if (Settings.LOW_HP_NOTIFICATIONS) {
				if (Settings.NOTIFICATION_SOUNDS) {
					// If always notification sounds or if game isn't focused, play audio
					if (Settings.SOUND_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						playNotificationSound();
						didNotify = true;
					}
				}
				if (Settings.TRAY_NOTIFS) {
					// If always tray notifications or if game isn't focused, display tray notification
					if (Settings.TRAY_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						displayNotification(title, text);
						didNotify = true;
					}
				}
			}
			break;
		}
		case FATIGUE: {
			if (Settings.FATIGUE_NOTIFICATIONS) {
				if (Settings.NOTIFICATION_SOUNDS) {
					// If always notification sounds or if game isn't focused, play audio
					if (Settings.SOUND_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						playNotificationSound();
						didNotify = true;
					}
				}
				if (Settings.TRAY_NOTIFS) {
					// If always tray notifications or if game isn't focused, display tray notification
					if (Settings.TRAY_NOTIFS_ALWAYS || (!Game.getInstance().getContentPane().hasFocus())) {
						displayNotification(title, text);
						didNotify = true;
					}
				}
			}
			break;
		}
		}
		return didNotify;
	}
	
	/**
	 * Displays a notification, playing sound if it is enabled
	 * 
	 * TODO: Add fade-in and fade-out or slide-in and slide-out animations
	 * 
	 * @param title The title of the notification
	 * @param text Text message of the notification
	 */
	public static void displayNotification(final String title, String text) {
		// Remove color/formatting codes
		final String sanitizedText = text.replaceAll("@...@", "").replaceAll("~...~", "");
		
		if (SwingUtilities.isEventDispatchThread()) {
			if (Settings.USE_SYSTEM_NOTIFICATIONS && SystemTray.isSupported()) {
				// TODO: When you click the system notification, it should focus the game client
				TrayHandler.getTrayIcon().displayMessage(title, sanitizedText, MessageType.NONE);
			} else {
				setNotificationWindowVisible(true);
				notificationTitle.setText(title);
				notificationTextArea.setText(sanitizedText);
				notificationFrame.repaint();
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					if (Settings.USE_SYSTEM_NOTIFICATIONS && SystemTray.isSupported()) {
						// TODO: When you click the system notification, it should focus the game client
						TrayHandler.getTrayIcon().displayMessage(title, sanitizedText, MessageType.NONE);
					} else {
						setNotificationWindowVisible(true);
						notificationTitle.setText(title);
						notificationTextArea.setText(sanitizedText);
						notificationFrame.repaint();
					}
				}
			});
		}
		setLastNotifTime(System.currentTimeMillis());
	}
	
	/**
	 * Sets visibility of the notification window. If this method is called from a thread other than the event dispatch
	 * thread, it will invokeLater() to hide the thread the next
	 * time the EDT is not busy.
	 * 
	 * @param isVisible Whether the window should be visible
	 */
	public static void setNotificationWindowVisible(final boolean isVisible) {
		
		if (SwingUtilities.isEventDispatchThread()) {
			notificationFrame.setVisible(isVisible);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					NotificationsHandler.notificationFrame.setVisible(isVisible);
				}
			});
		}
		
	}
	
	private static AudioInputStream notificationAudioIn;
	private static Clip notificationSoundClip;
	
	public static void loadNotificationSound() {
		try {
			notificationAudioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(Settings.getResourceAsStream("/assets/notification.wav")));
			notificationSoundClip = (Clip)AudioSystem.getLine(new DataLine.Info(Clip.class, notificationAudioIn.getFormat()));
			notificationSoundClip.open(notificationAudioIn);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public static void playNotificationSound() {
		if (notificationSoundClip == null)
			return;
		notificationSoundClip.stop();
		notificationSoundClip.flush();
		notificationSoundClip.setFramePosition(0);
		notificationSoundClip.start();
	}
	
	public static void closeNotificationSoundClip() {
		if (notificationSoundClip != null)
			notificationSoundClip.close();
	}
	
	public static void disposeNotificationHandler() {
		notificationFrame.dispose();
		setLastNotifTime(-1);
	}
}

class NotifsShowGameMouseListener implements MouseListener {
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		Game.getInstance().toFront();
		NotificationsHandler.setNotificationWindowVisible(false);
	}
	
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
	
	@Override
	public void mouseExited(MouseEvent arg0) {
	}
	
	@Override
	public void mousePressed(MouseEvent arg0) {
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
	
}
