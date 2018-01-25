/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using System;
using System.IO;
using BSLib;
using ZRLib.Core;
using NWR.Core;

namespace NWR.Game.Ghosts
{
    public sealed class GhostsList : BaseObject
    {
        public static readonly FileHeader RGL_Header = new FileHeader(new char[]{ 'R', 'G', 'L' }, new FileVersion(1, 0));

        private ExtList<Ghost> fList;

        public GhostsList()
        {
            fList = new ExtList<Ghost>(true);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fList.Dispose();
                fList = null;
            }
            base.Dispose(disposing);
        }

        public Ghost GetGhost(int index)
        {
            Ghost result = null;
            if (index >= 0 && index < fList.Count) {
                result = fList[index];
            }
            return result;
        }

        public int GhostCount
        {
            get {
                return fList.Count;
            }
        }

        public void Add(Ghost ghost)
        {
            fList.Add(ghost);
        }

        public void Clear()
        {
            fList.Clear();
        }

        public void Delete(int index)
        {
            fList.Delete(index);
        }

        public void Load(string fileName)
        {
            try {
                Clear();

                if (File.Exists(fileName)) {
                    FileStream fs = new FileStream(fileName, FileMode.Open);
                    using (var dis = new BinaryReader(fs)) {
                        FileHeader header = new FileHeader();
                        header.Read(dis);
                        
                        int cnt = StreamUtils.ReadInt(dis);
                        for (int i = 0; i < cnt; i++) {
                            Ghost ghost = new Ghost(null, null);
                            ghost.LoadFromStream(dis, header.Version);
                            Add(ghost);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("GhostsList.load(): " + ex.Message);
            }
        }

        public void Save(string fileName)
        {
            try {
                FileStream fs = new FileStream(fileName, FileMode.Create);
                using (var dos = new BinaryWriter(fs)) {
                    RGL_Header.Write(dos);
                    
                    int cnt = fList.Count;
                    StreamUtils.WriteInt(dos, cnt);
                    for (int i = 0; i < cnt; i++) {
                        Ghost ghost = fList[i];
                        ghost.SaveToStream(dos, null);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("GhostsList.save(): " + ex.Message);
            }
        }
    }
}
