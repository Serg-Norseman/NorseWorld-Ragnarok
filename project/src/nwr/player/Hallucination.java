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
package nwr.player;

import jzrlib.utils.AuxUtils;
import nwr.core.types.PlaceID;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public final class Hallucination
{
    private static final Map<Integer, int[]> hashmap;

    static {
        hashmap = new HashMap<>();
        hashmap.put(PlaceID.pid_Grass, new int[] {PlaceID.pid_Grass, PlaceID.pid_Water, PlaceID.pid_Quicksand, PlaceID.pid_Space, PlaceID.pid_Floor});
        hashmap.put(PlaceID.pid_Tree, new int[] {PlaceID.pid_Tree, PlaceID.pid_DeadTree, PlaceID.pid_Mountain, PlaceID.pid_Stalagmite, PlaceID.pid_Stump});
    }
    
    public static int getPlaceID(int placeID)
    {
        int[] array = hashmap.get(placeID);
        if (array == null) {
            return placeID;
        } else {
            int idx = AuxUtils.getRandom(array.length);
            return array[idx];
        }
    }
}
