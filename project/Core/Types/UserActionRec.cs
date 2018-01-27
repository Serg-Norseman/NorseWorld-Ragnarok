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

using ZRLib.Engine;

namespace NWR.Core.Types
{
	public sealed class UserActionRec
	{
		public string Sign;
		public int NameRes;
		public EventID Event;
		public HotKey HotKey;
		public bool WithoutShift;

		public static UserActionRec Create(string sign, int nameRes, EventID Event, HotKey hotKey, bool withoutShift)
		{
		    UserActionRec result = new UserActionRec();
			result.Sign = sign;
			result.NameRes = nameRes;
			result.Event = Event;
			result.HotKey = hotKey;
			result.WithoutShift = withoutShift;
			return result;
		}
	}
}
