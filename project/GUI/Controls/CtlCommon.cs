/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih.
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

using BSLib;
using NWR.Game;
using ZRLib.Engine;

namespace NWR.GUI.Controls
{
    public static class CtlCommon
    {
        public static Font SmFont;
        public static Font BgFont;
        public static Font SymFont;

        public static BaseImage CtlDecor;
        public static BaseImage WinBack;
        public static BaseImage WinDecor;

        public static void DrawCtlBorder(BaseScreen screen, ExtRect rect)
        {
            DrawCtlBorder(screen, rect, new CtlBorders(CtlBorders.cbLeft, CtlBorders.cbTop, CtlBorders.cbRight, CtlBorders.cbBottom));
        }

        public static void DrawCtlBorder(BaseScreen screen, ExtRect rect, CtlBorders borders)
        {
            if (screen != null && CtlDecor != null) {
                int L = rect.Left;
                int T = rect.Top;
                int R = rect.Right;
                int B = rect.Bottom;

                if (borders.Contains(CtlBorders.cbTop)) {
                    screen.DrawFilled(rect, BaseScreen.FILL_HORZ, 2, 0, 31, 2, L, T - 1, CtlDecor);
                }
                if (borders.Contains(CtlBorders.cbBottom)) {
                    screen.DrawFilled(rect, BaseScreen.FILL_HORZ, 2, 0, 31, 2, L, B - 1, CtlDecor);
                }
                if (borders.Contains(CtlBorders.cbLeft)) {
                    screen.DrawFilled(rect, BaseScreen.FILL_VERT, 0, 2, 2, 31, L - 1, T, CtlDecor);
                }
                if (borders.Contains(CtlBorders.cbRight)) {
                    screen.DrawFilled(rect, BaseScreen.FILL_VERT, 0, 2, 2, 31, R - 1, T, CtlDecor);
                }
            }
        }

        public static void InitCtlImages(BaseScreen screen)
        {
            CtlDecor = NWResourceManager.LoadImage(screen, "itf/ctl_decor.tga", Colors.White);
            WinBack = NWResourceManager.LoadImage(screen, "itf/back.tga", Colors.None);
            WinDecor = NWResourceManager.LoadImage(screen, "itf/win_decor.tga", Colors.White);
        }

        public static void DoneCtlImages()
        {
            if (CtlDecor != null) {
                CtlDecor.Dispose();
                CtlDecor = null;
            }

            if (WinBack != null) {
                WinBack.Dispose();
                WinBack = null;
            }

            if (WinDecor != null) {
                WinDecor.Dispose();
                WinDecor = null;
            }
        }
    }
}
