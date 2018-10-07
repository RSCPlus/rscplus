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
 * <p>Authors: see <https://github.com/OrN/rscplus> and <https://github.com/Hubcapp/rscplus>
 */
package Game;

import Client.Launcher;
import Client.Logger;
import Client.Settings;
import Client.Util;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ReplayQueue {
  public static int currentIndex = 0;
  public static String currentReplayName = "";
  public static boolean skipped = false;
  public static ArrayList<File> queue = new ArrayList<File>();

  // returns if it found valid replays in the directory chosen
  public static boolean replayFileSelectAdd() {
    JFileChooser j = new JFileChooser(Settings.Dir.REPLAY);
    j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int response = j.showDialog(Game.getInstance().getApplet(), "Select Folder");

    File selection = j.getSelectedFile();
    if (selection != null && response != JFileChooser.CANCEL_OPTION) {
      ReplayQueue.currentReplayName = selection.getPath();
      if (Replay.isValid(ReplayQueue.currentReplayName)) {
        return true;
      } else {
        ArrayList<File> replays = Util.getAllReplays(ReplayQueue.currentReplayName);
        if (replays.size() > 0) {
          ReplayQueue.queue.addAll(replays);
          Logger.Info(
              String.format(
                  "Added %d replays to the queue. New size: %d",
                  replays.size(), ReplayQueue.queue.size()));
          if (currentIndex < 0) currentIndex = 0;
          ReplayQueue.currentReplayName = queue.get(currentIndex).getAbsolutePath();
          return true;
        } else {
          JOptionPane.showMessageDialog(
              Game.getInstance().getApplet(),
              "The replay folder you selected is not valid.\n"
                  + "\n"
                  + "You need to select a folder that contains the 'version.bin', 'in.bin.gz', and 'keys.bin' for your replay.\n"
                  + "They're usually in a folder with your login username.",
              "rscplus",
              JOptionPane.ERROR_MESSAGE,
              Launcher.icon_warn);
        }
      }
    }
    return false;
  }

  static DropTarget dropReplays =
      new DropTarget() {
        public synchronized void drop(DropTargetDropEvent evt) {
          try {
            evt.acceptDrop(DnDConstants.ACTION_LINK);
            List<File> droppedFiles =
                (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            for (File selection : droppedFiles) {
              if (selection != null) {
                ReplayQueue.currentReplayName = selection.getPath();
                Logger.Debug(ReplayQueue.currentReplayName);
                if (Replay.isValid(ReplayQueue.currentReplayName)
                    && Client.state == Client.STATE_LOGIN) {
                  Logger.Info("Replay selected: " + ReplayQueue.currentReplayName);
                  Client.runReplayHook = true;
                  return;
                } else {
                  ArrayList<File> replays = Util.getAllReplays(ReplayQueue.currentReplayName);
                  if (replays.size() > 0) {
                    ReplayQueue.queue.addAll(replays);
                    Logger.Info(
                        String.format(
                            "Added %d replays to the queue. New size: %d",
                            replays.size(), ReplayQueue.queue.size()));
                    if (Client.state == Client.STATE_LOGIN) {
                      ReplayQueue.playFromQueue(currentIndex);
                    }
                  } else {
                    JOptionPane.showMessageDialog(
                        Game.getInstance().getApplet(),
                        "The folder you dropped onto the client is not a replay, nor does it contain replay folders.\n"
                            + "\n"
                            + "You need to drop a folder that contains a 'version.bin', 'in.bin.gz', and 'keys.bin' for the replay.",
                        "rscplus",
                        JOptionPane.ERROR_MESSAGE,
                        Launcher.icon_warn);
                  }
                }
              }
            }
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      };

  public static void nextReplay() {
    if (queue.size() - 1 > currentIndex) {
      currentIndex++;
      playFromQueue(currentIndex);
    } else {
      Logger.Info("Reached end of queue!");
    }
  }

  public static void previousReplay() {
    if (currentIndex > 0) {
      currentIndex--;
      playFromQueue(currentIndex);
    } else {
      Logger.Info("Reached beginning of queue!");
    }
  }

  public static void playFromQueue(int index) {
    if (index < 0) {
      index = 0;
    }
    if (index > queue.size() - 1) {
      index = queue.size() - 1;
    }
    if (Replay.isPlaying) {
      Replay.controlPlayback("stop");
      try {
        Thread.sleep(800); // without this at all, client says user is still logged in lol
      } catch (Exception e) { // through experimentation, I found that 700 is not long enough.
        // this value might work. shorter, and the replay server has trouble keeping its
      } // timestamps straight... TODO: eliminate need for this delay.
    }
    currentReplayName = queue.get(index).getAbsolutePath();
    Logger.Info("Selected " + currentReplayName);
    Client.runReplayHook = true;
    // Client.login_hook();
  }

  public static void clearQueue() {
    queue = new ArrayList<File>();
    if (Replay.isPlaying) {
      currentIndex =
          -1; // so that it goes to 0th recording when more are added to queue after clearing
    } else {
      currentIndex = 0;
    }
  }
}
