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
package nwr.effects.rays;

import jzrlib.utils.RefObject;
import nwr.creatures.NWCreature;
import nwr.core.types.RaceID;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class CancellationRay extends EffectRay
{
    public CancellationRay()
    {
    }

    @Override
    public void TileProc(int aX, int aY, RefObject<Boolean> aContinue)
    {
        NWField f = this.Field;
        this.Step(aX, aY);
        if (f.isBarrier(aX, aY)) {
            aContinue.argValue = false;
        } else {
            NWCreature c = (NWCreature) f.findCreature(aX, aY);
            if (c != null) {
                RaceID race = c.getEntry().Race;
                if (race == RaceID.crDefault || race == RaceID.crHuman) {
                    c.clearAbilities();
                    c.clearSkills();
                }
            }
        }
    }
}
