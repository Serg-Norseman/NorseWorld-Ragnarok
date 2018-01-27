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

using NWR.Universe;

namespace NWR.Creatures
{
    public sealed class ShootObj
    {
        public NWCreature ACreature;
        public NWCreature AEnemy;

        public void LineProc(int aX, int aY, ref bool aContinue)
        {
            NWField f = (NWField)ACreature.CurrentMap;
            if (f.IsBarrier(aX, aY)) {
                aContinue = false;
            } else {
                NWCreature cr = (NWCreature)f.FindCreature(aX, aY);
                if (cr == null) {
                    aContinue = true;
                } else {
                    aContinue = (cr.Equals(AEnemy) || cr.Equals(ACreature));
                }
            }
        }
    }
}
