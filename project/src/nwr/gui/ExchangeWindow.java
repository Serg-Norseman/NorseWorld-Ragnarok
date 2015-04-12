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

import jzrlib.core.Rect;
import jzrlib.utils.RefObject;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.engine.BaseControl;
import nwr.engine.BaseDragObject;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;
import nwr.engine.MouseEventArgs;
import nwr.engine.MouseMoveEventArgs;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.LBOptions;
import nwr.gui.controls.ListBox;
import nwr.item.Item;
import nwr.item.ItemsList;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ExchangeWindow extends DialogWindow
{
    private final ListBox FColList;
    private final ListBox FPackList;

    public ExchangeWindow(BaseControl owner)
    {
        super(owner);

        this.FPackList = new ListBox(this);
        this.FPackList.setBounds(new Rect(309, 28, 589, 398));
        this.FPackList.setVisible(true);
        this.FPackList.OnDragDrop = this::onPackDragDrop;
        this.FPackList.OnDragOver = this::onPackDragOver;
        this.FPackList.OnDragStart = this::onPackDragStart;
        this.FPackList.OnMouseDown = this::onListMouseDown;
        this.FPackList.OnMouseMove = this::onListMouseMove;
        this.FPackList.OnKeyDown = null;
        this.FPackList.ShowHints = true;
        this.FPackList.Options.include(LBOptions.lboIcons);

        this.FColList = new ListBox(this);
        this.FColList.setBounds(new Rect(10, 28, 290, 398));
        this.FColList.setVisible(true);
        this.FColList.OnDragDrop = this::onColDragDrop;
        this.FColList.OnDragOver = this::onColDragOver;
        this.FColList.OnDragStart = this::onColDragStart;
        this.FColList.OnMouseDown = this::onListMouseDown;
        this.FColList.OnMouseMove = this::onListMouseMove;
        this.FColList.OnKeyDown = null;
        this.FColList.ShowHints = true;
        this.FColList.Options.include(LBOptions.lboIcons);
    }

    private void updateView()
    {
        boolean onlyIcons = GlobalVars.nwrWin.getInventoryOnlyIcons();
        this.FPackList.getItems().beginUpdate();
        this.FColList.getItems().beginUpdate();
        this.FPackList.getItems().clear();
        this.FColList.getItems().clear();

        ItemsList pack = GlobalVars.nwrGame.getPlayer().getItems();

        int num = pack.getCount();
        for (int i = 0; i < num; i++) {
            Item item = pack.getItem(i);
            String nm = item.getName();
            if (item.getInUse()) {
                nm += " (*)";
            }
            super.addListItem(this.FPackList, nm, item, onlyIcons);
        }

        pack = this.fCollocutor.getItems();

        int num2 = pack.getCount();
        for (int i = 0; i < num2; i++) {
            Item item = pack.getItem(i);
            String nm = item.getName();
            if (item.getInUse()) {
                nm += " (*)";
            }
            super.addListItem(this.FColList, nm, item, onlyIcons);
        }

        this.FColList.getItems().endUpdate();
        this.FPackList.getItems().endUpdate();
    }

    private void onListMouseDown(Object sender, MouseEventArgs eventArgs)
    {
        if (eventArgs.Button == MouseButton.mbLeft) {
            ListBox aList = (ListBox) sender;
            if (aList.getItems().getCount() > 0 && aList.getSelIndex() >= 0) {
                aList.BeginDrag(false);
            }
        }
    }

    private void onListMouseMove(Object sender, MouseMoveEventArgs eventArgs)
    {
        super.onListMouseMove(sender, eventArgs.X, eventArgs.Y, false);
    }

    private void onPackDragDrop(Object sender, Object source, int x, int y)
    {
        int idx = this.FColList.getSelIndex();
        if (idx >= 0 && idx < this.FColList.getItems().getCount()) {
            Item item = (Item) this.FColList.getItems().getItem(idx).Data;
            this.fCollocutor.dropItem(item);
            GlobalVars.nwrGame.getPlayer().pickupItem(item);
            this.updateView();
        }
    }

    private void onPackDragOver(Object sender, Object source, int x, int y, RefObject<Boolean> Accept)
    {
        int idx = this.FColList.getSelIndex();
        Accept.argValue = (source.equals(this.FColList) && idx >= 0 && idx < this.FColList.getItems().getCount());
    }

    private void onPackDragStart(Object sender, RefObject<BaseDragObject> dragObject)
    {
        int idx = this.FPackList.getSelIndex();
        if (idx >= 0 && idx < this.FPackList.getItems().getCount()) {
            Item item = (Item) this.FPackList.getItems().getItem(idx).Data;
            dragObject.argValue = new InventoryObject();
            ((InventoryObject) dragObject.argValue).InvItem = item;
        }
    }

    private void onColDragDrop(Object sender, Object source, int x, int y)
    {
        int idx = this.FPackList.getSelIndex();
        if (idx >= 0 && idx < this.FPackList.getItems().getCount()) {
            Item item = (Item) this.FPackList.getItems().getItem(idx).Data;
            GlobalVars.nwrGame.getPlayer().dropItem(item);
            this.fCollocutor.pickupItem(item);
            this.updateView();
        }
    }

    private void onColDragOver(Object sender, Object source, int x, int y, RefObject<Boolean> Accept)
    {
        int idx = this.FPackList.getSelIndex();
        Accept.argValue = (source.equals(this.FPackList) && idx >= 0 && idx < this.FPackList.getItems().getCount());
    }

    private void onColDragStart(Object sender, RefObject<BaseDragObject> dragObject)
    {
        int idx = this.FColList.getSelIndex();
        if (idx >= 0 && idx < this.FColList.getItems().getCount()) {
            Item item = (Item) this.FColList.getItems().getItem(idx).Data;
            dragObject.argValue = new InventoryObject();
            ((InventoryObject) dragObject.argValue).InvItem = item;
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);

        String nm_self = GlobalVars.nwrGame.getPlayer().getName();
        String nm_col = this.fCollocutor.getName();

        screen.setTextColor(BaseScreen.clGold, true);

        Rect pakRt = this.FPackList.getBounds(); // 2
        Rect colRt = this.FColList.getBounds(); // 0
        
        int lcx = pakRt.Left + (pakRt.getWidth() - CtlCommon.smFont.getTextWidth(nm_self)) / 2;
        int rcx = colRt.Left + (colRt.getWidth() - CtlCommon.smFont.getTextWidth(nm_col)) / 2;

        int lcy = pakRt.Top - CtlCommon.smFont.Height;
        int rcy = colRt.Top - CtlCommon.smFont.Height;

        screen.drawText(lcx, lcy, nm_self, 0);
        screen.drawText(rcx, rcy, nm_col, 0);

        screen.drawText(pakRt.Left, pakRt.Bottom + 3, Locale.getStr(RS.rs_Weight) + ": " + String.format("%.2f / %.2f", new Object[]{GlobalVars.nwrGame.getPlayer().getTotalWeight(), GlobalVars.nwrGame.getPlayer().getMaxItemsWeight()}), 0);
        screen.drawText(colRt.Left, colRt.Bottom + 3, Locale.getStr(RS.rs_Weight) + ": " + String.format("%.2f / %.2f", new Object[]{this.fCollocutor.getTotalWeight(), this.fCollocutor.getMaxItemsWeight()}), 0);
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        NWWindow.setupItemsList(this.FPackList);
        NWWindow.setupItemsList(this.FColList);
        this.updateView();
    }
}
