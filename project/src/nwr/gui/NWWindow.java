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

import jzrlib.utils.AuxUtils;
import jzrlib.external.BinaryInputStream;
import jzrlib.utils.Logger;
import jzrlib.core.Rect;
import jzrlib.utils.StreamUtils;
import nwr.core.StaticData;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.BaseWindow;
import nwr.engine.KeyEventArgs;
import nwr.engine.Keys;
import nwr.engine.ResourceManager;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.ListBox;
import nwr.item.Item;
import nwr.main.GlobalVars;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class NWWindow extends BaseWindow
{
    public boolean BackDraw;
    public boolean Shifted;

    public NWWindow(BaseControl owner)
    {
        super(owner);
        this.BackDraw = true;
        this.Shifted = false;
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        if (CtlCommon.fWinBack != null && CtlCommon.fWinDecor != null) {
            Rect crt = super.getClientRect();
            int L = crt.Left;
            int T = crt.Top;
            int R = crt.Right;
            int B = crt.Bottom;

            if (this.BackDraw) {
                screen.drawFilled(crt, BaseScreen.FILL_TILE, 0, 0, (int) CtlCommon.fWinBack.Width, (int) CtlCommon.fWinBack.Height, 0, 0, CtlCommon.fWinBack);
            } else {
                screen.fillRect(crt, BaseScreen.clBlack);
            }

            screen.drawFilled(crt, BaseScreen.FILL_HORZ, 16, 0, 61, 8, L, T, CtlCommon.fWinDecor);
            screen.drawFilled(crt, BaseScreen.FILL_HORZ, 16, 0, 61, 8, L, B - 7, CtlCommon.fWinDecor);
            screen.drawFilled(crt, BaseScreen.FILL_VERT, 0, 16, 8, 61, L, T, CtlCommon.fWinDecor);
            screen.drawFilled(crt, BaseScreen.FILL_VERT, 0, 16, 8, 61, R - 7, T, CtlCommon.fWinDecor);

            screen.drawImage(L, T, 0, 0, 8, 8, CtlCommon.fWinDecor, 255);
            screen.drawImage(L, B - 7, 0, 0, 8, 8, CtlCommon.fWinDecor, 255);
            screen.drawImage(R - 7, T, 0, 0, 8, 8, CtlCommon.fWinDecor, 255);
            screen.drawImage(R - 7, B - 7, 0, 0, 8, 8, CtlCommon.fWinDecor, 255);
            screen.drawImage(L + 8, T + 8, 8, 8, 13, 10, CtlCommon.fWinDecor, 255);
            screen.drawImage(L + 8, B - 7 - 10, 8, 75, 13, 10, CtlCommon.fWinDecor, 255);
            screen.drawImage(R - 7 - 13, T + 8, 72, 8, 13, 10, CtlCommon.fWinDecor, 255);
            screen.drawImage(R - 7 - 13, B - 7 - 10, 72, 75, 13, 10, CtlCommon.fWinDecor, 255);
        }
    }

    @Override
    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        super.doKeyDownEvent(eventArgs);

        if (eventArgs.Key == Keys.GK_ESCAPE) {
            this.hide();
            this.DoClose();
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        if (this.Shifted /*GVHide*/) {
            GlobalVars.nwrWin.DialogVisibleRefs++;
        }

        if (this.Shifted) {
            int w;
            if (GlobalVars.nwrWin.getHideCtlPanel() && GlobalVars.nwrWin.getHideInfoPanel()) {
                w = GlobalVars.nwrWin.getWidth();
            } else {
                w = 640;
            }
            super.setLeft((w - super.getWidth()) / 2);
            super.setTop((StaticData.TV_Rect.Top - super.getHeight()) / 2);
        }
    }

    @Override
    protected void doHideEvent()
    {
        super.doHideEvent();

        if (this.Shifted /*GVHide*/) {
            GlobalVars.nwrWin.DialogVisibleRefs--;
        }
    }

    public static final void addListItem(ListBox aList, String aName, Item aItem, boolean aIcons)
    {
        LBItem listItem = aList.getItems().add(aName, aItem);
        if (aItem != null) {
            listItem.Color = aItem.getKind().Color;

            if (!aIcons) {
                listItem.ImageIndex = (int) ((byte) aItem.getSymbol().getValue());
            } else {
                listItem.ImageIndex = aItem.getImageIndex();
            }
        }
    }

    public static final void setupItemsList(ListBox aListBox)
    {
        int ih;
        int iw;
        if (!GlobalVars.nwrWin.getInventoryOnlyIcons()) {
            ih = 10;
            iw = 8;
            aListBox.ImagesList = GlobalVars.nwrWin.Symbols;
        } else {
            iw = 32;
            ih = 30;
            aListBox.ImagesList = GlobalVars.nwrWin.Resources;
        }
        aListBox.setIconHeight(ih);
        aListBox.setIconWidth(iw);
        if (!GlobalVars.nwrWin.getInventoryOnlyIcons()) {
            aListBox.setMode(ListBox.MODE_LIST);
            aListBox.setColumns(1);
        } else {
            aListBox.setMode(ListBox.MODE_ICONS);
        }
    }

    public static final void onListMouseMove(Object Sender, int X, int Y, boolean trade)
    {
        try {
            ListBox list = (ListBox) Sender;
            int idx = list.getItemByMouse(X, Y);

            if (idx >= 0 && idx < list.getItems().getCount()) {
                Object data = list.getItems().getItem(idx).Data;
                if (data != null && data instanceof Item) {
                    Item item = (Item) data;
                    list.Hint = item.getHint(trade);
                }
            }
        } catch (Exception ex) {
            Logger.write("NWWindow.xListMouseMove(): " + ex.getMessage());
        }
    }

    public static final String getTextFileByLang(String fileName)
    {
        String result;
        String ext = GlobalVars.nwrWin.getLangExt();
        fileName = fileName + "." + ext;

        try {
            InputStream is = ResourceManager.loadStream(fileName);
            try (BinaryInputStream dis = new BinaryInputStream(is, AuxUtils.binEndian)) {
                result = StreamUtils.readText(dis);
            }
        } catch (IOException e) {
            result = "";
        }

        return result;
    }
}
