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
import nwr.effects.EffectID;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum SkillID
{
    Sk_None(0, RS.rs_Reserved, SkillKind.ssPlain, "", EffectID.eid_None),
    Sk_Alchemy(1, RS.rs_Alchemy, SkillKind.ssPlain, "Alchemy", EffectID.eid_None),
    Sk_ArrowMake(2, RS.rs_ArrowMake, SkillKind.ssPlain, "ArrowMake", EffectID.eid_ArrowMake),
    Sk_Cartography(3, RS.rs_Cartography, SkillKind.ssPlain, "Cartography", EffectID.eid_Cartography),
    Sk_Diagnosis(4, RS.rs_Diagnosis, SkillKind.ssPlain, "Diagnosis", EffectID.eid_Diagnosis),
    Sk_Embalming(5, RS.rs_Embalming, SkillKind.ssPlain, "Embalming", EffectID.eid_Embalming),
    Sk_Fennling(6, RS.rs_Fennling, SkillKind.ssPlain, "Fennling", EffectID.eid_Fennling),
    Sk_GolemCreation(7, RS.rs_GolemCreation, SkillKind.ssPlain, "GolemCreation", EffectID.eid_GolemCreation),
    Sk_Husbandry(8, RS.rs_Husbandry, SkillKind.ssPlain, "Husbandry", EffectID.eid_Husbandry),
    Sk_Ironworking(9, RS.rs_Ironworking, SkillKind.ssPlain, "Ironworking", EffectID.eid_None),
    Sk_Precognition(10, RS.rs_Precognition, SkillKind.ssPlain, "Precognition", EffectID.eid_Precognition),
    Sk_Relocation(11, RS.rs_Relocation, SkillKind.ssPlain, "Relocation", EffectID.eid_Relocation),
    Sk_SlaveUse(12, RS.rs_SlaveUse, SkillKind.ssPlain, "SlaveUse", EffectID.eid_SlaveUse),
    Sk_Taming(13, RS.rs_Taming, SkillKind.ssPlain, "Taming", EffectID.eid_Taming),
    Sk_Ventriloquism(14, RS.rs_Ventriloquism, SkillKind.ssPlain, "Ventriloquism", EffectID.eid_Ventriloquism),
    Sk_Writing(15, RS.rs_Writing, SkillKind.ssPlain, "Writing", EffectID.eid_Writing),
    Sk_Animation(16, RS.rs_Animation, SkillKind.ssPlain, "Animation", EffectID.eid_Animation),
    Sk_DimensionTravel(17, RS.rs_DimensionTravel, SkillKind.ssPlain, "DimensionTravel", EffectID.eid_SwitchDimension),
    Sk_FireVision(18, RS.rs_FireVision, SkillKind.ssPlain, "FireVision", EffectID.eid_FireVision),
    Sk_HeatRadiation(19, RS.rs_HeatRadiation, SkillKind.ssPlain, "HeatRadiation", EffectID.eid_Heat),
    Sk_MindControl(20, RS.rs_MindControl, SkillKind.ssPlain, "MindControl", EffectID.eid_MindControl),
    Sk_PsiBlast(21, RS.rs_PsiBlast, SkillKind.ssPlain, "PsiBlast", EffectID.eid_PsiBlast),
    Sk_Terraforming(22, RS.rs_Terraforming, SkillKind.ssPlain, "Terraforming", EffectID.eid_Geology),
    Sk_Spellcasting(23, RS.rs_Spellcasting, SkillKind.ssMeta, "Spellcasting", EffectID.eid_None),
    Sk_InnatePower(24, RS.rs_InnatePower, SkillKind.ssMeta, "InnatePower", EffectID.eid_None),
    Sk_Basilisk_Poison(25, RS.rs_Basilisk_Poison, SkillKind.ssInnatePower, "", EffectID.eid_Basilisk_Poison),
    Sk_Borgonvile_Cloud(26, RS.rs_Borgonvile_Cloud, SkillKind.ssInnatePower, "", EffectID.eid_Borgonvile_Cloud),
    Sk_Breleor_Tendril(27, RS.rs_Breleor_Tendril, SkillKind.ssInnatePower, "", EffectID.eid_Breleor_Tendril),
    Sk_Ellegiant_Throw(28, RS.rs_Ellegiant_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Ellegiant_Throw),
    Sk_Ellegiant_Crush(29, RS.rs_Ellegiant_Crush, SkillKind.ssInnatePower, "", EffectID.eid_Ellegiant_Crush),
    Sk_Firedragon_Breath(30, RS.rs_Firedragon_Breath, SkillKind.ssInnatePower, "", EffectID.eid_Firedragon_Breath),
    Sk_Firegiant_Touch(31, RS.rs_Firegiant_Touch, SkillKind.ssInnatePower, "", EffectID.eid_Firegiant_Touch),
    Sk_Fyleisch_Cloud(32, RS.rs_Fyleisch_Cloud, SkillKind.ssInnatePower, "", EffectID.eid_Fyleisch_Cloud),
    Sk_Gasball_Explosion(33, RS.rs_Gasball_Explosion, SkillKind.ssInnatePower, "", EffectID.eid_Gasball_Explosion),
    Sk_Giantsquid_Crush(34, RS.rs_Giantsquid_Crush, SkillKind.ssInnatePower, "", EffectID.eid_Giantsquid_Crush),
    Sk_Glard_Poison(35, RS.rs_Glard_Poison, SkillKind.ssInnatePower, "", EffectID.eid_Glard_Poison),
    Sk_Hatchetfish_Teeth(36, RS.rs_Hatchetfish_Teeth, SkillKind.ssInnatePower, "", EffectID.eid_Hatchetfish_Teeth),
    Sk_Heldragon_Cloud(37, RS.rs_Heldragon_Cloud, SkillKind.ssInnatePower, "", EffectID.eid_Heldragon_Cloud),
    Sk_Hillgiant_Crush(38, RS.rs_Hillgiant_Crush, SkillKind.ssInnatePower, "", EffectID.eid_Hillgiant_Crush),
    Sk_Icedragon_Breath(39, RS.rs_Icedragon_Breath, SkillKind.ssInnatePower, "", EffectID.eid_Icedragon_Breath),
    Sk_Icesphere_Blast(40, RS.rs_Icesphere_Blast, SkillKind.ssInnatePower, "", EffectID.eid_Icesphere_Blast),
    Sk_Jagredin_Burning(41, RS.rs_Jagredin_Burning, SkillKind.ssInnatePower, "", EffectID.eid_Jagredin_Burning),
    Sk_Knellbird_Gaze(42, RS.rs_Knellbird_Gaze, SkillKind.ssInnatePower, "", EffectID.eid_Knellbird_Gaze),
    Sk_Kobold_Throw(43, RS.rs_Kobold_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Kobold_Throw),
    Sk_Lowerdwarf_Throw(44, RS.rs_Lowerdwarf_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Lowerdwarf_Throw),
    Sk_Moleman_Debris(45, RS.rs_Moleman_Debris, SkillKind.ssInnatePower, "", EffectID.eid_Moleman_Debris),
    Sk_Phantomasp_Poison(46, RS.rs_Phantomasp_Poison, SkillKind.ssInnatePower, "", EffectID.eid_Phantomasp_Poison),
    Sk_Pyrtaath_Throttle(47, RS.rs_Pyrtaath_Throttle, SkillKind.ssInnatePower, "", EffectID.eid_Pyrtaath_Throttle),
    Sk_Ramapith_FireTouch(48, RS.rs_Ramapith_FireTouch, SkillKind.ssInnatePower, "", EffectID.eid_Ramapith_FireTouch),
    Sk_Sandiff_Acid(49, RS.rs_Sandiff_Acid, SkillKind.ssInnatePower, "", EffectID.eid_Sandiff_Acid),
    Sk_Scyld_Breath(50, RS.rs_Scyld_Breath, SkillKind.ssInnatePower, "", EffectID.eid_Scyld_Breath),
    Sk_Scyld_Ray(51, RS.rs_Scyld_Ray, SkillKind.ssInnatePower, "", EffectID.eid_Scyld_Ray),
    Sk_Scyld_ShockWave(52, RS.rs_Scyld_ShockWave, SkillKind.ssInnatePower, "", EffectID.eid_Scyld_ShockWave),
    Sk_Sentinel_Gaze(53, RS.rs_Sentinel_Gaze, SkillKind.ssInnatePower, "", EffectID.eid_Sentinel_Gaze),
    Sk_Serpent_Poison(54, RS.rs_Serpent_Poison, SkillKind.ssInnatePower, "", EffectID.eid_Serpent_Poison),
    Sk_Shadow_Touch(55, RS.rs_Shadow_Touch, SkillKind.ssInnatePower, "", EffectID.eid_Shadow_Touch),
    Sk_Slinn_Gout(56, RS.rs_Slinn_Gout, SkillKind.ssInnatePower, "", EffectID.eid_Slinn_Gout),
    Sk_Spirit_Touch(57, RS.rs_Spirit_Touch, SkillKind.ssInnatePower, "", EffectID.eid_Spirit_Touch),
    Sk_Stunworm_Stun(58, RS.rs_Stunworm_Stun, SkillKind.ssInnatePower, "", EffectID.eid_Stunworm_Stun),
    Sk_Terrain_Burning(59, RS.rs_Terrain_Burning, SkillKind.ssInnatePower, "", EffectID.eid_Terrain_Burning),
    Sk_Warrior_Throw(60, RS.rs_Warrior_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Warrior_Throw),
    Sk_Watcher_Gaze(61, RS.rs_Watcher_Gaze, SkillKind.ssInnatePower, "", EffectID.eid_Watcher_Gaze),
    Sk_WeirdFume_Acid(62, RS.rs_Weirdfume_Acid, SkillKind.ssInnatePower, "", EffectID.eid_WeirdFume_Acid),
    Sk_Womera_Throw(63, RS.rs_Womera_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Womera_Throw),
    Sk_Wooddwarf_Throw(64, RS.rs_Wooddwarf_Throw, SkillKind.ssInnatePower, "", EffectID.eid_Wooddwarf_Throw),
    Sk_Wyvern_Breath(65, RS.rs_Wyvern_Breath, SkillKind.ssInnatePower, "", EffectID.eid_Wyvern_Breath),
    Sk_Zardon_PsiBlast(66, RS.rs_Zardon_PsiBlast, SkillKind.ssInnatePower, "", EffectID.eid_Zardon_PsiBlast),
    Sk_Dig(67, RS.rs_Dig, SkillKind.ssPlain, "Dig", EffectID.eid_Dig),
    Sk_Ull_Gaze(68, RS.rs_Ull_Gaze, SkillKind.ssInnatePower, "", EffectID.eid_Ull_Gaze),
    Sk_Prayer(69, RS.rs_Prayer, SkillKind.ssPlain, "Prayer", EffectID.eid_Prayer),
    Sk_Sacrifice(70, RS.rs_Sacrifice, SkillKind.ssPlain, "Sacrifice", EffectID.eid_Sacrifice),
    Sk_Divination(71, RS.rs_RunicDivination, SkillKind.ssPlain, "Divination", EffectID.eid_RunicDivination);

    public static final int Sk_First = 1;
    public static final int Sk_Last = 71;

    private final int intValue;
    public final int NameRS;
    public final SkillKind Kinds;
    public final String gfx;
    public final EffectID Effect;
    public int ImageIndex;

    private static HashMap<Integer, SkillID> mappings;

    private static HashMap<Integer, SkillID> getMappings()
    {
        synchronized (SkillID.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private SkillID(int value, int nameRS, SkillKind kind, String gfx, EffectID effect)
    {
        this.intValue = value;
        this.NameRS = nameRS;
        this.Kinds = kind;
        this.gfx = gfx;
        this.Effect = effect;
        this.ImageIndex = -1;

        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static SkillID forValue(int value)
    {
        return getMappings().get(value);
    }
}
