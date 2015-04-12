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
package nwr.creatures;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class HumanBody extends CustomBody
{
    public HumanBody(Object owner)
    {
        super(owner);
        this.initBody();
    }

    private void initBody()
    {
        this.clear();
        this.addPart(BodypartType.bp_Body, new BodypartAbilities(BodypartAbilities.bpa_HasMetabolism), 1);
        this.addPart(BodypartType.bp_Head, new BodypartAbilities(), 1);
        this.addPart(BodypartType.bp_Neck, new BodypartAbilities(), 1);
        this.addPart(BodypartType.bp_Torso, new BodypartAbilities(BodypartAbilities.bpa_HasLungs), 1);
        this.addPart(BodypartType.bp_Waist, new BodypartAbilities(), 1);
        this.addPart(BodypartType.bp_LHand, new BodypartAbilities(), 1);
        this.addPart(BodypartType.bp_RHand, new BodypartAbilities(), 1);
        this.addPart(BodypartType.bp_Arms, new BodypartAbilities(), 1);
        this.addPart(BodypartType.bp_Hands, new BodypartAbilities(BodypartAbilities.bpa_Wield, BodypartAbilities.bpa_Pickup, BodypartAbilities.bpa_Attack), 1);
        this.addPart(BodypartType.bp_Legs, new BodypartAbilities(), 1);
        this.addPart(BodypartType.bp_Feets, new BodypartAbilities(), 1);
        this.addPart(BodypartType.bp_Finger, new BodypartAbilities(), 10);
        this.addPart(BodypartType.bp_Back, new BodypartAbilities(), 1);
        this.addPart(BodypartType.bp_Eye, new BodypartAbilities(BodypartAbilities.bpa_Sight), 2);
    }
}
