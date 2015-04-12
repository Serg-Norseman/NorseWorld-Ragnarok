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

import nwr.core.types.QuestItemState;
import nwr.creatures.NWCreature;
import nwr.database.DataEntry;
import nwr.game.NWGameSpace;
import nwr.game.story.Quest;
import jzrlib.grammar.Case;
import nwr.item.Item;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.11.0
 */
public final class MainQuest extends Quest
{
    public final int ArtefactID;
    public final int DeityID;
    public QuestItemState Stage;

    public MainQuest(NWGameSpace space, int artefactID, int deityID)
    {
        super(space);
        this.ArtefactID = artefactID;
        this.DeityID = deityID;
        this.Stage = QuestItemState.qisNone;
    }

    @Override
    public String getDescription()
    {
        String res;
        
        DataEntry entry = this.fSpace.getDataEntry(ArtefactID);
        DataEntry target = this.fSpace.getDataEntry(DeityID);
        String qst = "Квест \"" + entry.getName() + "\"";
        String st = "";

        // for old saves compatibility
        QuestItemState qis;
        qis = this.fSpace.checkQuestItem(ArtefactID, DeityID);
        if (this.Stage != qis) {
            this.Stage = qis;
        }

        switch (Stage) {
            case qisNone:
                st = ": задание получено";
                break;
            case qisFounded:
                st = ": предмет найден";
                break;
            case qisComplete:
                st = ": предмет найден и передан " + target.getNounDeclension(jzrlib.grammar.Number.nSingle, Case.cDative);
                break;
        }

        res = qst + st;

        return res;
    }

    @Override
    protected boolean onPickupItem(Item item)
    {
        boolean res = (item.CLSID == ArtefactID);
        if (res) {
            this.Stage = QuestItemState.qisFounded;
        }
        return false; // quest not complete
    }

    @Override
    protected boolean onGiveupItem(Item item, NWCreature target)
    {
        boolean res = (item.CLSID == ArtefactID && target.CLSID == DeityID);
        if (res) {
            this.Stage = QuestItemState.qisComplete;
        }
        return res; // quest completed
    }
}
