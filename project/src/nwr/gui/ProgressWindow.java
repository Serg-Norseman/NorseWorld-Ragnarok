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

import jzrlib.utils.Logger;
import jzrlib.core.Rect;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.ProgressBar;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ProgressWindow extends NWWindow
{
    private final ProgressBar fBar;
    private final Rect fGaugeRect;

    public String StageLabel;

    public final int getStage()
    {
        return this.fBar.getPos();
    }

    public final int getStageCount()
    {
        return this.fBar.Max;
    }

    public final void setStage(int value)
    {
        if (this.fBar == null) {
            return;
        }

        this.fBar.setPos(value);
    }

    public final void setStageCount(int value)
    {
        this.fBar.Max = value;
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        try {
            super.doPaintEvent(screen);

            screen.setTextColor(BaseScreen.clGold, true);
            screen.drawText(this.fGaugeRect.Left, this.fGaugeRect.Top - CtlCommon.smFont.Height, this.StageLabel, 0);
        } catch (Exception ex) {
            Logger.write("ProgressWindow.DoPaintTo(): " + ex.getMessage());
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        this.setStage(0);
    }

    @Override
    protected void doHideEvent()
    {
        super.doHideEvent();
        this.setStage(0);
    }

    public ProgressWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(320);
        super.setHeight(CtlCommon.smFont.Height + 60);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        this.fGaugeRect = new Rect(20, 20 + CtlCommon.smFont.Height, 300, 40 + CtlCommon.smFont.Height);

        this.fBar = new ProgressBar(this);
        this.fBar.setBounds(this.fGaugeRect);
        this.setStageCount(100);
    }

    public final void step()
    {
        this.setStage(this.getStage() + 1);
    }
}
