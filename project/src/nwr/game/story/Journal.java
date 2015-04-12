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
package nwr.game.story;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import jzrlib.core.BaseObject;
import jzrlib.core.FileVersion;
import jzrlib.core.StringList;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.StreamUtils;
import nwr.core.Attribute;
import nwr.core.AttributeList;
import nwr.core.FileHeader;
import nwr.core.Locale;
import nwr.core.NWDateTime;
import nwr.core.RS;
import nwr.database.CreatureEntry;
import nwr.engine.BaseScreen;
import jzrlib.grammar.Case;
import jzrlib.grammar.Number;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public final class Journal extends BaseObject
{
    public static final FileHeader RSJ_Header = new FileHeader(new char[]{'R', 'S', 'J'}, new FileVersion(1, 0));

    private ArrayList<JournalItem> fMessages;
    private AttributeList fEnemies;

    /**
     * Creates a new instance of Journal
     */
    public Journal()
    {
        this.fMessages = new ArrayList<>();
        this.fEnemies = new AttributeList();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fMessages.clear();
            this.fMessages = null;

            this.fEnemies.dispose();
            this.fEnemies = null;
        }
        super.dispose(disposing);
    }

    public final int getCount()
    {
        return this.fMessages.size();
    }

    public final JournalItem getItem(int index)
    {
        return this.fMessages.get(index);
    }

    public final void clear()
    {
        this.fMessages.clear();
        this.fEnemies.clear();
    }

    public void storeMessage(int type, String text)
    {
        storeMessage(type, text, JournalItem.DEFAULT_COLOR);
    }

    public void storeMessage(int type, String text, int color)
    {
        JournalItem m = new JournalItem(type, text, color, JournalItem.DEFAULT_TURN);
        fMessages.add(m);
    }

    public void storeMessage(int type, String text, int color, int turn)
    {
        JournalItem m = new JournalItem(type, text, color, turn);
        fMessages.add(m);
    }

    public void storeMessage(int type, String text, int color, NWDateTime dateTime)
    {
        JournalItem m = new JournalItem(type, text, color, dateTime);
        fMessages.add(m);
    }

    public final void storeTime(NWDateTime time)
    {
        this.storeMessage(JournalItem.SIT_DAY, "    <" + time.toString(false, false) + ">", BaseScreen.clSkyBlue, time.clone());
    }

    public final void load(String fileName)
    {
        try {
            this.clear();

            if ((new java.io.File(fileName)).isFile()) {
                FileInputStream fs = new FileInputStream(fileName);
                try (BinaryInputStream dis = new BinaryInputStream(fs, AuxUtils.binEndian)) {
                    FileHeader header = new FileHeader();
                    header.read(dis);

                    int cnt = StreamUtils.readInt(dis);
                    for (int i = 0; i < cnt; i++) {
                        JournalItem m = new JournalItem();
                        m.loadFromStream(dis, header.Version);
                        this.fMessages.add(m);
                    }

                    if (dis.available() == 0) {
                        Logger.write("journalLoad(): ok");
                    } else {
                        Logger.write("journalLoad(): fail");
                    }
                } finally {
                    fs.close();
                }
            }
        } catch (IOException ex) {
            Logger.write("Journal.load(): " + ex.getMessage());
        }
    }

    public final void save(String fileName)
    {
        try {
            FileOutputStream fs = new FileOutputStream(fileName, false);
            try (BinaryOutputStream dos = new BinaryOutputStream(fs, AuxUtils.binEndian)) {
                Journal.RSJ_Header.write(dos);

                int cnt = this.fMessages.size();
                StreamUtils.writeInt(dos, cnt);
                for (int i = 0; i < cnt; i++) {
                    JournalItem m = this.fMessages.get(i);
                    m.saveToStream(dos, Journal.RSJ_Header.Version);
                }
            } finally {
                fs.close();
            }
        } catch (IOException ex) {
            Logger.write("Journal.save(): " + ex.getMessage());
        }
    }

    public final void generateStats(StringList lines)
    {
        int num = this.fEnemies.getCount();
        if (num == 0) {
            return;
        }

        lines.add("");
        lines.addObject("    " + Locale.getStr(RS.rs_Killed), new JournalItem(JournalItem.SIT_KILLED, "", BaseScreen.clRed, JournalItem.DEFAULT_TURN));

        for (int i = 0; i < num; i++) {
            Attribute attr = this.fEnemies.getItem(i);
            int id = attr.aID;
            int val = attr.aValue;

            CreatureEntry ce = (CreatureEntry) GlobalVars.nwrBase.getEntry(id);
            lines.add("  " + ce.getNounDeclension(Number.nSingle, Case.cNominative) + ": " + String.valueOf(val));
        }
    }

    public final void killed(int enemyID)
    {
        this.fEnemies.setValue(enemyID, this.fEnemies.getValue(enemyID) + 1);
    }
}
