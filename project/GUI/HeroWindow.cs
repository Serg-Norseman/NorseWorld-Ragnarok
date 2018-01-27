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
using NWR.Core;
using NWR.Core.Types;
using NWR.Game;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class HeroWindow : NWWindow
    {
        private readonly EditBox fEditBox;
        private readonly BaseImage[] fImages;

        protected override void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            Keys key = eventArgs.Key;

            if (key != Keys.GK_ESCAPE) {
                if (key == Keys.GK_F1) {
                    GlobalVars.nwrGame.Player.RandomName();
                    fEditBox.Text = GlobalVars.nwrGame.Player.Name;
                }
            } else {
                eventArgs.Key = Keys.GK_UNK;
            }

            base.DoKeyDownEvent(eventArgs);
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            base.DoMouseDownEvent(eventArgs);
            try {
                for (var pa = SysCreature.sc_First; pa <= SysCreature.sc_Last; pa++) {
                    SysCreatureRec sc = StaticData.dbSysCreatures[(int)pa];

                    if (sc.ScrRect.Contains(eventArgs.X, eventArgs.Y)) {
                        Hide();
                        GlobalVars.nwrGame.SelectHero(sc.Sign, fEditBox.Text);
                        return;
                    }
                }
            } catch (Exception ex) {
                Logger.Write("HeroWindow.DoMouseDown(): " + ex.Message);
                throw ex;
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);

            string s = BaseLocale.GetStr(RS.rs_AdventurerName);
            screen.SetTextColor(Colors.Gold, true);
            screen.DrawText((Width - CtlCommon.SmFont.GetTextWidth(s)) / 2, fEditBox.Top - CtlCommon.SmFont.Height, s, 0);
            s = BaseLocale.GetStr(RS.rs_Apprenticeship);
            screen.Font = CtlCommon.BgFont;
            screen.SetTextColor(Colors.Gold, true);
            screen.DrawText((Width - CtlCommon.BgFont.GetTextWidth(s)) / 2, 60, s, 0);
            screen.Font = CtlCommon.SmFont;

            for (var pa = SysCreature.sc_First; pa <= SysCreature.sc_Last; pa++) {
                SysCreatureRec sc = StaticData.dbSysCreatures[(int)pa];

                s = BaseLocale.GetStr(sc.Name);
                ExtRect r = sc.ScrRect;
                BaseImage img = fImages[(int)pa];
                screen.DrawImage(r.Left, r.Top, 0, 0, (int)img.Width, (int)img.Height, img, 255);
                screen.DrawText(r.Left + (r.Width - CtlCommon.SmFont.GetTextWidth(s)) / 2, r.Top - CtlCommon.SmFont.Height, s, 0);
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            fEditBox.Text = GlobalVars.nwrGame.Player.Name;
            ActiveControl = fEditBox;
        }

        public HeroWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 590;
            Height = 430;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            fImages = new BaseImage[6];
            for (int pa = (int)SysCreature.sc_First; pa <= (int)SysCreature.sc_Last; pa++) {
                SysCreatureRec sc = StaticData.dbSysCreatures[pa];

                fImages[pa] = NWResourceManager.LoadImage(MainWindow.Screen, "itf/pa_" + sc.Sign + ".tga", Colors.None);
            }

            fEditBox = new EditBox(this);
            fEditBox.Left = (Width - 200) / 2;
            fEditBox.Top = 35;
            fEditBox.Width = 200;
            fEditBox.Visible = true;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                for (var ap = SysCreature.sc_First; ap <= SysCreature.sc_Last; ap++) {
                    fImages[(int)ap].Dispose();
                }
            }
            base.Dispose(disposing);
        }
    }
}
