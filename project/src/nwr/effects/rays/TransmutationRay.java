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

import jzrlib.utils.AuxUtils;
import jzrlib.utils.RefObject;
import nwr.creatures.NWCreature;
import nwr.effects.EffectsFactory;
import nwr.universe.NWField;
import nwr.universe.NWTile;
import nwr.core.types.PlaceID;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class TransmutationRay extends EffectRay
{
    private static final int[] dbTransmutatedList = new int[]{PlaceID.pid_Mud, PlaceID.pid_Rubble};

    public TransmutationRay()
    {
    }

    @Override
    public void TileProc(int aX, int aY, RefObject<Boolean> refContinue)
    {
        NWField f = this.Field;
        if (f.isBarrier(aX, aY)) {
            refContinue.argValue = false;
        } else {
            NWTile tile = f.getTile(aX, aY);
            int bg = tile.getBackBase();
            if (bg == PlaceID.pid_Mud || bg == PlaceID.pid_Rubble) {
                tile.setBack(PlaceID.pid_Ground);
            } else {
                tile.setBack(dbTransmutatedList[AuxUtils.getRandom(2)]);
            }

            NWCreature c = (NWCreature) f.findCreature(aX, aY);
            if (c != null) {
                String cSign = c.getEntry().Sign;

                if (cSign.equals("Mudman") || cSign.equals("MudFlow")) {
                    EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Mud);
                } else {
                    if (cSign.equals("LavaFlow")) {
                        EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Lava);
                    } else {
                        if (cSign.equals("Jagredin") || cSign.equals("LiveRock")) {
                            EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Rubble);
                        } else {
                            if (cSign.equals("SandForm")) {
                                EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Quicksand);
                            } else {
                                if (cSign.equals("WateryForm")) {
                                    EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Water);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
