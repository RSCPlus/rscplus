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
	public boolean fetch(String jarURL)
	{
		Logger.Info("Fetching Jar: " + jarURL);

		try
		{
			JarInputStream in = new JarInputStream(Settings.getResourceAsStream(jarURL));
			Launcher.getInstance().setProgress(1, 1);

			JarEntry entry;
			while((entry = in.getNextJarEntry()) != null)
			{
				// Check if file is needed
				String name = entry.getName();

				// Read class to byte array
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				byte data[] = new byte[1024];
				int readSize;
				while((readSize = in.read(data, 0, data.length)) != -1)
					bOut.write(data, 0, readSize);
				byte classData[] = bOut.toByteArray();
				bOut.close();

				Logger.Info("Loading file: " + name);
				Launcher.getInstance().setStatus("Loading " + name + "...");

				if(name.endsWith(".class"))
				{
					name = name.substring(0, name.indexOf(".class"));
					classData = patchClass(classData);
					m_classData.put(name, classData);
				}
			}

			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private byte[] patchClass(byte data[])
	{
		return JClassPatcher.getInstance().patch(data);
	}

	@Override
	public final Class findClass(String name)
	{
		byte data[] = m_classData.get(name);
		if(data == null)
			return null;

		return defineClass(data, 0, data.length);
	}

	private Map<String, byte[]> m_classData = new HashMap<String, byte[]>();
}
