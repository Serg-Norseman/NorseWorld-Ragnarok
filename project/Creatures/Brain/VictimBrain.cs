/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih.
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

using BSLib;
using NWR.Database;
using NWR.Game;
using NWR.Game.Types;
using ZRLib.Core;

namespace NWR.Creatures.Brain
{
    using Services = EnumSet<Service>;

    /// <summary>
    /// 
    /// </summary>
    public sealed class VictimBrain : SentientBrain
    {
        private static readonly DialogEntry SfAgnarDialog;
        private static readonly DialogEntry SfHaddingrDialog;
        private static readonly DialogEntry SfKetillDialog;

        public VictimBrain(CreatureEntity owner)
            : base(owner)
        {
        }

        public override Services AvailableServices
        {
            get {
                return new Services();
            }
        }

        public override DialogEntry Dialog
        {
            get {
                NWCreature sf = (NWCreature)fSelf;
    
                if (sf.CLSID == GlobalVars.cid_Agnar) {
                    return SfAgnarDialog;
                } else if (sf.CLSID == GlobalVars.cid_Haddingr) {
                    return SfHaddingrDialog;
                } else if (sf.CLSID == GlobalVars.cid_Ketill) {
                    return SfKetillDialog;
                } else {
                    return null;
                }
            }
        }

        static VictimBrain()
        {
            SfAgnarDialog = new DialogEntry();
            ConversationEntry defConvers = SfAgnarDialog.AddConversation(BaseLocale.GetStr(RS.rs_Sacrifice));

            TopicEntry rootTopic = defConvers.AddTopic();
            rootTopic.Answer = BaseLocale.GetStr(RS.rs_Agnar_IsStrungUp) + " " + BaseLocale.GetStr(RS.rs_VictimPleads);

            TopicEntry topic1 = rootTopic.AddTopic();
            topic1.Phrase = BaseLocale.GetStr(RS.rs_Agnar_KickOutStump);
            topic1.Answer = BaseLocale.GetStr(RS.rs_SacrificeAward);
            topic1.Action = "NPC.sacrificeVictim();";

            TopicEntry topic2 = rootTopic.AddTopic();
            topic2.Phrase = BaseLocale.GetStr(RS.rs_SetHimFree);
            topic2.Answer = BaseLocale.GetStr(RS.rs_VictimFree);
            topic2.Action = "NPC.freeVictim();";

            TopicEntry topic3 = rootTopic.AddTopic();
            topic3.Phrase = BaseLocale.GetStr(RS.rs_DoNothing);
            topic3.Answer = "";


            SfHaddingrDialog = new DialogEntry();
            defConvers = SfHaddingrDialog.AddConversation(BaseLocale.GetStr(RS.rs_Sacrifice));

            rootTopic = defConvers.AddTopic();
            rootTopic.Answer = BaseLocale.GetStr(RS.rs_Haddingr_IsPinned) + " " + BaseLocale.GetStr(RS.rs_VictimPleads);

            topic1 = rootTopic.AddTopic();
            topic1.Phrase = BaseLocale.GetStr(RS.rs_Haddingr_TwistSpear);
            topic1.Answer = BaseLocale.GetStr(RS.rs_SacrificeAward);
            topic1.Action = "NPC.sacrificeVictim();";

            topic2 = rootTopic.AddTopic();
            topic2.Phrase = BaseLocale.GetStr(RS.rs_SetHimFree);
            topic2.Answer = BaseLocale.GetStr(RS.rs_VictimFree);
            topic2.Action = "NPC.freeVictim();";

            topic3 = rootTopic.AddTopic();
            topic3.Phrase = BaseLocale.GetStr(RS.rs_DoNothing);
            topic3.Answer = "";


            SfKetillDialog = new DialogEntry();
            defConvers = SfKetillDialog.AddConversation(BaseLocale.GetStr(RS.rs_Sacrifice));

            rootTopic = defConvers.AddTopic();
            rootTopic.Answer = BaseLocale.GetStr(RS.rs_Ketill_IsDangling) + " " + BaseLocale.GetStr(RS.rs_VictimPleads);

            topic1 = rootTopic.AddTopic();
            topic1.Phrase = BaseLocale.GetStr(RS.rs_Ketill_DrownHim);
            topic1.Answer = BaseLocale.GetStr(RS.rs_SacrificeAward);
            topic1.Action = "NPC.sacrificeVictim();";

            topic2 = rootTopic.AddTopic();
            topic2.Phrase = BaseLocale.GetStr(RS.rs_SetHimFree);
            topic2.Answer = BaseLocale.GetStr(RS.rs_VictimFree);
            topic2.Action = "NPC.freeVictim();";

            topic3 = rootTopic.AddTopic();
            topic3.Phrase = BaseLocale.GetStr(RS.rs_DoNothing);
            topic3.Answer = "";
        }
    }
}
