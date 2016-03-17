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

public class Item
{
	public Item(int x, int y, int width, int height, int id)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.id = id;
	}

	public static void patchItemNames()
	{

		for(int i = 0; i < item_name.length; i++)
		{
			if(i == 165)
				item_name[i] = item_name[444] + " (unidentified)";
			else if(i >= 435 && i <= 443)
				item_name[i] = item_name[i + 10] + " (unidentified)";
			else if(i == 454)
				item_name[i] = "Guam potion";
			else if(i == 455)
				item_name[i] = "Marrentill potion";
			else if(i == 456)
				item_name[i] = "Tarromin potion";
			else if(i == 457)
				item_name[i] = "Harralander potion";
			else if(i == 458)
				item_name[i] = "Ranarr potion";
			else if(i == 459)
				item_name[i] = "Irit potion";
			else if(i == 460)
				item_name[i] = "Avantoe potion";
			else if(i == 461)
				item_name[i] = "Kwuarm potion";
			else if(i == 462)
				item_name[i] = "Cadantine potion";
			else if(i == 463)
				item_name[i] = "Dwarfweed potion";
			else if(i == 464)
				item_name[i] = "Vial of water";
			else if(i == 815)
				item_name[i] = item_name[816] + " (unidentified)";
			else if(i == 817)
				item_name[i] = item_name[818] + " (unidentified)";
			else if(i == 819)
				item_name[i] = item_name[820] + " (unidentified)";
			else if(i == 821)
				item_name[i] = item_name[822] + " (unidentified)";
			else if(i == 823)
				item_name[i] = item_name[824] + " (unidentified)";
			else if(i == 933)
				item_name[i] = item_name[934] + " (unidentified)";
			else if(i == 935)
				item_name[i] = "Torstol potion";
			else if(i == 221) // Strength Potion
				item_name[i] = item_name[i] + " (4)";
			else if(i == 222)
				item_name[i] = item_name[i] + " (3)";
			else if(i == 223)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 224)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 474) // attack Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 475)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 476)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 477) // stat restoration Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 478)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 479)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 480) // defense Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 481)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 482)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 483) // restore prayer Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 484)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 485)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 486) // Super attack Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 487)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 488)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 489) // fishing Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 490)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 491)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 492) // Super strength Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 493)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 494)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 495) // Super defense Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 496)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 497)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 498) // ranging Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 499)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 500)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 566) // Cure poison Potion
				item_name[i] = item_name[i] + " (3)";
			else if(i == 567)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 568)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 569) // Poison antidote
				item_name[i] = item_name[i] + " (3)";
			else if(i == 570)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 571)
				item_name[i] = item_name[i] + " (1)";
			else if(i == 963) // Potion of Zamorak
				item_name[i] = item_name[i] + " (3)";
			else if(i == 964)
				item_name[i] = item_name[i] + " (2)";
			else if(i == 965)
				item_name[i] = item_name[i] + " (1)";
		}
	}

	public String getName()
	{
		return item_name[id];
	}

	public int x;
	public int y;
	public int width;
	public int height;
	public int id;

	public static String item_name[];
}
