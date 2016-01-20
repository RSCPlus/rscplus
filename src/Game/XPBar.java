package Game;

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
		if(delta >= 4750)
			m_alpha = (float)(5000 - delta) / 250.0f;
		else
			m_alpha = Math.min(1.0f, (float)delta / 2000.0f);

		int skillCurrentXP = (int)Client.getXPforLevel(Client.getBaseLevel(currentSkill));
		int skillNextXP = (int)Client.getXPforLevel(Client.getBaseLevel(currentSkill) + 1);

		int xp = (int)Client.getXP(currentSkill) - skillCurrentXP;
		int xpNeeded = skillNextXP - skillCurrentXP;

		int xpUntilLevel = (int)Client.getXPUntilLevel(currentSkill);

		Renderer.drawBarString(g, null, Renderer.width / 2,
				       24, Renderer.color_hp, m_alpha, xp, xpNeeded,
				       Client.skill_name[currentSkill]);
	}

	public int currentSkill;

	private long m_timer;
	private float m_alpha;
}
