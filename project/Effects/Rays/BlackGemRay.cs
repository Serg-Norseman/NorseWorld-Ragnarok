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
using NWR.Creatures;
using NWR.Game;
using NWR.Universe;
using NWR.Core.Types;

namespace NWR.Effects.Rays
{
    public sealed class BlackGemRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool  refContinue)
        {
            NWField f = Field;

            if (f.IsBarrier(aX, aY)) {
                refContinue = false;
            } else {
                Step(aX, aY);

                NWCreature c = (NWCreature)f.FindCreature(aX, aY);
                if (c != null) {
                    int val = RandomHelper.GetBoundedRnd(25, 40);
                    if (c.HPCur - val < 0) {
                        val = c.HPCur;
                    }
                    c.ApplyDamage(val, DamageKind.dkPhysical, null, "");
                    if (Creature.Items.FindByCLSID(GlobalVars.iid_SoulTrapping_Ring) == null) {
                        Creature.HPCur = Creature.HPCur + val;
                    }
                    // TODO: messages!!!
                }
                refContinue = (c == null);
            }
        }
    }
}
