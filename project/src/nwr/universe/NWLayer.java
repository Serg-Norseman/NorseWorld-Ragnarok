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
import jzrlib.core.CreatureEntity;
import jzrlib.core.FileVersion;
import jzrlib.core.LocatedEntity;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.map.AbstractMap;
import jzrlib.map.BaseTile;
import jzrlib.map.Movements;
import jzrlib.map.PathSearch;
import jzrlib.map.TileType;
import jzrlib.utils.Logger;
import jzrlib.utils.TypeUtils;
import nwr.core.StaticData;
import nwr.database.LayerEntry;
import nwr.effects.EffectsList;
import nwr.game.NWGameSpace;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class NWLayer extends AbstractMap
{
    private final int FH;
    private final int FW;

    private EffectsList fEffects;
    private final LayerEntry fEntry;
    private NWField[][] fFields;
    
    private final NWGameSpace fSpace;
    public final int EntryID;

    public NWLayer(NWGameSpace space, int layerID)
    {
        super(0, 0);

        this.fSpace = space;

        this.fEntry = (LayerEntry) GlobalVars.nwrBase.getEntry(layerID);
        this.FW = this.fEntry.W;
        this.FH = this.fEntry.H;

        super.resize(this.FW * StaticData.FieldWidth, this.FH * StaticData.FieldHeight);

        this.EntryID = layerID;

        this.fFields = new NWField[this.FH][this.FW];

        for (int y = 0; y < this.FH; y++) {
            for (int x = 0; x < this.FW; x++) {
                this.fFields[y][x] = new NWField(space, this, new Point(x, y));
            }
        }

        this.fEffects = new EffectsList(this, true);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fEffects.dispose();
            this.fEffects = null;

            for (int y = 0; y < this.FH; y++) {
                for (int x = 0; x < this.FW; x++) {
                    this.fFields[y][x].dispose();
                }
            }
            this.fFields = null;
        }
        super.dispose(disposing);
    }

    public final NWGameSpace getSpace()
    {
        return (NWGameSpace) this.fSpace;
    }

    public final EffectsList getEffects()
    {
        return this.fEffects;
    }

    public final LayerEntry getEntry()
    {
        return this.fEntry;
    }
    
    public final int getH()
    {
        return this.FH;
    }

    public final int getW()
    {
        return this.FW;
    }

    public final NWField getField(int X, int Y)
    {
        if (X >= 0 && X < this.FW && Y >= 0 && Y < this.FH) {
            return this.fFields[Y][X];
        } else {
            return null;
        }
    }

    public String getName()
    {
        return GlobalVars.nwrBase.getEntry(this.EntryID).getName();
    }

    @Override
    public BaseTile getTile(int x, int y)
    {
        BaseTile result = null;

        if (super.isValid(x, y)) {
            int fx = x / StaticData.FieldWidth;
            int fy = y / StaticData.FieldHeight;

            int tx = x % StaticData.FieldWidth;
            int ty = y % StaticData.FieldHeight;

            NWField fld = this.getField(fx, fy);
            result = fld.getTile(tx, ty);
        }

        return result;
    }

    @Override
    public boolean isBlockLOS(int x, int y)
    {
        boolean result = true;
        if (super.isValid(x, y)) {
            int fx = x / StaticData.FieldWidth;
            int fy = y / StaticData.FieldHeight;
            int tx = x % StaticData.FieldWidth;
            int ty = y % StaticData.FieldHeight;
            result = this.getField(fx, fy).isBlockLOS(tx, ty);
        }
        return result;
    }

    @Override
    public CreatureEntity findCreature(int aX, int aY)
    {
        int fx = aX / StaticData.FieldWidth;
        int fy = aY / StaticData.FieldHeight;
        int x = aX % StaticData.FieldWidth;
        int y = aY % StaticData.FieldHeight;
        NWField f = this.getField(fx, fy);
        return f.findCreature(x, y);
    }

    /**
     * Performs the passage of time for the entire layer. 
     * Necessary in cases, such as for example, the battle on the field Vigrid if the player is in Valhalla.
     */
    public final void doTurn()
    {
        try {
            for (int y = 0; y < this.FH; y++) {
                for (int x = 0; x < this.FW; x++) {
                    NWField fld = this.fFields[y][x];
                    fld.doTurn();
                }
            }

            this.fEffects.execute();
        } catch (Exception ex) {
            Logger.write("NWLayer.doTurn(): " + ex.getMessage());
        }
    }

    public final DungeonRoom findDungeonRoom(int aX, int aY)
    {
        int num = super.getFeatures().getCount();
        for (int i = 0; i < num; i++) {
            Object feat = super.getFeatures().getItem(i);
            if (feat instanceof DungeonRoom) {
                DungeonRoom dr = (DungeonRoom) feat;
                if (dr.inArea(aX, aY)) {
                    return dr;
                }
            }
        }

        return null;
    }

    public final boolean gen_Creature(int aID, int aX, int aY, boolean aFlock)
    {
        int fx = aX / StaticData.FieldWidth;
        int fy = aY / StaticData.FieldHeight;
        int x = aX % StaticData.FieldWidth;
        int y = aY % StaticData.FieldHeight;
        NWField f = this.getField(fx, fy);
        return UniverseBuilder.gen_Creature(f, aID, x, y, aFlock);
    }

    @Override
    public Movements getTileMovements(short tileID)
    {
        int pd = TypeUtils.getShortLo(tileID);
        return StaticData.dbPlaces[pd].Moves;
    }

    public final void initLayer()
    {
        LayerEntry layer_entry = (LayerEntry) GlobalVars.nwrBase.getEntry(this.EntryID);
        String entry_sign = layer_entry.Sign;

        if (GlobalVars.Debug_TestWorldGen) {
            Logger.write("NWLayer.initLayer().start >>>>> " + entry_sign);
        }

        try {
            for (int y = 0; y < this.FH; y++) {
                for (int x = 0; x < this.FW; x++) {
                    this.fFields[y][x].clear();
                    GlobalVars.nwrWin.ProgressStep();
                }
            }

            if (this.EntryID == GlobalVars.Layer_Svartalfheim1 || this.EntryID == GlobalVars.Layer_Svartalfheim2) {
                UniverseBuilder.build_Dungeon(this, this.getAreaRect());
            } else {
                if (this.EntryID == GlobalVars.Layer_Svartalfheim3) {
                    Rect area = new Rect(0, 0, StaticData.FieldWidth - 1, StaticData.FieldHeight * 3 - 1);
                    UniverseBuilder.build_Dungeon(this, area);

                    area = new Rect(StaticData.FieldWidth, StaticData.FieldHeight * 2, StaticData.FieldWidth * 2 - 1, StaticData.FieldHeight * 3 - 1);
                    UniverseBuilder.build_Dungeon(this, area);

                    area = new Rect(StaticData.FieldWidth, 0, StaticData.FieldWidth * 3 - 1, StaticData.FieldHeight * 2 - 1);
                    UniverseBuilder.build_Caves(this, area);
                }
            }

            for (int y = 0; y < this.FH; y++) {
                for (int x = 0; x < this.FW; x++) {
                    this.fFields[y][x].initField();
                    GlobalVars.nwrWin.ProgressStep();
                }
            }

            for (int y = 0; y < this.FH; y++) {
                for (int x = 0; x < this.FW; x++) {
                    this.fFields[y][x].normalize();
                    GlobalVars.nwrWin.ProgressStep();
                }
            }
        } catch (Exception ex) {
            Logger.write("NWLayer.initLayer(" + entry_sign + "): " + ex.getMessage());
        }

        if (GlobalVars.Debug_TestWorldGen) {
            Logger.write("NWLayer.initLayer().finish >>>>> " + entry_sign);
        }
    }

    @Override
    public boolean isBarrier(int x, int y)
    {
        boolean result = true;
        if (super.isValid(x, y)) {
            int fx = x / StaticData.FieldWidth;
            int fy = y / StaticData.FieldHeight;
            int tx = x % StaticData.FieldWidth;
            int ty = y % StaticData.FieldHeight;
            result = this.getField(fx, fy).isBarrier(tx, ty);
        }
        return result;
    }

    @Override
    public LocatedEntity findItem(int aX, int aY)
    {
        int fx = aX / StaticData.FieldWidth;
        int fy = aY / StaticData.FieldHeight;
        int x = aX % StaticData.FieldWidth;
        int y = aY % StaticData.FieldHeight;
        NWField f = this.getField(fx, fy);
        return f.findItem(x, y);
    }

    @Override
    public void normalize()
    {
        for (int y = 0; y < this.FH; y++) {
            for (int x = 0; x < this.FW; x++) {
                this.fFields[y][x].normalize();
            }
        }
    }

    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        try {
            for (int y = 0; y < this.FH; y++) {
                for (int x = 0; x < this.FW; x++) {
                    NWField fld = this.fFields[y][x];

                    fld.clear();
                    fld.loadFromStream(stream, version);

                    GlobalVars.nwrWin.ProgressStep();
                }
            }

            for (int y = 0; y < this.FH; y++) {
                for (int x = 0; x < this.FW; x++) {
                    NWField fld = this.fFields[y][x];

                    fld.normalize();
                    fld.normalizeFog();
                }
            }
        } catch (Exception ex) {
            Logger.write("NWLayer.loadFromStream(): " + ex.getMessage());
            throw ex;
        }
    }

    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        try {
            for (int y = 0; y < this.FH; y++) {
                for (int x = 0; x < this.FW; x++) {
                    this.fFields[y][x].saveToStream(stream, version);
                    GlobalVars.nwrWin.ProgressStep();
                }
            }
        } catch (Exception ex) {
            Logger.write("NWLayer.saveToStream(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public short translateTile(TileType defTile)
    {
        return UniverseBuilder.translateTile(defTile);
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
