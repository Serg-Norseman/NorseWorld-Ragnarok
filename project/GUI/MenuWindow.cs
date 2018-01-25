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

using NWR.Core.Types;
using NWR.Game;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class MenuWindow : NWWindow
    {
        private readonly BaseImage fSplash;

        private void OnBtnSave(object sender)
        {
            Hide();
            GlobalVars.nwrWin.DoEvent(EventID.event_Save, null, null, null);
        }

        private void OnBtnExit(object sender)
        {
            GlobalVars.nwrWin.DoEvent(EventID.event_Quit, null, null, null);
        }

        private void OnBtnLoad(object sender)
        {
            Hide();
            GlobalVars.nwrWin.DoEvent(EventID.event_Load, null, null, null);
        }

        private void OnBtnNew(object sender)
        {
            Hide();
            GlobalVars.nwrWin.SetScreen(GameScreen.gsStartup);
            GlobalVars.nwrWin.DoEvent(EventID.event_New, null, null, null);
        }

        private void OnBtnOptions(object sender)
        {
            Hide();
            GlobalVars.nwrWin.DoEvent(EventID.event_Options, null, null, null);
        }

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);
            int xx = (Width - (int)fSplash.Width) / 2;
            int yy = 20;
            screen.DrawImage(xx, yy, 0, 0, (int)fSplash.Width, (int)fSplash.Height, fSplash, 255);
        }

        public MenuWindow(BaseControl owner)
            : base(owner)
        {
            fSplash = NWResourceManager.LoadImage(MainWindow.Screen, "itf/Splash.tga", BaseScreen.clNone);

            int offset = 110;
            Width = 204;
            Height = 270 + offset;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            NWButton tRButton = new NWButton(this);
            tRButton.Left = 20;
            tRButton.Top = offset + 20;
            tRButton.Width = 164;
            tRButton.Height = 30;
            tRButton.ImageFile = "itf/MenuBtn.tga";
            tRButton.OnClick = OnBtnClose;
            tRButton.OnLangChange = GlobalVars.nwrWin.LangChange;
            tRButton.LangResID = 7;

            NWButton tRButton2 = new NWButton(this);
            tRButton2.Left = 20;
            tRButton2.Top = offset + 60;
            tRButton2.Width = 164;
            tRButton2.Height = 30;
            tRButton2.ImageFile = "itf/MenuBtn.tga";
            tRButton2.OnClick = OnBtnNew;
            tRButton2.OnLangChange = GlobalVars.nwrWin.LangChange;
            tRButton2.LangResID = 1;

            NWButton tRButton3 = new NWButton(this);
            tRButton3.Left = 20;
            tRButton3.Top = offset + 100;
            tRButton3.Width = 164;
            tRButton3.Height = 30;
            tRButton3.ImageFile = "itf/MenuBtn.tga";
            tRButton3.OnClick = OnBtnLoad;
            tRButton3.OnLangChange = GlobalVars.nwrWin.LangChange;
            tRButton3.LangResID = 2;

            NWButton tRButton4 = new NWButton(this);
            tRButton4.Left = 20;
            tRButton4.Top = offset + 140;
            tRButton4.Width = 164;
            tRButton4.Height = 30;
            tRButton4.ImageFile = "itf/MenuBtn.tga";
            tRButton4.OnClick = OnBtnSave;
            tRButton4.OnLangChange = GlobalVars.nwrWin.LangChange;
            tRButton4.LangResID = 3;

            NWButton tRButton5 = new NWButton(this);
            tRButton5.Left = 20;
            tRButton5.Top = offset + 180;
            tRButton5.Width = 164;
            tRButton5.Height = 30;
            tRButton5.ImageFile = "itf/MenuBtn.tga";
            tRButton5.OnClick = OnBtnOptions;
            tRButton5.OnLangChange = GlobalVars.nwrWin.LangChange;
            tRButton5.LangResID = 5;

            NWButton tRButton6 = new NWButton(this);
            tRButton6.Left = 20;
            tRButton6.Top = offset + 220;
            tRButton6.Width = 164;
            tRButton6.Height = 30;
            tRButton6.ImageFile = "itf/MenuBtn.tga";
            tRButton6.OnClick = OnBtnExit;
            tRButton6.OnLangChange = GlobalVars.nwrWin.LangChange;
            tRButton6.LangResID = 6;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fSplash.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
