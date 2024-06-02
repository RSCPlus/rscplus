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
package Client.Extensions;

import Client.Settings;
import Client.WorldPopulations;
import Game.Client;

/** Utility methods for the official OpenRSC extension, to be used by the hosted OpenRSC servers */
public class OpenRSCOfficialUtils {
  public static final WorldType PRESERVATION = WorldType.from("Preservation");
  public static final WorldType URANIUM = WorldType.from("Uranium");

  public static final String largeIconPath = "/assets/extensions/orsc/orsc_64.png";
  public static final String iconPath = "/assets/extensions/orsc/orsc_32.png";
  public static final String smallIconPath = "/assets/extensions/orsc/orsc_16.png";

  private static final int POP_CHECK_PERIOD = 10000;
  private static final int POP_WORLDS_SUPPORTED = 6;
  private static final int PRESERVATION_POP_IDX = 0;
  private static final int URANIUM_POP_IDX = 2;

  private static WorldPopulations populations = null;

  /** Check if the world is a "Preservation" type */
  public static boolean isPreservationType(String worldId) {
    return PRESERVATION.matchesWorldId(worldId);
  }

  /** Check if the world is a "Uranium" type */
  public static boolean isUraniumType(String worldId) {
    return URANIUM.matchesWorldId(worldId);
  }

  /**
   * Constructs a custom welcome message for OpenRSC worlds
   *
   * @return {@link String} containing the custom world message
   *     <p>TODO: OpenRSC: Refactor once multiple worlds per game type are supported
   */
  public static String getWelcomeMessage() {
    String openRSCWorldId = Settings.WORLD_ID.get(Settings.WORLD.get(Settings.currentProfile));
    if (isPreservationType(openRSCWorldId)) {
      return "Welcome to RSC Preservation";
    } else if (isUraniumType(openRSCWorldId)) {
      return "Welcome to RSC Uranium";
    } else {
      // Default welcome message
      return Client.strings == null ? "" : Client.strings[237];
    }
  }

  /** @return The static {@link WorldPopulations} instance for the official OpenRSC servers */
  public static WorldPopulations getPopulations() {
    if (populations == null) {
      populations =
          new WorldPopulations(
              "https://rsc.vet/onlinelookup",
              POP_WORLDS_SUPPORTED,
              POP_CHECK_PERIOD,
              "openrsc-pop-check");
    }

    return populations;
  }

  /** @return The current {@link #PRESERVATION} population text */
  public static String getPreservationPop() {
    return populations.getPopString(PRESERVATION_POP_IDX);
  }

  /** @return The current {@link #URANIUM} population text */
  public static String getUraniumPop() {
    return populations.getPopString(URANIUM_POP_IDX);
  }
}
