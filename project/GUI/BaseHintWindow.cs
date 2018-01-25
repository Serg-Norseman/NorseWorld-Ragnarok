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
using ZRLib.Engine;

namespace NWR.GUI
{
    public class BaseHintWindow : BaseWindow
    {
        protected StringList fText;

        public int Margin;

        protected override void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            base.DoKeyDownEvent(eventArgs);
            if (eventArgs.Key == Keys.GK_ESCAPE) {
                Hide();
            }
        }

        public override string Caption
        {
            set {
                base.Caption = value;
    
                fText.Text = value;
                int max = 0;
    
                int num = fText.Count;
                for (int i = 0; i < num; i++) {
                    int w = Font.GetTextWidth(fText[i]);
                    if (max < w) {
                        max = w;
                    }
                }
    
                int h = Font.Height * fText.Count;
    
                Height = h + (Margin << 1);
                Width = max + (Margin << 1);
            }
        }

        public BaseHintWindow(BaseControl owner)
            : base(owner)
        {
            WindowStyle = new WindowStyles();
            fText = new StringList();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fText.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
