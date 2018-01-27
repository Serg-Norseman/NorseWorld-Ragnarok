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
    public sealed class LandFlags : ZRLib.Core.FlagSet
    {
        public const int lsHasForest = 0;
        public const int lsHasMountain = 1;
        public const int lsHasCreatures = 2;
        public const int lsHasItems = 3;
        public const int lsIsCave = 4;
        public const int lsIsDungeon = 5;

        public LandFlags(params int[] args)
            : base(args)
        {
        }

        public LandFlags(string signature)
            : base(signature)
        {
        }

        private static readonly string[] fSignatures;

        static LandFlags()
        {
            fSignatures = new string[] {
                "lsHasForest",
                "lsHasMountain",
                "lsHasCreatures",
                "lsHasItems",
                "lsIsCave",
                "lsIsDungeon"
            };
        }

        protected override string[] SignaturesOrder
        {
            get { return fSignatures; }
        }
    }
}
