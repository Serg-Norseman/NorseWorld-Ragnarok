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

namespace NWR.Core.Types
{
    public enum ItfElement
    {
        id_Up,
        id_Down,
        id_Left,
        id_Right,
        id_Cursor,
        id_Cursor_N,
        id_Cursor_S,
        id_Cursor_W,
        id_Cursor_E,
        id_Cursor_NW,
        id_Cursor_NE,
        id_Cursor_SW,
        id_Cursor_SE,
        id_FileDelete,
        id_FileNum,
        
        id_First = id_Up,
        id_Last = id_FileNum
    }
}
