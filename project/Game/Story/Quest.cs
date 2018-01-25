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

using ZRLib.Map;
using NWR.Creatures;
using NWR.Items;
namespace NWR.Game.Story
{
    public class Quest
    {
        private bool fIsComplete = false;

        protected readonly NWGameSpace fSpace;

        public Quest(NWGameSpace space)
        {
            fSpace = space;
        }

        public virtual bool IsComplete
        {
            get { return fIsComplete; }
        }

        public virtual void Announce()
        {
            // dummy
        }

        public virtual string Description
        {
            get { return ""; }
        }

        public bool PickupItem(Item item)
        {
            if (OnPickupItem(item)) {
                CheckComplete();
            }
            return fIsComplete;
        }

        protected virtual bool OnPickupItem(Item item)
        {
            return false;
        }

        public bool GiveupItem(Item item, NWCreature target)
        {
            if (OnGiveupItem(item, target)) {
                CheckComplete();
            }
            return fIsComplete;
        }

        protected virtual bool OnGiveupItem(Item item, NWCreature target)
        {
            return false;
        }

        public bool KillMonster(NWCreature monster)
        {
            if (OnKillMonster(monster)) {
                CheckComplete();
            }
            return fIsComplete;
        }

        protected virtual bool OnKillMonster(NWCreature monster)
        {
            return false;
        }

        public bool EnterTile(BaseTile tile)
        {
            if (OnEnterTile(tile)) {
                CheckComplete();
            }
            return fIsComplete;
        }

        protected virtual bool OnEnterTile(BaseTile tile)
        {
            return false;
        }

        public virtual void CheckComplete()
        {
            if (fIsComplete) {
                return;
            }
            fIsComplete = true;

            //game.log.quest("You have completed your quest! Press \"q\" to exit the level.");
        }
    }
}
