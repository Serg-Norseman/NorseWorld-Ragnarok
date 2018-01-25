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

using System.Text;
using BSLib;
using ZRLib.Engine;

namespace NWR.GUI.Controls
{
    public sealed class EditBox : BaseControl
    {
        private int fCaretPos;
        private string fText;

        protected override void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            switch (eventArgs.Key) {
                case Keys.GK_BACK:
                    if (fCaretPos > 0) {
                        fText = fText.Substring(0, fCaretPos - 1) + fText.Substring(fCaretPos);
                        fCaretPos--;
                    }
                    break;

                case Keys.GK_DELETE:
                    if (fCaretPos >= 0 && fCaretPos < fText.Length) {
                        fText = fText.Substring(0, fCaretPos) + fText.Substring(fCaretPos + 1);
                    }
                    break;

                case Keys.GK_RIGHT:
                    if (fCaretPos < fText.Length) {
                        fCaretPos++;
                    }
                    break;

                case Keys.GK_LEFT:
                    if (fCaretPos > 0) {
                        fCaretPos--;
                    }
                    break;
            }

            base.DoKeyDownEvent(eventArgs);
        }

        protected override void DoKeyPressEvent(KeyPressEventArgs eventArgs)
        {
            if (Font.IsValidChar(eventArgs.Key)) {
                fText = (new StringBuilder(fText)).Insert(fCaretPos, eventArgs.Key).ToString();
                fCaretPos++;
            }
            base.DoKeyPressEvent(eventArgs);
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            base.DoMouseDownEvent(eventArgs);

            eventArgs.X -= (Height - Font.Height) / 2;
            int sh = 0;
            string ln = "";

            int num = (fText != null) ? fText.Length : 0;
            for (int i = 1; i <= num; i++) {
                ln += fText[i - 1];

                int nw = Font.GetTextWidth(ln);
                if (eventArgs.X >= sh && eventArgs.X <= nw) {
                    fCaretPos = i;
                    break;
                }

                sh = nw;
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            ExtRect crt = ClientRect;
            if ((ControlStyle.Contains(ControlStyles.сsOpaque))) {
                screen.FillRect(crt, BaseScreen.clBlack);
            }
            CtlCommon.DrawCtlBorder(screen, crt);

            screen.SetTextColor(BaseScreen.clGold, true);

            int th = Font.Height;
            int mg = (Height - th) / 2;
            int x = crt.Left + mg;
            int y = crt.Top + mg;
            screen.DrawText(x, y, fText, 0);

            int tw;
            if (fCaretPos == 0) {
                tw = 0;
            } else {
                tw = screen.GetTextWidth(fText.Substring(0, fCaretPos)) + 1;
            }

            int cw;
            if (fCaretPos >= fText.Length) {
                cw = 8;
            } else {
                cw = screen.GetTextWidth("" + fText[fCaretPos]);
            }
            x += tw;
            y = y + th + 1;
            screen.DrawLine(x, y, x + cw, y, BaseScreen.clGold);

            //screen.drawText(x + tw, y + 3, "_", 0); // cursor
        }

        public override ExtRect IntRect
        {
            get {
                return ExtRect.Create(2, 2, Width - 3, Height - 3);
            }
        }

        public string Text
        {
            get {
                return fText;
            }
            set {
                if ((fText != value)) {
                    fText = value;
    
                    if ((fText == "")) {
                        fCaretPos = 0;
                    } else {
                        fCaretPos = fText.Length;
                    }
                }
            }
        }

        public EditBox(BaseControl owner)
            : base(owner)
        {
            Text = "";

            if (Font != null) {
                Height = Font.Height + 8;
            } else {
                Height = 20;
            }

            Width = 200;
        }
    }
}
