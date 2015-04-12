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

import java.util.HashMap;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum PartyFormation
{
    pfWedge(0),
    pfRing(1),
    pfSquare(2),
    pfChain(3);

    public static final int pfFirst = 0;
    public static final int pfLast = 3;

    private final int intValue;
    private static HashMap<Integer, PartyFormation> mappings;

    private static HashMap<Integer, PartyFormation> getMappings()
    {
        synchronized (PartyFormation.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private PartyFormation(int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static PartyFormation forValue(int value)
    {
        return getMappings().get(value);
    }
}
