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

import jzrlib.core.StringList;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.CreatureAction;
import nwr.core.types.PlaceFlags;
import nwr.core.types.PlaceID;
import nwr.core.types.PlaceRec;
import nwr.core.types.SkillID;
import nwr.core.types.SkillKind;
import nwr.database.CreatureEntry;
import nwr.database.ItemEntry;
import nwr.database.ItemEntry.EffectEntry;
import nwr.effects.EffectID;
import nwr.effects.EffectParams;
import nwr.effects.EffectRec;
import nwr.effects.EffectsData;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;
import nwr.engine.KeyEventArgs;
import nwr.engine.Keys;
import nwr.engine.MouseButton;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.LBItem;
import nwr.gui.controls.LBOptions;
import nwr.gui.controls.ListBox;
import nwr.main.GlobalVars;
import nwr.player.Faith;
import nwr.player.Player;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class SkillsWindow extends NWWindow
{
    public static final int SWM_SKILLS = 0;
    public static final int SWM_SPELLS = 1;
    public static final int SWM_TILES = 2;
    public static final int SWM_LANDS = 3;
    public static final int SWM_SCROLLS = 4;
    public static final int SWM_GODS = 5;
    
    private int fMode;
    private final ListBox fSkillsList;

    public SkillsWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(600);
        super.setHeight(460);
        super.WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview);
        super.Shifted = true;
        super.BackDraw = false;

        this.fSkillsList = new ListBox(this);
        this.fSkillsList.setLeft(10);
        this.fSkillsList.setTop(10);
        this.fSkillsList.setWidth(super.getWidth() - 20);
        this.fSkillsList.setHeight(super.getHeight() - 20);
        this.fSkillsList.setItemHeight(34);
        this.fSkillsList.setIconHeight(30);
        this.fSkillsList.setIconWidth(32);
        this.fSkillsList.Options.include(LBOptions.lboIcons);
        this.fSkillsList.setVisible(true);
        this.fSkillsList.setColumns(2);
        this.fSkillsList.OnItemSelect = this::onSkillsSelect;
        this.fSkillsList.OnKeyDown = this::onSkillsKeyDown;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fSkillsList.dispose();
        }
        super.dispose(disposing);
    }

    private void onSkillsKeyDown(Object Sender, KeyEventArgs eventArgs)
    {
        if (eventArgs.Key == Keys.GK_RETURN && this.fSkillsList.getSelIndex() >= 0) {
            this.onSkillsSelect(Sender, MouseButton.mbRight, this.fSkillsList.getItems().getItem(this.fSkillsList.getSelIndex()));
        }
    }

    private void onSkillsSelect(Object Sender, MouseButton Button, LBItem Item)
    {
        if (Button == MouseButton.mbRight) {
            int idx = (int) Item.Data;
            switch (this.fMode) {
                case SWM_SKILLS: {
                    SkillID sk = SkillID.forValue(idx);
                    if (sk != SkillID.Sk_Alchemy) {
                        if (sk != SkillID.Sk_Ironworking) {
                            if (sk != SkillID.Sk_Spellcasting) {
                                this.hide();
                                GlobalVars.nwrGame.doPlayerAction(CreatureAction.caSkillUse, idx);
                            } else {
                                this.setMode(SWM_SPELLS);
                            }
                        } else {
                            String ts = GlobalVars.nwrGame.getPlayer().getCraft().checkSmithyTools();
                            if (ts.compareTo("") != 0) {
                                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.format(RS.rs_NoInstruments, new Object[]{ts}));
                            } else {
                                this.hide();
                                GlobalVars.nwrWin.showSmithyWin();
                            }
                        }
                    } else {
                        this.hide();
                        GlobalVars.nwrWin.showAlchemyWin();
                    }
                    break;
                }

                case SWM_SPELLS:
                    this.hide();
                    GlobalVars.nwrGame.doPlayerAction(CreatureAction.caSpellUse, idx);
                    break;

                case SWM_TILES:
                    this.hide();
                    GlobalVars.nwrWin.getTargetData().Ext.setParam(EffectParams.ep_TileID, idx);
                    GlobalVars.nwrWin.useTarget();
                    break;

                case SWM_LANDS:
                    this.hide();
                    GlobalVars.nwrWin.getTargetData().Ext.setParam(EffectParams.ep_Land, idx);
                    GlobalVars.nwrWin.useTarget();
                    break;

                case SWM_SCROLLS:
                    this.hide();
                    GlobalVars.nwrWin.getTargetData().Ext.setParam(EffectParams.ep_ScrollID, idx);
                    GlobalVars.nwrWin.useTarget();
                    break;

                case SWM_GODS:
                    this.hide();
                    GlobalVars.nwrWin.getTargetData().Ext.setParam(EffectParams.ep_GodID, idx);
                    GlobalVars.nwrWin.useTarget();
                    break;
            }
        }
    }

    public final void setMode(int value)
    {
        this.fMode = value;
        this.fSkillsList.getItems().clear();
        Player player = GlobalVars.nwrGame.getPlayer();

        switch (this.fMode) {
            case SWM_SKILLS: {
                GlobalVars.nwrWin.showTextAux(Locale.getStr(RS.rs_UseWhichSkill));

                for (int sk = SkillID.Sk_First; sk <= SkillID.Sk_Last; sk++) {
                    SkillID sid = SkillID.forValue(sk);
                    if (player.getSkill(sid) > 0) {
                        String s = Locale.getStr(sid.NameRS) + " (" + Locale.getStr(RS.rs_Level) + " " + String.valueOf(player.getSkill(sid)) + ")";
                        int imageIndex;
                        if (sid.Kinds == SkillKind.ssInnatePower) {
                            imageIndex = sid.ImageIndex;
                        } else {
                            imageIndex = sid.ImageIndex;
                        }
                        this.addItem(s, (Object) (int) sk, imageIndex);
                    }
                }
                break;
            }

            case SWM_SPELLS: {
                GlobalVars.nwrWin.showTextAux(Locale.getStr(RS.rs_CastWhichSpell));

                int skill_Spellcasting = player.getSkill(SkillID.Sk_Spellcasting);
                for (int eff = EffectID.eid_First; eff <= EffectID.eid_Last; eff++) {
                    EffectRec efRec = EffectsData.dbEffects[eff];
                    if (skill_Spellcasting >= (int) efRec.LevReq && efRec.MPReq <= player.MPCur) {
                        String st = " (" + String.valueOf(efRec.MPReq) + ")";
                        this.addItem(Locale.getStr(efRec.NameRS) + st, eff, SkillID.Sk_Spellcasting.ImageIndex);
                    }
                }

                break;
            }

            case SWM_TILES: {
                // "Change the terrain where?"
                GlobalVars.nwrWin.showTextAux(Locale.getStr(RS.rs_CreateWhichTerrain));

                for (int pd = PlaceID.pid_First; pd <= PlaceID.pid_Last; pd++) {
                    PlaceRec pRec = StaticData.dbPlaces[pd];
                    if ((pRec.Signs.contains(PlaceFlags.psCanCreate))) {
                        this.addItem(Locale.getStr(pRec.NameRS), pd, pRec.ImageIndex);
                    }
                }

                break;
            }

            case SWM_LANDS: {
                GlobalVars.nwrWin.showTextAux(Locale.getStr(RS.rs_TravelWhere));
                StringList lst = GlobalVars.nwrGame.getVisitedLands();

                int num = lst.getCount();
                for (int i = 0; i < num; i++) {
                    this.addItem(lst.get(i), lst.getObject(i), -1);
                }
                lst.dispose();
                break;
            }

            case SWM_SCROLLS: {
                // "Rewrite which?"
                GlobalVars.nwrWin.showTextAux(Locale.getStr(RS.rs_WriteWhichScroll));

                int num2 = GlobalVars.dbScrolls.getCount();
                for (int i = 0; i < num2; i++) {
                    ItemEntry scr = (ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.dbScrolls.get(i));
                    EffectEntry[] effects = scr.Effects;
                    if (((effects != null) ? effects.length : 0) >= 1) {
                        EffectID eff = scr.Effects[0].EffID;
                        if (player.getSkill(SkillID.Sk_Spellcasting) >= (int) EffectsData.dbEffects[eff.getValue()].LevReq) {
                            this.addItem(scr.getName(), GlobalVars.dbScrolls.get(i), -1);
                        }
                    }
                }
                break;
            }

            case SWM_GODS:
                for (int i = 0; i < Faith.Pantheon.length; i++) {
                    CreatureEntry deity = (CreatureEntry) GlobalVars.nwrBase.findEntryBySign(Faith.Pantheon[i].Sign);
                    if (deity != null) {
                        this.addItem(deity.getName(), deity.GUID, deity.ImageIndex);
                    }
                }
                break;
        }
    }

    private void addItem(String aName, Object aObj, int aImageIndex)
    {
        LBItem listItem = this.fSkillsList.getItems().add(aName, aObj);
        listItem.Color = BaseScreen.clGold;
        listItem.ImageIndex = aImageIndex;
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();
        this.fSkillsList.ImagesList = GlobalVars.nwrWin.Resources;
        super.setActiveControl(this.fSkillsList);
    }
}
