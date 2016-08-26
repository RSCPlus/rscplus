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

package rscplus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;

public class JClassLoader extends ClassLoader
{
	public JClassLoader()
	{
		super(rscplus.class.getClassLoader());
	}

	public boolean fetch(URL jarURL)
	{
		Logger.Info("Fetching Jar: " + jarURL);

		try
		{
			JarInputStream in = new JarInputStream(jarURL.openConnection().getInputStream());
			int totalNeeded = 0;
			int totalLoaded = 0;

			// Grab class info
			Map<String, String> filesNeeded = new HashMap<String, String>();
			Manifest manifest = in.getManifest();
			for(Map.Entry<String, Attributes> entry : manifest.getEntries().entrySet())
			{
				String name = entry.getKey();
				String hash = entry.getValue().getValue("SHA1-Digest");

				// Decode Base64
				byte[] hashDecoded = DatatypeConverter.parseBase64Binary(hash);
				hash = Util.byteHexString(hashDecoded);
				String fname = Settings.Dir.CACHE + "/" + hash;

				File cacheFile = new File(fname);
				if(cacheFile.exists())
				{
					FileInputStream fIn = new FileInputStream(cacheFile);

					MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

					ByteArrayOutputStream bOut = new ByteArrayOutputStream();
					byte data[] = new byte[1024];
					int readSize;
					while((readSize = fIn.read(data, 0, data.length)) > 0)
					{
						sha1.update(data, 0, readSize);
						bOut.write(data, 0, readSize);
					}
					fIn.close();

					byte classData[] = bOut.toByteArray();
					bOut.close();

					String newHash = Util.byteHexString(sha1.digest());
					if(newHash.equals(hash))
					{
						if(name.endsWith(".class"))
						{
							Logger.Info("Found cached file: " + name);
							rscplus.getInstance().setStatus("loading cached file " + name + "...");
							name = name.substring(0, name.indexOf(".class"));
							classData = patchClass(classData);
							m_classData.put(name, classData);
						}
						totalLoaded += 1;
					}
					else
					{
						filesNeeded.put(name, fname);
					}
				}
				else
				{
					filesNeeded.put(name, fname);
				}

				totalNeeded += 1;
				rscplus.getInstance().setProgress(totalLoaded, totalNeeded);
			}

			JarEntry entry;
			while(filesNeeded.size() > 0 && (entry = in.getNextJarEntry()) != null)
			{
				// Check if file is needed
				String name = entry.getName();
				String fname = filesNeeded.get(name);
				if(fname == null)
					continue;

				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				byte data[] = new byte[1024];
				int readSize;
				while((readSize = in.read(data, 0, data.length)) != -1)
					bOut.write(data, 0, readSize);

				byte classData[] = bOut.toByteArray();
				bOut.close();

				FileOutputStream fOut = new FileOutputStream(fname);
				fOut.write(classData, 0, classData.length);
				fOut.close();

				Logger.Info("Retrieved file: " + name);
				rscplus.getInstance().setStatus("downloading " + name + "...");

				if(name.endsWith(".class"))
				{
					filesNeeded.remove(name);
					name = name.substring(0, name.indexOf(".class"));
					classData = patchClass(classData);
					m_classData.put(name, classData);
				}

				totalLoaded += 1;
				rscplus.getInstance().setProgress(totalLoaded, totalNeeded);
			}

			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}

		rscplus.getInstance().setStatus("launching game");

		return true;
	}

	private byte[] patchClass(byte data[])
	{
		return JClassPatcher.getInstance().patch(data);
	}

	@Override
	public final Class findClass(String name)
	{
		Logger.Debug("ClassLoader findClass: " + name);

		byte data[] = m_classData.get(name);
		if(data == null)
			return null;

		return defineClass(data, 0, data.length);
	}

	private Map<String, byte[]> m_classData = new HashMap<String, byte[]>();
}
