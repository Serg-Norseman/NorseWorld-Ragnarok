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

import jzrlib.core.FileVersion;
import jzrlib.utils.Logger;
import jzrlib.core.Range;
import jzrlib.core.ProbabilityTable;
import nwr.creatures.BodypartType;
import nwr.core.types.ItemAttribute;
import nwr.core.types.ItemKind;
import nwr.core.types.MaterialKind;
import nwr.effects.EffectID;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ItemEntry extends VolatileEntry
{
    private static final String[] dbItemAttributes;

    public static final class EffectEntry
    {
        public EffectID EffID;
        public int ExtData;
    }

    public static final class ContentsEntry
    {
        public String ItemID;
        public int Chance;
    }

    public String ImageName;
    public ItemKind ItmKind;
    public ItemFlags Flags;
    public BodypartType eqKind;
    public byte Frequency;
    public short Satiety;
    public short Price;
    public float Weight;
    public int[] Attributes = new int[10];
    public EffectEntry[] Effects;
    public ContentsEntry[] Contents;
    public MaterialKind Material;
    public Range BonusRange;
    public byte FramesCount;

    public int Prefix;

    public byte FramesLoaded;
    public int ImageIndex;

    static {
        dbItemAttributes = new String[]{
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

    public ItemEntry(Object owner)
    {
        super(owner);
        this.FramesLoaded = 0;
        this.BonusRange = new Range();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.Effects = null;
            this.Contents = null;
        }
        super.dispose(disposing);
    }

    public final int getRnd()
    {
        int result = -1;

        ContentsEntry[] contents = this.Contents;
        int cnt = (contents != null) ? contents.length : 0;

        if (cnt != 0) {
            NWDatabase db = (NWDatabase) super.Owner;

            ProbabilityTable<Integer> data = new ProbabilityTable();
            for (int i = 0; i < cnt; i++) {
                String sign = contents[i].ItemID;
                ItemEntry entry = ((ItemEntry) db.findEntryBySign(sign));
                int freq = (int) entry.Frequency;
                if (freq > 0) {
                    data.add(entry.GUID, freq);
                }
            }

            result = data.getRandomItem();

            ItemEntry entry = ((ItemEntry) db.getEntry(result));
            if (entry != null && entry.isMeta()) {
                result = entry.getRnd();
            }
        }

        return result;
    }

    public final boolean isWare()
    {
        return this.ItmKind != ItemKind.ik_Coin;
    }

    public final boolean isMeta()
    {
        return this.Flags.contains(ItemFlags.if_IsMeta);
    }

    public final boolean isUnique()
    {
        return this.Flags.contains(ItemFlags.if_IsUnique);
    }

    public final boolean isCountable()
    {
        return this.Flags.contains(ItemFlags.if_IsCountable);
    }

    @Override
    public void loadXML(Element element, FileVersion version) throws XMLStreamException
    {
        try {
            super.loadXML(element, version);

            this.ImageName = DataEntry.readElement(element, "ImageName");
            this.ItmKind = ItemKind.valueOf(DataEntry.readElement(element, "Kind"));
            if (this.ItmKind == null) {
                throw new RuntimeException("ItemKind unknown ("+this.ImageName+")");
            }

            String signs = DataEntry.readElement(element, "Signs");
            this.Flags = new ItemFlags(signs);
            String newSigns = this.Flags.getSignature();
            if (!signs.equals(newSigns)) {
                throw new RuntimeException("ItemSigns not equals ("+this.ImageName+")");
            }

            this.eqKind = BodypartType.valueOf(DataEntry.readElement(element, "eqKind"));
            this.Frequency = Byte.parseByte(DataEntry.readElement(element, "Frequency"));
            this.Satiety = Short.parseShort(DataEntry.readElement(element, "Satiety"));
            this.Price = Short.parseShort(DataEntry.readElement(element, "Price"));
            this.Weight = Float.parseFloat(DataEntry.readElement(element, "Weight"));

            NodeList nl = element.getElementsByTagName("Attributes");
            Element ael = ((Element) nl.item(0));
            for (int i = ItemAttribute.ia_First; i <= ItemAttribute.ia_Last; i++) {
                String atSign = dbItemAttributes[i - 1];
                this.Attributes[i - 1] = Integer.parseInt(DataEntry.readElement(ael, atSign));
            }

            nl = element.getElementsByTagName("Effects");
            nl = ((Element) nl.item(0)).getElementsByTagName("Effect"); //getChildNodes();
            this.Effects = new EffectEntry[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    this.Effects[i] = new EffectEntry();
                    this.Effects[i].EffID = EffectID.valueOf(el.getAttribute("EffectID"));
                    this.Effects[i].ExtData = Integer.parseInt(el.getAttribute("ExtData"));
                }
            }

            nl = element.getElementsByTagName("Contents");
            nl = ((Element) nl.item(0)).getElementsByTagName("Item"); //getChildNodes();
            this.Contents = new ContentsEntry[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    this.Contents[i] = new ContentsEntry();
                    this.Contents[i].ItemID = el.getAttribute("ItemID");
                    this.Contents[i].Chance = Integer.parseInt(el.getAttribute("Chance"));
                }
            }

            this.Material = MaterialKind.valueOf(DataEntry.readElement(element, "Material"));

            this.FramesCount = Byte.parseByte(DataEntry.readElement(element, "FramesCount"));
            this.BonusRange.Min = Integer.parseInt(DataEntry.readElement(element, "BonusRange_Min"));
            this.BonusRange.Max = Integer.parseInt(DataEntry.readElement(element, "BonusRange_Max"));
        } catch (Exception ex) {
            Logger.write("ItemEntry.loadXML(): " + ex.getMessage());
            throw ex;
        }
    }
}
