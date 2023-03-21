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

import Client.NotificationsHandler;
import Client.Settings;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class defines items and provides a static method to patch item names as needed according to
 * {@link Settings#NAME_PATCH_TYPE}.
 */
public class Item {

  public static String[] item_name;
  public static String[] item_commands;
  public static int[] item_price;
  public static int[] item_stackable;
  public static List<Item> cool_items = new ArrayList<>();

  private static int name_patch_last = 0;

  public static int[] groundItemX;
  public static int[] groundItemY;
  public static int[] groundItemZ;
  public static int[] groundItemId;
  public static int groundItemCount;

  public int x;
  public int y;
  public int width;
  public int height;
  public int id;
  public long timestamp;

  public Item(int x, int y, int width, int height, int id) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.id = id;
  }

  public Item(int x, int y, int id, long timestamp) {
    this.x = x;
    this.y = y;
    this.id = id;
    this.timestamp = timestamp;
  }

  /** Patches item names as specified by {@link Settings#NAME_PATCH_TYPE}. */
  public static void patchItemNames() {
    int namePatchType = Settings.NAME_PATCH_TYPE.get(Settings.currentProfile);
    if (name_patch_last == namePatchType) {
      return;
    }
    ItemNamePatch.init();
    switch (namePatchType) {
      case 0:
        item_name = ItemNamePatch.item_name_original.clone();
        break;
      case 1:
        item_name = ItemNamePatch.item_name_patch1.clone();
        break;
      case 2:
        item_name = ItemNamePatch.item_name_patch2.clone();
        break;
      case 3:
        item_name = ItemNamePatch.item_name_patch3.clone();
        break;
    }
    name_patch_last = namePatchType;
  }

  /**
   * Patches discontinued edible item commands. Removes completely the option to eat or drink them
   */
  public static void patchItemCommands() {
    // ids of Half full wine jug, Pumpkin, Easter egg
    int[] edible_rare_item_ids = {246, 422, 677};
    String[] edible_rare_item_original_commands = {"Drink", "eat", "eat"};

    for (int i = 0; i < edible_rare_item_ids.length; i++) {
      if (Settings.COMMAND_PATCH_EDIBLE_RARES.get(Settings.currentProfile)) {
        item_commands[edible_rare_item_ids[i]] = "";
      } else {
        item_commands[edible_rare_item_ids[i]] = edible_rare_item_original_commands[i];
      }
    }

    // Disk of Returning patch, remove the ability to spin the disk.
    // Really not necessary, unless you are in Thordur's black hole and don't want to ever leave.
    // Keeping this setting since it existed in RSC+ before we knew whether or not it would consume
    // the disk.
    if (Settings.COMMAND_PATCH_DISK.get(Settings.currentProfile)) {
      item_commands[387] = "";
    } else {
      item_commands[387] = "spin";
    }
  }

  /** Patches quest only edible item commands. Swaps around the option to eat/drink */
  public static boolean shouldPatch(int index) {
    if (Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) return false;

    // ids of giant Carp, draynor malt whisky, Rock cake, nightshade
    int[] edible_quest_item_ids = {718, 584, 1061, 1086};
    boolean found = false;
    if (Settings.COMMAND_PATCH_QUEST.get(Settings.currentProfile)) {
      for (int i : edible_quest_item_ids) {
        if (index == i) {
          found = true;
          break;
        }
      }
      return found;
    } else {
      return false;
    }
  }

  public String getName() {
    return item_name[id];
  }

  // need to override this for Collections.frequency over in Renderer.java -> SHOW_ITEMINFO to count
  // duplicate-looking
  // items on ground correctly. without this, I believe it checks if location in memory is the same
  // for both objects.
  @Override
  public boolean equals(Object b) {
    if (b != null) {
      if (b.getClass() == this.getClass()) {
        Item bItem = (Item) b;
        return this.x == bItem.x && this.y == bItem.y && this.id == bItem.id;
      } else {
        return false;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    // This is an acceptable hash since it's fine if two unequal objects have the same hash
    // according to docs
    return this.x + this.y + this.width + this.height + this.id;
  }

  public static void checkForNewItems(int psize) {
    int offset = 1;
    while (offset < psize) {
      int remove = Client.lastIncomingBytes[offset++] & 0xFF;
      if (remove == 255) {
        int x = Client.worldX + (Client.lastIncomingBytes[offset++] & 0xFF);
        int y = Client.worldY + (Client.lastIncomingBytes[offset++] & 0xFF);
        removeFromCoolItems(x, y, -1);
      } else {
        int itemId = remove << 8 | Client.lastIncomingBytes[offset++] & 0xFF;
        int x = Client.worldX + (Client.lastIncomingBytes[offset++] & 0xFF);
        int y = Client.worldY + (Client.lastIncomingBytes[offset++] & 0xFF);
        // first bit on means removing a specific item, don't care in that case
        boolean addingAnItem = (remove & 0x80) >> 7 != 1;
        if (addingAnItem) {
          if (Renderer.stringIsWithinList(
              item_name[itemId], Settings.HIGHLIGHTED_ITEMS.get("custom"), true)) {
            cool_items.add(new Item(x, y, itemId, System.currentTimeMillis()));
          }
        } else {
          // TODO: this removes the oldest item, but not sure that's correct.
          removeFromCoolItems(x, y, itemId & 0x7FFF);
        }
      }
    }
  }

  private static void removeFromCoolItems(int x, int y, int itemId) {
    Iterator<Item> iterator = cool_items.iterator();
    Item coolItem;
    while (iterator.hasNext()) {
      coolItem = iterator.next();
      if (coolItem.x == x && coolItem.y == y) {
        if (itemId == -1 || itemId == coolItem.id) {
          iterator.remove();
          if (itemId != -1) {
            break;
          }
        }
      }
    }
  }

  public static void checkForImminentlyDespawningCoolItem() {
    try {
      Iterator<Item> iterator = cool_items.iterator();
      Item coolItem;
      while (iterator.hasNext()) {
        coolItem = iterator.next();
        boolean itemGoingStale =
            System.currentTimeMillis() - coolItem.timestamp
                > 1000 * Settings.HIGHLIGHTED_ITEM_NOTIF_VALUE.get(Settings.currentProfile);
        if (itemGoingStale) {
          // check if item still exists
          for (int i = 0; i < groundItemCount; i++) {
            int x = groundItemX[i] + Client.regionX;
            int y = groundItemY[i] + Client.regionY;
            if (x == coolItem.x && y == coolItem.y && groundItemId[i] == coolItem.id) {
              // "an" item with same item id & x & y coordinate still exists on the ground
              Client.displayMessage(
                  "@lre@[@gre@RSC+@lre@]: @red@Make sure to pick up your "
                      + item_name[coolItem.id]
                      + "!",
                  Client.CHAT_NONE);
              if (Settings.HIGHLIGHTED_ITEM_NOTIF_VALUE.get(Settings.currentProfile) > 0) {
                NotificationsHandler.notify(
                    NotificationsHandler.NotifType.HIGHLIGHTEDITEM,
                    "Highlighted Item Notification",
                    null,
                    item_name[coolItem.id]
                        + " has been on the ground for "
                        + Settings.HIGHLIGHTED_ITEM_NOTIF_VALUE.get(Settings.currentProfile)
                        + " second"
                        + (Settings.HIGHLIGHTED_ITEM_NOTIF_VALUE.get(Settings.currentProfile) == 1
                            ? "!"
                            : "s!"));
              } else {
                NotificationsHandler.notify(
                    NotificationsHandler.NotifType.HIGHLIGHTEDITEM,
                    "Highlighted Item Notification",
                    null,
                    item_name[coolItem.id] + " appeared!");
              }
              iterator.remove();
            }
          }
        }
      }
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
    }
  }
}
