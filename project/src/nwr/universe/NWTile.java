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
package nwr.universe;

import java.io.IOException;
import jzrlib.core.FileVersion;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.map.BaseTile;
import jzrlib.utils.StreamUtils;
import nwr.creatures.NWCreature;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class NWTile extends BaseTile
{
    public short FogID;
    public short FogExtID;
    public byte FogAge;

    public boolean Trap_Discovered;
    public int Lake_LiquidID;

    // runtime
    public NWCreature CreaturePtr;
    public byte ScentAge;
    public NWCreature ScentTrail;

    public NWTile()
    {
        super();
    }

    @Override
    public void assign(BaseTile source)
    {
        super.assign(source);
        NWTile srcTile = (NWTile) source;

        this.FogID = srcTile.FogID;
        this.FogExtID = srcTile.FogExtID;
        this.FogAge = srcTile.FogAge;

        this.Trap_Discovered = srcTile.Trap_Discovered;
        this.Lake_LiquidID = srcTile.Lake_LiquidID;

        // runtime
        //this.CreaturePtr;
        //this.ScentAge;
        //this.ScentTrail;
    }

    public final void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.Background = (short) StreamUtils.readWord(stream);
        this.Foreground = (short) StreamUtils.readWord(stream);
        this.States = StreamUtils.readByte(stream);

        // extData begin
        this.FogID = (short) StreamUtils.readWord(stream);
        this.FogAge = (byte) StreamUtils.readByte(stream);

        this.Trap_Discovered = StreamUtils.readBoolean(stream);
        this.Lake_LiquidID = StreamUtils.readInt(stream);

        // runtime init
        this.FogExtID = 0;
        // extData end

        // runtime init
        this.BackgroundExt = 0;
        this.ForegroundExt = 0;
        this.CreaturePtr = null;
    }

    public final void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        StreamUtils.writeWord(stream, this.Background);
        StreamUtils.writeWord(stream, this.Foreground);
        StreamUtils.writeByte(stream, (byte) this.States);

        // extData begin
        StreamUtils.writeWord(stream, this.FogID);
        StreamUtils.writeByte(stream, (byte) this.FogAge);

        StreamUtils.writeBoolean(stream, Trap_Discovered);
        StreamUtils.writeInt(stream, Lake_LiquidID);
        // extData end
    }
}
