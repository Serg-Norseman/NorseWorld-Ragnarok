/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih.
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

using System;
using System.IO;
using ZRLib.Core;
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain
{
    public abstract class NWBrainEntity : BrainEntity, ISerializable
    {
        public byte SerializeKind
        {
            get {
                return 0;
            }
        }

        protected NWBrainEntity(CreatureEntity owner) : base(owner)
        {
        }

        public void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            try {
                ClearGoals();

                int count = StreamUtils.ReadInt(stream);
                for (int i = 0; i < count; i++) {
                    sbyte kind = (sbyte)StreamUtils.ReadByte(stream);
                    NWGoalEntity item = (NWGoalEntity)SerializablesManager.CreateSerializable(kind, this);
                    item.LoadFromStream(stream, version);
                    fGoals.Add(item);
                }
            } catch (Exception ex) {
                Logger.Write("NWBrainEntity.loadFromStream(): " + ex.Message);
                throw ex;
            }
        }

        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            try {
                int count = fGoals.Count;

                int num = fGoals.Count;
                for (int i = 0; i < num; i++) {
                    ISerializable item = (ISerializable)fGoals[i];
                    if (item.SerializeKind <= 0) {
                        count--;
                    }
                }

                StreamUtils.WriteInt(stream, count);

                for (int i = 0; i < num; i++) {
                    ISerializable item = (ISerializable)fGoals[i];
                    byte kind = item.SerializeKind;
                    if (kind > 0) {
                        StreamUtils.WriteByte(stream, kind);
                        item.SaveToStream(stream, version);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWBrainEntity.saveToStream(): " + ex.Message);
                throw ex;
            }
        }
    }
}
