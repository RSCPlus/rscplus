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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.zip.GZIPInputStream;
import Client.Logger;

public class ReplayServer implements Runnable {
	String playbackDirectory;
	DataInputStream input = null;
	FileInputStream file_input = null;
	ServerSocketChannel sock = null;
	SocketChannel client = null;
	ByteBuffer readBuffer = null;
	long frame_timer = 0;
	
	public boolean isReady = false;
	public boolean isDone = false;
	public long size = 0;
	public long available = 0;
	public int timestamp_end = 0;
	
	ReplayServer(String directory) {
		playbackDirectory = directory;
		readBuffer = ByteBuffer.allocate(1024);
	}
	
	public int getPercentRemaining() {
		try {
			return (int)(available * 100 / size);
		} catch (Exception e) {
		}
		return 0;
	}
	
	public int getReplayEnding(File file) {
		int timestamp_ret = 0;
		
		try {
			DataInputStream fileInput = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
			for(;;) {
				int timestamp_input = fileInput.readInt();
				
				// EOF
				if (timestamp_input == -1)
					break;
				
				// Skip data, we need to find the last timestamp
				int length = fileInput.readInt();
				int skipped = fileInput.skipBytes(length);
				
				if (skipped != length)
					break;
					
				timestamp_ret = timestamp_input;
			}
			fileInput.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		
		return timestamp_ret;
	}
	
	@Override
	public void run() {
		sock = null;
		// this one will try to find open port
		int port = -1;
		int usePort;
		// attempt to find free port starting from default port
		for (int i = 0; i < 10; i++) {
			try {
				new ServerSocket(Replay.DEFAULT_PORT + i).close();
				port = Replay.DEFAULT_PORT + i;
				break;
			} catch (IOException e) {
				continue;
			}
		}
		
		try {
			File file = new File(playbackDirectory + "/in.bin.gz");
			size = file.length();
			file_input = new FileInputStream(file);
			input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(file_input)));
			timestamp_end = getReplayEnding(file);
			
			Logger.Info("ReplayServer: Replay loaded, waiting for client; length=" + timestamp_end);
			sock = ServerSocketChannel.open();
			// last attempt 10 + default port
			usePort = port == -1 ? Replay.DEFAULT_PORT + 10 : port;
			if (usePort != Replay.DEFAULT_PORT) {
				Replay.changePort(usePort);
			}
			sock.bind(new InetSocketAddress(usePort));
			
			// Let's connect our client
			Logger.Info("ReplayServer: Syncing playback to client...");
			isReady = true;
			client = sock.accept();
			
			Logger.Info("ReplayServer: Starting playback; port=" + usePort);
			
			isDone = false;
			frame_timer = System.currentTimeMillis();
			while (!isDone) {
				if (!Replay.paused) {
					if (!doTick()) {
						isDone = true;
					}
				} else {
					// Update timestamp immediately on unpausing
					frame_timer = System.currentTimeMillis();
					Thread.sleep(1);
				}
			}
			
			client.close();
			sock.close();
			input.close();
			Logger.Info("ReplayServer: Playback has finished");
		} catch (Exception e) {
			if (sock != null) {
				try {
					sock.close();
					client.close();
					input.close();
				} catch (Exception e2) {
				}
			}
			
			isReady = true;
			e.printStackTrace();
			Logger.Error("ReplayServer: Failed to serve replay");
		}
	}
	
	public boolean doTick() {
		try {
			int timestamp_input = input.readInt();
			
			// We've reached the end of the replay
			if (timestamp_input == -1)
				return false;
			
			int length = input.readInt();
			ByteBuffer buffer = ByteBuffer.allocate(length);
			input.read(buffer.array());
			available = file_input.available();
			
			int timestamp_diff = timestamp_input - Replay.timestamp;
			
			// If the timestamp is 300+ frames in the future, it's a client disconnection
			// So we disconnect and reconnect the client
			if (timestamp_diff > 300) {
				Logger.Info("ReplayServer: Killing client connection; timestamp=" + Replay.timestamp);
				client.close();
				client = sock.accept();
				timestamp_diff -= 300;
				Replay.timestamp = timestamp_input - timestamp_diff;
				Logger.Info("ReplayServer: Reconnected client; timestamp=" + Replay.timestamp);
			}
			
			// Synchronize the server to input
			while (Replay.timestamp < timestamp_input) {
				long time = System.currentTimeMillis();
				if (time >= frame_timer) {
					frame_timer += Replay.getFrameTimeSlice();
					Replay.incrementTimestamp();
				}
				
				// Don't hammer the cpu, unless we have to
				long sleepTime = frame_timer - System.currentTimeMillis() - 1;
				if (sleepTime > 0)
					Thread.sleep(sleepTime);
			}
			
			// Write out replay data to the client
			client.write(buffer);
			
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
		}
		
		return false;
	}
}
