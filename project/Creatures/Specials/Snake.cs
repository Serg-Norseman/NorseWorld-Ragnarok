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
using NWR.Game.Types;
using ZRLib.Core;
using ZRLib.Map;

namespace NWR.Creatures.Specials
{
    public sealed class Snake : SegmentedCreature
    {
        private sealed class SnakeSegmentRec
        {
            public SegKind Kind;
            public Directions Dir;

            public SnakeSegmentRec(SegKind kind, Directions dir)
            {
                Kind = kind;
                Dir = dir;
            }
        }

        private enum SegKind
        {
            skHead,
            skBody,
            skTail
        }

        private static readonly SnakeSegmentRec[] SnakeSegments;
        private int fLength;

        static Snake()
        {
            SnakeSegments = new SnakeSegmentRec[24];
            SnakeSegments[0] = new SnakeSegmentRec(SegKind.skHead, new Directions(Directions.DtNorth));
            SnakeSegments[1] = new SnakeSegmentRec(SegKind.skHead, new Directions(Directions.DtEast));
            SnakeSegments[2] = new SnakeSegmentRec(SegKind.skHead, new Directions(Directions.DtSouth));
            SnakeSegments[3] = new SnakeSegmentRec(SegKind.skHead, new Directions(Directions.DtWest));
            SnakeSegments[4] = new SnakeSegmentRec(SegKind.skBody, new Directions(Directions.DtWest, Directions.DtEast));
            SnakeSegments[5] = new SnakeSegmentRec(SegKind.skBody, new Directions(Directions.DtWest, Directions.DtEast));
            SnakeSegments[6] = new SnakeSegmentRec(SegKind.skBody, new Directions(Directions.DtNorth, Directions.DtSouth));
            SnakeSegments[7] = new SnakeSegmentRec(SegKind.skBody, new Directions(Directions.DtNorth, Directions.DtSouth));
            SnakeSegments[8] = new SnakeSegmentRec(SegKind.skBody, new Directions(Directions.DtWest, Directions.DtEast));
            SnakeSegments[9] = new SnakeSegmentRec(SegKind.skBody, new Directions(Directions.DtWest, Directions.DtEast));
            SnakeSegments[10] = new SnakeSegmentRec(SegKind.skBody, new Directions(Directions.DtNorth, Directions.DtSouth));
            SnakeSegments[11] = new SnakeSegmentRec(SegKind.skBody, new Directions(Directions.DtNorth, Directions.DtSouth));
            SnakeSegments[12] = new SnakeSegmentRec(SegKind.skBody, new Directions());
            SnakeSegments[13] = new SnakeSegmentRec(SegKind.skBody, new Directions());
            SnakeSegments[14] = new SnakeSegmentRec(SegKind.skBody, new Directions());
            SnakeSegments[15] = new SnakeSegmentRec(SegKind.skBody, new Directions());
            SnakeSegments[16] = new SnakeSegmentRec(SegKind.skTail, new Directions(Directions.DtNorth));
            SnakeSegments[17] = new SnakeSegmentRec(SegKind.skTail, new Directions(Directions.DtNorth));
            SnakeSegments[18] = new SnakeSegmentRec(SegKind.skTail, new Directions(Directions.DtEast));
            SnakeSegments[19] = new SnakeSegmentRec(SegKind.skTail, new Directions(Directions.DtEast));
            SnakeSegments[20] = new SnakeSegmentRec(SegKind.skTail, new Directions(Directions.DtSouth));
            SnakeSegments[21] = new SnakeSegmentRec(SegKind.skTail, new Directions(Directions.DtSouth));
            SnakeSegments[22] = new SnakeSegmentRec(SegKind.skTail, new Directions(Directions.DtWest));
            SnakeSegments[23] = new SnakeSegmentRec(SegKind.skTail, new Directions(Directions.DtWest));
        }

        public Snake(NWGameSpace space, object owner, int creatureID, bool total, bool setName)
            : base(space, owner, creatureID, total, setName)
        {
            fLength = 5;
            fSegments.Clear();

            Segment seg = Add();
            seg.X = -1;
            seg.Y = -1;
            seg.ImageIndex = -1;

            if (fLength - 1 >= 1) {
                for (int i = 1; i < fLength; i++) {
                    seg = Add();
                    seg.X = -1;
                    seg.Y = -1;
                    seg.ImageIndex = -1;
                }
            }
        }

        public override SymbolID Symbol
        {
            get {
                return SymbolID.sid_Snake;
            }
        }

        private int GetSegment(int dir, SegKind kind)
        {
            int result = -1;

            for (int i = 1; i <= 24; i++) {
                if (SnakeSegments[i - 1].Kind == kind && (SnakeSegments[i - 1].Dir.Contains(dir))) {
                    return i;
                }
            }

            return result;
        }

        public override bool CanMove(IMap map, int aX, int aY)
        {
            bool result = base.CanMove(map, aX, aY);
            int idx = FindByPos(aX, aY);
            return result && idx < 1;
        }

        public override void MoveTo(int NewX, int NewY)
        {
            int dx = NewX - PosX;
            int dy = NewY - PosY;

            if (dx != 0 && dy != 0) {
                int num = RandomHelper.GetRandom(2);
                if (num != 0) {
                    if (num == 1) {
                        base.MoveTo(PosX, PosY + dy);
                        base.MoveTo(PosX + dx, PosY);
                    }
                } else {
                    base.MoveTo(PosX + dx, PosY);
                    base.MoveTo(PosX, PosY + dy);
                }
            } else {
                base.MoveTo(NewX, NewY);
            }
        }

        public override void SetPos(int aPosX, int aPosY)
        {
            base.SetPos(aPosX, aPosY);

            for (int i = Size - 1; i >= 1; i--) {
                Segment tek1 = base.GetSegment(i);
                Segment tek2 = base.GetSegment(i - 1);
                tek1.X = tek2.X;
                tek1.Y = tek2.Y;
            }

            Segment tek = base.GetSegment(0);
            tek.X = aPosX;
            tek.Y = aPosY;
            tek.ImageIndex = GetSegment(LastDir, SegKind.skHead);
            int prev = Directions.DtNone;

            for (int i = Size - 1; i >= 1; i--) {
                tek = base.GetSegment(i);
                Segment tek2 = base.GetSegment(i - 1);
                int dir = Directions.GetDirByCoords(tek.X, tek.Y, tek2.X, tek2.Y);

                if (i == Size - 1) {
                    tek.ImageIndex = GetSegment(dir, SegKind.skTail);
                } else {
                    if ((prev == Directions.DtWest && dir == Directions.DtNorth) || (prev == Directions.DtSouth && dir == Directions.DtEast)) {
                        tek.ImageIndex = 13;
                    } else {
                        if ((prev == Directions.DtWest && dir == Directions.DtSouth) || (prev == Directions.DtNorth && dir == Directions.DtEast)) {
                            tek.ImageIndex = 14;
                        } else {
                            if ((prev == Directions.DtEast && dir == Directions.DtSouth) || (prev == Directions.DtNorth && dir == Directions.DtWest)) {
                                tek.ImageIndex = 15;
                            } else {
                                if ((prev == Directions.DtEast && dir == Directions.DtNorth) || (prev == Directions.DtSouth && dir == Directions.DtWest)) {
                                    tek.ImageIndex = 16;
                                } else {
                                    tek.ImageIndex = GetSegment(dir, SegKind.skBody);
                                }
                            }
                        }
                    }
                }
                prev = dir;
            }
        }
    }
}
