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

using ZRLib.Map;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Universe;

namespace NWR.Effects.Rays
{
    public sealed class DeanimationRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool  refContinue)
        {
            NWField f = Field;

            Step(aX, aY);

            if (f.IsBarrier(aX, aY)) {
                refContinue = false;
            } else {
                BaseTile tile = f.GetTile(aX, aY);
                NWCreature c = (NWCreature)f.FindCreature(aX, aY);
                if (c != null) {
                    if (c.Entry.Sign.Equals("Mudman") || c.Entry.Sign.Equals("MudFlow")) {
                        EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Mud);
                    } else {
                        if (c.Entry.Sign.Equals("LavaFlow")) {
                            EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Lava);
                        } else {
                            if (c.Entry.Sign.Equals("Jagredin") || c.Entry.Sign.Equals("LiveRock")) {
                                EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Rubble);
                            } else {
                                if (c.Entry.Sign.Equals("SandForm")) {
                                    EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Quicksand);
                                } else {
                                    if (c.Entry.Sign.Equals("WateryForm")) {
                                        EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Water);
                                    } else {
                                        if (c.State == CreatureState.csUndead) {
                                            c.Death("", null);
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
}
