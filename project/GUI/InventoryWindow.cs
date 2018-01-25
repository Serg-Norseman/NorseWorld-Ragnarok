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

using System;
using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Effects;
using NWR.Game;
using NWR.GUI.Controls;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class InventoryWindow : NWWindow
    {
        public const int IWMODE_GROUND = 0;
        public const int IWMODE_TRADER = 1;
        public const int IWMODE_INSHOP = 2;
        public const int IWMODE_MERCENARY = 3;

        private readonly ListBox fEquipList;
        private readonly ListBox fOutList;
        private readonly ListBox fPackList;
        private readonly NWButton fPayBtn;

        private Item fContainer;
        private int fMode;
        private int fSelPack;
        private int fSelEquip;
        private int fSelOut;

        public NWCreature Collocutor;

        private void OnItemDraw(object sender, BaseScreen screen, int itemIndex, ExtRect itemRect)
        {
            ListBox box = (ListBox)sender;

            if (box.Options.Contains(LBOptions.lboIcons) && GlobalVars.nwrWin.InventoryOnlyIcons) {
                LBItem listItem = box.Items.GetItem(itemIndex);
                Item item = (Item)listItem.Data;

                if (item != null && item.Countable && item.Count > 1) {
                    int tx = itemRect.Left + 2 + box.IconWidth - 1 - box.Font.GetTextWidth("+");
                    int ty = itemRect.Top + (box.ItemHeight - box.IconHeight) / 2;

                    screen.SetTextColor(BaseScreen.clLime, true);
                    screen.DrawText(tx, ty, "+", 0);
                }
            }
        }

        private void OnCommonKeyDown(object sender, KeyEventArgs eventArgs)
        {
            switch (eventArgs.Key) {
                case Keys.GK_F1:
                    if (ActiveControl is ListBox) {
                        ListBox box = (ListBox)ActiveControl;
                        int idx = box.SelIndex;
                        if (idx >= 0 && idx < box.Items.Count) {
                            Item item = (Item)box.Items.GetItem(idx).Data;
                            GlobalVars.nwrWin.ShowKnowledge(item.Entry.Name);
                        }
                    }
                    break;

                case Keys.GK_TAB:
                    if (ActiveControl.Equals(fEquipList)) {
                        ActiveControl = fPackList;
                    } else if (ActiveControl.Equals(fPackList)) {
                        ActiveControl = fOutList;
                    } else if (ActiveControl.Equals(fOutList)) {
                        ActiveControl = fEquipList;
                    }
                    break;

                case Keys.GK_RETURN:
                    if (ActiveControl.Equals(fEquipList) && fEquipList.SelIndex >= 0) {
                        Equip_ItemSelect(fEquipList, MouseButton.mbRight, fEquipList.Items.GetItem(fEquipList.SelIndex));
                    } else if (ActiveControl.Equals(fPackList) && fPackList.SelIndex >= 0) {
                        Pack_ItemSelect(fPackList, MouseButton.mbRight, fPackList.Items.GetItem(fPackList.SelIndex));
                    } else if (ActiveControl.Equals(fOutList) && fOutList.SelIndex >= 0) {
                        Out_ItemSelect(fOutList, MouseButton.mbRight, fOutList.Items.GetItem(fOutList.SelIndex));
                    }
                    break;

                case Keys.GK_UP:
                    if ((eventArgs.Shift.Contains(ShiftStates.SsCtrl))) {
                        if (ActiveControl.Equals(fPackList)) {
                            PackToEquip();
                        } else {
                            if (ActiveControl.Equals(fOutList)) {
                                OutToPack();
                            }
                        }
                    }
                    break;

                case Keys.GK_DOWN:
                    if ((eventArgs.Shift.Contains(ShiftStates.SsCtrl))) {
                        if (ActiveControl.Equals(fEquipList)) {
                            EquipToPack();
                        } else {
                            if (ActiveControl.Equals(fPackList)) {
                                PackToOut();
                            }
                        }
                    }
                    break;

                case Keys.GK_RIGHT:
                case Keys.GK_LEFT:
                    break;
            }
        }

        private void Equip_ItemSelect(object sender, MouseButton button, LBItem item)
        {
            if (button == MouseButton.mbRight) {
                Item itm = (Item)item.Data;
                UseInvItem(itm);
                UpdateEquipmentLists();
            }
        }

        private void Pack_ItemSelect(object sender, MouseButton button, LBItem item)
        {
            if (button == MouseButton.mbRight) {
                Item itm = (Item)item.Data;
                UseInvItem(itm);
                UpdateEquipmentLists();
            }
        }

        private void Out_ItemSelect(object sender, MouseButton button, LBItem item)
        {
            if (button == MouseButton.mbRight) {
                if (fMode != IWMODE_GROUND) {
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_UseWarning));
                } else {
                    Item itm = (Item)item.Data;
                    if (itm.EquipmentKind == BodypartType.bp_None) {
                        UseInvItem(itm);
                    } else {
                        TakeInvItem(itm, false);
                    }
                    UpdateEquipmentLists();
                }
            }
        }

        private void OnPackDragDrop(object sender, object source, int x, int y)
        {
            SaveSelections();
            if (source.Equals(fEquipList)) {
                EquipToPack();
            }
            if (source.Equals(fOutList)) {
                OutToPack();
            }
            RestoreSelections();
        }

        private void OnPackDragOver(object sender, object source, int X, int Y, ref bool accept)
        {
            accept = (source.Equals(fOutList) || source.Equals(fEquipList));
        }

        private void OnPackDragStart(object sender, ref BaseDragObject dragObject)
        {
            int idx = fPackList.SelIndex;
            if (idx >= 0 && idx < fPackList.Items.Count) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                dragObject = new InventoryObject();
                ((InventoryObject)dragObject).InvItem = item;
            }
        }

        private void OnEquipDragDrop(object sender, object source, int x, int y)
        {
            SaveSelections();
            if (source.Equals(fPackList)) {
                PackToEquip();
            }
            if (source.Equals(fOutList)) {
                OutToEquip();
            }
            RestoreSelections();
        }

        private void OnEquipDragOver(object sender, object source, int X, int Y, ref bool accept)
        {
            bool eqAccept = false;
            int idx = fPackList.SelIndex;
            if (source.Equals(fPackList) && idx >= 0 && idx < fPackList.Items.Count) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                if (!item.Equals(fContainer)) {
                    eqAccept = (fContainer != null || GlobalVars.nwrGame.Player.CanBeUsed(item));
                }
            }
            accept = (eqAccept || source.Equals(fOutList));
        }

        private void OnEquipDragStart(object sender, ref BaseDragObject dragObject)
        {
            int idx = fEquipList.SelIndex;
            if (idx >= 0 && idx < fEquipList.Items.Count) {
                Item item = (Item)fEquipList.Items.GetItem(idx).Data;
                dragObject = new InventoryObject();
                ((InventoryObject)dragObject).InvItem = item;
            }
        }

        private void OnOutDragDrop(object sender, object source, int x, int y)
        {
            SaveSelections();
            if (source.Equals(fPackList)) {
                PackToOut();
            }
            if (source.Equals(fEquipList)) {
                EquipToOut();
            }
            RestoreSelections();
        }

        private void OnOutDragOver(object sender, object source, int X, int Y, ref bool accept)
        {
            ListBox box = (ListBox)source;
            Item item = (Item)box.Items.GetItem(box.SelIndex).Data;
            accept = ((source.Equals(fPackList) || source.Equals(fEquipList)) && !item.Equals(fContainer));
        }

        private void OnOutDragStart(object sender, ref BaseDragObject  dragObject)
        {
            int idx = fOutList.SelIndex;
            if (idx >= 0 && idx < fOutList.Items.Count) {
                Item item = (Item)fOutList.Items.GetItem(idx).Data;
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
            NWWindow.OnListMouseMove(sender, eventArgs.X, eventArgs.Y, fMode > IWMODE_GROUND);
        }

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        private void OnBtnPay(object sender)
        {
            Player player = GlobalVars.nwrGame.Player;

            int debt;
            if (Collocutor == null) {
                debt = 0;
            } else {
                debt = player.GetDebt(Collocutor.Name);
            }

            int sum;
            if (player.Money >= debt) {
                sum = debt;
            } else {
                sum = player.Money;
            }
            player.SubMoney(sum);
            player.SubDebt(Collocutor.Name, sum);
            Collocutor.AddMoney(sum);
        }

        private void DropInvItem(Item aItem, bool aUnequip)
        {
            if (aUnequip) {
                GlobalVars.nwrGame.DoPlayerAction(CreatureAction.caItemRemove, aItem.UID_Renamed);
            }
            Player player = GlobalVars.nwrGame.Player;
            if (!aItem.Equipment || !aItem.InUse) {
                if (fMode != IWMODE_GROUND) {
                    if (Collocutor.CanBuy(aItem, player)) {
                        Collocutor.Buy(aItem, player, fMode == IWMODE_INSHOP);
                    }
                } else {
                    GlobalVars.nwrGame.DoPlayerAction(CreatureAction.caItemDrop, aItem.UID_Renamed);
                }
            }
        }

        private void UpdateEquipmentLists()
        {
            try {
                fPackList.Items.Clear();
                fEquipList.Items.Clear();
                fOutList.Items.Clear();

                bool onlyIcons = GlobalVars.nwrWin.InventoryOnlyIcons;
                Player player = GlobalVars.nwrGame.Player;
                NWField fld = (NWField)player.CurrentMap;
                bool blindness = player.Blindness;

                int debt = 0;
                fMode = IWMODE_GROUND;
                if (Collocutor != null) {
                    debt = player.GetDebt(Collocutor.Name);
                    if (Collocutor.IsTrader) {
                        Building house = (Building)Collocutor.FindHouse();

                        if (house.Area.Contains(player.PosX, player.PosY)) {
                            fMode = IWMODE_INSHOP;
                        } else {
                            fMode = IWMODE_TRADER;
                        }
                    } else {
                        if (Collocutor.Mercenary) {
                            fMode = IWMODE_MERCENARY;
                        }
                    }
                }

                fPayBtn.Enabled = (fMode != IWMODE_GROUND && debt != 0);

                switch (fMode) {
                    case IWMODE_GROUND:
                        {
                            EntityList items = player.Items;

                            int num5 = items.Count;
                            for (int i = 0; i < num5; i++) {
                                Item item = (Item)items.GetItem(i);
                                if (!item.Identified && player.Memory.Find(item.Entry.Sign) != null) {
                                    item.Identified = true;
                                }

                                if (item.Equipment && item.InUse && fContainer == null) {
                                    AddListItem(fEquipList, item.GetName(blindness), item, onlyIcons);
                                } else {
                                    AddListItem(fPackList, item.GetName(blindness), item, onlyIcons);
                                }
                            }

                            ExtList<LocatedEntity> groundItems = fld.Items.SearchListByPos(player.PosX, player.PosY);

                            int num6 = groundItems.Count;
                            for (int i = 0; i < num6; i++) {
                                Item item = (Item)groundItems[i];
                                if (!item.Identified && player.Memory.Find(item.Entry.Sign) != null) {
                                    item.Identified = true;
                                }
                                AddListItem(fOutList, item.GetName(blindness), item, onlyIcons);
                            }
                            groundItems.Dispose();
                        }
                        break;

                    case IWMODE_TRADER:
                    case IWMODE_INSHOP:
                        {
                            EntityList items = player.Items;

                            int num3 = items.Count;
                            for (int i = 0; i < num3; i++) {
                                Item item = (Item)items.GetItem(i);
                                if (!item.Identified && player.Memory.Find(item.Entry.Sign) != null) {
                                    item.Identified = true;
                                }

                                if (item.Equipment && item.InUse && fContainer == null) {
                                    AddListItem(fEquipList, item.GetName(blindness), item, onlyIcons);
                                } else {
                                    if (item.Ware) {
                                        AddListItem(fPackList, item.GetName(blindness) + " (" + Convert.ToString((int)item.GetTradePrice(Collocutor, player)) + "$)", item, onlyIcons);
                                    } else {
                                        AddListItem(fPackList, item.GetName(blindness), item, onlyIcons);
                                    }
                                }
                            }

                            Building house = fld.FindBuilding(player.PosX, player.PosY);
                            if (house != null) {
                                if (debt > 0) {
                                    house.SwitchDoors(Door.STATE_CLOSED);
                                } else {
                                    house.SwitchDoors(Door.STATE_OPENED);
                                }
                                ExtList<LocatedEntity> groundItems = fld.Items.SearchListByArea(house.Area);

                                int num4 = groundItems.Count;
                                for (int i = 0; i < num4; i++) {
                                    Item item = (Item)groundItems[i];
                                    item.Identified = true;
                                    AddListItem(fOutList, item.GetName(blindness) + " (" + Convert.ToString((int)item.GetTradePrice(player, Collocutor)) + "$)", item, onlyIcons);
                                }

                                groundItems.Dispose();
                            }
                        }
                        break;

                    case IWMODE_MERCENARY:
                        {
                            EntityList items = player.Items;

                            int num = items.Count;
                            for (int i = 0; i < num; i++) {
                                Item item = (Item)items.GetItem(i);
                                if (!item.Identified && player.Memory.Find(item.Entry.Sign) != null) {
                                    item.Identified = true;
                                }
                                if (item.Equipment && item.InUse && fContainer == null) {
                                    AddListItem(fEquipList, item.GetName(blindness), item, onlyIcons);
                                } else {
                                    if (item.Ware) {
                                        AddListItem(fPackList, item.GetName(blindness) + " (" + Convert.ToString(item.Price) + "$)", item, onlyIcons);
                                    } else {
                                        AddListItem(fPackList, item.GetName(blindness), item, onlyIcons);
                                    }
                                }
                            }
                            items = Collocutor.Items;

                            int num2 = items.Count;
                            for (int i = 0; i < num2; i++) {
                                Item item = (Item)items.GetItem(i);
                                AddListItem(fOutList, item.GetName(blindness) + " (" + Convert.ToString(item.Price) + "$)", item, onlyIcons);
                            }
                        }
                        break;
                }

                if (fContainer != null) {
                    EntityList items = fContainer.Contents;

                    int num7 = items.Count;
                    for (int i = 0; i < num7; i++) {
                        Item item = (Item)items.GetItem(i);
                        if (!item.Identified && player.Memory.Find(item.Entry.Sign) != null) {
                            item.Identified = true;
                        }
                        AddListItem(fEquipList, item.GetName(blindness), item, onlyIcons);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("InventoryWindow.updateEquipmentLists(): " + ex.Message);
                throw ex;
            }
        }

        private void EquipToOut()
        {
            int idx = fEquipList.SelIndex;
            if (idx >= 0 && idx < fEquipList.Items.Count) {
                Item item = (Item)fEquipList.Items.GetItem(idx).Data;
                if (fContainer != null) {
                    GetFromBag(item, GlobalVars.nwrGame.Player.Items);
                }
                DropInvItem(item, true);
                UpdateEquipmentLists();
            }
        }

        private void EquipToPack()
        {
            int idx = fEquipList.SelIndex;
            if (idx >= 0 && idx < fEquipList.Items.Count) {
                Item item = (Item)fEquipList.Items.GetItem(idx).Data;
                if (fContainer == null) {
                    GlobalVars.nwrGame.DoPlayerAction(CreatureAction.caItemRemove, item.UID_Renamed);
                } else {
                    GetFromBag(item, GlobalVars.nwrGame.Player.Items);
                }
                UpdateEquipmentLists();
            }
        }

        private void GetFromBag(Item aItem, EntityList aToList)
        {
            fContainer.Contents.Extract(aItem);
            aToList.Add(aItem);
        }

        private void OutToEquip()
        {
            int idx = fOutList.SelIndex;
            if (idx >= 0 && idx < fOutList.Items.Count) {
                Item item = (Item)fOutList.Items.GetItem(idx).Data;
                if (!item.Equals(fContainer)) {
                    TakeInvItem(item, true);
                    if (fContainer != null) {
                        PutToBag(item, GlobalVars.nwrGame.Player.CurrentField.Items);
                    }
                    UpdateEquipmentLists();
                }
            }
        }

        private void OutToPack()
        {
            int idx = fOutList.SelIndex;
            if (idx >= 0 && idx < fOutList.Items.Count) {
                Item item = (Item)fOutList.Items.GetItem(idx).Data;
                TakeInvItem(item, false);
                UpdateEquipmentLists();
            }
        }

        private void PackToEquip()
        {
            int idx = fPackList.SelIndex;
            if (idx >= 0 && idx < fPackList.Items.Count) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                if (!item.Equals(fContainer)) {
                    if (fContainer == null) {
                        GlobalVars.nwrGame.DoPlayerAction(CreatureAction.caItemWear, item.UID_Renamed);
                    } else {
                        PutToBag(item, GlobalVars.nwrGame.Player.Items);
                    }
                    UpdateEquipmentLists();
                }
            }
        }

        private void PackToOut()
        {
            int idx = fPackList.SelIndex;
            if (idx >= 0 && idx < fPackList.Items.Count) {
                Item item = (Item)fPackList.Items.GetItem(idx).Data;
                if (!item.Equals(fContainer)) {
                    DropInvItem(item, false);
                    UpdateEquipmentLists();
                }
            }
        }

        private void PutToBag(Item aItem, EntityList aFromList)
        {
            aItem.InUse = false;
            aFromList.Extract(aItem);
            fContainer.Contents.Add(aItem);
        }

        private void RestoreSelections()
        {
            if (fSelPack >= fPackList.Items.Count) {
                fPackList.SelIndex = fSelPack - 1;
            } else {
                fPackList.SelIndex = fSelPack;
            }
            if (fSelEquip >= fEquipList.Items.Count) {
                fEquipList.SelIndex = fSelEquip - 1;
            } else {
                fEquipList.SelIndex = fSelEquip;
            }
            if (fSelOut >= fOutList.Items.Count) {
                fOutList.SelIndex = fSelOut - 1;
            } else {
                fOutList.SelIndex = fSelOut;
            }
        }

        private void SaveSelections()
        {
            fSelPack = fPackList.SelIndex;
            fSelEquip = fEquipList.SelIndex;
            fSelOut = fOutList.SelIndex;
        }

        private bool TakeInvItem(Item aItem, bool aEquip)
        {
            bool result = true;
            Player player = GlobalVars.nwrGame.Player;
            if (player.CanTake(aItem, fMode > IWMODE_GROUND)) {
                if (fMode != IWMODE_GROUND) {
                    if (player.CanBuy(aItem, Collocutor)) {
                        player.Buy(aItem, Collocutor, fMode == IWMODE_INSHOP);
                    } else {
                        result = false;
                    }
                } else {
                    GlobalVars.nwrGame.DoPlayerAction(CreatureAction.caItemPickup, aItem.UID_Renamed);
                }
            } else {
                result = false;
            }
            if (result & aEquip) {
                GlobalVars.nwrGame.DoPlayerAction(CreatureAction.caItemWear, aItem.UID_Renamed);
            }
            return result;
        }

        private void HideWin()
        {
            Hide();
            GlobalVars.nwrWin.Repaint(10);
        }

        private void UseInvItem(Item item)
        {
            if (GlobalVars.nwrWin.TargetData == null) {
                if (item.EquipmentKind == BodypartType.bp_None) {
                    if (item.Entry.Sign.Equals("RedBag")) {
                        if (fContainer == null) {
                            fContainer = item;
                        } else {
                            fContainer = null;
                        }
                    } else {
                        HideWin();

                        GlobalVars.nwrGame.DoPlayerAction(CreatureAction.caItemUse, item.UID_Renamed);
                    }
                }
            } else {
                if (GlobalVars.nwrWin.TargetData.Target == EffectTarget.et_Item) {
                    HideWin();

                    GlobalVars.nwrWin.TargetData.Ext.SetParam(EffectParams.ep_Item, item);
                    GlobalVars.nwrWin.UseTarget();
                } else {
                    GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(StaticData.dbEffectTarget[(int)EffectTarget.et_Item].Invalid));
                }
            }
        }

        protected override void DoHideEvent()
        {
            base.DoHideEvent();
            GlobalVars.nwrWin.HideHint();
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            try {
                base.DoPaintEvent(screen);

                string eq;
                if (fContainer == null) {
                    eq = BaseLocale.GetStr(RS.rs_ItemsUsed);
                } else {
                    eq = fContainer.Name;
                }
                Player player = GlobalVars.nwrGame.Player;

                int debt;
                if (Collocutor == null) {
                    debt = 0;
                } else {
                    debt = player.GetDebt(Collocutor.Name);
                }

                screen.SetTextColor(BaseScreen.clGold, true);

                ExtRect pakRt = fPackList.Bounds; // 2
                ExtRect eqpRt = fEquipList.Bounds; // 0
                ExtRect outRt = fOutList.Bounds; // 1

                int lcx = pakRt.Left + (pakRt.Width - CtlCommon.SmFont.GetTextWidth(BaseLocale.GetStr(RS.rs_Equipment))) / 2;
                int rcx = eqpRt.Left + (eqpRt.Width - CtlCommon.SmFont.GetTextWidth(eq)) / 2;
                int ocx = outRt.Left + (outRt.Width - CtlCommon.SmFont.GetTextWidth(BaseLocale.GetStr(RS.rs_Ground))) / 2;

                int lcy = pakRt.Top - CtlCommon.SmFont.Height;
                int rcy = eqpRt.Top - CtlCommon.SmFont.Height;
                int ocy = outRt.Top - CtlCommon.SmFont.Height;

                screen.DrawText(lcx, lcy, BaseLocale.GetStr(RS.rs_Equipment), 0);
                screen.DrawText(rcx, rcy, eq, 0);
                screen.DrawText(ocx, ocy, BaseLocale.GetStr(RS.rs_Ground), 0);
                screen.DrawText(outRt.Left, 368, BaseLocale.GetStr(RS.rs_Money) + ": " + Convert.ToString(player.Money), 0);
                screen.DrawText(outRt.Left, 386, BaseLocale.GetStr(RS.rs_Debt) + ": " + Convert.ToString(debt), 0);
                screen.DrawText(outRt.Left, 404, BaseLocale.GetStr(RS.rs_Weight) + ": " + string.Format("{0:F2} / {1:F2}", new object[] {
                    player.TotalWeight,
                    player.MaxItemsWeight
                }), 0);
                screen.DrawText(outRt.Left, 422, BaseLocale.GetStr(RS.rs_EquipTurns), 0);
            } catch (Exception ex) {
                Logger.Write("InventoryWindow.DoPaintTo(): " + ex.Message);
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();

            SetupItemsList(fPackList);
            SetupItemsList(fEquipList);
            SetupItemsList(fOutList);

            UpdateEquipmentLists();

            ActiveControl = fPackList;
        }

        public InventoryWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 600;
            Height = 460;
            WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview);
            Shifted = true;
            OnKeyDown = OnCommonKeyDown;

            fContainer = null;

            fPayBtn = new NWButton(this);
            fPayBtn.Left = Width - 90 - 20 - 90 - 20;
            fPayBtn.Top = Height - 30 - 20;
            fPayBtn.Width = 90;
            fPayBtn.Height = 30;
            fPayBtn.ImageFile = "itf/DlgBtn.tga";
            fPayBtn.OnClick = OnBtnPay;
            fPayBtn.OnLangChange = GlobalVars.nwrWin.LangChange;
            fPayBtn.LangResID = 892;

            NWButton btnClose = new NWButton(this);
            btnClose.Left = Width - 90 - 20;
            btnClose.Top = Height - 30 - 20;
            btnClose.Width = 90;
            btnClose.Height = 30;
            btnClose.ImageFile = "itf/DlgBtn.tga";
            btnClose.OnClick = OnBtnClose;
            btnClose.OnLangChange = GlobalVars.nwrWin.LangChange;
            btnClose.LangResID = 8;

            fPackList = new ListBox(this);
            fPackList.Bounds = ExtRect.Create(309, 28, 589, 367); // 2
            fPackList.OnItemSelect = Pack_ItemSelect;
            fPackList.Visible = true;
            fPackList.OnDragDrop = OnPackDragDrop;
            fPackList.OnDragOver = OnPackDragOver;
            fPackList.OnDragStart = OnPackDragStart;
            fPackList.OnMouseDown = OnListMouseDown;
            fPackList.OnMouseMove = OnListMouseMove;
            fPackList.OnKeyDown = null;
            fPackList.ShowHints = true;
            fPackList.Options.Include(LBOptions.lboIcons);
            fPackList.OnItemDraw = OnItemDraw;

            fEquipList = new ListBox(this);
            fEquipList.Bounds = ExtRect.Create(10, 28, 290, 187); // 0
            fEquipList.OnItemSelect = Equip_ItemSelect;
            fEquipList.Visible = true;
            fEquipList.OnDragDrop = OnEquipDragDrop;
            fEquipList.OnDragOver = OnEquipDragOver;
            fEquipList.OnDragStart = OnEquipDragStart;
            fEquipList.OnMouseDown = OnListMouseDown;
            fEquipList.OnMouseMove = OnListMouseMove;
            fEquipList.OnKeyDown = null;
            fEquipList.ShowHints = true;
            fEquipList.Options.Include(LBOptions.lboIcons);
            fEquipList.OnItemDraw = OnItemDraw;

            fOutList = new ListBox(this);
            fOutList.Bounds = ExtRect.Create(10, 208, 290, 367); // 1
            fOutList.OnItemSelect = Out_ItemSelect;
            fOutList.Visible = true;
            fOutList.OnDragDrop = OnOutDragDrop;
            fOutList.OnDragOver = OnOutDragOver;
            fOutList.OnDragStart = OnOutDragStart;
            fOutList.OnMouseDown = OnListMouseDown;
            fOutList.OnMouseMove = OnListMouseMove;
            fOutList.OnKeyDown = null;
            fOutList.ShowHints = true;
            fOutList.Options.Include(LBOptions.lboIcons);
            fOutList.OnItemDraw = OnItemDraw;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fOutList.Dispose();
                fPackList.Dispose();
                fEquipList.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
