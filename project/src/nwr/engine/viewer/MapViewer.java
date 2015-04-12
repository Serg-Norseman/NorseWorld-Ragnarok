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
package nwr.engine.viewer;

import jzrlib.core.Point;
import jzrlib.core.Rect;
import nwr.engine.BaseScreen;
import jzrlib.map.AbstractMap;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class MapViewer
{
    protected Rect fClientRect;
    protected int fHeight;
    protected int fTileHeight;
    protected int fTileWidth;
    protected int fWidth;

    public Point CurrentTile;
    public AbstractMap Map;
    public int OffsetX;
    public int OffsetY;
    public boolean ShowCursor;
    public boolean ShowGrid;

    public IAfterPaintEvent OnAfterPaint;
    public IBeforePaintEvent OnBeforePaint;
    public ITilePaintEvent OnTilePaint;
    
    public final Rect getClientRect()
    {
        return this.fClientRect;
    }

    public final int getHeight()
    {
        return this.fHeight;
    }

    public final int getTileHeight()
    {
        return this.fTileHeight;
    }

    public final int getTileWidth()
    {
        return this.fTileWidth;
    }

    public final int getWidth()
    {
        return this.fWidth;
    }

    public void setTileHeight(int Value)
    {
        this.fTileHeight = Value;
    }

    public void setTileWidth(int Value)
    {
        this.fTileWidth = Value;
    }

    protected final boolean RectIsVisible(Rect rect)
    {
        boolean result = false;
        if (this.fClientRect.contains(rect.Left, rect.Top) || this.fClientRect.contains(rect.Left, rect.Bottom) || this.fClientRect.contains(rect.Right, rect.Top) || this.fClientRect.contains(rect.Right, rect.Bottom)) {
            result = true;
        }
        return result;
    }

    public MapViewer(AbstractMap map)
    {
        this.Map = map;
        this.OffsetX = 0;
        this.OffsetY = 0;
    }

    public abstract void BufferPaint(BaseScreen screen, int DestX, int DestY);

    public abstract void CenterByTile(int tileX, int tileY);

    public abstract Point TileByMouse(int mX, int mY);

    public abstract void TileCoords(int x, int y, Point scrPoint);

    public final void BufferResize(Rect aClientRect)
    {
        this.fClientRect = aClientRect;
        this.fWidth = aClientRect.Right - aClientRect.Left;
        this.fHeight = aClientRect.Bottom - aClientRect.Top;
    }
}
