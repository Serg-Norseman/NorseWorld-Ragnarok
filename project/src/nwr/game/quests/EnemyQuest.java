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
package nwr.game.quests;

import nwr.creatures.NWCreature;
import nwr.game.NWGameSpace;
import nwr.game.story.Quest;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.11.0
 */
public final class EnemyQuest extends Quest
{
    public final int EnemyID;
    public int Remains;

    public EnemyQuest(NWGameSpace space, int enemyID, int count)
    {
        super(space);
        this.EnemyID = enemyID;
        this.Remains = count;
    }

    @Override
    protected boolean onKillMonster(NWCreature monster)
    {
        if (monster.CLSID == EnemyID) {
            this.Remains--;
            
            if (this.Remains > 0) {
                this.fSpace.showText("{1} await[s] death at your hands.");
            }
        }
        return (this.Remains <= 0);
    }

    @Override
    public void announce()
    {
        // FIXME: localize it
        this.fSpace.showText("You must kill {1}.");
    }
}
