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

using NWR.Creatures;
using NWR.Game.Types;
using NWR.Universe;

namespace NWR.Effects.Rays
{
    public class AnnihilationRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool  aContinue)
        {
            NWField fld = Field;
            NWTile tile = (NWTile)fld.GetTile(aX, aY);

            Step(aX, aY);

            if (tile == null) {
                aContinue = false;
            } else {
                int fgp = tile.ForeBase;

                switch (fgp) {
                    case PlaceID.pid_Undefined:
                        break;

                    case PlaceID.pid_Mountain:
                    case PlaceID.pid_Vulcan:
                        aContinue = false;
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
                        tile.Foreground = PlaceID.pid_Rubble;
                        break;
                }

                NWCreature c = (NWCreature)fld.FindCreature(aX, aY);
                if (c != null) {
                    RaceID race = c.Entry.Race;
                    if (race == RaceID.crDefault || race == RaceID.crHuman) {
                        c.Death("", null);
                    }
                }
            }
        }
    }
}
