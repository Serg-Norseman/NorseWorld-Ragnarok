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
using System.IO;
using BSLib;
using ZRLib.Core;
using NWR.Core;

namespace NWR.Game.Scores
{
    public sealed class ScoresList : BaseObject
    {
        public static readonly FileHeader RSL_Header = new FileHeader(new char[]{ 'R', 'S', 'L' }, new FileVersion(1, 0));

        private ExtList<Score> fList;

        public ScoresList()
        {
            fList = new ExtList<Score>(true);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fList.Dispose();
                fList = null;
            }
            base.Dispose(disposing);
        }

        public Score GetScore(int index)
        {
            Score result = null;
            if (index >= 0 && index < fList.Count) {
                result = fList[index];
            }
            return result;
        }

        public int ScoreCount
        {
            get {
                return fList.Count;
            }
        }

        public void Add(byte kind, string name, string desc, int exp, int level)
        {
            Score sr = new Score();
            sr.Kind = kind;
            sr.Name = name;
            sr.Desc = desc;
            sr.Exp = exp;
            sr.Level = level;

            int i = 0;
            while (i < fList.Count && (fList[i].Exp > exp)) {
                i++;
            }
            fList.Insert(i, sr);
        }

        public void Clear()
        {
            fList.Clear();
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
                            byte kind = (byte)StreamUtils.ReadByte(dis);
                            string name = StreamUtils.ReadString(dis, StaticData.DefEncoding);
                            string desc = StreamUtils.ReadString(dis, StaticData.DefEncoding);
                            int exp = StreamUtils.ReadInt(dis);
                            int level = StreamUtils.ReadInt(dis);
                            Add(kind, name, desc, exp, level);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("ScoresList.load(): " + ex.Message);
            }
        }

        public void Save(string fileName)
        {
            try {
                FileStream fs = new FileStream(fileName, FileMode.Create);
                using (var dos = new BinaryWriter(fs)) {
                    RSL_Header.Write(dos);
                    
                    int cnt = fList.Count;
                    StreamUtils.WriteInt(dos, cnt);
                    for (int i = 0; i < cnt; i++) {
                        Score sr = fList[i];
                        
                        StreamUtils.WriteByte(dos, sr.Kind);
                        StreamUtils.WriteString(dos, sr.Name, StaticData.DefEncoding);
                        StreamUtils.WriteString(dos, sr.Desc, StaticData.DefEncoding);
                        StreamUtils.WriteInt(dos, sr.Exp);
                        StreamUtils.WriteInt(dos, sr.Level);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("ScoresList.save(): " + ex.Message);
            }
        }
    }
}
