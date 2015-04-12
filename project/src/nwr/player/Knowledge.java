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

import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.core.FileVersion;
import jzrlib.utils.StreamUtils;
import nwr.core.StaticData;
import nwr.main.GlobalVars;
import java.io.IOException;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class Knowledge extends MemoryEntry
{
    public int ID;
    public int RefsCount;

    public Knowledge(Object owner)
    {
        super(owner);
    }

    @Override
    public String getDesc()
    {
        return GlobalVars.nwrBase.getEntry(this.ID).getDesc();
    }

    @Override
    public String getName()
    {
        return GlobalVars.nwrBase.getEntry(this.ID).getName();
    }

    @Override
    public byte getSerializeKind()
    {
        return StaticData.SID_KNOWLEDGE;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.ID = StreamUtils.readInt(stream);
        this.RefsCount = StreamUtils.readWord(stream);

        this.Sign = GlobalVars.nwrBase.getEntry(this.ID).Sign;
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        StreamUtils.writeInt(stream, this.ID);
        StreamUtils.writeWord(stream, (short) this.RefsCount);
    }
}
