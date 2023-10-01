/**
 * rscplus
 *
 * <p>This file is part of rscplus.
 *
 * <p>rscplus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscplus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscplus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/RSCPlus/rscplus>
 */
package Client;

import static Client.Util.osScaleMul;

import Client.ConfigWindow.ConfigTab;
import Game.Game;
import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import javax.imageio.ImageIO;
import javax.swing.JTabbedPane;

/** Handles the creation of system tray icons and notifications */
public class TrayHandler implements MouseListener {

  /*
   * TODO: When the notification is clicked, it should bring up the game client
   * TODO: Let the user disable the tray icon without disabling notifications
   */

  private static TrayIcon trayIcon;
  private static SystemTray tray;

  /** Creates the tray icon. */
  public static void initTrayIcon() {

    // Load images
    Image trayIconImage = null;
    try {
      trayIconImage = ImageIO.read(Launcher.getResource("/assets/icon-small.gif"));
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

    Font scaledFont = new Font("sans-serif", Font.PLAIN, osScaleMul(12));

    // Create popup menu
    PopupMenu popup = new PopupMenu();

    MenuItem about = new MenuItem("About RSC+");
    about.setFont(scaledFont);

    JTabbedPane settingsTabbedPane = Launcher.getConfigWindow().tabbedPane;

    int authorsTabIndex = ConfigTab.getTabIndex(ConfigTab.AUTHORS);

    if (authorsTabIndex > -1) {
      about.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              Launcher.getConfigWindow().setInitiatedTab(-1); // Reset current tab
              settingsTabbedPane.setSelectedIndex(authorsTabIndex);
              Launcher.getConfigWindow().showConfigWindow();
            }
          });
    } else {
      about.setEnabled(false);
    }

    MenuItem settings = new MenuItem("Settings");
    settings.setFont(scaledFont);

    settings.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Launcher.getConfigWindow().setInitiatedTab(-1); // Reset current tab
            settingsTabbedPane.setSelectedIndex(0);
            Launcher.getConfigWindow().showConfigWindow();
          }
        });

    MenuItem exit = new MenuItem("Exit");
    exit.setFont(scaledFont);

    exit.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            // TODO: Perhaps find a way to close the client from the tray icon and call both
            // WindowClosing() and
            // WindowClosed(), though nothing seems broken from doing it this way
            Game.getInstance()
                .dispatchEvent(new WindowEvent(Game.getInstance(), WindowEvent.WINDOW_CLOSING));
          }
        });

    popup.add(about);
    popup.addSeparator();
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

  /** Removes the system tray icon. */
  public static void removeTrayIcon() {
    if (tray != null && trayIcon != null) tray.remove(trayIcon);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    ScaledWindow.getInstance().toFront();
  }

  @Override
  public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  /** @return the trayIcon */
  public static TrayIcon getTrayIcon() {
    return trayIcon;
  }
}
