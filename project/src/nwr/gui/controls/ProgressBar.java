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

import jzrlib.utils.Logger;
import jzrlib.core.Rect;
import nwr.engine.BaseControl;
import nwr.engine.BaseMainWindow;
import nwr.engine.BaseScreen;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ProgressBar extends BaseControl
{
    private int fPos;
    private int fPercent;

    public int Max;
    public int Min;
    public boolean ShowPos;

    public final int getPos()
    {
        return this.fPos;
    }

    public final void setPos(int value)
    {
        if (value < this.Min) {
            this.fPos = this.Min;
        } else {
            if (value > this.Max) {
                this.fPos = this.Max;
            } else {
                if (value != this.fPos) {
                    this.fPos = value;
                }
            }
        }
        int percent;

        if (this.Max == 0) {
            percent = 0;
        } else {
            percent = (int) ((long) Math.round((double) (this.fPos * 100) / (double) this.Max));
        }

        if (this.fPercent != percent) {
            BaseMainWindow mainWnd = super.getMainWindow();
            mainWnd.repaint();
        }
        this.fPercent = percent;
    }

    public static void drawGauge(BaseScreen screen, Rect R, int cur, int max, int cBorder, int cUnready, int cReady)
    {
        try {
            if (cur < 0) {
                cur = 0;
            }
            if (cur > max) {
                if (max > 0) {
                    cur %= max;
                } else {
                    cur = max;
                }
            }

            int L = R.getWidth();
            float percent;

            if (max == 0) {
                percent = 0f;
            } else {
                percent = (((float) cur / (float) max));
            }

            int rw = Math.round((float) L * percent);

            if (rw > 0) {
                screen.drawRectangle(new Rect(R.Left, R.Top, R.Left + rw, R.Bottom), cReady, cBorder);
            }
            if (rw < L) {
                screen.drawRectangle(new Rect(R.Left + rw - 1, R.Top, R.Right, R.Bottom), cUnready, cBorder);
            }
        } catch (Exception ex) {
            Logger.write("ProgressBar.drawGauge(): " + ex.getMessage());
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        Rect crt = super.getClientRect();
        CtlCommon.drawCtlBorder(screen, crt);
        crt.inflate(-1, -1);
        ProgressBar.drawGauge(screen, crt, this.getPos(), this.Max, BaseScreen.clBlack, BaseScreen.clGray, BaseScreen.clGold);
    }

    @Override
    public String getCaption()
    {
        return String.valueOf(this.getPos());
    }

    public ProgressBar(BaseControl owner)
    {
        super(owner);
        this.Min = 0;
        this.Max = 100;
        this.ShowPos = true;
        this.fPercent = 0;
    }
}
