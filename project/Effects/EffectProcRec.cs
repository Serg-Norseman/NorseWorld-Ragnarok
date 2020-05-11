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

using NWR.Creatures;
using NWR.Game.Types;

namespace NWR.Effects
{
    public delegate void IEffectProc(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext);
    public delegate void ICheckProc(EffectID effectID, NWCreature creature, object source, InvokeMode invokeMode, EffectExt ext);

    public sealed class EffectProcRec
    {
        public ICheckProc Check;
        public IEffectProc Proc;

        public EffectProcRec(ICheckProc Check, IEffectProc Proc)
        {
            this.Check = Check;
            this.Proc = Proc;
        }
    }
}
