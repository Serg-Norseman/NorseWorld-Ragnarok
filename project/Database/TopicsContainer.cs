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

using System.Xml;
using BSLib;
using ZRLib.Core;

namespace NWR.Database
{
    public class TopicsContainer : BaseObject
    {
        private readonly ExtList<TopicEntry> fTopics;

        public TopicsContainer()
        {
            fTopics = new ExtList<TopicEntry>();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                Clear();
                fTopics.Dispose();
            }
            base.Dispose(disposing);
        }

        public TopicEntry GetTopic(int index)
        {
            TopicEntry result = null;
            if (index >= 0 && index < fTopics.Count) {
                result = ((TopicEntry)fTopics[index]);
            }
            return result;
        }

        public int TopicsCount
        {
            get {
                return fTopics.Count;
            }
        }

        public TopicEntry AddTopic()
        {
            TopicEntry result = new TopicEntry();
            fTopics.Add(result);
            return result;
        }

        public void Clear()
        {
            for (int i = fTopics.Count - 1; i >= 0; i--) {
                DeleteTopic(i);
            }
            fTopics.Clear();
        }

        public void DeleteTopic(int index)
        {
            ((TopicEntry)fTopics[index]).Dispose();
            fTopics.Delete(index);
        }

        public int IndexOf(TopicEntry topic)
        {
            return fTopics.IndexOf(topic);
        }

        public virtual void LoadXML(XmlNode element, FileVersion version)
        {
            XmlNodeList nl = element.ChildNodes;
            for (int i = 0; i < nl.Count; i++) {
                XmlNode el = nl[i];
                if (el.Name.Equals("Topic", System.StringComparison.OrdinalIgnoreCase)) {
                    AddTopic().LoadXML(el, version);
                }
            }
        }
    }
}
