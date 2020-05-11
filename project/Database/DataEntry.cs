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
using BSLib;
using ZRLib.Core;
using ZRLib.Grammar;

namespace NWR.Database
{
    public abstract class DataEntry : BaseObject
    {
        public const sbyte ek_Unknown = 0;
        public const sbyte ek_Item = 1;
        public const sbyte ek_Creature = 2;
        public const sbyte ek_Land = 3;
        public const sbyte ek_Layer = 4;
        public const sbyte ek_Field = 5;
        public const sbyte ek_EventHandler = 6;
        public const sbyte ek_Information = 7;
        public const sbyte ek_Material = 8;

        public static readonly string[] EntryKinds = new string[] {
            "ek_Unknown",
            "ek_Item",
            "ek_Creature",
            "ek_Land",
            "ek_Layer",
            "ek_Field",
            "ek_EventHandler",
            "ek_Information",
            "ek_Material"
        };

        private string fName;
        private string fDesc;

        public int GUID;
        public readonly object Owner;

        public sbyte Kind;
        public string Morphology;
        public string Sign;

        public virtual string Name
        {
            get {
                return fName;
            }
            set {
                fName = value;
            }
        }


        public virtual string Desc
        {
            get {
                return fDesc;
            }
            set {
                fDesc = value;
            }
        }


        protected DataEntry(object owner)
        {
            Owner = owner;
        }

        public string GetNounDeclension(Number number, Case ncase)
        {
            string result = "";

            try {
                if (!string.IsNullOrEmpty(Morphology)) {
                    string word = AuxUtils.GetToken(Morphology, "[]", 1);
                    if (AuxUtils.GetTokensCount(Morphology, "[]") == 1) {
                        result = word;
                    } else {
                        string sfx = AuxUtils.GetToken(Morphology, "[]", 2);

                        int sf_idx = ((int)(number) - 1) * 6 + (int)(ncase);
                        string sf = AuxUtils.GetToken(sfx, ",", sf_idx);

                        if (sf.CompareTo("") == 0) {
                            result = word;
                        } else {
                            if (sf[0] != '-') {
                                result = sf;
                            } else {
                                sf = sf.Substring(1);
                                result = word + sf;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("BaseEntry.getNounDeclension(): " + ex.Message);
                throw ex;
            }

            return result;
        }

        public bool HasNounMorpheme(string sample)
        {
            return Morphology.Contains(sample);
        }

        public virtual void LoadXML(XmlNode element, FileVersion version)
        {
            GUID = Convert.ToInt32(element.Attributes["GUID"].InnerText);
            Sign = element.Attributes["Sign"].InnerText;
            fName = element.Attributes["Name"].InnerText;

            fDesc = ReadElement(element, "Desc");
        }

        public static sbyte ParseEntryKind(string value)
        {
            int num = EntryKinds.Length;
            for (sbyte i = 0; i < num; i++) {
                if ((EntryKinds[i] == value)) {
                    return i;
                }
            }
            throw new Exception("unknown entry kind");
        }

        public static string ReadElement(XmlNode parentElement, string tagName)
        {
            XmlNodeList nl = parentElement.ChildNodes;
            for (int i = 0; i < nl.Count; i++) {
                XmlNode node = nl[i];
                if (node.Name.Equals(tagName, StringComparison.CurrentCultureIgnoreCase)) {
                    string value = node.InnerText;
                    return value;
                }
            }
            return "";
        }
    }
}
