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
import jzrlib.utils.Logger;
import jzrlib.core.Point;
import jzrlib.utils.RefObject;
import nwr.creatures.NWCreature;
import jzrlib.core.Directions;
import nwr.universe.Door;
import nwr.universe.Building;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ShopReturnGoal extends LocatedGoal
{
    public ShopReturnGoal(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public void execute()
    {
        NWCreature self = (NWCreature) super.getSelf();

        try {
            Building house = (Building) self.findHouse();
            Door gp = null;
            int dist = 0;
            boolean outside = false;
            RefObject<Door> tempRef_gp = new RefObject<>(gp);
            RefObject<Integer> tempRef_dist = new RefObject<>(dist);
            RefObject<Boolean> tempRef_outside = new RefObject<>(outside);
            house.isNearestDoor(self.getPosX(), self.getPosY(), tempRef_gp, tempRef_dist, tempRef_outside);
            gp = tempRef_gp.argValue;
            dist = tempRef_dist.argValue;
            outside = tempRef_outside.argValue;
            if (outside && gp != null) {
                if (dist == 1) {
                    this.getBrain().stepTo(gp.X, gp.Y);
                } else {
                    Point temp_pos = new Point();
                    temp_pos.X = gp.X + Directions.Data[gp.Dir].dX;
                    temp_pos.Y = gp.Y + Directions.Data[gp.Dir].dY;
                    Point next = self.getStep(temp_pos);
                    if (next != null) {
                        this.getBrain().stepTo(next.X, next.Y);
                    }
                }
            } else {
                Point next = self.getStep(this.Position);
                if (next != null) {
                    this.getBrain().stepTo(next.X, next.Y);
                }
            }

            this.IsComplete = self.getLocation().equals(this.Position);
        } catch (Exception ex) {
            Logger.write("ShopReturnGoal.execute(): " + ex.getMessage());
        }
    }
}
