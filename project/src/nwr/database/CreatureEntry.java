/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
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
package nwr.database;

import nwr.core.AttributeList;
import jzrlib.core.FileVersion;
import jzrlib.utils.Logger;
import jzrlib.utils.TextUtils;
import nwr.core.types.AbilityID;
import nwr.core.types.AlignmentEx;
import jzrlib.common.CreatureSex;
import nwr.core.types.ItemState;
import nwr.core.types.RaceID;
import nwr.core.types.SkillID;
import javax.xml.stream.XMLStreamException;
import nwr.core.StaticData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class CreatureEntry extends VolatileEntry
{
    public static final class InventoryEntry
    {
        public String ItemSign;
        public int CountMin;
        public int CountMax;
        public ItemState State;
        public int Bonus;
    }

    public RaceID Race;
    public String gfx;
    public String sfx;
    public CreatureFlags Flags;
    public short minHP;
    public short maxHP;
    public short AC;
    public short ToHit;
    public byte Speed;
    public byte Attacks;
    public short Constitution;
    public short Strength;
    public short Dexterity;
    public short minDB;
    public short maxDB;
    public byte Survey;
    public String[] Lands;
    public byte Level;
    public AlignmentEx Alignment;
    public CreatureSex Sex;
    public float Weight;

    public boolean Extinctable;

    public int FleshEffect;
    public short FleshSatiety;
    public AttributeList Abilities;
    public AttributeList Skills;
    public InventoryEntry[] Inventory;
    public char Symbol;
    public byte Hear;
    public byte Smell;
    public byte Perception; // deprecated???
    public DialogEntry Dialog;
    public byte FramesCount;

    public byte FramesLoaded;
    public int ImageIndex;
    public int GrayImageIndex;

    public CreatureEntry(Object owner)
    {
        super(owner);
        this.Lands = new String[0];
        this.Inventory = new InventoryEntry[0];
        this.Abilities = new AttributeList();
        this.Skills = new AttributeList();
        this.Dialog = new DialogEntry();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.Abilities.dispose();
            this.Skills.dispose();
            this.Dialog.dispose();
            this.Inventory = null;
            this.Lands = null;
        }
        super.dispose(disposing);
    }

    public boolean isRespawn()
    {
        return this.Flags.contains(CreatureFlags.esRespawn);
    }

    public boolean isCorpsesPersist()
    {
        return this.Flags.contains(CreatureFlags.esCorpsesPersist);
    }

    public boolean isWithoutCorpse()
    {
        return this.Flags.contains(CreatureFlags.esWithoutCorpse);
    }

    public boolean isRemains()
    {
        return this.Flags.contains(CreatureFlags.esRemains);
    }
    
    @Override
    public void loadXML(Element element, FileVersion version) throws XMLStreamException
    {
        try {
            super.loadXML(element, version);

            this.Race = RaceID.valueOf(DataEntry.readElement(element, "Race"));
            this.gfx = DataEntry.readElement(element, "gfx");
            this.sfx = DataEntry.readElement(element, "sfx");

            String signs = DataEntry.readElement(element, "Signs");
            this.Flags = new CreatureFlags(signs);
            if (!signs.equals(this.Flags.getSignature())) {
                throw new RuntimeException("CreatureSigns not equals " + String.valueOf(this.GUID));
            }

            this.minHP = Short.parseShort(DataEntry.readElement(element, "minHP"));
            this.maxHP = Short.parseShort(DataEntry.readElement(element, "maxHP"));
            this.AC = Short.parseShort(DataEntry.readElement(element, "AC"));
            this.Speed = Byte.parseByte(DataEntry.readElement(element, "Speed"));
            this.ToHit = Short.parseShort(DataEntry.readElement(element, "ToHit"));
            this.Attacks = Byte.parseByte(DataEntry.readElement(element, "Attacks"));
            this.Constitution = Short.parseShort(DataEntry.readElement(element, "Constitution"));
            this.Strength = Short.parseShort(DataEntry.readElement(element, "Strength"));
            this.minDB = Short.parseShort(DataEntry.readElement(element, "minDB"));
            this.maxDB = Short.parseShort(DataEntry.readElement(element, "maxDB"));
            this.Survey = Byte.parseByte(DataEntry.readElement(element, "Survey"));

            NodeList nl = element.getElementsByTagName("Lands");
            nl = ((Element) nl.item(0)).getElementsByTagName("Land"); //getChildNodes();
            this.Lands = new String[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    this.Lands[i] = el.getAttribute("ID");
                }
            }

            this.Level = Byte.parseByte(DataEntry.readElement(element, "Level"));

            nl = element.getElementsByTagName("Abilities");
            nl = ((Element) nl.item(0)).getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;

                    AbilityID ab = AbilityID.valueOf(el.getAttribute("ID"));
                    int id = ab.getValue();
                    int val = Integer.parseInt(el.getAttribute("Value"));
                    this.Abilities.add(id, val);
                }
            }

            nl = element.getElementsByTagName("Skills");
            nl = ((Element) nl.item(0)).getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;

                    SkillID sk = SkillID.valueOf(el.getAttribute("ID"));
                    int id = sk.getValue();
                    int val = Integer.parseInt(el.getAttribute("Value"));
                    this.Skills.add(id, val);
                }
            }

            this.Alignment = AlignmentEx.valueOf(DataEntry.readElement(element, "Alignment"));
            this.Sex = StaticData.getSexBySign(DataEntry.readElement(element, "Sex"));
            this.Weight = Float.parseFloat(DataEntry.readElement(element, "Weight"));

            nl = element.getElementsByTagName("Inventory");
            nl = ((Element) nl.item(0)).getElementsByTagName("Item"); //getChildNodes();
            this.Inventory = new InventoryEntry[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    InventoryEntry invEntry = new InventoryEntry();
                    this.Inventory[i] = invEntry;
                    invEntry.ItemSign = el.getAttribute("ID");
                    invEntry.CountMin = Integer.parseInt(el.getAttribute("CountMin"));
                    invEntry.CountMax = Integer.parseInt(el.getAttribute("CountMax"));

                    CreatureEntry.parseStatus(invEntry, el.getAttribute("Status"));
                }
            }

            this.FleshEffect = Integer.parseInt(DataEntry.readElement(element, "FleshEffect"));
            this.FleshSatiety = Short.parseShort(DataEntry.readElement(element, "FleshSatiety"));

            String sym = DataEntry.readElement(element, "Symbol");
            this.Symbol = (TextUtils.isNullOrEmpty(sym) ? '?' : sym.charAt(0));

            NodeList dnl = element.getElementsByTagName("Dialog");
            if (dnl.getLength() > 0) {
                Element dialogElement = (Element) dnl.item(0);
                this.Dialog.loadXML(dialogElement, version, true);
            }

            this.Extinctable = Boolean.parseBoolean(DataEntry.readElement(element, "Extinctable"));
            this.Dexterity = Short.parseShort(DataEntry.readElement(element, "Dexterity"));
            this.Hear = Byte.parseByte(DataEntry.readElement(element, "Hear"));
            this.Smell = Byte.parseByte(DataEntry.readElement(element, "Smell"));
            this.FramesCount = Byte.parseByte(DataEntry.readElement(element, "FramesCount"));
        } catch (Exception ex) {
            Logger.write("CreatureEntry.loadXML(): " + ex.getMessage());
            throw ex;
        }
    }

    public final boolean isInhabitant(String landSign)
    {
        for (String land : this.Lands) {
            if (TextUtils.equals(land, landSign)) {
                return true;
            }
        }
        return false;
    }

    private static void parseStatus(InventoryEntry invEntry, String statusValue)
    {
        if (TextUtils.isNullOrEmpty(statusValue)) {
            invEntry.State = ItemState.is_Normal;
            return;
        }

        String val = statusValue;
        try {
            ItemState state = ItemState.is_Normal;
            if (val.startsWith(ItemState.is_Blessed.Sign)) {
                state = ItemState.is_Blessed;
                val = val.substring(ItemState.is_Blessed.Sign.length());
            } else if (val.startsWith(ItemState.is_Cursed.Sign)) {
                state = ItemState.is_Cursed;
                val = val.substring(ItemState.is_Cursed.Sign.length());
            }

            int bonus = 0;
            if (val.length() > 0) {
                bonus = Integer.parseInt(val);
            }

            invEntry.State = state;
            invEntry.Bonus = bonus;
        } catch (Exception ex) {
            Logger.write("CreatureEntry.parseStatus(" + statusValue + "): " + ex.getMessage());
        }
    }
}
