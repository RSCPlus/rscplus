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

import Client.ServerExtensions;

/**
 * Utility methods for the OpenRSC Framework extension
 *
 * <p><b>NOTE:</b> This is ONLY intended to be used by <em>3rd party</em> private servers running
 * OpenRSC server code
 */
public class OpenRSCFrameworkUtils {

  /** Value to be used when determining whether the lag warning notification should be shown */
  public static int LAG_WARNING_THRESHOLD = 50;

  /**
   * Checks whether an {@link ServerExtensions#OPENRSC_FRAMEWORK} extension is currently enabled,
   * including the official OpenRSC servers which use exclusive {@link
   * ServerExtensions#OPENRSC_OFFICIAL} extension.
   *
   * @return {@code boolean} indicating whether an OpenRSC-compatible extension is currently active
   */
  public static boolean isOpenRSCCompatible() {
    return ServerExtensions.enabled(
        ServerExtensions.OPENRSC_FRAMEWORK, ServerExtensions.OPENRSC_OFFICIAL);
  }
}
