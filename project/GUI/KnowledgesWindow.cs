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

using System.Collections.Generic;
using BSLib;
using NWR.Game;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class KnowledgesWindow : NWWindow
    {
        private readonly ListBox fList;
        private readonly TextBox fText;
        private bool fReady;

        public KnowledgesWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 600;
            Height = 460;
            WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview, WindowStyles.wsScreenCenter);
            Shifted = true;
            BackDraw = false;

            fReady = false;

            fList = new ListBox(this);
            fList.Bounds = ExtRect.Create(10, 10, 589, 199);
            fList.OnItemSelect = Knowledges_Select;
            fList.Columns = 2;
            fList.Visible = true;
            fList.Items.Sorted = true;

            fText = new TextBox(this);
            fText.Bounds = ExtRect.Create(10, 202, 589, 449);
            fText.Visible = true;
            fText.Links = true;
            fText.OnLinkClick = OnLinkClick;
            fText.TextColor = Colors.Gold;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fList.Dispose();
                fText.Dispose();
            }
            base.Dispose(disposing);
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();

            ActiveControl = fList;

            if (!fReady) {
                UpdateView();
            }
        }

        private void Knowledges_Select(object sender, MouseButton button, LBItem item)
        {
            fText.Lines.BeginUpdate();
            if (item.Data == null) {
                fText.Lines.Text = "";
            } else {
                MemoryEntry K = (MemoryEntry)item.Data;
                string desc = K.Desc.Replace("|", AuxUtils.CRLF);
                fText.Lines.Clear();
                fText.Lines.Add(K.Name);
                fText.Lines.Add("");
                fText.Lines.Add(desc);
            }
            fText.Lines.EndUpdate();
        }

        private void OnLinkClick(object sender, string aLinkValue)
        {
            Select(aLinkValue);
        }

        private void UpdateView()
        {
            fList.Items.BeginUpdate();
            fList.Items.Clear();
            fList.Mode = ListBox.MODE_LIST;

            foreach (KeyValuePair<string, MemoryEntry> entry in GlobalVars.nwrGame.Player.Memory.Data) {
                MemoryEntry mem = entry.Value;

                string txt = mem.Name;
                if (!txt.Equals("")) {
                    fList.Items.Add(txt, mem);
                }
            }

            fList.Items.EndUpdate();
        }

        public void Select(string name)
        {
            if (!Visible) {
                UpdateView();
            }

            int idx = fList.Items.AbsoluteIndexOf(name);

            if (idx >= 0) {
                fList.SelIndex = idx;
                Knowledges_Select(fList, MouseButton.mbLeft, fList.GetAbsoluteItem(idx));
                fReady = true;
                Show();
                fReady = false;
            }
        }
    }
}
