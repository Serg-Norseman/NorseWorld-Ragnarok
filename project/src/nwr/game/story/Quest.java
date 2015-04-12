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
package nwr.game.story;

import jzrlib.map.BaseTile;
import nwr.creatures.NWCreature;
import nwr.game.NWGameSpace;
import nwr.item.Item;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.11.0
 */
public class Quest
{
    private boolean fIsComplete = false;

    protected final NWGameSpace fSpace;

    public Quest(NWGameSpace space)
    {
        this.fSpace = space;
    }
    
    public boolean getIsComplete()
    {
        return this.fIsComplete;
    }

    public void announce()
    {
        // dummy
    }

    public String getDescription()
    {
        return "";
    }

    public final boolean pickupItem(Item item)
    {
        if (this.onPickupItem(item)) {
            this.checkComplete();
        }
        return this.fIsComplete;
    }

    protected boolean onPickupItem(Item item)
    {
        return false;
    }

    public final boolean giveupItem(Item item, NWCreature target)
    {
        if (this.onGiveupItem(item, target)) {
            this.checkComplete();
        }
        return this.fIsComplete;
    }

    protected boolean onGiveupItem(Item item, NWCreature target)
    {
        return false;
    }

    public final boolean killMonster(NWCreature monster)
    {
        if (this.onKillMonster(monster)) {
            this.checkComplete();
        }
        return this.fIsComplete;
    }

    protected boolean onKillMonster(NWCreature monster)
    {
        return false;
    }

    public final boolean enterTile(BaseTile tile)
    {
        if (this.onEnterTile(tile)) {
            this.checkComplete();
        }
        return this.fIsComplete;
    }

    protected boolean onEnterTile(BaseTile tile)
    {
        return false;
    }

    public void checkComplete()
    {
        if (this.fIsComplete) {
            return;
        }
        this.fIsComplete = true;

        //game.log.quest("You have completed your quest! Press \"q\" to exit the level.");
    }
}
