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
import nwr.core.FileHeader;
import jzrlib.utils.Logger;
import nwr.engine.ResourceManager;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class NWDatabase extends BaseObject
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
        this.fEntries = null;
        this.fEntriesCount = 0;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.clear();
        }
        super.dispose(disposing);
    }

    public final void clear()
    {
        int num = this.fEntriesCount;
        for (int id = 0; id < num; id++) {
            if (this.fEntries[id] != null) {
                this.fEntries[id].dispose();
            }
        }

        this.fEntries = null;
        this.fEntriesCount = 0;
    }

    public final int getEntriesCount()
    {
        return this.fEntriesCount;
    }

    public final DataEntry getEntry(int id)
    {
        if (id >= 0 && id < this.fEntriesCount) {
            return this.fEntries[id];
        } else {
            return null;
        }
    }

    private DataEntry createEntry(byte kind)
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

    public final void LoadXML(String fileName)
    {
        try {
            FileHeader header = new FileHeader();
            this.clear();

            InputStream is = ResourceManager.loadStream(fileName);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            Element root = doc.getDocumentElement();
            if (!root.getTagName().equals("RDB")) {
                throw new RuntimeException("Its not RDB!");
            }

            NodeList nl = root.getElementsByTagName("Version");
            if (nl.getLength() != 1) {
                //throw new RuntimeException("Not found version!");
            }
            Element vers = (Element) nl.item(0);

            nl = root.getElementsByTagName("Entries");
            if (nl.getLength() != 1) {
                //throw new RuntimeException("Not found entries!");
            }
            Element entries = (Element) nl.item(0);

            this.fEntriesCount = Integer.parseInt(entries.getAttribute("Count"));
            this.fEntries = new DataEntry[this.fEntriesCount];

            nl = entries.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    if (el.getTagName().equals("Entry")) {
                        String val = "";
                        try {
                            byte kind = DataEntry.parseEntryKind(el.getAttribute("Kind"));
                            int id = Integer.parseInt(el.getAttribute("GUID"));

                            DataEntry entry = null;
                            if (kind != DataEntry.ek_Unknown) {
                                entry = this.createEntry(kind);
                                entry.Kind = kind;
                                entry.loadXML(el, header.Version);
                            }
                            this.fEntries[id] = entry;
                        } catch (Exception ex) {
                            Logger.write("LoadXML.1("+val+"): " + ex.getMessage());
                        }
                    }
                }
            }

            Logger.write("dbLoad(): ok");
        } catch (SAXParseException ex) {
            Logger.write("LoadXML.sax(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("LoadXML(): " + ex.getMessage());
        }
    }

    public final DataEntry addEntry(byte kind)
    {
        if (kind == DataEntry.ek_Unknown) {
            throw new RuntimeException("NWR-DB entry kind is invalid");
        }
        this.fEntriesCount++;

        int newCount = this.fEntriesCount;
        DataEntry[] srcArray = this.fEntries;
        DataEntry[] destArray = new DataEntry[newCount];
        if (newCount > 0 && srcArray != null) {
            int oldCount = srcArray.length;
            if (oldCount > newCount) {
                oldCount = newCount;
            }
            if (oldCount > 0) {
                System.arraycopy(srcArray, 0, destArray, 0, oldCount);
            }
        }
        this.fEntries = destArray;

        int id = this.fEntriesCount - 1;

        DataEntry result = this.createEntry(kind);
        result.GUID = id;
        result.Kind = kind;
        this.fEntries[id] = result;

        return result;
    }

    public final DataEntry findEntryBySign(String sign)
    {
        int num = this.fEntriesCount;
        for (int id = 0; id < num; id++) {
            DataEntry entry = this.fEntries[id];
            if (entry != null && entry.Sign.compareTo(sign) == 0) {
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
