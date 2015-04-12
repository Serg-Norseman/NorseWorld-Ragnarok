/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
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
package nwr.creatures.brain;

import jzrlib.utils.AuxUtils;
import jzrlib.core.BytesSet;
import jzrlib.core.CreatureEntity;
import jzrlib.core.brain.Emitter;
import jzrlib.core.brain.GoalEntity;
import nwr.creatures.brain.goals.AreaGuardGoal;
import nwr.creatures.brain.goals.EscortGoal;
import nwr.creatures.brain.goals.PointGuardGoal;
import nwr.creatures.brain.goals.TravelGoal;
import nwr.creatures.NWCreature;
import jzrlib.map.AbstractMap;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class WarriorBrain extends SentientBrain
{
    public WarriorBrain(CreatureEntity owner)
    {
        super(owner);
        super.Emitters = new BytesSet((byte) EmitterKind.ek_GuardAlarm);
    }

    private void setAlarm()
    {
        NWCreature self = ((NWCreature) super.fSelf);
        if ((super.isTownsman()) && !self.isMercenary()) {
            this.getEmitters().addEmitter((byte) EmitterKind.ek_GuardAlarm, self.UID, self.getLocation(), 10f, 2, true);
        }
    }

    @Override
    protected void prepareChase(CreatureEntity enemy, AttackRisk risk, boolean canMove)
    {
        super.prepareChase(enemy, risk, canMove);
        this.setAlarm();
    }

    @Override
    protected void prepareEvade(CreatureEntity enemy, AttackRisk risk, boolean canMove)
    {
        super.prepareEvade(enemy, risk, canMove);
        this.setAlarm();
    }

    @Override
    protected void prepareEmitter(Emitter emitter)
    {
        if (emitter.EmitterKind == EmitterKind.ek_GuardAlarm) {
            TravelGoal goal = (TravelGoal) super.createGoal(GoalKind.gk_Travel);
            goal.Position = emitter.Position;
            goal.EmitterID = emitter.UID;
            goal.Duration = Math.round((float) AuxUtils.distance(emitter.Position, super.fSelf.getLocation()) * 1.5f);
        }
    }

    @Override
    protected void prepareGoals()
    {
        super.prepareGoals();
    }

    @Override
    protected void evaluateGoal(GoalEntity goal)
    {
        if (this.IsShipSail && (goal.Kind != GoalKind.gk_EnemyChase && goal.Kind != GoalKind.gk_EnemyEvade)) {
            goal.Value = -1.0f;
            return;
        }

        NWCreature self = (NWCreature) super.fSelf;

        if (self.isMercenary()) {
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

                case GoalKind.gk_Escort: {
                    int dist = AuxUtils.distance(self.getLocation(), ((EscortGoal) goal).Position);
                    goal.Value = ((0.3f + (float) dist / 20.0f));
                    break;
                }

                default:
                    super.evaluateGoal(goal);
                    break;
            }
        } else {
            switch (goal.Kind) {
                case GoalKind.gk_Travel: {
                    // ekGuardAlarm
                    if (goal.EmitterID != 0) {
                        goal.Value = 0.55f;
                    }
                    break;
                }

                case GoalKind.gk_PointGuard: {
                    int dist = AuxUtils.distance(self.getLocation(), ((PointGuardGoal) goal).Position);
                    goal.Value = (goal.Value + (float) dist / 10.0f * 0.75f);
                    break;
                }

                case GoalKind.gk_AreaGuard: {
                    int dist = AuxUtils.calcDistanceToArea(self.getLocation(), ((AreaGuardGoal) goal).Area);
                    goal.Value = ((goal.Value + (float) dist / 100.0f));
                    break;
                }

                default:
                    super.evaluateGoal(goal);
                    break;
            }
        }
    }
}
