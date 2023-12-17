/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014, 2020 by Serg V. Zhdanovskih.
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
using System.Collections.Generic;
using BSLib;
using NWR.Creatures.Brain.Goals;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.Game.Types;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Core.Brain;
using ZRLib.Map;

namespace NWR.Creatures.Brain
{
    public class BeastBrain : NWBrainEntity
    {
        public CreatureEntity fNearKinsfolk;
        public int fNearKinsfolkDist;

        public bool Flock;
        public bool IsLeader;
        public bool IsShipSail;
        public List<NWCreature> Kinsfolks;


        protected override EmitterList Emitters
        {
            get {
                return (((NWCreature)fSelf).CurrentField).Emitters;
            }
        }


        public BeastBrain(CreatureEntity owner) : base(owner)
        {
            Kinsfolks = new List<NWCreature>();
        }

        private void PrepareFlock()
        {
            try {
                IsLeader = true;
                Kinsfolks.Clear();

                NWCreature self = (NWCreature)fSelf;
                NWField fld = self.CurrentField;

                fNearKinsfolk = null;
                fNearKinsfolkDist = AuxUtils.MaxInt;

                int num = fld.Creatures.Count;
                for (int i = 0; i < num; i++) {
                    NWCreature cr = fld.Creatures[i];
                    int dist = MathHelper.Distance(cr.Location, self.Location);
                    if (!cr.Equals(self) && dist <= self.Survey && cr.CLSID == self.CLSID && fld.LineOfSight(self.PosX, self.PosY, cr.PosX, cr.PosY)) {
                        Kinsfolks.Add(cr);
                        if (fNearKinsfolkDist > dist) {
                            fNearKinsfolkDist = dist;
                            fNearKinsfolk = cr;
                        }
                        IsLeader = (IsLeader && self.Leadership > cr.Leadership);
                    }
                }

                if (FindGoalByKind(GoalKind.gk_Flock) == null) {
                    GoalEntity goal = CreateGoal(GoalKind.gk_Flock);
                    goal.Duration = 25;
                }
            } catch (Exception ex) {
                Logger.Write("BeastBrain.prepareFlock(): " + ex.Message);
            }
        }

        private void PrepareStalk()
        {
            try {
                NWCreature self = (NWCreature)fSelf;
                AbstractMap map = self.CurrentField;

                if (self.Entry.Sign.Equals("WildDog")) {
                    NWTile tile = (NWTile)map.GetTile(self.PosX, self.PosY);

                    if (tile.ScentTrail != null) {
                        int age = (int)tile.ScentAge;
                        int mx = self.PosX;
                        int my = self.PosY;

                        for (int y = self.PosY - 1; y <= self.PosY + 1; y++) {
                            for (int x = self.PosX - 1; x <= self.PosX + 1; x++) {
                                tile = ((NWTile)map.GetTile(x, y));
                                if (age < (int)tile.ScentAge) {
                                    age = (int)tile.ScentAge;
                                    mx = x;
                                    my = y;
                                }
                            }
                        }

                        StalkGoal goal = (StalkGoal)FindGoalByKind(GoalKind.gk_Stalk);
                        if (goal == null) {
                            goal = ((StalkGoal)CreateGoal(GoalKind.gk_Stalk));
                        }
                        goal.Position = new ExtPoint(mx, my);
                        goal.Duration = 2;
                    }
                }
            } catch (Exception ex) {
                Logger.Write("BeastBrain.prepareStalk(): " + ex.Message);
            }
        }

        private void PrepareTravel()
        {
            try {
                NWCreature self = (NWCreature)fSelf;

                if (FindGoalByKind(GoalKind.gk_Travel) == null) {
                    ExtPoint res = self.GetNearestPlace(self.Survey, true);
                    if (!res.IsEmpty) {
                        TravelGoal goal = (TravelGoal)CreateGoal(GoalKind.gk_Travel);
                        goal.Position = res;
                        goal.Duration = 25;
                    }
                }
            } catch (Exception ex) {
                Logger.Write("BeastBrain.prepareTravel(): " + ex.Message);
            }
        }

        protected virtual void PrepareChase(CreatureEntity enemy, AttackRisk risk, bool canMove)
        {
            EnemyChaseGoal chaseGoal = (EnemyChaseGoal)FindGoalByKind(GoalKind.gk_EnemyChase);
            if (chaseGoal == null || !chaseGoal.Enemy.Equals(enemy)) {
                chaseGoal = ((EnemyChaseGoal)CreateGoal(GoalKind.gk_EnemyChase));
                chaseGoal.Enemy = enemy;
                chaseGoal.Risk = risk;
                chaseGoal.CanMove = canMove;
            }
        }

        protected virtual void PrepareEvade(CreatureEntity enemy, AttackRisk risk, bool canMove)
        {
            EnemyEvadeGoal evadeGoal = (EnemyEvadeGoal)FindGoalByKind(GoalKind.gk_EnemyEvade);
            if (evadeGoal == null || !evadeGoal.Enemy.Equals(enemy)) {
                evadeGoal = ((EnemyEvadeGoal)CreateGoal(GoalKind.gk_EnemyEvade));
                evadeGoal.Enemy = enemy;
                evadeGoal.Risk = risk;
                evadeGoal.CanMove = canMove;
            }
        }

        private void PrepareEscort()
        {
            NWCreature self = (NWCreature)fSelf;
            EscortGoal goal = (EscortGoal)FindGoalByKind(GoalKind.gk_Escort);
            if (goal != null) {
                if (goal.NotParty) {
                    goal.Position = self.GetNearestPlace(goal.Leader.Location, 3, true);
                } else {
                    LeaderBrain leaderBrain = (LeaderBrain)goal.Leader.Brain;
                    goal.Position = leaderBrain.GetMemberPosition(self);
                }
            }
        }

        protected override void EvaluateGoal(GoalEntity goal)
        {
            switch (goal.Kind) {
                case GoalKind.gk_Unknown:
                    goal.Value = 0f;
                    break;

                case GoalKind.gk_Travel:
                    goal.Value = 0.225f;
                    break;

                case GoalKind.gk_PointGuard:
                    {
                        int dist = MathHelper.Distance(fSelf.Location, ((PointGuardGoal)goal).Position);
                        goal.Value = (0.2f + dist / 10.0f);
                        break;
                    }

                case GoalKind.gk_EnemyChase:
                    goal.Value = 0.6f;
                    break;

                case GoalKind.gk_EnemyEvade:
                    goal.Value = 0.75f;
                    if (Flock && !IsLeader) {
                        goal.Value = ((0.75f - 0.1f * Kinsfolks.Count));
                    }
                    break;

                case GoalKind.gk_Friend:
                    goal.Value = 0.27f;
                    break;

                case GoalKind.gk_AreaGuard:
                    goal.Value = 0.2f;
                    break;

                case GoalKind.gk_Escort:
                    {
                        int dist = MathHelper.Distance(fSelf.Location, ((EscortGoal)goal).Position);
                        goal.Value = ((0.3f + dist / 20.0f));
                        break;
                    }

                case GoalKind.gk_Flock:
                    goal.Value = 0.22f;
                    if (Flock && !IsLeader) {
                        goal.Value = ((0.22f + 0.01f * Kinsfolks.Count));
                    }
                    break;

                case GoalKind.gk_Stalk:
                    goal.Value = 0.55f;
                    break;
            }
        }

        protected override GoalEntity CreateGoalEx(int goalKind)
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

        private void PrepareEnemy()
        {
            try {
                NWCreature self = (NWCreature)fSelf;

                // search nearest enemy
                NWCreature enemy = self.FindEnemy();

                if (enemy != null) {
                    if (IsShipSail) {
                        PrepareChase(enemy, AttackRisk.Wary, false);
                        return;
                    }

                    AttackRisk ar;

                    RaceID race = self.Entry.Race;
                    if (GlobalVars.Debug_Fury || race == RaceID.crAesir || race == RaceID.crEvilGod || race == RaceID.crDaemon) {
                        ar = AttackRisk.Immediately;
                    } else {
                        bool vent = self.Effects.FindEffectByID(EffectID.eid_Ventriloquism) != null;
                        if (vent) {
                            ar = AttackRisk.Wait;
                        } else {
                            float arVal = self.GetAttackRate(enemy, Kinsfolks.Count);
                            ar = GetRiskKind(arVal);
                        }
                    }

                    switch (ar) {
                        case AttackRisk.RunAway:
                        case AttackRisk.Evade:
                            PrepareEvade(enemy, ar, true);
                            break;

                        case AttackRisk.Wait:
                            // dummy
                            break;

                        case AttackRisk.Wary:
                        case AttackRisk.Immediately:
                            PrepareChase(enemy, ar, true);
                            break;
                    }
                }
            } catch (Exception ex) {
                Logger.Write("BeastBrain.prepareEnemy(): " + ex.Message);
            }
        }

        protected override void PrepareGoals()
        {
            try {
                NWCreature self = (NWCreature)fSelf;

                // processing Skidbladnir
                Effect ef = self.Effects.FindEffectByID(EffectID.eid_Sail);
                IsShipSail = (ef != null);

                if (!IsShipSail) {
                    PrepareTravel();

                    // flock behavior
                    if (Flock) {
                        PrepareFlock();
                    }

                    // chase by the smell
                    PrepareStalk();

                    PrepareEscort();
                }

                // process chase and evade
                PrepareEnemy();
            } catch (Exception ex) {
                Logger.Write("BeastBrain.prepareGoals(): " + ex.Message);
            }
        }

        public static AttackRisk GetRiskKind(float risk)
        {
            AttackRisk result = AttackRisk.Wait;
            if (risk < 1f) {
                result = AttackRisk.RunAway;
            }
            if (risk < 0.75f) {
                result = AttackRisk.Evade;
            }
            if (risk < 0.6f) {
                result = AttackRisk.Wait;
            }
            if (risk < 0.4f) {
                result = AttackRisk.Wary;
            }
            if (risk < 0.25f) {
                result = AttackRisk.Immediately;
            }
            return result;
        }

        public override void Attack(CreatureEntity aEnemy, bool onlyRemote)
        {
            try {
                NWCreature self = (NWCreature)fSelf;
                NWCreature enemy = (NWCreature)aEnemy;

                int dist = MathHelper.Distance(self.Location, aEnemy.Location);

                bool shooting = false;
                int highestDamage;
                Item weapon = null;

                if (self.Entry.Flags.Contains(CreatureFlags.esMind) && (self.Entry.Flags.Contains(CreatureFlags.esUseItems))) {
                    bool canShoot = self.CanShoot(enemy);

                    BestWeaponSigns bw = new BestWeaponSigns();
                    if (canShoot) {
                        bw.Include(BestWeaponSigns.CanShoot);
                    }
                    if (onlyRemote) {
                        bw.Include(BestWeaponSigns.OnlyShoot);
                    }

                    highestDamage = self.CheckEquipment((float)dist, bw);

                    weapon = self.GetItemByEquipmentKind(BodypartType.bp_RHand);
                    ItemFlags ifs = (weapon != null) ? weapon.Flags : new ItemFlags();

                    shooting = (canShoot && weapon != null && (ifs.HasIntersect(ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon)));
                } else {
                    highestDamage = self.DamageBase;
                }

                int skDamage = 0;
                SkillID sk = self.GetAttackSkill(dist, ref skDamage);
                bool attackBySkill = (sk != SkillID.Sk_None && (skDamage > highestDamage || AuxUtils.Chance(15)));

                if (attackBySkill) {
                    EffectExt ext = new EffectExt();
                    ext.SetParam(EffectParams.ep_Creature, aEnemy);
                    self.UseSkill(sk, ext);
                } else {
                    if (shooting) {
                        self.ShootTo(enemy, weapon);
                    } else {
                        if (!onlyRemote) {
                            if (dist == 1) {
                                self.AttackTo(AttackKind.Melee, enemy, null, null);
                            } else {
                                ExtPoint next = self.GetStep(aEnemy.Location);
                                if (!next.IsEmpty) {
                                    StepTo(next.X, next.Y);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("BeastBrain.attack(): " + ex.Message);
            }
        }

        public override ExtPoint GetEvadePos(CreatureEntity enemy)
        {
            ExtPoint result = ExtPoint.Empty;

            NWCreature self = (NWCreature)fSelf;
            if (Flock && fNearKinsfolk != null) {
                int epX = fNearKinsfolk.PosX;
                int epY = fNearKinsfolk.PosY;
                bool res = self.CanMove(self.CurrentField, epX, epY);
                if (res) {
                    return new ExtPoint(epX, epY);
                }
            }

            Directions dangerDirs = new Directions();

            if (enemy.PosX > self.PosX) {
                dangerDirs.Include(Directions.DtEast);
            }
            if (enemy.PosX < self.PosX) {
                dangerDirs.Include(Directions.DtWest);
            }
            if (enemy.PosY > self.PosY) {
                dangerDirs.Include(Directions.DtSouth);
            }
            if (enemy.PosY < self.PosY) {
                dangerDirs.Include(Directions.DtNorth);
            }

            if (dangerDirs.ContainsAll(Directions.DtNorth, Directions.DtWest)) {
                dangerDirs.Include(Directions.DtNorthWest);
            }
            if (dangerDirs.ContainsAll(Directions.DtNorth, Directions.DtEast)) {
                dangerDirs.Include(Directions.DtNorthEast);
            }
            if (dangerDirs.ContainsAll(Directions.DtSouth, Directions.DtWest)) {
                dangerDirs.Include(Directions.DtSouthWest);
            }
            if (dangerDirs.ContainsAll(Directions.DtSouth, Directions.DtEast)) {
                dangerDirs.Include(Directions.DtSouthEast);
            }

            for (int dir = Directions.DtFlatFirst; dir <= Directions.DtFlatLast; dir++) {
                if (!dangerDirs.Contains(dir)) {
                    int epX = self.PosX + Directions.Data[dir].DX;
                    int epY = self.PosY + Directions.Data[dir].DY;

                    if (self.CanMove(self.CurrentField, epX, epY)) {
                        return new ExtPoint(epX, epY);
                    }
                }
            }

            return result;
        }

        public override bool IsAwareOfEmitter(Emitter emitter)
        {
            NWCreature iSelf = (NWCreature)fSelf;
            ExtPoint ePos = emitter.Position;

            bool result = false;
            switch (emitter.EmitterKind) {
                case EmitterKind.ek_Unknown:
                    {
                        break;
                    }
                case EmitterKind.ek_Damaged:
                    {
                        result = (emitter.SourceID == fSelf.UID);
                        break;
                    }
                case EmitterKind.ek_Combat:
                case EmitterKind.ek_BloodSpatter:
                case EmitterKind.ek_DeadBody:
                case EmitterKind.ek_Creature:
                case EmitterKind.ek_Item:
                case EmitterKind.ek_AngryTownsman:
                case EmitterKind.ek_UpsetTownsman:
                    {
                        result = (emitter.SourceID != fSelf.UID && iSelf.IsSeen(ePos.X, ePos.Y, true));
                        break;
                    }
                case EmitterKind.ek_BattleSounds:
                case EmitterKind.ek_Missile:
                case EmitterKind.ek_GuardAlarm:
                case EmitterKind.ek_Call:
                    {
                        result = (emitter.SourceID != fSelf.UID && MathHelper.Distance(fSelf.Location, ePos) < (int)((NWCreature)iSelf).Hear);
                        break;
                    }
                default:
                    {
                        Logger.Write("BeastBrain.isAwareOfEmitter(): Emitter not recognized: " + Convert.ToString((int)emitter.EmitterKind));
                        result = false;
                        break;
                    }
            }
            return result;
        }

        public override void StepTo(int aX, int aY)
        {
            fSelf.MoveTo(aX, aY);
        }

        public void SetPointGuardGoal(ExtPoint value)
        {
            PointGuardGoal goal = (PointGuardGoal)CreateGoal(GoalKind.gk_PointGuard);
            goal.Position = value;
        }

        public void SetAreaGuardGoal(ExtRect value)
        {
            AreaGuardGoal goal = (AreaGuardGoal)CreateGoal(GoalKind.gk_AreaGuard);
            goal.Area = value;
        }

        public void SetEscortGoal(CreatureEntity leader, bool isParty)
        {
            EscortGoal goal = (EscortGoal)CreateGoal(GoalKind.gk_Escort);
            goal.Leader = leader;
            goal.NotParty = !isParty;
        }
    }
}
