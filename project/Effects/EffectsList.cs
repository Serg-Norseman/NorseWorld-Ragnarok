/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih.
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

using System;
using ZRLib.Core;

namespace NWR.Effects
{
    public sealed class EffectsList : EntityList
    {
        public EffectsList(object owner, bool ownsObjects)
            : base(owner, ownsObjects)
        {
        }

        public new Effect GetItem(int index)
        {
            return (Effect)base.GetItem(index);
        }

        public int Add(Effect effect)
        {
            Effect ef = FindEffectByID((EffectID)effect.CLSID_Renamed);
            int result;
            if (ef == null || !ef.Assign(effect)) {
                result = base.Add(effect);
            } else {
                effect.Dispose();
                result = IndexOf(ef);
            }
            return result;
        }

        public Effect FindEffectByID(EffectID eid)
        {
            int num = Count;
            for (int i = 0; i < num; i++) {
                Effect ef = GetItem(i);
                if (ef.CLSID_Renamed == (int)eid) {
                    return ef;
                }
            }

            return null;
        }

        public int EffectBySource(GameEntity source)
        {
            int num = Count;
            for (int i = 0; i < num; i++) {
                if (GetItem(i).Source == source) {
                    return i;
                }
            }

            return -1;
        }

        public void Execute()
        {
            try {
                for (int i = Count - 1; i >= 0; i--) {
                    Effect eff = GetItem(i);

                    eff.Execute();

                    if (eff.Action != EffectAction.ea_Instant) {
                        if (eff.Duration > 0) {
                            int oldDuration = eff.Duration;
                            EffectID eid = (EffectID)eff.CLSID_Renamed;

                            EffectFlags flags = EffectsData.dbEffects[(int)eid].Flags;
                            if (flags.Contains(EffectFlags.ep_Decrease)) {
                                eff.Duration--;
                            } else if (flags.Contains(EffectFlags.ep_Increase)) {
                                eff.Duration++;
                            }

                            eff.Magnitude = (int)Math.Round((float)eff.Magnitude * ((float)eff.Duration / (float)oldDuration));

                            if (eff.Source == null && eff.Duration == 0) {
                                // e_Prowling has change effects list, 
                                // so we need check current effect
                                // its hack and bad code!
                                if (GetItem(i) == eff) {
                                    Delete(i);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("EffectsList.execute(): " + ex.Message);
            }
        }

        public override void Delete(int index)
        {
            base.Delete(index);
        }

        public override int Remove(GameEntity entity)
        {
            return base.Remove(entity);
        }
    }
}
