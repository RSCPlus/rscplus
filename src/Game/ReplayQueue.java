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

import Client.Launcher;
import Client.Logger;
import Client.QueueWindow;
import Client.Settings;
import Client.Util;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ReplayQueue {
  public static int currentIndex = 0;
  public static int lastIndex = 1;
  public static String currentReplayName = "";
  public static boolean skipped = false;
  public static ArrayList<File> queue = new ArrayList<File>();
  public static boolean foundBrokenReplay = false;

  // returns if it found valid replays in the directory chosen
  public static boolean replayFileSelectAdd() {
    JFileChooser j;
    try {
      j = new JFileChooser(Settings.REPLAY_BASE_PATH.get("custom"));
    } catch (Exception e) {
      j = new JFileChooser(System.getProperty("user.home"));
    }
    j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int response = j.showDialog(Game.getInstance().getApplet(), "Select Folder");

    File selection = j.getSelectedFile();
    if (selection != null && response != JFileChooser.CANCEL_OPTION) {
      List<File> selectionArr = new ArrayList<File>();
      selectionArr.add(selection);
      List<File> replays = Util.getAllReplays(selectionArr);
      if (replays.size() > 0) {
        ReplayQueue.queue.addAll(replays);
        QueueWindow.copyQueueToTable();
        Logger.Info(
            String.format(
                "@|cyan Added |@@|cyan,intensity_bold %d |@@|cyan replays to the queue. New size: |@@|cyan,intensity_bold %d |@",
                replays.size(), ReplayQueue.queue.size()));
        return true;
      } else {
        String replayFolderErrorMessage =
            "The replay folder you selected is not valid.<br/>"
                + "<br/>"
                + "You need to select a folder that contains the 'version.bin', 'in.bin.gz', and 'keys.bin' for your replay.<br/>"
                + "They're usually in a folder with your login username.";
        JPanel replayFolderErrorPanel = Util.createOptionMessagePanel(replayFolderErrorMessage);

        JOptionPane.showMessageDialog(
            Game.getInstance().getApplet(),
            replayFolderErrorPanel,
            "RSCPlus",
            JOptionPane.ERROR_MESSAGE,
            Launcher.scaled_icon_warn);
      }
    }
    return false;
  }

  public static DropTarget dropReplays =
      new DropTarget() {
        public synchronized void drop(DropTargetDropEvent evt) {
          try {
            if (evt.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
              evt.acceptDrop(DnDConstants.ACTION_LINK);
              List<File> droppedFiles =
                  (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
              List<File> replays = Util.getAllReplays(droppedFiles);
              Logger.Debug("Got some files");

              if (replays.size() == 0) {
                // no valid replays
                if (foundBrokenReplay) {
                  String brokenReplayErrorMessage =
                      "The replay you dragged onto the client has a keys.bin file which is empty.<br/>"
                          + "The data inside is encrypted without a key to decrypt it. :(<br/><br/>"
                          + "Some information might be able to be retrieved from this replay<br/>"
                          + "through reverse engineering, but basically it's broken.";
                  JPanel brokenReplayErrorPanel =
                      Util.createOptionMessagePanel(brokenReplayErrorMessage);

                  JOptionPane.showMessageDialog(
                      Game.getInstance().getApplet(),
                      brokenReplayErrorPanel,
                      "RSCPlus",
                      JOptionPane.ERROR_MESSAGE,
                      Launcher.scaled_icon_warn);
                } else {
                  boolean importedBank = false;
                  if (Client.show_bank && droppedFiles.size() == 1) {
                    if (droppedFiles.get(0).getName().contains("rscplus_bank")) {
                      // TODO: should use int return
                      String returnMes = Bank.importBankCsv(droppedFiles.get(0));
                      importedBank = returnMes.contains("Successfully imported");
                      Client.displayMessage(returnMes, Client.CHAT_QUEST);
                    }
                  }
                  if (!importedBank) {
                    String replayNotFoundErrorMessage =
                        "The folder you dropped onto the client is not a replay, nor does it contain replay folders.<br/>"
                            + "<br/>"
                            + "You need to drop a folder that contains a 'version.bin', 'in.bin.gz', and 'keys.bin' for the replay.";
                    JPanel replayNotFoundErrorPanel =
                        Util.createOptionMessagePanel(replayNotFoundErrorMessage);

                    // nothing that even looks like a replay was found
                    JOptionPane.showMessageDialog(
                        Game.getInstance().getApplet(),
                        replayNotFoundErrorPanel,
                        "RSCPlus",
                        JOptionPane.ERROR_MESSAGE,
                        Launcher.scaled_icon_warn);
                  }
                }
                return;
              } else {
                // at least 1 replay found
                ReplayQueue.queue.addAll(replays);
                QueueWindow.copyQueueToTable();
                Logger.Info(
                    String.format(
                        "Added %d replay%s to the queue. New size: %d",
                        replays.size(), replays.size() != 1 ? "s" : "", ReplayQueue.queue.size()));

                if (Client.state == Client.STATE_LOGIN) {
                  ReplayQueue.nextReplay();
                }
              }
            } else {
              // This can happen for example if a person drags rows from the replay queue window
              // into the main window
              Logger.Info(
                  "Whatever you just dragged into the main window, RSC+ doesn't know what to do with it.");
              Logger.Info("Please report this as a bug on GitHub if you believe it should work.");
            }
          } catch (Exception ex) {
            Logger.Error("Error in replay queue drop handler!");
            ex.printStackTrace();
          }
        }
      };

  public static void nextReplay() {
    if (currentIndex < queue.size()) {
      lastIndex = currentIndex;
      playFromQueue(currentIndex);
      currentIndex++;
    } else {
      Logger.Info("Reached end of queue!");
    }
  }

  public static void previousReplay() {
    if (currentIndex > 1) {
      lastIndex = currentIndex;
      currentIndex--;
      playFromQueue(currentIndex - 1);
    } else {
      Logger.Info("Reached beginning of queue!");
    }
  }

  public static void skipToReplay(int index) {
    skipped = true;
    lastIndex = currentIndex;
    currentIndex = index + 1;
    playFromQueue(index);
  }

  public static void removeReplay(int index) {
    Logger.Debug(String.format("removing %d aka %s", index, queue.get(index)));
    queue.remove(index);
  }

  public static int playIndex = -1;

  public static void processPlaybackQueue() {
    if (playIndex == -1) return;

    int index = playIndex;
    playIndex = -1;

    if (index < 0) {
      index = 0;
    }
    if (index > queue.size() - 1) {
      index = queue.size() - 1;
    }

    if (Replay.isPlaying) Client.runReplayCloseHook = true;

    currentReplayName = queue.get(index).getAbsolutePath();
    Logger.Info(
        "@|cyan Selected ("
            + (index + 1)
            + "): |@@|cyan,intensity_bold "
            + currentReplayName.replace(Settings.REPLAY_BASE_PATH.get("custom"), "")
            + "|@");
    Client.runReplayHook = true;
    QueueWindow.updatePlaying();
  }

  private static void playFromQueue(int index) {
    playIndex = index;
  }

  public static void clearQueue() {
    queue = new ArrayList<File>();
    lastIndex = -1;
    currentIndex = 0;
  }
}
