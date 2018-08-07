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

/** Handles adjustments to the camera */
public class Camera {

  public static Object instance = null;

  public static int fov = 9;
  public static int zoom;
  public static int rotation;
  public static int rotation_auto_x;
  public static int rotation_auto_y;
  public static int distance1;
  public static int distance2;
  public static int distance3; // This one is divided onto something to do with fog (it's usually 1)
  public static int distance4; // This one seems to be fog distance

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
