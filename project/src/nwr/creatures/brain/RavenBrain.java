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

import jzrlib.core.CreatureEntity;
import jzrlib.core.brain.GoalEntity;
import nwr.creatures.brain.goals.PlayerFindGoal;
import nwr.core.types.Services;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class RavenBrain extends SentientBrain
{
    public RavenBrain(CreatureEntity owner)
    {
        super(owner);
    }

    @Override
    protected void prepareGoals()
    {
        super.prepareGoals();
        PlayerFindGoal pfGoal = (PlayerFindGoal) super.defineGoal(GoalKind.gk_PlayerFind);
    }

    @Override
    protected void evaluateGoal(GoalEntity goal)
    {
        switch (goal.Kind) {
            case GoalKind.gk_PlayerFind:
                goal.Value = 0.8f;
                break;

            default:
                super.evaluateGoal(goal);
                break;
        }
    }

    @Override
    public Services getAvailableServices()
    {
        return new Services();
    }
}
