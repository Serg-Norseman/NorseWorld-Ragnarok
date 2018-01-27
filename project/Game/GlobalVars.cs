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

using NWR.Core;
using NWR.Database;
using NWR.GUI;

namespace NWR.Game
{
    public static class GlobalVars
    {
        public static NWMainWindow nwrWin;
        public static NWGameSpace nwrGame;
        public static NWDatabase nwrDB;

        public static IntList dbLayers;
        public static IntList dbCreatures;
        public static IntList dbItems;
        public static IntList dbPotions;
        public static IntList dbRings;
        public static IntList dbScrolls;
        public static IntList dbWands;
        public static IntList dbAmulets;
        public static IntList dbArmor;
        public static IntList dbFoods;
        public static IntList dbTools;
        public static IntList dbWeapon;
        public static IntList dbScripts;
        public static IntList dbKnowledges;

        public static int cid_Viking;
        public static int cid_Alchemist;
        public static int cid_Blacksmith;
        public static int cid_Conjurer;
        public static int cid_Sage;
        public static int cid_Woodsman;
        public static int cid_Merchant;
        public static int cid_Eitri;
        public static int cid_Hela;
        public static int cid_Thokk;
        public static int cid_Balder;
        public static int cid_Heimdall;
        public static int cid_Thor;
        public static int cid_Tyr;
        public static int cid_Freyr;
        public static int cid_Odin;
        public static int cid_Jormungand;
        public static int cid_Garm;
        public static int cid_Loki;
        public static int cid_Surtr;
        public static int cid_Fenrir;
        public static int cid_Emanon;
        public static int cid_Nidhogg;
        public static int cid_Anxarcule;
        public static int cid_KonrRig;
        public static int cid_Plog;
        public static int cid_Vanseril;
        public static int cid_Gulveig;
        public static int cid_Hobjoi;
        public static int cid_Gymir;
        public static int cid_Scyld;
        public static int cid_Uorik;
        public static int cid_Vidur;
        public static int cid_Harbard;
        public static int cid_Guardsman;
        public static int cid_Jarl;
        public static int cid_Werewolf;
        public static int cid_Raven;
        public static int cid_Aspenth;
        public static int cid_Norseman;
        public static int cid_Agnar;
        public static int cid_Haddingr;
        public static int cid_Ketill;
        public static int cid_Oldman;
        public static int cid_ShopKeeper;
        public static int cid_IvyCreeper;
        public static int cid_Snake;

        public static int Layer_Midgard;
        public static int Layer_Svartalfheim1;
        public static int Layer_Svartalfheim2;
        public static int Layer_Svartalfheim3;
        public static int Layer_Asgard;
        public static int Layer_MimerWell;
        public static int Layer_Vanaheim;
        public static int Layer_Niflheim;
        public static int Layer_Armory;
        public static int Layer_GrynrHalls;
        public static int Layer_Crossroads;
        public static int Layer_Muspelheim;
        public static int Layer_Wasteland;
        public static int Layer_GodsFortress;

        public static int Land_Crossroads;
        public static int Land_Valhalla;
        public static int Land_Ocean;
        public static int Land_Bifrost;
        public static int Land_Vigrid;
        public static int Land_Niflheim;
        public static int Land_GiollRiver;
        public static int Land_SlaeterSea;
        public static int Land_VidRiver;
        public static int Land_Caves;
        public static int Land_DeepCaves;
        public static int Land_GreatCaves;
        public static int Land_Crypt;
        public static int Land_Bazaar;
        public static int Land_Nidavellir;
        public static int Land_Forest;
        public static int Land_Village;
        public static int Land_Jotenheim;
        public static int Land_Wasteland;
        public static int Land_Muspelheim;
        public static int Land_Alfheim;
        public static int Land_MimerRealm;
        public static int Land_MimerWell;
        public static int Land_GodsFortress;
        public static int Land_GrynrHalls;
        public static int Land_Temple;
        public static int Land_Armory;
        public static int Land_Vanaheim;

        public static int Field_Bifrost;
        public static int Field_Bazaar;

        public static int iid_Coin;
        public static int iid_DeadBody;
        public static int iid_Arrow;
        public static int iid_Bolt;
        public static int iid_LongBow;
        public static int iid_CrossBow;
        public static int iid_Ingot;
        public static int iid_DwarvenArm;
        public static int iid_Mimming;
        public static int iid_Mjollnir;
        public static int iid_Gjall;
        public static int iid_Gungnir;
        public static int iid_SoulTrapping_Ring;
        public static int iid_Rnd_Scroll;
        public static int iid_Rnd_Potion;
        public static int iid_Rnd_Armor;
        public static int iid_Rnd_Weapon;
        public static int iid_Rnd_Wand;
        public static int iid_Rnd_Food;
        public static int iid_Rnd_Ring;
        public static int iid_Rnd_Amulet;
        public static int iid_Rnd_Tool;
        public static int iid_Anvil;
        public static int iid_PlatinumAnvil;
        public static int iid_Vial;
        public static int iid_Flask;
        public static int iid_Rnd_NatureObject;
        public static int iid_Tongs;
        public static int iid_Wand_Fire;
        public static int iid_PickAxe;
        public static int iid_Mummy;
        public static int iid_Ocarina;
        public static int iid_GlassOcarina;
        public static int iid_Stylus;
        public static int iid_Ring_Delusion;
        public static int iid_LazlulRope;
        public static int iid_GreenStone;
        public static int iid_Lodestone;
        public static int iid_Ring_Protection;
        public static int iid_DiamondNeedle;
        public static int iid_Amulet_Eternal_Life;
        public static int iid_Amulet_SertrudEye;
        public static int iid_Skidbladnir;
        public static int iid_Flute;
        public static int iid_Torch;

        public static bool Debug_ShowFPS = false;
        public static bool Debug_DevMode = true;
        public static bool Debug_Divinity = true;
        public static bool Debug_Fly = false;
        public static bool Debug_Swim = false;
        public static bool Debug_TestWorldGen = false;
        public static bool Debug_Freeze = false;
        public static bool Debug_Fury = true;
        public static bool Debug_CheckCrPtr = false;
        public static bool Debug_CheckScent = false;
        public static bool Debug_Silent = false;
        public static bool Debug_ManaRegen = false;
    }
}
