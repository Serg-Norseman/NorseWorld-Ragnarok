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
import jzrlib.common.GameTime;
import jzrlib.core.FileVersion;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.StreamUtils;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class NWDateTime extends GameTime
{
    public NWDateTime()
    {
        super();
    }

    public NWDateTime(int year, int month, int day, int hour, int minute, int second)
    {
        super(year, month, day, hour, minute, second);
    }

    @Override
    public NWDateTime clone()
    {
        NWDateTime varCopy = new NWDateTime(this.Year, this.Month, this.Day, this.Hour, this.Minute, this.Second);
        return varCopy;
    }

    public final void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.Year = (short) StreamUtils.readWord(stream);
        this.Month = (byte) StreamUtils.readByte(stream);
        this.Day = (byte) StreamUtils.readByte(stream);
        this.Hour = (byte) StreamUtils.readByte(stream);
        this.Minute = (byte) StreamUtils.readByte(stream);
        this.Second = (byte) StreamUtils.readByte(stream);
        this.dummy = (byte) StreamUtils.readByte(stream);
    }

    public final void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        StreamUtils.writeWord(stream, this.Year);
        StreamUtils.writeByte(stream, this.Month);
        StreamUtils.writeByte(stream, this.Day);
        StreamUtils.writeByte(stream, this.Hour);
        StreamUtils.writeByte(stream, this.Minute);
        StreamUtils.writeByte(stream, this.Second);
        StreamUtils.writeByte(stream, this.dummy);
    }
}
