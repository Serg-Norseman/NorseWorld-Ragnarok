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
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.StaticData;
import nwr.core.types.AbilityID;
import nwr.core.types.SkillID;
import nwr.core.types.TeachableKind;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.LBOptions;
import nwr.gui.controls.ListBox;
import nwr.main.GlobalVars;
import nwr.player.Player;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class TeachWindow extends DialogWindow
{
    private static final class TeachItem
    {
        public int Index;
        public int CurLev;
        public int Price;
        
        public TeachItem(int index, int curLev, int price)
        {
            this.Index = index;
            this.CurLev = curLev;
            this.Price = price;
        }
    }
    
    private final ListBox fDisciplinesList;

    public TeachWindow(BaseControl owner)
    {
        super(owner);
        this.fDisciplinesList = new ListBox(this);
        this.fDisciplinesList.setMode(ListBox.MODE_REPORT);
        this.fDisciplinesList.setColumns(2);

        this.fDisciplinesList.setBounds(new Rect(10, 10, 589, 398));

        this.fDisciplinesList.setItemHeight(34);
        this.fDisciplinesList.setIconHeight(30);
        this.fDisciplinesList.setIconWidth(32);
        this.fDisciplinesList.Options.include(LBOptions.lboIcons);
        this.fDisciplinesList.getColumnTitles().add("", 460);
        this.fDisciplinesList.getColumnTitles().add("", 72);
        this.fDisciplinesList.setVisible(true);
        this.fDisciplinesList.setColumns(1);
        this.fDisciplinesList.OnItemSelect = this::onDisciplineSelect;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fDisciplinesList.dispose();
        }
        super.dispose(disposing);
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        this.fDisciplinesList.ImagesList = GlobalVars.nwrWin.Resources;
        this.fDisciplinesList.setIconHeight(30);
        this.fDisciplinesList.setIconWidth(32);
        this.updateView();
    }

    private void onDisciplineSelect(Object sender, MouseButton button, LBItem item)
    {
        if (button == MouseButton.mbRight) {
            TeachItem ti = (TeachItem) item.Data;
            if (ti.Price <= GlobalVars.nwrGame.getPlayer().getMoney()) {
                GlobalVars.nwrGame.getPlayer().teachDiscipline(this.fCollocutor, ti.Index, ti.CurLev);
                this.updateView();
            }
        }
    }

    private void updateView()
    {
        this.fDisciplinesList.getItems().beginUpdate();
        this.fDisciplinesList.getItems().clear();
        NWCreature clt = this.fCollocutor;
        Player p = GlobalVars.nwrGame.getPlayer();

        for (int i = 0; i < StaticData.dbTeachable.length; i++) {
            int id = StaticData.dbTeachable[i].id;
            boolean res = false;
            TeachableKind kind = StaticData.dbTeachable[i].kind;

            String s = "";
            int imageIndex = -1;
            int curLev = 0;
            switch (kind) {
                case Ability:
                    AbilityID ab = AbilityID.forValue(id);
                    if (clt.getAbility(ab) > 0) {
                        s = Locale.getStr(ab.NameRS);
                        imageIndex = -1;
                        curLev = p.getAbility(ab);
                        res = (curLev < clt.getAbility(ab));
                    }
                    break;

                case Skill:
                    SkillID sk = SkillID.forValue(id);
                    if (clt.getSkill(sk) >= 0) {
                        s = Locale.getStr(sk.NameRS);
                        imageIndex = sk.ImageIndex;
                        curLev = p.getSkill(sk);
                        res = (curLev < clt.getSkill(sk));
                    }
                    break;
            }

            int price = (int) GlobalVars.nwrGame.getTeachablePrice(i, curLev);

            if (res) {
                String st = " ( " + String.valueOf(curLev) + " -> " + String.valueOf(curLev + 1) + " )";
                LBItem listItem = this.fDisciplinesList.getItems().add(s + st, new TeachItem(i, curLev, price));
                if (price > p.getMoney()) {
                    listItem.Color = BaseScreen.clRed;
                } else {
                    listItem.Color = BaseScreen.clGold;
                }
                listItem.ImageIndex = imageIndex;
                listItem.getSubItems().add(String.valueOf(price), null);
            }
        }

        this.fDisciplinesList.getItems().endUpdate();
    }
}
