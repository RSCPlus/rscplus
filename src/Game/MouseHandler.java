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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Listens to mouse events and stores relevant information about them
 */
public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
	
	public static int x = 0;
	public static int y = 0;
	public static boolean mouseClicked = false;
	public static MouseListener listener_mouse = null;
	public static MouseMotionListener listener_mouse_motion = null;
	
	private boolean m_rotating = false;
	private Point m_rotatePosition;
	private float m_rotateX = 0.0f;
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (listener_mouse == null)
			return;
		
		x = e.getX();
		y = e.getY();
		listener_mouse.mouseClicked(e);
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		if (listener_mouse == null)
			return;
		
		x = e.getX();
		y = e.getY();
		listener_mouse.mouseEntered(e);
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		if (listener_mouse == null)
			return;
		
		x = e.getX();
		y = e.getY();
		listener_mouse.mouseExited(e);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (listener_mouse == null)
			return;
		
		if (e.getButton() == MouseEvent.BUTTON2) {
			m_rotating = true;
			m_rotatePosition = e.getPoint();
			e.consume();
		}
		
		if (!e.isConsumed()) {
			x = e.getX();
			y = e.getY();
			listener_mouse.mousePressed(e);
		}
		
		mouseClicked = true;
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (listener_mouse == null)
			return;
		
		if (e.getButton() == MouseEvent.BUTTON2) {
			m_rotating = false;
			e.consume();
		}
		
		if (!e.isConsumed()) {
			x = e.getX();
			y = e.getY();
			listener_mouse.mouseReleased(e);
		}
		
		mouseClicked = false;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (listener_mouse_motion == null)
			return;
		
		if (m_rotating) {
			m_rotateX += (float)(e.getX() - m_rotatePosition.x) / 2.0f;
			int xDist = (int)m_rotateX;
			
			Camera.addRotation(xDist);
			m_rotateX -= xDist;
			
			m_rotatePosition = e.getPoint();
		}
		
		if (!e.isConsumed()) {
			x = e.getX();
			y = e.getY();
			listener_mouse_motion.mouseDragged(e);
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		if (listener_mouse_motion == null)
			return;
		
		x = e.getX();
		y = e.getY();
		listener_mouse_motion.mouseMoved(e);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Camera.addZoom(e.getWheelRotation() * 16);
	}
	
}
