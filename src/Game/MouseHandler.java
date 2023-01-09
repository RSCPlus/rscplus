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
import Client.Settings;
import Client.WikiURL;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.SwingUtilities;

/** Listens to mouse events and stores relevant information about them */
public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {

  private static final int MAGIC_SCROLL_LIMIT = 42;
  private static final int PRAYER_SCROLL_LIMIT = 8;
  private static final int QUEST_SCROLL_LIMIT = 34;

  public static int x = 0;
  public static int y = 0;
  public static boolean mouseClicked = false;
  public static boolean rightClick = false;
  public static MouseListener listener_mouse = null;
  public static MouseMotionListener listener_mouse_motion = null;

  private boolean m_rotating = false;
  private Point m_rotatePosition;
  private float m_rotateX = 0.0f;

  public static boolean inPlaybackButtonBounds(Rectangle bounds) {
    if (bounds == null) return false;
    if (Replay.isPlaying
        && Settings.SHOW_PLAYER_CONTROLS.get(Settings.currentProfile)
        && Settings.SHOW_SEEK_BAR.get(Settings.currentProfile)) {
      return MouseHandler.x >= bounds.x
          && MouseHandler.x <= bounds.x + bounds.width
          && MouseHandler.y >= bounds.y
          && MouseHandler.y <= bounds.y + bounds.height;
    }
    return false;
  }

  public boolean inConsumableButton() {
    return inPlaybackButtonBounds(Renderer.previousBounds)
        || inPlaybackButtonBounds(Renderer.slowForwardBounds)
        || inPlaybackButtonBounds(Renderer.playPauseBounds)
        || inPlaybackButtonBounds(Renderer.fastForwardBounds)
        || inPlaybackButtonBounds(Renderer.nextBounds)
        || inPlaybackButtonBounds(Renderer.queueBounds)
        || inPlaybackButtonBounds(Renderer.stopBounds)
        || XPBar.shouldConsume()
        || Bank.shouldConsume()
        || WikiURL.shouldConsume()
        || HiscoresURL.shouldConsume();
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (inConsumableButton()) {
      e.consume();
    }
    if (listener_mouse == null) return;

    if (Replay.isRecording) {
      Replay.dumpMouseInput(
          Replay.MOUSE_CLICKED,
          e.getX(),
          e.getY(),
          0,
          e.getModifiers(),
          e.getClickCount(),
          0,
          0,
          e.isPopupTrigger(),
          e.getButton());
    }

    if (!e.isConsumed()) {
      x = e.getX();
      y = e.getY();
      listener_mouse.mouseClicked(e);
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    if (listener_mouse == null) return;

    if (Replay.isRecording) {
      Replay.dumpMouseInput(
          Replay.MOUSE_ENTERED,
          e.getX(),
          e.getY(),
          0,
          e.getModifiers(),
          e.getClickCount(),
          0,
          0,
          e.isPopupTrigger(),
          e.getButton());
    }

    if (!e.isConsumed()) {
      x = e.getX();
      y = e.getY();
      listener_mouse.mouseEntered(e);
    }
  }

  @Override
  public void mouseExited(MouseEvent e) {
    if (listener_mouse == null) return;

    if (Replay.isRecording) {
      Replay.dumpMouseInput(
          Replay.MOUSE_EXITED,
          e.getX(),
          e.getY(),
          0,
          e.getModifiers(),
          e.getClickCount(),
          0,
          0,
          e.isPopupTrigger(),
          e.getButton());
    }

    if (!e.isConsumed()) {
      x = -100;
      y = -100;
      listener_mouse.mouseExited(e);
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (inConsumableButton()) {
      e.consume();
    }
    if (listener_mouse == null) return;

    if (Replay.isRecording) {
      Replay.dumpMouseInput(
          Replay.MOUSE_PRESSED,
          e.getX(),
          e.getY(),
          0,
          e.getModifiers(),
          e.getClickCount(),
          0,
          0,
          e.isPopupTrigger(),
          e.getButton());
    }

    if (e.getButton() == MouseEvent.BUTTON2) {
      m_rotating = true;
      m_rotatePosition = e.getPoint();
      e.consume();
    }

    if (!e.isConsumed()) {
      x = e.getX();
      y = e.getY();
      listener_mouse.mousePressed(e);
    }

    mouseClicked = true;
    rightClick = SwingUtilities.isRightMouseButton(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (inConsumableButton()) {
      e.consume();
    }
    if (listener_mouse == null) return;

    if (Replay.isRecording) {
      Replay.dumpMouseInput(
          Replay.MOUSE_RELEASED,
          e.getX(),
          e.getY(),
          0,
          e.getModifiers(),
          e.getClickCount(),
          0,
          0,
          e.isPopupTrigger(),
          e.getButton());
    }

    if (e.getButton() == MouseEvent.BUTTON2) {
      m_rotating = false;
      e.consume();
    }

    if (!e.isConsumed()) {
      x = e.getX();
      y = e.getY();
      listener_mouse.mouseReleased(e);
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (listener_mouse_motion == null) return;

    if (Replay.isRecording) {
      Replay.dumpMouseInput(
          Replay.MOUSE_DRAGGED,
          e.getX(),
          e.getY(),
          0,
          e.getModifiers(),
          e.getClickCount(),
          0,
          0,
          e.isPopupTrigger(),
          e.getButton());
    }

    if (Settings.CAMERA_ROTATABLE.get(Settings.currentProfile) && m_rotating) {
      m_rotateX += (float) (e.getX() - m_rotatePosition.x) / 2.0f;
      int xDist = (int) m_rotateX;

      Camera.addRotation(xDist);
      m_rotateX -= xDist;

      m_rotatePosition = e.getPoint();
    }

    if (!e.isConsumed()) {
      x = e.getX();
      y = e.getY();
      listener_mouse_motion.mouseDragged(e);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    if (listener_mouse_motion == null) return;

    if (Replay.isRecording) {
      Replay.dumpMouseInput(
          Replay.MOUSE_MOVED,
          e.getX(),
          e.getY(),
          0,
          e.getModifiers(),
          e.getClickCount(),
          0,
          0,
          e.isPopupTrigger(),
          e.getButton());
    }

    if (!e.isConsumed()) {
      x = e.getX();
      y = e.getY();
      listener_mouse_motion.mouseMoved(e);
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    final int wheelRotation = e.getWheelRotation();

    if (Replay.isRecording) {
      Replay.dumpMouseInput(
          Replay.MOUSE_WHEEL_MOVED,
          e.getX(),
          e.getY(),
          wheelRotation,
          e.getModifiers(),
          e.getClickCount(),
          e.getScrollType(),
          e.getScrollAmount(),
          e.isPopupTrigger(),
          0);
    }

    x = e.getX();
    y = e.getY();

    if (Settings.ENABLE_MOUSEWHEEL_SCROLLING.get(Settings.currentProfile)) {
      try {
        if (Client.show_menu == Client.MENU_MAGIC_PRAYERS) {
          int[] scroll = (int[]) Reflection.menuScroll.get(Menu.spell_menu);
          int currScrollLimit = Menu.spell_swap_idx == 0 ? MAGIC_SCROLL_LIMIT : PRAYER_SCROLL_LIMIT;

          handleMenuScroll(wheelRotation, currScrollLimit, scroll, Menu.spell_handle, Menu.spell_menu);
        } else if (Client.show_menu == Client.MENU_STATS_QUESTS) {
          int[] scroll = (int[]) Reflection.menuScroll.get(Menu.quest_menu);

          handleMenuScroll(wheelRotation, QUEST_SCROLL_LIMIT, scroll, Menu.quest_handle, Menu.quest_menu);
        } else if (Client.show_menu == Client.MENU_FRIENDS_IGNORE) {
          int[] scroll = (int[]) Reflection.menuScroll.get(Menu.friend_menu);
          int currScrollLimit = Menu.friends_swap_idx == 0 ? Client.friends_count - 9 : Client.ignores_count - 9;

          handleMenuScroll(wheelRotation, currScrollLimit, scroll, Menu.friend_handle, Menu.friend_menu);
        } else {
          zoomCamera(wheelRotation);
        }
      } catch (IllegalAccessException iae) {
        // no-op
      }
    } else {
      zoomCamera(wheelRotation);
    }
  }

  private void handleMenuScroll(int wheelRotation, int currScrollLimit, int[] currMenu, int menuIndex, Object reflectedMenu) {
    // TODO: May need to support macOS "natural scrolling"... check plist to determine whether to flip directions
    //  see: https://stackoverflow.com/questions/7074882/java-how-to-get-the-scrolling-method-in-os-x-lion
    try {
      if (wheelRotation > 0) {
        // down
        if (currMenu[menuIndex] < currScrollLimit) {
          if (currMenu[menuIndex] + wheelRotation > currScrollLimit) {
            currMenu[menuIndex] = currScrollLimit;
          } else {
            currMenu[menuIndex] += wheelRotation;
          }

          Reflection.menuScroll.set(reflectedMenu, currMenu);
        }
      } else if (wheelRotation < 0) {
        // up
        if (currMenu[menuIndex] > 0) {
          if (currMenu[menuIndex] + wheelRotation < 0) {
            currMenu[menuIndex] = 0;
          } else {
            currMenu[menuIndex] += wheelRotation;
          }

          Reflection.menuScroll.set(reflectedMenu, currMenu);
        }
      }
    } catch (Exception ex) {
      // no-op
    }
  }

  private void zoomCamera(int wheelRotation) {
    Camera.addZoom(wheelRotation * 16);
  }
}
