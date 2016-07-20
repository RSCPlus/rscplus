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
		currentSkill = -1;
	}

	void setSkill(int skill)
	{
		currentSkill = skill;
		m_timer = Renderer.time + 5000;
	}

	void draw(Graphics2D g)
	{
		if(Renderer.time > m_timer)
			currentSkill = -1;
		if(currentSkill == -1)
			return;

		long delta = m_timer - Renderer.time;

		float alpha = 1.0f;
		if(delta >= 4750)
			alpha = (float)(5000 - delta) / 250.0f;
		else if(delta < 2000)
			alpha = (float)delta / 2000.0f;

		int skillCurrentXP = (int)Client.getXPforLevel(Client.getBaseLevel(currentSkill));
		int skillNextXP = (int)Client.getXPforLevel(Client.getBaseLevel(currentSkill) + 1);
		int xpUntilLevel = (int)Client.getXPUntilLevel(currentSkill);

		int xp = (int)Client.getXP(currentSkill) - skillCurrentXP;
		int xpNeeded = skillNextXP - skillCurrentXP;

		// Draw bar
		Dimension bounds = new Dimension(110, 16);
		int x = (Renderer.width / 2) - (bounds.width / 2);
		int y = 24 - (bounds.height / 2);
		int percent = xp * (bounds.width - 2) / xpNeeded;

		Renderer.setAlpha(g, alpha);
		g.setColor(Renderer.color_gray);
		g.fillRect(x - 1, y - 1, bounds.width + 2, bounds.height + 2);

		g.setColor(Renderer.color_shadow);
		g.fillRect(x, y, bounds.width, bounds.height);

		g.setColor(Renderer.color_hp);
		g.fillRect(x + 1, y + 1, percent, bounds.height - 2);

		Renderer.drawShadowText(g, Client.skill_name[currentSkill] + " (" + Client.base_level[currentSkill] + ")", x + (bounds.width / 2), y + (bounds.height / 2) - 2,
					Renderer.color_text, true);

		if(MouseHandler.x >= x && MouseHandler.x <= x + bounds.width &&
		   MouseHandler.y > y && MouseHandler.y < y + bounds.height)
		{
			x = MouseHandler.x;
			y = MouseHandler.y + 16;
			g.setColor(Renderer.color_gray);
			Renderer.setAlpha(g, 0.5f);
			if(Client.showXpPerHour[currentSkill])
				g.fillRect(x - 100, y, 200, 48);
			else
				g.fillRect(x - 100, y, 200, 36);
			Renderer.setAlpha(g, 1.0f);

			y += 8;
			Renderer.drawShadowText(g, "XP: " + formatXP(Client.getXP(currentSkill)), x, y, Renderer.color_text, true); y += 12;
			Renderer.drawShadowText(g, "XP to Level: " + formatXP(Client.getXPUntilLevel(currentSkill)), x, y, Renderer.color_text, true); y += 12;
			if(Client.showXpPerHour[currentSkill])
				Renderer.drawShadowText(g, "XP/Hr: " + formatXP(Client.XpPerHour[currentSkill]), x, y, Renderer.color_text, true); y += 12;
		}

		Renderer.setAlpha(g, 1.0f);
	}
	
	public static String formatXP(double number) {
		return NumberFormat.getIntegerInstance().format(Math.round(number));
	}

	public int currentSkill;

	private long m_timer;
}
