/**
 * rscplus
 *
 * <p>This file is part of rscplus.
 *
 * <p>rscplus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscplus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscplus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/RSCPlus/rscplus>
 */
package Game;

/** Extends on the original Panel and makes user friendly existing methods used for new Panels */
public class Panel {
	
	public static int control_type;
	public static boolean control_shown;
	public static boolean control_clicked;
	public static int control_x;
	public static int control_y;
	public static int control_width;
	public static int control_height;
	public static int control_text_size;
	public static boolean control_use_alt_color;
	public static String control_text;
	
	public static void drawPanel(Object panelSource) {
		if (Reflection.drawPanel == null) return;

		try {
			Reflection.drawPanel.invoke(panelSource, (byte)39);
	    } catch (Exception e) {
	    }
	}
	
	/** rendering of new panel elements, use controlType 15 and onwards */
	public static void draw_extra_hook(Object panelSource, int controlId, int controlType) {
		int[] x, y, width, height;
		try {
			if (controlType == 15) {
				/** DEPRECTATED - mudclient already has built in rendering method for checkbox, using control type 14
				 * If mudclient didn't have a render method for checkbox the below would be needed.
				 * Instead can use as POC on how to render a new element if there is a need (though very unlikely since
				 * probably old panel element renderings from earlier mudclients are still there)
				 */
				// x = (int[]) Reflection.menuX.get(panelSource);
		        // y = (int[]) Reflection.menuY.get(panelSource);
		        // width = (int[]) Reflection.menuWidth.get(panelSource);
		        // height = (int[]) Reflection.menuHeight.get(panelSource);
				
		        // Panel.drawCheckBox(panelSource, controlId, x[controlId], y[controlId], width[controlId], height[controlId]);
			}
		} catch (Exception e) {
	      }
	}
	
	// obsolete, checkbox is control type 14 and mudclient kept the rendering method intact
	public static void drawCheckBox(Object panelSource, int id, int x, int y, int w, int h) {
		if (Reflection.menuRenderer == null || Reflection.drawBox == null 
				|| Reflection.drawLineHoriz == null || Reflection.drawLineVert == null) return;
		
		try {
			Object renderer = (Object) Reflection.menuRenderer.get(panelSource);
			int colorLeftRight = (int) Reflection.colorLeftRight.get(panelSource);
			int colorTopBottom = (int) Reflection.colorTopBottom.get(panelSource);
			Reflection.drawBox.invoke(renderer, x, (byte)-127, 16777215, y, h, w);
			Reflection.drawLineHoriz.invoke(renderer, w, colorTopBottom, x, y, (byte)-124);
			Reflection.drawLineVert.invoke(renderer, x, y, colorTopBottom, h, (byte)-124);
			Reflection.drawLineHoriz.invoke(renderer, w, colorLeftRight, x, y + h - 1, (byte)-124);
			Reflection.drawLineVert.invoke(renderer, x + w - 1, y, colorLeftRight, h, (byte)-124);
			
			int[] toggle = (int[]) Reflection.menuToggled.get(panelSource);
			
			if (toggle[id] == 1) {
				for (int i = 0; i < h; ++i) {
					Reflection.drawLineHoriz.invoke(renderer, 1, 0, x + i, y + i, (byte)-124);
					Reflection.drawLineHoriz.invoke(renderer, 1, 0, x + w - i - 1, y + i, (byte)-124);
				}
			}
		    } catch (Exception e) {
		    } 
	}
	
	public static void setFocus(Object panelSource, int controlId) {
		if (Reflection.setFocus == null) return;

		try {
	      Reflection.setFocus.invoke(panelSource, controlId, -105);
	    } catch (Exception e) {
	    }
	}
	
	public static void handleMouse(Object panelSource, int n1, int n2, int n3, int n4) {
		if (Reflection.handleMouse == null) return;

		try {
	      Reflection.handleMouse.invoke(panelSource, n1, n2, -9989, n3, n4);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public static void handleKey(Object panelSource, int n) {
		if (Reflection.handleKey == null) return;

		try {
	      Reflection.handleKey.invoke(panelSource, -12, n);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public static boolean isSelected(Object panelSource, int controlId) {
		if (Reflection.isSelected == null) return false;
		
		boolean selected = false;
		try {
			selected = (boolean)Reflection.isSelected.invoke(panelSource, (byte)-120, controlId);
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
		return selected;
	}
	
	public static int isToggle(Object panelSource, int controlId) {
		if (Reflection.isToggle == null) return 0;
		
		int toggle = 0;
		try {
			toggle = (int)Reflection.isToggle.invoke(panelSource, 14458, controlId);
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
		return toggle;
	}
	
	public static int addButtonBackTo(Object panelSource, int xPos, int yPos, int width, int height) {
		if (Reflection.addButtonBack == null) return -1;

	    int count = 0;
		try {
	      count = (int)Reflection.addButtonBack.invoke(panelSource, -120, width, height, xPos, yPos);
	    } catch (Exception e) {
	    }
		
		return count;
	}
	
	public static int addButtonTo(Object panelSource, int xPos, int yPos, int width, int height) {
		if (Reflection.addButton == null) return -1;

	    int count = 0;
		try {
	      count = (int)Reflection.addButton.invoke(panelSource, xPos, width, yPos, 91, height);
	    } catch (Exception e) {
	    }
		
		return count;
	}
	
	public static int addInputTo(Object panelSource, int n, int xPos, int yPos, int width, int height, int fontSize, int capacity, boolean isMasked, boolean isBackground) {
		if (Reflection.addInput == null) return -1;

	    int count = 0;
		try {
	      count = (int)Reflection.addInput.invoke(panelSource, n -3845, capacity, width, isBackground, yPos, fontSize, height, isMasked, xPos);
	    } catch (Exception e) {
	    }
		
		return count;
	}
	
	public static void setControlText(Object panelSource, int controlId, String text) {
		if (Reflection.setControlText == null) return;

		try {
			Reflection.setControlText.invoke(panelSource, controlId, text, 27642);
	    } catch (Exception e) {
	    }
		
	}
	
	public static String getControlText(Object panelSource, int controlId) {
		if (Reflection.getControlText == null) return null;

		String res = "";
		try {
			res = (String)Reflection.getControlText.invoke(panelSource, controlId, 4);
	    } catch (Exception e) {
	    }
		
		return res;
		
	}
	
	public static void setControlToggled(Object panelSource, int controlId, int toggle) {
		if (Reflection.menuToggled == null) return;

		try {
			int[] toggled = (int[]) Reflection.menuToggled.get(panelSource);
			toggled[controlId] = toggle;
	        Reflection.menuToggled.set(panelSource, toggled);
	    } catch (Exception e) {
	    }
	}
	
	public static int addCenterTextTo(Object panelSource, int xPos, int yPos, String textString, int fontSize, boolean isBackground) {
		if (Reflection.addCenterText == null) return -1;

	    int count = 0;
		try {
	      count = (int)Reflection.addCenterText.invoke(panelSource, isBackground, (byte)-126, fontSize, xPos, textString, yPos);
	    } catch (Exception e) {
	    }
		
		return count;
	}
	
	public static int addTextTo(Object panelSource, int xPos, int yPos, String textString, int fontSize, boolean isBackground) {
		int count = 0;
		try {
			count = (int) Reflection.menuCount.get(panelSource);
	        
			int[] type = (int[]) Reflection.menuType.get(panelSource);
	        type[count] = 0;
	        Reflection.menuType.set(panelSource, type);
	        
	        boolean[] shown = (boolean[]) Reflection.menuShown.get(panelSource);
	        shown[count] = true;
	        Reflection.menuShown.set(panelSource, shown);
	        
	        boolean[] clicked = (boolean[]) Reflection.menuClicked.get(panelSource);
	        clicked[count] = false;
	        Reflection.menuClicked.set(panelSource, clicked);
	        
	        int[] textSize = (int[]) Reflection.menuTextSize.get(panelSource);
	        textSize[count] = fontSize;
	        Reflection.menuTextSize.set(panelSource, textSize);
	        
	        boolean[] background = (boolean[]) Reflection.menuUseAltColor.get(panelSource);
	        background[count] = isBackground;
	        Reflection.menuUseAltColor.set(panelSource, background);
	        
	        int[] x = (int[]) Reflection.menuX.get(panelSource);
	        x[count] = xPos;
	        Reflection.menuX.set(panelSource, x);
	        
	        int[] y = (int[]) Reflection.menuY.get(panelSource);
	        y[count] = yPos;
	        Reflection.menuY.set(panelSource, y);
	        
	        String[] text = (String[]) Reflection.menuText.get(panelSource);
	        text[count] = textString;
	        Reflection.menuText.set(panelSource, text);
	        
	        count++;
	        Reflection.menuCount.set(panelSource, count);
	      } catch (Exception e) {
	      }
		return count;
	}
	
	public static int addCheckboxTo(Object panelSource, int xPos, int yPos, int size) {
		int count = 0;
		try {
			count = (int) Reflection.menuCount.get(panelSource);
	        
			int[] type = (int[]) Reflection.menuType.get(panelSource);
	        type[count] = 14;
	        Reflection.menuType.set(panelSource, type);
	        
	        boolean[] shown = (boolean[]) Reflection.menuShown.get(panelSource);
	        shown[count] = true;
	        Reflection.menuShown.set(panelSource, shown);
	        
	        boolean[] clicked = (boolean[]) Reflection.menuClicked.get(panelSource);
	        clicked[count] = false;
	        Reflection.menuClicked.set(panelSource, clicked);
	        
	        int[] x = (int[]) Reflection.menuX.get(panelSource);
	        x[count] = xPos - size / 2;
	        Reflection.menuX.set(panelSource, x);
	        
	        int[] y = (int[]) Reflection.menuY.get(panelSource);
	        y[count] = yPos - size / 2;
	        Reflection.menuY.set(panelSource, y);
	        
	        int[] width = (int[]) Reflection.menuWidth.get(panelSource);
	        width[count] = size;
	        Reflection.menuWidth.set(panelSource, width);
	        
	        int[] height = (int[]) Reflection.menuHeight.get(panelSource);
	        height[count] = size;
	        Reflection.menuHeight.set(panelSource, height);
	        
	        Reflection.menuCount.set(panelSource, count+1);
	      } catch (Exception e) {
	      }
		return count;
	}
	
}
