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
import jzrlib.core.GameSpace;
import jzrlib.core.LocatedEntity;
import jzrlib.core.Point;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.StreamUtils;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.PlaceFlags;
import nwr.core.types.PlaceID;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public final class Gate extends LocatedEntity
{
    public static final int KIND_NONE = 0;
    public static final int KIND_FREE = 1;
    public static final int KIND_FIX = 2;

    public int TargetLayer;
    public Point TargetField = new Point();
    public Point TargetPos = new Point();

    public Gate(GameSpace space, Object owner)
    {
        super(space, owner);
    }

    @Override
    public byte getSerializeKind()
    {
        return StaticData.SID_GATE;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        super.loadFromStream(stream, version);

        this.TargetLayer = StreamUtils.readInt(stream);
        this.TargetField.X = (byte) StreamUtils.readByte(stream);
        this.TargetField.Y = (byte) StreamUtils.readByte(stream);
        this.TargetPos.X = (byte) StreamUtils.readByte(stream);
        this.TargetPos.Y = (byte) StreamUtils.readByte(stream);
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        super.saveToStream(stream, version);

        StreamUtils.writeInt(stream, TargetLayer);
        StreamUtils.writeByte(stream, (byte) TargetField.X);
        StreamUtils.writeByte(stream, (byte) TargetField.Y);
        StreamUtils.writeByte(stream, (byte) TargetPos.X);
        StreamUtils.writeByte(stream, (byte) TargetPos.Y);
    }

    public final int getKind()
    {
        int result = KIND_NONE;

        int gateTile = (this.CLSID);
        if (gateTile != PlaceID.pid_Undefined) {
            PlaceFlags ps = StaticData.dbPlaces[gateTile].Signs;

            NWField field = (NWField) this.Owner;

            if ((ps.contains(PlaceFlags.psIsFreeGate)) || (gateTile == PlaceID.pid_Well && field.LandID == GlobalVars.Land_MimerRealm)) {
                result = KIND_FREE;
            } else {
                if ((ps.contains(PlaceFlags.psIsFixGate))) {
                    result = KIND_FIX;
                }
            }
        }

        return result;
    }

    @Override
    public String getName()
    {
        int fg = (this.CLSID);
        String result = String.format(Locale.getStr(RS.rs_GateTo), Locale.getStr(StaticData.dbPlaces[fg].NameRS), GlobalVars.nwrBase.getEntry(this.TargetLayer).getName());
        return result;
    }
}
