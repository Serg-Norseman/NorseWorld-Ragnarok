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
    public delegate void IInputAcceptProc(string aInput);

    /// <summary>
    /// 
    /// </summary>
    public sealed class InputWindow : NWWindow
    {
        private readonly EditBox fEditBox;

        public IInputAcceptProc AcceptProc;

        private void OnEditKeyDown(object sender, KeyEventArgs eventArgs)
        {
            if (eventArgs.Key == Keys.GK_RETURN) {
                if (AcceptProc != null) {
                    AcceptProc.Invoke(fEditBox.Text);
                }
                fEditBox.Text = "";
                Hide();
            }
        }

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        private void OnBtnAccept(object sender)
        {
            KeyEventArgs eventArgs = new KeyEventArgs();
            eventArgs.Key = Keys.GK_RETURN;
            OnEditKeyDown(this, eventArgs);
        }

        public string Value
        {
            get {
                return fEditBox.Text;
            }
            set {
                fEditBox.Text = value;
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);
            screen.DrawText(10, 10, Caption, 0);
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            ActiveControl = fEditBox;
        }

        public InputWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 480;
            Height = 118;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            fEditBox = new EditBox(this);
            fEditBox.Left = 10;
            fEditBox.Top = 10 + (CtlCommon.SmFont.Height + 10);
            fEditBox.Width = Width - 20;
            fEditBox.OnKeyDown = OnEditKeyDown;

            NWButton closeBtn = new NWButton(this);
            closeBtn.Width = 90;
            closeBtn.Height = 30;
            closeBtn.Left = Width - 90 - 10;
            closeBtn.Top = fEditBox.Top + fEditBox.Height + 10;
            closeBtn.ImageFile = "itf/DlgBtn.tga";
            closeBtn.OnClick = OnBtnClose;
            closeBtn.OnLangChange = GlobalVars.nwrWin.LangChange;
            closeBtn.LangResID = 8;

            NWButton tRButton = new NWButton(this);
            tRButton.Width = 90;
            tRButton.Height = 30;
            tRButton.Left = closeBtn.Left - 90 - 10;
            tRButton.Top = closeBtn.Top;
            tRButton.ImageFile = "itf/DlgBtn.tga";
            tRButton.OnClick = OnBtnAccept;
            tRButton.OnLangChange = GlobalVars.nwrWin.LangChange;
            tRButton.LangResID = 26;
        }
    }
}
