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
using ZRLib.Map;
using NWR.Core.Types;
using NWR.Universe;

namespace NWR.Effects
{
    public sealed class FyleischCloud
    {
        private static void ChangeFCTile(IMap map, int x, int y, object extData, ref bool  refContinue)
        {
            NWTile nwTile = (NWTile)map.GetTile(x, y);
            if (nwTile.Foreground == PlaceID.pid_Undefined) {
                nwTile.FogID = (ushort)PlaceID.pid_Fog;
                nwTile.FogAge = (sbyte)(nwTile.FogAge + RandomHelper.GetBoundedRnd(5, 17));
            }
        }

        public void Generate(NWField field, int pX, int pY)
        {
            ExtRect area = ExtRect.Create(pX - 7, pY - 7, pX + 7, pY + 7);
            field.Gen_RarefySpace(area, ChangeFCTile, 8, 50);

            field.NormalizeFog();
        }
    }
}
