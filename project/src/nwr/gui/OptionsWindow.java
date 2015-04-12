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

import jzrlib.core.Rect;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.types.GameScreen;
import nwr.core.types.UserAction;
import nwr.engine.BaseControl;
import nwr.engine.BaseMainWindow;
import nwr.engine.BaseScreen;
import nwr.engine.HotKey;
import nwr.engine.KeyEventArgs;
import nwr.engine.Keys;
import nwr.engine.MouseButton;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CheckBox;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.LBOptions;
import nwr.gui.controls.Label;
import nwr.gui.controls.ListBox;
import nwr.gui.controls.ScrollBar;
import nwr.gui.controls.TabControl;
import nwr.gui.controls.TabSheet;
import nwr.main.GlobalVars;
import nwr.main.NWMainWindow;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class OptionsWindow extends NWWindow
{
    private final CheckBox fAutoPickup;
    private final CheckBox fCircularFOV;
    private final CheckBox fExtremeMode;
    private final CheckBox fHideInfoPanel;
    private final CheckBox fHideCtlPanel;
    private final CheckBox fHideLocMap;
    private final CheckBox fInvOnlyIcons;
    private final ListBox fKeyList;
    private final ListBox fLangList;
    private final CheckBox fModernStyle;
    private final CheckBox fNewStyle;
    private final TabControl fPages;
    private final ScrollBar fSndVolume;
    private final ScrollBar fSngVolume;

    private boolean FKeyWait;

    private void onBtnClose(Object sender)
    {
        this.hide();
        this.DoClose();
    }

    private void onKeyListKeyDown(Object sender, KeyEventArgs eventArgs)
    {
        if (this.FKeyWait) {
            int idx = this.fKeyList.getSelIndex();
            if (idx >= 0 && idx < this.fKeyList.getItems().getCount()) {
                UserAction uAct = UserAction.forValue(idx);
                uAct.HotKey = new HotKey(eventArgs.Key, eventArgs.Shift);

                this.updateKeyList();

                this.fKeyList.setTopIndex(this.fKeyList.getTopIndex());
                this.fKeyList.setSelIndex(idx);
                this.FKeyWait = false;
                this.fKeyList.SelColor = BaseScreen.clGray;
                this.fKeyList.SelBorderColor = BaseScreen.clGray;
                eventArgs.Key = Keys.GK_UNK;
            }
        } else {
            if (eventArgs.Key == Keys.GK_RETURN) {
                this.FKeyWait = true;
                this.fKeyList.SelColor = BaseScreen.clNone;
                this.fKeyList.SelBorderColor = BaseScreen.clRed;
                eventArgs.Key = Keys.GK_UNK;
            }
        }
        this.fKeyList.ShowHints = !this.FKeyWait;
    }

    private void updateKeyList()
    {
        this.fKeyList.getItems().clear();

        for (int ua = UserAction.uaFirst; ua <= UserAction.uaLast; ua++) {
            UserAction uAct = UserAction.forValue(ua);

            LBItem item = this.fKeyList.getItems().add(Locale.getStr(uAct.NameRS), null);
            String s = BaseMainWindow.HotKeyToText(uAct.HotKey);
            item.getSubItems().add(s, null);
        }
    }

    private void onLangSelect(Object sender, MouseButton button, LBItem item)
    {
        GlobalVars.nwrWin.setLanguage(item.Text);
    }

    private void onAutoPickupClick(Object Sender)
    {
        GlobalVars.nwrWin.setAutoPickup(this.fAutoPickup.getChecked());
    }

    private void onCircularFOVClick(Object Sender)
    {
        GlobalVars.nwrWin.setCircularFOV(this.fCircularFOV.getChecked());
    }

    private void onExtremeModeClick(Object Sender)
    {
        GlobalVars.nwrWin.setExtremeMode(this.fExtremeMode.getChecked());
    }

    private void onHideCtlPanelClick(Object Sender)
    {
        GlobalVars.nwrWin.setHideCtlPanel(this.fHideCtlPanel.getChecked());
    }

    private void onHideInfoPanelClick(Object Sender)
    {
        GlobalVars.nwrWin.setHideInfoPanel(this.fHideInfoPanel.getChecked());
    }

    private void onHideLocMapClick(Object Sender)
    {
        GlobalVars.nwrWin.setHideLocMap(this.fHideLocMap.getChecked());
    }

    private void onInvOnlyIconsClick(Object Sender)
    {
        GlobalVars.nwrWin.setInventoryOnlyIcons(this.fInvOnlyIcons.getChecked());
    }

    private void onModernStyleClick(Object Sender)
    {
        if (this.fModernStyle.getChecked()) {
            GlobalVars.nwrWin.setStyle(NWMainWindow.RGS_MODERN);
        }
    }

    private void onNewStyleClick(Object Sender)
    {
        if (this.fNewStyle.getChecked()) {
            GlobalVars.nwrWin.setStyle(NWMainWindow.RGS_CLASSIC);
        }
    }

    private void onSongsVolumeChange(Object Sender)
    {
        GlobalVars.nwrWin.setSongsVolume(((ScrollBar) Sender).getPos());
    }

    private void onSoundsVolumeChange(Object Sender)
    {
        GlobalVars.nwrWin.setSoundsVolume(((ScrollBar) Sender).getPos());
    }

    @Override
    protected void DoClose()
    {
        if (GlobalVars.nwrWin.getMainScreen() == GameScreen.gsStartup) {
            GlobalVars.nwrWin.showStartupWin();
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        this.fSngVolume.setPos(GlobalVars.nwrWin.getSongsVolume());
        this.fSndVolume.setPos(GlobalVars.nwrWin.getSoundsVolume());
        this.fCircularFOV.setChecked(GlobalVars.nwrWin.getCircularFOV());
        this.fAutoPickup.setChecked(GlobalVars.nwrWin.getAutoPickup());
        this.fExtremeMode.setChecked(GlobalVars.nwrWin.getExtremeMode());

        int itfStyle = GlobalVars.nwrWin.getStyle();
        if (itfStyle != NWMainWindow.RGS_CLASSIC) {
            if (itfStyle == NWMainWindow.RGS_MODERN) {
                this.fModernStyle.setChecked(true);
            }
        } else {
            this.fNewStyle.setChecked(true);
        }

        this.fHideCtlPanel.setChecked(GlobalVars.nwrWin.getHideCtlPanel());
        this.fHideInfoPanel.setChecked(GlobalVars.nwrWin.getHideInfoPanel());
        this.fHideLocMap.setChecked(GlobalVars.nwrWin.getHideLocMap());
        this.fInvOnlyIcons.setChecked(GlobalVars.nwrWin.getInventoryOnlyIcons());

        this.updateKeyList();

        int idx = this.fLangList.getItems().indexOf(GlobalVars.nwrWin.getLanguage());
        if (idx >= 0) {
            this.fLangList.setSelIndex(idx);
            this.fLangList.getItems().getItem(idx).Checked = true;
        }
    }

    public OptionsWindow(BaseControl owner)
    {
        super(owner);

        super.setFont(CtlCommon.smFont);
        super.setWidth(480);
        super.setHeight(390);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        this.fPages = new TabControl(this);
        fPages.setLeft(10);
        fPages.setTop(10);
        fPages.setWidth(super.getWidth() - 20);
        fPages.setHeight(super.getHeight() - 30 - 40);

        TabSheet ts = fPages.addPage(Locale.getStr(RS.rs_CommonOptions));
        ts.OnLangChange = GlobalVars.nwrWin::LangChange;
        ts.setLangResID(9);

        this.fSngVolume = new ScrollBar(ts);
        fSngVolume.setMin(0);
        fSngVolume.setMax(255);
        fSngVolume.setPos(255);
        fSngVolume.OnChange = this::onSongsVolumeChange;
        fSngVolume.setKind(ScrollBar.SBK_HORIZONTAL);
        fSngVolume.setWidth(ts.getWidth() - 50);
        fSngVolume.setLeft(25);
        fSngVolume.setTop(30);

        Label label = new Label(ts);
        label.setBounds(new Rect(25, 30 - 20, 25 + fSngVolume.getWidth(), 30));
        label.setLangResID(RS.rs_MusicVolume);
        label.OnLangChange = GlobalVars.nwrWin::LangChange;

        this.fSndVolume = new ScrollBar(ts);
        fSndVolume.setMin(0);
        fSndVolume.setMax(255);
        fSndVolume.setPos(255);
        fSndVolume.OnChange = this::onSoundsVolumeChange;
        fSndVolume.setKind(ScrollBar.SBK_HORIZONTAL);
        fSndVolume.setWidth(ts.getWidth() - 50);
        fSndVolume.setLeft(25);
        fSndVolume.setTop(70);

        label = new Label(ts);
        label.setBounds(new Rect(25, 70 - 20, 25 + fSndVolume.getWidth(), 70));
        label.setLangResID(RS.rs_SoundsVolume);
        label.OnLangChange = GlobalVars.nwrWin::LangChange;

        this.fNewStyle = new CheckBox(ts);
        fNewStyle.OnLangChange = GlobalVars.nwrWin::LangChange;
        fNewStyle.setLangResID(12);
        fNewStyle.Group = 1;
        fNewStyle.setLeft(25);
        fNewStyle.setTop(100);
        fNewStyle.setWidth(super.getWidth() - 50);
        fNewStyle.OnClick = this::onNewStyleClick;

        this.fModernStyle = new CheckBox(ts);
        fModernStyle.OnLangChange = GlobalVars.nwrWin::LangChange;
        fModernStyle.setLangResID(13);
        fModernStyle.Group = 1;
        fModernStyle.setLeft(225);
        fModernStyle.setTop(100);
        fModernStyle.setWidth(super.getWidth() - 50);
        fModernStyle.OnClick = this::onModernStyleClick;

        this.fHideLocMap = new CheckBox(ts);
        fHideLocMap.OnLangChange = GlobalVars.nwrWin::LangChange;
        fHideLocMap.setLangResID(14);
        fHideLocMap.setLeft(25);
        fHideLocMap.setTop(130);
        fHideLocMap.setWidth(super.getWidth() - 50);
        fHideLocMap.OnClick = this::onHideLocMapClick;

        this.fHideCtlPanel = new CheckBox(ts);
        fHideCtlPanel.OnLangChange = GlobalVars.nwrWin::LangChange;
        fHideCtlPanel.setLangResID(15);
        fHideCtlPanel.setLeft(25);
        fHideCtlPanel.setTop(160);
        fHideCtlPanel.setWidth(super.getWidth() - 50);
        fHideCtlPanel.OnClick = this::onHideCtlPanelClick;

        this.fHideInfoPanel = new CheckBox(ts);
        fHideInfoPanel.OnLangChange = GlobalVars.nwrWin::LangChange;
        fHideInfoPanel.setLangResID(16);
        fHideInfoPanel.setLeft(25);
        fHideInfoPanel.setTop(190);
        fHideInfoPanel.setWidth(super.getWidth() - 50);
        fHideInfoPanel.OnClick = this::onHideInfoPanelClick;

        this.fInvOnlyIcons = new CheckBox(ts);
        fInvOnlyIcons.OnLangChange = GlobalVars.nwrWin::LangChange;
        fInvOnlyIcons.setLangResID(549);
        fInvOnlyIcons.setLeft(25);
        fInvOnlyIcons.setTop(220);
        fInvOnlyIcons.setWidth(super.getWidth() - 50);
        fInvOnlyIcons.OnClick = this::onInvOnlyIconsClick;

        ts = fPages.addPage(Locale.getStr(RS.rs_KeyOptions));
        ts.OnLangChange = GlobalVars.nwrWin::LangChange;
        ts.setLangResID(10);

        this.fKeyList = new ListBox(ts);
        fKeyList.setMode(ListBox.MODE_REPORT);
        fKeyList.setLeft(10);
        fKeyList.setTop(10);
        fKeyList.setWidth(ts.getWidth() - 20);
        fKeyList.setHeight(ts.getHeight() - 20);
        fKeyList.getColumnTitles().add("name", 300);
        fKeyList.getColumnTitles().add("key", 150);
        fKeyList.OnKeyDown = this::onKeyListKeyDown;
        fKeyList.ShowHints = true;
        fKeyList.Hint = "hint";

        ts = fPages.addPage(Locale.getStr(RS.rs_GameplayOptions));
        ts.OnLangChange = GlobalVars.nwrWin::LangChange;
        ts.setLangResID(11);

        this.fCircularFOV = new CheckBox(ts);
        fCircularFOV.OnLangChange = GlobalVars.nwrWin::LangChange;
        fCircularFOV.setLangResID(17);
        fCircularFOV.setLeft(25);
        fCircularFOV.setTop(25);
        fCircularFOV.setWidth(super.getWidth() - 50);
        fCircularFOV.OnClick = this::onCircularFOVClick;

        this.fAutoPickup = new CheckBox(ts);
        fAutoPickup.OnLangChange = GlobalVars.nwrWin::LangChange;
        fAutoPickup.setLangResID(536);
        fAutoPickup.setLeft(25);
        fAutoPickup.setTop(65);
        fAutoPickup.setWidth(super.getWidth() - 50);
        fAutoPickup.OnClick = this::onAutoPickupClick;

        this.fExtremeMode = new CheckBox(ts);
        fExtremeMode.OnLangChange = GlobalVars.nwrWin::LangChange;
        fExtremeMode.setLangResID(82);
        fExtremeMode.setLeft(25);
        fExtremeMode.setTop(105);
        fExtremeMode.setWidth(super.getWidth() - 50);
        fExtremeMode.OnClick = this::onExtremeModeClick;
        ts = fPages.addPage(Locale.getStr(RS.rs_Language));
        ts.OnLangChange = GlobalVars.nwrWin::LangChange;
        ts.setLangResID(815);

        this.fLangList = new ListBox(ts);
        fLangList.setMode(ListBox.MODE_LIST);
        fLangList.Options = new LBOptions(LBOptions.lboChecks, LBOptions.lboRadioChecks);
        fLangList.setLeft(10);
        fLangList.setTop(10);
        fLangList.setWidth(ts.getWidth() - 20);
        fLangList.setHeight(ts.getHeight() - 20);

        Locale locale = GlobalVars.nwrWin.getLocale();
        int num = locale.getLangsCount();
        for (int i = 0; i < num; i++) {
            fLangList.getItems().add(locale.getLang(i).Name, null);
        }
        fLangList.OnItemSelect = this::onLangSelect;

        fPages.setTabIndex(0);

        NWButton tRButton = new NWButton(this);
        tRButton.setWidth(90);
        tRButton.setHeight(30);
        tRButton.setLeft(super.getWidth() - 90 - 20);
        tRButton.setTop(super.getHeight() - 30 - 20);
        tRButton.setImageFile("itf/DlgBtn.tga");
        tRButton.OnClick = this::onBtnClose;
        tRButton.OnLangChange = GlobalVars.nwrWin::LangChange;
        tRButton.setLangResID(8);
    }

    @Override
    public void changeLang()
    {
        super.changeLang();

        this.fKeyList.Hint = Locale.getStr(RS.rs_HKHint);
        this.updateKeyList();
    }
}
