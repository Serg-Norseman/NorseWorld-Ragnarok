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

import jzrlib.core.Rect;
import jzrlib.utils.TextUtils;
import nwr.engine.ControlStyles;
import nwr.engine.KeyEventArgs;
import nwr.engine.KeyPressEventArgs;
import nwr.engine.MouseEventArgs;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class EditBox extends BaseControl
{
    private int fCaretPos;
    private String fText;

    @Override
    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        switch (eventArgs.Key) {
            case GK_BACK:
                if (this.fCaretPos > 0) {
                    this.fText = this.fText.substring(0, this.fCaretPos - 1) + this.fText.substring(this.fCaretPos);
                    this.fCaretPos--;
                }
                break;

            case GK_DELETE:
                if (this.fCaretPos >= 0 && this.fCaretPos < this.fText.length()) {
                    this.fText = this.fText.substring(0, this.fCaretPos) + this.fText.substring(this.fCaretPos + 1);
                }
                break;

            case GK_RIGHT:
                if (this.fCaretPos < this.fText.length()) {
                    this.fCaretPos++;
                }
                break;

            case GK_LEFT:
                if (this.fCaretPos > 0) {
                    this.fCaretPos--;
                }
                break;
        }

        super.doKeyDownEvent(eventArgs);
    }

    @Override
    protected void doKeyPressEvent(KeyPressEventArgs eventArgs)
    {
        if (super.getFont().isValidChar(eventArgs.Key)) {
            this.fText = new StringBuilder(this.fText).insert(this.fCaretPos, eventArgs.Key).toString();
            this.fCaretPos++;
        }
        super.doKeyPressEvent(eventArgs);
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        super.doMouseDownEvent(eventArgs);

        eventArgs.X -= (super.getHeight() - super.getFont().Height) / 2;
        int sh = 0;
        String ln = "";

        int num = (this.fText != null) ? this.fText.length() : 0;
        for (int i = 1; i <= num; i++) {
            ln += this.fText.charAt(i - 1);

            int nw = super.getFont().getTextWidth(ln);
            if (eventArgs.X >= sh && eventArgs.X <= nw) {
                this.fCaretPos = i;
                break;
            }

            sh = nw;
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        Rect crt = super.getClientRect();
        if ((super.ControlStyle.contains(ControlStyles.csOpaque))) {
            screen.fillRect(crt, BaseScreen.clBlack);
        }
        CtlCommon.drawCtlBorder(screen, crt);

        screen.setTextColor(BaseScreen.clGold, true);

        int th = super.getFont().Height;
        int mg = (super.getHeight() - th) / 2;
        int x = crt.Left + mg;
        int y = crt.Top + mg;
        screen.drawText(x, y, this.fText, 0);

        int tw;
        if (this.fCaretPos == 0) {
            tw = 0;
        } else {
            tw = screen.getTextWidth(this.fText.substring(0, this.fCaretPos)) + 1;
        }

        int cw;
        if (this.fCaretPos >= this.fText.length()) {
            cw = 8;
        } else {
            cw = screen.getTextWidth(this.fText.substring(this.fCaretPos - 1, this.fCaretPos + 1)); // FIXME: need caret line
        }
        x += tw;
        y = y + th + 1;
        screen.drawLine(x, y, x + cw, y, BaseScreen.clGold);

        //screen.drawText(x + tw, y + 3, "_", 0); // cursor
    }

    @Override
    public Rect getIntRect()
    {
        return new Rect(2, 2, super.getWidth() - 3, super.getHeight() - 3);
    }

    public String getText()
    {
        return this.fText;
    }

    public void setText(String value)
    {
        if (!TextUtils.equals(this.fText, value)) {
            this.fText = value;

            if (TextUtils.equals(this.fText, "")) {
                this.fCaretPos = 0;
            } else {
                this.fCaretPos = this.fText.length();
            }
        }
    }

    public EditBox(BaseControl owner)
    {
        super(owner);
        this.setText("");

        if (super.getFont() != null) {
            super.setHeight(super.getFont().Height + 8);
        } else {
            super.setHeight(20);
        }

        super.setWidth(200);
    }
}
