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
import nwr.creatures.brain.NWGoalEntity;
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.main.GlobalVars;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.core.Point;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class DebtTakeGoal extends NWGoalEntity
{
    public CreatureEntity Debtor;

    public DebtTakeGoal(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public void execute()
    {
        NWCreature self = (NWCreature) super.getSelf();

        try {
            int dist = AuxUtils.distance(self.getLocation(), this.Debtor.getLocation());
            if (dist == 1) {
                GlobalVars.nwrWin.showText(self, Locale.getStr(RS.rs_GiveMeTheMoney));
                GlobalVars.nwrWin.showInventory(self);
            } else {
                Point next = self.getStep(this.Debtor.getLocation());
                if (next != null) {
                    this.getBrain().stepTo(next.X, next.Y);
                }
            }
        } catch (Exception ex) {
            Logger.write("TraderBrain.ExecuteDebtTakeGoal(): " + ex.getMessage());
        }
    }
}
