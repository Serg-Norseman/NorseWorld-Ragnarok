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

using System.Collections.Generic;
using ZRLib.Core;
using ZRLib.Map;

namespace NWR.Universe
{
    public class BaseRoom : AreaEntity
    {
        private readonly List<Door> fDoorList;

        public IList<Door> Doors
        {
            get {
                return fDoorList;
            }
        }

        public virtual AbstractMap Map
        {
            get {
                return (AbstractMap)Owner;
            }
        }


        public BaseRoom(GameSpace space, object owner) : base(space, owner)
        {
            fDoorList = new List<Door>();
        }

        public Door AddDoor(int dx, int dy, int dir, DoorState state)
        {
            Door result = new Door();
            result.X = dx;
            result.Y = dy;
            result.Dir = dir;
            result.State = state;
            fDoorList.Add(result);
            return result;
        }
    }
}
