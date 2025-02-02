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

import static Client.ServerExtensions.BINARY_TYPE;
import static Client.ServerExtensions.BinaryInfo;
import static Client.ServerExtensions.Extension;
import static Client.Util.isMacOS;
import static Client.Util.isWindowsOS;
import static Client.Util.osScaleMul;

import Client.Extensions.OpenRSCOfficialUtils;
import Client.Extensions.WorldType;
import Game.Client;
import Game.Game;
import Game.GameApplet;
import Game.SoundEffects;
import com.apple.eawt.Application;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.WinDef;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.json.JSONArray;

/** Singleton main class which renders a loading window and the game client window. */
public class Launcher extends JFrame implements Runnable {

  // Launcher properties
  public static final String JAR_NAME = "rscplus.jar";
  public static Extension binaryFlavor;
  public static Double binaryVersion;
  public static String binaryPrefix = "";
  public static String appName = "RSCPlus";
  public static Extension worldSubscriptionId;
  public static File initWorldLock;
  public static Map<Extension, Map<WorldType, Boolean>> knownWorldTypes = new LinkedHashMap<>();
  public static Map<URI, List<World>> downloadedWorlds =
      Collections.synchronizedMap(new LinkedHashMap<>());
  public static List<File> subWorldFiles = new ArrayList<>();
  public static Set<World> blockedWorlds = new HashSet<>();
  private static final Set<LauncherError> launcherWarnings = new HashSet<>();
  private static final List<Integer> updatedWorldSubHashes = new ArrayList<>();
  private static Integer lastConnHash = null;
  private static boolean warnUserWorldChanged = false;

  // Singletons
  private static Launcher instance;
  private static ScaledWindow scaledWindow;
  private static ConfigWindow configWindow;
  private static WorldMapWindow worldMapWindow;
  private static QueueWindow queueWindow;

  public static Font controlsFont;

  // App icons
  public static String iconPath = null;
  public static String largeIconPath = null;
  public static String smallIconPath = null;
  public static String iconAbsolutePath = null;
  public static ImageIcon scaled_option_icon = null;
  public static ImageIcon icon_warn = null;
  public static ImageIcon scaled_icon_warn = null;
  private static List<Image> windowIcons = null;

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
  // TODO: Replace usage of these with Renderer.drawShadowText
  public static ImageIcon icon_filter_text = null;
  public static ImageIcon icon_sort_text = null;

  public static int numCores;

  private JProgressBar m_progressBar;
  private JClassLoader m_classLoader;

  public static double OSScalingFactor = 1.0;
  public static boolean forceDisableNimbus = false;

  private Launcher() {
    // Empty private constructor to prevent extra instances from being created.
  }

  /** Renders the launcher progress bar window, then calls {@link #run()}. */
  public void init() {
    Logger.Info("Starting " + appName);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    getContentPane().setBackground(Color.BLACK);

    // Set window icons
    setIconImages(getWindowIcons());

    // Store absolute path to the regular icon for notifications
    URL iconURL = getResource(iconPath);
    if (iconURL != null) {
      try {
        iconAbsolutePath = new File(iconURL.getPath()).getCanonicalPath();
      } catch (IOException e) {
        Logger.Warn("Failed to load icon for usage with notify-send");
      }
    }

    // Set scaled icon used in JOptionPanes
    URL optionIconURL = getResource(largeIconPath);
    if (optionIconURL != null) {
      try {
        BufferedImage iconOptionsBI = ImageIO.read(optionIconURL);
        scaled_option_icon =
            new ImageIcon(
                iconOptionsBI.getScaledInstance(
                    osScaleMul(iconOptionsBI.getWidth()),
                    osScaleMul(iconOptionsBI.getHeight()),
                    Image.SCALE_DEFAULT));
      } catch (IOException e) {
        // No-op
      }
    }

    // Set warning icon
    iconURL = getResource("/assets/icon_warn.png");
    if (iconURL != null) {
      icon_warn = new ImageIcon(iconURL);
      // Set scaled warning icon for JOptionPanes
      try {
        BufferedImage warnIconBI = ImageIO.read(iconURL);
        scaled_icon_warn =
            new ImageIcon(
                warnIconBI.getScaledInstance(
                    osScaleMul(warnIconBI.getWidth()),
                    osScaleMul(warnIconBI.getHeight()),
                    Image.SCALE_DEFAULT));
      } catch (IOException e) {
        // No-op
      }
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
    if (isUsingAppImage()) {
      // Make wider to accommodate possible larger download size
      getContentPane().setPreferredSize(osScaleMul(new Dimension(350, 32)));
    } else {
      getContentPane().setPreferredSize(osScaleMul(new Dimension(315, 32)));
    }
    setTitle(appName + " Launcher");
    setResizable(false);
    pack();
    setLocationRelativeTo(null);

    // Add progress bar
    m_progressBar = new JProgressBar();
    m_progressBar.setStringPainted(true);
    m_progressBar.setBorderPainted(true);
    m_progressBar.setForeground(Color.GRAY);
    m_progressBar.setBackground(Color.BLACK);
    if (Util.isUsingFlatLAFTheme()) {
      m_progressBar.setFont(new Font(Font.SERIF, Font.PLAIN, osScaleMul(14)));
    }
    m_progressBar.setString("Initializing");
    getContentPane().add(m_progressBar);

    setVisible(true);

    new Thread(this).start();
  }

  /** @return {@link List} of {@link Image} objects used for swing window frames icons */
  public static synchronized List<Image> getWindowIcons() {
    if (windowIcons != null) {
      return windowIcons;
    }

    List<Image> loadedIcons = new ArrayList<>();

    URL iconURL = getResource(iconPath);
    URL largeIconURL = getResource(largeIconPath);
    URL smallIconURL = getResource(smallIconPath);

    if (iconURL != null && smallIconURL != null) {
      loadedIcons.add(new ImageIcon(iconURL).getImage());
      loadedIcons.add(new ImageIcon(largeIconURL).getImage());
      loadedIcons.add(new ImageIcon(smallIconURL).getImage());
    }

    windowIcons = loadedIcons;

    return loadedIcons;
  }

  /** Generates a config file if needed and launches the main client window. */
  @Override
  public void run() {
    if (Settings.UPDATE_CONFIRMATION.get(Settings.currentProfile)) {
      Client.firstTimeRunningRSCPlus = true;

      String automaticUpdateMessage =
          appName
              + " has an automatic update feature.<br/>"
              + "<br/>"
              + "When enabled, "
              + appName
              + " will prompt for and install updates when launching the client.<br/>"
              + "The updates are obtained from our 'Latest' release on GitHub.<br/>"
              + "<br/>"
              + "Would you like to enable this feature?<br/>"
              + "<br/>"
              + "<b>NOTE:</b> This option can be toggled in the Settings interface under the General tab.";

      JPanel automaticUpdatePanel = Util.createOptionMessagePanel(automaticUpdateMessage);

      int response =
          JOptionPane.showConfirmDialog(
              this,
              automaticUpdatePanel,
              appName,
              JOptionPane.YES_NO_OPTION,
              JOptionPane.INFORMATION_MESSAGE,
              scaled_option_icon);
      if (response == JOptionPane.YES_OPTION || response == JOptionPane.CLOSED_OPTION) {
        Settings.CHECK_UPDATES.put(Settings.currentProfile, true);

        JPanel updateInfoPanel =
            Util.createOptionMessagePanel(
                appName + " is set to check for updates on GitHub at every launch!");
        JOptionPane.showMessageDialog(
            this, updateInfoPanel, appName, JOptionPane.INFORMATION_MESSAGE, scaled_option_icon);
      } else if (response == JOptionPane.NO_OPTION) {
        Settings.CHECK_UPDATES.put(Settings.currentProfile, false);

        String automaticUpdateDeniedMessage =
            appName
                + " will not check for updates automatically.<br/>"
                + "<br/>"
                + "You will not get notified when new releases are available. To update your client, you<br/>"
                + "will need to do it manually by replacing '"
                + JAR_NAME
                + "' in your "
                + appName
                + " directory"
                + (isUsingBinary()
                    ? ",<br/>or re-downloading the application installer.<br/>"
                    : ".<br/>")
                + "<br/>"
                + "You can enable GitHub updates again in the Settings interface under the General tab.";
        JPanel automaticUpdateDeniedPanel =
            Util.createOptionMessagePanel(automaticUpdateDeniedMessage);

        JOptionPane.showMessageDialog(
            this,
            automaticUpdateDeniedPanel,
            appName,
            JOptionPane.INFORMATION_MESSAGE,
            scaled_icon_warn);
      }
      Settings.UPDATE_CONFIRMATION.put(Settings.currentProfile, false);
      Settings.save();
    }

    if (Settings.CHECK_UPDATES.get(Settings.currentProfile)) {
      setStatus("Checking for " + appName + " update...");
      checkForUpdate(true, false);
    }

    setStatus("Creating JConfig...");
    JConfig config = Game.getInstance().getJConfig();
    config.create(Settings.WORLD.get(Settings.currentProfile));

    m_classLoader = new JClassLoader(Thread.currentThread().getContextClassLoader());
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

    // Ensure current world selection matches previous launch, warn if not
    validateWorldSelection();

    game.start();
  }

  /**
   * Compares the local value of {@link Settings#VERSION_NUMBER} to the value on the GitHub master
   * branch, as well as {@link Launcher#binaryVersion} to the published version for a given binary.
   *
   * <p>Used to check if there is a newer version of the client or installer available.
   *
   * @param promptForUpdate if the user should be prompted to accept an application or client update
   * @param announceIfUpToDate if a message should be displayed in chat if the client is up-to-date
   */
  public void checkForUpdate(boolean promptForUpdate, boolean announceIfUpToDate) {
    boolean binaryNeedsUpdate = false;

    // Check for application update first when client is running within a binary
    if (isUsingBinary()) {
      final BinaryInfo binaryInfo = ServerExtensions.getBinaryInfo(binaryFlavor);
      final Double latestBinaryVersion = fetchLatestVersionNumber(binaryInfo);

      if (latestBinaryVersion != null && binaryVersion < latestBinaryVersion) {
        binaryNeedsUpdate = true;

        if (promptForUpdate) {
          promptForUpdate(binaryInfo, binaryVersion, latestBinaryVersion);
        } else {
          announceUpdateAvailable("application", binaryVersion, latestBinaryVersion);
        }
      } else if (binaryVersion.equals(latestBinaryVersion) && announceIfUpToDate) {
        announceSameVersion("application", latestBinaryVersion);
      }
    }

    // If no binary update is needed, check for JAR update, unless ran from within an AppImage
    // (can't upgrade its JAR)
    if (!binaryNeedsUpdate && !isUsingAppImage()) {
      final Double latestJARVersion = fetchLatestVersionNumber(null);
      if (latestJARVersion != null && Settings.VERSION_NUMBER < latestJARVersion) {
        if (promptForUpdate) {
          promptForUpdate(null, Settings.VERSION_NUMBER, latestJARVersion);
        } else {
          announceUpdateAvailable("client", Settings.VERSION_NUMBER, latestJARVersion);
        }
      } else if (new Double(Settings.VERSION_NUMBER).equals(latestJARVersion)
          && announceIfUpToDate) {
        announceSameVersion("client", latestJARVersion);
      }
    }
  }

  /**
   * Fetches the value of {@link Settings#VERSION_NUMBER} or the binary version number.
   *
   * <p>Used to check the newest version of the client JAR or binary installer.
   *
   * @param binaryInfo {@link BinaryInfo} when checking for binary updates or {@code null} for
   *     client updates
   * @return the current version number
   */
  private static Double fetchLatestVersionNumber(BinaryInfo binaryInfo) {
    try {
      double currentVersion = 0.0;
      final URL updateURL;
      if (binaryInfo == null) {
        // In our current client version, we are looking at the source file of Settings.java in the
        // main repository in order to parse what the current version numbers are.
        updateURL =
            new URL(
                "https://raw.githubusercontent.com/RSCPlus/rscplus/master/src/Client/Settings.java");
      } else {
        // For checking binary versions
        updateURL = new URL(binaryInfo.getDownloadURI() + binaryInfo.getOSVersionFileName());
      }

      // Open connection
      URLConnection connection = updateURL.openConnection();
      connection.setConnectTimeout(3000);
      connection.setReadTimeout(3000);
      try (BufferedReader in =
          new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        String line;
        if (binaryInfo == null) {
          while ((line = in.readLine()) != null) {
            if (line.contains("VERSION_NUMBER")) {
              currentVersion =
                  Double.parseDouble(line.substring(line.indexOf('=') + 1, line.indexOf(';')));
              Logger.Info(String.format("@|green Current Client Version: %f|@", currentVersion));
              break;
            }
          }
        } else {
          if ((line = in.readLine()) != null) {
            currentVersion = Double.parseDouble(line);
            Logger.Info(String.format("@|green Current Application Version: %f|@", currentVersion));
            clearWarning(LauncherError.BINARY_UPDATE_CHECK);
          }
        }

        return currentVersion;
      }
    } catch (Exception e) {
      Logger.Warn(
          "Error checking latest version for the " + (binaryInfo == null ? "jar" : "binary"));
      e.printStackTrace();
      Client.displayMessage(
          "@dre@Error checking latest "
              + (binaryInfo == null ? "client" : "application")
              + " version",
          0);

      // For error rendering
      if (binaryInfo != null && !hasWarning(LauncherError.BINARY_UPDATE_CHECK)) {
        setWarning(LauncherError.BINARY_UPDATE_CHECK);
      }

      return null;
    }
  }

  /**
   * Display messages within the client to update whether an announcement is available or not
   *
   * @param updateType {@link String} value of {@code application} or {@code client}
   * @param currentVersion {@link Double} value indicating the current application or client version
   * @param latestVersion {@link Double} value indicating the latest application or client version
   */
  private static void announceUpdateAvailable(
      String updateType, Double currentVersion, Double latestVersion) {
    Client.displayMessage(
        "@gre@A new version of the " + binaryPrefix + "RSC+ " + updateType + " is available!",
        Client.CHAT_QUEST);
    Client.displayMessage(
        "The latest version is @gre@" + Util.formatVersion(latestVersion), Client.CHAT_QUEST);
    Client.displayMessage(
        "~034~ Your version is @red@" + Util.formatVersion(currentVersion), Client.CHAT_QUEST);
    if (Settings.CHECK_UPDATES.get(Settings.currentProfile)) {
      Client.displayMessage(
          "~034~ You will receive the update next time you restart " + appName, Client.CHAT_QUEST);
    }
  }

  /**
   * Display a message within the client to inform the user that their application or client is
   * up-to-date
   *
   * @param updateType {@link String} value of {@code application} or {@code client}
   * @param latestVersion {@link Double} value indicating the latest application or client version
   */
  private static void announceSameVersion(String updateType, Double latestVersion) {
    Client.displayMessage(
        "Your " + updateType + " is up to date: @gre@" + Util.formatVersion(latestVersion),
        Client.CHAT_QUEST);
  }

  /**
   * Prompt the user to update the application or client and perform update tasks if they agreed to
   * it and the download process was successful
   *
   * @param binaryInfo {@link BinaryInfo} when checking for binary updates or {@code null} for
   *     client updates
   * @param currentVersion {@link Double} value indicating the current application or client version
   * @param latestVersion {@link Double} value indicating the latest application or client version
   */
  private void promptForUpdate(
      BinaryInfo binaryInfo, final double currentVersion, final double latestVersion) {
    setStatus(appName + " update is available");
    final boolean shouldUpdateBinary = binaryInfo != null;

    final String updateType = shouldUpdateBinary ? "application" : "client";
    String clientUpdateMessage =
        "An "
            + appName
            + " "
            + updateType
            + " update is available!<br/>"
            + "<br/>"
            + "Latest: "
            + Util.formatVersion(latestVersion)
            + "<br/>"
            + "Installed: "
            + Util.formatVersion(currentVersion)
            + "<br/>"
            + "<br/>"
            + "Would you like to update now?";
    JPanel clientUpdatePanel = Util.createOptionMessagePanel(clientUpdateMessage);
    int response =
        JOptionPane.showConfirmDialog(
            this,
            clientUpdatePanel,
            appName,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            scaled_option_icon);

    if (response == JOptionPane.YES_OPTION) {
      // Perform update here
      File downloadLocation = downloadRSCPlusUpdate(binaryInfo);
      if (downloadLocation != null) {
        String updateSuccessMessage;
        if (shouldUpdateBinary && (isWindowsOS() || isMacOS())) {
          updateSuccessMessage =
              "The installer has finished downloading."
                  + "<br/><br/>"
                  + appName
                  + " will now begin the "
                  + (isWindowsOS() ? "auto-" : "")
                  + "update process.<br/><br/>"
                  + "Once the update is complete, please relaunch the game.";
        } else {
          updateSuccessMessage =
              appName
                  + " has been updated successfully!"
                  + "<br/><br/>"
                  + "The client requires a restart, and will now exit.";
        }

        JPanel updateSuccessPanel = Util.createOptionMessagePanel(updateSuccessMessage);

        JOptionPane.showMessageDialog(
            this, updateSuccessPanel, appName, JOptionPane.INFORMATION_MESSAGE, scaled_option_icon);

        // Add a shutdown hook to swap AppImages
        if (shouldUpdateBinary && isUsingAppImage()) {
          Runtime.getRuntime()
              .addShutdownHook(
                  new Thread(
                      () -> {
                        final String appImagePath =
                            System.getenv("OWD")
                                + File.separator
                                + binaryInfo.getBinaryDownloads().get(BINARY_TYPE.LINUX_APP_IMAGE);
                        File currentAppImage = new File(appImagePath);
                        File newAppImage =
                            new File(appImagePath + BinaryInfo.LINUX_APP_IMAGE_SUFFIX);

                        try {
                          // Delete the current AppImage
                          boolean deleteSuccess = currentAppImage.delete();

                          if (deleteSuccess) {
                            // Rename the download AppImage -> current
                            newAppImage.renameTo(currentAppImage);

                            // Reapply permissions
                            Util.execCmd(new String[] {"chmod", "+x", appImagePath});
                          }
                        } catch (Exception e) {
                          Logger.Error(
                              "Error occurred while attempting to swap to the newly-downloaded AppImage");
                          e.printStackTrace();
                        }
                      }));
        } else if (shouldUpdateBinary) {
          if (Util.isMacOS()) {
            Runtime.getRuntime()
                .addShutdownHook(
                    new Thread(
                        () -> {
                          try {
                            // Attempt to mount the downloaded DMG
                            Util.execCmd(
                                new String[] {"open", downloadLocation.getCanonicalPath()});
                          } catch (IOException ex) {
                            try {
                              // Fallback to opening the download location
                              Util.execCmd(new String[] {"open", downloadLocation.getParent()});
                            } catch (IOException ex2) {
                              // Application is closing anyway
                            }
                          }
                        }));
          } else if (Util.isWindowsOS()) {
            Runtime runTime = Runtime.getRuntime();
            try {
              // Leverages Innosetup feature to automatically update the application
              String command =
                  downloadLocation.getCanonicalPath()
                      + " /SP- /silent /noicons \"/dir=expand:"
                      + Settings.sanitizeDirTextValue(Settings.Dir.CONFIG_DIR)
                      + "\"";
              runTime.exec(command);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        }

        System.exit(0);
      } else {
        final String updateFailureMessage =
            appName
                + " has failed to update"
                + (shouldUpdateBinary ? ".<br/>" : ", please try again later.<br/>")
                + "<br/>"
                + (shouldUpdateBinary
                    ? "If this message persists, try re-downloading and re-installing the application.<br/><br/>"
                    : "")
                + "Would you like to continue without updating?";

        JPanel updateFailurePanel = Util.createOptionMessagePanel(updateFailureMessage);
        response =
            JOptionPane.showConfirmDialog(
                this,
                updateFailurePanel,
                appName,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                scaled_icon_warn);
        if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
          System.exit(0);
        }
      }
    }
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

  /**
   * Download the application or client update, prompting the user for a download location when
   * necessary
   *
   * @param binaryInfo {@link BinaryInfo} when download a binary update or {@code null} for client
   *     updates
   * @return a {@link File} for the chosen download location or {@code null} if cancelled
   */
  private File downloadRSCPlusUpdate(BinaryInfo binaryInfo) {
    boolean success = true;

    setStatus("Starting " + appName + " update...");
    setProgress(0, 1);

    File downloadLocation = null;
    // Resolve download URL based on user's system and launch type
    try {
      final URL url;
      if (binaryInfo == null) {
        // Only download the new JAR
        url = new URL("https://github.com/RSCPlus/rscplus/releases/download/Latest/" + JAR_NAME);
        downloadLocation = new File(Settings.Dir.JAR + File.separator + JAR_NAME);
      } else {
        // Determine download type for the binary / installer
        // Note: the following logic assumes that OS-specific definitions exist, since it would only
        // reach this code block if being launched from within a binary for the OS type.
        final Map<BINARY_TYPE, String> binaryDownloads = binaryInfo.getBinaryDownloads();

        if (isUsingAppImage()) {
          url =
              new URL(
                  binaryInfo.getDownloadURI() + binaryDownloads.get(BINARY_TYPE.LINUX_APP_IMAGE));
          // Save the new AppImage with a suffix - can't hotswap during runtime
          downloadLocation =
              new File(
                  System.getenv("OWD")
                      + File.separator
                      + binaryDownloads.get(BINARY_TYPE.LINUX_APP_IMAGE)
                      + BinaryInfo.LINUX_APP_IMAGE_SUFFIX);
        } else {
          final String osDownload;

          if (Util.isMacOS()) {
            if (System.getProperty("os.arch").contains("aarch")) {
              osDownload = binaryDownloads.get(BINARY_TYPE.MACOS_ARM);
            } else {
              osDownload = binaryDownloads.get(BINARY_TYPE.MACOS_X64);
            }
          } else if (Util.isWindowsOS()) {
            if (System.getProperty("os.arch").contains("64")) {
              osDownload = binaryDownloads.get(BINARY_TYPE.WINDOWS_X64);
            } else {
              osDownload = binaryDownloads.get(BINARY_TYPE.WINDOWS_X32);
            }
          } else {
            throw new RuntimeException("Could not detect OS for application updates");
          }

          url = new URL(binaryInfo.getDownloadURI() + osDownload);

          JPanel chooseDownloadLocationPanel =
              Util.createOptionMessagePanel("Please select a download location for the installer.");

          JOptionPane.showMessageDialog(
              this,
              chooseDownloadLocationPanel,
              appName,
              JOptionPane.INFORMATION_MESSAGE,
              scaled_option_icon);

          JFileChooser downloadDirChooser = new JFileChooser();
          downloadDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          downloadLocation = validateDownloadLocation(downloadDirChooser, osDownload);

          // User cancelled file selection, skip the download and return
          if (downloadLocation == null) {
            return null;
          }
        }
      }

      // Open connection, download the file
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(3000);
      connection.setReadTimeout(3000);

      int responseCode = connection.getResponseCode();
      if (responseCode != 200) {
        throw new RuntimeException("Connection returned HTTP response: [" + responseCode + "]");
      }

      int size = connection.getContentLength();
      if (size == 0) {
        throw new RuntimeException("Connection returned empty content");
      }

      int offset = 0;
      byte[] data = new byte[size];

      try (InputStream input = connection.getInputStream()) {
        int readSize;
        while ((readSize = input.read(data, offset, size - offset)) != -1) {
          offset += readSize;
          setStatus(
              "Updating " + appName + "(" + (offset / 1024) + "KiB / " + (size / 1024) + "KiB)");
          setProgress(offset, size);
        }
      }

      if (offset != size) {
        success = false;
      } else {
        try (FileOutputStream output = new FileOutputStream(downloadLocation)) {
          output.write(data);
          output.close();

          setStatus(appName + " update complete");
        }
      }
    } catch (Exception e) {
      Logger.Error("Error occurred while downloading the " + appName + " update");
      e.printStackTrace();
      success = false;
    }

    return success ? downloadLocation : null;
  }

  /**
   * Validates the chosen binary installer download location, looping until an acceptable location
   * is chosen or the user cancels the process
   *
   * @param downloadDirChooser {@link JFileChooser} instance used for selecting the download
   *     location
   * @param osDownload Location for the OS-specific download, as defined in {@link
   *     ServerExtensions#getBinaryInfo(Extension)}
   * @return {@link File} pointing to the chosen download location
   */
  private File validateDownloadLocation(JFileChooser downloadDirChooser, String osDownload) {
    int choice = downloadDirChooser.showOpenDialog(this);
    if (choice == JFileChooser.APPROVE_OPTION) {
      File chosenDir = downloadDirChooser.getSelectedFile();
      if (ConfigWindow.validateChosenDirectory(chosenDir)) {
        File downloadLocation = new File(chosenDir.getAbsolutePath() + File.separator + osDownload);
        return downloadLocation;
      } else {
        return validateDownloadLocation(downloadDirChooser, osDownload);
      }
    } else {
      return null;
    }
  }

  /**
   * Sets the progress value of the launcher progress bar.
   *
   * @param value the number of tasks that have been completed
   * @param total the total number of tasks to complete
   */
  public void setProgress(final long value, final long total) {
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            if (total == 0) {
              m_progressBar.setValue(0);
              return;
            }

            m_progressBar.setValue((int) (value * 100 / total));
          }
        });
  }

  public JClassLoader getClassLoader() {
    return m_classLoader;
  }

  /* Uses JNA to acquire accurate scale factor for JRE 8 */
  public static double getScaleFactor() {
    WinDef.HDC hdc = GDI32.INSTANCE.CreateCompatibleDC(null);
    if (hdc != null) {
      float actual = GDI32.INSTANCE.GetDeviceCaps(hdc, 10);
      float logical = GDI32.INSTANCE.GetDeviceCaps(hdc, 117);
      GDI32.INSTANCE.DeleteDC(hdc);
      if (logical != 0 && logical / actual > 1) {
        return (double) logical / actual;
      }
    }
    return Toolkit.getDefaultToolkit().getScreenResolution() / 96.0d;
  }

  /**
   * Validates the current world selection on startup, ensuring that it matches the
   * previously-selected world from the prior client launch. When the selection has changed, an
   * attempt will be made to locate a world with matching connection settings, in case it was just
   * the ordering that changed. If the world cannot be found, a message is presented to the user,
   * warning them to double-check the world file. Doing so helps minimize the risk that a user may
   * submit their credentials for one game server to another.
   */
  private static void validateWorldSelection() {
    // Nothing to compare, skip check
    if (Settings.noWorldsConfigured || lastConnHash == null) {
      return;
    }

    final Integer currConnHash =
        Integer.parseInt(Settings.WORLD_CONN_HASH.get(Settings.currentProfile));

    if (lastConnHash.equals(currConnHash)) {
      return;
    }

    // Look for a matching world connection
    int firstMatchingSub = -1;
    int numWorldFiles = Settings.getWorldFiles().size();
    for (int i = 1; i <= numWorldFiles; i++) {
      World world = World.fromSettings(i);

      // Look for the first world matching on the sub extension
      if (shouldDownloadWorlds()
          && firstMatchingSub == -1
          && worldSubscriptionId.equals(Extension.from(world.getServerExtension()))) {
        firstMatchingSub = i;
      }

      if (world.getConnectionHash().equals(lastConnHash)) {
        // A matching connection was found, update the current world
        Settings.WORLD.put(Settings.currentProfile, i);
        return;
      }
    }

    // Skip the warning if the previous world was removed as a result from the active subscription.
    // Though this assumes that the subscription extension would not change between client launches,
    // such a case would not happen without manual intervention.
    if (shouldDownloadWorlds() && updatedWorldSubHashes.contains(lastConnHash)) {
      String selectedWorldExtension =
          Settings.WORLD_SERVER_EXTENSION.get(Settings.WORLD.get(Settings.currentProfile));

      if (worldSubscriptionId.equals(Extension.from(selectedWorldExtension))) {
        return;
      }
    }

    if (firstMatchingSub != -1) {
      // If another world from the current sub was found, select it
      Settings.WORLD.put(Settings.currentProfile, firstMatchingSub);
      return;
    }

    // No matching worlds found, warn the user
    warnUserWorldChanged = true;
  }

  public static void main(String[] args) {
    /* Set application scaling */

    // MUST do this before anything else runs to override OS-level dpi settings,
    // since we have in-client scaling now (not applicable to macOS, which
    // implements OS-scaling in a different fashion)
    if (!Util.isMacOS()) {
      // Disable OS-level scaling in all JREs > 8
      System.setProperty("sun.java2d.uiScale.enabled", "false");
      System.setProperty("sun.java2d.uiScale", "1");

      // Required for newer versions of Oracle 8 to disable OS-level scaling
      System.setProperty("sun.java2d.dpiaware", "true");

      // Account for OS scaling on modern versions of Windows
      if (Util.isModernWindowsOS()) {
        OSScalingFactor = getScaleFactor();
        System.setProperty("flatlaf.uiScale", String.valueOf(OSScalingFactor));
        System.setProperty("flatlaf.useWindowDecorations", String.valueOf(false));

        // Forcibly disable the nimbus theme if selected, when the scaling factor is not 1.0
        if (OSScalingFactor != 1.0) {
          forceDisableNimbus = true;
        }
      }

      // Linux / other
      if (!Util.isWindowsOS()) {
        System.setProperty("GDK_SCALE", "1");
      }
    }

    if (Util.isMacOS()) {
      // Note: This only works on some Java 8 implementations... Hopefully
      // AdoptOpenJDK/Azul 8 fix their support for this eventually.
      System.setProperty("apple.awt.application.appearance", "system");
      System.setProperty("apple.awt.application.name", appName);
      System.setProperty("apple.laf.useScreenMenuBar", "true");
    }

    numCores = Runtime.getRuntime().availableProcessors();

    /* Proceed with all other initialization */

    // Initialize all server-extension related data
    ServerExtensions.bootstrap();

    // Extract startup properties related to binary usage and world subscriptions
    binaryFlavor = Extension.from(System.getProperty("usingBinary"));
    // Ensure the binary info is defined
    if (!ServerExtensions.hasBinaryInfo(binaryFlavor)) {
      binaryFlavor = null;
    }

    if (isUsingBinary()) {
      String binaryVersionString = System.getProperty("binaryVersion");
      if (binaryVersionString == null) {
        throw new RuntimeException("Must specify a binary version when declaring binary usage");
      }

      binaryVersion = Double.parseDouble(System.getProperty("binaryVersion"));

      binaryPrefix = ServerExtensions.getBinaryInfo(binaryFlavor).getConfigPrefix();
      appName = binaryPrefix + "RSCPlus";

      // Binary-specific icons
      if (binaryFlavor.equals(ServerExtensions.OPENRSC_OFFICIAL)) {
        largeIconPath = OpenRSCOfficialUtils.largeIconPath;
        iconPath = OpenRSCOfficialUtils.iconPath;
        smallIconPath = OpenRSCOfficialUtils.smallIconPath;
      } else {
        // Default binary icons
        iconPath = "/assets/rscplus_32.png";
        largeIconPath = "/assets/rscplus_64.png";
        smallIconPath = "/assets/rscplus_16.png";
      }
    } else {
      // Set default icons
      iconPath = "/assets/rscplus_32.png";
      largeIconPath = "/assets/rscplus_64.png";
      smallIconPath = "/assets/rscplus_16.png";
    }

    worldSubscriptionId = Extension.from(System.getProperty("downloadWorlds"));
    // Ensure the subscription is defined
    if (!ServerExtensions.hasWorldSubscription(worldSubscriptionId)) {
      worldSubscriptionId = null;
    }

    Settings.initDir();
    Logger.start();
    Logger.purgeOldestLogFiles();

    // Use a lock file to prevent multiple client instances from writing to the Worlds dir
    // simultaneously during app startup - this lock will be cleared in the Settings class,
    // immediately after "initWorlds()" has finished
    processWorldDirLock();

    // Deletes any empty worlds created via the "Add New World" settings button
    deleteEmptyWorlds();

    // Download server worlds for the current extension subscription
    if (shouldDownloadWorlds()) {
      downloadServerWorlds();
    } else {
      // Load known world types from settings such that they may get persisted
      loadKnownWorldTypesFromSettings();
    }

    final Properties props = Settings.initSettings();

    // Ensure the lock file got deleted if the proper removal failed for any reason
    if (initWorldLock.exists()) {
      initWorldLock.delete();
    }

    // For startup, can only detect via the packr-injected JVM flags (for regular end-users)
    if (shouldDownloadWorlds()) {
      Logger.Info("Using world subscription: [" + worldSubscriptionId + "]");
    }

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

    controlsFont = determineControlsFont();
    setScaledWindow(ScaledWindow.getInstance());
    setConfigWindow(new ConfigWindow());

    Settings.loadKeybinds(props);
    Settings.successfullyInitted = true;
    Settings.sanitizeSettings();
    Settings.loadCharacterSpecificSettings(true);

    // Fetch last world connection hash before any world changes occur
    if (props != null) {
      String connectionHash = (String) props.get("world_conn_hash");
      if (Util.isNotBlank(connectionHash)) {
        try {
          lastConnHash = Integer.parseInt(connectionHash);
        } catch (NumberFormatException nfe) {
          lastConnHash = null;
        }
      }
    }

    setWorldMapWindow(new WorldMapWindow());
    setQueueWindow(new QueueWindow());
    TrayHandler.initTrayIcon();

    // Add the "new instance" menu item to the macOS dock right-click menu
    if (isUsingBinary() && Util.isMacOS()) {
      // Add the tray menu to the right-click dock menu
      Application macApp = Application.getApplication();
      PopupMenu dockPopupMenu = new PopupMenu();
      dockPopupMenu.add(TrayHandler.createNewInstanceMenuItem());
      macApp.setDockMenu(dockPopupMenu);
    }

    NotificationsHandler.initialize();
    SoundEffects.loadCustomSoundEffects();

    Launcher.getInstance().init();
  }

  /**
   * Handles world directory file locking operations to prevent multiple application instances from
   * simultaneously writing to the directory
   */
  private static void processWorldDirLock() {
    if (Settings.Dir.CONFIG_DIR == null || Settings.Dir.WORLDS == null) {
      throw new RuntimeException(
          "Calling method before the proper directories have been initialized");
    }

    // Define the lock file
    initWorldLock = new File(Settings.Dir.CONFIG_DIR + File.separator + "initWorlds.lock");

    // Wait until the worlds lock has been released before proceeding
    // The following block of code will only affect people rapidly launching
    // multiple instances in succession
    Path downloadLockPath = initWorldLock.toPath();
    int iter = 0;
    try {
      // Max wait 30 seconds - when downloading worlds from a server subscription, this accounts
      // for roughly 10 different endpoints all timing out for the current sub, assuming someone
      // launched a second instance immediately after the first. The more instances they try to
      // launch at the same time, the higher the risk of hitting the limit.
      while (initWorldLock.exists() && iter < 120) {
        Thread.sleep(250);
        iter++;
      }
    } catch (InterruptedException e) {
      Logger.Error("Error waiting on world lock file to release");
    } finally {
      if (initWorldLock.exists()) {
        try {
          // Check if the lock file somehow got stuck and delete it if a reasonable amount of time
          // has passed, to cover the unbelievably remote possibility of something going wrong with
          // the shutdown hook or a still-running client having been unable to delete file
          long lockTime = Long.parseLong(new String(Files.readAllBytes(downloadLockPath)));
          if (System.currentTimeMillis() - lockTime > 120E3) { // Give it two minutes
            initWorldLock.delete();
          } else {
            // Tell the user to try again later - this really shouldn't ever happen unless
            // the user has a bizarrely huge amount of world files with the current subscription
            // and each request times out
            JFrame lockFileErrorFrame = Util.launchHiddenFrame();

            String lockFileTimeoutError =
                "A critical startup error occurred.<br/><br/>Please wait a minute or two before trying to relaunch the client.";
            JPanel lockFileTimeoutErrorPanel = Util.createOptionMessagePanel(lockFileTimeoutError);

            JOptionPane.showMessageDialog(
                lockFileErrorFrame,
                lockFileTimeoutErrorPanel,
                Launcher.appName,
                JOptionPane.ERROR_MESSAGE,
                Launcher.scaled_icon_warn);

            lockFileErrorFrame.dispose();

            System.exit(0);
          }
        } catch (IOException e) {
          Logger.Error("Error reading world lock file");
          // If it's all gone so wrong as to get to this point, just delete the lock file
          initWorldLock.delete();
        }
      }
    }

    try {
      // Create the lock file and store the current time
      Files.createFile(downloadLockPath);
      Files.write(downloadLockPath, String.valueOf(System.currentTimeMillis()).getBytes());
    } catch (IOException e) {
      // Don't want to prevent app startup if it can't do this
      Logger.Error("Error creating world lock file");
    } finally {
      // Add a shutdown hook to delete the lock file in case the app exited before method completion
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    if (initWorldLock.exists()) {
                      initWorldLock.delete();
                    }
                  }));
    }
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
   * @return {@code boolean} value indicating whether a binary launched this client instance
   *     <p>NOTE: Will return {@code null} if called before {@link ServerExtensions#bootstrap()}
   */
  public static boolean isUsingBinary() {
    return binaryFlavor != null && ServerExtensions.hasBinaryInfo(binaryFlavor);
  }

  /**
   * @return {@code boolean} value indicating whether a Linux AppImage launched the current instance
   *     of rscplus
   */
  public static boolean isUsingAppImage() {
    return isUsingBinary() && !Util.isWindowsOS() && !Util.isMacOS();
  }

  /**
   * Deletes empty worlds on startup to prevent launch errors due to world validation failures, in
   * case the user clicked the "add new world" button without filling it out, had a legacy "World 1"
   * file from earlier versions of RSC+, or the world file somehow managed to become empty.
   */
  private static void deleteEmptyWorlds() {
    final Map<File, World> existingWorldFileMap = World.parseFilesToMap(Settings.getWorldFiles());

    if (existingWorldFileMap.isEmpty()) {
      return;
    }

    final World emptyWorld = World.createEmptyWorld();

    existingWorldFileMap.forEach(
        (file, world) -> {
          if (file.length() <= 0 || world.connectionEquals(emptyWorld)) {
            file.delete();
            Logger.Info("Deleted empty world file: " + file.getAbsolutePath());
          }
        });
  }

  /**
   * @return {@code boolean} value indicating whether world files should be downloaded from a
   *     subscription API
   */
  public static boolean shouldDownloadWorlds() {
    return worldSubscriptionId != null
        && ServerExtensions.hasWorldSubscription(worldSubscriptionId);
  }

  /** Fetches and stores server world files for the current subscription */
  private static void downloadServerWorlds() {
    if (!shouldDownloadWorlds()) {
      return;
    }

    if (Settings.Dir.WORLDS == null) {
      throw new RuntimeException("Calling method before the worlds folder has been initialized");
    }

    final ServerExtensions.WorldSubscription serverWorldSub =
        ServerExtensions.getWorldSubscription(worldSubscriptionId);
    final String worldHost = serverWorldSub.getHostUrl();

    final List<WorldType> subTypeExclusions = new ArrayList<>();

    // Parse settings to determine which world types are excluded
    final Map<WorldType, URI> worldDownloadURIMap = serverWorldSub.getWorldDownloadURIs();
    final Properties configProps = Settings.loadPropsIfExist();
    final Map<WorldType, Boolean> currentWorldTypes = new LinkedHashMap<>();

    worldDownloadURIMap
        .keySet()
        .forEach(
            worldType -> {
              final boolean shouldDownload;

              if (configProps == null) {
                // First time running the client
                shouldDownload = serverWorldSub.shouldDownloadWorldsByDefault(worldType);
              } else {
                // All subsequent client launches
                final String downloadIdProp =
                    configProps.getProperty(
                        "dlWorldType." + worldSubscriptionId.getId() + "." + worldType);

                if (downloadIdProp == null) {
                  shouldDownload = serverWorldSub.shouldDownloadWorldsByDefault(worldType);
                } else {
                  shouldDownload = Boolean.parseBoolean(downloadIdProp);
                }
              }

              // Store download flag for the current type
              currentWorldTypes.put(worldType, shouldDownload);

              // Store exclusion for the current type
              if (!shouldDownload) {
                subTypeExclusions.add(worldType);
              }
            });

    // Ensure at least one type will be downloaded, if the user manually mucked around with their
    // config.ini file
    if (!currentWorldTypes.containsValue(true)) {
      worldDownloadURIMap
          .keySet()
          .forEach(
              worldType -> {
                boolean defaultDownload = serverWorldSub.shouldDownloadWorldsByDefault(worldType);
                currentWorldTypes.put(worldType, defaultDownload);

                if (defaultDownload) {
                  subTypeExclusions.remove(worldType);
                }
              });
    }

    // Flag current world type for downloading
    knownWorldTypes.put(worldSubscriptionId, currentWorldTypes);

    // Load all other previously-saved types so they get persisted
    if (configProps != null) {
      loadKnownWorldTypesFromSettings(configProps);
    }

    /* Download and process world files for each subscription API */
    final Map<File, World> existingWorldFiles = World.parseFilesToMap(Settings.getWorldFiles());

    worldDownloadURIMap.forEach(
        (worldType, uri) -> {
          // If the worldType is skipped due to user settings,
          // delete all world files that declare this type
          if (subTypeExclusions.contains(worldType)) {
            existingWorldFiles
                .entrySet()
                .removeIf(
                    entry -> {
                      File file = entry.getKey();
                      World world = entry.getValue();

                      if (world.wasDownloaded()
                          && (world.hostMatches(worldHost)
                              || serverWorldSub.formerDomainMatch(world))
                          && world.idMatchesWorldType(worldType)) {
                        file.delete();

                        updatedWorldSubHashes.add(world.getConnectionHash());

                        return true;
                      }

                      return false;
                    });

            return; // Move onto the next type
          }

          // Download worlds for the current type
          boolean errorObtainingAllFilesForType = false;
          final List<World> fetchedServerWorlds = fetchServerWorlds(uri);

          // Mark when files couldn't be downloaded for the type
          if (fetchedServerWorlds == null) {
            errorObtainingAllFilesForType = true;
          }

          // Ensure all worlds for the type are valid before proceeding
          if (!errorObtainingAllFilesForType) {
            for (World fetchedWorld : fetchedServerWorlds) {
              try {
                World.validateWorld(fetchedWorld);
              } catch (IllegalArgumentException iae) {
                // Treat this situation as a download failure
                setWarning(LauncherError.WORLD_DOWNLOADS);
                Launcher.downloadedWorlds.put(uri, null);
                Logger.Error(
                    "Worlds subscription for "
                        + worldSubscriptionId
                        + " provided an invalid world file that was not imported");

                errorObtainingAllFilesForType = true;
                break;
              }
            }
          }

          // Errors downloading the worlds for the type should short-circuit the replacement and
          // add all unique existing world files to the master list that match the current worldType
          if (errorObtainingAllFilesForType) {
            final Set<World> uniqueMatchingWorlds = new HashSet<>();
            existingWorldFiles.entrySet().stream()
                .filter(
                    entry -> {
                      World world = entry.getValue();

                      return world.wasDownloaded()
                          && (world.hostMatches(worldHost)
                              || serverWorldSub.formerDomainMatch(world))
                          && world.extensionMatches(worldSubscriptionId)
                          && world.idMatchesWorldType(worldType);
                    })
                .filter(entry -> uniqueMatchingWorlds.add(entry.getValue()))
                .map(Map.Entry::getKey)
                .forEach(file -> subWorldFiles.add(file));

            return; // Move onto the next world type
          }

          // Process each downloaded world for the current type
          for (World fetchedWorld : fetchedServerWorlds) {
            // Check for a mismatch between the retrieved world URL and the sub host URL in RSC+
            if (!fetchedWorld.hostMatches(worldHost)) {
              // Inform the user of the unexpected state
              setWarning(LauncherError.WORLD_SUB_MISMATCH);

              // Save the world to a blocklist to prevent the user from connecting to it later on
              blockedWorlds.add(fetchedWorld);
            }

            boolean existing = false;

            // Look for a pre-existing exact-match world file, to skip
            // replacement on disk when the sub data hasn't changed
            for (Map.Entry<File, World> existingWorldFile : existingWorldFiles.entrySet()) {
              if (existingWorldFile.getValue().equals(fetchedWorld)) {
                // Mark existing and add the existing world file to the master world list
                existing = true;
                subWorldFiles.add(existingWorldFile.getKey());
                break;
              }
            }

            // If no match was found, create a new world file and add it to the master world list
            if (!existing) {
              String newFileName = Settings.Dir.WORLDS + File.separator + fetchedWorld.getName();
              File newWorldFile = new File(newFileName + ".ini");

              // Fall back when world names are re-used, though ideally no one will do this
              // If this manages to generate a conflicting file let's call it an Easter egg
              if (newWorldFile.exists()) {
                newWorldFile =
                    new File(newFileName + "-" + java.util.UUID.randomUUID().hashCode() + ".ini");
              }

              try (FileWriter fw = new FileWriter(newWorldFile)) {
                World.toProperties(fetchedWorld).store(fw, null);
                subWorldFiles.add(newWorldFile);
              } catch (Exception e) {
                if (newWorldFile.exists()) {
                  newWorldFile.delete();
                }
                // Treat errors saving the file as if the download had failed
                // The old file will not be deleted since it wouldn't have been
                // added to subWorldFiles
                setWarning(LauncherError.WORLD_DOWNLOADS);
                Logger.Warn("Error occurred while attempting to store the downloaded world");
                e.printStackTrace();
              }
            }
          }

          // Gather all blocked URLs
          Set<String> blockedUrls = new HashSet<>();
          if (!blockedWorlds.isEmpty()) {
            blockedWorlds.forEach(world -> blockedUrls.add(world.getUrl()));
          }

          // Delete existing world files for the current WorldType that
          // were updated or removed in the subscription data
          existingWorldFiles.forEach(
              (file, world) -> {
                if (!subWorldFiles.contains(file) // wasn't downloaded this launch
                    && world.wasDownloaded() // was previously downloaded at some point
                    && (world.hostMatches(worldHost)
                        || world.hostMatchesAny(
                            blockedUrls)) // host matches or blocked world downloaded
                    && world.idMatchesWorldType(worldType)) { // world id matches the current type
                  file.delete();

                  // Add to the list of connection hashes for later comparison
                  updatedWorldSubHashes.add(world.getConnectionHash());
                }
              });
        });

    /* * *
     * Attempt to locate and delete any remaining world files that contain server URLs which match either the current
     * subscription host domain or any of its former domains. These would typically be files that were manually added
     * via the settings GUI or dropped into the worlds directory, as the logic earlier in this method would have
     * already deleted any world files that were previously obtained from subscription APIs, with some rare exceptions.
     */
    World.parseFilesToMap(Settings.getWorldFiles()).entrySet().stream()
        .filter(
            entry ->
                entry.getValue().hostMatches(worldHost) // host matches OR
                    || serverWorldSub.formerDomainMatch(entry.getValue())) // former domain matches
        .filter(entry -> !subWorldFiles.contains(entry.getKey())) // wasn't preserved earlier
        .forEach(
            entry -> {
              final File file = entry.getKey();
              final World world = entry.getValue();

              if (hasWarning(LauncherError.WORLD_DOWNLOADS)) {
                /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                 * When the sub endpoint is experiencing connectivity issues, any remaining world files pertaining to the
                 * current subscription host should be retained UNLESS a particular world:
                 *
                 * 1) Has matching connection data to another world which was already retained
                 *    OR
                 * 2) Contains a URL matching a retired host domain for the world sub
                 *
                 * This retention mechanism is needed to account for manually-added world files lacking extension data,
                 * such that users who do not already have a previously-downloaded world file may connect to the server
                 * even when the world subscription is down. Any extraneous files preserved via this mechanism will get
                 * properly replaced during a subsequent launch in which the sub API once again becomes reachable.
                 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
                List<World> subWorlds =
                    new ArrayList<>(World.parseFilesToMap(subWorldFiles).values());

                boolean hostMatches = world.hostMatches(worldHost);
                boolean noRetainedMatch =
                    subWorlds.stream().noneMatch(subWorld -> subWorld.connectionEquals(world));

                if (hostMatches && noRetainedMatch) {
                  subWorldFiles.add(file);
                } else {
                  file.delete();
                  updatedWorldSubHashes.add(world.getConnectionHash());
                }
              } else {
                file.delete();
                updatedWorldSubHashes.add(world.getConnectionHash());
              }
            });
  }

  /**
   * Overload for invoking {@link #loadKnownWorldTypesFromSettings(Properties)}, using config.ini
   * properties, if they exist
   */
  private static void loadKnownWorldTypesFromSettings() {
    final Properties configProps = Settings.loadPropsIfExist();

    // Nothing to load on first run
    if (configProps == null) {
      return;
    }

    loadKnownWorldTypesFromSettings(configProps);
  }

  /**
   * Loads known world types from settings such that they may get persisted on the next save
   *
   * @param configProps {@link Properties} instance corresponding to the config.ini file
   */
  private static void loadKnownWorldTypesFromSettings(Properties configProps) {
    if (configProps == null) {
      return;
    }

    // Gather valid subscriptions and their world types
    Set<Extension> serverSubs = new LinkedHashSet<>();
    List<String> dlWorldTypeSettings = new ArrayList<>();
    for (Object prop : configProps.keySet()) {
      final String dlWorldTypeSetting = (String) prop;
      if (dlWorldTypeSetting.startsWith("dlWorldType.")) {
        String[] dlWorldTypeSettingParts = dlWorldTypeSetting.split("\\.");
        // Known world types have already been handled for the
        // current subscription within downloadServerWorlds
        if (!Launcher.knownWorldTypes.containsKey(worldSubscriptionId)
            || !dlWorldTypeSettingParts[1].equals(worldSubscriptionId.getId())) {
          Extension settingExtension = Extension.from(dlWorldTypeSetting.split("\\.")[1]);
          // Check if it's still a valid world subscription - if not, the setting will get erased
          if (ServerExtensions.hasWorldSubscription(settingExtension)) {
            serverSubs.add(settingExtension);
            dlWorldTypeSettings.add(dlWorldTypeSetting);
          }
        }
      }
    }

    // Fill in the map with parsed data
    for (Extension serverSub : serverSubs) {
      Map<WorldType, Boolean> downloadWorldTypeFlags = new LinkedHashMap<>();
      for (String dlWorldTypeSetting : dlWorldTypeSettings) {
        String[] dlWorldTypeSettingParts = dlWorldTypeSetting.split("\\.");
        if (dlWorldTypeSettingParts[1].equals(serverSub.getId())) {
          try {
            downloadWorldTypeFlags.put(
                WorldType.from(dlWorldTypeSettingParts[2]),
                Boolean.parseBoolean(configProps.getProperty(dlWorldTypeSetting)));
          } catch (Exception e) {
            // no-op
          }
        }
      }
      knownWorldTypes.put(serverSub, downloadWorldTypeFlags);
    }
  }

  /**
   * Download the world files for a given server world type or return from the cached download
   *
   * @return A {@link List} of the downloaded and parsed {@link World}s or {@code null} if any
   *     errors were encountered while downloading the world files
   */
  static List<World> fetchServerWorlds(URI uri) {
    // Return from the cache if it exists
    if (downloadedWorlds.containsKey(uri)) {
      return downloadedWorlds.get(uri);
    }

    JDialog downloadingDialog = new JDialog();

    // Create the worker that will perform the download
    DownloadWorldsTask task = new DownloadWorldsTask(uri, downloadingDialog);

    downloadingDialog.setUndecorated(true);
    downloadingDialog.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
    JLabel label = new JLabel();

    ImageIcon scaledDownloadAlertIcon = null;
    URL downloadAlertIconURL = Launcher.getResource("/assets/downloading.png");
    try {
      BufferedImage downloadAlertBI = ImageIO.read(downloadAlertIconURL);
      scaledDownloadAlertIcon =
          new ImageIcon(
              downloadAlertBI.getScaledInstance(
                  osScaleMul(downloadAlertBI.getWidth()),
                  osScaleMul(downloadAlertBI.getHeight()),
                  Image.SCALE_DEFAULT));
    } catch (IOException e) {
      // No-op
    }

    downloadingDialog.setModal(true); // Needed to properly block the EDT
    downloadingDialog.add(label);
    downloadingDialog.pack();
    downloadingDialog.setLocationRelativeTo(null);

    // Only begin the download after the empty modal has been drawn to the screen
    ImageIcon finalScaledDownloadAlertIcon = scaledDownloadAlertIcon;
    downloadingDialog.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent evt) {
            super.componentShown(evt);
            long now = System.currentTimeMillis();
            // Begin downloading on a worker thread
            task.execute();

            // If enough time has elapsed without a success, draw the alert banner in the modal
            int loop = 0;
            while (!task.isDone() && loop < 10) {
              try {
                Thread.sleep(100);
                loop++;

                // After 1 second of waiting, display the banner
                if (loop == 10) {
                  if (finalScaledDownloadAlertIcon != null) {
                    label.setIcon(finalScaledDownloadAlertIcon);
                    int newX =
                        downloadingDialog.getX()
                            - (finalScaledDownloadAlertIcon.getIconWidth() / 2);
                    int newY =
                        downloadingDialog.getY()
                            - (finalScaledDownloadAlertIcon.getIconHeight() / 2);
                    downloadingDialog.setLocation(newX, newY);
                    downloadingDialog.pack();
                    downloadingDialog.revalidate();
                    downloadingDialog.repaint();
                  }
                }
              } catch (InterruptedException e) {
                Logger.Warn("Error fetching subscription worlds");
              }
            }
          }
        });

    // Open the modal, but don't draw the alert banner yet
    downloadingDialog.setVisible(true);

    if (task.isDone()) {
      try {
        // Retrieve the downloaded worlds
        return task.get();
      } catch (InterruptedException | ExecutionException e) {
        Logger.Warn("Error fetching subscription worlds");
        return null;
      }
    }

    return null;
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
        url = new URL("file://" + Util.findDirectoryReverseFromJAR("/assets") + fileName);
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
        if (Settings.Dir.JAR != null) {
          stream = new FileInputStream(Util.findDirectoryReverseFromJAR("/assets") + fileName);
        } else {
          stream = new FileInputStream(Util.findDirectoryReverseFromLoc(".", "/assets") + fileName);
        }
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

    // Load glyph data for custom Jagex fonts
    GameApplet.loadGlyphData();

    if (Client.firstTime) {
      // Set server type variables when no world was selected for replay comparisons
      if (Settings.noWorldsConfigured) {
        Client.members = true;
        Client.veterans = false;
        Client.worldMembers = true;
        Client.worldVeterans = false;
      }
      Client.firstTime = false;
    }

    // Warn the user that their last world choice has changed unexpectedly
    if (warnUserWorldChanged) {
      String worldChangedWarningMessage =
          "<b>IMPORTANT</b>: Your previously-selected world has unexpectedly changed since the last launch!<br/><br/>"
              + "Make sure you have the correct world selected <b>before trying to log in.</b>";
      JPanel worldChangedWarningPanel = Util.createOptionMessagePanel(worldChangedWarningMessage);

      JOptionPane.showMessageDialog(
          ScaledWindow.getInstance(),
          worldChangedWarningPanel,
          appName,
          JOptionPane.ERROR_MESSAGE,
          scaled_icon_warn);
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
    try (BufferedInputStream source = new BufferedInputStream(getResourceAsStream(pathInJar));
        BufferedOutputStream target =
            new BufferedOutputStream(Files.newOutputStream(destinationPath.toPath()))) {
      byte[] buf = new byte[8192];
      int length;
      while ((length = source.read(buf)) > 0) {
        target.write(buf, 0, length);
      }
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

  private static Font determineControlsFont() {
    Font font = new Font(Font.SANS_SERIF, Font.PLAIN, osScaleMul(18));
    if (canRenderControlSymbol(font)) {
      return font;
    }

    // Current font is not suitable, using our fallback
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    try (InputStream is = getResourceAsStream("/assets/Symbola_Hinted.ttf")) {
      ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
      font = new Font("Symbola", Font.PLAIN, osScaleMul(18));

      if (canRenderControlSymbol(font)) {
        return font;
      } else {
        // This shouldn't happen unless someone broke the Symbola asset
        Font firstSerifFont = getFirstAvailableSerifFont();
        if (firstSerifFont != null && canRenderControlSymbol(firstSerifFont)) {
          return firstSerifFont;
        } else {
          /*
           * If we can't render the default serif font, or load the provided Symbola font,
           * or render the first available font containing "serif" in the name, then just
           * set it to null and then later draw text labels for the buttons.
           * This is very much overkill.
           */
          return null;
        }
      }
    } catch (FontFormatException | IOException e) {
      // Will render text labels instead of unicode (see above)
      return null;
    }
  }

  private static Font getFirstAvailableSerifFont() {
    Optional<Font> fontOptional =
        Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
            .filter(f -> f.getFontName().toLowerCase().contains("serif"))
            .findFirst();
    return fontOptional.orElse(null);
  }

  /**
   * Workaround for observed broken behavior in {@link Font#canDisplayUpTo}, which incorrectly
   * reported that certain unicode characters may be rendered in a given font. This method instead
   * draws the character to a temporary image and checks whether bounds were set in order to make
   * the determination.
   *
   * @param font The font to check
   * @return {@code boolean} indicating whether the symbol can be rendered
   */
  private static boolean canRenderControlSymbol(Font font) {
    String uppermostChar = "\uD83D\uDD00";

    BufferedImage glyphTest = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = (Graphics2D) glyphTest.getGraphics();
    GlyphVector gv = font.createGlyphVector(g2.getFontRenderContext(), uppermostChar);

    boolean canDrawGlyph = !gv.getOutline().getBounds().isEmpty();

    g2.dispose();
    glyphTest.flush();

    return canDrawGlyph;
  }

  /** {@link SwingWorker} responsible for downloading world file data from a subscription API */
  static class DownloadWorldsTask extends SwingWorker<List<World>, Void> {
    final URI worldDownloadUri;
    final JDialog dialog;

    DownloadWorldsTask(URI worldDownloadUri, JDialog dialog) {
      this.worldDownloadUri = worldDownloadUri;
      this.dialog = dialog;
    }

    @Override
    public List<World> doInBackground() {
      try {
        final URL downloadURL = worldDownloadUri.toURL();

        HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
          throw new RuntimeException("Connection returned HTTP response: [" + responseCode + "]");
        }

        int size = connection.getContentLength();
        if (size == 0) {
          throw new RuntimeException("Connection returned empty content");
        }

        int offset = 0;
        byte[] data = new byte[size];

        try (InputStream input = connection.getInputStream()) {
          int readSize;
          while ((readSize = input.read(data, offset, size - offset)) != -1) {
            offset += readSize;
          }

          // Data successfully received
          if (offset == size) {
            JSONArray worldsJSON = new JSONArray(new String(data, StandardCharsets.UTF_8));

            // Parse and save world JSON files to map
            final List<World> fetchedWorlds = new ArrayList<>();

            for (int i = 0; i < worldsJSON.length(); i++) {
              World world = World.fromJSON(worldsJSON.getJSONObject(i));
              world.setDownloadFlag(true);

              // In case duplicate worlds were defined within a sub API, only save one of them
              if (!fetchedWorlds.contains(world)) {
                fetchedWorlds.add(world);
              }
            }

            // Cache downloaded info for this client session
            downloadedWorlds.put(worldDownloadUri, fetchedWorlds);

            return fetchedWorlds;
          } else {
            throw new RuntimeException("Failed to download server worlds");
          }
        }
      } catch (Exception e) {
        Logger.Warn(
            "Error occurred while fetching the server world listing for: ["
                + worldDownloadUri
                + "]");
        e.printStackTrace();
      }

      // Cache a negative hit for the client session to avoid repeated connection errors
      downloadedWorlds.put(worldDownloadUri, null);

      // For error rendering
      setWarning(LauncherError.WORLD_DOWNLOADS);

      return null;
    }

    @Override
    protected void done() {
      // Remove the "fetching" modal when finished
      dialog.setVisible(false);
      dialog.dispose();
    }
  }

  /* Launcher warnings */

  /**
   * Used for tracking various error condition states to make logical decisions and display warnings
   * to the user on the login screen
   */
  public enum LauncherError {
    BINARY_UPDATE_CHECK,
    WORLD_DOWNLOADS,
    WORLD_SUB_MISMATCH
  }

  public static boolean hasWarnings() {
    return !launcherWarnings.isEmpty();
  }

  public static boolean hasWarning(LauncherError launcherError) {
    return launcherWarnings.contains(launcherError);
  }

  public static void setWarning(LauncherError launcherError) {
    launcherWarnings.add(launcherError);
  }

  public static void clearWarning(LauncherError launcherError) {
    launcherWarnings.remove(launcherError);
  }
}
