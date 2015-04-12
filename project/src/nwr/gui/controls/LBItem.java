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
import nwr.engine.BaseScreen;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class LBItem extends BaseObject
{
    private final int fLevel;
    private final ListBox fOwner;
    private final LBItem fParent;
    private LBItemList fSubItems;

    public int AbsoluteIndex;
    public boolean Checked;
    public int Color;
    public Object Data;
    public int ImageIndex;
    public LBItemList List;
    public String Text;
    public int VisibleIndex;

    public final int getLevel()
    {
        return this.fLevel;
    }

    public final LBItem getParent()
    {
        return this.fParent;
    }

    public final LBItemList getSubItems()
    {
        return this.fSubItems;
    }

    public LBItem(ListBox owner, int level)
    {
        this.Color = BaseScreen.clGold;
        this.Checked = false;
        this.Data = null;
        this.fLevel = (int) level;
        this.fOwner = owner;
        this.fParent = null;
        this.fSubItems = new LBItemList(this.fOwner, this);
        this.Text = "";
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fSubItems.dispose();
            this.fSubItems = null;
        }
        super.dispose(disposing);
    }
}
