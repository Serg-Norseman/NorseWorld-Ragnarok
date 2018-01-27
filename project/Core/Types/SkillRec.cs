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

using NWR.Effects;

namespace NWR.Core.Types
{
	public sealed class SkillRec
	{
		public int Name;
		public SkillKind Kinds;
		public string gfx;
		public EffectID Effect;
		public int ImageIndex;

		public static SkillRec Create(int name, SkillKind kinds, string gfx, EffectID effect)
		{
		    SkillRec result = new SkillRec();
			result.Name = name;
			result.Kinds = kinds;
			result.gfx = gfx;
			result.Effect = effect;
			result.ImageIndex = -1;
			return result;
		}
	}
}
