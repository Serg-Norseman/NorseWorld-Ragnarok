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
import nwr.core.types.GameScreen;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class LandEntry extends DeclinableEntry
{
    public GameScreen Splash;
    public LandFlags Flags;
    public String BackSFX;
    public String[] ForeSFX;
    public String Song;

    public LandEntry(Object owner)
    {
        super(owner);
    }

    @Override
    public void loadXML(Element element, FileVersion version) throws XMLStreamException
    {
        try {
            super.loadXML(element, version);

            this.Splash = GameScreen.valueOf(DataEntry.readElement(element, "Splash"));

            String signs = DataEntry.readElement(element, "Signs");
            this.Flags = new LandFlags(signs);
            if (!signs.equals(this.Flags.getSignature())) {
                throw new RuntimeException("LandSigns not equals");
            }

            this.BackSFX = (DataEntry.readElement(element, "BackSFX"));
            this.Song = (DataEntry.readElement(element, "Song"));

            NodeList nl = element.getElementsByTagName("ForeSFX");
            nl = ((Element) nl.item(0)).getElementsByTagName("ForeSFX"); //getChildNodes();
            this.ForeSFX = new String[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    this.ForeSFX[i] = el.getTextContent();
                }
            }
        } catch (Exception ex) {
            Logger.write("LandEntry.loadXML(): " + ex.getMessage());
            throw ex;
        }
    }
}
