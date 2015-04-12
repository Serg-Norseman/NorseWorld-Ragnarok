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
import jzrlib.core.Directions;
import jzrlib.core.FileVersion;
import jzrlib.core.GameSpace;
import jzrlib.core.ProbabilityTable;
import jzrlib.core.Rect;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.map.AbstractMap;
import jzrlib.map.BaseTile;
import jzrlib.map.MapUtils;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.RefObject;
import jzrlib.utils.StreamUtils;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.BuildingID;
import nwr.core.types.EventID;
import nwr.core.types.PlaceID;
import nwr.creatures.NWCreature;
import nwr.game.NWGameSpace;
import nwr.item.Item;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Building extends BaseRoom
{
    public static boolean RuinsMode = false;
    
    private static final int[][] dbDoorsState;

    static {
        dbDoorsState = new int[4][2];
        dbDoorsState[0][0] = PlaceID.pid_DoorN_Closed;
        dbDoorsState[0][1] = PlaceID.pid_DoorN;
        dbDoorsState[1][0] = PlaceID.pid_DoorS_Closed;
        dbDoorsState[1][1] = PlaceID.pid_DoorS;
        dbDoorsState[2][0] = PlaceID.pid_DoorW_Closed;
        dbDoorsState[2][1] = PlaceID.pid_DoorW;
        dbDoorsState[3][0] = PlaceID.pid_DoorE_Closed;
        dbDoorsState[3][1] = PlaceID.pid_DoorE;
    }

    private static final int TryCount = 100;

    private BuildingID fID;

    public CreatureEntity Holder;

    public Building(GameSpace space, Object owner)
    {
        super(space, owner);
        this.fID = BuildingID.bid_None;
    }

    @Override
    public final NWGameSpace getSpace()
    {
        return (NWGameSpace) this.fSpace;
    }

    public final BuildingID getID()
    {
        return this.fID;
    }

    public final void setID(BuildingID value)
    {
        this.fID = value;
    }

    protected boolean canBuild(Rect br, Rect area)
    {
        try {
            AbstractMap map = (AbstractMap) super.Owner;

            boolean result = area.contains(br);
            if (result) {
                br = new Rect(br.Left - 1, br.Top - 1, br.Right + 1, br.Bottom + 1);

                for (int y = br.Top; y <= br.Bottom; y++) {
                    for (int x = br.Left; x <= br.Right; x++) {
                        BaseTile tile = map.getTile(x, y);
                        if (tile != null) {
                            int bg = tile.Background;
                            if (bg == PlaceID.pid_Floor || bg == PlaceID.pid_RnFloor) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            Logger.write("Building.canBuild(): " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String getName()
    {
        String result = Locale.getStr(this.fID.NameRS);
        if (this.Holder != null) {
            result = Locale.format(RS.rs_BuildingName, new Object[]{result, this.Holder.getName()});
        }
        return result;
    }

    @Override
    public byte getSerializeKind()
    {
        return StaticData.SID_BUILDING;
    }

    private void isPassableDoor(int dx, int dy)
    {
        AbstractMap map = (AbstractMap) super.Owner;

        for (int yy = dy - 1; yy <= dy + 1; yy++) {
            for (int xx = dx - 1; xx <= dx + 1; xx++) {
                BaseTile tile = map.getTile(xx, yy);
                if (tile != null) {
                    int fg = (int) tile.Foreground;
                    if (fg != PlaceID.pid_Undefined && !MapUtils.isBuilding(map, (short) fg)) {
                        tile.Foreground = PlaceID.pid_Undefined;
                    }
                }
            }
        }
    }

    public final boolean build(byte maxDoors, byte minSize, byte maxSize, Rect area)
    {
        try {
            int hr = AuxUtils.getBoundedRnd(minSize, maxSize);
            int wr = AuxUtils.getBoundedRnd(minSize, maxSize);
            int tH = area.getHeight() - hr;
            int tW = area.getWidth() - wr;

            int tries = Building.TryCount;
            while (tries > 0) {
                int x = area.Left + AuxUtils.getBoundedRnd(0, tW);
                int y = area.Top + AuxUtils.getBoundedRnd(0, tH);
                Rect br = new Rect(x, y, x + wr - 1, y + hr - 1);

                if (this.canBuild(br, area)) {
                    this.setArea(br.Left, br.Top, br.Right, br.Bottom);
                    if (maxDoors > 0) {
                        this.buildDoors(maxDoors, br);
                    }
                    this.flush();

                    return true;
                }
                tries--;
            }
        } catch (Exception ex) {
            Logger.write("Building.build(): " + ex.getMessage());
        }

        return false;
    }

    private void buildDoors(byte maxDoors, Rect br)
    {
        try {
            int dCnt;
            if (maxDoors == 1) {
                dCnt = 1;
            } else {
                dCnt = AuxUtils.getBoundedRnd((int) maxDoors - 1, (int) ((int) maxDoors + 1));
            }

            int dx = 0;
            int dy = 0;

            for (int i = 1; i <= dCnt; i++) {
                int tries = Building.TryCount;
                do {
                    int side = (AuxUtils.getBoundedRnd(Directions.dtFirst, Directions.dtLast));

                    switch (side) {
                        case Directions.dtNorth:
                            dx = AuxUtils.getBoundedRnd(br.Left + 2, br.Right - 2);
                            dy = br.Top;
                            break;
                        case Directions.dtSouth:
                            dx = AuxUtils.getBoundedRnd(br.Left + 2, br.Right - 2);
                            dy = br.Bottom;
                            break;
                        case Directions.dtWest:
                            dx = br.Left;
                            dy = AuxUtils.getBoundedRnd(br.Top + 2, br.Bottom - 2);
                            break;
                        case Directions.dtEast:
                            dx = br.Right;
                            dy = AuxUtils.getBoundedRnd(br.Top + 2, br.Bottom - 2);
                            break;
                    }

                    if (!this.isExistDoor(dx, dy)) {
                        super.addDoor(dx, dy, side, Door.STATE_OPENED);
                        this.isPassableDoor(dx, dy);
                        break;
                    }
                    
                    tries--;
                } while (tries > 0);
            }
        } catch (Exception ex) {
            Logger.write("Building.buildDoors(): " + ex.getMessage());
        }
    }

    public final boolean isExistDoor(int ax, int ay)
    {
        int num = super.getDoorsCount();
        for (int i = 0; i < num; i++) {
            Door dr = super.getDoor(i);
            if ((dr.X == ax && dr.Y == ay) || AuxUtils.distance(ax, ay, dr.X, dr.Y) == 1) {
                return true;
            }
        }

        return false;
    }
    
    public static final void drawWalls(NWField field, Rect r)
    {
        for (int x = r.Left; x <= r.Right; x++) {
            for (int y = r.Top; y <= r.Bottom; y++) {
                NWTile tile = field.getTile(x, y);

                int bpk = NWField.getBuildPlaceKind(x, y, r, RuinsMode);

                if (!RuinsMode) {
                    tile.setBack(PlaceID.pid_Floor);
                } else {
                    if (bpk == PlaceID.pid_Undefined) {
                        tile.setBack(NWField.getVarTile(PlaceID.pid_RnFloor));
                    }
                }

                tile.setFore(bpk);
            }
        }
    }

    public final void flush()
    {
        try {
            NWField fld = (NWField) super.Owner;
            if (fld.LandID != GlobalVars.Land_Bazaar) {
                Rect rt = super.getArea();

                Building.drawWalls(fld, rt);

                if (!RuinsMode) {
                    int num3 = super.getDoorsCount();
                    for (int i = 0; i < num3; i++) {
                        Door door = super.getDoor(i);
                        fld.getTile(door.X, door.Y).setFore(Building.dbDoorsState[door.Dir - 1][door.State]);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("Building.flush(): " + ex.getMessage());
        }
    }

    public final boolean isNearestDoor(int cx, int cy, RefObject<Door> aDoor, RefObject<Integer> dist, RefObject<Boolean> outside)
    {
        boolean result = false;

        aDoor.argValue = null;
        dist.argValue = 0;
        outside.argValue = false;

        int idx = -1;
        int min_d = StaticData.FieldWidth;

        int num = super.getDoorsCount();
        for (int i = 0; i < num; i++) {
            Door door = super.getDoor(i);
            int d = AuxUtils.distance(cx, cy, door.X, door.Y);
            if (d < min_d) {
                min_d = d;
                idx = i;
            }
        }

        if (idx >= 0) {
            outside.argValue = !super.getArea().contains(cx, cy);
            aDoor.argValue = super.getDoor(idx);
            dist.argValue = min_d;
            result = true;
        }

        return result;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        try {
            this.fID = BuildingID.forValue(StreamUtils.readInt(stream));
            this.setArea(StreamUtils.readRect(stream));

            super.clearDoors();
            byte count = (byte) StreamUtils.readByte(stream);
            for (int i = 0; i < count; i++) {
                int dx = StreamUtils.readInt(stream);
                int dy = StreamUtils.readInt(stream);
                int dir = StreamUtils.readByte(stream);
                int st = StreamUtils.readByte(stream);
                super.addDoor(dx, dy, dir, st);
            }

            int idx = StreamUtils.readInt(stream);
            this.Holder = ((idx == -1) ? null : (CreatureEntity) ((NWField) this.Owner).getCreatures().getItem(idx));
        } catch (Exception ex) {
            Logger.write("Building.loadFromStream(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        try {
            StreamUtils.writeInt(stream, this.fID.Value);
            StreamUtils.writeRect(stream, super.getArea());

            int count = super.getDoorsCount();
            StreamUtils.writeByte(stream, (byte) count);
            for (int i = 0; i < count; i++) {
                Door dr = super.getDoor(i);
                StreamUtils.writeInt(stream, dr.X);
                StreamUtils.writeInt(stream, dr.Y);
                StreamUtils.writeByte(stream, (byte) dr.Dir);
                StreamUtils.writeByte(stream, (byte) dr.State);
            }

            int idx = ((this.Holder == null) ? -1 : ((NWField) this.Owner).getCreatures().indexOf(this.Holder));
            StreamUtils.writeInt(stream, idx);
        } catch (Exception ex) {
            Logger.write("Building.saveToStream(): " + ex.getMessage());
            throw ex;
        }
    }

    public final void switchDoors(int ds)
    {
        NWField f = (NWField) super.Owner;

        int num = super.getDoorsCount();
        for (int i = 0; i < num; i++) {
            Door door = super.getDoor(i);
            if (door.State != ds) {
                door.State = ds;

                switch (ds) {
                    case Door.STATE_CLOSED:
                        this.getSpace().doEvent(EventID.event_DoorOpen, door, null, null);
                        break;
                    case Door.STATE_OPENED:
                        this.getSpace().doEvent(EventID.event_DoorClose, door, null, null);
                        break;
                }

                f.getTile(door.X, door.Y).setFore(Building.dbDoorsState[door.Dir - 1][door.State]);
            }
        }
    }

    // FIXME: total check & tests
    public final void prepare()
    {
        NWField fld = (NWField) super.Owner;
        Rect r = super.getArea().clone();
        r.inflate(-1, -1);

        int id = this.getID().Value;

        if (id >= BuildingID.bid_MerchantShop.Value && id <= BuildingID.bid_WoodsmanShop.Value) {
            NWCreature merchant = (NWCreature) this.Holder;
            if (merchant == null) {
                Item coins = new Item(this.fSpace, null);
                coins.setCLSID(GlobalVars.iid_Coin);
                coins.Count = (short) AuxUtils.getBoundedRnd(15000, 25000);
                int x = AuxUtils.getBoundedRnd(r.Left, r.Right);
                int y = AuxUtils.getBoundedRnd(r.Top, r.Bottom);
                merchant = fld.addCreature(x, y, GlobalVars.nwrBase.findEntryBySign(this.getID().Owner.Sign).GUID);
                merchant.setIsTrader(true);
                merchant.addItem(coins);
                this.Holder = merchant;
            } else {
                Item coins = (Item) merchant.getItems().findByCLSID(GlobalVars.iid_Coin);
                if (coins != null) {
                    coins.Count = (short) AuxUtils.getBoundedRnd(15000, 25000);
                }
            }

            ProbabilityTable<Integer> data = this.getID().Wares;
            if (((data != null) ? data.size() : 0) > 0) {
                for (int y = r.Top; y <= r.Bottom; y++) {
                    for (int x = r.Left; x <= r.Right; x++) {
                        Item item = (Item) fld.findItem(x, y);
                        if (item == null) {
                            item = new Item(this.fSpace, this);
                            item.setCLSID(data.getRandomItem());
                            item.setPos(x, y);
                            item.Owner = merchant;
                            item.genCount();
                            fld.getItems().add(item, false);
                        } else {
                            if (AuxUtils.chance(20)) {
                                item.setCLSID(data.getRandomItem());
                                item.genCount();
                            }
                        }
                    }
                }
            }
        }
    }

    public final void refresh()
    {
        try {
            if (this.Holder != null && super.getArea().contains(this.Holder.getPosX(), this.Holder.getPosY())) {
                this.prepare();
            }
        } catch (Exception ex) {
            Logger.write("Building.refresh(): " + ex.getMessage());
        }
    }
}
