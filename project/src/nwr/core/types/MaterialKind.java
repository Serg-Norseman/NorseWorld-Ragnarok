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
package nwr.core.types;

import nwr.core.RS;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum MaterialKind
{
    mk_None(0, RS.rs_Unknown),
    mk_Stone(1, RS.rs_Mat_Stone),
    mk_Metal(2, RS.rs_Mat_Metal),
    mk_Wood(3, RS.rs_Mat_Wood),
    mk_Glass(4, RS.rs_Mat_Glass),
    mk_Bone(5, RS.rs_Mat_Bone),
    mk_Leather(6, RS.rs_Mat_Leather),
    mk_Flesh(7, RS.rs_Mat_Flesh),
    mk_Paper(8, RS.rs_Mat_Paper),
    mk_Cloth(9, RS.rs_Mat_Cloth),
    mk_Liquid(10, RS.rs_Mat_Liquid),
    mk_Fiber(11, RS.rs_Mat_Fiber),
    mk_Steel(12, RS.rs_Mat_Steel),
    mk_Diamond(13, RS.rs_Mat_Diamond),
    mk_Silver(14, RS.rs_Mat_Silver),
    mk_Mithril(15, RS.rs_Mat_Mithril);

    public final int Value;
    public int NameRS;

    private MaterialKind(int value, int name)
    {
        this.Value = value;
        this.NameRS = name;
    }

    public static MaterialKind forValue(int value)
    {
        return values()[value];
    }
}
