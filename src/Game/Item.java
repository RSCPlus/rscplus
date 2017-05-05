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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import Client.Logger;
import Client.Settings;

/**
 * This class defines items and provides a static method to patch item names as needed according to
 * {@link Settings#NAME_PATCH_TYPE}.
 */
public class Item {
	
	public static String[] item_name;
	
	public int x;
	public int y;
	public int width;
	public int height;
	public int id;
	
	public Item(int x, int y, int width, int height, int id) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.id = id;
	}
	
	/**
	 * Patches item names as specified by {@link Settings#NAME_PATCH_TYPE}.
	 */
	public static void patchItemNames() {
		int namePatchType = Settings.NAME_PATCH_TYPE;
		Connection c = null;
		
		try {
			Class.forName("org.sqlite.JDBC");
			
			// Check if running from a jar so you know where to look for the database
			if (new File("assets/itempatch.db").exists()) {
				c = DriverManager.getConnection("jdbc:sqlite:assets/itempatch.db");
			} else {
				c = DriverManager.getConnection("jdbc:sqlite::resource:assets/itempatch.db");
			}
			
			c.setAutoCommit(false);
			Logger.Info("Opened item name database successfully");
			
			switch (namePatchType) {
			case 1:
				queryDatabaseAndPatchItem(c, "SELECT item_id, patched_name FROM patched_names_type" + namePatchType + ";");
				break;
			case 2:
				queryDatabaseAndPatchItem(c, "SELECT item_id, patched_name FROM patched_names_type" + namePatchType + ";");
				queryDatabaseAndPatchItem(c,
						"SELECT item_id, patched_name FROM patched_names_type1 WHERE patched_names_type1.item_id NOT IN (SELECT item_id FROM patched_names_type2);");
				break;
			case 3:
				queryDatabaseAndPatchItem(c, "SELECT item_id, patched_name FROM patched_names_type" + namePatchType + ";");
				queryDatabaseAndPatchItem(c,
						"SELECT item_id, patched_name FROM patched_names_type1 WHERE patched_names_type1.item_id NOT IN (SELECT item_id FROM patched_names_type2) AND patched_names_type1.item_id NOT IN (SELECT item_id FROM patched_names_type3);");
				queryDatabaseAndPatchItem(c,
						"SELECT item_id, patched_name FROM patched_names_type2 WHERE patched_names_type2.item_id NOT IN (SELECT item_id FROM patched_names_type3);");
				break;
			case 0:
			default:
				break;
			}
			c.close();
			
		} catch (SQLTimeoutException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Queries an opened database and patches the items and names it returns.
	 * 
	 * @param c the connection with a specific database
	 * @param query a SQLite query statement
	 */
	public static void queryDatabaseAndPatchItem(Connection c, String query) {
		try {
			Statement stmt = null;
			
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				int itemID = rs.getInt("item_id");
				String patchedName = rs.getString("patched_name");
				item_name[itemID] = patchedName;
			}
			
			rs.close();
			stmt.close();
			
		} catch (SQLException e) {
			Logger.Error("Error patching item names from database values.");
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return item_name[id];
	}
	
	// need to override this for Collections.frequency over in Renderer.java -> SHOW_ITEMINFO to count duplicate-looking
	// items on ground correctly. without this, I believe it checks if location in memory is the same for both objects.
	@Override
	public boolean equals(Object b) {
		if (b != null) {
			if (b.getClass() == this.getClass()) {
				Item bItem = (Item)b;
				return this.x == bItem.x && this.y == bItem.y && this.id == bItem.id;
			} else {
				return false;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		// This is an acceptable hash since it's fine if two unequal objects have the same hash according to docs
		return this.x + this.y + this.width + this.height + this.id;
	}
	
}
