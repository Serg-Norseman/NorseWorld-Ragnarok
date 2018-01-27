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

using ZRLib.Core;
using NWR.Core;
using NWR.Core.Types;
using NWR.Universe;

namespace NWR.Items
{
    public sealed class ItemsList : LocatedEntityList
    {
        public ItemsList(object owner, bool ownsObjects)
            : base(owner, ownsObjects)
        {
        }

        public new Item GetItem(int index)
        {
            return (Item)base.GetItem(index);
        }

        public int Add(Item item, bool assign)
        {
            int result;
            if (assign) {
                result = -1;

                int num = Count;
                for (int i = 0; i < num; i++) {
                    Item dummy = GetItem(i);
                    bool res = Owner == null || !(Owner is NWField) || (dummy.PosX == item.PosX && dummy.PosY == item.PosY);
                    if (res && dummy.Assign(item)) {
                        item.Dispose();
                        return result;
                    }
                }
            }

            result = base.Add(item);

            int num2 = Count;
            for (int i = 0; i < num2; i++) {
                for (int j = i + 1; j < num2; j++) {
                    ItemKind ik = GetItem(i).Kind;
                    ItemKind ik2 = GetItem(j).Kind;
                    if (StaticData.dbItemKinds[(int)ik].Order > StaticData.dbItemKinds[(int)ik2].Order) {
                        Exchange(i, j);
                    }
                }
            }

            return result;
        }
    }
}
