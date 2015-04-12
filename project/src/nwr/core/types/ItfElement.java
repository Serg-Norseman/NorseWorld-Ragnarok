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

import java.util.HashMap;
import nwr.engine.BaseScreen;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum ItfElement
{
    id_Up(0, "Up.tga", BaseScreen.clWhite),
    id_Down(1, "Down.tga", BaseScreen.clWhite),
    id_Left(2, "Left.tga", BaseScreen.clWhite),
    id_Right(3, "Right.tga", BaseScreen.clWhite),
    id_Cursor(4, "Cursor.tga", BaseScreen.clWhite),
    id_Cursor_N(5, "Cursor_N.tga", BaseScreen.clWhite),
    id_Cursor_S(6, "Cursor_S.tga", BaseScreen.clWhite),
    id_Cursor_W(7, "Cursor_W.tga", BaseScreen.clWhite),
    id_Cursor_E(8, "Cursor_E.tga", BaseScreen.clWhite),
    id_Cursor_NW(9, "Cursor_NW.tga", BaseScreen.clWhite),
    id_Cursor_NE(10, "Cursor_NE.tga", BaseScreen.clWhite),
    id_Cursor_SW(11, "Cursor_SW.tga", BaseScreen.clWhite),
    id_Cursor_SE(12, "Cursor_SE.tga", BaseScreen.clWhite),
    id_FileDelete(13, "FileDelete.tga", BaseScreen.clNone),
    id_FileNum(14, "FileNum.tga", BaseScreen.clNone);


    public static final int id_First = 0;
    public static final int id_Last = 14;

    private final int intValue;
    public final String fName;
    public final int transColor;
    public int ImageIndex;

    private static HashMap<Integer, ItfElement> mappings;

    private static HashMap<Integer, ItfElement> getMappings()
    {
        synchronized (ItfElement.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private ItfElement(int value, String fName, int transColor)
    {
        this.intValue = value;
        this.fName = fName;
        this.transColor = transColor;
        this.ImageIndex = -1;

        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static ItfElement forValue(int value)
    {
        return getMappings().get(value);
    }
}
