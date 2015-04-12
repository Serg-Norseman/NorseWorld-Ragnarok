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
package nwr.creatures.brain.goals;

import jzrlib.core.brain.BrainEntity;
import jzrlib.core.CreatureEntity;
import nwr.creatures.brain.AttackRisk;
import nwr.creatures.brain.NWGoalEntity;
import nwr.creatures.NWCreature;
import nwr.core.types.CreatureState;
import nwr.core.types.RaceID;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class EnemyChaseGoal extends NWGoalEntity
{
    public CreatureEntity Enemy;
    public AttackRisk Risk;
    public boolean CanMove;

    public EnemyChaseGoal(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public void execute()
    {
        NWCreature self = (NWCreature) super.getSelf();
        NWCreature enemy = (NWCreature) this.Enemy;

        boolean ex = ((NWField) self.getCurrentMap()).getCreatures().indexOf(enemy) >= 0;
        RaceID race = self.getEntry().Race;
        boolean los = (race == RaceID.crDefault || race == RaceID.crHuman);

        this.IsComplete = (!ex || enemy.getState() == CreatureState.csDead || !self.isAvailable(enemy, los));
        if (!this.IsComplete) {
            if (!this.CanMove) {
                this.getBrain().attack(enemy, true);
            } else {
                this.getBrain().attack(enemy, this.Risk == AttackRisk.ar_Wary);
            }

            this.IsComplete = (enemy.getState() == CreatureState.csDead);
        }
    }
}
