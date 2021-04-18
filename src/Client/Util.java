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

import Game.Replay;
import Game.ReplayQueue;
import java.awt.Point;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

/** A miscellaneous utility class */
public class Util {

  /** Stores the world populations in the array indices corresponding to the world numbers */
  static int[] worldPopArray;

  /** The last time the world populations were checked */
  static long lastPopCheck = 0;

  public static final float ANGLE_WEST = 0;
  public static final float ANGLE_SOUTH = 90;
  public static final float ANGLE_EAST = 180;
  public static final float ANGLE_NORTH = 270;

  public static String angleNames[] = {"W", "SW", "S", "SE", "E", "NE", "N", "NW"};

  static boolean hasXdgOpen = false;

  public static final int[] xpLevelTable =
      new int[] {
        0,
        0,
        83,
        174,
        276,
        388,
        512,
        650,
        801,
        969,
        1154,
        1358,
        1584,
        1833,
        2107,
        2411,
        2746,
        3115,
        3523,
        3973,
        4470,
        5018,
        5624,
        6291,
        7028,
        7842,
        8740,
        9730,
        10824,
        12031,
        13363,
        14833,
        16456,
        18247,
        20224,
        22406,
        24815,
        27473,
        30408,
        33648,
        37224,
        41171,
        45529,
        50339,
        55649,
        61512,
        67983,
        75127,
        83014,
        91721,
        101333,
        111945,
        123660,
        136594,
        150872,
        166636,
        184040,
        203254,
        224466,
        247886,
        273742,
        302288,
        333804,
        368599,
        407015,
        449428,
        496254,
        547953,
        605032,
        668051,
        737627,
        814445,
        899257,
        992895,
        1096278,
        1210421,
        1336443,
        1475581,
        1629200,
        1798808,
        1986068,
        2192818,
        2421087,
        2673114,
        2951373,
        3258594,
        3597792,
        3972294,
        4385776,
        4842295,
        5346332,
        5902831,
        6517253,
        7195629,
        7944614,
        8771558,
        9684577,
        10692629,
        11805606,
        13034431,
        14391160,
        15889109,
        17542976,
        19368992,
        21385073,
        23611006,
        26068632,
        28782069,
        31777943,
        35085654,
        38737661,
        42769801,
        47221641,
        52136869,
        57563718,
        63555443,
        70170840,
        77474828,
        85539082,
        94442737,
        104273167,
        115126824,
        127110256,
        140341024,
        154948976,
        171077440,
        188884736,
        208545568,
        230252880,
        254219712,
        280681216,
        309897088,
        342154016,
        377768576,
        417090208,
        460504800,
        508438432,
        561361408,
        619793088,
        684306880,
        755535936,
        834179200,
        921008320,
        1016875520,
        1122721536,
        1239584896,
        1368612480,
        1511070592,
        1668356992,
        1842015232,
        2033749632
      };

  private Util() {
    // Empty private constructor to prevent instantiation.
  }

  public static float lengthdir_x(float dist, float angle) {
    return dist * (float) Math.cos(Math.toRadians(angle));
  }

  public static float lengthdir_y(float dist, float angle) {
    return dist * (float) -Math.sin(Math.toRadians(angle));
  }

  public static float lerp(float a, float b, float c) {
    return a + c * (b - a);
  }

  public static float getAngle(Point source, Point target) {
    float angle = (float) Math.toDegrees(Math.atan2(target.y - source.y, target.x - source.x));

    if (angle < 0) angle += 360;

    return angle;
  }

  public static String readString(InputStream inputStream) throws IOException {
    ByteArrayOutputStream into = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    for (int n; 0 < (n = inputStream.read(buf)); ) {
      into.write(buf, 0, n);
    }
    into.close();
    return new String(into.toByteArray(), "UTF-8"); // Or whatever encoding
  }

  public static String getAngleDirectionName(float angle) {
    return angleNames[getAngleIndex(angle)];
  }

  public static int getAngleIndex(float angle) {
    int index = (int) ((angle / (360.0f / 8.0f)) + 0.5f);
    return index % 8;
  }

  public static String findDirectoryReverse(String name) {
    String ret = Settings.Dir.JAR;

    for (int i = 0; i < 8; i++) {
      File file = new File(ret + name);
      if (file.exists() && file.isDirectory()) return ret;
      ret += "/..";
    }

    return Settings.Dir.JAR;
  }

  public static boolean isMacOS() {
    String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    return (os.contains("mac") || os.contains("darwin"));
  }

  public static String formatTimeDuration(int millis, int endMillis) {
    int seconds = (millis / 1000) % 60;
    int minutes = (millis / 1000 / 60) % 60;
    int hours = millis / 1000 / 60 / 60;

    int endHours = endMillis / 1000 / 60 / 60;

    String ret = "";
    if (endHours != 0)
      ret += ((hours < 10) ? "0" + Integer.toString(hours) : Integer.toString(hours)) + ":";
    ret += ((minutes < 10) ? "0" + Integer.toString(minutes) : Integer.toString(minutes)) + ":";
    ret += ((seconds < 10) ? "0" + Integer.toString(seconds) : Integer.toString(seconds));
    return ret;
  }

  public static String formatTimeLongShort(int fiftythsOfSecond) {
    int total_centiseconds = fiftythsOfSecond * 2; // 50fps * 2; converts to hundreths of a second
    int leftover_centiseconds = total_centiseconds % 100;
    int total_seconds = (total_centiseconds - leftover_centiseconds) / 100;
    int leftover_seconds = total_seconds % 60;
    int total_minutes = (total_seconds - leftover_seconds) / 60;
    int leftover_minutes = total_minutes % 60;
    int total_hours = (total_minutes - leftover_minutes) / 60;
    int leftover_hours = total_hours % 24;
    int total_days = (total_hours - leftover_hours) / 24;

    if (total_days > 0) {
      return (String.format(
          "%d day%s %d:%02d:%02d",
          total_days,
          total_days == 1 ? "" : "s",
          leftover_hours,
          leftover_minutes,
          leftover_seconds));
    } else if (total_hours > 0) {
      return (String.format("%d:%02d:%02d", total_hours, leftover_minutes, leftover_seconds));
    } else if (leftover_minutes > 0) {
      return (String.format("%d:%02d", leftover_minutes, leftover_seconds));
    } else {
      return (String.format("%d.%02d•", leftover_seconds, leftover_centiseconds));
    }
  }

  /**
   * Gets the CRC32 of a given file name.
   *
   * @param fname Path to the file
   * @return CRC32 of the file data
   */
  public static long fileGetCRC32(String fname) {
    try {
      byte[] data = Files.readAllBytes(new File(fname).toPath());
      CRC32 crc = new CRC32();
      crc.update(data);
      return crc.getValue();
    } catch (Exception e) {
    }

    return -1;
  }

  /**
   * Gets the URL to the RSC jav_config.ws file for a given world.
   *
   * @param world the world
   * @return the URL to the jav_config.ws file
   */
  /* historical, unused
  public static String makeWorldURL(int world) {
    return "http://classic" + world + ".runescape.com/jav_config.ws";
  }
  */

  /**
   * Creates a directory relative to codebase, which is typically either the jar or location of the
   * package folders.
   *
   * @param name the name of the folder to create
   */
  public static void makeDirectory(String name) {
    File dir = new File(name);
    if (dir.isFile()) dir.delete();
    if (!dir.exists()) dir.mkdir();
  }

  /**
   * Converts a byte array into a String of 2 digit hexadecimal numbers.
   *
   * @param data a byte array to convert
   * @return a String of hexadecimal numbers
   * @see #hexStringByte
   */
  public static String byteHexString(byte[] data) {
    String ret = "";
    for (int i = 0; i < data.length; i++) ret += String.format("%02x", data[i]);
    return ret;
  }

  /**
   * Converts a String of 2 digit hexadecimal numbers into a byte array.
   *
   * @param data a String to convert
   * @return a byte array
   * @see #byteHexString
   */
  public static byte[] hexStringByte(String data) {
    byte[] bytes = new byte[data.length() / 2];
    int j;
    for (int i = 0; i < bytes.length; i++) {
      j = i * 2;
      String hex_pair = data.substring(j, j + 2);
      byte b = (byte) (Integer.parseInt(hex_pair, 16) & 0xFF);
      bytes[i] = b;
    }
    return bytes;
  }

  /**
   * Gets the populations of the worlds.
   *
   * @return an array containing the population of each world
   */
  public static int[] getPop() {
    if (worldPopArray == null) worldPopArray = new int[6];

    return worldPopArray; // RIP RSC, 2001-01-04 to 2018-08-06

    /* historical, we no longer even try to talk to jagex
     * this is what it used to look like when we did.

    if (System.currentTimeMillis() < lastPopCheck + 60000) return worldPopArray;

    lastPopCheck = System.currentTimeMillis();

    URL url;
    URLConnection con;
    InputStream is = null;
    BufferedReader br;

    try {
      url = new URL("http://www.runescape.com/classicapplet/playclassic.ws");
      con = url.openConnection();
      con.setConnectTimeout(3000);
      con.setReadTimeout(3000);
      is = con.getInputStream();
      br = new BufferedReader(new InputStreamReader(is));
    } catch (IOException ioe) {
      try {
        if (is != null) is.close();
      } catch (IOException e) {
      }
      return worldPopArray;
    }

    String line = null;

    try {
      while ((line = br.readLine()) != null) {
        if (line.contains("<span class='classic-worlds__name'>Classic ")) {
          String[] worldNumLine = line.split(" ");
          int worldNum = Integer.parseInt(worldNumLine[2].split("<")[0]);
          br.readLine();
          String[] worldPopLine = br.readLine().split(" ");
          int worldPop = Integer.parseInt(worldPopLine[0].trim());
          worldPopArray[worldNum] = worldPop;
        }
      }
    } catch (IOException ioe) {
      return worldPopArray;
    } catch (NumberFormatException nfe) {
      return worldPopArray;
    }

    try {
      br.close();
      is.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return worldPopArray;
    */
  }

  /**
   * Polyfill for Java 8 `String.join`
   *
   * <p>Convert an arraylist of strings to a single string, where each element is separated by some
   * deliminator.
   *
   * @param delim The string to use when combining elements
   * @param list The list to combine
   * @return The string of the arraylist
   */
  public static String joinAsString(String delim, ArrayList<String> list) {
    StringBuilder sb = new StringBuilder();
    for (String s : list) {
      sb.append(s);
      sb.append(delim);
    }
    return sb.toString();
  }

  public static List<File> getAllReplays(List<File> folderInputs) {
    ReplayQueue.foundBrokenReplay = false;
    List<File> potentialReplayFolders = new ArrayList<File>();
    List<File> replayFolders = new ArrayList<File>();

    for (File folderInputFile : folderInputs) {
      if (folderInputFile != null) {
        String folderInput = folderInputFile.getAbsolutePath();
        listf(folderInput, potentialReplayFolders);
        if (Replay.isValid(folderInput)) {
          Replay.checkAndGenerateMetadata(folderInput);
          replayFolders.add(new File(folderInput));
        } else {
          if (Replay.isBroken(folderInput)) {
            ReplayQueue.foundBrokenReplay = true;
          }
        }
      }
    }
    for (File file : potentialReplayFolders) {
      if (Replay.isValid(file.getAbsolutePath())) {
        Replay.checkAndGenerateMetadata(file.getAbsolutePath());
        replayFolders.add(file);
      } else {
        if (Replay.isBroken(file.getAbsolutePath())) {
          ReplayQueue.foundBrokenReplay = true;
        }
      }
    }

    Collections.sort(
        replayFolders,
        new Comparator<File>() {
          @Override
          public int compare(File file2, File file1) {
            // sorts alphabetically
            return file2.compareTo(file1);
          }
        });
    return replayFolders;
  }

  public static int getReplayEnding(File replay) {
    int timestamp_ret = 0;

    try {
      DataInputStream fileInput =
          new DataInputStream(
              new BufferedInputStream(new GZIPInputStream(new FileInputStream(replay))));
      for (; ; ) {
        int timestamp_input = fileInput.readInt();

        // EOF
        if (timestamp_input == -1) break;

        // Skip data, we need to find the last timestamp
        int length = fileInput.readInt();
        if (length > 0) {
          int skipped = fileInput.skipBytes(length);

          if (skipped != length) break;
        }

        timestamp_ret = timestamp_input;
      }
      fileInput.close();
    } catch (Exception e) {
      // e.printStackTrace();
    }

    return timestamp_ret;
  }

  // recurse through directory to get all folders
  public static void listf(String directoryName, List<File> files) {
    File directory = new File(directoryName);

    File[] fList = directory.listFiles();
    if (fList != null) {
      for (File file : fList) {
        if (file.isDirectory()) {
          listf(file.getAbsolutePath(), files);
          files.add(file);
        }
      }
    }
  }

  /** Used for RSC127 */
  public static long username2hash(String s) {
    String s1 = "";
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c >= 'a' && c <= 'z') s1 = s1 + c;
      else if (c >= 'A' && c <= 'Z') s1 = s1 + (char) ((c + 97) - 65);
      else if (c >= '0' && c <= '9') s1 = s1 + c;
      else s1 = s1 + ' ';
    }

    s1 = s1.trim();
    if (s1.length() > 12) s1 = s1.substring(0, 12);
    long hash = 0L;
    for (int j = 0; j < s1.length(); j++) {
      char c1 = s1.charAt(j);
      hash *= 37L;
      if (c1 >= 'a' && c1 <= 'z') hash += (1 + c1) - 97;
      else if (c1 >= '0' && c1 <= '9') hash += (27 + c1) - 48;
    }

    return hash;
  }

  /** Used for RSC127 */
  public static String hash2username(long hash) {
    if (hash < 0L) return "invalidName";
    String s = "";
    while (hash != 0L) {
      int i = (int) (hash % 37L);
      hash /= 37L;
      if (i == 0) s = " " + s;
      else if (i < 27) {
        if (hash % 37L == 0L) s = (char) ((i + 65) - 1) + s;
        else s = (char) ((i + 97) - 1) + s;
      } else {
        s = (char) ((i + 48) - 27) + s;
      }
    }
    return s;
  }

  /** RSC127 - put an int into buffer at specific offset */
  public static void int_put(byte[] buffer, int offset, int num) {
    buffer[offset] = (byte) (num >> 24);
    buffer[offset + 1] = (byte) (num >> 16);
    buffer[offset + 2] = (byte) (num >> 8);
    buffer[offset + 3] = (byte) num;
  }

  /** RSC175 - format a string to only have letters and numbers, with maxlength */
  public static String formatString(String s, int maxLen) {
    String lowerString = s.toLowerCase();
    String res = "";
    for (int i = 0; i < lowerString.length() && i < maxLen; ++i) {
      char ch = lowerString.charAt(i);
      if (ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
        res = String.valueOf(res) + ch;
      }
    }
    return res;
  }

  public static int boundUnsignedShort(String num) throws NumberFormatException {
    int result;
    int limit = Short.MAX_VALUE - Short.MIN_VALUE;
    try {
      result = Integer.parseInt(num);
      if (result < 0) {
        return 0;
      } else if (result >= 0 && result <= limit) {
        return result;
      } else {
        return limit;
      }
    } catch (NumberFormatException nfe) {
      throw nfe;
    }
  }

  public static String execCmd(String[] cmdArray) throws java.io.IOException {
    java.util.Scanner s =
        new java.util.Scanner(Runtime.getRuntime().exec(cmdArray).getInputStream())
            .useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  public static boolean detectBinaryAvailable(String binaryName, String reason) {
    if (System.getProperty("os.name").contains("Windows")) {
      return false; // don't trust Windows to run the detection code
    }

    try {
      // "whereis" is part of the util-linux package,
      // It is included in pretty much all unix-like operating systems; i.e. safe to use.
      final String whereis =
          execCmd(new String[] {"whereis", "-b", binaryName})
              .replace("\n", "")
              .replace(binaryName + ": ", "");
      if (whereis.length() < ("/" + binaryName).length()) {
        Logger.Error(
            String.format(
                "@|red !!! Please install %s for %s to work on Linux (or other systems with compatible binary) !!!|@",
                binaryName, reason));
        return false;
      } else {
        Logger.Info(binaryName + ": " + whereis);
        return true;
      }
    } catch (IOException e) {
      Logger.Error("Error while detecting " + binaryName + " binary: " + e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

  public static boolean notMacWindows() {
    if (System.getProperty("os.name").contains("Windows")) {
      return false;
    }
    return !isMacOS();
  }

  public static void openLinkInBrowser(String url) {
    Thread t = new Thread(new LinkOpener(url));
    t.start();
  }
}
