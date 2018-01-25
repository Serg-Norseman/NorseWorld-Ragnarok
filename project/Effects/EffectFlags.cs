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

namespace NWR.Effects
{
    public sealed class EffectFlags : ZRLib.Core.FlagSet
    {
        public const int ef_Ray = 0;
        public const int ef_Cumulative = 1;
        public const int ef_DirAnim = 2; // animation by direction
        public const int ef_RotAnim = 3; // animation by rotation

        // Effect Progression
        public const int ep_Decrease = 4;
        public const int ep_Increase = 5;

        // AI Tactical flags
        public const int ek_Offence = 6;
        public const int ek_Defence = 7;
        public const int ek_Enchant = 8;
        public const int ek_Summon = 9;

        public const int ek_Disease = 10;
        public const int ek_Medicine = 11;
        public const int ek_Advantage = 12;
        public const int ek_Trap = 13;

        public EffectFlags(params int[] args)
            : base(args)
        {
        }
    }
}
