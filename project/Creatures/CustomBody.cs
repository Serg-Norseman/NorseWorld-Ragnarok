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
using NWR.Game.Types;
using ZRLib.Core;
using ZRLib.Core.Body;

namespace NWR.Creatures
{
    public class CustomBody : AbstractBody, ISerializable
    {
        public byte SerializeKind
        {
            get {
                return 0;
            }
        }

        public CustomBody(object owner)
            : base(owner)
        {
        }

        public void AddPart(BodypartType part, BodypartAbilities props, int stdCount)
        {
            for (int i = 1; i <= stdCount; i++) {
                AddPart((int)part);
            }
        }

        public void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            try {
                Clear();

                int count = StreamUtils.ReadByte(stream);
                for (int i = 0; i < count; i++) {
                    int part = StreamUtils.ReadByte(stream);
                    int state = StreamUtils.ReadByte(stream);
                    AddPart(part, state);
                }
            } catch (Exception ex) {
                Logger.Write("AbstractBody.loadFromStream(): " + ex.Message);
                throw ex;
            }
        }

        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            try {
                int count = fParts.Count;

                StreamUtils.WriteByte(stream, (byte)count);

                for (int i = 0; i < count; i++) {
                    Bodypart entry = GetPart(i);
                    StreamUtils.WriteByte(stream, (byte)entry.Type);
                    StreamUtils.WriteByte(stream, (byte)entry.State);
                }
            } catch (Exception ex) {
                Logger.Write("AbstractBody.saveToStream(): " + ex.Message);
                throw ex;
            }
        }

        public override void Update()
        {
            // dummy
        }
    }
}
