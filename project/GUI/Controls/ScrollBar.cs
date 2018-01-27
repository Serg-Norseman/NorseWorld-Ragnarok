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

using System;
using BSLib;
using NWR.Game;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI.Controls
{
    public sealed class ScrollBar : BaseControl
    {
        public const sbyte SBK_HORIZONTAL = 0;
        public const sbyte SBK_VERTICAL = 1;

        private float fFactor;
        private sbyte fKind;
        private int fMax;
        private int fMin;
        private int fOldThumbPos;
        private int fOldThumbX;
        private int fOldThumbY;
        private int fPos;
        private BaseImage fThumb;
        private BaseImage fHBack;
        private BaseImage fHArrows;
        private BaseImage fVBack;
        private BaseImage fVArrows;

        public INotifyEvent OnChange;
        public int ArrowHeight;
        public int ArrowWidth;

        public sbyte Kind
        {
            get {
                return fKind;
            }
            set {
                fKind = value;
                Changed();
            }
        }

        public int Max
        {
            get {
                return fMax;
            }
            set {
                fMax = value;
                if (fPos > fMax) {
                    Pos = fMax;
                }
                Changed();
            }
        }

        public int Min
        {
            get {
                return fMin;
            }
            set {
                fMin = value;
                Changed();
            }
        }

        public int Pos
        {
            get {
                return fPos;
            }
            set {
                if (fPos != value) {
                    fPos = value;
                    if (OnChange != null) {
                        OnChange.Invoke(this);
                    }
                }
            }
        }

        public int ThumbHeight;
        public int ThumbWidth;


        private void Changed()
        {
            int size = fMax - fMin;
            if (size == 0) {
                fFactor = 1f;
            } else {
                if (fKind == SBK_HORIZONTAL) {
                    fFactor = (float)(((double)(Width - (ArrowWidth << 1) - ThumbWidth) / (double)size));
                } else {
                    fFactor = (float)(((double)(Height - (ArrowHeight << 1) - ThumbHeight) / (double)size));
                }
            }
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            if (eventArgs.Button == MouseButton.mbLeft) {
                int t = ThumbPos;
                if (fKind == SBK_VERTICAL) {
                    ExtRect rBeg = ExtRect.Create(0, 0, Width, ArrowHeight);
                    ExtRect rEnd = ExtRect.Create(0, Height - ArrowHeight, Width, Height);
                    ExtRect tt = ExtRect.Create(0, t, Width, t + ThumbHeight);
                    if (rBeg.Contains(eventArgs.X, eventArgs.Y) && fPos > fMin) {
                        Pos = Pos - 1;
                    } else {
                        if (rEnd.Contains(eventArgs.X, eventArgs.Y) && fPos < fMax) {
                            Pos = Pos + 1;
                        } else {
                            if (tt.Contains(eventArgs.X, eventArgs.Y)) {
                                fOldThumbY = eventArgs.Y;
                                fOldThumbPos = t;
                            }
                        }
                    }
                } else {
                    ExtRect rBeg = ExtRect.Create(0, 0, ArrowWidth, Height);
                    ExtRect rEnd = ExtRect.Create(Width - ArrowWidth, 0, Width, Height);
                    ExtRect tt = ExtRect.Create(t, 0, t + ThumbWidth, Height);
                    if (rBeg.Contains(eventArgs.X, eventArgs.Y) && fPos > fMin) {
                        Pos = Pos - 1;
                    } else {
                        if (rEnd.Contains(eventArgs.X, eventArgs.Y) && fPos < fMax) {
                            Pos = Pos + 1;
                        } else {
                            if (tt.Contains(eventArgs.X, eventArgs.Y)) {
                                fOldThumbX = eventArgs.X;
                                fOldThumbPos = t;
                            }
                        }
                    }
                }
            }
            base.DoMouseDownEvent(eventArgs);
        }

        protected override void DoMouseMoveEvent(MouseMoveEventArgs eventArgs)
        {
            if ((eventArgs.Shift.Contains(ShiftStates.SsLeft)) && fOldThumbPos > -1) {
                int delta;
                if (fKind == SBK_VERTICAL) {
                    delta = eventArgs.Y - fOldThumbY - ArrowHeight;
                } else {
                    delta = eventArgs.X - fOldThumbX - ArrowWidth;
                }
                int aPos = (int)((long)Math.Round((double)(fOldThumbPos + delta) / (double)fFactor));
                if (aPos < fMin) {
                    aPos = fMin;
                }
                if (aPos > fMax) {
                    aPos = fMax;
                }
                Pos = aPos;
            }
            base.DoMouseMoveEvent(eventArgs);
        }

        protected override void DoMouseUpEvent(MouseEventArgs eventArgs)
        {
            fOldThumbPos = -1;
            base.DoMouseUpEvent(eventArgs);
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            int tPos = ThumbPos;
            ExtRect crt = ClientRect;
            int AH = ArrowHeight;
            int AW = ArrowWidth;
            if (fKind == SBK_VERTICAL) {
                if (fVBack != null) {
                    int cnt = Height / (int)fVBack.Height + 1;

                    for (int i = 0; i < cnt; i++) {
                        int th = (int)fVBack.Height;
                        int t = crt.Top + i * (int)fVBack.Height;
                        if (t + th > crt.Top + Height) {
                            th = crt.Top + Height - t;
                        }
                        screen.DrawImage(crt.Left, t, 0, 0, (int)fVBack.Width, th, fVBack, 255);
                    }

                    if (tPos < 0) {
                        screen.DrawImage(crt.Left, crt.Top, 0, 34, AW, AH, fVArrows, 255);
                        screen.DrawImage(crt.Left, crt.Top + Height - AH, 0, 51, AW, Height, fVArrows, 255);
                    } else {
                        screen.DrawImage(crt.Left, crt.Top, 0, 0, AW, AH, fVArrows, 255);
                        screen.DrawImage(crt.Left, crt.Top + Height - AH, 0, AH, AW, AH, fVArrows, 255);
                        screen.DrawImage(crt.Left, crt.Top + tPos, 0, 0, (int)fThumb.Width, (int)fThumb.Height, fThumb, 255);
                    }
                }
            } else {
                if (fHBack != null) {
                    int cnt = Width / (int)fHBack.Width + 1;

                    for (int i = 0; i < cnt; i++) {
                        int tw = (int)fHBack.Width;
                        int t = crt.Left + i * (int)fHBack.Width;
                        if (t + tw > crt.Left + Width) {
                            tw = crt.Left + Width - t;
                        }
                        screen.DrawImage(t, crt.Top, 0, 0, tw, (int)fHBack.Height, fHBack, 255);
                    }

                    if (tPos < 0) {
                        screen.DrawImage(crt.Left, crt.Top, 34, 0, AW, AH, fHArrows, 255);
                        screen.DrawImage(crt.Left + Width - AW, crt.Top, 51, 0, AW, AH, fHArrows, 255);
                    } else {
                        screen.DrawImage(crt.Left, crt.Top, 0, 0, AW, AH, fHArrows, 255);
                        screen.DrawImage(crt.Left + Width - AW, crt.Top, AW, 0, AW, AH, fHArrows, 255);
                        screen.DrawImage(crt.Left + tPos, crt.Top, 0, 0, (int)fThumb.Width, (int)fThumb.Height, fThumb, 255);
                    }
                }
            }
        }

        protected override void DoResizeEvent()
        {
            Changed();
            base.DoResizeEvent();
            if (fKind == SBK_VERTICAL) {
                Width = 17;
            } else {
                Height = 17;
            }
        }

        private int ThumbPos
        {
            get {
                int result;
                if (fMax - fMin <= 0) {
                    result = -1;
                } else {
                    result = (int)((long)Math.Round(((double)fFactor * (double)fPos)));
                    if (fKind == SBK_HORIZONTAL) {
                        result += ArrowWidth;
                    } else {
                        result += ArrowHeight;
                    }
                }
                return result;
            }
        }

        public ScrollBar(BaseControl owner)
            : base(owner)
        {
            ControlStyle.Include(ControlStyles.сsCaptureMouse);
            fKind = SBK_VERTICAL;
            fOldThumbX = -1;
            fOldThumbY = -1;
            fOldThumbPos = -1;
            fMin = 0;
            fMax = 100;
            fPos = 0;
            ThumbHeight = 17;
            ThumbWidth = 17;
            ArrowHeight = 17;
            ArrowWidth = 17;
            BaseScreen scr = MainWindow.Screen;

            fThumb = NWResourceManager.LoadImage(scr, "itf/SBThumb.tga", Colors.None);
            fHBack = NWResourceManager.LoadImage(scr, "itf/SBHBack.tga", Colors.None);
            fHArrows = NWResourceManager.LoadImage(scr, "itf/SBHArrow.tga", Colors.None);
            fVBack = NWResourceManager.LoadImage(scr, "itf/SBVBack.tga", Colors.None);
            fVArrows = NWResourceManager.LoadImage(scr, "itf/SBVArrow.tga", Colors.None);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                if (fThumb != null) {
                    fThumb.Dispose();
                    fThumb = null;
                }

                if (fHBack != null) {
                    fHBack.Dispose();
                    fHBack = null;
                }

                if (fHArrows != null) {
                    fHArrows.Dispose();
                    fHArrows = null;
                }

                if (fVBack != null) {
                    fVBack.Dispose();
                    fVBack = null;
                }

                if (fVArrows != null) {
                    fVArrows.Dispose();
                    fVArrows = null;
                }
            }

            base.Dispose(disposing);
        }
    }
}
