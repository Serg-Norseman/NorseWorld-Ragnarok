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
package nwr.effects.rays;

import jzrlib.utils.RefObject;
import nwr.creatures.NWCreature;
import nwr.core.types.PlaceID;
import nwr.core.types.RaceID;
import nwr.universe.NWField;
import nwr.universe.NWTile;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class AnnihilationRay extends EffectRay
{
    public AnnihilationRay()
    {
    }

    @Override
    public void TileProc(int aX, int aY, RefObject<Boolean> aContinue)
    {
        NWField fld = this.Field;
        NWTile tile = fld.getTile(aX, aY);

        this.Step(aX, aY);

        if (tile == null) {
            aContinue.argValue = false;
        } else {
            int fgp = tile.getForeBase();

            switch (fgp) {
                case PlaceID.pid_Undefined:
                    break;

                case PlaceID.pid_Mountain:
                case PlaceID.pid_Vulcan:
                    aContinue.argValue = false;
                    break;

                case PlaceID.pid_Vortex:
                case PlaceID.pid_StairsDown:
                case PlaceID.pid_StairsUp:
                case PlaceID.pid_GStairsDown:
                case PlaceID.pid_GStairsUp:
                case PlaceID.pid_HoleDown:
                case PlaceID.pid_HoleUp:
                    break;

                default:
                    tile.setFore(PlaceID.pid_Rubble);
                    break;
            }

            NWCreature c = (NWCreature) fld.findCreature(aX, aY);
            if (c != null) {
                RaceID race = c.getEntry().Race;
                if (race == RaceID.crDefault || race == RaceID.crHuman) {
                    c.death("", null);
                }
            }
        }
    }
}
