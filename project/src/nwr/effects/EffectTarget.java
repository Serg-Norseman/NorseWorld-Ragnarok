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
package nwr.effects;

import nwr.core.RS;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum EffectTarget
{
    et_None(0, RS.rs_Reserved, RS.rs_Reserved),
    et_PlaceNear(1, RS.rs_PlaceNearNeed, RS.rs_PlaceInvalid),
    et_PlaceFar(2, RS.rs_PlaceFarNeed, RS.rs_PlaceInvalid),
    et_Direction(3, RS.rs_DirectionNeed, RS.rs_DirectionInvalid),
    et_Item(4, RS.rs_ItemNeed, RS.rs_ItemInvalid),
    et_Creature(5, RS.rs_CreatureNeed, RS.rs_CreatureInvalid),
    et_Area(6, RS.rs_AreaNeed, RS.rs_Reserved),
    et_Land(7, RS.rs_TravelWhere, RS.rs_LandInvalid),
    et_DunRoom(8, RS.rs_Reserved, RS.rs_Reserved);

    private final int fValue;

    public int QuestionRS;
    public int InvalidRS;
    
    private EffectTarget(int value, int questionRS, int invalidRS)
    {
        this.fValue = value;
        this.QuestionRS = questionRS;
        this.InvalidRS = invalidRS;
    }

    public int getValue()
    {
        return this.fValue;
    }

    public static EffectTarget forValue(int value)
    {
        return values()[value];
    }
}
