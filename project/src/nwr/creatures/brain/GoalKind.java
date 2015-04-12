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
package nwr.creatures.brain;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class GoalKind
{
    public static final int gk_Unknown = 0;
    public static final int gk_Travel = 1;
    public static final int gk_PointGuard = 2;
    public static final int gk_ItemAcquire = 3;
    public static final int gk_EnemyChase = 4;
    public static final int gk_EnemyEvade = 5;
    public static final int gk_Friend = 6;
    public static final int gk_AreaGuard = 7;
    public static final int gk_ShopReturn = 8;
    public static final int gk_PlayerFind = 9;
    public static final int gk_Escort = 10;
    public static final int gk_Flock = 11;
    public static final int gk_DebtTake = 12;
    public static final int gk_WareReturn = 13;
    public static final int gk_Stalk = 14;
    
}
