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

import jzrlib.utils.AuxUtils;
import jzrlib.core.EntityList;
import jzrlib.utils.Logger;
import jzrlib.utils.RefObject;
import jzrlib.utils.TextUtils;
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.types.DamageKind;
import nwr.core.types.EventID;
import nwr.core.types.ItemKind;
import nwr.core.types.ItemState;
import nwr.core.types.MaterialKind;
import nwr.core.types.RecipeRec;
import nwr.database.ItemEntry;
import nwr.item.Item;
import nwr.item.ItemsList;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public final class Craft
{
    private static final RecipeRec[] dbRecipes;

    public static final int RC_Ok = 0;
    public static final int RC_LackIngredients = 1;
    public static final int RC_DifferentMetals = 2;

    private final Player fPlayer;
    
    public Craft(Player player)
    {
        this.fPlayer = player;
    }

    static {
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

    private void showText(String text)
    {
        this.fPlayer.getSpace().showText(text);
    }

    private int findAlchemyResult(EntityList ingredients, EntityList ready, RefObject<Boolean> refExact)
    {
        for (int i = 1; i < dbRecipes.length; i++) {
            RecipeRec recipe = dbRecipes[i];

            int t_cnt = TextUtils.getTokensCount(recipe.Ingredients, "+");
            int finded = 0;
            ready.clear();

            for (int t = 1; t <= t_cnt; t++) {
                String tok = TextUtils.getToken(recipe.Ingredients, "+", (byte) t);
                char f = tok.charAt(0);
                if (f != '_' && f != '!') {
                    f = '?';
                } else {
                    tok = tok.substring(1, tok.length());
                }

                if (f == '_') {
                    Item item = (Item) ingredients.findByCLSID(GlobalVars.iid_DeadBody);
                    NWCreature db = (NWCreature) item.getContents().getItem(0);
                    if (db.getEntry().Sign.compareTo(tok) == 0) {
                        finded++;
                        ready.add(item);
                    }
                } else {
                    int id = GlobalVars.nwrBase.findEntryBySign(tok).GUID;
                    Item item = (Item) ingredients.findByCLSID(id);
                    if (item != null && (f == '?' || (f == '!' && item.State == ItemState.is_Blessed))) {
                        finded++;
                        ready.add(item);
                    }
                }
            }

            if (finded >= t_cnt) {
                refExact.argValue = (finded == t_cnt);
                return i;
            }
        }

        refExact.argValue = false;
        return -1;
    }

    public final Item alchemy(EntityList ingredients)
    {
        Item result = null;

        try {
            Item vial = (Item) this.fPlayer.getItems().findByCLSID(GlobalVars.iid_Vial);

            if (ingredients.getCount() < 2) {
                this.showText(Locale.getStr(RS.rs_IngredientsLack));
                return null;
            } else {
                if (vial == null) {
                    this.showText(Locale.getStr(RS.rs_NoFreeVial));
                    return null;
                } else {
                    //int alchemySkill = this.getSkill(TSkillID.Sk_Alchemy);
                    EntityList ready = new EntityList(null, false);
                    try {
                        boolean exact = false;
                        RefObject<Boolean> tempRef_exact = new RefObject<>(exact);
                        int rec = this.findAlchemyResult(ingredients, ready, tempRef_exact);
                        exact = tempRef_exact.argValue;

                        if (rec >= 0) {
                            EntityList list;
                            String p;

                            if (exact) {
                                list = ready;
                                p = dbRecipes[rec].Potion;
                            } else {
                                list = ingredients;
                                p = dbRecipes[0].Potion;
                            }

                            int num = list.getCount();
                            for (int i = 0; i < num; i++) {
                                Item item = (Item) list.getItem(i);
                                ItemKind kind = item.getKind();
                                if (kind == ItemKind.ik_Potion) {
                                    item.State = ItemState.is_Normal;
                                    item.setCLSID(GlobalVars.iid_Vial);
                                } else {
                                    this.fPlayer.deleteItem(item);
                                }
                            }

                            vial.setCLSID(GlobalVars.nwrBase.findEntryBySign(p).GUID);
                            this.showText(Locale.getStr(RS.rs_YouProduceNewConcoction));
                        } else {
                            vial.setCLSID(GlobalVars.nwrBase.findEntryBySign("Potion_Mystery").GUID);
                            this.showText(Locale.getStr(RS.rs_MixtureBubblesHorribly));
                            // TODO: required to verify the conditions

                            if (AuxUtils.chance(5)) {
                                this.showText(Locale.getStr(RS.rs_Boom));
                                this.fPlayer.applyDamage(AuxUtils.getBoundedRnd(25, 50), DamageKind.dkPhysical, null, Locale.getStr(RS.rs_KilledByExperiment));
                            }
                        }
                        result = vial;
                        this.fPlayer.getSpace().doEvent(EventID.event_ItemMix, this, null, vial);
                    } finally {
                        ready.dispose();
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("Craft.alchemy(): " + ex.getMessage());
        }

        return result;
    }

    public final void addForgeItem(int itemID, float weight, MaterialKind material)
    {
        Item res = new Item(this.fPlayer.getSpace(), this.fPlayer);
        res.setCLSID(itemID);
        res.setWeight(weight);
        res.Count = 1;
        //res.Material = material;
        this.fPlayer.getItems().add(res, res.isCountable());
    }

    public final int checkForgeIngredients(EntityList ingredients, RefObject<Float> sum, RefObject<MaterialKind> mat)
    {
        sum.argValue = 0f;
        mat.argValue = MaterialKind.mk_None;
        if (ingredients.getCount() < 1) {
            return Craft.RC_LackIngredients;
        }

        int num = ingredients.getCount();
        for (int i = 0; i < num; i++) {
            Item item = (Item) ingredients.getItem(i);
            sum.argValue = sum.argValue + item.getWeight();

            if (mat.argValue == MaterialKind.mk_None) {
                mat.argValue = item.getMaterial();
            } else {
                if (mat.argValue != item.getMaterial()) {
                    return Craft.RC_DifferentMetals;
                }
            }
        }

        return Craft.RC_Ok;
    }

    public final int forgeItem(EntityList ingredients, int itemID)
    {
        float sum = 0F;
        MaterialKind i = MaterialKind.mk_None;
        RefObject<Float> refSum = new RefObject<>(sum);
        RefObject<MaterialKind> refMaterial = new RefObject<>(i);
        int result = this.checkForgeIngredients(ingredients, refSum, refMaterial);
        sum = refSum.argValue;
        i = refMaterial.argValue;

        if (result != Craft.RC_Ok) {
            if (result == Craft.RC_DifferentMetals) {
                // dummy
            }
        } else {
            int num = ingredients.getCount();
            for (int j = 0; j < num; j++) {
                Item item = (Item) ingredients.getItem(j);
                this.fPlayer.deleteItem(item);
            }

            ingredients.clear();
            if (itemID == GlobalVars.iid_Ingot) {
                this.addForgeItem(GlobalVars.iid_Ingot, sum, i);
            } else {
                ItemEntry iEntry = (ItemEntry) GlobalVars.nwrBase.getEntry(itemID);
                this.addForgeItem(itemID, iEntry.Weight, i);
                float rem = ((sum - iEntry.Weight));
                if (rem > 0f) {
                    this.addForgeItem(GlobalVars.iid_Ingot, rem, i);
                }
            }
        }
        return result;
    }

    public final String checkSmithyTools()
    {
        String result = "";
        ItemsList items = this.fPlayer.getItems();
        if (items.findByCLSID(GlobalVars.iid_Anvil) == null) {
            result += Locale.getStr(RS.rs_NoAnvil);
        }
        if (items.findByCLSID(GlobalVars.iid_Tongs) == null) {
            if (result.compareTo("") != 0) {
                result += ", ";
            }
            result += Locale.getStr(RS.rs_NoTongs);
        }
        if (items.findByCLSID(GlobalVars.iid_Wand_Fire) == null) {
            if (result.compareTo("") != 0) {
                result += ", ";
            }
            result += Locale.getStr(RS.rs_NoWand_Fire);
        }
        return result;
    }
}
