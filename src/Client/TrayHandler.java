package Client;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;

/**
 * Handles the creation of system tray icons and notifications
 */
public class TrayHandler {

	/* TODO: Implement custom notifications that work on Windows, OS X, and Linux, but keep using system notifications on Windows 10 maybe? Might be too complicated to add a special case.
	 * TODO: When the notification is clicked on Windows 10, it should bring up the game client
	 * TODO: When the tray icon is clicked, it should bring up the game client
	 * TODO: Let the user disable the tray icon without disabling notifications (not sure it's possible with Windows 10 if using system notifications) 
	 * TODO: If using a custom notification, add a sound effect when a notification is triggered (toggled using Settings.NOTIFICATION_SOUNDS)
	 * TODO: Figure out if notification sounds should only play on systems that don't support system notifications
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
	public static void makePopupNotification(String title, String msg) {
		trayIcon.displayMessage(title, msg, TrayIcon.MessageType.NONE);
	}
}