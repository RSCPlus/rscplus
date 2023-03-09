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

import Chat.ChatWindow;
import Game.Client;
import Game.Game;
import Game.GameApplet;
import Game.SoundEffects;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/** Singleton main class which renders a loading window and the game client window. */
public class Launcher extends JFrame implements Runnable {

  // Singleton
  private static Launcher instance;
  private static ScaledWindow scaledWindow;
  private static ConfigWindow configWindow;
  private static WorldMapWindow worldMapWindow;
  private static QueueWindow queueWindow;

  private static ChatWindow chatWindow;

  public static ImageIcon icon = null;
  public static ImageIcon icon_warn = null;

  // bank filter/sort icons
  public static ImageIcon icon_satchel = null;
  public static ImageIcon icon_satchel_time = null;
  public static ImageIcon icon_no_satchel = null;
  public static ImageIcon icon_runes_weapons_armour = null;
  public static ImageIcon icon_lobster_potion = null;
  public static ImageIcon icon_herblaw = null;
  public static ImageIcon icon_resources_tools = null;
  public static ImageIcon icon_tools = null;
  public static ImageIcon icon_resources = null;
  public static ImageIcon icon_banksearch = null;
  public static ImageIcon icon_filter_reset = null;
  public static ImageIcon icon_release = null;
  public static ImageIcon icon_release_desc = null;
  public static ImageIcon icon_item_value = null;
  public static ImageIcon icon_item_value_rev = null;
  public static ImageIcon icon_alphabetical = null;
  public static ImageIcon icon_alphabetical_rev = null;
  public static ImageIcon icon_efficient = null;
  public static ImageIcon icon_user_custom = null;
  // TODO: if we can draw native text on top of other elements drawn by RSC+,
  // then we won't need to cheat by loading images of text like this
  public static ImageIcon icon_filter_text = null;
  public static ImageIcon icon_sort_text = null;

  public static int numCores;

  private JProgressBar m_progressBar;
  private JClassLoader m_classLoader;

  private Launcher() {
    // Empty private constructor to prevent extra instances from being created.
  }

  /** Renders the launcher progress bar window, then calls {@link #run()}. */
  public void init() {
    Logger.start();
    Logger.Info("Starting rscplus");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    getContentPane().setBackground(Color.BLACK);

    // Set window icon
    URL iconURL = getResource("/assets/icon.png");
    if (iconURL != null) {
      icon = new ImageIcon(iconURL);
      setIconImage(icon.getImage());
    }
    iconURL = getResource("/assets/icon_warn.png");
    if (iconURL != null) {
      icon_warn = new ImageIcon(iconURL);
    }

    iconURL = getResource("/assets/bank/filter.png");
    if (iconURL != null) {
      icon_filter_text = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/satchel.png");
    if (iconURL != null) {
      icon_satchel = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/satchel.time.png");
    if (iconURL != null) {
      icon_satchel_time = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/no.satchel.png");
    if (iconURL != null) {
      icon_no_satchel = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/runes.weapons.armour.png");
    if (iconURL != null) {
      icon_runes_weapons_armour = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/lobster.potion.png");
    if (iconURL != null) {
      icon_lobster_potion = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/herblaw.png");
    if (iconURL != null) {
      icon_herblaw = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/resources.tools.png");
    if (iconURL != null) {
      icon_resources_tools = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/tools.png");
    if (iconURL != null) {
      icon_tools = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/resources.png");
    if (iconURL != null) {
      icon_resources = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/magnifying.lens.png");
    if (iconURL != null) {
      icon_banksearch = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/reset.png");
    if (iconURL != null) {
      icon_filter_reset = new ImageIcon(iconURL);
    }

    iconURL = getResource("/assets/bank/sort.png");
    if (iconURL != null) {
      icon_sort_text = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/release.png");
    if (iconURL != null) {
      icon_release = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/release.desc.png");
    if (iconURL != null) {
      icon_release_desc = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/item.value.png");
    if (iconURL != null) {
      icon_item_value = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/item.value.rev.png");
    if (iconURL != null) {
      icon_item_value_rev = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/alphabetical.png");
    if (iconURL != null) {
      icon_alphabetical = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/alphabetical.rev.png");
    if (iconURL != null) {
      icon_alphabetical_rev = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/efficient.png");
    if (iconURL != null) {
      icon_efficient = new ImageIcon(iconURL);
    }
    iconURL = getResource("/assets/bank/user.config.png");
    if (iconURL != null) {
      icon_user_custom = new ImageIcon(iconURL);
    }

    // Extract libraries that only work outside the jar
    extractJInputNatives();

    // Load jf fonts
    GameApplet.loadJagexFonts();

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

  /** Generates a config file if needed and launches the main client window. */
  @Override
  public void run() {
    if (Settings.UPDATE_CONFIRMATION.get(Settings.currentProfile)) {
      Client.firstTimeRunningRSCPlus = true;
      int response =
          JOptionPane.showConfirmDialog(
              this,
              "rscplus has an automatic update feature.\n"
                  + "\n"
                  + "When enabled, rscplus will prompt for and install updates when launching the client.\n"
                  + "The updates are obtained from our 'Latest' release on GitHub.\n"
                  + "\n"
                  + "Would you like to enable this feature?\n"
                  + "\n"
                  + "NOTE: This option can be toggled in the Settings interface under the General tab.",
              "rscplus",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.INFORMATION_MESSAGE,
              icon);
      if (response == JOptionPane.YES_OPTION || response == JOptionPane.CLOSED_OPTION) {
        Settings.CHECK_UPDATES.put(Settings.currentProfile, true);
        JOptionPane.showMessageDialog(
            this,
            "rscplus is set to check for updates on GitHub at every launch!",
            "rscplus",
            JOptionPane.INFORMATION_MESSAGE,
            icon);
      } else if (response == JOptionPane.NO_OPTION) {
        Settings.CHECK_UPDATES.put(Settings.currentProfile, false);
        JOptionPane.showMessageDialog(
            this,
            "rscplus will not check for updates automatically.\n"
                + "\n"
                + "You will not get notified when new releases are available. To update your client, you\n"
                + "will need to do it manually by replacing 'rscplus.jar' in your rscplus directory.\n"
                + "\n"
                + "You can enable GitHub updates again in the Settings interface under the General tab.",
            "rscplus",
            JOptionPane.INFORMATION_MESSAGE,
            icon_warn);
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
        int response =
            JOptionPane.showConfirmDialog(
                this,
                "An rscplus client update is available!\n"
                    + "\n"
                    + "Latest: "
                    + String.format("%8.6f", latestVersion)
                    + "\n"
                    + "Installed: "
                    + String.format("%8.6f", Settings.VERSION_NUMBER)
                    + "\n"
                    + "\n"
                    + "Would you like to update now?",
                "rscplus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                icon);
        if (response == JOptionPane.YES_OPTION) {
          if (updateJar()) {
            JOptionPane.showMessageDialog(
                this,
                "rscplus has been updated successfully!\n"
                    + "\n"
                    + "The client requires a restart, and will now exit.",
                "rscplus",
                JOptionPane.INFORMATION_MESSAGE,
                icon);
            System.exit(0);
          } else {
            response =
                JOptionPane.showConfirmDialog(
                    this,
                    "rscplus has failed to update, please try again later.\n"
                        + "\n"
                        + "Would you like to continue without updating?",
                    "rscplus",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    icon_warn);
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
    if (!m_classLoader.fetch("/assets/rsclassic-1091943135.jar")) {
      error("Unable to fetch Jar");
    }

    setStatus("Launching game...");
    Game game = Game.getInstance();
    try {
      Class<?> client = m_classLoader.loadClass(config.getJarClass());
      game.setApplet((Applet) client.newInstance());
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
    SwingUtilities.invokeLater(
        new Runnable() {
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
      URL url = new URL("https://github.com/RSCPlus/rscplus/releases/download/Latest/rscplus.jar");

      // Open connection
      URLConnection connection = url.openConnection();
      connection.setConnectTimeout(3000);
      connection.setReadTimeout(3000);

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
    SwingUtilities.invokeLater(
        new Runnable() {
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
    // Do this before anything else runs to override OS-level
    // dpi settings, since we have in-client scaling now
    System.setProperty("sun.java2d.uiScale.enabled", "false");
    System.setProperty("sun.java2d.uiScale", "1");

    numCores = Runtime.getRuntime().availableProcessors();

    Logger.start();
    Settings.initDir();
    Properties props = Settings.initSettings();

    if (Settings.javaVersion >= 9) {
      Logger.Warn(
          "rsc wasn't designed for Java version "
              + Settings.javaVersion
              + ". You may encounter additional bugs, for best results use version 8.");
    } else if (Settings.javaVersion == -1) {
      Logger.Warn(
          "rsc wasn't designed for your Java version. "
              + "You may encounter additional bugs, for best results use version 8.");
    }

    setScaledWindow(ScaledWindow.getInstance());
    setConfigWindow(new ConfigWindow());
    setChatWindow(new ChatWindow());

    Settings.loadKeybinds(props);
    Settings.successfullyInitted = true;
    setWorldMapWindow(new WorldMapWindow());
    setQueueWindow(new QueueWindow());
    TrayHandler.initTrayIcon();
    NotificationsHandler.initialize();
    SoundEffects.loadCustomSoundEffects();
    Launcher.getInstance().init();

    Launcher.chatWindow.showChatWindow();
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
   * Creates a URL object that points to a specified file relative to the codebase, which is
   * typically either the jar or location of the package folders.
   *
   * @param fileName the file to parse as a URL
   * @return a URL that points to the specified file
   */
  public static URL getResource(String fileName) {
    URL url = null;
    try {
      url = Game.getInstance().getClass().getResource(fileName);
    } catch (Exception e) {
    }

    // Try finding assets
    if (url == null) {
      try {
        url = new URL("file://" + Util.findDirectoryReverse("/assets") + fileName);
      } catch (Exception e) {
      }
    }

    Logger.Info("Loading resource: " + fileName);

    if (fileName.equals("/assets/content/content10_ffffffffa95e7195") && Client.firstTime) {
      // members
      finishedLoading();
    } else if (fileName.equals("/assets/content/content6_ffffffffe997514b")
        && !Client.members
        && Client.firstTime) {
      // free
      finishedLoading();
    }

    return url;
  }

  /**
   * Creates an InputStream object that streams the contents of a specified file relative to the
   * codebase, which is typically either the jar or location of the package folders.
   *
   * @param fileName the file to open as an InputStream
   * @return an InputStream that streams the contents of the specified file
   */
  public static InputStream getResourceAsStream(String fileName) {
    InputStream stream = null;
    try {
      stream = Game.getInstance().getClass().getResourceAsStream(fileName);
    } catch (Exception e) {
    }

    // Try finding assets
    if (stream == null) {
      try {
        stream = new FileInputStream(Util.findDirectoryReverse("/assets") + fileName);
      } catch (Exception e) {
      }
    }

    Logger.Info("Loading resource as stream: " + fileName);

    return stream;
  }

  public static void finishedLoading() {
    // Remember world setting
    Client.lastIsMembers = Client.members;
    Game.getInstance().getJConfig().changeWorld(Settings.WORLD.get(Settings.currentProfile));
    GameApplet.syncWikiHbarImageWithFontSetting();
    if (Client.firstTime) {
      Client.firstTime = false;
    }
  }

  public static void extractJInputNatives() {
    extractResource(
        "/lib/jinput-natives/jinput-dx8_64.dll",
        new File(Settings.Dir.JINPUTNATIVELIB + "/jinput-dx8_64.dll"));
    extractResource(
        "/lib/jinput-natives/jinput-raw_64.dll",
        new File(Settings.Dir.JINPUTNATIVELIB + "/jinput-raw_64.dll"));
    extractResource(
        "/lib/jinput-natives/jinput-wintab.dll",
        new File(Settings.Dir.JINPUTNATIVELIB + "/jinput-wintab.dll"));
    extractResource(
        "/lib/jinput-natives/libjinput-linux64.so",
        new File(Settings.Dir.JINPUTNATIVELIB + "/libjinput-linux64.so"));
    extractResource(
        "/lib/jinput-natives/libjinput-osx.jnilib",
        new File(Settings.Dir.JINPUTNATIVELIB + "/libjinput-osx.jnilib"));
  }

  public static void extractResource(String pathInJar, File destinationPath) {
    try {
      BufferedInputStream source = new BufferedInputStream(Launcher.getResourceAsStream(pathInJar));
      BufferedOutputStream target = new BufferedOutputStream(new FileOutputStream(destinationPath));
      byte[] buf = new byte[8192];
      int length;
      while ((length = source.read(buf)) > 0) {
        target.write(buf, 0, length);
      }
      source.close();
      target.close();
      Logger.Info("Successfully extracted " + pathInJar);
    } catch (Exception e) {
      Logger.Error("Could not extract " + pathInJar);
      e.printStackTrace();
    }
  }

  /** @return the window */
  public static ConfigWindow getConfigWindow() {
    return configWindow;
  }

  /** @param configWindow the window to set */
  public static void setConfigWindow(ConfigWindow configWindow) {
    Launcher.configWindow = configWindow;
  }

  public static ChatWindow getChatWindow() {
    return chatWindow;
  }

  public static void setChatWindow(ChatWindow chatWindow) {
    Launcher.chatWindow = chatWindow;
  }

  /** @return the window */
  public static WorldMapWindow getWorldMapWindow() {
    return worldMapWindow;
  }

  /** @param worldMapWindow the window to set */
  public static void setWorldMapWindow(WorldMapWindow worldMapWindow) {
    Launcher.worldMapWindow = worldMapWindow;
  }

  /** @return the window */
  public static ScaledWindow getScaledWindow() {
    return scaledWindow;
  }

  /** @param scaledWindow the window to set */
  public static void setScaledWindow(ScaledWindow scaledWindow) {
    Launcher.scaledWindow = scaledWindow;
  }

  /** @return the window */
  public static QueueWindow getQueueWindow() {
    return queueWindow;
  }

  /** @param queueWindow the window to set */
  public static void setQueueWindow(QueueWindow queueWindow) {
    Launcher.queueWindow = queueWindow;
  }
}
