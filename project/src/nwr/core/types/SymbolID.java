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

import java.util.HashMap;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum SymbolID
{
    sid_None(0, "_None", 0),
    sid_Cursor(1, "_Cursor", 0),
    sid_Grass(2, "_Grass", 0),
    sid_Tree(3, "_Tree", 0),
    sid_Mountain(4, "_Mountain", 0),
    sid_Player(5, "_Player", 0),
    sid_Armor(6, "_Armor", 0),
    sid_DeadBody(7, "_DeadBody", 0),
    sid_Food(8, "_Food", 0),
    sid_Potion(9, "_Potion", 0),
    sid_Ring(10, "_Ring", 0),
    sid_Tool(11, "_Tool", 0),
    sid_Trap(12, "_Trap", 0),
    sid_Wand(13, "_Wand", 0),
    sid_Weapon(14, "_Weapon", 0),
    sid_Scroll(15, "_Scroll", 0),
    sid_Coin(16, "_Coin", 0),
    sid_Floor(17, "_Floor", 0),
    sid_WallN(18, "_WallN", 0),
    sid_WallS(19, "_WallS", 0),
    sid_WallW(20, "_WallW", 0),
    sid_WallE(21, "_WallE", 0),
    sid_WallNW(22, "_WallNW", 0),
    sid_WallNE(23, "_WallNE", 0),
    sid_WallSW(24, "_WallSW", 0),
    sid_WallSE(25, "_WallSE", 0),
    sid_Enemy(26, "_Enemy", 0),
    sid_Left(27, "_Left", 0),
    sid_Up(28, "_Up", 0),
    sid_Right(29, "_Right", 0),
    sid_Down(30, "_Down", 0),    
    sid_Vortex(31, "_Vortex", 0),
    sid_Lava(32, "_Lava", 0),
    sid_Mud(33, "_Mud", 0),
    sid_Water(34, "_Water", 0),
    sid_Ally(35, "_Ally", 0),
    sid_StairsDown(36, "_StairsDown", 0),
    sid_StairsUp(37, "_StairsUp", 0),
    sid_Quicksand(38, "_Quicksand", 0),
    sid_Space(39, "_Space", 0),
    sid_Amulet(40, "_Amulet", 0),
    sid_cr_a(41, "_cr_a", 0),
    sid_cr_b(42, "_cr_b", 0),
    sid_cr_ba(43, "_cr_ba", 0),
    sid_cr_g(44, "_cr_g", 0),
    sid_cr_gk(45, "_cr_gk", 0),
    sid_cr_k(46, "_cr_k", 0),
    sid_cr_l(47, "_cr_l", 0),
    sid_cr_r(48, "_cr_r", 0),
    sid_cr_w(49, "_cr_w", 0),
    sid_cr_wl(50, "_cr_wl", 0),
    sid_cr_y(51, "_cr_y", 0),
    sid_cr_yr(52, "_cr_yr", 0),
    sid_cr_disk(53, "_cr_disk", 0),
    sid_Fog(54, "_Fog", 0),
    sid_Hole(55, "_Hole", 0),
    sid_Bifrost(56, "_Bifrost", 150),
    sid_DeadTree(57, "_DeadTree", 0),
    sid_Well(58, "_Well", 0),
    sid_Stalagmite(59, "_Stalagmite", 0),
    sid_CaveFloor(60, "_CaveFloor", 0),
    sid_CaveWall(61, "_CaveWall", 0),
    sid_SmallPit(62, "_SmallPit", 0),
    sid_Rubble(63, "_Rubble", 0),
    sid_Liquid(64, "_Liquid", 0),
    sid_Vulcan(65, "_Vulcan", 0),
    sid_Ground(66, "_Ground", 0),
    sid_DoorN_Opened(67, "_Floor", 0),
    sid_DoorS_Opened(68, "_Floor", 0),
    sid_DoorW_Opened(69, "_Floor", 0),
    sid_DoorE_Opened(70, "_Floor", 0),
    sid_DoorN_Closed(71, "_WallN", 0),
    sid_DoorS_Closed(72, "_WallS", 0),
    sid_DoorW_Closed(73, "_WallW", 0),
    sid_DoorE_Closed(74, "_WallE", 0),
    sid_AllyHuman(75, "_AllyHuman", 0),
    sid_EnemyHuman(76, "_EnemyHuman", 0),
    sid_Ivy(77, "_Ivy", 14),
    sid_Snake(78, "_Snake", 24),
    sid_VortexStrange(79, "_VortexStrange", 0),
    sid_StoningHuman(80, "_StoningHuman", 0),
    sid_Ting(81, "_Ting", 64),
    sid_Road(82, "_Road", 15),
    sid_IronTree(83, "_IronTree", 0);

    public static final int sid_First = 0;
    public static final int sid_Last = 83;

    private final int intValue;
    public final String gfx;
    public final int subCount;
    public int ImageIndex;

    private static HashMap<Integer, SymbolID> mappings;

    private static HashMap<Integer, SymbolID> getMappings()
    {
        synchronized (SymbolID.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private SymbolID(int value, String gfx, int subCount)
    {
        this.intValue = value;
        this.gfx = gfx;
        this.subCount = subCount;

        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static SymbolID forValue(int value)
    {
        return getMappings().get(value);
    }
}
