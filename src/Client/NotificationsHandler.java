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
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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

public class NotificationsHandler {
		
	static JFrame notificationFrame;
	static JLabel iconLabel;
	static JLabel notificationTitle;
	static JTextArea notificationTextArea;
	static JPanel mainContentPanel;
	static Thread notifTimeoutThread;
	static long notifLastShownTime;
	
	/**
	 * @wbp.parser.entryPoint
	 * Initializes the Notification JFrame and prepares it to receive notifications
	 * TODO: Strongly consider moving all of this to the dispatch thread
	 */
	public static void initialize() {
		
		NotifsShowGameMouseListener mouseManager = new NotifsShowGameMouseListener();
		
		notificationFrame = new JFrame();
		JPanel contentPanel = new JPanel();
		notificationFrame.setContentPane(contentPanel);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		
		notificationFrame.setBounds(width-446, height-154, 449, 104);
		notificationFrame.setUndecorated(true);
		notificationFrame.setShape(new RoundRectangle2D.Double(0, 0, notificationFrame.getWidth(), notificationFrame.getHeight(), 16, 16));
		notificationFrame.setBackground(new Color(0,0,0,0));
		notificationFrame.setAutoRequestFocus(false);
		notificationFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		notificationFrame.setType(Window.Type.UTILITY);
		notificationFrame.setAlwaysOnTop(true);	
		
		contentPanel.setLayout(null);
		contentPanel.setBackground(new Color(0,0,0,0));
		
		mainContentPanel = new JPanel();
		mainContentPanel.setBounds(13, 13, 423, 79);
		mainContentPanel.setLayout(null);
		mainContentPanel.setBackground(new Color(249,249,247,0));
		mainContentPanel.addMouseListener(mouseManager);
		contentPanel.add(mainContentPanel);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(232,232,230,0));
		panel.setBounds(0, 0, 79, 79);
		panel.setLayout(new BorderLayout(0, 0));
		mainContentPanel.add(panel);

		
		iconLabel = new JLabel();
		iconLabel.setIcon(new ImageIcon(Settings.getResource("/assets/icon.png")));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		panel.add(iconLabel, BorderLayout.CENTER);
					
		notificationTitle = new JLabel();
		notificationTitle.setBounds(91, 3, 326, 26);
		notificationTitle.setForeground(new Color(0x1d, 0x1d, 0x1d));
		mainContentPanel.add(notificationTitle);
		
		notificationTextArea = new JTextArea();
		notificationTextArea.setDisabledTextColor(new Color(0x3f, 0x3f, 0x3f));
		notificationTextArea.setFocusable(false);
		notificationTextArea.setEnabled(false);
		notificationTextArea.setEditable(false);
		notificationTextArea.setBorder(null);
		notificationTextArea.setBackground(new Color(0,0,0,0));
		notificationTextArea.setLineWrap(true);
		notificationTextArea.setBounds(91, 30, 326, 43);
		notificationTextArea.addMouseListener(mouseManager);
		mainContentPanel.add(notificationTextArea);
		
		JButton closeButton = new JButton("");
		closeButton.addActionListener(new ActionListener() {
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
		
		
		JLabel lblNewLabel = new JLabel("");
		ImageIcon img = null;
		if (System.getProperty("os.name").contains("Windows")) {
			img = new ImageIcon(Settings.getResource("/assets/notification_background.png"));
			lblNewLabel.setBounds(0, 0, 442, 104);
		} else {
			img = new ImageIcon(Settings.getResource("/assets/notification_background_compat.png"));
			lblNewLabel.setBounds(0, 0, 422, 78);
			mainContentPanel.setBounds(0, 0, 422, 78);
			notificationFrame.setBounds(width-446, height-154, 422, 78);
			
		}
		lblNewLabel.setIcon(img);
		lblNewLabel.setBackground(new Color(0,0,0,0));
		lblNewLabel.setForeground(new Color(0,0,0,0));
		lblNewLabel.setOpaque(false);
		contentPanel.add(lblNewLabel);
		
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
	 * @return
	 */
	private static void createNotifTimerThread() {
		setLastNotifTime(0);
		notifTimeoutThread = new Thread(new NotifTimeoutHandler());
		notifTimeoutThread.start();
	}
	
	/**
	 * Thread for the notification timeout thread
	 *
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
				Logger.Error("The notifications timeout thread was interrupted unexpectedly!");
				//End the thread
			}
			
		}
	}
	
	/**
	 * 
	 * @param time - Current system time, or -1 to terminate the timeout thread. If this has been set to -1, it cannot be reset; this should only be done on close.
	 */
	public static synchronized void setLastNotifTime(long time) {
		if (notifLastShownTime != -1)
		notifLastShownTime = time;
	}
	
	/**
	 * 
	 * @return - The last millis system time of a notification being shown.
	 */
	public static synchronized long getLastNotifTime() {
		return notifLastShownTime;
	}
	
	/**
	 * Displays a notification, playing sound if it is enabled
	 * @param title - The title of the notification
	 * @param text - Text message of the notification
	 */
	public static void displayNotification(final String title, String text) {
		// TODO: Add fade-in and fade-out or slide-in and slide-out animations
		final String sanitizedText = text.replaceAll("@...@", "").replaceAll("~...~", ""); // Remove color/formatting codes
		
		if (SwingUtilities.isEventDispatchThread()) {
			if(Settings.USE_SYSTEM_NOTIFICATIONS && SystemTray.isSupported()) {
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
					if(Settings.USE_SYSTEM_NOTIFICATIONS && SystemTray.isSupported()) {
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
		
		if(Settings.USE_SYSTEM_NOTIFICATIONS && SystemTray.isSupported()) {
			// TODO: When you click the system notification, it should focus the game client
			TrayHandler.getTrayIcon().displayMessage(title, sanitizedText, MessageType.NONE);
		} else {
			setNotificationWindowVisible(true);
			notificationTitle.setText(title);
			notificationTextArea.setText(sanitizedText);
			notificationFrame.repaint();
		}
		if (Settings.NOTIFICATION_SOUNDS) {
			playNotificationSound();
		}
		setLastNotifTime(System.currentTimeMillis());
	}
	
	/**
	 * Sets visibility of the notification window. If this method is called from a thread other than the event dispatch thread, 
	 * it will invokeLater() to hide the thread the next time the EDT is not busy.
	 * @param isVisible - Whether the window should be visible
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
