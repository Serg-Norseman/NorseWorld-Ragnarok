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
import nwr.engine.BaseControl;
import nwr.engine.BaseHintWindow;
import nwr.engine.BaseScreen;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class HintWindow extends BaseHintWindow
{
    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        Rect crt = super.getClientRect();
        screen.drawRectangle(crt, BaseScreen.clBlack, BaseScreen.clBlack);
        CtlCommon.drawCtlBorder(screen, crt);
        screen.setTextColor(BaseScreen.clGold, true);
        int h = screen.getTextHeight("W");

        int num = this.fText.getCount();
        for (int i = 0; i < num; i++) {
            screen.drawText(crt.Left + 8, crt.Top + 8 + i * h, this.fText.get(i), 0);
        }
    }

    public HintWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.Margin = 8;
    }
}
