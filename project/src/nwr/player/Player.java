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
package nwr.player;

import jzrlib.core.CreatureEntity;
import jzrlib.core.EntityList;
import jzrlib.core.FileVersion;
import jzrlib.utils.StreamUtils;
import jzrlib.core.body.Bodypart;
import nwr.creatures.BodypartType;
import nwr.creatures.brain.BeastBrain;
import nwr.creatures.brain.LeaderBrain;
import nwr.creatures.brain.PartyFormation;
import nwr.creatures.CreaturesList;
import nwr.creatures.NWCreature;
import nwr.creatures.NamesLib;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.AbilityID;
import nwr.core.types.AlignmentEx;
import nwr.core.types.AttackKind;
import jzrlib.common.CreatureSex;
import nwr.core.types.CreatureState;
import jzrlib.common.DayTime;
import nwr.core.types.ItemKind;
import nwr.core.types.LogFeatures;
import nwr.core.types.PlaceFlags;
import nwr.core.types.PlaceID;
import nwr.core.types.SkillID;
import nwr.core.types.TeachableKind;
import nwr.effects.Effect;
import nwr.effects.EffectExt;
import nwr.effects.EffectID;
import nwr.effects.InvokeMode;
import nwr.game.NWGameSpace;
import nwr.game.ghosts.Ghost;
import nwr.item.Item;
import nwr.main.GlobalVars;
import jzrlib.map.AbstractMap;
import jzrlib.core.Directions;
import jzrlib.map.FOV;
import jzrlib.map.Movements;
import nwr.universe.NWField;
import nwr.universe.NWTile;
import nwr.universe.Region;
import java.io.IOException;
import jzrlib.utils.AuxUtils;
import jzrlib.map.IMap;
import jzrlib.utils.Logger;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.core.StringList;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.RefObject;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Player extends NWCreature
{
    public static final int SatietyMax = 3000;

    private final Craft fCraft;
    private final Faith fFaith;
    private final Memory fMemory;

    private short fSatiety;

    public final Craft getCraft()
    {
        return this.fCraft;
    }

    public final Faith getFaith()
    {
        return this.fFaith;
    }

    public final Memory getMemory()
    {
        return this.fMemory;
    }

    public String DeathReason;
    public int DivinePatron;
    public NWField HalMap;
    public byte Morality;
    
    public int LandID;

    public Player(NWGameSpace space, Object owner)
    {
        super(space, owner);
        super.setCLSID(GlobalVars.cid_Viking);
        super.setName(Locale.getStr(RS.rs_Unknown));
        super.Sex = CreatureSex.csMale;

        this.fMemory = new Memory(null);
        this.HalMap = new NWField(null, null, null);
        this.fFaith = new Faith(this);
        this.fCraft = new Craft(this);

        this.initBrain();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fMemory.dispose();
        }
        super.dispose(disposing);
    }

    private void applyLightEffect()
    {
        int rad = (int) super.getLightRadius();
        if (rad > 0) {
            CreaturesList crList = ((NWField) super.getCurrentMap()).getCreatures();
            int num = crList.getCount();
            for (int i = 0; i < num; i++) {
                NWCreature cr = crList.getItem(i);
                if (AuxUtils.distance(super.getLocation(), cr.getLocation()) <= rad && cr.getEntry().Sign.equals("Phausq")) {
                    cr.death(Locale.getStr(RS.rs_MonsterDestroyedByLight), null);
                }
            }

            // TODO: message for player: \"The light burns you!\"
        }
    }

    public final short getSatiety()
    {
        return this.fSatiety;
    }

    public final void setSatiety(short value)
    {
        if (this.fSatiety != value) {
            if ((int) value > Player.SatietyMax) {
                this.fSatiety = (short) Player.SatietyMax;
            } else {
                this.fSatiety = value;
            }
        }
    }

    /**
     * Attack the enemy using chosen weapon.
     * @param attackKind Kind of attack, internal.
     * @param enemy 
     * @param weapon
     * @param projectile
     * @return Result of attack: true - for successful attack, false - for miss.
     */
    @Override
    public boolean attackTo(AttackKind attackKind, NWCreature enemy, Item weapon, Item projectile)
    {
        try {
            boolean result = super.attackTo(attackKind, enemy, weapon, projectile);

            /*if (result) {
            }*/

            return result;
        } catch (Exception ex) {
            Logger.write("Player.attackTo(): " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean isMercenary()
    {
        return false;
    }

    @Override
    public Movements getMovements()
    {
        Movements result = super.getMovements();

        if (GlobalVars.Debug_Fly) {
            result.include(Movements.mkFly);
        }
        if (GlobalVars.Debug_Swim) {
            result.include(Movements.mkSwim);
        }

        return result;
    }

    @Override
    public byte getSerializeKind()
    {
        return 0;
    }

    @Override
    public byte getSurvey()
    {
        DayTime dayTime = this.getSpace().getDayTime();
        int bonus;
        if (dayTime.getValue() < DayTime.dt_DayFH.getValue() || dayTime.getValue() >= DayTime.dt_Dusk.getValue()) {
            bonus = (int) super.getLightRadius();
        } else {
            bonus = 0;
        }

        NWField fld = (NWField) super.getCurrentMap();
        float f;
        if (fld != null) {
            if (fld.isDark()) {
                f = 0.33f;
            } else {
                f = this.getSpace().getDayTime().RadMod;
            }
        } else {
            f = 1f;
        }

        return (byte) ((Math.round(((int) super.getSurvey() * f))) + bonus);
    }

    @Override
    protected void initBody()
    {
        super.initBody();
    }

    @Override
    protected void initBrain()
    {
        if (this.Prowling) {
            super.fBrain = new BeastBrain(this);
            return;
        }

        super.setBrain(new LeaderBrain(this));
    }

    public final void clearMercenaries()
    {
        if (this.fBrain instanceof LeaderBrain) {
            LeaderBrain leader = ((LeaderBrain) this.fBrain);

            for (int i = leader.getMembersCount() - 1; i >= 1; i--) {
                CreatureEntity member = leader.getMember(i);
                ((NWCreature) member).setIsMercenary(false);
            }
        }
    }

    public final void diagnosisBody(StringList aList)
    {
        int eyes = 0;
        int fingers = 0;

        int num = super.getBody().getPartsCount();
        for (int i = 0; i < num; i++) {
            Bodypart entry = super.getBody().getPart(i);

            if (entry.Type == BodypartType.bp_Eye.getValue() && entry.State == Bodypart.STATE_NORMAL) {
                eyes++;
            }

            if (entry.Type == BodypartType.bp_Finger.getValue() && entry.State == Bodypart.STATE_NORMAL) {
                fingers++;
            }
        }

        if (eyes != 2) {
            aList.add(Locale.getStr(RS.rs_Eyes) + " " + String.valueOf(eyes));
        }

        if (fingers != 10) {
            aList.add(Locale.getStr(RS.rs_Fingers) + " " + String.valueOf(fingers));
        }

        if (aList.getCount() == 0) {
            aList.add(Locale.getStr(RS.rs_PerfectHealth));
        }
    }

    private void prepareHallucinations()
    {
        NWField field = super.getCurrentField();
        this.HalMap = new NWField(null, field.getLayer(), field.getCoords().clone());

        for (int y = 0; y < StaticData.FieldHeight; y++) {
            for (int x = 0; x < StaticData.FieldWidth; x++) {
                NWTile tile = field.getTile(x, y);
                NWTile halTile = this.HalMap.getTile(x, y);
                halTile.assign(tile);

                halTile.setBack(Hallucination.getPlaceID(tile.getBackBase()));
                halTile.setFore(Hallucination.getPlaceID(tile.getForeBase()));
            }
        }
        
        this.HalMap.normalize();
    }

    @Override
    public void doTurn()
    {
        try {
            super.doTurn();

            this.fSatiety -= 1;

            NWField map = (NWField) super.getCurrentMap();
            if (super.isHallucinations()) {
                this.prepareHallucinations();
            }

            // processing Skidbladnir
            Effect ef = this.getEffects().findEffectByID(EffectID.eid_Sail);
            if (ef != null) {
                Item ship = this.findItem("Skidbladnir");
                EntityList members = ship.getContents();

                for (int i = members.getCount() - 1; i >= 0; i--) {
                    NWCreature cr = (NWCreature) members.getItem(i);
                    cr.resetStamina();
                }

                int rest;
                do {
                    rest = 0;
                    for (int i = members.getCount() - 1; i >= 0; i--) {
                        NWCreature cr = (NWCreature) members.getItem(i);
                        if (cr.getState() != CreatureState.csDead) {
                            
                            Effect ef1 = cr.getEffects().findEffectByID(EffectID.eid_Sail);
                            if (ef1 == null) {
                                throw new Exception("gluk!");
                            }
                            
                            cr.doTurn();
                            if (cr.Stamina >= this.getSpeed()) {
                                rest++;
                            }
                        }
                    }
                } while (rest != 0);
            }
            
            if (!super.Prowling) {
                this.applyLightEffect();

                if (!GlobalVars.Debug_Divinity) {
                    if (this.fSatiety == 200) {
                        GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_YouHungry));
                    } else {
                        if (this.fSatiety <= 150 && (int) this.fSatiety % 50 == 0) {
                            this.useEffect(EffectID.eid_Swoon, null, InvokeMode.im_ItSelf, null);
                        } else {
                            if (this.fSatiety <= 0) {
                                super.death(Locale.getStr(RS.rs_Starved), null);
                                return;
                            }
                        }
                    }

                    /*if (super.LayerID == GlobalVars.Layer_Muspelheim) {
                        // TODO: requires additional processing
                    }*/

                    if (super.LayerID == GlobalVars.Layer_Niflheim && super.getAbility(AbilityID.Resist_Cold) == 0) {
                        int num3 = AuxUtils.getRandom(2);
                        if (num3 != 0) {
                            if (num3 == 1) {
                                GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_YouNumbedByColdOfNiflheim));
                                super.death(Locale.getStr(RS.rs_FrozeToDeath), null);
                            }
                        } else {
                            GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_NiflheimFrostDeath));
                            super.death(Locale.getStr(RS.rs_TurnedToIce), null);
                        }
                        return;
                    }

                    if (super.LayerID == GlobalVars.Layer_Midgard && super.getField().X == 1 && super.getField().Y == 6 && super.getAbility(AbilityID.Resist_Heat) == 0) {
                        int num4 = AuxUtils.getRandom(2);
                        if (num4 != 0) {
                            if (num4 == 1) {
                                GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_YouBurntByFlamesOfBifrost));
                            }
                        } else {
                            GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_BifrostFlameDeath));
                        }
                        super.death(Locale.getStr(RS.rs_ConsumedByFire), null);
                        return;
                    }
                }

                if (!GlobalVars.Debug_TestVanaheim && super.LayerID == GlobalVars.Layer_Vanaheim && map.getCreatures().getCount() == 1) {
                    this.Morality = 0;
                    this.transferTo(GlobalVars.Layer_Midgard, 2, 2, -1, -1, StaticData.MapArea, true, false);
                    GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_VanaheimIsEmpty), new LogFeatures(LogFeatures.lfDialog));
                }

                if (this.Morality <= -100 && super.LayerID != GlobalVars.Layer_Niflheim) {
                    this.transferTo(GlobalVars.Layer_Niflheim, 1, 1, -1, -1, StaticData.MapArea, true, false);
                    GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_PlayerIsUnworthy), new LogFeatures(LogFeatures.lfDialog));
                }
            }
        } catch (Exception ex) {
            Logger.write("Player.doTurn(): " + ex.getMessage());
        }
    }

    public final String getMoralityName()
    {
        String result = "";
        if (this.Morality >= 50) {
            result = Locale.getStr(RS.rs_MoralityHigh);
        } else {
            if (this.Morality < 50) {
                result = Locale.getStr(RS.rs_MoralityAverage);
            } else {
                if (this.Morality <= 0) {
                    result = Locale.getStr(RS.rs_MoralityLow);
                } else {
                    if (this.Morality <= -50) {
                        result = Locale.getStr(RS.rs_MoralityAbsent);
                    }
                }
            }
        }
        return result;
    }

    public final int getScrollsCount()
    {
        int result = 0;

        int num = super.getItems().getCount();
        for (int i = 0; i < num; i++) {
            if (super.getItems().getItem(i).getKind() == ItemKind.ik_Scroll) {
                result++;
            }
        }

        return result;
    }

    @Override
    public void init(int creatureID, boolean total, boolean setName)
    {
        try {
            super.init(creatureID, total, setName);

            this.DeathReason = "";
            this.fSatiety = 2250;
            this.Morality = 100;

            if (super.CLSID == GlobalVars.cid_Conjurer) {
                super.MPMax = 5;
                super.MPCur = 5;
                super.setSkill(SkillID.Sk_Spellcasting, 1);
            } else if (super.CLSID == GlobalVars.cid_Woodsman) {
                super.setAbility(AbilityID.Ab_LongBow, 50);
                super.setAbility(AbilityID.Ab_CrossBow, 30);
            }

            int num = GlobalVars.dbKnowledges.getCount();
            for (int i = 0; i < num; i++) {
                this.knowIt(GlobalVars.dbKnowledges.get(i));
            }

            this.knowIt(GlobalVars.cid_Balder);
            this.knowIt(GlobalVars.cid_Thor);
            this.knowIt(GlobalVars.cid_Freyr);
            this.knowIt(GlobalVars.cid_Heimdall);
            this.knowIt(GlobalVars.cid_Odin);
            this.knowIt(GlobalVars.cid_Tyr);
            this.knowIt(GlobalVars.cid_Fenrir);
            this.knowIt(GlobalVars.cid_Loki);
            this.knowIt(GlobalVars.cid_Surtr);
            this.knowIt(GlobalVars.cid_Jormungand);
            this.knowIt(GlobalVars.cid_Garm);
            this.knowIt(GlobalVars.cid_Thokk);

            this.knowIt(GlobalVars.Land_Bifrost);
            this.knowIt(GlobalVars.Layer_Asgard);
            this.knowIt(GlobalVars.Layer_Midgard);
            this.knowIt(GlobalVars.Layer_Svartalfheim1);
            this.knowIt(GlobalVars.Layer_Svartalfheim2);
            this.knowIt(GlobalVars.Layer_Svartalfheim3);

            this.knowIt(GlobalVars.iid_Mimming);
            this.knowIt(GlobalVars.iid_Mjollnir);
            this.knowIt(GlobalVars.iid_Gungnir);
            this.knowIt(GlobalVars.iid_DwarvenArm);
            this.knowIt(GlobalVars.iid_Gjall);
            this.knowIt(GlobalVars.iid_SoulTrapping_Ring);

            this.knowIt(GlobalVars.iid_Skidbladnir);
            this.knowIt(GlobalVars.iid_Wand_Fire);

            this.knowIt(GlobalVars.nwrBase.findEntryBySign("Potion_Speed").GUID);
            this.knowIt(GlobalVars.nwrBase.findEntryBySign("Potion_Strength").GUID);
            this.knowIt(GlobalVars.nwrBase.findEntryBySign("Potion_Curing").GUID);
            this.knowIt(GlobalVars.nwrBase.findEntryBySign("Scroll_DispelHex").GUID);
            this.knowIt(GlobalVars.nwrBase.findEntryBySign("Scroll_Identification").GUID);
            this.knowIt(GlobalVars.nwrBase.findEntryBySign("Scroll_Blessing").GUID);
            this.knowIt(GlobalVars.nwrBase.findEntryBySign("Wand_Light").GUID);
            this.knowIt(GlobalVars.nwrBase.findEntryBySign("Wand_Ice").GUID);
            this.knowIt(GlobalVars.nwrBase.findEntryBySign("Ring_Agility").GUID);

            if (GlobalVars.Debug_DevMode) {
                for (int pd = PlaceID.pid_First; pd <= PlaceID.pid_Last; pd++) {
                    if ((StaticData.dbPlaces[pd].Signs.contains(PlaceFlags.psIsTrap))) {
                        StaticData.dbPlaces[pd].Signs.include(PlaceFlags.psCanCreate);
                    }
                }

                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("Potion_Curing").GUID, 5, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("FlintKnife").GUID, 15, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("Scroll_Extinction").GUID, 3, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("Potion_Mystery").GUID, 5, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("WhiteMushroom").GUID, 1, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("Potion_Hallucination").GUID, 1, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("SpeckledGrowth").GUID, 3, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("Wand_Wishing").GUID, 1, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("Potion_Lycanthropy").GUID, 5, true);

                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("Anvil").GUID, 1, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("Tongs").GUID, 1, true);
                Item.genItem(this, GlobalVars.nwrBase.findEntryBySign("Wand_Fire").GUID, 5, true);

                super.setSkill(SkillID.Sk_Spellcasting, 250);
                super.MPMax = 250;
                super.MPCur = 250;

                super.setSkill(SkillID.Sk_Cartography, 1);
                super.setSkill(SkillID.Sk_DimensionTravel, 1);
                super.setSkill(SkillID.Sk_Alchemy, 1);
                super.setSkill(SkillID.Sk_Diagnosis, 1);
                super.setSkill(SkillID.Sk_Relocation, 1);
                super.setSkill(SkillID.Sk_Writing, 1);
                super.setSkill(SkillID.Sk_Dig, 1);
                super.setSkill(SkillID.Sk_Animation, 1);
                super.setSkill(SkillID.Sk_Ironworking, 1);
                super.setSkill(SkillID.Sk_ArrowMake, 1);
                super.setSkill(SkillID.Sk_Embalming, 1);
                super.setSkill(SkillID.Sk_GolemCreation, 1);
                super.setSkill(SkillID.Sk_Terraforming, 1);
                super.setSkill(SkillID.Sk_Husbandry, 1);
                super.setSkill(SkillID.Sk_Fennling, 1);
                super.setSkill(SkillID.Sk_SlaveUse, 1);
                super.setSkill(SkillID.Sk_Ventriloquism, 1);
                super.setSkill(SkillID.Sk_Taming, 1);
                super.setSkill(SkillID.Sk_MindControl, 1);
                super.setSkill(SkillID.Sk_PsiBlast, 1);
                super.setSkill(SkillID.Sk_HeatRadiation, 1);
                super.setSkill(SkillID.Sk_Prayer, 1);
                super.setSkill(SkillID.Sk_Sacrifice, 1);
                super.setSkill(SkillID.Sk_Divination, 1);
                super.setAbility(AbilityID.Ab_MusicalAcuity, 1);
            }
        } catch (Exception ex) {
            Logger.write("Player.init(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public void removeItem(Item aItem)
    {
        super.removeItem(aItem);
    }

    @Override
    public void wearItem(Item aItem)
    {
        super.wearItem(aItem);
    }

    @Override
    public void knowIt(int entityID)
    {
        super.knowIt(entityID);

        String sign = GlobalVars.nwrBase.getEntry(entityID).Sign;
        if (sign.startsWith("Rune_")) {
            return;
        }
        
        Knowledge knw = (Knowledge) this.fMemory.find(sign);

        if (knw != null) {
            knw.RefsCount++;
            return;
        }

        knw = new Knowledge(this);
        knw.ID = entityID;
        knw.RefsCount = (knw.RefsCount + 1);
        this.fMemory.add(sign, knw);
    }

    @Override
    public void moveTo(int newX, int newY)
    {
        if (super.getEffects().findEffectByID(EffectID.eid_LegsMissing) != null && !super.hasMovement(Movements.mkFly)) {
            GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_YouHaveNoLegs));
        } else {
            if (super.getTotalWeight() > super.getMaxItemsWeight()) {
                GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_YouOverburdened));
            } else {
                super.moveTo(newX, newY);
                if (GlobalVars.nwrWin.getAutoPickup()) {
                    super.pickupAll();
                }
                this.getSpace().showPlaceInfo(super.getPosX(), super.getPosY(), false);
            }
        }
    }

    @Override
    public boolean canMove(IMap map, int aX, int aY)
    {
        boolean result = super.canMove(map, aX, aY);

        NWField fld = (NWField) map;
        int bg = fld.getTile(aX, aY).getBackBase();
        result = result || (bg == PlaceID.pid_Space) || (GlobalVars.Debug_Divinity);

        return result;
    }

    @Override
    public void enterPlace(NWField field, NWTile tile)
    {
        try {
            super.enterPlace(field, tile);

            if (field.LandID == GlobalVars.Land_Crossroads) {
                if (tile.getForeBase() == PlaceID.pid_cr_Disk) {
                    tile.setFore(PlaceID.pid_cr_Disk_Pressed);
                } else {
                    int lid = -1;
                    int fx = 0;
                    int fy = 0;
                    switch ((byte) tile.Background) {
                        case 31: {
                            NWField f = this.getSpace().getRndFieldByLands(GlobalVars.Land_Niflheim, GlobalVars.Land_GiollRiver);
                            if (f != null) {
                                lid = f.getLayer().EntryID;
                                fx = f.getCoords().X;
                                fy = f.getCoords().Y;
                            }
                            break;
                        }
                        case 32:
                        case 33: {
                            NWField f = this.getSpace().getRndFieldByLands(GlobalVars.Land_SlaeterSea, GlobalVars.Land_VidRiver);
                            if (f != null) {
                                lid = f.getLayer().EntryID;
                                fx = f.getCoords().X;
                                fy = f.getCoords().Y;
                            }
                            break;
                        }
                        case 34:
                        case 35: {
                            NWField f = this.getSpace().getRndFieldByLands(GlobalVars.Land_Caves, GlobalVars.Land_DeepCaves, GlobalVars.Land_GreatCaves, GlobalVars.Land_Crypt, GlobalVars.Land_Bazaar);
                            if (f != null) {
                                lid = f.getLayer().EntryID;
                                fx = f.getCoords().X;
                                fy = f.getCoords().Y;
                            }
                            break;
                        }
                        case 36: {
                            NWField f = this.getSpace().getRndFieldByLands(GlobalVars.Land_Nidavellir);
                            if (f != null) {
                                lid = GlobalVars.Layer_Midgard;
                                fx = f.getCoords().X;
                                fy = f.getCoords().Y;
                            }
                            break;
                        }
                        case 37: {
                            NWField f = this.getSpace().getRndFieldByLands(GlobalVars.Land_Forest, GlobalVars.Land_Village, GlobalVars.Land_Jotenheim);
                            if (f != null) {
                                lid = f.getLayer().EntryID;
                                fx = f.getCoords().X;
                                fy = f.getCoords().Y;
                            }
                            break;
                        }
                        case 38: {
                            NWField f = this.getSpace().getRndFieldByLands(GlobalVars.Land_Wasteland, GlobalVars.Land_Muspelheim);
                            if (f != null) {
                                lid = f.getLayer().EntryID;
                                fx = f.getCoords().X;
                                fy = f.getCoords().Y;
                            }
                            break;
                        }
                        case 39:
                        case 40: {
                            lid = GlobalVars.Layer_GrynrHalls;
                            fx = 0;
                            fy = 0;
                            break;
                        }
                        case 41:
                        case 42: {
                            NWField f = this.getSpace().getRndFieldByLands(GlobalVars.Land_Alfheim, GlobalVars.Land_MimerRealm);
                            if (f != null) {
                                lid = f.getLayer().EntryID;
                                fx = f.getCoords().X;
                                fy = f.getCoords().Y;
                            }
                            break;
                        }
                    }

                    if (lid != -1) {
                        this.transferTo(lid, fx, fy, -1, -1, StaticData.MapArea, true, true);
                    }
                }
            } else {
                tile.ScentAge = 100;
                tile.ScentTrail = this;
            }
        } catch (Exception ex) {
            Logger.write("Player.enterPlace(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void leavePlace(NWField field, NWTile tile)
    {
        try {
            super.leavePlace(field, tile);

            if (field.LandID == GlobalVars.Land_Crossroads) {
                int fg = tile.getForeBase();
                if (fg == PlaceID.pid_cr_Disk || fg == PlaceID.pid_cr_Disk_Pressed) {
                    tile.setFore(PlaceID.pid_Undefined);
                    GlobalVars.nwrWin.showText(this, Locale.getStr(RS.rs_DiskFalls));
                }
            }
        } catch (Exception ex) {
            Logger.write("Player.leavePlace(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void recruitMercenary(NWCreature collocutor, NWCreature mercenary, boolean byMoney)
    {
        boolean res = true;
        if (byMoney) {
            int hPrice = (int) mercenary.getHirePrice();
            if (super.getMoney() < hPrice) {
                res = false;
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_NoMoney));
            } else {
                super.subMoney(hPrice);
                collocutor.addMoney(hPrice);
            }
        }

        if (res) {
            mercenary.setIsMercenary(true);

            int dist = AuxUtils.distance(super.getLocation(), mercenary.getLocation());
            Point pt = this.getNearestPlace(3, true);
            if ((dist > (int) this.getSurvey()) && (pt != null)) {
                mercenary.checkTile(false);
                mercenary.setPos(pt.X, pt.Y);
                mercenary.checkTile(true);
            }
        }
    }

    public final void randomName()
    {
        super.setName(this.getSpace().generateName(this, NamesLib.NameGen_NorseDic)); //NameGen_RndSlabs
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        try {
            super.loadFromStream(stream, version);

            this.fSatiety = (short) StreamUtils.readWord(stream);
            this.fMemory.loadFromStream(stream, version);
            this.Morality = (byte) StreamUtils.readByte(stream);
            ((LeaderBrain) super.fBrain).Formation = PartyFormation.forValue(StreamUtils.readByte(stream));
        } catch (Exception ex) {
            Logger.write("Player.loadFromStream(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        super.saveToStream(stream, version);

        StreamUtils.writeWord(stream, (short) this.fSatiety);
        this.fMemory.saveToStream(stream, version);
        StreamUtils.writeByte(stream, (byte) this.Morality);
        StreamUtils.writeByte(stream, (byte) ((LeaderBrain) super.fBrain).Formation.getValue());
    }

    public final Ghost createGhost()
    {
        int landID = this.getCurrentField().LandID;
        if (landID != GlobalVars.Land_Forest && landID != GlobalVars.Land_Caves && landID != GlobalVars.Land_DeepCaves && landID != GlobalVars.Land_GrynrHalls) {
            return null;
        }

        Ghost result = new Ghost(null, null);
        result.assign(this, false);
        result.HPCur = result.HPMax;
        result.Alignment = AlignmentEx.am_Chaotic_Evil;

        result.copyTotalLocation(this);

        result.LastDir = Directions.dtNone;
        result.Ghost = true;

        return result;
    }

    public final void setPartyFormation(PartyFormation formation)
    {
        ((LeaderBrain) super.fBrain).Formation = formation;
    }

    @Override
    public void setPos(int posX, int posY)
    {
        try {
            super.setPos(posX, posY);

            if (!this.Prowling) {
                ((LeaderBrain) super.fBrain).setDir(super.LastDir);
            }

            AbstractMap fld = super.getCurrentMap();
            FOV.FOV_Prepare(fld, false);
            if (super.getEffects().findEffectByID(EffectID.eid_Blindness) == null && !super.getInFog()) {
                int dir;
                if (GlobalVars.nwrWin.getCircularFOV()) {
                    dir = Directions.dtNone;
                } else {
                    dir = super.LastDir;
                }
                FOV.FOV_Start(super.getPosX(), super.getPosY(), (int) super.getSurvey(), dir);
            }

            // this is need for work of the brain goals, also isAvailable and etc
            Effect ef = this.getEffects().findEffectByID(EffectID.eid_Sail);
            if (ef != null) {
                Item ship = this.findItem("Skidbladnir");
                EntityList members = ship.getContents();

                int fx = this.getField().X;
                int fy = this.getField().Y;
                
                for (int i = members.getCount() - 1; i >= 0; i--) {
                    NWCreature cr = (NWCreature) members.getItem(i);
                    cr.setGlobalPos(this.LayerID, fx, fy, true);
                    cr.setPos(posX, posY);
                }
            }
        } catch (Exception ex) {
            Logger.write("Player.setPos(): " + ex.getMessage());
            throw ex;
        }
    }

    public final int getDebt(String aLender)
    {
        Debt debt = (Debt) this.fMemory.find("Debt_" + aLender);
        int result;
        if (debt == null) {
            result = 0;
        } else {
            result = debt.Value;
        }
        return result;
    }

    public final void setRecallPos()
    {
        this.fMemory.delete("RecallPos");
        RecallPos rp = new RecallPos(this);
        rp.Layer = super.LayerID;
        rp.Field = super.getField();
        rp.Pos = super.getLocation();
        this.fMemory.add("RecallPos", rp);
    }

    public final void setSourceForm()
    {
        /*if (this.FMemory.Find("SourceForm") != null) {
         this.FMemory.Delete("SourceForm");
         }*/

        SourceForm sf = new SourceForm(this);
        sf.sfID = super.CLSID;
        this.fMemory.add("SourceForm", sf);
    }

    public final void addDebt(String lender, int value)
    {
        Debt debt = (Debt) this.getMemory().find("Debt_" + lender);
        if (debt == null) {
            debt = new Debt(this);
            debt.Lender = lender;
            this.fMemory.add("Debt_" + lender, debt);
        }
        debt.Value += value;
    }

    public final void subDebt(String lender, int value)
    {
        Debt debt = (Debt) this.getMemory().find("Debt_" + lender);
        if (debt != null) {
            if (debt.Value > value) {
                debt.Value -= value;
            } else {
                this.fMemory.delete("Debt_" + lender);
            }
        }
    }

    public final void teachDiscipline(NWCreature teacher, int teachableIndex, int curLev)
    {
        LeaderBrain party = (LeaderBrain) super.fBrain;
        int id = StaticData.dbTeachable[teachableIndex].id;
        TeachableKind kind = StaticData.dbTeachable[teachableIndex].kind;
        
        int price = (int) this.getSpace().getTeachablePrice(teachableIndex, curLev);
        super.subMoney(price);
        teacher.addMoney(price);

        switch (kind) {
            case Ability: {
                super.setAbility(AbilityID.forValue(id), curLev + 1);

                int num2 = party.getMembersCount();
                for (int i = 1; i < num2; i++) {
                    NWCreature j = (NWCreature) party.getMember(i);
                    j.setAbility(AbilityID.forValue(id), curLev + 1);
                }
            }
            break;

            case Skill: {
                super.setSkill(SkillID.forValue(id), curLev + 1);

                int num = party.getMembersCount();
                for (int i = 1; i < num; i++) {
                    NWCreature j = (NWCreature) party.getMember(i);
                    j.setSkill(SkillID.forValue(id), curLev + 1);
                }
            }
            break;
        }
    }

    public final boolean isSail()
    {
        return (this.getEffects().findEffectByID(EffectID.eid_Sail) != null);
    }

    @Override
    public void transferTo(int layerID, int fX, int fY, int pX, int pY, Rect area, boolean obligatory, boolean controlled)
    {
        try {
            NWField field = this.getSpace().getField(layerID, fX, fY);
            boolean withoutParty = (field.LandID == GlobalVars.Land_Crossroads) || this.isSail();
            
            boolean globalMove = (super.LayerID != layerID || super.getField().X != fX || super.getField().Y != fY);
            boolean partyMove = (globalMove && controlled && !withoutParty);

            super.transferTo(layerID, fX, fY, pX, pY, area, obligatory, controlled);

            /*if (globalMove) {
                // ???
            }*/

            if (partyMove) {
                try {
                    LeaderBrain party = (LeaderBrain) super.fBrain;
                    int num = party.getMembersCount();
                    for (int i = 1; i < num; i++) {
                        NWCreature member = (NWCreature) party.getMember(i);

                        Point pt = member.getNearestPlace(this.getLocation(), 4, true);
                        if (pt != null) {
                            member.transferTo(layerID, fX, fY, pt.X, pt.Y, area, obligatory, true);
                        } else {
                            Logger.write("Player.transferTo().transferParty().getNearestPlace() failed");
                        }
                    }
                } catch (Exception ex) {
                    Logger.write("Player.transferTo().transferParty(): " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            Logger.write("Player.transferTo(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void useEffect(EffectID effectID, Object source, InvokeMode invokeMode, EffectExt ext)
    {
        RefObject<EffectExt> refExt = new RefObject<>(ext);
        boolean paramsValid = Effect.initParams(effectID, this, source, invokeMode, refExt);
        ext = refExt.argValue;

        if (paramsValid) {
            super.useEffect(effectID, source, invokeMode, ext);
        } else {
            GlobalVars.nwrWin.initTarget(effectID, source, invokeMode, ext);
        }
    }

    public final void waitTurn()
    {
        AbstractMap fld = super.getCurrentMap();

        FOV.FOV_Prepare(fld, false);
        if (super.getEffects().findEffectByID(EffectID.eid_Blindness) == null) {
            FOV.FOV_Start(super.getPosX(), super.getPosY(), (int) super.getSurvey(), Directions.dtNone);
        }

        this.checkTile(true);
    }

    public final String getLocationName()
    {
        NWField fld = this.getCurrentField();
        Region region = fld.getRegion(this.getPosX(), this.getPosY());

        return (region != null) ? region.getName() : fld.getLandName();
    }
}
