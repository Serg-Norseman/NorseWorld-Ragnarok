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
package nwr.creatures.brain;

import jzrlib.core.brain.BrainEntity;
import jzrlib.core.FileVersion;
import jzrlib.core.brain.GoalEntity;
import jzrlib.core.ISerializable;
import jzrlib.utils.StreamUtils;
import java.io.IOException;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class NWGoalEntity extends GoalEntity implements ISerializable
{
    public NWGoalEntity(BrainEntity owner)
    {
        super(owner);
    }

    @Override
    public byte getSerializeKind()
    {
        return 0;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.Duration = StreamUtils.readInt(stream);
        this.EmitterID = StreamUtils.readInt(stream);
        this.Kind = StreamUtils.readInt(stream);
        this.SourceID = StreamUtils.readInt(stream);
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        StreamUtils.writeInt(stream, this.Duration);
        StreamUtils.writeInt(stream, this.EmitterID);
        StreamUtils.writeInt(stream, this.Kind);
        StreamUtils.writeInt(stream, this.SourceID);
    }
}
