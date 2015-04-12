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
package nwr.core.types;

import java.util.HashMap;
import jzrlib.core.ProbabilityTable;
import nwr.core.RS;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum BuildingID
{
    bid_None(0, RS.rs_Reserved, SysCreature.sc_Viking, new ItemKinds(), 0, 0, 0, 0, 0),
    bid_House(1, RS.rs_House, SysCreature.sc_Viking, new ItemKinds(), 1, 1, 2, 4, 5),
    bid_MerchantShop(2, RS.rs_MerchantShop, SysCreature.sc_Merchant, new ItemKinds(ItemKind.ik_Armor, ItemKind.ik_Food, ItemKind.ik_Potion, ItemKind.ik_Ring, ItemKind.ik_Tool, ItemKind.ik_Wand, ItemKind.ik_BluntWeapon, ItemKind.ik_Scroll, ItemKind.ik_Amulet, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Clothing, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor, ItemKind.ik_MusicalTool, ItemKind.ik_Projectile, ItemKind.ik_Misc), 1, 1, 2, 4, 6),
    bid_Smithy(3, RS.rs_Smithy, SysCreature.sc_Blacksmith, new ItemKinds(ItemKind.ik_Armor, ItemKind.ik_BluntWeapon, ItemKind.ik_ShortBlade, ItemKind.ik_LongBlade, ItemKind.ik_Shield, ItemKind.ik_Helmet, ItemKind.ik_Spear, ItemKind.ik_Axe, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_HeavyArmor, ItemKind.ik_MediumArmor, ItemKind.ik_LightArmor, ItemKind.ik_Projectile), 1, 1, 2, 4, 5),
    bid_AlchemistShop(4, RS.rs_AlchemistShop, SysCreature.sc_Alchemist, new ItemKinds(ItemKind.ik_Potion), 1, 1, 2, 4, 5),
    bid_ConjurerShop(5, RS.rs_ConjurerShop, SysCreature.sc_Conjurer, new ItemKinds(ItemKind.ik_Ring, ItemKind.ik_Wand, ItemKind.ik_Amulet), 1, 1, 2, 4, 5),
    bid_SageShop(6, RS.rs_SageShop, SysCreature.sc_Sage, new ItemKinds(ItemKind.ik_Scroll), 1, 1, 2, 4, 5),
    bid_WoodsmanShop(7, RS.rs_WoodsmanShop, SysCreature.sc_Woodsman, new ItemKinds(ItemKind.ik_Food, ItemKind.ik_ShortBlade, ItemKind.ik_Spear, ItemKind.ik_Bow, ItemKind.ik_CrossBow, ItemKind.ik_Projectile), 1, 1, 2, 4, 5);

    public static final int bid_First = 1;
    public static final int bid_Last = 7;

    public final int Value;
    public final int NameRS;
    public final SysCreature Owner;
    public final ItemKinds WaresKinds;
    public final byte minCount;
    public final byte maxCount;
    public final byte maxDoors;
    public final byte minSize;
    public final byte maxSize;
    public ProbabilityTable<Integer> Wares;

    private BuildingID(int value, int name, SysCreature owner, ItemKinds wares, int minCount, int maxCount, int maxDoors, int minSize, int maxSize)
    {
        this.Value = value;
        this.NameRS = name;
        this.Owner = owner;
        this.WaresKinds = wares;
        this.minCount = (byte) minCount;
        this.maxCount = (byte) maxCount;
        this.maxDoors = (byte) maxDoors;
        this.minSize = (byte) minSize;
        this.maxSize = (byte) maxSize;

        getMappings().put(value, this);
    }

    private static HashMap<Integer, BuildingID> mappings;

    private static HashMap<Integer, BuildingID> getMappings()
    {
        synchronized (BuildingID.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    public static BuildingID forValue(int value)
    {
        return getMappings().get(value);
    }
}
