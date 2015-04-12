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
package nwr.creatures.specials;

import java.util.ArrayList;
import jzrlib.core.Directions;
import jzrlib.core.FlagSet;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import nwr.core.types.SymbolID;
import nwr.game.NWGameSpace;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class IvyCreeper extends ArticulateCreature
{
    public static class IvyBranches extends FlagSet
    {
        public static final int ibkRoot = 0;
        public static final int ibkBranch = 1;
        public static final int ibkLeaf = 2;

        public IvyBranches(int... args)
        {
            super(args);
        }
    }

    public static class IvySegment extends ArticulateSegment
    {
        public int Id;
        public int EntryDir;
        public Directions AvailableEntries;
    }

    private final static class IvyBranchRec
    {
        public int Kind = IvyBranches.ibkRoot;
        public Directions Entries;

        public IvyBranchRec(int kind, Directions entries)
        {
            this.Kind = kind;
            this.Entries = entries;
        }
    }

    private static final IvyCreeper.IvyBranchRec[] dbIvyBranches;

    private int getBranchByDir(int dir, IvyBranches possible)
    {
        int result = -1;

        int cnt = 0;
        ArrayList<Byte> branches = new ArrayList<>();

        for (int i = 1; i <= 14; i++) {
            if (dbIvyBranches[i].Entries.contains(dir)) {
                int kind = IvyCreeper.dbIvyBranches[i].Kind;
                if (possible.contains(kind)) {
                    cnt++;
                    branches.add((byte) i);
                }
            }
        }

        if (cnt == 1) {
            result = (int) branches.get(0);
        } else {
            if (cnt > 1) {
                result = (int) branches.get(AuxUtils.getRandom(cnt));
            }
        }

        return result;
    }

    @Override
    protected ArticulateSegment createSegment()
    {
        return new IvySegment();
    }

    @Override
    public SymbolID getSymbol()
    {
        return SymbolID.sid_Ivy;
    }

    @Override
    public void doTurn()
    {
        try {
            super.doTurn();

            this.checkTile(false);

            if (AuxUtils.chance(25)) {
                int idx;
                if (super.getSize() == 1) {
                    idx = 0;
                } else {
                    idx = AuxUtils.getRandom(super.getSize());
                }

                IvySegment seg = (IvySegment) super.getSegment(idx);

                if (IvyCreeper.dbIvyBranches[seg.Id].Kind == IvyBranches.ibkLeaf) {
                    idx = this.getBranchByDir(seg.EntryDir, new IvyBranches(IvyBranches.ibkBranch));

                    if (idx >= 0) {
                        Directions avEntries = new Directions(dbIvyBranches[idx].Entries);
                        avEntries.exclude(seg.EntryDir);
                        
                        seg.Id = idx;
                        seg.AvailableEntries = avEntries;
                        seg.ImageIndex = idx;
                    }
                }

                int cnt = 0;
                ArrayList<Integer> entries = new ArrayList<>();

                for (int dir = Directions.dtNorth; dir <= Directions.dtEast; dir++) {
                    if (seg.AvailableEntries.contains(dir)) {
                        cnt++;
                        entries.add(dir);
                    }
                }

                if (cnt == 0) {
                    return;
                }

                idx = AuxUtils.getRandom(cnt);
                int bdir = entries.get(idx);
                int nx = seg.X + Directions.Data[bdir].dX;
                int ny = seg.Y + Directions.Data[bdir].dY;
                seg.AvailableEntries.exclude(bdir);

                int i = super.findByPos(nx, ny);

                if (!super.getCurrentMap().isBarrier(nx, ny) && i < 0) {
                    int opposite = Directions.Data[bdir].Opposite;
                    idx = this.getBranchByDir(opposite, new IvyBranches(IvyBranches.ibkBranch, IvyBranches.ibkLeaf));
                    if (idx >= 0) {
                        Directions avEntries = new Directions(dbIvyBranches[idx].Entries);
                        avEntries.exclude(opposite);
                        
                        IvySegment newSeg = (IvySegment) super.add();
                        newSeg.X = nx;
                        newSeg.Y = ny;
                        newSeg.Id = idx;
                        newSeg.EntryDir = opposite;
                        newSeg.AvailableEntries = avEntries;
                        newSeg.ImageIndex = idx;
                    }
                }
            }

            this.checkTile(true);
        } catch (Exception ex) {
            Logger.write("IvyCreeper.doTurn(): " + ex.getMessage());
        }
    }

    @Override
    public void setPos(int aPosX, int aPosY)
    {
        super.setPos(aPosX, aPosY);
        ArticulateSegment h = super.getHead();
        h.X = aPosX;
        h.Y = aPosY;
    }

    static {
        dbIvyBranches = new IvyCreeper.IvyBranchRec[15];
        dbIvyBranches[0] = new IvyBranchRec(IvyBranches.ibkRoot, new Directions(Directions.dtNorth, Directions.dtEast, Directions.dtSouth, Directions.dtWest));
        dbIvyBranches[1] = new IvyBranchRec(IvyBranches.ibkLeaf, new Directions(Directions.dtSouth));
        dbIvyBranches[2] = new IvyBranchRec(IvyBranches.ibkLeaf, new Directions(Directions.dtWest));
        dbIvyBranches[3] = new IvyBranchRec(IvyBranches.ibkLeaf, new Directions(Directions.dtNorth));
        dbIvyBranches[4] = new IvyBranchRec(IvyBranches.ibkLeaf, new Directions(Directions.dtEast));
        dbIvyBranches[5] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtNorth, Directions.dtSouth));
        dbIvyBranches[6] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtEast, Directions.dtWest));
        dbIvyBranches[7] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtSouth, Directions.dtEast, Directions.dtWest));
        dbIvyBranches[8] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtWest, Directions.dtNorth, Directions.dtSouth));
        dbIvyBranches[9] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtNorth, Directions.dtEast, Directions.dtWest));
        dbIvyBranches[10] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtEast, Directions.dtNorth, Directions.dtSouth));
        dbIvyBranches[11] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtEast, Directions.dtSouth));
        dbIvyBranches[12] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtWest, Directions.dtSouth));
        dbIvyBranches[13] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtWest, Directions.dtNorth));
        dbIvyBranches[14] = new IvyBranchRec(IvyBranches.ibkBranch, new Directions(Directions.dtEast, Directions.dtNorth));
    }

    public IvyCreeper(NWGameSpace space, Object owner, int creatureID, boolean total, boolean setName)
    {
        super(space, owner, creatureID, total, setName);

        IvyBranchRec rec = IvyCreeper.dbIvyBranches[0];

        IvySegment seg = (IvySegment) super.add();
        seg.X = -1;
        seg.Y = -1;
        seg.Id = 0;
        seg.EntryDir = Directions.dtNone;
        seg.AvailableEntries = new Directions(rec.Entries);
        seg.ImageIndex = 0;
    }
}
