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
using BSLib;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain.Goals
{
    public sealed class ShopReturnGoal : LocatedGoal
    {
        public ShopReturnGoal(BrainEntity owner)
            : base(owner)
        {
        }

        public override void Execute()
        {
            NWCreature self = (NWCreature)Self;

            try {
                Building house = self.FindHouse();
                Door gp = null;
                int dist = 0;
                bool outside = false;
                house.IsNearestDoor(self.PosX, self.PosY, ref gp, ref dist, ref outside);

                if (outside && gp != null) {
                    if (dist == 1) {
                        Brain.StepTo(gp.X, gp.Y);
                    } else {
                        ExtPoint tempPos = new ExtPoint();
                        tempPos.X = gp.X + Directions.Data[gp.Dir].DX;
                        tempPos.Y = gp.Y + Directions.Data[gp.Dir].DY;
                        ExtPoint next = self.GetStep(tempPos);
                        if (!next.IsEmpty) {
                            Brain.StepTo(next.X, next.Y);
                        }
                    }
                } else {
                    ExtPoint next = self.GetStep(Position);
                    if (!next.IsEmpty) {
                        Brain.StepTo(next.X, next.Y);
                    }
                }

                IsComplete = self.Location.Equals(Position);
            } catch (Exception ex) {
                Logger.Write("ShopReturnGoal.execute(): " + ex.Message);
            }
        }
    }
}
