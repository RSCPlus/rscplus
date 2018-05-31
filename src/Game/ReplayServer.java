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
	
	public boolean isDone = false;
	public long size = 0;
	public long available = 0;
	
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
			
			Logger.Info("ReplayServer: Waiting for client...");
			
			sock = ServerSocketChannel.open();
			// last attempt 10 + default port
			usePort = port == -1 ? Replay.DEFAULT_PORT + 10 : port;
			if (usePort != Replay.DEFAULT_PORT) {
				Replay.changePort(usePort);
			}
			sock.bind(new InetSocketAddress(usePort));
			client = sock.accept();
			client.configureBlocking(false);
			
			Logger.Info("ReplayServer: Starting playback");
			
			isDone = false;
			while (!isDone) {
				if (!Replay.paused) {
					if (!doTick()) {
						isDone = true;
					}
				}
				
				Thread.sleep(1);
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
			
			long frame_timer = System.currentTimeMillis() + Replay.getFrameTimeSlice();
			int diff = timestamp_input - Replay.timestamp;
			
			// If the timestamp is 300+ frames in the future, it's a client disconnection
			// So we disconnect and reconnect the client
			if (diff > 300) {
				Logger.Info("ReplayServer: Killing client connection; timestamp=" + Replay.timestamp);
				client.close();
				client = sock.accept();
				client.configureBlocking(false);
				diff -= 300;
				Replay.timestamp = timestamp_input - Integer.max(diff, 0);
				Logger.Info("ReplayServer: Reconnected client; timestamp=" + Replay.timestamp);
			}
			
			while (Replay.timestamp < timestamp_input) {
				long time = System.currentTimeMillis();
				if (time >= frame_timer) {
					frame_timer = time + Replay.getFrameTimeSlice();
					Replay.incrementTimestamp();
				}
				
				// Don't hammer the cpu
				Thread.sleep(1);
			}
			
			// Read from client, but don't do anything with the data
			while (client.read(readBuffer) > 0) {
				readBuffer.clear();
			}
			
			client.write(buffer);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
