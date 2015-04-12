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

import nwr.core.types.EventID;
import nwr.core.types.GameScreen;
import nwr.engine.BaseControl;
import nwr.engine.BaseImage;
import nwr.engine.BaseScreen;
import nwr.engine.ResourceManager;
import nwr.engine.WindowStyles;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class MenuWindow extends NWWindow
{
    private final BaseImage FSplash;

    private void onBtnSave(Object sender)
    {
        this.hide();
        GlobalVars.nwrWin.doEvent(EventID.event_Save, null, null, null);
    }

    private void onBtnExit(Object sender)
    {
        GlobalVars.nwrWin.doEvent(EventID.event_Quit, null, null, null);
    }

    private void onBtnLoad(Object sender)
    {
        this.hide();
        GlobalVars.nwrWin.doEvent(EventID.event_Load, null, null, null);
    }

    private void onBtnNew(Object sender)
    {
        this.hide();
        GlobalVars.nwrWin.setScreen(GameScreen.gsStartup);
        GlobalVars.nwrWin.doEvent(EventID.event_New, null, null, null);
    }

    private void onBtnOptions(Object sender)
    {
        this.hide();
        GlobalVars.nwrWin.doEvent(EventID.event_Options, null, null, null);
    }

    private void onBtnClose(Object sender)
    {
        this.hide();
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);
        int xx = (super.getWidth() - (int) this.FSplash.Width) / 2;
        int yy = 20;
        screen.drawImage(xx, yy, 0, 0, (int) this.FSplash.Width, (int) this.FSplash.Height, this.FSplash, 255);
    }

    public MenuWindow(BaseControl owner)
    {
        super(owner);

        this.FSplash = ResourceManager.loadImage(super.getMainWindow().getScreen(), "itf/Splash.tga", BaseScreen.clNone);

        int offset = 110;
        super.setWidth(204);
        super.setHeight(270 + offset);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        NWButton tRButton = new NWButton(this);
        tRButton.setLeft(20);
        tRButton.setTop(offset + 20);
        tRButton.setWidth(164);
        tRButton.setHeight(30);
        tRButton.setImageFile("itf/MenuBtn.tga");
        tRButton.OnClick = this::onBtnClose;
        tRButton.OnLangChange = GlobalVars.nwrWin::LangChange;
        tRButton.setLangResID(7);

        NWButton tRButton2 = new NWButton(this);
        tRButton2.setLeft(20);
        tRButton2.setTop(offset + 60);
        tRButton2.setWidth(164);
        tRButton2.setHeight(30);
        tRButton2.setImageFile("itf/MenuBtn.tga");
        tRButton2.OnClick = this::onBtnNew;
        tRButton2.OnLangChange = GlobalVars.nwrWin::LangChange;
        tRButton2.setLangResID(1);

        NWButton tRButton3 = new NWButton(this);
        tRButton3.setLeft(20);
        tRButton3.setTop(offset + 100);
        tRButton3.setWidth(164);
        tRButton3.setHeight(30);
        tRButton3.setImageFile("itf/MenuBtn.tga");
        tRButton3.OnClick = this::onBtnLoad;
        tRButton3.OnLangChange = GlobalVars.nwrWin::LangChange;
        tRButton3.setLangResID(2);

        NWButton tRButton4 = new NWButton(this);
        tRButton4.setLeft(20);
        tRButton4.setTop(offset + 140);
        tRButton4.setWidth(164);
        tRButton4.setHeight(30);
        tRButton4.setImageFile("itf/MenuBtn.tga");
        tRButton4.OnClick = this::onBtnSave;
        tRButton4.OnLangChange = GlobalVars.nwrWin::LangChange;
        tRButton4.setLangResID(3);

        NWButton tRButton5 = new NWButton(this);
        tRButton5.setLeft(20);
        tRButton5.setTop(offset + 180);
        tRButton5.setWidth(164);
        tRButton5.setHeight(30);
        tRButton5.setImageFile("itf/MenuBtn.tga");
        tRButton5.OnClick = this::onBtnOptions;
        tRButton5.OnLangChange = GlobalVars.nwrWin::LangChange;
        tRButton5.setLangResID(5);

        NWButton tRButton6 = new NWButton(this);
        tRButton6.setLeft(20);
        tRButton6.setTop(offset + 220);
        tRButton6.setWidth(164);
        tRButton6.setHeight(30);
        tRButton6.setImageFile("itf/MenuBtn.tga");
        tRButton6.OnClick = this::onBtnExit;
        tRButton6.OnLangChange = GlobalVars.nwrWin::LangChange;
        tRButton6.setLangResID(6);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.FSplash.dispose();
        }
        super.dispose(disposing);
    }
}
