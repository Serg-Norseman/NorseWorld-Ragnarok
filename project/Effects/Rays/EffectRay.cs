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

using NWR.Core.Types;
using NWR.Creatures;
using NWR.Game;
using NWR.Universe;
using ZRLib.Core;

namespace NWR.Effects.Rays
{
    public abstract class EffectRay
    {
        public NWCreature Creature;
        public DamageKind DmgKind;
        public int Dir;
        public EffectID EffID;
        public NWField Field;
        public MapObject MapObject;
        public bool TargetMeeted;

        public abstract void TileProc(int aX, int aY, ref bool  refContinue);

        private void LineProc(int x, int y, ref bool  refContinue)
        {
            TileProc(x, y, ref refContinue);
        }

        public void Exec(NWCreature creature, EffectID effectID, InvokeMode invokeMode, EffectExt ext, string rayMsg)
        {
            GlobalVars.nwrWin.ShowText(creature, rayMsg);

            int i = Effect.GetMagnitude(effectID);
            EffectTarget target = EffectTarget.et_None;
            if ((ext.ReqParams.Contains(EffectParams.ep_Direction))) {
                target = EffectTarget.et_Direction;
            } else {
                if ((ext.ReqParams.Contains(EffectParams.ep_Creature))) {
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
                x = creature.PosX;
                y = creature.PosY;
                NWCreature extCr = (NWCreature)ext.GetParam(EffectParams.ep_Creature);
                x2 = extCr.PosX;
                y2 = extCr.PosY;
                int dir = Directions.GetDirByCoords(x, y, x2, y2);
                x += Directions.Data[dir].DX;
                y += Directions.Data[dir].DY;
            } else {
                int dir = (int)ext.GetParam(EffectParams.ep_Direction);
                x = creature.PosX + Directions.Data[dir].DX;
                y = creature.PosY + Directions.Data[dir].DY;
                x2 = x + Directions.Data[dir].DX * i;
                y2 = y + Directions.Data[dir].DY * i;
            }
            Creature = creature;
            Dir = Directions.GetDirByCoords(x, y, x2, y2);
            EffID = effectID;

            Field = Creature.CurrentField;

            int eid = (int)effectID;
            EffectRec effRec = EffectsData.dbEffects[eid];

            if (effRec.FrameCount > 0) {
                MapObject = new MapObject(creature.Space, Field);
                MapObject.InitByEffect(effectID);
                Field.Features.Add(MapObject);
            } else {
                MapObject = null;
            }

            TargetMeeted = AuxUtils.DoLine(x, y, x2, y2, LineProc, true);

            if (MapObject != null) {
                Field.Features.Remove(MapObject);
            }
        }

        public void Step(int aX, int aY)
        {
            if (MapObject != null) {
                if (EffectsData.dbEffects[(int)EffID].Flags.Contains(EffectFlags.ef_DirAnim)) {
                    MapObject.Dir = Dir;
                }

                MapObject.NextSprite(aX, aY);
            }
        }
    }
}
