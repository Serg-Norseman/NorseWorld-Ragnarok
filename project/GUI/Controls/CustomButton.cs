/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
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
    public class CustomButton : BaseControl
    {
        private BaseImage fGlyph;
        private BaseImage fImage;

        public bool Down;

        private static void DrawButton(BaseScreen screen, ExtRect rect, string text, bool enabled, bool down, bool border)
        {
            if (border) {
                screen.DrawRectangle(rect, BaseScreen.clWhite, BaseScreen.clBlack);
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

            int h = screen.GetTextHeight("A");
            int w = screen.GetTextWidth(text);
            int x = rect.Left + (rect.Width - w) / 2;
            int y = rect.Top + (rect.Height - h) / 2;
            screen.DrawText(x, y, text, 0);
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            if (eventArgs.Button == MouseButton.mbLeft) {
                Down = true;
            }
            base.DoMouseDownEvent(eventArgs);
        }

        protected override void DoMouseUpEvent(MouseEventArgs eventArgs)
        {
            if (eventArgs.Button == MouseButton.mbLeft && Down) {
                Down = false;
                DoClickEvent();
            }
            base.DoMouseUpEvent(eventArgs);
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            ExtRect rt = ClientRect;

            if (fImage == null) {
                screen.SetTextColor(BaseScreen.clBlack, true);
                DrawButton(screen, rt, Caption, Enabled, Down, true);
            } else {
                if (Enabled) {
                    if (Down) {
                        screen.DrawImage(0, 0, 0, Height, Width, Height, fImage, 255);
                    } else {
                        screen.DrawImage(0, 0, 0, 0, Width, Height, fImage, 255);
                    }
                    screen.SetTextColor(BaseScreen.clGold, true);
                } else {
                    screen.DrawImage(0, 0, 0, Height << 1, Width, Height, fImage, 255);
                    screen.SetTextColor(BaseScreen.clSilver, true);
                }

                DrawButton(screen, rt, Caption, Enabled, Down, false);
            }

            if (fGlyph != null) {
                int xx = rt.Left + (rt.Width - fGlyph.Width) / 2;
                int yy = rt.Top + (rt.Height - fGlyph.Height) / 2;
                screen.DrawImage(xx, yy, 0, 0, fGlyph.Width, fGlyph.Height, fGlyph, 255);
            }
        }

        public CustomButton(BaseControl owner)
            : base(owner)
        {
            Down = false;
            fGlyph = null;
            fImage = null;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                if (fGlyph != null) {
                    fGlyph.Dispose();
                    fGlyph = null;
                }

                if (fImage != null) {
                    fImage.Dispose();
                    fImage = null;
                }
            }
            base.Dispose(disposing);
        }

        public string GlyphFile
        {
            set {
                if (fGlyph != null) {
                    fGlyph.Dispose();
                    fGlyph = null;
                }
    
                if (value.CompareTo("") != 0) {
                    fGlyph = NWResourceManager.LoadImage(MainWindow.Screen, value, BaseScreen.clNone);
                }
            }
        }

        public string ImageFile
        {
            set {
                if (fImage != null) {
                    fImage.Dispose();
                    fImage = null;
                }
    
                if (value.CompareTo("") != 0) {
                    fImage = NWResourceManager.LoadImage(MainWindow.Screen, value, BaseScreen.clNone);
                }
            }
        }
    }
}
