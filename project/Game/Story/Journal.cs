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
using NWR.Core;
using NWR.Database;
using NWR.Game;
using ZRLib.Core;
using ZRLib.Engine;
using ZRLib.Grammar;

namespace NWR.Game.Story
{
    public sealed class Journal : BaseObject
    {
        public static readonly FileHeader RSJ_Header = new FileHeader(new char[]{ 'R', 'S', 'J' }, new FileVersion(1, 0));

        private List<JournalItem> fMessages;
        private AttributeList fEnemies;

        public Journal()
        {
            fMessages = new List<JournalItem>();
            fEnemies = new AttributeList();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fMessages.Clear();
                fMessages = null;

                fEnemies.Dispose();
                fEnemies = null;
            }
            base.Dispose(disposing);
        }

        public int Count
        {
            get {
                return fMessages.Count;
            }
        }

        public JournalItem GetItem(int index)
        {
            return fMessages[index];
        }

        public void Clear()
        {
            fMessages.Clear();
            fEnemies.Clear();
        }

        public void StoreMessage(int type, string text)
        {
            StoreMessage(type, text, JournalItem.DEFAULT_COLOR);
        }

        public void StoreMessage(int type, string text, int color)
        {
            JournalItem m = new JournalItem(type, text, color, JournalItem.DEFAULT_TURN);
            fMessages.Add(m);
        }

        public void StoreMessage(int type, string text, int color, int turn)
        {
            JournalItem m = new JournalItem(type, text, color, turn);
            fMessages.Add(m);
        }

        public void StoreMessage(int type, string text, int color, NWDateTime dateTime)
        {
            JournalItem m = new JournalItem(type, text, color, dateTime);
            fMessages.Add(m);
        }

        public void StoreTime(NWDateTime time)
        {
            StoreMessage(JournalItem.SIT_DAY, "    <" + time.ToString(false, false) + ">", Colors.SkyBlue, time);
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
                            JournalItem m = new JournalItem();
                            m.LoadFromStream(dis, header.Version);
                            fMessages.Add(m);
                        }
                        
                        if (dis.BaseStream.Length - dis.BaseStream.Position == 0) {
                            Logger.Write("journalLoad(): ok");
                        } else {
                            Logger.Write("journalLoad(): fail");
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Journal.load(): " + ex.Message);
            }
        }

        public void Save(string fileName)
        {
            try {
                FileStream fs = new FileStream(fileName, FileMode.CreateNew);
                using (var dos = new BinaryWriter(fs)) {
                    RSJ_Header.Write(dos);
                    
                    int cnt = fMessages.Count;
                    StreamUtils.WriteInt(dos, cnt);
                    for (int i = 0; i < cnt; i++) {
                        JournalItem m = fMessages[i];
                        m.SaveToStream(dos, RSJ_Header.Version);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Journal.save(): " + ex.Message);
            }
        }

        public void GenerateStats(StringList lines)
        {
            int num = fEnemies.Count;
            if (num == 0) {
                return;
            }

            lines.Add("");
            lines.AddObject("    " + BaseLocale.GetStr(RS.rs_Killed), new JournalItem(JournalItem.SIT_KILLED, "", Colors.Red, JournalItem.DEFAULT_TURN));

            for (int i = 0; i < num; i++) {
                Core.Attribute attr = fEnemies.GetItem(i);
                int id = attr.AID;
                int val = attr.AValue;

                CreatureEntry ce = (CreatureEntry)GlobalVars.nwrDB.GetEntry(id);
                lines.Add("  " + ce.GetNounDeclension(Number.nSingle, Case.cNominative) + ": " + Convert.ToString(val));
            }
        }

        public void Killed(int enemyID)
        {
            fEnemies.SetValue(enemyID, fEnemies.GetValue(enemyID) + 1);
        }
    }
}
