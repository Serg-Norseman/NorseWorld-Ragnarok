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
package nwr.player;

import jzrlib.utils.AuxUtils;
import nwr.creatures.NWCreature;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.types.PantheonRec;
import nwr.core.types.AlignmentEx;
import nwr.core.types.CreatureState;
import nwr.core.types.DamageKind;
import nwr.core.types.ItemState;
import nwr.core.types.RaceID;
import nwr.database.CreatureEntry;
import nwr.database.LandEntry;
import nwr.effects.EffectID;
import nwr.effects.InvokeMode;
import nwr.engine.SoundEngine;
import jzrlib.grammar.Case;
import nwr.item.Item;
import nwr.main.GlobalVars;
import nwr.universe.NWField;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public final class Faith
{
    public static final PantheonRec[] Pantheon;

    // Worship kind
    public static final int WK_PRAYER = 0;
    public static final int WK_SACRIFICE = 1;
    
    private final Player fPlayer;
    
    static {
        Pantheon = new PantheonRec[12];
        Pantheon[0] = new PantheonRec("Freyr", "faith/Freyr.ogg");
        Pantheon[1] = new PantheonRec("Heimdall", "faith/Heimdall.ogg");
        Pantheon[2] = new PantheonRec("Odin", "faith/Odin.ogg");
        Pantheon[3] = new PantheonRec("Thor", "faith/Thor.ogg");

        Pantheon[4] = new PantheonRec("Tyr", "faith/LightGods.ogg");
        Pantheon[5] = new PantheonRec("Balder", "faith/LightGods.ogg");

        Pantheon[6] = new PantheonRec("Jormungand", "faith/DarkGods.ogg");
        Pantheon[7] = new PantheonRec("Fenrir", "faith/DarkGods.ogg");
        Pantheon[8] = new PantheonRec("Garm", "faith/DarkGods.ogg");
        Pantheon[9] = new PantheonRec("Surtr", "faith/DarkGods.ogg");

        Pantheon[10] = new PantheonRec("Loki", "faith/Loki.ogg");
        Pantheon[11] = new PantheonRec("Hela", "faith/Hela.ogg");
    }
    
    public Faith(Player player)
    {
        this.fPlayer = player;
    }

    private void showText(String text)
    {
        this.fPlayer.getSpace().showText(text);
    }

    private boolean checkSacrilege(int deityID)
    {
        boolean result = false;

        CreatureEntry prev_god = (CreatureEntry) GlobalVars.nwrBase.getEntry(this.fPlayer.DivinePatron);
        int prev_ge = AlignmentEx.getGE(prev_god.Alignment);

        CreatureEntry cur_god = (CreatureEntry) GlobalVars.nwrBase.getEntry(deityID);
        int cur_ge = AlignmentEx.getGE(cur_god.Alignment);

        if (this.fPlayer.DivinePatron != deityID && this.fPlayer.DivinePatron > 0) {
            this.showText(Locale.format(RS.rs_FaithLack, new Object[]{prev_god.getName()}));

            if (prev_ge == cur_ge) {
                int num = AuxUtils.getRandom(4);
                switch (num) {
                    case 0:
                        this.showText(Locale.format(RS.rs_PatronIntercedes, cur_god.getName()));
                        break;
                    case 1:
                        this.showText(Locale.format(RS.rs_FriendlyDeity, prev_god.getName()));
                        break;
                    case 2:
                        this.showText(Locale.format(RS.rs_TreacheryNoted, prev_god.getName()));
                        break;
                    case 3:
                        this.showText(Locale.format(RS.rs_LackIgnore, prev_god.getName()));
                        break;
                }
            } else {
                result = true;
            }
        }
        return result;
    }

    private boolean checkDivinePower(int deityID)
    {
        boolean result = true;

        CreatureEntry deity = (CreatureEntry) GlobalVars.nwrBase.getEntry(deityID);
        NWField fld = (NWField) this.fPlayer.getCurrentMap();
        if ((deity.Race == RaceID.crAesir && (fld.LandID == GlobalVars.Land_Muspelheim || fld.LandID == GlobalVars.Land_Niflheim || fld.LandID == GlobalVars.Land_Jotenheim)) || (deity.Race == RaceID.crEvilGod && (fld.LandID == GlobalVars.Land_Valhalla || fld.LandID == GlobalVars.Land_Vigrid || fld.LandID == GlobalVars.Land_Bifrost))) {
            LandEntry land = (LandEntry) GlobalVars.nwrBase.getEntry(fld.LandID);
            this.showText(Locale.format(RS.rs_WontHelp, new Object[]{land.getNounDeclension(jzrlib.grammar.Number.nSingle, Case.cPrepositional), deity.getName()}));
            result = false;
        }

        return result;
    }

    private int consumeSacrifice(int deityID, Item item)
    {
        String msg = "";

        CreatureEntry god = (CreatureEntry) GlobalVars.nwrBase.getEntry(deityID);

        if (this.fPlayer.getEffects().findEffectByID(EffectID.eid_Hallucination) != null) {
            int num = AuxUtils.getRandom(2);
            if (num != 0) {
                if (num == 1) {
                    msg = Locale.getStr(RS.rs_YourSacrificeDisappears2);
                }
            } else {
                msg = Locale.getStr(RS.rs_YourSacrificeDisappears1);
            }
        } else {
            if (this.fPlayer.isBlindness()) {
                msg = Locale.getStr(RS.rs_YourSacrificeDisappears);
            } else {
                if (deityID == GlobalVars.cid_Freyr || deityID == GlobalVars.cid_Heimdall || deityID == GlobalVars.cid_Odin || deityID == GlobalVars.cid_Thor || deityID == GlobalVars.cid_Tyr || deityID == GlobalVars.cid_Balder) {
                    msg = Locale.getStr(RS.rs_FlashOfLight);
                } else {
                    if (deityID == GlobalVars.cid_Surtr) {
                        msg = Locale.getStr(RS.rs_AwesomeColumnOfFire);
                    } else {
                        if (deityID == GlobalVars.cid_Loki) {
                            msg = Locale.getStr(RS.rs_BurstOfFlame);
                        } else {
                            if (deityID == GlobalVars.cid_Hela) {
                                msg = Locale.getStr(RS.rs_CloudOfGreenSmoke);
                            } else {
                                if (deityID == GlobalVars.cid_Jormungand || deityID == GlobalVars.cid_Fenrir || deityID == GlobalVars.cid_Garm) {
                                    msg = Locale.getStr(RS.rs_BlackFire);
                                }
                            }
                        }
                    }
                }

                msg = Locale.format(RS.rs_YourSacrificeIsConsumed, new Object[]{msg});
            }
        }

        this.showText(msg);

        int sacrifice_value;
        if (item.CLSID == GlobalVars.iid_DeadBody) {
            NWCreature victim = (NWCreature) item.getContents().getItem(0);
            sacrifice_value = victim.Level;
            if (AlignmentEx.getGE(victim.Alignment) == AlignmentEx.getGE(god.Alignment)) {
                sacrifice_value = -sacrifice_value;
            }
        } else {
            sacrifice_value = 0;
        }

        this.fPlayer.deleteItem(item);
        
        return sacrifice_value;
    }

    private static int getGodRecord(int deityID)
    {
        CreatureEntry deity = (CreatureEntry) GlobalVars.nwrBase.getEntry(deityID);
        for (int i = 0; i < Pantheon.length; i++) {
            if (Pantheon[i].Sign.equals(deity.Sign)) {
                return i;
            }
        }

        return -1;
    }

    public final void worship(int deityID, int worship, Item item)
    {
        int sacrifice_value;
        if (worship == Faith.WK_PRAYER && item == null) {
            sacrifice_value = 5;
        } else {
            sacrifice_value = this.consumeSacrifice(deityID, item);
        }
        
        if (this.checkDivinePower(deityID)) {
            if (this.checkSacrilege(deityID)) {
                this.takeDivineAward(this.fPlayer.DivinePatron, worship, 0, true);
            }
            this.takeDivineAward(deityID, worship, sacrifice_value, false);

            this.fPlayer.DivinePatron = deityID;
            CreatureEntry cur_god = (CreatureEntry) GlobalVars.nwrBase.getEntry(deityID);
            this.fPlayer.Alignment = cur_god.Alignment;
        }
    }

    private void takeDivineAward(int deityID, int worship, int value, boolean isSacrilege)
    {
        // TODO: eid_DivineGrace???

        CreatureEntry deity = (CreatureEntry) GlobalVars.nwrBase.getEntry(deityID);

        if (value == 0) {
            if (worship == Faith.WK_PRAYER) {
                this.showText(Locale.getStr(RS.rs_YourPrayerWasUnheard));
            } else {
                this.showText(Locale.format(RS.rs_GodIsUnimpressed, new Object[]{deity.getName()}));
            }
        } else {
            if (value < 0) {
                this.takeDivineAward(deityID, worship, 0, true);
                return;
            }
        }

        if (value > 0 && !isSacrilege) {
            int idx = getGodRecord(deityID);
            GlobalVars.nwrWin.playSound(Pantheon[idx].sfxAward, SoundEngine.sk_Sound, -1, -1);
        }
        
        if (deityID == GlobalVars.cid_Freyr) {
            if (isSacrilege) {
                int num = AuxUtils.getRandom(3);
                switch (num) {
                    case 0:
                        this.showText(Locale.getStr(RS.rs_YouInsultedYourDeity));
                        this.fPlayer.Luck--;
                        break;
                    case 1:
                        this.showText(Locale.getStr(RS.rs_YouAreTerrified));
                        this.fPlayer.useEffect(EffectID.eid_Paralysis, null, InvokeMode.im_ItSelf, null);
                        break;
                    case 2: {
                        this.showText(Locale.getStr(RS.rs_DarklingGlow));
                        this.showText(Locale.getStr(RS.rs_GlowSlowlyFades));

                        int num2 = this.fPlayer.getItems().getCount();
                        for (int i = 0; i < num2; i++) {
                            this.fPlayer.getItems().getItem(i).State = ItemState.is_Cursed;
                        }
                    }
                    break;
                }
            } else {
                this.showText(Locale.getStr(RS.rs_AFeelingOfSanctityComesOverYou));
            }
        } else if (deityID == GlobalVars.cid_Heimdall) {
            if (isSacrilege) {
            } else {
                this.showText(Locale.getStr(RS.rs_BlueFlameBurst));
                this.showText(Locale.getStr(RS.rs_VioletNimbus));
                this.fPlayer.useEffect(EffectID.eid_Speedup, null, InvokeMode.im_ItSelf, null);
            }
        } else if (deityID == GlobalVars.cid_Odin) {
            if (isSacrilege) {
                this.showText(Locale.getStr(RS.rs_OdinThunderbolt));
                this.fPlayer.applyDamage(this.fPlayer.Level * 5, DamageKind.dkPhysical, null, Locale.getStr(RS.rs_OdinWrath));
                if (this.fPlayer.getState() != CreatureState.csDead) {
                    this.showText(Locale.getStr(RS.rs_BoltWarpsYourBody));
                    this.fPlayer.Constitution /= 2;
                    this.fPlayer.Strength /= 2;
                }
            } else {
                this.showText(Locale.getStr(RS.rs_OdinThunderclap));
                int lev = value % 3;
                int num3 = AuxUtils.getRandom(lev);
                switch (num3) {
                    case 0:
                        this.showText(Locale.getStr(RS.rs_NothingHappens));
                        break;
                    case 1:
                        this.showText(Locale.getStr(RS.rs_ObjectAtYourFeet));
                        this.showText(Locale.getStr(RS.rs_UseMyGiftWisely));
                        break;
                    case 2:
                        this.showText(Locale.getStr(RS.rs_EternityGong));
                        break;
                    case 3:
                        this.showText(Locale.getStr(RS.rs_AnInvisibleChoirSings));
                        this.showText(Locale.getStr(RS.rs_CongratulationsMortal));
                        this.showText(Locale.getStr(RS.rs_InReturnForThyService));
                        this.showText(Locale.getStr(RS.rs_YouAscendToTheStatusDemigod));
                        this.fPlayer.useEffect(EffectID.eid_Invulnerable, null, InvokeMode.im_ItSelf, null);
                        break;
                }
            }
        } else if (deityID == GlobalVars.cid_Thor) {
            if (isSacrilege) {
                this.showText(Locale.getStr(RS.rs_YouAreZorchedByGodsfire));
                if (this.fPlayer.HPCur > 0) {
                    this.showText(Locale.getStr(RS.rs_FireBurnsYourExperience));
                    this.fPlayer.Level = 0;
                    this.fPlayer.setExperience(0);
                    this.fPlayer.HPCur = this.fPlayer.Constitution;
                    this.fPlayer.setHPMax(this.fPlayer.Constitution);
                    this.showText(Locale.getStr(RS.rs_YourPowerIsReducedByBlast));
                    this.fPlayer.MPCur = 0;
                    this.fPlayer.MPMax = 0;
                }
            } else {
            }
        } else if (deityID == GlobalVars.cid_Tyr) {
            if (isSacrilege) {
                this.showText(Locale.getStr(RS.rs_VileCreatureThouDurstCallUponMe));
                this.showText(Locale.getStr(RS.rs_WalkNoMorePerversionOfNature));
                this.showText(Locale.getStr(RS.rs_YouFeelLikeYouAreFallingApart));
            } else {
                this.showText(Locale.getStr(RS.rs_YouAreSurroundedByAShimmeringLight));
            }
        } else if (deityID == GlobalVars.cid_Balder) {
            if (isSacrilege) {
                this.showText(Locale.getStr(RS.rs_YourArdentPleaIsIgnored));
                this.showText(Locale.getStr(RS.rs_YouFeelAshamed));
                this.fPlayer.setExperience((int) (this.fPlayer.getExperience() - ((long) Math.round(this.fPlayer.getExperience() / 4.0))));
            } else {
                this.showText(Locale.getStr(RS.rs_AShaftOfLucentRadianceLancesDownFromTheHeavens));
                this.showText(Locale.getStr(RS.rs_YouFeelUplifted));
                this.fPlayer.setExperience(this.fPlayer.getExperience() + value);
                this.fPlayer.useEffect(EffectID.eid_Cure, null, InvokeMode.im_ItSelf, null);
                this.fPlayer.useEffect(EffectID.eid_Luck, null, InvokeMode.im_ItSelf, null);
            }
        } else if (deityID == GlobalVars.cid_Surtr) {
            if (isSacrilege) {
                this.showText(Locale.getStr(RS.rs_YouAreBlastedByShaftOfBlackFire));
                this.fPlayer.applyDamage(this.fPlayer.Level * 5, DamageKind.dkPhysical, null, Locale.getStr(RS.rs_SurtrAnger));
                if (this.fPlayer.HPCur > 0) {
                    this.showText(Locale.getStr(RS.rs_YouAreWreathedInCloudsOfSmoke));
                    for (int i = this.fPlayer.getItems().getCount() - 1; i >= 0; i--) {
                        if (this.fPlayer.getItems().getItem(i).State == ItemState.is_Blessed) {
                            this.fPlayer.deleteItem(this.fPlayer.getItems().getItem(i));
                        }
                    }
                    this.showText(Locale.getStr(RS.rs_YouFeelSurtrBlackHand));
                    this.fPlayer.Constitution /= 4;
                }
            } else {
                this.showText(Locale.getStr(RS.rs_YouLearntToSeeBeautyInFireAndDestruction));
            }
        } else if (deityID == GlobalVars.cid_Loki) {
            if (isSacrilege) {
                this.showText(Locale.format(RS.rs_DeityRejectsYourSacrifice, new Object[]{deity.getName()}));
                this.showText(Locale.getStr(RS.rs_SufferInfidel));
                this.fPlayer.Luck -= 5;
            }
        } else if (deityID == GlobalVars.cid_Hela) {
            if (isSacrilege) {
                this.showText(Locale.getStr(RS.rs_YouAreZappedByDarkMoonbeams));
                this.fPlayer.applyDamage(this.fPlayer.Level * 5, DamageKind.dkPhysical, null, Locale.getStr(RS.rs_HelaMalice));
                if (this.fPlayer.HPCur > 0) {
                    this.showText(Locale.getStr(RS.rs_BeamsLeachYouMagicalPower));
                    this.fPlayer.MPCur = this.fPlayer.MPMax / 5;
                    this.fPlayer.Constitution /= 5;
                    this.fPlayer.clearSkills();
                }
            }
        } else {
            /*if (deityID == GlobalVars.cid_Jormungand || deityID == GlobalVars.cid_Fenrir || deityID == GlobalVars.cid_Garm) {
            }*/
        }

        // "You feel that <god> is angry with you."
    }
}
