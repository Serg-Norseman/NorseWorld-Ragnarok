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

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum AbilityID
{
    Ab_None(0, RS.rs_Reserved),
    Ab_Swimming(1, RS.rs_Swimming),
    Ab_Identification(2, RS.rs_Identification),
    Ab_Telepathy(3, RS.rs_Telepathy),
    Ab_SixthSense(4, RS.rs_SixthSense),
    Ab_MusicalAcuity(5, RS.rs_MusicalAcuity),
    Ab_ShortBlades(6, RS.rs_ShortBlades),
    Ab_LongBow(7, RS.rs_LongBow),
    Ab_CrossBow(8, RS.rs_CrossBow),
    Ab_Levitation(9, RS.rs_Levitation),
    Ab_Marksman(10, RS.rs_Marksman),
    Ab_Spear(11, RS.rs_Spear),
    Ab_RayReflect(12, RS.rs_RayReflect),
    Ab_RayAbsorb(13, RS.rs_RayAbsorb),
    Resist_Cold(14, RS.rs_Resist_Cold),
    Resist_Heat(15, RS.rs_Resist_Heat),
    Resist_Acid(16, RS.rs_Resist_Acid),
    Resist_Poison(17, RS.rs_Resist_Poison),
    Resist_Ray(18, RS.rs_Resist_Ray),
    Resist_Teleport(19, RS.rs_Resist_Teleport),
    Resist_DeathRay(20, RS.rs_Resist_DeathRay),
    Resist_Petrification(21, RS.rs_Resist_Petrification),
    Resist_Psionic(22, RS.rs_Resist_Psionic),
    Resist_DisplacementRay(23, RS.rs_Resist_DisplacementRay),
    Resist_HasteningRay(24, RS.rs_Resist_HasteningRay),
    Resist_SleepRay(25, RS.rs_Resist_SleepRay),
    Ab_Regeneration(26, RS.rs_Regeneration),
    Ab_ThirdSight(27, RS.rs_ThirdSight),
    Ab_Axe(28, RS.rs_Axe),
    Ab_Parry(29, RS.rs_Parry),
    Ab_BluntWeapon(30, RS.rs_BluntWeapon),
    Ab_HandToHand(31, RS.rs_HandToHand),
    Ab_HeavyArmor(32, RS.rs_HeavyArmor),
    Ab_MediumArmor(33, RS.rs_MediumArmor),
    Ab_LightArmor(34, RS.rs_LightArmor),
    Ab_LongBlade(35, RS.rs_LongBlade);

    public static final int Ab_First = 1;
    public static final int Ab_Last = 35;

    private final int intValue;
    public final int NameRS;

    private static HashMap<Integer, AbilityID> mappings;

    private static HashMap<Integer, AbilityID> getMappings()
    {
        synchronized (AbilityID.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private AbilityID(int value, int nameRS)
    {
        this.intValue = value;
        this.NameRS = nameRS;
        getMappings().put(value, this);
    }

    public final int getValue()
    {
        return intValue;
    }

    public static AbilityID forValue(int value)
    {
        return getMappings().get(value);
    }
}
