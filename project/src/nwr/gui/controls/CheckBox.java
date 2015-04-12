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

import nwr.engine.ResourceManager;
import nwr.engine.BaseControl;
import nwr.engine.BaseImage;
import nwr.engine.MouseEventArgs;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class CheckBox extends BaseControl
{
    private boolean fChecked;
    private BaseImage FImage;

    public int Group;

    public final boolean getChecked()
    {
        return this.fChecked;
    }

    public final void setChecked(boolean Value)
    {
        if (this.fChecked != Value) {
            if (this.Group == 0) {
                this.fChecked = Value;
            } else {
                if (!this.fChecked) {
                    int cnt = 0;

                    int num = super.getOwner().getControls().getCount();
                    for (int i = 0; i < num; i++) {
                        BaseControl ctl = super.getOwner().getControls().get(i);
                        if (ctl instanceof CheckBox && !ctl.equals(this) && ((CheckBox) ctl).Group == this.Group) {
                            ((CheckBox) ctl).fChecked = false;
                            cnt++;
                        }
                    }

                    if (cnt > 0) {
                        this.fChecked = true;
                    }
                }
            }
        }
    }

    @Override
    protected void doMouseUpEvent(MouseEventArgs eventArgs)
    {
        if (eventArgs.Button == MouseButton.mbLeft) {
            this.setChecked(!this.fChecked);
            this.doClickEvent();
        }
        super.doMouseUpEvent(eventArgs);
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        if (super.Enabled) {
            if (this.fChecked) {
                screen.drawImage(0, 0, 0, 16, 15, 16, this.FImage, 255);
            } else {
                screen.drawImage(0, 0, 0, 0, 15, 16, this.FImage, 255);
            }
        } else {
            screen.drawImage(0, 0, 0, 32, 15, 16, this.FImage, 255);
        }

        screen.drawText(20, 0, super.getCaption(), 0);
    }

    public CheckBox(BaseControl owner)
    {
        super(owner);
        this.Group = 0;
        this.FImage = ResourceManager.loadImage(super.getMainWindow().getScreen(), "itf/Check.tga", BaseScreen.clNone);
        super.setHeight(20);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.FImage != null) {
                this.FImage.dispose();
                this.FImage = null;
            }
        }
        super.dispose(disposing);
    }
}
