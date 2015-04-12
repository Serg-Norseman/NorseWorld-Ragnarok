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
import java.io.IOException;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class SourceForm extends MemoryEntry
{
    public int sfID;

    @Override
    public String getDesc()
    {
        return "";
    }

    @Override
    public String getName()
    {
        return "";
    }

    @Override
    public byte getSerializeKind()
    {
        return StaticData.SID_SOURCE_FORM;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.sfID = StreamUtils.readInt(stream);
        super.Sign = "SourceForm";
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        StreamUtils.writeInt(stream, this.sfID);
    }

    public SourceForm(Object owner)
    {
        super(owner);
    }
}
