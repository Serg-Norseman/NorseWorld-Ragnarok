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
import nwr.core.types.ItemState;
import nwr.effects.EffectAction;
import nwr.effects.EffectID;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class StoningRay extends EffectRay
{
    public StoningRay()
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
            if (c != null && c.hasAffect(EffectID.eid_Stoning)) {
                c.addEffect(EffectID.eid_Stoning, ItemState.is_Normal, EffectAction.ea_Persistent, true, "");
            }
        }
    }
}
