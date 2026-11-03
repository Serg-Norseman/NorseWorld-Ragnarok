/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014, 2020 by Serg V. Zhdanovskih.
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

using NWR.Creatures;
using NWR.Game.Types;
using NWR.Universe;
using ZRLib.Map;

namespace NWR.Effects.Rays
{
    public sealed class DeanimationRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool refContinue)
        {
            NWField f = Field;

            Step(aX, aY);

            if (f.IsBarrier(aX, aY)) {
                refContinue = false;
            } else {
                BaseTile tile = f.GetTile(aX, aY);
                NWCreature c = (NWCreature)f.FindCreature(aX, aY);
                if (c != null) {
                    switch (c.Entry.Sign) {
                        case "Mudman":
                        case "MudFlow":
                            EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Mud);
                            break;
                        case "LavaFlow":
                            EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Lava);
                            break;
                        case "Jagredin":
                        case "LiveRock":
                            EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Rubble);
                            break;
                        case "SandForm":
                            EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Quicksand);
                            break;
                        case "WateryForm":
                            EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Water);
                            break;
                        default:
                            if (c.State == CreatureState.Undead) {
                                c.Death("", null);
                            }
                            break;
                    }
                }
            }
        }
    }
}
