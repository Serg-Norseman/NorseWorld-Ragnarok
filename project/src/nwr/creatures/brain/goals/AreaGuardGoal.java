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
import jzrlib.core.Rect;
import nwr.creatures.NWCreature;
import nwr.core.StaticData;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class AreaGuardGoal extends AreaGoal
{
    public AreaGuardGoal(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public void execute()
    {
        NWCreature self = (NWCreature) super.getSelf();

        Rect r = this.Area.clone();
        r.inflate(-1, -1);

        if (!r.contains(self.getPosX(), self.getPosY())) {
            Point pos = new Point();
            pos.X = AuxUtils.getBoundedRnd(r.Left, r.Right);
            pos.Y = AuxUtils.getBoundedRnd(r.Top, r.Bottom);
            Point next = self.getStep(pos);
            if (next != null) {
                this.getBrain().stepTo(next.X, next.Y);
            }
        }

        this.IsComplete = false;
        //refComplete.argValue = this.Area.contains(self.getPosX(), self.getPosY());
    }

    @Override
    public byte getSerializeKind()
    {
        return StaticData.SID_AREAGUARD_GOAL;
    }
}
