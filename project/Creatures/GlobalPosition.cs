/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using NWR.Core;

namespace NWR.Creatures
{
    public sealed class GlobalPosition
    {
        public int Fx;
        public int Fy;
        public int Px;
        public int Py;
        public bool GlobalChanged;

        public GlobalPosition()
        {
        }

        public GlobalPosition(int fx, int fy, int px, int py, bool globalChanged)
        {
            Fx = fx;
            Fy = fy;
            Px = px;
            Py = py;
            GlobalChanged = globalChanged;
        }

        public void CheckPos()
        {
            if (Px < 0 || Px >= StaticData.FieldWidth || Py < 0 || Py >= StaticData.FieldHeight) {
                if (Px < 0) {
                    Fx--;
                    Px = StaticData.FieldWidth - 1;
                }
                if (Px >= StaticData.FieldWidth) {
                    Fx++;
                    Px = 0;
                }
                if (Py < 0) {
                    Fy--;
                    Py = StaticData.FieldHeight - 1;
                }
                if (Py >= StaticData.FieldHeight) {
                    Fy++;
                    Py = 0;
                }
                GlobalChanged = true;
            }
        }
    }
}
