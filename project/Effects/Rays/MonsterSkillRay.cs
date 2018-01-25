/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using BSLib;
using ZRLib.Core;
using NWR.Creatures;
using NWR.Items;
using NWR.Game;
using NWR.Universe;

namespace NWR.Effects.Rays
{
    public sealed class MonsterSkillRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool  aContinue)
        {
            NWField fld = Field;

            Step(aX, aY);

            if (fld.IsBarrier(aX, aY)) {
                aContinue = false;
            } else {
                NWCreature c = (NWCreature)fld.FindCreature(aX, aY);
                aContinue = true;
                if (c != null) {
                    if (c.HasAffect(EffID)) {
                        MNXRange dmg = EffectsData.dbEffects[(int)EffID].Damage;
                        c.ApplyDamage(RandomHelper.GetBoundedRnd(dmg.Min, dmg.Max), DmgKind, null, "");
                    }
                    if (EffID == EffectID.eid_Slinn_Gout) {
                        c.Luck--;
                    } else {
                        if (EffID == EffectID.eid_Ellegiant_Throw) {
                            aContinue = false;
                            Item item = new Item(GameSpace.Instance, fld);
                            item.CLSID = GlobalVars.nwrDB.FindEntryBySign("Boulder").GUID;
                            item.Count = 1;
                            item.SetPos(aX, aY);
                            item.Owner = fld;
                            fld.Items.Add(item, false);
                        }
                    }
                }
            }
        }
    }
}
