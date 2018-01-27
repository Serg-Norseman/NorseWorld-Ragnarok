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

using ZRLib.Core;
using NWR.Core.Types;

namespace NWR.Effects
{
    public sealed class EffectRec
    {
        public int NameRS;
        public EffectParams ReqParams;
        public EffectFlags Flags;
        public sbyte FrameCount;
        public string GFX;
        public string SFX;
        public MNXRange Duration;
        public MNXRange Magnitude;
        public short MPReq;
        public short LevReq;
        public AbilityID Resistance;
        public MNXRange Damage;
        public int ImageIndex;

        public EffectRec(int name, EffectParams reqParams, EffectFlags flags,
                         int frameCount, string gfx, string sfx, MNXRange duration,
                         MNXRange magnitude, int mpReq, int levReq,
                         AbilityID resistance, MNXRange damage)
        {
            NameRS = name;
            ReqParams = reqParams;
            Flags = flags;
            FrameCount = (sbyte)frameCount;
            GFX = gfx;
            SFX = sfx;
            Duration = duration;
            Magnitude = magnitude;
            MPReq = (short)mpReq;
            LevReq = (short)levReq;
            Resistance = resistance;
            Damage = damage;
            ImageIndex = -1;
        }

        public AnimationKind AnimationKind
        {
            get {
                AnimationKind result;
    
                if (Flags.Contains(EffectFlags.ef_DirAnim)) {
                    result = AnimationKind.akByDirection;
                } else if (Flags.Contains(EffectFlags.ef_RotAnim)) {
                    result = AnimationKind.akByRotation;
                } else {
                    result = AnimationKind.akNone;
                }
    
                return result;
            }
        }
    }

}