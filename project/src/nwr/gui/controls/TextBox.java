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

import jzrlib.core.ExtList;
import jzrlib.utils.Logger;
import jzrlib.core.Rect;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.ControlStyles;
import nwr.engine.KeyEventArgs;
import nwr.engine.MouseEventArgs;
import nwr.engine.MouseWheelEventArgs;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class TextBox extends TextControl
{
    private static final int LK_INDEP = 0;
    private static final int LK_FIRST = 1;
    private static final int LK_SECOND = 2;

    private static class HyperLink
    {
        public int symBeg;
        public int symEnd;
        public int Row;
        public int x1;
        public int x2;
        public int Kind;
    }

    private boolean fLinks;
    private final ExtList<HyperLink> fLinksList;
    private final ScrollBar fScrollBar;
    private int fTopIndex;
    
    public ILinkClickEvent OnLinkClick;

    public boolean LastVisible;
    public int LinkColor;

    public TextBox(BaseControl owner)
    {
        super(owner);
        this.fTopIndex = 0;

        this.fScrollBar = new ScrollBar(this);
        this.fScrollBar.setMin(0);
        this.fScrollBar.setMax(0);
        this.fScrollBar.setPos(0);
        this.fScrollBar.OnChange = this::OnScroll;
        this.fScrollBar.setLeft(super.getWidth() - this.fScrollBar.getWidth());
        this.fScrollBar.setTop(0);
        this.fScrollBar.setHeight(104);

        this.fLinks = false;
        this.fLinksList = new ExtList<>(true);

        super.setHeight(100);
        super.setWidth(200);
        this.LinkColor = BaseScreen.clSkyBlue;
        super.fLineHeight = 20;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fLinksList.dispose();
        }
        super.dispose(disposing);
    }

    public final boolean getLinks()
    {
        return this.fLinks;
    }

    public final int getPageSize()
    {
        return this.fVisibleLines;
    }

    public final int getTopIndex()
    {
        return this.fTopIndex;
    }

    public final void setLinks(boolean Value)
    {
        this.fLinks = Value;
    }

    public final void setTopIndex(int Value)
    {
        if (this.fTopIndex != Value) {
            this.fTopIndex = Value;
            this.fScrollBar.setPos(this.fTopIndex);
        }
    }

    @Override
    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        switch (eventArgs.Key) {
            case GK_UP:
                if (this.fTopIndex > 0) {
                    this.setTopIndex(this.getTopIndex() - 1);
                }
                break;

            case GK_DOWN:
                if (this.fTopIndex < this.fLines.getCount() - 1 && this.fTopIndex + this.fVisibleLines < this.fLines.getCount()) {
                    this.setTopIndex(this.getTopIndex() + 1);
                }
                break;

            case GK_HOME:
                this.setTopIndex(0);
                break;

            case GK_END: {
                int idx = this.fLines.getCount() - this.fVisibleLines;
                if (idx >= 0) {
                    this.setTopIndex(idx);
                }
                break;
            }

            case GK_PRIOR:
                if (this.fTopIndex - this.fVisibleLines < 0) {
                    this.setTopIndex(0);
                } else {
                    this.setTopIndex(this.getTopIndex() - this.fVisibleLines);
                }
                break;

            case GK_NEXT:
                if (this.fTopIndex + this.fVisibleLines > this.fLines.getCount() - 1) {
                    int idx = this.fLines.getCount() - this.fVisibleLines;
                    if (idx >= 0) {
                        this.setTopIndex(idx);
                    }
                } else {
                    this.setTopIndex(this.getTopIndex() + this.fVisibleLines);
                }
                break;
        }

        super.doKeyDownEvent(eventArgs);
    }

    @Override
    protected void doMouseWheelEvent(MouseWheelEventArgs eventArgs)
    {
        super.doMouseWheelEvent(eventArgs);

        switch (eventArgs.WheelDelta) {
            case -1:
                if (this.fTopIndex > 0) {
                    this.setTopIndex(this.getTopIndex() - 1);
                }
                break;

            case +1:
                if (this.fTopIndex < this.fLines.getCount() - 1 && this.fTopIndex + this.fVisibleLines < this.fLines.getCount()) {
                    this.setTopIndex(this.getTopIndex() + 1);
                }
                break;
        }
    }

    private int findFirstLink(int row)
    {
        int num = this.fLinksList.getCount();
        for (int i = 0; i < num; i++) {
            HyperLink link = this.fLinksList.get(i);
            if (link.Row == row) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    protected void doPaintLine(BaseScreen screen, int index, Rect rect)
    {
        try {
            if (index < 0 || index >= super.fLines.getCount()) {
                return;
            }

            int x = rect.Left;
            int y = rect.Top;
            String line = super.fLines.get(index);
            screen.setTextColor(this.TextColor, true);

            if (!this.fLinks) {
                screen.drawText(x, y, line, 0);
            } else {
                int lnkIndex = this.findFirstLink(index);
                int offset = x;

                if (lnkIndex < 0) {
                    screen.drawText(x, y, line, 0);
                } else {
                    String chunk = "";
                    HyperLink curLink = null;

                    int i = 0;
                    while (i < line.length()) {
                        char chr = line.charAt(i);

                        if (chr != '@') {
                            chunk += chr;
                        } else {
                            if (curLink == null) {
                                screen.setTextColor(this.TextColor, true);
                                screen.drawText(offset, y, chunk, 0);

                                int sw = screen.getTextWidth(chunk);
                                offset += sw;

                                curLink = this.fLinksList.get(lnkIndex);
                                curLink.x1 = offset;
                            } else {
                                screen.setTextColor(this.LinkColor, true);
                                screen.drawText(offset, y, chunk, 0);

                                int sw = screen.getTextWidth(chunk);
                                offset += sw;

                                curLink.x2 = offset;
                                curLink = null;

                                lnkIndex++;
                            }

                            chunk = "";
                        }
                        
                        i++;
                    }

                    // end chunk
                    if (chunk.length() != 0) {
                            if (curLink == null) {
                                screen.setTextColor(this.TextColor, true);
                                screen.drawText(offset, y, chunk, 0);
                            } else {
                                screen.setTextColor(this.LinkColor, true);
                                screen.drawText(offset, y, chunk, 0);

                                int sw = screen.getTextWidth(chunk);
                                offset += sw;

                                curLink.x2 = offset;
                                curLink = null;
                            }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("TextBox.DoPaintLine(): " + ex.getMessage());
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        try {
            if (super.getFont() == null) {
                return;
            }

            Rect rt = super.getClientRect();
            if ((super.ControlStyle.contains(ControlStyles.csOpaque))) {
                screen.fillRect(rt, BaseScreen.clBlack);
            }
            CtlCommon.drawCtlBorder(screen, rt);

            screen.setTextColor(BaseScreen.clGold, true);

            int lb = this.fTopIndex;
            int hb = super.fLines.getCount() - 1;
            if (hb - lb >= this.fVisibleLines) {
                hb = lb + (this.fVisibleLines - 1);
            }

            rt = super.getIntRect();
            Rect r2 = new Rect();
            r2.Left = rt.Left + super.Margin;
            r2.Right = rt.Right - super.Margin;
            int top = rt.Top + super.Margin;

            for (int i = lb; i <= hb; i++) {
                r2.Top = top + (i - this.fTopIndex) * this.fLineHeight;
                r2.Bottom = r2.Top + this.fLineHeight - super.Margin;
                this.doPaintLine(screen, i, r2);
            }
        } catch (Exception ex) {
            Logger.write("TextBox.DoPaintTo(): " + ex.getMessage());
        }
    }

    @Override
    public Rect getIntRect()
    {
        Rect result = super.getIntRect();
        if (this.fScrollBar != null) {
            result.Right = result.Right - this.fScrollBar.getWidth() - (this.Margin << 1);
        }
        return result;
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        super.doMouseDownEvent(eventArgs);

        Rect r = super.getIntRect();
        int eX = eventArgs.X - r.Left;
        int eY = eventArgs.Y - r.Top;

        int row = this.fTopIndex + eY / this.fLineHeight;
        if (row >= 0 && row < super.fLines.getCount()) {
            int num = this.fLinksList.getCount();
            for (int i = 0; i < num; i++) {
                HyperLink link = this.fLinksList.get(i);

                if (link.Row == row && (eX > link.x1 && eX < link.x2)) {
                    this.doLinkClickEvent(link, i);
                    return;
                }
            }
        }
    }

    private void doLinkClickEvent(HyperLink link, int idx)
    {
        if (this.OnLinkClick != null) {
            String linkValue = this.processLink(link, idx);
            this.OnLinkClick.invoke(this, linkValue);
        }
    }

    private String processLink(HyperLink link, int idx)
    {
        String id = "";

        String line;
        switch (link.Kind) {
            case LK_INDEP:
                line = this.fLines.get(link.Row);
                id = line.substring(link.symBeg + 1, link.symEnd);
                break;

            case LK_FIRST: {
                line = this.fLines.get(link.Row);
                id = line.substring(link.symBeg + 1, link.symEnd + 1);

                HyperLink nextLink = this.fLinksList.get(idx + 1);
                line = this.fLines.get(nextLink.Row);
                id = id + line.substring(nextLink.symBeg, nextLink.symEnd);
                break;
            }

            case LK_SECOND: {
                HyperLink prevLink = this.fLinksList.get(idx - 1);
                line = this.fLines.get(prevLink.Row);
                id = line.substring(prevLink.symBeg + 1, prevLink.symEnd + 1);

                line = this.fLines.get(link.Row);
                id = id + line.substring(link.symBeg, link.symEnd);
                break;
            }
        }
        
        return id;
    }

    @Override
    protected void prepareText()
    {
        if (this.fLinks) {
            this.fLinksList.clear();

            HyperLink curLink = null;
            for (int row = 0; row < this.fLines.getCount(); row++) {
                String text = this.fLines.get(row);
                if (text.length() < 1) {
                    continue;
                }

                int idx = 0;
                while (idx < text.length()) {
                    char c = text.charAt(idx);
                    if (c == '@') {
                        if (curLink == null) {
                            curLink = new HyperLink();
                            curLink.Row = row;
                            curLink.Kind = LK_INDEP;
                            curLink.symBeg = idx;
                            this.fLinksList.add(curLink);
                        } else {
                            curLink.symEnd = idx;
                            curLink = null;
                        }
                    }
                    idx++;
                }

                if (curLink != null) {
                    curLink.symEnd = text.length() - 1;
                    curLink.Kind = LK_FIRST;

                    curLink = new HyperLink();
                    curLink.Row = row + 1;
                    curLink.Kind = LK_SECOND;
                    curLink.symBeg = 0;
                    this.fLinksList.add(curLink);
                }
            }
        }
    }

    @Override
    protected void doResizeEvent()
    {
        super.doResizeEvent();
        if (this.fScrollBar != null) {
            this.fScrollBar.setLeft(super.getWidth() - this.fScrollBar.getWidth());
            this.fScrollBar.setHeight(super.getHeight());
        }
    }

    @Override
    protected void OnChange(Object Sender)
    {
        super.OnChange(Sender);

        if (this.fScrollBar != null) {
            if (this.fLines.getCount() == 0) {
                this.fScrollBar.setMax(0);
                this.fScrollBar.setPos(0);
            } else {
                this.fScrollBar.setMax(this.fLines.getCount() - this.fVisibleLines);
                this.fScrollBar.setPos(0);
            }
        }

        if (this.LastVisible) {
            if (super.fLines.getCount() - this.getPageSize() < 0) {
                this.setTopIndex(0);
            } else {
                this.setTopIndex(super.fLines.getCount() - this.getPageSize());
            }
        }
    }

    protected final void OnScroll(Object Sender)
    {
        this.fTopIndex = this.fScrollBar.getPos();
    }

    public boolean isValidLink(String linkValue)
    {
        return true;
    }
}
