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

public class TrayHandler {

	private static TrayIcon trayIcon;
	private static SystemTray tray;
	
	// TODO: Make double clicking the tray icon pull up the game
	// TODO: Make clicking the tray notification pull up the game
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
	
	public static void removeTrayIcon() {
		tray.remove(trayIcon);
	}

	public static void makePopupNotification(String title, String msg) {
		trayIcon.displayMessage(title, msg, TrayIcon.MessageType.NONE);
	}
}