/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
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
using NWR.Core.Types;
using NWR.Database;
using NWR.Effects;
using ZRLib.Engine;
using ZRLib.Core;
using ZRLib.Grammar;

namespace NWR.Core
{
    using ItemKinds = EnumSet<ItemKind>;

    public static class StaticData
    {
        public static readonly Encoding DefEncoding = Encoding.GetEncoding(1251);

        public const string Rs_GameName = "NorseWorld: Ragnarok";
        public const string Rs_GameVersion = "v0.11.0";
        public const string Rs_GameDevTime = "2002-2008";
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
            dbAbilities[0] = AbilityRec.Create(RS.rs_Unknown);
            dbAbilities[1] = AbilityRec.Create(RS.rs_Swimming);
            dbAbilities[2] = AbilityRec.Create(RS.rs_Identification);
            dbAbilities[3] = AbilityRec.Create(RS.rs_Telepathy);
            dbAbilities[4] = AbilityRec.Create(RS.rs_SixthSense);
            dbAbilities[5] = AbilityRec.Create(RS.rs_MusicalAcuity);
            dbAbilities[6] = AbilityRec.Create(RS.rs_ShortBlades);
            dbAbilities[7] = AbilityRec.Create(RS.rs_LongBow);
            dbAbilities[8] = AbilityRec.Create(RS.rs_CrossBow);
            dbAbilities[9] = AbilityRec.Create(RS.rs_Levitation);
            dbAbilities[10] = AbilityRec.Create(RS.rs_Marksman);
            dbAbilities[11] = AbilityRec.Create(RS.rs_Spear);
            dbAbilities[12] = AbilityRec.Create(RS.rs_RayReflect);
            dbAbilities[13] = AbilityRec.Create(RS.rs_RayAbsorb);
            dbAbilities[14] = AbilityRec.Create(RS.rs_Resist_Cold);
            dbAbilities[15] = AbilityRec.Create(RS.rs_Resist_Heat);
            dbAbilities[16] = AbilityRec.Create(RS.rs_Resist_Acid);
            dbAbilities[17] = AbilityRec.Create(RS.rs_Resist_Poison);
            dbAbilities[18] = AbilityRec.Create(RS.rs_Resist_Ray);
            dbAbilities[19] = AbilityRec.Create(RS.rs_Resist_Teleport);
            dbAbilities[20] = AbilityRec.Create(RS.rs_Resist_DeathRay);
            dbAbilities[21] = AbilityRec.Create(RS.rs_Resist_Petrification);
            dbAbilities[22] = AbilityRec.Create(RS.rs_Resist_Psionic);
            dbAbilities[23] = AbilityRec.Create(RS.rs_Resist_DisplacementRay);
            dbAbilities[24] = AbilityRec.Create(RS.rs_Resist_HasteningRay);
            dbAbilities[25] = AbilityRec.Create(RS.rs_Resist_SleepRay);
            dbAbilities[26] = AbilityRec.Create(RS.rs_Regeneration);
            dbAbilities[27] = AbilityRec.Create(RS.rs_ThirdSight);
            dbAbilities[28] = AbilityRec.Create(RS.rs_Axe);
            dbAbilities[29] = AbilityRec.Create(RS.rs_Parry);
            dbAbilities[30] = AbilityRec.Create(RS.rs_BluntWeapon);
            dbAbilities[31] = AbilityRec.Create(RS.rs_HandToHand);
            dbAbilities[32] = AbilityRec.Create(RS.rs_HeavyArmor);
            dbAbilities[33] = AbilityRec.Create(RS.rs_MediumArmor);
            dbAbilities[34] = AbilityRec.Create(RS.rs_LightArmor);
            dbAbilities[35] = AbilityRec.Create(RS.rs_LongBlade);

            ActionByAttackKind = new CreatureAction[] {
                CreatureAction.caAttackMelee,
                CreatureAction.caAttackShoot,
                CreatureAction.caAttackThrow
            };

            dbBuildings = new BuildingRec[8];
            dbBuildings[0] = BuildingRec.Create(RS.rs_Reserved, SysCreature.sc_Viking, ItemKinds.Create(), 0, 0, 0, 0, 0);
            dbBuildings[1] = BuildingRec.Create(RS.rs_House, SysCreature.sc_Viking, ItemKinds.Create(), 1, 1, 2, 4, 5);
            dbBuildings[2] = BuildingRec.Create(RS.rs_MerchantShop, SysCreature.sc_Merchant, ItemKinds.Create(ItemKind.ik_Armor, ItemKind.ik_Food, ItemKind.ik_Potion, ItemKind.ik_Ring, ItemKind.ik_Tool, ItemKind.ik_Wand, ItemKind.ik_BluntWeapon, ItemKind.ik_Scroll, ItemKind.ik_Amulet, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Clothing, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor, ItemKind.ik_MusicalTool, ItemKind.ik_Projectile, ItemKind.ik_Misc), 1, 1, 2, 4, 6);
            dbBuildings[3] = BuildingRec.Create(RS.rs_Smithy, SysCreature.sc_Blacksmith, ItemKinds.Create(ItemKind.ik_Armor, ItemKind.ik_BluntWeapon, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor, ItemKind.ik_Projectile), 1, 1, 2, 4, 5);
            dbBuildings[4] = BuildingRec.Create(RS.rs_AlchemistShop, SysCreature.sc_Alchemist, ItemKinds.Create(ItemKind.ik_Potion), 1, 1, 2, 4, 5);
            dbBuildings[5] = BuildingRec.Create(RS.rs_ConjurerShop, SysCreature.sc_Conjurer, ItemKinds.Create(ItemKind.ik_Ring, ItemKind.ik_Wand, ItemKind.ik_Amulet), 1, 1, 2, 4, 5);
            dbBuildings[6] = BuildingRec.Create(RS.rs_SageShop, SysCreature.sc_Sage, ItemKinds.Create(ItemKind.ik_Scroll), 1, 1, 2, 4, 5);
            dbBuildings[7] = BuildingRec.Create(RS.rs_WoodsmanShop, SysCreature.sc_Woodsman, ItemKinds.Create(ItemKind.ik_Food, ItemKind.ik_ShortBlade, ItemKind.ik_Spear, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_Projectile), 1, 1, 2, 4, 5);

            dbCreatureActions = new ActionRec[16];
            dbCreatureActions[0] = ActionRec.Create(1f, new ActionFlags());
            dbCreatureActions[1] = ActionRec.Create(1f, new ActionFlags());
            dbCreatureActions[2] = ActionRec.Create(1f, new ActionFlags());
            dbCreatureActions[3] = ActionRec.Create(1f, new ActionFlags(ActionFlags.afWithItem, ActionFlags.afCheckAbility));
            dbCreatureActions[4] = ActionRec.Create(1f, new ActionFlags(ActionFlags.afWithItem, ActionFlags.afCheckAbility));
            dbCreatureActions[5] = ActionRec.Create(1f, new ActionFlags(ActionFlags.afWithItem, ActionFlags.afCheckAbility));
            dbCreatureActions[6] = ActionRec.Create(1f, new ActionFlags(ActionFlags.afWithItem, ActionFlags.afCheckAbility));
            dbCreatureActions[7] = ActionRec.Create(1f, new ActionFlags(ActionFlags.afWithItem));
            dbCreatureActions[8] = ActionRec.Create(1f, new ActionFlags(ActionFlags.afWithItem));
            dbCreatureActions[9] = ActionRec.Create(1f, new ActionFlags(ActionFlags.afWithItem));
            dbCreatureActions[10] = ActionRec.Create(1f, new ActionFlags(ActionFlags.afWithItem));
            dbCreatureActions[11] = ActionRec.Create(1f, new ActionFlags());
            dbCreatureActions[12] = ActionRec.Create(1f, new ActionFlags(ActionFlags.afWithItem));
            dbCreatureActions[13] = ActionRec.Create(1f, new ActionFlags());
            dbCreatureActions[14] = ActionRec.Create(1f, new ActionFlags());
            dbCreatureActions[15] = ActionRec.Create(1f, new ActionFlags());

            dbEvent = new EventRec[63];
            dbEvent[0] = EventRec.Create("event_Nothing", new EventFlags(), 0);
            dbEvent[1] = EventRec.Create("event_Startup", new EventFlags(), 0);
            dbEvent[2] = EventRec.Create("event_Map", new EventFlags(), 0);
            dbEvent[3] = EventRec.Create("event_About", new EventFlags(), 0);
            dbEvent[4] = EventRec.Create("event_Self", new EventFlags(), 0);
            dbEvent[5] = EventRec.Create("event_Wait", new EventFlags(), 0);
            dbEvent[6] = EventRec.Create("event_PickupAll", new EventFlags(), 0);
            dbEvent[7] = EventRec.Create("event_DoorClose", new EventFlags(), 0);
            dbEvent[8] = EventRec.Create("event_DoorOpen", new EventFlags(), 0);
            dbEvent[9] = EventRec.Create("event_Help", new EventFlags(), 0);
            dbEvent[10] = EventRec.Create("event_AutoStart", new EventFlags(), 0);
            dbEvent[11] = EventRec.Create("event_AutoStop", new EventFlags(), 0);
            dbEvent[12] = EventRec.Create("event_LandEnter", new EventFlags(EventFlags.efInJournal), 0);
            dbEvent[13] = EventRec.Create("event_Hit", new EventFlags(), 0);
            dbEvent[14] = EventRec.Create("event_Miss", new EventFlags(), 0);
            dbEvent[15] = EventRec.Create("event_Intro", new EventFlags(EventFlags.efInJournal), 0);
            dbEvent[16] = EventRec.Create("event_Knowledges", new EventFlags(), 0);
            dbEvent[17] = EventRec.Create("event_Skills", new EventFlags(), 0);
            dbEvent[18] = EventRec.Create("event_Menu", new EventFlags(), 0);
            dbEvent[19] = EventRec.Create("event_Attack", new EventFlags(EventFlags.efInQueue, EventFlags.efInJournal), 100);
            dbEvent[20] = EventRec.Create("event_Killed", new EventFlags(EventFlags.efInJournal), 0);
            dbEvent[21] = EventRec.Create("event_Move", new EventFlags(), 0);
            dbEvent[22] = EventRec.Create("event_Shot", new EventFlags(), 0);
            dbEvent[23] = EventRec.Create("event_Slay", new EventFlags(), 0);
            dbEvent[24] = EventRec.Create("event_Wounded", new EventFlags(EventFlags.efInJournal), 0);
            dbEvent[25] = EventRec.Create("event_Trap", new EventFlags(), 0);
            dbEvent[26] = EventRec.Create("event_Trade", new EventFlags(), 0);
            dbEvent[27] = EventRec.Create("event_LevelUp", new EventFlags(), 0);
            dbEvent[28] = EventRec.Create("event_Pack", new EventFlags(), 0);
            dbEvent[29] = EventRec.Create("event_DialogBegin", new EventFlags(), 0);
            dbEvent[30] = EventRec.Create("event_DialogEnd", new EventFlags(), 0);
            dbEvent[31] = EventRec.Create("event_DialogRemark", new EventFlags(), 0);
            dbEvent[32] = EventRec.Create("event_Options", new EventFlags(), 0);
            dbEvent[33] = EventRec.Create("event_Throw", new EventFlags(), 0);
            dbEvent[34] = EventRec.Create("event_Defeat", new EventFlags(), 0);
            dbEvent[35] = EventRec.Create("event_Dialog", new EventFlags(), 0);
            dbEvent[36] = EventRec.Create("event_LookAt", new EventFlags(), 0);
            dbEvent[37] = EventRec.Create("event_Quit", new EventFlags(), 0);
            dbEvent[38] = EventRec.Create("event_Save", new EventFlags(), 0);
            dbEvent[39] = EventRec.Create("event_Load", new EventFlags(), 0);
            dbEvent[40] = EventRec.Create("event_New", new EventFlags(), 0);
            dbEvent[41] = EventRec.Create("event_Dead", new EventFlags(), 0);
            dbEvent[42] = EventRec.Create("event_Party", new EventFlags(), 0);
            dbEvent[43] = EventRec.Create("event_Victory", new EventFlags(), 0);
            dbEvent[44] = EventRec.Create("event_EffectSound", new EventFlags(), 0);
            dbEvent[45] = EventRec.Create("event_ItemDrop", new EventFlags(), 0);
            dbEvent[46] = EventRec.Create("event_ItemPickup", new EventFlags(), 0);
            dbEvent[47] = EventRec.Create("event_ItemRemove", new EventFlags(), 0);
            dbEvent[48] = EventRec.Create("event_ItemWear", new EventFlags(), 0);
            dbEvent[49] = EventRec.Create("event_ItemBreak", new EventFlags(), 0);
            dbEvent[50] = EventRec.Create("event_ItemUse", new EventFlags(), 0);
            dbEvent[51] = EventRec.Create("event_ItemMix", new EventFlags(), 0);
            dbEvent[52] = EventRec.Create("event_PlayerMoveN", new EventFlags(), 0);
            dbEvent[53] = EventRec.Create("event_PlayerMoveS", new EventFlags(), 0);
            dbEvent[54] = EventRec.Create("event_PlayerMoveW", new EventFlags(), 0);
            dbEvent[55] = EventRec.Create("event_PlayerMoveE", new EventFlags(), 0);
            dbEvent[56] = EventRec.Create("event_PlayerMoveNW", new EventFlags(), 0);
            dbEvent[57] = EventRec.Create("event_PlayerMoveNE", new EventFlags(), 0);
            dbEvent[58] = EventRec.Create("event_PlayerMoveSW", new EventFlags(), 0);
            dbEvent[59] = EventRec.Create("event_PlayerMoveSE", new EventFlags(), 0);
            dbEvent[60] = EventRec.Create("event_PlayerMoveUp", new EventFlags(), 0);
            dbEvent[61] = EventRec.Create("event_PlayerMoveDown", new EventFlags(), 0);
            dbEvent[62] = EventRec.Create("event_Journal", new EventFlags(), 0);

            dbScreens = new ScreenRec[20];
            dbScreens[0] = ScreenRec.Create("gsStartup", "Startup", ScreenStatus.ssOnce);
            dbScreens[1] = ScreenRec.Create("gsVillage", "Village", ScreenStatus.ssOnce);
            dbScreens[2] = ScreenRec.Create("gsForest", "Forest", ScreenStatus.ssOnce);
            dbScreens[3] = ScreenRec.Create("gsJotenheim", "Jotenheim", ScreenStatus.ssOnce);
            dbScreens[4] = ScreenRec.Create("gsNidavell", "Nidavell", ScreenStatus.ssOnce);
            dbScreens[5] = ScreenRec.Create("gsNiflheim", "Niflheim", ScreenStatus.ssOnce);
            dbScreens[6] = ScreenRec.Create("gsCrossroad", "Crossroad", ScreenStatus.ssOnce);
            dbScreens[7] = ScreenRec.Create("gsDungeon", "Dungeon", ScreenStatus.ssOnce);
            dbScreens[8] = ScreenRec.Create("gsSea", "Sea", ScreenStatus.ssOnce);
            dbScreens[9] = ScreenRec.Create("gsAlfheim", "Alfheim", ScreenStatus.ssOnce);
            dbScreens[10] = ScreenRec.Create("gsMuspelheim", "Muspelheim", ScreenStatus.ssOnce);
            dbScreens[11] = ScreenRec.Create("gsWell", "Well", ScreenStatus.ssOnce);
            dbScreens[12] = ScreenRec.Create("gsTemple", "Temple", ScreenStatus.ssOnce);
            dbScreens[13] = ScreenRec.Create("gsWasteland", "Wasteland", ScreenStatus.ssOnce);
            dbScreens[14] = ScreenRec.Create("gsDead", "Dead", ScreenStatus.ssAlways);
            dbScreens[15] = ScreenRec.Create("gsMain", "", ScreenStatus.ssAlways);
            dbScreens[16] = ScreenRec.Create("gsDefeat", "Defeat", ScreenStatus.ssAlways);
            dbScreens[17] = ScreenRec.Create("gsVictory", "Victory", ScreenStatus.ssAlways);
            dbScreens[18] = ScreenRec.Create("gsSwirl", "Swirl", ScreenStatus.ssAlways);
            dbScreens[19] = ScreenRec.Create("gsNone", "", ScreenStatus.ssNever);

            dbUserActions = new UserActionRec[25];
            dbUserActions[0] = UserActionRec.Create("Dialog", RS.rs_Dialog, EventID.event_Dialog, new HotKey(Keys.GK_SPACE, new ShiftStates()), false);
            dbUserActions[1] = UserActionRec.Create("Menu", RS.rs_Menu, EventID.event_Menu, new HotKey(Keys.GK_Q, new ShiftStates()), false);
            dbUserActions[2] = UserActionRec.Create("Skills", RS.rs_Skills, EventID.event_Skills, new HotKey(Keys.GK_S, new ShiftStates()), false);
            dbUserActions[3] = UserActionRec.Create("Knowledges", RS.rs_Knowledges, EventID.event_Knowledges, new HotKey(Keys.GK_K, new ShiftStates()), false);
            dbUserActions[4] = UserActionRec.Create("Pack", RS.rs_Pack, EventID.event_Pack, new HotKey(Keys.GK_I, new ShiftStates()), false);
            dbUserActions[5] = UserActionRec.Create("Self", RS.rs_Self, EventID.event_Self, new HotKey(Keys.GK_C, new ShiftStates()), false);
            dbUserActions[6] = UserActionRec.Create("Map", RS.rs_Map, EventID.event_Map, new HotKey(Keys.GK_M, new ShiftStates()), false);
            dbUserActions[7] = UserActionRec.Create("Options", RS.rs_Options, EventID.event_Options, new HotKey(Keys.GK_O, new ShiftStates()), false);
            dbUserActions[8] = UserActionRec.Create("Help", RS.rs_Help, EventID.event_Help, new HotKey(Keys.GK_F1, new ShiftStates()), false);
            dbUserActions[9] = UserActionRec.Create("MoveN", RS.rs_MoveN, EventID.event_PlayerMoveN, new HotKey(Keys.GK_NUMPAD8, new ShiftStates()), true);
            dbUserActions[10] = UserActionRec.Create("MoveS", RS.rs_MoveS, EventID.event_PlayerMoveS, new HotKey(Keys.GK_NUMPAD2, new ShiftStates()), true);
            dbUserActions[11] = UserActionRec.Create("MoveW", RS.rs_MoveW, EventID.event_PlayerMoveW, new HotKey(Keys.GK_NUMPAD4, new ShiftStates()), true);
            dbUserActions[12] = UserActionRec.Create("MoveE", RS.rs_MoveE, EventID.event_PlayerMoveE, new HotKey(Keys.GK_NUMPAD6, new ShiftStates()), true);
            dbUserActions[13] = UserActionRec.Create("MoveNW", RS.rs_MoveNW, EventID.event_PlayerMoveNW, new HotKey(Keys.GK_NUMPAD7, new ShiftStates()), true);
            dbUserActions[14] = UserActionRec.Create("MoveNE", RS.rs_MoveNE, EventID.event_PlayerMoveNE, new HotKey(Keys.GK_NUMPAD9, new ShiftStates()), true);
            dbUserActions[15] = UserActionRec.Create("MoveSW", RS.rs_MoveSW, EventID.event_PlayerMoveSW, new HotKey(Keys.GK_NUMPAD1, new ShiftStates()), true);
            dbUserActions[16] = UserActionRec.Create("MoveSE", RS.rs_MoveSE, EventID.event_PlayerMoveSE, new HotKey(Keys.GK_NUMPAD3, new ShiftStates()), true);
            dbUserActions[17] = UserActionRec.Create("Wait", RS.rs_Wait, EventID.event_Wait, new HotKey(Keys.GK_PERIOD, new ShiftStates()), false);
            dbUserActions[18] = UserActionRec.Create("PickupAll", RS.rs_PickupAll, EventID.event_PickupAll, new HotKey(Keys.GK_COMMA, new ShiftStates()), false);
            dbUserActions[19] = UserActionRec.Create("Load", RS.rs_Load, EventID.event_Load, new HotKey(Keys.GK_F3, new ShiftStates()), false);
            dbUserActions[20] = UserActionRec.Create("Save", RS.rs_Save, EventID.event_Save, new HotKey(Keys.GK_F2, new ShiftStates()), false);
            dbUserActions[21] = UserActionRec.Create("Party", RS.rs_Party, EventID.event_Party, new HotKey(Keys.GK_P, new ShiftStates()), false);
            dbUserActions[22] = UserActionRec.Create("MoveUp", RS.rs_MoveUp, EventID.event_PlayerMoveUp, new HotKey(Keys.GK_PERIOD, new ShiftStates()), false);
            dbUserActions[23] = UserActionRec.Create("MoveDown", RS.rs_MoveDown, EventID.event_PlayerMoveDown, new HotKey(Keys.GK_COMMA, new ShiftStates()), false);
            dbUserActions[24] = UserActionRec.Create("Journal", RS.rs_Journal, EventID.event_Journal, new HotKey(Keys.GK_J, new ShiftStates()), false);

            dbSysCreatures = new SysCreatureRec[7];
            dbSysCreatures[0] = SysCreatureRec.Create(SysCreature.sc_Viking, RS.rs_Viking, "Viking", ExtRect.Create(64, 125, 138, 209));
            dbSysCreatures[1] = SysCreatureRec.Create(SysCreature.sc_Woodsman, RS.rs_Woodsman, "Woodsman", ExtRect.Create(257, 125, 331, 209));
            dbSysCreatures[2] = SysCreatureRec.Create(SysCreature.sc_Sage, RS.rs_Sage, "Sage", ExtRect.Create(451, 125, 525, 209));
            dbSysCreatures[3] = SysCreatureRec.Create(SysCreature.sc_Alchemist, RS.rs_Alchemist, "Alchemist", ExtRect.Create(64, 274, 138, 358));
            dbSysCreatures[4] = SysCreatureRec.Create(SysCreature.sc_Blacksmith, RS.rs_Blacksmith, "Blacksmith", ExtRect.Create(257, 274, 331, 358));
            dbSysCreatures[5] = SysCreatureRec.Create(SysCreature.sc_Conjurer, RS.rs_Conjurer, "Conjurer", ExtRect.Create(451, 274, 525, 358));
            dbSysCreatures[6] = SysCreatureRec.Create(SysCreature.sc_Merchant, RS.rs_Merchant, "Merchant", ExtRect.Create(0, 0, 0, 0));

            dbItemKinds = new ItemKindRec[26];
            dbItemKinds[0] = ItemKindRec.Create(RS.rs_IK_Armor, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[1] = ItemKindRec.Create(RS.rs_IK_DeadBody, 10, BaseScreen.clMaroon, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[2] = ItemKindRec.Create(RS.rs_IK_Food, 8, BaseScreen.clYellow, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[3] = ItemKindRec.Create(RS.rs_IK_Potion, 4, BaseScreen.clYellow, true, 13, "Potion", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[4] = ItemKindRec.Create(RS.rs_IK_Ring, 5, BaseScreen.clGold, true, 26, "Ring", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[5] = ItemKindRec.Create(RS.rs_IK_Tool, 9, BaseScreen.clBlue, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emHold);
            dbItemKinds[6] = ItemKindRec.Create(RS.rs_IK_Wand, 7, BaseScreen.clLime, true, 21, "Wand", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[7] = ItemKindRec.Create(RS.rs_IK_BluntWeapon, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_BluntWeapon, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[8] = ItemKindRec.Create(RS.rs_IK_Scroll, 6, BaseScreen.clAqua, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[9] = ItemKindRec.Create(RS.rs_IK_Coin, 11, BaseScreen.clGold, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[10] = ItemKindRec.Create(RS.rs_IK_Amulet, 1, BaseScreen.clGold, true, 25, "Amulet", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[11] = ItemKindRec.Create(RS.rs_IK_ShortBlade, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_ShortBlades, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[12] = ItemKindRec.Create(RS.rs_IK_LongBlade, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_LongBlade, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[13] = ItemKindRec.Create(RS.rs_IK_Shield, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_Parry, ItemFlags.Create(), EquipMode.emHold);
            dbItemKinds[14] = ItemKindRec.Create(RS.rs_IK_Helmet, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[15] = ItemKindRec.Create(RS.rs_IK_Clothing, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[16] = ItemKindRec.Create(RS.rs_IK_Spear, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_Spear, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[17] = ItemKindRec.Create(RS.rs_IK_Axe, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_Axe, ItemFlags.Create(ItemFlags.if_MeleeWeapon), EquipMode.emHold);
            dbItemKinds[18] = ItemKindRec.Create(RS.rs_IK_Bow, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_LongBow, ItemFlags.Create(ItemFlags.if_ShootWeapon, ItemFlags.if_TwoHanded), EquipMode.emHold);
            dbItemKinds[19] = ItemKindRec.Create(RS.rs_IK_CrossBow, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_CrossBow, ItemFlags.Create(ItemFlags.if_ShootWeapon, ItemFlags.if_TwoHanded), EquipMode.emHold);
            dbItemKinds[20] = ItemKindRec.Create(RS.rs_IK_HeavyArmor, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_HeavyArmor, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[21] = ItemKindRec.Create(RS.rs_IK_MediumArmor, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_MediumArmor, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[22] = ItemKindRec.Create(RS.rs_IK_LightArmor, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_LightArmor, ItemFlags.Create(), EquipMode.emWear);
            dbItemKinds[23] = ItemKindRec.Create(RS.rs_IK_MusicalTool, 9, BaseScreen.clBlue, false, 0, "", AbilityID.Ab_MusicalAcuity, ItemFlags.Create(), EquipMode.emNone);
            dbItemKinds[24] = ItemKindRec.Create(RS.rs_IK_Projectile, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(ItemFlags.if_Projectile), EquipMode.emWear);
            dbItemKinds[25] = ItemKindRec.Create(RS.rs_IK_Misc, 9, BaseScreen.clBlue, false, 0, "", AbilityID.Ab_None, ItemFlags.Create(), EquipMode.emNone);

            dbItemStates = new ItemStateRec[3];
            dbItemStates[0] = ItemStateRec.Create("", RS.rs_Reserved, 1f, 0);
            dbItemStates[1] = ItemStateRec.Create(" (б)", RS.rs_Blessed, 1.6f, 1);
            dbItemStates[2] = ItemStateRec.Create(" (п)", RS.rs_Cursed, 0.4f, -1);

            dbItfElements = new ItfElementRec[15];
            dbItfElements[0] = ItfElementRec.Create("Up.tga", BaseScreen.clWhite);
            dbItfElements[1] = ItfElementRec.Create("Down.tga", BaseScreen.clWhite);
            dbItfElements[2] = ItfElementRec.Create("Left.tga", BaseScreen.clWhite);
            dbItfElements[3] = ItfElementRec.Create("Right.tga", BaseScreen.clWhite);
            dbItfElements[4] = ItfElementRec.Create("Cursor.tga", BaseScreen.clWhite);
            dbItfElements[5] = ItfElementRec.Create("Cursor_N.tga", BaseScreen.clWhite);
            dbItfElements[6] = ItfElementRec.Create("Cursor_S.tga", BaseScreen.clWhite);
            dbItfElements[7] = ItfElementRec.Create("Cursor_W.tga", BaseScreen.clWhite);
            dbItfElements[8] = ItfElementRec.Create("Cursor_E.tga", BaseScreen.clWhite);
            dbItfElements[9] = ItfElementRec.Create("Cursor_NW.tga", BaseScreen.clWhite);
            dbItfElements[10] = ItfElementRec.Create("Cursor_NE.tga", BaseScreen.clWhite);
            dbItfElements[11] = ItfElementRec.Create("Cursor_SW.tga", BaseScreen.clWhite);
            dbItfElements[12] = ItfElementRec.Create("Cursor_SE.tga", BaseScreen.clWhite);
            dbItfElements[13] = ItfElementRec.Create("FileDelete.tga", BaseScreen.clNone);
            dbItfElements[14] = ItfElementRec.Create("FileNum.tga", BaseScreen.clNone);

            dbMainControls = new MainControlRec[10];
            dbMainControls[0] = MainControlRec.Create(RS.rs_Reserved, "", EventID.event_Nothing, ExtRect.Create(0, 0, 0, 0));
            dbMainControls[1] = MainControlRec.Create(RS.rs_Menu, "itf/MainStBtn.tga", EventID.event_Menu, ExtRect.Create(MC_X, MC_Y + BtnTH * 0, MC_X + St_BtnW, MC_Y + BtnHeight + BtnTH * 0));
            dbMainControls[2] = MainControlRec.Create(RS.rs_Map, "itf/MainStBtn.tga", EventID.event_Map, ExtRect.Create(MC_X + St_BtnW + 4, MC_Y + BtnTH * 0, MC_X + (St_BtnW << 1) + 4, MC_Y + BtnHeight + BtnTH * 0));
            dbMainControls[3] = MainControlRec.Create(RS.rs_Knowledges, "itf/MainStBtn.tga", EventID.event_Knowledges, ExtRect.Create(MC_X, MC_Y + (BtnTH << 0), MC_X + St_BtnW, MC_Y + BtnHeight + (BtnTH << 0)));
            dbMainControls[4] = MainControlRec.Create(RS.rs_Skills, "itf/MainStBtn.tga", EventID.event_Skills, ExtRect.Create(MC_X + St_BtnW + 4, MC_Y + (BtnTH << 0), MC_X + (St_BtnW << 1) + 4, MC_Y + BtnHeight + (BtnTH << 0)));
            dbMainControls[5] = MainControlRec.Create(RS.rs_Self, "itf/MainLgBtn.tga", EventID.event_Self, ExtRect.Create(MC_X, MC_Y + (BtnTH << 1), MC_X + Lg_BtnW, MC_Y + BtnHeight + (BtnTH << 1)));
            dbMainControls[6] = MainControlRec.Create(RS.rs_Pack, "itf/MainLgBtn.tga", EventID.event_Pack, ExtRect.Create(MC_X, MC_Y + BtnTH * 3, MC_X + Lg_BtnW, MC_Y + BtnHeight + BtnTH * 3));
            dbMainControls[7] = MainControlRec.Create(RS.rs_Reserved, "", EventID.event_Nothing, ExtRect.Create(640, 73, 792, 80));
            dbMainControls[8] = MainControlRec.Create(RS.rs_Reserved, "", EventID.event_Nothing, ExtRect.Create(640, 99, 792, 106));
            dbMainControls[9] = MainControlRec.Create(RS.rs_Reserved, "", EventID.event_Nothing, ExtRect.Create(640, 125, 792, 132));

            dbMaterialKind = new MaterialKindRec[16];
            dbMaterialKind[0] = MaterialKindRec.Create(RS.rs_Unknown);
            dbMaterialKind[1] = MaterialKindRec.Create(RS.rs_Mat_Stone);
            dbMaterialKind[2] = MaterialKindRec.Create(RS.rs_Mat_Metal);
            dbMaterialKind[3] = MaterialKindRec.Create(RS.rs_Mat_Wood);
            dbMaterialKind[4] = MaterialKindRec.Create(RS.rs_Mat_Glass);
            dbMaterialKind[5] = MaterialKindRec.Create(RS.rs_Mat_Bone);
            dbMaterialKind[6] = MaterialKindRec.Create(RS.rs_Mat_Leather);
            dbMaterialKind[7] = MaterialKindRec.Create(RS.rs_Mat_Flesh);
            dbMaterialKind[8] = MaterialKindRec.Create(RS.rs_Mat_Paper);
            dbMaterialKind[9] = MaterialKindRec.Create(RS.rs_Mat_Cloth);
            dbMaterialKind[10] = MaterialKindRec.Create(RS.rs_Mat_Liquid);
            dbMaterialKind[11] = MaterialKindRec.Create(RS.rs_Mat_Fiber);
            dbMaterialKind[12] = MaterialKindRec.Create(RS.rs_Mat_Steel);
            dbMaterialKind[13] = MaterialKindRec.Create(RS.rs_Mat_Diamond);
            dbMaterialKind[14] = MaterialKindRec.Create(RS.rs_Mat_Silver);
            dbMaterialKind[15] = MaterialKindRec.Create(RS.rs_Mat_Mithril);

            dbRaces = new RaceRec[5];
            dbRaces[0] = RaceRec.Create(RS.rs_Reserved, RS.rs_Reserved);
            dbRaces[1] = RaceRec.Create(RS.rs_Race_Human, RS.rs_KL_Race_Humans);
            dbRaces[2] = RaceRec.Create(RS.rs_Race_Aesir, RS.rs_KL_Race_Aesirs);
            dbRaces[3] = RaceRec.Create(RS.rs_Race_EvilGod, RS.rs_KL_Race_EvilGods);
            dbRaces[4] = RaceRec.Create(RS.rs_Race_Daemon, RS.rs_KL_Race_Daemons);

            dbSkills = new SkillRec[72];
            dbSkills[0] = SkillRec.Create(RS.rs_Reserved, SkillKind.ssPlain, "", EffectID.eid_None);
            dbSkills[1] = SkillRec.Create(RS.rs_Alchemy, SkillKind.ssPlain, "Alchemy", EffectID.eid_First);
            dbSkills[2] = SkillRec.Create(RS.rs_ArrowMake, SkillKind.ssPlain, "ArrowMake", EffectID.eid_ArrowMake);
            dbSkills[3] = SkillRec.Create(RS.rs_Cartography, SkillKind.ssPlain, "Cartography", EffectID.eid_Cartography);
            dbSkills[4] = SkillRec.Create(RS.rs_Diagnosis, SkillKind.ssPlain, "Diagnosis", EffectID.eid_Diagnosis);
            dbSkills[5] = SkillRec.Create(RS.rs_Embalming, SkillKind.ssPlain, "Embalming", EffectID.eid_Embalming);
            dbSkills[6] = SkillRec.Create(RS.rs_Fennling, SkillKind.ssPlain, "Fennling", EffectID.eid_Fennling);
            dbSkills[7] = SkillRec.Create(RS.rs_GolemCreation, SkillKind.ssPlain, "GolemCreation", EffectID.eid_GolemCreation);
            dbSkills[8] = SkillRec.Create(RS.rs_Husbandry, SkillKind.ssPlain, "Husbandry", EffectID.eid_Husbandry);
            dbSkills[9] = SkillRec.Create(RS.rs_Ironworking, SkillKind.ssPlain, "Ironworking", EffectID.eid_First);
            dbSkills[10] = SkillRec.Create(RS.rs_Precognition, SkillKind.ssPlain, "Precognition", EffectID.eid_Precognition);
            dbSkills[11] = SkillRec.Create(RS.rs_Relocation, SkillKind.ssPlain, "Relocation", EffectID.eid_Relocation);
            dbSkills[12] = SkillRec.Create(RS.rs_SlaveUse, SkillKind.ssPlain, "SlaveUse", EffectID.eid_SlaveUse);
            dbSkills[13] = SkillRec.Create(RS.rs_Taming, SkillKind.ssPlain, "Taming", EffectID.eid_Taming);
            dbSkills[14] = SkillRec.Create(RS.rs_Ventriloquism, SkillKind.ssPlain, "Ventriloquism", EffectID.eid_Ventriloquism);
            dbSkills[15] = SkillRec.Create(RS.rs_Writing, SkillKind.ssPlain, "Writing", EffectID.eid_Writing);
            dbSkills[16] = SkillRec.Create(RS.rs_Animation, SkillKind.ssPlain, "Animation", EffectID.eid_Animation);
            dbSkills[17] = SkillRec.Create(RS.rs_DimensionTravel, SkillKind.ssPlain, "DimensionTravel", EffectID.eid_SwitchDimension);
            dbSkills[18] = SkillRec.Create(RS.rs_FireVision, SkillKind.ssPlain, "FireVision", EffectID.eid_FireVision);
            dbSkills[19] = SkillRec.Create(RS.rs_HeatRadiation, SkillKind.ssPlain, "HeatRadiation", EffectID.eid_Heat);
            dbSkills[20] = SkillRec.Create(RS.rs_MindControl, SkillKind.ssPlain, "MindControl", EffectID.eid_MindControl);
            dbSkills[21] = SkillRec.Create(RS.rs_PsiBlast, SkillKind.ssPlain, "PsiBlast", EffectID.eid_PsiBlast);
            dbSkills[22] = SkillRec.Create(RS.rs_Terraforming, SkillKind.ssPlain, "Terraforming", EffectID.eid_Geology);
            dbSkills[23] = SkillRec.Create(RS.rs_Spellcasting, SkillKind.ssMeta, "Spellcasting", EffectID.eid_First);
            dbSkills[24] = SkillRec.Create(RS.rs_InnatePower, SkillKind.ssMeta, "InnatePower", EffectID.eid_First);
            dbSkills[25] = SkillRec.Create(RS.rs_Basilisk_Poison, SkillKind.ssInnatePower, "", EffectID.eid_Basilisk_Poison);
            dbSkills[26] = SkillRec.Create(RS.rs_Borgonvile_Cloud, SkillKind.ssInnatePower, "", EffectID.eid_Borgonvile_Cloud);
            dbSkills[27] = SkillRec.Create(RS.rs_Breleor_Tendril, SkillKind.ssInnatePower, "", EffectID.eid_Breleor_Tendril);
            dbSkills[28] = SkillRec.Create(RS.rs_Ellegiant_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Ellegiant_Throw);
            dbSkills[29] = SkillRec.Create(RS.rs_Ellegiant_Crush, SkillKind.ssInnatePower, "", EffectID.eid_Ellegiant_Crush);
            dbSkills[30] = SkillRec.Create(RS.rs_Firedragon_Breath, SkillKind.ssInnatePower, "", EffectID.eid_Firedragon_Breath);
            dbSkills[31] = SkillRec.Create(RS.rs_Firegiant_Touch, SkillKind.ssInnatePower, "", EffectID.eid_Firegiant_Touch);
            dbSkills[32] = SkillRec.Create(RS.rs_Fyleisch_Cloud, SkillKind.ssInnatePower, "", EffectID.eid_Fyleisch_Cloud);
            dbSkills[33] = SkillRec.Create(RS.rs_Gasball_Explosion, SkillKind.ssInnatePower, "", EffectID.eid_Gasball_Explosion);
            dbSkills[34] = SkillRec.Create(RS.rs_Giantsquid_Crush, SkillKind.ssInnatePower, "", EffectID.eid_Giantsquid_Crush);
            dbSkills[35] = SkillRec.Create(RS.rs_Glard_Poison, SkillKind.ssInnatePower, "", EffectID.eid_Glard_Poison);
            dbSkills[36] = SkillRec.Create(RS.rs_Hatchetfish_Teeth, SkillKind.ssInnatePower, "", EffectID.eid_Hatchetfish_Teeth);
            dbSkills[37] = SkillRec.Create(RS.rs_Heldragon_Cloud, SkillKind.ssInnatePower, "", EffectID.eid_Heldragon_Cloud);
            dbSkills[38] = SkillRec.Create(RS.rs_Hillgiant_Crush, SkillKind.ssInnatePower, "", EffectID.eid_Hillgiant_Crush);
            dbSkills[39] = SkillRec.Create(RS.rs_Icedragon_Breath, SkillKind.ssInnatePower, "", EffectID.eid_Icedragon_Breath);
            dbSkills[40] = SkillRec.Create(RS.rs_Icesphere_Blast, SkillKind.ssInnatePower, "", EffectID.eid_Icesphere_Blast);
            dbSkills[41] = SkillRec.Create(RS.rs_Jagredin_Burning, SkillKind.ssInnatePower, "", EffectID.eid_Jagredin_Burning);
            dbSkills[42] = SkillRec.Create(RS.rs_Knellbird_Gaze, SkillKind.ssInnatePower, "", EffectID.eid_Knellbird_Gaze);
            dbSkills[43] = SkillRec.Create(RS.rs_Kobold_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Kobold_Throw);
            dbSkills[44] = SkillRec.Create(RS.rs_Lowerdwarf_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Lowerdwarf_Throw);
            dbSkills[45] = SkillRec.Create(RS.rs_Moleman_Debris, SkillKind.ssInnatePower, "", EffectID.eid_Moleman_Debris);
            dbSkills[46] = SkillRec.Create(RS.rs_Phantomasp_Poison, SkillKind.ssInnatePower, "", EffectID.eid_Phantomasp_Poison);
            dbSkills[47] = SkillRec.Create(RS.rs_Pyrtaath_Throttle, SkillKind.ssInnatePower, "", EffectID.eid_Pyrtaath_Throttle);
            dbSkills[48] = SkillRec.Create(RS.rs_Ramapith_FireTouch, SkillKind.ssInnatePower, "", EffectID.eid_Ramapith_FireTouch);
            dbSkills[49] = SkillRec.Create(RS.rs_Sandiff_Acid, SkillKind.ssInnatePower, "", EffectID.eid_Sandiff_Acid);
            dbSkills[50] = SkillRec.Create(RS.rs_Scyld_Breath, SkillKind.ssInnatePower, "", EffectID.eid_Scyld_Breath);
            dbSkills[51] = SkillRec.Create(RS.rs_Scyld_Ray, SkillKind.ssInnatePower, "", EffectID.eid_Scyld_Ray);
            dbSkills[52] = SkillRec.Create(RS.rs_Scyld_ShockWave, SkillKind.ssInnatePower, "", EffectID.eid_Scyld_ShockWave);
            dbSkills[53] = SkillRec.Create(RS.rs_Sentinel_Gaze, SkillKind.ssInnatePower, "", EffectID.eid_Sentinel_Gaze);
            dbSkills[54] = SkillRec.Create(RS.rs_Serpent_Poison, SkillKind.ssInnatePower, "", EffectID.eid_Serpent_Poison);
            dbSkills[55] = SkillRec.Create(RS.rs_Shadow_Touch, SkillKind.ssInnatePower, "", EffectID.eid_Shadow_Touch);
            dbSkills[56] = SkillRec.Create(RS.rs_Slinn_Gout, SkillKind.ssInnatePower, "", EffectID.eid_Slinn_Gout);
            dbSkills[57] = SkillRec.Create(RS.rs_Spirit_Touch, SkillKind.ssInnatePower, "", EffectID.eid_Spirit_Touch);
            dbSkills[58] = SkillRec.Create(RS.rs_Stunworm_Stun, SkillKind.ssInnatePower, "", EffectID.eid_Stunworm_Stun);
            dbSkills[59] = SkillRec.Create(RS.rs_Terrain_Burning, SkillKind.ssInnatePower, "", EffectID.eid_Terrain_Burning);
            dbSkills[60] = SkillRec.Create(RS.rs_Warrior_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Warrior_Throw);
            dbSkills[61] = SkillRec.Create(RS.rs_Watcher_Gaze, SkillKind.ssInnatePower, "", EffectID.eid_Watcher_Gaze);
            dbSkills[62] = SkillRec.Create(RS.rs_Weirdfume_Acid, SkillKind.ssInnatePower, "", EffectID.eid_WeirdFume_Acid);
            dbSkills[63] = SkillRec.Create(RS.rs_Womera_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Womera_Throw);
            dbSkills[64] = SkillRec.Create(RS.rs_Wooddwarf_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Wooddwarf_Throw);
            dbSkills[65] = SkillRec.Create(RS.rs_Wyvern_Breath, SkillKind.ssInnatePower, "", EffectID.eid_Wyvern_Breath);
            dbSkills[66] = SkillRec.Create(RS.rs_Zardon_PsiBlast, SkillKind.ssInnatePower, "", EffectID.eid_Zardon_PsiBlast);
            dbSkills[67] = SkillRec.Create(RS.rs_Dig, SkillKind.ssPlain, "Dig", EffectID.eid_Dig);
            dbSkills[68] = SkillRec.Create(RS.rs_Ull_Gaze, SkillKind.ssInnatePower, "", EffectID.eid_Ull_Gaze);
            dbSkills[69] = SkillRec.Create(RS.rs_Prayer, SkillKind.ssPlain, "Prayer", EffectID.eid_Prayer);
            dbSkills[70] = SkillRec.Create(RS.rs_Sacrifice, SkillKind.ssPlain, "Sacrifice", EffectID.eid_Sacrifice);
            dbSkills[71] = SkillRec.Create(RS.rs_RunicDivination, SkillKind.ssPlain, "Divination", EffectID.eid_RunicDivination);

            dbSymbols = new SymbolRec[84];
            dbSymbols[0] = SymbolRec.Create("_None", 0, 'x', BaseScreen.clWhite);
            dbSymbols[1] = SymbolRec.Create("_Cursor", 0, 'x', BaseScreen.clWhite);
            dbSymbols[2] = SymbolRec.Create("_Grass", 0, '.', BaseScreen.clGreen);
            dbSymbols[3] = SymbolRec.Create("_Tree", 0, 'T', BaseScreen.clLime);
            dbSymbols[4] = SymbolRec.Create("_Mountain", 0, '^', BaseScreen.clOlive);
            dbSymbols[5] = SymbolRec.Create("_Player", 0, '@', BaseScreen.clWhite);
            dbSymbols[6] = SymbolRec.Create("_Armor", 0, '[', BaseScreen.clWhite);
            dbSymbols[7] = SymbolRec.Create("_DeadBody", 0, '+', BaseScreen.clWhite);
            dbSymbols[8] = SymbolRec.Create("_Food", 0, '%', BaseScreen.clWhite);
            dbSymbols[9] = SymbolRec.Create("_Potion", 0, '%', BaseScreen.clWhite);
            dbSymbols[10] = SymbolRec.Create("_Ring", 0, '*', BaseScreen.clYellow);
            dbSymbols[11] = SymbolRec.Create("_Tool", 0, '=', BaseScreen.clWhite);
            dbSymbols[12] = SymbolRec.Create("_Trap", 0, ',', BaseScreen.clMaroon);
            dbSymbols[13] = SymbolRec.Create("_Wand", 0, '|', BaseScreen.clWhite);
            dbSymbols[14] = SymbolRec.Create("_Weapon", 0, '(', BaseScreen.clWhite);
            dbSymbols[15] = SymbolRec.Create("_Scroll", 0, '?', BaseScreen.clWhite);
            dbSymbols[16] = SymbolRec.Create("_Coin", 0, '$', BaseScreen.clYellow);
            dbSymbols[17] = SymbolRec.Create("_Floor", 0, '.', BaseScreen.clGray);
            dbSymbols[18] = SymbolRec.Create("_WallN", 0, '#', BaseScreen.clGray);
            dbSymbols[19] = SymbolRec.Create("_WallS", 0, '#', BaseScreen.clGray);
            dbSymbols[20] = SymbolRec.Create("_WallW", 0, '#', BaseScreen.clGray);
            dbSymbols[21] = SymbolRec.Create("_WallE", 0, '#', BaseScreen.clGray);
            dbSymbols[22] = SymbolRec.Create("_WallNW", 0, '#', BaseScreen.clGray);
            dbSymbols[23] = SymbolRec.Create("_WallNE", 0, '#', BaseScreen.clGray);
            dbSymbols[24] = SymbolRec.Create("_WallSW", 0, '#', BaseScreen.clGray);
            dbSymbols[25] = SymbolRec.Create("_WallSE", 0, '#', BaseScreen.clGray);
            dbSymbols[26] = SymbolRec.Create("_Enemy", 0, 'x', BaseScreen.clWhite);
            dbSymbols[27] = SymbolRec.Create("_Left", 0, 'x', BaseScreen.clWhite);
            dbSymbols[28] = SymbolRec.Create("_Up", 0, 'x', BaseScreen.clWhite);
            dbSymbols[29] = SymbolRec.Create("_Right", 0, 'x', BaseScreen.clWhite);
            dbSymbols[30] = SymbolRec.Create("_Down", 0, 'x', BaseScreen.clWhite);
            dbSymbols[31] = SymbolRec.Create("_Vortex", 0, '0', BaseScreen.clWhite);
            dbSymbols[32] = SymbolRec.Create("_Lava", 0, '~', BaseScreen.clMaroon);
            dbSymbols[33] = SymbolRec.Create("_Mud", 0, '~', BaseScreen.clPurple);
            dbSymbols[34] = SymbolRec.Create("_Water", 0, '~', BaseScreen.clBlue);
            dbSymbols[35] = SymbolRec.Create("_Ally", 0, 'x', BaseScreen.clYellow);
            dbSymbols[36] = SymbolRec.Create("_StairsDown", 0, '>', BaseScreen.clGray);
            dbSymbols[37] = SymbolRec.Create("_StairsUp", 0, '<', BaseScreen.clGray);
            dbSymbols[38] = SymbolRec.Create("_Quicksand", 0, '~', BaseScreen.clSilver);
            dbSymbols[39] = SymbolRec.Create("_Space", 0, '.', BaseScreen.clNavy);
            dbSymbols[40] = SymbolRec.Create("_Amulet", 0, '*', BaseScreen.clYellow);
            dbSymbols[41] = SymbolRec.Create("_cr_a", 0, 'x', BaseScreen.clWhite);
            dbSymbols[42] = SymbolRec.Create("_cr_b", 0, 'x', BaseScreen.clWhite);
            dbSymbols[43] = SymbolRec.Create("_cr_ba", 0, 'x', BaseScreen.clWhite);
            dbSymbols[44] = SymbolRec.Create("_cr_g", 0, 'x', BaseScreen.clWhite);
            dbSymbols[45] = SymbolRec.Create("_cr_gk", 0, 'x', BaseScreen.clWhite);
            dbSymbols[46] = SymbolRec.Create("_cr_k", 0, 'x', BaseScreen.clWhite);
            dbSymbols[47] = SymbolRec.Create("_cr_l", 0, 'x', BaseScreen.clWhite);
            dbSymbols[48] = SymbolRec.Create("_cr_r", 0, 'x', BaseScreen.clWhite);
            dbSymbols[49] = SymbolRec.Create("_cr_w", 0, 'x', BaseScreen.clWhite);
            dbSymbols[50] = SymbolRec.Create("_cr_wl", 0, 'x', BaseScreen.clWhite);
            dbSymbols[51] = SymbolRec.Create("_cr_y", 0, 'x', BaseScreen.clWhite);
            dbSymbols[52] = SymbolRec.Create("_cr_yr", 0, 'x', BaseScreen.clWhite);
            dbSymbols[53] = SymbolRec.Create("_cr_disk", 0, 'x', BaseScreen.clWhite);
            dbSymbols[54] = SymbolRec.Create("_Fog", 0, '°', BaseScreen.clAqua);
            dbSymbols[55] = SymbolRec.Create("_Hole", 0, 'o', BaseScreen.clWhite);
            dbSymbols[56] = SymbolRec.Create("_Bifrost", 150, 'ъ', BaseScreen.clWhite);
            dbSymbols[57] = SymbolRec.Create("_DeadTree", 0, 'T', BaseScreen.clSilver);
            dbSymbols[58] = SymbolRec.Create("_Well", 0, 'o', BaseScreen.clSilver);
            dbSymbols[59] = SymbolRec.Create("_Stalagmite", 0, '!', BaseScreen.clSilver);
            dbSymbols[60] = SymbolRec.Create("_CaveFloor", 0, '.', BaseScreen.clOlive);
            dbSymbols[61] = SymbolRec.Create("_CaveWall", 0, '#', BaseScreen.clOlive);
            dbSymbols[62] = SymbolRec.Create("_SmallPit", 0, 'o', BaseScreen.clSilver);
            dbSymbols[63] = SymbolRec.Create("_Rubble", 0, '~', BaseScreen.clGray);
            dbSymbols[64] = SymbolRec.Create("_Liquid", 0, '~', BaseScreen.clAqua);
            dbSymbols[65] = SymbolRec.Create("_Vulcan", 0, '^', BaseScreen.clMaroon);
            dbSymbols[66] = SymbolRec.Create("_Ground", 0, '.', BaseScreen.clGray);
            dbSymbols[67] = SymbolRec.Create("_Floor", 0, 'ъ', BaseScreen.clGray);
            dbSymbols[68] = SymbolRec.Create("_Floor", 0, 'ъ', BaseScreen.clGray);
            dbSymbols[69] = SymbolRec.Create("_Floor", 0, 'ъ', BaseScreen.clGray);
            dbSymbols[70] = SymbolRec.Create("_Floor", 0, 'ъ', BaseScreen.clGray);
            dbSymbols[71] = SymbolRec.Create("_WallN", 0, 'x', BaseScreen.clGray);
            dbSymbols[72] = SymbolRec.Create("_WallS", 0, 'x', BaseScreen.clGray);
            dbSymbols[73] = SymbolRec.Create("_WallW", 0, 'x', BaseScreen.clGray);
            dbSymbols[74] = SymbolRec.Create("_WallE", 0, 'x', BaseScreen.clGray);
            dbSymbols[75] = SymbolRec.Create("_AllyHuman", 0, '@', BaseScreen.clLime);
            dbSymbols[76] = SymbolRec.Create("_EnemyHuman", 0, '@', BaseScreen.clRed);
            dbSymbols[77] = SymbolRec.Create("_Ivy", 14, 'E', BaseScreen.clRed);
            dbSymbols[78] = SymbolRec.Create("_Snake", 24, 's', BaseScreen.clRed);
            dbSymbols[79] = SymbolRec.Create("_VortexStrange", 0, '0', BaseScreen.clWhite);
            dbSymbols[80] = SymbolRec.Create("_StoningHuman", 0, '.', BaseScreen.clWhite);
            dbSymbols[81] = SymbolRec.Create("_Ting", 64, '.', BaseScreen.clWhite);
            dbSymbols[82] = SymbolRec.Create("_Road", 15, '.', BaseScreen.clWhite);
            dbSymbols[83] = SymbolRec.Create("_IronTree", 0, '.', BaseScreen.clWhite);

            dbDialogServices = new DialogServiceRec[4];
            dbDialogServices[0] = DialogServiceRec.Create(RS.rs_Teach, RS.rs_Teach_Question);
            dbDialogServices[1] = DialogServiceRec.Create(RS.rs_Trade, RS.rs_Trade_Question);
            dbDialogServices[2] = DialogServiceRec.Create(RS.rs_Exchange, RS.rs_Exchange_Question);
            dbDialogServices[3] = DialogServiceRec.Create(RS.rs_Recruit, RS.rs_Recruit_Question);

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
