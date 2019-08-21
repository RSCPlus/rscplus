/**
 * rscminus
 *
 * This file is part of rscminus.
 *
 * rscminus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * rscminus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with rscminus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * Authors: see <https://github.com/OrN/rscminus>
 */

package Replay.game;

public class PacketBuilder {
    public static final int OPCODE_QUEST_STATUS = 5;
    public static final int OPCODE_FLOOR_SET = 25;
    public static final int OPCODE_UPDATE_XP = 33;
    public static final int OPCODE_OBJECT_HANDLER = 48;
    public static final int OPCODE_PRIVACY_SETTINGS = 51;
    public static final int OPCODE_SET_INVENTORY = 53;
    public static final int OPCODE_SET_APPEARANCE = 59;
    public static final int OPCODE_CREATE_NPC = 79;
    public static final int OPCODE_SEND_PM = 87;
    public static final int OPCODE_SET_INVENTORY_SLOT = 90;
    public static final int OPCODE_WALLOBJECT_HANDLER = 91;
    public static final int OPCODE_GROUNDITEM_HANDLER = 99;
    public static final int OPCODE_UPDATE_NPC = 104;
    public static final int OPCODE_SET_IGNORE = 109;
    public static final int OPCODE_SKIP_TUTORIAL = 111;
    public static final int OPCODE_SET_FATIGUE = 114;
    public static final int OPCODE_RECV_PM = 120;
    public static final int OPCODE_REMOVE_INVENTORY_SLOT = 123;
    public static final int OPCODE_SEND_MESSAGE = 131;
    public static final int OPCODE_UPDATE_FRIEND = 149;
    public static final int OPCODE_SET_EQUIP_STATS = 153;
    public static final int OPCODE_SET_STATS = 156;
    public static final int OPCODE_UPDATE_STAT = 159;
    public static final int OPCODE_LOGOUT = 165;
    public static final int OPCODE_SHOW_WELCOME = 182;
    public static final int OPCODE_DENY_LOGOUT = 183;
    public static final int OPCODE_CREATE_PLAYERS = 191;
    public static final int OPCODE_PLAY_SOUND = 204;
    public static final int OPCODE_SET_PRAYERS = 206;
    public static final int OPCODE_UPDATE_PLAYERS = 234;
    public static final int OPCODE_UPDATE_IGNORE = 237;
    public static final int OPCODE_GAME_SETTINGS = 240;
}
