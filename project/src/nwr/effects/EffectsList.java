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
package nwr.effects;

import jzrlib.core.GameEntity;
import jzrlib.core.EntityList;
import jzrlib.utils.Logger;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class EffectsList extends EntityList
{
    public EffectsList(Object owner, boolean ownsObjects)
    {
        super(owner, ownsObjects);
    }

    @Override
    public final Effect getItem(int index)
    {
        return (Effect) super.getItem(index);
    }

    public final int Add(Effect effect)
    {
        Effect ef = this.findEffectByID(EffectID.forValue(effect.CLSID));
        int result;
        if (ef == null || !ef.assign(effect)) {
            result = super.add(effect);
        } else {
            effect.dispose();
            result = super.indexOf(ef);
        }
        return result;
    }

    public final Effect findEffectByID(EffectID eid)
    {
        int num = super.getCount();
        for (int i = 0; i < num; i++) {
            Effect ef = this.getItem(i);
            if (ef.CLSID == eid.getValue()) {
                return ef;
            }
        }

        return null;
    }

    public final int EffectBySource(GameEntity source)
    {
        int num = super.getCount();
        for (int i = 0; i < num; i++) {
            if (this.getItem(i).Source == source) {
                return i;
            }
        }

        return -1;
    }

    public final void execute()
    {
        try {
            for (int i = super.getCount() - 1; i >= 0; i--) {
                Effect eff = this.getItem(i);

                eff.execute();

                if (eff.Action != EffectAction.ea_Instant) {
                    if (eff.Duration > 0) {
                        int oldDuration = eff.Duration;
                        EffectID eid = EffectID.forValue(eff.CLSID);

                        EffectFlags flags = EffectsData.dbEffects[eid.getValue()].Flags;
                        if (flags.contains(EffectFlags.ep_Decrease)) {
                            eff.Duration--;
                        } else if (flags.contains(EffectFlags.ep_Increase)) {
                            eff.Duration++;
                        }

                        eff.Magnitude = Math.round((float) eff.Magnitude * ((float) eff.Duration / (float) oldDuration));

                        if (eff.Source == null && eff.Duration == 0) {
                            // e_Prowling has change effects list, 
                            // so we need check current effect
                            // its hack and bad code!
                            if (this.getItem(i) == eff) {
                                this.delete(i);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("EffectsList.execute(): " + ex.getMessage());
        }
    }
    
    @Override
    public void delete(int index)
    {
        super.delete(index);
    }

    @Override
    public int remove(GameEntity entity)
    {
        return super.remove(entity);
    }
}
