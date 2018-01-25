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
using System.IO;
using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Creatures.Brain;
using NWR.Effects;
using NWR.Game;
using NWR.Game.Ghosts;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Core.Body;
using ZRLib.Map;

namespace NWR.Game
{
    public sealed class Player : NWCreature
    {
        public const int SatietyMax = 3000;

        private readonly Craft fCraft;
        private readonly Faith fFaith;
        private readonly Memory fMemory;

        private short fSatiety;

        public Craft Craft
        {
            get {
                return fCraft;
            }
        }

        public Faith Faith
        {
            get {
                return fFaith;
            }
        }

        public Memory Memory
        {
            get {
                return fMemory;
            }
        }

        public string DeathReason;
        public int DivinePatron;
        public NWField HalMap;
        public sbyte Morality;

        public int LandID;

        public Player(NWGameSpace space, object owner)
            : base(space, owner)
        {
            CLSID = GlobalVars.cid_Viking;
            Name = BaseLocale.GetStr(RS.rs_Unknown);
            Sex = CreatureSex.csMale;

            fMemory = new Memory(null);
            HalMap = new NWField(null, null, ExtPoint.Empty);
            fFaith = new Faith(this);
            fCraft = new Craft(this);

            InitBrain();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fMemory.Dispose();
            }
            base.Dispose(disposing);
        }

        private void ApplyLightEffect()
        {
            int rad = (int)LightRadius;
            if (rad > 0) {
                CreaturesList crList = ((NWField)CurrentMap).Creatures;
                int num = crList.Count;
                for (int i = 0; i < num; i++) {
                    NWCreature cr = crList.GetItem(i);
                    if (MathHelper.Distance(Location, cr.Location) <= rad && cr.Entry.Sign.Equals("Phausq")) {
                        cr.Death(BaseLocale.GetStr(RS.rs_MonsterDestroyedByLight), null);
                    }
                }

                // TODO: message for player: \"The light burns you!\"
            }
        }

        public short Satiety
        {
            get {
                return fSatiety;
            }
            set {
                if (fSatiety != value) {
                    if ((int)value > SatietyMax) {
                        fSatiety = (short)SatietyMax;
                    } else {
                        fSatiety = value;
                    }
                }
            }
        }


        /// <summary>
        /// Attack the enemy using chosen weapon. </summary>
        /// <param name="attackKind"> Kind of attack, internal. </param>
        /// <param name="enemy"> </param>
        /// <param name="weapon"> </param>
        /// <param name="projectile"> </param>
        /// <returns> Result of attack: true - for successful attack, false - for miss. </returns>
        public override bool AttackTo(AttackKind attackKind, NWCreature enemy, Item weapon, Item projectile)
        {
            try {
                bool result = base.AttackTo(attackKind, enemy, weapon, projectile);

                /*if (result) {
                }*/

                return result;
            } catch (Exception ex) {
                Logger.Write("Player.attackTo(): " + ex.Message);
                return false;
            }
        }

        public override bool Mercenary
        {
            get {
                return false;
            }
        }

        public override Movements Movements
        {
            get {
                Movements result = base.Movements;
    
                if (GlobalVars.Debug_Fly) {
                    result.Include(Movements.mkFly);
                }
                if (GlobalVars.Debug_Swim) {
                    result.Include(Movements.mkSwim);
                }
    
                return result;
            }
        }

        public override byte SerializeKind
        {
            get {
                return 0;
            }
        }

        public override byte Survey
        {
            get {
                DayTime dayTime = Space.DayTime;
                int bonus;
                if (dayTime < DayTime.dt_DayFH || dayTime >= DayTime.dt_Dusk) {
                    bonus = (int)LightRadius;
                } else {
                    bonus = 0;
                }
    
                NWField fld = (NWField)CurrentMap;
                float f;
                if (fld != null) {
                    if (fld.Dark) {
                        f = 0.33f;
                    } else {
                        var dt = GameTime.DayTimes[(int)Space.DayTime];
                        f = dt.RadMod;
                    }
                } else {
                    f = 1f;
                }
    
                return (byte)((Math.Round(((int)base.Survey * f))) + bonus);
            }
        }

        protected override void InitBody()
        {
            base.InitBody();
        }

        protected override void InitBrain()
        {
            if (Prowling) {
                fBrain = new BeastBrain(this);
                return;
            }

            Brain = new LeaderBrain(this);
        }

        public void ClearMercenaries()
        {
            if (fBrain is LeaderBrain) {
                LeaderBrain leader = ((LeaderBrain)fBrain);

                for (int i = leader.MembersCount - 1; i >= 1; i--) {
                    CreatureEntity member = leader.GetMember(i);
                    ((NWCreature)member).IsMercenary = false;
                }
            }
        }

        public void DiagnosisBody(StringList aList)
        {
            int eyes = 0;
            int fingers = 0;

            int num = Body.PartsCount;
            for (int i = 0; i < num; i++) {
                Bodypart entry = Body.GetPart(i);

                if (entry.Type == (int)BodypartType.bp_Eye && entry.State == Bodypart.STATE_NORMAL) {
                    eyes++;
                }

                if (entry.Type == (int)BodypartType.bp_Finger && entry.State == Bodypart.STATE_NORMAL) {
                    fingers++;
                }
            }

            if (eyes != 2) {
                aList.Add(BaseLocale.GetStr(RS.rs_Eyes) + " " + Convert.ToString(eyes));
            }

            if (fingers != 10) {
                aList.Add(BaseLocale.GetStr(RS.rs_Fingers) + " " + Convert.ToString(fingers));
            }

            if (aList.Count == 0) {
                aList.Add(BaseLocale.GetStr(RS.rs_PerfectHealth));
            }
        }

        private void PrepareHallucinations()
        {
            NWField field = CurrentField;
            HalMap = new NWField(null, field.Layer, field.Coords.Clone());

            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = (NWTile)field.GetTile(x, y);
                    NWTile halTile = (NWTile)HalMap.GetTile(x, y);
                    halTile.Assign(tile);

                    halTile.Back = Hallucination.GetPlaceID(tile.BackBase);
                    halTile.Fore = Hallucination.GetPlaceID(tile.ForeBase);
                }
            }

            HalMap.Normalize();
        }

        public override void DoTurn()
        {
            try {
                base.DoTurn();

                fSatiety -= 1;

                NWField map = (NWField)CurrentMap;
                if (Hallucinations) {
                    PrepareHallucinations();
                }

                // processing Skidbladnir
                Effect ef = Effects.FindEffectByID(EffectID.eid_Sail);
                if (ef != null) {
                    Item ship = FindItem("Skidbladnir");
                    EntityList members = ship.Contents;

                    for (int i = members.Count - 1; i >= 0; i--) {
                        NWCreature cr = (NWCreature)members.GetItem(i);
                        cr.ResetStamina();
                    }

                    int rest;
                    do {
                        rest = 0;
                        for (int i = members.Count - 1; i >= 0; i--) {
                            NWCreature cr = (NWCreature)members.GetItem(i);
                            if (cr.State != CreatureState.csDead) {

                                Effect ef1 = cr.Effects.FindEffectByID(EffectID.eid_Sail);
                                if (ef1 == null) {
                                    throw new Exception("gluk!");
                                }

                                cr.DoTurn();
                                if (cr.Stamina >= Speed) {
                                    rest++;
                                }
                            }
                        }
                    } while (rest != 0);
                }

                if (!Prowling) {
                    ApplyLightEffect();

                    if (!GlobalVars.Debug_Divinity) {
                        if (fSatiety == 200) {
                            GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_YouHungry));
                        } else {
                            if (fSatiety <= 150 && (int)fSatiety % 50 == 0) {
                                UseEffect(EffectID.eid_Swoon, null, InvokeMode.im_ItSelf, null);
                            } else {
                                if (fSatiety <= 0) {
                                    Death(BaseLocale.GetStr(RS.rs_Starved), null);
                                    return;
                                }
                            }
                        }

                        /*if (super.LayerID == GlobalVars.Layer_Muspelheim) {
                            // TODO: requires additional processing
                        }*/

                        if (LayerID == GlobalVars.Layer_Niflheim && GetAbility(AbilityID.Resist_Cold) == 0) {
                            int num3 = RandomHelper.GetRandom(2);
                            if (num3 != 0) {
                                if (num3 == 1) {
                                    GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_YouNumbedByColdOfNiflheim));
                                    Death(BaseLocale.GetStr(RS.rs_FrozeToDeath), null);
                                }
                            } else {
                                GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_NiflheimFrostDeath));
                                Death(BaseLocale.GetStr(RS.rs_TurnedToIce), null);
                            }
                            return;
                        }

                        if (LayerID == GlobalVars.Layer_Midgard && Field.X == 1 && Field.Y == 6 && GetAbility(AbilityID.Resist_Heat) == 0) {
                            int num4 = RandomHelper.GetRandom(2);
                            if (num4 != 0) {
                                if (num4 == 1) {
                                    GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_YouBurntByFlamesOfBifrost));
                                }
                            } else {
                                GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_BifrostFlameDeath));
                            }
                            Death(BaseLocale.GetStr(RS.rs_ConsumedByFire), null);
                            return;
                        }
                    }

                    if (!GlobalVars.Debug_DevMode && LayerID == GlobalVars.Layer_Vanaheim && map.Creatures.Count == 1) {
                        Morality = 0;
                        TransferTo(GlobalVars.Layer_Midgard, 2, 2, -1, -1, StaticData.MapArea, true, false);
                        GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_VanaheimIsEmpty), new LogFeatures(LogFeatures.lfDialog));
                    }

                    if (Morality <= -100 && LayerID != GlobalVars.Layer_Niflheim) {
                        TransferTo(GlobalVars.Layer_Niflheim, 1, 1, -1, -1, StaticData.MapArea, true, false);
                        GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_PlayerIsUnworthy), new LogFeatures(LogFeatures.lfDialog));
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Player.doTurn(): " + ex.Message);
            }
        }

        public string MoralityName
        {
            get {
                string result = "";
                if (Morality >= 50) {
                    result = BaseLocale.GetStr(RS.rs_MoralityHigh);
                } else {
                    if (Morality < 50) {
                        result = BaseLocale.GetStr(RS.rs_MoralityAverage);
                    } else {
                        if (Morality <= 0) {
                            result = BaseLocale.GetStr(RS.rs_MoralityLow);
                        } else {
                            if (Morality <= -50) {
                                result = BaseLocale.GetStr(RS.rs_MoralityAbsent);
                            }
                        }
                    }
                }
                return result;
            }
        }

        public int ScrollsCount
        {
            get {
                int result = 0;
    
                int num = Items.Count;
                for (int i = 0; i < num; i++) {
                    if (Items.GetItem(i).Kind == ItemKind.ik_Scroll) {
                        result++;
                    }
                }
    
                return result;
            }
        }

        public override void Init(int creatureID, bool total, bool setName)
        {
            try {
                base.Init(creatureID, total, setName);

                DeathReason = "";
                fSatiety = 2250;
                Morality = 100;

                if (CLSID_Renamed == GlobalVars.cid_Conjurer) {
                    MPMax = 5;
                    MPCur = 5;
                    SetSkill(SkillID.Sk_Spellcasting, 1);
                } else if (CLSID_Renamed == GlobalVars.cid_Woodsman) {
                    SetAbility(AbilityID.Ab_LongBow, 50);
                    SetAbility(AbilityID.Ab_CrossBow, 30);
                }

                int num = GlobalVars.dbKnowledges.Count;
                for (int i = 0; i < num; i++) {
                    KnowIt(GlobalVars.dbKnowledges[i]);
                }

                KnowIt(GlobalVars.cid_Balder);
                KnowIt(GlobalVars.cid_Thor);
                KnowIt(GlobalVars.cid_Freyr);
                KnowIt(GlobalVars.cid_Heimdall);
                KnowIt(GlobalVars.cid_Odin);
                KnowIt(GlobalVars.cid_Tyr);
                KnowIt(GlobalVars.cid_Fenrir);
                KnowIt(GlobalVars.cid_Loki);
                KnowIt(GlobalVars.cid_Surtr);
                KnowIt(GlobalVars.cid_Jormungand);
                KnowIt(GlobalVars.cid_Garm);
                KnowIt(GlobalVars.cid_Thokk);

                KnowIt(GlobalVars.Land_Bifrost);
                KnowIt(GlobalVars.Layer_Asgard);
                KnowIt(GlobalVars.Layer_Midgard);
                KnowIt(GlobalVars.Layer_Svartalfheim1);
                KnowIt(GlobalVars.Layer_Svartalfheim2);
                KnowIt(GlobalVars.Layer_Svartalfheim3);

                KnowIt(GlobalVars.iid_Mimming);
                KnowIt(GlobalVars.iid_Mjollnir);
                KnowIt(GlobalVars.iid_Gungnir);
                KnowIt(GlobalVars.iid_DwarvenArm);
                KnowIt(GlobalVars.iid_Gjall);
                KnowIt(GlobalVars.iid_SoulTrapping_Ring);

                KnowIt(GlobalVars.iid_Skidbladnir);
                KnowIt(GlobalVars.iid_Wand_Fire);

                KnowIt(GlobalVars.nwrDB.FindEntryBySign("Potion_Speed").GUID);
                KnowIt(GlobalVars.nwrDB.FindEntryBySign("Potion_Strength").GUID);
                KnowIt(GlobalVars.nwrDB.FindEntryBySign("Potion_Curing").GUID);
                KnowIt(GlobalVars.nwrDB.FindEntryBySign("Scroll_DispelHex").GUID);
                KnowIt(GlobalVars.nwrDB.FindEntryBySign("Scroll_Identification").GUID);
                KnowIt(GlobalVars.nwrDB.FindEntryBySign("Scroll_Blessing").GUID);
                KnowIt(GlobalVars.nwrDB.FindEntryBySign("Wand_Light").GUID);
                KnowIt(GlobalVars.nwrDB.FindEntryBySign("Wand_Ice").GUID);
                KnowIt(GlobalVars.nwrDB.FindEntryBySign("Ring_Agility").GUID);

                if (GlobalVars.Debug_DevMode) {
                    for (int pd = PlaceID.pid_First; pd <= PlaceID.pid_Last; pd++) {
                        if ((StaticData.dbPlaces[pd].Signs.Contains(PlaceFlags.psIsTrap))) {
                            StaticData.dbPlaces[pd].Signs.Include(PlaceFlags.psCanCreate);
                        }
                    }

                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("Potion_Curing").GUID, 5, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("FlintKnife").GUID, 15, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("Scroll_Extinction").GUID, 3, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("Potion_Mystery").GUID, 5, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("WhiteMushroom").GUID, 1, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("Potion_Hallucination").GUID, 1, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("SpeckledGrowth").GUID, 3, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("Wand_Wishing").GUID, 1, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("Potion_Lycanthropy").GUID, 5, true);

                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("Anvil").GUID, 1, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("Tongs").GUID, 1, true);
                    Item.GenItem(this, GlobalVars.nwrDB.FindEntryBySign("Wand_Fire").GUID, 5, true);

                    SetSkill(SkillID.Sk_Spellcasting, 250);
                    MPMax = 250;
                    MPCur = 250;

                    SetSkill(SkillID.Sk_Cartography, 1);
                    SetSkill(SkillID.Sk_DimensionTravel, 1);
                    SetSkill(SkillID.Sk_Alchemy, 1);
                    SetSkill(SkillID.Sk_Diagnosis, 1);
                    SetSkill(SkillID.Sk_Relocation, 1);
                    SetSkill(SkillID.Sk_Writing, 1);
                    SetSkill(SkillID.Sk_Dig, 1);
                    SetSkill(SkillID.Sk_Animation, 1);
                    SetSkill(SkillID.Sk_Ironworking, 1);
                    SetSkill(SkillID.Sk_ArrowMake, 1);
                    SetSkill(SkillID.Sk_Embalming, 1);
                    SetSkill(SkillID.Sk_GolemCreation, 1);
                    SetSkill(SkillID.Sk_Terraforming, 1);
                    SetSkill(SkillID.Sk_Husbandry, 1);
                    SetSkill(SkillID.Sk_Fennling, 1);
                    SetSkill(SkillID.Sk_SlaveUse, 1);
                    SetSkill(SkillID.Sk_Ventriloquism, 1);
                    SetSkill(SkillID.Sk_Taming, 1);
                    SetSkill(SkillID.Sk_MindControl, 1);
                    SetSkill(SkillID.Sk_PsiBlast, 1);
                    SetSkill(SkillID.Sk_HeatRadiation, 1);
                    SetSkill(SkillID.Sk_Prayer, 1);
                    SetSkill(SkillID.Sk_Sacrifice, 1);
                    SetSkill(SkillID.Sk_Divination, 1);
                    SetAbility(AbilityID.Ab_MusicalAcuity, 1);
                }
            } catch (Exception ex) {
                Logger.Write("Player.init(): " + ex.Message);
                throw ex;
            }
        }

        public override bool IsPlayer
        {
            get {
                return true;
            }
        }

        public override void RemoveItem(Item aItem)
        {
            base.RemoveItem(aItem);
        }

        public override void WearItem(Item aItem)
        {
            base.WearItem(aItem);
        }

        public override void KnowIt(int entityID)
        {
            base.KnowIt(entityID);

            string sign = GlobalVars.nwrDB.GetEntry(entityID).Sign;
            if (sign.StartsWith("Rune_")) {
                return;
            }

            Knowledge knw = (Knowledge)fMemory.Find(sign);

            if (knw != null) {
                knw.RefsCount++;
                return;
            }

            knw = new Knowledge(this);
            knw.ID = entityID;
            knw.RefsCount = (knw.RefsCount + 1);
            fMemory.Add(sign, knw);
        }

        public override void MoveTo(int newX, int newY)
        {
            if (Effects.FindEffectByID(EffectID.eid_LegsMissing) != null && !HasMovement(Movements.mkFly)) {
                GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_YouHaveNoLegs));
            } else {
                if (TotalWeight > MaxItemsWeight) {
                    GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_YouOverburdened));
                } else {
                    base.MoveTo(newX, newY);
                    if (GlobalVars.nwrWin.AutoPickup) {
                        PickupAll();
                    }
                    Space.ShowPlaceInfo(PosX, PosY, false);
                }
            }
        }

        public override bool CanMove(IMap map, int aX, int aY)
        {
            bool result = base.CanMove(map, aX, aY);

            NWField fld = (NWField)map;
            int bg = fld.GetTile(aX, aY).BackBase;
            result = result || (bg == PlaceID.pid_Space) || (GlobalVars.Debug_Divinity);

            return result;
        }

        public override void EnterPlace(NWField field, NWTile tile)
        {
            try {
                base.EnterPlace(field, tile);

                if (field.LandID == GlobalVars.Land_Crossroads) {
                    if (tile.ForeBase == PlaceID.pid_cr_Disk) {
                        tile.Fore = PlaceID.pid_cr_Disk_Pressed;
                    } else {
                        int lid = -1;
                        int fx = 0;
                        int fy = 0;
                        switch ((sbyte)tile.Background) {
                            case 31:
                                {
                                    NWField f = Space.GetRndFieldByLands(GlobalVars.Land_Niflheim, GlobalVars.Land_GiollRiver);
                                    if (f != null) {
                                        lid = f.Layer.EntryID;
                                        fx = f.Coords.X;
                                        fy = f.Coords.Y;
                                    }
                                    break;
                                }
                            case 32:
                            case 33:
                                {
                                    NWField f = Space.GetRndFieldByLands(GlobalVars.Land_SlaeterSea, GlobalVars.Land_VidRiver);
                                    if (f != null) {
                                        lid = f.Layer.EntryID;
                                        fx = f.Coords.X;
                                        fy = f.Coords.Y;
                                    }
                                    break;
                                }
                            case 34:
                            case 35:
                                {
                                    NWField f = Space.GetRndFieldByLands(GlobalVars.Land_Caves, GlobalVars.Land_DeepCaves, GlobalVars.Land_GreatCaves, GlobalVars.Land_Crypt, GlobalVars.Land_Bazaar);
                                    if (f != null) {
                                        lid = f.Layer.EntryID;
                                        fx = f.Coords.X;
                                        fy = f.Coords.Y;
                                    }
                                    break;
                                }
                            case 36:
                                {
                                    NWField f = Space.GetRndFieldByLands(GlobalVars.Land_Nidavellir);
                                    if (f != null) {
                                        lid = GlobalVars.Layer_Midgard;
                                        fx = f.Coords.X;
                                        fy = f.Coords.Y;
                                    }
                                    break;
                                }
                            case 37:
                                {
                                    NWField f = Space.GetRndFieldByLands(GlobalVars.Land_Forest, GlobalVars.Land_Village, GlobalVars.Land_Jotenheim);
                                    if (f != null) {
                                        lid = f.Layer.EntryID;
                                        fx = f.Coords.X;
                                        fy = f.Coords.Y;
                                    }
                                    break;
                                }
                            case 38:
                                {
                                    NWField f = Space.GetRndFieldByLands(GlobalVars.Land_Wasteland, GlobalVars.Land_Muspelheim);
                                    if (f != null) {
                                        lid = f.Layer.EntryID;
                                        fx = f.Coords.X;
                                        fy = f.Coords.Y;
                                    }
                                    break;
                                }
                            case 39:
                            case 40:
                                {
                                    lid = GlobalVars.Layer_GrynrHalls;
                                    fx = 0;
                                    fy = 0;
                                    break;
                                }
                            case 41:
                            case 42:
                                {
                                    NWField f = Space.GetRndFieldByLands(GlobalVars.Land_Alfheim, GlobalVars.Land_MimerRealm);
                                    if (f != null) {
                                        lid = f.Layer.EntryID;
                                        fx = f.Coords.X;
                                        fy = f.Coords.Y;
                                    }
                                    break;
                                }
                        }

                        if (lid != -1) {
                            TransferTo(lid, fx, fy, -1, -1, StaticData.MapArea, true, true);
                        }
                    }
                } else {
                    tile.ScentAge = 100;
                    tile.ScentTrail = this;
                }
            } catch (Exception ex) {
                Logger.Write("Player.enterPlace(): " + ex.Message);
                throw ex;
            }
        }

        public override void LeavePlace(NWField field, NWTile tile)
        {
            try {
                base.LeavePlace(field, tile);

                if (field.LandID == GlobalVars.Land_Crossroads) {
                    int fg = tile.ForeBase;
                    if (fg == PlaceID.pid_cr_Disk || fg == PlaceID.pid_cr_Disk_Pressed) {
                        tile.Fore = PlaceID.pid_Undefined;
                        GlobalVars.nwrWin.ShowText(this, BaseLocale.GetStr(RS.rs_DiskFalls));
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Player.leavePlace(): " + ex.Message);
                throw ex;
            }
        }

        public void RecruitMercenary(NWCreature collocutor, NWCreature mercenary, bool byMoney)
        {
            bool res = true;
            if (byMoney) {
                int hPrice = (int)mercenary.HirePrice;
                if (Money < hPrice) {
                    res = false;
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_NoMoney));
                } else {
                    SubMoney(hPrice);
                    collocutor.AddMoney(hPrice);
                }
            }

            if (res) {
                mercenary.IsMercenary = true;

                int dist = MathHelper.Distance(Location, mercenary.Location);
                ExtPoint pt = GetNearestPlace(3, true);
                if ((dist > (int)Survey) && (!pt.IsEmpty)) {
                    mercenary.CheckTile(false);
                    mercenary.SetPos(pt.X, pt.Y);
                    mercenary.CheckTile(true);
                }
            }
        }

        public void RandomName()
        {
            Name = Space.GenerateName(this, NamesLib.NameGen_NorseDic); //NameGen_RndSlabs
        }

        public override void LoadFromStream(BinaryReader stream, FileVersion version)
        {
            try {
                base.LoadFromStream(stream, version);

                fSatiety = (short)StreamUtils.ReadWord(stream);
                fMemory.LoadFromStream(stream, version);
                Morality = (sbyte)StreamUtils.ReadByte(stream);
                ((LeaderBrain)fBrain).Formation = (PartyFormation)(StreamUtils.ReadByte(stream));
            } catch (Exception ex) {
                Logger.Write("Player.loadFromStream(): " + ex.Message);
                throw ex;
            }
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            base.SaveToStream(stream, version);

            StreamUtils.WriteWord(stream, (ushort)fSatiety);
            fMemory.SaveToStream(stream, version);
            StreamUtils.WriteByte(stream, (byte)Morality);
            StreamUtils.WriteByte(stream, (byte)((LeaderBrain)fBrain).Formation);
        }

        public Ghost CreateGhost()
        {
            int landID = CurrentField.LandID;
            if (landID != GlobalVars.Land_Forest && landID != GlobalVars.Land_Caves && landID != GlobalVars.Land_DeepCaves && landID != GlobalVars.Land_GrynrHalls) {
                return null;
            }

            Ghost result = new Ghost(null, null);
            result.Assign(this, false);
            result.HPCur = result.HPMax_Renamed;
            result.Alignment = Alignment.am_Chaotic_Evil;

            result.CopyTotalLocation(this);

            result.LastDir = Directions.DtNone;
            result.Ghost = true;

            return result;
        }

        public PartyFormation PartyFormation
        {
            set {
                ((LeaderBrain)fBrain).Formation = value;
            }
        }

        public override void SetPos(int posX, int posY)
        {
            try {
                base.SetPos(posX, posY);

                if (!Prowling) {
                    ((LeaderBrain)fBrain).Dir = LastDir;
                }

                AbstractMap fld = CurrentMap;
                FOV.FOV_Prepare(fld, false);
                if (Effects.FindEffectByID(EffectID.eid_Blindness) == null && !InFog) {
                    int dir;
                    if (GlobalVars.nwrWin.CircularFOV) {
                        dir = Directions.DtNone;
                    } else {
                        dir = LastDir;
                    }
                    FOV.FOV_Start(PosX, PosY, (int)base.Survey, dir);
                }

                // this is need for work of the brain goals, also isAvailable and etc
                Effect ef = Effects.FindEffectByID(EffectID.eid_Sail);
                if (ef != null) {
                    Item ship = FindItem("Skidbladnir");
                    EntityList members = ship.Contents;

                    int fx = Field.X;
                    int fy = Field.Y;

                    for (int i = members.Count - 1; i >= 0; i--) {
                        NWCreature cr = (NWCreature)members.GetItem(i);
                        cr.SetGlobalPos(LayerID, fx, fy, true);
                        cr.SetPos(posX, posY);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Player.setPos(): " + ex.Message);
                throw ex;
            }
        }

        public int GetDebt(string aLender)
        {
            Debt debt = (Debt)fMemory.Find("Debt_" + aLender);
            int result;
            if (debt == null) {
                result = 0;
            } else {
                result = debt.Value;
            }
            return result;
        }

        public void SetRecallPos()
        {
            fMemory.Delete("RecallPos");
            RecallPos rp = new RecallPos(this);
            rp.Layer = LayerID;
            rp.Field = Field;
            rp.Pos = Location;
            fMemory.Add("RecallPos", rp);
        }

        public void SetSourceForm()
        {
            /*if (this.FMemory.Find("SourceForm") != null) {
             this.FMemory.Delete("SourceForm");
             }*/

            SourceForm sf = new SourceForm(this);
            sf.SfID = CLSID_Renamed;
            fMemory.Add("SourceForm", sf);
        }

        public void AddDebt(string lender, int value)
        {
            Debt debt = (Debt)Memory.Find("Debt_" + lender);
            if (debt == null) {
                debt = new Debt(this);
                debt.Lender = lender;
                fMemory.Add("Debt_" + lender, debt);
            }
            debt.Value += value;
        }

        public void SubDebt(string lender, int value)
        {
            Debt debt = (Debt)Memory.Find("Debt_" + lender);
            if (debt != null) {
                if (debt.Value > value) {
                    debt.Value -= value;
                } else {
                    fMemory.Delete("Debt_" + lender);
                }
            }
        }

        public void TeachDiscipline(NWCreature teacher, int teachableIndex, int curLev)
        {
            LeaderBrain party = (LeaderBrain)fBrain;
            int id = StaticData.dbTeachable[teachableIndex].Id;
            TeachableKind kind = StaticData.dbTeachable[teachableIndex].Kind;

            int price = (int)Space.GetTeachablePrice(teachableIndex, curLev);
            SubMoney(price);
            teacher.AddMoney(price);

            switch (kind) {
                case TeachableKind.Ability:
                    {
                        SetAbility((AbilityID)id, curLev + 1);

                        int num2 = party.MembersCount;
                        for (int i = 1; i < num2; i++) {
                            NWCreature j = (NWCreature)party.GetMember(i);
                            j.SetAbility((AbilityID)id, curLev + 1);
                        }
                    }
                    break;

                case TeachableKind.Skill:
                    {
                        SetSkill((SkillID)id, curLev + 1);

                        int num = party.MembersCount;
                        for (int i = 1; i < num; i++) {
                            NWCreature j = (NWCreature)party.GetMember(i);
                            j.SetSkill((SkillID)id, curLev + 1);
                        }
                    }
                    break;
            }
        }

        public bool Sail
        {
            get {
                return (Effects.FindEffectByID(EffectID.eid_Sail) != null);
            }
        }

        public override void TransferTo(int layerID, int fX, int fY, int pX, int pY, ExtRect area, bool obligatory, bool controlled)
        {
            try {
                NWField field = Space.GetField(layerID, fX, fY);
                bool withoutParty = (field.LandID == GlobalVars.Land_Crossroads) || Sail;

                bool globalMove = (LayerID != layerID || Field.X != fX || Field.Y != fY);
                bool partyMove = (globalMove && controlled && !withoutParty);

                base.TransferTo(layerID, fX, fY, pX, pY, area, obligatory, controlled);

                /*if (globalMove) {
                    // ???
                }*/

                if (partyMove) {
                    try {
                        LeaderBrain party = (LeaderBrain)fBrain;
                        int num = party.MembersCount;
                        for (int i = 1; i < num; i++) {
                            NWCreature member = (NWCreature)party.GetMember(i);

                            ExtPoint pt = member.GetNearestPlace(Location, 4, true);
                            if (!pt.IsEmpty) {
                                member.TransferTo(layerID, fX, fY, pt.X, pt.Y, area, obligatory, true);
                            } else {
                                Logger.Write("Player.transferTo().transferParty().getNearestPlace() failed");
                            }
                        }
                    } catch (Exception ex) {
                        Logger.Write("Player.transferTo().transferParty(): " + ex.Message);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Player.transferTo(): " + ex.Message);
                throw ex;
            }
        }

        public override void UseEffect(EffectID effectID, object source, InvokeMode invokeMode, EffectExt ext)
        {
            bool paramsValid = Effect.InitParams(effectID, this, source, invokeMode, ref ext);

            if (paramsValid) {
                base.UseEffect(effectID, source, invokeMode, ext);
            } else {
                GlobalVars.nwrWin.InitTarget(effectID, source, invokeMode, ext);
            }
        }

        public void WaitTurn()
        {
            AbstractMap fld = CurrentMap;

            FOV.FOV_Prepare(fld, false);
            if (Effects.FindEffectByID(EffectID.eid_Blindness) == null) {
                FOV.FOV_Start(PosX, PosY, (int)base.Survey, Directions.DtNone);
            }

            CheckTile(true);
        }

        public string LocationName
        {
            get {
                NWField fld = CurrentField;
                Region region = fld.GetRegion(PosX, PosY);
    
                return (region != null) ? region.Name : fld.LandName;
            }
        }
    }

}