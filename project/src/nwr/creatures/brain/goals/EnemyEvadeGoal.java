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
import jzrlib.utils.AuxUtils;
import jzrlib.core.Point;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class EnemyEvadeGoal extends NWGoalEntity
{
    public CreatureEntity Enemy;
    public AttackRisk Risk;
    public boolean CanMove;

    public EnemyEvadeGoal(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public void execute()
    {
        NWCreature self = (NWCreature) super.getSelf();

        boolean ex = (self.getCurrentField()).getCreatures().indexOf(this.Enemy) >= 0;

        if (ex) {
            if (!this.CanMove) {
                this.getBrain().attack(this.Enemy, true);
            } else {
                int chn = AuxUtils.getRandom(5); // 20% chance
                if (chn == 0) {
                    if (this.Risk == AttackRisk.ar_Evade) {
                        this.getBrain().attack(this.Enemy, true);
                    }
                } else {
                    Point res = this.getBrain().getEvadePos(this.Enemy);
                    if (res != null) {
                        this.getBrain().stepTo(res.X, res.Y);
                    }
                }
            }
        }

        this.IsComplete = (!ex || !self.isAvailable(this.Enemy, true));
    }
}
