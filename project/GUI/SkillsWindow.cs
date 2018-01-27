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
using NWR.Core;
using NWR.Core.Types;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.GUI.Controls;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class SkillsWindow : NWWindow
    {
        public const int SWM_SKILLS = 0;
        public const int SWM_SPELLS = 1;
        public const int SWM_TILES = 2;
        public const int SWM_LANDS = 3;
        public const int SWM_SCROLLS = 4;
        public const int SWM_GODS = 5;

        private int fMode;
        private readonly ListBox fSkillsList;

        public SkillsWindow(BaseControl owner)
            : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 600;
            Height = 460;
            WindowStyle = new WindowStyles(WindowStyles.wsModal, WindowStyles.wsKeyPreview);
            Shifted = true;
            BackDraw = false;

            fSkillsList = new ListBox(this);
            fSkillsList.Left = 10;
            fSkillsList.Top = 10;
            fSkillsList.Width = Width - 20;
            fSkillsList.Height = Height - 20;
            fSkillsList.ItemHeight = 34;
            fSkillsList.IconHeight = 30;
            fSkillsList.IconWidth = 32;
            fSkillsList.Options.Include(LBOptions.lboIcons);
            fSkillsList.Visible = true;
            fSkillsList.Columns = 2;
            fSkillsList.OnItemSelect = OnSkillsSelect;
            fSkillsList.OnKeyDown = OnSkillsKeyDown;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fSkillsList.Dispose();
            }
            base.Dispose(disposing);
        }

        private void OnSkillsKeyDown(object Sender, KeyEventArgs eventArgs)
        {
            if (eventArgs.Key == Keys.GK_RETURN && fSkillsList.SelIndex >= 0) {
                OnSkillsSelect(Sender, MouseButton.mbRight, fSkillsList.Items.GetItem(fSkillsList.SelIndex));
            }
        }

        private void OnSkillsSelect(object Sender, MouseButton Button, LBItem Item)
        {
            if (Button == MouseButton.mbRight) {
                int idx = (int)Item.Data;
                switch (fMode) {
                    case SWM_SKILLS:
                        {
                            SkillID sk = (SkillID)(idx);
                            if (sk != SkillID.Sk_Alchemy) {
                                if (sk != SkillID.Sk_Ironworking) {
                                    if (sk != SkillID.Sk_Spellcasting) {
                                        Hide();
                                        GlobalVars.nwrGame.DoPlayerAction(CreatureAction.caSkillUse, idx);
                                    } else {
                                        Mode = SWM_SPELLS;
                                    }
                                } else {
                                    string ts = GlobalVars.nwrGame.Player.Craft.CheckSmithyTools();
                                    if (ts.CompareTo("") != 0) {
                                        GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.Format(RS.rs_NoInstruments, new object[]{ ts }));
                                    } else {
                                        Hide();
                                        GlobalVars.nwrWin.ShowSmithyWin();
                                    }
                                }
                            } else {
                                Hide();
                                GlobalVars.nwrWin.ShowAlchemyWin();
                            }
                            break;
                        }

                    case SWM_SPELLS:
                        Hide();
                        GlobalVars.nwrGame.DoPlayerAction(CreatureAction.caSpellUse, idx);
                        break;

                    case SWM_TILES:
                        Hide();
                        GlobalVars.nwrWin.TargetData.Ext.SetParam(EffectParams.ep_TileID, idx);
                        GlobalVars.nwrWin.UseTarget();
                        break;

                    case SWM_LANDS:
                        Hide();
                        GlobalVars.nwrWin.TargetData.Ext.SetParam(EffectParams.ep_Land, idx);
                        GlobalVars.nwrWin.UseTarget();
                        break;

                    case SWM_SCROLLS:
                        Hide();
                        GlobalVars.nwrWin.TargetData.Ext.SetParam(EffectParams.ep_ScrollID, idx);
                        GlobalVars.nwrWin.UseTarget();
                        break;

                    case SWM_GODS:
                        Hide();
                        GlobalVars.nwrWin.TargetData.Ext.SetParam(EffectParams.ep_GodID, idx);
                        GlobalVars.nwrWin.UseTarget();
                        break;
                }
            }
        }

        public int Mode
        {
            set {
                fMode = value;
                fSkillsList.Items.Clear();
                Player player = GlobalVars.nwrGame.Player;
    
                switch (fMode) {
                    case SWM_SKILLS:
                        {
                            GlobalVars.nwrWin.ShowTextAux(BaseLocale.GetStr(RS.rs_UseWhichSkill));
    
                            for (var sk = SkillID.Sk_First; sk <= SkillID.Sk_Last; sk++) {
                                SkillRec skRec = StaticData.dbSkills[(int)sk];
                                if (player.GetSkill(sk) > 0) {
                                    string s = BaseLocale.GetStr(skRec.Name) + " (" + BaseLocale.GetStr(RS.rs_Level) + " " + Convert.ToString(player.GetSkill(sk)) + ")";
                                    int imageIndex;
                                    if (skRec.Kinds == SkillKind.ssInnatePower) {
                                        imageIndex = skRec.ImageIndex;
                                    } else {
                                        imageIndex = skRec.ImageIndex;
                                    }
                                    AddItem(s, (object)(int)sk, imageIndex);
                                }
                            }
                            break;
                        }
    
                    case SWM_SPELLS:
                        {
                            GlobalVars.nwrWin.ShowTextAux(BaseLocale.GetStr(RS.rs_CastWhichSpell));
    
                            int skill_Spellcasting = player.GetSkill(SkillID.Sk_Spellcasting);
                            for (var eff = EffectID.eid_First; eff <= EffectID.eid_Last; eff++) {
                                EffectRec efRec = EffectsData.dbEffects[(int)eff];
                                if (skill_Spellcasting >= (int)efRec.LevReq && efRec.MPReq <= player.MPCur) {
                                    string st = " (" + Convert.ToString(efRec.MPReq) + ")";
                                    AddItem(BaseLocale.GetStr(efRec.NameRS) + st, eff,
                                                 StaticData.dbSkills[(int)SkillID.Sk_Spellcasting].ImageIndex);
                                }
                            }
    
                            break;
                        }
    
                    case SWM_TILES:
                        {
                            // "Change the terrain where?"
                            GlobalVars.nwrWin.ShowTextAux(BaseLocale.GetStr(RS.rs_CreateWhichTerrain));
    
                            for (int pd = PlaceID.pid_First; pd <= PlaceID.pid_Last; pd++) {
                                PlaceRec pRec = StaticData.dbPlaces[pd];
                                if ((pRec.Signs.Contains(PlaceFlags.psCanCreate))) {
                                    AddItem(BaseLocale.GetStr(pRec.NameRS), pd, pRec.ImageIndex);
                                }
                            }
    
                            break;
                        }
    
                    case SWM_LANDS:
                        {
                            GlobalVars.nwrWin.ShowTextAux(BaseLocale.GetStr(RS.rs_TravelWhere));
                            StringList lst = GlobalVars.nwrGame.VisitedLands;
    
                            int num = lst.Count;
                            for (int i = 0; i < num; i++) {
                                AddItem(lst[i], lst.GetObject(i), -1);
                            }
                            lst.Dispose();
                            break;
                        }
    
                    case SWM_SCROLLS:
                        {
                            // "Rewrite which?"
                            GlobalVars.nwrWin.ShowTextAux(BaseLocale.GetStr(RS.rs_WriteWhichScroll));
    
                            int num2 = GlobalVars.dbScrolls.Count;
                            for (int i = 0; i < num2; i++) {
                                ItemEntry scr = (ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.dbScrolls[i]);
                                ItemEntry.EffectEntry[] effects = scr.Effects;
                                if (((effects != null) ? effects.Length : 0) >= 1) {
                                    EffectID eff = scr.Effects[0].EffID;
                                    if (player.GetSkill(SkillID.Sk_Spellcasting) >= (int)EffectsData.dbEffects[(int)eff].LevReq) {
                                        AddItem(scr.Name, GlobalVars.dbScrolls[i], -1);
                                    }
                                }
                            }
                            break;
                        }
    
                    case SWM_GODS:
                        for (int i = 0; i < Faith.Pantheon.Length; i++) {
                            CreatureEntry deity = (CreatureEntry)GlobalVars.nwrDB.FindEntryBySign(Faith.Pantheon[i].Sign);
                            if (deity != null) {
                                AddItem(deity.Name, deity.GUID, deity.ImageIndex);
                            }
                        }
                        break;
                }
            }
        }

        private void AddItem(string aName, object aObj, int aImageIndex)
        {
            LBItem listItem = fSkillsList.Items.Add(aName, aObj);
            listItem.Color = Colors.Gold;
            listItem.ImageIndex = aImageIndex;
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();
            fSkillsList.ImagesList = GlobalVars.nwrWin.Resources;
            ActiveControl = fSkillsList;
        }
    }

}