/*
 *  The Simple DirectMedia Layer library (SDL).
 *  This library is distributed under the terms of the GNU LGPL license:
 *  http://www.gnu.org/copyleft/lesser.html
 *
 *  Java translation.
 *  Copyright (C) 2014 by Serg V. Zhdanovskih (aka Alchemist).
 */
package nwr.binds.sdl2;

import jzrlib.utils.AuxUtils;
import nwr.engine.ResourceManager;
import org.bridj.BridJ;
import org.bridj.CRuntime;
import org.bridj.Pointer;
import org.bridj.ann.Library;
import org.bridj.ann.Runtime;

@Library("SDL2")
@Runtime(CRuntime.class)
public final class SDL_Lib
{
    static {
        String path = ResourceManager.getExecPath();
        int sys = AuxUtils.getSystemModel();
        if (sys == 32) {
            path += "/win-x86";
        } else if (sys == 64) {
            path += "/win-x64";
        }

        BridJ.addLibraryPath(path);
        BridJ.register();
    }

    public static final int SDL_INIT_TIMER  = 0x00000001;
    public static final int SDL_INIT_AUDIO  = 0x00000010;
    public static final int SDL_INIT_VIDEO  = 0x00000020;
    public static final int SDL_INIT_EVENTS = 0x00004000;

    public static final int SDL_WINDOWPOS_UNDEFINED = 0x1FFF0000;
    public static final int SDL_WINDOWPOS_CENTERED = 0x2FFF0000;

    public static final int SDL_QUIT = 0x100;
    public static final int SDL_WINDOWEVENT = 0x200;
    public static final int SDL_KEYDOWN = 0x300;
    public static final int SDL_KEYUP = 0x300 + 1;
    public static final int SDL_TEXTINPUT = 0x300 + 3;
    public static final int SDL_MOUSEMOTION = 0x400;
    public static final int SDL_MOUSEBUTTONDOWN = 0x400 + 1;
    public static final int SDL_MOUSEBUTTONUP = 0x400 + 2;
    public static final int SDL_MOUSEWHEEL = 0x400 + 3;

    public static final int SDL_WINDOWEVENT_NONE = 0;           /**< Never used */
    public static final int SDL_WINDOWEVENT_SHOWN = 1;          /**< Window has been shown */
    public static final int SDL_WINDOWEVENT_HIDDEN = 2;         /**< Window has been hidden */
    public static final int SDL_WINDOWEVENT_EXPOSED = 3;        /**< Window has been exposed and should be redrawn */
    public static final int SDL_WINDOWEVENT_MOVED = 4;          /**< Window has been moved to data1, data2 */
    public static final int SDL_WINDOWEVENT_RESIZED = 5;        /**< Window has been resized to data1xdata2 */
    public static final int SDL_WINDOWEVENT_SIZE_CHANGED = 6;   /**< The window size has changed, either as a result of an API call or through the system or user changing the window size. */
    public static final int SDL_WINDOWEVENT_MINIMIZED = 7;      /**< Window has been minimized */
    public static final int SDL_WINDOWEVENT_MAXIMIZED = 8;      /**< Window has been maximized */
    public static final int SDL_WINDOWEVENT_RESTORED = 9;       /**< Window has been restored to normal size and position */
    public static final int SDL_WINDOWEVENT_ENTER = 10;          /**< Window has gained mouse focus */
    public static final int SDL_WINDOWEVENT_LEAVE = 11;          /**< Window has lost mouse focus */
    public static final int SDL_WINDOWEVENT_FOCUS_GAINED = 12;   /**< Window has gained keyboard focus */
    public static final int SDL_WINDOWEVENT_FOCUS_LOST = 13;     /**< Window has lost keyboard focus */
    public static final int SDL_WINDOWEVENT_CLOSE = 14;           /**< The window manager requests that the window be closed */
    
    public static final int KMOD_NONE = 0x0000;
    public static final int KMOD_LSHIFT = 0x0001;
    public static final int KMOD_RSHIFT = 0x0002;
    public static final int KMOD_LCTRL = 0x0040;
    public static final int KMOD_RCTRL = 0x0080;
    public static final int KMOD_LALT = 0x0100;
    public static final int KMOD_RALT = 0x0200;

    public static final int KMOD_CTRL = (KMOD_LCTRL | KMOD_RCTRL);
    public static final int KMOD_SHIFT = (KMOD_LSHIFT | KMOD_RSHIFT);
    public static final int KMOD_ALT = (KMOD_LALT | KMOD_RALT);

    public static native int SDL_Init(int flags);
    public static native void SDL_Quit();

    public static native Pointer<SDL_Window> SDL_CreateWindow(Pointer<Byte> title, int x, int y, int w, int h, int flags);
    public static native void SDL_DestroyWindow(Pointer<SDL_Window> window);
    public static native Pointer<SDL_Surface> SDL_GetWindowSurface(Pointer<SDL_Window> window);
    public static native int SDL_ShowCursor(int toggle);
    public static native void SDL_SetWindowTitle(Pointer<SDL_Window> window, Pointer<Byte> title);
    public static native void SDL_StartTextInput();
    public static native int SDL_PollEvent(Pointer<SDL_Event> sdlEvent);

    public static native Pointer<?> SDL_RWFromMem(Pointer<?> mem, int size);
    public static native int SDL_SetColorKey(Pointer<SDL_Surface> surface, int flag, int key);
    public static native void SDL_FreeSurface(Pointer<SDL_Surface> surface);

    public static native int SDL_FillRect(Pointer<SDL_Surface> surface, Pointer<SDL_Rect> rect, int color);
    public static native void SDL_SetClipRect(Pointer<SDL_Surface> surface, Pointer<SDL_Rect> rect);
    public static native int SDL_UpperBlit(Pointer<SDL_Surface> src, Pointer<SDL_Rect> srcrect, Pointer<SDL_Surface> dst, Pointer<SDL_Rect> dstrect);
    public static native int SDL_MapRGB(Pointer<SDL_PixelFormat> format, byte r, byte g, byte b);

    public static native int SDL_SetTextureAlphaMod(Pointer<SDL_Texture> texture, byte alpha);
    public static native int SDL_SetSurfaceAlphaMod(Pointer<SDL_Surface> surface, byte alpha);
    public static native void SDL_DestroyTexture(Pointer<SDL_Texture> texture);
    public static native int SDL_SetPaletteColors(Pointer<SDL_Palette> palette, Pointer<SDL_Color> colors, int firstcolor, int ncolors);
    public static native Pointer<SDL_Surface> SDL_ConvertSurface(Pointer<SDL_Surface> src, Pointer<SDL_PixelFormat> fmt, int flags);
    public static native Pointer<SDL_Texture> SDL_CreateTextureFromSurface(Pointer<SDL_Renderer> renderer, Pointer<SDL_Surface> surface);
    public static native int SDL_SetTextureColorMod(Pointer<SDL_Texture> texture, byte r, byte g, byte b);

    public static native int SDL_RenderCopy(Pointer<SDL_Renderer> renderer, Pointer<SDL_Texture> texture, Pointer<SDL_Rect> srcrect, Pointer<SDL_Rect> dstrect);
    public static native Pointer<SDL_Renderer> SDL_CreateRenderer(Pointer<SDL_Window> window, int index, int flags);
    public static native void SDL_DestroyRenderer(Pointer<SDL_Renderer> renderer);
    public static native int SDL_SetRenderDrawColor(Pointer<SDL_Renderer> renderer, byte r, byte g, byte b, byte a);
    public static native int SDL_RenderClear(Pointer<SDL_Renderer> renderer);
    public static native void SDL_RenderPresent(Pointer<SDL_Renderer> renderer);
    public static native int SDL_RenderDrawLine(Pointer<SDL_Renderer> renderer, int x1, int y1, int x2, int y2);
    public static native int SDL_RenderDrawRect(Pointer<SDL_Renderer> renderer, Pointer<SDL_Rect> rect);
    public static native int SDL_RenderFillRect(Pointer<SDL_Renderer> renderer, Pointer<SDL_Rect> rect);

    @Library("SDL2_image")
    public static native Pointer<SDL_Surface> IMG_LoadTGA_RW(Pointer src);
}
