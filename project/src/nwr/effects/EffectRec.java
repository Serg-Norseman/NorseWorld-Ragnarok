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

import jzrlib.core.Range;
import nwr.core.types.AbilityID;
import nwr.effects.EffectFlags;
import nwr.effects.EffectParams;
import nwr.core.types.AnimationKind;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class EffectRec
{
    public int NameRS;
    public EffectParams ReqParams;
    public EffectFlags Flags;
    public byte FrameCount;
    public String GFX;
    public String SFX;
    public Range Duration;
    public Range Magnitude;
    public short MPReq;
    public short LevReq;
    public AbilityID Resistance;
    public Range Damage;
    public int ImageIndex;

    public EffectRec(int Name, EffectParams ReqParams, EffectFlags Flags, 
            int FrameCount, String GFX, String SFX, Range Duration, Range Magnitude,
            int MPReq, int LevReq, AbilityID Resistance, Range Damage)
    {
        this.NameRS = Name;
        this.ReqParams = ReqParams;
        this.Flags = Flags;
        this.FrameCount = (byte) FrameCount;
        this.GFX = GFX;
        this.SFX = SFX;
        this.Duration = Duration;
        this.Magnitude = Magnitude;
        this.MPReq = (short) MPReq;
        this.LevReq = (short) LevReq;
        this.Resistance = Resistance;
        this.Damage = Damage;
        this.ImageIndex = -1;
    }
    
    public final AnimationKind getAnimationKind()
    {
        AnimationKind result;
        
        if (this.Flags.contains(EffectFlags.ef_DirAnim)) {
            result = AnimationKind.akByDirection;
        } else if (this.Flags.contains(EffectFlags.ef_RotAnim)) {
            result = AnimationKind.akByRotation;
        } else {
            result = AnimationKind.akNone;
        }
        
        return result;
    }
}
