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

using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Game;
using NWR.Universe;
using ZRLib.Core;

namespace NWR.Effects.Rays
{
    public sealed class GrapplingHookRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool  aContinue)
        {
            NWCreature self = Creature;
            NWField f = Field;
            if (f.IsBarrier(aX, aY)) {
                aContinue = false;
            } else {
                NWCreature c = (NWCreature)f.FindCreature(aX, aY);
                if (c != null) {
                    c.ApplyDamage(RandomHelper.GetBoundedRnd(9, 17), DamageKind.dkPhysical, null, "");
                } else {
                    self.CheckTile(false);
                    self.SetPos(aX, aY);
                    self.CheckTile(true);

                    Effect ef = self.Effects.FindEffectByID(EffectID.eid_PitTrap);
                    if (ef != null) {
                        self.Effects.Remove(ef);
                    } else {
                        ef = self.Effects.FindEffectByID(EffectID.eid_Quicksand);
                        if (ef != null) {
                            self.Effects.Remove(ef);
                        }
                    }
                    GlobalVars.nwrWin.ShowText(self, BaseLocale.GetStr(RS.rs_YouPullForward));
                }
                aContinue = false;
            }
        }
    }
}
