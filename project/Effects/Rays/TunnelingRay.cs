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

using NWR.Core;
using NWR.Core.Types;
using NWR.Game;
using ZRLib.Core;
using ZRLib.Map;

namespace NWR.Effects.Rays
{
    public sealed class TunnelingRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool  aContinue)
        {
            AuxUtils.ExStub("The ray shoots into space, a futile message to other worlds.");
            AuxUtils.ExStub("Pebbles fall on your head.");
            AuxUtils.ExStub("Pelted by a pebble");
            AuxUtils.ExStub("You bring the ceiling down.");
            AuxUtils.ExStub("You dig a pit.");
            AuxUtils.ExStub("You create a hole in Bifrost! ");
            AuxUtils.ExStub("Whoosh! ");

            Step(aX, aY);

            BaseTile tile = Field.GetTile(aX, aY);
            if (tile == null) {
                aContinue = false;
                return;
            }

            int fgp = tile.ForeBase;
            switch (fgp) {
                case PlaceID.pid_Mountain:
                case PlaceID.pid_Vulcan:
                    aContinue = false;
                    break;

                case PlaceID.pid_Tree:
                    GlobalVars.nwrWin.ShowText(Creature, BaseLocale.GetStr(RS.rs_ThereAreNowHoles));
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
                    tile.Fore = PlaceID.pid_Undefined;
                    break;
            }
        }
    }
}
