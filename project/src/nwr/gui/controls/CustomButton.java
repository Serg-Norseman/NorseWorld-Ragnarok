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
public class CustomButton extends BaseControl
{
    private BaseImage fGlyph;
    private BaseImage fImage;

    public boolean Down;

    private static void drawButton(BaseScreen screen, Rect rect, String text, boolean enabled, boolean down, boolean border)
    {
        if (border) {
            screen.drawRectangle(rect, BaseScreen.clWhite, BaseScreen.clBlack);
            /*int c = BaseScreen.clGray;
            int c2 = BaseScreen.clBlack;
            if (aDown) {
                screen.drawLine(aRect.Left + 1, aRect.Top + 1, aRect.Left + 1, aRect.Bottom, c2);
                screen.drawLine(aRect.Left + 1, aRect.Top + 1, aRect.Right, aRect.Top + 1, c2);
                screen.drawLine(aRect.Right - 1, aRect.Top + 2, aRect.Right - 1, aRect.Bottom, c);
                screen.drawLine(aRect.Left + 2, aRect.Bottom - 1, aRect.Right, aRect.Bottom - 1, c);
            } else {
                screen.drawLine(aRect.Left + 1, aRect.Top + 1, aRect.Left + 1, aRect.Bottom - 1, c);
                screen.drawLine(aRect.Left + 1, aRect.Top + 1, aRect.Right - 1, aRect.Top + 1, c);
                screen.drawLine(aRect.Right - 1, aRect.Top + 1, aRect.Right - 1, aRect.Bottom - 1, c2);
                screen.drawLine(aRect.Left + 1, aRect.Bottom - 1, aRect.Right, aRect.Bottom - 1, c2);
            }*/
        }

        int h = screen.getTextHeight("A");
        int w = screen.getTextWidth(text);
        int x = rect.Left + (rect.getWidth() - w) / 2;
        int y = rect.Top + (rect.getHeight() - h) / 2;
        screen.drawText(x, y, text, 0);
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        if (eventArgs.Button == MouseButton.mbLeft) {
            this.Down = true;
        }
        super.doMouseDownEvent(eventArgs);
    }

    @Override
    protected void doMouseUpEvent(MouseEventArgs eventArgs)
    {
        if (eventArgs.Button == MouseButton.mbLeft && this.Down) {
            this.Down = false;
            this.doClickEvent();
        }
        super.doMouseUpEvent(eventArgs);
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        Rect rt = this.getClientRect();

        if (this.fImage == null) {
            screen.setTextColor(BaseScreen.clBlack, true);
            drawButton(screen, rt, super.getCaption(), super.Enabled, this.Down, true);
        } else {
            if (super.Enabled) {
                if (this.Down) {
                    screen.drawImage(0, 0, 0, super.getHeight(), super.getWidth(), super.getHeight(), this.fImage, 255);
                } else {
                    screen.drawImage(0, 0, 0, 0, super.getWidth(), super.getHeight(), this.fImage, 255);
                }
                screen.setTextColor(BaseScreen.clGold, true);
            } else {
                screen.drawImage(0, 0, 0, super.getHeight() << 1, super.getWidth(), super.getHeight(), this.fImage, 255);
                screen.setTextColor(BaseScreen.clSilver, true);
            }

            drawButton(screen, rt, super.getCaption(), super.Enabled, this.Down, false);
        }

        if (this.fGlyph != null) {
            int xx = rt.Left + (rt.getWidth() - this.fGlyph.Width) / 2;
            int yy = rt.Top + (rt.getHeight() - this.fGlyph.Height) / 2;
            screen.drawImage(xx, yy, 0, 0, this.fGlyph.Width, this.fGlyph.Height, this.fGlyph, 255);
        }
    }

    public CustomButton(BaseControl owner)
    {
        super(owner);
        this.Down = false;
        this.fGlyph = null;
        this.fImage = null;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.fGlyph != null) {
                this.fGlyph.dispose();
                this.fGlyph = null;
            }

            if (this.fImage != null) {
                this.fImage.dispose();
                this.fImage = null;
            }
        }
        super.dispose(disposing);
    }

    public final void setGlyphFile(String fileName)
    {
        if (this.fGlyph != null) {
            this.fGlyph.dispose();
            this.fGlyph = null;
        }

        if (fileName.compareTo("") != 0) {
            this.fGlyph = ResourceManager.loadImage(super.getMainWindow().getScreen(), fileName, BaseScreen.clNone);
        }
    }

    public final void setImageFile(String fileName)
    {
        if (this.fImage != null) {
            this.fImage.dispose();
            this.fImage = null;
        }

        if (fileName.compareTo("") != 0) {
            this.fImage = ResourceManager.loadImage(super.getMainWindow().getScreen(), fileName, BaseScreen.clNone);
        }
    }
}
