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
    public delegate void IItemDrawEvent(object sender, BaseScreen screen, int itemIndex, ExtRect itemRect);
    public delegate void IItemSelectEvent(object sender, MouseButton button, LBItem item);

    public sealed class LBOptions : ZRLib.Core.FlagSet
    {
        public const int lboChecks = 0;
        public const int lboIcons = 1;
        public const int lboTextTop = 2;
        public const int lboRadioChecks = 3;
        public const int lboDoubleItem = 4;

        public LBOptions(params int[] args)
            : base(args)
        {
        }
    }

    public class ListBox : BaseControl
    {
        public const sbyte MODE_LIST = 0;
        public const sbyte MODE_REPORT = 1;
        public const sbyte MODE_ICONS = 2;

        private readonly LBColumnTitles fColumnTitles;
        private readonly LBItemList fItems;

        private int fColumns;
        private int fColumnSize;
        private int fColumnWidth;
        private int fItemHeight;
        private sbyte fMode;
        private int fSelIndex;
        private int fTopIndex;
        private int fVisibleItems;
        protected int fIconHeight;
        protected int fIconWidth;

        public int CheckHeight;
        public int CheckWidth;
        public int LevelOffset;
        public LBOptions Options;
        public ScrollBar ScrollBar;
        public int SelColor;
        public int SelBorderColor;
        public BaseImage CheckImage;
        public ImageList ImagesList;
        public BaseImage TVBtn;

        public IItemSelectEvent OnItemSelect;
        public IItemDrawEvent OnItemDraw;

        public int Columns
        {
            get {
                return fColumns;
            }
            set {
                if (fColumns != value) {
                    fColumns = value;
                    Reset(false);
                }
            }
        }

        public LBColumnTitles ColumnTitles
        {
            get {
                return fColumnTitles;
            }
        }

        public LBItemList Items
        {
            get {
                return fItems;
            }
        }

        public sbyte Mode
        {
            get {
                return fMode;
            }
            set {
                if (fMode != value) {
                    fMode = value;
    
                    if (fMode == MODE_ICONS) {
                        if (fIconWidth == 0) {
                            Columns = 1;
                        } else {
                            Columns = (Width - ScrollBar.Width) / fIconWidth;
                        }
                    }
                    Reset(false);
                }
            }
        }

        public int SelIndex
        {
            get {
                if (fSelIndex >= 0 && fSelIndex < fItems.Count) {
                    return fSelIndex;
                } else {
                    return -1;
                }
            }
            set {
                int lb;
                int hb;
                int idx;
    
                if (value < 0 || value >= fItems.Count) {
                    return;
                }
                fSelIndex = value;
                int col;
                if (fMode == MODE_REPORT) {
                    col = 0;
                } else {
                    col = fSelIndex / fColumnSize;
                }
                lb = fTopIndex + col * fColumnSize;
                hb = lb + (fVisibleItems - 1);
                idx = fSelIndex;
    
                if (idx > hb) {
                    TopIndex = fTopIndex + (idx - hb);
                } else {
                    if (idx < lb) {
                        TopIndex = fTopIndex + (idx - lb);
                    }
                }
            }
        }

        public int TopIndex
        {
            get {
                return fTopIndex;
            }
            set {
                if (fTopIndex != value && value >= 0) {
                    fTopIndex = value;
                    if (ScrollBar != null) {
                        ScrollBar.Pos = value;
                    }
                }
            }
        }

        public int IconHeight
        {
            get {
                return fIconHeight;
            }
            set {
                if (fIconHeight != value) {
                    fIconHeight = value;
                    Reset(false);
                }
            }
        }

        public int IconWidth
        {
            get {
                return fIconWidth;
            }
            set {
                if (fIconWidth != value) {
                    fIconWidth = value;
                    Reset(false);
                }
            }
        }

        private void ReindexList(LBItemList aList, ref int  aIndex)
        {
            int num = aList.Count;
            for (int i = 0; i < num; i++) {
                aIndex++;
                LBItem item = aList.GetItem(i);
                item.AbsoluteIndex = aIndex;
                item.VisibleIndex = aIndex;
                fColumnSize++;
            }
        }

        public bool GetChecked(int Index)
        {
            bool result = false;
            if (Index >= 0 && Index < fItems.Count) {
                result = fItems.GetItem(Index).Checked;
            }
            return result;
        }

        public void Reset(bool unset)
        {
            ExtRect r = base.IntRect;
            int ih = ItemHeight;
            if (ih == 0) {
                fVisibleItems = 1;
            } else {
                fVisibleItems = (r.Bottom - r.Top + 1) / ih;
            }
            fColumnWidth = r.Right - r.Left + 1;

            if (ScrollBar != null) {
                int idx = -1;
                ReindexList(fItems, ref idx);
                //idx = refIdx.argValue;

                fColumnWidth /= fColumns;
                fColumnSize = fVisibleItems;
                while (fColumnSize * fColumns < fItems.Count) {
                    fColumnSize++;
                }
                if (fSelIndex < 0 || fSelIndex >= fItems.Count) {
                    fSelIndex = 0;
                }

                if (fItems.Count == 0) {
                    ScrollBar.Max = 0;
                } else {
                    int idx1 = fColumnSize - fVisibleItems;
                    if (idx1 < 0) {
                        ScrollBar.Max = 0;
                    } else {
                        ScrollBar.Max = idx1;
                    }
                }

                if (unset) {
                    ScrollBar.Pos = 0;
                }
            }
        }

        public void SetChecked(int Index, bool Value)
        {
            if (Index >= 0 && Index < fItems.Count) {
                fItems.GetItem(Index).Checked = Value;
            }
        }


        public int ItemHeight
        {
            set {
                if (fItemHeight != value) {
                    fItemHeight = value;
                    Reset(false);
                }
            }
            get {
                int result;
                if (fIconHeight == 0) {
                    result = fItemHeight;
                } else {
                    if (fIconHeight > fItemHeight) {
                        result = fIconHeight;
                    } else {
                        result = fItemHeight;
                    }
                }
                if (Options.Contains(LBOptions.lboDoubleItem)) {
                    result = fItemHeight << 1;
                }
                return result;
            }
        }






        protected override void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            base.DoKeyDownEvent(eventArgs);

            if (eventArgs.Key == Keys.GK_UNK) {
                return;
            }

            int col;
            if (fColumnSize == 0) {
                col = 0;
            } else {
                col = fSelIndex / fColumnSize;
            }

            bool ok = true;
            LBItem item;

            switch (eventArgs.Key) {
                case Keys.GK_SPACE:
                    if (Options.Contains(LBOptions.lboChecks)) {
                        DoCheck(fSelIndex);
                    }
                    break;

                case Keys.GK_PRIOR:
                    if (fSelIndex - fVisibleItems < 0) {
                        SelIndex = 0;
                    } else {
                        SelIndex = fSelIndex - fVisibleItems;
                    }
                    break;

                case Keys.GK_NEXT:
                    if (fSelIndex + fVisibleItems > fItems.Count - 1) {
                        SelIndex = fItems.Count - 1;
                    } else {
                        SelIndex = fSelIndex + fVisibleItems;
                    }
                    break;

                case Keys.GK_RIGHT:
                    if (col < fColumns - 1) {
                        SelIndex = fSelIndex + fColumnSize;
                    } else {
                        SelIndex = fItems.Count - 1;
                    }
                    break;

                case Keys.GK_LEFT:
                    if (col > 0) {
                        SelIndex = fSelIndex - fColumnSize;
                    } else {
                        SelIndex = 0;
                    }
                    break;

                case Keys.GK_HOME:
                    SelIndex = 0;
                    break;

                case Keys.GK_END:
                    SelIndex = fItems.Count - 1;
                    break;

                case Keys.GK_UP:
                    SelIndex = fSelIndex - 1;
                    break;

                case Keys.GK_DOWN:
                    SelIndex = fSelIndex + 1;
                    break;

                default:
                    ok = false;
                    break;
            }

            item = fItems.GetItem(fSelIndex);

            if (ok) {
                DoSelectItem(this, MouseButton.mbLeft, 0, 0, item, GetItemRect(fSelIndex, -1));
            }
        }

        protected override void DoMouseWheelEvent(MouseWheelEventArgs eventArgs)
        {
            base.DoMouseWheelEvent(eventArgs);

            switch (eventArgs.WheelDelta) {
                case -1:
                    /*if (this.FTopIndex > 0) {
                        this.FTopIndex = this.FTopIndex - 1;
                    }*/
                    SelIndex = fSelIndex - 1;
                    //this.FTopIndex = 
                    break;

                case +1:
                    SelIndex = fSelIndex + 1;
                    break;
            }
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            if (eventArgs.Button == MouseButton.mbLeft || eventArgs.Button == MouseButton.mbRight) {
                int idx = GetItemByMouse(eventArgs.X, eventArgs.Y);
                if (idx > -1) {
                    SelIndex = idx;
                    DoSelectItem(this, eventArgs.Button, eventArgs.X, eventArgs.Y, fItems.GetItem(fSelIndex), GetItemRect(fSelIndex, -1));
                }

                base.DoMouseDownEvent(eventArgs);
            }
        }

        protected virtual void DoPaintItem(BaseScreen screen, LBItem item, int itemIndex, ExtRect itemRect)
        {
            if (fSelIndex == itemIndex) {
                screen.DrawRectangle(itemRect, SelColor, SelBorderColor);
            }

            int ih = ItemHeight;
            int tx = itemRect.Left + 2;
            int ty;

            if (Options.Contains(LBOptions.lboTextTop)) {
                ty = itemRect.Top + 1;
            } else {
                ty = itemRect.Top + (ih - Font.Height) / 2;
            }

            if (Options.Contains(LBOptions.lboChecks)) {
                int cd = (ih - CheckHeight) / 2;
                int cx = itemRect.Left + cd;
                int cy = itemRect.Top + cd;
                if (item.Checked) {
                    screen.DrawImage(cx, cy, 0, 16, 16, 17, CheckImage, 255);
                } else {
                    screen.DrawImage(cx, cy, 0, 0, 16, 17, CheckImage, 255);
                }
                tx = itemRect.Left + CheckWidth + (cd << 1);
            }

            if (Options.Contains(LBOptions.lboIcons)) {
                if (ImagesList != null && item.ImageIndex > -1) {
                    ImagesList.DrawImage(screen, tx, itemRect.Top + (ItemHeight - fIconHeight) / 2, item.ImageIndex, 255);
                }
                tx = tx + fIconWidth + 2;
            }

            if (fMode != MODE_ICONS) {
                Font.Color = item.Color;
                if (Options.Contains(LBOptions.lboDoubleItem)) {
                    StringList lst = new StringList();
                    lst.Text = item.Text;
                    screen.DrawText(tx, ty, lst[0], 0);
                    if (lst.Count > 1) {
                        screen.DrawText(tx, ty + CtlCommon.SmFont.Height, lst[1], 0);
                    }
                    lst.Dispose();
                } else {
                    screen.DrawText(tx, ty, item.Text, 0);
                }
            }

            if (OnItemDraw != null) {
                OnItemDraw.Invoke(this, screen, itemIndex, itemRect);
            }
        }

        protected virtual void DoPaintSubItem(BaseScreen screen, LBItem item, int itemIndex, int subIndex, ExtRect subRect)
        {
            if (fSelIndex == itemIndex) {
                screen.DrawRectangle(subRect, SelColor, SelBorderColor);
            }
            int tx = subRect.Left + 3;
            int ty = subRect.Top + (subRect.Bottom - subRect.Top - Font.Height) / 2;
            screen.DrawText(tx, ty, item.SubItems.GetItem(subIndex).Text, 0);
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            ExtRect crt = ClientRect;
            if ((ControlStyle.Contains(ControlStyles.сsOpaque))) {
                screen.FillRect(crt, BaseScreen.clBlack);
            }
            screen.SetTextColor(BaseScreen.clGold, true);
            CtlCommon.DrawCtlBorder(screen, crt);

            switch (fMode) {
                case MODE_LIST:
                case MODE_ICONS:
                    {
                        for (int col = 0; col < fColumns; col++) {
                            int lb = fTopIndex + col * fColumnSize;
                            int hb = lb + (fVisibleItems - 1);
                            if (hb >= fItems.Count) {
                                hb = fItems.Count - 1;
                            }
                            int defColor = screen.GetTextColor(true);

                            for (int i = lb; i <= hb; i++) {
                                LBItem item = fItems.GetItem(i);
                                DoPaintItem(screen, item, i, GetItemRect(i, -1));
                            }
                            screen.SetTextColor(defColor, true);
                        }
                    }
                    break;

                case MODE_REPORT:
                    {
                        int lb = fTopIndex;
                        int hb = lb + (fVisibleItems - 1);
                        if (hb >= fItems.Count) {
                            hb = fItems.Count - 1;
                        }

                        for (int i = lb; i <= hb; i++) {
                            LBItem item = fItems.GetItem(i);

                            int defColor = screen.GetTextColor(true);
                            DoPaintItem(screen, item, i, GetItemRect(i, -1));
                            screen.SetTextColor(defColor, true);

                            int num2 = item.SubItems.Count;
                            for (int j = 0; j < num2; j++) {
                                defColor = screen.GetTextColor(true);
                                DoPaintSubItem(screen, item, i, j, GetItemRect(i, j));
                                screen.SetTextColor(defColor, true);
                            }
                        }
                    }
                    break;
            }
        }

        protected override void DoResizeEvent()
        {
            base.DoResizeEvent();
            if (ScrollBar != null) {
                ScrollBar.Left = Width - ScrollBar.Width;
                ScrollBar.Height = Height;
            }
            Reset(false);
        }

        protected virtual void DoSelectItem(object Sender, MouseButton Button, int aX, int aY, LBItem Item, ExtRect aRect)
        {
            if (Options.Contains(LBOptions.lboChecks)) {
                int d = (aRect.Bottom - aRect.Top - CheckHeight) / 2;
                ExtRect checkRect = aRect;
                checkRect.Right = checkRect.Left + CheckWidth + (d << 1);
                if (checkRect.Contains(aX, aY) || Options.Contains(LBOptions.lboRadioChecks)) {
                    DoCheck(fSelIndex);
                }
            }
            if (OnItemSelect != null) {
                OnItemSelect.Invoke(this, Button, Item);
            }
        }


        public override ExtRect IntRect
        {
            get {
                ExtRect Result = base.IntRect;
                if (ScrollBar != null) {
                    Result.Right -= ScrollBar.Width;
                }
                return Result;
            }
        }

        protected ExtRect GetItemRect(int itemIndex, int subIndex)
        {
            ExtRect r = base.IntRect;
            int idx = itemIndex - fTopIndex;
            int row = idx % fColumnSize;
            int ax = r.Left;
            int ay = r.Top;

            ExtRect result = ExtRect.Empty;

            if (fMode != MODE_LIST) {
                if (fMode == MODE_REPORT) {
                    if (subIndex < 0) {
                        LBColumnTitle title = fColumnTitles.GetItem(0);
                        if (title == null) {
                            result = ExtRect.Create(ax, ay + row * ItemHeight, ax + (r.Right - r.Left + 1), ay + (row + 1) * ItemHeight);
                        } else {
                            result = ExtRect.Create(ax, ay + row * ItemHeight, ax + title.Width, ay + (row + 1) * ItemHeight);
                        }
                    } else {
                        int offset = 0;
                        int i = 0;
                        if (subIndex >= i) {
                            int num = subIndex + 1;
                            do {
                                LBColumnTitle title = fColumnTitles.GetItem(i);
                                offset += title.Width;
                                i++;
                            } while (i != num);
                        }
                        int w;
                        if (subIndex == fItems.GetItem(itemIndex).SubItems.Count - 1) {
                            w = r.Right - r.Left + 1 - offset;
                        } else {
                            LBColumnTitle title = fColumnTitles.GetItem(subIndex + 1);
                            if (title == null) {
                                w = 0;
                            } else {
                                w = title.Width;
                            }
                        }
                        result = ExtRect.Create(ax + offset, ay + row * ItemHeight, ax + offset + w, ay + (row + 1) * ItemHeight);
                    }
                    return result;
                }

                if (fMode != MODE_ICONS) {
                    return result;
                }
            }
            int col = idx / fColumnSize;
            result = ExtRect.Create(ax + col * fColumnWidth, ay + row * ItemHeight, ax + (col + 1) * fColumnWidth, ay + (row + 1) * ItemHeight);
            return result;
        }

        protected virtual void OnChange(object Sender)
        {
            Reset(false);
        }

        protected void OnScroll(object Sender)
        {
            fTopIndex = ScrollBar.Pos;
        }

        public ListBox(BaseControl owner)
            : base(owner)
        {

            fMode = MODE_LIST;
            fColumnTitles = new LBColumnTitles();
            fColumnTitles.OnChange = OnChange;
            fItems = new LBItemList(this, null);
            fItems.OnChange = OnChange;
            fColumns = 1;
            fTopIndex = 0;
            fSelIndex = 0;
            Options = new LBOptions();

            ScrollBar = new ScrollBar(this);
            ScrollBar.Min = 0;
            ScrollBar.Max = 100;
            ScrollBar.Pos = 0;
            ScrollBar.OnChange = OnScroll;
            ScrollBar.Left = Width - ScrollBar.Width;
            ScrollBar.Top = 0;
            ScrollBar.Height = 104;

            SelColor = BaseScreen.clGray;
            SelBorderColor = BaseScreen.clGray;
            Height = 100;
            Width = 200;
            ItemHeight = Font.Height + 2;
            BaseScreen scr = MainWindow.Screen;

            CheckImage = NWResourceManager.LoadImage(scr, "itf/Check.tga", BaseScreen.clNone);
            CheckHeight = (int)((int)CheckImage.Height / 3);
            CheckWidth = (int)CheckImage.Width;

            TVBtn = NWResourceManager.LoadImage(scr, "itf/TVBtn.tga", BaseScreen.clBlack);
            LevelOffset = (int)TVBtn.Width;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                if (CheckImage != null) {
                    CheckImage.Dispose();
                    CheckImage = null;
                }

                fItems.Dispose();
                fColumnTitles.Dispose();
            }
            base.Dispose(disposing);
        }

        private void DoCheck(int index)
        {
            if (Options.Contains(LBOptions.lboRadioChecks)) {
                int num = fItems.Count;
                for (int i = 0; i < num; i++) {
                    SetChecked(i, false);
                }
            }
            SetChecked(index, !GetChecked(index));
        }

        public LBItem GetAbsoluteItem(int index)
        {
            LBItem result = fItems.GetItem(index);
            return result;
        }

        public int GetItemByMouse(int aX, int aY)
        {
            int result = -1;

            ExtRect rCom = IntRect;
            int col = (aX - rCom.Left) / fColumnWidth;
            int row = (aY - rCom.Top) / ItemHeight;

            int idx = fTopIndex + col * fColumnSize + row;
            if (idx >= 0 && idx < fItems.Count) {
                result = idx;
            }

            return result;
        }
    }
}
