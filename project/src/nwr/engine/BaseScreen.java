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
package nwr.engine;

import jzrlib.core.BaseObject;
import jzrlib.core.Rect;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class BaseScreen extends BaseObject
{
    public static final byte FILL_HORZ = 0;
    public static final byte FILL_VERT = 1;
    public static final byte FILL_TILE = 2;

    public static final int clNone = 536870911;
    public static final int clBlack = 0;
    public static final int clNavy = 8388608;
    public static final int clGreen = 32768;
    public static final int clTeal = 8421376;
    public static final int clMaroon = 128;
    public static final int clPurple = 8388736;
    public static final int clOlive = 32896;
    public static final int clSilver = 12632256;
    public static final int clGray = 8421504;
    public static final int clBlue = 16711680;
    public static final int clLime = 65280;
    public static final int clAqua = 16776960;
    public static final int clRed = 255;
    public static final int clFuchsia = 16711935;
    public static final int clYellow = 65535;
    public static final int clWhite = 16777215;
    public static final int clSkyBlue = 15780518;
    public static final int clGold = 11530239;
    public static final int clGoldenrod = 2139610;

    private Rect fOldClipRect;

    protected boolean fFull;
    protected int fOffsetX;
    protected int fOffsetY;
    protected Rect fClipRect;

    public Font Font;
    public int Height;
    public int Width;

    public BaseScreen(Object wnd, boolean fullScreen)
    {
        //this.WndHandle = aWndHandle;
        this.fFull = fullScreen;
        this.Font = null;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.freeData();
        }
        super.dispose(disposing);
    }

    protected void createData()
    {
    }

    protected void freeData()
    {
    }

    public abstract void beginPaint();

    public abstract void endPaint();

    public final void setOffset(int dx, int dy)
    {
        this.fOffsetX = dx;
        this.fOffsetY = dy;
    }

    public abstract void clear(int color);

    public abstract void drawImage(int dX, int dY, int sX, int sY, int sW, int sH, BaseImage image, int opacity);

    public void drawLine(int x1, int y1, int x2, int y2, int color)
    {
    }
    
    public abstract void drawRectangle(Rect rect, int fillColor, int borderColor);

    public abstract void fillRect(Rect rect, int color);

    public final void drawFilled(Rect rect, byte fillKind, int sx, int sy, int sw, int sh, int tx, int ty, BaseImage aTex)
    {
        if (aTex != null) {
            int L = rect.Left;
            int T = rect.Top;
            int R = rect.Right;
            int B = rect.Bottom;
            int W = R - L + 1;
            int H = B - T + 1;

            switch (fillKind) {
                case FILL_HORZ: {
                    int cnt_w = W / sw + 1;
                    for (int x = 0; x < cnt_w; x++) {
                        int sz_w = sw;
                        int tmx = L + x * sw;
                        if (tmx + sw > R) {
                            sz_w = R - tmx;
                        }
                        this.drawImage(tmx, ty, sx, sy, sz_w, sh, aTex, 255);
                    }
                }
                break;

                case FILL_VERT: {
                    int cnt_h = H / sh + 1;
                    for (int y = 0; y < cnt_h; y++) {
                        int sz_h = sh;
                        int tmy = T + y * sh;
                        if (tmy + sh > B) {
                            sz_h = B - tmy;
                        }
                        this.drawImage(tx, tmy, sx, sy, sw, sz_h, aTex, 255);
                    }
                }
                break;

                case FILL_TILE: {
                    int cnt_h = H / sh;
                    if (H % sh != 0) {
                        cnt_h++;
                    }
                    int cnt_w = W / sw;
                    if (W % sw != 0) {
                        cnt_w++;
                    }

                    for (int y = 0; y < cnt_h; y++) {
                        int sz_h = sh;
                        int tmy = T + y * sh;
                        if (tmy + sh > B) {
                            sz_h = B - tmy;
                        }

                        for (int x = 0; x < cnt_w; x++) {
                            int sz_w = sw;
                            int tmx = L + x * sw;
                            if (tmx + sw > R) {
                                sz_w = R - tmx;
                            }
                            this.drawImage(tmx, tmy, sx, sy, sz_w, sz_h, aTex, 255);
                        }
                    }
                }
                break;
            }
        }
    }

    public final Font createFont(String fileName)
    {
        return new Font(this, fileName);
    }

    public BaseImage createImage()
    {
        return new BaseImage(this);
    }

    public BaseImage createImage(String fileName, int transColor)
    {
        return new BaseImage(this, fileName, transColor);
    }

    public static int RGB(int r, int g, int b)
    {
        return (int) (r | g << 8 | b << 16);
    }

    public final void initClip(Rect clipRect)
    {
        this.fOldClipRect = this.fClipRect;
        this.fClipRect = clipRect;

        if (this.fClipRect.Left > this.fClipRect.Right) {
            int temp = this.fClipRect.Left;
            this.fClipRect.Left = this.fClipRect.Right;
            this.fClipRect.Right = temp;
        }
        if (this.fClipRect.Top > this.fClipRect.Bottom) {
            int temp = this.fClipRect.Top;
            this.fClipRect.Top = this.fClipRect.Bottom;
            this.fClipRect.Bottom = temp;
        }
        if (this.fClipRect.Right >= this.Width) {
            this.fClipRect.Right = this.Width - 1;
        }
        if (this.fClipRect.Left < 0) {
            this.fClipRect.Left = 0;
        }
        if (this.fClipRect.Bottom >= this.Height) {
            this.fClipRect.Bottom = this.Height - 1;
        }
        if (this.fClipRect.Top < 0) {
            this.fClipRect.Top = 0;
        }
    }

    public final void doneClip()
    {
        this.fClipRect = this.fOldClipRect;
    }

    public final void setSize(int width, int height)
    {
        this.Width = width;
        this.Height = height;
        this.fClipRect = new Rect(0, 0, this.Width, this.Height);
        this.freeData();
        this.createData();
    }

    public final int getTextColor(boolean foreground)
    {
        return (this.Font != null) ? this.Font.getColor() : BaseScreen.clBlack;
    }

    public final void setTextColor(int color, boolean foreground)
    {
        if (this.Font != null && foreground) {
            this.Font.setColor(color);
        }
    }

    public final int getTextHeight(String text)
    {
        return (this.Font != null) ? this.Font.Height : 0;
    }

    public final int getTextWidth(String text)
    {
        return (this.Font != null) ? this.Font.getTextWidth(text) : 0;
    }

    public final void drawText(int x, int y, String text, int format)
    {
        if (this.Font != null && text != null) {
            int sy = y + this.fOffsetY;
            Rect clipRect = this.fClipRect;

            if (sy + this.Font.Height > clipRect.Top && sy < clipRect.Bottom) {
                this.Font.drawText(this, x, y, text);
            }
        }
    }
}
