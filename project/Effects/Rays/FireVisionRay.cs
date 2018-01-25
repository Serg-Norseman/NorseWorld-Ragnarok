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
using NWR.Universe;

namespace NWR.Effects.Rays
{
    public sealed class FireVisionRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool aContinue)
        {
            NWField f = Field;
            if (f.IsBarrier(aX, aY)) {
                aContinue = false;
            } else {
                NWCreature c = (NWCreature)f.FindCreature(aX, aY);
                if (c != null) {
                    EffectsFactory.FireHit(EffectID.eid_FireVision, Creature, c, RandomHelper.GetBoundedRnd(2, 40));
                }
                aContinue = (c == null);
            }
        }
    }
}
