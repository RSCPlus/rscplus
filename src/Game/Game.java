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
package Game;

import Client.JConfig;
import Client.Launcher;
import Client.Logger;
import Client.NotificationsHandler;
import Client.ScaledWindow;
import Client.Settings;
import Client.TrayHandler;
import Client.Util;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.net.URL;
import java.text.DecimalFormat;
import javax.swing.JFrame;

/**
 * Singleton class that handles packaging the client into a JFrame and starting the applet. <br>
 * <br>
 * This frame is never shown to the user, as all graphics rendered by the applet are displayed
 * within {@link ScaledWindow}, which in turn forwards all user input and window management events
 * back to this class, such that they may be received by the applet. <br>
 * <br>
 * This design is necessary due to the fact that the applet performs rendering calculations based on
 * the current window size, so in order to accommodate client scaling, we must trick the applet into
 * thinking it is always at a fixed window size. Resizes of the {@link ScaledWindow} will tell the
 * applet to resize itself, which will actually render outside the window bounds of the {@link Game}
 * window, though it is acceptable to do so, as the {@link Game} window is never shown to the user.
 */
public class Game extends JFrame implements AppletStub, ComponentListener, WindowListener {

  // Singleton
  private static Game instance = null;

  private JConfig m_config = new JConfig();
  private Applet m_applet = null;
  private String m_title = "";

  private Game() {
    // Empty private constructor to prevent extra instances from being created.
  }

  public void setApplet(Applet applet) {
    m_applet = applet;
    m_applet.setStub(this);
  }

  public Applet getApplet() {
    return m_applet;
  }

  /** Builds the main game client window and adds the applet to it. */
  public void start() {
    if (m_applet == null) return;

    // Set window properties
    setResizable(true); // Must be enabled to accommodate the current loading behavior
    addWindowListener(this);
    setMinimumSize(new Dimension(1, 1));

    // Add applet to window
    setContentPane(m_applet);
    getContentPane().setBackground(Color.BLACK);
    getContentPane().setPreferredSize(new Dimension(512, 346));
    addComponentListener(this);
    pack();

    // Position window, but do not display it,
    // as all graphics from the applet are
    // forwarded to the ScaledWindow class
    setLocationRelativeTo(null);
    setVisible(false);

    // Launch delegated rendering window
    ScaledWindow.getInstance().launchScaledWindow();

    updateTitle();

    MusicPlayer.init();
    Reflection.Load();
    Renderer.init();
    JoystickHandler.init();
  }

  public JConfig getJConfig() {
    return m_config;
  }

  /** Starts the game applet. */
  public void launchGame() {
    m_config.changeWorld(Settings.WORLD.get(Settings.currentProfile));
    m_applet.init();
    m_applet.start();
  }

  public void updateTitle() {
    String title = "rscplus (";

    if (!Replay.isPlaying) {
      title += Settings.WORLD_NAMES.get(Settings.WORLD.get(Settings.currentProfile));

      if (Client.player_name.length() != 0) {
        title += "; " + Client.player_name;
      }
    } else {
      String elapsed = Util.formatTimeDuration(Replay.elapsedTimeMillis(), Replay.endTimeMillis());
      String end = Util.formatTimeDuration(Replay.endTimeMillis(), Replay.endTimeMillis());
      title += elapsed + " / " + end;
      title += ", Speed: " + new DecimalFormat("##.##").format(Replay.fpsPlayMultiplier) + "x";
      if (Replay.paused) title += ", Paused";
    }

    if (Replay.isRecording) {
      String elapsed =
          Util.formatTimeDuration(Replay.elapsedTimeMillis(), Replay.elapsedTimeMillis());
      title += "; Recording: " + elapsed;
    }

    title += ")";

    if (m_title.equals(title)) {
      return;
    }
    m_title = title;
    super.setTitle(m_title);

    ScaledWindow.getInstance().setTitle(m_title);
  }

  /*
   * AppletStub methods
   */

  @Override
  public final URL getCodeBase() {
    return m_config.getURL("codebase");
  }

  @Override
  public final URL getDocumentBase() {
    return getCodeBase();
  }

  @Override
  public final String getParameter(String key) {
    return m_config.parameters.get(key);
  }

  @Override
  public final AppletContext getAppletContext() {
    return null;
  }

  @Override
  public final void appletResize(int width, int height) {}

  /*
   * WindowListener methods
   */

  @Override
  public final void windowClosed(WindowEvent e) {
    if (m_applet == null) return;

    MusicPlayer.close();

    m_applet.stop();
    m_applet.destroy();
    m_applet = null;

    Logger.stop();
  }

  @Override
  public final void windowClosing(WindowEvent e) {
    dispose();
    Launcher.getScaledWindow().disposeJFrame();
    Launcher.getConfigWindow().disposeJFrame();
    Launcher.getQueueWindow().disposeJFrame();
    Launcher.getWorldMapWindow().disposeJFrame();
    TrayHandler.removeTrayIcon();
    NotificationsHandler.closeNotificationSoundClip();
    NotificationsHandler.disposeNotificationHandler();
  }

  @Override
  public final void windowOpened(WindowEvent e) {}

  @Override
  public final void windowDeactivated(WindowEvent e) {}

  @Override
  public final void windowActivated(WindowEvent e) {}

  @Override
  public final void windowDeiconified(WindowEvent e) {}

  @Override
  public final void windowIconified(WindowEvent e) {}

  /*
   * ComponentListener methods
   */

  @Override
  public final void componentHidden(ComponentEvent e) {}

  @Override
  public final void componentMoved(ComponentEvent e) {}

  @Override
  public final void componentResized(ComponentEvent e) {
    if (m_applet == null) return;

    // Handle minimum size and launch game
    // TODO: This is probably a bad spot and should be moved
    if (getMinimumSize().width == 1) {
      setMinimumSize(getSize());
      launchGame();
    }
  }

  @Override
  public final void componentShown(ComponentEvent e) {}

  /**
   * Gets the game client instance. It makes one if one doesn't exist.
   *
   * @return The game client instance
   */
  public static Game getInstance() {
    if (instance == null) {
      synchronized (Game.class) {
        instance = new Game();
      }
    }
    return instance;
  }
}
