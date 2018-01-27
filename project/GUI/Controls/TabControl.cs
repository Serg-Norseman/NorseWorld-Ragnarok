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
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI.Controls
{
    public sealed class TabControl : BaseControl
    {
        private int fTabIndex;

        public INotifyEvent OnChange;

        public TabControl(BaseControl owner)
            : base(owner)
        {
            fTabIndex = -1;
        }

        public int TabIndex
        {
            get {
                return fTabIndex;
            }
            set {
                if (fTabIndex != value) {
                    TabSheet page = GetPage(fTabIndex);
                    if (page != null) {
                        page.Visible = false;
                    }
                    ActiveControl = null;
                    fTabIndex = value;
                    page = GetPage(fTabIndex);
                    if (page != null) {
                        page.Visible = true;
                        ActiveControl = page;
                    }
                    Change();
                }
            }
        }

        public TabSheet ActivePage
        {
            get {
                TabSheet result = null;
                if (fTabIndex >= 0 && fTabIndex < Controls.Count) {
                    result = ((TabSheet)Controls[fTabIndex]);
                }
                return result;
            }
        }

        public int PagesCount
        {
            get {
                return Controls.Count;
            }
        }

        public TabSheet GetPage(int index)
        {
            TabSheet result = null;
            if (index >= 0 && index < Controls.Count) {
                result = ((TabSheet)Controls[index]);
            }
            return result;
        }

        private void ResizeSheet(TabSheet sheet)
        {
            int th = 8;
            if (Font != null) {
                th += Font.Height;
            }
            sheet.Top = th;
            sheet.Left = 0;
            sheet.Width = Width;
            sheet.Height = Height - th;
        }


        private void Change()
        {
            if (OnChange != null) {
                OnChange.Invoke(this);
            }
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            if (eventArgs.Button == MouseButton.mbLeft || eventArgs.Button == MouseButton.mbRight) {
                int num = PagesCount;
                for (int idx = 0; idx < num; idx++) {
                    if (GetTabRect(idx).Contains(eventArgs.X, eventArgs.Y)) {
                        TabIndex = idx;
                        break;
                    }
                }
            }

            base.DoMouseDownEvent(eventArgs);
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            ExtRect crt = ClientRect;
            int L = crt.Left;
            int T = crt.Top;
            int R = crt.Right;
            int B = crt.Bottom;
            int h = 8;
            if (Font != null) {
                h += Font.Height;
            }
            int mw = 0;

            ExtRect tsr;
            int num = PagesCount;
            for (int idx = 0; idx < num; idx++) {
                TabSheet page = GetPage(idx);
                int tw = screen.GetTextWidth(page.Caption) + 16;
                tsr = ExtRect.Create(L + mw, T + 0, L + mw + tw - 1, T + h - 1);
                mw += tw;

                CtlBorders brd;
                if (idx == TabIndex) {
                    brd = new CtlBorders(CtlBorders.cbLeft, CtlBorders.cbTop, CtlBorders.cbRight);
                } else {
                    brd = new CtlBorders(CtlBorders.cbLeft, CtlBorders.cbTop, CtlBorders.cbRight, CtlBorders.cbBottom);
                }
                CtlCommon.DrawCtlBorder(screen, tsr, brd);
                screen.DrawText(tsr.Left + 8, tsr.Top + 4, page.Caption, 0);
            }

            tsr = ExtRect.Create(L + mw, T + h - 2, R, B);
            screen.DrawFilled(tsr, BaseScreen.FILL_HORZ, 2, 0, 31, 2, mw, T + h - 2, CtlCommon.CtlDecor);
            tsr = ExtRect.Create(L, T + h, R, B);
            CtlCommon.DrawCtlBorder(screen, tsr, new CtlBorders(CtlBorders.cbLeft, CtlBorders.cbRight, CtlBorders.cbBottom));
        }

        protected override void DoResizeEvent()
        {
            base.DoResizeEvent();

            int num = Controls.Count;
            for (int i = 0; i < num; i++) {
                ResizeSheet(GetPage(i));
            }
        }

        public TabSheet AddPage(string caption)
        {
            TabSheet result = new TabSheet(this);
            result.Caption = caption;
            result.Visible = false;
            ResizeSheet(result);
            return result;
        }

        public ExtRect GetTabRect(int index)
        {
            int h = Font.Height + 8;
            int mw = 0;

            int num = PagesCount;
            for (int i = 0; i < num; i++) {
                int tw = Font.GetTextWidth(GetPage(i).Caption) + 16;
                ExtRect rt = ExtRect.Create(mw, 0, mw + tw - 1, h - 1);
                mw += tw;

                if (i == index) {
                    return rt;
                }
            }

            return ExtRect.Empty;
        }
    }

}