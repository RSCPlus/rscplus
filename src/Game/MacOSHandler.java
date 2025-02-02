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

import Client.ConfigWindow;
import Client.Launcher;
import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/** macOS-specific application handling */
public class MacOSHandler implements QuitHandler, AboutHandler {
  private final JFrame gameFrame;

  /**
   * Public constructor for {@link MacOSHandler}
   *
   * @param gameFrame {@link Game} reference
   */
  MacOSHandler(JFrame gameFrame) {
    this.gameFrame = gameFrame;
  }

  /** Binds macOS-specific handlers */
  void bindAppHandlers() {
    Application application = Application.getApplication();

    // Hook "about" menu item to open the
    application.setAboutHandler(this);

    // Hook quit / cmd-q functionality to properly shut down the application
    application.setQuitHandler(this);
  }

  @Override
  public void handleAbout(AppEvent.AboutEvent aboutEvent) {
    int authorsTabIndex = ConfigWindow.ConfigTab.getTabIndex(ConfigWindow.ConfigTab.AUTHORS);

    if (authorsTabIndex > -1) {
      Launcher.getConfigWindow().setInitiatedTab(-1); // Reset current tab
      Launcher.getConfigWindow()
          .setSelectedTab(ConfigWindow.ConfigTab.getTabIndex(ConfigWindow.ConfigTab.AUTHORS));
      Launcher.getConfigWindow().showConfigWindow();
    }
  }

  @Override
  public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse) {
    // Used to handle the macOS ⌘Q event, to replicate the same behavior as closing the window
    gameFrame.dispatchEvent(new WindowEvent(gameFrame, WindowEvent.WINDOW_CLOSING));
  }
}
