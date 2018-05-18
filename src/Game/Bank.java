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

import Client.Settings;

public class Bank {
	
	/**
	 * This method resets the query flag and "exclude inventory-only item" flag, it sets up first time visiting bank
	 * flag
	 */
	public static void openedBankInterfaceHook() {
		Client.new_bank_items[253] = 0;
		Client.new_bank_items[254] = 1;
		Client.new_bank_items[255] = 0;
	}
	
	/**
	 * This method removes the inventory only items shown in bank, if user is in "searchable bank" mode
	 * otherwise it enters weird glitch mode as user does operations of withdraw/deposit on items
	 */
	public static void finalBankItemsHook() {
		if (Client.new_bank_items[253] != 0) {
			// remove inventory only items shown at bank, if its in searchable bank mode
			// to remove some glitches
			for (int i = Client.new_count_items_bank; i < 255; i++) {
				Client.bank_items[i] = 0;
				Client.bank_items_count[i] = 0;
				Client.new_bank_items[i] = 0;
				Client.new_bank_items_count[i] = 0;
			}
			Client.bank_items[255] = 0;
			Client.bank_items_count[255] = 0;
			Client.count_items_bank = Client.new_count_items_bank;
		}
	}
	
	/**
	 * This method hooks update bank
	 * new_bank_items = bank_items
	 * flags: new_bank_items[253] -> indicates if 'inventory only' items appearing at bank interface should be removed
	 * new_bank_items[254] -> indicates user first time opened the interface
	 * new_bank_items[255] -> indicates user is in "searchable bank" mode
	 */
	public static void updateBankItemsHook() {
		if (Client.new_bank_items[254] == 1) {
			// first time bank interface, unset the flag
			Client.new_bank_items[254] = 0;
			if (Settings.START_SEARCHEDBANK && !Settings.SEARCH_BANK_WORD.equals("")) {
				int[] tmpBankItems = Client.bank_items.clone();
				int[] tmpBankItemsCount = Client.bank_items_count.clone();
				int[] tmpNewBankItems = Client.new_bank_items.clone();
				int[] tmpNewBankItemsCount = Client.new_bank_items_count.clone();
				// clear everything to avoid error
				for (int i = 0; i < Client.bank_items.length; i++) {
					Client.bank_items[i] = 0;
					Client.bank_items_count[i] = 0;
					Client.new_bank_items[i] = 0;
					Client.new_bank_items_count[i] = 0;
					Client.count_items_bank = 0;
					Client.new_count_items_bank = 0;
				}
				int n = 0;
				// place only those items matching the criteria
				for (int i = 0; i < tmpNewBankItems.length; i++) {
					if (tmpNewBankItemsCount[i] == 0)
						break;
					if (Item.item_name[tmpNewBankItems[i]].toLowerCase().contains(Settings.SEARCH_BANK_WORD.toLowerCase())) {
						Client.bank_items[n] = tmpBankItems[i];
						Client.bank_items_count[n] = tmpBankItemsCount[i];
						Client.new_bank_items[n] = tmpNewBankItems[i];
						Client.new_bank_items_count[n] = tmpNewBankItemsCount[i];
						n++;
					}
				}
				
				Client.count_items_bank = n;
				Client.new_count_items_bank = n;
				// in searchable bank, setting this flag also to indicate to remove inventory only items
				Client.new_bank_items[253] = 1;
				Client.new_bank_items[255] = 1;
			}
		} else {
			if (Client.new_bank_items[255] == 1) {
				// this flag needs to be unset itself because it can cause noise when detecting changed item
				Client.new_bank_items[255] = 0;
				// only shown items saved in bank = queried bank
				boolean isQueried = true;
				int array_len = 256;
				for (int l = Client.new_count_items_bank; l < array_len; l++) {
					if (Client.bank_items[l] != 0 && Client.bank_items_count[l] == 0) {
						isQueried = false;
						break;
					}
				}
				if (isQueried) {
					try {
						// fix updating bank interface from deposit/withdraw after querying bank
						
						// need to detect changed item
						int posChanged, itemIdChanged, itemCountChanged;
						itemIdChanged = itemCountChanged = posChanged = -1;
						
						for (int i = 0; i < Client.new_count_items_bank; i++) {
							if (Client.new_bank_items[i] != Client.bank_items[i] && !(Client.new_bank_items[i] == 0 && Client.new_bank_items_count[i] == 0)) {
								if (Client.new_bank_items[i] == Client.bank_items[i + 1] && Client.new_bank_items_count[i] == Client.bank_items_count[i + 1])
									continue;
								
								itemIdChanged = Client.new_bank_items[i];
								itemCountChanged = Client.new_bank_items_count[i];
								posChanged = i;
								
								break;
							}
						}
						if (posChanged != -1) {
							// update changed item onto tempBankItems
							for (int i = 0; i < Client.new_count_items_bank; i++) {
								// did not withdraw all, can also be deposit some
								if (Client.bank_items[i] == itemIdChanged && Client.bank_items_count[i] != 0 && itemCountChanged > 0) {
									Client.new_bank_items_count[i] = itemCountChanged;
									if (Client.bank_items_count[posChanged] != 0 && Client.bank_items[posChanged] != 0) {
										Client.new_bank_items[posChanged] = Client.bank_items[posChanged];
										Client.new_bank_items_count[posChanged] = Client.bank_items_count[posChanged];
									} else {
										Client.new_bank_items[posChanged] = Client.bank_items[posChanged];
										Client.new_bank_items_count[posChanged] = Client.bank_items_count[posChanged];
									}
									break;
								}
							}
							
							// cleanup (might be redundant) but better safe
							int[] tmpNewItems = new int[array_len];
							int[] tmpNewItemsCount = new int[array_len];
							int n = 0;
							for (int i = 0; i < array_len; i++) {
								if (!(Client.new_bank_items[i] == 0 && Client.new_bank_items_count[i] == 0)) {
									tmpNewItems[n] = Client.new_bank_items[n];
									tmpNewItemsCount[n] = Client.new_bank_items_count[n];
									n++;
								}
							}
							
							Client.new_bank_items = tmpNewItems;
							Client.new_bank_items_count = tmpNewItemsCount;
							Client.new_count_items_bank = n;
							Client.count_items_bank = n;
						} else {
							// Could not detect which position changed since new_bank_items are exactly the same
							// as bank_items -> check inventory items until match.
							// if not ??
							
							int i, j;
							i = j = 0;
							for (i = 0; i < Client.inventory_count; i++) {
								for (j = 0; j < Client.new_count_items_bank; j++) {
									if (Client.inventory_items[i] == Client.new_bank_items[j]) {
										posChanged = j;
										break;
									}
								}
							}
							if (posChanged != -1) {
								// move elements up from index to fix glitch
								for (i = posChanged; i < Client.new_count_items_bank; i++) {
									Client.new_bank_items[i] = Client.new_bank_items[i + 1];
									Client.new_bank_items_count[i] = Client.new_bank_items_count[i + 1];
								}
								Client.new_bank_items[i] = Client.new_bank_items[i - 1];
								Client.new_bank_items_count[i] = Client.new_bank_items_count[i - 1];
							}
						}
						
						// in searchable bank, setting this flag also to indicate to remove inventory only items
						Client.new_bank_items[253] = 1;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// all operations finished, set searchable bank mode back on
				Client.new_bank_items[255] = 1;
			}
		}
	}
	
	/**
	 * Entry point, since the same command array is used from Client.processClientCommand, index starts with 1
	 * (validated before)
	 * 
	 * @param cmdArray the command array used on Client.processClientCommand
	 * @param help display help on command
	 */
	public static void search(String[] cmdArray, boolean help) {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < cmdArray.length; i++) {
			if (cmdArray[i].trim().equals(""))
				continue;
			sb.append(cmdArray[i].trim().toLowerCase());
			if (i < cmdArray.length - 1)
				sb.append(" ");
		}
		bankSearch(sb.toString(), help);
	}
	
	/**
	 * Filters out current bank state to match items with given keyword
	 * 
	 * @param search The complete clean search query to do the search
	 * @param help display help on command
	 */
	public static void bankSearch(String search, boolean help) {
		if (search.trim().equals("") || help) {
			Client.displayMessage("@whi@::banksearch is a searchable bank mode", Client.CHAT_QUEST);
			Client.displayMessage("@whi@Type \"::banksearch [aString]\" to search banked items with query string aString", Client.CHAT_QUEST);
			Client.displayMessage("@whi@Bank is updated to show only matched items.", Client.CHAT_QUEST);
			Client.displayMessage("@whi@The command stores the query string used", Client.CHAT_QUEST);
			Client.displayMessage("@whi@To exit the mode, speak to the banker again.", Client.CHAT_QUEST);
		} else {
			// not in bank, display notice
			if (!Client.show_bank) {
				Client.displayMessage("@whi@::banksearch is only available when bank interface is open", Client.CHAT_QUEST);
			} else {
				// overwrite query string on local config
				Settings.SEARCH_BANK_WORD = search;
				Settings.save();
				int[] tmpBankItems = Client.bank_items.clone();
				int[] tmpBankItemsCount = Client.bank_items_count.clone();
				int[] tmpNewBankItems = Client.new_bank_items.clone();
				int[] tmpNewBankItemsCount = Client.new_bank_items_count.clone();
				// clear everything to avoid error
				for (int i = 0; i < Client.bank_items.length; i++) {
					Client.bank_items[i] = 0;
					Client.bank_items_count[i] = 0;
					Client.new_bank_items[i] = 0;
					Client.new_bank_items_count[i] = 0;
					Client.count_items_bank = 0;
					Client.new_count_items_bank = 0;
				}
				int n = 0;
				// place only those items matching the criteria
				for (int i = 0; i < tmpNewBankItems.length; i++) {
					if (tmpBankItemsCount[i] == 0)
						break;
					if (Item.item_name[tmpBankItems[i]].toLowerCase().contains(search.toLowerCase())) {
						Client.bank_items[n] = tmpBankItems[i];
						Client.bank_items_count[n] = tmpBankItemsCount[i];
						Client.new_bank_items[n] = tmpNewBankItems[i];
						Client.new_bank_items_count[n] = tmpNewBankItemsCount[i];
						n++;
					}
				}
				
				Client.count_items_bank = n;
				Client.new_count_items_bank = n;
				Client.new_bank_items[255] = 1;
			}
		}
	}
	
	/**
	 * Bank search without modifying positions, just lists out banked elements and where they are located
	 * 
	 * @param search The complete clean search query to do the search
	 * @param help display help on command
	 */
	public static void query(String search, boolean help) {
		if (search.trim().equals("") || help) {
			Client.displayMessage("@whi@::querybank is a top-10 based system.", Client.CHAT_QUEST);
			Client.displayMessage("@whi@Type \"::querybank [aString]\" to search banked items with query string aString", Client.CHAT_QUEST);
			Client.displayMessage("@whi@The command stores the query string used", Client.CHAT_QUEST);
			Client.displayMessage("@whi@You can go back in 'Quest history' to read pages that have disappeared.", Client.CHAT_QUEST);
		} else {
			// not in bank, display notice
			if (!Client.show_bank) {
				Client.displayMessage("@whi@::querybank is only available when bank interface is open", Client.CHAT_QUEST);
			} else {
				// overwrite query string on local config
				Settings.SEARCH_BANK_WORD = search;
				Settings.save();
				int page, row, col, tmp;
				Client.displayMessage("@whi@" + "Queried bank with '" + search + "'", Client.CHAT_QUEST);
				for (int i = 0; i < Client.bank_items.length; i++) {
					if (Client.bank_items_count[i] == 0)
						break;
					if (Item.item_name[Client.bank_items[i]].toLowerCase().contains(search.toLowerCase())) {
						page = i / 48;
						tmp = i - 48 * page;
						page++;
						row = (tmp / 8) + 1;
						col = (tmp % 8) + 1;
						Client.displayMessage(
								"@whi@" + " " + Item.item_name[Client.bank_items[i]] + " (" + pluralize(Client.bank_items_count[i]) + ")" + " at Page " + page + ", Row " + row
										+ ", Column " + col,
								Client.CHAT_QUEST);
					}
				}
			}
		}
	}
	
	public static String pluralize(int count) {
		if (count == 1)
			return count + " pc";
		else
			return count + " pcs";
	}
	
}
