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
package nwr.core.types;

import jzrlib.map.Movements;
import nwr.effects.EffectID;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class PlaceRec
{
    public int NameRS;
    public String ImageName;
    public SymbolID Symbol;
    public PlaceFlags Signs;
    public Movements Moves;
    public int subTiles;
    public EffectID Effect;
    public int ImageIndex;
    public int SubsLoaded;

    public PlaceRec(int Name, String ImageName, SymbolID Symbol, PlaceFlags Signs, Movements movements, int subTiles, EffectID Effect)
    {
        this.NameRS = Name;
        this.ImageName = ImageName;
        this.Symbol = Symbol;
        this.Signs = Signs;
        this.Moves = movements;
        this.subTiles = subTiles;
        this.Effect = Effect;
        this.ImageIndex = -1;
        this.SubsLoaded = 0;
    }
}
