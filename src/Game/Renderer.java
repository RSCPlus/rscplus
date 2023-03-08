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

import Client.HiscoresURL;
import Client.ImageManip;
import Client.Launcher;
import Client.Logger;
import Client.NotificationsHandler;
import Client.NotificationsHandler.NotifType;
import Client.ScaledWindow;
import Client.Settings;
import Client.Util;
import Client.WikiURL;
import Client.WorldMapWindow;
import Game.MouseHandler.BufferedMouseClick;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImageConsumer;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.imageio.ImageIO;

/** Handles rendering overlays and client adjustments based on window size */
public class Renderer {

  public static Object instance = null;
  public static Graphics graphicsInstance = null;

  public static int width;
  public static int height;
  public static int height_client;
  public static int[] pixels;
  public static int sprite_media;

  public static int rs2hd_color_skyoverworld = 0xFFCAC1AB;
  public static int rs2hd_color_skyunderground = 0xFF1D150E;

  // Screen clear color, stored same way rsc stores colors
  private static int clearColor = 0;

  public static int fps;
  public static float alpha_time;
  public static float delta_time;
  public static long time;

  public static ImageConsumer image_consumer = null;

  public static Color color_dynamic;
  public static Color color_text = new Color(240, 240, 240);
  public static Color color_shadow = new Color(15, 15, 15);
  public static Color color_gray = new Color(60, 60, 60);
  public static Color color_hp = new Color(0, 210, 0);
  public static Color color_fatigue = new Color(210, 210, 0);
  public static Color color_prayer = new Color(160, 160, 210);
  public static Color color_low = new Color(255, 0, 0);
  public static Color color_poison = new Color(155, 205, 50);
  public static Color color_item = new Color(245, 245, 245);
  public static Color color_item_highlighted = new Color(245, 196, 70);
  public static Color color_replay = new Color(100, 185, 178);
  public static Color color_white = new Color(255, 255, 255);
  public static Color color_yellow = new Color(255, 255, 0);

  public static Image image_border;
  public static Image image_bar_frame;
  public static Image image_bar_frame_short;
  public static Image image_cursor;
  public static Image image_highlighted_item;
  public static Image image_wiki_hbar_inactive_system;
  public static Image image_wiki_hbar_active_system;
  public static Image image_wiki_hbar_inactive_jf;
  public static Image image_wiki_hbar_active_jf;
  public static Image image_wiki_hbar_inactive;
  public static Image image_wiki_hbar_active;
  private static BufferedImage game_image;
  public static int imageType;
  public static float renderingScalar;
  public static final float minScalar = 1.0f;
  public static final float maxIntegerScalar = 15.0f;
  public static final float maxInterpolationScalar = 5.0f;

  private static Dimension new_size = new Dimension(0, 0);

  private static Item last_item;

  public static Font font_main;
  public static Font font_big;

  private static int frames = 0;
  private static long fps_timer = 0;
  // Note: this should only ever be called in one place to guarantee queue integrity
  private static final Queue<Boolean> screenshotFlagBuffer = new LinkedList<>();
  public static int videorecord = 0;
  public static int videolength = 0;
  public static int screenshot_scenery_angle = 0;
  public static int screenshot_scenery_frames = 0;
  public static int screenshot_scenery_scenery_id = 0;
  public static int screenshot_scenery_scenery_rotation = 0;
  public static int screenshot_scenery_zoom = 750;
  public static int screenshot_scenery_bgcolor = 0x00FF00FF;

  public static boolean combat_menu_shown = false;

  public static int replayOption = 0;

  public static String[] shellStrings;

  public static boolean quietScreenshot = false;

  public static Rectangle barBounds;
  public static Rectangle previousBounds;
  public static Rectangle slowForwardBounds;
  public static Rectangle playPauseBounds;
  public static Rectangle fastForwardBounds;
  public static Rectangle nextBounds;
  public static Rectangle stopBounds;
  public static Rectangle queueBounds;
  private static int shapeHeight;
  private static int shapeX;

  private static int sleepTimer = 0;
  private static boolean lastInterlace = false;

  private static int bankResetTimer = 0;
  private static boolean show_bank_last = false;

  public static int getFogColor(int attenuation, int val) {
    int clearColor = getClearColor();

    // Return normal fog
    if (clearColor == 0 || Client.state == Client.STATE_LOGIN) return val + attenuation;

    // Disable fog for custom colors
    return attenuation;
  }

  public static int getClearColor() {
    // Login screen needs black background
    if (Client.state == Client.STATE_LOGIN) return 0;

    // Character creation needs black background
    if (Client.show_appearance) return 0;

    return clearColor;
  }

  public static void setClearColor(int color) {
    clearColor = color;
  }

  public static void init() {
    // patch copyright to match the year that jagex took down RSC
    shellStrings[23] = shellStrings[23].replaceAll("2015", "2018");

    // Resize game window
    new_size.width = 512;
    new_size.height = 346;

    handle_resize();

    // Set the rendering scalar according to the user's options
    updateRenderingScalarAndResize(true);

    // Load fonts
    try {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      InputStream is = Launcher.getResourceAsStream("/assets/Helvetica-Bold.ttf");
      Font font = Font.createFont(Font.TRUETYPE_FONT, is);
      ge.registerFont(font);
      font_main = font.deriveFont(Font.PLAIN, 11.0f);
      font_big = font.deriveFont(Font.PLAIN, 22.0f);

      is = Launcher.getResourceAsStream("/assets/TimesRoman.ttf");
      ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Load images
    try {
      image_border = ImageIO.read(Launcher.getResource("/assets/hbar/border.png"));
      image_bar_frame = ImageIO.read(Launcher.getResource("/assets/hbar/bar.png"));
      image_bar_frame_short = ImageIO.read(Launcher.getResource("/assets/hbar/bar_short.png"));
      image_wiki_hbar_inactive_system =
          ImageIO.read(Launcher.getResource("/assets/hbar/wiki_hbar_inactive.png"));
      image_wiki_hbar_active_system =
          ImageIO.read(Launcher.getResource("/assets/hbar/wiki_hbar_active.png"));
      image_wiki_hbar_inactive_jf =
          ImageIO.read(Launcher.getResource("/assets/hbar/wiki_hbar_inactive_jf.png"));
      image_wiki_hbar_active_jf =
          ImageIO.read(Launcher.getResource("/assets/hbar/wiki_hbar_active_jf.png"));
      GameApplet.syncWikiHbarImageWithFontSetting();
      image_cursor = ImageIO.read(Launcher.getResource("/assets/cursor.png"));
      image_highlighted_item = ImageIO.read(Launcher.getResource("/assets/highlighted_item.png"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Updates the rendering scalar and resizes the window accordingly */
  private static void updateRenderingScalarAndResize(boolean onInit) {
    float scalar = getRenderingScalar();
    imageType = getImageType();

    // Reset the game image with the current type to ensure that affineOp
    // scaling will always have matching source and destination types
    game_image = new BufferedImage(width, height, imageType);

    if (scalar != renderingScalar) {
      // Handle rendering scalar value changes
      renderingScalar = scalar;

      // Only reset the custom window size spinners after initial loading;
      // whatever was in the settings file before exiting last time can be
      // assumed to be valid on startup
      if (!onInit) {
        ScaledWindow.getInstance().updateCustomWindowSizeFromSettings();
      }

      // Resize window only after it has begun rendering the game image,
      // (ie. not the loading screen)
      if (ScaledWindow.getInstance().isViewportLoaded()) {
        ScaledWindow.getInstance().resizeWindowToScalar();
      }
    } else {
      // Handle window size changes when the scalar did not change,
      // typically due to settings window changes
      if (Settings.CUSTOM_CLIENT_SIZE.get(Settings.currentProfile)) {
        int customClientWidth = Settings.CUSTOM_CLIENT_SIZE_X.get(Settings.currentProfile);
        int customClientHeight = Settings.CUSTOM_CLIENT_SIZE_Y.get(Settings.currentProfile);

        Dimension maxWindowDimensions = ScaledWindow.getInstance().getMaximumEffectiveWindowSize();
        int maxWindowWidth = maxWindowDimensions.width;
        int maxWindowHeight = maxWindowDimensions.height;

        // If window is maximized in the Windows OS sense and custom size matches
        // max window boundaries, don't resize or realign the window
        if (ScaledWindow.getInstance().getExtendedState() == Frame.MAXIMIZED_BOTH
            && customClientWidth == maxWindowWidth
            && customClientHeight == maxWindowHeight) {
          return;
        }

        int frameWidth = customClientWidth + ScaledWindow.getInstance().getWindowWidthInsets();
        int frameHeight = customClientHeight + ScaledWindow.getInstance().getWindowHeightInsets();

        // Only set the size and align the window if the window actually changed sizes
        if (ScaledWindow.getInstance().getWidth() != frameWidth
            || ScaledWindow.getInstance().getHeight() != frameHeight) {
          ScaledWindow.getInstance().setWindowRealignmentIntent(true);
          ScaledWindow.getInstance().setSize(new Dimension(frameWidth, frameHeight));
        }
      }
    }
  }

  public static void resize(int w, int h) {
    new_size.width = Math.max(w, 512);
    new_size.height = Math.max(h, 346);
  }

  public static void handle_resize() {
    width = new_size.width;
    height = new_size.height;

    height_client = height - 12;
    pixels = new int[width * height];

    game_image = new BufferedImage(width, height, getImageType());

    Camera.resize();
    Menu.resize();

    if (image_consumer != null) image_consumer.setDimensions(width, height);

    if (Client.strings != null)
      Client.strings[262] =
          fixLengthString("~" + (Renderer.width - (512 - 439)) + "~@whi@Remove         WWWWWWWWWW");

    if (Renderer.instance != null && Reflection.setGameBounds != null) {
      try {
        Reflection.setGameBounds.invoke(
            Renderer.instance, 0, Renderer.width, Renderer.height, 0, (byte) 119);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Grab the scaling algorithm from the settings, defaulting to {@link BufferedImage#TYPE_INT_RGB}
   */
  private static int getImageType() {
    if (Settings.SCALED_CLIENT_WINDOW.get(Settings.currentProfile)) {
      if (Settings.SCALING_ALGORITHM
          .get(Settings.currentProfile)
          .equals(AffineTransformOp.TYPE_NEAREST_NEIGHBOR)) {
        return BufferedImage.TYPE_INT_RGB;
      } else if (Settings.SCALING_ALGORITHM
          .get(Settings.currentProfile)
          .equals(AffineTransformOp.TYPE_BILINEAR)) {
        return BufferedImage.TYPE_3BYTE_BGR;
      } else if (Settings.SCALING_ALGORITHM
          .get(Settings.currentProfile)
          .equals(AffineTransformOp.TYPE_BICUBIC)) {
        return BufferedImage.TYPE_3BYTE_BGR;
      }
    }

    return BufferedImage.TYPE_INT_RGB;
  }

  /**
   * Grab the rendering scalar from the settings, depending on the chosen scaling algorithm.
   * Defaults to {@code 1.0f}
   */
  private static float getRenderingScalar() {
    if (Settings.SCALED_CLIENT_WINDOW.get(Settings.currentProfile)) {
      if (Settings.SCALING_ALGORITHM
          .get(Settings.currentProfile)
          .equals(AffineTransformOp.TYPE_NEAREST_NEIGHBOR)) {
        return Settings.INTEGER_SCALING_FACTOR.get(Settings.currentProfile);
      } else if (Settings.SCALING_ALGORITHM
          .get(Settings.currentProfile)
          .equals(AffineTransformOp.TYPE_BILINEAR)) {
        return Settings.BILINEAR_SCALING_FACTOR.get(Settings.currentProfile);
      } else if (Settings.SCALING_ALGORITHM
          .get(Settings.currentProfile)
          .equals(AffineTransformOp.TYPE_BICUBIC)) {
        return Settings.BICUBIC_SCALING_FACTOR.get(Settings.currentProfile);
      }
    }

    return 1.0f;
  }

  private static int lastPercentHP = 100;
  private static int lastFatigue = 0;

  private static float lastBaseDrainRate = 0;
  private static float lastAdjustedDrainRate = 0;

  public static void present(Image image) {
    // Update timing
    long new_time = System.currentTimeMillis();
    delta_time = (float) (new_time - time) / 1000.0f;
    time = new_time;
    alpha_time = 0.25f + (((float) Math.sin(time / 100) + 1.0f) / 2.0f * 0.75f);

    // Reset dialogue option after force pressed in replay
    if (Replay.isPlaying && KeyboardHandler.dialogue_option != -1)
      KeyboardHandler.dialogue_option = -1;

    if (Settings.renderingScalarUpdateRequired) {
      updateRenderingScalarAndResize(false);
      Settings.renderingScalarUpdateRequired = false;
    }

    Graphics2D g2 =
        (Graphics2D) game_image.getGraphics(); // TODO: Declare g2 outside of the present method
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setFont(font_main);

    g2.drawImage(image, 0, 0, null);
    int startingPixel = Settings.PATCH_HBAR_512_LAST_PIXEL.get(Settings.currentProfile) ? 511 : 512;
    g2.drawImage(image_border, startingPixel, height - 13, width - startingPixel, 13, null);

    // In-game UI

    // Attempt to grab a buffered mouse click
    BufferedMouseClick bufferedMouseClick = MouseHandler.getBufferedMouseClick();

    // Grab the screenshot intent from the buffer
    boolean screenshot = getScreenshotFlag();

    if (Client.state == Client.STATE_GAME) {
      int npcCount = 0;
      int playerCount = 0;

      // Update player coords
      for (Iterator<NPC> iterator = Client.npc_list.iterator(); iterator.hasNext(); ) {
        NPC npc = iterator.next(); // TODO: Remove unnecessary allocations
        if (npc != null) {
          if (npc.type == NPC.TYPE_PLAYER) playerCount++;
          else if (npc.type == NPC.TYPE_MOB) npcCount++;

          if (Client.player_name.equals(npc.name)) {
            Client.player_posX = npc.x;
            Client.player_posY = npc.y;
            Client.player_height = npc.height;
            Client.player_width = npc.width;

            Client.isGameLoaded = true;
          }
        }
      }

      if (!Client.isInterfaceOpen() && Client.show_menu == Client.MENU_NONE) {
        List<Rectangle> npc_hitbox = new ArrayList<>();
        List<Rectangle> player_hitbox = new ArrayList<>();
        List<Point> entity_text_loc = new ArrayList<>();

        Client.npc_list.sort(
            Comparator.comparing(npc -> npc.name, Comparator.nullsLast(Comparator.naturalOrder())));

        for (Iterator<NPC> iterator = Client.npc_list.iterator(); iterator.hasNext(); ) {
          NPC npc = iterator.next(); // TODO: Remove unnecessary allocations
          Color color = color_low;

          boolean showName = false;
          if (npc.type == NPC.TYPE_PLAYER 
              && npc.name != null
              && Client.player_name != null
              && !npc.name.equals(Client.player_name)) {
            color = color_fatigue;

            if (Client.isFriend(npc.name)) {
              color = color_hp;
              if ((Settings.SHOW_FRIEND_NAME_OVERLAY.get(Settings.currentProfile)
                  || Settings.SHOW_PLAYER_NAME_OVERLAY.get(Settings.currentProfile))) {
                showName = true;
              }
            } else if (Settings.SHOW_PLAYER_NAME_OVERLAY.get(Settings.currentProfile)) {
              showName = true;
            }
          } else if (npc.type == NPC.TYPE_MOB
              && Settings.SHOW_NPC_NAME_OVERLAY.get(Settings.currentProfile)) {
            showName = true;
          }

          if (Settings.SHOW_HITBOX.get(Settings.currentProfile)) {
            List<Rectangle> hitbox = player_hitbox;
            boolean showHitbox = true;

            if (npc.type == NPC.TYPE_MOB) hitbox = npc_hitbox;

            for (Iterator<Rectangle> boxIterator = hitbox.iterator(); boxIterator.hasNext(); ) {
              Rectangle rect = boxIterator.next(); // TODO: Remove unnecessary allocations
              if (rect.x == npc.x
                  && rect.y == npc.y
                  && rect.width == npc.width
                  && rect.height == npc.height) {
                showHitbox = false;
                break;
              }
            }

            if (showHitbox) {
              setAlpha(g2, 0.3f);
              g2.setColor(color);
              g2.fillRect(npc.x, npc.y, npc.width, npc.height);
              g2.setColor(Color.BLACK);
              g2.drawRect(npc.x, npc.y, npc.width, npc.height);
              setAlpha(g2, 1.0f);
              hitbox.add(new Rectangle(npc.x, npc.y, npc.width, npc.height));
            }
          }

          // draw npc's name
          if (showName && npc.name != null) {
            int x = npc.x + (npc.width / 2);
            int y = npc.y - 20;
            for (Iterator<Point> locIterator = entity_text_loc.iterator();
                locIterator.hasNext(); ) {
              Point loc = locIterator.next(); // TODO: Remove unnecessary allocations
              if (loc.x == x && loc.y == y) y -= 12;
            }

            String text = npc.name;
            if (Settings.EXTEND_IDS_OVERLAY.get(Settings.currentProfile)) {
              text += (" (" + npc.id + "-" + npc.id2 + ")");
            }
            drawShadowText(g2, text, x, y, color, true);
            entity_text_loc.add(new Point(x, y));
          }
        }

        List<Rectangle> item_hitbox = new ArrayList<>();
        List<Point> item_text_loc = new ArrayList<>();

        if (Settings.SHOW_ITEM_GROUND_OVERLAY.get(
            Settings.currentProfile)) { // Don't sort if we aren't displaying any item names anyway
          try {
            // Keep items in (technically reverse) alphabetical order for SHOW_ITEMINFO instead of
            // randomly changing places each frame
            Collections.sort(Client.item_list, new ItemComparator());
          } catch (Exception e) {
            // Sometimes Java helpfully complains that the sorting method violates its general
            // contract.
            e.printStackTrace();
          }
        }

        for (Iterator<Item> iterator = Client.item_list.iterator(); iterator.hasNext(); ) {
          Item item = iterator.next(); // TODO: Remove unnecessary allocations

          if (Settings.SHOW_HITBOX.get(Settings.currentProfile)) {
            boolean show = true;
            for (Iterator<Rectangle> boxIterator = item_hitbox.iterator();
                boxIterator.hasNext(); ) {
              Rectangle rect = boxIterator.next(); // TODO: Remove unnecessary allocations
              if (rect.x == item.x
                  && rect.y == item.y
                  && rect.width == item.width
                  && rect.height == item.height) {
                show = false;
                break;
              }
            }

            if (show) {
              setAlpha(g2, 0.3f);
              g2.setColor(color_prayer);
              g2.fillRect(item.x, item.y, item.width, item.height);
              g2.setColor(Color.BLACK);
              g2.drawRect(item.x, item.y, item.width, item.height);
              setAlpha(g2, 1.0f);
              item_hitbox.add(new Rectangle(item.x, item.y, item.width, item.height));
            }
          }

          if (Settings.SHOW_ITEM_GROUND_OVERLAY.get(Settings.currentProfile)) {
            int x = item.x + (item.width / 2);
            int y = item.y - 20;
            int freq = Collections.frequency(Client.item_list, item);

            // Check if item is in blocked list
            boolean itemIsBlocked =
                stringIsWithinList(item.getName(), Settings.BLOCKED_ITEMS.get("custom"));

            // We've sorted item list in such a way that it is possible to not draw the ITEMINFO
            // unless it's the first time we've tried to for this itemid at that location
            // by just using last_item.
            // last_item == null necessary in case only one item on screen is being rendered. slight
            // speed increase from freq == 1 if compiler can stop early in conditional.
            if ((freq == 1 || !item.equals(last_item) || last_item == null) && !itemIsBlocked) {
              for (Iterator<Point> locIterator = item_text_loc.iterator();
                  locIterator.hasNext(); ) {
                Point loc = locIterator.next(); // TODO: Remove unnecessary allocations
                if (loc.x == x && loc.y == y) {
                  y -= 12;
                }
              }
              item_text_loc.add(new Point(x, y));

              Color itemColor = color_item;
              String itemText = item.getName() + ((freq == 1) ? "" : " (" + freq + ")");

              // Check if item is in highlighted list
              if (stringIsWithinList(item.getName(), Settings.HIGHLIGHTED_ITEMS.get("custom"))) {
                itemColor = color_item_highlighted;
                drawHighlighImage(g2, itemText, x, y);
              }

              // Note that it is not possible to show how many of a
              //   stackable item are in a stack on the ground.
              // That information is not transmitted in RSC, just that the item ID is there.
              drawShadowText(g2, itemText, x, y, itemColor, true);
            }
            last_item = item; // Done with item this loop, can save it as last_item
          }
        }
      }

      // Clear item list for next frame
      Client.item_list_retained = new ArrayList<Item>(Client.item_list);
      Client.item_list.clear();
      last_item = null;

      if (!Client.show_sleeping && Settings.SHOW_INVCOUNT.get(Settings.currentProfile))
        drawShadowText(
            g2,
            Client.inventory_count + "/" + Client.max_inventory,
            width - 19,
            17,
            getInventoryCountColor(),
            true);

      int percentHP = 0;
      int percentPrayer = 0;
      float alphaHP = 1.0f;
      float alphaPrayer = 1.0f;
      float alphaFatigue = 1.0f;
      Color colorHP = color_hp;
      Color colorPrayer = color_prayer;
      Color colorFatigue = color_fatigue;

      if (Client.getBaseLevel(Client.SKILL_HP) > 0) {
        percentHP =
            Client.getCurrentLevel(Client.SKILL_HP) * 100 / Client.getBaseLevel(Client.SKILL_HP);
        percentPrayer =
            Client.getCurrentLevel(Client.SKILL_PRAYER)
                * 100
                / Client.getBaseLevel(Client.SKILL_PRAYER);
      }

      if (percentHP < 30) {
        colorHP = color_low;
        alphaHP = alpha_time;
      }

      if (percentPrayer < 30) {
        colorPrayer = color_low;
        alphaPrayer = alpha_time;
      }

      if (Client.getFatigue() >= 80) {
        colorFatigue = color_low;
        alphaFatigue = alpha_time;
      }

      // Low HP notification
      if (percentHP <= Settings.LOW_HP_NOTIF_VALUE.get(Settings.currentProfile)
          && lastPercentHP > percentHP
          && lastPercentHP > Settings.LOW_HP_NOTIF_VALUE.get(Settings.currentProfile))
        NotificationsHandler.notify(
            NotifType.LOWHP, "Low HP Notification", null, "Your HP is at " + percentHP + "%");
      lastPercentHP = percentHP;

      // High fatigue notification
      if (Client.getFatigue() >= Settings.FATIGUE_NOTIF_VALUE.get(Settings.currentProfile)
          && lastFatigue < Client.getFatigue()
          && lastFatigue < Settings.FATIGUE_NOTIF_VALUE.get(Settings.currentProfile))
        NotificationsHandler.notify(
            NotifType.FATIGUE,
            "High Fatigue Notification",
            null,
            "Your fatigue is at " + Client.getFatigue() + "%");
      lastFatigue = Client.getFatigue();

      // Draw HP, Prayer, Fatigue overlay
      int x = 24;
      int y = 28;

      // combat menu is showing, so move everything down
      if (combat_menu_shown) y = 132;

      // NPC Post-processing for ui
      if (Settings.SHOW_COMBAT_INFO.get(Settings.currentProfile) && !Client.isInterfaceOpen()) {
        int bar_count = 0;
        for (Iterator<NPC> iterator = Client.npc_list.iterator(); iterator.hasNext(); ) {
          NPC npc = iterator.next();
          if (npc != null && Client.isInCombatWithNPC(npc)) {
            drawNPCBar(g2, 7, y, npc);
            // Increment y by npc bar height, so we can have multiple bars
            // NOTE: We should never (?) have more than one npc health bar, so multiple bars
            // indicates that our combat detection isn't accurate
            y += 50;
            bar_count++;
          }
        }
        if (bar_count > 0) {
          y += 16;
        }
      }

      if (Settings.SHOW_HP_PRAYER_FATIGUE_OVERLAY.get(Settings.currentProfile)) {
        if (!roomInHbarForHPPrayerFatigueOverlay()) {
          if (!Client.isInterfaceOpen() && !Client.show_questionmenu) {
            setAlpha(g2, alphaHP);
            drawShadowText(
                g2,
                "Hits: "
                    + Client.current_level[Client.SKILL_HP]
                    + "/"
                    + Client.base_level[Client.SKILL_HP],
                x,
                y,
                colorHP,
                false);
            y += 16;
            setAlpha(g2, alphaPrayer);
            drawShadowText(
                g2,
                "Prayer: "
                    + Client.current_level[Client.SKILL_PRAYER]
                    + "/"
                    + Client.base_level[Client.SKILL_PRAYER],
                x,
                y,
                colorPrayer,
                false);
            y += 16;
            setAlpha(g2, alphaFatigue);
            drawShadowText(
                g2, "Fatigue: " + Client.getFatigue() + "/100", x, y, colorFatigue, false);
            setAlpha(g2, 1.0f);
            y += 16;
          }
        } else {
          boolean fullSizeBar = useFullSizeHPPrayerFatigueBars();
          int barPadding = fullSizeBar ? 4 : 3;
          Image used_bar_frame = fullSizeBar ? image_bar_frame : image_bar_frame_short;
          int barSize = barPadding + used_bar_frame.getWidth(null);
          int x2 = width - (barPadding + barSize);
          int y2 = height - used_bar_frame.getHeight(null);

          drawBar(g2, used_bar_frame, x2, y2, colorFatigue, alphaFatigue, Client.getFatigue(), 100);
          x2 -= barSize;

          drawBar(
              g2,
              used_bar_frame,
              x2,
              y2,
              colorPrayer,
              alphaPrayer,
              Client.current_level[Client.SKILL_PRAYER],
              Client.base_level[Client.SKILL_PRAYER]);
          x2 -= barSize;

          drawBar(
              g2,
              used_bar_frame,
              x2,
              y2,
              colorHP,
              alphaHP,
              Client.current_level[Client.SKILL_HP],
              Client.base_level[Client.SKILL_HP]);
          x2 -= barSize;
        }
      }

      // Draw last menu action
      if (!Client.isInterfaceOpen()
          && !Client.show_questionmenu
          && Settings.SHOW_LAST_MENU_ACTION.get(Settings.currentProfile)) {
        if (time <= Client.menu_timer) {
          drawShadowText(g2, Client.lastAction, x, y, color_text, false);
          y += 14;
        }
      }

      // Draw under combat style info
      // buffs, debuffs and cooldowns
      if (!Client.isInterfaceOpen() && Settings.SHOW_BUFFS.get(Settings.currentProfile)) {
        if (time <= Client.magic_timer) {
          float timer = (float) Math.ceil((Client.magic_timer - time) / 1000.0);
          drawShadowText(g2, "Magic Timer: " + (int) timer, x, y, color_text, false);
          y += 14;
        }

        for (int i = 0; i < 18; i++) {
          if (Client.current_level[i] != Client.base_level[i]
              && (i != Client.SKILL_HP && i != Client.SKILL_PRAYER)) {
            int diff = Client.current_level[i] - Client.base_level[i];
            Color color = color_low;

            // Build our boost string
            // If the difference is greater than 0 (positive boost), we need to add a "+" to the
            // string
            // Otherwise, it can be left alone because the integer will already have a "-" when
            // converted to a string
            String boost = Integer.toString(diff);
            if (diff > 0) {
              boost = "+" + boost;
              color = color_hp;
            }

            drawShadowText(g2, boost, x, y, color, false);
            drawShadowText(g2, Client.skill_name[i], x + 32, y, color, false);
            y += 14;
          }
        }

        int base_drain_rate = 0;
        float adjusted_drain_rate = 0;
        // 14 selectable prayers
        for (int i = 0; i < 14; i++) {
          if (Client.prayers_on[i] == true) base_drain_rate += Client.DRAIN_RATES[i];
        }
        lastBaseDrainRate = base_drain_rate;
        if (base_drain_rate != 0) {
          // with prayer equipment, combat rounds get increased about 3.1% per +1
          float factor = 1.0f;
          if (Client.current_equipment_stats[4] > 1) {
            int boost = Client.current_equipment_stats[4] - 1;
            float increase_rounds = boost * 0.031f;
            // percentage of increase per round
            factor = 1 + increase_rounds;
          }
          adjusted_drain_rate = base_drain_rate / factor;
          lastAdjustedDrainRate = adjusted_drain_rate;
          // a drain_rate of 60 drains 1 point in 3.33 secs
          // 75 drains 1.25 points in 3.33 secs -> 3.33/(adjusted/60)
          float points_psec = (3.33f * 60.0f) / adjusted_drain_rate;

          drawShadowText(g2, "-1", x, y, color_low, false);
          drawShadowText(
              g2, "Prayer/" + Client.trimNumber(points_psec, 1) + "s", x + 32, y, color_low, false);
          y += 14;
        } else {
          // no prayer armour adjusting drain rate
          lastAdjustedDrainRate = lastBaseDrainRate;
        }
        if (time > Client.poison_timer && Client.is_poisoned) {
          // more than 20 seconds passed and last status was poison, user probably is no longer
          // poisoned
          Client.is_poisoned = false;
          Client.poison_timer = time;
        }
        if (Client.is_poisoned) {
          drawShadowText(g2, "Poisoned!", x, y, color_poison, false);
          y += 14;
        }
      }

      // Clear npc list for the next frame
      Client.npc_list_retained = new ArrayList<NPC>(Client.npc_list);
      Client.npc_list.clear();

      // render XP bar/drop
      Client.processFatigueXPDrops();
      Client.xpdrop_handler.draw(g2);
      Client.xpbar.draw(g2, bufferedMouseClick);

      if (!Client.isSleeping()) {
        Client.updateCurrentFatigue();
      }

      // Make the reset buttons for filter & sort flash red a few frames when clicked
      if (Bank.buttonActive[5]
          || Bank.buttonActive[11]
          || (Bank.buttonActive[10] || Bank.disableUserButton)) {
        if (bankResetTimer > 3) {
          if (Bank.disableUserButton) {
            Bank.buttonActive[10] = false;
            Bank.disableUserButton = false;
          } else {
            Bank.buttonActive[5] = false;
            Bank.buttonActive[11] = false;
          }
          bankResetTimer = 0;
        } else {
          ++bankResetTimer;
        }
      }
      // Handles drawing bank value, bank sort panel, & bank filter panel
      Bank.drawBankAugmentations(g2, bufferedMouseClick);

      // World Map
      // Arrow marker for destination
      if (WorldMapWindow.getWaypointPosition() != null
          && !Client.isInterfaceOpen()
          && Client.show_menu == Client.MENU_NONE) {
        float absCameraRotation =
            ((((float) Camera.rotation / 255.0f) * 360.0f) + 180.0f)
                + WorldMapWindow.getWaypointAngle();
        if (WorldMapWindow.getWaypointFloor() != Client.planeIndex) setAlpha(g2, 0.5f);
        Image arrowSprite = WorldMapWindow.directions[Util.getAngleIndex(absCameraRotation)];
        x = (width / 2);
        y = 54;
        g2.drawImage(arrowSprite, x - 12, y - 12, 24, 24, null);
        y += -20;
        drawShadowText(
            g2,
            "" + Util.getAngleDirectionName(WorldMapWindow.getWaypointAngle()),
            x,
            y,
            Renderer.color_low,
            true);
        setAlpha(g2, 1.0f);
      }

      // RSC Wiki integration
      if (WikiURL.nextClickIsLookup && bufferedMouseClick.isMouseClicked()) {
        if (bufferedMouseClick.isRightClick()) {
          WikiURL.nextClickIsLookup = false;
          Client.displayMessage("Cancelled lookup.", Client.CHAT_NONE);
        } else {
          if (System.currentTimeMillis() - WikiURL.lastLookupTime > WikiURL.cooldownTimer) {
            if (!MouseText.isPlayer) {
              String wikiURL = WikiURL.getURL();
              if (!wikiURL.equals("INVALID")) {
                WikiURL.lastLookupTime = System.currentTimeMillis();
                Client.displayMessage(
                    "@whi@Looking up @gre@" + MouseText.name + "@whi@ on the wiki!",
                    Client.CHAT_NONE);
                Util.openLinkInBrowser(wikiURL);
                WikiURL.nextClickIsLookup = false;
              }
            } else {
              Client.displayMessage(
                  "@lre@Players cannot be looked up on the wiki, try again.", Client.CHAT_NONE);
            }
          } else {
            Client.displayMessage(
                String.format(
                    "@lre@Please wait %1d seconds between wiki queries",
                    WikiURL.cooldownTimer / 1000),
                Client.CHAT_NONE);
          }
        }
      }

      if (Settings.WIKI_LOOKUP_ON_HBAR.get(Settings.currentProfile)) {
        int xCoord = Client.wikiLookupReplacesReportAbuse() ? 410 : 410 + 90 + 12;
        int yCoord = height - 16;
        // Handle wiki lookup click
        if (bufferedMouseClick.getX() >= xCoord + 3 // + 3 for hbar shadow included in image
            && bufferedMouseClick.getX() <= xCoord + 3 + 90
            && bufferedMouseClick.getY() >= height - 16
            && bufferedMouseClick.getY() <= height
            && bufferedMouseClick.isMouseClicked()) {
          if (!bufferedMouseClick.isRightClick()) {
            Client.displayMessage(
                "Click on something to look it up on the wiki...", Client.CHAT_NONE);
            WikiURL.nextClickIsLookup = true;
          }
        }
        if (WikiURL.nextClickIsLookup) {
          g2.drawImage(image_wiki_hbar_active, xCoord, yCoord, 96, 15, null);
        } else {
          g2.drawImage(image_wiki_hbar_inactive, xCoord, yCoord, 96, 15, null);
        }
      }

      // Hiscores integration
      if (HiscoresURL.nextClickIsLookup && bufferedMouseClick.isMouseClicked()) {
        if (bufferedMouseClick.isRightClick()) {
          HiscoresURL.nextClickIsLookup = false;
          Client.displayMessage("Cancelled lookup.", Client.CHAT_NONE);
        } else {
          if (System.currentTimeMillis() - HiscoresURL.lastLookupTime > HiscoresURL.cooldownTimer) {
            if (MouseText.isPlayer) {
              String hiscoresURL = HiscoresURL.getURL();
              if (hiscoresURL.equals("NO_URL")) {
                Client.displayMessage(
                    "@lre@No hiscores URL defined for this world.", Client.CHAT_NONE);
                Client.displayMessage(
                    "@lre@You can change this from the World List tab in settings.",
                    Client.CHAT_NONE);
              } else {
                if (!hiscoresURL.equals("INVALID")) {
                  HiscoresURL.lastLookupTime = System.currentTimeMillis();
                  Client.displayMessage(
                      "@whi@Looking up @gre@" + MouseText.name + "@whi@ on the hiscores!",
                      Client.CHAT_NONE);
                  Util.openLinkInBrowser(hiscoresURL);
                  HiscoresURL.nextClickIsLookup = false;
                }
              }
            } else {
              Client.displayMessage(
                  "@lre@Only players can be looked up on the hiscores, try again.",
                  Client.CHAT_NONE);
            }
          } else {
            Client.displayMessage(
                String.format(
                    "@lre@Please wait %1d seconds between hiscores queries",
                    WikiURL.cooldownTimer / 1000),
                Client.CHAT_NONE);
          }
        }
      }

      // Interface rsc+ buttons
      if (Settings.RSCPLUS_BUTTONS_FUNCTIONAL.get(Settings.currentProfile)
          || Settings.SHOW_RSCPLUS_BUTTONS.get(Settings.currentProfile)) {

        // Map Button
        Rectangle mapButtonBounds = new Rectangle(width - 68, 3, 32, 32);
        if ((!show_bank_last || mapButtonBounds.x >= 460) && !Client.show_sleeping) {
          if (Settings.SHOW_RSCPLUS_BUTTONS.get(Settings.currentProfile)) {
            g2.setColor(Renderer.color_text);
            g2.drawLine(
                mapButtonBounds.x + 4,
                mapButtonBounds.y + 1,
                mapButtonBounds.x + 4,
                mapButtonBounds.y + 1 + 6);
            g2.drawLine(
                mapButtonBounds.x + 1,
                mapButtonBounds.y + 4,
                mapButtonBounds.x + 7,
                mapButtonBounds.y + 4);
          }

          // Handle map button click
          if (bufferedMouseClick.getX() >= mapButtonBounds.x
              && bufferedMouseClick.getX() <= mapButtonBounds.x + mapButtonBounds.width
              && bufferedMouseClick.getY() >= mapButtonBounds.y
              && bufferedMouseClick.getY() <= mapButtonBounds.y + mapButtonBounds.height
              && bufferedMouseClick.isMouseClicked()) {

            Launcher.getWorldMapWindow().toggleWorldMapWindow();
          }
        }

        // Settings
        mapButtonBounds = new Rectangle(width - 200, 3, 32, 32);
        if ((!show_bank_last || mapButtonBounds.x >= 460) && !Client.show_sleeping) {
          if (Settings.SHOW_RSCPLUS_BUTTONS.get(Settings.currentProfile)) {
            g2.setColor(Renderer.color_text);
            g2.drawLine(
                mapButtonBounds.x + 4,
                mapButtonBounds.y + 1,
                mapButtonBounds.x + 4,
                mapButtonBounds.y + 1 + 6);
            g2.drawLine(
                mapButtonBounds.x + 1,
                mapButtonBounds.y + 4,
                mapButtonBounds.x + 7,
                mapButtonBounds.y + 4);
          }

          // Handle settings button click
          if (bufferedMouseClick.getX() >= mapButtonBounds.x
              && bufferedMouseClick.getX() <= mapButtonBounds.x + mapButtonBounds.width
              && bufferedMouseClick.getY() >= mapButtonBounds.y
              && bufferedMouseClick.getY() <= mapButtonBounds.y + mapButtonBounds.height
              && bufferedMouseClick.isMouseClicked()) {
            Launcher.getConfigWindow().toggleConfigWindow();
          }
        }

        // wiki button on magic book (Good for in-replay but not good if spam casting magic)
        if (Settings.WIKI_LOOKUP_ON_MAGIC_BOOK.get(Settings.currentProfile)) {
          mapButtonBounds = new Rectangle(width - 68 - 66, 3, 32, 32);
          if ((!show_bank_last || mapButtonBounds.x >= 460) && !Client.show_sleeping) {
            if (Settings.SHOW_RSCPLUS_BUTTONS.get(Settings.currentProfile)) {
              g2.setColor(Renderer.color_text);
              g2.drawLine(
                  mapButtonBounds.x + 4,
                  mapButtonBounds.y + 1,
                  mapButtonBounds.x + 4,
                  mapButtonBounds.y + 1 + 6);
              g2.drawLine(
                  mapButtonBounds.x + 1,
                  mapButtonBounds.y + 4,
                  mapButtonBounds.x + 7,
                  mapButtonBounds.y + 4);
            }

            // Handle magic book button click
            if (bufferedMouseClick.getX() >= mapButtonBounds.x
                && bufferedMouseClick.getX() <= mapButtonBounds.x + mapButtonBounds.width
                && bufferedMouseClick.getY() >= mapButtonBounds.y
                && bufferedMouseClick.getY() <= mapButtonBounds.y + mapButtonBounds.height
                && bufferedMouseClick.isMouseClicked()) {
              if (!bufferedMouseClick.isRightClick()) {
                Client.displayMessage(
                    "Click on something to look it up on the wiki...", Client.CHAT_NONE);
                WikiURL.nextClickIsLookup = true;
              }
            }
          }
        }

        // Hiscore lookup / Motivational Quotes
        if (Settings.MOTIVATIONAL_QUOTES_BUTTON.get(Settings.currentProfile)
            || Settings.HISCORES_LOOKUP_BUTTON.get(Settings.currentProfile)) {
          mapButtonBounds = new Rectangle(width - 68 - 99, 3, 32, 32);
          if ((!show_bank_last || mapButtonBounds.x >= 460) && !Client.show_sleeping) {
            if (Settings.SHOW_RSCPLUS_BUTTONS.get(Settings.currentProfile)) {
              g2.setColor(Renderer.color_text);
              g2.drawLine(
                  mapButtonBounds.x + 4,
                  mapButtonBounds.y + 1,
                  mapButtonBounds.x + 4,
                  mapButtonBounds.y + 1 + 6);
              g2.drawLine(
                  mapButtonBounds.x + 1,
                  mapButtonBounds.y + 4,
                  mapButtonBounds.x + 7,
                  mapButtonBounds.y + 4);
            }

            // Handle friends button click
            if (bufferedMouseClick.getX() >= mapButtonBounds.x
                && bufferedMouseClick.getX() <= mapButtonBounds.x + mapButtonBounds.width
                && bufferedMouseClick.getY() >= mapButtonBounds.y
                && bufferedMouseClick.getY() <= mapButtonBounds.y + mapButtonBounds.height
                && bufferedMouseClick.isMouseClicked()) {
              if (bufferedMouseClick.isRightClick()) {
                if (Settings.MOTIVATIONAL_QUOTES_BUTTON.get(Settings.currentProfile)) {
                  Client.displayMotivationalQuote();
                }
              } else {
                if (Settings.HISCORES_LOOKUP_BUTTON.get(Settings.currentProfile)) {
                  if (Settings.WORLD_HISCORES_URL
                      .get(Settings.WORLD.get(Settings.currentProfile))
                      .equals("")) {
                    Client.displayMessage(
                        "@lre@No hiscores URL defined for this world.", Client.CHAT_NONE);
                  } else {
                    Client.displayMessage(
                        "Click on a player to look them up on the hiscores...", Client.CHAT_NONE);
                    HiscoresURL.nextClickIsLookup = true;
                  }
                } else if (Settings.MOTIVATIONAL_QUOTES_BUTTON.get(Settings.currentProfile)) {
                  // any mouse button can display a motivational quote if Hiscores lookup is
                  // disabled
                  Client.displayMotivationalQuote();
                }
              }
            }
          }
        }

        // Toggle XP bar
        if (Settings.TOGGLE_XP_BAR_ON_STATS_BUTTON.get(Settings.currentProfile)) {
          mapButtonBounds = new Rectangle(width - 68 - 33, 3, 32, 32);
          if ((!show_bank_last || mapButtonBounds.x >= 460) && !Client.show_sleeping) {
            if (Settings.SHOW_RSCPLUS_BUTTONS.get(Settings.currentProfile)) {
              g2.setColor(Renderer.color_text);
              g2.drawLine(
                  mapButtonBounds.x + 4,
                  mapButtonBounds.y + 1,
                  mapButtonBounds.x + 4,
                  mapButtonBounds.y + 1 + 6);
              g2.drawLine(
                  mapButtonBounds.x + 1,
                  mapButtonBounds.y + 4,
                  mapButtonBounds.x + 7,
                  mapButtonBounds.y + 4);
            }

            // Handle stats menu click
            if (bufferedMouseClick.getX() >= mapButtonBounds.x
                && bufferedMouseClick.getX() <= mapButtonBounds.x + mapButtonBounds.width
                && bufferedMouseClick.getY() >= mapButtonBounds.y
                && bufferedMouseClick.getY() <= mapButtonBounds.y + mapButtonBounds.height
                && bufferedMouseClick.isMouseClicked()) {
              if (bufferedMouseClick.isRightClick()) {
                Settings.toggleXPBar();
              } else {
                Settings.toggleXPBarPin();
              }
            }
          }
        }
      }

      // Handle setting XP Bar stat from STATS menu
      if (Client.show_menu == Client.MENU_STATS_QUESTS
          && Client.show_stats_or_quests == Client.MENU_STATS
          && bufferedMouseClick.isMouseClicked()) {
        int xOffset = width - 199;
        int yOffset = 85;
        boolean clickedSkill = false;
        int selectedSkill = -1;
        for (int skillIdx = 0; skillIdx < 9; ++skillIdx) {
          // Column 1
          if (bufferedMouseClick.getX() > xOffset + 3
              && bufferedMouseClick.getX() < xOffset + 90
              && bufferedMouseClick.getY() >= yOffset - 11
              && bufferedMouseClick.getY() < yOffset + 2) {
            selectedSkill = skillIdx;
            clickedSkill = true;
            break;
          }
          // Column 2
          if (bufferedMouseClick.getX() >= xOffset + 90
              && bufferedMouseClick.getX() < xOffset + 196
              && bufferedMouseClick.getY() >= yOffset - 24
              && bufferedMouseClick.getY() < yOffset - 11) {
            selectedSkill = skillIdx + 9;
            clickedSkill = true;
            break;
          }
          yOffset += 13;
        }
        if (clickedSkill) {
          XPBar.pinnedSkill = selectedSkill;
        }
      }

      if (Settings.DEBUG.get(Settings.currentProfile)) {
        x = 32;
        y = 32;

        // Draw Skills
        for (int i = 0; i < 18; i++) {
          drawShadowText(
              g2,
              Client.skill_name[i]
                  + " ("
                  + i
                  + "): "
                  + Client.current_level[i]
                  + "/"
                  + Client.base_level[i]
                  + " ("
                  + Client.getXP(i)
                  + " xp)",
              x,
              y,
              color_text,
              false);
          y += 16;
        }

        // Draw Fatigue
        y += 16;
        drawShadowText(
            g2, "Fatigue: " + ((float) Client.fatigue * 100.0f / 750.0f), x, y, color_text, false);
        y += 16;

        // Draw Drain rates
        y += 16;
        drawShadowText(g2, "Base Drain Rate: " + lastBaseDrainRate, x, y, color_text, false);
        y += 16;
        drawShadowText(
            g2,
            "Adjusted Drain Rate: " + Client.trimNumber(lastAdjustedDrainRate, 1),
            x,
            y,
            color_text,
            false);
        y += 16;

        // Draw Mouse Info
        y += 16;
        drawShadowText(
            g2,
            "Mouse Position: " + MouseHandler.x + ", " + MouseHandler.y,
            x,
            y,
            color_text,
            false);
        y += 16;

        // Draw camera info
        y += 16;
        drawShadowText(g2, "Camera Pitch: " + Camera.pitch_internal, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Rotation: " + Camera.rotation, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Angle: " + Camera.angle, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Auto: " + Camera.auto, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Auto Speed: " + Camera.auto_speed, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Rotation Y: " + Camera.rotation_y, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Lookat X: " + Camera.lookat_x, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Lookat Y: " + Camera.lookat_y, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Zoom: " + Camera.zoom, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Distance1: " + Camera.distance1, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Distance2: " + Camera.distance2, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Distance3: " + Camera.distance3, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Camera Distance4: " + Camera.distance4, x, y, color_text, false);
        y += 16;

        x = 256;
        y = 32;
        drawShadowText(
            g2, "FPS: " + fps + " (" + Client.updatesPerSecond + ")", x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "FPS Sleepiness: " + sleepTimer, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Interlace: " + Client.getInterlace(), x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Game Size: " + width + "x" + height, x, y, color_text, false);
        y += 16;

        // Draw Inventory items
        y += 16;
        for (int i = 0; i < Client.inventory_count; i++) {
          drawShadowText(g2, "(" + i + "): " + Client.inventory_items[i], x, y, color_text, false);
          y += 16;
        }

        y += 16;
        drawShadowText(g2, "Menu: " + Client.show_menu, x, y, color_text, false);
        y += 16;

        x = 380;
        y = 32;
        drawShadowText(g2, Client.player_name, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Player Count: " + playerCount, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "NPC Count: " + npcCount, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Object Count: " + Client.objectCount, x, y, color_text, false);
        y += 16;
        drawShadowText(
            g2,
            "LocalRegion: (" + Client.localRegionX + "," + Client.localRegionY + ")",
            x,
            y,
            color_text,
            false);
        y += 16;
        drawShadowText(
            g2, "Region: (" + Client.regionX + "," + Client.regionY + ")", x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "WorldCoord: " + Client.getCoords(), x, y, color_text, false);
        y += 16;
        drawShadowText(
            g2,
            "ChunkCoord: " + Client.getFloor() + "" + Client.getChunkX() + "" + Client.getChunkY(),
            x,
            y,
            color_text,
            false);
        y += 16;
        drawShadowText(
            g2,
            "Plane: ("
                + Client.planeWidth
                + ","
                + Client.planeHeight
                + ","
                + Client.planeIndex
                + ")",
            x,
            y,
            color_text,
            false);
        y += 16;
        drawShadowText(g2, "combat_timer: " + Client.combat_timer, x, y, color_text, false);
        y += 32;
        drawShadowText(g2, "frame_time_slice: " + Replay.frame_time_slice, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "lag: " + Replay.timestamp_lag + " updates", x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "replay_timestamp: " + Replay.timestamp, x, y, color_text, false);
        y += 16;
        drawShadowText(
            g2,
            "replay_server_timestamp: " + Replay.timestamp_server_last,
            x,
            y,
            color_text,
            false);
        y += 16;
        drawShadowText(
            g2, "replay_client_timestamp: " + Replay.timestamp_client, x, y, color_text, false);
        y += 16;
        drawShadowText(
            g2, "replay_client_read: " + Replay.getClientRead(), x, y, color_text, false);
        y += 16;
        drawShadowText(
            g2, "replay_client_write: " + Replay.getClientWrite(), x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Last sound effect: " + Client.lastSoundEffect, x, y, color_text, false);
        y += 16;
        drawColoredText(g2, "@whi@Mouse Text: " + MouseText.mouseText, x, y, false);
        y += 16;
        drawShadowText(g2, "Hover: " + Client.is_hover, x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Java version: " + Settings.javaVersion, x, y, color_text, false);

        AreaDefinition area = Client.getCurrentAreaDefinition();
        y += 32;
        drawShadowText(g2, "~Area Information~", x, y, color_text, false);
        y += 16;
        drawShadowText(g2, "Music: " + area.music.trackname, x, y, color_text, false);
      }

      // A little over a full tick
      int threshold = 35;

      if (Replay.isPlaying && Replay.fpsPlayMultiplier > 1.0)
        threshold = 35 * 3; // this is to prevent blinking during fastforward

      if (Settings.LAG_INDICATOR.get(Settings.currentProfile)
          && Replay.getServerLag() >= threshold) {
        x = width - 80;
        y = height - 80;
        setAlpha(g2, alpha_time);
        g2.drawImage(Launcher.icon_warn.getImage(), x, y, 32, 32, null);
        x += 16;
        y += 38;
        drawShadowText(g2, "Server Lag", x, y, color_fatigue, true);
        y += 12;
        int lag = (Replay.getServerLag() - 31) * Replay.getFrameTimeSlice();
        drawShadowText(
            g2,
            new DecimalFormat("0.0").format((float) lag / 1000.0f) + "s",
            x,
            y,
            color_low,
            true);
        setAlpha(g2, 1.0f);
      }
      if (!(Replay.isPlaying && !Settings.TRIGGER_ALERTS_REPLAY.get(Settings.currentProfile))) {
        g2.setFont(font_big);
        if (Settings.FATIGUE_ALERT.get(Settings.currentProfile)
            && Client.getFatigue() >= 98
            && !Client.isInterfaceOpen()) {
          setAlpha(g2, alpha_time);
          drawShadowText(g2, "FATIGUED", width / 2, height / 2, color_low, true);
          setAlpha(g2, 1.0f);
        }
        if (Settings.INVENTORY_FULL_ALERT.get(Settings.currentProfile)
            && Client.inventory_count >= 30
            && !Client.isInterfaceOpen()) {
          setAlpha(g2, alpha_time);
          drawShadowText(g2, "INVENTORY FULL", width / 2, height / 2, color_low, true);
          setAlpha(g2, 1.0f);
        }
        g2.setFont(font_main);
      }

      if (Settings.SHOW_PLAYER_POSITION.get(Settings.currentProfile)) {
        y = Renderer.height - 19;
        int offset = 0;
        if (Client.is_in_wild) offset += 70;
        if (Replay.isPlaying) {
          if ((!screenshot && Settings.SHOW_SEEK_BAR.get(Settings.currentProfile))
              || Settings.SHOW_RETRO_FPS.get(Settings.currentProfile)) y -= 12;
        }
        if ((!Replay.isPlaying || screenshot)
            && Settings.SHOW_RETRO_FPS.get(Settings.currentProfile)) offset += 70;
        drawShadowText(
            g2,
            "Pos: " + Client.getCoords(),
            (Renderer.width - 92 - offset),
            y,
            color_yellow,
            false);
      }

      // Mouseover hover handling
      if (Settings.SHOW_MOUSE_TOOLTIP.get(Settings.currentProfile)
          && !Client.isInterfaceOpen()
          && !Client.show_questionmenu
          && Client.is_hover) {
        final int extraOptionsOffsetX = 8;
        final int extraOptionsOffsetY = 12;

        x = MouseHandler.x + 16;
        y = MouseHandler.y + 28;

        // Dont allow text to go off the screen
        Dimension bounds = getStringBounds(g2, MouseText.colorlessText);
        Dimension extraBounds = getStringBounds(g2, MouseText.extraOptions);
        if (MouseText.extraOptions.length() == 0) extraBounds.height = 0;

        bounds.height += extraOptionsOffsetY;
        if (Settings.SHOW_EXTENDED_TOOLTIP.get(Settings.currentProfile)) {
          extraBounds.width += extraOptionsOffsetX;
          bounds.width = (bounds.width > extraBounds.width) ? bounds.width : extraBounds.width;
          bounds.height += extraBounds.height;
        }
        if (x + bounds.width > Renderer.width - 4) x -= (x + bounds.width) - (Renderer.width - 4);
        if (y + bounds.height > Renderer.height) y -= (y + bounds.height) - (Renderer.height);

        // Draw the final outcome
        if (Settings.SHOW_EXTENDED_TOOLTIP.get(Settings.currentProfile)) {
          setAlpha(g2, 0.65f);
          g2.setColor(color_shadow);
          g2.fillRect(x - 4, y - 12, bounds.width + 8, bounds.height - 8);
          setAlpha(g2, 1.0f);
          drawColoredText(g2, MouseText.getMouseOverText(), x, y);
          x += extraOptionsOffsetX;
          y += extraOptionsOffsetY;
          drawColoredText(g2, "@whi@" + MouseText.extraOptions, x, y);
        } else {
          if (!MouseText.getMouseOverText().contains("Walk here")
              && !MouseText.getMouseOverText().contains("Choose a target")) {
            setAlpha(g2, 0.65f);
            g2.setColor(color_shadow);
            g2.fillRect(x - 4, y - 12, bounds.width + 8, bounds.height - 8);
            setAlpha(g2, 1.0f);
            drawColoredText(g2, MouseText.getMouseOverText(), x, y);
          }
        }
      }
    } else if (Client.state == Client.STATE_LOGIN) {
      if (Settings.DEBUG.get(Settings.currentProfile))
        drawShadowText(g2, "DEBUG MODE", 38, 8, color_text, true);

      // Draw world list
      drawShadowText(g2, "World (Click to change): ", 80, height - 8, color_text, true);
      for (int i = 1; i <= Settings.WORLDS_TO_DISPLAY; i++) {
        Rectangle bounds = new Rectangle(134 + (i * 18), height - 12, 16, 12);
        Color color = color_text;

        if (i == Settings.WORLD.get(Settings.currentProfile)) color = color_low;

        setAlpha(g2, 0.5f);
        g2.setColor(color);
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        setAlpha(g2, 1.0f);
        String worldString = Integer.toString(i);
        drawShadowText(
            g2, worldString, bounds.x + (bounds.width / 2), bounds.y + 4, color_text, true);

        // Handle world selection click
        if (bufferedMouseClick.getX() >= bounds.x
            && bufferedMouseClick.getX() <= bounds.x + bounds.width
            && bufferedMouseClick.getY() >= bounds.y
            && bufferedMouseClick.getY() <= bounds.y + bounds.height
            && bufferedMouseClick.isMouseClicked()) {
          Game.getInstance().getJConfig().changeWorld(i);
        }
      }

      // draw server replay buttons
      boolean wantToDrawServerReplayButtons =
          Client.login_screen == Client.SCREEN_USERNAME_PASSWORD_LOGIN
              || Client.login_screen == Client.SCREEN_CLICK_TO_LOGIN;
      if (wantToDrawServerReplayButtons) {
        // TODO: This will need to be adjusted when the login screen is resizable
        Rectangle recordButtonBounds;

        // TODO: if Lost Password button can be toggled separately, this should be changed
        boolean longForm = true;

        if (Client.login_screen == Client.SCREEN_CLICK_TO_LOGIN) {
          longForm = true;
        } else if (Client.login_screen == Client.SCREEN_USERNAME_PASSWORD_LOGIN) {
          longForm = Client.loginLostPasswordButton == 0;
        }
        if (longForm) {
          if (Client.login_screen == Client.SCREEN_USERNAME_PASSWORD_LOGIN) {
            recordButtonBounds = new Rectangle(512 - 148, 346 - 36, 48, 16);
          } else if (Client.login_screen == Client.SCREEN_CLICK_TO_LOGIN) {
            recordButtonBounds = new Rectangle(512 - 140, 288, 48, 16);
          } else {
            Logger.Error("Specify -server replay- bounds for this screen!");
            // original bounds, underneath the Cancel button
            recordButtonBounds = new Rectangle(512 - 148, 346 - 36, 48, 16);
          }
          drawShadowText(
              g2,
              "-server replay-",
              recordButtonBounds.x + 48,
              recordButtonBounds.y - 10,
              color_fatigue,
              true);
        } else {
          if (Client.login_screen == Client.SCREEN_USERNAME_PASSWORD_LOGIN) {
            recordButtonBounds = new Rectangle(512 - 33, 250 - 12, 25, 25);
            // bounds = new Rectangle(512 - 148, 346 - 36, 48, 16);
          } else {
            Logger.Error("Specify tiny -server replay- bounds for this screen!");
            // original bounds, underneath the Cancel button
            recordButtonBounds = new Rectangle(512 - 148, 346 - 36, 48, 16);
          }
        }

        setAlpha(g2, 0.5f);
        if (Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) {
          g2.setColor(color_hp);
        } else {
          if (replayOption == 1 || Settings.RECORD_AUTOMATICALLY.get(Settings.currentProfile)) {
            g2.setColor(color_low);
          } else {
            g2.setColor(color_text);
          }
        }
        if (longForm) {
          g2.fillRect(
              recordButtonBounds.x,
              recordButtonBounds.y,
              recordButtonBounds.width,
              recordButtonBounds.height);
        } else {
          g2.fillOval(
              recordButtonBounds.x,
              recordButtonBounds.y,
              recordButtonBounds.width,
              recordButtonBounds.height);
        }

        if (Settings.RECORD_AUTOMATICALLY.get(Settings.currentProfile)) {
          g2.setColor(color_text);
          if (longForm) {
            g2.drawRect(
                recordButtonBounds.x,
                recordButtonBounds.y,
                recordButtonBounds.width,
                recordButtonBounds.height);
          } else {
            g2.drawOval(
                recordButtonBounds.x,
                recordButtonBounds.y,
                recordButtonBounds.width,
                recordButtonBounds.height);
          }
        }
        setAlpha(g2, 1.0f);
        String recordButtonText = "";
        if (longForm) {
          if (Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) {
            recordButtonText = "speedy";
          } else {
            recordButtonText = "record";
          }
        } else {
          // smaller buttons, less room for text
          if (Settings.SPEEDRUNNER_MODE_ACTIVE.get(Settings.currentProfile)) {
            recordButtonText = "run";
          } else {
            recordButtonText = "rec";
          }
        }
        drawShadowText(
            g2,
            recordButtonText,
            recordButtonBounds.x + (recordButtonBounds.width / 2) + 1,
            recordButtonBounds.y + (longForm ? 6 : 10),
            color_text,
            true);

        // Handle replay record selection click
        if (bufferedMouseClick.getX() >= recordButtonBounds.x
            && bufferedMouseClick.getX() <= recordButtonBounds.x + recordButtonBounds.width
            && bufferedMouseClick.getY() >= recordButtonBounds.y
            && bufferedMouseClick.getY() <= recordButtonBounds.y + recordButtonBounds.height
            && bufferedMouseClick.isMouseClicked()) {
          Client.showRecordAlwaysDialogue = true;

          if (replayOption == 1) {
            replayOption = 0;
          } else {
            replayOption = 1;
          }
        }
        Rectangle playButtonBounds;
        if (longForm) {
          playButtonBounds =
              new Rectangle(
                  recordButtonBounds.x + recordButtonBounds.width + 4,
                  recordButtonBounds.y,
                  48,
                  recordButtonBounds.height);
        } else {
          playButtonBounds =
              new Rectangle(recordButtonBounds.x - 4, recordButtonBounds.y + 33, 32, 19);
        }
        setAlpha(g2, 0.5f);
        if (replayOption == 2) g2.setColor(color_low);
        else g2.setColor(color_text);
        g2.fillRect(
            playButtonBounds.x,
            playButtonBounds.y,
            playButtonBounds.width,
            playButtonBounds.height);
        setAlpha(g2, 1.0f);
        drawShadowText(
            g2,
            "play",
            playButtonBounds.x + (playButtonBounds.width / 2) + 1,
            playButtonBounds.y + (longForm ? 6 : 7),
            color_text,
            true);

        // Handle replay play selection click
        if (bufferedMouseClick.getX() >= playButtonBounds.x
            && bufferedMouseClick.getX() <= playButtonBounds.x + playButtonBounds.width
            && bufferedMouseClick.getY() >= playButtonBounds.y
            && bufferedMouseClick.getY() <= playButtonBounds.y + playButtonBounds.height
            && bufferedMouseClick.isMouseClicked()) {
          if (replayOption == 2) {
            replayOption = 0;
          } else {
            if (ReplayQueue.replayFileSelectAdd()) {
              Renderer.replayOption = 2;
              ReplayQueue.nextReplay();
            } else {
              Renderer.replayOption = 0;
            }
          }
        }
      }

      /* TODO: add button to main screen to open settings.
      // draw button to open settings
      if (Client.login_screen == Client.SCREEN_CLICK_TO_LOGIN) {
        g2.setColor(color_replay);
        Rectangle settingsButtonBounds = new Rectangle(512 - 140, 346 - 35, 100, 16);
        g2.fillRect(
            settingsButtonBounds.x,
            settingsButtonBounds.y,
            settingsButtonBounds.width,
            settingsButtonBounds.height);
      }
      */

      // TODO: Uncomment this information when we can provide it again
      /*drawShadowText(g2, "Populations", width - 67, 14, color_text, false);
      int worldPopArray[];
      int totalPop = 0;
      worldPopArray = Util.getPop();
      for (int i = 1; i < worldPopArray.length; i++) {
        drawShadowText(
            g2, "W" + i + " - " + worldPopArray[i], width - 56, 14 + (15 * i), color_text, false);
        totalPop += worldPopArray[i];
      }

      drawShadowText(
          g2,
          "There are currently " + totalPop + " players online.",
          width / 2,
          8,
          color_text,
          true);
      String daysString = "RuneScape Classic has been taken offline";
      drawShadowText(g2, daysString, width / 2, 24, Renderer.color_fatigue, true);
      //*/

      // Draw version information
      drawShadowText(
          g2,
          "rscplus v" + String.format("%8.6f", Settings.VERSION_NUMBER),
          width - 164,
          height - 2,
          color_text,
          false);
    }

    if (Client.state == Client.STATE_GAME && Replay.isPlaying && !screenshot) {
      if (Settings.SHOW_SEEK_BAR.get(Settings.currentProfile)) {
        float percent = (float) Replay.timestamp / Replay.getReplayEnd();

        if (Replay.isSeeking) {
          percent = (float) Replay.getSeekEnd() / Replay.getReplayEnd();
        }

        // "extended" is "in hover mode"
        boolean extended = (MouseHandler.y >= height - 28);

        // draw shadow box while hovering
        if (extended) {
          Rectangle bounds = new Rectangle(0, height - 28, width, 48);
          g2.setColor(color_shadow);
          setAlpha(g2, 0.75f);
          g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
          setAlpha(g2, 1.0f);
          g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        // Set small font
        g2.setFont(font_main);

        // Handle bar
        barBounds = new Rectangle(32, height - 27, width - 64, 8);
        g2.setColor(color_text);
        setAlpha(g2, 0.25f);
        g2.fillRect(barBounds.x, barBounds.y, barBounds.width, barBounds.height);
        g2.setColor(color_prayer);
        setAlpha(g2, 0.5f);
        g2.fillRect(
            barBounds.x, barBounds.y, (int) ((float) barBounds.width * percent), barBounds.height);
        g2.setColor(color_text);
        setAlpha(g2, 1.0f);
        g2.drawRect(
            barBounds.x, barBounds.y, (int) ((float) barBounds.width * percent), barBounds.height);
        g2.setColor(color_text);
        g2.drawRect(barBounds.x, barBounds.y, barBounds.width, barBounds.height);

        // flash the bar while paused
        if (Replay.paused) {
          g2.setColor(color_low);
          setAlpha(g2, alpha_time / 5.0f);
          g2.fillRect(barBounds.x, barBounds.y, barBounds.width, barBounds.height);
          setAlpha(g2, 1.0f);
        }

        // draw time-into-replay
        String elapsed =
            Util.formatTimeDuration(Replay.elapsedTimeMillis(), Replay.endTimeMillis());
        String end = Util.formatTimeDuration(Replay.endTimeMillis(), Replay.endTimeMillis());
        if (extended) {
          drawShadowText(
              g2,
              elapsed + " / " + end,
              barBounds.x + (barBounds.width / 2),
              barBounds.y + barBounds.height + 8,
              color_replay,
              true);
        }

        float percentClient = (float) Replay.getClientRead() / Replay.getClientWrite();
        int server_x = (int) (barBounds.width * percent);
        int client_x = (int) (server_x * percentClient);

        g2.setColor(color_prayer);
        setAlpha(g2, 0.5f);
        g2.fillRect(barBounds.x + 1, barBounds.y + 1, client_x - 1, barBounds.height - 1);

        if (MouseHandler.x >= barBounds.x
            && MouseHandler.x <= barBounds.x + barBounds.width
            && MouseHandler.y >= barBounds.y
            && MouseHandler.y <= barBounds.y + barBounds.height) {
          float percentEnd = (float) (MouseHandler.x - barBounds.x) / barBounds.width;
          int timestamp = (int) (Replay.getReplayEnd() * percentEnd);
          g2.setColor(color_fatigue);
          setAlpha(g2, 0.5f);
          g2.drawLine(MouseHandler.x, barBounds.y, MouseHandler.x, barBounds.y + barBounds.height);
          setAlpha(g2, 1.0f);
          drawShadowTextBorder(
              g2,
              Util.formatTimeDuration(timestamp * 20, Replay.endTimeMillis()),
              MouseHandler.x,
              barBounds.y - 8,
              color_text,
              1.0f,
              0.75f,
              true,
              0);

          if (!Replay.isSeeking && bufferedMouseClick.isMouseClicked()) Replay.seek(timestamp);
        }

        if (Replay.isSeeking) {
          drawShadowTextBorder(
              g2,
              "Seeking... Please wait",
              barBounds.x + (barBounds.width / 2),
              barBounds.y + barBounds.height - 18,
              color_fatigue,
              1.0f,
              0.75f,
              false,
              2);
        }
        // draw & handle gui "video player" control buttons
        if (extended && Settings.SHOW_PLAYER_CONTROLS.get(Settings.currentProfile)) {
          final int BUTTON_WIDTH = 30;
          final int BUTTON_HEIGHT = 11;
          final int BUTTON_OFFSET_X = 4; // how many pixels between each button horizontally
          final int BUTTON_OFFSET_Y = 4; // how many pixels "down" from the bottom of
          // the seek bar to  draw the control buttons

          // buttons are in this order:
          // previous slowforward playpause fastforward next stop --- queue

          // previous button
          previousBounds =
              new Rectangle(
                  barBounds.x,
                  barBounds.y + barBounds.height + BUTTON_OFFSET_Y,
                  BUTTON_WIDTH,
                  BUTTON_HEIGHT);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          g2.drawRect(
              previousBounds.x, previousBounds.y, previousBounds.width, previousBounds.height);
          g2.setColor(color_shadow);
          setAlpha(g2, 0.50f);
          g2.fillRect(
              previousBounds.x + 1,
              previousBounds.y + 1,
              previousBounds.width - 1,
              previousBounds.height - 1);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          shapeHeight = previousBounds.height - 3;
          shapeX =
              previousBounds.x
                  + (int) (((float) previousBounds.width) / 2.0)
                  - (int) ((float) shapeHeight / 2.0);
          drawPlayerControlShape(g2, shapeX, previousBounds.y + 2, shapeHeight, "previous");

          if (MouseHandler.inPlaybackButtonBounds(previousBounds)
              && bufferedMouseClick.isMouseClicked()) Replay.controlPlayback("prev");

          // slowdown button
          slowForwardBounds =
              new Rectangle(
                  previousBounds.x + previousBounds.width + BUTTON_OFFSET_X,
                  previousBounds.y,
                  BUTTON_WIDTH,
                  BUTTON_HEIGHT);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          g2.drawRect(
              slowForwardBounds.x,
              slowForwardBounds.y,
              slowForwardBounds.width,
              slowForwardBounds.height);
          g2.setColor(color_shadow);
          setAlpha(g2, 0.50f);
          g2.fillRect(
              slowForwardBounds.x + 1,
              slowForwardBounds.y + 1,
              slowForwardBounds.width - 1,
              slowForwardBounds.height - 1);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          shapeHeight = slowForwardBounds.height - 3;
          shapeX =
              slowForwardBounds.x
                  + (int) (((float) slowForwardBounds.width) / 2.0)
                  - (int) ((float) shapeHeight / 2.0);
          drawPlayerControlShape(g2, shapeX, slowForwardBounds.y + 2, shapeHeight, "slowforward");

          if (MouseHandler.inPlaybackButtonBounds(slowForwardBounds)
              && bufferedMouseClick.isMouseClicked()) {
            Replay.controlPlayback("ff_minus");
          }

          // play/pause (one button)
          playPauseBounds =
              new Rectangle(
                  slowForwardBounds.x + slowForwardBounds.width + BUTTON_OFFSET_X,
                  previousBounds.y,
                  BUTTON_WIDTH,
                  BUTTON_HEIGHT);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          g2.drawRect(
              playPauseBounds.x, playPauseBounds.y, playPauseBounds.width, playPauseBounds.height);
          g2.setColor(color_shadow);
          setAlpha(g2, 0.50f);
          g2.fillRect(
              playPauseBounds.x + 1,
              playPauseBounds.y + 1,
              playPauseBounds.width - 1,
              playPauseBounds.height - 1);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          shapeHeight = playPauseBounds.height - 3;
          shapeX =
              playPauseBounds.x
                  + (int) (((float) playPauseBounds.width) / 2.0)
                  - (int) ((float) shapeHeight / 2.0);
          drawPlayerControlShape(g2, shapeX, playPauseBounds.y + 2, shapeHeight, "playpause");

          if (MouseHandler.inPlaybackButtonBounds(playPauseBounds)
              && bufferedMouseClick.isMouseClicked()) {
            Replay.togglePause();
          }
          // fastforward button
          fastForwardBounds =
              new Rectangle(
                  playPauseBounds.x + playPauseBounds.width + BUTTON_OFFSET_X,
                  previousBounds.y,
                  BUTTON_WIDTH,
                  BUTTON_HEIGHT);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          g2.drawRect(
              fastForwardBounds.x,
              fastForwardBounds.y,
              fastForwardBounds.width,
              fastForwardBounds.height);
          g2.setColor(color_shadow);
          setAlpha(g2, 0.50f);
          g2.fillRect(
              fastForwardBounds.x + 1,
              fastForwardBounds.y + 1,
              fastForwardBounds.width - 1,
              fastForwardBounds.height - 1);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          shapeHeight = fastForwardBounds.height - 3;
          shapeX =
              fastForwardBounds.x
                  + (int) (((float) fastForwardBounds.width) / 2.0)
                  - (int) ((float) shapeHeight / 2.0);
          drawPlayerControlShape(g2, shapeX, fastForwardBounds.y + 2, shapeHeight, "fastforward");

          if (MouseHandler.inPlaybackButtonBounds(fastForwardBounds)
              && bufferedMouseClick.isMouseClicked()) {
            Replay.controlPlayback("ff_plus");
          }

          // next button
          nextBounds =
              new Rectangle(
                  fastForwardBounds.x + fastForwardBounds.width + BUTTON_OFFSET_X,
                  previousBounds.y,
                  BUTTON_WIDTH,
                  BUTTON_HEIGHT);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          g2.drawRect(nextBounds.x, nextBounds.y, nextBounds.width, nextBounds.height);
          g2.setColor(color_shadow);
          setAlpha(g2, 0.50f);
          g2.fillRect(
              nextBounds.x + 1, nextBounds.y + 1, nextBounds.width - 1, nextBounds.height - 1);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          shapeHeight = nextBounds.height - 3;
          shapeX =
              nextBounds.x
                  + (int) (((float) nextBounds.width) / 2.0)
                  - (int) ((float) shapeHeight / 2.0);
          drawPlayerControlShape(g2, shapeX, nextBounds.y + 2, shapeHeight, "next");

          if (MouseHandler.inPlaybackButtonBounds(nextBounds)
              && bufferedMouseClick.isMouseClicked()) Replay.controlPlayback("next");

          // open queue button (right aligned)
          queueBounds =
              new Rectangle(
                  barBounds.x + barBounds.width - BUTTON_WIDTH * 2,
                  previousBounds.y,
                  BUTTON_WIDTH * 2,
                  BUTTON_HEIGHT);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          g2.drawRect(queueBounds.x, queueBounds.y, queueBounds.width, queueBounds.height);
          g2.setColor(color_shadow);
          setAlpha(g2, 0.50f);
          g2.fillRect(
              queueBounds.x + 1, queueBounds.y + 1, queueBounds.width - 1, queueBounds.height - 1);
          g2.setColor(color_text);
          setAlpha(g2, 1.0f);

          shapeHeight = queueBounds.height - 3;
          shapeX = queueBounds.x + 3;
          drawPlayerControlShape(g2, shapeX, queueBounds.y + 2, shapeHeight, "queue");

          drawShadowText(
              g2,
              "Queue",
              queueBounds.x + BUTTON_OFFSET_X + (int) (shapeHeight * 2),
              queueBounds.y + queueBounds.height - 2,
              color_white,
              false);

          if (MouseHandler.inPlaybackButtonBounds(queueBounds)
              && bufferedMouseClick.isMouseClicked()) {
            Launcher.getQueueWindow().showQueueWindow();
          }

          // stop button
          stopBounds =
              new Rectangle(
                  queueBounds.x - BUTTON_WIDTH - BUTTON_OFFSET_X,
                  previousBounds.y,
                  BUTTON_WIDTH,
                  BUTTON_HEIGHT);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          g2.drawRect(stopBounds.x, stopBounds.y, stopBounds.width, stopBounds.height);
          g2.setColor(color_shadow);
          setAlpha(g2, 0.50f);
          g2.fillRect(
              stopBounds.x + 1, stopBounds.y + 1, stopBounds.width - 1, stopBounds.height - 1);

          g2.setColor(color_text);
          setAlpha(g2, 1.0f);
          shapeHeight = stopBounds.height - 3;
          shapeX =
              stopBounds.x
                  + (int) (((float) stopBounds.width) / 2.0)
                  - (int) ((float) shapeHeight / 2.0);
          drawPlayerControlShape(g2, shapeX, stopBounds.y + 2, shapeHeight, "stop");

          if (MouseHandler.inPlaybackButtonBounds(stopBounds)
              && bufferedMouseClick.isMouseClicked()) {
            Replay.controlPlayback("stop");
          }
        }
      }
    }

    // Draw software cursor
    if (screenshot || Settings.SOFTWARE_CURSOR.get(Settings.currentProfile)) {
      setAlpha(g2, 1.0f);
      g2.drawImage(image_cursor, MouseHandler.x, MouseHandler.y, null);
    }

    g2.dispose();

    // Forward the image to be drawn by ScaledWindow.java
    ScaledWindow.getInstance().setGameImage(game_image);

    // Right now is a good time to take a screenshot if one is requested
    if (screenshot) {
      try {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
        String fname =
            Settings.Dir.SCREENSHOT + "/" + "Screenshot from " + format.format(new Date()) + ".png";
        File screenshotFile = new File(fname);
        ImageIO.write(game_image, "png", screenshotFile);
        if (!quietScreenshot)
          Client.displayMessage(
              "@cya@Screenshot saved to '" + screenshotFile.toString() + "'", Client.CHAT_NONE);
      } catch (Exception e) {
      }
    }

    if (videorecord > 0) {
      String fname = Settings.Dir.VIDEO + "/" + "video" + (videolength - videorecord) + ".png";
      try {
        File screenshotFile = new File(fname);
        ImageIO.write(game_image, "png", screenshotFile);
      } catch (Exception e) {
      }
      videorecord--;
      if (videorecord <= 0) {
        Client.displayMessage("@cya@Many screenshots saved to '" + fname + "'", Client.CHAT_NONE);
      }
    }

    if (screenshot_scenery_frames > 0 && screenshot_scenery_angle < 256) {
      screenshot_scenery_frames--;
      if (screenshot_scenery_frames % 2 == 0 && screenshot_scenery_frames < 290) {
        String fname =
            Settings.Dir.SCREENSHOT
                + "/zoom"
                + Camera.zoom
                + "/rot"
                + screenshot_scenery_scenery_rotation
                + "/scenery"
                + screenshot_scenery_scenery_id
                + "rot"
                + screenshot_scenery_scenery_rotation
                + "angle"
                + (screenshot_scenery_angle + (screenshot_scenery_scenery_rotation * 32)) % 256
                + "zoom"
                + Camera.zoom
                + ".png";
        try {
          File screenshotFile = new File(fname);
          BufferedImage writtenImage;
          try {
            if (screenshot_scenery_scenery_id <= 1) {
              // error in replay makes it inconsistent at beginning, just crop more off bottom to
              // compensate
              writtenImage =
                  ImageManip.prepareSceneryImage(
                      game_image.getSubimage(
                          275, 0, game_image.getWidth() - 475, game_image.getHeight() - 227));
            } else {
              writtenImage =
                  ImageManip.prepareSceneryImage(
                      game_image.getSubimage(
                          275, 0, game_image.getWidth() - 475, game_image.getHeight() - 27));
            }
          } catch (RasterFormatException ex) {
            writtenImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            writtenImage.getGraphics().setColor(new Color(0, 0, 0, 0));
            writtenImage.getGraphics().fillRect(0, 0, 1, 1);
          }
          ImageIO.write(writtenImage, "png", screenshotFile);
        } catch (Exception e) {
          e.printStackTrace();
        }

        screenshot_scenery_angle += 8; // set angle for next screenshot
        Camera.rotation = screenshot_scenery_angle;
        Camera.zoom = screenshot_scenery_zoom;
        Camera.delta_rotation = (float) Camera.rotation;
      }
    }

    frames++;

    if (Settings.FPS_LIMIT_ENABLED.get(Settings.currentProfile)) {
      int targetFPS = Settings.FPS_LIMIT.get(Settings.currentProfile);

      // pretend that interlacing helps fps while frame limiting
      if (Client.getInterlace()) {
        targetFPS *= 2;
      }
      if (Client.getInterlace() != lastInterlace) {
        if (lastInterlace) {
          sleepTimer *= 2;
          if (sleepTimer > 1250) {
            sleepTimer = 1250;
          }
        } else {
          sleepTimer /= 2;
        }
      }
      lastInterlace = Client.getInterlace();

      // sleep the renderer thread to achieve target FPS
      if (frames > targetFPS / 2 && sleepTimer < 1250) {
        // maximum time to sleep is about 1.25 seconds. Would not like to sleep longer than that
        // ever.
        if (sleepTimer > 500) {
          // we must accelerate the sleep timer incrementation because at sub-3 fps
          // we can't increase it fast enough to reach target of 1 fps
          sleepTimer += 30;
        }
        if (sleepTimer > 300) {
          sleepTimer += 10;
        }
        if (sleepTimer > 200) {
          sleepTimer += 5;
        }
        if (sleepTimer > 100) {
          sleepTimer += 2;
        }
        sleepTimer++;

      } else if (frames < targetFPS / 2 && sleepTimer > 0) {
        if (sleepTimer > 500) {
          // would like to leave this state quickly
          sleepTimer -= 50;
        }
        if (sleepTimer > 300) {
          sleepTimer -= 10;
        }
        if (sleepTimer > 200) {
          sleepTimer -= 5;
        }
        if (sleepTimer > 100) {
          sleepTimer -= 2;
        }
        sleepTimer--;
      }

      try {
        // sleeping the renderer thread lowers the FPS
        Thread.sleep(sleepTimer);
      } catch (Exception e) {
      }
    }

    // calculate FPS
    time = System.currentTimeMillis();
    if (time > fps_timer) {
      fps = frames;
      frames = 0;
      fps_timer = time + 1000;
    }

    // handle resize
    if (width != new_size.width || height != new_size.height) {
      handle_resize();
    }
    if (Settings.fovUpdateRequired) {
      Camera.setFoV(Settings.FOV.get(Settings.currentProfile));
      Settings.fovUpdateRequired = false;
    }

    // state of show_bank "last frame"
    show_bank_last = Client.show_bank;
  }

  private static Color getInventoryCountColor() {
    if (Settings.SHOW_INVCOUNT_COLOURS.get(Settings.currentProfile)) {
      if (Client.inventory_count == 0) {
        return color_hp;
      }

      if (Client.inventory_count >= 25 && Client.inventory_count < Client.max_inventory) {
        return color_fatigue;
      }
    }

    if (Client.inventory_count == Client.max_inventory) {
      return color_low;
    }

    return color_text;
  }

  private static boolean roomInHbarForHPPrayerFatigueOverlay() {
    if (!Settings.REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(Settings.currentProfile)
        || Client.wikiLookupReplacesReportAbuse()) {
      return width >= 800 - 90 - 6; // 704
    } else {
      return width >= 800 - 112 - 90 - 3; // 595
    }
  }

  private static boolean useFullSizeHPPrayerFatigueBars() {
    if (!Settings.REMOVE_REPORT_ABUSE_BUTTON_HBAR.get(Settings.currentProfile)
        || Client.wikiLookupReplacesReportAbuse()) {
      return width >= 800;
    } else {
      return width >= 800 - 112;
    }
  }

  public static void drawBar(
      Graphics2D g, Image image, int x, int y, Color color, float alpha, int value, int total) {
    // Prevent divide by zero
    if (total == 0) return;

    int width = image.getWidth(null) - 2;
    int percent = value * width / total;

    g.setColor(color_shadow);
    g.fillRect(x + 1, y, width, image.getHeight(null));

    g.setColor(color);
    setAlpha(g, alpha);
    g.fillRect(x + 1, y, percent, image.getHeight(null));
    setAlpha(g, 1.0f);

    g.drawImage(image, x, y, null);
    drawShadowText(
        g,
        value + "/" + total,
        x + (image.getWidth(null) / 2),
        y + (image.getHeight(null) / 2) - 2,
        color_text,
        true);
  }

  public static void setAlpha(Graphics2D g, float alpha) {
    g.setComposite(AlphaComposite.SrcOver.derive(alpha));
  }

  public static boolean stringIsWithinList(String input, ArrayList<String> items) {
    if (items.size() <= 0) {
      return false;
    }
    Iterator it = items.iterator();
    while (it.hasNext()) {
      String item = String.valueOf(it.next());
      if (item.trim().length() > 0
          && input.trim().toLowerCase().contains(item.trim().toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  public static boolean exactStringIgnoreCaseIsWithinList(String input, ArrayList<String> items) {
    if (items.size() <= 0) {
      return false;
    }
    Iterator it = items.iterator();
    while (it.hasNext()) {
      String item = String.valueOf(it.next());
      if (item.trim().length() > 0 && input.trim().equalsIgnoreCase(item.trim())) {
        return true;
      }
    }
    return false;
  }

  public static void drawHighlighImage(Graphics2D g, String text, int x, int y) {
    int correctedX = x;
    int correctedY = y;
    // Adjust for centering
    Dimension bounds = getStringBounds(g, text);
    correctedX -= (bounds.width / 2);
    correctedY += (bounds.height / 2);
    g.drawImage(image_highlighted_item, correctedX - 15, correctedY - 10, null);
  }

  public static void drawShadowText(
      Graphics2D g, String text, int x, int y, Color textColor, boolean center) {
    int textX = x;
    int textY = y;
    if (center) {
      Dimension bounds = getStringBounds(g, text);
      textX -= (bounds.width / 2);
      textY += (bounds.height / 2);
    }

    g.setColor(color_shadow);
    g.drawString(text, textX + 1, textY);
    g.drawString(text, textX - 1, textY);
    g.drawString(text, textX, textY + 1);
    g.drawString(text, textX, textY - 1);

    g.setColor(textColor);
    g.drawString(text, textX, textY);
  }

  public static void drawColoredText(Graphics2D g, String text, int x, int y) {
    drawColoredText(g, text, x, y, false);
  }

  public static void drawColoredText(Graphics2D g, String text, int x, int y, boolean center) {
    int textX = x;
    int textY = y;

    if (center) {
      Dimension bounds = getStringBounds(g, text.replaceAll("@...@", ""));
      textX -= (bounds.width / 2);
      textY += (bounds.height / 2);
    }

    String outputText = "";
    Color outputColor = colorFromCode("@yel@");
    Color currentColor = outputColor;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '@' && text.charAt(i + 4) == '@') {
        outputColor = colorFromCode(text.substring(i, i + 4));
        i += 5;
        if (i >= text.length()) break;
      }

      if (currentColor != outputColor) {
        if (outputText.length() > 0) {
          g.setColor(color_shadow);
          g.drawString(outputText, textX + 1, textY);
          g.drawString(outputText, textX - 1, textY);
          g.drawString(outputText, textX, textY + 1);
          g.drawString(outputText, textX, textY - 1);

          g.setColor(currentColor);
          g.drawString(outputText, textX, textY);
          textX += getStringBounds(g, outputText).width;
        }
        currentColor = outputColor;
        outputText = "";
      }

      outputText += text.charAt(i);
    }

    g.setColor(color_shadow);
    g.drawString(outputText, textX + 1, textY);
    g.drawString(outputText, textX - 1, textY);
    g.drawString(outputText, textX, textY + 1);
    g.drawString(outputText, textX, textY - 1);

    g.setColor(currentColor);
    g.drawString(outputText, textX, textY);
  }

  public static void drawShadowTextBorder(
      Graphics2D g,
      String text,
      int x,
      int y,
      Color textColor,
      float alpha,
      float boxAlpha,
      boolean border,
      int borderSize) {
    int textX = x;
    int textY = y;
    Dimension bounds = getStringBounds(g, text);
    textX -= (bounds.width / 2);
    textY += (bounds.height / 2);

    g.setColor(color_shadow);
    int rectX = x - (bounds.width / 2) - 2 - borderSize;
    int rectY = y - (bounds.height / 2) + 2 - borderSize;
    int rectWidth = bounds.width + 2 + (borderSize * 2);
    int rectHeight = bounds.height + (borderSize * 2);
    if (border) {
      setAlpha(g, 1.0f);
      g.drawRect(rectX, rectY, rectWidth, rectHeight);
    }
    setAlpha(g, boxAlpha);
    g.fillRect(rectX, rectY, rectWidth, rectHeight);
    setAlpha(g, alpha);
    g.drawString(text, textX + 1, textY);
    g.drawString(text, textX - 1, textY);
    g.drawString(text, textX, textY + 1);
    g.drawString(text, textX, textY - 1);

    g.setColor(textColor);
    g.drawString(text, textX, textY);
  }

  // rather than import someone else's font and try to get the unicode to work,
  // just draw the classic player control shapes myself. they're not that complex.
  public static void drawPlayerControlShape(Graphics2D g, int x, int y, int height, String icon) {
    int nPoints;
    int[] xPoints;
    int[] yPoints;
    int halfx = (int) (x + (float) height / 2.0);
    int halfy = (int) (y + (float) height / 2.0);

    switch (icon) {
      case "fastforward": // ⏩
        // this  icon is two right angle triangles touching
        nPoints = 7;
        xPoints = new int[nPoints];
        yPoints = new int[nPoints];

        // define shape
        xPoints[0] = x;
        yPoints[0] = y;

        xPoints[1] = halfx;
        yPoints[1] = halfy;

        xPoints[2] = halfx;
        yPoints[2] = y;

        xPoints[3] = x + height;
        yPoints[3] = halfy;

        xPoints[4] = halfx;
        yPoints[4] = y + height;

        xPoints[5] = halfx;
        yPoints[5] = halfy;

        xPoints[6] = x;
        yPoints[6] = y + height;

        // goes back to x,y to close shape by itself
        g.fillPolygon(xPoints, yPoints, nPoints);
        break;
      case "slowforward": // ⏪
        // if I was clever, I'd do some math on "fastforward"'s
        // xPoints coordinates to generate slowforward.
        // but it's simple enough to just manually swap "x" & "x+height"
        // faster to execute too...
        nPoints = 7;
        xPoints = new int[nPoints];
        yPoints = new int[nPoints];

        xPoints[0] = x + height;
        yPoints[0] = y;

        xPoints[1] = halfx;
        yPoints[1] = halfy;

        xPoints[2] = halfx;
        yPoints[2] = y;

        xPoints[3] = x;
        yPoints[3] = halfy;

        xPoints[4] = halfx;
        yPoints[4] = y + height;

        xPoints[5] = halfx;
        yPoints[5] = halfy;

        xPoints[6] = x + height;
        yPoints[6] = y + height;

        // goes back to x,y to close shape by itself
        g.fillPolygon(xPoints, yPoints, nPoints);
        break;
      case "next": // ⏭
        // this is just fastforward but with a line drawn next  to it
        // the line should be 1 px width;
        drawPlayerControlShape(g, x, y, height, "fastforward");
        g.drawLine(x + height, y, x + height, y + height - 1);
        break;
      case "previous": // ⏮
        drawPlayerControlShape(g, x, y, height, "slowforward");
        g.drawLine(x, y, x, y + height - 1);
        break;
      case "playpause": // ⏯
        // pause's white space is as wide as single line of pause
        // each element in pause is 1/3rd width of triangle
        nPoints = 3;
        xPoints = new int[nPoints];
        yPoints = new int[nPoints];

        xPoints[0] = x;
        yPoints[0] = y;

        xPoints[1] = halfx;
        yPoints[1] = halfy;

        xPoints[2] = x;
        yPoints[2] = y + height;

        g.fillPolygon(xPoints, yPoints, nPoints); // triangle-y part

        // now draw pause symbol next to it
        g.fillRect(halfx, y, (int) ((float) height / 6.0), height);
        g.fillRect(
            (int) (x + height - ((float) height / 6.0)), y, (int) ((float) height / 6.0), height);
        break;
      case "stop": // ⏹
        // just a rectangle. :)
        // slightly smaller than height
        g.fillRect(
            x + (int) (height * 0.15),
            y + (int) (height * 0.15),
            (int) (height * 0.80),
            (int) (height * 0.80));
        break;
      case "queue":
        // this shape looks like ":=" but with an extra dot, and an extra line
        int right_bit_width = (int) ((float) height * 1.2);
        int line_size = (int) ((float) height / 5.0) + 1;
        for (int i = 0; i < 3; i++) {
          g.fillRect(x, y + (int) (i * line_size * 1.5), line_size, line_size); // left dot
          g.fillRect(
              x + line_size * 2,
              y + (int) (i * line_size * 1.5),
              right_bit_width,
              line_size); // right rectangle
        }
        break;

      default:
        Logger.Debug("drawPlayerControlShape given invalid shape");
    }
  }

  public static void drawBox(int x, int y, int w, int h, int color) {
    if (Reflection.drawBox == null) return;

    try {
      Reflection.drawBox.invoke(instance, x, (byte) -127, color, y, h, w);
    } catch (Exception e) {
    }
  }

  public static void drawBoxBorder(int x, int y, int w, int h, int color) {
    if (Reflection.drawBoxBorder == null) return;

    try {
      Reflection.drawBoxBorder.invoke(instance, x, w, y, 27785, h, color);
    } catch (Exception e) {
    }
  }

  public static void drawString(String text, int x, int y, int font, int color) {
    if (Reflection.drawString == null) return;

    try {
      Reflection.drawString.invoke(instance, text, x, y, color, false, font);
    } catch (Exception e) {
    }
  }

  public static void drawStringCenter(String text, int x, int y, int font, int color) {
    if (Reflection.drawStringCenter == null) return;

    try {
      Reflection.drawStringCenter.invoke(instance, x, text, color, 0, font, y);
    } catch (Exception e) {
    }
  }

  public static void drawSprite(int x, int y, int id) {
    if (Reflection.drawSprite == null) return;

    try {
      Reflection.drawSprite.invoke(instance, -1, id, y, x);
    } catch (Exception e) {
    }
  }

  /** Grabs a screenshot intent from the buffer */
  public static boolean getScreenshotFlag() {
    if (screenshotFlagBuffer.peek() == null) {
      return false;
    }

    return screenshotFlagBuffer.poll();
  }

  public static void takeScreenshot(boolean quiet) {
    quietScreenshot = quiet;

    // Add a screenshot intent to the buffer
    screenshotFlagBuffer.add(true);
  }

  private static String fixLengthString(String string) {
    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) == '~' && string.charAt(i + 4) == '~') {
        String coord = string.substring(i + 1, 3);
        string = string.replace(coord, "0" + coord);
      }
    }
    return string;
  }

  private static Dimension getStringBounds(Graphics2D g, String str) {
    FontRenderContext context = g.getFontRenderContext();
    Rectangle2D bounds = g.getFont().getStringBounds(str, context);
    return new Dimension((int) bounds.getWidth(), (int) bounds.getHeight());
  }

  private static Color colorFromCode(String s) {
    int hexCode = 0xffffff;

    if (s.substring(1, 4).equalsIgnoreCase("red")) hexCode = 0xff0000;
    else if (s.substring(1, 4).equalsIgnoreCase("lre")) hexCode = 0xff9040;
    else if (s.substring(1, 4).equalsIgnoreCase("yel")) hexCode = 0xffff00;
    else if (s.substring(1, 4).equalsIgnoreCase("gre")) hexCode = 65280;
    else if (s.substring(1, 4).equalsIgnoreCase("blu")) hexCode = 255;
    else if (s.substring(1, 4).equalsIgnoreCase("cya")) hexCode = 65535;
    else if (s.substring(1, 4).equalsIgnoreCase("mag")) hexCode = 0xff00ff;
    else if (s.substring(1, 4).equalsIgnoreCase("whi")) hexCode = 0xffffff;
    else if (s.substring(1, 4).equalsIgnoreCase("bla")) hexCode = 0;
    else if (s.substring(1, 4).equalsIgnoreCase("dre")) hexCode = 0xc00000;
    else if (s.substring(1, 4).equalsIgnoreCase("ora")) hexCode = 0xff9040;
    else if (s.substring(1, 4).equalsIgnoreCase("ran")) hexCode = (int) (Math.random() * 16777215D);
    else if (s.substring(1, 4).equalsIgnoreCase("or1")) hexCode = 0xffb000;
    else if (s.substring(1, 4).equalsIgnoreCase("or2")) hexCode = 0xff7000;
    else if (s.substring(1, 4).equalsIgnoreCase("or3")) hexCode = 0xff3000;
    else if (s.substring(1, 4).equalsIgnoreCase("gr1")) hexCode = 0xc0ff00;
    else if (s.substring(1, 4).equalsIgnoreCase("gr2")) hexCode = 0x80ff00;
    else if (s.substring(1, 4).equalsIgnoreCase("gr3")) hexCode = 0x40ff00;

    return new Color(hexCode);
  }

  private static void drawNPCBar(Graphics2D g, int x, int y, NPC npc) {
    Dimension bounds = new Dimension(173, 40);
    float hp_ratio = (float) (npc.currentHits) / (float) (npc.maxHits);

    // Container
    setAlpha(g, 0.5f);
    g.setColor(color_gray);
    g.fillRect(x - 1, y - 1, bounds.width + 2, bounds.height + 2);
    g.setColor(color_shadow);
    g.fillRect(x, y, bounds.width, bounds.height);

    // HP bar
    setAlpha(g, 1.0f);
    g.setColor(new Color(99, 20, 19));
    g.fillRect(x, y + 20, bounds.width, bounds.height / 2);
    g.setColor(new Color(10, 134, 51));
    g.fillRect(x, y + 20, (int) (bounds.width * hp_ratio), bounds.height / 2);

    // HP text
    if (Settings.NPC_HEALTH_SHOW_PERCENTAGE.get(Settings.currentProfile))
      drawShadowText(
          g,
          (int) Math.ceil(hp_ratio * 100) + "%",
          x + (bounds.width / 2),
          y + (bounds.height / 2) + 8,
          color_text,
          true);
    else
      drawShadowText(
          g,
          npc.currentHits + "/" + npc.maxHits,
          x + (bounds.width / 2),
          y + (bounds.height / 2) + 8,
          color_text,
          true);

    // NPC name
    drawShadowText(
        g, npc.name, x + (bounds.width / 2), y + (bounds.height / 2) - 12, color_text, true);
  }

  public static void prepareNewSceneryScreenshotSession(int id) {
    screenshot_scenery_scenery_id = id;
    screenshot_scenery_frames = 300;
    screenshot_scenery_angle = 0;
    /*
    if (JGameData.objectWidths[id] > 1 || JGameData.objectHeights[id] > 1) {
      int widthOffset = 0;
      int heightOffset = 0;
      if (JGameData.sceneryWidths[id] > 1) {
        widthOffset = (int) ((JGameData.sceneryWidths[id]) / 2.0f) * 128;
      }
      if (JGameData.objectHeights[id] > 1) {
        heightOffset = (int) ((JGameData.objectHeights[id]) / 2.0f) * 128;
      }
      switch (screenshot_scenery_scenery_rotation) {
        case 0:
          Camera.add_lookat_x = widthOffset;
          Camera.add_lookat_y = heightOffset;
          break;
        case 1:
        case 2:
        case 3:
        case 4:
          Camera.add_lookat_x = -widthOffset;
          Camera.add_lookat_y = heightOffset;
          break;
        case 5:
        case 6:
        case 7:
        default:
          Camera.add_lookat_x = 0;
          Camera.add_lookat_y = 0;
          break;
      }

    } else {
      Camera.add_lookat_x = 0;
      Camera.add_lookat_y = 0;
    }
    */
    Camera.add_lookat_x = 0;
    Camera.add_lookat_y = 0;
    Camera.rotation = screenshot_scenery_angle;
    Camera.delta_rotation = (float) Camera.rotation;
  }
}

class ItemComparator implements Comparator<Item> {

  @Override
  public int compare(Item a, Item b) {
    // this is reverse alphabetical order b/c we display them/in reverse order (y-=12 ea item)
    int offset = a.getName().compareToIgnoreCase(b.getName()) * -1;
    if (offset > 0) { // item a is alphabetically before item b
      offset = 10;
    } else if (offset < 0) { // item b is alphabetically before item a
      offset = -10;
      // items have the same name we would like to group items that are on the same tile as well,
      // not just having
      // the same name, so that we can use "last_item" in a useful way
    } else {
      if (a.x == b.x && a.y == b.y) {
        offset = 0; // name is the same and so is location, items are considered equal
      } else {
        if (a.x < b.x) {
          offset = -5;
        } else {
          offset = 5;
        }
      }
    }
    return offset;
  }
}
