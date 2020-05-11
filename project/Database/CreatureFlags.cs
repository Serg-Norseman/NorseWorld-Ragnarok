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

namespace NWR.Database
{
    public sealed class CreatureFlags : ZRLib.Core.FlagSet
    {
        public const int esWalking = 0;
        public const int esSwimming = 1;
        public const int esFlying = 2;
        public const int esPhasing = 3;
        public const int esUndead = 4;
        public const int esPlant = 5;
        public const int esCohorts = 6;
        public const int esMind = 7;
        public const int esUseItems = 8;
        public const int esUnique = 9;
        public const int esEdible = 10;
        public const int esPlayer = 11;
        public const int esOnlyMagicWeapon = 12;
        public const int esResurrection = 13;

        public const int esRespawn = 14;
        public const int esCorpsesPersist = 15;
        public const int esWithoutCorpse = 16;
        public const int esRemains = 17;
        public const int esExtinctable = 18;

        public const int esFirst = 0;
        public const int esLast = 18;

        public CreatureFlags(params int[] args)
            : base(args)
        {
        }

        public CreatureFlags(string signature)
            : base(signature)
        {
        }

        private static readonly string[] fSignatures;

        static CreatureFlags()
        {
            fSignatures = new string[] {
                "esWalking",
                "esSwimming",
                "esFlying",
                "esPhasing",
                "esUndead",
                "esPlant",
                "esCohorts",
                "esMind",
                "esUseItems",
                "esUnique",
                "esEdible",
                "esPlayer",
                "esOnlyMagicWeapon",
                "esResurrection",
                "esRespawn",
                "esCorpsesPersist",
                "esWithoutCorpse",
                "esRemains",
                "esExtinctable"
            };
        }

        protected override string[] SignaturesOrder
        {
            get { return fSignatures; }
        }
    }
}
