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

using BSLib;
using ZRLib.Core.Brain;
using NWR.Game;

namespace NWR.Creatures.Brain.Goals
{
    public class AreaGuardGoal : AreaGoal
    {
        public override byte SerializeKind
        {
            get {
                return StaticData.SID_AREAGUARD_GOAL;
            }
        }

        public AreaGuardGoal(BrainEntity owner)
            : base(owner)
        {
        }

        public override void Execute()
        {
            NWCreature self = (NWCreature)Self;

            ExtRect r = Area.Clone();
            r.Inflate(-1, -1);

            if (!r.Contains(self.PosX, self.PosY)) {
                ExtPoint pos = new ExtPoint();
                pos.X = RandomHelper.GetBoundedRnd(r.Left, r.Right);
                pos.Y = RandomHelper.GetBoundedRnd(r.Top, r.Bottom);
                ExtPoint next = self.GetStep(pos);
                if (!next.IsEmpty) {
                    Brain.StepTo(next.X, next.Y);
                }
            }

            IsComplete = false;
            //refComplete.argValue = this.Area.Contains(self.getPosX(), self.getPosY());
        }
    }
}
