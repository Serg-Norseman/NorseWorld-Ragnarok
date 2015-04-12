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

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum CreatureAction
{
    caNone(0, 1f, new ActionFlags()),
    caWait(1, 1f, new ActionFlags()),
    caMove(2, 1f, new ActionFlags()),
    caAttackMelee(3, 1f, new ActionFlags(ActionFlags.afWithItem, ActionFlags.afCheckAbility)),
    caAttackShoot(4, 1f, new ActionFlags(ActionFlags.afWithItem, ActionFlags.afCheckAbility)),
    caAttackThrow(5, 1f, new ActionFlags(ActionFlags.afWithItem, ActionFlags.afCheckAbility)),
    caAttackParry(6, 1f, new ActionFlags(ActionFlags.afWithItem, ActionFlags.afCheckAbility)),
    caItemPickup(7, 1f, new ActionFlags(ActionFlags.afWithItem)),
    caItemDrop(8, 1f, new ActionFlags(ActionFlags.afWithItem)),
    caItemWear(9, 1f, new ActionFlags(ActionFlags.afWithItem)),
    caItemRemove(10, 1f, new ActionFlags(ActionFlags.afWithItem)),
    caPickupAll(11, 1f, new ActionFlags()),
    caItemUse(12, 1f, new ActionFlags(ActionFlags.afWithItem)),
    caSpellUse(13, 1f, new ActionFlags()),
    caSkillUse(14, 1f, new ActionFlags());

    public final int Value;
    public final float Factor;
    public final ActionFlags Flags;
    
    private CreatureAction(int value, float factor, ActionFlags flags)
    {
        this.Value = value;
        this.Factor = factor;
        this.Flags = flags;
    }

    public static CreatureAction forValue(int value)
    {
        return values()[value];
    }
}
