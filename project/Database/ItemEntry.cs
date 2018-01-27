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
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Effects;

namespace NWR.Database
{
    public sealed class ItemEntry : VolatileEntry
    {
        private static readonly string[] dbItemAttributes;

        public sealed class EffectEntry
        {
            public EffectID EffID;
            public int ExtData;
        }

        public sealed class ContentsEntry
        {
            public string ItemID;
            public int Chance;
        }

        public string ImageName;
        public ItemKind ItmKind;
        public ItemFlags Flags;
        public BodypartType EqKind;
        public sbyte Frequency;
        public short Satiety;
        public short Price;
        public float Weight;
        public int[] Attributes = new int[10];
        public EffectEntry[] Effects;
        public ContentsEntry[] Contents;
        public MaterialKind Material;
        public MNXRange BonusRange;
        public sbyte FramesCount;

        public int Prefix;

        public sbyte FramesLoaded;
        public int ImageIndex;

        static ItemEntry()
        {
            dbItemAttributes = new string[] {
                "Defense",
                "DamageMin",
                "DamageMax",
                "Mdf_Str",
                "Mdf_Luck",
                "Mdf_Speed",
                "Mdf_Attacks",
                "Mdf_ToHit",
                "Mdf_Health",
                "Mdf_Mana"
            };
        }

        public ItemEntry(object owner)
            : base(owner)
        {
            FramesLoaded = 0;
            BonusRange = new MNXRange();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                Effects = null;
                Contents = null;
            }
            base.Dispose(disposing);
        }

        public int Rnd
        {
            get {
                int result = -1;
                
                ContentsEntry[] contents = Contents;
                int cnt = (contents != null) ? contents.Length : 0;
                
                if (cnt != 0) {
                    NWDatabase db = (NWDatabase)Owner;
                    
                    ProbabilityTable<int> data = new ProbabilityTable<int>();
                    for (int i = 0; i < cnt; i++) {
                        string sign = contents[i].ItemID;
                        ItemEntry entry = ((ItemEntry)db.FindEntryBySign(sign));
                        int freq = (int)entry.Frequency;
                        if (freq > 0) {
                            data.Add(entry.GUID, freq);
                        }
                    }
                    
                    result = data.GetRandomItem();
                    
                    ItemEntry iEntry = ((ItemEntry)db.GetEntry(result));
                    if (iEntry != null && iEntry.Meta) {
                        result = iEntry.Rnd;
                    }
                }
                
                return result;
            }
        }

        public bool Ware
        {
            get {
                return ItmKind != ItemKind.ik_Coin;
            }
        }

        public bool Meta
        {
            get {
                return Flags.Contains(ItemFlags.if_IsMeta);
            }
        }

        public bool Unique
        {
            get {
                return Flags.Contains(ItemFlags.if_IsUnique);
            }
        }

        public bool Countable
        {
            get {
                return Flags.Contains(ItemFlags.if_IsCountable);
            }
        }

        public override void LoadXML(XmlNode element, FileVersion version)
        {
            try {
                base.LoadXML(element, version);

                ImageName = ReadElement(element, "ImageName");
                ItmKind = (ItemKind)Enum.Parse(typeof(ItemKind), ReadElement(element, "Kind"));

                string signs = ReadElement(element, "Signs");
                Flags = new ItemFlags(signs);
                string newSigns = Flags.Signature;
                if (!signs.Equals(newSigns)) {
                    throw new Exception("ItemSigns not equals (" + ImageName + ")");
                }

                EqKind = (BodypartType)Enum.Parse(typeof(BodypartType), ReadElement(element, "eqKind"));
                Frequency = Convert.ToSByte(ReadElement(element, "Frequency"));
                Satiety = Convert.ToInt16(ReadElement(element, "Satiety"));
                Price = Convert.ToInt16(ReadElement(element, "Price"));
                Weight = (float)ConvertHelper.ParseFloat(ReadElement(element, "Weight"), 0.0f, true);

                XmlNode ael = element.SelectSingleNode("Attributes");
                for (int i = ItemAttribute.ia_First; i <= ItemAttribute.ia_Last; i++) {
                    string atSign = dbItemAttributes[i - 1];
                    Attributes[i - 1] = Convert.ToInt32(ReadElement(ael, atSign));
                }

                XmlNodeList nl = element.SelectSingleNode("Effects").ChildNodes;
                Effects = new EffectEntry[nl.Count];
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode n = nl[i];
                    Effects[i] = new EffectEntry();
                    Effects[i].EffID = (EffectID)Enum.Parse(typeof(EffectID), n.Attributes["EffectID"].InnerText);
                    Effects[i].ExtData = Convert.ToInt32(n.Attributes["ExtData"].InnerText);
                }

                nl = element.SelectSingleNode("Contents").ChildNodes;
                Contents = new ContentsEntry[nl.Count];
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode n = nl[i];
                    Contents[i] = new ContentsEntry();
                    Contents[i].ItemID = n.Attributes["ItemID"].InnerText;
                    Contents[i].Chance = Convert.ToInt32(n.Attributes["Chance"].InnerText);
                }

                Material = (MaterialKind)Enum.Parse(typeof(MaterialKind), ReadElement(element, "Material"));

                FramesCount = Convert.ToSByte(ReadElement(element, "FramesCount"));
                BonusRange.Min = Convert.ToInt32(ReadElement(element, "BonusRange_Min"));
                BonusRange.Max = Convert.ToInt32(ReadElement(element, "BonusRange_Max"));
            } catch (Exception ex) {
                Logger.Write("ItemEntry.loadXML(): " + ex.Message);
                throw ex;
            }
        }
    }

}