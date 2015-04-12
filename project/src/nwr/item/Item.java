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
package nwr.item;

import java.io.IOException;
import jzrlib.core.CreatureEntity;
import jzrlib.core.EntityList;
import jzrlib.core.FileVersion;
import jzrlib.core.GameEntity;
import jzrlib.core.GameSpace;
import jzrlib.core.LocatedEntity;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.grammar.Case;
import jzrlib.grammar.Gender;
import jzrlib.grammar.Grammar;
import jzrlib.grammar.Number;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.StreamUtils;
import jzrlib.utils.TextUtils;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.ItemAttribute;
import nwr.core.types.ItemKind;
import nwr.core.types.ItemState;
import nwr.core.types.ItemsCompareResult;
import nwr.core.types.MaterialKind;
import nwr.core.types.SymbolID;
import nwr.creatures.BodypartType;
import nwr.creatures.NWCreature;
import nwr.database.CreatureEntry;
import nwr.database.ItemEntry;
import nwr.database.ItemEntry.EffectEntry;
import nwr.database.ItemFlags;
import nwr.effects.EffectExt;
import nwr.effects.EffectParams;
import nwr.effects.InvokeMode;
import nwr.game.NWGameSpace;
import nwr.main.GlobalVars;
import nwr.universe.NWField;
import nwr.universe.NWLayer;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class Item extends LocatedEntity
{
    private EntityList fContents;
    private ItemEntry fEntry;
    private boolean fIdentified;
    private boolean fInUse;
    private float fWeight;

    public int Bonus;
    public short Count;
    public byte Frame;
    public ItemState State;

    public Item(GameSpace space, Object owner)
    {
        super(space, owner);
        this.Frame = 0;
        this.State = ItemState.is_Normal;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.fContents != null) {
                this.fContents.dispose();
            }
        }
        super.dispose(disposing);
    }

    public final NWGameSpace getSpace()
    {
        return (NWGameSpace) this.fSpace;
    }

    public final ItemEntry getEntry()
    {
        return this.fEntry;
    }

    public final boolean getIdentified()
    {
        return this.fIdentified;
    }

    public final boolean getInUse()
    {
        return this.fInUse;
    }

    /**
     *
     * @param index Value of ItemAttribute enum
     * @return
     */
    public final int getAttribute(int index)
    {
        int result = this.fEntry.Attributes[index - 1];
        if (index == ItemAttribute.ia_Defense) {
            if (this.isDefenceBonus()) {
                result += this.Bonus;
            }
            result++;
        }
        return result;
    }

    public final EntityList getContents()
    {
        EntityList result;
        if (!this.isContainer()) {
            result = null;
        } else {
            if (this.fContents == null) {
                this.fContents = new EntityList(null, true);
            }
            result = this.fContents;
        }
        return result;
    }

    public final BodypartType getEquipmentKind()
    {
        return this.fEntry.eqKind;
    }

    public final int getImageIndex()
    {
        int Result = this.fEntry.ImageIndex;
        if (super.CLSID == GlobalVars.iid_Torch && !this.getInUse()) {
            Result++;
        }
        if (this.Frame > 0 && this.Frame <= this.fEntry.FramesLoaded) {
            Result += (int) this.Frame;
        }
        return Result;
    }

    public final boolean isUnique()
    {
        return this.fEntry.Flags.contains(ItemFlags.if_IsUnique);
    }

    public final boolean isContainer()
    {
        return super.CLSID == GlobalVars.iid_SoulTrapping_Ring || this.fEntry.Flags.contains(ItemFlags.if_IsContainer);
    }

    public final boolean isEquipment()
    {
        return this.getEquipmentKind() != BodypartType.bp_None;
    }

    public final boolean isTwoHanded()
    {
        return this.fEntry.Flags.contains(ItemFlags.if_TwoHanded);
    }

    public final boolean isIngredient()
    {
        return this.fEntry.Flags.contains(ItemFlags.if_Ingredient);
    }

    public final ItemKind getKind()
    {
        ItemKind result;
        if (this.fEntry != null) {
            result = this.fEntry.ItmKind;
        } else {
            result = ItemKind.ik_Misc;
        }
        return result;
    }

    public final SymbolID getSymbol()
    {
        SymbolID Result = SymbolID.sid_Tool;
        if (this.fEntry == null) {
            return Result;
        }

        switch (this.fEntry.ItmKind) {
            case ik_Armor:
            case ik_Shield:
            case ik_Helmet:
            case ik_Clothing:
            case ik_HeavyArmor:
            case ik_MediumArmor:
            case ik_LightArmor:
                Result = SymbolID.sid_Armor;
                break;

            case ik_DeadBody:
                Result = SymbolID.sid_DeadBody;
                break;

            case ik_Food:
                Result = SymbolID.sid_Food;
                break;

            case ik_Potion:
                Result = SymbolID.sid_Potion;
                break;

            case ik_Ring:
                Result = SymbolID.sid_Ring;
                break;

            case ik_Tool:
                Result = SymbolID.sid_Tool;
                break;

            case ik_Wand:
                Result = SymbolID.sid_Wand;
                break;

            case ik_BluntWeapon:
            case ik_ShortBlade:
            case ik_LongBlade:
            case ik_Spear:
            case ik_Axe:
            case ik_Bow:
            case ik_CrossBow:
            case ik_Projectile:
                Result = SymbolID.sid_Weapon;
                break;

            case ik_Scroll:
                Result = SymbolID.sid_Scroll;
                break;

            case ik_Coin:
                Result = SymbolID.sid_Coin;
                break;

            case ik_Amulet:
                Result = SymbolID.sid_Amulet;
                break;

            case ik_Misc:
                Result = SymbolID.sid_Tool;
                break;
        }

        return Result;
    }

    public final void setAttribute(ItemAttribute index, int value)
    {
    }

    public final NWCreature getHolder()
    {
        if (super.Owner != null && super.Owner instanceof NWCreature) {
            return (NWCreature) super.Owner;
        } else {
            return null;
        }
    }
    
    public final void setIdentified(boolean value)
    {
        if (this.fIdentified != value) {
            this.fIdentified = value;
            if (this.fIdentified && super.Owner != null && super.Owner instanceof NWCreature) {
                ((NWCreature) super.Owner).knowIt(super.CLSID);
            }
        }
    }

    public final void setInUse(boolean value)
    {
        if (this.fInUse != value) {
            if (!value && this.State == ItemState.is_Cursed) {
                this.getSpace().showText(super.Owner, Locale.getStr(RS.rs_HexedItemsDoNotRemove));
            } else {
                this.fInUse = value;
                if (super.Owner != null && super.Owner instanceof NWCreature) {
                    NWCreature creature = (NWCreature) super.Owner;
                    if (this.fInUse) {
                        creature.ArmorClass += this.getAttribute(ItemAttribute.ia_Defense);
                        creature.DBMin += this.getAttribute(ItemAttribute.ia_DamageMin);
                        creature.DBMax += this.getAttribute(ItemAttribute.ia_DamageMax);
                        creature.Strength += this.getAttribute(ItemAttribute.ia_Mdf_Str);
                        creature.Luck += this.getAttribute(ItemAttribute.ia_Mdf_Luck);
                        creature.SpeedMod += this.getAttribute(ItemAttribute.ia_Mdf_Speed);
                        creature.Attacks += this.getAttribute(ItemAttribute.ia_Mdf_Attacks);
                        creature.ToHit += this.getAttribute(ItemAttribute.ia_Mdf_ToHit);
                        this.applyEffects(creature, InvokeMode.im_UseBegin, null);
                    } else {
                        creature.ArmorClass -= this.getAttribute(ItemAttribute.ia_Defense);
                        creature.DBMin -= this.getAttribute(ItemAttribute.ia_DamageMin);
                        creature.DBMax -= this.getAttribute(ItemAttribute.ia_DamageMax);
                        creature.Strength -= this.getAttribute(ItemAttribute.ia_Mdf_Str);
                        creature.Luck -= this.getAttribute(ItemAttribute.ia_Mdf_Luck);
                        creature.SpeedMod -= this.getAttribute(ItemAttribute.ia_Mdf_Speed);
                        creature.Attacks -= this.getAttribute(ItemAttribute.ia_Mdf_Attacks);
                        creature.ToHit -= this.getAttribute(ItemAttribute.ia_Mdf_ToHit);
                        this.applyEffects(creature, InvokeMode.im_UseEnd, null);
                    }
                }
            }
        }
    }

    public final short getSatiety()
    {
        short result;
        if (super.CLSID == GlobalVars.iid_DeadBody) {
            result = ((NWCreature) this.fContents.getItem(0)).getEntry().FleshSatiety;
        } else {
            result = this.fEntry.Satiety;
        }
        return result;
    }

    private String getCountableName(int count)
    {
        String num = String.valueOf(count);

        Case c = Case.cUndefined;
        Number i = Number.nSingle;

        char c2 = num.charAt(num.length() - 1);

        if (c2 == '1') {
            c = Case.cNominative;
            i = Number.nSingle;
        } else if ("234".indexOf(c2) >= 0) {
            c = Case.cGenitive;
            i = Number.nSingle;
        } else if ("567890".indexOf(c2) >= 0) {
            c = Case.cGenitive;
            i = Number.nPlural;
        }

        if (num.length() >= 2) {
            if (num.charAt(num.length() - 2) == '1') {
                c = Case.cGenitive;
                i = Number.nPlural;
            }
        }

        String result = this.fEntry.getNounDeclension(i, c);
        if (count > 1) {
            result = num + " " + result;
        }
        return result;
    }

    private static int compareInternal(int val1, int val2)
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

    public final boolean isCountable()
    {
        return this.fEntry.isCountable();
    }

    public final int getPrice()
    {
        int result = this.fEntry.Price;

        if (this.fEntry.ItmKind == ItemKind.ik_Wand) {
            result = (1 + result * this.Bonus);
        }

        if (this.fIdentified) {
            if (this.State != ItemState.is_Blessed) {
                if (this.State == ItemState.is_Cursed) {
                    result = (int) Math.round((result * 0.75));
                }
            } else {
                result = (int) Math.round((result * 1.25));
            }
        }
        if (result < 1) {
            result = 1;
        }
        result = (int) (result * this.Count);

        return result;
    }

    @Override
    public byte getSerializeKind()
    {
        return StaticData.SID_ITEM;
    }

    public final float getWeight()
    {
        float result;
        if (super.CLSID == GlobalVars.iid_DeadBody) {
            result = ((NWCreature) this.fContents.getItem(0)).getWeight();
        } else {
            result = (((float) this.fWeight * this.Count));
        }
        return result;
    }

    @Override
    public void setCLSID(int value)
    {
        super.setCLSID(value);
        this.fEntry = ((ItemEntry) GlobalVars.nwrBase.getEntry(value));

        this.fWeight = this.fEntry.Weight;
        if (this.fEntry.Sign.compareTo("Amulet_Holding") == 0) {
            this.State = ItemState.is_Cursed;
        }
        switch (this.getKind()) {
            case ik_Armor:
            case ik_Tool:
            case ik_Wand:
            case ik_BluntWeapon:
            case ik_Amulet:
            case ik_ShortBlade:
            case ik_LongBlade:
            case ik_Shield:
            case ik_Helmet:
            case ik_Clothing:
            case ik_Spear:
            case ik_Axe:
            case ik_Bow:
            case ik_CrossBow:
            case ik_HeavyArmor:
            case ik_MediumArmor:
            case ik_LightArmor:
            case ik_Projectile:
            case ik_Misc: {
                this.Bonus = this.genBonus();
                break;
            }
            case ik_DeadBody:
            case ik_Food:
            case ik_Potion:
            case ik_Scroll:
            case ik_Coin: {
                this.Bonus = 0;
                break;
            }
            case ik_Ring: {
                if (value == GlobalVars.iid_Ring_Delusion) {
                    this.Bonus = GlobalVars.dbRings.get(AuxUtils.getRandom(GlobalVars.dbRings.getCount()));
                } else {
                    if (value == GlobalVars.iid_SoulTrapping_Ring) {
                        this.Bonus = 0;
                    } else {
                        this.Bonus = this.genBonus();
                    }
                }
                break;
            }
        }
    }

    public final void setWeight(float value)
    {
        this.fWeight = value;
    }

    public final void applyEffects(CreatureEntity target, InvokeMode invokeMode, EffectExt extData)
    {
        try {
            EffectEntry[] effects = this.fEntry.Effects;
            if (effects == null) {
                return;
            }

            int num = effects.length;
            for (int i = 0; i < num; i++) {
                EffectEntry eff = this.fEntry.Effects[i];

                EffectExt ext;
                if (invokeMode.getValue() < InvokeMode.im_UseBegin.getValue() || invokeMode.getValue() >= InvokeMode.im_ItSelf.getValue()) {
                    ext = extData;
                } else {
                    ext = new EffectExt();
                    ext.setParam(EffectParams.ep_ItemExt, eff.ExtData);
                }

                ((NWCreature) target).useEffect(eff.EffID, this, invokeMode, ext);
            }
        } catch (Exception ex) {
            Logger.write("Item.applyEffects(): " + ex.getMessage());
        }
    }

    @Override
    public boolean assign(GameEntity item)
    {
        Item otherItem = (Item) item;
        boolean result = (super.CLSID == item.getCLSID()) && this.isCountable() && (this.Bonus == otherItem.Bonus);

        if (result) {
            this.Count = (short) (this.Count + otherItem.Count);
        }

        return result;
    }

    private int getBreakageChance(boolean bodyHit)
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

    public final boolean isBreakage(boolean bodyHit)
    {
        return AuxUtils.chance(this.getBreakageChance(bodyHit));
    }
    
    /**
     *
     * @param otherItem
     * @return Value of ItemsCompareResult enumeration
     */
    public final int compare(Item otherItem)
    {
        int result = ItemsCompareResult.icr_NotComparable;

        if (this.getKind() == otherItem.getKind() && this.getIdentified() && otherItem.getIdentified()) {
            switch (this.getKind()) {
                case ik_Armor:
                case ik_Shield:
                case ik_Helmet:
                case ik_Clothing:
                case ik_HeavyArmor:
                case ik_MediumArmor:
                case ik_LightArmor: {
                    result = Item.compareInternal(this.getAttribute(ItemAttribute.ia_Defense), otherItem.getAttribute(ItemAttribute.ia_Defense));
                    break;
                }
                case ik_Potion:
                case ik_Ring:
                case ik_Wand:
                case ik_Scroll:
                case ik_Amulet: {
                    if (super.CLSID != otherItem.CLSID) {
                        result = ItemsCompareResult.icr_NotComparable;
                    } else {
                        result = Item.compareInternal(this.Bonus + this.State.CompareValue, otherItem.Bonus + otherItem.State.CompareValue);
                    }
                    break;
                }
                case ik_BluntWeapon:
                case ik_ShortBlade:
                case ik_LongBlade:
                case ik_Spear:
                case ik_Axe:
                case ik_Bow:
                case ik_CrossBow:
                case ik_Projectile: {
                    ItemFlags wk = this.getFlags();
                    ItemFlags wk2 = otherItem.getFlags();
                    if (wk2 == wk) {
                        int dam = (this.getAttribute(ItemAttribute.ia_DamageMin) + this.getAttribute(ItemAttribute.ia_DamageMax)) / 2;
                        int dam2 = (otherItem.getAttribute(ItemAttribute.ia_DamageMin) + otherItem.getAttribute(ItemAttribute.ia_DamageMax)) / 2;
                        result = Item.compareInternal(dam, dam2);
                    }
                    break;
                }
            }
        }

        return result;
    }

    public final int genBonus()
    {
        int result;
        if (this.fEntry.Sign.compareTo("Ring_Protection") == 0) {
            result = 10 * AuxUtils.getBoundedRnd(1, 7);
        } else {
            if (this.fEntry.BonusRange.Min == 0 && this.fEntry.BonusRange.Max == 0) {
                result = 0;
            } else {
                result = AuxUtils.getBoundedRnd(this.fEntry.BonusRange.Min, this.fEntry.BonusRange.Max);
            }
        }
        return result;
    }

    public final void genCount()
    {
        if (super.CLSID == GlobalVars.iid_Arrow || super.CLSID == GlobalVars.iid_Bolt) {
            this.Count = (short) AuxUtils.getBoundedRnd(2, 25);
        } else {
            this.Count = 1;
        }
    }

    public final int getAverageDamage()
    {
        int result = 0;
        if (this.isWeapon()) {
            result = (int) (Math.round((double) (this.fEntry.Attributes[1] + this.fEntry.Attributes[2]) / 2.0f));
        }
        return result;
    }

    public final int getDamage()
    {
        int result = 0;
        if (this.isWeapon()) {
            result = AuxUtils.getBoundedRnd(this.fEntry.Attributes[1], this.fEntry.Attributes[2]);
        }
        return result;
    }

    @Override
    public String getName()
    {
        return this.getDeclinableName(Number.nUndefined, Case.cNominative, false);
    }

    public final String getName(boolean uncondUnknown)
    {
        return this.getDeclinableName(Number.nUndefined, Case.cNominative, uncondUnknown);
    }

    public final String getDeclinableName(Number aNumber, Case aCase, boolean uncondUnknown)
    {
        String result = "";

        if (super.CLSID == GlobalVars.iid_DeadBody || super.CLSID == GlobalVars.iid_Mummy) {
            if (this.getContents().getCount() == 1) {
                NWCreature mon = (NWCreature) this.getContents().getItem(0);
                String dbstr;
                if (mon.getEntry().isRemains()) {
                    dbstr = Locale.getStr(RS.rs_Remains);
                } else {
                    dbstr = this.fEntry.getName();
                }
                result = dbstr + " " + mon.getDeclinableName(Number.nSingle, Case.cGenitive);
            }
        } else {
            ItemKind kind = this.getKind();
            if (kind.Value < ItemKind.ik_Potion.Value
                    || (kind.Value >= ItemKind.ik_Tool.Value && kind != ItemKind.ik_Wand && kind != ItemKind.ik_Scroll && kind != ItemKind.ik_Amulet)) {
                if (this.isCountable()) {
                    result = this.getCountableName((int) this.Count);
                } else {
                    result = this.fEntry.getName();
                }
            } else {
                if (this.fIdentified && !uncondUnknown) {
                    if (super.CLSID == GlobalVars.iid_Ring_Delusion) {
                        if (this.Bonus > 0) {
                            result = GlobalVars.nwrBase.getEntry(this.Bonus).getName();
                        } else {
                            result = this.fEntry.getName();
                        }
                    } else {
                        if (super.CLSID == GlobalVars.iid_SoulTrapping_Ring && this.Bonus > 0) {
                            result = this.fEntry.getName() + " (" + ((CreatureEntry) GlobalVars.nwrBase.getEntry(this.Bonus)).getName() + ")";
                        } else {
                            if (super.CLSID == GlobalVars.iid_Ring_Protection) {
                                result = this.fEntry.getName() + " (" + String.valueOf(this.Bonus) + "%)";
                            } else {
                                if (this.getKind() == ItemKind.ik_Wand) {
                                    result = this.fEntry.getName() + " (" + String.valueOf(this.Bonus) + ")";
                                } else {
                                    result = this.fEntry.getName();
                                }
                            }
                        }
                    }
                } else {
                    result = this.getSecretiveName();
                }
            }

            if (super.CLSID != GlobalVars.iid_Coin) {
                result += this.getStateSym();
            }
        }

        return result;
    }

    private String getStateSym()
    {
        if (this.State == ItemState.is_Normal) {
            return "";
        } else {
            String name = Locale.getStr(this.State.NameRS);
            return " (" + name.charAt(0) + ")";
        }
    }
    
    private String getSecretiveName()
    {
        String result = "";

        ItemKind kind = this.getKind();
        switch (kind) {
            case ik_Potion:
                result = Locale.getStr(RS.rs_Potion);
                break;
            case ik_Ring:
                result = Locale.getStr(RS.rs_Ring);
                break;
            case ik_Wand:
                result = Locale.getStr(RS.rs_Wand);
                break;
            case ik_Scroll:
                result = Locale.getStr(RS.rs_Scroll);
                break;
            case ik_Amulet:
                result = Locale.getStr(RS.rs_IK_Amulet);
                break;
        }

        if (kind == ItemKind.ik_Amulet) {
            return result;
        } else {
            String prefix = this.getPrefix(kind);
            return prefix + " " + result;
        }
    }

    private String getPrefix(ItemKind kind)
    {
        String prefix = Locale.getStr(this.fEntry.Prefix);

        if (GlobalVars.nwrWin.getLangExt().equals("ru")) {
            Gender wordGender = Gender.gUndefined;
            switch (kind) {
                case ik_Potion:
                    wordGender = Gender.gNeutral;
                    break;
                case ik_Ring:
                    wordGender = Gender.gNeutral;
                    break;
                case ik_Wand:
                    wordGender = Gender.gFemale;
                    break;
                case ik_Scroll:
                    wordGender = Gender.gMale;
                    break;
            }

            prefix = Grammar.morphAdjective(prefix, Case.cNominative, Number.nSingle, wordGender);
        }

        return prefix;
    }

    public final ItemsList getItemContainer()
    {
        ItemsList result = null;

        if (super.Owner instanceof NWCreature) {
            result = ((NWCreature) super.Owner).getItems();
        } else {
            if (super.Owner instanceof NWField) {
                result = ((NWField) super.Owner).getItems();
            }
        }

        return result;
    }

    public final Item getSeveral(int aCount)
    {
        Item Result = null;
        if (this.isCountable() && this.Count != 1 && aCount < (int) this.Count) {
            this.Count = (short) ((int) this.Count - aCount);
            Result = new Item(this.fSpace, null);
            Result.setCLSID(super.CLSID);
            Result.Count = (short) aCount;
            Result.setIdentified(this.getIdentified());
        }
        return Result;
    }

    public final int getTradePrice(CreatureEntity aBuyer, CreatureEntity aSeller)
    {
        int result = this.getPrice();

        NWCreature buyer = (NWCreature) ((aBuyer instanceof NWCreature) ? aBuyer : null);
        NWCreature seller = (NWCreature) ((aSeller instanceof NWCreature) ? aSeller : null);
        if (buyer != null && seller != null) {
            if (seller.getIsTrader()) {
                result = (int) Math.round((result * 1.2));
            } else {
                if (buyer.getIsTrader()) {
                    result = (int) (result >>> 1);
                }
            }
        }

        return result;
    }

    public final ItemFlags getFlags()
    {
        return this.fEntry.Flags;
    }
    
    public final boolean isDefenceBonus()
    {
        ItemKind kind = this.getKind();
        return kind == ItemKind.ik_Armor
                || (kind.Value >= ItemKind.ik_Shield.Value && kind.Value < ItemKind.ik_Spear.Value)
                || (this.getKind() == ItemKind.ik_Ring && this.fEntry.Sign.compareTo("Ring_Agility") == 0);
    }

    public final boolean isWare()
    {
        return this.fEntry.isWare();
    }

    public final boolean isProjectile()
    {
        return this.fEntry.Flags.contains(ItemFlags.if_Projectile);
    }

    public final boolean isWeapon()
    {
        return (this.fEntry.Flags.hasIntersect(ItemFlags.if_MeleeWeapon, ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon, ItemFlags.if_Projectile));
    }

    public final MaterialKind getMaterial()
    {
        return this.fEntry.Material;
    }
    
    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        super.loadFromStream(stream, version);

        this.Count = (short) StreamUtils.readWord(stream);
        if (this.isContainer()) {
            this.getContents().loadFromStream(stream, version);
        }
        this.Bonus = StreamUtils.readInt(stream);
        this.fIdentified = StreamUtils.readBoolean(stream);
        this.State = ItemState.forValue(StreamUtils.readByte(stream));
        this.setInUse(StreamUtils.readBoolean(stream));
        this.fWeight = StreamUtils.readFloat(stream);
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        try {
            super.saveToStream(stream, version);

            StreamUtils.writeWord(stream, this.Count);
            if (this.isContainer()) {
                this.getContents().saveToStream(stream, version);
            }
            StreamUtils.writeInt(stream, this.Bonus);
            StreamUtils.writeBoolean(stream, this.fIdentified);
            StreamUtils.writeByte(stream, (byte) this.State.getValue());
            StreamUtils.writeBoolean(stream, this.fInUse);
            StreamUtils.writeFloat(stream, this.fWeight);
        } catch (Exception ex) {
            Logger.write("Item.saveToStream(): " + ex.getMessage());
            throw ex;
        }
    }

    public static Item createItem(Object owner, int itemID, boolean ident)
    {
        ItemEntry entry = (ItemEntry) GlobalVars.nwrBase.getEntry(itemID);
        if (entry == null) {
            return null;
        }

        Item result = null;

        int id;
        if (entry.isMeta()) {
            id = entry.getRnd();
            ident = false;
        } else {
            id = itemID;
        }

        EntityList list = null;
        if (owner instanceof NWCreature) {
            list = ((NWCreature) owner).getItems();
        } else if (owner instanceof NWField) {
            list = ((NWField) owner).getItems();
        } else if (owner instanceof NWLayer) {
            //list = ((NWLayer) owner).getItems();
        }

        if (list != null && id >= 0 && GlobalVars.nwrBase.getEntry(id) != null) {
            result = new Item(GameSpace.getInstance(), owner);
            result.setCLSID(id);
            result.Count = 1;
            result.setIdentified(ident);
            list.add(result);
        }

        return result;
    }

    public static void genItem(GameEntity owner, int itemID, int count, boolean ident)
    {
        ItemEntry itemEntry = (ItemEntry) GlobalVars.nwrBase.getEntry(itemID);

        if (itemEntry != null) {
            if (itemEntry.isCountable()) {
                Item.createItem(owner, itemID, ident).Count = (short) count;
            } else {
                for (int i = 1; i <= count; i++) {
                    Item.createItem(owner, itemID, ident);
                }
            }
        }
    }

    public final String getHint(boolean isTrade)
    {
        String res;
        res = this.getName() + AuxUtils.CRLF;

        res = TextUtils.upperFirst(res);
        if (!this.getIdentified()) {
            res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_IsNotIdentified);
        } else {
            if (this.State != ItemState.is_Normal) {
                res = res + AuxUtils.CRLF + Locale.getStr(this.State.NameRS);
            }
            ItemKind kind = this.getKind();
            if ((kind.Value < ItemKind.ik_DeadBody.Value || (kind.Value >= ItemKind.ik_Ring.Value && kind.Value != ItemKind.ik_Misc.Value))
                    && this.getMaterial() != MaterialKind.mk_None) {
                res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Material) + ": " + Locale.getStr(this.getMaterial().NameRS);
            }
            switch (this.getKind()) {
                case ik_Armor:
                case ik_HeavyArmor:
                case ik_MediumArmor:
                case ik_LightArmor: {
                    res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Armor) + ": " + String.valueOf(this.getAttribute(ItemAttribute.ia_Defense));
                    break;
                }
                case ik_Ring: {
                    res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Armor) + ": " + String.valueOf(this.getAttribute(ItemAttribute.ia_Defense));
                    res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Damage) + ": " + String.valueOf(this.getAttribute(ItemAttribute.ia_DamageMin)) + "-" + String.valueOf(this.getAttribute(ItemAttribute.ia_DamageMax));
                    break;
                }
                case ik_BluntWeapon:
                case ik_ShortBlade:
                case ik_LongBlade:
                case ik_Spear:
                case ik_Axe:
                case ik_Bow:
                case ik_CrossBow:
                case ik_Projectile: {
                    res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Damage) + ": " + String.valueOf(this.getAttribute(ItemAttribute.ia_DamageMin)) + "-" + String.valueOf(this.getAttribute(ItemAttribute.ia_DamageMax));
                    break;
                }
                case ik_Amulet: {
                    res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Armor) + ": " + String.valueOf(this.getAttribute(ItemAttribute.ia_Defense));
                    res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Damage) + ": " + String.valueOf(this.getAttribute(ItemAttribute.ia_DamageMin)) + "-" + String.valueOf(this.getAttribute(ItemAttribute.ia_DamageMax));
                    break;
                }
                case ik_Shield:
                case ik_Helmet:
                case ik_Clothing: {
                    res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Armor) + ": " + String.valueOf(this.getAttribute(ItemAttribute.ia_Defense));
                    break;
                }
            }
        }

        res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Weight) + ": " + String.format("%1.2f", new Object[]{this.getWeight()});

        if (isTrade) {
            res = res + AuxUtils.CRLF + Locale.getStr(RS.rs_Price) + ": " + String.valueOf(this.getPrice());
        }

        return res;
    }
}
