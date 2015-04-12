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
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class LayerEntry extends DataEntry
{
    public int W;
    public int H;
    public int MSX;
    public int MSY;
    public String IconsName;

    public int IconsIndex;

    public LayerEntry(Object owner)
    {
        super(owner);
    }

    public final FieldEntry getFieldEntry(int X, int Y)
    {
        FieldEntry result = null;
        if (X >= 0 && X < this.W && Y >= 0 && Y < this.H) {
            String temp = super.Sign.substring(6);
            temp = "Field_" + temp + String.valueOf(X) + String.valueOf(Y);

            result = (FieldEntry) ((NWDatabase) super.Owner).findEntryBySign(temp);
        }
        return result;
    }

    @Override
    public void loadXML(Element element, FileVersion version) throws XMLStreamException
    {
        try {
            super.loadXML(element, version);

            this.W = (Integer.parseInt(DataEntry.readElement(element, "W")));
            this.H = (Integer.parseInt(DataEntry.readElement(element, "H")));
            this.MSX = (Integer.parseInt(DataEntry.readElement(element, "MSX")));
            this.MSY = (Integer.parseInt(DataEntry.readElement(element, "MSY")));
            this.IconsName = DataEntry.readElement(element, "IconsName");
        } catch (Exception ex) {
            Logger.write("LayerEntry.loadXML(): " + ex.getMessage());
            throw ex;
        }
    }
}
