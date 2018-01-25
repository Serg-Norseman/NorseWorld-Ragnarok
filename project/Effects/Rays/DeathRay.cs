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

using NWR.Core;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Universe;
using ZRLib.Core;

namespace NWR.Effects.Rays
{
    public sealed class DeathRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool  refContinue)
        {
            NWField f = Field;

            Step(aX, aY);

            if (f.IsBarrier(aX, aY)) {
                refContinue = false;
            } else {
                NWCreature c = (NWCreature)f.FindCreature(aX, aY);
                if (c != null && c.HasAffect(EffectID.eid_Death)) {
                    RaceID race = c.Entry.Race;
                    if (race == RaceID.crDefault || race == RaceID.crHuman) {
                        if (c.IsPlayer) {
                            c.Death(BaseLocale.GetStr(RS.rs_KilledByDeathRay), null);
                        } else {
                            c.Death(BaseLocale.Format(RS.rs_TheXIsDestroyed, new object[]{ c.Name }), null);
                        }
                    }
                }
            }
        }
    }
}
