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

using NWR.Core.Types;
using NWR.Database;

namespace NWR.Core
{
    /// <summary>
    /// Host interface for calls to the main window.
    /// Object for future development.
    /// </summary>
    public interface IHost
    {
        void DoEvent(EventID eventID, object sender, object receiver, object extData);
        DataEntry GetDataEntry(int uid);
        void RepaintView(int delayInterval);
        void ShowText(string text);
        void ShowText(object sender, string text);
        void ShowText(object sender, string text, LogFeatures features);
        void ShowTextAux(string text);
    }
}
