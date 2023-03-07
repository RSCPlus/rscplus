package Client;

import Game.Client;
import Game.Game;
import Game.KeyboardHandler;
import Game.MouseHandler.BufferedMouseClick;
import Game.Renderer;
import Game.ReplayQueue;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;

/**
 * This class is responsible for rendering all output from the applet onto the screen, which it
 * receives via a {@link BufferedImage} from the {@link Renderer#present} method. All window
 * interactions are then forwarded to the applet contained within the {@link Game} class.
 */
public class ScaledWindow extends JFrame
    implements WindowListener,
        FocusListener,
        ComponentListener,
        MouseListener,
        MouseMotionListener,
        MouseWheelListener,
        KeyListener {

  // Singleton
  private static ScaledWindow instance = null;
  private static boolean initialRender = true;
  private static boolean isMacOS = false;
  private static boolean shouldRealign = false;
  private int frameWidth = 0;
  private int frameHeight = 0;
  private ScaledViewport scaledViewport;
  private int viewportWidth = 0;
  private int viewportHeight = 0;
  private BufferedImage unscaledBackground = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
  private int previousUnscaledWidth;
  private int previousUnscaledHeight;

  // Mouse click buffer used to prevent click de-syncs with custom overlays at low FPS
  private static final Queue<BufferedMouseClick> inputBuffer = new LinkedList<>();

  /** Private constructor to ensure singleton nature */
  private ScaledWindow() {
    try {
      // Set System L&F as the default
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException e) {
      Logger.Error("Unable to set L&F: Unsupported look and feel");
    } catch (ClassNotFoundException e) {
      Logger.Error("Unable to set L&F: Class not found");
    } catch (InstantiationException e) {
      Logger.Error("Unable to set L&F: Class object cannot be instantiated");
    } catch (IllegalAccessException e) {
      Logger.Error("Unable to set L&F: Illegal access exception");
    }

    Logger.Info("Creating scaled window");

    /* Initialize the contents of the frame. */
    try {
      SwingUtilities.invokeAndWait(
          new Runnable() {
            @Override
            public void run() {
              runInit();
            }
          });
    } catch (InvocationTargetException e) {
      Logger.Error("There was a thread-related error while setting up the scaled window!");
      e.printStackTrace();
    } catch (InterruptedException e) {
      Logger.Error(
          "There was a thread-related error while setting up the scaled window! The window may not be initialized properly!");
      e.printStackTrace();
    }
  }

  private void runInit() {
    // Set window properties
    setBackground(Color.black);
    setFocusTraversalKeysEnabled(false);

    // Add window listeners
    addWindowListener(this);
    addComponentListener(this);
    addFocusListener(this);
    addKeyListener(this);

    // Allows drag-n-dropping a replay onto the game window
    setDropTarget(ReplayQueue.dropReplays);

    // Enable macOS fullscreen button, if possible
    isMacOS = Util.isMacOS();

    if (isMacOS) {
      try {
        Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
        Class params[] = new Class[] {Window.class, Boolean.TYPE};
        Method method = util.getMethod("setWindowCanFullScreen", params);
        method.invoke(util, this, true);
      } catch (Exception ignored) {
      }
    }

    // Set minimum size to applet size
    setMinimumSize(new Dimension(512, 346));

    URL iconURL = Launcher.getResource("/assets/icon.png");
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      setIconImage(icon.getImage());
    }

    // Initialize scaled view
    scaledViewport = new ScaledViewport();

    scaledViewport.addMouseListener(this);
    scaledViewport.addMouseMotionListener(this);
    scaledViewport.addMouseWheelListener(this);

    scaledViewport.setSize(getSize());
    scaledViewport.setBackground(Color.black);
    scaledViewport.revalidate();
    scaledViewport.repaint();
    scaledViewport.setVisible(true);

    add(scaledViewport);

    pack();
    revalidate();
    repaint();
  }

  /**
   * Keep track of frame dimensions internally to avoid possible thread-safety issues when needing
   * to invoke a method that uses the frame size, immediately after setting it.
   *
   * <p>NOTE: Must <i>always</i> call setMinimumSize before invoking this method
   */
  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);
    frameWidth = width;
    frameHeight = height;
  }

  /** Sets a flag to align the window after resizing the applet */
  public void setWindowRealignmentIntent(boolean flag) {
    shouldRealign = flag;
  }

  /**
   * Centers the window or pins it to the top of the screen, if the custom size exactly matches the
   * available space.
   */
  private void alignWindow() {
    Rectangle currentScreenBounds =
        getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();

    int x = ((currentScreenBounds.width - frameWidth) / 2) + currentScreenBounds.x;
    int y = ((currentScreenBounds.height - frameHeight) / 2) + currentScreenBounds.y;

    if (Settings.CUSTOM_CLIENT_SIZE.get(Settings.currentProfile)) {
      int currentWidthSetting = Settings.CUSTOM_CLIENT_SIZE_X.get(Settings.currentProfile);
      int currentHeightSetting = Settings.CUSTOM_CLIENT_SIZE_Y.get(Settings.currentProfile);

      Dimension maxWindowDimensions = getMaximumEffectiveWindowSize();

      int maxWindowWidth = maxWindowDimensions.width;
      int maxWindowHeight = maxWindowDimensions.height;

      if (currentWidthSetting == maxWindowWidth && currentHeightSetting == maxWindowHeight) {
        // pin window to top
        y = currentScreenBounds.y;
      }
    }

    // Set the window location
    setLocation(x, y);
  }

  /**
   * Used to determine the user's maximum effective window size, taking the window's insets into
   * consideration.
   */
  public Dimension getMaximumEffectiveWindowSize() {
    Dimension maximumWindowSize = getMaximumWindowSize();

    // Subtract
    int windowWidth = maximumWindowSize.width - getWindowWidthInsets();
    int windowHeight = maximumWindowSize.height - getWindowHeightInsets();

    if (Util.isModernWindowsOS()) {
      windowWidth += 16;
      windowHeight += 8;
    }

    return new Dimension(windowWidth, windowHeight);
  }

  /** Used to determine the user's maximum window size */
  public Dimension getMaximumWindowSize() {
    GraphicsConfiguration graphicsConfiguration =
        getGraphicsConfiguration().getDevice().getDefaultConfiguration();
    Rectangle screenBounds = graphicsConfiguration.getBounds();
    Insets screenInsets = getToolkit().getScreenInsets(graphicsConfiguration);

    // Subtract the operating system insets from the current display's max bounds
    int maxWidth = screenBounds.width - screenInsets.left - screenInsets.right;
    int maxHeight = screenBounds.height - screenInsets.top - screenInsets.bottom;

    return new Dimension(maxWidth, maxHeight);
  }

  /** Opens the window */
  public void launchScaledWindow() {
    setLocationRelativeTo(null);
    setVisible(true);
  }

  /**
   * Used to pass the viewport's {@link Graphics} object to the applet such that it will render to
   * it instead of itself.
   */
  public static Graphics hookLoadingGraphics() {
    return getInstance().getGraphics();
  }

  /**
   * Sets the {@link BufferedImage} that the window should display, which ultimately comes from
   * {@link Renderer#present}. The first frame render will load the user's custom window size
   * settings, if present.
   */
  public void setGameImage(BufferedImage gameImage) {
    if (gameImage == null) {
      return;
    }

    if (scaledViewport.isViewportImageLoaded()) {
      if (initialRender) {
        // Resize to the custom window size, if it is defined
        if (Settings.CUSTOM_CLIENT_SIZE.get(Settings.currentProfile)) {
          int customClientWidth = Settings.CUSTOM_CLIENT_SIZE_X.get(Settings.currentProfile);
          int customClientHeight = Settings.CUSTOM_CLIENT_SIZE_Y.get(Settings.currentProfile);

          int frameWidth = customClientWidth + getWindowWidthInsets();
          int frameHeight = customClientHeight + getWindowHeightInsets();

          setWindowRealignmentIntent(true);

          setMinimumSize(getMinimumWindowSizeForScalar());
          setSize(frameWidth, frameHeight);
        } else {
          // Otherwise, set the window size for the scalar (will be realigned in the method)
          resizeWindowToScalar();
        }

        // Update the window size spinner minimum values
        Launcher.getConfigWindow()
            .updateCustomClientSizeMinValues(getMinimumViewportSizeForScalar());

        initialRender = false;
      }

      viewportWidth = gameImage.getWidth();
      viewportHeight = gameImage.getHeight();
    }

    if (Renderer.renderingScalar == 1.0f) {
      // Unscaled client behavior
      int newUnscaledWidth = gameImage.getWidth();
      int newUnscaledHeight = gameImage.getHeight();

      if (previousUnscaledWidth != newUnscaledWidth
          || previousUnscaledHeight != newUnscaledHeight) {
        unscaledBackground =
            new BufferedImage(newUnscaledWidth, newUnscaledHeight, gameImage.getType());

        previousUnscaledWidth = newUnscaledWidth;
        previousUnscaledHeight = newUnscaledHeight;
      }

      // Draw onto a new BufferedImage to prevent flickering
      Graphics2D g2d = (Graphics2D) unscaledBackground.getGraphics();
      g2d.drawImage(gameImage, 0, 0, null);
      g2d.dispose();

      scaledViewport.setViewportImage(unscaledBackground);

      scaledViewport.repaint();
    } else {
      // Scaled client behavior
      scaledViewport.setViewportImage(gameImage);

      int scaledWidth = Math.round(viewportWidth * Renderer.renderingScalar);
      int scaledHeight = Math.round(viewportHeight * Renderer.renderingScalar);

      try {
        SwingUtilities.invokeAndWait(
            () -> scaledViewport.paintImmediately(0, 0, scaledWidth, scaledHeight));
      } catch (InterruptedException | InvocationTargetException ignored) {
        // no-op
      }
    }
  }

  public boolean isViewportLoaded() {
    return scaledViewport.isViewportImageLoaded();
  }

  public int getWindowWidthInsets() {
    return getInsets().left + getInsets().right;
  }

  public int getWindowHeightInsets() {
    return getInsets().top + getInsets().bottom;
  }

  /** Resizes the window size for the scalar */
  public void resizeWindowToScalar() {
    Dimension minimumWindowSizeForScalar = getMinimumWindowSizeForScalar();

    if (!getSize().equals(minimumWindowSizeForScalar)) {
      // Update the window size as necessary, which will in turn
      // invoke the componentResized listener on this JFrame
      setWindowRealignmentIntent(true);

      setMinimumSize(minimumWindowSizeForScalar);
      setSize(minimumWindowSizeForScalar);
    } else {
      // Resize the viewport if the actual window size didn't change, since
      // the componentResized listener won't get triggered in that case.
      // e.g. custom size set to 1024x692, then scale x2 turned on
      setMinimumSize(minimumWindowSizeForScalar);
      resizeApplet();
    }

    // Update the window size spinner minimum values
    Launcher.getConfigWindow().updateCustomClientSizeMinValues(getMinimumViewportSizeForScalar());
  }

  /** Determines the smallest window size for the scalar, including insets */
  private Dimension getMinimumWindowSizeForScalar() {
    Dimension minimumViewPortSizeForScalar = getMinimumViewportSizeForScalar();

    int frameWidth = minimumViewPortSizeForScalar.width + getWindowWidthInsets();
    int frameHeight = minimumViewPortSizeForScalar.height + getWindowHeightInsets();

    return new Dimension(frameWidth, frameHeight);
  }

  /** Sets the current window size based on the user's settings */
  public void updateCustomWindowSizeFromSettings() {
    Dimension scaledMinimumWindowSize = getMinimumViewportSizeForScalar();

    Settings.CUSTOM_CLIENT_SIZE_X.put(Settings.currentProfile, scaledMinimumWindowSize.width);
    Settings.CUSTOM_CLIENT_SIZE_Y.put(Settings.currentProfile, scaledMinimumWindowSize.height);

    Settings.save();

    // Update the custom client size width and height spinners
    Launcher.getConfigWindow().synchronizeGuiValues();
  }

  /** Determines the minimum window size for the applet based on the scalar */
  public Dimension getMinimumViewportSizeForScalar() {
    return new Dimension(
        Math.round(512 * Renderer.renderingScalar), Math.round(346 * Renderer.renderingScalar));
  }

  /** Resizes the applet contained within {@link Game} */
  private void resizeApplet() {
    if (Renderer.renderingScalar == 0.0f || !isViewportLoaded()) {
      return;
    }

    int newWidth = Math.round(scaledViewport.getWidth() / Renderer.renderingScalar);
    int newHeight = Math.round(scaledViewport.getHeight() / Renderer.renderingScalar);

    Applet mudclient = Game.getInstance().getApplet();

    if (mudclient != null) {
      mudclient.setSize(newWidth, newHeight);
      Renderer.resize(newWidth, newHeight);
    }

    if (shouldRealign) {
      setWindowRealignmentIntent(false);
      alignWindow();
    }
  }

  public void disposeJFrame() {
    dispose();
  }

  public Queue<BufferedMouseClick> getInputBuffer() {
    return inputBuffer;
  }

  /*
   * WindowListener methods - forward to Game.java
   */

  @Override
  public void windowClosed(WindowEvent e) {
    Game.getInstance()
        .dispatchEvent(new WindowEvent(Game.getInstance(), WindowEvent.WINDOW_CLOSED));
  }

  @Override
  public void windowClosing(WindowEvent e) {
    Game.getInstance()
        .dispatchEvent(new WindowEvent(Game.getInstance(), WindowEvent.WINDOW_CLOSING));
  }

  @Override
  public void windowOpened(WindowEvent e) {}

  @Override
  public void windowDeactivated(WindowEvent e) {}

  @Override
  public void windowActivated(WindowEvent e) {}

  @Override
  public void windowDeiconified(WindowEvent e) {}

  @Override
  public void windowIconified(WindowEvent e) {}

  /*
   * FocusListener methods - forward to Game.java
   */

  @Override
  public void focusGained(FocusEvent e) {}

  @Override
  public void focusLost(FocusEvent e) {
    if (Client.handler_keyboard == null || Renderer.renderingScalar == 0.0f) return;

    KeyboardHandler.keyUp = false;
    KeyboardHandler.keyDown = false;
    KeyboardHandler.keyLeft = false;
    KeyboardHandler.keyRight = false;
    KeyboardHandler.keyShift = false;
  }

  /*
   * ComponentListener methods
   */

  @Override
  public void componentResized(ComponentEvent e) {
    resizeApplet();
  }

  @Override
  public void componentMoved(ComponentEvent e) {}

  @Override
  public void componentShown(ComponentEvent e) {}

  @Override
  public void componentHidden(ComponentEvent e) {}

  /*
   * MouseListener, MouseMotionListener, and MouseWheelListener methods
   * - forward to Client.handler_mouse
   */

  @Override
  public void mouseClicked(MouseEvent e) {
    if (Client.handler_mouse == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_mouse.mouseClicked(mapMouseEvent(e));
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (Client.handler_mouse == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_mouse.mousePressed(mapMouseEvent(e));
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (Client.handler_mouse == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_mouse.mouseReleased(mapMouseEvent(e));
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    if (Client.handler_mouse == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_mouse.mouseEntered(mapMouseEvent(e));
  }

  @Override
  public void mouseExited(MouseEvent e) {
    if (Client.handler_mouse == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_mouse.mouseExited(mapMouseEvent(e));
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (Client.handler_mouse == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_mouse.mouseDragged(mapMouseEvent(e));
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    if (Client.handler_mouse == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_mouse.mouseMoved(mapMouseEvent(e));
  }

  private static MouseEvent mapMouseEvent(MouseEvent e) {
    Component mouseEventSource = (Component) e.getSource();
    int mouseEventId = e.getID();
    long mouseEventWhen = e.getWhen();
    int mouseEventModifiers = e.getModifiers();
    int mappedMouseEventX = Math.round(e.getX() / Renderer.renderingScalar);
    int mappedMouseEventY = Math.round(e.getY() / Renderer.renderingScalar);
    int mouseEventXOnScreen = e.getXOnScreen();
    int mouseEventYOnScreen = e.getYOnScreen();
    int mouseEventClickCount = e.getClickCount();
    boolean mouseEventPopupTrigger = e.isPopupTrigger();
    int mouseEventButton = e.getButton();

    return new MouseEvent(
        mouseEventSource,
        mouseEventId,
        mouseEventWhen,
        mouseEventModifiers,
        mappedMouseEventX,
        mappedMouseEventY,
        mouseEventXOnScreen,
        mouseEventYOnScreen,
        mouseEventClickCount,
        mouseEventPopupTrigger,
        mouseEventButton);
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (Client.handler_mouse == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_mouse.mouseWheelMoved(mapMouseWheelEvent(e));
  }

  private static MouseWheelEvent mapMouseWheelEvent(MouseWheelEvent e) {
    Component mouseWheelEventSource = (Component) e.getSource();
    int mouseWheelEventId = e.getID();
    long mouseWheelEventWhen = e.getWhen();
    int mouseWheelEventModifiers = e.getModifiers();
    int mappedMouseWheelEventX = Math.round(e.getX() / Renderer.renderingScalar);
    int mappedMouseWheelEventY = Math.round(e.getY() / Renderer.renderingScalar);
    int mouseWheelEventXOnScreen = e.getXOnScreen();
    int mouseWheelEventYOnScreen = e.getYOnScreen();
    int mouseWheelEventClickCount = e.getClickCount();
    boolean mouseWheelEventPopupTrigger = e.isPopupTrigger();
    int mouseWheelEventScrollType = e.getScrollType();
    int mouseWheelEventScrollAmount = e.getScrollAmount();
    int mouseWheelEventWheelRotation = e.getWheelRotation();
    double mouseWheelEventPreciseWheelRotation = e.getPreciseWheelRotation();

    return new MouseWheelEvent(
        mouseWheelEventSource,
        mouseWheelEventId,
        mouseWheelEventWhen,
        mouseWheelEventModifiers,
        mappedMouseWheelEventX,
        mappedMouseWheelEventY,
        mouseWheelEventXOnScreen,
        mouseWheelEventYOnScreen,
        mouseWheelEventClickCount,
        mouseWheelEventPopupTrigger,
        mouseWheelEventScrollType,
        mouseWheelEventScrollAmount,
        mouseWheelEventWheelRotation,
        mouseWheelEventPreciseWheelRotation);
  }

  /*
   * KeyListener methods - forward to Client.handler_keyboard
   */

  @Override
  public void keyTyped(KeyEvent e) {
    if (Client.handler_keyboard == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_keyboard.keyTyped(e);
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (Client.handler_keyboard == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_keyboard.keyPressed(e);
  }

  @Override
  public void keyReleased(KeyEvent e) {
    if (Client.handler_keyboard == null || Renderer.renderingScalar == 0.0f) return;

    Client.handler_keyboard.keyReleased(e);
  }

  /**
   * Gets the scaled window instance. It makes one if one doesn't exist.
   *
   * @return The scaled window instance
   */
  public static ScaledWindow getInstance() {
    if (instance == null) {
      synchronized (ScaledWindow.class) {
        instance = new ScaledWindow();
      }
    }
    return instance;
  }

  /*
   * Image rendering
   */

  /** JPanel used for rendering the game viewport, with scaling capabilities */
  private static class ScaledViewport extends JPanel {
    BufferedImage interpolationBackground = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
    BufferedImage viewportImage;

    int previousWidth = 0;
    int previousHeight = 0;

    int newWidth;
    int newHeight;

    public ScaledViewport() {
      super();
      setOpaque(true);
      setBackground(Color.black);
    }

    /** Provides the game image to the viewport */
    public void setViewportImage(BufferedImage gameImage) {
      viewportImage = gameImage;
    }

    /** Ensures the viewport image has been set */
    public boolean isViewportImageLoaded() {
      return viewportImage != null;
    }

    @Override
    protected void paintComponent(Graphics g) {
      if (viewportImage == null
          || getInstance().viewportWidth == 0
          || getInstance().viewportHeight == 0) {
        return;
      }

      // Do not perform any scaling operations at a 1.0x scalar
      if (Renderer.renderingScalar == 1.0f) {
        g.drawImage(viewportImage, 0, 0, null);
        return;
      }

      newWidth = Math.round(viewportImage.getWidth() * Renderer.renderingScalar);
      newHeight = Math.round(viewportImage.getHeight() * Renderer.renderingScalar);

      // Nearest-neighbor scaling performs roughly 3x better when resized via drawImage(),
      // whereas interpolation scaling performs better using AffineTransformOp.
      if (isIntegerScaling()) {
        // Workaround for direct drawImage warping which seems to only
        // affect macOS on JDK 19
        if (isMacOS && Settings.javaVersion >= 19) {
          g.setClip(0, 0, newWidth, newHeight);
        }

        g.drawImage(viewportImage, 0, 0, newWidth, newHeight, null);
      } else {
        if (interpolationBackground == null) {
          return;
        }

        // Reset image background when the window properties have changed
        if (previousWidth != newWidth || previousHeight != newHeight) {
          interpolationBackground =
              new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_3BYTE_BGR);

          previousWidth = newWidth;
          previousHeight = newHeight;
        }

        Graphics2D g2d = (Graphics2D) interpolationBackground.getGraphics();

        // Only perform multi-threading when the number of cores
        // is enough to support true parallelization
        if (Launcher.numCores > 4) {
          g2d.drawImage(
              multiThreadedInterpolationScaling(viewportImage, newWidth, newHeight), 0, 0, null);
        } else {
          g2d.drawImage(affineTransformScale(viewportImage, newWidth, newHeight), 0, 0, null);
        }

        g2d.dispose();

        // Draw the interpolation-scaled image
        g.drawImage(interpolationBackground, 0, 0, null);
      }
    }

    /** Scales a {@link BufferedImage} using interpolation algorithms across four threads */
    private BufferedImage multiThreadedInterpolationScaling(
        BufferedImage originalImage, int width, int height) {
      BufferedImage[] splitImages = splitImage(originalImage);

      CompletableFuture<BufferedImage> future0 =
          CompletableFuture.supplyAsync(() -> interpolationScale(splitImages[0], width, height, 0));
      CompletableFuture<BufferedImage> future1 =
          CompletableFuture.supplyAsync(() -> interpolationScale(splitImages[1], width, height, 1));
      CompletableFuture<BufferedImage> future2 =
          CompletableFuture.supplyAsync(() -> interpolationScale(splitImages[2], width, height, 2));
      CompletableFuture<BufferedImage> future3 =
          CompletableFuture.supplyAsync(() -> interpolationScale(splitImages[3], width, height, 3));

      List<BufferedImage> scaledImages =
          Stream.of(future0, future1, future2, future3)
              .map(CompletableFuture::join)
              .collect(Collectors.toList());

      return stitchImageParts(scaledImages);
    }

    /**
     * Splits a {@link BufferedImage} into four equal parts, adding extra padding to account for
     * sampling around the seams
     */
    private static BufferedImage[] splitImage(BufferedImage originalImage) {
      int offsetBuffer = 10;

      BufferedImage[] imageParts = new BufferedImage[4];

      int widthPadding = (originalImage.getWidth() & 1) == 1 ? 1 : 0;
      int heightPadding = (originalImage.getHeight() & 1) == 1 ? 1 : 0;

      originalImage = padImageIfNeeded(originalImage, widthPadding, heightPadding);

      int halfWidth = originalImage.getWidth() / 2;
      int halfHeight = originalImage.getHeight() / 2;

      imageParts[0] =
          originalImage.getSubimage(0, 0, halfWidth + offsetBuffer, halfHeight + offsetBuffer);
      imageParts[1] =
          originalImage.getSubimage(
              halfWidth - offsetBuffer, 0, halfWidth + offsetBuffer, halfHeight + offsetBuffer);
      imageParts[2] =
          originalImage.getSubimage(
              0, halfHeight - offsetBuffer, halfWidth + offsetBuffer, halfHeight + offsetBuffer);
      imageParts[3] =
          originalImage.getSubimage(
              halfWidth - offsetBuffer,
              halfHeight - offsetBuffer,
              halfWidth + offsetBuffer,
              halfHeight + offsetBuffer);

      return imageParts;
    }

    /** Pads a {@link BufferedImage} with extra pixels in preparation for even splits */
    private static BufferedImage padImageIfNeeded(
        BufferedImage originalImage, int widthPadding, int heightPadding) {
      if (widthPadding == 0 && heightPadding == 0) {
        return originalImage;
      }

      BufferedImage paddedImage =
          new BufferedImage(
              originalImage.getWidth() + widthPadding,
              originalImage.getHeight() + heightPadding,
              originalImage.getType());

      Graphics2D g2d = (Graphics2D) paddedImage.getGraphics();
      g2d.drawImage(originalImage, 0, 0, null);
      g2d.dispose();

      return paddedImage;
    }

    /**
     * Scales a {@link BufferedImage} for interpolation stitching, removing extra padding used for
     * sampling edges
     */
    private static BufferedImage interpolationScale(
        BufferedImage originalImage, int width, int height, int n) {
      int scaledOffsetBuffer = (int) (10 * Renderer.renderingScalar);

      BufferedImage scaledImage =
          affineTransformScale(
              originalImage, (width / 2) + scaledOffsetBuffer, (height / 2) + scaledOffsetBuffer);

      if (n == 0) {
        return scaledImage.getSubimage(
            0,
            0,
            scaledImage.getWidth() - scaledOffsetBuffer,
            scaledImage.getHeight() - scaledOffsetBuffer);
      } else if (n == 1) {
        return scaledImage.getSubimage(
            scaledOffsetBuffer,
            0,
            scaledImage.getWidth() - scaledOffsetBuffer,
            scaledImage.getHeight() - scaledOffsetBuffer);
      } else if (n == 2) {
        return scaledImage.getSubimage(
            0,
            scaledOffsetBuffer,
            scaledImage.getWidth() - scaledOffsetBuffer,
            scaledImage.getHeight() - scaledOffsetBuffer);
      } else {
        return scaledImage.getSubimage(
            scaledOffsetBuffer,
            scaledOffsetBuffer,
            scaledImage.getWidth() - scaledOffsetBuffer,
            scaledImage.getHeight() - scaledOffsetBuffer);
      }
    }

    /** Scales a {@link BufferedImage} using the {@link AffineTransform} method */
    public static BufferedImage affineTransformScale(
        BufferedImage originalImage, int width, int height) {
      int imageWidth = originalImage.getWidth();
      int imageHeight = originalImage.getHeight();

      double scaleX = (double) width / imageWidth;
      double scaleY = (double) height / imageHeight;

      AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
      int scalingAlgorithm = Settings.SCALING_ALGORITHM.get(Settings.currentProfile);
      AffineTransformOp scalingOp = new AffineTransformOp(scaleTransform, scalingAlgorithm);

      return scalingOp.filter(
          originalImage, new BufferedImage(width, height, originalImage.getType()));
    }

    /** Stitches multiple {@link BufferedImage}s onto one canvas */
    private static BufferedImage stitchImageParts(List<BufferedImage> imageParts) {
      int maxHeight = 0;
      int maxWidth = 0;

      for (BufferedImage imagePart : imageParts) {
        int imageWidth = imagePart.getWidth(null);
        int imageHeight = imagePart.getHeight(null);

        maxHeight = Math.max(maxHeight, imageHeight);
        maxWidth = Math.max(maxWidth, imageWidth);
      }

      BufferedImage canvas =
          new BufferedImage(maxWidth * 2, maxHeight * 2, BufferedImage.TYPE_3BYTE_BGR);
      Graphics g = canvas.getGraphics();

      g.setColor(Color.black);
      g.fillRect(0, 0, canvas.getWidth(null), canvas.getHeight(null));

      int currCol = 0;
      int currRow = 0;

      for (BufferedImage imagePart : imageParts) {
        g.drawImage(imagePart, currCol * maxWidth, currRow * maxHeight, null);
        currCol++;

        if (currCol >= 2) {
          currCol = 0;
          currRow++;
        }
      }

      return canvas;
    }

    /** Checks whether the window should be integer scaling */
    private static boolean isIntegerScaling() {
      return Settings.SCALING_ALGORITHM.get(Settings.currentProfile)
              == AffineTransformOp.TYPE_NEAREST_NEIGHBOR
          || !Settings.SCALED_CLIENT_WINDOW.get(Settings.currentProfile);
    }
  }
}
