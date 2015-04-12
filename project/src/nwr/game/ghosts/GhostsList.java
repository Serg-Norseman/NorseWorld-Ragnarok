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
package nwr.game.ghosts;

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
 * @since 0.9.0
 */
public final class GhostsList extends BaseObject
{
    public static final FileHeader RGL_Header = new FileHeader(new char[]{'R', 'G', 'L'}, new FileVersion(1, 0));

    private ExtList<Ghost> fList;

    public GhostsList()
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

    public final Ghost getGhost(int index)
    {
        Ghost result = null;
        if (index >= 0 && index < this.fList.getCount()) {
            result = this.fList.get(index);
        }
        return result;
    }

    public final int getGhostCount()
    {
        return this.fList.getCount();
    }

    public final void add(Ghost ghost)
    {
        this.fList.add(ghost);
    }

    public final void clear()
    {
        this.fList.clear();
    }

    public final void delete(int index)
    {
        this.fList.delete(index);
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
                        Ghost ghost = new Ghost(null, null);
                        ghost.loadFromStream(dis, header.Version);
                        this.add(ghost);
                    }
                } finally {
                    fs.close();
                }
            }
        } catch (IOException ex) {
            Logger.write("GhostsList.load(): " + ex.getMessage());
        }
    }

    public final void save(String fileName)
    {
        try {
            FileOutputStream fs = new FileOutputStream(fileName, false);
            try (BinaryOutputStream dos = new BinaryOutputStream(fs, AuxUtils.binEndian)) {
                GhostsList.RGL_Header.write(dos);

                int cnt = this.fList.getCount();
                StreamUtils.writeInt(dos, cnt);
                for (int i = 0; i < cnt; i++) {
                    Ghost ghost = this.fList.get(i);
                    ghost.saveToStream(dos, null);
                }
            } finally {
                fs.close();
            }
        } catch (IOException ex) {
            Logger.write("GhostsList.save(): " + ex.getMessage());
        }
    }
}
