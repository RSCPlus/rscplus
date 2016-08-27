package Client;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import Game.Game;

/**
 * Handles the creation of system tray icons and notifications
 */
public class TrayHandler implements MouseListener {

	/* TODO: Implement custom notifications that work on Windows, OS X, and Linux
	 * TODO: When the notification is clicked, it should bring up the game client
	 * TODO: Let the user disable the tray icon without disabling notifications
	 * TODO: Add a sound effect when a notification is triggered (toggled using Settings.NOTIFICATION_SOUNDS)
	 */
	
	private static TrayIcon trayIcon;
	private static SystemTray tray;
	
	public static void initTrayIcon() {

		// Load images
		Image trayIconImage = null;
		try {
			trayIconImage = ImageIO.read(Settings.getResource("/assets/icon-small.gif"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (!SystemTray.isSupported()) {
			Logger.Error("System tray is not supported on OS");
			return;
		}
		
		trayIcon = new TrayIcon(trayIconImage);
		trayIcon.addMouseListener(new TrayHandler());
		
		tray = SystemTray.getSystemTray();

		// Create popup menu
		PopupMenu popup = new PopupMenu();
		MenuItem settings = new MenuItem("Settings");
		MenuItem exit = new MenuItem("Exit");

		settings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Launcher.getConfigWindow().showConfigWindow();
			}
		});
		
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: Close everything, making sure to trigger windowClosing() to clean up
				tray.remove(trayIcon);
				NotificationsHandler.closeNotificationSoundClip();
				System.exit(0);
			}
		});

		popup.add(settings);
		popup.add(exit);

		// Add tooltip and menu to trayIcon
		trayIcon.setToolTip("RSC+ Client");
		trayIcon.setPopupMenu(popup);

		// Add the trayIcon to system tray/notification area
		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			Logger.Error("Could not load tray icon");
		}
	}
	
	/**
	 * Remove the system tray icon
	 */
	public static void removeTrayIcon() {
		tray.remove(trayIcon);
	}

	/**
	 * Makes a system notification popup
	 * @param title - The title of the notification
	 * @param msg - The main content of the notification
	 */
//	public static void makePopupNotification(String title, String msg) {
//		NotificationsPanel.displayNotification(title, msg);
//	}
//	


	@Override
	public void mouseClicked(MouseEvent e) {
		Game.getInstance().toFront();
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}
}