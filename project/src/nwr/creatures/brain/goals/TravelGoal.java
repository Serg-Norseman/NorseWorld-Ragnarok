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
import jzrlib.core.Point;
import nwr.creatures.NWCreature;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class TravelGoal extends LocatedGoal
{
    public TravelGoal(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public void execute()
    {
        NWCreature self = (NWCreature) super.getSelf();

        Point next = self.getStep(this.Position);
        if (next != null) {
            this.getBrain().stepTo(next.X, next.Y);
        }
        this.IsComplete = (self.getLocation().equals(this.Position));
    }
}
