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
using System.Xml;
using ZRLib.Core;

namespace NWR.Database
{
    public sealed class EventHandlerEntry : DataEntry
    {
        public int EventID;
        public string SourceScript;

        public EventHandlerEntry(object owner)
            : base(owner)
        {
        }

        public override void LoadXML(XmlNode element, FileVersion version)
        {
            try {
                base.LoadXML(element, version);

                EventID = Convert.ToInt32(ReadElement(element, "EventID"));
                SourceScript = ReadElement(element, "SourceScript");
            } catch (Exception ex) {
                Logger.Write("EventHandlerEntry.loadXML(): " + ex.Message);
                throw ex;
            }
        }
    }
}
