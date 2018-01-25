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

using NWR.Game;
using NWR.GUI.Controls;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class MessageWindow : NWWindow
    {
        private readonly TextBox fTextBox;

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        public string Text
        {
            get {
                return fTextBox.Lines.Text;
            }
            set {
                fTextBox.Lines.Text = value;
            }
        }

        private void OnLinkClick(object sender, string linkValue)
        {
            GlobalVars.nwrWin.ShowKnowledge(linkValue);
        }

        protected override void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            base.DoKeyDownEvent(eventArgs);
            if (eventArgs.Key == Keys.GK_RETURN) {
                Hide();
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);

            screen.SetTextColor(BaseScreen.clGold, true);
        }

        public MessageWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 320;
            Height = 240;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            NWButton tRButton = new NWButton(this);
            tRButton.Left = 210;
            tRButton.Top = 190;
            tRButton.Width = 90;
            tRButton.Height = 30;
            tRButton.ImageFile = "itf/DlgBtn.tga";
            tRButton.OnClick = OnBtnClose;
            tRButton.OnLangChange = GlobalVars.nwrWin.LangChange;
            tRButton.LangResID = 8;

            fTextBox = new TextBox(this);
            fTextBox.Left = 20;
            fTextBox.Top = 20;
            fTextBox.Height = 160;
            fTextBox.Width = 280;
            fTextBox.Links = true;
            fTextBox.OnLinkClick = OnLinkClick;
        }
    }
}
