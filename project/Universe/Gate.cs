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
using NWR.Core;
using NWR.Core.Types;
using NWR.Game;
using ZRLib.Core;

namespace NWR.Universe
{
    public sealed class Gate : LocatedEntity
    {
        public const int KIND_NONE = 0;
        public const int KIND_FREE = 1;
        public const int KIND_FIX = 2;

        public int TargetLayer;
        public ExtPoint TargetField = new ExtPoint();
        public ExtPoint TargetPos = new ExtPoint();

        public Gate(GameSpace space, object owner)
            : base(space, owner)
        {
        }

        public override byte SerializeKind
        {
            get {
                return StaticData.SID_GATE;
            }
        }

        public override void LoadFromStream(BinaryReader stream, FileVersion version)
        {
            base.LoadFromStream(stream, version);

            TargetLayer = StreamUtils.ReadInt(stream);
            TargetField.X = (sbyte)StreamUtils.ReadByte(stream);
            TargetField.Y = (sbyte)StreamUtils.ReadByte(stream);
            TargetPos.X = (sbyte)StreamUtils.ReadByte(stream);
            TargetPos.Y = (sbyte)StreamUtils.ReadByte(stream);
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            base.SaveToStream(stream, version);

            StreamUtils.WriteInt(stream, TargetLayer);
            StreamUtils.WriteByte(stream, (byte)TargetField.X);
            StreamUtils.WriteByte(stream, (byte)TargetField.Y);
            StreamUtils.WriteByte(stream, (byte)TargetPos.X);
            StreamUtils.WriteByte(stream, (byte)TargetPos.Y);
        }

        public int Kind
        {
            get {
                int result = KIND_NONE;
    
                int gateTile = (CLSID_Renamed);
                if (gateTile != PlaceID.pid_Undefined) {
                    PlaceFlags ps = StaticData.dbPlaces[gateTile].Signs;
    
                    NWField field = (NWField)Owner;
    
                    if ((ps.Contains(PlaceFlags.psIsFreeGate)) || (gateTile == PlaceID.pid_Well && field.LandID == GlobalVars.Land_MimerRealm)) {
                        result = KIND_FREE;
                    } else {
                        if ((ps.Contains(PlaceFlags.psIsFixGate))) {
                            result = KIND_FIX;
                        }
                    }
                }
    
                return result;
            }
        }

        public override string Name
        {
            get {
                int fg = (CLSID_Renamed);
                string result = string.Format(BaseLocale.GetStr(RS.rs_GateTo), BaseLocale.GetStr(StaticData.dbPlaces[fg].NameRS), GlobalVars.nwrDB.GetEntry(TargetLayer).Name);
                return result;
            }
        }
    }

}