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
import nwr.creatures.brain.BeastBrain;
import nwr.creatures.brain.NWGoalEntity;
import nwr.creatures.NWCreature;
import jzrlib.core.Directions;
import jzrlib.core.Point;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class FlockGoal extends NWGoalEntity
{
    public FlockGoal(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public void execute()
    {
        NWCreature self = (NWCreature) super.getSelf();
        BeastBrain beastBrain = (BeastBrain) this.getBrain();

        int min = beastBrain.fNearKinsfolkDist;
        CreatureEntity nearKinsfolk = beastBrain.fNearKinsfolk;
        int cnt = beastBrain.fKinsfolks.getCount();
        int cohX = 0;
        int cohY = 0;
        int algX = 0;
        int algY = 0;

        for (int i = 0; i < cnt; i++) {
            NWCreature cr = (NWCreature) beastBrain.fKinsfolks.getItem(i);
            int dir = cr.LastDir;
            cohX += cr.getPosX();
            cohY += cr.getPosY();
            algX += Directions.Data[dir].dX;
            algY += Directions.Data[dir].dY;
        }

        int dX = 0;
        int dY = 0;
        if (cnt > 0) {
            dX = (dX + Math.round((float) algX / (float) cnt));
            dY = (dY + Math.round((float) algY / (float) cnt));
        }
        if (cnt > 0) {
            cohX = Math.round((float) cohX / (float) cnt);
            cohY = Math.round((float) cohY / (float) cnt);
            int dir = Directions.getDirByCoords(self.getPosX(), self.getPosY(), cohX, cohY);
            dX += Directions.Data[dir].dX;
            dY += Directions.Data[dir].dY;
        }
        if (min <= 3 && nearKinsfolk != null) {
            if (nearKinsfolk.getPosX() > self.getPosX()) {
                dX--;
            }
            if (nearKinsfolk.getPosX() < self.getPosX()) {
                dX++;
            }
            if (nearKinsfolk.getPosY() > self.getPosY()) {
                dY--;
            }
            if (nearKinsfolk.getPosY() < self.getPosY()) {
                dY++;
            }
        }

        Point newPos = new Point();
        newPos.X = self.getPosX() + dX;
        newPos.Y = self.getPosY() + dY;
        Point next = self.getStep(newPos);
        if (next != null) {
            this.getBrain().stepTo(next.X, next.Y);
        }
    }
}
