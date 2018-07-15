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

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import Client.Settings;

/**
 * Handles the rendering and behavior of XP drops
 */
public class XPDropHandler {
	
	private long m_timer;
	private List<XPDrop> m_list = new ArrayList<>();
	
	public void add(String text, Color color) {
		float y = (float)Renderer.height / 4.0f;
		
		float diff = 8.0f;
		for (int i = 0; i < m_list.size(); i++) {
			float indexDiff = m_list.get(i).y - y;
			if (indexDiff < diff) {
				y = m_list.get(i).y + 12.0f;
			}
		}
		
		XPDrop xpdrop = new XPDrop(text, color, y);
		m_list.add(xpdrop);
	}
	
	public void clear() {
		m_list.clear();
	}
	
	public void process() {
		for (Iterator<XPDrop> iterator = m_list.iterator(); iterator.hasNext();) {
			XPDrop xpdrop = iterator.next();
			xpdrop.process();
			if (xpdrop.y < 0 || xpdrop.y > Renderer.height || (Settings.SHOW_XP_BAR.get(Settings.currentProfile) && xpdrop.y <= XPBar.xp_bar_y + 5)) {
				iterator.remove();
			}
		}
	}
	
	public void draw(Graphics2D g) {
		for (Iterator<XPDrop> iterator = m_list.iterator(); iterator.hasNext();) {
			XPDrop xpdrop = iterator.next();
			xpdrop.draw(g);
		}
	}
	
	class XPDrop {
		
		XPDrop(String text, Color color, float y) {
			this.text = text;
			this.color = color;
			this.y = y;
			this.active = false;
		}
		
		public void process() {
			y -= (float)Renderer.height / 12.0f * Client.delta_time;
		}
		
		public void draw(Graphics2D g) {
			Renderer.drawShadowText(g, text, (XPBar.xp_bar_x + (XPBar.bounds.width / 2)), (int)y, this.color, true);
		}
		
		private String text;
		private Color color;
		private boolean active;
		public float y;
	}
	
}
