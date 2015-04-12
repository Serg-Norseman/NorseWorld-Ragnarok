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
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.types.SysCreature;
import nwr.engine.BaseControl;
import nwr.engine.BaseImage;
import nwr.engine.BaseScreen;
import nwr.engine.KeyEventArgs;
import nwr.engine.Keys;
import nwr.engine.MouseEventArgs;
import nwr.engine.ResourceManager;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.EditBox;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class HeroWindow extends NWWindow
{
    private final EditBox fEditBox;
    private final BaseImage[] fImages;

    @Override
    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        Keys key = eventArgs.Key;

        if (key != Keys.GK_ESCAPE) {
            if (key == Keys.GK_F1) {
                GlobalVars.nwrGame.getPlayer().randomName();
                this.fEditBox.setText(GlobalVars.nwrGame.getPlayer().getName());
            }
        } else {
            eventArgs.Key = Keys.GK_UNK;
        }

        super.doKeyDownEvent(eventArgs);
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        super.doMouseDownEvent(eventArgs);
        try {
            for (int pa = SysCreature.sc_First; pa <= SysCreature.sc_Last; pa++) {
                SysCreature sc = SysCreature.forValue(pa);
                
                if (sc.scrRect.contains(eventArgs.X, eventArgs.Y)) {
                    this.hide();
                    GlobalVars.nwrGame.selectHero(sc.Sign, this.fEditBox.getText());
                    return;
                }
            }
        } catch (Exception ex) {
            Logger.write("HeroWindow.DoMouseDown(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);

        String s = Locale.getStr(RS.rs_AdventurerName);
        screen.setTextColor(BaseScreen.clGold, true);
        screen.drawText((super.getWidth() - CtlCommon.smFont.getTextWidth(s)) / 2, this.fEditBox.getTop() - CtlCommon.smFont.Height, s, 0);
        s = Locale.getStr(RS.rs_Apprenticeship);
        screen.Font = CtlCommon.bgFont;
        screen.setTextColor(BaseScreen.clGold, true);
        screen.drawText((super.getWidth() - CtlCommon.bgFont.getTextWidth(s)) / 2, 60, s, 0);
        screen.Font = CtlCommon.smFont;

        for (int pa = SysCreature.sc_First; pa <= SysCreature.sc_Last; pa++) {
            SysCreature sc = SysCreature.forValue(pa);
            
            s = Locale.getStr(sc.NameRS);
            Rect r = sc.scrRect;
            BaseImage img = this.fImages[pa];
            screen.drawImage(r.Left, r.Top, 0, 0, (int) img.Width, (int) img.Height, img, 255);
            screen.drawText(r.Left + (r.getWidth() - CtlCommon.smFont.getTextWidth(s)) / 2, r.Top - CtlCommon.smFont.Height, s, 0);
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        this.fEditBox.setText(GlobalVars.nwrGame.getPlayer().getName());
        super.setActiveControl(this.fEditBox);
    }

    public HeroWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(590);
        super.setHeight(430);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        this.fImages = new BaseImage[6];
        for (int pa = SysCreature.sc_First; pa <= SysCreature.sc_Last; pa++) {
            SysCreature sc = SysCreature.forValue(pa);

            this.fImages[pa] = ResourceManager.loadImage(super.getMainWindow().getScreen(), "itf/pa_" + sc.Sign + ".tga", BaseScreen.clNone);
        }

        this.fEditBox = new EditBox(this);
        this.fEditBox.setLeft((super.getWidth() - 200) / 2);
        this.fEditBox.setTop(35);
        this.fEditBox.setWidth(200);
        this.fEditBox.setVisible(true);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            for (int ap = SysCreature.sc_First; ap <= SysCreature.sc_Last; ap++) {
                this.fImages[ap].dispose();
            }
        }
        super.dispose(disposing);
    }
}
