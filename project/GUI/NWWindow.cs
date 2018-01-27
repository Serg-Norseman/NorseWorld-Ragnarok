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
using System.IO;
using BSLib;
using NWR.Core;
using NWR.Game;
using NWR.GUI.Controls;
using NWR.Items;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public abstract class NWWindow : BaseWindow
    {
        public bool BackDraw;
        public bool Shifted;

        protected NWWindow(BaseControl owner)
            : base(owner)
        {
            BackDraw = true;
            Shifted = false;
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            if (CtlCommon.WinBack != null && CtlCommon.WinDecor != null) {
                ExtRect crt = ClientRect;
                int L = crt.Left;
                int T = crt.Top;
                int R = crt.Right;
                int B = crt.Bottom;

                if (BackDraw) {
                    screen.DrawFilled(crt, BaseScreen.FILL_TILE, 0, 0, (int)CtlCommon.WinBack.Width, (int)CtlCommon.WinBack.Height, 0, 0, CtlCommon.WinBack);
                } else {
                    screen.FillRect(crt, Colors.Black);
                }

                screen.DrawFilled(crt, BaseScreen.FILL_HORZ, 16, 0, 61, 8, L, T, CtlCommon.WinDecor);
                screen.DrawFilled(crt, BaseScreen.FILL_HORZ, 16, 0, 61, 8, L, B - 7, CtlCommon.WinDecor);
                screen.DrawFilled(crt, BaseScreen.FILL_VERT, 0, 16, 8, 61, L, T, CtlCommon.WinDecor);
                screen.DrawFilled(crt, BaseScreen.FILL_VERT, 0, 16, 8, 61, R - 7, T, CtlCommon.WinDecor);

                screen.DrawImage(L, T, 0, 0, 8, 8, CtlCommon.WinDecor, 255);
                screen.DrawImage(L, B - 7, 0, 0, 8, 8, CtlCommon.WinDecor, 255);
                screen.DrawImage(R - 7, T, 0, 0, 8, 8, CtlCommon.WinDecor, 255);
                screen.DrawImage(R - 7, B - 7, 0, 0, 8, 8, CtlCommon.WinDecor, 255);
                screen.DrawImage(L + 8, T + 8, 8, 8, 13, 10, CtlCommon.WinDecor, 255);
                screen.DrawImage(L + 8, B - 7 - 10, 8, 75, 13, 10, CtlCommon.WinDecor, 255);
                screen.DrawImage(R - 7 - 13, T + 8, 72, 8, 13, 10, CtlCommon.WinDecor, 255);
                screen.DrawImage(R - 7 - 13, B - 7 - 10, 72, 75, 13, 10, CtlCommon.WinDecor, 255);
            }
        }

        protected override void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            base.DoKeyDownEvent(eventArgs);

            if (eventArgs.Key == Keys.GK_ESCAPE) {
                Hide();
                DoClose();
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();

            if (Shifted) { //GVHide
                GlobalVars.nwrWin.DialogVisibleRefs++;
            }

            if (Shifted) {
                int w;
                if (GlobalVars.nwrWin.HideCtlPanel && GlobalVars.nwrWin.HideInfoPanel) {
                    w = GlobalVars.nwrWin.Width;
                } else {
                    w = 640;
                }
                Left = (w - Width) / 2;
                Top = (StaticData.TV_Rect.Top - Height) / 2;
            }
        }

        protected override void DoHideEvent()
        {
            base.DoHideEvent();

            if (Shifted) { //GVHide
                GlobalVars.nwrWin.DialogVisibleRefs--;
            }
        }

        public static void AddListItem(ListBox aList, string aName, Item aItem, bool aIcons)
        {
            LBItem listItem = aList.Items.Add(aName, aItem);
            if (aItem != null) {
                var ikRec = StaticData.dbItemKinds[(int)aItem.Kind];
                listItem.Color = ikRec.Color;

                if (!aIcons) {
                    listItem.ImageIndex = (int)aItem.Symbol;
                } else {
                    listItem.ImageIndex = aItem.ImageIndex;
                }
            }
        }

        public static void SetupItemsList(ListBox aListBox)
        {
            int ih;
            int iw;
            if (!GlobalVars.nwrWin.InventoryOnlyIcons) {
                ih = 10;
                iw = 8;
                aListBox.ImagesList = GlobalVars.nwrWin.Symbols;
            } else {
                iw = 32;
                ih = 30;
                aListBox.ImagesList = GlobalVars.nwrWin.Resources;
            }
            aListBox.IconHeight = ih;
            aListBox.IconWidth = iw;
            if (!GlobalVars.nwrWin.InventoryOnlyIcons) {
                aListBox.Mode = ListBox.MODE_LIST;
                aListBox.Columns = 1;
            } else {
                aListBox.Mode = ListBox.MODE_ICONS;
            }
        }

        public static void OnListMouseMove(object Sender, int X, int Y, bool trade)
        {
            try {
                ListBox list = (ListBox)Sender;
                int idx = list.GetItemByMouse(X, Y);

                if (idx >= 0 && idx < list.Items.Count) {
                    object data = list.Items.GetItem(idx).Data;
                    if (data != null && data is Item) {
                        Item item = (Item)data;
                        list.Hint = item.GetHint(trade);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWWindow.xListMouseMove(): " + ex.Message);
            }
        }

        public static string GetTextFileByLang(string fileName)
        {
            string result;
            string ext = GlobalVars.nwrWin.LangExt;
            fileName = fileName + "." + ext;

            try {
                Stream stm = NWResourceManager.LoadStream(fileName);
                using (BinaryReader dis = new BinaryReader(stm)) {
                    result = StreamUtils.ReadText(dis, StaticData.DefEncoding);
                }
            } catch (Exception) {
                result = "";
            }

            return result;
        }
    }
}
