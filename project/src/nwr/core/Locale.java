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
package nwr.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jzrlib.core.BaseLocale;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.TextUtils;
import nwr.database.CreatureEntry;
import nwr.database.DataEntry;
import nwr.engine.ResourceManager;
import nwr.main.GlobalVars;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public final class Locale extends BaseLocale
{
    public static class LangRec
    {
        public String Name;
        public String Prefix;

        public LangRec(String name, String prefix)
        {
            this.Name = name;
            this.Prefix = prefix;
        }
    }

    public static final String LANGS_FOLDER = "languages\\";
    private static final String LANGS_XML = LANGS_FOLDER + "langs.xml";

    static {
        initList(RS.rs_Last + 1);
    }
    
    private ArrayList<LangRec> fLangs;

    public Locale()
    {
        this.fLangs = new ArrayList<>();
        this.loadLangs();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fLangs = null;
        }
        super.dispose(disposing);
    }

    public final LangRec getLang(int index)
    {
        if (index >= 0 && index < this.fLangs.size()) {
            return this.fLangs.get(index);
        } else {
            return null;
        }
    }

    public final int getLangsCount()
    {
        return this.fLangs.size();
    }

    public final int findLang(String langName)
    {
        for (int i = 0; i < this.fLangs.size(); i++) {
            if (TextUtils.equals(this.fLangs.get(i).Name, langName)) {
                return i;
            }
        }
        return -1;
    }

    public final boolean setLang(String name)
    {
        int idx = this.findLang(name);
        if (idx < 0) {
            return false;
        } else {
            try {
                String prefix = LANGS_FOLDER + this.fLangs.get(idx).Prefix;
                String f = ResourceManager.getAppPath() + prefix;
                this.loadLangDB(f + "_db.xml");
                this.loadLangTexts(f + "_texts.xml");
                this.loadLangDialogs(f);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    private void loadLangs()
    {
        File f = new File(ResourceManager.getAppPath() + LANGS_XML);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);

            Element root = doc.getDocumentElement();
            if (!root.getTagName().equals("langs")) {
                throw new RuntimeException("Its not langs file!");
            }

            NodeList nl = root.getElementsByTagName("lang");
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                try {
                    String name = (el.getAttribute("name"));
                    String prefix = (el.getAttribute("prefix"));

                    this.fLangs.add(new LangRec(name, prefix));
                } catch (Exception ex) {
                    Logger.write("Locale.loadLangs.1(): " + ex.getMessage());
                }
            }
        } catch (SAXParseException ex) {
            Logger.write("Locale.loadLangs.sax(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("Locale.loadLangs(): " + ex.getMessage());
        }
    }

    private void loadLangDB(String fileName)
    {
        File f = new File(fileName);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);

            Element root = doc.getDocumentElement();
            if (!root.getTagName().equals("RDB")) {
                throw new RuntimeException("Its not RDB!");
            }

            NodeList nl = root.getElementsByTagName("Entries");
            if (nl.getLength() != 1) {
                //throw new RuntimeException("Not found entries!");
            }
            Element entries = (Element) nl.item(0);
            nl = entries.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    if (el.getTagName().equals("Entry")) {
                        try {
                            int id = Integer.parseInt(el.getAttribute("ID"));
                            String name = DataEntry.readElement(el, "Name");
                            String desc = DataEntry.readElement(el, "Desc");
                            String morph = DataEntry.readElement(el, "Morphology");

                            DataEntry entry = GlobalVars.nwrBase.getEntry(id);
                            if (entry != null) {
                                entry.setName(name);

                                desc = desc.replace(AuxUtils.CRLF, " ");
                                desc = desc.replace(("" + AuxUtils.CR), " ");
                                desc = desc.replace(("" + AuxUtils.LF), " ");
                                desc = desc.replace("  ", " ");
                                entry.setDesc(desc);

                                entry.Morphology = morph;
                            }
                        } catch (Exception ex) {
                            Logger.write("Locale.loadLangDB.1(): " + ex.getMessage());
                        }
                    }
                }
            }
        } catch (SAXParseException ex) {
            Logger.write("Locale.loadLangDB.sax(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("Locale.loadLangDB(): " + ex.getMessage());
        }
    }

    private void loadLangTexts(String fileName)
    {
        try {
            File f = new File(fileName);
            super.loadLangTexts(new FileInputStream(f));
        } catch (Exception ex) {
            Logger.write("Locale.loadLangTexts(): " + ex.getMessage());
        }
    }
    
    private Element loadLangDialog(String fileName)
    {
        File f = new File(fileName);
        if (!f.isFile()) {
            return null;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);

            Element root = doc.getDocumentElement();
            return root;
        } catch (SAXParseException ex) {
            Logger.write("Locale.loadLangDialog.sax(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("Locale.loadLangDialog(): " + ex.getMessage());
        }
        return null;
    }
    
    private void loadLangDialogs(String prefix)
    {
        int num = GlobalVars.nwrBase.getEntriesCount();
        for (int i = 0; i < num; i++) {
            DataEntry entry = GlobalVars.nwrBase.getEntry(i);
            if (entry != null && entry.Kind == DataEntry.ek_Creature) {
                CreatureEntry crEntry = (CreatureEntry) entry;

                if (!TextUtils.isNullOrEmpty(crEntry.Dialog.ExternalFile)) {
                    String filename = prefix + crEntry.Dialog.ExternalFile;

                    try {
                        Element dialogRoot = this.loadLangDialog(filename);
                        // may be null, but check only in loadXML
                        crEntry.Dialog.loadXML(dialogRoot, null, false);
                    } catch (Exception ex) {
                        Logger.write("Locale.loadLangDialogs(): " + ex.getMessage());
                    }
                }
            }
        }
    }
}
