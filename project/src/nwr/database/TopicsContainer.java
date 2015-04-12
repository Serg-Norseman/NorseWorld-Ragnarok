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
import jzrlib.core.ExtList;
import jzrlib.core.FileVersion;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public class TopicsContainer extends BaseObject
{
    private final ExtList<TopicEntry> fTopics;

    public TopicsContainer()
    {
        this.fTopics = new ExtList<>();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.clear();
            this.fTopics.dispose();
        }
        super.dispose(disposing);
    }

    public final TopicEntry getTopic(int index)
    {
        TopicEntry result = null;
        if (index >= 0 && index < this.fTopics.getCount()) {
            result = ((TopicEntry) this.fTopics.get(index));
        }
        return result;
    }

    public final int getTopicsCount()
    {
        return this.fTopics.getCount();
    }

    public final TopicEntry addTopic()
    {
        TopicEntry result = new TopicEntry();
        this.fTopics.add(result);
        return result;
    }

    public final void clear()
    {
        for (int i = this.fTopics.getCount() - 1; i >= 0; i--) {
            this.deleteTopic(i);
        }
        this.fTopics.clear();
    }

    public final void deleteTopic(int index)
    {
        ((TopicEntry) this.fTopics.get(index)).dispose();
        this.fTopics.delete(index);
    }

    public final int indexOf(TopicEntry topic)
    {
        return this.fTopics.indexOf(topic);
    }

    public void loadXML(Element element, FileVersion version) throws XMLStreamException
    {
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element el = (Element) node;
                if (el.getTagName().equalsIgnoreCase("Topic")) {
                    this.addTopic().loadXML(el, version);
                }
            }
        }
    }
}
