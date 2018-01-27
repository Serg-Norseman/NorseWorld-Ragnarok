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
using NWR.Core;
using NWR.Game;
using NWR.Game.Story;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class JournalWindow : NWWindow
    {
        private class JTextBox : TextBox
        {
            public JTextBox(BaseControl owner)
                : base(owner)
            {
            }

            protected override void DoPaintLine(BaseScreen screen, int index, ExtRect rect)
            {
                JournalItem item = (JournalItem)fLines.GetObject(index);
                if (item != null) {
                    TextColor = item.Color;
                }
                base.DoPaintLine(screen, index, rect);
            }
        }

        private readonly JTextBox fText;

        public JournalWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 600;
            Height = 460;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);
            Shifted = true;
            BackDraw = false;

            fText = new JTextBox(this);
            fText.Font = CtlCommon.SmFont;
            fText.Left = 10;
            fText.Top = 10;
            fText.Width = 580;
            fText.Height = 440;
            fText.Visible = true;
            //this.fText.setLinks(true);
            fText.OnGetVariable = OnGetVar;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fText.Dispose();
            }
            base.Dispose(disposing);
        }

        private void OnGetVar(object sender, ref string  refVar)
        {
            if (refVar.CompareTo("ver") == 0) {
                refVar = StaticData.Rs_GameVersion;
                refVar = refVar.Substring(0 + 1);
            } else {
                if (refVar.CompareTo("dev_time") == 0) {
                    refVar = StaticData.Rs_GameDevTime;
                }
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();

            NWGameSpace space = (NWGameSpace)GameSpace.Instance;
            Journal journal = space.Journal;
            StringList lines = fText.Lines;

            lines.Clear();
            lines.BeginUpdate();

            int type = -1;
            for (int i = 0; i < journal.Count; i++) {
                JournalItem item = journal.GetItem(i);

                if (type != item.Type) {
                    lines.Add("");
                    type = item.Type;
                }

                lines.AddObject(item.Text, item);
            }

            lines.Add("");
            lines.AddObject("    " + "Квесты:", new JournalItem(JournalItem.SIT_QUESTS));
            int num = space.QuestsCount;
            for (int i = 0; i < num; i++) {
                Quest quest = space.GetQuest(i);
                int stt = (quest.IsComplete ? JournalItem.SIT_QUEST_Y : JournalItem.SIT_QUEST_N);
                lines.AddObject("  --> " + quest.Description, new JournalItem(stt));
            }

            journal.GenerateStats(lines);

            lines.EndUpdate();

            ActiveControl = fText;
        }
    }
}
