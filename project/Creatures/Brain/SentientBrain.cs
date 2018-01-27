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
using ZRLib.Core;
using ZRLib.Core.Brain;
using NWR.Core.Types;
using NWR.Creatures.Brain.Goals;
using NWR.Database;
using NWR.Items;
using NWR.Game;

namespace NWR.Creatures.Brain
{
    using Services = EnumSet<Service>;

    public class SentientBrain : BeastBrain
    {
        public SentientBrain(CreatureEntity owner)
            : base(owner)
        {
        }

        private void PrepareItemAcquire()
        {
            NWCreature self = (NWCreature)fSelf;
            Item item = self.FindItem();
            if (item != null) {
                ItemAcquireGoal iGoal = (ItemAcquireGoal)FindGoalByKind(GoalKind.gk_ItemAcquire);
                if (iGoal == null || !iGoal.Position.Equals(item.Location)) {
                    iGoal = ((ItemAcquireGoal)CreateGoal(GoalKind.gk_ItemAcquire));
                    iGoal.Position = item.Location;
                    iGoal.Duration = 25;
                }
            }
        }

        private void PrepareCheckHealth()
        {

        }

        protected override void EvaluateGoal(GoalEntity goal)
        {
            switch (goal.Kind) {
                case GoalKind.gk_ItemAcquire:
                    goal.Value = 0.25f;
                    break;

                default:
                    base.EvaluateGoal(goal);
                    break;
            }
        }

        protected override void PrepareGoals()
        {
            base.PrepareGoals();

            NWCreature self = (NWCreature)fSelf;

            if (!IsShipSail) {
                if (self.Entry.Flags.Contains(CreatureFlags.esUseItems)) {
                    PrepareCheckHealth();
                    PrepareItemAcquire();
                }
            }
        }

        public virtual Services AvailableServices
        {
            get {
                return Services.Create(Service.ds_Teach, Service.ds_Trade, Service.ds_Exchange, Service.ds_Recruit);
            }
        }

        public virtual DialogEntry Dialog
        {
            get {
                NWCreature sf = (NWCreature)fSelf;
                DialogEntry dlg = sf.Entry.Dialog;
                return dlg;
            }
        }

        public TopicEntry GetTopic(CreatureEntity collocutor, int conversationIndex, TopicEntry parentTopic, string phrase)
        {
            TopicEntry result = null;

            DialogEntry dlg = Dialog;
            ConversationEntry curConvers = dlg.GetConversation(conversationIndex);

            if (parentTopic == null) {
                result = curConvers.GetTopic(0);
            } else {
                for (int i = 0; i < parentTopic.TopicsCount; i++) {
                    TopicEntry subTopic = parentTopic.GetTopic(i);
                    if ((subTopic.Phrase == phrase)) {
                        result = subTopic;
                        break;
                    }
                }
            }

            return result;
        }

        protected bool Townsman
        {
            get {
                int id = fSelf.CLSID_Renamed;
                return (id == GlobalVars.cid_Guardsman || id == GlobalVars.cid_Jarl);
            }
        }
    }
}
