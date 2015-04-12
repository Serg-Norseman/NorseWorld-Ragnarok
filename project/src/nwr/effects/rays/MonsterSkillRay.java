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
import jzrlib.core.GameSpace;
import jzrlib.core.Range;
import jzrlib.utils.RefObject;
import nwr.creatures.NWCreature;
import nwr.effects.EffectID;
import nwr.effects.EffectsData;
import nwr.item.Item;
import nwr.main.GlobalVars;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class MonsterSkillRay extends EffectRay
{
    public MonsterSkillRay()
    {
    }

    @Override
    public void TileProc(int aX, int aY, RefObject<Boolean> aContinue)
    {
        NWField fld = this.Field;

        this.Step(aX, aY);

        if (fld.isBarrier(aX, aY)) {
            aContinue.argValue = false;
        } else {
            NWCreature c = (NWCreature) fld.findCreature(aX, aY);
            aContinue.argValue = true;
            if (c != null) {
                if (c.hasAffect(this.EffID)) {
                    Range dmg = EffectsData.dbEffects[this.EffID.getValue()].Damage;
                    c.applyDamage(AuxUtils.getBoundedRnd(dmg.Min, dmg.Max), this.DmgKind, null, "");
                }
                if (this.EffID == EffectID.eid_Slinn_Gout) {
                    c.Luck--;
                } else {
                    if (this.EffID == EffectID.eid_Ellegiant_Throw) {
                        aContinue.argValue = false;
                        Item item = new Item(GameSpace.getInstance(), fld);
                        item.setCLSID(GlobalVars.nwrBase.findEntryBySign("Boulder").GUID);
                        item.Count = 1;
                        item.setPos(aX, aY);
                        item.Owner = fld;
                        fld.getItems().add(item, false);
                    }
                }
            }
        }
    }
}
