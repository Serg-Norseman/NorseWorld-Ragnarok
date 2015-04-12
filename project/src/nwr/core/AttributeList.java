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
import jzrlib.core.BaseObject;
import jzrlib.core.ExtList;
import jzrlib.core.FileVersion;
import jzrlib.core.ListException;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.Logger;
import jzrlib.utils.StreamUtils;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class AttributeList extends BaseObject
{
    // List Assign Operators
    public static final byte lao_Copy = 0;
    public static final byte lao_And = 1;
    public static final byte lao_Or = 2;

    private final ExtList<Attribute> fList;

    public AttributeList()
    {
        this.fList = new ExtList<>();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.clear();
            this.fList.dispose();
        }
        super.dispose(disposing);
    }

    public final int getCount()
    {
        return this.fList.getCount();
    }

    public final Attribute getItem(int index)
    {
        if (index >= 0 && index < this.fList.getCount()) {
            return (Attribute) this.fList.get(index);
        } else {
            return null;
        }
    }

    public final int getValue(int ID)
    {
        int idx = this.indexOf(ID);

        if (idx < 0) {
            return 0;
        } else {
            return ((Attribute) this.fList.get(idx)).aValue;
        }
    }

    public final void setValue(int ID, int Value)
    {
        int idx = this.indexOf(ID);
        if (idx < 0) {
            this.add(ID, Value);
        } else {
            ((Attribute) this.fList.get(idx)).aValue = Value;
        }
    }

    public final int add(int id, int value)
    {
        int idx = this.indexOf(id);
        if (idx >= 0) {
            Attribute item = (Attribute) this.fList.get(idx);
            item.aValue += value;
        } else {
            Attribute item = new Attribute(id, value);
            idx = this.fList.add(item);
        }
        return idx;
    }

    public final void assign(AttributeList source, byte operator)
    {
        switch (operator) {
            case AttributeList.lao_Copy: {
                this.clear();
                int count = source.getCount();
                for (int i = 0; i < count; i++) {
                    Attribute item = source.fList.get(i);
                    this.add(item.aID, item.aValue);
                }
            }
            break;

            case AttributeList.lao_And: {
                for (int i = this.getCount() - 1; i >= 0; i--) {
                    Attribute aItem = this.fList.get(i);
                    if (source.indexOf(aItem.aID) == -1) {
                        this.delete(i);
                    }
                }
            }
            break;

            case AttributeList.lao_Or: {
                int count = source.getCount();
                for (int i = 0; i < count; i++) {
                    Attribute aItem = source.fList.get(i);
                    if (this.indexOf(aItem.aID) == -1) {
                        this.add(aItem.aID, aItem.aValue);
                    }
                }
            }
            break;
        }
    }

    public final void clear()
    {
        /*int i = this.FList.Count - 1;
         if (i >= 0)
         {
         do
         {
         (this.FList[i] as TAttribute).Dispose();
         i--;
         }
         while (i != -1);
         }*/
        this.fList.clear();
    }

    public final void delete(int index)
    {
        if (index < 0 || index >= this.fList.getCount()) {
            throw new ListException("List index out of bounds (%d)", index);
        }
        //(this.FList[Index] as TAttribute).Dispose();
        this.fList.delete(index);
    }

    public final int indexOf(int id)
    {
        int result = 0;
        while (result < this.fList.getCount() && ((Attribute) this.fList.get(result)).aID != id) {
            result++;
        }
        if (result == this.fList.getCount()) {
            result = -1;
        }
        return result;
    }

    public final void remove(int id)
    {
        int i = this.indexOf(id);
        if (i >= 0) {
            this.delete(i);
        }
    }

    public final void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        try {
            this.clear();

            int count = StreamUtils.readInt(stream);
            for (int i = 0; i < count; i++) {
                int id = StreamUtils.readInt(stream);
                int val = StreamUtils.readInt(stream);
                this.add(id, val);
            }
        } catch (Exception ex) {
            Logger.write("AttributeList.LoadFromStream(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        try {
            int count = this.fList.getCount();

            StreamUtils.writeInt(stream, count);

            for (int i = 0; i < count; i++) {
                Attribute attr = this.fList.get(i);
                StreamUtils.writeInt(stream, attr.aID);
                StreamUtils.writeInt(stream, attr.aValue);
            }
        } catch (Exception ex) {
            Logger.write("AttributeList.SaveToStream(): " + ex.getMessage());
            throw ex;
        }
    }
}
