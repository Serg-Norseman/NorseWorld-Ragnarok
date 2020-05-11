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

using NWR.Effects;
using NWR.Game;
using NWR.Game.Types;
using ZRLib.Core;

namespace NWR.Universe
{
    /// <summary>
    /// 
    /// </summary>
    public class MapObject : GameEntity
    {
        private const int ANIM_SCALE = 5;

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

        public bool IsNeedUpdate;
        public bool IsFinished;

        public MapObject(GameSpace space, object owner)
            : base(space, owner)
        {
            Loops = 1;
            AnimKind = AnimationKind.None;
        }

        public virtual void SetPos(int ax, int ay)
        {
            PosX = ax;
            PosY = ay;
        }

        public virtual void InitByEffect(EffectID effectID)
        {
            int eid = (int)effectID;
            EffectRec effRec = EffectsData.dbEffects[eid];

            if (effRec.FrameCount > 0) {
                AnimKind = effRec.AnimationKind;
                ImagesOrigin = effRec.ImageIndex;
                FramesCount = (int)effRec.FrameCount;
            }
        }

        public void Update(long time)
        {
            long dt = time - fPrevTime;
            if (dt < 20 * ANIM_SCALE) {
                return;
            }
            fPrevTime = time;

            if (FrameIndex == FramesCount) {
                Loops--;
                if (Loops == 0) {
                    IsFinished = true;
                }

                FrameIndex = 1;
            } else {
                FrameIndex++;
            }

            ImageIndex = ImagesOrigin + (FrameIndex - 1);
        }

        public void NextSprite(int aX, int aY)
        {
            switch (AnimKind) {
                case AnimationKind.Direction:
                    ImageIndex = ImagesOrigin + (Dir - 1);
                    break;

                case AnimationKind.Rotation:
                    {
                        int fnum;
                        if (ImageIndex == 0) {
                            fnum = 1;
                        } else {
                            fnum = ImageIndex - ImagesOrigin + 1;
                        }

                        if (fnum == FramesCount) {
                            fnum = 1;
                        } else {
                            fnum++;
                        }
                        ImageIndex = ImagesOrigin + (fnum - 1);
                    }
                    break;
            }

            SetPos(aX, aY);

            NWGameSpace space = (NWGameSpace)fSpace;
            if (space.Player.IsSeen(aX, aY, false)) {
                space.RepaintView(25);
            }
        }
    }
}
