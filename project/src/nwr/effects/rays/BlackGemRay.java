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

import jzrlib.utils.AuxUtils;
import jzrlib.utils.RefObject;
import nwr.core.types.DamageKind;
import nwr.creatures.NWCreature;
import nwr.main.GlobalVars;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class BlackGemRay extends EffectRay
{
    public BlackGemRay()
    {
    }

    @Override
    public void TileProc(int aX, int aY, RefObject<Boolean> refContinue)
    {
        NWField f = this.Field;

        if (f.isBarrier(aX, aY)) {
            refContinue.argValue = false;
        } else {
            this.Step(aX, aY);

            NWCreature c = (NWCreature) f.findCreature(aX, aY);
            if (c != null) {
                int val = AuxUtils.getBoundedRnd(25, 40);
                if (c.HPCur - val < 0) {
                    val = c.HPCur;
                }
                c.applyDamage(val, DamageKind.dkPhysical, null, "");
                if (this.Creature.getItems().findByCLSID(GlobalVars.iid_SoulTrapping_Ring) == null) {
                    this.Creature.HPCur = this.Creature.HPCur + val;
                }
                // TODO: messages!!!
            }
            refContinue.argValue = (c == null);
        }
    }
}
