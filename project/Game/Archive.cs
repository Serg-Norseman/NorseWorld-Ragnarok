/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih.
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

using System;
using System.Collections.Generic;
using System.IO;
using BSLib;

namespace NWR.Game
{
    public sealed class Archive : BaseObject
    {
        private ZipStorer fZipFile;
        private readonly string fArchiveName;
        private readonly Dictionary<string, string> fNames;

        public Archive(string archiveName)
        {
            fArchiveName = archiveName;
            fZipFile = null;
            fNames = new Dictionary<string, string>();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                //this.fZipFile.close();
            }
            base.Dispose(disposing);
        }

        public Stream ExtractFile(string fileName)
        {
            /*try {
                ZipStorer.ZipFileEntry entry = this.GetEntry(fileName);
                if (entry != null) {
                    return this.FZipFile.getInputStream(entry);
                } else {
                    return null;
                }
            } catch (IOException ex) {
                Logger.Write("Archive.extractFile(" + fileName + "): " + ex.Message);
                return null;
            }*/
            return null;
        }

        public bool IsExist(string fileName)
        {
            ZipStorer.ZipFileEntry entry = GetEntry(fileName);
            return (entry != null);
        }

        private ZipStorer.ZipFileEntry GetEntry(string fileName)
        {
            /*if (string.IsNullOrEmpty(fileName)) {
                return null;
            }

            string entryName = this.FNames.GetValueOrNull(fileName.ToLower());
            if (entryName == null) {
                return null;
            } else {
                return this.FZipFile.GetEntry(entryName);
            }*/
            return null;
        }

        public void Unpack()
        {
            try {
                /*if (System.IO.File.Exists(FArchiveName)) {
                    this.FZipFile = new ZipFile(FArchiveName);

                    // optimize
                    IEnumerator<?> entries = this.FZipFile.entries();
                    while (entries.MoveNext()) {
                        ZipEntry entry = entries.Current;
                        this.FNames[entry.Name.ToLower()] = entry.Name;
                    }
                }*/
            } catch (Exception) {
                fZipFile = null;
            }
        }
    }
}
