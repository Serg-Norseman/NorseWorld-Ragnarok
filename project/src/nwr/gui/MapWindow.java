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
package nwr.gui;

import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.map.TileStates;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.ItfElement;
import nwr.database.LayerEntry;
import nwr.engine.BaseControl;
import nwr.engine.BaseImage;
import nwr.engine.BaseScreen;
import nwr.engine.MouseButton;
import nwr.engine.MouseEventArgs;
import nwr.engine.MouseMoveEventArgs;
import nwr.engine.ResourceManager;
import nwr.engine.WindowStyles;
import nwr.gui.controls.CtlCommon;
import nwr.main.GlobalVars;
import nwr.player.Player;
import nwr.universe.NWField;
import nwr.universe.NWLayer;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class MapWindow extends NWWindow
{
    private final BaseImage fImage;
    private boolean fMapCursor;
    private String fMapHint;
    private long fPrevTime;

    public MapWindow(BaseControl owner)
    {
        super(owner);
        super.setFont(CtlCommon.smFont);
        super.setWidth(656);
        super.setHeight(496);
        super.WindowStyle = new WindowStyles(WindowStyles.wsScreenCenter, WindowStyles.wsModal, WindowStyles.wsKeyPreview);
        super.BackDraw = false;
        super.Shifted = false;

        this.fImage = ResourceManager.loadImage(super.getMainWindow().getScreen(), "itf/MapBack.tga", BaseScreen.clNone);
        this.fMapHint = "";
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (fImage != null) {
                this.fImage.dispose();
            }
        }
        super.dispose(disposing);
    }

    @Override
    public void update(long time)
    {
        super.update(time);

        // timer default = 500
        if (time - fPrevTime >= 500 && this.getVisible()) {
            this.fMapCursor = !this.fMapCursor;
            this.fPrevTime = time;
        }
    }

    private static class SearchResult
    {
        int LID;
        int FieldX;
        int FieldY;
    }

    private SearchResult searchMapLocation(int aX, int aY)
    {
        aX -= 8;
        aY -= 8;

        int num = GlobalVars.nwrGame.getLayersCount();
        for (int idx = 0; idx < num; idx++) {
            NWLayer layer = GlobalVars.nwrGame.getLayer(idx);
            LayerEntry layerEntry = layer.getEntry();

            Rect rt = new Rect(layerEntry.MSX, layerEntry.MSY, layerEntry.MSX + (layerEntry.W << 5), layerEntry.MSY + layerEntry.H * 30);

            if (rt.contains(aX, aY)) {
                int xx = (aX - layerEntry.MSX) / 32;
                int yy = (aY - layerEntry.MSY) / 30;

                NWField fld = layer.getField(xx, yy);
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

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        super.doMouseDownEvent(eventArgs);

        if (eventArgs.Button == MouseButton.mbLeft && (GlobalVars.Debug_Divinity || GlobalVars.Debug_DevMode)) {
            SearchResult res = this.searchMapLocation(eventArgs.X, eventArgs.Y);
            if (res != null) {
                Player player = GlobalVars.nwrGame.getPlayer();
                player.transferTo(res.LID, res.FieldX, res.FieldY, -1, -1, StaticData.MapArea, true, true);
                player.getCurrentField().research(false, TileStates.include(TileStates.TS_SEEN, TileStates.TS_VISITED));
                this.hide();
            }
        }
    }

    @Override
    protected void doMouseMoveEvent(MouseMoveEventArgs eventArgs)
    {
        super.doMouseMoveEvent(eventArgs);

        this.fMapHint = "";

        SearchResult res = this.searchMapLocation(eventArgs.X, eventArgs.Y);
        if (res != null) {
            LayerEntry eLayer = (LayerEntry) GlobalVars.nwrBase.getEntry(res.LID);
            NWField fld = GlobalVars.nwrGame.getField(res.LID, res.FieldX, res.FieldY);
            this.fMapHint = eLayer.getName();
            if (fld.Visited) {
                String Land = fld.getLandName();
                if (this.fMapHint.compareTo(Land) != 0 && Land.compareTo("") != 0) {
                    this.fMapHint = this.fMapHint + " - " + Land;
                }
            } else {
                this.fMapHint = "";
            }
        }
    }

    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        super.doPaintEvent(screen);

        int ax = 8;
        int ay = 8;
        screen.drawImage(ax, ay, 0, 0, (int) this.fImage.Width, (int) this.fImage.Height, this.fImage, 255);

        screen.Font = CtlCommon.bgFont;
        screen.drawText(ax + 40, ay + 25, Locale.getStr(RS.rs_WorldsTree), 0);
        screen.Font = CtlCommon.smFont;

        int num = GlobalVars.nwrGame.getLayersCount();
        for (int i = 0; i < num; i++) {
            NWLayer layer = GlobalVars.nwrGame.getLayer(i);
            LayerEntry layerEntry = layer.getEntry();

            for (int y = 0; y < layer.getH(); y++) {
                for (int x = 0; x < layer.getW(); x++) {
                    if (layer.getField(x, y).Visited) {
                        GlobalVars.nwrWin.Resources.drawImage(screen, ax + layerEntry.MSX + (x << 5), ay + layerEntry.MSY + y * 30, layerEntry.IconsIndex + (y * layerEntry.W + x), 255);
                    }
                }
            }
        }

        Player player = GlobalVars.nwrGame.getPlayer();
        LayerEntry layerEntry = ((LayerEntry) GlobalVars.nwrBase.getEntry(player.LayerID));

        if (this.fMapCursor) {
            NWField fld = (NWField) player.getCurrentMap();
            Point f = fld.getCoords();
            GlobalVars.nwrWin.Resources.drawImage(screen, ax + layerEntry.MSX + (f.X << 5), ay + layerEntry.MSY + f.Y * 30, ItfElement.id_Cursor.ImageIndex, 255);
        }

        if (this.fMapHint.compareTo("") != 0) {
            int tw = CtlCommon.smFont.getTextWidth(this.fMapHint);
            CtlCommon.smFont.setColor(BaseScreen.clNavy);

            //screen.drawText(ax + 304 + ((288 - tw) / 2), ay + 410, this.fMapHint, 0);
            screen.drawText(ax + 58 + ((582 - tw) / 2), ay + 445, this.fMapHint, 0);
        }
    }
}
