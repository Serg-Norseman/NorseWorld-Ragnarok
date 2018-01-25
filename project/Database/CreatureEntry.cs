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
using BSLib;
using NWR.Core;
using NWR.Core.Types;
using ZRLib.Core;

namespace NWR.Database
{
    public sealed class CreatureEntry : VolatileEntry
    {
        public sealed class InventoryEntry
        {
            public string ItemSign;
            public int CountMin;
            public int CountMax;
            public ItemState State;
            public int Bonus;
        }

        public RaceID Race;
        public string Gfx;
        public string Sfx;
        public CreatureFlags Flags;
        public short MinHP;
        public short MaxHP;
        public short AC;
        public short ToHit;
        public sbyte Speed;
        public sbyte Attacks;
        public short Constitution;
        public short Strength;
        public ushort Dexterity;
        public short MinDB;
        public short MaxDB;
        public byte Survey;
        public string[] Lands;
        public sbyte Level;
        public Alignment Alignment;
        public CreatureSex Sex;
        public float Weight;

        public bool Extinctable;

        public int FleshEffect;
        public short FleshSatiety;
        public AttributeList Abilities;
        public AttributeList Skills;
        public InventoryEntry[] Inventory;
        public char Symbol;
        public byte Hear;
        public byte Smell;
        public byte Perception;
        // deprecated???
        public DialogEntry Dialog;
        public byte FramesCount;

        public byte FramesLoaded;
        public int ImageIndex;
        public int GrayImageIndex;

        public CreatureEntry(object owner)
            : base(owner)
        {
            Lands = new string[0];
            Inventory = new InventoryEntry[0];
            Abilities = new AttributeList();
            Skills = new AttributeList();
            Dialog = new DialogEntry();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                Abilities.Dispose();
                Skills.Dispose();
                Dialog.Dispose();
                Inventory = null;
                Lands = null;
            }
            base.Dispose(disposing);
        }

        public bool Respawn
        {
            get {
                return Flags.Contains(CreatureFlags.esRespawn);
            }
        }

        public bool CorpsesPersist
        {
            get {
                return Flags.Contains(CreatureFlags.esCorpsesPersist);
            }
        }

        public bool WithoutCorpse
        {
            get {
                return Flags.Contains(CreatureFlags.esWithoutCorpse);
            }
        }

        public bool Remains
        {
            get {
                return Flags.Contains(CreatureFlags.esRemains);
            }
        }

        public override void LoadXML(XmlNode element, FileVersion version)
        {
            try {
                base.LoadXML(element, version);

                Race = (RaceID)Enum.Parse(typeof(RaceID), ReadElement(element, "Race"));
                Gfx = ReadElement(element, "gfx");
                Sfx = ReadElement(element, "sfx");

                string signs = ReadElement(element, "Signs");
                Flags = new CreatureFlags(signs);
                if (!signs.Equals(Flags.Signature)) {
                    throw new Exception("CreatureSigns not equals " + Convert.ToString(GUID));
                }

                MinHP = Convert.ToInt16(ReadElement(element, "minHP"));
                MaxHP = Convert.ToInt16(ReadElement(element, "maxHP"));
                AC = Convert.ToInt16(ReadElement(element, "AC"));
                Speed = Convert.ToSByte(ReadElement(element, "Speed"));
                ToHit = Convert.ToInt16(ReadElement(element, "ToHit"));
                Attacks = Convert.ToSByte(ReadElement(element, "Attacks"));
                Constitution = Convert.ToInt16(ReadElement(element, "Constitution"));
                Strength = Convert.ToInt16(ReadElement(element, "Strength"));
                MinDB = Convert.ToInt16(ReadElement(element, "minDB"));
                MaxDB = Convert.ToInt16(ReadElement(element, "maxDB"));
                Survey = Convert.ToByte(ReadElement(element, "Survey"));
                Level = Convert.ToSByte(ReadElement(element, "Level"));
                Alignment = (Alignment)Enum.Parse(typeof(Alignment), ReadElement(element, "Alignment"));
                Weight = (float)ConvertHelper.ParseFloat(ReadElement(element, "Weight"), 0.0f, true);
                Sex = StaticData.GetSexBySign(ReadElement(element, "Sex"));

                FleshEffect = Convert.ToInt32(ReadElement(element, "FleshEffect"));
                FleshSatiety = Convert.ToInt16(ReadElement(element, "FleshSatiety"));

                string sym = ReadElement(element, "Symbol");
                Symbol = (string.IsNullOrEmpty(sym) ? '?' : sym[0]);
                Extinctable = Convert.ToBoolean(ReadElement(element, "Extinctable"));
                Dexterity = Convert.ToUInt16(ReadElement(element, "Dexterity"));
                Hear = Convert.ToByte(ReadElement(element, "Hear"));
                Smell = Convert.ToByte(ReadElement(element, "Smell"));
                FramesCount = Convert.ToByte(ReadElement(element, "FramesCount"));

                XmlNodeList nl = element.SelectSingleNode("Lands").ChildNodes;
                Lands = new string[nl.Count];
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode n = nl[i];
                    Lands[i] = n.Attributes["ID"].InnerText;
                }

                nl = element.SelectSingleNode("Abilities").ChildNodes;
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode n = nl[i];
                    AbilityID ab = (AbilityID)Enum.Parse(typeof(AbilityID), n.Attributes["ID"].InnerText);
                    int val = Convert.ToInt32(n.Attributes["Value"].InnerText);
                    Abilities.Add((int)ab, val);
                }

                nl = element.SelectSingleNode("Skills").ChildNodes;
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode n = nl[i];
                    SkillID sk = (SkillID)Enum.Parse(typeof(SkillID), n.Attributes["ID"].InnerText);
                    int val = Convert.ToInt32(n.Attributes["Value"].InnerText);
                    Skills.Add((int)sk, val);
                }

                nl = element.SelectSingleNode("Inventory").ChildNodes;
                Inventory = new InventoryEntry[nl.Count];
                for (int i = 0; i < nl.Count; i++) {
                    XmlNode n = nl[i];

                    InventoryEntry invEntry = new InventoryEntry();
                    Inventory[i] = invEntry;
                    invEntry.ItemSign = n.Attributes["ID"].InnerText;
                    invEntry.CountMin = Convert.ToInt32(n.Attributes["CountMin"].InnerText);
                    invEntry.CountMax = Convert.ToInt32(n.Attributes["CountMax"].InnerText);

                    XmlAttribute stat = n.Attributes["Status"];
                    if (stat != null)
                        ParseStatus(invEntry, stat.InnerText);
                }

                XmlNodeList dnl = element.SelectNodes("Dialog");
                if (dnl.Count > 0) {
                    XmlNode dialogXmlNode = (XmlNode)dnl[0];
                    Dialog.LoadXML(dialogXmlNode, version, true);
                }
            } catch (Exception ex) {
                Logger.Write("CreatureEntry.loadXML(): " + ex.Message);
                throw ex;
            }
        }

        public bool IsInhabitant(string landSign)
        {
            foreach (string land in Lands) {
                if ((land == landSign)) {
                    return true;
                }
            }
            return false;
        }

        private static void ParseStatus(InventoryEntry invEntry, string statusValue)
        {
            if (string.IsNullOrEmpty(statusValue)) {
                invEntry.State = ItemState.is_Normal;
                return;
            }

            string val = statusValue;
            try {
                ItemState state = ItemState.is_Normal;
                if (val.StartsWith(StaticData.dbItemStates[(int)ItemState.is_Blessed].Sign)) {
                    state = ItemState.is_Blessed;
                    val = val.Substring(StaticData.dbItemStates[(int)ItemState.is_Blessed].Sign.Length);
                } else if (val.StartsWith(StaticData.dbItemStates[(int)ItemState.is_Cursed].Sign)) {
                    state = ItemState.is_Cursed;
                    val = val.Substring(StaticData.dbItemStates[(int)ItemState.is_Cursed].Sign.Length);
                }

                int bonus = 0;
                if (val.Length > 0) {
                    bonus = Convert.ToInt32(val);
                }

                invEntry.State = state;
                invEntry.Bonus = bonus;
            } catch (Exception ex) {
                Logger.Write("CreatureEntry.parseStatus(" + statusValue + "): " + ex.Message);
            }
        }
    }

}