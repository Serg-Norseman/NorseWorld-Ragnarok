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
package nwr.creatures.brain;

import jzrlib.utils.AuxUtils;
import jzrlib.core.CreatureEntity;
import jzrlib.core.brain.GoalEntity;
import jzrlib.utils.Logger;
import jzrlib.core.Rect;
import nwr.creatures.brain.goals.DebtTakeGoal;
import nwr.creatures.brain.goals.ShopReturnGoal;
import nwr.creatures.brain.goals.WareReturnGoal;
import nwr.creatures.NWCreature;
import nwr.item.Item;
import nwr.main.GlobalVars;
import nwr.universe.Door;
import nwr.player.Player;
import nwr.universe.Building;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class TraderBrain extends SentientBrain
{
    public TraderBrain(CreatureEntity owner)
    {
        super(owner);
    }

    private void prepareShopReturn()
    {
        try {
            NWCreature self = (NWCreature) super.fSelf;
            Building house = (Building) self.findHouse();
            Rect houseArea = house.getArea().clone();
            if (!houseArea.contains(self.getPosX(), self.getPosY()) && !(super.findGoalByKind(GoalKind.gk_ShopReturn) instanceof ShopReturnGoal)) {
                houseArea.inflate(-1, -1);
                ShopReturnGoal srGoal = (ShopReturnGoal) super.createGoal(GoalKind.gk_ShopReturn);
                srGoal.Position = AuxUtils.getRandomPoint(houseArea);
            }
        } catch (Exception ex) {
            Logger.write("TraderBrain.prepareReturn(): " + ex.getMessage());
        }
    }

    private void prepareDebtTake()
    {
        try {
            NWCreature self = (NWCreature) super.fSelf;
            Building shop = (Building) self.findHouse();
            NWCreature debtor = null;

            Player player = GlobalVars.nwrGame.getPlayer();
            int debt = player.getDebt(self.getName());
            if (debt > 0 && self.isAvailable(player, true) && !player.inRect(shop.getArea())) {
                debtor = player;
            }

            DebtTakeGoal dtGoal = (DebtTakeGoal) super.findGoalByKind(GoalKind.gk_DebtTake);
            if (debtor != null) {
                if (dtGoal == null) {
                    dtGoal = ((DebtTakeGoal) super.createGoal(GoalKind.gk_DebtTake));
                }
                dtGoal.Debtor = debtor;
            } else {
                if (dtGoal != null) {
                    super.releaseGoal(dtGoal);
                }
            }
        } catch (Exception ex) {
            Logger.write("TraderBrain.prepareDebtTake(): " + ex.getMessage());
        }
    }

    @Override
    protected void evaluateGoal(GoalEntity goal)
    {
        try {
            NWCreature self = (NWCreature) super.fSelf;
            int gk = goal.Kind;

            switch (gk) {
                case GoalKind.gk_ShopReturn:
                    goal.Value = 0.23f;
                    break;

                case GoalKind.gk_DebtTake: {
                    DebtTakeGoal dtGoal = (DebtTakeGoal) goal;
                    if (self.isAvailable(dtGoal.Debtor, true)) {
                        goal.Value = 1f;
                    } else {
                        goal.Value = 0.1f;
                    }
                }
                break;

                case GoalKind.gk_WareReturn:
                    Building house = (Building) self.findHouse();
                    if (house.getArea().contains(self.getPosX(), self.getPosY())) {
                        goal.Value = 0.9f;
                    } else {
                        goal.Value = 0.22f;
                    }
                    break;

                default:
                    super.evaluateGoal(goal);
                    break;
            }
        } catch (Exception ex) {
            Logger.write("TraderBrain.evaluateGoal(): " + ex.getMessage());
        }
    }

    @Override
    protected void prepareGoals()
    {
        try {
            super.prepareGoals();

            this.prepareDebtTake();
            this.prepareShopReturn();
        } catch (Exception ex) {
            Logger.write("TraderBrain.prepareGoals(): " + ex.getMessage());
        }
    }

    public final void setWareReturnGoal(Item ware)
    {
        WareReturnGoal wrGoal = (WareReturnGoal) super.createGoal(GoalKind.gk_WareReturn);
        wrGoal.Ware = ware;
    }

    @Override
    public void stepTo(int aX, int aY)
    {
        try {
            Building house = (Building) ((NWCreature) super.fSelf).findHouse();

            if (house.getArea().contains(aX, aY)) {
                house.switchDoors(Door.STATE_OPENED);
                super.stepTo(aX, aY);
            } else {
                super.stepTo(aX, aY);
                house.switchDoors(Door.STATE_CLOSED);
            }
        } catch (Exception ex) {
            Logger.write("TraderBrain.stepTo(): " + ex.getMessage());
        }
    }
}
