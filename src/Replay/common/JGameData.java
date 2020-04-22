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
package Replay.common;

import Replay.game.constants.Game;

public class JGameData {
  public static int itemCount;
  public static String itemName[];
  public static String itemExamine[];
  public static String itemCommand[];
  public static int itemPrice[];
  public static boolean itemStackable[];
  public static int itemWearable[];
  public static boolean itemTradable[];
  public static boolean itemMembers[];
  public static int npcCount;
  public static String npcName[];
  public static String npcExamine[];
  public static int npcAttack[];
  public static int npcStrength[];
  public static int npcHits[];
  public static int npcDefense[];
  public static int npcAttackable[];
  public static String npcCommand[];
  public static int animationCount;
  public static String animationName[];
  public static int animationIndex[];
  public static int objectCount;
  public static int objectWidth[];
  public static int objectHeight[];
  public static int wallObjectCount;
  public static boolean wallObjectAdjacent[];
  public static boolean wallObjectPassable[];
  public static int tileCount;
  public static int tileDecoration[];
  public static int tileType[];
  public static int tileAdjacent[];

  public static byte regionCollisionMask[][][][];
  public static byte regionDirection[][][][];

  public static boolean init(boolean member) {
    JContent content = new JContent();
    JContent contentMembers = new JContent();

    // Read content0 (Configuration)
    if (!content.open("content0_229aa476")) return false;
    JContentFile string = content.unpack("string.dat");
    if (string == null) return false;
    JContentFile integer = content.unpack("integer.dat");
    if (integer == null) return false;
    content.close();

    // Read item data
    itemCount = integer.readUnsignedShort();
    itemName = new String[itemCount];
    itemExamine = new String[itemCount];
    itemCommand = new String[itemCount];
    itemPrice = new int[itemCount];
    itemStackable = new boolean[itemCount];
    itemWearable = new int[itemCount];
    itemTradable = new boolean[itemCount];
    itemMembers = new boolean[itemCount];
    for (int i = 0; i < itemCount; i++) itemName[i] = string.readString();
    for (int i = 0; i < itemCount; i++) itemExamine[i] = string.readString();
    for (int i = 0; i < itemCount; i++) itemCommand[i] = string.readString();
    for (int i = 0; i < itemCount; i++) integer.skip(2); // Sprite
    for (int i = 0; i < itemCount; i++) itemPrice[i] = integer.readUnsignedInt();
    for (int i = 0; i < itemCount; i++) itemStackable[i] = (integer.readUnsignedByte() == 0);
    for (int i = 0; i < itemCount; i++) integer.skip(1); // Unused
    for (int i = 0; i < itemCount; i++) itemWearable[i] = integer.readUnsignedShort();
    for (int i = 0; i < itemCount; i++) integer.skip(4); // Mask
    for (int i = 0; i < itemCount; i++) itemTradable[i] = (integer.readUnsignedByte() == 0);
    for (int i = 0; i < itemCount; i++) itemMembers[i] = (integer.readUnsignedByte() == 1);

    // Read npc data
    npcCount = integer.readUnsignedShort();
    npcName = new String[npcCount];
    npcExamine = new String[npcCount];
    npcAttack = new int[npcCount];
    npcStrength = new int[npcCount];
    npcHits = new int[npcCount];
    npcDefense = new int[npcCount];
    npcAttackable = new int[npcCount];
    npcCommand = new String[npcCount];
    for (int i = 0; i < npcCount; i++) npcName[i] = string.readString();
    for (int i = 0; i < npcCount; i++) npcExamine[i] = string.readString();
    for (int i = 0; i < npcCount; i++) npcAttack[i] = integer.readUnsignedByte();
    for (int i = 0; i < npcCount; i++) npcStrength[i] = integer.readUnsignedByte();
    for (int i = 0; i < npcCount; i++) npcHits[i] = integer.readUnsignedByte();
    for (int i = 0; i < npcCount; i++) npcDefense[i] = integer.readUnsignedByte();
    for (int i = 0; i < npcCount; i++) npcAttackable[i] = integer.readUnsignedByte();
    for (int i = 0; i < npcCount; i++) for (int i2 = 0; i2 < 12; i2++) integer.skip(1); // Sprite
    for (int i = 0; i < npcCount; i++) integer.skip(4); // Hair color
    for (int i = 0; i < npcCount; i++) integer.skip(4); // Top color
    for (int i = 0; i < npcCount; i++) integer.skip(4); // Bottom color
    for (int i = 0; i < npcCount; i++) integer.skip(4); // Skin color
    for (int i = 0; i < npcCount; i++) integer.skip(2); // Width
    for (int i = 0; i < npcCount; i++) integer.skip(2); // Height
    for (int i = 0; i < npcCount; i++) integer.skip(1); // Walk model (?)
    for (int i = 0; i < npcCount; i++) integer.skip(1); // Combat model (?)
    for (int i = 0; i < npcCount; i++) integer.skip(1); // Combat animation (?)
    for (int i = 0; i < npcCount; i++) npcCommand[i] = string.readString();

    // Read texture data
    int textureCount = integer.readUnsignedShort();
    for (int i = 0; i < textureCount; i++) string.readString(); // Name
    for (int i = 0; i < textureCount; i++) string.readString(); // Subtype name

    // Read animation data
    animationCount = integer.readUnsignedShort();
    animationName = new String[animationCount];
    animationIndex = new int[animationCount];
    for (int i = 0; i < animationCount; i++) animationName[i] = string.readString();
    for (int i = 0; i < animationCount; i++) integer.skip(4); // Unknown
    for (int i = 0; i < animationCount; i++) integer.skip(1); // Unknown
    for (int i = 0; i < animationCount; i++) integer.skip(1); // Unknown
    for (int i = 0; i < animationCount; i++) integer.skip(1); // Unknown
    for (int i = 0; i < animationCount; i++) animationIndex[i] = integer.readUnsignedByte();

    objectCount = integer.readUnsignedShort();
    objectWidth = new int[objectCount];
    objectHeight = new int[objectCount];
    for (int i = 0; i < objectCount; i++) string.readString(); // Name
    for (int i = 0; i < objectCount; i++) string.readString(); // Examine
    for (int i = 0; i < objectCount; i++) string.readString(); // Command 1
    for (int i = 0; i < objectCount; i++) string.readString(); // Command 2
    for (int i = 0; i < objectCount; i++) string.readString(); // Unknown
    for (int i = 0; i < objectCount; i++) objectWidth[i] = integer.readUnsignedByte();
    for (int i = 0; i < objectCount; i++) objectHeight[i] = integer.readUnsignedByte();
    for (int i = 0; i < objectCount; i++) integer.skip(1); // Type
    for (int i = 0; i < objectCount; i++) integer.skip(1); // Elevation

    wallObjectCount = integer.readUnsignedShort();
    wallObjectAdjacent = new boolean[wallObjectCount];
    wallObjectPassable = new boolean[wallObjectCount];
    for (int i = 0; i < wallObjectCount; i++) string.readString(); // Name
    for (int i = 0; i < wallObjectCount; i++) string.readString(); // Examine
    for (int i = 0; i < wallObjectCount; i++) string.readString(); // Command 1
    for (int i = 0; i < wallObjectCount; i++) string.readString(); // Command 2
    for (int i = 0; i < wallObjectCount; i++) integer.skip(2); // Unknown
    for (int i = 0; i < wallObjectCount; i++) integer.skip(4); // Texture 1
    for (int i = 0; i < wallObjectCount; i++) integer.skip(4); // Texture 2
    for (int i = 0; i < wallObjectCount; i++)
      wallObjectAdjacent[i] = (integer.readUnsignedByte() != 0); // Adjacent
    for (int i = 0; i < wallObjectCount; i++)
      wallObjectPassable[i] = (integer.readUnsignedByte() == 0); // Collidable

    int roofCount = integer.readUnsignedShort();
    for (int i = 0; i < roofCount; i++) integer.skip(1); // Height
    for (int i = 0; i < roofCount; i++) integer.skip(1); // Vertices count

    tileCount = integer.readUnsignedShort();
    tileDecoration = new int[tileCount];
    tileType = new int[tileCount];
    tileAdjacent = new int[tileCount];

    for (int i = 0; i < tileCount; i++) tileDecoration[i] = integer.readUnsignedInt();
    for (int i = 0; i < tileCount; i++) tileType[i] = integer.readUnsignedByte();
    for (int i = 0; i < tileCount; i++) tileAdjacent[i] = integer.readUnsignedByte();

    string.close();
    integer.close();

    // Convert member items
    if (!member) {
      for (int i = 0; i < itemCount; i++) {
        if (itemMembers[i]) {
          itemName[i] = "Members object";
          itemExamine[i] = "You need to be a member to use this object";
          itemCommand[i] = "";
          itemPrice[i] = 0;
          itemWearable[i] = 0;
          itemTradable[i] = false;
        }
      }
    }

    for (int i = 0; i < itemCount; i++)
      System.out.println(
          "id: "
              + i
              + ", name: '"
              + itemName[i]
              + "', examine: '"
              + itemExamine[i]
              + "', command: '"
              + itemCommand[i]
              + "', price: '"
              + itemPrice[i]
              + "gp', stackable: "
              + itemStackable[i]
              + ", wearable: "
              + itemWearable[i]
              + ", tradable: "
              + itemTradable[i]
              + ", members: "
              + itemMembers[i]);
    for (int i = 0; i < npcCount; i++) {
      System.out.println(
          "id: "
              + i
              + ", name: '"
              + npcName[i]
              + "', examine: '"
              + npcExamine[i]
              + "', attack: "
              + npcAttack[i]
              + ", strength: "
              + npcStrength[i]
              + ", hits: "
              + npcHits[i]
              + ", defense: "
              + npcDefense[i]
              + ", attackable: "
              + npcAttackable[i]
              + ", command: '"
              + npcCommand[i]
              + "'");
    }
    for (int i = 0; i < animationCount; i++) {
      System.out.println(
          "id: " + i + ", name: '" + animationName[i] + "', index: " + animationIndex[i]);
    }

    int maxRegionWidth = Game.WORLD_WIDTH / Game.REGION_WIDTH;
    int maxRegionHeight = Game.WORLD_HEIGHT / Game.REGION_HEIGHT;
    regionCollisionMask =
        new byte[maxRegionWidth][maxRegionHeight][Game.REGION_FLOORS][Game.REGION_SIZE];
    regionDirection =
        new byte[maxRegionWidth][maxRegionHeight][Game.REGION_FLOORS][Game.REGION_SIZE];

    // Read content6 (landscape)
    if (!content.open("content4_ffffffffaaca2b0d")) return false;
    if (!contentMembers.open("content5_6a1d6b00")) return false;
    for (int x = 0; x < maxRegionWidth; x++) {
      for (int y = 0; y < maxRegionHeight; y++) {
        for (int floor = 0; floor < Game.REGION_FLOORS; floor++) {
          if (!loadLandscape(content, x, y, floor) && member)
            loadLandscape(contentMembers, x, y, floor);
        }
      }
    }
    content.close();
    contentMembers.close();

    return true;
  }

  private static boolean loadLandscape(JContent content, int x, int y, int floor) {
    String mapName = "m" + floor + x / 10 + x % 10 + y / 10 + y % 10;

    JContentFile map = content.unpack(mapName + ".dat");
    if (map == null) {
      // Initialize collisions to collidable
      for (int i = 0; i < Game.REGION_SIZE; i++)
        regionCollisionMask[x][y][floor][i] = Game.COLLISION_TILE;
      return false;
    }

    // Clear collisions
    for (int i = 0; i < Game.REGION_SIZE; i++)
      regionCollisionMask[x][y][floor][i] = Game.COLLISION_NONE;

    for (int i = 0; i < Game.REGION_SIZE; i++) {
      int id = map.readUnsignedByte();
      regionCollisionMask[x][y][floor][i] |=
          (id > 0 && JGameData.wallObjectPassable[id - 1] && JGameData.wallObjectAdjacent[id - 1])
              ? Game.COLLISION_EASTWEST
              : Game.COLLISION_NONE;
    }
    for (int i = 0; i < Game.REGION_SIZE; i++) {
      int id = map.readUnsignedByte();
      regionCollisionMask[x][y][floor][i] |=
          (id > 0 && JGameData.wallObjectPassable[id - 1] && JGameData.wallObjectAdjacent[id - 1])
              ? Game.COLLISION_NORTHSOUTH
              : Game.COLLISION_NONE;
    }

    int data[] = new int[Game.REGION_SIZE];
    for (int i = 0; i < Game.REGION_SIZE; i++) data[i] = map.readUnsignedByte();
    for (int i = 0; i < Game.REGION_SIZE; i++) {
      int val = map.readUnsignedByte();
      if (val > 0) data[i] = 12000 + val;

      int id = data[i];
      regionCollisionMask[x][y][floor][i] |=
          (id > 0
                  && id < 12000
                  && JGameData.wallObjectPassable[id - 1]
                  && JGameData.wallObjectAdjacent[id - 1])
              ? Game.COLLISION_TILE
              : Game.COLLISION_NONE;
      regionCollisionMask[x][y][floor][i] |=
          (id >= 12000
                  && JGameData.wallObjectPassable[id - 12001]
                  && JGameData.wallObjectAdjacent[id - 12001])
              ? Game.COLLISION_TILE
              : Game.COLLISION_NONE;
    }

    // Unknown
    int prevValue = 0;
    for (int i = 0; i < Game.REGION_SIZE; i++) {
      int roof = map.readUnsignedByte();
      if (roof >= 128) {
        for (int i2 = 0; i2 < roof - 128; i2++) i++;
        i--;
      } else {
        prevValue = roof;
      }
    }

    prevValue = 0;
    for (int i = 0; i < Game.REGION_SIZE; i++) {
      int tileDecoration = map.readUnsignedByte();
      if (tileDecoration >= 128) {
        for (int i2 = 0; i2 < tileDecoration - 128; i2++) {
          if (prevValue > 0) {
            regionCollisionMask[x][y][floor][i] |=
                (JGameData.tileAdjacent[prevValue - 1] != 0)
                    ? Game.COLLISION_TILE
                    : Game.COLLISION_NONE;
            // Two floors?
            // regionCollisionMask[x][y][floor][i] |= (JGameData.tileType[prevValue - 1] == 2) ?
            // Game.COLLISION_TILE : Game.COLLISION_NONE;
          }
          i++;
        }
        i--;
      } else {
        if (tileDecoration > 0) {
          regionCollisionMask[x][y][floor][i] |=
              (JGameData.tileAdjacent[tileDecoration - 1] != 0)
                  ? Game.COLLISION_TILE
                  : Game.COLLISION_NONE;
          // Two floors?
          // regionCollisionMask[x][y][floor][i] |= (JGameData.tileType[tileDecoration - 1] == 2) ?
          // Game.COLLISION_TILE : Game.COLLISION_NONE;
        }
        prevValue = tileDecoration;
      }
    }

    for (int i = 0; i < Game.REGION_SIZE; i++) {
      int tileDirection = map.readUnsignedByte();
      if (tileDirection >= 128) {
        for (int i2 = 0; i2 < tileDirection - 128; i2++) regionDirection[x][y][floor][i++] = 0;
        i--;
      } else {
        regionDirection[x][y][floor][i] = (byte) tileDirection;
      }
    }

    System.out.println(mapName);

    map.close();

    return true;
  }
}
