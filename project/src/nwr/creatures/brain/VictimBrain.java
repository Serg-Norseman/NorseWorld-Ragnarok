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
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.database.ConversationEntry;
import nwr.database.DialogEntry;
import nwr.database.TopicEntry;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class VictimBrain extends SentientBrain
{
    private static final DialogEntry sfAgnarDialog;
    private static final DialogEntry sfHaddingrDialog;
    private static final DialogEntry sfKetillDialog;

    public VictimBrain(CreatureEntity owner)
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
        NWCreature sf = (NWCreature) super.fSelf;
        
        if (sf.CLSID == GlobalVars.cid_Agnar) {
            return sfAgnarDialog;
        } else if (sf.CLSID == GlobalVars.cid_Haddingr) {
            return sfHaddingrDialog;
        } else if (sf.CLSID == GlobalVars.cid_Ketill) {
            return sfKetillDialog;
        } else {
            return null;
        }
    }

    static {
        sfAgnarDialog = new DialogEntry();
        ConversationEntry defConvers = sfAgnarDialog.addConversation(Locale.getStr(RS.rs_Sacrifice));

        TopicEntry rootTopic = defConvers.addTopic();
        rootTopic.Answer = Locale.getStr(RS.rs_Agnar_IsStrungUp) + " " + Locale.getStr(RS.rs_VictimPleads);

        TopicEntry topic1 = rootTopic.addTopic();
        topic1.Phrase = Locale.getStr(RS.rs_Agnar_KickOutStump);
        topic1.Answer = Locale.getStr(RS.rs_SacrificeAward);
        topic1.Action = "NPC.sacrificeVictim();";

        TopicEntry topic2 = rootTopic.addTopic();
        topic2.Phrase = Locale.getStr(RS.rs_SetHimFree);
        topic2.Answer = Locale.getStr(RS.rs_VictimFree);
        topic2.Action = "NPC.freeVictim();";

        TopicEntry topic3 = rootTopic.addTopic();
        topic3.Phrase = Locale.getStr(RS.rs_DoNothing);
        topic3.Answer = "";

        /// *** ///
        sfHaddingrDialog = new DialogEntry();
        defConvers = sfHaddingrDialog.addConversation(Locale.getStr(RS.rs_Sacrifice));

        rootTopic = defConvers.addTopic();
        rootTopic.Answer = Locale.getStr(RS.rs_Haddingr_IsPinned) + " " + Locale.getStr(RS.rs_VictimPleads);

        topic1 = rootTopic.addTopic();
        topic1.Phrase = Locale.getStr(RS.rs_Haddingr_TwistSpear);
        topic1.Answer = Locale.getStr(RS.rs_SacrificeAward);
        topic1.Action = "NPC.sacrificeVictim();";

        topic2 = rootTopic.addTopic();
        topic2.Phrase = Locale.getStr(RS.rs_SetHimFree);
        topic2.Answer = Locale.getStr(RS.rs_VictimFree);
        topic2.Action = "NPC.freeVictim();";

        topic3 = rootTopic.addTopic();
        topic3.Phrase = Locale.getStr(RS.rs_DoNothing);
        topic3.Answer = "";

        /// *** ///
        sfKetillDialog = new DialogEntry();
        defConvers = sfKetillDialog.addConversation(Locale.getStr(RS.rs_Sacrifice));

        rootTopic = defConvers.addTopic();
        rootTopic.Answer = Locale.getStr(RS.rs_Ketill_IsDangling) + " " + Locale.getStr(RS.rs_VictimPleads);

        topic1 = rootTopic.addTopic();
        topic1.Phrase = Locale.getStr(RS.rs_Ketill_DrownHim);
        topic1.Answer = Locale.getStr(RS.rs_SacrificeAward);
        topic1.Action = "NPC.sacrificeVictim();";

        topic2 = rootTopic.addTopic();
        topic2.Phrase = Locale.getStr(RS.rs_SetHimFree);
        topic2.Answer = Locale.getStr(RS.rs_VictimFree);
        topic2.Action = "NPC.freeVictim();";

        topic3 = rootTopic.addTopic();
        topic3.Phrase = Locale.getStr(RS.rs_DoNothing);
        topic3.Answer = "";
    }
}
