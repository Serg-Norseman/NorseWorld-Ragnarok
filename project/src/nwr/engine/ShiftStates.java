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
package nwr.engine;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ShiftStates extends jzrlib.core.FlagSet
{
    public static final int ssShift = 0;
    public static final int ssAlt = 1;
    public static final int ssCtrl = 2;
    public static final int ssLeft = 3;
    public static final int ssRight = 4;
    public static final int ssMiddle = 5;
    public static final int ssDouble = 6;

    public ShiftStates(int... args)
    {
        super(args);
    }
}
