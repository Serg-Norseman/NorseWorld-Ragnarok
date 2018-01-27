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

using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Game;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class OptionsWindow : NWWindow
    {
        private readonly CheckBox fAutoPickup;
        private readonly CheckBox fCircularFOV;
        private readonly CheckBox fExtremeMode;
        private readonly CheckBox fHideInfoPanel;
        private readonly CheckBox fHideCtlPanel;
        private readonly CheckBox fHideLocMap;
        private readonly CheckBox fInvOnlyIcons;
        private readonly ListBox fKeyList;
        private readonly ListBox fLangList;
        private readonly CheckBox fModernStyle;
        private readonly CheckBox fNewStyle;
        private readonly TabControl fPages;
        private readonly ScrollBar fSndVolume;
        private readonly ScrollBar fSngVolume;

        private bool fKeyWait;

        private void OnBtnClose(object sender)
        {
            Hide();
            DoClose();
        }

        private void OnKeyListKeyDown(object sender, KeyEventArgs eventArgs)
        {
            if (fKeyWait) {
                int idx = fKeyList.SelIndex;
                if (idx >= 0 && idx < fKeyList.Items.Count) {
                    UserActionRec uAct = StaticData.dbUserActions[idx];
                    uAct.HotKey = new HotKey(eventArgs.Key, eventArgs.Shift);

                    UpdateKeyList();

                    fKeyList.TopIndex = fKeyList.TopIndex;
                    fKeyList.SelIndex = idx;
                    fKeyWait = false;
                    fKeyList.SelColor = Colors.Gray;
                    fKeyList.SelBorderColor = Colors.Gray;
                    eventArgs.Key = Keys.GK_UNK;
                }
            } else {
                if (eventArgs.Key == Keys.GK_RETURN) {
                    fKeyWait = true;
                    fKeyList.SelColor = Colors.None;
                    fKeyList.SelBorderColor = Colors.Red;
                    eventArgs.Key = Keys.GK_UNK;
                }
            }
            fKeyList.ShowHints = !fKeyWait;
        }

        private void UpdateKeyList()
        {
            fKeyList.Items.Clear();

            for (var ua = UserAction.uaFirst; ua <= UserAction.uaLast; ua++) {
                UserActionRec uAct = StaticData.dbUserActions[(int)ua];

                LBItem item = fKeyList.Items.Add(BaseLocale.GetStr(uAct.NameRes), null);
                string s = BaseMainWindow.HotKeyToText(uAct.HotKey);
                item.SubItems.Add(s, null);
            }
        }

        private void OnLangSelect(object sender, MouseButton button, LBItem item)
        {
            GlobalVars.nwrWin.Language = item.Text;
        }

        private void OnAutoPickupClick(object Sender)
        {
            GlobalVars.nwrWin.AutoPickup = fAutoPickup.Checked;
        }

        private void OnCircularFOVClick(object Sender)
        {
            GlobalVars.nwrWin.CircularFOV = fCircularFOV.Checked;
        }

        private void OnExtremeModeClick(object Sender)
        {
            GlobalVars.nwrWin.ExtremeMode = fExtremeMode.Checked;
        }

        private void OnHideCtlPanelClick(object Sender)
        {
            GlobalVars.nwrWin.HideCtlPanel = fHideCtlPanel.Checked;
        }

        private void OnHideInfoPanelClick(object Sender)
        {
            GlobalVars.nwrWin.HideInfoPanel = fHideInfoPanel.Checked;
        }

        private void OnHideLocMapClick(object Sender)
        {
            GlobalVars.nwrWin.HideLocMap = fHideLocMap.Checked;
        }

        private void OnInvOnlyIconsClick(object Sender)
        {
            GlobalVars.nwrWin.InventoryOnlyIcons = fInvOnlyIcons.Checked;
        }

        private void OnModernStyleClick(object Sender)
        {
            if (fModernStyle.Checked) {
                GlobalVars.nwrWin.Style = NWMainWindow.RGS_MODERN;
            }
        }

        private void OnNewStyleClick(object Sender)
        {
            if (fNewStyle.Checked) {
                GlobalVars.nwrWin.Style = NWMainWindow.RGS_CLASSIC;
            }
        }

        private void OnSongsVolumeChange(object Sender)
        {
            GlobalVars.nwrWin.SongsVolume = ((ScrollBar)Sender).Pos;
        }

        private void OnSoundsVolumeChange(object Sender)
        {
            GlobalVars.nwrWin.SoundsVolume = ((ScrollBar)Sender).Pos;
        }

        protected override void DoClose()
        {
            if (GlobalVars.nwrWin.MainScreen == GameScreen.gsStartup) {
                GlobalVars.nwrWin.ShowStartupWin();
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();

            fSngVolume.Pos = GlobalVars.nwrWin.SongsVolume;
            fSndVolume.Pos = GlobalVars.nwrWin.SoundsVolume;
            fCircularFOV.Checked = GlobalVars.nwrWin.CircularFOV;
            fAutoPickup.Checked = GlobalVars.nwrWin.AutoPickup;
            fExtremeMode.Checked = GlobalVars.nwrWin.ExtremeMode;

            int itfStyle = GlobalVars.nwrWin.Style;
            if (itfStyle != NWMainWindow.RGS_CLASSIC) {
                if (itfStyle == NWMainWindow.RGS_MODERN) {
                    fModernStyle.Checked = true;
                }
            } else {
                fNewStyle.Checked = true;
            }

            fHideCtlPanel.Checked = GlobalVars.nwrWin.HideCtlPanel;
            fHideInfoPanel.Checked = GlobalVars.nwrWin.HideInfoPanel;
            fHideLocMap.Checked = GlobalVars.nwrWin.HideLocMap;
            fInvOnlyIcons.Checked = GlobalVars.nwrWin.InventoryOnlyIcons;

            UpdateKeyList();

            int idx = fLangList.Items.IndexOf(GlobalVars.nwrWin.Language);
            if (idx >= 0) {
                fLangList.SelIndex = idx;
                fLangList.Items.GetItem(idx).Checked = true;
            }
        }

        public OptionsWindow(BaseControl owner)
            : base(owner)
        {

            Font = CtlCommon.SmFont;
            Width = 480;
            Height = 390;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            fPages = new TabControl(this);
            fPages.Left = 10;
            fPages.Top = 10;
            fPages.Width = Width - 20;
            fPages.Height = Height - 30 - 40;

            TabSheet ts = fPages.AddPage(BaseLocale.GetStr(RS.rs_CommonOptions));
            ts.OnLangChange = GlobalVars.nwrWin.LangChange;
            ts.LangResID = 9;

            fSngVolume = new ScrollBar(ts);
            fSngVolume.Min = 0;
            fSngVolume.Max = 255;
            fSngVolume.Pos = 255;
            fSngVolume.OnChange = OnSongsVolumeChange;
            fSngVolume.Kind = ScrollBar.SBK_HORIZONTAL;
            fSngVolume.Width = ts.Width - 50;
            fSngVolume.Left = 25;
            fSngVolume.Top = 30;

            Label label = new Label(ts);
            label.Bounds = ExtRect.Create(25, 30 - 20, 25 + fSngVolume.Width, 30);
            label.LangResID = RS.rs_MusicVolume;
            label.OnLangChange = GlobalVars.nwrWin.LangChange;

            fSndVolume = new ScrollBar(ts);
            fSndVolume.Min = 0;
            fSndVolume.Max = 255;
            fSndVolume.Pos = 255;
            fSndVolume.OnChange = OnSoundsVolumeChange;
            fSndVolume.Kind = ScrollBar.SBK_HORIZONTAL;
            fSndVolume.Width = ts.Width - 50;
            fSndVolume.Left = 25;
            fSndVolume.Top = 70;

            label = new Label(ts);
            label.Bounds = ExtRect.Create(25, 70 - 20, 25 + fSndVolume.Width, 70);
            label.LangResID = RS.rs_SoundsVolume;
            label.OnLangChange = GlobalVars.nwrWin.LangChange;

            fNewStyle = new CheckBox(ts);
            fNewStyle.OnLangChange = GlobalVars.nwrWin.LangChange;
            fNewStyle.LangResID = 12;
            fNewStyle.Group = 1;
            fNewStyle.Left = 25;
            fNewStyle.Top = 100;
            fNewStyle.Width = Width - 50;
            fNewStyle.OnClick = OnNewStyleClick;

            fModernStyle = new CheckBox(ts);
            fModernStyle.OnLangChange = GlobalVars.nwrWin.LangChange;
            fModernStyle.LangResID = 13;
            fModernStyle.Group = 1;
            fModernStyle.Left = 225;
            fModernStyle.Top = 100;
            fModernStyle.Width = Width - 50;
            fModernStyle.OnClick = OnModernStyleClick;

            fHideLocMap = new CheckBox(ts);
            fHideLocMap.OnLangChange = GlobalVars.nwrWin.LangChange;
            fHideLocMap.LangResID = 14;
            fHideLocMap.Left = 25;
            fHideLocMap.Top = 130;
            fHideLocMap.Width = Width - 50;
            fHideLocMap.OnClick = OnHideLocMapClick;

            fHideCtlPanel = new CheckBox(ts);
            fHideCtlPanel.OnLangChange = GlobalVars.nwrWin.LangChange;
            fHideCtlPanel.LangResID = 15;
            fHideCtlPanel.Left = 25;
            fHideCtlPanel.Top = 160;
            fHideCtlPanel.Width = Width - 50;
            fHideCtlPanel.OnClick = OnHideCtlPanelClick;

            fHideInfoPanel = new CheckBox(ts);
            fHideInfoPanel.OnLangChange = GlobalVars.nwrWin.LangChange;
            fHideInfoPanel.LangResID = 16;
            fHideInfoPanel.Left = 25;
            fHideInfoPanel.Top = 190;
            fHideInfoPanel.Width = Width - 50;
            fHideInfoPanel.OnClick = OnHideInfoPanelClick;

            fInvOnlyIcons = new CheckBox(ts);
            fInvOnlyIcons.OnLangChange = GlobalVars.nwrWin.LangChange;
            fInvOnlyIcons.LangResID = 549;
            fInvOnlyIcons.Left = 25;
            fInvOnlyIcons.Top = 220;
            fInvOnlyIcons.Width = Width - 50;
            fInvOnlyIcons.OnClick = OnInvOnlyIconsClick;

            ts = fPages.AddPage(BaseLocale.GetStr(RS.rs_KeyOptions));
            ts.OnLangChange = GlobalVars.nwrWin.LangChange;
            ts.LangResID = 10;

            fKeyList = new ListBox(ts);
            fKeyList.Mode = ListBox.MODE_REPORT;
            fKeyList.Left = 10;
            fKeyList.Top = 10;
            fKeyList.Width = ts.Width - 20;
            fKeyList.Height = ts.Height - 20;
            fKeyList.ColumnTitles.Add("name", 300);
            fKeyList.ColumnTitles.Add("key", 150);
            fKeyList.OnKeyDown = OnKeyListKeyDown;
            fKeyList.ShowHints = true;
            fKeyList.Hint = "hint";

            ts = fPages.AddPage(BaseLocale.GetStr(RS.rs_GameplayOptions));
            ts.OnLangChange = GlobalVars.nwrWin.LangChange;
            ts.LangResID = 11;

            fCircularFOV = new CheckBox(ts);
            fCircularFOV.OnLangChange = GlobalVars.nwrWin.LangChange;
            fCircularFOV.LangResID = 17;
            fCircularFOV.Left = 25;
            fCircularFOV.Top = 25;
            fCircularFOV.Width = Width - 50;
            fCircularFOV.OnClick = OnCircularFOVClick;

            fAutoPickup = new CheckBox(ts);
            fAutoPickup.OnLangChange = GlobalVars.nwrWin.LangChange;
            fAutoPickup.LangResID = 536;
            fAutoPickup.Left = 25;
            fAutoPickup.Top = 65;
            fAutoPickup.Width = Width - 50;
            fAutoPickup.OnClick = OnAutoPickupClick;

            fExtremeMode = new CheckBox(ts);
            fExtremeMode.OnLangChange = GlobalVars.nwrWin.LangChange;
            fExtremeMode.LangResID = 82;
            fExtremeMode.Left = 25;
            fExtremeMode.Top = 105;
            fExtremeMode.Width = Width - 50;
            fExtremeMode.OnClick = OnExtremeModeClick;
            ts = fPages.AddPage(BaseLocale.GetStr(RS.rs_Language));
            ts.OnLangChange = GlobalVars.nwrWin.LangChange;
            ts.LangResID = 815;

            fLangList = new ListBox(ts);
            fLangList.Mode = ListBox.MODE_LIST;
            fLangList.Options = new LBOptions(LBOptions.lboChecks, LBOptions.lboRadioChecks);
            fLangList.Left = 10;
            fLangList.Top = 10;
            fLangList.Width = ts.Width - 20;
            fLangList.Height = ts.Height - 20;

            Locale locale = GlobalVars.nwrWin.Locale;
            int num = locale.LangsCount;
            for (int i = 0; i < num; i++) {
                fLangList.Items.Add(locale.GetLang(i).Name, null);
            }
            fLangList.OnItemSelect = OnLangSelect;

            fPages.TabIndex = 0;

            NWButton tRButton = new NWButton(this);
            tRButton.Width = 90;
            tRButton.Height = 30;
            tRButton.Left = Width - 90 - 20;
            tRButton.Top = Height - 30 - 20;
            tRButton.ImageFile = "itf/DlgBtn.tga";
            tRButton.OnClick = OnBtnClose;
            tRButton.OnLangChange = GlobalVars.nwrWin.LangChange;
            tRButton.LangResID = 8;
        }

        public override void ChangeLang()
        {
            base.ChangeLang();

            fKeyList.Hint = BaseLocale.GetStr(RS.rs_HKHint);
            UpdateKeyList();
        }
    }
}
