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
package nwr.creatures.specials;

import jzrlib.core.ExtList;
import nwr.creatures.NWCreature;
import nwr.game.NWGameSpace;
import nwr.universe.NWField;
import nwr.universe.NWTile;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class ArticulateCreature extends NWCreature
{
    protected ExtList<ArticulateSegment> fSegments;

    public final int getSize()
    {
        return this.fSegments.getCount();
    }

    public final ArticulateSegment getSegment(int index)
    {
        ArticulateSegment result = null;
        if (index >= 0 && index < this.fSegments.getCount()) {
            result = (ArticulateSegment) this.fSegments.get(index);
        }
        return result;
    }

    public final ArticulateSegment getHead()
    {
        return this.getSegment(0);
    }

    protected ArticulateSegment createSegment()
    {
        return new ArticulateSegment();
    }

    public ArticulateCreature(NWGameSpace space, Object owner, int creatureID, boolean total, boolean setName)
    {
        super(space, owner, creatureID, total, setName);
        this.fSegments = new ExtList<>(true);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fSegments.dispose();
        }
        super.dispose(disposing);
    }

    public final ArticulateSegment add()
    {
        ArticulateSegment result = this.createSegment();
        this.fSegments.add(result);
        return result;
    }

    @Override
    public void checkTile(boolean aHere)
    {
        NWField map = (NWField) super.getCurrentMap();
        if (map == null) {
            return;
        }

        int num = this.fSegments.getCount();
        for (int i = 0; i < num; i++) {
            ArticulateSegment seg = this.getSegment(i);
            NWTile tile = (NWTile) map.getTile(seg.X, seg.Y);

            if (tile != null) {
                if (aHere) {
                    tile.CreaturePtr = this;
                } else {
                    tile.CreaturePtr = null;
                }
            }
        }
    }

    public final int findByPos(int aX, int aY)
    {
        int num = this.fSegments.getCount();
        for (int i = 0; i < num; i++) {
            ArticulateSegment seg = this.getSegment(i);
            if (seg.X == aX && seg.Y == aY) {
                return i;
            }
        }

        return -1;
    }
}
