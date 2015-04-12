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
package nwr.creatures;

import java.io.IOException;
import jzrlib.core.FileVersion;
import jzrlib.core.ISerializable;
import jzrlib.core.body.AbstractBody;
import jzrlib.core.body.Bodypart;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.Logger;
import jzrlib.utils.StreamUtils;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class CustomBody extends AbstractBody implements ISerializable
{
    public CustomBody(Object owner)
    {
        super(owner);
    }
    
    public final void addPart(BodypartType part, BodypartAbilities props, int stdCount)
    {
        for (int i = 1; i <= stdCount; i++) {
            this.addPart(part.ordinal());
        }
    }

    @Override
    public byte getSerializeKind()
    {
        return 0;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        try {
            this.clear();

            int count = (int) StreamUtils.readByte(stream);
            for (int i = 0; i < count; i++) {
                int part = StreamUtils.readByte(stream);
                int state = StreamUtils.readByte(stream);
                this.addPart(part, state);
            }
        } catch (Exception ex) {
            Logger.write("AbstractBody.loadFromStream(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        try {
            int count = this.fParts.size();

            StreamUtils.writeByte(stream, (byte) count);

            for (int i = 0; i < count; i++) {
                Bodypart entry = this.getPart(i);
                StreamUtils.writeByte(stream, (byte) entry.Type);
                StreamUtils.writeByte(stream, (byte) entry.State);
            }
        } catch (Exception ex) {
            Logger.write("AbstractBody.saveToStream(): " + ex.getMessage());
            throw ex;
        }
    }
    
    @Override
    public void update()
    {
        // dummy
    }
}
