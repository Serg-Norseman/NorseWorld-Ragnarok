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

using System.Collections.Generic;
using BSLib;
using ZRLib.Core;
using NWR.Core.Types;

namespace NWR.Game
{
    public static class Hallucination
    {
        private static readonly IDictionary<int, int[]> Hashmap;

        static Hallucination()
        {
            Hashmap = new Dictionary<int, int[]>();
            Hashmap[(int)PlaceID.pid_Grass] = new int[] {
                PlaceID.pid_Grass,
                PlaceID.pid_Water,
                PlaceID.pid_Quicksand,
                PlaceID.pid_Space,
                PlaceID.pid_Floor
            };
            Hashmap[(int)PlaceID.pid_Tree] = new int[] {
                PlaceID.pid_Tree,
                PlaceID.pid_DeadTree,
                PlaceID.pid_Mountain,
                PlaceID.pid_Stalagmite,
                PlaceID.pid_Stump
            };
        }

        public static int GetPlaceID(int placeID)
        {
            int[] array = Hashmap.GetValueOrNull(placeID);
            if (array == null) {
                return placeID;
            } else {
                int idx = RandomHelper.GetRandom(array.Length);
                return array[idx];
            }
        }
    }
}
