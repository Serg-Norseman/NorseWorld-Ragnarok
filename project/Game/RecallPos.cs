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

using System.IO;
using BSLib;
using ZRLib.Core;
using NWR.Core;

namespace NWR.Game
{
    public sealed class RecallPos : MemoryEntry
    {
        public int Layer;
        public ExtPoint Field;
        public ExtPoint Pos;

        public RecallPos(object owner)
            : base(owner)
        {
            Field = new ExtPoint();
            Pos = new ExtPoint();
        }

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
                return StaticData.SID_RECALL_POS;
            }
        }

        public override void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            Layer = StreamUtils.ReadInt(stream);
            Field.X = StreamUtils.ReadInt(stream);
            Field.Y = StreamUtils.ReadInt(stream);
            Pos.X = StreamUtils.ReadInt(stream);
            Pos.Y = StreamUtils.ReadInt(stream);

            Sign = "RecallPos";
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            StreamUtils.WriteInt(stream, Layer);
            StreamUtils.WriteInt(stream, Field.X);
            StreamUtils.WriteInt(stream, Field.Y);
            StreamUtils.WriteInt(stream, Pos.X);
            StreamUtils.WriteInt(stream, Pos.Y);
        }
    }
}
