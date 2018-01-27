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
using NWR.Core;
using NWR.Game;
using ZRLib.Grammar;

namespace NWR.Game
{
    public sealed class Debt : MemoryEntry
    {
        public string Lender;
        public int Value;

        public Debt(object owner)
            : base(owner)
        {
        }

        private string Title
        {
            get {
                string s;
                if (GlobalVars.nwrWin.LangExt == "en") {
                    s = " to ";
                } else {
                    s = " ";
                }
                return BaseLocale.GetStr(RS.rs_Debt) + s + StaticData.MorphCompNoun(Lender, Case.cDative, Number.nSingle, CreatureSex.csMale, true, false);
            }
        }

        public override string Desc
        {
            get {
                return Title + ": " + Convert.ToString(Value) + "$.";
            }
        }

        public override string Name
        {
            get {
                return Title;
            }
        }

        public override byte SerializeKind
        {
            get {
                return StaticData.SID_DEBT;
            }
        }

        public override void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            Lender = StreamUtils.ReadString(stream, StaticData.DefEncoding);
            Value = StreamUtils.ReadInt(stream);

            Sign = "Debt_" + Lender;
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            StreamUtils.WriteString(stream, Lender, StaticData.DefEncoding);
            StreamUtils.WriteInt(stream, Value);
        }
    }
}
