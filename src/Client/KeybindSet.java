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

package Client;

import java.awt.event.KeyEvent;

import javax.swing.JButton;

public class KeybindSet {
	
	public enum KeyModifier {
		NONE, CTRL, ALT, SHIFT
	}
	
	
	JButton button; //A reference to the JButton associated with this keybind
	String commandName; //A reference to the command associated with this keybind
	KeyModifier modifier; //A reference to the modifier key associated with this keybind, eg the control key.
	int key; //A reference to the actual key associated with this keybind, eg 'a'
	
	/**
	 * Creates a new KeybindSet object to store associated keybindings and their command values.
	 * @param button - The button associated with the keybind
	 * @param commandName - The command associated with the keybind. This should be a unique string with no spaces.
	 * @param modifier - The modifier key, a KeyModifier with a possible value of NONE, CTRL, ALT, or SHIFT
	 * @param key - The key associated with the keybind; does not include the modifier key.
	 */
	public KeybindSet(JButton button, String commandName, KeyModifier modifier, int key) {
		this.button = button;
		this.commandName = commandName;
		this.modifier = modifier;
		this.key = key;
	}

	public KeyModifier getModifier() {
		return modifier;
	}

	public void setModifier(KeyModifier modifier) {
		this.modifier = modifier;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public JButton getButton() {
		return button;
	}

	public String getCommandName() {
		return commandName;
	}
	
	public boolean isDuplicateKeybindSet(KeybindSet.KeyModifier modifier, int key) {
		return (this.modifier == modifier && this.key == key);
	}
	
    /**
     * Returns a human-friendly format of this KeybindSet's keybind, for use in buttons and printing.
     * @return - A string representing the keybind.
     */
    public String getFormattedKeybindText() {
        String modifierText = modifier.toString() + " + ";
        String keyText = KeyEvent.getKeyText(key);
        
        if (key == -1) 
            keyText = "NONE";
        
        if (modifier == KeyModifier.NONE)
            modifierText = "";
        
        if (keyText.equals("Open Bracket")) {
            keyText = "[";
        }
        if (keyText.equals("Close Bracket")) {
            keyText = "]";
        }
        if (keyText.equals("Unknown keyCode: 0x0")) {
            keyText = "???";
        }
        return modifierText + keyText;
    }

}
