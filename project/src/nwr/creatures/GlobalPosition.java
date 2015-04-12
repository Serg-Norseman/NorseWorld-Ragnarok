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
package nwr.creatures;

import nwr.core.StaticData;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
*/
public final class GlobalPosition
{
    public int fx;
    public int fy;
    public int px;
    public int py;
    public boolean globalChanged;

    public GlobalPosition()
    {
    }

    public GlobalPosition(int fx, int fy, int px, int py, boolean globalChanged)
    {
        this.fx = fx;
        this.fy = fy;
        this.px = px;
        this.py = py;
        this.globalChanged = globalChanged;
    }

    public final void checkPos()
    {
        if (this.px < 0 || this.px >= StaticData.FieldWidth || this.py < 0 || this.py >= StaticData.FieldHeight) {
            if (this.px < 0) {
                this.fx--;
                this.px = StaticData.FieldWidth - 1;
            }
            if (this.px >= StaticData.FieldWidth) {
                this.fx++;
                this.px = 0;
            }
            if (this.py < 0) {
                this.fy--;
                this.py = StaticData.FieldHeight - 1;
            }
            if (this.py >= StaticData.FieldHeight) {
                this.fy++;
                this.py = 0;
            }
            this.globalChanged = true;
        }
    }
}
