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
import nwr.core.types.MaterialKind;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class MaterialEntry extends VolatileEntry
{
    public MaterialKind MatKind;

    public MaterialEntry(Object owner)
    {
        super(owner);
        this.MatKind = MaterialKind.mk_None;
    }

    @Override
    public void loadXML(Element element, FileVersion version) throws XMLStreamException
    {
        try {
            super.loadXML(element, version);

            this.MatKind = MaterialKind.forValue(Integer.parseInt(DataEntry.readElement(element, "MatKind")));
        } catch (Exception ex) {
            Logger.write("MaterialEntry.loadXML(): " + ex.getMessage());
            throw ex;
        }
    }
}
