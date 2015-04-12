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

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class LBColumnTitles extends BaseObject
{
    private final ExtList<LBColumnTitle> fList;

    public INotifyEvent OnChange;
    public INotifyEvent OnChanging;

    public final int getCount()
    {
        return this.fList.getCount();
    }

    public final LBColumnTitle getItem(int index)
    {
        LBColumnTitle result = null;
        if (index >= 0 && index < this.fList.getCount()) {
            result = ((LBColumnTitle) this.fList.get(index));
        }
        return result;
    }

    protected void changed()
    {
        if (this.OnChange != null) {
            this.OnChange.invoke(this);
        }
    }

    protected void changing()
    {
        if (this.OnChanging != null) {
            this.OnChanging.invoke(this);
        }
    }

    public LBColumnTitles()
    {
        this.fList = new ExtList<>();
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

    public final int add(String text, int width)
    {
        LBColumnTitle ct = new LBColumnTitle();
        ct.Text = text;
        ct.Width = width;
        return this.fList.add(ct);
    }

    public final void clear()
    {
        this.changing();
        this.fList.clear();
        this.changed();
    }

    public final void delete(int index)
    {
        LBColumnTitle ct = this.getItem(index);
        if (ct != null) {
            this.fList.delete(index);
        }
    }
}
