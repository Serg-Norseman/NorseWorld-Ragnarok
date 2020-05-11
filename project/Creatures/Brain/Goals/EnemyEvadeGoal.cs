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
using ZRLib.Core;
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain.Goals
{
    public class EnemyEvadeGoal : NWGoalEntity
    {
        public CreatureEntity Enemy;
        public AttackRisk Risk;
        public bool CanMove;

        public EnemyEvadeGoal(BrainEntity owner)
            : base(owner)
        {
        }

        public override void Execute()
        {
            NWCreature self = (NWCreature)Self;

            bool ex = (self.CurrentField).Creatures.IndexOf(Enemy) >= 0;

            if (ex) {
                if (!CanMove) {
                    Brain.Attack(Enemy, true);
                } else {
                    int chn = RandomHelper.GetRandom(5); // 20% chance
                    if (chn == 0) {
                        if (Risk == AttackRisk.Evade) {
                            Brain.Attack(Enemy, true);
                        }
                    } else {
                        ExtPoint res = Brain.GetEvadePos(Enemy);
                        if (!res.IsEmpty) {
                            Brain.StepTo(res.X, res.Y);
                        }
                    }
                }
            }

            IsComplete = (!ex || !self.IsAvailable(Enemy, true));
        }
    }
}
