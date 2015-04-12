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
package nwr.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import jzrlib.common.DayTime;
import jzrlib.core.Directions;
import jzrlib.core.ExtList;
import jzrlib.core.FileVersion;
import jzrlib.core.GameEntity;
import jzrlib.core.GameSpace;
import jzrlib.core.ISerializable;
import jzrlib.core.LocatedEntity;
import jzrlib.core.Point;
import jzrlib.core.ProbabilityTable;
import jzrlib.core.Rect;
import jzrlib.core.SerializablesManager;
import jzrlib.core.StringList;
import jzrlib.core.brain.BrainEntity;
import jzrlib.core.brain.GoalEntity;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.grammar.Case;
import jzrlib.grammar.Grammar;
import jzrlib.grammar.Number;
import jzrlib.map.BaseTile;
import jzrlib.map.TileStates;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.StreamUtils;
import jzrlib.utils.TextUtils;
import nwr.core.FileHeader;
import nwr.core.GameEvent;
import nwr.core.IHost;
import nwr.core.IntList;
import nwr.core.Locale;
import nwr.core.NWDateTime;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.AlignmentEx;
import nwr.core.types.AttackKind;
import nwr.core.types.BuildingID;
import nwr.core.types.CreatureAction;
import nwr.core.types.CreatureState;
import nwr.core.types.EventID;
import nwr.core.types.GameScreen;
import nwr.core.types.GameState;
import nwr.core.types.ItemKind;
import nwr.core.types.LogFeatures;
import nwr.core.types.PlaceID;
import nwr.core.types.QuestItemState;
import nwr.core.types.RaceID;
import nwr.core.types.ScreenStatus;
import nwr.core.types.SkillID;
import nwr.core.types.TeachableKind;
import nwr.core.types.TurnState;
import nwr.core.types.VolatileState;
import nwr.creatures.NWCreature;
import nwr.creatures.NamesLib;
import nwr.creatures.brain.BeastBrain;
import nwr.creatures.brain.GoalKind;
import nwr.creatures.brain.LeaderBrain;
import nwr.creatures.brain.SentientBrain;
import nwr.creatures.brain.WarriorBrain;
import nwr.creatures.brain.goals.AreaGuardGoal;
import nwr.creatures.brain.goals.EnemyChaseGoal;
import nwr.creatures.brain.goals.EnemyEvadeGoal;
import nwr.creatures.brain.goals.PointGuardGoal;
import nwr.creatures.specials.IvyCreeper;
import nwr.creatures.specials.Snake;
import nwr.database.CreatureEntry;
import nwr.database.CreatureFlags;
import nwr.database.DataEntry;
import nwr.database.DeclinableEntry;
import nwr.database.ItemEntry;
import nwr.database.ItemEntry.EffectEntry;
import nwr.database.LandEntry;
import nwr.database.LandFlags;
import nwr.database.LayerEntry;
import nwr.database.NWDatabase;
import nwr.database.VolatileEntry;
import nwr.effects.Effect;
import nwr.effects.EffectAction;
import nwr.effects.EffectID;
import nwr.engine.BaseSystem;
import nwr.engine.ResourceManager;
import nwr.engine.SoundEngine;
import nwr.game.ghosts.Ghost;
import nwr.game.ghosts.GhostsList;
import nwr.game.quests.MainQuest;
import nwr.game.story.Journal;
import nwr.game.story.Quest;
import nwr.item.Item;
import nwr.item.ItemsList;
import nwr.main.GlobalVars;
import nwr.player.Debt;
import nwr.player.Knowledge;
import nwr.player.Player;
import nwr.player.RecallPos;
import nwr.player.SourceForm;
import nwr.universe.Building;
import nwr.universe.Door;
import nwr.universe.Gate;
import nwr.universe.NWField;
import nwr.universe.NWLayer;
import nwr.universe.NWTile;
import nwr.universe.UniverseBuilder;
import nwr.universe.Village;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class NWGameSpace extends GameSpace implements IHost
{
    public static final int SAVEFILE_PLAYER = 1;
    public static final int SAVEFILE_TERRAINS = 2;
    public static final int SAVEFILE_JOURNAL = 3;

    private static final int TurnSeconds = 20;
    private static final int CastleSize = 5;
    private static final Rect Castle = new Rect(1, 1, 3, 4);
    private static final String[] Defenders;
    private static final String[] Attackers;
    private static final String[] Undeads;

    public static final FileVersion RGF_Version = new FileVersion(1, 21);
    public static final char[] RGP_Sign = new char[]{'R', 'G', 'P'};
    public static final char[] RGT_Sign = new char[]{'R', 'G', 'T'};
    
    private NWDatabase fDB;
    private ExtList<Object> fEventsQueue;
    private int fFileIndex;
    private ArrayList<NWLayer> fLayers;
    private NamesLib fNameLib;
    private Player fPlayer;
    private NWDateTime fTime;
    private TurnState fTurnState;
    private Journal fJournal;
    private ArrayList<Quest> fQuests;

    public Point MimerWellPos;
    public NWCreature rt_Jormungand;
    //public TCreature rt_Raven;

    public boolean BifrostCollapsed; // todo: saving this state
    public boolean IsRagnarok;

    static {
        Defenders = new String[]{"Halcyon", "Roc", "Valkyrie", "Dwarf", "Edgewort"};
        Attackers = new String[]{"Nidslacr", "HelDragon", "ElleGiant", "LowerDwarf", "Ramapith", "Migdnart", "RockGiant", "Minion", "HillGiant", "Magician", "BorgonVile", "Wizard", "Shadow", "Guardian"};
        Undeads = new String[]{"Shade", "Spirit", "Ghost", "Shadow", "Wight", "Wraith"};
        
        registerSerializables();
    }

    public NWGameSpace(Object owner)
    {
        super(owner);

        GlobalVars.nwrGame = this;

        try {
            this.fEventsQueue = new ExtList<>(true);
            this.fFileIndex = -1;
            this.fTime = new NWDateTime();

            this.initData();

            this.fJournal = new Journal();
            this.fQuests = new ArrayList<>();

            this.fPlayer = new Player(this, null);

            this.fLayers = new ArrayList<>();
            int num = GlobalVars.dbLayers.getCount();
            for (int i = 0; i < num; i++) {
                this.fLayers.add(new NWLayer(this, GlobalVars.dbLayers.get(i)));
            }

            this.fNameLib = new NamesLib();
            this.fNameLib.initNorseDic();

            this.prepareWares();

            this.fTurnState = TurnState.gtsWait;
        } catch (Exception ex) {
            Logger.write("NWGameSpace.Create(): " + ex.getMessage());
        }
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            try {
                this.fNameLib.dispose();

                this.fPlayer.leaveField();

                int num = this.fLayers.size();
                for (int i = 0; i < num; i++) {
                    this.fLayers.get(i).dispose();
                }

                this.fLayers.clear();
                this.fLayers = null;
                
                this.fPlayer.dispose();
                
                this.fQuests = null;
                this.fJournal.dispose();

                this.doneData();
                this.fEventsQueue.dispose();
            } catch (Exception ex) {
                Logger.write("NWGameSpace.Dispose(): " + ex.getMessage());
            }
        }
        super.dispose(disposing);
    }

    public final int getFileIndex()
    {
        return this.fFileIndex;
    }

    public final int getLayersCount()
    {
        return this.fLayers.size();
    }

    public final NWLayer getLayer(int index)
    {
        return this.fLayers.get(index);
    }

    public final NamesLib getNameLib()
    {
        return this.fNameLib;
    }

    public final Player getPlayer()
    {
        return this.fPlayer;
    }

    public final NWDateTime getTime()
    {
        return this.fTime;
    }

    public final TurnState getTurnState()
    {
        return this.fTurnState;
    }

    public Journal getJournal()
    {
        return this.fJournal;
    }

    public final int getQuestsCount()
    {
        return this.fQuests.size();
    }

    public final Quest getQuest(int index)
    {
        return this.fQuests.get(index);
    }

    private void clearVolatiles()
    {
        int num = GlobalVars.nwrBase.getEntriesCount();
        for (int i = 0; i < num; i++) {
            DataEntry entry = GlobalVars.nwrBase.getEntry(i);
            if (entry instanceof VolatileEntry) {
                ((VolatileEntry) entry).RuntimeState = VolatileState.vesNone;
            }
        }
    }

    private void doneData()
    {
        GlobalVars.dbItems.dispose();
        GlobalVars.dbLayers.dispose();
        GlobalVars.dbCreatures.dispose();
        GlobalVars.dbRings.dispose();
        GlobalVars.dbPotions.dispose();
        GlobalVars.dbScrolls.dispose();
        GlobalVars.dbWands.dispose();
        GlobalVars.dbAmulets.dispose();
        GlobalVars.dbArmor.dispose();
        GlobalVars.dbFoods.dispose();
        GlobalVars.dbTools.dispose();
        GlobalVars.dbWeapon.dispose();
        GlobalVars.dbScripts.dispose();
        GlobalVars.dbKnowledges.dispose();
        GlobalVars.nwrBase.dispose();
    }

    private void initData()
    {
        try {
            this.fDB = new NWDatabase();
            this.fDB.LoadXML("RDatabase.xml");
            GlobalVars.nwrBase = this.fDB;

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

            int num = GlobalVars.nwrBase.getEntriesCount();
            for (int i = 0; i < num; i++) {
                DataEntry entry = GlobalVars.nwrBase.getEntry(i);
                if (entry != null) {
                    switch (entry.Kind) {
                        case DataEntry.ek_Item: {
                            GlobalVars.dbItems.add(entry.GUID);
                            ItemEntry eItem = (ItemEntry) entry;
                            if (!eItem.isMeta()) {
                                switch (eItem.ItmKind) {
                                    case ik_Armor:
                                    case ik_Shield:
                                    case ik_Helmet:
                                    case ik_Clothing:
                                    case ik_HeavyArmor:
                                    case ik_MediumArmor:
                                    case ik_LightArmor:
                                        GlobalVars.dbArmor.add(eItem.GUID);
                                        break;

                                    case ik_Food:
                                        GlobalVars.dbFoods.add(eItem.GUID);
                                        break;

                                    case ik_Potion:
                                        GlobalVars.dbPotions.add(eItem.GUID);
                                        break;

                                    case ik_Ring:
                                        GlobalVars.dbRings.add(eItem.GUID);
                                        break;

                                    case ik_Tool:
                                        GlobalVars.dbTools.add(eItem.GUID);
                                        break;

                                    case ik_Wand:
                                        GlobalVars.dbWands.add(eItem.GUID);
                                        break;

                                    case ik_BluntWeapon:
                                    case ik_ShortBlade:
                                    case ik_LongBlade:
                                    case ik_Spear:
                                    case ik_Axe:
                                    case ik_Bow:
                                    case ik_CrossBow:
                                    case ik_Projectile:
                                        GlobalVars.dbWeapon.add(eItem.GUID);
                                        break;

                                    case ik_Scroll:
                                        GlobalVars.dbScrolls.add(eItem.GUID);
                                        break;

                                    case ik_Amulet:
                                        GlobalVars.dbAmulets.add(eItem.GUID);
                                        break;

                                    case ik_Misc:
                                        // dummy
                                        break;
                                }
                            }
                            break;
                        }

                        case DataEntry.ek_Creature:
                            GlobalVars.dbCreatures.add(entry.GUID);
                            break;

                        case DataEntry.ek_Layer:
                            GlobalVars.dbLayers.add(entry.GUID);
                            break;

                        case DataEntry.ek_EventHandler:
                            GlobalVars.dbScripts.add(entry.GUID);
                            break;

                        case DataEntry.ek_Information:
                            GlobalVars.dbKnowledges.add(entry.GUID);
                            break;
                    }
                }
            }

            this.prepareEntries();
            this.preparePrefixes();
        } catch (Exception ex) {
            Logger.write("NWGameSpace.initData(): " + ex.getMessage());
            throw ex;
        }
    }

    public final boolean isTimeStop()
    {
        return (this.fPlayer.getEffects().findEffectByID(EffectID.eid_TimeStop) != null);
    }
    
    private void doGameTurn()
    {
        try {
            if (GlobalVars.nwrWin.getGameState() != GameState.gsWorldGen) {
                // process game time
                int day = this.fTime.Day;
                this.fTime.tick((int) (Math.round(((double) NWGameSpace.TurnSeconds * (this.fPlayer.getSpeed() / 10.0)))));
                if (day != this.fTime.Day) {
                    this.fJournal.storeTime(this.fTime);
                }

                this.fPlayer.doTurn();
                NWField playerField = (NWField) this.fPlayer.getCurrentMap();

                boolean timeStop = this.isTimeStop();
                if (!timeStop) {
                    this.prepareFieldTurn(playerField, false);
                    this.prepareDeads(playerField, this.fPlayer.getItems(), this.fPlayer.getLocation());
                }

                /* If Ragnarok is already go, but the player is not in Asgard - must further process the fields of Asgard. 
                 * Otherwise, if he in Asgard - layer has been processed. 
                 * TimeStop does not affect. */
                if (this.IsRagnarok && this.fPlayer.LayerID != GlobalVars.Layer_Asgard) {
                    NWField fld = this.getField(GlobalVars.Layer_Asgard, 0, 0);
                    this.prepareFieldTurn(fld, true);

                    fld = this.getField(GlobalVars.Layer_Asgard, 1, 0);
                    this.prepareFieldTurn(fld, true);
                }

                if (this.fPlayer.getState() == CreatureState.csDead) {
                    GlobalVars.nwrWin.doEvent(EventID.event_Dead, null, null, null);
                }

                if (this.fPlayer.Turn % 200 == 0) {
                    prepareRaven(playerField);
                }

                this.checkRagnarok();
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.doGameTurn(): " + ex.getMessage());
        }
    }

    private void checkRagnarok()
    {
        try {
            if (this.IsRagnarok) {
                boolean isGodsLose = this.getVolatileState(GlobalVars.cid_Heimdall) == VolatileState.vesDestroyed;
                isGodsLose = (isGodsLose && this.getVolatileState(GlobalVars.cid_Thor) == VolatileState.vesDestroyed);
                isGodsLose = (isGodsLose && this.getVolatileState(GlobalVars.cid_Tyr) == VolatileState.vesDestroyed);
                isGodsLose = (isGodsLose && this.getVolatileState(GlobalVars.cid_Freyr) == VolatileState.vesDestroyed);
                isGodsLose = (isGodsLose && this.getVolatileState(GlobalVars.cid_Odin) == VolatileState.vesDestroyed);

                boolean isGodsWin = (this.getVolatileState(GlobalVars.cid_Jormungand) == VolatileState.vesDestroyed);
                isGodsWin = (isGodsWin && this.getVolatileState(GlobalVars.cid_Garm) == VolatileState.vesDestroyed);
                isGodsWin = (isGodsWin && this.getVolatileState(GlobalVars.cid_Loki) == VolatileState.vesDestroyed);
                isGodsWin = (isGodsWin && this.getVolatileState(GlobalVars.cid_Surtr) == VolatileState.vesDestroyed);
                isGodsWin = (isGodsWin && this.getVolatileState(GlobalVars.cid_Fenrir) == VolatileState.vesDestroyed);

                if (isGodsWin) {
                    this.doEvent(EventID.event_Victory, null, null, null);
                } else if (isGodsLose) {
                    this.doEvent(EventID.event_Defeat, null, null, null);
                } else {
                    // not finished
                }
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.checkRagnarok(): " + ex.getMessage());
        }
    }

    public final void doRagnarok()
    {
        if (!this.IsRagnarok) {
            this.IsRagnarok = true;
            GlobalVars.nwrWin.showText(this.fPlayer, Locale.getStr(RS.rs_RagnarokAppear), new LogFeatures(LogFeatures.lfDialog));

            this.FinalDisplacement(GlobalVars.cid_Heimdall, false, GlobalVars.Land_Vigrid);
            this.FinalDisplacement(GlobalVars.cid_Thor, false, GlobalVars.Land_Vigrid);
            this.FinalDisplacement(GlobalVars.cid_Tyr, false, GlobalVars.Land_Vigrid);
            this.FinalDisplacement(GlobalVars.cid_Freyr, false, GlobalVars.Land_Vigrid);
            this.FinalDisplacement(GlobalVars.cid_Odin, false, GlobalVars.Land_Vigrid);
            this.FinalDisplacement(GlobalVars.cid_Jormungand, false, GlobalVars.Land_Vigrid);
            this.FinalDisplacement(GlobalVars.cid_Garm, true, GlobalVars.Land_Vigrid);
            this.FinalDisplacement(GlobalVars.cid_Loki, true, GlobalVars.Land_Vigrid);
            this.FinalDisplacement(GlobalVars.cid_Surtr, true, GlobalVars.Land_Vigrid);
            this.FinalDisplacement(GlobalVars.cid_Fenrir, true, GlobalVars.Land_Vigrid);

            this.FinalDisplacement(GlobalVars.cid_Emanon, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Nidhogg, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Anxarcule, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Hela, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_KonrRig, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Plog, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Vanseril, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Gulveig, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Hobjoi, true, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Gymir, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Scyld, true, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Uorik, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Vidur, false, GlobalVars.Land_Valhalla);
            this.FinalDisplacement(GlobalVars.cid_Harbard, false, GlobalVars.Land_Valhalla);

            int aCnt = AuxUtils.getBoundedRnd(350, 480);
            for (int i = 1; i <= aCnt; i++) {
                int k = AuxUtils.getBoundedRnd(0, Defenders.length - 1);
                int id = GlobalVars.nwrBase.findEntryBySign(NWGameSpace.Defenders[k]).GUID;
                if (id >= 0) {
                    this.addCreatureEx(GlobalVars.Layer_Asgard, 0, 0, -1, -1, id);
                }
            }

            aCnt = AuxUtils.getBoundedRnd(300, 400);
            for (int i = 1; i <= aCnt; i++) {
                int k = AuxUtils.getBoundedRnd(0, Attackers.length - 1);
                int id = GlobalVars.nwrBase.findEntryBySign(NWGameSpace.Attackers[k]).GUID;
                if (id >= 0 && this.canCreate(id)) {
                    this.addCreatureEx(GlobalVars.Layer_Asgard, 0, 0, -1, -1, id);
                }
            }
        }
    }

    private void prepareRaven(NWField playerField)
    {
        if (GlobalVars.Debug_Freeze || (playerField.getLandEntry().Flags.hasIntersect(LandFlags.lsIsDungeon, LandFlags.lsIsCave)) || this.fPlayer.isInWater()) {
            return;
        }

        Point fldCoords = playerField.getCoords();
        this.addCreatureEx(this.fPlayer.LayerID, fldCoords.X, fldCoords.Y, -1, -1, GlobalVars.cid_Raven);

        /*if (this.rt_Raven == null) {
            this.rt_Raven = this.addCreature(this.fPlayer.LayerID, fldCoords.X, fldCoords.Y, -1, -1, rGlobals.cid_Raven);
        } else {
            this.rt_Raven.TransferTo(this.fPlayer.LayerID, fldCoords.X, fldCoords.Y, -1, -1, rData.MapArea, true, false);
        }*/
    }

    public final DayTime getDayTime()
    {
        return this.fTime.getDayTime();
    }

    public final short getTileBrightness(NWField field, int tileStates, boolean localView)
    {
        short result;

        boolean seen = TileStates.hasState(tileStates, TileStates.TS_SEEN);
        if (seen || localView) {
            result = 255;
        } else {
            float brightnessFactor;
            if (field.isDark()) {
                brightnessFactor = DayTime.dt_Midnight.Brightness;
            } else {
                brightnessFactor = this.getDayTime().Brightness;
            }
            result = (short) (255 * brightnessFactor);
        }
        return result;
    }

    public boolean isCreatureVisible(NWCreature creature, BaseTile tile)
    {
        boolean result = true;
        try {
            result = (creature.equals(this.fPlayer) || GlobalVars.Debug_Divinity 
                    || (tile.hasState(TileStates.TS_SEEN) && this.fPlayer.checkVisible(creature)) || this.fPlayer.getEffects().findEffectByID(EffectID.eid_SoulSeeking) != null);
        } catch (Exception ex) {
            Logger.write("NWGameSpace.isCreatureVisible(): " + ex.getMessage());
        }
        return result;
    }

    private void loadVolatiles(BinaryInputStream stream, FileVersion version) throws IOException
    {
        int count = StreamUtils.readInt(stream);
        for (int i = 0; i < count; i++) {
            int id = StreamUtils.readInt(stream);
            VolatileState ves = VolatileState.forValue(StreamUtils.readByte(stream));
            DataEntry entry = this.getDataEntry(id);
            if (entry instanceof VolatileEntry) {
                ((VolatileEntry) entry).RuntimeState = ves;
            }
        }
    }

    private void saveVolatiles(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        int count = 0;

        int num = GlobalVars.nwrBase.getEntriesCount();
        for (int i = 0; i < num; i++) {
            DataEntry entry = GlobalVars.nwrBase.getEntry(i);
            if (entry instanceof VolatileEntry && ((VolatileEntry) entry).RuntimeState != VolatileState.vesNone) {
                count++;
            }
        }

        StreamUtils.writeInt(stream, count);

        int num2 = GlobalVars.nwrBase.getEntriesCount();
        for (int i = 0; i < num2; i++) {
            DataEntry entry = GlobalVars.nwrBase.getEntry(i);
            if (entry instanceof VolatileEntry && ((VolatileEntry) entry).RuntimeState != VolatileState.vesNone) {
                StreamUtils.writeInt(stream, entry.GUID);
                StreamUtils.writeByte(stream, (byte) ((VolatileEntry) entry).RuntimeState.getValue());
            }
        }
    }

    private void prepareDeads(NWField field, ItemsList List, Point rp)
    {
        try {
            for (int i = List.getCount() - 1; i >= 0; i--) {
                Item item = List.getItem(i);
                if (item.CLSID == GlobalVars.iid_DeadBody) {
                    NWCreature creature = (NWCreature) item.getContents().getItem(0);
                    creature.incTurn();

                    CreatureEntry entry = creature.getEntry();
                    if (creature.Turn == 0) {
                        if (entry.isRespawn()) {
                            if (rp.X == -1 && rp.Y == -1) {
                                rp = item.getLocation();
                            }
                            this.respawnDeadBody(field, rp, item);
                        } else {
                            if (!entry.isCorpsesPersist()) {
                                if (creature.isMercenary()) {
                                    ((LeaderBrain) this.fPlayer.getBrain()).removeMember(creature);
                                }
                                List.delete(i);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.prepareDeads(): " + ex.getMessage());
        }
    }

    public String getDayTimeInfo()
    {
        int dtv = this.getDayTime().Value;
        String rs = Locale.getStr(StaticData.dbDayTimeRS[dtv]);
        return this.fTime.toString(true, false)+ " (" + rs + ")";
    }

    public final void setTurnState(TurnState value)
    {
        this.fTurnState = value;
    }

    public final NWCreature addCreatureEx(int layerID, int fx, int fy, int px, int py, int creatureID)
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

            res.transferTo(layerID, fx, fy, px, py, StaticData.MapArea, true, false);

            CreatureEntry crEntry = (CreatureEntry) this.getDataEntry(creatureID);
            if (crEntry.Flags.contains(CreatureFlags.esUnique)) {
                this.setVolatileState(creatureID, VolatileState.vesCreated);
            }

            result = res;
        } catch (Exception ex) {
            Logger.write("NWGameSpace.AddCreature(): " + ex.getMessage());
            result = null;
        }

        return result;
    }

    public final Item addItemEx(int layerID, int fx, int fy, int px, int py, int itemID)
    {
        if (itemID < 0) {
            return null;
        }

        Item result = null;

        NWField fld = this.getField(layerID, fx, fy);
        if (fld != null) {
            if (px < 0 || py < 0) {
                Point pt = fld.searchFreeLocation();
                px = pt.X;
                py = pt.Y;
            }

            result = new Item(this, fld);
            result.setCLSID(itemID);
            result.setPos(px, py);
            result.genCount();
            fld.getItems().add(result, false);
        }

        return result;
    }

    public final NWCreature addTownsman(NWField field, Rect village, int creatureID)
    {
        NWCreature townsman = new NWCreature(this, null, creatureID, true, true);
        townsman.transferTo(field.getLayer().EntryID, field.getCoords().X, field.getCoords().Y, -1, -1, village, true, false);
        ((WarriorBrain) townsman.getBrain()).setAreaGuardGoal(village);
        return townsman;
    }

    public final boolean canCreate(int id)
    {
        return this.getVolatileState(id) == VolatileState.vesNone;
    }

    public final void clearEvents(Object aBy)
    {
        for (int i = this.fEventsQueue.getCount() - 1; i >= 0; i--) {
            GameEvent event = (GameEvent) this.fEventsQueue.get(i);
            if (event.Receiver.equals(aBy) || event.Sender.equals(aBy)) {
                this.fEventsQueue.delete(i);
            }
        }
    }

    public final void diagnoseCreature(NWCreature cr)
    {
        try {
            StringList strs = new StringList();
            try {
                strs.clear();
                strs.add(Locale.getStr(RS.rs_Creature) + ": @" + cr.getName() + "@");
                strs.add(Locale.getStr(RS.rs_HP) + ": " + String.valueOf(cr.HPCur) + "/" + String.valueOf(cr.HPMax));
                strs.add(Locale.getStr(RS.rs_Armor) + ": " + String.valueOf(cr.ArmorClass));
                strs.add(Locale.getStr(RS.rs_Speed) + ": " + String.valueOf(cr.getSpeed()));
                strs.add(Locale.getStr(RS.rs_ToHit) + ": " + String.valueOf(cr.ToHit));
                strs.add(Locale.getStr(RS.rs_Attacks) + ": " + String.valueOf(cr.Attacks));
                strs.add(Locale.getStr(RS.rs_Damage) + ": " + String.valueOf(cr.DBMin) + "-" + String.valueOf(cr.DBMax));

                if (GlobalVars.Debug_DevMode) {
                    int num = cr.getBrain().getGoalsCount();
                    for (int i = 0; i < num; i++) {
                        GoalEntity gl = cr.getBrain().getGoal(i);
                        if (gl.Kind == GoalKind.gk_EnemyChase) {
                            strs.add("  chase " + ((NWCreature) ((EnemyChaseGoal) gl).Enemy).getDeclinableName(Number.nSingle, Case.cAccusative));
                        } else {
                            if (gl.Kind == GoalKind.gk_EnemyEvade) {
                                strs.add("  evade of " + ((NWCreature) ((EnemyEvadeGoal) gl).Enemy).getDeclinableName(Number.nSingle, Case.cAccusative));
                            } else {
                                strs.add("  " + gl.getClass().getTypeName() + ": " + String.valueOf((double) gl.Value));
                            }
                        }
                    }
                }

                GlobalVars.nwrWin.showMessage(strs.getTextStr());
            } finally {
                strs.dispose();
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.diagnoseCreature(): " + ex.getMessage());
        }
    }

    public final void processGameStep()
    {
        try {
            switch (this.fTurnState) {
                case gtsWait:
                    break;

                case gtsDone:
                    this.setTurnState(TurnState.gtsWait);
                    this.doGameTurn();
                    break;

                case gtsSkip:
                    BaseSystem.sleep(1000);
                    this.doGameTurn();
                    break;
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.processGameStep(): " + ex.getMessage());
        }
    }

    public final void doPlayerAction(CreatureAction action, int aExt)
    {
        NWField fld = (NWField) this.fPlayer.getCurrentMap();
        switch (action) {
            case caWait: {
                this.fPlayer.waitTurn();
                break;
            }

            case caMove: {
                int dir = (aExt);
                if (dir != Directions.dtZenith) {
                    if (dir != Directions.dtNadir) {
                        int NewX = this.fPlayer.getPosX() + Directions.Data[dir].dX;
                        int NewY = this.fPlayer.getPosY() + Directions.Data[dir].dY;
                        this.fPlayer.moveTo(NewX, NewY);
                    } else {
                        this.fPlayer.moveToDown();
                    }
                } else {
                    this.fPlayer.moveToUp();
                }
                break;
            }

            case caAttackMelee: {
                int dir = (aExt);
                int NewX = this.fPlayer.getPosX() + Directions.Data[dir].dX;
                int NewY = this.fPlayer.getPosY() + Directions.Data[dir].dY;

                NWCreature enemy = (NWCreature) fld.findCreature(NewX, NewY);
                if (enemy != null) {
                    this.fPlayer.attackTo(AttackKind.akMelee, enemy, null, null);
                } else {
                    this.doPlayerAction(CreatureAction.caMove, aExt);
                }
                break;
            }

            case caAttackShoot: {
                int dir = (aExt);
                this.fPlayer.shootToDir(dir);
                break;
            }

            case caItemPickup: {
                Item item = (Item) GameSpace.getInstance().findEntity(aExt);
                if (item == null) {
                    throw new RuntimeException("Assertion failure #2");
                }
                this.fPlayer.pickupItem(item);
                break;
            }

            case caItemDrop: {
                Item item = (Item) GameSpace.getInstance().findEntity(aExt);
                if (item == null) {
                    throw new RuntimeException("Assertion failure #3");
                }
                this.fPlayer.dropItem(item);
                break;
            }

            case caItemWear: {
                Item item = (Item) GameSpace.getInstance().findEntity(aExt);
                if (item == null) {
                    throw new RuntimeException("Assertion failure #4");
                }
                if (this.fPlayer.canBeUsed(item)) {
                    this.fPlayer.wearItem(item);
                }
                break;
            }

            case caItemRemove: {
                Item item = (Item) GameSpace.getInstance().findEntity(aExt);
                if (item == null) {
                    throw new RuntimeException("Assertion failure #5");
                }
                this.fPlayer.removeItem(item);
                break;
            }

            case caPickupAll: {
                this.fPlayer.pickupAll();
                break;
            }

            case caItemUse: {
                Item item = (Item) GameSpace.getInstance().findEntity(aExt);
                if (item == null) {
                    throw new RuntimeException("Assertion failure #6");
                }
                this.fPlayer.useItem(item, null);
                break;
            }

            case caSpellUse: {
                this.fPlayer.useSpell(EffectID.forValue(aExt), null);
                break;
            }

            case caSkillUse: {
                SkillID sk = SkillID.forValue(aExt);
                this.fPlayer.useSkill(sk, null);
                break;
            }
        }

        if (this.fTurnState == TurnState.gtsWait) {
            this.setTurnState(TurnState.gtsDone);
        }
    }

    public static String getSaveFile(int kind, int index)
    {
        String result = ResourceManager.getAppPath() + "save/rgame_" + String.valueOf(index);

        switch (kind) {
            case NWGameSpace.SAVEFILE_PLAYER:
                result += ".rgp";
                break;
            case NWGameSpace.SAVEFILE_TERRAINS:
                result += ".rgt";
                break;
            case NWGameSpace.SAVEFILE_JOURNAL:
                result += ".rgj";
                break;                
        }

        return result;
    }

    private void checkPath()
    {
        String path = ResourceManager.getAppPath() + "/save";
        if (!(new File(path)).isDirectory()) {
            (new File(path)).mkdir();
        }
    }

    public final void eraseGame(int index)
    {
        try {
            (new java.io.File(NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_PLAYER, index))).delete();
            (new java.io.File(NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_TERRAINS, index))).delete();
            (new java.io.File(NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_JOURNAL, index))).delete();
        } catch (Exception ex) {
            Logger.write("NWGameSpace.eraseGame(): " + ex.getMessage());
        }
    }

    public static void loadPlayer(int index, Player player)
    {
        //aSaveTime.argValue = java.util.Date.FromBinary(0);

        try {
            String fName = NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_PLAYER, index);
            FileInputStream aStream = new FileInputStream(fName);
            try (BinaryInputStream dis = new BinaryInputStream(aStream, AuxUtils.binEndian)) {
                FileHeader header = new FileHeader();
                header.read(dis);

                player.loadFromStream(dis, header.Version);

                if (dis.available() == 0) {
                    Logger.write("playerLoad(): ok");
                } else {
                    Logger.write("playerLoad(): fail");
                }
            } finally {
                aStream.close();
            }
        } catch (IOException ex) {
            Logger.write("NWGameSpace.loadPlayer.io(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("NWGameSpace.loadPlayer(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void loadGame(int index)
    {
        this.checkPath();

        try {
            this.fPlayer.leaveField();
            NWGameSpace.loadPlayer(index, this.fPlayer);

            int total = 0;
            int num = this.fLayers.size();
            for (int i = 0; i < num; i++) {
                NWLayer layer = this.fLayers.get(i);
                total += layer.getH() * layer.getW();
            }

            GlobalVars.nwrWin.ProgressInit(total);
            GlobalVars.nwrWin.ProgressLabel(Locale.getStr(RS.rs_LevelsLoading) + " (0/" + String.valueOf(this.fLayers.size()) + ")");

            try {
                FileInputStream fileStream = new FileInputStream(NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_TERRAINS, index));
                try (BinaryInputStream dis = new BinaryInputStream(fileStream, AuxUtils.binEndian)) {
                    FileHeader header = new FileHeader();
                    header.read(dis);

                    if (header.Version.Revision >= 4) {
                        this.fTime.loadFromStream(dis, header.Version);
                    } else {
                        this.resetTime(this.fTime);
                    }

                    int num2 = this.fLayers.size();
                    for (int i = 0; i < num2; i++) {
                        this.fLayers.get(i).loadFromStream(dis, header.Version);
                        GlobalVars.nwrWin.ProgressLabel(Locale.getStr(RS.rs_LevelsLoading) + " (" + String.valueOf(i + 1) + "/" + String.valueOf(num2) + ")");
                    }

                    if (header.Version.Revision >= 3) {
                        if (header.Version.Revision <= 14) {
                            IntList oldExtincted = new IntList();
                            oldExtincted.loadFromStream(dis, header.Version);

                            int num3 = oldExtincted.getCount();
                            for (int i = 0; i < num3; i++) {
                                int id = oldExtincted.get(i);
                                DataEntry entry = this.getDataEntry(id);
                                if (entry instanceof VolatileEntry) {
                                    ((VolatileEntry) entry).RuntimeState = VolatileState.vesExtincted;
                                }
                            }
                            oldExtincted.dispose();
                        } else {
                            this.loadVolatiles(dis, header.Version);
                        }
                    }

                    if (dis.available() == 0) {
                        Logger.write("terrainsLoad(): ok");
                    } else {
                        Logger.write("terrainsLoad(): fail");
                    }
                } finally {
                    fileStream.close();
                }
            } catch (IOException e) {
                Logger.write("NWGameSpace.loadGame.io(): " + e.getMessage());
                //throw e;
            }

            this.fJournal.load(NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_JOURNAL, index));
            
            int num4 = this.fLayers.size();
            for (int i = 0; i < num4; i++) {
                NWLayer layer = this.fLayers.get(i);
                
                for (int fy = 0; fy < layer.getH(); fy++) {
                    for (int fx = 0; fx < layer.getW(); fx++) {
                        NWField fld = layer.getField(fx, fy);
                        GameScreen scr = fld.getLandEntry().Splash;
                        if (fld.Visited && scr.status == ScreenStatus.ssOnce) {
                            scr.status = ScreenStatus.ssAlready;
                        }
                    }
                }
            }

            this.fPlayer.transferTo(this.fPlayer.LayerID, this.fPlayer.getField().X, this.fPlayer.getField().Y, this.fPlayer.getPosX(), this.fPlayer.getPosY(), StaticData.MapArea, true, false);
            GlobalVars.nwrWin.ProgressDone();
            this.fFileIndex = index;
            
            GlobalVars.nwrWin.playSound("game_load.ogg", SoundEngine.sk_Sound, -1, -1);
        } catch (Exception ex) {
            Logger.write("NWGameSpace.loadGame(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void saveGame(int index)
    {
        this.checkPath();

        try {
            try {
                FileHeader header = new FileHeader();
                FileOutputStream fileStream = new FileOutputStream(NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_PLAYER, index), false);
                BinaryOutputStream dos = new BinaryOutputStream(fileStream, AuxUtils.binEndian);
                try {
                    System.arraycopy(NWGameSpace.RGP_Sign, 0, header.Sign, 0, 3);
                    header.Version = NWGameSpace.RGF_Version.clone();
                    header.write(dos);

                    this.fPlayer.saveToStream(dos, header.Version);
                } finally {
                    dos.close();
                    fileStream.close();
                }

                int total = 0;
                int num = this.fLayers.size();
                for (int i = 0; i < num; i++) {
                    total += (this.fLayers.get(i).getH() * this.fLayers.get(i).getW());
                }

                GlobalVars.nwrWin.ProgressInit(total);

                fileStream = new FileOutputStream(NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_TERRAINS, index), false);
                dos = new BinaryOutputStream(fileStream, AuxUtils.binEndian);
                try {
                    System.arraycopy(NWGameSpace.RGT_Sign, 0, header.Sign, 0, 3);
                    header.Version = NWGameSpace.RGF_Version.clone();
                    header.write(dos);

                    fTime.saveToStream(dos, header.Version);

                    int num2 = this.fLayers.size();
                    for (int i = 0; i < num2; i++) {
                        this.fLayers.get(i).saveToStream(dos, header.Version);
                        GlobalVars.nwrWin.ProgressLabel(Locale.getStr(RS.rs_LevelsSaving) + " (" + String.valueOf(i + 1) + "/" + String.valueOf(num2) + ")");
                    }

                    this.saveVolatiles(dos, header.Version);
                } finally {
                    dos.close();
                    fileStream.close();
                }

                this.fJournal.save(NWGameSpace.getSaveFile(NWGameSpace.SAVEFILE_JOURNAL, index));

                this.fFileIndex = index;
            } finally {
                GlobalVars.nwrWin.ProgressDone();
            }
            
            GlobalVars.nwrWin.playSound("game_save.ogg", SoundEngine.sk_Sound, -1, -1);
        } catch (IOException ex) {
            Logger.write("NWGameSpace.saveGame.IO(): " + ex.getMessage());
            //throw ex;
        } catch (Exception ex) {
            Logger.write("NWGameSpace.saveGame(): " + ex.getMessage());
            throw ex;
        }
    }

    public final String getEventMessage(EventID eventID, Object sender, Object receiver, Object data)
    {
        String result = "";

        try {
            switch (eventID) {
                case event_LandEnter: {
                    Player player = (Player) sender;
                    LandEntry eLand = (LandEntry) data;
                    if (player.LandID != eLand.GUID) {
                        result = String.format(Locale.getStr(RS.rs_EnterInLand), eLand.getNounDeclension(Number.nSingle, Case.cAccusative));
                        player.LandID = eLand.GUID;
                    }
                    break;
                }

                case event_Intro: {
                    Player player = (Player) sender;
                    result = String.format(Locale.getStr(RS.rs_IntroMsg), String.valueOf((int) this.getTime().Year), this.getDataEntry(player.CLSID).getName() + " " + player.getName());
                    break;
                }

                case event_Attack: {
                    NWCreature _sender = (NWCreature) sender;
                    NWCreature _enemy = (NWCreature) receiver;
                    if (_sender.isPlayer()) {
                        String dummy = _enemy.getDeclinableName(Number.nSingle, Case.cAccusative);
                        String verb = StaticData.getVerbRes(RS.rs_Verb_Attack, _sender.Sex, Grammar.VOICE_ACTIVE);
                        result = _sender.getName() + " " + verb + " " + dummy + ".";
                    } else {
                        String dummy = _sender.getDeclinableName(Number.nSingle, Case.cInstrumental);
                        String verb = StaticData.getVerbRes(RS.rs_Verb_Attack, _enemy.Sex, Grammar.VOICE_PASSIVE);
                        result = _enemy.getName() + " " + verb + " " + dummy + ".";
                    }
                    break;
                }

                case event_Killed:
                case event_Wounded: {
                    NWCreature _sender = (NWCreature) sender;
                    if (eventID != EventID.event_Killed) {
                        if (eventID == EventID.event_Wounded) {
                            String verb = StaticData.getVerbRes(RS.rs_Verb_Wound, _sender.Sex, Grammar.VOICE_PASSIVE);
                            result = _sender.getName() + " " + verb + ".";
                        }
                    } else {
                        String verb = StaticData.getVerbRes(RS.rs_Verb_Kill, _sender.Sex, Grammar.VOICE_PASSIVE);
                        result = _sender.getName() + " " + verb + ".";
                    }

                    if (eventID == EventID.event_Killed && this.fPlayer.equals(data)) {
                        this.fJournal.killed(_sender.CLSID);
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.getEventMessage(): " + ex.getMessage());
            throw ex;
        }

        return result;
    }

    public final NWField getField(int layerID, int fX, int fY)
    {
        NWLayer layer = this.getLayerByID(layerID);
        return ((layer == null) ? null : layer.getField(fX, fY));
    }

    public final NWLayer getLayerByID(int aID)
    {
        for (NWLayer layer : this.fLayers) {
            if (layer.EntryID == aID) {
                return layer;
            }
        }
        return null;
    }

    public final int getRndExtincted()
    {
        IntList list = new IntList();
        int num = GlobalVars.nwrBase.getEntriesCount();
        for (int i = 0; i < num; i++) {
            DataEntry entry = GlobalVars.nwrBase.getEntry(i);
            if (entry instanceof VolatileEntry && ((VolatileEntry) entry).RuntimeState == VolatileState.vesExtincted) {
                list.add(entry.GUID);
            }
        }

        int i = AuxUtils.getRandom(list.getCount());
        int result = list.get(i);
        list.dispose();

        return result;
    }

    public final NWField getRndFieldByLands(int... lands)
    {
        if (lands == null) {
            return null;
        }

        ArrayList<NWField> fsList = new ArrayList<>();

        int num = this.fLayers.size();
        for (int i = 0; i < num; i++) {
            NWLayer layer = this.fLayers.get(i);

            for (int fy = 0; fy < layer.getH(); fy++) {
                for (int fx = 0; fx < layer.getW(); fx++) {
                    NWField fld = layer.getField(fx, fy);

                    if (AuxUtils.indexOfInt(fld.LandID, lands) >= 0) {
                        fsList.add(fld);
                    }
                }
            }
        }

        if (fsList.size() > 0) {
            int i = AuxUtils.getRandom(fsList.size());
            return fsList.get(i);
        }

        return null;
    }

    public final short getTeachablePrice(int teachableIndex, int curLev)
    {
        int price = 0;
        int id = StaticData.dbTeachable[teachableIndex].id;
        TeachableKind kind = StaticData.dbTeachable[teachableIndex].kind;

        switch (kind) {
            case Ability:
                price = 1000;
                break;

            case Skill: {
                SkillID sk = SkillID.forValue(id);
                EffectID eff = sk.Effect;

                int num = GlobalVars.dbItems.getCount();
                for (int i = 0; i < num; i++) {
                    ItemEntry eItem = (ItemEntry) this.getDataEntry(GlobalVars.dbItems.get(i));
                    EffectEntry[] effects = eItem.Effects;
                    if (((effects != null) ? effects.length : 0) > 0 && eItem.Effects[0].EffID == eff) {
                        price = (int) eItem.Price;
                        break;
                    }
                }
            }
            break;
        }

        if (price == 0) {
            price = 1000;
        }

        LeaderBrain party = (LeaderBrain) this.fPlayer.getBrain();
        float f = (1.0f + (float) party.getMembersCount() / 10.0f) * (1.0f + (curLev + 1) / 5.0f);
        return (short) (Math.round(price * f));
    }

    public final StringList getVisitedLands()
    {
        StringList result = new StringList();
        IntList lands = new IntList();
        try {
            int num = this.fLayers.size();
            for (int i = 0; i < num; i++) {
                NWLayer layer = this.fLayers.get(i);

                int num2 = layer.getH();
                for (int fy = 0; fy < num2; fy++) {
                    int num3 = layer.getW();
                    for (int fx = 0; fx < num3; fx++) {
                        NWField fld = layer.getField(fx, fy);
                        int id = fld.LandID;
                        if (fld.Visited && lands.indexOf(id) < 0) {
                            lands.add(id);
                        }
                    }
                }
            }

            int num4 = lands.getCount();
            for (int i = 0; i < num4; i++) {
                int id = lands.get(i);
                result.addObject(this.getDataEntry(id).getName(), id);
            }
        } finally {
            lands.dispose();
        }
        result.sort();
        return result;
    }

    public final VolatileState getVolatileState(int id)
    {
        VolatileState result;

        DataEntry entry = this.getDataEntry(id);
        if (entry instanceof VolatileEntry) {
            result = ((VolatileEntry) entry).RuntimeState;
        } else {
            result = VolatileState.vesNone;
        }

        return result;
    }

    private void FinalDisplacement(int cid, boolean aCreate, int aLand)
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
            px = AuxUtils.getBoundedRnd(29, 68);
            py = AuxUtils.getBoundedRnd(3, StaticData.FieldHeight - 4);
        }

        NWCreature cr = this.findCreature(cid);
        if (cr != null) {
            cr.transferTo(GlobalVars.Layer_Asgard, fx, fy, px, py, StaticData.MapArea, true, true);
        } else {
            if (aCreate) {
                this.addCreatureEx(GlobalVars.Layer_Asgard, fx, fy, px, py, cid);
            }
        }
    }

    public final NWCreature findCreature(int aID)
    {
        int num = this.fLayers.size();
        for (int i = 0; i < num; i++) {
            NWLayer layer = this.fLayers.get(i);

            int num2 = layer.getH();
            for (int y = 0; y < num2; y++) {
                int num3 = layer.getW();
                for (int x = 0; x < num3; x++) {
                    GameEntity cr = layer.getField(x, y).getCreatures().findByCLSID(aID);
                    if (cr != null) {
                        return ((NWCreature) ((cr instanceof NWCreature) ? cr : null));
                    }
                }
            }
        }

        return null;
    }
    
    private void genForest()
    {
        //int fx = AuxUtils.BoundedRnd(3, 4); // 2, 3
        int fy = AuxUtils.getBoundedRnd(3, 4);

        NWField fldFB = this.getField(GlobalVars.Layer_Midgard, 3, fy);
        Rect area = fldFB.getAreaRect();
        area.inflate(-7, -4);

        Building house = new Building(this, fldFB);
        if (house.build((byte)1, (byte)4, (byte)5, area)) {
            fldFB.getFeatures().add(house);

            Rect brt = house.getArea().clone();
            int x = AuxUtils.getBoundedRnd(brt.Left, brt.Right);
            int y = AuxUtils.getBoundedRnd(brt.Top, brt.Bottom);
            NWCreature oldman = this.addCreatureEx(fldFB.getLayer().EntryID, fldFB.getCoords().X, fldFB.getCoords().Y, x, y, GlobalVars.cid_Oldman);
            house.Holder = oldman;
            ((SentientBrain) oldman.getBrain()).setAreaGuardGoal(brt);
        } else {
            house.dispose();
            house = null;
        }

        Point bd = null;
        if (house != null) {
            Door door = house.getDoor(0);
            bd = fldFB.getLayerTileCoords(door.X, door.Y);
        }

        NWLayer layer = this.getLayerByID(GlobalVars.Layer_Midgard);

        if (bd != null) {
            NWField fldV = this.getField(GlobalVars.Layer_Midgard, 2, 2);
            Village village = fldV.findVillage();

            Point vd = null;
            int max = layer.getWidth();
            for (int i = 0; i < village.Gates.size(); i++) {
                Point gpt = village.Gates.get(i);
                gpt = fldV.getLayerTileCoords(gpt.X, gpt.Y);
                int d = AuxUtils.distance(bd.X, bd.Y, gpt.X, gpt.Y);
                if (d < max) {
                    vd = gpt;
                    max = d;
                }
            }

            if (vd != null) {
                UniverseBuilder.gen_Road(layer, bd.X, bd.Y, vd.X, vd.Y, layer.getAreaRect(), PlaceID.pid_RoadMask2);
            }
        }

        // ancient roads
        genMidgardRoad(layer, 2, 4, 4, 3);
        genMidgardRoad(layer, 4, 2, 3, 4);
    }

    private void genMidgardRoad(NWLayer layer, int fx1, int fy1, int fx2, int fy2)
    {
        NWField fld1 = this.getField(GlobalVars.Layer_Midgard, fx1, fy1);
        Point pt1 = fld1.getLayerTileCoords(fld1.searchFreeLocation());

        NWField fld2 = this.getField(GlobalVars.Layer_Midgard, fx2, fy2);
        Point pt2 = fld2.getLayerTileCoords(fld2.searchFreeLocation());
        
        UniverseBuilder.gen_Road(layer, pt1.X, pt1.Y, pt2.X, pt2.Y, layer.getAreaRect(), PlaceID.pid_RoadMask2);
    }

    private void genGiollRiver()
    {
        // gate from Field_Midgard56 [63, 8] to Field_Niflheim20, -1, -1
        NWField fld = this.getField(GlobalVars.Layer_Midgard, 5, 6);
        fld.addGate(PlaceID.pid_StairsDown, 63, 8, GlobalVars.Layer_Niflheim, new Point(2, 0), new Point(-1, -1));

        Point pt = new Point(63, 8);

        NWCreature h = fld.addCreature(pt.X, pt.Y, GlobalVars.cid_Harbard);
        ((SentientBrain) h.getBrain()).setPointGuardGoal(pt);

        UniverseBuilder.gen_GiollFog(fld, pt.X, pt.Y);
    }

    private void genJotenheim()
    {
        // generate the "Gymir Castle"
        NWField fld = this.getField(GlobalVars.Layer_Midgard, 5, 5);
        int aX = AuxUtils.getBoundedRnd(5, StaticData.FieldWidth - 5);
        int aY = AuxUtils.getBoundedRnd(2, StaticData.FieldHeight - 8);
        Rect rtx = new Rect(0, 0, 4, 5);

        for (int yy = 0; yy <= rtx.Bottom; yy++) {
            for (int xx = 0; xx <= rtx.Right; xx++) {
                NWTile tile = fld.getTile(aX + xx, aY + yy);
                if (rtx.isBorder(xx, yy)) {
                    tile.setBack(PlaceID.pid_Grass);
                    if (yy == 5) {
                        tile.setFore(PlaceID.pid_Tree);
                    } else {
                        tile.setFore(PlaceID.pid_Mountain);
                    }
                } else {
                    tile.setBack(PlaceID.pid_Floor);
                    if (NWGameSpace.Castle.isBorder(xx, yy)) {
                        tile.setFore(NWField.getBuildPlaceKind(xx, yy, NWGameSpace.Castle, false));
                    }
                }
            }
        }

        fld.getTile(aX + 2, aY + 4).setFore(PlaceID.pid_DoorS);
        fld.addCreature(aX + 2, aY + 3, GlobalVars.cid_Gymir);
        
        fld = this.getField(GlobalVars.Layer_Midgard, 5, 3);
        UniverseBuilder.build_Jotenheim_Jarnvidr(fld);
    }

    private void genNiflheim()
    {
        this.addCreatureEx(GlobalVars.Layer_Niflheim, 0, 0, -1, -1, GlobalVars.cid_KonrRig);
        this.addCreatureEx(GlobalVars.Layer_Niflheim, 0, 1, -1, -1, GlobalVars.cid_Plog);
        this.addCreatureEx(GlobalVars.Layer_Niflheim, 0, 2, -1, -1, GlobalVars.cid_Gulveig);
        this.addCreatureEx(GlobalVars.Layer_Niflheim, 1, 0, -1, -1, GlobalVars.cid_Vanseril);
        this.addCreatureEx(GlobalVars.Layer_Niflheim, 1, 1, -1, -1, GlobalVars.cid_Emanon);
        this.addCreatureEx(GlobalVars.Layer_Niflheim, 1, 2, -1, -1, GlobalVars.cid_Nidhogg);
        this.addCreatureEx(GlobalVars.Layer_Niflheim, 2, 1, -1, -1, GlobalVars.cid_Anxarcule);
        this.addCreatureEx(GlobalVars.Layer_Niflheim, 2, 2, -1, -1, GlobalVars.cid_Hela);
    }

    private void genNidavellir()
    {
        // generate the "Uorik Castle" and gates
        NWField fldNidavellir = this.getField(GlobalVars.Layer_Midgard, 4, 6);
        NWField fldArmory = this.getField(GlobalVars.Layer_Armory, 0, 0);

        int aX = AuxUtils.getBoundedRnd(5, StaticData.FieldWidth - 5 - NWGameSpace.CastleSize);
        int aY = AuxUtils.getBoundedRnd(4, StaticData.FieldHeight - 4 - NWGameSpace.CastleSize);
        fldNidavellir.fillBorder(aX, aY, aX + 4, aY + 4, PlaceID.pid_Stalagmite, true);

        Point nidPt = new Point(aX + 2, aY + 2);
        Point armPt = fldArmory.searchFreeLocation();

        fldNidavellir.addGate(PlaceID.pid_StairsDown, nidPt.X, nidPt.Y, GlobalVars.Layer_Armory, new Point(0, 0), armPt);
        fldArmory.addGate(PlaceID.pid_StairsUp, armPt.X, armPt.Y, GlobalVars.Layer_Midgard, new Point(4, 6), nidPt);
        this.addCreatureEx(GlobalVars.Layer_Midgard, 4, 6, aX + 2, aY + 3, GlobalVars.cid_Uorik);
    }

    private void genAsgard()
    {
        // gate from Field_Asgard00 [40, 11] to Field_GodsFortress00, 40, 11
        NWField fld = this.getField(GlobalVars.Layer_Asgard, 0, 0);
        fld.addGate(PlaceID.pid_StairsUp, 40, 11, GlobalVars.Layer_GodsFortress, new Point(0, 0), new Point(40, 11));

        // gate from Field_GodsFortress00 [40, 11] to Field_Asgard00, 40, 11
        fld = this.getField(GlobalVars.Layer_GodsFortress, 0, 0);
        fld.addGate(PlaceID.pid_StairsDown, 40, 11, GlobalVars.Layer_Asgard, new Point(0, 0), new Point(40, 11));
    }

    private void genLink_Caves2_Caves3_3()
    {
        NWField svart2 = this.getField(GlobalVars.Layer_Svartalfheim2, 1, 2);
        Point sv2pt = svart2.searchFreeLocation();
        
        NWField svart3 = this.getField(GlobalVars.Layer_Svartalfheim3, 1, 2);
        Point sv3pt = svart3.searchFreeLocation();
        
        svart2.addGate(PlaceID.pid_HoleDown, sv2pt.X, sv2pt.Y, GlobalVars.Layer_Svartalfheim3, new Point(1, 2), sv3pt);
        svart3.addGate(PlaceID.pid_HoleUp, sv3pt.X, sv3pt.Y, GlobalVars.Layer_Svartalfheim2, new Point(1, 2), sv2pt);
        
        svart3.addItem(sv3pt.X, sv3pt.Y, GlobalVars.nwrBase.findEntryBySign("LazlulRope").GUID);
        svart3.addItem(sv3pt.X, sv3pt.Y, GlobalVars.nwrBase.findEntryBySign("Amulet_Infravision").GUID);
    }

    private void genLink_Caves2_Caves3_2()
    {
        Point fp3 = new Point(0, 0);

        NWField fldSvart2 = this.getField(GlobalVars.Layer_Svartalfheim2, fp3.X, fp3.Y);
        Point sv2pt = fldSvart2.searchFreeLocation();
        
        NWField fldSvart3 = this.getField(GlobalVars.Layer_Svartalfheim3, fp3.X, fp3.Y);
        Point sv3pt = fldSvart3.searchFreeLocation();
        
        fldSvart2.addGate(PlaceID.pid_GStairsDown, sv2pt.X, sv2pt.Y, GlobalVars.Layer_Svartalfheim3, new Point(fp3.X, fp3.Y), sv3pt);
        fldSvart3.addGate(PlaceID.pid_GStairsUp, sv3pt.X, sv3pt.Y, GlobalVars.Layer_Svartalfheim2, new Point(fp3.X, fp3.Y), sv2pt);
    }

    private void genLink_Caves2_Caves3()
    {
        Point fp3 = new Point(AuxUtils.getBoundedRnd(1, 2), AuxUtils.getBoundedRnd(0, 1));

        NWField fldSvart2 = this.getField(GlobalVars.Layer_Svartalfheim2, fp3.X, fp3.Y);
        Point sv2pt = fldSvart2.searchFreeLocation();
        
        NWField fldSvart3 = this.getField(GlobalVars.Layer_Svartalfheim3, fp3.X, fp3.Y);
        Point sv3pt = fldSvart3.searchFreeLocation();
        
        fldSvart2.addGate(PlaceID.pid_StairsDown, sv2pt.X, sv2pt.Y, GlobalVars.Layer_Svartalfheim3, new Point(fp3.X, fp3.Y), sv3pt);
        fldSvart3.addGate(PlaceID.pid_StairsUp, sv3pt.X, sv3pt.Y, GlobalVars.Layer_Svartalfheim2, new Point(fp3.X, fp3.Y), sv2pt);
    }

    private void genLink_Caves1_Caves2()
    {
        Point fp3 = new Point(AuxUtils.getBoundedRnd(0, 2), AuxUtils.getBoundedRnd(0, 2));

        NWField fldSvart1 = this.getField(GlobalVars.Layer_Svartalfheim1, fp3.X, fp3.Y);
        Point sv1pt = fldSvart1.searchFreeLocation();
        
        NWField fldSvart2 = this.getField(GlobalVars.Layer_Svartalfheim2, fp3.X, fp3.Y);
        Point sv2pt = fldSvart2.searchFreeLocation();
        
        fldSvart1.addGate(PlaceID.pid_StairsDown, sv1pt.X, sv1pt.Y, GlobalVars.Layer_Svartalfheim2, new Point(fp3.X, fp3.Y), sv2pt);
        fldSvart2.addGate(PlaceID.pid_StairsUp, sv2pt.X, sv2pt.Y, GlobalVars.Layer_Svartalfheim1, new Point(fp3.X, fp3.Y), sv1pt);
    }

    private void genLink_Midgard_GrynrHalls()
    {
        NWField fldMidgard35 = this.getField(GlobalVars.Layer_Midgard, 3, 5);
        Point midPt = fldMidgard35.searchFreeLocation();

        NWField fldGrynrHalls = this.getField(GlobalVars.Layer_GrynrHalls, 0, 0);
        Point ghPt = fldGrynrHalls.searchFreeLocation();

        fldMidgard35.addGate(PlaceID.pid_StairsDown, midPt.X, midPt.Y, GlobalVars.Layer_GrynrHalls, new Point(0, 0), ghPt);
        fldGrynrHalls.addGate(PlaceID.pid_StairsUp, ghPt.X, ghPt.Y, GlobalVars.Layer_Midgard, new Point(3, 5), midPt);
    }

    private void genLink_MimerRealm_Caves1()
    {
        NWField fldMimerRealm = this.getField(GlobalVars.Layer_Midgard, 5, 2);
        Point mrpt = fldMimerRealm.searchFreeLocation();
        
        NWField fldSvart1 = this.getField(GlobalVars.Layer_Svartalfheim1, 2, 0);
        Point sv1pt = fldSvart1.searchFreeLocation();
        
        fldMimerRealm.addGate(PlaceID.pid_VortexStrange, mrpt.X, mrpt.Y, GlobalVars.Layer_Svartalfheim1, new Point(2, 0), sv1pt);
        fldSvart1.addGate(PlaceID.pid_VortexStrange, sv1pt.X, sv1pt.Y, GlobalVars.Layer_Midgard, new Point(5, 2), mrpt);
    }

    private void genLink_MimerRealm_MimerWell()
    {
        NWField fldMimerRealm = this.getField(GlobalVars.Layer_Midgard, 5, 2);
        this.MimerWellPos = fldMimerRealm.searchFreeLocation();

        NWCreature aspenth = this.addCreatureEx(GlobalVars.Layer_MimerWell, 0, 0, -1, -1, GlobalVars.cid_Aspenth);
        Point aspPt = aspenth.getLocation();

        fldMimerRealm.addGate(PlaceID.pid_Well, this.MimerWellPos.X, this.MimerWellPos.Y, GlobalVars.Layer_MimerWell, new Point(0, 0), aspPt);
    }

    private void genLink_SlaeterSea_Caves1()
    {
        NWField fldSlaeterSea = this.getField(GlobalVars.Layer_Midgard, 5, 7);
        Point seaPt = fldSlaeterSea.searchFreeLocation();
        
        Point fp2 = new Point(AuxUtils.getBoundedRnd(0, 2), AuxUtils.getBoundedRnd(0, 2));
        NWField fldSvart1 = this.getField(GlobalVars.Layer_Svartalfheim1, fp2.X, fp2.Y);
        Point sv1pt = fldSvart1.searchFreeLocation();
        
        fldSlaeterSea.addGate(PlaceID.pid_Vortex, seaPt.X, seaPt.Y, GlobalVars.Layer_Svartalfheim1, fp2, sv1pt);
        fldSvart1.addGate(PlaceID.pid_Vortex, sv1pt.X, sv1pt.Y, GlobalVars.Layer_Midgard, new Point(5, 7), seaPt);
    }

    private void genLink_MidgardForest_SlaeterSea()
    {
        NWField fldMidgard44 = this.getField(GlobalVars.Layer_Midgard, 4, 4);
        Point mid44pt = fldMidgard44.searchFreeLocation();
        
        NWField fldMidgard57 = this.getField(GlobalVars.Layer_Midgard, 5, 7);
        Point mid57pt = fldMidgard57.searchFreeLocation();
        
        fldMidgard44.addGate(PlaceID.pid_Vortex, mid44pt.X, mid44pt.Y, GlobalVars.Layer_Midgard, new Point(5, 7), mid57pt);
        fldMidgard57.addGate(PlaceID.pid_Vortex, mid57pt.X, mid57pt.Y, GlobalVars.Layer_Midgard, new Point(4, 4), mid44pt);
        
        fldMidgard57.addCreature(-1, -1, GlobalVars.nwrBase.findEntryBySign("Norseman").GUID);
        fldMidgard57.addItem(26, 6, GlobalVars.iid_Skidbladnir);
    }

    private void genLink_MidgardForest_Caves1()
    {
        NWField fldMidgard44 = this.getField(GlobalVars.Layer_Midgard, 4, 4);
        Point mid44pt = fldMidgard44.searchFreeLocation();
        
        NWField fldSvart1 = this.getField(GlobalVars.Layer_Svartalfheim1, 1, 2);
        Point sv1pt = fldSvart1.searchFreeLocation();
        
        fldMidgard44.addGate(PlaceID.pid_StairsDown, mid44pt.X, mid44pt.Y, GlobalVars.Layer_Svartalfheim1, new Point(1, 2), sv1pt);
        fldSvart1.addGate(PlaceID.pid_StairsUp, sv1pt.X, sv1pt.Y, GlobalVars.Layer_Midgard, new Point(4, 4), mid44pt);
        
        fldMidgard44.addCreature(-1, -1, GlobalVars.cid_Thokk);
    }

    private void genWorld()
    {
        try {
            try {
                int stages = 0;

                int num = this.fLayers.size();
                for (int i = 0; i < num; i++) {
                    LayerEntry layerEntry = this.fLayers.get(i).getEntry();
                    stages += layerEntry.H * layerEntry.W * 3;
                }

                GlobalVars.nwrWin.ProgressInit(stages);

                int num2 = this.fLayers.size();
                for (int i = 0; i < num2; i++) {
                    GlobalVars.nwrWin.ProgressLabel(Locale.getStr(RS.rs_LevelsGen) + " (" + String.valueOf(i + 1) + "/" + String.valueOf(num2) + ")");
                    this.fLayers.get(i).initLayer();
                    GlobalVars.nwrWin.ProgressStep();
                }

                this.genForest();

                this.genLink_MidgardForest_Caves1();
                this.genLink_MidgardForest_SlaeterSea();
                this.genLink_SlaeterSea_Caves1();
                this.genLink_MimerRealm_MimerWell();
                this.genLink_MimerRealm_Caves1();

                this.genAsgard();
                this.genNidavellir();
                this.addCreatureEx(GlobalVars.Layer_Armory, 0, 0, -1, -1, GlobalVars.cid_Eitri);
                this.addCreatureEx(GlobalVars.Layer_Midgard, 1, 6, 46, 0, GlobalVars.cid_Heimdall);
                this.genNiflheim();
                this.genJotenheim();
                this.genGiollRiver();

                this.genLink_Midgard_GrynrHalls();
                this.genLink_Caves1_Caves2();

                this.genLink_Caves2_Caves3();
                this.genLink_Caves2_Caves3_2();
                this.genLink_Caves2_Caves3_3();
                
                this.genGhosts();
            } finally {
                GlobalVars.nwrWin.ProgressDone();
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.genWorld(): " + ex.getMessage());
        }
    }

    private void genGhosts()
    {
        GhostsList ghostsList = GlobalVars.nwrWin.getGhostsList();
        
        for (int i = 0; i < ghostsList.getGhostCount(); i++) {
            Ghost ghost = ghostsList.getGhost(i);

            NWCreature gst = new NWCreature(this, null);
            gst.assign(ghost, false);
            gst.getItems().clear();

            Point pt = ghost.getLocation();
            gst.transferTo(ghost.LayerID, ghost.getField().X, ghost.getField().Y, pt.X, pt.Y, StaticData.MapArea, true, false);
            gst.Ghost = true;
            gst.GhostIdx = i;

            ((BeastBrain) gst.getBrain()).setPointGuardGoal(pt);
        }
    }
    
    public final void InitBegin()
    {
        try {
            this.fPlayer.leaveField();

            this.fFileIndex = -1;
            this.IsRagnarok = false;
            this.clearVolatiles();
            this.resetTime(this.fTime);

            this.fJournal.storeTime(this.fTime);

            this.genWorld();
            this.genMainQuests();

            this.fPlayer.setCLSID(GlobalVars.cid_Viking);
            this.fPlayer.setName(Locale.getStr(RS.rs_Unknown));
        } catch (Exception ex) {
            Logger.write("NWGameSpace.InitBegin(): " + ex.getMessage());
        }
    }

    public final void InitEnd()
    {
        try {
            NWField fld = this.getField(GlobalVars.Layer_Midgard, 2, 2);
            Village village = fld.findVillage();

            Rect area;
            if (village == null) {
                area = fld.getAreaRect();
            } else {
                area = village.getArea();
            }

            this.fPlayer.transferTo(GlobalVars.Layer_Midgard, 2, 2, -1, -1, area, true, true);
        } catch (Exception ex) {
            Logger.write("NWGameSpace.InitEnd(): " + ex.getMessage());
        }
    }

    public final void resetTime(NWDateTime time)
    {
        time.Year = 609;
        time.Month = 7;
        time.Day = 15;
        time.Hour = 11;
        time.Minute = 45;
        time.Second = 0;
    }
    
    public final void selectHero(String paSign, String pName)
    {
        int id = GlobalVars.nwrBase.findEntryBySign(paSign).GUID;
        this.fPlayer.initEx(id, true, false);
        this.fPlayer.setName(pName);
        this.fPlayer.setState(CreatureState.csAlive);
        this.fPlayer.Turn = 0;
        this.fPlayer.setSourceForm();

        this.doEvent(EventID.event_Intro, this.fPlayer, null, null);
    }

    public final void prepareEntries()
    {
        GlobalVars.cid_Viking = GlobalVars.nwrBase.findEntryBySign("Viking").GUID;
        GlobalVars.cid_Alchemist = GlobalVars.nwrBase.findEntryBySign("Alchemist").GUID;
        GlobalVars.cid_Blacksmith = GlobalVars.nwrBase.findEntryBySign("Blacksmith").GUID;
        GlobalVars.cid_Conjurer = GlobalVars.nwrBase.findEntryBySign("Conjurer").GUID;
        GlobalVars.cid_Sage = GlobalVars.nwrBase.findEntryBySign("Sage").GUID;
        GlobalVars.cid_Woodsman = GlobalVars.nwrBase.findEntryBySign("Woodsman").GUID;
        GlobalVars.cid_Merchant = GlobalVars.nwrBase.findEntryBySign("Merchant").GUID;
        GlobalVars.cid_Eitri = GlobalVars.nwrBase.findEntryBySign("Eitri").GUID;
        GlobalVars.cid_Hela = GlobalVars.nwrBase.findEntryBySign("Hela").GUID;
        GlobalVars.cid_Thokk = GlobalVars.nwrBase.findEntryBySign("Thokk").GUID;
        GlobalVars.cid_Balder = GlobalVars.nwrBase.findEntryBySign("Balder").GUID;
        GlobalVars.cid_Heimdall = GlobalVars.nwrBase.findEntryBySign("Heimdall").GUID;
        GlobalVars.cid_Thor = GlobalVars.nwrBase.findEntryBySign("Thor").GUID;
        GlobalVars.cid_Tyr = GlobalVars.nwrBase.findEntryBySign("Tyr").GUID;
        GlobalVars.cid_Freyr = GlobalVars.nwrBase.findEntryBySign("Freyr").GUID;
        GlobalVars.cid_Odin = GlobalVars.nwrBase.findEntryBySign("Odin").GUID;
        GlobalVars.cid_Jormungand = GlobalVars.nwrBase.findEntryBySign("Jormungand").GUID;
        GlobalVars.cid_Garm = GlobalVars.nwrBase.findEntryBySign("Garm").GUID;
        GlobalVars.cid_Loki = GlobalVars.nwrBase.findEntryBySign("Loki").GUID;
        GlobalVars.cid_Surtr = GlobalVars.nwrBase.findEntryBySign("Surtr").GUID;
        GlobalVars.cid_Fenrir = GlobalVars.nwrBase.findEntryBySign("Fenrir").GUID;
        GlobalVars.cid_Emanon = GlobalVars.nwrBase.findEntryBySign("Emanon").GUID;
        GlobalVars.cid_Nidhogg = GlobalVars.nwrBase.findEntryBySign("Nidhogg").GUID;
        GlobalVars.cid_Anxarcule = GlobalVars.nwrBase.findEntryBySign("Anxarcule").GUID;
        GlobalVars.cid_KonrRig = GlobalVars.nwrBase.findEntryBySign("KonrRig").GUID;
        GlobalVars.cid_Plog = GlobalVars.nwrBase.findEntryBySign("Plog").GUID;
        GlobalVars.cid_Vanseril = GlobalVars.nwrBase.findEntryBySign("Vanseril").GUID;
        GlobalVars.cid_Gulveig = GlobalVars.nwrBase.findEntryBySign("Gulveig").GUID;
        GlobalVars.cid_Hobjoi = GlobalVars.nwrBase.findEntryBySign("Hobjoi").GUID;
        GlobalVars.cid_Gymir = GlobalVars.nwrBase.findEntryBySign("Gymir").GUID;
        GlobalVars.cid_Scyld = GlobalVars.nwrBase.findEntryBySign("Scyld").GUID;
        GlobalVars.cid_Uorik = GlobalVars.nwrBase.findEntryBySign("Uorik").GUID;
        GlobalVars.cid_Vidur = GlobalVars.nwrBase.findEntryBySign("Vidur").GUID;
        GlobalVars.cid_Harbard = GlobalVars.nwrBase.findEntryBySign("Harbard").GUID;
        GlobalVars.cid_Raven = GlobalVars.nwrBase.findEntryBySign("Raven").GUID;
        GlobalVars.cid_Guardsman = GlobalVars.nwrBase.findEntryBySign("Guardsman").GUID;
        GlobalVars.cid_Jarl = GlobalVars.nwrBase.findEntryBySign("Jarl").GUID;
        GlobalVars.cid_Werewolf = GlobalVars.nwrBase.findEntryBySign("Werewolf").GUID;
        GlobalVars.cid_Aspenth = GlobalVars.nwrBase.findEntryBySign("Aspenth").GUID;
        GlobalVars.cid_Norseman = GlobalVars.nwrBase.findEntryBySign("Norseman").GUID;
        GlobalVars.cid_Agnar = GlobalVars.nwrBase.findEntryBySign("Agnar").GUID;
        GlobalVars.cid_Haddingr = GlobalVars.nwrBase.findEntryBySign("Haddingr").GUID;
        GlobalVars.cid_Ketill = GlobalVars.nwrBase.findEntryBySign("Ketill").GUID;
        GlobalVars.cid_Oldman = GlobalVars.nwrBase.findEntryBySign("Oldman").GUID;
        GlobalVars.cid_ShopKeeper = GlobalVars.nwrBase.findEntryBySign("ShopKeeper").GUID;
        GlobalVars.cid_IvyCreeper = GlobalVars.nwrBase.findEntryBySign("IvyCreeper").GUID;
        GlobalVars.cid_Snake = GlobalVars.nwrBase.findEntryBySign("Snake").GUID;

        GlobalVars.Layer_Midgard = GlobalVars.nwrBase.findEntryBySign("Layer_Midgard").GUID;
        GlobalVars.Layer_Svartalfheim1 = GlobalVars.nwrBase.findEntryBySign("Layer_Caves1").GUID;
        GlobalVars.Layer_Svartalfheim2 = GlobalVars.nwrBase.findEntryBySign("Layer_Caves2").GUID;
        GlobalVars.Layer_Svartalfheim3 = GlobalVars.nwrBase.findEntryBySign("Layer_Caves3").GUID;
        GlobalVars.Layer_Asgard = GlobalVars.nwrBase.findEntryBySign("Layer_Asgard").GUID;
        GlobalVars.Layer_MimerWell = GlobalVars.nwrBase.findEntryBySign("Layer_MimerWell").GUID;
        GlobalVars.Layer_Vanaheim = GlobalVars.nwrBase.findEntryBySign("Layer_Vanaheim").GUID;
        GlobalVars.Layer_Niflheim = GlobalVars.nwrBase.findEntryBySign("Layer_Niflheim").GUID;
        GlobalVars.Layer_Armory = GlobalVars.nwrBase.findEntryBySign("Layer_Armory").GUID;
        GlobalVars.Layer_GrynrHalls = GlobalVars.nwrBase.findEntryBySign("Layer_GrynrHalls").GUID;
        GlobalVars.Layer_Crossroads = GlobalVars.nwrBase.findEntryBySign("Layer_Crossroads").GUID;
        GlobalVars.Layer_Muspelheim = GlobalVars.nwrBase.findEntryBySign("Layer_Muspelheim").GUID;
        GlobalVars.Layer_Wasteland = GlobalVars.nwrBase.findEntryBySign("Layer_Wasteland").GUID;
        GlobalVars.Layer_GodsFortress = GlobalVars.nwrBase.findEntryBySign("Layer_GodsFortress").GUID;

        GlobalVars.Land_Crossroads = GlobalVars.nwrBase.findEntryBySign("Land_Crossroads").GUID;
        GlobalVars.Land_Valhalla = GlobalVars.nwrBase.findEntryBySign("Land_Valhalla").GUID;
        GlobalVars.Land_Ocean = GlobalVars.nwrBase.findEntryBySign("Land_Ocean").GUID;
        GlobalVars.Land_Bifrost = GlobalVars.nwrBase.findEntryBySign("Land_Bifrost").GUID;
        GlobalVars.Land_Vigrid = GlobalVars.nwrBase.findEntryBySign("Land_Vigrid").GUID;
        GlobalVars.Land_Niflheim = GlobalVars.nwrBase.findEntryBySign("Land_Niflheim").GUID;
        GlobalVars.Land_GiollRiver = GlobalVars.nwrBase.findEntryBySign("Land_GiollRiver").GUID;
        GlobalVars.Land_SlaeterSea = GlobalVars.nwrBase.findEntryBySign("Land_SlaeterSea").GUID;
        GlobalVars.Land_VidRiver = GlobalVars.nwrBase.findEntryBySign("Land_VidRiver").GUID;
        GlobalVars.Land_Caves = GlobalVars.nwrBase.findEntryBySign("Land_Caves").GUID;
        GlobalVars.Land_DeepCaves = GlobalVars.nwrBase.findEntryBySign("Land_DeepCaves").GUID;
        GlobalVars.Land_GreatCaves = GlobalVars.nwrBase.findEntryBySign("Land_GreatCaves").GUID;
        GlobalVars.Land_Crypt = GlobalVars.nwrBase.findEntryBySign("Land_Crypt").GUID;
        GlobalVars.Land_Bazaar = GlobalVars.nwrBase.findEntryBySign("Land_Bazaar").GUID;
        GlobalVars.Land_Nidavellir = GlobalVars.nwrBase.findEntryBySign("Land_Nidavellir").GUID;
        GlobalVars.Land_Forest = GlobalVars.nwrBase.findEntryBySign("Land_Forest").GUID;
        GlobalVars.Land_Village = GlobalVars.nwrBase.findEntryBySign("Land_Village").GUID;
        GlobalVars.Land_Jotenheim = GlobalVars.nwrBase.findEntryBySign("Land_Jotenheim").GUID;
        GlobalVars.Land_Wasteland = GlobalVars.nwrBase.findEntryBySign("Land_Wasteland").GUID;
        GlobalVars.Land_Muspelheim = GlobalVars.nwrBase.findEntryBySign("Land_Muspelheim").GUID;
        GlobalVars.Land_Alfheim = GlobalVars.nwrBase.findEntryBySign("Land_Alfheim").GUID;
        GlobalVars.Land_MimerRealm = GlobalVars.nwrBase.findEntryBySign("Land_MimerRealm").GUID;
        GlobalVars.Land_MimerWell = GlobalVars.nwrBase.findEntryBySign("Land_MimerWell").GUID;
        GlobalVars.Land_GodsFortress = GlobalVars.nwrBase.findEntryBySign("Land_GodsFortress").GUID;
        GlobalVars.Land_GrynrHalls = GlobalVars.nwrBase.findEntryBySign("Land_GrynrHalls").GUID;
        GlobalVars.Land_Temple = GlobalVars.nwrBase.findEntryBySign("Land_Temple").GUID;
        GlobalVars.Land_Armory = GlobalVars.nwrBase.findEntryBySign("Land_Armory").GUID;
        GlobalVars.Land_Vanaheim = GlobalVars.nwrBase.findEntryBySign("Land_Vanaheim").GUID;

        GlobalVars.Field_Bifrost = GlobalVars.nwrBase.findEntryBySign("Field_Midgard16").GUID;
        GlobalVars.Field_Bazaar = GlobalVars.nwrBase.findEntryBySign("Field_Caves322").GUID;

        GlobalVars.iid_Coin = GlobalVars.nwrBase.findEntryBySign("Coin").GUID;
        GlobalVars.iid_DeadBody = GlobalVars.nwrBase.findEntryBySign("DeadBody").GUID;
        GlobalVars.iid_Arrow = GlobalVars.nwrBase.findEntryBySign("Arrow").GUID;
        GlobalVars.iid_Bolt = GlobalVars.nwrBase.findEntryBySign("Bolt").GUID;
        GlobalVars.iid_LongBow = GlobalVars.nwrBase.findEntryBySign("LongBow").GUID;
        GlobalVars.iid_CrossBow = GlobalVars.nwrBase.findEntryBySign("CrossBow").GUID;
        GlobalVars.iid_Ingot = GlobalVars.nwrBase.findEntryBySign("Ingot").GUID;
        GlobalVars.iid_SoulTrapping_Ring = GlobalVars.nwrBase.findEntryBySign("Ring_SoulTrapping").GUID;
        GlobalVars.iid_PickAxe = GlobalVars.nwrBase.findEntryBySign("PickAxe").GUID;
        GlobalVars.iid_Tongs = GlobalVars.nwrBase.findEntryBySign("Tongs").GUID;
        GlobalVars.iid_Wand_Fire = GlobalVars.nwrBase.findEntryBySign("Wand_Fire").GUID;
        GlobalVars.iid_Anvil = GlobalVars.nwrBase.findEntryBySign("Anvil").GUID;
        GlobalVars.iid_PlatinumAnvil = GlobalVars.nwrBase.findEntryBySign("PlatinumAnvil").GUID;
        GlobalVars.iid_Vial = GlobalVars.nwrBase.findEntryBySign("Vial").GUID;
        GlobalVars.iid_Flask = GlobalVars.nwrBase.findEntryBySign("Flask").GUID;
        GlobalVars.iid_Mummy = GlobalVars.nwrBase.findEntryBySign("Mummy").GUID;
        GlobalVars.iid_Ocarina = GlobalVars.nwrBase.findEntryBySign("Ocarina").GUID;
        GlobalVars.iid_GlassOcarina = GlobalVars.nwrBase.findEntryBySign("GlassOcarina").GUID;
        GlobalVars.iid_Stylus = GlobalVars.nwrBase.findEntryBySign("Stylus").GUID;
        GlobalVars.iid_Ring_Delusion = GlobalVars.nwrBase.findEntryBySign("Ring_Delusion").GUID;
        GlobalVars.iid_LazlulRope = GlobalVars.nwrBase.findEntryBySign("LazlulRope").GUID;
        GlobalVars.iid_GreenStone = GlobalVars.nwrBase.findEntryBySign("GreenStone").GUID;
        GlobalVars.iid_Lodestone = GlobalVars.nwrBase.findEntryBySign("Lodestone").GUID;
        GlobalVars.iid_Ring_Protection = GlobalVars.nwrBase.findEntryBySign("Ring_Protection").GUID;
        GlobalVars.iid_DiamondNeedle = GlobalVars.nwrBase.findEntryBySign("DiamondNeedle").GUID;
        GlobalVars.iid_Amulet_Eternal_Life = GlobalVars.nwrBase.findEntryBySign("Amulet_Eternal_Life").GUID;
        GlobalVars.iid_Amulet_SertrudEye = GlobalVars.nwrBase.findEntryBySign("Amulet_SertrudEye").GUID;
        GlobalVars.iid_Rnd_NatureObject = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_NatureObject").GUID;
        GlobalVars.iid_Rnd_Scroll = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_Scroll").GUID;
        GlobalVars.iid_Rnd_Potion = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_Potion").GUID;
        GlobalVars.iid_Rnd_Armor = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_Armor").GUID;
        GlobalVars.iid_Rnd_Weapon = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_Weapon").GUID;
        GlobalVars.iid_Rnd_Wand = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_Wand").GUID;
        GlobalVars.iid_Rnd_Food = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_Food").GUID;
        GlobalVars.iid_Rnd_Ring = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_Ring").GUID;
        GlobalVars.iid_Rnd_Amulet = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_Amulet").GUID;
        GlobalVars.iid_Rnd_Tool = GlobalVars.nwrBase.findEntryBySign("Meta_Rnd_Tool").GUID;
        GlobalVars.iid_DwarvenArm = GlobalVars.nwrBase.findEntryBySign("DwarvenArm").GUID;
        GlobalVars.iid_Mimming = GlobalVars.nwrBase.findEntryBySign("Mimming").GUID;
        GlobalVars.iid_Mjollnir = GlobalVars.nwrBase.findEntryBySign("Mjollnir").GUID;
        GlobalVars.iid_Gjall = GlobalVars.nwrBase.findEntryBySign("Gjall").GUID;
        GlobalVars.iid_Gungnir = GlobalVars.nwrBase.findEntryBySign("Gungnir").GUID;
        GlobalVars.iid_Skidbladnir = GlobalVars.nwrBase.findEntryBySign("Skidbladnir").GUID;
        GlobalVars.iid_Flute = GlobalVars.nwrBase.findEntryBySign("WoodenFlute").GUID;
        GlobalVars.iid_Torch = GlobalVars.nwrBase.findEntryBySign("Torch").GUID;
    }

    public final void prepareFieldTurn(NWField fld, boolean local)
    {
        try {
            if (local) {
                fld.doTurn();
            } else {
                fld.getLayer().doTurn();
            }

            if (!GlobalVars.Debug_Freeze) {
                for (int i = fld.getCreatures().getCount() - 1; i >= 0; i--) {
                    fld.getCreatures().getItem(i).resetStamina();
                }

                int rest;
                do {
                    rest = 0;
                    for (int i = fld.getCreatures().getCount() - 1; i >= 0; i--) {
                        NWCreature cr = fld.getCreatures().getItem(i);
                        if (!cr.isPlayer() && cr.getState() != CreatureState.csDead) {
                            cr.doTurn();

                            if (cr.Stamina >= this.fPlayer.getSpeed()) {
                                rest++;
                            }
                        }
                    }

                    for (int i = fld.getCreatures().getCount() - 1; i >= 0; i--) {
                        NWCreature cr = fld.getCreatures().getItem(i);
                        if (!cr.equals(this.fPlayer) && cr.getState() == CreatureState.csDead) {
                            cr = (NWCreature) fld.getCreatures().extract(cr);

                            this.clearEvents(cr);

                            boolean withoutCorpse = cr.getEntry().isWithoutCorpse() || cr.Ghost || cr.Illusion;

                            if (cr.Ghost) {
                                GlobalVars.nwrWin.getGhostsList().delete(cr.GhostIdx);
                            }

                            if (withoutCorpse) {
                                cr.dispose();
                            } else {
                                Item item = new Item(this, fld);
                                item.setCLSID(GlobalVars.iid_DeadBody);
                                item.Count = 1;
                                item.setPos(cr.getPosX(), cr.getPosY());
                                item.getContents().add(cr);
                                if (cr.canSink()) {
                                    GlobalVars.nwrWin.showText(cr, Locale.format(RS.rs_TheDeadSinks, new Object[]{item.getDeclinableName(Number.nSingle, Case.cNominative, false)}));
                                    item.dispose();
                                } else {
                                    fld.getItems().add(item, false);
                                }
                            }
                        }
                    }

                    this.prepareDeads(fld, fld.getItems(), new Point(-1, -1));
                } while (rest != 0);
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.prepareFieldTurn(): " + ex.getMessage());
        }
    }

    public final NWCreature respawnDeadBody(NWField field, Point pos, Item deadBody)
    {
        NWCreature cr = (NWCreature) deadBody.getContents().getItem(0);

        Point rpt;
        if (field.findCreature(pos.X, pos.Y) == null) {
            rpt = pos;
        } else {
            rpt = field.getNearestPlace(pos.X, pos.Y, 3, true, cr.getMovements());
            if (rpt == null) {
                rpt = pos;
                Logger.write("NWGameSpace.respawnDeadBody(): empty point");
            }
        }

        ItemsList iList = deadBody.getItemContainer();
        deadBody.getContents().extract(cr);
        cr.HPCur = cr.HPMax;
        cr.Turn = 0;
        cr.Owner = field;
        cr.setPos(rpt.X, rpt.Y);

        if (cr.getEntry().Flags.contains(CreatureFlags.esUndead)) {
            cr.setState(CreatureState.csUndead);
        } else {
            cr.setState(CreatureState.csAlive);
        }

        cr.enterField(field);
        iList.remove(deadBody);
        return cr;
    }

    public final void postDeath(NWCreature creature)
    {
        if (creature == null) {
            return;
        }

        try {
            RaceID race = creature.getEntry().Race;

            if (creature.CLSID != GlobalVars.cid_Hela) {
                if (race == RaceID.crDefault || race == RaceID.crHuman) {
                    if (creature.LastAttacker != null && creature.LastAttacker.isPlayer()) {
                        Item item = (Item) this.fPlayer.getItems().findByCLSID(GlobalVars.iid_SoulTrapping_Ring);
                        if (item != null && item.getInUse()) {
                            item.Bonus = creature.CLSID;
                            this.showText(this.fPlayer, Locale.getStr(RS.rs_NewSoul));
                        }
                    }
                }
            } else {
                NWCreature dummy = (NWCreature) this.getField(GlobalVars.Layer_Asgard, 1, 0).getCreatures().findByCLSID(GlobalVars.cid_Balder);
                String msg = Locale.getStr(RS.rs_HelaIsDead);
                if (dummy == null) {
                    msg = msg + " " + Locale.getStr(RS.rs_BalderIsFree);
                    this.addCreatureEx(GlobalVars.Layer_Asgard, 1, 0, -1, -1, GlobalVars.cid_Balder);
                } else {
                    msg += ".";
                }
                this.showText(this.fPlayer, msg, new LogFeatures(LogFeatures.lfDialog));
            }

            if (GlobalVars.nwrWin.getExtremeMode() && !creature.isPlayer()) {
                if (race == RaceID.crHuman) {
                    NWField fld = null;
                    int hero = -1;
                    int ge = AlignmentEx.getGE(creature.Alignment);
                    switch (ge) {
                        case AlignmentEx.am_Mask_Good:
                            fld = this.getField(GlobalVars.Layer_Asgard, 0, 0);
                            hero = GlobalVars.cid_Norseman;
                            break;
                        case AlignmentEx.am_Mask_Evil:
                            fld = this.getField(GlobalVars.Layer_Niflheim, 1, 1);
                            hero = this.findDataEntry("Shadow").GUID;
                            break;
                    }

                    if (fld != null && hero >= 0) {
                        fld.addCreature(-1, -1, hero);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.postDeath(): " + ex.getMessage());
        }
    }

    public final GameEvent peekEvent(Object receiver, EventID eventID, boolean remove)
    {
        GameEvent result = null;
        for (int idx = 0; idx < this.fEventsQueue.getCount(); idx++) {
            GameEvent event = (GameEvent) this.fEventsQueue.get(idx);
            if (event.Receiver.equals(receiver) && (eventID == EventID.event_Nothing || (eventID != EventID.event_Nothing && event.CLSID == eventID.getValue()))) {
                result = event;
                if (remove) {
                    this.fEventsQueue.extract(event);
                }
                break;
            }
        }
        return result;
    }

    public final void sendEvent(EventID eventID, int priority, Object sender, Object receiver)
    {
        GameEvent newEvent = new GameEvent(this, null);
        newEvent.setCLSID((int) (eventID.getValue()));
        newEvent.Priority = priority;
        newEvent.Receiver = receiver;
        newEvent.Sender = sender;
        int idx = 0;
        while (idx < this.fEventsQueue.getCount() && ((GameEvent) this.fEventsQueue.get(idx)).Priority > priority) {
            idx++;
        }
        this.fEventsQueue.insert(idx, newEvent);
    }

    public final void setVolatileState(int aID, VolatileState aState)
    {
        DataEntry entry = this.getDataEntry(aID);
        if (entry instanceof VolatileEntry) {
            ((VolatileEntry) entry).RuntimeState = aState;
        }
    }

    public final void showPlaceInfo(int aX, int aY, boolean Total)
    {
        try {
            NWCreature creature = null;
            if (this.fPlayer.isSeen(aX, aY, false)) {
                String s = "";

                NWField fld = (NWField) this.fPlayer.getCurrentMap();
                NWTile tile = fld.getTile(aX, aY);

                boolean unseen = this.fPlayer.getEffects().findEffectByID(EffectID.eid_Blindness) != null;
                boolean pnear = AuxUtils.distance(this.fPlayer.getPosX(), this.fPlayer.getPosY(), aX, aY) == 0;

                if (fld.isBarrier(aX, aY)) {
                    GlobalVars.nwrWin.showText(this.fPlayer, Locale.getStr(RS.rs_YouCannotSeeThere));
                } else {
                    if (tile.FogID != PlaceID.pid_Undefined) {
                        GlobalVars.nwrWin.showText(this.fPlayer, Locale.getStr(RS.rs_ItIsTooFoggy));
                        unseen = true;
                    }

                    if (Total && (!unseen || (unseen & pnear))) {
                        s = fld.getPlaceName(aX, aY);
                        creature = (NWCreature) fld.findCreature(aX, aY);
                        if (creature != null) {
                            if (creature.isStoning()) {
                                s = s + "; " + Locale.getStr(RS.rs_Statue) + " " + creature.getDeclinableName(Number.nSingle, Case.cGenitive);
                            } else {
                                s = s + "; @" + creature.getName() + "@";
                            }
                        }
                    }

                    ExtList<LocatedEntity> its = fld.getItems().searchListByPos(aX, aY);
                    if (its != null && (!unseen || (unseen & pnear))) {
                        if (s.compareTo("") != 0 && its.getCount() > 0) {
                            s += "; ";
                        }

                        int num = its.getCount();
                        for (int i = 0; i < num; i++) {
                            if (i > 0) {
                                s += ", ";
                            }

                            if (unseen) {
                                s += Locale.getStr(((Item) its.get(i)).getKind().NameRS);
                            } else {
                                s += ((Item) its.get(i)).getName();
                            }
                        }
                        its.dispose();
                    }

                    if (GlobalVars.Debug_DevMode) {
                        s = s + " (" + String.valueOf(aX) + " / " + String.valueOf(aY) + ")";
                    }

                    if (s.compareTo("") != 0) {
                        int pat;
                        if (unseen) {
                            pat = RS.rs_YouFeel;
                        } else {
                            pat = RS.rs_YouSee;
                        }
                        GlobalVars.nwrWin.showText(this.fPlayer, Locale.format(pat, new Object[]{s}));
                    }

                    if (creature != null && this.fPlayer.getSkill(SkillID.Sk_Diagnosis) > 0) {
                        this.diagnoseCreature(creature);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWGameSpace.showPlaceInfo(): " + ex.getMessage());
        }
    }

    public final void prepareWares()
    {
        for (int b = BuildingID.bid_First; b <= BuildingID.bid_Last; b++) {
            BuildingID bid = BuildingID.forValue(b);
            
            ProbabilityTable<Integer> temp = new ProbabilityTable();

            int num2 = GlobalVars.dbItems.getCount();
            for (int i = 0; i < num2; i++) {
                int id = GlobalVars.dbItems.get(i);
                ItemEntry eItem = (ItemEntry) this.getDataEntry(id);

                ItemKind kind = eItem.ItmKind;
                if ((bid.WaresKinds.contains(kind))) {
                    int freq = (int) eItem.Frequency;
                    if (freq > 0) {
                        temp.add(id, freq);
                    }
                }
            }

            bid.Wares = temp;
        }
    }

    private static String getDeclinable(Object obj, Number n_num, Case n_case)
    {
        String result;

        if (n_num == Number.nSingle && n_case == Case.cNominative) {
            if (obj instanceof Item) {
                result = ((Item) obj).getEntry().getName();
            } else {
                if (obj instanceof NWCreature) {
                    result = ((NWCreature) obj).getEntry().getName();
                } else {
                    if (obj instanceof DeclinableEntry) {
                        result = ((DeclinableEntry) obj).getName();
                    } else {
                        result = "";
                    }
                }
            }
        } else {
            if (obj instanceof Item) {
                result = ((Item) obj).getDeclinableName(n_num, n_case, false);
            } else {
                if (obj instanceof NWCreature) {
                    result = ((NWCreature) obj).getDeclinableName(n_num, n_case);
                } else {
                    if (obj instanceof DeclinableEntry) {
                        result = ((DeclinableEntry) obj).getNounDeclension(n_num, n_case);
                    } else {
                        result = "";
                    }
                }
            }
        }

        return result;
    }

    private static String doNoun(String aToken, Object obj, boolean hasParams)
    {
        Number n_num = Number.nSingle;
        Case n_case = Case.cNominative;

        if (GlobalVars.nwrWin.getLangExt().compareTo("ru") == 0 && hasParams) {
            String tok = TextUtils.getToken(aToken, "_", 2);
            char c = tok.charAt(0);
            if (c != 'p') {
                if (c == 's') {
                    n_num = Number.nSingle;
                }
            } else {
                n_num = Number.nPlural;
            }

            tok = TextUtils.getToken(aToken, "_", 3);
            switch (tok.charAt(0)) {
                case 'a': {
                    n_case = Case.cAccusative;
                    break;
                }
                case 'd': {
                    n_case = Case.cDative;
                    break;
                }
                case 'g': {
                    n_case = Case.cGenitive;
                    break;
                }
                case 'i': {
                    n_case = Case.cInstrumental;
                    break;
                }
                case 'n': {
                    n_case = Case.cNominative;
                    break;
                }
                case 'p': {
                    n_case = Case.cPrepositional;
                    break;
                }
            }
        }

        return NWGameSpace.getDeclinable(obj, n_num, n_case);
    }

    private static String prepareToken(String token, int tokIndex, Object... args)
    {
        token = token.substring(1, token.length() - 1);
        int cnt = (int) TextUtils.getTokensCount(token, "_");
        String tok = TextUtils.getToken(token, "_", 1);

        String result = "";

        if (tok.compareTo("noun") == 0 && cnt == 3) {
            if (args[tokIndex] != null) {
                Object obj = args[tokIndex];
                result = NWGameSpace.doNoun(token, obj, true);
            }
        } else {
            if (tok.compareTo("%s") == 0) {
                if (args[tokIndex] != null) {
                    Object obj = args[tokIndex];
                    result = NWGameSpace.doNoun(token, obj, false);
                } else {
                    if (args[tokIndex] instanceof String) {
                        result = ((String) ((args[tokIndex] instanceof String) ? args[tokIndex] : null));
                    }
                }
            }
        }

        return result;
    }

    public final String parseMessage(String str, Object... args)
    {
        try {
            if (args != null) {
                args = (Object[]) args.clone();
            }

            String result = str;
            int num = 0;
            int cnt = (int) TextUtils.getTokensCount(str, " .,");
            for (int i = 1; i <= cnt; i++) {
                String tok = TextUtils.getToken(str, " .,", i);
                if (TextUtils.isNullOrEmpty(tok)) {
                    continue;
                }

                if (tok.charAt(0) == '{' && tok.charAt(tok.length() - 1) == '}') {
                    num++;
                    String res = NWGameSpace.prepareToken(tok, num - 1, args);

                    int p = result.indexOf(tok);
                    result = result.substring(0, p) + result.substring(p + tok.length());

                    StringBuilder sb = new StringBuilder(result);
                    sb.insert(p, res);
                    result = sb.toString();
                }
            }

            return result;
        } catch (Exception ex) {
            Logger.write("NWGameSpace.parseMessage(): " + ex.getMessage());
            return "";
        }
    }
    
    public final String generateName(NWCreature creature, byte method)
    {
        return this.fNameLib.generateName(GlobalVars.nwrWin.getLangExt(), StaticData.GenderBySex[creature.Sex.Value], method);
    }

    // Implementing Fisher–Yates shuffle
    private static void shuffleArray(int[] array)
    {
        int index, temp;
        Random rnd = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = rnd.nextInt(i + 1);

            if (index != i) {
                temp = array[index];
                array[index] = array[i];
                array[i] = temp;
            }
        }
    }

    private void preparePrefixesArray(int low, int high, IntList entitySet, String name)
    {
        int prefixesCount = high - low + 1;
        int[] result = new int[prefixesCount];
        for (int i = low; i <= high; i++) {
            result[i - low] = i;
        }

        shuffleArray(result);

        int entitiesCount = entitySet.getCount();
        if (entitiesCount > prefixesCount) {
            throw new RuntimeException("no prefixes");
        }

        for (int i = 0; i < entitiesCount; i++) {
            ItemEntry itemEntry = (ItemEntry) this.getDataEntry(entitySet.get(i));
            itemEntry.Prefix = result[i];
        }
    }

    public final void preparePrefixes()
    {
        try {
            preparePrefixesArray(RS.rs_WandPrefixes_First, RS.rs_WandPrefixes_Last, GlobalVars.dbWands, "Wands");
            preparePrefixesArray(RS.rs_RingPrefixes_First, RS.rs_RingPrefixes_Last, GlobalVars.dbRings, "Rings");
            preparePrefixesArray(RS.rs_PotionPrefixes_First, RS.rs_PotionPrefixes_Last, GlobalVars.dbPotions, "Potions");
            //int[] raysPrefixes = preparePrefixesArray(RS.rs_RayPrefixes_First, RS.rs_RayPrefixes_Last);
            preparePrefixesArray(RS.rs_ScrollPrefixes_First, RS.rs_ScrollPrefixes_Last, GlobalVars.dbScrolls, "Scrolls");
        } catch (Exception ex) {
            Logger.write("NWGameSpace.preparePrefixes(): " + ex.getMessage());
        }
    }

    private boolean checkGrynrHalls(NWField field)
    {
        int sacred_eye_pits = 0;

        for (int y = 0; y < StaticData.FieldHeight; y++) {
            for (int x = 0; x < StaticData.FieldWidth; x++) {
                if (field.getTile(x, y).getForeBase() == PlaceID.pid_SmallPit) {
                    sacred_eye_pits++;
                }
            }
        }

        return sacred_eye_pits == 0;
    }

    public final void dropItem(NWField field, NWCreature creature, Item item, int tx, int ty)
    {
        this.doEvent(EventID.event_ItemDrop, creature, null, item);

        if (field.LandID == GlobalVars.Land_GrynrHalls) {
            NWTile tile = field.getTile(tx, ty);
            if (tile.getForeBase() == PlaceID.pid_SmallPit && item.CLSID == GlobalVars.iid_Amulet_SertrudEye) {
                tile.setFore(PlaceID.pid_Undefined);
                item.dispose();
                GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_EyeFitsIntoPit) + Locale.getStr(RS.rs_PitEnclosesEye));
                if (this.checkGrynrHalls(field)) {
                    Item i = this.addItemEx(field.getLayer().EntryID, field.getCoords().X, field.getCoords().Y, tx, ty, GlobalVars.iid_Mjollnir);
                    GlobalVars.nwrWin.showText(creature, Locale.getStr(RS.rs_ThunderousNoise) + Locale.getStr(RS.rs_BrightLight) + Locale.format(RS.rs_YouSeeThatItemAppeared, new Object[]{i.getName()}));
                }
                return;
            }
        }

        field.dropItem(creature, item, tx, ty);
    }

    public final void pickupItem(NWField field, NWCreature creature, Item item)
    {
        this.doEvent(EventID.event_ItemPickup, creature, null, item);

        field.pickupItem(creature, item);

        if (creature.isPlayer()) {
            int num = this.getQuestsCount();
            for (int i = 0; i < num; i++) {
                Quest quest = this.getQuest(i);
                quest.pickupItem(item);
            }
        } else {
            this.showText(creature, Locale.format(RS.rs_XPicksSomethingUp, new Object[]{creature.getDeclinableName(Number.nSingle, Case.cInstrumental)}));
        }
    }

    public final QuestItemState checkQuestItem(int iid, int targetID)
    {
        NWCreature target = this.findCreature(targetID);
        if (target != null && target.getItems().findByCLSID(iid) != null) {
            return QuestItemState.qisComplete;
        }

        target = this.getPlayer();
        if (target != null && target.getItems().findByCLSID(iid) != null) {
            return QuestItemState.qisFounded;
        }

        return QuestItemState.qisNone;
    }


    private void addQuest(Quest quest)
    {
        this.fQuests.add(quest);
    }

    public final void genMainQuests()
    {
        this.fQuests.clear();

        this.addQuest(new MainQuest(this, GlobalVars.iid_SoulTrapping_Ring, GlobalVars.cid_Hela));
        this.addQuest(new MainQuest(this, GlobalVars.iid_Gjall, GlobalVars.cid_Heimdall));
        this.addQuest(new MainQuest(this, GlobalVars.iid_Mjollnir, GlobalVars.cid_Thor));
        this.addQuest(new MainQuest(this, GlobalVars.iid_DwarvenArm, GlobalVars.cid_Tyr));
        this.addQuest(new MainQuest(this, GlobalVars.iid_Mimming, GlobalVars.cid_Freyr));
        this.addQuest(new MainQuest(this, GlobalVars.iid_Gungnir, GlobalVars.cid_Odin));
    }

    // <editor-fold defaultstate="collapsed" desc="IHost implementation">

    @Override
    public final void doEvent(EventID eventID, Object sender, Object receiver, Object extData)
    {
        GlobalVars.nwrWin.doEvent(eventID, sender, receiver, extData);
    }

    @Override
    public final DataEntry getDataEntry(int uid)
    {
        return this.fDB.getEntry(uid);
    }

    public final DataEntry findDataEntry(String sign)
    {
        return this.fDB.findEntryBySign(sign);
    }

    @Override
    public final void repaintView(int delayInterval)
    {
        GlobalVars.nwrWin.repaint(delayInterval);
    }

    @Override
    public final void showText(String text)
    {
        GlobalVars.nwrWin.showText(text);
    }

    @Override
    public final void showText(Object sender, String text)
    {
        GlobalVars.nwrWin.showText(sender, text);
    }

    @Override
    public final void showText(Object sender, String text, LogFeatures features)
    {
        GlobalVars.nwrWin.showText(sender, text, features);
    }

    @Override
    public final void showTextAux(String text)
    {
        GlobalVars.nwrWin.showTextAux(text);
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Serializables factory">

    private static void registerSerializables()
    {
        SerializablesManager.registerSerializable(StaticData.SID_BUILDING, NWGameSpace::createBuilding);
        SerializablesManager.registerSerializable(StaticData.SID_ITEM, NWGameSpace::createItem);
        SerializablesManager.registerSerializable(StaticData.SID_CREATURE, NWGameSpace::createCreature);
        SerializablesManager.registerSerializable(StaticData.SID_KNOWLEDGE, NWGameSpace::createKnowledge);
        SerializablesManager.registerSerializable(StaticData.SID_EFFECT, NWGameSpace::createEffect);
        SerializablesManager.registerSerializable(StaticData.SID_RECALL_POS, NWGameSpace::createRecallPos);
        SerializablesManager.registerSerializable(StaticData.SID_DEBT, NWGameSpace::createDebt);
        SerializablesManager.registerSerializable(StaticData.SID_SOURCE_FORM, NWGameSpace::createSourceForm);
        SerializablesManager.registerSerializable(StaticData.SID_POINTGUARD_GOAL, NWGameSpace::createPointGuardGoal);
        SerializablesManager.registerSerializable(StaticData.SID_AREAGUARD_GOAL, NWGameSpace::createAreaGuardGoal);
        SerializablesManager.registerSerializable(StaticData.SID_VILLAGE, NWGameSpace::createVillage);
        SerializablesManager.registerSerializable(StaticData.SID_GATE, NWGameSpace::createGate);
    }

    private static ISerializable createBuilding(Object owner)
    {
        NWGameSpace space = (NWGameSpace) getInstance();
        return new Building(space, owner);
    }

    private static ISerializable createItem(Object owner)
    {
        NWGameSpace space = (NWGameSpace) getInstance();
        return new Item(space, owner);
    }

    private static ISerializable createCreature(Object owner)
    {
        NWGameSpace space = (NWGameSpace) getInstance();
        return new NWCreature(space, owner);
    }

    private static ISerializable createKnowledge(Object owner)
    {
        return new Knowledge(owner);
    }

    private static ISerializable createEffect(Object owner)
    {
        NWGameSpace space = (NWGameSpace) getInstance();
        return new Effect(space, (GameEntity) owner, EffectID.eid_None, null, EffectAction.ea_Instant, 0, 0);
    }

    private static ISerializable createRecallPos(Object owner)
    {
        return new RecallPos(owner);
    }

    private static ISerializable createDebt(Object owner)
    {
        return new Debt(owner);
    }

    private static ISerializable createSourceForm(Object owner)
    {
        return new SourceForm(owner);
    }

    private static ISerializable createPointGuardGoal(Object owner)
    {
        return new PointGuardGoal((BrainEntity) owner);
    }

    private static ISerializable createAreaGuardGoal(Object owner)
    {
        return new AreaGuardGoal((BrainEntity) owner);
    }

    private static ISerializable createVillage(Object owner)
    {
        NWGameSpace space = (NWGameSpace) getInstance();
        return new Village(space, owner);
    }

    private static ISerializable createGate(Object owner)
    {
        NWGameSpace space = (NWGameSpace) getInstance();
        return new Gate(space, owner);
    }

    // </editor-fold>
}
