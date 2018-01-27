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
using NWR.Creatures.Brain;
using NWR.Game;
using NWR.GUI.Controls;
using NWR.Universe;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class RecruitWindow : DialogWindow
    {
        private readonly ListBox fMercenariesList;
        private readonly NWButton fRecruitBtn;

        public RecruitWindow(BaseControl owner)
            : base(owner)
        {
            fMercenariesList = new ListBox(this);
            fMercenariesList.Mode = ListBox.MODE_REPORT;
            fMercenariesList.Bounds = ExtRect.Create(10, 10, 589, 398);
            fMercenariesList.ColumnTitles.Add("name", 400);
            fMercenariesList.ColumnTitles.Add("cost", 150);

            fRecruitBtn = new NWButton(this);
            fRecruitBtn.OnClick = OnBtnRecruit;
            fRecruitBtn.OnLangChange = GlobalVars.nwrWin.LangChange;
            fRecruitBtn.LangResID = 49;
            fRecruitBtn.Width = 90;
            fRecruitBtn.Height = 30;
            fRecruitBtn.Left = Width - 210;
            fRecruitBtn.Top = Height - 30 - 20;
            fRecruitBtn.ImageFile = "itf/DlgBtn.tga";
        }

        private bool CanRecruit()
        {
            Player player = GlobalVars.nwrGame.Player;
            int membCount = ((LeaderBrain)player.Brain).MembersCount - 1;

            return (membCount < LeaderBrain.PartyMax);
        }

        private void OnBtnRecruit(object sender)
        {
            int idx = fMercenariesList.SelIndex;
            if (idx >= 0 && idx < fMercenariesList.Items.Count && CanRecruit()) {
                NWCreature mercenary = (NWCreature)fMercenariesList.Items.GetItem(idx).Data;
                GlobalVars.nwrGame.Player.RecruitMercenary(fCollocutor, mercenary, true);
                UpdateView();
            }
        }

        private void UpdateView()
        {
            fMercenariesList.Items.BeginUpdate();
            fMercenariesList.Items.Clear();
            if (fCollocutor.CLSID_Renamed == GlobalVars.cid_Jarl) {
                NWField fld = (NWField)fCollocutor.CurrentMap;

                int num = fld.Creatures.Count;
                for (int i = 0; i < num; i++) {
                    NWCreature j = fld.Creatures.GetItem(i);
                    if (j.CLSID_Renamed == GlobalVars.cid_Guardsman && !j.Mercenary) {
                        AddCandidate(j);
                    }
                }
            } else {
                if (fCollocutor.CLSID_Renamed == GlobalVars.cid_Merchant) {
                    AddCandidate(fCollocutor);
                }
            }
            fMercenariesList.Items.EndUpdate();
            fRecruitBtn.Enabled = (fMercenariesList.Items.Count > 0);
        }

        private void AddCandidate(NWCreature aCreature)
        {
            int hPrice = (int)aCreature.HirePrice;
            LBItem item = fMercenariesList.Items.Add(aCreature.Name, aCreature);
            item.SubItems.Add(Convert.ToString(hPrice), null);
            if (GlobalVars.nwrGame.Player.Money >= hPrice && CanRecruit()) {
                item.Color = Colors.Gold;
            } else {
                item.Color = Colors.Red;
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            UpdateView();
        }
    }
}
