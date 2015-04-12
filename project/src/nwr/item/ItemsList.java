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
package nwr.item;

import jzrlib.core.LocatedEntityList;
import nwr.core.types.ItemKind;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ItemsList extends LocatedEntityList
{
    public ItemsList(Object owner, boolean ownsObjects)
    {
        super(owner, ownsObjects);
    }

    @Override
    public final Item getItem(int index)
    {
        return (Item) super.getItem(index);
    }

    public final int add(Item item, boolean assign)
    {
        int result;
        if (assign) {
            result = -1;

            int num = super.getCount();
            for (int i = 0; i < num; i++) {
                Item dummy = this.getItem(i);
                boolean res = super.Owner == null || !(super.Owner instanceof NWField) || (dummy.getPosX() == item.getPosX() && dummy.getPosY() == item.getPosY());
                if (res && dummy.assign(item)) {
                    item.dispose();
                    return result;
                }
            }
        }

        result = super.add(item);

        int num2 = super.getCount();
        for (int i = 0; i < num2; i++) {
            for (int j = i + 1; j < num2; j++) {
                ItemKind ik = this.getItem(i).getKind();
                ItemKind ik2 = this.getItem(j).getKind();
                if (ik.Order > ik2.Order) {
                    super.exchange(i, j);
                }
            }
        }

        return result;
    }
}
