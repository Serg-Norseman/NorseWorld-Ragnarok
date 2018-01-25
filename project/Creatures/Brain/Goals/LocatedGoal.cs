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
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain.Goals
{
    public abstract class LocatedGoal : NWGoalEntity
    {
        public ExtPoint Position;

        protected LocatedGoal(BrainEntity owner)
            : base(owner)
        {
        }

        public override void LoadFromStream(BinaryReader stream, FileVersion version)
        {
            base.LoadFromStream(stream, version);
            Position = StreamUtils.ReadPoint(stream);
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            base.SaveToStream(stream, version);
            StreamUtils.WritePoint(stream, Position);
        }
    }
}
