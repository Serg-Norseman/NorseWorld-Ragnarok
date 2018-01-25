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

using BSLib;
using ZRLib.Core;
using ZRLib.Map;

namespace NWR.Universe
{
    public class BaseRoom : AreaEntity
    {
        private readonly ExtList<Door> fDoorList;

        public BaseRoom(GameSpace space, object owner)
            : base(space, owner)
        {
            fDoorList = new ExtList<Door>(true);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fDoorList.Dispose();
            }
            base.Dispose(disposing);
        }

        public virtual AbstractMap Map
        {
            get {
                return (AbstractMap)Owner;
            }
        }

        public Door GetDoor(int index)
        {
            Door result = null;
            if (index >= 0 && index < fDoorList.Count) {
                result = fDoorList[index];
            }
            return result;
        }

        public int DoorsCount
        {
            get {
                return fDoorList.Count;
            }
        }

        /// 
        /// <param name="dx"> </param>
        /// <param name="dy"> </param>
        /// <param name="dir"> </param>
        /// <param name="state"> value of {@code DoorState}
        /// @return </param>
        public Door AddDoor(int dx, int dy, int dir, int state)
        {
            Door result = new Door();
            result.X = dx;
            result.Y = dy;
            result.Dir = dir;
            result.State = state;
            fDoorList.Add(result);
            return result;
        }

        public void ClearDoors()
        {
            fDoorList.Clear();
        }

        public void DeleteDoor(int index)
        {
            fDoorList.Delete(index);
        }
    }
}
