/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014, 2020 by Serg V. Zhdanovskih.
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
using System.Collections.Generic;
using System.IO;
using System.Text;
using BSLib;
using NWR.Creatures;
using NWR.Creatures.Brain;
using NWR.Creatures.Brain.Goals;
using NWR.Creatures.Specials;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.Game.Ghosts;
using NWR.Game.Quests;
using NWR.Game.Story;
using NWR.Game.Types;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Core.Brain;
using ZRLib.Engine;
using ZRLib.Grammar;
using ZRLib.Map;

namespace NWR.Game
{
    public sealed class NWGameSpace : GameSpace, IHost
    {
        public const int SAVEFILE_PLAYER = 1;
        public const int SAVEFILE_TERRAINS = 2;
        public const int SAVEFILE_JOURNAL = 3;

        private const int TurnSeconds = 20;
        private const int CastleSize = 5;
        private static readonly ExtRect Castle = ExtRect.Create(1, 1, 3, 4);
        private static readonly string[] Defenders;
        private static readonly string[] Attackers;
        private static readonly string[] Undeads;

        public static readonly FileVersion RGF_Version = new FileVersion(1, 21);
        public static readonly char[] RGP_Sign = new char[]{ 'R', 'G', 'P' };
        public static readonly char[] RGT_Sign = new char[]{ 'R', 'G', 'T' };

        private NWDatabase fDB;
        private ExtList<object> fEventsQueue;
        private int fFileIndex;
        private List<NWLayer> fLayers;
        private NamesLib fNameLib;
        private Player fPlayer;
        private NWDateTime fTime;
        private TurnState fTurnState;
        private Journal fJournal;
        private List<Quest> fQuests;

        public ExtPoint MimerWellPos;
        public NWCreature rt_Jormungand;
        //public TCreature rt_Raven;

        public bool BifrostCollapsed;
        // todo: saving this state
        public bool IsRagnarok;

        static NWGameSpace()
        {
            Defenders = new string[]{ "Halcyon", "Roc", "Valkyrie", "Dwarf", "Edgewort" };
            Attackers = new string[] {
                "Nidslacr",
                "HelDragon",
                "ElleGiant",
                "LowerDwarf",
                "Ramapith",
                "Migdnart",
                "RockGiant",
                "Minion",
                "HillGiant",
                "Magician",
                "BorgonVile",
                "Wizard",
                "Shadow",
                "Guardian"
            };
            Undeads = new string[]{ "Shade", "Spirit", "Ghost", "Shadow", "Wight", "Wraith" };

            RegisterSerializables();
        }

        public NWGameSpace(object owner)
            : base(owner)
        {
            GlobalVars.nwrGame = this;

            try {
                fEventsQueue = new ExtList<object>(true);
                fFileIndex = -1;
                fTime = new NWDateTime();

                InitData();

                fJournal = new Journal();
                fQuests = new List<Quest>();

                fPlayer = new Player(this, null);

                fLayers = new List<NWLayer>();
                int num = GlobalVars.dbLayers.Count;
                for (int i = 0; i < num; i++) {
                    fLayers.Add(new NWLayer(this, GlobalVars.dbLayers[i]));
                }

                fNameLib = new NamesLib();
                fNameLib.InitNorseDic();

                PrepareWares();

                fTurnState = TurnState.Wait;
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.Create(): " + ex.Message);
            }
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                try {
                    fNameLib.Dispose();

                    fPlayer.LeaveField();

                    int num = fLayers.Count;
                    for (int i = 0; i < num; i++) {
                        fLayers[i].Dispose();
                    }

                    fLayers.Clear();
                    fLayers = null;

                    fPlayer.Dispose();

                    fQuests = null;
                    fJournal.Dispose();

                    DoneData();
                    fEventsQueue.Dispose();
                } catch (Exception ex) {
                    Logger.Write("NWGameSpace.Dispose(): " + ex.Message);
                }
            }
            base.Dispose(disposing);
        }

        public int FileIndex
        {
            get {
                return fFileIndex;
            }
        }

        public int LayersCount
        {
            get {
                return fLayers.Count;
            }
        }

        public NWLayer GetLayer(int index)
        {
            return fLayers[index];
        }

        public NamesLib NameLib
        {
            get {
                return fNameLib;
            }
        }

        public Player Player
        {
            get {
                return fPlayer;
            }
        }

        public NWDateTime Time
        {
            get {
                return fTime;
            }
        }

        public TurnState TurnState
        {
            get {
                return fTurnState;
            }
            set {
                fTurnState = value;
            }
        }

        public Journal Journal
        {
            get {
                return fJournal;
            }
        }

        public int QuestsCount
        {
            get {
                return fQuests.Count;
            }
        }

        public Quest GetQuest(int index)
        {
            return fQuests[index];
        }

        private void ClearVolatiles()
        {
            int num = GlobalVars.nwrDB.EntriesCount;
            for (int i = 0; i < num; i++) {
                DataEntry entry = GlobalVars.nwrDB.GetEntry(i);
                if (entry is VolatileEntry) {
                    ((VolatileEntry)entry).RuntimeState = VolatileState.None;
                }
            }
        }

        private void DoneData()
        {
            GlobalVars.nwrDB.Dispose();
        }

        private void InitData()
        {
            try {
                fDB = new NWDatabase();
                fDB.LoadXML("RDatabase.xml");
                GlobalVars.nwrDB = fDB;

                GlobalVars.dbScripts = new IntList();
                GlobalVars.dbLayers = new IntList();
                GlobalVars.dbCreatures = new IntList();
                GlobalVars.dbItems = new IntList();
                GlobalVars.dbPotions = new IntList();
                GlobalVars.dbRings = new IntList();
                GlobalVars.dbScrolls = new IntList();
                GlobalVars.dbWands = new IntList();
                GlobalVars.dbAmulets = new IntList();
                GlobalVars.dbArmor = new IntList();
                GlobalVars.dbFoods = new IntList();
                GlobalVars.dbTools = new IntList();
                GlobalVars.dbWeapon = new IntList();
                GlobalVars.dbKnowledges = new IntList();

                int num = GlobalVars.nwrDB.EntriesCount;
                for (int i = 0; i < num; i++) {
                    DataEntry entry = GlobalVars.nwrDB.GetEntry(i);
                    if (entry != null) {
                        switch (entry.Kind) {
                            case DataEntry.ek_Item:
                                {
                                    GlobalVars.dbItems.Add(entry.GUID);
                                    ItemEntry eItem = (ItemEntry)entry;
                                    if (!eItem.Meta) {
                                        switch (eItem.ItmKind) {
                                            case ItemKind.ik_Armor:
                                            case ItemKind.ik_Shield:
                                            case ItemKind.ik_Helmet:
                                            case ItemKind.ik_Clothing:
                                            case ItemKind.ik_HeavyArmor:
                                            case ItemKind.ik_MediumArmor:
                                            case ItemKind.ik_LightArmor:
                                                GlobalVars.dbArmor.Add(eItem.GUID);
                                                break;

                                            case ItemKind.ik_Food:
                                                GlobalVars.dbFoods.Add(eItem.GUID);
                                                break;

                                            case ItemKind.ik_Potion:
                                                GlobalVars.dbPotions.Add(eItem.GUID);
                                                break;

                                            case ItemKind.ik_Ring:
                                                GlobalVars.dbRings.Add(eItem.GUID);
                                                break;

                                            case ItemKind.ik_Tool:
                                                GlobalVars.dbTools.Add(eItem.GUID);
                                                break;

                                            case ItemKind.ik_Wand:
                                                GlobalVars.dbWands.Add(eItem.GUID);
                                                break;

                                            case ItemKind.ik_BluntWeapon:
                                            case ItemKind.ik_ShortBlade:
                                            case ItemKind.ik_LongBlade:
                                            case ItemKind.ik_Spear:
                                            case ItemKind.ik_Axe:
                                            case ItemKind.ik_Bow:
                                            case ItemKind.ik_CrossBow:
                                            case ItemKind.ik_Projectile:
                                                GlobalVars.dbWeapon.Add(eItem.GUID);
                                                break;

                                            case ItemKind.ik_Scroll:
                                                GlobalVars.dbScrolls.Add(eItem.GUID);
                                                break;

                                            case ItemKind.ik_Amulet:
                                                GlobalVars.dbAmulets.Add(eItem.GUID);
                                                break;

                                            case ItemKind.ik_Misc:
                                                // dummy
                                                break;
                                        }
                                    }
                                    break;
                                }

                            case DataEntry.ek_Creature:
                                GlobalVars.dbCreatures.Add(entry.GUID);
                                break;

                            case DataEntry.ek_Layer:
                                GlobalVars.dbLayers.Add(entry.GUID);
                                break;

                            case DataEntry.ek_EventHandler:
                                GlobalVars.dbScripts.Add(entry.GUID);
                                break;

                            case DataEntry.ek_Information:
                                GlobalVars.dbKnowledges.Add(entry.GUID);
                                break;
                        }
                    }
                }

                PrepareEntries();
                PreparePrefixes();
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.initData(): " + ex.Message);
                throw ex;
            }
        }

        public bool TimeStop
        {
            get {
                return (fPlayer.Effects.FindEffectByID(EffectID.eid_TimeStop) != null);
            }
        }

        private void DoGameTurn()
        {
            try {
                if (GlobalVars.nwrWin.GameState != GameState.gsWorldGen) {
                    // process game time
                    int day = fTime.Day;
                    fTime.Tick((int)(Math.Round(((double)TurnSeconds * (fPlayer.Speed / 10.0)))));
                    if (day != fTime.Day) {
                        fJournal.StoreTime(fTime);
                    }

                    fPlayer.DoTurn();
                    NWField playerField = fPlayer.CurrentField;

                    bool timeStop = TimeStop;
                    if (!timeStop) {
                        PrepareFieldTurn(playerField, false);
                        PrepareDeads(playerField, fPlayer.Items, fPlayer.Location);
                    }

                    /* If Ragnarok is already go, but the player is not in Asgard - must further process the fields of Asgard.
                     * Otherwise, if he in Asgard - layer has been processed.
                     * TimeStop does not affect. */
                    if (IsRagnarok && fPlayer.LayerID != GlobalVars.Layer_Asgard) {
                        NWField fld = GetField(GlobalVars.Layer_Asgard, 0, 0);
                        PrepareFieldTurn(fld, true);

                        fld = GetField(GlobalVars.Layer_Asgard, 1, 0);
                        PrepareFieldTurn(fld, true);
                    }

                    if (fPlayer.State == CreatureState.Dead) {
                        GlobalVars.nwrWin.DoEvent(EventID.event_Dead, null, null, null);
                    }

                    if (fPlayer.Turn % 200 == 0) {
                        PrepareRaven(playerField);
                    }

                    CheckRagnarok();
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.doGameTurn(): " + ex.Message);
            }
        }

        private void CheckRagnarok()
        {
            try {
                if (IsRagnarok) {
                    bool isGodsLose = GetVolatileState(GlobalVars.cid_Heimdall) == VolatileState.Destroyed;
                    isGodsLose = (isGodsLose && GetVolatileState(GlobalVars.cid_Thor) == VolatileState.Destroyed);
                    isGodsLose = (isGodsLose && GetVolatileState(GlobalVars.cid_Tyr) == VolatileState.Destroyed);
                    isGodsLose = (isGodsLose && GetVolatileState(GlobalVars.cid_Freyr) == VolatileState.Destroyed);
                    isGodsLose = (isGodsLose && GetVolatileState(GlobalVars.cid_Odin) == VolatileState.Destroyed);

                    bool isGodsWin = (GetVolatileState(GlobalVars.cid_Jormungand) == VolatileState.Destroyed);
                    isGodsWin = (isGodsWin && GetVolatileState(GlobalVars.cid_Garm) == VolatileState.Destroyed);
                    isGodsWin = (isGodsWin && GetVolatileState(GlobalVars.cid_Loki) == VolatileState.Destroyed);
                    isGodsWin = (isGodsWin && GetVolatileState(GlobalVars.cid_Surtr) == VolatileState.Destroyed);
                    isGodsWin = (isGodsWin && GetVolatileState(GlobalVars.cid_Fenrir) == VolatileState.Destroyed);

                    if (isGodsWin) {
                        DoEvent(EventID.event_Victory, null, null, null);
                    } else if (isGodsLose) {
                        DoEvent(EventID.event_Defeat, null, null, null);
                    } else {
                        // not finished
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.checkRagnarok(): " + ex.Message);
            }
        }

        public void DoRagnarok()
        {
            if (!IsRagnarok) {
                IsRagnarok = true;
                GlobalVars.nwrWin.ShowText(fPlayer, BaseLocale.GetStr(RS.rs_RagnarokAppear), new LogFeatures(LogFeatures.lfDialog));

                FinalDisplacement(GlobalVars.cid_Heimdall, false, GlobalVars.Land_Vigrid);
                FinalDisplacement(GlobalVars.cid_Thor, false, GlobalVars.Land_Vigrid);
                FinalDisplacement(GlobalVars.cid_Tyr, false, GlobalVars.Land_Vigrid);
                FinalDisplacement(GlobalVars.cid_Freyr, false, GlobalVars.Land_Vigrid);
                FinalDisplacement(GlobalVars.cid_Odin, false, GlobalVars.Land_Vigrid);
                FinalDisplacement(GlobalVars.cid_Jormungand, false, GlobalVars.Land_Vigrid);
                FinalDisplacement(GlobalVars.cid_Garm, true, GlobalVars.Land_Vigrid);
                FinalDisplacement(GlobalVars.cid_Loki, true, GlobalVars.Land_Vigrid);
                FinalDisplacement(GlobalVars.cid_Surtr, true, GlobalVars.Land_Vigrid);
                FinalDisplacement(GlobalVars.cid_Fenrir, true, GlobalVars.Land_Vigrid);

                FinalDisplacement(GlobalVars.cid_Emanon, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Nidhogg, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Anxarcule, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Hela, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_KonrRig, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Plog, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Vanseril, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Gulveig, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Hobjoi, true, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Gymir, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Scyld, true, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Uorik, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Vidur, false, GlobalVars.Land_Valhalla);
                FinalDisplacement(GlobalVars.cid_Harbard, false, GlobalVars.Land_Valhalla);

                int aCnt = RandomHelper.GetBoundedRnd(350, 480);
                for (int i = 1; i <= aCnt; i++) {
                    int k = RandomHelper.GetBoundedRnd(0, Defenders.Length - 1);
                    int id = GlobalVars.nwrDB.FindEntryBySign(Defenders[k]).GUID;
                    if (id >= 0) {
                        AddCreatureEx(GlobalVars.Layer_Asgard, 0, 0, -1, -1, id);
                    }
                }

                aCnt = RandomHelper.GetBoundedRnd(300, 400);
                for (int i = 1; i <= aCnt; i++) {
                    int k = RandomHelper.GetBoundedRnd(0, Attackers.Length - 1);
                    int id = GlobalVars.nwrDB.FindEntryBySign(Attackers[k]).GUID;
                    if (id >= 0 && CanCreate(id)) {
                        AddCreatureEx(GlobalVars.Layer_Asgard, 0, 0, -1, -1, id);
                    }
                }
            }
        }

        private void PrepareRaven(NWField playerField)
        {
            if (GlobalVars.Debug_Freeze || (playerField.LandEntry.Flags.HasIntersect(LandFlags.lsIsDungeon, LandFlags.lsIsCave)) || fPlayer.InWater) {
                return;
            }

            ExtPoint fldCoords = playerField.Coords;
            AddCreatureEx(fPlayer.LayerID, fldCoords.X, fldCoords.Y, -1, -1, GlobalVars.cid_Raven);

            /*if (this.rt_Raven == null) {
                this.rt_Raven = this.addCreature(this.fPlayer.LayerID, fldCoords.X, fldCoords.Y, -1, -1, rGlobals.cid_Raven);
            } else {
                this.rt_Raven.TransferTo(this.fPlayer.LayerID, fldCoords.X, fldCoords.Y, -1, -1, rData.MapArea, true, false);
            }*/
        }

        public DayTime DayTime
        {
            get {
                return fTime.DayTime;
            }
        }

        public short GetTileBrightness(NWField field, BaseTile tile, bool localView)
        {
            short result;

            bool seen = tile.HasState(BaseTile.TS_SEEN);
            if (seen || localView) {
                result = 255;
            } else {
                DayTime dt = field.Dark ? DayTime.dt_Midnight : DayTime;
                float brightnessFactor = GameTime.DayTimes[(int)dt].Brightness;
                result = (short)(255 * brightnessFactor);
            }
            return result;
        }

        public bool IsCreatureVisible(NWCreature creature, BaseTile tile)
        {
            bool result = true;
            try {
                result = (creature.Equals(fPlayer) || GlobalVars.Debug_Divinity || (tile.HasState(BaseTile.TS_SEEN) && fPlayer.CheckVisible(creature)) || fPlayer.Effects.FindEffectByID(EffectID.eid_SoulSeeking) != null);
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.isCreatureVisible(): " + ex.Message);
            }
            return result;
        }

        private void LoadVolatiles(BinaryReader stream, FileVersion version)
        {
            int count = StreamUtils.ReadInt(stream);
            for (int i = 0; i < count; i++) {
                int id = StreamUtils.ReadInt(stream);
                VolatileState ves = (VolatileState)StreamUtils.ReadByte(stream);
                DataEntry entry = GetDataEntry(id);
                if (entry is VolatileEntry) {
                    ((VolatileEntry)entry).RuntimeState = ves;
                }
            }
        }

        private void SaveVolatiles(BinaryWriter stream, FileVersion version)
        {
            int count = 0;

            int num = GlobalVars.nwrDB.EntriesCount;
            for (int i = 0; i < num; i++) {
                DataEntry entry = GlobalVars.nwrDB.GetEntry(i);
                if (entry is VolatileEntry && ((VolatileEntry)entry).RuntimeState != VolatileState.None) {
                    count++;
                }
            }

            StreamUtils.WriteInt(stream, count);

            int num2 = GlobalVars.nwrDB.EntriesCount;
            for (int i = 0; i < num2; i++) {
                DataEntry entry = GlobalVars.nwrDB.GetEntry(i);
                if (entry is VolatileEntry && ((VolatileEntry)entry).RuntimeState != VolatileState.None) {
                    StreamUtils.WriteInt(stream, entry.GUID);
                    StreamUtils.WriteByte(stream, (byte)((VolatileEntry)entry).RuntimeState);
                }
            }
        }

        private void PrepareDeads(NWField field, ItemsList List, ExtPoint rp)
        {
            try {
                for (int i = List.Count - 1; i >= 0; i--) {
                    Item item = List.GetItem(i);
                    if (item.CLSID == GlobalVars.iid_DeadBody) {
                        NWCreature creature = (NWCreature)item.Contents.GetItem(0);
                        creature.IncTurn();

                        CreatureEntry entry = creature.Entry;
                        if (creature.Turn == 0) {
                            if (entry.Respawn) {
                                if (rp.X == -1 && rp.Y == -1) {
                                    rp = item.Location;
                                }
                                RespawnDeadBody(field, rp, item);
                            } else {
                                if (!entry.CorpsesPersist) {
                                    if (creature.Mercenary) {
                                        ((LeaderBrain)fPlayer.Brain).RemoveMember(creature);
                                    }
                                    List.Delete(i);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.prepareDeads(): " + ex.Message);
            }
        }

        public string DayTimeInfo
        {
            get {
                int dtv = (int)DayTime;
                string rs = BaseLocale.GetStr(StaticData.dbDayTimeRS[dtv]);
                return fTime.ToString(true, false) + " (" + rs + ")";
            }
        }


        public NWCreature AddCreatureEx(int layerID, int fx, int fy, int px, int py, int creatureID)
        {
            NWCreature result;
            try {
                NWCreature res;
                if (creatureID == GlobalVars.cid_Snake) {
                    res = new Snake(this, null, creatureID, true, true);
                } else {
                    if (creatureID == GlobalVars.cid_IvyCreeper) {
                        res = new IvyCreeper(this, null, creatureID, true, true);
                    } else {
                        res = new NWCreature(this, null, creatureID, true, true);
                    }
                }

                res.TransferTo(layerID, fx, fy, px, py, StaticData.MapArea, true, false);

                CreatureEntry crEntry = (CreatureEntry)GetDataEntry(creatureID);
                if (crEntry.Flags.Contains(CreatureFlags.esUnique)) {
                    SetVolatileState(creatureID, VolatileState.Created);
                }

                result = res;
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.AddCreature(): " + ex.Message);
                result = null;
            }

            return result;
        }

        public Item AddItemEx(int layerID, int fx, int fy, int px, int py, int itemID)
        {
            if (itemID < 0) {
                return null;
            }

            Item result = null;

            NWField fld = GetField(layerID, fx, fy);
            if (fld != null) {
                if (px < 0 || py < 0) {
                    ExtPoint pt = fld.SearchFreeLocation();
                    px = pt.X;
                    py = pt.Y;
                }

                result = new Item(this, fld);
                result.CLSID = itemID;
                result.SetPos(px, py);
                result.GenCount();
                fld.Items.Add(result, false);
            }

            return result;
        }

        public NWCreature AddTownsman(NWField field, ExtRect village, int creatureID)
        {
            NWCreature townsman = new NWCreature(this, null, creatureID, true, true);
            townsman.TransferTo(field.Layer.EntryID, field.Coords.X, field.Coords.Y, -1, -1, village, true, false);
            ((WarriorBrain)townsman.Brain).SetAreaGuardGoal(village);
            return townsman;
        }

        public bool CanCreate(int id)
        {
            return GetVolatileState(id) == VolatileState.None;
        }

        public void ClearEvents(object aBy)
        {
            for (int i = fEventsQueue.Count - 1; i >= 0; i--) {
                GameEvent @event = (GameEvent)fEventsQueue[i];
                if (@event.Receiver.Equals(aBy) || @event.Sender.Equals(aBy)) {
                    fEventsQueue.Delete(i);
                }
            }
        }

        public void DiagnoseCreature(NWCreature cr)
        {
            try {
                StringList strs = new StringList();
                try {
                    strs.Clear();
                    strs.Add(BaseLocale.GetStr(RS.rs_Creature) + ": @" + cr.Name + "@");
                    strs.Add(BaseLocale.GetStr(RS.rs_HP) + ": " + Convert.ToString(cr.HPCur) + "/" + Convert.ToString(cr.HPMax_Renamed));
                    strs.Add(BaseLocale.GetStr(RS.rs_Armor) + ": " + Convert.ToString(cr.ArmorClass));
                    strs.Add(BaseLocale.GetStr(RS.rs_Speed) + ": " + Convert.ToString(cr.Speed));
                    strs.Add(BaseLocale.GetStr(RS.rs_ToHit) + ": " + Convert.ToString(cr.ToHit));
                    strs.Add(BaseLocale.GetStr(RS.rs_Attacks) + ": " + Convert.ToString(cr.Attacks));
                    strs.Add(BaseLocale.GetStr(RS.rs_Damage) + ": " + Convert.ToString(cr.DBMin) + "-" + Convert.ToString(cr.DBMax));

                    if (GlobalVars.Debug_DevMode) {
                        int num = cr.Brain.GoalsCount;
                        for (int i = 0; i < num; i++) {
                            GoalEntity gl = cr.Brain.GetGoal(i);
                            if (gl.Kind == GoalKind.gk_EnemyChase) {
                                strs.Add("  chase " + ((NWCreature)((EnemyChaseGoal)gl).Enemy).GetDeclinableName(Number.nSingle, Case.cAccusative));
                            } else {
                                if (gl.Kind == GoalKind.gk_EnemyEvade) {
                                    strs.Add("  evade of " + ((NWCreature)((EnemyEvadeGoal)gl).Enemy).GetDeclinableName(Number.nSingle, Case.cAccusative));
                                } else {
                                    strs.Add("  " + gl.GetType().Name + ": " + Convert.ToString((double)gl.Value));
                                }
                            }
                        }
                    }

                    GlobalVars.nwrWin.ShowMessage(strs.Text);
                } finally {
                    strs.Dispose();
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.diagnoseCreature(): " + ex.Message);
            }
        }

        public void ProcessGameStep()
        {
            try {
                switch (fTurnState) {
                    case TurnState.Wait:
                        break;

                    case TurnState.Done:
                        TurnState = TurnState.Wait;
                        DoGameTurn();
                        break;

                    case TurnState.Skip:
                        BaseSystem.Sleep(1000);
                        DoGameTurn();
                        break;
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.processGameStep(): " + ex.Message);
            }
        }

        public void DoPlayerAction(CreatureAction action, int aExt)
        {
            NWField fld = fPlayer.CurrentField;
            switch (action) {
                case CreatureAction.caWait:
                    {
                        fPlayer.WaitTurn();
                        break;
                    }

                case CreatureAction.caMove:
                    {
                        int dir = (aExt);
                        if (dir != Directions.DtZenith) {
                            if (dir != Directions.DtNadir) {
                                int NewX = fPlayer.PosX + Directions.Data[dir].DX;
                                int NewY = fPlayer.PosY + Directions.Data[dir].DY;
                                fPlayer.MoveTo(NewX, NewY);
                            } else {
                                fPlayer.MoveToDown();
                            }
                        } else {
                            fPlayer.MoveToUp();
                        }
                        break;
                    }

                case CreatureAction.caAttackMelee:
                    {
                        int dir = (aExt);
                        int NewX = fPlayer.PosX + Directions.Data[dir].DX;
                        int NewY = fPlayer.PosY + Directions.Data[dir].DY;

                        NWCreature enemy = (NWCreature)fld.FindCreature(NewX, NewY);
                        if (enemy != null) {
                            fPlayer.AttackTo(AttackKind.Melee, enemy, null, null);
                        } else {
                            DoPlayerAction(CreatureAction.caMove, aExt);
                        }
                        break;
                    }

                case CreatureAction.caAttackShoot:
                    {
                        int dir = (aExt);
                        fPlayer.ShootToDir(dir);
                        break;
                    }

                case CreatureAction.caItemPickup:
                    {
                        Item item = (Item)Instance.FindEntity(aExt);
                        if (item == null) {
                            throw new Exception("Assertion failure #2");
                        }
                        fPlayer.PickupItem(item);
                        break;
                    }

                case CreatureAction.caItemDrop:
                    {
                        Item item = (Item)Instance.FindEntity(aExt);
                        if (item == null) {
                            throw new Exception("Assertion failure #3");
                        }
                        fPlayer.DropItem(item);
                        break;
                    }

                case CreatureAction.caItemWear:
                    {
                        Item item = (Item)Instance.FindEntity(aExt);
                        if (item == null) {
                            throw new Exception("Assertion failure #4");
                        }
                        if (fPlayer.CanBeUsed(item)) {
                            fPlayer.WearItem(item);
                        }
                        break;
                    }

                case CreatureAction.caItemRemove:
                    {
                        Item item = (Item)Instance.FindEntity(aExt);
                        if (item == null) {
                            throw new Exception("Assertion failure #5");
                        }
                        fPlayer.RemoveItem(item);
                        break;
                    }

                case CreatureAction.caPickupAll:
                    {
                        fPlayer.PickupAll();
                        break;
                    }

                case CreatureAction.caItemUse:
                    {
                        Item item = (Item)Instance.FindEntity(aExt);
                        if (item == null) {
                            throw new Exception("Assertion failure #6");
                        }
                        fPlayer.UseItem(item, null);
                        break;
                    }

                case CreatureAction.caSpellUse:
                    {
                        fPlayer.UseSpell((EffectID)(aExt), null);
                        break;
                    }

                case CreatureAction.caSkillUse:
                    {
                        SkillID sk = (SkillID)(aExt);
                        fPlayer.UseSkill(sk, null);
                        break;
                    }
            }

            if (fTurnState == TurnState.Wait) {
                TurnState = TurnState.Done;
            }
        }

        public static string GetSaveFile(int kind, int index)
        {
            string result = NWResourceManager.GetAppPath() + "save/rgame_" + Convert.ToString(index);

            switch (kind) {
                case SAVEFILE_PLAYER:
                    result += ".rgp";
                    break;
                case SAVEFILE_TERRAINS:
                    result += ".rgt";
                    break;
                case SAVEFILE_JOURNAL:
                    result += ".rgj";
                    break;
            }

            return result;
        }

        private static void CheckPath()
        {
            string path = NWResourceManager.GetAppPath() + "/save";
            if (!Directory.Exists(path)) {
                Directory.CreateDirectory(path);
            }
        }

        public void EraseGame(int index)
        {
            try {
                if (Directory.Exists(GetSaveFile(SAVEFILE_PLAYER, index)))
                    Directory.Delete(GetSaveFile(SAVEFILE_PLAYER, index), true);
                else
                    File.Delete(GetSaveFile(SAVEFILE_PLAYER, index));
                if (Directory.Exists(GetSaveFile(SAVEFILE_TERRAINS, index)))
                    Directory.Delete(GetSaveFile(SAVEFILE_TERRAINS, index), true);
                else
                    File.Delete(GetSaveFile(SAVEFILE_TERRAINS, index));
                if (Directory.Exists(GetSaveFile(SAVEFILE_JOURNAL, index)))
                    Directory.Delete(GetSaveFile(SAVEFILE_JOURNAL, index), true);
                else
                    File.Delete(GetSaveFile(SAVEFILE_JOURNAL, index));
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.eraseGame(): " + ex.Message);
            }
        }

        public static void LoadPlayer(int index, Player player)
        {
            try {
                string fName = GetSaveFile(SAVEFILE_PLAYER, index);
                FileStream aStream = new FileStream(fName, FileMode.Open);
                using (var dis = new BinaryReader(aStream)) {
                    FileHeader header = new FileHeader();
                    header.Read(dis);
                    
                    player.LoadFromStream(dis, header.Version);
                    
                    if (dis.BaseStream.Length - dis.BaseStream.Position == 0) {
                        Logger.Write("playerLoad(): ok");
                    } else {
                        Logger.Write("playerLoad(): fail");
                    }
                }
            } catch (IOException ex) {
                Logger.Write("NWGameSpace.loadPlayer.io(): " + ex.Message);
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.loadPlayer(): " + ex.Message);
                throw ex;
            }
        }

        public void LoadGame(int index)
        {
            CheckPath();

            try {
                fPlayer.LeaveField();
                LoadPlayer(index, fPlayer);

                int total = 0;
                int num = fLayers.Count;
                for (int i = 0; i < num; i++) {
                    NWLayer layer = fLayers[i];
                    total += layer.H * layer.W;
                }

                GlobalVars.nwrWin.ProgressInit(total);
                GlobalVars.nwrWin.ProgressLabel(BaseLocale.GetStr(RS.rs_LevelsLoading) + " (0/" + Convert.ToString(fLayers.Count) + ")");

                try {
                    FileStream fileStream = new FileStream(GetSaveFile(SAVEFILE_TERRAINS, index), FileMode.Open);
                    using (var dis = new BinaryReader(fileStream)) {
                        FileHeader header = new FileHeader();
                        header.Read(dis);
                        
                        if (header.Version.Revision >= 4) {
                            fTime.LoadFromStream(dis, header.Version);
                        } else {
                            ResetTime(fTime);
                        }
                        
                        int num2 = fLayers.Count;
                        for (int i = 0; i < num2; i++) {
                            fLayers[i].LoadFromStream(dis, header.Version);
                            GlobalVars.nwrWin.ProgressLabel(BaseLocale.GetStr(RS.rs_LevelsLoading) + " (" + Convert.ToString(i + 1) + "/" + Convert.ToString(num2) + ")");
                        }
                        
                        if (header.Version.Revision >= 3) {
                            if (header.Version.Revision <= 14) {
                                IntList oldExtincted = new IntList();
                                oldExtincted.LoadFromStream(dis, header.Version);
                                
                                int num3 = oldExtincted.Count;
                                for (int i = 0; i < num3; i++) {
                                    int id = oldExtincted[i];
                                    DataEntry entry = GetDataEntry(id);
                                    if (entry is VolatileEntry) {
                                        ((VolatileEntry)entry).RuntimeState = VolatileState.Extincted;
                                    }
                                }
                            } else {
                                LoadVolatiles(dis, header.Version);
                            }
                        }
                        
                        if (dis.BaseStream.Length - dis.BaseStream.Position == 0) {
                            Logger.Write("terrainsLoad(): ok");
                        } else {
                            Logger.Write("terrainsLoad(): fail");
                        }
                    }
                } catch (IOException e) {
                    Logger.Write("NWGameSpace.loadGame.io(): " + e.Message);
                    //throw e;
                }

                fJournal.Load(GetSaveFile(SAVEFILE_JOURNAL, index));

                int num4 = fLayers.Count;
                for (int i = 0; i < num4; i++) {
                    NWLayer layer = fLayers[i];

                    for (int fy = 0; fy < layer.H; fy++) {
                        for (int fx = 0; fx < layer.W; fx++) {
                            NWField fld = layer.GetField(fx, fy);
                            GameScreen scr = fld.LandEntry.Splash;
                            var srcRec = StaticData.dbScreens[(int)scr];
                            if (fld.Visited && srcRec.Status == ScreenStatus.Once) {
                                srcRec.Status = ScreenStatus.Already;
                            }
                        }
                    }
                }

                fPlayer.TransferTo(fPlayer.LayerID, fPlayer.Field.X, fPlayer.Field.Y, fPlayer.PosX, fPlayer.PosY, StaticData.MapArea, true, false);
                GlobalVars.nwrWin.ProgressDone();
                fFileIndex = index;

                GlobalVars.nwrWin.PlaySound("game_load.ogg", SoundEngine.sk_Sound, -1, -1);
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.loadGame(): " + ex.Message);
                throw ex;
            }
        }

        public void SaveGame(int index)
        {
            CheckPath();

            try {
                try {
                    FileHeader header = new FileHeader();
                    FileStream fileStream = new FileStream(GetSaveFile(SAVEFILE_PLAYER, index), FileMode.CreateNew);
                    using (var dos = new BinaryWriter(fileStream)) {
                        Array.Copy(RGP_Sign, 0, header.Sign, 0, 3);
                        header.Version = RGF_Version.Clone();
                        header.Write(dos);
                        fPlayer.SaveToStream(dos, header.Version);
                    }

                    int total = 0;
                    int num = fLayers.Count;
                    for (int i = 0; i < num; i++) {
                        total += (fLayers[i].H * fLayers[i].W);
                    }

                    GlobalVars.nwrWin.ProgressInit(total);

                    fileStream = new FileStream(GetSaveFile(SAVEFILE_TERRAINS, index), FileMode.CreateNew);
                    using (var dos = new BinaryWriter(fileStream)) {
                        Array.Copy(RGT_Sign, 0, header.Sign, 0, 3);
                        header.Version = RGF_Version.Clone();
                        header.Write(dos);

                        fTime.SaveToStream(dos, header.Version);

                        int num2 = fLayers.Count;
                        for (int i = 0; i < num2; i++) {
                            fLayers[i].SaveToStream(dos, header.Version);
                            GlobalVars.nwrWin.ProgressLabel(BaseLocale.GetStr(RS.rs_LevelsSaving) + " (" + Convert.ToString(i + 1) + "/" + Convert.ToString(num2) + ")");
                        }

                        SaveVolatiles(dos, header.Version);
                    }

                    fJournal.Save(GetSaveFile(SAVEFILE_JOURNAL, index));

                    fFileIndex = index;
                } finally {
                    GlobalVars.nwrWin.ProgressDone();
                }

                GlobalVars.nwrWin.PlaySound("game_save.ogg", SoundEngine.sk_Sound, -1, -1);
            } catch (IOException ex) {
                Logger.Write("NWGameSpace.saveGame.IO(): " + ex.Message);
                //throw ex;
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.saveGame(): " + ex.Message);
                throw ex;
            }
        }

        public string GetEventMessage(EventID eventID, object sender, object receiver, object data)
        {
            string result = "";

            try {
                switch (eventID) {
                    case EventID.event_LandEnter:
                        {
                            Player player = (Player)sender;
                            LandEntry eLand = (LandEntry)data;
                            if (player.LandID != eLand.GUID) {
                                result = string.Format(BaseLocale.GetStr(RS.rs_EnterInLand), eLand.GetNounDeclension(Number.nSingle, Case.cAccusative));
                                player.LandID = eLand.GUID;
                            }
                            break;
                        }

                    case EventID.event_Intro:
                        {
                            Player player = (Player)sender;
                            result = string.Format(BaseLocale.GetStr(RS.rs_IntroMsg), Convert.ToString((int)Time.Year), GetDataEntry(player.CLSID).Name + " " + player.Name);
                            break;
                        }

                    case EventID.event_Attack:
                        {
                            NWCreature _sender = (NWCreature)sender;
                            NWCreature _enemy = (NWCreature)receiver;
                            if (_sender.IsPlayer) {
                                string dummy = _enemy.GetDeclinableName(Number.nSingle, Case.cAccusative);
                                string verb = StaticData.GetVerbRes(RS.rs_Verb_Attack, _sender.Sex, Grammar.VOICE_ACTIVE);
                                result = _sender.Name + " " + verb + " " + dummy + ".";
                            } else {
                                string dummy = _sender.GetDeclinableName(Number.nSingle, Case.cInstrumental);
                                string verb = StaticData.GetVerbRes(RS.rs_Verb_Attack, _enemy.Sex, Grammar.VOICE_PASSIVE);
                                result = _enemy.Name + " " + verb + " " + dummy + ".";
                            }
                            break;
                        }

                    case EventID.event_Killed:
                    case EventID.event_Wounded:
                        {
                            NWCreature _sender = (NWCreature)sender;
                            if (eventID != EventID.event_Killed) {
                                if (eventID == EventID.event_Wounded) {
                                    string verb = StaticData.GetVerbRes(RS.rs_Verb_Wound, _sender.Sex, Grammar.VOICE_PASSIVE);
                                    result = _sender.Name + " " + verb + ".";
                                }
                            } else {
                                string verb = StaticData.GetVerbRes(RS.rs_Verb_Kill, _sender.Sex, Grammar.VOICE_PASSIVE);
                                result = _sender.Name + " " + verb + ".";
                            }

                            if (eventID == EventID.event_Killed && fPlayer.Equals(data)) {
                                fJournal.Killed(_sender.CLSID);
                            }
                            break;
                        }
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.getEventMessage(): " + ex.Message);
                throw ex;
            }

            return result;
        }

        public NWField GetField(int layerID, int fX, int fY)
        {
            NWLayer layer = GetLayerByID(layerID);
            return ((layer == null) ? null : layer.GetField(fX, fY));
        }

        public NWLayer GetLayerByID(int aID)
        {
            foreach (NWLayer layer in fLayers) {
                if (layer.EntryID == aID) {
                    return layer;
                }
            }
            return null;
        }

        public int RndExtincted
        {
            get {
                IntList list = new IntList();
                int num = GlobalVars.nwrDB.EntriesCount;
                for (int k = 0; k < num; k++) {
                    DataEntry entry = GlobalVars.nwrDB.GetEntry(k);
                    if (entry is VolatileEntry && ((VolatileEntry)entry).RuntimeState == VolatileState.Extincted) {
                        list.Add(entry.GUID);
                    }
                }
                
                int i = RandomHelper.GetRandom(list.Count);
                int result = list[i];
                
                return result;
            }
        }

        public NWField GetRndFieldByLands(params int[] lands)
        {
            if (lands == null) {
                return null;
            }

            List<NWField> fsList = new List<NWField>();

            int num = fLayers.Count;
            for (int i = 0; i < num; i++) {
                NWLayer layer = fLayers[i];

                for (int fy = 0; fy < layer.H; fy++) {
                    for (int fx = 0; fx < layer.W; fx++) {
                        NWField fld = layer.GetField(fx, fy);

                        if (lands.IndexOf(fld.LandID) >= 0) {
                            fsList.Add(fld);
                        }
                    }
                }
            }

            if (fsList.Count > 0) {
                int i = RandomHelper.GetRandom(fsList.Count);
                return fsList[i];
            }

            return null;
        }

        public short GetTeachablePrice(int teachableIndex, int curLev)
        {
            int price = 0;
            int id = StaticData.dbTeachable[teachableIndex].Id;
            TeachableKind kind = StaticData.dbTeachable[teachableIndex].Kind;

            switch (kind) {
                case TeachableKind.Ability:
                    price = 1000;
                    break;

                case TeachableKind.Skill:
                    {
                        var skRec = StaticData.dbSkills[id];
                        EffectID eff = skRec.Effect;

                        int num = GlobalVars.dbItems.Count;
                        for (int i = 0; i < num; i++) {
                            ItemEntry eItem = (ItemEntry)GetDataEntry(GlobalVars.dbItems[i]);
                            ItemEntry.EffectEntry[] effects = eItem.Effects;
                            if (((effects != null) ? effects.Length : 0) > 0 && eItem.Effects[0].EffID == eff) {
                                price = (int)eItem.Price;
                                break;
                            }
                        }
                    }
                    break;
            }

            if (price == 0) {
                price = 1000;
            }

            LeaderBrain party = (LeaderBrain)fPlayer.Brain;
            float f = (1.0f + (float)party.Members.Count / 10.0f) * (1.0f + (curLev + 1) / 5.0f);
            return (short)(Math.Round(price * f));
        }

        public StringList VisitedLands
        {
            get {
                StringList result = new StringList();
                IntList lands = new IntList();
                try {
                    int num = fLayers.Count;
                    for (int i = 0; i < num; i++) {
                        NWLayer layer = fLayers[i];
                        
                        int num2 = layer.H;
                        for (int fy = 0; fy < num2; fy++) {
                            int num3 = layer.W;
                            for (int fx = 0; fx < num3; fx++) {
                                NWField fld = layer.GetField(fx, fy);
                                int id = fld.LandID;
                                if (fld.Visited && lands.IndexOf(id) < 0) {
                                    lands.Add(id);
                                }
                            }
                        }
                    }
                    
                    int num4 = lands.Count;
                    for (int i = 0; i < num4; i++) {
                        int id = lands[i];
                        result.AddObject(GetDataEntry(id).Name, id);
                    }
                } finally {
                }
                result.Sort();
                return result;
            }
        }

        public VolatileState GetVolatileState(int id)
        {
            VolatileState result;

            DataEntry entry = GetDataEntry(id);
            if (entry is VolatileEntry) {
                result = ((VolatileEntry)entry).RuntimeState;
            } else {
                result = VolatileState.None;
            }

            return result;
        }

        private void FinalDisplacement(int cid, bool aCreate, int aLand)
        {
            int fx;
            int fy;
            int px;
            int py;

            if (aLand == GlobalVars.Land_Valhalla) {
                fx = 0;
                fy = 0;
                px = -1;
                py = -1;
            } else {
                if (aLand != GlobalVars.Land_Vigrid) {
                    return;
                }
                fx = 1;
                fy = 0;
                px = RandomHelper.GetBoundedRnd(29, 68);
                py = RandomHelper.GetBoundedRnd(3, StaticData.FieldHeight - 4);
            }

            NWCreature cr = FindCreature(cid);
            if (cr != null) {
                cr.TransferTo(GlobalVars.Layer_Asgard, fx, fy, px, py, StaticData.MapArea, true, true);
            } else {
                if (aCreate) {
                    AddCreatureEx(GlobalVars.Layer_Asgard, fx, fy, px, py, cid);
                }
            }
        }

        public NWCreature FindCreature(int aID)
        {
            int num = fLayers.Count;
            for (int i = 0; i < num; i++) {
                NWLayer layer = fLayers[i];

                int num2 = layer.H;
                for (int y = 0; y < num2; y++) {
                    int num3 = layer.W;
                    for (int x = 0; x < num3; x++) {
                        GameEntity cr = layer.GetField(x, y).Creatures.FindByCLSID(aID);
                        if (cr != null) {
                            return ((NWCreature)((cr is NWCreature) ? cr : null));
                        }
                    }
                }
            }

            return null;
        }

        private void GenForest()
        {
            //int fx = AuxUtils.BoundedRnd(3, 4); // 2, 3
            int fy = RandomHelper.GetBoundedRnd(3, 4);

            NWField fldFB = GetField(GlobalVars.Layer_Midgard, 3, fy);
            ExtRect area = fldFB.AreaRect;
            area.Inflate(-7, -4);

            Building house = new Building(this, fldFB);
            if (house.Build(1, 4, 5, area)) {
                fldFB.Features.Add(house);

                ExtRect brt = house.Area.Clone();
                int x = RandomHelper.GetBoundedRnd(brt.Left, brt.Right);
                int y = RandomHelper.GetBoundedRnd(brt.Top, brt.Bottom);
                NWCreature oldman = AddCreatureEx(fldFB.Layer.EntryID, fldFB.Coords.X, fldFB.Coords.Y, x, y, GlobalVars.cid_Oldman);
                house.Holder = oldman;
                ((SentientBrain)oldman.Brain).SetAreaGuardGoal(brt);
            } else {
                house.Dispose();
                house = null;
            }

            ExtPoint bd = ExtPoint.Empty;
            if (house != null) {
                Door door = house.Doors[0];
                bd = fldFB.GetLayerTileCoords(door.X, door.Y);
            }

            NWLayer layer = GetLayerByID(GlobalVars.Layer_Midgard);

            if (!bd.IsEmpty) {
                NWField fldV = GetField(GlobalVars.Layer_Midgard, 2, 2);
                Village village = fldV.FindVillage();

                ExtPoint vd = ExtPoint.Empty;
                int max = layer.Width;
                for (int i = 0; i < village.Gates.Count; i++) {
                    ExtPoint gpt = village.Gates[i];
                    gpt = fldV.GetLayerTileCoords(gpt.X, gpt.Y);
                    int d = MathHelper.Distance(bd.X, bd.Y, gpt.X, gpt.Y);
                    if (d < max) {
                        vd = gpt;
                        max = d;
                    }
                }

                if (!vd.IsEmpty) {
                    UniverseBuilder.Gen_Road(layer, bd.X, bd.Y, vd.X, vd.Y, layer.AreaRect, PlaceID.pid_RoadMask2);
                }
            }

            // ancient roads
            GenMidgardRoad(layer, 2, 4, 4, 3);
            GenMidgardRoad(layer, 4, 2, 3, 4);
        }

        private void GenMidgardRoad(NWLayer layer, int fx1, int fy1, int fx2, int fy2)
        {
            NWField fld1 = GetField(GlobalVars.Layer_Midgard, fx1, fy1);
            ExtPoint pt1 = fld1.GetLayerTileCoords(fld1.SearchFreeLocation());

            NWField fld2 = GetField(GlobalVars.Layer_Midgard, fx2, fy2);
            ExtPoint pt2 = fld2.GetLayerTileCoords(fld2.SearchFreeLocation());

            UniverseBuilder.Gen_Road(layer, pt1.X, pt1.Y, pt2.X, pt2.Y, layer.AreaRect, PlaceID.pid_RoadMask2);
        }

        private void GenGiollRiver()
        {
            // gate from Field_Midgard56 [63, 8] to Field_Niflheim20, -1, -1
            NWField fld = GetField(GlobalVars.Layer_Midgard, 5, 6);
            fld.AddGate(PlaceID.pid_StairsDown, 63, 8, GlobalVars.Layer_Niflheim, new ExtPoint(2, 0), new ExtPoint(-1, -1));

            ExtPoint pt = new ExtPoint(63, 8);

            NWCreature h = fld.AddCreature(pt.X, pt.Y, GlobalVars.cid_Harbard);
            ((SentientBrain)h.Brain).SetPointGuardGoal(pt);

            UniverseBuilder.Gen_GiollFog(fld, pt.X, pt.Y);
        }

        private void GenJotenheim()
        {
            // generate the "Gymir Castle"
            NWField fld = GetField(GlobalVars.Layer_Midgard, 5, 5);
            int aX = RandomHelper.GetBoundedRnd(5, StaticData.FieldWidth - 5);
            int aY = RandomHelper.GetBoundedRnd(2, StaticData.FieldHeight - 8);
            ExtRect rtx = ExtRect.Create(0, 0, 4, 5);

            for (int yy = 0; yy <= rtx.Bottom; yy++) {
                for (int xx = 0; xx <= rtx.Right; xx++) {
                    NWTile tile = (NWTile)fld.GetTile(aX + xx, aY + yy);
                    if (rtx.IsBorder(xx, yy)) {
                        tile.Background = PlaceID.pid_Grass;
                        if (yy == 5) {
                            tile.Foreground = PlaceID.pid_Tree;
                        } else {
                            tile.Foreground = PlaceID.pid_Mountain;
                        }
                    } else {
                        tile.Background = PlaceID.pid_Floor;
                        if (Castle.IsBorder(xx, yy)) {
                            tile.Foreground = NWField.GetBuildPlaceKind(xx, yy, Castle, false);
                        }
                    }
                }
            }

            fld.GetTile(aX + 2, aY + 4).Foreground = PlaceID.pid_DoorS;
            fld.AddCreature(aX + 2, aY + 3, GlobalVars.cid_Gymir);

            fld = GetField(GlobalVars.Layer_Midgard, 5, 3);
            UniverseBuilder.Build_Jotenheim_Jarnvidr(fld);
        }

        private void GenNiflheim()
        {
            AddCreatureEx(GlobalVars.Layer_Niflheim, 0, 0, -1, -1, GlobalVars.cid_KonrRig);
            AddCreatureEx(GlobalVars.Layer_Niflheim, 0, 1, -1, -1, GlobalVars.cid_Plog);
            AddCreatureEx(GlobalVars.Layer_Niflheim, 0, 2, -1, -1, GlobalVars.cid_Gulveig);
            AddCreatureEx(GlobalVars.Layer_Niflheim, 1, 0, -1, -1, GlobalVars.cid_Vanseril);
            AddCreatureEx(GlobalVars.Layer_Niflheim, 1, 1, -1, -1, GlobalVars.cid_Emanon);
            AddCreatureEx(GlobalVars.Layer_Niflheim, 1, 2, -1, -1, GlobalVars.cid_Nidhogg);
            AddCreatureEx(GlobalVars.Layer_Niflheim, 2, 1, -1, -1, GlobalVars.cid_Anxarcule);
            AddCreatureEx(GlobalVars.Layer_Niflheim, 2, 2, -1, -1, GlobalVars.cid_Hela);
        }

        private void GenNidavellir()
        {
            // generate the "Uorik Castle" and gates
            NWField fldNidavellir = GetField(GlobalVars.Layer_Midgard, 4, 6);
            NWField fldArmory = GetField(GlobalVars.Layer_Armory, 0, 0);

            int aX = RandomHelper.GetBoundedRnd(5, StaticData.FieldWidth - 5 - CastleSize);
            int aY = RandomHelper.GetBoundedRnd(4, StaticData.FieldHeight - 4 - CastleSize);
            fldNidavellir.FillBorder(aX, aY, aX + 4, aY + 4, PlaceID.pid_Stalagmite, true);

            ExtPoint nidPt = new ExtPoint(aX + 2, aY + 2);
            ExtPoint armPt = fldArmory.SearchFreeLocation();

            fldNidavellir.AddGate(PlaceID.pid_StairsDown, nidPt.X, nidPt.Y, GlobalVars.Layer_Armory, new ExtPoint(0, 0), armPt);
            fldArmory.AddGate(PlaceID.pid_StairsUp, armPt.X, armPt.Y, GlobalVars.Layer_Midgard, new ExtPoint(4, 6), nidPt);
            AddCreatureEx(GlobalVars.Layer_Midgard, 4, 6, aX + 2, aY + 3, GlobalVars.cid_Uorik);
        }

        private void GenAsgard()
        {
            // gate from Field_Asgard00 [40, 11] to Field_GodsFortress00, 40, 11
            NWField fld = GetField(GlobalVars.Layer_Asgard, 0, 0);
            fld.AddGate(PlaceID.pid_StairsUp, 40, 11, GlobalVars.Layer_GodsFortress, new ExtPoint(0, 0), new ExtPoint(40, 11));

            // gate from Field_GodsFortress00 [40, 11] to Field_Asgard00, 40, 11
            fld = GetField(GlobalVars.Layer_GodsFortress, 0, 0);
            fld.AddGate(PlaceID.pid_StairsDown, 40, 11, GlobalVars.Layer_Asgard, new ExtPoint(0, 0), new ExtPoint(40, 11));
        }

        private void GenLink_Caves2_Caves3_3()
        {
            NWField svart2 = GetField(GlobalVars.Layer_Svartalfheim2, 1, 2);
            ExtPoint sv2pt = svart2.SearchFreeLocation();

            NWField svart3 = GetField(GlobalVars.Layer_Svartalfheim3, 1, 2);
            ExtPoint sv3pt = svart3.SearchFreeLocation();

            svart2.AddGate(PlaceID.pid_HoleDown, sv2pt.X, sv2pt.Y, GlobalVars.Layer_Svartalfheim3, new ExtPoint(1, 2), sv3pt);
            svart3.AddGate(PlaceID.pid_HoleUp, sv3pt.X, sv3pt.Y, GlobalVars.Layer_Svartalfheim2, new ExtPoint(1, 2), sv2pt);

            svart3.AddItem(sv3pt.X, sv3pt.Y, GlobalVars.nwrDB.FindEntryBySign("LazlulRope").GUID);
            svart3.AddItem(sv3pt.X, sv3pt.Y, GlobalVars.nwrDB.FindEntryBySign("Amulet_Infravision").GUID);
        }

        private void GenLink_Caves2_Caves3_2()
        {
            ExtPoint fp3 = new ExtPoint(0, 0);

            NWField fldSvart2 = GetField(GlobalVars.Layer_Svartalfheim2, fp3.X, fp3.Y);
            ExtPoint sv2pt = fldSvart2.SearchFreeLocation();

            NWField fldSvart3 = GetField(GlobalVars.Layer_Svartalfheim3, fp3.X, fp3.Y);
            ExtPoint sv3pt = fldSvart3.SearchFreeLocation();

            fldSvart2.AddGate(PlaceID.pid_GStairsDown, sv2pt.X, sv2pt.Y, GlobalVars.Layer_Svartalfheim3, new ExtPoint(fp3.X, fp3.Y), sv3pt);
            fldSvart3.AddGate(PlaceID.pid_GStairsUp, sv3pt.X, sv3pt.Y, GlobalVars.Layer_Svartalfheim2, new ExtPoint(fp3.X, fp3.Y), sv2pt);
        }

        private void GenLink_Caves2_Caves3()
        {
            ExtPoint fp3 = new ExtPoint(RandomHelper.GetBoundedRnd(1, 2), RandomHelper.GetBoundedRnd(0, 1));

            NWField fldSvart2 = GetField(GlobalVars.Layer_Svartalfheim2, fp3.X, fp3.Y);
            ExtPoint sv2pt = fldSvart2.SearchFreeLocation();

            NWField fldSvart3 = GetField(GlobalVars.Layer_Svartalfheim3, fp3.X, fp3.Y);
            ExtPoint sv3pt = fldSvart3.SearchFreeLocation();

            fldSvart2.AddGate(PlaceID.pid_StairsDown, sv2pt.X, sv2pt.Y, GlobalVars.Layer_Svartalfheim3, new ExtPoint(fp3.X, fp3.Y), sv3pt);
            fldSvart3.AddGate(PlaceID.pid_StairsUp, sv3pt.X, sv3pt.Y, GlobalVars.Layer_Svartalfheim2, new ExtPoint(fp3.X, fp3.Y), sv2pt);
        }

        private void GenLink_Caves1_Caves2()
        {
            ExtPoint fp3 = new ExtPoint(RandomHelper.GetBoundedRnd(0, 2), RandomHelper.GetBoundedRnd(0, 2));

            NWField fldSvart1 = GetField(GlobalVars.Layer_Svartalfheim1, fp3.X, fp3.Y);
            ExtPoint sv1pt = fldSvart1.SearchFreeLocation();

            NWField fldSvart2 = GetField(GlobalVars.Layer_Svartalfheim2, fp3.X, fp3.Y);
            ExtPoint sv2pt = fldSvart2.SearchFreeLocation();

            fldSvart1.AddGate(PlaceID.pid_StairsDown, sv1pt.X, sv1pt.Y, GlobalVars.Layer_Svartalfheim2, new ExtPoint(fp3.X, fp3.Y), sv2pt);
            fldSvart2.AddGate(PlaceID.pid_StairsUp, sv2pt.X, sv2pt.Y, GlobalVars.Layer_Svartalfheim1, new ExtPoint(fp3.X, fp3.Y), sv1pt);
        }

        private void GenLink_Midgard_GrynrHalls()
        {
            NWField fldMidgard35 = GetField(GlobalVars.Layer_Midgard, 3, 5);
            ExtPoint midPt = fldMidgard35.SearchFreeLocation();

            NWField fldGrynrHalls = GetField(GlobalVars.Layer_GrynrHalls, 0, 0);
            ExtPoint ghPt = fldGrynrHalls.SearchFreeLocation();

            fldMidgard35.AddGate(PlaceID.pid_StairsDown, midPt.X, midPt.Y, GlobalVars.Layer_GrynrHalls, new ExtPoint(0, 0), ghPt);
            fldGrynrHalls.AddGate(PlaceID.pid_StairsUp, ghPt.X, ghPt.Y, GlobalVars.Layer_Midgard, new ExtPoint(3, 5), midPt);
        }

        private void GenLink_MimerRealm_Caves1()
        {
            NWField fldMimerRealm = GetField(GlobalVars.Layer_Midgard, 5, 2);
            ExtPoint mrpt = fldMimerRealm.SearchFreeLocation();

            NWField fldSvart1 = GetField(GlobalVars.Layer_Svartalfheim1, 2, 0);
            ExtPoint sv1pt = fldSvart1.SearchFreeLocation();

            fldMimerRealm.AddGate(PlaceID.pid_VortexStrange, mrpt.X, mrpt.Y, GlobalVars.Layer_Svartalfheim1, new ExtPoint(2, 0), sv1pt);
            fldSvart1.AddGate(PlaceID.pid_VortexStrange, sv1pt.X, sv1pt.Y, GlobalVars.Layer_Midgard, new ExtPoint(5, 2), mrpt);
        }

        private void GenLink_MimerRealm_MimerWell()
        {
            NWField fldMimerRealm = GetField(GlobalVars.Layer_Midgard, 5, 2);
            MimerWellPos = fldMimerRealm.SearchFreeLocation();

            NWCreature aspenth = AddCreatureEx(GlobalVars.Layer_MimerWell, 0, 0, -1, -1, GlobalVars.cid_Aspenth);
            ExtPoint aspPt = aspenth.Location;

            fldMimerRealm.AddGate(PlaceID.pid_Well, MimerWellPos.X, MimerWellPos.Y, GlobalVars.Layer_MimerWell, new ExtPoint(0, 0), aspPt);
        }

        private void GenLink_SlaeterSea_Caves1()
        {
            NWField fldSlaeterSea = GetField(GlobalVars.Layer_Midgard, 5, 7);
            ExtPoint seaPt = fldSlaeterSea.SearchFreeLocation();

            ExtPoint fp2 = new ExtPoint(RandomHelper.GetBoundedRnd(0, 2), RandomHelper.GetBoundedRnd(0, 2));
            NWField fldSvart1 = GetField(GlobalVars.Layer_Svartalfheim1, fp2.X, fp2.Y);
            ExtPoint sv1pt = fldSvart1.SearchFreeLocation();

            fldSlaeterSea.AddGate(PlaceID.pid_Vortex, seaPt.X, seaPt.Y, GlobalVars.Layer_Svartalfheim1, fp2, sv1pt);
            fldSvart1.AddGate(PlaceID.pid_Vortex, sv1pt.X, sv1pt.Y, GlobalVars.Layer_Midgard, new ExtPoint(5, 7), seaPt);
        }

        private void GenLink_MidgardForest_SlaeterSea()
        {
            NWField fldMidgard44 = GetField(GlobalVars.Layer_Midgard, 4, 4);
            ExtPoint mid44pt = fldMidgard44.SearchFreeLocation();

            NWField fldMidgard57 = GetField(GlobalVars.Layer_Midgard, 5, 7);
            ExtPoint mid57pt = fldMidgard57.SearchFreeLocation();

            fldMidgard44.AddGate(PlaceID.pid_Vortex, mid44pt.X, mid44pt.Y, GlobalVars.Layer_Midgard, new ExtPoint(5, 7), mid57pt);
            fldMidgard57.AddGate(PlaceID.pid_Vortex, mid57pt.X, mid57pt.Y, GlobalVars.Layer_Midgard, new ExtPoint(4, 4), mid44pt);

            fldMidgard57.AddCreature(-1, -1, GlobalVars.nwrDB.FindEntryBySign("Norseman").GUID);
            fldMidgard57.AddItem(26, 6, GlobalVars.iid_Skidbladnir);
        }

        private void GenLink_MidgardForest_Caves1()
        {
            NWField fldMidgard44 = GetField(GlobalVars.Layer_Midgard, 4, 4);
            ExtPoint mid44pt = fldMidgard44.SearchFreeLocation();

            NWField fldSvart1 = GetField(GlobalVars.Layer_Svartalfheim1, 1, 2);
            ExtPoint sv1pt = fldSvart1.SearchFreeLocation();

            fldMidgard44.AddGate(PlaceID.pid_StairsDown, mid44pt.X, mid44pt.Y, GlobalVars.Layer_Svartalfheim1, new ExtPoint(1, 2), sv1pt);
            fldSvart1.AddGate(PlaceID.pid_StairsUp, sv1pt.X, sv1pt.Y, GlobalVars.Layer_Midgard, new ExtPoint(4, 4), mid44pt);

            fldMidgard44.AddCreature(-1, -1, GlobalVars.cid_Thokk);
        }

        private void GenWorld()
        {
            try {
                try {
                    int stages = 0;

                    int num = fLayers.Count;
                    for (int i = 0; i < num; i++) {
                        LayerEntry layerEntry = fLayers[i].Entry;
                        stages += layerEntry.H * layerEntry.W * 3;
                    }

                    GlobalVars.nwrWin.ProgressInit(stages);

                    int num2 = fLayers.Count;
                    for (int i = 0; i < num2; i++) {
                        GlobalVars.nwrWin.ProgressLabel(BaseLocale.GetStr(RS.rs_LevelsGen) + " (" + Convert.ToString(i + 1) + "/" + Convert.ToString(num2) + ")");
                        fLayers[i].InitLayer();
                        GlobalVars.nwrWin.ProgressStep();
                    }

                    GenForest();

                    GenLink_MidgardForest_Caves1();
                    GenLink_MidgardForest_SlaeterSea();
                    GenLink_SlaeterSea_Caves1();
                    GenLink_MimerRealm_MimerWell();
                    GenLink_MimerRealm_Caves1();

                    GenAsgard();
                    GenNidavellir();
                    AddCreatureEx(GlobalVars.Layer_Armory, 0, 0, -1, -1, GlobalVars.cid_Eitri);
                    AddCreatureEx(GlobalVars.Layer_Midgard, 1, 6, 46, 0, GlobalVars.cid_Heimdall);
                    GenNiflheim();
                    GenJotenheim();
                    GenGiollRiver();

                    GenLink_Midgard_GrynrHalls();
                    GenLink_Caves1_Caves2();

                    GenLink_Caves2_Caves3();
                    GenLink_Caves2_Caves3_2();
                    GenLink_Caves2_Caves3_3();

                    GenGhosts();
                } finally {
                    GlobalVars.nwrWin.ProgressDone();
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.genWorld(): " + ex.Message);
            }
        }

        private void GenGhosts()
        {
            GhostsList ghostsList = GlobalVars.nwrWin.GhostsList;

            for (int i = 0; i < ghostsList.GhostCount; i++) {
                Ghost ghost = ghostsList.GetGhost(i);

                NWCreature gst = new NWCreature(this, null);
                gst.Assign(ghost, false);
                gst.Items.Clear();

                ExtPoint pt = ghost.Location;
                gst.TransferTo(ghost.LayerID, ghost.Field.X, ghost.Field.Y, pt.X, pt.Y, StaticData.MapArea, true, false);
                gst.Ghost = true;
                gst.GhostIdx = i;

                ((BeastBrain)gst.Brain).SetPointGuardGoal(pt);
            }
        }

        public void InitBegin()
        {
            try {
                fPlayer.LeaveField();

                fFileIndex = -1;
                IsRagnarok = false;
                ClearVolatiles();
                ResetTime(fTime);

                fJournal.StoreTime(fTime);

                GenWorld();
                GenMainQuests();

                fPlayer.CLSID = GlobalVars.cid_Viking;
                fPlayer.Name = BaseLocale.GetStr(RS.rs_Unknown);
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.InitBegin(): " + ex.Message);
            }
        }

        public void InitEnd()
        {
            try {
                NWField fld = GetField(GlobalVars.Layer_Midgard, 2, 2);
                Village village = fld.FindVillage();

                ExtRect area;
                if (village == null) {
                    area = fld.AreaRect;
                } else {
                    area = village.Area;
                }

                fPlayer.TransferTo(GlobalVars.Layer_Midgard, 2, 2, -1, -1, area, true, true);
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.InitEnd(): " + ex.Message);
            }
        }

        public void ResetTime(NWDateTime time)
        {
            time.Year = 609;
            time.Month = 7;
            time.Day = 15;
            time.Hour = 11;
            time.Minute = 45;
            time.Second = 0;
        }

        public void SelectHero(string paSign, string pName)
        {
            int id = GlobalVars.nwrDB.FindEntryBySign(paSign).GUID;
            fPlayer.InitEx(id, true, false);
            fPlayer.Name = pName;
            fPlayer.State = CreatureState.Alive;
            fPlayer.Turn = 0;
            fPlayer.SetSourceForm();

            DoEvent(EventID.event_Intro, fPlayer, null, null);
        }

        public void PrepareEntries()
        {
            GlobalVars.cid_Viking = GlobalVars.nwrDB.FindEntryBySign("Viking").GUID;
            GlobalVars.cid_Alchemist = GlobalVars.nwrDB.FindEntryBySign("Alchemist").GUID;
            GlobalVars.cid_Blacksmith = GlobalVars.nwrDB.FindEntryBySign("Blacksmith").GUID;
            GlobalVars.cid_Conjurer = GlobalVars.nwrDB.FindEntryBySign("Conjurer").GUID;
            GlobalVars.cid_Sage = GlobalVars.nwrDB.FindEntryBySign("Sage").GUID;
            GlobalVars.cid_Woodsman = GlobalVars.nwrDB.FindEntryBySign("Woodsman").GUID;
            GlobalVars.cid_Merchant = GlobalVars.nwrDB.FindEntryBySign("Merchant").GUID;
            GlobalVars.cid_Eitri = GlobalVars.nwrDB.FindEntryBySign("Eitri").GUID;
            GlobalVars.cid_Hela = GlobalVars.nwrDB.FindEntryBySign("Hela").GUID;
            GlobalVars.cid_Thokk = GlobalVars.nwrDB.FindEntryBySign("Thokk").GUID;
            GlobalVars.cid_Balder = GlobalVars.nwrDB.FindEntryBySign("Balder").GUID;
            GlobalVars.cid_Heimdall = GlobalVars.nwrDB.FindEntryBySign("Heimdall").GUID;
            GlobalVars.cid_Thor = GlobalVars.nwrDB.FindEntryBySign("Thor").GUID;
            GlobalVars.cid_Tyr = GlobalVars.nwrDB.FindEntryBySign("Tyr").GUID;
            GlobalVars.cid_Freyr = GlobalVars.nwrDB.FindEntryBySign("Freyr").GUID;
            GlobalVars.cid_Odin = GlobalVars.nwrDB.FindEntryBySign("Odin").GUID;
            GlobalVars.cid_Jormungand = GlobalVars.nwrDB.FindEntryBySign("Jormungand").GUID;
            GlobalVars.cid_Garm = GlobalVars.nwrDB.FindEntryBySign("Garm").GUID;
            GlobalVars.cid_Loki = GlobalVars.nwrDB.FindEntryBySign("Loki").GUID;
            GlobalVars.cid_Surtr = GlobalVars.nwrDB.FindEntryBySign("Surtr").GUID;
            GlobalVars.cid_Fenrir = GlobalVars.nwrDB.FindEntryBySign("Fenrir").GUID;
            GlobalVars.cid_Emanon = GlobalVars.nwrDB.FindEntryBySign("Emanon").GUID;
            GlobalVars.cid_Nidhogg = GlobalVars.nwrDB.FindEntryBySign("Nidhogg").GUID;
            GlobalVars.cid_Anxarcule = GlobalVars.nwrDB.FindEntryBySign("Anxarcule").GUID;
            GlobalVars.cid_KonrRig = GlobalVars.nwrDB.FindEntryBySign("KonrRig").GUID;
            GlobalVars.cid_Plog = GlobalVars.nwrDB.FindEntryBySign("Plog").GUID;
            GlobalVars.cid_Vanseril = GlobalVars.nwrDB.FindEntryBySign("Vanseril").GUID;
            GlobalVars.cid_Gulveig = GlobalVars.nwrDB.FindEntryBySign("Gulveig").GUID;
            GlobalVars.cid_Hobjoi = GlobalVars.nwrDB.FindEntryBySign("Hobjoi").GUID;
            GlobalVars.cid_Gymir = GlobalVars.nwrDB.FindEntryBySign("Gymir").GUID;
            GlobalVars.cid_Scyld = GlobalVars.nwrDB.FindEntryBySign("Scyld").GUID;
            GlobalVars.cid_Uorik = GlobalVars.nwrDB.FindEntryBySign("Uorik").GUID;
            GlobalVars.cid_Vidur = GlobalVars.nwrDB.FindEntryBySign("Vidur").GUID;
            GlobalVars.cid_Harbard = GlobalVars.nwrDB.FindEntryBySign("Harbard").GUID;
            GlobalVars.cid_Raven = GlobalVars.nwrDB.FindEntryBySign("Raven").GUID;
            GlobalVars.cid_Guardsman = GlobalVars.nwrDB.FindEntryBySign("Guardsman").GUID;
            GlobalVars.cid_Jarl = GlobalVars.nwrDB.FindEntryBySign("Jarl").GUID;
            GlobalVars.cid_Werewolf = GlobalVars.nwrDB.FindEntryBySign("Werewolf").GUID;
            GlobalVars.cid_Aspenth = GlobalVars.nwrDB.FindEntryBySign("Aspenth").GUID;
            GlobalVars.cid_Norseman = GlobalVars.nwrDB.FindEntryBySign("Norseman").GUID;
            GlobalVars.cid_Agnar = GlobalVars.nwrDB.FindEntryBySign("Agnar").GUID;
            GlobalVars.cid_Haddingr = GlobalVars.nwrDB.FindEntryBySign("Haddingr").GUID;
            GlobalVars.cid_Ketill = GlobalVars.nwrDB.FindEntryBySign("Ketill").GUID;
            GlobalVars.cid_Oldman = GlobalVars.nwrDB.FindEntryBySign("Oldman").GUID;
            GlobalVars.cid_ShopKeeper = GlobalVars.nwrDB.FindEntryBySign("ShopKeeper").GUID;
            GlobalVars.cid_IvyCreeper = GlobalVars.nwrDB.FindEntryBySign("IvyCreeper").GUID;
            GlobalVars.cid_Snake = GlobalVars.nwrDB.FindEntryBySign("Snake").GUID;

            GlobalVars.Layer_Midgard = GlobalVars.nwrDB.FindEntryBySign("Layer_Midgard").GUID;
            GlobalVars.Layer_Svartalfheim1 = GlobalVars.nwrDB.FindEntryBySign("Layer_Caves1").GUID;
            GlobalVars.Layer_Svartalfheim2 = GlobalVars.nwrDB.FindEntryBySign("Layer_Caves2").GUID;
            GlobalVars.Layer_Svartalfheim3 = GlobalVars.nwrDB.FindEntryBySign("Layer_Caves3").GUID;
            GlobalVars.Layer_Asgard = GlobalVars.nwrDB.FindEntryBySign("Layer_Asgard").GUID;
            GlobalVars.Layer_MimerWell = GlobalVars.nwrDB.FindEntryBySign("Layer_MimerWell").GUID;
            GlobalVars.Layer_Vanaheim = GlobalVars.nwrDB.FindEntryBySign("Layer_Vanaheim").GUID;
            GlobalVars.Layer_Niflheim = GlobalVars.nwrDB.FindEntryBySign("Layer_Niflheim").GUID;
            GlobalVars.Layer_Armory = GlobalVars.nwrDB.FindEntryBySign("Layer_Armory").GUID;
            GlobalVars.Layer_GrynrHalls = GlobalVars.nwrDB.FindEntryBySign("Layer_GrynrHalls").GUID;
            GlobalVars.Layer_Crossroads = GlobalVars.nwrDB.FindEntryBySign("Layer_Crossroads").GUID;
            GlobalVars.Layer_Muspelheim = GlobalVars.nwrDB.FindEntryBySign("Layer_Muspelheim").GUID;
            GlobalVars.Layer_Wasteland = GlobalVars.nwrDB.FindEntryBySign("Layer_Wasteland").GUID;
            GlobalVars.Layer_GodsFortress = GlobalVars.nwrDB.FindEntryBySign("Layer_GodsFortress").GUID;

            GlobalVars.Land_Crossroads = GlobalVars.nwrDB.FindEntryBySign("Land_Crossroads").GUID;
            GlobalVars.Land_Valhalla = GlobalVars.nwrDB.FindEntryBySign("Land_Valhalla").GUID;
            GlobalVars.Land_Ocean = GlobalVars.nwrDB.FindEntryBySign("Land_Ocean").GUID;
            GlobalVars.Land_Bifrost = GlobalVars.nwrDB.FindEntryBySign("Land_Bifrost").GUID;
            GlobalVars.Land_Vigrid = GlobalVars.nwrDB.FindEntryBySign("Land_Vigrid").GUID;
            GlobalVars.Land_Niflheim = GlobalVars.nwrDB.FindEntryBySign("Land_Niflheim").GUID;
            GlobalVars.Land_GiollRiver = GlobalVars.nwrDB.FindEntryBySign("Land_GiollRiver").GUID;
            GlobalVars.Land_SlaeterSea = GlobalVars.nwrDB.FindEntryBySign("Land_SlaeterSea").GUID;
            GlobalVars.Land_VidRiver = GlobalVars.nwrDB.FindEntryBySign("Land_VidRiver").GUID;
            GlobalVars.Land_Caves = GlobalVars.nwrDB.FindEntryBySign("Land_Caves").GUID;
            GlobalVars.Land_DeepCaves = GlobalVars.nwrDB.FindEntryBySign("Land_DeepCaves").GUID;
            GlobalVars.Land_GreatCaves = GlobalVars.nwrDB.FindEntryBySign("Land_GreatCaves").GUID;
            GlobalVars.Land_Crypt = GlobalVars.nwrDB.FindEntryBySign("Land_Crypt").GUID;
            GlobalVars.Land_Bazaar = GlobalVars.nwrDB.FindEntryBySign("Land_Bazaar").GUID;
            GlobalVars.Land_Nidavellir = GlobalVars.nwrDB.FindEntryBySign("Land_Nidavellir").GUID;
            GlobalVars.Land_Forest = GlobalVars.nwrDB.FindEntryBySign("Land_Forest").GUID;
            GlobalVars.Land_Village = GlobalVars.nwrDB.FindEntryBySign("Land_Village").GUID;
            GlobalVars.Land_Jotenheim = GlobalVars.nwrDB.FindEntryBySign("Land_Jotenheim").GUID;
            GlobalVars.Land_Wasteland = GlobalVars.nwrDB.FindEntryBySign("Land_Wasteland").GUID;
            GlobalVars.Land_Muspelheim = GlobalVars.nwrDB.FindEntryBySign("Land_Muspelheim").GUID;
            GlobalVars.Land_Alfheim = GlobalVars.nwrDB.FindEntryBySign("Land_Alfheim").GUID;
            GlobalVars.Land_MimerRealm = GlobalVars.nwrDB.FindEntryBySign("Land_MimerRealm").GUID;
            GlobalVars.Land_MimerWell = GlobalVars.nwrDB.FindEntryBySign("Land_MimerWell").GUID;
            GlobalVars.Land_GodsFortress = GlobalVars.nwrDB.FindEntryBySign("Land_GodsFortress").GUID;
            GlobalVars.Land_GrynrHalls = GlobalVars.nwrDB.FindEntryBySign("Land_GrynrHalls").GUID;
            GlobalVars.Land_Temple = GlobalVars.nwrDB.FindEntryBySign("Land_Temple").GUID;
            GlobalVars.Land_Armory = GlobalVars.nwrDB.FindEntryBySign("Land_Armory").GUID;
            GlobalVars.Land_Vanaheim = GlobalVars.nwrDB.FindEntryBySign("Land_Vanaheim").GUID;

            GlobalVars.Field_Bifrost = GlobalVars.nwrDB.FindEntryBySign("Field_Midgard16").GUID;
            GlobalVars.Field_Bazaar = GlobalVars.nwrDB.FindEntryBySign("Field_Caves322").GUID;

            GlobalVars.iid_Coin = GlobalVars.nwrDB.FindEntryBySign("Coin").GUID;
            GlobalVars.iid_DeadBody = GlobalVars.nwrDB.FindEntryBySign("DeadBody").GUID;
            GlobalVars.iid_Arrow = GlobalVars.nwrDB.FindEntryBySign("Arrow").GUID;
            GlobalVars.iid_Bolt = GlobalVars.nwrDB.FindEntryBySign("Bolt").GUID;
            GlobalVars.iid_LongBow = GlobalVars.nwrDB.FindEntryBySign("LongBow").GUID;
            GlobalVars.iid_CrossBow = GlobalVars.nwrDB.FindEntryBySign("CrossBow").GUID;
            GlobalVars.iid_Ingot = GlobalVars.nwrDB.FindEntryBySign("Ingot").GUID;
            GlobalVars.iid_SoulTrapping_Ring = GlobalVars.nwrDB.FindEntryBySign("Ring_SoulTrapping").GUID;
            GlobalVars.iid_PickAxe = GlobalVars.nwrDB.FindEntryBySign("PickAxe").GUID;
            GlobalVars.iid_Tongs = GlobalVars.nwrDB.FindEntryBySign("Tongs").GUID;
            GlobalVars.iid_Wand_Fire = GlobalVars.nwrDB.FindEntryBySign("Wand_Fire").GUID;
            GlobalVars.iid_Anvil = GlobalVars.nwrDB.FindEntryBySign("Anvil").GUID;
            GlobalVars.iid_PlatinumAnvil = GlobalVars.nwrDB.FindEntryBySign("PlatinumAnvil").GUID;
            GlobalVars.iid_Vial = GlobalVars.nwrDB.FindEntryBySign("Vial").GUID;
            GlobalVars.iid_Flask = GlobalVars.nwrDB.FindEntryBySign("Flask").GUID;
            GlobalVars.iid_Mummy = GlobalVars.nwrDB.FindEntryBySign("Mummy").GUID;
            GlobalVars.iid_Ocarina = GlobalVars.nwrDB.FindEntryBySign("Ocarina").GUID;
            GlobalVars.iid_GlassOcarina = GlobalVars.nwrDB.FindEntryBySign("GlassOcarina").GUID;
            GlobalVars.iid_Stylus = GlobalVars.nwrDB.FindEntryBySign("Stylus").GUID;
            GlobalVars.iid_Ring_Delusion = GlobalVars.nwrDB.FindEntryBySign("Ring_Delusion").GUID;
            GlobalVars.iid_LazlulRope = GlobalVars.nwrDB.FindEntryBySign("LazlulRope").GUID;
            GlobalVars.iid_GreenStone = GlobalVars.nwrDB.FindEntryBySign("GreenStone").GUID;
            GlobalVars.iid_Lodestone = GlobalVars.nwrDB.FindEntryBySign("Lodestone").GUID;
            GlobalVars.iid_Ring_Protection = GlobalVars.nwrDB.FindEntryBySign("Ring_Protection").GUID;
            GlobalVars.iid_DiamondNeedle = GlobalVars.nwrDB.FindEntryBySign("DiamondNeedle").GUID;
            GlobalVars.iid_Amulet_Eternal_Life = GlobalVars.nwrDB.FindEntryBySign("Amulet_Eternal_Life").GUID;
            GlobalVars.iid_Amulet_SertrudEye = GlobalVars.nwrDB.FindEntryBySign("Amulet_SertrudEye").GUID;
            GlobalVars.iid_Rnd_NatureObject = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_NatureObject").GUID;
            GlobalVars.iid_Rnd_Scroll = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_Scroll").GUID;
            GlobalVars.iid_Rnd_Potion = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_Potion").GUID;
            GlobalVars.iid_Rnd_Armor = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_Armor").GUID;
            GlobalVars.iid_Rnd_Weapon = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_Weapon").GUID;
            GlobalVars.iid_Rnd_Wand = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_Wand").GUID;
            GlobalVars.iid_Rnd_Food = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_Food").GUID;
            GlobalVars.iid_Rnd_Ring = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_Ring").GUID;
            GlobalVars.iid_Rnd_Amulet = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_Amulet").GUID;
            GlobalVars.iid_Rnd_Tool = GlobalVars.nwrDB.FindEntryBySign("Meta_Rnd_Tool").GUID;
            GlobalVars.iid_DwarvenArm = GlobalVars.nwrDB.FindEntryBySign("DwarvenArm").GUID;
            GlobalVars.iid_Mimming = GlobalVars.nwrDB.FindEntryBySign("Mimming").GUID;
            GlobalVars.iid_Mjollnir = GlobalVars.nwrDB.FindEntryBySign("Mjollnir").GUID;
            GlobalVars.iid_Gjall = GlobalVars.nwrDB.FindEntryBySign("Gjall").GUID;
            GlobalVars.iid_Gungnir = GlobalVars.nwrDB.FindEntryBySign("Gungnir").GUID;
            GlobalVars.iid_Skidbladnir = GlobalVars.nwrDB.FindEntryBySign("Skidbladnir").GUID;
            GlobalVars.iid_Flute = GlobalVars.nwrDB.FindEntryBySign("WoodenFlute").GUID;
            GlobalVars.iid_Torch = GlobalVars.nwrDB.FindEntryBySign("Torch").GUID;
        }

        public void PrepareFieldTurn(NWField fld, bool local)
        {
            try {
                if (local) {
                    fld.DoTurn();
                } else {
                    fld.Layer.DoTurn();
                }

                if (!GlobalVars.Debug_Freeze) {
                    for (int i = fld.Creatures.Count - 1; i >= 0; i--) {
                        fld.Creatures.GetItem(i).ResetStamina();
                    }

                    int rest;
                    do {
                        rest = 0;
                        for (int i = fld.Creatures.Count - 1; i >= 0; i--) {
                            NWCreature cr = fld.Creatures.GetItem(i);
                            if (!cr.IsPlayer && cr.State != CreatureState.Dead) {
                                cr.DoTurn();

                                if (cr.Stamina >= fPlayer.Speed) {
                                    rest++;
                                }
                            }
                        }

                        for (int i = fld.Creatures.Count - 1; i >= 0; i--) {
                            NWCreature cr = fld.Creatures.GetItem(i);
                            if (!cr.Equals(fPlayer) && cr.State == CreatureState.Dead) {
                                cr = (NWCreature)fld.Creatures.Extract(cr);

                                ClearEvents(cr);

                                bool withoutCorpse = cr.Entry.WithoutCorpse || cr.Ghost || cr.Illusion;

                                if (cr.Ghost) {
                                    GlobalVars.nwrWin.GhostsList.Delete(cr.GhostIdx);
                                }

                                if (withoutCorpse) {
                                    cr.Dispose();
                                } else {
                                    Item item = new Item(this, fld);
                                    item.CLSID = GlobalVars.iid_DeadBody;
                                    item.Count = 1;
                                    item.SetPos(cr.PosX, cr.PosY);
                                    item.Contents.Add(cr);
                                    if (cr.CanSink()) {
                                        GlobalVars.nwrWin.ShowText(cr, BaseLocale.Format(RS.rs_TheDeadSinks, new object[]{ item.GetDeclinableName(Number.nSingle, Case.cNominative, false) }));
                                        item.Dispose();
                                    } else {
                                        fld.Items.Add(item, false);
                                    }
                                }
                            }
                        }

                        PrepareDeads(fld, fld.Items, new ExtPoint(-1, -1));
                    } while (rest != 0);
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.prepareFieldTurn(): " + ex.Message);
            }
        }

        public NWCreature RespawnDeadBody(NWField field, ExtPoint pos, Item deadBody)
        {
            NWCreature cr = (NWCreature)deadBody.Contents.GetItem(0);

            ExtPoint rpt;
            if (field.FindCreature(pos.X, pos.Y) == null) {
                rpt = pos;
            } else {
                rpt = field.GetNearestPlace(pos.X, pos.Y, 3, true, cr.Movements);
                if (rpt.IsEmpty) {
                    rpt = pos;
                    Logger.Write("NWGameSpace.respawnDeadBody(): empty point");
                }
            }

            ItemsList iList = deadBody.ItemContainer;
            deadBody.Contents.Extract(cr);
            cr.HPCur = cr.HPMax_Renamed;
            cr.Turn = 0;
            cr.Owner = field;
            cr.SetPos(rpt.X, rpt.Y);

            if (cr.Entry.Flags.Contains(CreatureFlags.esUndead)) {
                cr.State = CreatureState.Undead;
            } else {
                cr.State = CreatureState.Alive;
            }

            cr.EnterField(field);
            iList.Remove(deadBody);
            return cr;
        }

        public void PostDeath(NWCreature creature)
        {
            if (creature == null) {
                return;
            }

            try {
                RaceID race = creature.Entry.Race;

                if (creature.CLSID != GlobalVars.cid_Hela) {
                    if (race == RaceID.crDefault || race == RaceID.crHuman) {
                        if (creature.LastAttacker != null && creature.LastAttacker.IsPlayer) {
                            Item item = (Item)fPlayer.Items.FindByCLSID(GlobalVars.iid_SoulTrapping_Ring);
                            if (item != null && item.InUse) {
                                item.Bonus = creature.CLSID;
                                ShowText(fPlayer, BaseLocale.GetStr(RS.rs_NewSoul));
                            }
                        }
                    }
                } else {
                    NWCreature dummy = (NWCreature)GetField(GlobalVars.Layer_Asgard, 1, 0).Creatures.FindByCLSID(GlobalVars.cid_Balder);
                    string msg = BaseLocale.GetStr(RS.rs_HelaIsDead);
                    if (dummy == null) {
                        msg = msg + " " + BaseLocale.GetStr(RS.rs_BalderIsFree);
                        AddCreatureEx(GlobalVars.Layer_Asgard, 1, 0, -1, -1, GlobalVars.cid_Balder);
                    } else {
                        msg += ".";
                    }
                    ShowText(fPlayer, msg, new LogFeatures(LogFeatures.lfDialog));
                }

                if (GlobalVars.nwrWin.ExtremeMode && !creature.IsPlayer) {
                    if (race == RaceID.crHuman) {
                        NWField fld = null;
                        int hero = -1;
                        int ge = creature.Alignment.GetGE();
                        switch (ge) {
                            case AlignmentEx.am_Mask_Good:
                                fld = GetField(GlobalVars.Layer_Asgard, 0, 0);
                                hero = GlobalVars.cid_Norseman;
                                break;
                            case AlignmentEx.am_Mask_Evil:
                                fld = GetField(GlobalVars.Layer_Niflheim, 1, 1);
                                hero = FindDataEntry("Shadow").GUID;
                                break;
                        }

                        if (fld != null && hero >= 0) {
                            fld.AddCreature(-1, -1, hero);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.postDeath(): " + ex.Message);
            }
        }

        public GameEvent PeekEvent(object receiver, EventID eventID, bool remove)
        {
            GameEvent result = null;
            for (int idx = 0; idx < fEventsQueue.Count; idx++) {
                GameEvent @event = (GameEvent)fEventsQueue[idx];
                if (@event.Receiver.Equals(receiver) && (eventID == EventID.event_Nothing || (eventID != EventID.event_Nothing && @event.CLSID == (int)eventID))) {
                    result = @event;
                    if (remove) {
                        fEventsQueue.Extract(@event);
                    }
                    break;
                }
            }
            return result;
        }

        public void SendEvent(EventID eventID, int priority, object sender, object receiver)
        {
            GameEvent newEvent = new GameEvent(this, null);
            newEvent.CLSID = (int)(eventID);
            newEvent.Priority = priority;
            newEvent.Receiver = receiver;
            newEvent.Sender = sender;
            int idx = 0;
            while (idx < fEventsQueue.Count && ((GameEvent)fEventsQueue[idx]).Priority > priority) {
                idx++;
            }
            fEventsQueue.Insert(idx, newEvent);
        }

        public void SetVolatileState(int aID, VolatileState aState)
        {
            DataEntry entry = GetDataEntry(aID);
            if (entry is VolatileEntry) {
                ((VolatileEntry)entry).RuntimeState = aState;
            }
        }

        public void ShowPlaceInfo(int aX, int aY, bool Total)
        {
            try {
                NWCreature creature = null;
                if (fPlayer.IsSeen(aX, aY, false)) {
                    string s = "";

                    NWField fld = fPlayer.CurrentField;
                    NWTile tile = (NWTile)fld.GetTile(aX, aY);

                    bool unseen = fPlayer.Effects.FindEffectByID(EffectID.eid_Blindness) != null;
                    bool pnear = MathHelper.Distance(fPlayer.PosX, fPlayer.PosY, aX, aY) == 0;

                    if (fld.IsBarrier(aX, aY)) {
                        GlobalVars.nwrWin.ShowText(fPlayer, BaseLocale.GetStr(RS.rs_YouCannotSeeThere));
                    } else {
                        if (tile.FogID != PlaceID.pid_Undefined) {
                            GlobalVars.nwrWin.ShowText(fPlayer, BaseLocale.GetStr(RS.rs_ItIsTooFoggy));
                            unseen = true;
                        }

                        if (Total && (!unseen || (unseen & pnear))) {
                            s = fld.GetPlaceName(aX, aY);
                            creature = (NWCreature)fld.FindCreature(aX, aY);
                            if (creature != null) {
                                if (creature.Stoning) {
                                    s = s + "; " + BaseLocale.GetStr(RS.rs_Statue) + " " + creature.GetDeclinableName(Number.nSingle, Case.cGenitive);
                                } else {
                                    s = s + "; @" + creature.Name + "@";
                                }
                            }
                        }

                        ExtList<LocatedEntity> its = fld.Items.SearchListByPos(aX, aY);
                        if (its != null && (!unseen || (unseen & pnear))) {
                            if (s.CompareTo("") != 0 && its.Count > 0) {
                                s += "; ";
                            }

                            int num = its.Count;
                            for (int i = 0; i < num; i++) {
                                if (i > 0) {
                                    s += ", ";
                                }

                                if (unseen) {
                                    s += BaseLocale.GetStr(StaticData.dbItemKinds[(int)((Item)its[i]).Kind].Name);
                                } else {
                                    s += ((Item)its[i]).Name;
                                }
                            }
                            its.Dispose();
                        }

                        if (GlobalVars.Debug_DevMode) {
                            s = s + " (" + Convert.ToString(aX) + " / " + Convert.ToString(aY) + ")";
                        }

                        if (s.CompareTo("") != 0) {
                            int pat;
                            if (unseen) {
                                pat = RS.rs_YouFeel;
                            } else {
                                pat = RS.rs_YouSee;
                            }
                            GlobalVars.nwrWin.ShowText(fPlayer, BaseLocale.Format(pat, new object[]{ s }));
                        }

                        if (creature != null && fPlayer.GetSkill(SkillID.Sk_Diagnosis) > 0) {
                            DiagnoseCreature(creature);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.showPlaceInfo(): " + ex.Message);
            }
        }

        public void PrepareWares()
        {
            for (var b = BuildingID.bid_First; b <= BuildingID.bid_Last; b++) {
                var blRec = StaticData.dbBuildings[(int)b];

                ProbabilityTable<int> temp = new ProbabilityTable<int>();

                int num2 = GlobalVars.dbItems.Count;
                for (int i = 0; i < num2; i++) {
                    int id = GlobalVars.dbItems[i];
                    ItemEntry eItem = (ItemEntry)GetDataEntry(id);

                    ItemKind kind = eItem.ItmKind;
                    if ((blRec.WaresKinds.Contains(kind))) {
                        int freq = eItem.Frequency;
                        if (freq > 0) {
                            temp.Add(id, freq);
                        }
                    }
                }

                blRec.Wares = temp;
            }
        }

        private static string GetDeclinable(object obj, Number n_num, Case n_case)
        {
            string result;

            if (n_num == Number.nSingle && n_case == Case.cNominative) {
                if (obj is Item) {
                    result = ((Item)obj).Entry.Name;
                } else {
                    if (obj is NWCreature) {
                        result = ((NWCreature)obj).Entry.Name;
                    } else {
                        if (obj is DeclinableEntry) {
                            result = ((DeclinableEntry)obj).Name;
                        } else {
                            result = "";
                        }
                    }
                }
            } else {
                if (obj is Item) {
                    result = ((Item)obj).GetDeclinableName(n_num, n_case, false);
                } else {
                    if (obj is NWCreature) {
                        result = ((NWCreature)obj).GetDeclinableName(n_num, n_case);
                    } else {
                        if (obj is DeclinableEntry) {
                            result = ((DeclinableEntry)obj).GetNounDeclension(n_num, n_case);
                        } else {
                            result = "";
                        }
                    }
                }
            }

            return result;
        }

        private static string DoNoun(string aToken, object obj, bool hasParams)
        {
            Number n_num = Number.nSingle;
            Case n_case = Case.cNominative;

            if (GlobalVars.nwrWin.LangExt == "ru" && hasParams) {
                string tok = AuxUtils.GetToken(aToken, "_", 2);
                char c = tok[0];
                if (c != 'p') {
                    if (c == 's') {
                        n_num = Number.nSingle;
                    }
                } else {
                    n_num = Number.nPlural;
                }

                tok = AuxUtils.GetToken(aToken, "_", 3);
                switch (tok[0]) {
                    case 'a':
                        {
                            n_case = Case.cAccusative;
                            break;
                        }
                    case 'd':
                        {
                            n_case = Case.cDative;
                            break;
                        }
                    case 'g':
                        {
                            n_case = Case.cGenitive;
                            break;
                        }
                    case 'i':
                        {
                            n_case = Case.cInstrumental;
                            break;
                        }
                    case 'n':
                        {
                            n_case = Case.cNominative;
                            break;
                        }
                    case 'p':
                        {
                            n_case = Case.cPrepositional;
                            break;
                        }
                }
            }

            return GetDeclinable(obj, n_num, n_case);
        }

        private static string PrepareToken(string token, int tokIndex, params object[] args)
        {
            token = token.Substring(1, token.Length - 1 - 1);
            int cnt = (int)AuxUtils.GetTokensCount(token, "_");
            string tok = AuxUtils.GetToken(token, "_", 1);

            string result = "";

            if (tok.CompareTo("noun") == 0 && cnt == 3) {
                if (args[tokIndex] != null) {
                    object obj = args[tokIndex];
                    result = DoNoun(token, obj, true);
                }
            } else {
                if (tok.CompareTo("%s") == 0) {
                    if (args[tokIndex] != null) {
                        object obj = args[tokIndex];
                        result = DoNoun(token, obj, false);
                    } else {
                        if (args[tokIndex] is string) {
                            result = ((string)((args[tokIndex] is string) ? args[tokIndex] : null));
                        }
                    }
                }
            }

            return result;
        }

        public string ParseMessage(string str, params object[] args)
        {
            try {
                if (args != null) {
                    args = (object[])args.Clone();
                }

                string result = str;
                int num = 0;
                int cnt = (int)AuxUtils.GetTokensCount(str, " .,");
                for (int i = 1; i <= cnt; i++) {
                    string tok = AuxUtils.GetToken(str, " .,", i);
                    if (string.IsNullOrEmpty(tok)) {
                        continue;
                    }

                    if (tok[0] == '{' && tok[tok.Length - 1] == '}') {
                        num++;
                        string res = PrepareToken(tok, num - 1, args);

                        int p = result.IndexOf(tok);
                        result = result.Substring(0, p) + result.Substring(p + tok.Length);

                        StringBuilder sb = new StringBuilder(result);
                        sb.Insert(p, res);
                        result = sb.ToString();
                    }
                }

                return result;
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.parseMessage(): " + ex.Message);
                return "";
            }
        }

        public string GenerateName(NWCreature creature, sbyte method)
        {
            return fNameLib.GenerateName(GlobalVars.nwrWin.LangExt, StaticData.GenderBySex[(int)creature.Sex], method);
        }

        // Implementing Fisher�"Yates shuffle
        private static void ShuffleArray(int[] array)
        {
            int index, temp;
            Random rnd = new Random();
            for (int i = array.Length - 1; i > 0; i--) {
                index = rnd.Next(i + 1);

                if (index != i) {
                    temp = array[index];
                    array[index] = array[i];
                    array[i] = temp;
                }
            }
        }

        private void PreparePrefixesArray(int low, int high, IntList entitySet, string name)
        {
            int prefixesCount = high - low + 1;
            int[] result = new int[prefixesCount];
            for (int i = low; i <= high; i++) {
                result[i - low] = i;
            }

            ShuffleArray(result);

            int entitiesCount = entitySet.Count;
            if (entitiesCount > prefixesCount) {
                throw new Exception("no prefixes");
            }

            for (int i = 0; i < entitiesCount; i++) {
                ItemEntry itemEntry = (ItemEntry)GetDataEntry(entitySet[i]);
                itemEntry.Prefix = result[i];
            }
        }

        public void PreparePrefixes()
        {
            try {
                PreparePrefixesArray(RS.rs_WandPrefixes_First, RS.rs_WandPrefixes_Last, GlobalVars.dbWands, "Wands");
                PreparePrefixesArray(RS.rs_RingPrefixes_First, RS.rs_RingPrefixes_Last, GlobalVars.dbRings, "Rings");
                PreparePrefixesArray(RS.rs_PotionPrefixes_First, RS.rs_PotionPrefixes_Last, GlobalVars.dbPotions, "Potions");
                //int[] raysPrefixes = preparePrefixesArray(RS.rs_RayPrefixes_First, RS.rs_RayPrefixes_Last);
                PreparePrefixesArray(RS.rs_ScrollPrefixes_First, RS.rs_ScrollPrefixes_Last, GlobalVars.dbScrolls, "Scrolls");
            } catch (Exception ex) {
                Logger.Write("NWGameSpace.preparePrefixes(): " + ex.Message);
            }
        }

        private static bool CheckGrynrHalls(NWField field)
        {
            int sacred_eye_pits = 0;

            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    if (field.GetTile(x, y).ForeBase == PlaceID.pid_SmallPit) {
                        sacred_eye_pits++;
                    }
                }
            }

            return sacred_eye_pits == 0;
        }

        public void DropItem(NWField field, NWCreature creature, Item item, int tx, int ty)
        {
            DoEvent(EventID.event_ItemDrop, creature, null, item);

            if (field.LandID == GlobalVars.Land_GrynrHalls) {
                NWTile tile = (NWTile)field.GetTile(tx, ty);
                if (tile.ForeBase == PlaceID.pid_SmallPit && item.CLSID == GlobalVars.iid_Amulet_SertrudEye) {
                    tile.Foreground = PlaceID.pid_Undefined;
                    item.Dispose();
                    GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_EyeFitsIntoPit) + BaseLocale.GetStr(RS.rs_PitEnclosesEye));
                    if (CheckGrynrHalls(field)) {
                        Item i = AddItemEx(field.Layer.EntryID, field.Coords.X, field.Coords.Y, tx, ty, GlobalVars.iid_Mjollnir);
                        GlobalVars.nwrWin.ShowText(creature, BaseLocale.GetStr(RS.rs_ThunderousNoise) + BaseLocale.GetStr(RS.rs_BrightLight) + BaseLocale.Format(RS.rs_YouSeeThatItemAppeared, new object[]{ i.Name }));
                    }
                    return;
                }
            }

            field.DropItem(creature, item, tx, ty);
        }

        public void PickupItem(NWField field, NWCreature creature, Item item)
        {
            DoEvent(EventID.event_ItemPickup, creature, null, item);

            field.PickupItem(creature, item);

            if (creature.IsPlayer) {
                int num = QuestsCount;
                for (int i = 0; i < num; i++) {
                    Quest quest = GetQuest(i);
                    quest.PickupItem(item);
                }
            } else {
                ShowText(creature, BaseLocale.Format(RS.rs_XPicksSomethingUp, new object[]{ creature.GetDeclinableName(Number.nSingle, Case.cInstrumental) }));
            }
        }

        public QuestItemState CheckQuestItem(int iid, int targetID)
        {
            NWCreature target = FindCreature(targetID);
            if (target != null && target.Items.FindByCLSID(iid) != null) {
                return QuestItemState.Completed;
            }

            target = Player;
            if (target != null && target.Items.FindByCLSID(iid) != null) {
                return QuestItemState.Founded;
            }

            return QuestItemState.None;
        }


        private void AddQuest(Quest quest)
        {
            fQuests.Add(quest);
        }

        public void GenMainQuests()
        {
            fQuests.Clear();

            AddQuest(new MainQuest(this, GlobalVars.iid_SoulTrapping_Ring, GlobalVars.cid_Hela));
            AddQuest(new MainQuest(this, GlobalVars.iid_Gjall, GlobalVars.cid_Heimdall));
            AddQuest(new MainQuest(this, GlobalVars.iid_Mjollnir, GlobalVars.cid_Thor));
            AddQuest(new MainQuest(this, GlobalVars.iid_DwarvenArm, GlobalVars.cid_Tyr));
            AddQuest(new MainQuest(this, GlobalVars.iid_Mimming, GlobalVars.cid_Freyr));
            AddQuest(new MainQuest(this, GlobalVars.iid_Gungnir, GlobalVars.cid_Odin));
        }

        #region IHost

        public void DoEvent(EventID eventID, object sender, object receiver, object extData)
        {
            GlobalVars.nwrWin.DoEvent(eventID, sender, receiver, extData);
        }

        public DataEntry GetDataEntry(int uid)
        {
            return fDB.GetEntry(uid);
        }

        public DataEntry FindDataEntry(string sign)
        {
            return fDB.FindEntryBySign(sign);
        }

        public void RepaintView(int delayInterval)
        {
            GlobalVars.nwrWin.Repaint(delayInterval);
        }

        public void ShowText(string text)
        {
            GlobalVars.nwrWin.ShowText(text);
        }

        public void ShowText(object sender, string text)
        {
            GlobalVars.nwrWin.ShowText(sender, text);
        }

        public void ShowText(object sender, string text, LogFeatures features)
        {
            GlobalVars.nwrWin.ShowText(sender, text, features);
        }

        public void ShowTextAux(string text)
        {
            GlobalVars.nwrWin.ShowTextAux(text);
        }

        #endregion

        // <editor-fold defaultstate="collapsed" desc="Serializables factory">

        private static void RegisterSerializables()
        {
            SerializablesManager.RegisterSerializable(StaticData.SID_BUILDING, CreateBuilding);
            SerializablesManager.RegisterSerializable(StaticData.SID_ITEM, CreateItem);
            SerializablesManager.RegisterSerializable(StaticData.SID_CREATURE, CreateCreature);
            SerializablesManager.RegisterSerializable(StaticData.SID_KNOWLEDGE, CreateKnowledge);
            SerializablesManager.RegisterSerializable(StaticData.SID_EFFECT, CreateEffect);
            SerializablesManager.RegisterSerializable(StaticData.SID_RECALL_POS, CreateRecallPos);
            SerializablesManager.RegisterSerializable(StaticData.SID_DEBT, CreateDebt);
            SerializablesManager.RegisterSerializable(StaticData.SID_SOURCE_FORM, CreateSourceForm);
            SerializablesManager.RegisterSerializable(StaticData.SID_POINTGUARD_GOAL, CreatePointGuardGoal);
            SerializablesManager.RegisterSerializable(StaticData.SID_AREAGUARD_GOAL, CreateAreaGuardGoal);
            SerializablesManager.RegisterSerializable(StaticData.SID_VILLAGE, CreateVillage);
            SerializablesManager.RegisterSerializable(StaticData.SID_GATE, CreateGate);
        }

        private static ISerializable CreateBuilding(object owner)
        {
            NWGameSpace space = (NWGameSpace)Instance;
            return new Building(space, owner);
        }

        private static ISerializable CreateItem(object owner)
        {
            NWGameSpace space = (NWGameSpace)Instance;
            return new Item(space, owner);
        }

        private static ISerializable CreateCreature(object owner)
        {
            NWGameSpace space = (NWGameSpace)Instance;
            return new NWCreature(space, owner);
        }

        private static ISerializable CreateKnowledge(object owner)
        {
            return new Knowledge(owner);
        }

        private static ISerializable CreateEffect(object owner)
        {
            NWGameSpace space = (NWGameSpace)Instance;
            return new Effect(space, (GameEntity)owner, EffectID.eid_None, null, EffectAction.ea_Instant, 0, 0);
        }

        private static ISerializable CreateRecallPos(object owner)
        {
            return new RecallPos(owner);
        }

        private static ISerializable CreateDebt(object owner)
        {
            return new Debt(owner);
        }

        private static ISerializable CreateSourceForm(object owner)
        {
            return new SourceForm(owner);
        }

        private static ISerializable CreatePointGuardGoal(object owner)
        {
            return new PointGuardGoal((BrainEntity)owner);
        }

        private static ISerializable CreateAreaGuardGoal(object owner)
        {
            return new AreaGuardGoal((BrainEntity)owner);
        }

        private static ISerializable CreateVillage(object owner)
        {
            NWGameSpace space = (NWGameSpace)Instance;
            return new Village(space, owner);
        }

        private static ISerializable CreateGate(object owner)
        {
            NWGameSpace space = (NWGameSpace)Instance;
            return new Gate(space, owner);
        }

        // </editor-fold>
    }
}

