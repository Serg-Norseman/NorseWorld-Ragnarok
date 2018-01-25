/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using BSLib;
using NWR.Core;
using NWR.Creatures;
using NWR.Creatures.Brain;
using NWR.Game;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class PartyWindow : NWWindow
    {
        private readonly ListBox fFormationList;
        private readonly ListBox fMercenariesList;
        private readonly ListBox fMercenaryPropsList;

        public PartyWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 600;
            Height = 460;
            WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview);
            Shifted = true;

            fFormationList = new ListBox(this);
            fFormationList.Mode = ListBox.MODE_LIST;
            fFormationList.Bounds = ExtRect.Create(10, 28, 290, 157);
            fFormationList.OnItemSelect = OnFormationSelect;
            fFormationList.Options = new LBOptions(LBOptions.lboChecks, LBOptions.lboRadioChecks);

            fMercenariesList = new ListBox(this);
            fMercenariesList.Mode = ListBox.MODE_LIST;
            fMercenariesList.Bounds = ExtRect.Create(10, 179, 290, 398); // 370
            fMercenariesList.OnItemSelect = OnMercenarySelect;

            fMercenaryPropsList = new ListBox(this);
            fMercenaryPropsList.Mode = ListBox.MODE_LIST;
            fMercenaryPropsList.Bounds = ExtRect.Create(309, 28, 589, 398);

            NWButton btn = new NWButton(this);
            btn.Width = 90;
            btn.Height = 30;
            btn.Left = Width - 90 - 20;
            btn.Top = Height - 30 - 20;
            btn.ImageFile = "itf/DlgBtn.tga";
            btn.OnClick = OnBtnClose;
            btn.OnLangChange = GlobalVars.nwrWin.LangChange;
            btn.LangResID = 8;

            btn = new NWButton(this);
            btn.Width = 90;
            btn.Height = 30;
            btn.Left = 20;
            btn.Top = Height - 30 - 20;
            btn.ImageFile = "itf/DlgBtn.tga";
            btn.OnClick = OnBtnExchange;
            btn.OnLangChange = GlobalVars.nwrWin.LangChange;
            btn.LangResID = (int)RS.rs_Exchange;

            btn = new NWButton(this);
            btn.Width = 90;
            btn.Height = 30;
            btn.Left = 20 + 90 + 20;
            btn.Top = Height - 30 - 20;
            btn.ImageFile = "itf/DlgBtn.tga";
            btn.OnClick = OnBtnTeach;
            btn.OnLangChange = GlobalVars.nwrWin.LangChange;
            btn.LangResID = (int)RS.rs_Teach;
        }

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        private void OnBtnTeach(object sender)
        {
            int idx = fMercenariesList.SelIndex;
            LeaderBrain party = (LeaderBrain)GlobalVars.nwrGame.Player.Brain;
            NWCreature member = (NWCreature)party.GetMember(idx + 1);

            if (member != null) {
                GlobalVars.nwrWin.ShowTeachWin(member);
            }
        }

        private void OnBtnExchange(object sender)
        {
            int idx = fMercenariesList.SelIndex;
            LeaderBrain party = (LeaderBrain)GlobalVars.nwrGame.Player.Brain;
            NWCreature member = (NWCreature)party.GetMember(idx + 1);

            if (member != null) {
                GlobalVars.nwrWin.ShowExchangeWin(member);
            }
        }

        private void OnFormationSelect(object sender, MouseButton button, LBItem item)
        {
            GlobalVars.nwrGame.Player.PartyFormation = (PartyFormation)(item.AbsoluteIndex);
        }

        private void OnMercenarySelect(object sender, MouseButton button, LBItem item)
        {
            fMercenaryPropsList.Items.Clear();

            if (item != null) {
                NWCreature merc = (NWCreature)item.Data;
                LBItemList items = fMercenaryPropsList.Items;

                StringList props = merc.Props;
                for (int i = 0; i < props.Count; i++) {
                    items.Add(props[i], null);
                }
            }
        }

        private void UpdateView()
        {
            fMercenariesList.Items.BeginUpdate();
            fMercenariesList.Items.Clear();

            LeaderBrain party = (LeaderBrain)GlobalVars.nwrGame.Player.Brain;

            int num = party.MembersCount;
            for (int i = 1; i < num; i++) {
                NWCreature j = (NWCreature)party.GetMember(i);
                fMercenariesList.Items.Add(j.Name, j);
            }

            fMercenariesList.Items.EndUpdate();
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);

            screen.SetTextColor(BaseScreen.clGold, true);

            ExtRect r1 = fFormationList.Bounds;
            int ml = r1.Left + (r1.Width - CtlCommon.SmFont.GetTextWidth(BaseLocale.GetStr(RS.rs_PartyFormation))) / 2;
            screen.DrawText(ml, 8, BaseLocale.GetStr(RS.rs_PartyFormation), 0);

            ExtRect r2 = fMercenariesList.Bounds;
            ml = r2.Left + (r2.Width - CtlCommon.SmFont.GetTextWidth(BaseLocale.GetStr(RS.rs_Mercenaries))) / 2;
            screen.DrawText(ml, r1.Bottom + 2, BaseLocale.GetStr(RS.rs_Mercenaries), 0);
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();

            fFormationList.Items.BeginUpdate();
            fFormationList.Items.Clear();

            for (var pf = PartyFormation.pfFirst; pf <= PartyFormation.pfLast; pf++) {
                fFormationList.Items.Add(BaseLocale.GetStr(LeaderBrain.PartyFormationsRS[(int)pf]), null);
            }

            fFormationList.Items.EndUpdate();
            int idx = (int)((LeaderBrain)GlobalVars.nwrGame.Player.Brain).Formation;
            fFormationList.SelIndex = idx;
            fFormationList.Items.GetItem(idx).Checked = true;
            UpdateView();
        }
    }
}
