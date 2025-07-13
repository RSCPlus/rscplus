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
import static Game.Renderer.exactStringIgnoreCaseIsWithinList;

import Game.Client;
import Game.Replay;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
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

/** Handles system and pseudo-system notifications */
public class NotificationsHandler {

  static JFrame notificationFrame;
  static JLabel iconLabel;
  static JLabel notificationTitle;
  static JTextArea notificationTextArea;
  static JPanel mainContentPanel;
  static Thread notifTimeoutThread;
  static long notifLastShownTime;
  static boolean hasNotifySend = Util.detectBinaryAvailable("notify-send", "native notifications");

  public enum NotifType {
    PM,
    TRADE,
    DUEL,
    LOGOUT,
    LOWHP,
    FATIGUE,
    HIGHLIGHTEDITEM,
    IMPORTANT_MESSAGE
  }

  /** Initializes the Notification JFrame and prepares it to receive notifications */
  public static void initialize() {
    Logger.Info("Creating notification window");
    try {
      SwingUtilities.invokeAndWait(
          new Runnable() {

            @Override
            public void run() {
              runInit();
            }
          });
    } catch (InvocationTargetException e) {
      Logger.Error("There was a thread-related error while setting up the notifications window!");
      e.printStackTrace();
    } catch (InterruptedException e) {
      Logger.Error(
          "There was a thread-related error while setting up the notifications window! The window may not be initialized properly!");
      e.printStackTrace();
    }
  }

  /** Sets up pseudo-system notifications. */
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
    iconPanel.setBounds(0, 0, osScaleMul(79), osScaleMul(79));
    iconPanel.setLayout(new BorderLayout(0, 0));

    // 4
    iconLabel = new JLabel();
    Image iconImg = new ImageIcon(Launcher.getResource(Launcher.largeIconPath)).getImage();
    iconLabel.setIcon(
        new ImageIcon(
            iconImg.getScaledInstance(
                osScaleMul(iconImg.getWidth(null)),
                osScaleMul(iconImg.getHeight(null)),
                Image.SCALE_DEFAULT)));
    iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
    iconLabel.setVerticalAlignment(SwingConstants.CENTER);
    iconPanel.add(iconLabel, BorderLayout.CENTER);

    // 5
    notificationTitle = new JLabel();
    notificationTitle.setBounds(osScaleMul(91), osScaleMul(3), osScaleMul(326), osScaleMul(26));
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
    notificationTextArea.setBounds(osScaleMul(91), osScaleMul(30), osScaleMul(326), osScaleMul(43));
    notificationTextArea.addMouseListener(mouseManager);

    // 7
    JButton closeButton = new JButton("");
    closeButton.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent arg0) {
            setNotificationWindowVisible(false);
          }
        });
    closeButton.setBounds(osScaleMul(400), osScaleMul(5), osScaleMul(17), osScaleMul(17));
    Image closeButtonImg =
        new ImageIcon(Launcher.getResource("/assets/notification_close.png")).getImage();
    closeButton.setIcon(
        new ImageIcon(
            closeButtonImg.getScaledInstance(
                osScaleMul(closeButtonImg.getWidth(null)),
                osScaleMul(closeButtonImg.getHeight(null)),
                Image.SCALE_DEFAULT)));
    Image closeHighlightButtonImg =
        new ImageIcon(Launcher.getResource("/assets/notification_close_highlighted.png"))
            .getImage();
    closeButton.setSelectedIcon(
        new ImageIcon(
            closeHighlightButtonImg.getScaledInstance(
                osScaleMul(closeHighlightButtonImg.getWidth(null)),
                osScaleMul(closeHighlightButtonImg.getHeight(null)),
                Image.SCALE_DEFAULT)));
    closeButton.setBorder(BorderFactory.createEmptyBorder());
    closeButton.setContentAreaFilled(false);
    mainContentPanel.add(closeButton);

    // 8 (add the background image to the JPanel if on windows)

    /*
     * So basically, if we're running windows, everything renders normally and looks great. If we aren't, we assume
     * everything breaks and revert to a simpler but compatible look
     */
    if (Util.isWindowsOS()) {
      // 1
      // Configure the frame to have rounded corners and to be transparent
      notificationFrame.setShape(
          new RoundRectangle2D.Double(
              0,
              0,
              osScaleMul(notificationFrame.getWidth()),
              osScaleMul(notificationFrame.getHeight()),
              osScaleMul(16),
              osScaleMul(16)));

      notificationFrame.setBackground(new Color(0, 0, 0, 0)); // Make the JFrame itself transparent.
      contentPanel.setBackground(new Color(0, 0, 0, 0));
      notificationFrame.setBounds(
          width - osScaleMul(446), height - osScaleMul(154), osScaleMul(449), osScaleMul(104));
      notificationFrame.setMaximumSize(osScaleMul(new Dimension(449, 104)));
      notificationFrame.setMaximizedBounds(
          new Rectangle(
              width - osScaleMul(446), height - osScaleMul(154), osScaleMul(449), osScaleMul(104)));

      // 2
      mainContentPanel.setBounds(osScaleMul(13), osScaleMul(13), osScaleMul(423), osScaleMul(79));
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
      Image backgroundImg =
          new ImageIcon(Launcher.getResource("/assets/notification_background.png")).getImage();
      backgroundImage.setBounds(0, 0, osScaleMul(442), osScaleMul(104));

      backgroundImage.setIcon(
          new ImageIcon(
              backgroundImg.getScaledInstance(
                  osScaleMul(backgroundImg.getWidth(null)),
                  osScaleMul(backgroundImg.getHeight(null)),
                  Image.SCALE_DEFAULT)));
      backgroundImage.setBackground(new Color(0, 0, 0, 0));
      backgroundImage.setForeground(new Color(0, 0, 0, 0));
      backgroundImage.setOpaque(false);
      contentPanel.add(backgroundImage);
    } else { // Linux, macOS, possibly others (BSD?)

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
      Font font =
          Font.createFont(
              Font.TRUETYPE_FONT, Launcher.getResourceAsStream("/assets/arial.ttf"));
      Font boldFont =
          Font.createFont(
              Font.TRUETYPE_FONT, Launcher.getResourceAsStream("/assets/Helvetica-Bold.ttf"));

      notificationTitle.setFont(boldFont.deriveFont(Font.BOLD, osScaleMul(18)));
      notificationTextArea.setFont(font.deriveFont(Font.PLAIN, osScaleMul(16)));
    } catch (FontFormatException | IOException e) {
      Logger.Error("Error while setting up notifications font:" + e.getMessage());
      e.printStackTrace();
    }

    loadNotificationSound();
    notificationFrame.repaint();
    createNotifTimerThread();
  }

  /**
   * Creates the notification timeout thread for closing the non-native notification after a few
   * seconds
   */
  private static void createNotifTimerThread() {
    setLastNotifTime(0);
    notifTimeoutThread = new Thread(new NotifTimeoutHandler());
    try {
      notifTimeoutThread.setName("Notifications Timeout");
    } catch (SecurityException e) {
      Logger.Error(
          "Access denied attempting to set the name of the notifications thread. This is not fatal.");
    }
    // Make sure this thread doesn't keep the program alive if something breaks horribly
    notifTimeoutThread.setDaemon(true);
    notifTimeoutThread.start();
  }

  /** Thread for the notification timeout thread */
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
        Logger.Error(
            "The notifications timeout thread was interrupted unexpectedly! Perhaps the game crashed or was killed?");
        // End the thread
      }
    }
  }

  /**
   * @param time Current system time, or -1 to terminate the timeout thread. If this has been set to
   *     -1, it cannot be reset; this should only be done on close.
   */
  public static synchronized void setLastNotifTime(long time) {
    if (notifLastShownTime != -1) notifLastShownTime = time;
  }

  /** @return The last millis system time of a notification being shown. */
  public static synchronized long getLastNotifTime() {
    return notifLastShownTime;
  }

  /**
   * Displays/plays a notification popup or sound. This method checks whether each of the respective
   * settings for that specific notification type.<br>
   * This method does <i>not</i> check for values such as low HP or fatigue amounts, as the code
   * that does so is local to the Render method.
   *
   * @param type The NotifType to display. This can be one of SYSTEM, PM, TRADE, DUEL LOGOUT, LOWHP,
   *     or FATIGUE as of the writing of this documentation.
   * @param title The title to use for the notification.
   * @param username The username to use for the notification, if available.
   * @param text Text message of the notification.
   * @return True if at least one type of notification (audio/popup) was attempted; false otherwise
   */
  public static boolean notify(NotifType type, String title, String username, String text) {
    return notify(type, title, username, text, "default");
  }

  public static boolean notify(
      NotifType type, String title, String username, String text, String sound) {
    boolean didNotify = false;

    if (Replay.isPlaying && !Settings.TRIGGER_ALERTS_REPLAY.get(Settings.currentProfile)) {
      return false;
    }

    switch (type) {
      case PM:
        {
          if (Settings.PM_NOTIFICATIONS.get(Settings.currentProfile)
              && !exactStringIgnoreCaseIsWithinList(username, Settings.PM_DENYLIST.get("custom"))) {
            if (Settings.NOTIFICATION_SOUNDS.get(Settings.currentProfile)) {
              // If always notification sounds or if game isn't focused, play audio
              if (Settings.SOUND_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                playNotificationSound(sound);
                didNotify = true;
              }
            }
            if (Settings.TRAY_NOTIFS.get(Settings.currentProfile)) {
              // If always tray notifications or if game isn't focused, display tray notification
              if (Settings.TRAY_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                displayNotification(title, text, "normal");
                didNotify = true;
              }
            }
          }
          break;
        }
      case TRADE:
        {
          if (Settings.TRADE_NOTIFICATIONS.get(Settings.currentProfile)) {
            if (Settings.NOTIFICATION_SOUNDS.get(Settings.currentProfile)) {
              // If always notification sounds or if game isn't focused, play audio
              if (Settings.SOUND_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                playNotificationSound(sound);
                didNotify = true;
              }
            }
            if (Settings.TRAY_NOTIFS.get(Settings.currentProfile)) {
              // If always tray notifications or if game isn't focused, display tray notification
              if (Settings.TRAY_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                displayNotification(title, text, "normal");
                didNotify = true;
              }
            }
          }
          break;
        }
      case DUEL:
        {
          if (Settings.DUEL_NOTIFICATIONS.get(Settings.currentProfile)) {
            if (Settings.NOTIFICATION_SOUNDS.get(Settings.currentProfile)) {
              // If always notification sounds or if game isn't focused, play audio
              if (Settings.SOUND_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                playNotificationSound(sound);
                didNotify = true;
              }
            }
            if (Settings.TRAY_NOTIFS.get(Settings.currentProfile)) {
              // If always tray notifications or if game isn't focused, display tray notification
              if (Settings.TRAY_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                displayNotification(title, text, "normal");
                didNotify = true;
              }
            }
          }
          break;
        }
      case LOGOUT:
        {
          if (Settings.LOGOUT_NOTIFICATIONS.get(Settings.currentProfile)) {
            if (Settings.NOTIFICATION_SOUNDS.get(Settings.currentProfile)) {
              // If always notification sounds or if game isn't focused, play audio
              if (Settings.SOUND_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                playNotificationSound(sound);
                didNotify = true;
              }
            }
            if (Settings.TRAY_NOTIFS.get(Settings.currentProfile)) {
              // If always tray notifications or if game isn't focused, display tray notification
              if (Settings.TRAY_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                displayNotification(title, text, "critical");
                didNotify = true;
              }
            }
          }
          break;
        }
      case LOWHP:
        {
          if (Settings.LOW_HP_NOTIFICATIONS.get(Settings.currentProfile)) {
            if (Settings.NOTIFICATION_SOUNDS.get(Settings.currentProfile)) {
              // If always notification sounds or if game isn't focused, play audio
              if (Settings.SOUND_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                playNotificationSound(sound);
                didNotify = true;
              }
            }
            if (Settings.TRAY_NOTIFS.get(Settings.currentProfile)) {
              // If always tray notifications or if game isn't focused, display tray notification
              if (Settings.TRAY_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                displayNotification(title, text, "critical");
                didNotify = true;
              }
            }
          }
          break;
        }
      case FATIGUE:
        {
          if (Settings.FATIGUE_NOTIFICATIONS.get(Settings.currentProfile)) {
            if (Settings.NOTIFICATION_SOUNDS.get(Settings.currentProfile)) {
              // If always notification sounds or if game isn't focused, play audio
              if (Settings.SOUND_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                playNotificationSound(sound);
                didNotify = true;
              }
            }
            if (Settings.TRAY_NOTIFS.get(Settings.currentProfile)) {
              // If always tray notifications or if game isn't focused, display tray notification
              if (Settings.TRAY_NOTIFS_ALWAYS.get(Settings.currentProfile)
                  || (!ScaledWindow.getInstance().hasFocus())) {
                displayNotification(title, text, "critical");
                didNotify = true;
              }
            }
          }
          break;
        }
      case HIGHLIGHTEDITEM:
        if (Settings.HIGHLIGHTED_ITEM_NOTIFICATIONS.get(Settings.currentProfile)) {
          if (Settings.NOTIFICATION_SOUNDS.get(Settings.currentProfile)) {
            // If notification sounds, play audio
            playNotificationSound(sound);
            didNotify = true;
          }
          if (Settings.TRAY_NOTIFS.get(Settings.currentProfile)) {
            // This is very important, we will always warn, even if game is focused
            displayNotification(title, text, "critical");
            didNotify = true;
          }
        }
        break;
      case IMPORTANT_MESSAGE:
        if (!Settings.MUTE_IMPORTANT_MESSAGE_SOUNDS.get(Settings.currentProfile)) {
          playNotificationSound(sound);
        }
        displayNotification(title, text, "critical");
        didNotify = true;
        break;
    }
    return didNotify;
  }

  /**
   * Displays a notification, playing sound if it is enabled
   *
   * <p>TODO: Add fade-in and fade-out or slide-in and slide-out animations
   *
   * @param title The title of the notification
   * @param text Text message of the notification
   */
  private static void displayNotification(final String title, String text, String urgency) {
    // Remove color/formatting codes
    final String sanitizedText =
        text.replaceAll("@...@", "").replaceAll("~...~", "").replaceAll("\\\\", "\\\\\\\\");

    if (Settings.USE_SYSTEM_NOTIFICATIONS.get(Settings.currentProfile) && !Util.isWindowsOS()) {
      if (!hasNotifySend) {
        Client.displayMessage(
            "@red@You have to install notify-send for native system notifications!",
            Client.CHAT_QUEST);
        Client.displayMessage(
            "@red@(restart rsc+ if you have installed notify-send)", Client.CHAT_QUEST);
      } else {
        try {
          String output =
              Util.execCmd(
                  new String[] {
                    "notify-send",
                    "-u",
                    urgency,
                    "-i",
                    Launcher.iconAbsolutePath,
                    title,
                    sanitizedText
                  });
        } catch (IOException e) {
          Logger.Error("Error while running notify-send binary: " + e.getMessage());
          e.printStackTrace();
        }
      }
    } else if (SwingUtilities.isEventDispatchThread()) {
      if (Settings.USE_SYSTEM_NOTIFICATIONS.get(Settings.currentProfile)
          && SystemTray.isSupported()) {
        // TODO: When you click the system notification, it should focus the game client
        TrayHandler.getTrayIcon().displayMessage(title, sanitizedText, MessageType.NONE);
      } else {
        setNotificationWindowVisible(true);
        notificationTitle.setText(title);
        notificationTextArea.setText(sanitizedText);
        notificationFrame.repaint();
      }
    } else {
      SwingUtilities.invokeLater(
          new Runnable() {

            @Override
            public void run() {
              if (Settings.USE_SYSTEM_NOTIFICATIONS.get(Settings.currentProfile)
                  && SystemTray.isSupported()) {
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
   * Sets visibility of the notification window. If this method is called from a thread other than
   * the event dispatch thread, it will invokeLater() to hide the thread the next time the EDT is
   * not busy.
   *
   * @param isVisible Whether the window should be visible
   */
  public static void setNotificationWindowVisible(final boolean isVisible) {

    if (SwingUtilities.isEventDispatchThread()) {
      notificationFrame.setVisible(isVisible);
    } else {
      SwingUtilities.invokeLater(
          new Runnable() {

            @Override
            public void run() {
              NotificationsHandler.notificationFrame.setVisible(isVisible);
            }
          });
    }
  }

  private static AudioInputStream notificationAudioIn;
  private static AudioInputStream sadNotificationAudioIn;
  private static Clip notificationSoundClip;
  private static Clip sadNotificationSoundClip;

  public static void loadNotificationSound() {
    try {
      notificationAudioIn =
          AudioSystem.getAudioInputStream(
              new BufferedInputStream(Launcher.getResourceAsStream("/assets/notification.wav")));
      notificationSoundClip =
          (Clip)
              AudioSystem.getLine(new DataLine.Info(Clip.class, notificationAudioIn.getFormat()));
      notificationSoundClip.open(notificationAudioIn);
      sadNotificationAudioIn =
          AudioSystem.getAudioInputStream(
              new BufferedInputStream(
                  Launcher.getResourceAsStream("/assets/notification_sad.wav")));
      sadNotificationSoundClip =
          (Clip)
              AudioSystem.getLine(
                  new DataLine.Info(Clip.class, sadNotificationAudioIn.getFormat()));
      sadNotificationSoundClip.open(sadNotificationAudioIn);
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (LineUnavailableException e) {
      e.printStackTrace();
    }
  }

  public static void playNotificationSound(String sound) {
    Clip usedSound;
    switch (sound) {
      case "sad":
        usedSound = sadNotificationSoundClip;
        break;
      default:
        usedSound = notificationSoundClip;
    }
    if (usedSound == null) return;
    usedSound.stop();
    usedSound.flush();
    usedSound.setFramePosition(0);
    usedSound.start();
  }

  public static void closeNotificationSoundClip() {
    if (notificationSoundClip != null) notificationSoundClip.close();
    if (sadNotificationSoundClip != null) sadNotificationSoundClip.close();
  }

  public static void disposeNotificationHandler() {
    notificationFrame.dispose();
    setLastNotifTime(-1);
  }
}

class NotifsShowGameMouseListener implements MouseListener {

  @Override
  public void mouseClicked(MouseEvent arg0) {
    ScaledWindow.getInstance().toFront();
    NotificationsHandler.setNotificationWindowVisible(false);
  }

  @Override
  public void mouseEntered(MouseEvent arg0) {}

  @Override
  public void mouseExited(MouseEvent arg0) {}

  @Override
  public void mousePressed(MouseEvent arg0) {}

  @Override
  public void mouseReleased(MouseEvent arg0) {}
}
