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

using BSLib;
using ZRLib.Engine;

namespace NWR.GUI.Controls
{
    public sealed class LBItem : BaseObject
    {
        private readonly int fLevel;
        private readonly ListBox fOwner;
        private readonly LBItem fParent;
        private LBItemList fSubItems;

        public int AbsoluteIndex;
        public bool Checked;
        public int Color;
        public object Data;
        public int ImageIndex;
        public LBItemList List;
        public string Text;
        public int VisibleIndex;

        public int Level
        {
            get {
                return fLevel;
            }
        }

        public LBItem Parent
        {
            get {
                return fParent;
            }
        }

        public LBItemList SubItems
        {
            get {
                return fSubItems;
            }
        }

        public LBItem(ListBox owner, int level)
        {
            Color = Colors.Gold;
            Checked = false;
            Data = null;
            fLevel = (int)level;
            fOwner = owner;
            fParent = null;
            fSubItems = new LBItemList(fOwner, this);
            Text = "";
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fSubItems.Dispose();
                fSubItems = null;
            }
            base.Dispose(disposing);
        }
    }
}
