/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
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
package nwr.gui;

import jzrlib.utils.Logger;
import jzrlib.core.Rect;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.types.GameScreen;
import nwr.core.types.ItfElement;
import nwr.database.LandEntry;
import nwr.database.LayerEntry;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.KeyPressEventArgs;
import nwr.engine.MouseButton;
import nwr.engine.MouseEventArgs;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.game.NWGameSpace;
import nwr.main.GlobalVars;
import nwr.player.Player;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class FilesWindow extends NWWindow
{
    private static final class GameFile
    {
        public boolean Exist;
        public String PlayerName;
        public String LandName;
        public Date SaveTime = new Date(0);
    }

    public static final int MI_FILEDELETE = 0;
    public static final int MI_FILENUM = 1;

    public static final int FWMODE_LOAD = 0;
    public static final int FWMODE_SAVE = 1;

    private static final int MaxList = 10;

    private final GameFile[] fFiles;

    public int FilesMode;

    public FilesWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(592);
        super.setHeight(432);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        this.fFiles = new GameFile[MaxList];
        
        NWButton btnClose = new NWButton(this);
        btnClose.setWidth(90);
        btnClose.setHeight(30);
        btnClose.setLeft(super.getWidth() - 90 - 20);
        btnClose.setTop(super.getHeight() - 30 - 20);
        btnClose.setImageFile("itf/DlgBtn.tga");
        btnClose.OnClick = this::onBtnClose;
        btnClose.OnLangChange = GlobalVars.nwrWin::LangChange;
        btnClose.setLangResID(8);
    }

    private Rect getFileOpRect(int i, int op)
    {
        Rect result = Rect.Empty();

        switch (op) {
            case MI_FILEDELETE:
                result = new Rect(30, 86 + i * 24, 52, 86 + i * 24 + 22);
                break;

            case MI_FILENUM:
                result = new Rect(56, 86 + i * 24, 78, 86 + i * 24 + 22);
                break;
        }

        return result;
    }

    private void onBtnClose(Object sender)
    {
        this.hide();
        this.DoClose();
    }

    private void prepareFile(int index)
    {
        try {
            try {
                switch (this.FilesMode) {
                    case FWMODE_LOAD:
                        if (this.fFiles[index].Exist) {
                            GlobalVars.nwrWin.clear();
                            GlobalVars.nwrGame.loadGame(index);
                            this.hide();
                        }
                        break;

                    case FWMODE_SAVE:
                        GlobalVars.nwrGame.saveGame(index);
                        this.hide();
                        break;
                }
            } finally {

            }
        } catch (Exception ex) {
            Logger.write("FilesWindow.prepareFile(): " + ex.getMessage());
            GlobalVars.nwrWin.showMessage("Critical error!");
        }
    }

    private void updateList()
    {
        Player player = new Player(null, null);
        try {
            for (int i = 0; i < MaxList; i++) {
                String fn = NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_PLAYER, i);

                this.fFiles[i] = new GameFile();
                this.fFiles[i].Exist = (new java.io.File(fn)).isFile();
                if (this.fFiles[i].Exist) {
                    this.fFiles[i].SaveTime = new java.util.Date((new java.io.File(fn)).lastModified());

                    try {
                        NWGameSpace.loadPlayer(i, player);

                        this.fFiles[i].PlayerName = player.getName();

                        int fx = player.getField().X;
                        int fy = player.getField().Y;

                        LayerEntry layer = (LayerEntry) GlobalVars.nwrBase.getEntry(player.LayerID);
                        String landSign = layer.getFieldEntry(fx, fy).LandSign;

                        LandEntry land = (LandEntry) GlobalVars.nwrBase.findEntryBySign(landSign);
                        this.fFiles[i].LandName = land.getName();
                    } catch (Exception ex) {
                        Logger.write("FilesWindow.refreshList.PlayerLoad(" + fn + "): " + ex.getMessage());
                        this.fFiles[i].PlayerName = "<error>";
                        this.fFiles[i].LandName = "<error>";
                    }
                } else {
                    this.fFiles[i].PlayerName = Locale.getStr(RS.rs_PlayerUnknown);
                    this.fFiles[i].LandName = "-";
                    this.fFiles[i].SaveTime = new java.util.Date(0);
                }
            }
        } finally {
            player.dispose();
        }
    }

    @Override
    protected void DoClose()
    {
        if (GlobalVars.nwrWin.getMainScreen() == GameScreen.gsStartup) {
            GlobalVars.nwrWin.showStartupWin();
        }
    }

    @Override
    protected void doKeyPressEvent(KeyPressEventArgs eventArgs)
    {
        super.doKeyPressEvent(eventArgs);

        if ("0123456789".indexOf(eventArgs.Key) >= 0) {
            int i = (int) eventArgs.Key - 48;
            this.prepareFile(i);
        }
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        super.doMouseDownEvent(eventArgs);

        if (eventArgs.Button == MouseButton.mbLeft) {
            for (int i = 0; i <= 9; i++) {
                Rect r = this.getFileOpRect(i, MI_FILEDELETE);
                if (r.contains(eventArgs.X, eventArgs.Y) && this.fFiles[i].Exist) {
                    GlobalVars.nwrGame.eraseGame(i);
                    this.fFiles[i].Exist = false;
                    this.updateList();
                    break;
                }

                r = this.getFileOpRect(i, MI_FILENUM);
                if (r.contains(eventArgs.X, eventArgs.Y)) {
                    this.prepareFile(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);

        CtlCommon.smFont.setColor(BaseScreen.clGold);
        CtlCommon.bgFont.setColor(BaseScreen.clGold);

        screen.Font = CtlCommon.bgFont;

        switch (this.FilesMode) {
            case FWMODE_LOAD:
                screen.drawText(30, 22, Locale.getStr(RS.rs_GameLoad), 0);
                break;

            case FWMODE_SAVE:
                screen.drawText(30, 22, Locale.getStr(RS.rs_GameSave), 0);
                break;
        }

        screen.Font = CtlCommon.smFont;

        for (int i = 0; i < MaxList; i++) {
            Rect rd = this.getFileOpRect(i, MI_FILEDELETE);
            GlobalVars.nwrWin.Resources.drawImage(screen, rd.Left, rd.Top, ItfElement.id_FileDelete.ImageIndex, 255);
            Rect rn = this.getFileOpRect(i, MI_FILENUM);
            GlobalVars.nwrWin.Resources.drawImage(screen, rn.Left, rn.Top, ItfElement.id_FileNum.ImageIndex, 255);
            int col = BaseScreen.clNavy;
            screen.setTextColor(col, true);
            screen.drawText(rd.Left + 34, rd.Top + 2, String.valueOf(i), 0);
            screen.setTextColor(BaseScreen.clGold, true);
            screen.drawText(rd.Left + 54, rd.Top + 2, this.fFiles[i].PlayerName, 0);
            screen.drawText(rd.Left + 200, rd.Top + 2, this.fFiles[i].LandName, 0);

            String time;
            if (this.fFiles[i].SaveTime == new java.util.Date(0)) {
                time = "(?)";
            } else {
                SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
                time = "(" + ft.format(this.fFiles[i].SaveTime) + ")";
            }
            screen.drawText(rd.Left + 380, rd.Top + 2, time, 0);
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        this.updateList();
    }
}
