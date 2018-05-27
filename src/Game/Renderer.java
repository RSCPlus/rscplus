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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageConsumer;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import Client.NotificationsHandler;
import Client.NotificationsHandler.NotifType;
import Client.Settings;
import Client.Util;

/**
 * Handles rendering overlays and client adjustments based on window size
 */
public class Renderer {
	
	public static Object instance = null;
	
	public static int width;
	public static int height;
	public static int height_client;
	public static int[] pixels;
	
	public static int fps;
	public static float alpha_time;
	public static float delta_time;
	public static long time;
	
	public static ImageConsumer image_consumer = null;
	
	public static Color color_text = new Color(240, 240, 240);
	public static Color color_shadow = new Color(15, 15, 15);
	public static Color color_gray = new Color(60, 60, 60);
	public static Color color_hp = new Color(0, 210, 0);
	public static Color color_fatigue = new Color(210, 210, 0);
	public static Color color_prayer = new Color(160, 160, 210);
	public static Color color_low = new Color(255, 0, 0);
	public static Color color_item = new Color(245, 245, 245);
	public static Color color_item_highlighted = new Color(245, 196, 70);
	
	public static Image image_border;
	public static Image image_bar_frame;
	public static Image image_cursor;
	public static Image image_highlighted_item;
	private static BufferedImage game_image;
	
	private static Dimension new_size = new Dimension(0, 0);
	
	private static Item last_item;
	
	private static Font font_main;
	private static Font font_big;
	
	private static int frames = 0;
	private static long fps_timer = 0;
	private static boolean screenshot = false;
	
	public static String[] shellStrings;
	
	private static boolean macOS_resize_workaround = Util.isMacOS();
	
	public static void init() {
		// patch copyright to match current year
		shellStrings[23] = shellStrings[23].replaceAll("2015", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
		
		// Resize game window
		new_size.width = 512;
		new_size.height = 346;
		handle_resize();
		
		// Load fonts
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			InputStream is = Settings.getResourceAsStream("/assets/Helvetica-Bold.ttf");
			Font font = Font.createFont(Font.TRUETYPE_FONT, is);
			ge.registerFont(font);
			font_main = font.deriveFont(Font.PLAIN, 11.0f);
			font_big = font.deriveFont(Font.PLAIN, 22.0f);
			
			is = Settings.getResourceAsStream("/assets/TimesRoman.ttf");
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Load images
		try {
			image_border = ImageIO.read(Settings.getResource("/assets/border.png"));
			image_bar_frame = ImageIO.read(Settings.getResource("/assets/bar.png"));
			image_cursor = ImageIO.read(Settings.getResource("/assets/cursor.png"));
			image_highlighted_item = ImageIO.read(Settings.getResource("/assets/highlighted_item.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void resize(int w, int h) {
		new_size.width = w;
		new_size.height = h;
	}
	
	public static void handle_resize() {
		width = new_size.width;
		height = new_size.height;
		
		height_client = height - 12;
		pixels = new int[width * height];
		game_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Camera.resize();
		Menu.resize();
		
		if (image_consumer != null)
			image_consumer.setDimensions(width, height);
		
		if (Client.strings != null)
			Client.strings[262] = fixLengthString("~" + (Renderer.width - (512 - 439)) + "~@whi@Remove         WWWWWWWWWW");
		
		if (Renderer.instance != null && Reflection.setGameBounds != null) {
			try {
				Reflection.setGameBounds.invoke(Renderer.instance, 0, Renderer.width, Renderer.height, 0, (byte)119);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static int lastPercentHP = 100;
	private static int lastFatigue = 0;
	
	public static void present(Graphics g, Image image) {
		// Update timing
		long new_time = System.currentTimeMillis();
		delta_time = (float)(new_time - time) / 1000.0f;
		time = new_time;
		alpha_time = 0.25f + (((float)Math.sin(time / 100) + 1.0f) / 2.0f * 0.75f);
		
		// This workaround is required to use custom resolution on macOS
		if (macOS_resize_workaround) {
			if (Settings.CUSTOM_CLIENT_SIZE) {
				Game.getInstance().resizeFrameWithContents();
			} else {
				Game.getInstance().pack();
				Game.getInstance().setLocationRelativeTo(null);
			}
			macOS_resize_workaround = false;
		}
		
		// Run other parts update methods
		Client.update();
		
		Graphics2D g2 = (Graphics2D)game_image.getGraphics(); // TODO: Declare g2 outside of the present method
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(font_main);
		
		g2.drawImage(image, 0, 0, null);
		g2.drawImage(image_border, 512, height - 13, width - 512, 13, null);
		
		// In-game UI
		if (Client.state == Client.STATE_GAME) {
			if (!Client.isInterfaceOpen() && Client.show_menu == Client.MENU_NONE) {
				List<Rectangle> npc_hitbox = new ArrayList<>();
				List<Rectangle> player_hitbox = new ArrayList<>();
				List<Point> entity_text_loc = new ArrayList<>();
				
				float alphaHP = 1.0f;
				Color colorHP = color_hp;
				
				for (Iterator<NPC> iterator = Client.npc_list.iterator(); iterator.hasNext();) {
					NPC npc = iterator.next(); // TODO: Remove unnecessary allocations
					Color color = color_low;
					
					if (Client.player_name == null) {
						Client.getPlayerName();
					}
					
					// Update player coords
					if (npc != null && Client.player_name.equals(npc.name)) {
						Client.player_posX = npc.x;
						Client.player_posY = npc.y;
						Client.player_height = npc.height;
						Client.player_width = npc.width;
					}
					
					boolean show = false;
					
					if (npc.type == NPC.TYPE_PLAYER) {
						color = color_fatigue;
						
						if (Client.isFriend(npc.name) && (Settings.SHOW_FRIENDINFO || Settings.SHOW_PLAYERINFO)) {
							color = color_hp;
							show = true;
						} else if (Settings.SHOW_PLAYERINFO) {
							show = true;
						}
					} else if (npc.type == NPC.TYPE_MOB && Settings.SHOW_NPCINFO) {
						show = true;
					}
					
					if (Settings.SHOW_HITBOX) {
						List<Rectangle> hitbox = player_hitbox;
						boolean showHitbox = true;
						
						if (npc.type == NPC.TYPE_MOB)
							hitbox = npc_hitbox;
						
						for (Iterator<Rectangle> boxIterator = hitbox.iterator(); boxIterator.hasNext();) {
							Rectangle rect = boxIterator.next(); // TODO: Remove unnecessary allocations
							if (rect.x == npc.x && rect.y == npc.y && rect.width == npc.width && rect.height == npc.height) {
								showHitbox = false;
								break;
							}
						}
						
						if (showHitbox) {
							setAlpha(g2, 0.3f);
							g2.setColor(color);
							g2.fillRect(npc.x, npc.y, npc.width, npc.height);
							g2.setColor(Color.BLACK);
							g2.drawRect(npc.x, npc.y, npc.width, npc.height);
							setAlpha(g2, 1.0f);
							hitbox.add(new Rectangle(npc.x, npc.y, npc.width, npc.height));
						}
					}
					
					if (Settings.SHOW_STATUSDISPLAY && npc.name != null) {
						int x = npc.x + (npc.width / 2);
						int y = npc.y - 20;
						for (Iterator<Point> locIterator = entity_text_loc.iterator(); locIterator.hasNext();) {
							Point loc = locIterator.next(); // TODO: Remove unnecessary allocations
							if (loc.x == x && loc.y == y)
								y -= 12;
						}
						if (show) {
							drawShadowText(g2, npc.name, x, y, color, true);
							if (npc.currentHits != 0 && npc.maxHits != 0) {
								drawShadowText(g2, npc.currentHits + "/" + npc.maxHits, x, y - 12, color, true);
							}
						}
						entity_text_loc.add(new Point(x, y));
					}
				}
				
				List<Rectangle> item_hitbox = new ArrayList<>();
				List<Point> item_text_loc = new ArrayList<>();
				
				if (Settings.SHOW_ITEMINFO) { // Don't sort if we aren't displaying any item names anyway
					try {
						// Keep items in (technically reverse) alphabetical order for SHOW_ITEMINFO instead of randomly
						// changing places each frame
						Collections.sort(Client.item_list, new ItemComparator());
					} catch (Exception e) {
						// Sometimes Java helpfully complains that the sorting method violates its general contract.
						e.printStackTrace();
					}
				}
				
				for (Iterator<Item> iterator = Client.item_list.iterator(); iterator.hasNext();) {
					Item item = iterator.next(); // TODO: Remove unnecessary allocations
					
					if (Settings.SHOW_HITBOX) {
						boolean show = true;
						for (Iterator<Rectangle> boxIterator = item_hitbox.iterator(); boxIterator.hasNext();) {
							Rectangle rect = boxIterator.next(); // TODO: Remove unnecessary allocations
							if (rect.x == item.x && rect.y == item.y && rect.width == item.width && rect.height == item.height) {
								show = false;
								break;
							}
						}
						
						if (show) {
							setAlpha(g2, 0.3f);
							g2.setColor(color_prayer);
							g2.fillRect(item.x, item.y, item.width, item.height);
							g2.setColor(Color.BLACK);
							g2.drawRect(item.x, item.y, item.width, item.height);
							setAlpha(g2, 1.0f);
							item_hitbox.add(new Rectangle(item.x, item.y, item.width, item.height));
						}
					}
					
					if (Settings.SHOW_ITEMINFO) {
						int x = item.x + (item.width / 2);
						int y = item.y - 20;
						int freq = Collections.frequency(Client.item_list, item);

						// Check if item is in blocked list
						boolean itemIsBlocked = stringIsWithinList(item.getName(), Settings.BLOCKED_ITEMS);
						
						// We've sorted item list in such a way that it is possible to not draw the ITEMINFO unless it's
						// the first time we've tried to for this itemid at that location by just using last_item.
						// last_item == null necessary in case only one item on screen is being rendered. slight speed
						// increase from freq == 1 if compiler can stop early in conditional.
						if ((freq == 1 || !item.equals(last_item) || last_item == null) && !itemIsBlocked) {
							for (Iterator<Point> locIterator = item_text_loc.iterator(); locIterator.hasNext();) {
								Point loc = locIterator.next(); // TODO: Remove unnecessary allocations
								if (loc.x == x && loc.y == y) {
									y -= 12;
								}
							}
							item_text_loc.add(new Point(x, y));

							Color itemColor = color_item;
							String itemText = item.getName() + ((freq == 1) ? "" : " (" + freq + ")");

							// Check if item is in highlighted list
							if (stringIsWithinList(item.getName(), Settings.HIGHLIGHTED_ITEMS)) {
								itemColor = color_item_highlighted;
								drawHighlighImage(g2, itemText, x, y);
							}

							// TODO: it would be nice if for items like Coins or Runes, we showed how many of the item
							// were on the ground instead of how many times you have to click to pick them all up.
							// Currently will just show "Coins (2)" if there are two stacks of coins on the ground.
							drawShadowText(g2, itemText, x, y, itemColor, true);
						}
						last_item = item; // Done with item this loop, can save it as last_item
					}
				}
			}
			
			if (!Client.isSleeping()) {
				Client.updateCurrentFatigue();
			}
			
			if (!Client.isWelcomeScreen() && null == Client.player_name) {
				Client.getPlayerName();
			}
			
			// Clear item list for next frame
			Client.item_list.clear();
			last_item = null;
			
			if (!Client.show_sleeping && Settings.SHOW_INVCOUNT)
				drawShadowText(g2, Client.inventory_count + "/" + Client.max_inventory, width - 19, 17, color_text, true);
			
			int percentHP = 0;
			int percentPrayer = 0;
			float alphaHP = 1.0f;
			float alphaPrayer = 1.0f;
			float alphaFatigue = 1.0f;
			Color colorHP = color_hp;
			Color colorPrayer = color_prayer;
			Color colorFatigue = color_fatigue;
			
			if (Client.getBaseLevel(Client.SKILL_HP) > 0) {
				percentHP = Client.getCurrentLevel(Client.SKILL_HP) * 100 / Client.getBaseLevel(Client.SKILL_HP);
				percentPrayer = Client.getCurrentLevel(Client.SKILL_PRAYER) * 100 / Client.getBaseLevel(Client.SKILL_PRAYER);
			}
			
			if (percentHP < 30) {
				colorHP = color_low;
				alphaHP = alpha_time;
			}
			
			if (percentPrayer < 30) {
				colorPrayer = color_low;
				alphaPrayer = alpha_time;
			}
			
			if (Client.getFatigue() >= 80) {
				colorFatigue = color_low;
				alphaFatigue = alpha_time;
			}
			
			// Low HP notification
			if (percentHP <= Settings.LOW_HP_NOTIF_VALUE && lastPercentHP > percentHP && lastPercentHP > Settings.LOW_HP_NOTIF_VALUE)
				NotificationsHandler.notify(NotifType.LOWHP, "Low HP Notification", "Your HP is at " + percentHP + "%");
			lastPercentHP = percentHP;
			
			// High fatigue notification
			if (Client.getFatigue() >= Settings.FATIGUE_NOTIF_VALUE && lastFatigue < Client.getFatigue() && lastFatigue < Settings.FATIGUE_NOTIF_VALUE)
				NotificationsHandler.notify(NotifType.FATIGUE, "High Fatigue Notification", "Your fatigue is at " + Client.getFatigue() + "%");
			lastFatigue = Client.getFatigue();
			
			// Draw HP, Prayer, Fatigue overlay
			int x = 24;
			int y = 32;
			
			if (Client.isInCombat() || Settings.COMBAT_MENU) { // combat menu is showing, so move everything down
				y = 138;
			}
			if (Settings.SHOW_STATUSDISPLAY) {
				if (width < 800) {
					if (!Client.isInterfaceOpen() && !Client.show_questionmenu) {
						setAlpha(g2, alphaHP);
						drawShadowText(g2, "Hits: " + Client.current_level[Client.SKILL_HP] + "/" + Client.base_level[Client.SKILL_HP], x, y, colorHP, false);
						y += 16;
						setAlpha(g2, alphaPrayer);
						drawShadowText(g2, "Prayer: " + Client.current_level[Client.SKILL_PRAYER] + "/" + Client.base_level[Client.SKILL_PRAYER], x, y, colorPrayer, false);
						y += 16;
						setAlpha(g2, alphaFatigue);
						drawShadowText(g2, "Fatigue: " + Client.getFatigue() + "/100", x, y, colorFatigue, false);
						y += 16;
						setAlpha(g2, 1.0f);
					}
				} else {
					int barSize = 4 + image_bar_frame.getWidth(null);
					int x2 = width - (4 + barSize);
					int y2 = height - image_bar_frame.getHeight(null);
					
					drawBar(g2, image_bar_frame, x2, y2, colorFatigue, alphaFatigue, Client.getFatigue(), 100);
					x2 -= barSize;
					
					drawBar(g2, image_bar_frame, x2, y2, colorPrayer, alphaPrayer, Client.current_level[Client.SKILL_PRAYER], Client.base_level[Client.SKILL_PRAYER]);
					x2 -= barSize;
					
					drawBar(g2, image_bar_frame, x2, y2, colorHP, alphaHP, Client.current_level[Client.SKILL_HP], Client.base_level[Client.SKILL_HP]);
					x2 -= barSize;
				}
			}
			
			// Draw under combat style info
			if (!Client.isInterfaceOpen()) {
				if (time <= Client.magic_timer) {
					float timer = (float)Math.ceil((Client.magic_timer - time) / 1000.0);
					drawShadowText(g2, "Magic Timer: " + (int)timer, x, y, color_text, false);
					y += 14;
				}
				
				for (int i = 0; i < 18; i++) {
					if (Client.current_level[i] != Client.base_level[i] && (i != Client.SKILL_HP && i != Client.SKILL_PRAYER)) {
						int diff = Client.current_level[i] - Client.base_level[i];
						Color color = color_low;
						
						// Build our boost string
						// If the difference is greater than 0 (positive boost), we need to add a "+" to the string
						// Otherwise, it can be left alone because the integer will already have a "-" when converted to a string
						String boost = Integer.toString(diff);
						if (diff > 0) {
							boost = "+" + boost;
							color = color_hp;
						}
						
						drawShadowText(g2, boost, x, y, color, false);
						drawShadowText(g2, Client.skill_name[i], x + 32, y, color, false);
						y += 14;
					}
				}
			}
			
			// NPC Post-processing for ui
			if (Settings.SHOW_COMBAT_INFO && !Client.isInterfaceOpen()) {
				for (Iterator<NPC> iterator = Client.npc_list.iterator(); iterator.hasNext();) {
					NPC npc = iterator.next();
					if (npc != null && isInCombatWithNPC(npc)) {
						drawNPCBar(g2, 7, y, npc);
						// Increment y by npc bar height, so we can have multiple bars
						// NOTE: We should never (?) have more than one npc health bar, so multiple bars indicates
						// that our combat detection isn't accurate
						y += 50;
					}
				}
			}
			// Clear npc list for the next frame
			Client.npc_list.clear();
			
			Client.xpdrop_handler.draw(g2);
			Client.xpbar.draw(g2);
			
			if (Settings.DEBUG) {
				x = 32;
				y = 32;
				
				// Draw Skills
				for (int i = 0; i < 18; i++) {
					drawShadowText(g2, Client.skill_name[i] + " (" + i + "): " + Client.current_level[i] + "/" + Client.base_level[i] + " (" + Client.getXP(i) + " xp)", x, y,
							color_text, false);
					y += 16;
				}
				
				// Draw Fatigue
				y += 16;
				drawShadowText(g2, "Fatigue: " + ((float)Client.fatigue * 100.0f / 750.0f), x, y, color_text, false);
				y += 16;
				
				// Draw Mouse Info
				y += 16;
				drawShadowText(g2, "Mouse Position: " + MouseHandler.x + ", " + MouseHandler.y, x, y, color_text, false);
				y += 16;
				
				// Draw camera info
				y += 16;
				drawShadowText(g2, "Camera Rotation: " + Camera.rotation, x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "Camera Zoom: " + Camera.zoom, x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "Camera Distance1: " + Camera.distance1, x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "Camera Distance2: " + Camera.distance2, x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "Camera Distance3: " + Camera.distance3, x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "Camera Distance4: " + Camera.distance4, x, y, color_text, false);
				y += 16;
				
				x = 256;
				y = 32;
				drawShadowText(g2, "FPS: " + fps, x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "Game Size: " + width + "x" + height, x, y, color_text, false);
				y += 16;
				
				// Draw Inventory items
				y += 16;
				for (int i = 0; i < Client.inventory_count; i++) {
					drawShadowText(g2, "(" + i + "): " + Client.inventory_items[i], x, y, color_text, false);
					y += 16;
				}
				
				y += 16;
				drawShadowText(g2, "Menu: " + Client.show_menu, x, y, color_text, false);
				y += 16;
				
				x = 380;
				y = 32;
				if (Client.player_name != null)
					drawShadowText(g2, Client.player_name, x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "LocalRegion: (" + Client.localRegionX + "," + Client.localRegionY + ")", x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "Region: (" + Client.regionX + "," + Client.regionY + ")", x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "WorldCoord: (" + (Client.localRegionX + Client.regionX) + "," + (Client.localRegionY + Client.regionY) + ")", x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "Plane: (" + Client.planeWidth + "," + Client.planeHeight + "," + Client.planeIndex + ")", x, y, color_text, false);
				y += 16;
				drawShadowText(g2, "combat_timer: " + Client.combat_timer, x, y, color_text, false);
			}
			
			// drawShadowText(g2, "Test: " + Client.test, 100, 100, color_text, false);
			
			g2.setFont(font_big);
			if (Settings.FATIGUE_ALERT && Client.getFatigue() >= 98 && !Client.isInterfaceOpen()) {
				setAlpha(g2, alpha_time);
				drawShadowText(g2, "FATIGUED", width / 2, height / 2, color_low, true);
				setAlpha(g2, 1.0f);
			}
			if (Settings.INVENTORY_FULL_ALERT && Client.inventory_count >= 30 && !Client.isInterfaceOpen()) {
				setAlpha(g2, alpha_time);
				drawShadowText(g2, "INVENTORY FULL", width / 2, height / 2, color_low, true);
				setAlpha(g2, 1.0f);
			}
		} else if (Client.state == Client.STATE_LOGIN) {
			if (Settings.DEBUG)
				drawShadowText(g2, "DEBUG MODE", 38, 8, color_text, true);
			
			// Draw world list
			drawShadowText(g2, "World (Click to change): ", 80, height - 8, color_text, true);
			for (int i = 0; i < Settings.WORLD_LIST.length; i++) {
				Rectangle bounds = new Rectangle(152 + (i * 18), height - 12, 16, 12);
				Color color = color_text;
				
				if (i == Settings.WORLD - 1)
					color = color_low;
				
				setAlpha(g2, 0.5f);
				g2.setColor(color);
				g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
				setAlpha(g2, 1.0f);
				drawShadowText(g2, Settings.WORLD_LIST[i], bounds.x + (bounds.width / 2), bounds.y + 4, color_text, true);
				
				// Handle world selection click
				if (MouseHandler.x >= bounds.x && MouseHandler.x <= bounds.x + bounds.width && MouseHandler.y >= bounds.y && MouseHandler.y <= bounds.y + bounds.height
						&& MouseHandler.mouseClicked) {
					Game.getInstance().getJConfig().changeWorld(i + 1);
				}
			}
			
			drawShadowText(g2, "Populations", width - 67, 14, color_text, false);
			
			int worldPopArray[];
			int totalPop = 0;
			worldPopArray = Util.getPop();
			for (int i = 1; i < worldPopArray.length; i++) {
				drawShadowText(g2, "W" + i + " - " + worldPopArray[i], width - 56, 14 + (15 * i), color_text, false);
				totalPop += worldPopArray[i];
			}
			
			drawShadowText(g2, "There are currently " + totalPop + " players online.", width / 2, 8, color_text, true);
			
			// The RuneScape Classic servers will be taken offline at 8:00am BST on the 6th of August 2018.
			// https://services.runescape.com/m=news/runescape-classic-farewell?oldschool=1
			Calendar closeDate = new GregorianCalendar();
			closeDate.setTimeZone(TimeZone.getTimeZone("BST"));
			closeDate.set(2018, 7, 6, 8, 0, 0);
			Date currentDate = Calendar.getInstance().getTime();
			long timeRemaining = (closeDate.getTime().getTime() - 1000) - currentDate.getTime();
			long daysRemaining = timeRemaining / (1000 * 60 * 60 * 24);
			String daysString;
			if (timeRemaining < 0) {
				daysString = "RuneScape Classic has been taken offline";
			} else if (timeRemaining < (1000 * 60 * 60 * 24)) {
				daysString = "RuneScape Classic will be taken offline today";
			} else {
				daysString = "RuneScape Classic will be taken offline in " + (daysRemaining == 1 ? "1 day" : (daysRemaining + " days"));
			}
			drawShadowText(g2, daysString, width / 2, 24, Renderer.color_fatigue, true);
			
			// Draw version information
			drawShadowText(g2, "rscplus v" + String.format("%8.6f", Settings.VERSION_NUMBER), width - 164, height - 2, color_text, false);
		}
		
		// Draw software cursor
		if (Settings.SOFTWARE_CURSOR) {
			setAlpha(g2, 1.0f);
			g2.drawImage(image_cursor, MouseHandler.x, MouseHandler.y, null);
		}
		
		g2.dispose();
		
		// Right now is a good time to take a screenshot if one is requested
		if (screenshot) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
				String fname = Settings.Dir.SCREENSHOT + "/" + "Screenshot from " + format.format(new Date()) + ".png";
				ImageIO.write(game_image, "png", new File(fname));
				Client.displayMessage("@cya@Screenshot saved to '" + fname + "'", Client.CHAT_NONE);
			} catch (Exception e) {
			}
			screenshot = false;
		}
		
		g.drawImage(game_image, 0, 0, null);
		
		frames++;
		if (time > fps_timer) {
			fps = frames;
			frames = 0;
			fps_timer = time + 1000;
		}
		
		if (width != new_size.width || height != new_size.height)
			handle_resize();
		if (Settings.fovUpdateRequired) {
			Camera.setFoV(Settings.FOV);
			Settings.fovUpdateRequired = false;
		}
	}
	
	public static void drawBar(Graphics2D g, Image image, int x, int y, Color color, float alpha, int value, int total) {
		// Prevent divide by zero
		if (total == 0)
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
	
	public static void setAlpha(Graphics2D g, float alpha) {
		g.setComposite(AlphaComposite.SrcOver.derive(alpha));
	}

	public static boolean stringIsWithinList (String input, ArrayList<String> items) {
		if (items.size() <= 0) {
			return false;
		}
		Iterator it = items.iterator();
		while (it.hasNext()) {
			String item = String.valueOf(it.next());
			if (item.trim().length() > 0 && input.trim().toLowerCase().contains(item.trim().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static void drawHighlighImage(Graphics2D g, String text, int x, int y) {
		int correctedX = x;
		int correctedY = y;
		// Adjust for centering
		Dimension bounds = getStringBounds(g, text);
		correctedX -= (bounds.width / 2);
		correctedY += (bounds.height / 2);
		g.drawImage(image_highlighted_item, correctedX - 15, correctedY - 10, null);
	}

	public static void drawShadowText(Graphics2D g, String text, int x, int y, Color textColor, boolean center) {
		int textX = x;
		int textY = y;
		if (center) {
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
	
	public static void takeScreenshot() {
		screenshot = true;
	}
	
	private static String fixLengthString(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '~' && string.charAt(i + 4) == '~') {
				String coord = string.substring(i + 1, 3);
				string = string.replace(coord, "0" + coord);
			}
		}
		return string;
	}
	
	private static Dimension getStringBounds(Graphics2D g, String str) {
		FontRenderContext context = g.getFontRenderContext();
		Rectangle2D bounds = g.getFont().getStringBounds(str, context);
		return new Dimension((int)bounds.getWidth(), (int)bounds.getHeight());
	}
	
	private static boolean isInCombatWithNPC(NPC npc) {
		if (npc == null) {
			return false;
		}
		
		if (Client.player_name == null) {
			Client.getPlayerName();
		}
		
		int bottom_posY_npc = npc.y + npc.height;
		int bottom_posY_player = Client.player_posY + Client.player_height;
		
		// NPC's in combat with the player are always on the same bottom y coord, however
		// when moving the screen around they can be slightly off for a moment. To prevent
		// flickering, just give them a very small buffer of difference.
		boolean inCombatCandidate = (Math.abs(bottom_posY_npc - bottom_posY_player) < 5);
		
		// Hitboxes will intersect on the X axis from what I've tested, giving this a small
		// buffer as well just in case there are edge cases with very small monsters that
		// don't follow this pattern exactly.
		boolean hitboxesIntersectOnXAxis = (Client.player_posX - 10) < (npc.x + npc.width);
		
		// The NPC you're fighting is always on the left side of the player.
		boolean isOnLeftOfPlayer = Client.player_posX > npc.x;
		
		return Client.isInCombat() &&
				npc.currentHits != 0 &&
				npc.maxHits != 0 &&
				!Client.player_name.equals(npc.name) &&
				inCombatCandidate &&
				isOnLeftOfPlayer &&
				hitboxesIntersectOnXAxis;
	}
	
	private static void drawNPCBar(Graphics2D g, int x, int y, NPC npc) {
		Dimension bounds = new Dimension(173, 40);
		float hp_ratio = (float)(npc.currentHits) / (float)(npc.maxHits);
		
		// Container
		setAlpha(g, 0.5f);
		g.setColor(color_gray);
		g.fillRect(x - 1, y - 1, bounds.width + 2, bounds.height + 2);
		g.setColor(color_shadow);
		g.fillRect(x, y, bounds.width, bounds.height);
		
		// HP bar
		setAlpha(g, 1.0f);
		g.setColor(new Color(99, 20, 19));
		g.fillRect(x, y + 20, bounds.width, bounds.height / 2);
		g.setColor(new Color(10, 134, 51));
		g.fillRect(x, y + 20, (int)(bounds.width * hp_ratio), bounds.height / 2);
		
		// HP text
		if (Settings.USE_PERCENTAGE)
			drawShadowText(g, (int)Math.ceil(hp_ratio * 100) + "%", x + (bounds.width / 2), y + (bounds.height / 2) + 8, color_text, true);
		else
			drawShadowText(g, npc.currentHits + "/" + npc.maxHits, x + (bounds.width / 2), y + (bounds.height / 2) + 8, color_text, true);
		
		// NPC name
		drawShadowText(g, npc.name, x + (bounds.width / 2), y + (bounds.height / 2) - 12, color_text, true);
	}
	
}

class ItemComparator implements Comparator<Item> {
	
	@Override
	public int compare(Item a, Item b) {
		// this is reverse alphabetical order b/c we display them/in reverse order (y-=12 ea item)
		int offset = a.getName().compareToIgnoreCase(b.getName()) * -1;
		if (offset > 0) { // item a is alphabetically before item b
			offset = 10;
		} else if (offset < 0) { // item b is alphabetically before item a
			offset = -10;
			// items have the same name we would like to group items that are on the same tile as well, not just having
			// the same name, so that we can use "last_item" in a useful way
		} else {
			if (a.x == b.x && a.y == b.y) {
				offset = 0; // name is the same and so is location, items are considered equal
			} else {
				if (a.x < b.x) {
					offset = -5;
				} else {
					offset = 5;
				}
			}
		}
		return offset;
	}
}
