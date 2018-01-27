/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih.
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

using System;
using System.Collections.Generic;
using BSLib;
using ZRLib.Core;
using ZRLib.Map;
using ZRLib.Map.Builders;
using ZRLib.Map.Dungeons;
using NWR.Core;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Database;
using NWR.Items;
using NWR.Game;

namespace NWR.Universe
{
    public static class UniverseBuilder
    {
        // Bazaar Item Kinds
        private const int Bik_Ring = 0;
        private const int Bik_Potion = 1;
        private const int Bik_Wand = 2;
        private const int Bik_Food = 3;
        private const int Bik_Scroll = 4;
        private const int Bik_Armor = 5;
        private const int Bik_Tool = 6;
        private const int Bik_Weapon = 7;

        private static readonly int[,] AreaItem;
        private static readonly OctantRec[] Octants;
        private static readonly int[] DbWaters;
        private static readonly MagicRec[] DbDgnMagics;
        private static readonly MagicRec[] DbRoadMagics;
        public static readonly ExtPoint[] DbAdjacentTiles;
        public static readonly MagicRec[] DbMaskMagics;

        static UniverseBuilder()
        {
            AreaItem = new int[2,4];
            AreaItem[0, 0] = Bik_Ring;
            AreaItem[0, 1] = Bik_Potion;
            AreaItem[0, 2] = Bik_Wand;
            AreaItem[0, 3] = Bik_Food;
            AreaItem[1, 0] = Bik_Scroll;
            AreaItem[1, 1] = Bik_Armor;
            AreaItem[1, 2] = Bik_Tool;
            AreaItem[1, 3] = Bik_Weapon;

            Octants = new OctantRec[8];
            Octants[0] = new OctantRec(38, 9, 50, 38, 75, 9, 17, new Directions(Directions.DtNorth, Directions.DtSouth, Directions.DtEast, Directions.DtNorthEast, Directions.DtSouthEast));
            Octants[1] = new OctantRec(38, 9, 10, 38, 46, 9, 17, new Directions(Directions.DtSouth, Directions.DtWest, Directions.DtEast, Directions.DtSouthWest, Directions.DtSouthEast));
            Octants[2] = new OctantRec(37, 9, 10, 29, 37, 9, 17, new Directions(Directions.DtSouth, Directions.DtWest, Directions.DtEast, Directions.DtSouthWest, Directions.DtSouthEast));
            Octants[3] = new OctantRec(37, 9, 50, 0, 37, 9, 17, new Directions(Directions.DtNorth, Directions.DtSouth, Directions.DtWest, Directions.DtNorthWest, Directions.DtSouthWest));
            Octants[4] = new OctantRec(37, 8, 50, 0, 37, 0, 8, new Directions(Directions.DtNorth, Directions.DtSouth, Directions.DtWest, Directions.DtNorthWest, Directions.DtSouthWest));
            Octants[5] = new OctantRec(37, 8, 10, 29, 37, 0, 8, new Directions(Directions.DtNorth, Directions.DtWest, Directions.DtEast, Directions.DtNorthWest, Directions.DtNorthEast));
            Octants[6] = new OctantRec(38, 8, 10, 38, 46, 0, 8, new Directions(Directions.DtNorth, Directions.DtWest, Directions.DtEast, Directions.DtNorthWest, Directions.DtNorthEast));
            Octants[7] = new OctantRec(38, 8, 50, 38, 75, 0, 8, new Directions(Directions.DtNorth, Directions.DtSouth, Directions.DtEast, Directions.DtNorthEast, Directions.DtSouthEast));

            DbWaters = new int[] {
                PlaceID.pid_WaterTrap,
                PlaceID.pid_LavaPool,
                PlaceID.pid_Water,
                PlaceID.pid_Quicksand,
                PlaceID.pid_Lava,
                PlaceID.pid_Liquid,
                PlaceID.pid_Mud
            };

            DbAdjacentTiles = new ExtPoint[8];
            DbAdjacentTiles[0] = new ExtPoint(0, -1);
            DbAdjacentTiles[1] = new ExtPoint(+1, -1);
            DbAdjacentTiles[2] = new ExtPoint(+1, 0);
            DbAdjacentTiles[3] = new ExtPoint(+1, +1);
            DbAdjacentTiles[4] = new ExtPoint(0, +1);
            DbAdjacentTiles[5] = new ExtPoint(-1, +1);
            DbAdjacentTiles[6] = new ExtPoint(-1, 0);
            DbAdjacentTiles[7] = new ExtPoint(-1, -1);

            DbDgnMagics = new MagicRec[47];
            DbDgnMagics[0] = new MagicRec(new BytesSet(), new BytesSet());
            DbDgnMagics[1] = new MagicRec(new BytesSet(1, 3, 129, 131), new BytesSet());
            DbDgnMagics[2] = new MagicRec(new BytesSet(7, 15, 135, 143), new BytesSet());
            DbDgnMagics[3] = new MagicRec(new BytesSet(4, 6, 12, 14), new BytesSet());
            DbDgnMagics[4] = new MagicRec(new BytesSet(28, 30, 60, 62), new BytesSet());
            DbDgnMagics[5] = new MagicRec(new BytesSet(16, 24, 48, 56), new BytesSet());
            DbDgnMagics[6] = new MagicRec(new BytesSet(112, 120, 240, 248), new BytesSet());
            DbDgnMagics[7] = new MagicRec(new BytesSet(64, 96, 192, 224), new BytesSet());
            DbDgnMagics[8] = new MagicRec(new BytesSet(193, 195, 225, 227), new BytesSet());

            DbDgnMagics[9] = new MagicRec(new BytesSet(17, 19, 25, 27, 49, 51, 57, 59, 145, 147, 153, 155, 177, 179, 185, 187), new BytesSet());
            DbDgnMagics[10] = new MagicRec(new BytesSet(68, 70, 76, 78, 100, 102, 108, 110, 196, 198, 204, 206, 228, 230, 236, 238), new BytesSet());

            DbDgnMagics[11] = new MagicRec(new BytesSet(199, 207, 231, 239), new BytesSet());
            DbDgnMagics[12] = new MagicRec(new BytesSet(31, 63, 159, 191), new BytesSet());
            DbDgnMagics[13] = new MagicRec(new BytesSet(124, 126, 252, 254), new BytesSet());
            DbDgnMagics[14] = new MagicRec(new BytesSet(241, 243, 249, 251), new BytesSet());
            DbDgnMagics[15] = new MagicRec(new BytesSet(170), new BytesSet());

            DbDgnMagics[16] = new MagicRec(new BytesSet(130), new BytesSet());
            DbDgnMagics[17] = new MagicRec(new BytesSet(2), new BytesSet());
            DbDgnMagics[18] = new MagicRec(new BytesSet(10), new BytesSet());
            DbDgnMagics[19] = new MagicRec(new BytesSet(8), new BytesSet());
            DbDgnMagics[20] = new MagicRec(new BytesSet(40), new BytesSet());
            DbDgnMagics[21] = new MagicRec(new BytesSet(32), new BytesSet());
            DbDgnMagics[22] = new MagicRec(new BytesSet(160), new BytesSet());
            DbDgnMagics[23] = new MagicRec(new BytesSet(128), new BytesSet());

            DbDgnMagics[24] = new MagicRec(new BytesSet(39, 47, 167, 175), new BytesSet());
            DbDgnMagics[25] = new MagicRec(new BytesSet(156, 158, 188, 190), new BytesSet());
            DbDgnMagics[26] = new MagicRec(new BytesSet(114, 122, 242, 250), new BytesSet());
            DbDgnMagics[27] = new MagicRec(new BytesSet(201, 203, 233, 235), new BytesSet());

            DbDgnMagics[28] = new MagicRec(new BytesSet(41, 43, 169, 171), new BytesSet());
            DbDgnMagics[29] = new MagicRec(new BytesSet(33, 35, 161, 163), new BytesSet());
            DbDgnMagics[30] = new MagicRec(new BytesSet(9, 11, 137, 139), new BytesSet());
            DbDgnMagics[31] = new MagicRec(new BytesSet(164, 166, 172, 174), new BytesSet());
            DbDgnMagics[32] = new MagicRec(new BytesSet(132, 134, 140, 142), new BytesSet());
            DbDgnMagics[33] = new MagicRec(new BytesSet(36, 38, 44, 46), new BytesSet());
            DbDgnMagics[34] = new MagicRec(new BytesSet(146, 154, 178, 186), new BytesSet());
            DbDgnMagics[35] = new MagicRec(new BytesSet(144, 152, 176, 184), new BytesSet());
            DbDgnMagics[36] = new MagicRec(new BytesSet(18, 26, 50, 58), new BytesSet());
            DbDgnMagics[37] = new MagicRec(new BytesSet(74, 106, 202, 234), new BytesSet());
            DbDgnMagics[38] = new MagicRec(new BytesSet(66, 98, 194, 226), new BytesSet());
            DbDgnMagics[39] = new MagicRec(new BytesSet(72, 104, 200, 232), new BytesSet());

            DbDgnMagics[40] = new MagicRec(new BytesSet(255), new BytesSet());
            DbDgnMagics[41] = new MagicRec(new BytesSet(34), new BytesSet());
            DbDgnMagics[42] = new MagicRec(new BytesSet(136), new BytesSet());

            DbDgnMagics[43] = new MagicRec(new BytesSet(138), new BytesSet());
            DbDgnMagics[44] = new MagicRec(new BytesSet(42), new BytesSet());
            DbDgnMagics[45] = new MagicRec(new BytesSet(168), new BytesSet());
            DbDgnMagics[46] = new MagicRec(new BytesSet(162), new BytesSet());

            DbRoadMagics = new MagicRec[16];
            DbRoadMagics[0] = new MagicRec(new BytesSet(), new BytesSet());
            DbRoadMagics[1] = new MagicRec(new BytesSet(68, 70, 76, 78, 100, 102, 110, 196, 204, 206, 228, 230, 236), new BytesSet());
            DbRoadMagics[2] = new MagicRec(new BytesSet(17, 19, 25, 49, 51, 57, 59, 145, 147, 153, 155, 179, 185), new BytesSet());
            DbRoadMagics[3] = new MagicRec(new BytesSet(0, 85), new BytesSet());
            DbRoadMagics[4] = new MagicRec(new BytesSet(84, 86, 212, 214), new BytesSet());
            DbRoadMagics[5] = new MagicRec(new BytesSet(81, 83, 89, 91), new BytesSet());
            DbRoadMagics[6] = new MagicRec(new BytesSet(69, 77, 101, 109), new BytesSet());
            DbRoadMagics[7] = new MagicRec(new BytesSet(21, 53, 149, 181), new BytesSet());
            DbRoadMagics[8] = new MagicRec(new BytesSet(80, 88, 208, 216), new BytesSet());
            DbRoadMagics[9] = new MagicRec(new BytesSet(65, 67, 97, 99), new BytesSet());
            DbRoadMagics[10] = new MagicRec(new BytesSet(5, 13, 133, 141), new BytesSet());
            DbRoadMagics[11] = new MagicRec(new BytesSet(20, 22, 52, 54), new BytesSet());
            DbRoadMagics[12] = new MagicRec(new BytesSet(64, 96, 98, 192, 200, 224, 66, 72), new BytesSet());
            DbRoadMagics[13] = new MagicRec(new BytesSet(16, 24, 48, 50, 56, 152, 18, 144), new BytesSet());
            DbRoadMagics[14] = new MagicRec(new BytesSet(4, 6, 12, 14, 38, 140, 36, 132), new BytesSet());
            DbRoadMagics[15] = new MagicRec(new BytesSet(1, 3, 35, 129, 131, 137, 9, 33), new BytesSet());

            DbMaskMagics = new MagicRec[15];
            DbMaskMagics[0] = new MagicRec(new BytesSet(7, 15, 39, 47, 135, 143, 167, 175), new BytesSet());
            DbMaskMagics[1] = new MagicRec(new BytesSet(28, 30, 60, 62, 156, 158, 188, 190), new BytesSet());
            DbMaskMagics[2] = new MagicRec(new BytesSet(112, 114, 120, 122, 240, 242, 248, 250), new BytesSet());
            DbMaskMagics[3] = new MagicRec(new BytesSet(193, 195, 201, 203, 225, 227, 233, 235), new BytesSet());
            DbMaskMagics[4] = new MagicRec(new BytesSet(199, 207, 231, 111, 237, 239), new BytesSet());
            DbMaskMagics[5] = new MagicRec(new BytesSet(31, 63, 159, 183, 189, 191), new BytesSet());
            DbMaskMagics[6] = new MagicRec(new BytesSet(124, 126, 252, 222, 246, 254), new BytesSet());
            DbMaskMagics[7] = new MagicRec(new BytesSet(241, 243, 249, 123, 219, 251), new BytesSet());
            DbMaskMagics[8] = new MagicRec(new BytesSet(127, 223, 247, 253, 255), new BytesSet());
            DbMaskMagics[9] = new MagicRec(new BytesSet(), new BytesSet());
            DbMaskMagics[10] = new MagicRec(new BytesSet(), new BytesSet());
            DbMaskMagics[11] = new MagicRec(new BytesSet(), new BytesSet());
            DbMaskMagics[12] = new MagicRec(new BytesSet(), new BytesSet());
            DbMaskMagics[13] = new MagicRec(new BytesSet(119), new BytesSet());
            DbMaskMagics[14] = new MagicRec(new BytesSet(221), new BytesSet());
        }

        public static ushort TranslateTile(TileType aTile)
        {
            ushort res = (ushort)PlaceID.pid_Undefined;

            switch (aTile) {
                case TileType.ttGrass:
                    res = NWField.GetVarTile(PlaceID.pid_Grass);
                    break;

                case TileType.ttTree:
                    res = (ushort)PlaceID.pid_Tree;
                    break;

                case TileType.ttWater:
                case TileType.ttDeepWater:
                case TileType.ttDeeperWater:
                    res = NWField.GetVarTile(PlaceID.pid_Water);
                    break;

                case TileType.ttBWallN:
                    res = (ushort)PlaceID.pid_FenceN;
                    break;

                case TileType.ttBWallS:
                    res = (ushort)PlaceID.pid_FenceS;
                    break;

                case TileType.ttBWallW:
                    res = (ushort)PlaceID.pid_FenceW;
                    break;

                case TileType.ttBWallE:
                    res = (ushort)PlaceID.pid_FenceE;
                    break;

                case TileType.ttBWallNW:
                    res = (ushort)PlaceID.pid_FenceNW;
                    break;

                case TileType.ttBWallNE:
                    res = (ushort)PlaceID.pid_FenceNE;
                    break;

                case TileType.ttBWallSW:
                    res = (ushort)PlaceID.pid_FenceSW;
                    break;

                case TileType.ttBWallSE:
                    res = (ushort)PlaceID.pid_FenceSE;
                    break;

                case TileType.ttCaveFloor:
                    res = (ushort)PlaceID.pid_CaveFloor;
                    break;

                case TileType.ttRock:
                case TileType.ttCaveWall:
                    res = (ushort)PlaceID.pid_CaveWall;
                    break;

                case TileType.ttMountain:
                    res = (ushort)PlaceID.pid_Mountain;
                    break;


                case TileType.ttLinearCorridorWall:
                    res = (ushort)PlaceID.pid_Stalagmite;
                    break;

                case TileType.ttRectRoomWall:
                    res = (ushort)PlaceID.pid_QuicksandTrap;
                    break;

                case TileType.ttCylindricityRoomWall:
                    res = (ushort)PlaceID.pid_LavaTrap;
                    break;

                case TileType.ttDungeonWall:
                    res = (ushort)PlaceID.pid_CaveWall;
                    break;
            }

            return res;
        }

        public static void Build_Caves(AbstractMap map, ExtRect area)
        {
            CaveBuilder cavesBuilder = new CaveBuilder(map, area);
            cavesBuilder.Build();
        }

        public static void Build_Dungeon(AbstractMap map, ExtRect dungeonArea)
        { //, boolean debugByStep
            try {
                DungeonBuilder builder = new DungeonBuilder(map, dungeonArea);
                try {
                    /*TDungeonBuilder.Debug_StepByStep = debugByStep;*/

                    builder.SetAreaWeight(AreaType.AtRectangularRoom, 45);
                    builder.SetAreaWeight(AreaType.AtLinearCorridor, 20);
                    builder.SetAreaWeight(AreaType.AtCylindricityRoom, 0);
                    builder.SetAreaWeight(AreaType.AtQuadrantCorridor, 0);
                    builder.SetAreaWeight(AreaType.AtGenevaWheel, 4);
                    builder.SetAreaWeight(AreaType.AtQuakeIIArena, 0);
                    builder.SetAreaWeight(AreaType.AtTemple, 4);
                    builder.SetAreaWeight(AreaType.AtMonasticCells, 4);
                    builder.SetAreaWeight(AreaType.AtCrypt, 4);
                    builder.SetAreaWeight(AreaType.AtRoseRoom, 4);
                    builder.SetAreaWeight(AreaType.AtCrossroad, 4);
                    builder.SetAreaWeight(AreaType.AtStarRoom, 0);
                    builder.SetAreaWeight(AreaType.AtFaithRoom, 0);
                    builder.SetAreaWeight(AreaType.AtSpiderRoom, 0);
                    builder.SetAreaWeight(AreaType.AtAlt1Room, 2);
                    builder.SetAreaWeight(AreaType.AtAlt2Room, 3);
                    builder.SetAreaWeight(AreaType.AtAlt3Room, 2);
                    builder.SetAreaWeight(AreaType.AtAlt4Room, 3);
                    builder.SetAreaWeight(AreaType.AtAlt5Room, 1);

                    builder.Build();

                    IList<DungeonArea> areas = builder.AreasList;
                    foreach (DungeonArea area in areas) {
                        if (area is RectangularRoom) {
                            RectangularRoom room = (RectangularRoom)area;
                            DungeonRoom dunRoom = new DungeonRoom(GameSpace.Instance, map);
                            dunRoom.SetArea(room.Left, room.Top, room.Right, room.Bottom);
                            map.Features.Add(dunRoom);

                            foreach (DungeonMark mark in room.MarksList) {
                                switch (mark.State) {
                                    case DungeonMark.Ms_Undefined:
                                    case DungeonMark.Ms_RetriesExhaust:
                                        // dummy
                                        break;
                                    case DungeonMark.Ms_AreaGenerator:
                                    case DungeonMark.Ms_PointToOtherArea:
                                        dunRoom.AddDoor(mark.Location.X, mark.Location.Y, mark.Direction, Door.STATE_OPENED);
                                        break;
                                }
                            }
                        }
                    }

                    NormalizeDungeon(map, dungeonArea);
                } finally {
                    builder.Dispose();
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Dungeon(): " + ex.Message);
                throw ex;
            }
        }

        // Tile select levels
        public const int TSL_BACK = 0;
        public const int TSL_FORE = 1;
        public const int TSL_BACK_EXT = 2;
        public const int TSL_FORE_EXT = 3;
        public const int TSL_FOG = 4;

        // FIXME: NWR dup
        public static int GetAdjacentMagic(AbstractMap map, int x, int y, int def, int check, int select)
        {
            int magic = 0;
            for (int i = 0; i < DbAdjacentTiles.Length; i++) {
                ExtPoint pt = DbAdjacentTiles[i];

                int tid;
                NWTile tile = (NWTile)map.GetTile(x + pt.X, y + pt.Y);
                if (tile == null) {
                    tid = def;
                } else {
                    switch (select) {
                        case TSL_BACK:
                            tid = tile.BackBase;
                            break;
                        case TSL_FORE:
                            tid = tile.ForeBase;
                            break;
                        case TSL_BACK_EXT:
                            tid = AuxUtils.GetShortLo(tile.BackgroundExt);
                            break;
                        case TSL_FORE_EXT:
                            tid = AuxUtils.GetShortLo(tile.ForegroundExt);
                            break;
                        case TSL_FOG:
                            tid = AuxUtils.GetShortLo(tile.FogID);
                            break;
                        default:
                            tid = 0;
                            break;
                    }
                }

                if (tid == check) {
                    magic = BitHelper.SetBit(magic, i);
                }
            }

            return magic;
        }

        private static void NormalizeTile(NWField field, int x, int y, NWTile tile, int def, int pid, int mask, bool aFog, bool Ext, int select)
        {
            try {
                int magic = GetAdjacentMagic(field, x, y, def, pid, select);

                int offset = -1;
                if (magic != 0) {
                    for (int i = 0; i < DbMaskMagics.Length; i++) {
                        MagicRec mRec = DbMaskMagics[i];
                        if (mRec.Main.Contains(magic) || (Ext && mRec.Ext.Contains(magic))) {
                            offset = i;
                            break;
                        }
                    }
                }

                if (offset > -1) {
                    int res = BaseTile.GetVarID((byte)mask, (byte)offset);
                    if (aFog) {
                        tile.FogExtID = (ushort)res;
                    } else {
                        tile.BackgroundExt = (ushort)res;
                    }
                } else {
                    int res = def;
                    if (aFog) {
                        tile.FogExtID = (ushort)res;
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.normalizeTile(): " + ex.Message);
            }
        }

        public static void NormalizeFieldMasks(NWField field, bool fog)
        {
            try {
                bool caves = field.LandID == GlobalVars.Land_Armory || field.LandID == GlobalVars.Land_GrynrHalls || field.LandID == GlobalVars.Land_Caves || field.LandID == GlobalVars.Land_DeepCaves || field.LandID == GlobalVars.Land_GreatCaves;

                int select = (fog) ? TSL_FOG : TSL_BACK;

                for (int y = 0; y < StaticData.FieldHeight; y++) {
                    for (int x = 0; x < StaticData.FieldWidth; x++) {
                        NWTile tile = (NWTile)field.GetTile(x, y);

                        int fore = tile.ForeBase;
                        bool isDoor = (fore == PlaceID.pid_DoorN || fore == PlaceID.pid_DoorS || fore == PlaceID.pid_DoorW || fore == PlaceID.pid_DoorE);
                        if (caves && isDoor) {
                            tile.Foreground = PlaceID.pid_Undefined;
                        }

                        if (!fog) {
                            tile.BackgroundExt = (ushort)PlaceID.pid_Undefined;

                            int back = tile.BackBase;

                            if (back != PlaceID.pid_Grass) {
                                if (back == PlaceID.pid_Water || back == PlaceID.pid_Space || back == PlaceID.pid_Lava || back == PlaceID.pid_Liquid || back == PlaceID.pid_Mud || back == PlaceID.pid_Rubble) {
                                    NormalizeTile(field, x, y, tile, back, PlaceID.pid_Grass, PlaceID.pid_GrassMask, fog, true, select);
                                }
                            } else {
                                NormalizeTile(field, x, y, tile, back, PlaceID.pid_Space, PlaceID.pid_SpaceMask, fog, false, select);
                                NormalizeTile(field, x, y, tile, back, PlaceID.pid_Water, PlaceID.pid_WaterMask, fog, false, select);
                                NormalizeTile(field, x, y, tile, back, PlaceID.pid_Liquid, PlaceID.pid_LiquidMask, fog, false, select);
                                NormalizeTile(field, x, y, tile, back, PlaceID.pid_Lava, PlaceID.pid_LavaMask, fog, false, select);
                                NormalizeTile(field, x, y, tile, back, PlaceID.pid_Mud, PlaceID.pid_MudMask, fog, false, select);
                                NormalizeTile(field, x, y, tile, back, PlaceID.pid_Rubble, PlaceID.pid_RubbleMask, fog, false, select);
                            }
                        } else {
                            tile.FogExtID = (ushort)PlaceID.pid_Undefined;

                            int defp = (tile.FogID);

                            if (defp != PlaceID.pid_Undefined) {
                                if (defp == PlaceID.pid_Fog) {
                                    NormalizeTile(field, x, y, tile, defp, PlaceID.pid_Undefined, PlaceID.pid_FogMask2, fog, true, select);
                                }
                            } else {
                                NormalizeTile(field, x, y, tile, defp, PlaceID.pid_Fog, PlaceID.pid_FogMask1, fog, false, select);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.normalizeFieldMasks(): " + ex.Message);
            }
        }

        private static void NormalizeDungeon(AbstractMap map, ExtRect dungeonArea)
        {
            try {
                for (int y = dungeonArea.Top; y <= dungeonArea.Bottom; y++) {
                    for (int x = dungeonArea.Left; x <= dungeonArea.Right; x++) {
                        BaseTile tile = map.GetTile(x, y);

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
                        BaseTile tile = map.GetTile(x, y);

                        int fg = tile.Foreground;
                        if (fg == PlaceID.pid_DungeonWall) {
                            int magic = GetAdjacentMagic(map, x, y, PlaceID.pid_Undefined, PlaceID.pid_Undefined, TSL_FORE);
                            if (magic != 0) {
                                for (int offset = 0; offset < DbDgnMagics.Length; offset++) {
                                    if (DbDgnMagics[offset].Main.Contains(magic)) {
                                        tile.SetFore(PlaceID.pid_DungeonWall, offset);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.normalizeDungeon(): " + ex.Message);
            }
        }

        public static void NormalizeRoad(AbstractMap map, int roadTile)
        {
            try {
                for (int y = 0; y < map.Height; y++) {
                    for (int x = 0; x < map.Width; x++) {
                        NWTile tile = (NWTile)map.GetTile(x, y);

                        int fg = tile.ForeBase;
                        if (fg == roadTile) {
                            int magic = GetAdjacentMagic(map, x, y, PlaceID.pid_Undefined, roadTile, TSL_FORE);
                            if (magic != 0) {
                                for (int offset = 0; offset < DbRoadMagics.Length; offset++) {
                                    if (DbRoadMagics[offset].Main.Contains(magic)) {
                                        tile.SetFore(roadTile, offset);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.normalizeRoad(): " + ex.Message);
            }
        }

        public static void Gen_Road(AbstractMap map, int px1, int py1, int px2, int py2, ExtRect area, int tid)
        {
            RoadBuilder rb = new RoadBuilder(map);
            rb.Generate(px1, py1, px2, py2, 15, tid, SetRoadTile);

            //map.gen_Path(px1, py1, px2, py2, map.getAreaRect(), tid, false, false, UniverseBuilder.setRoadTile);
            NormalizeRoad(map, tid);
        }

        private static void SetRoadTile(IMap map, int x, int y, object extData, ref bool  refContinue)
        {
            NWTile tile = (NWTile)map.GetTile(x, y);
            int tid = (int)extData;

            if (tile != null) {
                int bg = tile.BackBase;
                int fg = tile.ForeBase;

                if (fg == tid) {
                    refContinue = false;
                    return;
                }

                if (bg != PlaceID.pid_Floor) {
                    if (fg == 0) {
                        tile.Foreground = (ushort)tid;
                    } else {
                        int r = RandomHelper.GetRandom(10);
                        if ((fg == PlaceID.pid_Tree && r == 0) || (fg == PlaceID.pid_Bush || fg == PlaceID.pid_Stump)) {
                            tile.Foreground = (ushort)tid;
                        }
                    }
                }
            }
        }

        private static void GenAlfheimLiquidLake(NWField field, ExtRect area, int gran, ushort aTile, int Liquid)
        {
            SetAlfheimTile(field, area.Left + (area.Right - area.Left) / 2, area.Top + (area.Bottom - area.Top) / 2, aTile, Liquid);
            int g = gran / 2;
            if (g >= 1) {
                for (int y = area.Top; y <= area.Bottom; y += gran) {
                    for (int x = area.Left; x <= area.Right; x += gran) {
                        if (RandomHelper.GetRandom(2) == 0) {
                            SetAlfheimTile(field, x + g, y, GetAlfheimTileFull(field, x, y, aTile), Liquid);
                        } else {
                            SetAlfheimTile(field, x + g, y, GetAlfheimTileFull(field, x + gran, y, aTile), Liquid);
                        }
                        if (RandomHelper.GetRandom(2) == 0) {
                            SetAlfheimTile(field, x, y + g, GetAlfheimTileFull(field, x, y, aTile), Liquid);
                        } else {
                            SetAlfheimTile(field, x, y + g, GetAlfheimTileFull(field, x, y + gran, aTile), Liquid);
                        }
                    }
                }

                for (int y = area.Top; y <= area.Bottom; y += gran) {
                    for (int x = area.Left; x <= area.Right; x += gran) {
                        int num = RandomHelper.GetRandom(4) + 1;
                        ushort c;
                        if (num != 1) {
                            if (num != 2) {
                                if (num != 3) {
                                    c = GetAlfheimTileFull(field, x + gran, y + g, aTile);
                                } else {
                                    c = GetAlfheimTileFull(field, x, y + g, aTile);
                                }
                            } else {
                                c = GetAlfheimTileFull(field, x + g, y + gran, aTile);
                            }
                        } else {
                            c = GetAlfheimTileFull(field, x + g, y, aTile);
                        }
                        SetAlfheimTile(field, x + g, y + g, c, Liquid);
                    }
                }
                if (g > 1) {
                    GenAlfheimLiquidLake(field, area, g, aTile, Liquid);
                }
            }
        }

        private static ushort GetAlfheimTileFull(NWField field, int x, int y, ushort placeID)
        {
            ushort result = placeID;
            NWTile tile = (NWTile)field.GetTile(x, y);
            if (tile != null) {
                result = tile.BackBase;
            }
            return result;
        }

        private static void SetAlfheimTile(NWField field, int x, int y, ushort tileId, int liquid)
        {
            NWTile tile = (NWTile)field.GetTile(x, y);
            if (tile != null) {
                tile.Background = tileId;
                tile.Lake_LiquidID = liquid;
            }
        }

        public static void Build_Alfheim(NWField field)
        {
            try {
                field.FillBackground(PlaceID.pid_Grass);
                field.FillForeground(PlaceID.pid_Undefined);
                Gen_Valley(field, false, false, false);

                int wid = GlobalVars.nwrDB.FindEntryBySign("Potion_DrinkingWater").GUID;
                int cnt = RandomHelper.GetBoundedRnd(20, 33);
                for (int i = 1; i <= cnt; i++) {
                    int x = RandomHelper.GetBoundedRnd(1, field.Width - 2);
                    int y = RandomHelper.GetBoundedRnd(1, field.Height - 2);
                    int rad = 4;
                    ExtRect area = ExtRect.Create(x - rad, y - rad, x + rad, y + rad);
                    int lid = GlobalVars.dbPotions[RandomHelper.GetBoundedRnd(0, GlobalVars.dbPotions.Count - 1)];
                    if (lid != wid) {
                        GenAlfheimLiquidLake(field, area, 4, PlaceID.pid_Liquid, lid);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Alfheim(): " + ex.Message);
                throw ex;
            }
        }

        public static void Build_Vanaheim(NWField field)
        {
            try {
                field.FillBackground(PlaceID.pid_Grass);
                field.FillForeground(PlaceID.pid_Undefined);
                Gen_Valley(field, false, true, false);

                ExtRect area = field.AreaRect;

                Building.RuinsMode = true;

                int cnt = RandomHelper.GetBoundedRnd(3, 7);
                for (int i = 1; i <= cnt; i++) {
                    Building bld = new Building(GameSpace.Instance, field);

                    if (bld.Build(3, 4, 7, area)) {
                        bld.ID = BuildingID.bid_None;
                        field.Features.Add(bld);

                        ExtRect rt = bld.Area;
                        int z = (int)Math.Round(rt.Square * 0.25f);
                        for (int k = 1; k <= z; k++) {
                            int x = RandomHelper.GetBoundedRnd(rt.Left, rt.Right);
                            int y = RandomHelper.GetBoundedRnd(rt.Top, rt.Bottom);

                            BaseTile tile = field.GetTile(x, y);
                            //tile.backGround = PlaceID.pid_Grass;
                            tile.Foreground = PlaceID.pid_Undefined;
                        }
                    } else {
                        bld.Dispose();
                    }
                }

                Building.RuinsMode = false;
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Vanaheim(): " + ex.Message);
            }
        }

        public static void Build_Bazaar(NWField field)
        {
            try {
                field.FillBackground(PlaceID.pid_Floor);
                field.FillForeground(PlaceID.pid_Undefined);

                ExtRect r = field.AreaRect;

                int num = field.Height;
                for (int y = 0; y < num; y++) {
                    int num2 = field.Width;
                    for (int x = 0; x < num2; x++) {
                        NWTile tile = (NWTile)field.GetTile(x, y);
                        tile.Foreground = NWField.GetBuildPlaceKind(x, y, r, false);
                    }
                }

                for (int yy = 0; yy <= 1; yy++) {
                    for (int xx = 0; xx <= 3; xx++) {
                        int x2 = 1 + xx * 19;
                        int y2 = 1 + yy * 9;

                        r = ExtRect.Create(x2, y2, x2 + 16, y2 + 6);
                        Building building = new Building(GameSpace.Instance, field);
                        building.ID = BuildingID.bid_House;
                        building.SetArea(r.Left, r.Top, r.Right, r.Bottom);
                        building.Prepare();
                        building.Flush();
                        field.Features.Add(building);

                        int cx = RandomHelper.GetBoundedRnd(r.Left + 1, r.Right - 1);
                        int cy = RandomHelper.GetBoundedRnd(r.Top + 1, r.Bottom - 1);
                        NWCreature merchant = field.AddCreature(cx, cy, GlobalVars.cid_Merchant);
                        merchant.IsTrader = true;
                        merchant.AddMoney(RandomHelper.GetBoundedRnd(15000, 25000));
                        building.Holder = merchant;

                        for (int y = r.Top + 1; y <= r.Bottom - 1; y++) {
                            for (int x = r.Left + 1; x <= r.Right - 1; x++) {
                                DataEntry entry = null;

                                switch (AreaItem[yy, xx]) {
                                    case Bik_Ring:
                                        if (AuxUtils.Chance(10)) {
                                            entry = GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Amulet);
                                        } else {
                                            entry = GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Ring);
                                        }
                                        break;
                                    case Bik_Potion:
                                        entry = GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Potion);
                                        break;
                                    case Bik_Wand:
                                        entry = GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Wand);
                                        break;
                                    case Bik_Food:
                                        entry = GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Food);
                                        break;
                                    case Bik_Scroll:
                                        entry = GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Scroll);
                                        break;
                                    case Bik_Armor:
                                        entry = GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Armor);
                                        break;
                                    case Bik_Tool:
                                        entry = GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Tool);
                                        break;
                                    case Bik_Weapon:
                                        entry = GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Weapon);
                                        break;
                                }

                                if (entry != null) {
                                    int id = ((ItemEntry)entry).Rnd;
                                    field.AddItem(x, y, id).Owner = merchant;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Bazaar(): " + ex.Message);
                throw ex;
            }
        }

        public static bool Gen_Creature(NWField field, int aID, int aX, int aY, bool aFlock)
        {
            bool result = false;

            try {
                List<int> validCreatures = field.ValidCreatures;
                int cnt = (validCreatures != null) ? validCreatures.Count : 0;
                if (cnt < 1) {
                    return result;
                } else {
                    int tries = 10;
                    do {
                        tries--;
                        int cid;
                        if (aID == -1) {
                            cid = field.ValidCreatures[RandomHelper.GetRandom(cnt)];
                        } else {
                            cid = aID;
                        }

                        if (GlobalVars.nwrGame.CanCreate(cid)) {
                            NWCreature cr = field.AddCreature(aX, aY, cid);
                            result = true;
                            if (aFlock && (cr.Entry.Flags.Contains(CreatureFlags.esCohorts))) {
                                int num = RandomHelper.GetBoundedRnd(1, 3);
                                for (int i = 1; i <= num; i++) {
                                    ExtPoint pt = cr.GetNearestPlace((int)cr.Survey, true);
                                    if (!pt.IsEmpty) {
                                        field.AddCreature(pt.X, pt.Y, cid);
                                    }
                                }
                            }
                        }
                    } while (!result && tries != 0);
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.gen_Creature(): " + ex.Message);
            }

            return result;
        }

        public static void Gen_Creatures(NWField field, int aCount)
        {
            try {
                if (aCount == -1) {
                    aCount = RandomHelper.GetBoundedRnd(10, 20);
                }

                for (int i = 1; i <= aCount; i++) {
                    Gen_Creature(field, -1, -1, -1, true);
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.gen_Creatures(): " + ex.Message);
            }
        }

        private static int GetRndDir(NWField field, int pX, int pY, int pOct)
        {
            int result = Directions.DtNone;

            OctantRec octRec = Octants[pOct - 1];
            int iX = (int)octRec.IX;
            int iY = (int)octRec.IY;
            int xLB = (int)octRec.XLB;
            int xHB = (int)octRec.XHB;
            int yLB = (int)octRec.YLB;
            int yHB = (int)octRec.YHB;

            // define the list of valid directions
            Directions vd = new Directions();
            int vdCnt = 0;

            for (int cd = Directions.DtFlatFirst; cd <= Directions.DtFlatLast; cd++) {
                if (octRec.ValidDirs.Contains(cd)) {
                    int newX = pX + Directions.Data[cd].DX;
                    int newY = pY + Directions.Data[cd].DY;
                    int dX = Math.Abs(newX - iX);
                    int dY = Math.Abs(newY - iY);

                    switch (pOct) {
                        case 1:
                        case 4:
                            {
                                yHB = yLB + dX - 1;
                                break;
                            }
                        case 2:
                        case 7:
                            {
                                xHB = xLB + dY - 1;
                                break;
                            }
                        case 3:
                        case 6:
                            {
                                xLB = xHB - dY + 1;
                                break;
                            }
                        case 5:
                        case 8:
                            {
                                yLB = yHB - dX + 1;
                                break;
                            }
                    }

                    if (newX >= xLB && newX <= xHB && newY >= yLB && newY <= yHB && field.IsValid(newX, newY)) {
                        vd.Include(cd);
                        vdCnt++;
                    }
                }
            }

            if (vdCnt > 0) {
                int i = 0;
                int j = RandomHelper.GetRandom(vdCnt) + 1;

                for (int cd = Directions.DtFlatFirst; cd <= Directions.DtFlatLast; cd++) {
                    if (vd.Contains(cd)) {
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

        public static void Build_Crossroads(NWField field)
        {
            try {
                field.GetTile(37, 8).Background = PlaceID.pid_cr_yr;
                field.GetTile(38, 8).Background = PlaceID.pid_cr_ba;
                field.GetTile(37, 9).Background = PlaceID.pid_cr_gk;
                field.GetTile(38, 9).Background = PlaceID.pid_cr_wl;

                for (int y = 0; y < StaticData.FieldHeight; y++) {
                    for (int x = 0; x < StaticData.FieldWidth; x++) {
                        NWTile tile = (NWTile)field.GetTile(x, y);

                        if (x > 0 && x < StaticData.FieldWidth - 1) {
                            if (field.GetTile(x - 1, y).BackBase == PlaceID.pid_cr_y && field.GetTile(x + 1, y).BackBase == PlaceID.pid_cr_r) {
                                tile.Background = PlaceID.pid_cr_yr;
                            }
                            if (field.GetTile(x - 1, y).BackBase == PlaceID.pid_cr_b && field.GetTile(x + 1, y).BackBase == PlaceID.pid_cr_a) {
                                tile.Background = PlaceID.pid_cr_ba;
                            }
                            if (field.GetTile(x - 1, y).BackBase == PlaceID.pid_cr_g && field.GetTile(x + 1, y).BackBase == PlaceID.pid_cr_k) {
                                tile.Background = PlaceID.pid_cr_gk;
                            }
                            if (field.GetTile(x - 1, y).BackBase == PlaceID.pid_cr_w && field.GetTile(x + 1, y).BackBase == PlaceID.pid_cr_l) {
                                tile.Background = PlaceID.pid_cr_wl;
                            }
                        }
                    }
                }

                for (int oct = 1; oct <= 8; oct++) {
                    int pX = (int)Octants[oct - 1].IX;
                    int pY = (int)Octants[oct - 1].IY;
                    field.GetTile(pX, pY).Foreground = PlaceID.pid_cr_Disk;

                    int num = (int)Octants[oct - 1].PathLen;
                    for (int i = 1; i <= num; i++) {
                        int newDir = GetRndDir(field, pX, pY, oct);
                        if (newDir == Directions.DtNone) {
                            break;
                        }

                        pX += Directions.Data[newDir].DX;
                        pY += Directions.Data[newDir].DY;
                        field.GetTile(pX, pY).Foreground = PlaceID.pid_cr_Disk;
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Crossroads(): " + ex.Message);
                throw ex;
            }
        }

        public static void Gen_CaveObjects(NWField field)
        {
            try {
                int num = RandomHelper.GetBoundedRnd(150, 250);
                for (int i = 1; i <= num; i++) {
                    int x = RandomHelper.GetBoundedRnd(1, StaticData.FieldWidth - 2);
                    int y = RandomHelper.GetBoundedRnd(1, StaticData.FieldHeight - 2);

                    BaseTile tile = field.GetTile(x, y);
                    if (!field.IsBarrier(x, y) && (int)tile.Background == PlaceID.pid_Floor) {
                        tile.Foreground = (ushort)PlaceID.pid_Stalagmite;
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.gen_CaveObjects(): " + ex.Message);
                throw ex;
            }
        }

        private static void Gen_ForestObjects(NWField field, ExtRect area, int count, int tileID)
        {
            for (int i = 1; i <= count; i++) {
                int x = RandomHelper.GetBoundedRnd(area.Left, area.Right);
                int y = RandomHelper.GetBoundedRnd(area.Top, area.Bottom);
                NWTile tile = (NWTile)field.GetTile(x, y);
                if (tile.Foreground == PlaceID.pid_Undefined && tile.Background == PlaceID.pid_Grass) {
                    tile.Foreground = NWField.GetVarTile(tileID);
                }
            }
        }

        public static void Build_Forest(NWField field)
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

                Gen_ForestObjects(field, field.AreaRect, cnt, tid);

                if (field.LandID == GlobalVars.Land_Forest) {
                    Gen_ForestObjects(field, field.AreaRect, 50, PlaceID.pid_Bush);
                    Gen_ForestObjects(field, field.AreaRect, 25, PlaceID.pid_Stump);
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Forest(): " + ex.Message);
                throw ex;
            }
        }

        public static void Build_GrynrHalls(NWField field)
        {
            try {
                for (int i = 1; i <= 5; i++) {
                    ExtPoint pt = field.SearchFreeLocation();
                    field.GetTile(pt.X, pt.Y).Foreground = (ushort)PlaceID.pid_SmallPit;
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_GrynrHalls(): " + ex.Message);
                throw ex;
            }
        }

        public static void Gen_Items(NWField field, int itemsCount)
        {
            try {
                bool caves = (field.LandID == GlobalVars.Land_Caves || field.LandID == GlobalVars.Land_DeepCaves || field.LandID == GlobalVars.Land_GreatCaves || field.LandID == GlobalVars.Land_GrynrHalls);
                bool armory = (field.LandID == GlobalVars.Land_Armory);

                if (itemsCount == -1) {
                    if (caves) {
                        itemsCount = RandomHelper.GetBoundedRnd(5, 15);
                    } else {
                        if (armory) {
                            itemsCount = RandomHelper.GetBoundedRnd(10, 15);
                        } else {
                            itemsCount = RandomHelper.GetBoundedRnd(5, 30);
                        }
                    }
                }

                for (int i = 1; i <= itemsCount; i++) {
                    while (true) {
                        int x = RandomHelper.GetBoundedRnd(0, field.Width - 1);
                        int y = RandomHelper.GetBoundedRnd(0, field.Height - 1);

                        NWTile tile = (NWTile)field.GetTile(x, y);
                        int bg = tile.BackBase;

                        if (!field.IsBarrier(x, y) && (StaticData.dbPlaces[bg].Signs.Contains(PlaceFlags.psIsGround))) {
                            ItemEntry eItem = null;

                            if (armory) {
                                int num = RandomHelper.GetBoundedRnd(1, 2);
                                if (num == 1) {
                                    eItem = (ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Armor);
                                } else {
                                    eItem = (ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Weapon);
                                }
                            } else {
                                if (caves) {
                                    switch (RandomHelper.GetBoundedRnd(1, 5)) {
                                        case 1:
                                            eItem = ((ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Armor));
                                            break;
                                        case 2:
                                            eItem = ((ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Weapon));
                                            break;
                                        case 3:
                                            eItem = ((ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Scroll));
                                            break;
                                        case 4:
                                            eItem = ((ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Potion));
                                            break;
                                        case 5:
                                            eItem = ((ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_Wand));
                                            break;
                                    }
                                } else {
                                    eItem = ((ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Rnd_NatureObject));
                                }
                            }

                            if (eItem != null) {
                                int iid = eItem.Rnd;
                                Item item = new Item(GameSpace.Instance, field);
                                item.CLSID = iid;
                                item.SetPos(x, y);
                                field.Items.Add(item, false);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.gen_Items(): " + ex.Message);
                throw ex;
            }
        }

        public static void GenBackTiles(NWField field, int count, int tileID)
        {
            for (int i = 1; i <= count; i++) {
                int x = RandomHelper.GetBoundedRnd(0, StaticData.FieldWidth - 1);
                int y = RandomHelper.GetBoundedRnd(0, StaticData.FieldHeight - 1);

                NWTile tile = (NWTile)field.GetTile(x, y);
                if (tile.Background == PlaceID.pid_Grass) {
                    tile.Background = NWField.GetVarTile(tileID);
                }
            }
        }

        public static void Build_MimerRealm(NWField field)
        {
            GenBackTiles(field, RandomHelper.GetBoundedRnd(20, 30), PlaceID.pid_Water);
            GenBackTiles(field, RandomHelper.GetBoundedRnd(20, 30), PlaceID.pid_Lava);
            GenBackTiles(field, RandomHelper.GetBoundedRnd(20, 30), PlaceID.pid_Quicksand);
            GenBackTiles(field, RandomHelper.GetBoundedRnd(20, 30), PlaceID.pid_Mud);
        }

        private static void ChangeMWTile(IMap map, int x, int y, object extData, ref bool  refContinue)
        {
            BaseTile tile = map.GetTile(x, y);
            if (tile != null) {
                tile.Background = (ushort)PlaceID.pid_Water;
            }
        }

        public static void Build_MimerWell(NWField field)
        {
            try {
                field.FillBackground(PlaceID.pid_Rock);
                field.FillForeground(PlaceID.pid_Undefined);

                int x = StaticData.FieldWidth / 2;
                int y = StaticData.FieldHeight / 2;
                ExtRect area = ExtRect.Create(x - 8, y - 8, x + 8, y + 8);
                field.Gen_Lake(area, ChangeMWTile);

                field.Normalize();
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_MimerWell(): " + ex.Message);
                throw ex;
            }
        }

        public static void Build_Jotenheim_Jarnvidr(NWField field)
        {
            ExtRect area = ExtRect.Create(2, 3, 50, 15);
            Gen_Forest(field, area, PlaceID.pid_IronTree, false);

            Region region = new Region(field.Space, field);
            region.Area = area;
            region.CLSID_Renamed = field.Space.FindDataEntry("iJarnvidr").GUID;
            field.Features.Add(region);
        }

        public static void Gen_RarelyMountains(NWField field)
        {
            try {
                int cnt;
                if (field.LandID == GlobalVars.Land_Niflheim || field.LandID == GlobalVars.Land_Jotenheim) {
                    cnt = 200;
                } else {
                    cnt = 50;
                }

                for (int i = 1; i <= cnt; i++) {
                    int x = RandomHelper.GetBoundedRnd(0, StaticData.FieldWidth - 1);
                    int y = RandomHelper.GetBoundedRnd(0, StaticData.FieldHeight - 1);
                    NWTile tile = (NWTile)field.GetTile(x, y);
                    int bg = tile.BackBase;
                    int fg = tile.ForeBase;

                    if (fg == PlaceID.pid_Undefined && bg != PlaceID.pid_Space && bg != PlaceID.pid_Water) {
                        tile.Foreground = PlaceID.pid_Mountain;
                    }
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.gen_Mountains(): " + ex.Message);
                throw ex;
            }
        }

        public static void Build_Muspelheim(NWField field)
        {
            try {
                RandomHelper.Randomize();

                int num = field.Height;
                for (int y = 0; y < num; y++) {
                    int num2 = field.Width;
                    for (int x = 0; x < num2; x++) {
                        NWTile tile = (NWTile)field.GetTile(x, y);
                        tile.Background = NWField.Muspel_bgTiles[RandomHelper.GetBoundedRnd(0, 6)];
                        tile.Foreground = NWField.Muspel_fgTiles[RandomHelper.GetBoundedRnd(0, 5)];
                    }
                }

                field.Normalize();
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Muspelheim(): " + ex.Message);
                throw ex;
            }
        }

        public static void Regen_Muspelheim(NWField field)
        {
            try {
                RandomHelper.Randomize();

                ushort bg = RandomHelper.GetRandomItem(NWField.Muspel_bgTiles);
                //int fg = AuxUtils.getRandomArrayInt(NWField.Muspel_fgTiles);

                field.SpreadTiles(bg, 0.25f);
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.regen_Muspelheim(): " + ex.Message);
            }
        }

        public static void Build_Ocean(NWField field)
        {
            if (GlobalVars.nwrGame.rt_Jormungand == null) {
                GlobalVars.nwrGame.rt_Jormungand = field.AddCreature(-1, -1, GlobalVars.cid_Jormungand);
            }
        }

        private static void FillAreaItems(NWField field, int x1, int y1, int x2, int y2, int meta_id)
        {
            for (int yy = y1; yy <= y2; yy++) {
                for (int xx = x1; xx <= x2; xx++) {
                    ItemEntry aEntry = (ItemEntry)GlobalVars.nwrDB.GetEntry(meta_id);
                    for (int i = 1; i <= 10; i++) {
                        int id = aEntry.Rnd;
                        Item res = new Item(GameSpace.Instance, field);
                        res.CLSID = id;
                        res.Count = 1;
                        res.Identified = true;
                        res.SetPos(xx, yy);
                        field.Items.Add(res, false);
                    }
                }
            }
        }

        public static void Gen_Traps(NWField field, int factor)
        {
            try {
                int f = factor;

                if (field.LandID == GlobalVars.Land_Village) {
                    if (factor < 0) {
                        f = -2;
                    }
                    field.AddSeveralTraps(new ushort[]{ PlaceID.pid_PitTrap, PlaceID.pid_TeleportTrap }, f);
                }

                if (field.LandID == GlobalVars.Land_Forest) {
                    if (factor < 0) {
                        f = -3;
                    }
                    field.AddSeveralTraps(new ushort[] {
                        PlaceID.pid_PoisonSpikeTrap,
                        PlaceID.pid_WaterTrap,
                        PlaceID.pid_PitTrap
                    }, f);
                }

                if (field.LandID == GlobalVars.Land_Alfheim) {
                    if (factor < 0) {
                        f = -4;
                    }
                    field.AddSeveralTraps(new ushort[] {
                        PlaceID.pid_StunGasTrap,
                        PlaceID.pid_PoisonSpikeTrap,
                        PlaceID.pid_FrostTrap,
                        PlaceID.pid_PitTrap,
                        PlaceID.pid_LavaTrap
                    }, f);
                }

                if (field.LandID == GlobalVars.Land_Wasteland) {
                    if (factor < 0) {
                        f = -3;
                    }
                    field.AddSeveralTraps(new ushort[]{ PlaceID.pid_FireTrap, PlaceID.pid_LavaTrap }, f);
                }

                if (field.LandID == GlobalVars.Land_Niflheim) {
                    if (factor < 0) {
                        f = -3;
                    }
                    field.AddSeveralTraps(new ushort[] {
                        PlaceID.pid_FrostTrap,
                        PlaceID.pid_FireTrap,
                        PlaceID.pid_PitTrap,
                        PlaceID.pid_TeleportTrap,
                        PlaceID.pid_StunGasTrap,
                        PlaceID.pid_LavaTrap
                    }, f);
                }

                if (field.LandID == GlobalVars.Land_Nidavellir) {
                    if (factor < 0) {
                        f = -3;
                    }
                    field.AddSeveralTraps(new ushort[]{ PlaceID.pid_FireTrap }, f);
                }

                if (field.LandID == GlobalVars.Land_Vanaheim) {
                    if (factor < 0) {
                        f = -7;
                    }
                    field.AddSeveralTraps(new ushort[] {
                        PlaceID.pid_PoisonSpikeTrap,
                        PlaceID.pid_QuicksandTrap,
                        PlaceID.pid_WaterTrap,
                        PlaceID.pid_StunGasTrap,
                        PlaceID.pid_PitTrap,
                        PlaceID.pid_MistTrap,
                        PlaceID.pid_ArrowTrap,
                        PlaceID.pid_TeleportTrap
                    }, f);
                }
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.gen_Traps(): " + ex.Message);
                throw ex;
            }
        }

        private static void DrawWall(NWField field, int x, ushort pid)
        {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                NWTile tile = (NWTile)field.GetTile(x, y);
                tile.Background = PlaceID.pid_Floor;
                tile.Foreground = pid;
            }
        }

        public static void Build_GodsFortress(NWField field)
        {
            try {
                Building.DrawWalls(field, ExtRect.Create(33, 0, 64, 6));
                field.GetTile(36, 6).Foreground = PlaceID.pid_DoorS;
                field.GetTile(37, 6).Foreground = PlaceID.pid_DoorS;
                field.GetTile(47, 6).Foreground = PlaceID.pid_DoorS;
                field.GetTile(48, 6).Foreground = PlaceID.pid_DoorS;
                field.GetTile(60, 6).Foreground = PlaceID.pid_DoorS;
                field.GetTile(61, 6).Foreground = PlaceID.pid_DoorS;

                Building.DrawWalls(field, ExtRect.Create(32, 10, 41, 17));
                field.GetTile(36, 10).Foreground = PlaceID.pid_DoorN;
                field.GetTile(37, 10).Foreground = PlaceID.pid_DoorN;

                Building.DrawWalls(field, ExtRect.Create(43, 10, 52, 17));
                field.GetTile(47, 10).Foreground = PlaceID.pid_DoorN;
                field.GetTile(48, 10).Foreground = PlaceID.pid_DoorN;

                Building.DrawWalls(field, ExtRect.Create(54, 10, 67, 17));
                field.GetTile(60, 10).Foreground = PlaceID.pid_DoorN;
                field.GetTile(61, 10).Foreground = PlaceID.pid_DoorN;
                //
                //
                FillAreaItems(field, 33, 14, 40, 16, GlobalVars.iid_Rnd_Scroll);
                FillAreaItems(field, 44, 14, 51, 16, GlobalVars.iid_Rnd_Potion);
                FillAreaItems(field, 55, 14, 66, 14, GlobalVars.iid_Rnd_Armor);
                FillAreaItems(field, 55, 15, 66, 15, GlobalVars.iid_Rnd_Weapon);
                FillAreaItems(field, 55, 16, 66, 16, GlobalVars.iid_Rnd_Wand);
                //
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_GodsFortress(): " + ex.Message);
                throw ex;
            }
        }

        public static void Build_Valhalla(NWField field)
        {
            try {
                DrawWall(field, 1, PlaceID.pid_WallW);
                field.GetTile(1, 8).Foreground = PlaceID.pid_DoorW;
                field.GetTile(1, 9).Foreground = PlaceID.pid_DoorW;

                DrawWall(field, 72, PlaceID.pid_WallE);
                field.GetTile(72, 8).Foreground = PlaceID.pid_DoorE;
                field.GetTile(72, 9).Foreground = PlaceID.pid_DoorE;
                //
                Building.DrawWalls(field, ExtRect.Create(33, 0, 64, 6));
                field.GetTile(33, 3).Foreground = PlaceID.pid_DoorW;

                Building.DrawWalls(field, ExtRect.Create(21, 10, 30, 17));
                field.GetTile(25, 10).Foreground = PlaceID.pid_DoorN;
                field.GetTile(26, 10).Foreground = PlaceID.pid_DoorN;

                Building.DrawWalls(field, ExtRect.Create(32, 10, 41, 17));
                field.GetTile(36, 10).Foreground = PlaceID.pid_DoorN;
                field.GetTile(37, 10).Foreground = PlaceID.pid_DoorN;

                Building.DrawWalls(field, ExtRect.Create(43, 10, 52, 17));
                field.GetTile(47, 10).Foreground = PlaceID.pid_DoorN;
                field.GetTile(48, 10).Foreground = PlaceID.pid_DoorN;

                Building.DrawWalls(field, ExtRect.Create(54, 10, 67, 17));
                field.GetTile(60, 10).Foreground = PlaceID.pid_DoorN;
                field.GetTile(61, 10).Foreground = PlaceID.pid_DoorN;
                //
                field.AddCreature(-1, -1, GlobalVars.cid_Thor);
                field.AddCreature(-1, -1, GlobalVars.cid_Tyr);
                field.AddCreature(-1, -1, GlobalVars.cid_Freyr);
                field.AddCreature(-1, -1, GlobalVars.cid_Odin);
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Valhalla(): " + ex.Message);
                throw ex;
            }
        }

        public static void Build_VidurTemple(NWField field)
        {
            try {
                field.AddCreature(-1, -1, GlobalVars.cid_Agnar);
                field.AddCreature(-1, -1, GlobalVars.cid_Haddingr);
                field.AddCreature(-1, -1, GlobalVars.cid_Ketill);
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_VidurTemple(): " + ex.Message);
                throw ex;
            }
        }

        public static void Build_Village(NWField field)
        {
            try {
                Village village = new Village(GameSpace.Instance, field);
                field.Features.Add(village);

                ExtRect v = village.Area.Clone();
                v.Inflate(1, 1);
                village.BuildVillage(field, v);

                int num = field.Features.Count;
                for (int i = 0; i < num; i++) {
                    GameEntity entry = field.Features.GetItem(i);
                    if (entry is Building) {
                        ((Building)entry).Prepare();
                    }
                }

                GlobalVars.nwrGame.AddTownsman(field, v, GlobalVars.cid_Jarl);

                int cnt = RandomHelper.GetBoundedRnd(5, 9);
                for (int i = 1; i <= cnt; i++) {
                    GlobalVars.nwrGame.AddTownsman(field, v, GlobalVars.cid_Guardsman);
                }

                field.Research(true, BaseTile.TS_VISITED);
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Village(): " + ex.Message);
                throw ex;
            }
        }

        private static void ChangeWLTile(IMap map, int x, int y, object extData, ref bool  refContinue)
        {
            BaseTile tile = map.GetTile(x, y);
            if (tile != null) {
                tile.Background = (ushort)PlaceID.pid_Lava;
            }
        }

        public static void Build_Wasteland(NWField field)
        {
            try {
                int x = RandomHelper.GetBoundedRnd(1, field.Width - 2);
                int y = RandomHelper.GetBoundedRnd(1, field.Height - 2);
                int rad = RandomHelper.GetBoundedRnd(2, 5);

                ExtRect area = ExtRect.Create(x - rad, y - rad, x + rad, y + rad);
                field.Gen_Lake(area, ChangeWLTile);

                field.GetTile(x, y).Foreground = (ushort)PlaceID.pid_Vulcan;

                field.Normalize();
            } catch (Exception ex) {
                Logger.Write("UniverseBuilder.build_Wasteland(): " + ex.Message);
                throw ex;
            }
        }

        private static void ChangeFogTile(IMap map, int x, int y, object extData, ref bool  refContinue)
        {
            BaseTile tile = map.GetTile(x, y);
            if (tile != null) {
                NWTile nwTile = (NWTile)tile;
                nwTile.FogID = (ushort)PlaceID.pid_Fog;
                nwTile.FogAge = -1;
            }
        }

        public static void Gen_GiollFog(NWField field, int aX, int aY)
        {
            ExtRect area = ExtRect.Create(aX - 10, aY - 5, aX + 10, aY + 5);
            field.Gen_RarefySpace(area, ChangeFogTile, 8, 400);

            NWTile tile = (NWTile)field.GetTile(aX, aY);
            tile.FogID = (ushort)PlaceID.pid_Undefined;
            tile.FogAge = 0;

            field.NormalizeFog();
        }

        public static bool IsWaters(int placeID)
        {
            for (int i = 0; i < DbWaters.Length; i++) {
                if (DbWaters[i] == placeID) {
                    return true;
                }
            }
            return false;
        }

        public static void Gen_BigRiver(AbstractMap map)
        {
            int y = 0;
            int x = 0;
            int cur_hgt = map.Height;
            int cur_wid = map.Width;

            int y2 = RandomHelper.GetRandom(cur_hgt / 2 - 2) + cur_hgt / 2;
            int x2 = RandomHelper.GetRandom(cur_wid / 2 - 2) + cur_wid / 2;

            int num = RandomHelper.GetRandom(4);
            switch (num) {
                case 0:
                    x = RandomHelper.GetRandom(cur_wid - 2) + 1;
                    y = 1;
                    break;
                case 1:
                    x = 1;
                    y = RandomHelper.GetRandom(cur_hgt - 2) + 1;
                    break;
                case 2:
                    x = cur_wid - 1;
                    y = RandomHelper.GetRandom(cur_hgt - 2) + 1;
                    break;
                case 3:
                    x = RandomHelper.GetRandom(cur_wid - 2) + 1;
                    y = cur_hgt - 1;
                    break;
            }

            map.Gen_Path(x, y, x2, y2, map.AreaRect, PlaceID.pid_Water, true, true, null);
        }

        private static bool CheckNW(AbstractMap map, int x, int y, int dmin, int dmax)
        {
            int num = Math.Min(x + dmax, map.Width - 1);
            for (int xx = Math.Max(x - dmax, 0); xx <= num; xx++) {
                int num2 = Math.Min(y + dmax, map.Height - 1);
                for (int yy = Math.Max(y - dmax, 0); yy <= num2; yy++) {
                    if (MathHelper.Distance(x, y, xx, yy) >= dmin && 
                        MathHelper.Distance(x, y, xx, yy) <= dmax && 
                        !MapUtils.IsWaters(map, map.GetTile(xx, yy).Background)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public static void NormalizeWater(AbstractMap map, ExtRect area)
        {
            for (int y = area.Top; y <= area.Bottom; y++) {
                for (int x = area.Left; x <= area.Right; x++) {
                    BaseTile tile = map.GetTile(x, y);
                    if (tile != null && tile.Background == map.TranslateTile(TileType.ttWater)) {
                        if (CheckNW(map, x, y, 1, 1)) {
                            tile.Background = map.TranslateTile(TileType.ttWater);
                        } else {
                            if (CheckNW(map, x, y, 2, 3)) {
                                tile.Background = map.TranslateTile(TileType.ttDeepWater);
                            } else {
                                tile.Background = map.TranslateTile(TileType.ttDeeperWater);
                            }
                        }
                    }
                }
            }
        }

        public static void Gen_ForeObjects(AbstractMap map, ushort tid, float freq)
        {
            int cnt = (int)Math.Round((double)(map.Width * map.Height) * freq);
            for (int t = 1; t <= cnt; t++) {
                int X = RandomHelper.GetBoundedRnd(1, map.Width - 2);
                int Y = RandomHelper.GetBoundedRnd(1, map.Height - 2);
                BaseTile tile = map.GetTile(X, Y);
                if (tile != null && tile.Foreground == 0) {
                    tile.Foreground = tid;
                }
            }
        }

        public static void Gen_MountainRanges(AbstractMap map)
        {
            RandomHelper.Randomize();
            Gen_ForeObjects(map, map.TranslateTile(TileType.ttMountain), 0.45f);

            int apply; // for debug in future
            do {
                for (int y = 0; y < map.Height; y++) {
                    for (int x = 0; x < map.Width; x++) {
                        int count = map.CheckForeAdjacently(x, y, TileType.ttMountain);

                        if (map.CheckFore(x, y, TileType.ttMountain)) {
                            if (count < 4) {
                                map.GetTile(x, y).Foreground = map.TranslateTile(TileType.ttUndefined);
                            }
                        } else {
                            if (count > 4) {
                                map.GetTile(x, y).Foreground = map.TranslateTile(TileType.ttMountain);
                            }
                        }
                    }
                }
                apply = 1;
            } while (apply < 1);
        }

        public static void Gen_Forest(AbstractMap map, ExtRect area, int tileID, bool fieldClear)
        {
            if (fieldClear) {
                map.FillBackground(map.TranslateTile(TileType.ttGrass));
                map.FillForeground(map.TranslateTile(TileType.ttUndefined));
            }

            int num = RandomHelper.GetBoundedRnd(15, 25);
            for (int i = 1; i <= num; i++) {
                int x = RandomHelper.GetBoundedRnd(area.Left, area.Right);
                int y = RandomHelper.GetBoundedRnd(area.Top, area.Bottom);

                int j = 5;
                do {
                    int dir = RandomHelper.GetBoundedRnd(Directions.DtNorth, Directions.DtSouthEast);
                    x += Directions.Data[dir].DX;
                    y += Directions.Data[dir].DY;

                    BaseTile tile = map.GetTile(x, y);
                    if (tile != null && tile.Foreground == 0) {
                        tile.Foreground = (ushort)tileID;
                    }
                    j--;
                } while (j != 0);
            }
        }

        public static void Gen_Valley(AbstractMap map, bool clear, bool river, bool lakes)
        {
            if (clear) {
                map.FillBackground(map.TranslateTile(TileType.ttGrass));
                map.FillForeground(map.TranslateTile(TileType.ttUndefined));
            }

            Gen_MountainRanges(map);
            Gen_Forest(map, map.AreaRect, PlaceID.pid_Tree, false);

            if (river) {
                Gen_BigRiver(map);
            }

            if (lakes) {
                int cnt = RandomHelper.GetBoundedRnd(4, (int)Math.Round((float)map.Height / (float)map.Width * 15.0f));
                for (int i = 1; i <= cnt; i++) {
                    int X = RandomHelper.GetBoundedRnd(map.AreaRect.Left, map.AreaRect.Right);
                    int Y = RandomHelper.GetBoundedRnd(map.AreaRect.Top, map.AreaRect.Bottom);
                    ushort bg = map.GetTile(X, Y).Background;
                    if (!MapUtils.IsWaters(map, bg)) {
                        int rad = RandomHelper.GetBoundedRnd(4, 10);
                        map.Gen_Lake(ExtRect.Create(X - rad, Y - rad, X + rad, Y + rad), LakeTilesChanger);
                    }
                }
            }
        }

        private static void LakeTilesChanger(IMap map, int x, int y, object extData, ref bool  refContinue)
        {
            BaseTile tile = map.GetTile(x, y);
            if (tile != null && tile.BackBase == PlaceID.pid_Grass && tile.Foreground == 0) {
                tile.Background = PlaceID.pid_Water;
            }
        }
    }

}