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

import jzrlib.external.BinaryInputStream;
import jzrlib.utils.Logger;
import nwr.binds.sdl2.SDL_Lib;
import nwr.binds.sdl2.SDL_PixelFormat;
import nwr.binds.sdl2.SDL_Surface;
import nwr.binds.sdl2.SDL_Texture;
import nwr.engine.BaseImage;
import nwr.engine.BaseScreen;
import org.bridj.Pointer;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class SDL2Image extends BaseImage
{
    protected Pointer<SDL_Surface> fSurfacePtr;
    protected Pointer<SDL_Texture> fTexturePtr;

    public SDL2Image(BaseScreen screen)
    {
        super(screen);
    }

    public SDL2Image(BaseScreen screen, String fileName, int transColor)
    {
        super(screen, fileName, transColor);
    }

    @Override
    protected void Done()
    {
        if (this.fSurfacePtr != null) {
            SDL_Lib.SDL_FreeSurface(this.fSurfacePtr);
            this.fSurfacePtr = null;
        }
        if (this.fTexturePtr != null) {
            SDL_Lib.SDL_DestroyTexture(this.fTexturePtr);
            this.fTexturePtr = null;
        }
        super.Done();
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, int transColor)
    {
        try {
            int num = stream.available();
            byte[] buffer = new byte[num];
            stream.read(buffer, 0, num);

            Pointer<Byte> mem = Pointer.pointerToBytes(buffer);

            Pointer<SDL_Surface> imHandle = SDL_Lib.IMG_LoadTGA_RW(SDL_Lib.SDL_RWFromMem(mem, num));
            if (imHandle != null) {
                this.fSurfacePtr = imHandle;
            }

            if (this.fSurfacePtr != null) {
                SDL_Surface srfStr = imHandle.get();
                this.Height = (short) srfStr.h();
                this.Width = (short) srfStr.w();
                this.TransColor = transColor;

                if (!this.PaletteMode) {
                    Pointer<SDL_PixelFormat> fmt = ((SDL2Screen) this.fScreen).fFormatPtr;
                    Pointer<SDL_Surface> srf = SDL_Lib.SDL_ConvertSurface(this.fSurfacePtr, fmt, 0);
                    SDL_Lib.SDL_FreeSurface(this.fSurfacePtr);
                    this.fSurfacePtr = srf;
                }

                if (transColor != BaseScreen.clNone) {
                    int tc = SDL2Screen.convertColor(this.fSurfacePtr.get().format(), transColor);
                    SDL_Lib.SDL_SetColorKey(this.fSurfacePtr, 4096, tc);
                }

                this.fTexturePtr = SDL_Lib.SDL_CreateTextureFromSurface(((SDL2Screen) this.fScreen).fRenderPtr, this.fSurfacePtr);
            }
        } catch (Exception ex) {
            Logger.write("SDLImage.loadFromStream(): " + ex.getMessage());
        }
    }

    @Override
    public void setTransDefault()
    {
    }

    @Override
    public void replaceColor(int index, int replacement)
    {
        // used only for fonts rendering, don't change!
        if (this.fTexturePtr != null) {
            byte r = (byte) (replacement & 0xff);
            byte g = (byte) ((replacement >> 8) & 0xff);
            byte b = (byte) ((replacement >> 16) & 0xff);
            SDL_Lib.SDL_SetTextureColorMod(this.fTexturePtr, r, g, b);
        }
    }
}
