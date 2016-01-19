package Game;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XPDropHandler
{
	public void add(String text)
	{
		XPDrop xpdrop = new XPDrop(text);
		m_list.add(xpdrop);
	}

	public void draw(Graphics2D g)
	{
		for (Iterator<XPDrop> iterator = m_list.iterator(); iterator.hasNext();)
		{
			XPDrop xpdrop = iterator.next();
			xpdrop.process(g);
			if(xpdrop.y < 0)
				iterator.remove();
		}
	}

	class XPDrop
	{
		XPDrop(String text)
		{
			this.text = text;
			y = (float)Renderer.height / 4.0f;
			active = false;
		}

		public void process(Graphics2D g)
		{
			if(!active)
			{
				if(Renderer.time > m_timer)
				{
					m_timer = Renderer.time + 400;
					active = true;
				}
				else
				{
					return;
				}
			}

			Renderer.drawShadowText(g, text, Renderer.width / 2, (int)y, Renderer.color_text, true);
			y -= (float)Renderer.height / 12.0f * Renderer.delta_time;
		}

		private String text;
		private boolean active;
		public float y;
	}

	private long m_timer;
	private List<XPDrop> m_list = new ArrayList<XPDrop>();
}
