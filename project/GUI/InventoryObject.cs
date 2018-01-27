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

using BSLib;
using NWR.GUI.Controls;
using NWR.Items;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class InventoryObject : BaseDragObject
    {
        public Item InvItem;

        public override void Draw(BaseScreen screen)
        {
            if (InvItem == null) return;

            string iName = InvItem.Name;
            int c;
            if (Accepted) {
                c = Colors.Green;
            } else {
                c = Colors.Red;
            }
            int h = CtlCommon.SmFont.Height + 10;
            int w = CtlCommon.SmFont.GetTextWidth(iName) + 10;
            ExtRect r = ExtRect.Create(DragPos.X, DragPos.Y, DragPos.X + w, DragPos.Y + h);
            screen.DrawRectangle(r, c, Colors.Yellow);
            CtlCommon.SmFont.Color = BaseScreen.RGB(1, 1, 1);
            screen.DrawText(DragPos.X + 5, DragPos.Y + 5, iName, 0);
        }
    }
}
