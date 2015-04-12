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
package nwr.creatures;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import jzrlib.common.CreatureSex;
import jzrlib.core.CreatureEntity;
import jzrlib.core.Directions;
import jzrlib.core.EntityList;
import jzrlib.core.ExtList;
import jzrlib.core.FileVersion;
import jzrlib.core.GameEntity;
import jzrlib.core.IEquippedCreature;
import jzrlib.core.LocatedEntity;
import jzrlib.core.Point;
import jzrlib.core.Range;
import jzrlib.core.Rect;
import jzrlib.core.StringList;
import jzrlib.core.body.AbstractBody;
import jzrlib.core.body.Bodypart;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.map.AbstractMap;
import jzrlib.map.IMap;
import jzrlib.map.Movements;
import jzrlib.map.PathSearch;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.RefObject;
import jzrlib.utils.StreamUtils;
import jzrlib.utils.TextUtils;
import nwr.core.AttributeList;
import nwr.core.GameEvent;
import nwr.core.IntList;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.AbilityID;
import nwr.core.types.ActionFlags;
import nwr.core.types.AlignmentEx;
import nwr.core.types.AttackKind;
import nwr.core.types.BestWeaponSigns;
import nwr.core.types.CreatureAction;
import nwr.core.types.CreatureState;
import nwr.core.types.DamageKind;
import nwr.core.types.EventID;
import nwr.core.types.ItemAttribute;
import nwr.core.types.ItemKind;
import nwr.core.types.ItemState;
import nwr.core.types.ItemStates;
import nwr.core.types.ItemsCompareResult;
import nwr.core.types.LogFeatures;
import nwr.core.types.PlaceID;
import nwr.core.types.RaceID;
import nwr.core.types.Reaction;
import nwr.core.types.RequestKind;
import nwr.core.types.SkillID;
import nwr.core.types.SymbolID;
import nwr.core.types.TeachableKind;
import nwr.core.types.TrapResult;
import nwr.core.types.TurnState;
import nwr.core.types.VolatileState;
import nwr.creatures.brain.BeastBrain;
import nwr.creatures.brain.EitriBrain;
import nwr.creatures.brain.LeaderBrain;
import nwr.creatures.brain.NWBrainEntity;
import nwr.creatures.brain.RavenBrain;
import nwr.creatures.brain.SentientBrain;
import nwr.creatures.brain.TraderBrain;
import nwr.creatures.brain.VictimBrain;
import nwr.creatures.brain.WarriorBrain;
import nwr.database.CreatureEntry;
import nwr.database.CreatureEntry.InventoryEntry;
import nwr.database.CreatureFlags;
import nwr.database.ItemEntry;
import nwr.database.ItemEntry.EffectEntry;
import nwr.database.ItemFlags;
import nwr.database.LayerEntry;
import nwr.effects.Effect;
import nwr.effects.EffectAction;
import nwr.effects.EffectExt;
import nwr.effects.EffectFlags;
import nwr.effects.EffectID;
import nwr.effects.EffectRec;
import nwr.effects.EffectsData;
import nwr.effects.EffectsFactory;
import nwr.effects.EffectsList;
import nwr.effects.InvokeMode;
import nwr.game.NWGameSpace;
import jzrlib.grammar.Case;
import jzrlib.grammar.Number;
import nwr.item.Item;
import nwr.item.ItemsList;
import nwr.main.GlobalVars;
import nwr.player.Player;
import nwr.universe.Building;
import nwr.universe.DungeonRoom;
import nwr.universe.Gate;
import nwr.universe.NWField;
import nwr.universe.NWLayer;
import nwr.universe.NWTile;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class NWCreature extends CreatureEntity implements IEquippedCreature
{
    protected static final class AttackInfo
    {
        public int ToHit;
        public int Damage;
        public int Parry;
        public CreatureAction Action;
    }

    private static final String[] NotImprisonable;
    private static final Reaction[][] relations;
    public static final int[] ShootIsoTrans;

    private AttributeList fAbilities;
    private AbstractBody fBody;
    private int fDamageBase;
    private EffectsList fEffects;
    private CreatureEntry fEntry;
    private int fExperience;
    private Point fField;
    private Building fHouse;
    private boolean fInFog;
    private boolean fIsMercenary;
    private ItemsList fItems;
    //private bool FItemsEnlarged;
    private boolean fIsTrader;
    private String fName;
    private EffectID FProwlSource;
    private AttributeList fSkills;
    private int fSpeed;
    private CreatureState fState;
    private byte FSurvey;

    public int SpeedMod;

    public AlignmentEx Alignment;
    public int ArmorClass;
    public int Attacks;
    public int Constitution;
    public int DBMax;
    public int DBMin;
    public short Dexterity;
    public byte Hear;
    public int HPCur;
    public int HPMax;
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

    public boolean Ghost;
    public int GhostIdx;
    public boolean Illusion;
    public boolean Prowling;
    public byte[] ProwlImage;
    
    static {
        NotImprisonable = new String[]{"Aspenth", "Balder", "Bartan", "Eitri", "Elcich", "Fenrir", "Freyr", "Garm", "GiantSquid", "Gymir", "Harbard", "Hatchetfish", "Heimdall", "Hreset", "IvyCreeper", "Jormungand", "LiKrin", "Loki", "Lorkesth", "Odin", "PaleMoss", "Qivuit", "Rashok", "Retchweed", "Scyld", "Surtr", "Thokk", "Thor", "Trader", "Tyr", "Uorik"};

        // (am_Mask_Good..am_Mask_Evil)*(am_Mask_Good..am_Mask_Evil)
        Reaction[][] array = new Reaction[3][3];
        array[0][0] = Reaction.rAlly;
        array[0][1] = Reaction.rNeutral;
        array[0][2] = Reaction.rHostile;
        array[1][0] = Reaction.rNeutral;
        array[1][1] = Reaction.rAlly;
        array[1][2] = Reaction.rNeutral;
        array[2][0] = Reaction.rHostile;
        array[2][1] = Reaction.rNeutral;
        array[2][2] = Reaction.rAlly;
        relations = array;

        ShootIsoTrans = new int[]{Directions.dtNone, Directions.dtNorthEast, Directions.dtSouthWest, Directions.dtNorthWest, Directions.dtSouthEast, Directions.dtNorth, Directions.dtEast, Directions.dtWest, Directions.dtSouth, Directions.dtZenith, Directions.dtNadir, Directions.dtPlace};
    }

    public NWCreature(NWGameSpace space, Object owner)
    {
        super(space, owner);

        this.fField = new Point();

        this.fAbilities = new AttributeList();
        this.fSkills = new AttributeList();
        this.fEffects = new EffectsList(this, true);
        this.fItems = new ItemsList(this, true);

        this.fHouse = null;
        this.Turn = 0;
        this.Stamina = 0;
    }

    public NWCreature(NWGameSpace space, Object owner, int creatureID, boolean total, boolean setName)
    {
        this(space, owner);
        this.initEx(creatureID, total, setName);
    }

    @Override
    public void setCLSID(int value)
    {
        super.setCLSID(value);

        this.fEntry = ((CreatureEntry) GlobalVars.nwrBase.getEntry(value));
    }

    public final void initEx(int creatureID, boolean total, boolean setName)
    {
        this.setCLSID(creatureID);
        this.init(-1, total, setName);
    }

    public void init(int creatureID, boolean total, boolean setName)
    {
        try {
            CreatureEntry entry;
            if (creatureID == -1) {
                entry = this.fEntry;
            } else {
                entry = ((CreatureEntry) GlobalVars.nwrBase.getEntry(creatureID));
            }

            this.Alignment = entry.Alignment;
            this.Sex = entry.Sex;
            this.Level = (int) entry.Level;
            this.fExperience = 0;
            this.Strength = (int) entry.Strength;
            this.fSpeed = (int) entry.Speed;
            this.Attacks = (int) entry.Attacks;
            this.ToHit = (int) entry.ToHit;
            this.Luck = 7;
            this.Constitution = (int) entry.Constitution;
            this.Dexterity = entry.Dexterity;
            this.FSurvey = entry.Survey;
            this.Hear = entry.Hear;
            this.Smell = entry.Smell;
            this.ArmorClass = (int) entry.AC;
            this.HPMax = AuxUtils.getBoundedRnd((int) entry.minHP, (int) entry.maxHP);
            this.HPCur = this.HPMax;
            this.DBMin = (int) entry.minDB;
            this.DBMax = (int) entry.maxDB;
            this.Perception = entry.Perception;

            byte op;
            if (creatureID == GlobalVars.cid_Merchant) {
                op = AttributeList.lao_Or;
            } else {
                if (total) {
                    op = AttributeList.lao_Copy;
                } else {
                    op = AttributeList.lao_Or;
                }
            }
            this.fAbilities.assign(entry.Abilities, op);
            this.fSkills.assign(entry.Skills, op);

            if (setName && entry.Race == RaceID.crHuman) {
                this.fName = this.getSpace().generateName(this, NamesLib.NameGen_NorseDic);
            }

            if (entry.Flags.contains(CreatureFlags.esUndead)) {
                this.setState(CreatureState.csUndead);
            } else {
                this.setState(CreatureState.csAlive);
            }

            this.initBody();
            this.initBrain();

            this.fEffects.clear();
            this.fItems.clear();

            if (total) {
                for (InventoryEntry inventory : entry.Inventory) {
                    ItemEntry itemEntry = (ItemEntry) GlobalVars.nwrBase.findEntryBySign(inventory.ItemSign);

                    if (itemEntry != null) {
                        int cnt = AuxUtils.getBoundedRnd(inventory.CountMin, inventory.CountMax);

                        if (itemEntry.isCountable()) {
                            Item item = Item.createItem(this, itemEntry.GUID, true);
                            item.Count = (short) cnt;
                            item.Bonus = inventory.Bonus;
                            item.State = inventory.State;
                        } else {
                            for (int i = 1; i <= cnt; i++) {
                                Item item = Item.createItem(this, itemEntry.GUID, true);
                                item.Bonus = inventory.Bonus;
                                item.State = inventory.State;
                            }
                        }
                    }
                }

                this.prepareItems();
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.init(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void assign(NWCreature source, boolean illusion)
    {
        this.initEx(source.CLSID, true, true);

        this.Level = source.Level;
        this.fExperience = source.fExperience;
        this.Strength = source.Strength;
        this.fSpeed = source.fSpeed;
        this.Attacks = source.Attacks;
        this.ToHit = source.ToHit;
        this.Luck = source.Luck;
        this.Constitution = source.Constitution;
        this.ArmorClass = source.ArmorClass;
        this.HPMax = source.HPMax;
        this.HPCur = source.HPCur;
        this.fIsTrader = false;
        this.fName = source.fName;
        this.Alignment = source.Alignment;
        
        if (illusion) {
            this.fDamageBase = 0;
            this.MPMax = 0;
            this.MPCur = 0;
            this.DBMin = 0;
            this.DBMax = 0;

            this.fAbilities.clear();
            this.fSkills.clear();
            this.fItems.clear();
        } else {
            this.fDamageBase = source.fDamageBase;
            this.MPMax = source.MPMax;
            this.MPCur = source.MPCur;
            this.DBMin = source.DBMin;
            this.DBMax = source.DBMax;
        }
    }

    public final NWCreature clone(boolean illusion)
    {
        NWCreature result = null;

        Point pt = this.getNearestPlace(3, true);
        if (pt != null) {
            result = new NWCreature(this.getSpace(), null);
            result.assign(this, illusion);

            if (this.isPlayer()) {
                result.setBrain(new SentientBrain(result));
            }

            result.transferTo(this.LayerID, this.fField.X, this.fField.Y, pt.X, pt.Y, StaticData.MapArea, true, false);
        }

        return result;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fItems.dispose();
            this.fEffects.dispose();
            this.fSkills.dispose();
            this.fAbilities.dispose();

            if (this.fBody != null) {
                this.fBody.dispose();
            }
        }
        super.dispose(disposing);
    }

    @Override
    public final NWGameSpace getSpace()
    {
        return (NWGameSpace) this.fSpace;
    }

    public final void setDamageBase(int value)
    {
        this.fDamageBase = value;
    }

    public final EffectsList getEffects()
    {
        return this.fEffects;
    }

    public final CreatureEntry getEntry()
    {
        return this.fEntry;
    }

    public final Point getField()
    {
        return this.fField;
    }

    public final boolean getInFog()
    {
        return this.fInFog;
    }

    public final boolean getIsTrader()
    {
        return this.fIsTrader;
    }

    public final ItemsList getItems()
    {
        return this.fItems;
    }

    public final int getSpeed()
    {
        int result = this.fSpeed + this.SpeedMod;

        int num = this.fEffects.getCount();
        for (int i = 0; i < num; i++) {
            Effect eff = this.fEffects.getItem(i);
            EffectID effectID = EffectID.forValue(eff.CLSID);

            switch (effectID) {
                case eid_Speedup:
                    result += eff.Magnitude;
                    break;
                case eid_SpeedDown:
                    result -= eff.Magnitude;
                    break;
                case eid_Sail:
                    result += 40;
                    break;
                case eid_Lycanthropy:
                    result += 20;
                    break;
            }
        }

        /*if (GlobalVars.Debug_DevMode && this.isMercenary()) {
            result += 30;
        }*/

        if (result > 100) {
            Logger.write("error!!!");
        }
        
        return result;
    }

    public final int getAbility(AbilityID ID)
    {
        return this.fAbilities.getValue(ID.getValue());
    }

    public final void setAbility(AbilityID ID, int Value)
    {
        this.fAbilities.setValue(ID.getValue(), Value);
    }

    public final void clearAbilities()
    {
        this.fAbilities.clear();
    }

    public final int getSkill(SkillID ID)
    {
        return this.fSkills.getValue(ID.getValue());
    }

    public final void setSkill(SkillID ID, int Value)
    {
        this.fSkills.setValue(ID.getValue(), Value);
    }

    public final int getSkillsCount()
    {
        return this.fSkills.getCount();
    }

    public final void clearSkills()
    {
        this.fSkills.clear();
    }

    private boolean canBeDefeated(NWCreature enemy, Item weapon, Item projectile)
    {
        boolean omw = (enemy.fEntry.Flags.contains(CreatureFlags.esOnlyMagicWeapon));
        return !omw || (omw && ((weapon != null && (weapon.getEntry().Flags.contains(ItemFlags.if_MagicWeapon)) || (projectile != null && (projectile.getEntry().Flags.contains(ItemFlags.if_MagicWeapon))))));
    }

    private int getTrapListIndex(int trapID)
    {
        for (int i = 0; i < StaticData.dbTraps.length; i++) {
            if (StaticData.dbTraps[i].TileID == trapID) {
                return i;
            }
        }
        return -1;
    }

    public final int getDamageBase()
    {
        return AuxUtils.getBoundedRnd(this.DBMin, this.DBMax);
    }

    public final float getLeadership()
    {
        return (((float) (this.Strength + this.ArmorClass + this.Attacks + this.fSpeed) / 4.0f));
    }

    public final float getMaxItemsWeight()
    {
        float result = ((360.0f * (this.Strength / 18.0f))); // in original = 360
        return result;
    }

    // <editor-fold defaultstate="collapsed" desc="IEquippedCreature implementation">

    //@Override
    public final AbstractBody getBody()
    {
        return this.fBody;
    }

    //@Override
    public final void setBody(AbstractBody value)
    {
        if (this.fBody != null) {
            this.fBody.dispose();
        }
        this.fBody = value;
    }
    
    @Override
    public final int getMoney()
    {
        int result = 0;

        int num = this.fItems.getCount();
        for (int i = 0; i < num; i++) {
            Item item = this.fItems.getItem(i);
            if (item.CLSID == GlobalVars.iid_Coin) {
                result += (int) item.Count;
            }
        }

        return result;
    }

    public final void buy(Item item, NWCreature seller, boolean inShop)
    {
        this.getSpace().doEvent(EventID.event_Trade, null, null, null);

        int price = (int) item.getTradePrice(this, seller);

        if (this.isPlayer() && seller.fIsTrader) {
            ((Player) this).addDebt(seller.getName(), price);
        } else {
            if (this.fIsTrader && seller.isPlayer()) {
                int debt = ((Player) seller).getDebt(super.getName());
                if (debt == 0) {
                    this.subMoney(price);
                    seller.addMoney(price);
                } else {
                    if (debt >= price) {
                        ((Player) seller).subDebt(super.getName(), price);
                    } else {
                        int rem = price - debt;
                        ((Player) seller).subDebt(super.getName(), debt);
                        this.subMoney(rem);
                        seller.addMoney(rem);
                    }
                }
            } else {
                this.subMoney(price);
                seller.addMoney(price);
            }
        }

        if (inShop) {
            if (this.isPlayer()) {
                this.pickupItem(item);
            } else {
                seller.dropItem(item);
            }
        } else {
            seller.getItems().extract(item);
            this.addItem(item);
            if (this.fIsTrader) {
                ((TraderBrain) super.fBrain).setWareReturnGoal(item);
            }
        }
    }

    public final boolean canBeUsed(Item aItem)
    {
        boolean Result = false;
        if (this.fBody != null) {
            BodypartType kind = aItem.getEquipmentKind();
            if (kind != BodypartType.bp_None) {
                Result = (this.fBody.getUnoccupiedPart(kind.ordinal()) != null);
            }
        }
        return Result;
    }

    public final boolean canBuy(Item aItem, NWCreature aSeller)
    {
        boolean result = false;
        if (!this.isEnemy(aSeller)) {
            if (this.getMoney() >= (int) aItem.getTradePrice(this, aSeller)) {
                result = true;
            } else {
                this.getSpace().showText(this, Locale.getStr(RS.rs_NoMoney));
            }
        }
        return result;
    }

    public final boolean canTake(Item aItem, boolean isBuy)
    {
        boolean result;
        if (aItem.CLSID == GlobalVars.iid_Coin) {
            result = true;
        } else {
            float mWeight;
            if (!isBuy) {
                mWeight = 0f;
            } else {
                ItemEntry coinEntry = (ItemEntry) GlobalVars.nwrBase.findEntryBySign("Coin");
                mWeight = ((aItem.getPrice() * (float) coinEntry.Weight));
            }
            result = ((this.getTotalWeight() + aItem.getWeight() - mWeight) <= this.getMaxItemsWeight());
            if (!result) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_YouCannotCarryThatMuchWeight));
            }
        }
        return result;
    }

    public final void dropAll()
    {
        while (this.fItems.getCount() > 0) {
            Item it = this.fItems.getItem(0);
            it.setInUse(false);
            this.dropItem(it);
        }
    }

    public final float getTotalWeight()
    {
        float result = 0f;

        int num = this.fItems.getCount();
        for (int i = 0; i < num; i++) {
            result = ((result + this.fItems.getItem(i).getWeight()));
        }

        return result;
    }

    public final Item getItemByEquipmentKind(BodypartType kind)
    {
        int num = this.fItems.getCount();
        for (int idx = 0; idx < num; idx++) {
            Item item = this.fItems.getItem(idx);
            if (item.getInUse() && item.getEquipmentKind() == kind) {
                return item;
            }
        }

        return null;
    }

    public final void addItem(Item item)
    {
        item.Owner = this;
        this.fItems.add(item, true);
        //this.FItemsEnlarged = true;

        if (this.getAbility(AbilityID.Ab_Identification) > 0) {
            item.setIdentified(true);
        }
    }

    public final void deleteItem(Item item)
    {
        item.Owner = null;
        this.fItems.remove(item);
    }

    public final void dropItem(Item item)
    {
        this.fItems.extract(item);

        this.getSpace().dropItem(this.getCurrentField(), this, item, super.getPosX(), super.getPosY());
    }

    public final void pickupItem(Item item)
    {
        CreatureFlags cs = new CreatureFlags();
        if (this.fEntry.Race == RaceID.crHuman) {
            cs.include(CreatureFlags.esUseItems);
        } else {
            cs = this.fEntry.Flags;
        }

        if (!cs.contains(CreatureFlags.esUseItems)) {
            this.getSpace().showText(this, Locale.getStr(RS.rs_YouCannotCarryAnythingElse));
        } else {
            if (this.isInWater()) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_ItIsTooDeepHere));
            } else {
                Item fact_item;
                if (item.CLSID == GlobalVars.iid_Coin) {
                    float possible = ((this.getMaxItemsWeight() - this.getTotalWeight()));
                    if (possible >= item.getWeight()) {
                        fact_item = item;
                    } else {
                        int part_count = (int) Math.round((double) (possible / item.getEntry().Weight));
                        fact_item = item.getSeveral(part_count);
                        this.getSpace().showText(this, Locale.getStr(RS.rs_YouCannotCarryRest));
                    }
                } else {
                    fact_item = item;
                }

                this.pickupInternal(fact_item);
            }
        }
    }

    private void pickupInternal(Item item)
    {
        if (item != null) {
            this.getSpace().pickupItem(this.getCurrentField(), this, item);

            if (item.CLSID == GlobalVars.iid_DiamondNeedle) {
                this.setSkill(SkillID.Sk_PsiBlast, this.getSkill(SkillID.Sk_PsiBlast) + (int) item.Count);
                this.applyDamage(AuxUtils.getBoundedRnd(21, 28) * (int) item.Count, DamageKind.dkPhysical, null, "");
                item.dispose();
                int num = AuxUtils.getRandom(2);
                if (num != 0) {
                    if (num == 1) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_BurrowsIntoBrain));
                    }
                } else {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_BurrowsIntoSkull));
                }
            } else {
                this.addItem(item);
            }
        }
    }

    public void removeItem(Item item)
    {
        this.getSpace().doEvent(EventID.event_ItemRemove, this, null, item);
        if (this.fBody != null) {
            BodypartType eq = item.getEquipmentKind();
            if (eq != BodypartType.bp_None) {
                Bodypart bodyEntry = this.fBody.getOccupiedPart(item);
                if (bodyEntry != null) {
                    bodyEntry.Item = null;
                }
                if (item.isTwoHanded()) {
                    bodyEntry = this.fBody.getOccupiedPart(item);
                    if (bodyEntry != null) {
                        bodyEntry.Item = null;
                    }
                }
            }
        }
        item.setInUse(false);
    }

    public void wearItem(Item item)
    {
        this.getSpace().doEvent(EventID.event_ItemWear, this, null, item);
        if (this.fBody != null) {
            BodypartType eq = item.getEquipmentKind();
            if (eq != BodypartType.bp_None) {
                if (eq.getValue() >= BodypartType.bp_Legs.getValue() && eq.getValue() < BodypartType.bp_Finger.getValue()) {
                    if (this.fEffects.findEffectByID(EffectID.eid_LegsMissing) != null) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_YouCannotWearWithoutLegs));
                        return;
                    }
                }

                Bodypart bodyEntry = this.fBody.getUnoccupiedPart(eq.getValue());
                if (bodyEntry != null) {
                    bodyEntry.Item = item;
                } else {
                    if (eq == BodypartType.bp_Finger && item.getKind() == ItemKind.ik_Ring) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_YouCannotWearMoreRings));
                    }
                }

                if (item.isTwoHanded()) {
                    bodyEntry = this.fBody.getUnoccupiedPart(BodypartType.bp_LHand.getValue());
                    if (bodyEntry != null) {
                        bodyEntry.Item = item;
                    }
                }
            }
        }

        if (item.CLSID == GlobalVars.iid_Amulet_Eternal_Life) {
            this.getSpace().showText(this, Locale.getStr(RS.rs_YouCanNoLongerMove) + " " + Locale.getStr(RS.rs_YouMayNeverDie));
            this.death(Locale.getStr(RS.rs_BecameALivingStatue), null);
        } else {
            item.setInUse(true);
        }
    }

    public final void prepareItems()
    {
        int num = this.fItems.getCount();
        for (int i = 0; i < num; i++) {
            Item item = this.fItems.getItem(i);
            BodypartType ek = item.getEquipmentKind();
            if (ek != BodypartType.bp_None && !item.getInUse()) {
                Item dummyItem = this.getItemByEquipmentKind(ek);
                if (dummyItem == null) {
                    item.setInUse(true);
                } else {
                    if (dummyItem.compare(item) == ItemsCompareResult.icr_Better) {
                        dummyItem.setInUse(false);
                        item.setInUse(true);
                    }
                }
            }
        }

        //this.FItemsEnlarged = false;
    }

    public final void addMoney(int aCount)
    {
        Item item = new Item(this.fSpace, this);
        item.setCLSID(GlobalVars.iid_Coin);
        item.Count = (short) aCount;
        this.getItems().add(item, true);
    }

    public final void subMoney(int aCount)
    {
        int idx = -1;

        int num = this.getItems().getCount();
        for (int i = 0; i < num; i++) {
            if (this.getItems().getItem(i).CLSID == GlobalVars.iid_Coin) {
                idx = i;
                break;
            }
        }

        if (idx > -1) {
            Item item = this.getItems().getItem(idx);
            item.Count = (short) ((int) item.Count - aCount);
            if (item.Count <= 0) {
                this.deleteItem(item);
            }
        }
    }

    public final void pickupAll()
    {
        ExtList<LocatedEntity> items = ((NWField) this.getCurrentMap()).getItems().searchListByPos(super.getPosX(), super.getPosY());
        if (this.isPlayer() && items.getCount() < 1) {
            this.getSpace().showText(this, Locale.getStr(RS.rs_NothingHere));
        }

        int num = items.getCount();
        for (int i = 0; i < num; i++) {
            Item it = (Item) items.get(i);
            if (it.CLSID != GlobalVars.iid_DeadBody && it.CLSID != GlobalVars.iid_Mummy) {
                this.pickupItem(it);
            }
        }

        items.dispose();
    }

    // </editor-fold>
    
    public final String getRace()
    {
        String result;

        if (this.fEntry.Race == RaceID.crDefault) {
            result = this.fEntry.getName();
        } else {
            result = Locale.getStr(this.fEntry.Race.NameRS);
        }

        return result;
    }

    public final float getWeight()
    {
        return this.fEntry.Weight;
    }

    public final void setHPMax(int value)
    {
        this.HPMax = value;
        if (this.HPCur > this.HPMax) {
            this.HPCur = this.HPMax;
        }
    }

    public final void setIsTrader(boolean value)
    {
        if (this.fIsTrader != value) {
            this.fIsTrader = value;
            if (!this.fIsTrader) {
                this.init(super.CLSID, true, false);
            } else {
                this.init(GlobalVars.cid_Merchant, true, false);
            }
        }
    }

    public final void setLeadership(float value)
    {
    }

    public final void setInFog(boolean value)
    {
        try {
            if (this.fInFog != value) {
                this.fInFog = value;
                if (this.fSpace != null && this.isPlayer()) {
                    if (value) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_ThisFogIsVeryThick));
                    } else {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_YouCanSeeAgain));
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.setInFog(): " + ex.getMessage());
            throw ex;
        }
    }

    private AbilityID getArmorAbility()
    {
        AbilityID result = AbilityID.Ab_None;
        Item item = this.getItemByEquipmentKind(BodypartType.bp_Torso);
        if (item != null) {
            result = item.getKind().Ability;
        }
        return result;
    }

    private NWCreature findEnemyByAttack()
    {
        int nearestDist = StaticData.FieldWidth;
        NWCreature nearestAttacker = null;

        GameEvent evt = this.getSpace().peekEvent(this, EventID.event_Attack, true);
        while (evt != null) {
            NWCreature enemy = (NWCreature) evt.Sender;
            int dist = AuxUtils.distance(super.getLocation(), enemy.getLocation());

            if (dist < nearestDist) {
                nearestDist = dist;
                nearestAttacker = enemy;
            }

            evt.dispose();
            evt = this.getSpace().peekEvent(this, EventID.event_Attack, true);
        }

        return nearestAttacker;
    }

    private NWCreature getDefaultEnemy()
    {
        NWCreature result = null;

        if (super.CLSID == GlobalVars.cid_Heimdall) {
            result = this.getSpace().findCreature(GlobalVars.cid_Loki);
        } else if (super.CLSID == GlobalVars.cid_Thor) {
            result = this.getSpace().findCreature(GlobalVars.cid_Jormungand);
        } else if (super.CLSID == GlobalVars.cid_Tyr) {
            result = this.getSpace().findCreature(GlobalVars.cid_Garm);
        } else if (super.CLSID == GlobalVars.cid_Freyr) {
            result = this.getSpace().findCreature(GlobalVars.cid_Surtr);
        } else if (super.CLSID == GlobalVars.cid_Odin) {
            result = this.getSpace().findCreature(GlobalVars.cid_Fenrir);
        } else if (super.CLSID == GlobalVars.cid_Jormungand) {
            result = this.getSpace().findCreature(GlobalVars.cid_Thor);
        } else if (super.CLSID == GlobalVars.cid_Garm) {
            result = this.getSpace().findCreature(GlobalVars.cid_Tyr);
        } else if (super.CLSID == GlobalVars.cid_Loki) {
            result = this.getSpace().findCreature(GlobalVars.cid_Heimdall);
        } else if (super.CLSID == GlobalVars.cid_Surtr) {
            result = this.getSpace().findCreature(GlobalVars.cid_Freyr);
        } else if (super.CLSID == GlobalVars.cid_Fenrir) {
            result = this.getSpace().findCreature(GlobalVars.cid_Odin);
        }

        if (result != null && !this.getCurrentMap().equals(result.getCurrentMap())) {
            return null;
        }

        return result;
    }

    private void postDeath_SetTile(int tileID)
    {
        NWField map = (NWField) this.getCurrentMap();
        map.getTile(super.getPosX(), super.getPosY()).setBack(tileID);
        map.normalize();
    }

    private void checkHealth_Solve(EffectID effectID, String problem, RefObject<Boolean> refTurnUsed)
    {
        Item item = this.canCure(effectID);
        if (item != null) {
            refTurnUsed.argValue = true;
            this.useItem(item, null);
        } else {
            if (this.isMercenary()) {
                String msg = Locale.format(RS.rs_MercenaryProblem, new Object[]{problem});
                this.getSpace().showText(this, msg);
            }
        }
    }

    private void gout()
    {
        AuxUtils.exStub("The ");
        AuxUtils.exStub(" spews liquid.");
        AuxUtils.exStub("The liquid burns you.");
        AuxUtils.exStub("Scalded by unholy water");
        AuxUtils.exStub("You dodge the stream.");
    }

    private void takeItemDef(int iid, String message)
    {
        NWCreature cr = this.getSpace().getPlayer();

        Item it = (Item) cr.getItems().findByCLSID(iid);
        if (AuxUtils.distance(super.getLocation(), cr.getLocation()) <= 3 && it != null) {
            if (super.CLSID == GlobalVars.cid_Hela && it.Bonus != GlobalVars.cid_Thokk) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_HelaIsNotImpressed), new LogFeatures(LogFeatures.lfDialog));
            } else {
                if (message.compareTo("") == 0) {
                    message = Locale.format(RS.rs_ItemTaked, new Object[]{super.getName(), it.getName()});
                }
                this.getSpace().showText(this, message, new LogFeatures(LogFeatures.lfDialog));

                it.setInUse(false);
                cr.getItems().extract(it);
                this.fItems.add(it, false);

                if (super.CLSID == GlobalVars.cid_Hela) {
                    this.getSpace().addCreatureEx(GlobalVars.Layer_Asgard, 1, 0, AuxUtils.getBoundedRnd(29, 68), AuxUtils.getBoundedRnd(3, StaticData.FieldHeight - 4), GlobalVars.cid_Balder);
                } else {
                    if (it.CLSID == GlobalVars.iid_Gjall) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_HeimdallUseGjall));
                        this.useItem(it, null);
                    } else {
                        it.setInUse(true);
                    }
                }
            }
        }
    }

    protected final void doDefaultAction()
    {
        try {
            if (super.CLSID == GlobalVars.cid_Hela) {
                this.takeItemDef(GlobalVars.iid_SoulTrapping_Ring, Locale.getStr(RS.rs_ThokkTaked));
            } else {
                if (super.CLSID == GlobalVars.cid_Heimdall) {
                    this.takeItemDef(GlobalVars.iid_Gjall, "");
                } else {
                    if (super.CLSID == GlobalVars.cid_Thor) {
                        this.takeItemDef(GlobalVars.iid_Mjollnir, "");
                    } else {
                        if (super.CLSID == GlobalVars.cid_Tyr) {
                            this.takeItemDef(GlobalVars.iid_DwarvenArm, "");
                        } else {
                            if (super.CLSID == GlobalVars.cid_Freyr) {
                                this.takeItemDef(GlobalVars.iid_Mimming, "");
                            } else {
                                if (super.CLSID == GlobalVars.cid_Odin) {
                                    this.takeItemDef(GlobalVars.iid_Gungnir, "");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.doDefaultAction(): " + ex.getMessage());
        }
    }

    public boolean isMercenary()
    {
        return this.fIsMercenary;
    }

    public Movements getMovements()
    {
        Movements result = new Movements();

        if (this.fEntry.Flags.contains(CreatureFlags.esWalking)) {
            result.include(Movements.mkWalk);
        }
        if (this.fEntry.Flags.contains(CreatureFlags.esSwimming)) {
            result.include(Movements.mkSwim);
        }
        if (this.fEntry.Flags.contains(CreatureFlags.esFlying)) {
            result.include(Movements.mkFly);
        }

        if (this.fEffects.findEffectByID(EffectID.eid_LegsMissing) != null) {
            result.exclude(Movements.mkWalk);
        }

        if (this.getAbility(AbilityID.Ab_Swimming) > 0 || this.fEffects.findEffectByID(EffectID.eid_Sail) != null) {
            result.include(Movements.mkSwim);
        }

        if (this.getAbility(AbilityID.Ab_Levitation) > 0) {
            result.include(Movements.mkFly);
        }

        if (this.fEffects.findEffectByID(EffectID.eid_Phase) != null) {
            result.include(Movements.mkEthereality);
        }

        return result;
    }

    @Override
    public String getName()
    {
        String result;

        if (this.Ghost) {
            result = Locale.getStr(RS.rs_Ghost) + " " + this.fName;
        } else if (this.Illusion) {
            result = Locale.getStr(RS.rs_Illusion) + " " + this.fName;
        } else {
            RaceID race = this.fEntry.Race;
            if (race == RaceID.crHuman) {
                if (this.isPlayer()) {
                    result = this.fName;
                } else {
                    result = this.fEntry.getName() + " " + this.fName;
                }
            } else {
                result = this.fEntry.getName();
            }
        }

        return result;
    }

    @Override
    public byte getSerializeKind()
    {
        return StaticData.SID_CREATURE;
    }

    public SymbolID getSymbol()
    {
        return SymbolID.sid_None;
    }

    protected void initBody()
    {
        try {
            RaceID race = this.fEntry.Race;

            if (race == RaceID.crHuman) {
                this.setBody(new HumanBody(this));
            } else {
                this.setBody(new CustomBody(this));
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.initBody(): " + ex.getMessage());
            throw ex;
        }
    }

    protected void initBrain()
    {
        try {
            if (this.Prowling) {
                super.fBrain = new BeastBrain(this);
                return;
            }

            if (super.CLSID == GlobalVars.cid_Agnar || super.CLSID == GlobalVars.cid_Haddingr || super.CLSID == GlobalVars.cid_Ketill) {
                super.setBrain(new VictimBrain(this));
            } else if (super.CLSID == GlobalVars.cid_Eitri) {
                super.setBrain(new EitriBrain(this));
            } else if (super.CLSID == GlobalVars.cid_Raven) {
                super.setBrain(new RavenBrain(this));
            } else if (super.CLSID == GlobalVars.cid_Guardsman || super.CLSID == GlobalVars.cid_Jarl) {
                super.setBrain(new WarriorBrain(this));
            } else if (this.fEntry.Flags.contains(CreatureFlags.esMind)) {
                if (this.fIsTrader) {
                    super.setBrain(new TraderBrain(this));
                } else {
                    super.setBrain(new SentientBrain(this));
                }
            } else {
                super.setBrain(new BeastBrain(this));
            }

            if (super.fBrain instanceof BeastBrain && (this.fEntry.Flags.contains(CreatureFlags.esCohorts))) {
                ((BeastBrain) super.fBrain).Flock = true;
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.initBrain(): " + ex.getMessage());
            throw ex;
        }
    }

    protected final void recovery()
    {
        if (this.HPCur < this.HPMax && this.fEffects.findEffectByID(EffectID.eid_Withered) == null) {
            float str = ((float) this.Strength);
            float life_recovery = 0.25f;
            int mag = 0;

            int num = this.fEffects.getCount();
            for (int i = 0; i < num; i++) {
                if (this.fEffects.getItem(i).CLSID == EffectID.eid_Regeneration.getValue()) {
                    mag += 60;
                }
            }

            float res = (((str - 8.0f) * life_recovery * (1.0f + (float) mag / 100.0f)));
            if ((this.HPCur + (double) res) > this.HPMax) {
                this.HPCur = this.HPMax;
            } else {
                this.HPCur = (int) (this.HPCur + ((long) Math.round((double) res)));
            }
        }

        if (this.MPCur < this.MPMax && GlobalVars.Debug_ManaRegen) {
            //float temp = ((float) this.Perception);
        }
    }

    public final AbstractMap getCurrentMap()
    {
        if (this.fSpace == null) {
            return null;
        } else {
            return this.getSpace().getField(this.LayerID, this.fField.X, this.fField.Y);
        }
    }

    public final NWField getCurrentField()
    {
        if (this.fSpace == null) {
            return null;
        } else {
            return this.getSpace().getField(this.LayerID, this.fField.X, this.fField.Y);
        }
    }

    public final void setGlobalPos(int layerID, int fieldX, int fieldY, boolean obligatory)
    {
        try {
            if (this.LayerID != layerID || this.fField.X != fieldX || this.fField.Y != fieldY || obligatory) {
                LayerEntry eLayer = (LayerEntry) GlobalVars.nwrBase.getEntry(layerID);

                if (fieldX >= eLayer.W) {
                    fieldX = 0;
                }
                if (fieldY >= eLayer.H) {
                    fieldY = 0;
                }

                this.leaveField();
                this.LayerID = layerID;
                this.fField = new Point(fieldX, fieldY);
                NWField fld = this.getSpace().getField(this.LayerID, this.fField.X, this.fField.Y);
                this.enterField(fld);

                if (this.isPlayer()) {
                    fld.Visited = true;
                    int nextLand = fld.LandID;
                    this.knowIt(nextLand);
                    this.getSpace().doEvent(EventID.event_LandEnter, this, null, GlobalVars.nwrBase.getEntry(nextLand));
                }
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.setGlobalPos(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void enterField(NWField field)
    {
        try {
            field.getCreatures().add(this);

            if (this.isPlayer()) {
                if (field.LandID == GlobalVars.Land_Bifrost) {
                    int eid = GlobalVars.nwrBase.findEntryBySign("Edgewort").GUID;
                    field.addCreature(-1, -1, eid);
                    field.addCreature(-1, -1, eid);
                    field.addCreature(-1, -1, eid);
                    field.addCreature(-1, -1, eid);
                }

                if (field.LandID == GlobalVars.Land_Ocean && this.getSpace().rt_Jormungand != null) {
                    this.getSpace().rt_Jormungand.transferTo(field.getLayer().EntryID, field.getCoords().X, field.getCoords().Y, -1, -1, StaticData.MapArea, true, false);
                }

                if (field.LandID == GlobalVars.Land_Crossroads) {
                    // dummy
                }

                field.setAmbient();
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.enterField(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void leaveField()
    {
        NWField field = this.getCurrentField();
        if (field != null) {
            field.getCreatures().extract(this);
        }
    }

    public final void setIsMercenary(boolean value)
    {
        if (this.fIsMercenary != value) {
            this.fIsMercenary = value;
            this.resetMercenary(value);
        }
    }

    private void resetMercenary(boolean value)
    {
        Player player = this.getSpace().getPlayer();
        LeaderBrain leader = ((LeaderBrain) player.fBrain);

        if (value) {
            boolean res = leader.addMember(this);
            if (res) {
                super.fBrain = new WarriorBrain(this);
                ((WarriorBrain) super.fBrain).setEscortGoal(player, true);
            }
        } else {
            leader.removeMember(this);
        }
    }
    
    @Override
    public void setName(String value)
    {
        RaceID race = this.fEntry.Race;

        if (race == RaceID.crHuman) {
            if (!TextUtils.equals(this.fName, value)) {
                this.fName = value;
            }
        }
    }

    public final CreatureState getState()
    {
        return this.fState;
    }

    public final void setState(CreatureState value)
    {
        this.fState = value;

        switch (this.fState) {
            case csAlive:
            case csUndead:
                this.checkTile(true);
                break;

            case csDead:
                this.checkTile(false);
                break;
        }
    }

    public byte getSurvey()
    {
        return this.FSurvey;
    }

    public void setSurvey(byte value)
    {
        this.FSurvey = value;
    }

    public final int getExperience()
    {
        return this.fExperience;
    }

    public final void setExperience(int value)
    {
        // TODO: отработать потерю уровня от проклятого зелья опыта

        try {
            this.fExperience = value;
            int exp = this.getNextLevelExp();
            if (this.fExperience >= exp) {
                this.Level++;
                this.HPMax += AuxUtils.getBoundedRnd(7, 11);
                this.HPCur = this.HPMax;

                if (this.isPlayer()) {
                    this.getSpace().doEvent(EventID.event_LevelUp, this, null, null);
                }
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.setExperience(): " + ex.getMessage());
        }
    }

    private int getNextLevelExp()
    {
        int result;
        try {
            int lev = this.Level + 1;
            result = (int) ((Math.round((Math.pow(2.0, (double) lev) + Math.pow((double) lev, 4.0)))) + (25 * lev));
        } catch (Exception ex) {
            result = 0;
            Logger.write("NWCreature.getNextLevelExp(): " + ex.getMessage());
        }
        return result;
    }

    public final void applyDamage(int damage, DamageKind damageKind, Object source, String deathMsg)
    {
        try {
            if (this.isPlayer() && GlobalVars.Debug_Divinity) {
                return;
            }

            if (damageKind == DamageKind.dkRadiation && this.getAbility(AbilityID.Ab_RayAbsorb) > 0) {
                if (this.isPlayer()) {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_YouAbsorbEnergy));
                }
                this.HPCur += damage;
            } else {
                int newHP = this.HPCur - damage;

                if (newHP <= 0) {
                    Effect immort_ef = this.fEffects.findEffectByID(EffectID.eid_Immortal);
                    if (immort_ef != null) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_SecondLife));
                        this.fEffects.remove(immort_ef);
                    } else {
                        this.death(deathMsg, source);
                        this.getSpace().doEvent(EventID.event_Killed, this, null, source);
                    }
                } else {
                    this.HPCur -= damage;
                    this.getSpace().doEvent(EventID.event_Wounded, this, null, source);
                }
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.applyDamage(): " + ex.getMessage());
            throw ex;
        }
    }

    private void applySpecialItems()
    {
        try {
            GameEntity it = this.fItems.findByCLSID(GlobalVars.iid_Lodestone);
            if (it != null) {
                this.useEffect(EffectID.eid_Lodestone, it, InvokeMode.im_Use, null);
            }

            it = this.fItems.findByCLSID(GlobalVars.nwrBase.findEntryBySign("BlueCube").GUID);
            if (it != null) {
                this.useEffect(EffectID.eid_Cube, it, InvokeMode.im_Use, null);
            }

            it = this.fItems.findByCLSID(GlobalVars.nwrBase.findEntryBySign("GreyCube").GUID);
            if (it != null) {
                this.useEffect(EffectID.eid_Cube, it, InvokeMode.im_Use, null);
            }

            it = this.fItems.findByCLSID(GlobalVars.nwrBase.findEntryBySign("OrangeCube").GUID);
            if (it != null) {
                this.useEffect(EffectID.eid_Cube, it, InvokeMode.im_Use, null);
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.applySpecialItems(): " + ex.getMessage());
        }
    }

    protected AttackInfo calcAttackInfo(AttackKind attackKind, NWCreature enemy, Item weapon, Item projectile)
    {
        AttackInfo result = new AttackInfo();

        try {
            boolean phase = this.fEffects.findEffectByID(EffectID.eid_Phase) != null;
            int pHit;
            int damage;

            if (projectile == null) {
                pHit = this.ToHit;
                damage = this.getDamageBase();
            } else {
                pHit = (int) (Math.round((this.Strength / 7.0 - 2 * enemy.ArmorClass + this.Luck / 10.0 + 30.0 + (double) projectile.Bonus)));

                if (projectile.getEntry().Sign.compareTo("FlintKnife") == 0) {
                    pHit += 5;
                } else {
                    if (projectile.CLSID == GlobalVars.iid_Mjollnir) {
                        pHit += 80 + projectile.Bonus * 3;
                    } else {
                        if (projectile.CLSID == GlobalVars.iid_Gungnir) {
                            pHit += (int) (125 + (Math.round(this.getAbility(AbilityID.Ab_Spear) / 2.0)));
                        } else {
                            if (projectile.getEntry().Sign.compareTo("CrudeSpear") == 0) {
                                pHit += (int) (8 + (Math.round((double) projectile.Bonus / 3.0)));
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

                damage = (int) ((projectile.getDamage() + projectile.Bonus) + (Math.round((this.Strength / 14.0 + this.Luck / 40.0 + this.Level / 6.0))));
            }

            CreatureAction act = attackKind.Action;
            AbilityID abil = this.getActionAbility(act, weapon);

            int parry = (int) (Math.round(enemy.getAbility(AbilityID.Ab_Parry) / 10.0f));
            pHit = (int) (pHit + (Math.round((this.getAbility(abil) / 10.0f * act.Factor))) - parry);

            if (!this.checkVisible(enemy)) {
                pHit -= AuxUtils.getBoundedRnd(5, 8);
            }

            if (this.Strength > 18) {
                pHit += 4 * (this.Strength - 18);
            }
            if (phase) {
                pHit -= 40;
            }

            pHit = Math.abs(pHit);
            if (GlobalVars.Debug_Divinity && this.isPlayer()) {
                pHit = 100;
            }
            if (enemy.CLSID == GlobalVars.cid_Scyld && this.fItems.findByCLSID(GlobalVars.iid_GreenStone) == null) {
                pHit = 0;
            }

            if (damage < 0) {
                damage = 0;
            }
            if (enemy.fEffects.findEffectByID(EffectID.eid_Fragile) != null) {
                damage <<= 1;
            }
            if (enemy.fEffects.findEffectByID(EffectID.eid_Invulnerable) != null) {
                damage = 0;
            }
            
            result.Damage = damage;
            result.ToHit = pHit;
            result.Parry = parry;
            result.Action = act;
        } catch (Exception ex) {
            Logger.write("NWCreature.calcAttackInfo(): " + ex.getMessage());
        }

        return result;
    }

    public final Item getPrimaryWeapon()
    {
        return this.getItemByEquipmentKind(BodypartType.bp_RHand);
    }

    public boolean attackTo(AttackKind attackKind, NWCreature enemy, Item weapon, Item projectile)
    {
        enemy.LastAttacker = this;
        if (weapon == null) {
            weapon = this.getPrimaryWeapon();
        }

        try {
            this.getSpace().doEvent(EventID.event_Attack, this, enemy, this);

            if (!this.canBeDefeated(enemy, weapon, projectile)) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_ThisCreatureCanOnlyBeHitWithMagicalWeapons));
                return false;
            }

            this.attackSpecialEffect(enemy);

            AttackInfo attackInfo = this.calcAttackInfo(attackKind, enemy, weapon, projectile);

            if (!AuxUtils.chance(attackInfo.ToHit)) {
                if (enemy.CLSID == GlobalVars.cid_Scyld) {
                    this.getSpace().showText(this, Locale.format(RS.rs_YouTickle, new Object[]{enemy.getDeclinableName(Number.nSingle, Case.cAccusative)}));
                }

                this.getSpace().doEvent(EventID.event_Miss, this, null, null);

                if (AuxUtils.chance(attackInfo.ToHit - attackInfo.Parry)) {
                    enemy.checkActionAbility(CreatureAction.caAttackParry, null);
                }
                return false;
            } else {
                this.getSpace().doEvent(EventID.event_Hit, this, enemy, null);
                // FIXME: влияние навыка доспеха врага на урон
                int hp = (int) (enemy.HPCur - (Math.round((double) attackInfo.Damage)));

                String msg = "";
                if (hp <= 0) {
                    if (enemy.isPlayer()) {
                        msg = this.getDeclinableName(Number.nSingle, Case.cInstrumental);
                        msg = Locale.format(RS.rs_Rsn_KilledByEnemy, new Object[]{msg});
                    }
                } else {
                    msg = "";
                    AbilityID abil = enemy.getArmorAbility();
                    if (abil != AbilityID.Ab_None) {
                        enemy.setAbility(abil, enemy.getAbility(abil) + 1);
                    }
                }

                enemy.applyDamage(attackInfo.Damage, DamageKind.dkPhysical, this, msg);

                if (enemy.getState() == CreatureState.csDead) {
                    int xp = this.getAttackExp(enemy);
                    this.setExperience(this.getExperience() + xp);

                    this.knowIt(enemy.CLSID);
                }

                this.checkActionAbility(attackInfo.Action, weapon);

                return true;
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.attackTo(): " + ex.getMessage());
            return false;
        }
    }

    public final void attackSpecialEffect(NWCreature enemy)
    {
        // TODO: all

        String sign = this.fEntry.Sign;
        if (sign.compareTo("Anssk") != 0) {
            if (sign.equals("Anxarcule")) {
                AuxUtils.exStub(" pulverizes your legs and devours them! ");
                AuxUtils.exStub(" rips your weapon from your grasp! ");
                AuxUtils.exStub("s");
                AuxUtils.exStub(" attempts to");
                AuxUtils.exStub(" summon");
                AuxUtils.exStub(" your double from an ");
                AuxUtils.exStub("alternate universe to fight yourself.");
            } else if (sign.equals("Aspenth")) {
                AuxUtils.exStub(" opens your mouth with his tentacles.");
                AuxUtils.exStub("You begin to choke on water.");
                AuxUtils.exStub(" bites your abdomen.");
            } else if (sign.equals("Breleor")) {
                AuxUtils.exStub(" (????, can be not his)");
                AuxUtils.exStub("The %s sends you away.");
                AuxUtils.exStub("Tendrils tunnel into your skin.");
                AuxUtils.exStub("A tendril reaches your heart.");
                AuxUtils.exStub("Pierced by a' (on death)");
                AuxUtils.exStub("It feels strange.' (if tendril not kill)");
            } else if (sign.equals("Cockatrice")) {
                AuxUtils.exStub("The %s is turned to stone.");
            } else if (sign.equals("Dreg")) {
                AuxUtils.exStub("???");
            } else if (sign.equals("Edgewort")) {
                AuxUtils.exStub(" pushes");
                AuxUtils.exStub(" pulls");
                AuxUtils.exStub("The ");
                AuxUtils.exStub(" you toward your death.");
                AuxUtils.exStub(" hurls you to your death.");
                AuxUtils.exStub("Thrown from Bifrost+");
            } else if (sign.equals("Emanon")) {
                AuxUtils.exStub("s hair stands on end.");
                AuxUtils.exStub(" are");
                AuxUtils.exStub(" is");
                AuxUtils.exStub("Your ");
                AuxUtils.exStub("stripped off.");
                AuxUtils.exStub("Your weapon is knocked from your grasp.");
                AuxUtils.exStub(" opens a gate.");
                AuxUtils.exStub("His minions surge through! ");
                AuxUtils.exStub("You feel worried.");
            } else if (sign.equals("Enchantress")) {
                AuxUtils.exStub("The %s is stunned.");
                this.getSpace().showText(enemy, Locale.format(RS.rs_SongCharmsYou, new Object[]{this.getDeclinableName(Number.nSingle, Case.cGenitive)}));
            } else if (sign.equals("GasBall")) {
                AuxUtils.exStub(" explodes.");
                AuxUtils.exStub("It deafens you.");
                AuxUtils.exStub("todo: +deafness");
            } else if (sign.equals("Ghost")) {
                int num = AuxUtils.getRandom(3);
                if (num != 0) {
                    if (num != 1) {
                        if (num == 2) {
                            this.getSpace().showText(enemy, Locale.getStr(RS.rs_YouQuiverInFear));
                        }
                    } else {
                        this.getSpace().showText(enemy, Locale.getStr(RS.rs_YouFleeInTerror));
                    }
                } else {
                    this.getSpace().showText(enemy, Locale.getStr(RS.rs_YouOvercomeWithFear));
                }
            } else if (sign.equals("GiantSquid")) {
                AuxUtils.exStub("The %s is drowned.");
            } else if (sign.equals("Gorm")) {
                this.getSpace().showText(enemy, Locale.getStr(RS.rs_LongSleepBite));
                AuxUtils.exStub("rs_EternitySleepBite");
                AuxUtils.exStub("rs_YouAwakeDisoriented");
            } else if (sign.equals("Gulveig")) {
                AuxUtils.exStub(" opens a gate.");
                AuxUtils.exStub("s minions surge through! ");
                AuxUtils.exStub(" summons his worshippers.");
                AuxUtils.exStub(" slides a tentacle into your flesh.");
                AuxUtils.exStub("A chill sweeps through you.");
                AuxUtils.exStub("s tentacles miss you.");
                AuxUtils.exStub("+ Gout()");
            } else if (sign.equals("Hatchetfish")) {
                AuxUtils.exStub("The razor-sharp teeth of the ");
                AuxUtils.exStub(" rip ");
                AuxUtils.exStub("into ");
                AuxUtils.exStub(".");
                this.getSpace().showText(enemy, Locale.format(RS.rs_TeethRipIntoFlesh, new Object[]{this.getDeclinableName(Number.nSingle, Case.cGenitive)}));
            } else if (sign.equals("Hela")) {
                AuxUtils.exStub(" rises her domain against you. (rp_satt5)");
                AuxUtils.exStub(" calls out to' (may be not she)");
                AuxUtils.exStub(" the souls of those you have slain in combat.' (may be not she)");
            } else if (sign.equals("Iridorn")) {
                AuxUtils.exStub("The %s's head is torn off.");
                AuxUtils.exStub("The ");
                AuxUtils.exStub(" smiles ");
                AuxUtils.exStub("as it tears your head off with its talons.");
                AuxUtils.exStub("Beheaded");
                AuxUtils.exStub("s talons scratch against your gorget.");
                AuxUtils.exStub(" misses your throat.");
            } else if (sign.equals("IvyCreeper")) {
                AuxUtils.exStub("rs_YouAreBeingCrushed");
            } else if (sign.equals("Jagredin")) {
                AuxUtils.exStub("Your flesh burns.");
                AuxUtils.exStub("You are not affected.");
            } else if (sign.equals("KnellBird")) {
                AuxUtils.exStub("Your mind reels with pain! ");
                AuxUtils.exStub("n");
                AuxUtils.exStub("Pierced by the gaze of a");
                AuxUtils.exStub(" ");
                AuxUtils.exStub("The ");
                AuxUtils.exStub(" dies.");
                AuxUtils.exStub("Something");
                AuxUtils.exStub(" screams in pain! ");
            } else if (sign.equals("KonrRig")) {
                AuxUtils.exStub(" probes your mind. (rp_satt5)");
                AuxUtils.exStub("rs_YouBecomesInsane");
            } else if (sign.equals("Magician")) {
                this.getSpace().showText(enemy, Locale.format(RS.rs_XGesticulates, new Object[]{super.getName()}));
                this.getSpace().showText(enemy, Locale.getStr(RS.rs_YourPackQuivers));
            } else if (sign.equals("Minion")) {
                AuxUtils.exStub("The creatures of Niflheim subdue you! ");
            } else if (sign.equals("Moleman")) {
                AuxUtils.exStub("The %s collapses the ceiling on %s.");
            } else if (sign.equals("Nidhogg")) {
                AuxUtils.exStub(" absorbs some of your invulnerability.");
                AuxUtils.exStub(" barks. Lackeys appear.");
                AuxUtils.exStub("killhim");
                AuxUtils.exStub("White dust fills the air around you.");
                AuxUtils.exStub("You begin to hallucinate.");
                AuxUtils.exStub("You are momentarily stunned.");
                AuxUtils.exStub("You can move again.");
                AuxUtils.exStub(" wills you to wield a new weapon.");
                AuxUtils.exStub("You drop your weapon.");
                AuxUtils.exStub("You wield nothing.");
                AuxUtils.exStub("You now wield ");
                AuxUtils.exStub(".");
                AuxUtils.exStub("You resist ");
                AuxUtils.exStub("s will.");
                AuxUtils.exStub(" siphons off the effects of your potion.");
                AuxUtils.exStub("A tendril from ");
                AuxUtils.exStub("s body probes your spine.");
                AuxUtils.exStub("You feel slower.");
                AuxUtils.exStub(" belches smoke.");
                AuxUtils.exStub("You hear a strange whine inside your head.");
                AuxUtils.exStub("hel ration");
                AuxUtils.exStub("Your pack shifts, as if restless.");
            } else if (sign.equals("Nidslacr")) {
                this.getSpace().showText(enemy, Locale.getStr(RS.rs_YouFeelTingling));
                AuxUtils.exStub("Your eyes recede.");
                AuxUtils.exStub("You can no longer levitate.");
                AuxUtils.exStub("You can no longer control others.");
                AuxUtils.exStub("Your power to identify objects is gone.");
                AuxUtils.exStub("You no longer remember the art of alchemy.");
                AuxUtils.exStub("You have lost the ability to swim.");
                AuxUtils.exStub("You can no longer teleport at will.");
            } else if (sign.equals("Pelgrat")) {
                AuxUtils.exStub("s");
                AuxUtils.exStub("The ");
                AuxUtils.exStub(" draws charges from your ");
                AuxUtils.exStub("wand");
                AuxUtils.exStub(" and grows stronger.");
                AuxUtils.exStub("It seems to have changed its shape slightly.");
            } else if (sign.equals("PhantomAsp")) {
                AuxUtils.exStub("The %s is poisoned.");
            } else if (sign.equals("Plog")) {
                AuxUtils.exStub("+ Gout()");
            } else if (sign.equals("Preden")) {
                enemy.useEffect(EffectID.eid_Fever, null, InvokeMode.im_Use, null);
            } else if (sign.equals("PyrtaAth")) {
                AuxUtils.exStub("The ");
                AuxUtils.exStub(" suffocates you! ");
                AuxUtils.exStub("You can no longer breathe.");
                AuxUtils.exStub("Smothered by a");
                AuxUtils.exStub(" ");
                AuxUtils.exStub(" engulfs your head! ");
            } else if (sign.equals("RedOoze")) {
                AuxUtils.exStub("The %s is consumed.");
                this.getSpace().showText(enemy, Locale.getStr(RS.rs_YouCannotBreakFree));
                AuxUtils.exStub("rs_YourFleshBeginsToMelt      = 1094;");
                AuxUtils.exStub("rs_YouEscapeGrasp             = 1095;");
                AuxUtils.exStub("rs_YouAwakeDisoriented        = 1096;");
                AuxUtils.exStub("??? > The red ooze consumes your mace.");
                AuxUtils.exStub("??? > The red ooze begins consume you.");
            } else if (sign.equals("Retchweed")) {
                this.getSpace().showText(enemy, Locale.getStr(RS.rs_NauseaBrings));
            } else if (sign.equals("Roc")) {
                AuxUtils.exStub("The / grabs the /, /soars high into the air, and hurls it down.");
            } else if (sign.equals("Scyld")) {
                AuxUtils.exStub("Horrible rays emanate from his eyes! ");
                AuxUtils.exStub("The rays hit you.");
                AuxUtils.exStub("The rays miss.");
                AuxUtils.exStub(" ignores you.");
                AuxUtils.exStub(" claps.");
                AuxUtils.exStub("You are hurled back by a shock wave.");
                AuxUtils.exStub("You collide with ");
                AuxUtils.exStub("the ");
                AuxUtils.exStub(".");
                AuxUtils.exStub("a");
                AuxUtils.exStub("Smashed into");
                AuxUtils.exStub(" ");
                AuxUtils.exStub("The ");
                AuxUtils.exStub(" is killed.");
                AuxUtils.exStub("pile of rubble");
                AuxUtils.exStub("tree");
                AuxUtils.exStub("rock");
                AuxUtils.exStub("wall");
                AuxUtils.exStub(" the ");
                AuxUtils.exStub("You slam into");
                AuxUtils.exStub("You bounce off of");
                AuxUtils.exStub("Smashed into a wall");
                AuxUtils.exStub(" exhales.");
                AuxUtils.exStub("The temperature surrounding him approaches ");
                AuxUtils.exStub("absolute zero.");
                AuxUtils.exStub("turned to ice");
                AuxUtils.exStub(" teleports you far away.");
            } else if (sign.equals("Slinn")) {
                AuxUtils.exStub("+ Gout();");
            } else if (sign.equals("Slywert")) {
                AuxUtils.exStub(" shifts in a glint of light.");
                AuxUtils.exStub(" whispers your name.");
                AuxUtils.exStub(" snacks on a small creature.");
                this.getSpace().showText(enemy, Locale.format(RS.rs_FlashesToothyGrin, new Object[]{super.getName()}));
            } else if (sign.equals("Spirit")) {
                AuxUtils.exStub("The %s drains %s.");
                AuxUtils.exStub("The ");
                AuxUtils.exStub(" drains you! ");
                AuxUtils.exStub("You are drained.");
                AuxUtils.exStub("s touch paralyzes you.");
                AuxUtils.exStub("You are paralyzed.");
            } else if (sign.equals("Summoner")) {
                this.getSpace().showText(enemy, Locale.format(RS.rs_XZapsWand, new Object[]{super.getName()}));
            } else if (sign.equals("<terrain>")) {
                AuxUtils.exStub("todo: lava and ?");
                AuxUtils.exStub(" almost");
                AuxUtils.exStub("The %s drowns you.");
                AuxUtils.exStub("Drowned by a ");
                AuxUtils.exStub("Burned by animated lava");
                AuxUtils.exStub("rs_LavaEncasesYou");
                AuxUtils.exStub("rs_EncasedInVolcanicRock");
            } else if (sign.equals("Vampire")) {
                AuxUtils.exStub("The %s drains %s.");
                AuxUtils.exStub(" The vampire drains you. You lose a life level.");
            } else if (sign.equals("Vanisher")) {
                AuxUtils.exStub("You begin to disappear.");
            } else if (sign.equals("Wier")) {
                AuxUtils.exStub("Greed overwhelms you.");
                AuxUtils.exStub("You sit to count your gold.");
            } else if (sign.equals("Wight")) {
                enemy.addEffect(EffectID.eid_Withered, ItemState.is_Normal, EffectAction.ea_Persistent, false, Locale.format(RS.rs_TouchWithersYou, new Object[]{this.getDeclinableName(Number.nSingle, Case.cGenitive)}));
            } else if (sign.equals("Wizard")) {
                AuxUtils.exStub("rs_YouAreFrozen");
                AuxUtils.exStub("nwrWin.ShowText(aEnemy, rsList(rs_YouHearPowerfulSpell]);");
                AuxUtils.exStub("nwrWin.ShowText(aEnemy, Format(rsList(rs_XCastsSpell], [Self.Name]));");
                AuxUtils.exStub("Drained by a");
            } else if (sign.equals("Ull")) {
                AuxUtils.exStub("???");
            }
        }
    }

    public final void defenseSpecialEffect(NWCreature enemy)
    {
        String entrySign = this.fEntry.Sign;
        
        if (entrySign.equals("Anssk")) {
            AuxUtils.exStub("The ");
            AuxUtils.exStub("s gaze");
            AuxUtils.exStub("    nwrWin.ShowText(aEnemy, Format(rsList(rs_YouHypnotized], [Self.GetDeclinableName(nSingle, cGenitive)]));");
            AuxUtils.exStub(" reflects off your shield.");
        } else if (entrySign.equals("Breleor")) {
            AuxUtils.exStub("//UseEffect(eid_Breleor_Tendril, nil, im_ItSelf, );");
        } else if (entrySign.equals("Cockatrice")) {
            AuxUtils.exStub("You hit the ");
            AuxUtils.exStub(" with your ");
            AuxUtils.exStub(".");
            AuxUtils.exStub("rs_YouTurnToStone");
            AuxUtils.exStub("Touched a");
            AuxUtils.exStub(" ");
            AuxUtils.exStub("%");
        } else if (entrySign.equals("Dreg")) {
            AuxUtils.exStub("    nwrWin.ShowText(aEnemy, Format(rsList(rs_XSplashesYou], [Self.Name]));");
            AuxUtils.exStub("Sizzled by a");
            AuxUtils.exStub(" ");
            AuxUtils.exStub("rs_YouSeemUnaffected");
            AuxUtils.exStub("s");
            AuxUtils.exStub("Your ");
            AuxUtils.exStub(" corrode");
            AuxUtils.exStub(".");
            AuxUtils.exStub("     nwrWin.ShowText(aEnemy, rsList(rs_YourItemsCorrodes]);");
            AuxUtils.exStub("     -1 bonus у некоторых предметов (в оригинале - у армора)");
        } else if (entrySign.equals("<evil_one>")) {
            AuxUtils.exStub(" looks your direction.");
            AuxUtils.exStub("Pain tears through your mind and body.");
            AuxUtils.exStub("Annoyed ");
        } else if (entrySign.equals("Faleryn")) {
            AuxUtils.exStub("You chop off one of the ");
            AuxUtils.exStub("s branches.");
        } else if (entrySign.equals("GiantSquid")) {
            AuxUtils.exStub("You cut off a tentacle.");
        } else if (entrySign.equals("Gorm")) {
            AuxUtils.exStub("Your weapon corrodes.");
            AuxUtils.exStub("The %s's blood splashes you.");
            AuxUtils.exStub("Killed by molecular acid");
            AuxUtils.exStub("rs_YouSeemUnaffected");
        } else if (entrySign.equals("IvyCreeper")) {
            AuxUtils.exStub("You release a cloud of spores.");
            AuxUtils.exStub("You inhale poison gas.");
            AuxUtils.exStub("rs_YouGoBlind");
            AuxUtils.exStub("You skin feels squishy.");
        } else if (entrySign.equals("Jagredin")) {
            AuxUtils.exStub("Your flesh burns.");
            AuxUtils.exStub("Died from system shock");
        } else if (entrySign.equals("KonrRig")) {
            AuxUtils.exStub(" grows stronger.");
        } else if (entrySign.equals("Nidhogg")) {
            AuxUtils.exStub(" is temporarily invulnerable.");
        } else if (entrySign.equals("Nidslacr")) {
            AuxUtils.exStub("The ");
            AuxUtils.exStub("the ");
            AuxUtils.exStub(" catches ");
            AuxUtils.exStub(".");
            AuxUtils.exStub("them");
            AuxUtils.exStub("it");
            AuxUtils.exStub("He throws ");
            AuxUtils.exStub(" on the ground in disgust.");
        } else if (entrySign.equals("Plog")) {
            AuxUtils.exStub(" opens a gate.");
            AuxUtils.exStub("His minions surge through! ");
            AuxUtils.exStub("killhim");
            AuxUtils.exStub(" snarls a curse.");
            AuxUtils.exStub("s hot breath slaps your face.");
            AuxUtils.exStub("s");
            AuxUtils.exStub("Your wand");
            AuxUtils.exStub(" pulsate");
            AuxUtils.exStub(".");
            AuxUtils.exStub(" grows stronger.");
            AuxUtils.exStub(" summons his worshippers.");
        } else if (entrySign.equals("PyrtaAth")) {
            AuxUtils.exStub("You hurt your head as well as the %s.");
            AuxUtils.exStub("Inadvertantly killed ");
            AuxUtils.exStub("self");
        } else if (entrySign.equals("RedOoze")) {
            AuxUtils.exStub("You hit the ");
            AuxUtils.exStub(".");
            AuxUtils.exStub("The ");
            AuxUtils.exStub(" consumes your gloves.");
            AuxUtils.exStub("rs_YourFleshBeginsToMelt");
            AuxUtils.exStub("Eaten by ");
            AuxUtils.exStub(" consumes your ");
        } else if (entrySign.equals("Sentinel")) {
            AuxUtils.exStub("You destroy the ");
            AuxUtils.exStub("s main eye.");
            AuxUtils.exStub("You destroy ");
            AuxUtils.exStub("the last ");
            AuxUtils.exStub("an ");
            AuxUtils.exStub("eyestalk.");
        } else if (entrySign.equals("StunJelly")) {
            AuxUtils.exStub("You are stuck.");
        }
    }

    public final Item canCure(EffectID eid)
    {
        Item result = null;

        // TODO: ???
        if (eid == EffectID.eid_None) {
            // wounds
            result = this.findMedicineItem(EffectID.eid_Cure, new ItemStates(ItemState.is_Normal, ItemState.is_Blessed));
            if (result == null) {
                result = this.findMedicineItem(EffectID.eid_Rejuvenation, new ItemStates(ItemState.is_Normal, ItemState.is_Blessed));
            }
            if (result == null) {
                result = this.findMedicineItem(EffectID.eid_Endurance, new ItemStates(ItemState.is_Normal, ItemState.is_Blessed));
            }
        } else {
            if (eid == EffectID.eid_Confusion) {
                result = this.findMedicineItem(EffectID.eid_Cure, new ItemStates(ItemState.is_Normal, ItemState.is_Blessed));
                AuxUtils.exStub("todo: +IvyCreeper");
            } else {
                if (eid == EffectID.eid_Fading) {
                    AuxUtils.exStub("???");
                } else {
                    if (eid == EffectID.eid_Intoxicate || eid == EffectID.eid_Contamination) {
                        result = this.findMedicineItem(EffectID.eid_Cure, new ItemStates(ItemState.is_Blessed));
                    } else {
                        if (eid == EffectID.eid_Blindness || eid == EffectID.eid_Fever || eid == EffectID.eid_Withered) {
                            result = this.findMedicineItem(EffectID.eid_Cure, new ItemStates(ItemState.is_Normal, ItemState.is_Blessed));
                        } else {
                            if (eid == EffectID.eid_Hallucination) {
                                result = this.findMedicineItem(EffectID.eid_Cure, new ItemStates(ItemState.is_Normal, ItemState.is_Blessed));
                                AuxUtils.exStub("todo: +IvyCreeper");
                            } else {
                                if (eid == EffectID.eid_HealInability || eid == EffectID.eid_Diseased) {
                                    result = this.findMedicineItem(EffectID.eid_Cure, new ItemStates(ItemState.is_Normal, ItemState.is_Blessed));
                                } else {
                                    if (eid == EffectID.eid_Burns || eid == EffectID.eid_LegsMissing || eid == EffectID.eid_Vertigo || eid == EffectID.eid_Lycanthropy) {
                                        result = this.findMedicineItem(EffectID.eid_Cure, new ItemStates(ItemState.is_Blessed));
                                    } else {
                                        if (eid == EffectID.eid_BrainScarring) {
                                            result = this.findMedicineItem(EffectID.eid_Cure, new ItemStates(ItemState.is_Blessed));
                                            AuxUtils.exStub("todo: +IvyCreeper");
                                        } else {
                                            if (eid == EffectID.eid_Impregnation || eid == EffectID.eid_EyesMissing || eid == EffectID.eid_Deafness) {
                                                result = this.findMedicineItem(EffectID.eid_Cure, new ItemStates(ItemState.is_Blessed));
                                            } else {
                                                if (eid == EffectID.eid_Fragile) {
                                                    AuxUtils.exStub("???");
                                                } else {
                                                    if (eid == EffectID.eid_Draining) {
                                                        AuxUtils.exStub("???");
                                                    } else {
                                                        if (eid == EffectID.eid_Famine) {
                                                            AuxUtils.exStub("???");
                                                        } else {
                                                            if (eid == EffectID.eid_Genesis) {
                                                                AuxUtils.exStub("???");
                                                            } else {
                                                                if (eid == EffectID.eid_Paralysis) {
                                                                    AuxUtils.exStub("???");
                                                                } else {
                                                                    if (eid != EffectID.eid_Insanity) {
                                                                        throw new RuntimeException("untilled disease");
                                                                    }
                                                                    AuxUtils.exStub("todo: Result := FindCureItem(0, [], aMedicineItem);");
                                                                    AuxUtils.exStub("todo: +IvyCreeper");
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

    public final boolean isAvailable(LocatedEntity entity, boolean checkLOS)
    {
        boolean result;
        if (entity instanceof NWCreature) {
            NWCreature cr = (NWCreature) entity;
            result = (this.LayerID == cr.LayerID && this.fField.X == cr.fField.X && this.fField.Y == cr.fField.Y);
            if (!result) {
                return result;
            }
        }
        result = this.isSeen(entity.getPosX(), entity.getPosY(), checkLOS);
        return result;
    }

    @Override
    public boolean isSeen(int aX, int aY, boolean checkLOS)
    {
        int dist = AuxUtils.distance(super.getPosX(), super.getPosY(), aX, aY);
        boolean result = (dist <= this.getSurvey());

        if (result && checkLOS) {
            result = this.getCurrentMap().lineOfSight(super.getPosX(), super.getPosY(), aX, aY);
        }

        return result;
    }

    // FIXME: how usage - strange code
    public final boolean checkVisible(NWCreature creature)
    {
        boolean invisible = creature.fEffects.findEffectByID(EffectID.eid_Invisibility) != null;
        return !invisible || this.isSeekInvisible();
    }

    public final boolean canShoot(NWCreature enemy)
    {
        if (this.isInWater()) {
            return false;
        } else {
            ShootObj shootObj = new ShootObj();
            shootObj.aCreature = this;
            shootObj.aEnemy = enemy;
            return AuxUtils.doLine(super.getPosX(), super.getPosY(), enemy.getPosX(), enemy.getPosY(), shootObj::LineProc, true);
        }
    }

    public final boolean canSink()
    {
        return this.getCurrentField().canSink(super.getPosX(), super.getPosY());
    }

    public final boolean canTeach(NWCreature subject)
    {
        int cnt = 0;
        for (int i = 0; i < StaticData.dbTeachable.length; i++) {
            int id = StaticData.dbTeachable[i].id;
            TeachableKind kind = StaticData.dbTeachable[i].kind;
            boolean res = false;

            switch (kind) {
                case Ability:
                    AbilityID ab = AbilityID.forValue(id);
                    res = (this.fAbilities.indexOf(id) >= 0 && subject.getAbility(ab) < this.getAbility(ab));
                    break;

                case Skill:
                    SkillID sk = SkillID.forValue(id);
                    res = (this.fSkills.indexOf(id) >= 0 && subject.getSkill(sk) < this.getSkill(sk));
                    break;
            }
            
            if (res) {
                cnt++;
            }
        }
        return cnt > 0;
    }

    // FIXME: ugly code!
    public final int checkEquipment(float dist, BestWeaponSigns bwSigns)
    {
        try {
            int highestDamage = 0;

            int num = this.fItems.getCount();
            for (int i = 0; i < num; i++) {
                Item item = this.fItems.getItem(i);
                ItemFlags ifs = item.getFlags();
                if (ifs.hasIntersect(ItemFlags.if_MeleeWeapon, ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon)) {
                    boolean onlyShootWeapon = (ifs.hasIntersect(ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon) && !ifs.contains(ItemFlags.if_MeleeWeapon));

                    if (!bwSigns.contains(BestWeaponSigns.bwsCanShoot) && onlyShootWeapon) {
                        continue;
                    }

                    if (bwSigns.contains(BestWeaponSigns.bwsOnlyShoot) && (!ifs.hasIntersect(ItemFlags.if_ShootWeapon, ItemFlags.if_ThrowWeapon))) {
                        continue;
                    }

                    if ((ifs.contains(ItemFlags.if_ShootWeapon)) && this.getAmmoCount(item) <= 0) {
                        continue;
                    }

                    int damage = this.getWeaponDamage(item);
                    if (damage > highestDamage) {
                        highestDamage = damage;
                    }
                }
            }

            float fDam = 0.0f;
            if (highestDamage > 0) {
                fDam = ((1.0f / (float) highestDamage));
            }

            float highestArmor = 0f;
            Item bestShield = null;

            Item bestWeapon = null;
            float bestWeight = 0f;

            for (int i = 0; i < num; i++) {
                Item item = this.fItems.getItem(i);
                ItemFlags ifs = item.getFlags();
                if (ifs.hasIntersect(ItemFlags.if_MeleeWeapon, ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon)) {
                    boolean onlyShootWeapon = (ifs.hasIntersect(ItemFlags.if_ThrowWeapon, ItemFlags.if_ShootWeapon) && !ifs.contains(ItemFlags.if_MeleeWeapon));
                    
                    if (!bwSigns.contains(BestWeaponSigns.bwsCanShoot) && onlyShootWeapon) {
                        continue;
                    }

                    if (bwSigns.contains(BestWeaponSigns.bwsOnlyShoot) && (!ifs.hasIntersect(ItemFlags.if_ShootWeapon, ItemFlags.if_ThrowWeapon))) {
                        continue;
                    }

                    if ((ifs.contains(ItemFlags.if_ShootWeapon)) && this.getAmmoCount(item) <= 0) {
                        continue;
                    }

                    float Val = (((float) dist - (float) this.getWeaponRange(item)));
                    if (Val < 1f) {
                        Val = 1f;
                    } else {
                        Val = ((1.0f / Val));
                    }

                    float weight = ((fDam * (float) this.getWeaponDamage(item) + Val));
                    if (weight > bestWeight) {
                        bestWeight = weight;
                        bestWeapon = item;
                    }
                } else {
                    if (item.getKind() == ItemKind.ik_Shield && item.getAttribute(ItemAttribute.ia_Defense) > (double) highestArmor) {
                        bestShield = item;
                        highestArmor = ((float) item.getAttribute(ItemAttribute.ia_Defense));
                    }
                }
            }

            if (bestWeapon != null) {
                Item item = this.getItemByEquipmentKind(BodypartType.bp_RHand);
                if (item != null) {
                    item.setInUse(false);
                }

                item = this.getItemByEquipmentKind(BodypartType.bp_LHand);
                if (item != null) {
                    item.setInUse(false);
                }

                bestWeapon.setInUse(true);
                highestDamage = this.getWeaponDamage(bestWeapon);
                ItemFlags wks = bestWeapon.getFlags();
                if (wks.contains(ItemFlags.if_ShootWeapon)) {
                    item = this.getItemByEquipmentKind(BodypartType.bp_Back);
                    if (item != null) {
                        item.setInUse(false);
                    }
                    item = this.getWeaponAmmo(bestWeapon);
                    item.setInUse(true);
                }
            }

            if (bestShield != null && bestWeapon != null && (!bestWeapon.isTwoHanded())) {
                Item lhItem = this.getItemByEquipmentKind(BodypartType.bp_LHand);
                if (lhItem != bestShield) {
                    if (lhItem != null) {
                        lhItem.setInUse(false);
                    }
                    bestShield.setInUse(true);
                }
            }
            
            return highestDamage;
        } catch (Exception ex) {
            Logger.write("NWCreature.checkEquipment(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void checkHealth(RefObject<Boolean> TurnUsed)
    {
        try {
            TurnUsed.argValue = false;

            if (this.HPCur < (this.HPMax * 0.6)) {
                this.checkHealth_Solve(EffectID.eid_None, Locale.getStr(RS.rs_Wounds), TurnUsed);
                if (TurnUsed.argValue) {
                    return;
                }
            }

            int num = this.fEffects.getCount();
            for (int i = 0; i < num; i++) {
                int eff = this.fEffects.getItem(i).CLSID;
                EffectID eid = EffectID.forValue(eff);
                if (EffectsData.dbEffects[eff].Flags.contains(EffectFlags.ek_Disease)) {
                    this.checkHealth_Solve(eid, Locale.getStr(EffectsData.dbEffects[eff].NameRS), TurnUsed);

                    if (TurnUsed.argValue) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.checkHealth(): " + ex.getMessage());
        }
    }

    /*public final boolean hasCondition(int condition)
    {
        return this.fConditions.contains(condition);
    }

    public final void setCondition(int condition)
    {
        this.fConditions.include(condition);
    }

    public final void unsetCondition(int condition)
    {
        this.fConditions.exclude(condition);
    }*/

    public final void addEffect(EffectID effectID, ItemState state, EffectAction actionKind, boolean invert, String msg)
    {
        int turns = Effect.getDuration(effectID, state, invert);
        this.addEffect(effectID, state, actionKind, turns, msg);
    }

    public final void addEffect(EffectID effectID, ItemState state, EffectAction actionKind, int duration, String msg)
    {
        int i = Effect.getMagnitude(effectID);
        Effect effect = new Effect(this.fSpace, this, effectID, null, actionKind, duration, i);
        this.fEffects.Add(effect);
        if (this.isPlayer()) {
            this.getSpace().showText(this, msg);
        }
    }

    public final void initEffect(EffectID effectID, Object source, EffectAction actionKind)
    {
        Effect effect = new Effect(this.fSpace, this, effectID, source, actionKind, 0, Effect.getMagnitude(effectID));
        this.fEffects.Add(effect);
    }

    public final void doneEffect(EffectID effectID, Object source)
    {
        int num = this.fEffects.getCount();
        for (int i = 0; i < num; i++) {
            Effect eff = this.fEffects.getItem(i);
            if (eff.Source.equals(source) && eff.CLSID == effectID.getValue()) {
                this.fEffects.delete(i);
                break;
            }
        }
    }

    public final void death(String message, Object source)
    {
        try {
            this.getSpace().showText(this, message);

            this.setState(CreatureState.csDead);
            this.Turn = -AuxUtils.getBoundedRnd(10, 20);

            if ((this.fEntry.Flags.contains(CreatureFlags.esUnique)) && !this.fEntry.isCorpsesPersist() && !this.fEntry.isRespawn()) {
                this.getSpace().setVolatileState(this.fEntry.GUID, VolatileState.vesDestroyed);
            }

            this.dropAll();
            if (this.fIsTrader) {
                Building house = (Building) this.findHouse();
                if (house != null) {
                    house.Holder = null;
                }
            }

            this.postDeath();
        } catch (Exception ex) {
            Logger.write("NWCreature.death(): " + ex.getMessage());
            throw ex;
        }
    }

    private void postDeath()
    {
        String entrySign = this.fEntry.Sign;
        if (entrySign.equals("Bartok")) {
            AuxUtils.exStub("todo: генерация псионической волны");
            AuxUtils.exStub("todo: ???nwrWin.ShowText(?, rsList(rs_YouShakenViolently]);");
        } else {
            if (entrySign.equals("LavaFlow")) {
                this.postDeath_SetTile(PlaceID.pid_Lava);
            } else if (entrySign.equals("LiveRock")) {
                this.postDeath_SetTile(PlaceID.pid_CaveFloor);
            } else if (entrySign.equals("MudFlow")) {
                this.postDeath_SetTile(PlaceID.pid_Mud);
            } else if (entrySign.equals("Nidhogg")) {
                if (this.LastAttacker.equals(this.getSpace().getPlayer())) {
                    this.getSpace().showText(this, Locale.format(RS.rs_XSlavers, new Object[]{super.getName()}));
                }
            } else if (entrySign.equals("SandForm")) {
                this.postDeath_SetTile(PlaceID.pid_Quicksand);
            } else if (entrySign.equals("WateryForm")) {
                this.postDeath_SetTile(PlaceID.pid_Water);
            }
        }

        this.getSpace().postDeath(this);
    }

    public final void checkActionAbility(CreatureAction action, Item item)
    {
        if (action.Flags.hasIntersect(ActionFlags.afWithItem, ActionFlags.afCheckAbility)) {
            AbilityID ab = this.getActionAbility(action, item);
            if (ab != AbilityID.Ab_None) {
                int val = this.getAbility(ab);
                if (val < 100) {
                    this.setAbility(ab, val + 1);
                }
            }
        }
    }

    public final boolean hasTurn()
    {
        int num = this.fEffects.getCount();
        for (int i = 0; i < num; i++) {
            Effect effect = this.fEffects.getItem(i);
            EffectID eff = EffectID.forValue(effect.CLSID);
            if (eff == EffectID.eid_Paralysis || eff == EffectID.eid_Sleep || eff == EffectID.eid_Stoning || eff == EffectID.eid_CaughtInNet) {
                return false;
            }
        }

        return true;
    }

    public void doTurn()
    {
        try {
            this.incTurn();
            this.fEffects.execute();
            this.recovery();

            if (this.fEntry.Flags.contains(CreatureFlags.esUseItems)) {
                this.applySpecialItems();
            }

            int playerSpeed = this.getSpace().getPlayer().getSpeed();

            if ((this.Stamina >= playerSpeed && !this.isPlayer()) || this.Prowling) {
                this.LastAttacker = null;

                if (this.hasTurn()) {
                    // FIXME: move to goal!
                    boolean turn_used = false;
                    if (this.fEntry.Flags.hasIntersect(CreatureFlags.esMind, CreatureFlags.esUseItems) && (this.isMercenary() || GlobalVars.nwrWin.getExtremeMode())) {
                        RefObject<Boolean> tempRef_turn_used = new RefObject<>(turn_used);
                        this.checkHealth(tempRef_turn_used);
                        turn_used = tempRef_turn_used.argValue;
                    }

                    if (!turn_used) {
                        // FIXME: move to goal!
                        this.doDefaultAction();

                        if (super.fBrain != null) {
                            super.fBrain.think();
                        }
                    }
                }

                this.Stamina -= playerSpeed;
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.doTurn(): " + ex.getMessage());
        }
    }

    public final void drown()
    {
        if (!this.fEntry.Flags.hasIntersect(CreatureFlags.esSwimming, CreatureFlags.esFlying)) {
            if (this.fEffects.findEffectByID(EffectID.eid_Deafness) == null) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_Gurgle));
            }
            this.death(Locale.getStr(RS.rs_XDrowns), null);
        }
    }

    public final Item findMedicineItem(EffectID cureEffect, ItemStates states)
    {
        int num = this.fItems.getCount();
        for (int i = 0; i < num; i++) {
            Item item = this.fItems.getItem(i);
            EffectEntry[] effects = item.getEntry().Effects;
            if (((effects != null) ? effects.length : 0) >= 1) {
                EffectID eid = effects[0].EffID;
                if (eid == cureEffect && states.contains(item.State)) {
                    return item;
                }
            }
        }

        return null;
    }

    public final DungeonRoom findDungeonRoom()
    {
        DungeonRoom result = null;

        NWField fld = this.getCurrentField();
        int id = fld.getLayer().EntryID;
        if (id == GlobalVars.Layer_Svartalfheim1 || id == GlobalVars.Layer_Svartalfheim2 || id == GlobalVars.Layer_Svartalfheim3 || id == GlobalVars.Layer_Armory || id == GlobalVars.Layer_GrynrHalls) {
            int x = super.getPosX();
            int y = super.getPosY();

            result = fld.findDungeonRoom(x, y);

            if (result == null) {
                x += fld.getCoords().X * StaticData.FieldWidth;
                y += fld.getCoords().Y * StaticData.FieldHeight;

                result = fld.getLayer().findDungeonRoom(x, y);
            }
        }

        return result;
    }

    public final NWCreature findEnemy()
    {
        NWCreature enemy = this.getDefaultEnemy();
        if (enemy != null) {
            return enemy;
        }

        enemy = this.findEnemyByAttack();
        if (enemy != null) {
            return enemy;
        }

        NWField fld = this.getCurrentField();
        int dist = StaticData.FieldWidth;
        CreaturesList crt = fld.getCreatures();
        int num = crt.getCount();
        for (int i = 0; i < num; i++) {
            NWCreature cr = crt.getItem(i);
            if (!cr.equals(this)) {
                int dt = AuxUtils.distance(super.getLocation(), cr.getLocation());
                if (dt < this.getSurvey()) {
                    boolean ok = this.isAvailable(cr, true) && this.isEnemy(cr) && this.checkVisible(cr);
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

        Player player = this.getSpace().getPlayer();
        if (player.getEffects().findEffectByID(EffectID.eid_Genesis) != null) {
            return player;
        }

        return null;
    }

    public final Building findHouse()
    {
        if (this.fHouse == null) {
            EntityList features = this.getCurrentMap().getFeatures();
            int num = features.getCount();
            for (int i = 0; i < num; i++) {
                GameEntity b = features.getItem(i);

                if (b instanceof Building && this.equals(((Building) b).Holder)) {
                    this.fHouse = ((Building) b);
                    break;
                }
            }
        }

        return this.fHouse;
    }

    public final Item findItem()
    {
        Item result = null;
        int idx = -1;
        int dt = (int) (this.getSurvey() + 1);

        NWField fld = (NWField) this.getCurrentMap();

        int num = fld.getItems().getCount();
        for (int i = 0; i < num; i++) {
            Item item = fld.getItems().getItem(i);
            int dist = AuxUtils.distance(super.getLocation(), item.getLocation());
            Building b = fld.findBuilding(item.getPosX(), item.getPosY());
            if ((b == null || b.Holder == null) && dist < dt && item.CLSID != GlobalVars.iid_DeadBody && item.CLSID != GlobalVars.iid_Mummy) {
                dt = dist;
                idx = i;
            }
        }

        if (idx >= 0) {
            result = fld.getItems().getItem(idx);
        }

        return result;
    }

    public final AbilityID getActionAbility(CreatureAction action, Item item)
    {
        AbilityID result = AbilityID.Ab_None;
        switch (action) {
            case caAttackMelee:
                if (item == null) {
                    result = AbilityID.Ab_HandToHand;
                } else {
                    result = item.getKind().Ability;
                }
                break;

            case caAttackShoot:
                if (item != null) {
                    result = item.getKind().Ability;
                }
                break;

            case caAttackThrow:
                result = AbilityID.Ab_Marksman;
                break;

            case caAttackParry:
                result = AbilityID.Ab_Parry;
                break;

            case caItemUse:
                if (item != null) {
                    result = item.getKind().Ability;
                }
                break;
        }
        return result;
    }

    public final int getAmmoCount(Item aWeapon)
    {
        int Result = 0;
        Item ammo = this.getWeaponAmmo(aWeapon);
        if (ammo != null) {
            Result = (int) ammo.Count;
        }
        return Result;
    }

    public final SkillID getAttackSkill(int aDistance, RefObject<Integer> aDamage)
    {
        SkillID result = SkillID.Sk_None;

        aDamage.argValue = 0;
        IntList sks = new IntList();
        try {
            int num = this.fSkills.getCount();
            for (int i = 0; i < num; i++) {
                SkillID sk = SkillID.forValue(this.fSkills.getItem(i).aID);
                EffectID e = sk.Effect;
                Range range = EffectsData.dbEffects[e.getValue()].Magnitude;
                int rangeVal = (int) ((long) Math.round((range.Min + range.Max) / 2.0d));
                if (e != EffectID.eid_None && EffectsData.dbEffects[e.getValue()].Flags.contains(EffectFlags.ek_Offence) && rangeVal >= aDistance) {
                    sks.add(sk.getValue());
                }
            }

            int cnt = sks.getCount();
            if (cnt > 1) {
                result = SkillID.forValue(sks.get(AuxUtils.getRandom(cnt)));
            } else {
                if (cnt == 1) {
                    result = SkillID.forValue(sks.get(0));
                }
            }

            if (result != SkillID.Sk_None) {
                EffectID e = result.Effect;
                aDamage.argValue = (int) ((long) Math.round((double) (EffectsData.dbEffects[e.getValue()].Damage.Min + EffectsData.dbEffects[e.getValue()].Damage.Max) / 2.0));
            }
        } finally {
            sks.dispose();
        }
        return result;
    }

    public final String getDeclinableName(Number dNumber, Case dCase)
    {
        try {
            String result;

            String dn = this.fEntry.getNounDeclension(dNumber, dCase);
            RaceID race = this.fEntry.Race;

            if (race == RaceID.crHuman) {
                if (this.isPlayer()) {
                    result = this.fName;
                } else {
                    result = dn + " " + StaticData.morphCompNoun(this.fName, dCase, dNumber, this.Sex, true, false);
                }
            } else {
                result = dn;
            }

            return result;
        } catch (Exception ex) {
            Logger.write("NWCreature.getDeclinableName(): " + ex.getMessage());
            throw ex;
        }
    }

    public final TrapResult getEntrapRes(RefObject<String> refMessage)
    {
        refMessage.argValue = "";
        TrapResult result = TrapResult.tr_Absent;

        Effect eff = this.fEffects.findEffectByID(EffectID.eid_PitTrap);
        if (eff != null) {
            int chn = 100 - eff.Duration * 10;
            if (!AuxUtils.chance(chn)) {
                refMessage.argValue = Locale.getStr(AuxUtils.getBoundedRnd(541, 545));
                result = TrapResult.tr_EscapeBreak;
            } else {
                this.fEffects.remove(eff);
                refMessage.argValue = Locale.getStr(RS.rs_PitTrapGetoutOk);
                result = TrapResult.tr_EscapeOk;
            }
        } else {
            eff = this.fEffects.findEffectByID(EffectID.eid_Quicksand);
            if (eff != null) {
                if (eff.Duration == 1) {
                    refMessage.argValue = Locale.getStr(RS.rs_YouDrownedInQuicksand);
                    result = TrapResult.tr_EscapeDeath;
                } else {
                    if (eff.Duration == 2) {
                        refMessage.argValue = Locale.getStr(RS.rs_YouSinkToYourChin);
                        result = TrapResult.tr_EscapeBreak;
                    } else {
                        refMessage.argValue = Locale.getStr(RS.rs_StrugglingIsUseless);
                        result = TrapResult.tr_EscapeBreak;
                    }
                }
            } else {
                eff = this.fEffects.findEffectByID(EffectID.eid_PhaseTrap);
                if (eff != null) {
                    if (this.fEffects.findEffectByID(EffectID.eid_Phase) != null) {
                        result = TrapResult.tr_Absent;
                    } else {
                        refMessage.argValue = Locale.getStr(RS.rs_YouInVeryConfinedSpace);
                        result = TrapResult.tr_EscapeBreak;
                    }
                } else {
                    int bg = ((NWTile) this.getCurrentMap().getTile(super.getPosX(), super.getPosY())).getBackBase();
                    if (bg == PlaceID.pid_Mud && AuxUtils.chance(50)) {
                        refMessage.argValue = Locale.getStr(RS.rs_YouSlipInMud);
                        result = TrapResult.tr_EscapeBreak;
                    }
                }
            }
        }

        return result;
    }

    public final short getHirePrice()
    {
        float temp;

        if (this.CLSID == GlobalVars.cid_Guardsman) {
            Player player = this.getSpace().getPlayer();
            
            temp = (this.HPCur - (float) (this.fEntry.maxHP - this.fEntry.minHP) / 2);
            temp += (ArmorClass - this.fEntry.AC);
            temp += (ToHit - this.fEntry.ToHit);
            temp += (Attacks - this.fEntry.Attacks);
            temp += (this.fSpeed - this.fEntry.Speed);
            temp += (Constitution - this.fEntry.Constitution);
            temp += (Strength - this.fEntry.Strength);
            temp += (Dexterity - this.fEntry.Dexterity);
            temp += (((float) (DBMax - DBMin) / 2) - ((float) (this.fEntry.maxDB - this.fEntry.minDB) / 2));
            temp += (this.getSurvey() - this.fEntry.Survey);

            for (int i = AbilityID.Ab_First; i <= AbilityID.Ab_Last; i++) {
                temp = temp + (this.fAbilities.getValue(i) - this.fEntry.Abilities.getValue(i));
            }

            for (int i = SkillID.Sk_First; i <= SkillID.Sk_Last; i++) {
                temp = temp + (this.fSkills.getValue(i) - this.fEntry.Skills.getValue(i));
            }

            for (int i = 0; i < this.fItems.getCount(); i++) {
                Item item = this.fItems.getItem(i);
                temp = temp + item.getPrice();
            }

            float lev = (float) this.Level;
            float f = (lev / (lev + player.Level));

            temp = temp * (1.0f + f);
        } else {
            temp = 10000;
        }

        return (short) Math.round(temp);
    }

    public final byte getLightRadius()
    {
        byte result = 0;

        Item torch = this.getItemByEquipmentKind(BodypartType.bp_LHand);
        if (torch != null && torch.CLSID == GlobalVars.iid_Torch) {
            result = (byte) ((int) result + torch.Bonus);
        }
        Effect eLight = this.fEffects.findEffectByID(EffectID.eid_Light);
        if (eLight != null) {
            result = (byte) ((int) result + eLight.Magnitude);
        }

        return result;
    }

    public final NWCreature getNearestCreature(int aX, int aY, boolean onlyAllies)
    {
        NWField fld = (NWField) this.getCurrentMap();
        int num = fld.getCreatures().getCount();
        for (int i = 0; i < num; i++) {
            NWCreature creat = fld.getCreatures().getItem(i);

            if (!creat.equals(this)) {
                int dx = Math.abs(aX - creat.getPosX());
                int dy = Math.abs(aY - creat.getPosY());
                if (dx <= 1 && dy <= 1 && (!onlyAllies || (onlyAllies && !this.isEnemy(creat)))) {
                    return creat;
                }
            }
        }
        return null;
    }

    public final Point getNearestPlace(int radius, boolean withoutLive)
    {
        return this.getCurrentMap().getNearestPlace(this.getPosX(), this.getPosY(), radius, withoutLive, this.getMovements());
    }

    public final Point getNearestPlace(int cx, int cy, int radius, boolean withoutLive)
    {
        return this.getCurrentMap().getNearestPlace(cx, cy, radius, withoutLive, this.getMovements());
    }

    public final Point getNearestPlace(Point cpt, int radius, boolean withoutLive)
    {
        return this.getCurrentMap().getNearestPlace(cpt.X, cpt.Y, radius, withoutLive, this.getMovements());
    }

    public final Point getStep(Point target)
    {
        Point result = null;
        try {
            AbstractMap map = this.getCurrentMap();
            Point src = super.getLocation();

            if (!src.equals(target) && map.isValid(target.X, target.Y)) {
                int dist = AuxUtils.distance(src, target);

                if (dist == 1 && this.canMove(map, target.X, target.Y)) {
                    result = target;
                }

                if ((result == null) && this.isSeen(target.X, target.Y, true)) {
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

                    if (this.canMove(map, tx, ty)) {
                        result = new Point(tx, ty);
                    }
                }

                if (result == null) {
                    PathSearch.PSResult res = PathSearch.search(map, src, target, this);
                    if (res != null) {
                        result = res.Step;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.getStep(): " + ex.getMessage());
        }
        return result;
    }

    public final Item getWeaponAmmo(Item aWeapon)
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
            result = ((Item) this.getItems().findByCLSID(ammoID));
        }

        return result;
    }

    public final int getWeaponDamage(Item weapon)
    {
        int result = 0;

        if (weapon != null && weapon.isWeapon()) {
            ItemFlags wks = weapon.getFlags();
            if (wks.contains(ItemFlags.if_MeleeWeapon)) {
                result = weapon.getAverageDamage();
            } else {
                Item projectile = null;
                if (wks.contains(ItemFlags.if_ThrowWeapon)) {
                    projectile = weapon;
                } else {
                    if (wks.contains(ItemFlags.if_ShootWeapon)) {
                        projectile = this.getWeaponAmmo(weapon);
                    }
                }

                if (projectile != null) {
                    result = projectile.getAverageDamage();
                }
            }
        }

        return result;
    }

    public final int getWeaponRange(Item weapon)
    {
        int result = 0;

        if (weapon != null && weapon.isWeapon()) {
            ItemFlags wks = weapon.getFlags();
            if (wks.contains(ItemFlags.if_MeleeWeapon)) {
                result = 1;
            } else {
                Item projectile = null;
                if (wks.contains(ItemFlags.if_ThrowWeapon)) {
                    projectile = weapon;
                } else {
                    if (wks.contains(ItemFlags.if_ShootWeapon)) {
                        projectile = this.getWeaponAmmo(weapon);
                    }
                }

                if (projectile != null) {
                    result = getProjectileRange(projectile);
                }
            }
        }

        return result;
    }

    private int getProjectileRange(Item projectile)
    {
        if (projectile != null) {
            int range = Math.max(3, (int) ((long) Math.round((this.Strength - 9 - (double) projectile.getWeight() / 7.0))));
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

    public final boolean hasAffect(EffectID effectID)
    {
        int eid = effectID.getValue();
        AbilityID resist = EffectsData.dbEffects[eid].Resistance;

        return this.getAbility(resist) <= 0 && (!EffectsData.dbEffects[eid].Flags.contains(EffectFlags.ef_Ray) || this.getAbility(AbilityID.Resist_Ray) <= 0);
    }

    public final boolean isBlindness()
    {
        return (this.fEffects.findEffectByID(EffectID.eid_Blindness) != null);
    }

    public final boolean hasMovement(int kind)
    {
        return this.getMovements().contains(kind);
    }

    public final void incTurn()
    {
        this.Turn++;
    }

    public final void invertHostility()
    {
        this.Alignment = this.Alignment.invert();
    }

    public final boolean isInWater()
    {
        NWTile tile = this.getCurrentField().getTile(super.getPosX(), super.getPosY());
        boolean isUnderwater = (tile.Background == PlaceID.pid_Water && this.LayerID != GlobalVars.Layer_MimerWell);

        if (isUnderwater) {
            Effect efSail = this.fEffects.findEffectByID(EffectID.eid_Sail);
            return (isUnderwater && efSail == null);
        } else {
            return false;
        }
    }

    public final boolean isConfused()
    {
        return this.fEffects.findEffectByID(EffectID.eid_Confusion) != null;
    }

    public final boolean isHallucinations()
    {
        return this.fEffects.findEffectByID(EffectID.eid_Hallucination) != null || this.fEffects.findEffectByID(EffectID.eid_Insanity) != null;
    }

    public final boolean isEnemy(NWCreature aOther)
    {
        return this.getReaction(aOther) == Reaction.rHostile;
    }

    public final boolean isImprisonable()
    {
        boolean Result = true;
        int i = 0;
        while (this.fEntry.Sign.compareTo(NWCreature.NotImprisonable[i]) != 0) {
            i++;
            if (i == 31) {
                return Result;
            }
        }
        Result = false;
        return Result;
    }

    public final boolean isStoning()
    {
        return (this.fEffects.findEffectByID(EffectID.eid_Stoning) != null);
    }

    public final boolean isNear(Point point)
    {
        return AuxUtils.distance(super.getPosX(), super.getPosY(), point.X, point.Y) == 1;
    }

    public boolean isPlayer()
    {
        return false;
    }

    public final boolean isSeekInvisible()
    {
        return this.getAbility(AbilityID.Ab_SixthSense) > 0 || this.getAbility(AbilityID.Ab_Telepathy) > 0 || this.fEffects.findEffectByID(EffectID.eid_ThirdSight) != null;
    }

    public void knowIt(int entityID)
    {
    }

    public final boolean checkMove(Movements aNeed)
    {
        boolean result = (this.getMovements().hasIntersect(aNeed));
        if (!result) {
            if ((aNeed.contains(Movements.mkWalk)) && this.fEffects.findEffectByID(EffectID.eid_LegsMissing) != null) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_YouHaveNoLegs));
            }
            if ((aNeed.contains(Movements.mkSwim)) && (this.getAbility(AbilityID.Ab_Swimming) <= 0 || this.fEffects.findEffectByID(EffectID.eid_Sail) == null)) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_YouCannotSwim));
            }
            if ((aNeed.contains(Movements.mkFly)) && this.getAbility(AbilityID.Ab_Levitation) <= 0) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_YouCannotLevitate));
            }
        }
        return result;
    }

    @Override
    public boolean canMove(IMap map, int aX, int aY)
    {
        Movements ms = this.getMovements();
        return ((AbstractMap) map).isValidMove(aX, aY, ms);
    }

    public final Point searchRndLocation(AbstractMap map, Rect area)
    {
        try {
            NWField fld = (NWField) map;

            int tries = 150;
            while (tries > 0) {
                int x = AuxUtils.getBoundedRnd(area.Left, area.Right);
                int y = AuxUtils.getBoundedRnd(area.Top, area.Bottom);

                if (fld.findCreature(x, y) == null) {
                    // WARN: this check not removal to other place
                    if (fld.LandID == GlobalVars.Land_Crossroads) {
                        int fore = fld.getTile(x, y).getForeBase();
                        if (fore == PlaceID.pid_cr_Disk) {
                            return new Point(x, y);
                        }
                    } else {
                        if (this.canMove(map, x, y)) {
                            return new Point(x, y);
                        }
                    }
                }

                tries--;
            }

            return null;
        } catch (Exception ex) {
            Logger.write("NWCreature.searchLocation(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void moveRnd()
    {
        AbstractMap map = this.getCurrentMap();
        Point pt = this.searchRndLocation(map, map.getAreaRect());

        this.checkTile(false);
        this.setPos(pt.X, pt.Y);
        this.checkTile(true);
    }

    public final void moveSpecialEffect(NWField field, int oldX, int oldY)
    {
        // TODO: checkit
        if (this.fEntry.Sign.equals("Jagredin")) {
            NWTile tile = field.getTile(oldX, oldY);
            tile.Background = PlaceID.pid_Rubble;
            field.normalize();
        }
    }

    @Override
    public void moveTo(int newX, int newY)
    {
        AuxUtils.exStub("msg:  rs_ThisAreaIsMagicallyBounded (Alfheim's plane)");

        try {
            String msg = "";
            RefObject<String> tempRef_msg = new RefObject<>(msg);
            TrapResult et_res = this.getEntrapRes(tempRef_msg);
            msg = tempRef_msg.argValue;
            switch (et_res) {
                case tr_Absent:
                    // dummy
                    break;

                case tr_EscapeOk:
                    if (this.isPlayer()) {
                        this.getSpace().showText(this, msg);
                    }
                    break;

                case tr_EscapeBreak:
                    if (this.isPlayer()) {
                        this.getSpace().showText(this, msg);
                    }
                    return;

                case tr_EscapeDeath:
                    this.death(msg, null);
                    return;
            }

            int newLayer = this.LayerID;
            int fx = this.fField.X;
            int fy = this.fField.Y;
            int px = newX;
            int py = newY;

            NWField fld = (NWField) this.getCurrentMap();
            this.moveSpecialEffect(fld, this.getPosX(), this.getPosY());

            boolean gpChanged = false;
            GlobalPosition cpi = new GlobalPosition(fx, fy, px, py, gpChanged);
            cpi.checkPos();
            fx = cpi.fx;
            fy = cpi.fy;
            px = cpi.px;
            py = cpi.py;
            gpChanged = cpi.globalChanged;

            if (!gpChanged) {
                if (fld.EntryID == GlobalVars.Field_Bifrost && py == 0) {
                    newLayer = GlobalVars.Layer_Asgard;
                    fx = 0;
                    fy = 0;
                    px = 0;
                    py = 9;
                    gpChanged = true;
                    if (this.isPlayer()) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_BifrostCollapses));
                        this.getSpace().BifrostCollapsed = true;
                    }
                } else {
                    Gate gate = fld.findGate(px, py);
                    if (gate != null && gate.getKind() == Gate.KIND_FIX) {
                        NWTile tile = fld.getTile(px, py);
                        int fgp = tile.getForeBase();
                        if (fgp == PlaceID.pid_Vortex || fgp == PlaceID.pid_VortexStrange) {
                            // TODO: What's the difference (Vortex vs VortexStrange)?
                            int num = AuxUtils.getRandom(4);
                            switch (num) {
                                case 0:
                                    this.getSpace().showText(this, Locale.getStr(RS.rs_YouEnterThePortal));
                                    break;
                                case 1:
                                    this.getSpace().showText(this, Locale.getStr(RS.rs_YouDiveIntoVortex));
                                    break;
                                case 2:
                                    this.getSpace().showText(this, Locale.getStr(RS.rs_YouDiveIntoWhirlpool));
                                    break;
                                case 3:
                                    this.getSpace().showText(this, Locale.getStr(RS.rs_YouFallIntoWhirlpoolVortex));
                                    break;
                            }
                        }

                        newLayer = gate.TargetLayer;
                        fx = (int) gate.TargetField.X;
                        fy = (int) gate.TargetField.Y;
                        px = (int) gate.TargetPos.X;
                        py = (int) gate.TargetPos.Y;

                        if (px < 0 || py < 0) {
                            fld = this.getSpace().getField(newLayer, fx, fy);
                            Point pt = this.searchRndLocation(fld, fld.getAreaRect());
                            px = pt.X;
                            py = pt.Y;
                        }
                        gpChanged = true;
                    }
                }
            }

            fld = this.getSpace().getField(newLayer, fx, fy);
            if (fld == null) {
                return;
            }

            if (this.isPlayer() && fld.LandID == GlobalVars.Land_Bifrost && this.getSpace().BifrostCollapsed) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_YouCannotReturnAcrossBifrost));
                return;
            }

            if (this.canMove(fld, px, py)) {
                boolean res = true;
                NWCreature creature = (NWCreature) fld.findCreature(px, py);
                if (creature != null) {
                    if (!gpChanged && this.isEnemy(creature)) {
                        this.attackTo(AttackKind.akMelee, creature, null, null);
                        res = false;
                    } else {
                        res = creature.request(this, RequestKind.rk_PlaceYield);
                    }
                }

                if (res) {
                    this.transferTo(newLayer, fx, fy, px, py, fld.getAreaRect(), false, true);
                }
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.moveTo(): " + ex.getMessage());
        }
    }

    public void moveToDown()
    {
        this.moveToGate(false);
    }

    public void moveToUp()
    {
        if (this.isPlayer() && this.LayerID == GlobalVars.Layer_MimerWell) {
            Point MimerWell = this.getSpace().MimerWellPos;
            this.transferTo(GlobalVars.Layer_Midgard, 5, 2, MimerWell.X, MimerWell.Y, StaticData.MapArea, true, true);
        } else {
            this.moveToGate(true);
        }
    }

    private void moveToGate(boolean aUp)
    {
        if (this.checkMove(new Movements(Movements.mkWalk, Movements.mkFly))) {
            NWField fld = (NWField) this.getCurrentMap();
            int px = super.getPosX();
            int py = super.getPosY();

            NWTile tile = fld.getTile(px, py);
            int fg = tile.getForeBase();

            Gate gate = fld.findGate(px, py);
            if (gate == null) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_HereNoStairwaysOrHoles));
            } else {
                if (aUp) {
                    if (fg != PlaceID.pid_StairsUp && fg != PlaceID.pid_GStairsUp && fg != PlaceID.pid_HoleUp) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_CannotGoUp));
                        return;
                    }
                } else {
                    boolean isMimerWell = (fg == PlaceID.pid_Well && fld.LandID == GlobalVars.Land_MimerRealm);

                    if (fg != PlaceID.pid_StairsDown && fg != PlaceID.pid_GStairsDown && fg != PlaceID.pid_HoleDown && !isMimerWell) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_CannotGoDown));
                        return;
                    }
                }

                if (fg == PlaceID.pid_HoleUp && !this.hasMovement(Movements.mkFly) && this.fItems.findByCLSID(GlobalVars.iid_LazlulRope) == null) {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_NoRope));
                } else {
                    int newLayer = gate.TargetLayer;
                    int fx = (int) gate.TargetField.X;
                    int fy = (int) gate.TargetField.Y;
                    px = (int) gate.TargetPos.X;
                    py = (int) gate.TargetPos.Y;

                    fld = this.getSpace().getField(newLayer, fx, fy);
                    if (fld == null) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_ThisPassageGoesNowhere));
                    } else {
                        this.transferTo(newLayer, fx, fy, px, py, fld.getAreaRect(), true, true);
                    }
                }
            }
        }
    }

    private void enterSpace(NWField field, NWTile tile)
    {
        try {
            if (field.LandID != GlobalVars.Land_Bifrost) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_YouFallIntoSpace));
                this.getSpace().showText(this, Locale.getStr(RS.rs_YourBodyWillBeLost));
                this.death(Locale.getStr(RS.rs_DiedInExpanseOfNowhere), null);
                return;
            }

            this.getSpace().showText(this, Locale.getStr(RS.rs_YouFallToNiflheim));
            this.transferTo(GlobalVars.Layer_Niflheim, -1, -1, -1, -1, StaticData.MapArea, false, false);

            // my fantasy, not from original
            if (AuxUtils.chance(10)) {
                this.death(Locale.getStr(RS.rs_FellToNiflheim), null);
            } else {
                this.getSpace().showText(this, Locale.getStr(RS.rs_YouLandGently));
            }

            AuxUtils.exStub("todo: on go down in space -  rs_YouDescendThroughSpace");
        } catch (Exception ex) {
            Logger.write("NWCreature.enterSpace(): " + ex.getMessage());
            throw ex;
        }
    }

    private void enterTrap(NWField field, NWTile tile, int pFore)
    {
        try {
            int idx = this.getTrapListIndex(pFore);
            boolean entrapped = idx >= 0 && (!this.getMovements().hasIntersect(StaticData.dbTraps[idx].EscMovements));
            if (entrapped) {
                this.useEffect(StaticData.dbPlaces[pFore].Effect, tile, InvokeMode.im_Use, null);
                this.getSpace().doEvent(EventID.event_Trap, this, null, tile);
                if (StaticData.dbTraps[idx].Disposable) {
                    tile.setFore(PlaceID.pid_Undefined);
                } else {
                    tile.Trap_Discovered = true;
                }
            } else {
                String msg = Locale.getStr(StaticData.dbTraps[idx].EscapeMsgRS);
                this.getSpace().showText(this, msg);
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.enterTrap(): " + ex.getMessage());
            throw ex;
        }
    }

    private void enterFog(NWField field, NWTile tile)
    {
        try {
            if (tile.FogID == PlaceID.pid_Fog) {
                this.setInFog(true);

                if (tile.FogAge > 0 && this.getAbility(AbilityID.Resist_Acid) <= 0) {
                    this.applyDamage(AuxUtils.getBoundedRnd(1, 40), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_YourBodyIsCoveredWithAcid));
                }
            } else {
                this.setInFog(false);
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.enterFog(): " + ex.getMessage());
            throw ex;
        }
    }

    public void enterPlace(NWField field, NWTile tile)
    {
        try {
            int bg = tile.getBackBase();
            int fg = tile.getForeBase();

            Effect ef = this.fEffects.findEffectByID(EffectID.eid_Sail);
            if (ef != null && bg != PlaceID.pid_Water) {
                this.fEffects.remove(ef);
                EffectsFactory.unloadShip((Player) this);
            }

            if (bg == PlaceID.pid_Space && (!this.getMovements().contains(Movements.mkFly))) {
                this.enterSpace(field, tile);
            }

            if (field.isTrap(super.getPosX(), super.getPosY())) {
                this.enterTrap(field, tile, fg);
            }

            this.enterFog(field, tile);
        } catch (Exception ex) {
            Logger.write("NWCreature.enterPlace(): " + ex.getMessage());
            throw ex;
        }
    }

    public void leavePlace(NWField field, NWTile tile)
    {
    }

    public void checkTile(boolean aHere)
    {
        try {
            NWField map = (NWField) this.getCurrentMap();
            if (map == null) {
                return;
            }

            NWTile tile = map.getTile(super.getPosX(), super.getPosY());
            if (tile == null) {
                return;
            }

            if (aHere) {
                tile.CreaturePtr = this;
                this.enterPlace(map, tile);
            } else {
                this.leavePlace(map, tile);
                tile.CreaturePtr = null;
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.checkTile(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void ProwlingBegin(EffectID effectID)
    {
        try {
            if (this.isPlayer()) {
                ((Player) this).clearMercenaries();
            }

            if (effectID == EffectID.eid_Lycanthropy) {
                this.dropAll();
                this.getSpace().showText(this, Locale.getStr(RS.rs_Lycanthropy_Beg));
            } else {
                this.getSpace().showText(this, Locale.getStr(RS.rs_Insanity_Beg));
            }

            ByteArrayOutputStream memStream = new ByteArrayOutputStream();
            this.saveToStream(new BinaryOutputStream(memStream, AuxUtils.binEndian), NWGameSpace.RGF_Version);
            this.ProwlImage = memStream.toByteArray();

            this.FProwlSource = effectID;
            this.Prowling = true;
            if (this.isPlayer()) {
                this.getSpace().setTurnState(TurnState.gtsSkip);
            }

            if (effectID == EffectID.eid_Lycanthropy) {
                this.initEx(GlobalVars.cid_Werewolf, true, false);
                this.Alignment = AlignmentEx.am_Chaotic_Evil;
            } else {
                this.initBrain();
                this.Alignment = AlignmentEx.am_Chaotic_Neutral;
            }

            this.addEffect(EffectID.eid_Prowling, ItemState.is_Normal, EffectAction.ea_Persistent, true, "");
        } catch (IOException ex) {
            Logger.write("NWCreature.ProwlingBegin.io(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("NWCreature.ProwlingBegin(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void ProwlingEnd()
    {
        try {
            if (this.isPlayer()) {
                if (this.FProwlSource == EffectID.eid_Lycanthropy) {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_RestoreForm));
                    // rs_Lycanthropy_End is deprecated
                } else {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_Insanity_End));
                }
                this.getSpace().setTurnState(TurnState.gtsDone);
            }
            this.Prowling = false;
            this.FProwlSource = EffectID.eid_None;

            int lid = this.LayerID;
            int fx = this.fField.X;
            int fy = this.fField.Y;
            Point p = super.getLocation();

            ByteArrayInputStream memStream = new ByteArrayInputStream(this.ProwlImage);
            this.loadFromStream(new BinaryInputStream(memStream, AuxUtils.binEndian), NWGameSpace.RGF_Version);
            this.transferTo(lid, fx, fy, p.X, p.Y, StaticData.MapArea, true, false);
            this.ProwlImage = null;
        } catch (IOException ex) {
            Logger.write("NWCreature.ProwlingEnd.io(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("NWCreature.ProwlingEnd(): " + ex.getMessage());
            throw ex;
        }
    }

    public final Reaction getReaction(NWCreature aCreature)
    {
        Reaction result = Reaction.rNeutral;

        AlignmentEx a1 = this.Alignment;
        int lc1 = AlignmentEx.getLC(a1);

        AlignmentEx a2 = aCreature.Alignment;
        int lc2 = AlignmentEx.getLC(a2);

        if (lc1 == AlignmentEx.am_Mask_Lawful || lc1 == AlignmentEx.am_Mask_LCNeutral) {
            int ge = AlignmentEx.getGE(a1);
            int ge2 = AlignmentEx.getGE(a2);
            result = NWCreature.relations[ge - 1][ge2 - 1];
        } else if (lc1 == AlignmentEx.am_Mask_Evil) {
            result = Reaction.forValue(AuxUtils.getRandom(Reaction.rAlly.getValue() + 1));
        }

        return result;
    }

    public final boolean request(NWCreature aSender, RequestKind aKind)
    {
        boolean result = false;
        if (this.isPlayer()) {
            return result;
        }

        if (aKind == RequestKind.rk_PlaceYield) {
            if (!this.fEntry.Flags.contains(CreatureFlags.esPlant)) {
                NWField fld = (NWField) this.getCurrentMap();
                int nX;
                int nY;

                for (int dir = Directions.dtFlatFirst; dir <= Directions.dtFlatLast; dir++) {
                    nX = super.getPosX() + Directions.Data[dir].dX;
                    nY = super.getPosY() + Directions.Data[dir].dY;

                    if (fld.isValid(nX, nY) && this.canMove(fld, nX, nY) && fld.findCreature(nX, nY) == null) {
                        this.moveTo(nX, nY);
                        result = true;
                        break;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        try {
            super.loadFromStream(stream, version);

            this.Sex = CreatureSex.forValue(StreamUtils.readByte(stream));
            this.fState = CreatureState.forValue(StreamUtils.readByte(stream));
            this.Alignment = AlignmentEx.forValue(StreamUtils.readByte(stream));
            this.fIsTrader = StreamUtils.readBoolean(stream);
            this.fName = StreamUtils.readString(stream);
            this.Turn = StreamUtils.readInt(stream);
            this.LayerID = StreamUtils.readInt(stream);
            this.fField.X = StreamUtils.readInt(stream);
            this.fField.Y = StreamUtils.readInt(stream);
            this.Level = StreamUtils.readInt(stream);
            this.fExperience = StreamUtils.readInt(stream);
            this.Strength = StreamUtils.readInt(stream);
            this.fSpeed = StreamUtils.readInt(stream);
            this.Attacks = StreamUtils.readInt(stream);
            this.ToHit = StreamUtils.readInt(stream);
            this.Luck = StreamUtils.readInt(stream);
            this.Constitution = StreamUtils.readInt(stream);
            this.ArmorClass = StreamUtils.readInt(stream);
            this.fDamageBase = StreamUtils.readInt(stream);
            this.HPMax = StreamUtils.readInt(stream);
            this.HPCur = StreamUtils.readInt(stream);
            this.MPMax = StreamUtils.readInt(stream);
            this.MPCur = StreamUtils.readInt(stream);
            this.DBMin = StreamUtils.readInt(stream);
            this.DBMax = StreamUtils.readInt(stream);

            this.fEffects.loadFromStream(stream, version);
            int num = this.fEffects.getCount();
            for (int i = 0; i < num; i++) {
                this.fEffects.getItem(i).Owner = this;
            }

            this.fItems.loadFromStream(stream, version);
            int num2 = this.fItems.getCount();
            for (int i = 0; i < num2; i++) {
                this.fItems.getItem(i).Owner = this;
            }

            this.fAbilities.loadFromStream(stream, version);
            this.fSkills.loadFromStream(stream, version);

            this.fIsMercenary = StreamUtils.readBoolean(stream);
            this.Perception = (byte) StreamUtils.readByte(stream);
            this.Dexterity = (short) StreamUtils.readWord(stream);
            this.FSurvey = (byte) StreamUtils.readByte(stream);
            this.Hear = (byte) StreamUtils.readByte(stream);
            this.Smell = (byte) StreamUtils.readByte(stream);
            this.LastDir = (StreamUtils.readByte(stream));

            this.initBody();
            this.initBrain();

            NWBrainEntity brain = (NWBrainEntity) super.fBrain;
            if (brain != null) {
                brain.loadFromStream(stream, version);
            }

            if (this.fBody != null) {
                ((CustomBody) this.fBody).loadFromStream(stream, version);
            }

            // FIXME: bad code
            if (this.fIsMercenary) {
                this.resetMercenary(this.fIsMercenary);
            }

            if (this.fState != CreatureState.csDead) {
                this.checkTile(true);
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.loadFromStream(): " + ex.getMessage());
            Logger.write("NWCreature.avail: " + String.valueOf(stream.available()));
            throw ex;
        }
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        try {
            super.saveToStream(stream, version);

            StreamUtils.writeByte(stream, (byte) this.Sex.Value);
            StreamUtils.writeByte(stream, (byte) this.fState.getValue());
            StreamUtils.writeByte(stream, (byte) (this.Alignment.getValue()));
            StreamUtils.writeBoolean(stream, this.fIsTrader);
            StreamUtils.writeString(stream, this.fName);
            StreamUtils.writeInt(stream, this.Turn);
            StreamUtils.writeInt(stream, this.LayerID);
            StreamUtils.writeInt(stream, this.fField.X);
            StreamUtils.writeInt(stream, this.fField.Y);
            StreamUtils.writeInt(stream, this.Level);
            StreamUtils.writeInt(stream, this.fExperience);
            StreamUtils.writeInt(stream, this.Strength);
            StreamUtils.writeInt(stream, this.fSpeed);
            StreamUtils.writeInt(stream, this.Attacks);
            StreamUtils.writeInt(stream, this.ToHit);
            StreamUtils.writeInt(stream, this.Luck);
            StreamUtils.writeInt(stream, this.Constitution);
            StreamUtils.writeInt(stream, this.ArmorClass);
            StreamUtils.writeInt(stream, this.fDamageBase);
            StreamUtils.writeInt(stream, this.HPMax);
            StreamUtils.writeInt(stream, this.HPCur);
            StreamUtils.writeInt(stream, this.MPMax);
            StreamUtils.writeInt(stream, this.MPCur);
            StreamUtils.writeInt(stream, this.DBMin);
            StreamUtils.writeInt(stream, this.DBMax);

            this.fEffects.saveToStream(stream, version);
            this.fItems.saveToStream(stream, version);
            this.fAbilities.saveToStream(stream, version);
            this.fSkills.saveToStream(stream, version);

            StreamUtils.writeBoolean(stream, this.fIsMercenary);
            StreamUtils.writeByte(stream, this.Perception);
            StreamUtils.writeWord(stream, this.Dexterity);
            StreamUtils.writeByte(stream, this.FSurvey);
            StreamUtils.writeByte(stream, this.Hear);
            StreamUtils.writeByte(stream, this.Smell);
            StreamUtils.writeByte(stream, (byte) (this.LastDir));

            NWBrainEntity brain = (NWBrainEntity) super.fBrain;
            if (brain != null) {
                brain.saveToStream(stream, version);
            }

            if (this.fBody != null) {
                ((CustomBody) this.fBody).saveToStream(stream, version);
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.saveToStream(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void setPos(int posX, int posY)
    {
        try {
            this.LastDir = Directions.getDirByCoords(super.getPosX(), super.getPosY(), posX, posY);
            super.setPos(posX, posY);
        } catch (Exception ex) {
            Logger.write("NWCreature.setPos(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void copyTotalLocation(NWCreature other)
    {
        this.LayerID = other.LayerID;
        this.fField = other.getField().clone();
        this.setPos(other.getPosX(), other.getPosY());
    }

    public final void shootTo(NWCreature enemy, Item weapon)
    {
        try {
            Item projectile = null;
            ItemFlags wks = weapon.getFlags();
            AttackKind kind = AttackKind.akMelee;

            if (wks.contains(ItemFlags.if_ShootWeapon)) {
                kind = AttackKind.akShoot;
                projectile = this.getItemByEquipmentKind(BodypartType.bp_Back);
            } else {
                if (wks.contains(ItemFlags.if_ThrowWeapon)) {
                    kind = AttackKind.akThrow;
                    projectile = weapon;
                }
            }

            if (projectile != null) {
                int range = getProjectileRange(projectile);
                this.shootToInternal(kind, enemy.getPosX(), enemy.getPosY(), range, weapon, projectile);
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.shootTo(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void shootToDir(int dir)
    {
        try {
            if (this.isInWater()) {
                if (this.isPlayer()) {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_YouCannotThrowUnderwater));
                }
                return;
            }

            Item weapon = this.getItemByEquipmentKind(BodypartType.bp_RHand);
            ItemFlags wfs = (weapon != null) ? weapon.getFlags() : new ItemFlags();

            AttackKind kind;
            Item projectile;

            if (weapon != null) {
                if (wfs.contains(ItemFlags.if_ShootWeapon)) {
                    /*if (!this.isPlayer()) {
                     throw new RuntimeException("not implemented");
                     }*/
                    kind = AttackKind.akShoot;

                    projectile = this.getItemByEquipmentKind(BodypartType.bp_Back);
                    if (projectile == null) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_NoAmmo));
                        return;
                    } else {
                        wfs = projectile.getFlags();
                        if (!wfs.contains(ItemFlags.if_Projectile)) {
                            this.getSpace().showText(this, Locale.getStr(RS.rs_NoAmmo));
                            return;
                        }
                    }
                } else if (wfs.contains(ItemFlags.if_ThrowWeapon)) {
                    kind = AttackKind.akThrow;
                    projectile = weapon;
                } else {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_WeaponNotChoosed));
                    return;
                }
            } else {
                this.getSpace().showText(this, Locale.getStr(RS.rs_WeaponNotChoosed));
                return;
            }

            int range = getProjectileRange(projectile);
            int tx = super.getPosX() + Directions.Data[dir].dX * range;
            int ty = super.getPosY() + Directions.Data[dir].dY * range;
            if (!this.shootToInternal(kind, tx, ty, range, weapon, projectile)) {
                this.getSpace().showText(this, Locale.getStr(RS.rs_CannotShoot));
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.shootToDir(): " + ex.getMessage());
            throw ex;
        }
    }

    private boolean shootToInternal(AttackKind attackKind, int tX, int tY, int range, Item weapon, Item projectile)
    {
        boolean result = false;

        try {
            NWField fld = (NWField) this.getCurrentMap();

            ItemFlags pfs = projectile.getFlags();
            if (pfs.hasIntersect(ItemFlags.if_ThrowWeapon, ItemFlags.if_Projectile)) {
                if (projectile.Count > 1) {
                    projectile = projectile.getSeveral(1);
                    fld.getItems().add(projectile, false);
                } else {
                    this.removeItem(projectile);

                    if (!projectile.getInUse()) {
                        this.dropItem(projectile);
                    } else {
                        projectile = null;
                    }
                }

                if (projectile != null) {
                    Projectile projObj = new Projectile(this, fld, attackKind, range, weapon, projectile);
                    projObj.run(tX, tY);

                    result = true;
                }
            }
        } catch (Exception ex) {
            Logger.write("NWCreature.shootToInternal(): " + ex.getMessage());
            throw ex;
        }

        return result;
    }

    public final void resetStamina()
    {
        this.Stamina += this.getSpeed();
    }

    public final void switchBody(NWCreature target)
    {
        super.setCLSID(target.CLSID);
        this.setState(target.getState());
        this.Alignment = target.Alignment;
        this.Sex = target.Sex;
        this.Level = target.Level;
        this.fExperience = target.fExperience;
        this.Strength = target.Strength;
        this.fSpeed = target.fSpeed;
        this.Attacks = target.Attacks;
        this.ToHit = target.ToHit;
        this.Luck = target.Luck;
        this.Constitution = target.Constitution;
        this.Dexterity = target.Dexterity;
        this.FSurvey = target.FSurvey;
        this.Hear = target.Hear;
        this.Smell = target.Smell;
        this.ArmorClass = target.ArmorClass;
        this.HPMax = target.HPMax;
        this.HPCur = target.HPCur;
        this.DBMin = target.DBMin;
        this.DBMax = target.DBMax;
        this.Perception = target.Perception;
        this.fAbilities.assign(target.fAbilities, AttributeList.lao_Or);
        this.fSkills.assign(target.fSkills, AttributeList.lao_Or);
        this.dropAll();
        target.getItems().setOwnsObjects(false);
        this.getItems().assign(target.getItems());
        this.prepareItems();
    }

    public void transferTo(int layerID, int fX, int fY, int pX, int pY, Rect area, boolean obligatory, boolean controlled)
    {
        try {
            if (fX < 0 || fY < 0 || pX < 0 || pY < 0) {
                AuxUtils.randomize();

                NWLayer lr = this.getSpace().getLayerByID(layerID);
                int tries = 10;
                boolean res;
                do {
                    if (fX < 0 || fY < 0) {
                        fX = AuxUtils.getRandom(lr.getW());
                        fY = AuxUtils.getRandom(lr.getH());
                    }

                    if (pX < 0 || pY < 0) {
                        NWField fld = lr.getField(fX, fY);
                        Point pos = this.searchRndLocation(fld, area);
                        res = (pos != null);
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

            this.getSpace().doEvent(EventID.event_Move, this, null, this);

            this.checkTile(false);
            this.setGlobalPos(layerID, fX, fY, obligatory);
            this.setPos(pX, pY);
            this.checkTile(true);
        } catch (Exception ex) {
            Logger.write("NWCreature.transferTo(): " + ex.getMessage());
            throw ex;
        }
    }

    public void useEffect(EffectID effectID, Object source, InvokeMode invokeMode, EffectExt ext)
    {
        Effect.invokeEffect(effectID, this, source, invokeMode, EffectAction.ea_Instant, ext);
    }

    public final void useItem(Item item, EffectExt extData)
    {
        boolean fin_remove = false;

        ItemsList itemContainer = item.getItemContainer();
        this.getSpace().doEvent(EventID.event_ItemUse, this, null, item);
        switch (item.getKind()) {
            case ik_DeadBody:
            case ik_Food: {
                if (this.isPlayer()) {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_YouEat) + item.getDeclinableName(Number.nSingle, Case.cAccusative, this.isBlindness()) + ".");
                    ((Player) this).setSatiety((short) (((Player) this).getSatiety() + item.getSatiety()));

                    this.useEffect(EffectID.eid_FoodEat, item, InvokeMode.im_Use, null);

                    if (item.getKind() == ItemKind.ik_DeadBody) {
                        NWCreature dead = (NWCreature) item.getContents().getItem(0);
                        if (dead.Turn > -5) {
                            this.getSpace().showText(this, Locale.getStr(RS.rs_MeatWasOld));
                        } else {
                            this.getSpace().showText(this, Locale.format(RS.rs_Unsavoury, new Object[]{dead.getDeclinableName(Number.nSingle, Case.cGenitive)}));
                        }
                        boolean cannibalism = (this.fEntry.Race == RaceID.crDefault && super.CLSID == dead.CLSID) || this.fEntry.Race == dead.fEntry.Race;
                        if (cannibalism) {
                            this.getSpace().showText(this, Locale.getStr(RS.rs_CannibalismIsImmoral));
                            ((Player) this).Morality = (byte) ((int) ((Player) this).Morality - 25);
                        }
                    }
                }
                item.applyEffects(this, InvokeMode.im_Use, extData);
                fin_remove = true;
                break;
            }
            case ik_Potion:
            case ik_Scroll: {
                if (this.isPlayer()) {
                    if (item.getEntry().ItmKind == ItemKind.ik_Scroll) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_YouRead) + item.getDeclinableName(Number.nSingle, Case.cAccusative, this.isBlindness()) + ".");
                        this.getSpace().showText(this, Locale.getStr(RS.rs_ScrollDisappears));
                        if (this.isConfused()) {
                            this.getSpace().showText(this, Locale.getStr(RS.rs_GarbleWords));
                        }
                    }
                    if (item.getEntry().ItmKind == ItemKind.ik_Potion) {
                        this.getSpace().showText(this, Locale.getStr(RS.rs_YouDrink) + item.getDeclinableName(Number.nSingle, Case.cAccusative, this.isBlindness()) + ".");
                    }
                }
                item.applyEffects(this, InvokeMode.im_Use, extData);
                fin_remove = true;
                if (item.getEntry().ItmKind == ItemKind.ik_Potion) {
                    Item.genItem(this, GlobalVars.iid_Vial, 1, true);
                }
                break;
            }
            case ik_Tool:
            case ik_MusicalTool:
                item.applyEffects(this, InvokeMode.im_Use, extData);
                break;

            case ik_Wand: {
                if (this.isInWater()) {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_ZappingNotWorkUnderwater));
                    return;
                }
                if (item.Bonus > 0) {
                    item.applyEffects(this, InvokeMode.im_Use, extData);
                    item.Bonus--;
                } else {
                    this.getSpace().showText(this, Locale.getStr(RS.rs_NoCharges));
                    fin_remove = true;
                }
                break;
            }
            case ik_Misc:
                // dummy
                break;
        }

        this.checkActionAbility(CreatureAction.caItemUse, item);

        if (itemContainer.indexOf(item) >= 0) {
            if (!item.getIdentified()) {
                item.setIdentified(true);
            }
            if (fin_remove) {
                if (item.isCountable() && item.Count > 0) {
                    item.Count = (short) ((int) item.Count - 1);
                }
                if (!item.isCountable() || (item.isCountable() && item.Count == 0)) {
                    itemContainer.remove(item);
                }
            }
        }
    }

    public final void useSkill(SkillID skill, EffectExt aExt)
    {
        try {
            this.useEffect(skill.Effect, null, InvokeMode.im_ItSelf, aExt);
        } catch (Exception ex) {
            Logger.write("NWCreature.useSkill(): " + ex.getMessage());
        }
    }

    public final void useSpell(EffectID spellEffect, EffectExt ext)
    {
        int eid = spellEffect.getValue();
        EffectRec efRec = EffectsData.dbEffects[eid];
        if (this.MPCur < (int) efRec.MPReq) {
            this.getSpace().showText(this, Locale.getStr(RS.rs_NoEnergyToSpellcast));
        } else {
            this.MPCur -= (int) efRec.MPReq;
            this.useEffect(spellEffect, null, InvokeMode.im_ItSelf, ext);
        }
    }

    public final int getAttackExp(NWCreature enemy)
    {
        float div = (this.Level + enemy.Level);
        float dLevel = (div == 0.0f) ? 0 : (enemy.Level / div);

        if (dLevel == 0f) {
            dLevel = 1f;
        }

        int result = (int) (Math.round((enemy.HPMax * (double) dLevel)));
        if (result <= 0) {
            result = 1;
        }
        return result;
    }

    public final float getAttackRate(NWCreature enemy, int kinsfolks)
    {
        float result;
        try {
            float s_daf = (((this.DBMin + this.DBMax) / 2.0f * this.Attacks));
            float t_daf = (((enemy.DBMin + enemy.DBMax) / 2.0f * enemy.Attacks));
            float s_af;
            if (s_daf == 0f) {
                s_af = 1f;
            } else {
                s_af = ((enemy.HPCur / (float) s_daf));
            }
            float t_af;
            if (t_daf == 0f) {
                t_af = 1f;
            } else {
                t_af = ((this.HPCur / (float) t_daf));
            }
            float kf = ((1.0f / ((float) kinsfolks * 0.6f + 1.0f)));
            result = ((s_af / (s_af + t_af) / kf));
        } catch (Exception ex) {
            result = 0.5f;
            Logger.write("getAttackRate(): " + ex.getMessage());
        }
        return result;
    }
    
    public final StringList getProps()
    {
        StringList props = new StringList();
        
        int mpr;
        if (this.MPMax == 0) {
            mpr = 0;
        } else {
            mpr = (int) (Math.round((this.MPCur / this.MPMax) * 100.0f));
        }

        props.add(this.getName());
        props.add(this.getRace() + ", " + Locale.getStr(StaticData.dbSex[this.Sex.Value].NameRS));
        props.add("  ");
        props.add(Locale.getStr(RS.rs_Level) + ": " + String.valueOf(this.Level));
        props.add(Locale.getStr(RS.rs_Experience) + ": " + String.valueOf(this.getExperience()));
        props.add(Locale.getStr(RS.rs_Perception) + ": " + String.valueOf((int) this.Perception));
        props.add(Locale.getStr(RS.rs_Constitution) + ": " + String.valueOf(this.Constitution));
        props.add(Locale.getStr(RS.rs_Strength) + ": " + String.valueOf(this.Strength));
        props.add(Locale.getStr(RS.rs_Speed) + ": " + String.valueOf(this.getSpeed()));
        props.add(Locale.getStr(RS.rs_Luck) + ": " + String.valueOf(this.Luck));
        props.add(Locale.getStr(RS.rs_Armor) + ": " + String.valueOf(this.ArmorClass));
        props.add(Locale.getStr(RS.rs_HP) + ": " + String.valueOf((int) (Math.round(((float) this.HPCur / (float) this.HPMax) * 100.0f))) + " %");
        props.add(Locale.getStr(RS.rs_MP) + ": " + String.valueOf(mpr) + " %");
        props.add(Locale.getStr(RS.rs_Damage) + ": " + String.valueOf(this.DBMin) + "-" + String.valueOf(this.DBMax));
        props.add(Locale.getStr(RS.rs_Dexterity) + ": " + String.valueOf((int) this.Dexterity));

        return props;
    }

    /**
     * Check availability of the Item from the signature.
     * <b>Need for scripting</b>.
     * @param sign sign of item
     * @return availability
     */
    public final boolean hasItem(String sign)
    {
        return (this.findItem(sign) != null);
    }

    /**
     * Return the item from the signature.
     * <b>Need for scripting</b>.
     * @param sign sign of item
     * @return
     */
    public final Item findItem(String sign)
    {
        for (int i = 0; i < this.fItems.getCount(); i++) {
            Item item = this.fItems.getItem(i);
            if (item.getEntry().Sign.equals(sign)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * 
     * <b>Need for scripting</b>.
     * @param acceptor
     * @param sign
     * @return
     */
    public final boolean transferItem(NWCreature acceptor, String sign)
    {
        Item item = this.findItem(sign);
        if (item != null) {
            item = ((Item) this.getItems().extract(item));
            acceptor.addItem(item);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * <b>Need for scripting</b>.
     * @return
     */
    public final boolean sacrificeVictim()
    {
        Player player = this.getSpace().getPlayer();
        
        this.getSpace().setVolatileState(this.CLSID, VolatileState.vesDestroyed);
        this.death(Locale.format(RS.rs_VictimKill, new Object[]{this.getName()}), null);

        VolatileState a = this.getSpace().getVolatileState(GlobalVars.cid_Agnar);
        VolatileState h = this.getSpace().getVolatileState(GlobalVars.cid_Haddingr);
        VolatileState k = this.getSpace().getVolatileState(GlobalVars.cid_Ketill);
        if (a == VolatileState.vesDestroyed && h == VolatileState.vesDestroyed && k == VolatileState.vesDestroyed) {
            this.getSpace().addItemEx(GlobalVars.Layer_Svartalfheim3, 1, 2, player.getPosX(), player.getPosY(), GlobalVars.iid_Gungnir);
        }

        return true;
    }

    /**
     *
     * <b>Need for scripting</b>.
     * @return
     */
    public final boolean freeVictim()
    {
        Player player = this.getSpace().getPlayer();
        
        this.initEx(GlobalVars.cid_Norseman, true, false);
        player.recruitMercenary(null, this, false);

        if (this.getSpace().getVolatileState(GlobalVars.cid_Vidur) == VolatileState.vesNone) {
            Point pt = player.getNearestPlace(5, true);
            this.getSpace().addCreatureEx(GlobalVars.Layer_Svartalfheim3, 1, 2, pt.X, pt.Y, GlobalVars.cid_Vidur);
            this.getSpace().showText(player, Locale.getStr(RS.rs_Vidur_IsAngered));
        }

        return true;
    }
    
    /**
     *
     * <b>Need for scripting</b>.
     * @return
     */
    public final boolean isFieldCleared()
    {
        NWField field = this.getCurrentField();
        for (int i = 0; i < field.getCreatures().getCount(); i++) {
            NWCreature creat = field.getCreatures().getItem(i);
            if (!this.equals(creat) && this.isEnemy(creat)) {
                return false;
            }
        }
        
        return true;
    }
}
