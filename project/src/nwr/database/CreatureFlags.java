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
public final class CreatureFlags extends jzrlib.core.FlagSet
{
    public static final int esWalking = 0;
    public static final int esSwimming = 1;
    public static final int esFlying = 2;
    public static final int esPhasing = 3;
    public static final int esUndead = 4;
    public static final int esPlant = 5;
    public static final int esCohorts = 6;
    public static final int esMind = 7;
    public static final int esUseItems = 8;
    public static final int esUnique = 9;
    public static final int esEdible = 10;
    public static final int esPlayer = 11;
    public static final int esOnlyMagicWeapon = 12;
    public static final int esResurrection = 13;

    public static final int esRespawn = 14;
    public static final int esCorpsesPersist = 15;
    public static final int esWithoutCorpse = 16;
    public static final int esRemains = 17;
    public static final int esExtinctable = 18;

    public static final int esFirst = 0;
    public static final int esLast = 18;

    public CreatureFlags(int... args)
    {
        super(args);
    }

    public CreatureFlags(String signature)
    {
        super(signature);
    }

    private static final String[] fSignatures;

    static {
        fSignatures = new String[]{"esWalking", "esSwimming", "esFlying", 
            "esPhasing", "esUndead", "esPlant", "esCohorts", 
            "esMind", "esUseItems", "esUnique", "esEdible", 
            "esPlayer", "esOnlyMagicWeapon", "esResurrection",
            "esRespawn", "esCorpsesPersist", "esWithoutCorpse", "esRemains", 
            "esExtinctable"};
    }

    @Override
    protected String[] getSignaturesOrder()
    {
        return fSignatures;
    }
}
