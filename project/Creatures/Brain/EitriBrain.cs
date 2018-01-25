/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using BSLib;
using ZRLib.Core;
using NWR.Core;
using NWR.Core.Types;
using NWR.Database;

namespace NWR.Creatures.Brain
{
    using Services = EnumSet<Service>;

    /// <summary>
    /// 
    /// </summary>
    public sealed class EitriBrain : SentientBrain
    {
        private static readonly DialogEntry SfDialog;

        public EitriBrain(CreatureEntity owner)
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
                return SfDialog;
            }
        }

        static EitriBrain()
        {
            SfDialog = new DialogEntry();

            ConversationEntry defConvers = SfDialog.AddConversation();
            defConvers.Name = BaseLocale.GetStr(RS.rs_EitriTopic);

            TopicEntry rootTopic = defConvers.AddTopic();
            rootTopic.Answer = BaseLocale.GetStr(RS.rs_EitriSaid) + "\"" + BaseLocale.GetStr(RS.rs_Eitri1) + "\".";

            TopicEntry topic = rootTopic.AddTopic();
            topic.Condition = "player.hasItem('PlatinumAnvil')";
            topic.Phrase = BaseLocale.GetStr(RS.rs_Eitri_Yes);
            topic.Answer = BaseLocale.GetStr(RS.rs_Eitri3) + " " + BaseLocale.GetStr(RS.rs_Eitri4);
            topic.Action = "player.transferItem(NPC, 'PlatinumAnvil'); NPC.transferItem(player, 'DwarvenArm');";

            topic = rootTopic.AddTopic();
            topic.Phrase = BaseLocale.GetStr(RS.rs_Eitri_No);
            topic.Answer = BaseLocale.GetStr(RS.rs_Eitri2);
        }
    }

}