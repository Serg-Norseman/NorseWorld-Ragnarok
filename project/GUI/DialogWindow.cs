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

using NWR.Creatures;
using NWR.Game;
using NWR.GUI.Controls;

namespace NWR.GUI
{
    public class DialogWindow : NWWindow
    {
        protected NWCreature fCollocutor;

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        public NWCreature Collocutor
        {
            set {
                if (fCollocutor != value) {
                    fCollocutor = value;
                }
            }
        }

        public DialogWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 600;
            Height = 460;
            WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview);
            Shifted = true;

            NWButton btnClose = new NWButton(this);
            btnClose.Width = 90;
            btnClose.Height = 30;
            btnClose.Left = Width - 90 - 20;
            btnClose.Top = Height - 30 - 20;
            btnClose.ImageFile = "itf/DlgBtn.tga";
            btnClose.OnClick = OnBtnClose;
            btnClose.OnLangChange = GlobalVars.nwrWin.LangChange;
            btnClose.LangResID = 8;
        }
    }
}
