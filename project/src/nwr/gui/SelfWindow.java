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
package nwr.gui;

import jzrlib.utils.AuxUtils;
import jzrlib.core.Rect;
import jzrlib.core.StringList;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.AbilityID;
import nwr.core.types.CreatureState;
import nwr.core.types.SkillID;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.ControlStyles;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.ListBox;
import nwr.main.GlobalVars;
import nwr.player.Player;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class SelfWindow extends NWWindow
{
    private final ListBox fDiagnosisCtl;
    private final ListBox fSkillsCtl;
    private final ListBox fAbilitiesCtl;

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);

        Player player = GlobalVars.nwrGame.getPlayer();

        int x = 14; // 3
        int x2 = 222; // 4
        int x3 = 431; // 5
        CreatureState state = player.getState();

        String ps = "";
        if (state != CreatureState.csAlive) {
            if (state == CreatureState.csUndead) {
                ps = Locale.getStr(RS.rs_Undead);
            }
        } else {
            ps = Locale.getStr(RS.rs_Alive);
        }

        int mpr;
        if (player.MPMax == 0) {
            mpr = 0;
        } else {
            mpr = (Math.round(((float) player.MPCur / player.MPMax) * 100.0f));
        }

        screen.setTextColor(BaseScreen.clGold, true);
        screen.drawText(x, 8, player.getName(), 0);
        screen.drawText(x, 24, Locale.getStr(RS.rs_Race) + ": " + player.getRace(), 0);
        screen.drawText(x, 40, Locale.getStr(RS.rs_Sex) + ": " + Locale.getStr(StaticData.dbSex[player.Sex.Value].NameRS), 0);
        screen.drawText(x, 56, Locale.getStr(RS.rs_Level) + ": " + String.valueOf(player.Level), 0);
        screen.drawText(x, 72, Locale.getStr(RS.rs_Experience) + ": " + String.valueOf(player.getExperience()), 0);
        screen.drawText(x, 88, Locale.getStr(RS.rs_Morality) + ": " + player.getMoralityName(), 0);
        screen.drawText(x, 104, Locale.getStr(RS.rs_Perception) + ": " + String.valueOf((int) player.Perception), 0);
        screen.drawText(x2, 8, ps, 0);
        screen.drawText(x2, 24, Locale.getStr(RS.rs_Constitution) + ": " + String.valueOf(player.Constitution), 0);
        screen.drawText(x2, 40, Locale.getStr(RS.rs_Strength) + ": " + String.valueOf(player.Strength), 0);
        screen.drawText(x2, 56, Locale.getStr(RS.rs_Speed) + ": " + String.valueOf(player.getSpeed()), 0);
        screen.drawText(x2, 72, Locale.getStr(RS.rs_Luck) + ": " + String.valueOf(player.Luck), 0);
        screen.drawText(x2, 88, Locale.getStr(RS.rs_Armor) + ": " + String.valueOf(player.ArmorClass), 0);
        screen.drawText(x3, 24, Locale.getStr(RS.rs_HP) + ": " + String.valueOf((int) (Math.round(((float) player.HPCur / (float) player.HPMax) * 100.0f))) + " %", 0);
        screen.drawText(x3, 40, Locale.getStr(RS.rs_MP) + ": " + String.valueOf(mpr) + " %", 0);
        screen.drawText(x3, 56, Locale.getStr(RS.rs_Satiety) + ": " + String.valueOf((int) (Math.round(((float) player.getSatiety() / (float) Player.SatietyMax) * 100.0f))) + " %", 0);
        screen.drawText(x3, 72, Locale.getStr(RS.rs_Damage) + ": " + String.valueOf(player.DBMin) + "-" + String.valueOf(player.DBMax), 0);
        screen.drawText(x3, 88, Locale.getStr(RS.rs_Dexterity) + ": " + String.valueOf((int) player.Dexterity), 0);

        screen.drawText(14, 130, Locale.getStr(RS.rs_Diagnosis), 0);
        screen.drawText(222, 130, Locale.getStr(RS.rs_Skills), 0);
        screen.drawText(431, 130, Locale.getStr(RS.rs_Abilities), 0);
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        this.fDiagnosisCtl.getItems().clear();
        this.fSkillsCtl.getItems().clear();
        this.fAbilitiesCtl.getItems().clear();

        for (int ab = AbilityID.Ab_First; ab <= AbilityID.Ab_Last; ab++) {
            AbilityID abid = AbilityID.forValue(ab);
            int val = GlobalVars.nwrGame.getPlayer().getAbility(abid);
            if (val > 0) {
                this.fAbilitiesCtl.getItems().add(Locale.getStr(abid.NameRS) + " (" + String.valueOf(val) + ")", null);
            }
        }

        for (int sk = SkillID.Sk_First; sk <= SkillID.Sk_Last; sk++) {
            SkillID skid = SkillID.forValue(sk);
            int val = GlobalVars.nwrGame.getPlayer().getSkill(skid);
            if (val > 0) {
                this.fSkillsCtl.getItems().add(Locale.getStr(skid.NameRS) + " (" + String.valueOf(val) + ")", null);
            }
        }

        int num = GlobalVars.nwrGame.getPlayer().getEffects().getCount();
        for (int i = 0; i < num; i++) {
            this.fDiagnosisCtl.getItems().add(GlobalVars.nwrGame.getPlayer().getEffects().getItem(i).getName(), null);
        }

        StringList body_diag = new StringList();
        try {
            GlobalVars.nwrGame.getPlayer().diagnosisBody(body_diag);

            int num2 = body_diag.getCount();
            for (int i = 0; i < num2; i++) {
                this.fDiagnosisCtl.getItems().add(body_diag.get(i), null);
            }
        } finally {
            body_diag.dispose();
        }
    }

    public SelfWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(640);
        super.setHeight(480);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);
        //super.Shifted = true;

        this.fDiagnosisCtl = new ListBox(this);
        this.fDiagnosisCtl.setBounds(new Rect(10, 130 + StaticData.rsFontHeight, 212, 469)); // 3
        this.fDiagnosisCtl.setVisible(true);

        this.fSkillsCtl = new ListBox(this);
        this.fSkillsCtl.setBounds(new Rect(218, 130 + StaticData.rsFontHeight, 421, 469)); // 4
        this.fSkillsCtl.setVisible(true);

        this.fAbilitiesCtl = new ListBox(this);
        this.fAbilitiesCtl.setBounds(new Rect(427, 130 + StaticData.rsFontHeight, 629, 469)); // 5
        this.fAbilitiesCtl.setVisible(true);

        this.fDiagnosisCtl.ControlStyle.exclude(ControlStyles.csOpaque);
        this.fSkillsCtl.ControlStyle.exclude(ControlStyles.csOpaque);
        this.fAbilitiesCtl.ControlStyle.exclude(ControlStyles.csOpaque);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fDiagnosisCtl.dispose();
            this.fSkillsCtl.dispose();
            this.fAbilitiesCtl.dispose();
        }
        super.dispose(disposing);
    }
}
