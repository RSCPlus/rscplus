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
import Game.Game;
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
				Settings.toggleHideRoofs();

			if(command_key == KeyEvent.VK_H)
				Settings.toggleShowHitbox();

			if(command_key == KeyEvent.VK_C)
				Settings.toggleCombatMenu();

			if(command_key == KeyEvent.VK_D)
				Settings.toggleDebug();

			if(command_key == KeyEvent.VK_F)
				Settings.toggleFatigueAlert();

			if(command_key == KeyEvent.VK_T)
				Settings.toggleTwitchHide();

			if(command_key == KeyEvent.VK_I)
				Settings.toggleShowItemInfo();

			if(command_key == KeyEvent.VK_N)
				Settings.toggleShowNPCInfo();

			if(command_key == KeyEvent.VK_P)
				Settings.toggleShowPlayerInfo();
			
			if(command_key == KeyEvent.VK_OPEN_BRACKET)
				Settings.toggleXpDrops();
			
			if(command_key == KeyEvent.VK_CLOSE_BRACKET)
				Settings.toggleFatigueDrops();

			if(Client.state == Client.STATE_LOGIN)
			{
				int worldSwitch = 0;
				if(e.getKeyCode() == KeyEvent.VK_1)
					worldSwitch = 1;
				else if(e.getKeyCode() == KeyEvent.VK_2)
					worldSwitch = 2;
				else if(e.getKeyCode() == KeyEvent.VK_3)
					worldSwitch = 3;
				else if(e.getKeyCode() == KeyEvent.VK_4)
					worldSwitch = 4;
				else if(e.getKeyCode() == KeyEvent.VK_5)
					worldSwitch = 5;

				if(worldSwitch != 0)
					Game.getInstance().getJConfig().changeWorld(worldSwitch);
			}

			if(Client.state != Client.STATE_LOGIN)
			{
				if(e.getKeyCode() == KeyEvent.VK_L)
					Client.logout();
			}

			e.consume();
		}

		if(Client.show_questionmenu && !e.isConsumed())
		{
			if(e.getKeyCode() == KeyEvent.VK_1 || e.getKeyCode() == KeyEvent.VK_NUMPAD1)
				dialogue_option = 0;
			else if(e.getKeyCode() == KeyEvent.VK_2 || e.getKeyCode() == KeyEvent.VK_NUMPAD2)
				dialogue_option = 1;
			else if(e.getKeyCode() == KeyEvent.VK_3 || e.getKeyCode() == KeyEvent.VK_NUMPAD3)
				dialogue_option = 2;
			else if(e.getKeyCode() == KeyEvent.VK_4 || e.getKeyCode() == KeyEvent.VK_NUMPAD4)
				dialogue_option = 3;
			else if(e.getKeyCode() == KeyEvent.VK_5 || e.getKeyCode() == KeyEvent.VK_NUMPAD5)
				dialogue_option = 4;
			else if(e.getKeyCode() == KeyEvent.VK_6 || e.getKeyCode() == KeyEvent.VK_NUMPAD6)
				dialogue_option = 5;
			else if(e.getKeyCode() == KeyEvent.VK_7 || e.getKeyCode() == KeyEvent.VK_NUMPAD7)
				dialogue_option = 6;
			else if(e.getKeyCode() == KeyEvent.VK_8 || e.getKeyCode() == KeyEvent.VK_NUMPAD8)
				dialogue_option = 7;
			else if(e.getKeyCode() == KeyEvent.VK_9 || e.getKeyCode() == KeyEvent.VK_NUMPAD9)
				dialogue_option = 8;

			if(dialogue_option >= 0)
				e.consume();
		}

		if(Client.state == Client.STATE_GAME && e.getKeyCode() == KeyEvent.VK_TAB && !Client.isInterfaceOpen())
		{
			if(Client.lastpm_username != null)
			{
				Client.pm_text = "";
				Client.pm_enteredText = "";
				Client.pm_username = Client.lastpm_username;
				Client.show_friends = 2;
			}
			e.consume();
		}

		if(!e.isConsumed())
		{
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

		if(isCommandKey(e) || e.getKeyCode() == KeyEvent.VK_TAB)
			e.consume();

		if(!e.isConsumed())
		{
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
