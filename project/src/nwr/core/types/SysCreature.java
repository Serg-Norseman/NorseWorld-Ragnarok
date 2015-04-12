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

import jzrlib.core.Rect;
import nwr.core.RS;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum SysCreature
{
    sc_Viking(0, RS.rs_Viking, "Viking", new Rect(64, 125, 138, 209)),
    sc_Woodsman(1, RS.rs_Woodsman, "Woodsman", new Rect(257, 125, 331, 209)),
    sc_Sage(2, RS.rs_Sage, "Sage", new Rect(451, 125, 525, 209)),
    sc_Alchemist(3, RS.rs_Alchemist, "Alchemist", new Rect(64, 274, 138, 358)),
    sc_Blacksmith(4, RS.rs_Blacksmith, "Blacksmith", new Rect(257, 274, 331, 358)),
    sc_Conjurer(5, RS.rs_Conjurer, "Conjurer", new Rect(451, 274, 525, 358)),
    sc_Merchant(6, RS.rs_Merchant, "Merchant", new Rect(0, 0, 0, 0));

    public static final int sc_First = 0;
    public static final int sc_Last = 5;

    public final int Value;
    public final int NameRS;
    public final String Sign;
    public final Rect scrRect;

    private SysCreature(int value, int Name, String Sign, Rect scrRect)
    {
        this.Value = value;
        this.NameRS = Name;
        this.Sign = Sign;
        this.scrRect = scrRect;
    }
    
    public static SysCreature forValue(int value)
    {
        return values()[value];
    }
}
