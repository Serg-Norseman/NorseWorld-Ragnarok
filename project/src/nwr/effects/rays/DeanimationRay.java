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
import nwr.core.types.CreatureState;
import nwr.core.types.PlaceID;
import nwr.effects.EffectsFactory;
import jzrlib.map.BaseTile;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class DeanimationRay extends EffectRay
{
    public DeanimationRay()
    {
    }

    @Override
    public void TileProc(int aX, int aY, RefObject<Boolean> refContinue)
    {
        NWField f = this.Field;

        this.Step(aX, aY);

        if (f.isBarrier(aX, aY)) {
            refContinue.argValue = false;
        } else {
            BaseTile tile = f.getTile(aX, aY);
            NWCreature c = (NWCreature) f.findCreature(aX, aY);
            if (c != null) {
                if (c.getEntry().Sign.equals("Mudman") || c.getEntry().Sign.equals("MudFlow")) {
                    EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Mud);
                } else {
                    if (c.getEntry().Sign.equals("LavaFlow")) {
                        EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Lava);
                    } else {
                        if (c.getEntry().Sign.equals("Jagredin") || c.getEntry().Sign.equals("LiveRock")) {
                            EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Rubble);
                        } else {
                            if (c.getEntry().Sign.equals("SandForm")) {
                                EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Quicksand);
                            } else {
                                if (c.getEntry().Sign.equals("WateryForm")) {
                                    EffectsFactory.deanimate(f, c, tile, PlaceID.pid_Water);
                                } else {
                                    if (c.getState() == CreatureState.csUndead) {
                                        c.death("", null);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
