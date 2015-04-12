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
package nwr.core.types;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class PlaceID
{
    public static final int pid_Undefined = 0;
    public static final int pid_PoisonSpikeTrap = 1;
    public static final int pid_QuicksandTrap = 2;
    public static final int pid_WaterTrap = 3;
    public static final int pid_Grass = 4;
    public static final int pid_Tree = 5;
    public static final int pid_Mountain = 6;
    public static final int pid_Fog = 7;
    public static final int pid_LavaPool = 8;
    public static final int pid_DeadTree = 9;
    public static final int pid_Water = 10;
    public static final int pid_Quicksand = 11;
    public static final int pid_Space = 12;
    public static final int pid_Floor = 13;
    public static final int pid_Bifrost = 14;
    public static final int pid_DoorN = 15;
    public static final int pid_DoorS = 16;
    public static final int pid_DoorW = 17;
    public static final int pid_DoorE = 18;
    public static final int pid_DoorN_Closed = 19;
    public static final int pid_DoorS_Closed = 20;
    public static final int pid_DoorW_Closed = 21;
    public static final int pid_DoorE_Closed = 22;
    public static final int pid_WallN = 23;
    public static final int pid_WallS = 24;
    public static final int pid_WallW = 25;
    public static final int pid_WallE = 26;
    public static final int pid_WallNW = 27;
    public static final int pid_WallNE = 28;
    public static final int pid_WallSW = 29;
    public static final int pid_WallSE = 30;
    public static final int pid_cr_a = 31;
    public static final int pid_cr_b = 32;
    public static final int pid_cr_ba = 33;
    public static final int pid_cr_g = 34;
    public static final int pid_cr_gk = 35;
    public static final int pid_cr_k = 36;
    public static final int pid_cr_l = 37;
    public static final int pid_cr_r = 38;
    public static final int pid_cr_w = 39;
    public static final int pid_cr_wl = 40;
    public static final int pid_cr_y = 41;
    public static final int pid_cr_yr = 42;
    public static final int pid_cr_Disk = 43;
    public static final int pid_Vortex = 44;
    public static final int pid_StairsDown = 45;
    public static final int pid_StairsUp = 46;
    public static final int pid_TeleportTrap = 47;
    public static final int pid_StunGasTrap = 48;
    public static final int pid_Well = 49;
    public static final int pid_GStairsDown = 50;
    public static final int pid_GStairsUp = 51;
    public static final int pid_PitTrap = 52;
    public static final int pid_DoorTrap = 53;
    public static final int pid_FireTrap = 54;
    public static final int pid_FrostTrap = 55;
    public static final int pid_LavaTrap = 56;
    public static final int pid_Rock = 57;
    public static final int pid_Stalagmite = 58;
    public static final int pid_MonsterTrap = 59;
    public static final int pid_PhaseTrap = 60;
    public static final int pid_ArrowTrap = 61;
    public static final int pid_CrushRoofTrap = 62;
    public static final int pid_MistTrap = 63;
    public static final int pid_CaveFloor = 64;
    public static final int pid_CaveWall = 65;
    public static final int pid_FenceN = 66;
    public static final int pid_FenceS = 67;
    public static final int pid_FenceW = 68;
    public static final int pid_FenceE = 69;
    public static final int pid_FenceNE = 70;
    public static final int pid_FenceNW = 71;
    public static final int pid_FenceSE = 72;
    public static final int pid_FenceSW = 73;
    public static final int pid_FenceGateN = 74;
    public static final int pid_FenceGateS = 75;
    public static final int pid_FenceGateW = 76;
    public static final int pid_FenceGateE = 77;
    public static final int pid_GrassMask = 78;
    public static final int pid_WaterMask = 79;
    public static final int pid_Lava = 80;
    public static final int pid_LavaMask = 81;
    public static final int pid_Vulcan = 82;
    public static final int pid_Liquid = 83;
    public static final int pid_LiquidMask = 84;
    public static final int pid_HoleDown = 85;
    public static final int pid_HoleUp = 86;
    public static final int pid_Mud = 87;
    public static final int pid_SmallPit = 88;
    public static final int pid_MudMask = 89;
    public static final int pid_Rubble = 90;
    public static final int pid_RubbleMask = 91;
    public static final int pid_Ground = 92;
    public static final int pid_RoadMask1 = 93;
    public static final int pid_RoadMask2 = 94;
    public static final int pid_RoadMask3 = 95;
    public static final int pid_Bush = 96;
    public static final int pid_Stump = 97;
    public static final int pid_cr_Disk_Pressed = 98;
    public static final int pid_FogMask1 = 99;
    public static final int pid_FogMask2 = 100;
    public static final int pid_AsgardWallN = 101;
    public static final int pid_AsgardWallS = 102;
    public static final int pid_AsgardWallW = 103;
    public static final int pid_AsgardWallE = 104;
    public static final int pid_AsgardWallNW = 105;
    public static final int pid_AsgardWallNE = 106;
    public static final int pid_AsgardWallSW = 107;
    public static final int pid_AsgardWallSE = 108;
    public static final int pid_AsgardDoorN = 109;
    public static final int pid_AsgardDoorS = 110;
    public static final int pid_AsgardDoorW = 111;
    public static final int pid_AsgardDoorE = 112;
    public static final int pid_VortexStrange = 113;
    public static final int pid_SpaceMask = 114;
    public static final int pid_Ting = 115;
    public static final int pid_DungeonFloor = 116;
    public static final int pid_DungeonWall = 117;
    
    public static final int pid_RnFloor = 118;
    public static final int pid_RnWallN = 119;
    public static final int pid_RnWallS = 120;
    public static final int pid_RnWallW = 121;
    public static final int pid_RnWallE = 122;
    public static final int pid_RnWallNW = 123;
    public static final int pid_RnWallNE = 124;
    public static final int pid_RnWallSW = 125;
    public static final int pid_RnWallSE = 126;

    public static final int pid_IronTree = 127;
    
    public static final int pid_First = 1;
    public static final int pid_Last = 127;
}
