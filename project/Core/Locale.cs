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
using System.Collections.Generic;
using System.IO;
using System.Xml;
using ZRLib.Core;
using NWR.Database;
using ZRLib.Engine;
using NWR.Game;

namespace NWR.Core
{
    public sealed class Locale : BaseLocale
    {
        public class LangRec
        {
            public string Name;
            public string Prefix;

            public LangRec(string name, string prefix)
            {
                Name = name;
                Prefix = prefix;
            }
        }

        public const string LANGS_FOLDER = "languages\\";
        private static readonly string LANGS_XML = LANGS_FOLDER + "langs.xml";

        static Locale()
        {
            InitList(RS.rs_Last + 1);
        }

        private List<LangRec> fLangs;

        public Locale()
        {
            fLangs = new List<LangRec>();
            LoadLangs();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fLangs = null;
            }
            base.Dispose(disposing);
        }

        public LangRec GetLang(int index)
        {
            if (index >= 0 && index < fLangs.Count) {
                return fLangs[index];
            } else {
                return null;
            }
        }

        public int LangsCount
        {
            get {
                return fLangs.Count;
            }
        }

        public int FindLang(string langName)
        {
            for (int i = 0; i < fLangs.Count; i++) {
                if ((fLangs[i].Name == langName)) {
                    return i;
                }
            }
            return -1;
        }

        public bool SetLang(string name)
        {
            int idx = FindLang(name);
            if (idx < 0) {
                return false;
            } else {
                try {
                    string prefix = LANGS_FOLDER + fLangs[idx].Prefix;
                    string f = NWResourceManager.GetAppPath() + prefix;
                    LoadLangDB(f + "_db.xml");
                    LoadLangTexts(f + "_texts.xml");
                    LoadLangDialogs(f);
                    return true;
                } catch (Exception) {
                    return false;
                }
            }
        }

        private void LoadLangs()
        {
            var f = new FileStream(NWResourceManager.GetAppPath() + LANGS_XML, FileMode.Open);
            try {
                XmlDocument xmlDocument = new XmlDocument();
                xmlDocument.Load(f);

                XmlNode root = xmlDocument.DocumentElement;
                if (!root.Name.Equals("langs")) {
                    throw new Exception("Its not langs file!");
                }

                XmlNodeList nl = root.SelectNodes("lang");
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode el = nl[i];
                    try {
                        string name = (el.Attributes["name"].InnerText);
                        string prefix = (el.Attributes["prefix"].InnerText);

                        fLangs.Add(new LangRec(name, prefix));
                    } catch (Exception ex) {
                        Logger.Write("Locale.loadLangs.1(): " + ex.Message);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Locale.loadLangs(): " + ex.Message);
            }
        }

        private void LoadLangDB(string fileName)
        {
            var f = new FileStream(fileName, FileMode.Open);
            try {
                XmlDocument xmlDocument = new XmlDocument();
                xmlDocument.Load(f);

                XmlNode root = xmlDocument.DocumentElement;
                if (!root.Name.Equals("RDB")) {
                    throw new Exception("Its not RDB!");
                }

                XmlNode entries = root.SelectSingleNode("Entries");
                if (entries == null) {
                    //throw new RuntimeException("Not found entries!");
                }
                XmlNodeList nl = entries.ChildNodes;
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode el = nl[i];
                    if (el.Name.Equals("Entry")) {
                        try {
                            int id = Convert.ToInt32(el.Attributes["ID"].InnerText);
                            string name = DataEntry.ReadElement(el, "Name");
                            string desc = DataEntry.ReadElement(el, "Desc");
                            string morph = DataEntry.ReadElement(el, "Morphology");

                            DataEntry entry = GlobalVars.nwrDB.GetEntry(id);
                            if (entry != null) {
                                entry.Name = name;

                                desc = desc.Replace(AuxUtils.CRLF, " ");
                                desc = desc.Replace(("" + AuxUtils.CR), " ");
                                desc = desc.Replace(("" + AuxUtils.LF), " ");
                                desc = desc.Replace("  ", " ");
                                entry.Desc = desc;

                                entry.Morphology = morph;
                            }
                        } catch (Exception ex) {
                            Logger.Write("Locale.loadLangDB.1(): " + ex.Message);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Locale.loadLangDB(): " + ex.Message);
            }
        }

        private void LoadLangTexts(string fileName)
        {
            try {
                base.LoadLangTexts(new FileStream(fileName, FileMode.Open));
            } catch (Exception ex) {
                Logger.Write("Locale.loadLangTexts(): " + ex.Message);
            }
        }

        private XmlNode LoadLangDialog(string fileName)
        {
            if (!File.Exists(fileName)) {
                return null;
            }

            try {
                XmlDocument xmlDocument = new XmlDocument();
                xmlDocument.Load(new FileStream(fileName, FileMode.Open));

                XmlNode root = xmlDocument.DocumentElement;
                return root;
            } catch (Exception ex) {
                Logger.Write("Locale.loadLangDialog(): " + ex.Message);
            }
            return null;
        }

        private void LoadLangDialogs(string prefix)
        {
            int num = GlobalVars.nwrDB.EntriesCount;
            for (int i = 0; i < num; i++) {
                DataEntry entry = GlobalVars.nwrDB.GetEntry(i);
                if (entry != null && entry.Kind == DataEntry.ek_Creature) {
                    CreatureEntry crEntry = (CreatureEntry)entry;

                    if (!string.IsNullOrEmpty(crEntry.Dialog.ExternalFile)) {
                        string filename = prefix + crEntry.Dialog.ExternalFile;

                        try {
                            XmlNode dialogRoot = LoadLangDialog(filename);
                            // may be null, but check only in loadXML
                            crEntry.Dialog.LoadXML(dialogRoot, null, false);
                        } catch (Exception ex) {
                            Logger.Write("Locale.loadLangDialogs(): " + ex.Message);
                        }
                    }
                }
            }
        }
    }
}
