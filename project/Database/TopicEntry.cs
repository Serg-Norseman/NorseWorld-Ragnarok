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

using System.Xml;
using ZRLib.Core;

namespace NWR.Database
{
    public sealed class TopicEntry : TopicsContainer
    {
        public string Condition;
        public string Phrase;
        public string Answer;
        public string Action;

        public TopicEntry()
            : base()
        {
        }

        public TopicEntry(string phrase, string answer)
            : base()
        {
            Phrase = phrase;
            Answer = answer;
            Action = "";
        }

        public override void LoadXML(XmlNode element, FileVersion version)
        {
            Condition = (DataEntry.ReadElement(element, "Condition"));
            Phrase = DataEntry.ReadElement(element, "Phrase");
            Answer = DataEntry.ReadElement(element, "Answer");
            Action = DataEntry.ReadElement(element, "Action");

            base.LoadXML(element, version);
        }
    }
}
