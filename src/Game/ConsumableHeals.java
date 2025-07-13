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

import java.util.HashMap;
import java.util.Map;

/** Cache for consumable item healing values */
public class ConsumableHeals {
  private static final Map<Integer, String> consumableDB = new HashMap<>();

  static {
    /* "Eat" commands */
    consumableDB.put(18, "1");
    consumableDB.put(132, "3");
    consumableDB.put(138, "4");
    consumableDB.put(179, "2");
    consumableDB.put(210, "?"); // Kebab
    consumableDB.put(228, "1");
    consumableDB.put(249, "2");
    consumableDB.put(257, "5");
    consumableDB.put(258, "3");
    consumableDB.put(259, "4");
    consumableDB.put(261, "4");
    consumableDB.put(262, "3");
    consumableDB.put(263, "5");
    consumableDB.put(319, "2");
    consumableDB.put(320, "2");
    consumableDB.put(325, "11");
    consumableDB.put(326, "7");
    consumableDB.put(327, "8");
    consumableDB.put(328, "7");
    consumableDB.put(329, "8");
    consumableDB.put(330, "4");
    consumableDB.put(332, "5");
    consumableDB.put(333, "4");
    consumableDB.put(334, "5");
    consumableDB.put(335, "4");
    consumableDB.put(336, "5");
    consumableDB.put(337, "3");
    consumableDB.put(346, "9");
    consumableDB.put(350, "3");
    consumableDB.put(352, "1");
    consumableDB.put(355, "4");
    consumableDB.put(357, "9");
    consumableDB.put(359, "7");
    consumableDB.put(362, "5");
    consumableDB.put(364, "8");
    consumableDB.put(367, "10");
    consumableDB.put(370, "14");
    consumableDB.put(373, "12");
    consumableDB.put(422, "14");
    consumableDB.put(546, "20");
    consumableDB.put(551, "7");
    consumableDB.put(553, "6");
    consumableDB.put(555, "13");
    consumableDB.put(590, "11");
    consumableDB.put(677, "14");
    consumableDB.put(709, "19");
    consumableDB.put(718, "6");
    consumableDB.put(749, "2");
    consumableDB.put(750, "10");
    consumableDB.put(751, "10");
    consumableDB.put(765, "2");
    consumableDB.put(801, "0");
    consumableDB.put(855, "2");
    consumableDB.put(856, "2");
    consumableDB.put(857, "2");
    consumableDB.put(858, "2");
    consumableDB.put(859, "2");
    consumableDB.put(860, "2");
    consumableDB.put(861, "2");
    consumableDB.put(862, "2");
    consumableDB.put(863, "2");
    consumableDB.put(864, "2");
    consumableDB.put(865, "2");
    consumableDB.put(871, "2");
    consumableDB.put(873, "1");
    consumableDB.put(885, "2");
    consumableDB.put(896, "3");
    consumableDB.put(897, "2");
    consumableDB.put(900, "2");
    consumableDB.put(901, "11");
    consumableDB.put(902, "11");
    consumableDB.put(903, "11");
    consumableDB.put(904, "11");
    consumableDB.put(905, "11");
    consumableDB.put(906, "11");
    consumableDB.put(907, "15");
    consumableDB.put(908, "12");
    consumableDB.put(909, "12");
    consumableDB.put(910, "15");
    consumableDB.put(911, "7");
    consumableDB.put(912, "8");
    consumableDB.put(913, "8");
    consumableDB.put(914, "7");
    consumableDB.put(923, "19");
    consumableDB.put(924, "19");
    consumableDB.put(936, "2");
    consumableDB.put(944, "11");
    consumableDB.put(945, "11");
    consumableDB.put(946, "11");
    consumableDB.put(947, "11");
    consumableDB.put(948, "11");
    consumableDB.put(949, "11");
    consumableDB.put(950, "15");
    consumableDB.put(951, "12");
    consumableDB.put(952, "12");
    consumableDB.put(953, "11");
    consumableDB.put(954, "7");
    consumableDB.put(955, "8");
    consumableDB.put(956, "8");
    consumableDB.put(957, "7");
    consumableDB.put(1061, "4");
    consumableDB.put(1102, "19");
    consumableDB.put(1103, "3");
    consumableDB.put(1191, "20");
    consumableDB.put(1193, "20");
    consumableDB.put(1245, "4");
    consumableDB.put(1269, "8");

    /* "Drink" commands */
    consumableDB.put(142, "11");
    consumableDB.put(180, "0");
    consumableDB.put(193, "1");
    consumableDB.put(246, "5");
    consumableDB.put(267, "1");
    consumableDB.put(268, "1");
    consumableDB.put(269, "1");
    consumableDB.put(598, "3");
    consumableDB.put(737, "?"); // Poison chalice
    consumableDB.put(770, "4");
    consumableDB.put(829, "1");
    consumableDB.put(830, "1");
    consumableDB.put(853, "0");
    consumableDB.put(854, "0");
    consumableDB.put(866, "8");
    consumableDB.put(867, "0");
    consumableDB.put(868, "4");
    consumableDB.put(869, "4");
    consumableDB.put(870, "4");
    consumableDB.put(872, "5");
    consumableDB.put(874, "5");
    consumableDB.put(875, "5");
    consumableDB.put(876, "4");
    consumableDB.put(877, "5");
    consumableDB.put(878, "5");
    consumableDB.put(879, "9");
    consumableDB.put(937, "8");
    consumableDB.put(938, "5");
    consumableDB.put(939, "5");
    consumableDB.put(940, "8");
    consumableDB.put(941, "5");
    consumableDB.put(942, "5");
    consumableDB.put(943, "5");

    // Note: Not a lot of data is available about gnome cocktails,
    // so values may not be authentic
  }

  /** @return Healing display value */
  public static String getValue(int id) {
    return consumableDB.get(id);
  }

  /** @return Damaging value for nightshade (1086) */
  public static String getNightShadeValue() {
    int amount = (int) (Client.current_level[Client.SKILL_HP] * 0.166666666D) + 14;
    return String.valueOf(-1 * amount);
  }

  /** @return Damaging value for Zamorak potion (963,964,965) */
  public static String getZamorakPotValue() {
    int amount = (int) (Client.current_level[Client.SKILL_HP] * 0.1);
    return String.valueOf(-1 * amount);
  }

  /** @return Healing value for tea (739) */
  public static String getTeaValue() {
    int amount = (int) (Client.base_level[Client.SKILL_HP] * 0.02) + 2;
    return String.valueOf(amount);
  }
}
