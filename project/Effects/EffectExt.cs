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

namespace NWR.Effects
{
    public sealed class EffectExt : BaseObject
    {
        private readonly EffectParams fHasParams;
        private readonly object[] fParams = new object[EffectParams.ep_Last + 1];

        public EffectParams ReqParams;

        public EffectExt()
        {
            ReqParams = new EffectParams();
            fHasParams = new EffectParams();
        }

        public bool Valid
        {
            get {
                return ReqParams.Empty || ReqParams.Equals(fHasParams);
            }
        }

        public bool IsRequire(int param)
        {
            return ReqParams.Contains(param) && !fHasParams.Contains(param);
        }

        public object GetParam(int param)
        {
            bool result = fHasParams.Contains(param);

            if (result) {
                return fParams[param];
            } else {
                return null;
            }
        }

        public void SetParam(int param, object value)
        {
            fParams[param] = value;
            fHasParams.Include(param);
        }
    }
}
