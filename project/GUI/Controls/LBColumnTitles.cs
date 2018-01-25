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

using BSLib;
using ZRLib.Core;

namespace NWR.GUI.Controls
{
    public sealed class LBColumnTitles : BaseObject
    {
        private readonly ExtList<LBColumnTitle> fList;

        public INotifyEvent OnChange;
        public INotifyEvent OnChanging;

        public int Count
        {
            get {
                return fList.Count;
            }
        }

        public LBColumnTitle GetItem(int index)
        {
            LBColumnTitle result = null;
            if (index >= 0 && index < fList.Count) {
                result = fList[index];
            }
            return result;
        }

        private void Changed()
        {
            if (OnChange != null) {
                OnChange.Invoke(this);
            }
        }

        private void Changing()
        {
            if (OnChanging != null) {
                OnChanging.Invoke(this);
            }
        }

        public LBColumnTitles()
        {
            fList = new ExtList<LBColumnTitle>();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                OnChange = null;
                OnChanging = null;
                Clear();
                fList.Dispose();
            }
            base.Dispose(disposing);
        }

        public int Add(string text, int width)
        {
            LBColumnTitle ct = new LBColumnTitle();
            ct.Text = text;
            ct.Width = width;
            return fList.Add(ct);
        }

        public void Clear()
        {
            Changing();
            fList.Clear();
            Changed();
        }

        public void Delete(int index)
        {
            LBColumnTitle ct = GetItem(index);
            if (ct != null) {
                fList.Delete(index);
            }
        }
    }
}
