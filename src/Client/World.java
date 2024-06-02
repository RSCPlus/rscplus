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

import Client.Extensions.WorldType;
import Client.ServerExtensions.Extension;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.json.JSONObject;

/** POJO and utility methods for dealing with world .ini files */
public class World {

  // World file definition fields
  public static final String NAME = "name";
  public static final String URL = "url";
  public static final String PORT = "port";
  public static final String RSA_PUB_KEY = "rsa_pub_key";
  public static final String RSA_EXPONENT = "rsa_exponent";
  public static final String SERVER_TYPE = "servertype";
  public static final String HISCORES_URL = "hiscores_url";
  public static final String REGISTRATION_API_URL = "registration_api_url";
  public static final String WORLD_POPULATION_URL = "world_population_url";
  public static final String SERVER_EXTENSION = "server_extension";
  public static final String WORLD_ID = "world_id";
  public static final String DOWNLOAD_FLAG = "download_flag";

  private final String name;
  private final String url;
  private final String port;
  private final String rsaPubKey;
  private final String rsaExponent;
  private final String serverType;
  private final String hiScoresUrl;
  private final String registrationApiUrl;
  private final String worldPopulationUrl;
  private final String serverExtension;
  private final String worldId;
  private final Integer connectionHash;
  private String downloadFlag;

  private World(
      String name,
      String url,
      String port,
      String rsaPubKey,
      String rsaExponent,
      String serverType,
      String hiScoresUrl,
      String registrationApiUrl,
      String worldPopulationUrl,
      String serverExtension,
      String worldId,
      String downloadFlag) {
    this.name = name;
    this.url = url;
    this.port = port;
    this.rsaPubKey = rsaPubKey;
    this.rsaExponent = rsaExponent;
    this.serverType = serverType;
    this.hiScoresUrl = hiScoresUrl;
    this.registrationApiUrl = registrationApiUrl;
    this.worldPopulationUrl = worldPopulationUrl;
    this.serverExtension = serverExtension;
    this.worldId = worldId;
    this.downloadFlag = downloadFlag;

    final String urlETLD1 = Util.getETLD1(url);
    this.connectionHash =
        ((urlETLD1 != null ? urlETLD1 : url) + port + rsaPubKey + rsaExponent + serverType)
            .hashCode();
  }

  /** @return A default empty {@link World}, used for comparisons */
  static World createEmptyWorld() {
    return new World("World 1", "", "43594", "", "", "1", "", "", "", "", "", "");
  }

  /**
   * Constructs a {@link World} object from a File
   *
   * @param worldFile {@link File} pointing to a world ini on disk
   * @return The constructed {@link World} instance or {@code null} on failure
   */
  public static World fromFile(File worldFile) {
    Properties worldProps = Settings.loadPropertiesFile(worldFile);
    if (worldProps != null) {
      return fromProps(worldProps);
    }

    return null;
  }

  /**
   * Constructs a {@link World} object from properties, with sensible defaults
   *
   * @param propsFile {@link Properties} pertaining to a world file
   * @return The constructed {@link World} instance
   */
  public static World fromProps(Properties propsFile) {
    final String name = (String) propsFile.getOrDefault(NAME, "Unknown World");
    final String url = (String) propsFile.getOrDefault(URL, "");
    final String port = (String) propsFile.getOrDefault(PORT, "");
    final String rsaPubKey = (String) propsFile.getOrDefault(RSA_PUB_KEY, "");
    final String rsaExponent = (String) propsFile.getOrDefault(RSA_EXPONENT, "");
    final String serverType = (String) propsFile.getOrDefault(SERVER_TYPE, "1");
    final String hiScoresUrl = (String) propsFile.getOrDefault(HISCORES_URL, "");
    final String registrationApiUrl = (String) propsFile.getOrDefault(REGISTRATION_API_URL, "");
    final String worldPopulationUrl = (String) propsFile.getOrDefault(WORLD_POPULATION_URL, "");
    final String serverExtension = (String) propsFile.getOrDefault(SERVER_EXTENSION, "");
    final String worldId = (String) propsFile.getOrDefault(WORLD_ID, "");
    final String downloadFlag = (String) propsFile.getOrDefault(DOWNLOAD_FLAG, "");

    return new World(
        name,
        url,
        port,
        rsaPubKey,
        rsaExponent,
        serverType,
        hiScoresUrl,
        registrationApiUrl,
        worldPopulationUrl,
        serverExtension,
        worldId,
        downloadFlag);
  }

  /**
   * Constructs a {@link World} object from JSON, with sensible defaults
   *
   * <p>Used for the world subscription API
   *
   * @param worldJSONObject The {@link JSONObject} with world data
   * @return The constructed {@link World} instance
   */
  public static World fromJSON(JSONObject worldJSONObject) {
    Map<String, Object> worldJSON = worldJSONObject.toMap();

    final String name = (String) worldJSON.getOrDefault(NAME, "Unknown World");
    final String url = (String) worldJSON.getOrDefault(URL, "");
    final String port = (String) worldJSON.getOrDefault(PORT, "");
    final String rsaPubKey = (String) worldJSON.getOrDefault(RSA_PUB_KEY, "");
    final String rsaExponent = (String) worldJSON.getOrDefault(RSA_EXPONENT, "");
    final String serverType = (String) worldJSON.getOrDefault(SERVER_TYPE, "");
    final String hiScoresUrl = (String) worldJSON.getOrDefault(HISCORES_URL, "");
    final String registrationApiUrl = (String) worldJSON.getOrDefault(REGISTRATION_API_URL, "");
    final String worldPopulationUrl = (String) worldJSON.getOrDefault(WORLD_POPULATION_URL, "");
    final String serverExtension = (String) worldJSON.getOrDefault(SERVER_EXTENSION, "");
    final String worldId = (String) worldJSON.getOrDefault(WORLD_ID, "");
    final String downloadFlag =
        (String) worldJSON.getOrDefault(DOWNLOAD_FLAG, String.valueOf(true));

    return new World(
        name,
        url,
        port,
        rsaPubKey,
        rsaExponent,
        serverType,
        hiScoresUrl,
        registrationApiUrl,
        worldPopulationUrl,
        serverExtension,
        worldId,
        downloadFlag);
  }

  /**
   * Constructs a {@link World} object from {@link Settings}
   *
   * @param worldIndex Index to the requested world from the WORLD settings maps
   * @return The constructed {@link World} instance
   */
  public static World fromSettings(int worldIndex) {
    final String name = Settings.WORLD_NAMES.get(worldIndex);
    final String url = Settings.WORLD_URLS.get(worldIndex);
    final String port = Integer.toString(Settings.WORLD_PORTS.get(worldIndex));
    final String rsaPubKey = Settings.WORLD_RSA_PUB_KEYS.get(worldIndex);
    final String rsaExponent = Settings.WORLD_RSA_EXPONENTS.get(worldIndex);
    final String serverType = Integer.toString(Settings.WORLD_SERVER_TYPES.get(worldIndex));
    final String hiScoresUrl = Settings.WORLD_HISCORES_URL.get(worldIndex);
    final String registrationApiUrl = Settings.WORLD_REG_API_URL.get(worldIndex);
    final String worldPopulationUrl = Settings.WORLD_POPULATION_URL.get(worldIndex);
    final String serverExtension = Settings.WORLD_SERVER_EXTENSION.get(worldIndex);
    final String worldId = Settings.WORLD_ID.get(worldIndex);
    final String downloadFlag = Settings.WORLD_DOWNLOAD_FLAG.get(worldIndex);

    return new World(
        name,
        url,
        port,
        rsaPubKey,
        rsaExponent,
        serverType,
        hiScoresUrl,
        registrationApiUrl,
        worldPopulationUrl,
        serverExtension,
        worldId,
        downloadFlag);
  }

  /**
   * Immutably strips all extension data from a provided {@link World}
   *
   * @param world {@link World} object to process
   * @return The processed {@link World} object
   */
  public static World removeExtensionData(World world) {
    return new World(
        world.getName(),
        world.getUrl(),
        world.getPort(),
        world.getRsaPubKey(),
        world.getRsaExponent(),
        world.getServerType(),
        world.getHiScoresUrl(),
        world.getRegistrationApiUrl(),
        world.getWorldPopulationUrl(),
        "",
        "",
        world.getDownloadFlag());
  }

  /**
   * Validates a {@link World} instance, checking to ensure that all required fields are present.
   *
   * @param world {@link World} object to validate
   * @throws IllegalArgumentException when the world is not valid. The error message contains an
   *     HTML unordered list of invalid attributes, used for displaying in error modals.
   */
  public static void validateWorld(World world) throws IllegalArgumentException {
    List<String> invalidFields = new ArrayList<>();

    if (Util.isBlank(world.getName())) invalidFields.add("Name");
    if (Util.isBlank(world.getUrl())) invalidFields.add("URL");
    if (Util.isBlank(world.getPort())) invalidFields.add("Port");
    if (Util.isBlank(world.getServerType())) invalidFields.add("Server type");
    if (Util.isBlank(world.getRsaPubKey())) invalidFields.add("RSA key");
    if (Util.isBlank(world.getRsaExponent())) invalidFields.add("RSA exponent");

    // Ensure required fields are present
    if (!invalidFields.isEmpty()) {
      throw new IllegalArgumentException(
          "missing required attributes:" + Util.buildHTMLBulletList(invalidFields));
    }

    // Ensure boxed fields are correct
    try {
      new BigInteger(world.getRsaPubKey());
    } catch (NumberFormatException nfe) {
      invalidFields.add("RSA key");
    }

    try {
      new BigInteger(world.getRsaExponent());
    } catch (NumberFormatException nfe) {
      invalidFields.add("RSA exponent");
    }

    try {
      Integer.parseInt(world.getPort());
    } catch (NumberFormatException nfe) {
      invalidFields.add("Port");
    }

    try {
      Integer.parseInt(world.getServerType());
    } catch (NumberFormatException nfe) {
      invalidFields.add("Server type");
    }

    if (!invalidFields.isEmpty()) {
      throw new IllegalArgumentException(
          "invalid required attributes:" + Util.buildHTMLBulletList(invalidFields));
    }
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  /**
   * Invokes {@link #hostMatches(String)} with a Set of values, returning {@code true} if any of
   * them match
   *
   * @param hostUrls {@link Set} of {@link String} host URLs to check
   * @return {@code boolean} indicating the match result
   */
  public boolean hostMatchesAny(Set<String> hostUrls) {
    for (String hostUrl : hostUrls) {
      if (hostMatches(hostUrl)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Tests whether the World {@link #url} matches a provided host URL by first checking for exact
   * matches, in case the URL is either an IPv4 address or a special non-valid domain for testing
   * (localhost, rsc.test, etc). When the values are not identical, the eTLD+1 is extracted from
   * both the current URL and the input value and are then checked for equality, such that
   * 'rsc.plus' and 'test.rsc.plus' result in a valid match.
   *
   * @param hostUrl Provided host url as a {@code String}
   * @return {@code boolean} indicating the match result
   * @throws RuntimeException if the provided host URL is null
   */
  public boolean hostMatches(String hostUrl) throws RuntimeException {
    if (hostUrl == null) {
      throw new RuntimeException("Provided host URL must not be null");
    }

    if (hostUrl.equals(this.url)) {
      return true;
    }

    // Because the equality check did not pass and the eTLD+1 calculation would return null anyway,
    // this saves the program from having to go through that logic and check every possible suffix
    if (Util.isIpV4Address(hostUrl)) {
      return false;
    }

    final String worldETLD1 = Util.getETLD1(this.url);

    // Because the equality check did not pass above, there's no way for this to match below
    if (worldETLD1 == null) {
      return false;
    }

    final String hostETLD1 = Util.getETLD1(hostUrl);

    return worldETLD1.equals(hostETLD1);
  }

  public String getPort() {
    return port;
  }

  public String getRsaPubKey() {
    return rsaPubKey;
  }

  public String getRsaExponent() {
    return rsaExponent;
  }

  public String getServerType() {
    return serverType;
  }

  public String getHiScoresUrl() {
    return hiScoresUrl;
  }

  public String getRegistrationApiUrl() {
    return registrationApiUrl;
  }

  public String getWorldPopulationUrl() {
    return worldPopulationUrl;
  }

  public String getServerExtension() {
    return serverExtension;
  }

  public String getWorldId() {
    return worldId;
  }

  /**
   * Tests for equality between this world's extension and a provided value
   *
   * @param extension {@link Extension} to check against
   * @return {@code boolean} indicating match status
   */
  public boolean extensionMatches(Extension extension) {
    if (extension == null) {
      return false;
    }

    return extension.equals(Extension.from(this.serverExtension));
  }

  /**
   * Tests for equivalence between this world's world ID and a provided type
   *
   * @param worldType {@link WorldType} to check against
   * @return {@code boolean} indicating match status
   */
  public boolean idMatchesWorldType(WorldType worldType) {
    if (worldType == null) {
      return false;
    }

    return worldType.matchesWorldId(worldId);
  }

  public String getDownloadFlag() {
    return downloadFlag;
  }

  void setDownloadFlag(boolean flag) {
    downloadFlag = String.valueOf(flag);
  }

  /**
   * Syntax sugar to handle {@link Boolean} unboxing
   *
   * @return download flag status
   */
  public boolean wasDownloaded() {
    return Boolean.parseBoolean(downloadFlag);
  }

  public Integer getConnectionHash() {
    return this.connectionHash;
  }

  /**
   * Constructs a {@link Properties} object from a given {@link World}
   *
   * @param world The {@link World} to parse
   * @return The constructed {@link Properties} instance
   */
  public static Properties toProperties(World world) {
    Properties worldProps = new Properties();

    worldProps.put(NAME, world.getName());
    worldProps.put(URL, world.getUrl());
    worldProps.put(PORT, world.getPort());
    worldProps.put(RSA_PUB_KEY, world.getRsaPubKey());
    worldProps.put(RSA_EXPONENT, world.getRsaExponent());
    worldProps.put(SERVER_TYPE, world.getServerType());
    worldProps.put(HISCORES_URL, world.getHiScoresUrl());
    worldProps.put(REGISTRATION_API_URL, world.getRegistrationApiUrl());
    worldProps.put(WORLD_POPULATION_URL, world.getWorldPopulationUrl());
    worldProps.put(SERVER_EXTENSION, world.getServerExtension());
    worldProps.put(WORLD_ID, world.getWorldId());
    worldProps.put(DOWNLOAD_FLAG, world.getDownloadFlag());

    return worldProps;
  }

  /**
   * Constructs a mapping of File objects to World instances, primarily used in conjunction with the
   * Worlds directory in {@link Settings#getWorldFiles}
   *
   * @param worldFiles {@link List} of {@link File} objects to parse
   * @return The constructed {@link Map} of {@link File} to {@link World}
   */
  public static Map<File, World> parseFilesToMap(List<File> worldFiles) {
    Map<File, World> worldsMap = new LinkedHashMap<>();

    for (File worldFile : worldFiles) {
      if (worldFile.getName().endsWith(".ini")) {
        Properties worldProps = Settings.loadPropertiesFile(worldFile);

        if (worldProps == null) {
          continue;
        }

        worldsMap.put(worldFile, World.fromProps(worldProps));
      }
    }

    return worldsMap;
  }

  /** @return {@code boolean} value indicating whether the connection definitions are equivalent */
  public boolean connectionEquals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    World world = (World) o;
    return hostMatches(world.url)
        && Objects.equals(port, world.port)
        && Objects.equals(rsaPubKey, world.rsaPubKey)
        && Objects.equals(rsaExponent, world.rsaExponent)
        && Objects.equals(serverType, world.serverType);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    World world = (World) o;
    return Objects.equals(name, world.name)
        && Objects.equals(url, world.url)
        && Objects.equals(port, world.port)
        && Objects.equals(rsaPubKey, world.rsaPubKey)
        && Objects.equals(rsaExponent, world.rsaExponent)
        && Objects.equals(serverType, world.serverType)
        && Objects.equals(hiScoresUrl, world.hiScoresUrl)
        && Objects.equals(registrationApiUrl, world.registrationApiUrl)
        && Objects.equals(worldPopulationUrl, world.worldPopulationUrl)
        && Objects.equals(serverExtension, world.serverExtension)
        && Objects.equals(worldId, world.worldId)
        && Objects.equals(downloadFlag, world.downloadFlag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        url,
        port,
        rsaPubKey,
        rsaExponent,
        serverType,
        hiScoresUrl,
        registrationApiUrl,
        worldPopulationUrl,
        serverExtension,
        worldId,
        downloadFlag);
  }
}
