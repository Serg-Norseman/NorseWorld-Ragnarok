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
package nwr.core;

import java.io.IOException;
import java.util.ArrayList;
import jzrlib.core.BaseObject;
import jzrlib.core.FileVersion;
import jzrlib.core.ListException;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.StreamUtils;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class IntList extends BaseObject
{
    private ArrayList<Integer> fList;

    public IntList()
    {
        this.fList = new ArrayList<>();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fList.clear();
            this.fList = null;
        }
        super.dispose(disposing);
    }

    public final int getCount()
    {
        return this.fList.size();
    }

    public final int get(int index)
    {
        if (index < 0 || index >= this.fList.size()) {
            throw new ListException("List index out of bounds (%d)", index);
        }
        return this.fList.get(index);
    }

    public final int add(int item)
    {
        int Result = this.fList.size();
        this.fList.add(item);
        return Result;
    }

    public final int indexOf(int item)
    {
        return this.fList.indexOf(item);
    }

    public final void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.fList.clear();

        int count = StreamUtils.readInt(stream);
        for (int i = 0; i < count; i++) {
            this.add(StreamUtils.readInt(stream));
        }
    }

    public final void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        int count = this.fList.size();
        StreamUtils.writeInt(stream, count);
        for (int i = 0; i < count; i++) {
            StreamUtils.writeInt(stream, this.fList.get(i));
        }
    }
}
