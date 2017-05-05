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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import Client.KeybindSet;
import Client.KeybindSet.KeyModifier;
import Client.Settings;

/**
 * Listens to keyboard events to trigger specified operations
 */
public class KeyboardHandler implements KeyListener {
	
	public static int dialogue_option = -1;
	public static KeyListener listener_key;
	
	/**
	 * ArrayList containing all registered KeybindSet values
	 */
	public static ArrayList<KeybindSet> keybindSetList = new ArrayList<KeybindSet>();
	
	/**
	 * Hashmap containing all default KeybindSet values. This is used in the ConfigWindow restore default keybinds
	 * method.
	 */
	public static HashMap<String, KeybindSet> defaultKeybindSetList = new HashMap<String, KeybindSet>();
	
	// TODO: Make spacebar clear the login message screen
	@Override
	public void keyPressed(KeyEvent e) {
		if (listener_key == null)
			return;
		
		if (e.isControlDown()) {
			for (KeybindSet kbs : keybindSetList) {
				if (kbs.getModifier() == KeyModifier.CTRL && e.getKeyCode() == kbs.getKey()) {
					Settings.processKeybindCommand(kbs.getCommandName());
					e.consume();
				}
			}
			
		} else if (e.isShiftDown()) {
			for (KeybindSet kbs : keybindSetList) {
				if (kbs.getModifier() == KeyModifier.SHIFT && e.getKeyCode() == kbs.getKey()) {
					Settings.processKeybindCommand(kbs.getCommandName());
					e.consume();
				}
			}
			
		} else if (e.isAltDown()) {
			for (KeybindSet kbs : keybindSetList) {
				if (kbs.getModifier() == KeyModifier.ALT && e.getKeyCode() == kbs.getKey()) {
					Settings.processKeybindCommand(kbs.getCommandName());
					e.consume();
				}
			}
			
		} else {
			for (KeybindSet kbs : keybindSetList) {
				if (kbs.getModifier() == KeyModifier.NONE && e.getKeyCode() == kbs.getKey()) {
					Settings.processKeybindCommand(kbs.getCommandName());
					e.consume();
				}
			}
		}
		
		if (Client.show_questionmenu && !e.isConsumed()) {
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
			
			if (dialogue_option >= 0)
				e.consume();
		}
		
		if (Client.state == Client.STATE_GAME && e.getKeyCode() == KeyEvent.VK_TAB && !Client.isInterfaceOpen()) {
			if (Client.lastpm_username != null) {
				Client.pm_text = "";
				Client.pm_enteredText = "";
				Client.pm_username = Client.lastpm_username;
				Client.show_friends = 2;
			}
			e.consume();
		}
		
		if (!e.isConsumed()) {
			listener_key.keyPressed(e);
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if (listener_key == null)
			return;
		
		// Reset dialogue option
		if (dialogue_option >= 0) {
			dialogue_option = -1;
			e.consume();
		}
		
		if (e.getKeyCode() == KeyEvent.VK_TAB)
			e.consume();
		
		if (!e.isConsumed()) {
			listener_key.keyReleased(e);
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		if (listener_key == null)
			return;
		
		if (dialogue_option >= 0)
			e.consume();
		
		if (!e.isConsumed()) {
			listener_key.keyTyped(e);
		}
	}
	
}
