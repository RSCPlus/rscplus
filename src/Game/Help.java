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


public class Help
{
	public static void help(int page, String commandType) //this is for users who want to read the readme in game
	{
		if (page == 0) {
			Client.displayMessage("@whi@::help is a page based system.", Client.CHAT_QUEST);
			Client.displayMessage("@whi@Type \"::help text 1\" ~123~to get the first help page on text commands", Client.CHAT_QUEST);
			Client.displayMessage("@whi@Type \"::help keys 1\" ~123~to get the first help page on keyboard commands", Client.CHAT_QUEST);
			Client.displayMessage("@whi@You can go back in 'Quest history' to read pages that have disappeared.", Client.CHAT_QUEST);
		} else	{
			if (commandType.contentEquals("text")) { //list text commands e.g. ::bank
				switch (page) {
					case 1:
						Client.displayMessage("@whi@Page 1 of 4", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::[skillname] - Tells skill level and xp in chat", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::next_[skillname] - Tells how much xp needed until next level in chat", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::total - Shows total level and total xp in chat", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::fatigue - Shows the fatigue percentage accurately in chat", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::screenshot - Take screenshot (Saved in the screenshots directory)", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::toggleroofs - Toggle roof hiding", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglecolor - Toggle colored text in terminal", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglecombat - Toggle combat experience menu", Client.CHAT_QUEST);
						break;
					case 2:
						Client.displayMessage("@whi@Page 2 of 3", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglefatigue - Toggle fatigue alert at 100%", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::debug - Toggle debug mode", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglelogindetails - Toggle IP/Dns login details shown at welcome screen", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::toggletwitch - Toggle twitch chat hidden/shown", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::toggleiteminfo - Toggle Item info", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglenpcinfo - Toggle NPC info", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::toggleplayerinfo - Toggle Player info", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglehitbox - Toggle hitboxes for NPC info", Client.CHAT_QUEST);
						break;
					case 3:
						Client.displayMessage("@whi@Page 3 of 3", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglexpdrops - Toggle XP drops", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglefatiguedrops - Toggle fatigue drops", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::fov - Change FoV from 8-9", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::logout - Logout", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglefriendinfo - Toggle Player info for Friends only", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::toggleinvcount - Toggle the overlay of current inventory used", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::togglestatusdisplay - Toggle Hits/Prayer/Fatigue display", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::cmb - Breaks the character limit and @lre@may be bannable@whi@ especially if sent over PM to RS2/RS3. Displays combat stats like osbuddy's !cmb", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::cmbnocolor - Mimic's osbuddy's !cmb; doesn't use color codes, within character limits", Client.CHAT_QUEST);
						Client.displayMessage("@whi@::bank - brings up the bank interface anywhere", Client.CHAT_QUEST);
						break;
					default:
						Client.displayMessage("@whi@page does not exist", Client.CHAT_QUEST);
				}
			} else if (commandType.contentEquals("keys")) { //list key commands e.g. ctrl-h
				switch (page) {
					case 1:
						Client.displayMessage("@whi@Page 1 of 3", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] can be either Alt or Ctrl.", Client.CHAT_QUEST);
						Client.displayMessage("@whi@Middle mouse click - Rotate camera", Client.CHAT_QUEST);
						Client.displayMessage("@whi@Mouse wheel scroll - Zoom camera", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + s - Take screenshot (Saved in the screenshots directory)", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + r - Toggle roof hiding", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + c - Toggle combat experience menu", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + f - Toggle fatigue alert at 100%", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + e - Toggle inventory count overlay", Client.CHAT_QUEST);
						break;
					case 2:
						Client.displayMessage("@whi@Page 2 of 3", Client.CHAT_QUEST);	
						Client.displayMessage("@whi@[Command Key] + u - Toggle Hits/Prayer/Fatigue display", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + g - Toggle Friend info", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + d - Toggle debug mode", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + t - Toggle twitch chat hidden/shown", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + i - Toggle Item info", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + n - Toggle NPC info", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + p - Toggle Player info", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + h - Toggle hitboxes for NPC info", Client.CHAT_QUEST);
						break;
					case 3:
						Client.displayMessage("@whi@Page 3 of 3", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + [ - Toggle XP drops", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + ] - Toggle fatigue drops", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + l - Logout", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + z - Toggle colored chat text in terminal", Client.CHAT_QUEST);
						Client.displayMessage("@whi@[Command Key] + 1-5 - World switch on login screen", Client.CHAT_QUEST);
						break;
					default:
						Client.displayMessage("@whi@page does not exist", Client.CHAT_QUEST);
				}
			} else { //player specified a page number (argument 3) but misspelled "keys" or "text"
				Client.displayMessage("@whi@Misspelled command", Client.CHAT_QUEST);
				help(0,"help");
			}
		}				
	}
}
