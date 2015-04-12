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
import jzrlib.utils.TextUtils;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class DialogEntry extends BaseObject
{
    private final ExtList<ConversationEntry> fConversations;
    
    public String ExternalFile;

    public DialogEntry()
    {
        this.fConversations = new ExtList<>();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.clear();
            this.fConversations.dispose();
        }
        super.dispose(disposing);
    }

    public final ConversationEntry getConversation(int index)
    {
        ConversationEntry result = null;
        if (index >= 0 && index < this.fConversations.getCount()) {
            result = ((ConversationEntry) this.fConversations.get(index));
        }
        return result;
    }

    public final int getConversationsCount()
    {
        return this.fConversations.getCount();
    }

    public final ConversationEntry addConversation()
    {
        ConversationEntry result = new ConversationEntry();
        this.fConversations.add(result);
        return result;
    }

    public final ConversationEntry addConversation(String name)
    {
        ConversationEntry result = new ConversationEntry(name);
        this.fConversations.add(result);
        return result;
    }

    public final void clear()
    {
        for (int i = this.fConversations.getCount() - 1; i >= 0; i--) {
            this.deleteConversation(i);
        }
        this.fConversations.clear();
    }

    public final void deleteConversation(int index)
    {
        ((ConversationEntry) this.fConversations.get(index)).dispose();
        this.fConversations.delete(index);
    }

    public final int indexOf(ConversationEntry convers)
    {
        return this.fConversations.indexOf(convers);
    }

    public void loadXML(Element element, FileVersion version, boolean dbSource) throws XMLStreamException
    {
        this.clear();

        if (element != null) {
            if (dbSource) {
                this.ExternalFile = element.getAttribute("ExtFile");
            }

            if (!dbSource || TextUtils.isNullOrEmpty(this.ExternalFile)) {
                NodeList nl = element.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node = nl.item(i);
                    if (node instanceof Element) {
                        Element el = (Element) node;
                        if (el.getTagName().equals("Conversation")) {
                            this.addConversation().loadXML(el, version);
                        }
                    }
                }
            }
        }
    }
}
