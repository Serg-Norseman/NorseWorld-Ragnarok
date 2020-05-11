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

namespace NWR.Creatures.Brain
{
    /// <summary>
    /// 
    /// </summary>
    public static class GoalKind
    {
        public const int gk_Unknown = 0;
        public const int gk_Travel = 1;
        public const int gk_PointGuard = 2;
        public const int gk_ItemAcquire = 3;
        public const int gk_EnemyChase = 4;
        public const int gk_EnemyEvade = 5;
        public const int gk_Friend = 6;
        public const int gk_AreaGuard = 7;
        public const int gk_ShopReturn = 8;
        public const int gk_PlayerFind = 9;
        public const int gk_Escort = 10;
        public const int gk_Flock = 11;
        public const int gk_DebtTake = 12;
        public const int gk_WareReturn = 13;
        public const int gk_Stalk = 14;
    }
}
