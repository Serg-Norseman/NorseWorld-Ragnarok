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
using System.IO;
using BSLib;
using NWR.Creatures;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.Game.Types;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Grammar;

namespace NWR.Items
{
    public class Item : LocatedEntity
    {
        private EntityList<LocatedEntity> fContents;
        private ItemEntry fEntry;
        private bool fIdentified;
        private bool fInUse;
        private float fWeight;

        public int Bonus;
        public ushort Count;
        public byte Frame;
        public ItemState State;

        public Item(GameSpace space, object owner)
            : base(space, owner)
        {
            Frame = 0;
            State = ItemState.is_Normal;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                if (fContents != null) {
                    fContents.Dispose();
                }
            }
            base.Dispose(disposing);
        }

        public new NWGameSpace Space
        {
            get {
                return (NWGameSpace)fSpace;
            }
        }

        public ItemEntry Entry
        {
            get {
                return fEntry;
            }
        }

        public bool Identified
        {
            get {
                return fIdentified;
            }
            set {
                if (fIdentified != value) {
                    fIdentified = value;
                    if (fIdentified && Owner != null && Owner is NWCreature) {
                        ((NWCreature)Owner).KnowIt(CLSID);
                    }
                }
            }
        }

        public bool InUse
        {
            get {
                return fInUse;
            }
            set {
                if (fInUse != value) {
                    if (!value && State == ItemState.is_Cursed) {
                        Space.ShowText(Owner, BaseLocale.GetStr(RS.rs_HexedItemsDoNotRemove));
                    } else {
                        fInUse = value;
                        if (Owner != null && Owner is NWCreature) {
                            NWCreature creature = (NWCreature)Owner;
                            if (fInUse) {
                                creature.ArmorClass += GetAttribute(ItemAttribute.ia_Defense);
                                creature.DBMin += GetAttribute(ItemAttribute.ia_DamageMin);
                                creature.DBMax += GetAttribute(ItemAttribute.ia_DamageMax);
                                creature.Strength += GetAttribute(ItemAttribute.ia_Mdf_Str);
                                creature.Luck += GetAttribute(ItemAttribute.ia_Mdf_Luck);
                                creature.SpeedMod += GetAttribute(ItemAttribute.ia_Mdf_Speed);
                                creature.Attacks += GetAttribute(ItemAttribute.ia_Mdf_Attacks);
                                creature.ToHit += GetAttribute(ItemAttribute.ia_Mdf_ToHit);
                                ApplyEffects(creature, InvokeMode.im_UseBegin, null);
                            } else {
                                creature.ArmorClass -= GetAttribute(ItemAttribute.ia_Defense);
                                creature.DBMin -= GetAttribute(ItemAttribute.ia_DamageMin);
                                creature.DBMax -= GetAttribute(ItemAttribute.ia_DamageMax);
                                creature.Strength -= GetAttribute(ItemAttribute.ia_Mdf_Str);
                                creature.Luck -= GetAttribute(ItemAttribute.ia_Mdf_Luck);
                                creature.SpeedMod -= GetAttribute(ItemAttribute.ia_Mdf_Speed);
                                creature.Attacks -= GetAttribute(ItemAttribute.ia_Mdf_Attacks);
                                creature.ToHit -= GetAttribute(ItemAttribute.ia_Mdf_ToHit);
                                ApplyEffects(creature, InvokeMode.im_UseEnd, null);
                            }
                        }
                    }
                }
            }
        }

        /// 
        /// <param name="index"> Value of ItemAttribute enum
        /// @return </param>
        public int GetAttribute(int index)
        {
            int result = fEntry.Attributes[index - 1];
            if (index == ItemAttribute.ia_Defense) {
                if (DefenceBonus) {
                    result += Bonus;
                }
                result++;
            }
            return result;
        }

        public EntityList<LocatedEntity> Contents
        {
            get {
                EntityList<LocatedEntity> result;
                if (!Container) {
                    result = null;
                } else {
                    if (fContents == null) {
                        fContents = new EntityList<LocatedEntity>(null);
                    }
                    result = fContents;
                }
                return result;
            }
        }

        public BodypartType EquipmentKind
        {
            get {
                return fEntry.EqKind;
            }
        }

        public int ImageIndex
        {
            get {
                int Result = fEntry.ImageIndex;
                if (CLSID == GlobalVars.iid_Torch && !InUse) {
                    Result++;
                }
                if (Frame > 0 && Frame <= fEntry.FramesLoaded) {
                    Result += (int)Frame;
                }
                return Result;
            }
        }

        public bool Unique
        {
            get {
                return fEntry.Flags.Contains(ItemFlags.if_IsUnique);
            }
        }

        public bool Container
        {
            get {
                return CLSID == GlobalVars.iid_SoulTrapping_Ring || fEntry.Flags.Contains(ItemFlags.if_IsContainer);
            }
        }

        public bool Equipment
        {
            get {
                return EquipmentKind != BodypartType.bp_None;
            }
        }

        public bool TwoHanded
        {
            get {
                return fEntry.Flags.Contains(ItemFlags.if_TwoHanded);
            }
        }

        public bool Ingredient
        {
            get {
                return fEntry.Flags.Contains(ItemFlags.if_Ingredient);
            }
        }

        public ItemKind Kind
        {
            get {
                ItemKind result;
                if (fEntry != null) {
                    result = fEntry.ItmKind;
                } else {
                    result = ItemKind.ik_Misc;
                }
                return result;
            }
        }

        public SymbolID Symbol
        {
            get {
                SymbolID Result = SymbolID.sid_Tool;
                if (fEntry == null) {
                    return Result;
                }
    
                switch (fEntry.ItmKind) {
                    case ItemKind.ik_Armor:
                    case ItemKind.ik_Shield:
                    case ItemKind.ik_Helmet:
                    case ItemKind.ik_Clothing:
                    case ItemKind.ik_HeavyArmor:
                    case ItemKind.ik_MediumArmor:
                    case ItemKind.ik_LightArmor:
                        Result = SymbolID.sid_Armor;
                        break;
    
                    case ItemKind.ik_DeadBody:
                        Result = SymbolID.sid_DeadBody;
                        break;
    
                    case ItemKind.ik_Food:
                        Result = SymbolID.sid_Food;
                        break;
    
                    case ItemKind.ik_Potion:
                        Result = SymbolID.sid_Potion;
                        break;
    
                    case ItemKind.ik_Ring:
                        Result = SymbolID.sid_Ring;
                        break;
    
                    case ItemKind.ik_Tool:
                        Result = SymbolID.sid_Tool;
                        break;
    
                    case ItemKind.ik_Wand:
                        Result = SymbolID.sid_Wand;
                        break;
    
                    case ItemKind.ik_BluntWeapon:
                    case ItemKind.ik_ShortBlade:
                    case ItemKind.ik_LongBlade:
                    case ItemKind.ik_Spear:
                    case ItemKind.ik_Axe:
                    case ItemKind.ik_Bow:
                    case ItemKind.ik_CrossBow:
                    case ItemKind.ik_Projectile:
                        Result = SymbolID.sid_Weapon;
                        break;
    
                    case ItemKind.ik_Scroll:
                        Result = SymbolID.sid_Scroll;
                        break;
    
                    case ItemKind.ik_Coin:
                        Result = SymbolID.sid_Coin;
                        break;
    
                    case ItemKind.ik_Amulet:
                        Result = SymbolID.sid_Amulet;
                        break;
    
                    case ItemKind.ik_Misc:
                        Result = SymbolID.sid_Tool;
                        break;
                }
    
                return Result;
            }
        }

        public void SetAttribute(int index, int value)
        {
        }

        public NWCreature Holder
        {
            get {
                if (Owner != null && Owner is NWCreature) {
                    return (NWCreature)Owner;
                } else {
                    return null;
                }
            }
        }



        public short Satiety
        {
            get {
                short result;
                if (CLSID == GlobalVars.iid_DeadBody) {
                    result = ((NWCreature)fContents[0]).Entry.FleshSatiety;
                } else {
                    result = fEntry.Satiety;
                }
                return result;
            }
        }

        private string GetCountableName(int count)
        {
            string num = Convert.ToString(count);

            Case c = Case.cUndefined;
            Number i = Number.nSingle;

            char c2 = num[num.Length - 1];

            if (c2 == '1') {
                c = Case.cNominative;
                i = Number.nSingle;
            } else if ("234".IndexOf(c2) >= 0) {
                c = Case.cGenitive;
                i = Number.nSingle;
            } else if ("567890".IndexOf(c2) >= 0) {
                c = Case.cGenitive;
                i = Number.nPlural;
            }

            if (num.Length >= 2) {
                if (num[num.Length - 2] == '1') {
                    c = Case.cGenitive;
                    i = Number.nPlural;
                }
            }

            string result = fEntry.GetNounDeclension(i, c);
            if (count > 1) {
                result = num + " " + result;
            }
            return result;
        }

        private static int CompareInternal(int val1, int val2)
        {
            int res = val2 - val1;
            if (res < 0) {
                res = -1;
            } else {
                if (res > 0) {
                    res = 1;
                }
            }
            return res;
        }

        public bool Countable
        {
            get {
                return fEntry.Countable;
            }
        }

        public int Price
        {
            get {
                int result = fEntry.Price;
    
                if (fEntry.ItmKind == ItemKind.ik_Wand) {
                    result = (1 + result * Bonus);
                }
    
                if (fIdentified) {
                    if (State != ItemState.is_Blessed) {
                        if (State == ItemState.is_Cursed) {
                            result = (int)Math.Round((result * 0.75));
                        }
                    } else {
                        result = (int)Math.Round((result * 1.25));
                    }
                }
                if (result < 1) {
                    result = 1;
                }
                result = (int)(result * Count);
    
                return result;
            }
        }

        public override byte SerializeKind
        {
            get {
                return StaticData.SID_ITEM;
            }
        }

        public float Weight
        {
            get {
                float result;
                if (CLSID == GlobalVars.iid_DeadBody) {
                    result = ((NWCreature)fContents[0]).Weight;
                } else {
                    result = (((float)fWeight * Count));
                }
                return result;
            }
            set {
                fWeight = value;
            }
        }

        public override int CLSID
        {
            set {
                base.CLSID = value;
                fEntry = ((ItemEntry)GlobalVars.nwrDB.GetEntry(value));
    
                fWeight = fEntry.Weight;
                if (fEntry.Sign.CompareTo("Amulet_Holding") == 0) {
                    State = ItemState.is_Cursed;
                }
                switch (Kind) {
                    case ItemKind.ik_Armor:
                    case ItemKind.ik_Tool:
                    case ItemKind.ik_Wand:
                    case ItemKind.ik_BluntWeapon:
                    case ItemKind.ik_Amulet:
                    case ItemKind.ik_ShortBlade:
                    case ItemKind.ik_LongBlade:
                    case ItemKind.ik_Shield:
                    case ItemKind.ik_Helmet:
                    case ItemKind.ik_Clothing:
                    case ItemKind.ik_Spear:
                    case ItemKind.ik_Axe:
                    case ItemKind.ik_Bow:
                    case ItemKind.ik_CrossBow:
                    case ItemKind.ik_HeavyArmor:
                    case ItemKind.ik_MediumArmor:
                    case ItemKind.ik_LightArmor:
                    case ItemKind.ik_Projectile:
                    case ItemKind.ik_Misc:
                        {
                            Bonus = GenBonus();
                            break;
                        }
                    case ItemKind.ik_DeadBody:
                    case ItemKind.ik_Food:
                    case ItemKind.ik_Potion:
                    case ItemKind.ik_Scroll:
                    case ItemKind.ik_Coin:
                        {
                            Bonus = 0;
                            break;
                        }
                    case ItemKind.ik_Ring:
                        {
                            if (value == GlobalVars.iid_Ring_Delusion) {
                                Bonus = GlobalVars.dbRings[RandomHelper.GetRandom(GlobalVars.dbRings.Count)];
                            } else {
                                if (value == GlobalVars.iid_SoulTrapping_Ring) {
                                    Bonus = 0;
                                } else {
                                    Bonus = GenBonus();
                                }
                            }
                            break;
                        }
                }
            }
        }

        public void ApplyEffects(CreatureEntity target, InvokeMode invokeMode, EffectExt extData)
        {
            try {
                ItemEntry.EffectEntry[] effects = fEntry.Effects;
                if (effects == null) {
                    return;
                }

                int num = effects.Length;
                for (int i = 0; i < num; i++) {
                    ItemEntry.EffectEntry eff = fEntry.Effects[i];

                    EffectExt ext;
                    if (invokeMode < InvokeMode.im_UseBegin || invokeMode >= InvokeMode.im_ItSelf) {
                        ext = extData;
                    } else {
                        ext = new EffectExt();
                        ext.SetParam(EffectParams.ep_ItemExt, eff.ExtData);
                    }

                    ((NWCreature)target).UseEffect(eff.EffID, this, invokeMode, ext);
                }
            } catch (Exception ex) {
                Logger.Write("Item.applyEffects(): " + ex.Message);
            }
        }

        public override bool Assign(GameEntity item)
        {
            Item otherItem = (Item)item;
            bool result = (CLSID == item.CLSID) && Countable && (Bonus == otherItem.Bonus);

            if (result) {
                Count = (ushort)(Count + otherItem.Count);
            }

            return result;
        }

        private static int GetBreakageChance(bool bodyHit)
        {
            int result = 10;
            if (bodyHit) {
                result <<= 1;
            }
            if (result > 100) {
                result = 100;
            }
            return result;
        }

        public bool IsBreakage(bool bodyHit)
        {
            return AuxUtils.Chance(GetBreakageChance(bodyHit));
        }

        /// 
        /// <param name="otherItem"> </param>
        /// <returns> Value of ItemsCompareResult enumeration </returns>
        public int Compare(Item otherItem)
        {
            int result = ItemsCompareResult.NotComparable;

            if (Kind == otherItem.Kind && Identified && otherItem.Identified) {
                switch (Kind) {
                    case ItemKind.ik_Armor:
                    case ItemKind.ik_Shield:
                    case ItemKind.ik_Helmet:
                    case ItemKind.ik_Clothing:
                    case ItemKind.ik_HeavyArmor:
                    case ItemKind.ik_MediumArmor:
                    case ItemKind.ik_LightArmor:
                        {
                            result = CompareInternal(GetAttribute(ItemAttribute.ia_Defense), otherItem.GetAttribute(ItemAttribute.ia_Defense));
                            break;
                        }
                    case ItemKind.ik_Potion:
                    case ItemKind.ik_Ring:
                    case ItemKind.ik_Wand:
                    case ItemKind.ik_Scroll:
                    case ItemKind.ik_Amulet:
                        {
                            if (CLSID != otherItem.CLSID) {
                                result = ItemsCompareResult.NotComparable;
                            } else {
                                result = CompareInternal(Bonus + StaticData.dbItemStates[(int)State].CompareValue, otherItem.Bonus + StaticData.dbItemStates[(int)otherItem.State].CompareValue);
                            }
                            break;
                        }
                    case ItemKind.ik_BluntWeapon:
                    case ItemKind.ik_ShortBlade:
                    case ItemKind.ik_LongBlade:
                    case ItemKind.ik_Spear:
                    case ItemKind.ik_Axe:
                    case ItemKind.ik_Bow:
                    case ItemKind.ik_CrossBow:
                    case ItemKind.ik_Projectile:
                        {
                            ItemFlags wk = Flags;
                            ItemFlags wk2 = otherItem.Flags;
                            if (wk2 == wk) {
                                int dam = (GetAttribute(ItemAttribute.ia_DamageMin) + GetAttribute(ItemAttribute.ia_DamageMax)) / 2;
                                int dam2 = (otherItem.GetAttribute(ItemAttribute.ia_DamageMin) + otherItem.GetAttribute(ItemAttribute.ia_DamageMax)) / 2;
                                result = CompareInternal(dam, dam2);
                            }
                            break;
                        }
                }
            }

            return result;
        }

        public int GenBonus()
        {
            int result;
            if (fEntry.Sign.CompareTo("Ring_Protection") == 0) {
                result = 10 * RandomHelper.GetBoundedRnd(1, 7);
            } else {
                if (fEntry.BonusRange.Min == 0 && fEntry.BonusRange.Max == 0) {
                    result = 0;
                } else {
                    result = RandomHelper.GetBoundedRnd(fEntry.BonusRange.Min, fEntry.BonusRange.Max);
                }
            }
            return result;
        }

        public void GenCount()
        {
            if (CLSID == GlobalVars.iid_Arrow || CLSID == GlobalVars.iid_Bolt) {
                Count = (ushort)RandomHelper.GetBoundedRnd(2, 25);
            } else {
                Count = 1;
            }
        }

        public int AverageDamage
        {
            get {
                int result = 0;
                if (Weapon) {
                    result = (int)(Math.Round((double)(fEntry.Attributes[1] + fEntry.Attributes[2]) / 2.0f));
                }
                return result;
            }
        }

        public int Damage
        {
            get {
                int result = 0;
                if (Weapon) {
                    result = RandomHelper.GetBoundedRnd(fEntry.Attributes[1], fEntry.Attributes[2]);
                }
                return result;
            }
        }

        public override string Name
        {
            get {
                return GetDeclinableName(Number.nUndefined, Case.cNominative, false);
            }
        }

        public string GetName(bool uncondUnknown)
        {
            return GetDeclinableName(Number.nUndefined, Case.cNominative, uncondUnknown);
        }

        public string GetDeclinableName(Number aNumber, Case aCase, bool uncondUnknown)
        {
            string result = "";

            if (CLSID == GlobalVars.iid_DeadBody || CLSID == GlobalVars.iid_Mummy) {
                if (Contents.Count == 1) {
                    NWCreature mon = (NWCreature)Contents[0];
                    string dbstr;
                    if (mon.Entry.Remains) {
                        dbstr = BaseLocale.GetStr(RS.rs_Remains);
                    } else {
                        dbstr = fEntry.Name;
                    }
                    result = dbstr + " " + mon.GetDeclinableName(Number.nSingle, Case.cGenitive);
                }
            } else {
                ItemKind kind = Kind;
                if (kind < ItemKind.ik_Potion || (kind >= ItemKind.ik_Tool && kind != ItemKind.ik_Wand && kind != ItemKind.ik_Scroll && kind != ItemKind.ik_Amulet)) {
                    if (Countable) {
                        result = GetCountableName((int)Count);
                    } else {
                        result = fEntry.Name;
                    }
                } else {
                    if (fIdentified && !uncondUnknown) {
                        if (CLSID == GlobalVars.iid_Ring_Delusion) {
                            if (Bonus > 0) {
                                result = GlobalVars.nwrDB.GetEntry(Bonus).Name;
                            } else {
                                result = fEntry.Name;
                            }
                        } else {
                            if (CLSID == GlobalVars.iid_SoulTrapping_Ring && Bonus > 0) {
                                result = fEntry.Name + " (" + ((CreatureEntry)GlobalVars.nwrDB.GetEntry(Bonus)).Name + ")";
                            } else {
                                if (CLSID == GlobalVars.iid_Ring_Protection) {
                                    result = fEntry.Name + " (" + Convert.ToString(Bonus) + "%)";
                                } else {
                                    if (Kind == ItemKind.ik_Wand) {
                                        result = fEntry.Name + " (" + Convert.ToString(Bonus) + ")";
                                    } else {
                                        result = fEntry.Name;
                                    }
                                }
                            }
                        }
                    } else {
                        result = SecretiveName;
                    }
                }

                if (CLSID != GlobalVars.iid_Coin) {
                    result += StateSym;
                }
            }

            return result;
        }

        private string StateSym
        {
            get {
                if (State == ItemState.is_Normal) {
                    return "";
                } else {
                    string name = BaseLocale.GetStr(StaticData.dbItemStates[(int)State].Name);
                    return " (" + name[0] + ")";
                }
            }
        }

        private string SecretiveName
        {
            get {
                string result = "";
    
                ItemKind kind = Kind;
                switch (kind) {
                    case ItemKind.ik_Potion:
                        result = BaseLocale.GetStr(RS.rs_Potion);
                        break;
                    case ItemKind.ik_Ring:
                        result = BaseLocale.GetStr(RS.rs_Ring);
                        break;
                    case ItemKind.ik_Wand:
                        result = BaseLocale.GetStr(RS.rs_Wand);
                        break;
                    case ItemKind.ik_Scroll:
                        result = BaseLocale.GetStr(RS.rs_Scroll);
                        break;
                    case ItemKind.ik_Amulet:
                        result = BaseLocale.GetStr(RS.rs_IK_Amulet);
                        break;
                }
    
                if (kind == ItemKind.ik_Amulet) {
                    return result;
                } else {
                    string prefix = GetPrefix(kind);
                    return prefix + " " + result;
                }
            }
        }

        private string GetPrefix(ItemKind kind)
        {
            string prefix = BaseLocale.GetStr(fEntry.Prefix);

            if (GlobalVars.nwrWin.LangExt.Equals("ru")) {
                Gender wordGender = Gender.gUndefined;
                switch (kind) {
                    case ItemKind.ik_Potion:
                        wordGender = Gender.gNeutral;
                        break;
                    case ItemKind.ik_Ring:
                        wordGender = Gender.gNeutral;
                        break;
                    case ItemKind.ik_Wand:
                        wordGender = Gender.gFemale;
                        break;
                    case ItemKind.ik_Scroll:
                        wordGender = Gender.gMale;
                        break;
                }

                prefix = Grammar.morphAdjective(prefix, Case.cNominative, Number.nSingle, wordGender);
            }

            return prefix;
        }

        public ItemsList ItemContainer
        {
            get {
                ItemsList result = null;
    
                if (Owner is NWCreature) {
                    result = ((NWCreature)Owner).Items;
                } else {
                    if (Owner is NWField) {
                        result = ((NWField)Owner).Items;
                    }
                }
    
                return result;
            }
        }

        public Item GetSeveral(int aCount)
        {
            Item Result = null;
            if (Countable && Count != 1 && aCount < (int)Count) {
                Count = (ushort)(Count - aCount);
                Result = new Item(fSpace, null);
                Result.CLSID = CLSID;
                Result.Count = (ushort)aCount;
                Result.Identified = Identified;
            }
            return Result;
        }

        public int GetTradePrice(CreatureEntity aBuyer, CreatureEntity aSeller)
        {
            int result = Price;

            NWCreature buyer = (NWCreature)((aBuyer is NWCreature) ? aBuyer : null);
            NWCreature seller = (NWCreature)((aSeller is NWCreature) ? aSeller : null);
            if (buyer != null && seller != null) {
                if (seller.IsTrader) {
                    result = (int)Math.Round((result * 1.2));
                } else {
                    if (buyer.IsTrader) {
                        result = (int)((int)((uint)result >> 1));
                    }
                }
            }

            return result;
        }

        public ItemFlags Flags
        {
            get {
                return fEntry.Flags;
            }
        }

        public bool DefenceBonus
        {
            get {
                ItemKind kind = Kind;
                return kind == ItemKind.ik_Armor || (kind >= ItemKind.ik_Shield && kind < ItemKind.ik_Spear) || (Kind == ItemKind.ik_Ring && fEntry.Sign.CompareTo("Ring_Agility") == 0);
            }
        }

        public bool Ware
        {
            get {
                return fEntry.Ware;
            }
        }

        public bool Projectile
        {
            get {
                return fEntry.Flags.Contains(ItemFlags.if_Projectile);
            }
        }

        public bool Weapon
        {
            get {
                return (fEntry.Flags.HasIntersect(ItemFlags.if_MeleeWeapon, ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon, ItemFlags.if_Projectile));
            }
        }

        public MaterialKind Material
        {
            get {
                return fEntry.Material;
            }
        }

        public override void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            base.LoadFromStream(stream, version);

            Count = (ushort)StreamUtils.ReadWord(stream);
            if (Container) {
                Contents.LoadFromStream(stream, version);
            }
            Bonus = StreamUtils.ReadInt(stream);
            fIdentified = StreamUtils.ReadBoolean(stream);
            State = (ItemState)StreamUtils.ReadByte(stream);
            InUse = StreamUtils.ReadBoolean(stream);
            fWeight = StreamUtils.ReadFloat(stream);
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            try {
                base.SaveToStream(stream, version);

                StreamUtils.WriteWord(stream, Count);
                if (Container) {
                    Contents.SaveToStream(stream, version);
                }
                StreamUtils.WriteInt(stream, Bonus);
                StreamUtils.WriteBoolean(stream, fIdentified);
                StreamUtils.WriteByte(stream, (byte)State);
                StreamUtils.WriteBoolean(stream, fInUse);
                StreamUtils.WriteFloat(stream, fWeight);
            } catch (Exception ex) {
                Logger.Write("Item.saveToStream(): " + ex.Message);
                throw ex;
            }
        }

        public static Item CreateItem(object owner, int itemID, bool ident)
        {
            ItemEntry entry = (ItemEntry)GlobalVars.nwrDB.GetEntry(itemID);
            if (entry == null) {
                return null;
            }

            Item result = null;

            int id;
            if (entry.Meta) {
                id = entry.Rnd;
                ident = false;
            } else {
                id = itemID;
            }

            EntityList<Item> list = null;
            if (owner is NWCreature) {
                list = ((NWCreature)owner).Items;
            } else if (owner is NWField) {
                list = ((NWField)owner).Items;
            } else if (owner is NWLayer) {
                //list = ((NWLayer) owner).getItems();
            }

            if (list != null && id >= 0 && GlobalVars.nwrDB.GetEntry(id) != null) {
                result = new Item(GameSpace.Instance, owner);
                result.CLSID = id;
                result.Count = 1;
                result.Identified = ident;
                list.Add(result);
            }

            return result;
        }

        public static void GenItem(GameEntity owner, int itemID, int count, bool ident)
        {
            ItemEntry itemEntry = (ItemEntry)GlobalVars.nwrDB.GetEntry(itemID);

            if (itemEntry != null) {
                if (itemEntry.Countable) {
                    CreateItem(owner, itemID, ident).Count = (ushort)count;
                } else {
                    for (int i = 1; i <= count; i++) {
                        CreateItem(owner, itemID, ident);
                    }
                }
            }
        }

        public string GetHint(bool isTrade)
        {
            string res;
            res = Name + AuxUtils.CRLF;

            res = StringHelper.UniformName(res);
            if (!Identified) {
                res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_IsNotIdentified);
            } else {
                if (State != ItemState.is_Normal) {
                    res = res + AuxUtils.CRLF + BaseLocale.GetStr(StaticData.dbItemStates[(int)State].Name);
                }
                ItemKind kind = Kind;
                if ((kind < ItemKind.ik_DeadBody || (kind >= ItemKind.ik_Ring && kind != ItemKind.ik_Misc)) && Material != MaterialKind.mk_None) {
                    res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Material) + ": " + BaseLocale.GetStr(StaticData.dbMaterialKind[(int)Material].Name);
                }
                switch (Kind) {
                    case ItemKind.ik_Armor:
                    case ItemKind.ik_HeavyArmor:
                    case ItemKind.ik_MediumArmor:
                    case ItemKind.ik_LightArmor:
                        {
                            res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Armor) + ": " + Convert.ToString(GetAttribute(ItemAttribute.ia_Defense));
                            break;
                        }
                    case ItemKind.ik_Ring:
                        {
                            res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Armor) + ": " + Convert.ToString(GetAttribute(ItemAttribute.ia_Defense));
                            res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Damage) + ": " + Convert.ToString(GetAttribute(ItemAttribute.ia_DamageMin)) + "-" + Convert.ToString(GetAttribute(ItemAttribute.ia_DamageMax));
                            break;
                        }
                    case ItemKind.ik_BluntWeapon:
                    case ItemKind.ik_ShortBlade:
                    case ItemKind.ik_LongBlade:
                    case ItemKind.ik_Spear:
                    case ItemKind.ik_Axe:
                    case ItemKind.ik_Bow:
                    case ItemKind.ik_CrossBow:
                    case ItemKind.ik_Projectile:
                        {
                            res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Damage) + ": " + Convert.ToString(GetAttribute(ItemAttribute.ia_DamageMin)) + "-" + Convert.ToString(GetAttribute(ItemAttribute.ia_DamageMax));
                            break;
                        }
                    case ItemKind.ik_Amulet:
                        {
                            res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Armor) + ": " + Convert.ToString(GetAttribute(ItemAttribute.ia_Defense));
                            res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Damage) + ": " + Convert.ToString(GetAttribute(ItemAttribute.ia_DamageMin)) + "-" + Convert.ToString(GetAttribute(ItemAttribute.ia_DamageMax));
                            break;
                        }
                    case ItemKind.ik_Shield:
                    case ItemKind.ik_Helmet:
                    case ItemKind.ik_Clothing:
                        {
                            res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Armor) + ": " + Convert.ToString(GetAttribute(ItemAttribute.ia_Defense));
                            break;
                        }
                }
            }

            res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Weight) + ": " + string.Format("{0,1:F2}", new object[]{ Weight });

            if (isTrade) {
                res = res + AuxUtils.CRLF + BaseLocale.GetStr(RS.rs_Price) + ": " + Convert.ToString(Price);
            }

            return res;
        }
    }
}
