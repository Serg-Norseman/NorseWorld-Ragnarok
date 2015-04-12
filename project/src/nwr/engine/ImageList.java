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
package nwr.engine;

import jzrlib.core.BaseObject;
import jzrlib.utils.Logger;
import java.util.ArrayList;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ImageList extends BaseObject
{
    private final BaseScreen fScreen;

    private ArrayList<BaseImage> fImages;
    private ArrayList<ImageSection> fSections;

    public ImageList(BaseScreen screen)
    {
        this.fScreen = screen;
        this.fImages = new ArrayList<>();
        this.fSections = new ArrayList<>();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.clear();
            this.fImages = null;
            this.fSections = null;
        }
        super.dispose(disposing);
    }

    private int addSection(int aImageIndex, int aX, int aY, int aWidth, int aHeight)
    {
        ImageSection sect = new ImageSection();
        sect.ImageIndex = aImageIndex;
        sect.Left = aX;
        sect.Top = aY;
        sect.Height = aHeight;
        sect.Width = aWidth;

        int result = this.fSections.size();
        this.fSections.add(sect);
        return result;
    }

    public final BaseImage getImage(int index)
    {
        BaseImage result = null;
        if (index >= 0 && index < this.fImages.size()) {
            result = this.fImages.get(index);
        }
        return result;
    }

    public final ImageSection getSection(int index)
    {
        ImageSection result = null;
        if (index >= 0 && index < this.fSections.size()) {
            result = this.fSections.get(index);
        }
        return result;
    }

    public final int getImagesCount()
    {
        return this.fImages.size();
    }

    public final int getSectionsCount()
    {
        return this.fSections.size();
    }

    public final int addImage(String fileName, int transColor, boolean transDefault)
    {
        try {
            BaseImage img = ResourceManager.loadImage(this.fScreen, fileName, transColor);
            if (img != null) {
                if (transDefault) {
                    img.setTransDefault();
                }

                int result = this.fImages.size();
                this.fImages.add(img);

                return result;
            } else {
                return -1;
            }
        } catch (Exception ex) {
            Logger.write("ImageList.addImage(" + fileName + "): " + ex.getMessage());
            return -1;
        }
    }

    public final void addImageSet(String fileName, int transColor, int segWidth, int segHeight, int divider)
    {
        int imageIndex = this.addImage(fileName, transColor, false);
        BaseImage image = this.fImages.get(imageIndex);

        int sx = (int) image.Width / (divider + segWidth);
        int sy = (int) image.Height / (divider + segHeight);

        for (int ty = 0; ty <= sy - 1; ty++) {
            for (int tx = 0; tx <= sx - 1; tx++) {
                int xx = divider * (tx + 1) + segWidth * tx;
                int yy = divider * (ty + 1) + segHeight * ty;
                this.addSection(imageIndex, xx, yy, segWidth, segHeight);
            }
        }
    }

    public final void clear()
    {
        int num = this.fImages.size();
        for (int i = 0; i < num; i++) {
            this.fImages.get(i).dispose();
        }
        this.fImages.clear();
        this.fImages = null;

        this.fSections.clear();
        this.fSections = null;
    }

    public final void drawImage(BaseScreen screen, int aX, int aY, int index, int opacity)
    {
        if (index >= 0 && index < this.fImages.size()) {
            BaseImage img = this.fImages.get(index);
            screen.drawImage(aX, aY, 0, 0, img.Width, img.Height, img, opacity);
        }
    }

    public final void drawSection(BaseScreen screen, int aX, int aY, int index, int opacity)
    {
        if (index >= 0 && index < this.fSections.size()) {
            ImageSection sec = this.fSections.get(index);
            if (sec.ImageIndex >= 0 && sec.ImageIndex < this.fImages.size()) {
                BaseImage img = this.fImages.get(sec.ImageIndex);
                screen.drawImage(aX, aY, sec.Left, sec.Top, sec.Width, sec.Height, img, opacity);
            }
        }
    }
}
