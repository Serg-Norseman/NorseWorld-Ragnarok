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
import jzrlib.utils.RefObject;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.ControlStyles;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Marquee extends TextControl
{
    private boolean fActive;
    private float fSpeed;
    private long fStartTime;
    private int fY;
    
    public IMarqueeDoneEvent OnMarqueeDone;

    public Marquee(BaseControl owner)
    {
        super(owner);
        this.fY = 0;
        this.fSpeed = 0.015f;
        super.setHeight(100);
        super.setWidth(200);
        super.fLineHeight = 20;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (super.getVisible()) {
                this.doHideEvent();
            }
        }
        super.dispose(disposing);
    }

    public final boolean getActive()
    {
        return this.fActive;
    }

    public final void setActive(boolean value)
    {
        if (this.fActive != value) {
            this.fActive = value;
        }
    }

    protected final void DoDone(RefObject<Boolean> refRepeat)
    {
        refRepeat.argValue = true;
        if (this.OnMarqueeDone != null) {
            this.OnMarqueeDone.invoke(this, refRepeat);
        }
    }

    @Override
    protected void doHideEvent()
    {
        this.setActive(false);
        super.doHideEvent();
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        Rect rt = super.getClientRect();
        if ((super.ControlStyle.contains(ControlStyles.csOpaque))) {
            screen.fillRect(rt, BaseScreen.clBlack);
        }
        CtlCommon.drawCtlBorder(screen, rt);

        rt = super.getIntRect();
        Rect r2 = new Rect();
        r2.Left = rt.Left + super.Margin;
        r2.Top = rt.Top + super.Margin;
        r2.Right = rt.Right - super.Margin;
        r2.Bottom = rt.Bottom - super.Margin;
        int top = rt.Top - super.fLineHeight;
        int bot = rt.Bottom;
        rt.Left = r2.Left;
        rt.Right = r2.Right;

        screen.setTextColor(this.TextColor, true);

        int num = super.fLines.getCount();
        for (int i = 0; i < num; i++) {
            int yOffset = this.fY + i * super.fLineHeight;
            if (yOffset >= top && yOffset < bot) {
                rt.Top = r2.Top + yOffset;
                rt.Bottom = r2.Bottom + yOffset;
                this.doPaintLine(screen, i, rt);
            }
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        this.setActive(true);
    }

    @Override
    public void update(long time)
    {
        super.update(time);

        if (this.fActive) {
            boolean first;
            if (this.fStartTime == 0) {
                this.fStartTime = time;
                first = true;
            } else {
                first = false;
            }

            float y = (float) super.getIntRect().Bottom - this.fSpeed * (time - this.fStartTime);
            if ((y + (super.fLines.getCount() * super.fLineHeight)) < (double) super.getIntRect().Top) {
                if (!first) {
                    boolean repeat = true;
                    RefObject<Boolean> refRepeat = new RefObject<>(repeat);
                    this.DoDone(refRepeat);
                    repeat = refRepeat.argValue;
                    if (!repeat) {
                        this.setActive(false);
                    }
                }
                this.fStartTime = 0;
            }
            this.fY = Math.round(y);
        }
    }
}
