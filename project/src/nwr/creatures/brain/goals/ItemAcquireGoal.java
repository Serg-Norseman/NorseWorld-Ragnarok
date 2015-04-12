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

import jzrlib.utils.AuxUtils;
import jzrlib.core.brain.BrainEntity;
import jzrlib.core.Point;
import nwr.creatures.NWCreature;
import nwr.item.Item;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class ItemAcquireGoal extends LocatedGoal
{
    public ItemAcquireGoal(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public void execute()
    {
        NWCreature self = (NWCreature) super.getSelf();
        Point iPos = this.Position;

        if (self.isSeen(iPos.X, iPos.Y, true) && !(self.getCurrentMap().findItem(iPos.X, iPos.Y) instanceof Item)) {
            this.IsComplete = true;
        } else {
            if (AuxUtils.distance(self.getLocation(), iPos) == 0) {
                self.pickupAll();
                this.IsComplete = true;
            } else {
                Point next = self.getStep(iPos);
                if (next != null) {
                    this.getBrain().stepTo(next.X, next.Y);
                } else {
                    this.IsComplete = true;
                }
            }
        }
    }
}
