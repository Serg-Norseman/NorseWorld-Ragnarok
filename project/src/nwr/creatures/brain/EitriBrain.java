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

import nwr.core.types.Services;
import jzrlib.core.CreatureEntity;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.database.ConversationEntry;
import nwr.database.DialogEntry;
import nwr.database.TopicEntry;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class EitriBrain extends SentientBrain
{
    private static final DialogEntry sfDialog;

    public EitriBrain(CreatureEntity owner)
    {
        super(owner);
    }

    @Override
    public Services getAvailableServices()
    {
        return new Services();
    }

    @Override
    public DialogEntry getDialog()
    {
        return sfDialog;
    }

    static {
        sfDialog = new DialogEntry();
        
        ConversationEntry defConvers = sfDialog.addConversation();
        defConvers.Name = Locale.getStr(RS.rs_EitriTopic);
        
        TopicEntry rootTopic = defConvers.addTopic();
        rootTopic.Answer = Locale.getStr(RS.rs_EitriSaid) + "\"" + Locale.getStr(RS.rs_Eitri1) + "\".";
        
        TopicEntry topic = rootTopic.addTopic();
        topic.Condition = "player.hasItem('PlatinumAnvil')";
        topic.Phrase = Locale.getStr(RS.rs_Eitri_Yes);
        topic.Answer = Locale.getStr(RS.rs_Eitri3) + " " + Locale.getStr(RS.rs_Eitri4);
        topic.Action = "player.transferItem(NPC, 'PlatinumAnvil'); NPC.transferItem(player, 'DwarvenArm');";
        
        topic = rootTopic.addTopic();
        topic.Phrase = Locale.getStr(RS.rs_Eitri_No);
        topic.Answer = Locale.getStr(RS.rs_Eitri2);
    }
}
