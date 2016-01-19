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

public class Client
{
	public static void init()
	{
		for(int i = 0; i < strings.length; i++)
		{
			// Patch friend's list remove text coordinate
			if(strings[i].contains("~439~"))
			{
				strings[i] = strings[i].replace("~439~", "~" + (Renderer.width - (512 - 439)) + "~");
				strings[i] = fixLengthString(strings[i]);
			}
		}

		handler_mouse = new MouseHandler();
		handler_keyboard = new KeyboardHandler();
		Game.instance.applet.addMouseListener(handler_mouse);
		Game.instance.applet.addMouseMotionListener(handler_mouse);
		Game.instance.applet.addMouseWheelListener(handler_mouse);
		Game.instance.applet.addKeyListener(handler_keyboard);

		init_login();
	}

	public static void update()
	{
		if(mode == MODE_GAME)
		{
			// Process XP drops
			boolean dropXP = (xpdrop_state[SKILL_HP] > 0.0f);
			for(int i = 0; i < xpdrop_state.length; i++)
			{
				float xpGain = getXP(i) - xpdrop_state[i];
				xpdrop_state[i] += xpGain;

				if(xpGain > 0.0f && dropXP)
					xpdrop_handler.add("+" + xpGain + " (" + skill_name[i] + ")");
			}
		}
	}

	public static void init_login()
	{
		Camera.init();
		mode = MODE_LOGIN;
	}

	public static void init_game()
	{
		for(int i = 0; i < xpdrop_state.length; i++)
			xpdrop_state[i] = 0.0f;

		Camera.init();
		mode = MODE_GAME;
	}

	public static int getLevel(int i)
	{
		return current_level[i];
	}

	public static int getBaseLevel(int i)
	{
		return base_level[i];
	}

	public static float getXP(int i)
	{
		return (float)xp[i] / 4.0f;
	}

	public static int getFatigue()
	{
		return (fatigue * 100 / 750);
	}

	private static String fixLengthString(String string)
	{
		for(int i = 0; i < string.length(); i++)
		{
			if(string.charAt(i) == '~' && string.charAt(i + 4) == '~')
			{
				String coord = string.substring(i + 1, 3);
				string = string.replace(coord, "0" + coord);
			}
		}
		return string;
	}

	public static void test()
	{
		System.out.println("options");
	}

	public static final int SKILL_HP = 3;
	public static final int SKILL_PRAYER = 5;

	public static final int MODE_LOGIN = 1;
	public static final int MODE_GAME = 2;

	public static int mode = MODE_LOGIN;

	public static int inventory_count;

	public static boolean show_questionmenu;

	public static int fatigue;
	public static int current_level[];
	public static int base_level[];
	public static int xp[];
	public static String skill_name[];

	public static String strings[];

	public static XPDropHandler xpdrop_handler = new XPDropHandler();

	private static MouseHandler handler_mouse;
	private static KeyboardHandler handler_keyboard;
	private static float xpdrop_state[] = new float[18];
}
