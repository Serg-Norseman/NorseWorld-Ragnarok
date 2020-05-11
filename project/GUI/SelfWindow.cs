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
using BSLib;
using NWR.Game;
using NWR.Game.Types;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class SelfWindow : NWWindow
    {
        private readonly ListBox fDiagnosisCtl;
        private readonly ListBox fSkillsCtl;
        private readonly ListBox fAbilitiesCtl;

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);

            Player player = GlobalVars.nwrGame.Player;

            int x = 14; // 3
            int x2 = 222; // 4
            int x3 = 431; // 5
            CreatureState state = player.State;

            string ps = "";
            if (state != CreatureState.Alive) {
                if (state == CreatureState.Undead) {
                    ps = BaseLocale.GetStr(RS.rs_Undead);
                }
            } else {
                ps = BaseLocale.GetStr(RS.rs_Alive);
            }

            int mpr;
            if (player.MPMax == 0) {
                mpr = 0;
            } else {
                mpr = ((int)Math.Round(((float)player.MPCur / player.MPMax) * 100.0f));
            }

            screen.SetTextColor(Colors.Gold, true);
            screen.DrawText(x, 8, player.Name, 0);
            screen.DrawText(x, 24, BaseLocale.GetStr(RS.rs_Race) + ": " + player.Race, 0);
            screen.DrawText(x, 40, BaseLocale.GetStr(RS.rs_Sex) + ": " + BaseLocale.GetStr(StaticData.dbSex[(int)player.Sex].NameRS), 0);
            screen.DrawText(x, 56, BaseLocale.GetStr(RS.rs_Level) + ": " + Convert.ToString(player.Level), 0);
            screen.DrawText(x, 72, BaseLocale.GetStr(RS.rs_Experience) + ": " + Convert.ToString(player.Experience), 0);
            screen.DrawText(x, 88, BaseLocale.GetStr(RS.rs_Morality) + ": " + player.GetMoralityName(), 0);
            screen.DrawText(x, 104, BaseLocale.GetStr(RS.rs_Perception) + ": " + Convert.ToString((int)player.Perception), 0);
            screen.DrawText(x2, 8, ps, 0);
            screen.DrawText(x2, 24, BaseLocale.GetStr(RS.rs_Constitution) + ": " + Convert.ToString(player.Constitution), 0);
            screen.DrawText(x2, 40, BaseLocale.GetStr(RS.rs_Strength) + ": " + Convert.ToString(player.Strength), 0);
            screen.DrawText(x2, 56, BaseLocale.GetStr(RS.rs_Speed) + ": " + Convert.ToString(player.Speed), 0);
            screen.DrawText(x2, 72, BaseLocale.GetStr(RS.rs_Luck) + ": " + Convert.ToString(player.Luck), 0);
            screen.DrawText(x2, 88, BaseLocale.GetStr(RS.rs_Armor) + ": " + Convert.ToString(player.ArmorClass), 0);
            screen.DrawText(x3, 24, BaseLocale.GetStr(RS.rs_HP) + ": " + Convert.ToString((int)(Math.Round(((float)player.HPCur / (float)player.HPMax_Renamed) * 100.0f))) + " %", 0);
            screen.DrawText(x3, 40, BaseLocale.GetStr(RS.rs_MP) + ": " + Convert.ToString(mpr) + " %", 0);
            screen.DrawText(x3, 56, BaseLocale.GetStr(RS.rs_Satiety) + ": " + Convert.ToString((int)(Math.Round(((float)player.Satiety / (float)Player.SatietyMax) * 100.0f))) + " %", 0);
            screen.DrawText(x3, 72, BaseLocale.GetStr(RS.rs_Damage) + ": " + Convert.ToString(player.DBMin) + "-" + Convert.ToString(player.DBMax), 0);
            screen.DrawText(x3, 88, BaseLocale.GetStr(RS.rs_Dexterity) + ": " + Convert.ToString((int)player.Dexterity), 0);

            screen.DrawText(14, 130, BaseLocale.GetStr(RS.rs_Diagnosis), 0);
            screen.DrawText(222, 130, BaseLocale.GetStr(RS.rs_Skills), 0);
            screen.DrawText(431, 130, BaseLocale.GetStr(RS.rs_Abilities), 0);
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();

            fDiagnosisCtl.Items.Clear();
            fSkillsCtl.Items.Clear();
            fAbilitiesCtl.Items.Clear();

            for (var ab = AbilityID.Ab_First; ab <= AbilityID.Ab_Last; ab++) {
                AbilityRec abid = StaticData.dbAbilities[(int)ab];
                int val = GlobalVars.nwrGame.Player.GetAbility(ab);
                if (val > 0) {
                    fAbilitiesCtl.Items.Add(BaseLocale.GetStr(abid.Name) + " (" + Convert.ToString(val) + ")", null);
                }
            }

            for (var sk = SkillID.Sk_First; sk <= SkillID.Sk_Last; sk++) {
                int val = GlobalVars.nwrGame.Player.GetSkill(sk);
                if (val > 0) {
                    fSkillsCtl.Items.Add(BaseLocale.GetStr(StaticData.dbSkills[(int)sk].Name) + " (" + Convert.ToString(val) + ")", null);
                }
            }

            int num = GlobalVars.nwrGame.Player.Effects.Count;
            for (int i = 0; i < num; i++) {
                fDiagnosisCtl.Items.Add(GlobalVars.nwrGame.Player.Effects.GetItem(i).Name, null);
            }

            StringList body_diag = new StringList();
            try {
                GlobalVars.nwrGame.Player.DiagnosisBody(body_diag);

                int num2 = body_diag.Count;
                for (int i = 0; i < num2; i++) {
                    fDiagnosisCtl.Items.Add(body_diag[i], null);
                }
            } finally {
                body_diag.Dispose();
            }
        }

        public SelfWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 640;
            Height = 480;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);
            //super.Shifted = true;

            fDiagnosisCtl = new ListBox(this);
            fDiagnosisCtl.Bounds = ExtRect.Create(10, 130 + StaticData.RsFontHeight, 212, 469); // 3
            fDiagnosisCtl.Visible = true;

            fSkillsCtl = new ListBox(this);
            fSkillsCtl.Bounds = ExtRect.Create(218, 130 + StaticData.RsFontHeight, 421, 469); // 4
            fSkillsCtl.Visible = true;

            fAbilitiesCtl = new ListBox(this);
            fAbilitiesCtl.Bounds = ExtRect.Create(427, 130 + StaticData.RsFontHeight, 629, 469); // 5
            fAbilitiesCtl.Visible = true;

            fDiagnosisCtl.ControlStyle.Exclude(ControlStyles.сsOpaque);
            fSkillsCtl.ControlStyle.Exclude(ControlStyles.сsOpaque);
            fAbilitiesCtl.ControlStyle.Exclude(ControlStyles.сsOpaque);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fDiagnosisCtl.Dispose();
                fSkillsCtl.Dispose();
                fAbilitiesCtl.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
