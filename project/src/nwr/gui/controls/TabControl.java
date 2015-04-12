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

import jzrlib.core.INotifyEvent;
import jzrlib.core.Rect;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;
import nwr.engine.MouseEventArgs;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class TabControl extends BaseControl
{
    private int fTabIndex;

    public INotifyEvent OnChange;

    public TabControl(BaseControl owner)
    {
        super(owner);
        this.fTabIndex = -1;
    }

    public final int getTabIndex()
    {
        return this.fTabIndex;
    }

    public final TabSheet getActivePage()
    {
        TabSheet result = null;
        if (this.fTabIndex >= 0 && this.fTabIndex < super.getControls().getCount()) {
            result = ((TabSheet) super.getControls().get(this.fTabIndex));
        }
        return result;
    }

    public final int getPagesCount()
    {
        return super.getControls().getCount();
    }

    public final TabSheet getPage(int index)
    {
        TabSheet result = null;
        if (index >= 0 && index < super.getControls().getCount()) {
            result = ((TabSheet) super.getControls().get(index));
        }
        return result;
    }

    private void resizeSheet(TabSheet sheet)
    {
        int th = 8;
        if (super.getFont() != null) {
            th += super.getFont().Height;
        }
        sheet.setTop(th);
        sheet.setLeft(0);
        sheet.setWidth(super.getWidth());
        sheet.setHeight(super.getHeight() - th);
    }

    public final void setTabIndex(int value)
    {
        if (this.fTabIndex != value) {
            TabSheet page = this.getPage(this.fTabIndex);
            if (page != null) {
                page.setVisible(false);
            }
            super.setActiveControl(null);
            this.fTabIndex = value;
            page = this.getPage(this.fTabIndex);
            if (page != null) {
                page.setVisible(true);
                super.setActiveControl(page);
            }
            this.Change();
        }
    }

    protected final void Change()
    {
        if (this.OnChange != null) {
            this.OnChange.invoke(this);
        }
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        if (eventArgs.Button == MouseButton.mbLeft || eventArgs.Button == MouseButton.mbRight) {
            int num = this.getPagesCount();
            for (int idx = 0; idx < num; idx++) {
                if (this.getTabRect(idx).contains(eventArgs.X, eventArgs.Y)) {
                    this.setTabIndex(idx);
                    break;
                }
            }
        }

        super.doMouseDownEvent(eventArgs);
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        Rect crt = super.getClientRect();
        int L = crt.Left;
        int T = crt.Top;
        int R = crt.Right;
        int B = crt.Bottom;
        int h = 8;
        if (super.getFont() != null) {
            h += super.getFont().Height;
        }
        int mw = 0;

        Rect tsr;
        int num = this.getPagesCount();
        for (int idx = 0; idx < num; idx++) {
            TabSheet page = this.getPage(idx);
            int tw = screen.getTextWidth(page.getCaption()) + 16;
            tsr = new Rect(L + mw, T + 0, L + mw + tw - 1, T + h - 1);
            mw += tw;

            CtlBorders brd;
            if (idx == this.getTabIndex()) {
                brd = new CtlBorders(CtlBorders.cbLeft, CtlBorders.cbTop, CtlBorders.cbRight);
            } else {
                brd = new CtlBorders(CtlBorders.cbLeft, CtlBorders.cbTop, CtlBorders.cbRight, CtlBorders.cbBottom);
            }
            CtlCommon.drawCtlBorder(screen, tsr, brd);
            screen.drawText(tsr.Left + 8, tsr.Top + 4, page.getCaption(), 0);
        }

        tsr = new Rect(L + mw, T + h - 2, R, B);
        screen.drawFilled(tsr, BaseScreen.FILL_HORZ, 2, 0, 31, 2, mw, T + h - 2, CtlCommon.fCtlDecor);
        tsr = new Rect(L, T + h, R, B);
        CtlCommon.drawCtlBorder(screen, tsr, new CtlBorders(CtlBorders.cbLeft, CtlBorders.cbRight, CtlBorders.cbBottom));
    }

    @Override
    protected void doResizeEvent()
    {
        super.doResizeEvent();

        int num = super.getControls().getCount();
        for (int i = 0; i < num; i++) {
            this.resizeSheet(this.getPage(i));
        }
    }

    public final TabSheet addPage(String caption)
    {
        TabSheet result = new TabSheet(this);
        result.setCaption(caption);
        result.setVisible(false);
        this.resizeSheet(result);
        return result;
    }

    public final Rect getTabRect(int index)
    {
        int h = super.getFont().Height + 8;
        int mw = 0;

        int num = this.getPagesCount();
        for (int i = 0; i < num; i++) {
            int tw = super.getFont().getTextWidth(this.getPage(i).getCaption()) + 16;
            Rect rt = new Rect(mw, 0, mw + tw - 1, h - 1);
            mw += tw;

            if (i == index) {
                return rt;
            }
        }

        return Rect.Empty();
    }
}
