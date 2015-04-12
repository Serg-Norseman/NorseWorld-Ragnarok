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
import nwr.core.RS;
import nwr.engine.BaseScreen;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum ItemKind
{
    ik_Armor(0, RS.rs_IK_Armor, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_None),
    ik_DeadBody(1, RS.rs_IK_DeadBody, 10, BaseScreen.clMaroon, false, 0, "", AbilityID.Ab_None),
    ik_Food(2, RS.rs_IK_Food, 8, BaseScreen.clYellow, false, 0, "", AbilityID.Ab_None),
    ik_Potion(3, RS.rs_IK_Potion, 4, BaseScreen.clYellow, true, 13, "Potion", AbilityID.Ab_None),
    ik_Ring(4, RS.rs_IK_Ring, 5, BaseScreen.clGold, true, 26, "Ring", AbilityID.Ab_None),
    ik_Tool(5, RS.rs_IK_Tool, 9, BaseScreen.clBlue, false, 0, "", AbilityID.Ab_None),
    ik_Wand(6, RS.rs_IK_Wand, 7, BaseScreen.clLime, true, 21, "Wand", AbilityID.Ab_None),
    ik_BluntWeapon(7, RS.rs_IK_BluntWeapon, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_BluntWeapon),
    ik_Scroll(8, RS.rs_IK_Scroll, 6, BaseScreen.clAqua, false, 0, "", AbilityID.Ab_None),
    ik_Coin(9, RS.rs_IK_Coin, 11, BaseScreen.clGold, false, 0, "", AbilityID.Ab_None),
    ik_Amulet(10, RS.rs_IK_Amulet, 1, BaseScreen.clGold, true, 25, "Amulet", AbilityID.Ab_None),
    ik_ShortBlade(11, RS.rs_IK_ShortBlade, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_ShortBlades),
    ik_LongBlade(12, RS.rs_IK_LongBlade, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_LongBlade),
    ik_Shield(13, RS.rs_IK_Shield, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_Parry),
    ik_Helmet(14, RS.rs_IK_Helmet, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_None),
    ik_Clothing(15, RS.rs_IK_Clothing, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_None),
    ik_Spear(16, RS.rs_IK_Spear, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_Spear),
    ik_Axe(17, RS.rs_IK_Axe, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_Axe),
    ik_Bow(18, RS.rs_IK_Bow, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_LongBow),
    ik_CrossBow(19, RS.rs_IK_CrossBow, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_CrossBow),
    ik_HeavyArmor(20, RS.rs_IK_HeavyArmor, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_HeavyArmor),
    ik_MediumArmor(21, RS.rs_IK_MediumArmor, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_MediumArmor),
    ik_LightArmor(22, RS.rs_IK_LightArmor, 2, BaseScreen.clTeal, false, 0, "", AbilityID.Ab_LightArmor),
    ik_MusicalTool(23, RS.rs_IK_MusicalTool, 9, BaseScreen.clBlue, false, 0, "", AbilityID.Ab_MusicalAcuity),
    ik_Projectile(24, RS.rs_IK_Projectile, 3, BaseScreen.clGreen, false, 0, "", AbilityID.Ab_None),
    ik_Misc(25, RS.rs_IK_Misc, 9, BaseScreen.clBlue, false, 0, "", AbilityID.Ab_None);

    public static final int ik_First = 0;
    public static final int ik_Last = 25;

    public final int Value;
    public final int NameRS;
    public final byte Order;
    public final int Color;
    public final boolean RndImage;
    public final byte Images;
    public final String ImageSign;
    public final AbilityID Ability;

    public int ImageIndex;
    public int ImagesLoaded;

    private ItemKind(int value, int name, int order, int color, boolean rndImage, int images, String imageSign, AbilityID ability)
    {
        this.Value = value;
        this.NameRS = name;
        this.Order = (byte) order;
        this.Color = color;
        this.RndImage = rndImage;
        this.Images = (byte) images;
        this.ImageSign = imageSign;
        this.Ability = ability;

        this.ImageIndex = -1;
        this.ImagesLoaded = 0;

        getMappings().put(value, this);
    }

    private static HashMap<Integer, ItemKind> mappings;

    private static HashMap<Integer, ItemKind> getMappings()
    {
        synchronized (ItemKind.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    public static ItemKind forValue(int value)
    {
        return getMappings().get(value);
    }
}
