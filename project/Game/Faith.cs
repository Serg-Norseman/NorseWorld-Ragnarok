/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
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
using NWR.Creatures;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.Items;
using NWR.Universe;
using ZRLib.Grammar;

namespace NWR.Game
{
    public sealed class Faith
    {
        public static readonly PantheonRec[] Pantheon;

        // Worship kind
        public const int WK_PRAYER = 0;
        public const int WK_SACRIFICE = 1;

        private readonly Player fPlayer;

        static Faith()
        {
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
            fPlayer = player;
        }

        private void ShowText(string text)
        {
            fPlayer.Space.ShowText(text);
        }

        private bool CheckSacrilege(int deityID)
        {
            bool result = false;

            CreatureEntry prevGod = (CreatureEntry)GlobalVars.nwrDB.GetEntry(fPlayer.DivinePatron);
            int prevGe = prevGod.Alignment.GetGE();

            CreatureEntry curGod = (CreatureEntry)GlobalVars.nwrDB.GetEntry(deityID);
            int curGe = curGod.Alignment.GetGE();

            if (fPlayer.DivinePatron != deityID && fPlayer.DivinePatron > 0) {
                ShowText(Locale.Format(RS.rs_FaithLack, new object[]{ prevGod.Name }));

                if (prevGe == curGe) {
                    int num = RandomHelper.GetRandom(4);
                    switch (num) {
                        case 0:
                            ShowText(Locale.Format(RS.rs_PatronIntercedes, curGod.Name));
                            break;
                        case 1:
                            ShowText(Locale.Format(RS.rs_FriendlyDeity, prevGod.Name));
                            break;
                        case 2:
                            ShowText(Locale.Format(RS.rs_TreacheryNoted, prevGod.Name));
                            break;
                        case 3:
                            ShowText(Locale.Format(RS.rs_LackIgnore, prevGod.Name));
                            break;
                    }
                } else {
                    result = true;
                }
            }
            return result;
        }

        private bool CheckDivinePower(int deityID)
        {
            bool result = true;

            CreatureEntry deity = (CreatureEntry)GlobalVars.nwrDB.GetEntry(deityID);
            NWField fld = (NWField)fPlayer.CurrentMap;
            if ((deity.Race == RaceID.crAesir && (fld.LandID == GlobalVars.Land_Muspelheim || fld.LandID == GlobalVars.Land_Niflheim || fld.LandID == GlobalVars.Land_Jotenheim)) || (deity.Race == RaceID.crEvilGod && (fld.LandID == GlobalVars.Land_Valhalla || fld.LandID == GlobalVars.Land_Vigrid || fld.LandID == GlobalVars.Land_Bifrost))) {
                LandEntry land = (LandEntry)GlobalVars.nwrDB.GetEntry(fld.LandID);
                ShowText(Locale.Format(RS.rs_WontHelp, new object[] {
                    land.GetNounDeclension(Number.nSingle, Case.cPrepositional),
                    deity.Name
                }));
                result = false;
            }

            return result;
        }

        private int ConsumeSacrifice(int deityID, Item item)
        {
            string msg = "";

            CreatureEntry god = (CreatureEntry)GlobalVars.nwrDB.GetEntry(deityID);

            if (fPlayer.Effects.FindEffectByID(EffectID.eid_Hallucination) != null) {
                int num = RandomHelper.GetRandom(2);
                if (num != 0) {
                    if (num == 1) {
                        msg = Locale.GetStr(RS.rs_YourSacrificeDisappears2);
                    }
                } else {
                    msg = Locale.GetStr(RS.rs_YourSacrificeDisappears1);
                }
            } else {
                if (fPlayer.Blindness) {
                    msg = Locale.GetStr(RS.rs_YourSacrificeDisappears);
                } else {
                    if (deityID == GlobalVars.cid_Freyr || deityID == GlobalVars.cid_Heimdall || deityID == GlobalVars.cid_Odin || deityID == GlobalVars.cid_Thor || deityID == GlobalVars.cid_Tyr || deityID == GlobalVars.cid_Balder) {
                        msg = Locale.GetStr(RS.rs_FlashOfLight);
                    } else {
                        if (deityID == GlobalVars.cid_Surtr) {
                            msg = Locale.GetStr(RS.rs_AwesomeColumnOfFire);
                        } else {
                            if (deityID == GlobalVars.cid_Loki) {
                                msg = Locale.GetStr(RS.rs_BurstOfFlame);
                            } else {
                                if (deityID == GlobalVars.cid_Hela) {
                                    msg = Locale.GetStr(RS.rs_CloudOfGreenSmoke);
                                } else {
                                    if (deityID == GlobalVars.cid_Jormungand || deityID == GlobalVars.cid_Fenrir || deityID == GlobalVars.cid_Garm) {
                                        msg = Locale.GetStr(RS.rs_BlackFire);
                                    }
                                }
                            }
                        }
                    }

                    msg = Locale.Format(RS.rs_YourSacrificeIsConsumed, new object[]{ msg });
                }
            }

            ShowText(msg);

            int sacrificeValue;
            if (item.CLSID_Renamed == GlobalVars.iid_DeadBody) {
                NWCreature victim = (NWCreature)item.Contents.GetItem(0);
                sacrificeValue = victim.Level;
                if (victim.Alignment.GetGE() == god.Alignment.GetGE()) {
                    sacrificeValue = -sacrificeValue;
                }
            } else {
                sacrificeValue = 0;
            }

            fPlayer.DeleteItem(item);

            return sacrificeValue;
        }

        private static int GetGodRecord(int deityID)
        {
            CreatureEntry deity = (CreatureEntry)GlobalVars.nwrDB.GetEntry(deityID);
            for (int i = 0; i < Pantheon.Length; i++) {
                if (Pantheon[i].Sign.Equals(deity.Sign)) {
                    return i;
                }
            }

            return -1;
        }

        public void Worship(int deityID, int worship, Item item)
        {
            int sacrificeValue;
            if (worship == Faith.WK_PRAYER && item == null) {
                sacrificeValue = 5;
            } else {
                sacrificeValue = ConsumeSacrifice(deityID, item);
            }

            if (CheckDivinePower(deityID)) {
                if (CheckSacrilege(deityID)) {
                    TakeDivineAward(fPlayer.DivinePatron, worship, 0, true);
                }
                TakeDivineAward(deityID, worship, sacrificeValue, false);

                fPlayer.DivinePatron = deityID;
                CreatureEntry curGod = (CreatureEntry)GlobalVars.nwrDB.GetEntry(deityID);
                fPlayer.Alignment = curGod.Alignment;
            }
        }

        private void TakeDivineAward(int deityID, int worship, int value, bool isSacrilege)
        {
            // TODO: eid_DivineGrace???

            CreatureEntry deity = (CreatureEntry)GlobalVars.nwrDB.GetEntry(deityID);

            if (value == 0) {
                if (worship == Faith.WK_PRAYER) {
                    ShowText(Locale.GetStr(RS.rs_YourPrayerWasUnheard));
                } else {
                    ShowText(Locale.Format(RS.rs_GodIsUnimpressed, new object[]{ deity.Name }));
                }
            } else {
                if (value < 0) {
                    TakeDivineAward(deityID, worship, 0, true);
                    return;
                }
            }

            if (value > 0 && !isSacrilege) {
                int idx = GetGodRecord(deityID);
                GlobalVars.nwrWin.PlaySound(Pantheon[idx].SfxAward, SoundEngine.sk_Sound, -1, -1);
            }

            if (deityID == GlobalVars.cid_Freyr) {
                if (isSacrilege) {
                    int num = RandomHelper.GetRandom(3);
                    switch (num) {
                        case 0:
                            ShowText(Locale.GetStr(RS.rs_YouInsultedYourDeity));
                            fPlayer.Luck--;
                            break;
                        case 1:
                            ShowText(Locale.GetStr(RS.rs_YouAreTerrified));
                            fPlayer.UseEffect(EffectID.eid_Paralysis, null, InvokeMode.im_ItSelf, null);
                            break;
                        case 2:
                            {
                                ShowText(Locale.GetStr(RS.rs_DarklingGlow));
                                ShowText(Locale.GetStr(RS.rs_GlowSlowlyFades));

                                int num2 = fPlayer.Items.Count;
                                for (int i = 0; i < num2; i++) {
                                    fPlayer.Items.GetItem(i).State = ItemState.is_Cursed;
                                }
                            }
                            break;
                    }
                } else {
                    ShowText(Locale.GetStr(RS.rs_AFeelingOfSanctityComesOverYou));
                }
            } else if (deityID == GlobalVars.cid_Heimdall) {
                if (isSacrilege) {
                } else {
                    ShowText(Locale.GetStr(RS.rs_BlueFlameBurst));
                    ShowText(Locale.GetStr(RS.rs_VioletNimbus));
                    fPlayer.UseEffect(EffectID.eid_Speedup, null, InvokeMode.im_ItSelf, null);
                }
            } else if (deityID == GlobalVars.cid_Odin) {
                if (isSacrilege) {
                    ShowText(Locale.GetStr(RS.rs_OdinThunderbolt));
                    fPlayer.ApplyDamage(fPlayer.Level * 5, DamageKind.dkPhysical, null, Locale.GetStr(RS.rs_OdinWrath));
                    if (fPlayer.State != CreatureState.csDead) {
                        ShowText(Locale.GetStr(RS.rs_BoltWarpsYourBody));
                        fPlayer.Constitution /= 2;
                        fPlayer.Strength /= 2;
                    }
                } else {
                    ShowText(Locale.GetStr(RS.rs_OdinThunderclap));
                    int lev = value % 3;
                    int num3 = RandomHelper.GetRandom(lev);
                    switch (num3) {
                        case 0:
                            ShowText(Locale.GetStr(RS.rs_NothingHappens));
                            break;
                        case 1:
                            ShowText(Locale.GetStr(RS.rs_ObjectAtYourFeet));
                            ShowText(Locale.GetStr(RS.rs_UseMyGiftWisely));
                            break;
                        case 2:
                            ShowText(Locale.GetStr(RS.rs_EternityGong));
                            break;
                        case 3:
                            ShowText(Locale.GetStr(RS.rs_AnInvisibleChoirSings));
                            ShowText(Locale.GetStr(RS.rs_CongratulationsMortal));
                            ShowText(Locale.GetStr(RS.rs_InReturnForThyService));
                            ShowText(Locale.GetStr(RS.rs_YouAscendToTheStatusDemigod));
                            fPlayer.UseEffect(EffectID.eid_Invulnerable, null, InvokeMode.im_ItSelf, null);
                            break;
                    }
                }
            } else if (deityID == GlobalVars.cid_Thor) {
                if (isSacrilege) {
                    ShowText(Locale.GetStr(RS.rs_YouAreZorchedByGodsfire));
                    if (fPlayer.HPCur > 0) {
                        ShowText(Locale.GetStr(RS.rs_FireBurnsYourExperience));
                        fPlayer.Level = 0;
                        fPlayer.Experience = 0;
                        fPlayer.HPCur = fPlayer.Constitution;
                        fPlayer.HPMax = fPlayer.Constitution;
                        ShowText(Locale.GetStr(RS.rs_YourPowerIsReducedByBlast));
                        fPlayer.MPCur = 0;
                        fPlayer.MPMax = 0;
                    }
                } else {
                }
            } else if (deityID == GlobalVars.cid_Tyr) {
                if (isSacrilege) {
                    ShowText(Locale.GetStr(RS.rs_VileCreatureThouDurstCallUponMe));
                    ShowText(Locale.GetStr(RS.rs_WalkNoMorePerversionOfNature));
                    ShowText(Locale.GetStr(RS.rs_YouFeelLikeYouAreFallingApart));
                } else {
                    ShowText(Locale.GetStr(RS.rs_YouAreSurroundedByAShimmeringLight));
                }
            } else if (deityID == GlobalVars.cid_Balder) {
                if (isSacrilege) {
                    ShowText(Locale.GetStr(RS.rs_YourArdentPleaIsIgnored));
                    ShowText(Locale.GetStr(RS.rs_YouFeelAshamed));
                    fPlayer.Experience = (int)(fPlayer.Experience - ((long)Math.Round(fPlayer.Experience / 4.0)));
                } else {
                    ShowText(Locale.GetStr(RS.rs_AShaftOfLucentRadianceLancesDownFromTheHeavens));
                    ShowText(Locale.GetStr(RS.rs_YouFeelUplifted));
                    fPlayer.Experience = fPlayer.Experience + value;
                    fPlayer.UseEffect(EffectID.eid_Cure, null, InvokeMode.im_ItSelf, null);
                    fPlayer.UseEffect(EffectID.eid_Luck, null, InvokeMode.im_ItSelf, null);
                }
            } else if (deityID == GlobalVars.cid_Surtr) {
                if (isSacrilege) {
                    ShowText(Locale.GetStr(RS.rs_YouAreBlastedByShaftOfBlackFire));
                    fPlayer.ApplyDamage(fPlayer.Level * 5, DamageKind.dkPhysical, null, Locale.GetStr(RS.rs_SurtrAnger));
                    if (fPlayer.HPCur > 0) {
                        ShowText(Locale.GetStr(RS.rs_YouAreWreathedInCloudsOfSmoke));
                        for (int i = fPlayer.Items.Count - 1; i >= 0; i--) {
                            if (fPlayer.Items.GetItem(i).State == ItemState.is_Blessed) {
                                fPlayer.DeleteItem(fPlayer.Items.GetItem(i));
                            }
                        }
                        ShowText(Locale.GetStr(RS.rs_YouFeelSurtrBlackHand));
                        fPlayer.Constitution /= 4;
                    }
                } else {
                    ShowText(Locale.GetStr(RS.rs_YouLearntToSeeBeautyInFireAndDestruction));
                }
            } else if (deityID == GlobalVars.cid_Loki) {
                if (isSacrilege) {
                    ShowText(Locale.Format(RS.rs_DeityRejectsYourSacrifice, new object[]{ deity.Name }));
                    ShowText(Locale.GetStr(RS.rs_SufferInfidel));
                    fPlayer.Luck -= 5;
                }
            } else if (deityID == GlobalVars.cid_Hela) {
                if (isSacrilege) {
                    ShowText(Locale.GetStr(RS.rs_YouAreZappedByDarkMoonbeams));
                    fPlayer.ApplyDamage(fPlayer.Level * 5, DamageKind.dkPhysical, null, Locale.GetStr(RS.rs_HelaMalice));
                    if (fPlayer.HPCur > 0) {
                        ShowText(Locale.GetStr(RS.rs_BeamsLeachYouMagicalPower));
                        fPlayer.MPCur = fPlayer.MPMax / 5;
                        fPlayer.Constitution /= 5;
                        fPlayer.ClearSkills();
                    }
                }
            } else {
                /*if (deityID == GlobalVars.cid_Jormungand || deityID == GlobalVars.cid_Fenrir || deityID == GlobalVars.cid_Garm) {
                }*/
            }

            // "You feel that <god> is angry with you."
        }
    }
}
