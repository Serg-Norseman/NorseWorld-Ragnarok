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
package nwr.universe;

import java.io.IOException;
import java.util.ArrayList;
import jzrlib.core.CreatureEntity;
import jzrlib.core.EntityList;
import jzrlib.core.FileVersion;
import jzrlib.core.GameEntity;
import jzrlib.core.LocatedEntity;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.core.brain.EmitterList;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.map.BaseTile;
import jzrlib.map.CustomMap;
import jzrlib.map.Movements;
import jzrlib.map.PathSearch;
import jzrlib.map.TileStates;
import jzrlib.map.TileType;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.StreamUtils;
import jzrlib.utils.TypeUtils;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.PlaceFlags;
import nwr.core.types.PlaceID;
import nwr.creatures.CreaturesList;
import nwr.creatures.NWCreature;
import nwr.database.CreatureEntry;
import nwr.database.FieldEntry;
import nwr.database.FieldSource;
import nwr.database.LandEntry;
import nwr.database.LandFlags;
import nwr.database.LayerEntry;
import nwr.effects.EffectsList;
import nwr.engine.SoundEngine;
import nwr.game.NWGameSpace;
import nwr.item.Item;
import nwr.item.ItemsList;
import nwr.main.GlobalVars;
import nwr.player.Player;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class NWField extends CustomMap
{
    // Wasteland States
    private static final int WS_ERUPTION = 0;
    private static final int WS_FINISH = 1;
    private static final int WS_VULCAN_NOT_EXISTS = 2;
    
    // other
    public static final int[] Muspel_bgTiles;
    public static final int[] Muspel_fgTiles;

    private final Point fCoords;
    private final CreaturesList fCreatures;
    private final EffectsList fEffects;
    private final EmitterList fEmitters;
    private final FieldEntry fEntry;
    private final ItemsList fItems;
    private final LandEntry fLandEntry;
    
    public final int LandID;
    public final ArrayList<Integer> ValidCreatures;
    public boolean Visited;

    private final NWGameSpace fSpace;
    private final NWLayer fLayer;
    public final int EntryID;

    public NWField(NWGameSpace space, NWLayer layer, Point coords)
    {
        super(StaticData.FieldWidth, StaticData.FieldHeight);

        this.fSpace = space;
        this.fLayer = layer;
        this.fCoords = coords;

        this.fCreatures = new CreaturesList(this, true);
        this.fItems = new ItemsList(this, true);
        this.fEffects = new EffectsList(this, true);
        this.fEmitters = new EmitterList();
        this.ValidCreatures = new ArrayList<>();

        if (this.getLayer() != null) {
            LayerEntry layerEntry = (LayerEntry) GlobalVars.nwrBase.getEntry(this.getLayer().EntryID);
            this.fEntry = layerEntry.getFieldEntry(this.fCoords.X, this.fCoords.Y);

            this.EntryID = this.fEntry.GUID;

            this.fLandEntry = (LandEntry) GlobalVars.nwrBase.findEntryBySign(this.fEntry.LandSign);
            this.LandID = this.fLandEntry.GUID;

            this.prepareCreatures();
        } else {
            this.fEntry = null;
            this.EntryID = -1;
            this.fLandEntry = null;
            this.LandID = -1;
        }        
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fEmitters.dispose();
            this.fEffects.dispose();
            this.fItems.dispose();
            this.fCreatures.dispose();
            //this.ValidCreatures = null;
        }
        super.dispose(disposing);
    }

    @Override
    protected BaseTile createTile()
    {
        return new NWTile();
    }

    @Override
    public NWTile getTile(int x, int y)
    {
        NWTile result = (NWTile) super.getTile(x, y);

        if (result == null) {
            int fx = this.fCoords.X;
            int fy = this.fCoords.Y;
            int px = x;
            int py = y;

            if (x < 0 || x >= StaticData.FieldWidth) {
                px = Math.abs(Math.abs(x) - StaticData.FieldWidth);
            }
            if (y < 0 || y >= StaticData.FieldHeight) {
                py = Math.abs(Math.abs(y) - StaticData.FieldHeight);
            }
            if (x < 0) {
                fx--;
            }
            if (x >= StaticData.FieldWidth) {
                fx++;
            }
            if (y < 0) {
                fy--;
            }
            if (y >= StaticData.FieldHeight) {
                fy++;
            }

            NWLayer layer = this.getLayer();

            if (layer != null) {
                NWField fld = layer.getField(fx, fy);
                if (fld != null) {
                    result = fld.getTile(px, py);
                }
            }
        }

        return result;
    }

    public final NWGameSpace getSpace()
    {
        return (NWGameSpace) this.fSpace;
    }

    public final NWLayer getLayer()
    {
        return this.fLayer;
    }

    public final Point getCoords()
    {
        return this.fCoords;
    }

    public final Point getLayerTileCoords(int tx, int ty)
    {
        return new Point(this.fCoords.X * StaticData.FieldWidth + tx, this.fCoords.Y * StaticData.FieldHeight + ty);
    }

    public final Point getLayerTileCoords(Point pt)
    {
        return new Point(this.fCoords.X * StaticData.FieldWidth + pt.X, this.fCoords.Y * StaticData.FieldHeight + pt.Y);
    }

    public final CreaturesList getCreatures()
    {
        return this.fCreatures;
    }

    public final EffectsList getEffects()
    {
        return this.fEffects;
    }

    public final EmitterList getEmitters()
    {
        return this.fEmitters;
    }

    public final ItemsList getItems()
    {
        return this.fItems;
    }

    public final LandEntry getLandEntry()
    {
        return this.fLandEntry;
    }

    private void prepareCreatures()
    {
        try {
            this.ValidCreatures.clear();

            int num2 = GlobalVars.dbCreatures.getCount();
            for (int i = 0; i < num2; i++) {
                CreatureEntry crEntry = (CreatureEntry) GlobalVars.nwrBase.getEntry(GlobalVars.dbCreatures.get(i));
                if (crEntry.isInhabitant(this.fLandEntry.Sign)) {
                    this.ValidCreatures.add(GlobalVars.dbCreatures.get(i));
                }
            }
        } catch (Exception ex) {
            Logger.write("NWField.prepareCreatures(): " + ex.getMessage());
            throw ex;
        }
    }

    public final String getLandName()
    {
        return this.fLandEntry.getName();
    }

    @Override
    public void normalize()
    {
        UniverseBuilder.normalizeFieldMasks(this, false);
    }

    public final void normalizeFog()
    {
        UniverseBuilder.normalizeFieldMasks(this, true);
    }

    private void doTurn_prepareFogs()
    {
        for (int y = 0; y < StaticData.FieldHeight; y++) {
            for (int x = 0; x < StaticData.FieldWidth; x++) {
                NWTile tile = this.getTile(x, y);
                if (tile.FogID == PlaceID.pid_Fog) {
                    if (tile.FogAge > 0) {
                        tile.FogAge = (byte) (tile.FogAge - 1);
                    }
                    if (tile.FogAge == 0) {
                        tile.FogID = (short) PlaceID.pid_Undefined;
                    }
                }
            }
        }

        this.normalizeFog();
    }

    private void doTurn_prepareScentTrails()
    {
        for (int y = 0; y < StaticData.FieldHeight; y++) {
            for (int x = 0; x < StaticData.FieldWidth; x++) {
                NWTile tile = this.getTile(x, y);
                if (tile.ScentAge > 0) {
                    tile.ScentAge = (byte) ((int) tile.ScentAge - 1);
                }
                if (tile.ScentAge == 0) {
                    tile.ScentTrail = null;
                }
            }
        }
    }

    public final void spreadTiles(int tileID, float density)
    {
        try {
            int tsz = 0;
            ArrayList<Point> bounds = new ArrayList<>();
            for (int y = 0; y < super.getHeight(); y++) {
                for (int x = 0; x < super.getWidth(); x++) {
                    NWTile tile = this.getTile(x, y);
                    if (tile.getBackBase() != tileID) {
                        int cnt = this.getBackTilesCount(x, y, (short) tileID);
                        if (cnt > 0) {
                            bounds.add(new Point(x, y));
                        }
                    } else {
                        tsz++;
                    }
                }
            }

            int xcnt = AuxUtils.getBoundedRnd(1, Math.max(1, Math.round(bounds.size() * density)));
            for (int i = 1; i <= xcnt; i++) {
                int idx = AuxUtils.getRandom(bounds.size());
                Point pt = bounds.get(idx);
                bounds.remove(idx);

                this.changeTile(pt.X, pt.Y, tileID, false);
            }
            tsz += xcnt;

            // ALERT: change genBackTiles for compatibility with changeTile()
            float min = this.getAreaRect().getSquare() * 0.1f;
            if ((float) tsz < min) {
                UniverseBuilder.genBackTiles(this, (int) (min - tsz), tileID);
            }

            this.normalize();
        } catch (Exception ex) {
            Logger.write("NWField.spreadTiles(): " + ex.getMessage());
        }
    }

    // FIXME: change total other functions for compatibiliy with this
    private void changeTile(int x, int y, int tileID, boolean fg)
    {
        BaseTile tile = this.getTile(x, y);
        if (tile != null) {
            if (!fg) {
                tile.setBack(tileID);
            } else {
                tile.setFore(tileID);
            }
            
            switch (tileID) {
                case PlaceID.pid_Lava: {
                    if (tile.getForeBase() == PlaceID.pid_Tree) {
                        tile.setFore(PlaceID.pid_DeadTree);
                        GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_TreeBursts));
                    }

                    // ALERT: fixme, where resistances?!!
                    NWCreature cr = (NWCreature) this.findCreature(x, y);
                    if (cr != null) {
                        if (cr.isPlayer()) {
                            GlobalVars.nwrWin.showText(cr, Locale.getStr(RS.rs_LavaEncasesYou));
                            cr.death(Locale.getStr(RS.rs_EncasedInVolcanicRock), null);
                        } else {
                            cr.death(Locale.getStr(RS.rs_XIsConsumedByLava), null);
                        }
                    }
                    break;
                }
            }
        }
    }
    
    private int doTurn_eruptionWastelandVulcan()
    {
        int state = NWField.WS_ERUPTION;

        try {
            boolean vulcan_ex = false;

            ArrayList<Point> lava_bounds = new ArrayList<>();

            for (int y = 0; y < super.getHeight(); y++) {
                for (int x = 0; x < super.getWidth(); x++) {
                    NWTile tile = this.getTile(x, y);
                    int cnt = this.getBackTilesCount(x, y, (short) PlaceID.pid_Lava);

                    if (tile.getForeBase() == PlaceID.pid_Vulcan) {
                        vulcan_ex = true;
                    }

                    if (tile.getBackBase() == PlaceID.pid_Grass && cnt > 0) {
                        lava_bounds.add(new Point(x, y));
                    }
                }
            }

            if (lava_bounds.isEmpty()) {
                state = NWField.WS_FINISH;
            }
            if (!vulcan_ex) {
                state = NWField.WS_VULCAN_NOT_EXISTS;
            }

            if (state != NWField.WS_ERUPTION) {
                return state;
            }

            if (lava_bounds.size() > 5) {
                GlobalVars.nwrWin.showText(GlobalVars.nwrWin, Locale.getStr(RS.rs_GroundBeginsToShake));
            }

            int xcnt = AuxUtils.getBoundedRnd(1, Math.max(1, Math.round(lava_bounds.size() * 0.25f)));
            for (int i = 1; i <= xcnt; i++) {
                int idx = AuxUtils.getRandom(lava_bounds.size());
                Point pt = lava_bounds.get(idx);
                lava_bounds.remove(idx);

                this.changeTile(pt.X, pt.Y, PlaceID.pid_Lava, false);
            }

            this.normalize();
        } catch (Exception ex) {
            Logger.write("NWField.doTurn_eruptionWastelandVulcan(): " + ex.getMessage());
        }

        return state;
    }

    private void processVariants()
    {
        for (int y = 0; y < StaticData.FieldHeight; y++) {
            for (int x = 0; x < StaticData.FieldWidth; x++) {
                NWTile tile = this.getTile(x, y);
                if (tile.getForeBase() == PlaceID.pid_Mountain) {
                    tile.setFore(NWField.getVarTile(PlaceID.pid_Mountain));
                }
            }
        }
    }

    public final void addSeveralTraps(int[] kinds, int f)
    {
        int cnt;
        if (f > 0) {
            cnt = f;
        } else {
            cnt = AuxUtils.getBoundedRnd(3, -4 * f);
        }

        int fx = this.fCoords.X;
        int fy = this.fCoords.Y;
        
        for (int i = 1; i <= cnt; i++) {
            while (true) {
                int y = AuxUtils.getBoundedRnd(0, super.getHeight() - 1);
                int x = AuxUtils.getBoundedRnd(0, super.getWidth() - 1);
                NWTile tile = this.getTile(x, y);

                boolean ready = (this.findBuilding(x, y) == null && tile.getForeBase() == PlaceID.pid_Undefined);
                if (ready) {
                    int trapKind;
                    do {
                        trapKind = AuxUtils.getRandomArrayInt(kinds);
                        boolean crt = (trapKind == PlaceID.pid_CrushRoofTrap);
                        
                        int lx = (fx * StaticData.FieldWidth) + x;
                        int ly = (fy * StaticData.FieldHeight) + y;
                        
                        boolean hasDunRoom = crt && (this.findDungeonRoom(x, y) != null || this.getLayer().findDungeonRoom(lx, ly) != null);
                        ready = (!crt || (crt & hasDunRoom));
                    } while (!ready);

                    this.addTrap(x, y, trapKind, false);
                    break;
                }
            }
        }
    }

    public final void addTrap(int aX, int aY, int trapTileID, boolean aDiscovered)
    {
        NWTile tile = this.getTile(aX, aY);
        tile.setFore(trapTileID);
        tile.Trap_Discovered = aDiscovered;
    }

    @Override
    public boolean isBlockLOS(int x, int y)
    {
        boolean result;

        NWTile tile = this.getTile(x, y);
        if (tile == null) {
            result = true;
        } else {
            int bg = tile.getBackBase();
            int fg = tile.getForeBase();

            result = ((StaticData.dbPlaces[bg].Signs.contains(PlaceFlags.psBlockLOS)) 
                    || (fg != PlaceID.pid_Undefined && (StaticData.dbPlaces[fg].Signs.contains(PlaceFlags.psBlockLOS))));
        }

        return result;
    }

    @Override
    public boolean isBarrier(int x, int y)
    {
        boolean result = true;
        NWTile p = this.getTile(x, y);
        if (p != null) {
            int bg = (p.getBackBase());
            int fg = (p.getForeBase());

            result = ((StaticData.dbPlaces[bg].Signs.contains(PlaceFlags.psBarrier)) 
                    || (fg != PlaceID.pid_Undefined && (StaticData.dbPlaces[fg].Signs.contains(PlaceFlags.psBarrier))));
        }
        return result;
    }

    public final void close(boolean needFG)
    {
        for (int y = 0; y < StaticData.FieldHeight; y++) {
            for (int x = 0; x < StaticData.FieldWidth; x++) {
                NWTile tile = this.getTile(x, y);
                if (needFG && tile.getForeBase() != PlaceID.pid_Undefined) {
                    // dummy
                } else {
                    tile.excludeState(TileStates.TS_VISITED);
                }
            }
        }
    }

    @Override
    public CreatureEntity findCreature(int aX, int aY)
    {
        NWTile tile = this.getTile(aX, aY);

        CreatureEntity result;

        if (tile != null) {
            result = tile.CreaturePtr;
        } else {
            result = null;
        }

        return result;
    }

    public final void setAmbient()
    {
        SoundEngine.Reverb reverb = SoundEngine.Reverb.Forest;
        
        if (this.LandID == GlobalVars.Land_Vanaheim || this.LandID == GlobalVars.Land_Wasteland
                || this.LandID == GlobalVars.Land_Alfheim || this.LandID == GlobalVars.Land_Crossroads
                || this.LandID == GlobalVars.Land_Niflheim || this.LandID == GlobalVars.Land_Ocean) {
            reverb = SoundEngine.Reverb.Plain;
        }
        if (this.LandID == GlobalVars.Land_Village || this.LandID == GlobalVars.Land_Forest) {
            reverb = SoundEngine.Reverb.Forest;
        }
        if (this.LandID == GlobalVars.Land_MimerRealm || this.LandID == GlobalVars.Land_Muspelheim 
                || this.LandID == GlobalVars.Land_Jotenheim) {
            reverb = SoundEngine.Reverb.Mountains;
        }
        if (this.LandID == GlobalVars.Land_MimerWell) {
            reverb = SoundEngine.Reverb.Underwater;
        }
        if (this.LandID == GlobalVars.Land_GreatCaves || this.LandID == GlobalVars.Land_Nidavellir) {
            reverb = SoundEngine.Reverb.Cave;
        }
        if (this.LandID == GlobalVars.Land_Caves || this.LandID == GlobalVars.Land_DeepCaves
                || this.LandID == GlobalVars.Land_GrynrHalls || this.LandID == GlobalVars.Land_Temple
                || this.LandID == GlobalVars.Land_Armory || this.LandID == GlobalVars.Land_Bazaar
                || this.LandID == GlobalVars.Land_Crypt) {
            reverb = SoundEngine.Reverb.Dungeon;
        }
        if (this.LandID == GlobalVars.Land_GodsFortress) {
            reverb = SoundEngine.Reverb.Room;
        }
        /*if (this.LandID == GlobalVars.Land_Valhalla || this.LandID == GlobalVars.Land_Vigrid) {
            reverb = SoundEngine.Reverb.Arena;
        }*/
        if (this.LandID == GlobalVars.Land_Valhalla) {
            reverb = SoundEngine.Reverb.CHall;
        }
        if (this.LandID == GlobalVars.Land_Vigrid) {
            reverb = SoundEngine.Reverb.Quarry;
        }

        GlobalVars.nwrWin.setSoundsReverb(reverb);
    }

    public final void doTurn()
    {
        try {
            this.fEmitters.updateEmitters(1);
            this.fEffects.execute();

            this.doTurn_prepareFogs();
            this.doTurn_prepareScentTrails();

            if (this.LandID == GlobalVars.Land_Wasteland) {
                this.doTurn_eruptionWastelandVulcan();
            }

            if (this.LandID == GlobalVars.Land_Muspelheim) {
                UniverseBuilder.regen_Muspelheim(this);
            }

            Player player = this.getSpace().getPlayer();
            if (this.LandID == GlobalVars.Land_Village && AuxUtils.chance(10) && this.findBuilding(player.getPosX(), player.getPosY()) == null) {
                int num = super.getFeatures().getCount();
                for (int i = 0; i < num; i++) {
                    GameEntity entry = super.getFeatures().getItem(i);
                    if (entry instanceof Building) {
                        ((Building) entry).refresh();
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWField.doTurn(): " + ex.getMessage());
        }
    }

    public final Building findBuilding(int aX, int aY)
    {
        int num = super.getFeatures().getCount();
        for (int i = 0; i < num; i++) {
            GameEntity feat = super.getFeatures().getItem(i);
            if (feat instanceof Building && ((Building) feat).getArea().contains(aX, aY)) {
                return ((Building) feat);
            }
        }
        return null;
    }

    public final DungeonRoom findDungeonRoom(int aX, int aY)
    {
        int num = super.getFeatures().getCount();
        for (int i = 0; i < num; i++) {
            Object feat = super.getFeatures().getItem(i);
            if (feat instanceof DungeonRoom) {
                DungeonRoom dr = ((DungeonRoom) feat);
                if (dr.inArea(aX, aY)) {
                    return dr;
                }
            }
        }

        return null;
    }

    public final Village findVillage()
    {
        int num = super.getFeatures().getCount();
        for (int i = 0; i < num; i++) {
            Object feat = super.getFeatures().getItem(i);
            if (feat instanceof Village) {
                return ((Village) feat);
            }
        }

        return null;
    }

    public final String getPlaceName(int x, int y)
    {
        String result = "";

        NWTile tile = (NWTile) super.getTile(x, y);
        if (tile != null && !tile.isEmptyStates()) {
            int bg = tile.getBackBase();
            int fg = tile.getForeBase();

            if (bg != PlaceID.pid_Undefined) {
                result = Locale.getStr(StaticData.dbPlaces[bg].NameRS);
                if (bg == PlaceID.pid_Liquid) {
                    result = result + " (" + GlobalVars.nwrBase.getEntry(tile.Lake_LiquidID).getName() + ")";
                }
            }

            if (fg != PlaceID.pid_Undefined) {
                if (this.isTrap(x, y) && tile.Trap_Discovered) {
                    result = result + ", " + Locale.getStr(StaticData.dbPlaces[fg].NameRS);
                } else {
                    Gate gate = this.findGate(x, y);
                    if (gate != null) {
                        result = result + ", " + gate.getName();
                    } else {
                        result = result + ", " + Locale.getStr(StaticData.dbPlaces[fg].NameRS);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Movements getTileMovements(short tileID)
    {
        int pd = TypeUtils.getShortLo(tileID);
        return StaticData.dbPlaces[pd].Moves;
    }

    private void prepare(int defPlace)
    {
        for (int y = 0; y < StaticData.FieldHeight; y++) {
            for (int x = 0; x < StaticData.FieldWidth; x++) {
                BaseTile tile = super.getTile(x, y);
                tile.Background = (short) defPlace;
                tile.Foreground = (short) PlaceID.pid_Undefined;
                tile.BackgroundExt = (short) PlaceID.pid_Undefined;
                tile.ForegroundExt = (short) PlaceID.pid_Undefined;
            }
        }
    }

    public final void initField()
    {
        String entry_sign = this.fEntry.Sign;
        if (GlobalVars.Debug_TestWorldGen) {
            Logger.write("NWField.initField().start >>>>> " + entry_sign);
        }

        try {
            NWLayer layer = this.getLayer();
            if (this.fEntry != null && this.fEntry.Source == FieldSource.fsTemplate) {
                this.loadFromEntry(this.fEntry);
                this.processVariants();
            } else {
                if (this.LandID == GlobalVars.Land_Armory || this.LandID == GlobalVars.Land_GrynrHalls) {
                    UniverseBuilder.build_Dungeon(this, this.getAreaRect());
                } else {
                    if (layer.EntryID != GlobalVars.Layer_Svartalfheim1 && layer.EntryID != GlobalVars.Layer_Svartalfheim2 && layer.EntryID != GlobalVars.Layer_Svartalfheim3) {
                        this.prepare(PlaceID.pid_Grass);
                    }
                }
            }

            int defTS = 0;
            if (this.LandID == GlobalVars.Land_Bifrost || this.LandID == GlobalVars.Land_Crossroads) {
                defTS = TileStates.include(0, TileStates.TS_VISITED);
            }

            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = this.getTile(x, y);
                    tile.BackgroundExt = 0;
                    tile.ForegroundExt = 0;
                    tile.ScentAge = 0;
                    tile.ScentTrail = null;
                    tile.addStates(defTS);
                }
            }

            if (this.fLandEntry.Flags.contains(LandFlags.lsHasForest)) {
                UniverseBuilder.build_Forest(this);
            }
            if (this.fLandEntry.Flags.contains(LandFlags.lsHasMountain)) {
                UniverseBuilder.gen_RarelyMountains(this);
            }
            if (this.fLandEntry.Flags.contains(LandFlags.lsHasItems)) {
                UniverseBuilder.gen_Items(this, -1);
            }
            if (this.fLandEntry.Flags.contains(LandFlags.lsIsCave)) {
                UniverseBuilder.gen_CaveObjects(this);
            }

            if (this.LandID == GlobalVars.Land_Ocean) {
                this.prepare(PlaceID.pid_Water);
            }

            if (this.LandID == GlobalVars.Land_Vanaheim) {
                UniverseBuilder.build_Vanaheim(this);
            }
            if (this.LandID == GlobalVars.Land_Alfheim) {
                UniverseBuilder.build_Alfheim(this);
            }
            if (this.LandID == GlobalVars.Land_Wasteland) {
                UniverseBuilder.build_Wasteland(this);
            }
            if (this.LandID == GlobalVars.Land_Crossroads) {
                UniverseBuilder.build_Crossroads(this);
            }
            if (this.LandID == GlobalVars.Land_Village) {
                UniverseBuilder.build_Village(this);
            }
            if (this.LandID == GlobalVars.Land_MimerRealm) {
                UniverseBuilder.build_MimerRealm(this);
            }
            if (this.LandID == GlobalVars.Land_MimerWell) {
                UniverseBuilder.build_MimerWell(this);
            }
            if (this.LandID == GlobalVars.Land_GrynrHalls) {
                UniverseBuilder.build_GrynrHalls(this);
            }
            if (this.LandID == GlobalVars.Land_Bazaar) {
                UniverseBuilder.build_Bazaar(this);
            }
            if (this.LandID == GlobalVars.Land_Temple) {
                UniverseBuilder.build_VidurTemple(this);
            }
            if (this.LandID == GlobalVars.Land_GodsFortress) {
                UniverseBuilder.build_GodsFortress(this);
            }
            if (this.LandID == GlobalVars.Land_Valhalla) {
                UniverseBuilder.build_Valhalla(this);
            }
            if (this.LandID == GlobalVars.Land_Muspelheim) {
                UniverseBuilder.build_Muspelheim(this);
            }

            if (this.LandID == GlobalVars.Land_Ocean) {
                UniverseBuilder.build_Ocean(this);
            } else {
                UniverseBuilder.gen_Creatures(this, -1);
                UniverseBuilder.gen_Traps(this, -2);
            }
        } catch (Exception ex) {
            Logger.write("NWField.initField(" + this.getDebugSign() + "): " + ex.getMessage());
        }

        if (GlobalVars.Debug_TestWorldGen) {
            Logger.write("NWField.initField().finish >>>>> " + entry_sign);
        }
    }

    public final boolean isDark()
    {
        return this.fLandEntry.Flags.hasIntersect(LandFlags.lsIsDungeon, LandFlags.lsIsCave);
    }

    public final void addGate(int tileID, int posX, int posY, int targetLayer, Point targetField, Point targetPos)
    {
        NWTile tile = this.getTile(posX, posY);
        tile.setFore(tileID);

        Gate gate = new Gate(this.fSpace, this);
        gate.setCLSID(tileID);
        gate.setPos(posX, posY);
        gate.TargetLayer = targetLayer;
        gate.TargetField = targetField.clone();
        gate.TargetPos = targetPos.clone();
        this.getFeatures().add(gate);
    }

    public final Gate findGate(int tx, int ty)
    {
        EntityList features = this.getFeatures();
        int num = features.getCount();
        for (int i = 0; i < num; i++) {
            GameEntity feat = features.getItem(i);
            if (feat instanceof Gate) {
                Gate gate = (Gate) feat;
                if (gate.getLocation().equals(tx, ty)) {
                    return gate;
                }
            }
        }
        return null;
    }

    public final boolean isTrap(int tx, int ty)
    {
        boolean result = false;
        NWTile tile = this.getTile(tx, ty);
        int fg = tile.getForeBase();
        if (fg != PlaceID.pid_Undefined) {
            result = ((StaticData.dbPlaces[fg].Signs.contains(PlaceFlags.psIsTrap)));
        }
        return result;
    }

    @Override
    public LocatedEntity findItem(int aX, int aY)
    {
        return this.fItems.searchItemByPos(aX, aY);
    }

    public final void dropItem(NWCreature creature, Item item, int tx, int ty)
    {
        item.Owner = this;
        item.setPos(tx, ty);
        this.fItems.add(item, false);
    }

    public final void pickupItem(NWCreature creature, Item item)
    {
        this.fItems.extract(item);
    }

    public final void repackItems()
    {
        try {
            int num = this.fItems.getCount();
            if (num < 2) {
                return;
            }

            for (int i = 0; i < this.fItems.getCount(); i++) {
                Item it1 = this.fItems.getItem(i);

                for (int k = this.fItems.getCount() - 1; k >= i + 1; k--) {
                    Item it2 = this.fItems.getItem(k);
                    if (it1.getPosX() == it2.getPosX() && it1.getPosY() == it2.getPosY() && it1.assign(it2)) {
                        this.fItems.delete(k);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWField.repackItems(): " + ex.getMessage());
        }
    }

    public final void loadFromEntry(FieldEntry entry)
    {
        for (int yy = 0; yy < StaticData.FieldHeight; yy++) {
            for (int xx = 0; xx < StaticData.FieldWidth; xx++) {
                NWTile tile = this.getTile(xx, yy);

                FieldEntry.FDTile fdTile = entry.Data.Tiles[yy][xx];

                tile.Background = fdTile.backGround;
                tile.BackgroundExt = (short) PlaceID.pid_Undefined;

                tile.Foreground = fdTile.foreGround;
                tile.ForegroundExt = (short) PlaceID.pid_Undefined;

                tile.CreaturePtr = null;

                tile.FogID = (short) PlaceID.pid_Undefined;
                tile.FogExtID = (short) PlaceID.pid_Undefined;
                tile.FogAge = 0;
            }
        }
    }

    public final void clear()
    {
        try {
            super.getFeatures().clear();
            this.fCreatures.clear();
            this.fItems.clear();

            this.Visited = false;
        } catch (Exception ex) {
            Logger.write("NWField.clear(" + this.getDebugSign() + "): " + ex.getMessage());
            throw ex;
        }
    }

    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        //Logger.LogWrite("TField.avail: " + String.valueOf(stream.available()));
        
        try {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = this.getTile(x, y);
                    tile.loadFromStream(stream, version);
                }
            }

            this.fCreatures.loadFromStream(stream, version);
            this.fItems.loadFromStream(stream, version);

            int num3 = this.fItems.getCount();
            for (int i = 0; i < num3; i++) {
                this.fItems.getItem(i).Owner = this;
            }

            super.getFeatures().loadFromStream(stream, version);

            this.Visited = StreamUtils.readBoolean(stream);
        } catch (Exception ex) {
            Logger.write("NWField.loadFromStream(): " + ex.getMessage());
            throw ex;
        }
    }

    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        try {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = this.getTile(x, y);
                    tile.saveToStream(stream, version);
                }
            }

            this.fCreatures.saveToStream(stream, version);
            this.fItems.saveToStream(stream, version);
            super.getFeatures().saveToStream(stream, version);

            StreamUtils.writeBoolean(stream, this.Visited);
        } catch (Exception ex) {
            Logger.write("NWField.saveToStream(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void research(boolean onlyForeground, int tileStates)
    {
        for (int y = 0; y < StaticData.FieldHeight; y++) {
            for (int x = 0; x < StaticData.FieldWidth; x++) {
                NWTile tile = this.getTile(x, y);

                if (onlyForeground) {
                    if (tile.getForeBase() != PlaceID.pid_Undefined) {
                        tile.addStates(tileStates);
                    }
                } else {
                    tile.addStates(tileStates);
                }
            }
        }
    }

    public final boolean canSink(int x, int y)
    {
        NWTile tile = this.getTile(x, y);
        int bg = tile.getBackBase();
        return bg == PlaceID.pid_Water || bg == PlaceID.pid_Quicksand;
    }

    @Override
    public short translateTile(TileType defTile)
    {
        return UniverseBuilder.translateTile(defTile);
    }

    public static int getBuildPlaceKind(int x, int y, Rect r, boolean isRuins)
    {
        int res = PlaceID.pid_Undefined;

        if (x == r.Left && y == r.Top) {
            res = PlaceID.pid_WallNW;
        } else {
            if (x == r.Right && y == r.Top) {
                res = PlaceID.pid_WallNE;
            } else {
                if (x == r.Left && y == r.Bottom) {
                    res = PlaceID.pid_WallSW;
                } else {
                    if (x == r.Right && y == r.Bottom) {
                        res = PlaceID.pid_WallSE;
                    } else {
                        if (x > r.Left && x < r.Right && y == r.Top) {
                            res = PlaceID.pid_WallN;
                        } else {
                            if (x > r.Left && x < r.Right && y == r.Bottom) {
                                res = PlaceID.pid_WallS;
                            } else {
                                if (y > r.Top && y < r.Bottom && x == r.Left) {
                                    res = PlaceID.pid_WallW;
                                } else {
                                    if (y > r.Top && y < r.Bottom && x == r.Right) {
                                        res = PlaceID.pid_WallE;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isRuins) {
            switch (res) {
                case PlaceID.pid_WallNW:
                    res = PlaceID.pid_RnWallNW;
                    break;
                case PlaceID.pid_WallNE:
                    res = PlaceID.pid_RnWallNE;
                    break;
                case PlaceID.pid_WallSW:
                    res = PlaceID.pid_RnWallSW;
                    break;
                case PlaceID.pid_WallSE:
                    res = PlaceID.pid_RnWallSE;
                    break;
                case PlaceID.pid_WallN:
                    res = PlaceID.pid_RnWallN;
                    break;
                case PlaceID.pid_WallS:
                    res = PlaceID.pid_RnWallS;
                    break;
                case PlaceID.pid_WallW:
                    res = PlaceID.pid_RnWallW;
                    break;
                case PlaceID.pid_WallE:
                    res = PlaceID.pid_RnWallE;
                    break;
            }
        }
        
        return res;
    }

    public static short getVarTile(int tid)
    {
        int base = tid;
        int var;

        if ((StaticData.dbPlaces[base].Signs.contains(PlaceFlags.psVarDirect)) && StaticData.dbPlaces[base].SubsLoaded > 0) {
            var = AuxUtils.getBoundedRnd(0, StaticData.dbPlaces[base].SubsLoaded);
        } else {
            var = 0;
        }

        return (short)TypeUtils.fitShort(base, var);
    }

    public final String getDebugSign()
    {
        String res = String.format("%d, %d, %d, %s", this.EntryID, this.fCoords.X, this.fCoords.Y, this.fLandEntry.Sign);
        return res;
    }

    public final NWCreature addCreature(int px, int py, int creatureID)
    {
        return this.getSpace().addCreatureEx(this.getLayer().EntryID, this.fCoords.X, this.fCoords.Y, px, py, creatureID);
    }

    public final Item addItem(int px, int py, int itemID)
    {
        return this.getSpace().addItemEx(this.getLayer().EntryID, this.fCoords.X, this.fCoords.Y, px, py, itemID);
    }

    public final Region getRegion(int ax, int ay)
    {
        int num = super.getFeatures().getCount();
        for (int i = 0; i < num; i++) {
            GameEntity entry = super.getFeatures().getItem(i);
            if (entry instanceof Region) {
                Region region = ((Region) entry);
                if (region.inArea(ax, ay)) {
                    return region;
                }
            }
        }

        return null;
    }

    static {
        Muspel_bgTiles = new int[]{PlaceID.pid_Grass, PlaceID.pid_Water, PlaceID.pid_Quicksand, PlaceID.pid_Lava, PlaceID.pid_Mud, PlaceID.pid_Rubble, PlaceID.pid_Ground};

        Muspel_fgTiles = new int[]{PlaceID.pid_Undefined, PlaceID.pid_Tree, PlaceID.pid_Undefined, PlaceID.pid_Mountain, PlaceID.pid_Undefined, PlaceID.pid_DeadTree};
    }
    
    /// ALERT!

    @Override
    public void setMetaTile(int x, int y, TileType tile)
    {
        BaseTile baseTile = this.getTile(x, y);
        if (baseTile != null) {
            baseTile.Background = this.translateTile(tile);
        }
    }

    @Override
    public void fillMetaBorder(int x1, int y1, int x2, int y2, TileType tile)
    {
        short defTile = this.translateTile(tile);
        this.fillBorder(x1, y1, x2, y2, defTile, false);
    }

    public char getMetaTile(int x, int y)
    {
        BaseTile baseTile = this.getTile(x, y);
        if (baseTile != null) {
            return (char) baseTile.Background;
        } else {
            return ' ';
        }
    }

    @Override
    public float getPathTileCost(CreatureEntity creature, int tx, int ty, BaseTile tile)
    {
        boolean barrier;
        if (creature == null) {
            barrier = this.isBarrier(tx, ty);
        } else {
            barrier = !creature.canMove(this, tx, ty);
            if (!barrier && creature.isSeen(tx, ty, false)) {
                barrier = (this.findCreature(tx, ty) != null);
            }
        }

        return (barrier ? PathSearch.BARRIER_COST : 1.0f);
    }
}
