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

import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.KeyEventArgs;
import nwr.engine.MouseEventArgs;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.Marquee;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class IntroWindow extends NWWindow
{
    private final Marquee fMarquee;

    public IntroWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(590);
        super.setHeight(430);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);
        super.Shifted = false;
        super.BackDraw = false;

        this.fMarquee = new Marquee(this);
        this.fMarquee.setActive(false);
        this.fMarquee.setFont(CtlCommon.smFont);
        this.fMarquee.setLeft(10);
        this.fMarquee.setTop(10);
        this.fMarquee.setWidth(570);
        this.fMarquee.setHeight(410);
        this.fMarquee.setVisible(true);
        this.fMarquee.OnMouseDown = this::onMarqueeMouseDown;
        this.fMarquee.TextColor = BaseScreen.clGoldenrod;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fMarquee.dispose();
        }
        super.dispose(disposing);
    }

    @Override
    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        this.doneIntro();
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        super.doMouseDownEvent(eventArgs);
        this.doneIntro();
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        this.fMarquee.getLines().beginUpdate();
        this.fMarquee.getLines().setTextStr(NWWindow.getTextFileByLang("Intro"));
        this.fMarquee.getLines().endUpdate();
    }

    private void doneIntro()
    {
        this.hide();
        GlobalVars.nwrWin.newGame_Finish();
    }

    private void onMarqueeMouseDown(Object sender, MouseEventArgs eventArgs)
    {
        this.doneIntro();
    }
}
