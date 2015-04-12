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
import jzrlib.core.Point;
import jzrlib.utils.StreamUtils;
import nwr.core.StaticData;
import java.io.IOException;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class RecallPos extends MemoryEntry
{
    public int Layer;
    public Point Field;
    public Point Pos;

    public RecallPos(Object owner)
    {
        super(owner);
        this.Field = new Point();
        this.Pos = new Point();
    }

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
        return StaticData.SID_RECALL_POS;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.Layer = StreamUtils.readInt(stream);
        this.Field.X = StreamUtils.readInt(stream);
        this.Field.Y = StreamUtils.readInt(stream);
        this.Pos.X = StreamUtils.readInt(stream);
        this.Pos.Y = StreamUtils.readInt(stream);

        this.Sign = "RecallPos";
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        StreamUtils.writeInt(stream, this.Layer);
        StreamUtils.writeInt(stream, this.Field.X);
        StreamUtils.writeInt(stream, this.Field.Y);
        StreamUtils.writeInt(stream, this.Pos.X);
        StreamUtils.writeInt(stream, this.Pos.Y);
    }
}
