/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using ZRLib.Core;
using NWR.Effects;

namespace NWR.Core.Types
{
    public sealed class PlaceRec
    {
        public int NameRS;
        public string ImageName;
        public SymbolID Symbol;
        public PlaceFlags Signs;
        public Movements Moves;
        public int SubTiles;
        public EffectID Effect;
        public int ImageIndex;
        public int SubsLoaded;

        public PlaceRec(int name, string imageName, SymbolID symbol,
            PlaceFlags signs, Movements movements, int subTiles,
            EffectID effect)
        {
            NameRS = name;
            ImageName = imageName;
            Symbol = symbol;
            Signs = signs;
            Moves = movements;
            SubTiles = subTiles;
            Effect = effect;
            ImageIndex = -1;
            SubsLoaded = 0;
        }
    }
}
