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
import jzrlib.core.brain.BrainEntity;
import jzrlib.core.CreatureEntity;
import jzrlib.core.brain.Emitter;
import jzrlib.core.brain.EmitterList;
import jzrlib.core.brain.GoalEntity;
import jzrlib.core.LocatedEntityList;
import jzrlib.utils.Logger;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.utils.RefObject;
import nwr.creatures.BodypartType;
import nwr.creatures.brain.goals.AreaGuardGoal;
import nwr.creatures.brain.goals.DebtTakeGoal;
import nwr.creatures.brain.goals.EnemyChaseGoal;
import nwr.creatures.brain.goals.EnemyEvadeGoal;
import nwr.creatures.brain.goals.EscortGoal;
import nwr.creatures.brain.goals.FlockGoal;
import nwr.creatures.brain.goals.ItemAcquireGoal;
import nwr.creatures.brain.goals.PlayerFindGoal;
import nwr.creatures.brain.goals.PointGuardGoal;
import nwr.creatures.brain.goals.ShopReturnGoal;
import nwr.creatures.brain.goals.StalkGoal;
import nwr.creatures.brain.goals.TravelGoal;
import nwr.creatures.brain.goals.WareReturnGoal;
import nwr.creatures.NWCreature;
import nwr.core.types.AttackKind;
import nwr.core.types.BestWeaponSigns;
import nwr.core.types.RaceID;
import nwr.core.types.SkillID;
import nwr.database.CreatureFlags;
import nwr.database.ItemFlags;
import nwr.effects.Effect;
import nwr.effects.EffectExt;
import nwr.effects.EffectID;
import nwr.effects.EffectParams;
import nwr.item.Item;
import nwr.main.GlobalVars;
import jzrlib.map.AbstractMap;
import jzrlib.core.Directions;
import nwr.universe.NWField;
import nwr.universe.NWTile;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class BeastBrain extends NWBrainEntity
{
    public LocatedEntityList fKinsfolks;
    public CreatureEntity fNearKinsfolk;
    public int fNearKinsfolkDist;

    public boolean Flock;
    public boolean IsLeader;
    public boolean IsShipSail;

    public BeastBrain(CreatureEntity owner)
    {
        super(owner);
        this.fKinsfolks = new LocatedEntityList(null, false);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fKinsfolks.dispose();
            this.fKinsfolks = null;
        }
        super.dispose(disposing);
    }

    private void prepareFlock()
    {
        try {
            this.IsLeader = true;
            this.fKinsfolks.clear();

            NWCreature self = (NWCreature) super.fSelf;
            NWField fld = (NWField) self.getCurrentMap();

            this.fNearKinsfolk = null;
            this.fNearKinsfolkDist = AuxUtils.MaxInt;

            int num = fld.getCreatures().getCount();
            for (int i = 0; i < num; i++) {
                NWCreature cr = fld.getCreatures().getItem(i);
                int dist = AuxUtils.distance(cr.getLocation(), self.getLocation());
                if (!cr.equals(self) && dist <= (int) self.getSurvey() && cr.CLSID == self.CLSID && fld.lineOfSight(self.getPosX(), self.getPosY(), cr.getPosX(), cr.getPosY())) {
                    this.fKinsfolks.add(cr);
                    if (this.fNearKinsfolkDist > dist) {
                        this.fNearKinsfolkDist = dist;
                        this.fNearKinsfolk = cr;
                    }
                    this.IsLeader = (this.IsLeader && self.getLeadership() > cr.getLeadership());
                }
            }

            if (super.findGoalByKind(GoalKind.gk_Flock) == null) {
                GoalEntity goal = super.createGoal(GoalKind.gk_Flock);
                goal.Duration = 25;
            }
        } catch (Exception ex) {
            Logger.write("BeastBrain.prepareFlock(): " + ex.getMessage());
        }
    }

    private void prepareStalk()
    {
        try {
            NWCreature self = (NWCreature) super.fSelf;
            AbstractMap map = self.getCurrentMap();

            if (self.getEntry().Sign.equals("WildDog")) {
                NWTile tile = (NWTile) map.getTile(self.getPosX(), self.getPosY());

                if (tile.ScentTrail != null) {
                    int age = (int) tile.ScentAge;
                    int mx = self.getPosX();
                    int my = self.getPosY();

                    for (int y = self.getPosY() - 1; y <= self.getPosY() + 1; y++) {
                        for (int x = self.getPosX() - 1; x <= self.getPosX() + 1; x++) {
                            tile = ((NWTile) map.getTile(x, y));
                            if (age < (int) tile.ScentAge) {
                                age = (int) tile.ScentAge;
                                mx = x;
                                my = y;
                            }
                        }
                    }

                    StalkGoal goal = (StalkGoal) super.findGoalByKind(GoalKind.gk_Stalk);
                    if (goal == null) {
                        goal = ((StalkGoal) super.createGoal(GoalKind.gk_Stalk));
                    }
                    goal.Position = new Point(mx, my);
                    goal.Duration = 2;
                }
            }
        } catch (Exception ex) {
            Logger.write("BeastBrain.prepareStalk(): " + ex.getMessage());
        }
    }

    private void prepareTravel()
    {
        try {
            NWCreature self = (NWCreature) super.fSelf;

            if (super.findGoalByKind(GoalKind.gk_Travel) == null) {
                Point res = self.getNearestPlace(self.getSurvey(), true);
                if (res != null) {
                    TravelGoal goal = (TravelGoal) super.createGoal(GoalKind.gk_Travel);
                    goal.Position = res;
                    goal.Duration = 25;
                }
            }
        } catch (Exception ex) {
            Logger.write("BeastBrain.prepareTravel(): " + ex.getMessage());
        }
    }

    protected void prepareChase(CreatureEntity enemy, AttackRisk risk, boolean canMove)
    {
        EnemyChaseGoal chaseGoal = (EnemyChaseGoal) super.findGoalByKind(GoalKind.gk_EnemyChase);
        if (chaseGoal == null || !chaseGoal.Enemy.equals(enemy)) {
            chaseGoal = ((EnemyChaseGoal) super.createGoal(GoalKind.gk_EnemyChase));
            chaseGoal.Enemy = enemy;
            chaseGoal.Risk = risk;
            chaseGoal.CanMove = canMove;
        }
    }

    protected void prepareEvade(CreatureEntity enemy, AttackRisk risk, boolean canMove)
    {
        EnemyEvadeGoal evadeGoal = (EnemyEvadeGoal) super.findGoalByKind(GoalKind.gk_EnemyEvade);
        if (evadeGoal == null || !evadeGoal.Enemy.equals(enemy)) {
            evadeGoal = ((EnemyEvadeGoal) super.createGoal(GoalKind.gk_EnemyEvade));
            evadeGoal.Enemy = enemy;
            evadeGoal.Risk = risk;
            evadeGoal.CanMove = canMove;
        }
    }

    private void prepareEscort()
    {
        NWCreature self = (NWCreature) super.fSelf;
        EscortGoal goal = (EscortGoal) super.findGoalByKind(GoalKind.gk_Escort);
        if (goal != null) {
            if (goal.NotParty) {
                goal.Position = self.getNearestPlace(goal.Leader.getLocation(), 3, true);
            } else {
                LeaderBrain leaderBrain = (LeaderBrain) goal.Leader.getBrain();
                goal.Position = leaderBrain.getMemberPosition(super.fSelf);
            }
        }
    }

    @Override
    protected void evaluateGoal(GoalEntity goal)
    {
        switch (goal.Kind) {
            case GoalKind.gk_Unknown:
                goal.Value = 0f;
                break;

            case GoalKind.gk_Travel:
                goal.Value = 0.225f;
                break;

            case GoalKind.gk_PointGuard: {
                int dist = AuxUtils.distance(super.fSelf.getLocation(), ((PointGuardGoal) goal).Position);
                goal.Value = (0.2f + dist / 10.0f);
                break;
            }

            case GoalKind.gk_EnemyChase:
                goal.Value = 0.6f;
                break;

            case GoalKind.gk_EnemyEvade:
                goal.Value = 0.75f;
                if (this.Flock && !this.IsLeader) {
                    int kins = this.fKinsfolks.getCount();
                    goal.Value = ((0.75f - 0.1f * (float) kins));
                }
                break;

            case GoalKind.gk_Friend:
                goal.Value = 0.27f;
                break;

            case GoalKind.gk_AreaGuard:
                goal.Value = 0.2f;
                break;

            case GoalKind.gk_Escort: {
                int dist = AuxUtils.distance(super.fSelf.getLocation(), ((EscortGoal) goal).Position);
                goal.Value = ((0.3f + dist / 20.0f));
                break;
            }

            case GoalKind.gk_Flock:
                goal.Value = 0.22f;
                if (this.Flock && !this.IsLeader) {
                    int kins = this.fKinsfolks.getCount();
                    goal.Value = ((0.22f + 0.01f * (float) kins));
                }
                break;

            case GoalKind.gk_Stalk:
                goal.Value = 0.55f;
                break;
        }
    }

    @Override
    protected EmitterList getEmitters()
    {
        return (((NWCreature) super.fSelf).getCurrentField()).getEmitters();
    }

    @Override
    protected GoalEntity createGoalEx(int goalKind)
    {
        GoalEntity result = null;

        switch (goalKind) {
            case GoalKind.gk_Travel:
                result = new TravelGoal(this);
                break;

            case GoalKind.gk_PointGuard:
                result = new PointGuardGoal(this);
                break;

            case GoalKind.gk_ItemAcquire:
                result = new ItemAcquireGoal(this);
                break;

            case GoalKind.gk_EnemyChase:
                result = new EnemyChaseGoal(this);
                break;

            case GoalKind.gk_EnemyEvade:
                result = new EnemyEvadeGoal(this);
                break;

            case GoalKind.gk_AreaGuard:
                result = new AreaGuardGoal(this);
                break;

            case GoalKind.gk_ShopReturn:
                result = new ShopReturnGoal(this);
                break;

            case GoalKind.gk_PlayerFind:
                result = new PlayerFindGoal(this);
                break;

            case GoalKind.gk_Escort:
                result = new EscortGoal(this);
                break;

            case GoalKind.gk_Flock:
                result = new FlockGoal(this);
                break;

            case GoalKind.gk_DebtTake:
                result = new DebtTakeGoal(this);
                break;

            case GoalKind.gk_WareReturn:
                result = new WareReturnGoal(this);
                break;

            case GoalKind.gk_Stalk:
                result = new StalkGoal(this);
                break;
        }
        return result;
    }

    private void prepareEnemy()
    {
        try {
            NWCreature self = (NWCreature) super.fSelf;

            // search nearest enemy
            NWCreature enemy = self.findEnemy();

            if (enemy != null) {
                if (this.IsShipSail) {
                    this.prepareChase(enemy, AttackRisk.ar_Wary, false);
                    return;
                }
                
                AttackRisk ar;

                RaceID race = self.getEntry().Race;
                if (GlobalVars.Debug_Fury || race == RaceID.crAesir || race == RaceID.crEvilGod || race == RaceID.crDaemon) {
                    ar = AttackRisk.ar_Immediately;
                } else {
                    boolean vent = self.getEffects().findEffectByID(EffectID.eid_Ventriloquism) != null;
                    if (vent) {
                        ar = AttackRisk.ar_Wait;
                    } else {
                        float arVal = self.getAttackRate(enemy, this.fKinsfolks.getCount());
                        ar = BeastBrain.getRiskKind(arVal);
                    }
                }

                switch (ar) {
                    case ar_RunAway:
                    case ar_Evade:
                        this.prepareEvade(enemy, ar, true);
                        break;
                    case ar_Wait:
                        // dummy
                        break;
                    case ar_Wary:
                    case ar_Immediately:
                        this.prepareChase(enemy, ar, true);
                        break;
                }
            }
        } catch (Exception ex) {
            Logger.write("BeastBrain.prepareEnemy(): " + ex.getMessage());
        }
    }
    
    @Override
    protected void prepareGoals()
    {
        try {
            NWCreature self = (NWCreature) super.fSelf;

            // processing Skidbladnir
            Effect ef = self.getEffects().findEffectByID(EffectID.eid_Sail);
            this.IsShipSail = (ef != null);

            if (!this.IsShipSail) {
                this.prepareTravel();

                // flock behavior
                if (this.Flock) {
                    this.prepareFlock();
                }

                // chase by the smell
                this.prepareStalk();

                this.prepareEscort();
            }

            // process chase and evade
            this.prepareEnemy();
        } catch (Exception ex) {
            Logger.write("BeastBrain.prepareGoals(): " + ex.getMessage());
        }
    }

    public static AttackRisk getRiskKind(float risk)
    {
        AttackRisk result = AttackRisk.ar_Wait;
        if (risk < 1f) {
            result = AttackRisk.ar_RunAway;
        }
        if (risk < 0.75f) {
            result = AttackRisk.ar_Evade;
        }
        if (risk < 0.6f) {
            result = AttackRisk.ar_Wait;
        }
        if (risk < 0.4f) {
            result = AttackRisk.ar_Wary;
        }
        if (risk < 0.25f) {
            result = AttackRisk.ar_Immediately;
        }
        return result;
    }

    @Override
    public final void attack(CreatureEntity aEnemy, boolean onlyRemote)
    {
        try {
            NWCreature self = (NWCreature) super.fSelf;
            NWCreature enemy = (NWCreature) aEnemy;

            int dist = AuxUtils.distance(self.getLocation(), aEnemy.getLocation());

            boolean shooting = false;
            int highestDamage;
            Item weapon = null;

            if (self.getEntry().Flags.contains(CreatureFlags.esMind) && (self.getEntry().Flags.contains(CreatureFlags.esUseItems))) {
                boolean canShoot = self.canShoot(enemy);

                BestWeaponSigns bw = new BestWeaponSigns();
                if (canShoot) {
                    bw.include(BestWeaponSigns.bwsCanShoot);
                }
                if (onlyRemote) {
                    bw.include(BestWeaponSigns.bwsOnlyShoot);
                }

                highestDamage = self.checkEquipment((float) dist, bw);

                weapon = self.getItemByEquipmentKind(BodypartType.bp_RHand);
                ItemFlags ifs = (weapon != null) ? weapon.getFlags() : new ItemFlags();

                shooting = (canShoot && weapon != null && (ifs.hasIntersect(ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon)));
            } else {
                highestDamage = self.getDamageBase();
            }

            int skDamage = 0;
            RefObject<Integer> refSkDamage = new RefObject<>(skDamage);
            SkillID sk = self.getAttackSkill(dist, refSkDamage);
            skDamage = refSkDamage.argValue;
            boolean attackBySkill = (sk != SkillID.Sk_None && (skDamage > highestDamage || AuxUtils.chance(15)));

            if (attackBySkill) {
                EffectExt ext = new EffectExt();
                ext.setParam(EffectParams.ep_Creature, aEnemy);
                self.useSkill(sk, ext);
            } else {
                if (shooting) {
                    self.shootTo(enemy, weapon);
                } else {
                    if (!onlyRemote) {
                        if (dist == 1) {
                            self.attackTo(AttackKind.akMelee, enemy, null, null);
                        } else {
                            Point next = self.getStep(aEnemy.getLocation());
                            if (next != null) {
                                this.stepTo(next.X, next.Y);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("BeastBrain.attack(): " + ex.getMessage());
        }
    }

    @Override
    public Point getEvadePos(CreatureEntity enemy)
    {
        Point result = null;

        NWCreature self = (NWCreature) super.fSelf;
        if (this.Flock && this.fNearKinsfolk != null) {
            int epX = this.fNearKinsfolk.getPosX();
            int epY = this.fNearKinsfolk.getPosY();
            boolean res = self.canMove(self.getCurrentMap(), epX, epY);
            if (res) {
                return new Point(epX, epY);
            }
        }

        Directions dangerDirs = new Directions();

        if (enemy.getPosX() > self.getPosX()) {
            dangerDirs.include(Directions.dtEast);
        }
        if (enemy.getPosX() < self.getPosX()) {
            dangerDirs.include(Directions.dtWest);
        }
        if (enemy.getPosY() > self.getPosY()) {
            dangerDirs.include(Directions.dtSouth);
        }
        if (enemy.getPosY() < self.getPosY()) {
            dangerDirs.include(Directions.dtNorth);
        }

        if (dangerDirs.containsAll(Directions.dtNorth, Directions.dtWest)) {
            dangerDirs.include(Directions.dtNorthWest);
        }
        if (dangerDirs.containsAll(Directions.dtNorth, Directions.dtEast)) {
            dangerDirs.include(Directions.dtNorthEast);
        }
        if (dangerDirs.containsAll(Directions.dtSouth, Directions.dtWest)) {
            dangerDirs.include(Directions.dtSouthWest);
        }
        if (dangerDirs.containsAll(Directions.dtSouth, Directions.dtEast)) {
            dangerDirs.include(Directions.dtSouthEast);
        }

        for (int dir = Directions.dtFlatFirst; dir <= Directions.dtFlatLast; dir++) {
            if (!dangerDirs.contains(dir)) {
                int epX = self.getPosX() + Directions.Data[dir].dX;
                int epY = self.getPosY() + Directions.Data[dir].dY;

                if (self.canMove(self.getCurrentMap(), epX, epY)) {
                    return new Point(epX, epY);
                }
            }
        }

        return result;
    }

    @Override
    public boolean isAwareOfEmitter(Emitter emitter)
    {
        NWCreature iSelf = (NWCreature) super.fSelf;
        Point ePos = emitter.Position;

        boolean result = false;
        switch (emitter.EmitterKind) {
            case EmitterKind.ek_Unknown: {
                break;
            }
            case EmitterKind.ek_Damaged: {
                result = (emitter.SourceID == super.fSelf.UID);
                break;
            }
            case EmitterKind.ek_Combat:
            case EmitterKind.ek_BloodSpatter:
            case EmitterKind.ek_DeadBody:
            case EmitterKind.ek_Creature:
            case EmitterKind.ek_Item:
            case EmitterKind.ek_AngryTownsman:
            case EmitterKind.ek_UpsetTownsman: {
                result = (emitter.SourceID != super.fSelf.UID && iSelf.isSeen(ePos.X, ePos.Y, true));
                break;
            }
            case EmitterKind.ek_BattleSounds:
            case EmitterKind.ek_Missile:
            case EmitterKind.ek_GuardAlarm:
            case EmitterKind.ek_Call: {
                result = (emitter.SourceID != super.fSelf.UID && AuxUtils.distance(super.fSelf.getLocation(), ePos) < (int) ((NWCreature) iSelf).Hear);
                break;
            }
            default: {
                Logger.write("BeastBrain.isAwareOfEmitter(): Emitter not recognized: " + String.valueOf((int) emitter.EmitterKind));
                result = false;
                break;
            }
        }
        return result;
    }

    @Override
    public void stepTo(int aX, int aY)
    {
        super.fSelf.moveTo(aX, aY);
    }

    public final void setPointGuardGoal(Point point)
    {
        PointGuardGoal goal = (PointGuardGoal) super.createGoal(GoalKind.gk_PointGuard);
        goal.Position = point;
    }

    public final void setAreaGuardGoal(Rect area)
    {
        AreaGuardGoal goal = (AreaGuardGoal) super.createGoal(GoalKind.gk_AreaGuard);
        goal.Area = area;
    }

    public final void setEscortGoal(CreatureEntity leader, boolean isParty)
    {
        EscortGoal goal = (EscortGoal) super.createGoal(GoalKind.gk_Escort);
        goal.Leader = leader;
        goal.NotParty = !isParty;
    }
}
