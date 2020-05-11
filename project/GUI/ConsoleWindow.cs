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
using BSLib;
using NWR.Creatures;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.GUI.Controls;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Engine;
using ZRLib.Grammar;

namespace NWR.GUI
{
    public sealed class ConsoleWindow : NWWindow
    {
        private readonly StringList fCmdHistory;
        private readonly EditBox fEditBox;
        private readonly TextBox fTextBox;
        private int fCmdIndex;

        public ConsoleWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Left = 120;
            Top = 90;
            Width = 480;
            Height = 300;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

            fCmdHistory = new StringList();
            fCmdIndex = -1;

            NWButton btn = new NWButton(this);
            btn.Width = 90;
            btn.Height = 30;
            btn.Left = Width - 90 - 20;
            btn.Top = Height - 30 - 20;
            btn.ImageFile = "itf/DlgBtn.tga";
            btn.OnClick = OnBtnClose;
            btn.OnLangChange = GlobalVars.nwrWin.LangChange;
            btn.LangResID = 8;

            fEditBox = new EditBox(this);
            fEditBox.Left = 20;
            fEditBox.Top = 20;
            fEditBox.Width = Width - 40;
            fEditBox.OnKeyDown = OnEditKeyDown;

            fTextBox = new TextBox(this);
            fTextBox.Left = 20;
            fTextBox.Top = 20 + fEditBox.Height + 10;
            fTextBox.Width = Width - 40;
            fTextBox.Height = Height - 100 - fEditBox.Height;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fCmdHistory.Dispose();
            }
            base.Dispose(disposing);
        }

        private void OnBtnClose(object sender)
        {
            Hide();
        }

        private void DoCommand(string cmd)
        {
            try {
                int tokCount = (int)AuxUtils.GetTokensCount(cmd, " ");
                string token = AuxUtils.GetToken(cmd, " ", 1).ToLower();
                Player player = GlobalVars.nwrGame.Player;
                if (token.Equals("name_gen")) {
                    for (int i = 1; i <= 10; i++) {
                        AddMessage(GlobalVars.nwrGame.NameLib.GenerateName("ru", Gender.gMale, NamesLib.NameGen_RndSlabs));
                    }
                } else {
                    if (token.Equals("test")) {
                        player.Body = new HumanBody(player);
                    } else {
                        if (token.Equals("suicide")) {
                            player.Death("Suicide", null);
                        } else {
                            if (token.Equals("set_fury")) {
                                GlobalVars.Debug_Fury = !GlobalVars.Debug_Fury;
                                string temp = GlobalVars.Debug_Fury ? "yes" : "no";
                                AddMessage(">> (fury = " + temp + ")");
                            } else {
                                if (token.Equals("transform")) {
                                    if (tokCount == 1) {
                                        throw new Exception(BaseLocale.GetStr(RS.rs_NoValue));
                                    }
                                    string temp = AuxUtils.GetToken(cmd, " ", 2);
                                    DataEntry entry = GlobalVars.nwrDB.FindEntryBySign(temp);
                                    if (entry != null) {
                                        EffectExt ext = new EffectExt();
                                        ext.SetParam(EffectParams.ep_MonsterID, entry.GUID);
                                        player.UseEffect(EffectID.eid_Transformation, null, InvokeMode.im_ItSelf, ext);
                                    }
                                } else {
                                    if (token.Equals("add_monster")) {
                                        if (tokCount == 1) {
                                            throw new Exception(BaseLocale.GetStr(RS.rs_NoValue));
                                        }
                                        string temp = AuxUtils.GetToken(cmd, " ", 2);
                                        int tx;
                                        int ty;
                                        if (tokCount == 4) {
                                            tx = Convert.ToInt32(AuxUtils.GetToken(cmd, " ", 3));
                                            ty = Convert.ToInt32(AuxUtils.GetToken(cmd, " ", 4));
                                        } else {
                                            tx = -1;
                                            ty = -1;
                                        }
                                        DataEntry entry = GlobalVars.nwrDB.FindEntryBySign(temp);
                                        if (entry != null) {
                                            GlobalVars.nwrGame.AddCreatureEx(player.LayerID, player.Field.X, player.Field.Y, tx, ty, entry.GUID);
                                        }
                                    } else {
                                        if (token.Equals("kill_all")) {
                                            NWField fld = player.CurrentField;
                                            for (int i = fld.Creatures.Count - 1; i >= 0; i--) {
                                                NWCreature cr = fld.Creatures.GetItem(i);
                                                if (!cr.IsPlayer && !cr.Mercenary) {
                                                    cr.Death("", null);
                                                }
                                            }
                                        } else {
                                            if (token.Equals("show_goals")) {
                                                ShowGoals();
                                            } else {
                                                if (token.Equals("set_divinity")) {
                                                    GlobalVars.Debug_Divinity = !GlobalVars.Debug_Divinity;
                                                    string temp;
                                                    if (GlobalVars.Debug_Divinity) {
                                                        temp = "yes";
                                                    } else {
                                                        temp = "no";
                                                    }
                                                    AddMessage(">> (divinity = " + temp + ")");
                                                } else {
                                                    if (token.Equals("set_freeze")) {
                                                        GlobalVars.Debug_Freeze = !GlobalVars.Debug_Freeze;
                                                        string temp;
                                                        if (GlobalVars.Debug_Freeze) {
                                                            temp = "yes";
                                                        } else {
                                                            temp = "no";
                                                        }
                                                        AddMessage(">> (freeze = " + temp + ")");
                                                    } else {
                                                        if (token.Equals("set_morality")) {
                                                            if (tokCount == 1) {
                                                                throw new Exception(BaseLocale.GetStr(RS.rs_NoValue));
                                                            }
                                                            int dummy = Convert.ToInt32(AuxUtils.GetToken(cmd, " ", 2));
                                                            player.Morality = (sbyte)dummy;
                                                            AddMessage(">> (Player.Morality = " + Convert.ToString(dummy) + ")");
                                                        } else {
                                                            if (token.Equals("takeitem")) {
                                                                if (tokCount == 1) {
                                                                    throw new Exception(BaseLocale.GetStr(RS.rs_NoName));
                                                                }
                                                                if (tokCount == 2) {
                                                                    throw new Exception(BaseLocale.GetStr(RS.rs_NoCount));
                                                                }
                                                                token = AuxUtils.GetToken(cmd, " ", 2);
                                                                int dummy = Convert.ToInt32(AuxUtils.GetToken(cmd, " ", 3));
                                                                TakePlayerItem(player, token, dummy);
                                                            } else {
                                                                AddMessage(BaseLocale.GetStr(RS.rs_CommandUnknown));
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                AddMessage(BaseLocale.GetStr(RS.rs_CommandUnknown));
                AddMessage(ex.Message);
            }
        }

        private void TakePlayerItem(Player player, string item, int aCount)
        {
            ItemEntry itemEntry = (ItemEntry)GlobalVars.nwrDB.FindEntryBySign(item);
            if (itemEntry != null) {
                Item.GenItem(player, itemEntry.GUID, aCount, true);
            } else {
                TakeHackItem(player, item, aCount);
            }
        }

        private static void TakeHackItem(Player player, string needItem, int aCount)
        {
            needItem = needItem.ToLower();

            if (needItem.Equals("artifacts")) {
                Item.GenItem(player, GlobalVars.iid_DwarvenArm, 1, true);
                Item.GenItem(player, GlobalVars.iid_Mimming, 1, true);
                Item.GenItem(player, GlobalVars.iid_Mjollnir, 1, true);
                Item.GenItem(player, GlobalVars.iid_Gjall, 1, true);
                Item.GenItem(player, GlobalVars.iid_Gungnir, 1, true);
            } else if (needItem.Equals("all_rings")) {
                for (int i = 0; i < GlobalVars.dbRings.Count; i++) {
                    Item.GenItem(player, GlobalVars.dbRings[i], aCount, true);
                }
            } else if (needItem.Equals("all_potions")) {
                for (int i = 0; i < GlobalVars.dbPotions.Count; i++) {
                    Item.GenItem(player, GlobalVars.dbPotions[i], aCount, true);
                }
            } else if (needItem.Equals("all_scrolls")) {
                for (int i = 0; i < GlobalVars.dbScrolls.Count; i++) {
                    Item.GenItem(player, GlobalVars.dbScrolls[i], aCount, true);
                }
            } else if (needItem.Equals("all_wands")) {
                for (int i = 0; i < GlobalVars.dbWands.Count; i++) {
                    Item.GenItem(player, GlobalVars.dbWands[i], aCount, true);
                }
            } else if (needItem.Equals("all_amulets")) {
                for (int i = 0; i < GlobalVars.dbAmulets.Count; i++) {
                    Item.GenItem(player, GlobalVars.dbAmulets[i], aCount, true);
                }
            } else if (needItem.Equals("all_armor")) {
                for (int i = 0; i < GlobalVars.dbArmor.Count; i++) {
                    Item.GenItem(player, GlobalVars.dbArmor[i], aCount, true);
                }
            } else if (needItem.Equals("all_foods")) {
                for (int i = 0; i < GlobalVars.dbFoods.Count; i++) {
                    Item.GenItem(player, GlobalVars.dbFoods[i], aCount, true);
                }
            } else if (needItem.Equals("all_tools")) {
                for (int i = 0; i < GlobalVars.dbTools.Count; i++) {
                    Item.GenItem(player, GlobalVars.dbTools[i], aCount, true);
                }
            } else if (needItem.Equals("all_weapon")) {
                for (int i = 0; i < GlobalVars.dbWeapon.Count; i++) {
                    Item.GenItem(player, GlobalVars.dbWeapon[i], aCount, true);
                }
            }
        }

        private void OnEditKeyDown(object sender, KeyEventArgs eventArgs)
        {
            switch (eventArgs.Key) {
                case Keys.GK_RETURN:
                    fCmdIndex = fCmdHistory.Add(fEditBox.Text);
                    DoCommand(fEditBox.Text);
                    fEditBox.Text = "";
                    break;

                case Keys.GK_UP:
                    if (fCmdIndex > 0) {
                        fCmdIndex--;
                        fEditBox.Text = fCmdHistory[fCmdIndex];
                    }
                    break;

                case Keys.GK_DOWN:
                    if (fCmdIndex < fCmdHistory.Count - 1) {
                        fCmdIndex++;
                        fEditBox.Text = fCmdHistory[fCmdIndex];
                    }
                    break;
            }
        }

        private void ShowGoals()
        {
            NWField fld = GlobalVars.nwrGame.Player.CurrentField;
            LocatedEntityList cl = (fld).Creatures;

            int num = cl.Count;
            for (int i = 0; i < num; i++) {
                NWCreature c = (NWCreature)cl.GetItem(i);
                AddMessage(c.Name + ": " + Convert.ToString(c.Brain.GoalsCount));
            }
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            ActiveControl = fEditBox;
        }

        public void AddMessage(string text)
        {
            fTextBox.Lines.Add(text);
        }
    }
}
