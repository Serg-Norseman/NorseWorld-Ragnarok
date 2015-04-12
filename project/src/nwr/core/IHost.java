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
package nwr.core;

import nwr.core.types.EventID;
import nwr.core.types.LogFeatures;
import nwr.database.DataEntry;

/**
 * Host interface for calls to the main window.
 * Object for future development.
 * @author Serg V. Zhdanovskih
 */
public interface IHost
{
    void doEvent(EventID eventID, Object sender, Object receiver, Object extData);
    DataEntry getDataEntry(int uid);
    void repaintView(int delayInterval);
    void showText(String text);
    void showText(Object sender, String text);
    void showText(Object sender, String text, LogFeatures features);
    void showTextAux(String text);
}
