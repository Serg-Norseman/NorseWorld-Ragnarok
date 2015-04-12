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

import jzrlib.core.Point;
import jzrlib.core.Rect;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class BaseDragObject
{
    public boolean Accepted;
    public boolean ActiveDrag;
    public boolean Cancelling;
    public Point DragPos;
    public Point DragStartPos;
    public BaseControl DragTarget;

    public void draw(BaseScreen screen)
    {
        int c;
        if (this.Accepted) {
            c = BaseScreen.clGreen;
        } else {
            c = BaseScreen.clRed;
        }
        screen.drawRectangle(new Rect(this.DragPos.X, this.DragPos.Y, this.DragPos.X + 10, this.DragPos.Y + 10), c, BaseScreen.clYellow);
    }
}
