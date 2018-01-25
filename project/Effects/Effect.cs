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

using System;
using System.IO;
using BSLib;
using ZRLib.Core;
using NWR.Core;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Items;
using NWR.Game;
using NWR.Universe;

namespace NWR.Effects
{
    public class Effect : GameEntity
    {
        public EffectAction Action;
        public int Duration;
        public int Magnitude;
        public object Source;

        public Effect(GameSpace space, object owner, EffectID eID, object source, EffectAction actionKind, int duration, int magnitude)
            : base(space, owner)
        {
            CLSID = (int)eID;
            Action = actionKind;
            Duration = duration;
            Magnitude = magnitude;
            Source = source;
        }

        public override string Desc
        {
            get {
                return "";
            }
        }

        public override string Name
        {
            get {
                int eid = CLSID_Renamed;
                return BaseLocale.GetStr(EffectsData.dbEffects[eid].NameRS);
            }
        }

        public override byte SerializeKind
        {
            get {
                byte result;
                if (Source == null) {
                    result = StaticData.SID_EFFECT;
                } else {
                    result = 0;
                }
                return result;
            }
        }

        public override bool Assign(GameEntity entity)
        {
            Effect eff = (Effect)entity;

            bool result = (EffectsData.dbEffects[eff.CLSID_Renamed].Flags.Contains(EffectFlags.ef_Cumulative)) && Source == null && eff.Source == null;

            if (result) {
                Duration += eff.Duration;
                Magnitude += eff.Magnitude;
            }

            return result;
        }

        public void Execute()
        {
            try {
                bool exec = false;
                InvokeMode mode = InvokeMode.im_ItSelf;

                switch (Action) {
                    case EffectAction.ea_Instant:
                        // dummy
                        break;

                    case EffectAction.ea_Persistent:
                        exec = (Duration == 1);
                        mode = InvokeMode.im_FinAction;
                        break;

                    case EffectAction.ea_EachTurn:
                        exec = true;
                        break;

                    case EffectAction.ea_RandomTurn:
                        exec = AuxUtils.Chance(10);
                        break;

                    case EffectAction.ea_LastTurn:
                        exec = (Duration == 1);
                        break;
                }

                if (exec) {
                    EffectID eid = (EffectID)CLSID_Renamed;

                    if (this is MapEffect) {
                        EffectExt ext = ((MapEffect)this).Ext;
                        InvokeEffect(eid, null, Source, mode, EffectAction.ea_Instant, ext);
                    } else {
                        NWCreature creature = (NWCreature)Owner;
                        int lid = ((NWField)creature.CurrentMap).LandID;
                        bool LocusValid = (eid == EffectID.eid_Relocation) && creature.IsPlayer && creature.Effects.FindEffectByID(EffectID.eid_LocusMastery) != null && lid != GlobalVars.Land_Valhalla && lid != GlobalVars.Land_Vigrid && lid != GlobalVars.Land_Bifrost && lid != GlobalVars.Land_Nidavellir && lid != GlobalVars.Land_Niflheim && lid != GlobalVars.Land_Ocean;
                        if (LocusValid) {
                            EffectExt ext = new EffectExt();
                            ext.ReqParams = new EffectParams(EffectParams.ep_Place);
                            GlobalVars.nwrWin.InitTarget(EffectID.eid_Relocation, null, mode, ext);
                        } else {
                            creature.UseEffect(eid, Source, mode, null);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Effect.execute(): " + ex.Message);
            }
        }

        public override void LoadFromStream(BinaryReader stream, FileVersion version)
        {
            base.LoadFromStream(stream, version);

            Action = (EffectAction)(StreamUtils.ReadByte(stream));
            Duration = StreamUtils.ReadInt(stream);
            Magnitude = StreamUtils.ReadInt(stream);
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            base.SaveToStream(stream, version);

            StreamUtils.WriteByte(stream, (byte)Action);
            StreamUtils.WriteInt(stream, Duration);
            StreamUtils.WriteInt(stream, Magnitude);
        }

        public static int GetDuration(EffectID effectID, ItemState state, bool Inv)
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
            MNXRange r = EffectsData.dbEffects[(int)effectID].Duration;
            float f = StaticData.dbItemStates[(int)state].Factor;
            return RandomHelper.GetBoundedRnd((int)((long)Math.Round(((double)r.Min * (double)f))), (int)((long)Math.Round(((double)r.Max * (double)f))));
        }

        public static int GetMagnitude(EffectID effectID)
        {
            MNXRange r = EffectsData.dbEffects[(int)effectID].Magnitude;
            return RandomHelper.GetBoundedRnd(r.Min, r.Max);
        }

        public static bool InitParams(EffectID effectID, LocatedEntity creature, object source, InvokeMode invokeMode, ref EffectExt  refExt)
        {
            if (refExt == null) {
                refExt = new EffectExt();
            }

            refExt.ReqParams = EffectsData.dbEffects[(int)effectID].ReqParams;
            ICheckProc proc = EffectsData.dbEffectProcs[(int)effectID].Check;
            if (proc != null) {
                proc.Invoke(effectID, (NWCreature)creature, source, invokeMode, refExt);
            }

            return refExt.Valid;
        }

        public static void InvokeEffect(EffectID effectID, LocatedEntity creature, object source, InvokeMode invokeMode, EffectAction action, EffectExt ext)
        {
            if (effectID == EffectID.eid_None) {
                return;
            }

            try {
                if (creature != null) {
                    GameEvent @event = new GameEvent(GameSpace.Instance, null);
                    @event.CLSID = (int)effectID;
                    @event.SetPos(creature.PosX, creature.PosY);
                    GlobalVars.nwrWin.DoEvent(EventID.event_EffectSound, null, null, @event);
                    @event.Dispose();
                }

                ItemState state;
                if (source != null && source is Item) {
                    state = ((Item)source).State;
                } else {
                    state = ItemState.is_Normal;
                }

                bool valid = (ext == null || ext.Valid);
                if (valid) {
                    IEffectProc proc = EffectsData.dbEffectProcs[(int)effectID].Proc;
                    if (proc != null) {
                        proc.Invoke(effectID, (NWCreature)creature, source, state, invokeMode, ext);
                    }
                }
            } catch (Exception ex) {
                //EffectsData.dbEffects[(int)effectID]
                string msg = "Effect.invokeEffect(): " + effectID.ToString();
                if (creature != null) {
                    msg = msg + "/" + ((NWCreature)creature).Entry.Sign;
                }
                if (source != null && source is Item) {
                    msg = msg + "/" + ((Item)source).Entry.Sign;
                }
                msg = msg + ", " + ex.Message;
                Logger.Write(msg);
            }
        }
    }

}