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
using System.Collections.Generic;
using System.IO;
using BSLib;
using NWR.Game;
using NWR.Game.Types;
using ZRLib.Core;
using ZRLib.Map;

namespace NWR.Universe
{
    public sealed class Village : AreaEntity
    {
        // only for road generation
        public List<ExtPoint> Gates;

        public override byte SerializeKind
        {
            get { return StaticData.SID_VILLAGE; }
        }


        public Village(GameSpace space, object owner) : base(space, owner)
        {
            NWField fld = (NWField)owner;
            ExtRect v = fld.AreaRect;
            v.Left += 18;
            v.Right -= 18;
            v.Top += 7;
            v.Bottom -= 5;
            Area = v;

            Gates = new List<ExtPoint>();
        }

        public void BuildBattlement(NWField field)
        {
            try {
                int lsd = field.Width - 1;
                int rsd = 0;
                int tsd = field.Height - 1;
                int bsd = 0;

                int num = field.Features.Count;
                for (int i = 0; i < num; i++) {
                    GameEntity feat = field.Features.GetItem(i);
                    if (feat is Building) {
                        Building b = (Building)feat;
                        ExtRect area = b.Area;

                        if (lsd > area.Left) {
                            lsd = area.Left;
                        }
                        if (rsd < area.Right) {
                            rsd = area.Right;
                        }
                        if (tsd > area.Top) {
                            tsd = area.Top;
                        }
                        if (bsd < area.Bottom) {
                            bsd = area.Bottom;
                        }
                    }
                }

                ExtRect r = ExtRect.Create(lsd - 2, tsd - 2, rsd + 2, bsd + 2);

                for (int x = r.Left; x <= r.Right; x++) {
                    for (int y = r.Top; y <= r.Bottom; y++) {
                        TileType wKind = MapUtils.GetWallKind(x, y, r);
                        if (wKind != TileType.ttFloor) {
                            field.GetTile(x, y).Foreground = field.TranslateTile(wKind);
                        }
                    }
                }

                for (int side = Directions.DtSouth; side <= Directions.DtEast; side++) {
                    int x = RandomHelper.GetBoundedRnd(r.Left + 1, r.Right - 1);
                    int y = RandomHelper.GetBoundedRnd(r.Top + 1, r.Bottom - 1);

                    switch (side) {
                        case Directions.DtNorth:
                            y = r.Top;
                            break;
                        case Directions.DtSouth:
                            y = r.Bottom;
                            break;
                        case Directions.DtWest:
                            x = r.Left;
                            break;
                        case Directions.DtEast:
                            x = r.Right;
                            break;
                    }

                    field.GetTile(x, y).Foreground = field.TranslateTile(TileType.ttUndefined);
                    x += Directions.Data[side].DX;
                    y += Directions.Data[side].DY;
                    field.GetTile(x, y).Foreground = field.TranslateTile(TileType.ttUndefined);
                    Gates.Add(new ExtPoint(x, y));
                }
            } catch (Exception ex) {
                Logger.Write("Village.buildBattlement(): " + ex.Message);
                throw ex;
            }
        }

        public void BuildVillage(NWField field, ExtRect area)
        {
            if (field.AreaRect.Contains(area)) {
                for (int y = area.Top; y <= area.Bottom; y++) {
                    for (int x = area.Left; x <= area.Right; x++) {
                        BaseTile ft = field.GetTile(x, y);
                        ft.Background = field.TranslateTile(TileType.ttGrass);
                        int fg = (int)ft.Foreground;
                        if (fg != field.TranslateTile(TileType.ttTree)) {
                            ft.Foreground = PlaceID.pid_Undefined;
                        }
                    }
                }

                for (var bid = BuildingID.bid_First; bid <= BuildingID.bid_Last; bid++) {
                    BuildingRec bRec = StaticData.dbBuildings[(int)bid];

                    int cnt = RandomHelper.GetBoundedRnd((int)bRec.MinCount, (int)bRec.MaxCount);
                    for (int j = 1; j <= cnt; j++) {
                        Building b = new Building(fSpace, field);
                        if (b.Build(bRec.MaxDoors, bRec.MinSize, bRec.MaxSize, area)) {
                            b.ID = bid;
                            field.Features.Add(b);
                        } else {
                            b.Dispose();
                        }
                    }
                }

                BuildBattlement(field);

                int wpX;
                int wpY;
                BaseTile tile;
                do {
                    wpX = RandomHelper.GetBoundedRnd(area.Left + 5, area.Right - 5);
                    wpY = RandomHelper.GetBoundedRnd(area.Top + 5, area.Bottom - 5);
                    tile = field.GetTile(wpX, wpY);
                } while (tile.BackBase != PlaceID.pid_Grass || tile.ForeBase != PlaceID.pid_Undefined);

                field.GetTile(wpX, wpY).Foreground = PlaceID.pid_Well;
            }
        }

        public override void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            ExtRect rt = StreamUtils.ReadRect(stream);
            Area = rt;
        }

        public override void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            ExtRect rt = Area;
            StreamUtils.WriteRect(stream, rt);
        }
    }
}
