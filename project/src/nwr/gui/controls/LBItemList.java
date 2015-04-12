/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
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
package nwr.gui.controls;

import jzrlib.core.BaseObject;
import jzrlib.core.ExtList;
import jzrlib.core.INotifyEvent;
import jzrlib.utils.TextUtils;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class LBItemList extends BaseObject
{
    private final ExtList<LBItem> fList;
    private final ListBox fOwner;
    private final LBItem fParentItem;
    private boolean fSorted;
    private int fUpdateCount;

    public INotifyEvent OnChange;
    public INotifyEvent OnChanging;

    public final LBItem getParentItem()
    {
        return this.fParentItem;
    }

    public final int getCount()
    {
        return this.fList.getCount();
    }

    public final LBItem getItem(int index)
    {
        LBItem result = null;
        if (index >= 0 && index < this.fList.getCount()) {
            result = ((LBItem) this.fList.get(index));
        }
        return result;
    }

    public final boolean getSorted()
    {
        boolean result;
        if (this.fParentItem == null) {
            result = this.fSorted;
        } else {
            result = this.fParentItem.List.getSorted();
        }
        return result;
    }

    private int compareText(int Index1, int Index2)
    {
        return TextUtils.compareText(this.getItem(Index1).Text, this.getItem(Index2).Text);
    }

    private void quickSort(int L, int R)
    {
        int I;
        do {
            I = L;
            int J = R;
            int P = (int) ((L + R) >>> 1);
            do {
                while (this.compareText(I, P) < 0) {
                    I++;
                }
                while (this.compareText(J, P) > 0) {
                    J--;
                }
                if (I <= J) {
                    this.exchange(I, J);
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
                this.quickSort(L, J);
            }
            L = I;
        } while (I < R);
    }

    public final void setSorted(boolean Value)
    {
        if (this.fSorted != Value) {
            this.fSorted = Value;
            if (this.fSorted) {
                this.sort();
            }
        }
    }

    protected void changed()
    {
        if (this.fUpdateCount == 0 && this.OnChange != null) {
            this.OnChange.invoke(this);
        }
    }

    protected void changing()
    {
        if (this.fUpdateCount == 0 && this.OnChanging != null) {
            this.OnChanging.invoke(this);
        }
    }

    protected void setUpdateState(boolean Updating)
    {
        if (Updating) {
            this.changing();
        } else {
            this.changed();
        }
    }

    public LBItemList(ListBox owner, LBItem parentItem)
    {
        this.fList = new ExtList<>();
        this.fOwner = owner;
        this.fParentItem = parentItem;
        this.fSorted = false;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.OnChange = null;
            this.OnChanging = null;
            this.clear();
            this.fList.dispose();
        }
        super.dispose(disposing);
    }

    public final LBItem add(String S, Object aData)
    {
        this.changing();

        int lev;
        if (this.fParentItem == null) {
            lev = 0;
        } else {
            lev = this.fParentItem.getLevel() + 1;
        }

        LBItem Result = new LBItem(this.fOwner, lev);
        Result.Text = S;
        Result.Data = aData;
        Result.List = this;
        Result.ImageIndex = -1;
        this.fList.add(Result);
        if (this.getSorted()) {
            this.sort();
        }
        this.fOwner.Reset(true);
        this.changed();
        return Result;
    }

    public final void beginUpdate()
    {
        if (this.fUpdateCount == 0) {
            this.setUpdateState(true);
        }
        this.fUpdateCount++;
    }

    public final void endUpdate()
    {
        this.fUpdateCount--;
        if (this.fUpdateCount == 0) {
            this.setUpdateState(false);
        }
    }

    public final void clear()
    {
        this.changing();
        for (int i = this.fList.getCount() - 1; i >= 0; i--) {
            ((LBItem) this.fList.get(i)).dispose();
        }
        this.fList.clear();
        this.changed();
    }

    public final void delete(int Index)
    {
        this.changing();
        LBItem item = this.getItem(Index);
        if (item != null) {
            item.dispose();
        }
        this.fList.delete(Index);
        this.changed();
    }

    public final void exchange(int Index1, int Index2)
    {
        this.changing();
        this.fList.exchange(Index1, Index2);
        this.changed();
    }

    public final int absoluteIndexOf(String S)
    {
        int num = this.fList.getCount();
        for (int i = 0; i < num; i++) {
            LBItem item = this.fList.get(i);
            if (item.Text.compareTo(S) == 0) {
                return item.AbsoluteIndex;
            }
        }
        return -1;
    }

    public final int indexOf(String S)
    {
        int num = this.fList.getCount();
        for (int i = 0; i < num; i++) {
            LBItem item = (LBItem) this.fList.get(i);
            if (item.Text.compareTo(S) == 0) {
                return i;
            }
        }
        return -1;
    }

    public final int indexOf(LBItem item)
    {
        int num = this.fList.getCount();
        for (int i = 0; i < num; i++) {
            if (this.fList.get(i).equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public final LBItem itemOf(String S)
    {
        LBItem result = null;

        int num = this.fList.getCount();
        for (int i = 0; i < num; i++) {
            LBItem item = (LBItem) this.fList.get(i);

            if (item.Text.compareTo(S) == 0) {
                result = item;
            } else {
                result = item.getSubItems().itemOf(S);
            }

            if (result != null) {
                break;
            }
        }

        return result;
    }

    public final void sort()
    {
        if (this.getCount() != 0) {
            this.quickSort(0, this.getCount() - 1);
        }
    }
}
