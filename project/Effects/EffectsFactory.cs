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
using NWR.Creatures;
using NWR.Creatures.Brain;
using NWR.Database;
using NWR.Effects.Rays;
using NWR.Game;
using NWR.Game.Types;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Engine;
using ZRLib.Grammar;
using ZRLib.Map;

namespace NWR.Effects
{
    using ItemKinds = EnumSet<ItemKind>;

    public static class EffectsFactory
    {
        private static readonly ItemKinds Bless_ik;
        private static readonly ItemKinds Bless_ikArmor;
        private static readonly TabooItemRec[] DbTabooItems;

        static EffectsFactory()
        {
            Bless_ik = ItemKinds.Create(ItemKind.ik_Armor, ItemKind.ik_Potion, ItemKind.ik_Ring, ItemKind.ik_BluntWeapon, ItemKind.ik_Scroll, ItemKind.ik_Amulet, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Clothing, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor, ItemKind.ik_Projectile);
            Bless_ikArmor = ItemKinds.Create(ItemKind.ik_Armor, ItemKind.ik_BluntWeapon, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Clothing, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor);

            DbTabooItems = new TabooItemRec[32];
            DbTabooItems[0] = new TabooItemRec("Amulet_Eternal_Life", "Amulet_Eternal_Life");
            DbTabooItems[1] = new TabooItemRec("BlackSword", "BlackSword");
            DbTabooItems[2] = new TabooItemRec("ChinStrap", "ChinStrap");
            DbTabooItems[3] = new TabooItemRec("GoldenTogs", "GoldenTogs");
            DbTabooItems[4] = new TabooItemRec("Lodestone", "Lodestone");
            DbTabooItems[5] = new TabooItemRec("Wand_Amusement", "Wand_Amusement");
            DbTabooItems[6] = new TabooItemRec("Amulet_Ethereality", "Amulet_Eternal_Life");
            DbTabooItems[7] = new TabooItemRec("Amulet_Might", "Amulet_Eternal_Life");
            DbTabooItems[8] = new TabooItemRec("BlazingCape", "GoldenTogs");
            DbTabooItems[9] = new TabooItemRec("DwarvenArm", "BlackSword");
            DbTabooItems[10] = new TabooItemRec("Gjall", "Lodestone");
            DbTabooItems[11] = new TabooItemRec("Coin", "Lodestone");
            DbTabooItems[12] = new TabooItemRec("GreenStone", "Lodestone");
            DbTabooItems[13] = new TabooItemRec("Gungnir", "BlackSword");
            DbTabooItems[14] = new TabooItemRec("KnowledgeHelm", "ChinStrap");
            DbTabooItems[15] = new TabooItemRec("LazlulRope", "Lodestone");
            DbTabooItems[16] = new TabooItemRec("Mimming", "BlackSword");
            DbTabooItems[17] = new TabooItemRec("Mjollnir", "BlackSword");
            DbTabooItems[18] = new TabooItemRec("Ring_SoulTrapping", "Lodestone");
            DbTabooItems[19] = new TabooItemRec("Runesword", "BlackSword");
            DbTabooItems[20] = new TabooItemRec("Scroll_Diary", "Lodestone");
            DbTabooItems[21] = new TabooItemRec("Scroll_BodiesSwitch", "Lodestone");
            DbTabooItems[22] = new TabooItemRec("Scroll_Transport", "Lodestone");
            DbTabooItems[23] = new TabooItemRec("Scythe", "BlackSword");
            DbTabooItems[24] = new TabooItemRec("Wand_Wishing", "Wand_Amusement");
            DbTabooItems[25] = new TabooItemRec("WarVest", "GoldenTogs");
            DbTabooItems[26] = new TabooItemRec("CrystalGloves", "GoldenTogs");
            DbTabooItems[27] = new TabooItemRec("BlueCube", "Lodestone");
            DbTabooItems[28] = new TabooItemRec("GreyCube", "Lodestone");
            DbTabooItems[29] = new TabooItemRec("OrangeCube", "Lodestone");
            DbTabooItems[30] = new TabooItemRec("NorseBoots", "GoldenTogs");
            DbTabooItems[31] = new TabooItemRec("-Potions of alchemy **", "");
        }

        public static void e_Alliance(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            bool cursed = (state == ItemState.is_Cursed);
            if (!cursed) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_CeasesAggression));
            }

            ExtRect r = ExtRect.Create(creature.PosX - 1, creature.PosY - 1, creature.PosX + 1, creature.PosY + 1);
            ExtList<LocatedEntity> list = creature.CurrentField.Creatures.SearchListByArea(r);

            int num = list.Count;
            for (int i = 0; i < num; i++) {
                NWCreature cr = (NWCreature)list[i];
                RaceID race = cr.Entry.Race;
                if (cr == creature || race == RaceID.crAesir || race == RaceID.crEvilGod || race == RaceID.crDaemon) {
                    continue;
                }

                if (cursed) {
                    cr.Alignment = AlignmentEx.GetOppositeAlignment(creature.Alignment, false);
                } else {
                    cr.Alignment = creature.Alignment;
                }
            }

            list.Dispose();
        }

        public static void e_Amusement(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (RandomHelper.GetRandom(5)) {
                case 0:
                    {
                        creature.Strength -= 3;
                        break;
                    }
                case 1:
                    {
                        UniverseBuilder.Gen_Traps((creature.CurrentField), 125);
                        break;
                    }
                case 2:
                    {
                        creature.Constitution--;
                        break;
                    }
                case 3:
                    {
                        creature.ArmorClass -= 10;
                        break;
                    }
                case 4:
                    {
                        creature.TransferTo(creature.LayerID, -1, -1, -1, -1, StaticData.MapArea, true, false);
                        break;
                    }
            }
        }

        public static void e_CheckAnimation(EffectID effectID, NWCreature creature, object source, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode == InvokeMode.im_ItSelf) {
                ext.ReqParams = new EffectParams(EffectParams.ep_Item);
            } else {
                ext.ReqParams = new EffectParams();
            }
        }

        public static void e_Animation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("You play god. (неудачная попытка)");
            AuxUtils.ExStub("rs_YourPowerFails (непонятно)");

            if (invokeMode == InvokeMode.im_Use) {
                creature.SetSkill(SkillID.Sk_Animation, 100);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveGetAnAnimationSkill));
            } else if (invokeMode == InvokeMode.im_ItSelf) {
                Item item = (Item)ext.GetParam(EffectParams.ep_Item);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_LifeForceFlows));
                if (item.CLSID == GlobalVars.iid_DeadBody || item.CLSID == GlobalVars.iid_Mummy) {
                    NWCreature cr = AnimateDeadBody(creature, item);
                    cr.Alignment = creature.Alignment;
                    string dummy = cr.GetDeclinableName(Number.nSingle, Case.cAccusative);
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveAnimated) + dummy + ".");
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NecessaryToPointACorpse));
                }
            }
        }

        public static void e_Annihilation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("Tons of rock fall on your head. (dir=up, -~2300hp!, +tile rubble)");
            AuxUtils.ExStub("Crushed beneath tons of rock");
            AuxUtils.ExStub("The ray shoots off into the sky. (dir=up, land=non caves)");
            AuxUtils.ExStub("You dig a giant hole. (dir=down)");
            AuxUtils.ExStub("        grynr halls -> space");
            AuxUtils.ExStub("        midgard -> niflheim");
            AuxUtils.ExStub("The ");
            AuxUtils.ExStub(" plummets into space.");
            AuxUtils.ExStub("You dig a bottomless pit below you.");
            AuxUtils.ExStub("Still plummeting!");

            NWField fld = creature.CurrentField;
            if (fld.LandID == GlobalVars.Land_MimerRealm || fld.LandID == GlobalVars.Land_Nidavellir || fld.LandID == GlobalVars.Land_Jotenheim || fld.LandID == GlobalVars.Land_Valhalla || fld.LandID == GlobalVars.Land_GodsFortress || fld.LandID == GlobalVars.Land_Muspelheim || fld.LandID == GlobalVars.Land_Wasteland || fld.LandID == GlobalVars.Land_Bazaar || fld.LandID == GlobalVars.Land_Crypt || fld.LandID == GlobalVars.Land_Vanaheim || fld.LandID == GlobalVars.Land_Armory || fld.LandID == GlobalVars.Land_Ocean || fld.LandID == GlobalVars.Land_Niflheim || fld.LandID == GlobalVars.Land_Temple || fld.LandID == GlobalVars.Land_MimerWell || fld.LandID == GlobalVars.Land_Crossroads || fld.LandID == GlobalVars.Land_Bifrost || fld.LandID == GlobalVars.Land_SlaeterSea || fld.LandID == GlobalVars.Land_Alfheim || fld.LandID == GlobalVars.Land_GiollRiver) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_AnnihilationRayDoesntWorkHere));
            } else {
                AuxUtils.ExStub("todo: 7 этапов через каждые 7 тайлов, на 2 в ширину увеличивается.");
                AnnihilationRay ray = new AnnihilationRay();
                try {
                    ray.Exec(creature, EffectID.eid_Annihilation, invokeMode, ext, BaseLocale.GetStr(RS.rs_AnnihilationRaySwepsAwayEverything));
                } finally {
                    fld.Normalize();
                }
                AuxUtils.ExStub("todo: orig-msg: Blinding light explodes out of the wand.");
            }
        }

        public static void e_Armoring(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            e_Scaling(EffectID.eid_Scaling, (NWCreature)ext.GetParam(EffectParams.ep_Creature), source, state, InvokeMode.im_ItSelf, ext);
        }

        public static void e_ArrowMake(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub(" Cut arrows in which direction?");
            AuxUtils.ExStub(" You can only create arrows from trees.");
            AuxUtils.ExStub(" You create some arrows.");
            AuxUtils.ExStub(" You become distracted and ruin your handiwork.");

            ExtPoint pt = (ExtPoint)ext.GetParam(EffectParams.ep_Place);

            if (!creature.IsNear(pt)) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(StaticData.dbEffectTarget[(int)EffectTarget.et_PlaceNear].Invalid));
            } else {
                NWTile tile = (NWTile)creature.CurrentField.GetTile(pt.X, pt.Y);

                if (tile.ForeBase != PlaceID.pid_Tree) {
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_HereNoTrees));
                } else {
                    int aID;
                    if (AuxUtils.Chance(50)) {
                        aID = GlobalVars.iid_Arrow;
                    } else {
                        aID = GlobalVars.iid_Bolt;
                    }
                    Item.GenItem(creature, aID, RandomHelper.GetBoundedRnd(3, 17), true);
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_ArrowsCreated));
                }
            }
        }

        public static void e_ArrowTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveWoundedByTheArrow));
            creature.ApplyDamage(RandomHelper.GetBoundedRnd(5, 10), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_KilledByArrowTrap));
        }

        public static void e_Ashes(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode == InvokeMode.im_UseBegin) {
                creature.InitEffect(effectID, source, EffectAction.ea_Persistent);
            } else if (invokeMode == InvokeMode.im_UseEnd) {
                creature.DoneEffect(effectID, source);
            }
        }

        public static void e_BlackGemUse(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: messages");
            BlackGemRay ray = new BlackGemRay();
            ray.Exec(creature, EffectID.eid_BlackGemUse, invokeMode, ext, "");
        }

        private static void IntBlessing(NWCreature creature, Item extItem)
        {
            ItemKind kind = extItem.Kind;
            string sign = extItem.Entry.Sign;

            if ((Bless_ik.Contains(kind))) {
                if (sign.Equals("Potion_DrinkingWater")) {
                    extItem.CLSID = GlobalVars.nwrDB.FindEntryBySign("Potion_HolyWater").GUID;
                } else {
                    if (sign.CompareTo("Amulet_Holding") != 0) {
                        switch (extItem.State) {
                            case ItemState.is_Normal:
                                extItem.State = ItemState.is_Blessed;
                                break;
                            case ItemState.is_Cursed:
                                extItem.State = ItemState.is_Normal;
                                break;
                        }
                    }
                }

                if (((Bless_ikArmor.Contains(kind))) || (kind == ItemKind.ik_Ring && (sign.Equals("Ring_Agility") || sign.Equals("Ring_Protection")))) {
                    // TODO: Повышение характеристик
                }
            }
        }

        public static void e_Blessing(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.Confused) {
                int num = creature.Items.Count;
                for (int idx = 0; idx < num; idx++) {
                    Item item = creature.Items.GetItem(idx);
                    AuxUtils.ExStub("todo: //if (aExtItem = aItem) then Continue; вставить везде");
                    if (AuxUtils.Chance(60) && state != ItemState.is_Cursed) {
                        IntBlessing(creature, item);
                    }
                }
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveBlessedAllItemsInInventory));
            } else {
                Item item = (Item)ext.GetParam(EffectParams.ep_Item);
                if (state != ItemState.is_Cursed) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_YouveBlessed, new object[]{ item.Name }));
                    IntBlessing(creature, item);
                }
            }
        }

        public static void e_Blindness(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            creature.AddEffect(EffectID.eid_Blindness, state, EffectAction.ea_Persistent, true, BaseLocale.GetStr(RS.rs_YouAreBlinding));
        }

        public static void e_BrainScarring(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Burns(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Cancellation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub(" The ... ray hits the <monster>.");
            CancellationRay ray = new CancellationRay();
            ray.Exec(creature, EffectID.eid_Cancellation, invokeMode, ext, WandRayMsg(effectID, source));
        }

        public static void e_Cartography(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (creature.IsPlayer) {
                NWField field = creature.CurrentField;

                int num = field.Height;
                for (int y = 0; y < num; y++) {
                    int num2 = field.Width;
                    for (int x = 0; x < num2; x++) {
                        NWTile tile = (NWTile)field.GetTile(x, y);

                        switch (state) {
                            case ItemState.is_Normal:
                                tile.IncludeState(BaseTile.TS_VISITED);
                                break;
                            case ItemState.is_Blessed:
                                tile.IncludeState(BaseTile.TS_VISITED);
                                if (field.IsTrap(x, y)) {
                                    tile.Trap_Discovered = true;
                                }
                                break;
                            case ItemState.is_Cursed:
                                tile.ExcludeState(BaseTile.TS_VISITED);
                                break;
                        }
                    }
                }

                if (state == ItemState.is_Cursed) {
                    field.Visited = false;
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_SurroundingsSeemUnfamiliar));
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouSeeALandscape));
                }
            }
        }

        public static void e_ChangeAbility(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            object obj = ext.GetParam(EffectParams.ep_ItemExt);
            AbilityID ab = (AbilityID)obj;

            if (ab != AbilityID.Ab_None) {
                if (invokeMode != InvokeMode.im_Use && invokeMode != InvokeMode.im_UseBegin) {
                    if (invokeMode == InvokeMode.im_UseEnd) {
                        creature.SetAbility(ab, creature.GetAbility(ab) - 1);
                        return;
                    }
                    if (invokeMode != InvokeMode.im_ItSelf) {
                        return;
                    }
                }
                creature.SetAbility(ab, creature.GetAbility(ab) + 1);
            }
        }

        public static void e_ChangeSkill(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            object obj = ext.GetParam(EffectParams.ep_ItemExt);
            SkillID sk = (SkillID)obj;

            if (sk != SkillID.Sk_None) {
                if (invokeMode != InvokeMode.im_Use && invokeMode != InvokeMode.im_UseBegin) {
                    if (invokeMode == InvokeMode.im_UseEnd) {
                        creature.SetSkill(sk, creature.GetSkill(sk) - 1);
                        return;
                    }
                    if (invokeMode != InvokeMode.im_ItSelf) {
                        return;
                    }
                }
                creature.SetSkill(sk, creature.GetSkill(sk) + 1);
            }
        }

        private static void ChangeChLakeTile(IMap map, int x, int y, object extData, ref bool  refContinue)
        {
            BaseTile tile = map.GetTile(x, y);
            if (tile != null) {
                tile.Background = (ushort)PlaceID.pid_Water;
            }
        }

        public static void e_Chaos(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;

            AuxUtils.ExStub("You have lost your appetite.");
            AuxUtils.ExStub("You change appearance.");

            int idx = RandomHelper.GetBoundedRnd(1, 5);
            switch (idx) {
                case 1:
                    {
                        // none original messages
                        while (creature.Items.Count > 0) {
                            Item aExtItem = creature.Items.GetItem(0);
                            creature.Items.Extract(aExtItem);
                            aExtItem.InUse = false;

                            ExtPoint pt = fld.SearchFreeLocation();
                            if (pt.IsEmpty) {
                                pt = creature.Location;
                            }

                            aExtItem.SetPos(pt.X, pt.Y);
                            fld.Items.Add(aExtItem, false);
                        }
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_AllYourItemsAreScatteredOnLevel));
                        break;
                    }
                case 2:
                    {
                        // none original messages
                        ExtRect area = ExtRect.Create(creature.PosX - 5, creature.PosY - 5, creature.PosX + 5, creature.PosY + 5);
                        fld.Gen_Lake(area, ChangeChLakeTile);
                        fld.Normalize();
                        break;
                    }
                case 3:
                    {
                        // none original messages
                        UniverseBuilder.Gen_Creatures(fld, RandomHelper.GetBoundedRnd(3, 19));
                        break;
                    }
                case 4:
                    {
                        // none original messages
                        UniverseBuilder.Gen_Items(fld, RandomHelper.GetBoundedRnd(21, 23));
                        break;
                    }
                case 5:
                    {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveGetAPersecutionMania));
                        break;
                    }
            }
        }

        public static void e_Confusion(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo:  messages");
            AuxUtils.ExStub("todo: You are no longer confused.");
            AuxUtils.ExStub("duration: ~ 26-47 turns;");
            AuxUtils.ExStub("  if >50 then feel more intox;");
            AuxUtils.ExStub("  pass out -> ~10 turns -> can move again;");
            creature.AddEffect(EffectID.eid_Confusion, state, EffectAction.ea_Persistent, true, "");
            Effect ef = creature.Effects.FindEffectByID(EffectID.eid_Confusion);
            if (ef != null && ef.Duration > 75 && creature.HasAffect(EffectID.eid_Intoxicate)) {
                creature.AddEffect(EffectID.eid_Intoxicate, state, EffectAction.ea_EachTurn, true, "");
            }
        }

        public static void e_Constitution(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            switch (state) {
                case ItemState.is_Normal:
                    creature.Constitution++;
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsPowerAndHealthy));
                    break;
                case ItemState.is_Blessed:
                    creature.Constitution += 2;
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelTougher));
                    break;
                case ItemState.is_Cursed:
                    creature.Constitution--;
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsPowerlessAndHealthless));
                    break;
            }
        }

        public static void e_Contamination(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode == InvokeMode.im_ItSelf) {
                    Effect e = creature.Effects.FindEffectByID(EffectID.eid_Contamination);
                    if (e.Source == null) {
                        if (e.Duration == 1) {
                            creature.Death(BaseLocale.GetStr(RS.rs_Degenerated), null);
                        } else {
                            if (e.Duration == 5) {
                                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouAreDying));
                            } else {
                                if (e.Duration == 10) {
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourSkinIsDecomposes));
                                } else {
                                    if (e.Duration == 25) {
                                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourHairsAreFallsOut));
                                    } else {
                                        if (e.Duration == 50) {
                                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelVerySick));
                                        }
                                    }
                                }
                            }
                        }

                        if (e.Duration <= 50) {
                            int dg = RandomHelper.GetBoundedRnd(1, 9);
                            creature.ApplyDamage(dg, DamageKind.Physical, null, "");
                        }
                    }
                }
            } else {
                if (creature.HasAffect(EffectID.eid_Contamination)) {
                    creature.AddEffect(EffectID.eid_Contamination, state, EffectAction.ea_EachTurn, true, BaseLocale.GetStr(RS.rs_YourBodyIsLighted));
                }
            }
        }

        public static void e_CrushRoof(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            DungeonRoom room = (DungeonRoom)ext.GetParam(EffectParams.ep_DunRoom);
            if (room != null) {
                GlobalVars.nwrWin.ShowText(null, BaseLocale.GetStr(RS.rs_CeilingCracks));
                CrushRoof(room, true);
            }
        }

        public static void e_CrushRoofTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            DungeonRoom dunRoom = creature.FindDungeonRoom();
            if (dunRoom != null) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_CeilingCracks));
                CrushRoof(dunRoom, false);
            }
        }

        public static void e_CrystalSkin(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Cube(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (AuxUtils.Chance(15) && creature.IsPlayer) {
                AuxUtils.ExStub("todo: messages");
                AuxUtils.ExStub("todo: отсчет времени");

                Item item = (Item)source;
                if (item.Entry.Sign.CompareTo("BlueCube") == 0) {
                    int val = RandomHelper.GetBoundedRnd(1, 100);
                    if (creature.Turn < val) {
                        val = creature.Turn;
                    }
                    creature.Turn -= val;
                } else {
                    if (item.Entry.Sign.CompareTo("GreyCube") == 0) {
                        int val = RandomHelper.GetBoundedRnd(1, 500);
                        creature.Turn += val;
                        AuxUtils.ExStub("!!!");
                    } else {
                        if (item.Entry.Sign.CompareTo("OrangeCube") == 0) {
                            creature.TransferTo(GlobalVars.Layer_Wasteland, 0, 0, -1, -1, StaticData.MapArea, true, true);
                        }
                    }
                }
            }
        }

        public static void CreateMapObject(EffectID effectID, NWField fld, int posX, int posY, bool needUpdate)
        {
            MapObject mapObj = new MapObject(GameSpace.Instance, fld);
            mapObj.InitByEffect(effectID);
            mapObj.SetPos(posX, posY);
            mapObj.IsNeedUpdate = needUpdate;
            fld.Features.Add(mapObj);
        }

        private static void CureEffect(NWCreature creature, EffectID effectID, string msg)
        {
            Effect ef = creature.Effects.FindEffectByID(effectID);
            if (ef != null) {
                creature.Effects.Remove(ef);
                GlobalVars.nwrWin.ShowText(creature, msg);
            }
        }

        public static void e_Cure(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;

            CreateMapObject(effectID, fld, creature.PosX, creature.PosY, true);

            if (state != ItemState.is_Cursed) {
                CureEffect(creature, EffectID.eid_Blindness, BaseLocale.GetStr(RS.rs_BlindnessIsCured));
                CureEffect(creature, EffectID.eid_Fever, BaseLocale.GetStr(RS.rs_FeverIsCured));
                CureEffect(creature, EffectID.eid_Withered, BaseLocale.GetStr(RS.rs_WitheredIsCured));
                CureEffect(creature, EffectID.eid_Confusion, BaseLocale.GetStr(RS.rs_ConfusionIsCured));
                CureEffect(creature, EffectID.eid_Hallucination, BaseLocale.GetStr(RS.rs_HallucinationIsCured));
                CureEffect(creature, EffectID.eid_HealInability, BaseLocale.GetStr(RS.rs_HealInabilityIsCured));
                CureEffect(creature, EffectID.eid_Diseased, BaseLocale.GetStr(RS.rs_YouCured));
            }

            switch (state) {
                case ItemState.is_Normal:
                    if (creature.HPCur == creature.HPMax_Renamed) {
                        creature.HPMax = creature.HPMax_Renamed + 1;
                        creature.HPCur = creature.HPMax_Renamed;
                    } else {
                        creature.HPCur += RandomHelper.GetBoundedRnd(1, creature.HPMax_Renamed - creature.HPCur);
                    }
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsBetter));
                    break;

                case ItemState.is_Blessed:
                    if (creature.HPCur == creature.HPMax_Renamed) {
                        creature.HPMax = creature.HPMax_Renamed + RandomHelper.GetBoundedRnd(4, 7);
                        creature.HPCur = creature.HPMax_Renamed;
                    } else {
                        creature.HPCur = creature.HPMax_Renamed;
                    }
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsMoreBetter));
                    CureEffect(creature, EffectID.eid_Contamination, BaseLocale.GetStr(RS.rs_ContaminationIsCured));
                    CureEffect(creature, EffectID.eid_Burns, BaseLocale.GetStr(RS.rs_BurnsIsCured));
                    CureEffect(creature, EffectID.eid_LegsMissing, BaseLocale.GetStr(RS.rs_LegsMissingIsCured));
                    CureEffect(creature, EffectID.eid_Intoxicate, BaseLocale.GetStr(RS.rs_IntoxicateIsCured));
                    CureEffect(creature, EffectID.eid_EyesMissing, BaseLocale.GetStr(RS.rs_EyesMissingIsCured));
                    CureEffect(creature, EffectID.eid_Deafness, BaseLocale.GetStr(RS.rs_DeafnessIsCured));
                    CureEffect(creature, EffectID.eid_Vertigo, BaseLocale.GetStr(RS.rs_VertigoIsCured));
                    CureEffect(creature, EffectID.eid_Lycanthropy, BaseLocale.GetStr(RS.rs_LycanthropyIsCured));
                    CureEffect(creature, EffectID.eid_BrainScarring, BaseLocale.GetStr(RS.rs_BrainScarringIsCured));
                    CureEffect(creature, EffectID.eid_Impregnation, BaseLocale.GetStr(RS.rs_ImpregnationIsCured));
                    CureEffect(creature, EffectID.eid_Poisoned, BaseLocale.GetStr(RS.rs_VenomIsNeutralized));
                    break;

                case ItemState.is_Cursed:
                    creature.ApplyDamage(RandomHelper.GetBoundedRnd(3, 7), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_YouFeelsWorse));
                    break;
            }
        }

        public static void e_Deafness(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Deanimation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub(" The ... ray hits the <monster>.");
            DeanimationRay ray = new DeanimationRay();
            try {
                ray.Exec(creature, EffectID.eid_Deanimation, invokeMode, ext, WandRayMsg(effectID, source));
            } finally {
                creature.CurrentField.Normalize();
            }
        }

        public static void e_Death(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub(" The ... ray hits the <monster>.");
            DeathRay ray = new DeathRay();
            ray.Exec(creature, EffectID.eid_Death, invokeMode, ext, WandRayMsg(EffectID.eid_Death, source));
        }

        public static void e_Destruction(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            ItemsList list = creature.Items;

            if (creature.Confused) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ScrollWasDestroyAllItems));
                for (int i = list.Count - 1; i >= 0; i--) {
                    if (AuxUtils.Chance(45)) {
                        Item item = list.GetItem(i);
                        item.InUse = false;
                        list.Remove(item);
                    }
                }
            } else {
                Item item = (Item)ext.GetParam(EffectParams.ep_Item);
                item.InUse = false;
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_ScrollWasDestroy, new object[]{ item.Name }));
                list.Remove(item);
            }
        }

        public static void e_DetectItems(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (invokeMode == InvokeMode.im_Use || invokeMode == InvokeMode.im_ItSelf) {
                AuxUtils.ExStub("@deprecated: You sense nothing of value in this area.");
                creature.AddEffect(EffectID.eid_DetectItems, state, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_ThiefEyeWillShowYouAllItems));
            }
        }

        public static void e_CheckDiagnosis(EffectID effectID, NWCreature creature, object source, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode == InvokeMode.im_ItSelf) {
                ext.ReqParams = new EffectParams(EffectParams.ep_Creature);
            } else {
                ext.ReqParams = new EffectParams();
            }
        }

        public static void e_Diagnosis(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode == InvokeMode.im_Use) {
                creature.SetSkill(SkillID.Sk_Diagnosis, creature.GetSkill(SkillID.Sk_Diagnosis) + 1);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveGetADiagnosisSkill));
            } else if (invokeMode == InvokeMode.im_ItSelf) {
                GlobalVars.nwrGame.DiagnoseCreature((NWCreature)ext.GetParam(EffectParams.ep_Creature));
            }
        }

        private static bool DialogCheck(NWCreature collocutor)
        {
            bool result = false;
            if (!(collocutor.Brain is SentientBrain)) {
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_ThisNotCollocutor));
            } else {
                result = true;
            }
            return result;
        }

        public static void e_Dialog(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWCreature col = null;
            ExtList<NWCreature> cr_list = new ExtList<NWCreature>();
            NWField fld = creature.CurrentField;

            int num = fld.Creatures.Count;
            for (int i = 0; i < num; i++) {
                NWCreature creat = fld.Creatures.GetItem(i);
                if (!creat.Equals(creature)) {
                    int dist = MathHelper.Distance(creature.Location, creat.Location);
                    if (dist == 1) {
                        cr_list.Add(creat);
                    }
                }
            }

            if (cr_list.Count == 0) {
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_HereNobody));
            } else {
                if (cr_list.Count == 1) {
                    col = ((NWCreature)cr_list[0]);
                    if (!DialogCheck(col)) {
                        col = null;
                    }
                } else {
                    ExtPoint pt = (ExtPoint)ext.GetParam(EffectParams.ep_Place);

                    if (!creature.IsNear(pt)) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(StaticData.dbEffectTarget[(int)EffectTarget.et_PlaceNear].Invalid));
                        return;
                    }

                    col = ((NWCreature)creature.CurrentField.FindCreature(pt.X, pt.Y));
                    if (col == null) {
                        GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_HereNobody));
                    } else {
                        if (!DialogCheck(col)) {
                            col = null;
                        }
                    }
                }
            }

            cr_list.Dispose();

            if (col != null) {
                GlobalVars.nwrWin.ShowNPCDialog(col);
            }
        }

        public static void e_Diary(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            AuxUtils.ExStub("todo: messages   rs_YouAreMesmerized ? ");
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFindInterestingEntry));
            string s = BaseLocale.GetStr(RandomHelper.GetBoundedRnd(RS.rs_Diary_First, RS.rs_Diary_Last));
            GlobalVars.nwrWin.ShowText(creature, s, new LogFeatures(LogFeatures.lfDialog));
        }

        public static void e_Dig(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.Items.FindByCLSID(GlobalVars.iid_PickAxe) == null) {
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_YouHaventPickaxe));
            } else {
                ExtPoint pt = (ExtPoint)ext.GetParam(EffectParams.ep_Place);

                if (!creature.IsNear(pt)) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(StaticData.dbEffectTarget[(int)EffectTarget.et_PlaceNear].Invalid));
                } else {
                    creature.CurrentField.GetTile(pt.X, pt.Y).Foreground = PlaceID.pid_PitTrap;
                }
            }
        }

        public static void e_Diseased(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ");

            if (invokeMode == InvokeMode.im_Use) {
                if (creature.HasAffect(EffectID.eid_Diseased)) {
                    creature.AddEffect(EffectID.eid_Diseased, state, EffectAction.ea_EachTurn, true, "");
                }
            } else if (invokeMode == InvokeMode.im_ItSelf) {
                Effect e = creature.Effects.FindEffectByID(EffectID.eid_Diseased);
                if (e.Source == null) {
                    if (e.Duration == 1) {
                        creature.Death(BaseLocale.GetStr(RS.rs_KilledByRottingDisease), null);
                    } else {
                        if (AuxUtils.Chance(30)) {
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourSkinCrawlsWithDisease));
                            creature.ApplyDamage(RandomHelper.GetBoundedRnd(1, 7), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_KilledByRottingDisease));
                        }
                    }
                }
            }
        }

        public static void e_DispelHex(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            bool confused = creature.Confused;

            int num = creature.Items.Count;
            for (int idx = 0; idx < num; idx++) {
                Item item = creature.Items.GetItem(idx);
                if (confused) {
                    item.State = ItemState.is_Cursed;
                } else {
                    item.State = ItemState.is_Normal;
                }
            }

            if (confused) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ScrollWasCurseAllItems));
            } else {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ScrollWasDispelHexFromAllItems));
            }
        }

        public static void e_Displacement(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            ((NWCreature)ext.GetParam(EffectParams.ep_Creature)).MoveRnd();
        }

        public static void e_DoorTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            int id = (creature.CurrentField).Layer.EntryID;
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
            creature.TransferTo(id, -1, -1, -1, -1, StaticData.MapArea, true, false);
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFallThroughTrapDoor));
        }

        public static void e_Draining(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_UseBegin:
                    creature.InitEffect(effectID, source, EffectAction.ea_EachTurn);
                    break;
                case InvokeMode.im_UseEnd:
                    creature.DoneEffect(effectID, source);
                    break;
                case InvokeMode.im_ItSelf:
                    creature.ApplyDamage(RandomHelper.GetBoundedRnd(1, 3), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_DrainedByRing));
                    break;
            }
        }

        public static void e_DrawLife(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_TouchNowDrawsEnergy));
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_SpellAlreadyEmpowers));
        }

        public static void e_Embalming(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("pulpy mummy");
            AuxUtils.ExStub("charred mummy");
            AuxUtils.ExStub("You cannot embalm ");

            Item aExtItem = (Item)ext.GetParam(EffectParams.ep_Item);

            if (aExtItem.CLSID != GlobalVars.iid_DeadBody) {
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_ThisNotDeadbody));
            } else {
                NWCreature mon = (NWCreature)aExtItem.Contents.GetItem(0);
                aExtItem.CLSID = GlobalVars.iid_Mummy;
                aExtItem.Weight = (((float)mon.Weight * 0.1f));
                string cs = mon.GetDeclinableName(Number.nSingle, Case.cGenitive);
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.Format(RS.rs_MummyCreated, new object[]{ cs }));
            }
        }

        private static bool EnchantResult(NWCreature creature, Item extItem, bool value)
        {
            int temp;
            if (value) {
                temp = RS.rs_ItemGlows;
            } else {
                temp = RS.rs_NewfoundBeauty;
            }
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(temp, new object[]{ extItem.Name }));
            return value;
        }

        public static void e_Enchantment(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo:  + rs_ScrollHasNoApparentEffect");
            bool res = false;

            Item extItem = (Item)ext.GetParam(EffectParams.ep_Item);
            extItem.Identified = true;
            string sign = extItem.Entry.Sign;

            switch (extItem.Kind) {
                case ItemKind.ik_Armor:
                case ItemKind.ik_Ring:
                case ItemKind.ik_BluntWeapon:
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
                    {
                        res = EnchantResult(creature, extItem, true);

                        if (extItem.State == ItemState.is_Cursed) {
                            extItem.State = ItemState.is_Normal;
                        }

                        if (extItem.Kind != ItemKind.ik_Ring || (extItem.Kind == ItemKind.ik_Ring && (sign.Equals("Ring_Agility") || sign.Equals("Ring_Protection")))) {
                            switch (state) {
                                case ItemState.is_Normal:
                                    extItem.Bonus += RandomHelper.GetBoundedRnd(5, 15);
                                    break;
                                case ItemState.is_Blessed:
                                    extItem.Bonus += RandomHelper.GetBoundedRnd(10, 15);
                                    break;
                                case ItemState.is_Cursed:
                                    extItem.Bonus += RandomHelper.GetBoundedRnd(1, 5);
                                    break;
                            }
                        }
                        break;
                    }

                case ItemKind.ik_Potion:
                case ItemKind.ik_Scroll:
                    {
                        res = EnchantResult(creature, extItem, true);
                        extItem.State = ItemState.is_Blessed;
                        break;
                    }

                case ItemKind.ik_Wand:
                    {
                        res = EnchantResult(creature, extItem, true);
                        if (sign.Equals("Wand_Wishing")) {
                            extItem.Bonus++;
                        } else {
                            switch (state) {
                                case ItemState.is_Normal:
                                    extItem.Bonus += RandomHelper.GetBoundedRnd(5, 15);
                                    break;
                                case ItemState.is_Blessed:
                                    extItem.Bonus += RandomHelper.GetBoundedRnd(10, 15);
                                    break;
                                case ItemState.is_Cursed:
                                    extItem.Bonus += RandomHelper.GetBoundedRnd(1, 5);
                                    break;
                            }
                        }
                        break;
                    }

                case ItemKind.ik_Amulet:
                    {
                        if (sign.CompareTo("Amulet_Holding") != 0 && extItem.State == ItemState.is_Cursed) {
                            res = EnchantResult(creature, extItem, true);
                            extItem.State = ItemState.is_Normal;
                        }
                        break;
                    }

                default:
                    if (sign.Equals("Anvil")) {
                        res = EnchantResult(creature, extItem, true);
                        extItem.CLSID = GlobalVars.nwrDB.FindEntryBySign("PlatinumAnvil").GUID;
                    }
                    if (sign.Equals("GreyCube") || sign.Equals("BlueCube")) {
                        res = EnchantResult(creature, extItem, true);
                        extItem.CLSID = GlobalVars.nwrDB.FindEntryBySign("OrangeCube").GUID;
                    }
                    if (sign.Equals("Ocarina")) {
                        res = EnchantResult(creature, extItem, true);
                        extItem.CLSID = GlobalVars.nwrDB.FindEntryBySign("GlassOcarina").GUID;
                    }
                    break;
            }

            if (!res) {
                EnchantResult(creature, extItem, res);
            } else {
                // TODO: вывести сообщение о новых свойствах
            }
        }

        public static void e_Endurance(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            int cnt = 0;
            switch (state) {
                case ItemState.is_Normal:
                    cnt = RandomHelper.GetBoundedRnd(3, 10);
                    break;
                case ItemState.is_Blessed:
                    cnt = RandomHelper.GetBoundedRnd(8, 20);
                    break;
                case ItemState.is_Cursed:
                    cnt = -RandomHelper.GetBoundedRnd(1, 10);
                    break;
            }

            if (creature.IsPlayer) {
                if (cnt >= 0) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsYourselfMoreRobust));
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsDeadlyWeakness));
                }
            }

            creature.HPCur = creature.HPMax_Renamed + cnt;
            if (creature.HPCur <= 0) {
                creature.Death(BaseLocale.GetStr(RS.rs_Shriveled), null);
            }
        }

        public static void e_Entomb(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Eternal_Life(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Evocation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            ExtPoint pt = creature.GetNearestPlace(2, true);
            if (!pt.IsEmpty) {
                UniverseBuilder.Gen_Creature(creature.CurrentField, -1, pt.X, pt.Y, true);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_MonsterEvocated));
            }
        }

        public static void e_Experience(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            switch (state) {
                case ItemState.is_Normal:
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_YouFeelsYouselfExperienced, new object[]{ "" }));
                    creature.Experience = creature.Experience + 1;
                    break;

                case ItemState.is_Blessed:
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_YouFeelsYouselfExperienced, new object[]{ BaseLocale.GetStr(RS.rs_Much) }));
                    creature.Experience = creature.Experience + 2;
                    break;

                case ItemState.is_Cursed:
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsYouselfUnexperienced));
                    creature.Experience = creature.Experience - 1;
                    if (creature.Experience < 0) {
                        creature.Death(BaseLocale.GetStr(RS.rs_SuckedDry) + ((Item)source).GetDeclinableName(Number.nSingle, Case.cInstrumental, creature.Blindness), null);
                    }
                    break;
            }
        }

        public static void e_Extinction(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: проверить реализацию");
            AuxUtils.ExStub("You genocide idiots.");

            if (creature.Confused) {
                int id = GlobalVars.nwrGame.RndExtincted;
                if (id >= 0) {
                    GlobalVars.nwrGame.SetVolatileState(id, VolatileState.None);
                    //CreatureEntry cEntry = (CreatureEntry)GlobalVars.nwrDB.GetEntry(id);
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_YouAccidentallyCreateRace));
                }
            } else {
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_YouHavePowerOfGenocide));
                GlobalVars.nwrWin.HideInventory();
                GlobalVars.nwrWin.ShowInput(BaseLocale.GetStr(RS.rs_RaceToDestroy), ExtinctionAcceptProc);
            }
        }

        public static void e_EyesMissing(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Fading(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ");
        }

        public static void e_Famine(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_UseBegin:
                    creature.InitEffect(effectID, source, EffectAction.ea_EachTurn);
                    break;
                case InvokeMode.im_UseEnd:
                    creature.DoneEffect(effectID, source);
                    break;
                case InvokeMode.im_ItSelf:
                    if (creature.IsPlayer) {
                        ((Player)creature).Satiety = (short)(((Player)creature).Satiety - 10);
                    }
                    break;
            }
        }

        public static void e_Fennling(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            Item wand = (Item)ext.GetParam(EffectParams.ep_Item);
            if (wand.Kind != ItemKind.ik_Wand) {
                AuxUtils.ExStub("//nwrWin.ShowText(creature, rsList(]);");
                return;
            }

            Item wand2 = null;
            int num = creature.Items.Count;
            for (int i = 0; i < num; i++) {
                Item it = creature.Items.GetItem(i);

                if (it.Kind == ItemKind.ik_Wand && !it.Equals(wand)) {
                    wand2 = it;
                    break;
                }
            }

            if (wand2 == null) {
                AuxUtils.ExStub("//nwrWin.ShowText(creature, rsList(Нет аналогичной палочки]);");
            } else {
                wand.Bonus += wand2.Bonus;
                creature.Items.Remove(wand2);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_TransferIsSuccesful));
                AuxUtils.ExStub(">> They must be similar wands.");
            }
        }

        public static void e_Fever(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("//creature.Death(rsList(rs_Poisoned]);");

            switch (invokeMode) {
                case InvokeMode.im_Use:
                    if (creature.HasAffect(EffectID.eid_Fever)) {
                        creature.AddEffect(EffectID.eid_Fever, state, EffectAction.ea_EachTurn, true, BaseLocale.GetStr(RS.rs_YouveGetAFever));
                    }
                    break;

                case InvokeMode.im_ItSelf:
                    Effect e = creature.Effects.FindEffectByID(EffectID.eid_Fever);
                    if (e.Source == null) {
                        if (e.Duration == 1) {
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouSuccumbToYourIllness));
                            creature.Death(BaseLocale.GetStr(RS.rs_FelledByDisease), null);
                        } else {
                            if (AuxUtils.Chance(30)) {
                                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourFeverWeaksYou));
                                creature.ApplyDamage(RandomHelper.GetBoundedRnd(1, 7), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_FelledByDisease));
                            }
                        }
                    }
                    break;
            }
        }

        public static void e_FillVial(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            bool res = false;
            Item item = (Item)source;

            AbstractMap fld = creature.CurrentField;
            ExtPoint pt = (ExtPoint)ext.GetParam(EffectParams.ep_Place);

            if (!creature.IsNear(pt)) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(StaticData.dbEffectTarget[(int)EffectTarget.et_PlaceNear].Invalid));
            } else {
                NWTile tile = (NWTile)fld.GetTile(pt.X, pt.Y);

                if (tile != null) {
                    if (tile.BackBase == PlaceID.pid_Liquid && tile.Lake_LiquidID != 0) {
                        item.CLSID = tile.Lake_LiquidID;
                        res = true;
                    } else {
                        if (tile.ForeBase == PlaceID.pid_Well) {
                            item.CLSID = GlobalVars.nwrDB.FindEntryBySign("Potion_DrinkingWater").GUID;
                            res = true;
                        }
                    }
                }

                if (!res) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_WaterSourceNotFound));
                }
            }
        }

        public static void e_Fire(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            FireRay ray = new FireRay();
            ray.Exec(creature, EffectID.eid_Fire, invokeMode, ext, WandRayMsg(EffectID.eid_Fire, source));
        }

        // FIXME: unused!!!
        public static void e_FireStorm(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AbstractMap fld = creature.CurrentField;
            ExtPoint pt = (ExtPoint)ext.GetParam(EffectParams.ep_Place);
            NWCreature cr = (NWCreature)fld.FindCreature(pt.X, pt.Y);

            MapObject mapObj = new MapObject(GameSpace.Instance, fld);
            mapObj.InitByEffect(effectID);
            fld.Features.Add(mapObj);

            int dur = Effect.GetDuration(effectID, state, false);
            for (int i = 1; i <= dur; i++) {
                mapObj.NextSprite(pt.X, pt.Y);
                int val = RandomHelper.GetBoundedRnd(35, 65);
                if (cr.HasAffect(EffectID.eid_FireStorm)) {
                    cr.ApplyDamage(val, DamageKind.Radiation, null, "");
                }
                BaseSystem.Sleep(100);
            }
            fld.Features.Remove(mapObj);
        }

        public static void e_FireTrap(EffectID effectID, NWCreature creature, object source, ItemState itemState, InvokeMode invokeMode, EffectExt ext)
        {
            bool ashes = creature.Effects.FindEffectByID(EffectID.eid_Ashes) != null;
            bool resist = !creature.HasAffect(EffectID.eid_FireTrap);
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouEngulfedInFlames));

            if (!ashes && !resist) {
                if (creature.Effects.FindEffectByID(EffectID.eid_Burns) == null) {
                    creature.AddEffect(EffectID.eid_Burns, ItemState.is_Normal, EffectAction.ea_EachTurn, false, BaseLocale.GetStr(RS.rs_YouBurned));
                    creature.ApplyDamage(RandomHelper.GetBoundedRnd(25, 45), DamageKind.Radiation, null, BaseLocale.GetStr(RS.rs_KilledByFireTrap));
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouTooBadlyBurned));
                    creature.Death(BaseLocale.GetStr(RS.rs_KilledByFireTrap), null);
                }
            }

            if (!ashes) {
                bool scrBurns = false;

                ItemsList list = creature.Items;
                for (int i = list.Count - 1; i >= 0; i--) {
                    Item it = list.GetItem(i);
                    if (it.Kind == ItemKind.ik_Scroll && AuxUtils.Chance(35)) {
                        list.Remove(it);
                        scrBurns = true;
                    }
                }

                if (scrBurns) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_SomeScrollsBurnup));
                }
            }
        }

        public static void e_FireVision(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: A crimson ray comes out of your eyes.");
            AuxUtils.ExStub("todo: A crimson ray hits the ...");

            FireVisionRay ray = new FireVisionRay();
            ray.Exec(creature, EffectID.eid_FireVision, invokeMode, ext, BaseLocale.GetStr(RS.rs_FireVisionExec));
        }

        public static void e_Flame(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            bool ashes = creature.Effects.FindEffectByID(EffectID.eid_Ashes) != null || creature.GetAbility(AbilityID.Resist_Heat) > 0;
            int val = 0;
            if (state != ItemState.is_Normal) {
                if (state != ItemState.is_Blessed) {
                    if (state == ItemState.is_Cursed) {
                        val = RandomHelper.GetBoundedRnd(2, 7);
                    }
                } else {
                    val = RandomHelper.GetBoundedRnd(2, 7);
                }
            } else {
                val = RandomHelper.GetBoundedRnd(2, 15);
            }

            if (!ashes) {
                string s = "";
                if (state != ItemState.is_Normal) {
                    if (state != ItemState.is_Blessed) {
                        if (state == ItemState.is_Cursed) {
                            s = BaseLocale.GetStr(RS.rs_FlameGrey);
                        }
                    } else {
                        s = BaseLocale.GetStr(RS.rs_FlameWhite);
                    }
                } else {
                    s = BaseLocale.GetStr(RS.rs_FlameYellow);
                }

                GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_FlameComesFromScroll, new object[]{ s }));

                if (state != ItemState.is_Normal) {
                    if (state != ItemState.is_Blessed) {
                        if (state == ItemState.is_Cursed) {
                            creature.HPMax = creature.HPMax_Renamed - val;
                        }
                    } else {
                        creature.HPMax = creature.HPMax_Renamed + val;
                    }
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveDamagedByFire));
                    creature.ApplyDamage(val, DamageKind.Radiation, null, BaseLocale.GetStr(RS.rs_BurnedToDeath));
                }
            }

            if (creature.Confused) {
                ExtRect r = ExtRect.Create(creature.PosX - 1, creature.PosY - 1, creature.PosX + 1, creature.PosY + 1);
                ExtList<LocatedEntity> list = creature.CurrentField.Creatures.SearchListByArea(r);

                int num = list.Count;
                for (int i = 0; i < num; i++) {
                    NWCreature cr = (NWCreature)list[i];
                    if (cr.HasAffect(EffectID.eid_Flame)) {
                        cr.ApplyDamage(val, DamageKind.Radiation, null, "");
                    }
                }
                list.Dispose();
            }
        }

        public static void e_Flaying(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            FlayingRay ray = new FlayingRay();
            ray.Exec(creature, EffectID.eid_Flaying, invokeMode, ext, BaseLocale.GetStr(RS.rs_TheRayOfFlayingIsZapped));
        }

        public static void e_Flood(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            DungeonRoom room = (DungeonRoom)ext.GetParam(EffectParams.ep_DunRoom);
            if (room != null) {
                AuxUtils.ExStub("todo: заблокировать двери, смерть утопающих");

                ExtRect r = room.Area.Clone();
                r.Inflate(-1, -1);

                AbstractMap map = room.Map;
                while ((int)map.GetTile(r.Left, r.Top).Background == PlaceID.pid_Water) {
                    r.Inflate(-1, -1);
                }

                for (int x = r.Left; x <= r.Right; x++) {
                    for (int y = r.Top; y <= r.Bottom; y++) {
                        BaseTile tile = map.GetTile(x, y);
                        if (r.IsBorder(x, y)) {
                            tile.Background = (ushort)PlaceID.pid_Water;
                        }

                        NWCreature cr = (NWCreature)map.FindCreature(x, y);
                        cr.Drown();
                    }
                }
            }
        }

        public static void e_FluteUse(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            Item flute = (Item)source;

            bool good = flute.Bonus > 0;
            if (good) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_PlayQuietTune));
            } else {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_PlayRestlessTune));
            }

            if (creature.GetAbility(AbilityID.Ab_MusicalAcuity) <= 0 && good) {
                flute.Bonus--;
            }

            ExtRect r = ExtRect.Create(creature.PosX - 3, creature.PosY - 3, creature.PosX + 3, creature.PosY + 3);
            ExtList<LocatedEntity> list = (creature.CurrentField).Creatures.SearchListByArea(r);

            int num = list.Count;
            for (int i = 0; i < num; i++) {
                NWCreature cr = (NWCreature)list[i];
                if (!cr.Equals(creature)) {
                    RaceID race = cr.Entry.Race;
                    if (race == RaceID.crDefault || race == RaceID.crHuman) {
                        byte lc = creature.Alignment.GetLC();
                        if (good) {
                            cr.Alignment = AlignmentEx.GenAlignment(lc, AlignmentEx.am_Mask_GENeutral);
                        } else {
                            cr.Alignment = AlignmentEx.GenAlignment(lc, AlignmentEx.am_Mask_Evil);
                        }
                    }
                }
            }
            list.Dispose();
        }

        private static void CureStoning(NWCreature creature, object source)
        {
            // check-ok
            Effect ef = creature.Effects.FindEffectByID(EffectID.eid_Stoning);
            if (ef != null) {
                creature.Effects.Remove(ef);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourGreyColorFades));
            } else {
                if (source != null) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_WhyDidYouEatThat));
                }
            }
        }

        public static void e_FoodEat(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: messages");
            Item item = (Item)source;
            ItemKind kind = item.Kind;

            if (kind != ItemKind.ik_DeadBody) {
                if (kind == ItemKind.ik_Food) {
                    string foodSign = item.Entry.Sign;

                    if (foodSign.Equals("BleachedRoot")) {
                        AuxUtils.ExStub("msg-none");
                    } else if (foodSign.Equals("BlackMushroom")) {
                        // check-ok
                        creature.SetSkill(SkillID.Sk_FireVision, 100);
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourEyesBleed));
                        creature.ApplyDamage(RandomHelper.GetBoundedRnd(0, 20), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_KilledByMushroom));
                    } else if (foodSign.Equals("ClayLump")) {
                        // check-ok
                        CureStoning(creature, source);
                    } else if (foodSign.Equals("GnarledRoot")) {
                        AuxUtils.ExStub("msg-none");
                    } else if (foodSign.Equals("GreenLump")) {
                        // check-ok
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_LumpTastedWoefullyBad));
                    } else if (foodSign.Equals("GreenMushroom")) {
                        // check-ok
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_TastedLikeDirt));
                    } else if (foodSign.Equals("MagicCookie")) {
                        AuxUtils.ExStub("msg-none");
                    } else if (foodSign.Equals("MottledMushroom")) {
                        // check-ok
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_WasDelicious, new object[]{ item.Name }));
                        AuxUtils.ExStub("todo: \"приятен на вкус\", но наносит 30-40 повреждений несколько ходов спустя");
                    } else if (foodSign.Equals("RedMushroom")) {
                        // check-ok
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouGoBlind));
                    } else if (foodSign.Equals("SpeckledGrowth")) {
                        AuxUtils.ExStub("msg-partially");
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_FungusTastesStrange));
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ShapesMesmerizingFlow));
                        AuxUtils.ExStub("todo: это сообщение вызывается почти каждый ход, пока глюки,используются какие попало предметы, все встречные твари получаютдикие имена и т.п.}");
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouDroolHappily));
                    } else if (foodSign.Equals("SpongyMass")) {
                        AuxUtils.ExStub("msg-none");
                        creature.SetAbility(AbilityID.Ab_SixthSense, 0);
                    } else if (foodSign.Equals("StrangeHerb")) {
                        AuxUtils.ExStub("msg-none");
                    } else if (foodSign.Equals("Urn")) {
                        switch (RandomHelper.GetRandom(7)) {
                            case 0:
                                {
                                    creature.Strength++;
                                    if (creature.IsPlayer) {
                                        ((Player)creature).Satiety = (short)(((Player)creature).Satiety + 10);
                                    }
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThisUrnContains) + BaseLocale.GetStr(RS.rs_Urn_GreenPowder));
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsAPower));
                                    break;
                                }
                            case 1:
                                {
                                    if (AuxUtils.Chance(10)) {
                                        creature.AddEffect(EffectID.eid_ThirdSight, ItemState.is_Normal, EffectAction.ea_Instant, false, "");
                                    }
                                    if (creature.IsPlayer) {
                                        ((Player)creature).Satiety = (short)(((Player)creature).Satiety + 10);
                                    }
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThisUrnContains) + BaseLocale.GetStr(RS.rs_Urn_OrangePowder));
                                    break;
                                }
                            case 2:
                                {
                                    if (creature.IsPlayer) {
                                        ((Player)creature).Satiety = (short)(((Player)creature).Satiety + 0);
                                    }
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThisUrnContains) + BaseLocale.GetStr(RS.rs_Urn_Nothing));
                                    break;
                                }
                            case 3:
                                {
                                    if (creature.IsPlayer) {
                                        ((Player)creature).Satiety = (short)(((Player)creature).Satiety + 200);
                                    }
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThisUrnContains) + BaseLocale.GetStr(RS.rs_Urn_Meat));
                                    break;
                                }
                            case 4:
                                {
                                    if (creature.IsPlayer) {
                                        ((Player)creature).Satiety = (short)(((Player)creature).Satiety + 300);
                                    }
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThisUrnContains) + BaseLocale.GetStr(RS.rs_Urn_Food));
                                    break;
                                }
                            case 5:
                                {
                                    if (creature.IsPlayer) {
                                        ((Player)creature).Satiety = (short)(((Player)creature).Satiety - 10);
                                    }
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThisUrnContains) + BaseLocale.GetStr(RS.rs_Urn_RottenFood) + " " + BaseLocale.GetStr(RS.rs_YouGag));
                                    break;
                                }
                            case 6:
                                {
                                    if (creature.IsPlayer) {
                                        ((Player)creature).Satiety = (short)(((Player)creature).Satiety + 0);
                                    }
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThisUrnContains) + BaseLocale.GetStr(RS.rs_Urn_SmallerUrn));
                                    break;
                                }
                        }
                    } else if (foodSign.Equals("YellowMushroom")) {
                        // check-ok
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_Ulch));
                    }
                }
            } else {
                NWCreature dead = (NWCreature)item.Contents.GetItem(0);
                if (dead == null) {
                    throw new Exception("Assertion failure #1");
                }

                string deadSign = dead.Entry.Sign;

                if (deadSign.Equals("AirGhola")) {
                    // check-ok
                    if (AuxUtils.Chance(30)) {
                        creature.Strength--;
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsAWeakness));
                    }
                } else if (deadSign.Equals("Anssk")) {
                    // check-ok
                    creature.SetAbility(AbilityID.Ab_SixthSense, creature.GetAbility(AbilityID.Ab_SixthSense) + 1);
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_MinorAcheEmanates));
                } else if (deadSign.Equals("Basilisk")) {
                    // check-ok
                    if (creature.GetAbility(AbilityID.Resist_Petrification) <= 0) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouTurnToStone));
                        AuxUtils.ExStub("Cockatrice?");
                        creature.Death(BaseLocale.GetStr(RS.rs_KilledByGluttony), null);
                    }
                } else if (deadSign.Equals("Bloodslug")) {
                    // check-ok
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_Ughh));
                } else if (deadSign.Equals("Blur")) {
                    // check-ok
                    e_Speedup(EffectID.eid_Speedup, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                } else if (deadSign.Equals("Breleor")) {
                    // check-ok
                    creature.SetSkill(SkillID.Sk_Cartography, creature.GetSkill(SkillID.Sk_Cartography) + 1);
                    creature.SetSkill(SkillID.Sk_DimensionTravel, creature.GetSkill(SkillID.Sk_DimensionTravel) + 1);
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_WorldComesIntoFocus));
                } else if (deadSign.Equals("BrownBat")) {
                    // check-ok
                    AuxUtils.ExStub("msg-none");
                    e_Confusion(EffectID.eid_Confusion, creature, source, state, invokeMode, null);
                } else if (deadSign.Equals("Cockatrice")) {
                    // check-ok
                    if (creature.GetAbility(AbilityID.Resist_Petrification) <= 0) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_TouchesYourSkin, new object[]{ item.Name }));
                        creature.Death(BaseLocale.Format(RS.rs_Touched, new object[]{ item.Name }), null);
                    }
                } else if (deadSign.Equals("Corpse")) {
                    // check-ok
                    creature.AddEffect(EffectID.eid_Diseased, state, EffectAction.ea_Persistent, false, "");
                } else if (deadSign.Equals("Dreg")) {
                    // check-ok
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThatWasPoisonous));
                    if (creature.GetAbility(AbilityID.Resist_Acid) <= 0) {
                        e_Confusion(EffectID.eid_Confusion, creature, source, state, invokeMode, null);
                        creature.ApplyDamage(RandomHelper.GetBoundedRnd(2, 15), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_KilledByGluttony));
                        creature.Strength--;
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsAWeakness));
                    } else {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouSeemUnaffected));
                    }
                } else if (deadSign.Equals("Faleryn")) {
                    // check-ok
                    creature.DropAll();
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourArmorFallsOff));

                    int crID = GlobalVars.nwrDB.FindEntryBySign("Faleryn").GUID;
                    creature.InitEx(crID, true, false);

                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_YouShallNowBeKnownAsX, new object[]{ creature.Name }));
                } else if (deadSign.Equals("FireDragon")) {
                    AuxUtils.ExStub("msg-none");
                    creature.SetAbility(AbilityID.Resist_Heat, creature.GetAbility(AbilityID.Resist_Heat) + 1);
                } else if (deadSign.Equals("FireGiant") || deadSign.Equals("Minion")) {
                    // check-ok
                    if (creature.GetAbility(AbilityID.Resist_Heat) <= 0) {
                        creature.ApplyDamage(RandomHelper.GetBoundedRnd(16, 25), DamageKind.Radiation, null, BaseLocale.GetStr(RS.rs_ItBurnsYourThroat));
                    } else {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouSeemUnaffected));
                    }
                } else if (deadSign.Equals("Fyleisch")) {
                    // check-ok
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_SwallowingKnives));
                    creature.Death(BaseLocale.GetStr(RS.rs_KilledByGluttony), null);
                } else if (deadSign.Equals("Glard") || deadSign.Equals("Phausq") || deadSign.Equals("Serpent")) {
                    // check-ok
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThatWasPoisonous));
                    if (creature.GetAbility(AbilityID.Resist_Poison) <= 0) {
                        if (AuxUtils.Chance(5) && !deadSign.Equals("Serpent")) {
                            creature.Death(BaseLocale.GetStr(RS.rs_KilledByGluttony), null);
                        } else {
                            if (deadSign.Equals("Glard")) {
                                creature.SetAbility(AbilityID.Resist_Poison, creature.GetAbility(AbilityID.Resist_Poison) + 1);
                            }
                            e_Confusion(EffectID.eid_Confusion, creature, source, state, invokeMode, null);
                            creature.ApplyDamage(RandomHelper.GetBoundedRnd(2, 15), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_KilledByGluttony));
                            creature.Strength--;
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsAWeakness));
                        }
                    } else {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouSeemUnaffected));
                    }
                } else if (deadSign.Equals("Gorm")) {
                    // check-ok
                    if (creature.GetAbility(AbilityID.Resist_Acid) <= 0) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NotGoodIdea));
                        creature.Death(BaseLocale.GetStr(RS.rs_KilledByGluttony), null);
                    }
                } else if (deadSign.Equals("HelDragon")) {
                    // check-ok
                    creature.SetAbility(AbilityID.Resist_Petrification, creature.GetAbility(AbilityID.Resist_Petrification) + 1);
                    creature.Constitution += RandomHelper.GetBoundedRnd(1, 5);
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_MeatHumms));
                } else if (deadSign.Equals("IceDragon") || deadSign.Equals("IceSphere")) {
                    AuxUtils.ExStub("msg-none");
                    creature.SetAbility(AbilityID.Resist_Cold, creature.GetAbility(AbilityID.Resist_Cold) + 1);
                } else if (deadSign.Equals("IvyCreeper")) {
                    CureEffect(creature, EffectID.eid_BrainScarring, BaseLocale.GetStr(RS.rs_BrainScarringIsCured));
                    CureEffect(creature, EffectID.eid_Hallucination, BaseLocale.GetStr(RS.rs_HallucinationIsCured));
                    CureEffect(creature, EffectID.eid_Confusion, BaseLocale.GetStr(RS.rs_ConfusionIsCured));
                    CureEffect(creature, EffectID.eid_Insanity, "");
                } else if (deadSign.Equals("Jagredin")) {
                    creature.ApplyDamage(RandomHelper.GetBoundedRnd(24, 109), DamageKind.Physical, null, "");
                } else if (deadSign.Equals("Knilb") || deadSign.Equals("Nymph")) {
                    AuxUtils.ExStub("msg-none");
                    creature.AddEffect(EffectID.eid_Relocation, ItemState.is_Normal, EffectAction.ea_RandomTurn, false, "");
                    AuxUtils.ExStub("todo: добавить модификатор неизлечимости");
                } else if (deadSign.Equals("LowerDwarf")) {
                    // check-ok
                    creature.SetAbility(AbilityID.Resist_Heat, 0);
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_LakeOfFire));
                } else if (deadSign.Equals("Mudman")) {
                    // check-ok
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_TastedGritty, new object[]{ dead.GetDeclinableName(Number.nSingle, Case.cGenitive) }));
                } else if (deadSign.Equals("PhantomAsp")) {
                    // check-ok
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThatWasPoisonous));
                    if (creature.GetAbility(AbilityID.Resist_Poison) <= 0) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_PoisonWasDeadly));
                        creature.Death(BaseLocale.GetStr(RS.rs_KilledByGluttony), null);
                    }
                } else if (deadSign.Equals("Preden")) {
                    // check-ok
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_Ughh));
                } else {
                    if (deadSign.Equals("Retchweed")) {
                        // check-ok
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouRetch));
                    } else if (deadSign.Equals("Sandiff")) {
                        // check-ok
                        if (creature.GetAbility(AbilityID.Resist_Acid) <= 0) {
                            creature.ApplyDamage(RandomHelper.GetBoundedRnd(0, 45), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_AcidicMeatBurns));
                        }
                    } else if (deadSign.Equals("Shade")) {
                        // check-ok
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_YouGnawOnBones, new object[]{ dead.GetDeclinableName(Number.nSingle, Case.cGenitive) }));
                    } else if (deadSign.Equals("Slinn")) {
                        // check-ok
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_MeatRots));
                    } else if (deadSign.Equals("Stalker")) {
                        AuxUtils.ExStub("msg-none");
                        e_Confusion(EffectID.eid_Confusion, creature, source, state, invokeMode, ext);
                        AuxUtils.ExStub("todo: усилить в два раза");
                        e_Invisibility(EffectID.eid_Invisibility, creature, source, state, invokeMode, ext);
                        AuxUtils.ExStub("todo: ослабить в 5 раз 110-210 -> 35-41");
                    } else if (deadSign.Equals("StunWorm")) {
                        // check-ok
                        creature.ApplyDamage(RandomHelper.GetBoundedRnd(11, 20), DamageKind.Radiation, null, BaseLocale.GetStr(RS.rs_YouFeelNumb));
                    } else if (deadSign.Equals("Wight")) {
                        // check-ok
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelHalfDead));
                        creature.HPMax = creature.HPMax_Renamed / 2;
                        creature.HPCur = creature.HPMax_Renamed;
                        if (creature.HPCur <= 1) {
                            creature.Death(BaseLocale.GetStr(RS.rs_Starved), null);
                        }
                    } else if (deadSign.Equals("Wraith")) {
                        // check-ok
                        e_Experience(EffectID.eid_Experience, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                    }
                }
            }
        }

        public static void e_Fragile(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_FrostTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            bool insul = creature.Effects.FindEffectByID(EffectID.eid_Insulation) != null;
            bool resist = !creature.HasAffect(EffectID.eid_FrostTrap);

            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouCoveredInFrost));
            AuxUtils.ExStub("todo: rs_KilledByIceTrap!!!");

            if (!insul && !resist) {
                creature.ApplyDamage(RandomHelper.GetBoundedRnd(25, 45), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_YouTooBadlyFreezed));
            }

            if (!insul) {
                ItemsList list = creature.Items;

                bool brk = false;
                int num = list.Count;
                for (int i = 0; i < num; i++) {
                    Item it = list.GetItem(i);
                    if (it.Kind == ItemKind.ik_Potion && AuxUtils.Chance(51)) {
                        list.Remove(it);
                        brk = true;
                    }
                }

                if (brk) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_SomePotionsFreeze));
                }
            }
        }

        public static void e_FunnelHurl(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: not joined!!!");
            AuxUtils.ExStub("tree");
            AuxUtils.ExStub("dead tree");
            AuxUtils.ExStub("The funnel engulfs the ");
            AuxUtils.ExStub(".");
            AuxUtils.ExStub("the ");
            AuxUtils.ExStub("The funnels destroy each other.");
            AuxUtils.ExStub("The funnel kills ");
            AuxUtils.ExStub("The funnel envelops ");
            AuxUtils.ExStub("The funnel destroys ");
            AuxUtils.ExStub("The gale-force winds shred you to bits.");
            AuxUtils.ExStub("Torn apart in a tornado");
            AuxUtils.ExStub("The funnel spews out the dead tree.");
            AuxUtils.ExStub("The dead tree collides with ");
            AuxUtils.ExStub("The dead tree slams into you.");
            AuxUtils.ExStub("Hit by a flying tree");
            AuxUtils.ExStub("The funnel releases ");
            AuxUtils.ExStub("The funnel hurls ");
            AuxUtils.ExStub("The funnel flings you like a rag doll.");
        }

        public static void e_Genesis(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub(" -> rCreature.TCreature.DoTurn ");
            if (invokeMode != InvokeMode.im_UseBegin) {
                if (invokeMode == InvokeMode.im_UseEnd) {
                    creature.DoneEffect(effectID, source);
                }
            } else {
                creature.InitEffect(effectID, source, EffectAction.ea_Persistent);
            }
        }

        public static void e_CheckGeology(EffectID effectID, NWCreature creature, object source, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode == InvokeMode.im_ItSelf) {
                ext.ReqParams = new EffectParams(EffectParams.ep_Place, EffectParams.ep_TileID);
            } else {
                ext.ReqParams = new EffectParams();
            }
        }

        public static void e_Geology(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                    creature.SetSkill(SkillID.Sk_Terraforming, creature.GetSkill(SkillID.Sk_Terraforming) + 1);
                    if (creature.GetSkill(SkillID.Sk_Terraforming) == 1) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveGetATerraformingSkill));
                    } else {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ReshapePowerGrows));
                    }
                    break;

                case InvokeMode.im_ItSelf:
                    NWField fld = creature.CurrentField;
                    ExtPoint pt = (ExtPoint)ext.GetParam(EffectParams.ep_Place);
                    NWTile tile = (NWTile)fld.GetTile(pt.X, pt.Y);
                    Geology_PrepareTile(fld, creature, ext, pt, tile);
                    fld.Normalize();
                    break;
            }
        }

        public static void e_GjallUse(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            GlobalVars.nwrGame.DoRagnarok();
        }

        public static void e_GolemCreation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("pulpy mass");
            AuxUtils.ExStub("charred wraith");
            AuxUtils.ExStub("You cannot make a golem of ");
            AuxUtils.ExStub(" is ");
            AuxUtils.ExStub(" are ");
            AuxUtils.ExStub("grafted onto your ");
            AuxUtils.ExStub("todo: После убийства голема остается \"труп\", называется \"pulpy mass\"");

            Item aExtItem = (Item)ext.GetParam(EffectParams.ep_Item);
            if (aExtItem.CLSID != GlobalVars.iid_DeadBody) {
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_ThisNotDeadbody));
            } else {
                NWCreature mon = AnimateDeadBody(creature, aExtItem);
                string cs = mon.GetDeclinableName(Number.nSingle, Case.cGenitive);
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.Format(RS.rs_GolemCreated, new object[]{ cs }));
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_GolemUncontrolled));
            }
        }

        public static void e_GrapplingHookUse(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.Effects.FindEffectByID(EffectID.eid_PhaseTrap) == null) {
                GrapplingHookRay ray = new GrapplingHookRay();
                ray.Exec(creature, EffectID.eid_GrapplingHookUse, invokeMode, ext, "");
                AuxUtils.ExStub("rsList(rs_StoningRayIsZapped]");
            }
        }

        public static void e_Hallucination(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: You are temporarily slack-jawed and unable to move.'->'You can move again.");

            switch (invokeMode) {
                case InvokeMode.im_Use:
                    if (creature.HasAffect(EffectID.eid_Hallucination)) {
                        creature.AddEffect(EffectID.eid_Hallucination, state, EffectAction.ea_Persistent, true, BaseLocale.GetStr(RS.rs_YouSeeAHallucination));
                    }
                    break;
                case InvokeMode.im_ItSelf:
                    break;
                case InvokeMode.im_FinAction:
                    break;
            }

        }

        public static void e_Hardening(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            AuxUtils.ExStub("todo:  remove stoning?");
            AuxUtils.ExStub("todo:  это эффект sentinel'я - окаменение");
            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode == InvokeMode.im_ItSelf) {
                    Effect e = creature.Effects.FindEffectByID(EffectID.eid_Hardening);
                    if (e.Source == null && e.Duration == 1) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouAreStatue));
                        creature.Death(BaseLocale.GetStr(RS.rs_ImmortalizedInStone), null);
                    }
                }
            } else {
                if (creature.HasAffect(EffectID.eid_Hardening)) {
                    creature.AddEffect(EffectID.eid_Hardening, state, EffectAction.ea_EachTurn, true, BaseLocale.GetStr(RS.rs_YourBodyIsHardening));
                }
            }
        }

        public static void e_Hastening(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWCreature c = (NWCreature)ext.GetParam(EffectParams.ep_Creature);
            if (c.HasAffect(EffectID.eid_Hastening)) {
                c.AddEffect(EffectID.eid_Speedup, state, EffectAction.ea_Persistent, false, "");
            }
        }

        public static void e_HealInability(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ");
        }

        public static void e_Healing(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            e_Cure(EffectID.eid_Cure, (NWCreature)ext.GetParam(EffectParams.ep_Creature), source, ItemState.is_Normal, InvokeMode.im_ItSelf, ext);
        }

        private static void HeatCloud(NWField field, int aX, int aY)
        {
            RandomHelper.Randomize();
            for (int y = aY - 3; y <= aY + 3; y++) {
                for (int x = aX - 3; x <= aX + 3; x++) {
                    if (MathHelper.Distance(aX, aY, x, y) <= 3) {
                        MapObject mapObj = new MapObject(GameSpace.Instance, field);
                        mapObj.SetPos(x, y);
                        mapObj.InitByEffect(EffectID.eid_Heat);
                        mapObj.IsNeedUpdate = true;
                        mapObj.FrameIndex = 1 + RandomHelper.GetRandom(3);
                        mapObj.Loops = 5;
                        field.Features.Add(mapObj);
                    }
                }
            }
        }

        public static void e_Heat(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode == InvokeMode.im_ItSelf) {
                    NWField f = creature.CurrentField;

                    int px = creature.PosX;
                    int py = creature.PosY;

                    HeatCloud(f, px, py);

                    AuxUtils.ExStub("todo:  this message only for skill \"heat radiation\"");
                    AuxUtils.ExStub("todo:   other skills and effects - other messages");
                    AuxUtils.ExStub("todo:   -> separate ");
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_BurnCloudStrikesNearYou));

                    for (int y = py - 3; y <= py + 3; y++) {
                        for (int x = px - 3; x <= px + 3; x++) {
                            NWTile tile = (NWTile)f.GetTile(x, y);
                            if (tile != null && tile.ForeBase == PlaceID.pid_Tree) {
                                tile.Foreground = PlaceID.pid_Undefined;
                                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_TreeBursts));
                            }

                            NWCreature c = (NWCreature)f.FindCreature(x, y);
                            if (c != null && !c.Equals(creature) && c.HasAffect(EffectID.eid_Heat)) {
                                string msg = BaseLocale.Format(RS.rs_IsSlain, new object[]{ c.GetDeclinableName(Number.nSingle, Case.cNominative) });
                                c.ApplyDamage(RandomHelper.GetBoundedRnd(21, 40), DamageKind.Radiation, null, msg);
                            }
                        }
                    }
                }
            } else {
                creature.SetSkill(SkillID.Sk_HeatRadiation, creature.GetSkill(SkillID.Sk_HeatRadiation) + 1);
                if (creature.GetSkill(SkillID.Sk_HeatRadiation) == 1) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouGetAHeatRadiationSkill));
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourBodyGrowsWarmer));
                }
            }
        }

        public static void e_HPEnlarge(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("msg-none");
            AuxUtils.ExStub("green mushroom");
            creature.HPMax = creature.HPMax_Renamed + 1;
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveBecomeMoreRobust));
        }

        public static void e_Husbandry(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWCreature cr = (NWCreature)ext.GetParam(EffectParams.ep_Creature);
            if (!cr.Equals(creature) && !cr.Entry.Flags.Contains(CreatureFlags.esMind)) {
                cr.Clone(false);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_BreedingIsSuccesful));
            } else {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourAttemptsAreFutile));
            }
        }

        public static void e_Ice(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub(" The ... ray hits the <monster>.");
            IceRay ray = new IceRay();
            ray.Exec(creature, EffectID.eid_Ice, invokeMode, ext, WandRayMsg(EffectID.eid_Ice, source));
        }

        public static void e_Identify(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: question msg: rs_EmpowerWhich");
            if (creature.Confused || state == ItemState.is_Cursed) {
                int num = creature.Items.Count;
                for (int i = 0; i < num; i++) {
                    creature.Items.GetItem(i).Identified = false;
                }
            } else {
                if (state != ItemState.is_Normal) {
                    if (state == ItemState.is_Blessed) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_BuzzingSound));

                        int num2 = creature.Items.Count;
                        for (int i = 0; i < num2; i++) {
                            creature.Items.GetItem(i).Identified = true;
                        }
                    }
                } else {
                    Item aExtItem = (Item)ext.GetParam(EffectParams.ep_Item);
                    aExtItem.Identified = true;
                }
            }
        }

        public static void e_Idle(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // stub for copy/paste
            switch (state) {
                case ItemState.is_Normal:
                    break;
                case ItemState.is_Blessed:
                    break;
                case ItemState.is_Cursed:
                    break;
            }

            // stub for copy/paste
            switch (invokeMode) {
                case InvokeMode.im_Use:
                    break;
                case InvokeMode.im_UseBegin:
                    break;
                case InvokeMode.im_UseEnd:
                    break;
                case InvokeMode.im_ItSelf:
                    break;
                case InvokeMode.im_FinAction:
                    break;
            }

            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NoHappens));
        }

        public static void e_IllusorySelf(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWCreature illusion = creature.Clone(true);
            if (illusion != null) {
                illusion.Illusion = true;
                ((SentientBrain)illusion.Brain).SetEscortGoal(creature, false);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveMadeAnIllusionOfYourself));
            }
        }

        public static void e_Immortal(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                case InvokeMode.im_ItSelf:
                    CreateMapObject(effectID, creature.CurrentField, creature.PosX, creature.PosY, true);
                    creature.AddEffect(EffectID.eid_Immortal, state, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_YouveGetASecondLife));
                    break;
            }
        }

        public static void e_Immunity(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_UseBegin:
                    creature.InitEffect(effectID, source, EffectAction.ea_Persistent);
                    break;
                case InvokeMode.im_UseEnd:
                    creature.DoneEffect(effectID, source);
                    break;
            }
        }

        public static void e_Impregnation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.HasAffect(EffectID.eid_Impregnation)) {
                AuxUtils.ExStub("todo: ???");
            }
        }

        public static void e_Imprisoning(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: target msg 'Imprison which?'");
            NWCreature victim = (NWCreature)ext.GetParam(EffectParams.ep_Creature);

            if (victim.Entry.Flags.HasIntersect(CreatureFlags.esSwimming, CreatureFlags.esPlant)) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_OrbHasNoPower));
            } else {
                if (victim.Imprisonable) {
                    victim.TransferTo(GlobalVars.Layer_Vanaheim, 0, 0, -1, -1, StaticData.MapArea, true, false);
                    if (creature.IsPlayer) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_ScreamsAndDisappearsInThickenedAir, new object[]{ victim.Name }));
                        if (((Player)creature).Morality <= -10 && creature.LayerID != GlobalVars.Layer_Vanaheim) {
                            creature.TransferTo(GlobalVars.Layer_Vanaheim, 0, 0, -1, -1, StaticData.MapArea, true, false);
                            string msg = BaseLocale.GetStr(RS.rs_SphereMiredYouInItselfAndDisappears);
                            GlobalVars.nwrWin.ShowText(creature, msg, new LogFeatures(LogFeatures.lfDialog));
                        }
                    }
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_TooMightyAndDoesntSubjectedToIt));
                }
            }
        }

        public static void e_Infravision(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Insanity(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                    AuxUtils.ExStub("todo: trace source by IDA");
                    creature.AddEffect(EffectID.eid_Insanity, state, EffectAction.ea_RandomTurn, false, BaseLocale.GetStr(RS.rs_YouBecomesInsane));
                    e_Hallucination(EffectID.eid_Hallucination, creature, source, state, invokeMode, ext);
                    break;

                case InvokeMode.im_ItSelf:
                    if (!creature.Prowling) {
                        e_Prowling(EffectID.eid_Insanity, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                    }
                    break;

                case InvokeMode.im_FinAction:
                    break;
            }
        }

        public static void e_Insulation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Intoxicate(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo:  remove confused?");
            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode == InvokeMode.im_ItSelf) {
                    AuxUtils.ExStub("todo:  incorrect code, replace by skipped turns for [rs_YouPassOut]}");
                }
            } else {
                if (creature.HasAffect(EffectID.eid_Intoxicate)) {
                    Effect ef = creature.Effects.FindEffectByID(EffectID.eid_Intoxicate);
                    if (ef == null) {
                        creature.AddEffect(EffectID.eid_Intoxicate, ItemState.is_Normal, EffectAction.ea_EachTurn, false, BaseLocale.GetStr(RS.rs_YouFeelIntoxicated));
                    } else {
                        ef.Duration += Effect.GetDuration(EffectID.eid_Intoxicate, ItemState.is_Normal, false);
                        if (ef.Duration > 160) {
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouHaveOverworkedYourLiver));
                            creature.Death(BaseLocale.GetStr(RS.rs_KilledByAlcoholAbuse), null);
                        } else {
                            if (ef.Duration > 120) {
                                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouGag));
                            } else {
                                if (ef.Duration > 80) {
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelMoreIntoxicated));
                                }
                            }
                        }
                    }
                }
            }
        }

        public static void e_Invisibility(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                    creature.AddEffect(EffectID.eid_Invisibility, state, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_YouAreInvisible));
                    break;

                case InvokeMode.im_UseBegin:
                    creature.InitEffect(EffectID.eid_Invisibility, source, EffectAction.ea_Persistent);
                    break;

                case InvokeMode.im_UseEnd:
                    creature.DoneEffect(EffectID.eid_Invisibility, source);
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouVisibleAgain));
                    break;

                case InvokeMode.im_ItSelf:
                    break;
            }
        }

        public static void e_Invulnerable(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode != InvokeMode.im_UseBegin) {
                    if (invokeMode == InvokeMode.im_UseEnd) {
                        creature.DoneEffect(EffectID.eid_Invulnerable, source);
                    }
                } else {
                    creature.InitEffect(EffectID.eid_Invulnerable, source, EffectAction.ea_Persistent);
                }
            } else {
                creature.AddEffect(EffectID.eid_Invulnerable, state, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_YouAreInvulnerable));
            }
        }

        public static void e_Knowledge(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("this scroll explains the art of ");
            AuxUtils.ExStub("swimming.");
            AuxUtils.ExStub("alchemy.");
            AuxUtils.ExStub("teleporting at will.");
            AuxUtils.ExStub("the swordsman.");
            AuxUtils.ExStub("identification.");
            AuxUtils.ExStub("telepathy.");
            AuxUtils.ExStub("map-making.");
            AuxUtils.ExStub("the blacksmith.");
            AuxUtils.ExStub("heat vision.");
            AuxUtils.ExStub("animal husbandry.");
            AuxUtils.ExStub("levitation.");
            AuxUtils.ExStub("mind control.");
            AuxUtils.ExStub("golem creation.");
            AuxUtils.ExStub("corpse preservation.");
            AuxUtils.ExStub("ventriloquism. (Amaze your friends) ");
            AuxUtils.ExStub("writing.");
            AuxUtils.ExStub("terraforming.");

            int i = RandomHelper.GetRandom(StaticData.dbTeachable.Length);
            int id = StaticData.dbTeachable[i].Id;
            if (StaticData.dbTeachable[i].Kind == TeachableKind.Ability) {
                AbilityID ab = (AbilityID)id;
                creature.SetAbility(ab, creature.GetAbility(ab) + 1);
            } else {
                SkillID sk = (SkillID)id;
                creature.SetSkill(sk, creature.GetSkill(sk) + 1);
            }
            AuxUtils.ExStub("You are now a fletcher.");
            AuxUtils.ExStub("You now hold sway over lesser creatures.");
            AuxUtils.ExStub("You learn how to write new scrolls.");
            AuxUtils.ExStub("limited ");
            AuxUtils.ExStub("You gain the ");
            AuxUtils.ExStub("skill of writing.");
            AuxUtils.ExStub("Your knowledge of writing increases.");
            if (StaticData.dbTeachable[i].CommentRS != RS.rs_Reserved) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(StaticData.dbTeachable[i].CommentRS));
            }
        }

        public static void e_LavaStrike(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;

            if (creature.Confused) {
                int id = GlobalVars.nwrDB.FindEntryBySign("BlueRock").GUID;
                int cnt = RandomHelper.GetBoundedRnd(2, 5);
                for (int i = 1; i <= cnt; i++) {
                    ExtPoint pt = creature.GetNearestPlace(3, false);
                    if (!pt.IsEmpty) {
                        Item item = new Item(GameSpace.Instance, fld);
                        item.CLSID = id;
                        item.Count = 1;
                        item.SetPos(pt.X, pt.Y);
                        item.Owner = fld;
                        fld.Items.Add(item, false);

                        NWCreature c = (NWCreature)fld.FindCreature(pt.X, pt.Y);
                        if (c != null) {
                            c.ApplyDamage(RandomHelper.GetBoundedRnd(20, 40), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_RocksAreRainedOnYourHead));
                        }
                    }
                }
            } else {
                int cnt = RandomHelper.GetBoundedRnd(2, 5);
                for (int i = 1; i <= cnt; i++) {
                    ExtPoint pt = creature.GetNearestPlace(3, false);
                    if (!pt.IsEmpty) {
                        NWTile tile = (NWTile)fld.GetTile(pt.X, pt.Y);
                        tile.Foreground = (ushort)PlaceID.pid_LavaPool;

                        NWCreature c = (NWCreature)fld.FindCreature(pt.X, pt.Y);
                        if (c != null) {
                            RaceID race = c.Entry.Race;
                            if (race == RaceID.crDefault || race == RaceID.crHuman) {
                                c.Death("", null);
                            }
                        }
                    }
                }

                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_LavaIsRained));
            }
        }

        public static void e_LavaTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            bool ashes = creature.Effects.FindEffectByID(EffectID.eid_Ashes) != null;
            bool resist = !creature.HasAffect(EffectID.eid_LavaTrap);

            NWField fld = creature.CurrentField;

            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_GroundHereTurnsToLava));
            fld.GetTile(creature.PosX, creature.PosY).Background = (ushort)PlaceID.pid_Lava;
            fld.Normalize();

            if (!ashes && !resist) {
                if (creature.Effects.FindEffectByID(EffectID.eid_Burns) == null) {
                    creature.ApplyDamage(RandomHelper.GetBoundedRnd(15, 40), DamageKind.Physical, null, "");
                    creature.AddEffect(EffectID.eid_Burns, ItemState.is_Normal, EffectAction.ea_EachTurn, false, BaseLocale.GetStr(RS.rs_LavaBurnsYou));
                } else {
                    creature.Death(BaseLocale.GetStr(RS.rs_YouTooBadlyBurned), null);
                }
            }
        }

        public static void e_LazlulRopeUse(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            creature.MoveToUp();
        }

        public static void e_LegsMissing(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Light(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode == InvokeMode.im_ItSelf) {
                    AuxUtils.ExStub("action in TPlayer.DoTurn().ApplyLightEffect();");
                }
            } else {
                creature.AddEffect(EffectID.eid_Light, state, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_WandGlows));
            }
        }

        public static void e_LocusMastery(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("-> TEffect.Execute");
            if (invokeMode != InvokeMode.im_UseBegin) {
                if (invokeMode == InvokeMode.im_UseEnd) {
                    creature.DoneEffect(effectID, source);
                }
            } else {
                creature.InitEffect(effectID, source, EffectAction.ea_Persistent);
            }
        }

        public static void e_Lodestone(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;

            switch (RandomHelper.GetRandom(5)) {
                case 0:
                    {
                        int tx = RandomHelper.GetBoundedRnd(0, StaticData.FieldWidth - 1);
                        int ty = RandomHelper.GetBoundedRnd(0, StaticData.FieldHeight - 1);

                        NWTile tile = (NWTile)fld.GetTile(tx, ty);
                        tile.Foreground = (ushort)PlaceID.pid_Rock;

                        NWCreature cr = (NWCreature)fld.FindCreature(tx, ty);
                        if (cr != null && cr.Effects.FindEffectByID(EffectID.eid_Phase) == null) {
                            cr.Death(BaseLocale.GetStr(RS.rs_FusedInStone), null);
                        }
                        AuxUtils.ExStub("todo: message");
                        break;
                    }
                case 1:
                    {
                        int cnt = RandomHelper.GetBoundedRnd(2, 5);
                        for (int i = 1; i <= cnt; i++) {
                            ExtPoint pt = fld.GetNearestPlace(creature.PosX, creature.PosY, 5, true, new Movements(Movements.mkWalk, Movements.mkFly));
                            if (pt.IsEmpty) {
                                Logger.Write("EffectsFactory.e_Lodestone.1(): empty point");
                            } else {
                                UniverseBuilder.Gen_Creature(fld, -1, pt.X, pt.Y, true);
                            }
                        }
                        AuxUtils.ExStub("todo: message");
                        break;
                    }
                case 2:
                    {
                        int i = RandomHelper.GetRandom(creature.Items.Count);
                        creature.DeleteItem(creature.Items.GetItem(i));
                        AuxUtils.ExStub("todo: message");
                        break;
                    }
                case 3:
                    {
                        creature.Luck--;
                        AuxUtils.ExStub("todo: message");
                        break;
                    }
                case 4:
                    {
                        creature.ApplyDamage(RandomHelper.GetBoundedRnd(5, 27), DamageKind.Physical, null, "");
                        AuxUtils.ExStub("todo: message");
                        break;
                    }
            }
        }

        public static void e_Luck(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (state == ItemState.is_Cursed) {
                creature.Luck -= RandomHelper.GetBoundedRnd(1, 4);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_SkinBurns));
                creature.ApplyDamage(RandomHelper.GetBoundedRnd(1, 4), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_Consumed));
            } else {
                creature.Luck++;
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_HolyWater));
            }
        }

        public static void e_Prowling(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                case InvokeMode.im_ItSelf:
                    creature.ProwlingBegin(effectID);
                    break;

                case InvokeMode.im_FinAction:
                    creature.ProwlingEnd();
                    break;
            }
        }

        public static void e_Lycanthropy(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                    if (creature.HasAffect(EffectID.eid_Lycanthropy)) {
                        creature.AddEffect(EffectID.eid_Lycanthropy, state, EffectAction.ea_RandomTurn, true, BaseLocale.GetStr(RS.rs_YouveGetALicanthropy));
                    }
                    break;

                case InvokeMode.im_ItSelf:
                    if (!creature.Prowling) {
                        e_Prowling(EffectID.eid_Lycanthropy, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                    }
                    break;
            }
        }

        public static void e_Might(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_MindControl(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWCreature cr = (NWCreature)ext.GetParam(EffectParams.ep_Creature);
            if (cr != null && !cr.Equals(creature)) {
                RaceID race = cr.Entry.Race;
                if ((race == RaceID.crDefault || race == RaceID.crHuman) && (cr.Entry.Flags.Contains(CreatureFlags.esMind) && cr.HasAffect(EffectID.eid_MindControl))) {
                    cr.AddEffect(EffectID.eid_Obedience, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_MindControlDone));
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NothingHappens));
                }
            } else {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NothingHappens));
            }
        }

        public static void e_MistTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_StrangeMistSeepsOutOfGround));
            switch (RandomHelper.GetRandom(8)) {
                case 0:
                    {
                        e_Cure(EffectID.eid_Cure, creature, null, ItemState.is_Blessed, InvokeMode.im_ItSelf, null);
                        break;
                    }
                case 1:
                    {
                        if (creature.GetAbility(AbilityID.Resist_Acid) <= 0) {
                            creature.ApplyDamage(RandomHelper.GetBoundedRnd(1, creature.HPCur / 2), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_YourBodyIsCoveredWithAcid));
                        }
                        break;
                    }
                case 2:
                    {
                        creature.Strength = 3;
                        creature.AddEffect(EffectID.eid_Withered, state, EffectAction.ea_EachTurn, false, BaseLocale.GetStr(RS.rs_YouAreWithered));
                        break;
                    }
                case 3:
                    {
                        creature.Constitution -= RandomHelper.GetBoundedRnd(1, 3);
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourHealthIsAffected));
                        break;
                    }
                case 4:
                    {
                        e_Insanity(EffectID.eid_Insanity, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourMindIsAffected));
                        break;
                    }
                case 5:
                    {
                        AuxUtils.ExStub("todo: message");
                        creature.ArmorClass -= RandomHelper.GetBoundedRnd(2, 5);
                        break;
                    }
                case 6:
                    {
                        creature.Strength -= RandomHelper.GetBoundedRnd(1, 3);
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourStrengthIsAffected));
                        break;
                    }
                case 7:
                    {
                        creature.Dexterity = (ushort)(creature.Dexterity - RandomHelper.GetBoundedRnd(1, 3));
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourDexterityIsAffected));
                        break;
                    }
                case 8:
                    {
                        AuxUtils.ExStub("rs_YourSkinIsAffected");
                        break;
                    }
            }
            AuxUtils.ExStub("todo: прочие эффекты неизвестны");
            AuxUtils.ExStub("Vanseril := TField(creature.CurrentMap).Creatures.EntityByCLSID(cid_Vanseril);if (Vanseril <> nil) then beginval := BoundedRnd(15, 35);creature.ApplyDamage(val, ');Vanseril.HPCur := Vanseril.HPCur + val;fxme: проверить - это другой эффектend;");
        }

        public static void e_MonsterSkill(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWCreature enemy = (NWCreature)ext.GetParam(EffectParams.ep_Creature);

            switch (effectID) {
                case EffectID.eid_Basilisk_Poison:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Physical);
                        break;
                    }

                case EffectID.eid_Borgonvile_Cloud:
                    {
                        AuxUtils.ExStub(" смерчь кружащихся скал (whirling cloud of rocks) ");
                        AuxUtils.ExStub("The whirling rocks from the borgon vile assault you.");
                        AuxUtils.ExStub("A cloud of whirling rocks assault you.");
                        AuxUtils.ExStub("iid_DiamondNeedle - раскидать");
                        break;
                    }

                case EffectID.eid_Breleor_Tendril:
                    {
                        AuxUtils.ExStub("... -> dd");
                        AuxUtils.ExStub("rs_YouCharmed");
                        break;
                    }

                case EffectID.eid_Ellegiant_Throw:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Physical);
                        AuxUtils.ExStub(" метание валунов ok ");
                        break;
                    }

                case EffectID.eid_Ellegiant_Crush:
                    {
                        SimpleAttack(enemy, effectID, DamageKind.Physical, "");
                        break;
                    }

                case EffectID.eid_Firedragon_Breath:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Radiation);
                        break;
                    }

                case EffectID.eid_Firegiant_Touch:
                    {
                        e_Heat(effectID, creature, null, ItemState.is_Normal, InvokeMode.im_ItSelf, ext);
                        break;
                    }

                case EffectID.eid_Fyleisch_Cloud:
                    {
                        FyleischCloud f_cloud = new FyleischCloud();
                        f_cloud.Generate(creature.CurrentField, creature.PosX, creature.PosY);
                        break;
                    }

                case EffectID.eid_Gasball_Explosion:
                    {
                        SimpleAttack(enemy, effectID, DamageKind.Physical, "");
                        AuxUtils.ExStub("взрываются около своих жертв, причиняя физический ущерб и потерю слуха. Жертвы будут оглушены на несколько ходов");
                        break;
                    }

                case EffectID.eid_Giantsquid_Crush:
                    {
                        AuxUtils.ExStub("хватают и уничтожают жертву своими щупальцами");
                        break;
                    }

                case EffectID.eid_Glard_Poison:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Physical);
                        break;
                    }

                case EffectID.eid_Hatchetfish_Teeth:
                    {
                        SimpleAttack(enemy, effectID, DamageKind.Physical, "");
                        break;
                    }

                case EffectID.eid_Heldragon_Cloud:
                    {
                        AuxUtils.ExStub("?? > The blinding wind from the hel dragon tears into you.");
                        break;
                    }

                case EffectID.eid_Hillgiant_Crush:
                    {
                        AuxUtils.ExStub("?? > A rolling boulder misses/hit you.");
                        AuxUtils.ExStub("?? > The hill giant hits.");
                        GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_YouAreBeingCrushed));
                        SimpleAttack(enemy, effectID, DamageKind.Physical, "");
                        break;
                    }

                case EffectID.eid_Icedragon_Breath:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Radiation);
                        break;
                    }

                case EffectID.eid_Icesphere_Blast:
                    {
                        AuxUtils.ExStub("простой взрыв");
                        break;
                    }

                case EffectID.eid_Jagredin_Burning:
                    {
                        SimpleAttack(enemy, effectID, DamageKind.Physical, "");
                        break;
                    }

                case EffectID.eid_Knellbird_Gaze:
                    {
                        AuxUtils.ExStub("хаотична, атакует смертельным взглядом");
                        break;
                    }

                case EffectID.eid_Kobold_Throw:
                case EffectID.eid_Lowerdwarf_Throw:
                    {
                        AuxUtils.ExStub("обычное метание, deprecated");
                        break;
                    }

                case EffectID.eid_Moleman_Debris:
                    {
                        AuxUtils.ExStub("обрушение потолка");
                        break;
                    }

                case EffectID.eid_Phantomasp_Poison:
                    {
                        AuxUtils.ExStub("Укус призрачного аспида - немедленная смерть. Даже устойчивые к ядам, будучи им укушены умрут с вероятностью 4% (если не наденут военный жилет");
                        break;
                    }

                case EffectID.eid_Pyrtaath_Throttle:
                    {
                        SimpleAttack(enemy, effectID, DamageKind.Physical, "");
                        break;
                    }

                case EffectID.eid_Ramapith_FireTouch:
                    {
                        SimpleAttack(enemy, effectID, DamageKind.Physical, "");
                        break;
                    }

                case EffectID.eid_Sandiff_Acid:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Physical);
                        break;
                    }

                case EffectID.eid_Scyld_Breath:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Radiation);
                        break;
                    }

                case EffectID.eid_Scyld_Ray:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Radiation);
                        break;
                    }

                case EffectID.eid_Scyld_ShockWave:
                    {
                        AuxUtils.ExStub("отбрасывание на большое расстояние");
                        break;
                    }

                case EffectID.eid_Sentinel_Gaze:
                    {
                        AuxUtils.ExStub("... -> dd");
                        if (RandomHelper.GetRandom(4) == 0) {
                            enemy.AddEffect(EffectID.eid_Hardening, ItemState.is_Normal, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_YourBodyIsHardening));
                        }
                        break;
                    }

                case EffectID.eid_Serpent_Poison:
                    {
                        SimpleAttack(enemy, effectID, DamageKind.Physical, "");
                        break;
                    }

                case EffectID.eid_Shadow_Touch:
                    {
                        AuxUtils.ExStub(" вытягивают Ваш максимум hit point'ов 1 ед.");
                        enemy.HPMax = enemy.HPMax_Renamed - 1;
                        enemy.ApplyDamage(1, DamageKind.Physical, null, "");
                        break;
                    }

                case EffectID.eid_Slinn_Gout:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Physical);
                        break;
                    }

                case EffectID.eid_Spirit_Touch:
                    {
                        AuxUtils.ExStub("прикосновение может заморозить кровь. Духи могут вытянуть у своей жертвы максимум hit point'ов и оглушить (stun) её на несколько ходов");
                        break;
                    }

                case EffectID.eid_Stunworm_Stun:
                    {
                        SimpleAttack(enemy, effectID, DamageKind.Radiation, "");
                        break;
                    }

                case EffectID.eid_Terrain_Burning:
                    {
                        AuxUtils.ExStub("... -> dd");
                        break;
                    }

                case EffectID.eid_Warrior_Throw:
                case EffectID.eid_Womera_Throw:
                case EffectID.eid_Wooddwarf_Throw:
                    {
                        AuxUtils.ExStub("обычное метание, deprecated");
                        break;
                    }

                case EffectID.eid_Watcher_Gaze:
                    {
                        AuxUtils.ExStub("???");
                        break;
                    }

                case EffectID.eid_Wyvern_Breath:
                    {
                        Ray(effectID, creature, invokeMode, ext, DamageKind.Radiation);
                        break;
                    }

                case EffectID.eid_Zardon_PsiBlast:
                    {
                        AuxUtils.ExStub("Ментальный удар на большом расстоянии");
                        AuxUtils.ExStub("msg: ???");
                        break;
                    }

                case EffectID.eid_Ull_Gaze:
                    {
                        AuxUtils.ExStub("todo: ???");
                        AuxUtils.ExStub("??? e_Confusion(eid_Confusion, enemy, nil, is_Normal, im_ItSelf, ");
                        GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_GazeMakesYouFeelDisoriented));
                        break;
                    }
            }
        }

        public static void e_MonsterTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;

            DungeonRoom dunRoom = creature.FindDungeonRoom();
            if (dunRoom != null) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_MonsterTrapEntering));
                List<int> validCreatures = fld.ValidCreatures;
                int cnt = (validCreatures != null) ? validCreatures.Count : 0;
                int id = fld.ValidCreatures[RandomHelper.GetRandom(cnt)];

                ExtRect r = dunRoom.Area.Clone();
                r.Inflate(-1, -1);

                AbstractMap map = (AbstractMap)dunRoom.Owner;

                for (int y = r.Top; y <= r.Bottom; y++) {
                    for (int x = r.Left; x <= r.Right; x++) {
                        if (r.IsBorder(x, y)) {
                            if (map is NWField) {
                                UniverseBuilder.Gen_Creature(((NWField)map), id, x, y, false);
                            } else {
                                if (map is NWLayer) {
                                    ((NWLayer)map).Gen_Creature(id, x, y, false);
                                }
                            }
                        }
                    }
                }
            }
        }

        public static void e_Music(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (invokeMode == InvokeMode.im_Use) {
                creature.SetAbility(AbilityID.Ab_MusicalAcuity, creature.GetAbility(AbilityID.Ab_MusicalAcuity) + 1);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveGetAMusicalAcuty));
            }
        }

        public static void e_Mystery(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode == InvokeMode.im_Use) {
                int num = RandomHelper.GetRandom(3);
                if (num != 0) {
                    if (num != 1) {
                        if (num == 2) {
                            creature.SetSkill(SkillID.Sk_Writing, creature.GetSkill(SkillID.Sk_Writing) + 1);
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouLearnWrite));
                        }
                    } else {
                        creature.SetSkill(SkillID.Sk_Fennling, creature.GetSkill(SkillID.Sk_Fennling) + 1);
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouLearnFennl));
                    }
                } else {
                    creature.AddEffect(EffectID.eid_LegsMissing, ItemState.is_Normal, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_LegsGone));
                }
            }
        }

        public static void e_CheckOcarinaUse(EffectID effectID, NWCreature creature, object source, InvokeMode invokeMode, EffectExt ext)
        {
            if (((Item)source).CLSID == GlobalVars.iid_GlassOcarina && creature.GetAbility(AbilityID.Ab_MusicalAcuity) > 0) {
                ext.ReqParams = new EffectParams(EffectParams.ep_Land);
            } else {
                ext.ReqParams = new EffectParams();
            }
        }

        public static void e_OcarinaUse(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (source is Item) {
                bool music = creature.GetAbility(AbilityID.Ab_MusicalAcuity) > 0;
                if (!music) {
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_OcarinaTune));
                } else {
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_OcarinaBeautifulTune));
                }

                if (((Item)source).CLSID == GlobalVars.iid_GlassOcarina) {
                    int land_id = (ext == null) ? -1 : (int)ext.GetParam(EffectParams.ep_Land);

                    if (!music || land_id > 0) {
                        int id = creature.LayerID;
                        int fx = -1;
                        int fy = -1;
                        NWField f;
                        if (music) {
                            f = GlobalVars.nwrGame.GetRndFieldByLands(land_id);
                            if (f != null) {
                                id = f.Layer.EntryID;
                                fx = f.Coords.X;
                                fy = f.Coords.Y;
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

                        NWField old_fld = creature.CurrentField;
                        creature.TransferTo(id, fx, fy, -1, -1, StaticData.MapArea, true, true);

                        f = creature.CurrentField;

                        if (old_fld.LandID == GlobalVars.Land_Muspelheim && f.LandID != old_fld.LandID) {
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouReturnToYourPlaneOfExistence));
                        } else {
                            AuxUtils.ExStub("todo: You remain in this realm.");
                        }
                    }
                }
            }
        }

        public static void e_Paralysis(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            switch (invokeMode) {
                case InvokeMode.im_Use:
                case InvokeMode.im_ItSelf:
                    CreateMapObject(effectID, creature.CurrentField, creature.PosX, creature.PosY, true);

                    if (creature.HasAffect(EffectID.eid_Paralysis)) {
                        creature.AddEffect(EffectID.eid_Paralysis, state, EffectAction.ea_Persistent, true, BaseLocale.GetStr(RS.rs_Paralysis_Beg));
                        if (creature.IsPlayer) {
                            GlobalVars.nwrGame.TurnState = TurnState.Skip;
                        }
                    }
                    break;

                case InvokeMode.im_FinAction:
                    if (creature.IsPlayer) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouCanMoveAgain));
                        GlobalVars.nwrGame.TurnState = TurnState.Done;
                    }
                    break;
            }
        }

        public static void e_Phase(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode != InvokeMode.im_UseBegin) {
                    if (invokeMode != InvokeMode.im_UseEnd) {
                        if (invokeMode == InvokeMode.im_ItSelf) {
                            Effect e = creature.Effects.FindEffectByID(EffectID.eid_Phase);
                            if (e.Source == null) {
                                if (e.Duration == 1) {
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouSolidAgain));
                                } else {
                                    if (e.Duration < 5) {
                                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_FazingActionFinishes));
                                    }
                                }
                            }
                        }
                    } else {
                        creature.DoneEffect(EffectID.eid_Phase, source);
                    }
                } else {
                    creature.InitEffect(EffectID.eid_Phase, source, EffectAction.ea_Persistent);
                }
            } else {
                creature.AddEffect(EffectID.eid_Phase, state, EffectAction.ea_EachTurn, false, BaseLocale.GetStr(RS.rs_YouFeelLessSubstantial));
            }
        }

        public static void e_PhaseTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            creature.AddEffect(EffectID.eid_PhaseTrap, state, EffectAction.ea_Persistent, true, BaseLocale.GetStr(RS.rs_YouTriggerPhaseTrap));
        }

        public static void e_PickAxeUse(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            int dir = (int)ext.GetParam(EffectParams.ep_Direction);
            int pX = creature.PosX + Directions.Data[dir].DX;
            int pY = creature.PosY + Directions.Data[dir].DY;
            NWTile tile = (NWTile)creature.CurrentField.GetTile(pX, pY);
            tile.Foreground = PlaceID.pid_Undefined;
        }

        public static void e_PitTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: this is stub");
            string txt;
            if (creature.IsPlayer) {
                txt = BaseLocale.GetStr(RS.rs_YouFallIntoPit);
            } else {
                txt = "";
            }
            creature.AddEffect(EffectID.eid_PitTrap, state, EffectAction.ea_Persistent, true, txt);
        }

        public static void e_Poisoned(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode == InvokeMode.im_ItSelf) {
                    Effect e = creature.Effects.FindEffectByID(EffectID.eid_Poisoned);
                    if (e.Source == null && e.Duration == 1) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_VenomReachesYourHeart));
                        creature.Death(BaseLocale.GetStr(RS.rs_KilledByDeadlyPotion), null);
                    }
                }
            } else {
                if (creature.HasAffect(EffectID.eid_Poisoned)) {
                    creature.AddEffect(EffectID.eid_Poisoned, state, EffectAction.ea_EachTurn, true, BaseLocale.GetStr(RS.rs_ThatMayBeToxic));
                }
            }
        }

        public static void e_PoisonSpikeTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.HasAffect(EffectID.eid_PoisonSpikeTrap)) {
                creature.Strength -= RandomHelper.GetBoundedRnd(1, 5);
                creature.ApplyDamage(RandomHelper.GetBoundedRnd(15, 30), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_PoisonedSpikesHitYou));
            }
        }

        public static void e_Polymorph(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            PolymorphRay ray = new PolymorphRay();
            ray.Exec(creature, EffectID.eid_Polymorph, invokeMode, ext, WandRayMsg(effectID, source));
        }

        public static void e_CheckPrecognition(EffectID effectID, NWCreature creature, object source, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode == InvokeMode.im_ItSelf) {
                ext.ReqParams = new EffectParams(EffectParams.ep_Item);
            } else {
                ext.ReqParams = new EffectParams();
            }
        }

        public static void e_Precognition(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode == InvokeMode.im_ItSelf) {
                    Item item = (Item)ext.GetParam(EffectParams.ep_Item);
                    item.Identified = true;
                }
            } else {
                creature.SetSkill(SkillID.Sk_Precognition, creature.GetSkill(SkillID.Sk_Precognition) + 1);
                if (creature.GetSkill(SkillID.Sk_Precognition) == 1) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveGetAPrecognitionSkill));
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_PowerPrecognitionGrows));
                }
            }
        }

        public static void e_Protection(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_UseBegin:
                    creature.InitEffect(effectID, source, EffectAction.ea_EachTurn);
                    break;

                case InvokeMode.im_UseEnd:
                    creature.DoneEffect(effectID, source);
                    break;

                case InvokeMode.im_ItSelf:
                    if (creature.IsPlayer) {
                        Item item = (Item)source;
                        if (item != null && AuxUtils.Chance(item.Bonus)) {
                            int px = creature.PosX;
                            int py = creature.PosY;

                            NWField map = creature.CurrentField;

                            for (int y = py - 3; y <= py + 3; y++) {
                                for (int x = px - 3; x <= px + 3; x++) {
                                    if (MathHelper.Distance(px, py, x, y) <= 3) {
                                        NWTile tile = (NWTile)map.GetTile(x, y);
                                        if (tile != null && map.IsTrap(x, y)) {
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

        public static void e_PsiBlast(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: Your psionic power needs to be recuperated.");

            NWCreature cr = (NWCreature)ext.GetParam(EffectParams.ep_Creature);
            if (cr != null && !cr.Equals(creature)) {
                RaceID race = cr.Entry.Race;
                if ((race == RaceID.crDefault || race == RaceID.crHuman) && cr.HasAffect(EffectID.eid_PsiBlast)) {
                    cr.Death(BaseLocale.GetStr(RS.rs_YouProbeAForeignMind), null);
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelNoResponse));
                }
            } else {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelNoResponse));
            }
        }

        public static void e_PureEvil(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;

            AuxUtils.ExStub("todo: messages");
            AuxUtils.ExStub("todo: You can no longer see. The air grows cold. -> blind");

            switch (RandomHelper.GetBoundedRnd(1, 7)) {
                case 1:
                    {
                        int num = creature.Items.Count;
                        for (int i = 0; i < num; i++) {
                            creature.Items.GetItem(i).State = ItemState.is_Cursed;
                        }
                        break;
                    }

                case 2:
                    {
                        int i = 100;
                        while (true) {
                            int x = RandomHelper.GetRandom(StaticData.FieldWidth);
                            int y = RandomHelper.GetRandom(StaticData.FieldHeight);
                            if (!fld.IsBarrier(x, y)) {
                                ushort pid = StaticData.dbTraps[RandomHelper.GetRandom(15)].TileID;
                                fld.AddTrap(x, y, pid, false);
                                i--;
                                if (i == 0) {
                                    break;
                                }
                            }
                        }
                        break;
                    }
                case 3:
                    {
                        creature.TransferTo(GlobalVars.Layer_Niflheim, -1, -1, -1, -1, StaticData.MapArea, true, false);
                        break;
                    }
                case 4:
                    {
                        int px = creature.PosX;
                        int py = creature.PosY;

                        for (int y = py - 1; y <= py + 1; y++) {
                            for (int x = px - 1; x <= px + 1; x++) {
                                if (y != py && x != px && AuxUtils.Chance(45)) {
                                    fld.AddCreature(x, y, GlobalVars.nwrDB.FindEntryBySign("Migdnart").GUID);
                                }
                            }
                        }
                        break;
                    }
                case 5:
                    {
                        UniverseBuilder.Gen_Creatures(fld, 100);
                        break;
                    }
                case 6:
                    {
                        if (creature.IsPlayer) {
                            ((Player)creature).Morality = (sbyte)((int)((Player)creature).Morality - 15);
                        }
                        creature.AddEffect(EffectID.eid_Blindness, ItemState.is_Normal, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_YouAreBlinding));
                        break;
                    }
                case 7:
                    {
                        int cnt = RandomHelper.GetBoundedRnd(5, 37);
                        for (int i = 1; i <= cnt; i++) {
                            fld.AddCreature(-1, -1, GlobalVars.nwrDB.FindEntryBySign("Denizen").GUID);
                        }
                        break;
                    }
            }
        }

        public static void e_QuicksandTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: на каком этапе это сообщение?");
            AuxUtils.ExStub("todo: //nwrWin.ShowText(creature, rsList('Drowned in quicksand']);");

            NWField fld = creature.CurrentField;
            NWTile tile = (NWTile)fld.GetTile(creature.PosX, creature.PosY);
            if (tile.ForeBase == PlaceID.pid_QuicksandTrap) {
                tile.Background = PlaceID.pid_Quicksand;
                fld.Normalize();
            }
            creature.AddEffect(EffectID.eid_Quicksand, state, EffectAction.ea_Persistent, true, BaseLocale.GetStr(RS.rs_YouSinkInQuicksand));
        }

        public static void e_RaiseMagic(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            CreateMapObject(effectID, creature.CurrentField, creature.PosX, creature.PosY, true);

            int cnt = 0;
            switch (state) {
                case ItemState.is_Normal:
                    cnt = 1;
                    break;
                case ItemState.is_Blessed:
                    cnt = 2;
                    break;
                case ItemState.is_Cursed:
                    cnt = -1;
                    break;
            }

            creature.MPMax += cnt;
            if (cnt > 0) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourMagicAbilityIncreases));
            } else {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourMagicAbilityDecreases));
            }
        }

        public static void e_Recall(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (creature.IsPlayer) {
                NWField fld = creature.CurrentField;
                if (fld.EntryID == GlobalVars.Field_Bazaar) {
                    creature.Death(BaseLocale.Format(RS.rs_BazaarMagicShieldDeath, new object[]{ creature.Name }), null);
                } else {
                    RecallPos rp = (RecallPos)((Player)creature).Memory.Find("RecallPos");
                    if (rp != null) {
                        creature.TransferTo(rp.Layer, rp.Field.X, rp.Field.Y, rp.Pos.X, rp.Pos.Y, StaticData.MapArea, true, true);
                    } else {
                        creature.TransferTo(GlobalVars.Layer_Midgard, 2, 2, -1, -1, StaticData.MapArea, true, true);
                    }
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourWorldSpins));
                }
            }
        }

        public static void e_Recharging(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok

            AuxUtils.ExStub("todo: question msg: rs_EmpowerWhich");
            AuxUtils.ExStub("todo:  + disruption horn");
            AuxUtils.ExStub("todo:  + rs_ScrollHasNoApparentEffect");

            Item aExtItem = (Item)ext.GetParam(EffectParams.ep_Item);

            if (creature.Confused) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_WandsDrained));

                int num = creature.Items.Count;
                for (int i = 0; i < num; i++) {
                    Item item = creature.Items.GetItem(i);
                    if (item.Kind == ItemKind.ik_Wand || (item.Entry.Sign.Equals("BarbedWhip")) || (item.Entry.Sign.Equals("WoodenFlute"))) {
                        item.Bonus = 0;
                    }
                }
            } else {
                if (aExtItem.Kind == ItemKind.ik_Wand) {
                    if (aExtItem.Entry.Sign.CompareTo("Wand_Wishing") == 0) {
                        aExtItem.Bonus++;
                    } else {
                        aExtItem.Bonus += RandomHelper.GetBoundedRnd(5, 15);
                    }
                } else {
                    if (aExtItem.Entry.Sign.CompareTo("BarbedWhip") == 0 || aExtItem.Entry.Sign.CompareTo("WoodenFlute") == 0) {
                        aExtItem.Bonus = RandomHelper.GetBoundedRnd(7, 15);
                    }
                }

                GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_WandPulsates, new object[]{ aExtItem.GetDeclinableName(Number.nSingle, Case.cNominative, creature.Blindness) }));
            }
        }

        public static void e_Reflect(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouNowReflect));
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_SpellAlreadyProtects));
        }

        public static void e_Regeneration(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // -> NWCreature.Recovery()

            switch (invokeMode) {
                case InvokeMode.im_UseBegin:
                    creature.InitEffect(effectID, source, EffectAction.ea_Persistent);
                    break;
                case InvokeMode.im_UseEnd:
                    creature.DoneEffect(effectID, source);
                    break;
            }
        }

        public static void e_Rejuvenation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: messages");
            AuxUtils.ExStub("todo: if all ok then Nothng seems to happen");

            if (creature.Effects.FindEffectByID(EffectID.eid_Withered) != null) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouExhaustedNoRejuvenation));
            } else {
                switch (state) {
                    case ItemState.is_Blessed:
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourLiverTingles));
                        creature.SetAbility(AbilityID.Resist_Poison, 100);
                        CureEffect(creature, EffectID.eid_Poisoned, BaseLocale.GetStr(RS.rs_VenomIsNeutralized));
                        break;

                    case ItemState.is_Cursed:
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourLiverAches));
                        creature.SetAbility(AbilityID.Resist_Poison, 0);
                        break;
                }

                if (state != ItemState.is_Cursed && creature.HPCur != creature.HPMax_Renamed) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourLostStrengthIsRestored));
                    // todo: original - strength
                    creature.HPCur = creature.HPMax_Renamed;
                }
            }
        }

        public static void e_Relocation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                    {
                        if (creature.HasAffect(EffectID.eid_Relocation)) {
                            NWField fld = creature.CurrentField;
                            if (source is BaseTile && (int)((BaseTile)source).Foreground == PlaceID.pid_TeleportTrap) {
                                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouSetOffTeleportationTrap));
                            }

                            ExtPoint pt = ExtPoint.Empty;
                            bool res = (ext == null);
                            if (!res) {
                                pt = (ExtPoint)ext.GetParam(EffectParams.ep_Place);
                                res = (pt.IsEmpty);
                            }

                            if (res) {
                                pt = creature.SearchRndLocation(fld, fld.AreaRect);
                            }

                            creature.CheckTile(false);
                            creature.SetPos(pt.X, pt.Y);
                            creature.CheckTile(true);

                            if (creature.IsPlayer) {
                                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveTeleported));
                            }
                        }
                        break;
                    }

                case InvokeMode.im_UseBegin:
                    creature.InitEffect(effectID, source, EffectAction.ea_RandomTurn);
                    break;

                case InvokeMode.im_UseEnd:
                    creature.DoneEffect(effectID, source);
                    break;
            }
        }

        public static void e_Restoration(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.IsPlayer) {
                if (creature.IsPlayer && creature.CLSID == GlobalVars.cid_Werewolf) {
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_WerebloodDissipates));
                    AuxUtils.ExStub("todo:  ");
                }

                SourceForm sf = (SourceForm)((Player)creature).Memory.Find("SourceForm");

                creature.CLSID = sf.SfID;

                AuxUtils.ExStub("todo:  messages");
                AuxUtils.ExStub("todo: rs_OldSelfAgain");
                AuxUtils.ExStub("todo: rs_ReturnOriginalForm");
                AuxUtils.ExStub("todo: rs_HumansAreExtinct");
                AuxUtils.ExStub("todo: rs_TurnedIntoMass");

                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.Format(RS.rs_RestoreForm, new object[]{ creature.Name }));
            }
        }

        public static void e_RestoreMagic(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (creature.MPCur == creature.MPMax) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NothingHappens));
            } else {
                creature.MPCur = creature.MPMax;
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouAreRestoredYourMana));
            }
        }

        public static void e_RunicDivination(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            GlobalVars.nwrWin.ShowDivination();
        }

        public static void e_Prayer(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            object obj = ext.GetParam(EffectParams.ep_GodID);
            int deityID = (int)obj;

            CreatureEntry deity = (CreatureEntry)GlobalVars.nwrDB.GetEntry(deityID);
            if (deity != null) {
                GlobalVars.nwrWin.ShowTextRes(creature, RS.rs_PraysTo, new object[]{ creature, deity });

                ((Player)creature).Faith.Worship(deityID, Faith.WK_PRAYER, null);
            }
        }

        public static void e_Sacrifice(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            object obj = ext.GetParam(EffectParams.ep_GodID);
            int deityID = (int)obj;

            CreatureEntry deity = (CreatureEntry)GlobalVars.nwrDB.GetEntry(deityID);
            if (deity != null) {
                Item item = (Item)ext.GetParam(EffectParams.ep_Item);
                GlobalVars.nwrWin.ShowTextRes(creature, RS.rs_ItemSacrificed, new object[]{ item, deity });

                ((Player)creature).Faith.Worship(deityID, Faith.WK_SACRIFICE, item);
            }
        }

        public static void e_Scaling(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            int cnt = 0;
            switch (state) {
                case ItemState.is_Normal:
                    cnt = 1;
                    break;
                case ItemState.is_Blessed:
                    cnt = 2;
                    break;
                case ItemState.is_Cursed:
                    cnt = -1;
                    break;
            }

            creature.ArmorClass += cnt;

            if (creature.IsPlayer) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouSkinFeelsPinching));
                if (cnt > 0) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouBecameStronger));
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouBecameLessArmored));
                }
            }
        }

        public static void e_SetRecall(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.IsPlayer) {
                NWField fld = creature.CurrentField;
                if (fld.LandID == GlobalVars.Land_Jotenheim) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_SpellFails));
                }
                ((Player)creature).SetRecallPos();
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveMakeAPointOfReturn));
            }
        }

        private static void LoadShip(Player player)
        {
            try {
                Item ship = player.FindItem("Skidbladnir");
                LeaderBrain party = (LeaderBrain)player.Brain;

                for (int i = 1; i < party.Members.Count; i++) {
                    NWCreature member = party.Members[i];
                    member.AddEffect(EffectID.eid_Sail, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                    member.CheckTile(false);
                    member.LeaveField();

                    ship.Contents.Add(member);
                }
            } catch (Exception ex) {
                Logger.Write("EffectsFactory.loadShip(): " + ex.Message);
                throw ex;
            }
        }

        public static void UnloadShip(Player player)
        {
            try {
                Item ship = player.FindItem("Skidbladnir");
                LeaderBrain party = (LeaderBrain)player.Brain;
                //NWField field = player.getCurrentField();

                for (int i = 1; i < party.Members.Count; i++) {
                    NWCreature member = party.Members[i];
                    ship.Contents.Extract(member);

                    Effect ef = member.Effects.FindEffectByID(EffectID.eid_Sail);
                    member.Effects.Remove(ef);

                    ExtPoint pt = member.GetNearestPlace(player.Location, 2, true);
                    if (!pt.IsEmpty) {
                        member.TransferTo(player.LayerID, player.Field.X, player.Field.Y, pt.X, pt.Y, StaticData.MapArea, true, true);
                    } else {
                        Logger.Write("EffectsFactory.unloadShip().getNearestPlace() failed");
                    }
                }
            } catch (Exception ex) {
                Logger.Write("EffectsFactory.unloadShip(): " + ex.Message);
                throw ex;
            }
        }

        public static void e_Skidbladnir(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.IsPlayer) {
                int dir = (int)ext.GetParam(EffectParams.ep_Direction);
                int nx = creature.PosX + Directions.Data[dir].DX;
                int ny = creature.PosY + Directions.Data[dir].DY;
                NWTile tile = (NWTile)creature.CurrentField.GetTile(nx, ny);
                if (tile.BackBase != PlaceID.pid_Water) {
                    AuxUtils.ExStub("todo: check message");
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NeedLaunchOntoWater));
                } else {
                    LoadShip((Player)creature);
                    creature.AddEffect(EffectID.eid_Sail, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                    creature.MoveTo(nx, ny);
                }
            }
        }

        public static void e_SlaveUse(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("if not(HasAffect(eid_SlaveUse, False))");
            AuxUtils.ExStub("then nwrWin.ShowText(creature, rsList(rs_SlaveUseFail]);");
        }

        public static void e_Sleep(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: fix to ray");
            AuxUtils.ExStub("todo: msg: '%s falls asleep.");

            switch (invokeMode) {
                case InvokeMode.im_Use:
                case InvokeMode.im_ItSelf:
                    if (creature.HasAffect(EffectID.eid_Sleep)) {
                        creature.AddEffect(EffectID.eid_Sleep, state, EffectAction.ea_Persistent, true, BaseLocale.GetStr(RS.rs_Sleep_Beg));
                        if (creature.IsPlayer) {
                            GlobalVars.nwrGame.TurnState = TurnState.Skip;
                        }
                    }
                    break;

                case InvokeMode.im_FinAction:
                    if (creature.IsPlayer) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouCanMoveAgain));
                        GlobalVars.nwrGame.TurnState = TurnState.Done;
                    }
                    break;
            }
        }

        public static void e_SoulSeeking(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            AuxUtils.ExStub("@deprecated: You sense no life in this area.");
            creature.AddEffect(EffectID.eid_SoulSeeking, state, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_SoulSeekingWillShowYouAllCreatures));
        }

        public static void e_SoulTrapping(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_SpeedDown(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Speedup(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok

            switch (invokeMode) {
                case InvokeMode.im_Use:
                case InvokeMode.im_ItSelf:
                    creature.AddEffect(EffectID.eid_Speedup, state, EffectAction.ea_Persistent, false, "");

                    int speed = creature.Speed;
                    if (speed < 50) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouStayFaster));
                    } else {
                        if (speed < 70) {
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_DangerouslyFast));
                        } else {
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_HeartBursts));
                            creature.Death(BaseLocale.GetStr(RS.rs_KilledByAcceleration), null);
                        }
                    }
                    break;

                case InvokeMode.im_UseBegin:
                    creature.InitEffect(EffectID.eid_Speedup, source, EffectAction.ea_Persistent);
                    break;

                case InvokeMode.im_UseEnd:
                    creature.DoneEffect(EffectID.eid_Speedup, source);
                    break;
            }
        }

        public static void e_Stoning(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            StoningRay ray = new StoningRay();
            ray.Exec(creature, EffectID.eid_Stoning, invokeMode, ext, WandRayMsg(EffectID.eid_Stoning, source));
        }

        public static void e_Strength(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            if (state != ItemState.is_Normal) {
                if (state != ItemState.is_Blessed) {
                    if (state == ItemState.is_Cursed) {
                        creature.Strength--;
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsAWeakness));
                    }
                } else {
                    creature.Strength += RandomHelper.GetBoundedRnd(1, 4);
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelMuchStronger));
                }
            } else {
                creature.Strength++;
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelsAPower));
            }
        }

        public static void e_StrengthReduce(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            AuxUtils.ExStub("yellow mushroom");
            e_Strength(EffectID.eid_Strength, creature, source, ItemState.is_Cursed, InvokeMode.im_Use, null);
        }

        public static void e_StunGasTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.HasAffect(EffectID.eid_StunGasTrap)) {
                creature.AddEffect(EffectID.eid_SpeedDown, state, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_StunGasEntrapped));
            }
        }

        public static void e_Summoning(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            NWField field = creature.CurrentField;

            if (creature.Confused) {
                if (AuxUtils.Chance(93)) {
                    for (int i = field.Creatures.Count - 1; i >= 0; i--) {
                        NWCreature cr = field.Creatures.GetItem(i);
                        RaceID race = cr.Entry.Race;
                        if ((race == RaceID.crDefault || race == RaceID.crHuman) && !cr.Equals(creature)) {
                            int id = GlobalVars.dbLayers[RandomHelper.GetBoundedRnd(0, GlobalVars.dbLayers.Count - 1)];
                            NWLayer tempLayer = GlobalVars.nwrGame.GetLayer(id);

                            int x = RandomHelper.GetBoundedRnd(0, tempLayer.W - 1);
                            int y = RandomHelper.GetBoundedRnd(0, tempLayer.H - 1);

                            cr.TransferTo(id, x, y, -1, -1, StaticData.MapArea, true, false);
                        }
                    }

                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_AlcoRecallCausedACollapse));
                } else {
                    int x = RandomHelper.GetBoundedRnd(creature.PosX - 5, creature.PosX + 5);
                    int y = RandomHelper.GetBoundedRnd(creature.PosY - 5, creature.PosY + 5);
                    field.AddCreature(x, y, GlobalVars.nwrDB.FindEntryBySign("GreyTerror").GUID);
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourHairStandsOnEnd));
                }
            } else {
                string temp = "";
                switch (RandomHelper.GetBoundedRnd(1, 5)) {
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

                int x = RandomHelper.GetBoundedRnd(creature.PosX - 5, creature.PosX + 5);
                int y = RandomHelper.GetBoundedRnd(creature.PosY - 5, creature.PosY + 5);
                field.AddCreature(x, y, GlobalVars.nwrDB.FindEntryBySign(temp).GUID);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveGetAReinforcement));
            }
        }

        public static void e_SwitchBodies(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.IsPlayer) {
                EntityList cs = creature.CurrentField.Creatures;

                if (cs.Count == 1) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NoCreatureExists));
                } else {
                    NWCreature weak = null;
                    NWCreature strong = null;
                    float wf = 0f;
                    float sf = 1f;

                    int num = cs.Count;
                    for (int i = 0; i < num; i++) {
                        NWCreature c = (NWCreature)cs.GetItem(i);

                        RaceID race = c.Entry.Race;
                        if (race == RaceID.crDefault || race == RaceID.crHuman) {
                            float f = c.GetAttackRate(creature, 0);
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
                    if (creature.Equals(weak)) {
                        weak = null;
                    }
                    if (creature.Equals(strong)) {
                        strong = null;
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouAreMostPowerful));
                    }

                    if (creature.Confused && weak != null) {
                        target = weak;
                    } else {
                        if (strong != null) {
                            target = strong;
                        }
                    }

                    if (target != null) {
                        ExtPoint tPos = target.Location;
                        creature.SwitchBody(target);
                        target.CheckTile(false);
                        cs.Remove(target);
                        creature.CheckTile(false);
                        creature.SetPos(tPos.X, tPos.Y);
                        creature.CheckTile(true);
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouSwitchBodies));
                    }
                }
            }
        }

        public static void e_SwitchDimension(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;

            if (invokeMode != InvokeMode.im_Use) {
                if (invokeMode == InvokeMode.im_ItSelf) {
                    creature.TransferTo(GlobalVars.Layer_Crossroads, 0, 0, -1, -1, StaticData.MapArea, true, true);
                    fld.Research(false, (BaseTile.TS_SEEN | BaseTile.TS_VISITED));
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouCameOnCrossroads));
                }
            } else {
                creature.SetSkill(SkillID.Sk_DimensionTravel, creature.GetSkill(SkillID.Sk_DimensionTravel) + 1);
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouGetDimTravelAbility));
            }
        }

        public static void e_Swoon(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                case InvokeMode.im_ItSelf:
                    creature.AddEffect(EffectID.eid_Swoon, ItemState.is_Normal, EffectAction.ea_Persistent, true, BaseLocale.GetStr(RS.rs_YouFaintFromHunger));
                    GlobalVars.nwrGame.TurnState = TurnState.Skip;
                    break;

                case InvokeMode.im_FinAction:
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouCanMoveAgain));
                    GlobalVars.nwrGame.TurnState = TurnState.Done;
                    break;
            }
        }

        public static void e_Taming(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWCreature cr = (NWCreature)ext.GetParam(EffectParams.ep_Creature);
            if (cr != null && !cr.Equals(creature)) {
                RaceID race = cr.Entry.Race;
                if ((race == RaceID.crDefault || race == RaceID.crHuman) && (!cr.Entry.Flags.Contains(CreatureFlags.esPlant))) {
                    cr.AddEffect(EffectID.eid_Obedience, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouCharmTheMonster));
                } else {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NothingHappens));
                }
            } else {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_NothingHappens));
            }
        }

        public static void e_TerrainLife(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo:  not joined!!!");
            AuxUtils.ExStub("todo: rp_terrainlife");
            AuxUtils.ExStub("todo: 'rock'");
            AuxUtils.ExStub("todo: 'watery form'");
            AuxUtils.ExStub("todo: 'sand form'");
            AuxUtils.ExStub("todo: 'mud floe'");
            AuxUtils.ExStub("todo: 'lava flow'");
            AuxUtils.ExStub("todo: 'tree'");
            AuxUtils.ExStub("todo: 'wall'");
        }

        public static void e_ThirdSight(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode != InvokeMode.im_UseBegin) {
                if (invokeMode == InvokeMode.im_UseEnd) {
                    creature.DoneEffect(effectID, source);
                }
            } else {
                creature.InitEffect(effectID, source, EffectAction.ea_Persistent);
            }
        }

        public static void e_HurtleThroughTime(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                case InvokeMode.im_ItSelf:
                    AuxUtils.ExStub("prepare in e_TimeStop()");
                    break;

                case InvokeMode.im_FinAction:
                    if (creature.IsPlayer) {
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouStepIntoFlowOfTime));
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouCanMoveAgain));
                        GlobalVars.nwrGame.TurnState = TurnState.Done;
                    }
                    break;
            }
        }

        public static void e_TimeStop(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;

            switch (invokeMode) {
                case InvokeMode.im_Use:
                    if (creature.IsPlayer && fld.LandID != GlobalVars.Land_Bazaar && fld.LandID != GlobalVars.Land_MimerWell) {
                        if (fld.LandID == GlobalVars.Land_MimerRealm || fld.LandID == GlobalVars.Land_Niflheim || fld.LandID == GlobalVars.Land_Jotenheim || fld.LandID == GlobalVars.Land_Nidavellir || fld.LandID == GlobalVars.Land_Armory || fld.LandID == GlobalVars.Land_Vigrid || fld.LandID == GlobalVars.Land_Valhalla || fld.LandID == GlobalVars.Land_GodsFortress || fld.LandID == GlobalVars.Land_Ocean || fld.LandID == GlobalVars.Land_Bifrost || fld.LandID == GlobalVars.Land_GiollRiver) {
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouOpenedMagicGate));

                            int id = RandomHelper.GetRandom(5);
                            switch (id) {
                                case 0:
                                    id = GlobalVars.nwrDB.FindEntryBySign("Migdnart").GUID;
                                    break;
                                case 1:
                                    id = GlobalVars.nwrDB.FindEntryBySign("Minion").GUID;
                                    break;
                                case 2:
                                    id = GlobalVars.nwrDB.FindEntryBySign("PyrtaAth").GUID;
                                    break;
                                case 3:
                                    id = GlobalVars.nwrDB.FindEntryBySign("TimeMaster").GUID;
                                    break;
                                case 4:
                                    id = GlobalVars.nwrDB.FindEntryBySign("Draugr").GUID;
                                    break;
                            }
                            int cnt = RandomHelper.GetBoundedRnd(5, 17);
                            int px = creature.PosX;
                            int py = creature.PosY;

                            for (int idx = 1; idx <= cnt; idx++) {
                                int x = RandomHelper.GetBoundedRnd(px - 5, px - 5);
                                int y = RandomHelper.GetBoundedRnd(py - 5, py - 5);
                                if (!fld.IsBarrier(x, y)) {
                                    fld.AddCreature(x, y, id);
                                }
                            }
                        }

                        if (creature.Confused) {
                            int cnt = RandomHelper.GetBoundedRnd(1000, 10000);
                            creature.AddEffect(EffectID.eid_HurtleThroughTime, state, EffectAction.ea_Persistent, cnt, BaseLocale.GetStr(RS.rs_YouHurtleThroughTime));
                            if (creature.IsPlayer) {
                                GlobalVars.nwrGame.TurnState = TurnState.Skip;
                            }
                        } else {
                            int cnt = 0;
                            switch (state) {
                                case ItemState.is_Normal:
                                case ItemState.is_Cursed:
                                    cnt = RandomHelper.GetBoundedRnd(10, 30);
                                    break;
                                case ItemState.is_Blessed:
                                    cnt = RandomHelper.GetBoundedRnd(100, 126);
                                    break;
                            }

                            switch (state) {
                                case ItemState.is_Normal:
                                case ItemState.is_Blessed:
                                    {
                                        string msg;
                                        if (RandomHelper.GetRandom(2) == 0) {
                                            msg = BaseLocale.GetStr(RS.rs_TimeIsStopInUniverse); // my
                                        } else {
                                            msg = BaseLocale.GetStr(RS.rs_YouAreOutsideFlowOfTime); // original
                                        }

                                        Effect effect = new Effect(creature.Space, creature, EffectID.eid_TimeStop, null, EffectAction.ea_Persistent, cnt, 0);
                                        creature.Effects.Add(effect);
                                        GlobalVars.nwrWin.ShowText(creature, msg);
                                    }
                                    break;

                                case ItemState.is_Cursed:
                                    {
                                        string msg;
                                        if (RandomHelper.GetRandom(2) == 0) {
                                            msg = BaseLocale.GetStr(RS.rs_TimeIsStopForYou); // my
                                        } else {
                                            msg = BaseLocale.GetStr(RS.rs_TimeWhizzes); // original
                                        }

                                        creature.AddEffect(EffectID.eid_HurtleThroughTime, state, EffectAction.ea_Persistent, cnt, msg);
                                        if (creature.IsPlayer) {
                                            GlobalVars.nwrGame.TurnState = TurnState.Skip;
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                    break;

                case InvokeMode.im_ItSelf:
                    break;

                case InvokeMode.im_FinAction:
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouStepIntoFlowOfTime));
                    break;
            }
        }

        public static void e_Transformation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok

            NWField fld = creature.CurrentField;
            int id;
            if (ext != null) {
                object obj = ext.GetParam(EffectParams.ep_MonsterID);
                id = (int)obj;
            } else {
                List<int> validCreatures = fld.ValidCreatures;
                int cnt = (validCreatures != null) ? validCreatures.Count : 0;
                if (cnt < 1) {
                    return;
                }
                int i = RandomHelper.GetRandom(cnt);
                id = fld.ValidCreatures[i];
            }

            creature.InitEx(id, false, false);

            if (!creature.Entry.Flags.Contains(CreatureFlags.esUseItems)) {
                creature.DropAll();
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourArmorFallsOff));
            }

            AuxUtils.ExStub("todo:  messages");
            AuxUtils.ExStub("todo: Your amulet falls off.");
            AuxUtils.ExStub("todo: Your armor strains and bursts.");
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_YouShallNowBeKnownAsXX, new object[] {
                creature.Name,
                creature.Race
            }));
        }

        public static void e_Transmutation(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            TransmutationRay ray = new TransmutationRay();
            try {
                ray.Exec(creature, EffectID.eid_Transmutation, invokeMode, ext, WandRayMsg(EffectID.eid_Transmutation, source));
            } finally {
                creature.CurrentField.Normalize();
            }
        }

        public static void e_Transport(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok
            NWField fld = creature.CurrentField;

            if (creature.IsPlayer) {
                if (creature.LayerID == GlobalVars.Layer_Svartalfheim3) {
                    creature.TransferTo(GlobalVars.Layer_Midgard, 2, 2, -1, -1, StaticData.MapArea, true, true);
                } else {
                    creature.TransferTo(GlobalVars.Layer_Svartalfheim3, 2, 2, -1, -1, StaticData.MapArea, true, true);
                    fld.Research(false, (BaseTile.TS_SEEN | BaseTile.TS_VISITED));
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourWorldSpins));
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_SignReads));
                }
            }
        }

        public static void e_TrapDetection(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.IsPlayer) {
                NWField field = creature.CurrentField;

                int num = field.Height;
                for (int y = 0; y < num; y++) {
                    int num2 = field.Width;
                    for (int x = 0; x < num2; x++) {
                        NWTile tile = (NWTile)field.GetTile(x, y);

                        if (field.IsTrap(x, y)) {
                            tile.IncludeState(BaseTile.TS_VISITED);
                            tile.Trap_Discovered = true;
                        } else {
                            Gate gate = field.FindGate(x, y);
                            if (gate != null) {
                                tile.IncludeState(BaseTile.TS_VISITED);
                            }
                        }
                    }
                }

                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouKnowAboutAllTrapsAndPortals));
            }
        }

        public static void e_TrapGeneration(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok

            NWField fld = creature.CurrentField;

            if (creature.Confused) {
                for (int y = 0; y < StaticData.FieldHeight; y++) {
                    for (int x = 0; x < StaticData.FieldWidth; x++) {
                        if (fld.IsTrap(x, y)) {
                            NWTile tile = (NWTile)fld.GetTile(x, y);
                            tile.Foreground = PlaceID.pid_Undefined;
                        }
                    }
                }
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelSenseOfEase));
            } else {
                int px = creature.PosX;
                int py = creature.PosY;

                for (int y = py - 2; y <= py + 2; y++) {
                    for (int x = px - 2; x <= px + 2; x++) {
                        if (x != px && y != py && AuxUtils.Chance(40)) {
                            ushort id = StaticData.dbTraps[RandomHelper.GetRandom(15)].TileID;
                            fld.AddTrap(x, y, id, true);
                        }
                    }
                }
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouCreateTraps));
            }
        }

        public static void e_Traveling(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.IsPlayer) {
                if (creature.Confused || state == ItemState.is_Cursed) {
                    int i = RandomHelper.GetRandom(GlobalVars.dbLayers.Count);
                    creature.TransferTo(GlobalVars.dbLayers[i], -1, -1, -1, -1, StaticData.MapArea, true, false);
                } else {
                    if (state != ItemState.is_Normal) {
                        if (state == ItemState.is_Blessed) {
                            e_SwitchDimension(EffectID.eid_SwitchDimension, creature, source, state, InvokeMode.im_ItSelf, ext);
                        }
                    } else {
                        creature.TransferTo(creature.LayerID, creature.Field.X, creature.Field.Y, -1, -1, StaticData.MapArea, true, true);
                    }
                }
            }
        }

        public static void e_Tunneling(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            TunnelingRay ray = new TunnelingRay();
            try {
                ray.Exec(creature, EffectID.eid_Tunneling, invokeMode, ext, WandRayMsg(EffectID.eid_Tunneling, source));
            } finally {
                creature.CurrentField.Normalize();
            }
        }

        public static void e_TwelveGates(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            // check-ok

            NWField fld = creature.CurrentField;
            int px = creature.PosX;
            int py = creature.PosY;

            if (creature.Confused) {
                int num = RandomHelper.GetBoundedRnd(2, 4);
                for (int i = 1; i <= num; i++) {
                    while (true) {
                        int x = RandomHelper.GetBoundedRnd(px - 2, px + 2);
                        int y = RandomHelper.GetBoundedRnd(py - 2, py + 2);

                        if (!fld.IsBarrier(x, y) && (px != x || py != y)) {
                            Item item = fld.AddItem(x, y, GlobalVars.iid_Coin);
                            item.Count = (ushort)RandomHelper.GetBoundedRnd(20, 115);
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_GoldAppears));
                            string temp;
                            if (fld.EntryID == GlobalVars.Field_Bazaar) {
                                temp = "BlueWisp";
                            } else {
                                temp = "Wier";
                            }
                            fld.AddCreature(x, y, GlobalVars.nwrDB.FindEntryBySign(temp).GUID);

                            break;
                        }
                    }
                }
            } else {
                int num2 = RandomHelper.GetBoundedRnd(2, 6);
                for (int i = 1; i <= num2; i++) {
                    while (true) {
                        int x = RandomHelper.GetBoundedRnd(px - 2, px + 2);
                        int y = RandomHelper.GetBoundedRnd(py - 2, py + 2);

                        if (!fld.IsBarrier(x, y) && (px != x || py != y)) {
                            UniverseBuilder.Gen_Creature(fld, -1, x, y, true);
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_GatesOpen));

                            break;
                        }
                    }
                }
            }
        }

        public static void e_Ventriloquism(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField f = creature.CurrentField;

            int cx = creature.PosX;
            int cy = creature.PosY;

            for (int y = cy - 5; y <= cy + 5; y++) {
                for (int x = cx - 5; x <= cx + 5; x++) {
                    if (f.IsValid(x, y)) {
                        NWCreature cr = (NWCreature)f.FindCreature(x, y);
                        if (cr != null && !cr.Equals(creature)) {
                            RaceID race = cr.Entry.Race;
                            if (race == RaceID.crDefault || race == RaceID.crHuman) {
                                cr.AddEffect(EffectID.eid_Ventriloquism, ItemState.is_Normal, EffectAction.ea_Persistent, false, "");
                            }
                        }
                    }
                }
            }

            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThrowVoice));
        }

        public static void e_Vertigo(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Wands(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            int num = RandomHelper.GetRandom(2);
            if (num != 0) {
                if (num == 1) {
                    AuxUtils.ExStub("todo: !!!");
                    int i = RandomHelper.GetBoundedRnd(0, GlobalVars.dbWands.Count - 1);
                    ItemEntry entry = (ItemEntry)GlobalVars.nwrDB.GetEntry(i);
                    EffectID eff = entry.Effects[0].EffID;
                    Effect.InvokeEffect(eff, creature, source, InvokeMode.im_Use, EffectAction.ea_Instant, ext);
                }
            } else {
                NWField f = creature.CurrentField;

                for (int y = creature.PosY - 3; y <= creature.PosY + 3; y++) {
                    for (int x = creature.PosX - 3; x <= creature.PosX + 3; x++) {
                        NWTile tile = (NWTile)f.GetTile(x, y);
                        if (tile != null) {
                            int fg = tile.ForeBase;
                            if (fg != PlaceID.pid_Tree) {
                                if (fg == PlaceID.pid_DeadTree) {
                                    tile.Foreground = PlaceID.pid_Undefined;
                                }
                            } else {
                                tile.Foreground = PlaceID.pid_DeadTree;
                            }

                            NWCreature cr = (NWCreature)f.FindCreature(x, y);
                            if (!cr.Equals(creature)) {
                                int num4 = RandomHelper.GetRandom(4);
                                if (num4 != 0) {
                                    if (num4 != 1) {
                                        if (num4 != 2) {
                                            if (num4 == 3) {
                                                cr.AddEffect(EffectID.eid_LegsMissing, ItemState.is_Normal, EffectAction.ea_Persistent, false, BaseLocale.GetStr(RS.rs_LegsGone));
                                            }
                                        } else {
                                            RaceID race = cr.Entry.Race;
                                            if (race == RaceID.crDefault || race == RaceID.crHuman) {
                                                cr.CLSID = GlobalVars.nwrDB.FindEntryBySign("Rat").GUID;
                                            }
                                        }
                                    } else {
                                        e_Relocation(EffectID.eid_Relocation, cr, source, ItemState.is_Normal, InvokeMode.im_ItSelf, null);
                                    }
                                } else {
                                    e_Insanity(EffectID.eid_Insanity, cr, source, ItemState.is_Normal, InvokeMode.im_Use, null);
                                }
                            }
                        }
                    }
                }
            }
        }

        public static void e_DisruptionHornUse(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;
            AuxUtils.ExStub("Great thunder rumbles.");
            AuxUtils.ExStub("Your horn explodes! ");
            AuxUtils.ExStub("Your horn is nearly split.");
            AuxUtils.ExStub("Your horn begins to crack.");
            AuxUtils.ExStub("The walls and floor are too tough.");

            DungeonRoom dunRoom = creature.FindDungeonRoom();
            if (dunRoom != null) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_CeilingCracks));
                int turns = Effect.GetDuration(EffectID.eid_CrushRoof, ItemState.is_Normal, false);
                EffectExt newExt = new EffectExt();
                newExt.SetParam(EffectParams.ep_DunRoom, dunRoom);
                MapEffect eff = new MapEffect(creature.Space, fld, EffectID.eid_CrushRoof, null, EffectAction.ea_LastTurn, turns, 0);
                eff.Ext = newExt;

                AbstractMap map = (AbstractMap)dunRoom.Owner;
                if (map is NWField) {
                    ((NWField)map).Effects.Add(eff);
                } else {
                    if (map is NWLayer) {
                        ((NWLayer)map).Effects.Add(eff);
                    }
                }
            }
        }

        public static void e_WaterTrap(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            DungeonRoom dunRoom = creature.FindDungeonRoom();
            if (dunRoom != null) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_FloorBeneathYouSinks));

                EffectExt newExt = new EffectExt();
                newExt.SetParam(EffectParams.ep_DunRoom, dunRoom);

                AbstractMap map = (AbstractMap)dunRoom.Owner;

                MapEffect eff = new MapEffect(creature.Space, map, EffectID.eid_Flood, null, EffectAction.ea_EachTurn, 20, 0);
                eff.Ext = newExt;

                if (map is NWField) {
                    ((NWField)map).Effects.Add(eff);
                } else {
                    if (map is NWLayer) {
                        ((NWLayer)map).Effects.Add(eff);
                    }
                }
            }
        }

        public static void e_WeirdFume_Skill(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWCreature enemy = (NWCreature)ext.GetParam(EffectParams.ep_Creature);

            GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_WeirdFumeEnvelops));
            switch (RandomHelper.GetRandom(8)) {
                case 0:
                    enemy.CurrentField.Close(true);
                    GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_MindIsRearranged));
                    break;

                case 1:
                    SimpleAttack(enemy, EffectID.eid_WeirdFume_Acid, DamageKind.Physical, "");
                    GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_AcidDropletsFall));
                    break;

                case 2:
                    if (enemy.Body != null) {
                        enemy.Body.AddPart((int)BodypartType.bp_Finger);
                        GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_FingerGrow));
                    }
                    break;

                case 3:
                    if (enemy.Body != null) {
                        enemy.Body.AddPart((int)BodypartType.bp_Eye);
                        GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_EyeGrow));
                    }
                    break;

                case 4:
                    {
                        CreatureSex sex = enemy.Sex;
                        if (sex != CreatureSex.csFemale && sex != CreatureSex.csMale) {
                            GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_Unchanged));
                        } else {
                            CreatureSex sex2 = enemy.Sex;
                            if (sex2 != CreatureSex.csFemale) {
                                if (sex2 == CreatureSex.csMale) {
                                    enemy.Sex = CreatureSex.csFemale;
                                }
                            } else {
                                enemy.Sex = CreatureSex.csMale;
                            }
                            GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_GenderChange));
                        }
                        break;
                    }
                case 5:
                    {
                        GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_Unchanged));
                        break;
                    }
                case 6:
                    {
                        GlobalVars.nwrWin.ShowText(enemy, BaseLocale.GetStr(RS.rs_BrainThrobs));
                        AuxUtils.ExStub("todo: vertiginous");
                        break;
                    }
                case 7:
                    {
                        AuxUtils.ExStub("todo: //nwrWin.ShowText(enemy, rsList('You are dazed.']);");
                        AuxUtils.ExStub("todo: -> confusion");
                        break;
                    }
            }
        }

        public static void e_Winds(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            AuxUtils.ExStub("todo: ???");
        }

        public static void e_Wishing(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            GlobalVars.nwrWin.HideInventory();
            GlobalVars.nwrWin.ShowInput(BaseLocale.GetStr(RS.rs_WishedItem), WishAcceptProc);
        }

        public static void e_Withered(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (invokeMode == InvokeMode.im_Use) {
                creature.AddEffect(EffectID.eid_Withered, state, EffectAction.ea_EachTurn, true, BaseLocale.GetStr(RS.rs_YouAreWithered));
            }
        }

        private static void FieldDries(NWField f, NWCreature creature)
        {
            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_AllLiquidIsDriedHere));
            if (f.LandID != GlobalVars.Land_Ocean) {
                bool mud = false;
                bool water = false;
                bool quicksand = false;
                bool lava = false;

                for (int y = 0; y < f.Height; y++) {
                    for (int x = 0; x < f.Width; x++) {
                        NWTile tile = (NWTile)f.GetTile(x, y);

                        int bg = tile.BackBase;
                        if (UniverseBuilder.IsWaters(bg)) {
                            tile.Background = (ushort)PlaceID.pid_Grass;
                            tile.BackgroundExt = (ushort)PlaceID.pid_Undefined;
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

                        int fg = tile.ForeBase;
                        if (UniverseBuilder.IsWaters(fg)) {
                            tile.Foreground = (ushort)PlaceID.pid_Undefined;
                            tile.ForegroundExt = (ushort)PlaceID.pid_Undefined;
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

                f.Normalize();

                if (mud) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_MudDries));
                }
                if (water) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_WaterDries));
                }
                if (quicksand) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_QuicksandDries));
                }
                if (lava) {
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_LavaDries));
                }
            }

            EntityList cs = f.Creatures;
            for (int i = 0; i < cs.Count; i++) {
                NWCreature creat = (NWCreature)cs.GetItem(i);

                int num4 = creat.Items.Count;
                for (int j = 0; j < num4; j++) {
                    Item it = creat.Items.GetItem(j);
                    if (it.Kind == ItemKind.ik_Potion) {
                        it.CLSID = GlobalVars.iid_Vial;
                    }
                }
            }

            int num5 = f.Items.Count;
            for (int i = 0; i < num5; i++) {
                Item it = f.Items.GetItem(i);
                if (it.Kind == ItemKind.ik_Potion) {
                    it.CLSID = GlobalVars.iid_Vial;
                }
            }
        }

        public static void e_Wonder(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            NWField fld = creature.CurrentField;

            AuxUtils.ExStub("You hear the sounds of a marketplace.");
            AuxUtils.ExStub("You hear a faraway voice.");
            AuxUtils.ExStub("You smell gold nearby.");
            AuxUtils.ExStub("You hear the distant swaying of plants.");
            AuxUtils.ExStub("You recall the smell of freshly cut barley.");
            AuxUtils.ExStub("You feel a thousand angry voices in your head.");
            AuxUtils.ExStub("You feel strangely pacifistic.");
            AuxUtils.ExStub("Everything is slowing down.");
            AuxUtils.ExStub("Time warps slightly around you.");
            AuxUtils.ExStub("Time is passing you by.");
            AuxUtils.ExStub("You narrowly escape the wrath of an evil being.");
            AuxUtils.ExStub("The ");
            AuxUtils.ExStub(" flops helplessly and dies.");
            AuxUtils.ExStub(" suffocates.");
            AuxUtils.ExStub("antijag");
            AuxUtils.ExStub("You have awakened something evil.");
            AuxUtils.ExStub("The humidity increases. comment:(all field to mud)");
            AuxUtils.ExStub("A strange energy whirls around you. (оживляет предметы)");
            AuxUtils.ExStub("Particles of energy surge around you.");
            AuxUtils.ExStub(" is swept away by the tree! ");
            AuxUtils.ExStub("Tiny seeds begin to float around you.");

            if (state == ItemState.is_Blessed || state == ItemState.is_Cursed) {
                e_Transformation(EffectID.eid_Transformation, creature, source, state, InvokeMode.im_ItSelf, ext);
            } else {
                if (creature.Confused) {
                    UniverseBuilder.Gen_BigRiver(fld);
                }

                int i = RandomHelper.GetBoundedRnd(1, 10);
                switch (i) {
                    case 1:
                        {
                            // check-ok
                            for (int y = creature.PosY - 2; y <= creature.PosY + 2; y++) {
                                for (int x = creature.PosX - 2; x <= creature.PosX + 2; x++) {
                                    if (!fld.IsBarrier(x, y) && !creature.Location.Equals(x, y)) {
                                        fld.GetTile(x, y).Foreground = (ushort)PlaceID.pid_Tree;
                                    }
                                }
                            }
                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_TreesSprout));
                            break;
                        }

                    case 3:
                        FieldDries(fld, creature);
                        break;

                    case 4:
                        e_Transformation(EffectID.eid_Transformation, creature, source, state, InvokeMode.im_ItSelf, ext);
                        break;

                    case 5:
                        {
                            int num3 = fld.Creatures.Count;
                            for (int k = 0; k < num3; k++) {
                                fld.Creatures.GetItem(k).InvertHostility();
                            }
                            break;
                        }

                    case 6:
                        {
                            // check-ok
                            i = GlobalVars.nwrDB.FindEntryBySign("Retchweed").GUID;

                            for (int y = 0; y < StaticData.FieldHeight; y++) {
                                for (int x = 0; x < StaticData.FieldWidth; x++) {
                                    NWTile tile = (NWTile)fld.GetTile(x, y);
                                    if (tile.Foreground == PlaceID.pid_Tree) {
                                        int num6 = RandomHelper.GetRandom(2);
                                        if (num6 != 0) {
                                            if (num6 == 1) {
                                                tile.Foreground = (ushort)PlaceID.pid_Undefined;
                                                fld.AddCreature(x, y, i);
                                            }
                                        } else {
                                            tile.Foreground = (ushort)PlaceID.pid_DeadTree;
                                        }
                                    }
                                }
                            }

                            GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ForestTransforms));
                            break;
                        }

                    case 7:
                        e_Speedup(EffectID.eid_Speedup, creature, source, state, InvokeMode.im_ItSelf, ext);
                        break;

                    case 8:
                        {
                            int num7 = fld.Creatures.Count;
                            for (i = 0; i < num7; i++) {
                                NWCreature creat = fld.Creatures.GetItem(i);
                                if (!creat.Equals(creature)) {
                                    e_Speedup(EffectID.eid_Speedup, creat, source, state, InvokeMode.im_ItSelf, ext);
                                }
                            }
                            break;
                        }
                    case 9:
                        creature.SetAbility(AbilityID.Resist_Teleport, 100);
                        creature.SetSkill(SkillID.Sk_Relocation, 5);
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveGetTeleportResistance));
                        break;

                    case 10:
                        {
                            AbilityID ab = (AbilityID)RandomHelper.GetBoundedRnd((int)AbilityID.Resist_Cold, (int)AbilityID.Resist_SleepRay);
                            creature.SetAbility(ab, 100);

                            switch (ab) {
                                case AbilityID.Resist_Cold:
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelStrangelyCool));
                                    break;
                                case AbilityID.Resist_Heat:
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelStrangelyWarm));
                                    break;
                                case AbilityID.Resist_Acid:
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourSkinFeelsSlick));
                                    break;
                                case AbilityID.Resist_Poison:
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourLiverSquirms));
                                    CureEffect(creature, EffectID.eid_Poisoned, BaseLocale.GetStr(RS.rs_VenomIsNeutralized));
                                    break;
                                case AbilityID.Resist_Ray:
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YourBodyHasStrangeSheen));
                                    break;
                                case AbilityID.Resist_DeathRay:
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouWillLiveForever));
                                    break;
                                case AbilityID.Resist_Petrification:
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouFeelOneWithEarth));
                                    CureStoning(creature, null);
                                    break;

                                default:
                                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_YouveGetANewResistance));
                                    break;
                            }
                            break;
                        }
                }
            }
        }

        public static void e_Writing(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            if (creature.Items.FindByCLSID(GlobalVars.iid_Stylus) == null) {
                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_StylusNecessary));
            } else {
                object obj = ext.GetParam(EffectParams.ep_ScrollID);
                int sid = (int)obj;

                Item scroll = (Item)ext.GetParam(EffectParams.ep_Item);
                if (scroll.Kind == ItemKind.ik_Scroll) {
                    scroll.CLSID = sid;
                    AuxUtils.ExStub("msg: You rewrite the scroll.");
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.Format(RS.rs_ScrollWrited, new object[]{ scroll.Name }));
                } else {
                    // message?
                }
            }
        }

        public static void e_CaughtInNet(EffectID effectID, NWCreature creature, object source, ItemState state, InvokeMode invokeMode, EffectExt ext)
        {
            switch (invokeMode) {
                case InvokeMode.im_Use:
                    RaceID race = creature.Entry.Race;
                    if (race == RaceID.crAesir || race == RaceID.crEvilGod || race == RaceID.crDaemon) {
                        return;
                    }
                    string msg = string.Format(BaseLocale.GetStr(RS.rs_IsCaughtInTheNet), creature.Name);
                    creature.AddEffect(EffectID.eid_CaughtInNet, state, EffectAction.ea_Persistent, false, msg);
                    break;

                case InvokeMode.im_ItSelf:
                    break;

                case InvokeMode.im_FinAction:
                    break;
            }
        }

        private static NWCreature AnimateDeadBody(NWCreature creature, Item extItem)
        {
            NWField fld = creature.CurrentField;
            return GlobalVars.nwrGame.RespawnDeadBody(fld, creature.Location, extItem);
        }

        public static void Deanimate(NWField field, NWCreature monster, BaseTile tile, int tileID)
        {
            tile.Background = (ushort)tileID;
            string ms = monster.GetDeclinableName(Number.nSingle, Case.cAccusative);
            monster.CheckTile(false);
            field.Creatures.Remove(monster);
            GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.Format(RS.rs_RayDeanimateMonster, new object[]{ ms }));
        }

        private static int FindExtinctCreature(string input)
        {
            int num = GlobalVars.dbCreatures.Count;
            for (int i = 0; i < num; i++) {
                DataEntry entry = GlobalVars.nwrDB.GetEntry(GlobalVars.dbCreatures[i]);
                if (entry is DeclinableEntry && ((DeclinableEntry)entry).HasNounMorpheme(input)) {
                    return GlobalVars.dbCreatures[i];
                }
            }
            return -1;
        }

        private static void ExtinctionAcceptProc(string input)
        {
            int id = FindExtinctCreature(input);
            CreatureEntry cEntry = (CreatureEntry)GlobalVars.nwrDB.GetEntry(id);

            Player player = GlobalVars.nwrGame.Player;
            if (id > -1) {
                if (cEntry.Race == player.Entry.Race) {
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_AsYouWish));
                    if (cEntry.Race == RaceID.crHuman) {
                        GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_HumanRaceDestroyed));
                    }
                    player.Death(BaseLocale.GetStr(RS.rs_DestroyedHisRace), null);
                    GlobalVars.nwrWin.DoEvent(EventID.event_Dead, null, null, null);
                } else {
                    if (cEntry.Extinctable) {
                        GlobalVars.nwrWin.ProgressInit(GlobalVars.dbLayers.Count);

                        int num = GlobalVars.dbLayers.Count;
                        for (int i = 0; i < num; i++) {
                            NWLayer layer = GlobalVars.nwrGame.GetLayer(i);

                            int num2 = layer.H;
                            for (int fy = 0; fy < num2; fy++) {
                                int num3 = layer.W;
                                for (int fx = 0; fx < num3; fx++) {
                                    NWField f = layer.GetField(fx, fy);

                                    for (int j = f.Creatures.Count - 1; j >= 0; j--) {
                                        NWCreature cr = f.Creatures.GetItem(j);
                                        if (cr.CLSID == id) {
                                            cr.DropAll();
                                            cr.CheckTile(false);
                                            f.Creatures.Remove(cr);
                                        }
                                    }
                                }
                            }

                            GlobalVars.nwrWin.ProgressLabel(BaseLocale.GetStr(RS.rs_Henocide) + " (" + Convert.ToString(i + 1) + "/" + Convert.ToString(GlobalVars.dbLayers.Count) + ")");
                            GlobalVars.nwrWin.ProgressStep();
                        }

                        GlobalVars.nwrWin.ProgressDone();
                        player.Morality = (sbyte)((int)player.Morality - 15);
                        GlobalVars.nwrGame.SetVolatileState(id, VolatileState.Extincted);
                        GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_RaceDestroyed) + cEntry.GetNounDeclension(Number.nPlural, Case.cAccusative) + ".");
                        GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_MoraleLowered));
                    } else {
                        GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_RaceUndestroyable));
                    }
                }
            } else {
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_NoSuchRace));
            }
        }

        public static void FireHit(EffectID effectID, NWCreature anAttacker, NWCreature aVictim, int aDmg)
        {
            HitByRay(EffectID.eid_Fire, anAttacker, aVictim, aDmg, BaseLocale.Format(RS.rs_CreatureIncinerated, new object[]{ aVictim.Name }), BaseLocale.GetStr(RS.rs_BurntToCrisp));
        }

        private static void Geology_PrepareTile(NWField fld, NWCreature creature, EffectExt ext, ExtPoint p, NWTile aTile)
        {
            bool res = false;
            object obj = ext.GetParam(EffectParams.ep_TileID);

            ushort tile_id = ((ushort)obj);
            ushort tile_id_inv = PlaceID.pid_Undefined;

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

            AuxUtils.ExStub("todo:  msg");

            switch (tile_id) {
                case PlaceID.pid_StairsDown:
                case PlaceID.pid_HoleDown:
                    {
                        int lid = -1;
                        ExtPoint fp = ExtPoint.Empty;

                        if (fld.Layer.EntryID == GlobalVars.Layer_Midgard) {
                            int x = fld.Coords.X;
                            int y = fld.Coords.Y;
                            if ((x >= 3 && x <= 5) && (y >= 4 && y <= 6)) {
                                lid = GlobalVars.Layer_Svartalfheim1;
                                fp.X = fld.Coords.X - 3;
                                fp.Y = fld.Coords.Y - 4;
                            } else {
                                res = true;
                            }
                        } else if (fld.Layer.EntryID == GlobalVars.Layer_Svartalfheim1) {
                            lid = GlobalVars.Layer_Svartalfheim2;
                            fp = fld.Coords;
                        } else if (fld.Layer.EntryID == GlobalVars.Layer_Svartalfheim2) {
                            if (fld.Coords.X == 2 && fld.Coords.Y == 2) {
                                res = true;
                            } else {
                                lid = GlobalVars.Layer_Svartalfheim3;
                                fp = fld.Coords;
                            }
                        }

                        if (lid >= 0) {
                            NWField new_fld = GlobalVars.nwrGame.GetField(lid, fp.X, fp.Y);
                            ExtPoint new_pos;

                            if (creature.CanMove(new_fld, p.X, p.Y)) {
                                new_pos = p;
                            } else {
                                new_pos = new_fld.SearchFreeLocation();
                            }

                            fld.AddGate(tile_id, p.X, p.Y, lid, new_fld.Coords, new_pos);
                            new_fld.AddGate(tile_id_inv, new_pos.X, new_pos.Y, fld.Layer.EntryID, fld.Coords, p);
                        } else {
                            res = true;
                        }
                    }
                    break;

                case PlaceID.pid_StairsUp:
                case PlaceID.pid_HoleUp:
                    {
                        int lid = -1;
                        ExtPoint fp = ExtPoint.Empty;

                        if (fld.Layer.EntryID == GlobalVars.Layer_Svartalfheim3) {
                            if (fld.LandID != GlobalVars.Land_Bazaar) {
                                lid = GlobalVars.Layer_Svartalfheim2;
                                fp = fld.Coords;
                            } else {
                                GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThisAreaIsMagicallyBounded));
                                return;
                            }
                        } else {
                            if (fld.Layer.EntryID == GlobalVars.Layer_Svartalfheim2) {
                                lid = GlobalVars.Layer_Svartalfheim1;
                                fp = fld.Coords;
                            } else {
                                if (fld.Layer.EntryID == GlobalVars.Layer_Svartalfheim1) {
                                    lid = GlobalVars.Layer_Midgard;
                                    fp.X = fld.Coords.X + 3;
                                    fp.Y = fld.Coords.Y + 4;
                                }
                            }
                        }

                        if (lid >= 0) {
                            NWField new_fld = GlobalVars.nwrGame.GetField(lid, fp.X, fp.Y);
                            ExtPoint new_pos;
                            if (creature.CanMove(new_fld, p.X, p.Y)) {
                                new_pos = p;
                            } else {
                                new_pos = new_fld.SearchFreeLocation();
                            }

                            fld.AddGate(tile_id, p.X, p.Y, lid, new_fld.Coords, new_pos);
                            new_fld.AddGate(tile_id_inv, new_pos.X, new_pos.Y, fld.Layer.EntryID, fld.Coords, p);
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

            ushort tid = (ushort)tile_id;
            if ((StaticData.dbPlaces[tid].Signs.Contains(PlaceFlags.psBackground))) {
                aTile.Background = tid;
                return;
            }

            if ((StaticData.dbPlaces[tid].Signs.Contains(PlaceFlags.psForeground))) {
                aTile.Foreground = tid;
            }
        }

        private static int FindWishItem(string str)
        {
            int num = GlobalVars.dbItems.Count;
            for (int i = 0; i < num; i++) {
                int id = GlobalVars.dbItems[i];
                DataEntry entry = GlobalVars.nwrDB.GetEntry(id);
                if (entry is DeclinableEntry && ((DeclinableEntry)entry).HasNounMorpheme(str)) {
                    return id;
                }
            }
            return -1;
        }

        private static int IsTabooItem(string sign)
        {
            for (int i = 0; i < DbTabooItems.Length; i++) {
                if (DbTabooItems[i].Origin.Equals(sign)) {
                    return i;
                }
            }
            return -1;
        }

        private static void WishAcceptProc(string input)
        {
            string name;
            string count;
            if (ConvertHelper.IsValidInt(AuxUtils.GetToken(input, " ", 1))) {
                count = AuxUtils.GetToken(input, " ", 1);
                name = AuxUtils.GetToken(input, " ", 2);
            } else {
                count = "1";
                name = input;
            }

            AuxUtils.ExStub("todo: You receive nothing.");
            AuxUtils.ExStub("todo: Your wish does not materialize.");
            AuxUtils.ExStub("todo: little");
            AuxUtils.ExStub("todo: much");
            AuxUtils.ExStub("todo: You have wished for too %s.");
            AuxUtils.ExStub("todo: Your pack is full.");

            int id = FindWishItem(name);
            ItemEntry cEntry = (ItemEntry)GlobalVars.nwrDB.GetEntry(id);
            if (id > -1) {
                Player player = GlobalVars.nwrGame.Player;
                bool res = false;

                int tabIdx = IsTabooItem(cEntry.Sign);
                if (tabIdx >= 0) {
                    cEntry = ((ItemEntry)GlobalVars.nwrDB.FindEntryBySign(DbTabooItems[tabIdx].Substitute));
                    Item.GenItem(player, cEntry.GUID, 1, false);
                    res = true;
                } else {
                    if (!cEntry.Unique) {
                        Item.GenItem(player, id, Convert.ToInt32(count), true);
                        res = true;
                    }
                }
                if (res) {
                    int i = RandomHelper.GetBoundedRnd(910, 912);
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(i));
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_WishedItemTaked));
                }
            } else {
                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_NoSuchItem));
            }
        }

        private static void CrushTile(AbstractMap map, int xx, int yy)
        {
            NWTile tile = (NWTile)map.GetTile(xx, yy);
            tile.Foreground = PlaceID.pid_Rubble;

            NWCreature cr = (NWCreature)map.FindCreature(xx, yy);
            if (cr != null && cr.Effects.FindEffectByID(EffectID.eid_Phase) == null) {
                GlobalVars.nwrWin.ShowText(cr, BaseLocale.GetStr(RS.rs_FallingDebrisHitsYou));
                cr.Death(BaseLocale.GetStr(RS.rs_KilledByRoofCrushing), null);
            }
        }

        private static void CrushRoof(DungeonRoom dunRoom, bool total)
        {
            // check-ok

            AbstractMap map = (AbstractMap)dunRoom.Owner;
            ExtRect dunArea = dunRoom.Area.Clone();
            dunArea.Inflate(-1, -1);

            if (total) {
                for (int y = dunArea.Top; y <= dunArea.Bottom; y++) {
                    for (int x = dunArea.Left; x <= dunArea.Right; x++) {
                        CrushTile(map, x, y);
                    }
                }
            } else {
                int num3 = RandomHelper.GetBoundedRnd(3, 7);
                for (int i = 1; i <= num3; i++) {
                    int x = RandomHelper.GetBoundedRnd(dunArea.Left, dunArea.Right);
                    int y = RandomHelper.GetBoundedRnd(dunArea.Top, dunArea.Bottom);
                    CrushTile(map, x, y);
                }
            }

            map.Normalize();
        }

        private static void Ray(EffectID effectID, NWCreature creature, InvokeMode invokeMode, EffectExt ext, DamageKind dk)
        {
            MonsterSkillRay ray = new MonsterSkillRay();
            ray.DmgKind = dk;
            try {
                ray.Exec(creature, effectID, invokeMode, ext, "");
                AuxUtils.ExStub("rsList(rs_StoningRayIsZapped]");
            } finally {
                creature.CurrentField.Normalize();
            }
        }

        private static void HitByRay(EffectID effectID, NWCreature attacker, NWCreature victim, int damage, string hitMsg, string deathMsg)
        {
            AuxUtils.ExStub("todo: распространить на все эффекты с лучом и прямым уроном");
            NWCreature cr;
            if (victim.GetAbility(AbilityID.Ab_RayReflect) <= 0) {
                cr = victim;
            } else {
                cr = attacker;
            }
            if (cr.HasAffect(effectID)) {
                AuxUtils.ExStub("todo: aMsg := 'x отразил луч' + aMsg;");
                GlobalVars.nwrWin.ShowText(victim, hitMsg);
                cr.ApplyDamage(damage, DamageKind.Radiation, null, deathMsg);
            }
        }

        private static void SimpleAttack(NWCreature enemy, EffectID effect, DamageKind dk, string deathMsg)
        {
            if (enemy.HasAffect(effect)) {
                MNXRange dmg = EffectsData.dbEffects[(int)effect].Damage;
                enemy.ApplyDamage(RandomHelper.GetBoundedRnd(dmg.Min, dmg.Max), dk, null, deathMsg);
            }
        }

        private static string WandRayMsg(EffectID effectID, object source)
        {
            if (source != null && source is Item) {
                Item item = (Item)source;

                string tmp = item.Entry.Name;
                tmp = tmp.Substring(tmp.IndexOf(" ") + 1);

                return BaseLocale.Format(RS.rs_RayComesOut, new object[]{ tmp });
            }
            throw new Exception("XRay(" + Convert.ToString((int)effectID) + "): not implemented");
        }
    }
}
