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
import nwr.engine.BaseImage;
import nwr.engine.BaseScreen;
import nwr.engine.Font;
import nwr.engine.ResourceManager;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class CtlCommon
{
    public static Font smFont;
    public static Font bgFont;
    public static Font symFont;

    public static BaseImage fCtlDecor;
    public static BaseImage fWinBack;
    public static BaseImage fWinDecor;

    public static void drawCtlBorder(BaseScreen screen, Rect rect)
    {
        drawCtlBorder(screen, rect, new CtlBorders(CtlBorders.cbLeft, CtlBorders.cbTop, CtlBorders.cbRight, CtlBorders.cbBottom));
    }

    public static void drawCtlBorder(BaseScreen screen, Rect rect, CtlBorders borders)
    {
        if (screen != null && CtlCommon.fCtlDecor != null) {
            int L = rect.Left;
            int T = rect.Top;
            int R = rect.Right;
            int B = rect.Bottom;

            if (borders.contains(CtlBorders.cbTop)) {
                screen.drawFilled(rect, BaseScreen.FILL_HORZ, 2, 0, 31, 2, L, T - 1, CtlCommon.fCtlDecor);
            }
            if (borders.contains(CtlBorders.cbBottom)) {
                screen.drawFilled(rect, BaseScreen.FILL_HORZ, 2, 0, 31, 2, L, B - 1, CtlCommon.fCtlDecor);
            }
            if (borders.contains(CtlBorders.cbLeft)) {
                screen.drawFilled(rect, BaseScreen.FILL_VERT, 0, 2, 2, 31, L - 1, T, CtlCommon.fCtlDecor);
            }
            if (borders.contains(CtlBorders.cbRight)) {
                screen.drawFilled(rect, BaseScreen.FILL_VERT, 0, 2, 2, 31, R - 1, T, CtlCommon.fCtlDecor);
            }
        }
    }

    public static void initCtlImages(BaseScreen screen)
    {
        CtlCommon.fCtlDecor = ResourceManager.loadImage(screen, "itf/ctl_decor.tga", BaseScreen.clWhite);
        CtlCommon.fWinBack = ResourceManager.loadImage(screen, "itf/back.tga", BaseScreen.clNone);
        CtlCommon.fWinDecor = ResourceManager.loadImage(screen, "itf/win_decor.tga", BaseScreen.clWhite);
    }

    public static void doneCtlImages()
    {
        if (CtlCommon.fCtlDecor != null) {
            CtlCommon.fCtlDecor.dispose();
            CtlCommon.fCtlDecor = null;
        }

        if (CtlCommon.fWinBack != null) {
            CtlCommon.fWinBack.dispose();
            CtlCommon.fWinBack = null;
        }

        if (CtlCommon.fWinDecor != null) {
            CtlCommon.fWinDecor.dispose();
            CtlCommon.fWinDecor = null;
        }
    }
}
