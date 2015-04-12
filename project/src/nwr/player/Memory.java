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
package nwr.player;

import jzrlib.core.BaseEntity;
import jzrlib.core.FileVersion;
import jzrlib.core.SerializablesManager;
import jzrlib.utils.StreamUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Memory extends BaseEntity
{
    private final HashMap<String, MemoryEntry> fTable;

    public Memory(Object owner)
    {
        super(owner);
        this.fTable = new HashMap<>();
    }

    public final HashMap<String, MemoryEntry> getData()
    {
        return this.fTable;
    }

    public final MemoryEntry add(String sign, MemoryEntry data)
    {
        this.fTable.put(sign, data);
        return data;
    }

    public final void delete(String sign)
    {
        this.fTable.remove(sign);
    }

    public final MemoryEntry find(String sign)
    {
        MemoryEntry result = null;
        if (this.fTable.containsKey(sign) ? (result = this.fTable.get(sign)) == result : false) {
            return result;
        } else {
            return null;
        }
    }

    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.fTable.clear();

        int count = StreamUtils.readInt(stream);
        for (int i = 0; i < count; i++) {
            byte kind = (byte) StreamUtils.readByte(stream);
            MemoryEntry data = (MemoryEntry) SerializablesManager.createSerializable(kind, super.Owner);
            data.loadFromStream(stream, version);
            this.fTable.put(data.Sign, data);
        }
    }

    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        StreamUtils.writeInt(stream, fTable.size());

        for (Map.Entry<String, MemoryEntry> dicEntry : this.fTable.entrySet()) {
            MemoryEntry data = (MemoryEntry) dicEntry.getValue();
            byte kind = data.getSerializeKind();
            StreamUtils.writeByte(stream, kind);
            data.saveToStream(stream, version);
        }
    }
}
