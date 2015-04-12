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
import jzrlib.core.CreatureEntity;
import jzrlib.core.FileVersion;
import jzrlib.core.ISerializable;
import jzrlib.core.SerializablesManager;
import jzrlib.utils.StreamUtils;
import java.io.IOException;
import jzrlib.utils.Logger;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class NWBrainEntity extends BrainEntity implements ISerializable
{
    public NWBrainEntity(CreatureEntity owner)
    {
        super(owner);
    }

    @Override
    public byte getSerializeKind()
    {
        return 0;
    }

    @Override
    public final void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        try {
            this.clearGoals();

            int count = StreamUtils.readInt(stream);
            for (int i = 0; i < count; i++) {
                byte kind = (byte) StreamUtils.readByte(stream);
                NWGoalEntity item = (NWGoalEntity) SerializablesManager.createSerializable(kind, this);
                item.loadFromStream(stream, version);
                this.fGoals.add(item);
            }
        } catch (Exception ex) {
            Logger.write("NWBrainEntity.loadFromStream(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public final void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        try {
            int count = this.fGoals.size();

            int num = this.fGoals.size();
            for (int i = 0; i < num; i++) {
                ISerializable item = (ISerializable) this.fGoals.get(i);
                if (item.getSerializeKind() <= 0) {
                    count--;
                }
            }

            StreamUtils.writeInt(stream, count);

            for (int i = 0; i < num; i++) {
                ISerializable item = (ISerializable) this.fGoals.get(i);
                byte kind = item.getSerializeKind();
                if (kind > 0) {
                    StreamUtils.writeByte(stream, kind);
                    item.saveToStream(stream, version);
                }
            }
        } catch (Exception ex) {
            Logger.write("NWBrainEntity.saveToStream(): " + ex.getMessage());
            throw ex;
        }
    }
}
