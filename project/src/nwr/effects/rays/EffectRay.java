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
package nwr.effects.rays;

import nwr.creatures.NWCreature;
import nwr.core.types.DamageKind;
import nwr.effects.Effect;
import nwr.effects.EffectExt;
import nwr.effects.EffectFlags;
import nwr.effects.EffectID;
import nwr.effects.EffectParams;
import nwr.effects.EffectRec;
import nwr.effects.EffectTarget;
import nwr.effects.EffectsData;
import nwr.effects.InvokeMode;
import nwr.main.GlobalVars;
import jzrlib.core.Directions;
import nwr.universe.MapObject;
import nwr.universe.NWField;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.RefObject;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class EffectRay
{
    public NWCreature Creature;
    public DamageKind DmgKind;
    public int Dir;
    public EffectID EffID;
    public NWField Field;
    public MapObject MapObject;
    public boolean TargetMeeted;

    public abstract void TileProc(int aX, int aY, RefObject<Boolean> refContinue);

    private void LineProc(int x, int y, RefObject<Boolean> refContinue)
    {
        this.TileProc(x, y, refContinue);
    }

    public final void Exec(NWCreature creature, EffectID effectID, InvokeMode invokeMode, EffectExt ext, String rayMsg)
    {
        GlobalVars.nwrWin.showText(creature, rayMsg);

        int i = Effect.getMagnitude(effectID);
        EffectTarget target = EffectTarget.et_None;
        if ((ext.ReqParams.contains(EffectParams.ep_Direction))) {
            target = EffectTarget.et_Direction;
        } else {
            if ((ext.ReqParams.contains(EffectParams.ep_Creature))) {
                target = EffectTarget.et_Creature;
            }
        }
        int x;
        int y;
        int x2;
        int y2;
        if (target != EffectTarget.et_Direction) {
            if (target != EffectTarget.et_Creature) {
                return;
            }
            x = creature.getPosX();
            y = creature.getPosY();
            NWCreature extCr = (NWCreature) ext.getParam(EffectParams.ep_Creature);
            x2 = extCr.getPosX();
            y2 = extCr.getPosY();
            int dir = Directions.getDirByCoords(x, y, x2, y2);
            x += Directions.Data[dir].dX;
            y += Directions.Data[dir].dY;
        } else {
            int dir = (Integer) ext.getParam(EffectParams.ep_Direction);
            x = creature.getPosX() + Directions.Data[dir].dX;
            y = creature.getPosY() + Directions.Data[dir].dY;
            x2 = x + Directions.Data[dir].dX * i;
            y2 = y + Directions.Data[dir].dY * i;
        }
        this.Creature = creature;
        this.Dir = Directions.getDirByCoords(x, y, x2, y2);
        this.EffID = effectID;

        this.Field = this.Creature.getCurrentField();

        int eid = effectID.getValue();
        EffectRec effRec = EffectsData.dbEffects[eid];
        
        if (effRec.FrameCount > 0) {
            this.MapObject = new MapObject(creature.getSpace(), this.Field);
            this.MapObject.initByEffect(effectID);
            this.Field.getFeatures().add(this.MapObject);
        } else {
            this.MapObject = null;
        }

        this.TargetMeeted = AuxUtils.doLine(x, y, x2, y2, this::LineProc, true);

        if (this.MapObject != null) {
            this.Field.getFeatures().remove(this.MapObject);
        }
    }

    public final void Step(int aX, int aY)
    {
        if (this.MapObject != null) {
            if (EffectsData.dbEffects[this.EffID.getValue()].Flags.contains(EffectFlags.ef_DirAnim)) {
                this.MapObject.Dir = this.Dir;
            }

            this.MapObject.nextSprite(aX, aY);
        }
    }
}
