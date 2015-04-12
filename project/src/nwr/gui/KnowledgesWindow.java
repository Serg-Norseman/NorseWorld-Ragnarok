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

import jzrlib.utils.AuxUtils;
import jzrlib.core.Rect;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.ListBox;
import nwr.gui.controls.TextBox;
import nwr.main.GlobalVars;
import nwr.player.MemoryEntry;
import java.util.Map;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class KnowledgesWindow extends NWWindow
{
    private final ListBox fList;
    private final TextBox fText;
    private boolean fReady;

    public KnowledgesWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(600);
        super.setHeight(460);
        super.WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview, WindowStyles.wsScreenCenter);
        super.Shifted = true;
        super.BackDraw = false;

        this.fReady = false;

        this.fList = new ListBox(this);
        this.fList.setBounds(new Rect(10, 10, 589, 199));
        this.fList.OnItemSelect = this::Knowledges_Select;
        this.fList.setColumns(2);
        this.fList.setVisible(true);
        this.fList.getItems().setSorted(true);

        this.fText = new TextBox(this);
        this.fText.setBounds(new Rect(10, 202, 589, 449));
        this.fText.setVisible(true);
        this.fText.setLinks(true);
        this.fText.OnLinkClick = this::onLinkClick;
        this.fText.TextColor = BaseScreen.clGold;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fList.dispose();
            this.fText.dispose();
        }
        super.dispose(disposing);
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        super.setActiveControl(this.fList);

        if (!this.fReady) {
            this.updateView();
        }
    }

    private void Knowledges_Select(Object sender, MouseButton button, LBItem item)
    {
        this.fText.getLines().beginUpdate();
        if (item.Data == null) {
            this.fText.getLines().setTextStr("");
        } else {
            MemoryEntry K = (MemoryEntry) item.Data;
            String desc = K.getDesc().replace("|", AuxUtils.CRLF);
            this.fText.getLines().clear();
            this.fText.getLines().add(K.getName());
            this.fText.getLines().add("");
            this.fText.getLines().add(desc);
        }
        this.fText.getLines().endUpdate();
    }

    private void onLinkClick(Object sender, String aLinkValue)
    {
        this.select(aLinkValue);
    }

    private void updateView()
    {
        this.fList.getItems().beginUpdate();
        this.fList.getItems().clear();
        this.fList.setMode(ListBox.MODE_LIST);

        for (Map.Entry<String, MemoryEntry> entry : GlobalVars.nwrGame.getPlayer().getMemory().getData().entrySet()) {
            MemoryEntry mem = entry.getValue();

            String txt = mem.getName();
            if (!txt.equals("")) {
                this.fList.getItems().add(txt, mem);
            }
        }

        this.fList.getItems().endUpdate();
    }

    public final void select(String name)
    {
        if (!super.getVisible()) {
            this.updateView();
        }

        int idx = this.fList.getItems().absoluteIndexOf(name);

        if (idx >= 0) {
            this.fList.setSelIndex(idx);
            this.Knowledges_Select(this.fList, MouseButton.mbLeft, this.fList.getAbsoluteItem(idx));
            this.fReady = true;
            this.show();
            this.fReady = false;
        }
    }
}
