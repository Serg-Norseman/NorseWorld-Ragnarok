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

using NWR.Game;
using ZRLib.Engine;

namespace NWR.GUI.Controls
{
    /// <summary>
    /// 
    /// </summary>
    public class CheckBox : BaseControl
    {
        private bool fChecked;
        private BaseImage fImage;

        public int Group;

        public bool Checked
        {
            get {
                return fChecked;
            }
            set {
                if (fChecked != value) {
                    if (Group == 0) {
                        fChecked = value;
                    } else {
                        if (!fChecked) {
                            int cnt = 0;
    
                            int num = Owner.Controls.Count;
                            for (int i = 0; i < num; i++) {
                                BaseControl ctl = Owner.Controls[i];
                                if (ctl is CheckBox && !ctl.Equals(this) && ((CheckBox)ctl).Group == Group) {
                                    ((CheckBox)ctl).fChecked = false;
                                    cnt++;
                                }
                            }
    
                            if (cnt > 0) {
                                fChecked = true;
                            }
                        }
                    }
                }
            }
        }

        protected override void DoMouseUpEvent(MouseEventArgs eventArgs)
        {
            if (eventArgs.Button == MouseButton.mbLeft) {
                Checked = !fChecked;
                DoClickEvent();
            }
            base.DoMouseUpEvent(eventArgs);
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            if (Enabled) {
                if (fChecked) {
                    screen.DrawImage(0, 0, 0, 16, 15, 16, fImage, 255);
                } else {
                    screen.DrawImage(0, 0, 0, 0, 15, 16, fImage, 255);
                }
            } else {
                screen.DrawImage(0, 0, 0, 32, 15, 16, fImage, 255);
            }

            screen.DrawText(20, 0, Caption, 0);
        }

        public CheckBox(BaseControl owner)
            : base(owner)
        {
            Group = 0;
            fImage = NWResourceManager.LoadImage(MainWindow.Screen, "itf/Check.tga", BaseScreen.clNone);
            Height = 20;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                if (fImage != null) {
                    fImage.Dispose();
                    fImage = null;
                }
            }
            base.Dispose(disposing);
        }
    }
}
