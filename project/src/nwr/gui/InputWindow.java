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
import nwr.gui.controls.EditBox;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class InputWindow extends NWWindow
{
    private final EditBox fEditBox;

    public IInputAcceptProc AcceptProc;

    private void onEditKeyDown(Object Sender, KeyEventArgs eventArgs)
    {
        if (eventArgs.Key == Keys.GK_RETURN) {
            if (this.AcceptProc != null) {
                this.AcceptProc.invoke(this.fEditBox.getText());
            }
            this.fEditBox.setText("");
            this.hide();
        }
    }

    private void onBtnClose(Object Sender)
    {
        this.hide();
    }

    private void onBtnAccept(Object Sender)
    {
        KeyEventArgs eventArgs = new KeyEventArgs();
        eventArgs.Key = Keys.GK_RETURN;
        this.onEditKeyDown(this, eventArgs);
    }

    public final String getValue()
    {
        return this.fEditBox.getText();
    }

    public final void setValue(String value)
    {
        this.fEditBox.setText(value);
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);
        screen.drawText(10, 10, super.getCaption(), 0);
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        super.setActiveControl(this.fEditBox);
    }

    public InputWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(480);
        super.setHeight(118);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        this.fEditBox = new EditBox(this);
        EditBox fEditBox = this.fEditBox;
        fEditBox.setLeft(10);
        fEditBox.setTop(10 + (CtlCommon.smFont.Height + 10));
        fEditBox.setWidth(super.getWidth() - 20);
        fEditBox.OnKeyDown = this::onEditKeyDown;

        NWButton CloseBtn = new NWButton(this);
        CloseBtn.setWidth(90);
        CloseBtn.setHeight(30);
        CloseBtn.setLeft(super.getWidth() - 90 - 10);
        CloseBtn.setTop(this.fEditBox.getTop() + this.fEditBox.getHeight() + 10);
        CloseBtn.setImageFile("itf/DlgBtn.tga");
        CloseBtn.OnClick = this::onBtnClose;
        CloseBtn.OnLangChange = GlobalVars.nwrWin::LangChange;
        CloseBtn.setLangResID(8);

        NWButton tRButton = new NWButton(this);
        tRButton.setWidth(90);
        tRButton.setHeight(30);
        tRButton.setLeft(CloseBtn.getLeft() - 90 - 10);
        tRButton.setTop(CloseBtn.getTop());
        tRButton.setImageFile("itf/DlgBtn.tga");
        tRButton.OnClick = this::onBtnAccept;
        tRButton.OnLangChange = GlobalVars.nwrWin::LangChange;
        tRButton.setLangResID(26);
    }
}
