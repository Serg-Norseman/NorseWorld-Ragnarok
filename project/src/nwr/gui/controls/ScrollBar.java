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

import jzrlib.core.INotifyEvent;
import jzrlib.core.Rect;
import nwr.engine.ControlStyles;
import nwr.engine.MouseEventArgs;
import nwr.engine.MouseMoveEventArgs;
import nwr.engine.ResourceManager;
import nwr.engine.BaseControl;
import nwr.engine.BaseImage;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;
import nwr.engine.ShiftStates;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ScrollBar extends BaseControl
{
    public static final byte SBK_HORIZONTAL = 0;
    public static final byte SBK_VERTICAL = 1;

    private float FFactor;
    private byte fKind;
    private int FMax;
    private int FMin;
    private int FOldThumbPos;
    private int FOldThumbX;
    private int FOldThumbY;
    private int FPos;
    private BaseImage FThumb;
    private BaseImage FHBack;
    private BaseImage FHArrows;
    private BaseImage FVBack;
    private BaseImage FVArrows;

    public INotifyEvent OnChange;
    public int ArrowHeight;
    public int ArrowWidth;

    public final byte getKind()
    {
        return this.fKind;
    }

    public final int getMax()
    {
        return this.FMax;
    }

    public final int getMin()
    {
        return this.FMin;
    }

    public final int getPos()
    {
        return this.FPos;
    }

    public int ThumbHeight;
    public int ThumbWidth;

    public final void setMax(int Value)
    {
        this.FMax = Value;
        if (this.FPos > this.FMax) {
            this.setPos(this.FMax);
        }
        this.changed();
    }

    public final void setMin(int Value)
    {
        this.FMin = Value;
        this.changed();
    }

    public final void setPos(int Value)
    {
        if (this.FPos != Value) {
            this.FPos = Value;
            if (this.OnChange != null) {
                this.OnChange.invoke(this);
            }
        }
    }

    public final void setKind(byte Value)
    {
        this.fKind = Value;
        this.changed();
    }

    protected void changed()
    {
        int size = this.FMax - this.FMin;
        if (size == 0) {
            this.FFactor = 1f;
        } else {
            if (this.fKind == ScrollBar.SBK_HORIZONTAL) {
                this.FFactor = (float) (((double) (super.getWidth() - (this.ArrowWidth << 1) - this.ThumbWidth) / (double) size));
            } else {
                this.FFactor = (float) (((double) (super.getHeight() - (this.ArrowHeight << 1) - this.ThumbHeight) / (double) size));
            }
        }
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        if (eventArgs.Button == MouseButton.mbLeft) {
            int t = this.getThumbPos();
            if (this.fKind == ScrollBar.SBK_VERTICAL) {
                Rect rBeg = new Rect(0, 0, super.getWidth(), this.ArrowHeight);
                Rect rEnd = new Rect(0, super.getHeight() - this.ArrowHeight, super.getWidth(), super.getHeight());
                Rect tt = new Rect(0, t, super.getWidth(), t + this.ThumbHeight);
                if (rBeg.contains(eventArgs.X, eventArgs.Y) && this.FPos > this.FMin) {
                    this.setPos(this.getPos() - 1);
                } else {
                    if (rEnd.contains(eventArgs.X, eventArgs.Y) && this.FPos < this.FMax) {
                        this.setPos(this.getPos() + 1);
                    } else {
                        if (tt.contains(eventArgs.X, eventArgs.Y)) {
                            this.FOldThumbY = eventArgs.Y;
                            this.FOldThumbPos = t;
                        }
                    }
                }
            } else {
                Rect rBeg = new Rect(0, 0, this.ArrowWidth, super.getHeight());
                Rect rEnd = new Rect(super.getWidth() - this.ArrowWidth, 0, super.getWidth(), super.getHeight());
                Rect tt = new Rect(t, 0, t + this.ThumbWidth, super.getHeight());
                if (rBeg.contains(eventArgs.X, eventArgs.Y) && this.FPos > this.FMin) {
                    this.setPos(this.getPos() - 1);
                } else {
                    if (rEnd.contains(eventArgs.X, eventArgs.Y) && this.FPos < this.FMax) {
                        this.setPos(this.getPos() + 1);
                    } else {
                        if (tt.contains(eventArgs.X, eventArgs.Y)) {
                            this.FOldThumbX = eventArgs.X;
                            this.FOldThumbPos = t;
                        }
                    }
                }
            }
        }
        super.doMouseDownEvent(eventArgs);
    }

    @Override
    protected void doMouseMoveEvent(MouseMoveEventArgs eventArgs)
    {
        if ((eventArgs.Shift.contains(ShiftStates.ssLeft)) && this.FOldThumbPos > -1) {
            int delta;
            if (this.fKind == ScrollBar.SBK_VERTICAL) {
                delta = eventArgs.Y - this.FOldThumbY - this.ArrowHeight;
            } else {
                delta = eventArgs.X - this.FOldThumbX - this.ArrowWidth;
            }
            int aPos = (int) ((long) Math.round((double) (this.FOldThumbPos + delta) / (double) this.FFactor));
            if (aPos < this.FMin) {
                aPos = this.FMin;
            }
            if (aPos > this.FMax) {
                aPos = this.FMax;
            }
            this.setPos(aPos);
        }
        super.doMouseMoveEvent(eventArgs);
    }

    @Override
    protected void doMouseUpEvent(MouseEventArgs eventArgs)
    {
        this.FOldThumbPos = -1;
        super.doMouseUpEvent(eventArgs);
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        int tPos = this.getThumbPos();
        Rect crt = super.getClientRect();
        int AH = this.ArrowHeight;
        int AW = this.ArrowWidth;
        if (this.fKind == ScrollBar.SBK_VERTICAL) {
            if (this.FVBack != null) {
                int cnt = super.getHeight() / (int) this.FVBack.Height + 1;

                for (int i = 0; i < cnt; i++) {
                    int th = (int) this.FVBack.Height;
                    int t = crt.Top + i * (int) this.FVBack.Height;
                    if (t + th > crt.Top + super.getHeight()) {
                        th = crt.Top + super.getHeight() - t;
                    }
                    screen.drawImage(crt.Left, t, 0, 0, (int) this.FVBack.Width, th, this.FVBack, 255);
                }

                if (tPos < 0) {
                    screen.drawImage(crt.Left, crt.Top, 0, 34, AW, AH, this.FVArrows, 255);
                    screen.drawImage(crt.Left, crt.Top + super.getHeight() - AH, 0, 51, AW, super.getHeight(), this.FVArrows, 255);
                } else {
                    screen.drawImage(crt.Left, crt.Top, 0, 0, AW, AH, this.FVArrows, 255);
                    screen.drawImage(crt.Left, crt.Top + super.getHeight() - AH, 0, AH, AW, AH, this.FVArrows, 255);
                    screen.drawImage(crt.Left, crt.Top + tPos, 0, 0, (int) this.FThumb.Width, (int) this.FThumb.Height, this.FThumb, 255);
                }
            }
        } else {
            if (this.FHBack != null) {
                int cnt = super.getWidth() / (int) this.FHBack.Width + 1;

                for (int i = 0; i < cnt; i++) {
                    int tw = (int) this.FHBack.Width;
                    int t = crt.Left + i * (int) this.FHBack.Width;
                    if (t + tw > crt.Left + super.getWidth()) {
                        tw = crt.Left + super.getWidth() - t;
                    }
                    screen.drawImage(t, crt.Top, 0, 0, tw, (int) this.FHBack.Height, this.FHBack, 255);
                }

                if (tPos < 0) {
                    screen.drawImage(crt.Left, crt.Top, 34, 0, AW, AH, this.FHArrows, 255);
                    screen.drawImage(crt.Left + super.getWidth() - AW, crt.Top, 51, 0, AW, AH, this.FHArrows, 255);
                } else {
                    screen.drawImage(crt.Left, crt.Top, 0, 0, AW, AH, this.FHArrows, 255);
                    screen.drawImage(crt.Left + super.getWidth() - AW, crt.Top, AW, 0, AW, AH, this.FHArrows, 255);
                    screen.drawImage(crt.Left + tPos, crt.Top, 0, 0, (int) this.FThumb.Width, (int) this.FThumb.Height, this.FThumb, 255);
                }
            }
        }
    }

    @Override
    protected void doResizeEvent()
    {
        this.changed();
        super.doResizeEvent();
        if (this.fKind == ScrollBar.SBK_VERTICAL) {
            super.setWidth(17);
        } else {
            super.setHeight(17);
        }
    }

    protected final int getThumbPos()
    {
        int result;
        if (this.FMax - this.FMin <= 0) {
            result = -1;
        } else {
            result = (int) ((long) Math.round(((double) this.FFactor * (double) this.FPos)));
            if (this.fKind == ScrollBar.SBK_HORIZONTAL) {
                result += this.ArrowWidth;
            } else {
                result += this.ArrowHeight;
            }
        }
        return result;
    }

    public ScrollBar(BaseControl owner)
    {
        super(owner);
        super.ControlStyle.include(ControlStyles.csCaptureMouse);
        this.fKind = ScrollBar.SBK_VERTICAL;
        this.FOldThumbX = -1;
        this.FOldThumbY = -1;
        this.FOldThumbPos = -1;
        this.FMin = 0;
        this.FMax = 100;
        this.FPos = 0;
        this.ThumbHeight = 17;
        this.ThumbWidth = 17;
        this.ArrowHeight = 17;
        this.ArrowWidth = 17;
        BaseScreen scr = super.getMainWindow().getScreen();

        this.FThumb = ResourceManager.loadImage(scr, "itf/SBThumb.tga", BaseScreen.clNone);
        this.FHBack = ResourceManager.loadImage(scr, "itf/SBHBack.tga", BaseScreen.clNone);
        this.FHArrows = ResourceManager.loadImage(scr, "itf/SBHArrow.tga", BaseScreen.clNone);
        this.FVBack = ResourceManager.loadImage(scr, "itf/SBVBack.tga", BaseScreen.clNone);
        this.FVArrows = ResourceManager.loadImage(scr, "itf/SBVArrow.tga", BaseScreen.clNone);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.FThumb != null) {
                this.FThumb.dispose();
                this.FThumb = null;
            }

            if (this.FHBack != null) {
                this.FHBack.dispose();
                this.FHBack = null;
            }

            if (this.FHArrows != null) {
                this.FHArrows.dispose();
                this.FHArrows = null;
            }

            if (this.FVBack != null) {
                this.FVBack.dispose();
                this.FVBack = null;
            }

            if (this.FVArrows != null) {
                this.FVArrows.dispose();
                this.FVArrows = null;
            }
        }

        super.dispose(disposing);
    }
}
