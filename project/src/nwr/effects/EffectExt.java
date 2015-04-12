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
package nwr.effects;

import jzrlib.core.BaseObject;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class EffectExt extends BaseObject
{
    private final EffectParams fHasParams;
    private final Object[] fParams = new Object[EffectParams.ep_Last + 1];

    public EffectParams ReqParams;

    public EffectExt()
    {
        this.ReqParams = new EffectParams();
        this.fHasParams = new EffectParams();
    }

    public final boolean isValid()
    {
        return this.ReqParams.isEmpty() || this.ReqParams.equals(this.fHasParams);
    }

    public final boolean isRequire(int param)
    {
        return this.ReqParams.contains(param) && !this.fHasParams.contains(param);
    }
    
    public final Object getParam(int param)
    {
        boolean result = this.fHasParams.contains(param);

        if (result) {
            return this.fParams[param];
        } else {
            return null;
        }
    }

    public final void setParam(int param, Object value)
    {
        this.fParams[param] = value;
        this.fHasParams.include(param);
    }
}
