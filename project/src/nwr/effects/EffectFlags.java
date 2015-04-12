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
public final class EffectFlags extends jzrlib.core.FlagSet
{
    public static final int ef_Ray = 0;
    public static final int ef_Cumulative = 1;

    public static final int ef_DirAnim = 2; // animation by direction
    public static final int ef_RotAnim = 3; // animation by rotation

    // Effect Progression
    public static final int ep_Decrease = 4;
    public static final int ep_Increase = 5;

    // AI Tactical flags
    public static final int ek_Offence = 6;
    public static final int ek_Defence = 7;
    public static final int ek_Enchant = 8;
    public static final int ek_Summon = 9;

    public static final int ek_Disease = 10;
    public static final int ek_Medicine = 11;
    public static final int ek_Advantage = 12;
    public static final int ek_Trap = 13;
    
    public EffectFlags(int... args)
    {
        super(args);
    }
}
