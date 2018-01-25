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

using ZRLib.Core;

namespace NWR.Effects
{
    public sealed class EffectParams : FlagSet
    {
        public const int ep_Place = 0;
        public const int ep_Direction = 1;
        public const int ep_Item = 2;
        public const int ep_Creature = 3;
        public const int ep_Area = 4;
        public const int ep_Land = 5;
        public const int ep_DunRoom = 6;
        public const int ep_MonsterID = 7;
        public const int ep_TileID = 8;
        public const int ep_ScrollID = 9;
        public const int ep_GodID = 10;
        public const int ep_ItemExt = 11;

        public const int ep_First = 0;
        public const int ep_Last = 11;

        public EffectParams(params int[] args)
            : base(args)
        {
        }
    }
}
