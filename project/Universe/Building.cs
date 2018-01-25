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
using System.IO;
using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Game;
using NWR.Items;
using ZRLib.Core;
using ZRLib.Map;

namespace NWR.Universe
{
    public sealed class Building : BaseRoom
    {
        public static bool RuinsMode = false;

        private static readonly int[,] dbDoorsState;

        static Building()
        {
            dbDoorsState = new int[4, 2];
            dbDoorsState[0, 0] = PlaceID.pid_DoorN_Closed;
            dbDoorsState[0, 1] = PlaceID.pid_DoorN;
            dbDoorsState[1, 0] = PlaceID.pid_DoorS_Closed;
            dbDoorsState[1, 1] = PlaceID.pid_DoorS;
            dbDoorsState[2, 0] = PlaceID.pid_DoorW_Closed;
            dbDoorsState[2, 1] = PlaceID.pid_DoorW;
            dbDoorsState[3, 0] = PlaceID.pid_DoorE_Closed;
            dbDoorsState[3, 1] = PlaceID.pid_DoorE;
        }

        private const int TryCount = 100;

        private BuildingID fID;

        public CreatureEntity Holder;

        public Building(GameSpace space, object owner)
            : base(space, owner)
        {
            fID = BuildingID.bid_None;
        }

        public new NWGameSpace Space
        {
            get { return (NWGameSpace)fSpace; }
        }

        public BuildingID ID
        {
            get { return fID; }
            set { fID = value; }
        }


        private bool CanBuild(ExtRect br, ExtRect area)
        {
            try {
                AbstractMap map = (AbstractMap)Owner;

                bool result = area.Contains(br);
                if (result) {
                    br.Inflate(+1, +1);

                    bool res = map.ForEachTile(br.Left, br.Top, br.Right, br.Bottom, CanBuildCheck);
                    if (!res)
                        return false;
                    /*for (int y = br.Top; y <= br.Bottom; y++) {
                        for (int x = br.Left; x <= br.Right; x++) {
                            BaseTile tile = map.GetTile(x, y);
                            if (tile != null) {
                                int bg = tile.Background;
                                if (bg == PlaceID.pid_Floor || bg == PlaceID.pid_RnFloor) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }
                    }*/
                }

                return true;
            } catch (Exception ex) {
                Logger.Write("Building.canBuild(): " + ex.Message);
                return false;
            }
        }

        private bool CanBuildCheck(int x, int y, BaseTile tile)
        {
            if (tile != null) {
                ushort bg = tile.Background;
                if (bg == PlaceID.pid_Floor || bg == PlaceID.pid_RnFloor) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }

        public override string Name
        {
            get {
                string result = BaseLocale.GetStr(StaticData.dbBuildings[(int)fID].Name);
                if (Holder != null) {
                    result = BaseLocale.Format(RS.rs_BuildingName, new object[] {
                        result,
                        Holder.Name
                    });
                }
                return result;
            }
        }

        public override byte SerializeKind
        {
            get { return StaticData.SID_BUILDING; }
        }

        private void IsPassableDoor(int dx, int dy)
        {
            AbstractMap map = (AbstractMap)Owner;

            for (int yy = dy - 1; yy <= dy + 1; yy++) {
                for (int xx = dx - 1; xx <= dx + 1; xx++) {
                    BaseTile tile = map.GetTile(xx, yy);
                    if (tile != null) {
                        ushort fg = tile.Foreground;
                        if (fg != PlaceID.pid_Undefined && !MapUtils.IsBuilding(map, fg)) {
                            tile.Foreground = PlaceID.pid_Undefined;
                        }
                    }
                }
            }
        }

        public bool Build(byte maxDoors, byte minSize, byte maxSize, ExtRect area)
        {
            try {
                int hr = RandomHelper.GetBoundedRnd(minSize, maxSize);
                int wr = RandomHelper.GetBoundedRnd(minSize, maxSize);
                int tH = area.Height - hr;
                int tW = area.Width - wr;

                int tries = TryCount;
                while (tries > 0) {
                    int x = area.Left + RandomHelper.GetBoundedRnd(0, tW);
                    int y = area.Top + RandomHelper.GetBoundedRnd(0, tH);
                    ExtRect br = ExtRect.CreateBounds(x, y, wr, hr);

                    if (CanBuild(br, area)) {
                        Area = br;
                        if (maxDoors > 0) {
                            BuildDoors(maxDoors, br);
                        }
                        Flush();

                        return true;
                    }
                    tries--;
                }
            } catch (Exception ex) {
                Logger.Write("Building.build(): " + ex.Message);
            }

            return false;
        }

        private void BuildDoors(byte maxDoors, ExtRect br)
        {
            try {
                int dCnt;
                if (maxDoors == 1) {
                    dCnt = 1;
                } else {
                    dCnt = RandomHelper.GetBoundedRnd((int)maxDoors - 1, (int)((int)maxDoors + 1));
                }

                int dx = 0;
                int dy = 0;

                for (int i = 1; i <= dCnt; i++) {
                    int tries = TryCount;
                    do {
                        int side = (RandomHelper.GetBoundedRnd(Directions.DtFirst, Directions.DtLast));

                        switch (side) {
                            case Directions.DtNorth:
                                dx = RandomHelper.GetBoundedRnd(br.Left + 2, br.Right - 2);
                                dy = br.Top;
                                break;
                            case Directions.DtSouth:
                                dx = RandomHelper.GetBoundedRnd(br.Left + 2, br.Right - 2);
                                dy = br.Bottom;
                                break;
                            case Directions.DtWest:
                                dx = br.Left;
                                dy = RandomHelper.GetBoundedRnd(br.Top + 2, br.Bottom - 2);
                                break;
                            case Directions.DtEast:
                                dx = br.Right;
                                dy = RandomHelper.GetBoundedRnd(br.Top + 2, br.Bottom - 2);
                                break;
                        }

                        if (!IsExistDoor(dx, dy)) {
                            AddDoor(dx, dy, side, Door.STATE_OPENED);
                            IsPassableDoor(dx, dy);
                            break;
                        }

                        tries--;
                    } while (tries > 0);
                }
            } catch (Exception ex) {
                Logger.Write("Building.buildDoors(): " + ex.Message);
            }
        }

        public bool IsExistDoor(int ax, int ay)
        {
            int num = DoorsCount;
            for (int i = 0; i < num; i++) {
                Door dr = GetDoor(i);
                if ((dr.X == ax && dr.Y == ay) || MathHelper.Distance(ax, ay, dr.X, dr.Y) == 1) {
                    return true;
                }
            }

            return false;
        }

        public static void DrawWalls(NWField field, ExtRect r)
        {
            for (int x = r.Left; x <= r.Right; x++) {
                for (int y = r.Top; y <= r.Bottom; y++) {
                    NWTile tile = (NWTile)field.GetTile(x, y);

                    int bpk = NWField.GetBuildPlaceKind(x, y, r, RuinsMode);

                    if (!RuinsMode) {
                        tile.Back = PlaceID.pid_Floor;
                    } else {
                        if (bpk == PlaceID.pid_Undefined) {
                            tile.Back = NWField.GetVarTile(PlaceID.pid_RnFloor);
                        }
                    }

                    tile.Fore = bpk;
                }
            }
        }

        public void Flush()
        {
            try {
                NWField fld = (NWField)Owner;
                if (fld.LandID != GlobalVars.Land_Bazaar) {
                    ExtRect rt = Area;
                    DrawWalls(fld, rt);

                    if (!RuinsMode) {
                        int num3 = DoorsCount;
                        for (int i = 0; i < num3; i++) {
                            Door door = GetDoor(i);
                            fld.GetTile(door.X, door.Y).Fore = dbDoorsState[door.Dir - 1, door.State];
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("Building.flush(): " + ex.Message);
            }
        }

        public bool IsNearestDoor(int cx, int cy, ref Door aDoor, ref int dist, ref bool outside)
        {
            bool result = false;

            aDoor = null;
            dist = 0;
            outside = false;

            int idx = -1;
            int min_d = StaticData.FieldWidth;

            int num = DoorsCount;
            for (int i = 0; i < num; i++) {
                Door door = GetDoor(i);
                int d = MathHelper.Distance(cx, cy, door.X, door.Y);
                if (d < min_d) {
                    min_d = d;
                    idx = i;
                }
            }

            if (idx >= 0) {
                outside = !Area.Contains(cx, cy);
                aDoor = GetDoor(idx);
                dist = min_d;
                result = true;
            }

            return result;
        }

        public override void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            try {
                fID = (BuildingID)StreamUtils.ReadInt(stream);
                Area = StreamUtils.ReadRect(stream);

                ClearDoors();
                sbyte count = (sbyte)StreamUtils.ReadByte(stream);
                for (int i = 0; i < count; i++) {
                    int dx = StreamUtils.ReadInt(stream);
                    int dy = StreamUtils.ReadInt(stream);
                    int dir = StreamUtils.ReadByte(stream);
                    int st = StreamUtils.ReadByte(stream);
                    AddDoor(dx, dy, dir, st);
                }

                int idx = StreamUtils.ReadInt(stream);
                Holder = ((idx == -1) ? null : (CreatureEntity)((NWField)Owner).Creatures.GetItem(idx));
            } catch (Exception ex) {
                Logger.Write("Building.loadFromStream(): " + ex.Message);
                throw ex;
            }
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            try {
                StreamUtils.WriteInt(stream, (int)fID);
                StreamUtils.WriteRect(stream, Area);

                int count = DoorsCount;
                StreamUtils.WriteByte(stream, (byte)count);
                for (int i = 0; i < count; i++) {
                    Door dr = GetDoor(i);
                    StreamUtils.WriteInt(stream, dr.X);
                    StreamUtils.WriteInt(stream, dr.Y);
                    StreamUtils.WriteByte(stream, (byte)dr.Dir);
                    StreamUtils.WriteByte(stream, (byte)dr.State);
                }

                int idx = ((Holder == null) ? -1 : ((NWField)Owner).Creatures.IndexOf(Holder));
                StreamUtils.WriteInt(stream, idx);
            } catch (Exception ex) {
                Logger.Write("Building.saveToStream(): " + ex.Message);
                throw ex;
            }
        }

        public void SwitchDoors(int ds)
        {
            NWField f = (NWField)Owner;

            int num = DoorsCount;
            for (int i = 0; i < num; i++) {
                Door door = GetDoor(i);
                if (door.State != ds) {
                    door.State = ds;

                    switch (ds) {
                        case Door.STATE_CLOSED:
                            Space.DoEvent(EventID.event_DoorOpen, door, null, null);
                            break;
                        case Door.STATE_OPENED:
                            Space.DoEvent(EventID.event_DoorClose, door, null, null);
                            break;
                    }

                    f.GetTile(door.X, door.Y).Fore = dbDoorsState[door.Dir - 1, door.State];
                }
            }
        }

        // FIXME: total check & tests
        public void Prepare()
        {
            NWField fld = (NWField)Owner;
            ExtRect r = Area;
            r.Inflate(-1, -1);

            int id = (int)ID;
            var blRec = StaticData.dbBuildings[id];

            if (ID >= BuildingID.bid_MerchantShop && ID <= BuildingID.bid_WoodsmanShop) {
                NWCreature merchant = (NWCreature)Holder;
                if (merchant == null) {
                    Item coins = new Item(fSpace, null);
                    coins.CLSID = GlobalVars.iid_Coin;
                    coins.Count = (ushort)RandomHelper.GetBoundedRnd(15000, 25000);
                    int x = RandomHelper.GetBoundedRnd(r.Left, r.Right);
                    int y = RandomHelper.GetBoundedRnd(r.Top, r.Bottom);
                    merchant = fld.AddCreature(x, y, GlobalVars.nwrDB.FindEntryBySign(StaticData.dbSysCreatures[(int)blRec.Owner].Sign).GUID);
                    merchant.IsTrader = true;
                    merchant.AddItem(coins);
                    Holder = merchant;
                } else {
                    Item coins = (Item)merchant.Items.FindByCLSID(GlobalVars.iid_Coin);
                    if (coins != null) {
                        coins.Count = (ushort)RandomHelper.GetBoundedRnd(15000, 25000);
                    }
                }

                ProbabilityTable<int> data = blRec.Wares;
                if (((data != null) ? data.Size() : 0) > 0) {
                    for (int y = r.Top; y <= r.Bottom; y++) {
                        for (int x = r.Left; x <= r.Right; x++) {
                            Item item = (Item)fld.FindItem(x, y);
                            if (item == null) {
                                item = new Item(fSpace, this);
                                item.CLSID = data.GetRandomItem();
                                item.SetPos(x, y);
                                item.Owner = merchant;
                                item.GenCount();
                                fld.Items.Add(item, false);
                            } else {
                                if (AuxUtils.Chance(20)) {
                                    item.CLSID = data.GetRandomItem();
                                    item.GenCount();
                                }
                            }
                        }
                    }
                }
            }
        }

        public void Refresh()
        {
            try {
                if (Holder != null && Area.Contains(Holder.PosX, Holder.PosY)) {
                    Prepare();
                }
            } catch (Exception ex) {
                Logger.Write("Building.refresh(): " + ex.Message);
            }
        }
    }
}
