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

namespace NWR.Core.Types
{
    public sealed class PlaceFlags : FlagSet
    {
        public const int psBackground = 0;
        public const int psForeground = 1;
        public const int psMask = 2;
        public const int psVarDirect = 3;
        public const int psIsTrap = 4;
        public const int psIsFreeGate = 5;
        public const int psIsFixGate = 6;
        public const int psIsBuilding = 7;
        public const int psIsGround = 8;
        public const int psBlockLOS = 9;
        public const int psBarrier = 10;
        public const int psCanCreate = 11;
        public const int psNotInLegend = 12;

        public PlaceFlags(params int[] args)
            : base(args)
        {
        }
    }
}
