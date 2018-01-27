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

namespace NWR.Core
{
    public sealed class AttributeList : BaseObject
    {
        // List Assign Operators
        public const byte Lao_Copy = 0;
        public const byte Lao_And = 1;
        public const byte Lao_Or = 2;

        private readonly ExtList<Attribute> fList;

        public AttributeList()
        {
            fList = new ExtList<Attribute>();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                Clear();
                fList.Dispose();
            }
            base.Dispose(disposing);
        }

        public int Count
        {
            get {
                return fList.Count;
            }
        }

        public Attribute GetItem(int index)
        {
            if (index >= 0 && index < fList.Count) {
                return fList[index];
            } else {
                return null;
            }
        }

        public int GetValue(int ID)
        {
            int idx = IndexOf(ID);

            if (idx < 0) {
                return 0;
            } else {
                return fList[idx].AValue;
            }
        }

        public void SetValue(int ID, int Value)
        {
            int idx = IndexOf(ID);
            if (idx < 0) {
                Add(ID, Value);
            } else {
                fList[idx].AValue = Value;
            }
        }

        public int Add(int id, int value)
        {
            int idx = IndexOf(id);
            if (idx >= 0) {
                Attribute item = fList[idx];
                item.AValue += value;
            } else {
                Attribute item = new Attribute(id, value);
                idx = fList.Add(item);
            }
            return idx;
        }

        public void Assign(AttributeList source, byte @operator)
        {
            switch (@operator) {
                case Lao_Copy:
                    {
                        Clear();
                        int count = source.Count;
                        for (int i = 0; i < count; i++) {
                            Attribute item = source.fList[i];
                            Add(item.AID, item.AValue);
                        }
                    }
                    break;

                case Lao_And:
                    {
                        for (int i = Count - 1; i >= 0; i--) {
                            Attribute aItem = fList[i];
                            if (source.IndexOf(aItem.AID) == -1) {
                                Delete(i);
                            }
                        }
                    }
                    break;

                case Lao_Or:
                    {
                        int count = source.Count;
                        for (int i = 0; i < count; i++) {
                            Attribute aItem = source.fList[i];
                            if (IndexOf(aItem.AID) == -1) {
                                Add(aItem.AID, aItem.AValue);
                            }
                        }
                    }
                    break;
            }
        }

        public void Clear()
        {
            /*int i = this.FList.Count - 1;
             if (i >= 0)
             {
             do
             {
             (this.FList[i] as TAttribute).Dispose();
             i--;
             }
             while (i != -1);
             }*/
            fList.Clear();
        }

        public void Delete(int index)
        {
            if (index < 0 || index >= fList.Count) {
                throw new ListException("List index out of bounds (%d)", index);
            }
            //(this.FList[Index] as TAttribute).Dispose();
            fList.Delete(index);
        }

        public int IndexOf(int id)
        {
            int result = 0;
            while (result < fList.Count && fList[result].AID != id) {
                result++;
            }
            if (result == fList.Count) {
                result = -1;
            }
            return result;
        }

        public void Remove(int id)
        {
            int i = IndexOf(id);
            if (i >= 0) {
                Delete(i);
            }
        }



        public void LoadFromStream(BinaryReader stream, FileVersion version)
        {
            try {
                Clear();

                int count = StreamUtils.ReadInt(stream);
                for (int i = 0; i < count; i++) {
                    int id = StreamUtils.ReadInt(stream);
                    int val = StreamUtils.ReadInt(stream);
                    Add(id, val);
                }
            } catch (Exception ex) {
                Logger.Write("AttributeList.LoadFromStream(): " + ex.Message);
                throw ex;
            }
        }


        
        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            try {
                int count = fList.Count;

                StreamUtils.WriteInt(stream, count);

                for (int i = 0; i < count; i++) {
                    Attribute attr = fList[i];
                    StreamUtils.WriteInt(stream, attr.AID);
                    StreamUtils.WriteInt(stream, attr.AValue);
                }
            } catch (Exception ex) {
                Logger.Write("AttributeList.SaveToStream(): " + ex.Message);
                throw ex;
            }
        }
    }
}
