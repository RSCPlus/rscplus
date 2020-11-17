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

import Client.JClassLoader;
import Client.Launcher;
import Client.Logger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/** Loads and sets fields and methods found in the vanilla RSC jar's classes */
public class Reflection {

  public static Field gameReference = null;

  public static Constructor stream = null;
  public static Constructor buffer = null;

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

  public static Field clientStreamField = null;
  public static Field menuRenderer = null;
  public static Field colorLeftRight = null;
  public static Field colorTopBottom = null;
  public static Field menuX = null;
  public static Field menuY = null;
  public static Field menuScroll = null;
  public static Field menuWidth = null;
  public static Field menuHeight = null;
  public static Field menuUknown = null;
  public static Field menuType = null;
  public static Field menuShown = null;
  public static Field menuClicked = null;
  public static Field menuToggled = null;
  public static Field menuUseAltColor = null;
  public static Field menuTextSize = null;
  public static Field menuText = null;
  public static Field menuCount = null;

  public static Method getParameter = null;
  public static Method displayMessage = null;
  public static Method setCameraSize = null;
  public static Method setGameBounds = null;
  public static Method setLoginText = null;
  public static Method closeConnection = null;
  public static Method loseConnection = null;
  public static Method login = null;
  public static Method logout = null;
  public static Method itemClick = null;
  public static Method menuGen = null;
  public static Method drawBox = null;
  public static Method drawString = null;
  public static Method drawLineHoriz = null;
  public static Method drawLineVert = null;

  public static Method newPacket = null;
  public static Method putByte = null;
  public static Method putShort = null;
  public static Method putInt = null;
  public static Method putStr = null;
  public static Method putInt3Byte = null;
  public static Method encrypt = null;
  public static Method xteaEncrypt = null;
  public static Method putBytes = null;
  public static Method setBlockLength = null;
  public static Method sendPacket = null;
  public static Method flushPacket = null;
  public static Method initIsaac = null;
  public static Method readResponse = null;
  public static Field maxRetriesField = null;
  public static Field bufferField = null;
  public static Field bufferOffset = null;
  public static Field bufferByteArray = null;
  public static Method createSocket = null;

  public static Method getNpc = null;
  public static Method getPlayer = null;

  public static Method clearScreen = null;
  public static Method preGameDisplay = null;
  public static Method resetTimings = null;
  public static Method formatText = null;
  public static Method putRandom = null;

  public static Method addButtonBack = null;
  public static Method addCenterText = null;
  public static Method addButton = null;
  public static Method addInput = null;
  public static Method addText = null;
  public static Method addCheckbox = null;
  public static Method drawPanel = null;
  public static Method setFocus = null;
  public static Method isSelected = null;
  public static Method isToggle = null;
  public static Method setControlText = null;
  public static Method getControlText = null;
  public static Method handleMouse = null;
  public static Method handleKey = null;

  // Constructor descriptions
  private static final String STREAM = "da(java.net.Socket,e) throws java.io.IOException";
  private static final String BUFFER = "tb(int)";

  // Method descriptions
  private static final String GETPARAMETER =
      "public final java.lang.String e.getParameter(java.lang.String)";
  private static final String DISPLAYMESSAGE =
      "private final void client.a(boolean,java.lang.String,int,java.lang.String,int,int,java.lang.String,java.lang.String)";
  private static final String SETCAMERASIZE = "final void lb.a(int,boolean,int,int,int,int,int)";
  private static final String SETGAMEBOUNDS = "final void ua.a(int,int,int,int,byte)";
  private static final String SETLOGINTEXT =
      "private final void client.b(byte,java.lang.String,java.lang.String)";
  private static final String CLOSECONNECTION = "private final void client.a(boolean,int)";
  private static final String LOSECONNECTION = "final void da.a(boolean)";
  private static final String LOGIN =
      "private final void client.a(int,java.lang.String,java.lang.String,boolean)";
  private static final String LOGOUT = "private final void client.B(int)";
  private static final String ITEMCLICK = "private final void client.b(boolean,int)";
  private static final String MENUGEN =
      "final void wb.a(int,int,boolean,java.lang.String,java.lang.String)";
  private static final String DRAWBOX = "final void ua.a(int,byte,int,int,int,int)";
  private static final String DRAWSTRING =
      "final void ua.a(java.lang.String,int,int,int,boolean,int)";
  private static final String DRAWLINEHORIZ = "final void ua.b(int,int,int,int,byte)";
  private static final String DRAWLINEVERT = "final void ua.b(int,int,int,int,int)";

  private static final String NEWPACKET = "final void b.b(int,int)";
  private static final String PUTBYTE = "final void tb.c(int,int)";
  private static final String PUTSHORT = "final void tb.e(int,int)";
  private static final String PUTINT = "final void tb.b(int,int)";
  private static final String PUTSTR = "final void tb.a(byte,java.lang.String)";
  private static final String PUTINT3BYTE = "final void tb.b(int,byte)";
  private static final String ENCRYPT =
      "final void tb.a(java.math.BigInteger,int,java.math.BigInteger)";
  private static final String XTEA_ENCRYPT = "final void tb.a(byte,int,int[],int)";
  private static final String PUTBYTES = "final void tb.a(int,int,int,byte[])";
  private static final String SETBLOCKLENGTH = "final void tb.d(int,int)";
  private static final String SENDPACKET = "final void b.b(int)";
  private static final String FLUSHPACKET = "final void b.a(int) throws java.io.IOException";
  private static final String INIT_ISAAC = "final void b.a(byte,int[])";
  private static final String READ_RESPONSE = "final int da.b(boolean) throws java.io.IOException";
  private static final String CREATE_SOCKET =
      "private final java.net.Socket client.a(int,int,java.lang.String) throws java.io.IOException";

  private static final String GETNPC = "private final ta client.b(int,byte)";
  private static final String GETPLAYER = "private final ta client.d(int,int)";

  private static final String CLEARSCREEN = "final void ua.a(boolean)";

  private static final String PREGAME_DISPLAY = "private final void client.k(int)";
  private static final String RESET_TIMINGS = "final void e.c(int)";

  private static final String FORMAT_TEXT =
      "static final java.lang.String b.a(int,byte,java.lang.String)";
  private static final String PUTRANDOM = "static final void f.a(int,tb)";

  private static final String ADDBUTTONBACK = "final int qa.c(int,int,int,int,int)";
  private static final String ADDCENTERTEXT =
      "final int qa.a(boolean,byte,int,int,java.lang.String,int)";
  private static final String ADDBUTTON = "final int qa.d(int,int,int,int,int)";
  private static final String ADDINPUT =
      "final int qa.a(int,int,int,boolean,int,int,int,boolean,int)";
  private static final String DRAWPANEL = "final void qa.a(byte)";
  private static final String SETFOCUS = "final void qa.d(int,int)";
  private static final String ISSELECTED = "final boolean qa.a(byte,int)";
  private static final String ISTOGGLE = "final int qa.f(int,int)";
  private static final String SETCONTROLTEXT = "final void qa.a(int,java.lang.String,int)";
  private static final String GETCONTROLTEXT = "final java.lang.String qa.g(int,int)";
  private static final String HANDLEMOUSE = "final void qa.b(int,int,int,int,int)";
  private static final String HANDLEKEY = "final void qa.a(int,int)";

  public static void Load() {
    try {
      JClassLoader classLoader = Launcher.getInstance().getClassLoader();
      ArrayList<String> leftMethods =
          new ArrayList<String>(); // expected virtual methods to find in given class

      // Game Applet
      Class<?> c = classLoader.loadClass("e");
      Method[] methods = c.getDeclaredMethods();
      for (Method method : methods) {
        if (method.toGenericString().equals(RESET_TIMINGS)) {
          resetTimings = method;
          Logger.Info("Found resetTimings");
        } else if (method.toGenericString().equals(GETPARAMETER)) {
          getParameter = method;
          Logger.Info("Found getParameter");
        }
      }

      // Client
      c = classLoader.loadClass("client");
      clientStreamField = c.getDeclaredField("Jh");
      methods = c.getDeclaredMethods();
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
        } else if (method.toGenericString().equals(GETNPC)) {
          getNpc = method;
          Logger.Info("Found getNpc");
        } else if (method.toGenericString().equals(GETPLAYER)) {
          getPlayer = method;
          Logger.Info("Found getPlayer");
        } else if (method.toGenericString().equals(PREGAME_DISPLAY)) {
          preGameDisplay = method;
          Logger.Info("Found preGameDisplay");
        } else if (method.toGenericString().equals(CREATE_SOCKET)) {
          createSocket = method;
          Logger.Info("Found createSocket");
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
      if (maxInventory != null) maxInventory.setAccessible(true);

      // Client Stream
      c = classLoader.loadClass("da");
      Constructor[] constructors = c.getDeclaredConstructors();
      for (Constructor constructor : constructors) {
        if (constructor.toGenericString().equals(STREAM)) {
          stream = constructor;
          Logger.Info("Found stream");
        }
      }

      leftMethods.addAll(
          Arrays.asList(
              NEWPACKET, LOSECONNECTION, SENDPACKET, FLUSHPACKET, INIT_ISAAC, READ_RESPONSE));
      while (c != null && leftMethods.size() > 0) {
        methods = c.getDeclaredMethods();
        for (Method method : methods) {
          if (leftMethods.contains(NEWPACKET) && method.toGenericString().equals(NEWPACKET)) {
            newPacket = method;
            Logger.Info("Found newPacket");
            leftMethods.remove(NEWPACKET);
            continue;
          }
          if (leftMethods.contains(LOSECONNECTION)
              && method.toGenericString().contains(LOSECONNECTION)) {
            loseConnection = method;
            Logger.Info("Found loseConnection");
            leftMethods.remove(LOSECONNECTION);
            continue;
          }
          if (leftMethods.contains(SENDPACKET) && method.toGenericString().equals(SENDPACKET)) {
            sendPacket = method;
            Logger.Info("Found sendPacket");
            leftMethods.remove(SENDPACKET);
            continue;
          }
          if (leftMethods.contains(FLUSHPACKET) && method.toGenericString().equals(FLUSHPACKET)) {
            flushPacket = method;
            Logger.Info("Found flushPacket");
            leftMethods.remove(FLUSHPACKET);
            continue;
          }
          if (leftMethods.contains(INIT_ISAAC) && method.toGenericString().equals(INIT_ISAAC)) {
            initIsaac = method;
            Logger.Info("Found initIsaac");
            leftMethods.remove(INIT_ISAAC);
            continue;
          }
          if (leftMethods.contains(READ_RESPONSE)
              && method.toGenericString().equals(READ_RESPONSE)) {
            readResponse = method;
            Logger.Info("Found readResponse");
            leftMethods.remove(READ_RESPONSE);
            continue;
          }
        }
        c = c.getSuperclass();
      }
      if (leftMethods.size() > 0) {
        System.out.println("Not found : " + leftMethods.size() + "methods for da class");
      }

      // Buffer field of clientStream
      c = classLoader.loadClass("b");
      Field[] fields = c.getDeclaredFields();
      for (Field field : fields) {
        if (field.getName().equals("f")) {
          bufferField = field;
          Logger.Info("Found bufferField");
        }
        if (field.getName().equals("d")) {
          maxRetriesField = field;
          Logger.Info("Found maxRetriesField");
        }
      }
      methods = c.getDeclaredMethods();
      for (Method method : methods) {
        if (method.toGenericString().equals(FORMAT_TEXT)) {
          formatText = method;
          Logger.Info("Found formatText");
        }
      }

      // Write buffer
      c = classLoader.loadClass("ja");
      leftMethods.addAll(
          Arrays.asList(
              PUTBYTE,
              PUTSHORT,
              PUTINT,
              PUTSTR,
              PUTINT3BYTE,
              PUTBYTES,
              SETBLOCKLENGTH,
              ENCRYPT,
              XTEA_ENCRYPT));
      while (c != null && leftMethods.size() > 0) {
        methods = c.getDeclaredMethods();
        for (Method method : methods) {
          if (leftMethods.contains(PUTBYTE) && method.toGenericString().equals(PUTBYTE)) {
            putByte = method;
            Logger.Info("Found putByte");
            leftMethods.remove(PUTBYTE);
            continue;
          }
          if (leftMethods.contains(PUTSHORT) && method.toGenericString().equals(PUTSHORT)) {
            putShort = method;
            Logger.Info("Found putShort");
            leftMethods.remove(PUTSHORT);
            continue;
          }
          if (leftMethods.contains(PUTINT) && method.toGenericString().equals(PUTINT)) {
            putInt = method;
            Logger.Info("Found putInt");
            leftMethods.remove(PUTINT);
            continue;
          }
          if (leftMethods.contains(PUTSTR) && method.toGenericString().equals(PUTSTR)) {
            putStr = method;
            Logger.Info("Found putStr");
            leftMethods.remove(PUTSTR);
            continue;
          }
          if (leftMethods.contains(PUTINT3BYTE) && method.toGenericString().equals(PUTINT3BYTE)) {
            putInt3Byte = method;
            Logger.Info("Found putInt3Byte");
            leftMethods.remove(PUTINT3BYTE);
            continue;
          }
          if (leftMethods.contains(PUTBYTES) && method.toGenericString().equals(PUTBYTES)) {
            putBytes = method;
            Logger.Info("Found putBytes");
            leftMethods.remove(PUTBYTES);
            continue;
          }
          if (leftMethods.contains(SETBLOCKLENGTH)
              && method.toGenericString().equals(SETBLOCKLENGTH)) {
            setBlockLength = method;
            Logger.Info("Found setBlockLength");
            leftMethods.remove(SETBLOCKLENGTH);
            continue;
          }
          if (leftMethods.contains(ENCRYPT) && method.toGenericString().equals(ENCRYPT)) {
            encrypt = method;
            Logger.Info("Found encrypt");
            leftMethods.remove(ENCRYPT);
            continue;
          }
          if (leftMethods.contains(XTEA_ENCRYPT) && method.toGenericString().equals(XTEA_ENCRYPT)) {
            xteaEncrypt = method;
            Logger.Info("Found xteaEncrypt");
            leftMethods.remove(XTEA_ENCRYPT);
            continue;
          }
        }
        c = c.getSuperclass();
      }
      if (leftMethods.size() > 0) {
        System.out.println("Not found : " + leftMethods.size() + "methods for ja class");
      }

      // Buffer block class?
      c = classLoader.loadClass("tb");
      constructors = c.getDeclaredConstructors();
      for (Constructor constructor : constructors) {
        if (constructor.toGenericString().equals(BUFFER)) {
          buffer = constructor;
          Logger.Info("Found buffer");
        }
      }
      fields = c.getDeclaredFields();
      for (Field field : fields) {
        if (field.getName().equals("w")) {
          bufferOffset = field;
          Logger.Info("Found bufferOffset");
        }
        if (field.getName().equals("F")) {
          bufferByteArray = field;
          Logger.Info("Found bufferByteArray");
        }
      }

      // Helper class for buffer block?
      c = classLoader.loadClass("f");
      methods = c.getDeclaredMethods();
      for (Method method : methods) {
        if (method.toGenericString().equals(PUTRANDOM)) {
          putRandom = method;
          Logger.Info("Found putRandom");
        }
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
        if (method.toGenericString().equals(CLEARSCREEN)) {
          clearScreen = method;
          Logger.Info("Found clearScreen");
        }
        if (method.toGenericString().equals(DRAWBOX)) {
          drawBox = method;
          Logger.Info("Found drawBox");
        }
        if (method.toGenericString().equals(DRAWSTRING)) {
          drawString = method;
          Logger.Info("Found drawString");
        }
        if (method.toGenericString().equals(DRAWLINEHORIZ)) {
          drawLineHoriz = method;
          Logger.Info("Found drawLineHoriz");
        }
        if (method.toGenericString().equals(DRAWLINEVERT)) {
          drawLineVert = method;
          Logger.Info("Found drawLineVert");
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
      if (characterName != null) characterName.setAccessible(true);
      if (characterDisplayName != null) characterDisplayName.setAccessible(true);
      if (characterX != null) characterX.setAccessible(true);
      if (characterY != null) characterY.setAccessible(true);
      if (characterDamageTaken != null) characterDamageTaken.setAccessible(true);
      if (characterCurrentHits != null) characterCurrentHits.setAccessible(true);
      if (characterMaxHits != null) characterMaxHits.setAccessible(true);
      if (characterWaypointX != null) characterWaypointX.setAccessible(true);
      if (characterWaypointY != null) characterWaypointY.setAccessible(true);
      if (attackingPlayerIdx != null) attackingPlayerIdx.setAccessible(true);
      if (attackingNpcIdx != null) attackingNpcIdx.setAccessible(true);

      // Menu
      c = classLoader.loadClass("qa");
      menuRenderer = c.getDeclaredField("w");
      colorLeftRight = c.getDeclaredField("J");
      colorTopBottom = c.getDeclaredField("tb");
      menuX = c.getDeclaredField("kb");
      menuY = c.getDeclaredField("B");
      menuScroll = c.getDeclaredField("j");
      menuWidth = c.getDeclaredField("ob");
      // this menu height for chats I believe
      menuHeight = c.getDeclaredField("O");
      // this menu array I'm not sure whats for
      menuUknown = c.getDeclaredField("sb");
      menuType = c.getDeclaredField("U");
      menuShown = c.getDeclaredField("g");
      menuClicked = c.getDeclaredField("D");
      menuToggled = c.getDeclaredField("vb");
      menuUseAltColor = c.getDeclaredField("Y");
      menuTextSize = c.getDeclaredField("k");
      menuText = c.getDeclaredField("yb");
      menuCount = c.getDeclaredField("eb");
      methods = c.getDeclaredMethods();
      for (Method method : methods) {
        if (method.toGenericString().equals(ADDBUTTONBACK)) {
          addButtonBack = method;
          Logger.Info("Found addButtonBack");
        }
        if (method.toGenericString().equals(ADDCENTERTEXT)) {
          addCenterText = method;
          Logger.Info("Found addText");
        }
        if (method.toGenericString().equals(ADDBUTTON)) {
          addButton = method;
          Logger.Info("Found addButton");
        }
        if (method.toGenericString().equals(ADDINPUT)) {
          addInput = method;
          Logger.Info("Found addInput");
        }
        if (method.toGenericString().equals(DRAWPANEL)) {
          drawPanel = method;
          Logger.Info("Found drawPanel");
        }
        if (method.toGenericString().equals(SETFOCUS)) {
          setFocus = method;
          Logger.Info("Found setFocus");
        }
        if (method.toGenericString().equals(ISSELECTED)) {
          isSelected = method;
          Logger.Info("Found isSelected");
        }
        if (method.toGenericString().equals(ISTOGGLE)) {
          isToggle = method;
          Logger.Info("Found isToggle");
        }
        if (method.toGenericString().equals(SETCONTROLTEXT)) {
          setControlText = method;
          Logger.Info("Found setControlText");
        }
        if (method.toGenericString().equals(GETCONTROLTEXT)) {
          getControlText = method;
          Logger.Info("Found getControlText");
        }
        if (method.toGenericString().equals(HANDLEMOUSE)) {
          handleMouse = method;
          Logger.Info("Found handleMouse");
        }
        if (method.toGenericString().equals(HANDLEKEY)) {
          handleKey = method;
          Logger.Info("Found handleKey");
        }
      }

      c = classLoader.loadClass("wb");
      methods = c.getDeclaredMethods();
      for (Method method : methods) {
        if (method.toGenericString().equals(MENUGEN)) {
          menuGen = method;
          Logger.Info("Found menuGen");
        }
      }

      c = classLoader.loadClass("kb");
      fields = c.getDeclaredFields();
      for (Field field : fields) {
        if (field.getName().equals("a")) {
          gameReference = field;
          Logger.Info("Found gameReference");
        }
      }

      // Set all accessible
      if (clientStreamField != null) clientStreamField.setAccessible(true);
      if (menuRenderer != null) menuRenderer.setAccessible(true);
      if (colorLeftRight != null) colorLeftRight.setAccessible(true);
      if (colorTopBottom != null) colorTopBottom.setAccessible(true);
      if (menuX != null) menuX.setAccessible(true);
      if (menuY != null) menuY.setAccessible(true);
      if (menuScroll != null) menuScroll.setAccessible(true);
      if (menuWidth != null) menuWidth.setAccessible(true);
      if (menuHeight != null) menuHeight.setAccessible(true);
      if (menuUknown != null) menuUknown.setAccessible(true);
      if (menuType != null) menuType.setAccessible(true);
      if (menuShown != null) menuShown.setAccessible(true);
      if (menuClicked != null) menuClicked.setAccessible(true);
      if (menuToggled != null) menuToggled.setAccessible(true);
      if (menuUseAltColor != null) menuUseAltColor.setAccessible(true);
      if (menuTextSize != null) menuTextSize.setAccessible(true);
      if (menuText != null) menuText.setAccessible(true);
      if (menuCount != null) menuCount.setAccessible(true);
      if (getParameter != null) getParameter.setAccessible(true);
      if (displayMessage != null) displayMessage.setAccessible(true);
      if (setCameraSize != null) setCameraSize.setAccessible(true);
      if (setGameBounds != null) setGameBounds.setAccessible(true);
      if (setLoginText != null) setLoginText.setAccessible(true);
      if (closeConnection != null) closeConnection.setAccessible(true);
      if (loseConnection != null) loseConnection.setAccessible(true);
      if (login != null) login.setAccessible(true);
      if (logout != null) logout.setAccessible(true);
      if (itemClick != null) itemClick.setAccessible(true);
      if (menuGen != null) menuGen.setAccessible(true);
      if (drawBox != null) drawBox.setAccessible(true);
      if (drawString != null) drawString.setAccessible(true);
      if (drawLineHoriz != null) drawLineHoriz.setAccessible(true);
      if (drawLineVert != null) drawLineVert.setAccessible(true);
      if (stream != null) stream.setAccessible(true);
      if (newPacket != null) newPacket.setAccessible(true);
      if (putByte != null) putByte.setAccessible(true);
      if (putShort != null) putShort.setAccessible(true);
      if (putInt != null) putInt.setAccessible(true);
      if (putStr != null) putStr.setAccessible(true);
      if (putInt3Byte != null) putInt3Byte.setAccessible(true);
      if (encrypt != null) encrypt.setAccessible(true);
      if (xteaEncrypt != null) xteaEncrypt.setAccessible(true);
      if (putBytes != null) putBytes.setAccessible(true);
      if (setBlockLength != null) setBlockLength.setAccessible(true);
      if (sendPacket != null) sendPacket.setAccessible(true);
      if (flushPacket != null) flushPacket.setAccessible(true);
      if (initIsaac != null) initIsaac.setAccessible(true);
      if (readResponse != null) readResponse.setAccessible(true);
      if (bufferField != null) bufferField.setAccessible(true);
      if (bufferOffset != null) bufferOffset.setAccessible(true);
      if (bufferByteArray != null) bufferByteArray.setAccessible(true);
      if (maxRetriesField != null) maxRetriesField.setAccessible(true);
      if (createSocket != null) createSocket.setAccessible(true);
      if (buffer != null) buffer.setAccessible(true);
      if (getNpc != null) getNpc.setAccessible(true);
      if (getPlayer != null) getPlayer.setAccessible(true);
      if (clearScreen != null) clearScreen.setAccessible(true);
      if (preGameDisplay != null) preGameDisplay.setAccessible(true);
      if (resetTimings != null) resetTimings.setAccessible(true);
      if (formatText != null) formatText.setAccessible(true);
      if (putRandom != null) putRandom.setAccessible(true);
      if (addButtonBack != null) addButtonBack.setAccessible(true);
      if (addCenterText != null) addCenterText.setAccessible(true);
      if (addButton != null) addButton.setAccessible(true);
      if (addInput != null) addInput.setAccessible(true);
      if (addCheckbox != null) addCheckbox.setAccessible(true);
      if (drawPanel != null) drawPanel.setAccessible(true);
      if (setFocus != null) setFocus.setAccessible(true);
      if (isSelected != null) isSelected.setAccessible(true);
      if (isToggle != null) isToggle.setAccessible(true);
      if (setControlText != null) setControlText.setAccessible(true);
      if (getControlText != null) getControlText.setAccessible(true);
      if (handleMouse != null) handleMouse.setAccessible(true);
      if (handleKey != null) handleKey.setAccessible(true);
      if (gameReference != null) gameReference.setAccessible(true);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
