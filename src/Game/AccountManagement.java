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

import java.net.Socket;
import Client.Logger;

public class AccountManagement {
	
	// § SECTION stream IO interaction §
	
	public static void register(String user, String pass) {
		if (Client.login_delay > 0) {
			Client.setLoginMessage("Connecting to server", "Please wait...");
			try {
				Client.shadowSleep(11200, 2000L);
			} catch (Exception e) {}
			Client.setLoginMessage("Please try again later", "Sorry! The server is currently full.");
		} else {
			try {
				Client.username_login = user;
				Client.password_login = pass;
				String formatPass = (String)Reflection.formatText.invoke(null, 20, (byte)-5, pass);
				Client.setLoginMessage("Connecting to server", "Please wait...");
				//int port = Client.autologin_timeout <= 1 ? Client.serverjag_port : Replay.connection_port;
				int port = Replay.connection_port;
				Socket connectionSocket = (Socket)Reflection.createSocket.invoke(Client.instance, -12, port, Client.server_address);
				Reflection.clientStreamField.set(Client.instance, Reflection.stream.newInstance(connectionSocket, Client.instance));
				Client.clientStream = Reflection.clientStreamField.get(Client.instance);
				Reflection.maxRetriesField.set(Client.clientStream, Client.maxRetries);
				byte limit30 = 0;
				String param = ((String)Reflection.getParameter.invoke(Client.instance, "limit30"));
				if (param != null && param.equals("1")) {
					limit30 = 1;
				}
				
				int[] keys = new int[]{
						(int)(9.9999999E7 * Math.random()),
						(int)(9.9999999E7 * Math.random()),
						(int)(9.9999999E7 * Math.random()),
						(int)(9.9999999E7 * Math.random())
				};
				Reflection.newPacket.invoke(Client.clientStream, 2, -12^-12);
				Object buffer = Reflection.bufferField.get(Client.clientStream);
				Reflection.putInt.invoke(buffer, -422797528, Client.version);
				Object block = Reflection.buffer.newInstance(500);
				Reflection.putByte.invoke(block, 11, -117); //register packet encrypted id
				Reflection.putInt.invoke(block, -422797528, keys[0]); // isaac/xtea keys
				Reflection.putInt.invoke(block, -422797528, keys[1]);
				Reflection.putInt.invoke(block, -422797528, keys[2]);
				Reflection.putInt.invoke(block, -422797528, keys[3]);
				Reflection.putStr.invoke(block, (byte)-39, formatPass); // password

                for (int var10 = 0; var10 < 5; ++var10) {
                	Reflection.putInt.invoke(block, -422797528, (int) (9.9999999E7D * Math.random())); // nonces
                }
                
                Reflection.putInt3Byte.invoke(block, (int) (9.9999999E7D * Math.random()), (byte)-13); //nonce
                Reflection.encrypt.invoke(block, Client.modulus, -118, Client.exponent); // encrypt the block
                Reflection.putBytes.invoke(buffer, 0, -123, (int)Reflection.bufferOffset.get(block), (byte[])Reflection.bufferByteArray.get(buffer)); // add the block
                Reflection.putShort.invoke(buffer, 393, 0); // xtea block offset holder
                int xtea_start = (int)Reflection.bufferOffset.get(buffer);
                Reflection.putByte.invoke(buffer, limit30, -12 + 50);
                Reflection.padBuffer.invoke(null, (int)22607, buffer);
                Reflection.putStr.invoke(buffer, (byte)-39, Client.username_login);
                Reflection.xteaEncrypt.invoke(buffer, (byte)87, xtea_start, (int[])keys, (int)Reflection.bufferOffset.get(buffer));
                Reflection.setBlockLength.invoke(buffer, (int)Reflection.bufferOffset.get(buffer) - xtea_start, 1);
                Reflection.flushPacket.invoke(Client.clientStream, -6924);
                Reflection.initIsaac.invoke(Client.clientStream, (byte)-119, (int[])keys);
                
                int response = (int)Reflection.readResponse.invoke(Client.clientStream, true);				
				Logger.Game("Newplayer response: " + response);
                
				if (response == 2) {
					AccountManagement.success_register();
				} else if (response == 3) {
					Client.setLoginMessage("Please choose another username", "Username already taken.");
				} else if (response == 4) {
					Client.setLoginMessage("Wait 60 seconds then retry", "That username is already in use.");
				} else if (response == 5) {
					Client.setLoginMessage("Please reload this page", "The client has been updated.");
				} else if (response == 6) {
					Client.setLoginMessage("Your ip-address is already in use", "You may only use 1 character at once.");
				} else if (response == 7) {
					Client.setLoginMessage("Please try again in 5 minutes", "Login attempts exceeded!");
				} else if (response == 11) {
					Client.setLoginMessage("for cheating or abuse", "Account has been temporarily disabled");
				} else if (response == 12) {
					Client.setLoginMessage("for cheating or abuse", "Account has been permanently disabled");
				} else if (response == 13) {
					Client.setLoginMessage("Please choose another username", "Username already taken.");
				} else if (response == 14) {
					Client.setLoginMessage("Please try again later", "Sorry! The server is currently full.");
					Client.login_delay = 1500;
				} else if (response == 15) {
					Client.setLoginMessage("to login to this server", "You need a members account");
				} else if (response == 16) {
					Client.setLoginMessage("to access member-only features", "Please login to a members server");
				} else {
					Client.setLoginMessage("Check your internet settings", "Sorry! Unable to connect to server.");
				}
			} catch (Exception e) {
				Logger.Game(String.valueOf(e));
				Client.setLoginMessage("Check your internet settings", "Sorry! Unable to connect to server.");
			}
		}
	}
	
	// § SECTION hook between screen and renderers §
	
	public static void success_register() {
		try {
			String user = Panel.getControlText(Client.panelRegister, Client.chooseUserInput);
			String pass = Panel.getControlText(Client.panelRegister, Client.choosePasswordInput);
			Client.login_screen = 2;
			Client.resetLoginMessage();
			Panel.setControlText(Client.panelLogin, Client.loginUserInput, user);
			Panel.setControlText(Client.panelLogin, Client.loginPassInput, pass);
			Reflection.preGameDisplay.invoke(Client.instance, 2540);
			Reflection.resetTimings.invoke(Client.instance, -28492);
			Client.login(false, user, pass);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void pregame_hook() {
		if (Reflection.drawPanel == null || Reflection.clearScreen == null) return;
				
		try {
			if (Client.login_screen == 1) {
				Reflection.clearScreen.invoke(Renderer.instance, true);
				Reflection.drawPanel.invoke(Client.panelRegister, (byte)39);
			} else if (Client.login_screen == 3) {
				Reflection.clearScreen.invoke(Renderer.instance, true);
			}
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public static void response_display_hook(String text1, String text2) {
		try {
			if (Client.login_screen == 1) {
				Panel.setControlText(Client.panelRegister, Client.controlRegister, text1 + " " + text2);
			}
			
			if (Client.login_screen == 3) {
				
			}
			
			// continue checking if == 2 (login screen) with existing code
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public static void welcome_new_user_hook() {
		try {
			if (Panel.isSelected(Client.panelWelcome, Client.registerButton)) {
				Client.login_screen = 1;
				Panel.setControlText(Client.panelRegister, Client.chooseUserInput, "");
				Panel.setControlText(Client.panelRegister, Client.choosePasswordInput, "");
				Panel.setControlText(Client.panelRegister, Client.chooseConfirmPassInput, "");
				Panel.setFocus(Client.panelRegister, Client.chooseUserInput);
				Panel.setControlToggled(Client.panelRegister, Client.acceptTermsCheckbox, 0);
				Panel.setControlText(Client.panelRegister, Client.controlRegister, "To create an account please enter all the requested details");
			}
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public static void panel_welcome_hook(int n) {
	    try {
	      Panel.addButtonBackTo(Client.panelWelcome, 86, 40 + 250, 100, 35);
	      Panel.addCenterTextTo(Client.panelWelcome, 86, 40 + 250, "Signup", 5, false);
	      Client.registerButton = Panel.addButtonTo(Client.panelWelcome, 86, 40 + 250, 100, 35);
	      
	      int var1 = 70;
	      Client.controlRegister = Panel.addCenterTextTo(Client.panelRegister, 256, var1 + 8, "To create an account please enter all the requested details", 4, true);
	      int var2 = var1 + 25;
	      Panel.addButtonBackTo(Client.panelRegister, 256, var2 + 17, 250, 34);
	      Panel.addCenterTextTo(Client.panelRegister, 256, var2 + 8, "Choose a Username", 4, false);
	      Client.chooseUserInput = Panel.addInputTo(Client.panelRegister, n, 256, var2 + 25, 200, 40, 4, 320, false, false);
	      Panel.setFocus(Client.panelRegister, Client.chooseUserInput);
	      var2 += 40;
	      Panel.addButtonBackTo(Client.panelRegister, 141, var2 + 17, 220, 34);
	      Panel.addCenterTextTo(Client.panelRegister, 141, var2 + 8, "Choose a Password", 4, false);
	      Client.choosePasswordInput = Panel.addInputTo(Client.panelRegister, n, 141, var2 + 25, 220, 40, 4, 20, true, false);
	      Panel.addButtonBackTo(Client.panelRegister, 371, var2 + 17, 220, 34);
	      Panel.addCenterTextTo(Client.panelRegister, 371, var2 + 8, "Confirm Password", 4, false);
	      Client.chooseConfirmPassInput = Panel.addInputTo(Client.panelRegister, n, 371, var2 + 25, 220, 40, 4, 20, true, false);
	      var2 += 40;
	      var2 += 20;
	      Client.acceptTermsCheckbox = Panel.addCheckboxTo(Client.panelRegister, 60, var2, 14);
	      Panel.addTextTo(Client.panelRegister, 75, var2, "I have read and agree to the terms+conditions listed at:", 4, true);
	      var2 += 15;
	      Panel.addCenterTextTo(Client.panelRegister, 256, var2, "http://www.runescape.com/runeterms.html", 4, true);
	      var2 += 20;
	      Panel.addButtonBackTo(Client.panelRegister, 156, var2 + 17, 150, 34);
	      Panel.addCenterTextTo(Client.panelRegister, 156, var2 + 17, "Submit", 5, false);
	      Client.chooseSubmitRegisterButton = Panel.addButtonTo(Client.panelRegister, 156, var2 + 17, 150, 34);
	      Panel.addButtonBackTo(Client.panelRegister, 356, var2 + 17, 150, 34);
	      Panel.addCenterTextTo(Client.panelRegister, 356, var2 + 17, "Cancel", 5, false);
	      Client.chooseCancelRegisterButton = Panel.addButtonTo(Client.panelRegister, 356, var2 + 17, 150, 34);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	// § SECTION hook key and mouse consumption §
	
	public static void account_panels_key_hook(int a, int key) {
		try {
			if (Client.login_screen == 1) {
				Panel.handleKey(Client.panelRegister, key);
			}
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	public static void account_panels_input_hook(int n1, int mouseY, int a, int n3, int mouseX) {
		try {
			if (Client.login_screen == 1) {
				Panel.handleMouse(Client.panelRegister, n1, mouseY, n3, mouseX);
				if (Panel.isSelected(Client.panelRegister, Client.chooseUserInput)) {
					Panel.setFocus(Client.panelRegister, Client.choosePasswordInput);
				}

				if (Panel.isSelected(Client.panelRegister, Client.choosePasswordInput)) {
					Panel.setFocus(Client.panelRegister, Client.chooseConfirmPassInput);
				}

				if (Panel.isSelected(Client.panelRegister, Client.chooseConfirmPassInput)) {
					Panel.setFocus(Client.panelRegister, Client.chooseUserInput);
				}

				if (Panel.isSelected(Client.panelRegister, Client.chooseCancelRegisterButton)) {
					Client.login_screen = 0;
				}
				
				if (Panel.isSelected(Client.panelRegister, Client.chooseSubmitRegisterButton)) {
					if (Panel.getControlText(Client.panelRegister, Client.chooseUserInput) != null && Panel.getControlText(Client.panelRegister, Client.chooseUserInput).length() != 0
						&& Panel.getControlText(Client.panelRegister, Client.choosePasswordInput) != null && Panel.getControlText(Client.panelRegister, Client.choosePasswordInput).length() != 0) {
						if (!Panel.getControlText(Client.panelRegister, Client.choosePasswordInput).equalsIgnoreCase(Panel.getControlText(Client.panelRegister, Client.chooseConfirmPassInput))) {
							Panel.setControlText(Client.panelRegister, Client.controlRegister, "@yel@The two passwords entered are not the same as each other!");
							return;
						}
						
						if (Panel.getControlText(Client.panelRegister, Client.choosePasswordInput).length() < 5) {
							Panel.setControlText(Client.panelRegister, Client.controlRegister, "@yel@Your password must be at least 5 letters long");
							return;
						}
												
						if (Panel.isToggle(Client.panelRegister, Client.acceptTermsCheckbox) == 0) {
							Panel.setControlText(Client.panelRegister, Client.controlRegister, "@yel@You must agree to the terms+conditions to continue");
							return;
						}
						
						Panel.setControlText(Client.panelRegister, Client.controlRegister, "Please wait... Creating new account");
						Reflection.preGameDisplay.invoke(Client.instance, 2540);
						Reflection.resetTimings.invoke(Client.instance, -28492);
						String user, pass;
						user = Panel.getControlText(Client.panelRegister, Client.chooseUserInput);
						pass = Panel.getControlText(Client.panelRegister, Client.choosePasswordInput);
						AccountManagement.register(user, pass);
						return;
					}
					Panel.setControlText(Client.panelRegister, Client.controlRegister, "@yel@Please fill in ALL requested information to continue!");
					return;
				}
			}
		} catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
}
