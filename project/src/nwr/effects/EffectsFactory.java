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
package nwr.effects;

import java.util.ArrayList;
import jzrlib.core.Directions;
import jzrlib.core.EntityList;
import jzrlib.core.ExtList;
import jzrlib.core.GameSpace;
import jzrlib.core.LocatedEntity;
import jzrlib.core.Point;
import jzrlib.core.Range;
import jzrlib.core.Rect;
import jzrlib.map.AbstractMap;
import jzrlib.map.BaseTile;
import jzrlib.map.IMap;
import jzrlib.map.Movements;
import jzrlib.map.TileStates;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.RefObject;
import jzrlib.utils.TextUtils;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.AbilityID;
import nwr.core.types.AlignmentEx;
import jzrlib.common.CreatureSex;
import nwr.core.types.DamageKind;
import nwr.core.types.EventID;
import nwr.core.types.ItemKind;
import nwr.core.types.ItemKinds;
import nwr.core.types.ItemState;
import nwr.core.types.LogFeatures;
import nwr.core.types.PlaceFlags;
import nwr.core.types.PlaceID;
import nwr.core.types.RaceID;
import nwr.core.types.SkillID;
import nwr.core.types.TabooItemRec;
import nwr.core.types.TeachableKind;
import nwr.core.types.TurnState;
import nwr.core.types.VolatileState;
import nwr.creatures.BodypartType;
import nwr.creatures.NWCreature;
import nwr.creatures.brain.LeaderBrain;
import nwr.creatures.brain.SentientBrain;
import nwr.database.CreatureEntry;
import nwr.database.CreatureFlags;
import nwr.database.DataEntry;
import nwr.database.DeclinableEntry;
import nwr.database.ItemEntry;
import nwr.effects.rays.AnnihilationRay;
import nwr.effects.rays.BlackGemRay;
import nwr.effects.rays.CancellationRay;
import nwr.effects.rays.DeanimationRay;
import nwr.effects.rays.DeathRay;
import nwr.effects.rays.FireRay;
import nwr.effects.rays.FireVisionRay;
import nwr.effects.rays.FlayingRay;
import nwr.effects.rays.GrapplingHookRay;
import nwr.effects.rays.IceRay;
import nwr.effects.rays.MonsterSkillRay;
import nwr.effects.rays.PolymorphRay;
import nwr.effects.rays.StoningRay;
import nwr.effects.rays.TransmutationRay;
import nwr.effects.rays.TunnelingRay;
import nwr.engine.BaseSystem;
import jzrlib.grammar.Case;
import jzrlib.grammar.Number;
import nwr.item.Item;
import nwr.item.ItemsList;
import nwr.main.GlobalVars;
import nwr.player.Faith;
import nwr.player.Player;
import nwr.player.RecallPos;
import nwr.player.SourceForm;
import nwr.universe.DungeonRoom;
import nwr.universe.Gate;
import nwr.universe.MapEffect;
import nwr.universe.MapObject;
import nwr.universe.NWField;
import nwr.universe.NWLayer;
import nwr.universe.NWTile;
import nwr.universe.UniverseBuilder;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class EffectsFactory
{
    private static final ItemKinds bless_ik;
    private static final ItemKinds bless_ikArmor;
    private static final TabooItemRec[] dbTabooItems;

    static {
        bless_ik = new ItemKinds(ItemKind.ik_Armor, ItemKind.ik_Potion, ItemKind.ik_Ring, ItemKind.ik_BluntWeapon, ItemKind.ik_Scroll, ItemKind.ik_Amulet, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Clothing, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor, ItemKind.ik_Projectile);
        bless_ikArmor = new ItemKinds(ItemKind.ik_Armor, ItemKind.ik_BluntWeapon, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Clothing, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor);

        dbTabooItems = new TabooItemRec[32];
        dbTabooItems[0] = new TabooItemRec("Amulet_Eternal_Life", "Amulet_Eternal_Life");
        dbTabooItems[1] = new TabooItemRec("BlackSword", "BlackSword");
        dbTabooItems[2] = new TabooItemRec("ChinStrap", "ChinStrap");
        dbTabooItems[3] = new TabooItemRec("GoldenTogs", "GoldenTogs");
        dbTabooItems[4] = new TabooItemRec("Lodestone", "Lodestone");
        dbTabooItems[5] = new TabooItemRec("Wand_Amusement", "Wand_Amusement");
        dbTabooItems[6] = new TabooItemRec("Amulet_Ethereality", "Amulet_Eternal_Life");
        dbTabooItems[7] = new TabooItemRec("Amulet_Might", "Amulet_Eternal_Life");
        dbTabooItems[8] = new TabooItemRec("BlazingCape", "GoldenTogs");
        dbTabooItems[9] = new TabooItemRec("DwarvenArm", "BlackSword");
        dbTabooItems[10] = new TabooItemRec("Gjall", "Lodestone");
        dbTabooItems[11] = new TabooItemRec("Coin", "Lodestone");
        dbTabooItems[12] = new TabooItemRec("GreenStone", "Lodestone");
        dbTabooItems[13] = new TabooItemRec("Gungnir", "BlackSword");
        dbTabooItems[14] = new TabooItemRec("KnowledgeHelm", "ChinStrap");
        dbTabooItems[15] = new TabooItemRec("LazlulRope", "Lodestone");
        dbTabooItems[16] = new TabooItemRec("Mimming", "BlackSword");
        dbTabooItems[17] = new TabooItemRec("Mjollnir", "BlackSword");
        dbTabooItems[18] = new TabooItemRec("Ring_SoulTrapping", "Lodestone");
        dbTabooItems[19] = new TabooItemRec("Runesword", "BlackSword");
        dbTabooItems[20] = new TabooItemRec("Scroll_Diary", "Lodestone");
        dbTabooItems[21] = new TabooItemRec("Scroll_BodiesSwitch", "Lodestone");
        dbTabooItems[22] = new TabooItemRec("Scroll_Transport", "Lodestone");
        dbTabooItems[23] = new TabooItemRec("Scythe", "BlackSword");
        dbTabooItems[24] = new TabooItemRec("Wand_Wishing", "Wand_Amusement");
        dbTabooItems[25] = new TabooItemRec("WarVest", "GoldenTogs");
        dbTabooItems[26] = new TabooItemRec("CrystalGloves", "GoldenTogs");
        dbTabooItems[27] = new TabooItemRec("BlueCube", "Lodestone");
        dbTabooItems[28] = new TabooItemRec("GreyCube", "Lodestone");
        dbTabooItems[29] = new TabooItemRec("OrangeCube", "Lodestone");
        dbTabooItems[30] = new TabooItemRec("NorseBoots", "GoldenTogs");
        dbTabooItems[31] = new TabooItemRec("-Potions of alchemy **", "");
    }

    public static void e_Alliance(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        boolean cursed = (state == ItemState.is_Cursed);
        if (!cursed) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_CeasesAggression));
        }

        Rect r = new Rect(creature.getPosX() - 1, creature.getPosY() - 1, creature.getPosX() + 1, creature.getPosY() + 1);
        ExtList<LocatedEntity> list = creature.getCurrentField().getCreatures().searchListByArea(r);

        int num = list.getCount();
        for (int i = 0; i < num; i++) {
            NWCreature cr = (NWCreature) list.get(i);
            RaceID race = cr.getEntry().Race;
            if (cr == creature || race == RaceID.crAesir || race == RaceID.crEvilGod || race == RaceID.crDaemon) {
                continue;
            }

            if (cursed) {
                cr.Alignment = AlignmentEx.getOppositeAlignment(creature.Alignment, false);
            } else {
                cr.Alignment = creature.Alignment;
            }
        }

        list.dispose();
    }

    public static void e_Amusement(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (AuxUtils.getRandom(5)) {
            case 0: {
                creature.Strength -= 3;
                break;
            }
            case 1: {
                UniverseBuilder.gen_Traps((creature.getCurrentField()), 125);
                break;
            }
            case 2: {
                creature.Constitution--;
                break;
            }
            case 3: {
                creature.ArmorClass -= 10;
                break;
            }
            case 4: {
                creature.transferTo(creature.LayerID, -1, -1, -1, -1, StaticData.MapArea, true, false);
                break;
            }
        }
    }

    public static void e_CheckAnimation(EffectID effectID, NWCreature creature, Object source, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode == InvokeMode.im_ItSelf) {
            ext.ReqParams = new EffectParams(EffectParams.ep_Item);
        } else {
            ext.ReqParams = new EffectParams();
        }
    }

    public static void e_Animation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("You play god. (неудачная попытка)");
        AuxUtils.exStub("rs_YourPowerFails (непонятно)");

        if (invokeMode == InvokeMode.im_Use) {
            creature.setSkill(SkillID.Sk_Animation, 100);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveGetAnAnimationSkill));
        } else if (invokeMode == InvokeMode.im_ItSelf) {
            Item item = (Item) ext.getParam(EffectParams.ep_Item);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_LifeForceFlows));
            if (item.CLSID == GlobalVars.iid_DeadBody || item.CLSID == GlobalVars.iid_Mummy) {
                NWCreature cr = EffectsFactory.animateDeadBody(creature, item);
                cr.Alignment = creature.Alignment;
                String dummy = cr.getDeclinableName(Number.nSingle, Case.cAccusative);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveAnimated) + dummy + ".");
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NecessaryToPointACorpse));
            }
        }
    }

    public static void e_Annihilation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("Tons of rock fall on your head. (dir=up, -~2300hp!, +tile rubble)");
        AuxUtils.exStub("Crushed beneath tons of rock");
        AuxUtils.exStub("The ray shoots off into the sky. (dir=up, land=non caves)");
        AuxUtils.exStub("You dig a giant hole. (dir=down)");
        AuxUtils.exStub("        grynr halls -> space");
        AuxUtils.exStub("        midgard -> niflheim");
        AuxUtils.exStub("The ");
        AuxUtils.exStub(" plummets into space.");
        AuxUtils.exStub("You dig a bottomless pit below you.");
        AuxUtils.exStub("Still plummeting!");

        NWField fld = creature.getCurrentField();
        if (fld.LandID == GlobalVars.Land_MimerRealm || fld.LandID == GlobalVars.Land_Nidavellir || fld.LandID == GlobalVars.Land_Jotenheim || fld.LandID == GlobalVars.Land_Valhalla || fld.LandID == GlobalVars.Land_GodsFortress || fld.LandID == GlobalVars.Land_Muspelheim || fld.LandID == GlobalVars.Land_Wasteland || fld.LandID == GlobalVars.Land_Bazaar || fld.LandID == GlobalVars.Land_Crypt || fld.LandID == GlobalVars.Land_Vanaheim || fld.LandID == GlobalVars.Land_Armory || fld.LandID == GlobalVars.Land_Ocean || fld.LandID == GlobalVars.Land_Niflheim || fld.LandID == GlobalVars.Land_Temple || fld.LandID == GlobalVars.Land_MimerWell || fld.LandID == GlobalVars.Land_Crossroads || fld.LandID == GlobalVars.Land_Bifrost || fld.LandID == GlobalVars.Land_SlaeterSea || fld.LandID == GlobalVars.Land_Alfheim || fld.LandID == GlobalVars.Land_GiollRiver) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_AnnihilationRayDoesntWorkHere));
        } else {
            AuxUtils.exStub("todo: 7 этапов через каждые 7 тайлов, на 2 в ширину увеличивается.");
            AnnihilationRay ray = new AnnihilationRay();
            try {
                ray.Exec(creature, EffectID.eid_Annihilation, invokeMode, ext, Locale.getStr(RS.rs_AnnihilationRaySwepsAwayEverything));
            } finally {
                fld.normalize();
            }
            AuxUtils.exStub("todo: orig-msg: Blinding light explodes out of the wand.");
        }
    }

    public static void e_Armoring(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        EffectsFactory.e_Scaling(EffectID.eid_Scaling, (NWCreature) ext.getParam(EffectParams.ep_Creature), source, state, InvokeMode.im_ItSelf, ext);
    }

    public static void e_ArrowMake(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub(" Cut arrows in which direction?");
        AuxUtils.exStub(" You can only create arrows from trees.");
        AuxUtils.exStub(" You create some arrows.");
        AuxUtils.exStub(" You become distracted and ruin your handiwork.");

        Point pt = (Point) ext.getParam(EffectParams.ep_Place);

        if (!creature.isNear(pt)) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(EffectTarget.et_PlaceNear.InvalidRS));
        } else {
            NWTile tile = creature.getCurrentField().getTile(pt.X, pt.Y);

            if (tile.getForeBase() != PlaceID.pid_Tree) {
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_HereNoTrees));
            } else {
                int aID;
                if (AuxUtils.chance(50)) {
                    aID = GlobalVars.iid_Arrow;
                } else {
                    aID = GlobalVars.iid_Bolt;
                }
                Item.genItem(creature, aID, AuxUtils.getBoundedRnd(3, 17), true);
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_ArrowsCreated));
            }
        }
    }

    public static void e_ArrowTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveWoundedByTheArrow));
        creature.applyDamage(AuxUtils.getBoundedRnd(5, 10), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_KilledByArrowTrap));
    }

    public static void e_Ashes(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode == InvokeMode.im_UseBegin) {
            creature.initEffect(effectID, source, EffectAction.ea_Persistent);
        } else if (invokeMode == InvokeMode.im_UseEnd) {
            creature.doneEffect(effectID, source);
        }
    }

    public static void e_BlackGemUse(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: messages");
        BlackGemRay ray = new BlackGemRay();
        ray.Exec(creature, EffectID.eid_BlackGemUse, invokeMode, ext, "");
    }

    private static void IntBlessing(NWCreature creature, Item extItem)
    {
        ItemKind kind = extItem.getKind();
        String sign = extItem.getEntry().Sign;

        if ((bless_ik.contains(kind))) {
            if (sign.equals("Potion_DrinkingWater")) {
                extItem.setCLSID(GlobalVars.nwrBase.findEntryBySign("Potion_HolyWater").GUID);
            } else {
                if (sign.compareTo("Amulet_Holding") != 0) {
                    switch (extItem.State) {
                        case is_Normal:
                            extItem.State = ItemState.is_Blessed;
                            break;
                        case is_Cursed:
                            extItem.State = ItemState.is_Normal;
                            break;
                    }
                }
            }

            if (((bless_ikArmor.contains(kind))) || (kind == ItemKind.ik_Ring && (sign.equals("Ring_Agility") || sign.equals("Ring_Protection")))) {
                // TODO: Повышение характеристик
            }
        }
    }

    public static void e_Blessing(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.isConfused()) {
            int num = creature.getItems().getCount();
            for (int idx = 0; idx < num; idx++) {
                Item item = creature.getItems().getItem(idx);
                AuxUtils.exStub("todo: //if (aExtItem = aItem) then Continue; вставить везде");
                if (AuxUtils.chance(60) && state != ItemState.is_Cursed) {
                    EffectsFactory.IntBlessing(creature, item);
                }
            }
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveBlessedAllItemsInInventory));
        } else {
            Item item = (Item) ext.getParam(EffectParams.ep_Item);
            if (state != ItemState.is_Cursed) {
                GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_YouveBlessed, new Object[]{item.getName()}));
                EffectsFactory.IntBlessing(creature, item);
            }
        }
    }

    public static void e_Blindness(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        creature.addEffect(EffectID.eid_Blindness, state, EffectAction.ea_Persistent, true, Locale.getStr(RS.rs_YouAreBlinding));
    }

    public static void e_BrainScarring(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Burns(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Cancellation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub(" The ... ray hits the <monster>.");
        CancellationRay ray = new CancellationRay();
        try {
            ray.Exec(creature, EffectID.eid_Cancellation, invokeMode, ext, EffectsFactory.WandRayMsg(effectID, source));
        } finally {
        }
    }

    public static void e_Cartography(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (creature.isPlayer()) {
            NWField field = creature.getCurrentField();

            int num = field.getHeight();
            for (int y = 0; y < num; y++) {
                int num2 = field.getWidth();
                for (int x = 0; x < num2; x++) {
                    NWTile tile = field.getTile(x, y);

                    switch (state) {
                        case is_Normal:
                            tile.includeState(TileStates.TS_VISITED);
                            break;
                        case is_Blessed:
                            tile.includeState(TileStates.TS_VISITED);
                            if (field.isTrap(x, y)) {
                                tile.Trap_Discovered = true;
                            }
                            break;
                        case is_Cursed:
                            tile.excludeState(TileStates.TS_VISITED);
                            break;
                    }
                }
            }

            if (state == ItemState.is_Cursed) {
                field.Visited = false;
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_SurroundingsSeemUnfamiliar));
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouSeeALandscape));
            }
        }
    }

    public static void e_ChangeAbility(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        Object obj = ext.getParam(EffectParams.ep_ItemExt);
        AbilityID ab = (AbilityID) obj;

        if (ab != AbilityID.Ab_None) {
            if (invokeMode != InvokeMode.im_Use && invokeMode != InvokeMode.im_UseBegin) {
                if (invokeMode == InvokeMode.im_UseEnd) {
                    creature.setAbility(ab, creature.getAbility(ab) - 1);
                    return;
                }
                if (invokeMode != InvokeMode.im_ItSelf) {
                    return;
                }
            }
            creature.setAbility(ab, creature.getAbility(ab) + 1);
        }
    }

    public static void e_ChangeSkill(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        Object obj = ext.getParam(EffectParams.ep_ItemExt);
        SkillID sk = (SkillID) obj;

        if (sk != SkillID.Sk_None) {
            if (invokeMode != InvokeMode.im_Use && invokeMode != InvokeMode.im_UseBegin) {
                if (invokeMode == InvokeMode.im_UseEnd) {
                    creature.setSkill(sk, creature.getSkill(sk) - 1);
                    return;
                }
                if (invokeMode != InvokeMode.im_ItSelf) {
                    return;
                }
            }
            creature.setSkill(sk, creature.getSkill(sk) + 1);
        }
    }

    private static void changeChLakeTile(IMap map, int x, int y, Object extData, RefObject<Boolean> refContinue)
    {
        BaseTile tile = map.getTile(x, y);
        if (tile != null) {
            tile.Background = (short) PlaceID.pid_Water;
        }
    }

    public static void e_Chaos(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();

        AuxUtils.exStub("You have lost your appetite.");
        AuxUtils.exStub("You change appearance.");

        int idx = AuxUtils.getBoundedRnd(1, 5);
        switch (idx) {
            case 1: {
                // none original messages
                while (creature.getItems().getCount() > 0) {
                    Item aExtItem = creature.getItems().getItem(0);
                    creature.getItems().extract(aExtItem);
                    aExtItem.setInUse(false);

                    Point pt = fld.searchFreeLocation();
                    if (pt == null) {
                        pt = creature.getLocation();
                    }

                    aExtItem.setPos(pt.X, pt.Y);
                    fld.getItems().add(aExtItem, false);
                }
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_AllYourItemsAreScatteredOnLevel));
                break;
            }
            case 2: {
                // none original messages
                Rect area = new Rect(creature.getPosX() - 5, creature.getPosY() - 5, creature.getPosX() + 5, creature.getPosY() + 5);
                fld.gen_Lake(area, EffectsFactory::changeChLakeTile);
                fld.normalize();
                break;
            }
            case 3: {
                // none original messages
                UniverseBuilder.gen_Creatures(fld, AuxUtils.getBoundedRnd(3, 19));
                break;
            }
            case 4: {
                // none original messages
                UniverseBuilder.gen_Items(fld, AuxUtils.getBoundedRnd(21, 23));
                break;
            }
            case 5: {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveGetAPersecutionMania));
                break;
            }
        }
    }

    public static void e_Confusion(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo:  messages");
        AuxUtils.exStub("todo: You are no longer confused.");
        AuxUtils.exStub("duration: ~ 26-47 turns;");
        AuxUtils.exStub("  if >50 then feel more intox;");
        AuxUtils.exStub("  pass out -> ~10 turns -> can move again;");
        creature.addEffect(EffectID.eid_Confusion, state, EffectAction.ea_Persistent, true, "");
        Effect ef = creature.getEffects().findEffectByID(EffectID.eid_Confusion);
        if (ef != null && ef.Duration > 75 && creature.hasAffect(EffectID.eid_Intoxicate)) {
            creature.addEffect(EffectID.eid_Intoxicate, state, EffectAction.ea_EachTurn, true, "");
        }
    }

    public static void e_Constitution(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        switch (state) {
            case is_Normal:
                creature.Constitution++;
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsPowerAndHealthy));
                break;
            case is_Blessed:
                creature.Constitution += 2;
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelTougher));
                break;
            case is_Cursed:
                creature.Constitution--;
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsPowerlessAndHealthless));
                break;
        }
    }

    public static void e_Contamination(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode == InvokeMode.im_ItSelf) {
                Effect e = creature.getEffects().findEffectByID(EffectID.eid_Contamination);
                if (e.Source == null) {
                    if (e.Duration == 1) {
                        creature.death(Locale.getStr(RS.rs_Degenerated), null);
                    } else {
                        if (e.Duration == 5) {
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouAreDying));
                        } else {
                            if (e.Duration == 10) {
                                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourSkinIsDecomposes));
                            } else {
                                if (e.Duration == 25) {
                                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourHairsAreFallsOut));
                                } else {
                                    if (e.Duration == 50) {
                                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelVerySick));
                                    }
                                }
                            }
                        }
                    }

                    if (e.Duration <= 50) {
                        int dg = AuxUtils.getBoundedRnd(1, 9);
                        creature.applyDamage(dg, DamageKind.dkPhysical, null, "");
                    }
                }
            }
        } else {
            if (creature.hasAffect(EffectID.eid_Contamination)) {
                creature.addEffect(EffectID.eid_Contamination, state, EffectAction.ea_EachTurn, true, Locale.getStr(RS.rs_YourBodyIsLighted));
            }
        }
    }

    public static void e_CrushRoof(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        DungeonRoom room = (DungeonRoom) ext.getParam(EffectParams.ep_DunRoom);
        if (room != null) {
            GlobalVars.nwrWin.showText(null, Locale.getStr(RS.rs_CeilingCracks));
            EffectsFactory.CrushRoof(room, true);
        }
    }

    public static void e_CrushRoofTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        DungeonRoom dunRoom = creature.findDungeonRoom();
        if (dunRoom != null) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_CeilingCracks));
            EffectsFactory.CrushRoof(dunRoom, false);
        }
    }

    public static void e_CrystalSkin(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Cube(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (AuxUtils.chance(15) && creature.isPlayer()) {
            AuxUtils.exStub("todo: messages");
            AuxUtils.exStub("todo: отсчет времени");

            Item item = (Item) source;
            if (item.getEntry().Sign.compareTo("BlueCube") == 0) {
                int val = AuxUtils.getBoundedRnd(1, 100);
                if (creature.Turn < val) {
                    val = creature.Turn;
                }
                creature.Turn -= val;
            } else {
                if (item.getEntry().Sign.compareTo("GreyCube") == 0) {
                    int val = AuxUtils.getBoundedRnd(1, 500);
                    creature.Turn += val;
                    AuxUtils.exStub("!!!");
                } else {
                    if (item.getEntry().Sign.compareTo("OrangeCube") == 0) {
                        creature.transferTo(GlobalVars.Layer_Wasteland, 0, 0, -1, -1, StaticData.MapArea, true, true);
                    }
                }
            }
        }
    }

    public static void createMapObject(EffectID effectID, NWField fld, int posX, int posY, boolean needUpdate)
    {
        MapObject mapObj = new MapObject(GameSpace.getInstance(), fld);
        mapObj.initByEffect(effectID);
        mapObj.setPos(posX, posY);
        mapObj.IsNeedUpdate = needUpdate;
        fld.getFeatures().add(mapObj);
    }

    private static void CureEffect(NWCreature creature, EffectID effectID, String msg)
    {
        Effect ef = creature.getEffects().findEffectByID(effectID);
        if (ef != null) {
            creature.getEffects().remove(ef);
            GlobalVars.nwrWin.showText(creature, msg);
        }
    }

    public static void e_Cure(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();

        createMapObject(effectID, fld, creature.getPosX(), creature.getPosY(), true);

        if (state != ItemState.is_Cursed) {
            EffectsFactory.CureEffect(creature, EffectID.eid_Blindness, Locale.getStr(RS.rs_BlindnessIsCured));
            EffectsFactory.CureEffect(creature, EffectID.eid_Fever, Locale.getStr(RS.rs_FeverIsCured));
            EffectsFactory.CureEffect(creature, EffectID.eid_Withered, Locale.getStr(RS.rs_WitheredIsCured));
            EffectsFactory.CureEffect(creature, EffectID.eid_Confusion, Locale.getStr(RS.rs_ConfusionIsCured));
            EffectsFactory.CureEffect(creature, EffectID.eid_Hallucination, Locale.getStr(RS.rs_HallucinationIsCured));
            EffectsFactory.CureEffect(creature, EffectID.eid_HealInability, Locale.getStr(RS.rs_HealInabilityIsCured));
            EffectsFactory.CureEffect(creature, EffectID.eid_Diseased, Locale.getStr(RS.rs_YouCured));
        }

        switch (state) {
            case is_Normal:
                if (creature.HPCur == creature.HPMax) {
                    creature.setHPMax(creature.HPMax + 1);
                    creature.HPCur = creature.HPMax;
                } else {
                    creature.HPCur += AuxUtils.getBoundedRnd(1, creature.HPMax - creature.HPCur);
                }
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsBetter));
                break;

            case is_Blessed:
                if (creature.HPCur == creature.HPMax) {
                    creature.setHPMax(creature.HPMax + AuxUtils.getBoundedRnd(4, 7));
                    creature.HPCur = creature.HPMax;
                } else {
                    creature.HPCur = creature.HPMax;
                }
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsMoreBetter));
                EffectsFactory.CureEffect(creature, EffectID.eid_Contamination, Locale.getStr(RS.rs_ContaminationIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Burns, Locale.getStr(RS.rs_BurnsIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_LegsMissing, Locale.getStr(RS.rs_LegsMissingIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Intoxicate, Locale.getStr(RS.rs_IntoxicateIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_EyesMissing, Locale.getStr(RS.rs_EyesMissingIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Deafness, Locale.getStr(RS.rs_DeafnessIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Vertigo, Locale.getStr(RS.rs_VertigoIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Lycanthropy, Locale.getStr(RS.rs_LycanthropyIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_BrainScarring, Locale.getStr(RS.rs_BrainScarringIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Impregnation, Locale.getStr(RS.rs_ImpregnationIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Poisoned, Locale.getStr(RS.rs_VenomIsNeutralized));
                break;

            case is_Cursed:
                creature.applyDamage(AuxUtils.getBoundedRnd(3, 7), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_YouFeelsWorse));
                break;
        }
    }

    public static void e_Deafness(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Deanimation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub(" The ... ray hits the <monster>.");
        DeanimationRay ray = new DeanimationRay();
        try {
            ray.Exec(creature, EffectID.eid_Deanimation, invokeMode, ext, EffectsFactory.WandRayMsg(effectID, source));
        } finally {
            creature.getCurrentField().normalize();
        }
    }

    public static void e_Death(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub(" The ... ray hits the <monster>.");
        DeathRay ray = new DeathRay();
        try {
            ray.Exec(creature, EffectID.eid_Death, invokeMode, ext, EffectsFactory.WandRayMsg(EffectID.eid_Death, source));
        } finally {
        }
    }

    public static void e_Destruction(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        ItemsList list = creature.getItems();

        if (creature.isConfused()) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ScrollWasDestroyAllItems));
            for (int i = list.getCount() - 1; i >= 0; i--) {
                if (AuxUtils.chance(45)) {
                    Item item = list.getItem(i);
                    item.setInUse(false);
                    list.remove(item);
                }
            }
        } else {
            Item item = (Item) ext.getParam(EffectParams.ep_Item);
            item.setInUse(false);
            GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_ScrollWasDestroy, new Object[]{item.getName()}));
            list.remove(item);
        }
    }

    public static void e_DetectItems(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (invokeMode == InvokeMode.im_Use || invokeMode == InvokeMode.im_ItSelf) {
            AuxUtils.exStub("@deprecated: You sense nothing of value in this area.");
            creature.addEffect(EffectID.eid_DetectItems, state, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_ThiefEyeWillShowYouAllItems));
        }
    }

    public static void e_CheckDiagnosis(EffectID effectID, NWCreature creature, Object source, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode == InvokeMode.im_ItSelf) {
            ext.ReqParams = new EffectParams(EffectParams.ep_Creature);
        } else {
            ext.ReqParams = new EffectParams();
        }
    }

    public static void e_Diagnosis(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode == InvokeMode.im_Use) {
            creature.setSkill(SkillID.Sk_Diagnosis, creature.getSkill(SkillID.Sk_Diagnosis) + 1);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveGetADiagnosisSkill));
        } else if (invokeMode == InvokeMode.im_ItSelf) {
            GlobalVars.nwrGame.diagnoseCreature((NWCreature) ext.getParam(EffectParams.ep_Creature));
        }
    }

    private static boolean DialogCheck(NWCreature collocutor)
    {
        boolean result = false;
        if (!(collocutor.getBrain() instanceof SentientBrain)) {
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_ThisNotCollocutor));
        } else {
            result = true;
        }
        return result;
    }

    public static void e_Dialog(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWCreature col = null;
        ExtList<NWCreature> cr_list = new ExtList<>();
        NWField fld = creature.getCurrentField();

        int num = fld.getCreatures().getCount();
        for (int i = 0; i < num; i++) {
            NWCreature creat = fld.getCreatures().getItem(i);
            if (!creat.equals(creature)) {
                int dist = AuxUtils.distance(creature.getLocation(), creat.getLocation());
                if (dist == 1) {
                    cr_list.add(creat);
                }
            }
        }

        if (cr_list.getCount() == 0) {
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_HereNobody));
        } else {
            if (cr_list.getCount() == 1) {
                col = ((NWCreature) cr_list.get(0));
                if (!EffectsFactory.DialogCheck(col)) {
                    col = null;
                }
            } else {
                Point pt = (Point) ext.getParam(EffectParams.ep_Place);

                if (!creature.isNear(pt)) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(EffectTarget.et_PlaceNear.InvalidRS));
                    return;
                }

                col = ((NWCreature) creature.getCurrentMap().findCreature(pt.X, pt.Y));
                if (col == null) {
                    GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_HereNobody));
                } else {
                    if (!EffectsFactory.DialogCheck(col)) {
                        col = null;
                    }
                }
            }
        }

        cr_list.dispose();

        if (col != null) {
            GlobalVars.nwrWin.showNPCDialog(col);
        }
    }

    public static void e_Diary(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        AuxUtils.exStub("todo: messages   rs_YouAreMesmerized ? ");
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFindInterestingEntry));
        String s = Locale.getStr(AuxUtils.getBoundedRnd(RS.rs_Diary_First, RS.rs_Diary_Last));
        GlobalVars.nwrWin.showText(creature, s, new LogFeatures(LogFeatures.lfDialog));
    }

    public static void e_Dig(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.getItems().findByCLSID(GlobalVars.iid_PickAxe) == null) {
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_YouHaventPickaxe));
        } else {
            Point pt = (Point) ext.getParam(EffectParams.ep_Place);

            if (!creature.isNear(pt)) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(EffectTarget.et_PlaceNear.InvalidRS));
            } else {
                creature.getCurrentMap().getTile(pt.X, pt.Y).setFore(PlaceID.pid_PitTrap);
            }
        }
    }

    public static void e_Diseased(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ");

        if (invokeMode == InvokeMode.im_Use) {
            if (creature.hasAffect(EffectID.eid_Diseased)) {
                creature.addEffect(EffectID.eid_Diseased, state, EffectAction.ea_EachTurn, true, "");
            }
        } else if (invokeMode == InvokeMode.im_ItSelf) {
            Effect e = creature.getEffects().findEffectByID(EffectID.eid_Diseased);
            if (e.Source == null) {
                if (e.Duration == 1) {
                    creature.death(Locale.getStr(RS.rs_KilledByRottingDisease), null);
                } else {
                    if (AuxUtils.chance(30)) {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourSkinCrawlsWithDisease));
                        creature.applyDamage(AuxUtils.getBoundedRnd(1, 7), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_KilledByRottingDisease));
                    }
                }
            }
        }
    }

    public static void e_DispelHex(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        boolean confused = creature.isConfused();

        int num = creature.getItems().getCount();
        for (int idx = 0; idx < num; idx++) {
            Item item = creature.getItems().getItem(idx);
            if (confused) {
                item.State = ItemState.is_Cursed;
            } else {
                item.State = ItemState.is_Normal;
            }
        }

        if (confused) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ScrollWasCurseAllItems));
        } else {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ScrollWasDispelHexFromAllItems));
        }
    }

    public static void e_Displacement(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        ((NWCreature) ext.getParam(EffectParams.ep_Creature)).moveRnd();
    }

    public static void e_DoorTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        int id = (creature.getCurrentField()).getLayer().EntryID;
        if (id == GlobalVars.Layer_Midgard) {
            id = GlobalVars.Layer_Svartalfheim1;
        } else {
            if (id == GlobalVars.Layer_Svartalfheim1) {
                id = GlobalVars.Layer_Svartalfheim2;
            } else {
                if (id == GlobalVars.Layer_Svartalfheim2) {
                    id = GlobalVars.Layer_Svartalfheim3;
                } else {
                    if (id != GlobalVars.Layer_Svartalfheim3) {
                        return;
                    }
                    id = GlobalVars.Layer_GrynrHalls;
                }
            }
        }
        creature.transferTo(id, -1, -1, -1, -1, StaticData.MapArea, true, false);
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFallThroughTrapDoor));
    }

    public static void e_Draining(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_UseBegin:
                creature.initEffect(effectID, source, EffectAction.ea_EachTurn);
                break;
            case im_UseEnd:
                creature.doneEffect(effectID, source);
                break;
            case im_ItSelf:
                creature.applyDamage(AuxUtils.getBoundedRnd(1, 3), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_DrainedByRing));
                break;
        }
    }

    public static void e_DrawLife(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_TouchNowDrawsEnergy));
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_SpellAlreadyEmpowers));
    }

    public static void e_Embalming(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("pulpy mummy");
        AuxUtils.exStub("charred mummy");
        AuxUtils.exStub("You cannot embalm ");

        Item aExtItem = (Item) ext.getParam(EffectParams.ep_Item);
        
        if (aExtItem.CLSID != GlobalVars.iid_DeadBody) {
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_ThisNotDeadbody));
        } else {
            NWCreature mon = (NWCreature) aExtItem.getContents().getItem(0);
            aExtItem.setCLSID(GlobalVars.iid_Mummy);
            aExtItem.setWeight((((float) mon.getWeight() * 0.1f)));
            String cs = mon.getDeclinableName(Number.nSingle, Case.cGenitive);
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.format(RS.rs_MummyCreated, new Object[]{cs}));
        }
    }

    private static boolean EnchantResult(NWCreature creature, Item extItem, boolean value)
    {
        int temp;
        if (value) {
            temp = RS.rs_ItemGlows;
        } else {
            temp = RS.rs_NewfoundBeauty;
        }
        GlobalVars.nwrWin.showText(creature, Locale.format(temp, new Object[]{extItem.getName()}));
        return value;
    }

    public static void e_Enchantment(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo:  + rs_ScrollHasNoApparentEffect");
        boolean res = false;

        Item extItem = (Item) ext.getParam(EffectParams.ep_Item);
        extItem.setIdentified(true);
        String sign = extItem.getEntry().Sign;

        switch (extItem.getKind()) {
            case ik_Armor:
            case ik_Ring:
            case ik_BluntWeapon:
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
            case ik_Projectile: {
                res = EffectsFactory.EnchantResult(creature, extItem, true);

                if (extItem.State == ItemState.is_Cursed) {
                    extItem.State = ItemState.is_Normal;
                }

                if (extItem.getKind() != ItemKind.ik_Ring || (extItem.getKind() == ItemKind.ik_Ring && (sign.equals("Ring_Agility") || sign.equals("Ring_Protection")))) {
                    switch (state) {
                        case is_Normal:
                            extItem.Bonus += AuxUtils.getBoundedRnd(5, 15);
                            break;
                        case is_Blessed:
                            extItem.Bonus += AuxUtils.getBoundedRnd(10, 15);
                            break;
                        case is_Cursed:
                            extItem.Bonus += AuxUtils.getBoundedRnd(1, 5);
                            break;
                    }
                }
                break;
            }

            case ik_Potion:
            case ik_Scroll: {
                res = EffectsFactory.EnchantResult(creature, extItem, true);
                extItem.State = ItemState.is_Blessed;
                break;
            }

            case ik_Wand: {
                res = EffectsFactory.EnchantResult(creature, extItem, true);
                if (sign.equals("Wand_Wishing")) {
                    extItem.Bonus++;
                } else {
                    switch (state) {
                        case is_Normal:
                            extItem.Bonus += AuxUtils.getBoundedRnd(5, 15);
                            break;
                        case is_Blessed:
                            extItem.Bonus += AuxUtils.getBoundedRnd(10, 15);
                            break;
                        case is_Cursed:
                            extItem.Bonus += AuxUtils.getBoundedRnd(1, 5);
                            break;
                    }
                }
                break;
            }

            case ik_Amulet: {
                if (sign.compareTo("Amulet_Holding") != 0 && extItem.State == ItemState.is_Cursed) {
                    res = EffectsFactory.EnchantResult(creature, extItem, true);
                    extItem.State = ItemState.is_Normal;
                }
                break;
            }

            default:
                if (sign.equals("Anvil")) {
                    res = EffectsFactory.EnchantResult(creature, extItem, true);
                    extItem.setCLSID(GlobalVars.nwrBase.findEntryBySign("PlatinumAnvil").GUID);
                }
                if (sign.equals("GreyCube") || sign.equals("BlueCube")) {
                    res = EffectsFactory.EnchantResult(creature, extItem, true);
                    extItem.setCLSID(GlobalVars.nwrBase.findEntryBySign("OrangeCube").GUID);
                }
                if (sign.equals("Ocarina")) {
                    res = EffectsFactory.EnchantResult(creature, extItem, true);
                    extItem.setCLSID(GlobalVars.nwrBase.findEntryBySign("GlassOcarina").GUID);
                }
                break;
        }

        if (!res) {
            EffectsFactory.EnchantResult(creature, extItem, res);
        } else {
            // TODO: вывести сообщение о новых свойствах
        }
    }

    public static void e_Endurance(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        int cnt = 0;
        switch (state) {
            case is_Normal:
                cnt = AuxUtils.getBoundedRnd(3, 10);
                break;
            case is_Blessed:
                cnt = AuxUtils.getBoundedRnd(8, 20);
                break;
            case is_Cursed:
                cnt = -AuxUtils.getBoundedRnd(1, 10);
                break;
        }

        if (creature.isPlayer()) {
            if (cnt >= 0) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsYourselfMoreRobust));
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsDeadlyWeakness));
            }
        }

        creature.HPCur = creature.HPMax + cnt;
        if (creature.HPCur <= 0) {
            creature.death(Locale.getStr(RS.rs_Shriveled), null);
        }
    }

    public static void e_Entomb(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Eternal_Life(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Evocation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        Point pt = creature.getNearestPlace(2, true);
        if (pt != null) {
            UniverseBuilder.gen_Creature(creature.getCurrentField(), -1, pt.X, pt.Y, true);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_MonsterEvocated));
        }
    }

    public static void e_Experience(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        switch (state) {
            case is_Normal:
                GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_YouFeelsYouselfExperienced, new Object[]{""}));
                creature.setExperience(creature.getExperience() + 1);
                break;

            case is_Blessed:
                GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_YouFeelsYouselfExperienced, new Object[]{Locale.getStr(RS.rs_Much)}));
                creature.setExperience(creature.getExperience() + 2);
                break;

            case is_Cursed:
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsYouselfUnexperienced));
                creature.setExperience(creature.getExperience() - 1);
                if (creature.getExperience() < 0) {
                    creature.death(Locale.getStr(RS.rs_SuckedDry) + ((Item) source).getDeclinableName(Number.nSingle, Case.cInstrumental, creature.isBlindness()), null);
                }
                break;
        }
    }

    public static void e_Extinction(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: проверить реализацию");
        AuxUtils.exStub("You genocide idiots.");

        if (creature.isConfused()) {
            int id = GlobalVars.nwrGame.getRndExtincted();
            if (id >= 0) {
                GlobalVars.nwrGame.setVolatileState(id, VolatileState.vesNone);
                CreatureEntry cEntry = (CreatureEntry) GlobalVars.nwrBase.getEntry(id);
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_YouAccidentallyCreateRace));
            }
        } else {
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_YouHavePowerOfGenocide));
            GlobalVars.nwrWin.hideInventory();
            GlobalVars.nwrWin.showInput(Locale.getStr(RS.rs_RaceToDestroy), EffectsFactory::ExtinctionAcceptProc);
        }
    }

    public static void e_EyesMissing(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Fading(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ");
    }

    public static void e_Famine(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_UseBegin:
                creature.initEffect(effectID, source, EffectAction.ea_EachTurn);
                break;
            case im_UseEnd:
                creature.doneEffect(effectID, source);
                break;
            case im_ItSelf:
                if (creature.isPlayer()) {
                    ((Player) creature).setSatiety((short) (((Player) creature).getSatiety() - 10));
                }
                break;
        }
    }

    public static void e_Fennling(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        Item wand = (Item) ext.getParam(EffectParams.ep_Item);
        if (wand.getKind() != ItemKind.ik_Wand) {
            AuxUtils.exStub("//nwrWin.ShowText(creature, rsList(]);");
            return;
        }

        Item wand2 = null;
        int num = creature.getItems().getCount();
        for (int i = 0; i < num; i++) {
            Item it = creature.getItems().getItem(i);

            if (it.getKind() == ItemKind.ik_Wand && !it.equals(wand)) {
                wand2 = it;
                break;
            }
        }

        if (wand2 == null) {
            AuxUtils.exStub("//nwrWin.ShowText(creature, rsList(Нет аналогичной палочки]);");
        } else {
            wand.Bonus += wand2.Bonus;
            creature.getItems().remove(wand2);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_TransferIsSuccesful));
            AuxUtils.exStub(">> They must be similar wands.");
        }
    }

    public static void e_Fever(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("//creature.Death(rsList(rs_Poisoned]);");

        switch (invokeMode) {
            case im_Use:
                if (creature.hasAffect(EffectID.eid_Fever)) {
                    creature.addEffect(EffectID.eid_Fever, state, EffectAction.ea_EachTurn, true, Locale.getStr(RS.rs_YouveGetAFever));
                }
                break;

            case im_ItSelf:
                Effect e = creature.getEffects().findEffectByID(EffectID.eid_Fever);
                if (e.Source == null) {
                    if (e.Duration == 1) {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouSuccumbToYourIllness));
                        creature.death(Locale.getStr(RS.rs_FelledByDisease), null);
                    } else {
                        if (AuxUtils.chance(30)) {
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourFeverWeaksYou));
                            creature.applyDamage(AuxUtils.getBoundedRnd(1, 7), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_FelledByDisease));
                        }
                    }
                }
                break;
        }
    }

    public static void e_FillVial(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        boolean res = false;
        Item item = (Item) source;

        AbstractMap fld = creature.getCurrentMap();
        Point pt = (Point) ext.getParam(EffectParams.ep_Place);

        if (!creature.isNear(pt)) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(EffectTarget.et_PlaceNear.InvalidRS));
        } else {
            NWTile tile = (NWTile) fld.getTile(pt.X, pt.Y);

            if (tile != null) {
                if (tile.getBackBase() == PlaceID.pid_Liquid && tile.Lake_LiquidID != 0) {
                    item.setCLSID(tile.Lake_LiquidID);
                    res = true;
                } else {
                    if (tile.getForeBase() == PlaceID.pid_Well) {
                        item.setCLSID(GlobalVars.nwrBase.findEntryBySign("Potion_DrinkingWater").GUID);
                        res = true;
                    }
                }
            }

            if (!res) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_WaterSourceNotFound));
            }
        }
    }

    public static void e_Fire(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        FireRay ray = new FireRay();
        ray.Exec(creature, EffectID.eid_Fire, invokeMode, ext, EffectsFactory.WandRayMsg(EffectID.eid_Fire, source));
    }

    // FIXME: unused!!!
    public static void e_FireStorm(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AbstractMap fld = creature.getCurrentMap();
        Point pt = (Point) ext.getParam(EffectParams.ep_Place);
        NWCreature cr = (NWCreature) fld.findCreature(pt.X, pt.Y);

        MapObject mapObj = new MapObject(GameSpace.getInstance(), fld);
        mapObj.initByEffect(effectID);
        fld.getFeatures().add(mapObj);

        int dur = Effect.getDuration(effectID, state, false);
        for (int i = 1; i <= dur; i++) {
            mapObj.nextSprite(pt.X, pt.Y);
            int val = AuxUtils.getBoundedRnd(35, 65);
            if (cr.hasAffect(EffectID.eid_FireStorm)) {
                cr.applyDamage(val, DamageKind.dkRadiation, null, "");
            }
            BaseSystem.sleep(100);
        }
        fld.getFeatures().remove(mapObj);
    }

    public static void e_FireTrap(EffectID effectID, NWCreature creature, Object source, ItemState itemState, InvokeMode invokeMode, EffectExt ext)
    {
        boolean ashes = creature.getEffects().findEffectByID(EffectID.eid_Ashes) != null;
        boolean resist = !creature.hasAffect(EffectID.eid_FireTrap);
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouEngulfedInFlames));

        if (!ashes && !resist) {
            if (creature.getEffects().findEffectByID(EffectID.eid_Burns) == null) {
                creature.addEffect(EffectID.eid_Burns, ItemState.is_Normal, EffectAction.ea_EachTurn, false, Locale.getStr(RS.rs_YouBurned));
                creature.applyDamage(AuxUtils.getBoundedRnd(25, 45), DamageKind.dkRadiation, null, Locale.getStr(RS.rs_KilledByFireTrap));
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouTooBadlyBurned));
                creature.death(Locale.getStr(RS.rs_KilledByFireTrap), null);
            }
        }

        if (!ashes) {
            boolean scrBurns = false;

            ItemsList list = creature.getItems();
            for (int i = list.getCount() - 1; i >= 0; i--) {
                Item it = list.getItem(i);
                if (it.getKind() == ItemKind.ik_Scroll && AuxUtils.chance(35)) {
                    list.remove(it);
                    scrBurns = true;
                }
            }

            if (scrBurns) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_SomeScrollsBurnup));
            }
        }
    }

    public static void e_FireVision(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: A crimson ray comes out of your eyes.");
        AuxUtils.exStub("todo: A crimson ray hits the ...");

        FireVisionRay ray = new FireVisionRay();
        ray.Exec(creature, EffectID.eid_FireVision, invokeMode, ext, Locale.getStr(RS.rs_FireVisionExec));
    }

    public static void e_Flame(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        boolean ashes = creature.getEffects().findEffectByID(EffectID.eid_Ashes) != null || creature.getAbility(AbilityID.Resist_Heat) > 0;
        int val = 0;
        if (state != ItemState.is_Normal) {
            if (state != ItemState.is_Blessed) {
                if (state == ItemState.is_Cursed) {
                    val = AuxUtils.getBoundedRnd(2, 7);
                }
            } else {
                val = AuxUtils.getBoundedRnd(2, 7);
            }
        } else {
            val = AuxUtils.getBoundedRnd(2, 15);
        }

        if (!ashes) {
            String s = "";
            if (state != ItemState.is_Normal) {
                if (state != ItemState.is_Blessed) {
                    if (state == ItemState.is_Cursed) {
                        s = Locale.getStr(RS.rs_FlameGrey);
                    }
                } else {
                    s = Locale.getStr(RS.rs_FlameWhite);
                }
            } else {
                s = Locale.getStr(RS.rs_FlameYellow);
            }

            GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_FlameComesFromScroll, new Object[]{s}));

            if (state != ItemState.is_Normal) {
                if (state != ItemState.is_Blessed) {
                    if (state == ItemState.is_Cursed) {
                        creature.setHPMax(creature.HPMax - val);
                    }
                } else {
                    creature.setHPMax(creature.HPMax + val);
                }
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveDamagedByFire));
                creature.applyDamage(val, DamageKind.dkRadiation, null, Locale.getStr(RS.rs_BurnedToDeath));
            }
        }

        if (creature.isConfused()) {
            Rect r = new Rect(creature.getPosX() - 1, creature.getPosY() - 1, creature.getPosX() + 1, creature.getPosY() + 1);
            ExtList<LocatedEntity> list = creature.getCurrentField().getCreatures().searchListByArea(r);

            int num = list.getCount();
            for (int i = 0; i < num; i++) {
                NWCreature cr = (NWCreature) list.get(i);
                if (cr.hasAffect(EffectID.eid_Flame)) {
                    cr.applyDamage(val, DamageKind.dkRadiation, null, "");
                }
            }
            list.dispose();
        }
    }

    public static void e_Flaying(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        FlayingRay ray = new FlayingRay();
        ray.Exec(creature, EffectID.eid_Flaying, invokeMode, ext, Locale.getStr(RS.rs_TheRayOfFlayingIsZapped));
    }

    public static void e_Flood(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        DungeonRoom room = (DungeonRoom) ext.getParam(EffectParams.ep_DunRoom);
        if (room != null) {
            AuxUtils.exStub("todo: заблокировать двери, смерть утопающих");

            Rect r = room.getArea().clone();
            r.inflate(-1, -1);

            AbstractMap map = room.getMap();
            while ((int) map.getTile(r.Left, r.Top).Background == PlaceID.pid_Water) {
                r.inflate(-1, -1);
            }

            for (int x = r.Left; x <= r.Right; x++) {
                for (int y = r.Top; y <= r.Bottom; y++) {
                    BaseTile tile = map.getTile(x, y);
                    if (r.isBorder(x, y)) {
                        tile.Background = (short) PlaceID.pid_Water;
                    }

                    NWCreature cr = (NWCreature) map.findCreature(x, y);
                    cr.drown();
                }
            }
        }
    }

    public static void e_FluteUse(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        Item flute = (Item) source;

        boolean good = flute.Bonus > 0;
        if (good) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_PlayQuietTune));
        } else {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_PlayRestlessTune));
        }

        if (creature.getAbility(AbilityID.Ab_MusicalAcuity) <= 0 && good) {
            flute.Bonus--;
        }

        Rect r = new Rect(creature.getPosX() - 3, creature.getPosY() - 3, creature.getPosX() + 3, creature.getPosY() + 3);
        ExtList<LocatedEntity> list = (creature.getCurrentField()).getCreatures().searchListByArea(r);

        int num = list.getCount();
        for (int i = 0; i < num; i++) {
            NWCreature cr = (NWCreature) list.get(i);
            if (!cr.equals(creature)) {
                RaceID race = cr.getEntry().Race;
                if (race == RaceID.crDefault || race == RaceID.crHuman) {
                    int lc = AlignmentEx.getLC(creature.Alignment);
                    if (good) {
                        cr.Alignment = AlignmentEx.genAlignment(lc, AlignmentEx.am_Mask_GENeutral);
                    } else {
                        cr.Alignment = AlignmentEx.genAlignment(lc, AlignmentEx.am_Mask_Evil);
                    }
                }
            }
        }
        list.dispose();
    }

    private static void CureStoning(NWCreature creature, Object source)
    {
        // check-ok
        Effect ef = creature.getEffects().findEffectByID(EffectID.eid_Stoning);
        if (ef != null) {
            creature.getEffects().remove(ef);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourGreyColorFades));
        } else {
            if (source != null) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_WhyDidYouEatThat));
            }
        }
    }

    public static void e_FoodEat(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: messages");
        Item item = (Item) source;
        ItemKind kind = item.getKind();

        if (kind != ItemKind.ik_DeadBody) {
            if (kind == ItemKind.ik_Food) {
                String foodSign = item.getEntry().Sign;

                if (foodSign.equals("BleachedRoot")) {
                    AuxUtils.exStub("msg-none");
                } else if (foodSign.equals("BlackMushroom")) {
                    // check-ok
                    creature.setSkill(SkillID.Sk_FireVision, 100);
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourEyesBleed));
                    creature.applyDamage(AuxUtils.getBoundedRnd(0, 20), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_KilledByMushroom));
                } else if (foodSign.equals("ClayLump")) {
                    // check-ok
                    EffectsFactory.CureStoning(creature, source);
                } else if (foodSign.equals("GnarledRoot")) {
                    AuxUtils.exStub("msg-none");
                } else if (foodSign.equals("GreenLump")) {
                    // check-ok
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_LumpTastedWoefullyBad));
                } else if (foodSign.equals("GreenMushroom")) {
                    // check-ok
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_TastedLikeDirt));
                } else if (foodSign.equals("MagicCookie")) {
                    AuxUtils.exStub("msg-none");
                } else if (foodSign.equals("MottledMushroom")) {
                    // check-ok
                    GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_WasDelicious, new Object[]{item.getName()}));
                    AuxUtils.exStub("todo: \"приятен на вкус\", но наносит 30-40 повреждений несколько ходов спустя");
                } else if (foodSign.equals("RedMushroom")) {
                    // check-ok
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouGoBlind));
                } else if (foodSign.equals("SpeckledGrowth")) {
                    AuxUtils.exStub("msg-partially");
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_FungusTastesStrange));
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ShapesMesmerizingFlow));
                    AuxUtils.exStub("todo: это сообщение вызывается почти каждый ход, пока глюки,используются какие попало предметы, все встречные твари получаютдикие имена и т.п.}");
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouDroolHappily));
                } else if (foodSign.equals("SpongyMass")) {
                    AuxUtils.exStub("msg-none");
                    creature.setAbility(AbilityID.Ab_SixthSense, 0);
                } else if (foodSign.equals("StrangeHerb")) {
                    AuxUtils.exStub("msg-none");
                } else if (foodSign.equals("Urn")) {
                    switch (AuxUtils.getRandom(7)) {
                        case 0: {
                            creature.Strength++;
                            if (creature.isPlayer()) {
                                ((Player) creature).setSatiety((short) (((Player) creature).getSatiety() + 10));
                            }
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThisUrnContains) + Locale.getStr(RS.rs_Urn_GreenPowder));
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsAPower));
                            break;
                        }
                        case 1: {
                            if (AuxUtils.chance(10)) {
                                creature.addEffect(EffectID.eid_ThirdSight, ItemState.is_Normal, EffectAction.ea_Instant, false, "");
                            }
                            if (creature.isPlayer()) {
                                ((Player) creature).setSatiety((short) (((Player) creature).getSatiety() + 10));
                            }
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThisUrnContains) + Locale.getStr(RS.rs_Urn_OrangePowder));
                            break;
                        }
                        case 2: {
                            if (creature.isPlayer()) {
                                ((Player) creature).setSatiety((short) (((Player) creature).getSatiety() + 0));
                            }
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThisUrnContains) + Locale.getStr(RS.rs_Urn_Nothing));
                            break;
                        }
                        case 3: {
                            if (creature.isPlayer()) {
                                ((Player) creature).setSatiety((short) (((Player) creature).getSatiety() + 200));
                            }
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThisUrnContains) + Locale.getStr(RS.rs_Urn_Meat));
                            break;
                        }
                        case 4: {
                            if (creature.isPlayer()) {
                                ((Player) creature).setSatiety((short) (((Player) creature).getSatiety() + 300));
                            }
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThisUrnContains) + Locale.getStr(RS.rs_Urn_Food));
                            break;
                        }
                        case 5: {
                            if (creature.isPlayer()) {
                                ((Player) creature).setSatiety((short) (((Player) creature).getSatiety() - 10));
                            }
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThisUrnContains) + Locale.getStr(RS.rs_Urn_RottenFood) + " " + Locale.getStr(RS.rs_YouGag));
                            break;
                        }
                        case 6: {
                            if (creature.isPlayer()) {
                                ((Player) creature).setSatiety((short) (((Player) creature).getSatiety() + 0));
                            }
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThisUrnContains) + Locale.getStr(RS.rs_Urn_SmallerUrn));
                            break;
                        }
                    }
                } else if (foodSign.equals("YellowMushroom")) {
                    // check-ok
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_Ulch));
                }
            }
        } else {
            NWCreature dead = (NWCreature) item.getContents().getItem(0);
            if (dead == null) {
                throw new RuntimeException("Assertion failure #1");
            }

            String deadSign = dead.getEntry().Sign;

            if (deadSign.equals("AirGhola")) {
                // check-ok
                if (AuxUtils.chance(30)) {
                    creature.Strength--;
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsAWeakness));
                }
            } else if (deadSign.equals("Anssk")) {
                // check-ok
                creature.setAbility(AbilityID.Ab_SixthSense, creature.getAbility(AbilityID.Ab_SixthSense) + 1);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_MinorAcheEmanates));
            } else if (deadSign.equals("Basilisk")) {
                // check-ok
                if (creature.getAbility(AbilityID.Resist_Petrification) <= 0) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouTurnToStone));
                    AuxUtils.exStub("Cockatrice?");
                    creature.death(Locale.getStr(RS.rs_KilledByGluttony), null);
                }
            } else if (deadSign.equals("Bloodslug")) {
                // check-ok
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_Ughh));
            } else if (deadSign.equals("Blur")) {
                // check-ok
                EffectsFactory.e_Speedup(EffectID.eid_Speedup, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
            } else if (deadSign.equals("Breleor")) {
                // check-ok
                creature.setSkill(SkillID.Sk_Cartography, creature.getSkill(SkillID.Sk_Cartography) + 1);
                creature.setSkill(SkillID.Sk_DimensionTravel, creature.getSkill(SkillID.Sk_DimensionTravel) + 1);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_WorldComesIntoFocus));
            } else if (deadSign.equals("BrownBat")) {
                // check-ok
                AuxUtils.exStub("msg-none");
                EffectsFactory.e_Confusion(EffectID.eid_Confusion, creature, source, state, invokeMode, null);
            } else if (deadSign.equals("Cockatrice")) {
                // check-ok
                if (creature.getAbility(AbilityID.Resist_Petrification) <= 0) {
                    GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_TouchesYourSkin, new Object[]{item.getName()}));
                    creature.death(Locale.format(RS.rs_Touched, new Object[]{item.getName()}), null);
                }
            } else if (deadSign.equals("Corpse")) {
                // check-ok
                creature.addEffect(EffectID.eid_Diseased, state, EffectAction.ea_Persistent, false, "");
            } else if (deadSign.equals("Dreg")) {
                // check-ok
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThatWasPoisonous));
                if (creature.getAbility(AbilityID.Resist_Acid) <= 0) {
                    EffectsFactory.e_Confusion(EffectID.eid_Confusion, creature, source, state, invokeMode, null);
                    creature.applyDamage(AuxUtils.getBoundedRnd(2, 15), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_KilledByGluttony));
                    creature.Strength--;
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsAWeakness));
                } else {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouSeemUnaffected));
                }
            } else if (deadSign.equals("Faleryn")) {
                // check-ok
                creature.dropAll();
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourArmorFallsOff));

                int crID = GlobalVars.nwrBase.findEntryBySign("Faleryn").GUID;
                creature.initEx(crID, true, false);

                GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_YouShallNowBeKnownAsX, new Object[]{creature.getName()}));
            } else if (deadSign.equals("FireDragon")) {
                AuxUtils.exStub("msg-none");
                creature.setAbility(AbilityID.Resist_Heat, creature.getAbility(AbilityID.Resist_Heat) + 1);
            } else if (deadSign.equals("FireGiant") || deadSign.equals("Minion")) {
                // check-ok
                if (creature.getAbility(AbilityID.Resist_Heat) <= 0) {
                    creature.applyDamage(AuxUtils.getBoundedRnd(16, 25), DamageKind.dkRadiation, null, Locale.getStr(RS.rs_ItBurnsYourThroat));
                } else {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouSeemUnaffected));
                }
            } else if (deadSign.equals("Fyleisch")) {
                // check-ok
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_SwallowingKnives));
                creature.death(Locale.getStr(RS.rs_KilledByGluttony), null);
            } else if (deadSign.equals("Glard") || deadSign.equals("Phausq") || deadSign.equals("Serpent")) {
                // check-ok
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThatWasPoisonous));
                if (creature.getAbility(AbilityID.Resist_Poison) <= 0) {
                    if (AuxUtils.chance(5) && !deadSign.equals("Serpent")) {
                        creature.death(Locale.getStr(RS.rs_KilledByGluttony), null);
                    } else {
                        if (deadSign.equals("Glard")) {
                            creature.setAbility(AbilityID.Resist_Poison, creature.getAbility(AbilityID.Resist_Poison) + 1);
                        }
                        EffectsFactory.e_Confusion(EffectID.eid_Confusion, creature, source, state, invokeMode, null);
                        creature.applyDamage(AuxUtils.getBoundedRnd(2, 15), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_KilledByGluttony));
                        creature.Strength--;
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsAWeakness));
                    }
                } else {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouSeemUnaffected));
                }
            } else if (deadSign.equals("Gorm")) {
                // check-ok
                if (creature.getAbility(AbilityID.Resist_Acid) <= 0) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NotGoodIdea));
                    creature.death(Locale.getStr(RS.rs_KilledByGluttony), null);
                }
            } else if (deadSign.equals("HelDragon")) {
                // check-ok
                creature.setAbility(AbilityID.Resist_Petrification, creature.getAbility(AbilityID.Resist_Petrification) + 1);
                creature.Constitution += AuxUtils.getBoundedRnd(1, 5);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_MeatHumms));
            } else if (deadSign.equals("IceDragon") || deadSign.equals("IceSphere")) {
                AuxUtils.exStub("msg-none");
                creature.setAbility(AbilityID.Resist_Cold, creature.getAbility(AbilityID.Resist_Cold) + 1);
            } else if (deadSign.equals("IvyCreeper")) {
                EffectsFactory.CureEffect(creature, EffectID.eid_BrainScarring, Locale.getStr(RS.rs_BrainScarringIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Hallucination, Locale.getStr(RS.rs_HallucinationIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Confusion, Locale.getStr(RS.rs_ConfusionIsCured));
                EffectsFactory.CureEffect(creature, EffectID.eid_Insanity, "");
            } else if (deadSign.equals("Jagredin")) {
                creature.applyDamage(AuxUtils.getBoundedRnd(24, 109), DamageKind.dkPhysical, null, "");
            } else if (deadSign.equals("Knilb") || deadSign.equals("Nymph")) {
                AuxUtils.exStub("msg-none");
                creature.addEffect(EffectID.eid_Relocation, ItemState.is_Normal, EffectAction.ea_RandomTurn, false, "");
                AuxUtils.exStub("todo: добавить модификатор неизлечимости");
            } else if (deadSign.equals("LowerDwarf")) {
                // check-ok
                creature.setAbility(AbilityID.Resist_Heat, 0);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_LakeOfFire));
            } else if (deadSign.equals("Mudman")) {
                // check-ok
                GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_TastedGritty, new Object[]{dead.getDeclinableName(Number.nSingle, Case.cGenitive)}));
            } else if (deadSign.equals("PhantomAsp")) {
                // check-ok
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThatWasPoisonous));
                if (creature.getAbility(AbilityID.Resist_Poison) <= 0) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_PoisonWasDeadly));
                    creature.death(Locale.getStr(RS.rs_KilledByGluttony), null);
                }
            } else if (deadSign.equals("Preden")) {
                // check-ok
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_Ughh));
            } else {
                if (deadSign.equals("Retchweed")) {
                    // check-ok
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouRetch));
                } else if (deadSign.equals("Sandiff")) {
                    // check-ok
                    if (creature.getAbility(AbilityID.Resist_Acid) <= 0) {
                        creature.applyDamage(AuxUtils.getBoundedRnd(0, 45), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_AcidicMeatBurns));
                    }
                } else if (deadSign.equals("Shade")) {
                    // check-ok
                    GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_YouGnawOnBones, new Object[]{dead.getDeclinableName(Number.nSingle, Case.cGenitive)}));
                } else if (deadSign.equals("Slinn")) {
                    // check-ok
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_MeatRots));
                } else if (deadSign.equals("Stalker")) {
                    AuxUtils.exStub("msg-none");
                    EffectsFactory.e_Confusion(EffectID.eid_Confusion, creature, source, state, invokeMode, ext);
                    AuxUtils.exStub("todo: усилить в два раза");
                    EffectsFactory.e_Invisibility(EffectID.eid_Invisibility, creature, source, state, invokeMode, ext);
                    AuxUtils.exStub("todo: ослабить в 5 раз 110-210 -> 35-41");
                } else if (deadSign.equals("StunWorm")) {
                    // check-ok
                    creature.applyDamage(AuxUtils.getBoundedRnd(11, 20), DamageKind.dkRadiation, null, Locale.getStr(RS.rs_YouFeelNumb));
                } else if (deadSign.equals("Wight")) {
                    // check-ok
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelHalfDead));
                    creature.setHPMax(creature.HPMax / 2);
                    creature.HPCur = creature.HPMax;
                    if (creature.HPCur <= 1) {
                        creature.death(Locale.getStr(RS.rs_Starved), null);
                    }
                } else if (deadSign.equals("Wraith")) {
                    // check-ok
                    EffectsFactory.e_Experience(EffectID.eid_Experience, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                }
            }
        }
    }

    public static void e_Fragile(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_FrostTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        boolean insul = creature.getEffects().findEffectByID(EffectID.eid_Insulation) != null;
        boolean resist = !creature.hasAffect(EffectID.eid_FrostTrap);

        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouCoveredInFrost));
        AuxUtils.exStub("todo: rs_KilledByIceTrap!!!");

        if (!insul && !resist) {
            creature.applyDamage(AuxUtils.getBoundedRnd(25, 45), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_YouTooBadlyFreezed));
        }

        if (!insul) {
            ItemsList list = creature.getItems();

            boolean brk = false;
            int num = list.getCount();
            for (int i = 0; i < num; i++) {
                Item it = list.getItem(i);
                if (it.getKind() == ItemKind.ik_Potion && AuxUtils.chance(51)) {
                    list.remove(it);
                    brk = true;
                }
            }
            
            if (brk) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_SomePotionsFreeze));
            }
        }
    }

    public static void e_FunnelHurl(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: not joined!!!");
        AuxUtils.exStub("tree");
        AuxUtils.exStub("dead tree");
        AuxUtils.exStub("The funnel engulfs the ");
        AuxUtils.exStub(".");
        AuxUtils.exStub("the ");
        AuxUtils.exStub("The funnels destroy each other.");
        AuxUtils.exStub("The funnel kills ");
        AuxUtils.exStub("The funnel envelops ");
        AuxUtils.exStub("The funnel destroys ");
        AuxUtils.exStub("The gale-force winds shred you to bits.");
        AuxUtils.exStub("Torn apart in a tornado");
        AuxUtils.exStub("The funnel spews out the dead tree.");
        AuxUtils.exStub("The dead tree collides with ");
        AuxUtils.exStub("The dead tree slams into you.");
        AuxUtils.exStub("Hit by a flying tree");
        AuxUtils.exStub("The funnel releases ");
        AuxUtils.exStub("The funnel hurls ");
        AuxUtils.exStub("The funnel flings you like a rag doll.");
    }

    public static void e_Genesis(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub(" -> rCreature.TCreature.DoTurn ");
        if (invokeMode != InvokeMode.im_UseBegin) {
            if (invokeMode == InvokeMode.im_UseEnd) {
                creature.doneEffect(effectID, source);
            }
        } else {
            creature.initEffect(effectID, source, EffectAction.ea_Persistent);
        }
    }

    public static void e_CheckGeology(EffectID effectID, NWCreature creature, Object source, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode == InvokeMode.im_ItSelf) {
            ext.ReqParams = new EffectParams(EffectParams.ep_Place, EffectParams.ep_TileID);
        } else {
            ext.ReqParams = new EffectParams();
        }
    }

    public static void e_Geology(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use:
                creature.setSkill(SkillID.Sk_Terraforming, creature.getSkill(SkillID.Sk_Terraforming) + 1);
                if (creature.getSkill(SkillID.Sk_Terraforming) == 1) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveGetATerraformingSkill));
                } else {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ReshapePowerGrows));
                }
                break;

            case im_ItSelf:
                NWField fld = creature.getCurrentField();
                Point pt = (Point) ext.getParam(EffectParams.ep_Place);
                NWTile tile = fld.getTile(pt.X, pt.Y);
                EffectsFactory.Geology_PrepareTile(fld, creature, ext, pt, tile);
                fld.normalize();
                break;
        }
    }

    public static void e_GjallUse(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        GlobalVars.nwrGame.doRagnarok();
    }

    public static void e_GolemCreation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("pulpy mass");
        AuxUtils.exStub("charred wraith");
        AuxUtils.exStub("You cannot make a golem of ");
        AuxUtils.exStub(" is ");
        AuxUtils.exStub(" are ");
        AuxUtils.exStub("grafted onto your ");
        AuxUtils.exStub("todo: После убийства голема остается \"труп\", называется \"pulpy mass\"");

        Item aExtItem = (Item) ext.getParam(EffectParams.ep_Item);
        if (aExtItem.CLSID != GlobalVars.iid_DeadBody) {
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_ThisNotDeadbody));
        } else {
            NWCreature mon = EffectsFactory.animateDeadBody(creature, aExtItem);
            String cs = mon.getDeclinableName(Number.nSingle, Case.cGenitive);
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.format(RS.rs_GolemCreated, new Object[]{cs}));
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_GolemUncontrolled));
        }
    }

    public static void e_GrapplingHookUse(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.getEffects().findEffectByID(EffectID.eid_PhaseTrap) == null) {
            GrapplingHookRay ray = new GrapplingHookRay();
            try {
                ray.Exec(creature, EffectID.eid_GrapplingHookUse, invokeMode, ext, "");
                AuxUtils.exStub("rsList(rs_StoningRayIsZapped]");
            } finally {
            }
        }
    }

    public static void e_Hallucination(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: You are temporarily slack-jawed and unable to move.'->'You can move again.");

        switch (invokeMode) {
            case im_Use:
                if (creature.hasAffect(EffectID.eid_Hallucination)) {
                    creature.addEffect(EffectID.eid_Hallucination, state, EffectAction.ea_Persistent, true, Locale.getStr(RS.rs_YouSeeAHallucination));
                }
                break;
            case im_ItSelf:
                break;
            case im_FinAction:
                break;
        }

    }

    public static void e_Hardening(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        AuxUtils.exStub("todo:  remove stoning?");
        AuxUtils.exStub("todo:  это эффект sentinel'я - окаменение");
        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode == InvokeMode.im_ItSelf) {
                Effect e = creature.getEffects().findEffectByID(EffectID.eid_Hardening);
                if (e.Source == null && e.Duration == 1) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouAreStatue));
                    creature.death(Locale.getStr(RS.rs_ImmortalizedInStone), null);
                }
            }
        } else {
            if (creature.hasAffect(EffectID.eid_Hardening)) {
                creature.addEffect(EffectID.eid_Hardening, state, EffectAction.ea_EachTurn, true, Locale.getStr(RS.rs_YourBodyIsHardening));
            }
        }
    }

    public static void e_Hastening(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWCreature c = (NWCreature) ext.getParam(EffectParams.ep_Creature);
        if (c.hasAffect(EffectID.eid_Hastening)) {
            c.addEffect(EffectID.eid_Speedup, state, EffectAction.ea_Persistent, false, "");
        }
    }

    public static void e_HealInability(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ");
    }

    public static void e_Healing(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        EffectsFactory.e_Cure(EffectID.eid_Cure, (NWCreature) ext.getParam(EffectParams.ep_Creature), source, ItemState.is_Normal, InvokeMode.im_ItSelf, ext);
    }

    private static void HeatCloud(NWField field, int aX, int aY)
    {
        AuxUtils.randomize();
        for (int y = aY - 3; y <= aY + 3; y++) {
            for (int x = aX - 3; x <= aX + 3; x++) {
                if (AuxUtils.distance(aX, aY, x, y) <= 3) {
                    MapObject mapObj = new MapObject(GameSpace.getInstance(), field);
                    mapObj.setPos(x, y);
                    mapObj.initByEffect(EffectID.eid_Heat);
                    mapObj.IsNeedUpdate = true;
                    mapObj.FrameIndex = 1 + AuxUtils.getRandom(3);
                    mapObj.Loops = 5;
                    field.getFeatures().add(mapObj);
                }                
            }
        }
    }

    public static void e_Heat(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode == InvokeMode.im_ItSelf) {
                NWField f = creature.getCurrentField();

                int px = creature.getPosX();
                int py = creature.getPosY();

                EffectsFactory.HeatCloud(f, px, py);

                AuxUtils.exStub("todo:  this message only for skill \"heat radiation\"");
                AuxUtils.exStub("todo:   other skills and effects - other messages");
                AuxUtils.exStub("todo:   -> separate ");
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_BurnCloudStrikesNearYou));

                for (int y = py - 3; y <= py + 3; y++) {
                    for (int x = px - 3; x <= px + 3; x++) {
                        NWTile tile = (NWTile) f.getTile(x, y);
                        if (tile != null && tile.getForeBase() == PlaceID.pid_Tree) {
                            tile.setFore(PlaceID.pid_Undefined);
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_TreeBursts));
                        }

                        NWCreature c = (NWCreature) f.findCreature(x, y);
                        if (c != null && !c.equals(creature) && c.hasAffect(EffectID.eid_Heat)) {
                            String msg = Locale.format(RS.rs_IsSlain, new Object[]{c.getDeclinableName(Number.nSingle, Case.cNominative)});
                            c.applyDamage(AuxUtils.getBoundedRnd(21, 40), DamageKind.dkRadiation, null, msg);
                        }
                    }
                }
            }
        } else {
            creature.setSkill(SkillID.Sk_HeatRadiation, creature.getSkill(SkillID.Sk_HeatRadiation) + 1);
            if (creature.getSkill(SkillID.Sk_HeatRadiation) == 1) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouGetAHeatRadiationSkill));
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourBodyGrowsWarmer));
            }
        }
    }

    public static void e_HPEnlarge(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("msg-none");
        AuxUtils.exStub("green mushroom");
        creature.setHPMax(creature.HPMax + 1);
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveBecomeMoreRobust));
    }

    public static void e_Husbandry(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWCreature cr = (NWCreature) ext.getParam(EffectParams.ep_Creature);
        if (!cr.equals(creature) && !cr.getEntry().Flags.contains(CreatureFlags.esMind)) {
            cr.clone(false);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_BreedingIsSuccesful));
        } else {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourAttemptsAreFutile));
        }
    }

    public static void e_Ice(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub(" The ... ray hits the <monster>.");
        IceRay ray = new IceRay();
        ray.Exec(creature, EffectID.eid_Ice, invokeMode, ext, EffectsFactory.WandRayMsg(EffectID.eid_Ice, source));
    }

    public static void e_Identify(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: question msg: rs_EmpowerWhich");
        if (creature.isConfused() || state == ItemState.is_Cursed) {
            int num = creature.getItems().getCount();
            for (int i = 0; i < num; i++) {
                creature.getItems().getItem(i).setIdentified(false);
            }
        } else {
            if (state != ItemState.is_Normal) {
                if (state == ItemState.is_Blessed) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_BuzzingSound));

                    int num2 = creature.getItems().getCount();
                    for (int i = 0; i < num2; i++) {
                        creature.getItems().getItem(i).setIdentified(true);
                    }
                }
            } else {
                Item aExtItem = (Item) ext.getParam(EffectParams.ep_Item);
                aExtItem.setIdentified(true);
            }
        }
    }

    public static void e_Idle(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // stub for copy/paste
        switch (state) {
            case is_Normal:
                break;
            case is_Blessed:
                break;
            case is_Cursed:
                break;
        }

        // stub for copy/paste
        switch (invokeMode) {
            case im_Use:
                break;
            case im_UseBegin:
                break;
            case im_UseEnd:
                break;
            case im_ItSelf:
                break;
            case im_FinAction:
                break;
        }

        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NoHappens));
    }

    public static void e_IllusorySelf(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWCreature illusion = creature.clone(true);
        if (illusion != null) {
            illusion.Illusion = true;
            ((SentientBrain) illusion.getBrain()).setEscortGoal(creature, false);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveMadeAnIllusionOfYourself));
        }
    }

    public static void e_Immortal(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use:
            case im_ItSelf:
                createMapObject(effectID, creature.getCurrentField(), creature.getPosX(), creature.getPosY(), true);
                creature.addEffect(EffectID.eid_Immortal, state, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_YouveGetASecondLife));
                break;
        }
    }

    public static void e_Immunity(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_UseBegin:
                creature.initEffect(effectID, source, EffectAction.ea_Persistent);
                break;
            case im_UseEnd:
                creature.doneEffect(effectID, source);
                break;
        }
    }

    public static void e_Impregnation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.hasAffect(EffectID.eid_Impregnation)) {
            AuxUtils.exStub("todo: ???");
        }
    }

    public static void e_Imprisoning(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: target msg 'Imprison which?'");
        NWCreature victim = (NWCreature) ext.getParam(EffectParams.ep_Creature);

        if (victim.getEntry().Flags.hasIntersect(CreatureFlags.esSwimming, CreatureFlags.esPlant)) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_OrbHasNoPower));
        } else {
            if (victim.isImprisonable()) {
                victim.transferTo(GlobalVars.Layer_Vanaheim, 0, 0, -1, -1, StaticData.MapArea, true, false);
                if (creature.isPlayer()) {
                    GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_ScreamsAndDisappearsInThickenedAir, new Object[]{victim.getName()}));
                    if (((Player) creature).Morality <= -10 && creature.LayerID != GlobalVars.Layer_Vanaheim) {
                        creature.transferTo(GlobalVars.Layer_Vanaheim, 0, 0, -1, -1, StaticData.MapArea, true, false);
                        String msg = Locale.getStr(RS.rs_SphereMiredYouInItselfAndDisappears);
                        GlobalVars.nwrWin.showText(creature, msg, new LogFeatures(LogFeatures.lfDialog));
                    }
                }
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_TooMightyAndDoesntSubjectedToIt));
            }
        }
    }

    public static void e_Infravision(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Insanity(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use:
                AuxUtils.exStub("todo: trace source by IDA");
                creature.addEffect(EffectID.eid_Insanity, state, EffectAction.ea_RandomTurn, false, Locale.getStr(RS.rs_YouBecomesInsane));
                EffectsFactory.e_Hallucination(EffectID.eid_Hallucination, creature, source, state, invokeMode, ext);
                break;

            case im_ItSelf:
                if (!creature.Prowling) {
                    EffectsFactory.e_Prowling(EffectID.eid_Insanity, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                }
                break;

            case im_FinAction:
                break;
        }
    }

    public static void e_Insulation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Intoxicate(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo:  remove confused?");
        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode == InvokeMode.im_ItSelf) {
                AuxUtils.exStub("todo:  incorrect code, replace by skipped turns for [rs_YouPassOut]}");
            }
        } else {
            if (creature.hasAffect(EffectID.eid_Intoxicate)) {
                Effect ef = creature.getEffects().findEffectByID(EffectID.eid_Intoxicate);
                if (ef == null) {
                    creature.addEffect(EffectID.eid_Intoxicate, ItemState.is_Normal, EffectAction.ea_EachTurn, false, Locale.getStr(RS.rs_YouFeelIntoxicated));
                } else {
                    ef.Duration += Effect.getDuration(EffectID.eid_Intoxicate, ItemState.is_Normal, false);
                    if (ef.Duration > 160) {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouHaveOverworkedYourLiver));
                        creature.death(Locale.getStr(RS.rs_KilledByAlcoholAbuse), null);
                    } else {
                        if (ef.Duration > 120) {
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouGag));
                        } else {
                            if (ef.Duration > 80) {
                                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelMoreIntoxicated));
                            }
                        }
                    }
                }
            }
        }
    }

    public static void e_Invisibility(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use:
                creature.addEffect(EffectID.eid_Invisibility, state, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_YouAreInvisible));
                break;

            case im_UseBegin:
                creature.initEffect(EffectID.eid_Invisibility, source, EffectAction.ea_Persistent);
                break;

            case im_UseEnd:
                creature.doneEffect(EffectID.eid_Invisibility, source);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouVisibleAgain));
                break;

            case im_ItSelf:
                break;
        }
    }

    public static void e_Invulnerable(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode != InvokeMode.im_UseBegin) {
                if (invokeMode == InvokeMode.im_UseEnd) {
                    creature.doneEffect(EffectID.eid_Invulnerable, source);
                }
            } else {
                creature.initEffect(EffectID.eid_Invulnerable, source, EffectAction.ea_Persistent);
            }
        } else {
            creature.addEffect(EffectID.eid_Invulnerable, state, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_YouAreInvulnerable));
        }
    }

    public static void e_Knowledge(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("This scroll explains the art of ");
        AuxUtils.exStub("swimming.");
        AuxUtils.exStub("alchemy.");
        AuxUtils.exStub("teleporting at will.");
        AuxUtils.exStub("the swordsman.");
        AuxUtils.exStub("identification.");
        AuxUtils.exStub("telepathy.");
        AuxUtils.exStub("map-making.");
        AuxUtils.exStub("the blacksmith.");
        AuxUtils.exStub("heat vision.");
        AuxUtils.exStub("animal husbandry.");
        AuxUtils.exStub("levitation.");
        AuxUtils.exStub("mind control.");
        AuxUtils.exStub("golem creation.");
        AuxUtils.exStub("corpse preservation.");
        AuxUtils.exStub("ventriloquism. (Amaze your friends) ");
        AuxUtils.exStub("writing.");
        AuxUtils.exStub("terraforming.");

        int i = AuxUtils.getRandom(StaticData.dbTeachable.length);
        int id = StaticData.dbTeachable[i].id;
        if (StaticData.dbTeachable[i].kind == TeachableKind.Ability) {
            AbilityID ab = AbilityID.forValue(id);
            creature.setAbility(ab, creature.getAbility(ab) + 1);
        } else {
            SkillID sk = SkillID.forValue(id);
            creature.setSkill(sk, creature.getSkill(sk) + 1);
        }
        AuxUtils.exStub("You are now a fletcher.");
        AuxUtils.exStub("You now hold sway over lesser creatures.");
        AuxUtils.exStub("You learn how to write new scrolls.");
        AuxUtils.exStub("limited ");
        AuxUtils.exStub("You gain the ");
        AuxUtils.exStub("skill of writing.");
        AuxUtils.exStub("Your knowledge of writing increases.");
        if (StaticData.dbTeachable[i].CommentRS != RS.rs_Reserved) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(StaticData.dbTeachable[i].CommentRS));
        }
    }

    public static void e_LavaStrike(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();

        if (creature.isConfused()) {
            int id = GlobalVars.nwrBase.findEntryBySign("BlueRock").GUID;
            int cnt = AuxUtils.getBoundedRnd(2, 5);
            for (int i = 1; i <= cnt; i++) {
                Point pt = creature.getNearestPlace(3, false);
                if (pt != null) {
                    Item item = new Item(GameSpace.getInstance(), fld);
                    item.setCLSID(id);
                    item.Count = 1;
                    item.setPos(pt.X, pt.Y);
                    item.Owner = fld;
                    fld.getItems().add(item, false);

                    NWCreature c = (NWCreature) fld.findCreature(pt.X, pt.Y);
                    if (c != null) {
                        c.applyDamage(AuxUtils.getBoundedRnd(20, 40), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_RocksAreRainedOnYourHead));
                    }
                }
            }
        } else {
            int cnt = AuxUtils.getBoundedRnd(2, 5);
            for (int i = 1; i <= cnt; i++) {
                Point pt = creature.getNearestPlace(3, false);
                if (pt != null) {
                    NWTile tile = fld.getTile(pt.X, pt.Y);
                    tile.Foreground = (short) PlaceID.pid_LavaPool;

                    NWCreature c = (NWCreature) fld.findCreature(pt.X, pt.Y);
                    if (c != null) {
                        RaceID race = c.getEntry().Race;
                        if (race == RaceID.crDefault || race == RaceID.crHuman) {
                            c.death("", null);
                        }
                    }
                }
            }

            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_LavaIsRained));
        }
    }

    public static void e_LavaTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        boolean ashes = creature.getEffects().findEffectByID(EffectID.eid_Ashes) != null;
        boolean resist = !creature.hasAffect(EffectID.eid_LavaTrap);

        NWField fld = creature.getCurrentField();

        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_GroundHereTurnsToLava));
        fld.getTile(creature.getPosX(), creature.getPosY()).Background = (short) PlaceID.pid_Lava;
        fld.normalize();

        if (!ashes && !resist) {
            if (creature.getEffects().findEffectByID(EffectID.eid_Burns) == null) {
                creature.applyDamage(AuxUtils.getBoundedRnd(15, 40), DamageKind.dkPhysical, null, "");
                creature.addEffect(EffectID.eid_Burns, ItemState.is_Normal, EffectAction.ea_EachTurn, false, Locale.getStr(RS.rs_LavaBurnsYou));
            } else {
                creature.death(Locale.getStr(RS.rs_YouTooBadlyBurned), null);
            }
        }
    }

    public static void e_LazlulRopeUse(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        creature.moveToUp();
    }

    public static void e_LegsMissing(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Light(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode == InvokeMode.im_ItSelf) {
                AuxUtils.exStub("action in TPlayer.DoTurn().ApplyLightEffect();");
            }
        } else {
            creature.addEffect(EffectID.eid_Light, state, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_WandGlows));
        }
    }

    public static void e_LocusMastery(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("-> TEffect.Execute");
        if (invokeMode != InvokeMode.im_UseBegin) {
            if (invokeMode == InvokeMode.im_UseEnd) {
                creature.doneEffect(effectID, source);
            }
        } else {
            creature.initEffect(effectID, source, EffectAction.ea_Persistent);
        }
    }

    public static void e_Lodestone(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();

        switch (AuxUtils.getRandom(5)) {
            case 0: {
                int tx = AuxUtils.getBoundedRnd(0, StaticData.FieldWidth - 1);
                int ty = AuxUtils.getBoundedRnd(0, StaticData.FieldHeight - 1);

                NWTile tile = fld.getTile(tx, ty);
                tile.Foreground = (short) PlaceID.pid_Rock;

                NWCreature cr = (NWCreature) fld.findCreature(tx, ty);
                if (cr != null && cr.getEffects().findEffectByID(EffectID.eid_Phase) == null) {
                    cr.death(Locale.getStr(RS.rs_FusedInStone), null);
                }
                AuxUtils.exStub("todo: message");
                break;
            }
            case 1: {
                int cnt = AuxUtils.getBoundedRnd(2, 5);
                for (int i = 1; i <= cnt; i++) {
                    Point pt = fld.getNearestPlace(creature.getPosX(), creature.getPosY(), 5, true, new Movements(Movements.mkWalk, Movements.mkFly));
                    if (pt == null) {
                        Logger.write("EffectsFactory.e_Lodestone.1(): empty point");
                    } else {
                        UniverseBuilder.gen_Creature(fld, -1, pt.X, pt.Y, true);
                    }
                }
                AuxUtils.exStub("todo: message");
                break;
            }
            case 2: {
                int i = AuxUtils.getRandom(creature.getItems().getCount());
                creature.deleteItem(creature.getItems().getItem(i));
                AuxUtils.exStub("todo: message");
                break;
            }
            case 3: {
                creature.Luck--;
                AuxUtils.exStub("todo: message");
                break;
            }
            case 4: {
                creature.applyDamage(AuxUtils.getBoundedRnd(5, 27), DamageKind.dkPhysical, null, "");
                AuxUtils.exStub("todo: message");
                break;
            }
        }
    }

    public static void e_Luck(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (state == ItemState.is_Cursed) {
                creature.Luck -= AuxUtils.getBoundedRnd(1, 4);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_SkinBurns));
                creature.applyDamage(AuxUtils.getBoundedRnd(1, 4), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_Consumed));
        } else {
            creature.Luck++;
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_HolyWater));
        }
    }

    public static void e_Prowling(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use:
            case im_ItSelf:
                creature.ProwlingBegin(effectID);
                break;

            case im_FinAction:
                creature.ProwlingEnd();
                break;
        }
    }

    public static void e_Lycanthropy(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use:
                if (creature.hasAffect(EffectID.eid_Lycanthropy)) {
                    creature.addEffect(EffectID.eid_Lycanthropy, state, EffectAction.ea_RandomTurn, true, Locale.getStr(RS.rs_YouveGetALicanthropy));
                }
                break;

            case im_ItSelf:
                if (!creature.Prowling) {
                    EffectsFactory.e_Prowling(EffectID.eid_Lycanthropy, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                }
                break;
        }
    }

    public static void e_Might(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_MindControl(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWCreature cr = (NWCreature) ext.getParam(EffectParams.ep_Creature);
        if (cr != null && !cr.equals(creature)) {
            RaceID race = cr.getEntry().Race;
            if ((race == RaceID.crDefault || race == RaceID.crHuman) && (cr.getEntry().Flags.contains(CreatureFlags.esMind) && cr.hasAffect(EffectID.eid_MindControl))) {
                cr.addEffect(EffectID.eid_Obedience, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_MindControlDone));
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NothingHappens));
            }
        } else {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NothingHappens));
        }
    }

    public static void e_MistTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_StrangeMistSeepsOutOfGround));
        switch (AuxUtils.getRandom(8)) {
            case 0: {
                EffectsFactory.e_Cure(EffectID.eid_Cure, creature, null, ItemState.is_Blessed, InvokeMode.im_ItSelf, null);
                break;
            }
            case 1: {
                if (creature.getAbility(AbilityID.Resist_Acid) <= 0) {
                    creature.applyDamage(AuxUtils.getBoundedRnd(1, creature.HPCur / 2), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_YourBodyIsCoveredWithAcid));
                }
                break;
            }
            case 2: {
                creature.Strength = 3;
                creature.addEffect(EffectID.eid_Withered, state, EffectAction.ea_EachTurn, false, Locale.getStr(RS.rs_YouAreWithered));
                break;
            }
            case 3: {
                creature.Constitution -= AuxUtils.getBoundedRnd(1, 3);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourHealthIsAffected));
                break;
            }
            case 4: {
                EffectsFactory.e_Insanity(EffectID.eid_Insanity, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourMindIsAffected));
                break;
            }
            case 5: {
                AuxUtils.exStub("todo: message");
                creature.ArmorClass -= AuxUtils.getBoundedRnd(2, 5);
                break;
            }
            case 6: {
                creature.Strength -= AuxUtils.getBoundedRnd(1, 3);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourStrengthIsAffected));
                break;
            }
            case 7: {
                creature.Dexterity = (short) ((int) creature.Dexterity - AuxUtils.getBoundedRnd(1, 3));
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourDexterityIsAffected));
                break;
            }
            case 8: {
                AuxUtils.exStub("rs_YourSkinIsAffected");
                break;
            }
        }
        AuxUtils.exStub("todo: прочие эффекты неизвестны");
        AuxUtils.exStub("Vanseril := TField(creature.CurrentMap).Creatures.EntityByCLSID(cid_Vanseril);if (Vanseril <> nil) then beginval := BoundedRnd(15, 35);creature.ApplyDamage(val, ');Vanseril.HPCur := Vanseril.HPCur + val;fxme: проверить - это другой эффектend;");
    }

    public static void e_MonsterSkill(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWCreature enemy = (NWCreature) ext.getParam(EffectParams.ep_Creature);

        switch (effectID) {
            case eid_Basilisk_Poison: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkPhysical);
                break;
            }

            case eid_Borgonvile_Cloud: {
                AuxUtils.exStub(" смерчь кружащихся скал (whirling cloud of rocks) ");
                AuxUtils.exStub("The whirling rocks from the borgon vile assault you.");
                AuxUtils.exStub("A cloud of whirling rocks assault you.");
                AuxUtils.exStub("iid_DiamondNeedle - раскидать");
                break;
            }

            case eid_Breleor_Tendril: {
                AuxUtils.exStub("... -> dd");
                AuxUtils.exStub("rs_YouCharmed");
                break;
            }

            case eid_Ellegiant_Throw: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkPhysical);
                AuxUtils.exStub(" метание валунов ok ");
                break;
            }

            case eid_Ellegiant_Crush: {
                EffectsFactory.SimpleAttack(enemy, effectID, DamageKind.dkPhysical, "");
                break;
            }

            case eid_Firedragon_Breath: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkRadiation);
                break;
            }

            case eid_Firegiant_Touch: {
                EffectsFactory.e_Heat(effectID, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, ext);
                break;
            }

            case eid_Fyleisch_Cloud: {
                FyleischCloud f_cloud = new FyleischCloud();
                f_cloud.generate(creature.getCurrentField(), creature.getPosX(), creature.getPosY());
                break;
            }

            case eid_Gasball_Explosion: {
                EffectsFactory.SimpleAttack(enemy, effectID, DamageKind.dkPhysical, "");
                AuxUtils.exStub("взрываются около своих жертв, причиняя физический ущерб и потерю слуха. Жертвы будут оглушены на несколько ходов");
                break;
            }

            case eid_Giantsquid_Crush: {
                AuxUtils.exStub("хватают и уничтожают жертву своими щупальцами");
                break;
            }

            case eid_Glard_Poison: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkPhysical);
                break;
            }

            case eid_Hatchetfish_Teeth: {
                EffectsFactory.SimpleAttack(enemy, effectID, DamageKind.dkPhysical, "");
                break;
            }

            case eid_Heldragon_Cloud: {
                AuxUtils.exStub("?? > The blinding wind from the hel dragon tears into you.");
                break;
            }

            case eid_Hillgiant_Crush: {
                AuxUtils.exStub("?? > A rolling boulder misses/hit you.");
                AuxUtils.exStub("?? > The hill giant hits.");
                GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_YouAreBeingCrushed));
                EffectsFactory.SimpleAttack(enemy, effectID, DamageKind.dkPhysical, "");
                break;
            }

            case eid_Icedragon_Breath: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkRadiation);
                break;
            }

            case eid_Icesphere_Blast: {
                AuxUtils.exStub("простой взрыв");
                break;
            }

            case eid_Jagredin_Burning: {
                EffectsFactory.SimpleAttack(enemy, effectID, DamageKind.dkPhysical, "");
                break;
            }

            case eid_Knellbird_Gaze: {
                AuxUtils.exStub("хаотична, атакует смертельным взглядом");
                break;
            }

            case eid_Kobold_Throw:
            case eid_Lowerdwarf_Throw: {
                AuxUtils.exStub("обычное метание, deprecated");
                break;
            }

            case eid_Moleman_Debris: {
                AuxUtils.exStub("обрушение потолка");
                break;
            }

            case eid_Phantomasp_Poison: {
                AuxUtils.exStub("Укус призрачного аспида - немедленная смерть. Даже устойчивые к ядам, будучи им укушены умрут с вероятностью 4% (если не наденут военный жилет");
                break;
            }

            case eid_Pyrtaath_Throttle: {
                EffectsFactory.SimpleAttack(enemy, effectID, DamageKind.dkPhysical, "");
                break;
            }

            case eid_Ramapith_FireTouch: {
                EffectsFactory.SimpleAttack(enemy, effectID, DamageKind.dkPhysical, "");
                break;
            }

            case eid_Sandiff_Acid: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkPhysical);
                break;
            }

            case eid_Scyld_Breath: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkRadiation);
                break;
            }

            case eid_Scyld_Ray: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkRadiation);
                break;
            }

            case eid_Scyld_ShockWave: {
                AuxUtils.exStub("отбрасывание на большое расстояние");
                break;
            }

            case eid_Sentinel_Gaze: {
                AuxUtils.exStub("... -> dd");
                if (AuxUtils.getRandom(4) == 0) {
                    enemy.addEffect(EffectID.eid_Hardening, ItemState.is_Normal, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_YourBodyIsHardening));
                }
                break;
            }

            case eid_Serpent_Poison: {
                EffectsFactory.SimpleAttack(enemy, effectID, DamageKind.dkPhysical, "");
                break;
            }

            case eid_Shadow_Touch: {
                AuxUtils.exStub(" вытягивают Ваш максимум hit point'ов 1 ед.");
                enemy.setHPMax(enemy.HPMax - 1);
                enemy.applyDamage(1, DamageKind.dkPhysical, null, "");
                break;
            }

            case eid_Slinn_Gout: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkPhysical);
                break;
            }

            case eid_Spirit_Touch: {
                AuxUtils.exStub("прикосновение может заморозить кровь. Духи могут вытянуть у своей жертвы максимум hit point'ов и оглушить (stun) её на несколько ходов");
                break;
            }

            case eid_Stunworm_Stun: {
                EffectsFactory.SimpleAttack(enemy, effectID, DamageKind.dkRadiation, "");
                break;
            }

            case eid_Terrain_Burning: {
                AuxUtils.exStub("... -> dd");
                break;
            }

            case eid_Warrior_Throw:
            case eid_Womera_Throw:
            case eid_Wooddwarf_Throw: {
                AuxUtils.exStub("обычное метание, deprecated");
                break;
            }

            case eid_Watcher_Gaze: {
                AuxUtils.exStub("???");
                break;
            }

            case eid_Wyvern_Breath: {
                EffectsFactory.Ray(effectID, creature, invokeMode, ext, DamageKind.dkRadiation);
                break;
            }

            case eid_Zardon_PsiBlast: {
                AuxUtils.exStub("Ментальный удар на большом расстоянии");
                AuxUtils.exStub("msg: ???");
                break;
            }

            case eid_Ull_Gaze: {
                AuxUtils.exStub("todo: ???");
                AuxUtils.exStub("??? e_Confusion(eid_Confusion, enemy, nil, is_Normal, im_ItSelf, ");
                GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_GazeMakesYouFeelDisoriented));
                break;
            }
        }
    }

    public static void e_MonsterTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();

        DungeonRoom dunRoom = creature.findDungeonRoom();
        if (dunRoom != null) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_MonsterTrapEntering));
            ArrayList<Integer> validCreatures = fld.ValidCreatures;
            int cnt = (validCreatures != null) ? validCreatures.size() : 0;
            int id = fld.ValidCreatures.get(AuxUtils.getRandom(cnt));

            Rect r = dunRoom.getArea().clone();
            r.inflate(-1, -1);

            AbstractMap map = (AbstractMap) dunRoom.Owner;

            for (int y = r.Top; y <= r.Bottom; y++) {
                for (int x = r.Left; x <= r.Right; x++) {
                    if (r.isBorder(x, y)) {
                        if (map instanceof NWField) {
                            UniverseBuilder.gen_Creature(((NWField) map), id, x, y, false);
                        } else {
                            if (map instanceof NWLayer) {
                                ((NWLayer) map).gen_Creature(id, x, y, false);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void e_Music(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (invokeMode == InvokeMode.im_Use) {
            creature.setAbility(AbilityID.Ab_MusicalAcuity, creature.getAbility(AbilityID.Ab_MusicalAcuity) + 1);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveGetAMusicalAcuty));
        }
    }

    public static void e_Mystery(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode == InvokeMode.im_Use) {
            int num = AuxUtils.getRandom(3);
            if (num != 0) {
                if (num != 1) {
                    if (num == 2) {
                        creature.setSkill(SkillID.Sk_Writing, creature.getSkill(SkillID.Sk_Writing) + 1);
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouLearnWrite));
                    }
                } else {
                    creature.setSkill(SkillID.Sk_Fennling, creature.getSkill(SkillID.Sk_Fennling) + 1);
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouLearnFennl));
                }
            } else {
                creature.addEffect(EffectID.eid_LegsMissing, ItemState.is_Normal, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_LegsGone));
            }
        }
    }

    public static void e_CheckOcarinaUse(EffectID effectID, NWCreature creature, Object source, InvokeMode invokeMode, EffectExt ext)
    {
        if (((Item) source).CLSID == GlobalVars.iid_GlassOcarina && creature.getAbility(AbilityID.Ab_MusicalAcuity) > 0) {
            ext.ReqParams = new EffectParams(EffectParams.ep_Land);
        } else {
            ext.ReqParams = new EffectParams();
        }
    }

    public static void e_OcarinaUse(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (source instanceof Item) {
            boolean music = creature.getAbility(AbilityID.Ab_MusicalAcuity) > 0;
            if (!music) {
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_OcarinaTune));
            } else {
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_OcarinaBeautifulTune));
            }

            if (((Item) source).CLSID == GlobalVars.iid_GlassOcarina) {
                int land_id = (ext == null) ? -1 : (Integer) ext.getParam(EffectParams.ep_Land);

                if (!music || land_id > 0) {
                    int id = creature.LayerID;
                    int fx = -1;
                    int fy = -1;
                    NWField f;
                    if (music) {
                        f = GlobalVars.nwrGame.getRndFieldByLands(land_id);
                        if (f != null) {
                            id = f.getLayer().EntryID;
                            fx = f.getCoords().X;
                            fy = f.getCoords().Y;
                        }
                    } else {
                        if (id == GlobalVars.Layer_Midgard) {
                            id = GlobalVars.Layer_Muspelheim;
                        } else {
                            if (id == GlobalVars.Layer_Muspelheim) {
                                id = GlobalVars.Layer_Midgard;
                            }
                        }
                        fx = -1;
                        fy = -1;
                    }

                    NWField old_fld = creature.getCurrentField();
                    creature.transferTo(id, fx, fy, -1, -1, StaticData.MapArea, true, true);

                    f = creature.getCurrentField();

                    if (old_fld.LandID == GlobalVars.Land_Muspelheim && f.LandID != old_fld.LandID) {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouReturnToYourPlaneOfExistence));
                    } else {
                        AuxUtils.exStub("todo: You remain in this realm.");
                    }
                }
            }
        }
    }

    public static void e_Paralysis(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        switch (invokeMode) {
            case im_Use:
            case im_ItSelf:
                createMapObject(effectID, creature.getCurrentField(), creature.getPosX(), creature.getPosY(), true);

                if (creature.hasAffect(EffectID.eid_Paralysis)) {
                    creature.addEffect(EffectID.eid_Paralysis, state, EffectAction.ea_Persistent, true, Locale.getStr(RS.rs_Paralysis_Beg));
                    if (creature.isPlayer()) {
                        GlobalVars.nwrGame.setTurnState(TurnState.gtsSkip);
                    }
                }
                break;

            case im_FinAction:
                if (creature.isPlayer()) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouCanMoveAgain));
                    GlobalVars.nwrGame.setTurnState(TurnState.gtsDone);
                }
                break;
        }
    }

    public static void e_Phase(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode != InvokeMode.im_UseBegin) {
                if (invokeMode != InvokeMode.im_UseEnd) {
                    if (invokeMode == InvokeMode.im_ItSelf) {
                        Effect e = creature.getEffects().findEffectByID(EffectID.eid_Phase);
                        if (e.Source == null) {
                            if (e.Duration == 1) {
                                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouSolidAgain));
                            } else {
                                if (e.Duration < 5) {
                                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_FazingActionFinishes));
                                }
                            }
                        }
                    }
                } else {
                    creature.doneEffect(EffectID.eid_Phase, source);
                }
            } else {
                creature.initEffect(EffectID.eid_Phase, source, EffectAction.ea_Persistent);
            }
        } else {
            creature.addEffect(EffectID.eid_Phase, state, EffectAction.ea_EachTurn, false, Locale.getStr(RS.rs_YouFeelLessSubstantial));
        }
    }

    public static void e_PhaseTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        creature.addEffect(EffectID.eid_PhaseTrap, state, EffectAction.ea_Persistent, true, Locale.getStr(RS.rs_YouTriggerPhaseTrap));
    }

    public static void e_PickAxeUse(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        int dir = (Integer) ext.getParam(EffectParams.ep_Direction);
        int pX = creature.getPosX() + Directions.Data[dir].dX;
        int pY = creature.getPosY() + Directions.Data[dir].dY;
        NWTile tile = (NWTile) creature.getCurrentMap().getTile(pX, pY);
        tile.setFore(PlaceID.pid_Undefined);
    }

    public static void e_PitTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: this is stub");
        String txt;
        if (creature.isPlayer()) {
            txt = Locale.getStr(RS.rs_YouFallIntoPit);
        } else {
            txt = "";
        }
        creature.addEffect(EffectID.eid_PitTrap, state, EffectAction.ea_Persistent, true, txt);
    }

    public static void e_Poisoned(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode == InvokeMode.im_ItSelf) {
                Effect e = creature.getEffects().findEffectByID(EffectID.eid_Poisoned);
                if (e.Source == null && e.Duration == 1) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_VenomReachesYourHeart));
                    creature.death(Locale.getStr(RS.rs_KilledByDeadlyPotion), null);
                }
            }
        } else {
            if (creature.hasAffect(EffectID.eid_Poisoned)) {
                creature.addEffect(EffectID.eid_Poisoned, state, EffectAction.ea_EachTurn, true, Locale.getStr(RS.rs_ThatMayBeToxic));
            }
        }
    }

    public static void e_PoisonSpikeTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.hasAffect(EffectID.eid_PoisonSpikeTrap)) {
            creature.Strength -= AuxUtils.getBoundedRnd(1, 5);
            creature.applyDamage(AuxUtils.getBoundedRnd(15, 30), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_PoisonedSpikesHitYou));
        }
    }

    public static void e_Polymorph(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        PolymorphRay ray = new PolymorphRay();
        ray.Exec(creature, EffectID.eid_Polymorph, invokeMode, ext, EffectsFactory.WandRayMsg(effectID, source));
    }

    public static void e_CheckPrecognition(EffectID effectID, NWCreature creature, Object source, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode == InvokeMode.im_ItSelf) {
            ext.ReqParams = new EffectParams(EffectParams.ep_Item);
        } else {
            ext.ReqParams = new EffectParams();
        }
    }

    public static void e_Precognition(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode == InvokeMode.im_ItSelf) {
                Item item = (Item) ext.getParam(EffectParams.ep_Item);
                item.setIdentified(true);
            }
        } else {
            creature.setSkill(SkillID.Sk_Precognition, creature.getSkill(SkillID.Sk_Precognition) + 1);
            if (creature.getSkill(SkillID.Sk_Precognition) == 1) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveGetAPrecognitionSkill));
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_PowerPrecognitionGrows));
            }
        }
    }

    public static void e_Protection(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_UseBegin:
                creature.initEffect(effectID, source, EffectAction.ea_EachTurn);
                break;

            case im_UseEnd:
                creature.doneEffect(effectID, source);
                break;

            case im_ItSelf:
                if (creature.isPlayer()) {
                    Item item = (Item) source;
                    if (item != null && AuxUtils.chance(item.Bonus)) {
                        int px = creature.getPosX();
                        int py = creature.getPosY();

                        NWField map = creature.getCurrentField();

                        for (int y = py - 3; y <= py + 3; y++) {
                            for (int x = px - 3; x <= px + 3; x++) {
                                if (AuxUtils.distance(px, py, x, y) <= 3) {
                                    NWTile tile = map.getTile(x, y);
                                    if (tile != null && map.isTrap(x, y)) {
                                        tile.Trap_Discovered = true;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    public static void e_PsiBlast(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: Your psionic power needs to be recuperated.");

        NWCreature cr = (NWCreature) ext.getParam(EffectParams.ep_Creature);
        if (cr != null && !cr.equals(creature)) {
            RaceID race = cr.getEntry().Race;
            if ((race == RaceID.crDefault || race == RaceID.crHuman) && cr.hasAffect(EffectID.eid_PsiBlast)) {
                cr.death(Locale.getStr(RS.rs_YouProbeAForeignMind), null);
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelNoResponse));
            }
        } else {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelNoResponse));
        }
    }

    public static void e_PureEvil(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();

        AuxUtils.exStub("todo: messages");
        AuxUtils.exStub("todo: You can no longer see. The air grows cold. -> blind");

        switch (AuxUtils.getBoundedRnd(1, 7)) {
            case 1: {
                int num = creature.getItems().getCount();
                for (int i = 0; i < num; i++) {
                    creature.getItems().getItem(i).State = ItemState.is_Cursed;
                }
                break;
            }

            case 2: {
                int i = 100;
                while (true) {
                    int x = AuxUtils.getRandom(StaticData.FieldWidth);
                    int y = AuxUtils.getRandom(StaticData.FieldHeight);
                    if (!fld.isBarrier(x, y)) {
                        int pid = StaticData.dbTraps[AuxUtils.getRandom(15)].TileID;
                        fld.addTrap(x, y, pid, false);
                        i--;
                        if (i == 0) {
                            break;
                        }
                    }
                }
                break;
            }
            case 3: {
                creature.transferTo(GlobalVars.Layer_Niflheim, -1, -1, -1, -1, StaticData.MapArea, true, false);
                break;
            }
            case 4: {
                int px = creature.getPosX();
                int py = creature.getPosY();

                for (int y = py - 1; y <= py + 1; y++) {
                    for (int x = px - 1; x <= px + 1; x++) {
                        if (y != py && x != px && AuxUtils.chance(45)) {
                            fld.addCreature(x, y, GlobalVars.nwrBase.findEntryBySign("Migdnart").GUID);
                        }
                    }
                }
                break;
            }
            case 5: {
                UniverseBuilder.gen_Creatures(fld, 100);
                break;
            }
            case 6: {
                if (creature.isPlayer()) {
                    ((Player) creature).Morality = (byte) ((int) ((Player) creature).Morality - 15);
                }
                creature.addEffect(EffectID.eid_Blindness, ItemState.is_Normal, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_YouAreBlinding));
                break;
            }
            case 7: {
                int cnt = AuxUtils.getBoundedRnd(5, 37);
                for (int i = 1; i <= cnt; i++) {
                    fld.addCreature(-1, -1, GlobalVars.nwrBase.findEntryBySign("Denizen").GUID);
                }
                break;
            }
        }
    }

    public static void e_QuicksandTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: на каком этапе это сообщение?");
        AuxUtils.exStub("todo: //nwrWin.ShowText(creature, rsList('Drowned in quicksand']);");

        NWField fld = creature.getCurrentField();
        NWTile tile = fld.getTile(creature.getPosX(), creature.getPosY());
        if (tile.getForeBase() == PlaceID.pid_QuicksandTrap) {
            tile.setBack(PlaceID.pid_Quicksand);
            fld.normalize();
        }
        creature.addEffect(EffectID.eid_Quicksand, state, EffectAction.ea_Persistent, true, Locale.getStr(RS.rs_YouSinkInQuicksand));
    }

    public static void e_RaiseMagic(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        createMapObject(effectID, creature.getCurrentField(), creature.getPosX(), creature.getPosY(), true);
        
        int cnt = 0;
        switch (state) {
            case is_Normal:
                cnt = 1;
                break;
            case is_Blessed:
                cnt = 2;
                break;
            case is_Cursed:
                cnt = -1;
                break;
        }
        
        creature.MPMax += cnt;
        if (cnt > 0) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourMagicAbilityIncreases));
        } else {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourMagicAbilityDecreases));
        }
    }

    public static void e_Recall(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (creature.isPlayer()) {
            NWField fld = creature.getCurrentField();
            if (fld.EntryID == GlobalVars.Field_Bazaar) {
                creature.death(Locale.format(RS.rs_BazaarMagicShieldDeath, new Object[]{creature.getName()}), null);
            } else {
                RecallPos rp = (RecallPos) ((Player) creature).getMemory().find("RecallPos");
                if (rp != null) {
                    creature.transferTo(rp.Layer, rp.Field.X, rp.Field.Y, rp.Pos.X, rp.Pos.Y, StaticData.MapArea, true, true);
                } else {
                    creature.transferTo(GlobalVars.Layer_Midgard, 2, 2, -1, -1, StaticData.MapArea, true, true);
                }
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourWorldSpins));
            }
        }
    }

    public static void e_Recharging(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok

        AuxUtils.exStub("todo: question msg: rs_EmpowerWhich");
        AuxUtils.exStub("todo:  + disruption horn");
        AuxUtils.exStub("todo:  + rs_ScrollHasNoApparentEffect");

        Item aExtItem = (Item) ext.getParam(EffectParams.ep_Item);

        if (creature.isConfused()) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_WandsDrained));

            int num = creature.getItems().getCount();
            for (int i = 0; i < num; i++) {
                Item item = creature.getItems().getItem(i);
                if (item.getKind() == ItemKind.ik_Wand || (item.getEntry().Sign.equals("BarbedWhip")) || (item.getEntry().Sign.equals("WoodenFlute"))) {
                    item.Bonus = 0;
                }
            }
        } else {
            if (aExtItem.getKind() == ItemKind.ik_Wand) {
                if (aExtItem.getEntry().Sign.compareTo("Wand_Wishing") == 0) {
                    aExtItem.Bonus++;
                } else {
                    aExtItem.Bonus += AuxUtils.getBoundedRnd(5, 15);
                }
            } else {
                if (aExtItem.getEntry().Sign.compareTo("BarbedWhip") == 0 || aExtItem.getEntry().Sign.compareTo("WoodenFlute") == 0) {
                    aExtItem.Bonus = AuxUtils.getBoundedRnd(7, 15);
                }
            }

            GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_WandPulsates, new Object[]{aExtItem.getDeclinableName(Number.nSingle, Case.cNominative, creature.isBlindness())}));
        }
    }

    public static void e_Reflect(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouNowReflect));
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_SpellAlreadyProtects));
    }

    public static void e_Regeneration(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // -> NWCreature.Recovery()

        switch (invokeMode) {
            case im_UseBegin:
                creature.initEffect(effectID, source, EffectAction.ea_Persistent);
                break;
            case im_UseEnd:
                creature.doneEffect(effectID, source);
                break;
        }
    }

    public static void e_Rejuvenation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: messages");
        AuxUtils.exStub("todo: if all ok then Nothng seems to happen");

        if (creature.getEffects().findEffectByID(EffectID.eid_Withered) != null) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouExhaustedNoRejuvenation));
        } else {
            switch (state) {
                case is_Blessed:
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourLiverTingles));
                    creature.setAbility(AbilityID.Resist_Poison, 100);
                    EffectsFactory.CureEffect(creature, EffectID.eid_Poisoned, Locale.getStr(RS.rs_VenomIsNeutralized));
                    break;

                case is_Cursed:
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourLiverAches));
                    creature.setAbility(AbilityID.Resist_Poison, 0);
                    break;
            }

            if (state != ItemState.is_Cursed && creature.HPCur != creature.HPMax) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourLostStrengthIsRestored));
                // todo: original - strength
                creature.HPCur = creature.HPMax;
            }
        }
    }

    public static void e_Relocation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use: {
                if (creature.hasAffect(EffectID.eid_Relocation)) {
                    NWField fld = creature.getCurrentField();
                    if (source instanceof BaseTile && (int) ((BaseTile) source).Foreground == PlaceID.pid_TeleportTrap) {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouSetOffTeleportationTrap));
                    }

                    Point pt = null;
                    boolean res = (ext == null);
                    if (!res) {
                        pt = (Point) ext.getParam(EffectParams.ep_Place);
                        res = (pt == null);
                    }

                    if (res) {
                        pt = creature.searchRndLocation(fld, fld.getAreaRect());
                    }

                    creature.checkTile(false);
                    creature.setPos(pt.X, pt.Y);
                    creature.checkTile(true);

                    if (creature.isPlayer()) {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveTeleported));
                    }
                }
                break;
            }

            case im_UseBegin:
                creature.initEffect(effectID, source, EffectAction.ea_RandomTurn);
                break;

            case im_UseEnd:
                creature.doneEffect(effectID, source);
                break;
        }
    }

    public static void e_Restoration(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.isPlayer()) {
            if (creature.isPlayer() && creature.CLSID == GlobalVars.cid_Werewolf) {
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_WerebloodDissipates));
                AuxUtils.exStub("todo:  ");
            }

            SourceForm sf = (SourceForm) ((Player) creature).getMemory().find("SourceForm");

            creature.setCLSID(sf.sfID);

            AuxUtils.exStub("todo:  messages");
            AuxUtils.exStub("todo: rs_OldSelfAgain");
            AuxUtils.exStub("todo: rs_ReturnOriginalForm");
            AuxUtils.exStub("todo: rs_HumansAreExtinct");
            AuxUtils.exStub("todo: rs_TurnedIntoMass");

            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.format(RS.rs_RestoreForm, new Object[]{creature.getName()}));
        }
    }

    public static void e_RestoreMagic(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (creature.MPCur == creature.MPMax) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NothingHappens));
        } else {
            creature.MPCur = creature.MPMax;
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouAreRestoredYourMana));
        }
    }

    public static void e_RunicDivination(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        GlobalVars.nwrWin.showDivination();
    }

    public static void e_Prayer(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        Object obj = ext.getParam(EffectParams.ep_GodID);
        int deityID = (Integer) obj;

        CreatureEntry deity = (CreatureEntry) GlobalVars.nwrBase.getEntry(deityID);
        if (deity != null) {
            GlobalVars.nwrWin.showTextRes(creature, RS.rs_PraysTo, new Object[]{creature, deity});

            ((Player) creature).getFaith().worship(deityID, Faith.WK_PRAYER, null);
        }
    }

    public static void e_Sacrifice(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        Object obj = ext.getParam(EffectParams.ep_GodID);
        int deityID = (Integer) obj;

        CreatureEntry deity = (CreatureEntry) GlobalVars.nwrBase.getEntry(deityID);
        if (deity != null) {
            Item item = (Item) ext.getParam(EffectParams.ep_Item);
            GlobalVars.nwrWin.showTextRes(creature, RS.rs_ItemSacrificed, new Object[]{item, deity});

            ((Player) creature).getFaith().worship(deityID, Faith.WK_SACRIFICE, item);
        }
    }

    public static void e_Scaling(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        int cnt = 0;
        switch (state) {
            case is_Normal:
                cnt = 1;
                break;
            case is_Blessed:
                cnt = 2;
                break;
            case is_Cursed:
                cnt = -1;
                break;
        }

        creature.ArmorClass += cnt;

        if (creature.isPlayer()) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouSkinFeelsPinching));
            if (cnt > 0) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouBecameStronger));
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouBecameLessArmored));
            }
        }
    }

    public static void e_SetRecall(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.isPlayer()) {
            NWField fld = creature.getCurrentField();
            if (fld.LandID == GlobalVars.Land_Jotenheim) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_SpellFails));
            }
            ((Player) creature).setRecallPos();
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveMakeAPointOfReturn));
        }
    }

    private static void loadShip(Player player)
    {
        try {
            Item ship = player.findItem("Skidbladnir");
            LeaderBrain party = (LeaderBrain) player.getBrain();

            for (int i = 1; i < party.getMembersCount(); i++) {
                NWCreature member = (NWCreature) party.getMember(i);
                member.addEffect(EffectID.eid_Sail, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                member.checkTile(false);
                member.leaveField();

                ship.getContents().add(member);
            }
        } catch (Exception ex) {
            Logger.write("EffectsFactory.loadShip(): " + ex.getMessage());
            throw ex;
        }
    }

    public static void unloadShip(Player player)
    {
        try {
            Item ship = player.findItem("Skidbladnir");
            LeaderBrain party = (LeaderBrain) player.getBrain();
            //NWField field = player.getCurrentField();

            for (int i = 1; i < party.getMembersCount(); i++) {
                NWCreature member = (NWCreature) party.getMember(i);
                ship.getContents().extract(member);

                Effect ef = member.getEffects().findEffectByID(EffectID.eid_Sail);
                member.getEffects().remove(ef);

                Point pt = member.getNearestPlace(player.getLocation(), 2, true);
                if (pt != null) {
                    member.transferTo(player.LayerID, player.getField().X, player.getField().Y, pt.X, pt.Y, StaticData.MapArea, true, true);
                } else {
                    Logger.write("EffectsFactory.unloadShip().getNearestPlace() failed");
                }
            }
        } catch (Exception ex) {
            Logger.write("EffectsFactory.unloadShip(): " + ex.getMessage());
            throw ex;
        }
    }
    
    public static void e_Skidbladnir(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.isPlayer()) {
            int dir = (Integer) ext.getParam(EffectParams.ep_Direction);
            int nx = creature.getPosX() + Directions.Data[dir].dX;
            int ny = creature.getPosY() + Directions.Data[dir].dY;
            NWTile tile = (NWTile) creature.getCurrentMap().getTile(nx, ny);
            if (tile.getBackBase() != PlaceID.pid_Water) {
                AuxUtils.exStub("todo: check message");
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NeedLaunchOntoWater));
            } else {
                loadShip((Player) creature);
                creature.addEffect(EffectID.eid_Sail, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                creature.moveTo(nx, ny);
            }
        }
    }

    public static void e_SlaveUse(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("if not(HasAffect(eid_SlaveUse, False))");
        AuxUtils.exStub("then nwrWin.ShowText(creature, rsList(rs_SlaveUseFail]);");
    }

    public static void e_Sleep(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: fix to ray");
        AuxUtils.exStub("todo: msg: '%s falls asleep.");

        switch (invokeMode) {
            case im_Use:
            case im_ItSelf:
                if (creature.hasAffect(EffectID.eid_Sleep)) {
                    creature.addEffect(EffectID.eid_Sleep, state, EffectAction.ea_Persistent, true, Locale.getStr(RS.rs_Sleep_Beg));
                    if (creature.isPlayer()) {
                        GlobalVars.nwrGame.setTurnState(TurnState.gtsSkip);
                    }
                }
                break;

            case im_FinAction:
                if (creature.isPlayer()) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouCanMoveAgain));
                    GlobalVars.nwrGame.setTurnState(TurnState.gtsDone);
                }
                break;
        }
    }

    public static void e_SoulSeeking(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        AuxUtils.exStub("@deprecated: You sense no life in this area.");
        creature.addEffect(EffectID.eid_SoulSeeking, state, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_SoulSeekingWillShowYouAllCreatures));
    }

    public static void e_SoulTrapping(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_SpeedDown(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Speedup(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok

        switch (invokeMode) {
            case im_Use:
            case im_ItSelf:
                creature.addEffect(EffectID.eid_Speedup, state, EffectAction.ea_Persistent, false, "");

                int speed = creature.getSpeed();
                if (speed < 50) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouStayFaster));
                } else {
                    if (speed < 70) {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_DangerouslyFast));
                    } else {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_HeartBursts));
                        creature.death(Locale.getStr(RS.rs_KilledByAcceleration), null);
                    }
                }
                break;

            case im_UseBegin:
                creature.initEffect(EffectID.eid_Speedup, source, EffectAction.ea_Persistent);
                break;

            case im_UseEnd:
                creature.doneEffect(EffectID.eid_Speedup, source);
                break;
        }
    }

    public static void e_Stoning(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        StoningRay ray = new StoningRay();
        ray.Exec(creature, EffectID.eid_Stoning, invokeMode, ext, EffectsFactory.WandRayMsg(EffectID.eid_Stoning, source));
    }

    public static void e_Strength(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        if (state != ItemState.is_Normal) {
            if (state != ItemState.is_Blessed) {
                if (state == ItemState.is_Cursed) {
                    creature.Strength--;
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsAWeakness));
                }
            } else {
                creature.Strength += AuxUtils.getBoundedRnd(1, 4);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelMuchStronger));
            }
        } else {
            creature.Strength++;
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelsAPower));
        }
    }

    public static void e_StrengthReduce(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        AuxUtils.exStub("yellow mushroom");
        EffectsFactory.e_Strength(EffectID.eid_Strength, creature, source, ItemState.is_Cursed, InvokeMode.im_Use, null);
    }

    public static void e_StunGasTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.hasAffect(EffectID.eid_StunGasTrap)) {
            creature.addEffect(EffectID.eid_SpeedDown, state, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_StunGasEntrapped));
        }
    }

    public static void e_Summoning(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        NWField field = creature.getCurrentField();

        if (creature.isConfused()) {
            if (AuxUtils.chance(93)) {
                for (int i = field.getCreatures().getCount() - 1; i >= 0; i--) {
                    NWCreature cr = field.getCreatures().getItem(i);
                    RaceID race = cr.getEntry().Race;
                    if ((race == RaceID.crDefault || race == RaceID.crHuman) && !cr.equals(creature)) {
                        int id = GlobalVars.dbLayers.get(AuxUtils.getBoundedRnd(0, GlobalVars.dbLayers.getCount() - 1));
                        NWLayer tempLayer = GlobalVars.nwrGame.getLayer(id);

                        int x = AuxUtils.getBoundedRnd(0, tempLayer.getW() - 1);
                        int y = AuxUtils.getBoundedRnd(0, tempLayer.getH() - 1);

                        cr.transferTo(id, x, y, -1, -1, StaticData.MapArea, true, false);
                    }
                }

                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_AlcoRecallCausedACollapse));
            } else {
                int x = AuxUtils.getBoundedRnd(creature.getPosX() - 5, creature.getPosX() + 5);
                int y = AuxUtils.getBoundedRnd(creature.getPosY() - 5, creature.getPosY() + 5);
                field.addCreature(x, y, GlobalVars.nwrBase.findEntryBySign("GreyTerror").GUID);
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourHairStandsOnEnd));
            }
        } else {
            String temp = "";
            switch (AuxUtils.getBoundedRnd(1, 5)) {
                case 1:
                    temp = "Rashok";
                    break;
                case 2:
                    temp = "Hreset";
                    break;
                case 3:
                    temp = "LiKrin";
                    break;
                case 4:
                    temp = "Elcich";
                    break;
                case 5:
                    temp = "Bartan";
                    break;
            }

            int x = AuxUtils.getBoundedRnd(creature.getPosX() - 5, creature.getPosX() + 5);
            int y = AuxUtils.getBoundedRnd(creature.getPosY() - 5, creature.getPosY() + 5);
            field.addCreature(x, y, GlobalVars.nwrBase.findEntryBySign(temp).GUID);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveGetAReinforcement));
        }
    }

    public static void e_SwitchBodies(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.isPlayer()) {
            EntityList cs = creature.getCurrentField().getCreatures();

            if (cs.getCount() == 1) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NoCreatureExists));
            } else {
                NWCreature weak = null;
                NWCreature strong = null;
                float wf = 0f;
                float sf = 1f;

                int num = cs.getCount();
                for (int i = 0; i < num; i++) {
                    NWCreature c = (NWCreature) cs.getItem(i);

                    RaceID race = c.getEntry().Race;
                    if (race == RaceID.crDefault || race == RaceID.crHuman) {
                        float f = c.getAttackRate(creature, 0);
                        if (sf > f) {
                            sf = f;
                            strong = c;
                        } else {
                            if (wf < f) {
                                wf = f;
                                weak = c;
                            }
                        }
                    }
                }

                NWCreature target = null;
                if (creature.equals(weak)) {
                    weak = null;
                }
                if (creature.equals(strong)) {
                    strong = null;
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouAreMostPowerful));
                }

                if (creature.isConfused() && weak != null) {
                    target = weak;
                } else {
                    if (strong != null) {
                        target = strong;
                    }
                }

                if (target != null) {
                    Point tPos = target.getLocation();
                    creature.switchBody(target);
                    target.checkTile(false);
                    cs.remove(target);
                    creature.checkTile(false);
                    creature.setPos(tPos.X, tPos.Y);
                    creature.checkTile(true);
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouSwitchBodies));
                }
            }
        }
    }

    public static void e_SwitchDimension(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();

        if (invokeMode != InvokeMode.im_Use) {
            if (invokeMode == InvokeMode.im_ItSelf) {
                creature.transferTo(GlobalVars.Layer_Crossroads, 0, 0, -1, -1, StaticData.MapArea, true, true);
                fld.research(false, TileStates.include(0, TileStates.TS_SEEN, TileStates.TS_VISITED));
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouCameOnCrossroads));
            }
        } else {
            creature.setSkill(SkillID.Sk_DimensionTravel, creature.getSkill(SkillID.Sk_DimensionTravel) + 1);
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouGetDimTravelAbility));
        }
    }

    public static void e_Swoon(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use:
            case im_ItSelf:
                creature.addEffect(EffectID.eid_Swoon, ItemState.is_Normal, EffectAction.ea_Persistent, true, Locale.getStr(RS.rs_YouFaintFromHunger));
                GlobalVars.nwrGame.setTurnState(TurnState.gtsSkip);
                break;

            case im_FinAction:
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouCanMoveAgain));
                GlobalVars.nwrGame.setTurnState(TurnState.gtsDone);
                break;
        }
    }

    public static void e_Taming(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWCreature cr = (NWCreature) ext.getParam(EffectParams.ep_Creature);
        if (cr != null && !cr.equals(creature)) {
            RaceID race = cr.getEntry().Race;
            if ((race == RaceID.crDefault || race == RaceID.crHuman) && (!cr.getEntry().Flags.contains(CreatureFlags.esPlant))) {
                cr.addEffect(EffectID.eid_Obedience, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouCharmTheMonster));
            } else {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NothingHappens));
            }
        } else {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_NothingHappens));
        }
    }

    public static void e_TerrainLife(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo:  not joined!!!");
        AuxUtils.exStub("todo: rp_terrainlife");
        AuxUtils.exStub("todo: 'rock'");
        AuxUtils.exStub("todo: 'watery form'");
        AuxUtils.exStub("todo: 'sand form'");
        AuxUtils.exStub("todo: 'mud floe'");
        AuxUtils.exStub("todo: 'lava flow'");
        AuxUtils.exStub("todo: 'tree'");
        AuxUtils.exStub("todo: 'wall'");
    }

    public static void e_ThirdSight(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode != InvokeMode.im_UseBegin) {
            if (invokeMode == InvokeMode.im_UseEnd) {
                creature.doneEffect(effectID, source);
            }
        } else {
            creature.initEffect(effectID, source, EffectAction.ea_Persistent);
        }
    }

    public static void e_HurtleThroughTime(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use:
            case im_ItSelf:
                AuxUtils.exStub("prepare in e_TimeStop()");
                break;

            case im_FinAction:
                if (creature.isPlayer()) {
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouStepIntoFlowOfTime));
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouCanMoveAgain));
                    GlobalVars.nwrGame.setTurnState(TurnState.gtsDone);
                }
                break;
        }
    }

    public static void e_TimeStop(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();

        switch (invokeMode) {
            case im_Use:
                if (creature.isPlayer() && fld.LandID != GlobalVars.Land_Bazaar && fld.LandID != GlobalVars.Land_MimerWell) {
                    if (fld.LandID == GlobalVars.Land_MimerRealm || fld.LandID == GlobalVars.Land_Niflheim || fld.LandID == GlobalVars.Land_Jotenheim || fld.LandID == GlobalVars.Land_Nidavellir || fld.LandID == GlobalVars.Land_Armory || fld.LandID == GlobalVars.Land_Vigrid || fld.LandID == GlobalVars.Land_Valhalla || fld.LandID == GlobalVars.Land_GodsFortress || fld.LandID == GlobalVars.Land_Ocean || fld.LandID == GlobalVars.Land_Bifrost || fld.LandID == GlobalVars.Land_GiollRiver) {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouOpenedMagicGate));

                        int id = AuxUtils.getRandom(5);
                        switch (id) {
                            case 0:
                                id = GlobalVars.nwrBase.findEntryBySign("Migdnart").GUID;
                                break;
                            case 1:
                                id = GlobalVars.nwrBase.findEntryBySign("Minion").GUID;
                                break;
                            case 2:
                                id = GlobalVars.nwrBase.findEntryBySign("PyrtaAth").GUID;
                                break;
                            case 3:
                                id = GlobalVars.nwrBase.findEntryBySign("TimeMaster").GUID;
                                break;
                            case 4:
                                id = GlobalVars.nwrBase.findEntryBySign("Draugr").GUID;
                                break;
                        }
                        int cnt = AuxUtils.getBoundedRnd(5, 17);
                        int px = creature.getPosX();
                        int py = creature.getPosY();

                        for (int idx = 1; idx <= cnt; idx++) {
                            int x = AuxUtils.getBoundedRnd(px - 5, px - 5);
                            int y = AuxUtils.getBoundedRnd(py - 5, py - 5);
                            if (!fld.isBarrier(x, y)) {
                                fld.addCreature(x, y, id);
                            }
                        }
                    }

                    if (creature.isConfused()) {
                        int cnt = AuxUtils.getBoundedRnd(1000, 10000);
                        creature.addEffect(EffectID.eid_HurtleThroughTime, state, EffectAction.ea_Persistent, cnt, Locale.getStr(RS.rs_YouHurtleThroughTime));
                        if (creature.isPlayer()) {
                            GlobalVars.nwrGame.setTurnState(TurnState.gtsSkip);
                        }
                    } else {
                        int cnt = 0;
                        switch (state) {
                            case is_Normal:
                            case is_Cursed:
                                cnt = AuxUtils.getBoundedRnd(10, 30);
                                break;
                            case is_Blessed:
                                cnt = AuxUtils.getBoundedRnd(100, 126);
                                break;
                        }

                        switch (state) {
                            case is_Normal:
                            case is_Blessed: {
                                String msg;
                                if (AuxUtils.getRandom(2) == 0) {
                                    msg = Locale.getStr(RS.rs_TimeIsStopInUniverse); // my
                                } else {
                                    msg = Locale.getStr(RS.rs_YouAreOutsideFlowOfTime); // original
                                }

                                Effect effect = new Effect(creature.getSpace(), creature, EffectID.eid_TimeStop, null, EffectAction.ea_Persistent, cnt, 0);
                                creature.getEffects().Add(effect);
                                GlobalVars.nwrWin.showText(creature, msg);
                            }
                            break;

                            case is_Cursed: {
                                String msg;
                                if (AuxUtils.getRandom(2) == 0) {
                                    msg = Locale.getStr(RS.rs_TimeIsStopForYou); // my
                                } else {
                                    msg = Locale.getStr(RS.rs_TimeWhizzes); // original
                                }

                                creature.addEffect(EffectID.eid_HurtleThroughTime, state, EffectAction.ea_Persistent, cnt, msg);
                                if (creature.isPlayer()) {
                                    GlobalVars.nwrGame.setTurnState(TurnState.gtsSkip);
                                }
                            }
                            break;
                        }
                    }
                }
                break;

            case im_ItSelf:
                break;

            case im_FinAction:
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouStepIntoFlowOfTime));
                break;
        }
    }

    public static void e_Transformation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok

        NWField fld = creature.getCurrentField();
        int id;
        if (ext != null) {
            Object obj = ext.getParam(EffectParams.ep_MonsterID);
            id = (Integer) obj;
        } else {
            ArrayList<Integer> validCreatures = fld.ValidCreatures;
            int cnt = (validCreatures != null) ? validCreatures.size() : 0;
            if (cnt < 1) {
                return;
            }
            int i = AuxUtils.getRandom(cnt);
            id = fld.ValidCreatures.get(i);
        }

        creature.initEx(id, false, false);

        if (!creature.getEntry().Flags.contains(CreatureFlags.esUseItems)) {
            creature.dropAll();
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourArmorFallsOff));
        }

        AuxUtils.exStub("todo:  messages");
        AuxUtils.exStub("todo: Your amulet falls off.");
        AuxUtils.exStub("todo: Your armor strains and bursts.");
        GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_YouShallNowBeKnownAsXX, new Object[]{creature.getName(), creature.getRace()}));
    }

    public static void e_Transmutation(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        TransmutationRay ray = new TransmutationRay();
        try {
            ray.Exec(creature, EffectID.eid_Transmutation, invokeMode, ext, EffectsFactory.WandRayMsg(EffectID.eid_Transmutation, source));
        } finally {
            creature.getCurrentField().normalize();
        }
    }

    public static void e_Transport(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok
        NWField fld = creature.getCurrentField();

        if (creature.isPlayer()) {
            if (creature.LayerID == GlobalVars.Layer_Svartalfheim3) {
                creature.transferTo(GlobalVars.Layer_Midgard, 2, 2, -1, -1, StaticData.MapArea, true, true);
            } else {
                creature.transferTo(GlobalVars.Layer_Svartalfheim3, 2, 2, -1, -1, StaticData.MapArea, true, true);
                fld.research(false, TileStates.include(0, TileStates.TS_SEEN, TileStates.TS_VISITED));
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourWorldSpins));
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_SignReads));
            }
        }
    }

    public static void e_TrapDetection(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.isPlayer()) {
            NWField field = creature.getCurrentField();

            int num = field.getHeight();
            for (int y = 0; y < num; y++) {
                int num2 = field.getWidth();
                for (int x = 0; x < num2; x++) {
                    NWTile tile = field.getTile(x, y);

                    if (field.isTrap(x, y)) {
                        tile.includeState(TileStates.TS_VISITED);
                        tile.Trap_Discovered = true;
                    } else {
                        Gate gate = field.findGate(x, y);
                        if (gate != null) {
                            tile.includeState(TileStates.TS_VISITED);
                        }
                    }
                }
            }

            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouKnowAboutAllTrapsAndPortals));
        }
    }

    public static void e_TrapGeneration(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok

        NWField fld = creature.getCurrentField();

        if (creature.isConfused()) {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    if (fld.isTrap(x, y)) {
                        NWTile tile = fld.getTile(x, y);
                        tile.setFore(PlaceID.pid_Undefined);
                    }
                }
            }
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelSenseOfEase));
        } else {
            int px = creature.getPosX();
            int py = creature.getPosY();

            for (int y = py - 2; y <= py + 2; y++) {
                for (int x = px - 2; x <= px + 2; x++) {
                    if (x != px && y != py && AuxUtils.chance(40)) {
                        int id = StaticData.dbTraps[AuxUtils.getRandom(15)].TileID;
                        fld.addTrap(x, y, id, true);
                    }
                }
            }
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouCreateTraps));
        }
    }

    public static void e_Traveling(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.isPlayer()) {
            if (creature.isConfused() || state == ItemState.is_Cursed) {
                int i = AuxUtils.getRandom(GlobalVars.dbLayers.getCount());
                creature.transferTo(GlobalVars.dbLayers.get(i), -1, -1, -1, -1, StaticData.MapArea, true, false);
            } else {
                if (state != ItemState.is_Normal) {
                    if (state == ItemState.is_Blessed) {
                        EffectsFactory.e_SwitchDimension(EffectID.eid_SwitchDimension, creature, source, state, InvokeMode.im_ItSelf, ext);
                    }
                } else {
                    creature.transferTo(creature.LayerID, creature.getField().X, creature.getField().Y, -1, -1, StaticData.MapArea, true, true);
                }
            }
        }
    }

    public static void e_Tunneling(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        TunnelingRay ray = new TunnelingRay();
        try {
            ray.Exec(creature, EffectID.eid_Tunneling, invokeMode, ext, EffectsFactory.WandRayMsg(EffectID.eid_Tunneling, source));
        } finally {
            creature.getCurrentField().normalize();
        }
    }

    public static void e_TwelveGates(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        // check-ok

        NWField fld = creature.getCurrentField();
        int px = creature.getPosX();
        int py = creature.getPosY();

        if (creature.isConfused()) {
            int num = AuxUtils.getBoundedRnd(2, 4);
            for (int i = 1; i <= num; i++) {
                while (true) {
                    int x = AuxUtils.getBoundedRnd(px - 2, px + 2);
                    int y = AuxUtils.getBoundedRnd(py - 2, py + 2);

                    if (!fld.isBarrier(x, y) && (px != x || py != y)) {
                        Item item = fld.addItem(x, y, GlobalVars.iid_Coin);
                        item.Count = (short) AuxUtils.getBoundedRnd(20, 115);
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_GoldAppears));
                        String temp;
                        if (fld.EntryID == GlobalVars.Field_Bazaar) {
                            temp = "BlueWisp";
                        } else {
                            temp = "Wier";
                        }
                        fld.addCreature(x, y, GlobalVars.nwrBase.findEntryBySign(temp).GUID);

                        break;
                    }
                }
            }
        } else {
            int num2 = AuxUtils.getBoundedRnd(2, 6);
            for (int i = 1; i <= num2; i++) {
                while (true) {
                    int x = AuxUtils.getBoundedRnd(px - 2, px + 2);
                    int y = AuxUtils.getBoundedRnd(py - 2, py + 2);

                    if (!fld.isBarrier(x, y) && (px != x || py != y)) {
                        UniverseBuilder.gen_Creature(fld, -1, x, y, true);
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_GatesOpen));

                        break;
                    }
                }
            }
        }
    }

    public static void e_Ventriloquism(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField f = creature.getCurrentField();

        int cx = creature.getPosX();
        int cy = creature.getPosY();

        for (int y = cy - 5; y <= cy + 5; y++) {
            for (int x = cx - 5; x <= cx + 5; x++) {
                if (f.isValid(x, y)) {
                    NWCreature cr = (NWCreature) f.findCreature(x, y);
                    if (cr != null && !cr.equals(creature)) {
                        RaceID race = cr.getEntry().Race;
                        if (race == RaceID.crDefault || race == RaceID.crHuman) {
                            cr.addEffect(EffectID.eid_Ventriloquism, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                        }
                    }
                }
            }
        }

        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThrowVoice));
    }

    public static void e_Vertigo(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Wands(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        int num = AuxUtils.getRandom(2);
        if (num != 0) {
            if (num == 1) {
                AuxUtils.exStub("todo: !!!");
                int i = AuxUtils.getBoundedRnd(0, GlobalVars.dbWands.getCount() - 1);
                ItemEntry entry = (ItemEntry) GlobalVars.nwrBase.getEntry(i);
                EffectID eff = entry.Effects[0].EffID;
                Effect.invokeEffect(eff, creature, source, InvokeMode.im_Use, EffectAction.ea_Instant, ext);
            }
        } else {
            NWField f = creature.getCurrentField();

            for (int y = creature.getPosY() - 3; y <= creature.getPosY() + 3; y++) {
                for (int x = creature.getPosX() - 3; x <= creature.getPosX() + 3; x++) {
                    NWTile tile = f.getTile(x, y);
                    if (tile != null) {
                        int fg = tile.getForeBase();
                        if (fg != PlaceID.pid_Tree) {
                            if (fg == PlaceID.pid_DeadTree) {
                                tile.setFore(PlaceID.pid_Undefined);
                            }
                        } else {
                            tile.setFore(PlaceID.pid_DeadTree);
                        }

                        NWCreature cr = (NWCreature) f.findCreature(x, y);
                        if (!cr.equals(creature)) {
                            int num4 = AuxUtils.getRandom(4);
                            if (num4 != 0) {
                                if (num4 != 1) {
                                    if (num4 != 2) {
                                        if (num4 == 3) {
                                            cr.addEffect(EffectID.eid_LegsMissing, ItemState.is_Normal, EffectAction.ea_Persistent, false, Locale.getStr(RS.rs_LegsGone));
                                        }
                                    } else {
                                        RaceID race = cr.getEntry().Race;
                                        if (race == RaceID.crDefault || race == RaceID.crHuman) {
                                            cr.setCLSID(GlobalVars.nwrBase.findEntryBySign("Rat").GUID);
                                        }
                                    }
                                } else {
                                    EffectsFactory.e_Relocation(EffectID.eid_Relocation, cr, source, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                                }
                            } else {
                                EffectsFactory.e_Insanity(EffectID.eid_Insanity, cr, source, ItemState.is_Normal, InvokeMode.im_Use, null);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void e_DisruptionHornUse(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();
        AuxUtils.exStub("Great thunder rumbles.");
        AuxUtils.exStub("Your horn explodes! ");
        AuxUtils.exStub("Your horn is nearly split.");
        AuxUtils.exStub("Your horn begins to crack.");
        AuxUtils.exStub("The walls and floor are too tough.");

        DungeonRoom dunRoom = creature.findDungeonRoom();
        if (dunRoom != null) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_CeilingCracks));
            int turns = Effect.getDuration(EffectID.eid_CrushRoof, ItemState.is_Normal, false);
            EffectExt newExt = new EffectExt();
            newExt.setParam(EffectParams.ep_DunRoom, dunRoom);
            MapEffect eff = new MapEffect(creature.getSpace(), fld, EffectID.eid_CrushRoof, null, EffectAction.ea_LastTurn, turns, 0);
            eff.Ext = newExt;
            
            AbstractMap map = (AbstractMap) dunRoom.Owner;
            if (map instanceof NWField) {
                ((NWField) map).getEffects().Add(eff);
            } else {
                if (map instanceof NWLayer) {
                    ((NWLayer) map).getEffects().Add(eff);
                }
            }
        }
    }

    public static void e_WaterTrap(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        DungeonRoom dunRoom = creature.findDungeonRoom();
        if (dunRoom != null) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_FloorBeneathYouSinks));

            EffectExt newExt = new EffectExt();
            newExt.setParam(EffectParams.ep_DunRoom, dunRoom);

            AbstractMap map = (AbstractMap) dunRoom.Owner;

            MapEffect eff = new MapEffect(creature.getSpace(), map, EffectID.eid_Flood, null, EffectAction.ea_EachTurn, 20, 0);
            eff.Ext = newExt;

            if (map instanceof NWField) {
                ((NWField) map).getEffects().Add(eff);
            } else {
                if (map instanceof NWLayer) {
                    ((NWLayer) map).getEffects().Add(eff);
                }
            }
        }
    }

    public static void e_WeirdFume_Skill(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWCreature enemy = (NWCreature) ext.getParam(EffectParams.ep_Creature);

        GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_WeirdFumeEnvelops));
        switch (AuxUtils.getRandom(8)) {
            case 0:
                enemy.getCurrentField().close(true);
                GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_MindIsRearranged));
                break;

            case 1:
                EffectsFactory.SimpleAttack(enemy, EffectID.eid_WeirdFume_Acid, DamageKind.dkPhysical, "");
                GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_AcidDropletsFall));
                break;

            case 2:
                if (enemy.getBody() != null) {
                    enemy.getBody().addPart(BodypartType.bp_Finger.getValue());
                    GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_FingerGrow));
                }
                break;

            case 3:
                if (enemy.getBody() != null) {
                    enemy.getBody().addPart(BodypartType.bp_Eye.getValue());
                    GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_EyeGrow));
                }
                break;

            case 4: {
                CreatureSex sex = enemy.Sex;
                if (sex != CreatureSex.csFemale && sex != CreatureSex.csMale) {
                    GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_Unchanged));
                } else {
                    CreatureSex sex2 = enemy.Sex;
                    if (sex2 != CreatureSex.csFemale) {
                        if (sex2 == CreatureSex.csMale) {
                            enemy.Sex = CreatureSex.csFemale;
                        }
                    } else {
                        enemy.Sex = CreatureSex.csMale;
                    }
                    GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_GenderChange));
                }
                break;
            }
            case 5: {
                GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_Unchanged));
                break;
            }
            case 6: {
                GlobalVars.nwrWin.showText(enemy, Locale.getStr(RS.rs_BrainThrobs));
                AuxUtils.exStub("todo: vertiginous");
                break;
            }
            case 7: {
                AuxUtils.exStub("todo: //nwrWin.ShowText(enemy, rsList('You are dazed.']);");
                AuxUtils.exStub("todo: -> confusion");
                break;
            }
        }
    }

    public static void e_Winds(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        AuxUtils.exStub("todo: ???");
    }

    public static void e_Wishing(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        GlobalVars.nwrWin.hideInventory();
        GlobalVars.nwrWin.showInput(Locale.getStr(RS.rs_WishedItem), EffectsFactory::WishAcceptProc);
    }

    public static void e_Withered(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (invokeMode == InvokeMode.im_Use) {
            creature.addEffect(EffectID.eid_Withered, state, EffectAction.ea_EachTurn, true, Locale.getStr(RS.rs_YouAreWithered));
        }
    }

    private static void FieldDries(NWField f, NWCreature creature)
    {
        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_AllLiquidIsDriedHere));
        if (f.LandID != GlobalVars.Land_Ocean) {
            boolean mud = false;
            boolean water = false;
            boolean quicksand = false;
            boolean lava = false;

            for (int y = 0; y < f.getHeight(); y++) {
                for (int x = 0; x < f.getWidth(); x++) {
                    NWTile tile = f.getTile(x, y);

                    int bg = tile.getBackBase();
                    if (UniverseBuilder.isWaters(bg)) {
                        tile.Background = (short) PlaceID.pid_Grass;
                        tile.BackgroundExt = (short) PlaceID.pid_Undefined;
                    }

                    if (bg == PlaceID.pid_Mud) {
                        mud = true;
                    }
                    if (bg == PlaceID.pid_Liquid || bg == PlaceID.pid_Water || bg == PlaceID.pid_WaterTrap) {
                        water = true;
                    }
                    if (bg == PlaceID.pid_Quicksand) {
                        quicksand = true;
                    }
                    if (bg == PlaceID.pid_Lava || bg == PlaceID.pid_LavaPool) {
                        lava = true;
                    }

                    int fg = tile.getForeBase();
                    if (UniverseBuilder.isWaters(fg)) {
                        tile.Foreground = (short) PlaceID.pid_Undefined;
                        tile.ForegroundExt = (short) PlaceID.pid_Undefined;
                    }
                    if (fg == PlaceID.pid_Mud) {
                        mud = true;
                    }
                    if (fg == PlaceID.pid_Liquid || fg == PlaceID.pid_Water || fg == PlaceID.pid_WaterTrap) {
                        water = true;
                    }
                    if (fg == PlaceID.pid_Quicksand) {
                        quicksand = true;
                    }
                    if (fg == PlaceID.pid_Lava || fg == PlaceID.pid_LavaPool) {
                        lava = true;
                    }
                }
            }

            f.normalize();

            if (mud) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_MudDries));
            }
            if (water) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_WaterDries));
            }
            if (quicksand) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_QuicksandDries));
            }
            if (lava) {
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_LavaDries));
            }
        }

        EntityList cs = f.getCreatures();
        for (int i = 0; i < cs.getCount(); i++) {
            NWCreature creat = (NWCreature) cs.getItem(i);

            int num4 = creat.getItems().getCount();
            for (int j = 0; j < num4; j++) {
                Item it = creat.getItems().getItem(j);
                if (it.getKind() == ItemKind.ik_Potion) {
                    it.setCLSID(GlobalVars.iid_Vial);
                }
            }
        }

        int num5 = f.getItems().getCount();
        for (int i = 0; i < num5; i++) {
            Item it = f.getItems().getItem(i);
            if (it.getKind() == ItemKind.ik_Potion) {
                it.setCLSID(GlobalVars.iid_Vial);
            }
        }
    }

    public static void e_Wonder(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        NWField fld = creature.getCurrentField();

        AuxUtils.exStub("You hear the sounds of a marketplace.");
        AuxUtils.exStub("You hear a faraway voice.");
        AuxUtils.exStub("You smell gold nearby.");
        AuxUtils.exStub("You hear the distant swaying of plants.");
        AuxUtils.exStub("You recall the smell of freshly cut barley.");
        AuxUtils.exStub("You feel a thousand angry voices in your head.");
        AuxUtils.exStub("You feel strangely pacifistic.");
        AuxUtils.exStub("Everything is slowing down.");
        AuxUtils.exStub("Time warps slightly around you.");
        AuxUtils.exStub("Time is passing you by.");
        AuxUtils.exStub("You narrowly escape the wrath of an evil being.");
        AuxUtils.exStub("The ");
        AuxUtils.exStub(" flops helplessly and dies.");
        AuxUtils.exStub(" suffocates.");
        AuxUtils.exStub("antijag");
        AuxUtils.exStub("You have awakened something evil.");
        AuxUtils.exStub("The humidity increases. comment:(all field to mud)");
        AuxUtils.exStub("A strange energy whirls around you. (оживляет предметы)");
        AuxUtils.exStub("Particles of energy surge around you.");
        AuxUtils.exStub(" is swept away by the tree! ");
        AuxUtils.exStub("Tiny seeds begin to float around you.");

        if (state == ItemState.is_Blessed || state == ItemState.is_Cursed) {
            EffectsFactory.e_Transformation(EffectID.eid_Transformation, creature, source, state, InvokeMode.im_ItSelf, ext);
        } else {
            if (creature.isConfused()) {
                UniverseBuilder.gen_BigRiver(fld);
            }

            int i = AuxUtils.getBoundedRnd(1, 10);
            switch (i) {
                case 1: {
                    // check-ok
                    for (int y = creature.getPosY() - 2; y <= creature.getPosY() + 2; y++) {
                        for (int x = creature.getPosX() - 2; x <= creature.getPosX() + 2; x++) {
                            if (!fld.isBarrier(x, y) && !creature.getLocation().equals(x, y)) {
                                fld.getTile(x, y).Foreground = (short) PlaceID.pid_Tree;
                            }
                        }
                    }
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_TreesSprout));
                    break;
                }

                case 3:
                    EffectsFactory.FieldDries(fld, creature);
                    break;

                case 4:
                    EffectsFactory.e_Transformation(EffectID.eid_Transformation, creature, source, state, InvokeMode.im_ItSelf, ext);
                    break;

                case 5: {
                    int num3 = fld.getCreatures().getCount();
                    for (int k = 0; k < num3; k++) {
                        fld.getCreatures().getItem(k).invertHostility();
                    }
                    break;
                }

                case 6: {
                    // check-ok
                    i = GlobalVars.nwrBase.findEntryBySign("Retchweed").GUID;

                    for (int y = 0; y < StaticData.FieldHeight; y++) {
                        for (int x = 0; x < StaticData.FieldWidth; x++) {
                            NWTile tile = (NWTile) fld.getTile(x, y);
                            if (tile.Foreground == PlaceID.pid_Tree) {
                                int num6 = AuxUtils.getRandom(2);
                                if (num6 != 0) {
                                    if (num6 == 1) {
                                        tile.Foreground = (short) PlaceID.pid_Undefined;
                                        fld.addCreature(x, y, i);
                                    }
                                } else {
                                    tile.Foreground = (short) PlaceID.pid_DeadTree;
                                }
                            }
                        }
                    }

                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ForestTransforms));
                    break;
                }

                case 7:
                    EffectsFactory.e_Speedup(EffectID.eid_Speedup, creature, source, state, InvokeMode.im_ItSelf, ext);
                    break;

                case 8: {
                    int num7 = fld.getCreatures().getCount();
                    for (i = 0; i < num7; i++) {
                        NWCreature creat = fld.getCreatures().getItem(i);
                        if (!creat.equals(creature)) {
                            EffectsFactory.e_Speedup(EffectID.eid_Speedup, creat, source, state, InvokeMode.im_ItSelf, ext);
                        }
                    }
                    break;
                }
                case 9:
                    creature.setAbility(AbilityID.Resist_Teleport, 100);
                    creature.setSkill(SkillID.Sk_Relocation, 5);
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveGetTeleportResistance));
                    break;

                case 10: {
                    AbilityID ab = AbilityID.forValue(AuxUtils.getBoundedRnd(AbilityID.Resist_Cold.getValue(), AbilityID.Resist_SleepRay.getValue()));
                    creature.setAbility(ab, 100);

                    switch (ab) {
                        case Resist_Cold:
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelStrangelyCool));
                            break;
                        case Resist_Heat:
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelStrangelyWarm));
                            break;
                        case Resist_Acid:
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourSkinFeelsSlick));
                            break;
                        case Resist_Poison:
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourLiverSquirms));
                            EffectsFactory.CureEffect(creature, EffectID.eid_Poisoned, Locale.getStr(RS.rs_VenomIsNeutralized));
                            break;
                        case Resist_Ray:
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YourBodyHasStrangeSheen));
                            break;
                        case Resist_DeathRay:
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouWillLiveForever));
                            break;
                        case Resist_Petrification:
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouFeelOneWithEarth));
                            EffectsFactory.CureStoning(creature, null);
                            break;

                        default:
                            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_YouveGetANewResistance));
                            break;
                    }
                    break;
                }
            }
        }
    }

    public static void e_Writing(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        if (creature.getItems().findByCLSID(GlobalVars.iid_Stylus) == null) {
            GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_StylusNecessary));
        } else {
            Object obj = ext.getParam(EffectParams.ep_ScrollID);
            int sid = (Integer) obj;

            Item scroll = (Item) ext.getParam(EffectParams.ep_Item);
            if (scroll.getKind() == ItemKind.ik_Scroll) {
                scroll.setCLSID(sid);
                AuxUtils.exStub("msg: You rewrite the scroll.");
                GlobalVars.nwrWin.showText(creature, Locale.format(RS.rs_ScrollWrited, new Object[]{scroll.getName()}));
            } else {
                // message?
            }
        }
    }

    public static void e_CaughtInNet(EffectID effectID, NWCreature creature, Object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
    {
        switch (invokeMode) {
            case im_Use:
                RaceID race = creature.getEntry().Race;
                if (race == RaceID.crAesir || race == RaceID.crEvilGod || race == RaceID.crDaemon) {
                    return;
                }
                String msg = String.format(Locale.getStr(RS.rs_IsCaughtInTheNet), creature.getName());
                creature.addEffect(EffectID.eid_CaughtInNet, state, EffectAction.ea_Persistent, false, msg);
                break;

            case im_ItSelf:
                break;

            case im_FinAction:
                break;
        }
    }

    private static NWCreature animateDeadBody(NWCreature creature, Item extItem)
    {
        NWField fld = creature.getCurrentField();
        return GlobalVars.nwrGame.respawnDeadBody(fld, creature.getLocation(), extItem);
    }

    public static void deanimate(NWField field, NWCreature monster, BaseTile tile, int tileID)
    {
        tile.Background = (short) tileID;
        String ms = monster.getDeclinableName(Number.nSingle, Case.cAccusative);
        monster.checkTile(false);
        field.getCreatures().remove(monster);
        GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.format(RS.rs_RayDeanimateMonster, new Object[]{ms}));
    }

    private static int findExtinctCreature(String input)
    {
        int num = GlobalVars.dbCreatures.getCount();
        for (int i = 0; i < num; i++) {
            DataEntry entry = GlobalVars.nwrBase.getEntry(GlobalVars.dbCreatures.get(i));
            if (entry instanceof DeclinableEntry && ((DeclinableEntry) entry).hasNounMorpheme(input)) {
                return GlobalVars.dbCreatures.get(i);
            }
        }
        return -1;
    }

    private static void ExtinctionAcceptProc(String input)
    {
        int id = EffectsFactory.findExtinctCreature(input);
        CreatureEntry cEntry = (CreatureEntry) GlobalVars.nwrBase.getEntry(id);

        Player player = GlobalVars.nwrGame.getPlayer();
        if (id > -1) {
            if (cEntry.Race == player.getEntry().Race) {
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_AsYouWish));
                if (cEntry.Race == RaceID.crHuman) {
                    GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_HumanRaceDestroyed));
                }
                player.death(Locale.getStr(RS.rs_DestroyedHisRace), null);
                GlobalVars.nwrWin.doEvent(EventID.event_Dead, null, null, null);
            } else {
                if (cEntry.Extinctable) {
                    GlobalVars.nwrWin.ProgressInit(GlobalVars.dbLayers.getCount());

                    int num = GlobalVars.dbLayers.getCount();
                    for (int i = 0; i < num; i++) {
                        NWLayer layer = GlobalVars.nwrGame.getLayer(i);

                        int num2 = layer.getH();
                        for (int fy = 0; fy < num2; fy++) {
                            int num3 = layer.getW();
                            for (int fx = 0; fx < num3; fx++) {
                                NWField f = layer.getField(fx, fy);

                                for (int j = f.getCreatures().getCount() - 1; j >= 0; j--) {
                                    NWCreature cr = f.getCreatures().getItem(j);
                                    if (cr.CLSID == id) {
                                        cr.dropAll();
                                        cr.checkTile(false);
                                        f.getCreatures().remove(cr);
                                    }
                                }
                            }
                        }

                        GlobalVars.nwrWin.ProgressLabel(Locale.getStr(RS.rs_Henocide) + " (" + String.valueOf(i + 1) + "/" + String.valueOf(GlobalVars.dbLayers.getCount()) + ")");
                        GlobalVars.nwrWin.ProgressStep();
                    }

                    GlobalVars.nwrWin.ProgressDone();
                    player.Morality = (byte) ((int) player.Morality - 15);
                    GlobalVars.nwrGame.setVolatileState(id, VolatileState.vesExtincted);
                    GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_RaceDestroyed) + cEntry.getNounDeclension(Number.nPlural, Case.cAccusative) + ".");
                    GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_MoraleLowered));
                } else {
                    GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_RaceUndestroyable));
                }
            }
        } else {
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_NoSuchRace));
        }
    }

    public static void FireHit(EffectID effectID, NWCreature anAttacker, NWCreature aVictim, int aDmg)
    {
        EffectsFactory.HitByRay(EffectID.eid_Fire, anAttacker, aVictim, aDmg, Locale.format(RS.rs_CreatureIncinerated, new Object[]{aVictim.getName()}), Locale.getStr(RS.rs_BurntToCrisp));
    }

    private static void Geology_PrepareTile(NWField fld, NWCreature creature, EffectExt ext, Point p, NWTile aTile)
    {
        boolean res = false;
        Object obj = ext.getParam(EffectParams.ep_TileID);

        int tile_id = ((Integer) obj);
        int tile_id_inv = PlaceID.pid_Undefined;

        switch (tile_id) {
            case PlaceID.pid_StairsDown:
                tile_id_inv = PlaceID.pid_StairsUp;
                break;
            case PlaceID.pid_StairsUp:
                tile_id_inv = PlaceID.pid_StairsDown;
                break;
            case PlaceID.pid_HoleDown:
                tile_id_inv = PlaceID.pid_HoleUp;
                break;
            case PlaceID.pid_HoleUp:
                tile_id_inv = PlaceID.pid_HoleDown;
                break;
        }

        AuxUtils.exStub("todo:  msg");

        switch (tile_id) {
            case PlaceID.pid_StairsDown:
            case PlaceID.pid_HoleDown: {
                int lid = -1;
                Point fp = Point.Zero();

                if (fld.getLayer().EntryID == GlobalVars.Layer_Midgard) {
                    int x = fld.getCoords().X;
                    int y = fld.getCoords().Y;
                    if ((x >= 3 && x <= 5) && (y >= 4 && y <= 6)) {
                        lid = GlobalVars.Layer_Svartalfheim1;
                        fp.X = fld.getCoords().X - 3;
                        fp.Y = fld.getCoords().Y - 4;
                    } else {
                        res = true;
                    }
                } else if (fld.getLayer().EntryID == GlobalVars.Layer_Svartalfheim1) {
                    lid = GlobalVars.Layer_Svartalfheim2;
                    fp = fld.getCoords();
                } else if (fld.getLayer().EntryID == GlobalVars.Layer_Svartalfheim2) {
                    if (fld.getCoords().X == 2 && fld.getCoords().Y == 2) {
                        res = true;
                    } else {
                        lid = GlobalVars.Layer_Svartalfheim3;
                        fp = fld.getCoords();
                    }
                }

                if (lid >= 0) {
                    NWField new_fld = GlobalVars.nwrGame.getField(lid, fp.X, fp.Y);
                    Point new_pos;

                    if (creature.canMove(new_fld, p.X, p.Y)) {
                        new_pos = p;
                    } else {
                        new_pos = new_fld.searchFreeLocation();
                    }

                    fld.addGate(tile_id, p.X, p.Y, lid, new_fld.getCoords(), new_pos);
                    new_fld.addGate(tile_id_inv, new_pos.X, new_pos.Y, fld.getLayer().EntryID, fld.getCoords(), p);
                } else {
                    res = true;
                }
            }
            break;

            case PlaceID.pid_StairsUp:
            case PlaceID.pid_HoleUp: {
                int lid = -1;
                Point fp = Point.Zero();

                if (fld.getLayer().EntryID == GlobalVars.Layer_Svartalfheim3) {
                    if (fld.LandID != GlobalVars.Land_Bazaar) {
                        lid = GlobalVars.Layer_Svartalfheim2;
                        fp = fld.getCoords();
                    } else {
                        GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThisAreaIsMagicallyBounded));
                        return;
                    }
                } else {
                    if (fld.getLayer().EntryID == GlobalVars.Layer_Svartalfheim2) {
                        lid = GlobalVars.Layer_Svartalfheim1;
                        fp = fld.getCoords();
                    } else {
                        if (fld.getLayer().EntryID == GlobalVars.Layer_Svartalfheim1) {
                            lid = GlobalVars.Layer_Midgard;
                            fp.X = fld.getCoords().X + 3;
                            fp.Y = fld.getCoords().Y + 4;
                        }
                    }
                }

                if (lid >= 0) {
                    NWField new_fld = GlobalVars.nwrGame.getField(lid, fp.X, fp.Y);
                    Point new_pos;
                    if (creature.canMove(new_fld, p.X, p.Y)) {
                        new_pos = p;
                    } else {
                        new_pos = new_fld.searchFreeLocation();
                    }

                    fld.addGate(tile_id, p.X, p.Y, lid, new_fld.getCoords(), new_pos);
                    new_fld.addGate(tile_id_inv, new_pos.X, new_pos.Y, fld.getLayer().EntryID, fld.getCoords(), p);
                } else {
                    res = true;
                }
            }
            break;

            default:
                res = true;
                break;
        }

        if (!res) {
            return;
        }

        int tid = tile_id;
        if ((StaticData.dbPlaces[tid].Signs.contains(PlaceFlags.psBackground))) {
            aTile.setBack(tile_id);
            return;
        }

        if ((StaticData.dbPlaces[tid].Signs.contains(PlaceFlags.psForeground))) {
            aTile.setFore(tile_id);
        }
    }

    private static int findWishItem(String str)
    {
        int num = GlobalVars.dbItems.getCount();
        for (int i = 0; i < num; i++) {
            int id = GlobalVars.dbItems.get(i);
            DataEntry entry = GlobalVars.nwrBase.getEntry(id);
            if (entry instanceof DeclinableEntry && ((DeclinableEntry) entry).hasNounMorpheme(str)) {
                return id;
            }
        }
        return -1;
    }

    private static int isTabooItem(String sign)
    {
        for (int i = 0; i < dbTabooItems.length; i++) {
            if (dbTabooItems[i].Origin.equals(sign)) {
                return i;
            }
        }
        return -1;
    }

    private static void WishAcceptProc(String input)
    {
        String name;
        String count;
        if (AuxUtils.isValidInt(TextUtils.getToken(input, " ", 1))) {
            count = TextUtils.getToken(input, " ", 1);
            name = TextUtils.getToken(input, " ", 2);
        } else {
            count = "1";
            name = input;
        }

        AuxUtils.exStub("todo: You receive nothing.");
        AuxUtils.exStub("todo: Your wish does not materialize.");
        AuxUtils.exStub("todo: little");
        AuxUtils.exStub("todo: much");
        AuxUtils.exStub("todo: You have wished for too %s.");
        AuxUtils.exStub("todo: Your pack is full.");

        int id = EffectsFactory.findWishItem(name);
        ItemEntry cEntry = (ItemEntry) GlobalVars.nwrBase.getEntry(id);
        if (id > -1) {
            Player player = GlobalVars.nwrGame.getPlayer();
            boolean res = false;

            int tabIdx = EffectsFactory.isTabooItem(cEntry.Sign);
            if (tabIdx >= 0) {
                cEntry = ((ItemEntry) GlobalVars.nwrBase.findEntryBySign(dbTabooItems[tabIdx].Substitute));
                Item.genItem(player, cEntry.GUID, 1, false);
                res = true;
            } else {
                if (!cEntry.isUnique()) {
                    Item.genItem(player, id, Integer.parseInt(count), true);
                    res = true;
                }
            }
            if (res) {
                int i = AuxUtils.getBoundedRnd(910, 912);
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(i));
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_WishedItemTaked));
            }
        } else {
            GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_NoSuchItem));
        }
    }

    private static void CrushTile(AbstractMap map, int xx, int yy)
    {
        NWTile tile = (NWTile) map.getTile(xx, yy);
        tile.setFore(PlaceID.pid_Rubble);

        NWCreature cr = (NWCreature) map.findCreature(xx, yy);
        if (cr != null && cr.getEffects().findEffectByID(EffectID.eid_Phase) == null) {
            GlobalVars.nwrWin.showText(cr, Locale.getStr(RS.rs_FallingDebrisHitsYou));
            cr.death(Locale.getStr(RS.rs_KilledByRoofCrushing), null);
        }
    }

    private static void CrushRoof(DungeonRoom dunRoom, boolean total)
    {
        // check-ok

        AbstractMap map = (AbstractMap) dunRoom.Owner;
        Rect dunArea = dunRoom.getArea().clone();
        dunArea.inflate(-1, -1);

        if (total) {
            for (int y = dunArea.Top; y <= dunArea.Bottom; y++) {
                for (int x = dunArea.Left; x <= dunArea.Right; x++) {
                    EffectsFactory.CrushTile(map, x, y);
                }
            }
        } else {
            int num3 = AuxUtils.getBoundedRnd(3, 7);
            for (int i = 1; i <= num3; i++) {
                int x = AuxUtils.getBoundedRnd(dunArea.Left, dunArea.Right);
                int y = AuxUtils.getBoundedRnd(dunArea.Top, dunArea.Bottom);
                EffectsFactory.CrushTile(map, x, y);
            }
        }

        map.normalize();
    }

    private static void Ray(EffectID effectID, NWCreature creature, InvokeMode invokeMode, EffectExt ext, DamageKind dk)
    {
        MonsterSkillRay ray = new MonsterSkillRay();
        ray.DmgKind = dk;
        try {
            ray.Exec(creature, effectID, invokeMode, ext, "");
            AuxUtils.exStub("rsList(rs_StoningRayIsZapped]");
        } finally {
            creature.getCurrentField().normalize();
        }
    }

    private static void HitByRay(EffectID effectID, NWCreature attacker, NWCreature victim, int damage, String hitMsg, String deathMsg)
    {
        AuxUtils.exStub("todo: распространить на все эффекты с лучом и прямым уроном");
        NWCreature cr;
        if (victim.getAbility(AbilityID.Ab_RayReflect) <= 0) {
            cr = victim;
        } else {
            cr = attacker;
        }
        if (cr.hasAffect(effectID)) {
            AuxUtils.exStub("todo: aMsg := 'x отразил луч' + aMsg;");
            GlobalVars.nwrWin.showText(victim, hitMsg);
            cr.applyDamage(damage, DamageKind.dkRadiation, null, deathMsg);
        }
    }

    private static void SimpleAttack(NWCreature enemy, EffectID effect, DamageKind dk, String deathMsg)
    {
        if (enemy.hasAffect(effect)) {
            Range dmg = EffectsData.dbEffects[effect.getValue()].Damage;
            enemy.applyDamage(AuxUtils.getBoundedRnd(dmg.Min, dmg.Max), dk, null, deathMsg);
        }
    }

    private static String WandRayMsg(EffectID effectID, Object source)
    {
        if (source != null && source instanceof Item) {
            Item item = (Item) source;

            String tmp = item.getEntry().getName();
            tmp = tmp.substring(tmp.indexOf(" ") + 1);

            return Locale.format(RS.rs_RayComesOut, new Object[]{tmp});
        }
        throw new RuntimeException("XRay(" + String.valueOf((int) effectID.getValue()) + "): not implemented");
    }
}
