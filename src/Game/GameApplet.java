/**
 *	rscplus
 *
 *	This file is part of rscplus.
 *
 *	rscplus is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	rscplus is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with rscplus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Authors: see <https://github.com/OrN/rscplus>
 */

package Game;

import java.io.File;
import java.net.URL;
import Client.Settings;

public class GameApplet {
	public static URL cacheURLHook(URL url) {
		String file = url.getFile();
		if (file.startsWith("/contentcrcs")) {
			file = "/contentcrcs";
		}
		
		File cache_file = new File(Settings.Dir.CACHE + file);
		
		if (cache_file.exists()) {
			try {
				return new URL(cache_file.toURI().toString());
			} catch (Exception e) {
				return url;
			}
		} else {
			// File doesn't exist in the cache, use the original method
			return url;
		}
	}
}
