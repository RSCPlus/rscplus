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

import Client.Settings;

/** Handles adjusting the position and behavior of the in-game menu */
public class Menu {

  public static Object chat_menu;
  public static int chat_type1;
  public static int chat_type2;
  public static int chat_type3;
  public static int chat_input;
  public static int chat_selected;

  public static Object quest_menu;
  public static int quest_handle;

  public static Object friend_menu;
  public static int friend_handle;

  public static Object spell_menu;
  public static int spell_handle;

  /** Keeps track of the last spell book used. 0 for the Magic tab, 1 for the Prayer tab. */
  public static int spell_swap_idx = 0;

  public static int friends_swap_idx = 0;

  private static int[] spell_swap_scroll = {0, 0};

  /** Adjusts the in-game menu position based on the window size */
  public static void resize() {
    if (chat_menu != null && Reflection.menuX != null) {
      try {
        int[] y = (int[]) Reflection.menuY.get(chat_menu);
        y[chat_input] = Renderer.height_client - (334 - 324);
        y[chat_type1] = Renderer.height_client - (334 - 269);
        y[chat_type2] = Renderer.height_client - (334 - 269);
        y[chat_type3] = Renderer.height_client - (334 - 269);
        Reflection.menuY.set(chat_menu, y);

        int[] w = (int[]) Reflection.menuWidth.get(chat_menu);
        w[chat_input] = Renderer.width - 10;
        w[chat_type1] = Renderer.width - 10;
        w[chat_type2] = Renderer.width - 10;
        w[chat_type3] = Renderer.width - 10;
        Reflection.menuWidth.set(chat_menu, w);

        int[] x = (int[]) Reflection.menuX.get(quest_menu);
        x[quest_handle] = Renderer.width - 199;
        Reflection.menuX.set(quest_menu, x);

        x = (int[]) Reflection.menuX.get(friend_menu);
        x[friend_handle] = Renderer.width - 199;
        Reflection.menuX.set(friend_menu, x);

        x = (int[]) Reflection.menuX.get(spell_menu);
        x[spell_handle] = Renderer.width - 199;
        Reflection.menuX.set(spell_menu, x);
      } catch (Exception e) {
      }
    }
  }

  /**
   * Preserves the scroll bar values for the Magic and Prayer spell menus when switching between
   * them.
   *
   * @param menu the menu object
   * @return if the menu is not the spell menu
   */
  public static boolean switchList(Object menu) {
    if (menu == spell_menu) {
      try {
        int[] scroll = (int[]) Reflection.menuScroll.get(spell_menu);

        if (Settings.KEEP_SCROLLBAR_POS_MAGIC_PRAYER.get(Settings.currentProfile)
            && !Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) {
          // Swap scroll values
          spell_swap_scroll[spell_swap_idx] = scroll[spell_handle];
          spell_swap_idx ^= 1;
          scroll[spell_handle] = spell_swap_scroll[spell_swap_idx];
        } else {
          // Keep track of current view regardless of scrollbar position setting
          spell_swap_idx ^= 1;
          // Reset scroll position
          scroll[spell_handle] = 0;
        }

        Reflection.menuScroll.set(spell_menu, scroll);

        return false;
      } catch (Exception e) {
      }
    } else if (menu == friend_menu) {
      friends_swap_idx ^= 1;
    }

    return true;
  }
}
