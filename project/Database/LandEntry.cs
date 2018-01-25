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
using NWR.Core.Types;

namespace NWR.Database
{
    public sealed class LandEntry : DeclinableEntry
    {
        public GameScreen Splash;
        public LandFlags Flags;
        public string BackSFX;
        public string[] ForeSFX;
        public string Song;

        public LandEntry(object owner)
            : base(owner)
        {
        }

        public override void LoadXML(XmlNode element, FileVersion version)
        {
            try {
                base.LoadXML(element, version);

                Splash = (GameScreen)Enum.Parse(typeof(GameScreen), ReadElement(element, "Splash"));

                string signs = ReadElement(element, "Signs");
                Flags = new LandFlags(signs);
                if (!signs.Equals(Flags.Signature)) {
                    throw new Exception("LandSigns not equals");
                }

                BackSFX = (ReadElement(element, "BackSFX"));
                Song = (ReadElement(element, "Song"));

                XmlNodeList nl = element.SelectNodes("ForeSFX");
                ForeSFX = new string[nl.Count];
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode n = nl[i];
                    ForeSFX[i] = n.InnerText;
                }
            } catch (Exception ex) {
                Logger.Write("LandEntry.loadXML(): " + ex.Message);
                throw ex;
            }
        }
    }
}
