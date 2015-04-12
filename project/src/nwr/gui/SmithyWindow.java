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
import nwr.core.types.MaterialKind;
import nwr.database.ItemEntry;
import nwr.engine.BaseControl;
import nwr.engine.BaseDragObject;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;
import nwr.engine.MouseEventArgs;
import nwr.engine.MouseMoveEventArgs;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.LBOptions;
import nwr.gui.controls.ListBox;
import nwr.item.Item;
import nwr.main.GlobalVars;
import nwr.player.Craft;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class SmithyWindow extends NWWindow
{
    private final EntityList fIngredients;
    private final ListBox fIngredientsList;
    private final ListBox fPackList;
    private final ListBox fResList;

    private void onBtnClose(Object sender)
    {
        this.hide();
    }

    private void onBtnSmithery(Object sender)
    {
        int idx = this.fResList.getSelIndex();
        if (idx < 0) {
            return;
        }

        int id;
        if (idx == 0) {
            id = GlobalVars.iid_Ingot;
        } else {
            id = ((ItemEntry) this.fResList.getItems().getItem(idx).Data).GUID;
        }

        int res = GlobalVars.nwrGame.getPlayer().getCraft().forgeItem(this.fIngredients, id);

        if (res != Craft.RC_Ok) {
            if (res == Craft.RC_DifferentMetals) {
                // dummy
            }
        } else {
            this.fResList.getItems().add(Locale.getStr(RS.rs_MeltInIngot), null);
            this.updateView();
        }
    }

    private void updateView()
    {
        this.fPackList.getItems().clear();
        this.fIngredientsList.getItems().clear();
        this.fResList.getItems().clear();

        EntityList items = GlobalVars.nwrGame.getPlayer().getItems();
        MaterialKind mat = MaterialKind.mk_None;
        int num = items.getCount();
        for (int i = 0; i < num; i++) {
            Item item = (Item) items.getItem(i);
            mat = item.getEntry().Material;
            if (mat != MaterialKind.mk_None && this.fIngredients.findByGUID(item.UID) == null) {
                NWWindow.addListItem(this.fPackList, this.getItemFullName(item), item, true);
            }
        }

        int num2 = this.fIngredients.getCount();
        for (int i = 0; i < num2; i++) {
            Item item = (Item) this.fIngredients.getItem(i);
            NWWindow.addListItem(this.fIngredientsList, this.getItemFullName(item), item, true);
        }

        float sum = 0F;
        RefObject<Float> tempRef_sum = new RefObject<>(sum);
        RefObject<MaterialKind> refMat = new RefObject<>(mat);
        int res = GlobalVars.nwrGame.getPlayer().getCraft().checkForgeIngredients(this.fIngredients, tempRef_sum, refMat);
        sum = tempRef_sum.argValue;
        mat = refMat.argValue;
        if (res != Craft.RC_Ok) {
            if (res == Craft.RC_DifferentMetals) {
                this.fResList.getItems().add(Locale.getStr(RS.rs_DifferentMetals), null);
            }
        } else {
            ItemEntry iEntry = (ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Ingot);

            if (iEntry.Material == mat) {
                LBItem listItem = this.fResList.getItems().add(Locale.getStr(RS.rs_MeltInIngot), null);
                listItem.ImageIndex = iEntry.ImageIndex;
                listItem.Color = iEntry.ItmKind.Color;
            }

            int num3 = GlobalVars.dbWeapon.getCount();
            for (int i = 0; i < num3; i++) {
                iEntry = ((ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.dbWeapon.get(i)));
                if (iEntry.Material == mat && iEntry.Weight <= sum && !iEntry.isUnique() && !iEntry.isMeta()) {
                    LBItem listItem = this.fResList.getItems().add(this.getItemFullName(iEntry), iEntry);
                    listItem.ImageIndex = iEntry.ImageIndex;
                    listItem.Color = iEntry.ItmKind.Color;
                }
            }
        }
    }

    private void onIngrDragDrop(Object Sender, Object Source, int X, int Y)
    {
        int idx = this.fPackList.getSelIndex();
        if (Source.equals(this.fPackList) && idx >= 0 && idx < this.fPackList.getItems().getCount()) {
            Item item = (Item) this.fPackList.getItems().getItem(idx).Data;
            this.fIngredients.add(item);
            this.updateView();
        }
    }

    private void onIngrDragOver(Object sender, Object source, int x, int y, RefObject<Boolean> Accept)
    {
        int idx = this.fPackList.getSelIndex();
        Accept.argValue = (source.equals(this.fPackList) && idx >= 0 && idx < this.fPackList.getItems().getCount());
        if (Accept.argValue) {
            Item item = (Item) this.fPackList.getItems().getItem(idx).Data;
            Accept.argValue = (this.fIngredients.findByGUID(item.UID) == null);
        }
    }

    private void onIngrDragStart(Object sender, RefObject<BaseDragObject> dragObject)
    {
        int idx = this.fIngredientsList.getSelIndex();
        if (idx >= 0 && idx < this.fIngredientsList.getItems().getCount()) {
            Item item = (Item) ((this.fIngredientsList.getItems().getItem(idx).Data instanceof Item) ? this.fIngredientsList.getItems().getItem(idx).Data : null);
            dragObject.argValue = new InventoryObject();
            ((InventoryObject) dragObject.argValue).InvItem = item;
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
            this.fIngredients.remove((GameEntity) ((this.fIngredientsList.getItems().getItem(idx).Data instanceof GameEntity) ? this.fIngredientsList.getItems().getItem(idx).Data : null));
            this.updateView();
        }
    }

    private void onPackDragOver(Object sender, Object source, int x, int y, RefObject<Boolean> Accept)
    {
        Accept.argValue = source.equals(this.fIngredientsList);
    }

    private void onPackDragStart(Object sender, RefObject<BaseDragObject> DragObject)
    {
        int idx = this.fPackList.getSelIndex();
        if (idx >= 0 && idx < this.fPackList.getItems().getCount()) {
            Item item = (Item) this.fPackList.getItems().getItem(idx).Data;
            if (this.fIngredients.findByGUID(item.UID) == null) {
                DragObject.argValue = new InventoryObject();
                ((InventoryObject) DragObject.argValue).InvItem = item;
            } else {
                DragObject.argValue = null;
            }
        }
    }

    private String getItemFullName(Object itemObj)
    {
        float iWeight = 0.0f;
        MaterialKind iMaterial = MaterialKind.mk_None;
        String Result = "";

        if (itemObj instanceof Item) {
            Item item = (Item) itemObj;
            iWeight = item.getWeight();
            iMaterial = item.getMaterial();
            Result = item.getName();
        } else {
            if (itemObj instanceof ItemEntry) {
                ItemEntry itemEntry = (ItemEntry) itemObj;
                iWeight = itemEntry.Weight;
                iMaterial = itemEntry.Material;
                Result = itemEntry.getName();
            }
        }

        Result = Result + "\r\n" + Locale.getStr(RS.rs_Weight) + ": " + String.valueOf((double) iWeight) + "; " + Locale.getStr(iMaterial.NameRS);

        return Result;
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);
        CtlCommon.smFont.setColor(BaseScreen.clGold);
        
        Rect pakRt = this.fPackList.getBounds(); // 8
        Rect ingRt = this.fIngredientsList.getBounds(); // 6
        Rect resRt = this.fResList.getBounds(); // 7

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
        int h = (int) GlobalVars.nwrWin.Resources.getImage(0).Height;
        int w = (int) GlobalVars.nwrWin.Resources.getImage(0).Width;

        this.fPackList.ImagesList = GlobalVars.nwrWin.Resources;
        this.fPackList.setIconHeight(h);
        this.fPackList.setIconWidth(w);

        this.fIngredientsList.ImagesList = GlobalVars.nwrWin.Resources;
        this.fIngredientsList.setIconHeight(h);
        this.fIngredientsList.setIconWidth(w);

        this.fResList.ImagesList = GlobalVars.nwrWin.Resources;
        this.fResList.setIconHeight(h);
        this.fResList.setIconWidth(w);
        this.updateView();
    }

    public SmithyWindow(BaseControl owner)
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
        this.fPackList.Options.include(LBOptions.lboIcons, LBOptions.lboTextTop, LBOptions.lboDoubleItem);
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
        this.fIngredientsList.Options.include(LBOptions.lboIcons, LBOptions.lboTextTop, LBOptions.lboDoubleItem);
        this.fIngredientsList.ShowHints = true;
        this.fIngredientsList.OnMouseMove = this::onListMouseMove;

        this.fResList = new ListBox(this);
        this.fResList.setBounds(new Rect(10, 225, 290, 398));
        this.fResList.setVisible(true);
        this.fResList.Options.include(LBOptions.lboIcons, LBOptions.lboTextTop, LBOptions.lboDoubleItem);
        this.fResList.ShowHints = true;
        this.fResList.OnMouseMove = this::onListMouseMove;

        NWButton btnForge = new NWButton(this);
        btnForge.setWidth(90);
        btnForge.setHeight(30);
        btnForge.setLeft(this.fPackList.getLeft() + 20);
        btnForge.setTop(super.getHeight() - 30 - 20);
        btnForge.OnClick = this::onBtnSmithery;
        btnForge.OnLangChange = GlobalVars.nwrWin::LangChange;
        btnForge.setLangResID(132);
        btnForge.setImageFile("itf/DlgBtn.tga");

        NWButton btnClose = new NWButton(this);
        btnClose.setWidth(90);
        btnClose.setHeight(30);
        btnClose.setLeft(super.getWidth() - 90 - 20);
        btnClose.setTop(super.getHeight() - 30 - 20);
        btnClose.OnClick = this::onBtnClose;
        btnClose.OnLangChange = GlobalVars.nwrWin::LangChange;
        btnClose.setLangResID(8);
        btnClose.setImageFile("itf/DlgBtn.tga");

        this.fIngredients = new EntityList(null, false);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fIngredients.dispose();
            this.fPackList.dispose();
            this.fIngredientsList.dispose();
            this.fResList.dispose();
        }
        super.dispose(disposing);
    }
}
