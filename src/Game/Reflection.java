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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import Client.JClassLoader;
import Client.Launcher;
import Client.Logger;

/**
 * Loads and sets fields and methods found in the vanilla RSC jar's classes
 */
public class Reflection {
	
	public static Field characterName = null;
	public static Field characterDisplayName = null;
	public static Field characterX = null;
	public static Field characterY = null;
	public static Field characterDamageTaken = null;
	public static Field characterCurrentHits = null;
	public static Field characterMaxHits = null;
	public static Field characterWaypointX = null;
	public static Field characterWaypointY = null;
	public static Field attackingPlayerIdx = null;
	public static Field attackingNpcIdx = null;
	public static Field lastMouseAction = null;
	
	public static Field maxInventory = null;
	
	public static Field menuX = null;
	public static Field menuY = null;
	public static Field menuScroll = null;
	public static Field menuWidth = null;
	public static Field menuHeight = null;
	public static Field menuUknown = null;
	
	public static Method displayMessage = null;
	public static Method setCameraSize = null;
	public static Method setGameBounds = null;
	public static Method setLoginText = null;
	public static Method closeConnection = null;
	public static Method login = null;
	public static Method logout = null;
	public static Method itemClick = null;
	public static Method menuGen = null;
	
	public static Method newPacket = null;
	public static Method putShort = null;
	public static Method sendPacket = null;
	public static Field bufferField = null;
	
	// Method descriptions
	private static final String DISPLAYMESSAGE = "private final void client.a(boolean,java.lang.String,int,java.lang.String,int,int,java.lang.String,java.lang.String)";
	private static final String SETCAMERASIZE = "final void lb.a(int,boolean,int,int,int,int,int)";
	private static final String SETGAMEBOUNDS = "final void ua.a(int,int,int,int,byte)";
	private static final String SETLOGINTEXT = "private final void client.b(byte,java.lang.String,java.lang.String)";
	private static final String CLOSECONNECTION = "private final void client.a(boolean,int)";
	private static final String LOGIN = "private final void client.a(int,java.lang.String,java.lang.String,boolean)";
	private static final String LOGOUT = "private final void client.B(int)";
	private static final String ITEMCLICK = "private final void client.b(boolean,int)";
	private static final String MENUGEN = "final void wb.a(int,int,boolean,java.lang.String,java.lang.String)";
	
	private static final String NEWPACKET = "final void b.b(int,int)";
	private static final String PUTSHORT = "final void tb.e(int,int)";
	private static final String SENDPACKET = "final void b.b(int)";
	
	public static void Load() {
		try {
			JClassLoader classLoader = Launcher.getInstance().getClassLoader();
			boolean found = false;
			boolean found2 = false;
			
			// Client
			Class<?> c = classLoader.loadClass("client");
			Method[] methods = c.getDeclaredMethods();
			for (Method method : methods) {
				if (method.toGenericString().equals(DISPLAYMESSAGE)) {
					displayMessage = method;
					Logger.Info("Found displayMessage");
				} else if (method.toGenericString().equals(SETLOGINTEXT)) {
					setLoginText = method;
					Logger.Info("Found setLoginText");
				} else if (method.toGenericString().equals(CLOSECONNECTION)) {
					closeConnection = method;
					Logger.Info("Found closeConnection");
				} else if (method.toGenericString().equals(LOGIN)) {
					login = method;
					Logger.Info("Found login");
				} else if (method.toGenericString().equals(LOGOUT)) {
					logout = method;
					Logger.Info("Found logout");
				} else if (method.toGenericString().equals(ITEMCLICK)) {
					itemClick = method;
					Logger.Info("Found itemClick");
				}
			}
			
			// Game Applet
			lastMouseAction = c.getSuperclass().getDeclaredField("sb");
			lastMouseAction.setAccessible(true);
			
			// Region X and Region Y
			c.getDeclaredField("Qg").setAccessible(true);
			c.getDeclaredField("zg").setAccessible(true);
			// Local Region X and Local Region Y
			c.getDeclaredField("Lf").setAccessible(true);
			c.getDeclaredField("sh").setAccessible(true);
			// Plane related info + loadingArea var (though this one changes way too fast)
			c.getDeclaredField("Ki").setAccessible(true);
			c.getDeclaredField("sk").setAccessible(true);
			c.getDeclaredField("bc").setAccessible(true);
			c.getDeclaredField("Ub").setAccessible(true);
			// Maximum inventory (30)
			maxInventory = c.getDeclaredField("cl");
			if (maxInventory != null)
				maxInventory.setAccessible(true);
			
			// Client Stream
			c = classLoader.loadClass("da");
			found = false;
			found2 = false;
			while (c != null && !found && !found2) {
				methods = c.getDeclaredMethods();
				for (Method method : methods) {
					if (method.toGenericString().equals(NEWPACKET)) {
						newPacket = method;
						Logger.Info("Found newPacket");
						found = true;
						continue;
					}
					if (method.toGenericString().equals(SENDPACKET)) {
						sendPacket = method;
						Logger.Info("Found sendPacket");
						found2 = true;
						continue;
					}
				}
				c = c.getSuperclass();
			}
			
			// Buffer field of clientStream
			c = classLoader.loadClass("b");
			Field[] fields = c.getDeclaredFields();
			for (Field field : fields) {
				if (field.getName().equals("f")) {
					bufferField = field;
					Logger.Info("Found bufferField");
				}
			}
			
			// Write buffer
			c = classLoader.loadClass("ja");
			found = false;
			while (c != null && !found) {
				methods = c.getDeclaredMethods();
				for (Method method : methods) {
					if (method.toGenericString().equals(PUTSHORT)) {
						putShort = method;
						Logger.Info("Found putShort");
						found = true;
						break;
					}
				}
				c = c.getSuperclass();
			}
			
			// Camera
			c = classLoader.loadClass("lb");
			methods = c.getDeclaredMethods();
			for (Method method : methods) {
				if (method.toGenericString().equals(SETCAMERASIZE)) {
					setCameraSize = method;
					Logger.Info("Found setCameraSize");
				}
			}
			
			// Renderer
			c = classLoader.loadClass("ua");
			methods = c.getDeclaredMethods();
			for (Method method : methods) {
				if (method.toGenericString().equals(SETGAMEBOUNDS)) {
					setGameBounds = method;
					Logger.Info("Found setGameBounds");
				}
			}
			
			// Character
			c = classLoader.loadClass("ta");
			characterName = c.getDeclaredField("C");
			characterDisplayName = c.getDeclaredField("c");
			characterX = c.getDeclaredField("i");
			characterY = c.getDeclaredField("K");
			characterDamageTaken = c.getDeclaredField("u");
			characterCurrentHits = c.getDeclaredField("B");
			characterMaxHits = c.getDeclaredField("G");
			characterWaypointX = c.getDeclaredField("i");
			characterWaypointY = c.getDeclaredField("K");
			attackingPlayerIdx = c.getDeclaredField("z");
			attackingNpcIdx = c.getDeclaredField("h");
			if (characterName != null)
				characterName.setAccessible(true);
			if (characterDisplayName != null)
				characterDisplayName.setAccessible(true);
			if (characterX != null)
				characterX.setAccessible(true);
			if (characterY != null)
				characterY.setAccessible(true);
			if (characterDamageTaken != null)
				characterDamageTaken.setAccessible(true);
			if (characterCurrentHits != null)
				characterCurrentHits.setAccessible(true);
			if (characterMaxHits != null)
				characterMaxHits.setAccessible(true);
			if (characterWaypointX != null)
				characterWaypointX.setAccessible(true);
			if (characterWaypointY != null)
				characterWaypointY.setAccessible(true);
			if (attackingPlayerIdx != null)
				attackingPlayerIdx.setAccessible(true);
			if (attackingNpcIdx != null)
				attackingNpcIdx.setAccessible(true);
			
			// Menu
			c = classLoader.loadClass("qa");
			menuX = c.getDeclaredField("kb");
			menuY = c.getDeclaredField("B");
			menuScroll = c.getDeclaredField("j");
			menuWidth = c.getDeclaredField("ob");
			// this menu height for chats I believe
			menuHeight = c.getDeclaredField("O");
			// this menu array I'm not sure whats for
			menuUknown = c.getDeclaredField("sb");
			
			c = classLoader.loadClass("wb");
			methods = c.getDeclaredMethods();
			for (Method method : methods) {
				if (method.toGenericString().equals(MENUGEN)) {
					menuGen = method;
					Logger.Info("Found menuGen");
				}
			}
			
			// Set all accessible
			if (menuX != null)
				menuX.setAccessible(true);
			if (menuY != null)
				menuY.setAccessible(true);
			if (menuScroll != null)
				menuScroll.setAccessible(true);
			if (menuWidth != null)
				menuWidth.setAccessible(true);
			if (menuHeight != null)
				menuHeight.setAccessible(true);
			if (menuUknown != null)
				menuUknown.setAccessible(true);
			if (displayMessage != null)
				displayMessage.setAccessible(true);
			if (setCameraSize != null)
				setCameraSize.setAccessible(true);
			if (setGameBounds != null)
				setGameBounds.setAccessible(true);
			if (setLoginText != null)
				setLoginText.setAccessible(true);
			if (closeConnection != null)
				closeConnection.setAccessible(true);
			if (login != null)
				login.setAccessible(true);
			if (logout != null)
				logout.setAccessible(true);
			if (itemClick != null)
				itemClick.setAccessible(true);
			if (menuGen != null)
				menuGen.setAccessible(true);
			if (newPacket != null)
				newPacket.setAccessible(true);
			if (putShort != null)
				putShort.setAccessible(true);
			if (sendPacket != null)
				sendPacket.setAccessible(true);
			if (bufferField != null)
				bufferField.setAccessible(true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
