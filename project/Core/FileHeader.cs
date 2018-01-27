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

namespace NWR.Core
{
    public sealed class FileHeader : ICloneable
    {
        public char[] Sign;
        public FileVersion Version;

        public FileHeader()
        {
            Sign = new char[3];
        }

        public FileHeader(char[] sign, FileVersion version)
        {
            Sign = sign;
            Version = version;
        }

        public object Clone()
        {
            FileHeader varCopy = new FileHeader(Sign, Version);
            return varCopy;
        }

        public void Read(BinaryReader S)
        {
            byte[] signBuffer = new byte[3];
            for (int i = 0; i <= 2; i++) {
                signBuffer[i] = (byte)StreamUtils.ReadByte(S);
            }
            string str = StaticData.DefEncoding.GetString(signBuffer);
            for (int i = 0; i <= 2; i++) {
                Sign[i] = str[i];
            }

            Version = StreamUtils.ReadFileVersion(S);
        }

        public void Write(BinaryWriter S)
        {
            string str = new string(Sign);
            byte[] signBuffer = StaticData.DefEncoding.GetBytes(str);
            for (int i = 0; i <= 2; i++) {
                StreamUtils.WriteByte(S, signBuffer[i]);
            }

            StreamUtils.WriteFileVersion(S, Version);
        }
    }
}
