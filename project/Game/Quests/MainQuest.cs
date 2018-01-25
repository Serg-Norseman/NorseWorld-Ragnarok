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

using NWR.Core.Types;
using NWR.Creatures;
using NWR.Database;
using NWR.Game.Story;
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
            Stage = QuestItemState.qisNone;
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
                    case QuestItemState.qisNone:
                        st = ": задание получено";
                        break;
                    case QuestItemState.qisFounded:
                        st = ": предмет найден";
                        break;
                    case QuestItemState.qisComplete:
                        st = ": предмет найден и передан " + target.GetNounDeclension(Number.nSingle, Case.cDative);
                        break;
                }
    
                res = qst + st;
    
                return res;
            }
        }

        protected override bool OnPickupItem(Item item)
        {
            bool res = (item.CLSID_Renamed == ArtefactID);
            if (res) {
                Stage = QuestItemState.qisFounded;
            }
            return false; // quest not complete
        }

        protected override bool OnGiveupItem(Item item, NWCreature target)
        {
            bool res = (item.CLSID_Renamed == ArtefactID && target.CLSID_Renamed == DeityID);
            if (res) {
                Stage = QuestItemState.qisComplete;
            }
            return res; // quest completed
        }
    }
}
