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
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain
{
    public abstract class NWGoalEntity : GoalEntity, ISerializable
    {
        public virtual byte SerializeKind
        {
            get { return 0; }
        }

        protected NWGoalEntity(BrainEntity owner) : base(owner)
        {
        }

        public virtual void LoadFromStream(BinaryReader stream, FileVersion version)
        {
            Duration = StreamUtils.ReadInt(stream);
            EmitterID = StreamUtils.ReadInt(stream);
            Kind = StreamUtils.ReadInt(stream);
            SourceID = StreamUtils.ReadInt(stream);
        }

        public virtual void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            StreamUtils.WriteInt(stream, Duration);
            StreamUtils.WriteInt(stream, EmitterID);
            StreamUtils.WriteInt(stream, Kind);
            StreamUtils.WriteInt(stream, SourceID);
        }
    }
}
