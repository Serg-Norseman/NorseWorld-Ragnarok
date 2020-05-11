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
using NWR.Game;
using NWR.GUI.Controls;
using NWR.Items;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class AlchemyWindow : NWWindow
    {
        private readonly ListBox fIngredientsList;
        private readonly ListBox fPackList;
        private readonly ListBox fResList;

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        private void OnBtnAlchemy(object sender)
        {
            EntityList ingredients = new EntityList(null, false);
            try {
                int num = fIngredientsList.Items.Count;
                for (int i = 0; i < num; i++) {
                    GameEntity ing = (GameEntity)fIngredientsList.Items.GetItem(i).Data;
                    ingredients.Add(ing);
                }

                Item res = GlobalVars.nwrGame.Player.Craft.Alchemy(ingredients);

                UpdateLists();

                if (res != null) {
                    fResList.Items.Add(res.Name, res);
                }
            } finally {
                ingredients.Dispose();
            }
        }

        private void UpdateLists()
        {
            fPackList.Items.Clear();
            fIngredientsList.Items.Clear();
            fResList.Items.Clear();

            EntityList items = GlobalVars.nwrGame.Player.Items;
            int num = items.Count;
            for (int i = 0; i < num; i++) {
                Item item = (Item)items.GetItem(i);
                if (item.Ingredient) {
                    AddListItem(fPackList, item.Name, item, false);
                }
            }
        }

        private void OnIngrDragDrop(object sender, object source, int x, int y)
        {
            int idx = fPackList.SelIndex;
            if (source.Equals(fPackList) && idx >= 0 && idx < fPackList.Items.Count) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                fIngredientsList.Items.Add(item.Name, item);
            }
        }

        private void OnIngrDragOver(object sender, object source, int x, int y, ref bool accept)
        {
            int idx = fPackList.SelIndex;
            accept = (source.Equals(fPackList) && idx >= 0 && idx < fPackList.Items.Count);
        }

        private void OnIngrDragStart(object sender, ref BaseDragObject dragObject)
        {
            int idx = fIngredientsList.SelIndex;
            if (idx >= 0 && idx < fIngredientsList.Items.Count) {
                Item item = (Item)fIngredientsList.Items.GetItem(idx).Data;
                dragObject = new InventoryObject();
                ((InventoryObject)dragObject).InvItem = item;
            }
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
            NWWindow.OnListMouseMove(sender, eventArgs.X, eventArgs.Y, false);
        }

        private void OnPackDragDrop(object sender, object source, int x, int y)
        {
            int idx = fIngredientsList.SelIndex;
            if (source.Equals(fIngredientsList) && idx >= 0 && idx < fIngredientsList.Items.Count) {
                fIngredientsList.Items.Delete(idx);
            }
        }

        private void OnPackDragOver(object sender, object source, int x, int y, ref bool accept)
        {
            accept = source.Equals(fIngredientsList);
        }

        private void OnPackDragStart(object sender, ref BaseDragObject dragObject)
        {
            int idx = fPackList.SelIndex;
            if (idx >= 0 && idx < fPackList.Items.Count) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                if (fIngredientsList.Items.IndexOf(item.Name) < 0) {
                    dragObject = new InventoryObject();
                    ((InventoryObject)dragObject).InvItem = item;
                }
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);

            ExtRect pakRt = fPackList.Bounds; // 8
            ExtRect ingRt = fIngredientsList.Bounds; // 6
            ExtRect resRt = fResList.Bounds; // 7

            CtlCommon.SmFont.Color = Colors.Gold;
            int lcx = pakRt.Left + (pakRt.Width - CtlCommon.SmFont.GetTextWidth(BaseLocale.GetStr(RS.rs_Ingredients))) / 2;
            int rcx = ingRt.Left + (ingRt.Width - CtlCommon.SmFont.GetTextWidth(BaseLocale.GetStr(RS.rs_ItemsChoosed))) / 2;
            int ocx = resRt.Left + (resRt.Width - CtlCommon.SmFont.GetTextWidth(BaseLocale.GetStr(RS.rs_Result))) / 2;

            screen.DrawText(lcx, 9, BaseLocale.GetStr(RS.rs_Ingredients), 0);
            screen.DrawText(rcx, 9, BaseLocale.GetStr(RS.rs_ItemsChoosed), 0);
            screen.DrawText(ocx, 206, BaseLocale.GetStr(RS.rs_Result), 0);
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            fPackList.ImagesList = GlobalVars.nwrWin.Symbols;
            fIngredientsList.ImagesList = GlobalVars.nwrWin.Symbols;
            fResList.ImagesList = GlobalVars.nwrWin.Symbols;
            UpdateLists();
        }

        public AlchemyWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 600;
            Height = 460;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);
            Shifted = true;

            fPackList = new ListBox(this);
            fPackList.Bounds = ExtRect.Create(309, 28, 589, 398);
            fPackList.OnItemSelect = null;
            fPackList.Visible = true;
            fPackList.OnDragDrop = OnPackDragDrop;
            fPackList.OnDragOver = OnPackDragOver;
            fPackList.OnDragStart = OnPackDragStart;
            fPackList.OnMouseDown = OnListMouseDown;
            fPackList.ShowHints = true;
            fPackList.OnMouseMove = OnListMouseMove;

            fIngredientsList = new ListBox(this);
            fIngredientsList.Bounds = ExtRect.Create(10, 28, 290, 207);
            fIngredientsList.OnItemSelect = null;
            fIngredientsList.Visible = true;
            fIngredientsList.OnDragDrop = OnIngrDragDrop;
            fIngredientsList.OnDragOver = OnIngrDragOver;
            fIngredientsList.OnDragStart = OnIngrDragStart;
            fIngredientsList.OnMouseDown = OnListMouseDown;
            fIngredientsList.ShowHints = true;
            fIngredientsList.OnMouseMove = OnListMouseMove;

            fResList = new ListBox(this);
            fResList.Bounds = ExtRect.Create(10, 225, 290, 398); // 7
            fResList.Visible = true;
            fResList.ShowHints = true;
            fResList.OnMouseMove = OnListMouseMove;

            NWButton btnAlchemy = new NWButton(this);
            btnAlchemy.Width = 90;
            btnAlchemy.Height = 30;
            btnAlchemy.Left = fPackList.Left + 20;
            btnAlchemy.Top = Height - 30 - 20;
            btnAlchemy.OnClick = OnBtnAlchemy;
            btnAlchemy.OnLangChange = GlobalVars.nwrWin.LangChange;
            btnAlchemy.LangResID = 131;
            btnAlchemy.ImageFile = "itf/DlgBtn.tga";

            NWButton btnClose = new NWButton(this);
            btnClose.Width = 90;
            btnClose.Height = 30;
            btnClose.Left = Width - 90 - 20;
            btnClose.Top = Height - 30 - 20;
            btnClose.OnClick = OnBtnClose;
            btnClose.OnLangChange = GlobalVars.nwrWin.LangChange;
            btnClose.LangResID = 8;
            btnClose.ImageFile = "itf/DlgBtn.tga";
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fPackList.Dispose();
                fIngredientsList.Dispose();
                fResList.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
