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
package nwr.universe;

import jzrlib.core.GameSpace;
import nwr.effects.Effect;
import nwr.effects.EffectAction;
import nwr.effects.EffectExt;
import nwr.effects.EffectID;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class MapEffect extends Effect
{
    public EffectExt Ext;

    public MapEffect(GameSpace space, Object owner, EffectID eID, Object source, EffectAction actionKind, int duration, int magnitude)
    {
        super(space, owner, eID, source, actionKind, duration, magnitude);
        this.Ext = null;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.Ext != null) {
                this.Ext.dispose();
            }
        }
        super.dispose(disposing);
    }
}
