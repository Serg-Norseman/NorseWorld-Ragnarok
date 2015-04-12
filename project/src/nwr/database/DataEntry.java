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

import jzrlib.core.BaseObject;
import jzrlib.core.FileVersion;
import jzrlib.utils.Logger;
import jzrlib.utils.TextUtils;
import jzrlib.grammar.Case;
import jzrlib.grammar.Number;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class DataEntry extends BaseObject
{
    public static final byte ek_Unknown = 0;
    public static final byte ek_Item = 1;
    public static final byte ek_Creature = 2;
    public static final byte ek_Land = 3;
    public static final byte ek_Layer = 4;
    public static final byte ek_Field = 5;
    public static final byte ek_EventHandler = 6;
    public static final byte ek_Information = 7;
    public static final byte ek_Material = 8;

    public static final String[] EntryKinds = new String[]{
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

    private String fName;
    private String fDesc;

    public int GUID;
    public final Object Owner;

    public byte Kind;
    public String Morphology;
    public String Sign;

    public String getName()
    {
        return this.fName;
    }

    public void setName(String value)
    {
        this.fName = value;
    }

    public String getDesc()
    {
        return this.fDesc;
    }

    public void setDesc(String value)
    {
        this.fDesc = value;
    }

    public DataEntry(Object owner)
    {
        this.Owner = owner;
    }

    public final String getNounDeclension(Number number, Case ncase)
    {
        String result = "";

        try {
            if (!TextUtils.isNullOrEmpty(this.Morphology)) {
                String word = TextUtils.getToken(this.Morphology, "[]", 1);
                if (TextUtils.getTokensCount(this.Morphology, "[]") == 1) {
                    result = word;
                } else {
                    String sfx = TextUtils.getToken(this.Morphology, "[]", 2);

                    int sf_idx = ((int) (number.getValue()) - 1) * 6 + (int) (ncase.getValue());
                    String sf = TextUtils.getToken(sfx, ",", sf_idx);

                    if (sf.compareTo("") == 0) {
                        result = word;
                    } else {
                        if (sf.charAt(0) != '-') {
                            result = sf;
                        } else {
                            sf = sf.substring(1);
                            result = word + sf;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("BaseEntry.getNounDeclension(): " + ex.getMessage());
            throw ex;
        }

        return result;
    }

    public final boolean hasNounMorpheme(String sample)
    {
        return this.Morphology.contains(sample);
    }

    public void loadXML(Element element, FileVersion version) throws XMLStreamException
    {
        this.GUID = Integer.parseInt(element.getAttribute("GUID"));
        this.Sign = element.getAttribute("Sign");
        this.fName = element.getAttribute("Name");

        this.fDesc = DataEntry.readElement(element, "Desc");
    }

    public static final byte parseEntryKind(String value)
    {
        int num = EntryKinds.length;
        for (byte i = 0; i < num; i++) {
            if (TextUtils.equals(EntryKinds[i], value)) {
                return i;
            }
        }
        throw new RuntimeException("unknown entry kind");
    }

    public static final String readElement(Element parentElement, String tagName)
    {
        NodeList nl = parentElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            String nodeName = node.getNodeName();
            if (node instanceof Element && nodeName.equalsIgnoreCase(tagName)) {
                String value = ((Element) node).getTextContent();
                return value;
            }
        }
        return "";
    }
}
