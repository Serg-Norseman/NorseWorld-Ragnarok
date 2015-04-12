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

import nwr.creatures.NWCreature;
import nwr.engine.BaseControl;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class DialogWindow extends NWWindow
{
    protected NWCreature fCollocutor;

    private void onBtnClose(Object sender)
    {
        this.hide();
    }

    public final void setCollocutor(NWCreature value)
    {
        if (this.fCollocutor != value) {
            this.fCollocutor = value;
        }
    }

    public DialogWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(600);
        super.setHeight(460);
        super.WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview);
        super.Shifted = true;

        NWButton btnClose = new NWButton(this);
        btnClose.setWidth(90);
        btnClose.setHeight(30);
        btnClose.setLeft(super.getWidth() - 90 - 20);
        btnClose.setTop(super.getHeight() - 30 - 20);
        btnClose.setImageFile("itf/DlgBtn.tga");
        btnClose.OnClick = this::onBtnClose;
        btnClose.OnLangChange = GlobalVars.nwrWin::LangChange;
        btnClose.setLangResID(8);
    }
}
