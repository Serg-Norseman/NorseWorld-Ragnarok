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
package nwr.main;

import jzrlib.core.Directions;
import jzrlib.core.Rect;
import jzrlib.map.TileStates;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.TypeUtils;
import nwr.core.StaticData;
import nwr.core.types.ItfElement;
import nwr.core.types.PlaceID;
import nwr.core.types.SymbolID;
import nwr.effects.EffectTarget;
import nwr.engine.BaseScreen;
import nwr.engine.ImageList;
import nwr.game.NWGameSpace;
import nwr.gui.controls.CtlCommon;
import nwr.player.Player;
import nwr.universe.NWField;
import nwr.universe.NWTile;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public final class Painter
{
    public static void drawSymTile(NWGameSpace space, NWField field, int px, int py, 
            BaseScreen screen, final Rect mapRect, final Rect viewRect, ImageList symImages)
    {
        NWTile place = field.getTile(px, py);
        int sx = viewRect.Left + 8 * (px + 1);
        int sy = viewRect.Top + 10 * (py + 1);

        if (place != null && !place.isEmptyStates()) {
            int bg = (int) place.Background;
            int fg = (int) place.Foreground;

            fg = Painter.ptTransDoor((short) fg, place.States);
            short op = space.getTileBrightness(field, place.States, true);
            symImages.drawImage(screen, sx, sy, Painter.getSymImageIndex((short) bg), op);

            int fog = place.FogID;
            if (fog != PlaceID.pid_Undefined) {
                symImages.drawImage(screen, sx, sy, Painter.getSymImageIndex((short) PlaceID.pid_Fog), op);
            } else {
                if (fg != PlaceID.pid_Undefined) {
                    boolean trap = field.isTrap(px, py);
                    if (!trap || (trap && (place.Trap_Discovered || GlobalVars.Debug_Divinity))) {
                        symImages.drawImage(screen, sx, sy, Painter.getSymImageIndex((short) fg), op);
                    }
                }
            }

            if (!field.isBarrier(px, py) && mapRect.isBorder(px, py)) {
                symImages.drawImage(screen, sx, sy, Painter.getBorderSymbol(px, py).ImageIndex, op);
            }
        }
    }

    public static void drawLocTile(NWGameSpace space, NWField field, int px, int py, 
            BaseScreen screen, Player player, final Rect mapRect, final Rect viewRect, ImageList resImages)
    {
        NWTile place = field.getTile(px, py);
        int xx = viewRect.Left + 32 * (px - mapRect.Left);
        int yy = viewRect.Top + 30 * (py - mapRect.Top);

        if (place != null && !place.isEmptyStates()) {
            int bg = (int) place.Background;
            int fg = (int) place.Foreground;
            int bgExt = (int) place.BackgroundExt;
            int fgExt = (int) place.ForegroundExt;
            int fog = place.FogID;
            int fogExt = place.FogExtID;

            fg = Painter.ptTransDoor((short) fg, place.States);
            short op = space.getTileBrightness(field, place.States, false);
            resImages.drawImage(screen, xx, yy, Painter.getTileImageIndex((short) bg), op);
            if (bgExt != PlaceID.pid_Undefined) {
                resImages.drawImage(screen, xx, yy, Painter.getTileImageIndex((short) bgExt), op);
            }
            if (fog != PlaceID.pid_Undefined) {
                resImages.drawImage(screen, xx, yy, Painter.getTileImageIndex((short) fogExt), op);
            } else {
                if (fg != PlaceID.pid_Undefined) {
                    boolean trap = field.isTrap(px, py);
                    if (!trap || (trap && (place.Trap_Discovered || GlobalVars.Debug_Divinity))) {
                        resImages.drawImage(screen, xx, yy, Painter.getTileImageIndex((short) fg), op);
                        if (fgExt != PlaceID.pid_Undefined) {
                            resImages.drawImage(screen, xx, yy, Painter.getTileImageIndex((short) fgExt), op);
                        }
                    }
                }
                if (fogExt != PlaceID.pid_Undefined) {
                    resImages.drawImage(screen, xx, yy, Painter.getTileImageIndex((short) fogExt), op);
                }
            }

            SymbolID sid = Painter.getBorderSymbol(px, py);
            if (sid != SymbolID.sid_None && player.canMove(field, px, py)) {
                switch (sid) {
                    case sid_Left:
                        resImages.drawImage(screen, xx, yy, ItfElement.id_Left.ImageIndex, op);
                        break;
                    case sid_Up:
                        resImages.drawImage(screen, xx, yy, ItfElement.id_Up.ImageIndex, op);
                        break;
                    case sid_Right:
                        resImages.drawImage(screen, xx, yy, ItfElement.id_Right.ImageIndex, op);
                        break;
                    case sid_Down:
                        resImages.drawImage(screen, xx, yy, ItfElement.id_Down.ImageIndex, op);
                        break;
                }
            }
        }
    }

    public static void drawProperty(BaseScreen screen, int Left, int Top, int Right, String title, String value)
    {
        screen.drawText(Left, Top, title, 0);
        screen.drawText(Right - CtlCommon.smFont.getTextWidth(value), Top, value, 0);
    }

    public static ItfElement ptGetCursor(EffectTarget target, Player player, int mx, int my)
    {
        ItfElement result = ItfElement.id_Cursor;
        if (target == EffectTarget.et_Direction || target == EffectTarget.et_Creature) {
            int px = player.getPosX();
            int py = player.getPosY();
            int dist = AuxUtils.distance(px, py, mx, my);
            if (dist > 0) {
                switch (Directions.getDirByCoords(px, py, mx, my)) {
                    case Directions.dtNorth:
                        result = ItfElement.id_Cursor_N;
                        break;
                    case Directions.dtSouth:
                        result = ItfElement.id_Cursor_S;
                        break;
                    case Directions.dtWest:
                        result = ItfElement.id_Cursor_W;
                        break;
                    case Directions.dtEast:
                        result = ItfElement.id_Cursor_E;
                        break;
                    case Directions.dtNorthWest:
                        result = ItfElement.id_Cursor_NW;
                        break;
                    case Directions.dtNorthEast:
                        result = ItfElement.id_Cursor_NE;
                        break;
                    case Directions.dtSouthWest:
                        result = ItfElement.id_Cursor_SW;
                        break;
                    case Directions.dtSouthEast:
                        result = ItfElement.id_Cursor_SE;
                        break;
                }
            }
        }
        return result;
    }

    public static short ptTransDoor(short tid, int tileStates)
    {
        short result;

        if (!TileStates.hasState(tileStates, TileStates.TS_SEEN)) {
            int base = TypeUtils.getShortLo(tid);
            int var = TypeUtils.getShortHi(tid);

            int p = (base);
            int res;

            switch (p) {
                case PlaceID.pid_DoorN_Closed:
                    res = PlaceID.pid_DoorN;
                    break;
                case PlaceID.pid_DoorS_Closed:
                    res = PlaceID.pid_DoorS;
                    break;
                case PlaceID.pid_DoorW_Closed:
                    res = PlaceID.pid_DoorW;
                    break;
                case PlaceID.pid_DoorE_Closed:
                    res = PlaceID.pid_DoorE;
                    break;
                default:
                    res = p;
                    break;
            }

            result = (short) TypeUtils.fitShort(res, var);
        } else {
            result = tid;
        }

        return result;
    }

    public static int vtpChange(NWField fld, int tid)
    {
        int result;

        if (fld.LandID == GlobalVars.Land_Valhalla || fld.LandID == GlobalVars.Land_GodsFortress) {
            int pbase = (tid);
            int res;

            switch (pbase) {
                case PlaceID.pid_DoorN:
                    res = PlaceID.pid_AsgardDoorN;
                    break;
                case PlaceID.pid_DoorS:
                    res = PlaceID.pid_DoorS;
                    break;
                case PlaceID.pid_DoorW:
                    res = PlaceID.pid_DoorW;
                    break;
                case PlaceID.pid_DoorE:
                    res = PlaceID.pid_DoorE;
                    break;
                case PlaceID.pid_WallN:
                    res = PlaceID.pid_AsgardWallN;
                    break;
                case PlaceID.pid_WallS:
                    res = PlaceID.pid_AsgardWallS;
                    break;
                case PlaceID.pid_WallW:
                    res = PlaceID.pid_AsgardWallW;
                    break;
                case PlaceID.pid_WallE:
                    res = PlaceID.pid_AsgardWallE;
                    break;
                case PlaceID.pid_WallNW:
                    res = PlaceID.pid_WallNW;
                    break;
                case PlaceID.pid_WallNE:
                    res = PlaceID.pid_WallNE;
                    break;
                case PlaceID.pid_WallSW:
                    res = PlaceID.pid_WallSW;
                    break;
                case PlaceID.pid_WallSE:
                    res = PlaceID.pid_WallSE;
                    break;
                default:
                    res = pbase;
                    break;
            }

            result = (int) res;
        } else {
            result = tid;
        }

        return result;
    }

    public static boolean vtpForwardPlayer(Player player, int X, int Y)
    {
        int dX = player.getPosX() - X;
        int dY = player.getPosY() - Y;
        return dX <= 0 && dX >= -2 && dY <= 0 && dY >= -2;
    }

    public static int getTileImageIndex(short tid)
    {
        int result = -1;

        int base = TypeUtils.getShortLo(tid);
        int var = TypeUtils.getShortHi(tid);

        if (base >= PlaceID.pid_First && base <= PlaceID.pid_Last) {
            result = StaticData.dbPlaces[base].ImageIndex;
        }

        if (var > 0) {
            if (var < 1 || var > StaticData.dbPlaces[base].SubsLoaded) {
                var = 0;
            }

            result = result + var;
        }

        return result;
    }

    public static int getSymImageIndex(short tid)
    {
        int result;

        int base = TypeUtils.getShortLo(tid);
        int var = TypeUtils.getShortHi(tid);

        SymbolID sym = StaticData.dbPlaces[base].Symbol;
        result = sym.ImageIndex;

        if (var > 0) {
            if (var <= sym.subCount) {
                result += var;
            }
        }

        return result;
    }

    public static SymbolID getBorderSymbol(int x, int y)
    {
        SymbolID result = SymbolID.sid_None;

        if (y != -1) {
            if (y != 18) {
                if (x != -1) {
                    if (x == 76) {
                        result = SymbolID.sid_Right;
                    }
                } else {
                    result = SymbolID.sid_Left;
                }
            } else {
                result = SymbolID.sid_Down;
            }
        } else {
            result = SymbolID.sid_Up;
        }

        return result;
    }
}
