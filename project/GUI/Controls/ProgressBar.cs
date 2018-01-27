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
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI.Controls
{
    public sealed class ProgressBar : BaseControl
    {
        private int fPos;
        private int fPercent;

        public int Max;
        public int Min;
        public bool ShowPos;

        public int Pos
        {
            get {
                return fPos;
            }
            set {
                if (value < Min) {
                    fPos = Min;
                } else {
                    if (value > Max) {
                        fPos = Max;
                    } else {
                        if (value != fPos) {
                            fPos = value;
                        }
                    }
                }
                int percent;
    
                if (Max == 0) {
                    percent = 0;
                } else {
                    percent = (int)((long)Math.Round((double)(fPos * 100) / (double)Max));
                }
    
                if (fPercent != percent) {
                    BaseMainWindow mainWnd = MainWindow;
                    mainWnd.Repaint();
                }
                fPercent = percent;
            }
        }

        public static void DrawGauge(BaseScreen screen, ExtRect R, int cur, int max, int cBorder, int cUnready, int cReady)
        {
            try {
                if (cur < 0) {
                    cur = 0;
                }
                if (cur > max) {
                    if (max > 0) {
                        cur %= max;
                    } else {
                        cur = max;
                    }
                }

                int L = R.Width;
                float percent;

                if (max == 0) {
                    percent = 0f;
                } else {
                    percent = (((float)cur / (float)max));
                }

                int rw = (int)Math.Round((float)L * percent);

                if (rw > 0) {
                    screen.DrawRectangle(ExtRect.Create(R.Left, R.Top, R.Left + rw, R.Bottom), cReady, cBorder);
                }
                if (rw < L) {
                    screen.DrawRectangle(ExtRect.Create(R.Left + rw - 1, R.Top, R.Right, R.Bottom), cUnready, cBorder);
                }
            } catch (Exception ex) {
                Logger.Write("ProgressBar.drawGauge(): " + ex.Message);
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            ExtRect crt = ClientRect;
            CtlCommon.DrawCtlBorder(screen, crt);
            crt.Inflate(-1, -1);
            DrawGauge(screen, crt, Pos, Max, Colors.Black, Colors.Gray, Colors.Gold);
        }

        public override string Caption
        {
            get {
                return Convert.ToString(Pos);
            }
        }

        public ProgressBar(BaseControl owner)
            : base(owner)
        {
            Min = 0;
            Max = 100;
            ShowPos = true;
            fPercent = 0;
        }
    }
}
