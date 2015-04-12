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

import jzrlib.core.GameEntity;
import jzrlib.core.GameSpace;
import nwr.core.types.AnimationKind;
import nwr.effects.EffectID;
import nwr.effects.EffectRec;
import nwr.effects.EffectsData;
import nwr.game.NWGameSpace;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class MapObject extends GameEntity
{
    private static final int ANIM_SCALE = 5;

    private long fPrevTime;

    public int Dir;
    public int PosX;
    public int PosY;

    public int ImageIndex;
    public AnimationKind AnimKind;
    public int FramesCount;
    public int ImagesOrigin;
    public int Loops;
    public int FrameIndex;

    public boolean IsNeedUpdate;
    public boolean IsFinished;

    public MapObject(GameSpace space, Object owner)
    {
        super(space, owner);

        this.Loops = 1;
        this.AnimKind = AnimationKind.akNone;
    }

    public void setPos(int ax, int ay)
    {
        this.PosX = ax;
        this.PosY = ay;
    }

    public void initByEffect(EffectID effectID)
    {
        int eid = effectID.getValue();
        EffectRec effRec = EffectsData.dbEffects[eid];
        
        if (effRec.FrameCount > 0) {
            this.AnimKind = effRec.getAnimationKind();
            this.ImagesOrigin = effRec.ImageIndex;
            this.FramesCount = (int) effRec.FrameCount;
        }
    }

    public final void update(long time)
    {
        long dt = time - this.fPrevTime;
        if (dt < 20 * ANIM_SCALE) {
            return;
        }
        this.fPrevTime = time;

        if (this.FrameIndex == this.FramesCount) {
            this.Loops--;
            if (this.Loops == 0) {
                this.IsFinished = true;
            }

            this.FrameIndex = 1;
        } else {
            this.FrameIndex++;
        }

        this.ImageIndex = this.ImagesOrigin + (this.FrameIndex - 1);
    }

    public final void nextSprite(int aX, int aY)
    {
        switch (this.AnimKind) {
            case akByDirection:
                this.ImageIndex = this.ImagesOrigin + (this.Dir - 1);
                break;

            case akByRotation: {
                int fnum;
                if (this.ImageIndex == 0) {
                    fnum = 1;
                } else {
                    fnum = this.ImageIndex - this.ImagesOrigin + 1;
                }

                if (fnum == this.FramesCount) {
                    fnum = 1;
                } else {
                    fnum++;
                }
                this.ImageIndex = this.ImagesOrigin + (fnum - 1);
            }
            break;
        }

        this.setPos(aX, aY);

        NWGameSpace space = (NWGameSpace) this.fSpace;
        if (space.getPlayer().isSeen(aX, aY, false)) {
            space.repaintView(25);
        }
    }
}
