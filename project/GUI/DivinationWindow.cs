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
using NWR.Core.Types;
using NWR.Database;
using NWR.Game;
using NWR.GUI.Controls;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class DivinationWindow : NWWindow
    {
        private readonly TextBox fTextBox;

        private int[] fRunes;

        private void DoDivination()
        {
            fRunes = new int[3];
            fRunes[0] = RandomHelper.GetRandom(StaticData.dbRunes.Length);
            fRunes[1] = RandomHelper.GetRandom(StaticData.dbRunes.Length);
            fRunes[2] = RandomHelper.GetRandom(StaticData.dbRunes.Length);
        }

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            DoDivination();
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

            ExtRect crt = ClientRect;
            ExtRect cr = new ExtRect();
            cr.Left = crt.Left + 10;
            cr.Right = crt.Right - 10;
            cr.Top = crt.Top + 10;
            cr.Bottom = cr.Top + 36 - 1;
            CtlCommon.DrawCtlBorder(screen, cr);

            //int ww = (32 * 3) + (10 * 2);
            //cr.inflate(-2, -2);
            int offset = (cr.Width) / 3; //- ww

            int sy = cr.Top + (cr.Height - screen.GetTextHeight("A")) / 2;

            for (int i = 0; i < 3; i++) {
                int runeId = fRunes[i];
                RuneRec rune = StaticData.dbRunes[runeId];
                //GlobalVars.nwrWin.Resources.drawImage(screen, cr.Left + offset + (i * (32 + 10)), cr.Top + 3, rune.ImageIndex, 255);
                InfoEntry inf = (InfoEntry)GlobalVars.nwrDB.FindEntryBySign(rune.Sign);

                int sx = cr.Left + (offset * i);
                GlobalVars.nwrWin.Resources.DrawImage(screen, sx, cr.Top + 3, rune.ImageIndex, 255);

                sx = sx + 32 + 3;
                screen.DrawText(sx, sy, inf.Name, 0);
            }

            /*rt = new TRect(cr.Left + 2, cr.Top + 2, cr.Left + 2 + 32 - 1, cr.Top + 2 + 32 - 1);
            screen.fillRect(rt, BaseScreen.clWhite);
            
            screen.drawText(rt.Right + 10, cr.Top + (36 - screen.getTextHeight("A")) / 2, this.fCollocutor.getName(), 0);*/
        }

        public DivinationWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 420;
            Height = 340;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            fTextBox = new TextBox(this);
            fTextBox.Left = 20;
            fTextBox.Top = 50;
            fTextBox.Height = 230;
            fTextBox.Width = 380;

            NWButton btnClose = new NWButton(this);
            btnClose.Left = 310;
            btnClose.Top = 290;
            btnClose.Width = 90;
            btnClose.Height = 30;
            btnClose.ImageFile = "itf/DlgBtn.tga";
            btnClose.OnClick = OnBtnClose;
            btnClose.OnLangChange = GlobalVars.nwrWin.LangChange;
            btnClose.LangResID = 8;
        }
    }
}
