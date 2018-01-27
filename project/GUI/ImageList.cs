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
using BSLib;
using NWR.Game;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class ImageSection
    {
        public int ImageIndex;
        public int Left;
        public int Top;
        public int Height;
        public int Width;
    }

    public sealed class ImageList : BaseObject
    {
        private readonly BaseScreen fScreen;

        private List<BaseImage> fImages;
        private List<ImageSection> fSections;

        public ImageList(BaseScreen screen)
        {
            fScreen = screen;
            fImages = new List<BaseImage>();
            fSections = new List<ImageSection>();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                Clear();
                fImages = null;
                fSections = null;
            }
            base.Dispose(disposing);
        }

        private int AddSection(int imageIndex, int aX, int aY, int aWidth, int aHeight)
        {
            ImageSection sect = new ImageSection();
            sect.ImageIndex = imageIndex;
            sect.Left = aX;
            sect.Top = aY;
            sect.Height = aHeight;
            sect.Width = aWidth;

            int result = fSections.Count;
            fSections.Add(sect);
            return result;
        }

        public BaseImage GetImage(int index)
        {
            BaseImage result = null;
            if (index >= 0 && index < fImages.Count) {
                result = fImages[index];
            }
            return result;
        }

        public ImageSection GetSection(int index)
        {
            ImageSection result = null;
            if (index >= 0 && index < fSections.Count) {
                result = fSections[index];
            }
            return result;
        }

        public int ImagesCount
        {
            get {
                return fImages.Count;
            }
        }

        public int SectionsCount
        {
            get {
                return fSections.Count;
            }
        }

        public int AddImage(string fileName, int transColor, bool transDefault)
        {
            try {
                BaseImage img = NWResourceManager.LoadImage(fScreen, fileName, transColor);
                if (img != null) {
                    if (transDefault) {
                        img.SetTransDefault();
                    }

                    int result = fImages.Count;
                    fImages.Add(img);

                    return result;
                } else {
                    return -1;
                }
            } catch (Exception ex) {
                Logger.Write("ImageList.addImage(" + fileName + "): " + ex.Message);
                return -1;
            }
        }

        public void AddImageSet(string fileName, int transColor, int segWidth, int segHeight, int divider)
        {
            int imageIndex = AddImage(fileName, transColor, false);
            BaseImage image = fImages[imageIndex];

            int sx = image.Width / (divider + segWidth);
            int sy = image.Height / (divider + segHeight);

            for (int ty = 0; ty <= sy - 1; ty++) {
                for (int tx = 0; tx <= sx - 1; tx++) {
                    int xx = divider * (tx + 1) + segWidth * tx;
                    int yy = divider * (ty + 1) + segHeight * ty;
                    AddSection(imageIndex, xx, yy, segWidth, segHeight);
                }
            }
        }

        public void Clear()
        {
            int num = fImages.Count;
            for (int i = 0; i < num; i++) {
                fImages[i].Dispose();
            }
            fImages.Clear();
            fImages = null;

            fSections.Clear();
            fSections = null;
        }

        public void DrawImage(BaseScreen screen, int aX, int aY, int index, int opacity)
        {
            if (index >= 0 && index < fImages.Count) {
                BaseImage img = fImages[index];
                screen.DrawImage(aX, aY, 0, 0, img.Width, img.Height, img, opacity);
            }
        }

        public void DrawSection(BaseScreen screen, int aX, int aY, int index, int opacity)
        {
            if (index >= 0 && index < fSections.Count) {
                ImageSection sec = fSections[index];
                if (sec.ImageIndex >= 0 && sec.ImageIndex < fImages.Count) {
                    BaseImage img = fImages[sec.ImageIndex];
                    screen.DrawImage(aX, aY, sec.Left, sec.Top, sec.Width, sec.Height, img, opacity);
                }
            }
        }
    }
}
