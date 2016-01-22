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

import Client.Settings;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

public class Renderer
{
	public static void init()
	{
		width = Game.instance.getContentPane().getWidth();
		height = Game.instance.getContentPane().getHeight();
		height_client = height - 12;
		game_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Load fonts
		try
		{
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			InputStream is = Game.instance.getClass().getResourceAsStream("/assets/Helvetica-Bold.ttf");
			Font font = Font.createFont(Font.TRUETYPE_FONT, is);
			ge.registerFont(font);
			font_main = font.deriveFont(Font.PLAIN, 11.0f);
			font_big = font.deriveFont(Font.PLAIN, 22.0f);

			is = Game.instance.getClass().getResourceAsStream("/assets/TimesRoman.ttf");
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// Load images
		try
		{
			image_border = ImageIO.read(Game.instance.getClass().getResource("/assets/border.png"));
			image_bar_frame = ImageIO.read(Game.instance.getClass().getResource("/assets/bar.png"));
		}
		catch(Exception e) {}
	}

	public static void resize(int w, int h)
	{
	}

	public static void present(Graphics g, Image image)
	{
		// Update timing
		long new_time = System.currentTimeMillis();
		delta_time = (float)(new_time - time) / 1000.0f;
		time = new_time;
		alpha_time =  0.25f + (((float)Math.sin(time / 100) + 1.0f) / 2.0f * 0.75f);

		// Run other parts update methods
		Client.update();

		Graphics2D g2 = (Graphics2D)game_image.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(font_main);

		g2.drawImage(image, 0, 0, null);
		g2.drawImage(image_border, 512, height - 13, width - 512, 13, null);

		if(Client.state == Client.STATE_GAME)
		{
			if(width >= 800)
				drawLargeBars(g2);
			else
				drawSmallBars(g2);

			// TODO: Inventory max is hardcoded here, I think there's a variable somewhere
			// in client.class that contains the max inventory slots
			drawShadowText(g2, Client.inventory_count + "/" + 30, width - 19, 17, color_text, true);

			Client.xpdrop_handler.draw(g2);
			Client.xpbar.draw(g2);

			g2.setFont(font_big);
			if(Client.getFatigue() >= 100)
			{
				setAlpha(g2, alpha_time);
				drawShadowText(g2, "FATIGUED", width / 2, height / 2, color_low, true);
				setAlpha(g2, 1.0f);
			}
		}

		g2.dispose();

		g.drawImage(game_image, 0, 0, null);

		// Right now is a good time to take a screenshot
		if(screenshot)
		{
			try
			{
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
				ImageIO.write(game_image, "png", new File(Settings.Dir.SCREENSHOT + "/" + "Screenshot from " + format.format(new Date()) + ".png"));
			}
			catch(Exception e) {}
			screenshot = false;
		}

		frames++;
		if(time > fps_timer)
		{
			fps = frames;
			frames = 0;
			fps_timer = time + 1000;

			Game.instance.setTitle("FPS: " + fps);
		}
	}

	public static void drawBar(Graphics2D g, Image image, int x, int y, Color color, float alpha, int value, int total)
	{
		// Prevent divide by zero
		if(total == 0)
			return;

		int width = image.getWidth(null) - 2;
		int percent = value * width / total;

		g.setColor(color_shadow);
		g.fillRect(x + 1, y, width, image.getHeight(null));

		g.setColor(color);
		setAlpha(g, alpha);
		g.fillRect(x + 1, y, percent, image.getHeight(null));
		setAlpha(g, 1.0f);

		g.drawImage(image_bar_frame, x, y, null);
		drawShadowText(g, value + "/" + total, x + (image.getWidth(null) / 2), y + (image.getHeight(null) / 2) - 2, color_text, true);
	}

	public static void setAlpha(Graphics2D g, float alpha)
	{
		g.setComposite(AlphaComposite.SrcOver.derive(alpha));
	}

	public static void drawShadowText(Graphics2D g, String text, int x, int y, Color textColor, boolean center)
	{
		int textX = x;
		int textY = y;
		if(center)
		{
			Dimension bounds = getStringBounds(g, text);
			textX -= (bounds.width / 2);
			textY += (bounds.height / 2);
		}

		g.setColor(color_shadow);
		g.drawString(text, textX + 1, textY);
		g.drawString(text, textX - 1, textY);
		g.drawString(text, textX, textY + 1);
		g.drawString(text, textX, textY - 1);

		g.setColor(textColor);
		g.drawString(text, textX, textY);
	}

	public static void takeScreenshot()
	{
		screenshot = true;
	}

	private static void drawLargeBars(Graphics2D g2)
	{
		int barSize = 4 + image_bar_frame.getWidth(null);
		int x = width - (4 + barSize);
		int y = height - image_bar_frame.getHeight(null);

		int percentHP = 0;
		int percentPrayer = 0;

		if(Client.getBaseLevel(Client.SKILL_HP) > 0)
		{
			percentHP = Client.getLevel(Client.SKILL_HP) * 100 / Client.getBaseLevel(Client.SKILL_HP);
			percentPrayer = Client.getLevel(Client.SKILL_PRAYER) * 100 / Client.getBaseLevel(Client.SKILL_PRAYER);
		}

		if(Client.getFatigue() >= 80)
			drawBar(g2, image_bar_frame, x, y, color_low, alpha_time, Client.getFatigue(), 100);
		else
			drawBar(g2, image_bar_frame, x, y, color_fatigue, 1.0f, Client.getFatigue(), 100);
		x -= barSize;

		if(percentPrayer <= 30)
			drawBar(g2, image_bar_frame, x, y, color_low, alpha_time, Client.getLevel(Client.SKILL_PRAYER), Client.getBaseLevel(Client.SKILL_PRAYER));
		else
			drawBar(g2, image_bar_frame, x, y, color_prayer, 1.0f, Client.getLevel(Client.SKILL_PRAYER), Client.getBaseLevel(Client.SKILL_PRAYER));
		x -= barSize;

		if(percentHP <= 30)
			drawBar(g2, image_bar_frame, x, y, color_low, alpha_time, Client.getLevel(Client.SKILL_HP), Client.getBaseLevel(Client.SKILL_HP));
		else
			drawBar(g2, image_bar_frame, x, y, color_hp, 1.0f, Client.getLevel(Client.SKILL_HP), Client.getBaseLevel(Client.SKILL_HP));
		x -= barSize;
	}

	private static void drawSmallBars(Graphics2D g2)
	{
		// TODO: Draw smaller version of bars
	}

	private static Dimension getStringBounds(Graphics2D g, String str)
	{
		FontRenderContext context = g.getFontRenderContext();
		Rectangle2D bounds = g.getFont().getStringBounds(str, context);
		return new Dimension((int)bounds.getWidth(), (int)bounds.getHeight());
	}

	public static int width;
	public static int height;
	public static int height_client;
	public static int fps;
	public static float alpha_time;
	public static float delta_time;
	public static long time;

	private static Font font_main;
	private static Font font_big;

	private static int frames = 0;
	private static long fps_timer = 0;
	private static boolean screenshot = false;

	public static Color color_text = new Color(240, 240, 240);
	public static Color color_shadow = new Color(15, 15, 15);
	public static Color color_gray = new Color(60, 60, 60);
	public static Color color_hp = new Color(0, 210, 0);
	public static Color color_fatigue = new Color(210, 210, 0);
	public static Color color_prayer = new Color(0, 105, 210);
	public static Color color_low = new Color(255, 0, 0);

	public static Image image_border;
	public static Image image_bar_frame;
	private static BufferedImage game_image;
}
