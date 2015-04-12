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
import nwr.engine.Keys;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.TextBox;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class MessageWindow extends NWWindow
{
    private final TextBox fTextBox;

    private void onBtnClose(Object sender)
    {
        this.hide();
    }

    public final String getText()
    {
        return this.fTextBox.getLines().getTextStr();
    }

    public final void setText(String value)
    {
        this.fTextBox.getLines().setTextStr(value);
    }

    private void onLinkClick(Object sender, String linkValue)
    {
        GlobalVars.nwrWin.showKnowledge(linkValue);
    }

    @Override
    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        super.doKeyDownEvent(eventArgs);
        if (eventArgs.Key == Keys.GK_RETURN) {
            this.hide();
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);

        screen.setTextColor(BaseScreen.clGold, true);
    }

    public MessageWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(320);
        super.setHeight(240);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        NWButton tRButton = new NWButton(this);
        tRButton.setLeft(210);
        tRButton.setTop(190);
        tRButton.setWidth(90);
        tRButton.setHeight(30);
        tRButton.setImageFile("itf/DlgBtn.tga");
        tRButton.OnClick = this::onBtnClose;
        tRButton.OnLangChange = GlobalVars.nwrWin::LangChange;
        tRButton.setLangResID(8);

        this.fTextBox = new TextBox(this);
        this.fTextBox.setLeft(20);
        this.fTextBox.setTop(20);
        this.fTextBox.setHeight(160);
        this.fTextBox.setWidth(280);
        this.fTextBox.setLinks(true);
        this.fTextBox.OnLinkClick = this::onLinkClick;
    }
}
