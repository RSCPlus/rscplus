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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.JOptionPane;
import Client.Launcher;
import Client.Logger;
import Client.Settings;
import Client.Util;

public class Replay {
	// If we ever change replays in a way that breaks backwards compatibility, we need to increment this
	public static int VERSION = 0;
	
	static DataOutputStream output = null;
	static DataOutputStream input = null;
	static DataOutputStream keys = null;
	static DataOutputStream keyboard = null;
	static DataOutputStream mouse = null;
	
	static DataInputStream play_keys = null;
	static DataInputStream play_keyboard = null;
	static DataInputStream play_mouse = null;
	
	public static final byte KEYBOARD_TYPED = 0;
	public static final byte KEYBOARD_PRESSED = 1;
	public static final byte KEYBOARD_RELEASED = 2;
	
	public static final byte MOUSE_CLICKED = 0;
	public static final byte MOUSE_ENTERED = 1;
	public static final byte MOUSE_EXITED = 2;
	public static final byte MOUSE_PRESSED = 3;
	public static final byte MOUSE_RELEASED = 4;
	public static final byte MOUSE_DRAGGED = 5;
	public static final byte MOUSE_MOVED = 6;
	public static final byte MOUSE_WHEEL_MOVED = 7;
	
	public static final int DEFAULT_PORT = 43594;
	
	public static final int TIMESTAMP_EOF = -1;
	
	public static boolean isPlaying = false;
	public static boolean isRecording = false;
	public static boolean paused = false;
	public static boolean closeDialogue = false;
	
	// Hack for player position
	public static boolean ignoreFirstMovement = true;
	
	public static int fps = 50;
	public static float fpsPlayMultiplier = 1.0f;
	public static float prevFPSPlayMultiplier = fpsPlayMultiplier;
	public static int frame_time_slice;
	public static int connection_port;
	
	public static ReplayServer replayServer = null;
	public static Thread replayThread = null;
	
	public static int replay_version;
	public static int client_version;
	public static int prevPlayerX;
	public static int prevPlayerY;
	
	public static int timestamp;
	public static int timestamp_client;
	public static int timestamp_server_last;
	public static int timestamp_kb_input;
	public static int timestamp_mouse_input;
    
    public static boolean started_record_kb_mouse = true;
	
	public static int enc_opcode;
	public static int retained_timestamp;
	public static byte[] retained_bytes = null;
	public static int retained_off;
	public static int retained_bread;
	
	public static int timestamp_lag = 0;
	
	public static void incrementTimestamp() {
		timestamp++;
		
		if (timestamp == TIMESTAMP_EOF) {
			timestamp = 0;
		}
	}
	
	public static void incrementTimestampClient() {
		timestamp_client++;
		
		if (timestamp_client == TIMESTAMP_EOF) {
			timestamp_client = 0;
		}
	}
	
	public static void init() {
		timestamp = 0;
		timestamp_client = 0;
		timestamp_server_last = 0;
	}
	
	public static int getServerLag() {
		return timestamp - timestamp_server_last;
	}
	
	public static boolean initializeReplayPlayback(String replayDirectory) {
		try {
			// We read in this information to adjust our replay method based on versioning
			// No need to check if output matches until other revisions come out
			DataInputStream version = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(replayDirectory + "/version.bin"))));
			replay_version = version.readInt();
			client_version = version.readInt();
			version.close();
			
			if (replay_version > Replay.VERSION) {
				JOptionPane.showMessageDialog(Game.getInstance().getApplet(), "The replay you selected is for replay version " + replay_version + ".\n" +
						"You may need to update rscplus to run this replay.\n", "rscplus",
						JOptionPane.ERROR_MESSAGE,
						Launcher.icon_warn);
				return false;
			}
			
			if (client_version < 234 || client_version > 235) {
				JOptionPane.showMessageDialog(Game.getInstance().getApplet(), "The replay you selected is for client version " + client_version + ".\n" +
						"rscplus currently only supports versions 234 and 235.\n", "rscplus",
						JOptionPane.ERROR_MESSAGE,
						Launcher.icon_warn);
				return false;
			}
			
			play_keys = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(replayDirectory + "/keys.bin"))));
            if (Settings.RECORD_KB_MOUSE) {
				File file = new File(replayDirectory + "/keyboard.bin.gz");
				if (file.exists()) {
					play_keyboard = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
					timestamp_kb_input = play_keyboard.readInt();
				}
				file = new File(replayDirectory + "/mouse.bin.gz");
				if (file.exists()) {
					play_mouse = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(new File(replayDirectory + "/mouse.bin.gz")))));
					timestamp_mouse_input = play_mouse.readInt();
				}
                started_record_kb_mouse = true;
            } else {
                started_record_kb_mouse = false;
            }
		} catch (Exception e) {
			play_keys = null;
			play_keyboard = null;
			play_mouse = null;
			JOptionPane.showMessageDialog(Game.getInstance().getApplet(), "An error has occured while trying to open the replay.", "rscplus",
					JOptionPane.ERROR_MESSAGE,
					Launcher.icon_warn);
			return false;
		}
		Game.getInstance().getJConfig().changeWorld(6);
		replayServer = new ReplayServer(replayDirectory);
		replayThread = new Thread(replayServer);
		replayThread.start();
		ignoreFirstMovement = true;
		// if (Client.strings[662].startsWith("from:")) {
		// Client.strings[662] = "@bla@from:";
		// }
		isPlaying = true;
		
		// Wait
		try {
			while (!replayServer.isReady)
				Thread.sleep(1);
		} catch (Exception e) {
		}
		Client.login(false, "Replay", "");
		return true;
	}
	
	public static void closeReplayPlayback() {
		if (play_keys == null)
			return;
		
		try {
			play_keys.close();
			play_keyboard.close();
			play_mouse.close();
			
			play_keys = null;
			play_keyboard = null;
			play_mouse = null;
		} catch (Exception e) {
			play_keys = null;
			play_keyboard = null;
			play_mouse = null;
		}
		
		Game.getInstance().getJConfig().changeWorld(Settings.WORLD);
		resetFrameTimeSlice();
		Client.closeConnection(true);
		resetPort();
		fpsPlayMultiplier = 1.0f;
		replayServer.isDone = true;
		resetPatchClient();
		isPlaying = false;
	}
	
	public static void initializeReplayRecording() {
		// No username specified, exit
		if (Client.username_login.length() == 0)
			return;
		
		String timeStamp = new SimpleDateFormat("MM-dd-yyyy HH.mm.ss").format(new Date());
		
		String recordingDirectory = Settings.Dir.REPLAY + "/" + Client.username_login;
		Util.makeDirectory(recordingDirectory);
		recordingDirectory = recordingDirectory + "/" + timeStamp;
		Util.makeDirectory(recordingDirectory);
		
		try {
			// Write out version information
			DataOutputStream version = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(recordingDirectory + "/version.bin"))));
			version.writeInt(Replay.VERSION);
			version.writeInt(Client.version);
			version.close();
			
			output = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(recordingDirectory + "/out.bin.gz")))));
			input = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(recordingDirectory + "/in.bin.gz")))));
			keys = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(recordingDirectory + "/keys.bin"))));
            if (Settings.RECORD_KB_MOUSE) {
				keyboard = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(recordingDirectory + "/keyboard.bin.gz")))));
				mouse = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(recordingDirectory + "/mouse.bin.gz")))));
                started_record_kb_mouse = true; //need this to know whether or not to close the file if the user changes settings mid-recording
            } else {
                started_record_kb_mouse = false;
            }
			
			Logger.Info("Replay recording started");
		} catch (Exception e) {
			output = null;
			input = null;
			keys = null;
			keyboard = null;
			mouse = null;
			Logger.Error("Unable to create replay files");
			return;
		}
		
		retained_timestamp = TIMESTAMP_EOF;
		retained_bytes = null;
		isRecording = true;
	}
	
	public static void closeReplayRecording() {
		if (input == null)
			return;
		
		try {
			// since we are working with packet retention, last packet on memory has not been written,
			// write it here
			if (retained_timestamp != TIMESTAMP_EOF && retained_bytes != null) {
				try {
					input.writeInt(retained_timestamp);
					input.writeInt(retained_bread);
					input.write(retained_bytes, retained_off, retained_bread);
				} catch (Exception e) {
					e.printStackTrace();
					shutdown_error();
				}
			}
			
			// Write EOF values
			input.writeInt(TIMESTAMP_EOF);
			output.writeInt(TIMESTAMP_EOF);
			
			output.close();
			input.close();
			keys.close();
            if (started_record_kb_mouse) {
				keyboard.writeInt(TIMESTAMP_EOF);
				mouse.writeInt(TIMESTAMP_EOF);
                keyboard.close();
                mouse.close();
            }
			
			output = null;
			input = null;
			keys = null;
			keyboard = null;
			mouse = null;
			
			retained_timestamp = TIMESTAMP_EOF;
			retained_bytes = null;
			
			Logger.Info("Replay recording stopped");
		} catch (Exception e) {
			output = null;
			input = null;
			keys = null;
			keyboard = null;
			mouse = null;
			Logger.Error("Unable to close replay files");
			return;
		}
		
		isRecording = false;
	}
	
	public static void update() {
		// If the replay is done playing, disable replay mode
		if (isPlaying) {
			// Reset inactivity timer, we're not the ones playing the game
			Client.setInactivityTimer(0);

			int playerX = prevPlayerX;
			int playerY = prevPlayerY;
			
			// Make sure we're loaded in
			if (Client.isGameLoaded) {
				playerX = Client.localRegionX + Client.regionX;
				playerY = Client.localRegionY + Client.regionY;
				
				// Reset dialogue option after force pressed in replay
				if (KeyboardHandler.dialogue_option != -1)
					KeyboardHandler.dialogue_option = -1;
			}
			
			// If the player moves, we're going to run some events
			if (playerX != prevPlayerX || playerY != prevPlayerY) {
				if(!ignoreFirstMovement) {
					prevPlayerX = playerX;
					prevPlayerY = playerY;
					
					// Close dialogues
					closeDialogue = true;
				} else {
					ignoreFirstMovement = false;
					prevPlayerX = playerX;
					prevPlayerY = playerY;
				}
			}
			
			if (closeDialogue) {
				// Close welcome screen
				if (Client.isWelcomeScreen())
					Client.show_welcome = false;
				
				KeyboardHandler.dialogue_option = 1;
				closeDialogue = false;
			}

			// Replay server is no longer running
			if (replayServer.isDone)
				closeReplayPlayback();
		}
		
		// Increment the replay timestamp
		if (!Replay.isPlaying)
			Replay.incrementTimestamp();
	}
	
	public static int getPercentPlayed() {
		return 100 - replayServer.getPercentRemaining();
	}

	public static void playKeyboardInput() {
		try {
			while (timestamp >= timestamp_kb_input) {
				byte event = play_keyboard.readByte();
				char keychar = play_keyboard.readChar();
				int keycode = play_keyboard.readInt();
				int modifier = play_keyboard.readInt();
                    KeyEvent keyEvent;
                    switch (event) {
                    case KEYBOARD_PRESSED:
                        keyEvent = new KeyEvent(Game.getInstance().getApplet(), KeyEvent.KEY_PRESSED, timestamp, modifier, keycode, keychar);
                        Client.handler_keyboard.keyPressed(keyEvent);
                        break;
                    case KEYBOARD_RELEASED:
                        keyEvent = new KeyEvent(Game.getInstance().getApplet(), KeyEvent.KEY_RELEASED, timestamp, modifier, keycode, keychar);
                        Client.handler_keyboard.keyReleased(keyEvent);
                        break;
                    case KEYBOARD_TYPED:
                        keyEvent = new KeyEvent(Game.getInstance().getApplet(), KeyEvent.KEY_TYPED, timestamp, modifier, keycode, keychar);
                        Client.handler_keyboard.keyTyped(keyEvent);
                        break;
                    }
                    timestamp_kb_input = play_keyboard.readInt();
			}
		} catch (Exception e) {
		}
	}
	
	public static void playMouseInput() {
		try {
			while (timestamp >= timestamp_mouse_input) {
				byte event = play_mouse.readByte();
				int x = play_mouse.readInt();
				int y = play_mouse.readInt();
				int rotation = play_mouse.readInt();
				int modifier = play_mouse.readInt();
				int clickCount = play_mouse.readInt();
				int scrollType = play_mouse.readInt();
				int scrollAmount = play_mouse.readInt();
				boolean popupTrigger = play_mouse.readBoolean();
				int button = play_mouse.readInt();
				MouseEvent mouseEvent;
				switch (event) {
				case MOUSE_CLICKED:
					mouseEvent = new MouseEvent(Game.getInstance().getApplet(), MouseEvent.MOUSE_CLICKED, timestamp, modifier, x, y, clickCount, popupTrigger, button);
					Client.handler_mouse.mouseClicked(mouseEvent);
					break;
				case MOUSE_ENTERED:
					mouseEvent = new MouseEvent(Game.getInstance().getApplet(), MouseEvent.MOUSE_ENTERED, timestamp, modifier, x, y, clickCount, popupTrigger, button);
					Client.handler_mouse.mouseEntered(mouseEvent);
					break;
				case MOUSE_EXITED:
					mouseEvent = new MouseEvent(Game.getInstance().getApplet(), MouseEvent.MOUSE_EXITED, timestamp, modifier, x, y, clickCount, popupTrigger, button);
					Client.handler_mouse.mouseExited(mouseEvent);
					break;
				case MOUSE_PRESSED:
					mouseEvent = new MouseEvent(Game.getInstance().getApplet(), MouseEvent.MOUSE_PRESSED, timestamp, modifier, x, y, clickCount, popupTrigger, button);
					Client.handler_mouse.mousePressed(mouseEvent);
					break;
				case MOUSE_RELEASED:
					mouseEvent = new MouseEvent(Game.getInstance().getApplet(), MouseEvent.MOUSE_RELEASED, timestamp, modifier, x, y, clickCount, popupTrigger, button);
					Client.handler_mouse.mouseReleased(mouseEvent);
					break;
				case MOUSE_DRAGGED:
					mouseEvent = new MouseEvent(Game.getInstance().getApplet(), MouseEvent.MOUSE_DRAGGED, timestamp, modifier, x, y, clickCount, popupTrigger, button);
					Client.handler_mouse.mouseDragged(mouseEvent);
					break;
				case MOUSE_MOVED:
					mouseEvent = new MouseEvent(Game.getInstance().getApplet(), MouseEvent.MOUSE_MOVED, timestamp, modifier, x, y, clickCount, popupTrigger, button);
					Client.handler_mouse.mouseMoved(mouseEvent);
					break;
				case MOUSE_WHEEL_MOVED:
					MouseWheelEvent wheelEvent = new MouseWheelEvent(Game.getInstance().getApplet(), MouseWheelEvent.MOUSE_WHEEL, timestamp, modifier, x, y, clickCount,
							popupTrigger, scrollType, scrollAmount, rotation);
					Client.handler_mouse.mouseWheelMoved(wheelEvent);
					break;
				}
				timestamp_mouse_input = play_mouse.readInt();
			}
		} catch (Exception e) {
		}
	}

	public static void togglePause() {
		paused = !paused;
		
		if (paused) {
			resetFrameTimeSlice();
		} else {
			updateFrameTimeSlice();
		}
	}
	
	public static boolean isValid(String path) {
		return (new File(path + "/in.bin.gz").exists() && new File(path + "/keys.bin").exists() && new File(path + "/version.bin").exists());
	}
	
	public static void resetFrameTimeSlice() {
		frame_time_slice = 1000 / fps;
	}
	
	// adjusts frame time slice
	public static int getFrameTimeSlice() {
		return frame_time_slice;
	}
	
	// Returns video elapsed time in millis
	public static int elapsedTimeMillis() {
		int time_slice = 1000 / fps;
		return timestamp * time_slice;
	}
	
	// Returns video length in millis
	public static int endTimeMillis() {
		int time_slice = 1000 / fps;
		return replayServer.timestamp_end * time_slice;
	}
	
	public static void updateFrameTimeSlice() {
		if (paused)
			return;
		
		if (isPlaying) {
			frame_time_slice = 1000 / ((int)(fps * fpsPlayMultiplier));
			return;
		}
		
		frame_time_slice = 1000 / fps;
	}
		
	public static int getFPS() {	
		if (isPlaying) {
			return (int)(fps * fpsPlayMultiplier);
		}
		
		return fps;
	}
	
	// only change port in replay
	public static void changePort(int newPort) {
		if (isPlaying) {
			Logger.Info("Replay: Changing port to " + newPort);
			connection_port = newPort;
		}
	}
	
	public static void resetPort() {
		connection_port = Replay.DEFAULT_PORT;
	}
	
	public static boolean controlPlayback(String action) {
        if (isPlaying) {
            switch (action){
			case "stop":
				closeReplayPlayback();
				break;
                case "pause":
                    togglePause();
                    Client.displayMessage(paused ? "Playback paused." : "Playback unpaused.", Client.CHAT_QUEST);
                    break;
                case "ff_plus":
				if (fpsPlayMultiplier < 1.0f) {
					fpsPlayMultiplier += 0.25f;
				} else if (fpsPlayMultiplier < 20.0f) {
                    	fpsPlayMultiplier += 1.0f;
                    	}
				updateFrameTimeSlice();
                    Client.displayMessage("Playback speed set to " + new DecimalFormat("##.##").format(fpsPlayMultiplier) + "x.", Client.CHAT_QUEST);
                    break;
                case "ff_minus":
				if (fpsPlayMultiplier > 1.0f) {
					fpsPlayMultiplier -= 1.0f;
				} else if (fpsPlayMultiplier > 0.25f) {
					fpsPlayMultiplier -= 0.25f;
                    }
				updateFrameTimeSlice();
                    Client.displayMessage("Playback speed set to " + new DecimalFormat("##.##").format(fpsPlayMultiplier) + "x.", Client.CHAT_QUEST);
                    break;
                case "ff_reset":
                    fpsPlayMultiplier = 1.0f;
				updateFrameTimeSlice();
                    Client.displayMessage("Playback speed reset to 1x.", Client.CHAT_QUEST);
                    break;
                default:
                    Logger.Error("An unrecognized command was sent to controlPlayback: " + action);
                    break;
            }
            return true;
        } else {
            return false;
        }
    }
	
	public static void shutdown_error() {
		closeReplayPlayback();
		closeReplayRecording();
		if (Client.state == Client.STATE_GAME) {
			Client.displayMessage("Recording has been stopped because of an error", Client.CHAT_QUEST);
			Client.displayMessage("Please log back in to start recording again", Client.CHAT_QUEST);
		}
	}
    
	public static void dumpKeyboardInput(int keycode, byte event, char keychar, int modifier) {
		if (keyboard == null)
			return;
		
		try {
			keyboard.writeInt(timestamp);
			keyboard.writeByte(event);
			keyboard.writeChar(keychar);
			keyboard.writeInt(keycode);
			keyboard.writeInt(modifier);
		} catch (Exception e) {
			e.printStackTrace();
			shutdown_error();
		}
	}
	
	public static void dumpMouseInput(byte event, int x, int y, int rotation, int modifier, int clickCount, int scrollType, int scrollAmount, boolean popupTrigger, int button) {
		if (mouse == null)
			return;
		
		try {
			mouse.writeInt(timestamp);
			mouse.writeByte(event);
			mouse.writeInt(x);
			mouse.writeInt(y);
			mouse.writeInt(rotation);
			mouse.writeInt(modifier);
			mouse.writeInt(clickCount);
			mouse.writeInt(scrollType);
			mouse.writeInt(scrollAmount);
			mouse.writeBoolean(popupTrigger);
			mouse.writeInt(button);
		} catch (Exception e) {
			e.printStackTrace();
			shutdown_error();
		}
	}
	
	public static void dumpRawInputStream(byte[] b, int n, int n2, int n5, int bytesread) {
		// Save timestamp of last time we saw data from the server
		if (bytesread > 0) {
			int lag = timestamp - timestamp_server_last;
			if (lag > 10)
				timestamp_lag = lag;
			timestamp_server_last = timestamp;
		}
		
		if (input == null)
			return;
		
		int off = n2 + n5;
		// when packet 182 is received retained_timestamp should be TIMESTAMP_EOF
		// to indicate not to dump previous packet
		if (retained_timestamp != TIMESTAMP_EOF) {
			// new set of packets arrived, dump previous ones
			try {
				input.writeInt(retained_timestamp);
				input.writeInt(retained_bread);
				input.write(retained_bytes, retained_off, retained_bread);
			} catch (Exception e) {
				e.printStackTrace();
				shutdown_error();
			}
		}
		retained_timestamp = timestamp;
		// Important! Cloned since it gets modified by decryption in game logic
		retained_bytes = b.clone();
		retained_off = off;
		retained_bread = bytesread;
	}
	
	public static void dumpRawOutputStream(byte[] b, int off, int len) {
		if (output == null)
			return;
		
		try {
			boolean isLogin = false;
			int pos = -1;
			byte[] out_b = null;
			// for the first bytes if byte == (byte)Client.version, 4 bytes before indicate if its
			// login or reconnect and 5 its what determines if its login-related
			for (int i = off + 5; i < off + Math.min(15, len); i++) {
				if (b[i] == (byte)Client.version && b[i - 5] == 0) {
					isLogin = true;
					pos = i + 1;
					out_b = b.clone();
					break;
				}
			}
			if (isLogin && pos != -1) {
				for (int i = pos; i < off + len; i++) {
					out_b[i] = 0x00;
				}
				
				Logger.Info("Replay: Removed login block from client output");
				
				output.writeInt(timestamp);
				output.writeInt(len);
				output.write(out_b, off, len);
				return;
			}
			
			output.writeInt(timestamp);
			output.writeInt(len);
			output.write(b, off, len);
		} catch (Exception e) {
			e.printStackTrace();
			shutdown_error();
		}
	}
	
	public static int hookXTEAKey(int key) {
		if (play_keys != null) {
			try {
				return play_keys.readInt();
			} catch (Exception e) {
				// e.printStackTrace();
				shutdown_error();
				return key;
			}
		}
		
		if (keys == null)
			return key;
		
		try {
			keys.writeInt(key); // data length
		} catch (Exception e) {
			// e.printStackTrace();
			shutdown_error();
		}
		
		return key;
	}
	
	public static void saveEncOpcode(int inopcode) {
		if (isRecording) {
			enc_opcode = inopcode;
		}
	}
	
	public static void checkPoint(int opcode, int len) {
		if (input == null)
			return;
		
		if (isRecording) {
			// received packet 182, set flag, do not dump bytes
			if (opcode == 182) {
				// in here probably would need to check the position
				// don't care about the packet if 182, just rewrite it using the enc opcode
				try {
					input.writeInt(retained_timestamp);
					input.writeInt(retained_bread);
					retained_bytes[retained_off + 1] = (byte)127;
					retained_bytes[retained_off + 2] = 0;
					retained_bytes[retained_off + 3] = 0;
					retained_bytes[retained_off + 4] = 1;
					input.write(retained_bytes, retained_off, retained_bread);
					Logger.Info("Replay: Removed host block from client input");
				} catch (Exception e) {
					e.printStackTrace();
					shutdown_error();
				}
				retained_timestamp = TIMESTAMP_EOF;
				// free memory
				retained_bytes = null;
			}
		}
	}
	
	public static void patchClient() {
		// This is called from the client to apply fixes specific to replay
		// We only run this while playing replays
		if(!isPlaying)
			return;
		
		// The client doesn't remove friends during replay because they're removed client-side
		// Instead, lets increase the array size so we can still see added friends and not crash the client
		if (Client.friends_count == Client.friends.length) {
			int newLength = Client.friends.length + 200;
			Client.friends = Arrays.copyOf(Client.friends, newLength);
			Client.friends_world = Arrays.copyOf(Client.friends_world, newLength);
			Client.friends_formerly = Arrays.copyOf(Client.friends_formerly, newLength);
			Client.friends_online = Arrays.copyOf(Client.friends_online, newLength);
			Logger.Info("Replay.patchClient(): Applied friends list length patch to fix playback; newLength: " + newLength);
		}
		
		// The client doesn't remove ignores during replay because they're removed client-side
		// Instead, lets increase the array size so we can still see added ignores and not crash the client
		if (Client.ignores_count == Client.ignores.length) {
			int newLength = Client.ignores.length + 100;
			Client.ignores = Arrays.copyOf(Client.ignores, newLength);
			Client.ignores_formerly = Arrays.copyOf(Client.ignores_formerly, newLength);
			Client.ignores_copy = Arrays.copyOf(Client.ignores_copy, newLength);
			Client.ignores_formerly_copy = Arrays.copyOf(Client.ignores_formerly_copy, newLength);
			Logger.Info("Replay.patchClient(): Applied ignores list length patch to fix playback; newLength: " + newLength);
		}
	}
	
	public static void resetPatchClient() {
		// Resets all replay patching
		Client.friends_count = 0;
		Client.friends = new String[200];
		Client.friends_world = new String[200];
		Client.friends_formerly = new String[200];
		Client.friends_online = new int[200];
		
		Client.ignores_count = 0;
		Client.ignores = new String[100];
		Client.ignores_formerly = new String[100];
		Client.ignores_copy = new String[100];
		Client.ignores_formerly_copy = new String[100];
	}
	
}
