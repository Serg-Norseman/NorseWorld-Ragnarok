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

using System;
using System.Collections.Generic;
using BSLib;
using NWR.Game;
using NWR.Game.Types;
using ZRLib.Core;

namespace NWR.Creatures.Specials
{
    public sealed class IvyCreeper : SegmentedCreature
    {
        public class IvyBranches : FlagSet
        {
            public const int ibkRoot = 0;
            public const int ibkBranch = 1;
            public const int ibkLeaf = 2;

            public IvyBranches(params int[] args)
                : base(args)
            {
            }
        }

        public class IvySegment : Segment
        {
            public int Id;
            public int EntryDir;
            public Directions AvailableEntries;
        }

        private sealed class IvyBranchRec
        {
            public int Kind;
            public Directions Entries;

            public IvyBranchRec(int kind, Directions entries)
            {
                Kind = kind;
                Entries = entries;
            }
        }

        private static readonly IvyBranchRec[] dbIvyBranches;

        public override SymbolID Symbol
        {
            get { return SymbolID.sid_Ivy; }
        }

        static IvyCreeper()
        {
            dbIvyBranches = new IvyBranchRec[15];
            dbIvyBranches[0] = new IvyBranchRec(IvyBranches.ibkRoot, new Directions(Directions.DtNorth, Directions.DtEast, Directions.DtSouth, Directions.DtWest));
            dbIvyBranches[1] = new IvyBranchRec(IvyBranches.ibkLeaf, new Directions(Directions.DtSouth));
            dbIvyBranches[2] = new IvyBranchRec(IvyBranches.ibkLeaf, new Directions(Directions.DtWest));
            dbIvyBranches[3] = new IvyBranchRec(IvyBranches.ibkLeaf, new Directions(Directions.DtNorth));
            dbIvyBranches[4] = new IvyBranchRec(IvyBranches.ibkLeaf, new Directions(Directions.DtEast));
            dbIvyBranches[5] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtNorth, Directions.DtSouth));
            dbIvyBranches[6] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtEast, Directions.DtWest));
            dbIvyBranches[7] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtSouth, Directions.DtEast, Directions.DtWest));
            dbIvyBranches[8] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtWest, Directions.DtNorth, Directions.DtSouth));
            dbIvyBranches[9] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtNorth, Directions.DtEast, Directions.DtWest));
            dbIvyBranches[10] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtEast, Directions.DtNorth, Directions.DtSouth));
            dbIvyBranches[11] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtEast, Directions.DtSouth));
            dbIvyBranches[12] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtWest, Directions.DtSouth));
            dbIvyBranches[13] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtWest, Directions.DtNorth));
            dbIvyBranches[14] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.DtEast, Directions.DtNorth));
        }

        public IvyCreeper(NWGameSpace space, object owner, int creatureID, bool total, bool setName)
            : base(space, owner, creatureID, total, setName)
        {
            IvyBranchRec rec = dbIvyBranches[0];

            IvySegment seg = (IvySegment)Add();
            seg.X = -1;
            seg.Y = -1;
            seg.Id = 0;
            seg.EntryDir = Directions.DtNone;
            seg.AvailableEntries = new Directions(rec.Entries);
            seg.ImageIndex = 0;
        }

        private static int GetBranchByDir(int dir, IvyBranches possible)
        {
            int result = -1;

            int cnt = 0;
            List<int> branches = new List<int>();

            for (int i = 1; i <= 14; i++) {
                if (dbIvyBranches[i].Entries.Contains(dir)) {
                    int kind = dbIvyBranches[i].Kind;
                    if (possible.Contains(kind)) {
                        cnt++;
                        branches.Add(i);
                    }
                }
            }

            if (cnt == 1) {
                result = branches[0];
            } else {
                if (cnt > 1) {
                    result = branches[RandomHelper.GetRandom(cnt)];
                }
            }

            return result;
        }

        protected override Segment CreateSegment()
        {
            return new IvySegment();
        }

        public override void DoTurn()
        {
            try {
                base.DoTurn();

                CheckTile(false);

                if (AuxUtils.Chance(25)) {
                    int idx;
                    if (Size == 1) {
                        idx = 0;
                    } else {
                        idx = RandomHelper.GetRandom(Size);
                    }

                    IvySegment seg = (IvySegment)GetSegment(idx);

                    if (dbIvyBranches[seg.Id].Kind == IvyBranches.ibkLeaf) {
                        idx = GetBranchByDir(seg.EntryDir, new IvyBranches(IvyBranches.ibkBranch));

                        if (idx >= 0) {
                            Directions avEntries = new Directions(dbIvyBranches[idx].Entries);
                            avEntries.Exclude(seg.EntryDir);

                            seg.Id = idx;
                            seg.AvailableEntries = avEntries;
                            seg.ImageIndex = idx;
                        }
                    }

                    int cnt = 0;
                    List<int> entries = new List<int>();

                    for (int dir = Directions.DtNorth; dir <= Directions.DtEast; dir++) {
                        if (seg.AvailableEntries.Contains(dir)) {
                            cnt++;
                            entries.Add(dir);
                        }
                    }

                    if (cnt == 0) {
                        return;
                    }

                    idx = RandomHelper.GetRandom(cnt);
                    int bdir = entries[idx];
                    int nx = seg.X + Directions.Data[bdir].DX;
                    int ny = seg.Y + Directions.Data[bdir].DY;
                    seg.AvailableEntries.Exclude(bdir);

                    int i = FindByPos(nx, ny);

                    if (!CurrentField.IsBarrier(nx, ny) && i < 0) {
                        int opposite = Directions.Data[bdir].Opposite;
                        idx = GetBranchByDir(opposite, new IvyBranches(IvyBranches.ibkBranch, IvyBranches.ibkLeaf));
                        if (idx >= 0) {
                            Directions avEntries = new Directions(dbIvyBranches[idx].Entries);
                            avEntries.Exclude(opposite);

                            IvySegment newSeg = (IvySegment)Add();
                            newSeg.X = nx;
                            newSeg.Y = ny;
                            newSeg.Id = idx;
                            newSeg.EntryDir = opposite;
                            newSeg.AvailableEntries = avEntries;
                            newSeg.ImageIndex = idx;
                        }
                    }
                }

                CheckTile(true);
            } catch (Exception ex) {
                Logger.Write("IvyCreeper.doTurn(): " + ex.Message);
            }
        }

        public override void SetPos(int aPosX, int aPosY)
        {
            base.SetPos(aPosX, aPosY);
            Segment h = Head;
            h.X = aPosX;
            h.Y = aPosY;
        }
    }
}
