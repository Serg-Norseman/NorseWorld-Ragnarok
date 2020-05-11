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

using System.IO;
using BSLib;
using NWR.Game;
using NWR.Game.Types;
using ZRLib.Core;

namespace NWR.Universe
{
    public enum GateKind
    {
        None,
        Free,
        Fix
    }

    public sealed class Gate : LocatedEntity
    {
        public int TargetLayer;
        public ExtPoint TargetField = new ExtPoint();
        public ExtPoint TargetPos = new ExtPoint();


        public GateKind Kind
        {
            get {
                GateKind result = GateKind.None;
    
                int gateTile = (CLSID);
                if (gateTile != PlaceID.pid_Undefined) {
                    PlaceFlags ps = StaticData.dbPlaces[gateTile].Signs;
    
                    NWField field = (NWField)Owner;
    
                    if ((ps.Contains(PlaceFlags.psIsFreeGate)) || (gateTile == PlaceID.pid_Well && field.LandID == GlobalVars.Land_MimerRealm)) {
                        result = GateKind.Free;
                    } else {
                        if ((ps.Contains(PlaceFlags.psIsFixGate))) {
                            result = GateKind.Fix;
                        }
                    }
                }
    
                return result;
            }
        }

        public override string Name
        {
            get {
                int fg = (CLSID);
                string result = string.Format(BaseLocale.GetStr(RS.rs_GateTo), BaseLocale.GetStr(StaticData.dbPlaces[fg].NameRS), GlobalVars.nwrDB.GetEntry(TargetLayer).Name);
                return result;
            }
        }

        public override byte SerializeKind
        {
            get {
                return StaticData.SID_GATE;
            }
        }


        public Gate(GameSpace space, object owner) : base(space, owner)
        {
        }

        public override void LoadFromStream(BinaryReader stream, FileVersion version)
        {
            base.LoadFromStream(stream, version);

            TargetLayer = StreamUtils.ReadInt(stream);
            TargetField.X = StreamUtils.ReadSByte(stream);
            TargetField.Y = StreamUtils.ReadSByte(stream);
            TargetPos.X = StreamUtils.ReadSByte(stream);
            TargetPos.Y = StreamUtils.ReadSByte(stream);
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            base.SaveToStream(stream, version);

            StreamUtils.WriteInt(stream, TargetLayer);
            StreamUtils.WriteSByte(stream, (sbyte)TargetField.X);
            StreamUtils.WriteSByte(stream, (sbyte)TargetField.Y);
            StreamUtils.WriteSByte(stream, (sbyte)TargetPos.X);
            StreamUtils.WriteSByte(stream, (sbyte)TargetPos.Y);
        }
    }
}
