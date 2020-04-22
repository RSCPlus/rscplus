/**
 * rscminus
 *
 * <p>This file is part of rscminus.
 *
 * <p>rscminus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscminus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscminus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/RSCPlus/rscminus>
 */
package Replay.game;

public class PacketBuilder {
  public static final int OPCODE_QUEST_STATUS = 5;
  public static final int OPCODE_UPDATE_STAKED_ITEMS_OPPONENT = 6;
  public static final int OPCODE_UPDATE_TRADE_ACCEPTANCE = 15;
  public static final int OPCODE_SHOW_CONFIRM_TRADE = 20;
  public static final int OPCODE_FLOOR_SET = 25;
  public static final int OPCODE_SYNC_DUEL_SETTINGS = 30;
  public static final int OPCODE_UPDATE_XP = 33;
  public static final int OPCODE_DISPLAY_TELEPORT_TELEGRAB_BUBBLE = 36;
  public static final int OPCODE_OPEN_BANK = 42;
  public static final int OPCODE_OBJECT_HANDLER = 48;
  public static final int OPCODE_PRIVACY_SETTINGS = 51;
  public static final int OPCODE_UPDATE_SYSTEM_UPDATE_TIMER = 52;
  public static final int OPCODE_SET_INVENTORY = 53;
  public static final int OPCODE_SHOW_APPEARANCE_CHANGE = 59;
  public static final int OPCODE_CREATE_NPC = 79;
  public static final int OPCODE_DISPLAY_DEATH_SCREEN = 83;
  public static final int OPCODE_WAKE_UP = 84;
  public static final int OPCODE_SEND_PM = 87;
  public static final int OPCODE_SHOW_DIALOGUE_SERVER_MESSAGE_NOT_TOP = 89;
  public static final int OPCODE_SET_INVENTORY_SLOT = 90;
  public static final int OPCODE_WALLOBJECT_HANDLER = 91;
  public static final int OPCODE_INITIATE_TRADE = 92;
  public static final int OPCODE_UPDATE_ITEMS_TRADED_TO_YOU = 97;
  public static final int OPCODE_GROUNDITEM_HANDLER = 99;
  public static final int OPCODE_SHOW_SHOP = 101;
  public static final int OPCODE_UPDATE_NPC = 104;
  public static final int OPCODE_SET_IGNORE = 109;
  public static final int OPCODE_SKIP_TUTORIAL = 111;
  public static final int OPCODE_SET_FATIGUE = 114;
  public static final int OPCODE_FALL_ASLEEP = 117;
  public static final int OPCODE_RECEIVE_PM = 120;
  public static final int OPCODE_REMOVE_INVENTORY_SLOT = 123;
  public static final int OPCODE_CONCLUDE_TRADE = 128;
  public static final int OPCODE_SEND_MESSAGE = 131;
  public static final int OPCODE_EXIT_SHOP = 137;
  public static final int OPCODE_UPDATE_FRIEND = 149;
  public static final int OPCODE_SET_EQUIP_STATS = 153;
  public static final int OPCODE_SET_STATS = 156;
  public static final int OPCODE_UPDATE_STAT = 159;
  public static final int OPCODE_UPDATE_TRADE_RECIPIENT_ACCEPTANCE = 162;
  public static final int OPCODE_LOGOUT = 165;
  public static final int OPCODE_SHOW_CONFIRM_DUEL = 172;
  public static final int OPCODE_SHOW_DIALOGUE_DUEL = 176;
  public static final int OPCODE_SHOW_WELCOME = 182;
  public static final int OPCODE_DENY_LOGOUT = 183;
  public static final int OPCODE_CREATE_PLAYERS = 191;
  public static final int OPCODE_INCORRECT_SLEEPWORD = 194;
  public static final int OPCODE_CLOSE_BANK = 203;
  public static final int OPCODE_PLAY_SOUND = 204;
  public static final int OPCODE_SET_PRAYERS = 206;
  public static final int OPCODE_UPDATE_DUEL_ACCEPTANCE = 210;
  public static final int OPCODE_UPDATE_ENTITIES = 211;
  public static final int OPCODE_NO_OP_WHILE_WAITING_FOR_NEW_APPEARANCE = 213;
  public static final int OPCODE_SHOW_DIALOGUE_SERVER_MESSAGE_TOP = 222;
  public static final int OPCODE_CANCEL_DUEL_DIALOGUE = 225;
  public static final int OPCODE_UPDATE_PLAYERS = 234;
  public static final int OPCODE_UPDATE_IGNORE = 237;
  public static final int OPCODE_GAME_SETTINGS = 240;
  public static final int OPCODE_SET_FATIGUE_SLEEPING = 244;
  public static final int OPCODE_POPULATE_OPTION_MENU = 245;
  public static final int OPCODE_UPDATE_BANK_ITEMS_DISPLAY = 249;
  public static final int OPCODE_DISABLE_OPTION_MENU = 252;
  public static final int OPCODE_UPDATE_DUEL_OPPONENT_ACCEPTANCE = 253;
}
