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
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class ProgressWindow : NWWindow
    {
        private readonly ProgressBar fBar;
        private readonly ExtRect fGaugeRect;

        public string StageLabel;

        public int Stage
        {
            get {
                return fBar.Pos;
            }
            set {
                if (fBar == null) {
                    return;
                }
    
                fBar.Pos = value;
            }
        }

        public int StageCount
        {
            get {
                return fBar.Max;
            }
            set {
                fBar.Max = value;
            }
        }



        protected override void DoPaintEvent(BaseScreen screen)
        {
            try {
                base.DoPaintEvent(screen);

                screen.SetTextColor(Colors.Gold, true);
                screen.DrawText(fGaugeRect.Left, fGaugeRect.Top - CtlCommon.SmFont.Height, StageLabel, 0);
            } catch (Exception ex) {
                Logger.Write("ProgressWindow.DoPaintTo(): " + ex.Message);
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            Stage = 0;
        }

        protected override void DoHideEvent()
        {
            base.DoHideEvent();
            Stage = 0;
        }

        public ProgressWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 320;
            Height = CtlCommon.SmFont.Height + 60;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            fGaugeRect = ExtRect.Create(20, 20 + CtlCommon.SmFont.Height, 300, 40 + CtlCommon.SmFont.Height);

            fBar = new ProgressBar(this);
            fBar.Bounds = fGaugeRect;
            StageCount = 100;
        }

        public void Step()
        {
            Stage = Stage + 1;
        }
    }
}
