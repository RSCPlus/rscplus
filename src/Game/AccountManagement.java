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

import java.math.BigInteger;
import Client.CRC16;
import Client.Logger;
import Client.Settings;
import Client.Util;

public class AccountManagement {

  public static int panelPasswordChangeMode;
  public static String oldPassword;
  public static String newPassword;
  public static int customQuestionEntry = -1;
  
  public static String recoveryQuestions[] = new String[]{"Where were you born?", "What was your first teachers name?", "What is your fathers middle name?", "Who was your first best friend?", "What is your favourite vacation spot?", "What is your mothers middle name?", "What was your first pets name?", "What was the name of your first school?", "What is your mothers maiden name?", "Who was your first boyfriend/girlfriend?", "What was the first computer game you purchased?", "Who is your favourite actor/actress?", "Who is your favourite author?", "Who is your favourite musician?", "Who is your favourite cartoon character?", "What is your favourite book?", "What is your favourite food?", "What is your favourite movie?"};
  public static int recoveryIndices[] = new int[] { 0, 1, 2, 3, 4 };


  // § SECTION stream IO interaction §

  /** Intended to be RSC127 compatible */
  public static void register(String user, String pass) {
    if (Client.login_delay > 0) {
      Client.setResponseMessage("Connecting to server", "Please wait...");
      try {
        Client.shadowSleep(11200, 2000L);
      } catch (Exception e) {
      }
      Client.setResponseMessage("Please try again later", "Sorry! The server is currently full.");
    } else {
      try {
        Client.username_login = user;
        Client.password_login = pass;
        String formatPass = Client.formatText(pass, 20);
        Client.setResponseMessage("Connecting to server", "Please wait...");
        // int port = Client.autologin_timeout <= 1 ? Client.serverjag_port :
        // Replay.connection_port;
        int port = Replay.connection_port;
        StreamUtil.initializeStream(Client.server_address, port);
        StreamUtil.setStreamMaxRetries(Client.maxRetries);

        Client.session_id = 0; // TODO: should read int to get session ID here, triggered by TCP handshake

        StreamUtil.newPacket(2);
        Object buffer = StreamUtil.getStreamBuffer();
        StreamUtil.putShortTo(buffer, (short) (Client.version & 0xFFFF));

        // Put Username
        long formatUser = Util.username2hash(Client.username_login);
        StreamUtil.putLongTo(buffer, formatUser);
        
        /** If we wanted a RSC175 would need to get added just below a putShort based on refererid / limit30 */

        // Put Password
        enc_cred_put(buffer, formatPass, Client.session_id);

        // In 235 putRandom of "random.dat" is 24 bytes, but for 127 is expected 4 bytes.
        Object randBlock = StreamUtil.getNewBuffer(24);
        StreamUtil.putRandom(randBlock);
        byte[] randArr = StreamUtil.getBufferByteArray(randBlock);
        CRC16 sum = new CRC16();
        sum.update(randArr);
        int rand = (int) sum.getValue();
        StreamUtil.putIntTo(buffer, rand);

        StreamUtil.flushPacket();

        StreamUtil.readStream(); // Unknown what data this contained, client doesn't use it.
        int response = StreamUtil.readStream();
        Logger.Game("Newplayer response: " + response);

        if (response == 2) {
          AccountManagement.success_register();
        } else if (response == 3) {
          Client.setResponseMessage("Please choose another username", "Username already taken.");
        } else if (response == 4) {
          Client.setResponseMessage("Wait 60 seconds then retry", "That username is already in use.");
        } else if (response == 5) {
          Client.setResponseMessage("Please reload this page", "The client has been updated.");
        } else if (response == 6) {
          Client.setResponseMessage(
              "Your ip-address is already in use", "You may only use 1 character at once.");
        } else if (response == 7) {
          Client.setResponseMessage("Please try again in 5 minutes", "Login attempts exceeded!");
        } else if (response == 11) {
          Client.setResponseMessage("for cheating or abuse", "Account has been temporarily disabled");
        } else if (response == 12) {
          Client.setResponseMessage("for cheating or abuse", "Account has been permanently disabled");
        } else if (response == 13) {
          Client.setResponseMessage("Please choose another username", "Username already taken.");
        } else if (response == 14) {
          Client.setResponseMessage("Please try again later", "Sorry! The server is currently full.");
          Client.login_delay = 1500;
        } else if (response == 15) {
          Client.setResponseMessage("to login to this server", "You need a members account");
        } else if (response == 16) {
          Client.setResponseMessage(
              "to access member-only features", "Please login to a members server");
        } else {
          Client.setResponseMessage(
              "Check your internet settings", "Sorry! Unable to connect to server.");
        }
      } catch (Exception e) {
        Logger.Game(String.valueOf(e));
        Client.setResponseMessage(
            "Check your internet settings", "Sorry! Unable to connect to server.");
      }
    }
  }
  
  /** Intended to be RSC175 compatible. It keeps block of reading questions generated from RSC127 ("~:")
    * In order to render them "properly" */
  public static void forgotPass(String user) {
	  Client.setResponseMessage("Connecting to server", "Please wait...");

		try {
			int port = Replay.connection_port;
	        StreamUtil.initializeStream(Client.server_address, port);
	        StreamUtil.setStreamMaxRetries(Client.maxRetries);

	        Client.session_id = 0; // TODO: should read int to get session ID here, triggered by TCP handshake
	        
	        StreamUtil.newPacket(4);
	        Object buffer = StreamUtil.getStreamBuffer();
	        StreamUtil.putLongTo(buffer, Util.username2hash(user));
	        StreamUtil.flushPacket();
	        
			StreamUtil.readShort(); // Unknown what data this contained, client doesn't use it.
			int response = StreamUtil.readStream();
			Logger.Game("Getpq response: " + response);
			if (response == 0) {
				Client.setResponseMessage("", "Sorry, the recovery questions for this user have not been set");
				return;
			}

			String question;
			int idx;
			for (int i = 0; i < 5; ++i) {
				int length = StreamUtil.readStream();
                byte[] arr = new byte[5000];
                StreamUtil.readBytes(arr, length);
                question = new String(arr, 0, length);
                
             // This 'if' section is for parsing questions set in RSC127
                if (question.startsWith("~:")) {
                	question = question.substring(2);
					idx = 0;

					try {
						idx = Integer.parseInt(question);
					} catch (Exception ex) { }

					question = recoveryQuestions[idx];
				}
                
                Panel.setControlText(Client.panelRecovery, Client.controlRecoveryQuestion[i], question);
			}

			if (Client.failedRecovery) {
				Client.setResponseMessage("", "Sorry, you have already attempted 1 recovery, try again later");
				return;
			}

			Client.login_screen = Client.SCREEN_PASSWORD_RECOVERY;
			Panel.setControlText(Client.panelRecovery, Client.controlRecoveryTop,
					"@yel@To prove this is your account please provide the answers to");
			Panel.setControlText(Client.panelRecovery, Client.controlRecoveryBottom,
					"@yel@your security questions. You will then be able to reset your password");

			for (int i = 0; i < 5; ++i) {
				Panel.setControlText(Client.panelRecovery, Client.controlRecoveryInput[i], "");
			}

			Panel.setControlText(Client.panelRecovery, Client.recoverOldPassInput, "");
			Panel.setControlText(Client.panelRecovery, Client.recoverNewPassInput, "");
			Panel.setControlText(Client.panelRecovery, Client.recoverConfirmPassInput, "");
			return;
		} catch (Exception e) {
			Client.setResponseMessage("Check your internet settings", "Sorry! Unable to connect to server.");
			return;
		}
  }
  
  /** Intended to be RSC175 compatible. Different from RSC127 */
  public static void recover() {
	  Client.setResponseMessage("Connecting to server", "Please wait...");

		try {
			int port = Replay.connection_port;
	        StreamUtil.initializeStream(Client.server_address, port);
	        StreamUtil.setStreamMaxRetries(Client.maxRetries);
	        
	        Client.session_id = 0; // TODO: should read int to get session ID here, triggered by TCP handshake
	        
			String oldPass = Client.formatText(Panel.getControlText(Client.panelRecovery, Client.recoverOldPassInput), 20);
			String newPass = Client.formatText(Panel.getControlText(Client.panelRecovery, Client.recoverNewPassInput), 20);
			
			StreamUtil.newPacket(8);
	        Object buffer = StreamUtil.getStreamBuffer();
	        StreamUtil.putLongTo(buffer, Util.username2hash(Panel.getControlText(Client.panelLogin, Client.loginUserInput)));
			
			// In 235 putRandom of "random.dat" is 24 bytes, but for 127 is expected 4 bytes.
	        Object randBlock = StreamUtil.getNewBuffer(24);
	        StreamUtil.putRandom(randBlock);
	        byte[] randArr = StreamUtil.getBufferByteArray(randBlock);
	        CRC16 sum = new CRC16();
	        sum.update(randArr);
	        int rand = (int) sum.getValue();
	        StreamUtil.putIntTo(buffer, rand);
			
	        enc_cred_put(buffer, oldPass + newPass, Client.session_id);
	        
	        for (int i=0; i < 5; ++i) {
	        	/** In RSC127 it would have sent an encrypted "hash" like of answers, but seems in later revisions
	          	 * incl. up to RSC175 was changed to just send the encrypted answers, probably due to collisions
	          	 * in RSC127 hasher function */
	        	String answer = Panel.getControlText(Client.panelRecovery, Client.controlRecoveryInput[i]);
	        	answer = Util.formatString(answer, 50);
	        	StreamUtil.putByteTo(buffer, (byte)answer.length());
	            enc_cred_put(buffer, answer, Client.session_id);
	        }

			StreamUtil.flushPacket();
			
			StreamUtil.readStream(); // Unknown what data this contained, client doesn't use it.
			int response = StreamUtil.readStream();
			Logger.Game("Recover response: " + response);
			if (response == 0) {
				Client.login_screen = Client.SCREEN_USERNAME_PASSWORD_LOGIN;
				Client.setResponseMessage("", "Sorry, recovery failed. You may try again in 1 hour");
				Client.failedRecovery = true;
				return;
			}

			if (response == 1) {
				Client.login_screen = Client.SCREEN_USERNAME_PASSWORD_LOGIN;
				Client.setResponseMessage("", "Your pass has been reset. You may now use the new pass to login");
				return;
			}

			Client.login_screen = Client.SCREEN_USERNAME_PASSWORD_LOGIN;
			Client.setResponseMessage("", "Recovery failed! Attempts exceeded?");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			Client.setResponseMessage("Check your internet settings", "Sorry! Unable to connect to server.");
		}
  }
  
  /** Intended to be RSC175 compatible. Can be also used under RSC127 */
  public static void sendPassChange(String oldPwd, String newPwd) {
	  String oldPass = Client.formatText(oldPwd, 20); 
	  String newPass = Client.formatText(newPwd, 20);
	  
	  StreamUtil.newPacket(25);
	  
	  Object buffer = StreamUtil.getStreamBuffer();
	  enc_cred_put(buffer, oldPass + newPass, Client.session_id);
	  
	  StreamUtil.sendPacket();
  }
  
  /** Intended to be RSC175 compatible. Different from RSC127 */
  public static void sendRecoveryQuestions() {
	  StreamUtil.newPacket(208);
      Object buffer = StreamUtil.getStreamBuffer();

		for (int idx = 0; idx < 5; ++idx) {
			String question = Client.controlRecoveryText[idx];
			if (question == null || question.length() == 0) {
              question = String.valueOf(idx + 1);
          }
          if (question.length() > 50) {
              question = question.substring(0, 50);
          }
          String answer = Panel.getControlText(Client.panelRecoveryQuestions, Client.controlAnswerInput[idx]);
          answer = Util.formatString(answer, 50);
          
          StreamUtil.putByteTo(buffer, (byte)question.length());
          StreamUtil.putRegStrTo(buffer, question);
          /** In RSC127 it would have sent an encrypted "hash" like of answers, but seems in later revisions
      	 * incl. up to RSC175 was changed to just send the encrypted answers, probably due to collisions
      	 * in RSC127 hasher function */
          StreamUtil.putByteTo(buffer, (byte)answer.length());
          enc_cred_put(buffer, answer, Client.session_id);
		}

		StreamUtil.sendPacket();
  }
  
  /** Intended to be RSC175 compatible. Not present in RSC127 */
  public static void sendContactDetails(String fullName, String zipCode, String country, String email) {
	  StreamUtil.newPacket(253);
      Object buffer = StreamUtil.getStreamBuffer();
      
      StreamUtil.putByteTo(buffer, (byte)fullName.length());
      StreamUtil.putRegStrTo(buffer, fullName);
      StreamUtil.putByteTo(buffer, (byte)zipCode.length());
      StreamUtil.putRegStrTo(buffer, zipCode);
      StreamUtil.putByteTo(buffer, (byte)country.length());
      StreamUtil.putRegStrTo(buffer, country);
      StreamUtil.putByteTo(buffer, (byte)email.length());
      StreamUtil.putRegStrTo(buffer, email);
      
      StreamUtil.sendPacket();
  }
  
  public static boolean processPacket(int opcode, int psize) {
	  boolean processed = false;
	  if (Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile) || (Replay.isPlaying || Replay.isSeeking || Replay.isRestarting)) {
		  if (opcode == 224) { // Opcode is intended to be RSC175 compatible. The RSC127 would have appended "~:" to recoveryQuestions[recoveryIndices[idx]] and adapted accordingly the control text
			  Client.showRecoveryQuestions = true;
			  for (int questionIndex = 0; questionIndex < 5; ++questionIndex) {
					recoveryIndices[questionIndex] = questionIndex;
	                Client.controlRecoveryText[questionIndex] = recoveryQuestions[recoveryIndices[questionIndex]];
	                Panel.setControlText(
				            Client.panelRecoveryQuestions,Client.controlAnswerInput[questionIndex], "");
	                Panel.setControlText(
				            Client.panelRecoveryQuestions,Client.controlRecoveryIns[questionIndex], questionIndex + 1 + ": " + Client.controlRecoveryText[questionIndex]);
				}
			  processed = true;
		  } else if (opcode == 232) { //Opcode is intended to be RSC175 compatible. Not present in RSC127
			  Client.showContactDetails = true;
			  Panel.setControlText(Client.controlContactDetails, Client.fullNameInput, "");
			  Panel.setControlText(Client.controlContactDetails, Client.zipCodeInput, "");
			  Panel.setControlText(Client.controlContactDetails, Client.countryInput, "");
			  Panel.setControlText(Client.controlContactDetails, Client.emailInput, "");
			  processed = true;
		  }
	  }
	  return processed;
  }

  public static void enc_cred_put(Object buffer, String str, int sessionId) {
    byte[] data = str.getBytes();
    int len = data.length;
    byte[] block = new byte[15];

    for (int i = 0; i < len; i += 7) {
      block[0] = (byte) ((int) (1.0D + Math.random() * 127.0D));
      block[1] = (byte) ((int) (Math.random() * 256.0D));
      block[2] = (byte) ((int) (Math.random() * 256.0D));
      block[3] = (byte) ((int) (Math.random() * 256.0D));

      // Put session ID
      Util.int_put(block, 4, sessionId);

      for (int j = 0; j < 7; ++j) {
        if (i + j < len) {
          block[8 + j] = data[i + j];
        } else {
          block[8 + j] = 32;
        }
      }

      BigInteger _block = new BigInteger(1, block);
      BigInteger _enc = _block.modPow(Client.exponent, Client.modulus);
      byte[] enc = _enc.toByteArray();

      StreamUtil.putByteTo(buffer, (byte) enc.length);

      for (int j = 0; j < enc.length; ++j) {
        StreamUtil.putByteTo(buffer, enc[j]);
      }
    }
  }

  // § SECTION hook between screen and renderers §

  public static void success_register() {
    try {
      String user = Panel.getControlText(Client.panelRegister, Client.chooseUserInput);
      String pass = Panel.getControlText(Client.panelRegister, Client.choosePasswordInput);
      Client.login_screen = Client.SCREEN_USERNAME_PASSWORD_LOGIN;
      Client.resetLoginMessage();
      Panel.setControlText(Client.panelLogin, Client.loginUserInput, user);
      Panel.setControlText(Client.panelLogin, Client.loginPassInput, pass);
      Client.preGameDisplay();
      Client.resetTimings();
      Client.login(false, user, pass);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void pregame_hook() {
    try {
      if (Client.login_screen == Client.SCREEN_REGISTER_NEW_ACCOUNT) {
        Client.clearScreen();
        Panel.drawPanel(Client.panelRegister);
      } else if (Client.login_screen == Client.SCREEN_PASSWORD_RECOVERY) {
        Client.clearScreen();
        Panel.drawPanel(Client.panelRecovery);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void response_display_hook(String text1, String text2) {
    try {
      if (Client.login_screen == Client.SCREEN_REGISTER_NEW_ACCOUNT) {
        Panel.setControlText(Client.panelRegister, Client.controlRegister, text1 + " " + text2);
      }

      if (Client.login_screen == Client.SCREEN_PASSWORD_RECOVERY) {
    	  Panel.setControlText(Client.panelRecovery, Client.controlRecoveryTop, text1);
    	  Panel.setControlText(Client.panelRecovery, Client.controlRecoveryBottom, text2);
      }

      // continue checking if == 2 (login screen) with existing code
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static boolean pending_render() {
	  boolean processed = false;
	  if (Client.showRecoveryQuestions) {
		  Client.setInterlace(false);
		  Client.clearScreen();
		  Panel.drawPanel(Client.panelRecoveryQuestions);
		  if (customQuestionEntry != -1) {
	            int yPos = 150;
	            Renderer.drawBox(26, yPos, 460, 60, 0);
	            Renderer.drawBoxBorder(26, yPos, 460, 60, 0xFFFFFF);
	            yPos += 22;
	            Renderer.drawStringCenter("Please enter your question", 256, yPos, 4, 0xFFFFFF);
	            yPos += 25;
	            Renderer.drawStringCenter(Client.pm_enteredText + "*", 256, yPos, 4, 0xFFFFFF);
	        }
		  Renderer.drawSprite(0, Renderer.height_client, Renderer.sprite_media + 22);
		  Client.drawGraphics();
		  processed = true;
	  } else if (Client.showContactDetails) {
		  Client.setInterlace(false);
		  Client.clearScreen();
		  Panel.drawPanel(Client.panelContactDetails);
		  Renderer.drawSprite(0, Renderer.height_client, Renderer.sprite_media + 22);
		  Client.drawGraphics();
		  processed = true;
	  }
	  return processed;
  }

  public static void welcome_new_user_hook() {
    try {
      if (Panel.isSelected(Client.panelWelcome, Client.registerButton)) {
        Client.login_screen = Client.SCREEN_REGISTER_NEW_ACCOUNT;
        Panel.setControlText(Client.panelRegister, Client.chooseUserInput, "");
        Panel.setControlText(Client.panelRegister, Client.choosePasswordInput, "");
        Panel.setControlText(Client.panelRegister, Client.chooseConfirmPassInput, "");
        Panel.setFocus(Client.panelRegister, Client.chooseUserInput);
        Panel.setControlToggled(Client.panelRegister, Client.acceptTermsCheckbox, 0);
        Panel.setControlText(
            Client.panelRegister,
            Client.controlRegister,
            "To create an account please enter all the requested details");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void panel_welcome_hook(int n) {
    try {
      if (Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile)) {
        Panel.addButtonBackTo(Client.panelWelcome, 86, 40 + 250, 100, 35);
        Panel.addCenterTextTo(Client.panelWelcome, 86, 40 + 250, "Signup", 5, false);
        Client.registerButton = Panel.addButtonTo(Client.panelWelcome, 86, 40 + 250, 100, 35);
      }

      Client.panelRegister = Panel.createPanel(50);
      int yPos = 70;
      Client.controlRegister =
          Panel.addCenterTextTo(
              Client.panelRegister,
              256,
              yPos + 8,
              "To create an account please enter all the requested details",
              4,
              true);
      yPos += 25;
      Panel.addButtonBackTo(Client.panelRegister, 256, yPos + 17, 250, 34);
      Panel.addCenterTextTo(Client.panelRegister, 256, yPos + 8, "Choose a Username", 4, false);
      Client.chooseUserInput =
          Panel.addInputTo(Client.panelRegister, 256, yPos + 25, 200, 40, 4, 320, false, false);
      Panel.setFocus(Client.panelRegister, Client.chooseUserInput);
      yPos += 40;
      Panel.addButtonBackTo(Client.panelRegister, 141, yPos + 17, 220, 34);
      Panel.addCenterTextTo(Client.panelRegister, 141, yPos + 8, "Choose a Password", 4, false);
      Client.choosePasswordInput =
          Panel.addInputTo(Client.panelRegister, 141, yPos + 25, 220, 40, 4, 20, true, false);
      Panel.addButtonBackTo(Client.panelRegister, 371, yPos + 17, 220, 34);
      Panel.addCenterTextTo(Client.panelRegister, 371, yPos + 8, "Confirm Password", 4, false);
      Client.chooseConfirmPassInput =
          Panel.addInputTo(Client.panelRegister, 371, yPos + 25, 220, 40, 4, 20, true, false);
      yPos += 40;
      yPos += 20;
      Client.acceptTermsCheckbox = Panel.addCheckboxTo(Client.panelRegister, 60, yPos, 14);
      Panel.addTextTo(
          Client.panelRegister,
          75,
          yPos,
          "I have read and agree to the terms+conditions listed at:",
          4,
          true);
      yPos += 15;
      Panel.addCenterTextTo(
          Client.panelRegister, 256, yPos, "http://www.runescape.com/runeterms.html", 4, true);
      yPos += 20;
      Panel.addButtonBackTo(Client.panelRegister, 156, yPos + 17, 150, 34);
      Panel.addCenterTextTo(Client.panelRegister, 156, yPos + 17, "Submit", 5, false);
      Client.chooseSubmitRegisterButton =
          Panel.addButtonTo(Client.panelRegister, 156, yPos + 17, 150, 34);
      Panel.addButtonBackTo(Client.panelRegister, 356, yPos + 17, 150, 34);
      Panel.addCenterTextTo(Client.panelRegister, 356, yPos + 17, "Cancel", 5, false);
      Client.chooseCancelRegisterButton =
          Panel.addButtonTo(Client.panelRegister, 356, yPos + 17, 150, 34);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void panel_login_hook(int n, int startingYPos) {
	  try {
	      if (Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile)) {
	    	int yPos = startingYPos + 30;
	    	Panel.addButtonBackTo(Client.panelLogin, 410, yPos, 160, 25);
	    	Panel.addCenterTextTo(Client.panelLogin, 410, yPos, "I've lost my password", 4, false);
	    	Client.loginLostPasswordButton = Panel.addButtonTo(Client.panelLogin, 410, yPos, 160, 25);
	      }
	  } catch (Exception e) {
	      e.printStackTrace();
	    }
  }
  
  public static void create_account_recovery() {
	  try {
		  Client.panelRecovery = Panel.createPanel(100);
	        int yPos = 10;
	        Client.controlRecoveryTop = Panel.addCenterTextTo(Client.panelRecovery, 256, yPos, "@yel@To prove this is your account please provide the answers to", 1, true);
	        yPos += 15;
	        Client.controlRecoveryBottom = Panel.addCenterTextTo(Client.panelRecovery, 256, yPos, "@yel@your security questions. You will then be able to reset your password", 1, true);
	        yPos += 35;
	        
	        for (int i = 0; i < 5; ++i) {
	        	Panel.addButtonBackTo(Client.panelRecovery, 256, yPos, 410, 30);
	            Client.controlRecoveryQuestion[i] = Panel.addCenterTextTo(Client.panelRecovery, 256, yPos - 7, i + 1 + ": question?", 1, true);
	            Client.controlRecoveryInput[i] = Panel.addInputTo(Client.panelRecovery, 256, yPos + 7, 310, 30, 1, 80, true, true);
	            yPos += 35;
			}
	        
	        Panel.setFocus(Client.panelRecovery, Client.controlRecoveryInput[0]);
	        Panel.addButtonBackTo(Client.panelRecovery, 256, yPos, 410, 30);
	        Panel.addCenterTextTo(Client.panelRecovery, 256, yPos - 7, "If you know it, enter a previous password used on this account", 1, true);
	        Client.recoverOldPassInput = Panel.addInputTo(Client.panelRecovery, 256, yPos + 7, 310, 30, 1, 80, true, true);
	        yPos += 35;
	        Panel.addButtonBackTo(Client.panelRecovery, 151, yPos, 200, 30);
	        Panel.addCenterTextTo(Client.panelRecovery, 151, yPos - 7, "Choose a NEW password", 1, true);
	        Client.recoverNewPassInput = Panel.addInputTo(Client.panelRecovery, 146, yPos + 7, 200, 30, 1, 80, true, true);
	        Panel.addButtonBackTo(Client.panelRecovery, 361, yPos, 200, 30);
	        Panel.addCenterTextTo(Client.panelRecovery, 361, yPos - 7, "Confirm new password", 1, true);
	        Client.recoverConfirmPassInput = Panel.addInputTo(Client.panelRecovery, 366, yPos + 7, 200, 30, 1, 80, true, true);
	        yPos += 35;
	        Panel.addButtonBackTo(Client.panelRecovery, 201, yPos, 100, 30);
	        Panel.addCenterTextTo(Client.panelRecovery, 201, yPos, "Submit", 4, true);
	        Client.chooseSubmitRecoveryButton = Panel.addButtonTo(Client.panelRecovery, 201, yPos, 100, 30);
	        Panel.addButtonBackTo(Client.panelRecovery, 311, yPos, 100, 30);
	        Panel.addCenterTextTo(Client.panelRecovery, 311, yPos, "Cancel", 4, true);
	        Client.chooseCancelRecoveryButton = Panel.addButtonTo(Client.panelRecovery, 311, yPos, 100, 30);
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }
  
  public static void create_recovery_questions() {
	  try {
		  Client.panelRecoveryQuestions = Panel.createPanel(100);
	        int yPos = 8;
	        Client.controlRecoveryQuestions = Panel.addCenterTextTo(
                    Client.panelRecoveryQuestions,256, yPos, "@yel@Please provide 5 security questions in case you lose your password", 1, true);
	        yPos += 22;
	        Panel.addCenterTextTo(
                    Client.panelRecoveryQuestions,256, yPos, "If you ever lose your password, you will need these to prove you own your account.", 1, true);
	        yPos += 13;
	        Panel.addCenterTextTo(
                    Client.panelRecoveryQuestions,256, yPos, "Your answers are encrypted and are ONLY used for password recovery purposes.", 1, true);
	        yPos += 22;
	        Panel.addCenterTextTo(
                    Client.panelRecoveryQuestions,256, yPos, "@ora@IMPORTANT:@whi@ To recover your password you must give the EXACT same answers you", 1, true);
	        yPos += 13;
	        Panel.addCenterTextTo(
                    Client.panelRecoveryQuestions,256, yPos, "give here. If you think you might forget an answer, or someone else could guess the", 1, true);
	        yPos += 13;
	        Panel.addCenterTextTo(
                    Client.panelRecoveryQuestions,256, yPos, "answer, then press the 'different question' button to get a better question.", 1, true);
	        yPos += 35;
	        for (int questionIndex = 0; questionIndex < 5; ++questionIndex) {
	        	Panel.addButtonBackTo(Client.panelRecoveryQuestions,170, yPos, 310, 30);
	            Client.controlRecoveryText[questionIndex] = recoveryQuestions[recoveryIndices[questionIndex]];
	            Client.controlRecoveryIns[questionIndex] = Panel.addCenterTextTo(
	                    Client.panelRecoveryQuestions,170, yPos - 7, questionIndex + 1 + ": " + recoveryQuestions[recoveryIndices[questionIndex]], 1, true);
	            Client.controlAnswerInput[questionIndex] =  Panel.addInputTo(Client.panelRecoveryQuestions, 170, yPos + 7, 310, 30, 1, 80, false, true);
	            Panel.addButtonBackTo(Client.panelRecoveryQuestions,370, yPos, 80, 30);
	            Panel.addCenterTextTo(
	                    Client.panelRecoveryQuestions,370, yPos - 7, "Different", 1, true);
	            Panel.addCenterTextTo(
	                    Client.panelRecoveryQuestions,370, yPos + 7, "Question", 1, true);
	            Client.controlQuestion[questionIndex] = Panel.addButtonTo(Client.panelRecoveryQuestions,370, yPos, 80, 30);
	            Panel.addButtonBackTo(Client.panelRecoveryQuestions,455, yPos, 80, 30);
	            Panel.addCenterTextTo(
	                    Client.panelRecoveryQuestions,455, yPos - 7, "Enter own", 1, true);
	            Panel.addCenterTextTo(
	                    Client.panelRecoveryQuestions,455, yPos + 7, "Question", 1, true);
	            Client.controlCustomQuestion[questionIndex] = Panel.addButtonTo(Client.panelRecoveryQuestions,455, yPos, 80, 30);
	            yPos += 35;
	        }
	        Panel.setFocus(Client.panelRecoveryQuestions, Client.controlAnswerInput[0]);
	        yPos += 10;
	        Panel.addButtonBackTo(Client.panelRecoveryQuestions,256, yPos, 250, 30);
	        Panel.addCenterTextTo(
                    Client.panelRecoveryQuestions,256, yPos, "Click here when finished", 4, true);
	        Client.chooseFinishSetRecoveryButton = Panel.addButtonTo(Client.panelRecoveryQuestions,256, yPos, 250, 30);

	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }
  
  public static void create_contact_details() {
	  try {
		  Client.panelContactDetails = Panel.createPanel(100);
	        int xPos = 256;
	        int boxWidth = 400;
	        int yPos = 25;
	        Client.controlContactDetails =
	                Panel.addCenterTextTo(
	                    Client.panelContactDetails,256, yPos, "@yel@Please supply your contact details", 5, true);
	        yPos += 30;
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,256, yPos, "We need this information to provide an efficient customer support service ", 1, true);
	        yPos += 15;
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,256, yPos, "and also to work out where to locate future RuneScape servers.", 1, true);
	        yPos += 25;
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,256, yPos, "We know some people are concerned about entering their email address on", 1, true);
	        yPos += 15;
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,255, yPos, "websites, and for this reason we take our users privacy very seriously.", 1, true);
	        yPos += 15;
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,256, yPos, "For our full policy please click the relevant link below this game window", 1, true);
	        yPos += 40;
	        Panel.addButtonBackTo(Client.panelContactDetails,xPos, yPos, boxWidth, 30);
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,xPos, yPos - 7, "Full name", 1, true);
	        Client.fullNameInput = Panel.addInputTo(Client.panelContactDetails,xPos, yPos + 7, boxWidth, 30, 1, 80, false, true);
	        yPos += 35;
	        Panel.addButtonBackTo(Client.panelContactDetails,xPos, yPos, boxWidth, 30);
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,xPos, yPos - 7, "Postcode/Zipcode", 1, true);
	        Client.zipCodeInput = Panel.addInputTo(Client.panelContactDetails,xPos, yPos + 7, boxWidth, 30, 1, 80, false, true);
	        yPos += 35;
	        Panel.addButtonBackTo(Client.panelContactDetails,xPos, yPos, boxWidth, 30);
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,xPos, yPos - 7, "Country", 1, true);
	        Client.countryInput = Panel.addInputTo(Client.panelContactDetails,xPos, yPos + 7, boxWidth, 30, 1, 80, false, true);
	        yPos += 35;
	        Panel.addButtonBackTo(Client.panelContactDetails,xPos, yPos, boxWidth, 30);
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,xPos, yPos - 7, "Email address", 1, true);
	        Client.emailInput = Panel.addInputTo(Client.panelContactDetails,xPos, yPos + 7, boxWidth, 30, 1, 80, false, true);
	        yPos += 35;
	        Panel.addButtonBackTo(Client.panelContactDetails,xPos, yPos, 100, 30);
	        Panel.addCenterTextTo(
                    Client.panelContactDetails,xPos, yPos, "Submit", 4, true);
	        Client.chooseSubmitContactDetailsButton = Panel.addButtonTo(Client.panelContactDetails,xPos, yPos, 100, 30);
	        Panel.setFocus(Client.panelContactDetails,Client.fullNameInput);
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }

  public static int options_security_hook(int xPos, int yPos, int mouseX, int mouseY) {
    short uiWidth = 196;
    int currYPos = yPos;
    if (Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile)) { //Security settings -> Change password,  recovery questions, Change contact details from RSC175
      //currYPos += 5; //this is not needed since was generally patched to line up options panel correctly
      Renderer.drawString("Security settings", xPos, currYPos, 1, 0);
      currYPos += 15;
      int textColor = 0xFFFFFF;
      if (mouseX > xPos
          && mouseX < xPos + uiWidth
          && mouseY > currYPos - 12
          && mouseY < currYPos + 4) {
        textColor = 0xFFFF00; // yellow
      }
      Renderer.drawString("Change password", xPos, currYPos, 1, textColor);
      currYPos += 15;
      textColor = 0xFFFFFF;
      if (mouseX > xPos
          && mouseX < xPos + uiWidth
          && mouseY > currYPos - 12
          && mouseY < currYPos + 4) {
        textColor = 0xFFFF00; // yellow
      }
      Renderer.drawString("Change recovery questions", xPos, currYPos, 1, textColor);
      currYPos += 15;
      textColor = 0xFFFFFF;
      if (mouseX > xPos
          && mouseX < xPos + uiWidth
          && mouseY > currYPos - 12
          && mouseY < currYPos + 4) {
        textColor = 0xFFFF00; // yellow
      }
      Renderer.drawString("Change contact details", xPos, currYPos, 1, textColor);
      currYPos += 15;
      currYPos += 5;
    }
    return currYPos - yPos;
  }

  public static int options_security_click_hook(
      int xPos, int yPos, int mouseX, int mouseY, int mouseButtonClick) {
    short uiWidth = 196;
    int currYPos = yPos;
    if (Settings.SHOW_ACCOUNT_SECURITY_SETTINGS.get(Settings.currentProfile)) { //Security settings -> Change password,  recovery questions, Change contact details from RSC175
      currYPos += 15;
      currYPos += 20;
      if (mouseX > xPos
          && mouseX < xPos + uiWidth
          && mouseY > currYPos - 12
          && mouseY < currYPos + 4
          && mouseButtonClick == 1) {
        AccountManagement.panelPasswordChangeMode = 6;
        Client.modal_enteredText = "";
        Client.modal_text = "";
      }
      currYPos += 15;
      if (mouseX > xPos
          && mouseX < xPos + uiWidth
          && mouseY > currYPos - 12
          && mouseY < currYPos + 4
          && mouseButtonClick == 1) {
        StreamUtil.newPacket(197); // this opcode is "Change recovery questions" as found in RSC175
        StreamUtil.sendPacket();
      }
      currYPos += 15;
      if (mouseX > xPos
          && mouseX < xPos + uiWidth
          && mouseY > currYPos - 12
          && mouseY < currYPos + 4
          && mouseButtonClick == 1) {
        StreamUtil.newPacket(247); // this opcode is "Change contact details" as found in RSC175
        StreamUtil.sendPacket();
      }
      currYPos += 15;
      currYPos += 35;
    }
    return currYPos - yPos;
  }

  public static void draw_change_pass_hook(int mouseX, int mouseY, int mouseButtonClick) {
    if (mouseButtonClick != 0) {
      mouseButtonClick = 0;
      if (mouseX < Renderer.width / 2 - 150 || mouseY < Renderer.height / 2 - 32 
    		  || mouseX > Renderer.width / 2 + 150 || mouseY > Renderer.height / 2 + 28) {
        panelPasswordChangeMode = 0;
        return;
      }
    }

    int yPos = Renderer.height / 2 - 32;
    Renderer.drawBox(Renderer.width / 2 - 150, yPos, 300, 60, 0);
    Renderer.drawBoxBorder(Renderer.width / 2 - 150, yPos, 300, 60, 0xFFFFFF);
    yPos += 22;
    String displayString;
    int i;
    if (panelPasswordChangeMode == 6) {
      Renderer.drawStringCenter("Please enter your current password", Renderer.width / 2, yPos, 4, 0xFFFFFF);
      yPos += 25;
      displayString = "*";

      for (i = 0; i < Client.modal_enteredText.length(); ++i) {
        displayString = "X" + displayString;
      }

      Renderer.drawStringCenter(displayString, Renderer.width / 2, yPos, 4, 0xFFFFFF);
      if (Client.modal_text.length() > 0) {
        oldPassword = Client.modal_text;
        Client.modal_enteredText = "";
        Client.modal_text = "";
        panelPasswordChangeMode = 1;
        return;
      }
    } else if (panelPasswordChangeMode == 1) {
      Renderer.drawStringCenter("Please enter your new password", Renderer.width / 2, yPos, 4, 0xFFFFFF);
      yPos += 25;
      displayString = "*";

      for (i = 0; i < Client.modal_enteredText.length(); ++i) {
        displayString = "X" + displayString;
      }

      Renderer.drawStringCenter(displayString, Renderer.width / 2, yPos, 4, 0xFFFFFF);
      if (Client.modal_text.length() > 0) {
        newPassword = Client.modal_text;
        Client.modal_enteredText = "";
        Client.modal_text = "";
        panelPasswordChangeMode = 2;
        if (newPassword.length() < 5) {
        	panelPasswordChangeMode = 5;
          return;
        }
        if (newPassword.trim().equalsIgnoreCase(Panel.getControlText(Client.panelLogin, Client.loginUserInput).trim())) {
        	panelPasswordChangeMode = 7;
          return;
        }
        return;
      }
    } else if (panelPasswordChangeMode == 2) {
      Renderer.drawStringCenter("Enter password again to confirm", Renderer.width / 2, yPos, 4, 0xFFFFFF);
      yPos += 25;
      displayString = "*";

      for (i = 0; i < Client.modal_enteredText.length(); ++i) {
        displayString = "X" + displayString;
      }

      Renderer.drawStringCenter(displayString, Renderer.width / 2, yPos, 4, 0xFFFFFF);
      if (Client.modal_text.length() > 0) {
        if (Client.modal_text.equalsIgnoreCase(newPassword)) {
          panelPasswordChangeMode = 4;
          sendPassChange(oldPassword, newPassword);
          return;
        }

        panelPasswordChangeMode = 3;
        return;
      }
    } else {
      if (panelPasswordChangeMode == 3) {
        Renderer.drawStringCenter("Passwords do not match!", Renderer.width / 2, yPos, 4, 0xFFFFFF);
        yPos += 25;
        Renderer.drawStringCenter("Press any key to close", Renderer.width / 2, yPos, 4, 0xFFFFFF);
        return;
      }

      if (panelPasswordChangeMode == 4) {
        Renderer.drawStringCenter("Ok, your request has been sent", Renderer.width / 2, yPos, 4, 0xFFFFFF);
        yPos += 25;
        Renderer.drawStringCenter("Press any key to close", Renderer.width / 2, yPos, 4, 0xFFFFFF);
        return;
      }

      if (panelPasswordChangeMode == 5) {
        Renderer.drawStringCenter("Password must be at", Renderer.width / 2, yPos, 4, 0xFFFFFF);
        yPos += 25;
        Renderer.drawStringCenter("least 5 letters long", Renderer.width / 2, yPos, 4, 0xFFFFFF);
        return;
      }
      
      if (panelPasswordChangeMode == 7) {
          Renderer.drawStringCenter("Your password must not be", Renderer.width / 2, yPos, 4, 0xFFFFFF);
          yPos += 25;
          Renderer.drawStringCenter("the same as your username", Renderer.width / 2, yPos, 4, 0xFFFFFF);
          return;
        }
    }
  }
  
  public static void processForgotPassword() {
	  if (Client.loginLostPasswordButton != 0) {
		  if (Panel.isSelected(Client.panelLogin, Client.loginLostPasswordButton)) {
			  String user = Panel.getControlText(Client.panelLogin, Client.loginUserInput);
				if (user.trim().length() == 0) {
					Client.setResponseMessage("", "You must enter your username to recover your password");
					return;
				}

				Client.username_login = user;
				forgotPass(user);
		  }
	  }
	  
  }

  // § SECTION hook key and mouse consumption §

  /*
   * Intercept the part of checking if other panels should consume key event in game,
   * we may have logged in flag distinct than 0, but is already checked after this hook,
   * so 0 is returned to continue on that flow
   * Password change states 3, 4, 5 any 7 are final, so they are changed to 0 and a non-zero
   * response is given to break out of trying to consume key on other panels
   * For any other non-zero password change state, non-zero response given
   */
  public static int ingame_keyhandler_hook(int loggedIn, int key) {
	  if (loggedIn != 1) return 0;
	  
	  if (Client.showRecoveryQuestions) {
		  if (customQuestionEntry != -1) return 1;
		  Panel.handleKey(Client.panelRecoveryQuestions, key);
		  return 1;
	  }
	  
	  if (Client.showContactDetails) {
		  Panel.handleKey(Client.panelContactDetails, key);
		  return 1;
	  }
	  
	  if (panelPasswordChangeMode == 3 || panelPasswordChangeMode == 4 || panelPasswordChangeMode == 5 || panelPasswordChangeMode == 7) {
		  panelPasswordChangeMode = 0;
			return 1;
	  }
	  
	  return panelPasswordChangeMode > 0 ? 1 : 0;
  }
  
  public static void account_panels_key_hook(int a, int key) {
    try {
      if (Client.login_screen == Client.SCREEN_REGISTER_NEW_ACCOUNT) {
        Panel.handleKey(Client.panelRegister, key);
      }
      
      if (Client.login_screen == Client.SCREEN_PASSWORD_RECOVERY) {
    	  Panel.handleKey(Client.panelRecovery, key);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void account_panels_input_hook(int n1, int mouseY, int n3, int mouseX) {
    try {
      if (Client.login_screen == Client.SCREEN_REGISTER_NEW_ACCOUNT) {
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
          Client.login_screen = Client.SCREEN_CLICK_TO_LOGIN;
        }

        if (Panel.isSelected(Client.panelRegister, Client.chooseSubmitRegisterButton)) {
          if (Panel.getControlText(Client.panelRegister, Client.chooseUserInput) != null
              && Panel.getControlText(Client.panelRegister, Client.chooseUserInput).length() != 0
              && Panel.getControlText(Client.panelRegister, Client.choosePasswordInput) != null
              && Panel.getControlText(Client.panelRegister, Client.choosePasswordInput).length()
                  != 0) {
            if (!Panel.getControlText(Client.panelRegister, Client.choosePasswordInput)
                .equalsIgnoreCase(
                    Panel.getControlText(Client.panelRegister, Client.chooseConfirmPassInput))) {
              Panel.setControlText(
                  Client.panelRegister,
                  Client.controlRegister,
                  "@yel@The two passwords entered are not the same as each other!");
              return;
            }

            if (Panel.getControlText(Client.panelRegister, Client.choosePasswordInput).length()
                < 5) {
              Panel.setControlText(
                  Client.panelRegister,
                  Client.controlRegister,
                  "@yel@Your password must be at least 5 letters long");
              return;
            }
            
            if (Panel.getControlText(Client.panelRegister, Client.choosePasswordInput).trim().equalsIgnoreCase(Panel.getControlText(Client.panelRegister, Client.chooseUserInput).trim())) {
            	Panel.setControlText(
                        Client.panelRegister,
                        Client.controlRegister, "@yel@Your password must not be the same as your username!");
                return;
            }

            if (Panel.isToggle(Client.panelRegister, Client.acceptTermsCheckbox) == 0) {
              Panel.setControlText(
                  Client.panelRegister,
                  Client.controlRegister,
                  "@yel@You must agree to the terms+conditions to continue");
              return;
            }

            Panel.setControlText(
                Client.panelRegister,
                Client.controlRegister,
                "Please wait... Creating new account");
            Client.preGameDisplay();
            Client.resetTimings();
            String user, pass;
            user = Panel.getControlText(Client.panelRegister, Client.chooseUserInput);
            pass = Panel.getControlText(Client.panelRegister, Client.choosePasswordInput);
            AccountManagement.register(user, pass);
            return;
          }
          Panel.setControlText(
              Client.panelRegister,
              Client.controlRegister,
              "@yel@Please fill in ALL requested information to continue!");
          return;
        }
      }
      
      if (Client.login_screen == Client.SCREEN_PASSWORD_RECOVERY) {
    	  Panel.handleMouse(Client.panelRecovery, n1, mouseY, n3, mouseX);
			if (Panel.isSelected(Client.panelRecovery, Client.chooseSubmitRecoveryButton)) {
				String newPass = Panel.getControlText(Client.panelRecovery, Client.recoverNewPassInput);
				String confirmPass = Panel.getControlText(Client.panelRecovery, Client.recoverConfirmPassInput);
				if (!newPass.equalsIgnoreCase(confirmPass)) {
					Client.setResponseMessage("", "@yel@The two new passwords entered are not the same as each other!");
					return;
				}

				if (newPass.length() < 5) {
					Client.setResponseMessage("", "@yel@Your new password must be at least 5 letters long");
					return;
				}

				recover();
			}

			if (Panel.isSelected(Client.panelRecovery, Client.chooseCancelRecoveryButton)) {
				Client.login_screen = Client.SCREEN_CLICK_TO_LOGIN;
			}
		}
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void recovery_questions_input(int n1, int mouseY, int n3, int mouseX) {
	  try {
		  if (customQuestionEntry != -1) {
			  if (Client.pm_text.length() > 0) {
				  Client.controlRecoveryText[customQuestionEntry] = Client.pm_text;
				  Panel.setControlText(
				            Client.panelRecoveryQuestions,Client.controlRecoveryIns[customQuestionEntry], customQuestionEntry + 1 + ": " + Client.controlRecoveryText[customQuestionEntry]);
				  Panel.setControlText(
				            Client.panelRecoveryQuestions,Client.controlAnswerInput[customQuestionEntry], "");
		            customQuestionEntry = -1;
			  }
		  } else {
			  Panel.handleMouse(Client.panelRecoveryQuestions, n1, mouseY, n3, mouseX);
			  
			  int cursorIndex;
				for (int questionIndex = 0; questionIndex < 5; ++questionIndex) {
					if (Panel.isSelected(Client.panelRecoveryQuestions, Client.controlQuestion[questionIndex])) {
						boolean hasDistinctQuestions = false;

						while (!hasDistinctQuestions) {
							recoveryIndices[questionIndex] = (recoveryIndices[questionIndex] + 1) % recoveryQuestions.length;
							hasDistinctQuestions = true;

							for (cursorIndex = 0; cursorIndex < 5; ++cursorIndex) {
								if (cursorIndex != questionIndex && recoveryIndices[cursorIndex] == recoveryIndices[questionIndex]) {
									hasDistinctQuestions = false;
								}
							}
						}
						
						Client.controlRecoveryText[questionIndex] = recoveryQuestions[recoveryIndices[questionIndex]];
			            Panel.setControlText(Client.panelRecoveryQuestions,Client.controlRecoveryIns[questionIndex], questionIndex + 1 + ": " + recoveryQuestions[recoveryIndices[questionIndex]]);
			            Panel.setControlText(Client.panelRecoveryQuestions,Client.controlAnswerInput[questionIndex], "");
					}
				}
				
				for (int i = 0; i < 5; ++i) {
					if (Panel.isSelected(Client.panelRecoveryQuestions,Client.controlCustomQuestion[i])) {
						customQuestionEntry = i;
		                Client.pm_enteredText = "";
		                Client.pm_text = "";
					}
				}
				
				if (Panel.isSelected(Client.panelRecoveryQuestions,Client.chooseFinishSetRecoveryButton)) {
					cursorIndex = 0;
					
					while (true) {
						if (cursorIndex >= 5) {
							for (int outerIdx = 0; outerIdx < 5; ++outerIdx) {
								String checkedAnswer = Panel.getControlText(Client.panelRecoveryQuestions, Client.controlAnswerInput[outerIdx]);

								for (int innerIdx = 0; innerIdx < outerIdx; ++innerIdx) {
									String questionAnswer = Panel.getControlText(Client.panelRecoveryQuestions, Client.controlAnswerInput[innerIdx]);
									if (checkedAnswer.equalsIgnoreCase(questionAnswer)) {
										Panel.setControlText(Client.panelRecoveryQuestions,Client.controlRecoveryQuestions, "@yel@Each question must have a different answer");
										return;
									}
								}
							}

							sendRecoveryQuestions(); //this generates & sends RSC175 opcode 208

							for (int questionIndex = 0; questionIndex < 5; ++questionIndex) {
								recoveryIndices[questionIndex] = questionIndex;
					            Client.controlRecoveryText[questionIndex] = recoveryQuestions[recoveryIndices[questionIndex]];
					            Panel.setControlText(Client.panelRecoveryQuestions,Client.controlAnswerInput[questionIndex], "");
					            Panel.setControlText(Client.panelRecoveryQuestions,Client.controlRecoveryIns[questionIndex], questionIndex + 1 + ": " + Client.controlRecoveryText[questionIndex]);
							}

							Client.clearScreen();
				            Client.showRecoveryQuestions = false;
							break;
						}

						String chkAnswer = Panel.getControlText(Client.panelRecoveryQuestions, Client.controlAnswerInput[cursorIndex]);
						if (chkAnswer == null || chkAnswer.length() < 3) {
			                Panel.setControlText(Client.panelRecoveryQuestions,Client.controlRecoveryQuestions, "@yel@Please provide a longer answer to question: " + (cursorIndex + 1));
			                return;
			            }

						++cursorIndex;
					}
				}
		  }
	  } catch (Exception e) {
	      e.printStackTrace();
	    }
  }
  
  public static void contact_details_input(int n1, int mouseY, int n3, int mouseX) {
	  try {
		  Panel.handleMouse(Client.panelContactDetails, n1, mouseY, n3, mouseX);
	        if (Panel.isSelected(Client.panelContactDetails, Client.fullNameInput)) {
	        	Panel.setFocus(Client.panelContactDetails, Client.zipCodeInput);
	        }
	        if (Panel.isSelected(Client.panelContactDetails, Client.zipCodeInput)) {
	        	Panel.setFocus(Client.panelContactDetails, Client.countryInput);
	        }
	        if (Panel.isSelected(Client.panelContactDetails, Client.countryInput)) {
	        	Panel.setFocus(Client.panelContactDetails, Client.emailInput);
	        }
	        if (Panel.isSelected(Client.panelContactDetails, Client.emailInput)) {
	        	Panel.setFocus(Client.panelContactDetails, Client.fullNameInput);
	        }
	        if (!Panel.isSelected(Client.panelContactDetails, Client.chooseSubmitContactDetailsButton)) {
	        	return;
	        }
	        String name = Panel.getControlText(Client.panelContactDetails, Client.fullNameInput);
	        String zipcode = Panel.getControlText(Client.panelContactDetails, Client.zipCodeInput);
	        String country = Panel.getControlText(Client.panelContactDetails, Client.countryInput);
	        String email = Panel.getControlText(Client.panelContactDetails, Client.emailInput);
	        if (name != null && name.length() != 0 && zipcode != null && zipcode.length() != 0 && country != null && country.length() != 0 && email != null && email.length() != 0) {
	        	sendContactDetails(name, zipcode, country, email);  //this generates & sends RSC175 opcode 253
	            Client.clearScreen();
	            Client.showContactDetails = false;
	            return;
	        }
	        Panel.setControlText(Client.panelContactDetails, Client.controlContactDetails, "@yel@Please fill in all the requested details");
	  } catch (Exception e) {
	      e.printStackTrace();
	    }
  }
}
