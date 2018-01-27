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
using System.Xml;
using BSLib;
using NWR.Core;
using NWR.Game;
using ZRLib.Core;

namespace NWR.Database
{
    public sealed class NWDatabase : BaseObject
    {
        /*private static final char[] RDBSign;
        private static final FileVersion RDBVersion;*/

        private DataEntry[] fEntries;
        private int fEntriesCount;

        /*static {
            RDBSign = new char[]{'B', 'D', 'B'};
            RDBVersion = new FileVersion(4, 47);
        }*/

        public NWDatabase()
        {
            fEntries = null;
            fEntriesCount = 0;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                Clear();
            }
            base.Dispose(disposing);
        }

        public void Clear()
        {
            int num = fEntriesCount;
            for (int id = 0; id < num; id++) {
                if (fEntries[id] != null) {
                    fEntries[id].Dispose();
                }
            }

            fEntries = null;
            fEntriesCount = 0;
        }

        public int EntriesCount
        {
            get {
                return fEntriesCount;
            }
        }

        public DataEntry GetEntry(int id)
        {
            if (id >= 0 && id < fEntriesCount) {
                return fEntries[id];
            } else {
                return null;
            }
        }

        private DataEntry CreateEntry(sbyte kind)
        {
            DataEntry result;

            switch (kind) {
                case DataEntry.ek_Item:
                    result = new ItemEntry(this);
                    break;

                case DataEntry.ek_Creature:
                    result = new CreatureEntry(this);
                    break;

                case DataEntry.ek_Land:
                    result = new LandEntry(this);
                    break;

                case DataEntry.ek_Layer:
                    result = new LayerEntry(this);
                    break;

                case DataEntry.ek_Field:
                    result = new FieldEntry(this);
                    break;

                case DataEntry.ek_EventHandler:
                    result = new EventHandlerEntry(this);
                    break;

                case DataEntry.ek_Information:
                    result = new InfoEntry(this);
                    break;

                case DataEntry.ek_Material:
                    result = new MaterialEntry(this);
                    break;

                default:
                    result = new InfoEntry(this);
                    break;
            }

            return result;
        }

        public void LoadXML(string fileName)
        {
            try {
                FileHeader header = new FileHeader();
                Clear();

                Stream @is = NWResourceManager.LoadStream(fileName);

                XmlDocument xmlDocument = new XmlDocument();
                xmlDocument.Load(@is);

                XmlNode root = xmlDocument.DocumentElement;
                if (!root.Name.Equals("RDB")) {
                    throw new Exception("Its not RDB!");
                }

                XmlNode vers = root.SelectSingleNode("Version");
                if (vers == null) {
                    //throw new RuntimeException("Not found version!");
                }

                XmlNode entries = root.SelectSingleNode("Entries");
                if (entries == null) {
                    //throw new RuntimeException("Not found entries!");
                }

                fEntriesCount = Convert.ToInt32(entries.Attributes["Count"].InnerText);
                fEntries = new DataEntry[fEntriesCount];

                XmlNodeList nl = entries.ChildNodes;
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode el = nl[i];
                    if (el.Name.Equals("Entry")) {
                        string val = "";
                        try {
                            sbyte kind = DataEntry.ParseEntryKind(el.Attributes["Kind"].InnerText);
                            int id = Convert.ToInt32(el.Attributes["GUID"].InnerText);

                            DataEntry entry = null;
                            if (kind != DataEntry.ek_Unknown) {
                                entry = CreateEntry(kind);
                                entry.Kind = kind;
                                entry.LoadXML(el, header.Version);
                            }
                            fEntries[id] = entry;
                        } catch (Exception ex) {
                            Logger.Write("LoadXML.1(" + val + "): " + ex.Message);
                        }
                    }
                }

                Logger.Write("dbLoad(): ok");
            } catch (Exception ex) {
                Logger.Write("LoadXML(): " + ex.Message);
            }
        }

        public DataEntry AddEntry(sbyte kind)
        {
            if (kind == DataEntry.ek_Unknown) {
                throw new Exception("NWR-DB entry kind is invalid");
            }
            fEntriesCount++;

            int newCount = fEntriesCount;
            DataEntry[] srcArray = fEntries;
            DataEntry[] destArray = new DataEntry[newCount];
            if (newCount > 0 && srcArray != null) {
                int oldCount = srcArray.Length;
                if (oldCount > newCount) {
                    oldCount = newCount;
                }
                if (oldCount > 0) {
                    Array.Copy(srcArray, 0, destArray, 0, oldCount);
                }
            }
            fEntries = destArray;

            int id = fEntriesCount - 1;

            DataEntry result = CreateEntry(kind);
            result.GUID = id;
            result.Kind = kind;
            fEntries[id] = result;

            return result;
        }

        public DataEntry FindEntryBySign(string sign)
        {
            int num = fEntriesCount;
            for (int id = 0; id < num; id++) {
                DataEntry entry = fEntries[id];
                if (entry != null && entry.Sign.CompareTo(sign) == 0) {
                    return entry;
                }
            }

            return null;
        }

        /*public final EntitySet findEntriesByKind(byte kind)
        {
            EntitySet result = new EntitySet();
    
            int num = this.fEntriesCount;
            for (int id = 0; id < num; id++) {
                BaseEntry entry = this.fEntries[id];
                if (entry != null && entry.Kind == kind) {
                    result.Add(entry.GUID);
                }
            }
    
            return result;
        }*/
    }
}