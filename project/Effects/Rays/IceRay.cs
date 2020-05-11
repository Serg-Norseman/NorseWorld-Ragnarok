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
using NWR.Game;
using NWR.Game.Types;
using NWR.Universe;
using ZRLib.Core;

namespace NWR.Effects.Rays
{
    public sealed class IceRay : EffectRay
    {
        public override void TileProc(int aX, int aY, ref bool  refContinue)
        {
            NWField f = Field;
            NWTile tile = (NWTile)f.GetTile(aX, aY);

            Step(aX, aY);

            if (f.IsBarrier(aX, aY)) {
                if (tile.ForeBase == PlaceID.pid_Tree) {
                    tile.Foreground = PlaceID.pid_DeadTree;
                    string tmp = BaseLocale.GetStr(StaticData.dbPlaces[5].NameRS);
                    GlobalVars.nwrWin.ShowText(Creature, BaseLocale.Format(RS.rs_TheXIsFrozen, new object[]{ tmp }));
                }
                refContinue = false;
            } else {
                NWCreature c = (NWCreature)f.FindCreature(aX, aY);
                if (c != null && c.HasAffect(EffectID.eid_Ice)) {
                    c.AddEffect(EffectID.eid_Ice, ItemState.is_Normal, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_YouAreFrozen));
                    Effect e = c.Effects.FindEffectByID(EffectID.eid_Ice);
                    if (e != null && e.Magnitude >= 30) {
                        string tmp;
                        if (c.IsPlayer) {
                            tmp = BaseLocale.GetStr(RS.rs_EncasedInIce);
                        } else {
                            tmp = BaseLocale.Format(RS.rs_TheXIsFrozen, new object[]{ c.Name });
                        }
                        c.Death(tmp, null);
                    }
                }
            }
        }
    }
}
