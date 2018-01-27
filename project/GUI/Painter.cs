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

using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Effects;
using NWR.Game;
using NWR.GUI.Controls;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Engine;
using ZRLib.Map;

namespace NWR.GUI
{
    public static class Painter
    {
        public static void DrawSymTile(NWGameSpace space, NWField field, int px, int py, BaseScreen screen, ExtRect mapRect, ExtRect viewRect, ImageList symImages)
        {
            NWTile place = (NWTile)field.GetTile(px, py);
            int sx = viewRect.Left + 8 * (px + 1);
            int sy = viewRect.Top + 10 * (py + 1);

            if (place != null && !place.EmptyStates) {
                ushort bg = place.Background;
                ushort fg = place.Foreground;

                fg = PtTransDoor(fg, place);
                short op = space.GetTileBrightness(field, place, true);
                symImages.DrawImage(screen, sx, sy, GetSymImageIndex(bg), op);

                int fog = place.FogID;
                if (fog != PlaceID.pid_Undefined) {
                    symImages.DrawImage(screen, sx, sy, GetSymImageIndex((ushort)PlaceID.pid_Fog), op);
                } else {
                    if (fg != PlaceID.pid_Undefined) {
                        bool trap = field.IsTrap(px, py);
                        if (!trap || (trap && (place.Trap_Discovered || GlobalVars.Debug_Divinity))) {
                            symImages.DrawImage(screen, sx, sy, GetSymImageIndex(fg), op);
                        }
                    }
                }

                if (!field.IsBarrier(px, py) && mapRect.IsBorder(px, py)) {
                    symImages.DrawImage(screen, sx, sy, StaticData.dbSymbols[(int)GetBorderSymbol(px, py)].ImageIndex, op);
                }
            }
        }

        public static void DrawLocTile(NWGameSpace space, NWField field, int px, int py, BaseScreen screen, Player player, ExtRect mapRect, ExtRect viewRect, ImageList resImages)
        {
            NWTile place = (NWTile)field.GetTile(px, py);
            int xx = viewRect.Left + 32 * (px - mapRect.Left);
            int yy = viewRect.Top + 30 * (py - mapRect.Top);

            if (place != null && !place.EmptyStates) {
                int bg = (int)place.Background;
                int fg = (int)place.Foreground;
                int bgExt = (int)place.BackgroundExt;
                int fgExt = (int)place.ForegroundExt;
                int fog = place.FogID;
                int fogExt = place.FogExtID;

                fg = PtTransDoor((ushort)fg, place);
                short op = space.GetTileBrightness(field, place, false);
                resImages.DrawImage(screen, xx, yy, GetTileImageIndex((ushort)bg), op);
                if (bgExt != PlaceID.pid_Undefined) {
                    resImages.DrawImage(screen, xx, yy, GetTileImageIndex((ushort)bgExt), op);
                }
                if (fog != PlaceID.pid_Undefined) {
                    resImages.DrawImage(screen, xx, yy, GetTileImageIndex((ushort)fogExt), op);
                } else {
                    if (fg != PlaceID.pid_Undefined) {
                        bool trap = field.IsTrap(px, py);
                        if (!trap || (trap && (place.Trap_Discovered || GlobalVars.Debug_Divinity))) {
                            resImages.DrawImage(screen, xx, yy, GetTileImageIndex((ushort)fg), op);
                            if (fgExt != PlaceID.pid_Undefined) {
                                resImages.DrawImage(screen, xx, yy, GetTileImageIndex((ushort)fgExt), op);
                            }
                        }
                    }
                    if (fogExt != PlaceID.pid_Undefined) {
                        resImages.DrawImage(screen, xx, yy, GetTileImageIndex((ushort)fogExt), op);
                    }
                }

                SymbolID sid = GetBorderSymbol(px, py);
                if (sid != SymbolID.sid_None && player.CanMove(field, px, py)) {
                    switch (sid) {
                        case SymbolID.sid_Left:
                            resImages.DrawImage(screen, xx, yy, StaticData.dbItfElements[(int)ItfElement.id_Left].ImageIndex, op);
                            break;
                        case SymbolID.sid_Up:
                            resImages.DrawImage(screen, xx, yy, StaticData.dbItfElements[(int)ItfElement.id_Up].ImageIndex, op);
                            break;
                        case SymbolID.sid_Right:
                            resImages.DrawImage(screen, xx, yy, StaticData.dbItfElements[(int)ItfElement.id_Right].ImageIndex, op);
                            break;
                        case SymbolID.sid_Down:
                            resImages.DrawImage(screen, xx, yy, StaticData.dbItfElements[(int)ItfElement.id_Down].ImageIndex, op);
                            break;
                    }
                }
            }
        }

        public static void DrawProperty(BaseScreen screen, int Left, int Top, int Right, string title, string value)
        {
            screen.DrawText(Left, Top, title, 0);
            screen.DrawText(Right - CtlCommon.SmFont.GetTextWidth(value), Top, value, 0);
        }

        public static ItfElement PtGetCursor(EffectTarget target, Player player, int mx, int my)
        {
            ItfElement result = ItfElement.id_Cursor;
            if (target == EffectTarget.et_Direction || target == EffectTarget.et_Creature) {
                int px = player.PosX;
                int py = player.PosY;
                int dist = MathHelper.Distance(px, py, mx, my);
                if (dist > 0) {
                    switch (Directions.GetDirByCoords(px, py, mx, my)) {
                        case Directions.DtNorth:
                            result = ItfElement.id_Cursor_N;
                            break;
                        case Directions.DtSouth:
                            result = ItfElement.id_Cursor_S;
                            break;
                        case Directions.DtWest:
                            result = ItfElement.id_Cursor_W;
                            break;
                        case Directions.DtEast:
                            result = ItfElement.id_Cursor_E;
                            break;
                        case Directions.DtNorthWest:
                            result = ItfElement.id_Cursor_NW;
                            break;
                        case Directions.DtNorthEast:
                            result = ItfElement.id_Cursor_NE;
                            break;
                        case Directions.DtSouthWest:
                            result = ItfElement.id_Cursor_SW;
                            break;
                        case Directions.DtSouthEast:
                            result = ItfElement.id_Cursor_SE;
                            break;
                    }
                }
            }
            return result;
        }

        public static ushort PtTransDoor(ushort tid, BaseTile tile)
        {
            ushort result;

            if (!tile.HasState(BaseTile.TS_SEEN)) {
                int @base = AuxUtils.GetShortLo(tid);
                int @var = AuxUtils.GetShortHi(tid);

                int p = (@base);
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

                result = AuxUtils.FitShort(res, @var);
            } else {
                result = tid;
            }

            return result;
        }

        public static int VtpChange(NWField fld, int tid)
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

                result = (int)res;
            } else {
                result = tid;
            }

            return result;
        }

        public static bool VtpForwardPlayer(Player player, int X, int Y)
        {
            int dX = player.PosX - X;
            int dY = player.PosY - Y;
            return dX <= 0 && dX >= -2 && dY <= 0 && dY >= -2;
        }

        public static int GetTileImageIndex(ushort tid)
        {
            int result = -1;

            int @base = AuxUtils.GetShortLo(tid);
            int @var = AuxUtils.GetShortHi(tid);

            if (@base >= PlaceID.pid_First && @base <= PlaceID.pid_Last) {
                result = StaticData.dbPlaces[@base].ImageIndex;
            }

            if (@var > 0) {
                if (@var < 1 || @var > StaticData.dbPlaces[@base].SubsLoaded) {
                    @var = 0;
                }

                result = result + @var;
            }

            return result;
        }

        public static int GetSymImageIndex(ushort tid)
        {
            int result;

            int @base = AuxUtils.GetShortLo(tid);
            int @var = AuxUtils.GetShortHi(tid);

            SymbolID sym = StaticData.dbPlaces[@base].Symbol;
            SymbolRec symRec = StaticData.dbSymbols[(int)sym];
            result = symRec.ImageIndex;

            if (@var > 0) {
                if (@var <= symRec.SubCount) {
                    result += @var;
                }
            }

            return result;
        }

        public static SymbolID GetBorderSymbol(int x, int y)
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
}
