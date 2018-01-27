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
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Universe;

namespace NWR.Effects.Rays
{
    public sealed class TransmutationRay : EffectRay
    {
        private static readonly ushort[] dbTransmutatedList = new ushort[] {
            PlaceID.pid_Mud,
            PlaceID.pid_Rubble
        };

        public override void TileProc(int aX, int aY, ref bool  refContinue)
        {
            NWField f = Field;
            if (f.IsBarrier(aX, aY)) {
                refContinue = false;
            } else {
                NWTile tile = (NWTile)f.GetTile(aX, aY);
                int bg = tile.BackBase;
                if (bg == PlaceID.pid_Mud || bg == PlaceID.pid_Rubble) {
                    tile.Background = PlaceID.pid_Ground;
                } else {
                    tile.Background = dbTransmutatedList[RandomHelper.GetRandom(2)];
                }

                NWCreature c = (NWCreature)f.FindCreature(aX, aY);
                if (c != null) {
                    string cSign = c.Entry.Sign;

                    if (cSign.Equals("Mudman") || cSign.Equals("MudFlow")) {
                        EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Mud);
                    } else {
                        if (cSign.Equals("LavaFlow")) {
                            EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Lava);
                        } else {
                            if (cSign.Equals("Jagredin") || cSign.Equals("LiveRock")) {
                                EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Rubble);
                            } else {
                                if (cSign.Equals("SandForm")) {
                                    EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Quicksand);
                                } else {
                                    if (cSign.Equals("WateryForm")) {
                                        EffectsFactory.Deanimate(f, c, tile, PlaceID.pid_Water);
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
