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
package nwr.database;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class LandFlags extends jzrlib.core.FlagSet
{
    public static final int lsHasForest = 0;
    public static final int lsHasMountain = 1;
    public static final int lsHasCreatures = 2;
    public static final int lsHasItems = 3;
    public static final int lsIsCave = 4;
    public static final int lsIsDungeon = 5;

    public LandFlags(int... args)
    {
        super(args);
    }

    public LandFlags(String signature)
    {
        super(signature);
    }

    private static final String[] fSignatures;

    static {
        fSignatures = new String[]{
            "lsHasForest",
            "lsHasMountain",
            "lsHasCreatures",
            "lsHasItems",
            "lsIsCave",
            "lsIsDungeon"
        };
    }

    @Override
    protected String[] getSignaturesOrder()
    {
        return fSignatures;
    }
}
