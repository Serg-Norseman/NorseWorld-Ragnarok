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

using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Creatures.Brain;
using NWR.Database;
using NWR.Game;
using NWR.Game.Story;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    using Services = EnumSet<Service>;

    public sealed class NPCWindow : DialogWindow
    {
        private readonly ListBox fServices;
        private readonly ListBox fConversations;
        private readonly TextBox fTextBox;
        private TopicEntry fCurTopic;

        public NPCWindow(BaseControl owner)
            : base(owner)
        {
            fServices = new ListBox(this);
            fServices.Bounds = ExtRect.Create(429, 50, 589, 140); // w=160, h=90
            fServices.OnItemSelect = OnServiceSelect;

            fConversations = new ListBox(this);
            fConversations.Bounds = ExtRect.Create(429, 144, 589, 398);
            fConversations.OnItemSelect = OnConversationSelect;

            fTextBox = new TextBox(this);
            fTextBox.Bounds = ExtRect.Create(10, 50, 425, 398);
            fTextBox.Links = true;
            fTextBox.OnGetVariable = OnGetVar;
            fTextBox.OnLinkClick = OnLinkClick;
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();

            fServices.Items.BeginUpdate();
            fServices.Items.Clear();

            SentientBrain colBrain = (SentientBrain)fCollocutor.Brain;
            Services avail = colBrain.AvailableServices;
            for (var si = Service.ds_First; si <= Service.ds_Last; si++) {
                Service srv = (Service)(si);
                if (avail.Contains(srv)) {
                    fServices.Items.Add(BaseLocale.GetStr(StaticData.dbDialogServices[(int)srv].NameRes), srv);
                }
            }

            fServices.Items.EndUpdate();

            UpdateConversations();

            GlobalVars.nwrWin.SetScriptVar("NPC", fCollocutor);
            GlobalVars.nwrWin.SetScriptVar("dialog", this);

            GlobalVars.nwrWin.DoEvent(EventID.event_DialogBegin, GlobalVars.nwrGame.Player, fCollocutor, null);
        }

        protected override void DoHideEvent()
        {
            base.DoHideEvent();

            GlobalVars.nwrWin.SetScriptVar("NPC", null);
            GlobalVars.nwrWin.SetScriptVar("dialog", null);

            GlobalVars.nwrWin.DoEvent(EventID.event_DialogEnd, GlobalVars.nwrGame.Player, fCollocutor, null);
        }

        private void OnGetVar(object sender, ref string  @var)
        {
            if (@var.CompareTo("player") == 0) {
                @var = GlobalVars.nwrGame.Player.Name;
            } else {
                if (@var.CompareTo("self") == 0) {
                    @var = fCollocutor.Name;
                }
            }
        }

        private void OnServiceSelect(object sender, MouseButton button, LBItem item)
        {
            NWCreature p = GlobalVars.nwrGame.Player;

            Service serv = ((Service)item.Data);
            string qst = BaseLocale.GetStr(StaticData.dbDialogServices[(int)serv].QuestionRes);

            switch (serv) {
                case Service.ds_Teach:
                    if (!fCollocutor.CanTeach(p)) {
                        Message = qst;
                        Message = BaseLocale.GetStr(RS.rs_NothingToTeach);
                    } else {
                        GlobalVars.nwrWin.ShowTeachWin(fCollocutor);
                    }
                    break;

                case Service.ds_Trade:
                    if (!fCollocutor.IsTrader && !fCollocutor.Mercenary) {
                        Message = qst;
                        Message = BaseLocale.GetStr(RS.rs_NotTrader);
                    } else {
                        GlobalVars.nwrWin.ShowInventory(fCollocutor);
                    }
                    break;

                case Service.ds_Exchange:
                    if (!fCollocutor.Mercenary) {
                        Message = qst;
                        Message = BaseLocale.GetStr(RS.rs_NothingExchange);
                    } else {
                        GlobalVars.nwrWin.ShowExchangeWin(fCollocutor);
                    }
                    break;

                case Service.ds_Recruit:
                    if (fCollocutor.Mercenary) {
                        Message = qst;
                        Message = BaseLocale.GetStr(RS.rs_AlreadyHired);
                    } else {
                        if (fCollocutor.CLSID_Renamed == GlobalVars.cid_Guardsman) {
                            Message = qst;
                            Message = BaseLocale.GetStr(RS.rs_DoNotHired);
                        } else {
                            GlobalVars.nwrWin.ShowRecruit(fCollocutor);
                        }
                    }
                    break;
            }
        }

        private string Message
        {
            set {
                fTextBox.Lines.Text = value;
            }
        }

        private void AddText(string text)
        {
            string resText = "   - " + text;
            fTextBox.Lines.Add(resText);
            GlobalVars.nwrGame.Journal.StoreMessage(JournalItem.SIT_DIALOG, resText);
        }

        private void UpdateConversations()
        {
            fConversations.Items.BeginUpdate();
            fConversations.Items.Clear();

            SentientBrain b = (SentientBrain)fCollocutor.Brain;
            DialogEntry dlg = b.Dialog;

            int num = dlg.ConversationsCount;
            for (int i = 0; i < num; i++) {
                fConversations.Items.Add(dlg.GetConversation(i).Name, null);
            }

            fConversations.Items.EndUpdate();

            fTextBox.Lines.Text = "";
        }

        private void ShowTopic(TopicEntry topic, bool isRoot)
        {
            if (topic == null) {
                AddText(BaseLocale.GetStr(RS.rs_NothingToSay));
            } else {
                if (isRoot) {
                    string qst = topic.Phrase;
                    if (!string.IsNullOrEmpty(qst)) {
                        AddText(qst);
                    }
                }

                if (CheckScript(topic.Action) && !string.IsNullOrEmpty(topic.Answer)) {
                    AddText(topic.Answer);

                    for (int i = 0; i < topic.TopicsCount; i++) {
                        TopicEntry subTopic = topic.GetTopic(i);

                        if (CheckScript(subTopic.Condition)) {
                            AddText("@" + subTopic.Phrase + "@ ");
                        }
                    }
                }
            }
        }

        private bool CheckScript(string script)
        {
            bool result = true;

            if (!string.IsNullOrEmpty(script)) {
                object res = GlobalVars.nwrWin.ExecuteScript(script);
                if (res != null) {
                    result = (res.Equals(true));
                }
            }

            return result;
        }

        private void OnConversationSelect(object sender, MouseButton button, LBItem item)
        {
            fTextBox.Lines.Clear();

            int idx = fConversations.SelIndex;

            SentientBrain colBrain = (SentientBrain)fCollocutor.Brain;
            fCurTopic = colBrain.GetTopic(GlobalVars.nwrGame.Player, idx, null, "");
            ShowTopic(fCurTopic, true);
        }

        private void OnLinkClick(object sender, string linkValue)
        {
            int idx = fConversations.SelIndex;

            if (idx != -1) {
                SentientBrain b = (SentientBrain)fCollocutor.Brain;
                fCurTopic = b.GetTopic(GlobalVars.nwrGame.Player, idx, fCurTopic, linkValue);
                ShowTopic(fCurTopic, false);
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);

            ExtRect crt = ClientRect;
            ExtRect cr = new ExtRect();
            cr.Left = crt.Left + 10;
            cr.Right = crt.Right - 10;
            cr.Top = crt.Top + 10;
            cr.Bottom = cr.Top + 36 - 1;
            CtlCommon.DrawCtlBorder(screen, cr);
            crt = ExtRect.Create(cr.Left + 2, cr.Top + 2, cr.Left + 2 + 32 - 1, cr.Top + 2 + 32 - 1);
            screen.FillRect(crt, Colors.White);
            GlobalVars.nwrWin.Resources.DrawImage(screen, cr.Left + 2, cr.Top + 3, fCollocutor.Entry.ImageIndex, 255);
            screen.DrawText(crt.Right + 10, cr.Top + (36 - screen.GetTextHeight("A")) / 2, fCollocutor.Name, 0);
        }
    }

}