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

using System.IO;
using ZRLib.Core;

namespace NWR.Game
{
    public sealed class NWDateTime : GameTime
    {
        public NWDateTime()
            : base()
        {
        }

        public NWDateTime(int year, int month, int day, int hour, int minute, int second)
            : base(year, month, day, hour, minute, second)
        {
        }

        public override GameTime Clone()
        {
            NWDateTime varCopy = new NWDateTime(Year, Month, Day, Hour, Minute, Second);
            return varCopy;
        }

        public void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            Year = (short)StreamUtils.ReadWord(stream);
            Month = StreamUtils.ReadByte(stream);
            Day = StreamUtils.ReadByte(stream);
            Hour = StreamUtils.ReadByte(stream);
            Minute = StreamUtils.ReadByte(stream);
            Second = StreamUtils.ReadByte(stream);
            Dummy = StreamUtils.ReadByte(stream);
        }

        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            StreamUtils.WriteWord(stream, (ushort)Year);
            StreamUtils.WriteByte(stream, Month);
            StreamUtils.WriteByte(stream, Day);
            StreamUtils.WriteByte(stream, Hour);
            StreamUtils.WriteByte(stream, Minute);
            StreamUtils.WriteByte(stream, Second);
            StreamUtils.WriteByte(stream, Dummy);
        }
    }
}
