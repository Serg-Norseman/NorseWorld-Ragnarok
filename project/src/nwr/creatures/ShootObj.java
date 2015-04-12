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
package nwr.creatures;

import jzrlib.utils.RefObject;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ShootObj
{
    public NWCreature aCreature;
    public NWCreature aEnemy;

    public ShootObj()
    {
    }

    public final void LineProc(int aX, int aY, RefObject<Boolean> refContinue)
    {
        NWField f = (NWField) aCreature.getCurrentMap();
        if (f.isBarrier(aX, aY)) {
            refContinue.argValue = false;
        } else {
            NWCreature cr = (NWCreature) f.findCreature(aX, aY);
            if (cr == null) {
                refContinue.argValue = true;
            } else {
                refContinue.argValue = (cr.equals(this.aEnemy) || cr.equals(this.aCreature));
            }
        }
    }
}
