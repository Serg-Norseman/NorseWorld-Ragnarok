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
package nwr.engine;

import jzrlib.core.BaseObject;
import jzrlib.utils.Logger;
import jzrlib.utils.TextUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Archive extends BaseObject
{
    private ZipFile fZipFile;
    private final String fArchiveName;
    private final HashMap<String, String> fNames;

    public Archive(String archiveName)
    {
        this.fArchiveName = archiveName;
        this.fZipFile = null;
        this.fNames = new HashMap<>();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            //this.fZipFile.close();
        }
        super.dispose(disposing);
    }

    public final InputStream extractFile(String fileName)
    {
        try {
            ZipEntry entry = this.getEntry(fileName);
            if (entry != null) {
                return new BufferedInputStream(this.fZipFile.getInputStream(entry));
            } else {
                return null;
            }
        } catch (IOException ex) {
            Logger.write("Archive.extractFile(" + fileName + "): " + ex.getMessage());
            return null;
        }
    }

    public final boolean isExist(String fileName)
    {
        ZipEntry entry = this.getEntry(fileName);
        return (entry != null);
    }

    private ZipEntry getEntry(String fileName)
    {
        if (TextUtils.isNullOrEmpty(fileName)) {
            return null;
        }

        String entryName = this.fNames.get(fileName.toLowerCase());
        if (entryName == null) {
            return null;
        } else {
            return this.fZipFile.getEntry(entryName);
        }
    }

    public final void unpack()
    {
        try {
            if ((new java.io.File(fArchiveName)).isFile()) {
                this.fZipFile = new ZipFile(fArchiveName);

                // optimize
                Enumeration<? extends ZipEntry> entries = this.fZipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    this.fNames.put(entry.getName().toLowerCase(), entry.getName());
                }
            }
        } catch (IOException ex) {
            this.fZipFile = null;
        }
    }
}
