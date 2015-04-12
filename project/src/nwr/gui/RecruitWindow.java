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
import nwr.creatures.brain.LeaderBrain;
import nwr.creatures.NWCreature;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.ListBox;
import nwr.main.GlobalVars;
import nwr.player.Player;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class RecruitWindow extends DialogWindow
{
    private final ListBox fMercenariesList;
    private final NWButton fRecruitBtn;

    public RecruitWindow(BaseControl owner)
    {
        super(owner);

        this.fMercenariesList = new ListBox(this);
        fMercenariesList.setMode(ListBox.MODE_REPORT);
        fMercenariesList.setBounds(new Rect(10, 10, 589, 398));
        fMercenariesList.getColumnTitles().add("name", 400);
        fMercenariesList.getColumnTitles().add("cost", 150);

        this.fRecruitBtn = new NWButton(this);
        fRecruitBtn.OnClick = this::onBtnRecruit;
        fRecruitBtn.OnLangChange = GlobalVars.nwrWin::LangChange;
        fRecruitBtn.setLangResID(49);
        fRecruitBtn.setWidth(90);
        fRecruitBtn.setHeight(30);
        fRecruitBtn.setLeft(super.getWidth() - 210);
        fRecruitBtn.setTop(super.getHeight() - 30 - 20);
        fRecruitBtn.setImageFile("itf/DlgBtn.tga");
    }

    private boolean canRecruit()
    {
        Player player = GlobalVars.nwrGame.getPlayer();
        int membCount = ((LeaderBrain) player.getBrain()).getMembersCount() - 1;
        
        return (membCount < LeaderBrain.PartyMax);
    }
    
    private void onBtnRecruit(Object sender)
    {
        int idx = this.fMercenariesList.getSelIndex();
        if (idx >= 0 && idx < this.fMercenariesList.getItems().getCount() && canRecruit()) {
            NWCreature mercenary = (NWCreature) this.fMercenariesList.getItems().getItem(idx).Data;
            GlobalVars.nwrGame.getPlayer().recruitMercenary(this.fCollocutor, mercenary, true);
            this.updateView();
        }
    }

    private void updateView()
    {
        this.fMercenariesList.getItems().beginUpdate();
        this.fMercenariesList.getItems().clear();
        if (this.fCollocutor.CLSID == GlobalVars.cid_Jarl) {
            NWField fld = (NWField) this.fCollocutor.getCurrentMap();

            int num = fld.getCreatures().getCount();
            for (int i = 0; i < num; i++) {
                NWCreature j = fld.getCreatures().getItem(i);
                if (j.CLSID == GlobalVars.cid_Guardsman && !j.isMercenary()) {
                    this.addCandidate(j);
                }
            }
        } else {
            if (this.fCollocutor.CLSID == GlobalVars.cid_Merchant) {
                this.addCandidate(this.fCollocutor);
            }
        }
        this.fMercenariesList.getItems().endUpdate();
        this.fRecruitBtn.Enabled = (this.fMercenariesList.getItems().getCount() > 0);
    }

    private void addCandidate(NWCreature aCreature)
    {
        int hPrice = (int) aCreature.getHirePrice();
        LBItem item = this.fMercenariesList.getItems().add(aCreature.getName(), aCreature);
        item.getSubItems().add(String.valueOf(hPrice), null);
        if (GlobalVars.nwrGame.getPlayer().getMoney() >= hPrice && canRecruit()) {
            item.Color = BaseScreen.clGold;
        } else {
            item.Color = BaseScreen.clRed;
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        this.updateView();
    }
}
