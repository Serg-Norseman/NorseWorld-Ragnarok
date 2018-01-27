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

namespace NWR.GUI.Controls
{
    public sealed class LBItemList : BaseObject
    {
        private readonly ExtList<LBItem> fList;
        private readonly ListBox fOwner;
        private readonly LBItem fParentItem;
        private bool fSorted;
        private int fUpdateCount;

        public INotifyEvent OnChange;
        public INotifyEvent OnChanging;

        public LBItem ParentItem
        {
            get {
                return fParentItem;
            }
        }

        public int Count
        {
            get {
                return fList.Count;
            }
        }

        public LBItem GetItem(int index)
        {
            LBItem result = null;
            if (index >= 0 && index < fList.Count) {
                result = ((LBItem)fList[index]);
            }
            return result;
        }

        public bool Sorted
        {
            get {
                bool result;
                if (fParentItem == null) {
                    result = fSorted;
                } else {
                    result = fParentItem.List.Sorted;
                }
                return result;
            }
            set {
                if (fSorted != value) {
                    fSorted = value;
                    if (fSorted) {
                        Sort();
                    }
                }
            }
        }

        private int CompareText(int Index1, int Index2)
        {
            return string.Compare(GetItem(Index1).Text, GetItem(Index2).Text, true);
        }

        private void QuickSort(int L, int R)
        {
            int I;
            do {
                I = L;
                int J = R;
                int P = (int)((int)((uint)(L + R) >> 1));
                do {
                    while (CompareText(I, P) < 0) {
                        I++;
                    }
                    while (CompareText(J, P) > 0) {
                        J--;
                    }
                    if (I <= J) {
                        Exchange(I, J);
                        if (P == I) {
                            P = J;
                        } else {
                            if (P == J) {
                                P = I;
                            }
                        }
                        I++;
                        J--;
                    }
                } while (I <= J);
                if (L < J) {
                    QuickSort(L, J);
                }
                L = I;
            } while (I < R);
        }


        private void Changed()
        {
            if (fUpdateCount == 0 && OnChange != null) {
                OnChange.Invoke(this);
            }
        }

        private void Changing()
        {
            if (fUpdateCount == 0 && OnChanging != null) {
                OnChanging.Invoke(this);
            }
        }

        private bool UpdateState
        {
            set {
                if (value) {
                    Changing();
                } else {
                    Changed();
                }
            }
        }

        public LBItemList(ListBox owner, LBItem parentItem)
        {
            fList = new ExtList<LBItem>();
            fOwner = owner;
            fParentItem = parentItem;
            fSorted = false;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                OnChange = null;
                OnChanging = null;
                Clear();
                fList.Dispose();
            }
            base.Dispose(disposing);
        }

        public LBItem Add(string S, object aData)
        {
            Changing();

            int lev;
            if (fParentItem == null) {
                lev = 0;
            } else {
                lev = fParentItem.Level + 1;
            }

            LBItem Result = new LBItem(fOwner, lev);
            Result.Text = S;
            Result.Data = aData;
            Result.List = this;
            Result.ImageIndex = -1;
            fList.Add(Result);
            if (Sorted) {
                Sort();
            }
            fOwner.Reset(true);
            Changed();
            return Result;
        }

        public void BeginUpdate()
        {
            if (fUpdateCount == 0) {
                UpdateState = true;
            }
            fUpdateCount++;
        }

        public void EndUpdate()
        {
            fUpdateCount--;
            if (fUpdateCount == 0) {
                UpdateState = false;
            }
        }

        public void Clear()
        {
            Changing();
            for (int i = fList.Count - 1; i >= 0; i--) {
                ((LBItem)fList[i]).Dispose();
            }
            fList.Clear();
            Changed();
        }

        public void Delete(int Index)
        {
            Changing();
            LBItem item = GetItem(Index);
            if (item != null) {
                item.Dispose();
            }
            fList.Delete(Index);
            Changed();
        }

        public void Exchange(int Index1, int Index2)
        {
            Changing();
            fList.Exchange(Index1, Index2);
            Changed();
        }

        public int AbsoluteIndexOf(string S)
        {
            int num = fList.Count;
            for (int i = 0; i < num; i++) {
                LBItem item = fList[i];
                if (item.Text.CompareTo(S) == 0) {
                    return item.AbsoluteIndex;
                }
            }
            return -1;
        }

        public int IndexOf(string S)
        {
            int num = fList.Count;
            for (int i = 0; i < num; i++) {
                LBItem item = (LBItem)fList[i];
                if (item.Text.CompareTo(S) == 0) {
                    return i;
                }
            }
            return -1;
        }

        public int IndexOf(LBItem item)
        {
            int num = fList.Count;
            for (int i = 0; i < num; i++) {
                if (fList[i].Equals(item)) {
                    return i;
                }
            }
            return -1;
        }

        public LBItem ItemOf(string S)
        {
            LBItem result = null;

            int num = fList.Count;
            for (int i = 0; i < num; i++) {
                LBItem item = (LBItem)fList[i];

                if (item.Text.CompareTo(S) == 0) {
                    result = item;
                } else {
                    result = item.SubItems.ItemOf(S);
                }

                if (result != null) {
                    break;
                }
            }

            return result;
        }

        public void Sort()
        {
            if (Count != 0) {
                QuickSort(0, Count - 1);
            }
        }
    }

}