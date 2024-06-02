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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Manages world population data fetching
 *
 * <p><b>Note:</b> The HTTP endpoint provided to the constructor MUST contain population data in a
 * single-line CSV format. Each data point in the list represents the current population for a
 * particular world.
 *
 * <p>Example: {@code 117,15,3,0,200} or simply {@code 125} for a single world
 *
 * <p>Code loosely adapted from the OpenRSC codebase:
 *
 * @see <a
 *     href="https://gitlab.com/open-runescape-classic/core/-/blob/ORSC-5.30.0/PC_Launcher/src/main/java/launcher/Utils/WorldPopulations.java">GitLab
 *     link</a>
 */
public class WorldPopulations {
  private static final int TIMEOUT = 3000;

  private final String populationUrl;
  private final int numWorlds;
  private final int period;
  private final String[] worldOnlineTexts;
  private final String threadName;

  private long lastPopCheck;

  private volatile boolean running = false;

  /**
   * Constructor for {@link WorldPopulations}
   *
   * @param populationUrl {@link String} representing the population endpoint
   * @param numWorlds Number of worlds represented by the endpoint data
   * @param period How often to poll the endpoint, in ms
   * @param threadName Name for the query thread
   */
  public WorldPopulations(String populationUrl, int numWorlds, int period, String threadName) {
    if (numWorlds < 1) {
      throw new IllegalArgumentException("Must support at least one world");
    }

    this.populationUrl = populationUrl;
    this.numWorlds = numWorlds;
    this.period = period;
    this.worldOnlineTexts = new String[numWorlds];
    this.lastPopCheck = 0;
    this.threadName = threadName;

    // Initialize population array
    for (int i = 0; i < numWorlds; i++) {
      this.worldOnlineTexts[i] = "Loading...";
    }
  }

  /** Schedules an API query if the last retrieval was greater than the configured period */
  public void update() {
    if (System.currentTimeMillis() < lastPopCheck + period) {
      return;
    }

    // Initialize fetching thread
    if (!running) {
      running = true;
      Thread t = new Thread(new FetchPopulationTask(), threadName);
      t.start();
    }
  }

  /** Force population check timer reset */
  public void resetPopCheck() {
    lastPopCheck = 0;
  }

  /**
   * Retrieve the current population text for a given world
   *
   * @param index World population index as it relates to the CSV data
   * @return {@link String} representing the world population status
   */
  public String getPopString(int index) {
    return worldOnlineTexts[index];
  }

  /** {@link Runnable} responsible for querying the world population data endpoint */
  private class FetchPopulationTask implements Runnable {
    @Override
    public void run() {
      lastPopCheck = System.currentTimeMillis();

      final URL url;
      final URLConnection con;

      try {
        url = new URL(populationUrl);
        con = url.openConnection();
        con.setConnectTimeout(TIMEOUT);
        con.setReadTimeout(TIMEOUT);

        con.addRequestProperty("User-Agent", generateUserAgent());
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
          String[] splitWorldTotals = null;

          String line;
          while ((line = br.readLine()) != null) {
            splitWorldTotals = line.split(",");
          }

          if (null == splitWorldTotals) {
            Arrays.fill(worldOnlineTexts, "");
          } else {
            for (int i = 0; i < numWorlds && i < splitWorldTotals.length; i++) {
              try {
                Integer.parseInt(splitWorldTotals[i]);
                splitWorldTotals[i] += " online";
              } catch (NumberFormatException nfe) {
                // No-op
              }

              worldOnlineTexts[i] = splitWorldTotals[i];
            }
          }
        }
      } catch (UnknownHostException uhe) {
        Arrays.fill(worldOnlineTexts, "You're offline");
      } catch (SocketTimeoutException ste) {
        Arrays.fill(worldOnlineTexts, "Socket timeout");
      } catch (IOException ioe) {
        if (ioe.toString().contains("Server returned HTTP response code: 521")) {
          Arrays.fill(worldOnlineTexts, "Webserver offline");
        } else {
          Arrays.fill(worldOnlineTexts, "Webserver offline?");
        }
      } finally {
        running = false;
      }
    }
  }

  /** Generates the User Agent string used when querying population counts */
  private static String generateUserAgent() {
    return "Mozilla/5.0 ("
        + System.getProperty("os.name")
        + "; "
        + System.getProperty("os.arch")
        + "; "
        + System.getProperty("os.version")
        + ") "
        + Launcher.appName
        + "/"
        + Util.formatVersion(Settings.VERSION_NUMBER);
  }
}
