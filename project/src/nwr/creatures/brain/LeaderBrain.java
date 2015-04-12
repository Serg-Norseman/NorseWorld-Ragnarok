/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
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
package nwr.creatures.brain;

import jzrlib.utils.AuxUtils;
import jzrlib.core.brain.BrainEntity;
import jzrlib.core.CreatureEntity;
import jzrlib.core.ExtList;
import jzrlib.core.brain.GoalEntity;
import jzrlib.utils.Logger;
import jzrlib.core.Point;
import nwr.core.RS;
import jzrlib.core.Directions;
import nwr.player.Player;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class LeaderBrain extends NWBrainEntity
{
    private static final int RK_CCW90 = 0;
    private static final int RK_CW90 = 1;
    private static final int RK_CW180 = 2;

    public static final class PMPos
    {
        public byte aX;
        public byte aY;
        public byte rX;
        public byte rY;
        
        public byte mX;
        public byte mY;
    }

    public static final int[] PartyFormationsRS;

    private static final String[] WedgeN;
    private static final String[] WedgeNE;
    private static final String[] RingN;
    private static final String[] RingNE;
    private static final String[] SquareN;
    private static final String[] SquareNE;
    private static final String[] ChainN;
    private static final String[] ChainNE;
    
    private static final int MaskSize = 11;
    public static final int PartyMax = 4;

    private int fDir;
    private ExtList<CreatureEntity> fList;
    private PMPos[] fOffsets = new PMPos[PartyMax + 1];

    public PartyFormation Formation;

    public LeaderBrain(CreatureEntity owner)
    {
        super(owner);

        this.fDir = Directions.dtNone;
        this.fList = new ExtList<>();
        this.Formation = PartyFormation.pfWedge;
        this.addMember(owner);

        this.fOffsets = new PMPos[PartyMax + 1];
        for (int i = 0; i <= PartyMax; i++) {
            this.fOffsets[i] = new PMPos();
        }
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fList.dispose();
            this.fList = null;
        }
        super.dispose(disposing);
    }

    public final CreatureEntity getMember(int index)
    {
        return (CreatureEntity) this.fList.get(index);
    }

    public final int getMembersCount()
    {
        return this.fList.getCount();
    }

    public final boolean addMember(CreatureEntity member)
    {
        boolean result = false;
        if (member != null && this.fList.getCount() < 10 && this.indexOfMember(member) < 0) {
            this.fList.add(member);
            result = true;
        }
        return result;
    }

    public final Point getMemberPosition(CreatureEntity member)
    {
        int idx = this.fList.indexOf(member);
        return new Point(
                super.fSelf.getPosX() + (int) this.fOffsets[idx].rX, 
                super.fSelf.getPosY() + (int) this.fOffsets[idx].rY);
    }

    public final int indexOfMember(CreatureEntity member)
    {
        return this.fList.indexOf(member);
    }

    public final boolean removeMember(CreatureEntity member)
    {
        boolean result = false;
        if (member != null) {
            this.fList.remove(member);
            result = true;
        }
        return result;
    }

    public final int getDir()
    {
        return this.fDir;
    }

    public final void setDir(int value)
    {
        if (this.fDir != value && value != Directions.dtNone) {
            this.fDir = value;
        }

        if (this.fDir < Directions.dtNorth || this.fDir > Directions.dtSouthEast) {
            return;
        }

        if (((Player) this.getSelf()).isSail()) {
            return;
        }

        try {
            int[][] mask = selectMask(this.Formation, this.fDir);

            Point leaderPos = this.fList.get(0).getLocation();
            
            for (int y = 0; y < MaskSize; y++) {
                for (int x = 0; x < MaskSize; x++) {
                    int idx = mask[y][x];
                    if (idx >= 0) {
                        PMPos pmPos = fOffsets[idx];

                        pmPos.aX = (byte) x;
                        pmPos.aY = (byte) y;

                        pmPos.rX = (byte) (pmPos.aX - fOffsets[0].aX);
                        pmPos.rY = (byte) (pmPos.aY - fOffsets[0].aY);

                        pmPos.mX = (byte) (leaderPos.X + pmPos.rX);
                        pmPos.mY = (byte) (leaderPos.Y + pmPos.rY);
                    }
                }
            }

            // reorder
            int mercCount = this.fList.getCount() - 1;
            for (int mi = 1; mi <= mercCount; mi++) {
                PMPos pmPos = fOffsets[mi];
                
                int nearIdx = -1;
                int nearDist = 50;
                
                for (int m = 1; m <= mercCount; m++) {
                    CreatureEntity member = this.fList.get(m);

                    int dist = AuxUtils.distance(pmPos.mX, pmPos.mY, member.getPosX(), member.getPosY());
                    if (dist < nearDist) {
                        nearDist = dist;
                        nearIdx = m;
                    }
                }
                
                if (mi != nearIdx) {
                    this.fList.exchange(mi, nearIdx);
                }
            }
        } catch (Exception ex) {
            Logger.write("LeaderBrain.setDir(): " + ex.getMessage());
        }
    }

    private static int[][] selectMask(PartyFormation formation, int dir)
    {
        String[] srcMask = new String[MaskSize];
        switch (formation) {
            case pfWedge:
                switch (dir) {
                    case Directions.dtNorth:
                    case Directions.dtEast:
                    case Directions.dtSouth:
                    case Directions.dtWest:
                        srcMask = WedgeN;
                        break;

                    case Directions.dtNorthEast:
                    case Directions.dtSouthEast:
                    case Directions.dtSouthWest:
                    case Directions.dtNorthWest:
                        srcMask = WedgeNE;
                        break;
                }
                break;

            case pfRing:
                switch (dir) {
                    case Directions.dtNorth:
                    case Directions.dtEast:
                    case Directions.dtSouth:
                    case Directions.dtWest:
                        srcMask = RingN;
                        break;

                    case Directions.dtNorthEast:
                    case Directions.dtSouthEast:
                    case Directions.dtSouthWest:
                    case Directions.dtNorthWest:
                        srcMask = RingNE;
                        break;
                }
                break;

            case pfSquare:
                switch (dir) {
                    case Directions.dtNorth:
                    case Directions.dtEast:
                    case Directions.dtSouth:
                    case Directions.dtWest:
                        srcMask = SquareN;
                        break;

                    case Directions.dtNorthEast:
                    case Directions.dtSouthEast:
                    case Directions.dtSouthWest:
                    case Directions.dtNorthWest:
                        srcMask = SquareNE;
                        break;
                }
                break;

            case pfChain:
                switch (dir) {
                    case Directions.dtNorth:
                    case Directions.dtEast:
                    case Directions.dtSouth:
                    case Directions.dtWest:
                        srcMask = ChainN;
                        break;

                    case Directions.dtNorthEast:
                    case Directions.dtSouthEast:
                    case Directions.dtSouthWest:
                    case Directions.dtNorthWest:
                        srcMask = ChainNE;
                        break;
                }
                break;
        }

        int[][] destMask = new int[MaskSize][MaskSize];
        for (int y = 0; y < MaskSize; y++) {
            String line = srcMask[y];
            for (int x = 0; x < MaskSize; x++) {
                char sym = line.charAt(x);
                int cell;
                switch (sym) {
                    case ' ':
                        cell = -1;
                        break;
                    case 'L':
                        cell = 0;
                        break;
                    default:
                        String num = String.valueOf(sym);
                        cell = Integer.valueOf(num);
                        break;

                }
                destMask[y][x] = cell;
            }
        }

        int res[][];
        switch (dir) {
            case Directions.dtNorth:
            case Directions.dtNorthEast:
                res = destMask;
                break;

            case Directions.dtEast:
            case Directions.dtSouthEast:
                res = rotate(destMask, RK_CW90);
                break;

            case Directions.dtSouth:
            case Directions.dtSouthWest:
                res = rotate(destMask, RK_CW180);
                break;

            case Directions.dtWest:
            case Directions.dtNorthWest:
                res = rotate(destMask, RK_CCW90);
                break;

            default:
                res = destMask;
                break;
        }

        return res;
    }

    private static int[][] rotate(int[][] mask, int rotateKind)
    {
        int [][] res = new int[MaskSize][MaskSize];
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
                    res[y1][x1] = mask[y][x];
                }
            }
        }

        return res;
    }

    @Override
    protected GoalEntity createGoalEx(int goalKind)
    {
        return null;
    }

    static {
        PartyFormationsRS = new int[]{RS.rs_PF_Wedge, RS.rs_PF_Ring, RS.rs_PF_Square, RS.rs_PF_Chain};

        WedgeN = new String[]{
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
            "           "};

        WedgeNE = new String[]{
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
            "           "};

        RingN = new String[]{
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
            "           "};

        RingNE = new String[]{
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
            "           "};

        SquareN = new String[]{
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
            "           "};

        SquareNE = new String[]{
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
            "           "};

        ChainN = new String[]{
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
            "           "};

        ChainNE = new String[]{
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
            "           "};
    }
}
