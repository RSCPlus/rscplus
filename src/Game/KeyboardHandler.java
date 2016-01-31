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

import Client.Logger;
import Client.Settings;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardHandler implements KeyListener
{
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(listener_key == null)
			return;

		if(e.isControlDown() || e.isAltDown())
		{
			command_key = e.getKeyCode();

			if(command_key == KeyEvent.VK_S)
				Renderer.takeScreenshot();

			if(command_key == KeyEvent.VK_R)
			{
				Settings.toggleHideRoofs();
				if(Settings.HIDE_ROOFS)
					Client.displayMessage("@cya@Roofs are now hidden", Client.CHAT_NONE);
				else
					Client.displayMessage("@cya@Roofs are now shown", Client.CHAT_NONE);
			}

			if(command_key == KeyEvent.VK_H)
			{
				Settings.toggleShowHitbox();
				if(Settings.SHOW_HITBOX)
					Client.displayMessage("@cya@Hitboxes are now shown", Client.CHAT_NONE);
				else
					Client.displayMessage("@cya@Hitboxes are now hidden", Client.CHAT_NONE);
			}

			if(command_key == KeyEvent.VK_C)
			{
				Settings.toggleCombatMenu();
				if(Settings.COMBAT_MENU)
					Client.displayMessage("@cya@Combat style is now shown", Client.CHAT_NONE);
				else
					Client.displayMessage("@cya@Combat style is now hidden", Client.CHAT_NONE);
			}

			if(command_key == KeyEvent.VK_D)
			{
				Settings.toggleDebug();
				if(Settings.DEBUG)
					Client.displayMessage("@cya@Debug mode is on", Client.CHAT_NONE);
				else
					Client.displayMessage("@cya@Debug mode is off", Client.CHAT_NONE);
			}

			if(command_key == KeyEvent.VK_F)
			{
				Settings.toggleFatigueAlert();
				if(Settings.FATIGUE_ALERT)
					Client.displayMessage("@cya@Fatigue alert is now on", Client.CHAT_NONE);
				else
					Client.displayMessage("@cya@Fatigue alert is now off", Client.CHAT_NONE);
			}

			if(command_key == KeyEvent.VK_T)
			{
				Settings.toggleTwitchHide();
				if(Settings.TWITCH_HIDE)
					Client.displayMessage("@cya@Twitch chat is now hidden", Client.CHAT_NONE);
				else
					Client.displayMessage("@cya@Twitch chat is now shown", Client.CHAT_NONE);
			}

			if(command_key == KeyEvent.VK_N)
			{
				Settings.toggleShowNPCInfo();
				if(Settings.SHOW_NPCINFO)
					Client.displayMessage("@cya@NPC info is now shown", Client.CHAT_NONE);
				else
					Client.displayMessage("@cya@NPC info is now hidden", Client.CHAT_NONE);
			}

			e.consume();
		}

		if(Client.show_questionmenu)
		{
			if(e.getKeyCode() == KeyEvent.VK_1)
				dialogue_option = 0;
			else if(e.getKeyCode() == KeyEvent.VK_2)
				dialogue_option = 1;
			else if(e.getKeyCode() == KeyEvent.VK_3)
				dialogue_option = 2;
			else if(e.getKeyCode() == KeyEvent.VK_4)
				dialogue_option = 3;
			else if(e.getKeyCode() == KeyEvent.VK_5)
				dialogue_option = 4;
			else if(e.getKeyCode() == KeyEvent.VK_6)
				dialogue_option = 5;
			else if(e.getKeyCode() == KeyEvent.VK_7)
				dialogue_option = 6;
			else if(e.getKeyCode() == KeyEvent.VK_8)
				dialogue_option = 7;
			else if(e.getKeyCode() == KeyEvent.VK_9)
				dialogue_option = 8;

			if(dialogue_option >= 0)
				e.consume();
		}

		if(!e.isConsumed())
		{
			Logger.Debug("Key Pressed: " + e.getKeyCode());
			listener_key.keyPressed(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if(listener_key == null)
			return;

		// Reset dialogue option
		if(dialogue_option >= 0)
		{
			dialogue_option = -1;
			e.consume();
		}

		if(isCommandKey(e))
			e.consume();

		if(!e.isConsumed())
		{
			Logger.Debug("Key Released: " + e.getKeyCode());
			listener_key.keyReleased(e);
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		if(listener_key == null)
			return;

		if(isCommandKey(e))
			e.consume();

		if(dialogue_option >= 0)
			e.consume();

		if(!e.isConsumed())
		{
			Logger.Debug("Key Typed: " + e.getKeyCode());
			listener_key.keyTyped(e);
		}
	}

	private static boolean isCommandKey(KeyEvent e)
	{
		return (e.isControlDown() || e.isAltDown() || e.getKeyCode() == KeyEvent.VK_ALT ||
			e.getKeyCode() == KeyEvent.VK_CONTROL || e.getKeyCode() == command_key);
	}

	private static int command_key = -1;

	public static int dialogue_option = -1;
	public static KeyListener listener_key;
}
