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
using System.IO;
using BSLib;
using NWR.Creatures.Brain;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.Game.Types;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Core.Body;
using ZRLib.Grammar;
using ZRLib.Map;

namespace NWR.Creatures
{
    using ItemStates = EnumSet<ItemState>;

    public class NWCreature : CreatureEntity
    {
        protected sealed class AttackInfo
        {
            public int ToHit;
            public int Damage;
            public int Parry;
            public CreatureAction Action;
        }

        private static readonly string[] NotImprisonable;
        private static readonly Reaction[, ] Relations;
        public static readonly int[] ShootIsoTrans;

        private AttributeList fAbilities;
        private AbstractBody fBody;
        private int fDamageBase;
        private EffectsList fEffects;
        private CreatureEntry fEntry;
        private int fExperience;
        private ExtPoint fField;
        private Building fHouse;
        private bool fInFog;
        private bool fIsMercenary;
        private ItemsList fItems;
        //private bool FItemsEnlarged;
        private bool fIsTrader;
        private string fName;
        private EffectID fProwlSource;
        private AttributeList fSkills;
        private int fSpeed;
        private CreatureState fState;
        private byte fSurvey;

        public int SpeedMod;

        public Alignment Alignment;
        public int ArmorClass;
        public int Attacks;
        public int Constitution;
        public int DBMax;
        public int DBMin;
        public ushort Dexterity;
        public byte Hear;
        public int HPCur;
        public int HPMax_Renamed;
        public int LastDir;
        public int Level;
        public int Luck;
        public int MPCur;
        public int MPMax;
        public byte Perception;
        public CreatureSex Sex;
        public byte Smell;
        public int Stamina;
        public int Strength;
        public int ToHit;
        public int Turn;

        public NWCreature LastAttacker;
        public int LayerID;

        public bool Ghost;
        public int GhostIdx;
        public bool Illusion;
        public bool Prowling;
        public byte[] ProwlImage;


        public NWField CurrentField
        {
            get {
                return (fSpace == null) ? null : Space.GetField(LayerID, fField.X, fField.Y);
            }
        }

        public new NWGameSpace Space
        {
            get {
                return (NWGameSpace)fSpace;
            }
        }


        static NWCreature()
        {
            NotImprisonable = new string[] {
                "Aspenth",
                "Balder",
                "Bartan",
                "Eitri",
                "Elcich",
                "Fenrir",
                "Freyr",
                "Garm",
                "GiantSquid",
                "Gymir",
                "Harbard",
                "Hatchetfish",
                "Heimdall",
                "Hreset",
                "IvyCreeper",
                "Jormungand",
                "LiKrin",
                "Loki",
                "Lorkesth",
                "Odin",
                "PaleMoss",
                "Qivuit",
                "Rashok",
                "Retchweed",
                "Scyld",
                "Surtr",
                "Thokk",
                "Thor",
                "Trader",
                "Tyr",
                "Uorik"
            };

            // (am_Mask_Good..am_Mask_Evil)*(am_Mask_Good..am_Mask_Evil)
            Reaction[, ] array = new Reaction[3, 3];
            array[0, 0] = Reaction.Ally;
            array[0, 1] = Reaction.Neutral;
            array[0, 2] = Reaction.Hostile;
            array[1, 0] = Reaction.Neutral;
            array[1, 1] = Reaction.Ally;
            array[1, 2] = Reaction.Neutral;
            array[2, 0] = Reaction.Hostile;
            array[2, 1] = Reaction.Neutral;
            array[2, 2] = Reaction.Ally;
            Relations = array;

            ShootIsoTrans = new int[] {
                Directions.DtNone,
                Directions.DtNorthEast,
                Directions.DtSouthWest,
                Directions.DtNorthWest,
                Directions.DtSouthEast,
                Directions.DtNorth,
                Directions.DtEast,
                Directions.DtWest,
                Directions.DtSouth,
                Directions.DtZenith,
                Directions.DtNadir,
                Directions.DtPlace
            };
        }

        public NWCreature(NWGameSpace space, object owner)
            : base(space, owner)
        {
            fField = new ExtPoint();

            fAbilities = new AttributeList();
            fSkills = new AttributeList();
            fEffects = new EffectsList(this, true);
            fItems = new ItemsList(this, true);

            fHouse = null;
            Turn = 0;
            Stamina = 0;
        }

        public NWCreature(NWGameSpace space, object owner, int creatureID, bool total, bool setName)
            : this(space, owner)
        {
            InitEx(creatureID, total, setName);
        }

        public override int CLSID
        {
            set {
                base.CLSID = value;
    
                fEntry = ((CreatureEntry)GlobalVars.nwrDB.GetEntry(value));
            }
        }

        public void InitEx(int creatureID, bool total, bool setName)
        {
            CLSID = creatureID;
            Init(-1, total, setName);
        }

        public virtual void Init(int creatureID, bool total, bool setName)
        {
            try {
                CreatureEntry entry;
                if (creatureID == -1) {
                    entry = fEntry;
                } else {
                    entry = ((CreatureEntry)GlobalVars.nwrDB.GetEntry(creatureID));
                }

                Alignment = entry.Alignment;
                Sex = entry.Sex;
                Level = (int)entry.Level;
                fExperience = 0;
                Strength = (int)entry.Strength;
                fSpeed = (int)entry.Speed;
                Attacks = (int)entry.Attacks;
                ToHit = (int)entry.ToHit;
                Luck = 7;
                Constitution = (int)entry.Constitution;
                Dexterity = entry.Dexterity;
                fSurvey = entry.Survey;
                Hear = entry.Hear;
                Smell = entry.Smell;
                ArmorClass = (int)entry.AC;
                HPMax_Renamed = RandomHelper.GetBoundedRnd((int)entry.MinHP, (int)entry.MaxHP);
                HPCur = HPMax_Renamed;
                DBMin = (int)entry.MinDB;
                DBMax = (int)entry.MaxDB;
                Perception = entry.Perception;

                byte op;
                if (creatureID == GlobalVars.cid_Merchant) {
                    op = AttributeList.Lao_Or;
                } else {
                    if (total) {
                        op = AttributeList.Lao_Copy;
                    } else {
                        op = AttributeList.Lao_Or;
                    }
                }
                fAbilities.Assign(entry.Abilities, op);
                fSkills.Assign(entry.Skills, op);

                if (setName && entry.Race == RaceID.crHuman) {
                    fName = Space.GenerateName(this, NamesLib.NameGen_NorseDic);
                }

                if (entry.Flags.Contains(CreatureFlags.esUndead)) {
                    State = CreatureState.Undead;
                } else {
                    State = CreatureState.Alive;
                }

                InitBody();
                InitBrain();

                fEffects.Clear();
                fItems.Clear();

                if (total) {
                    foreach (CreatureEntry.InventoryEntry inventory in entry.Inventory) {
                        ItemEntry itemEntry = (ItemEntry)GlobalVars.nwrDB.FindEntryBySign(inventory.ItemSign);

                        if (itemEntry != null) {
                            int cnt = RandomHelper.GetBoundedRnd(inventory.CountMin, inventory.CountMax);

                            if (itemEntry.Countable) {
                                Item item = Item.CreateItem(this, itemEntry.GUID, true);
                                item.Count = (ushort)cnt;
                                item.Bonus = inventory.Bonus;
                                item.State = inventory.State;
                            } else {
                                for (int i = 1; i <= cnt; i++) {
                                    Item item = Item.CreateItem(this, itemEntry.GUID, true);
                                    item.Bonus = inventory.Bonus;
                                    item.State = inventory.State;
                                }
                            }
                        }
                    }

                    PrepareItems();
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.init(): " + ex.Message);
                throw ex;
            }
        }

        public void Assign(NWCreature source, bool illusion)
        {
            InitEx(source.CLSID, true, true);

            Level = source.Level;
            fExperience = source.fExperience;
            Strength = source.Strength;
            fSpeed = source.fSpeed;
            Attacks = source.Attacks;
            ToHit = source.ToHit;
            Luck = source.Luck;
            Constitution = source.Constitution;
            ArmorClass = source.ArmorClass;
            HPMax_Renamed = source.HPMax_Renamed;
            HPCur = source.HPCur;
            fIsTrader = false;
            fName = source.fName;
            Alignment = source.Alignment;

            if (illusion) {
                fDamageBase = 0;
                MPMax = 0;
                MPCur = 0;
                DBMin = 0;
                DBMax = 0;

                fAbilities.Clear();
                fSkills.Clear();
                fItems.Clear();
            } else {
                fDamageBase = source.fDamageBase;
                MPMax = source.MPMax;
                MPCur = source.MPCur;
                DBMin = source.DBMin;
                DBMax = source.DBMax;
            }
        }

        public NWCreature Clone(bool illusion)
        {
            NWCreature result = null;

            ExtPoint pt = GetNearestPlace(3, true);
            if (!pt.IsEmpty) {
                result = new NWCreature(Space, null);
                result.Assign(this, illusion);

                if (IsPlayer) {
                    result.Brain = new SentientBrain(result);
                }

                result.TransferTo(LayerID, fField.X, fField.Y, pt.X, pt.Y, StaticData.MapArea, true, false);
            }

            return result;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fItems.Dispose();
                fEffects.Dispose();
                fSkills.Dispose();
                fAbilities.Dispose();

                if (fBody != null) {
                    fBody.Dispose();
                }
            }
            base.Dispose(disposing);
        }

        public int DamageBase
        {
            set {
                fDamageBase = value;
            }
            get {
                return RandomHelper.GetBoundedRnd(DBMin, DBMax);
            }
        }

        public EffectsList Effects
        {
            get {
                return fEffects;
            }
        }

        public CreatureEntry Entry
        {
            get {
                return fEntry;
            }
        }

        public ExtPoint Field
        {
            get {
                return fField;
            }
        }

        public bool InFog
        {
            get {
                return fInFog;
            }
            set {
                try {
                    if (fInFog != value) {
                        fInFog = value;
                        if (fSpace != null && IsPlayer) {
                            if (value) {
                                Space.ShowText(this, BaseLocale.GetStr(RS.rs_ThisFogIsVeryThick));
                            } else {
                                Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCanSeeAgain));
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.Write("NWCreature.setInFog(): " + ex.Message);
                    throw ex;
                }
            }
        }

        public bool IsTrader
        {
            get {
                return fIsTrader;
            }
            set {
                if (fIsTrader != value) {
                    fIsTrader = value;
                    if (!fIsTrader) {
                        Init(CLSID, true, false);
                    } else {
                        Init(GlobalVars.cid_Merchant, true, false);
                    }
                }
            }
        }

        public ItemsList Items
        {
            get {
                return fItems;
            }
        }

        public int Speed
        {
            get {
                int result = fSpeed + SpeedMod;
    
                int num = fEffects.Count;
                for (int i = 0; i < num; i++) {
                    Effect eff = fEffects[i];
                    EffectID effectID = (EffectID)eff.CLSID;
    
                    switch (effectID) {
                        case EffectID.eid_Speedup:
                            result += eff.Magnitude;
                            break;
                        case EffectID.eid_SpeedDown:
                            result -= eff.Magnitude;
                            break;
                        case EffectID.eid_Sail:
                            result += 40;
                            break;
                        case EffectID.eid_Lycanthropy:
                            result += 20;
                            break;
                    }
                }
    
                /*if (GlobalVars.Debug_DevMode && this.isMercenary()) {
                    result += 30;
                }*/
    
                if (result > 100) {
                    Logger.Write("error!!!");
                }
    
                return result;
            }
        }

        public StringList Props
        {
            get {
                StringList props = new StringList();
    
                int mpr;
                if (MPMax == 0) {
                    mpr = 0;
                } else {
                    mpr = (int)(Math.Round((MPCur / MPMax) * 100.0f));
                }
    
                props.Add(Name);
                props.Add(Race + ", " + BaseLocale.GetStr(StaticData.dbSex[(int)Sex].NameRS));
                props.Add("  ");
                props.Add(BaseLocale.GetStr(RS.rs_Level) + ": " + Convert.ToString(Level));
                props.Add(BaseLocale.GetStr(RS.rs_Experience) + ": " + Convert.ToString(Experience));
                props.Add(BaseLocale.GetStr(RS.rs_Perception) + ": " + Convert.ToString((int)Perception));
                props.Add(BaseLocale.GetStr(RS.rs_Constitution) + ": " + Convert.ToString(Constitution));
                props.Add(BaseLocale.GetStr(RS.rs_Strength) + ": " + Convert.ToString(Strength));
                props.Add(BaseLocale.GetStr(RS.rs_Speed) + ": " + Convert.ToString(Speed));
                props.Add(BaseLocale.GetStr(RS.rs_Luck) + ": " + Convert.ToString(Luck));
                props.Add(BaseLocale.GetStr(RS.rs_Armor) + ": " + Convert.ToString(ArmorClass));
                props.Add(BaseLocale.GetStr(RS.rs_HP) + ": " + Convert.ToString((int)(Math.Round(((float)HPCur / (float)HPMax_Renamed) * 100.0f))) + " %");
                props.Add(BaseLocale.GetStr(RS.rs_MP) + ": " + Convert.ToString(mpr) + " %");
                props.Add(BaseLocale.GetStr(RS.rs_Damage) + ": " + Convert.ToString(DBMin) + "-" + Convert.ToString(DBMax));
                props.Add(BaseLocale.GetStr(RS.rs_Dexterity) + ": " + Convert.ToString((int)Dexterity));
    
                return props;
            }
        }

        public int GetAbility(AbilityID ID)
        {
            return fAbilities.GetValue((int)ID);
        }

        public void SetAbility(AbilityID ID, int Value)
        {
            fAbilities.SetValue((int)ID, Value);
        }

        public void ClearAbilities()
        {
            fAbilities.Clear();
        }

        public int GetSkill(SkillID ID)
        {
            return fSkills.GetValue((int)ID);
        }

        public void SetSkill(SkillID ID, int Value)
        {
            fSkills.SetValue((int)ID, Value);
        }

        public int SkillsCount
        {
            get {
                return fSkills.Count;
            }
        }

        public void ClearSkills()
        {
            fSkills.Clear();
        }

        private static bool CanBeDefeated(NWCreature enemy, Item weapon, Item projectile)
        {
            bool omw = (enemy.fEntry.Flags.Contains(CreatureFlags.esOnlyMagicWeapon));
            return !omw || (omw && ((weapon != null && (weapon.Entry.Flags.Contains(ItemFlags.if_MagicWeapon)) || (projectile != null && (projectile.Entry.Flags.Contains(ItemFlags.if_MagicWeapon))))));
        }

        private static int GetTrapListIndex(int trapID)
        {
            for (int i = 0; i < StaticData.dbTraps.Length; i++) {
                if (StaticData.dbTraps[i].TileID == trapID) {
                    return i;
                }
            }
            return -1;
        }


        public float Leadership
        {
            get {
                return (((float)(Strength + ArmorClass + Attacks + fSpeed) / 4.0f));
            }
            set {
            }
        }

        public float MaxItemsWeight
        {
            get {
                float result = ((360.0f * (Strength / 18.0f))); // in original = 360
                return result;
            }
        }

        // <editor-fold defaultstate="collapsed" desc="IEquippedCreature implementation">

        //@Override
        public AbstractBody Body
        {
            get {
                return fBody;
            }
            set {
                if (fBody != null) {
                    fBody.Dispose();
                }
                fBody = value;
            }
        }

        public int Money
        {
            get {
                int result = 0;
    
                int num = fItems.Count;
                for (int i = 0; i < num; i++) {
                    Item item = fItems[i];
                    if (item.CLSID == GlobalVars.iid_Coin) {
                        result += (int)item.Count;
                    }
                }
    
                return result;
            }
        }

        /// 
        /// <summary>
        /// <b>Need for scripting</b>.
        /// @return
        /// </summary>
        public bool FieldCleared
        {
            get {
                NWField field = CurrentField;
                for (int i = 0; i < field.Creatures.Count; i++) {
                    NWCreature creat = field.Creatures[i];
                    if (!Equals(creat) && IsEnemy(creat)) {
                        return false;
                    }
                }
    
                return true;
            }
        }

        public bool Stoning
        {
            get {
                return (fEffects.FindEffectByID(EffectID.eid_Stoning) != null);
            }
        }

        public virtual bool IsPlayer
        {
            get {
                return false;
            }
        }

        public bool IsMercenary
        {
            set {
                if (fIsMercenary != value) {
                    fIsMercenary = value;
                    ResetMercenary(value);
                }
            }
        }

        public bool SeekInvisible
        {
            get {
                return GetAbility(AbilityID.Ab_SixthSense) > 0 || GetAbility(AbilityID.Ab_Telepathy) > 0 || fEffects.FindEffectByID(EffectID.eid_ThirdSight) != null;
            }
        }

        public CreatureState State
        {
            get {
                return fState;
            }
            set {
                fState = value;
    
                switch (fState) {
                    case CreatureState.Alive:
                    case CreatureState.Undead:
                        CheckTile(true);
                        break;
    
                    case CreatureState.Dead:
                        CheckTile(false);
                        break;
                }
            }
        }

        public virtual byte Survey
        {
            get {
                return fSurvey;
            }
            set {
                fSurvey = value;
            }
        }

        public int Experience
        {
            get {
                return fExperience;
            }
            set {
                // TODO: отработать потерю уровня от проклятого зелья опыта
    
                try {
                    fExperience = value;
                    int exp = GetNextLevelExp();
                    if (fExperience >= exp) {
                        Level++;
                        HPMax_Renamed += RandomHelper.GetBoundedRnd(7, 11);
                        HPCur = HPMax_Renamed;
    
                        if (IsPlayer) {
                            Space.DoEvent(EventID.event_LevelUp, this, null, null);
                        }
                    }
                } catch (Exception ex) {
                    Logger.Write("NWCreature.setExperience(): " + ex.Message);
                }
            }
        }

        public Item PrimaryWeapon
        {
            get {
                return GetItemByEquipmentKind(BodypartType.bp_RHand);
            }
        }

        public bool InWater
        {
            get {
                BaseTile tile = CurrentField.GetTile(PosX, PosY);
                bool isUnderwater = (tile.Background == PlaceID.pid_Water && LayerID != GlobalVars.Layer_MimerWell);
    
                if (isUnderwater) {
                    Effect efSail = fEffects.FindEffectByID(EffectID.eid_Sail);
                    return (isUnderwater && efSail == null);
                } else {
                    return false;
                }
            }
        }

        public bool Confused
        {
            get {
                return fEffects.FindEffectByID(EffectID.eid_Confusion) != null;
            }
        }

        public bool Hallucinations
        {
            get {
                return fEffects.FindEffectByID(EffectID.eid_Hallucination) != null || fEffects.FindEffectByID(EffectID.eid_Insanity) != null;
            }
        }

        public bool Imprisonable
        {
            get {
                bool Result = true;
                int i = 0;
                while (fEntry.Sign.CompareTo(NotImprisonable[i]) != 0) {
                    i++;
                    if (i == 31) {
                        return Result;
                    }
                }
                Result = false;
                return Result;
            }
        }

        public bool Blindness
        {
            get {
                return (fEffects.FindEffectByID(EffectID.eid_Blindness) != null);
            }
        }

        public short HirePrice
        {
            get {
                float temp;
    
                if (CLSID == GlobalVars.cid_Guardsman) {
                    Player player = Space.Player;
    
                    temp = (HPCur - (float)(fEntry.MaxHP - fEntry.MinHP) / 2);
                    temp += (ArmorClass - fEntry.AC);
                    temp += (ToHit - fEntry.ToHit);
                    temp += (Attacks - fEntry.Attacks);
                    temp += (fSpeed - fEntry.Speed);
                    temp += (Constitution - fEntry.Constitution);
                    temp += (Strength - fEntry.Strength);
                    temp += (Dexterity - fEntry.Dexterity);
                    temp += (((float)(DBMax - DBMin) / 2) - ((float)(fEntry.MaxDB - fEntry.MinDB) / 2));
                    temp += (Survey - fEntry.Survey);
    
                    for (var i = AbilityID.Ab_First; i <= AbilityID.Ab_Last; i++) {
                        temp = temp + (fAbilities.GetValue((int)i) - fEntry.Abilities.GetValue((int)i));
                    }
    
                    for (var i = SkillID.Sk_First; i <= SkillID.Sk_Last; i++) {
                        temp = temp + (fSkills.GetValue((int)i) - fEntry.Skills.GetValue((int)i));
                    }
    
                    for (int i = 0; i < fItems.Count; i++) {
                        Item item = fItems[i];
                        temp = temp + item.Price;
                    }
    
                    float lev = (float)Level;
                    float f = (lev / (lev + player.Level));
    
                    temp = temp * (1.0f + f);
                } else {
                    temp = 10000;
                }
    
                return (short)Math.Round(temp);
            }
        }

        public sbyte LightRadius
        {
            get {
                sbyte result = 0;
    
                Item torch = GetItemByEquipmentKind(BodypartType.bp_LHand);
                if (torch != null && torch.CLSID == GlobalVars.iid_Torch) {
                    result = (sbyte)((int)result + torch.Bonus);
                }
                Effect eLight = fEffects.FindEffectByID(EffectID.eid_Light);
                if (eLight != null) {
                    result = (sbyte)((int)result + eLight.Magnitude);
                }
    
                return result;
            }
        }


        public void Buy(Item item, NWCreature seller, bool inShop)
        {
            Space.DoEvent(EventID.event_Trade, null, null, null);

            int price = (int)item.GetTradePrice(this, seller);

            if (IsPlayer && seller.fIsTrader) {
                ((Player)this).AddDebt(seller.Name, price);
            } else {
                if (fIsTrader && seller.IsPlayer) {
                    int debt = ((Player)seller).GetDebt(base.Name);
                    if (debt == 0) {
                        SubMoney(price);
                        seller.AddMoney(price);
                    } else {
                        if (debt >= price) {
                            ((Player)seller).SubDebt(base.Name, price);
                        } else {
                            int rem = price - debt;
                            ((Player)seller).SubDebt(base.Name, debt);
                            SubMoney(rem);
                            seller.AddMoney(rem);
                        }
                    }
                } else {
                    SubMoney(price);
                    seller.AddMoney(price);
                }
            }

            if (inShop) {
                if (IsPlayer) {
                    PickupItem(item);
                } else {
                    seller.DropItem(item);
                }
            } else {
                seller.Items.Extract(item);
                AddItem(item);
                if (fIsTrader) {
                    ((TraderBrain)fBrain).WareReturnGoal = item;
                }
            }
        }

        public bool CanBeUsed(Item aItem)
        {
            bool Result = false;
            if (fBody != null) {
                BodypartType kind = aItem.EquipmentKind;
                if (kind != BodypartType.bp_None) {
                    Result = (fBody.GetUnoccupiedPart((int)kind) != null);
                }
            }
            return Result;
        }

        public bool CanBuy(Item aItem, NWCreature aSeller)
        {
            bool result = false;
            if (!IsEnemy(aSeller)) {
                if (Money >= (int)aItem.GetTradePrice(this, aSeller)) {
                    result = true;
                } else {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_NoMoney));
                }
            }
            return result;
        }

        public bool CanTake(Item aItem, bool isBuy)
        {
            bool result;
            if (aItem.CLSID == GlobalVars.iid_Coin) {
                result = true;
            } else {
                float mWeight;
                if (!isBuy) {
                    mWeight = 0f;
                } else {
                    ItemEntry coinEntry = (ItemEntry)GlobalVars.nwrDB.FindEntryBySign("Coin");
                    mWeight = ((aItem.Price * (float)coinEntry.Weight));
                }
                result = ((TotalWeight + aItem.Weight - mWeight) <= MaxItemsWeight);
                if (!result) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCannotCarryThatMuchWeight));
                }
            }
            return result;
        }

        public void DropAll()
        {
            while (fItems.Count > 0) {
                Item it = fItems[0];
                it.InUse = false;
                DropItem(it);
            }
        }

        public float TotalWeight
        {
            get {
                float result = 0f;
    
                int num = fItems.Count;
                for (int i = 0; i < num; i++) {
                    result = ((result + fItems[i].Weight));
                }
    
                return result;
            }
        }

        public Item GetItemByEquipmentKind(BodypartType kind)
        {
            int num = fItems.Count;
            for (int idx = 0; idx < num; idx++) {
                Item item = fItems[idx];
                if (item.InUse && item.EquipmentKind == kind) {
                    return item;
                }
            }

            return null;
        }

        public void AddItem(Item item)
        {
            item.Owner = this;
            fItems.Add(item, true);
            //this.FItemsEnlarged = true;

            if (GetAbility(AbilityID.Ab_Identification) > 0) {
                item.Identified = true;
            }
        }

        public void DeleteItem(Item item)
        {
            item.Owner = null;
            fItems.Remove(item);
        }

        public void DropItem(Item item)
        {
            fItems.Extract(item);

            Space.DropItem(CurrentField, this, item, PosX, PosY);
        }

        public void PickupItem(Item item)
        {
            CreatureFlags cs = new CreatureFlags();
            if (fEntry.Race == RaceID.crHuman) {
                cs.Include(CreatureFlags.esUseItems);
            } else {
                cs = fEntry.Flags;
            }

            if (!cs.Contains(CreatureFlags.esUseItems)) {
                Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCannotCarryAnythingElse));
            } else {
                if (InWater) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_ItIsTooDeepHere));
                } else {
                    Item fact_item;
                    if (item.CLSID == GlobalVars.iid_Coin) {
                        float possible = ((MaxItemsWeight - TotalWeight));
                        if (possible >= item.Weight) {
                            fact_item = item;
                        } else {
                            int part_count = (int)Math.Round((double)(possible / item.Entry.Weight));
                            fact_item = item.GetSeveral(part_count);
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCannotCarryRest));
                        }
                    } else {
                        fact_item = item;
                    }

                    PickupInternal(fact_item);
                }
            }
        }

        private void PickupInternal(Item item)
        {
            if (item != null) {
                Space.PickupItem(CurrentField, this, item);

                if (item.CLSID == GlobalVars.iid_DiamondNeedle) {
                    SetSkill(SkillID.Sk_PsiBlast, GetSkill(SkillID.Sk_PsiBlast) + (int)item.Count);
                    ApplyDamage(RandomHelper.GetBoundedRnd(21, 28) * (int)item.Count, DamageKind.Physical, null, "");
                    item.Dispose();
                    int num = RandomHelper.GetRandom(2);
                    if (num != 0) {
                        if (num == 1) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_BurrowsIntoBrain));
                        }
                    } else {
                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_BurrowsIntoSkull));
                    }
                } else {
                    AddItem(item);
                }
            }
        }

        public virtual void RemoveItem(Item item)
        {
            Space.DoEvent(EventID.event_ItemRemove, this, null, item);
            if (fBody != null) {
                BodypartType eq = item.EquipmentKind;
                if (eq != BodypartType.bp_None) {
                    Bodypart bodyEntry = fBody.GetOccupiedPart(item);
                    if (bodyEntry != null) {
                        bodyEntry.Item = null;
                    }
                    if (item.TwoHanded) {
                        bodyEntry = fBody.GetOccupiedPart(item);
                        if (bodyEntry != null) {
                            bodyEntry.Item = null;
                        }
                    }
                }
            }
            item.InUse = false;
        }

        public virtual void WearItem(Item item)
        {
            Space.DoEvent(EventID.event_ItemWear, this, null, item);
            if (fBody != null) {
                BodypartType eq = item.EquipmentKind;
                if (eq != BodypartType.bp_None) {
                    if (eq >= BodypartType.bp_Legs && eq < BodypartType.bp_Finger) {
                        if (fEffects.FindEffectByID(EffectID.eid_LegsMissing) != null) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCannotWearWithoutLegs));
                            return;
                        }
                    }

                    Bodypart bodyEntry = fBody.GetUnoccupiedPart((int)eq);
                    if (bodyEntry != null) {
                        bodyEntry.Item = item;
                    } else {
                        if (eq == BodypartType.bp_Finger && item.Kind == ItemKind.ik_Ring) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCannotWearMoreRings));
                        }
                    }

                    if (item.TwoHanded) {
                        bodyEntry = fBody.GetUnoccupiedPart((int)BodypartType.bp_LHand);
                        if (bodyEntry != null) {
                            bodyEntry.Item = item;
                        }
                    }
                }
            }

            if (item.CLSID == GlobalVars.iid_Amulet_Eternal_Life) {
                Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCanNoLongerMove) + " " + BaseLocale.GetStr(RS.rs_YouMayNeverDie));
                Death(BaseLocale.GetStr(RS.rs_BecameALivingStatue), null);
            } else {
                item.InUse = true;
            }
        }

        public void PrepareItems()
        {
            int num = fItems.Count;
            for (int i = 0; i < num; i++) {
                Item item = fItems[i];
                BodypartType ek = item.EquipmentKind;
                if (ek != BodypartType.bp_None && !item.InUse) {
                    Item dummyItem = GetItemByEquipmentKind(ek);
                    if (dummyItem == null) {
                        item.InUse = true;
                    } else {
                        if (dummyItem.Compare(item) == ItemsCompareResult.Better) {
                            dummyItem.InUse = false;
                            item.InUse = true;
                        }
                    }
                }
            }

            //this.FItemsEnlarged = false;
        }

        public void AddMoney(int aCount)
        {
            Item item = new Item(fSpace, this);
            item.CLSID = GlobalVars.iid_Coin;
            item.Count = (ushort)aCount;
            Items.Add(item, true);
        }

        public void SubMoney(int aCount)
        {
            int idx = -1;

            int num = Items.Count;
            for (int i = 0; i < num; i++) {
                if (Items[i].CLSID == GlobalVars.iid_Coin) {
                    idx = i;
                    break;
                }
            }

            if (idx > -1) {
                Item item = Items[idx];
                item.Count = (ushort)(item.Count - aCount);
                if (item.Count <= 0) {
                    DeleteItem(item);
                }
            }
        }

        public void PickupAll()
        {
            var items = CurrentField.Items.SearchListByPos(PosX, PosY);
            if (IsPlayer && items.Count < 1) {
                Space.ShowText(this, BaseLocale.GetStr(RS.rs_NothingHere));
            }

            int num = items.Count;
            for (int i = 0; i < num; i++) {
                Item it = (Item)items[i];
                if (it.CLSID != GlobalVars.iid_DeadBody && it.CLSID != GlobalVars.iid_Mummy) {
                    PickupItem(it);
                }
            }

            items.Dispose();
        }

        // </editor-fold>

        public string Race
        {
            get {
                string result;
    
                if (fEntry.Race == RaceID.crDefault) {
                    result = fEntry.Name;
                } else {
                    var rcRec = StaticData.dbRaces[(int)fEntry.Race];
                    result = BaseLocale.GetStr(rcRec.Name);
                }
    
                return result;
            }
        }

        public float Weight
        {
            get {
                return fEntry.Weight;
            }
        }

        public int HPMax
        {
            set {
                HPMax_Renamed = value;
                if (HPCur > HPMax_Renamed) {
                    HPCur = HPMax_Renamed;
                }
            }
        }




        private AbilityID ArmorAbility
        {
            get {
                AbilityID result = AbilityID.Ab_None;
                Item item = GetItemByEquipmentKind(BodypartType.bp_Torso);
                if (item != null) {
                    var ikRec = StaticData.dbItemKinds[(int)item.Kind];
                    result = ikRec.Ability;
                }
                return result;
            }
        }

        private NWCreature FindEnemyByAttack()
        {
            int nearestDist = StaticData.FieldWidth;
            NWCreature nearestAttacker = null;

            GameEvent evt = Space.PeekEvent(this, EventID.event_Attack, true);
            while (evt != null) {
                NWCreature enemy = (NWCreature)evt.Sender;
                int dist = MathHelper.Distance(Location, enemy.Location);

                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestAttacker = enemy;
                }

                evt.Dispose();
                evt = Space.PeekEvent(this, EventID.event_Attack, true);
            }

            return nearestAttacker;
        }

        private NWCreature DefaultEnemy
        {
            get {
                NWCreature result = null;
    
                if (CLSID == GlobalVars.cid_Heimdall) {
                    result = Space.FindCreature(GlobalVars.cid_Loki);
                } else if (CLSID == GlobalVars.cid_Thor) {
                    result = Space.FindCreature(GlobalVars.cid_Jormungand);
                } else if (CLSID == GlobalVars.cid_Tyr) {
                    result = Space.FindCreature(GlobalVars.cid_Garm);
                } else if (CLSID == GlobalVars.cid_Freyr) {
                    result = Space.FindCreature(GlobalVars.cid_Surtr);
                } else if (CLSID == GlobalVars.cid_Odin) {
                    result = Space.FindCreature(GlobalVars.cid_Fenrir);
                } else if (CLSID == GlobalVars.cid_Jormungand) {
                    result = Space.FindCreature(GlobalVars.cid_Thor);
                } else if (CLSID == GlobalVars.cid_Garm) {
                    result = Space.FindCreature(GlobalVars.cid_Tyr);
                } else if (CLSID == GlobalVars.cid_Loki) {
                    result = Space.FindCreature(GlobalVars.cid_Heimdall);
                } else if (CLSID == GlobalVars.cid_Surtr) {
                    result = Space.FindCreature(GlobalVars.cid_Freyr);
                } else if (CLSID == GlobalVars.cid_Fenrir) {
                    result = Space.FindCreature(GlobalVars.cid_Odin);
                }
    
                if (result != null && !CurrentField.Equals(result.CurrentField)) {
                    return null;
                }
    
                return result;
            }
        }

        public virtual bool Mercenary
        {
            get {
                return fIsMercenary;
            }
        }

        public virtual Movements Movements
        {
            get {
                Movements result = new Movements();
    
                if (fEntry.Flags.Contains(CreatureFlags.esWalking)) {
                    result.Include(Movements.mkWalk);
                }
                if (fEntry.Flags.Contains(CreatureFlags.esSwimming)) {
                    result.Include(Movements.mkSwim);
                }
                if (fEntry.Flags.Contains(CreatureFlags.esFlying)) {
                    result.Include(Movements.mkFly);
                }
    
                if (fEffects.FindEffectByID(EffectID.eid_LegsMissing) != null) {
                    result.Exclude(Movements.mkWalk);
                }
    
                if (GetAbility(AbilityID.Ab_Swimming) > 0 || fEffects.FindEffectByID(EffectID.eid_Sail) != null) {
                    result.Include(Movements.mkSwim);
                }
    
                if (GetAbility(AbilityID.Ab_Levitation) > 0) {
                    result.Include(Movements.mkFly);
                }
    
                if (fEffects.FindEffectByID(EffectID.eid_Phase) != null) {
                    result.Include(Movements.mkEthereality);
                }
    
                return result;
            }
        }

        public override string Name
        {
            get {
                string result;
    
                if (Ghost) {
                    result = BaseLocale.GetStr(RS.rs_Ghost) + " " + fName;
                } else if (Illusion) {
                    result = BaseLocale.GetStr(RS.rs_Illusion) + " " + fName;
                } else {
                    RaceID race = fEntry.Race;
                    if (race == RaceID.crHuman) {
                        if (IsPlayer) {
                            result = fName;
                        } else {
                            result = fEntry.Name + " " + fName;
                        }
                    } else {
                        result = fEntry.Name;
                    }
                }
    
                return result;
            }
            set {
                RaceID race = fEntry.Race;
    
                if (race == RaceID.crHuman) {
                    if ((fName != value)) {
                        fName = value;
                    }
                }
            }
        }

        public override byte SerializeKind
        {
            get {
                return StaticData.SID_CREATURE;
            }
        }

        public virtual SymbolID Symbol
        {
            get {
                return SymbolID.sid_None;
            }
        }

        private void PostDeath_SetTile(ushort tileID)
        {
            NWField map = CurrentField;
            map.GetTile(PosX, PosY).Background = tileID;
            map.Normalize();
        }

        private void CheckHealth_Solve(EffectID effectID, string problem, ref bool  refTurnUsed)
        {
            Item item = CanCure(effectID);
            if (item != null) {
                refTurnUsed = true;
                UseItem(item, null);
            } else {
                if (Mercenary) {
                    string msg = BaseLocale.Format(RS.rs_MercenaryProblem, new object[]{ problem });
                    Space.ShowText(this, msg);
                }
            }
        }

        private void Gout()
        {
            AuxUtils.ExStub("The ");
            AuxUtils.ExStub(" spews liquid.");
            AuxUtils.ExStub("The liquid burns you.");
            AuxUtils.ExStub("Scalded by unholy water");
            AuxUtils.ExStub("You dodge the stream.");
        }

        private void TakeItemDef(int iid, string message)
        {
            NWCreature cr = Space.Player;

            Item it = (Item)cr.Items.FindByCLSID(iid);
            if (MathHelper.Distance(Location, cr.Location) <= 3 && it != null) {
                if (CLSID == GlobalVars.cid_Hela && it.Bonus != GlobalVars.cid_Thokk) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_HelaIsNotImpressed), new LogFeatures(LogFeatures.lfDialog));
                } else {
                    if (message.CompareTo("") == 0) {
                        message = BaseLocale.Format(RS.rs_ItemTaked, new object[]{ base.Name, it.Name });
                    }
                    Space.ShowText(this, message, new LogFeatures(LogFeatures.lfDialog));

                    it.InUse = false;
                    cr.Items.Extract(it);
                    fItems.Add(it, false);

                    if (CLSID == GlobalVars.cid_Hela) {
                        Space.AddCreatureEx(GlobalVars.Layer_Asgard, 1, 0, RandomHelper.GetBoundedRnd(29, 68), RandomHelper.GetBoundedRnd(3, StaticData.FieldHeight - 4), GlobalVars.cid_Balder);
                    } else {
                        if (it.CLSID == GlobalVars.iid_Gjall) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_HeimdallUseGjall));
                            UseItem(it, null);
                        } else {
                            it.InUse = true;
                        }
                    }
                }
            }
        }

        protected void DoDefaultAction()
        {
            try {
                if (CLSID == GlobalVars.cid_Hela) {
                    TakeItemDef(GlobalVars.iid_SoulTrapping_Ring, BaseLocale.GetStr(RS.rs_ThokkTaked));
                } else {
                    if (CLSID == GlobalVars.cid_Heimdall) {
                        TakeItemDef(GlobalVars.iid_Gjall, "");
                    } else {
                        if (CLSID == GlobalVars.cid_Thor) {
                            TakeItemDef(GlobalVars.iid_Mjollnir, "");
                        } else {
                            if (CLSID == GlobalVars.cid_Tyr) {
                                TakeItemDef(GlobalVars.iid_DwarvenArm, "");
                            } else {
                                if (CLSID == GlobalVars.cid_Freyr) {
                                    TakeItemDef(GlobalVars.iid_Mimming, "");
                                } else {
                                    if (CLSID == GlobalVars.cid_Odin) {
                                        TakeItemDef(GlobalVars.iid_Gungnir, "");
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.doDefaultAction(): " + ex.Message);
            }
        }

        protected virtual void InitBody()
        {
            try {
                RaceID race = fEntry.Race;

                if (race == RaceID.crHuman) {
                    Body = new HumanBody(this);
                } else {
                    Body = new CustomBody(this);
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.initBody(): " + ex.Message);
                throw ex;
            }
        }

        protected virtual void InitBrain()
        {
            try {
                if (Prowling) {
                    fBrain = new BeastBrain(this);
                    return;
                }

                if (CLSID == GlobalVars.cid_Agnar || CLSID == GlobalVars.cid_Haddingr || CLSID == GlobalVars.cid_Ketill) {
                    Brain = new VictimBrain(this);
                } else if (CLSID == GlobalVars.cid_Eitri) {
                    Brain = new EitriBrain(this);
                } else if (CLSID == GlobalVars.cid_Raven) {
                    Brain = new RavenBrain(this);
                } else if (CLSID == GlobalVars.cid_Guardsman || CLSID == GlobalVars.cid_Jarl) {
                    Brain = new WarriorBrain(this);
                } else if (fEntry.Flags.Contains(CreatureFlags.esMind)) {
                    if (fIsTrader) {
                        Brain = new TraderBrain(this);
                    } else {
                        Brain = new SentientBrain(this);
                    }
                } else {
                    Brain = new BeastBrain(this);
                }

                if (fBrain is BeastBrain && (fEntry.Flags.Contains(CreatureFlags.esCohorts))) {
                    ((BeastBrain)fBrain).Flock = true;
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.initBrain(): " + ex.Message);
                throw ex;
            }
        }

        protected void Recovery()
        {
            if (HPCur < HPMax_Renamed && fEffects.FindEffectByID(EffectID.eid_Withered) == null) {
                float str = ((float)Strength);
                float life_recovery = 0.25f;
                int mag = 0;

                int num = fEffects.Count;
                for (int i = 0; i < num; i++) {
                    if (fEffects[i].CLSID == (int)EffectID.eid_Regeneration) {
                        mag += 60;
                    }
                }

                float res = (((str - 8.0f) * life_recovery * (1.0f + (float)mag / 100.0f)));
                if ((HPCur + (double)res) > HPMax_Renamed) {
                    HPCur = HPMax_Renamed;
                } else {
                    HPCur = (int)(HPCur + ((long)Math.Round((double)res)));
                }
            }

            if (MPCur < MPMax && GlobalVars.Debug_ManaRegen) {
                //float temp = ((float) this.Perception);
            }
        }

        public void SetGlobalPos(int layerID, int fieldX, int fieldY, bool obligatory)
        {
            try {
                if (LayerID != layerID || fField.X != fieldX || fField.Y != fieldY || obligatory) {
                    LayerEntry eLayer = (LayerEntry)GlobalVars.nwrDB.GetEntry(layerID);

                    if (fieldX >= eLayer.W) {
                        fieldX = 0;
                    }
                    if (fieldY >= eLayer.H) {
                        fieldY = 0;
                    }

                    LeaveField();
                    LayerID = layerID;
                    fField = new ExtPoint(fieldX, fieldY);
                    NWField fld = Space.GetField(LayerID, fField.X, fField.Y);
                    EnterField(fld);

                    if (IsPlayer) {
                        fld.Visited = true;
                        int nextLand = fld.LandID;
                        KnowIt(nextLand);
                        Space.DoEvent(EventID.event_LandEnter, this, null, GlobalVars.nwrDB.GetEntry(nextLand));
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.setGlobalPos(): " + ex.Message);
                throw ex;
            }
        }

        public void EnterField(NWField field)
        {
            try {
                field.Creatures.Add(this);

                if (IsPlayer) {
                    if (field.LandID == GlobalVars.Land_Bifrost) {
                        int eid = GlobalVars.nwrDB.FindEntryBySign("Edgewort").GUID;
                        field.AddCreature(-1, -1, eid);
                        field.AddCreature(-1, -1, eid);
                        field.AddCreature(-1, -1, eid);
                        field.AddCreature(-1, -1, eid);
                    }

                    if (field.LandID == GlobalVars.Land_Ocean && Space.rt_Jormungand != null) {
                        Space.rt_Jormungand.TransferTo(field.Layer.EntryID, field.Coords.X, field.Coords.Y, -1, -1, StaticData.MapArea, true, false);
                    }

                    if (field.LandID == GlobalVars.Land_Crossroads) {
                        // dummy
                    }

                    field.SetAmbient();
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.enterField(): " + ex.Message);
                throw ex;
            }
        }

        public void LeaveField()
        {
            NWField field = CurrentField;
            if (field != null) {
                field.Creatures.Extract(this);
            }
        }

        private void ResetMercenary(bool value)
        {
            Player player = Space.Player;
            LeaderBrain leader = ((LeaderBrain)player.fBrain);

            if (value) {
                bool res = leader.AddMember(this);
                if (res) {
                    fBrain = new WarriorBrain(this);
                    ((WarriorBrain)fBrain).SetEscortGoal(player, true);
                }
            } else {
                leader.RemoveMember(this);
            }
        }

        private int GetNextLevelExp()
        {
            int result;
            try {
                double lev = Level + 1;
                result = (int)Math.Round((Math.Pow(2.0, lev) + Math.Pow(lev, 4.0)) + (25 * lev));
            } catch (Exception ex) {
                Logger.Write("NWCreature.GetNextLevelExp(): " + ex.Message);
                result = 0;
            }
            return result;
        }

        public void ApplyDamage(int damage, DamageKind damageKind, object source, string deathMsg)
        {
            try {
                if (IsPlayer && GlobalVars.Debug_Divinity) {
                    return;
                }

                if (damageKind == DamageKind.Radiation && GetAbility(AbilityID.Ab_RayAbsorb) > 0) {
                    if (IsPlayer) {
                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouAbsorbEnergy));
                    }
                    HPCur += damage;
                } else {
                    int newHP = HPCur - damage;

                    if (newHP <= 0) {
                        Effect immort_ef = fEffects.FindEffectByID(EffectID.eid_Immortal);
                        if (immort_ef != null) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_SecondLife));
                            fEffects.Remove(immort_ef);
                        } else {
                            Death(deathMsg, source);
                            Space.DoEvent(EventID.event_Killed, this, null, source);
                        }
                    } else {
                        HPCur -= damage;
                        Space.DoEvent(EventID.event_Wounded, this, null, source);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.ApplyDamage(): " + ex.Message);
                throw ex;
            }
        }

        private void ApplySpecialItems()
        {
            try {
                GameEntity it = fItems.FindByCLSID(GlobalVars.iid_Lodestone);
                if (it != null) {
                    UseEffect(EffectID.eid_Lodestone, it, InvokeMode.im_Use, null);
                }

                it = fItems.FindByCLSID(GlobalVars.nwrDB.FindEntryBySign("BlueCube").GUID);
                if (it != null) {
                    UseEffect(EffectID.eid_Cube, it, InvokeMode.im_Use, null);
                }

                it = fItems.FindByCLSID(GlobalVars.nwrDB.FindEntryBySign("GreyCube").GUID);
                if (it != null) {
                    UseEffect(EffectID.eid_Cube, it, InvokeMode.im_Use, null);
                }

                it = fItems.FindByCLSID(GlobalVars.nwrDB.FindEntryBySign("OrangeCube").GUID);
                if (it != null) {
                    UseEffect(EffectID.eid_Cube, it, InvokeMode.im_Use, null);
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.ApplySpecialItems(): " + ex.Message);
            }
        }

        protected virtual AttackInfo CalcAttackInfo(AttackKind attackKind, NWCreature enemy, Item weapon, Item projectile)
        {
            AttackInfo result = new AttackInfo();

            try {
                bool phase = fEffects.FindEffectByID(EffectID.eid_Phase) != null;
                int pHit;
                int damage;

                if (projectile == null) {
                    pHit = ToHit;
                    damage = DamageBase;
                } else {
                    pHit = (int)(Math.Round((Strength / 7.0 - 2 * enemy.ArmorClass + Luck / 10.0 + 30.0 + (double)projectile.Bonus)));

                    if (projectile.Entry.Sign.CompareTo("FlintKnife") == 0) {
                        pHit += 5;
                    } else {
                        if (projectile.CLSID == GlobalVars.iid_Mjollnir) {
                            pHit += 80 + projectile.Bonus * 3;
                        } else {
                            if (projectile.CLSID == GlobalVars.iid_Gungnir) {
                                pHit += (int)(125 + (Math.Round(GetAbility(AbilityID.Ab_Spear) / 2.0)));
                            } else {
                                if (projectile.Entry.Sign.CompareTo("CrudeSpear") == 0) {
                                    pHit += (int)(8 + (Math.Round((double)projectile.Bonus / 3.0)));
                                } else {
                                    if (projectile.CLSID == GlobalVars.iid_Arrow && weapon.CLSID == GlobalVars.iid_LongBow) {
                                        pHit += 15 + weapon.Bonus;
                                    } else {
                                        if (projectile.CLSID == GlobalVars.iid_Bolt && weapon.CLSID == GlobalVars.iid_CrossBow) {
                                            pHit += 25 + weapon.Bonus;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    damage = (int)((projectile.Damage + projectile.Bonus) + (Math.Round((Strength / 14.0 + Luck / 40.0 + Level / 6.0))));
                }

                CreatureAction act = StaticData.ActionByAttackKind[(int)attackKind];
                var caRec = StaticData.dbCreatureActions[(int)act];
                AbilityID abil = GetActionAbility(act, weapon);

                int parry = (int)(Math.Round(enemy.GetAbility(AbilityID.Ab_Parry) / 10.0f));
                pHit = (int)(pHit + (Math.Round((GetAbility(abil) / 10.0f * caRec.Factor))) - parry);

                if (!CheckVisible(enemy)) {
                    pHit -= RandomHelper.GetBoundedRnd(5, 8);
                }

                if (Strength > 18) {
                    pHit += 4 * (Strength - 18);
                }
                if (phase) {
                    pHit -= 40;
                }

                pHit = Math.Abs(pHit);
                if (GlobalVars.Debug_Divinity && IsPlayer) {
                    pHit = 100;
                }
                if (enemy.CLSID == GlobalVars.cid_Scyld && fItems.FindByCLSID(GlobalVars.iid_GreenStone) == null) {
                    pHit = 0;
                }

                if (damage < 0) {
                    damage = 0;
                }
                if (enemy.fEffects.FindEffectByID(EffectID.eid_Fragile) != null) {
                    damage <<= 1;
                }
                if (enemy.fEffects.FindEffectByID(EffectID.eid_Invulnerable) != null) {
                    damage = 0;
                }

                result.Damage = damage;
                result.ToHit = pHit;
                result.Parry = parry;
                result.Action = act;
            } catch (Exception ex) {
                Logger.Write("NWCreature.CalcAttackInfo(): " + ex.Message);
            }

            return result;
        }

        public virtual bool AttackTo(AttackKind attackKind, NWCreature enemy, Item weapon, Item projectile)
        {
            enemy.LastAttacker = this;
            if (weapon == null) {
                weapon = PrimaryWeapon;
            }

            try {
                Space.DoEvent(EventID.event_Attack, this, enemy, this);

                if (!CanBeDefeated(enemy, weapon, projectile)) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_ThisCreatureCanOnlyBeHitWithMagicalWeapons));
                    return false;
                }

                AttackSpecialEffect(enemy);

                AttackInfo attackInfo = CalcAttackInfo(attackKind, enemy, weapon, projectile);

                if (!AuxUtils.Chance(attackInfo.ToHit)) {
                    if (enemy.CLSID == GlobalVars.cid_Scyld) {
                        Space.ShowText(this, BaseLocale.Format(RS.rs_YouTickle, new object[]{ enemy.GetDeclinableName(Number.nSingle, Case.cAccusative) }));
                    }

                    Space.DoEvent(EventID.event_Miss, this, null, null);

                    if (AuxUtils.Chance(attackInfo.ToHit - attackInfo.Parry)) {
                        enemy.CheckActionAbility(CreatureAction.caAttackParry, null);
                    }
                    return false;
                } else {
                    Space.DoEvent(EventID.event_Hit, this, enemy, null);
                    // FIXME: влияние навыка доспеха врага на урон
                    int hp = (int)(enemy.HPCur - (Math.Round((double)attackInfo.Damage)));

                    string msg = "";
                    if (hp <= 0) {
                        if (enemy.IsPlayer) {
                            msg = GetDeclinableName(Number.nSingle, Case.cInstrumental);
                            msg = BaseLocale.Format(RS.rs_Rsn_KilledByEnemy, new object[]{ msg });
                        }
                    } else {
                        msg = "";
                        AbilityID abil = enemy.ArmorAbility;
                        if (abil != AbilityID.Ab_None) {
                            enemy.SetAbility(abil, enemy.GetAbility(abil) + 1);
                        }
                    }

                    enemy.ApplyDamage(attackInfo.Damage, DamageKind.Physical, this, msg);

                    if (enemy.State == CreatureState.Dead) {
                        int xp = GetAttackExp(enemy);
                        Experience = Experience + xp;

                        KnowIt(enemy.CLSID);
                    }

                    CheckActionAbility(attackInfo.Action, weapon);

                    return true;
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.attackTo(): " + ex.Message);
                return false;
            }
        }

        public void AttackSpecialEffect(NWCreature enemy)
        {
            // TODO: all

            string sign = fEntry.Sign;
            if (sign.CompareTo("Anssk") != 0) {
                if (sign.Equals("Anxarcule")) {
                    AuxUtils.ExStub(" pulverizes your legs and devours them! ");
                    AuxUtils.ExStub(" rips your weapon from your grasp! ");
                    AuxUtils.ExStub("s");
                    AuxUtils.ExStub(" attempts to");
                    AuxUtils.ExStub(" summon");
                    AuxUtils.ExStub(" your double from an ");
                    AuxUtils.ExStub("alternate universe to fight yourself.");
                } else if (sign.Equals("Aspenth")) {
                    AuxUtils.ExStub(" opens your mouth with his tentacles.");
                    AuxUtils.ExStub("You begin to choke on water.");
                    AuxUtils.ExStub(" bites your abdomen.");
                } else if (sign.Equals("Breleor")) {
                    AuxUtils.ExStub(" (????, can be not his)");
                    AuxUtils.ExStub("The %s sends you away.");
                    AuxUtils.ExStub("Tendrils tunnel into your skin.");
                    AuxUtils.ExStub("A tendril reaches your heart.");
                    AuxUtils.ExStub("Pierced by a' (on death)");
                    AuxUtils.ExStub("It feels strange.' (if tendril not kill)");
                } else if (sign.Equals("Cockatrice")) {
                    AuxUtils.ExStub("The %s is turned to stone.");
                } else if (sign.Equals("Dreg")) {
                    AuxUtils.ExStub("???");
                } else if (sign.Equals("Edgewort")) {
                    AuxUtils.ExStub(" pushes");
                    AuxUtils.ExStub(" pulls");
                    AuxUtils.ExStub("The ");
                    AuxUtils.ExStub(" you toward your death.");
                    AuxUtils.ExStub(" hurls you to your death.");
                    AuxUtils.ExStub("Thrown from Bifrost+");
                } else if (sign.Equals("Emanon")) {
                    AuxUtils.ExStub("s hair stands on end.");
                    AuxUtils.ExStub(" are");
                    AuxUtils.ExStub(" is");
                    AuxUtils.ExStub("Your ");
                    AuxUtils.ExStub("stripped off.");
                    AuxUtils.ExStub("Your weapon is knocked from your grasp.");
                    AuxUtils.ExStub(" opens a gate.");
                    AuxUtils.ExStub("His minions surge through! ");
                    AuxUtils.ExStub("You feel worried.");
                } else if (sign.Equals("Enchantress")) {
                    AuxUtils.ExStub("The %s is stunned.");
                    Space.ShowText(enemy, BaseLocale.Format(RS.rs_SongCharmsYou, new object[]{ GetDeclinableName(Number.nSingle, Case.cGenitive) }));
                } else if (sign.Equals("GasBall")) {
                    AuxUtils.ExStub(" explodes.");
                    AuxUtils.ExStub("It deafens you.");
                    AuxUtils.ExStub("todo: +deafness");
                } else if (sign.Equals("Ghost")) {
                    int num = RandomHelper.GetRandom(3);
                    if (num != 0) {
                        if (num != 1) {
                            if (num == 2) {
                                Space.ShowText(enemy, BaseLocale.GetStr(RS.rs_YouQuiverInFear));
                            }
                        } else {
                            Space.ShowText(enemy, BaseLocale.GetStr(RS.rs_YouFleeInTerror));
                        }
                    } else {
                        Space.ShowText(enemy, BaseLocale.GetStr(RS.rs_YouOvercomeWithFear));
                    }
                } else if (sign.Equals("GiantSquid")) {
                    AuxUtils.ExStub("The %s is drowned.");
                } else if (sign.Equals("Gorm")) {
                    Space.ShowText(enemy, BaseLocale.GetStr(RS.rs_LongSleepBite));
                    AuxUtils.ExStub("rs_EternitySleepBite");
                    AuxUtils.ExStub("rs_YouAwakeDisoriented");
                } else if (sign.Equals("Gulveig")) {
                    AuxUtils.ExStub(" opens a gate.");
                    AuxUtils.ExStub("s minions surge through! ");
                    AuxUtils.ExStub(" summons his worshippers.");
                    AuxUtils.ExStub(" slides a tentacle into your flesh.");
                    AuxUtils.ExStub("A chill sweeps through you.");
                    AuxUtils.ExStub("s tentacles miss you.");
                    AuxUtils.ExStub("+ Gout()");
                } else if (sign.Equals("Hatchetfish")) {
                    AuxUtils.ExStub("The razor-sharp teeth of the ");
                    AuxUtils.ExStub(" rip ");
                    AuxUtils.ExStub("into ");
                    AuxUtils.ExStub(".");
                    Space.ShowText(enemy, BaseLocale.Format(RS.rs_TeethRipIntoFlesh, new object[]{ GetDeclinableName(Number.nSingle, Case.cGenitive) }));
                } else if (sign.Equals("Hela")) {
                    AuxUtils.ExStub(" rises her domain against you. (rp_satt5)");
                    AuxUtils.ExStub(" calls out to' (may be not she)");
                    AuxUtils.ExStub(" the souls of those you have slain in combat.' (may be not she)");
                } else if (sign.Equals("Iridorn")) {
                    AuxUtils.ExStub("The %s's head is torn off.");
                    AuxUtils.ExStub("The ");
                    AuxUtils.ExStub(" smiles ");
                    AuxUtils.ExStub("as it tears your head off with its talons.");
                    AuxUtils.ExStub("Beheaded");
                    AuxUtils.ExStub("s talons scratch against your gorget.");
                    AuxUtils.ExStub(" misses your throat.");
                } else if (sign.Equals("IvyCreeper")) {
                    AuxUtils.ExStub("rs_YouAreBeingCrushed");
                } else if (sign.Equals("Jagredin")) {
                    AuxUtils.ExStub("Your flesh burns.");
                    AuxUtils.ExStub("You are not affected.");
                } else if (sign.Equals("KnellBird")) {
                    AuxUtils.ExStub("Your mind reels with pain! ");
                    AuxUtils.ExStub("n");
                    AuxUtils.ExStub("Pierced by the gaze of a");
                    AuxUtils.ExStub(" ");
                    AuxUtils.ExStub("The ");
                    AuxUtils.ExStub(" dies.");
                    AuxUtils.ExStub("Something");
                    AuxUtils.ExStub(" screams in pain! ");
                } else if (sign.Equals("KonrRig")) {
                    AuxUtils.ExStub(" probes your mind. (rp_satt5)");
                    AuxUtils.ExStub("rs_YouBecomesInsane");
                } else if (sign.Equals("Magician")) {
                    Space.ShowText(enemy, BaseLocale.Format(RS.rs_XGesticulates, new object[]{ base.Name }));
                    Space.ShowText(enemy, BaseLocale.GetStr(RS.rs_YourPackQuivers));
                } else if (sign.Equals("Minion")) {
                    AuxUtils.ExStub("The creatures of Niflheim subdue you! ");
                } else if (sign.Equals("Moleman")) {
                    AuxUtils.ExStub("The %s collapses the ceiling on %s.");
                } else if (sign.Equals("Nidhogg")) {
                    AuxUtils.ExStub(" absorbs some of your invulnerability.");
                    AuxUtils.ExStub(" barks. Lackeys appear.");
                    AuxUtils.ExStub("killhim");
                    AuxUtils.ExStub("White dust fills the air around you.");
                    AuxUtils.ExStub("You begin to hallucinate.");
                    AuxUtils.ExStub("You are momentarily stunned.");
                    AuxUtils.ExStub("You can move again.");
                    AuxUtils.ExStub(" wills you to wield a new weapon.");
                    AuxUtils.ExStub("You drop your weapon.");
                    AuxUtils.ExStub("You wield nothing.");
                    AuxUtils.ExStub("You now wield ");
                    AuxUtils.ExStub(".");
                    AuxUtils.ExStub("You resist ");
                    AuxUtils.ExStub("s will.");
                    AuxUtils.ExStub(" siphons off the effects of your potion.");
                    AuxUtils.ExStub("A tendril from ");
                    AuxUtils.ExStub("s body probes your spine.");
                    AuxUtils.ExStub("You feel slower.");
                    AuxUtils.ExStub(" belches smoke.");
                    AuxUtils.ExStub("You hear a strange whine inside your head.");
                    AuxUtils.ExStub("hel ration");
                    AuxUtils.ExStub("Your pack shifts, as if restless.");
                } else if (sign.Equals("Nidslacr")) {
                    Space.ShowText(enemy, BaseLocale.GetStr(RS.rs_YouFeelTingling));
                    AuxUtils.ExStub("Your eyes recede.");
                    AuxUtils.ExStub("You can no longer levitate.");
                    AuxUtils.ExStub("You can no longer control others.");
                    AuxUtils.ExStub("Your power to identify objects is gone.");
                    AuxUtils.ExStub("You no longer remember the art of alchemy.");
                    AuxUtils.ExStub("You have lost the ability to swim.");
                    AuxUtils.ExStub("You can no longer teleport at will.");
                } else if (sign.Equals("Pelgrat")) {
                    AuxUtils.ExStub("s");
                    AuxUtils.ExStub("The ");
                    AuxUtils.ExStub(" draws charges from your ");
                    AuxUtils.ExStub("wand");
                    AuxUtils.ExStub(" and grows stronger.");
                    AuxUtils.ExStub("It seems to have changed its shape slightly.");
                } else if (sign.Equals("PhantomAsp")) {
                    AuxUtils.ExStub("The %s is poisoned.");
                } else if (sign.Equals("Plog")) {
                    AuxUtils.ExStub("+ Gout()");
                } else if (sign.Equals("Preden")) {
                    enemy.UseEffect(EffectID.eid_Fever, null, InvokeMode.im_Use, null);
                } else if (sign.Equals("PyrtaAth")) {
                    AuxUtils.ExStub("The ");
                    AuxUtils.ExStub(" suffocates you! ");
                    AuxUtils.ExStub("You can no longer breathe.");
                    AuxUtils.ExStub("Smothered by a");
                    AuxUtils.ExStub(" ");
                    AuxUtils.ExStub(" engulfs your head! ");
                } else if (sign.Equals("RedOoze")) {
                    AuxUtils.ExStub("The %s is consumed.");
                    Space.ShowText(enemy, BaseLocale.GetStr(RS.rs_YouCannotBreakFree));
                    AuxUtils.ExStub("rs_YourFleshBeginsToMelt      = 1094;");
                    AuxUtils.ExStub("rs_YouEscapeGrasp             = 1095;");
                    AuxUtils.ExStub("rs_YouAwakeDisoriented        = 1096;");
                    AuxUtils.ExStub("??? > The red ooze consumes your mace.");
                    AuxUtils.ExStub("??? > The red ooze begins consume you.");
                } else if (sign.Equals("Retchweed")) {
                    Space.ShowText(enemy, BaseLocale.GetStr(RS.rs_NauseaBrings));
                } else if (sign.Equals("Roc")) {
                    AuxUtils.ExStub("The / grabs the /, /soars high into the air, and hurls it down.");
                } else if (sign.Equals("Scyld")) {
                    AuxUtils.ExStub("Horrible rays emanate from his eyes! ");
                    AuxUtils.ExStub("The rays hit you.");
                    AuxUtils.ExStub("The rays miss.");
                    AuxUtils.ExStub(" ignores you.");
                    AuxUtils.ExStub(" claps.");
                    AuxUtils.ExStub("You are hurled back by a shock wave.");
                    AuxUtils.ExStub("You collide with ");
                    AuxUtils.ExStub("the ");
                    AuxUtils.ExStub(".");
                    AuxUtils.ExStub("a");
                    AuxUtils.ExStub("Smashed into");
                    AuxUtils.ExStub(" ");
                    AuxUtils.ExStub("The ");
                    AuxUtils.ExStub(" is killed.");
                    AuxUtils.ExStub("pile of rubble");
                    AuxUtils.ExStub("tree");
                    AuxUtils.ExStub("rock");
                    AuxUtils.ExStub("wall");
                    AuxUtils.ExStub(" the ");
                    AuxUtils.ExStub("You slam into");
                    AuxUtils.ExStub("You bounce off of");
                    AuxUtils.ExStub("Smashed into a wall");
                    AuxUtils.ExStub(" exhales.");
                    AuxUtils.ExStub("The temperature surrounding him approaches ");
                    AuxUtils.ExStub("absolute zero.");
                    AuxUtils.ExStub("turned to ice");
                    AuxUtils.ExStub(" teleports you far away.");
                } else if (sign.Equals("Slinn")) {
                    AuxUtils.ExStub("+ Gout();");
                } else if (sign.Equals("Slywert")) {
                    AuxUtils.ExStub(" shifts in a glint of light.");
                    AuxUtils.ExStub(" whispers your name.");
                    AuxUtils.ExStub(" snacks on a small creature.");
                    Space.ShowText(enemy, BaseLocale.Format(RS.rs_FlashesToothyGrin, new object[]{ base.Name }));
                } else if (sign.Equals("Spirit")) {
                    AuxUtils.ExStub("The %s drains %s.");
                    AuxUtils.ExStub("The ");
                    AuxUtils.ExStub(" drains you! ");
                    AuxUtils.ExStub("You are drained.");
                    AuxUtils.ExStub("s touch paralyzes you.");
                    AuxUtils.ExStub("You are paralyzed.");
                } else if (sign.Equals("Summoner")) {
                    Space.ShowText(enemy, BaseLocale.Format(RS.rs_XZapsWand, new object[]{ base.Name }));
                } else if (sign.Equals("<terrain>")) {
                    AuxUtils.ExStub("todo: lava and ?");
                    AuxUtils.ExStub(" almost");
                    AuxUtils.ExStub("The %s drowns you.");
                    AuxUtils.ExStub("Drowned by a ");
                    AuxUtils.ExStub("Burned by animated lava");
                    AuxUtils.ExStub("rs_LavaEncasesYou");
                    AuxUtils.ExStub("rs_EncasedInVolcanicRock");
                } else if (sign.Equals("Vampire")) {
                    AuxUtils.ExStub("The %s drains %s.");
                    AuxUtils.ExStub(" The vampire drains you. You lose a life level.");
                } else if (sign.Equals("Vanisher")) {
                    AuxUtils.ExStub("You begin to disappear.");
                } else if (sign.Equals("Wier")) {
                    AuxUtils.ExStub("Greed overwhelms you.");
                    AuxUtils.ExStub("You sit to count your gold.");
                } else if (sign.Equals("Wight")) {
                    enemy.AddEffect(EffectID.eid_Withered, ItemState.is_Normal, EffectAction.ea_Persistent, false, BaseLocale.Format(RS.rs_TouchWithersYou, new object[]{ GetDeclinableName(Number.nSingle, Case.cGenitive) }));
                } else if (sign.Equals("Wizard")) {
                    AuxUtils.ExStub("rs_YouAreFrozen");
                    AuxUtils.ExStub("nwrWin.ShowText(aEnemy, rsList(rs_YouHearPowerfulSpell]);");
                    AuxUtils.ExStub("nwrWin.ShowText(aEnemy, Format(rsList(rs_XCastsSpell], [Self.Name]));");
                    AuxUtils.ExStub("Drained by a");
                } else if (sign.Equals("Ull")) {
                    AuxUtils.ExStub("???");
                }
            }
        }

        public void DefenseSpecialEffect(NWCreature enemy)
        {
            string entrySign = fEntry.Sign;

            if (entrySign.Equals("Anssk")) {
                AuxUtils.ExStub("The ");
                AuxUtils.ExStub("s gaze");
                AuxUtils.ExStub("    nwrWin.ShowText(aEnemy, Format(rsList(rs_YouHypnotized], [Self.GetDeclinableName(nSingle, cGenitive)]));");
                AuxUtils.ExStub(" reflects off your shield.");
            } else if (entrySign.Equals("Breleor")) {
                AuxUtils.ExStub("//UseEffect(eid_Breleor_Tendril, nil, im_ItSelf, );");
            } else if (entrySign.Equals("Cockatrice")) {
                AuxUtils.ExStub("You hit the ");
                AuxUtils.ExStub(" with your ");
                AuxUtils.ExStub(".");
                AuxUtils.ExStub("rs_YouTurnToStone");
                AuxUtils.ExStub("Touched a");
                AuxUtils.ExStub(" ");
                AuxUtils.ExStub("%");
            } else if (entrySign.Equals("Dreg")) {
                AuxUtils.ExStub("    nwrWin.ShowText(aEnemy, Format(rsList(rs_XSplashesYou], [Self.Name]));");
                AuxUtils.ExStub("Sizzled by a");
                AuxUtils.ExStub(" ");
                AuxUtils.ExStub("rs_YouSeemUnaffected");
                AuxUtils.ExStub("s");
                AuxUtils.ExStub("Your ");
                AuxUtils.ExStub(" corrode");
                AuxUtils.ExStub(".");
                AuxUtils.ExStub("     nwrWin.ShowText(aEnemy, rsList(rs_YourItemsCorrodes]);");
                AuxUtils.ExStub("     -1 bonus у некоторых предметов (в оригинале - у армора)");
            } else if (entrySign.Equals("<evil_one>")) {
                AuxUtils.ExStub(" looks your direction.");
                AuxUtils.ExStub("Pain tears through your mind and body.");
                AuxUtils.ExStub("Annoyed ");
            } else if (entrySign.Equals("Faleryn")) {
                AuxUtils.ExStub("You chop off one of the ");
                AuxUtils.ExStub("s branches.");
            } else if (entrySign.Equals("GiantSquid")) {
                AuxUtils.ExStub("You cut off a tentacle.");
            } else if (entrySign.Equals("Gorm")) {
                AuxUtils.ExStub("Your weapon corrodes.");
                AuxUtils.ExStub("The %s's blood splashes you.");
                AuxUtils.ExStub("Killed by molecular acid");
                AuxUtils.ExStub("rs_YouSeemUnaffected");
            } else if (entrySign.Equals("IvyCreeper")) {
                AuxUtils.ExStub("You release a cloud of spores.");
                AuxUtils.ExStub("You inhale poison gas.");
                AuxUtils.ExStub("rs_YouGoBlind");
                AuxUtils.ExStub("You skin feels squishy.");
            } else if (entrySign.Equals("Jagredin")) {
                AuxUtils.ExStub("Your flesh burns.");
                AuxUtils.ExStub("Died from system shock");
            } else if (entrySign.Equals("KonrRig")) {
                AuxUtils.ExStub(" grows stronger.");
            } else if (entrySign.Equals("Nidhogg")) {
                AuxUtils.ExStub(" is temporarily invulnerable.");
            } else if (entrySign.Equals("Nidslacr")) {
                AuxUtils.ExStub("The ");
                AuxUtils.ExStub("the ");
                AuxUtils.ExStub(" catches ");
                AuxUtils.ExStub(".");
                AuxUtils.ExStub("them");
                AuxUtils.ExStub("it");
                AuxUtils.ExStub("He throws ");
                AuxUtils.ExStub(" on the ground in disgust.");
            } else if (entrySign.Equals("Plog")) {
                AuxUtils.ExStub(" opens a gate.");
                AuxUtils.ExStub("His minions surge through! ");
                AuxUtils.ExStub("killhim");
                AuxUtils.ExStub(" snarls a curse.");
                AuxUtils.ExStub("s hot breath slaps your face.");
                AuxUtils.ExStub("s");
                AuxUtils.ExStub("Your wand");
                AuxUtils.ExStub(" pulsate");
                AuxUtils.ExStub(".");
                AuxUtils.ExStub(" grows stronger.");
                AuxUtils.ExStub(" summons his worshippers.");
            } else if (entrySign.Equals("PyrtaAth")) {
                AuxUtils.ExStub("You hurt your head as well as the %s.");
                AuxUtils.ExStub("Inadvertantly killed ");
                AuxUtils.ExStub("self");
            } else if (entrySign.Equals("RedOoze")) {
                AuxUtils.ExStub("You hit the ");
                AuxUtils.ExStub(".");
                AuxUtils.ExStub("The ");
                AuxUtils.ExStub(" consumes your gloves.");
                AuxUtils.ExStub("rs_YourFleshBeginsToMelt");
                AuxUtils.ExStub("Eaten by ");
                AuxUtils.ExStub(" consumes your ");
            } else if (entrySign.Equals("Sentinel")) {
                AuxUtils.ExStub("You destroy the ");
                AuxUtils.ExStub("s main eye.");
                AuxUtils.ExStub("You destroy ");
                AuxUtils.ExStub("the last ");
                AuxUtils.ExStub("an ");
                AuxUtils.ExStub("eyestalk.");
            } else if (entrySign.Equals("StunJelly")) {
                AuxUtils.ExStub("You are stuck.");
            }
        }

        public Item CanCure(EffectID eid)
        {
            Item result = null;

            // TODO: ???
            if (eid == EffectID.eid_None) {
                // wounds
                result = FindMedicineItem(EffectID.eid_Cure, ItemStates.Create(ItemState.is_Normal, ItemState.is_Blessed));
                if (result == null) {
                    result = FindMedicineItem(EffectID.eid_Rejuvenation, ItemStates.Create(ItemState.is_Normal, ItemState.is_Blessed));
                }
                if (result == null) {
                    result = FindMedicineItem(EffectID.eid_Endurance, ItemStates.Create(ItemState.is_Normal, ItemState.is_Blessed));
                }
            } else {
                if (eid == EffectID.eid_Confusion) {
                    result = FindMedicineItem(EffectID.eid_Cure, ItemStates.Create(ItemState.is_Normal, ItemState.is_Blessed));
                    AuxUtils.ExStub("todo: +IvyCreeper");
                } else {
                    if (eid == EffectID.eid_Fading) {
                        AuxUtils.ExStub("???");
                    } else {
                        if (eid == EffectID.eid_Intoxicate || eid == EffectID.eid_Contamination) {
                            result = FindMedicineItem(EffectID.eid_Cure, ItemStates.Create(ItemState.is_Blessed));
                        } else {
                            if (eid == EffectID.eid_Blindness || eid == EffectID.eid_Fever || eid == EffectID.eid_Withered) {
                                result = FindMedicineItem(EffectID.eid_Cure, ItemStates.Create(ItemState.is_Normal, ItemState.is_Blessed));
                            } else {
                                if (eid == EffectID.eid_Hallucination) {
                                    result = FindMedicineItem(EffectID.eid_Cure, ItemStates.Create(ItemState.is_Normal, ItemState.is_Blessed));
                                    AuxUtils.ExStub("todo: +IvyCreeper");
                                } else {
                                    if (eid == EffectID.eid_HealInability || eid == EffectID.eid_Diseased) {
                                        result = FindMedicineItem(EffectID.eid_Cure, ItemStates.Create(ItemState.is_Normal, ItemState.is_Blessed));
                                    } else {
                                        if (eid == EffectID.eid_Burns || eid == EffectID.eid_LegsMissing || eid == EffectID.eid_Vertigo || eid == EffectID.eid_Lycanthropy) {
                                            result = FindMedicineItem(EffectID.eid_Cure, ItemStates.Create(ItemState.is_Blessed));
                                        } else {
                                            if (eid == EffectID.eid_BrainScarring) {
                                                result = FindMedicineItem(EffectID.eid_Cure, ItemStates.Create(ItemState.is_Blessed));
                                                AuxUtils.ExStub("todo: +IvyCreeper");
                                            } else {
                                                if (eid == EffectID.eid_Impregnation || eid == EffectID.eid_EyesMissing || eid == EffectID.eid_Deafness) {
                                                    result = FindMedicineItem(EffectID.eid_Cure, ItemStates.Create(ItemState.is_Blessed));
                                                } else {
                                                    if (eid == EffectID.eid_Fragile) {
                                                        AuxUtils.ExStub("???");
                                                    } else {
                                                        if (eid == EffectID.eid_Draining) {
                                                            AuxUtils.ExStub("???");
                                                        } else {
                                                            if (eid == EffectID.eid_Famine) {
                                                                AuxUtils.ExStub("???");
                                                            } else {
                                                                if (eid == EffectID.eid_Genesis) {
                                                                    AuxUtils.ExStub("???");
                                                                } else {
                                                                    if (eid == EffectID.eid_Paralysis) {
                                                                        AuxUtils.ExStub("???");
                                                                    } else {
                                                                        if (eid != EffectID.eid_Insanity) {
                                                                            throw new Exception("untilled disease");
                                                                        }
                                                                        AuxUtils.ExStub("todo: Result := FindCureItem(0, [], aMedicineItem);");
                                                                        AuxUtils.ExStub("todo: +IvyCreeper");
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return result;
        }

        public bool IsAvailable(LocatedEntity entity, bool checkLOS)
        {
            bool result;
            if (entity is NWCreature) {
                NWCreature cr = (NWCreature)entity;
                result = (LayerID == cr.LayerID && fField.X == cr.fField.X && fField.Y == cr.fField.Y);
                if (!result) {
                    return result;
                }
            }
            result = IsSeen(entity.PosX, entity.PosY, checkLOS);
            return result;
        }

        public override bool IsSeen(int aX, int aY, bool checkLOS)
        {
            int dist = MathHelper.Distance(PosX, PosY, aX, aY);
            bool result = (dist <= Survey);

            if (result && checkLOS) {
                result = CurrentField.LineOfSight(PosX, PosY, aX, aY);
            }

            return result;
        }

        // FIXME: how usage - strange code
        public bool CheckVisible(NWCreature creature)
        {
            bool invisible = creature.fEffects.FindEffectByID(EffectID.eid_Invisibility) != null;
            return !invisible || SeekInvisible;
        }

        public bool CanShoot(NWCreature enemy)
        {
            if (InWater) {
                return false;
            } else {
                ShootObj shootObj = new ShootObj();
                shootObj.ACreature = this;
                shootObj.AEnemy = enemy;
                return AuxUtils.DoLine(PosX, PosY, enemy.PosX, enemy.PosY, shootObj.LineProc, true);
            }
        }

        public bool CanSink()
        {
            return CurrentField.CanSink(PosX, PosY);
        }

        public bool CanTeach(NWCreature subject)
        {
            int cnt = 0;
            for (int i = 0; i < StaticData.dbTeachable.Length; i++) {
                int id = StaticData.dbTeachable[i].Id;
                TeachableKind kind = StaticData.dbTeachable[i].Kind;
                bool res = false;

                switch (kind) {
                    case TeachableKind.Ability:
                        AbilityID ab = (AbilityID)id;
                        res = (fAbilities.IndexOf(id) >= 0 && subject.GetAbility(ab) < GetAbility(ab));
                        break;

                    case TeachableKind.Skill:
                        SkillID sk = (SkillID)id;
                        res = (fSkills.IndexOf(id) >= 0 && subject.GetSkill(sk) < GetSkill(sk));
                        break;
                }

                if (res) {
                    cnt++;
                }
            }
            return cnt > 0;
        }

        // FIXME: ugly code!
        public int CheckEquipment(float dist, BestWeaponSigns bwSigns)
        {
            try {
                int highestDamage = 0;

                int num = fItems.Count;
                for (int i = 0; i < num; i++) {
                    Item item = fItems[i];
                    ItemFlags ifs = item.Flags;
                    if (ifs.HasIntersect(ItemFlags.if_MeleeWeapon, ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon)) {
                        bool onlyShootWeapon = (ifs.HasIntersect(ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon) && !ifs.Contains(ItemFlags.if_MeleeWeapon));

                        if (!bwSigns.Contains(BestWeaponSigns.CanShoot) && onlyShootWeapon) {
                            continue;
                        }

                        if (bwSigns.Contains(BestWeaponSigns.OnlyShoot) && (!ifs.HasIntersect(ItemFlags.if_ShootWeapon, ItemFlags.if_ThrowWeapon))) {
                            continue;
                        }

                        if ((ifs.Contains(ItemFlags.if_ShootWeapon)) && GetAmmoCount(item) <= 0) {
                            continue;
                        }

                        int damage = GetWeaponDamage(item);
                        if (damage > highestDamage) {
                            highestDamage = damage;
                        }
                    }
                }

                float fDam = 0.0f;
                if (highestDamage > 0) {
                    fDam = ((1.0f / (float)highestDamage));
                }

                float highestArmor = 0f;
                Item bestShield = null;

                Item bestWeapon = null;
                float bestWeight = 0f;

                for (int i = 0; i < num; i++) {
                    Item item = fItems[i];
                    ItemFlags ifs = item.Flags;
                    if (ifs.HasIntersect(ItemFlags.if_MeleeWeapon, ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon)) {
                        bool onlyShootWeapon = (ifs.HasIntersect(ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon) && !ifs.Contains(ItemFlags.if_MeleeWeapon));

                        if (!bwSigns.Contains(BestWeaponSigns.CanShoot) && onlyShootWeapon) {
                            continue;
                        }

                        if (bwSigns.Contains(BestWeaponSigns.OnlyShoot) && (!ifs.HasIntersect(ItemFlags.if_ShootWeapon, ItemFlags.if_ThrowWeapon))) {
                            continue;
                        }

                        if ((ifs.Contains(ItemFlags.if_ShootWeapon)) && GetAmmoCount(item) <= 0) {
                            continue;
                        }

                        float Val = (((float)dist - (float)GetWeaponRange(item)));
                        if (Val < 1f) {
                            Val = 1f;
                        } else {
                            Val = ((1.0f / Val));
                        }

                        float weight = ((fDam * (float)GetWeaponDamage(item) + Val));
                        if (weight > bestWeight) {
                            bestWeight = weight;
                            bestWeapon = item;
                        }
                    } else {
                        if (item.Kind == ItemKind.ik_Shield && item.GetAttribute(ItemAttribute.ia_Defense) > (double)highestArmor) {
                            bestShield = item;
                            highestArmor = ((float)item.GetAttribute(ItemAttribute.ia_Defense));
                        }
                    }
                }

                if (bestWeapon != null) {
                    Item item = GetItemByEquipmentKind(BodypartType.bp_RHand);
                    if (item != null) {
                        item.InUse = false;
                    }

                    item = GetItemByEquipmentKind(BodypartType.bp_LHand);
                    if (item != null) {
                        item.InUse = false;
                    }

                    bestWeapon.InUse = true;
                    highestDamage = GetWeaponDamage(bestWeapon);
                    ItemFlags wks = bestWeapon.Flags;
                    if (wks.Contains(ItemFlags.if_ShootWeapon)) {
                        item = GetItemByEquipmentKind(BodypartType.bp_Back);
                        if (item != null) {
                            item.InUse = false;
                        }
                        item = GetWeaponAmmo(bestWeapon);
                        item.InUse = true;
                    }
                }

                if (bestShield != null && bestWeapon != null && (!bestWeapon.TwoHanded)) {
                    Item lhItem = GetItemByEquipmentKind(BodypartType.bp_LHand);
                    if (lhItem != bestShield) {
                        if (lhItem != null) {
                            lhItem.InUse = false;
                        }
                        bestShield.InUse = true;
                    }
                }

                return highestDamage;
            } catch (Exception ex) {
                Logger.Write("NWCreature.checkEquipment(): " + ex.Message);
                throw ex;
            }
        }

        public void CheckHealth(ref bool  TurnUsed)
        {
            try {
                TurnUsed = false;

                if (HPCur < (HPMax_Renamed * 0.6)) {
                    CheckHealth_Solve(EffectID.eid_None, BaseLocale.GetStr(RS.rs_Wounds), ref TurnUsed);
                    if (TurnUsed) {
                        return;
                    }
                }

                int num = fEffects.Count;
                for (int i = 0; i < num; i++) {
                    int eff = fEffects[i].CLSID;
                    EffectID eid = (EffectID)eff;
                    if (EffectsData.dbEffects[eff].Flags.Contains(EffectFlags.ek_Disease)) {
                        CheckHealth_Solve(eid, BaseLocale.GetStr(EffectsData.dbEffects[eff].NameRS), ref TurnUsed);

                        if (TurnUsed) {
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.checkHealth(): " + ex.Message);
            }
        }

        /*public final boolean hasCondition(int condition)
        {
            return this.fConditions.Contains(condition);
        }
    
        public final void setCondition(int condition)
        {
            this.fConditions.include(condition);
        }
    
        public final void unsetCondition(int condition)
        {
            this.fConditions.exclude(condition);
        }*/

        public void AddEffect(EffectID effectID, ItemState state, EffectAction actionKind, bool invert, string msg)
        {
            int turns = Effect.GetDuration(effectID, state, invert);
            AddEffect(effectID, state, actionKind, turns, msg);
        }

        public void AddEffect(EffectID effectID, ItemState state, EffectAction actionKind, int duration, string msg)
        {
            int i = Effect.GetMagnitude(effectID);
            Effect effect = new Effect(fSpace, this, effectID, null, actionKind, duration, i);
            fEffects.Add(effect);
            if (IsPlayer) {
                Space.ShowText(this, msg);
            }
        }

        public void InitEffect(EffectID effectID, object source, EffectAction actionKind)
        {
            Effect effect = new Effect(fSpace, this, effectID, source, actionKind, 0, Effect.GetMagnitude(effectID));
            fEffects.Add(effect);
        }

        public void DoneEffect(EffectID effectID, object source)
        {
            int num = fEffects.Count;
            for (int i = 0; i < num; i++) {
                Effect eff = fEffects[i];
                if (eff.Source.Equals(source) && eff.CLSID == (int)effectID) {
                    fEffects.Delete(i);
                    break;
                }
            }
        }

        public void Death(string message, object source)
        {
            try {
                Space.ShowText(this, message);

                State = CreatureState.Dead;
                Turn = -RandomHelper.GetBoundedRnd(10, 20);

                if ((fEntry.Flags.Contains(CreatureFlags.esUnique)) && !fEntry.CorpsesPersist && !fEntry.Respawn) {
                    Space.SetVolatileState(fEntry.GUID, VolatileState.Destroyed);
                }

                DropAll();
                if (fIsTrader) {
                    Building house = (Building)FindHouse();
                    if (house != null) {
                        house.Holder = null;
                    }
                }

                PostDeath();
            } catch (Exception ex) {
                Logger.Write("NWCreature.death(): " + ex.Message);
                throw ex;
            }
        }

        private void PostDeath()
        {
            string entrySign = fEntry.Sign;
            if (entrySign.Equals("Bartok")) {
                AuxUtils.ExStub("todo: генерация псионической волны");
                AuxUtils.ExStub("todo: ???nwrWin.ShowText(?, rsList(rs_YouShakenViolently]);");
            } else {
                if (entrySign.Equals("LavaFlow")) {
                    PostDeath_SetTile(PlaceID.pid_Lava);
                } else if (entrySign.Equals("LiveRock")) {
                    PostDeath_SetTile(PlaceID.pid_CaveFloor);
                } else if (entrySign.Equals("MudFlow")) {
                    PostDeath_SetTile(PlaceID.pid_Mud);
                } else if (entrySign.Equals("Nidhogg")) {
                    if (LastAttacker.Equals(Space.Player)) {
                        Space.ShowText(this, BaseLocale.Format(RS.rs_XSlavers, new object[]{ base.Name }));
                    }
                } else if (entrySign.Equals("SandForm")) {
                    PostDeath_SetTile(PlaceID.pid_Quicksand);
                } else if (entrySign.Equals("WateryForm")) {
                    PostDeath_SetTile(PlaceID.pid_Water);
                }
            }

            Space.PostDeath(this);
        }

        public void CheckActionAbility(CreatureAction action, Item item)
        {
            var caRec = StaticData.dbCreatureActions[(int)action];
            if (caRec.Flags.HasIntersect(ActionFlags.WithItem, ActionFlags.CheckAbility)) {
                AbilityID ab = GetActionAbility(action, item);
                if (ab != AbilityID.Ab_None) {
                    int val = GetAbility(ab);
                    if (val < 100) {
                        SetAbility(ab, val + 1);
                    }
                }
            }
        }

        public bool HasTurn()
        {
            int num = fEffects.Count;
            for (int i = 0; i < num; i++) {
                Effect effect = fEffects[i];
                EffectID eff = (EffectID)effect.CLSID;
                if (eff == EffectID.eid_Paralysis || eff == EffectID.eid_Sleep || eff == EffectID.eid_Stoning || eff == EffectID.eid_CaughtInNet) {
                    return false;
                }
            }

            return true;
        }

        public virtual void DoTurn()
        {
            try {
                IncTurn();
                fEffects.Execute();
                Recovery();

                if (fEntry.Flags.Contains(CreatureFlags.esUseItems)) {
                    ApplySpecialItems();
                }

                int playerSpeed = Space.Player.Speed;

                if ((Stamina >= playerSpeed && !IsPlayer) || Prowling) {
                    LastAttacker = null;

                    if (HasTurn()) {
                        // FIXME: move to goal!
                        bool turn_used = false;
                        if (fEntry.Flags.HasIntersect(CreatureFlags.esMind, CreatureFlags.esUseItems) && (Mercenary || GlobalVars.nwrWin.ExtremeMode)) {
                            CheckHealth(ref turn_used);
                        }

                        if (!turn_used) {
                            // FIXME: move to goal!
                            DoDefaultAction();

                            if (fBrain != null) {
                                fBrain.Think();
                            }
                        }
                    }

                    Stamina -= playerSpeed;
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.doTurn(): " + ex.Message);
            }
        }

        public void Drown()
        {
            if (!fEntry.Flags.HasIntersect(CreatureFlags.esSwimming, CreatureFlags.esFlying)) {
                if (fEffects.FindEffectByID(EffectID.eid_Deafness) == null) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_Gurgle));
                }
                Death(BaseLocale.GetStr(RS.rs_XDrowns), null);
            }
        }

        public Item FindMedicineItem(EffectID cureEffect, ItemStates states)
        {
            int num = fItems.Count;
            for (int i = 0; i < num; i++) {
                Item item = fItems[i];
                ItemEntry.EffectEntry[] effects = item.Entry.Effects;
                if (((effects != null) ? effects.Length : 0) >= 1) {
                    EffectID eid = effects[0].EffID;
                    if (eid == cureEffect && states.Contains(item.State)) {
                        return item;
                    }
                }
            }

            return null;
        }

        public DungeonRoom FindDungeonRoom()
        {
            DungeonRoom result = null;

            NWField fld = CurrentField;
            int id = fld.Layer.EntryID;
            if (id == GlobalVars.Layer_Svartalfheim1 || id == GlobalVars.Layer_Svartalfheim2 || id == GlobalVars.Layer_Svartalfheim3 || id == GlobalVars.Layer_Armory || id == GlobalVars.Layer_GrynrHalls) {
                int x = PosX;
                int y = PosY;

                result = fld.FindDungeonRoom(x, y);

                if (result == null) {
                    x += fld.Coords.X * StaticData.FieldWidth;
                    y += fld.Coords.Y * StaticData.FieldHeight;

                    result = fld.Layer.FindDungeonRoom(x, y);
                }
            }

            return result;
        }

        public NWCreature FindEnemy()
        {
            NWCreature enemy = DefaultEnemy;
            if (enemy != null) {
                return enemy;
            }

            enemy = FindEnemyByAttack();
            if (enemy != null) {
                return enemy;
            }

            NWField fld = CurrentField;
            int dist = StaticData.FieldWidth;
            CreaturesList crt = fld.Creatures;
            int num = crt.Count;
            for (int i = 0; i < num; i++) {
                NWCreature cr = crt[i];
                if (!cr.Equals(this)) {
                    int dt = MathHelper.Distance(Location, cr.Location);
                    if (dt < Survey) {
                        bool ok = IsAvailable(cr, true) && IsEnemy(cr) && CheckVisible(cr);
                        if (ok && dt < dist) {
                            enemy = cr;
                            dist = dt;
                        }
                    }
                }
            }

            if (enemy != null) {
                return enemy;
            }

            Player player = Space.Player;
            if (player.Effects.FindEffectByID(EffectID.eid_Genesis) != null) {
                return player;
            }

            return null;
        }

        public Building FindHouse()
        {
            if (fHouse == null) {
                var features = CurrentField.Features;
                int num = features.Count;
                for (int i = 0; i < num; i++) {
                    GameEntity b = features[i];

                    if (b is Building && Equals(((Building)b).Holder)) {
                        fHouse = ((Building)b);
                        break;
                    }
                }
            }

            return fHouse;
        }

        public Item FindItem()
        {
            Item result = null;
            int idx = -1;
            int dt = (int)(Survey + 1);

            NWField fld = CurrentField;

            int num = fld.Items.Count;
            for (int i = 0; i < num; i++) {
                Item item = fld.Items[i];
                int dist = MathHelper.Distance(Location, item.Location);
                Building b = fld.FindBuilding(item.PosX, item.PosY);
                if ((b == null || b.Holder == null) && dist < dt && item.CLSID != GlobalVars.iid_DeadBody && item.CLSID != GlobalVars.iid_Mummy) {
                    dt = dist;
                    idx = i;
                }
            }

            if (idx >= 0) {
                result = fld.Items[idx];
            }

            return result;
        }

        public AbilityID GetActionAbility(CreatureAction action, Item item)
        {
            AbilityID result = AbilityID.Ab_None;
            switch (action) {
                case CreatureAction.caAttackMelee:
                    if (item == null) {
                        result = AbilityID.Ab_HandToHand;
                    } else {
                        result = StaticData.dbItemKinds[(int)item.Kind].Ability;
                    }
                    break;

                case CreatureAction.caAttackShoot:
                    if (item != null) {
                        result = StaticData.dbItemKinds[(int)item.Kind].Ability;
                    }
                    break;

                case CreatureAction.caAttackThrow:
                    result = AbilityID.Ab_Marksman;
                    break;

                case CreatureAction.caAttackParry:
                    result = AbilityID.Ab_Parry;
                    break;

                case CreatureAction.caItemUse:
                    if (item != null) {
                        result = StaticData.dbItemKinds[(int)item.Kind].Ability;
                    }
                    break;
            }
            return result;
        }

        public int GetAmmoCount(Item aWeapon)
        {
            int Result = 0;
            Item ammo = GetWeaponAmmo(aWeapon);
            if (ammo != null) {
                Result = (int)ammo.Count;
            }
            return Result;
        }

        public SkillID GetAttackSkill(int aDistance, ref int  aDamage)
        {
            SkillID result = SkillID.Sk_None;

            aDamage = 0;
            IntList sks = new IntList();
            try {
                int num = fSkills.Count;
                for (int i = 0; i < num; i++) {
                    int skid = fSkills.GetItem(i).AID;
                    SkillRec skRec = StaticData.dbSkills[skid];
                    EffectID e = skRec.Effect;
                    MNXRange range = EffectsData.dbEffects[(int)e].Magnitude;
                    int rangeVal = (int)Math.Round((range.Min + range.Max) / 2.0d);
                    if (e != EffectID.eid_None && EffectsData.dbEffects[(int)e].Flags.Contains(EffectFlags.ek_Offence) && rangeVal >= aDistance) {
                        sks.Add(skid);
                    }
                }

                int cnt = sks.Count;
                if (cnt > 1) {
                    result = (SkillID)(sks[RandomHelper.GetRandom(cnt)]);
                } else {
                    if (cnt == 1) {
                        result = (SkillID)(sks[0]);
                    }
                }

                if (result != SkillID.Sk_None) {
                    SkillRec skRec = StaticData.dbSkills[(int)result];
                    int e = (int)skRec.Effect;
                    aDamage = (int)((long)Math.Round((double)(EffectsData.dbEffects[e].Damage.Min + EffectsData.dbEffects[e].Damage.Max) / 2.0));
                }
            } finally {
            }
            return result;
        }

        public string GetDeclinableName(Number dNumber, Case dCase)
        {
            try {
                string result;

                string dn = fEntry.GetNounDeclension(dNumber, dCase);
                RaceID race = fEntry.Race;

                if (race == RaceID.crHuman) {
                    if (IsPlayer) {
                        result = fName;
                    } else {
                        result = dn + " " + StaticData.MorphCompNoun(fName, dCase, dNumber, Sex, true, false);
                    }
                } else {
                    result = dn;
                }

                return result;
            } catch (Exception ex) {
                Logger.Write("NWCreature.getDeclinableName(): " + ex.Message);
                throw ex;
            }
        }

        public TrapResult GetEntrapRes(ref string  refMessage)
        {
            refMessage = "";
            TrapResult result = TrapResult.Absent;

            Effect eff = fEffects.FindEffectByID(EffectID.eid_PitTrap);
            if (eff != null) {
                int chn = 100 - eff.Duration * 10;
                if (!AuxUtils.Chance(chn)) {
                    refMessage = BaseLocale.GetStr(RandomHelper.GetBoundedRnd(541, 545));
                    result = TrapResult.EscapeBreak;
                } else {
                    fEffects.Remove(eff);
                    refMessage = BaseLocale.GetStr(RS.rs_PitTrapGetoutOk);
                    result = TrapResult.EscapeOk;
                }
            } else {
                eff = fEffects.FindEffectByID(EffectID.eid_Quicksand);
                if (eff != null) {
                    if (eff.Duration == 1) {
                        refMessage = BaseLocale.GetStr(RS.rs_YouDrownedInQuicksand);
                        result = TrapResult.EscapeDeath;
                    } else {
                        if (eff.Duration == 2) {
                            refMessage = BaseLocale.GetStr(RS.rs_YouSinkToYourChin);
                            result = TrapResult.EscapeBreak;
                        } else {
                            refMessage = BaseLocale.GetStr(RS.rs_StrugglingIsUseless);
                            result = TrapResult.EscapeBreak;
                        }
                    }
                } else {
                    eff = fEffects.FindEffectByID(EffectID.eid_PhaseTrap);
                    if (eff != null) {
                        if (fEffects.FindEffectByID(EffectID.eid_Phase) != null) {
                            result = TrapResult.Absent;
                        } else {
                            refMessage = BaseLocale.GetStr(RS.rs_YouInVeryConfinedSpace);
                            result = TrapResult.EscapeBreak;
                        }
                    } else {
                        int bg = ((NWTile)CurrentField.GetTile(PosX, PosY)).BackBase;
                        if (bg == PlaceID.pid_Mud && AuxUtils.Chance(50)) {
                            refMessage = BaseLocale.GetStr(RS.rs_YouSlipInMud);
                            result = TrapResult.EscapeBreak;
                        }
                    }
                }
            }

            return result;
        }

        public NWCreature GetNearestCreature(int aX, int aY, bool onlyAllies)
        {
            NWField fld = CurrentField;
            int num = fld.Creatures.Count;
            for (int i = 0; i < num; i++) {
                NWCreature creat = fld.Creatures[i];

                if (!creat.Equals(this)) {
                    int dx = Math.Abs(aX - creat.PosX);
                    int dy = Math.Abs(aY - creat.PosY);
                    if (dx <= 1 && dy <= 1 && (!onlyAllies || (onlyAllies && !IsEnemy(creat)))) {
                        return creat;
                    }
                }
            }
            return null;
        }

        public ExtPoint GetNearestPlace(int radius, bool withoutLive)
        {
            return CurrentField.GetNearestPlace(PosX, PosY, radius, withoutLive, Movements);
        }

        public ExtPoint GetNearestPlace(int cx, int cy, int radius, bool withoutLive)
        {
            return CurrentField.GetNearestPlace(cx, cy, radius, withoutLive, Movements);
        }

        public ExtPoint GetNearestPlace(ExtPoint cpt, int radius, bool withoutLive)
        {
            return CurrentField.GetNearestPlace(cpt.X, cpt.Y, radius, withoutLive, Movements);
        }

        public ExtPoint GetStep(ExtPoint target)
        {
            ExtPoint result = ExtPoint.Empty;
            try {
                AbstractMap map = CurrentField;
                ExtPoint src = Location;

                if (!src.Equals(target) && map.IsValid(target.X, target.Y)) {
                    int dist = MathHelper.Distance(src, target);

                    if (dist == 1 && CanMove(map, target.X, target.Y)) {
                        result = target;
                    }

                    if ((result.IsEmpty) && IsSeen(target.X, target.Y, true)) {
                        int tx = src.X;
                        int ty = src.Y;
                        if (target.X > tx) {
                            tx++;
                        } else {
                            if (target.X < tx) {
                                tx--;
                            }
                        }
                        if (target.Y > ty) {
                            ty++;
                        } else {
                            if (target.Y < ty) {
                                ty--;
                            }
                        }

                        if (CanMove(map, tx, ty)) {
                            result = new ExtPoint(tx, ty);
                        }
                    }

                    if (result.IsEmpty) {
                        PathSearch.PSResult res = PathSearch.Search(map, src, target, this);
                        if (res != null) {
                            result = res.Step;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.getStep(): " + ex.Message);
            }
            return result;
        }

        public Item GetWeaponAmmo(Item aWeapon)
        {
            Item result = null;

            int ammoID = -1;
            if (aWeapon.CLSID == GlobalVars.iid_LongBow) {
                ammoID = GlobalVars.iid_Arrow;
            } else {
                if (aWeapon.CLSID == GlobalVars.iid_CrossBow) {
                    ammoID = GlobalVars.iid_Bolt;
                }
            }

            if (ammoID > -1) {
                result = ((Item)Items.FindByCLSID(ammoID));
            }

            return result;
        }

        public int GetWeaponDamage(Item weapon)
        {
            int result = 0;

            if (weapon != null && weapon.Weapon) {
                ItemFlags wks = weapon.Flags;
                if (wks.Contains(ItemFlags.if_MeleeWeapon)) {
                    result = weapon.AverageDamage;
                } else {
                    Item projectile = null;
                    if (wks.Contains(ItemFlags.if_ThrowWeapon)) {
                        projectile = weapon;
                    } else {
                        if (wks.Contains(ItemFlags.if_ShootWeapon)) {
                            projectile = GetWeaponAmmo(weapon);
                        }
                    }

                    if (projectile != null) {
                        result = projectile.AverageDamage;
                    }
                }
            }

            return result;
        }

        public int GetWeaponRange(Item weapon)
        {
            int result = 0;

            if (weapon != null && weapon.Weapon) {
                ItemFlags wks = weapon.Flags;
                if (wks.Contains(ItemFlags.if_MeleeWeapon)) {
                    result = 1;
                } else {
                    Item projectile = null;
                    if (wks.Contains(ItemFlags.if_ThrowWeapon)) {
                        projectile = weapon;
                    } else {
                        if (wks.Contains(ItemFlags.if_ShootWeapon)) {
                            projectile = GetWeaponAmmo(weapon);
                        }
                    }

                    if (projectile != null) {
                        result = GetProjectileRange(projectile);
                    }
                }
            }

            return result;
        }

        private int GetProjectileRange(Item projectile)
        {
            if (projectile != null) {
                int range = Math.Max(3, (int)((long)Math.Round((Strength - 9 - (double)projectile.Weight / 7.0))));
                if (projectile.CLSID == GlobalVars.iid_Mjollnir) {
                    range += 4;
                } else {
                    if (projectile.CLSID == GlobalVars.iid_Arrow || projectile.CLSID == GlobalVars.iid_Bolt) {
                        range += 5;
                    }
                }
                return range;
            } else {
                return 0;
            }
        }

        public bool HasAffect(EffectID effectID)
        {
            int eid = (int)effectID;
            AbilityID resist = EffectsData.dbEffects[eid].Resistance;

            return GetAbility(resist) <= 0 && (!EffectsData.dbEffects[eid].Flags.Contains(EffectFlags.ef_Ray) || GetAbility(AbilityID.Resist_Ray) <= 0);
        }

        public bool HasMovement(int kind)
        {
            return Movements.Contains(kind);
        }

        public void IncTurn()
        {
            Turn++;
        }

        public void InvertHostility()
        {
            Alignment = Alignment.Invert();
        }

        public bool IsEnemy(NWCreature aOther)
        {
            return GetReaction(aOther) == Reaction.Hostile;
        }

        public bool IsNear(ExtPoint point)
        {
            return MathHelper.Distance(PosX, PosY, point.X, point.Y) == 1;
        }

        public virtual void KnowIt(int entityID)
        {
        }

        public bool CheckMove(Movements aNeed)
        {
            bool result = (Movements.HasIntersect(aNeed));
            if (!result) {
                if ((aNeed.Contains(Movements.mkWalk)) && fEffects.FindEffectByID(EffectID.eid_LegsMissing) != null) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouHaveNoLegs));
                }
                if ((aNeed.Contains(Movements.mkSwim)) && (GetAbility(AbilityID.Ab_Swimming) <= 0 || fEffects.FindEffectByID(EffectID.eid_Sail) == null)) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCannotSwim));
                }
                if ((aNeed.Contains(Movements.mkFly)) && GetAbility(AbilityID.Ab_Levitation) <= 0) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCannotLevitate));
                }
            }
            return result;
        }

        public override bool CanMove(IMap map, int aX, int aY)
        {
            Movements ms = Movements;
            return ((AbstractMap)map).IsValidMove(aX, aY, ms);
        }

        public ExtPoint SearchRndLocation(AbstractMap map, ExtRect area)
        {
            try {
                NWField fld = (NWField)map;

                int tries = 150;
                while (tries > 0) {
                    int x = RandomHelper.GetBoundedRnd(area.Left, area.Right);
                    int y = RandomHelper.GetBoundedRnd(area.Top, area.Bottom);

                    if (fld.FindCreature(x, y) == null) {
                        // WARN: this check not removal to other place
                        if (fld.LandID == GlobalVars.Land_Crossroads) {
                            int fore = fld.GetTile(x, y).ForeBase;
                            if (fore == PlaceID.pid_cr_Disk) {
                                return new ExtPoint(x, y);
                            }
                        } else {
                            if (CanMove(map, x, y)) {
                                return new ExtPoint(x, y);
                            }
                        }
                    }

                    tries--;
                }

                return ExtPoint.Empty;
            } catch (Exception ex) {
                Logger.Write("NWCreature.searchLocation(): " + ex.Message);
                throw ex;
            }
        }

        public void MoveRnd()
        {
            AbstractMap map = CurrentField;
            ExtPoint pt = SearchRndLocation(map, map.AreaRect);

            CheckTile(false);
            SetPos(pt.X, pt.Y);
            CheckTile(true);
        }

        public void MoveSpecialEffect(NWField field, int oldX, int oldY)
        {
            // TODO: checkit
            if (fEntry.Sign.Equals("Jagredin")) {
                BaseTile tile = field.GetTile(oldX, oldY);
                tile.Background = PlaceID.pid_Rubble;
                field.Normalize();
            }
        }

        public override void MoveTo(int newX, int newY)
        {
            AuxUtils.ExStub("msg:  rs_thisAreaIsMagicallyBounded (Alfheim's plane)");

            try {
                string msg = "";
                TrapResult et_res = GetEntrapRes(ref msg);
                switch (et_res) {
                    case TrapResult.Absent:
                        // dummy
                        break;

                    case TrapResult.EscapeOk:
                        if (IsPlayer) {
                            Space.ShowText(this, msg);
                        }
                        break;

                    case TrapResult.EscapeBreak:
                        if (IsPlayer) {
                            Space.ShowText(this, msg);
                        }
                        return;

                    case TrapResult.EscapeDeath:
                        Death(msg, null);
                        return;
                }

                int newLayer = LayerID;
                int fx = fField.X;
                int fy = fField.Y;
                int px = newX;
                int py = newY;

                NWField fld = CurrentField;
                MoveSpecialEffect(fld, PosX, PosY);

                bool gpChanged = false;
                GlobalPosition cpi = new GlobalPosition(fx, fy, px, py, gpChanged);
                cpi.CheckPos();
                fx = cpi.Fx;
                fy = cpi.Fy;
                px = cpi.Px;
                py = cpi.Py;
                gpChanged = cpi.GlobalChanged;

                if (!gpChanged) {
                    if (fld.EntryID == GlobalVars.Field_Bifrost && py == 0) {
                        newLayer = GlobalVars.Layer_Asgard;
                        fx = 0;
                        fy = 0;
                        px = 0;
                        py = 9;
                        gpChanged = true;
                        if (IsPlayer) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_BifrostCollapses));
                            Space.BifrostCollapsed = true;
                        }
                    } else {
                        Gate gate = fld.FindGate(px, py);
                        if (gate != null && gate.Kind == GateKind.Fix) {
                            NWTile tile = (NWTile)fld.GetTile(px, py);
                            int fgp = tile.ForeBase;
                            if (fgp == PlaceID.pid_Vortex || fgp == PlaceID.pid_VortexStrange) {
                                // TODO: What's the difference (Vortex vs VortexStrange)?
                                int num = RandomHelper.GetRandom(4);
                                switch (num) {
                                    case 0:
                                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouEnterThePortal));
                                        break;
                                    case 1:
                                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouDiveIntoVortex));
                                        break;
                                    case 2:
                                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouDiveIntoWhirlpool));
                                        break;
                                    case 3:
                                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouFallIntoWhirlpoolVortex));
                                        break;
                                }
                            }

                            newLayer = gate.TargetLayer;
                            fx = (int)gate.TargetField.X;
                            fy = (int)gate.TargetField.Y;
                            px = (int)gate.TargetPos.X;
                            py = (int)gate.TargetPos.Y;

                            if (px < 0 || py < 0) {
                                fld = Space.GetField(newLayer, fx, fy);
                                ExtPoint pt = SearchRndLocation(fld, fld.AreaRect);
                                px = pt.X;
                                py = pt.Y;
                            }
                            gpChanged = true;
                        }
                    }
                }

                fld = Space.GetField(newLayer, fx, fy);
                if (fld == null) {
                    return;
                }

                if (IsPlayer && fld.LandID == GlobalVars.Land_Bifrost && Space.BifrostCollapsed) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCannotReturnAcrossBifrost));
                    return;
                }

                if (CanMove(fld, px, py)) {
                    bool res = true;
                    NWCreature creature = (NWCreature)fld.FindCreature(px, py);
                    if (creature != null) {
                        if (!gpChanged && IsEnemy(creature)) {
                            AttackTo(AttackKind.Melee, creature, null, null);
                            res = false;
                        } else {
                            res = creature.Request(this, RequestKind.PlaceYield);
                        }
                    }

                    if (res) {
                        TransferTo(newLayer, fx, fy, px, py, fld.AreaRect, false, true);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.moveTo(): " + ex.Message);
            }
        }

        public virtual void MoveToDown()
        {
            MoveToGate(false);
        }

        public virtual void MoveToUp()
        {
            if (IsPlayer && LayerID == GlobalVars.Layer_MimerWell) {
                ExtPoint MimerWell = Space.MimerWellPos;
                TransferTo(GlobalVars.Layer_Midgard, 5, 2, MimerWell.X, MimerWell.Y, StaticData.MapArea, true, true);
            } else {
                MoveToGate(true);
            }
        }

        private void MoveToGate(bool aUp)
        {
            if (CheckMove(new Movements(Movements.mkWalk, Movements.mkFly))) {
                NWField fld = CurrentField;
                int px = PosX;
                int py = PosY;

                NWTile tile = (NWTile)fld.GetTile(px, py);
                int fg = tile.ForeBase;

                Gate gate = fld.FindGate(px, py);
                if (gate == null) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_HereNoStairwaysOrHoles));
                } else {
                    if (aUp) {
                        if (fg != PlaceID.pid_StairsUp && fg != PlaceID.pid_GStairsUp && fg != PlaceID.pid_HoleUp) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_CannotGoUp));
                            return;
                        }
                    } else {
                        bool isMimerWell = (fg == PlaceID.pid_Well && fld.LandID == GlobalVars.Land_MimerRealm);

                        if (fg != PlaceID.pid_StairsDown && fg != PlaceID.pid_GStairsDown && fg != PlaceID.pid_HoleDown && !isMimerWell) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_CannotGoDown));
                            return;
                        }
                    }

                    if (fg == PlaceID.pid_HoleUp && !HasMovement(Movements.mkFly) && fItems.FindByCLSID(GlobalVars.iid_LazlulRope) == null) {
                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_NoRope));
                    } else {
                        int newLayer = gate.TargetLayer;
                        int fx = (int)gate.TargetField.X;
                        int fy = (int)gate.TargetField.Y;
                        px = (int)gate.TargetPos.X;
                        py = (int)gate.TargetPos.Y;

                        fld = Space.GetField(newLayer, fx, fy);
                        if (fld == null) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_ThisPassageGoesNowhere));
                        } else {
                            TransferTo(newLayer, fx, fy, px, py, fld.AreaRect, true, true);
                        }
                    }
                }
            }
        }

        private void EnterSpace(NWField field, NWTile tile)
        {
            try {
                if (field.LandID != GlobalVars.Land_Bifrost) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouFallIntoSpace));
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_YourBodyWillBeLost));
                    Death(BaseLocale.GetStr(RS.rs_DiedInExpanseOfNowhere), null);
                    return;
                }

                Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouFallToNiflheim));
                TransferTo(GlobalVars.Layer_Niflheim, -1, -1, -1, -1, StaticData.MapArea, false, false);

                // my fantasy, not from original
                if (AuxUtils.Chance(10)) {
                    Death(BaseLocale.GetStr(RS.rs_FellToNiflheim), null);
                } else {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouLandGently));
                }

                AuxUtils.ExStub("todo: on go down in space -  rs_YouDescendThroughSpace");
            } catch (Exception ex) {
                Logger.Write("NWCreature.enterSpace(): " + ex.Message);
                throw ex;
            }
        }

        private void EnterTrap(NWField field, NWTile tile, int pFore)
        {
            try {
                int idx = GetTrapListIndex(pFore);
                bool entrapped = idx >= 0 && (!Movements.HasIntersect(StaticData.dbTraps[idx].EscMovements));
                if (entrapped) {
                    UseEffect(StaticData.dbPlaces[pFore].Effect, tile, InvokeMode.im_Use, null);
                    Space.DoEvent(EventID.event_Trap, this, null, tile);
                    if (StaticData.dbTraps[idx].Disposable) {
                        tile.Foreground = PlaceID.pid_Undefined;
                    } else {
                        tile.Trap_Discovered = true;
                    }
                } else {
                    string msg = BaseLocale.GetStr(StaticData.dbTraps[idx].EscapeMsgRS);
                    Space.ShowText(this, msg);
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.enterTrap(): " + ex.Message);
                throw ex;
            }
        }

        private void EnterFog(NWField field, NWTile tile)
        {
            try {
                if (tile.FogID == PlaceID.pid_Fog) {
                    InFog = true;

                    if (tile.FogAge > 0 && GetAbility(AbilityID.Resist_Acid) <= 0) {
                        ApplyDamage(RandomHelper.GetBoundedRnd(1, 40), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_YourBodyIsCoveredWithAcid));
                    }
                } else {
                    InFog = false;
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.enterFog(): " + ex.Message);
                throw ex;
            }
        }

        public virtual void EnterPlace(NWField field, NWTile tile)
        {
            try {
                int bg = tile.BackBase;
                int fg = tile.ForeBase;

                Effect ef = fEffects.FindEffectByID(EffectID.eid_Sail);
                if (ef != null && bg != PlaceID.pid_Water) {
                    fEffects.Remove(ef);
                    EffectsFactory.UnloadShip((Player)this);
                }

                if (bg == PlaceID.pid_Space && (!Movements.Contains(Movements.mkFly))) {
                    EnterSpace(field, tile);
                }

                if (field.IsTrap(PosX, PosY)) {
                    EnterTrap(field, tile, fg);
                }

                EnterFog(field, tile);
            } catch (Exception ex) {
                Logger.Write("NWCreature.enterPlace(): " + ex.Message);
                throw ex;
            }
        }

        public virtual void LeavePlace(NWField field, NWTile tile)
        {
        }

        public virtual void CheckTile(bool aHere)
        {
            try {
                NWField map = CurrentField;
                if (map == null) {
                    return;
                }

                NWTile tile = (NWTile)map.GetTile(PosX, PosY);
                if (tile == null) {
                    return;
                }

                if (aHere) {
                    tile.CreaturePtr = this;
                    EnterPlace(map, tile);
                } else {
                    LeavePlace(map, tile);
                    tile.CreaturePtr = null;
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.checkTile(): " + ex.Message);
                throw ex;
            }
        }

        public void ProwlingBegin(EffectID effectID)
        {
            try {
                if (IsPlayer) {
                    ((Player)this).ClearMercenaries();
                }

                if (effectID == EffectID.eid_Lycanthropy) {
                    DropAll();
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_Lycanthropy_Beg));
                } else {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_Insanity_Beg));
                }

                var memStream = new MemoryStream();
                SaveToStream(new BinaryWriter(memStream), NWGameSpace.RGF_Version);
                ProwlImage = memStream.ToArray();

                fProwlSource = effectID;
                Prowling = true;
                if (IsPlayer) {
                    Space.TurnState = TurnState.Skip;
                }

                if (effectID == EffectID.eid_Lycanthropy) {
                    InitEx(GlobalVars.cid_Werewolf, true, false);
                    Alignment = Alignment.am_Chaotic_Evil;
                } else {
                    InitBrain();
                    Alignment = Alignment.am_Chaotic_Neutral;
                }

                AddEffect(EffectID.eid_Prowling, ItemState.is_Normal, EffectAction.ea_Persistent, true, "");
            } catch (IOException ex) {
                Logger.Write("NWCreature.ProwlingBegin.io(): " + ex.Message);
            } catch (Exception ex) {
                Logger.Write("NWCreature.ProwlingBegin(): " + ex.Message);
                throw ex;
            }
        }

        public void ProwlingEnd()
        {
            try {
                if (IsPlayer) {
                    if (fProwlSource == EffectID.eid_Lycanthropy) {
                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_RestoreForm));
                        // rs_Lycanthropy_End is deprecated
                    } else {
                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_Insanity_End));
                    }
                    Space.TurnState = TurnState.Done;
                }
                Prowling = false;
                fProwlSource = EffectID.eid_None;

                int lid = LayerID;
                int fx = fField.X;
                int fy = fField.Y;
                ExtPoint p = Location;

                var memStream = new MemoryStream(ProwlImage);
                LoadFromStream(new BinaryReader(memStream), NWGameSpace.RGF_Version);
                TransferTo(lid, fx, fy, p.X, p.Y, StaticData.MapArea, true, false);
                ProwlImage = null;
            } catch (IOException ex) {
                Logger.Write("NWCreature.ProwlingEnd.io(): " + ex.Message);
            } catch (Exception ex) {
                Logger.Write("NWCreature.ProwlingEnd(): " + ex.Message);
                throw ex;
            }
        }

        public Reaction GetReaction(NWCreature aCreature)
        {
            Reaction result = Reaction.Neutral;

            Alignment a1 = Alignment;
            int lc1 = a1.GetLC();

            Alignment a2 = aCreature.Alignment;
            int lc2 = a2.GetLC();

            if (lc1 == AlignmentEx.am_Mask_Lawful || lc1 == AlignmentEx.am_Mask_LCNeutral) {
                int ge = a1.GetGE();
                int ge2 = a2.GetGE();
                result = Relations[ge - 1, ge2 - 1];
            } else if (lc1 == AlignmentEx.am_Mask_Evil) {
                result = (Reaction)(RandomHelper.GetRandom((int)Reaction.Ally + 1));
            }

            return result;
        }

        public bool Request(NWCreature aSender, RequestKind aKind)
        {
            bool result = false;
            if (IsPlayer) {
                return result;
            }

            if (aKind == RequestKind.PlaceYield) {
                if (!fEntry.Flags.Contains(CreatureFlags.esPlant)) {
                    NWField fld = CurrentField;
                    int nX;
                    int nY;

                    for (int dir = Directions.DtFlatFirst; dir <= Directions.DtFlatLast; dir++) {
                        nX = PosX + Directions.Data[dir].DX;
                        nY = PosY + Directions.Data[dir].DY;

                        if (fld.IsValid(nX, nY) && CanMove(fld, nX, nY) && fld.FindCreature(nX, nY) == null) {
                            MoveTo(nX, nY);
                            result = true;
                            break;
                        }
                    }
                }
            }

            return result;
        }

        public override void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            try {
                base.LoadFromStream(stream, version);

                Sex = (CreatureSex)(StreamUtils.ReadByte(stream));
                fState = (CreatureState)(StreamUtils.ReadByte(stream));
                Alignment = (Alignment)(StreamUtils.ReadByte(stream));
                fIsTrader = StreamUtils.ReadBoolean(stream);
                fName = StreamUtils.ReadString(stream, StaticData.DefEncoding);
                Turn = StreamUtils.ReadInt(stream);
                LayerID = StreamUtils.ReadInt(stream);
                fField.X = StreamUtils.ReadInt(stream);
                fField.Y = StreamUtils.ReadInt(stream);
                Level = StreamUtils.ReadInt(stream);
                fExperience = StreamUtils.ReadInt(stream);
                Strength = StreamUtils.ReadInt(stream);
                fSpeed = StreamUtils.ReadInt(stream);
                Attacks = StreamUtils.ReadInt(stream);
                ToHit = StreamUtils.ReadInt(stream);
                Luck = StreamUtils.ReadInt(stream);
                Constitution = StreamUtils.ReadInt(stream);
                ArmorClass = StreamUtils.ReadInt(stream);
                fDamageBase = StreamUtils.ReadInt(stream);
                HPMax_Renamed = StreamUtils.ReadInt(stream);
                HPCur = StreamUtils.ReadInt(stream);
                MPMax = StreamUtils.ReadInt(stream);
                MPCur = StreamUtils.ReadInt(stream);
                DBMin = StreamUtils.ReadInt(stream);
                DBMax = StreamUtils.ReadInt(stream);

                fEffects.LoadFromStream(stream, version);
                int num = fEffects.Count;
                for (int i = 0; i < num; i++) {
                    fEffects[i].Owner = this;
                }

                fItems.LoadFromStream(stream, version);
                int num2 = fItems.Count;
                for (int i = 0; i < num2; i++) {
                    fItems[i].Owner = this;
                }

                fAbilities.LoadFromStream(stream, version);
                fSkills.LoadFromStream(stream, version);

                fIsMercenary = StreamUtils.ReadBoolean(stream);
                Perception = StreamUtils.ReadByte(stream);
                Dexterity = (ushort)StreamUtils.ReadWord(stream);
                fSurvey = StreamUtils.ReadByte(stream);
                Hear = StreamUtils.ReadByte(stream);
                Smell = StreamUtils.ReadByte(stream);
                LastDir = (StreamUtils.ReadByte(stream));

                InitBody();
                InitBrain();

                NWBrainEntity brain = (NWBrainEntity)fBrain;
                if (brain != null) {
                    brain.LoadFromStream(stream, version);
                }

                if (fBody != null) {
                    ((CustomBody)fBody).LoadFromStream(stream, version);
                }

                // FIXME: bad code
                if (fIsMercenary) {
                    ResetMercenary(fIsMercenary);
                }

                if (fState != CreatureState.Dead) {
                    CheckTile(true);
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.loadFromStream(): " + ex.Message);
                Logger.Write("NWCreature.avail: " + Convert.ToString((stream.BaseStream.Length - stream.BaseStream.Position)));
                throw ex;
            }
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            try {
                base.SaveToStream(stream, version);

                StreamUtils.WriteByte(stream, (byte)Sex);
                StreamUtils.WriteByte(stream, (byte)fState);
                StreamUtils.WriteByte(stream, (byte)(Alignment));
                StreamUtils.WriteBoolean(stream, fIsTrader);
                StreamUtils.WriteString(stream, fName, StaticData.DefEncoding);
                StreamUtils.WriteInt(stream, Turn);
                StreamUtils.WriteInt(stream, LayerID);
                StreamUtils.WriteInt(stream, fField.X);
                StreamUtils.WriteInt(stream, fField.Y);
                StreamUtils.WriteInt(stream, Level);
                StreamUtils.WriteInt(stream, fExperience);
                StreamUtils.WriteInt(stream, Strength);
                StreamUtils.WriteInt(stream, fSpeed);
                StreamUtils.WriteInt(stream, Attacks);
                StreamUtils.WriteInt(stream, ToHit);
                StreamUtils.WriteInt(stream, Luck);
                StreamUtils.WriteInt(stream, Constitution);
                StreamUtils.WriteInt(stream, ArmorClass);
                StreamUtils.WriteInt(stream, fDamageBase);
                StreamUtils.WriteInt(stream, HPMax_Renamed);
                StreamUtils.WriteInt(stream, HPCur);
                StreamUtils.WriteInt(stream, MPMax);
                StreamUtils.WriteInt(stream, MPCur);
                StreamUtils.WriteInt(stream, DBMin);
                StreamUtils.WriteInt(stream, DBMax);

                fEffects.SaveToStream(stream, version);
                fItems.SaveToStream(stream, version);
                fAbilities.SaveToStream(stream, version);
                fSkills.SaveToStream(stream, version);

                StreamUtils.WriteBoolean(stream, fIsMercenary);
                StreamUtils.WriteByte(stream, Perception);
                StreamUtils.WriteWord(stream, Dexterity);
                StreamUtils.WriteByte(stream, fSurvey);
                StreamUtils.WriteByte(stream, Hear);
                StreamUtils.WriteByte(stream, Smell);
                StreamUtils.WriteByte(stream, (byte)(LastDir));

                NWBrainEntity brain = (NWBrainEntity)fBrain;
                if (brain != null) {
                    brain.SaveToStream(stream, version);
                }

                if (fBody != null) {
                    ((CustomBody)fBody).SaveToStream(stream, version);
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.saveToStream(): " + ex.Message);
                throw ex;
            }
        }

        public override void SetPos(int posX, int posY)
        {
            try {
                LastDir = Directions.GetDirByCoords(PosX, PosY, posX, posY);
                base.SetPos(posX, posY);
            } catch (Exception ex) {
                Logger.Write("NWCreature.setPos(): " + ex.Message);
                throw ex;
            }
        }

        public void CopyTotalLocation(NWCreature other)
        {
            LayerID = other.LayerID;
            fField = other.Field.Clone();
            SetPos(other.PosX, other.PosY);
        }

        public void ShootTo(NWCreature enemy, Item weapon)
        {
            try {
                Item projectile = null;
                ItemFlags wks = weapon.Flags;
                AttackKind kind = AttackKind.Melee;

                if (wks.Contains(ItemFlags.if_ShootWeapon)) {
                    kind = AttackKind.Shoot;
                    projectile = GetItemByEquipmentKind(BodypartType.bp_Back);
                } else {
                    if (wks.Contains(ItemFlags.if_ThrowWeapon)) {
                        kind = AttackKind.Throw;
                        projectile = weapon;
                    }
                }

                if (projectile != null) {
                    int range = GetProjectileRange(projectile);
                    ShootToInternal(kind, enemy.PosX, enemy.PosY, range, weapon, projectile);
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.shootTo(): " + ex.Message);
                throw ex;
            }
        }

        public void ShootToDir(int dir)
        {
            try {
                if (InWater) {
                    if (IsPlayer) {
                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouCannotThrowUnderwater));
                    }
                    return;
                }

                Item weapon = GetItemByEquipmentKind(BodypartType.bp_RHand);
                ItemFlags wfs = (weapon != null) ? weapon.Flags : new ItemFlags();

                AttackKind kind;
                Item projectile;

                if (weapon != null) {
                    if (wfs.Contains(ItemFlags.if_ShootWeapon)) {
                        /*if (!this.isPlayer()) {
                         throw new RuntimeException("not implemented");
                         }*/
                        kind = AttackKind.Shoot;

                        projectile = GetItemByEquipmentKind(BodypartType.bp_Back);
                        if (projectile == null) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_NoAmmo));
                            return;
                        } else {
                            wfs = projectile.Flags;
                            if (!wfs.Contains(ItemFlags.if_Projectile)) {
                                Space.ShowText(this, BaseLocale.GetStr(RS.rs_NoAmmo));
                                return;
                            }
                        }
                    } else if (wfs.Contains(ItemFlags.if_ThrowWeapon)) {
                        kind = AttackKind.Throw;
                        projectile = weapon;
                    } else {
                        Space.ShowText(this, BaseLocale.GetStr(RS.rs_WeaponNotChoosed));
                        return;
                    }
                } else {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_WeaponNotChoosed));
                    return;
                }

                int range = GetProjectileRange(projectile);
                int tx = PosX + Directions.Data[dir].DX * range;
                int ty = PosY + Directions.Data[dir].DY * range;
                if (!ShootToInternal(kind, tx, ty, range, weapon, projectile)) {
                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_CannotShoot));
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.shootToDir(): " + ex.Message);
                throw ex;
            }
        }

        private bool ShootToInternal(AttackKind attackKind, int tX, int tY, int range, Item weapon, Item projectile)
        {
            bool result = false;

            try {
                NWField fld = CurrentField;

                ItemFlags pfs = projectile.Flags;
                if (pfs.HasIntersect(ItemFlags.if_ThrowWeapon, ItemFlags.if_Projectile)) {
                    if (projectile.Count > 1) {
                        projectile = projectile.GetSeveral(1);
                        fld.Items.Add(projectile, false);
                    } else {
                        RemoveItem(projectile);

                        if (!projectile.InUse) {
                            DropItem(projectile);
                        } else {
                            projectile = null;
                        }
                    }

                    if (projectile != null) {
                        Projectile projObj = new Projectile(this, fld, attackKind, range, weapon, projectile);
                        projObj.Run(tX, tY);

                        result = true;
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWCreature.shootToInternal(): " + ex.Message);
                throw ex;
            }

            return result;
        }

        public void ResetStamina()
        {
            Stamina += Speed;
        }

        public void SwitchBody(NWCreature target)
        {
            base.CLSID = target.CLSID;
            State = target.State;
            Alignment = target.Alignment;
            Sex = target.Sex;
            Level = target.Level;
            fExperience = target.fExperience;
            Strength = target.Strength;
            fSpeed = target.fSpeed;
            Attacks = target.Attacks;
            ToHit = target.ToHit;
            Luck = target.Luck;
            Constitution = target.Constitution;
            Dexterity = target.Dexterity;
            fSurvey = target.fSurvey;
            Hear = target.Hear;
            Smell = target.Smell;
            ArmorClass = target.ArmorClass;
            HPMax_Renamed = target.HPMax_Renamed;
            HPCur = target.HPCur;
            DBMin = target.DBMin;
            DBMax = target.DBMax;
            Perception = target.Perception;
            fAbilities.Assign(target.fAbilities, AttributeList.Lao_Or);
            fSkills.Assign(target.fSkills, AttributeList.Lao_Or);
            DropAll();
            Items.Assign(target.Items);
            PrepareItems();
        }

        public virtual void TransferTo(int layerID, int fX, int fY, int pX, int pY, ExtRect area, bool obligatory, bool controlled)
        {
            try {
                if (fX < 0 || fY < 0 || pX < 0 || pY < 0) {
                    RandomHelper.Randomize();

                    NWLayer lr = Space.GetLayerByID(layerID);
                    int tries = 10;
                    bool res;
                    do {
                        if (fX < 0 || fY < 0) {
                            fX = RandomHelper.GetRandom(lr.W);
                            fY = RandomHelper.GetRandom(lr.H);
                        }

                        if (pX < 0 || pY < 0) {
                            NWField fld = lr.GetField(fX, fY);
                            ExtPoint pos = SearchRndLocation(fld, area);
                            res = (!pos.IsEmpty);
                            if (res) {
                                pX = pos.X;
                                pY = pos.Y;
                            }
                        } else {
                            res = true;
                        }
                        tries--;
                    } while (!res && tries > 0);
                }

                Space.DoEvent(EventID.event_Move, this, null, this);

                CheckTile(false);
                SetGlobalPos(layerID, fX, fY, obligatory);
                SetPos(pX, pY);
                CheckTile(true);
            } catch (Exception ex) {
                Logger.Write("NWCreature.transferTo(): " + ex.Message);
                throw ex;
            }
        }

        public virtual void UseEffect(EffectID effectID, object source, InvokeMode invokeMode, EffectExt ext)
        {
            Effect.InvokeEffect(effectID, this, source, invokeMode, EffectAction.ea_Instant, ext);
        }

        public void UseItem(Item item, EffectExt extData)
        {
            bool fin_remove = false;

            ItemsList itemContainer = item.ItemContainer;
            Space.DoEvent(EventID.event_ItemUse, this, null, item);
            switch (item.Kind) {
                case ItemKind.ik_DeadBody:
                case ItemKind.ik_Food:
                    {
                        if (IsPlayer) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouEat) + item.GetDeclinableName(Number.nSingle, Case.cAccusative, Blindness) + ".");
                            ((Player)this).Satiety = (short)(((Player)this).Satiety + item.Satiety);

                            UseEffect(EffectID.eid_FoodEat, item, InvokeMode.im_Use, null);

                            if (item.Kind == ItemKind.ik_DeadBody) {
                                NWCreature dead = (NWCreature)item.Contents[0];
                                if (dead.Turn > -5) {
                                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_MeatWasOld));
                                } else {
                                    Space.ShowText(this, BaseLocale.Format(RS.rs_Unsavoury, new object[]{ dead.GetDeclinableName(Number.nSingle, Case.cGenitive) }));
                                }
                                bool cannibalism = (fEntry.Race == RaceID.crDefault && CLSID == dead.CLSID) || fEntry.Race == dead.fEntry.Race;
                                if (cannibalism) {
                                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_CannibalismIsImmoral));
                                    ((Player)this).Morality = (sbyte)((int)((Player)this).Morality - 25);
                                }
                            }
                        }
                        item.ApplyEffects(this, InvokeMode.im_Use, extData);
                        fin_remove = true;
                        break;
                    }
                case ItemKind.ik_Potion:
                case ItemKind.ik_Scroll:
                    {
                        if (IsPlayer) {
                            if (item.Entry.ItmKind == ItemKind.ik_Scroll) {
                                Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouRead) + item.GetDeclinableName(Number.nSingle, Case.cAccusative, Blindness) + ".");
                                Space.ShowText(this, BaseLocale.GetStr(RS.rs_ScrollDisappears));
                                if (Confused) {
                                    Space.ShowText(this, BaseLocale.GetStr(RS.rs_GarbleWords));
                                }
                            }
                            if (item.Entry.ItmKind == ItemKind.ik_Potion) {
                                Space.ShowText(this, BaseLocale.GetStr(RS.rs_YouDrink) + item.GetDeclinableName(Number.nSingle, Case.cAccusative, Blindness) + ".");
                            }
                        }
                        item.ApplyEffects(this, InvokeMode.im_Use, extData);
                        fin_remove = true;
                        if (item.Entry.ItmKind == ItemKind.ik_Potion) {
                            Item.GenItem(this, GlobalVars.iid_Vial, 1, true);
                        }
                        break;
                    }
                case ItemKind.ik_Tool:
                case ItemKind.ik_MusicalTool:
                    item.ApplyEffects(this, InvokeMode.im_Use, extData);
                    break;

                case ItemKind.ik_Wand:
                    {
                        if (InWater) {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_ZappingNotWorkUnderwater));
                            return;
                        }
                        if (item.Bonus > 0) {
                            item.ApplyEffects(this, InvokeMode.im_Use, extData);
                            item.Bonus--;
                        } else {
                            Space.ShowText(this, BaseLocale.GetStr(RS.rs_NoCharges));
                            fin_remove = true;
                        }
                        break;
                    }
                case ItemKind.ik_Misc:
                    // dummy
                    break;
            }

            CheckActionAbility(CreatureAction.caItemUse, item);

            if (itemContainer.IndexOf(item) >= 0) {
                if (!item.Identified) {
                    item.Identified = true;
                }
                if (fin_remove) {
                    if (item.Countable && item.Count > 0) {
                        item.Count = (ushort)(item.Count - 1);
                    }
                    if (!item.Countable || (item.Countable && item.Count == 0)) {
                        itemContainer.Remove(item);
                    }
                }
            }
        }

        public void UseSkill(SkillID skill, EffectExt aExt)
        {
            try {
                SkillRec skRec = StaticData.dbSkills[(int)skill];
                UseEffect(skRec.Effect, null, InvokeMode.im_ItSelf, aExt);
            } catch (Exception ex) {
                Logger.Write("NWCreature.useSkill(): " + ex.Message);
            }
        }

        public void UseSpell(EffectID spellEffect, EffectExt ext)
        {
            int eid = (int)spellEffect;
            EffectRec efRec = EffectsData.dbEffects[eid];
            if (MPCur < (int)efRec.MPReq) {
                Space.ShowText(this, BaseLocale.GetStr(RS.rs_NoEnergyToSpellcast));
            } else {
                MPCur -= (int)efRec.MPReq;
                UseEffect(spellEffect, null, InvokeMode.im_ItSelf, ext);
            }
        }

        public int GetAttackExp(NWCreature enemy)
        {
            float div = (Level + enemy.Level);
            float dLevel = (div == 0.0f) ? 0 : (enemy.Level / div);

            if (dLevel == 0f) {
                dLevel = 1f;
            }

            int result = (int)(Math.Round((enemy.HPMax_Renamed * (double)dLevel)));
            if (result <= 0) {
                result = 1;
            }
            return result;
        }

        public float GetAttackRate(NWCreature enemy, int kinsfolks)
        {
            float result;
            try {
                float s_daf = (((DBMin + DBMax) / 2.0f * Attacks));
                float t_daf = (((enemy.DBMin + enemy.DBMax) / 2.0f * enemy.Attacks));
                float s_af;
                if (s_daf == 0f) {
                    s_af = 1f;
                } else {
                    s_af = ((enemy.HPCur / (float)s_daf));
                }
                float t_af;
                if (t_daf == 0f) {
                    t_af = 1f;
                } else {
                    t_af = ((HPCur / (float)t_daf));
                }
                float kf = ((1.0f / ((float)kinsfolks * 0.6f + 1.0f)));
                result = ((s_af / (s_af + t_af) / kf));
            } catch (Exception ex) {
                result = 0.5f;
                Logger.Write("getAttackRate(): " + ex.Message);
            }
            return result;
        }

        /// <summary>
        /// Check availability of the Item from the signature.
        /// <b>Need for scripting</b>. </summary>
        /// <param name="sign"> sign of item </param>
        /// <returns> availability </returns>
        public bool HasItem(string sign)
        {
            return (FindItem(sign) != null);
        }

        /// <summary>
        /// Return the item from the signature.
        /// <b>Need for scripting</b>. </summary>
        /// <param name="sign"> sign of item
        /// @return </param>
        public Item FindItem(string sign)
        {
            for (int i = 0; i < fItems.Count; i++) {
                Item item = fItems[i];
                if (item.Entry.Sign.Equals(sign)) {
                    return item;
                }
            }
            return null;
        }

        /// 
        /// <summary>
        /// <b>Need for scripting</b>. </summary>
        /// <param name="acceptor"> </param>
        /// <param name="sign">
        /// @return </param>
        public bool TransferItem(NWCreature acceptor, string sign)
        {
            Item item = FindItem(sign);
            if (item != null) {
                item = ((Item)Items.Extract(item));
                acceptor.AddItem(item);
                return true;
            } else {
                return false;
            }
        }

        /// 
        /// <summary>
        /// <b>Need for scripting</b>.
        /// @return
        /// </summary>
        public bool SacrificeVictim()
        {
            Player player = Space.Player;

            Space.SetVolatileState(CLSID, VolatileState.Destroyed);
            Death(BaseLocale.Format(RS.rs_VictimKill, new object[]{ Name }), null);

            VolatileState a = Space.GetVolatileState(GlobalVars.cid_Agnar);
            VolatileState h = Space.GetVolatileState(GlobalVars.cid_Haddingr);
            VolatileState k = Space.GetVolatileState(GlobalVars.cid_Ketill);
            if (a == VolatileState.Destroyed && h == VolatileState.Destroyed && k == VolatileState.Destroyed) {
                Space.AddItemEx(GlobalVars.Layer_Svartalfheim3, 1, 2, player.PosX, player.PosY, GlobalVars.iid_Gungnir);
            }

            return true;
        }

        /// 
        /// <summary>
        /// <b>Need for scripting</b>.
        /// @return
        /// </summary>
        public bool FreeVictim()
        {
            Player player = Space.Player;

            InitEx(GlobalVars.cid_Norseman, true, false);
            player.RecruitMercenary(null, this, false);

            if (Space.GetVolatileState(GlobalVars.cid_Vidur) == VolatileState.None) {
                ExtPoint pt = player.GetNearestPlace(5, true);
                Space.AddCreatureEx(GlobalVars.Layer_Svartalfheim3, 1, 2, pt.X, pt.Y, GlobalVars.cid_Vidur);
                Space.ShowText(player, BaseLocale.GetStr(RS.rs_Vidur_IsAngered));
            }

            return true;
        }
    }
}
