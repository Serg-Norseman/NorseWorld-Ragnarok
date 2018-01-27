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
using NWR.GUI.Controls;
using NWR.Items;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class ExchangeWindow : DialogWindow
    {
        private readonly ListBox fColList;
        private readonly ListBox fPackList;

        public ExchangeWindow(BaseControl owner)
            : base(owner)
        {
            fPackList = new ListBox(this);
            fPackList.Bounds = ExtRect.Create(309, 28, 589, 398);
            fPackList.Visible = true;
            fPackList.OnDragDrop = OnPackDragDrop;
            fPackList.OnDragOver = OnPackDragOver;
            fPackList.OnDragStart = OnPackDragStart;
            fPackList.OnMouseDown = OnListMouseDown;
            fPackList.OnMouseMove = OnListMouseMove;
            fPackList.OnKeyDown = null;
            fPackList.ShowHints = true;
            fPackList.Options.Include(LBOptions.lboIcons);

            fColList = new ListBox(this);
            fColList.Bounds = ExtRect.Create(10, 28, 290, 398);
            fColList.Visible = true;
            fColList.OnDragDrop = OnColDragDrop;
            fColList.OnDragOver = OnColDragOver;
            fColList.OnDragStart = OnColDragStart;
            fColList.OnMouseDown = OnListMouseDown;
            fColList.OnMouseMove = OnListMouseMove;
            fColList.OnKeyDown = null;
            fColList.ShowHints = true;
            fColList.Options.Include(LBOptions.lboIcons);
        }

        private void UpdateView()
        {
            bool onlyIcons = GlobalVars.nwrWin.InventoryOnlyIcons;
            fPackList.Items.BeginUpdate();
            fColList.Items.BeginUpdate();
            fPackList.Items.Clear();
            fColList.Items.Clear();

            ItemsList pack = GlobalVars.nwrGame.Player.Items;

            int num = pack.Count;
            for (int i = 0; i < num; i++) {
                Item item = pack.GetItem(i);
                string nm = item.Name;
                if (item.InUse) {
                    nm += " (*)";
                }
                AddListItem(fPackList, nm, item, onlyIcons);
            }

            pack = fCollocutor.Items;

            int num2 = pack.Count;
            for (int i = 0; i < num2; i++) {
                Item item = pack.GetItem(i);
                string nm = item.Name;
                if (item.InUse) {
                    nm += " (*)";
                }
                AddListItem(fColList, nm, item, onlyIcons);
            }

            fColList.Items.EndUpdate();
            fPackList.Items.EndUpdate();
        }

        private void OnListMouseDown(object sender, MouseEventArgs eventArgs)
        {
            if (eventArgs.Button == MouseButton.mbLeft) {
                ListBox aList = (ListBox)sender;
                if (aList.Items.Count > 0 && aList.SelIndex >= 0) {
                    aList.BeginDrag(false);
                }
            }
        }

        private void OnListMouseMove(object sender, MouseMoveEventArgs eventArgs)
        {
            OnListMouseMove(sender, eventArgs.X, eventArgs.Y, false);
        }

        private void OnPackDragDrop(object sender, object source, int x, int y)
        {
            int idx = fColList.SelIndex;
            if (idx >= 0 && idx < fColList.Items.Count) {
                Item item = (Item)fColList.Items.GetItem(idx).Data;
                fCollocutor.DropItem(item);
                GlobalVars.nwrGame.Player.PickupItem(item);
                UpdateView();
            }
        }

        private void OnPackDragOver(object sender, object source, int x, int y, ref bool  Accept)
        {
            int idx = fColList.SelIndex;
            Accept = (source.Equals(fColList) && idx >= 0 && idx < fColList.Items.Count);
        }

        private void OnPackDragStart(object sender, ref BaseDragObject  dragObject)
        {
            int idx = fPackList.SelIndex;
            if (idx >= 0 && idx < fPackList.Items.Count) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                dragObject = new InventoryObject();
                ((InventoryObject)dragObject).InvItem = item;
            }
        }

        private void OnColDragDrop(object sender, object source, int x, int y)
        {
            int idx = fPackList.SelIndex;
            if (idx >= 0 && idx < fPackList.Items.Count) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                GlobalVars.nwrGame.Player.DropItem(item);
                fCollocutor.PickupItem(item);
                UpdateView();
            }
        }

        private void OnColDragOver(object sender, object source, int x, int y, ref bool  Accept)
        {
            int idx = fPackList.SelIndex;
            Accept = (source.Equals(fPackList) && idx >= 0 && idx < fPackList.Items.Count);
        }

        private void OnColDragStart(object sender, ref BaseDragObject  dragObject)
        {
            int idx = fColList.SelIndex;
            if (idx >= 0 && idx < fColList.Items.Count) {
                Item item = (Item)fColList.Items.GetItem(idx).Data;
                dragObject = new InventoryObject();
                ((InventoryObject)dragObject).InvItem = item;
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);

            string nm_self = GlobalVars.nwrGame.Player.Name;
            string nm_col = fCollocutor.Name;

            screen.SetTextColor(Colors.Gold, true);

            ExtRect pakRt = fPackList.Bounds; // 2
            ExtRect colRt = fColList.Bounds; // 0

            int lcx = pakRt.Left + (pakRt.Width - CtlCommon.SmFont.GetTextWidth(nm_self)) / 2;
            int rcx = colRt.Left + (colRt.Width - CtlCommon.SmFont.GetTextWidth(nm_col)) / 2;

            int lcy = pakRt.Top - CtlCommon.SmFont.Height;
            int rcy = colRt.Top - CtlCommon.SmFont.Height;

            screen.DrawText(lcx, lcy, nm_self, 0);
            screen.DrawText(rcx, rcy, nm_col, 0);

            screen.DrawText(pakRt.Left, pakRt.Bottom + 3, BaseLocale.GetStr(RS.rs_Weight) + ": " + string.Format("{0:F2} / {1:F2}", new object[] {
                GlobalVars.nwrGame.Player.TotalWeight,
                GlobalVars.nwrGame.Player.MaxItemsWeight
            }), 0);
            screen.DrawText(colRt.Left, colRt.Bottom + 3, BaseLocale.GetStr(RS.rs_Weight) + ": " + string.Format("{0:F2} / {1:F2}", new object[] {
                fCollocutor.TotalWeight,
                fCollocutor.MaxItemsWeight
            }), 0);
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            SetupItemsList(fPackList);
            SetupItemsList(fColList);
            UpdateView();
        }
    }
}
