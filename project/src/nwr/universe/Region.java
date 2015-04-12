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
package nwr.universe;

import jzrlib.core.AreaEntity;
import jzrlib.core.GameSpace;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.database.DataEntry;
import nwr.game.NWGameSpace;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.10.0
 */
public final class Region extends AreaEntity
{
    public Region(GameSpace space, Object owner)
    {
        super(space, owner);
    }

    @Override
    public final NWGameSpace getSpace()
    {
        return (NWGameSpace) this.fSpace;
    }

    @Override
    public final String getName()
    {
        DataEntry entry = this.getSpace().getDataEntry(this.CLSID);
        return (entry == null) ? Locale.getStr(RS.rs_Unknown) : entry.getName();
    }
}
