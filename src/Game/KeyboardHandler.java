/**
 *	rscplus, RuneScape Classic injection client to enhance the game
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

public class KeyboardHandler implements KeyListener
{
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(listener_key == null)
			return;

		if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S)
		{
			Renderer.takeScreenshot();
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
			listener_key.keyPressed(e);
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

		if(!e.isConsumed())
			listener_key.keyReleased(e);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		if(listener_key == null)
			return;

		listener_key.keyTyped(e);
	}

	public static int dialogue_option = -1;
	public static KeyListener listener_key;
}
