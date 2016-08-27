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

import Game.Client;

public class Logger
{
	public enum Type
	{
		DEBUG(0),
		INFO(1),
		WARNING(2),
		ERROR(3);

		Type(int id)
		{
			this.id = id;
		}

		public int id;
	};

	public static void Log(Type type, String message)
	{
		// Don't print debug messages if debug mode is disabled
		if(!Settings.DEBUG && type == Type.DEBUG)
			return;

		String msg = "[" + m_logTypeName[type.id] + "] " + message;

		if(type == Type.ERROR)
			System.err.println(msg);
		else
			System.out.println(msg);
	}

	public static void Debug(String message)
	{
		Log(Type.DEBUG, message);
	}

	public static void Info(String message)
	{
		Log(Type.INFO, message);
	}

	public static void Warning(String message)
	{
		Log(Type.WARNING, message);
	}

	public static void Error(String message)
	{
		Log(Type.ERROR, message);
	}

	private static final String m_logTypeName[] =
	{
		"  DEBUG",
		"   INFO",
		"WARNING",
		"  ERROR"
	};
}
