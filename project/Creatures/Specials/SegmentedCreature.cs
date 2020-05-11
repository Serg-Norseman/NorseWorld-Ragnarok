/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014, 2020 by Serg V. Zhdanovskih.
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
    public abstract class SegmentedCreature : NWCreature
    {
        public class Segment
        {
            public int X;
            public int Y;
            public int ImageIndex;
        }


        protected ExtList<Segment> fSegments;


        public int Size
        {
            get { return fSegments.Count; }
        }

        public Segment Head
        {
            get {
                return GetSegment(0);
            }
        }

        protected SegmentedCreature(NWGameSpace space, object owner, int creatureID, bool total, bool setName)
            : base(space, owner, creatureID, total, setName)
        {
            fSegments = new ExtList<Segment>(true);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fSegments.Dispose();
            }
            base.Dispose(disposing);
        }

        public Segment GetSegment(int index)
        {
            Segment result = null;
            if (index >= 0 && index < fSegments.Count) {
                result = fSegments[index];
            }
            return result;
        }

        protected virtual Segment CreateSegment()
        {
            return new Segment();
        }

        public Segment Add()
        {
            Segment result = CreateSegment();
            fSegments.Add(result);
            return result;
        }

        public override void CheckTile(bool aHere)
        {
            NWField map = CurrentField;
            if (map == null) {
                return;
            }

            int num = fSegments.Count;
            for (int i = 0; i < num; i++) {
                Segment seg = GetSegment(i);
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
                Segment seg = GetSegment(i);
                if (seg.X == aX && seg.Y == aY) {
                    return i;
                }
            }

            return -1;
        }
    }
}
