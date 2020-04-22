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
package Replay.game.constants;

import java.util.HashMap;
import java.util.Map;

public class Game {
  // World
  public static final int WORLD_WIDTH = 4608; // Unsure
  public static final int WORLD_HEIGHT = 3776;
  public static final int WORLD_PLANE_X = 2304;
  public static final int WORLD_PLANE_Y = 1776;
  public static final int WORLD_Y_OFFSET = 944;
  public static final int REGION_WIDTH = 48;
  public static final int REGION_HEIGHT = 48;
  public static final int REGION_SIZE = REGION_WIDTH * REGION_HEIGHT;
  public static final int REGION_FLOORS = 4;

  // Collision
  public static final int COLLISION_NONE = 0x00;
  public static final int COLLISION_EASTWEST = 0x01;
  public static final int COLLISION_NORTHSOUTH = 0x02;
  public static final int COLLISION_TILE = 0x04;

  // Direction
  public static final int DIRECTION_NORTH = 0;
  public static final int DIRECTION_NORTHWEST = 1;
  public static final int DIRECTION_WEST = 2;
  public static final int DIRECTION_SOUTHWEST = 3;
  public static final int DIRECTION_SOUTH = 4;
  public static final int DIRECTION_SOUTHEAST = 5;
  public static final int DIRECTION_EAST = 6;
  public static final int DIRECTION_NORTHEAST = 7;

  // Wall object direction
  public static final int DIRECTION_WALLOBJECT_NORTHSOUTH = 0;
  public static final int DIRECTION_WALLOBJECT_EASTWEST = 1;

  // Quests
  public static final int QUEST_BLACK_KNIGHTS_FORTRESS = 0;
  public static final int QUEST_COOKS_ASSISTANT = 1;
  public static final int QUEST_DEMON_SLAYER = 2;
  public static final int QUEST_DORICS_QUEST = 3;
  public static final int QUEST_THE_RESTLESS_GHOST = 4;
  public static final int QUEST_GOBLIN_DIPLOMACY = 5;
  public static final int QUEST_ERNEST_THE_CHICKEN = 6;
  public static final int QUEST_IMP_CATCHER = 7;
  public static final int QUEST_PIRATES_TREASURE = 8;
  public static final int QUEST_PRINCE_ALI_RESCUE = 9;
  public static final int QUEST_ROMEO_AND_JULIET = 10;
  public static final int QUEST_SHEEP_SHEARER = 11;
  public static final int QUEST_SHIELD_OF_ARRAV = 12;
  public static final int QUEST_THE_KNIGHTS_SWORD = 13;
  public static final int QUEST_VAMPIRE_SLAYER = 14;
  public static final int QUEST_WITCHS_POTION = 15;
  public static final int QUEST_DRAGON_SLAYER = 16;
  public static final int QUEST_WITCHS_HOUSE = 17;
  public static final int QUEST_LOST_CITY = 18;
  public static final int QUEST_HEROS_QUEST = 19;
  public static final int QUEST_DRUIDIC_RITUAL = 20;
  public static final int QUEST_MERLINS_CRYSTAL = 21;
  public static final int QUEST_SCORPION_CATCHER = 22;
  public static final int QUEST_FAMILY_CREST = 23;
  public static final int QUEST_TRIBAL_TOTEM = 24;
  public static final int QUEST_FISHING_CONTEST = 25;
  public static final int QUEST_MONKS_FRIEND = 26;
  public static final int QUEST_TEMPLE_OF_IKOV = 27;
  public static final int QUEST_CLOCK_TOWER = 28;
  public static final int QUEST_THE_HOLY_GRAIL = 29;
  public static final int QUEST_FIGHT_ARENA = 30;
  public static final int QUEST_TREE_GNOME_VILLAGE = 31;
  public static final int QUEST_THE_HAZEEL_CULT = 32;
  public static final int QUEST_SHEEP_HERDER = 33;
  public static final int QUEST_PLAGUE_CITY = 34;
  public static final int QUEST_SEA_SLUG = 35;
  public static final int QUEST_WATERFALL_QUEST = 36;
  public static final int QUEST_BIOHAZARD = 37;
  public static final int QUEST_JUNGLE_POTION = 38;
  public static final int QUEST_GRAND_TREE = 39;
  public static final int QUEST_SHILO_VILLAGE = 40;
  public static final int QUEST_UNDERGROUND_PASS = 41;
  public static final int QUEST_OBSERVATORY_QUEST = 42;
  public static final int QUEST_TOURIST_TRAP = 43;
  public static final int QUEST_WATCHTOWER = 44;
  public static final int QUEST_DWARF_CANNON = 45;
  public static final int QUEST_MURDER_MYSTERY = 46;
  public static final int QUEST_DIGSITE = 47;
  public static final int QUEST_GERTRUDES_CAT = 48;
  public static final int QUEST_LEGENDS_QUEST = 49;
  public static final int QUEST_COUNT = 50;

  // Welcome screen constants
  public static final int WELCOME_IP_HIDE = 0;
  public static final int WELCOME_MESSAGES_HIDE = 0;
  public static final int WELCOME_MESSAGES_SHOW = 1;
  public static final int WELCOME_RECOVERY_UNSET = 200;
  public static final int WELCOME_RECOVERY_HIDE = 201;

  // Inventory
  public static final int INVENTORY_COUNT = 35;

  // Equipment stats
  public static final int EQUIP_STAT_ARMOUR = 0;
  public static final int EQUIP_STAT_WEAPONAIM = 1;
  public static final int EQUIP_STAT_WEAPONPOWER = 2;
  public static final int EQUIP_STAT_MAGIC = 3;
  public static final int EQUIP_STAT_PRAYER = 4;
  public static final int EQUIP_STAT_COUNT = 5;

  // Prayers
  public static final int PRAYER_COUNT = 14;

  // Stats
  public static final int STAT_ATTACK = 0;
  public static final int STAT_DEFENSE = 1;
  public static final int STAT_STRENGTH = 2;
  public static final int STAT_HITS = 3;
  public static final int STAT_RANGED = 4;
  public static final int STAT_PRAYER = 5;
  public static final int STAT_MAGIC = 6;
  public static final int STAT_COUNT = 18;

  // Chat Types
  public static final int CHAT_QUEST = 3;
  public static final int CHAT_CHAT = 4;

  // Incoming Opcodes
  public static final Map<Integer, String> incomingOpcodeMap =
      new HashMap<Integer, String>() {
        {
          put(4, "CLOSE_CONNECTION_NOTIFY");
          put(5, "QUEST_STATUS");
          put(6, "UPDATE_STAKED_ITEMS_OPPONENT");
          put(15, "UPDATE_TRADE_ACCEPTANCE");
          put(20, "SHOW_CONFIRM_TRADE");
          put(25, "FLOOR_SET");
          put(30, "SYNC_DUEL_SETTINGS");
          put(33, "UPDATE_XP");
          put(36, "DISPLAY_TELEPORT_TELEGRAB_BUBBLE");
          put(42, "OPEN_BANK");
          put(48, "SCENERY_HANDLER");
          put(51, "PRIVACY_SETTINGS");
          put(52, "UPDATE_SYSTEM_UPDATE_TIMER");
          put(53, "SET_INVENTORY");
          put(59, "SHOW_APPEARANCE_CHANGE");
          put(79, "NPC_COORDS");
          put(83, "DISPLAY_DEATH_SCREEN");
          put(84, "WAKE_UP");
          put(87, "SEND_PM");
          put(89, "SHOW_DIALOGUE_SERVER_MESSAGE_NOT_TOP");
          put(90, "SET_INVENTORY_SLOT");
          put(91, "BOUNDARY_HANDLER");
          put(92, "INITIATE_TRADE");
          put(97, "UPDATE_ITEMS_TRADED_TO_YOU");
          put(99, "GROUNDITEM_HANDLER");
          put(101, "SHOW_SHOP");
          put(104, "UPDATE_NPC");
          put(109, "SET_IGNORE");
          put(111, "COMPLETED_TUTORIAL");
          put(114, "SET_FATIGUE");
          put(117, "FALL_ASLEEP");
          put(120, "RECEIVE_PM");
          put(123, "REMOVE_INVENTORY_SLOT");
          put(128, "CONCLUDE_TRADE");
          put(131, "SEND_MESSAGE");
          put(137, "EXIT_SHOP");
          put(149, "UPDATE_FRIEND");
          put(153, "SET_EQUIP_STATS");
          put(156, "SET_STATS");
          put(159, "UPDATE_STAT");
          put(162, "UPDATE_TRADE_RECIPIENT_ACCEPTANCE");
          put(165, "CLOSE_CONNECTION");
          put(172, "SHOW_CONFIRM_DUEL");
          put(176, "SHOW_DIALOGUE_DUEL");
          put(182, "SHOW_WELCOME");
          put(183, "DENY_LOGOUT");
          put(191, "PLAYER_COORDS");
          put(194, "INCORRECT_SLEEPWORD");
          put(203, "CLOSE_BANK");
          put(204, "PLAY_SOUND");
          put(206, "SET_PRAYERS");
          put(210, "UPDATE_DUEL_ACCEPTANCE");
          put(211, "UPDATE_ENTITIES");
          put(213, "NO_OP_WHILE_WAITING_FOR_NEW_APPEARANCE");
          put(222, "SHOW_DIALOGUE_SERVER_MESSAGE_TOP");
          put(225, "CANCEL_DUEL_DIALOGUE");
          put(234, "UPDATE_PLAYERS");
          put(237, "UPDATE_IGNORE_BECAUSE_OF_NAME_CHANGE");
          put(240, "GAME_SETTINGS");
          put(244, "SET_FATIGUE_SLEEPING");
          put(245, "SHOW_DIALOGUE_MENU");
          put(249, "UPDATE_BANK_ITEMS_DISPLAY");
          put(252, "DISABLE_OPTION_MENU");
          put(253, "UPDATE_DUEL_OPPONENT_ACCEPTANCE");
          put(10000, "VIRTUAL_OPCODE_LOGIN_RESPONSE");
        }
      };

  // Outgoing Opcodes
  public static final Map<Integer, String> outgoingOpcodeMap =
      new HashMap<Integer, String>() {
        {
          put(4, "CAST_ON_INV_ITEM");
          put(8, "SEND_DUEL_SETTINGS");
          put(14, "INTERACT_WITH_WALL_OBJECT");
          put(16, "WALK_AND_PERFORM_ACTION");
          put(22, "BANK_DEPOSIT");
          put(23, "BANK_WITHDRAW");
          put(29, "SEND_COMBAT_STYLE");
          put(31, "CLOSE_CONNECTION_REPLY");
          put(33, "SEND_STAKED_ITEMS");
          put(38, "SEND_COMMAND_STRING");
          put(45, "SEND_SLEEPWORD");
          put(46, "OFFER_TRADE_ITEM");
          put(50, "CAST_NPC");
          put(53, "USE_ON_GROUND_ITEM");
          put(55, "SET_TRADE_ACCEPTED");
          put(60, "ENABLE_PRAYER");
          put(64, "SEND_PRIVACY_SETTING");
          put(67, "HEARTBEAT");
          put(77, "DUEL_CONFIRM_ACCEPT");
          put(79, "INTEACT_WITH_OBJECT2");
          put(84, "SKIP_TUTORIAL");
          put(90, "INV_COMMAND");
          put(91, "USE_WITH_INV_ITEM");
          put(99, "CAST_ON_OBJECT");
          put(102, "REQUEST_LOGOUT");
          put(103, "SEND_DUEL_REQUEST");
          put(104, "CONFIRM_ACCEPT_TRADE");
          put(111, "SEND_NEW_SETTINGS");
          put(113, "USE_WITH_PLAYER");
          put(115, "USE_WITH_OBJECT");
          put(116, "SELECT_DIALOGUE_OPTION");
          put(127, "INTERACT_WITH_WALL_OBJECT2");
          put(132, "ADD_IGNORE");
          put(135, "USE_ON_NPC");
          put(136, "INTERACT_WITH_OBJECT");
          put(137, "CAST_ON_SELF");
          put(142, "AGREE_TO_TRADE");
          put(153, "TALK_TO_NPC");
          put(158, "CAST_ON_GROUND");
          put(161, "USE_WITH_WALL_OBJECT");
          put(165, "FOLLOW_PLAYER");
          put(166, "CLOSE_SHOP");
          put(167, "REMOVE_FRIEND");
          put(169, "EQUIP_ITEM");
          put(170, "UNEQUIP_ITEM");
          put(171, "ATTACK_PLAYER");
          put(176, "ACCEPT_DUEL");
          put(180, "CAST_WALL_OBJECT");
          put(187, "WALK");
          put(190, "ATTACK_NPC");
          put(195, "ADD_FRIEND");
          put(197, "DECLINE_DUEL");
          put(202, "INTERACT_NPC");
          put(206, "SEND_REPORT");
          put(212, "BANK_CLOSE");
          put(216, "SEND_CHAT_MESSAGE");
          put(218, "SEND_PM");
          put(221, "SELL_TO_SHOP");
          put(229, "CAST_PVP");
          put(230, "ABORT_DIALOGUE");
          put(235, "SEND_APPEARANCE");
          put(236, "BUY_FROM_SHOP");
          put(241, "REMOVE_IGNORED");
          put(246, "DROP_ITEM");
          put(247, "TAKE_GROUND_ITEM");
          put(249, "CAST_ON_GROUND_ITEM");
          put(254, "DISABLE_PRAYER");
          put(10000, "VIRTUAL_OPCODE_LOGIN_REQUEST");
        }
      };
}
