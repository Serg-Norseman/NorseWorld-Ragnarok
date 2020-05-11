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

using NWR.Game;
using NWR.Game.Types;
using NWR.GUI.Controls;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class StartupWindow : NWWindow
    {
        private void OnBtnNew(object sender)
        {
            Hide();
            GlobalVars.nwrWin.DoEvent(EventID.event_New, null, null, null);
        }

        private void OnBtnLoad(object sender)
        {
            Hide();
            GlobalVars.nwrWin.DoEvent(EventID.event_Load, null, null, null);
        }

        private void OnBtnAbout(object sender)
        {
            Hide();
            GlobalVars.nwrWin.DoEvent(EventID.event_About, null, null, null);
        }

        private void OnBtnOptions(object sender)
        {
            Hide();
            GlobalVars.nwrWin.DoEvent(EventID.event_Options, null, null, null);
        }

        private void OnBtnExit(object sender)
        {
            GlobalVars.nwrWin.DoEvent(EventID.event_Quit, null, null, null);
        }

        protected override void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            switch (eventArgs.Key)
            {
                case Keys.GK_ESCAPE:
                    eventArgs.Key = Keys.GK_UNK;
                    break;

                case Keys.GK_S:
                    // FIXME: temp stub!
                    GlobalVars.nwrWin.SetScreen(GameScreen.gsDead);
                    Hide();
                    break;

                default:
                    GlobalVars.nwrWin.TestSound(eventArgs.Key);
                    break;
            }

            base.DoKeyDownEvent(eventArgs);
        }

        public StartupWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 203;
            Height = 230;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            NWButton btn = new NWButton(this);
            btn.Left = 20;
            btn.Top = 20;
            btn.Width = 163;
            btn.Height = StaticData.BtnHeight;
            btn.ImageFile = "itf/MenuBtn.tga";
            btn.OnClick = OnBtnNew;
            btn.OnLangChange = GlobalVars.nwrWin.LangChange;
            btn.LangResID = 1;

            btn = new NWButton(this);
            btn.Left = 20;
            btn.Top = 60;
            btn.Width = 163;
            btn.Height = StaticData.BtnHeight;
            btn.ImageFile = "itf/MenuBtn.tga";
            btn.OnClick = OnBtnLoad;
            btn.OnLangChange = GlobalVars.nwrWin.LangChange;
            btn.LangResID = 2;

            btn = new NWButton(this);
            btn.Left = 20;
            btn.Top = 100;
            btn.Width = 163;
            btn.Height = StaticData.BtnHeight;
            btn.ImageFile = "itf/MenuBtn.tga";
            btn.OnClick = OnBtnOptions;
            btn.OnLangChange = GlobalVars.nwrWin.LangChange;
            btn.LangResID = 5;

            btn = new NWButton(this);
            btn.Left = 20;
            btn.Top = 140;
            btn.Width = 163;
            btn.Height = StaticData.BtnHeight;
            btn.ImageFile = "itf/MenuBtn.tga";
            btn.OnClick = OnBtnAbout;
            btn.OnLangChange = GlobalVars.nwrWin.LangChange;
            btn.LangResID = 4;

            btn = new NWButton(this);
            btn.Left = 20;
            btn.Top = 180;
            btn.Width = 163;
            btn.Height = StaticData.BtnHeight;
            btn.ImageFile = "itf/MenuBtn.tga";
            btn.OnClick = OnBtnExit;
            btn.OnLangChange = GlobalVars.nwrWin.LangChange;
            btn.LangResID = 6;
        }
    }
}
