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

using ZRLib.Core;

namespace NWR.Core.Types
{
    public sealed class BodypartAbilities : FlagSet
    {
        public const int bpa_Sight = 0;
        public const int bpa_Smell = 1;
        public const int bpa_Hearing = 2;
        public const int bpa_Eat = 3;
        public const int bpa_Shouts = 4;
        public const int bpa_Wield = 5;
        public const int bpa_Pickup = 6;
        public const int bpa_Attack = 7;
        public const int bpa_HasMetabolism = 8;
        public const int bpa_HasLungs = 9;
        public const int bpa_Duplicates = 10;
        public const int bpa_Explodes = 11;
        public const int bpa_HeatFeels = 12;
        public const int bpa_Poisons = 13;
        public const int bpa_Telepathy = 14;
        public const int bpa_Drains = 15;
        public const int bpa_Regenerate = 16;

        public BodypartAbilities(params int[] args)
            : base(args)
        {
        }
    }
}
