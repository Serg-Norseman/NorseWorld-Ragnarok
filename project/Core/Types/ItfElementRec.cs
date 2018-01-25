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

namespace NWR.Core.Types
{
    // TODO: process all structs to classes!
	public sealed class ItfElementRec
	{
		public string FileName;
		public int TransColor;
		public int ImageIndex;

		public static ItfElementRec Create(string fileName, int transColor)
		{
		    ItfElementRec result = new ItfElementRec();
			result.FileName = fileName;
			result.TransColor = transColor;
			result.ImageIndex = -1;
			return result;
		}
	}
}
