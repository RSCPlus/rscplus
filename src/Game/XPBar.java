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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.text.NumberFormat;

public class XPBar
{
	public XPBar()
	{
		current_skill = -1;
	}

	void setSkill(int skill)
	{
		current_skill = skill;
		last_timer_finished = m_timer - Renderer.time <= 0;
		m_timer = Renderer.time + timer_length;
	}

	void draw(Graphics2D g)
	{
		if(Renderer.time > m_timer)
			current_skill = -1;
		
		if(current_skill == -1)
			return;
		
		long delta = m_timer - Renderer.time;

		float alpha = 1.0f;
		if(delta >= timer_length - 250 && last_timer_finished)
			alpha = (float)(timer_length - delta) / 250.0f; //fade in over 1/4th second
		else if(delta < timer_fadeout) //less than timer_fadeout milliseconds left to display the xpbar
			alpha = (float)delta / timer_fadeout;

		int skill_current_xp = (int)Client.getXPforLevel(Client.getBaseLevel(current_skill));
		int skill_next_xp = (int)Client.getXPforLevel(Client.getBaseLevel(current_skill) + 1);
		int xp_until_level = (int)Client.getXPUntilLevel(current_skill);

		int xp = (int)Client.getXP(current_skill) - skill_current_xp;
		int xp_needed = skill_next_xp - skill_current_xp;

		// Draw bar

		xp_bar_x = Renderer.width - 210 - bounds.width; //position to the left of the Settings wrench
		int percent = xp * (bounds.width - 2) / xp_needed;
		
		int x = xp_bar_x;
		int y = xp_bar_y;
		Renderer.setAlpha(g, alpha);
		g.setColor(Renderer.color_gray);
		g.fillRect(x - 1, y - 1, bounds.width + 2, bounds.height + 2);

		g.setColor(Renderer.color_shadow);
		g.fillRect(x, y, bounds.width, bounds.height);

		g.setColor(Renderer.color_hp);
		g.fillRect(x + 1, y + 1, percent, bounds.height - 2);

		Renderer.drawShadowText(g, Client.skill_name[current_skill] + " (" + Client.base_level[current_skill] + ")", x + (bounds.width / 2), y + (bounds.height / 2) - 2,
					Renderer.color_text, true);

		// Draw info box 
		if(MouseHandler.x >= x && MouseHandler.x <= x + bounds.width &&
		   MouseHandler.y > y && MouseHandler.y < y + bounds.height)
		{
			x = MouseHandler.x;
			y = MouseHandler.y + 16;
			g.setColor(Renderer.color_gray);
			Renderer.setAlpha(g, 0.5f);
			if(Client.showXpPerHour[current_skill])
				g.fillRect(x - 100, y, 200, 60);
			else
				g.fillRect(x - 100, y, 200, 36);
			Renderer.setAlpha(g, 1.0f);

			y += 8;
			Renderer.drawShadowText(g, "XP: " + formatXP(Client.getXP(current_skill)), x, y, Renderer.color_text, true); y += 12;
			Renderer.drawShadowText(g, "XP until Level: " + formatXP(Client.getXPUntilLevel(current_skill)), x, y, Renderer.color_text, true); y += 12;
			if(Client.showXpPerHour[current_skill]) {
				Renderer.drawShadowText(g, "XP/Hr: " + formatXP(Client.XpPerHour[current_skill]), x, y, Renderer.color_text, true); y += 12;
				Renderer.drawShadowText(g, "Actions until Level: " + formatXP( Client.getXPUntilLevel(current_skill) / (Client.lastXpGain[current_skill][0]/(Client.lastXpGain[current_skill][3] + 1)) ), x, y, Renderer.color_text, true); y += 12;
			}
			
			if(delta < timer_fadeout + 100) { //don't allow xpbar to disappear while user is still interacting with it.
				m_timer += timer_fadeout + 1500;
				last_timer_finished = false;
			}
		}

		Renderer.setAlpha(g, 1.0f);
	}
	
	public static String formatXP(double number) {
		return NumberFormat.getIntegerInstance().format(Math.ceil(number));
	}
	
	public static Dimension bounds = new Dimension(110, 16);
	public static int xp_bar_x;
	public static int xp_bar_y = 20 - (bounds.height / 2); //don't need to set this more than once; we are always positioning the xp_bar to be vertically center aligned with the Settings wrench.

	public int current_skill;
	public int timer_length = 5000;
	public long timer_fadeout = 2000;
	
	private boolean last_timer_finished = false; //don't fade in if xp bar is already displayed
	private long m_timer;
}
