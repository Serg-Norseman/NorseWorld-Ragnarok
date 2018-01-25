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

using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Game;
using ZRLib.Core;
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain.Goals
{
    public sealed class PlayerFindGoal : NWGoalEntity
    {
        public PlayerFindGoal(BrainEntity owner)
            : base(owner)
        {
        }

        public override void Execute()
        {
            NWCreature self = (NWCreature)Self;
            ExtPoint playerPt = self.Space.Player.Location;

            if (MathHelper.Distance(playerPt, self.Location) == 1) {
                int idx = RandomHelper.GetBoundedRnd(RS.rs_Diary_First, RS.rs_Diary_Last);
                string s = BaseLocale.Format(RS.rs_RavenSaid, BaseLocale.GetStr(idx));
                GlobalVars.nwrWin.ShowText(self, s, new LogFeatures(LogFeatures.lfDialog));

                self.State = CreatureState.csDead;
                IsComplete = true;
            } else {
                ExtPoint next = self.GetStep(playerPt);
                if (!next.IsEmpty) {
                    Brain.StepTo(next.X, next.Y);
                }
            }
        }
    }

}