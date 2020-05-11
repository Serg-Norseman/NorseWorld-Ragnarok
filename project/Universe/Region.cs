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

using NWR.Database;
using NWR.Game;
using ZRLib.Core;

namespace NWR.Universe
{
    /// <summary>
    /// 
    /// </summary>
    public sealed class Region : AreaEntity
    {
        public Region(GameSpace space, object owner)
            : base(space, owner)
        {
        }

        public new NWGameSpace Space
        {
            get { return (NWGameSpace)fSpace; }
        }

        public override string Name
        {
            get {
                DataEntry entry = Space.GetDataEntry(CLSID);
                return (entry == null) ? BaseLocale.GetStr(RS.rs_Unknown) : entry.Name;
            }
        }
    }
}
