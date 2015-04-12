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

import jzrlib.utils.AuxUtils;
import jzrlib.core.Rect;
import nwr.core.StaticData;
import nwr.core.types.RuneRec;
import nwr.database.InfoEntry;
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
public final class DivinationWindow extends NWWindow
{
    private final TextBox fTextBox;

    private int[] fRunes;

    private void doDivination()
    {
        this.fRunes = new int[3];
        
        this.fRunes[0] = AuxUtils.getRandom(StaticData.dbRunes.length);
        this.fRunes[1] = AuxUtils.getRandom(StaticData.dbRunes.length);
        this.fRunes[2] = AuxUtils.getRandom(StaticData.dbRunes.length);
    }

    private void onBtnClose(Object sender)
    {
        this.hide();
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        this.doDivination();
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

        Rect crt = super.getClientRect();
        Rect cr = new Rect();
        cr.Left = crt.Left + 10;
        cr.Right = crt.Right - 10;
        cr.Top = crt.Top + 10;
        cr.Bottom = cr.Top + 36 - 1;
        CtlCommon.drawCtlBorder(screen, cr);

        //int ww = (32 * 3) + (10 * 2);
        //cr.inflate(-2, -2);
        int offset = (cr.getWidth() /*- ww*/) / 3;

        int sy = cr.Top + (cr.getHeight() - screen.getTextHeight("A")) / 2;
        
        for (int i = 0; i < 3; i++) {
            int runeId = this.fRunes[i];
            RuneRec rune = StaticData.dbRunes[runeId];
            //GlobalVars.nwrWin.Resources.drawImage(screen, cr.Left + offset + (i * (32 + 10)), cr.Top + 3, rune.ImageIndex, 255);
            InfoEntry inf = (InfoEntry) GlobalVars.nwrBase.findEntryBySign(rune.Sign);

            int sx = cr.Left + (offset * i);
            GlobalVars.nwrWin.Resources.drawImage(screen, sx, cr.Top + 3, rune.ImageIndex, 255);

            sx = sx + 32 + 3;
            screen.drawText(sx, sy, inf.getName(), 0);
        }

        /*rt = new TRect(cr.Left + 2, cr.Top + 2, cr.Left + 2 + 32 - 1, cr.Top + 2 + 32 - 1);
        screen.fillRect(rt, TGEScreen.clWhite);
        
        screen.drawText(rt.Right + 10, cr.Top + (36 - screen.getTextHeight("A")) / 2, this.fCollocutor.getName(), 0);*/
    }

    public DivinationWindow(BaseControl owner)
    {
        super(owner);

        super.setFont(CtlCommon.smFont);
        super.setWidth(420); 
        super.setHeight(340); 
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        this.fTextBox = new TextBox(this);
        this.fTextBox.setLeft(20);
        this.fTextBox.setTop(50);
        this.fTextBox.setHeight(230); 
        this.fTextBox.setWidth(380);

        NWButton btnClose = new NWButton(this);
        btnClose.setLeft(310);
        btnClose.setTop(290);
        btnClose.setWidth(90);
        btnClose.setHeight(30);
        btnClose.setImageFile("itf/DlgBtn.tga");
        btnClose.OnClick = this::onBtnClose;
        btnClose.OnLangChange = GlobalVars.nwrWin::LangChange;
        btnClose.setLangResID(8);
    }
}
