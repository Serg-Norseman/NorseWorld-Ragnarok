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

using System;
using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Database;
using NWR.Game;
using NWR.GUI.Controls;
using NWR.Items;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class SmithyWindow : NWWindow
    {
        private readonly EntityList fIngredients;
        private readonly ListBox fIngredientsList;
        private readonly ListBox fPackList;
        private readonly ListBox fResList;

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        private void OnBtnSmithery(object sender)
        {
            int idx = fResList.SelIndex;
            if (idx < 0) {
                return;
            }

            int id;
            if (idx == 0) {
                id = GlobalVars.iid_Ingot;
            } else {
                id = ((ItemEntry)fResList.Items.GetItem(idx).Data).GUID;
            }

            int res = GlobalVars.nwrGame.Player.Craft.ForgeItem(fIngredients, id);

            if (res != Craft.RC_Ok) {
                if (res == Craft.RC_DifferentMetals) {
                    // dummy
                }
            } else {
                fResList.Items.Add(BaseLocale.GetStr(RS.rs_MeltInIngot), null);
                UpdateView();
            }
        }

        private void UpdateView()
        {
            fPackList.Items.Clear();
            fIngredientsList.Items.Clear();
            fResList.Items.Clear();

            EntityList items = GlobalVars.nwrGame.Player.Items;
            MaterialKind mat = MaterialKind.mk_None;
            int num = items.Count;
            for (int i = 0; i < num; i++) {
                Item item = (Item)items.GetItem(i);
                mat = item.Entry.Material;
                if (mat != MaterialKind.mk_None && fIngredients.FindByGUID(item.UID_Renamed) == null) {
                    AddListItem(fPackList, GetItemFullName(item), item, true);
                }
            }

            int num2 = fIngredients.Count;
            for (int i = 0; i < num2; i++) {
                Item item = (Item)fIngredients.GetItem(i);
                AddListItem(fIngredientsList, GetItemFullName(item), item, true);
            }

            float sum = 0F;
            int res = GlobalVars.nwrGame.Player.Craft.CheckForgeIngredients(fIngredients, ref sum, ref mat);

            if (res != Craft.RC_Ok) {
                if (res == Craft.RC_DifferentMetals) {
                    fResList.Items.Add(BaseLocale.GetStr(RS.rs_DifferentMetals), null);
                }
            } else {
                ItemEntry iEntry = (ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Ingot);
                var ikRec = StaticData.dbItemKinds[(int)iEntry.ItmKind];

                if (iEntry.Material == mat) {
                    LBItem listItem = fResList.Items.Add(BaseLocale.GetStr(RS.rs_MeltInIngot), null);
                    listItem.ImageIndex = iEntry.ImageIndex;
                    listItem.Color = ikRec.Color;
                }

                int num3 = GlobalVars.dbWeapon.Count;
                for (int i = 0; i < num3; i++) {
                    iEntry = ((ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.dbWeapon[i]));
                    if (iEntry.Material == mat && iEntry.Weight <= sum && !iEntry.Unique && !iEntry.Meta) {
                        LBItem listItem = fResList.Items.Add(GetItemFullName(iEntry), iEntry);
                        listItem.ImageIndex = iEntry.ImageIndex;
                        listItem.Color = ikRec.Color;
                    }
                }
            }
        }

        private void OnIngrDragDrop(object sender, object source, int x, int y)
        {
            int idx = fPackList.SelIndex;
            if (source.Equals(fPackList) && idx >= 0 && idx < fPackList.Items.Count) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                fIngredients.Add(item);
                UpdateView();
            }
        }

        private void OnIngrDragOver(object sender, object source, int x, int y, ref bool accept)
        {
            int idx = fPackList.SelIndex;
            accept = (source.Equals(fPackList) && idx >= 0 && idx < fPackList.Items.Count);
            if (accept) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                accept = (fIngredients.FindByGUID(item.UID_Renamed) == null);
            }
        }

        private void OnIngrDragStart(object sender, ref BaseDragObject dragObject)
        {
            int idx = fIngredientsList.SelIndex;
            if (idx >= 0 && idx < fIngredientsList.Items.Count) {
                Item item = (Item)((fIngredientsList.Items.GetItem(idx).Data is Item) ? fIngredientsList.Items.GetItem(idx).Data : null);
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
                fIngredients.Remove((GameEntity)((fIngredientsList.Items.GetItem(idx).Data is GameEntity) ? fIngredientsList.Items.GetItem(idx).Data : null));
                UpdateView();
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
                if (fIngredients.FindByGUID(item.UID_Renamed) == null) {
                    dragObject = new InventoryObject();
                    ((InventoryObject)dragObject).InvItem = item;
                } else {
                    dragObject = null;
                }
            }
        }

        private static string GetItemFullName(object itemObj)
        {
            float iWeight = 0.0f;
            MaterialKind iMaterial = MaterialKind.mk_None;
            string result = "";

            if (itemObj is Item) {
                Item item = (Item)itemObj;
                iWeight = item.Weight;
                iMaterial = item.Material;
                result = item.Name;
            } else {
                if (itemObj is ItemEntry) {
                    ItemEntry itemEntry = (ItemEntry)itemObj;
                    iWeight = itemEntry.Weight;
                    iMaterial = itemEntry.Material;
                    result = itemEntry.Name;
                }
            }

            result = result + "\r\n" + BaseLocale.GetStr(RS.rs_Weight) + ": " + Convert.ToString((double)iWeight) + "; " + BaseLocale.GetStr(StaticData.dbMaterialKind[(int)iMaterial].Name);

            return result;
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);
            CtlCommon.SmFont.Color = Colors.Gold;

            ExtRect pakRt = fPackList.Bounds; // 8
            ExtRect ingRt = fIngredientsList.Bounds; // 6
            ExtRect resRt = fResList.Bounds; // 7

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
            int h = GlobalVars.nwrWin.Resources.GetImage(0).Height;
            int w = GlobalVars.nwrWin.Resources.GetImage(0).Width;

            fPackList.ImagesList = GlobalVars.nwrWin.Resources;
            fPackList.IconHeight = h;
            fPackList.IconWidth = w;

            fIngredientsList.ImagesList = GlobalVars.nwrWin.Resources;
            fIngredientsList.IconHeight = h;
            fIngredientsList.IconWidth = w;

            fResList.ImagesList = GlobalVars.nwrWin.Resources;
            fResList.IconHeight = h;
            fResList.IconWidth = w;
            UpdateView();
        }

        public SmithyWindow(BaseControl owner)
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
            fPackList.Options.Include(LBOptions.lboIcons, LBOptions.lboTextTop, LBOptions.lboDoubleItem);
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
            fIngredientsList.Options.Include(LBOptions.lboIcons, LBOptions.lboTextTop, LBOptions.lboDoubleItem);
            fIngredientsList.ShowHints = true;
            fIngredientsList.OnMouseMove = OnListMouseMove;

            fResList = new ListBox(this);
            fResList.Bounds = ExtRect.Create(10, 225, 290, 398);
            fResList.Visible = true;
            fResList.Options.Include(LBOptions.lboIcons, LBOptions.lboTextTop, LBOptions.lboDoubleItem);
            fResList.ShowHints = true;
            fResList.OnMouseMove = OnListMouseMove;

            NWButton btnForge = new NWButton(this);
            btnForge.Width = 90;
            btnForge.Height = 30;
            btnForge.Left = fPackList.Left + 20;
            btnForge.Top = Height - 30 - 20;
            btnForge.OnClick = OnBtnSmithery;
            btnForge.OnLangChange = GlobalVars.nwrWin.LangChange;
            btnForge.LangResID = 132;
            btnForge.ImageFile = "itf/DlgBtn.tga";

            NWButton btnClose = new NWButton(this);
            btnClose.Width = 90;
            btnClose.Height = 30;
            btnClose.Left = Width - 90 - 20;
            btnClose.Top = Height - 30 - 20;
            btnClose.OnClick = OnBtnClose;
            btnClose.OnLangChange = GlobalVars.nwrWin.LangChange;
            btnClose.LangResID = 8;
            btnClose.ImageFile = "itf/DlgBtn.tga";

            fIngredients = new EntityList(null, false);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fIngredients.Dispose();
                fPackList.Dispose();
                fIngredientsList.Dispose();
                fResList.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
