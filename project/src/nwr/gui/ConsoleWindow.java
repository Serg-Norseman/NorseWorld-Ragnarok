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

import jzrlib.core.LocatedEntityList;
import jzrlib.core.StringList;
import jzrlib.utils.TextUtils;
import nwr.creatures.HumanBody;
import nwr.creatures.NWCreature;
import nwr.creatures.NamesLib;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.database.DataEntry;
import nwr.database.ItemEntry;
import nwr.effects.EffectExt;
import nwr.effects.EffectID;
import nwr.effects.EffectParams;
import nwr.effects.InvokeMode;
import nwr.engine.BaseControl;
import nwr.engine.KeyEventArgs;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.EditBox;
import nwr.gui.controls.TextBox;
import jzrlib.grammar.Gender;
import nwr.item.Item;
import nwr.main.GlobalVars;
import nwr.player.Player;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ConsoleWindow extends NWWindow
{
    private final StringList fCmdHistory;
    private final EditBox fEditBox;
    private final TextBox fTextBox;
    private int fCmdIndex;

    public ConsoleWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setLeft(120);
        super.setTop(90);
        super.setWidth(480);
        super.setHeight(300);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);

        this.fCmdHistory = new StringList();
        this.fCmdIndex = -1;

        NWButton btn = new NWButton(this);
        btn.setWidth(90);
        btn.setHeight(30);
        btn.setLeft(super.getWidth() - 90 - 20);
        btn.setTop(super.getHeight() - 30 - 20);
        btn.setImageFile("itf/DlgBtn.tga");
        btn.OnClick = this::onBtnClose;
        btn.OnLangChange = GlobalVars.nwrWin::LangChange;
        btn.setLangResID(8);

        this.fEditBox = new EditBox(this);
        this.fEditBox.setLeft(20);
        this.fEditBox.setTop(20);
        this.fEditBox.setWidth(super.getWidth() - 40);
        this.fEditBox.OnKeyDown = this::onEditKeyDown;

        this.fTextBox = new TextBox(this);
        this.fTextBox.setLeft(20);
        this.fTextBox.setTop(20 + this.fEditBox.getHeight() + 10);
        this.fTextBox.setWidth(super.getWidth() - 40);
        this.fTextBox.setHeight(super.getHeight() - 100 - this.fEditBox.getHeight());
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fCmdHistory.dispose();
        }
        super.dispose(disposing);
    }

    private void onBtnClose(Object sender)
    {
        this.hide();
    }

    private void doCommand(String cmd)
    {
        try {
            int tokCount = (int) TextUtils.getTokensCount(cmd, " ");
            String token = TextUtils.getToken(cmd, " ", 1).toLowerCase();
            Player player = GlobalVars.nwrGame.getPlayer();
            if (token.equals("name_gen")) {
                for (int i = 1; i <= 10; i++) {
                    this.addMessage(GlobalVars.nwrGame.getNameLib().generateName("ru", Gender.gMale, NamesLib.NameGen_RndSlabs));
                }
            } else {
                if (token.equals("test")) {
                    player.setBody(new HumanBody(player));
                } else {
                    if (token.equals("suicide")) {
                        player.death("Suicide", null);
                    } else {
                        if (token.equals("set_fury")) {
                            GlobalVars.Debug_Fury = !GlobalVars.Debug_Fury;
                            String temp;
                            if (GlobalVars.Debug_Fury) {
                                temp = "yes";
                            } else {
                                temp = "no";
                            }
                            this.addMessage(">> (fury = " + temp + ")");
                        } else {
                            if (token.equals("transform")) {
                                if (tokCount == 1) {
                                    throw new RuntimeException(Locale.getStr(RS.rs_NoValue));
                                }
                                String temp = TextUtils.getToken(cmd, " ", 2);
                                DataEntry entry = GlobalVars.nwrBase.findEntryBySign(temp);
                                if (entry != null) {
                                    EffectExt ext = new EffectExt();
                                    ext.setParam(EffectParams.ep_MonsterID, entry.GUID);
                                    player.useEffect(EffectID.eid_Transformation, null, InvokeMode.im_ItSelf, ext);
                                }
                            } else {
                                if (token.equals("add_monster")) {
                                    if (tokCount == 1) {
                                        throw new RuntimeException(Locale.getStr(RS.rs_NoValue));
                                    }
                                    String temp = TextUtils.getToken(cmd, " ", 2);
                                    int tx;
                                    int ty;
                                    if (tokCount == 4) {
                                        tx = Integer.parseInt(TextUtils.getToken(cmd, " ", 3));
                                        ty = Integer.parseInt(TextUtils.getToken(cmd, " ", 4));
                                    } else {
                                        tx = -1;
                                        ty = -1;
                                    }
                                    DataEntry entry = GlobalVars.nwrBase.findEntryBySign(temp);
                                    if (entry != null) {
                                        GlobalVars.nwrGame.addCreatureEx(player.LayerID, player.getField().X, player.getField().Y, tx, ty, entry.GUID);
                                    }
                                } else {
                                    if (token.equals("kill_all")) {
                                        NWField fld = player.getCurrentField();
                                        for (int i = fld.getCreatures().getCount() - 1; i >= 0; i--) {
                                            NWCreature cr = fld.getCreatures().getItem(i);
                                            if (!cr.isPlayer() && !cr.isMercenary()) {
                                                cr.death("", null);
                                            }
                                        }
                                    } else {
                                        if (token.equals("show_goals")) {
                                            this.showGoals();
                                        } else {
                                            if (token.equals("set_divinity")) {
                                                GlobalVars.Debug_Divinity = !GlobalVars.Debug_Divinity;
                                                String temp;
                                                if (GlobalVars.Debug_Divinity) {
                                                    temp = "yes";
                                                } else {
                                                    temp = "no";
                                                }
                                                this.addMessage(">> (divinity = " + temp + ")");
                                            } else {
                                                if (token.equals("set_freeze")) {
                                                    GlobalVars.Debug_Freeze = !GlobalVars.Debug_Freeze;
                                                    String temp;
                                                    if (GlobalVars.Debug_Freeze) {
                                                        temp = "yes";
                                                    } else {
                                                        temp = "no";
                                                    }
                                                    this.addMessage(">> (freeze = " + temp + ")");
                                                } else {
                                                    if (token.equals("set_morality")) {
                                                        if (tokCount == 1) {
                                                            throw new RuntimeException(Locale.getStr(RS.rs_NoValue));
                                                        }
                                                        int dummy = Integer.parseInt(TextUtils.getToken(cmd, " ", 2));
                                                        player.Morality = (byte) dummy;
                                                        this.addMessage(">> (Player.Morality = " + String.valueOf(dummy) + ")");
                                                    } else {
                                                        if (token.equals("takeitem")) {
                                                            if (tokCount == 1) {
                                                                throw new RuntimeException(Locale.getStr(RS.rs_NoName));
                                                            }
                                                            if (tokCount == 2) {
                                                                throw new RuntimeException(Locale.getStr(RS.rs_NoCount));
                                                            }
                                                            token = TextUtils.getToken(cmd, " ", 2);
                                                            int dummy = Integer.parseInt(TextUtils.getToken(cmd, " ", 3));
                                                            takePlayerItem(player, token, dummy);
                                                        } else {
                                                            this.addMessage(Locale.getStr(RS.rs_CommandUnknown));
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
            this.addMessage(Locale.getStr(RS.rs_CommandUnknown));
            this.addMessage(ex.getMessage());
        }
    }

    private void takePlayerItem(Player player, String item, int aCount)
    {
        ItemEntry itemEntry = (ItemEntry) GlobalVars.nwrBase.findEntryBySign(item);
        if (itemEntry != null) {
            Item.genItem(player, itemEntry.GUID, aCount, true);
        } else {
            takeHackItem(player, item, aCount);
        }
    }

    private static void takeHackItem(Player player, String needItem, int aCount)
    {
        needItem = needItem.toLowerCase();

        if (needItem.equals("artifacts")) {
            Item.genItem(player, GlobalVars.iid_DwarvenArm, 1, true);
            Item.genItem(player, GlobalVars.iid_Mimming, 1, true);
            Item.genItem(player, GlobalVars.iid_Mjollnir, 1, true);
            Item.genItem(player, GlobalVars.iid_Gjall, 1, true);
            Item.genItem(player, GlobalVars.iid_Gungnir, 1, true);
        } else if (needItem.equals("all_rings")) {
            for (int i = 0; i < GlobalVars.dbRings.getCount(); i++) {
                Item.genItem(player, GlobalVars.dbRings.get(i), aCount, true);
            }
        } else if (needItem.equals("all_potions")) {
            for (int i = 0; i < GlobalVars.dbPotions.getCount(); i++) {
                Item.genItem(player, GlobalVars.dbPotions.get(i), aCount, true);
            }
        } else if (needItem.equals("all_scrolls")) {
            for (int i = 0; i < GlobalVars.dbScrolls.getCount(); i++) {
                Item.genItem(player, GlobalVars.dbScrolls.get(i), aCount, true);
            }
        } else if (needItem.equals("all_wands")) {
            for (int i = 0; i < GlobalVars.dbWands.getCount(); i++) {
                Item.genItem(player, GlobalVars.dbWands.get(i), aCount, true);
            }
        } else if (needItem.equals("all_amulets")) {
            for (int i = 0; i < GlobalVars.dbAmulets.getCount(); i++) {
                Item.genItem(player, GlobalVars.dbAmulets.get(i), aCount, true);
            }
        } else if (needItem.equals("all_armor")) {
            for (int i = 0; i < GlobalVars.dbArmor.getCount(); i++) {
                Item.genItem(player, GlobalVars.dbArmor.get(i), aCount, true);
            }
        } else if (needItem.equals("all_foods")) {
            for (int i = 0; i < GlobalVars.dbFoods.getCount(); i++) {
                Item.genItem(player, GlobalVars.dbFoods.get(i), aCount, true);
            }
        } else if (needItem.equals("all_tools")) {
            for (int i = 0; i < GlobalVars.dbTools.getCount(); i++) {
                Item.genItem(player, GlobalVars.dbTools.get(i), aCount, true);
            }
        } else if (needItem.equals("all_weapon")) {
            for (int i = 0; i < GlobalVars.dbWeapon.getCount(); i++) {
                Item.genItem(player, GlobalVars.dbWeapon.get(i), aCount, true);
            }
        }
    }

    private void onEditKeyDown(Object sender, KeyEventArgs eventArgs)
    {
        switch (eventArgs.Key) {
            case GK_RETURN:
                this.fCmdIndex = this.fCmdHistory.add(this.fEditBox.getText());
                this.doCommand(this.fEditBox.getText());
                this.fEditBox.setText("");
                break;

            case GK_UP:
                if (this.fCmdIndex > 0) {
                    this.fCmdIndex--;
                    this.fEditBox.setText(this.fCmdHistory.get(this.fCmdIndex));
                }
                break;

            case GK_DOWN:
                if (this.fCmdIndex < this.fCmdHistory.getCount() - 1) {
                    this.fCmdIndex++;
                    this.fEditBox.setText(this.fCmdHistory.get(this.fCmdIndex));
                }
                break;
        }
    }

    private void showGoals()
    {
        NWField fld = GlobalVars.nwrGame.getPlayer().getCurrentField();
        LocatedEntityList cl = (fld).getCreatures();

        int num = cl.getCount();
        for (int i = 0; i < num; i++) {
            NWCreature c = (NWCreature) cl.getItem(i);
            this.addMessage(c.getName() + ": " + String.valueOf(c.getBrain().getGoalsCount()));
        }
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        super.setActiveControl(this.fEditBox);
    }

    public final void addMessage(String text)
    {
        this.fTextBox.getLines().add(text);
    }
}
