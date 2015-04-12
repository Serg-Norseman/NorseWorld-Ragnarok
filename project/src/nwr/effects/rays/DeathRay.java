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
import nwr.core.Locale;
import nwr.core.RS;
import nwr.effects.EffectID;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class DeathRay extends EffectRay
{
    public DeathRay()
    {
    }

    @Override
    public void TileProc(int aX, int aY, RefObject<Boolean> refContinue)
    {
        NWField f = this.Field;

        this.Step(aX, aY);

        if (f.isBarrier(aX, aY)) {
            refContinue.argValue = false;
        } else {
            NWCreature c = (NWCreature) f.findCreature(aX, aY);
            if (c != null && c.hasAffect(EffectID.eid_Death)) {
                RaceID race = c.getEntry().Race;
                if (race == RaceID.crDefault || race == RaceID.crHuman) {
                    if (c.isPlayer()) {
                        c.death(Locale.getStr(RS.rs_KilledByDeathRay), null);
                    } else {
                        c.death(Locale.format(RS.rs_TheXIsDestroyed, new Object[]{c.getName()}), null);
                    }
                }
            }
        }
    }
}
