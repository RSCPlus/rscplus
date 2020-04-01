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

import Client.KeybindSet;
import Client.KeybindSet.KeyModifier;
import Client.Settings;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

/** Listens to keyboard events to trigger specified operations */
public class KeyboardHandler implements KeyListener {

  public static int dialogue_option = -1;
  public static KeyListener listener_key;
  public static boolean keyLeft = false;
  public static boolean keyRight = false;
  public static boolean keyUp = false;
  public static boolean keyDown = false;
  public static boolean keyShift = false;

  /** ArrayList containing all registered KeybindSet values */
  public static ArrayList<KeybindSet> keybindSetList = new ArrayList<KeybindSet>();

  /**
   * Hashmap containing all default KeybindSet values. This is used in the ConfigWindow restore
   * default keybinds method.
   */
  public static HashMap<String, KeybindSet> defaultKeybindSetList =
      new HashMap<String, KeybindSet>();

  // to detect if its altgr = ctr + alt
  // TODO: Make spacebar clear the login message screen
  @Override
  public void keyPressed(KeyEvent e) {
    boolean shouldConsume;

    boolean altgr = false;
    if (e.isControlDown() && e.isAltDown() || e.isAltGraphDown()) {
      altgr = true;
    }

    if (e.isControlDown() && !altgr) {
      for (KeybindSet kbs : keybindSetList) {
        if (kbs.getModifier() == KeyModifier.CTRL && e.getKeyCode() == kbs.getKey()) {
          shouldConsume = Settings.processKeybindCommand(kbs.getCommandName());
          if (shouldConsume) {
            e.consume();
          }
        }
      }

    } else if (e.isShiftDown()) {
      for (KeybindSet kbs : keybindSetList) {
        if (kbs.getModifier() == KeyModifier.SHIFT && e.getKeyCode() == kbs.getKey()) {
          shouldConsume = Settings.processKeybindCommand(kbs.getCommandName());
          if (shouldConsume) {
            e.consume();
          }
        }
      }

    } else if (e.isAltDown() && !altgr) {
      for (KeybindSet kbs : keybindSetList) {
        if (kbs.getModifier() == KeyModifier.ALT && e.getKeyCode() == kbs.getKey()) {
          shouldConsume = Settings.processKeybindCommand(kbs.getCommandName());
          if (shouldConsume) {
            e.consume();
          }
        }
      }

    } else {
      for (KeybindSet kbs : keybindSetList) {
        if (kbs.getModifier() == KeyModifier.NONE && e.getKeyCode() == kbs.getKey()) {
          shouldConsume = Settings.processKeybindCommand(kbs.getCommandName());
          if (shouldConsume) {
            e.consume();
          }
        }
      }
    }

    if (Replay.isRecording && !e.isConsumed()) {
      Replay.dumpKeyboardInput(
          e.getKeyCode(), Replay.KEYBOARD_PRESSED, e.getKeyChar(), e.getModifiers());
    }

    if (Client.show_questionmenu && !e.isConsumed() && !Replay.isPlaying) {
      if (e.getKeyCode() == KeyEvent.VK_1 || e.getKeyCode() == KeyEvent.VK_NUMPAD1)
        dialogue_option = 0;
      else if (e.getKeyCode() == KeyEvent.VK_2 || e.getKeyCode() == KeyEvent.VK_NUMPAD2)
        dialogue_option = 1;
      else if (e.getKeyCode() == KeyEvent.VK_3 || e.getKeyCode() == KeyEvent.VK_NUMPAD3)
        dialogue_option = 2;
      else if (e.getKeyCode() == KeyEvent.VK_4 || e.getKeyCode() == KeyEvent.VK_NUMPAD4)
        dialogue_option = 3;
      else if (e.getKeyCode() == KeyEvent.VK_5 || e.getKeyCode() == KeyEvent.VK_NUMPAD5)
        dialogue_option = 4;
      else if (e.getKeyCode() == KeyEvent.VK_6 || e.getKeyCode() == KeyEvent.VK_NUMPAD6)
        dialogue_option = 5;
      else if (e.getKeyCode() == KeyEvent.VK_7 || e.getKeyCode() == KeyEvent.VK_NUMPAD7)
        dialogue_option = 6;
      else if (e.getKeyCode() == KeyEvent.VK_8 || e.getKeyCode() == KeyEvent.VK_NUMPAD8)
        dialogue_option = 7;
      else if (e.getKeyCode() == KeyEvent.VK_9 || e.getKeyCode() == KeyEvent.VK_NUMPAD9)
        dialogue_option = 8;

      if (dialogue_option >= 0) e.consume();
    }

    if (Client.state == Client.STATE_GAME
        && e.getKeyCode() == KeyEvent.VK_TAB
        && !Client.isInterfaceOpen()) {
      if (!Replay.isPlaying && Client.lastpm_username != null) {
        Client.pm_text = "";
        Client.pm_enteredText = "";
        Client.pm_username = Client.lastpm_username;
        Client.show_friends = 2;
      }
      e.consume();
    }

    // Handle camera keys
    if (!e.isConsumed()) {
      if (e.getKeyCode() == KeyEvent.VK_LEFT) {
        keyLeft = true;
        if (Settings.CAMERA_ROTATABLE.get(Settings.currentProfile)) e.consume();
      }
      if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
        keyRight = true;
        if (Settings.CAMERA_ROTATABLE.get(Settings.currentProfile)) e.consume();
      }
      if (e.getKeyCode() == KeyEvent.VK_UP) {
        keyUp = true;
        if (Settings.CAMERA_ZOOMABLE.get(Settings.currentProfile)) e.consume();
      }
      if (e.getKeyCode() == KeyEvent.VK_DOWN) {
        keyDown = true;
        if (Settings.CAMERA_ZOOMABLE.get(Settings.currentProfile)) e.consume();
      }

      keyShift = e.isShiftDown();
    }

    if (listener_key != null && !e.isConsumed()) {
      listener_key.keyPressed(e);
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    if (Replay.isRecording) {
      Replay.dumpKeyboardInput(
          e.getKeyCode(), Replay.KEYBOARD_RELEASED, e.getKeyChar(), e.getModifiers());
    }

    // Reset dialogue option
    if (dialogue_option >= 0 && !Replay.isPlaying) {
      dialogue_option = -1;
      e.consume();
    }

    if (e.getKeyCode() == KeyEvent.VK_TAB) e.consume();

    // Handle camera keys
    if (!e.isConsumed()) {
      if (e.getKeyCode() == KeyEvent.VK_LEFT) {
        keyLeft = false;
        if (Settings.CAMERA_ROTATABLE.get(Settings.currentProfile)) e.consume();
      }
      if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
        keyRight = false;
        if (Settings.CAMERA_ROTATABLE.get(Settings.currentProfile)) e.consume();
      }
      if (e.getKeyCode() == KeyEvent.VK_UP) {
        keyUp = false;
        if (Settings.CAMERA_ZOOMABLE.get(Settings.currentProfile)) e.consume();
      }
      if (e.getKeyCode() == KeyEvent.VK_DOWN) {
        keyDown = false;
        if (Settings.CAMERA_ZOOMABLE.get(Settings.currentProfile)) e.consume();
      }

      keyShift = e.isShiftDown();
    }

    if (listener_key != null && !e.isConsumed()) {
      listener_key.keyReleased(e);
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {
    if (Replay.isRecording) {
      Replay.dumpKeyboardInput(
          e.getKeyCode(), Replay.KEYBOARD_TYPED, e.getKeyChar(), e.getModifiers());
    }

    if (dialogue_option >= 0 && !Replay.isPlaying) e.consume();

    // Handle camera rotation keys
    if (!e.isConsumed()) {
      if (e.getKeyCode() == KeyEvent.VK_LEFT) {
        keyLeft = true;
        if (Settings.CAMERA_ROTATABLE.get(Settings.currentProfile)) e.consume();
      }
      if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
        keyRight = true;
        if (Settings.CAMERA_ROTATABLE.get(Settings.currentProfile)) e.consume();
      }
      if (e.getKeyCode() == KeyEvent.VK_UP) {
        keyUp = true;
        if (Settings.CAMERA_ZOOMABLE.get(Settings.currentProfile)) e.consume();
      }
      if (e.getKeyCode() == KeyEvent.VK_DOWN) {
        keyDown = true;
        if (Settings.CAMERA_ZOOMABLE.get(Settings.currentProfile)) e.consume();
      }

      keyShift = e.isShiftDown();
    }

    if (listener_key != null && !e.isConsumed()) {
      listener_key.keyTyped(e);
    }
  }
}
