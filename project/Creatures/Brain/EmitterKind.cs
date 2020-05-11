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
    public static class EmitterKind
    {
        public const int ek_Unknown = 0;
        public const int ek_Damaged = 1;
        public const int ek_Combat = 2;
        public const int ek_BattleSounds = 3;
        public const int ek_BloodSpatter = 4;
        public const int ek_DeadBody = 5;
        public const int ek_Missile = 6;
        public const int ek_GuardAlarm = 7;
        public const int ek_Call = 8;
        public const int ek_Creature = 9;
        public const int ek_Item = 10;
        public const int ek_AngryTownsman = 11;
        public const int ek_UpsetTownsman = 12;
    }
}
