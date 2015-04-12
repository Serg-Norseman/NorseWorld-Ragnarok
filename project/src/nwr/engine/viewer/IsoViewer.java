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

import jzrlib.map.BaseTile;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import nwr.engine.BaseScreen;
import jzrlib.map.AbstractMap;
import jzrlib.map.BaseTile;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class IsoViewer extends MapViewer
{
    private int FTH;
    private int FTHd2;
    private int FTW;
    private int FTWd2;

    public IsoViewer(AbstractMap map)
    {
        super(map);
    }

    @Override
    public void setTileHeight(int value)
    {
        super.setTileHeight(value);
        this.FTH = value;

        if ((this.FTH & 1) != 0) {
            this.FTH--;
        }

        this.FTHd2 = (int) (this.FTH >>> 1);
    }

    @Override
    public void setTileWidth(int value)
    {
        super.setTileWidth(value);
        this.FTW = value;
        this.FTWd2 = (int) (this.FTW >>> 1);
    }

    @Override
    public void BufferPaint(BaseScreen screen, int destX, int destY)
    {
        screen.initClip(new Rect(destX, destY, destX + super.getWidth(), destY + super.getHeight()));

        if (this.OnBeforePaint != null) {
            this.OnBeforePaint.invoke(this, screen);
        }

        screen.fillRect(new Rect(destX, destY, destX + super.getWidth(), destY + super.getHeight()), BaseScreen.clBlack);

        if (this.OnTilePaint != null) {
            int isoWidth = super.Map.getWidth() * super.getTileHeight() + super.Map.getHeight() * super.getTileHeight();
            int isoHeight = super.Map.getWidth() * this.FTHd2 + super.Map.getHeight() * this.FTHd2;
            if (isoWidth < super.getWidth()) {
                super.OffsetX = (int) ((super.getWidth() - isoWidth) >> 1) + (super.Map.getHeight() - 1) * super.getTileHeight();
            }
            if (isoHeight < super.getHeight()) {
                super.OffsetY = (int) (super.getHeight() - isoHeight) >> 1;
            }

            Point centerTile = this.TileByMouse(this.fWidth >> 1, this.fHeight >> 1);
            int dx = this.fWidth / this.fTileWidth;
            int dy = this.fHeight / this.fTileHeight;
            int d = Math.max(dx, dy) + 1;
            Point scrPt = new Point();

            for (int y = centerTile.Y - d; y <= centerTile.Y + d; y++) {
                for (int x = centerTile.X - d; x <= centerTile.X + d; x++) {
                    BaseTile tile = super.Map.getTile(x, y);
                    if (tile != null) {
                        this.TileCoords(x, y, scrPt);
                        int ax = scrPt.X;
                        int ay = scrPt.Y;

                        if (ax > -super.getTileWidth() && ax < super.getWidth() && ay > -super.getTileHeight() && ay < super.getHeight()) {
                            Rect R = new Rect();
                            R.Left = ax + destX;
                            R.Top = ay + destY;
                            R.Right = R.Left + super.getTileWidth();
                            R.Bottom = R.Top + super.getTileHeight();

                            this.OnTilePaint.invoke(this, x, y, tile, R, screen);

                            /*if (super.ShowGrid) {
                                screen.drawLine(R.Left, R.Top + this.FTHd2, R.Left + this.FTWd2, R.Top, BaseScreen.clRed);
                                screen.drawLine(R.Left + this.FTWd2, R.Top, R.Right, R.Top + this.FTHd2, BaseScreen.clRed);
                                screen.drawLine(R.Right, R.Top + this.FTHd2, R.Left + this.FTWd2, R.Bottom, BaseScreen.clRed);
                                screen.drawLine(R.Left + this.FTWd2, R.Bottom, R.Left, R.Top + this.FTHd2, BaseScreen.clRed);
                            }

                            if (super.ShowCursor && x == super.CurrentTile.X && y == super.CurrentTile.Y) {
                                screen.drawLine(R.Left, R.Top + this.FTHd2, R.Left + this.FTWd2, R.Top, BaseScreen.clWhite);
                                screen.drawLine(R.Left + this.FTWd2, R.Top, R.Right, R.Top + this.FTHd2, BaseScreen.clWhite);
                                screen.drawLine(R.Right, R.Top + this.FTHd2, R.Left + this.FTWd2, R.Bottom, BaseScreen.clWhite);
                                screen.drawLine(R.Left + this.FTWd2, R.Bottom, R.Left, R.Top + this.FTHd2, BaseScreen.clWhite);
                            }*/
                        }
                    }
                }
            }

            if (this.OnAfterPaint != null) {
                this.OnAfterPaint.invoke(this, screen);
            }

            screen.doneClip();
        }
    }

    @Override
    public void CenterByTile(int tileX, int tileY)
    {
        Point scrPt = new Point();
        this.TileCoords(tileX, tileY, scrPt);
        int oldX = scrPt.X;
        int oldY = scrPt.Y;

        super.OffsetX = (super.getWidth() >> 1) - (oldX - super.OffsetX) - this.FTH;/*super.getTileHeight()*/;
        super.OffsetY = (super.getHeight() >> 1) - (oldY - super.OffsetY) - this.FTHd2;
    }

    @Override
    public Point TileByMouse(int mX, int mY)
    {
        int xo = mX - (super.OffsetX + this.FTH);
        int yo = mY - (super.OffsetY + this.FTHd2);
        int xx = (int) (Math.round(((float) yo + xo / 2.0f) / (float) this.FTH));
        int yy = (int) (Math.round(((float) yo - xo / 2.0f) / (float) this.FTH));
        return new Point(xx, yy);
    }

    @Override
    public void TileCoords(int x, int y, Point scrPoint)
    {
        scrPoint.X = super.OffsetX + (x - y) * this.FTH;
        scrPoint.Y = super.OffsetY + (x + y) * this.FTHd2;
    }
}
