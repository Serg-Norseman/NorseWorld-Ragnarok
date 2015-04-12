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
package nwr.core;

import jzrlib.common.CreatureSex;
import jzrlib.core.Rect;
import jzrlib.grammar.Case;
import jzrlib.grammar.Gender;
import jzrlib.grammar.Grammar;
import jzrlib.map.Movements;
import jzrlib.utils.Logger;
import jzrlib.utils.TextUtils;
import nwr.core.types.AbilityID;
import nwr.core.types.PlaceFlags;
import nwr.core.types.PlaceID;
import nwr.core.types.PlaceRec;
import nwr.core.types.RuneRec;
import nwr.core.types.SexRec;
import nwr.core.types.SkillID;
import nwr.core.types.SymbolID;
import nwr.core.types.TeachableKind;
import nwr.core.types.TeachableRec;
import nwr.core.types.TrapRec;
import nwr.effects.EffectID;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class StaticData
{
    public static final String rs_GameName = "NorseWorld: Ragnarok";
    public static final String rs_GameVersion = "v0.11.0";
    public static final String rs_GameDevTime = "2002-2008, 2014-2015";
    public static final String rs_GameCopyright = "Copyright (c) " + rs_GameDevTime + " by Alchemist Team";

    public static final int FieldWidth = 76;
    public static final int FieldHeight = 18;
    public static final Rect FR = new Rect(-1, -1, FieldWidth, FieldHeight);
    public static final Rect MapArea = new Rect(0, 0, StaticData.FieldWidth - 1, StaticData.FieldHeight - 1);
    public static final Rect TV_Rect = new Rect(8, 494, 792, 592);

    public static final int rsFontHeight = 18;
    public static final int MC_X = 640;
    public static final int MC_Y = 8;
    public static final int Lg_BtnW = 152;
    public static final int St_BtnW = 74;
    public static final int btnHeight = 30;
    public static final int BtnTH = btnHeight + 4;

    public static final String OPTIONS_FILE = "Ragnarok.Options.xml";
    public static final String SCORES_FILE = "Ragnarok.Scores.rsl";
    public static final String GHOSTS_FILE = "Ragnarok.Ghosts.rgl";

    // Serializable <Entity> ID (SID)
    public static final byte SID_UNKNOWN = 0;
    public static final byte SID_BUILDING = 1;
    public static final byte SID_ITEM = 2;
    public static final byte SID_CREATURE = 3;
    public static final byte SID_KNOWLEDGE = 4;
    public static final byte SID_EFFECT = 5;
    public static final byte SID_RECALL_POS = 6;
    public static final byte SID_DEBT = 7;
    public static final byte SID_SOURCE_FORM = 8;
    public static final byte SID_POINTGUARD_GOAL = 9;
    public static final byte SID_AREAGUARD_GOAL = 10;
    public static final byte SID_VILLAGE = 11;
    public static final byte SID_GATE = 12;

    // Data Arrays
    public static final PlaceRec[] dbPlaces;
    public static final TrapRec[] dbTraps;
    public static final RuneRec[] dbRunes;
    public static final TeachableRec[] dbTeachable;
    public static final int[] dbDayTimeRS;
    public static final SexRec[] dbSex;
    public static final Gender[] GenderBySex;

    static {
        GenderBySex = new Gender[]{
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
        dbPlaces[12] = new PlaceRec(RS.rs_Tile_Space, "Space", SymbolID.sid_Space, new PlaceFlags(PlaceFlags.psBackground, PlaceFlags.psBarrier), new Movements(/*Movements.mkWalk, */Movements.mkFly), 0, EffectID.eid_None);
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
        dbTeachable[0] = new TeachableRec(AbilityID.Ab_Identification.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[1] = new TeachableRec(AbilityID.Ab_ShortBlades.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[2] = new TeachableRec(AbilityID.Ab_LongBow.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[3] = new TeachableRec(AbilityID.Ab_CrossBow.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[4] = new TeachableRec(AbilityID.Ab_Spear.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[5] = new TeachableRec(AbilityID.Ab_Axe.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[6] = new TeachableRec(AbilityID.Ab_Parry.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[7] = new TeachableRec(AbilityID.Ab_BluntWeapon.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[8] = new TeachableRec(AbilityID.Ab_HandToHand.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[9] = new TeachableRec(AbilityID.Ab_HeavyArmor.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[10] = new TeachableRec(AbilityID.Ab_LightArmor.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[11] = new TeachableRec(AbilityID.Ab_LongBlade.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[12] = new TeachableRec(AbilityID.Ab_Marksman.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[13] = new TeachableRec(AbilityID.Ab_MediumArmor.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[14] = new TeachableRec(AbilityID.Ab_Levitation.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[15] = new TeachableRec(AbilityID.Ab_Swimming.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[16] = new TeachableRec(AbilityID.Ab_Telepathy.getValue(), TeachableKind.Ability, RS.rs_Reserved);
        dbTeachable[17] = new TeachableRec(SkillID.Sk_Alchemy.getValue(), TeachableKind.Skill, RS.rs_YouLearnAlchemy);
        dbTeachable[18] = new TeachableRec(SkillID.Sk_ArrowMake.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[19] = new TeachableRec(SkillID.Sk_Cartography.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[20] = new TeachableRec(SkillID.Sk_Diagnosis.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[21] = new TeachableRec(SkillID.Sk_Embalming.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[22] = new TeachableRec(SkillID.Sk_Fennling.getValue(), TeachableKind.Skill, RS.rs_YouLearnFennl);
        dbTeachable[23] = new TeachableRec(SkillID.Sk_GolemCreation.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[24] = new TeachableRec(SkillID.Sk_Husbandry.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[25] = new TeachableRec(SkillID.Sk_Ironworking.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[26] = new TeachableRec(SkillID.Sk_Precognition.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[27] = new TeachableRec(SkillID.Sk_Relocation.getValue(), TeachableKind.Skill, RS.rs_YouLearnTeleportAtWill);
        dbTeachable[28] = new TeachableRec(SkillID.Sk_SlaveUse.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[29] = new TeachableRec(SkillID.Sk_Taming.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[30] = new TeachableRec(SkillID.Sk_Ventriloquism.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[31] = new TeachableRec(SkillID.Sk_Writing.getValue(), TeachableKind.Skill, RS.rs_YouLearnWrite);
        dbTeachable[32] = new TeachableRec(SkillID.Sk_Animation.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[33] = new TeachableRec(SkillID.Sk_DimensionTravel.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[34] = new TeachableRec(SkillID.Sk_MindControl.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[35] = new TeachableRec(SkillID.Sk_Terraforming.getValue(), TeachableKind.Skill, RS.rs_YouLearnTerraform);
        dbTeachable[36] = new TeachableRec(SkillID.Sk_Spellcasting.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[37] = new TeachableRec(SkillID.Sk_FireVision.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[38] = new TeachableRec(SkillID.Sk_Prayer.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[39] = new TeachableRec(SkillID.Sk_Sacrifice.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        dbTeachable[40] = new TeachableRec(SkillID.Sk_Divination.getValue(), TeachableKind.Skill, RS.rs_Reserved);
        
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
    }
    
    public static final CreatureSex getSexBySign(String sign)
    {
        for (SexRec rec : dbSex) {
            if (rec.Sign.equals(sign)) {
                return rec.Value;
            }
        }
        return CreatureSex.csNone;
    }

    public static String morphCompNoun(String aComp, Case c, jzrlib.grammar.Number q, CreatureSex sex, boolean animate, boolean endingstressed)
    {
        if (TextUtils.isNullOrEmpty(aComp)) {
            return "";
        }

        try {
            Gender gender = StaticData.GenderBySex[sex.Value];
            String result = "";

            int cnt = TextUtils.getTokensCount(aComp, " ");
            for (int i = 1; i <= cnt; i++) {
                if (i > 1) {
                    result += " ";
                }
                String tok = TextUtils.getToken(aComp, " ", i);
                result += Grammar.morphNoun(tok, c, q, gender, animate, endingstressed);
            }

            return result;
        } catch (Exception ex) {
            Logger.write("StaticData.morphCompNoun(): " + ex.getMessage());
            throw ex;
        }
    }

    public static final String getVerbRes(String wordRes, Gender gender, int voice)
    {
        try {
            String result;

            String word = TextUtils.getToken(wordRes, "[]", 1);
            String sfx = TextUtils.getToken(wordRes, "[]", 2);

            int sf_idx = (voice - 1) * 3 + (gender.getValue());
            String sf = TextUtils.getToken(sfx, ",", sf_idx);
            if (sf.compareTo("") == 0) {
                result = word;
            } else {
                if (sf.charAt(0) != '-') {
                    result = sf;
                } else {
                    sf = sf.substring(1);
                    result = word + sf;
                }
            }

            return result;
        } catch (Exception ex) {
            Logger.write("StaticData.getVerbRes(): " + ex.getMessage());
            throw ex;
        }
    }

    public static final String getVerbRes(int wordRes, CreatureSex sex, int voice)
    {
        return StaticData.getVerbRes(Locale.getStr(wordRes), StaticData.GenderBySex[sex.Value], voice);
    }
}
