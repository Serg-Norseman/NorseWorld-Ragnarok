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

using ZRLib.Core;
using ZRLib.Core.Brain;
using NWR.Core.Types;
using NWR.Universe;

namespace NWR.Creatures.Brain.Goals
{
    /// <summary>
    /// 
    /// </summary>
    public class EnemyChaseGoal : NWGoalEntity
    {
        public CreatureEntity Enemy;
        public AttackRisk Risk;
        public bool CanMove;

        public EnemyChaseGoal(BrainEntity owner)
            : base(owner)
        {
        }

        public override void Execute()
        {
            NWCreature self = (NWCreature)Self;
            NWCreature enemy = (NWCreature)Enemy;

            bool ex = ((NWField)self.CurrentMap).Creatures.IndexOf(enemy) >= 0;
            RaceID race = self.Entry.Race;
            bool los = (race == RaceID.crDefault || race == RaceID.crHuman);

            IsComplete = (!ex || enemy.State == CreatureState.csDead || !self.IsAvailable(enemy, los));
            if (!IsComplete) {
                if (!CanMove) {
                    Brain.Attack(enemy, true);
                } else {
                    Brain.Attack(enemy, Risk == AttackRisk.ar_Wary);
                }

                IsComplete = (enemy.State == CreatureState.csDead);
            }
        }
    }
}
