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

import Client.Launcher;
import Client.Logger;
import java.lang.reflect.Method;

public class Reflection
{
	public static void Load()
	{
		try
		{
			Class<?> c = Launcher.instance.classLoader.loadClass("client");

			Method[] methods = c.getDeclaredMethods();
			for(Method method : methods)
			{
				if(method.toGenericString().equals(DISPLAYMESSAGE))
				{
					displayMessage = method;
					Logger.Debug("Found displayMessage");
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static Method displayMessage = null;

	// Method descriptions
	private static final String DISPLAYMESSAGE = "private final void client.a(boolean,java.lang.String,int,java.lang.String,int,int,java.lang.String,java.lang.String)";
}
