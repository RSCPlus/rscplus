/**
 *	rscplus
 *
 *	This file is part of rscplus.
 *
 *	rscplus is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	rscplus is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with rscplus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Authors: see <https://github.com/OrN/rscplus>
 */

package Game;

import Client.Settings;

/**
 * Handles adjustments to the camera
 */
public class Camera {
	
	public static Object instance = null;
	
	public static int fov = 9;
	public static int zoom;
	public static int rotation;
	public static int distance1;
	public static int distance2;
	public static int distance3; // This one is divided onto something to do with fog (it's usually 1)
	public static int distance4; // This one seems to be fog distance
	
	private Camera() {
		// Empty private constructor to prevent instantiation.
	}
	
	public static void init() {
		zoom = 750; // TODO: Preserve zoom on relog, but not necessarily between sessions
		rotation = 126;
		setDistance(Settings.VIEW_DISTANCE);
	}
	
	public static void resize() {
		if (Reflection.displayMessage == null || instance == null)
			return;
		
		try {
			Reflection.setCameraSize.invoke(Camera.instance, Renderer.height_client / 2, true, Renderer.width, Renderer.width / 2, Renderer.height_client / 2, fov,
					Renderer.width / 2);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Sets the view distance, to be updated on the next tick by the Renderer. Synchronization added to hopefully
	 * prevent thread-safety concerns.
	 * 
	 * @param distance the view distance
	 */
	public static synchronized void setDistance(int distance) {
		distance1 = distance + 100;
		distance2 = distance + 100;
		distance3 = 1;
		distance4 = distance;
	}
	
	public static void setFoV(int fov) {
		// lower than 7 crashes (FoV 4, 5, 6 all crash)
		if (fov < 7)
			fov = 7;
		// higher than 16 doesn't crash, but above 16 isn't impressive (screen just turns 1 color) and does crash
		// eventually (e.g. FoV 100)
		if (fov > 16)
			fov = 16;
		Camera.fov = fov;
		resize();
	}
	
	public static void addRotation(int amount) {
		rotation = (rotation + amount) & 0xFF;
	}
	
	public static void addZoom(int amount) {
		if (amount == 0)
			return;
		
		zoom += amount;
		if (zoom > 1238)
			zoom = 1238;
		else if (zoom < 262)
			zoom = 262;
		
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
		while (((zoom - 500) / 15) + 16 == 0)
			zoom -= 1;
	}
	
}
