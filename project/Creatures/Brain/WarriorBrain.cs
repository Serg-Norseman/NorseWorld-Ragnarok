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
using NWR.Creatures.Brain.Goals;
using ZRLib.Core;
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain
{
    public sealed class WarriorBrain : SentientBrain
    {
        public WarriorBrain(CreatureEntity owner)
            : base(owner)
        {
            EmittersX = new BytesSet((sbyte)EmitterKind.ek_GuardAlarm);
        }

        private void SetAlarm()
        {
            NWCreature self = ((NWCreature)fSelf);
            if ((Townsman) && !self.Mercenary) {
                Emitters.AddEmitter(EmitterKind.ek_GuardAlarm, self.UID, self.Location, 10f, 2, true);
            }
        }

        protected override void PrepareChase(CreatureEntity enemy, AttackRisk risk, bool canMove)
        {
            base.PrepareChase(enemy, risk, canMove);
            SetAlarm();
        }

        protected override void PrepareEvade(CreatureEntity enemy, AttackRisk risk, bool canMove)
        {
            base.PrepareEvade(enemy, risk, canMove);
            SetAlarm();
        }

        protected override void PrepareEmitter(Emitter emitter)
        {
            if (emitter.EmitterKind == EmitterKind.ek_GuardAlarm) {
                TravelGoal goal = (TravelGoal)CreateGoal(GoalKind.gk_Travel);
                goal.Position = emitter.Position;
                goal.EmitterID = emitter.UID;
                goal.Duration = (int)Math.Round(MathHelper.Distance(emitter.Position, fSelf.Location) * 1.5f);
            }
        }

        protected override void PrepareGoals()
        {
            base.PrepareGoals();
        }

        protected override void EvaluateGoal(GoalEntity goal)
        {
            if (IsShipSail && (goal.Kind != GoalKind.gk_EnemyChase && goal.Kind != GoalKind.gk_EnemyEvade)) {
                goal.Value = -1.0f;
                return;
            }

            NWCreature self = (NWCreature)fSelf;

            if (self.Mercenary) {
                switch (goal.Kind) {
                    case GoalKind.gk_ItemAcquire:
                        goal.Value = 0.5f;
                        break;

                    case GoalKind.gk_EnemyChase:
                        goal.Value = 0.6f;
                        break;

                    case GoalKind.gk_EnemyEvade:
                        goal.Value = 0.25f;
                        break;

                    case GoalKind.gk_Escort:
                        {
                            int dist = MathHelper.Distance(self.Location, ((EscortGoal)goal).Position);
                            goal.Value = ((0.3f + dist / 20.0f));
                            break;
                        }

                    default:
                        base.EvaluateGoal(goal);
                        break;
                }
            } else {
                switch (goal.Kind) {
                    case GoalKind.gk_Travel:
                        {
                            // ekGuardAlarm
                            if (goal.EmitterID != 0) {
                                goal.Value = 0.55f;
                            }
                            break;
                        }

                    case GoalKind.gk_PointGuard:
                        {
                            int dist = MathHelper.Distance(self.Location, ((PointGuardGoal)goal).Position);
                            goal.Value = (goal.Value + dist / 10.0f * 0.75f);
                            break;
                        }

                    case GoalKind.gk_AreaGuard:
                        {
                            int dist = AuxUtils.CalcDistanceToArea(self.Location, ((AreaGuardGoal)goal).Area);
                            goal.Value = ((goal.Value + dist / 100.0f));
                            break;
                        }

                    default:
                        base.EvaluateGoal(goal);
                        break;
                }
            }
        }
    }
}
