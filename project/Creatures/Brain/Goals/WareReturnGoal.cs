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

using System;
using ZRLib.Core;
using ZRLib.Core.Brain;
using NWR.Items;
using NWR.Universe;

namespace NWR.Creatures.Brain.Goals
{
    public sealed class WareReturnGoal : NWGoalEntity
    {
        public Item Ware;

        public WareReturnGoal(BrainEntity owner)
            : base(owner)
        {
        }

        public override void Execute()
        {
            NWCreature self = (NWCreature)Self;

            try {
                Building house = self.FindHouse();
                if (house.Area.Contains(self.PosX, self.PosY)) {
                    self.DropItem(Ware);
                    IsComplete = true;
                } else {
                    IsComplete = false;
                }
            } catch (Exception ex) {
                Logger.Write("WareReturnGoal.execute(): " + ex.Message);
            }
        }
    }
}
