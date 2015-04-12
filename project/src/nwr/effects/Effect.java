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

import jzrlib.utils.AuxUtils;
import jzrlib.core.GameEntity;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.core.FileVersion;
import jzrlib.core.GameSpace;
import jzrlib.core.LocatedEntity;
import jzrlib.utils.Logger;
import jzrlib.core.Range;
import jzrlib.utils.RefObject;
import jzrlib.utils.StreamUtils;
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.StaticData;
import nwr.core.types.EventID;
import nwr.core.types.ItemState;
import nwr.core.GameEvent;
import nwr.item.Item;
import nwr.main.GlobalVars;
import nwr.universe.MapEffect;
import nwr.universe.NWField;
import java.io.IOException;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class Effect extends GameEntity
{
    public EffectAction Action;
    public int Duration;
    public int Magnitude;
    public Object Source;

    public Effect(GameSpace space, Object owner, EffectID eID, Object source, EffectAction actionKind, int duration, int magnitude)
    {
        super(space, owner);
        super.setCLSID(eID.getValue());
        this.Action = actionKind;
        this.Duration = duration;
        this.Magnitude = magnitude;
        this.Source = source;
    }

    @Override
    public String getDesc()
    {
        return "";
    }

    @Override
    public String getName()
    {
        int eid = super.CLSID;
        return Locale.getStr(EffectsData.dbEffects[eid].NameRS);
    }

    @Override
    public byte getSerializeKind()
    {
        byte result;
        if (this.Source == null) {
            result = StaticData.SID_EFFECT;
        } else {
            result = 0;
        }
        return result;
    }

    @Override
    public boolean assign(GameEntity entity)
    {
        Effect eff = (Effect) entity;

        boolean result = (EffectsData.dbEffects[eff.CLSID].Flags.contains(EffectFlags.ef_Cumulative)) && this.Source == null && eff.Source == null;

        if (result) {
            this.Duration += eff.Duration;
            this.Magnitude += eff.Magnitude;
        }

        return result;
    }

    public final void execute()
    {
        try {
            boolean exec = false;
            InvokeMode mode = InvokeMode.im_ItSelf;

            switch (this.Action) {
                case ea_Instant:
                    // dummy
                    break;

                case ea_Persistent:
                    exec = (this.Duration == 1);
                    mode = InvokeMode.im_FinAction;
                    break;

                case ea_EachTurn:
                    exec = true;
                    break;

                case ea_RandomTurn:
                    exec = AuxUtils.chance(10);
                    break;

                case ea_LastTurn:
                    exec = (this.Duration == 1);
                    break;
            }

            if (exec) {
                EffectID eid = EffectID.forValue(super.CLSID);
                
                if (this instanceof MapEffect) {
                    EffectExt ext = ((MapEffect) this).Ext;
                    Effect.invokeEffect(eid, null, this.Source, mode, EffectAction.ea_Instant, ext);
                } else {
                    NWCreature creature = (NWCreature) super.Owner;
                    int lid = ((NWField) creature.getCurrentMap()).LandID;
                    boolean LocusValid = (eid == EffectID.eid_Relocation) && creature.isPlayer() && creature.getEffects().findEffectByID(EffectID.eid_LocusMastery) != null && lid != GlobalVars.Land_Valhalla && lid != GlobalVars.Land_Vigrid && lid != GlobalVars.Land_Bifrost && lid != GlobalVars.Land_Nidavellir && lid != GlobalVars.Land_Niflheim && lid != GlobalVars.Land_Ocean;
                    if (LocusValid) {
                        EffectExt ext = new EffectExt();
                        ext.ReqParams = new EffectParams(EffectParams.ep_Place);
                        GlobalVars.nwrWin.initTarget(EffectID.eid_Relocation, null, mode, ext);
                    } else {
                        creature.useEffect(eid, this.Source, mode, null);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("Effect.execute(): " + ex.getMessage());
        }
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        super.loadFromStream(stream, version);

        this.Action = EffectAction.forValue(StreamUtils.readByte(stream));
        this.Duration = StreamUtils.readInt(stream);
        this.Magnitude = StreamUtils.readInt(stream);
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        super.saveToStream(stream, version);

        StreamUtils.writeByte(stream, (byte) this.Action.getValue());
        StreamUtils.writeInt(stream, this.Duration);
        StreamUtils.writeInt(stream, this.Magnitude);
    }

    public static int getDuration(EffectID effectID, ItemState state, boolean Inv)
    {
        if (Inv) {
            if (state != ItemState.is_Blessed) {
                if (state == ItemState.is_Cursed) {
                    state = ItemState.is_Blessed;
                }
            } else {
                state = ItemState.is_Cursed;
            }
        }
        Range r = EffectsData.dbEffects[effectID.getValue()].Duration;
        float f = state.Factor;
        return AuxUtils.getBoundedRnd((int) ((long) Math.round(((double) r.Min * (double) f))), (int) ((long) Math.round(((double) r.Max * (double) f))));
    }

    public static int getMagnitude(EffectID effectID)
    {
        Range r = EffectsData.dbEffects[effectID.getValue()].Magnitude;
        return AuxUtils.getBoundedRnd(r.Min, r.Max);
    }

    public static boolean initParams(EffectID effectID, LocatedEntity creature, Object source, InvokeMode invokeMode, RefObject<EffectExt> refExt)
    {
        if (refExt.argValue == null) {
            refExt.argValue = new EffectExt();
        }

        refExt.argValue.ReqParams = EffectsData.dbEffects[effectID.getValue()].ReqParams;
        ICheckProc proc = EffectsData.dbEffectProcs[effectID.getValue()].Check;
        if (proc != null) {
            proc.invoke(effectID, (NWCreature) creature, source, invokeMode, refExt.argValue);
        }

        return refExt.argValue.isValid();
    }

    public static void invokeEffect(EffectID effectID, LocatedEntity creature, Object source, InvokeMode invokeMode, EffectAction action, EffectExt ext)
    {
        if (effectID == EffectID.eid_None) {
            return;
        }

        try {
            if (creature != null) {
                GameEvent event = new GameEvent(GameSpace.getInstance(), null);
                event.setCLSID(effectID.getValue());
                event.setPos(creature.getPosX(), creature.getPosY());
                GlobalVars.nwrWin.doEvent(EventID.event_EffectSound, null, null, event);
                event.dispose();
            }

            ItemState state;
            if (source != null && source instanceof Item) {
                state = ((Item) source).State;
            } else {
                state = ItemState.is_Normal;
            }

            boolean valid = (ext == null || ext.isValid());
            if (valid) {
                IEffectProc proc = EffectsData.dbEffectProcs[effectID.getValue()].Proc;
                if (proc != null) {
                    proc.invoke(effectID, (NWCreature) creature, source, state, invokeMode, ext);
                }
            }
        } catch (Exception ex) {
            String msg = "Effect.invokeEffect(): " + effectID.name();
            if (creature != null) {
                msg = msg + "/" + ((NWCreature) creature).getEntry().Sign;
            }
            if (source != null && source instanceof Item) {
                msg = msg + "/" + ((Item) source).getEntry().Sign;
            }
            msg = msg + ", " + ex.getMessage();
            Logger.write(msg);
        }
    }
}
