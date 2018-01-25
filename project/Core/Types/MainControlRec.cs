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
using NWR.GUI.Controls;

namespace NWR.Core.Types
{
    public sealed class MainControlRec
    {
        public int NameRes;
        public string ImageFile;
        public EventID Event;
        public ExtRect R;
        public CustomButton Button;

        public static MainControlRec Create(int nameRes, string imageFile, EventID _event, ExtRect rt)
        {
            MainControlRec result = new MainControlRec();
            result.NameRes = nameRes;
            result.ImageFile = imageFile;
            result.Event = _event;
            result.R = rt;
            result.Button = null;
            return result;
        }
    }
}
