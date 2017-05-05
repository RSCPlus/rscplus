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

/**
 * A simple logger
 */
public class Logger {
	
	private static final String[] m_logTypeName = { "DEBUG", " INFO", "ERROR" };
	
	public enum Type {
		DEBUG(0), INFO(1), ERROR(2);
		
		Type(int id) {
			this.id = id;
		}
		
		public int id;
	}
	
	public static void Log(Type type, String message) {
		if (!Settings.DEBUG && type == Type.DEBUG)
			return;
		
		String msg = "[" + m_logTypeName[type.id] + "] " + message;
		
		if (type != Type.ERROR)
			System.out.println(msg);
		else
			System.err.println(msg);
	}
	
	public static void Debug(String message) {
		Log(Type.DEBUG, message);
	}
	
	public static void Info(String message) {
		Log(Type.INFO, message);
	}
	
	public static void Error(String message) {
		Log(Type.ERROR, message);
	}
	
}
