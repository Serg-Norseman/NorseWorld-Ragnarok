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

import jzrlib.core.Rect;
import jzrlib.map.IMap;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.RefObject;
import nwr.core.types.PlaceID;
import nwr.universe.NWField;
import nwr.universe.NWTile;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class FyleischCloud
{
    private void changeFCTile(IMap map, int x, int y, Object extData, RefObject<Boolean> refContinue)
    {
        NWTile nwTile = (NWTile) map.getTile(x, y);
        if (nwTile.Foreground == PlaceID.pid_Undefined) {
            nwTile.FogID = (short) PlaceID.pid_Fog;
            nwTile.FogAge = (byte) (nwTile.FogAge + AuxUtils.getBoundedRnd(5, 17));
        }
    }

    public final void generate(NWField field, int pX, int pY)
    {
        Rect area = new Rect(pX - 7, pY - 7, pX + 7, pY + 7);
        field.gen_RarefySpace(area, this::changeFCTile, 8, 50);

        field.normalizeFog();
    }
}
