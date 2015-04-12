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
package nwr.game.scores;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import jzrlib.core.BaseObject;
import jzrlib.core.ExtList;
import jzrlib.core.FileVersion;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.StreamUtils;
import nwr.core.FileHeader;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ScoresList extends BaseObject
{
    public static final FileHeader RSL_Header = new FileHeader(new char[]{'R', 'S', 'L'}, new FileVersion(1, 0));

    private ExtList<Score> fList;

    public ScoresList()
    {
        this.fList = new ExtList<>(true);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fList.dispose();
            this.fList = null;
        }
        super.dispose(disposing);
    }

    public final Score getScore(int index)
    {
        Score result = null;
        if (index >= 0 && index < this.fList.getCount()) {
            result = this.fList.get(index);
        }
        return result;
    }

    public final int getScoreCount()
    {
        return this.fList.getCount();
    }

    public final void add(byte kind, String name, String desc, int exp, int level)
    {
        Score sr = new Score();
        sr.Kind = kind;
        sr.Name = name;
        sr.Desc = desc;
        sr.Exp = exp;
        sr.Level = level;

        int i = 0;
        while (i < this.fList.getCount() && (this.fList.get(i).Exp > exp)) {
            i++;
        }
        this.fList.insert(i, sr);
    }

    public final void clear()
    {
        this.fList.clear();
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
                        byte kind = (byte) StreamUtils.readByte(dis);
                        String name = StreamUtils.readString(dis);
                        String desc = StreamUtils.readString(dis);
                        int exp = StreamUtils.readInt(dis);
                        int level = StreamUtils.readInt(dis);
                        this.add(kind, name, desc, exp, level);
                    }
                } finally {
                    fs.close();
                }
            }
        } catch (IOException ex) {
            Logger.write("ScoresList.load(): " + ex.getMessage());
        }
    }

    public final void save(String fileName)
    {
        try {
            FileOutputStream fs = new FileOutputStream(fileName, false);
            try (BinaryOutputStream dos = new BinaryOutputStream(fs, AuxUtils.binEndian)) {
                ScoresList.RSL_Header.write(dos);

                int cnt = this.fList.getCount();
                StreamUtils.writeInt(dos, cnt);
                for (int i = 0; i < cnt; i++) {
                    Score sr = this.fList.get(i);

                    StreamUtils.writeByte(dos, sr.Kind);
                    StreamUtils.writeString(dos, sr.Name);
                    StreamUtils.writeString(dos, sr.Desc);
                    StreamUtils.writeInt(dos, sr.Exp);
                    StreamUtils.writeInt(dos, sr.Level);
                }
            } finally {
                fs.close();
            }
        } catch (IOException ex) {
            Logger.write("ScoresList.save(): " + ex.getMessage());
        }
    }
}
