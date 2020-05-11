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
using ZRLib.Map;
using NWR.Creatures;

namespace NWR.Universe
{
    public sealed class NWTile : BaseTile
    {
        public ushort FogID;
        public ushort FogExtID;
        public sbyte FogAge;

        public bool Trap_Discovered;
        public int Lake_LiquidID;

        // runtime
        public NWCreature CreaturePtr;
        public byte ScentAge;
        public NWCreature ScentTrail;

        public override void Assign(BaseTile source)
        {
            base.Assign(source);
            NWTile srcTile = (NWTile)source;

            FogID = srcTile.FogID;
            FogExtID = srcTile.FogExtID;
            FogAge = srcTile.FogAge;

            Trap_Discovered = srcTile.Trap_Discovered;
            Lake_LiquidID = srcTile.Lake_LiquidID;

            // runtime
            //this.CreaturePtr;
            //this.ScentAge;
            //this.ScentTrail;
        }

        public void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            Background = StreamUtils.ReadWord(stream);
            Foreground = StreamUtils.ReadWord(stream);
            States = StreamUtils.ReadByte(stream);

            // extData begin
            FogID = StreamUtils.ReadWord(stream);
            FogAge = StreamUtils.ReadSByte(stream);

            Trap_Discovered = StreamUtils.ReadBoolean(stream);
            Lake_LiquidID = StreamUtils.ReadInt(stream);

            // runtime init
            FogExtID = 0;
            // extData end

            // runtime init
            BackgroundExt = 0;
            ForegroundExt = 0;
            CreaturePtr = null;
        }

        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            StreamUtils.WriteWord(stream, Background);
            StreamUtils.WriteWord(stream, Foreground);
            StreamUtils.WriteByte(stream, (byte)States);

            // extData begin
            StreamUtils.WriteWord(stream, FogID);
            StreamUtils.WriteSByte(stream, FogAge);

            StreamUtils.WriteBoolean(stream, Trap_Discovered);
            StreamUtils.WriteInt(stream, Lake_LiquidID);
            // extData end
        }
    }
}
