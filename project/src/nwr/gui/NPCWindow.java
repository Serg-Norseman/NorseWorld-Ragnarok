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
import jzrlib.utils.TextUtils;
import nwr.creatures.brain.SentientBrain;
import nwr.core.types.Services;
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.types.EventID;
import nwr.core.types.Service;
import nwr.database.DialogEntry;
import nwr.database.TopicEntry;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.ListBox;
import nwr.gui.controls.TextBox;
import nwr.game.story.JournalItem;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class NPCWindow extends DialogWindow
{
    private final ListBox fServices;
    private final ListBox fConversations;
    private final TextBox fTextBox;
    private TopicEntry fCurTopic;

    public NPCWindow(BaseControl owner)
    {
        super(owner);

        this.fServices = new ListBox(this);
        this.fServices.setBounds(new Rect(429, 50, 589, 140)); // w=160, h=90
        this.fServices.OnItemSelect = this::onServiceSelect;

        this.fConversations = new ListBox(this);
        this.fConversations.setBounds(new Rect(429, 144, 589, 398));
        this.fConversations.OnItemSelect = this::onConversationSelect;

        this.fTextBox = new TextBox(this);
        this.fTextBox.setBounds(new Rect(10, 50, 425, 398));
        this.fTextBox.setLinks(true);
        this.fTextBox.OnGetVariable = this::onGetVar;
        this.fTextBox.OnLinkClick = this::onLinkClick;
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        this.fServices.getItems().beginUpdate();
        this.fServices.getItems().clear();

        SentientBrain colBrain = (SentientBrain) this.fCollocutor.getBrain();
        Services avail = colBrain.getAvailableServices();
        for (int si = Service.ds_First; si <= Service.ds_Last; si++) {
            Service srv = Service.forValue(si);
            if (avail.contains(srv)) {
                this.fServices.getItems().add(Locale.getStr(srv.NameRS), srv);
            }
        }

        this.fServices.getItems().endUpdate();

        this.updateConversations();

        GlobalVars.nwrWin.setScriptVar("NPC", this.fCollocutor);
        GlobalVars.nwrWin.setScriptVar("dialog", this);

        GlobalVars.nwrWin.doEvent(EventID.event_DialogBegin, GlobalVars.nwrGame.getPlayer(), this.fCollocutor, null);
    }

    @Override
    protected void doHideEvent()
    {
        super.doHideEvent();

        GlobalVars.nwrWin.setScriptVar("NPC", null);
        GlobalVars.nwrWin.setScriptVar("dialog", null);

        GlobalVars.nwrWin.doEvent(EventID.event_DialogEnd, GlobalVars.nwrGame.getPlayer(), this.fCollocutor, null);
    }

    private void onGetVar(Object sender, RefObject<String> var)
    {
        if (var.argValue.compareTo("player") == 0) {
            var.argValue = GlobalVars.nwrGame.getPlayer().getName();
        } else {
            if (var.argValue.compareTo("self") == 0) {
                var.argValue = this.fCollocutor.getName();
            }
        }
    }

    private void onServiceSelect(Object sender, MouseButton button, LBItem item)
    {
        NWCreature p = GlobalVars.nwrGame.getPlayer();

        Service serv = ((Service) item.Data);
        String qst = Locale.getStr(serv.QuestionRS);

        switch (serv) {
            case ds_Teach:
                if (!this.fCollocutor.canTeach(p)) {
                    this.setMessage(qst);
                    this.setMessage(Locale.getStr(RS.rs_NothingToTeach));
                } else {
                    GlobalVars.nwrWin.showTeachWin(this.fCollocutor);
                }
                break;

            case ds_Trade:
                if (!this.fCollocutor.getIsTrader() && !this.fCollocutor.isMercenary()) {
                    this.setMessage(qst);
                    this.setMessage(Locale.getStr(RS.rs_NotTrader));
                } else {
                    GlobalVars.nwrWin.showInventory(this.fCollocutor);
                }
                break;

            case ds_Exchange:
                if (!this.fCollocutor.isMercenary()) {
                    this.setMessage(qst);
                    this.setMessage(Locale.getStr(RS.rs_NothingExchange));
                } else {
                    GlobalVars.nwrWin.showExchangeWin(this.fCollocutor);
                }
                break;

            case ds_Recruit:
                if (this.fCollocutor.isMercenary()) {
                    this.setMessage(qst);
                    this.setMessage(Locale.getStr(RS.rs_AlreadyHired));
                } else {
                    if (this.fCollocutor.CLSID == GlobalVars.cid_Guardsman) {
                        this.setMessage(qst);
                        this.setMessage(Locale.getStr(RS.rs_DoNotHired));
                    } else {
                        GlobalVars.nwrWin.showRecruit(this.fCollocutor);
                    }
                }
                break;
        }
    }

    private void setMessage(String msg)
    {
        this.fTextBox.getLines().setTextStr(msg);
    }

    private void addText(String text)
    {
        String resText = "   - " + text;
        this.fTextBox.getLines().add(resText);
        GlobalVars.nwrGame.getJournal().storeMessage(JournalItem.SIT_DIALOG, resText);
    }

    private void updateConversations()
    {
        this.fConversations.getItems().beginUpdate();
        this.fConversations.getItems().clear();

        SentientBrain b = (SentientBrain) this.fCollocutor.getBrain();
        DialogEntry dlg = b.getDialog();

        int num = dlg.getConversationsCount();
        for (int i = 0; i < num; i++) {
            this.fConversations.getItems().add(dlg.getConversation(i).Name, null);
        }

        this.fConversations.getItems().endUpdate();

        this.fTextBox.getLines().setTextStr("");
    }

    private void showTopic(TopicEntry topic, boolean isRoot)
    {
        if (topic == null) {
            this.addText(Locale.getStr(RS.rs_NothingToSay));
        } else {
            if (isRoot) {
                String qst = topic.Phrase;
                if (!TextUtils.isNullOrEmpty(qst)) {
                    this.addText(qst);
                }
            }

            if (this.checkScript(topic.Action) && !TextUtils.isNullOrEmpty(topic.Answer)) {
                this.addText(topic.Answer);

                for (int i = 0; i < topic.getTopicsCount(); i++) {
                    TopicEntry subTopic = topic.getTopic(i);

                    if (this.checkScript(subTopic.Condition)) {
                        this.addText("@" + subTopic.Phrase + "@ ");
                    }
                }
            }
        }
    }

    private boolean checkScript(String script)
    {
        boolean result = true;

        if (!TextUtils.isNullOrEmpty(script)) {
            Object res = GlobalVars.nwrWin.executeScript(script);
            if (res != null) {
                result = (res.equals(true));
            }
        }

        return result;
    }

    private void onConversationSelect(Object sender, MouseButton button, LBItem item)
    {
        this.fTextBox.getLines().clear();

        int idx = this.fConversations.getSelIndex();

        SentientBrain colBrain = (SentientBrain) this.fCollocutor.getBrain();
        this.fCurTopic = colBrain.getTopic(GlobalVars.nwrGame.getPlayer(), idx, null, "");
        this.showTopic(this.fCurTopic, true);
    }

    private void onLinkClick(Object sender, String linkValue)
    {
        int idx = this.fConversations.getSelIndex();

        if (idx != -1) {
            SentientBrain b = (SentientBrain) this.fCollocutor.getBrain();
            this.fCurTopic = b.getTopic(GlobalVars.nwrGame.getPlayer(), idx, this.fCurTopic, linkValue);
            this.showTopic(this.fCurTopic, false);
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);

        Rect crt = super.getClientRect();
        Rect cr = new Rect();
        cr.Left = crt.Left + 10;
        cr.Right = crt.Right - 10;
        cr.Top = crt.Top + 10;
        cr.Bottom = cr.Top + 36 - 1;
        CtlCommon.drawCtlBorder(screen, cr);
        crt = new Rect(cr.Left + 2, cr.Top + 2, cr.Left + 2 + 32 - 1, cr.Top + 2 + 32 - 1);
        screen.fillRect(crt, BaseScreen.clWhite);
        GlobalVars.nwrWin.Resources.drawImage(screen, cr.Left + 2, cr.Top + 3, this.fCollocutor.getEntry().ImageIndex, 255);
        screen.drawText(crt.Right + 10, cr.Top + (36 - screen.getTextHeight("A")) / 2, this.fCollocutor.getName(), 0);
    }
}
