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
package nwr.universe;

import jzrlib.core.AreaEntity;
import jzrlib.core.ExtList;
import jzrlib.core.GameSpace;
import jzrlib.map.AbstractMap;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class BaseRoom extends AreaEntity
{
    private final ExtList<Door> fDoorList;

    public BaseRoom(GameSpace space, Object owner)
    {
        super(space, owner);
        this.fDoorList = new ExtList<>(true);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fDoorList.dispose();
        }
        super.dispose(disposing);
    }

    public AbstractMap getMap()
    {
        return (AbstractMap) this.Owner;
    }

    public final Door getDoor(int index)
    {
        Door result = null;
        if (index >= 0 && index < this.fDoorList.getCount()) {
            result = this.fDoorList.get(index);
        }
        return result;
    }

    public final int getDoorsCount()
    {
        return this.fDoorList.getCount();
    }

    /**
     *
     * @param dx
     * @param dy
     * @param dir
     * @param state value of {@code DoorState}
     * @return
     */
    public final Door addDoor(int dx, int dy, int dir, int state)
    {
        Door result = new Door();
        result.X = dx;
        result.Y = dy;
        result.Dir = dir;
        result.State = state;
        this.fDoorList.add(result);
        return result;
    }

    public final void clearDoors()
    {
        this.fDoorList.clear();
    }

    public final void deleteDoor(int index)
    {
        this.fDoorList.delete(index);
    }
}
