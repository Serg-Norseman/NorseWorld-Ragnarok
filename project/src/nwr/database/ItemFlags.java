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
public final class ItemFlags extends jzrlib.core.FlagSet
{
    public static final int if_Ingredient = 0;
    public static final int if_IsContainer = 1;
    public static final int if_IsUnique = 2;
    public static final int if_IsMeta = 3;
    public static final int if_IsCountable = 4;
    public static final int if_MeleeWeapon = 5;
    public static final int if_ShootWeapon = 6;
    public static final int if_ThrowWeapon = 7;
    public static final int if_TwoHanded = 8;
    public static final int if_MagicWeapon = 9;
    public static final int if_ReturnWeapon = 10;
    public static final int if_Projectile = 11;

    public static final int if_Reserved8 = 12;
    public static final int if_Reserved14 = 13;
    public static final int if_Reserved15 = 14;
    public static final int if_Reserved16 = 15;
    public static final int if_Reserved17 = 16;
    public static final int if_Reserved18 = 17;
    public static final int if_Reserved19 = 18;

    public ItemFlags(int... args)
    {
        super(args);
    }

    public ItemFlags(String signature)
    {
        super(signature);
    }

    private static final String[] fSignatures;

    static {
        fSignatures = new String[]{
            "if_Ingredient", "if_IsContainer", "if_IsUnique", "if_IsMeta", "if_IsCountable",
            "if_MeleeWeapon", "if_ShootWeapon", "if_ThrowWeapon", "if_TwoHanded", 
            "if_MagicWeapon", "if_ReturnWeapon", "if_Projectile", 

            "if_Reserved8", "if_Reserved14", "if_Reserved15", "if_Reserved16",
            "if_Reserved17", "if_Reserved18", "if_Reserved19"};
    }

    @Override
    protected String[] getSignaturesOrder()
    {
        return fSignatures;
    }
}
