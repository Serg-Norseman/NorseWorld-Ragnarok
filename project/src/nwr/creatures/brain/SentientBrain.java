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
import jzrlib.utils.TextUtils;
import nwr.creatures.brain.goals.ItemAcquireGoal;
import nwr.creatures.NWCreature;
import nwr.core.types.Service;
import nwr.core.types.Services;
import nwr.database.ConversationEntry;
import nwr.database.CreatureFlags;
import nwr.database.DialogEntry;
import nwr.database.TopicEntry;
import nwr.item.Item;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class SentientBrain extends BeastBrain
{
    public SentientBrain(CreatureEntity owner)
    {
        super(owner);
    }

    private void prepareItemAcquire()
    {
        NWCreature self = (NWCreature) super.fSelf;
        Item item = self.findItem();
        if (item != null) {
            ItemAcquireGoal iGoal = (ItemAcquireGoal) super.findGoalByKind(GoalKind.gk_ItemAcquire);
            if (iGoal == null || !iGoal.Position.equals(item.getLocation())) {
                iGoal = ((ItemAcquireGoal) super.createGoal(GoalKind.gk_ItemAcquire));
                iGoal.Position = item.getLocation();
                iGoal.Duration = 25;
            }
        }
    }

    private void prepareCheckHealth()
    {
        
    }

    @Override
    protected void evaluateGoal(GoalEntity goal)
    {
        switch (goal.Kind) {
            case GoalKind.gk_ItemAcquire:
                goal.Value = 0.25f;
                break;

            default:
                super.evaluateGoal(goal);
                break;
        }
    }

    @Override
    protected void prepareGoals()
    {
        super.prepareGoals();

        NWCreature self = (NWCreature) super.fSelf;

        if (!this.IsShipSail) {
            if (self.getEntry().Flags.contains(CreatureFlags.esUseItems)) {
                this.prepareCheckHealth();
                this.prepareItemAcquire();
            }
        }
    }

    public Services getAvailableServices()
    {
        return new Services(Service.ds_Teach, Service.ds_Trade, Service.ds_Exchange, Service.ds_Recruit);
    }

    public DialogEntry getDialog()
    {
        NWCreature sf = (NWCreature) super.fSelf;
        DialogEntry dlg = sf.getEntry().Dialog;
        return dlg;
    }

    public final TopicEntry getTopic(CreatureEntity collocutor, int conversationIndex, TopicEntry parentTopic, String phrase)
    {
        TopicEntry result = null;

        DialogEntry dlg = this.getDialog();
        ConversationEntry curConvers = dlg.getConversation(conversationIndex);

        if (parentTopic == null) {
            result = curConvers.getTopic(0);
        } else {
            for (int i = 0; i < parentTopic.getTopicsCount(); i++) {
                TopicEntry subTopic = parentTopic.getTopic(i);
                if (TextUtils.equals(subTopic.Phrase, phrase)) {
                    result = subTopic;
                    break;
                }
            }
        }

        return result;
    }

    protected final boolean isTownsman()
    {
        int id = this.fSelf.CLSID;
        return (id == GlobalVars.cid_Guardsman || id == GlobalVars.cid_Jarl);
    }
}
