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

using System;
using BSLib;
using ZRLib.Engine;

namespace NWR.GUI.Controls
{
    public delegate void IMarqueeDoneEvent(object sender, ref bool  refRepeat);

    public sealed class Marquee : TextControl
    {
        private bool fActiveScroll;
        private float fSpeed;
        private long fStartTime;
        private int fY;

        public IMarqueeDoneEvent OnMarqueeDone;

        public Marquee(BaseControl owner)
            : base(owner)
        {
            fY = 0;
            fSpeed = 0.015f;
            Height = 100;
            Width = 200;
            fLineHeight = 20;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                if (Visible) {
                    DoHideEvent();
                }
            }
            base.Dispose(disposing);
        }

        public bool ActiveScroll
        {
            get {
                return fActiveScroll;
            }
            set {
                if (fActiveScroll != value) {
                    fActiveScroll = value;
                }
            }
        }


        private void DoDone(ref bool refRepeat)
        {
            refRepeat = true;
            if (OnMarqueeDone != null) {
                OnMarqueeDone.Invoke(this, ref refRepeat);
            }
        }

        protected override void DoHideEvent()
        {
            ActiveScroll = false;
            base.DoHideEvent();
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            ExtRect rt = ClientRect;
            if (ControlStyle.Contains(ControlStyles.сsOpaque)) {
                screen.FillRect(rt, BaseScreen.clBlack);
            }
            CtlCommon.DrawCtlBorder(screen, rt);

            rt = IntRect;
            ExtRect r2 = new ExtRect();
            r2.Left = rt.Left + Margin;
            r2.Top = rt.Top + Margin;
            r2.Right = rt.Right - Margin;
            r2.Bottom = rt.Bottom - Margin;
            int top = rt.Top - fLineHeight;
            int bot = rt.Bottom;
            rt.Left = r2.Left;
            rt.Right = r2.Right;

            screen.SetTextColor(TextColor, true);

            int num = fLines.Count;
            for (int i = 0; i < num; i++) {
                int yOffset = fY + i * fLineHeight;
                if (yOffset >= top && yOffset < bot) {
                    rt.Top = r2.Top + yOffset;
                    rt.Bottom = r2.Bottom + yOffset;
                    DoPaintLine(screen, i, rt);
                }
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            ActiveScroll = true;
        }

        public override void Update(long time)
        {
            base.Update(time);

            if (fActiveScroll) {
                bool first;
                if (fStartTime == 0) {
                    fStartTime = time;
                    first = true;
                } else {
                    first = false;
                }

                float y = (float)IntRect.Bottom - fSpeed * (time - fStartTime);
                if ((y + (fLines.Count * fLineHeight)) < (double)IntRect.Top) {
                    if (!first) {
                        bool repeat = true;
                        DoDone(ref repeat);
                        if (!repeat) {
                            ActiveScroll = false;
                        }
                    }
                    fStartTime = 0;
                }
                fY = (int)Math.Round(y);
            }
        }
    }
}
