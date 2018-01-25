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
using BSLib;
using NWR.Creatures.Brain.Goals;
using NWR.Game;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Core.Brain;

namespace NWR.Creatures.Brain
{
    public sealed class TraderBrain : SentientBrain
    {
        public TraderBrain(CreatureEntity owner)
            : base(owner)
        {
        }

        private void PrepareShopReturn()
        {
            try {
                NWCreature self = (NWCreature)fSelf;
                Building house = self.FindHouse();
                ExtRect houseArea = house.Area.Clone();
                if (!houseArea.Contains(self.PosX, self.PosY) && !(FindGoalByKind(GoalKind.gk_ShopReturn) is ShopReturnGoal)) {
                    houseArea.Inflate(-1, -1);
                    ShopReturnGoal srGoal = (ShopReturnGoal)CreateGoal(GoalKind.gk_ShopReturn);
                    srGoal.Position = RandomHelper.GetRandomPoint(houseArea);
                }
            } catch (Exception ex) {
                Logger.Write("TraderBrain.prepareReturn(): " + ex.Message);
            }
        }

        private void PrepareDebtTake()
        {
            try {
                NWCreature self = (NWCreature)fSelf;
                Building shop = self.FindHouse();
                NWCreature debtor = null;

                Player player = GlobalVars.nwrGame.Player;
                int debt = player.GetDebt(self.Name);
                if (debt > 0 && self.IsAvailable(player, true) && !player.InRect(shop.Area)) {
                    debtor = player;
                }

                DebtTakeGoal dtGoal = (DebtTakeGoal)FindGoalByKind(GoalKind.gk_DebtTake);
                if (debtor != null) {
                    if (dtGoal == null) {
                        dtGoal = ((DebtTakeGoal)CreateGoal(GoalKind.gk_DebtTake));
                    }
                    dtGoal.Debtor = debtor;
                } else {
                    if (dtGoal != null) {
                        ReleaseGoal(dtGoal);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("TraderBrain.prepareDebtTake(): " + ex.Message);
            }
        }

        protected override void EvaluateGoal(GoalEntity goal)
        {
            try {
                NWCreature self = (NWCreature)fSelf;
                int gk = goal.Kind;

                switch (gk) {
                    case GoalKind.gk_ShopReturn:
                        goal.Value = 0.23f;
                        break;

                    case GoalKind.gk_DebtTake:
                        {
                            DebtTakeGoal dtGoal = (DebtTakeGoal)goal;
                            if (self.IsAvailable(dtGoal.Debtor, true)) {
                                goal.Value = 1f;
                            } else {
                                goal.Value = 0.1f;
                            }
                        }
                        break;

                    case GoalKind.gk_WareReturn:
                        Building house = (Building)self.FindHouse();
                        if (house.Area.Contains(self.PosX, self.PosY)) {
                            goal.Value = 0.9f;
                        } else {
                            goal.Value = 0.22f;
                        }
                        break;

                    default:
                        base.EvaluateGoal(goal);
                        break;
                }
            } catch (Exception ex) {
                Logger.Write("TraderBrain.evaluateGoal(): " + ex.Message);
            }
        }

        protected override void PrepareGoals()
        {
            try {
                base.PrepareGoals();

                PrepareDebtTake();
                PrepareShopReturn();
            } catch (Exception ex) {
                Logger.Write("TraderBrain.prepareGoals(): " + ex.Message);
            }
        }

        public Item WareReturnGoal
        {
            set {
                WareReturnGoal wrGoal = (WareReturnGoal)CreateGoal(GoalKind.gk_WareReturn);
                wrGoal.Ware = value;
            }
        }

        public override void StepTo(int aX, int aY)
        {
            try {
                Building house = (Building)((NWCreature)fSelf).FindHouse();

                if (house.Area.Contains(aX, aY)) {
                    house.SwitchDoors(Door.STATE_OPENED);
                    base.StepTo(aX, aY);
                } else {
                    base.StepTo(aX, aY);
                    house.SwitchDoors(Door.STATE_CLOSED);
                }
            } catch (Exception ex) {
                Logger.Write("TraderBrain.stepTo(): " + ex.Message);
            }
        }
    }
}
