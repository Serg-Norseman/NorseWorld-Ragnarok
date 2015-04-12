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

import jzrlib.utils.RefObject;
import nwr.core.StaticData;
import nwr.core.types.GameScreen;
import nwr.engine.BaseControl;
import nwr.engine.ControlStyles;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.TextBox;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class AboutWindow extends NWWindow
{
    private final TextBox fText;

    public AboutWindow(BaseControl owner)
    {
        super(owner);

        super.setFont(CtlCommon.smFont);
        super.setWidth(590);
        super.setHeight(430);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        this.fText = new TextBox(this);
        this.fText.setFont(CtlCommon.smFont);
        this.fText.setLeft(10);
        this.fText.setTop(10);
        this.fText.ControlStyle.exclude(ControlStyles.csOpaque);
        this.fText.setWidth(570);
        this.fText.setHeight(410);
        this.fText.setVisible(true);
        this.fText.OnGetVariable = this::onGetVar;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fText.dispose();
        }
        super.dispose(disposing);
    }

    private void onGetVar(Object sender, RefObject<String> refVar)
    {
        if (refVar.argValue.compareTo("ver") == 0) {
            refVar.argValue = StaticData.rs_GameVersion;
            refVar.argValue = refVar.argValue.substring(1);
        } else {
            if (refVar.argValue.compareTo("dev_time") == 0) {
                refVar.argValue = StaticData.rs_GameDevTime;
            }
        }
    }

    @Override
    protected void DoClose()
    {
        if (GlobalVars.nwrWin.getMainScreen() == GameScreen.gsStartup) {
            GlobalVars.nwrWin.showStartupWin();
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        this.fText.getLines().beginUpdate();
        this.fText.getLines().setTextStr(NWWindow.getTextFileByLang("About"));
        this.fText.getLines().endUpdate();
        super.setActiveControl(this.fText);
    }
}
