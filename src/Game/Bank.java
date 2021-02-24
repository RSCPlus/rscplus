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
import Client.Settings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class Bank {
  private static int[] bankItemsActual = new int[256];
  private static int[] bankItemCountsActual = new int[256];
  private static boolean[] bankItemsShown = new boolean[256];
  private static int bankNumberOfItemsActual = 0;

  private static int[] tmpNewBankItems = new int[256];
  private static int[] tmpNewBankItemsCount = new int[256];
  private static int tmpNewBankCount = 0;

  private static int[] sideBarDimensions = new int[8];
  private static boolean[] hoveringOverButton = new boolean[12];
  public static boolean disableUserButton = false;
  public static boolean[] buttonActive = new boolean[12];
  public static int[] buttonMode = new int[12];
  private static final int[] buttonModeLimits = // how many sort settings are enabled in one button
      {
    2, // inventory filter
    1, // melee filter
    2, // food/potions filter
    3, // tools/resources filter
    1, // magnifying glass
    1, // reset filter
    2, // release date sort
    2, // item value sort
    2, // alphabetical sort
    1, // "efficient" sort
    1, // user settings sort
    1 // reset sort
  };

  private static String bankValue = "";

  private static final int SHOW_BANK = 42;
  private static final int UPDATE_BANK_ITEM = 249;
  private static final int REMOVE_INVENTORY_SLOT = 123;
  private static boolean catchMeNextOpcode = false;

  static boolean processPacket(int opcode, int psize) {
    boolean processed = false;
    if (opcode == SHOW_BANK || opcode == UPDATE_BANK_ITEM) {
      // Take over reading the packets.
      // Always done no matter the user settings, so don't change those functions...!
      if (opcode == SHOW_BANK) {
        readShowBankPacket();
      } else { // UPDATE_BANK_ITEM
        readUpdateBankItemPacket();
      }

      doFilterSort();
      processed = true;

      if (Settings.SHOW_BANK_VALUE.get(Settings.currentProfile)) {
        // May have already calculated Bank Value in doFilterSort().
        if (!Settings.SORT_FILTER_BANK.get(Settings.currentProfile)) {
          calculateBankValue();
        }
      }
    }

    fixFilterWhenItemRemovedFromInventory(opcode);

    return processed;
  }

  private static void doFilterSort() {
    if (Settings.SORT_FILTER_BANK.get(Settings.currentProfile)) {
      boolean shouldWriteInventory = filterBank();
      sortBank();
      writeProcessedToClient();
      if (shouldWriteInventory) {
        writeInventoryToEndOfBank();
      }
      if (Settings.SHOW_BANK_VALUE.get(Settings.currentProfile)) {
        calculateBankValue();
      }
    } else {
      // This is why I have the comments to not mess with
      // readShowBankPacket() & readUpdateBankItemPacket()
      // When SORT_FILTER_BANK is off, we still handle the bank behaviour,
      // identical to the original client code's behaviour.
      Client.new_count_items_bank = bankNumberOfItemsActual;
      Client.new_bank_items = bankItemsActual.clone();
      Client.new_bank_items_count = bankItemCountsActual.clone();
      try {
        // This is currently equivalent to "resetSearch(); writeInventoryToEndOfBank();",
        // but it's better to call the original function
        // in case someone has changed those functions.
        Reflection.updateBankItems.invoke(Client.instance, -1129);
      } catch (Exception e) {
      }
    }
  }

  private static boolean filterBank() {
    boolean shouldWriteInventory = true;
    // place only those items matching the criteria
    for (int i = 0; i < bankNumberOfItemsActual; i++) {
      boolean shouldInclude = false;
      int filterCount = 0;
      if (buttonActive[1]) { // runes/weapons/armour
        switch (buttonMode[1]) {
          case 1:
            shouldInclude |=
                intInOrderedArray(bankItemsActual[i], BankSorters.runesWeaponsArmourSorted);
            break;
        }

        shouldWriteInventory = false;
        ++filterCount;
      }
      if (buttonActive[2]) { // food/potions
        switch (buttonMode[2]) {
          case 1:
            shouldInclude |= intInOrderedArray(bankItemsActual[i], BankSorters.foodPotionsSorted);
            break;
          case 2:
            shouldInclude |= intInOrderedArray(bankItemsActual[i], BankSorters.herblawSorted);
            break;
        }
        shouldWriteInventory = false;
        ++filterCount;
      }
      if (buttonActive[3]) { // resources/tools
        switch (buttonMode[3]) {
          case 1:
            shouldInclude |=
                intInOrderedArray(bankItemsActual[i], BankSorters.toolsResourcesSorted);
            break;
          case 2:
            shouldInclude |= intInOrderedArray(bankItemsActual[i], BankSorters.toolsSorted);
            break;
          case 3:
            shouldInclude |= intInOrderedArray(bankItemsActual[i], BankSorters.resourcesSorted);
            break;
        }
        shouldWriteInventory = false;
        ++filterCount;
      }
      if (buttonActive[4]) { // search term
        // TODO: could search for e.g. "salarin" and get items for that?
        // TODO: could add new filter that makes this a blacklist instead of a whitelist
        if (!Settings.SEARCH_BANK_WORD.get("custom").equals("")) {
          String[] searchTerms = Settings.SEARCH_BANK_WORD.get("custom").split(",");
          for (String searchTerm : searchTerms) {
            if (!searchTerm.equals("")) {
              shouldInclude |=
                  Item.item_name[bankItemsActual[i]]
                      .toLowerCase()
                      .contains(searchTerm.trim().toLowerCase());
            }
          }
          shouldWriteInventory = false;
          ++filterCount;
        }
      }
      if (buttonActive[0]) { // inventory
        switch (buttonMode[0]) {
          case 1:
            for (int invIdx = 0; invIdx < Client.inventory_count; invIdx++) {
              if (bankItemsActual[i] == Client.inventory_items[invIdx]) {
                shouldInclude = true;
                break;
              }
            }
            break;
          case 2:
            // TODO: this is a bit unpredictable & could be made better.
            shouldInclude |= intInArray(bankItemsActual[i], Client.inventory_items);
            break;
        }
        // set back to true if this filter is active & another filter turned it off
        shouldWriteInventory = true;

        ++filterCount;
      }

      bankItemsShown[i] = (shouldInclude || filterCount == 0) && bankItemCountsActual[i] > 0;
    }
    return shouldWriteInventory;
  }

  private static void sortBank() {
    // This function prepares tmpNewBankItems to be copied to the client.
    // It applies filtered items in the order desired.
    int idx;
    for (idx = 0; idx < tmpNewBankItems.length; idx++) {
      tmpNewBankItems[idx] = 0;
      tmpNewBankItemsCount[idx] = 0;
    }
    idx = 0;
    boolean sortedBank = false;

    if (buttonActive[6]) {
      switch (buttonMode[6]) {
        case 1:
          // release date sort
          // TODO: this is technically just item ID sort.
          // A few exceptions do exist to the Item ID = Release Date rule.
          for (int itemId = 0; itemId < 1290; itemId++) {
            for (int i = 0; i < bankNumberOfItemsActual; i++) {
              if (bankItemsActual[i] == itemId && bankItemsShown[i]) {
                tmpNewBankItems[idx] = bankItemsActual[i];
                tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
                ++idx;
                break;
              }
            }
          }
          break;
        case 2:
          // item id sort rev
          for (int itemId = 1289; itemId >= 0; itemId--) {
            for (int i = 0; i < bankNumberOfItemsActual; i++) {
              if (bankItemsActual[i] == itemId && bankItemsShown[i]) {
                tmpNewBankItems[idx] = bankItemsActual[i];
                tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
                ++idx;
                break;
              }
            }
          }
          break;
      }
      sortedBank = true;
    } else if (buttonActive[7]) {
      switch (buttonMode[7]) {
        case 1:
          // alch value sort
          for (int valueIdx = 0; valueIdx < BankSorters.mostValuableItems.length; valueIdx++) {
            for (int i = 0; i < bankNumberOfItemsActual; i++) {
              if (bankItemsActual[i] == BankSorters.mostValuableItems[valueIdx]
                  && bankItemsShown[i]) {
                tmpNewBankItems[idx] = bankItemsActual[i];
                tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
                ++idx;
                break;
              }
            }
          }
          break;
        case 2:
          // alch value sort rev
          for (int valueIdx = BankSorters.mostValuableItems.length - 1; valueIdx >= 0; valueIdx--) {
            for (int i = 0; i < bankNumberOfItemsActual; i++) {
              if (bankItemsActual[i] == BankSorters.mostValuableItems[valueIdx]
                  && bankItemsShown[i]) {
                tmpNewBankItems[idx] = bankItemsActual[i];
                tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
                ++idx;
                break;
              }
            }
          }
          break;
      }
      sortedBank = true;
    } else if (buttonActive[8]) {
      switch (buttonMode[8]) {
        case 1:
          // Alphabetical sort
          for (int valueIdx = 0; valueIdx < BankSorters.itemsAlphabetical.length; valueIdx++) {
            for (int i = 0; i < bankNumberOfItemsActual; i++) {
              if (bankItemsActual[i] == BankSorters.itemsAlphabetical[valueIdx]
                  && bankItemsShown[i]) {
                tmpNewBankItems[idx] = bankItemsActual[i];
                tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
                ++idx;
                break;
              }
            }
          }
          break;
        case 2:
          // Reverse Alphabetical sort
          for (int valueIdx = BankSorters.itemsAlphabetical.length - 1; valueIdx >= 0; valueIdx--) {
            for (int i = 0; i < bankNumberOfItemsActual; i++) {
              if (bankItemsActual[i] == BankSorters.itemsAlphabetical[valueIdx]
                  && bankItemsShown[i]) {
                tmpNewBankItems[idx] = bankItemsActual[i];
                tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
                ++idx;
                break;
              }
            }
          }
          break;
      }
      sortedBank = true;
    } else if (buttonActive[9]) {
      boolean[] addedToSortedBank = new boolean[1290];
      // Efficiency sort
      // TODO: depends on user settings which efficient layout they want typeSortNice first
      for (int valueIdx = 0; valueIdx < BankSorters.typeSortNice.length; valueIdx++) {
        for (int i = 0; i < bankNumberOfItemsActual; i++) {
          if (bankItemsActual[i] == BankSorters.typeSortNice[valueIdx]
              && bankItemsShown[i]
              && !addedToSortedBank[BankSorters.typeSortNice[valueIdx]]) {
            addedToSortedBank[BankSorters.typeSortNice[valueIdx]] = true;
            tmpNewBankItems[idx] = bankItemsActual[i];
            tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
            ++idx;
            break;
          }
        }
      }

      // add any items that exist in bank and don't have a defined order
      for (int itemId = 0; itemId < 1290; itemId++) {
        for (int i = 0; i < bankNumberOfItemsActual; i++) {
          if (bankItemsActual[i] == itemId && bankItemsShown[i] && !addedToSortedBank[itemId]) {
            tmpNewBankItems[idx] = bankItemsActual[i];
            tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
            ++idx;
            break;
          }
        }
      }

      sortedBank = true;
    } else if (buttonActive[10]) {
      // User defined order
      boolean[] addedToSortedBank = new boolean[1290];
      Integer[] userBankSort = Settings.USER_BANK_SORT.get(Client.player_name);
      if (userBankSort == null) {
        importBankCsv(null);
        userBankSort = Settings.USER_BANK_SORT.get(Client.player_name);
      }
      if (userBankSort != null) {
        for (int i = 0; i < userBankSort.length && userBankSort[i] != null; i++) {
          for (int j = 0; j < bankNumberOfItemsActual; j++) {
            if (bankItemsActual[j] == userBankSort[i]
                && bankItemsShown[j]
                && !addedToSortedBank[userBankSort[i]]) {
              tmpNewBankItems[idx] = bankItemsActual[j];
              tmpNewBankItemsCount[idx] = bankItemCountsActual[j];
              addedToSortedBank[userBankSort[i]] = true;
              ++idx;
              break;
            }
          }
        }

        // add any items that exist in bank and don't have a defined order
        for (int itemId = 0; itemId < 1290; itemId++) {
          for (int i = 0; i < bankNumberOfItemsActual; i++) {
            if (bankItemsActual[i] == itemId && bankItemsShown[i] && !addedToSortedBank[itemId]) {
              tmpNewBankItems[idx] = bankItemsActual[i];
              tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
              ++idx;
              break;
            }
          }
        }

        sortedBank = true;
      } else {
        Client.displayMessage(
            "@lre@You don't have any user bank sort settings defined.", Client.CHAT_QUEST);
        Client.displayMessage(
            "@whi@To use this button, open the settings with @mag@<ctrl-o> @whi@and then navigate to the @lre@\"Bank\"@whi@ tab.",
            Client.CHAT_QUEST);
        disableUserButton = true;
      }
    }

    // still apply filter even if no sort is applied
    if (!sortedBank) {
      for (int i = 0; i < bankNumberOfItemsActual; i++) {
        if (bankItemsShown[i]) {
          tmpNewBankItems[idx] = bankItemsActual[i];
          tmpNewBankItemsCount[idx] = bankItemCountsActual[i];
          idx++;
        }
      }
    }
    tmpNewBankCount = idx;
  }

  private static void writeInventoryToEndOfBank() {
    for (int inventoryIdx = 0;
        inventoryIdx < Client.inventory_count && Client.count_items_bank < Client.bank_items_max;
        ++inventoryIdx) {

      int inventoryItemId = Client.inventory_items[inventoryIdx];
      // Logger.Info(inventoryIdx + ". Looking for " +
      // Item.item_name[Client.inventory_items[inventoryIdx]]);
      boolean bankHasItem = false;

      for (int bankIdx = 0; bankIdx < Client.count_items_bank; ++bankIdx) {
        if (inventoryItemId == Client.bank_items[bankIdx]) {
          bankHasItem = true;
          break;
        }
      }

      if (!bankHasItem) {
        Client.bank_items[Client.count_items_bank] = inventoryItemId;
        Client.bank_items_count[Client.count_items_bank] = 0;
        ++Client.count_items_bank;
      }
    }
  }

  private static void writeProcessedToClient() {
    for (int i = 0; i < Client.bank_items.length; i++) {
      Client.bank_items[i] = tmpNewBankItems[i];
      Client.bank_items_count[i] = tmpNewBankItemsCount[i];
      Client.new_bank_items[i] = tmpNewBankItems[i];
      Client.new_bank_items_count[i] = tmpNewBankItemsCount[i];
    }
    Client.count_items_bank = tmpNewBankCount;
    Client.new_count_items_bank = tmpNewBankCount;
  }

  private static void calculateBankValue() {
    int value = 0;
    for (int idx = 0; idx < Client.new_count_items_bank; idx++) {
      int itemId = Client.new_bank_items[idx];
      if (itemId > 1289) {
        Logger.Info("itemId " + itemId + " at position " + idx + " @|red fwehhhh|@");
      } else {
        value += BankSorters.itemValues[itemId] * Client.new_bank_items_count[idx];
      }
    }
    bankValue = String.format("Bank Value: (%d gp)", value);
  }

  private static void resetSearch() {
    Client.new_bank_items = bankItemsActual.clone();
    Client.new_bank_items_count = bankItemCountsActual.clone();
    Client.bank_items = Client.new_bank_items.clone();
    Client.bank_items_count = Client.new_bank_items_count.clone();
    Client.count_items_bank = Client.new_count_items_bank = bankNumberOfItemsActual;
  }

  public static String exportBank() {
    if (!Client.show_bank) {
      // technically not true, just what we are enforcing.
      // must have actually opened bank at least one time to export.
      return "You must have your bank open to export!";
    }

    StringBuilder csvData = new StringBuilder();
    try {
      for (int i = 0; i < Client.count_items_bank; i++) {
        csvData.append(
            String.format(
                "%d%s", Client.bank_items[i], i == Client.count_items_bank - 1 ? "\n" : ","));
      }
      for (int i = 0; i < Client.count_items_bank; i++) {
        csvData.append(
            String.format(
                "%d%s", Client.bank_items_count[i], i == Client.count_items_bank - 1 ? "\n" : ","));
      }
    } catch (Exception e) {
      return "Error reading bank info";
    }

    if (Replay.isPlaying) {
      String replayFolder = ReplayQueue.queue.get(ReplayQueue.currentIndex - 1).getAbsolutePath();
      Object[] metadata = Replay.readMetadata(replayFolder);
      csvData.append((long) metadata[1]).append(","); // replay date modified (accurate enough)
      csvData.append("\"");
      csvData.append(replayFolder);
      csvData.append("\"");
    } else {
      csvData.append(System.currentTimeMillis()).append(","); // current time

      int world = Settings.WORLD.get(Settings.currentProfile);
      String curWorldURL = Settings.WORLD_URLS.get(world);
      int port = Settings.WORLD_PORTS.getOrDefault(world, Replay.DEFAULT_PORT);
      csvData.append("\"");
      csvData.append(curWorldURL);
      csvData.append(":");
      csvData.append(port);
      csvData.append("\"");
    }
    // original player name, even if imported somewhere else
    csvData.append(",");
    csvData.append(Client.player_name);

    File file = new File(Settings.Dir.BANK + "/" + Client.player_name + "_rscplus_bank.csv");
    int attempts = 1;
    while (file.exists()) {
      if (attempts > 256) {
        return "Too many files exported for this user";
      }
      file =
          new File(
              Settings.Dir.BANK + "/" + Client.player_name + "_rscplus_bank_" + attempts + ".csv");
      attempts++;
    }

    try {
      DataOutputStream os = new DataOutputStream(new FileOutputStream(file));
      os.write(csvData.toString().getBytes());
      os.close();
      return "Exported to " + file.getAbsolutePath();
    } catch (Exception e) {
      return "Unable to write file";
    }
  }

  public static String importBank() {
    if (Client.player_name.equals("")) {
      return "Not logged in yet...";
    }

    JFileChooser j;
    try {
      j = new JFileChooser(Settings.Dir.BANK);
    } catch (Exception e) {
      return "Could not open Settings.Dir.BANK directory!";
    }

    j.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    FileFilter bankFiles =
        new FileFilter() {
          public boolean accept(File file) {
            return file.getName().endsWith(".csv") && file.getName().contains("rscplus_bank");
          }

          @Override
          public String getDescription() {
            return "rscplus bank files";
          }
        };
    j.setFileFilter(bankFiles);
    int response = j.showDialog(Game.getInstance().getApplet(), "Select bank csv");

    File selection = j.getSelectedFile();
    if (selection != null && response != JFileChooser.CANCEL_OPTION) {
      List<File> selectionArr = new ArrayList<File>();
      selectionArr.add(selection);
      if (selectionArr.size() == 1) {
        return importBankCsv(selection);
      } else {
        return "Please select exactly 1 file.";
      }
    }
    if (response == JFileChooser.CANCEL_OPTION) {
      return "User cancelled";
    }
    return "Error, unable to import bank for some unknown reason.";
  }

  public static String importBankCsv(File file) {
    boolean needsCopy = true;
    String returnMe;
    if (file == null) {
      if (Client.player_name.equals("")) {
        returnMe =
            "importBankCsv called when player_name is not yet set!"; // should not be able to happen
        Logger.Error(returnMe);
        return returnMe;
      }
      file = new File(Settings.Dir.BANK + "/" + Client.player_name + "_rscplus_bank.csv");
      needsCopy = false;
    }
    if (file.exists()) {
      Integer[] sortOrder = new Integer[256];
      try {
        BufferedReader csvReader = new BufferedReader(new FileReader(file));
        String[] csvOrder = csvReader.readLine().split(",");
        csvReader.close();
        for (int i = 0; i < csvOrder.length; i++) {
          try {
            sortOrder[i] = Integer.parseInt(csvOrder[i]);
          } catch (Exception e) {
            if (!csvOrder[i].equals("blank")) {
              returnMe = "Non-numeric csv value in " + file.getName();
              Logger.Warn(returnMe);
              return returnMe;
            }
          }
        }
        Settings.USER_BANK_SORT.put(Client.player_name, sortOrder);
        if (needsCopy) {
          File moveTo =
              new File(Settings.Dir.BANK + "/" + Client.player_name + "_rscplus_bank.csv");
          if (moveTo.exists()) {
            int attempts = 1;
            File moveOldTo =
                new File(
                    Settings.Dir.BANK
                        + "/"
                        + Client.player_name
                        + "_rscplus_bank_old_"
                        + attempts
                        + ".csv");
            while (moveOldTo.exists()) {
              if (attempts > 256) {
                returnMe =
                    "Imported for this session, but error saving settings permanently because too many files exist";
                Logger.Warn(returnMe);
                return returnMe;
              }
              moveOldTo =
                  new File(
                      Settings.Dir.BANK
                          + "/"
                          + Client.player_name
                          + "_rscplus_bank_old_"
                          + attempts
                          + ".csv");
              attempts++;
            }
            boolean moved = moveTo.renameTo(moveOldTo);
            if (!moved) {
              returnMe =
                  "Imported for this session, but error saving settings permanently because we could not safely move the old rscplus_bank.csv file";
              Logger.Warn(returnMe);
              return returnMe;
            }
            moveTo =
                new File(
                    Settings.Dir.BANK
                        + "/"
                        + Client.player_name
                        + "_rscplus_bank.csv"); // ready to move there now
          }

          try {
            Files.copy(file.toPath(), moveTo.toPath());
          } catch (Exception e) {
            returnMe =
                "Imported for this session, but error saving settings permanently due to copy error";
            Logger.Warn(returnMe);
            return returnMe;
          }
        }
        if (Client.show_bank && buttonActive[10]) {
          doFilterSort();
        }
        return "Successfully imported " + file.getName() + "!"; // the only success...
      } catch (Exception e) {
        returnMe = "Error reading file " + file.getName();
        Logger.Warn(returnMe);
        return returnMe;
      }
    } else {
      // either not defined yet or file was deleted
      returnMe = "No user sort settings for current user";
      return returnMe;
    }
  }

  // Draws extra buttons on the side of the bank to control filtering
  public static void drawBankAugmentations(Graphics2D g2) {
    if (Client.show_bank) {
      if (Settings.SORT_FILTER_BANK.get(Settings.currentProfile)) {
        // existing bank interface dimensions
        int bankWidth = 408;
        int bankHeight = 271;
        int screenWidthAuthentic = 512; // TODO: renderer.width, once bank is resizeable
        int sideSpacing = ((screenWidthAuthentic - bankWidth) / 2);

        // existing bank interface box colour
        Renderer.setAlpha(g2, 0.625f); // 160/256
        Color bankBackgroundColour = new Color(0x989898);
        Color bankItemActiveColour = new Color(0xcc0000);

        g2.setColor(bankBackgroundColour); // bank box colour

        // box dimensions
        int heightFromTop = 3 + 16 + 9;
        int width = sideSpacing - 4;
        int height = bankHeight - heightFromTop;
        int buttonSpacing = 3;
        int buttonHeight = 32;
        int buttonWidth = 48;

        // top left of new background
        // TODO: adjust once bank is resizable
        int x = bankWidth + sideSpacing;
        int y = heightFromTop + 12; // bank authentically starts render 12 px down

        // draw a background for our buttons to go on
        g2.fillRect(x, y, width, height);
        sideBarDimensions[0] = x;
        sideBarDimensions[1] = y;
        sideBarDimensions[2] = width;
        sideBarDimensions[3] = height;

        // Draw Filter panel
        x -= 4;
        y += 8;
        Renderer.setAlpha(g2, 1.0f);
        g2.drawImage(
            Launcher.icon_filter_text.getImage(), x + 3 + (buttonWidth - 28) / 2, y, 28, 10, null);
        y += 6 + 10;

        int i = 0;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        if (buttonMode[0] != 2) {
          // might possibly want to draw this 3 pixels to the left, to line up with the other icon,
          // but maybe not.
          g2.drawImage(
              Launcher.icon_satchel.getImage(), x + (buttonWidth - 32) / 2, y, 32, 32, null);
        } else {
          g2.drawImage(
              Launcher.icon_satchel_time.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        }
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + buttonSpacing;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        g2.drawImage(
            Launcher.icon_runes_weapons_armour.getImage(),
            x + (buttonWidth - 48) / 2,
            y,
            48,
            32,
            null);
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + buttonSpacing;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        if (buttonMode[2] != 2) {
          g2.drawImage(
              Launcher.icon_lobster_potion.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        } else {
          g2.drawImage(
              Launcher.icon_herblaw.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        }
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + buttonSpacing;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        if (buttonMode[3] == 2) {
          g2.drawImage(Launcher.icon_tools.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        } else if (buttonMode[3] == 3) {
          g2.drawImage(
              Launcher.icon_resources.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        } else {
          g2.drawImage(
              Launcher.icon_resources_tools.getImage(),
              x + (buttonWidth - 48) / 2,
              y,
              48,
              32,
              null);
        }

        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + buttonSpacing;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        g2.drawImage(
            Launcher.icon_banksearch.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + 7;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x + 8, y, 40, 32);
        Renderer.setAlpha(g2, 1.0f);
        g2.drawImage(
            Launcher.icon_filter_reset.getImage(), x + 4 + (buttonWidth - 40) / 2, y, 40, 32, null);
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + 40
                && MouseHandler.y > y
                && MouseHandler.y < y + 32);

        // Draw Sort panel
        x -= bankWidth + sideSpacing - 8;
        y = heightFromTop + 12;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        g2.setColor(bankBackgroundColour);
        g2.fillRect(x, y, width, height);
        sideBarDimensions[4] = x;
        sideBarDimensions[5] = y;
        sideBarDimensions[6] = width;
        sideBarDimensions[7] = height;

        x += 4;
        y += 8;
        Renderer.setAlpha(g2, 1.0f);
        g2.drawImage(
            Launcher.icon_sort_text.getImage(), x + 3 + (buttonWidth - 28) / 2, y, 24, 10, null);
        y += 6 + 10;

        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        if (buttonMode[6] != 2) {
          g2.drawImage(
              Launcher.icon_release.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        } else {
          g2.drawImage(
              Launcher.icon_release_desc.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        }
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + buttonSpacing;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        if (buttonMode[7] != 2) {
          g2.drawImage(
              Launcher.icon_item_value.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        } else {
          g2.drawImage(
              Launcher.icon_item_value_rev.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        }
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + buttonSpacing;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        if (buttonMode[8] != 2) {
          g2.drawImage(
              Launcher.icon_alphabetical.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        } else {
          g2.drawImage(
              Launcher.icon_alphabetical_rev.getImage(),
              x + (buttonWidth - 48) / 2,
              y,
              48,
              32,
              null);
        }
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + buttonSpacing;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        g2.drawImage(
            Launcher.icon_efficient.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + buttonSpacing;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, buttonWidth, buttonHeight);
        Renderer.setAlpha(g2, 1.0f);
        g2.drawImage(
            Launcher.icon_user_custom.getImage(), x + (buttonWidth - 48) / 2, y, 48, 32, null);
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + buttonWidth
                && MouseHandler.y > y
                && MouseHandler.y < y + buttonHeight);

        y += buttonHeight + 7;
        Renderer.setAlpha(g2, 0.625f); // 160/256
        if (buttonActive[i]) {
          g2.setColor(bankItemActiveColour);
        } else {
          g2.setColor(bankBackgroundColour);
        }
        g2.fillRect(x, y, 40, 32);
        Renderer.setAlpha(g2, 1.0f);
        g2.drawImage(
            Launcher.icon_filter_reset.getImage(), x - 4 + (buttonWidth - 40) / 2, y, 40, 32, null);
        hoveringOverButton[i++] =
            (MouseHandler.x >= x
                && MouseHandler.x <= x + 40
                && MouseHandler.y > y
                && MouseHandler.y < y + 32);

        // Handle button presses
        if (MouseHandler.mouseClicked && shouldConsume()) {
          for (i = 0; i < 12; i++) {
            if (hoveringOverButton[i]) {
              if (i != 5 && i != 11) {
                ++buttonMode[i];
                if (buttonMode[i] > buttonModeLimits[i]) {
                  buttonMode[i] = 0;
                  buttonActive[i] = false;
                } else {
                  buttonActive[i] = true;
                }
              } else {
                buttonActive[i] = !buttonActive[i];
              }
              if (buttonActive[i]) {
                if (i >= 6) {
                  // only one sort can be active at once, disable others when one is clicked
                  for (int j = 6; j < 11; j++) {
                    if (j != i) {
                      buttonMode[j] = 0;
                      buttonActive[j] = false;
                    }
                  }
                } else {
                  // multiple filters can work together additively, only disable if reset is pressed
                  if (i == 5) {
                    for (int j = 0; j < 5; j++) {
                      buttonMode[j] = 0;
                      buttonActive[j] = false;
                    }
                  }
                }
              }
            }
          }

          if (MouseHandler.mouseClicked && shouldConsume()) {
            if (buttonActive[5] || buttonActive[11]) {
              resetSearch();
            }
          }

          doFilterSort();
          Settings.save(); // to save buttonMode array
        }
      }
      if (Settings.SHOW_BANK_VALUE.get(Settings.currentProfile)) {
        Renderer.drawShadowText(
            g2,
            bankValue,
            269, // TODO: this will have to be adjusted once the bank is resizable
            21,
            Renderer.color_yellow,
            true);
      }
    }
  }

  public static boolean shouldConsume() {
    if (Client.show_bank && Settings.SORT_FILTER_BANK.get(Settings.currentProfile)) {
      boolean hoveringOverFilter =
          (MouseHandler.x >= sideBarDimensions[0]
              && MouseHandler.x <= sideBarDimensions[0] + sideBarDimensions[2]
              && MouseHandler.y > sideBarDimensions[1]
              && MouseHandler.y < sideBarDimensions[1] + sideBarDimensions[3]);
      boolean hoveringOverSort =
          (MouseHandler.x >= sideBarDimensions[4]
              && MouseHandler.x <= sideBarDimensions[4] + sideBarDimensions[6]
              && MouseHandler.y > sideBarDimensions[5]
              && MouseHandler.y < sideBarDimensions[5] + sideBarDimensions[7]);
      return hoveringOverSort || hoveringOverFilter;
    } else {
      return false;
    }
  }

  /**
   * Entry point, since the same command array is used from Client.processClientCommand, index
   * starts with 1 (validated before)
   *
   * @param cmdArray the command array used on Client.processClientCommand
   * @param help display help on command
   */
  public static void search(String[] cmdArray, boolean help) {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < cmdArray.length; i++) {
      if (cmdArray[i].trim().equals("")) continue;
      sb.append(cmdArray[i].trim().toLowerCase());
      if (i < cmdArray.length - 1) sb.append(" ");
    }
    bankSearch(sb.toString(), help);
  }

  /**
   * Filters out current bank state to match items with given keyword
   *
   * @param search The complete clean search query to do the search
   * @param help display help on command
   */
  private static void bankSearch(String search, boolean help) {
    if (search.trim().equals("") || help) {
      Client.displayMessage("@whi@::banksearch is a searchable bank mode", Client.CHAT_QUEST);
      Client.displayMessage(
          "@whi@Type \"::banksearch [aString]\" to search banked items with query string aString",
          Client.CHAT_QUEST);
      Client.displayMessage("@whi@Bank is updated to show only matched items.", Client.CHAT_QUEST);
      Client.displayMessage("@whi@The command stores the query string used", Client.CHAT_QUEST);
      Client.displayMessage("@whi@To exit the mode, speak to the banker again.", Client.CHAT_QUEST);
    } else {
      // not in bank, display notice
      if (!Client.show_bank) {
        Client.displayMessage(
            "@whi@::banksearch is only available when bank interface is open", Client.CHAT_QUEST);
      } else {
        if (search.trim().equals("reset")) {
          resetSearch();
        } else {
          Settings.SEARCH_BANK_WORD.put("custom", search);
          Settings.save();
          doFilterSort();
        }
      }
    }
  }

  /**
   * Bank search without modifying positions, just lists out banked elements and where they are
   * located
   *
   * @param search The complete clean search query to do the search
   * @param help display help on command
   */
  public static void query(String search, boolean help) {
    if (search.trim().equals("") || help) {
      Client.displayMessage("@whi@::querybank is a top-10 based system.", Client.CHAT_QUEST);
      Client.displayMessage(
          "@whi@Type \"::querybank [aString]\" to search banked items with query string aString",
          Client.CHAT_QUEST);
      Client.displayMessage("@whi@The command stores the query string used", Client.CHAT_QUEST);
      Client.displayMessage(
          "@whi@You can go back in 'Quest history' to read pages that have disappeared.",
          Client.CHAT_QUEST);
    } else {
      // not in bank, display notice
      if (!Client.show_bank) {
        Client.displayMessage(
            "@whi@::querybank is only available when bank interface is open", Client.CHAT_QUEST);
      } else {
        // overwrite query string on local config
        Settings.SEARCH_BANK_WORD.put("custom", search);
        Settings.save();
        int page, row, col, tmp;
        Client.displayMessage("@whi@" + "Queried bank with '" + search + "'", Client.CHAT_QUEST);
        for (int i = 0; i < Client.bank_items.length; i++) {
          if (Client.bank_items_count[i] == 0) break;
          if (Item.item_name[Client.bank_items[i]].toLowerCase().contains(search.toLowerCase())) {
            page = i / 48;
            tmp = i - 48 * page;
            page++;
            row = (tmp / 8) + 1;
            col = (tmp % 8) + 1;
            Client.displayMessage(
                "@whi@"
                    + " "
                    + Item.item_name[Client.bank_items[i]]
                    + " ("
                    + pluralize(Client.bank_items_count[i])
                    + ")"
                    + " at Page "
                    + page
                    + ", Row "
                    + row
                    + ", Column "
                    + col,
                Client.CHAT_QUEST);
          }
        }
      }
    }
  }

  private static boolean intInArray(int id, int[] array) {
    for (int i = 0; i < array.length; i++) {
      if (id == array[i]) {
        return true;
      }
    }
    return false;
  }

  private static boolean intInOrderedArray(int id, int[] array) {
    for (int i = 0; i < array.length && i <= array[i]; i++) {
      if (id == array[i]) {
        return true;
      }
    }
    return false;
  }

  public static String pluralize(int count) {
    if (count == 1) return count + " pc";
    else return count + " pcs";
  }

  public static void loadButtonMode(String modes) {
    Logger.Debug("reading from: " + modes);
    for (int i = 0; i < modes.length(); i++) {
      buttonMode[i] = modes.charAt(i) - 48; // ascii character 48 is "0"

      if (buttonMode[i] < 0) {
        buttonMode[i] = 0;
      }
      if (buttonMode[i] > buttonModeLimits[i]) {
        buttonMode[i] = buttonModeLimits[i];
      }

      buttonActive[i] = buttonMode[i] > 0;
    }
  }

  public static String getButtonModeString() {
    char[] buttonModeChar = new char[buttonMode.length];
    for (int i = 0; i < buttonMode.length; i++) {
      buttonModeChar[i] = (char) (buttonMode[i] + 48); // ascii character 48 is "0"
    }
    return new String(buttonModeChar);
  }

  // Needed because packet order removes item from inventory AFTER updating bank. (authentic)
  // without this, "items in inventory" erroneously contains our recently removed item.
  private static void fixFilterWhenItemRemovedFromInventory(int opcode) {
    if (catchMeNextOpcode) {
      doFilterSort();
      catchMeNextOpcode = false;
    }
    if (Client.show_bank && opcode == REMOVE_INVENTORY_SLOT) {
      // Don't want to handle reading the contents of this packet right now.
      // It's almost as good to just set a flag that we should re-evaluate the bank
      // after a point where we know this opcode has finished processing.
      catchMeNextOpcode = true;
    }
  }

  // ** Don't mess with this function! It's perfect and any deviation is an inauthenticity bug! **
  private static void readShowBankPacket() {
    // Reads server opcode 42.
    // Same behaviour as original, but now in its own function, and write to a safe array!
    try {
      Reflection.showBank.set(Client.instance, true);
      Client.show_bank = true;
    } catch (Exception e) {
      // error could not set showBank to true for some unknown reason
      Client.show_bank = false;
    }
    bankNumberOfItemsActual = StreamUtil.getUnsignedByte(Client.packetsIncoming) & 0xFF;
    Client.bank_items_max = StreamUtil.getUnsignedByte(Client.packetsIncoming) & 0xFF;

    for (int index = 0; index < bankNumberOfItemsActual; ++index) {
      bankItemsActual[index] = StreamUtil.getUnsignedShort(Client.packetsIncoming) & 0xFFFF;
      bankItemCountsActual[index] = StreamUtil.getUnsignedInt3(Client.packetsIncoming);
    }
  }

  // ** Don't mess with this function! It's perfect and any deviation is an inauthenticity bug! **
  private static void readUpdateBankItemPacket() {
    // Reads server opcode 249.
    // Same behaviour as original, but now in its own function, and write to a safe array!
    int slot = StreamUtil.getUnsignedByte(Client.packetsIncoming) & 0xFF;
    int item = StreamUtil.getUnsignedShort(Client.packetsIncoming) & 0xFFFF;
    int itemCount = StreamUtil.getUnsignedInt3(Client.packetsIncoming);
    if (itemCount == 0) {
      --bankNumberOfItemsActual;

      for (int index = slot; index < bankNumberOfItemsActual; ++index) {
        bankItemsActual[index] = bankItemsActual[index + 1];
        bankItemCountsActual[index] = bankItemCountsActual[index + 1];
      }
    } else {
      bankItemsActual[slot] = item;
      bankItemCountsActual[slot] = itemCount;
      if (slot >= bankNumberOfItemsActual) {
        bankNumberOfItemsActual = slot + 1;
      }
    }
  }
}
