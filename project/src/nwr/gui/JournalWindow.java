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
import jzrlib.utils.RefObject;
import jzrlib.core.StringList;
import nwr.core.StaticData;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.TextBox;
import nwr.game.NWGameSpace;
import nwr.game.story.Journal;
import nwr.game.story.JournalItem;
import nwr.game.story.Quest;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class JournalWindow extends NWWindow
{
    private static class JTextBox extends TextBox
    {
        public JTextBox(BaseControl owner)
        {
            super(owner);
        }

        @Override
        protected void doPaintLine(BaseScreen screen, int index, Rect rect)
        {
            JournalItem item = (JournalItem) super.fLines.getObject(index);
            if (item != null) {
                super.TextColor = item.Color;
            }
            super.doPaintLine(screen, index, rect);
        }
    }
    
    private final JTextBox fText;

    public JournalWindow(BaseControl owner)
    {
        super(owner);

        super.setFont(CtlCommon.smFont);
        super.setWidth(600);
        super.setHeight(460);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);
        super.Shifted = true;
        super.BackDraw = false;

        this.fText = new JTextBox(this);
        this.fText.setFont(CtlCommon.smFont);
        this.fText.setLeft(10);
        this.fText.setTop(10);
        this.fText.setWidth(580);
        this.fText.setHeight(440);
        this.fText.setVisible(true);
        //this.fText.setLinks(true);
        this.fText.OnGetVariable = this::onGetVar;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fText.dispose();
        }
        super.dispose(disposing);
    }

    private void onGetVar(Object sender, RefObject<String> refVar)
    {
        if (refVar.argValue.compareTo("ver") == 0) {
            refVar.argValue = StaticData.rs_GameVersion;
            refVar.argValue = refVar.argValue.substring(0 + 1);
        } else {
            if (refVar.argValue.compareTo("dev_time") == 0) {
                refVar.argValue = StaticData.rs_GameDevTime;
            }
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        NWGameSpace space = (NWGameSpace) NWGameSpace.getInstance();
        Journal journal = space.getJournal();
        StringList lines = this.fText.getLines();

        lines.clear();
        lines.beginUpdate();

        int type = -1;
        for (int i = 0; i < journal.getCount(); i++) {
            JournalItem item = journal.getItem(i);
            
            if (type != item.Type) {
                lines.add("");
                type = item.Type;
            }
            
            lines.addObject(item.Text, item);
        }

        lines.add("");
        lines.addObject("    " + "Квесты:", new JournalItem(JournalItem.SIT_QUESTS));
        int num = space.getQuestsCount();
        for (int i = 0; i < num; i++) {
            Quest quest = space.getQuest(i);
            int stt = (quest.getIsComplete() ? JournalItem.SIT_QUEST_Y : JournalItem.SIT_QUEST_N);
            lines.addObject("  --> " + quest.getDescription(), new JournalItem(stt));
        }

        journal.generateStats(lines);

        lines.endUpdate();

        super.setActiveControl(this.fText);
    }
}
