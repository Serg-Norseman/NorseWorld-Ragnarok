/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using System;
using System.IO;
using System.Reflection;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.Game
{
    public sealed class NWResourceManager : ResourceManager
    {
        private const string RES_FOLDER = "resources/";
        private const bool DEBUG_FILES = false;

        public static string GetAppPath()
        {
            Module[] mods = Assembly.GetExecutingAssembly().GetModules();
            string fn = mods[0].FullyQualifiedName;
            return Path.GetDirectoryName(fn) + Path.DirectorySeparatorChar;
        }

        private static Archive FArchive = null;

        public static void Load()
        {
            fInstance = new NWResourceManager();

            string appPath = GetAppPath();
            string arc = appPath + "Ragnarok.rfa";

            if (File.Exists(arc)) {
                FArchive = new Archive(arc);
                FArchive.Unpack();
            } else {
                FArchive = null;
            }
        }

        public static void Close()
        {

        }

        public static bool HasStream(string fileName)
        {
            try {
                if (FArchive == null) {
                    string resPath = GetAppPath() + RES_FOLDER + fileName;
                    return (File.Exists(resPath));
                } else {
                    return FArchive.IsExist(RES_FOLDER + fileName);
                }
            } catch (Exception ex) {
                if (DEBUG_FILES) {
                    Logger.Write("ResourceManager.hasStream(" + fileName + "): " + ex.Message);
                }
                return false;
            }
        }

        protected override Stream LoadStreamInternal(string fileName)
        {
            try {
                if (FArchive == null) {
                    string resPath = GetAppPath() + RES_FOLDER + fileName;
                    return new FileStream(resPath, FileMode.Open); //AuxUtils.ConvertPath
                } else {
                    return FArchive.ExtractFile(RES_FOLDER + fileName);
                }
            } catch (IOException ex) {
                if (DEBUG_FILES) {
                    Logger.Write("ResourceManager.loadStream(" + fileName + "): " + ex.Message);
                }
                return null;
            }
        }

        public static BaseImage LoadImage(BaseScreen screen, string fileName, int transColor)
        {
            BaseImage result = null;

            try {
                BaseImage image = screen.CreateImage();
                Stream stm = LoadStream(fileName);
                if (stm != null) {
                    try {
                        image.LoadFromStream(stm, transColor);
                        result = image;
                    } finally {
                        stm.Close();
                    }
                }
            } catch (IOException ex) {
                Logger.Write("ResourceManager.loadImage(" + fileName + "): " + ex.Message);
            }

            return result;
        }
    }
}
