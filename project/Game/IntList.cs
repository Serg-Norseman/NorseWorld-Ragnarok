/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014, 2020 by Serg V. Zhdanovskih.
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

using System.Collections.Generic;
using System.IO;
using ZRLib.Core;

namespace NWR.Game
{
    public sealed class IntList : List<int>
    {
        public IntList() : base()
        {
        }

        public void LoadFromStream(BinaryReader stream, FileVersion version)
        {
            Clear();

            int count = StreamUtils.ReadInt(stream);
            for (int i = 0; i < count; i++) {
                Add(StreamUtils.ReadInt(stream));
            }
        }

        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            int count = Count;
            StreamUtils.WriteInt(stream, count);
            for (int i = 0; i < count; i++) {
                StreamUtils.WriteInt(stream, this[i]);
            }
        }
    }
}
