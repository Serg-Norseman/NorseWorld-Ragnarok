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

using BSLib;

namespace NWR.Game.Types
{
    using ItemKinds = EnumSet<ItemKind>;

    public sealed class BuildingRec
    {
        public int Name;
        public SysCreature Owner;
        public ItemKinds WaresKinds;
        public byte MinCount;
        public byte MaxCount;
        public byte MaxDoors;
        public byte MinSize;
        public byte MaxSize;
        public ProbabilityTable<int> Wares;

        public BuildingRec(int name, SysCreature owner, ItemKinds wares, 
            byte minCount, byte maxCount, byte maxDoors, byte minSize, byte maxSize)
        {
            Name = name;
            Owner = owner;
            WaresKinds = wares;
            MinCount = minCount;
            MaxCount = maxCount;
            MaxDoors = maxDoors;
            MinSize = minSize;
            MaxSize = maxSize;
        }
    }
}
