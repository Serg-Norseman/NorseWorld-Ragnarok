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

namespace NWR.GUI
{
    public sealed class AboutWindow : NWWindow
    {
        private readonly TextBox fText;

        public AboutWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 590;
            Height = 430;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            fText = new TextBox(this);
            fText.Font = CtlCommon.SmFont;
            fText.Left = 10;
            fText.Top = 10;
            fText.ControlStyle.Exclude(ControlStyles.сsOpaque);
            fText.Width = 570;
            fText.Height = 410;
            fText.Visible = true;
            fText.OnGetVariable = OnGetVar;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fText.Dispose();
            }
            base.Dispose(disposing);
        }

        private void OnGetVar(object sender, ref string  refVar)
        {
            if (refVar.CompareTo("ver") == 0) {
                refVar = StaticData.Rs_GameVersion;
                refVar = refVar.Substring(1);
            } else {
                if (refVar.CompareTo("dev_time") == 0) {
                    refVar = StaticData.Rs_GameDevTime;
                }
            }
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

            fText.Lines.BeginUpdate();
            fText.Lines.Text = GetTextFileByLang("About");
            fText.Lines.EndUpdate();
            ActiveControl = fText;
        }
    }
}
