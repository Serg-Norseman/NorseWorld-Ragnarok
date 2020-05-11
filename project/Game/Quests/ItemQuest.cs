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

using NWR.Game.Story;
using NWR.Items;

namespace NWR.Game.Quests
{
    /// <summary>
    /// 
    /// </summary>
    public sealed class ItemQuest : Quest
    {
        public readonly int ItemID;

        public ItemQuest(NWGameSpace space, int itemID)
            : base(space)
        {
            ItemID = itemID;
        }

        protected override bool OnPickupItem(Item item)
        {
            return (item.CLSID == ItemID);
        }

        public override void Announce()
        {
            // FIXME: localize it
            fSpace.ShowText("You must find a {1}.");
        }
    }
}
