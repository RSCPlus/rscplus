package Client;

import Game.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.imageio.ImageIO;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import Game.Renderer;
import org.json.JSONArray;
import org.json.JSONObject;

public class WorldMapWindow {
  private static class CameraPoint {
    public float x;
    public float y;

    public CameraPoint(float x, float y) {
      this.x = x;
      this.y = y;
    }
  };

  private static JFrame frame;
  private static JPanel mapView;

  private static Point prevMousePoint;
  private static Point prevMousePointMap;
  private static CameraPoint cameraCurrentPosition;
  private static Point cameraPosition;
  private static float cameraFloatPosX;
  private static float cameraFloatPosY;
  private static Point cameraLerpPosition;
  private static float cameraLerpElapsed;

  private static Font fonts[];
  private static Font fontsBold[];
  private static Image planes[];
  private static Image legends[];
  public static Image directions[];
  private static Image pointImage;
  private static Image waypointImage;
  private static int planeIndex;
  public static boolean renderChunkGrid;
  private static boolean showLegend;
  public static boolean showLabels;
  public static boolean showScenery;
  public static boolean showIcons;
  public static boolean showOtherFloors;
  private static boolean followPlayer;
  private static String searchText;
  private static int searchNumber;
  private static int searchIndex;
  private static SearchResult[] searchResults;
  private static boolean searchOverflow;
  private static boolean searchValid;
  private static float zoom = 2.0f;

  private static boolean developmentMode;

  private static Rectangle zoomUpBounds;
  private static Rectangle zoomDownBounds;
  private static Rectangle floorUpBounds;
  private static Rectangle floorDownBounds;
  private static Point floorTextBounds;
  private static Point zoomTextBounds;
  private static Point posTextBounds;
  private static Rectangle legendBounds;
  private static Rectangle chunkGridBounds;
  private static Rectangle showLabelsBounds;
  private static Rectangle showSceneryBounds;
  private static Rectangle showIconsBounds;
  private static Rectangle showOtherFloorsBounds;
  private static Rectangle followPlayerBounds;
  private static Rectangle searchBounds;
  private static Rectangle searchRefreshBounds;

  private static Point waypointPosition;
  private static float waypointAngle;
  private static int waypointFloor;

  private static Point playerPosition;
  private static int playerPlane;

  private static int BORDER_SIZE = 8;

  private static int keyboardModMask = 0;

  public static Color color_water = new Color(36, 64, 127);
  public static Color color_scenery_tree = new Color(0, 161, 49);
  public static Color color_scenery_deadtree = new Color(125, 67, 17);
  public static Color color_scenery_normal = new Color(195, 99, 30);
  public static Color color_scenery_indoors = new Color(0, 255, 255);

  public static final int SEARCH_RESULTS_MAX = 5;
  public static final int SEARCH_RESULTS_LIMIT = 10000;

  private static ArrayList<MapLabel> mapLabels = new ArrayList<MapLabel>();
  private static ArrayList<MapScenery> mapSceneries = new ArrayList<MapScenery>();
  private static ArrayList<MapGlyph> mapGlyphs = new ArrayList<MapGlyph>();
  private static ArrayList<MapLink> mapLinks = new ArrayList<MapLink>();

  private static BufferedImage[] mapImageBuffer;
  private static boolean[] mapImageUpdate;

  private static Object renderLock = new Object();

  private static String legendText[] = {
    "Rare Trees",
    "Dungeon Entrance",
    "Altar",
    "Amulet Shop",
    "Anvil (for smithing)",
    "Apothecary",
    "Archery Shop",
    "Armour Conversion",
    "Axe Shop",
    "Bank",
    "Bed",
    "Body-Armour Shop",
    "Certificate Trader",
    "Clothes Shop",
    "Combat Practice",
    "Cookery Shop",
    "Crafting Shop",
    "Fishing Shop",
    "Food Shop",
    "Furnace (for smelting)",
    "Gem Shop",
    "General Store",
    "Helmet Shop",
    "Herblaw shop",
    "Jewellery Shop",
    "Kebab Shop",
    "Leg-Armour Shop",
    "Mace Shop",
    "Magic Shop",
    "Mining Site",
    "Pickable Lock",
    "Pub/Inn",
    "Quest start point",
    "Scimitar Shop",
    "Shield Shop",
    "Silk Trader",
    "Skirt-Armour Shop",
    "Spinning Wheel",
    "Staff Shop",
    "Sword Shop",
    "Tannery",
    "Fishing Point"
  };

  public WorldMapWindow() {
    try {
      // Set System L&F as a fall-back option.
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          NimbusLookAndFeel laf = (NimbusLookAndFeel) UIManager.getLookAndFeel();
          laf.getDefaults().put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 11));
          laf.getDefaults().put("Table.alternateRowColor", new Color(230, 230, 255));
          break;
        }
      }
    } catch (UnsupportedLookAndFeelException e) {
      Logger.Error("Unable to set L&F: Unsupported look and feel");
    } catch (ClassNotFoundException e) {
      Logger.Error("Unable to set L&F: Class not found");
    } catch (InstantiationException e) {
      Logger.Error("Unable to set L&F: Class object cannot be instantiated");
    } catch (IllegalAccessException e) {
      Logger.Error("Unable to set L&F: Illegal access exception");
    }
    initialize();
  }

  /** Initialize the contents of the frame. */
  private static void initialize() {
    Logger.Info("Creating world map window");
    try {
      SwingUtilities.invokeAndWait(
          new Runnable() {
            @Override
            public void run() {
              runInit();
            }
          });
    } catch (InvocationTargetException e) {
      Logger.Error("There was a thread-related error while setting up the world map window!");
      e.printStackTrace();
    } catch (InterruptedException e) {
      Logger.Error(
          "There was a thread-related error while setting up the world map window! The window may not be initialized properly!");
      e.printStackTrace();
    }

    cameraPosition = new Point(0, 0);
    waypointPosition = null;
    floorUpBounds = new Rectangle(0, 0, 24, 24);
    floorDownBounds = new Rectangle(0, 0, 24, 24);
    zoomUpBounds = new Rectangle(0, 0, 24, 24);
    zoomDownBounds = new Rectangle(0, 0, 24, 24);
    floorTextBounds = new Point(0, 0);
    zoomTextBounds = new Point(0, 0);
    posTextBounds = new Point(0, 0);
    prevMousePoint = new Point(0, 0);
    prevMousePointMap = new Point(0, 0);
    playerPosition = new Point(0, 0);
    chunkGridBounds = new Rectangle(0, 0, 116, 24);
    showLabelsBounds = new Rectangle(0, 0, 116, 24);
    showSceneryBounds = new Rectangle(0, 0, 116, 24);
    showIconsBounds = new Rectangle(0, 0, 116, 24);
    showOtherFloorsBounds = new Rectangle(0, 0, 116, 24);
    followPlayerBounds = new Rectangle(0, 0, 116, 24);
    legendBounds = new Rectangle(0, 0, 150, 24);
    searchBounds = new Rectangle(0, 0, 250, 24);
    searchRefreshBounds = new Rectangle(0, 0, 64, 24);
    planeIndex = 0;
    playerPlane = -1;
    followPlayer = true;
    searchText = "";
    searchIndex = 0;
    cameraLerpPosition = null;
    developmentMode = false;

    GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
    GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();

    // If the source image has no alpha info use Transparency.OPAQUE instead

    mapImageBuffer = new BufferedImage[4];
    for (int i = 0; i < 4; i++)
      mapImageBuffer[i] =
          graphicsConfiguration.createCompatibleImage(
              2448 + 1, 2736 + 1, Transparency.OPAQUE); // + 1 coords for border
    mapImageUpdate = new boolean[4];
    // mapImageBuffer = new BufferedImage(2448, 2736, BufferedImage.TYPE_INT_RGB);
  }

  private static MapScenery getSceneryAtCoord(int x, int y) {
    for (MapScenery scenery : mapSceneries) {
      if (scenery.x == x && scenery.y == y) return scenery;
    }
    return null;
  }

  private static void updateMapFloorRender(int plane, boolean force) {
    if (!force && mapImageUpdate[plane]) return;

    int mapWidth = mapImageBuffer[plane].getWidth();
    int mapHeight = mapImageBuffer[plane].getHeight();

    Graphics2D g = (Graphics2D) mapImageBuffer[plane].getGraphics();

    if (showOtherFloors || planeIndex == 0) g.setColor(color_water);
    else g.setColor(Color.black);

    synchronized (renderLock) {
      g.fillRect(0, 0, mapWidth, mapHeight);

      if (showOtherFloors) {
        if (plane == 3) {
          g.drawImage(planes[0], 0, 0, null);
          setAlpha(g, 0.8f);
          g.setColor(Color.black);
          g.fillRect(0, 0, mapWidth, mapHeight);
          setAlpha(g, 1.0f);
        } else {
          for (int i = 0; i < plane; i++) {
            g.drawImage(planes[i], 0, 0, null);
            setAlpha(g, 0.5f);
            g.setColor(Color.black);
            g.fillRect(0, 0, mapWidth, mapHeight);
            setAlpha(g, 1.0f);
          }
        }
      }
      g.drawImage(planes[plane], 0, 0, null);

      if (showScenery) {
        for (MapScenery scenery : mapSceneries) {
          Rectangle p = convertWorldCoordsToMapRaw(scenery.x, scenery.y);
          if (p.width != plane) continue;

          if (scenery.isSearchable()
              && searchText.length() > 0
              && (searchNumber == scenery.id
                  || scenery.searchName.toLowerCase().contains(searchText.toLowerCase())))
            g.setColor(Renderer.color_item_highlighted);
          else if (p.height == 0 && (scenery.id == 0 || scenery.id == 1))
            g.setColor(color_scenery_tree);
          else if (p.height == 1
              && (scenery.id == 70
                  || scenery.id == 205
                  || scenery.id == 38
                  || scenery.id == 4
                  || scenery.id == 208
                  || scenery.id == 108)) g.setColor(color_scenery_deadtree);
          else if (plane == 0 || plane == 3) g.setColor(color_scenery_normal);
          else g.setColor(color_scenery_indoors);

          // Renderer.drawShadowText(g, scenery.id + "", p.x, p.y - 16, Renderer.color_text, true);
          drawMapPoint(g, p.x, p.y);
        }
      }

      for (MapLink link : mapLinks) {
        Rectangle p = convertWorldCoordsToMapRaw(link.loc.x, link.loc.y);
        if (p.width != plane) continue;

        int x = p.x;
        int y = p.y;
        int w = link.loc.width;
        int h = link.loc.height;
        g.setColor(Renderer.color_text);
        g.fillOval(x, y, w, h);
        g.setColor(Renderer.color_shadow);
        g.drawOval(x, y, w, h);
      }

      // Only draw labels and glyphs for floor 0
      if (plane == 0) {
        if (showIcons) {
          for (MapGlyph glyph : mapGlyphs) {
            if (glyph.id == -1) continue;

            int glyphX = glyph.x;
            int glyphY = glyph.y;

            Color glyphBackgroundColor = Renderer.color_text;
            if (searchText.length() > 0
                && legendText[glyph.id].toLowerCase().contains(searchText.toLowerCase()))
              glyphBackgroundColor = Renderer.color_item_highlighted;

            g.setColor(glyphBackgroundColor);
            g.fillOval(glyphX, glyphY, 14, 14);
            g.setColor(Renderer.color_shadow);
            g.drawOval(glyphX, glyphY, 14, 14);
            g.drawImage(
                legends[glyph.id],
                glyphX + (7 - legends[glyph.id].getWidth(null) / 2),
                glyphY + (7 - legends[glyph.id].getHeight(null) / 2),
                null);
          }
        }

        if (showLabels) {
          for (MapLabel label : mapLabels) drawMapLabel(g, label.x, label.y, label);
        }
      }

      int chunkSize = 48 * 3;

      if (renderChunkGrid) {
        g.setColor(Renderer.color_shadow);
        setAlpha(g, 0.5f);
        for (int x = 0; x <= mapWidth; x += chunkSize) g.drawLine(x, 0, x, mapHeight);
        for (int y = 0; y <= mapHeight; y += chunkSize) g.drawLine(0, y, mapWidth, y);
        setAlpha(g, 1.0f);
      }

      for (int x = 0; x < AreaDefinition.SIZE_X; x++)
      {
        for (int y = 0; y < AreaDefinition.SIZE_Y; y++)
        {
          int indexX = AreaDefinition.SIZE_X - x - 1;
          int indexY = y;
          int drawX = x * chunkSize;
          int drawY = y * chunkSize + 16;
          g.setFont(Renderer.font_main);

          Color color = Renderer.color_hp;
          String music = Client.areaDefinitions[indexX][indexY].music;

          if (music.length() == 0)
            continue;

          Renderer.drawShadowText(g, indexX + ", " + indexY + ": " + music, drawX, drawY, color, false);
        }
      }
    }

    g.dispose();
    mapImageUpdate[plane] = true;
  }

  private static void updateMapRender() {
    for (int i = 0; i < 4; i++) mapImageUpdate[i] = false;

    updateMapFloorRender(planeIndex, false);
  }

  private static void setAlpha(Graphics2D g, float alpha) {
    g.setComposite(AlphaComposite.SrcOver.derive(alpha));
  }

  private static void drawButton(Graphics2D g, String text, Rectangle bounds) {
    g.setFont(Renderer.font_main);
    setAlpha(g, 0.5f);
    g.setColor(Renderer.color_text);
    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    setAlpha(g, 1.0f);
    g.setColor(Renderer.color_shadow);
    g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    Renderer.drawShadowText(
        g,
        text,
        bounds.x + (bounds.width / 2),
        bounds.y + (bounds.height / 2),
        Renderer.color_text,
        true);
  }

  private static void buildSearchResults() {
    boolean updateMap = false;
    String prevSearchText = searchText;

    searchOverflow = false;

    if (searchValid) updateMap = true;
    searchValid = false;

    if (searchText.length() == 0) {
      searchResults = null;
      if (updateMap) updateMapRender();
      return;
    }

    searchNumber = -1;
    try {
      searchNumber = Integer.parseInt(searchText);
    } catch (Exception e) {
    }

    ArrayList<SearchResult> results = new ArrayList<SearchResult>();
    for (SearchResult glyph : mapGlyphs) {
      if (glyph.isSearchable()
          && glyph.getSearchName().toLowerCase().contains(searchText.toLowerCase())) {
        results.add(glyph);
        if (results.size() > SEARCH_RESULTS_LIMIT) {
          searchOverflow = true;
          searchResults = null;
          if (updateMap) {
            searchText = "";
            updateMapRender();
            searchText = prevSearchText;
          }
          return;
        }
      }
    }

    for (SearchResult label : mapLabels) {
      if (label.isSearchable()
          && label.getSearchName().toLowerCase().contains(searchText.toLowerCase())) {
        results.add(label);
        if (results.size() > SEARCH_RESULTS_LIMIT) {
          searchOverflow = true;
          searchResults = null;
          if (updateMap) {
            searchText = "";
            updateMapRender();
            searchText = prevSearchText;
          }
          return;
        }
      }
    }

    for (SearchResult scenery : mapSceneries) {
      MapScenery sceneryObj = (MapScenery) scenery;
      if (scenery.isSearchable()
          && (sceneryObj.id == searchNumber
              || scenery.getSearchName().toLowerCase().contains(searchText.toLowerCase()))) {
        results.add(scenery);
        if (results.size() > SEARCH_RESULTS_LIMIT) {
          searchOverflow = true;
          searchResults = null;
          if (updateMap) {
            searchText = "";
            updateMapRender();
            searchText = prevSearchText;
          }
          return;
        }
      }
    }

    // Sort search if player is logged in
    if (playerPlane != -1) Collections.sort(results, new SearchResultComparator());

    SearchResult[] prevResults = searchResults;
    searchResults = new SearchResult[results.size()];
    searchResults = results.toArray(searchResults);
    searchIndex = 0;
    searchValid = searchResults.length > 0;

    if (!searchValid && updateMap) {
      searchText = "";
      updateMapRender();
    } else if (searchValid && (prevResults == null || searchResults.length != prevResults.length)) {
      updateMapRender();
    }
    searchText = prevSearchText;
  }

  private static void drawSearch(Graphics2D g, String text, Rectangle bounds) {
    String searchTerm = text;
    Color searchColor = Renderer.color_text;
    if (searchTerm.length() == 0) {
      searchTerm = "Type to search...";
      searchColor = Color.gray;
    } else {
      searchTerm += "*";
    }

    g.setFont(Renderer.font_main);
    g.setColor(Renderer.color_gray);
    setAlpha(g, 0.5f);
    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    g.setColor(Renderer.color_shadow);
    setAlpha(g, 1.0f);
    g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    Renderer.drawShadowText(
        g,
        searchTerm,
        bounds.x + BORDER_SIZE,
        bounds.y + (bounds.height / 2) + 4,
        searchColor,
        false);

    if (searchResults != null || searchOverflow) {
      // Calculate height
      int searchHeight;
      if (searchOverflow || searchResults.length == 0) searchHeight = 16 + BORDER_SIZE;
      else if (searchResults.length <= SEARCH_RESULTS_MAX)
        searchHeight = searchResults.length * 16 + BORDER_SIZE;
      else searchHeight = (SEARCH_RESULTS_MAX + 1) * 16 + BORDER_SIZE;

      if (searchHeight > 0) {
        g.setColor(Renderer.color_gray);
        setAlpha(g, 0.5f);
        g.fillRect(bounds.x, bounds.y - searchHeight, bounds.width, searchHeight);
        g.setColor(Renderer.color_shadow);
        setAlpha(g, 1.0f);
        g.drawRect(bounds.x, bounds.y - searchHeight, bounds.width, searchHeight);
      }

      boolean hasFirstResult = false;
      for (int i = 0; i < SEARCH_RESULTS_MAX; i++) {
        // No results
        if (searchResults == null || searchResults.length == 0) break;

        int index = (searchIndex + i) % searchResults.length;
        Color resultColor = Renderer.color_gray;
        if (searchIndex == index) {
          if (!hasFirstResult) {
            resultColor = Renderer.color_text;
            hasFirstResult = true;
          } else {
            break;
          }
        }
        SearchResult result = searchResults[index];

        String resultName = Integer.toString(index + 1) + ". " + result.getSearchName();
        String extraName = result.getSearchNameExtra();
        if (extraName.length() > 0) {
          resultName += " [" + extraName + "]";
        }

        Renderer.drawShadowText(
            g, resultName, bounds.x + BORDER_SIZE, bounds.y - 8 - (i * 16), resultColor, false);

        // Only 1 result
        if (searchResults.length == 1) break;
      }

      if (searchOverflow) {
        Renderer.drawShadowText(
            g,
            "Too many results, narrow your search",
            bounds.x + BORDER_SIZE,
            bounds.y - 8,
            Renderer.color_text,
            false);
      } else if (searchResults.length == 0) {
        Renderer.drawShadowText(
            g,
            "No matches found",
            bounds.x + BORDER_SIZE,
            bounds.y - 8,
            Renderer.color_text,
            false);
      } else {
        if (searchResults.length > SEARCH_RESULTS_MAX) {
          int remaining = searchResults.length - searchIndex;
          String resultText = "results";
          if (remaining == 1) resultText = "result";
          Renderer.drawShadowText(
              g,
              Integer.toString(remaining) + " more " + resultText + "...",
              bounds.x + BORDER_SIZE,
              bounds.y - 8 - (SEARCH_RESULTS_MAX * 16),
              Renderer.color_text,
              false);
        }
      }
    }
  }

  private static boolean process(Point mouse, Rectangle bounds) {
    if (mouse.x >= bounds.x
        && mouse.y >= bounds.y
        && mouse.x <= bounds.x + bounds.width
        && mouse.y <= bounds.y + bounds.height) return true;

    return false;
  }

  private static void setLerpPosition(int x, int y) {
    cameraLerpPosition = new Point(x, y);
    cameraFloatPosX = cameraCurrentPosition.x;
    cameraFloatPosY = cameraCurrentPosition.y;
    cameraLerpElapsed = 0.0f;
  }

  private static void runInit() {
    initAssets();

    cameraCurrentPosition =
        new CameraPoint(planes[0].getWidth(null) / 2, planes[0].getHeight(null) / 2);

    // Initialize window
    frame = new JFrame();
    frame.setTitle("World Map");
    frame.setBounds(0, 0, 800, 580);
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    frame.getContentPane().setLayout(new BorderLayout());
    URL iconURL = Launcher.getResource("/assets/icon.png");
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      frame.setIconImage(icon.getImage());
    }

    // Initialize map view
    mapView = new JPanel();
    mapView.setLayout(new BorderLayout());
    mapView.add(new MapRenderer());
    mapView.revalidate();
    mapView.repaint();
    mapView.setVisible(true);

    frame.addFocusListener(
        new FocusListener() {
          @Override
          public void focusGained(FocusEvent e) {}

          @Override
          public void focusLost(FocusEvent e) {
            keyboardModMask = 0;
          }
        });

    frame.addMouseWheelListener(
        new MouseWheelListener() {
          @Override
          public void mouseWheelMoved(MouseWheelEvent e) {
            int notches = e.getWheelRotation() * -1;
            setZoom(zoom + notches);
            mapView.repaint();
            e.consume();
          }
        });

    frame.addKeyListener(
        new KeyListener() {
          @Override
          public void keyTyped(KeyEvent e) {
            String prevSearchText = searchText;

            // We currently do not support ctrl, it will add garbage to search
            int stepMask = InputEvent.CTRL_MASK;
            if ((e.getModifiers() & stepMask) == stepMask) return;

            char keyChar = e.getKeyChar();

            if (keyChar == KeyEvent.VK_DELETE
                || keyChar == KeyEvent.VK_ESCAPE
                || keyChar == KeyEvent.VK_ENTER) return;

            if (keyChar == KeyEvent.VK_BACK_SPACE) {
              if (searchText.length() > 0)
                searchText = searchText.substring(0, searchText.length() - 1);
            } else {
              searchText += keyChar;
            }

            buildSearchResults();

            if (prevSearchText != searchText) {
              mapView.repaint();
            }
          }

          @Override
          public void keyPressed(KeyEvent e) {
            boolean dirty = false;

            keyboardModMask |= e.getModifiers();

            int devMask = InputEvent.ALT_MASK | InputEvent.CTRL_MASK;
            if ((e.getModifiers() & devMask) == devMask) {
              // Ctrl + Alt + D enables development mode
              if (e.getKeyCode() == KeyEvent.VK_D) {
                developmentMode = !developmentMode;
                dirty = true;
              }

              // Ctrl + Alt + R reloads assets and rebuilds map
              if (e.getKeyCode() == KeyEvent.VK_R) {
                initAssets();
                initScenery();
                updateMapRender();
                dirty = true;
              }
            }

            if (searchResults != null && searchResults.length > 1) {
              if (e.getKeyCode() == KeyEvent.VK_UP) {
                searchIndex += 1;
                if (searchIndex >= searchResults.length) searchIndex = 0;
                dirty = true;
              }

              if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                searchIndex -= 1;
                if (searchIndex < 0) searchIndex = searchResults.length - 1;
                dirty = true;
              }
            } else {
              if (e.getKeyCode() == KeyEvent.VK_UP) {
                setFloor(planeIndex + 1);
                dirty = true;
              }
              if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                setFloor(planeIndex - 1);
                dirty = true;
              }
            }

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              if (searchText.length() > 0) {
                if (searchResults != null && searchResults.length > 0) {
                  SearchResult result = searchResults[searchIndex];

                  if (result.getSearchType() == SearchResult.SEARCH_GLYPH) {
                    MapGlyph glyph = (MapGlyph) result;
                    setLerpPosition(glyph.x, glyph.y);
                    setFloor(0);
                    followPlayer = false;
                    dirty = true;
                  } else if (result.getSearchType() == SearchResult.SEARCH_LABEL) {
                    MapLabel label = (MapLabel) result;
                    setLerpPosition(label.x, label.y);
                    setFloor(0);
                    followPlayer = false;
                    dirty = true;
                  } else if (result.getSearchType() == SearchResult.SEARCH_SCENERY) {
                    MapScenery scenery = (MapScenery) result;
                    Rectangle p = convertWorldCoordsToMapRaw(scenery.x, scenery.y);
                    setLerpPosition(p.x, p.y);
                    setFloor(p.width);
                    followPlayer = false;
                    dirty = true;
                  }

                  // Wrap search index
                  searchIndex += 1;
                  if (searchIndex >= searchResults.length) searchIndex = 0;
                }
              }
            }

            if (dirty) mapView.repaint();
          }

          @Override
          public void keyReleased(KeyEvent e) {
            keyboardModMask &= ~e.getModifiers();
          }
        });

    mapView.addMouseListener(
        new MouseListener() {
          @Override
          public void mouseClicked(MouseEvent e) {
            Point p = e.getPoint();
          }

          @Override
          public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            Point scaledPoint = new Point(getInvZoomInt(p.x), getInvZoomInt(p.y));

            // Get world coords
            Point worldCoords =
                new Point(
                    getInvZoomInt(cameraPosition.x) + scaledPoint.x,
                    getInvZoomInt(cameraPosition.y) + scaledPoint.y);
            worldCoords.x = ((planes[planeIndex].getWidth(null) - worldCoords.x - 4) / 3) + 1;
            worldCoords.y = (worldCoords.y / 3) + (planeIndex * 944);

            if (developmentMode) {
              if (e.getButton() == MouseEvent.BUTTON2) {
                Point p2 =
                    new Point(
                        getInvZoomInt(cameraPosition.x) + scaledPoint.x,
                        getInvZoomInt(cameraPosition.y) + scaledPoint.y);
                MapScenery sceneryObject = getSceneryAtCoord(worldCoords.x, worldCoords.y);
                String objectInfo = "";
                if (sceneryObject != null)
                  objectInfo = "; Scenery ID: " + Integer.toString(sceneryObject.id);
                Logger.Info(
                    "[MAPDEVMODE] Map Coordinates: "
                        + p2.x
                        + ", "
                        + p2.y
                        + "; Point Coordinates: "
                        + (p2.x - 7)
                        + ", "
                        + (p2.y - 7)
                        + objectInfo);
                return;
              }
            }

            int shiftClickMask = KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK;
            if ((keyboardModMask & shiftClickMask) == shiftClickMask) {
              if (e.getButton() == MouseEvent.BUTTON1) {
                // Teleport user on servers that allow them to
                Object buffer = StreamUtil.getStreamBuffer();
                StreamUtil.newPacket(59);
                StreamUtil.putShortTo(buffer, (short) worldCoords.x);
                StreamUtil.putShortTo(buffer, (short) worldCoords.y);
                StreamUtil.sendPacket();
              }
            }

            if (e.getButton() == MouseEvent.BUTTON3) {
              Point newPos = worldCoords;
              if (waypointPosition != null) {
                int distance =
                    (int)
                        Point.distance(waypointPosition.x, waypointPosition.y, newPos.x, newPos.y);
                if (distance <= 2) waypointPosition = null;
                else waypointPosition = newPos;
              } else {
                waypointPosition = newPos;
              }
            }

            if (e.getButton() == MouseEvent.BUTTON1) {
              if (process(p, floorUpBounds)) {
                setFloor(planeIndex + 1);
                followPlayer = false;
              }

              if (process(p, floorDownBounds)) {
                setFloor(planeIndex - 1);
                followPlayer = false;
              }

              if (process(p, zoomUpBounds)) {
                setZoom(zoom + 1.0f);
              }

              if (process(p, zoomDownBounds)) {
                setZoom(zoom - 1.0f);
              }

              if (process(p, chunkGridBounds)) {
                renderChunkGrid = !renderChunkGrid;
                Settings.save();
                updateMapRender();
              }

              if (process(p, showLabelsBounds)) {
                showLabels = !showLabels;
                Settings.save();
                updateMapFloorRender(0, true);
              }

              if (process(p, showSceneryBounds)) {
                showScenery = !showScenery;
                Settings.save();
                updateMapFloorRender(0, true);
              }

              if (process(p, showIconsBounds)) {
                showIcons = !showIcons;
                Settings.save();
                updateMapFloorRender(0, true);
              }

              if (planeIndex != 0) {
                if (process(p, showOtherFloorsBounds)) {
                  showOtherFloors = !showOtherFloors;
                  Settings.save();
                  updateMapRender();
                }
              }

              if (process(p, followPlayerBounds)) followPlayer = !followPlayer;

              if (searchText.length() > 0) {
                if (process(p, searchRefreshBounds)) buildSearchResults();
              }
            }

            for (MapLink link : mapLinks) {
              if (link.floor != planeIndex) continue;

              Rectangle linkPos = new Rectangle();
              linkPos.x = -cameraPosition.x + (planes[0].getWidth(null) - (link.loc.x * 3));
              linkPos.y =
                  -cameraPosition.y + (((link.loc.y - planeIndex) - (943 * planeIndex)) * 3);

              if (linkPos.x < 0
                  || linkPos.x > mapView.getWidth()
                  || linkPos.y < 0
                  || linkPos.y > mapView.getHeight()) continue;

              linkPos.width = link.loc.width;
              linkPos.height = link.loc.height;

              if (process(p, linkPos)) {
                int type = link.type;

                if (type == MapLink.LINK_BOTH_FLOOR) {
                  if (e.getButton() == MouseEvent.BUTTON1) {
                    type = MapLink.LINK_NEXT_FLOOR;
                  } else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (planeIndex > 0) type = MapLink.LINK_PREV_FLOOR;
                  }
                }

                if (type == MapLink.LINK_NEXT_FLOOR) setFloor(planeIndex + 1);
                else if (type == MapLink.LINK_PREV_FLOOR) setFloor(planeIndex - 1);

                break;
              }
            }

            prevMousePoint = p;
            mapView.repaint();
          }

          @Override
          public void mouseReleased(MouseEvent e) {}

          @Override
          public void mouseEntered(MouseEvent e) {}

          @Override
          public void mouseExited(MouseEvent e) {}
        });

    // Add listeners
    mapView.addMouseMotionListener(
        new MouseMotionListener() {
          @Override
          public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();

            showLegend = process(p, legendBounds);

            if (!SwingUtilities.isLeftMouseButton(e)
                && (!SwingUtilities.isMiddleMouseButton(e) || developmentMode)) return;

            int diffX = (prevMousePoint.x - p.x);
            int diffY = (prevMousePoint.y - p.y);

            prevMousePoint = p;
            if (diffX != 0 || diffY != 0) {
              followPlayer = false;
              cameraLerpPosition = null;
              cameraCurrentPosition.x += diffX / zoom;
              cameraCurrentPosition.y += diffY / zoom;

              int mapWidth = planes[0].getWidth(null);
              int mapHeight = planes[0].getHeight(null);
              if (cameraCurrentPosition.x < 0) cameraCurrentPosition.x = 0;
              if (cameraCurrentPosition.y < 0) cameraCurrentPosition.y = 0;
              if (cameraCurrentPosition.x > mapWidth) cameraCurrentPosition.x = mapWidth;
              if (cameraCurrentPosition.y > mapHeight) cameraCurrentPosition.y = mapHeight;

              mapView.repaint();
            }
          }

          @Override
          public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();

            showLegend = process(p, legendBounds);

            prevMousePoint = p;
            mapView.repaint();
          }
        });

    // Populate frame
    frame.setContentPane(mapView);
  }

  public static void initAssets() {
    // Load labels
    mapLabels.clear();
    try {
      String labelJson =
          Util.readString(Launcher.getResource("/assets/map/labels.json").openStream());
      JSONArray obj = new JSONArray(labelJson);
      for (int i = 0; i < obj.length(); i++) {
        JSONObject entry = obj.getJSONObject(i);
        MapLabel label = new MapLabel();
        label.text = entry.getString("text");
        label.x = entry.getInt("x");
        label.y = entry.getInt("y");
        label.size = entry.getInt("size");
        String align = entry.getString("align");
        label.centered = align.equalsIgnoreCase("center");
        label.bold = false;
        label.color = Color.white;
        if (entry.has("bold")) label.bold = entry.getBoolean("bold");
        if (entry.has("colour")) {
          String color = entry.getString("colour");
          color = color.substring(4, color.length() - 1);

          int[] colorRGB = new int[3];
          String[] colorString = color.split(",");
          for (int j = 0; j < 3; j++) {
            colorString[j] = colorString[j].trim();
            colorRGB[j] = Integer.parseInt(colorString[j]);
          }

          label.color = new Color(colorRGB[0], colorRGB[1], colorRGB[2]);
        }
        mapLabels.add(label);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Load Glyphs
    mapGlyphs.clear();
    try {
      String labelJson =
          Util.readString(Launcher.getResource("/assets/map/points.json").openStream());
      JSONArray obj = new JSONArray(labelJson);
      for (int i = 0; i < obj.length(); i++) {
        JSONObject entry = obj.getJSONObject(i);
        MapGlyph glyph = new MapGlyph();

        String type = entry.getString("type");
        if (type.equalsIgnoreCase("rare-trees")) glyph.id = 0;
        else if (type.equalsIgnoreCase("dungeon")) glyph.id = 1;
        else if (type.equalsIgnoreCase("altar")) glyph.id = 2;
        else if (type.equalsIgnoreCase("amulet-shop")) glyph.id = 3;
        else if (type.equalsIgnoreCase("anvil")) glyph.id = 4;
        else if (type.equalsIgnoreCase("apothecary")) glyph.id = 5;
        else if (type.equalsIgnoreCase("archery-shop")) glyph.id = 6;
        else if (type.equalsIgnoreCase("armour-conversion")) glyph.id = 7;
        else if (type.equalsIgnoreCase("axe-shop")) glyph.id = 8;
        else if (type.equalsIgnoreCase("bank")) glyph.id = 9;
        else if (type.equalsIgnoreCase("bed")) glyph.id = 10;
        else if (type.equalsIgnoreCase("body-armour-shop")) glyph.id = 11;
        else if (type.equalsIgnoreCase("certificate-trader")) glyph.id = 12;
        else if (type.equalsIgnoreCase("clothes-shop")) glyph.id = 13;
        else if (type.equalsIgnoreCase("combat-practice")) glyph.id = 14;
        else if (type.equalsIgnoreCase("cookery-shop")) glyph.id = 15;
        else if (type.equalsIgnoreCase("crafting-shop")) glyph.id = 16;
        else if (type.equalsIgnoreCase("fishing-shop")) glyph.id = 17;
        else if (type.equalsIgnoreCase("food-shop")) glyph.id = 18;
        else if (type.equalsIgnoreCase("furnace")) glyph.id = 19;
        else if (type.equalsIgnoreCase("gem-shop")) glyph.id = 20;
        else if (type.equalsIgnoreCase("general-shop")) glyph.id = 21;
        else if (type.equalsIgnoreCase("helmet-shop")) glyph.id = 22;
        else if (type.equalsIgnoreCase("herblaw-shop")) glyph.id = 23;
        else if (type.equalsIgnoreCase("jewellery-shop")) glyph.id = 24;
        else if (type.equalsIgnoreCase("kebab-shop")) glyph.id = 25;
        else if (type.equalsIgnoreCase("leg-armour-shop")) glyph.id = 26;
        else if (type.equalsIgnoreCase("mace-shop")) glyph.id = 27;
        else if (type.equalsIgnoreCase("magic-shop")) glyph.id = 28;
        else if (type.equalsIgnoreCase("mining-site")) glyph.id = 29;
        else if (type.equalsIgnoreCase("pickable-lock")) glyph.id = 30;
        else if (type.equalsIgnoreCase("pub")) glyph.id = 31;
        else if (type.equalsIgnoreCase("quest")) glyph.id = 32;
        else if (type.equalsIgnoreCase("scimitar-shop")) glyph.id = 33;
        else if (type.equalsIgnoreCase("shield-shop")) glyph.id = 34;
        else if (type.equalsIgnoreCase("silk-trader")) glyph.id = 35;
        else if (type.equalsIgnoreCase("skirt-armour-shop")) glyph.id = 36;
        else if (type.equalsIgnoreCase("spinning-wheel")) glyph.id = 37;
        else if (type.equalsIgnoreCase("staff-shop")) glyph.id = 38;
        else if (type.equalsIgnoreCase("sword-shop")) glyph.id = 39;
        else if (type.equalsIgnoreCase("tannery")) glyph.id = 40;
        else if (type.equalsIgnoreCase("fishing-point")) glyph.id = 41;
        else glyph.id = -1;

        glyph.x = entry.getInt("x");
        glyph.y = entry.getInt("y");

        mapGlyphs.add(glyph);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Load fonts
    fonts = new Font[20];
    fontsBold = new Font[20];
    try {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      InputStream is = Launcher.getResourceAsStream("/assets/arial.ttf");
      InputStream is2 = Launcher.getResourceAsStream("/assets/Arial Bold.ttf");
      Font font = Font.createFont(Font.TRUETYPE_FONT, is);
      Font boldFont = Font.createFont(Font.TRUETYPE_FONT, is2);
      ge.registerFont(font);
      for (int i = 0; i < fonts.length; i++) {
        fonts[i] = font.deriveFont(Font.PLAIN, i + 2);
        fontsBold[i] = boldFont.deriveFont(Font.BOLD, i + 2);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Initialize resources
    try {
      planes = new BufferedImage[4];
      planes[0] = ImageIO.read(Launcher.getResource("/assets/map/plane-0.png"));
      planes[1] = ImageIO.read(Launcher.getResource("/assets/map/plane-1.png"));
      planes[2] = ImageIO.read(Launcher.getResource("/assets/map/plane-2.png"));
      planes[3] = ImageIO.read(Launcher.getResource("/assets/map/plane-3.png"));
      pointImage = ImageIO.read(Launcher.getResource("/assets/map/point.png"));
      waypointImage = ImageIO.read(Launcher.getResource("/assets/map/waypoint.png"));
      legends = new BufferedImage[42];
      legends[0] = ImageIO.read(Launcher.getResource("/assets/map/legend/rare-trees.png"));
      legends[1] = ImageIO.read(Launcher.getResource("/assets/map/legend/dungeon.png"));
      legends[2] = ImageIO.read(Launcher.getResource("/assets/map/legend/altar.png"));
      legends[3] = ImageIO.read(Launcher.getResource("/assets/map/legend/amulet-shop.png"));
      legends[4] = ImageIO.read(Launcher.getResource("/assets/map/legend/anvil.png"));
      legends[5] = ImageIO.read(Launcher.getResource("/assets/map/legend/apothecary.png"));
      legends[6] = ImageIO.read(Launcher.getResource("/assets/map/legend/archery-shop.png"));
      legends[7] = ImageIO.read(Launcher.getResource("/assets/map/legend/armour-conversion.png"));
      legends[8] = ImageIO.read(Launcher.getResource("/assets/map/legend/axe-shop.png"));
      legends[9] = ImageIO.read(Launcher.getResource("/assets/map/legend/bank.png"));
      legends[10] = ImageIO.read(Launcher.getResource("/assets/map/legend/bed.png"));
      legends[11] = ImageIO.read(Launcher.getResource("/assets/map/legend/body-armour-shop.png"));
      legends[12] = ImageIO.read(Launcher.getResource("/assets/map/legend/certificate-trader.png"));
      legends[13] = ImageIO.read(Launcher.getResource("/assets/map/legend/clothes-shop.png"));
      legends[14] = ImageIO.read(Launcher.getResource("/assets/map/legend/combat-practice.png"));
      legends[15] = ImageIO.read(Launcher.getResource("/assets/map/legend/cookery-shop.png"));
      legends[16] = ImageIO.read(Launcher.getResource("/assets/map/legend/crafting-shop.png"));
      legends[17] = ImageIO.read(Launcher.getResource("/assets/map/legend/fishing-shop.png"));
      legends[18] = ImageIO.read(Launcher.getResource("/assets/map/legend/food-shop.png"));
      legends[19] = ImageIO.read(Launcher.getResource("/assets/map/legend/furnace.png"));
      legends[20] = ImageIO.read(Launcher.getResource("/assets/map/legend/gem-shop.png"));
      legends[21] = ImageIO.read(Launcher.getResource("/assets/map/legend/general-shop.png"));
      legends[22] = ImageIO.read(Launcher.getResource("/assets/map/legend/helmet-shop.png"));
      legends[23] = ImageIO.read(Launcher.getResource("/assets/map/legend/herblaw-shop.png"));
      legends[24] = ImageIO.read(Launcher.getResource("/assets/map/legend/jewellery-shop.png"));
      legends[25] = ImageIO.read(Launcher.getResource("/assets/map/legend/kebab-shop.png"));
      legends[26] = ImageIO.read(Launcher.getResource("/assets/map/legend/leg-armour-shop.png"));
      legends[27] = ImageIO.read(Launcher.getResource("/assets/map/legend/mace-shop.png"));
      legends[28] = ImageIO.read(Launcher.getResource("/assets/map/legend/magic-shop.png"));
      legends[29] = ImageIO.read(Launcher.getResource("/assets/map/legend/mining-site.png"));
      legends[30] = ImageIO.read(Launcher.getResource("/assets/map/legend/pickable-lock.png"));
      legends[31] = ImageIO.read(Launcher.getResource("/assets/map/legend/pub.png"));
      legends[32] = ImageIO.read(Launcher.getResource("/assets/map/legend/quest.png"));
      legends[33] = ImageIO.read(Launcher.getResource("/assets/map/legend/scimitar-shop.png"));
      legends[34] = ImageIO.read(Launcher.getResource("/assets/map/legend/shield-shop.png"));
      legends[35] = ImageIO.read(Launcher.getResource("/assets/map/legend/silk-trader.png"));
      legends[36] = ImageIO.read(Launcher.getResource("/assets/map/legend/skirt-armour-shop.png"));
      legends[37] = ImageIO.read(Launcher.getResource("/assets/map/legend/spinning-wheel.png"));
      legends[38] = ImageIO.read(Launcher.getResource("/assets/map/legend/staff-shop.png"));
      legends[39] = ImageIO.read(Launcher.getResource("/assets/map/legend/sword-shop.png"));
      legends[40] = ImageIO.read(Launcher.getResource("/assets/map/legend/tannery.png"));
      legends[41] = ImageIO.read(Launcher.getResource("/assets/map/legend/fishing-point.png"));
      directions = new BufferedImage[8];
      directions[0] = ImageIO.read(Launcher.getResource("/assets/map/W.png"));
      directions[1] = ImageIO.read(Launcher.getResource("/assets/map/SW.png"));
      directions[2] = ImageIO.read(Launcher.getResource("/assets/map/S.png"));
      directions[3] = ImageIO.read(Launcher.getResource("/assets/map/SE.png"));
      directions[4] = ImageIO.read(Launcher.getResource("/assets/map/E.png"));
      directions[5] = ImageIO.read(Launcher.getResource("/assets/map/NE.png"));
      directions[6] = ImageIO.read(Launcher.getResource("/assets/map/N.png"));
      directions[7] = ImageIO.read(Launcher.getResource("/assets/map/NW.png"));
    } catch (Exception e) {
    }
  }

  public static void initScenery() {
    mapSceneries.clear();

    // Load Scenery
    try {
      DataInputStream in =
          new DataInputStream(Launcher.getResource("/assets/map/scenery.bin").openStream());
      int count = in.readInt();
      for (int i = 0; i < count; i++) {
        MapScenery scenery = new MapScenery();
        scenery.x = in.readShort();
        scenery.y = in.readShort();
        scenery.id = in.readShort();

        // These scenery objects will show up in search
        switch (scenery.id) {
            // Mining
          case 100:
          case 101:
            scenery.searchName = "Copper Rock";
            break;
          case 102:
          case 103:
            scenery.searchName = "Iron Rock";
            break;
          case 104: // 1030
          case 105:
            scenery.searchName = "Tin Rock";
            break;
          case 106:
          case 107:
            scenery.searchName = "Mithril Rock";
            break;
          case 108:
          case 109:
            scenery.searchName = "Adamantite Rock";
            break;
          case 110:
          case 111:
            scenery.searchName = "Coal Rock";
            break;
          case 112:
          case 113:
            scenery.searchName = "Gold Rock";
            break;
          case 114:
          case 115:
            scenery.searchName = "Clay Rock";
            break;
          case 176:
            scenery.searchName = "Blurite Rock";
            break;
          case 195:
          case 196:
            scenery.searchName = "Silver Rock";
            break;
          case 210:
          case 211:
            scenery.searchName = "Runite Rock";
            break;
          case 315:
            scenery.searchName = "Perfect Gold Rock";
            break;
          case 555:
            scenery.searchName = "Mossy Rocks";
            break;

            // Woodcutting
          case 310:
            scenery.searchName = "Magic Tree";
            break;

            // Fishing
          case 192:
            scenery.searchName = "Lure/Bait Fish";
            break;
          case 193:
            scenery.searchName = "Net/Bait Fish";
            break;
          case 194:
            scenery.searchName = "Harpoon/Cage Fish";
            break;
          case 261:
            scenery.searchName = "Net/Harpoon Fish";
            break;
          case 271:
            scenery.searchName = "Bait Fish (Lava Eel)";
            break;
          case 351:
            scenery.searchName = "Bait Fish (Sardine)";
            break;
          case 352:
            scenery.searchName = "Bait Fish (Giant Carp)";
            break;
          case 376:
            scenery.searchName = "Cage/Harpoon Fish";
            break;
          case 493:
            scenery.searchName = "Net Fish (tutorial)";
            break;

            // Use object name for the remaining object ids
          default:
            scenery.searchName = JGameData.objectNames[scenery.id];
            break;
        }

        mapSceneries.add(scenery);
      }
      in.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Point getWaypointPosition() {
    return waypointPosition;
  }

  public static float getWaypointAngle() {
    return waypointAngle;
  }

  public static int getWaypointFloor() {
    return waypointFloor;
  }

  public void showWorldMapWindow() {
    if (!frame.isVisible()) {
      searchText = "";
      updateMapRender();
    }
    frame.setVisible(true);
  }

  private static void setZoom(float val) {
    float prevZoom = zoom;
    zoom = val;
    if (zoom < 1.0f) zoom = 1.0f;
    else if (zoom > 8.0f) zoom = 8.0f;

    if ((!followPlayer || playerPlane == -1) && prevZoom < zoom) {
      float newX = (planes[0].getWidth(null)) - prevMousePointMap.x * 3;
      float newY = (prevMousePointMap.y - planeIndex * 944) * 3;
      float diffX = newX - cameraCurrentPosition.x;
      float diffY = newY - cameraCurrentPosition.y;

      cameraCurrentPosition.x += diffX / zoom;
      cameraCurrentPosition.y += diffY / zoom;

      int mapWidth = planes[planeIndex].getWidth(null);
      int mapHeight = planes[planeIndex].getHeight(null);
      if (cameraCurrentPosition.x < 0) cameraCurrentPosition.x = 0;
      if (cameraCurrentPosition.y < 0) cameraCurrentPosition.y = 0;
      if (cameraCurrentPosition.x > mapWidth) cameraCurrentPosition.x = mapWidth;
      if (cameraCurrentPosition.y > mapHeight) cameraCurrentPosition.y = mapHeight;
    }
  }

  private static void setFloor(int floor) {
    planeIndex = floor;
    if (planeIndex < 0) planeIndex = 3;
    if (planeIndex > 3) planeIndex = 0;
    updateMapFloorRender(planeIndex, false);
  }

  private static int getZoomInt(int val) {
    return (int) (val * zoom);
  }

  private static int getInvZoomInt(int val) {
    return (int) (val / zoom);
  }

  private static void updateCameraView() {
    cameraPosition.x = getZoomInt((int) cameraCurrentPosition.x) - mapView.getWidth() / 2;
    cameraPosition.y = getZoomInt((int) cameraCurrentPosition.y) - mapView.getHeight() / 2;
  }

  public static void UpdateView() {
    if (cameraLerpPosition == null) return;

    if (cameraLerpElapsed < 5.0f) {
      cameraFloatPosX = Util.lerp(cameraFloatPosX, cameraLerpPosition.x, cameraLerpElapsed / 5.0f);
      cameraFloatPosY = Util.lerp(cameraFloatPosY, cameraLerpPosition.y, cameraLerpElapsed / 5.0f);
      cameraCurrentPosition.x = (int) cameraFloatPosX;
      cameraCurrentPosition.y = (int) cameraFloatPosY;

      float diffX = Math.abs(cameraFloatPosX - cameraLerpPosition.x);
      float diffY = Math.abs(cameraFloatPosY - cameraLerpPosition.y);
      if (diffX < 0.5f && diffY < 0.5f) {
        cameraCurrentPosition.x = cameraLerpPosition.x;
        cameraFloatPosX = cameraCurrentPosition.x;
        cameraCurrentPosition.y = cameraLerpPosition.y;
        cameraFloatPosY = cameraCurrentPosition.y;
        cameraLerpPosition = null;
      }
    } else {
      cameraCurrentPosition.x = cameraLerpPosition.x;
      cameraFloatPosX = cameraCurrentPosition.x;
      cameraCurrentPosition.y = cameraLerpPosition.y;
      cameraFloatPosY = cameraCurrentPosition.y;
      cameraLerpPosition = null;
    }

    cameraLerpElapsed += Renderer.delta_time;

    mapView.repaint();
  }

  public static void Update() {
    Point prevPosition = new Point(playerPosition.x, playerPosition.y);
    CameraPoint prevCameraPosition =
        new CameraPoint(cameraCurrentPosition.x, cameraCurrentPosition.y);
    int prevPlane = playerPlane;
    boolean dirty = false;

    playerPosition = new Point(Client.worldX, Client.worldY);
    playerPlane = Client.planeIndex;

    // Handle waypoint removal when destination is reached
    if (waypointPosition != null) {
      int distance =
          (int)
              Point.distance(
                  playerPosition.x, playerPosition.y, waypointPosition.x, waypointPosition.y);
      if (distance <= 2) {
        waypointPosition = null;
        dirty = true;
      }

      if (waypointPosition != null) {
        Rectangle p = convertWorldCoordsToMapRaw(playerPosition.x, playerPosition.y);
        Point p1 = new Point(p.x, p.y);
        p = convertWorldCoordsToMapRaw(waypointPosition.x, waypointPosition.y);
        Point p2 = new Point(p.x, p.y);
        waypointAngle = 360.0f - Util.getAngle(p1, p2) + 180.0f;
        waypointFloor = p.width;
      }
    }

    if (playerPlane != -1 && followPlayer) {
      setFloor(playerPlane);
      int y = playerPosition.y;
      if (y <= 1007) {
      } else if (y <= 1950) {
        y -= 943;
      } else if (y <= 2893) {
        y -= 1886;
      } else {
        y -= 2829;
      }
      int tileSize = getZoomInt(3);
      cameraCurrentPosition.x = (planes[planeIndex].getWidth(null) - (playerPosition.x * 3)) - 1;
      cameraCurrentPosition.y = ((y - planeIndex) * 3) + 1;
    }

    if (!frame.isVisible()) return;

    // Repaint if data changed
    if (dirty
        || prevPosition.x != playerPosition.x
        || prevPosition.y != playerPosition.y
        || prevPlane != playerPlane
        || prevCameraPosition.x != cameraCurrentPosition.x
        || prevCameraPosition.y != cameraCurrentPosition.y) mapView.repaint();
  }

  public static void Reset() {
    int prevPlayerPlane = playerPlane;
    playerPlane = -1;
    if (playerPlane != prevPlayerPlane) mapView.repaint();
  }

  public static Rectangle convertWorldCoordsToMapRaw(int x, int y, int tileSize) {
    int plane = y / 945;
    int wilderness = 0;
    if (plane == 0) {
      if (x <= 336 && y <= 429) wilderness = 1;
    }
    x += 1;
    y -= plane * 944;
    return new Rectangle(
        (planes[planeIndex].getWidth(null) / 3 - x) * tileSize, y * tileSize, plane, wilderness);
  }

  public static Rectangle convertWorldCoordsToMapRaw(int x, int y) {
    return convertWorldCoordsToMapRaw(x, y, 3);
  }

  public static Rectangle convertWorldCoordsToMap(int x, int y) {
    int tileSize = getZoomInt(3);
    x = x;
    y = y;
    Rectangle p = convertWorldCoordsToMapRaw(x, y, tileSize);
    return new Rectangle(-cameraPosition.x + p.x, -cameraPosition.y + p.y, p.width, p.height);
  }

  public static void drawMapPoint(Graphics2D g, int x, int y) {
    g.drawLine(x + 2, y - 1, x + 2, y + 1);
    g.drawLine(x + 1, y, x + 3, y);
    // Centered
    // g.drawLine(x + 1, y, x + 1, y + 2);
    // g.drawLine(x, y + 1, x + 2, y + 1);
  }

  public static void drawMapLabel(Graphics2D g, int x, int y, MapLabel label) {
    if (label.bold) g.setFont(fontsBold[label.size]);
    else g.setFont(fonts[label.size]);

    int width = 0;
    for (String line : label.text.split("\n")) {
      int lineWidth = g.getFontMetrics().stringWidth(line);
      if (label.centered) {
        if (width < lineWidth) width = lineWidth;
      }
    }

    for (String line : label.text.split("\n")) {
      int offsetX = 0;
      int offsetY = 0;
      if (label.centered) {
        offsetX = g.getFontMetrics().stringWidth(line) / 2;
      }
      offsetY = g.getFontMetrics().getHeight() / 2;
      int height = g.getFontMetrics().getHeight();

      // Highlight searched names
      Color labelColor = label.color;
      if (searchText.length() > 0
          && label.text.replaceAll("\n", " ").toLowerCase().contains(searchText.toLowerCase()))
        labelColor = Renderer.color_item_highlighted;

      Renderer.drawShadowText(g, line, x - offsetX + width / 2, y + offsetY, labelColor, false);
      y += height;
    }
  }

  public static class MapRenderer extends JComponent {
    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);

      updateCameraView();

      int canvasWidth = getWidth();
      int canvasHeight = getHeight();
      int mapWidth = planes[planeIndex].getWidth(null);
      int mapHeight = planes[planeIndex].getHeight(null);
      int tileSize = getZoomInt(3);

      floorDownBounds.x = canvasWidth - floorDownBounds.width - BORDER_SIZE;
      floorDownBounds.y = canvasHeight - floorDownBounds.height - BORDER_SIZE;
      floorUpBounds.x = floorDownBounds.x;
      floorUpBounds.y = floorDownBounds.y - 64;
      floorTextBounds.x = floorDownBounds.x + (floorDownBounds.width / 2);
      floorTextBounds.y = floorDownBounds.y - 24;

      zoomDownBounds.x = floorUpBounds.x;
      zoomDownBounds.y = floorUpBounds.y - zoomDownBounds.height - BORDER_SIZE;
      zoomTextBounds.x = zoomDownBounds.x + (zoomDownBounds.width / 2);
      zoomTextBounds.y = zoomDownBounds.y - 24;
      zoomUpBounds.x = zoomDownBounds.x;
      zoomUpBounds.y = zoomDownBounds.y - 64;

      posTextBounds.x = BORDER_SIZE;
      posTextBounds.y = BORDER_SIZE * 2;
      chunkGridBounds.x = floorDownBounds.x - BORDER_SIZE - chunkGridBounds.width;
      chunkGridBounds.y = floorDownBounds.y;
      showLabelsBounds.x = chunkGridBounds.x;
      showLabelsBounds.y = chunkGridBounds.y - BORDER_SIZE - showLabelsBounds.height;
      showSceneryBounds.x = showLabelsBounds.x;
      showSceneryBounds.y = showLabelsBounds.y - BORDER_SIZE - showSceneryBounds.height;
      showIconsBounds.x = showSceneryBounds.x;
      showIconsBounds.y = showSceneryBounds.y - BORDER_SIZE - showIconsBounds.height;
      showOtherFloorsBounds.x = showIconsBounds.x;
      showOtherFloorsBounds.y = showIconsBounds.y - BORDER_SIZE - showOtherFloorsBounds.height;
      followPlayerBounds.x = chunkGridBounds.x - BORDER_SIZE - chunkGridBounds.width;
      followPlayerBounds.y = chunkGridBounds.y;
      searchBounds.x = BORDER_SIZE;
      searchBounds.y = canvasHeight - BORDER_SIZE - searchBounds.height;
      searchRefreshBounds.x = searchBounds.x + searchBounds.width + BORDER_SIZE;
      searchRefreshBounds.y = searchBounds.y;

      // Get world coords
      Point scaledPoint =
          new Point(getInvZoomInt(prevMousePoint.x), getInvZoomInt(prevMousePoint.y));
      Point worldCoords =
          new Point(
              getInvZoomInt(cameraPosition.x) + scaledPoint.x,
              getInvZoomInt(cameraPosition.y) + scaledPoint.y);
      prevMousePointMap.x = ((planes[planeIndex].getWidth(null) - worldCoords.x - 4) / 3) + 1;
      prevMousePointMap.y = (worldCoords.y / 3) + (planeIndex * 944);

      // Initialize
      Shape rootShape = new Rectangle2D.Float(0, 0, getWidth(), getHeight());
      Graphics2D g = (Graphics2D) graphics;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (showOtherFloors || planeIndex == 0) g.setColor(color_water);
      else g.setColor(Color.black);

      g.fill(rootShape);

      if (showOtherFloors) {
        if (planeIndex == 3) {
          setAlpha(g, 0.8f);
          g.setColor(Color.black);
          g.fill(rootShape);
          setAlpha(g, 1.0f);
        } else {
          for (int i = 0; i < planeIndex; i++) {
            setAlpha(g, 0.5f);
            g.setColor(Color.black);
            g.fill(rootShape);
            setAlpha(g, 1.0f);
          }
        }
      }

      synchronized (renderLock) {
        int bufferWidth = getZoomInt(mapImageBuffer[planeIndex].getWidth());
        int bufferHeight = getZoomInt(mapImageBuffer[planeIndex].getHeight());
        g.drawImage(
            mapImageBuffer[planeIndex],
            -cameraPosition.x,
            -cameraPosition.y,
            bufferWidth,
            bufferHeight,
            null);
      }

      Rectangle hoverTilePoint = convertWorldCoordsToMap(prevMousePointMap.x, prevMousePointMap.y);
      g.setColor(Renderer.color_low);
      setAlpha(g, 0.5f);
      g.fillRect(hoverTilePoint.x, hoverTilePoint.y, tileSize, tileSize);
      setAlpha(g, 1.0f);

      // Render waypoint
      if (waypointPosition != null) {
        Rectangle p = convertWorldCoordsToMap(waypointPosition.x, waypointPosition.y);
        setAlpha(g, p.width == planeIndex ? 1.0f : 0.25f);
        g.setFont(fontsBold[18]);
        g.drawImage(
            waypointImage,
            p.x - pointImage.getWidth(null) / 2 + (tileSize / 2),
            p.y - pointImage.getHeight(null) / 2 + (tileSize / 2),
            null);
        Renderer.drawShadowText(
            g, "Your destination", p.x + tileSize / 2, p.y - 28, Renderer.color_low, true);
        setAlpha(g, 1.0f);
      }

      // Render player
      if (playerPlane != -1) {
        setAlpha(g, playerPlane == planeIndex ? 1.0f : 0.25f);
        Rectangle p = convertWorldCoordsToMap(playerPosition.x, playerPosition.y);
        g.setFont(fontsBold[18]);
        g.drawImage(
            pointImage,
            p.x - pointImage.getWidth(null) / 2 + (tileSize / 2),
            p.y - pointImage.getHeight(null) / 2 + (tileSize / 2),
            null);
        Renderer.drawShadowText(
            g, "You are here", p.x + tileSize / 2, p.y - 28, Renderer.color_item_highlighted, true);
        setAlpha(g, 1.0f);
      }

      int renderY = posTextBounds.y;
      g.setFont(Renderer.font_main);
      if (developmentMode) {
        Renderer.drawShadowText(
            g, "DEVELOPMENT MODE", posTextBounds.x, renderY, Renderer.color_text, false);
        renderY += 16;
      }
      Renderer.drawShadowText(
          g,
          Integer.toString(prevMousePointMap.x) + ", " + Integer.toString(prevMousePointMap.y),
          posTextBounds.x,
          renderY,
          Renderer.color_fatigue,
          false);

      drawButton(g, "^", floorUpBounds);
      drawButton(g, "v", floorDownBounds);
      drawButton(g, "-", zoomDownBounds);
      drawButton(g, "+", zoomUpBounds);
      // Renderer.drawShadowText(g, "Floor", floorTextBounds.x, floorTextBounds.y - 54,
      // Renderer.color_text, true);
      g.setFont(Renderer.font_big);

      final String[] floorNames = {"G", "1", "2", "B"};

      Renderer.drawShadowText(
          g,
          floorNames[planeIndex],
          floorTextBounds.x,
          floorTextBounds.y,
          Renderer.color_text,
          true);
      Renderer.drawShadowText(
          g,
          Integer.toString((int) zoom) + "x",
          zoomTextBounds.x,
          zoomTextBounds.y,
          Renderer.color_text,
          true);

      if (renderChunkGrid) {
        drawButton(g, "Disable Chunk Grid", chunkGridBounds);
      } else {
        drawButton(g, "Enable Chunk Grid", chunkGridBounds);
      }

      if (showLabels) {
        drawButton(g, "Hide Labels", showLabelsBounds);
      } else {
        drawButton(g, "Show Labels", showLabelsBounds);
      }

      if (showScenery) {
        drawButton(g, "Hide Scenery", showSceneryBounds);
      } else {
        drawButton(g, "Show Scenery", showSceneryBounds);
      }

      if (showIcons) {
        drawButton(g, "Hide Icons", showIconsBounds);
      } else {
        drawButton(g, "Show Icons", showIconsBounds);
      }
      if (planeIndex != 0) {
        if (showOtherFloors) {
          drawButton(g, "Hide Other Floors", showOtherFloorsBounds);
        } else {
          drawButton(g, "Show Other Floors", showOtherFloorsBounds);
        }
      }

      if (followPlayer) {
        drawButton(g, "Free Movement", followPlayerBounds);
      } else {
        drawButton(g, "Follow Player", followPlayerBounds);
      }

      drawSearch(g, searchText, searchBounds);
      if (searchText.length() > 0) drawButton(g, "Refresh", searchRefreshBounds);

      if (showLegend) {
        // First size legend and draw background
        int startX = legendBounds.x - 150 + BORDER_SIZE;
        int x = startX;
        int y = legendBounds.y + BORDER_SIZE;
        int offsetY = 0;
        int maxY = 16 * 21;
        for (int i = 0; i < legends.length; i++) {
          offsetY += 16;
          if (offsetY >= maxY) {
            x += 150;
            offsetY = 0;
          }
        }
        legendBounds.width = x - startX;
        legendBounds.height = maxY + (BORDER_SIZE * 2);
        legendBounds.x = canvasWidth - legendBounds.width - BORDER_SIZE;
        legendBounds.y = BORDER_SIZE;
        drawButton(g, showLegend ? "" : "Legend", legendBounds);

        // Actually draw legend icons and text
        x = legendBounds.x + BORDER_SIZE;
        y = legendBounds.y + BORDER_SIZE;
        offsetY = 0;
        for (int i = 0; i < legends.length; i++) {
          g.drawImage(
              legends[i],
              x + (7 - legends[i].getWidth(null) / 2),
              y + offsetY + (7 - legends[i].getHeight(null) / 2),
              null);
          Renderer.drawShadowText(
              g, legendText[i], x + 18, y + offsetY + 12, Renderer.color_text, false);
          offsetY += 16;
          if (offsetY >= maxY) {
            x += 150;
            offsetY = 0;
          }
        }
      } else {
        legendBounds.height = 24;
        legendBounds.width = 150;
        legendBounds.x = canvasWidth - legendBounds.width - BORDER_SIZE;
        legendBounds.y = BORDER_SIZE;
        drawButton(g, showLegend ? "" : "Legend", legendBounds);
      }
    }
  }

  public static class SearchResult {
    public static final int SEARCH_NONE = 0;
    public static final int SEARCH_LABEL = 1;
    public static final int SEARCH_GLYPH = 2;
    public static final int SEARCH_SCENERY = 3;

    public String getSearchName() {
      return "";
    }

    public String getSearchNameExtra() {
      return "";
    }

    public int getSearchType() {
      return SEARCH_NONE;
    }

    public int getPlayerDistance() {
      return 0;
    }

    public boolean isSearchable() {
      return false;
    }
  }

  public static class SearchResultComparator implements Comparator<SearchResult> {
    @Override
    public int compare(SearchResult sc, SearchResult scc) {
      int scDist = sc.getPlayerDistance();
      int sccDist = scc.getPlayerDistance();
      int diff = scDist - sccDist;
      return diff < 0 ? -1 : diff > 0 ? 1 : 0;
    }
  }

  public static void disposeJFrame() {
    frame.dispose();
  }

  public static class MapLink {
    Rectangle loc;
    int floor;
    int type;

    public static final int LINK_NONE = 1;
    public static final int LINK_NEXT_FLOOR = 1;
    public static final int LINK_PREV_FLOOR = 2;
    public static final int LINK_BOTH_FLOOR = 3;

    public MapLink() {
      loc = new Rectangle(0, 0, 16, 16);
      type = LINK_NONE;
    }
  }

  public static class MapGlyph extends SearchResult {
    int x;
    int y;
    int id;

    @Override
    public String getSearchName() {
      return legendText[id];
    }

    @Override
    public int getSearchType() {
      return SearchResult.SEARCH_GLYPH;
    }

    @Override
    public int getPlayerDistance() {
      int playerX = playerPosition.x;
      int playerY = playerPosition.y;
      Rectangle p = convertWorldCoordsToMap(playerPosition.x, playerPosition.y);
      int distance = (int) Point.distance(p.x, p.y, -cameraPosition.x + x, -cameraPosition.y + y);
      return distance;
    }

    @Override
    public boolean isSearchable() {
      return true;
    }
  }

  public static class MapScenery extends SearchResult {
    int x;
    int y;
    int id;
    String searchName;

    @Override
    public String getSearchName() {
      return searchName;
    }

    public String getSearchNameExtra() {
      return "id: " + Integer.toString(id);
    }

    @Override
    public boolean isSearchable() {
      return (searchName != null);
    }

    @Override
    public int getSearchType() {
      return SEARCH_SCENERY;
    }

    @Override
    public int getPlayerDistance() {
      Rectangle p = convertWorldCoordsToMap(playerPosition.x, playerPosition.y);
      Rectangle p2 = convertWorldCoordsToMap(x, y);
      int distance = (int) Point.distance(p.x, p.y, p2.x, p2.y);
      return distance;
    }
  }

  public static class MapLabel extends SearchResult {
    String text;
    int x;
    int y;
    int size;
    boolean centered;
    boolean bold;
    Color color;

    @Override
    public String getSearchName() {
      return text.replaceAll("\n", " ");
    }

    @Override
    public int getSearchType() {
      return SEARCH_LABEL;
    }

    @Override
    public int getPlayerDistance() {
      int playerX = playerPosition.x;
      int playerY = playerPosition.y;
      Rectangle p = convertWorldCoordsToMap(playerPosition.x, playerPosition.y);
      int distance = (int) Point.distance(p.x, p.y, -cameraPosition.x + x, -cameraPosition.y + y);
      return distance;
    }

    @Override
    public boolean isSearchable() {
      return true;
    }
  }
}
