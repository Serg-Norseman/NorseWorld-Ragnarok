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

import jzrlib.core.GameEntity;
import jzrlib.core.EntityList;
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
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.ListBox;
import nwr.item.Item;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class AlchemyWindow extends NWWindow
{
    private final ListBox fIngredientsList;
    private final ListBox fPackList;
    private final ListBox fResList;

    private void onBtnClose(Object sender)
    {
        this.hide();
    }

    private void onBtnAlchemy(Object sender)
    {
        EntityList ingredients = new EntityList(null, false);
        try {
            int num = this.fIngredientsList.getItems().getCount();
            for (int i = 0; i < num; i++) {
                GameEntity ing = (GameEntity) this.fIngredientsList.getItems().getItem(i).Data;
                ingredients.add(ing);
            }

            Item res = GlobalVars.nwrGame.getPlayer().getCraft().alchemy(ingredients);

            this.updateLists();

            if (res != null) {
                this.fResList.getItems().add(res.getName(), res);
            }
        } finally {
            ingredients.dispose();
        }
    }

    private void updateLists()
    {
        this.fPackList.getItems().clear();
        this.fIngredientsList.getItems().clear();
        this.fResList.getItems().clear();

        EntityList items = GlobalVars.nwrGame.getPlayer().getItems();
        int num = items.getCount();
        for (int i = 0; i < num; i++) {
            Item item = (Item) items.getItem(i);
            if (item.isIngredient()) {
                NWWindow.addListItem(this.fPackList, item.getName(), item, false);
            }
        }
    }

    private void onIngrDragDrop(Object sender, Object Source, int X, int Y)
    {
        int idx = this.fPackList.getSelIndex();
        if (Source.equals(this.fPackList) && idx >= 0 && idx < this.fPackList.getItems().getCount()) {
            Item item = (Item) this.fPackList.getItems().getItem(idx).Data;
            this.fIngredientsList.getItems().add(item.getName(), item);
        }
    }

    private void onIngrDragOver(Object sender, Object Source, int X, int Y, RefObject<Boolean> Accept)
    {
        int idx = this.fPackList.getSelIndex();
        Accept.argValue = (Source.equals(this.fPackList) && idx >= 0 && idx < this.fPackList.getItems().getCount());
    }

    private void onIngrDragStart(Object sender, RefObject<BaseDragObject> DragObject)
    {
        int idx = this.fIngredientsList.getSelIndex();
        if (idx >= 0 && idx < this.fIngredientsList.getItems().getCount()) {
            Item item = (Item) this.fIngredientsList.getItems().getItem(idx).Data;
            DragObject.argValue = new InventoryObject();
            ((InventoryObject) DragObject.argValue).InvItem = item;
        }
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
        NWWindow.onListMouseMove(sender, eventArgs.X, eventArgs.Y, false);
    }

    private void onPackDragDrop(Object sender, Object Source, int X, int Y)
    {
        int idx = this.fIngredientsList.getSelIndex();
        if (Source.equals(this.fIngredientsList) && idx >= 0 && idx < this.fIngredientsList.getItems().getCount()) {
            this.fIngredientsList.getItems().delete(idx);
        }
    }

    private void onPackDragOver(Object sender, Object Source, int X, int Y, RefObject<Boolean> Accept)
    {
        Accept.argValue = Source.equals(this.fIngredientsList);
    }

    private void onPackDragStart(Object sender, RefObject<BaseDragObject> DragObject)
    {
        int idx = this.fPackList.getSelIndex();
        if (idx >= 0 && idx < this.fPackList.getItems().getCount()) {
            Item item = (Item) this.fPackList.getItems().getItem(idx).Data;
            if (this.fIngredientsList.getItems().indexOf(item.getName()) < 0) {
                DragObject.argValue = new InventoryObject();
                ((InventoryObject) DragObject.argValue).InvItem = item;
            }
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);

        Rect pakRt = this.fPackList.getBounds(); // 8
        Rect ingRt = this.fIngredientsList.getBounds(); // 6
        Rect resRt = this.fResList.getBounds(); // 7

        CtlCommon.smFont.setColor(BaseScreen.clGold);
        int lcx = pakRt.Left + (pakRt.getWidth() - CtlCommon.smFont.getTextWidth(Locale.getStr(RS.rs_Ingredients))) / 2;
        int rcx = ingRt.Left + (ingRt.getWidth() - CtlCommon.smFont.getTextWidth(Locale.getStr(RS.rs_ItemsChoosed))) / 2;
        int ocx = resRt.Left + (resRt.getWidth() - CtlCommon.smFont.getTextWidth(Locale.getStr(RS.rs_Result))) / 2;

        screen.drawText(lcx, 9, Locale.getStr(RS.rs_Ingredients), 0);
        screen.drawText(rcx, 9, Locale.getStr(RS.rs_ItemsChoosed), 0);
        screen.drawText(ocx, 206, Locale.getStr(RS.rs_Result), 0);
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        this.fPackList.ImagesList = GlobalVars.nwrWin.Symbols;
        this.fIngredientsList.ImagesList = GlobalVars.nwrWin.Symbols;
        this.fResList.ImagesList = GlobalVars.nwrWin.Symbols;
        this.updateLists();
    }

    public AlchemyWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(600);
        super.setHeight(460);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);
        super.Shifted = true;

        this.fPackList = new ListBox(this);
        this.fPackList.setBounds(new Rect(309, 28, 589, 398));
        this.fPackList.OnItemSelect = null;
        this.fPackList.setVisible(true);
        this.fPackList.OnDragDrop = this::onPackDragDrop;
        this.fPackList.OnDragOver = this::onPackDragOver;
        this.fPackList.OnDragStart = this::onPackDragStart;
        this.fPackList.OnMouseDown = this::onListMouseDown;
        this.fPackList.ShowHints = true;
        this.fPackList.OnMouseMove = this::onListMouseMove;

        this.fIngredientsList = new ListBox(this);
        this.fIngredientsList.setBounds(new Rect(10, 28, 290, 207));
        this.fIngredientsList.OnItemSelect = null;
        this.fIngredientsList.setVisible(true);
        this.fIngredientsList.OnDragDrop = this::onIngrDragDrop;
        this.fIngredientsList.OnDragOver = this::onIngrDragOver;
        this.fIngredientsList.OnDragStart = this::onIngrDragStart;
        this.fIngredientsList.OnMouseDown = this::onListMouseDown;
        this.fIngredientsList.ShowHints = true;
        this.fIngredientsList.OnMouseMove = this::onListMouseMove;

        this.fResList = new ListBox(this);
        this.fResList.setBounds(new Rect(10, 225, 290, 398)); // 7
        this.fResList.setVisible(true);
        this.fResList.ShowHints = true;
        this.fResList.OnMouseMove = this::onListMouseMove;

        NWButton btnAlchemy = new NWButton(this);
        btnAlchemy.setWidth(90);
        btnAlchemy.setHeight(30);
        btnAlchemy.setLeft(this.fPackList.getLeft() + 20);
        btnAlchemy.setTop(super.getHeight() - 30 - 20);
        btnAlchemy.OnClick = this::onBtnAlchemy;
        btnAlchemy.OnLangChange = GlobalVars.nwrWin::LangChange;
        btnAlchemy.setLangResID(131);
        btnAlchemy.setImageFile("itf/DlgBtn.tga");

        NWButton btnClose = new NWButton(this);
        btnClose.setWidth(90);
        btnClose.setHeight(30);
        btnClose.setLeft(super.getWidth() - 90 - 20);
        btnClose.setTop(super.getHeight() - 30 - 20);
        btnClose.OnClick = this::onBtnClose;
        btnClose.OnLangChange = GlobalVars.nwrWin::LangChange;
        btnClose.setLangResID(8);
        btnClose.setImageFile("itf/DlgBtn.tga");
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fPackList.dispose();
            this.fIngredientsList.dispose();
            this.fResList.dispose();
        }
        super.dispose(disposing);
    }
}
