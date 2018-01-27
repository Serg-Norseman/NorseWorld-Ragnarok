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
    public sealed class DialogEntry : BaseObject
    {
        private readonly ExtList<ConversationEntry> fConversations;

        public string ExternalFile;

        public DialogEntry()
        {
            fConversations = new ExtList<ConversationEntry>();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                Clear();
                fConversations.Dispose();
            }
            base.Dispose(disposing);
        }

        public ConversationEntry GetConversation(int index)
        {
            ConversationEntry result = null;
            if (index >= 0 && index < fConversations.Count) {
                result = ((ConversationEntry)fConversations[index]);
            }
            return result;
        }

        public int ConversationsCount
        {
            get {
                return fConversations.Count;
            }
        }

        public ConversationEntry AddConversation()
        {
            ConversationEntry result = new ConversationEntry();
            fConversations.Add(result);
            return result;
        }

        public ConversationEntry AddConversation(string name)
        {
            ConversationEntry result = new ConversationEntry(name);
            fConversations.Add(result);
            return result;
        }

        public void Clear()
        {
            for (int i = fConversations.Count - 1; i >= 0; i--) {
                DeleteConversation(i);
            }
            fConversations.Clear();
        }

        public void DeleteConversation(int index)
        {
            ((ConversationEntry)fConversations[index]).Dispose();
            fConversations.Delete(index);
        }

        public int IndexOf(ConversationEntry convers)
        {
            return fConversations.IndexOf(convers);
        }

        public void LoadXML(XmlNode element, FileVersion version, bool dbSource)
        {
            Clear();

            if (element != null) {
                if (dbSource) {
                    XmlAttribute extFile = element.Attributes["ExtFile"];
                    ExternalFile = (extFile != null) ? extFile.InnerText : "";
                }

                if (!dbSource || string.IsNullOrEmpty(ExternalFile)) {
                    XmlNodeList nl = element.ChildNodes;
                    for (int i = 0; i < nl.Count; i++) {
                        XmlNode el = nl[i];
                        if (el.Name.Equals("Conversation")) {
                            AddConversation().LoadXML(el, version);
                        }
                    }
                }
            }
        }
    }
}