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
using NWR.Game;
using NWR.Universe;

namespace NWR.Creatures.Specials
{
    public abstract class ArticulateCreature : NWCreature
    {
        protected ExtList<ArticulateSegment> fSegments;

        public int Size
        {
            get { return fSegments.Count; }
        }

        public ArticulateSegment GetSegment(int index)
        {
            ArticulateSegment result = null;
            if (index >= 0 && index < fSegments.Count) {
                result = fSegments[index];
            }
            return result;
        }

        public ArticulateSegment Head
        {
            get {
                return GetSegment(0);
            }
        }

        protected virtual ArticulateSegment CreateSegment()
        {
            return new ArticulateSegment();
        }

        protected ArticulateCreature(NWGameSpace space, object owner, int creatureID, bool total, bool setName)
            : base(space, owner, creatureID, total, setName)
        {
            fSegments = new ExtList<ArticulateSegment>(true);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fSegments.Dispose();
            }
            base.Dispose(disposing);
        }

        public ArticulateSegment Add()
        {
            ArticulateSegment result = CreateSegment();
            fSegments.Add(result);
            return result;
        }

        public override void CheckTile(bool aHere)
        {
            NWField map = (NWField)CurrentMap;
            if (map == null) {
                return;
            }

            int num = fSegments.Count;
            for (int i = 0; i < num; i++) {
                ArticulateSegment seg = GetSegment(i);
                NWTile tile = (NWTile)map.GetTile(seg.X, seg.Y);

                if (tile != null) {
                    if (aHere) {
                        tile.CreaturePtr = this;
                    } else {
                        tile.CreaturePtr = null;
                    }
                }
            }
        }

        public int FindByPos(int aX, int aY)
        {
            int num = fSegments.Count;
            for (int i = 0; i < num; i++) {
                ArticulateSegment seg = GetSegment(i);
                if (seg.X == aX && seg.Y == aY) {
                    return i;
                }
            }

            return -1;
        }
    }
}
