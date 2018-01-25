/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using System.Collections.Generic;
using System.IO;
using ZRLib.Core;

namespace NWR.Game
{
    public sealed class Memory : BaseEntity
    {
        private readonly Dictionary<string, MemoryEntry> fTable;

        public Memory(object owner)
            : base(owner)
        {
            fTable = new Dictionary<string, MemoryEntry>();
        }

        public Dictionary<string, MemoryEntry> Data
        {
            get {
                return fTable;
            }
        }

        public MemoryEntry Add(string sign, MemoryEntry data)
        {
            fTable[sign] = data;
            return data;
        }

        public void Delete(string sign)
        {
            fTable.Remove(sign);
        }

        public MemoryEntry Find(string sign)
        {
            MemoryEntry result = null;
            if (fTable.ContainsKey(sign) ? (result = fTable.GetValueOrNull(sign)) == result : false) {
                return result;
            } else {
                return null;
            }
        }

        public void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            fTable.Clear();

            int count = StreamUtils.ReadInt(stream);
            for (int i = 0; i < count; i++) {
                sbyte kind = (sbyte)StreamUtils.ReadByte(stream);
                MemoryEntry data = (MemoryEntry)SerializablesManager.CreateSerializable(kind, Owner);
                data.LoadFromStream(stream, version);
                fTable[data.Sign] = data;
            }
        }

        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            StreamUtils.WriteInt(stream, fTable.Count);

            foreach (KeyValuePair<string, MemoryEntry> dicEntry in fTable) {
                MemoryEntry data = (MemoryEntry)dicEntry.Value;
                byte kind = data.SerializeKind;
                StreamUtils.WriteByte(stream, kind);
                data.SaveToStream(stream, version);
            }
        }
    }
}
