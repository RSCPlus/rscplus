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

import Client.Extensions.OpenRSCOfficialUtils;
import Client.Extensions.WorldType;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Custom server extension configuration management
 *
 * <p>Follow this process to correctly register a new server extension:
 *
 * <ol>
 *   <li>First, decide whether your extension should be exclusive to a specific private server host
 *       or if it may be freely used by any world.ini file that specifies it. Common extensions are
 *       not allowed to configure a {@link WorldSubscription} or {@link BinaryInfo}, while exclusive
 *       extensions MUST register a {@link WorldSubscription}. See below for more information
 *       regarding these features.
 *   <li>Above {@link #bootstrap()}, define a public static constant representing the extension
 *   <li>To register an exclusive extension:
 *       <ol>
 *         <li>Create a private static helper method above {@link #setupDefaultExtension}, following
 *             this naming convention: {@code setupMyExtension()}
 *         <li>Within your helper method:
 *             <ol>
 *               <li>As the first instruction within the helper, invoke {@link
 *                   Config#registerExtension(Extension)}, passing in your new constant
 *               <li>To enable automatic world downloading from a subscription API and extension
 *                   validation features:
 *                   <ol>
 *                     <li>First, construct a new {@link WorldSubscription} object via {@link
 *                         WorldSubscription#WorldSubscription(String)}.
 *                     <li>Then, for every endpoint you plan on hosting, invoke {@link
 *                         WorldSubscription#addDownloadURL(WorldType, String, boolean)}, providing
 *                         a {@link WorldType} to identify a logical grouping of worlds. Note:
 *                         downloaded world files will be sorted in the same order as the call chain
 *                         here
 *                     <li>Finally, invoke {@link Config#setWorldSubscription(Extension,
 *                         WorldSubscription)} to finalize setup.
 *                   </ol>
 *               <li><i>(Optional)</i> To enable your extension to work with custom binaries:
 *                   <ol>
 *                     <li>First, construct a new {@link BinaryInfo} object via {@link
 *                         BinaryInfo#BinaryInfo(String, String)}.
 *                     <li>Then, for every distribution type you wish to support, invoke {@link
 *                         BinaryInfo#addBinaryDownload(BINARY_TYPE, String, String)}, providing a
 *                         {@link BINARY_TYPE} to identify each download.
 *                     <li>Finally, invoke {@link Config#setBinaryInfo(Extension, BinaryInfo)} to
 *                         finalize setup.
 *                   </ol>
 *               <li><i>(Optional)</i> Exclusive extensions that manage their own population
 *                   fetching logic should opt-out of the world file-based population feature by
 *                   invoking {@link #populationFeatureOptOut(Extension)}
 *             </ol>
 *         <li>Finally, invoke your helper from within {@link ServerExtensions#bootstrap()}, within
 *             the block labeled "Exclusive extensions."
 *       </ol>
 *   <li>To register a common extension, simply invoke {@link #addCommonExtension(Extension)} within
 *       {@link #bootstrap()}, above the block labeled "Common extensions."
 * </ol>
 *
 * <p>Validation will be performed on application launch to ensure mistakes weren't made in the
 * bootstrapping process - errors will be thrown to assist in correcting any issues.
 *
 * <p>For more information on Server Extension usage and all related features, see {@code README.md}
 * located in the {@link Client.Extensions} package.
 */
public class ServerExtensions {

  // Internal usage
  public static final Set<Extension> commonExtensions = new HashSet<>();
  public static final Extension NONE =
      new Extension("NONE", ""); // Used when no extensions are active
  private static final Extension RSCPLUS =
      new Extension("rscplus", "RSCPlus"); // Built-in extension for the official RSCPlus binary
  private static Extension activeServerExtension = NONE; // The currently-active extension
  private static final Set<Extension> populationOptOutSet = new HashSet<>();

  /* Bootstrap known server data */

  // Server extension declarations
  public static final Extension OPENRSC_FRAMEWORK = new Extension("openrsc", "OpenRSC Framework");
  public static final Extension OPENRSC_OFFICIAL = new Extension("openrsc_official", "OpenRSC");

  /** Initialize all {@link ServerExtensions} configurations for use throughout the application */
  static void bootstrap() {
    // Built-in RSCPlus data
    setupDefaultExtension();

    /* Common extensions */
    addCommonExtension(OPENRSC_FRAMEWORK);

    /* Exclusive extensions */

    // OpenRSC Extension
    setupOpenRSCOfficialExtension();

    /* Validate extensions setup */
    Config.validateDeveloperExtensionRegistration();
  }

  /** OpenRSC extension configuration */
  private static void setupOpenRSCOfficialExtension() {
    Config.registerExtension(OPENRSC_OFFICIAL);

    WorldSubscription openRSCOfficialWorldSub = new WorldSubscription("openrsc.com");

    openRSCOfficialWorldSub.addDownloadURL(
        OpenRSCOfficialUtils.PRESERVATION,
        "https://rsc.vet/worlds/preservation.json" + Util.getUTMParams(),
        true);
    openRSCOfficialWorldSub.addDownloadURL(
        OpenRSCOfficialUtils.URANIUM,
        "https://rsc.vet/worlds/uranium.json" + Util.getUTMParams(),
        false);

    // Compile world subscription objects
    Config.setWorldSubscription(OPENRSC_OFFICIAL, openRSCOfficialWorldSub);

    // OpenRSC binary info
    final BinaryInfo openRSCOfficialBinaryInfo =
        new BinaryInfo("Open", "https://github.com/RSCPlus/OpenRSCPlus/releases/download/Latest/");
    openRSCOfficialBinaryInfo.addBinaryDownload(
        BINARY_TYPE.WINDOWS_X64, "OpenRSCPlusSetup.exe", "version_windows_64.txt");
    openRSCOfficialBinaryInfo.addBinaryDownload(
        BINARY_TYPE.WINDOWS_X32, "OpenRSCPlusSetup32.exe", "version_windows_32.txt");
    openRSCOfficialBinaryInfo.addBinaryDownload(
        BINARY_TYPE.MACOS_ARM, "OpenRSCPlus-aarch64.dmg", "version_macos.txt");
    openRSCOfficialBinaryInfo.addBinaryDownload(
        BINARY_TYPE.MACOS_X64, "OpenRSCPlus-x64.dmg", "version_macos.txt");
    openRSCOfficialBinaryInfo.addBinaryDownload(
        BINARY_TYPE.LINUX_APP_IMAGE, "OpenRSCPlus.AppImage", "version_linux_appimage.txt");

    Config.setBinaryInfo(OPENRSC_OFFICIAL, openRSCOfficialBinaryInfo);

    // Has a specialized mechanism for fetching populations
    populationFeatureOptOut(OPENRSC_OFFICIAL);
  }

  /**
   * Default RSCPlus extension configuration
   *
   * <p>Used to define download information for the official binary and reserve the "RSCPlus" name
   */
  private static void setupDefaultExtension() {
    Config.registerExtension(RSCPLUS);

    final BinaryInfo RSCPlusBinaryInfo =
        new BinaryInfo("", "https://github.com/RSCPlus/rscplus/releases/download/Latest/");
    RSCPlusBinaryInfo.addBinaryDownload(
        BINARY_TYPE.WINDOWS_X64, "RSCPlusSetup.exe", "version_windows_64.txt");
    RSCPlusBinaryInfo.addBinaryDownload(
        BINARY_TYPE.WINDOWS_X32, "RSCPlusSetup32.exe", "version_windows_32.txt");
    RSCPlusBinaryInfo.addBinaryDownload(
        BINARY_TYPE.MACOS_ARM, "RSCPlus-aarch64.dmg", "version_macos.txt");
    RSCPlusBinaryInfo.addBinaryDownload(
        BINARY_TYPE.MACOS_X64, "RSCPlus-x64.dmg", "version_macos.txt");
    RSCPlusBinaryInfo.addBinaryDownload(
        BINARY_TYPE.LINUX_APP_IMAGE, "RSCPlus.AppImage", "version_linux_appimage.txt");

    Config.setBinaryInfo(RSCPLUS, RSCPlusBinaryInfo);
  }

  private static void addCommonExtension(Extension extension) {
    Config.registerExtension(extension);
    commonExtensions.add(extension);
  }

  /* End of server extension bootstrapping */

  /**
   * Check whether the provided extension is active, for use when following logic should only apply
   * to a single extension
   *
   * @param extension The {@link Extension} to check
   * @return {@code boolean} indicating the result
   */
  public static boolean enabled(Extension extension) {
    return activeServerExtension.equals(extension);
  }

  /**
   * Check whether any of the provided extensions are active, for use when following logic can apply
   * to multiple extensions
   *
   * @param extensions {@code varargs} list of {@link Extension}s to check
   * @return {@code boolean} indicating the result
   */
  public static boolean enabled(Extension... extensions) {
    return Arrays.asList(extensions).contains(activeServerExtension);
  }

  /** @return {@code boolean} Indicating whether any extension is currently active */
  public static boolean anyEnabled() {
    return !activeServerExtension.equals(NONE);
  }

  /** @return {@code boolean} Indicating whether the provided extension is a common type */
  public static boolean isCommonExtension(Extension extension) {
    return commonExtensions.contains(extension);
  }

  /**
   * Checks whether the world subscription map has defined data for a given extension
   *
   * @param extension The {@link Extension} to look for
   * @return {@code boolean} indicating found status
   */
  public static boolean hasWorldSubscription(Extension extension) {
    return Config.worldSubscriptionMap.containsKey(extension);
  }

  /**
   * Checks whether the binary info map has defined data for a given extension
   *
   * @param extension The {@link Extension} to look for
   * @return {@code boolean} indicating found status
   */
  public static boolean hasBinaryInfo(Extension extension) {
    return Config.binaryInfoMap.containsKey(extension);
  }

  /**
   * Returns the configured {@link WorldSubscription} for a given extension
   *
   * @param extension The {@link Extension} to retrieve world subscription data for
   * @return {@link WorldSubscription} tied to the provided extension
   */
  public static WorldSubscription getWorldSubscription(Extension extension) {
    return Config.worldSubscriptionMap.get(extension);
  }

  /**
   * Returns the {@link BinaryInfo} for a given extension
   *
   * @param extension The {@link Extension} to retrieve binary data for
   * @return {@link BinaryInfo} tied to the provided extension
   */
  public static BinaryInfo getBinaryInfo(Extension extension) {
    return Config.binaryInfoMap.get(extension);
  }

  /**
   * Retrieve the currently-active extension
   *
   * <p>Note: For use solely in cases when following logic is agnostic to the actual extension
   *
   * @return the active {@link Extension}
   * @see #enabled(Extension)
   * @see #enabled(Extension...)
   */
  public static Extension getActiveExtension() {
    return activeServerExtension;
  }

  /**
   * Sets the active extension, ensuring that it is both defined and valid
   *
   * @param extension The {@link Extension} to be used
   */
  protected static void setActiveExtension(Extension extension) {
    if (extension.equals(NONE) || extension.equals(RSCPLUS)) {
      // Reserved for internal usage
      activeServerExtension = NONE;
    } else if (Config.knownExtensions.contains(extension)) {
      activeServerExtension = extension;
    } else {
      Logger.Warn("Prevented setting unknown extension: [" + extension + "]");
    }
  }

  /**
   * Opt-out of the world.ini-based population fetching feature for a given exclusive extension,
   * when it defines its own methodology for fetching populations
   *
   * @param extension The exclusive {@link Extension} wishing to opt-out
   */
  private static void populationFeatureOptOut(Extension extension) {
    if (NONE.equals(extension) || RSCPLUS.equals(extension)) {
      throw new IllegalArgumentException("Cannot opt-out of the NONE or rscplus extensions");
    }

    if (isCommonExtension(extension)) {
      throw new IllegalArgumentException(
          "Common extensions cannot opt out of the world population feature");
    }

    populationOptOutSet.add(extension);
  }

  /**
   * @return {@code boolean} indicating whether the extension has opted-out of the world.ini-based
   *     population fetching feature
   */
  public static boolean populationFeatureDisabled(Extension extension) {
    return populationOptOutSet.contains(extension);
  }

  /**
   * Sanitizes server extension-related {@link Settings} for a specified world by matching the ini
   * values to the {@link Config} data declared within {@link ServerExtensions#bootstrap()}.
   *
   * <p>When a corresponding {@link WorldSubscription} is defined, this will attempt to fetch world
   * data to validate extension settings against, unless it was already downloaded on startup. This
   * is particularly useful for automatically fetching extension data even when the client is run
   * without a provided {@link Launcher#worldSubscriptionId}.
   *
   * <p>This method is called when parsing world ini files during application startup in {@link
   * Settings#initWorlds()} and when settings are saved from the {@link ConfigWindow}.
   *
   * <p>For more information on Server Extension usage, see {@code README.md} located in the {@link
   * Client.Extensions} package.
   *
   * @param worldIndex {@link Settings} map index for the world being processed
   */
  protected static void validateServerExtensionSettings(int worldIndex) {
    final World currWorld = World.fromSettings(worldIndex);

    // Skip validation for local testing, allows the use of any extension/world ID on a local server
    if (Util.isLocalhost(currWorld.getUrl())) {
      return;
    }

    // Check to see if the URL has been retired
    if (Config.getWorldSubscriptionMap().entrySet().stream()
        .anyMatch((entry) -> entry.getValue().formerDomainMatch(currWorld))) {
      // Block connections to the world
      Launcher.setWarning(Launcher.LauncherError.WORLD_SUB_MISMATCH);
      Launcher.blockedWorlds.add(currWorld);
    }

    // Check if the world was downloaded this launch and validate the received data
    for (Map.Entry<URI, List<World>> downloadedSubWorlds : Launcher.downloadedWorlds.entrySet()) {
      List<World> downloadedSubWorldsList = downloadedSubWorlds.getValue();

      if (downloadedSubWorldsList != null) {
        for (World downloadedWorld : downloadedSubWorldsList) {
          // Found the world in the download list
          if (downloadedWorld.equals(currWorld)) {
            validateDownloadedWorld(currWorld, downloadedWorld, worldIndex);

            return; // Skip other validations
          }
        }
      }
    }

    final String currExtensionVal = currWorld.getServerExtension();

    // When no extension exists, attempt to discover it
    if (Util.isBlank(currExtensionVal)) {
      World sanitizedWorld = currWorld;
      // If there was a world ID defined without an extension, remove it
      if (!currWorld.getWorldId().isEmpty()) {
        Settings.WORLD_ID.put(worldIndex, "");
        sanitizedWorld = World.removeExtensionData(currWorld);
      }
      discoverExtension(worldIndex, sanitizedWorld);
      return;
    }

    final Extension currExtension = Extension.from(currExtensionVal);

    // Look for a subscription defined for the extension
    WorldSubscription extensionSubscription = Config.getWorldSubscriptionMap().get(currExtension);

    // No subscription exists or the extension doesn't exist
    if (extensionSubscription == null) {
      // No subscription exists for the extension - ensure extension is valid
      if (!currExtension.equals(NONE)
          && !currExtension.equals(RSCPLUS)
          && Config.knownExtensions.contains(currExtension)) {
        // Check if current world URL matches any host URL for a different subscription
        if (Config.getWorldSubscriptionMap().values().stream()
            .anyMatch(
                sub ->
                    currWorld.hostMatches(sub.getHostUrl())
                        || currWorld.hostMatchesAny(sub.getFormerHostDomains()))) {
          invalidateExtensionAttemptDiscovery(worldIndex, currWorld);
          return;
        }

        // A defined World ID without a matching subscription would be invalid,
        // as there is no way to validate that it is legitimate
        Settings.WORLD_ID.put(worldIndex, "");
        return;
      }

      // Remove the non-existent extension and attempt to resolve the current world by its URL
      invalidateExtensionAttemptDiscovery(worldIndex, currWorld);
      return;
    }

    // When sub host and world URL don't match, the world is either misconfigured or someone is
    // trying to force the use of an invalid extension for the world
    if (!currWorld.hostMatches(extensionSubscription.getHostUrl())) {
      // If the file predated a legitimate host domain change and the sub was unreachable after the
      // user had already updated their client, the original world file would have been preserved if
      // its extension data matched, and it should not have its extension data removed, to prevent
      // its deletion in subsequent launches. Users will be unable to connect to it regardless.
      if (Launcher.shouldDownloadWorlds()) {
        if (!extensionSubscription.formerDomainMatch(currWorld)) {
          invalidateExtensionAttemptDiscovery(worldIndex, currWorld);
        }
      } else {
        invalidateExtensionAttemptDiscovery(worldIndex, currWorld);
      }
      return;
    }

    // Download subscription data for the world file
    downloadAndVerifyWorldData(extensionSubscription, worldIndex, currWorld);
  }

  /**
   * Helper for {@link #validateServerExtensionSettings}
   *
   * <p>Removes extension data from the world and invokes extension discovery
   *
   * @param worldIndex Index corresponding to the world in the Settings map
   * @param currWorld The {@link World} being processed
   */
  private static void invalidateExtensionAttemptDiscovery(int worldIndex, World currWorld) {
    Settings.WORLD_SERVER_EXTENSION.put(worldIndex, "");
    Settings.WORLD_ID.put(worldIndex, "");
    currWorld = World.removeExtensionData(currWorld);
    discoverExtension(worldIndex, currWorld);
  }

  /**
   * Helper for {@link #validateServerExtensionSettings}
   *
   * <p>Performs extension discovery via world URL to host matching
   *
   * @param worldIndex Index corresponding to the world in the Settings map
   * @param currWorld The {@link World} being processed
   */
  private static void discoverExtension(int worldIndex, World currWorld) {
    // Look for an existing subscription based on the world URL
    WorldSubscription matchingSub =
        Config.getWorldSubscriptionMap().entrySet().stream()
            .filter((entry) -> currWorld.hostMatches(entry.getValue().getHostUrl()))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(null);

    // Extension or subscription don't exist, or the client is out of date
    if (matchingSub == null) {
      // A defined world ID would be the result of a misconfiguration
      Settings.WORLD_ID.put(worldIndex, "");

      return;
    }

    // Download subscription data for the world file
    downloadAndVerifyWorldData(matchingSub, worldIndex, currWorld);
  }

  /**
   * Helper for {@link #validateServerExtensionSettings}
   *
   * <p>Downloads and verifies world data from a {@link WorldSubscription}
   *
   * @param worldSub The {@link WorldSubscription} used to download world data
   * @param worldIndex Index corresponding to the world in the Settings map
   * @param currWorld The {@link World} being processed
   */
  private static void downloadAndVerifyWorldData(
      WorldSubscription worldSub, int worldIndex, World currWorld) {
    URI typeDownloadURL = worldSub.getDownloadURIForWorldID(currWorld.getWorldId());

    // An existing world ID was defined in the file, so the exact sub URL is known
    if (typeDownloadURL != null) {
      List<World> fetchedWorlds = Launcher.fetchServerWorlds(typeDownloadURL);

      // Couldn't connect to the sub - keep all data
      if (fetchedWorlds == null) {
        return;
      }

      Optional<World> matchedWorldOptional =
          fetchedWorlds.stream()
              .filter(fetchedWorld -> fetchedWorld.connectionEquals(currWorld))
              .findAny();

      if (matchedWorldOptional.isPresent()) {
        final World downloadedWorld = matchedWorldOptional.get();

        // Validate the matched world and update world settings
        if (validateDownloadedWorld(currWorld, downloadedWorld, worldIndex)) {
          upgradeWorldData(worldIndex, downloadedWorld);
        }
      } else {
        // No matching world was found for the download - block connections
        // and tell the user to update
        Launcher.setWarning(Launcher.LauncherError.WORLD_SUB_MISMATCH);
        Launcher.blockedWorlds.add(currWorld);
      }
    } else {
      // Good world ID was not defined, need to query all sub URLs
      boolean allDownloadsSucceeded = true;
      final List<World> allWorldsForSub = new ArrayList<>();

      for (URI uri : worldSub.getWorldDownloadURIs().values()) {
        List<World> fetchedWorlds = Launcher.fetchServerWorlds(uri);
        if (fetchedWorlds != null) {
          allWorldsForSub.addAll(fetchedWorlds);
        } else {
          allDownloadsSucceeded = false;
        }
      }

      Optional<World> matchedWorldOptional =
          allWorldsForSub.stream()
              .filter(fetchedWorld -> fetchedWorld.connectionEquals(currWorld))
              .findFirst();

      // No matching world was found for the download when all downloads succeeded
      if (allDownloadsSucceeded && !matchedWorldOptional.isPresent()) {
        // Block connections to the world, telling the user to update
        Launcher.setWarning(Launcher.LauncherError.WORLD_SUB_MISMATCH);
        Launcher.blockedWorlds.add(currWorld);
        return;
      }

      // Note: Don't block connections if all downloads did not succeed
      if (matchedWorldOptional.isPresent()) {
        // Upgrade the world file if valid data was presented
        World downloadedWorld = matchedWorldOptional.get();
        if (validateDownloadedWorld(currWorld, downloadedWorld, worldIndex)) {
          upgradeWorldData(worldIndex, downloadedWorld);
        }
      } else {
        // Since we can derive the extension, we can set it when it's not defined
        if (currWorld.getServerExtension().isEmpty()) {
          Extension currExtension = Config.getExtensionForWorldSub(worldSub);
          if (currExtension != null) {
            Settings.WORLD_SERVER_EXTENSION.put(worldIndex, currExtension.getId());
          }
        }

        // Remove the world ID when it exists, since it could not have been valid
        // in order to reach this block in the first place
        Settings.WORLD_ID.put(worldIndex, "");
      }
    }
  }

  /**
   * Helper for {@link #validateServerExtensionSettings}
   *
   * <p>Verifies data fields from within the successfully-downloaded {@link World}
   *
   * @param currWorld The {@link World} being processed
   * @param downloadedWorld The downloaded {@link World}
   * @param worldIndex Index corresponding to the world in the Settings map
   * @return {@code boolean} indicating if the downloaded world was valid
   */
  public static boolean validateDownloadedWorld(
      World currWorld, World downloadedWorld, int worldIndex) {
    // First, ensure the sub defined an extension and a world ID
    boolean subDataValid =
        !downloadedWorld.getServerExtension().isEmpty() && !downloadedWorld.getWorldId().isEmpty();

    // Next, ensure the extension data is valid
    if (subDataValid) {
      WorldSubscription matchedWorldSub =
          getWorldSubscription(Extension.from(downloadedWorld.getServerExtension()));

      // Ensure the extension and host match and that the world ID is valid for the type
      if (matchedWorldSub == null
          || !downloadedWorld.hostMatches(matchedWorldSub.getHostUrl())
          || matchedWorldSub.getWorldDownloadURIs().keySet().stream()
              .noneMatch(downloadedWorld::idMatchesWorldType)) {
        subDataValid = false;
      }

      // Ensure the world ID matches the download location
      if (subDataValid) {
        URI worldIDDownloadURI =
            matchedWorldSub.getDownloadURIForWorldID(downloadedWorld.getWorldId());

        Optional<List<World>> worldsForType =
            Launcher.downloadedWorlds.entrySet().stream()
                .filter(entry -> entry.getKey().equals(worldIDDownloadURI))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .findFirst();

        if (!worldsForType.isPresent() || !worldsForType.get().contains(downloadedWorld)) {
          subDataValid = false;
        }
      }
    }

    if (!subDataValid) {
      // If the world was blocked during initial download due to a domain mismatch,
      // the extension data should not be removed such that subsequent launches don't
      // create infinite copies. Users won't be able to connect to the world anyway and
      // if the sub gets fixed the data will be replaced in the future.
      if (Launcher.blockedWorlds.contains(currWorld)
          && Launcher.subWorldFiles.contains(new File(Settings.WORLD_FILE_PATHS.get(worldIndex)))) {
        return false;
      }

      // Block connections to the world, telling the user to update
      Launcher.setWarning(Launcher.LauncherError.WORLD_SUB_MISMATCH);
      Launcher.blockedWorlds.add(currWorld);

      // Remove server extension and world ID if it doesn't match the subscription
      Settings.WORLD_SERVER_EXTENSION.put(worldIndex, "");
      Settings.WORLD_ID.put(worldIndex, "");

      // Update downloaded world data to prevent incorrect re-processing in the future
      final World noExtensionWorld = World.removeExtensionData(downloadedWorld);
      Launcher.downloadedWorlds.values().stream()
          .filter(worlds -> worlds != null && worlds.contains(downloadedWorld))
          .forEach(worlds -> worlds.set(worlds.indexOf(downloadedWorld), noExtensionWorld));

      return false;
    }

    return true;
  }

  /**
   * Helper for {@link #validateServerExtensionSettings}
   *
   * <p>Upgrades certain fields for an existing {@link World} using values from the subscription API
   *
   * @param worldIndex Index corresponding to the world in the Settings map
   * @param downloadedWorld The downloaded {@link World}
   */
  private static void upgradeWorldData(int worldIndex, World downloadedWorld) {
    // Update world fields
    Settings.WORLD_DOWNLOAD_FLAG.put(worldIndex, String.valueOf(true));
    Settings.WORLD_REG_API_URL.put(worldIndex, downloadedWorld.getRegistrationApiUrl());
    Settings.WORLD_POPULATION_URL.put(worldIndex, downloadedWorld.getWorldPopulationUrl());
    Settings.WORLD_HISCORES_URL.put(worldIndex, downloadedWorld.getHiScoresUrl());
    Settings.WORLD_SERVER_EXTENSION.put(worldIndex, downloadedWorld.getServerExtension());
    Settings.WORLD_ID.put(worldIndex, downloadedWorld.getWorldId());
  }

  /**
   * Get the eTLD+1 for provided world host URL
   *
   * <p>e.g. "something.example.com" will return "example.com"
   *
   * @param url Provided host URL as a {@link String}
   * @return {@link String} representation of the eTLD+1 or the original value if none exists
   */
  private static String getHostDomain(String url) {
    if (Util.isIpV4Address(url)) {
      // Will not have an eTLD+1, fall back to current URL
      return url;
    }

    final String eTLD1 = Util.getETLD1(url);
    if (eTLD1 == null) {
      // No real eTLD1, fall back to current URL
      return url;
    }

    return eTLD1;
  }

  /* Internal classes */

  /** Configuration definition for a Server Extension */
  public static final class Extension {
    private final String id;
    private final String name;
    private final int hash;

    /**
     * Private constructor <b>ONLY</b> for use within {@link ServerExtensions}
     *
     * <p>NOTE: It is CRITICAL that this never gets called outside of bootstrap()
     *
     * @param id Extension identifier
     * @param name Displayable name
     */
    private Extension(String id, String name) {
      if (Util.isBlank(id)) {
        throw new RuntimeException("Extension id cannot be null or empty");
      }

      // The special "NONE" extension does not have a displayable name value
      if (!id.equals("NONE") && Util.isBlank(name)) {
        throw new RuntimeException("Extension name cannot be null or empty");
      }

      if (id.contains(".")) {
        throw new RuntimeException("Extension names cannot contain \".\" characters");
      }

      this.id = id;
      this.name = name;
      this.hash = id.toLowerCase().hashCode(); // Normalize
    }

    /**
     * Public constructor which performs data validation, that may be used outside of this class
     *
     * @param id Displayable id for the extension
     * @return The constructed {@link Extension} instance
     */
    public static Extension from(String id) {
      // Special handling for when no extension was defined
      if (Util.isBlank(id)) {
        return NONE;
      }

      // Treat undefined extensions as "NONE"
      return Config.knownExtensions.stream()
          .filter(extension -> extension.getId().equalsIgnoreCase(id))
          .findFirst()
          .orElse(NONE);
    }

    /** @return The extension identifier */
    public String getId() {
      return id;
    }

    /** @return The displayable name */
    public String getName() {
      return name;
    }

    /** Note: should only be used for display purposes */
    @Override
    public String toString() {
      return getId();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      return id.equalsIgnoreCase(((Extension) o).id);
    }

    @Override
    public int hashCode() {
      return hash;
    }
  }

  /**
   * Configuration data required for serving world files from a subscription API
   *
   * <p><b>The following is required to utilize this feature:</b>
   *
   * <ol>
   *   <li>A host URL corresponding to the hosted server location, used to match world files to
   *       their subscription APIs. It is recommended to make this your server's eTLD+1 value.
   *   <li>For downloading and managing world files, a set of:
   *       <ol>
   *         <li>{@link WorldType} defining a logical grouping of worlds, matching on the world file
   *             "world ID" field
   *         <li>Subscription API URL where worlds JSON data is hosted for a given {@link WorldType}
   *         <li>A boolean flag indicating whether worlds for the type should be downloaded by
   *             default
   *       </ol>
   *       <i>See {@link #addDownloadURL(WorldType, String, boolean)}</i>
   * </ol>
   *
   * <p>For more information on Server Extension usage, see {@code README.md} located in the {@link
   * Client.Extensions} package.
   */
  public static class WorldSubscription {
    String hostUrl;
    Set<String> formerHostDomains = new HashSet<>();
    Map<WorldType, URI> worldDownloads = new LinkedHashMap<>();
    Map<WorldType, Boolean> worldDownloadDefaults = new LinkedHashMap<>();

    /**
     * Construct a new {@link WorldSubscription}
     *
     * @param hostUrl {@link String} value representing a host URL, may contain subdomains
     */
    private WorldSubscription(String hostUrl) {
      if (Util.isBlank(hostUrl)) {
        throw new IllegalArgumentException("Host URL must be defined");
      }

      this.hostUrl = hostUrl;
    }

    public Map<WorldType, URI> getWorldDownloadURIs() {
      return worldDownloads;
    }

    /**
     * Determine and retrieve the download location for a world ID value
     *
     * @param worldId {@link String} representing the world ID value from a {@link World}
     * @return The matched {@link URI}
     */
    public URI getDownloadURIForWorldID(String worldId) {
      Optional<Map.Entry<WorldType, URI>> typeURL =
          worldDownloads.entrySet().stream()
              .filter(entry -> entry.getKey().matchesWorldId(worldId))
              .findAny();

      return typeURL.map(Map.Entry::getValue).orElse(null);
    }

    public boolean shouldDownloadWorldsByDefault(WorldType worldType) {
      return worldDownloadDefaults.get(worldType);
    }

    public String getHostUrl() {
      return hostUrl;
    }

    public boolean formerDomainMatch(World world) {
      return formerHostDomains.stream().anyMatch(world::hostMatches);
    }

    public Set<String> getFormerHostDomains() {
      return formerHostDomains;
    }

    /**
     * <b>ONLY TO BE USED FOR EMERGENCY SITUATIONS</b>
     *
     * <p>Adds a previously-used host URL to a world subscription, to be used when a world sub host
     * URL needs to change.
     *
     * <p>If for a <b>very good</b> reason it no longer becomes acceptable to continue using a
     * particular host domain for a specific world subscription, it is imperative to perform the
     * following steps:
     *
     * <ol>
     *   <li>Update the host URL for worlds defined in the actual world subscription endpoint
     *   <li>Update the host URL value for the {@link WorldSubscription} declaration in the
     *       extension's setup helper method
     *   <li>Invoke this method on the object, providing the former domain value as the argument
     *   <li>Request that a new version of RSCPlus be released, containing the updated host names
     * </ol>
     *
     * Until both the client and the world subscription host agree on a common host domain, users
     * will be blocked from accessing the world, unless they do not update the application AND the
     * subscription API cannot be reached. As such, this method is to only be used in dire
     * situations, such as domain theft, where user's credentials are at risk.
     *
     * @param formerHostUrl The former host URL
     */
    private void addFormerHostUrl(String formerHostUrl) {
      if (Util.isBlank(formerHostUrl)) {
        throw new IllegalArgumentException("Former host URL must not be null or empty");
      }

      if (Util.isLocalhost(formerHostUrl)) {
        throw new IllegalArgumentException("Former host URL must not be the local host");
      }

      // First, check for equality in case the values are not valid domains
      // (ip address, test value, etc.)
      if (formerHostUrl.equals(hostUrl)) {
        throw new IllegalArgumentException(
            "Cannot add a former host URL that matches the current host URL: ["
                + formerHostUrl
                + "]");
      }

      final String formerHostDomainValue = getHostDomain(formerHostUrl);
      final String currentHostDomainValue = getHostDomain(hostUrl);

      if (formerHostDomainValue.equals(currentHostDomainValue)) {
        throw new IllegalArgumentException(
            "Cannot add a former host URL that matches the current host domain: ["
                + formerHostUrl
                + "]");
      }

      formerHostDomains.add(formerHostDomainValue);
    }

    /**
     * Defines a world data endpoint for a defined {@link WorldSubscription}
     *
     * <p>For more information on Server Extension usage, see {@code README.md} located in the
     * {@link Client.Extensions} package.
     *
     * @param worldType The {@link WorldType} defining a logical grouping of worlds, matching on the
     *     world file "world ID" field
     * @param urlString {@link String} value for the actual endpoint URL where worlds JSON data will
     *     be hosted for the above world type
     * @param isDefault {@code boolean} indicating whether worlds belonging to this type should be
     *     downloaded by default
     */
    private void addDownloadURL(WorldType worldType, String urlString, boolean isDefault) {
      if (Util.isBlank(urlString)) {
        throw new IllegalArgumentException("Subscription URL must not be null or empty");
      }

      final URI uri = URI.create(urlString);

      if (worldDownloads.containsKey(worldType)) {
        throw new IllegalArgumentException(
            "Attempting to add duplicate World Type: [" + worldType + "]");
      }

      if (worldDownloads.containsValue(uri)) {
        throw new IllegalArgumentException(
            "Attempting to add duplicate download URL: [" + urlString + "]");
      }

      worldDownloads.put(worldType, uri);
      worldDownloadDefaults.put(worldType, isDefault);
    }
  }

  /**
   * Configuration data required when distributing a custom RSCPlus binary for download
   *
   * <p><b>The following is required to utilize this feature:</b>
   *
   * <ol>
   *   <li>A prefix string, used for namespacing config data on disk, e.g. {@code
   *       ~/.config/<some>RSCPlus} and updating display labels throughout the client
   *   <li>For performing application updates, a set of:
   *       <ol>
   *         <li>A base URL where binary downloads and versions will be located
   *         <li>The supported os/architecture type identified by {@link BINARY_TYPE}
   *         <li>Name of the download file
   *         <li>Name of the version file
   *       </ol>
   *       <i>See {@link #addBinaryDownload(BINARY_TYPE, String, String)}</i>
   * </ol>
   *
   * <p>For more information on Server Extension usage, see {@code README.md} located in the {@link
   * Client.Extensions} package.
   */
  public static class BinaryInfo {
    static final String LINUX_APP_IMAGE_SUFFIX = ".new";

    private final String configPrefix;
    private final String downloadURI;
    private final Map<BINARY_TYPE, String> binaryDownloads = new HashMap<>();
    private final Map<BINARY_TYPE, String> binaryVersions = new HashMap<>();

    /**
     * Constructor for {@link BinaryInfo}
     *
     * @param configPrefix prefix for the binary, used for config directory naming and for updating
     *     the displayable application name throughout the client
     * @param downloadURI Base URL for all available binary downloads
     */
    private BinaryInfo(String configPrefix, String downloadURI) {
      if (configPrefix == null) {
        throw new IllegalArgumentException("Config prefix must not be null");
      }

      if (configPrefix.length() > 4) {
        throw new IllegalArgumentException("Config prefix must be 4 characters or less");
      }

      if (Config.getBinaryInfoMap().entrySet().stream()
          .anyMatch(entry -> entry.getValue().getConfigPrefix().equals(configPrefix))) {
        throw new RuntimeException("Config prefix must be unique");
      }

      if (downloadURI == null) {
        throw new RuntimeException("Download URI must not be null");
      }

      if (!downloadURI.endsWith("/")) {
        downloadURI += "/";
      }

      this.configPrefix = configPrefix;
      this.downloadURI = downloadURI;
    }

    public String getConfigPrefix() {
      return configPrefix;
    }

    public String getDownloadURI() {
      return downloadURI;
    }

    public Map<BINARY_TYPE, String> getBinaryDownloads() {
      return binaryDownloads;
    }

    /**
     * Uses information about the user's system to fetch the appropriate binary download location
     *
     * @return {@link String} containing the file name
     */
    public String getOSVersionFileName() {
      if (Launcher.isUsingAppImage()) {
        return binaryVersions.get(BINARY_TYPE.LINUX_APP_IMAGE);
      }

      if (Util.isMacOS()) {
        if (System.getProperty("os.arch").contains("aarch")) {
          return binaryVersions.get(BINARY_TYPE.MACOS_ARM);
        } else {
          return binaryVersions.get(BINARY_TYPE.MACOS_X64);
        }
      }

      if (Util.isWindowsOS()) {
        if (System.getProperty("os.arch").contains("64")) {
          return binaryVersions.get(BINARY_TYPE.WINDOWS_X64);
        } else {
          return binaryVersions.get(BINARY_TYPE.WINDOWS_X32);
        }
      }

      return null;
    }

    /**
     * Defines a new binary download
     *
     * @param binaryType {@link BINARY_TYPE} constant representing the OS/architecture combination
     * @param fileName {@link String} representing the binary download file name
     * @param versionFileName {@link String} representing the binary version file name
     */
    private void addBinaryDownload(
        BINARY_TYPE binaryType, String fileName, String versionFileName) {
      if (binaryDownloads.containsKey(binaryType)) {
        throw new IllegalArgumentException(
            "Attempting to add duplicate binary type: [" + binaryType + "]");
      }

      binaryDownloads.put(binaryType, fileName);
      binaryVersions.put(binaryType, versionFileName);
    }
  }

  /** Possible supported binary download types */
  public enum BINARY_TYPE {
    WINDOWS_X64,
    WINDOWS_X32,
    MACOS_ARM,
    MACOS_X64,
    LINUX_APP_IMAGE
  }

  /**
   * Server extension configuration and validation
   *
   * <p><b>Note:</b> Internal use only!
   */
  private static class Config {
    private static final Set<Extension> knownExtensions = new HashSet<>();
    private static final Map<Extension, WorldSubscription> worldSubscriptionMap = new HashMap<>();
    private static final Map<Extension, BinaryInfo> binaryInfoMap = new HashMap<>();

    public static Map<Extension, WorldSubscription> getWorldSubscriptionMap() {
      return worldSubscriptionMap;
    }

    /**
     * Retrieves the extension configured for a world subscription
     *
     * @param worldSub The provided {@link WorldSubscription}
     * @return The {@link Extension} tied to the {@link WorldSubscription}
     */
    public static Extension getExtensionForWorldSub(WorldSubscription worldSub) {
      return worldSubscriptionMap.entrySet().stream()
          .filter(entry -> entry.getValue().equals(worldSub))
          .findFirst()
          .map(Map.Entry::getKey)
          .orElse(null);
    }

    public static Map<Extension, BinaryInfo> getBinaryInfoMap() {
      return binaryInfoMap;
    }

    /**
     * Registers a new extension to use throughout the application
     *
     * @param extension The {@link Extension} to register
     */
    private static void registerExtension(Extension extension) {
      if (extension == null) {
        throw new IllegalArgumentException("The extension must not be null");
      }

      if (extension.equals(NONE)) {
        throw new IllegalArgumentException("Cannot register the NONE extension");
      }

      if (extension.equals(RSCPLUS) && !knownExtensions.isEmpty()) {
        throw new IllegalStateException("The default extension must be defined first!");
      }

      if (knownExtensions.contains(extension)) {
        throw new IllegalArgumentException(
            "Attempting to re-define extension: [" + extension + "]");
      }

      knownExtensions.add(extension);
    }

    /**
     * Defines a world subscription for a provided extension
     *
     * @param extension The provided {@link Extension}
     * @param worldSub The provided {@link WorldSubscription}
     */
    private static void setWorldSubscription(Extension extension, WorldSubscription worldSub) {
      if (extension == null || worldSub == null) {
        throw new IllegalArgumentException("The extension and world subscription must not be null");
      }

      if (extension.equals(NONE) || extension.equals(RSCPLUS)) {
        throw new IllegalArgumentException(
            "Attempting to set a world subscription for an internal extension");
      }

      if (isCommonExtension(extension)) {
        throw new IllegalArgumentException(
            "Cannot register a world subscription for a common extension: [" + extension + "]");
      }

      if (!knownExtensions.contains(extension)) {
        throw new IllegalArgumentException(
            "Attempting to set world subscription for undefined extension: [" + extension + "]");
      }

      if (worldSubscriptionMap.containsKey(extension)) {
        throw new IllegalArgumentException(
            "Attempting to re-define world subscription for extension: [" + extension + "]");
      }

      worldSubscriptionMap.put(extension, worldSub);
    }

    /**
     * Defines binary download information for a provided extension
     *
     * @param extension The provided {@link Extension}
     * @param binaryInfo The provided {@link BinaryInfo}
     */
    private static void setBinaryInfo(Extension extension, BinaryInfo binaryInfo) {
      if (extension == null || binaryInfo == null) {
        throw new IllegalArgumentException("The extension and binary info must not be null");
      }

      if (extension.equals(NONE)) {
        throw new IllegalArgumentException("Attempting to set binary info for the NONE extension");
      }

      if (isCommonExtension(extension)) {
        throw new IllegalArgumentException(
            "Cannot register binary information for a common extension: [" + extension + "]");
      }

      if (!knownExtensions.contains(extension)) {
        throw new IllegalArgumentException(
            "Attempting to set binary info for undefined extension: [" + extension + "]");
      }

      if (binaryInfoMap.containsKey(extension)) {
        throw new IllegalArgumentException(
            "Attempting to re-define binary info for extension: [" + extension + "]");
      }

      binaryInfoMap.put(extension, binaryInfo);
    }

    /** As the last step in the bootstrapping process, validate all defined information */
    private static void validateDeveloperExtensionRegistration() {
      // Ensure the RSCPlus extension was registered
      if (!knownExtensions.contains(RSCPLUS)) {
        throw new RuntimeException("The RSCPlus extension must be registered");
      }

      // Ensure all non-special extensions were properly defined as exclusive or common types
      for (Extension extension : knownExtensions) {
        if (RSCPLUS.equals(extension)) {
          continue;
        }

        if (!isCommonExtension(extension) && !worldSubscriptionMap.containsKey(extension)) {
          throw new RuntimeException(
              "Extension ["
                  + extension
                  + "] must register a World Subscription or be declared as a common extension");
        }
      }

      // Ensure world subscription was initialized correctly
      final Set<String> allCurrentHostDomains = new HashSet<>();
      final Set<String> allFormerHostDomains = new HashSet<>();
      worldSubscriptionMap.forEach(
          (extension, worldSub) -> {
            if (worldSub.getWorldDownloadURIs().isEmpty()) {
              throw new RuntimeException(
                  "WorldSubscription must contain at least one download URL for extension: ["
                      + extension
                      + "]");
            }

            if (!worldSub.worldDownloadDefaults.containsValue(true)) {
              throw new RuntimeException(
                  "At least one world type must be downloaded by default for extension: ["
                      + extension
                      + "]");
            }

            String hostDomain = getHostDomain(worldSub.getHostUrl());
            if (allCurrentHostDomains.contains(hostDomain)) {
              throw new RuntimeException(
                  "Host domains be unique across world subscriptions: ["
                      + worldSub.getHostUrl()
                      + "]");
            } else {
              allCurrentHostDomains.add(hostDomain);
            }

            Set<String> formerHostDomains = worldSub.getFormerHostDomains();
            formerHostDomains.forEach(
                formerHostDomain -> {
                  if (allFormerHostDomains.contains(getHostDomain(formerHostDomain))) {
                    throw new RuntimeException(
                        "Former host domains be unique across world subscriptions: ["
                            + formerHostDomain
                            + "]");
                  } else {
                    allFormerHostDomains.add(formerHostDomain);
                  }
                });
          });

      worldSubscriptionMap.forEach(
          (extension, worldSub) -> {
            if (allFormerHostDomains.contains(getHostDomain(worldSub.getHostUrl()))) {
              throw new RuntimeException(
                  "Former host domains must not match any active world sub host url: ["
                      + worldSub.getHostUrl()
                      + "]");
            }
          });

      binaryInfoMap.forEach(
          (extension, binaryInfo) -> {
            if (binaryInfo.getBinaryDownloads().isEmpty()) {
              throw new RuntimeException(
                  "BinaryInfo must contain at least one download URL for extension: ["
                      + extension
                      + "]");
            }
          });
    }
  }
}
