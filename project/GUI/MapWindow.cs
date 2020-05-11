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

using BSLib;
using NWR.Database;
using NWR.Game;
using NWR.Game.Types;
using NWR.GUI.Controls;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Engine;
using ZRLib.Map;

namespace NWR.GUI
{
    public sealed class MapWindow : NWWindow
    {
        private readonly BaseImage fImage;
        private bool fMapCursor;
        private string fMapHint;
        private long fPrevTime;


        public MapWindow(BaseControl owner) : base(owner)
        {
            Font = CtlCommon.SmFont;
            Width = 656;
            Height = 496;
            WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);
            BackDraw = false;
            Shifted = false;

            fImage = NWResourceManager.LoadImage(MainWindow.Screen, "itf/MapBack.tga", Colors.None);
            fMapHint = "";
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                if (fImage != null) {
                    fImage.Dispose();
                }
            }
            base.Dispose(disposing);
        }

        public override void Update(long time)
        {
            base.Update(time);

            // timer default = 500
            if (time - fPrevTime >= 500 && Visible) {
                fMapCursor = !fMapCursor;
                fPrevTime = time;
            }
        }

        private class SearchResult
        {
            internal int LID;
            internal int FieldX;
            internal int FieldY;
        }

        private static SearchResult SearchMapLocation(int aX, int aY)
        {
            aX -= 8;
            aY -= 8;

            int num = GlobalVars.nwrGame.LayersCount;
            for (int idx = 0; idx < num; idx++) {
                NWLayer layer = GlobalVars.nwrGame.GetLayer(idx);
                LayerEntry layerEntry = layer.Entry;

                ExtRect rt = ExtRect.Create(layerEntry.MSX, layerEntry.MSY, layerEntry.MSX + (layerEntry.W << 5), layerEntry.MSY + layerEntry.H * 30);

                if (rt.Contains(aX, aY)) {
                    int xx = (aX - layerEntry.MSX) / 32;
                    int yy = (aY - layerEntry.MSY) / 30;

                    NWField fld = layer.GetField(xx, yy);
                    if (fld != null) {
                        SearchResult result = new SearchResult();
                        result.LID = layerEntry.GUID;
                        result.FieldX = xx;
                        result.FieldY = yy;
                        return result;
                    }
                }
            }

            return null;
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            base.DoMouseDownEvent(eventArgs);

            if (eventArgs.Button == MouseButton.mbLeft && (GlobalVars.Debug_Divinity || GlobalVars.Debug_DevMode)) {
                SearchResult res = SearchMapLocation(eventArgs.X, eventArgs.Y);
                if (res != null) {
                    Player player = GlobalVars.nwrGame.Player;
                    player.TransferTo(res.LID, res.FieldX, res.FieldY, -1, -1, StaticData.MapArea, true, true);
                    player.CurrentField.Research(false, (BaseTile.TS_SEEN | BaseTile.TS_VISITED));
                    Hide();
                }
            }
        }

        protected override void DoMouseMoveEvent(MouseMoveEventArgs eventArgs)
        {
            base.DoMouseMoveEvent(eventArgs);

            fMapHint = "";
            SearchResult res = SearchMapLocation(eventArgs.X, eventArgs.Y);
            if (res != null) {
                LayerEntry eLayer = (LayerEntry)GlobalVars.nwrDB.GetEntry(res.LID);
                NWField fld = GlobalVars.nwrGame.GetField(res.LID, res.FieldX, res.FieldY);
                fMapHint = eLayer.Name;
                if (fld.Visited) {
                    string Land = fld.LandName;
                    if (fMapHint.CompareTo(Land) != 0 && Land.CompareTo("") != 0) {
                        fMapHint = fMapHint + " - " + Land;
                    }
                } else {
                    fMapHint = "";
                }
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            base.DoPaintEvent(screen);

            int ax = 8;
            int ay = 8;
            screen.DrawImage(ax, ay, 0, 0, (int)fImage.Width, (int)fImage.Height, fImage, 255);

            screen.Font = CtlCommon.BgFont;
            screen.DrawText(ax + 40, ay + 25, BaseLocale.GetStr(RS.rs_WorldsTree), 0);
            screen.Font = CtlCommon.SmFont;

            int num = GlobalVars.nwrGame.LayersCount;
            for (int i = 0; i < num; i++) {
                NWLayer layer = GlobalVars.nwrGame.GetLayer(i);
                LayerEntry lre = layer.Entry;

                for (int y = 0; y < layer.H; y++) {
                    for (int x = 0; x < layer.W; x++) {
                        if (layer.GetField(x, y).Visited) {
                            GlobalVars.nwrWin.Resources.DrawImage(screen,
                                ax + lre.MSX + (x << 5), ay + lre.MSY + y * 30,
                                lre.IconsIndex + (y * lre.W + x), 255);
                        }
                    }
                }
            }

            Player player = GlobalVars.nwrGame.Player;
            LayerEntry layerEntry = ((LayerEntry)GlobalVars.nwrDB.GetEntry(player.LayerID));

            if (fMapCursor) {
                ExtPoint f = player.CurrentField.Coords;
                GlobalVars.nwrWin.Resources.DrawImage(screen, 
                    ax + layerEntry.MSX + (f.X << 5), ay + layerEntry.MSY + f.Y * 30,
                    StaticData.dbItfElements[(int)ItfElement.id_Cursor].ImageIndex, 255);
            }

            if (!string.IsNullOrEmpty(fMapHint)) {
                int tw = CtlCommon.SmFont.GetTextWidth(fMapHint);
                CtlCommon.SmFont.Color = Colors.Navy;

                //screen.drawText(ax + 304 + ((288 - tw) / 2), ay + 410, this.fMapHint, 0);
                screen.DrawText(ax + 58 + ((582 - tw) / 2), ay + 445, fMapHint, 0);
            }
        }
    }
}
