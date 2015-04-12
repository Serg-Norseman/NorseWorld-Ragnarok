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
import nwr.core.Locale;
import nwr.core.RS;
import nwr.effects.EffectID;
import nwr.effects.Effect;
import nwr.main.GlobalVars;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class GrapplingHookRay extends EffectRay
{
    public GrapplingHookRay()
    {
    }

    @Override
    public void TileProc(int aX, int aY, RefObject<Boolean> aContinue)
    {
        NWCreature self = this.Creature;
        NWField f = this.Field;
        if (f.isBarrier(aX, aY)) {
            aContinue.argValue = false;
        } else {
            NWCreature c = (NWCreature) f.findCreature(aX, aY);
            if (c != null) {
                c.applyDamage(AuxUtils.getBoundedRnd(9, 17), DamageKind.dkPhysical, null, "");
            } else {
                self.checkTile(false);
                self.setPos(aX, aY);
                self.checkTile(true);

                Effect ef = self.getEffects().findEffectByID(EffectID.eid_PitTrap);
                if (ef != null) {
                    self.getEffects().remove(ef);
                } else {
                    ef = self.getEffects().findEffectByID(EffectID.eid_Quicksand);
                    if (ef != null) {
                        self.getEffects().remove(ef);
                    }
                }
                GlobalVars.nwrWin.showText(self, Locale.getStr(RS.rs_YouPullForward));
            }
            aContinue.argValue = false;
        }
    }
}
