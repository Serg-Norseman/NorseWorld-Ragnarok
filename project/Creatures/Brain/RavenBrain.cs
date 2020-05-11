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
using NWR.Game.Types;
using ZRLib.Core;
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain
{
    using Services = EnumSet<Service>;

    /// <summary>
    /// 
    /// </summary>
    public sealed class RavenBrain : SentientBrain
    {
        public override Services AvailableServices
        {
            get {
                return new Services();
            }
        }

        public RavenBrain(CreatureEntity owner) : base(owner)
        {
        }

        protected override void PrepareGoals()
        {
            base.PrepareGoals();
            DefineGoal(GoalKind.gk_PlayerFind);
        }

        protected override void EvaluateGoal(GoalEntity goal)
        {
            switch (goal.Kind) {
                case GoalKind.gk_PlayerFind:
                    goal.Value = 0.8f;
                    break;

                default:
                    base.EvaluateGoal(goal);
                    break;
            }
        }
    }
}
