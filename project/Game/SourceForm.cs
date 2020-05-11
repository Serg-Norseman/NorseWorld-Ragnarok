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
    public class SourceForm : MemoryEntry
    {
        public int SfID;

        public override string Desc
        {
            get {
                return "";
            }
        }

        public override string Name
        {
            get {
                return "";
            }
        }

        public override byte SerializeKind
        {
            get {
                return StaticData.SID_SOURCE_FORM;
            }
        }

        public override void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            SfID = StreamUtils.ReadInt(stream);
            Sign = "SourceForm";
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            StreamUtils.WriteInt(stream, SfID);
        }

        public SourceForm(object owner)
            : base(owner)
        {
        }
    }
}
