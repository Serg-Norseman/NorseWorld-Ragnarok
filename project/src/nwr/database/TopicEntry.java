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
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class TopicEntry extends TopicsContainer
{
    public String Condition;
    public String Phrase;
    public String Answer;
    public String Action;

    public TopicEntry()
    {
        super();
    }

    public TopicEntry(String phrase, String answer)
    {
        super();

        this.Phrase = phrase;
        this.Answer = answer;
        this.Action = "";
    }

    @Override
    public void loadXML(Element element, FileVersion version) throws XMLStreamException
    {
        this.Condition = (DataEntry.readElement(element, "Condition"));
        this.Phrase = (DataEntry.readElement(element, "Phrase"));
        this.Answer = (DataEntry.readElement(element, "Answer"));
        this.Action = (DataEntry.readElement(element, "Action"));

        super.loadXML(element, version);
    }
}
