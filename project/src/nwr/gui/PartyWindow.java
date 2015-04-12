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
import jzrlib.core.StringList;
import nwr.creatures.brain.LeaderBrain;
import nwr.creatures.brain.PartyFormation;
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.LBItemList;
import nwr.gui.controls.LBOptions;
import nwr.gui.controls.ListBox;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class PartyWindow extends NWWindow
{
    private final ListBox fFormationList;
    private final ListBox fMercenariesList;
    private final ListBox fMercenaryPropsList;

    public PartyWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(600);
        super.setHeight(460);
        super.WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview);
        super.Shifted = true;

        this.fFormationList = new ListBox(this);
        this.fFormationList.setMode(ListBox.MODE_LIST);
        this.fFormationList.setBounds(new Rect(10, 28, 290, 157));
        this.fFormationList.OnItemSelect = this::onFormationSelect;
        this.fFormationList.Options = new LBOptions(LBOptions.lboChecks, LBOptions.lboRadioChecks);

        this.fMercenariesList = new ListBox(this);
        this.fMercenariesList.setMode(ListBox.MODE_LIST);
        this.fMercenariesList.setBounds(new Rect(10, 179, 290, 398)); // 370
        this.fMercenariesList.OnItemSelect = this::onMercenarySelect;

        this.fMercenaryPropsList = new ListBox(this);
        this.fMercenaryPropsList.setMode(ListBox.MODE_LIST);
        this.fMercenaryPropsList.setBounds(new Rect(309, 28, 589, 398));

        NWButton btn = new NWButton(this);
        btn.setWidth(90);
        btn.setHeight(30);
        btn.setLeft(super.getWidth() - 90 - 20);
        btn.setTop(super.getHeight() - 30 - 20);
        btn.setImageFile("itf/DlgBtn.tga");
        btn.OnClick = this::onBtnClose;
        btn.OnLangChange = GlobalVars.nwrWin::LangChange;
        btn.setLangResID(8);

        btn = new NWButton(this);
        btn.setWidth(90);
        btn.setHeight(30);
        btn.setLeft(20);
        btn.setTop(super.getHeight() - 30 - 20);
        btn.setImageFile("itf/DlgBtn.tga");
        btn.OnClick = this::onBtnExchange;
        btn.OnLangChange = GlobalVars.nwrWin::LangChange;
        btn.setLangResID(RS.rs_Exchange);

        btn = new NWButton(this);
        btn.setWidth(90);
        btn.setHeight(30);
        btn.setLeft(20 + 90 + 20);
        btn.setTop(super.getHeight() - 30 - 20);
        btn.setImageFile("itf/DlgBtn.tga");
        btn.OnClick = this::onBtnTeach;
        btn.OnLangChange = GlobalVars.nwrWin::LangChange;
        btn.setLangResID(RS.rs_Teach);
    }

    private void onBtnClose(Object sender)
    {
        this.hide();
    }

    private void onBtnTeach(Object sender)
    {
        int idx = this.fMercenariesList.getSelIndex();
        LeaderBrain party = (LeaderBrain) GlobalVars.nwrGame.getPlayer().getBrain();
        NWCreature member = (NWCreature) party.getMember(idx + 1);

        if (member != null) {
            GlobalVars.nwrWin.showTeachWin(member);
        }
    }

    private void onBtnExchange(Object sender)
    {
        int idx = this.fMercenariesList.getSelIndex();
        LeaderBrain party = (LeaderBrain) GlobalVars.nwrGame.getPlayer().getBrain();
        NWCreature member = (NWCreature) party.getMember(idx + 1);

        if (member != null) {
            GlobalVars.nwrWin.showExchangeWin(member);
        }
    }

    private void onFormationSelect(Object sender, MouseButton button, LBItem item)
    {
        GlobalVars.nwrGame.getPlayer().setPartyFormation(PartyFormation.forValue(item.AbsoluteIndex));
    }

    private void onMercenarySelect(Object sender, MouseButton button, LBItem item)
    {
        this.fMercenaryPropsList.getItems().clear();

        if (item != null) {
            NWCreature merc = (NWCreature) item.Data;
            LBItemList items = this.fMercenaryPropsList.getItems();
            
            StringList props = merc.getProps();
            for (int i = 0; i < props.getCount(); i++) {
                items.add(props.get(i), null);
            }
        }
    }

    private void updateView()
    {
        this.fMercenariesList.getItems().beginUpdate();
        this.fMercenariesList.getItems().clear();

        LeaderBrain party = (LeaderBrain) GlobalVars.nwrGame.getPlayer().getBrain();

        int num = party.getMembersCount();
        for (int i = 1; i < num; i++) {
            NWCreature j = (NWCreature) party.getMember(i);
            this.fMercenariesList.getItems().add(j.getName(), j);
        }

        this.fMercenariesList.getItems().endUpdate();
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);

        screen.setTextColor(BaseScreen.clGold, true);

        Rect r1 = this.fFormationList.getBounds();
        int ml = r1.Left + (r1.getWidth() - CtlCommon.smFont.getTextWidth(Locale.getStr(RS.rs_PartyFormation))) / 2;
        screen.drawText(ml, 8, Locale.getStr(RS.rs_PartyFormation), 0);

        Rect r2 = this.fMercenariesList.getBounds();
        ml = r2.Left + (r2.getWidth() - CtlCommon.smFont.getTextWidth(Locale.getStr(RS.rs_Mercenaries))) / 2;
        screen.drawText(ml, r1.Bottom + 2, Locale.getStr(RS.rs_Mercenaries), 0);
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        this.fFormationList.getItems().beginUpdate();
        this.fFormationList.getItems().clear();

        for (int pf = PartyFormation.pfFirst; pf <= PartyFormation.pfLast; pf++) {
            this.fFormationList.getItems().add(Locale.getStr(LeaderBrain.PartyFormationsRS[pf]), null);
        }

        this.fFormationList.getItems().endUpdate();
        int idx = ((LeaderBrain) GlobalVars.nwrGame.getPlayer().getBrain()).Formation.getValue();
        this.fFormationList.setSelIndex(idx);
        this.fFormationList.getItems().getItem(idx).Checked = true;
        this.updateView();
    }
}
