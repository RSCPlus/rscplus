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

package Client;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32;

public class CacheDownload {
	private String m_serverAddress;
	private String m_file;
	private ByteBuffer m_data;
	private int m_crc;
	
	CacheDownload(String file) {
		// Randomize the world we download from to reduce load among users/downloads
		int world = 1 + (int)(4.99 * Math.random());
		m_serverAddress = "classic" + world + ".runescape.com";
		
		m_file = file;
		m_data = null;
		m_crc = 0;
	}
	
	public ByteBuffer getDataBuffer() {
		return m_data;
	}
	
	public int getCRC() {
		return m_crc;
	}
	
	public boolean dump(String fname) {
		try {
			FileOutputStream stream = new FileOutputStream(fname);
			FileChannel out = stream.getChannel();
			out.write(m_data);
			out.close();
			stream.close();
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public boolean fetch(Launcher launcher) {
		launcher.setProgress(0, 1);
		
		try {
			URL url = new URL("http://" + m_serverAddress + m_file);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			int contentLength = connection.getContentLength();
			InputStream input = url.openStream();
			
			m_data = ByteBuffer.allocate(contentLength);
			int readSize = 0;
			int offset = 0;
			while ((readSize = input.read(m_data.array(), offset, m_data.capacity() - offset)) >= 0) {
				offset += readSize;
				launcher.setProgress(offset, contentLength);
			}
			input.close();
			
			if (contentLength != offset) {
				return false;
			}
			
			CRC32 crc = new CRC32();
			crc.update(m_data.array());
			m_crc = (int)crc.getValue();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
