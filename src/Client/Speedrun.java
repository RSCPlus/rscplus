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

import Game.Client;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Speedrun {
  static int totalTicks = 0;
  static ArrayList<Long> startTimes = new ArrayList<>();
  static ArrayList<Long> endTimes = new ArrayList<>();
  static boolean active = false;
  static boolean endTheRUNNN = false;
  static String finishedSpeedrun = "unknown";

  static final int REASON_NEED_LOAD = -1;
  static final int REASON_NO_PREVIOUS_FILES = 0;
  static final int REASON_INVALID_FILE = 1;
  static final int REASON_LAST_SPEEDRUN_FINISHED = 2;
  static final int REASON_ALL_OK = 3;
  static final int REASON_DIFFERENT_ACCOUNT = 4;
  static final int REASON_UNEXPECTED = 5;
  static int loadResult = REASON_NEED_LOAD;

  // IDs must not overlap between coordinateGoal & messageGoal.
  public enum coordinateGoal {
    TUTORIAL_ISLAND(0, "Tutorial Island", 120, 648), // initial lumbridge spawns
    CHAMPIONS_GUILD(2, "Champion's Guild", 150, 554); // just inside the champion's guild door

    coordinateGoal(int id, String name, int xCoord, int yCoord) {
      this.id = id;
      this.name = name;
      this.xCoord = xCoord;
      this.yCoord = yCoord;
    }

    public int id;
    public String name;
    public int xCoord;
    public int yCoord;
  }

  public enum messageGoal {
    BLACK_KNIGHTS_FORTRESS(
        1,
        "Black Knight's Fortress",
        "Well done.You have completed the Black Knights fortress quest"),
    DRAGON_SLAYER(3, "Dragon Slayer", "Well done you have completed the dragon slayer quest");

    messageGoal(int id, String name, String systemMessage) {
      this.id = id;
      this.name = name;
      this.systemMessage = systemMessage;
    }

    public int id;
    public String name;
    public String systemMessage;
  }

  static int goalsDefined = messageGoal.values().length + coordinateGoal.values().length;
  static int[] completionTicks = new int[goalsDefined];
  static long[] completionTimes = new long[goalsDefined];

  public static void checkCoordinateCompletions() {
    if (Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) {
      for (coordinateGoal goal : coordinateGoal.values()) {
        if (completionTimes[goal.id] == 0) {
          if (goal.xCoord == Client.worldX && goal.yCoord == Client.worldY) {
            completionTicks[goal.id] = totalTicks;
            completionTimes[goal.id] = System.currentTimeMillis();
            printGoalCompletion(goal.name, completionTicks[goal.id], completionTimes[goal.id]);
          }
        }
      }
    }
  }

  public static void checkMessageCompletions(String message) {
    if (Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) {
      for (messageGoal goal : messageGoal.values()) {
        if (completionTimes[goal.id] == 0) {
          if (goal.systemMessage == message) {
            completionTicks[goal.id] = totalTicks;
            completionTimes[goal.id] = System.currentTimeMillis();
            printGoalCompletion(goal.name, completionTicks[goal.id], completionTimes[goal.id]);
          }
        }
      }
    }
  }

  public static void checkAndBeginSpeedrun() {
    long timeCalled = System.currentTimeMillis();
    if (Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) {
      if (!active) {
        if (loadResult != REASON_NEED_LOAD) {
          return;
        }
        loadResult = loadSpeedrun();

        switch (loadResult) {
          case REASON_ALL_OK:
            Client.displayMessage("Successfully loaded previous run data.", Client.CHAT_QUEST);
            beginSpeedrun(timeCalled);
            break;
          case REASON_LAST_SPEEDRUN_FINISHED:
            Client.displayMessage(
                String.format(
                    "Last speedrun (%s) finished. Starting new speedrun at %d",
                    finishedSpeedrun, timeCalled),
                Client.CHAT_QUEST);
            beginSpeedrun(timeCalled);
            break;
          case REASON_NO_PREVIOUS_FILES:
            beginSpeedrun(timeCalled);
            break;
          case REASON_DIFFERENT_ACCOUNT:
            resetSpeedrun();
            Client.displayMessage(
                "@red@Not logged into your existing speedrun's account, but speedrunner mode is active.",
                Client.CHAT_QUEST);
            Client.displayMessage("@ora@No new speedrun can begin.", Client.CHAT_QUEST);
            Client.displayMessage(
                "@yel@Rectify this by taking one of the following actions:", Client.CHAT_QUEST);
            Client.displayMessage(
                "1.) Delete your existing speedrun attempt from " + Settings.Dir.SPEEDRUN,
                Client.CHAT_QUEST);
            Client.displayMessage(
                "2.) Log back in to your speedrun account and finish the run.", Client.CHAT_QUEST);
            Client.displayMessage(
                "3.) Turn off speedrun mode in the config settings.", Client.CHAT_QUEST);
            Client.displayMessage(
                "4.) Ignore this message & just play but without speedrun timer features.",
                Client.CHAT_QUEST);
            Client.displayMessage(
                "@yel@Use <CTRL-O> or right click the tray icon to enter Settings.",
                Client.CHAT_QUEST);
            break;
          case REASON_INVALID_FILE:
            resetSpeedrun();
            Client.displayMessage(
                "@red@There is some garbage file in your speedruns folder. Please remove it.",
                Client.CHAT_QUEST);
            break;
          case REASON_UNEXPECTED:
          default:
            resetSpeedrun();
            Client.displayMessage(
                "@red@Exited from loadSpeedrun() in a strange way. Not starting speedrun.",
                Client.CHAT_QUEST);
            break;
        }
      }
    }
  }

  private static void beginSpeedrun(long timeCalled) {
    active = true;
    addStartTime(timeCalled);
  }

  private static void resetSpeedrun() {
    startTimes.clear();
    endTimes.clear();
    completionTicks = new int[goalsDefined];
    completionTimes = new long[goalsDefined];
  }

  public static void addStartTime(long startTime) {
    startTimes.add(startTime);
    Client.displayMessage(
        String.format("Starting segment at millisecond %d!!", startTime), Client.CHAT_QUEST);
  }

  public static void endTheRun() {
    Settings.SPEEDRUNNER_MODE_ACTIVE.put(Settings.currentProfile, false);
    endTheRUNNN = true;
    saveAndQuitSpeedrun();
  }

  public static void addEndTime(long endTime) {
    endTimes.add(endTime);
    Client.displayMessage(
        String.format("Ending segment at millisecond %d!!", endTime), Client.CHAT_QUEST);
    printTimeSinceLastSegment(true);
    printTotalTicks();
  }

  public static void printGoalCompletion(String name, int ticks, long time) {
    Client.displayMessage(
        String.format(
            "Completed %s in %d ticks! (%d millis since last segment)",
            name, ticks, calcTimeSinceLastSegment(time)),
        Client.CHAT_QUEST);
  }

  public static void printTimeSinceLastSegment(boolean referenceEndTimes) {
    long timeDelta;
    if (referenceEndTimes) {
      timeDelta = endTimes.get(endTimes.size() - 1) - startTimes.get(startTimes.size() - 1);
    } else {
      timeDelta = calcTimeSinceLastSegment(System.currentTimeMillis());
    }
    Client.displayMessage(
        String.format("Millis since last time segment: %d", timeDelta), Client.CHAT_QUEST);
  }

  public static long calcTimeSinceLastSegment(long millis) {
    int startSize = startTimes.size();
    if (startSize >= 1) return millis - startTimes.get(startSize - 1);
    else return millis;
  }

  public static void incrementTicks() {
    if (active) totalTicks++;
  }

  public static void printTotalTicks() {
    Client.displayMessage(
        String.format("Total ticks spent in speedrun: %d", totalTicks), Client.CHAT_QUEST);
  }

  public static void saveAndQuitSpeedrun() {
    loadResult = REASON_NEED_LOAD;
    if (active) {
      active = false;
      addEndTime(System.currentTimeMillis());
      try {
        DataOutputStream speedrunData =
            new DataOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(
                        new File(
                            Settings.Dir.SPEEDRUN
                                + String.format(
                                    "/%d.%s.bin",
                                    startTimes.get(0),
                                    Client.username_login.replaceAll("[^a-zA-Z0-9]", ""))))));

        // Header information
        speedrunData.writeInt(
            1533546026); // unix timestamp that RSC was taken offline (2018-08-06 9:00:26 UTC)
        speedrunData.writeBoolean(!endTheRUNNN);
        if (endTheRUNNN) {
          endTheRUNNN = false;
        }
        speedrunData.writeInt(totalTicks);
        speedrunData.writeInt(startTimes.size());
        speedrunData.writeInt(endTimes.size());
        speedrunData.writeInt(goalsDefined);
        speedrunData.writeInt(Client.username_login.length());
        for (int i = 0; i < 32; i++) speedrunData.writeInt(0); // reserved

        // Speedrun data
        for (long time : startTimes) {
          speedrunData.writeLong(time);
        }
        for (long time : endTimes) {
          speedrunData.writeLong(time);
        }
        for (int i = 0; i < goalsDefined; i++) {
          speedrunData.writeInt(completionTicks[i]);
          speedrunData.writeLong(completionTimes[i]);
        }
        speedrunData.writeChars(Client.username_login);

        speedrunData.flush();
        speedrunData.close();
      } catch (IOException e) {
        Logger.Error("@|red Couldn't write speedrun save file!|@");
        Client.displayMessage("@red@Couldn't write speedrun save file!", Client.CHAT_QUEST);
        Client.displayMessage("Here are your times:", Client.CHAT_QUEST); // TODO
      }
    }
  }

  public static int loadSpeedrun() {
    File[] fList = new File(Settings.Dir.SPEEDRUN).listFiles();
    if (fList == null || fList.length == 0) return REASON_NO_PREVIOUS_FILES;
    Arrays.sort(fList);
    File newestData = fList[fList.length - 1];
    // This file can be "found" again later because the filename is based on startTimes[0], which
    // will remain constant.
    Logger.Info("Attempting to load speedrun at " + newestData.getAbsolutePath());
    try {
      DataInputStream speedrunData =
          new DataInputStream(new BufferedInputStream(new FileInputStream(newestData)));

      // Check if the file being read is in fact a speedrun save file
      if (speedrunData.readInt() != 1533546026) {
        return REASON_INVALID_FILE;
      }
      // Check if the speedrun being read already finished (no reason to load)
      if (!speedrunData.readBoolean()) {
        finishedSpeedrun = newestData.getName();
        return REASON_LAST_SPEEDRUN_FINISHED;
      }
      totalTicks = speedrunData.readInt();
      int startTimesSize = speedrunData.readInt();
      int endTimesSize = speedrunData.readInt();
      int goalsDefined = speedrunData.readInt();
      int usernameLength = speedrunData.readInt();
      for (int i = 0; i < 32; i++) speedrunData.readInt(); // reserved

      startTimes.clear();
      for (int i = 0; i < startTimesSize; i++) {
        startTimes.add(speedrunData.readLong());
      }

      endTimes.clear();
      for (int i = 0; i < endTimesSize; i++) {
        endTimes.add(speedrunData.readLong());
      }
      for (int i = 0; i < goalsDefined; i++) {
        completionTicks[i] = speedrunData.readInt();
        completionTimes[i] = speedrunData.readLong();
      }
      char[] usernameArr = new char[usernameLength];
      for (int i = 0; i < usernameLength; i++) usernameArr[i] = speedrunData.readChar();
      String loadedUsername = new String(usernameArr);

      if (Client.username_login.equalsIgnoreCase(loadedUsername)) {
        return REASON_ALL_OK;
      } else {
        return REASON_DIFFERENT_ACCOUNT;
      }
    } catch (IOException e) {
      e.printStackTrace();
      Client.displayMessage("@red@Unable to parse speedrun save file!", Client.CHAT_QUEST);
      Logger.Warn("Unable to parse speedrun save file!");
    }
    return REASON_UNEXPECTED;
  }
}
