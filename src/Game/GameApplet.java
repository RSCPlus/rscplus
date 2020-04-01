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
import java.net.URL;

public class GameApplet {
  public static URL cacheURLHook(URL url) {
    String file = url.getFile();
    if (file.startsWith("/contentcrcs")) {
      file = "/contentcrcs";
    }

    URL urlFile = Settings.getResource("/assets/content" + file);
    if (urlFile != null) return urlFile;

    return url;
  }
}
