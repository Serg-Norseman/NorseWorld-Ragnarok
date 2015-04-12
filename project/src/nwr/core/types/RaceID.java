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
public enum RaceID
{
    // WARNING: don't change enum's names, they used in db!
    crDefault(0, RS.rs_Reserved),
    crHuman(1, RS.rs_Race_Human),
    crAesir(2, RS.rs_Race_Aesir),
    crEvilGod(3, RS.rs_Race_EvilGod),
    crDaemon(4, RS.rs_Race_Daemon);

    public final int Value;
    public int NameRS;

    private RaceID(int value, int nameRS)
    {
        this.Value = value;
        this.NameRS = nameRS;
    }
    
    public int getValue()
    {
        return this.ordinal();
    }

    public static RaceID forValue(int value)
    {
        return values()[value];
    }
}