/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014, 2020 by Serg V. Zhdanovskih.
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

using BSLib;
using ZRLib.Core.Brain;
using NWR.Items;

namespace NWR.Creatures.Brain.Goals
{
    public class ItemAcquireGoal : LocatedGoal
    {
        public ItemAcquireGoal(BrainEntity owner)
            : base(owner)
        {
        }

        public override void Execute()
        {
            NWCreature self = (NWCreature)Self;
            ExtPoint iPos = Position;

            if (self.IsSeen(iPos.X, iPos.Y, true) && !(self.CurrentField.FindItem(iPos.X, iPos.Y) is Item)) {
                IsComplete = true;
            } else {
                if (MathHelper.Distance(self.Location, iPos) == 0) {
                    self.PickupAll();
                    IsComplete = true;
                } else {
                    ExtPoint next = self.GetStep(iPos);
                    if (!next.IsEmpty) {
                        Brain.StepTo(next.X, next.Y);
                    } else {
                        IsComplete = true;
                    }
                }
            }
        }
    }
}
