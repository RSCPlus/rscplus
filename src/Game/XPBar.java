package Game;

import java.awt.Dimension;
import java.awt.Graphics2D;

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

		Renderer.drawShadowText(g, Client.skill_name[currentSkill], x + (bounds.width / 2), y + (bounds.height / 2) - 2,
					Renderer.color_text, true);

		Renderer.setAlpha(g, 1.0f);
	}

	public int currentSkill;

	private long m_timer;
}
