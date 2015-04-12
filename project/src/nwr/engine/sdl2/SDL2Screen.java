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
package nwr.engine.sdl2;

import nwr.binds.sdl2.SDL_Lib;
import nwr.binds.sdl2.SDL_PixelFormat;
import nwr.binds.sdl2.SDL_Rect;
import nwr.binds.sdl2.SDL_Renderer;
import nwr.binds.sdl2.SDL_Surface;
import nwr.binds.sdl2.SDL_Texture;
import nwr.binds.sdl2.SDL_Window;
import jzrlib.core.Rect;
import nwr.engine.BaseImage;
import nwr.engine.BaseScreen;
import org.bridj.Pointer;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class SDL2Screen extends BaseScreen
{
    private final Pointer<SDL_Window> fWindowPtr;
    private final Pointer<SDL_Surface> fSurfacePtr;
    protected Pointer<SDL_Renderer> fRenderPtr;
    protected Pointer<SDL_PixelFormat> fFormatPtr;

    // <editor-fold defaultstate="collapsed" desc="Runtime fields for optimization">

    private final Pointer<SDL_Rect> fSrcRect = Pointer.allocate(SDL_Rect.class);
    private final Pointer<SDL_Rect> fDstRect = Pointer.allocate(SDL_Rect.class);
    private final Pointer<SDL_Rect> fRect = Pointer.allocate(SDL_Rect.class);

    // </editor-fold>

    public SDL2Screen(Object wnd, boolean fullScreen)
    {
        super(wnd, fullScreen);
        this.fWindowPtr = (Pointer<SDL_Window>) wnd;

        this.fRenderPtr = SDL_Lib.SDL_CreateRenderer(this.fWindowPtr, -1, 0);
        this.fSurfacePtr = SDL_Lib.SDL_GetWindowSurface(this.fWindowPtr);
        this.fFormatPtr = this.fSurfacePtr.get().format();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.fRenderPtr != null) {
                SDL_Lib.SDL_DestroyRenderer(this.fRenderPtr);
            }
            if (this.fSurfacePtr != null) {
                SDL_Lib.SDL_FreeSurface(this.fSurfacePtr);
            }
        }
        super.dispose(disposing);
    }

    public static int convertColor(Pointer<SDL_PixelFormat> surfaceFormat, int color)
    {
        int r = (color & 0xff);
        int g = ((color >> 8) & 0xff);
        int b = ((color >> 16) & 0xff);
        return SDL_Lib.SDL_MapRGB(surfaceFormat, (byte) r, (byte) g, (byte) b);
    }

    @Override
    public BaseImage createImage()
    {
        return new SDL2Image(this);
    }

    @Override
    public BaseImage createImage(String fileName, int transColor)
    {
        return new SDL2Image(this, fileName, transColor);
    }

    @Override
    public void clear(int color)
    {
        if (this.fRenderPtr != null) {
            fRect.get().setBounds(0, 0, super.Width, super.Height);

            this.setDrawColor(color);
            SDL_Lib.SDL_RenderFillRect(this.fRenderPtr, fRect);
        }
    }

    @Override
    public void beginPaint()
    {
        if (this.fRenderPtr != null) {
            //SDL_Lib.SDL_SetRenderDrawColor(this.fRenderPtr, (byte) 100, (byte) 0, (byte) 0, (byte) 0);
            //SDL_Lib.SDL_RenderClear(this.fRenderPtr);
        }
    }

    @Override
    public void endPaint()
    {
        if (this.fRenderPtr != null) {
            SDL_Lib.SDL_RenderPresent(this.fRenderPtr);
        }
    }

    private void setDrawColor(int color)
    {
        int r = (color & 0xff);
        int g = ((color >> 8) & 0xff);
        int b = ((color >> 16) & 0xff);
        SDL_Lib.SDL_SetRenderDrawColor(this.fRenderPtr, (byte) r, (byte) g, (byte) b, (byte) 0);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int color)
    {
        if (this.fRenderPtr != Pointer.NULL) {
            this.setDrawColor(color);
            SDL_Lib.SDL_RenderDrawLine(this.fRenderPtr, 
                    this.fOffsetX + x1, this.fOffsetY + y1, this.fOffsetX + x2, this.fOffsetY + y2);
        }
    }

    @Override
    public void drawRectangle(Rect rect, int fillColor, int borderColor)
    {
        if (fillColor != BaseScreen.clNone) {
            this.fillRect(rect.clone(), fillColor);
        }

        if (borderColor != BaseScreen.clNone) {
            rect = rect.offset(this.fOffsetX, this.fOffsetY);
            fRect.get().setBounds(rect.Left, rect.Top, rect.getWidth(), rect.getHeight());

            this.setDrawColor(borderColor);
            SDL_Lib.SDL_RenderDrawRect(this.fRenderPtr, fRect);
        }
    }

    @Override
    public void fillRect(Rect rect, int fillColor)
    {
        if (fillColor != BaseScreen.clNone) {
            rect = rect.offset(this.fOffsetX, this.fOffsetY);
            fRect.get().setBounds(rect.Left, rect.Top, rect.getWidth(), rect.getHeight());

            this.setDrawColor(fillColor);
            SDL_Lib.SDL_RenderFillRect(this.fRenderPtr, fRect);
        }
    }

    @Override
    public void drawImage(int dX, int dY, int sX, int sY, int sW, int sH, BaseImage image, int opacity)
    {
        if (image != null) {
            dX += this.fOffsetX;
            dY += this.fOffsetY;
            
            if (dX >= this.fClipRect.Right || dY >= this.fClipRect.Bottom
                    || dX + sW <= this.fClipRect.Left || dY + sH <= this.fClipRect.Top) {
                return;
            }
            
            sX = Math.abs(sX);

            if (dX < this.fClipRect.Left) {
                int delta = this.fClipRect.Left - dX;
                sW -= delta;
                sX += delta;
                dX = this.fClipRect.Left;
            }
            if (dY < this.fClipRect.Top) {
                int delta = this.fClipRect.Top - dY;
                sH -= delta;
                sY += delta;
                dY = this.fClipRect.Top;
            }

            if (dX + sW > this.fClipRect.Right) {
                sW -= dX + sW - this.fClipRect.Right - 1;
            }
            if (dY + sH > this.fClipRect.Bottom) {
                sH -= dY + sH - this.fClipRect.Bottom - 1;
            }

            if (sW + sX > image.Width) {
                sW = image.Width - sX;
            }
            if (sH + sY > image.Height) {
                sH = image.Height - sY;
            }

            if (sH > 0 && sW > 0 && dX <= this.Width && dY <= this.Height && dX + image.Width >= 0 && dY + image.Height >= 0) {
                if (sW + dX > this.Width) {
                    sW = this.Width - dX;
                }
                if (sH + dY > this.Height) {
                    sH = this.Height - dY;
                }
            }

            Pointer<SDL_Texture> imTexture = ((SDL2Image) image).fTexturePtr;
            if (imTexture != null) {
                fSrcRect.get().setBounds(sX, sY, sW, sH);
                fDstRect.get().setBounds(dX, dY, sW, sH);

                if (opacity == 255) {
                    SDL_Lib.SDL_SetTextureAlphaMod(imTexture, (byte) opacity);
                } else {
                    SDL_Lib.SDL_SetTextureAlphaMod(imTexture, (byte) opacity);
                }

                SDL_Lib.SDL_RenderCopy(this.fRenderPtr, imTexture, fSrcRect, fDstRect);
            }
        }
    }
}
