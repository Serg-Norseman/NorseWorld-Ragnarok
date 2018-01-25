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

using System;
using System.Xml;
using ZRLib.Core;

namespace NWR.Database
{
    public sealed class LayerEntry : DataEntry
    {
        public int W;
        public int H;
        public int MSX;
        public int MSY;
        public string IconsName;

        public int IconsIndex;

        public LayerEntry(object owner)
            : base(owner)
        {
        }

        public FieldEntry GetFieldEntry(int X, int Y)
        {
            FieldEntry result = null;
            if (X >= 0 && X < W && Y >= 0 && Y < H) {
                string temp = Sign.Substring(6);
                temp = "Field_" + temp + Convert.ToString(X) + Convert.ToString(Y);

                result = (FieldEntry)((NWDatabase)Owner).FindEntryBySign(temp);
            }
            return result;
        }

        public override void LoadXML(XmlNode element, FileVersion version)
        {
            try {
                base.LoadXML(element, version);

                W = (Convert.ToInt32(ReadElement(element, "W")));
                H = (Convert.ToInt32(ReadElement(element, "H")));
                MSX = (Convert.ToInt32(ReadElement(element, "MSX")));
                MSY = (Convert.ToInt32(ReadElement(element, "MSY")));
                IconsName = ReadElement(element, "IconsName");
            } catch (Exception ex) {
                Logger.Write("LayerEntry.loadXML(): " + ex.Message);
                throw ex;
            }
        }
    }
}
