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

import nwr.core.types.SymbolID;
import nwr.game.NWGameSpace;
import jzrlib.core.Directions;
import jzrlib.utils.AuxUtils;
import jzrlib.map.IMap;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Snake extends ArticulateCreature
{
    private final static class TSnakeSegmentRec
    {
        public Snake.TSegKind Kind = Snake.TSegKind.values()[0];
        public Directions Dir;

        public TSnakeSegmentRec(Snake.TSegKind kind, Directions dir)
        {
            this.Kind = kind;
            this.Dir = dir;
        }
    }

    private enum TSegKind
    {
        skHead,
        skBody,
        skTail;

        public int getValue()
        {
            return this.ordinal();
        }

        public static TSegKind forValue(int value)
        {
            return values()[value];
        }
    }

    private static final Snake.TSnakeSegmentRec[] SnakeSegments;
    private int fLength;

    private int getSegment(int dir, TSegKind kind)
    {
        int result = -1;

        for (int i = 1; i <= 24; i++) {
            if (Snake.SnakeSegments[i - 1].Kind == kind && (Snake.SnakeSegments[i - 1].Dir.contains(dir))) {
                return i;
            }
        }

        return result;
    }

    @Override
    public SymbolID getSymbol()
    {
        return SymbolID.sid_Snake;
    }

    @Override
    public boolean canMove(IMap map, int aX, int aY)
    {
        boolean result = super.canMove(map, aX, aY);
        int idx = super.findByPos(aX, aY);
        return result && idx < 1;
    }

    @Override
    public void moveTo(int NewX, int NewY)
    {
        int dx = NewX - super.getPosX();
        int dy = NewY - super.getPosY();

        if (dx != 0 && dy != 0) {
            int num = AuxUtils.getRandom(2);
            if (num != 0) {
                if (num == 1) {
                    super.moveTo(super.getPosX(), super.getPosY() + dy);
                    super.moveTo(super.getPosX() + dx, super.getPosY());
                }
            } else {
                super.moveTo(super.getPosX() + dx, super.getPosY());
                super.moveTo(super.getPosX(), super.getPosY() + dy);
            }
        } else {
            super.moveTo(NewX, NewY);
        }
    }

    @Override
    public void setPos(int aPosX, int aPosY)
    {
        super.setPos(aPosX, aPosY);

        for (int i = super.getSize() - 1; i >= 1; i--) {
            ArticulateSegment tek = super.getSegment(i);
            ArticulateSegment tek2 = super.getSegment(i - 1);
            tek.X = tek2.X;
            tek.Y = tek2.Y;
        }

        ArticulateSegment tek = super.getSegment(0);
        tek.X = aPosX;
        tek.Y = aPosY;
        tek.ImageIndex = this.getSegment(super.LastDir, Snake.TSegKind.skHead);
        int prev = Directions.dtNone;

        for (int i = super.getSize() - 1; i >= 1; i--) {
            tek = super.getSegment(i);
            ArticulateSegment tek2 = super.getSegment(i - 1);
            int dir = Directions.getDirByCoords(tek.X, tek.Y, tek2.X, tek2.Y);

            if (i == super.getSize() - 1) {
                tek.ImageIndex = this.getSegment(dir, Snake.TSegKind.skTail);
            } else {
                if ((prev == Directions.dtWest && dir == Directions.dtNorth) || (prev == Directions.dtSouth && dir == Directions.dtEast)) {
                    tek.ImageIndex = 13;
                } else {
                    if ((prev == Directions.dtWest && dir == Directions.dtSouth) || (prev == Directions.dtNorth && dir == Directions.dtEast)) {
                        tek.ImageIndex = 14;
                    } else {
                        if ((prev == Directions.dtEast && dir == Directions.dtSouth) || (prev == Directions.dtNorth && dir == Directions.dtWest)) {
                            tek.ImageIndex = 15;
                        } else {
                            if ((prev == Directions.dtEast && dir == Directions.dtNorth) || (prev == Directions.dtSouth && dir == Directions.dtWest)) {
                                tek.ImageIndex = 16;
                            } else {
                                tek.ImageIndex = this.getSegment(dir, Snake.TSegKind.skBody);
                            }
                        }
                    }
                }
            }
            prev = dir;
        }
    }

    static {
        SnakeSegments = new Snake.TSnakeSegmentRec[24];
        SnakeSegments[0] = new TSnakeSegmentRec(Snake.TSegKind.skHead, new Directions(Directions.dtNorth));
        SnakeSegments[1] = new TSnakeSegmentRec(Snake.TSegKind.skHead, new Directions(Directions.dtEast));
        SnakeSegments[2] = new TSnakeSegmentRec(Snake.TSegKind.skHead, new Directions(Directions.dtSouth));
        SnakeSegments[3] = new TSnakeSegmentRec(Snake.TSegKind.skHead, new Directions(Directions.dtWest));
        SnakeSegments[4] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions(Directions.dtWest, Directions.dtEast));
        SnakeSegments[5] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions(Directions.dtWest, Directions.dtEast));
        SnakeSegments[6] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions(Directions.dtNorth, Directions.dtSouth));
        SnakeSegments[7] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions(Directions.dtNorth, Directions.dtSouth));
        SnakeSegments[8] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions(Directions.dtWest, Directions.dtEast));
        SnakeSegments[9] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions(Directions.dtWest, Directions.dtEast));
        SnakeSegments[10] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions(Directions.dtNorth, Directions.dtSouth));
        SnakeSegments[11] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions(Directions.dtNorth, Directions.dtSouth));
        SnakeSegments[12] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions());
        SnakeSegments[13] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions());
        SnakeSegments[14] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions());
        SnakeSegments[15] = new TSnakeSegmentRec(Snake.TSegKind.skBody, new Directions());
        SnakeSegments[16] = new TSnakeSegmentRec(Snake.TSegKind.skTail, new Directions(Directions.dtNorth));
        SnakeSegments[17] = new TSnakeSegmentRec(Snake.TSegKind.skTail, new Directions(Directions.dtNorth));
        SnakeSegments[18] = new TSnakeSegmentRec(Snake.TSegKind.skTail, new Directions(Directions.dtEast));
        SnakeSegments[19] = new TSnakeSegmentRec(Snake.TSegKind.skTail, new Directions(Directions.dtEast));
        SnakeSegments[20] = new TSnakeSegmentRec(Snake.TSegKind.skTail, new Directions(Directions.dtSouth));
        SnakeSegments[21] = new TSnakeSegmentRec(Snake.TSegKind.skTail, new Directions(Directions.dtSouth));
        SnakeSegments[22] = new TSnakeSegmentRec(Snake.TSegKind.skTail, new Directions(Directions.dtWest));
        SnakeSegments[23] = new TSnakeSegmentRec(Snake.TSegKind.skTail, new Directions(Directions.dtWest));
    }

    public Snake(NWGameSpace space, Object owner, int creatureID, boolean total, boolean setName)
    {
        super(space, owner, creatureID, total, setName);

        this.fLength = 5;
        this.fSegments.clear();

        ArticulateSegment seg = super.add();
        seg.X = -1;
        seg.Y = -1;
        seg.ImageIndex = -1;

        if (this.fLength - 1 >= 1) {
            for (int i = 1; i < this.fLength; i++) {
                seg = super.add();
                seg.X = -1;
                seg.Y = -1;
                seg.ImageIndex = -1;
            }
        }
    }
}
