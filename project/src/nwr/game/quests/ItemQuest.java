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
package nwr.game.quests;

import nwr.game.NWGameSpace;
import nwr.game.story.Quest;
import nwr.item.Item;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.11.0
 */
public final class ItemQuest extends Quest
{
    public final int ItemID;

    public ItemQuest(NWGameSpace space, int itemID)
    {
        super(space);
        this.ItemID = itemID;
    }

    @Override
    protected boolean onPickupItem(Item item)
    {
        return (item.CLSID == ItemID);
    }

    @Override
    public void announce()
    {
        // FIXME: localize it
        this.fSpace.showText("You must find a %s.");
    }
}
