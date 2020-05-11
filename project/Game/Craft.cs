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
using BSLib;
using NWR.Creatures;
using NWR.Database;
using NWR.Game;
using NWR.Game.Types;
using NWR.Items;
using ZRLib.Core;

namespace NWR.Game
{
    public sealed class Craft
    {
        private static readonly RecipeRec[] dbRecipes;

        public const int RC_Ok = 0;
        public const int RC_LackIngredients = 1;
        public const int RC_DifferentMetals = 2;

        private readonly Player fPlayer;

        public Craft(Player player)
        {
            fPlayer = player;
        }

        static Craft()
        {
            dbRecipes = new RecipeRec[17];
            dbRecipes[0] = new RecipeRec("Potion_Mystery", "*");
            dbRecipes[1] = new RecipeRec("Potion_Rejuvenation", "Potion_Curing+Potion_RestoreMagic");
            dbRecipes[2] = new RecipeRec("Potion_Lycanthropy", "_Werewolf+Potion_Transformation");
            dbRecipes[3] = new RecipeRec("Potion_Curing", "Potion_DrinkingWater+GnarledRoot");
            dbRecipes[4] = new RecipeRec("Potion_Speed", "_Blur+Potion_DrinkingWater");
            dbRecipes[5] = new RecipeRec("Potion_Blindness", "_IvyCreeper+Potion_DrinkingWater+RedMushroom");
            dbRecipes[6] = new RecipeRec("Potion_Constitution", "Potion_DrinkingWater+BleachedRoot");
            dbRecipes[7] = new RecipeRec("Potion_Hallucination", "Potion_DrinkingWater+SpeckledGrowth+Potion_Mead");
            dbRecipes[8] = new RecipeRec("Potion_Endurance", "GreenMushroom+Potion_DrinkingWater");
            dbRecipes[9] = new RecipeRec("Potion_SecondLife", "_LowerDwarf+!Potion_Curing+!Potion_HolyWater");
            dbRecipes[10] = new RecipeRec("Potion_Transformation", "_Morph+Potion_DrinkingWater");
            dbRecipes[11] = new RecipeRec("Potion_Depredation", "_Nymph+Potion_DrinkingWater");
            dbRecipes[12] = new RecipeRec("Potion_Translucence", "_Stalker+Potion_DrinkingWater");
            dbRecipes[13] = new RecipeRec("Potion_Venom", "_Glard+Potion_DrinkingWater");
            dbRecipes[14] = new RecipeRec("Potion_Paralysis", "_Homunculus+Potion_DrinkingWater");
            dbRecipes[15] = new RecipeRec("Potion_Experience", "_Wraith+Potion_DrinkingWater");
            dbRecipes[16] = new RecipeRec("Potion_DimensionSwitch", "_Breleor+Potion_DrinkingWater");
        }

        private void ShowText(string text)
        {
            fPlayer.Space.ShowText(text);
        }

        private static int FindAlchemyResult(EntityList ingredients, EntityList ready, ref bool refExact)
        {
            for (int i = 1; i < dbRecipes.Length; i++) {
                RecipeRec recipe = dbRecipes[i];

                int t_cnt = AuxUtils.GetTokensCount(recipe.Ingredients, "+");
                int finded = 0;
                ready.Clear();

                for (int t = 1; t <= t_cnt; t++) {
                    string tok = AuxUtils.GetToken(recipe.Ingredients, "+", (sbyte)t);
                    char f = tok[0];
                    if (f != '_' && f != '!') {
                        f = '?';
                    } else {
                        tok = tok.Substring(1, tok.Length - 1);
                    }

                    if (f == '_') {
                        Item item = (Item)ingredients.FindByCLSID(GlobalVars.iid_DeadBody);
                        NWCreature db = (NWCreature)item.Contents.GetItem(0);
                        if (db.Entry.Sign.CompareTo(tok) == 0) {
                            finded++;
                            ready.Add(item);
                        }
                    } else {
                        int id = GlobalVars.nwrDB.FindEntryBySign(tok).GUID;
                        Item item = (Item)ingredients.FindByCLSID(id);
                        if (item != null && (f == '?' || (f == '!' && item.State == ItemState.is_Blessed))) {
                            finded++;
                            ready.Add(item);
                        }
                    }
                }

                if (finded >= t_cnt) {
                    refExact = (finded == t_cnt);
                    return i;
                }
            }

            refExact = false;
            return -1;
        }

        public Item Alchemy(EntityList ingredients)
        {
            Item result = null;

            try {
                Item vial = (Item)fPlayer.Items.FindByCLSID(GlobalVars.iid_Vial);

                if (ingredients.Count < 2) {
                    ShowText(BaseLocale.GetStr(RS.rs_IngredientsLack));
                    return null;
                } else {
                    if (vial == null) {
                        ShowText(BaseLocale.GetStr(RS.rs_NoFreeVial));
                        return null;
                    } else {
                        //int alchemySkill = this.getSkill(TSkillID.Sk_Alchemy);
                        EntityList ready = new EntityList(null, false);
                        try {
                            bool exact = false;
                            int rec = FindAlchemyResult(ingredients, ready, ref exact);

                            if (rec >= 0) {
                                EntityList list;
                                string p;

                                if (exact) {
                                    list = ready;
                                    p = dbRecipes[rec].Potion;
                                } else {
                                    list = ingredients;
                                    p = dbRecipes[0].Potion;
                                }

                                int num = list.Count;
                                for (int i = 0; i < num; i++) {
                                    Item item = (Item)list.GetItem(i);
                                    ItemKind kind = item.Kind;
                                    if (kind == ItemKind.ik_Potion) {
                                        item.State = ItemState.is_Normal;
                                        item.CLSID = GlobalVars.iid_Vial;
                                    } else {
                                        fPlayer.DeleteItem(item);
                                    }
                                }

                                vial.CLSID = GlobalVars.nwrDB.FindEntryBySign(p).GUID;
                                ShowText(BaseLocale.GetStr(RS.rs_YouProduceNewConcoction));
                            } else {
                                vial.CLSID = GlobalVars.nwrDB.FindEntryBySign("Potion_Mystery").GUID;
                                ShowText(BaseLocale.GetStr(RS.rs_MixtureBubblesHorribly));
                                // TODO: required to verify the conditions

                                if (AuxUtils.Chance(5)) {
                                    ShowText(BaseLocale.GetStr(RS.rs_Boom));
                                    fPlayer.ApplyDamage(RandomHelper.GetBoundedRnd(25, 50), DamageKind.Physical, null, BaseLocale.GetStr(RS.rs_KilledByExperiment));
                                }
                            }
                            result = vial;
                            fPlayer.Space.DoEvent(EventID.event_ItemMix, this, null, vial);
                        } finally {
                            ready.Dispose();
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Craft.alchemy(): " + ex.Message);
            }

            return result;
        }

        public void AddForgeItem(int itemID, float weight, MaterialKind material)
        {
            Item res = new Item(fPlayer.Space, fPlayer);
            res.CLSID = itemID;
            res.Weight = weight;
            res.Count = 1;
            //res.Material = material;
            fPlayer.Items.Add(res, res.Countable);
        }

        public int CheckForgeIngredients(EntityList ingredients, ref float  sum, ref MaterialKind  mat)
        {
            sum = 0f;
            mat = MaterialKind.mk_None;
            if (ingredients.Count < 1) {
                return RC_LackIngredients;
            }

            int num = ingredients.Count;
            for (int i = 0; i < num; i++) {
                Item item = (Item)ingredients.GetItem(i);
                sum = sum + item.Weight;

                if (mat == MaterialKind.mk_None) {
                    mat = item.Material;
                } else {
                    if (mat != item.Material) {
                        return RC_DifferentMetals;
                    }
                }
            }

            return RC_Ok;
        }

        public int ForgeItem(EntityList ingredients, int itemID)
        {
            float sum = 0F;
            MaterialKind i = MaterialKind.mk_None;
            int result = CheckForgeIngredients(ingredients, ref sum, ref i);

            if (result != RC_Ok) {
                if (result == RC_DifferentMetals) {
                    // dummy
                }
            } else {
                int num = ingredients.Count;
                for (int j = 0; j < num; j++) {
                    Item item = (Item)ingredients.GetItem(j);
                    fPlayer.DeleteItem(item);
                }

                ingredients.Clear();
                if (itemID == GlobalVars.iid_Ingot) {
                    AddForgeItem(GlobalVars.iid_Ingot, sum, i);
                } else {
                    ItemEntry iEntry = (ItemEntry)GlobalVars.nwrDB.GetEntry(itemID);
                    AddForgeItem(itemID, iEntry.Weight, i);
                    float rem = ((sum - iEntry.Weight));
                    if (rem > 0f) {
                        AddForgeItem(GlobalVars.iid_Ingot, rem, i);
                    }
                }
            }
            return result;
        }

        public string CheckSmithyTools()
        {
            string result = "";
            ItemsList items = fPlayer.Items;
            if (items.FindByCLSID(GlobalVars.iid_Anvil) == null) {
                result += BaseLocale.GetStr(RS.rs_NoAnvil);
            }
            if (items.FindByCLSID(GlobalVars.iid_Tongs) == null) {
                if (result.CompareTo("") != 0) {
                    result += ", ";
                }
                result += BaseLocale.GetStr(RS.rs_NoTongs);
            }
            if (items.FindByCLSID(GlobalVars.iid_Wand_Fire) == null) {
                if (result.CompareTo("") != 0) {
                    result += ", ";
                }
                result += BaseLocale.GetStr(RS.rs_NoWand_Fire);
            }
            return result;
        }
    }
}
