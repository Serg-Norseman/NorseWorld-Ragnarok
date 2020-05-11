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

using System;
using BSLib;
using ZRLib.Core;
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain.Goals
{
    public sealed class FlockGoal : NWGoalEntity
    {
        public FlockGoal(BrainEntity owner)
            : base(owner)
        {
        }

        public override void Execute()
        {
            NWCreature self = (NWCreature)Self;
            BeastBrain beastBrain = (BeastBrain)Brain;

            int min = beastBrain.fNearKinsfolkDist;
            CreatureEntity nearKinsfolk = beastBrain.fNearKinsfolk;
            int cohX = 0;
            int cohY = 0;
            int algX = 0;
            int algY = 0;

            int cnt = beastBrain.Kinsfolks.Count;
            for (int i = 0; i < cnt; i++) {
                NWCreature cr = beastBrain.Kinsfolks[i];
                int dir = cr.LastDir;
                cohX += cr.PosX;
                cohY += cr.PosY;
                algX += Directions.Data[dir].DX;
                algY += Directions.Data[dir].DY;
            }

            int dX = 0;
            int dY = 0;
            if (cnt > 0) {
                dX = (dX + (int)Math.Round(algX / (float)cnt));
                dY = (dY + (int)Math.Round(algY / (float)cnt));
            }
            if (cnt > 0) {
                cohX = (int)Math.Round(cohX / (float)cnt);
                cohY = (int)Math.Round(cohY / (float)cnt);
                int dir = Directions.GetDirByCoords(self.PosX, self.PosY, cohX, cohY);
                dX += Directions.Data[dir].DX;
                dY += Directions.Data[dir].DY;
            }
            if (min <= 3 && nearKinsfolk != null) {
                if (nearKinsfolk.PosX > self.PosX) {
                    dX--;
                }
                if (nearKinsfolk.PosX < self.PosX) {
                    dX++;
                }
                if (nearKinsfolk.PosY > self.PosY) {
                    dY--;
                }
                if (nearKinsfolk.PosY < self.PosY) {
                    dY++;
                }
            }

            ExtPoint newPos = new ExtPoint();
            newPos.X = self.PosX + dX;
            newPos.Y = self.PosY + dY;
            ExtPoint next = self.GetStep(newPos);
            if (!next.IsEmpty) {
                Brain.StepTo(next.X, next.Y);
            }
        }
    }
}
