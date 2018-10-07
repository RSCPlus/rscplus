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
 * <p>Authors: see <https://github.com/OrN/rscplus>
 */
package Game;

import Client.Settings;
import Client.Util;

/** Handles adjustments to the camera */
public class Camera {

  public static Object instance = null;

  private static boolean relative = false;

  public static boolean auto = false;
  public static int fov = 9;
  public static int zoom;
  public static int auto_speed;
  public static int rotation;
  public static int angle;
  public static int rotation_y;
  public static int lookat_x;
  public static int lookat_y;
  public static int distance1;
  public static int distance2;
  public static int distance3; // This one is divided onto something to do with fog (it's usually 1)
  public static int distance4; // This one seems to be fog distance

  public static float add_lookat_x;
  public static float add_lookat_y;
  public static int new_lookat_x;
  public static int new_lookat_y;
  public static float delta_lookat_x;
  public static float delta_lookat_y;
  public static float delta_zoom = 0.0f;
  public static float delta_rotation = 0.0f;

  private Camera() {
    // Empty private constructor to prevent instantiation.
  }

  public static void init() {
    zoom = 750;
    rotation = 126;
    delta_zoom = (float) zoom;
    delta_rotation = (float) rotation;
    setDistance(Settings.VIEW_DISTANCE.get(Settings.currentProfile));
    setFoV(Settings.FOV.get(Settings.currentProfile));
  }

  public static void reset_lookat() {
    lookat_x = Client.getPlayerWaypointX();
    lookat_y = Client.getPlayerWaypointY();
    new_lookat_x = lookat_x;
    new_lookat_y = lookat_y;
    delta_lookat_x = new_lookat_x;
    delta_lookat_y = new_lookat_y;
    add_lookat_x = 0.0f;
    add_lookat_y = 0.0f;
  }

  public static void update(float delta_time) {
    // Handle camera rotation, zoom, and movement
    final float camera_speed = 24.0f;
    if (KeyboardHandler.keyLeft) {
      if (KeyboardHandler.keyShift) move(-camera_speed * delta_time);
      else addRotation(2 * 50 * delta_time);
    }
    if (KeyboardHandler.keyRight) {
      if (KeyboardHandler.keyShift) move(camera_speed * delta_time);
      else addRotation(-2 * 50 * delta_time);
    }
    if (KeyboardHandler.keyUp) {
      if (KeyboardHandler.keyShift) strafe(-camera_speed * delta_time);
      else addZoom(-8 * 50 * delta_time);
    }
    if (KeyboardHandler.keyDown) {
      if (KeyboardHandler.keyShift) strafe(camera_speed * delta_time);
      else addZoom(8 * 50 * delta_time);
    }

    if (!KeyboardHandler.keyShift) {
      int tileX = ((int) add_lookat_x / 128) * 128;
      int tileY = ((int) add_lookat_y / 128) * 128;
      add_lookat_x = Util.lerp(add_lookat_x, tileX, 8.0f * delta_time);
      add_lookat_y = Util.lerp(add_lookat_y, tileY, 8.0f * delta_time);

      if (!Settings.CAMERA_MOVABLE_RELATIVE.get(Settings.currentProfile)) {
        if ((add_lookat_x != 0.0f && add_lookat_y != 0.0f)
            && (add_lookat_x < new_lookat_x + 128 && add_lookat_x > new_lookat_x - 128)
            && (add_lookat_y < new_lookat_y + 128 && add_lookat_y > new_lookat_y - 128)) {
          add_lookat_x = 0.0f;
          add_lookat_y = 0.0f;
          Client.displayMessage("The camera is now following the player", Client.CHAT_NONE);
        }
      }
    }

    // If the user changes modes, reset
    if (relative != Settings.CAMERA_MOVABLE_RELATIVE.get(Settings.currentProfile)) {
      add_lookat_x = 0;
      add_lookat_y = 0;
      relative = Settings.CAMERA_MOVABLE_RELATIVE.get(Settings.currentProfile);
    }

    delta_lookat_x = new_lookat_x; // Util.lerp(delta_lookat_x, new_lookat_x, 8.0f * delta_time);
    delta_lookat_y = new_lookat_y; // Util.lerp(delta_lookat_y, new_lookat_y, 8.0f * delta_time);

    if (!Settings.CAMERA_MOVABLE_RELATIVE.get(Settings.currentProfile)) {
      // Absolute mode
      if (add_lookat_x == 0.0f) lookat_x = (int) delta_lookat_x;
      else lookat_x = (int) add_lookat_x;

      if (add_lookat_y == 0.0f) lookat_y = (int) delta_lookat_y;
      else lookat_y = (int) add_lookat_y;
    } else {
      // Relative camera mode
      lookat_x = (int) delta_lookat_x + (int) add_lookat_x;
      lookat_y = (int) delta_lookat_y + (int) add_lookat_y;
    }

    // Reset auto speed
    if (Settings.CAMERA_ROTATABLE.get(Settings.currentProfile)) auto_speed = 0;
  }

  public static void resize() {
    if (Reflection.displayMessage == null || instance == null) return;

    try {
      Reflection.setCameraSize.invoke(
          Camera.instance,
          Renderer.height_client / 2,
          true,
          Renderer.width,
          Renderer.width / 2,
          Renderer.height_client / 2,
          fov,
          Renderer.width / 2);
    } catch (Exception e) {
    }
  }

  public static void strafe(float speed) {
    float rotation_degrees = ((float) rotation / 255.0f) * 360.0f + 90.0f;
    float xDiff = Util.lengthdir_x(64, rotation_degrees);
    float yDiff = Util.lengthdir_y(64, rotation_degrees);
    add_movement(xDiff * speed, yDiff * speed);
  }

  public static void move(float speed) {
    float rotation_degrees = ((float) rotation / 255.0f) * 360.0f;
    float xDiff = Util.lengthdir_x(64, rotation_degrees);
    float yDiff = Util.lengthdir_y(64, rotation_degrees);
    add_movement(xDiff * speed, yDiff * speed);
  }

  public static void add_movement(float x, float y) {
    if (!Settings.CAMERA_MOVABLE_RELATIVE.get(Settings.currentProfile)
        && ((add_lookat_x == 0.0f && x != 0.0f) || (add_lookat_y == 0.0f && y != 0.0f))) {
      add_lookat_x = lookat_x;
      add_lookat_y = lookat_y;
      Client.displayMessage("The camera is no longer following the player", Client.CHAT_NONE);
    }

    add_lookat_x += x;
    add_lookat_y += y;
  }

  public static void setLookatTile(int x, int y) {
    new_lookat_x = x;
    new_lookat_y = y;
  }

  /**
   * Sets the view distance, to be updated on the next tick by the Renderer. Synchronization added
   * to hopefully prevent thread-safety concerns.
   *
   * @param distance the view distance
   */
  public static synchronized void setDistance(int distance) {
    Settings.VIEW_DISTANCE_BOOL = (distance != 2300);
    distance1 = distance + 100;
    distance2 = distance + 100;
    distance3 = 1;
    distance4 = distance;
  }

  public static void setFoV(int fov) {
    Settings.FOV_BOOL = (fov != 9);

    // lower than 7 crashes (FoV 4, 5, 6 all crash)
    if (fov < 7) fov = 7;
    // higher than 16 doesn't crash, but above 16 isn't impressive (screen just turns 1 color) and
    // does crash
    // eventually (e.g. FoV 100)
    if (fov > 16) fov = 16;
    Camera.fov = fov;
    resize();
  }

  public static void addRotation(float amount) {
    if (!Settings.CAMERA_ROTATABLE.get(Settings.currentProfile)) return;

    delta_rotation += amount;
    rotation = (int) delta_rotation & 0xFF;
  }

  public static void addZoom(float amount) {
    if (amount == 0 || !Settings.CAMERA_ZOOMABLE.get(Settings.currentProfile)) return;

    delta_zoom += amount;
    if (delta_zoom > 1238.0f) delta_zoom = 1238.0f;
    else if (delta_zoom < 262.0f) delta_zoom = 262.0f;
    zoom = (int) delta_zoom;

    // Crash fix for camera zoom, camera zoom can't be zero after (((zoom - 500) / 15) + 16
    //
    // GETSTATIC Game/Camera.zoom : I
    // SIPUSH -500
    // IADD
    // BIPUSH 15
    // IDIV
    // BIPUSH 16
    // IADD <-- If zoom is -16 here, it's a guaranteed crash
    // IDIV <-- Divide by zero if zoom is (-16 + 16)
    while (((zoom - 500) / 15) + 16 == 0) {
      delta_zoom -= 1.0f;
      zoom = (int) delta_zoom;
    }
  }
}
