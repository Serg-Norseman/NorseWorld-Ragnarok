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

using ZRLib.Core;

namespace NWR.Database
{
    public sealed class ItemFlags : FlagSet
    {
        public const int if_Ingredient = 0;
        public const int if_IsContainer = 1;
        public const int if_IsUnique = 2;
        public const int if_IsMeta = 3;
        public const int if_IsCountable = 4;
        public const int if_MeleeWeapon = 5;
        public const int if_ShootWeapon = 6;
        public const int if_ThrowWeapon = 7;
        public const int if_TwoHanded = 8;
        public const int if_MagicWeapon = 9;
        public const int if_ReturnWeapon = 10;
        public const int if_Projectile = 11;

        public const int if_Reserved8 = 12;
        public const int if_Reserved14 = 13;
        public const int if_Reserved15 = 14;
        public const int if_Reserved16 = 15;
        public const int if_Reserved17 = 16;
        public const int if_Reserved18 = 17;
        public const int if_Reserved19 = 18;

        public ItemFlags(params int[] args)
            : base(args)
        {
        }

        public ItemFlags(string signature)
            : base(signature)
        {
        }

        public static ItemFlags Create()
        {
            return new ItemFlags();
        }

        public static ItemFlags Create(params int[] args)
        {
            return new ItemFlags(args);
        }

        private static readonly string[] fSignatures;

        static ItemFlags()
        {
            fSignatures = new string[] {
                "if_Ingredient",
                "if_IsContainer",
                "if_IsUnique",
                "if_IsMeta",
                "if_IsCountable",
                "if_MeleeWeapon",
                "if_ShootWeapon",
                "if_ThrowWeapon",
                "if_TwoHanded",
                "if_MagicWeapon",
                "if_ReturnWeapon",
                "if_Projectile",
                "if_Reserved8",
                "if_Reserved14",
                "if_Reserved15",
                "if_Reserved16",
                "if_Reserved17",
                "if_Reserved18",
                "if_Reserved19"
            };
        }

        protected override string[] SignaturesOrder
        {
            get { return fSignatures; }
        }
    }
}
