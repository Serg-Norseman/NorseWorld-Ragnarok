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

using NWR.Creatures;
using NWR.Database;
using NWR.Game.Story;
using NWR.Game.Types;
using NWR.Items;
using ZRLib.Grammar;

namespace NWR.Game.Quests
{
    /// <summary>
    /// 
    /// </summary>
    public sealed class MainQuest : Quest
    {
        public readonly int ArtefactID;
        public readonly int DeityID;
        public QuestItemState Stage;

        public MainQuest(NWGameSpace space, int artefactID, int deityID)
            : base(space)
        {
            ArtefactID = artefactID;
            DeityID = deityID;
            Stage = QuestItemState.None;
        }

        public override string Description
        {
            get {
                string res;
    
                DataEntry entry = fSpace.GetDataEntry(ArtefactID);
                DataEntry target = fSpace.GetDataEntry(DeityID);
                string qst = "Квест \"" + entry.Name + "\"";
                string st = "";
    
                // for old saves compatibility
                QuestItemState qis;
                qis = fSpace.CheckQuestItem(ArtefactID, DeityID);
                if (Stage != qis) {
                    Stage = qis;
                }
    
                switch (Stage) {
                    case QuestItemState.None:
                        st = ": задание получено";
                        break;
                    case QuestItemState.Founded:
                        st = ": предмет найден";
                        break;
                    case QuestItemState.Completed:
                        st = ": предмет найден и передан " + target.GetNounDeclension(Number.nSingle, Case.cDative);
                        break;
                }
    
                res = qst + st;
    
                return res;
            }
        }

        protected override bool OnPickupItem(Item item)
        {
            bool res = (item.CLSID == ArtefactID);
            if (res) {
                Stage = QuestItemState.Founded;
            }
            return false; // quest not complete
        }

        protected override bool OnGiveupItem(Item item, NWCreature target)
        {
            bool res = (item.CLSID == ArtefactID && target.CLSID == DeityID);
            if (res) {
                Stage = QuestItemState.Completed;
            }
            return res; // quest completed
        }
    }
}
