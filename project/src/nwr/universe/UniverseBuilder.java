/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  This file is part of "NorseWorld: Ragnarok".
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nwr.universe;

import java.util.ArrayList;
import java.util.List;
import jzrlib.core.BytesSet;
import jzrlib.core.Directions;
import jzrlib.core.GameEntity;
import jzrlib.core.GameSpace;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.map.AbstractMap;
import jzrlib.map.BaseTile;
import jzrlib.map.IMap;
import jzrlib.map.MapUtils;
import jzrlib.map.TileStates;
import jzrlib.map.TileType;
import jzrlib.map.builders.CaveBuilder;
import jzrlib.map.builders.RoadBuilder;
import jzrlib.map.dungeons.AreaType;
import jzrlib.map.dungeons.DungeonArea;
import jzrlib.map.dungeons.DungeonBuilder;
import jzrlib.map.dungeons.DungeonMark;
import jzrlib.map.dungeons.RectangularRoom;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.RefObject;
import jzrlib.utils.TypeUtils;
import nwr.core.StaticData;
import nwr.core.types.BuildingID;
import nwr.core.types.MagicRec;
import nwr.core.types.OctantRec;
import nwr.core.types.PlaceFlags;
import nwr.core.types.PlaceID;
import nwr.creatures.NWCreature;
import nwr.database.CreatureFlags;
import nwr.database.DataEntry;
import nwr.database.ItemEntry;
import nwr.item.Item;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class UniverseBuilder
{
    // Bazaar Item Kinds
    private static final int bik_Ring = 0;
    private static final int bik_Potion = 1;
    private static final int bik_Wand = 2;
    private static final int bik_Food = 3;
    private static final int bik_Scroll = 4;
    private static final int bik_Armor = 5;
    private static final int bik_Tool = 6;
    private static final int bik_Weapon = 7;
    
    private static final int[][] AreaItem;
    private static final OctantRec[] Octants;
    private static final int[] dbWaters;
    private static final MagicRec[] dbDgnMagics;
    private static final MagicRec[] dbRoadMagics;
    public static final Point[] dbAdjacentTiles;
    public static final MagicRec[] dbMaskMagics;

    static {
        AreaItem = new int[2][4];
        AreaItem[0][0] = bik_Ring;
        AreaItem[0][1] = bik_Potion;
        AreaItem[0][2] = bik_Wand;
        AreaItem[0][3] = bik_Food;
        AreaItem[1][0] = bik_Scroll;
        AreaItem[1][1] = bik_Armor;
        AreaItem[1][2] = bik_Tool;
        AreaItem[1][3] = bik_Weapon;

        Octants = new OctantRec[8];
        Octants[0] = new OctantRec(38, 9, 50, 38, 75, 9, 17, new Directions(Directions.dtNorth, Directions.dtSouth, Directions.dtEast, Directions.dtNorthEast, Directions.dtSouthEast));
        Octants[1] = new OctantRec(38, 9, 10, 38, 46, 9, 17, new Directions(Directions.dtSouth, Directions.dtWest, Directions.dtEast, Directions.dtSouthWest, Directions.dtSouthEast));
        Octants[2] = new OctantRec(37, 9, 10, 29, 37, 9, 17, new Directions(Directions.dtSouth, Directions.dtWest, Directions.dtEast, Directions.dtSouthWest, Directions.dtSouthEast));
        Octants[3] = new OctantRec(37, 9, 50, 0, 37, 9, 17, new Directions(Directions.dtNorth, Directions.dtSouth, Directions.dtWest, Directions.dtNorthWest, Directions.dtSouthWest));
        Octants[4] = new OctantRec(37, 8, 50, 0, 37, 0, 8, new Directions(Directions.dtNorth, Directions.dtSouth, Directions.dtWest, Directions.dtNorthWest, Directions.dtSouthWest));
        Octants[5] = new OctantRec(37, 8, 10, 29, 37, 0, 8, new Directions(Directions.dtNorth, Directions.dtWest, Directions.dtEast, Directions.dtNorthWest, Directions.dtNorthEast));
        Octants[6] = new OctantRec(38, 8, 10, 38, 46, 0, 8, new Directions(Directions.dtNorth, Directions.dtWest, Directions.dtEast, Directions.dtNorthWest, Directions.dtNorthEast));
        Octants[7] = new OctantRec(38, 8, 50, 38, 75, 0, 8, new Directions(Directions.dtNorth, Directions.dtSouth, Directions.dtEast, Directions.dtNorthEast, Directions.dtSouthEast));

        dbWaters = new int[]{PlaceID.pid_WaterTrap, PlaceID.pid_LavaPool, PlaceID.pid_Water, PlaceID.pid_Quicksand, PlaceID.pid_Lava, PlaceID.pid_Liquid, PlaceID.pid_Mud};

        dbAdjacentTiles = new Point[8];
        dbAdjacentTiles[0] = new Point( 0, -1);
        dbAdjacentTiles[1] = new Point(+1, -1);
        dbAdjacentTiles[2] = new Point(+1,  0);
        dbAdjacentTiles[3] = new Point(+1, +1);
        dbAdjacentTiles[4] = new Point( 0, +1);
        dbAdjacentTiles[5] = new Point(-1, +1);
        dbAdjacentTiles[6] = new Point(-1,  0);
        dbAdjacentTiles[7] = new Point(-1, -1);

        dbDgnMagics = new MagicRec[47];
        dbDgnMagics[0] = new MagicRec(new BytesSet(), new BytesSet());
        dbDgnMagics[1] = new MagicRec(new BytesSet(1, 3, 129, 131), new BytesSet());
        dbDgnMagics[2] = new MagicRec(new BytesSet(7, 15, 135, 143), new BytesSet());
        dbDgnMagics[3] = new MagicRec(new BytesSet(4, 6, 12, 14), new BytesSet());
        dbDgnMagics[4] = new MagicRec(new BytesSet(28, 30, 60, 62), new BytesSet());
        dbDgnMagics[5] = new MagicRec(new BytesSet(16, 24, 48, 56), new BytesSet());
        dbDgnMagics[6] = new MagicRec(new BytesSet(112, 120, 240, 248), new BytesSet());
        dbDgnMagics[7] = new MagicRec(new BytesSet(64, 96, 192, 224), new BytesSet());
        dbDgnMagics[8] = new MagicRec(new BytesSet(193, 195, 225, 227), new BytesSet());

        dbDgnMagics[9] = new MagicRec(new BytesSet(17, 19, 25, 27, 49, 51, 57, 59, 145, 147, 153, 155, 177, 179, 185, 187), new BytesSet());
        dbDgnMagics[10] = new MagicRec(new BytesSet(68, 70, 76, 78, 100, 102, 108, 110, 196, 198, 204, 206, 228, 230, 236, 238), new BytesSet());

        dbDgnMagics[11] = new MagicRec(new BytesSet(199, 207, 231, 239), new BytesSet());
        dbDgnMagics[12] = new MagicRec(new BytesSet(31, 63, 159, 191), new BytesSet());
        dbDgnMagics[13] = new MagicRec(new BytesSet(124, 126, 252, 254), new BytesSet());
        dbDgnMagics[14] = new MagicRec(new BytesSet(241, 243, 249, 251), new BytesSet());
        dbDgnMagics[15] = new MagicRec(new BytesSet(170), new BytesSet());

        dbDgnMagics[16] = new MagicRec(new BytesSet(130), new BytesSet());
        dbDgnMagics[17] = new MagicRec(new BytesSet(2), new BytesSet());
        dbDgnMagics[18] = new MagicRec(new BytesSet(10), new BytesSet());
        dbDgnMagics[19] = new MagicRec(new BytesSet(8), new BytesSet());
        dbDgnMagics[20] = new MagicRec(new BytesSet(40), new BytesSet());
        dbDgnMagics[21] = new MagicRec(new BytesSet(32), new BytesSet());
        dbDgnMagics[22] = new MagicRec(new BytesSet(160), new BytesSet());
        dbDgnMagics[23] = new MagicRec(new BytesSet(128), new BytesSet());

        dbDgnMagics[24] = new MagicRec(new BytesSet(39, 47, 167, 175), new BytesSet());
        dbDgnMagics[25] = new MagicRec(new BytesSet(156, 158, 188, 190), new BytesSet());
        dbDgnMagics[26] = new MagicRec(new BytesSet(114, 122, 242, 250), new BytesSet());
        dbDgnMagics[27] = new MagicRec(new BytesSet(201, 203, 233, 235), new BytesSet());

        dbDgnMagics[28] = new MagicRec(new BytesSet(41, 43, 169, 171), new BytesSet());
        dbDgnMagics[29] = new MagicRec(new BytesSet(33, 35, 161, 163), new BytesSet());
        dbDgnMagics[30] = new MagicRec(new BytesSet(9, 11, 137, 139), new BytesSet());
        dbDgnMagics[31] = new MagicRec(new BytesSet(164, 166, 172, 174), new BytesSet());
        dbDgnMagics[32] = new MagicRec(new BytesSet(132, 134, 140, 142), new BytesSet());
        dbDgnMagics[33] = new MagicRec(new BytesSet(36, 38, 44, 46), new BytesSet());
        dbDgnMagics[34] = new MagicRec(new BytesSet(146, 154, 178, 186), new BytesSet());
        dbDgnMagics[35] = new MagicRec(new BytesSet(144, 152, 176, 184), new BytesSet());
        dbDgnMagics[36] = new MagicRec(new BytesSet(18, 26, 50, 58), new BytesSet());
        dbDgnMagics[37] = new MagicRec(new BytesSet(74, 106, 202, 234), new BytesSet());
        dbDgnMagics[38] = new MagicRec(new BytesSet(66, 98, 194, 226), new BytesSet());
        dbDgnMagics[39] = new MagicRec(new BytesSet(72, 104, 200, 232), new BytesSet());

        dbDgnMagics[40] = new MagicRec(new BytesSet(255), new BytesSet());
        dbDgnMagics[41] = new MagicRec(new BytesSet(34), new BytesSet());
        dbDgnMagics[42] = new MagicRec(new BytesSet(136), new BytesSet());

        dbDgnMagics[43] = new MagicRec(new BytesSet(138), new BytesSet());
        dbDgnMagics[44] = new MagicRec(new BytesSet(42), new BytesSet());
        dbDgnMagics[45] = new MagicRec(new BytesSet(168), new BytesSet());
        dbDgnMagics[46] = new MagicRec(new BytesSet(162), new BytesSet());

        dbRoadMagics = new MagicRec[16];
        dbRoadMagics[0] = new MagicRec(new BytesSet(), new BytesSet());
        dbRoadMagics[1] = new MagicRec(new BytesSet(68, 70, 76, 78, 100, 102, 110, 196, 204, 206, 228, 230, 236), new BytesSet());
        dbRoadMagics[2] = new MagicRec(new BytesSet(17, 19, 25, 49, 51, 57, 59, 145, 147, 153, 155, 179, 185), new BytesSet());
        dbRoadMagics[3] = new MagicRec(new BytesSet(0, 85), new BytesSet());
        dbRoadMagics[4] = new MagicRec(new BytesSet(84, 86, 212, 214), new BytesSet());
        dbRoadMagics[5] = new MagicRec(new BytesSet(81, 83, 89, 91), new BytesSet());
        dbRoadMagics[6] = new MagicRec(new BytesSet(69, 77, 101, 109), new BytesSet());
        dbRoadMagics[7] = new MagicRec(new BytesSet(21, 53, 149, 181), new BytesSet());
        dbRoadMagics[8] = new MagicRec(new BytesSet(80, 88, 208, 216), new BytesSet());
        dbRoadMagics[9] = new MagicRec(new BytesSet(65, 67, 97, 99), new BytesSet());
        dbRoadMagics[10] = new MagicRec(new BytesSet(5, 13, 133, 141), new BytesSet());
        dbRoadMagics[11] = new MagicRec(new BytesSet(20, 22, 52, 54), new BytesSet());
        dbRoadMagics[12] = new MagicRec(new BytesSet(64, 96, 98, 192, 200, 224, 66, 72), new BytesSet());
        dbRoadMagics[13] = new MagicRec(new BytesSet(16, 24, 48, 50, 56, 152, 18, 144), new BytesSet());
        dbRoadMagics[14] = new MagicRec(new BytesSet(4, 6, 12, 14, 38, 140, 36, 132), new BytesSet());
        dbRoadMagics[15] = new MagicRec(new BytesSet(1, 3, 35, 129, 131, 137, 9, 33), new BytesSet());

        dbMaskMagics = new MagicRec[15];
        dbMaskMagics[0] = new MagicRec(new BytesSet(7, 15, 39, 47, 135, 143, 167, 175), new BytesSet());
        dbMaskMagics[1] = new MagicRec(new BytesSet(28, 30, 60, 62, 156, 158, 188, 190), new BytesSet());
        dbMaskMagics[2] = new MagicRec(new BytesSet(112, 114, 120, 122, 240, 242, 248, 250), new BytesSet());
        dbMaskMagics[3] = new MagicRec(new BytesSet(193, 195, 201, 203, 225, 227, 233, 235), new BytesSet());
        dbMaskMagics[4] = new MagicRec(new BytesSet(199, 207, 231, 111, 237, 239), new BytesSet());
        dbMaskMagics[5] = new MagicRec(new BytesSet(31, 63, 159, 183, 189, 191), new BytesSet());
        dbMaskMagics[6] = new MagicRec(new BytesSet(124, 126, 252, 222, 246, 254), new BytesSet());
        dbMaskMagics[7] = new MagicRec(new BytesSet(241, 243, 249, 123, 219, 251), new BytesSet());
        dbMaskMagics[8] = new MagicRec(new BytesSet(127, 223, 247, 253, 255), new BytesSet());
        dbMaskMagics[9] = new MagicRec(new BytesSet(), new BytesSet());
        dbMaskMagics[10] = new MagicRec(new BytesSet(), new BytesSet());
        dbMaskMagics[11] = new MagicRec(new BytesSet(), new BytesSet());
        dbMaskMagics[12] = new MagicRec(new BytesSet(), new BytesSet());
        dbMaskMagics[13] = new MagicRec(new BytesSet(119), new BytesSet());
        dbMaskMagics[14] = new MagicRec(new BytesSet(221), new BytesSet());
    }

    public static short translateTile(TileType aTile)
    {
        short res = (short)PlaceID.pid_Undefined;

        switch (aTile) {
            case ttGrass:
                res = NWField.getVarTile(PlaceID.pid_Grass);
                break;

            case ttTree:
                res = (short)PlaceID.pid_Tree;
                break;

            case ttWater:
            case ttDeepWater:
            case ttDeeperWater:
                res = NWField.getVarTile(PlaceID.pid_Water);
                break;

            case ttBWallN:
                res = (short)PlaceID.pid_FenceN;
                break;

            case ttBWallS:
                res = (short)PlaceID.pid_FenceS;
                break;

            case ttBWallW:
                res = (short)PlaceID.pid_FenceW;
                break;

            case ttBWallE:
                res = (short)PlaceID.pid_FenceE;
                break;

            case ttBWallNW:
                res = (short)PlaceID.pid_FenceNW;
                break;

            case ttBWallNE:
                res = (short)PlaceID.pid_FenceNE;
                break;

            case ttBWallSW:
                res = (short)PlaceID.pid_FenceSW;
                break;

            case ttBWallSE:
                res = (short)PlaceID.pid_FenceSE;
                break;

            case ttCaveFloor:
                res = (short)PlaceID.pid_CaveFloor;
                break;

            case ttRock:
            case ttCaveWall:
                res = (short)PlaceID.pid_CaveWall;
                break;

            case ttMountain:
                res = (short)PlaceID.pid_Mountain;
                break;


            case ttLinearCorridorWall:
                res = (short)PlaceID.pid_Stalagmite;
                break;

            case ttRectRoomWall:
                res = (short)PlaceID.pid_QuicksandTrap;
                break;

            case ttCylindricityRoomWall:
                res = (short)PlaceID.pid_LavaTrap;
                break;

            case ttDungeonWall:
                res = (short)PlaceID.pid_CaveWall;
                break;
        }

        return res;
    }
    
    public static final void build_Caves(AbstractMap map, Rect area)
    {
        CaveBuilder cavesBuilder = new CaveBuilder(map, area);
        cavesBuilder.build();
    }

    public static final void build_Dungeon(AbstractMap map, Rect dungeonArea/*, boolean debugByStep*/)
    {
        try {
            DungeonBuilder builder = new DungeonBuilder(map, dungeonArea);
            try {
                /*TDungeonBuilder.Debug_StepByStep = debugByStep;*/

                builder.setAreaWeight(AreaType.atRectangularRoom, 45);
                builder.setAreaWeight(AreaType.atLinearCorridor, 20);
                builder.setAreaWeight(AreaType.atCylindricityRoom, 0);
                builder.setAreaWeight(AreaType.atQuadrantCorridor, 0);
                builder.setAreaWeight(AreaType.atGenevaWheel, 4);
                builder.setAreaWeight(AreaType.atQuakeIIArena, 0);
                builder.setAreaWeight(AreaType.atTemple, 4);
                builder.setAreaWeight(AreaType.atMonasticCells, 4);
                builder.setAreaWeight(AreaType.atCrypt, 4);
                builder.setAreaWeight(AreaType.atRoseRoom, 4);
                builder.setAreaWeight(AreaType.atCrossroad, 4);
                builder.setAreaWeight(AreaType.atStarRoom, 0);
                builder.setAreaWeight(AreaType.atFaithRoom, 0);
                builder.setAreaWeight(AreaType.atSpiderRoom, 0);
                builder.setAreaWeight(AreaType.atAlt1Room, 2);
                builder.setAreaWeight(AreaType.atAlt2Room, 3);
                builder.setAreaWeight(AreaType.atAlt3Room, 2);
                builder.setAreaWeight(AreaType.atAlt4Room, 3);
                builder.setAreaWeight(AreaType.atAlt5Room, 1);

                builder.build();

                List<DungeonArea> areas = builder.getAreasList();
                for (DungeonArea area : areas) {
                    if (area instanceof RectangularRoom) {
                        RectangularRoom room = (RectangularRoom) area;
                        DungeonRoom dunRoom = new DungeonRoom(GameSpace.getInstance(), map);
                        dunRoom.setArea(room.Left, room.Top, room.getRight(), room.getBottom());
                        map.getFeatures().add(dunRoom);

                        for (DungeonMark mark : room.MarksList) {
                            switch (mark.getState()) {
                                case DungeonMark.ms_Undefined:
                                case DungeonMark.ms_RetriesExhaust:
                                    // dummy
                                    break;
                                case DungeonMark.ms_AreaGenerator:
                                case DungeonMark.ms_PointToOtherArea:
                                    dunRoom.addDoor(mark.Location.X, mark.Location.Y, mark.Direction, Door.STATE_OPENED);
                                    break;
                            }
                        }
                    }
                }
                
                normalizeDungeon(map, dungeonArea);
            } finally {
                builder.dispose();
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Dungeon(): " + ex.getMessage());
            throw ex;
        }
    }

    // Tile select levels
    public static final int TSL_BACK = 0;
    public static final int TSL_FORE = 1;
    public static final int TSL_BACK_EXT = 2;
    public static final int TSL_FORE_EXT = 3;
    public static final int TSL_FOG = 4;
    
    public static int getAdjacentMagic(AbstractMap map, int x, int y, int def, int check, int select)
    {
        int magic = 0;
        for (int i = 0; i < dbAdjacentTiles.length; i++) {
            Point pt = dbAdjacentTiles[i];

            int tid;
            NWTile tile = (NWTile) map.getTile(x + pt.X, y + pt.Y);
            if (tile == null) {
                tid = def;
            } else {
                switch (select) {
                    case TSL_BACK:
                        tid = TypeUtils.getShortLo(tile.Background);
                        break;
                    case TSL_FORE:
                        tid = TypeUtils.getShortLo(tile.Foreground);
                        break;
                    case TSL_BACK_EXT:
                        tid = TypeUtils.getShortLo(tile.BackgroundExt);
                        break;
                    case TSL_FORE_EXT:
                        tid = TypeUtils.getShortLo(tile.ForegroundExt);
                        break;
                    case TSL_FOG:
                        tid = TypeUtils.getShortLo(tile.FogID);
                        break;
                    default:
                        tid = 0;
                        break;
                }
            }

            if (tid == check) {
                magic = TypeUtils.setBit(magic, i, 1);
            }
        }
        return magic;
    }
    
    private static void normalizeTile(NWField field, int x, int y, NWTile tile, int def, int pid, int mask, boolean aFog, boolean Ext, int select)
    {
        try {
            int magic = UniverseBuilder.getAdjacentMagic(field, x, y, def, pid, select);

            int offset = -1;
            if (magic != 0) {
                for (int i = 0; i < UniverseBuilder.dbMaskMagics.length; i++) {
                    MagicRec mRec = UniverseBuilder.dbMaskMagics[i];
                    if (mRec.main.contains(magic) || (Ext && mRec.ext.contains(magic))) {
                        offset = i;
                        break;
                    }
                }
            }

            if (offset > -1) {
                int res = BaseTile.getVarID((byte) mask, (byte) offset);
                if (aFog) {
                    tile.FogExtID = (short) res;
                } else {
                    tile.BackgroundExt = (short) res;
                }
            } else {
                int res = def;
                if (aFog) {
                    tile.FogExtID = (short) res;
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.normalizeTile(): " + ex.getMessage());
        }
    }

    public static void normalizeFieldMasks(NWField field, boolean fog)
    {
        try {
            boolean caves = field.LandID == GlobalVars.Land_Armory || field.LandID == GlobalVars.Land_GrynrHalls || field.LandID == GlobalVars.Land_Caves || field.LandID == GlobalVars.Land_DeepCaves || field.LandID == GlobalVars.Land_GreatCaves;

            int select = (fog) ? UniverseBuilder.TSL_FOG : UniverseBuilder.TSL_BACK;
            
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = field.getTile(x, y);

                    int fore = tile.getForeBase();
                    boolean isDoor = (fore == PlaceID.pid_DoorN || fore == PlaceID.pid_DoorS || fore == PlaceID.pid_DoorW || fore == PlaceID.pid_DoorE);
                    if (caves && isDoor) {
                        tile.setFore(PlaceID.pid_Undefined);
                    }

                    if (!fog) {
                        tile.BackgroundExt = (short) PlaceID.pid_Undefined;

                        int back = tile.getBackBase();

                        if (back != PlaceID.pid_Grass) {
                            if (back == PlaceID.pid_Water || back == PlaceID.pid_Space || back == PlaceID.pid_Lava || back == PlaceID.pid_Liquid || back == PlaceID.pid_Mud || back == PlaceID.pid_Rubble) {
                                normalizeTile(field, x, y, tile, back, PlaceID.pid_Grass, PlaceID.pid_GrassMask, fog, true, select);
                            }
                        } else {
                            normalizeTile(field, x, y, tile, back, PlaceID.pid_Space, PlaceID.pid_SpaceMask, fog, false, select);
                            normalizeTile(field, x, y, tile, back, PlaceID.pid_Water, PlaceID.pid_WaterMask, fog, false, select);
                            normalizeTile(field, x, y, tile, back, PlaceID.pid_Liquid, PlaceID.pid_LiquidMask, fog, false, select);
                            normalizeTile(field, x, y, tile, back, PlaceID.pid_Lava, PlaceID.pid_LavaMask, fog, false, select);
                            normalizeTile(field, x, y, tile, back, PlaceID.pid_Mud, PlaceID.pid_MudMask, fog, false, select);
                            normalizeTile(field, x, y, tile, back, PlaceID.pid_Rubble, PlaceID.pid_RubbleMask, fog, false, select);
                        }
                    } else {
                        tile.FogExtID = (short) PlaceID.pid_Undefined;

                        int defp = (tile.FogID);

                        if (defp != PlaceID.pid_Undefined) {
                            if (defp == PlaceID.pid_Fog) {
                                normalizeTile(field, x, y, tile, defp, PlaceID.pid_Undefined, PlaceID.pid_FogMask2, fog, true, select);
                            }
                        } else {
                            normalizeTile(field, x, y, tile, defp, PlaceID.pid_Fog, PlaceID.pid_FogMask1, fog, false, select);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.normalizeFieldMasks(): " + ex.getMessage());
        }
    }

    private static void normalizeDungeon(AbstractMap map, Rect dungeonArea)
    {
        try {
            for (int y = dungeonArea.Top; y <= dungeonArea.Bottom; y++) {
                for (int x = dungeonArea.Left; x <= dungeonArea.Right; x++) {
                    BaseTile tile = map.getTile(x, y);

                    if (tile.Background == PlaceID.pid_CaveFloor) {
                        tile.Background = PlaceID.pid_DungeonFloor;
                    }

                    if (tile.Foreground == PlaceID.pid_CaveWall) {
                        tile.Foreground = PlaceID.pid_DungeonWall;
                    }
                }
            }

            for (int y = dungeonArea.Top; y <= dungeonArea.Bottom; y++) {
                for (int x = dungeonArea.Left; x <= dungeonArea.Right; x++) {
                    BaseTile tile = map.getTile(x, y);
                    
                    int fg = tile.Foreground;
                    if (fg == PlaceID.pid_DungeonWall) {
                        int magic = getAdjacentMagic(map, x, y, PlaceID.pid_Undefined, PlaceID.pid_Undefined, TSL_FORE);
                        if (magic != 0) {
                            for (int offset = 0; offset < dbDgnMagics.length; offset++) {
                                if (dbDgnMagics[offset].main.contains(magic)) {
                                    tile.setFore(PlaceID.pid_DungeonWall, offset);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.normalizeDungeon(): " + ex.getMessage());
        }
    }
    
    public static void normalizeRoad(AbstractMap map, int roadTile)
    {
        try {
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    NWTile tile = (NWTile) map.getTile(x, y);
                    
                    int fg = TypeUtils.getShortLo(tile.Foreground);
                    if (fg == roadTile) {
                        int magic = getAdjacentMagic(map, x, y, PlaceID.pid_Undefined, roadTile, TSL_FORE);
                        if (magic != 0) {
                            for (int offset = 0; offset < dbRoadMagics.length; offset++) {
                                if (dbRoadMagics[offset].main.contains(magic)) {
                                    tile.Foreground = BaseTile.getVarID((byte) roadTile, (byte) offset);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.normalizeRoad(): " + ex.getMessage());
        }
    }

    public static final void gen_Road(AbstractMap map, int px1, int py1, int px2, int py2, Rect area, int tid)
    {
        RoadBuilder rb = new RoadBuilder(map);
        rb.generate(px1, py1, px2, py2, 15, tid, UniverseBuilder::setRoadTile);

        //map.gen_Path(px1, py1, px2, py2, map.getAreaRect(), tid, false, false, UniverseBuilder::setRoadTile);
        UniverseBuilder.normalizeRoad(map, tid);
    }

    private static void setRoadTile(IMap map, int x, int y, Object extData, RefObject<Boolean> refContinue)
    {
        NWTile tile = (NWTile) map.getTile(x, y);
        int tid = (int) extData;
        
        if (tile != null) {
            int bg = tile.getBackBase();
            int fg = tile.getForeBase();

            if (fg == tid) {
                refContinue.argValue = false;
                return;
            }
            
            if (bg != PlaceID.pid_Floor) {
                if (fg == 0) {
                    tile.Foreground = (short) tid;
                } else {
                    int r = AuxUtils.getRandom(10);
                    if ((fg == PlaceID.pid_Tree && r == 0) || (fg == PlaceID.pid_Bush || fg == PlaceID.pid_Stump)) {
                        tile.Foreground = (short) tid;
                    }
                }
            }            
        }
    }
    
    private static void genAlfheimLiquidLake(NWField field, Rect area, int gran, int aTile, int Liquid)
    {
        UniverseBuilder.setAlfheimTile(field, area.Left + (area.Right - area.Left) / 2, area.Top + (area.Bottom - area.Top) / 2, aTile, Liquid);
        int g = gran / 2;
        if (g >= 1) {
            for (int y = area.Top; y <= area.Bottom; y += gran) {
                for (int x = area.Left; x <= area.Right; x += gran) {
                    if (AuxUtils.getRandom(2) == 0) {
                        UniverseBuilder.setAlfheimTile(field, x + g, y, UniverseBuilder.getAlfheimTileFull(field, x, y, aTile), Liquid);
                    } else {
                        UniverseBuilder.setAlfheimTile(field, x + g, y, UniverseBuilder.getAlfheimTileFull(field, x + gran, y, aTile), Liquid);
                    }
                    if (AuxUtils.getRandom(2) == 0) {
                        UniverseBuilder.setAlfheimTile(field, x, y + g, UniverseBuilder.getAlfheimTileFull(field, x, y, aTile), Liquid);
                    } else {
                        UniverseBuilder.setAlfheimTile(field, x, y + g, UniverseBuilder.getAlfheimTileFull(field, x, y + gran, aTile), Liquid);
                    }
                }
            }

            for (int y = area.Top; y <= area.Bottom; y += gran) {
                for (int x = area.Left; x <= area.Right; x += gran) {
                    int num = AuxUtils.getRandom(4) + 1;
                    int c;
                    if (num != 1) {
                        if (num != 2) {
                            if (num != 3) {
                                c = UniverseBuilder.getAlfheimTileFull(field, x + gran, y + g, aTile);
                            } else {
                                c = UniverseBuilder.getAlfheimTileFull(field, x, y + g, aTile);
                            }
                        } else {
                            c = UniverseBuilder.getAlfheimTileFull(field, x + g, y + gran, aTile);
                        }
                    } else {
                        c = UniverseBuilder.getAlfheimTileFull(field, x + g, y, aTile);
                    }
                    UniverseBuilder.setAlfheimTile(field, x + g, y + g, c, Liquid);
                }
            }
            if (g > 1) {
                UniverseBuilder.genAlfheimLiquidLake(field, area, g, aTile, Liquid);
            }
        }
    }

    private static int getAlfheimTileFull(NWField field, int x, int y, int placeID)
    {
        int result = placeID;
        NWTile tile = field.getTile(x, y);
        if (tile != null) {
            result = tile.getBackBase();
        }
        return result;
    }

    private static void setAlfheimTile(NWField field, int x, int y, int aTile, int Liquid)
    {
        NWTile tile = field.getTile(x, y);
        if (tile != null) {
            tile.setBack(aTile);
            tile.Lake_LiquidID = Liquid;
        }
    }

    public static void build_Alfheim(NWField field)
    {
        try {
            field.fillBackground(PlaceID.pid_Grass);
            field.fillForeground(PlaceID.pid_Undefined);
            UniverseBuilder.gen_Valley(field, false, false, false);

            int wid = GlobalVars.nwrBase.findEntryBySign("Potion_DrinkingWater").GUID;
            int cnt = AuxUtils.getBoundedRnd(20, 33);
            for (int i = 1; i <= cnt; i++) {
                int x = AuxUtils.getBoundedRnd(1, field.getWidth() - 2);
                int y = AuxUtils.getBoundedRnd(1, field.getHeight() - 2);
                int rad = 4;
                Rect area = new Rect(x - rad, y - rad, x + rad, y + rad);
                int lid = GlobalVars.dbPotions.get(AuxUtils.getBoundedRnd(0, GlobalVars.dbPotions.getCount() - 1));
                if (lid != wid) {
                    UniverseBuilder.genAlfheimLiquidLake(field, area, 4, PlaceID.pid_Liquid, lid);
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Alfheim(): " + ex.getMessage());
            throw ex;
        }
    }

    public static final void build_Vanaheim(NWField field)
    {
        try {
            field.fillBackground(PlaceID.pid_Grass);
            field.fillForeground(PlaceID.pid_Undefined);
            UniverseBuilder.gen_Valley(field, false, true, false);

            if (GlobalVars.Debug_TestWorldGen) {
                Logger.write("UniverseBuilder.build_Vanaheim().gen_Valley().ok");
            }

            Rect area = field.getAreaRect();

            Building.RuinsMode = true;

            int cnt = AuxUtils.getBoundedRnd(3, 7);
            for (int i = 1; i <= cnt; i++) {
                Building bld = new Building(GameSpace.getInstance(), field);

                if (bld.build((byte) 3, (byte) 4, (byte) 7, area)) {
                    bld.setID(BuildingID.bid_None);
                    field.getFeatures().add(bld);

                    Rect rt = bld.getArea();
                    int z = Math.round(rt.getSquare() * 0.25f);
                    for (int k = 1; k <= z; k++) {
                        int x = AuxUtils.getBoundedRnd(rt.Left, rt.Right);
                        int y = AuxUtils.getBoundedRnd(rt.Top, rt.Bottom);

                        BaseTile tile = field.getTile(x, y);
                        //tile.backGround = PlaceID.pid_Grass;
                        tile.Foreground = PlaceID.pid_Undefined;
                    }
                } else {
                    bld.dispose();
                }
            }

            Building.RuinsMode = false;
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Vanaheim(): " + ex.getMessage());
        }
    }
    
    public static void build_Bazaar(NWField field)
    {
        try {
            field.fillBackground(PlaceID.pid_Floor);
            field.fillForeground(PlaceID.pid_Undefined);

            Rect r = field.getAreaRect();

            int num = field.getHeight();
            for (int y = 0; y < num; y++) {
                int num2 = field.getWidth();
                for (int x = 0; x < num2; x++) {
                    NWTile tile = field.getTile(x, y);
                    tile.setFore(NWField.getBuildPlaceKind(x, y, r, false));
                }
            }

            for (int yy = 0; yy <= 1; yy++) {
                for (int xx = 0; xx <= 3; xx++) {
                    int x2 = 1 + xx * 19;
                    int y2 = 1 + yy * 9;

                    r = new Rect(x2, y2, x2 + 16, y2 + 6);
                    Building building = new Building(GameSpace.getInstance(), field);
                    building.setID(BuildingID.bid_House);
                    building.setArea(r.Left, r.Top, r.Right, r.Bottom);
                    building.prepare();
                    building.flush();
                    field.getFeatures().add(building);

                    int cx = AuxUtils.getBoundedRnd(r.Left + 1, r.Right - 1);
                    int cy = AuxUtils.getBoundedRnd(r.Top + 1, r.Bottom - 1);
                    NWCreature merchant = field.addCreature(cx, cy, GlobalVars.cid_Merchant);
                    merchant.setIsTrader(true);
                    merchant.addMoney(AuxUtils.getBoundedRnd(15000, 25000));
                    building.Holder = merchant;

                    for (int y = r.Top + 1; y <= r.Bottom - 1; y++) {
                        for (int x = r.Left + 1; x <= r.Right - 1; x++) {
                            DataEntry entry = null;

                            switch (AreaItem[yy][xx]) {
                                case bik_Ring:
                                    if (AuxUtils.chance(10)) {
                                        entry = GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Amulet);
                                    } else {
                                        entry = GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Ring);
                                    }
                                    break;
                                case bik_Potion:
                                    entry = GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Potion);
                                    break;
                                case bik_Wand:
                                    entry = GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Wand);
                                    break;
                                case bik_Food:
                                    entry = GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Food);
                                    break;
                                case bik_Scroll:
                                    entry = GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Scroll);
                                    break;
                                case bik_Armor:
                                    entry = GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Armor);
                                    break;
                                case bik_Tool: 
                                    entry = GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Tool);
                                    break;
                                case bik_Weapon:
                                    entry = GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Weapon);
                                    break;
                            }

                            if (entry != null) {
                                int id = ((ItemEntry) entry).getRnd();
                                field.addItem(x, y, id).Owner = merchant;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Bazaar(): " + ex.getMessage());
            throw ex;
        }
    }

    public static boolean gen_Creature(NWField field, int aID, int aX, int aY, boolean aFlock)
    {
        boolean result = false;

        try {
            ArrayList<Integer> validCreatures = field.ValidCreatures;
            int cnt = (validCreatures != null) ? validCreatures.size() : 0;
            if (cnt < 1) {
                return result;
            } else {
                int tries = 10;
                do {
                    tries--;
                    int cid;
                    if (aID == -1) {
                        cid = field.ValidCreatures.get(AuxUtils.getRandom(cnt));
                    } else {
                        cid = aID;
                    }

                    if (GlobalVars.nwrGame.canCreate(cid)) {
                        NWCreature cr = field.addCreature(aX, aY, cid);
                        result = true;
                        if (aFlock && (cr.getEntry().Flags.contains(CreatureFlags.esCohorts))) {
                            int num = AuxUtils.getBoundedRnd(1, 3);
                            for (int i = 1; i <= num; i++) {
                                Point pt = cr.getNearestPlace((int) cr.getSurvey(), true);
                                if (pt != null) {
                                    field.addCreature(pt.X, pt.Y, cid);
                                }
                            }
                        }
                    }
                } while (!result && tries != 0);
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.gen_Creature(): " + ex.getMessage());
        }

        return result;
    }

    public static void gen_Creatures(NWField field, int aCount)
    {
        try {
            if (aCount == -1) {
                aCount = AuxUtils.getBoundedRnd(10, 20);
            }

            for (int i = 1; i <= aCount; i++) {
                UniverseBuilder.gen_Creature(field, -1, -1, -1, true);
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.gen_Creatures(): " + ex.getMessage());
        }
    }

    private static int getRndDir(NWField field, int pX, int pY, int pOct)
    {
        int result = Directions.dtNone;
        
        OctantRec octRec = Octants[pOct - 1];
        int iX = (int) octRec.iX;
        int iY = (int) octRec.iY;
        int xLB = (int) octRec.xLB;
        int xHB = (int) octRec.xHB;
        int yLB = (int) octRec.yLB;
        int yHB = (int) octRec.yHB;

        // define the list of valid directions
        Directions vd = new Directions();
        int vdCnt = 0;

        for (int cd = Directions.dtFlatFirst; cd <= Directions.dtFlatLast; cd++) {
            if (octRec.ValidDirs.contains(cd)) {
                int newX = pX + Directions.Data[cd].dX;
                int newY = pY + Directions.Data[cd].dY;
                int dX = Math.abs(newX - iX);
                int dY = Math.abs(newY - iY);

                switch (pOct) {
                    case 1:
                    case 4: {
                        yHB = yLB + dX - 1;
                        break;
                    }
                    case 2:
                    case 7: {
                        xHB = xLB + dY - 1;
                        break;
                    }
                    case 3:
                    case 6: {
                        xLB = xHB - dY + 1;
                        break;
                    }
                    case 5:
                    case 8: {
                        yLB = yHB - dX + 1;
                        break;
                    }
                }

                if (newX >= xLB && newX <= xHB && newY >= yLB && newY <= yHB && field.isValid(newX, newY)) {
                    vd.include(cd);
                    vdCnt++;
                }
            }
        }

        if (vdCnt > 0) {
            int i = 0;
            int j = AuxUtils.getRandom(vdCnt) + 1;

            for (int cd = Directions.dtFlatFirst; cd <= Directions.dtFlatLast; cd++) {
                if (vd.contains(cd)) {
                    i++;
                }

                if (i == j) {
                    result = cd;
                    break;
                }
            }
        }

        return result;
    }

    public static void build_Crossroads(NWField field)
    {
        try {
            field.getTile(37, 8).setBack(PlaceID.pid_cr_yr);
            field.getTile(38, 8).setBack(PlaceID.pid_cr_ba);
            field.getTile(37, 9).setBack(PlaceID.pid_cr_gk);
            field.getTile(38, 9).setBack(PlaceID.pid_cr_wl);

            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = field.getTile(x, y);

                    if (x > 0 && x < StaticData.FieldWidth - 1) {
                        if (field.getTile(x - 1, y).getBackBase() == PlaceID.pid_cr_y && field.getTile(x + 1, y).getBackBase() == PlaceID.pid_cr_r) {
                            tile.setBack(PlaceID.pid_cr_yr);
                        }
                        if (field.getTile(x - 1, y).getBackBase() == PlaceID.pid_cr_b && field.getTile(x + 1, y).getBackBase() == PlaceID.pid_cr_a) {
                            tile.setBack(PlaceID.pid_cr_ba);
                        }
                        if (field.getTile(x - 1, y).getBackBase() == PlaceID.pid_cr_g && field.getTile(x + 1, y).getBackBase() == PlaceID.pid_cr_k) {
                            tile.setBack(PlaceID.pid_cr_gk);
                        }
                        if (field.getTile(x - 1, y).getBackBase() == PlaceID.pid_cr_w && field.getTile(x + 1, y).getBackBase() == PlaceID.pid_cr_l) {
                            tile.setBack(PlaceID.pid_cr_wl);
                        }
                    }
                }
            }

            for (int oct = 1; oct <= 8; oct++) {
                int pX = (int) Octants[oct - 1].iX;
                int pY = (int) Octants[oct - 1].iY;
                field.getTile(pX, pY).setFore(PlaceID.pid_cr_Disk);

                int num = (int) Octants[oct - 1].PathLen;
                for (int i = 1; i <= num; i++) {
                    int newDir = getRndDir(field, pX, pY, oct);
                    if (newDir == Directions.dtNone) {
                        break;
                    }

                    pX += Directions.Data[newDir].dX;
                    pY += Directions.Data[newDir].dY;
                    field.getTile(pX, pY).setFore(PlaceID.pid_cr_Disk);
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Crossroads(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void gen_CaveObjects(NWField field)
    {
        try {
            int num = AuxUtils.getBoundedRnd(150, 250);
            for (int i = 1; i <= num; i++) {
                int x = AuxUtils.getBoundedRnd(1, StaticData.FieldWidth - 2);
                int y = AuxUtils.getBoundedRnd(1, StaticData.FieldHeight - 2);

                BaseTile tile = field.getTile(x, y);
                if (!field.isBarrier(x, y) && (int) tile.Background == PlaceID.pid_Floor) {
                    tile.Foreground = (short) PlaceID.pid_Stalagmite;
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.gen_CaveObjects(): " + ex.getMessage());
            throw ex;
        }
    }

    private static void gen_ForestObjects(NWField field, Rect area, int count, int tileID)
    {
        for (int i = 1; i <= count; i++) {
            int x = AuxUtils.getBoundedRnd(area.Left, area.Right);
            int y = AuxUtils.getBoundedRnd(area.Top, area.Bottom);
            NWTile tile = field.getTile(x, y);
            if (tile.Foreground == PlaceID.pid_Undefined && tile.Background == PlaceID.pid_Grass) {
                tile.Foreground = NWField.getVarTile(tileID);
            }
        }
    }

    public static void build_Forest(NWField field)
    {
        try {
            int tid;
            if (field.LandID == GlobalVars.Land_GiollRiver || field.LandID == GlobalVars.Land_Niflheim) {
                tid = PlaceID.pid_DeadTree;
            } else {
                tid = PlaceID.pid_Tree;
            }
            int cnt;
            if (field.LandID == GlobalVars.Land_Forest) {
                cnt = 250;
            } else {
                if (field.LandID == GlobalVars.Land_VidRiver) {
                    cnt = 50;
                } else {
                    cnt = 150;
                }
            }

            UniverseBuilder.gen_ForestObjects(field, field.getAreaRect(), cnt, tid);

            if (field.LandID == GlobalVars.Land_Forest) {
                UniverseBuilder.gen_ForestObjects(field, field.getAreaRect(), 50, PlaceID.pid_Bush);
                UniverseBuilder.gen_ForestObjects(field, field.getAreaRect(), 25, PlaceID.pid_Stump);
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Forest(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void build_GrynrHalls(NWField field)
    {
        try {
            for (int i = 1; i <= 5; i++) {
                Point pt = field.searchFreeLocation();
                field.getTile(pt.X, pt.Y).Foreground = (short) PlaceID.pid_SmallPit;
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_GrynrHalls(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void gen_Items(NWField field, int itemsCount)
    {
        try {
            boolean caves = (field.LandID == GlobalVars.Land_Caves || field.LandID == GlobalVars.Land_DeepCaves || field.LandID == GlobalVars.Land_GreatCaves || field.LandID == GlobalVars.Land_GrynrHalls);
            boolean armory = (field.LandID == GlobalVars.Land_Armory);

            if (itemsCount == -1) {
                if (caves) {
                    itemsCount = AuxUtils.getBoundedRnd(5, 15);
                } else {
                    if (armory) {
                        itemsCount = AuxUtils.getBoundedRnd(10, 15);
                    } else {
                        itemsCount = AuxUtils.getBoundedRnd(5, 30);
                    }
                }
            }

            for (int i = 1; i <= itemsCount; i++) {
                while (true) {
                    int x = AuxUtils.getBoundedRnd(0, field.getWidth() - 1);
                    int y = AuxUtils.getBoundedRnd(0, field.getHeight() - 1);

                    NWTile tile = field.getTile(x, y);
                    int bg = tile.getBackBase();

                    if (!field.isBarrier(x, y) && (StaticData.dbPlaces[bg].Signs.contains(PlaceFlags.psIsGround))) {
                        ItemEntry eItem = null;

                        if (armory) {
                            int num = AuxUtils.getBoundedRnd(1, 2);
                            if (num == 1) {
                                eItem = (ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Armor);
                            } else {
                                eItem = (ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Weapon);
                            }
                        } else {
                            if (caves) {
                                switch (AuxUtils.getBoundedRnd(1, 5)) {
                                    case 1:
                                        eItem = ((ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Armor));
                                        break;
                                    case 2:
                                        eItem = ((ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Weapon));
                                        break;
                                    case 3:
                                        eItem = ((ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Scroll));
                                        break;
                                    case 4:
                                        eItem = ((ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Potion));
                                        break;
                                    case 5:
                                        eItem = ((ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_Wand));
                                        break;
                                }
                            } else {
                                eItem = ((ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Rnd_NatureObject));
                            }
                        }

                        if (eItem != null) {
                            int iid = eItem.getRnd();
                            Item item = new Item(GameSpace.getInstance(), field);
                            item.setCLSID(iid);
                            item.setPos(x, y);
                            field.getItems().add(item, false);
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.gen_Items(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void genBackTiles(NWField field, int count, int tileID)
    {
        for (int i = 1; i <= count; i++) {
            int x = AuxUtils.getBoundedRnd(0, StaticData.FieldWidth - 1);
            int y = AuxUtils.getBoundedRnd(0, StaticData.FieldHeight - 1);

            NWTile tile = field.getTile(x, y);
            if (tile.Background == PlaceID.pid_Grass) {
                tile.Background = NWField.getVarTile(tileID);
            }
        }
    }

    public static void build_MimerRealm(NWField field)
    {
        genBackTiles(field, AuxUtils.getBoundedRnd(20, 30), PlaceID.pid_Water);
        genBackTiles(field, AuxUtils.getBoundedRnd(20, 30), PlaceID.pid_Lava);
        genBackTiles(field, AuxUtils.getBoundedRnd(20, 30), PlaceID.pid_Quicksand);
        genBackTiles(field, AuxUtils.getBoundedRnd(20, 30), PlaceID.pid_Mud);
    }

    private static void changeMWTile(IMap map, int x, int y, Object extData, RefObject<Boolean> refContinue)
    {
        BaseTile tile = map.getTile(x, y);
        if (tile != null) {
            tile.Background = (short) PlaceID.pid_Water;
        }
    }

    public static void build_MimerWell(NWField field)
    {
        try {
            field.fillBackground(PlaceID.pid_Rock);
            field.fillForeground(PlaceID.pid_Undefined);

            int x = StaticData.FieldWidth / 2;
            int y = StaticData.FieldHeight / 2;
            Rect area = new Rect(x - 8, y - 8, x + 8, y + 8);
            field.gen_Lake(area, UniverseBuilder::changeMWTile);

            field.normalize();
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_MimerWell(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void build_Jotenheim_Jarnvidr(NWField field)
    {
        Rect area = new Rect(2, 3, 50, 15);
        gen_Forest(field, area, PlaceID.pid_IronTree, false);
        
        Region region = new Region(field.getSpace(), field);
        region.setArea(area);
        region.CLSID = field.getSpace().findDataEntry("iJarnvidr").GUID;
        field.getFeatures().add(region);
    }

    public static void gen_RarelyMountains(NWField field)
    {
        try {
            int cnt;
            if (field.LandID == GlobalVars.Land_Niflheim || field.LandID == GlobalVars.Land_Jotenheim) {
                cnt = 200;
            } else {
                cnt = 50;
            }

            for (int i = 1; i <= cnt; i++) {
                int x = AuxUtils.getBoundedRnd(0, StaticData.FieldWidth - 1);
                int y = AuxUtils.getBoundedRnd(0, StaticData.FieldHeight - 1);
                NWTile tile = field.getTile(x, y);
                int bg = tile.getBackBase();
                int fg = tile.getForeBase();

                if (fg == PlaceID.pid_Undefined && bg != PlaceID.pid_Space && bg != PlaceID.pid_Water) {
                    tile.setFore(PlaceID.pid_Mountain);
                }
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.gen_Mountains(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void build_Muspelheim(NWField field)
    {
        try {
            AuxUtils.randomize();

            int num = field.getHeight();
            for (int y = 0; y < num; y++) {
                int num2 = field.getWidth();
                for (int x = 0; x < num2; x++) {
                    NWTile tile = field.getTile(x, y);
                    tile.setBack(NWField.Muspel_bgTiles[AuxUtils.getBoundedRnd(0, 6)]);
                    tile.setFore(NWField.Muspel_fgTiles[AuxUtils.getBoundedRnd(0, 5)]);
                }
            }

            field.normalize();
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Muspelheim(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void regen_Muspelheim(NWField field)
    {
        try {
            AuxUtils.randomize();

            int bg = AuxUtils.getRandomArrayInt(NWField.Muspel_bgTiles);
            //int fg = AuxUtils.getRandomArrayInt(NWField.Muspel_fgTiles);

            field.spreadTiles(bg, 0.25f);
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.regen_Muspelheim(): " + ex.getMessage());
        }
    }

    public static void build_Ocean(NWField field)
    {
        if (GlobalVars.nwrGame.rt_Jormungand == null) {
            GlobalVars.nwrGame.rt_Jormungand = field.addCreature(-1, -1, GlobalVars.cid_Jormungand);
        }
    }

    private static void fillAreaItems(NWField field, int x1, int y1, int x2, int y2, int meta_id)
    {
        for (int yy = y1; yy <= y2; yy++) {
            for (int xx = x1; xx <= x2; xx++) {
                ItemEntry aEntry = (ItemEntry) GlobalVars.nwrBase.getEntry(meta_id);
                for (int i = 1; i <= 10; i++) {
                    int id = aEntry.getRnd();
                    Item res = new Item(GameSpace.getInstance(), field);
                    res.setCLSID(id);
                    res.Count = 1;
                    res.setIdentified(true);
                    res.setPos(xx, yy);
                    field.getItems().add(res, false);
                }
            }
        }
    }

    public static void gen_Traps(NWField field, int factor)
    {
        try {
            int f = factor;

            if (field.LandID == GlobalVars.Land_Village) {
                if (factor < 0) {
                    f = -2;
                }
                field.addSeveralTraps(new int[]{PlaceID.pid_PitTrap, PlaceID.pid_TeleportTrap}, f);
            }

            if (field.LandID == GlobalVars.Land_Forest) {
                if (factor < 0) {
                    f = -3;
                }
                field.addSeveralTraps(new int[]{PlaceID.pid_PoisonSpikeTrap, PlaceID.pid_WaterTrap, PlaceID.pid_PitTrap}, f);
            }

            if (field.LandID == GlobalVars.Land_Alfheim) {
                if (factor < 0) {
                    f = -4;
                }
                field.addSeveralTraps(new int[]{PlaceID.pid_StunGasTrap, PlaceID.pid_PoisonSpikeTrap, PlaceID.pid_FrostTrap, PlaceID.pid_PitTrap, PlaceID.pid_LavaTrap}, f);
            }

            if (field.LandID == GlobalVars.Land_Wasteland) {
                if (factor < 0) {
                    f = -3;
                }
                field.addSeveralTraps(new int[]{PlaceID.pid_FireTrap, PlaceID.pid_LavaTrap}, f);
            }

            if (field.LandID == GlobalVars.Land_Niflheim) {
                if (factor < 0) {
                    f = -3;
                }
                field.addSeveralTraps(new int[]{PlaceID.pid_FrostTrap, PlaceID.pid_FireTrap, PlaceID.pid_PitTrap, PlaceID.pid_TeleportTrap, PlaceID.pid_StunGasTrap, PlaceID.pid_LavaTrap}, f);
            }

            if (field.LandID == GlobalVars.Land_Nidavellir) {
                if (factor < 0) {
                    f = -3;
                }
                field.addSeveralTraps(new int[]{PlaceID.pid_FireTrap}, f);
            }

            if (field.LandID == GlobalVars.Land_Vanaheim) {
                if (factor < 0) {
                    f = -7;
                }
                field.addSeveralTraps(new int[]{PlaceID.pid_PoisonSpikeTrap, PlaceID.pid_QuicksandTrap, PlaceID.pid_WaterTrap, PlaceID.pid_StunGasTrap, PlaceID.pid_PitTrap, PlaceID.pid_MistTrap, PlaceID.pid_ArrowTrap, PlaceID.pid_TeleportTrap}, f);
            }
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.gen_Traps(): " + ex.getMessage());
            throw ex;
        }
    }

    private static void drawWall(NWField field, int x, int pid)
    {
        for (int y = 0; y < StaticData.FieldHeight; y++) {
            NWTile tile = field.getTile(x, y);
            tile.setBack(PlaceID.pid_Floor);
            tile.setFore(pid);
        }
    }

    public static void build_GodsFortress(NWField field)
    {
        try {
            Building.drawWalls(field, new Rect(33, 0, 64, 6));
            field.getTile(36, 6).setFore(PlaceID.pid_DoorS);
            field.getTile(37, 6).setFore(PlaceID.pid_DoorS);
            field.getTile(47, 6).setFore(PlaceID.pid_DoorS);
            field.getTile(48, 6).setFore(PlaceID.pid_DoorS);
            field.getTile(60, 6).setFore(PlaceID.pid_DoorS);
            field.getTile(61, 6).setFore(PlaceID.pid_DoorS);

            Building.drawWalls(field, new Rect(32, 10, 41, 17));
            field.getTile(36, 10).setFore(PlaceID.pid_DoorN);
            field.getTile(37, 10).setFore(PlaceID.pid_DoorN);

            Building.drawWalls(field, new Rect(43, 10, 52, 17));
            field.getTile(47, 10).setFore(PlaceID.pid_DoorN);
            field.getTile(48, 10).setFore(PlaceID.pid_DoorN);

            Building.drawWalls(field, new Rect(54, 10, 67, 17));
            field.getTile(60, 10).setFore(PlaceID.pid_DoorN);
            field.getTile(61, 10).setFore(PlaceID.pid_DoorN);
            //
            //
            UniverseBuilder.fillAreaItems(field, 33, 14, 40, 16, GlobalVars.iid_Rnd_Scroll);
            UniverseBuilder.fillAreaItems(field, 44, 14, 51, 16, GlobalVars.iid_Rnd_Potion);
            UniverseBuilder.fillAreaItems(field, 55, 14, 66, 14, GlobalVars.iid_Rnd_Armor);
            UniverseBuilder.fillAreaItems(field, 55, 15, 66, 15, GlobalVars.iid_Rnd_Weapon);
            UniverseBuilder.fillAreaItems(field, 55, 16, 66, 16, GlobalVars.iid_Rnd_Wand);
            //
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_GodsFortress(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void build_Valhalla(NWField field)
    {
        try {
            drawWall(field, 1, PlaceID.pid_WallW);
            field.getTile(1, 8).setFore(PlaceID.pid_DoorW);
            field.getTile(1, 9).setFore(PlaceID.pid_DoorW);

            drawWall(field, 72, PlaceID.pid_WallE);
            field.getTile(72, 8).setFore(PlaceID.pid_DoorE);
            field.getTile(72, 9).setFore(PlaceID.pid_DoorE);
            //
            Building.drawWalls(field, new Rect(33, 0, 64, 6));
            field.getTile(33, 3).setFore(PlaceID.pid_DoorW);

            Building.drawWalls(field, new Rect(21, 10, 30, 17));
            field.getTile(25, 10).setFore(PlaceID.pid_DoorN);
            field.getTile(26, 10).setFore(PlaceID.pid_DoorN);

            Building.drawWalls(field, new Rect(32, 10, 41, 17));
            field.getTile(36, 10).setFore(PlaceID.pid_DoorN);
            field.getTile(37, 10).setFore(PlaceID.pid_DoorN);

            Building.drawWalls(field, new Rect(43, 10, 52, 17));
            field.getTile(47, 10).setFore(PlaceID.pid_DoorN);
            field.getTile(48, 10).setFore(PlaceID.pid_DoorN);

            Building.drawWalls(field, new Rect(54, 10, 67, 17));
            field.getTile(60, 10).setFore(PlaceID.pid_DoorN);
            field.getTile(61, 10).setFore(PlaceID.pid_DoorN);
            //
            field.addCreature(-1, -1, GlobalVars.cid_Thor);
            field.addCreature(-1, -1, GlobalVars.cid_Tyr);
            field.addCreature(-1, -1, GlobalVars.cid_Freyr);
            field.addCreature(-1, -1, GlobalVars.cid_Odin);
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Valhalla(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void build_VidurTemple(NWField field)
    {
        try {
            field.addCreature(-1, -1, GlobalVars.cid_Agnar);
            field.addCreature(-1, -1, GlobalVars.cid_Haddingr);
            field.addCreature(-1, -1, GlobalVars.cid_Ketill);
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_VidurTemple(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void build_Village(NWField field)
    {
        try {
            Village village = new Village(GameSpace.getInstance(), field);
            field.getFeatures().add(village);

            Rect v = village.getArea().clone();
            v.inflate(1, 1);
            village.buildVillage(field, v);

            int num = field.getFeatures().getCount();
            for (int i = 0; i < num; i++) {
                GameEntity entry = field.getFeatures().getItem(i);
                if (entry instanceof Building) {
                    ((Building) entry).prepare();
                }
            }

            GlobalVars.nwrGame.addTownsman(field, v, GlobalVars.cid_Jarl);

            int cnt = AuxUtils.getBoundedRnd(5, 9);
            for (int i = 1; i <= cnt; i++) {
                GlobalVars.nwrGame.addTownsman(field, v, GlobalVars.cid_Guardsman);
            }

            field.research(true, TileStates.include(0, TileStates.TS_VISITED));
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Village(): " + ex.getMessage());
            throw ex;
        }
    }

    private static void changeWLTile(IMap map, int x, int y, Object extData, RefObject<Boolean> refContinue)
    {
        BaseTile tile = map.getTile(x, y);
        if (tile != null) {
            tile.Background = (short) PlaceID.pid_Lava;
        }
    }

    public static void build_Wasteland(NWField field)
    {
        try {
            int x = AuxUtils.getBoundedRnd(1, field.getWidth() - 2);
            int y = AuxUtils.getBoundedRnd(1, field.getHeight() - 2);
            int rad = AuxUtils.getBoundedRnd(2, 5);

            Rect area = new Rect(x - rad, y - rad, x + rad, y + rad);
            field.gen_Lake(area, UniverseBuilder::changeWLTile);

            field.getTile(x, y).Foreground = (short) PlaceID.pid_Vulcan;

            field.normalize();
        } catch (Exception ex) {
            Logger.write("UniverseBuilder.build_Wasteland(): " + ex.getMessage());
            throw ex;
        }
    }

    private static void changeFogTile(IMap map, int x, int y, Object extData, RefObject<Boolean> refContinue)
    {
        BaseTile tile = map.getTile(x, y);
        if (tile != null) {
            NWTile nwTile = (NWTile) tile;
            nwTile.FogID = (short) PlaceID.pid_Fog;
            nwTile.FogAge = -1;
        }
    }

    public static final void gen_GiollFog(NWField field, int aX, int aY)
    {
        Rect area = new Rect(aX - 10, aY - 5, aX + 10, aY + 5);
        field.gen_RarefySpace(area, UniverseBuilder::changeFogTile, 8, 400);

        NWTile tile = field.getTile(aX, aY);
        tile.FogID = (short) PlaceID.pid_Undefined;
        tile.FogAge = 0;

        field.normalizeFog();
    }

    public static boolean isWaters(int placeID)
    {
        for (int i = 0; i < dbWaters.length; i++) {
            if (dbWaters[i] == placeID) {
                return true;
            }
        }
        return false;
    }

    public static void gen_BigRiver(AbstractMap map)
    {
        int y = 0;
        int x = 0;
        int cur_hgt = map.getHeight();
        int cur_wid = map.getWidth();

        int y2 = AuxUtils.getRandom(cur_hgt / 2 - 2) + cur_hgt / 2;
        int x2 = AuxUtils.getRandom(cur_wid / 2 - 2) + cur_wid / 2;

        int num = AuxUtils.getRandom(4);
        switch (num) {
            case 0:
                x = AuxUtils.getRandom(cur_wid - 2) + 1;
                y = 1;
                break;
            case 1:
                x = 1;
                y = AuxUtils.getRandom(cur_hgt - 2) + 1;
                break;
            case 2:
                x = cur_wid - 1;
                y = AuxUtils.getRandom(cur_hgt - 2) + 1;
                break;
            case 3:
                x = AuxUtils.getRandom(cur_wid - 2) + 1;
                y = cur_hgt - 1;
                break;
        }

        map.gen_Path(x, y, x2, y2, map.getAreaRect(), PlaceID.pid_Water, true, true, null);
    }

    private static boolean checkNW(AbstractMap map, int x, int y, int dmin, int dmax)
    {
        int num = Math.min(x + dmax, map.getWidth() - 1);
        for (int i = Math.max(x - dmax, 0); i <= num; i++) {
            int num2 = Math.min(y + dmax, map.getHeight() - 1);
            for (int j = Math.max(y - dmax, 0); j <= num2; j++) {
                if (Math.hypot((double) (y - j), (double) (x - i)) >= (double) dmin && Math.hypot((double) (y - j), (double) (x - i)) <= (double) dmax && !MapUtils.isWaters(map, map.getTile(i, j).Background)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void normalizeWater(AbstractMap map, Rect area)
    {
        for (int y = area.Top; y <= area.Bottom; y++) {
            for (int x = area.Left; x <= area.Right; x++) {
                BaseTile tile = map.getTile(x, y);
                if (tile != null && tile.Background == map.translateTile(TileType.ttWater)) {
                    if (checkNW(map, x, y, 1, 1)) {
                        tile.Background = map.translateTile(TileType.ttWater);
                    } else {
                        if (checkNW(map, x, y, 2, 3)) {
                            tile.Background = map.translateTile(TileType.ttDeepWater);
                        } else {
                            tile.Background = map.translateTile(TileType.ttDeeperWater);
                        }
                    }
                }
            }
        }
    }

    public static void gen_ForeObjects(AbstractMap map, short tid, float freq)
    {
        int cnt = (int) Math.round((double) (map.getWidth() * map.getHeight()) * freq);
        for (int t = 1; t <= cnt; t++) {
            int X = AuxUtils.getBoundedRnd(1, map.getWidth() - 2);
            int Y = AuxUtils.getBoundedRnd(1, map.getHeight() - 2);
            BaseTile tile = map.getTile(X, Y);
            if (tile != null && tile.Foreground == 0) {
                tile.Foreground = tid;
            }
        }
    }

    public static void gen_MountainRanges(AbstractMap map)
    {
        AuxUtils.randomize();
        gen_ForeObjects(map, map.translateTile(TileType.ttMountain), 0.45f);

        int apply; // for debug in future
        do {
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    int count = map.checkForeAdjacently(x, y, TileType.ttMountain);

                    if (map.checkFore(x, y, TileType.ttMountain)) {
                        if (count < 4) {
                            map.getTile(x, y).Foreground = map.translateTile(TileType.ttUndefined);
                        }
                    } else {
                        if (count > 4) {
                            map.getTile(x, y).Foreground = map.translateTile(TileType.ttMountain);
                        }
                    }
                }
            }
            apply = 1;
        } while (apply < 1);
    }

    public static void gen_Forest(AbstractMap map, Rect area, int tileID, boolean fieldClear)
    {
        if (fieldClear) {
            map.fillBackground(map.translateTile(TileType.ttGrass));
            map.fillForeground(map.translateTile(TileType.ttUndefined));
        }

        int num = AuxUtils.getBoundedRnd(15, 25);
        for (int i = 1; i <= num; i++) {
            int x = AuxUtils.getBoundedRnd(area.Left, area.Right);
            int y = AuxUtils.getBoundedRnd(area.Top, area.Bottom);

            int j = 5;
            do {
                int dir = AuxUtils.getBoundedRnd(Directions.dtNorth, Directions.dtSouthEast);
                x += Directions.Data[dir].dX;
                y += Directions.Data[dir].dY;

                BaseTile tile = map.getTile(x, y);
                if (tile != null && tile.Foreground == 0) {
                    tile.Foreground = (short) tileID;
                }
                j--;
            } while (j != 0);
        }
    }

    public static void gen_Valley(AbstractMap map, boolean clear, boolean river, boolean lakes)
    {
        if (clear) {
            map.fillBackground(map.translateTile(TileType.ttGrass));
            map.fillForeground(map.translateTile(TileType.ttUndefined));
        }

        gen_MountainRanges(map);
        gen_Forest(map, map.getAreaRect(), PlaceID.pid_Tree, false);
        
        if (river) {
            gen_BigRiver(map);
        }

        if (lakes) {
            int cnt = AuxUtils.getBoundedRnd(4, Math.round((float) map.getHeight() / (float) map.getWidth() * 15.0f));
            for (int i = 1; i <= cnt; i++) {
                int X = AuxUtils.getBoundedRnd(map.getAreaRect().Left, map.getAreaRect().Right);
                int Y = AuxUtils.getBoundedRnd(map.getAreaRect().Top, map.getAreaRect().Bottom);
                int bg = (int) map.getTile(X, Y).Background;
                if (!MapUtils.isWaters(map, (short) bg)) {
                    int rad = AuxUtils.getBoundedRnd(4, 10);
                    map.gen_Lake(new Rect(X - rad, Y - rad, X + rad, Y + rad), UniverseBuilder::lakeTilesChanger);
                }
            }
        }
    }

    private static void lakeTilesChanger(IMap map, int x, int y, Object extData, RefObject<Boolean> refContinue)
    {
        BaseTile tile = map.getTile(x, y);
        if (tile != null && tile.getBackBase() == PlaceID.pid_Grass && tile.Foreground == 0) {
            tile.Background = PlaceID.pid_Water;
        }
    }
}
