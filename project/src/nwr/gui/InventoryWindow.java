/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
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
package nwr.gui;

import jzrlib.core.EntityList;
import jzrlib.core.ExtList;
import jzrlib.core.LocatedEntity;
import jzrlib.utils.Logger;
import jzrlib.core.Rect;
import jzrlib.utils.RefObject;
import nwr.creatures.BodypartType;
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.types.CreatureAction;
import nwr.effects.EffectParams;
import nwr.effects.EffectTarget;
import nwr.engine.BaseControl;
import nwr.engine.BaseDragObject;
import nwr.engine.BaseScreen;
import nwr.engine.KeyEventArgs;
import nwr.engine.MouseButton;
import nwr.engine.MouseEventArgs;
import nwr.engine.MouseMoveEventArgs;
import nwr.engine.ShiftStates;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.LBOptions;
import nwr.gui.controls.ListBox;
import nwr.item.Item;
import nwr.main.GlobalVars;
import nwr.universe.Door;
import nwr.player.Player;
import nwr.universe.Building;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class InventoryWindow extends NWWindow
{
    public static final int IWMODE_GROUND = 0;
    public static final int IWMODE_TRADER = 1;
    public static final int IWMODE_INSHOP = 2;
    public static final int IWMODE_MERCENARY = 3;
    
    private final ListBox fEquipList;
    private final ListBox fOutList;
    private final ListBox fPackList;
    private final NWButton fPayBtn;

    private Item fContainer;
    private int fMode;
    private int fSelPack;
    private int fSelEquip;
    private int fSelOut;

    public NWCreature Collocutor;

    private void onItemDraw(Object sender, BaseScreen screen, int itemIndex, Rect itemRect)
    {
        ListBox box = (ListBox) sender;
        
        if (box.Options.contains(LBOptions.lboIcons) && GlobalVars.nwrWin.getInventoryOnlyIcons()) {
            LBItem listItem = box.getItems().getItem(itemIndex);
            Item item = (Item) listItem.Data;

            if (item != null && item.isCountable() && item.Count > 1) {
                int tx = itemRect.Left + 2 + box.getIconWidth() - 1 - box.getFont().getTextWidth("+");
                int ty = itemRect.Top + (box.getItemHeight() - box.getIconHeight()) / 2;

                screen.setTextColor(BaseScreen.clLime, true);
                screen.drawText(tx, ty, "+", 0);
            }
        }
    }
    
    private void onCommonKeyDown(Object sender, KeyEventArgs eventArgs)
    {
        switch (eventArgs.Key) {
            case GK_F1:
                if (super.getActiveControl() instanceof ListBox) {
                    ListBox box = (ListBox) super.getActiveControl();
                    int idx = box.getSelIndex();
                    if (idx >= 0 && idx < box.getItems().getCount()) {
                        Item item = (Item) box.getItems().getItem(idx).Data;
                        GlobalVars.nwrWin.showKnowledge(item.getEntry().getName());
                    }
                }
                break;

            case GK_TAB:
                if (super.getActiveControl().equals(this.fEquipList)) {
                    super.setActiveControl(this.fPackList);
                } else if (super.getActiveControl().equals(this.fPackList)) {
                    super.setActiveControl(this.fOutList);
                } else if (super.getActiveControl().equals(this.fOutList)) {
                    super.setActiveControl(this.fEquipList);
                }
                break;

            case GK_RETURN:
                if (super.getActiveControl().equals(this.fEquipList) && this.fEquipList.getSelIndex() >= 0) {
                    this.Equip_ItemSelect(this.fEquipList, MouseButton.mbRight, this.fEquipList.getItems().getItem(this.fEquipList.getSelIndex()));
                } else if (super.getActiveControl().equals(this.fPackList) && this.fPackList.getSelIndex() >= 0) {
                    this.Pack_ItemSelect(this.fPackList, MouseButton.mbRight, this.fPackList.getItems().getItem(this.fPackList.getSelIndex()));
                } else if (super.getActiveControl().equals(this.fOutList) && this.fOutList.getSelIndex() >= 0) {
                    this.Out_ItemSelect(this.fOutList, MouseButton.mbRight, this.fOutList.getItems().getItem(this.fOutList.getSelIndex()));
                }
                break;

            case GK_UP:
                if ((eventArgs.Shift.contains(ShiftStates.ssCtrl))) {
                    if (super.getActiveControl().equals(this.fPackList)) {
                        this.packToEquip();
                    } else {
                        if (super.getActiveControl().equals(this.fOutList)) {
                            this.outToPack();
                        }
                    }
                }
                break;

            case GK_DOWN:
                if ((eventArgs.Shift.contains(ShiftStates.ssCtrl))) {
                    if (super.getActiveControl().equals(this.fEquipList)) {
                        this.equipToPack();
                    } else {
                        if (super.getActiveControl().equals(this.fPackList)) {
                            this.packToOut();
                        }
                    }
                }
                break;

            case GK_RIGHT:
            case GK_LEFT:
                break;
        }
    }

    private void Equip_ItemSelect(Object sender, MouseButton Button, LBItem Item)
    {
        if (Button == MouseButton.mbRight) {
            Item itm = (Item) Item.Data;
            this.useInvItem(itm);
            this.updateEquipmentLists();
        }
    }

    private void Pack_ItemSelect(Object sender, MouseButton Button, LBItem Item)
    {
        if (Button == MouseButton.mbRight) {
            Item itm = (Item) Item.Data;
            this.useInvItem(itm);
            this.updateEquipmentLists();
        }
    }

    private void Out_ItemSelect(Object Sender, MouseButton Button, LBItem Item)
    {
        if (Button == MouseButton.mbRight) {
            if (this.fMode != IWMODE_GROUND) {
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_UseWarning));
            } else {
                Item itm = (Item) Item.Data;
                if (itm.getEquipmentKind() == BodypartType.bp_None) {
                    this.useInvItem(itm);
                } else {
                    this.takeInvItem(itm, false);
                }
                this.updateEquipmentLists();
            }
        }
    }

    private void onPackDragDrop(Object sender, Object Source, int X, int Y)
    {
        this.saveSelections();
        if (Source.equals(this.fEquipList)) {
            this.equipToPack();
        }
        if (Source.equals(this.fOutList)) {
            this.outToPack();
        }
        this.restoreSelections();
    }

    private void onPackDragOver(Object sender, Object Source, int X, int Y, RefObject<Boolean> Accept)
    {
        Accept.argValue = (Source.equals(this.fOutList) || Source.equals(this.fEquipList));
    }

    private void onPackDragStart(Object sender, RefObject<BaseDragObject> DragObject)
    {
        int idx = this.fPackList.getSelIndex();
        if (idx >= 0 && idx < this.fPackList.getItems().getCount()) {
            Item item = (Item) this.fPackList.getItems().getItem(idx).Data;
            DragObject.argValue = new InventoryObject();
            ((InventoryObject) DragObject.argValue).InvItem = item;
        }
    }

    private void onEquipDragDrop(Object Sender, Object Source, int X, int Y)
    {
        this.saveSelections();
        if (Source.equals(this.fPackList)) {
            this.packToEquip();
        }
        if (Source.equals(this.fOutList)) {
            this.outToEquip();
        }
        this.restoreSelections();
    }

    private void onEquipDragOver(Object Sender, Object Source, int X, int Y, RefObject<Boolean> Accept)
    {
        boolean eqAccept = false;
        int idx = this.fPackList.getSelIndex();
        if (Source.equals(this.fPackList) && idx >= 0 && idx < this.fPackList.getItems().getCount()) {
            Item item = (Item) this.fPackList.getItems().getItem(idx).Data;
            if (!item.equals(this.fContainer)) {
                eqAccept = (this.fContainer != null || GlobalVars.nwrGame.getPlayer().canBeUsed(item));
            }
        }
        Accept.argValue = (eqAccept || Source.equals(this.fOutList));
    }

    private void onEquipDragStart(Object Sender, RefObject<BaseDragObject> DragObject)
    {
        int idx = this.fEquipList.getSelIndex();
        if (idx >= 0 && idx < this.fEquipList.getItems().getCount()) {
            Item item = (Item) this.fEquipList.getItems().getItem(idx).Data;
            DragObject.argValue = new InventoryObject();
            ((InventoryObject) DragObject.argValue).InvItem = item;
        }
    }

    private void onOutDragDrop(Object Sender, Object Source, int X, int Y)
    {
        this.saveSelections();
        if (Source.equals(this.fPackList)) {
            this.packToOut();
        }
        if (Source.equals(this.fEquipList)) {
            this.equipToOut();
        }
        this.restoreSelections();
    }

    private void onOutDragOver(Object Sender, Object Source, int X, int Y, RefObject<Boolean> Accept)
    {
        ListBox box = (ListBox) Source;
        Item item = (Item) box.getItems().getItem(box.getSelIndex()).Data;
        Accept.argValue = ((Source.equals(this.fPackList) || Source.equals(this.fEquipList)) && !item.equals(this.fContainer));
    }

    private void onOutDragStart(Object Sender, RefObject<BaseDragObject> DragObject)
    {
        int idx = this.fOutList.getSelIndex();
        if (idx >= 0 && idx < this.fOutList.getItems().getCount()) {
            Item item = (Item) this.fOutList.getItems().getItem(idx).Data;
            DragObject.argValue = new InventoryObject();
            ((InventoryObject) DragObject.argValue).InvItem = item;
        }
    }

    private void onListMouseDown(Object Sender, MouseEventArgs eventArgs)
    {
        if (eventArgs.Button == MouseButton.mbLeft) {
            ListBox aList = (ListBox) Sender;
            if (aList.getItems().getCount() > 0 && aList.getSelIndex() >= 0) {
                aList.BeginDrag(false);
            }
        }
    }

    private void onListMouseMove(Object Sender, MouseMoveEventArgs eventArgs)
    {
        NWWindow.onListMouseMove(Sender, eventArgs.X, eventArgs.Y, this.fMode > IWMODE_GROUND);
    }

    private void onBtnClose(Object Sender)
    {
        this.hide();
    }

    private void onBtnPay(Object Sender)
    {
        Player player = GlobalVars.nwrGame.getPlayer();

        int debt;
        if (this.Collocutor == null) {
            debt = 0;
        } else {
            debt = player.getDebt(this.Collocutor.getName());
        }

        int sum;
        if (player.getMoney() >= debt) {
            sum = debt;
        } else {
            sum = player.getMoney();
        }
        player.subMoney(sum);
        player.subDebt(this.Collocutor.getName(), sum);
        this.Collocutor.addMoney(sum);
    }

    private void dropInvItem(Item aItem, boolean aUnequip)
    {
        if (aUnequip) {
            GlobalVars.nwrGame.doPlayerAction(CreatureAction.caItemRemove, aItem.UID);
        }
        Player player = GlobalVars.nwrGame.getPlayer();
        if (!aItem.isEquipment() || !aItem.getInUse()) {
            if (this.fMode != IWMODE_GROUND) {
                if (this.Collocutor.canBuy(aItem, player)) {
                    this.Collocutor.buy(aItem, player, this.fMode == IWMODE_INSHOP);
                }
            } else {
                GlobalVars.nwrGame.doPlayerAction(CreatureAction.caItemDrop, aItem.UID);
            }
        }
    }

    private void updateEquipmentLists()
    {
        try {
            this.fPackList.getItems().clear();
            this.fEquipList.getItems().clear();
            this.fOutList.getItems().clear();

            boolean onlyIcons = GlobalVars.nwrWin.getInventoryOnlyIcons();
            Player player = GlobalVars.nwrGame.getPlayer();
            NWField fld = (NWField) player.getCurrentMap();
            boolean blindness = player.isBlindness();

            int debt = 0;
            this.fMode = IWMODE_GROUND;
            if (this.Collocutor != null) {
                debt = player.getDebt(this.Collocutor.getName());
                if (this.Collocutor.getIsTrader()) {
                    Building house = (Building) this.Collocutor.findHouse();

                    if (house.getArea().contains(player.getPosX(), player.getPosY())) {
                        this.fMode = IWMODE_INSHOP;
                    } else {
                        this.fMode = IWMODE_TRADER;
                    }
                } else {
                    if (this.Collocutor.isMercenary()) {
                        this.fMode = IWMODE_MERCENARY;
                    }
                }
            }

            this.fPayBtn.Enabled = (this.fMode != IWMODE_GROUND && debt != 0);

            switch (this.fMode) {
                case IWMODE_GROUND: {
                    EntityList items = player.getItems();

                    int num5 = items.getCount();
                    for (int i = 0; i < num5; i++) {
                        Item item = (Item) items.getItem(i);
                        if (!item.getIdentified() && player.getMemory().find(item.getEntry().Sign) != null) {
                            item.setIdentified(true);
                        }

                        if (item.isEquipment() && item.getInUse() && this.fContainer == null) {
                            NWWindow.addListItem(this.fEquipList, item.getName(blindness), item, onlyIcons);
                        } else {
                            NWWindow.addListItem(this.fPackList, item.getName(blindness), item, onlyIcons);
                        }
                    }

                    ExtList<LocatedEntity> groundItems = fld.getItems().searchListByPos(player.getPosX(), player.getPosY());

                    int num6 = groundItems.getCount();
                    for (int i = 0; i < num6; i++) {
                        Item item = (Item) groundItems.get(i);
                        if (!item.getIdentified() && player.getMemory().find(item.getEntry().Sign) != null) {
                            item.setIdentified(true);
                        }
                        NWWindow.addListItem(this.fOutList, item.getName(blindness), item, onlyIcons);
                    }
                    groundItems.dispose();
                }
                break;

                case IWMODE_TRADER:
                case IWMODE_INSHOP: {
                    EntityList items = player.getItems();

                    int num3 = items.getCount();
                    for (int i = 0; i < num3; i++) {
                        Item item = (Item) items.getItem(i);
                        if (!item.getIdentified() && player.getMemory().find(item.getEntry().Sign) != null) {
                            item.setIdentified(true);
                        }

                        if (item.isEquipment() && item.getInUse() && this.fContainer == null) {
                            NWWindow.addListItem(this.fEquipList, item.getName(blindness), item, onlyIcons);
                        } else {
                            if (item.isWare()) {
                                NWWindow.addListItem(this.fPackList, item.getName(blindness) + " (" + String.valueOf((int) item.getTradePrice(this.Collocutor, player)) + "$)", item, onlyIcons);
                            } else {
                                NWWindow.addListItem(this.fPackList, item.getName(blindness), item, onlyIcons);
                            }
                        }
                    }

                    Building house = fld.findBuilding(player.getPosX(), player.getPosY());
                    if (house != null) {
                        if (debt > 0) {
                            house.switchDoors(Door.STATE_CLOSED);
                        } else {
                            house.switchDoors(Door.STATE_OPENED);
                        }
                        ExtList<LocatedEntity> groundItems = fld.getItems().searchListByArea(house.getArea());

                        int num4 = groundItems.getCount();
                        for (int i = 0; i < num4; i++) {
                            Item item = (Item) groundItems.get(i);
                            item.setIdentified(true);
                            NWWindow.addListItem(this.fOutList, item.getName(blindness) + " (" + String.valueOf((int) item.getTradePrice(player, this.Collocutor)) + "$)", item, onlyIcons);
                        }

                        groundItems.dispose();
                    }
                }
                break;

                case IWMODE_MERCENARY: {
                    EntityList items = player.getItems();

                    int num = items.getCount();
                    for (int i = 0; i < num; i++) {
                        Item item = (Item) items.getItem(i);
                        if (!item.getIdentified() && player.getMemory().find(item.getEntry().Sign) != null) {
                            item.setIdentified(true);
                        }
                        if (item.isEquipment() && item.getInUse() && this.fContainer == null) {
                            NWWindow.addListItem(this.fEquipList, item.getName(blindness), item, onlyIcons);
                        } else {
                            if (item.isWare()) {
                                NWWindow.addListItem(this.fPackList, item.getName(blindness) + " (" + String.valueOf(item.getPrice()) + "$)", item, onlyIcons);
                            } else {
                                NWWindow.addListItem(this.fPackList, item.getName(blindness), item, onlyIcons);
                            }
                        }
                    }
                    items = this.Collocutor.getItems();

                    int num2 = items.getCount();
                    for (int i = 0; i < num2; i++) {
                        Item item = (Item) items.getItem(i);
                        NWWindow.addListItem(this.fOutList, item.getName(blindness) + " (" + String.valueOf(item.getPrice()) + "$)", item, onlyIcons);
                    }
                }
                break;
            }

            if (this.fContainer != null) {
                EntityList items = this.fContainer.getContents();

                int num7 = items.getCount();
                for (int i = 0; i < num7; i++) {
                    Item item = (Item) items.getItem(i);
                    if (!item.getIdentified() && player.getMemory().find(item.getEntry().Sign) != null) {
                        item.setIdentified(true);
                    }
                    NWWindow.addListItem(this.fEquipList, item.getName(blindness), item, onlyIcons);
                }
            }
        } catch (Exception ex) {
            Logger.write("InventoryWindow.updateEquipmentLists(): " + ex.getMessage());
            throw ex;
        }
    }

    private void equipToOut()
    {
        int idx = this.fEquipList.getSelIndex();
        if (idx >= 0 && idx < this.fEquipList.getItems().getCount()) {
            Item item = (Item) this.fEquipList.getItems().getItem(idx).Data;
            if (this.fContainer != null) {
                this.getFromBag(item, GlobalVars.nwrGame.getPlayer().getItems());
            }
            this.dropInvItem(item, true);
            this.updateEquipmentLists();
        }
    }

    private void equipToPack()
    {
        int idx = this.fEquipList.getSelIndex();
        if (idx >= 0 && idx < this.fEquipList.getItems().getCount()) {
            Item item = (Item) this.fEquipList.getItems().getItem(idx).Data;
            if (this.fContainer == null) {
                GlobalVars.nwrGame.doPlayerAction(CreatureAction.caItemRemove, item.UID);
            } else {
                this.getFromBag(item, GlobalVars.nwrGame.getPlayer().getItems());
            }
            this.updateEquipmentLists();
        }
    }

    private void getFromBag(Item aItem, EntityList aToList)
    {
        this.fContainer.getContents().extract(aItem);
        aToList.add(aItem);
    }

    private void outToEquip()
    {
        int idx = this.fOutList.getSelIndex();
        if (idx >= 0 && idx < this.fOutList.getItems().getCount()) {
            Item item = (Item) this.fOutList.getItems().getItem(idx).Data;
            if (!item.equals(this.fContainer)) {
                this.takeInvItem(item, true);
                if (this.fContainer != null) {
                    this.putToBag(item, GlobalVars.nwrGame.getPlayer().getCurrentField().getItems());
                }
                this.updateEquipmentLists();
            }
        }
    }

    private void outToPack()
    {
        int idx = this.fOutList.getSelIndex();
        if (idx >= 0 && idx < this.fOutList.getItems().getCount()) {
            Item item = (Item) this.fOutList.getItems().getItem(idx).Data;
            this.takeInvItem(item, false);
            this.updateEquipmentLists();
        }
    }

    private void packToEquip()
    {
        int idx = this.fPackList.getSelIndex();
        if (idx >= 0 && idx < this.fPackList.getItems().getCount()) {
            Item item = (Item) this.fPackList.getItems().getItem(idx).Data;
            if (!item.equals(this.fContainer)) {
                if (this.fContainer == null) {
                    GlobalVars.nwrGame.doPlayerAction(CreatureAction.caItemWear, item.UID);
                } else {
                    this.putToBag(item, GlobalVars.nwrGame.getPlayer().getItems());
                }
                this.updateEquipmentLists();
            }
        }
    }

    private void packToOut()
    {
        int idx = this.fPackList.getSelIndex();
        if (idx >= 0 && idx < this.fPackList.getItems().getCount()) {
            Item item = (Item) this.fPackList.getItems().getItem(idx).Data;
            if (!item.equals(this.fContainer)) {
                this.dropInvItem(item, false);
                this.updateEquipmentLists();
            }
        }
    }

    private void putToBag(Item aItem, EntityList aFromList)
    {
        aItem.setInUse(false);
        aFromList.extract(aItem);
        this.fContainer.getContents().add(aItem);
    }

    private void restoreSelections()
    {
        if (this.fSelPack >= this.fPackList.getItems().getCount()) {
            this.fPackList.setSelIndex(this.fSelPack - 1);
        } else {
            this.fPackList.setSelIndex(this.fSelPack);
        }
        if (this.fSelEquip >= this.fEquipList.getItems().getCount()) {
            this.fEquipList.setSelIndex(this.fSelEquip - 1);
        } else {
            this.fEquipList.setSelIndex(this.fSelEquip);
        }
        if (this.fSelOut >= this.fOutList.getItems().getCount()) {
            this.fOutList.setSelIndex(this.fSelOut - 1);
        } else {
            this.fOutList.setSelIndex(this.fSelOut);
        }
    }

    private void saveSelections()
    {
        this.fSelPack = this.fPackList.getSelIndex();
        this.fSelEquip = this.fEquipList.getSelIndex();
        this.fSelOut = this.fOutList.getSelIndex();
    }

    private boolean takeInvItem(Item aItem, boolean aEquip)
    {
        boolean result = true;
        Player player = GlobalVars.nwrGame.getPlayer();
        if (player.canTake(aItem, this.fMode > IWMODE_GROUND)) {
            if (this.fMode != IWMODE_GROUND) {
                if (player.canBuy(aItem, this.Collocutor)) {
                    player.buy(aItem, this.Collocutor, this.fMode == IWMODE_INSHOP);
                } else {
                    result = false;
                }
            } else {
                GlobalVars.nwrGame.doPlayerAction(CreatureAction.caItemPickup, aItem.UID);
            }
        } else {
            result = false;
        }
        if (result & aEquip) {
            GlobalVars.nwrGame.doPlayerAction(CreatureAction.caItemWear, aItem.UID);
        }
        return result;
    }

    private void hideWin()
    {
        this.hide();
        GlobalVars.nwrWin.repaint(10);
    }
    
    private void useInvItem(Item item)
    {
        if (GlobalVars.nwrWin.getTargetData() == null) {
            if (item.getEquipmentKind() == BodypartType.bp_None) {
                if (item.getEntry().Sign.equals("RedBag")) {
                    if (this.fContainer == null) {
                        this.fContainer = item;
                    } else {
                        this.fContainer = null;
                    }
                } else {
                    hideWin();

                    GlobalVars.nwrGame.doPlayerAction(CreatureAction.caItemUse, item.UID);
                }
            }
        } else {
            if (GlobalVars.nwrWin.getTargetData().Target == EffectTarget.et_Item) {
                hideWin();

                GlobalVars.nwrWin.getTargetData().Ext.setParam(EffectParams.ep_Item, item);
                GlobalVars.nwrWin.useTarget();
            } else {
                GlobalVars.nwrWin.showText(this, Locale.getStr(EffectTarget.et_Item.InvalidRS));
            }
        }
    }

    @Override
    protected void doHideEvent()
    {
        super.doHideEvent();
        GlobalVars.nwrWin.hideHint();
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        try {
            super.doPaintEvent(screen);

            String eq;
            if (this.fContainer == null) {
                eq = Locale.getStr(RS.rs_ItemsUsed);
            } else {
                eq = this.fContainer.getName();
            }
            Player player = GlobalVars.nwrGame.getPlayer();

            int debt;
            if (this.Collocutor == null) {
                debt = 0;
            } else {
                debt = player.getDebt(this.Collocutor.getName());
            }

            screen.setTextColor(BaseScreen.clGold, true);

            Rect pakRt = this.fPackList.getBounds(); // 2
            Rect eqpRt = this.fEquipList.getBounds(); // 0
            Rect outRt = this.fOutList.getBounds(); // 1
            
            int lcx = pakRt.Left + (pakRt.getWidth() - CtlCommon.smFont.getTextWidth(Locale.getStr(RS.rs_Equipment))) / 2;
            int rcx = eqpRt.Left + (eqpRt.getWidth() - CtlCommon.smFont.getTextWidth(eq)) / 2;
            int ocx = outRt.Left + (outRt.getWidth() - CtlCommon.smFont.getTextWidth(Locale.getStr(RS.rs_Ground))) / 2;

            int lcy = pakRt.Top - CtlCommon.smFont.Height;
            int rcy = eqpRt.Top - CtlCommon.smFont.Height;
            int ocy = outRt.Top - CtlCommon.smFont.Height;

            screen.drawText(lcx, lcy, Locale.getStr(RS.rs_Equipment), 0);
            screen.drawText(rcx, rcy, eq, 0);
            screen.drawText(ocx, ocy, Locale.getStr(RS.rs_Ground), 0);
            screen.drawText(outRt.Left, 368, Locale.getStr(RS.rs_Money) + ": " + String.valueOf(player.getMoney()), 0);
            screen.drawText(outRt.Left, 386, Locale.getStr(RS.rs_Debt) + ": " + String.valueOf(debt), 0);
            screen.drawText(outRt.Left, 404, Locale.getStr(RS.rs_Weight) + ": " + String.format("%.2f / %.2f", new Object[]{player.getTotalWeight(), player.getMaxItemsWeight()}), 0);
            screen.drawText(outRt.Left, 422, Locale.getStr(RS.rs_EquipTurns), 0);
        } catch (Exception ex) {
            Logger.write("InventoryWindow.DoPaintTo(): " + ex.getMessage());
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        NWWindow.setupItemsList(this.fPackList);
        NWWindow.setupItemsList(this.fEquipList);
        NWWindow.setupItemsList(this.fOutList);

        this.updateEquipmentLists();

        super.setActiveControl(this.fPackList);
    }

    public InventoryWindow(BaseControl owner)
    {
        super(owner);

        super.setFont(CtlCommon.smFont);
        super.setWidth(600);
        super.setHeight(460);
        super.WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview);
        super.Shifted = true;
        super.OnKeyDown = this::onCommonKeyDown;

        this.fContainer = null;

        this.fPayBtn = new NWButton(this);
        this.fPayBtn.setLeft(super.getWidth() - 90 - 20 - 90 - 20);
        this.fPayBtn.setTop(super.getHeight() - 30 - 20);
        this.fPayBtn.setWidth(90);
        this.fPayBtn.setHeight(30);
        this.fPayBtn.setImageFile("itf/DlgBtn.tga");
        this.fPayBtn.OnClick = this::onBtnPay;
        this.fPayBtn.OnLangChange = GlobalVars.nwrWin::LangChange;
        this.fPayBtn.setLangResID(892);

        NWButton btnClose = new NWButton(this);
        btnClose.setLeft(super.getWidth() - 90 - 20);
        btnClose.setTop(super.getHeight() - 30 - 20);
        btnClose.setWidth(90);
        btnClose.setHeight(30);
        btnClose.setImageFile("itf/DlgBtn.tga");
        btnClose.OnClick = this::onBtnClose;
        btnClose.OnLangChange = GlobalVars.nwrWin::LangChange;
        btnClose.setLangResID(8);

        this.fPackList = new ListBox(this);
        this.fPackList.setBounds(new Rect(309, 28, 589, 367)); // 2
        this.fPackList.OnItemSelect = this::Pack_ItemSelect;
        this.fPackList.setVisible(true);
        this.fPackList.OnDragDrop = this::onPackDragDrop;
        this.fPackList.OnDragOver = this::onPackDragOver;
        this.fPackList.OnDragStart = this::onPackDragStart;
        this.fPackList.OnMouseDown = this::onListMouseDown;
        this.fPackList.OnMouseMove = this::onListMouseMove;
        this.fPackList.OnKeyDown = null;
        this.fPackList.ShowHints = true;
        this.fPackList.Options.include(LBOptions.lboIcons);
        this.fPackList.OnItemDraw = this::onItemDraw;

        this.fEquipList = new ListBox(this);
        this.fEquipList.setBounds(new Rect(10, 28, 290, 187)); // 0
        this.fEquipList.OnItemSelect = this::Equip_ItemSelect;
        this.fEquipList.setVisible(true);
        this.fEquipList.OnDragDrop = this::onEquipDragDrop;
        this.fEquipList.OnDragOver = this::onEquipDragOver;
        this.fEquipList.OnDragStart = this::onEquipDragStart;
        this.fEquipList.OnMouseDown = this::onListMouseDown;
        this.fEquipList.OnMouseMove = this::onListMouseMove;
        this.fEquipList.OnKeyDown = null;
        this.fEquipList.ShowHints = true;
        this.fEquipList.Options.include(LBOptions.lboIcons);
        this.fEquipList.OnItemDraw = this::onItemDraw;

        this.fOutList = new ListBox(this);
        this.fOutList.setBounds(new Rect(10, 208, 290, 367)); // 1
        this.fOutList.OnItemSelect = this::Out_ItemSelect;
        this.fOutList.setVisible(true);
        this.fOutList.OnDragDrop = this::onOutDragDrop;
        this.fOutList.OnDragOver = this::onOutDragOver;
        this.fOutList.OnDragStart = this::onOutDragStart;
        this.fOutList.OnMouseDown = this::onListMouseDown;
        this.fOutList.OnMouseMove = this::onListMouseMove;
        this.fOutList.OnKeyDown = null;
        this.fOutList.ShowHints = true;
        this.fOutList.Options.include(LBOptions.lboIcons);
        this.fOutList.OnItemDraw = this::onItemDraw;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fOutList.dispose();
            this.fPackList.dispose();
            this.fEquipList.dispose();
        }
        super.dispose(disposing);
    }
}
