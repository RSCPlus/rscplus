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
import Client.Settings;

/**
 * Handles rendering the XP bar and hover information
 */
public class XPBar {
	
	public static Dimension bounds = new Dimension(110, 16);
	public static int xp_bar_x;
	// Don't need to set this more than once; we are always positioning the xp_bar to be vertically center aligned with
	// the Settings wrench.
	public static final int xp_bar_y = 20 - (bounds.height / 2);
	
	public static final int TIMER_LENGTH = 5000;
	public static final long TIMER_FADEOUT = 2000;
	
	public int current_skill;
	
	/**
	 * Keeps track of whether the XP bar from the last XP drop is still showing.
	 */
	private boolean last_timer_finished = false;
	private long m_timer;
	
	public XPBar() {
		current_skill = -1;
	}
	
	void setSkill(int skill) {
		current_skill = skill;
		last_timer_finished = m_timer - Renderer.time <= 0;
		m_timer = Renderer.time + TIMER_LENGTH;
	}
	
	/**
	 * Handles rendering the XP bar and hover information
	 * 
	 * @param g the Graphics2D object
	 */
	void draw(Graphics2D g) {
		if (Renderer.time > m_timer) {
			current_skill = -1;
			return;
		}
		
		if (current_skill == -1)
			return;
		
		long delta = m_timer - Renderer.time;
		
		float alpha = 1.0f;
		if (delta >= TIMER_LENGTH - 250 && last_timer_finished) // Don't fade in if XP bar is already displayed
			alpha = (float)(TIMER_LENGTH - delta) / 250.0f; // Fade in over 1/4th second
		else if (delta < TIMER_FADEOUT) // Less than TIMER_FADEOUT milliseconds left to display the XP bar
			alpha = (float)delta / TIMER_FADEOUT;
		
		int skill_current_xp = (int)Client.getXPforLevel(Client.getBaseLevel(current_skill));
		int skill_next_xp = (int)Client.getXPforLevel(Client.getBaseLevel(current_skill) + 1);
		int xp_until_level = (int)Client.getXPUntilLevel(current_skill); // TODO: Use this variable or remove it
		
		int xp = (int)Client.getXP(current_skill) - skill_current_xp;
		int xp_needed = skill_next_xp - skill_current_xp;
		
		// Draw bar
		
		// Check and set the appropriate display position
		if (Settings.CENTER_XPDROPS)
			xp_bar_x = (Renderer.width - bounds.width) / 2; // Position in the center
		else
			xp_bar_x = Renderer.width - 210 - bounds.width; // Position to the left of the Settings wrench
			
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
		if (MouseHandler.x >= x && MouseHandler.x <= x + bounds.width && MouseHandler.y > y && MouseHandler.y < y + bounds.height) {
			x = MouseHandler.x;
			y = MouseHandler.y + 16;
			g.setColor(Renderer.color_gray);
			Renderer.setAlpha(g, 0.5f);
			if (Client.getShowXpPerHour()[current_skill])
				g.fillRect(x - 100, y, 200, 60);
			else
				g.fillRect(x - 100, y, 200, 36);
			Renderer.setAlpha(g, 1.0f);
			
			y += 8;
			Renderer.drawShadowText(g, "XP: " + formatXP(Client.getXP(current_skill)), x, y, Renderer.color_text, true);
			y += 12;
			Renderer.drawShadowText(g, "XP until Level: " + formatXP(Client.getXPUntilLevel(current_skill)), x, y, Renderer.color_text, true);
			y += 12;
			if (Client.getShowXpPerHour()[current_skill]) {
				Renderer.drawShadowText(g, "XP/Hr: " + formatXP(Client.getXpPerHour()[current_skill]), x, y, Renderer.color_text, true);
				y += 12;
				Renderer.drawShadowText(g,
						"Actions until Level: "
								+ formatXP(Client.getXPUntilLevel(current_skill) / (Client.getLastXpGain()[current_skill][0] / (Client.getLastXpGain()[current_skill][3] + 1))),
						x, y, Renderer.color_text, true);
				y += 12;
			}
			
			// Don't allow XP bar to disappear while user is still interacting with it.
			if (delta < TIMER_FADEOUT + 100) {
				m_timer += TIMER_FADEOUT + 1500;
				last_timer_finished = false;
			}
		}
		
		Renderer.setAlpha(g, 1.0f);
	}
	
	/**
	 * Rounds up a double to to the nearest integer and adds commas, periods, etc. according to the local of the user
	 * 
	 * @param number the number to round
	 * @return a formatted version of the double as a String
	 */
	public static String formatXP(double number) {
		return NumberFormat.getIntegerInstance().format(Math.ceil(number));
	}
	
}
