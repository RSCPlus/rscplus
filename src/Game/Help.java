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

/**
 * Generates a version of the README in a format suitable for displaying through in-game chat.
 */
public class Help {
	
	/**
	 * Generates a version of the README in a format suitable for displaying through in-game chat.
	 * 
	 * @param page the page of the help menu to display
	 * @param commandType the type of commands to be displayed
	 */
	public static void help(int page, String commandType) // this is for users who want to read the readme in game
	{
		if (page == 0) {
			Client.displayMessage("@whi@::help is a page based system.", Client.CHAT_QUEST);
			Client.displayMessage("@whi@Type \"::help chats 1\" to get the first help page on quickchat commands", Client.CHAT_QUEST);
			Client.displayMessage("@whi@Type \"::help settings 1\" to get the first help page on commands which change settings", Client.CHAT_QUEST);
			Client.displayMessage("@whi@Type \"::help misc 1\" to get the first help page for all other commands", Client.CHAT_QUEST);
			Client.displayMessage("@whi@You can go back in 'Quest history' to read pages that have disappeared.", Client.CHAT_QUEST);
		} else {
			if (commandType.contentEquals("settings")) { // list text commands e.g. ::bank
				switch (page) {
				case 1:
					Client.displayMessage("@whi@Page 1 of 3", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::debug - Toggle debug mode", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::fov - Change FoV from 8-9", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglecolor - Toggle colored text in terminal", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglecombat - Toggle combat experience menu", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglefatigue - Toggle fatigue alert at 100%", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglefatiguedrops - Toggle fatigue drops", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglefriendinfo - Toggle Player info for Friends only", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglehitbox - Toggle hitboxes for NPC info", Client.CHAT_QUEST);
					break;
				case 2:
					Client.displayMessage("@whi@Page 2 of 3", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::toggleinvcount - Toggle the overlay of current inventory used", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::toggleiteminfo - Toggle Item info", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglelogindetails - Toggle IP/Dns login details shown at welcome screen", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglenpcinfo - Toggle NPC info", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::toggleplayerinfo - Toggle Player info", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::toggleroofs - Toggle roof hiding", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglestatusdisplay - Toggle Hits/Prayer/Fatigue display", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::toggletwitch - Toggle twitch chat hidden/shown", Client.CHAT_QUEST);
					break;
				case 3:
					Client.displayMessage("@whi@Page 3 of 3", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::togglexpdrops - Toggle XP drops", Client.CHAT_QUEST);
					break;
				default:
					Client.displayMessage("@whi@page does not exist", Client.CHAT_QUEST);
				}
			} else if (commandType.contentEquals("chats")) { // list commands which type chat messages e.g. ::cmb
				switch (page) {
				case 1:
					Client.displayMessage("@whi@Page 1 of 2", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::[skillname] - Tells skill level and xp in chat", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::next_[skillname] - Tells how much xp needed until next level in chat", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::total - Shows total level and total xp in chat", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::fatigue - Shows the fatigue percentage accurately in chat", Client.CHAT_QUEST);
					Client.displayMessage(
							"@whi@::cmb - Breaks the character limit and @lre@may be bannable@whi@ especially if sent over PM to RS2/RS3. Displays combat stats like osbuddy's !cmb",
							Client.CHAT_QUEST);
					Client.displayMessage("@whi@::cmbnocolor - Mimics osbuddy's !cmb; doesn't use color codes, within character limits", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::bank - brings up the bank interface anywhere", Client.CHAT_QUEST);
					break;
				case 2:
					Client.displayMessage("@whi@Page 2 of 2", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::xmas - Formats your message in festive Red Green and White colors", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::rainbow - Formats your message in all colors of the rainbow (or at least 6)", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::system - Hides your username in chat and then outputs your message", Client.CHAT_QUEST);
					break;
				default:
					Client.displayMessage("@whi@page does not exist", Client.CHAT_QUEST);
				}
			} else if (commandType.contentEquals("misc")) { // list miscellaneous commands
				switch (page) {
				case 1:
					Client.displayMessage("@whi@Page 1 of 1", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::logout - Logout", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::screenshot - Take screenshot (Saved in the screenshots directory)", Client.CHAT_QUEST);
					Client.displayMessage("@whi@::update - manually check if RSC+ has updated", Client.CHAT_QUEST);
					Client.displayMessage("@whi@Middle mouse click - Rotate camera", Client.CHAT_QUEST);
					Client.displayMessage("@whi@Mouse wheel scroll - Zoom camera", Client.CHAT_QUEST);
					break;
				default:
					Client.displayMessage("@whi@page does not exist", Client.CHAT_QUEST);
				}
			} else { // player specified a page number (argument 3) but misspelled "chats", "settings", or "misc"
				Client.displayMessage("@whi@Misspelled command", Client.CHAT_QUEST);
				help(0, "help");
			}
		}
	}
}
