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
using NWR.Game;
using NWR.Game.Types;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class TeachWindow : DialogWindow
    {
        private sealed class TeachItem
        {
            public int Index;
            public int CurLev;
            public int Price;

            public TeachItem(int index, int curLev, int price)
            {
                Index = index;
                CurLev = curLev;
                Price = price;
            }
        }

        private readonly ListBox fDisciplinesList;

        public TeachWindow(BaseControl owner)
            : base(owner)
        {
            fDisciplinesList = new ListBox(this);
            fDisciplinesList.Mode = ListBox.MODE_REPORT;
            fDisciplinesList.Columns = 2;

            fDisciplinesList.Bounds = ExtRect.Create(10, 10, 589, 398);

            fDisciplinesList.ItemHeight = 34;
            fDisciplinesList.IconHeight = 30;
            fDisciplinesList.IconWidth = 32;
            fDisciplinesList.Options.Include(LBOptions.lboIcons);
            fDisciplinesList.ColumnTitles.Add("", 460);
            fDisciplinesList.ColumnTitles.Add("", 72);
            fDisciplinesList.Visible = true;
            fDisciplinesList.Columns = 1;
            fDisciplinesList.OnItemSelect = OnDisciplineSelect;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fDisciplinesList.Dispose();
            }
            base.Dispose(disposing);
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            fDisciplinesList.ImagesList = GlobalVars.nwrWin.Resources;
            fDisciplinesList.IconHeight = 30;
            fDisciplinesList.IconWidth = 32;
            UpdateView();
        }

        private void OnDisciplineSelect(object sender, MouseButton button, LBItem item)
        {
            if (button == MouseButton.mbRight) {
                TeachItem ti = (TeachItem)item.Data;
                if (ti.Price <= GlobalVars.nwrGame.Player.Money) {
                    GlobalVars.nwrGame.Player.TeachDiscipline(fCollocutor, ti.Index, ti.CurLev);
                    UpdateView();
                }
            }
        }

        private void UpdateView()
        {
            fDisciplinesList.Items.BeginUpdate();
            fDisciplinesList.Items.Clear();
            NWCreature clt = fCollocutor;
            Player p = GlobalVars.nwrGame.Player;

            for (int i = 0; i < StaticData.dbTeachable.Length; i++) {
                int id = StaticData.dbTeachable[i].Id;
                bool res = false;
                TeachableKind kind = StaticData.dbTeachable[i].Kind;

                string s = "";
                int imageIndex = -1;
                int curLev = 0;
                switch (kind) {
                    case TeachableKind.Ability:
                        AbilityRec abRec = StaticData.dbAbilities[id];
                        AbilityID ab = (AbilityID)id;
                        if (clt.GetAbility(ab) > 0) {
                            s = BaseLocale.GetStr(abRec.Name);
                            imageIndex = -1;
                            curLev = p.GetAbility(ab);
                            res = (curLev < clt.GetAbility(ab));
                        }
                        break;

                    case TeachableKind.Skill:
                        SkillRec skRec = StaticData.dbSkills[id];
                        SkillID sk = (SkillID)id;
                        if (clt.GetSkill(sk) >= 0) {
                            s = BaseLocale.GetStr(skRec.Name);
                            imageIndex = skRec.ImageIndex;
                            curLev = p.GetSkill(sk);
                            res = (curLev < clt.GetSkill(sk));
                        }
                        break;
                }

                int price = (int)GlobalVars.nwrGame.GetTeachablePrice(i, curLev);

                if (res) {
                    string st = " ( " + Convert.ToString(curLev) + " -> " + Convert.ToString(curLev + 1) + " )";
                    LBItem listItem = fDisciplinesList.Items.Add(s + st, new TeachItem(i, curLev, price));
                    if (price > p.Money) {
                        listItem.Color = Colors.Red;
                    } else {
                        listItem.Color = Colors.Gold;
                    }
                    listItem.ImageIndex = imageIndex;
                    listItem.SubItems.Add(Convert.ToString(price), null);
                }
            }

            fDisciplinesList.Items.EndUpdate();
        }
    }

}