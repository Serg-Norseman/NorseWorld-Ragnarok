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
import jzrlib.core.AreaEntity;
import jzrlib.core.Directions;
import jzrlib.core.FileVersion;
import jzrlib.core.GameEntity;
import jzrlib.core.GameSpace;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.map.MapUtils;
import jzrlib.map.TileType;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.StreamUtils;
import nwr.core.StaticData;
import nwr.core.types.BuildingID;
import nwr.core.types.PlaceID;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Village extends AreaEntity
{
    // only for road generation
    public ArrayList<Point> Gates;

    public Village(GameSpace space, Object owner)
    {
        super(space, owner);
        
        NWField fld = (NWField) owner;
        Rect v = fld.getAreaRect();
        v.Left += 18;
        v.Right -= 18;
        v.Top += 7;
        v.Bottom -= 5;
        this.setArea(v);
        
        this.Gates = new ArrayList<>();
    }

    public void buildBattlement(NWField field)
    {
        try {
            int lsd = field.getWidth() - 1;
            int rsd = 0;
            int tsd = field.getHeight() - 1;
            int bsd = 0;

            int num = field.getFeatures().getCount();
            for (int i = 0; i < num; i++) {
                GameEntity feat = field.getFeatures().getItem(i);
                if (feat instanceof Building) {
                    Building b = (Building) feat;
                    Rect area = b.getArea();

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

            Rect r = new Rect(lsd - 2, tsd - 2, rsd + 2, bsd + 2);

            for (int x = r.Left; x <= r.Right; x++) {
                for (int y = r.Top; y <= r.Bottom; y++) {
                    TileType wKind = MapUtils.getWallKind(x, y, r);
                    if (wKind != TileType.ttFloor) {
                        field.getTile(x, y).Foreground = field.translateTile(wKind);
                    }
                }
            }

            for (int side = Directions.dtSouth; side <= Directions.dtEast; side++) {
                int x = AuxUtils.getBoundedRnd(r.Left + 1, r.Right - 1);
                int y = AuxUtils.getBoundedRnd(r.Top + 1, r.Bottom - 1);

                switch (side) {
                    case Directions.dtNorth:
                        y = r.Top;
                        break;
                    case Directions.dtSouth:
                        y = r.Bottom;
                        break;
                    case Directions.dtWest:
                        x = r.Left;
                        break;
                    case Directions.dtEast:
                        x = r.Right;
                        break;
                }

                field.getTile(x, y).Foreground = field.translateTile(TileType.ttUndefined);
                x += Directions.Data[side].dX;
                y += Directions.Data[side].dY;
                field.getTile(x, y).Foreground = field.translateTile(TileType.ttUndefined);
                this.Gates.add(new Point(x, y));
            }
        } catch (Exception ex) {
            Logger.write("Village.buildBattlement(): " + ex.getMessage());
            throw ex;
        }
    }

    public void buildVillage(NWField field, final Rect area)
    {
        if (field.getAreaRect().contains(area)) {
            for (int y = area.Top; y <= area.Bottom; y++) {
                for (int x = area.Left; x <= area.Right; x++) {
                    NWTile tile = field.getTile(x, y);
                    tile.Background = field.translateTile(TileType.ttGrass);
                    int fg = (int) tile.Foreground;
                    if (fg != field.translateTile(TileType.ttTree)) {
                        tile.setFore(PlaceID.pid_Undefined);
                    }
                }
            }

            for (int i = BuildingID.bid_First; i <= BuildingID.bid_Last; i++) {
                BuildingID bid = BuildingID.forValue(i);

                int cnt = AuxUtils.getBoundedRnd((int) bid.minCount, (int) bid.maxCount);
                for (int j = 1; j <= cnt; j++) {
                    Building b = new Building(this.fSpace, field);
                    if (b.build(bid.maxDoors, bid.minSize, bid.maxSize, area)) {
                        b.setID(bid);
                        field.getFeatures().add(b);
                    } else {
                        b.dispose();
                    }
                }
            }

            buildBattlement(field);

            int wpX;
            int wpY;
            NWTile tile;
            do {
                wpX = AuxUtils.getBoundedRnd(area.Left + 5, area.Right - 5);
                wpY = AuxUtils.getBoundedRnd(area.Top + 5, area.Bottom - 5);
                tile = field.getTile(wpX, wpY);
            } while (tile.getBackBase() != PlaceID.pid_Grass || tile.getForeBase() != PlaceID.pid_Undefined);

            field.getTile(wpX, wpY).setFore(PlaceID.pid_Well);
        }
    }
    
    @Override
    public byte getSerializeKind()
    {
        return StaticData.SID_VILLAGE;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        Rect rt = StreamUtils.readRect(stream);
        super.setArea(rt);
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        Rect rt = super.getArea();
        StreamUtils.writeRect(stream, rt);
    }
}
