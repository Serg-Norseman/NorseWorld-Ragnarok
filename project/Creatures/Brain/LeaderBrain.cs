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
using BSLib;
using NWR.Game;
using ZRLib.Core;
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain
{
    public enum PartyFormation
    {
        pfWedge = 0,
        pfRing = 1,
        pfSquare = 2,
        pfChain = 3,

        pfFirst = pfWedge,
        pfLast = pfChain
    }

    public sealed class LeaderBrain : NWBrainEntity
    {
        private const int RK_CCW90 = 0;
        private const int RK_CW90 = 1;
        private const int RK_CW180 = 2;

        public sealed class PMPos
        {
            public sbyte AX;
            public sbyte AY;
            public sbyte RX;
            public sbyte RY;

            public sbyte MX;
            public sbyte MY;
        }

        public static readonly int[] PartyFormationsRS;

        private static readonly string[] WedgeN;
        private static readonly string[] WedgeNE;
        private static readonly string[] RingN;
        private static readonly string[] RingNE;
        private static readonly string[] SquareN;
        private static readonly string[] SquareNE;
        private static readonly string[] ChainN;
        private static readonly string[] ChainNE;

        private const int MaskSize = 11;
        public const int PartyMax = 4;

        private int fDir;
        private ExtList<NWCreature> fList;
        private PMPos[] fOffsets;

        public PartyFormation Formation;

        public ExtList<NWCreature> Members
        {
            get {
                return fList;
            }
        }

        public int Dir
        {
            get {
                return fDir;
            }
            set {
                if (fDir != value && value != Directions.DtNone) {
                    fDir = value;
                }
    
                if (fDir < Directions.DtNorth || fDir > Directions.DtSouthEast) {
                    return;
                }
    
                if (((Player)Self).Sail) {
                    return;
                }
    
                try {
                    int[, ] mask = SelectMask(Formation, fDir);
    
                    ExtPoint leaderPos = fList[0].Location;
    
                    for (int y = 0; y < MaskSize; y++) {
                        for (int x = 0; x < MaskSize; x++) {
                            int idx = mask[y, x];
                            if (idx >= 0) {
                                PMPos pmPos = fOffsets[idx];
    
                                pmPos.AX = (sbyte)x;
                                pmPos.AY = (sbyte)y;
    
                                pmPos.RX = (sbyte)(pmPos.AX - fOffsets[0].AX);
                                pmPos.RY = (sbyte)(pmPos.AY - fOffsets[0].AY);
    
                                pmPos.MX = (sbyte)(leaderPos.X + pmPos.RX);
                                pmPos.MY = (sbyte)(leaderPos.Y + pmPos.RY);
                            }
                        }
                    }
    
                    // reorder
                    int mercCount = fList.Count - 1;
                    for (int mi = 1; mi <= mercCount; mi++) {
                        PMPos pmPos = fOffsets[mi];
    
                        int nearIdx = -1;
                        int nearDist = 50;
    
                        for (int m = 1; m <= mercCount; m++) {
                            CreatureEntity member = fList[m];
    
                            int dist = MathHelper.Distance(pmPos.MX, pmPos.MY, member.PosX, member.PosY);
                            if (dist < nearDist) {
                                nearDist = dist;
                                nearIdx = m;
                            }
                        }
    
                        if (mi != nearIdx) {
                            fList.Exchange(mi, nearIdx);
                        }
                    }
                } catch (Exception ex) {
                    Logger.Write("LeaderBrain.setDir(): " + ex.Message);
                }
            }
        }


        public LeaderBrain(NWCreature owner) : base(owner)
        {
            fDir = Directions.DtNone;
            fList = new ExtList<NWCreature>();
            Formation = PartyFormation.pfWedge;
            AddMember(owner);

            fOffsets = new PMPos[PartyMax + 1];
            for (int i = 0; i <= PartyMax; i++) {
                fOffsets[i] = new PMPos();
            }
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fList.Dispose();
                fList = null;
            }
            base.Dispose(disposing);
        }

        public bool AddMember(NWCreature member)
        {
            bool result = false;
            if (member != null && fList.Count < 10 && IndexOfMember(member) < 0) {
                fList.Add(member);
                result = true;
            }
            return result;
        }

        public ExtPoint GetMemberPosition(NWCreature member)
        {
            int idx = fList.IndexOf(member);
            return new ExtPoint(fSelf.PosX + (int)fOffsets[idx].RX, fSelf.PosY + (int)fOffsets[idx].RY);
        }

        public int IndexOfMember(NWCreature member)
        {
            return fList.IndexOf(member);
        }

        public bool RemoveMember(NWCreature member)
        {
            bool result = false;
            if (member != null) {
                fList.Remove(member);
                result = true;
            }
            return result;
        }

        private static int[, ] SelectMask(PartyFormation formation, int dir)
        {
            string[] srcMask = new string[MaskSize];
            switch (formation) {
                case PartyFormation.pfWedge:
                    switch (dir) {
                        case Directions.DtNorth:
                        case Directions.DtEast:
                        case Directions.DtSouth:
                        case Directions.DtWest:
                            srcMask = WedgeN;
                            break;

                        case Directions.DtNorthEast:
                        case Directions.DtSouthEast:
                        case Directions.DtSouthWest:
                        case Directions.DtNorthWest:
                            srcMask = WedgeNE;
                            break;
                    }
                    break;

                case PartyFormation.pfRing:
                    switch (dir) {
                        case Directions.DtNorth:
                        case Directions.DtEast:
                        case Directions.DtSouth:
                        case Directions.DtWest:
                            srcMask = RingN;
                            break;

                        case Directions.DtNorthEast:
                        case Directions.DtSouthEast:
                        case Directions.DtSouthWest:
                        case Directions.DtNorthWest:
                            srcMask = RingNE;
                            break;
                    }
                    break;

                case PartyFormation.pfSquare:
                    switch (dir) {
                        case Directions.DtNorth:
                        case Directions.DtEast:
                        case Directions.DtSouth:
                        case Directions.DtWest:
                            srcMask = SquareN;
                            break;

                        case Directions.DtNorthEast:
                        case Directions.DtSouthEast:
                        case Directions.DtSouthWest:
                        case Directions.DtNorthWest:
                            srcMask = SquareNE;
                            break;
                    }
                    break;

                case PartyFormation.pfChain:
                    switch (dir) {
                        case Directions.DtNorth:
                        case Directions.DtEast:
                        case Directions.DtSouth:
                        case Directions.DtWest:
                            srcMask = ChainN;
                            break;

                        case Directions.DtNorthEast:
                        case Directions.DtSouthEast:
                        case Directions.DtSouthWest:
                        case Directions.DtNorthWest:
                            srcMask = ChainNE;
                            break;
                    }
                    break;
            }

            int[, ] destMask = new int[MaskSize, MaskSize];
            for (int y = 0; y < MaskSize; y++) {
                string line = srcMask[y];
                for (int x = 0; x < MaskSize; x++) {
                    char sym = line[x];
                    int cell;
                    switch (sym) {
                        case ' ':
                            cell = -1;
                            break;
                        case 'L':
                            cell = 0;
                            break;
                        default:
                            string num = Convert.ToString(sym);
                            cell = Convert.ToInt32(num);
                            break;

                    }
                    destMask[y, x] = cell;
                }
            }

            int[, ] res;
            switch (dir) {
                case Directions.DtNorth:
                case Directions.DtNorthEast:
                    res = destMask;
                    break;

                case Directions.DtEast:
                case Directions.DtSouthEast:
                    res = Rotate(destMask, RK_CW90);
                    break;

                case Directions.DtSouth:
                case Directions.DtSouthWest:
                    res = Rotate(destMask, RK_CW180);
                    break;

                case Directions.DtWest:
                case Directions.DtNorthWest:
                    res = Rotate(destMask, RK_CCW90);
                    break;

                default:
                    res = destMask;
                    break;
            }

            return res;
        }

        private static int[, ] Rotate(int[, ] mask, int rotateKind)
        {
            int[, ] res = new int[MaskSize, MaskSize];
            for (int y = 0; y < MaskSize; y++) {
                for (int x = 0; x < MaskSize; x++) {
                    int x1 = 0, y1 = 0;

                    switch (rotateKind) {
                        case RK_CCW90:
                            x1 = y;
                            y1 = MaskSize - 1 - x;
                            break;

                        case RK_CW90:
                            x1 = MaskSize - 1 - y;
                            y1 = x;
                            break;

                        case RK_CW180:
                            x1 = MaskSize - 1 - x;
                            y1 = MaskSize - 1 - y;
                            break;
                    }

                    if ((y1 >= 0) && (y1 < MaskSize) && (x1 >= 0) && (x1 < MaskSize)) {
                        res[y1, x1] = mask[y, x];
                    }
                }
            }

            return res;
        }

        protected override GoalEntity CreateGoalEx(int goalKind)
        {
            return null;
        }

        static LeaderBrain()
        {
            PartyFormationsRS = new int[]{ RS.rs_PF_Wedge, RS.rs_PF_Ring, RS.rs_PF_Square, RS.rs_PF_Chain };

            WedgeN = new string[] {
                "           ",
                "           ",
                "           ",
                "     L     ",
                "           ",
                "   1   2   ",
                "           ",
                " 3       4 ",
                "           ",
                "           ",
                "           "
            };

            WedgeNE = new string[] {
                "           ",
                "           ",
                "           ",
                "   3 1 L   ",
                "           ",
                "       2   ",
                "           ",
                "       4   ",
                "           ",
                "           ",
                "           "
            };

            RingN = new string[] {
                "           ",
                "           ",
                "           ",
                "     1     ",
                "           ",
                "   3 L 4   ",
                "           ",
                "     2     ",
                "           ",
                "           ",
                "           "
            };

            RingNE = new string[] {
                "           ",
                "           ",
                "           ",
                "   3   1   ",
                "           ",
                "     L     ",
                "           ",
                "   2   4   ",
                "           ",
                "           ",
                "           "
            };

            SquareN = new string[] {
                "           ",
                "           ",
                "           ",
                "   1   2   ",
                "           ",
                "     L     ",
                "           ",
                "   3   4   ",
                "           ",
                "           ",
                "           "
            };

            SquareNE = new string[] {
                "           ",
                "           ",
                "           ",
                "     1     ",
                "           ",
                "   3 L 2   ",
                "           ",
                "     4     ",
                "           ",
                "           ",
                "           "
            };

            ChainN = new string[] {
                "           ",
                "           ",
                "           ",
                "     L     ",
                "     1     ",
                "     2     ",
                "     3     ",
                "     4     ",
                "           ",
                "           ",
                "           "
            };

            ChainNE = new string[] {
                "           ",
                "           ",
                "           ",
                "       L   ",
                "      1    ",
                "     2     ",
                "    3      ",
                "   4       ",
                "           ",
                "           ",
                "           "
            };
        }
    }
}
