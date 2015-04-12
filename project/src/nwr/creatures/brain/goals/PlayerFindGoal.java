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
import nwr.creatures.brain.NWGoalEntity;
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.types.CreatureState;
import nwr.core.types.LogFeatures;
import nwr.main.GlobalVars;
import jzrlib.utils.AuxUtils;
import jzrlib.core.Point;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class PlayerFindGoal extends NWGoalEntity
{
    public PlayerFindGoal(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public void execute()
    {
        NWCreature self = (NWCreature) super.getSelf();
        Point playerPt = self.getSpace().getPlayer().getLocation();

        if (AuxUtils.distance(playerPt, self.getLocation()) == 1) {
            int idx = AuxUtils.getBoundedRnd(RS.rs_Diary_First, RS.rs_Diary_Last);
            String s = Locale.format(RS.rs_RavenSaid, Locale.getStr(idx));
            GlobalVars.nwrWin.showText(self, s, new LogFeatures(LogFeatures.lfDialog));

            self.setState(CreatureState.csDead);
            this.IsComplete = true;
        } else {
            Point next = self.getStep(playerPt);
            if (next != null) {
                this.getBrain().stepTo(next.X, next.Y);
            }
        }
    }
}
