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
package nwr.effects;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class EffectParams extends jzrlib.core.FlagSet
{
    public static final int ep_Place = 0;
    public static final int ep_Direction = 1;
    public static final int ep_Item = 2;
    public static final int ep_Creature = 3;
    public static final int ep_Area = 4;
    public static final int ep_Land = 5;
    public static final int ep_DunRoom = 6;
    public static final int ep_MonsterID = 7;
    public static final int ep_TileID = 8;
    public static final int ep_ScrollID = 9;
    public static final int ep_GodID = 10;
    public static final int ep_ItemExt = 11;

    public static final int ep_First = 0;
    public static final int ep_Last = 11;

    public EffectParams(int... args)
    {
        super(args);
    }
}
