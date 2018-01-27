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
using NWR.Core.Types;
using NWR.Database;
using NWR.Game;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class FilesWindow : NWWindow
    {
        private sealed class GameFile
        {
            public bool Exist;
            public string PlayerName;
            public string LandName;
            public DateTime SaveTime = new DateTime();
        }

        public const int MI_FILEDELETE = 0;
        public const int MI_FILENUM = 1;

        public const int FWMODE_LOAD = 0;
        public const int FWMODE_SAVE = 1;

        private const int MaxList = 10;

        private readonly GameFile[] fFiles;

        public int FilesMode;

        public FilesWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 592;
            Height = 432;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            fFiles = new GameFile[MaxList];

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

        private static ExtRect GetFileOpRect(int i, int op)
        {
            ExtRect result = ExtRect.Empty;

            switch (op) {
                case MI_FILEDELETE:
                    result = ExtRect.Create(30, 86 + i * 24, 52, 86 + i * 24 + 22);
                    break;

                case MI_FILENUM:
                    result = ExtRect.Create(56, 86 + i * 24, 78, 86 + i * 24 + 22);
                    break;
            }

            return result;
        }

        private void OnBtnClose(object sender)
        {
            Hide();
            DoClose();
        }

        private void PrepareFile(int index)
        {
            try
            {
                switch (FilesMode) {
                    case FWMODE_LOAD:
                        if (fFiles[index].Exist) {
                            GlobalVars.nwrWin.Clear();
                            GlobalVars.nwrGame.LoadGame(index);
                            Hide();
                        }
                        break;

                    case FWMODE_SAVE:
                        GlobalVars.nwrGame.SaveGame(index);
                        Hide();
                        break;
                }
            }
            catch (Exception ex) {
                Logger.Write("FilesWindow.prepareFile(): " + ex.Message);
                GlobalVars.nwrWin.ShowMessage("Critical error!");
            }
        }

        private void UpdateList()
        {
            Player player = new Player(null, null);
            try {
                for (int i = 0; i < MaxList; i++) {
                    string fn = NWGameSpace.GetSaveFile(NWGameSpace.SAVEFILE_PLAYER, i);

                    fFiles[i] = new GameFile();
                    fFiles[i].Exist = File.Exists(fn);
                    if (fFiles[i].Exist) {
                        fFiles[i].SaveTime = File.GetLastWriteTime(fn);

                        try {
                            NWGameSpace.LoadPlayer(i, player);

                            fFiles[i].PlayerName = player.Name;

                            int fx = player.Field.X;
                            int fy = player.Field.Y;

                            LayerEntry layer = (LayerEntry)GlobalVars.nwrDB.GetEntry(player.LayerID);
                            string landSign = layer.GetFieldEntry(fx, fy).LandSign;

                            LandEntry land = (LandEntry)GlobalVars.nwrDB.FindEntryBySign(landSign);
                            fFiles[i].LandName = land.Name;
                        } catch (Exception ex) {
                            Logger.Write("FilesWindow.refreshList.PlayerLoad(" + fn + "): " + ex.Message);
                            fFiles[i].PlayerName = "<error>";
                            fFiles[i].LandName = "<error>";
                        }
                    } else {
                        fFiles[i].PlayerName = BaseLocale.GetStr(RS.rs_PlayerUnknown);
                        fFiles[i].LandName = "-";
                        fFiles[i].SaveTime = new DateTime();
                    }
                }
            } finally {
                player.Dispose();
            }
        }

        protected override void DoClose()
        {
            if (GlobalVars.nwrWin.MainScreen == GameScreen.gsStartup) {
                GlobalVars.nwrWin.ShowStartupWin();
            }
        }

        protected override void DoKeyPressEvent(KeyPressEventArgs eventArgs)
        {
            base.DoKeyPressEvent(eventArgs);

            if ("0123456789".IndexOf(eventArgs.Key) >= 0) {
                int i = (int)eventArgs.Key - 48;
                PrepareFile(i);
            }
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            base.DoMouseDownEvent(eventArgs);

            if (eventArgs.Button == MouseButton.mbLeft) {
                for (int i = 0; i <= 9; i++) {
                    ExtRect r = GetFileOpRect(i, MI_FILEDELETE);
                    if (r.Contains(eventArgs.X, eventArgs.Y) && fFiles[i].Exist) {
                        GlobalVars.nwrGame.EraseGame(i);
                        fFiles[i].Exist = false;
                        UpdateList();
                        break;
                    }

                    r = GetFileOpRect(i, MI_FILENUM);
                    if (r.Contains(eventArgs.X, eventArgs.Y)) {
                        PrepareFile(i);
                        break;
                    }
                }
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);

            CtlCommon.SmFont.Color = Colors.Gold;
            CtlCommon.BgFont.Color = Colors.Gold;

            screen.Font = CtlCommon.BgFont;

            switch (FilesMode) {
                case FWMODE_LOAD:
                    screen.DrawText(30, 22, BaseLocale.GetStr(RS.rs_GameLoad), 0);
                    break;

                case FWMODE_SAVE:
                    screen.DrawText(30, 22, BaseLocale.GetStr(RS.rs_GameSave), 0);
                    break;
            }

            screen.Font = CtlCommon.SmFont;

            for (int i = 0; i < MaxList; i++) {
                ExtRect rd = GetFileOpRect(i, MI_FILEDELETE);
                GlobalVars.nwrWin.Resources.DrawImage(screen, rd.Left, rd.Top, StaticData.dbItfElements[(int)ItfElement.id_FileDelete].ImageIndex, 255);
                ExtRect rn = GetFileOpRect(i, MI_FILENUM);
                GlobalVars.nwrWin.Resources.DrawImage(screen, rn.Left, rn.Top, StaticData.dbItfElements[(int)ItfElement.id_FileNum].ImageIndex, 255);
                int col = Colors.Navy;
                screen.SetTextColor(col, true);
                screen.DrawText(rd.Left + 34, rd.Top + 2, Convert.ToString(i), 0);
                screen.SetTextColor(Colors.Gold, true);
                screen.DrawText(rd.Left + 54, rd.Top + 2, fFiles[i].PlayerName, 0);
                screen.DrawText(rd.Left + 200, rd.Top + 2, fFiles[i].LandName, 0);

                string time;
                if (fFiles[i].SaveTime == new DateTime()) {
                    time = "(?)";
                } else {
                    time = "(" + fFiles[i].SaveTime.ToString("yyyy.MM.dd hh:mm:ss") + ")";
                }
                screen.DrawText(rd.Left + 380, rd.Top + 2, time, 0);
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            UpdateList();
        }
    }

}