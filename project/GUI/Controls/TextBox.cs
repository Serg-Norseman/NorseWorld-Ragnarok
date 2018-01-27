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
    public delegate void ILinkClickEvent(object sender, string linkValue);

    public class TextBox : TextControl
    {
        private const int LK_INDEP = 0;
        private const int LK_FIRST = 1;
        private const int LK_SECOND = 2;

        private class HyperLink
        {
            public int SymBeg;
            public int SymEnd;
            public int Row;
            public int X1;
            public int X2;
            public int Kind;
        }

        private bool fLinks;
        private readonly ExtList<HyperLink> fLinksList;
        private readonly ScrollBar fScrollBar;
        private int fTopIndex;

        public ILinkClickEvent OnLinkClick;

        public bool LastVisible;
        public int LinkColor;

        public TextBox(BaseControl owner)
            : base(owner)
        {
            fTopIndex = 0;

            fScrollBar = new ScrollBar(this);
            fScrollBar.Min = 0;
            fScrollBar.Max = 0;
            fScrollBar.Pos = 0;
            fScrollBar.OnChange = OnScroll;
            fScrollBar.Left = Width - fScrollBar.Width;
            fScrollBar.Top = 0;
            fScrollBar.Height = 104;

            fLinks = false;
            fLinksList = new ExtList<HyperLink>(true);

            Height = 100;
            Width = 200;
            LinkColor = Colors.SkyBlue;
            fLineHeight = 20;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fLinksList.Dispose();
            }
            base.Dispose(disposing);
        }

        public bool Links
        {
            get {
                return fLinks;
            }
            set {
                fLinks = value;
            }
        }

        public int PageSize
        {
            get {
                return fVisibleLines;
            }
        }

        public int TopIndex
        {
            get {
                return fTopIndex;
            }
            set {
                if (fTopIndex != value) {
                    fTopIndex = value;
                    fScrollBar.Pos = fTopIndex;
                }
            }
        }



        protected override void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            switch (eventArgs.Key) {
                case Keys.GK_UP:
                    if (fTopIndex > 0) {
                        TopIndex = TopIndex - 1;
                    }
                    break;

                case Keys.GK_DOWN:
                    if (fTopIndex < fLines.Count - 1 && fTopIndex + fVisibleLines < fLines.Count) {
                        TopIndex = TopIndex + 1;
                    }
                    break;

                case Keys.GK_HOME:
                    TopIndex = 0;
                    break;

                case Keys.GK_END:
                    {
                        int idx = fLines.Count - fVisibleLines;
                        if (idx >= 0) {
                            TopIndex = idx;
                        }
                        break;
                    }

                case Keys.GK_PRIOR:
                    if (fTopIndex - fVisibleLines < 0) {
                        TopIndex = 0;
                    } else {
                        TopIndex = TopIndex - fVisibleLines;
                    }
                    break;

                case Keys.GK_NEXT:
                    if (fTopIndex + fVisibleLines > fLines.Count - 1) {
                        int idx = fLines.Count - fVisibleLines;
                        if (idx >= 0) {
                            TopIndex = idx;
                        }
                    } else {
                        TopIndex = TopIndex + fVisibleLines;
                    }
                    break;
            }

            base.DoKeyDownEvent(eventArgs);
        }

        protected override void DoMouseWheelEvent(MouseWheelEventArgs eventArgs)
        {
            base.DoMouseWheelEvent(eventArgs);

            switch (eventArgs.WheelDelta) {
                case -1:
                    if (fTopIndex > 0) {
                        TopIndex = TopIndex - 1;
                    }
                    break;

                case +1:
                    if (fTopIndex < fLines.Count - 1 && fTopIndex + fVisibleLines < fLines.Count) {
                        TopIndex = TopIndex + 1;
                    }
                    break;
            }
        }

        private int FindFirstLink(int row)
        {
            int num = fLinksList.Count;
            for (int i = 0; i < num; i++) {
                HyperLink link = fLinksList[i];
                if (link.Row == row) {
                    return i;
                }
            }
            return -1;
        }

        protected override void DoPaintLine(BaseScreen screen, int index, ExtRect rect)
        {
            try {
                if (index < 0 || index >= fLines.Count) {
                    return;
                }

                int x = rect.Left;
                int y = rect.Top;
                string line = fLines[index];
                screen.SetTextColor(TextColor, true);

                if (!fLinks) {
                    screen.DrawText(x, y, line, 0);
                } else {
                    int lnkIndex = FindFirstLink(index);
                    int offset = x;

                    if (lnkIndex < 0) {
                        screen.DrawText(x, y, line, 0);
                    } else {
                        string chunk = "";
                        HyperLink curLink = null;

                        int i = 0;
                        while (i < line.Length) {
                            char chr = line[i];

                            if (chr != '@') {
                                chunk += chr;
                            } else {
                                if (curLink == null) {
                                    screen.SetTextColor(TextColor, true);
                                    screen.DrawText(offset, y, chunk, 0);

                                    int sw = screen.GetTextWidth(chunk);
                                    offset += sw;

                                    curLink = fLinksList[lnkIndex];
                                    curLink.X1 = offset;
                                } else {
                                    screen.SetTextColor(LinkColor, true);
                                    screen.DrawText(offset, y, chunk, 0);

                                    int sw = screen.GetTextWidth(chunk);
                                    offset += sw;

                                    curLink.X2 = offset;
                                    curLink = null;

                                    lnkIndex++;
                                }

                                chunk = "";
                            }

                            i++;
                        }

                        // end chunk
                        if (chunk.Length != 0) {
                            if (curLink == null) {
                                screen.SetTextColor(TextColor, true);
                                screen.DrawText(offset, y, chunk, 0);
                            } else {
                                screen.SetTextColor(LinkColor, true);
                                screen.DrawText(offset, y, chunk, 0);

                                int sw = screen.GetTextWidth(chunk);
                                offset += sw;

                                curLink.X2 = offset;
                                curLink = null;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("TextBox.DoPaintLine(): " + ex.Message);
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            try {
                if (Font == null) {
                    return;
                }

                ExtRect rt = ClientRect;
                if ((ControlStyle.Contains(ControlStyles.сsOpaque))) {
                    screen.FillRect(rt, Colors.Black);
                }
                CtlCommon.DrawCtlBorder(screen, rt);

                screen.SetTextColor(Colors.Gold, true);

                int lb = fTopIndex;
                int hb = fLines.Count - 1;
                if (hb - lb >= fVisibleLines) {
                    hb = lb + (fVisibleLines - 1);
                }

                rt = base.IntRect;
                ExtRect r2 = new ExtRect();
                r2.Left = rt.Left + Margin;
                r2.Right = rt.Right - Margin;
                int top = rt.Top + Margin;

                for (int i = lb; i <= hb; i++) {
                    r2.Top = top + (i - fTopIndex) * fLineHeight;
                    r2.Bottom = r2.Top + fLineHeight - Margin;
                    DoPaintLine(screen, i, r2);
                }
            } catch (Exception ex) {
                Logger.Write("TextBox.DoPaintTo(): " + ex.Message);
            }
        }

        public override ExtRect IntRect
        {
            get {
                ExtRect result = base.IntRect;
                if (fScrollBar != null) {
                    result.Right = result.Right - fScrollBar.Width - (Margin << 1);
                }
                return result;
            }
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            base.DoMouseDownEvent(eventArgs);

            ExtRect r = base.IntRect;
            int eX = eventArgs.X - r.Left;
            int eY = eventArgs.Y - r.Top;

            int row = fTopIndex + eY / fLineHeight;
            if (row >= 0 && row < fLines.Count) {
                int num = fLinksList.Count;
                for (int i = 0; i < num; i++) {
                    HyperLink link = fLinksList[i];

                    if (link.Row == row && (eX > link.X1 && eX < link.X2)) {
                        DoLinkClickEvent(link, i);
                        return;
                    }
                }
            }
        }

        private void DoLinkClickEvent(HyperLink link, int idx)
        {
            if (OnLinkClick != null) {
                string linkValue = ProcessLink(link, idx);
                OnLinkClick.Invoke(this, linkValue);
            }
        }

        private string ProcessLink(HyperLink link, int idx)
        {
            string id = "";

            string line;
            switch (link.Kind) {
                case LK_INDEP:
                    line = fLines[link.Row];
                    id = line.Substring(link.SymBeg + 1, link.SymEnd - (link.SymBeg + 1));
                    break;

                case LK_FIRST:
                    {
                        line = fLines[link.Row];
                        id = line.Substring(link.SymBeg + 1, link.SymEnd + 1 - (link.SymBeg + 1));

                        HyperLink nextLink = fLinksList[idx + 1];
                        line = fLines[nextLink.Row];
                        id = id + line.Substring(nextLink.SymBeg, nextLink.SymEnd - nextLink.SymBeg);
                        break;
                    }

                case LK_SECOND:
                    {
                        HyperLink prevLink = fLinksList[idx - 1];
                        line = fLines[prevLink.Row];
                        id = line.Substring(prevLink.SymBeg + 1, prevLink.SymEnd + 1 - (prevLink.SymBeg + 1));

                        line = fLines[link.Row];
                        id = id + line.Substring(link.SymBeg, link.SymEnd - link.SymBeg);
                        break;
                    }
            }

            return id;
        }

        protected override void PrepareText()
        {
            if (fLinks) {
                fLinksList.Clear();

                HyperLink curLink = null;
                for (int row = 0; row < fLines.Count; row++) {
                    string text = fLines[row];
                    if (text.Length < 1) {
                        continue;
                    }

                    int idx = 0;
                    while (idx < text.Length) {
                        char c = text[idx];
                        if (c == '@') {
                            if (curLink == null) {
                                curLink = new HyperLink();
                                curLink.Row = row;
                                curLink.Kind = LK_INDEP;
                                curLink.SymBeg = idx;
                                fLinksList.Add(curLink);
                            } else {
                                curLink.SymEnd = idx;
                                curLink = null;
                            }
                        }
                        idx++;
                    }

                    if (curLink != null) {
                        curLink.SymEnd = text.Length - 1;
                        curLink.Kind = LK_FIRST;

                        curLink = new HyperLink();
                        curLink.Row = row + 1;
                        curLink.Kind = LK_SECOND;
                        curLink.SymBeg = 0;
                        fLinksList.Add(curLink);
                    }
                }
            }
        }

        protected override void DoResizeEvent()
        {
            base.DoResizeEvent();
            if (fScrollBar != null) {
                fScrollBar.Left = Width - fScrollBar.Width;
                fScrollBar.Height = Height;
            }
        }

        protected override void OnChange(object Sender)
        {
            base.OnChange(Sender);

            if (fScrollBar != null) {
                if (fLines.Count == 0) {
                    fScrollBar.Max = 0;
                    fScrollBar.Pos = 0;
                } else {
                    fScrollBar.Max = fLines.Count - fVisibleLines;
                    fScrollBar.Pos = 0;
                }
            }

            if (LastVisible) {
                if (fLines.Count - PageSize < 0) {
                    TopIndex = 0;
                } else {
                    TopIndex = fLines.Count - PageSize;
                }
            }
        }

        protected void OnScroll(object Sender)
        {
            fTopIndex = fScrollBar.Pos;
        }

        public virtual bool IsValidLink(string linkValue)
        {
            return true;
        }
    }
}
