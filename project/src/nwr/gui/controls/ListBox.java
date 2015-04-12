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
package nwr.gui.controls;

import jzrlib.utils.RefObject;
import jzrlib.core.StringList;
import jzrlib.core.Rect;
import nwr.engine.BaseControl;
import nwr.engine.BaseImage;
import nwr.engine.BaseScreen;
import nwr.engine.ControlStyles;
import nwr.engine.ImageList;
import nwr.engine.KeyEventArgs;
import nwr.engine.Keys;
import nwr.engine.MouseButton;
import nwr.engine.MouseEventArgs;
import nwr.engine.MouseWheelEventArgs;
import nwr.engine.ResourceManager;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class ListBox extends BaseControl
{
    public static final byte MODE_LIST = 0;
    public static final byte MODE_REPORT = 1;
    public static final byte MODE_ICONS = 2;

    private final LBColumnTitles fColumnTitles;
    private final LBItemList fItems;

    private int fColumns;
    private int fColumnSize;
    private int fColumnWidth;
    private int fItemHeight;
    private byte fMode;
    private int fSelIndex;
    private int fTopIndex;
    private int fVisibleItems;
    protected int fIconHeight;
    protected int fIconWidth;

    public int CheckHeight;
    public int CheckWidth;
    public int LevelOffset;
    public LBOptions Options;
    public ScrollBar ScrollBar;
    public int SelColor;
    public int SelBorderColor;
    public BaseImage CheckImage;
    public ImageList ImagesList;
    public BaseImage TVBtn;

    public IItemSelectEvent OnItemSelect;
    public IItemDrawEvent OnItemDraw;

    public final int getColumns()
    {
        return this.fColumns;
    }

    public final LBColumnTitles getColumnTitles()
    {
        return this.fColumnTitles;
    }

    public final LBItemList getItems()
    {
        return this.fItems;
    }

    public final byte getMode()
    {
        return this.fMode;
    }

    public final int getSelIndex()
    {
        if (this.fSelIndex >= 0 && this.fSelIndex < this.fItems.getCount()) {
            return this.fSelIndex;
        } else {
            return -1;
        }
    }

    public final int getTopIndex()
    {
        return this.fTopIndex;
    }

    public final int getIconHeight()
    {
        return this.fIconHeight;
    }

    public final int getIconWidth()
    {
        return this.fIconWidth;
    }

    private void reindexList(LBItemList aList, RefObject<Integer> aIndex)
    {
        int num = aList.getCount();
        for (int i = 0; i < num; i++) {
            aIndex.argValue++;
            LBItem item = aList.getItem(i);
            item.AbsoluteIndex = aIndex.argValue;
            item.VisibleIndex = aIndex.argValue;
            this.fColumnSize++;
        }
    }

    public final boolean getChecked(int Index)
    {
        boolean result = false;
        if (Index >= 0 && Index < this.fItems.getCount()) {
            result = this.fItems.getItem(Index).Checked;
        }
        return result;
    }

    public final void Reset(boolean unset)
    {
        Rect r = super.getIntRect();
        int ih = this.getItemHeight();
        if (ih == 0) {
            this.fVisibleItems = 1;
        } else {
            this.fVisibleItems = (r.Bottom - r.Top + 1) / ih;
        }
        this.fColumnWidth = r.Right - r.Left + 1;

        if (this.ScrollBar != null) {
            int idx = -1;
            RefObject<Integer> refIdx = new RefObject<>(idx);
            this.reindexList(this.fItems, refIdx);
            //idx = refIdx.argValue;

            this.fColumnWidth /= this.fColumns;
            this.fColumnSize = this.fVisibleItems;
            while (this.fColumnSize * this.fColumns < this.fItems.getCount()) {
                this.fColumnSize++;
            }
            if (this.fSelIndex < 0 || this.fSelIndex >= this.fItems.getCount()) {
                this.fSelIndex = 0;
            }

            if (this.fItems.getCount() == 0) {
                this.ScrollBar.setMax(0);
            } else {
                int idx1 = this.fColumnSize - this.fVisibleItems;
                if (idx1 < 0) {
                    this.ScrollBar.setMax(0);
                } else {
                    this.ScrollBar.setMax(idx1);
                }
            }

            if (unset) {
                this.ScrollBar.setPos(0);
            }
        }
    }

    public final void setChecked(int Index, boolean Value)
    {
        if (Index >= 0 && Index < this.fItems.getCount()) {
            this.fItems.getItem(Index).Checked = Value;
        }
    }

    public final void setColumns(int Value)
    {
        if (this.fColumns != Value) {
            this.fColumns = Value;
            this.Reset(false);
        }
    }

    public final void setItemHeight(int value)
    {
        if (this.fItemHeight != value) {
            this.fItemHeight = value;
            this.Reset(false);
        }
    }

    public final void setMode(byte value)
    {
        if (this.fMode != value) {
            this.fMode = value;

            if (this.fMode == ListBox.MODE_ICONS) {
                if (this.fIconWidth == 0) {
                    this.setColumns(1);
                } else {
                    this.setColumns((super.getWidth() - this.ScrollBar.getWidth()) / this.fIconWidth);
                }
            }
            this.Reset(false);
        }
    }

    public final void setSelIndex(int value)
    {
        int lb;
        int hb;
        int idx;

        if (value < 0 || value >= this.fItems.getCount()) {
            return;
        }
        this.fSelIndex = value;
        int col;
        if (this.fMode == ListBox.MODE_REPORT) {
            col = 0;
        } else {
            col = this.fSelIndex / this.fColumnSize;
        }
        lb = this.fTopIndex + col * this.fColumnSize;
        hb = lb + (this.fVisibleItems - 1);
        idx = this.fSelIndex;

        if (idx > hb) {
            this.setTopIndex(this.fTopIndex + (idx - hb));
        } else {
            if (idx < lb) {
                this.setTopIndex(this.fTopIndex + (idx - lb));
            }
        }
    }

    public final void setTopIndex(int value)
    {
        if (this.fTopIndex != value && value >= 0) {
            this.fTopIndex = value;
            if (this.ScrollBar != null) {
                this.ScrollBar.setPos(value);
            }
        }
    }

    public final void setIconHeight(int value)
    {
        if (this.fIconHeight != value) {
            this.fIconHeight = value;
            this.Reset(false);
        }
    }

    public final void setIconWidth(int value)
    {
        if (this.fIconWidth != value) {
            this.fIconWidth = value;
            this.Reset(false);
        }
    }

    @Override
    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        super.doKeyDownEvent(eventArgs);

        if (eventArgs.Key == Keys.GK_UNK) {
            return;
        }

        int col;
        if (this.fColumnSize == 0) {
            col = 0;
        } else {
            col = this.fSelIndex / this.fColumnSize;
        }

        boolean ok = true;
        LBItem item;

        switch (eventArgs.Key) {
            case GK_SPACE:
                if (this.Options.contains(LBOptions.lboChecks)) {
                    this.doCheck(this.fSelIndex);
                }
                break;

            case GK_PRIOR:
                if (this.fSelIndex - this.fVisibleItems < 0) {
                    this.setSelIndex(0);
                } else {
                    this.setSelIndex(this.fSelIndex - this.fVisibleItems);
                }
                break;

            case GK_NEXT:
                if (this.fSelIndex + this.fVisibleItems > this.fItems.getCount() - 1) {
                    this.setSelIndex(this.fItems.getCount() - 1);
                } else {
                    this.setSelIndex(this.fSelIndex + this.fVisibleItems);
                }
                break;

            case GK_RIGHT:
                if (col < this.fColumns - 1) {
                    this.setSelIndex(this.fSelIndex + this.fColumnSize);
                } else {
                    this.setSelIndex(this.fItems.getCount() - 1);
                }
                break;

            case GK_LEFT:
                if (col > 0) {
                    this.setSelIndex(this.fSelIndex - this.fColumnSize);
                } else {
                    this.setSelIndex(0);
                }
                break;

            case GK_HOME:
                this.setSelIndex(0);
                break;

            case GK_END:
                this.setSelIndex(this.fItems.getCount() - 1);
                break;

            case GK_UP:
                this.setSelIndex(this.fSelIndex - 1);
                break;

            case GK_DOWN:
                this.setSelIndex(this.fSelIndex + 1);
                break;

            default:
                ok = false;
                break;
        }

        item = this.fItems.getItem(this.fSelIndex);

        if (ok) {
            this.doSelectItem(this, MouseButton.mbLeft, 0, 0, item, this.getItemRect(this.fSelIndex, -1));
        }
    }

    @Override
    protected void doMouseWheelEvent(MouseWheelEventArgs eventArgs)
    {
        super.doMouseWheelEvent(eventArgs);

        switch (eventArgs.WheelDelta) {
            case -1:
                /*if (this.FTopIndex > 0) {
                    this.FTopIndex = this.FTopIndex - 1;
                }*/
                this.setSelIndex(this.fSelIndex - 1);
                //this.FTopIndex = 
                break;

            case +1:
                this.setSelIndex(this.fSelIndex + 1);
                break;
        }
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        if (eventArgs.Button == MouseButton.mbLeft || eventArgs.Button == MouseButton.mbRight) {
            int idx = this.getItemByMouse(eventArgs.X, eventArgs.Y);
            if (idx > -1) {
                this.setSelIndex(idx);
                this.doSelectItem(this, eventArgs.Button, eventArgs.X, eventArgs.Y, this.fItems.getItem(this.fSelIndex), this.getItemRect(this.fSelIndex, -1));
            }

            super.doMouseDownEvent(eventArgs);
        }
    }

    protected void doPaintItem(BaseScreen screen, LBItem item, int itemIndex, Rect itemRect)
    {
        if (this.fSelIndex == itemIndex) {
            screen.drawRectangle(itemRect, this.SelColor, this.SelBorderColor);
        }

        int ih = this.getItemHeight();
        int tx = itemRect.Left + 2;
        int ty;

        if (this.Options.contains(LBOptions.lboTextTop)) {
            ty = itemRect.Top + 1;
        } else {
            ty = itemRect.Top + (ih - super.getFont().Height) / 2;
        }

        if (this.Options.contains(LBOptions.lboChecks)) {
            int cd = (ih - this.CheckHeight) / 2;
            int cx = itemRect.Left + cd;
            int cy = itemRect.Top + cd;
            if (item.Checked) {
                screen.drawImage(cx, cy, 0, 16, 16, 17, this.CheckImage, 255);
            } else {
                screen.drawImage(cx, cy, 0, 0, 16, 17, this.CheckImage, 255);
            }
            tx = itemRect.Left + this.CheckWidth + (cd << 1);
        }

        if (this.Options.contains(LBOptions.lboIcons)) {
            if (this.ImagesList != null && item.ImageIndex > -1) {
                this.ImagesList.drawImage(screen, tx, itemRect.Top + (this.getItemHeight() - this.fIconHeight) / 2, item.ImageIndex, 255);
            }
            tx = tx + this.fIconWidth + 2;
        }

        if (this.fMode != ListBox.MODE_ICONS) {
            super.getFont().setColor(item.Color);
            if (this.Options.contains(LBOptions.lboDoubleItem)) {
                StringList lst = new StringList();
                lst.setTextStr(item.Text);
                screen.drawText(tx, ty, lst.get(0), 0);
                if (lst.getCount() > 1) {
                    screen.drawText(tx, ty + CtlCommon.smFont.Height, lst.get(1), 0);
                }
                lst.dispose();
            } else {
                screen.drawText(tx, ty, item.Text, 0);
            }
        }
        
        if (this.OnItemDraw != null) {
            this.OnItemDraw.invoke(this, screen, itemIndex, itemRect);
        }
    }

    protected void doPaintSubItem(BaseScreen screen, LBItem item, int itemIndex, int subIndex, Rect subRect)
    {
        if (this.fSelIndex == itemIndex) {
            screen.drawRectangle(subRect, this.SelColor, this.SelBorderColor);
        }
        int tx = subRect.Left + 3;
        int ty = subRect.Top + (subRect.Bottom - subRect.Top - super.getFont().Height) / 2;
        screen.drawText(tx, ty, item.getSubItems().getItem(subIndex).Text, 0);
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        Rect crt = super.getClientRect();
        if ((super.ControlStyle.contains(ControlStyles.csOpaque))) {
            screen.fillRect(crt, BaseScreen.clBlack);
        }
        screen.setTextColor(BaseScreen.clGold, true);
        CtlCommon.drawCtlBorder(screen, crt);

        switch (this.fMode) {
            case MODE_LIST:
            case MODE_ICONS: {
                for (int col = 0; col < this.fColumns; col++) {
                    int lb = this.fTopIndex + col * this.fColumnSize;
                    int hb = lb + (this.fVisibleItems - 1);
                    if (hb >= this.fItems.getCount()) {
                        hb = this.fItems.getCount() - 1;
                    }
                    int defColor = screen.getTextColor(true);

                    for (int i = lb; i <= hb; i++) {
                        LBItem item = this.fItems.getItem(i);
                        this.doPaintItem(screen, item, i, this.getItemRect(i, -1));
                    }
                    screen.setTextColor(defColor, true);
                }
            }
            break;

            case MODE_REPORT: {
                int lb = this.fTopIndex;
                int hb = lb + (this.fVisibleItems - 1);
                if (hb >= this.fItems.getCount()) {
                    hb = this.fItems.getCount() - 1;
                }

                for (int i = lb; i <= hb; i++) {
                    LBItem item = this.fItems.getItem(i);

                    int defColor = screen.getTextColor(true);
                    this.doPaintItem(screen, item, i, this.getItemRect(i, -1));
                    screen.setTextColor(defColor, true);

                    int num2 = item.getSubItems().getCount();
                    for (int j = 0; j < num2; j++) {
                        defColor = screen.getTextColor(true);
                        this.doPaintSubItem(screen, item, i, j, this.getItemRect(i, j));
                        screen.setTextColor(defColor, true);
                    }
                }
            }
            break;                
        }
    }

    @Override
    protected void doResizeEvent()
    {
        super.doResizeEvent();
        if (this.ScrollBar != null) {
            this.ScrollBar.setLeft(super.getWidth() - this.ScrollBar.getWidth());
            this.ScrollBar.setHeight(super.getHeight());
        }
        this.Reset(false);
    }

    protected void doSelectItem(Object Sender, MouseButton Button, int aX, int aY, LBItem Item, Rect aRect)
    {
        if (this.Options.contains(LBOptions.lboChecks)) {
            int d = (aRect.Bottom - aRect.Top - this.CheckHeight) / 2;
            Rect checkRect = aRect;
            checkRect.Right = checkRect.Left + this.CheckWidth + (d << 1);
            if (checkRect.contains(aX, aY) || this.Options.contains(LBOptions.lboRadioChecks)) {
                this.doCheck(this.fSelIndex);
            }
        }
        if (this.OnItemSelect != null) {
            this.OnItemSelect.invoke(this, Button, Item);
        }
    }

    public final int getItemHeight()
    {
        int result;
        if (this.fIconHeight == 0) {
            result = this.fItemHeight;
        } else {
            if (this.fIconHeight > this.fItemHeight) {
                result = this.fIconHeight;
            } else {
                result = this.fItemHeight;
            }
        }
        if (this.Options.contains(LBOptions.lboDoubleItem)) {
            result = this.fItemHeight << 1;
        }
        return result;
    }

    @Override
    public Rect getIntRect()
    {
        Rect Result = super.getIntRect();
        if (this.ScrollBar != null) {
            Result.Right -= this.ScrollBar.getWidth();
        }
        return Result;
    }

    protected final Rect getItemRect(int itemIndex, int subIndex)
    {
        Rect r = super.getIntRect();
        int idx = itemIndex - this.fTopIndex;
        int row = idx % this.fColumnSize;
        int ax = r.Left;
        int ay = r.Top;

        Rect result = Rect.Empty();

        if (this.fMode != ListBox.MODE_LIST) {
            if (this.fMode == ListBox.MODE_REPORT) {
                if (subIndex < 0) {
                    LBColumnTitle title = this.fColumnTitles.getItem(0);
                    if (title == null) {
                        result = new Rect(ax, ay + row * this.getItemHeight(), ax + (r.Right - r.Left + 1), ay + (row + 1) * this.getItemHeight());
                    } else {
                        result = new Rect(ax, ay + row * this.getItemHeight(), ax + title.Width, ay + (row + 1) * this.getItemHeight());
                    }
                } else {
                    int offset = 0;
                    int i = 0;
                    if (subIndex >= i) {
                        int num = subIndex + 1;
                        do {
                            LBColumnTitle title = this.fColumnTitles.getItem(i);
                            offset += title.Width;
                            i++;
                        } while (i != num);
                    }
                    int w;
                    if (subIndex == this.fItems.getItem(itemIndex).getSubItems().getCount() - 1) {
                        w = r.Right - r.Left + 1 - offset;
                    } else {
                        LBColumnTitle title = this.fColumnTitles.getItem(subIndex + 1);
                        if (title == null) {
                            w = 0;
                        } else {
                            w = title.Width;
                        }
                    }
                    result = new Rect(ax + offset, ay + row * this.getItemHeight(), ax + offset + w, ay + (row + 1) * this.getItemHeight());
                }
                return result;
            }

            if (this.fMode != ListBox.MODE_ICONS) {
                return result;
            }
        }
        int col = idx / this.fColumnSize;
        result = new Rect(ax + col * this.fColumnWidth, ay + row * this.getItemHeight(), ax + (col + 1) * this.fColumnWidth, ay + (row + 1) * this.getItemHeight());
        return result;
    }

    protected void OnChange(Object Sender)
    {
        this.Reset(false);
    }

    protected final void OnScroll(Object Sender)
    {
        this.fTopIndex = this.ScrollBar.getPos();
    }

    public ListBox(BaseControl owner)
    {
        super(owner);

        this.fMode = ListBox.MODE_LIST;
        this.fColumnTitles = new LBColumnTitles();
        this.fColumnTitles.OnChange = this::OnChange;
        this.fItems = new LBItemList(this, null);
        this.fItems.OnChange = this::OnChange;
        this.fColumns = 1;
        this.fTopIndex = 0;
        this.fSelIndex = 0;
        this.Options = new LBOptions();

        this.ScrollBar = new ScrollBar(this);
        this.ScrollBar.setMin(0);
        this.ScrollBar.setMax(100);
        this.ScrollBar.setPos(0);
        this.ScrollBar.OnChange = this::OnScroll;
        this.ScrollBar.setLeft(super.getWidth() - this.ScrollBar.getWidth());
        this.ScrollBar.setTop(0);
        this.ScrollBar.setHeight(104);

        this.SelColor = BaseScreen.clGray;
        this.SelBorderColor = BaseScreen.clGray;
        super.setHeight(100);
        super.setWidth(200);
        this.setItemHeight(super.getFont().Height + 2);
        BaseScreen scr = super.getMainWindow().getScreen();

        this.CheckImage = ResourceManager.loadImage(scr, "itf/Check.tga", BaseScreen.clNone);
        this.CheckHeight = (int) ((int) this.CheckImage.Height / 3);
        this.CheckWidth = (int) this.CheckImage.Width;

        this.TVBtn = ResourceManager.loadImage(scr, "itf/TVBtn.tga", BaseScreen.clBlack);
        this.LevelOffset = (int) this.TVBtn.Width;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.CheckImage != null) {
                this.CheckImage.dispose();
                this.CheckImage = null;
            }

            this.fItems.dispose();
            this.fColumnTitles.dispose();
        }
        super.dispose(disposing);
    }

    private void doCheck(int index)
    {
        if (this.Options.contains(LBOptions.lboRadioChecks)) {
            int num = this.fItems.getCount();
            for (int i = 0; i < num; i++) {
                this.setChecked(i, false);
            }
        }
        this.setChecked(index, !this.getChecked(index));
    }

    public final LBItem getAbsoluteItem(int index)
    {
        LBItem result = this.fItems.getItem(index);
        return result;
    }

    public final int getItemByMouse(int aX, int aY)
    {
        int result = -1;

        Rect rCom = this.getIntRect();
        int col = (aX - rCom.Left) / this.fColumnWidth;
        int row = (aY - rCom.Top) / this.getItemHeight();

        int idx = this.fTopIndex + col * this.fColumnSize + row;
        if (idx >= 0 && idx < this.fItems.getCount()) {
            result = idx;
        }

        return result;
    }
}
