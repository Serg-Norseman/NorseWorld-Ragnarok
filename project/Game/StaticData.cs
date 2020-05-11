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
using System.Text;
using BSLib;
using NWR.Database;
using NWR.Effects;
using NWR.Game.Types;
using ZRLib.Core;
using ZRLib.Engine;
using ZRLib.Grammar;

namespace NWR.Game
{
    using ItemKinds = EnumSet<ItemKind>;

    public static class StaticData
    {
        public static readonly Encoding DefEncoding = Encoding.GetEncoding(1251);

        public const string Rs_GameName = "NorseWorld: Ragnarok";
        public const string Rs_GameVersion = "v0.11.0";
        public const string Rs_GameDevTime = "2002-2008,2014,2020";
        public static readonly string Rs_GameCopyright = "Copyright (c) " + Rs_GameDevTime + " by Alchemist Team";

        public const int FieldWidth = 76;
        public const int FieldHeight = 18;
        public static readonly ExtRect FR = ExtRect.Create(-1, -1, FieldWidth, FieldHeight);
        public static readonly ExtRect MapArea = ExtRect.Create(0, 0, FieldWidth - 1, FieldHeight - 1);
        public static readonly ExtRect TV_Rect = ExtRect.Create(8, 494, 792, 592);

        public const int RsFontHeight = 18;
        public const int MC_X = 640;
        public const int MC_Y = 8;
        public const int Lg_BtnW = 152;
        public const int St_BtnW = 74;
        public const int BtnHeight = 30;
        public static readonly int BtnTH = BtnHeight + 4;

        public const string OPTIONS_FILE = "Ragnarok.Options.xml";
        public const string SCORES_FILE = "Ragnarok.Scores.rsl";
        public const string GHOSTS_FILE = "Ragnarok.Ghosts.rgl";

        // Serializable <Entity> ID (SID)
        public const byte SID_UNKNOWN = 0;
        public const byte SID_BUILDING = 1;
        public const byte SID_ITEM = 2;
        public const byte SID_CREATURE = 3;
        public const byte SID_KNOWLEDGE = 4;
        public const byte SID_EFFECT = 5;
        public const byte SID_RECALL_POS = 6;
        public const byte SID_DEBT = 7;
        public const byte SID_SOURCE_FORM = 8;
        public const byte SID_POINTGUARD_GOAL = 9;
        public const byte SID_AREAGUARD_GOAL = 10;
        public const byte SID_VILLAGE = 11;
        public const byte SID_GATE = 12;

        // Data Arrays
        public static readonly PlaceRec[] dbPlaces;
        public static readonly TrapRec[] dbTraps;
        public static readonly RuneRec[] dbRunes;
        public static readonly TeachableRec[] dbTeachable;
        public static readonly int[] dbDayTimeRS;
        public static readonly SexRec[] dbSex;
        public static readonly Gender[] GenderBySex;
        public static AbilityRec[] dbAbilities;
        public static readonly CreatureAction[] ActionByAttackKind;
        public static BuildingRec[] dbBuildings;
        public static ActionRec[] dbCreatureActions;
        public static EventRec[] dbEvent;
        public static ScreenRec[] dbScreens;
        public static UserActionRec[] dbUserActions;
        public static SysCreatureRec[] dbSysCreatures;
        public static ItemKindRec[] dbItemKinds;
        public static ItemStateRec[] dbItemStates;
        public static ItfElementRec[] dbItfElements;
        public static MainControlRec[] dbMainControls;
        public static MaterialKindRec[] dbMaterialKind;
        public static RaceRec[] dbRaces;
        public static SkillRec[] dbSkills;
        public static SymbolRec[] dbSymbols;
        public static DialogServiceRec[] dbDialogServices;
        public static EffectTargetRec[] dbEffectTarget;

        static StaticData()
        {
            GenderBySex = new Gender[] {
                Gender.gUndefined,
                Gender.gFemale,
                Gender.gMale,
                Gender.gNeutral
            };

            dbSex = new SexRec[4];
            dbSex[0] = new SexRec(CreatureSex.csNone, "csNone", RS.rs_Sex_None);
            dbSex[1] = new SexRec(CreatureSex.csFemale, "csFemale", RS.rs_Sex_Female);
            dbSex[2] = new SexRec(CreatureSex.csMale, "csMale", RS.rs_Sex_Male);
            dbSex[3] = new SexRec(CreatureSex.csHermaphrodite, "csHermaphrodite", RS.rs_Sex_Hermaphrodite);

            dbTraps = new TrapRec[15];
            dbTraps[0] = new TrapRec(PlaceID.pid_PoisonSpikeTrap, new Movements(Movements.mkFly), true, RS.rs_AvoidPoisonSpikeTrap);
            dbTraps[1] = new TrapRec(PlaceID.pid_QuicksandTrap, new Movements(Movements.mkFly), true, RS.rs_Reserved);
            dbTraps[2] = new TrapRec(PlaceID.pid_WaterTrap, new Movements(Movements.mkSwim, Movements.mkFly), false, RS.rs_Reserved);
            dbTraps[3] = new TrapRec(PlaceID.pid_TeleportTrap, new Movements(Movements.mkFly), false, RS.rs_Reserved);
            dbTraps[4] = new TrapRec(PlaceID.pid_StunGasTrap, new Movements(Movements.mkFly), false, RS.rs_AvoidStunGasTrap);
            dbTraps[5] = new TrapRec(PlaceID.pid_PitTrap, new Movements(Movements.mkFly), false, RS.rs_Reserved);
            dbTraps[6] = new TrapRec(PlaceID.pid_DoorTrap, new Movements(Movements.mkFly), false, RS.rs_AvoidDoorTrap);
            dbTraps[7] = new TrapRec(PlaceID.pid_FireTrap, new Movements(Movements.mkFly), false, RS.rs_AvoidFireTrap);
            dbTraps[8] = new TrapRec(PlaceID.pid_FrostTrap, new Movements(Movements.mkFly), false, RS.rs_AvoidFrostTrap);
            dbTraps[9] = new TrapRec(PlaceID.pid_LavaTrap, new Movements(Movements.mkFly), true, RS.rs_Reserved);
            dbTraps[10] = new TrapRec(PlaceID.pid_MonsterTrap, new Movements(Movements.mkFly), true, RS.rs_Reserved);
            dbTraps[11] = new TrapRec(PlaceID.pid_PhaseTrap, new Movements(Movements.mkFly), false, RS.rs_AvoidPhaseTrap);
            dbTraps[12] = new TrapRec(PlaceID.pid_ArrowTrap, new Movements(Movements.mkFly), true, RS.rs_Reserved);
            dbTraps[13] = new TrapRec(PlaceID.pid_CrushRoofTrap, new Movements(Movements.mkFly), true, RS.rs_Reserved);
            dbTraps[14] = new TrapRec(PlaceID.pid_MistTrap, new Movements(Movements.mkFly), false, RS.rs_Reserved);

            dbPlaces = new PlaceRec[128];
            dbPlaces[0] = new PlaceRec(RS.rs_Reserved, "", SymbolID.sid_None, new PlaceFlags(), new Movements(), 0, EffectID.eid_None);
            dbPlaces[1] = new PlaceRec(RS.rs_Tile_PoisonSpikeTrap, "PoisonSpikeTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_PoisonSpikeTrap);
            dbPlaces[2] = new PlaceRec(RS.rs_Tile_QuicksandTrap, "QuicksandTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_Quicksand);
            dbPlaces[3] = new PlaceRec(RS.rs_Tile_WaterTrap, "WaterTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_WaterTrap);
            dbPlaces[4] = new PlaceRec(RS.rs_Tile_Grass, "Grass", SymbolID.sid_Grass, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psVarDirect, PlaceFlags.psIsGround, PlaceFlags.psCanCreate, PlaceFlags.psNotInLegend), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[5] = new PlaceRec(RS.rs_Tile_Tree, "Tree", SymbolID.sid_Tree, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psVarDirect, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier, PlaceFlags.psCanCreate), new Movements(Movements.mkFly, Movements.mkEthereality), 7, EffectID.eid_None);
            dbPlaces[6] = new PlaceRec(RS.rs_Tile_Mountain, "Mountain", SymbolID.sid_Mountain, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psVarDirect, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier, PlaceFlags.psCanCreate), new Movements(Movements.mkFly), 3, EffectID.eid_None);
            dbPlaces[7] = new PlaceRec(RS.rs_Tile_Fog, "Fog", SymbolID.sid_Fog, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkSwim, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[8] = new PlaceRec(RS.rs_Tile_LavaPool, "LavaPool", SymbolID.sid_Lava, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkSwim, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[9] = new PlaceRec(RS.rs_Tile_DeadTree, "DeadTree", SymbolID.sid_DeadTree, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[10] = new PlaceRec(RS.rs_Tile_Water, "Water", SymbolID.sid_Water, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psVarDirect, PlaceFlags.psCanCreate), new Movements(Movements.mkSwim, Movements.mkFly), 3, EffectID.eid_None);
            dbPlaces[11] = new PlaceRec(RS.rs_Tile_Quicksand, "Quicksand", SymbolID.sid_Quicksand, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psIsGround, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_Quicksand);
            dbPlaces[12] = new PlaceRec(RS.rs_Tile_Space, "Space", SymbolID.sid_Space, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psBarrier), new Movements(Movements.mkFly), 0, EffectID.eid_None); //Movements.mkWalk,
            dbPlaces[13] = new PlaceRec(RS.rs_Tile_Floor, "Floor", SymbolID.sid_Floor, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psIsGround, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[14] = new PlaceRec(RS.rs_Tile_Bifrost, "Bifrost", SymbolID.sid_Bifrost, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk, Movements.mkFly), 150, EffectID.eid_None);
            dbPlaces[15] = new PlaceRec(RS.rs_Tile_Door, "DoorN", SymbolID.sid_DoorN_Opened, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[16] = new PlaceRec(RS.rs_Tile_Door, "DoorS", SymbolID.sid_DoorS_Opened, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[17] = new PlaceRec(RS.rs_Tile_Door, "DoorW", SymbolID.sid_DoorW_Opened, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[18] = new PlaceRec(RS.rs_Tile_Door, "DoorE", SymbolID.sid_DoorE_Opened, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[19] = new PlaceRec(RS.rs_Tile_DoorClosed, "DoorN_Closed", SymbolID.sid_DoorN_Closed, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[20] = new PlaceRec(RS.rs_Tile_DoorClosed, "DoorS_Closed", SymbolID.sid_DoorS_Closed, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[21] = new PlaceRec(RS.rs_Tile_DoorClosed, "DoorW_Closed", SymbolID.sid_DoorW_Closed, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[22] = new PlaceRec(RS.rs_Tile_DoorClosed, "DoorE_Closed", SymbolID.sid_DoorE_Closed, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[23] = new PlaceRec(RS.rs_Tile_Wall, "WallN", SymbolID.sid_WallN, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[24] = new PlaceRec(RS.rs_Tile_Wall, "WallS", SymbolID.sid_WallS, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[25] = new PlaceRec(RS.rs_Tile_Wall, "WallW", SymbolID.sid_WallW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[26] = new PlaceRec(RS.rs_Tile_Wall, "WallE", SymbolID.sid_WallE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[27] = new PlaceRec(RS.rs_Tile_Wall, "WallNW", SymbolID.sid_WallNW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[28] = new PlaceRec(RS.rs_Tile_Wall, "WallNE", SymbolID.sid_WallNE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[29] = new PlaceRec(RS.rs_Tile_Wall, "WallSW", SymbolID.sid_WallSW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[30] = new PlaceRec(RS.rs_Tile_Wall, "WallSE", SymbolID.sid_WallSE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[31] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_A", SymbolID.sid_cr_a, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[32] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_B", SymbolID.sid_cr_b, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[33] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_BA", SymbolID.sid_cr_ba, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[34] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_G", SymbolID.sid_cr_g, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[35] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_GK", SymbolID.sid_cr_gk, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[36] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_K", SymbolID.sid_cr_k, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[37] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_L", SymbolID.sid_cr_l, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[38] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_R", SymbolID.sid_cr_r, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[39] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_W", SymbolID.sid_cr_w, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[40] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_WL", SymbolID.sid_cr_wl, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[41] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_Y", SymbolID.sid_cr_y, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[42] = new PlaceRec(RS.rs_Tile_Crossroads, "cr_YR", SymbolID.sid_cr_yr, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[43] = new PlaceRec(RS.rs_Tile_Crossroads_Disk, "cr_Disk", SymbolID.sid_cr_disk, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[44] = new PlaceRec(RS.rs_Tile_Vortex, "Vortex", SymbolID.sid_Vortex, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsFixGate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[45] = new PlaceRec(RS.rs_Tile_StairsDown, "StairsDown", SymbolID.sid_StairsDown, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsFreeGate, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 1, EffectID.eid_None);
            dbPlaces[46] = new PlaceRec(RS.rs_Tile_StairsUp, "StairsUp", SymbolID.sid_StairsUp, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsFreeGate, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 1, EffectID.eid_None);
            dbPlaces[47] = new PlaceRec(RS.rs_Tile_TeleportTrap, "TeleportTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_Relocation);
            dbPlaces[48] = new PlaceRec(RS.rs_Tile_StunGasTrap, "StunGasTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_StunGasTrap);
            dbPlaces[49] = new PlaceRec(RS.rs_Tile_Well, "Well", SymbolID.sid_Well, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[50] = new PlaceRec(RS.rs_Tile_StairsDown, "GStairsDown", SymbolID.sid_StairsDown, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsFreeGate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[51] = new PlaceRec(RS.rs_Tile_StairsUp, "GStairsUp", SymbolID.sid_StairsUp, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsFreeGate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[52] = new PlaceRec(RS.rs_Tile_PitTrap, "Pit", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_PitTrap);
            dbPlaces[53] = new PlaceRec(RS.rs_Tile_DoorTrap, "DoorTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_DoorTrap);
            dbPlaces[54] = new PlaceRec(RS.rs_Tile_FireTrap, "FireTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_FireTrap);
            dbPlaces[55] = new PlaceRec(RS.rs_Tile_FrostTrap, "FrostTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_FrostTrap);
            dbPlaces[56] = new PlaceRec(RS.rs_Tile_LavaTrap, "LavaTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_LavaTrap);
            dbPlaces[57] = new PlaceRec(RS.rs_Tile_Rock, "Rock", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[58] = new PlaceRec(RS.rs_Tile_Stalagmite, "Stalagmite", SymbolID.sid_Stalagmite, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psVarDirect, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 29, EffectID.eid_None);
            dbPlaces[59] = new PlaceRec(RS.rs_Tile_MonsterTrap, "MonsterTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_MonsterTrap);
            dbPlaces[60] = new PlaceRec(RS.rs_Tile_PhaseTrap, "PhaseTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_PhaseTrap);
            dbPlaces[61] = new PlaceRec(RS.rs_Tile_ArrowTrap, "ArrowTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_ArrowTrap);
            dbPlaces[62] = new PlaceRec(RS.rs_Tile_CrushRoofTrap, "CrushRoofTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_CrushRoofTrap);
            dbPlaces[63] = new PlaceRec(RS.rs_Tile_MistTrap, "MistTrap", SymbolID.sid_Trap, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsTrap), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_MistTrap);
            dbPlaces[64] = new PlaceRec(RS.rs_Tile_CaveFloor, "CaveFloor", SymbolID.sid_CaveFloor, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psIsGround), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[65] = new PlaceRec(RS.rs_Tile_CaveWall, "CaveWall", SymbolID.sid_CaveWall, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 2, EffectID.eid_None);
            dbPlaces[66] = new PlaceRec(RS.rs_Tile_Fence, "FenceN", SymbolID.sid_WallN, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBarrier), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[67] = new PlaceRec(RS.rs_Tile_Fence, "FenceS", SymbolID.sid_WallS, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBarrier), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[68] = new PlaceRec(RS.rs_Tile_Fence, "FenceW", SymbolID.sid_WallW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBarrier), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[69] = new PlaceRec(RS.rs_Tile_Fence, "FenceE", SymbolID.sid_WallE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBarrier), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[70] = new PlaceRec(RS.rs_Tile_Fence, "FenceNE", SymbolID.sid_WallNE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBarrier), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[71] = new PlaceRec(RS.rs_Tile_Fence, "FenceNW", SymbolID.sid_WallNW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBarrier), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[72] = new PlaceRec(RS.rs_Tile_Fence, "FenceSE", SymbolID.sid_WallSE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBarrier), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[73] = new PlaceRec(RS.rs_Tile_Fence, "FenceSW", SymbolID.sid_WallSW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBarrier), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[74] = new PlaceRec(RS.rs_Tile_FenceGate, "FenceGateN", SymbolID.sid_Grass, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[75] = new PlaceRec(RS.rs_Tile_FenceGate, "FenceGateS", SymbolID.sid_Grass, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[76] = new PlaceRec(RS.rs_Tile_FenceGate, "FenceGateW", SymbolID.sid_Grass, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[77] = new PlaceRec(RS.rs_Tile_FenceGate, "FenceGateE", SymbolID.sid_Grass, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[78] = new PlaceRec(RS.rs_Unknown, "GrassMask", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psMask), new Movements(), 15, EffectID.eid_None);
            dbPlaces[79] = new PlaceRec(RS.rs_Unknown, "WaterMask", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psMask), new Movements(), 15, EffectID.eid_None);
            dbPlaces[80] = new PlaceRec(RS.rs_Tile_Lava, "Lava", SymbolID.sid_Lava, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkSwim, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[81] = new PlaceRec(RS.rs_Unknown, "LavaMask", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psMask), new Movements(), 15, EffectID.eid_None);
            dbPlaces[82] = new PlaceRec(RS.rs_Tile_Vulcan, "Vulcan", SymbolID.sid_Vulcan, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBlockLOS), new Movements(), 0, EffectID.eid_None);
            dbPlaces[83] = new PlaceRec(RS.rs_Tile_Liquid, "Liquid", SymbolID.sid_Liquid, new PlaceFlags(PlaceFlags.psBackground), new Movements(Movements.mkSwim, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[84] = new PlaceRec(RS.rs_Unknown, "LiquidMask", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psMask), new Movements(), 15, EffectID.eid_None);
            dbPlaces[85] = new PlaceRec(RS.rs_Tile_HoleDown, "HoleDown", SymbolID.sid_SmallPit, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsFreeGate, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[86] = new PlaceRec(RS.rs_Tile_HoleUp, "HoleUp", SymbolID.sid_SmallPit, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsFreeGate, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[87] = new PlaceRec(RS.rs_Tile_Mud, "Mud", SymbolID.sid_Mud, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[88] = new PlaceRec(RS.rs_Tile_SmallPit, "SmallPit", SymbolID.sid_SmallPit, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[89] = new PlaceRec(RS.rs_Unknown, "MudMask", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psMask), new Movements(), 15, EffectID.eid_None);
            dbPlaces[90] = new PlaceRec(RS.rs_Tile_Rubble, "Rubble", SymbolID.sid_Rubble, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psCanCreate), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[91] = new PlaceRec(RS.rs_Unknown, "RubbleMask", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psMask), new Movements(), 15, EffectID.eid_None);
            dbPlaces[92] = new PlaceRec(RS.rs_Tile_Ground, "Ground", SymbolID.sid_Ground, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[93] = new PlaceRec(RS.rs_Tile_Road, "RoadMask1", SymbolID.sid_Road, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 15, EffectID.eid_None);
            dbPlaces[94] = new PlaceRec(RS.rs_Tile_Road, "RoadMask2", SymbolID.sid_Road, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 15, EffectID.eid_None);
            dbPlaces[95] = new PlaceRec(RS.rs_Tile_Road, "RoadMask3", SymbolID.sid_Road, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 15, EffectID.eid_None);
            dbPlaces[96] = new PlaceRec(RS.rs_Tile_Bush, "Bush", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psVarDirect), new Movements(Movements.mkWalk, Movements.mkFly), 3, EffectID.eid_None);
            dbPlaces[97] = new PlaceRec(RS.rs_Tile_Stump, "Stump", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psVarDirect), new Movements(Movements.mkWalk, Movements.mkFly), 2, EffectID.eid_None);
            dbPlaces[98] = new PlaceRec(RS.rs_Tile_Crossroads_Disk, "cr_Disk_Pressed", SymbolID.sid_cr_disk, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk), 0, EffectID.eid_None);
            dbPlaces[99] = new PlaceRec(RS.rs_Unknown, "FogMask1", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psMask), new Movements(), 15, EffectID.eid_None);
            dbPlaces[100] = new PlaceRec(RS.rs_Unknown, "FogMask2", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psMask), new Movements(), 9, EffectID.eid_None);
            dbPlaces[101] = new PlaceRec(RS.rs_Tile_Wall, "AsgardWallN", SymbolID.sid_WallN, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[102] = new PlaceRec(RS.rs_Tile_Wall, "AsgardWallS", SymbolID.sid_WallS, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[103] = new PlaceRec(RS.rs_Tile_Wall, "AsgardWallW", SymbolID.sid_WallW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[104] = new PlaceRec(RS.rs_Tile_Wall, "AsgardWallE", SymbolID.sid_WallE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[105] = new PlaceRec(RS.rs_Tile_Wall, "AsgardWallNW", SymbolID.sid_WallNW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[106] = new PlaceRec(RS.rs_Tile_Wall, "AsgardWallNE", SymbolID.sid_WallNE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[107] = new PlaceRec(RS.rs_Tile_Wall, "AsgardWallSW", SymbolID.sid_WallSW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[108] = new PlaceRec(RS.rs_Tile_Wall, "AsgardWallSE", SymbolID.sid_WallSE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[109] = new PlaceRec(RS.rs_Tile_Door, "AsgardDoorN", SymbolID.sid_DoorN_Opened, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[110] = new PlaceRec(RS.rs_Tile_Door, "AsgardDoorS", SymbolID.sid_DoorS_Opened, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[111] = new PlaceRec(RS.rs_Tile_Door, "AsgardDoorW", SymbolID.sid_DoorW_Opened, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[112] = new PlaceRec(RS.rs_Tile_Door, "AsgardDoorE", SymbolID.sid_DoorE_Opened, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[113] = new PlaceRec(RS.rs_Tile_Vortex, "VortexStrange", SymbolID.sid_VortexStrange, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsFixGate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[114] = new PlaceRec(RS.rs_Unknown, "SpaceMask", SymbolID.sid_None, new PlaceFlags(PlaceFlags.psMask), new Movements(), 15, EffectID.eid_None);
            dbPlaces[115] = new PlaceRec(RS.rs_Unknown, "Ting", SymbolID.sid_Ting, new PlaceFlags(PlaceFlags.psForeground), new Movements(Movements.mkWalk, Movements.mkFly), 64, EffectID.eid_None);
            dbPlaces[116] = new PlaceRec(RS.rs_Tile_Floor, "DungeonFloor", SymbolID.sid_CaveFloor, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psIsGround, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 0, EffectID.eid_None);
            dbPlaces[117] = new PlaceRec(RS.rs_Tile_Wall, "DungeonWall", SymbolID.sid_CaveWall, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 46, EffectID.eid_None);
            dbPlaces[118] = new PlaceRec(RS.rs_Tile_Floor, "RnFloor", SymbolID.sid_Floor, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psIsGround, PlaceFlags.psCanCreate), new Movements(Movements.mkWalk, Movements.mkFly), 2, EffectID.eid_None);
            dbPlaces[119] = new PlaceRec(RS.rs_Tile_Wall, "RnWallN", SymbolID.sid_WallN, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[120] = new PlaceRec(RS.rs_Tile_Wall, "RnWallS", SymbolID.sid_WallS, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[121] = new PlaceRec(RS.rs_Tile_Wall, "RnWallW", SymbolID.sid_WallW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[122] = new PlaceRec(RS.rs_Tile_Wall, "RnWallE", SymbolID.sid_WallE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[123] = new PlaceRec(RS.rs_Tile_Wall, "RnWallNW", SymbolID.sid_WallNW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[124] = new PlaceRec(RS.rs_Tile_Wall, "RnWallNE", SymbolID.sid_WallNE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[125] = new PlaceRec(RS.rs_Tile_Wall, "RnWallSW", SymbolID.sid_WallSW, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[126] = new PlaceRec(RS.rs_Tile_Wall, "RnWallSE", SymbolID.sid_WallSE, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psIsBuilding, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier), new Movements(Movements.mkEthereality), 0, EffectID.eid_None);
            dbPlaces[127] = new PlaceRec(RS.rs_Tile_Tree, "IronTree", SymbolID.sid_IronTree, new PlaceFlags(PlaceFlags.psForeground, PlaceFlags.psVarDirect, PlaceFlags.psBlockLOS, PlaceFlags.psBarrier, PlaceFlags.psCanCreate), new Movements(Movements.mkFly, Movements.mkEthereality), 0, EffectID.eid_None);

            dbRunes = new RuneRec[25];
            dbRunes[0] = new RuneRec("Rune_Fehu");
            dbRunes[1] = new RuneRec("Rune_Uruz");
            dbRunes[2] = new RuneRec("Rune_Thurisaz");
            dbRunes[3] = new RuneRec("Rune_Ansuz");
            dbRunes[4] = new RuneRec("Rune_Raido");
            dbRunes[5] = new RuneRec("Rune_Kaunan");
            dbRunes[6] = new RuneRec("Rune_Gebo");
            dbRunes[7] = new RuneRec("Rune_Wunjo");
            dbRunes[8] = new RuneRec("Rune_Hagalaz");
            dbRunes[9] = new RuneRec("Rune_Naudiz");
            dbRunes[10] = new RuneRec("Rune_Isaz");
            dbRunes[11] = new RuneRec("Rune_Jeran");
            dbRunes[12] = new RuneRec("Rune_Eihwaz");
            dbRunes[13] = new RuneRec("Rune_Pertho");
            dbRunes[14] = new RuneRec("Rune_Algiz");
            dbRunes[15] = new RuneRec("Rune_Sowilo");
            dbRunes[16] = new RuneRec("Rune_Tiwaz");
            dbRunes[17] = new RuneRec("Rune_Berkana");
            dbRunes[18] = new RuneRec("Rune_Ehwaz");
            dbRunes[19] = new RuneRec("Rune_Mannaz");
            dbRunes[20] = new RuneRec("Rune_Laguz");
            dbRunes[21] = new RuneRec("Rune_Ingwaz");
            dbRunes[22] = new RuneRec("Rune_Dagaz");
            dbRunes[23] = new RuneRec("Rune_Othalan");
            dbRunes[24] = new RuneRec("Rune_Empty");

            dbTeachable = new TeachableRec[41];
            dbTeachable[0] = new TeachableRec((int)AbilityID.Ab_Identification, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[1] = new TeachableRec((int)AbilityID.Ab_ShortBlades, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[2] = new TeachableRec((int)AbilityID.Ab_LongBow, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[3] = new TeachableRec((int)AbilityID.Ab_CrossBow, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[4] = new TeachableRec((int)AbilityID.Ab_Spear, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[5] = new TeachableRec((int)AbilityID.Ab_Axe, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[6] = new TeachableRec((int)AbilityID.Ab_Parry, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[7] = new TeachableRec((int)AbilityID.Ab_BluntWeapon, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[8] = new TeachableRec((int)AbilityID.Ab_HandToHand, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[9] = new TeachableRec((int)AbilityID.Ab_HeavyArmor, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[10] = new TeachableRec((int)AbilityID.Ab_LightArmor, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[11] = new TeachableRec((int)AbilityID.Ab_LongBlade, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[12] = new TeachableRec((int)AbilityID.Ab_Marksman, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[13] = new TeachableRec((int)AbilityID.Ab_MediumArmor, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[14] = new TeachableRec((int)AbilityID.Ab_Levitation, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[15] = new TeachableRec((int)AbilityID.Ab_Swimming, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[16] = new TeachableRec((int)AbilityID.Ab_Telepathy, TeachableKind.Ability, RS.rs_Reserved);
            dbTeachable[17] = new TeachableRec((int)SkillID.Sk_Alchemy, TeachableKind.Skill, RS.rs_YouLearnAlchemy);
            dbTeachable[18] = new TeachableRec((int)SkillID.Sk_ArrowMake, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[19] = new TeachableRec((int)SkillID.Sk_Cartography, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[20] = new TeachableRec((int)SkillID.Sk_Diagnosis, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[21] = new TeachableRec((int)SkillID.Sk_Embalming, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[22] = new TeachableRec((int)SkillID.Sk_Fennling, TeachableKind.Skill, RS.rs_YouLearnFennl);
            dbTeachable[23] = new TeachableRec((int)SkillID.Sk_GolemCreation, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[24] = new TeachableRec((int)SkillID.Sk_Husbandry, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[25] = new TeachableRec((int)SkillID.Sk_Ironworking, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[26] = new TeachableRec((int)SkillID.Sk_Precognition, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[27] = new TeachableRec((int)SkillID.Sk_Relocation, TeachableKind.Skill, RS.rs_YouLearnTeleportAtWill);
            dbTeachable[28] = new TeachableRec((int)SkillID.Sk_SlaveUse, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[29] = new TeachableRec((int)SkillID.Sk_Taming, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[30] = new TeachableRec((int)SkillID.Sk_Ventriloquism, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[31] = new TeachableRec((int)SkillID.Sk_Writing, TeachableKind.Skill, RS.rs_YouLearnWrite);
            dbTeachable[32] = new TeachableRec((int)SkillID.Sk_Animation, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[33] = new TeachableRec((int)SkillID.Sk_DimensionTravel, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[34] = new TeachableRec((int)SkillID.Sk_MindControl, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[35] = new TeachableRec((int)SkillID.Sk_Terraforming, TeachableKind.Skill, RS.rs_YouLearnTerraform);
            dbTeachable[36] = new TeachableRec((int)SkillID.Sk_Spellcasting, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[37] = new TeachableRec((int)SkillID.Sk_FireVision, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[38] = new TeachableRec((int)SkillID.Sk_Prayer, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[39] = new TeachableRec((int)SkillID.Sk_Sacrifice, TeachableKind.Skill, RS.rs_Reserved);
            dbTeachable[40] = new TeachableRec((int)SkillID.Sk_Divination, TeachableKind.Skill, RS.rs_Reserved);

            dbDayTimeRS = new int[9];
            dbDayTimeRS[0] = RS.rs_Reserved;
            dbDayTimeRS[1] = RS.rs_Night;
            dbDayTimeRS[2] = RS.rs_Midnight;
            dbDayTimeRS[3] = RS.rs_Night;
            dbDayTimeRS[4] = RS.rs_Dawn;
            dbDayTimeRS[5] = RS.rs_Day;
            dbDayTimeRS[6] = RS.rs_Noon;
            dbDayTimeRS[7] = RS.rs_Day;
            dbDayTimeRS[8] = RS.rs_Dusk;

            dbAbilities = new AbilityRec[36];
            dbAbilities[0] = new AbilityRec(RS.rs_Unknown);
            dbAbilities[1] = new AbilityRec(RS.rs_Swimming);
            dbAbilities[2] = new AbilityRec(RS.rs_Identification);
            dbAbilities[3] = new AbilityRec(RS.rs_Telepathy);
            dbAbilities[4] = new AbilityRec(RS.rs_SixthSense);
            dbAbilities[5] = new AbilityRec(RS.rs_MusicalAcuity);
            dbAbilities[6] = new AbilityRec(RS.rs_ShortBlades);
            dbAbilities[7] = new AbilityRec(RS.rs_LongBow);
            dbAbilities[8] = new AbilityRec(RS.rs_CrossBow);
            dbAbilities[9] = new AbilityRec(RS.rs_Levitation);
            dbAbilities[10] = new AbilityRec(RS.rs_Marksman);
            dbAbilities[11] = new AbilityRec(RS.rs_Spear);
            dbAbilities[12] = new AbilityRec(RS.rs_RayReflect);
            dbAbilities[13] = new AbilityRec(RS.rs_RayAbsorb);
            dbAbilities[14] = new AbilityRec(RS.rs_Resist_Cold);
            dbAbilities[15] = new AbilityRec(RS.rs_Resist_Heat);
            dbAbilities[16] = new AbilityRec(RS.rs_Resist_Acid);
            dbAbilities[17] = new AbilityRec(RS.rs_Resist_Poison);
            dbAbilities[18] = new AbilityRec(RS.rs_Resist_Ray);
            dbAbilities[19] = new AbilityRec(RS.rs_Resist_Teleport);
            dbAbilities[20] = new AbilityRec(RS.rs_Resist_DeathRay);
            dbAbilities[21] = new AbilityRec(RS.rs_Resist_Petrification);
            dbAbilities[22] = new AbilityRec(RS.rs_Resist_Psionic);
            dbAbilities[23] = new AbilityRec(RS.rs_Resist_DisplacementRay);
            dbAbilities[24] = new AbilityRec(RS.rs_Resist_HasteningRay);
            dbAbilities[25] = new AbilityRec(RS.rs_Resist_SleepRay);
            dbAbilities[26] = new AbilityRec(RS.rs_Regeneration);
            dbAbilities[27] = new AbilityRec(RS.rs_ThirdSight);
            dbAbilities[28] = new AbilityRec(RS.rs_Axe);
            dbAbilities[29] = new AbilityRec(RS.rs_Parry);
            dbAbilities[30] = new AbilityRec(RS.rs_BluntWeapon);
            dbAbilities[31] = new AbilityRec(RS.rs_HandToHand);
            dbAbilities[32] = new AbilityRec(RS.rs_HeavyArmor);
            dbAbilities[33] = new AbilityRec(RS.rs_MediumArmor);
            dbAbilities[34] = new AbilityRec(RS.rs_LightArmor);
            dbAbilities[35] = new AbilityRec(RS.rs_LongBlade);

            ActionByAttackKind = new CreatureAction[] {
                CreatureAction.caAttackMelee,
                CreatureAction.caAttackShoot,
                CreatureAction.caAttackThrow
            };

            dbBuildings = new BuildingRec[8];
            dbBuildings[0] = new BuildingRec(RS.rs_Reserved, SysCreature.sc_Viking, ItemKinds.Create(), 0, 0, 0, 0, 0);
            dbBuildings[1] = new BuildingRec(RS.rs_House, SysCreature.sc_Viking, ItemKinds.Create(), 1, 1, 2, 4, 5);
            dbBuildings[2] = new BuildingRec(RS.rs_MerchantShop, SysCreature.sc_Merchant, ItemKinds.Create(ItemKind.ik_Armor, ItemKind.ik_Food, ItemKind.ik_Potion, ItemKind.ik_Ring, ItemKind.ik_Tool, ItemKind.ik_Wand, ItemKind.ik_BluntWeapon, ItemKind.ik_Scroll, ItemKind.ik_Amulet, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Clothing, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor, ItemKind.ik_MusicalTool, ItemKind.ik_Projectile, ItemKind.ik_Misc), 1, 1, 2, 4, 6);
            dbBuildings[3] = new BuildingRec(RS.rs_Smithy, SysCreature.sc_Blacksmith, ItemKinds.Create(ItemKind.ik_Armor, ItemKind.ik_BluntWeapon, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor, ItemKind.ik_Projectile), 1, 1, 2, 4, 5);
            dbBuildings[4] = new BuildingRec(RS.rs_AlchemistShop, SysCreature.sc_Alchemist, ItemKinds.Create(ItemKind.ik_Potion), 1, 1, 2, 4, 5);
            dbBuildings[5] = new BuildingRec(RS.rs_ConjurerShop, SysCreature.sc_Conjurer, ItemKinds.Create(ItemKind.ik_Ring, ItemKind.ik_Wand, ItemKind.ik_Amulet), 1, 1, 2, 4, 5);
            dbBuildings[6] = new BuildingRec(RS.rs_SageShop, SysCreature.sc_Sage, ItemKinds.Create(ItemKind.ik_Scroll), 1, 1, 2, 4, 5);
            dbBuildings[7] = new BuildingRec(RS.rs_WoodsmanShop, SysCreature.sc_Woodsman, ItemKinds.Create(ItemKind.ik_Food, ItemKind.ik_ShortBlade, ItemKind.ik_Spear, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_Projectile), 1, 1, 2, 4, 5);

            dbCreatureActions = new ActionRec[16];
            dbCreatureActions[0] = new ActionRec(1f, new ActionFlags());
            dbCreatureActions[1] = new ActionRec(1f, new ActionFlags());
            dbCreatureActions[2] = new ActionRec(1f, new ActionFlags());
            dbCreatureActions[3] = new ActionRec(1f, new ActionFlags(ActionFlags.WithItem, ActionFlags.CheckAbility));
            dbCreatureActions[4] = new ActionRec(1f, new ActionFlags(ActionFlags.WithItem, ActionFlags.CheckAbility));
            dbCreatureActions[5] = new ActionRec(1f, new ActionFlags(ActionFlags.WithItem, ActionFlags.CheckAbility));
            dbCreatureActions[6] = new ActionRec(1f, new ActionFlags(ActionFlags.WithItem, ActionFlags.CheckAbility));
            dbCreatureActions[7] = new ActionRec(1f, new ActionFlags(ActionFlags.WithItem));
            dbCreatureActions[8] = new ActionRec(1f, new ActionFlags(ActionFlags.WithItem));
            dbCreatureActions[9] = new ActionRec(1f, new ActionFlags(ActionFlags.WithItem));
            dbCreatureActions[10] = new ActionRec(1f, new ActionFlags(ActionFlags.WithItem));
            dbCreatureActions[11] = new ActionRec(1f, new ActionFlags());
            dbCreatureActions[12] = new ActionRec(1f, new ActionFlags(ActionFlags.WithItem));
            dbCreatureActions[13] = new ActionRec(1f, new ActionFlags());
            dbCreatureActions[14] = new ActionRec(1f, new ActionFlags());
            dbCreatureActions[15] = new ActionRec(1f, new ActionFlags());

            dbEvent = new EventRec[63];
            dbEvent[0] = new EventRec("event_Nothing", new EventFlags(), 0);
            dbEvent[1] = new EventRec("event_Startup", new EventFlags(), 0);
            dbEvent[2] = new EventRec("event_Map", new EventFlags(), 0);
            dbEvent[3] = new EventRec("event_About", new EventFlags(), 0);
            dbEvent[4] = new EventRec("event_Self", new EventFlags(), 0);
            dbEvent[5] = new EventRec("event_Wait", new EventFlags(), 0);
            dbEvent[6] = new EventRec("event_PickupAll", new EventFlags(), 0);
            dbEvent[7] = new EventRec("event_DoorClose", new EventFlags(), 0);
            dbEvent[8] = new EventRec("event_DoorOpen", new EventFlags(), 0);
            dbEvent[9] = new EventRec("event_Help", new EventFlags(), 0);
            dbEvent[10] = new EventRec("event_AutoStart", new EventFlags(), 0);
            dbEvent[11] = new EventRec("event_AutoStop", new EventFlags(), 0);
            dbEvent[12] = new EventRec("event_LandEnter", new EventFlags(EventFlags.InJournal), 0);
            dbEvent[13] = new EventRec("event_Hit", new EventFlags(), 0);
            dbEvent[14] = new EventRec("event_Miss", new EventFlags(), 0);
            dbEvent[15] = new EventRec("event_Intro", new EventFlags(EventFlags.InJournal), 0);
            dbEvent[16] = new EventRec("event_Knowledges", new EventFlags(), 0);
            dbEvent[17] = new EventRec("event_Skills", new EventFlags(), 0);
            dbEvent[18] = new EventRec("event_Menu", new EventFlags(), 0);
            dbEvent[19] = new EventRec("event_Attack", new EventFlags(EventFlags.InQueue, EventFlags.InJournal), 100);
            dbEvent[20] = new EventRec("event_Killed", new EventFlags(EventFlags.InJournal), 0);
            dbEvent[21] = new EventRec("event_Move", new EventFlags(), 0);
            dbEvent[22] = new EventRec("event_Shot", new EventFlags(), 0);
            dbEvent[23] = new EventRec("event_Slay", new EventFlags(), 0);
            dbEvent[24] = new EventRec("event_Wounded", new EventFlags(EventFlags.InJournal), 0);
            dbEvent[25] = new EventRec("event_Trap", new EventFlags(), 0);
            dbEvent[26] = new EventRec("event_Trade", new EventFlags(), 0);
            dbEvent[27] = new EventRec("event_LevelUp", new EventFlags(), 0);
            dbEvent[28] = new EventRec("event_Pack", new EventFlags(), 0);
            dbEvent[29] = new EventRec("event_DialogBegin", new EventFlags(), 0);
            dbEvent[30] = new EventRec("event_DialogEnd", new EventFlags(), 0);
            dbEvent[31] = new EventRec("event_DialogRemark", new EventFlags(), 0);
            dbEvent[32] = new EventRec("event_Options", new EventFlags(), 0);
            dbEvent[33] = new EventRec("event_Throw", new EventFlags(), 0);
            dbEvent[34] = new EventRec("event_Defeat", new EventFlags(), 0);
            dbEvent[35] = new EventRec("event_Dialog", new EventFlags(), 0);
            dbEvent[36] = new EventRec("event_LookAt", new EventFlags(), 0);
            dbEvent[37] = new EventRec("event_Quit", new EventFlags(), 0);
            dbEvent[38] = new EventRec("event_Save", new EventFlags(), 0);
            dbEvent[39] = new EventRec("event_Load", new EventFlags(), 0);
            dbEvent[40] = new EventRec("event_New", new EventFlags(), 0);
            dbEvent[41] = new EventRec("event_Dead", new EventFlags(), 0);
            dbEvent[42] = new EventRec("event_Party", new EventFlags(), 0);
            dbEvent[43] = new EventRec("event_Victory", new EventFlags(), 0);
            dbEvent[44] = new EventRec("event_EffectSound", new EventFlags(), 0);
            dbEvent[45] = new EventRec("event_ItemDrop", new EventFlags(), 0);
            dbEvent[46] = new EventRec("event_ItemPickup", new EventFlags(), 0);
            dbEvent[47] = new EventRec("event_ItemRemove", new EventFlags(), 0);
            dbEvent[48] = new EventRec("event_ItemWear", new EventFlags(), 0);
            dbEvent[49] = new EventRec("event_ItemBreak", new EventFlags(), 0);
            dbEvent[50] = new EventRec("event_ItemUse", new EventFlags(), 0);
            dbEvent[51] = new EventRec("event_ItemMix", new EventFlags(), 0);
            dbEvent[52] = new EventRec("event_PlayerMoveN", new EventFlags(), 0);
            dbEvent[53] = new EventRec("event_PlayerMoveS", new EventFlags(), 0);
            dbEvent[54] = new EventRec("event_PlayerMoveW", new EventFlags(), 0);
            dbEvent[55] = new EventRec("event_PlayerMoveE", new EventFlags(), 0);
            dbEvent[56] = new EventRec("event_PlayerMoveNW", new EventFlags(), 0);
            dbEvent[57] = new EventRec("event_PlayerMoveNE", new EventFlags(), 0);
            dbEvent[58] = new EventRec("event_PlayerMoveSW", new EventFlags(), 0);
            dbEvent[59] = new EventRec("event_PlayerMoveSE", new EventFlags(), 0);
            dbEvent[60] = new EventRec("event_PlayerMoveUp", new EventFlags(), 0);
            dbEvent[61] = new EventRec("event_PlayerMoveDown", new EventFlags(), 0);
            dbEvent[62] = new EventRec("event_Journal", new EventFlags(), 0);

            dbScreens = new ScreenRec[20];
            dbScreens[0] = new ScreenRec("gsStartup", "Startup", ScreenStatus.Once);
            dbScreens[1] = new ScreenRec("gsVillage", "Village", ScreenStatus.Once);
            dbScreens[2] = new ScreenRec("gsForest", "Forest", ScreenStatus.Once);
            dbScreens[3] = new ScreenRec("gsJotenheim", "Jotenheim", ScreenStatus.Once);
            dbScreens[4] = new ScreenRec("gsNidavell", "Nidavell", ScreenStatus.Once);
            dbScreens[5] = new ScreenRec("gsNiflheim", "Niflheim", ScreenStatus.Once);
            dbScreens[6] = new ScreenRec("gsCrossroad", "Crossroad", ScreenStatus.Once);
            dbScreens[7] = new ScreenRec("gsDungeon", "Dungeon", ScreenStatus.Once);
            dbScreens[8] = new ScreenRec("gsSea", "Sea", ScreenStatus.Once);
            dbScreens[9] = new ScreenRec("gsAlfheim", "Alfheim", ScreenStatus.Once);
            dbScreens[10] = new ScreenRec("gsMuspelheim", "Muspelheim", ScreenStatus.Once);
            dbScreens[11] = new ScreenRec("gsWell", "Well", ScreenStatus.Once);
            dbScreens[12] = new ScreenRec("gsTemple", "Temple", ScreenStatus.Once);
            dbScreens[13] = new ScreenRec("gsWasteland", "Wasteland", ScreenStatus.Once);
            dbScreens[14] = new ScreenRec("gsDead", "Dead", ScreenStatus.Always);
            dbScreens[15] = new ScreenRec("gsMain", "", ScreenStatus.Always);
            dbScreens[16] = new ScreenRec("gsDefeat", "Defeat", ScreenStatus.Always);
            dbScreens[17] = new ScreenRec("gsVictory", "Victory", ScreenStatus.Always);
            dbScreens[18] = new ScreenRec("gsSwirl", "Swirl", ScreenStatus.Always);
            dbScreens[19] = new ScreenRec("gsNone", "", ScreenStatus.Never);

            dbUserActions = new UserActionRec[25];
            dbUserActions[0] = new UserActionRec("Dialog", RS.rs_Dialog, EventID.event_Dialog, new HotKey(Keys.GK_SPACE, new ShiftStates()), false);
            dbUserActions[1] = new UserActionRec("Menu", RS.rs_Menu, EventID.event_Menu, new HotKey(Keys.GK_Q, new ShiftStates()), false);
            dbUserActions[2] = new UserActionRec("Skills", RS.rs_Skills, EventID.event_Skills, new HotKey(Keys.GK_S, new ShiftStates()), false);
            dbUserActions[3] = new UserActionRec("Knowledges", RS.rs_Knowledges, EventID.event_Knowledges, new HotKey(Keys.GK_K, new ShiftStates()), false);
            dbUserActions[4] = new UserActionRec("Pack", RS.rs_Pack, EventID.event_Pack, new HotKey(Keys.GK_I, new ShiftStates()), false);
            dbUserActions[5] = new UserActionRec("Self", RS.rs_Self, EventID.event_Self, new HotKey(Keys.GK_C, new ShiftStates()), false);
            dbUserActions[6] = new UserActionRec("Map", RS.rs_Map, EventID.event_Map, new HotKey(Keys.GK_M, new ShiftStates()), false);
            dbUserActions[7] = new UserActionRec("Options", RS.rs_Options, EventID.event_Options, new HotKey(Keys.GK_O, new ShiftStates()), false);
            dbUserActions[8] = new UserActionRec("Help", RS.rs_Help, EventID.event_Help, new HotKey(Keys.GK_F1, new ShiftStates()), false);
            dbUserActions[9] = new UserActionRec("MoveN", RS.rs_MoveN, EventID.event_PlayerMoveN, new HotKey(Keys.GK_NUMPAD8, new ShiftStates()), true);
            dbUserActions[10] = new UserActionRec("MoveS", RS.rs_MoveS, EventID.event_PlayerMoveS, new HotKey(Keys.GK_NUMPAD2, new ShiftStates()), true);
            dbUserActions[11] = new UserActionRec("MoveW", RS.rs_MoveW, EventID.event_PlayerMoveW, new HotKey(Keys.GK_NUMPAD4, new ShiftStates()), true);
            dbUserActions[12] = new UserActionRec("MoveE", RS.rs_MoveE, EventID.event_PlayerMoveE, new HotKey(Keys.GK_NUMPAD6, new ShiftStates()), true);
            dbUserActions[13] = new UserActionRec("MoveNW", RS.rs_MoveNW, EventID.event_PlayerMoveNW, new HotKey(Keys.GK_NUMPAD7, new ShiftStates()), true);
            dbUserActions[14] = new UserActionRec("MoveNE", RS.rs_MoveNE, EventID.event_PlayerMoveNE, new HotKey(Keys.GK_NUMPAD9, new ShiftStates()), true);
            dbUserActions[15] = new UserActionRec("MoveSW", RS.rs_MoveSW, EventID.event_PlayerMoveSW, new HotKey(Keys.GK_NUMPAD1, new ShiftStates()), true);
            dbUserActions[16] = new UserActionRec("MoveSE", RS.rs_MoveSE, EventID.event_PlayerMoveSE, new HotKey(Keys.GK_NUMPAD3, new ShiftStates()), true);
            dbUserActions[17] = new UserActionRec("Wait", RS.rs_Wait, EventID.event_Wait, new HotKey(Keys.GK_PERIOD, new ShiftStates()), false);
            dbUserActions[18] = new UserActionRec("PickupAll", RS.rs_PickupAll, EventID.event_PickupAll, new HotKey(Keys.GK_COMMA, new ShiftStates()), false);
            dbUserActions[19] = new UserActionRec("Load", RS.rs_Load, EventID.event_Load, new HotKey(Keys.GK_F3, new ShiftStates()), false);
            dbUserActions[20] = new UserActionRec("Save", RS.rs_Save, EventID.event_Save, new HotKey(Keys.GK_F2, new ShiftStates()), false);
            dbUserActions[21] = new UserActionRec("Party", RS.rs_Party, EventID.event_Party, new HotKey(Keys.GK_P, new ShiftStates()), false);
            dbUserActions[22] = new UserActionRec("MoveUp", RS.rs_MoveUp, EventID.event_PlayerMoveUp, new HotKey(Keys.GK_PERIOD, new ShiftStates()), false);
            dbUserActions[23] = new UserActionRec("MoveDown", RS.rs_MoveDown, EventID.event_PlayerMoveDown, new HotKey(Keys.GK_COMMA, new ShiftStates()), false);
            dbUserActions[24] = new UserActionRec("Journal", RS.rs_Journal, EventID.event_Journal, new HotKey(Keys.GK_J, new ShiftStates()), false);

            dbSysCreatures = new SysCreatureRec[7];
            dbSysCreatures[0] = new SysCreatureRec(SysCreature.sc_Viking, RS.rs_Viking, "Viking", ExtRect.Create(64, 125, 138, 209));
            dbSysCreatures[1] = new SysCreatureRec(SysCreature.sc_Woodsman, RS.rs_Woodsman, "Woodsman", ExtRect.Create(257, 125, 331, 209));
            dbSysCreatures[2] = new SysCreatureRec(SysCreature.sc_Sage, RS.rs_Sage, "Sage", ExtRect.Create(451, 125, 525, 209));
            dbSysCreatures[3] = new SysCreatureRec(SysCreature.sc_Alchemist, RS.rs_Alchemist, "Alchemist", ExtRect.Create(64, 274, 138, 358));
            dbSysCreatures[4] = new SysCreatureRec(SysCreature.sc_Blacksmith, RS.rs_Blacksmith, "Blacksmith", ExtRect.Create(257, 274, 331, 358));
            dbSysCreatures[5] = new SysCreatureRec(SysCreature.sc_Conjurer, RS.rs_Conjurer, "Conjurer", ExtRect.Create(451, 274, 525, 358));
            dbSysCreatures[6] = new SysCreatureRec(SysCreature.sc_Merchant, RS.rs_Merchant, "Merchant", ExtRect.Create(0, 0, 0, 0));

            dbItemKinds = new ItemKindRec[26];
            dbItemKinds[0] = new ItemKindRec(RS.rs_IK_Armor, 2, Colors.Teal, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[1] = new ItemKindRec(RS.rs_IK_DeadBody, 10, Colors.Maroon, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[2] = new ItemKindRec(RS.rs_IK_Food, 8, Colors.Yellow, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[3] = new ItemKindRec(RS.rs_IK_Potion, 4, Colors.Yellow, true, 13, "Potion", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[4] = new ItemKindRec(RS.rs_IK_Ring, 5, Colors.Gold, true, 26, "Ring", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[5] = new ItemKindRec(RS.rs_IK_Tool, 9, Colors.Blue, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emHold);
            dbItemKinds[6] = new ItemKindRec(RS.rs_IK_Wand, 7, Colors.Lime, true, 21, "Wand", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[7] = new ItemKindRec(RS.rs_IK_BluntWeapon, 3, Colors.Green, false, 0, "", AbilityID.Ab_BluntWeapon, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[8] = new ItemKindRec(RS.rs_IK_Scroll, 6, Colors.Aqua, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[9] = new ItemKindRec(RS.rs_IK_Coin, 11, Colors.Gold, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[10] = new ItemKindRec(RS.rs_IK_Amulet, 1, Colors.Gold, true, 25, "Amulet", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[11] = new ItemKindRec(RS.rs_IK_ShortBlade, 3, Colors.Green, false, 0, "", AbilityID.Ab_ShortBlades, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[12] = new ItemKindRec(RS.rs_IK_LongBlade, 3, Colors.Green, false, 0, "", AbilityID.Ab_LongBlade, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[13] = new ItemKindRec(RS.rs_IK_Shield, 2, Colors.Teal, false, 0, "", AbilityID.Ab_Parry, ItemFlags.Create(), EquipMode.emHold);
            dbItemKinds[14] = new ItemKindRec(RS.rs_IK_Helmet, 2, Colors.Teal, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[15] = new ItemKindRec(RS.rs_IK_Clothing, 2, Colors.Teal, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[16] = new ItemKindRec(RS.rs_IK_Spear, 3, Colors.Green, false, 0, "", AbilityID.Ab_Spear, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[17] = new ItemKindRec(RS.rs_IK_Axe, 3, Colors.Green, false, 0, "", AbilityID.Ab_Axe, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[18] = new ItemKindRec(RS.rs_IK_Bow, 3, Colors.Green, false, 0, "", AbilityID.Ab_LongBow, ItemFlags.Create(ItemFlags.if_ShootWeapon, ItemFlags.if_TwoHanded), EquipMode.emHold);
            dbItemKinds[19] = new ItemKindRec(RS.rs_IK_CrossBow, 3, Colors.Green, false, 0, "", AbilityID.Ab_CrossBow, ItemFlags.Create(ItemFlags.if_ShootWeapon, ItemFlags.if_TwoHanded), EquipMode.emHold);
            dbItemKinds[20] = new ItemKindRec(RS.rs_IK_HeavyArmor, 2, Colors.Teal, false, 0, "", AbilityID.Ab_HeavyArmor, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[21] = new ItemKindRec(RS.rs_IK_MediumArmor, 2, Colors.Teal, false, 0, "", AbilityID.Ab_MediumArmor, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[22] = new ItemKindRec(RS.rs_IK_LightArmor, 2, Colors.Teal, false, 0, "", AbilityID.Ab_LightArmor, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[23] = new ItemKindRec(RS.rs_IK_MusicalTool, 9, Colors.Blue, false, 0, "", AbilityID.Ab_MusicalAcuity, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[24] = new ItemKindRec(RS.rs_IK_Projectile, 3, Colors.Green, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(ItemFlags.if_Projectile), EquipMode.emWear);
            dbItemKinds[25] = new ItemKindRec(RS.rs_IK_Misc, 9, Colors.Blue, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);

            dbItemStates = new ItemStateRec[3];
            dbItemStates[0] = new ItemStateRec("", RS.rs_Reserved, 1f, 0);
            dbItemStates[1] = new ItemStateRec(" (б)", RS.rs_Blessed, 1.6f, 1);
            dbItemStates[2] = new ItemStateRec(" (п)", RS.rs_Cursed, 0.4f, -1);

            dbItfElements = new ItfElementRec[15];
            dbItfElements[0] = new ItfElementRec("Up.tga", Colors.White);
            dbItfElements[1] = new ItfElementRec("Down.tga", Colors.White);
            dbItfElements[2] = new ItfElementRec("Left.tga", Colors.White);
            dbItfElements[3] = new ItfElementRec("Right.tga", Colors.White);
            dbItfElements[4] = new ItfElementRec("Cursor.tga", Colors.White);
            dbItfElements[5] = new ItfElementRec("Cursor_N.tga", Colors.White);
            dbItfElements[6] = new ItfElementRec("Cursor_S.tga", Colors.White);
            dbItfElements[7] = new ItfElementRec("Cursor_W.tga", Colors.White);
            dbItfElements[8] = new ItfElementRec("Cursor_E.tga", Colors.White);
            dbItfElements[9] = new ItfElementRec("Cursor_NW.tga", Colors.White);
            dbItfElements[10] = new ItfElementRec("Cursor_NE.tga", Colors.White);
            dbItfElements[11] = new ItfElementRec("Cursor_SW.tga", Colors.White);
            dbItfElements[12] = new ItfElementRec("Cursor_SE.tga", Colors.White);
            dbItfElements[13] = new ItfElementRec("FileDelete.tga", Colors.None);
            dbItfElements[14] = new ItfElementRec("FileNum.tga", Colors.None);

            dbMainControls = new MainControlRec[10];
            dbMainControls[0] = new MainControlRec(RS.rs_Reserved, "", EventID.event_Nothing, ExtRect.Create(0, 0, 0, 0));
            dbMainControls[1] = new MainControlRec(RS.rs_Menu, "itf/MainStBtn.tga", EventID.event_Menu, ExtRect.Create(MC_X, MC_Y + BtnTH * 0, MC_X + St_BtnW, MC_Y + BtnHeight + BtnTH * 0));
            dbMainControls[2] = new MainControlRec(RS.rs_Map, "itf/MainStBtn.tga", EventID.event_Map, ExtRect.Create(MC_X + St_BtnW + 4, MC_Y + BtnTH * 0, MC_X + (St_BtnW << 1) + 4, MC_Y + BtnHeight + BtnTH * 0));
            dbMainControls[3] = new MainControlRec(RS.rs_Knowledges, "itf/MainStBtn.tga", EventID.event_Knowledges, ExtRect.Create(MC_X, MC_Y + (BtnTH << 0), MC_X + St_BtnW, MC_Y + BtnHeight + (BtnTH << 0)));
            dbMainControls[4] = new MainControlRec(RS.rs_Skills, "itf/MainStBtn.tga", EventID.event_Skills, ExtRect.Create(MC_X + St_BtnW + 4, MC_Y + (BtnTH << 0), MC_X + (St_BtnW << 1) + 4, MC_Y + BtnHeight + (BtnTH << 0)));
            dbMainControls[5] = new MainControlRec(RS.rs_Self, "itf/MainLgBtn.tga", EventID.event_Self, ExtRect.Create(MC_X, MC_Y + (BtnTH << 1), MC_X + Lg_BtnW, MC_Y + BtnHeight + (BtnTH << 1)));
            dbMainControls[6] = new MainControlRec(RS.rs_Pack, "itf/MainLgBtn.tga", EventID.event_Pack, ExtRect.Create(MC_X, MC_Y + BtnTH * 3, MC_X + Lg_BtnW, MC_Y + BtnHeight + BtnTH * 3));
            dbMainControls[7] = new MainControlRec(RS.rs_Reserved, "", EventID.event_Nothing, ExtRect.Create(640, 73, 792, 80));
            dbMainControls[8] = new MainControlRec(RS.rs_Reserved, "", EventID.event_Nothing, ExtRect.Create(640, 99, 792, 106));
            dbMainControls[9] = new MainControlRec(RS.rs_Reserved, "", EventID.event_Nothing, ExtRect.Create(640, 125, 792, 132));

            dbMaterialKind = new MaterialKindRec[16];
            dbMaterialKind[0] = new MaterialKindRec(RS.rs_Unknown);
            dbMaterialKind[1] = new MaterialKindRec(RS.rs_Mat_Stone);
            dbMaterialKind[2] = new MaterialKindRec(RS.rs_Mat_Metal);
            dbMaterialKind[3] = new MaterialKindRec(RS.rs_Mat_Wood);
            dbMaterialKind[4] = new MaterialKindRec(RS.rs_Mat_Glass);
            dbMaterialKind[5] = new MaterialKindRec(RS.rs_Mat_Bone);
            dbMaterialKind[6] = new MaterialKindRec(RS.rs_Mat_Leather);
            dbMaterialKind[7] = new MaterialKindRec(RS.rs_Mat_Flesh);
            dbMaterialKind[8] = new MaterialKindRec(RS.rs_Mat_Paper);
            dbMaterialKind[9] = new MaterialKindRec(RS.rs_Mat_Cloth);
            dbMaterialKind[10] = new MaterialKindRec(RS.rs_Mat_Liquid);
            dbMaterialKind[11] = new MaterialKindRec(RS.rs_Mat_Fiber);
            dbMaterialKind[12] = new MaterialKindRec(RS.rs_Mat_Steel);
            dbMaterialKind[13] = new MaterialKindRec(RS.rs_Mat_Diamond);
            dbMaterialKind[14] = new MaterialKindRec(RS.rs_Mat_Silver);
            dbMaterialKind[15] = new MaterialKindRec(RS.rs_Mat_Mithril);

            dbRaces = new RaceRec[5];
            dbRaces[0] = new RaceRec(RS.rs_Reserved, RS.rs_Reserved);
            dbRaces[1] = new RaceRec(RS.rs_Race_Human, RS.rs_KL_Race_Humans);
            dbRaces[2] = new RaceRec(RS.rs_Race_Aesir, RS.rs_KL_Race_Aesirs);
            dbRaces[3] = new RaceRec(RS.rs_Race_EvilGod, RS.rs_KL_Race_EvilGods);
            dbRaces[4] = new RaceRec(RS.rs_Race_Daemon, RS.rs_KL_Race_Daemons);

            dbSkills = new SkillRec[72];
            dbSkills[0] = new SkillRec(RS.rs_Reserved, SkillKind.Plain, "", EffectID.eid_None);
            dbSkills[1] = new SkillRec(RS.rs_Alchemy, SkillKind.Plain, "Alchemy", EffectID.eid_First);
            dbSkills[2] = new SkillRec(RS.rs_ArrowMake, SkillKind.Plain, "ArrowMake", EffectID.eid_ArrowMake);
            dbSkills[3] = new SkillRec(RS.rs_Cartography, SkillKind.Plain, "Cartography", EffectID.eid_Cartography);
            dbSkills[4] = new SkillRec(RS.rs_Diagnosis, SkillKind.Plain, "Diagnosis", EffectID.eid_Diagnosis);
            dbSkills[5] = new SkillRec(RS.rs_Embalming, SkillKind.Plain, "Embalming", EffectID.eid_Embalming);
            dbSkills[6] = new SkillRec(RS.rs_Fennling, SkillKind.Plain, "Fennling", EffectID.eid_Fennling);
            dbSkills[7] = new SkillRec(RS.rs_GolemCreation, SkillKind.Plain, "GolemCreation", EffectID.eid_GolemCreation);
            dbSkills[8] = new SkillRec(RS.rs_Husbandry, SkillKind.Plain, "Husbandry", EffectID.eid_Husbandry);
            dbSkills[9] = new SkillRec(RS.rs_Ironworking, SkillKind.Plain, "Ironworking", EffectID.eid_First);
            dbSkills[10] = new SkillRec(RS.rs_Precognition, SkillKind.Plain, "Precognition", EffectID.eid_Precognition);
            dbSkills[11] = new SkillRec(RS.rs_Relocation, SkillKind.Plain, "Relocation", EffectID.eid_Relocation);
            dbSkills[12] = new SkillRec(RS.rs_SlaveUse, SkillKind.Plain, "SlaveUse", EffectID.eid_SlaveUse);
            dbSkills[13] = new SkillRec(RS.rs_Taming, SkillKind.Plain, "Taming", EffectID.eid_Taming);
            dbSkills[14] = new SkillRec(RS.rs_Ventriloquism, SkillKind.Plain, "Ventriloquism", EffectID.eid_Ventriloquism);
            dbSkills[15] = new SkillRec(RS.rs_Writing, SkillKind.Plain, "Writing", EffectID.eid_Writing);
            dbSkills[16] = new SkillRec(RS.rs_Animation, SkillKind.Plain, "Animation", EffectID.eid_Animation);
            dbSkills[17] = new SkillRec(RS.rs_DimensionTravel, SkillKind.Plain, "DimensionTravel", EffectID.eid_SwitchDimension);
            dbSkills[18] = new SkillRec(RS.rs_FireVision, SkillKind.Plain, "FireVision", EffectID.eid_FireVision);
            dbSkills[19] = new SkillRec(RS.rs_HeatRadiation, SkillKind.Plain, "HeatRadiation", EffectID.eid_Heat);
            dbSkills[20] = new SkillRec(RS.rs_MindControl, SkillKind.Plain, "MindControl", EffectID.eid_MindControl);
            dbSkills[21] = new SkillRec(RS.rs_PsiBlast, SkillKind.Plain, "PsiBlast", EffectID.eid_PsiBlast);
            dbSkills[22] = new SkillRec(RS.rs_Terraforming, SkillKind.Plain, "Terraforming", EffectID.eid_Geology);
            dbSkills[23] = new SkillRec(RS.rs_Spellcasting, SkillKind.Meta, "Spellcasting", EffectID.eid_First);
            dbSkills[24] = new SkillRec(RS.rs_InnatePower, SkillKind.Meta, "InnatePower", EffectID.eid_First);
            dbSkills[25] = new SkillRec(RS.rs_Basilisk_Poison, SkillKind.InnatePower, "", EffectID.eid_Basilisk_Poison);
            dbSkills[26] = new SkillRec(RS.rs_Borgonvile_Cloud, SkillKind.InnatePower, "", EffectID.eid_Borgonvile_Cloud);
            dbSkills[27] = new SkillRec(RS.rs_Breleor_Tendril, SkillKind.InnatePower, "", EffectID.eid_Breleor_Tendril);
            dbSkills[28] = new SkillRec(RS.rs_Ellegiant_Throw, SkillKind.InnatePower, "", EffectID.eid_Ellegiant_Throw);
            dbSkills[29] = new SkillRec(RS.rs_Ellegiant_Crush, SkillKind.InnatePower, "", EffectID.eid_Ellegiant_Crush);
            dbSkills[30] = new SkillRec(RS.rs_Firedragon_Breath, SkillKind.InnatePower, "", EffectID.eid_Firedragon_Breath);
            dbSkills[31] = new SkillRec(RS.rs_Firegiant_Touch, SkillKind.InnatePower, "", EffectID.eid_Firegiant_Touch);
            dbSkills[32] = new SkillRec(RS.rs_Fyleisch_Cloud, SkillKind.InnatePower, "", EffectID.eid_Fyleisch_Cloud);
            dbSkills[33] = new SkillRec(RS.rs_Gasball_Explosion, SkillKind.InnatePower, "", EffectID.eid_Gasball_Explosion);
            dbSkills[34] = new SkillRec(RS.rs_Giantsquid_Crush, SkillKind.InnatePower, "", EffectID.eid_Giantsquid_Crush);
            dbSkills[35] = new SkillRec(RS.rs_Glard_Poison, SkillKind.InnatePower, "", EffectID.eid_Glard_Poison);
            dbSkills[36] = new SkillRec(RS.rs_Hatchetfish_Teeth, SkillKind.InnatePower, "", EffectID.eid_Hatchetfish_Teeth);
            dbSkills[37] = new SkillRec(RS.rs_Heldragon_Cloud, SkillKind.InnatePower, "", EffectID.eid_Heldragon_Cloud);
            dbSkills[38] = new SkillRec(RS.rs_Hillgiant_Crush, SkillKind.InnatePower, "", EffectID.eid_Hillgiant_Crush);
            dbSkills[39] = new SkillRec(RS.rs_Icedragon_Breath, SkillKind.InnatePower, "", EffectID.eid_Icedragon_Breath);
            dbSkills[40] = new SkillRec(RS.rs_Icesphere_Blast, SkillKind.InnatePower, "", EffectID.eid_Icesphere_Blast);
            dbSkills[41] = new SkillRec(RS.rs_Jagredin_Burning, SkillKind.InnatePower, "", EffectID.eid_Jagredin_Burning);
            dbSkills[42] = new SkillRec(RS.rs_Knellbird_Gaze, SkillKind.InnatePower, "", EffectID.eid_Knellbird_Gaze);
            dbSkills[43] = new SkillRec(RS.rs_Kobold_Throw, SkillKind.InnatePower, "", EffectID.eid_Kobold_Throw);
            dbSkills[44] = new SkillRec(RS.rs_Lowerdwarf_Throw, SkillKind.InnatePower, "", EffectID.eid_Lowerdwarf_Throw);
            dbSkills[45] = new SkillRec(RS.rs_Moleman_Debris, SkillKind.InnatePower, "", EffectID.eid_Moleman_Debris);
            dbSkills[46] = new SkillRec(RS.rs_Phantomasp_Poison, SkillKind.InnatePower, "", EffectID.eid_Phantomasp_Poison);
            dbSkills[47] = new SkillRec(RS.rs_Pyrtaath_Throttle, SkillKind.InnatePower, "", EffectID.eid_Pyrtaath_Throttle);
            dbSkills[48] = new SkillRec(RS.rs_Ramapith_FireTouch, SkillKind.InnatePower, "", EffectID.eid_Ramapith_FireTouch);
            dbSkills[49] = new SkillRec(RS.rs_Sandiff_Acid, SkillKind.InnatePower, "", EffectID.eid_Sandiff_Acid);
            dbSkills[50] = new SkillRec(RS.rs_Scyld_Breath, SkillKind.InnatePower, "", EffectID.eid_Scyld_Breath);
            dbSkills[51] = new SkillRec(RS.rs_Scyld_Ray, SkillKind.InnatePower, "", EffectID.eid_Scyld_Ray);
            dbSkills[52] = new SkillRec(RS.rs_Scyld_ShockWave, SkillKind.InnatePower, "", EffectID.eid_Scyld_ShockWave);
            dbSkills[53] = new SkillRec(RS.rs_Sentinel_Gaze, SkillKind.InnatePower, "", EffectID.eid_Sentinel_Gaze);
            dbSkills[54] = new SkillRec(RS.rs_Serpent_Poison, SkillKind.InnatePower, "", EffectID.eid_Serpent_Poison);
            dbSkills[55] = new SkillRec(RS.rs_Shadow_Touch, SkillKind.InnatePower, "", EffectID.eid_Shadow_Touch);
            dbSkills[56] = new SkillRec(RS.rs_Slinn_Gout, SkillKind.InnatePower, "", EffectID.eid_Slinn_Gout);
            dbSkills[57] = new SkillRec(RS.rs_Spirit_Touch, SkillKind.InnatePower, "", EffectID.eid_Spirit_Touch);
            dbSkills[58] = new SkillRec(RS.rs_Stunworm_Stun, SkillKind.InnatePower, "", EffectID.eid_Stunworm_Stun);
            dbSkills[59] = new SkillRec(RS.rs_Terrain_Burning, SkillKind.InnatePower, "", EffectID.eid_Terrain_Burning);
            dbSkills[60] = new SkillRec(RS.rs_Warrior_Throw, SkillKind.InnatePower, "", EffectID.eid_Warrior_Throw);
            dbSkills[61] = new SkillRec(RS.rs_Watcher_Gaze, SkillKind.InnatePower, "", EffectID.eid_Watcher_Gaze);
            dbSkills[62] = new SkillRec(RS.rs_Weirdfume_Acid, SkillKind.InnatePower, "", EffectID.eid_WeirdFume_Acid);
            dbSkills[63] = new SkillRec(RS.rs_Womera_Throw, SkillKind.InnatePower, "", EffectID.eid_Womera_Throw);
            dbSkills[64] = new SkillRec(RS.rs_Wooddwarf_Throw, SkillKind.InnatePower, "", EffectID.eid_Wooddwarf_Throw);
            dbSkills[65] = new SkillRec(RS.rs_Wyvern_Breath, SkillKind.InnatePower, "", EffectID.eid_Wyvern_Breath);
            dbSkills[66] = new SkillRec(RS.rs_Zardon_PsiBlast, SkillKind.InnatePower, "", EffectID.eid_Zardon_PsiBlast);
            dbSkills[67] = new SkillRec(RS.rs_Dig, SkillKind.Plain, "Dig", EffectID.eid_Dig);
            dbSkills[68] = new SkillRec(RS.rs_Ull_Gaze, SkillKind.InnatePower, "", EffectID.eid_Ull_Gaze);
            dbSkills[69] = new SkillRec(RS.rs_Prayer, SkillKind.Plain, "Prayer", EffectID.eid_Prayer);
            dbSkills[70] = new SkillRec(RS.rs_Sacrifice, SkillKind.Plain, "Sacrifice", EffectID.eid_Sacrifice);
            dbSkills[71] = new SkillRec(RS.rs_RunicDivination, SkillKind.Plain, "Divination", EffectID.eid_RunicDivination);

            dbSymbols = new SymbolRec[84];
            dbSymbols[0] = new SymbolRec("_None", 0, 'x', Colors.White);
            dbSymbols[1] = new SymbolRec("_Cursor", 0, 'x', Colors.White);
            dbSymbols[2] = new SymbolRec("_Grass", 0, '.', Colors.Green);
            dbSymbols[3] = new SymbolRec("_Tree", 0, 'T', Colors.Lime);
            dbSymbols[4] = new SymbolRec("_Mountain", 0, '^', Colors.Olive);
            dbSymbols[5] = new SymbolRec("_Player", 0, '@', Colors.White);
            dbSymbols[6] = new SymbolRec("_Armor", 0, '[', Colors.White);
            dbSymbols[7] = new SymbolRec("_DeadBody", 0, '+', Colors.White);
            dbSymbols[8] = new SymbolRec("_Food", 0, '%', Colors.White);
            dbSymbols[9] = new SymbolRec("_Potion", 0, '%', Colors.White);
            dbSymbols[10] = new SymbolRec("_Ring", 0, '*', Colors.Yellow);
            dbSymbols[11] = new SymbolRec("_Tool", 0, '=', Colors.White);
            dbSymbols[12] = new SymbolRec("_Trap", 0, ',', Colors.Maroon);
            dbSymbols[13] = new SymbolRec("_Wand", 0, '|', Colors.White);
            dbSymbols[14] = new SymbolRec("_Weapon", 0, '(', Colors.White);
            dbSymbols[15] = new SymbolRec("_Scroll", 0, '?', Colors.White);
            dbSymbols[16] = new SymbolRec("_Coin", 0, '$', Colors.Yellow);
            dbSymbols[17] = new SymbolRec("_Floor", 0, '.', Colors.Gray);
            dbSymbols[18] = new SymbolRec("_WallN", 0, '#', Colors.Gray);
            dbSymbols[19] = new SymbolRec("_WallS", 0, '#', Colors.Gray);
            dbSymbols[20] = new SymbolRec("_WallW", 0, '#', Colors.Gray);
            dbSymbols[21] = new SymbolRec("_WallE", 0, '#', Colors.Gray);
            dbSymbols[22] = new SymbolRec("_WallNW", 0, '#', Colors.Gray);
            dbSymbols[23] = new SymbolRec("_WallNE", 0, '#', Colors.Gray);
            dbSymbols[24] = new SymbolRec("_WallSW", 0, '#', Colors.Gray);
            dbSymbols[25] = new SymbolRec("_WallSE", 0, '#', Colors.Gray);
            dbSymbols[26] = new SymbolRec("_Enemy", 0, 'x', Colors.White);
            dbSymbols[27] = new SymbolRec("_Left", 0, 'x', Colors.White);
            dbSymbols[28] = new SymbolRec("_Up", 0, 'x', Colors.White);
            dbSymbols[29] = new SymbolRec("_Right", 0, 'x', Colors.White);
            dbSymbols[30] = new SymbolRec("_Down", 0, 'x', Colors.White);
            dbSymbols[31] = new SymbolRec("_Vortex", 0, '0', Colors.White);
            dbSymbols[32] = new SymbolRec("_Lava", 0, '~', Colors.Maroon);
            dbSymbols[33] = new SymbolRec("_Mud", 0, '~', Colors.Purple);
            dbSymbols[34] = new SymbolRec("_Water", 0, '~', Colors.Blue);
            dbSymbols[35] = new SymbolRec("_Ally", 0, 'x', Colors.Yellow);
            dbSymbols[36] = new SymbolRec("_StairsDown", 0, '>', Colors.Gray);
            dbSymbols[37] = new SymbolRec("_StairsUp", 0, '<', Colors.Gray);
            dbSymbols[38] = new SymbolRec("_Quicksand", 0, '~', Colors.Silver);
            dbSymbols[39] = new SymbolRec("_Space", 0, '.', Colors.Navy);
            dbSymbols[40] = new SymbolRec("_Amulet", 0, '*', Colors.Yellow);
            dbSymbols[41] = new SymbolRec("_cr_a", 0, 'x', Colors.White);
            dbSymbols[42] = new SymbolRec("_cr_b", 0, 'x', Colors.White);
            dbSymbols[43] = new SymbolRec("_cr_ba", 0, 'x', Colors.White);
            dbSymbols[44] = new SymbolRec("_cr_g", 0, 'x', Colors.White);
            dbSymbols[45] = new SymbolRec("_cr_gk", 0, 'x', Colors.White);
            dbSymbols[46] = new SymbolRec("_cr_k", 0, 'x', Colors.White);
            dbSymbols[47] = new SymbolRec("_cr_l", 0, 'x', Colors.White);
            dbSymbols[48] = new SymbolRec("_cr_r", 0, 'x', Colors.White);
            dbSymbols[49] = new SymbolRec("_cr_w", 0, 'x', Colors.White);
            dbSymbols[50] = new SymbolRec("_cr_wl", 0, 'x', Colors.White);
            dbSymbols[51] = new SymbolRec("_cr_y", 0, 'x', Colors.White);
            dbSymbols[52] = new SymbolRec("_cr_yr", 0, 'x', Colors.White);
            dbSymbols[53] = new SymbolRec("_cr_disk", 0, 'x', Colors.White);
            dbSymbols[54] = new SymbolRec("_Fog", 0, '°', Colors.Aqua);
            dbSymbols[55] = new SymbolRec("_Hole", 0, 'o', Colors.White);
            dbSymbols[56] = new SymbolRec("_Bifrost", 150, 'ъ', Colors.White);
            dbSymbols[57] = new SymbolRec("_DeadTree", 0, 'T', Colors.Silver);
            dbSymbols[58] = new SymbolRec("_Well", 0, 'o', Colors.Silver);
            dbSymbols[59] = new SymbolRec("_Stalagmite", 0, '!', Colors.Silver);
            dbSymbols[60] = new SymbolRec("_CaveFloor", 0, '.', Colors.Olive);
            dbSymbols[61] = new SymbolRec("_CaveWall", 0, '#', Colors.Olive);
            dbSymbols[62] = new SymbolRec("_SmallPit", 0, 'o', Colors.Silver);
            dbSymbols[63] = new SymbolRec("_Rubble", 0, '~', Colors.Gray);
            dbSymbols[64] = new SymbolRec("_Liquid", 0, '~', Colors.Aqua);
            dbSymbols[65] = new SymbolRec("_Vulcan", 0, '^', Colors.Maroon);
            dbSymbols[66] = new SymbolRec("_Ground", 0, '.', Colors.Gray);
            dbSymbols[67] = new SymbolRec("_Floor", 0, 'ъ', Colors.Gray);
            dbSymbols[68] = new SymbolRec("_Floor", 0, 'ъ', Colors.Gray);
            dbSymbols[69] = new SymbolRec("_Floor", 0, 'ъ', Colors.Gray);
            dbSymbols[70] = new SymbolRec("_Floor", 0, 'ъ', Colors.Gray);
            dbSymbols[71] = new SymbolRec("_WallN", 0, 'x', Colors.Gray);
            dbSymbols[72] = new SymbolRec("_WallS", 0, 'x', Colors.Gray);
            dbSymbols[73] = new SymbolRec("_WallW", 0, 'x', Colors.Gray);
            dbSymbols[74] = new SymbolRec("_WallE", 0, 'x', Colors.Gray);
            dbSymbols[75] = new SymbolRec("_AllyHuman", 0, '@', Colors.Lime);
            dbSymbols[76] = new SymbolRec("_EnemyHuman", 0, '@', Colors.Red);
            dbSymbols[77] = new SymbolRec("_Ivy", 14, 'E', Colors.Red);
            dbSymbols[78] = new SymbolRec("_Snake", 24, 's', Colors.Red);
            dbSymbols[79] = new SymbolRec("_VortexStrange", 0, '0', Colors.White);
            dbSymbols[80] = new SymbolRec("_StoningHuman", 0, '.', Colors.White);
            dbSymbols[81] = new SymbolRec("_Ting", 64, '.', Colors.White);
            dbSymbols[82] = new SymbolRec("_Road", 15, '.', Colors.White);
            dbSymbols[83] = new SymbolRec("_IronTree", 0, '.', Colors.White);

            dbDialogServices = new DialogServiceRec[4];
            dbDialogServices[0] = new DialogServiceRec(RS.rs_Teach, RS.rs_Teach_Question);
            dbDialogServices[1] = new DialogServiceRec(RS.rs_Trade, RS.rs_Trade_Question);
            dbDialogServices[2] = new DialogServiceRec(RS.rs_Exchange, RS.rs_Exchange_Question);
            dbDialogServices[3] = new DialogServiceRec(RS.rs_Recruit, RS.rs_Recruit_Question);

            dbEffectTarget = new EffectTargetRec[9];
            dbEffectTarget[0] = new EffectTargetRec(RS.rs_Reserved, RS.rs_Reserved);
            dbEffectTarget[1] = new EffectTargetRec(RS.rs_PlaceNearNeed, RS.rs_PlaceInvalid);
            dbEffectTarget[2] = new EffectTargetRec(RS.rs_PlaceFarNeed, RS.rs_PlaceInvalid);
            dbEffectTarget[3] = new EffectTargetRec(RS.rs_DirectionNeed, RS.rs_DirectionInvalid);
            dbEffectTarget[4] = new EffectTargetRec(RS.rs_ItemNeed, RS.rs_ItemInvalid);
            dbEffectTarget[5] = new EffectTargetRec(RS.rs_CreatureNeed, RS.rs_CreatureInvalid);
            dbEffectTarget[6] = new EffectTargetRec(RS.rs_AreaNeed, RS.rs_Reserved);
            dbEffectTarget[7] = new EffectTargetRec(RS.rs_TravelWhere, RS.rs_LandInvalid);
            dbEffectTarget[8] = new EffectTargetRec(RS.rs_Reserved, RS.rs_Reserved);
        }

        public static CreatureSex GetSexBySign(string sign)
        {
            foreach (SexRec rec in dbSex) {
                if (rec.Sign.Equals(sign)) {
                    return rec.Value;
                }
            }
            return CreatureSex.csNone;
        }

        public static string MorphCompNoun(string aComp, Case c, Number q, CreatureSex sex, bool animate, bool endingstressed)
        {
            if (string.IsNullOrEmpty(aComp)) {
                return "";
            }

            try {
                Gender gender = StaticData.GenderBySex[(int)sex];
                string result = "";

                int cnt = AuxUtils.GetTokensCount(aComp, " ");
                for (int i = 1; i <= cnt; i++) {
                    if (i > 1) {
                        result += " ";
                    }
                    string tok = AuxUtils.GetToken(aComp, " ", i);
                    result += Grammar.morphNoun(tok, c, q, gender, animate, endingstressed);
                }

                return result;
            } catch (Exception ex) {
                Logger.Write("StaticData.morphCompNoun(): " + ex.Message);
                throw ex;
            }
        }

        public static string GetVerbRes(string wordRes, Gender gender, int voice)
        {
            try {
                string result;

                string word = AuxUtils.GetToken(wordRes, "[]", 1);
                string sfx = AuxUtils.GetToken(wordRes, "[]", 2);

                int sf_idx = (voice - 1) * 3 + ((int)gender);
                string sf = AuxUtils.GetToken(sfx, ",", sf_idx);
                if (sf.CompareTo("") == 0) {
                    result = word;
                } else {
                    if (sf[0] != '-') {
                        result = sf;
                    } else {
                        sf = sf.Substring(1);
                        result = word + sf;
                    }
                }

                return result;
            } catch (Exception ex) {
                Logger.Write("StaticData.getVerbRes(): " + ex.Message);
                throw ex;
            }
        }

        public static string GetVerbRes(int wordRes, CreatureSex sex, int voice)
        {
            return StaticData.GetVerbRes(Locale.GetStr(wordRes), StaticData.GenderBySex[(int)sex], voice);
        }
    }
}
