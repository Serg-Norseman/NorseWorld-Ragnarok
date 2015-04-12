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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.external.BinaryInputStream;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class ResourceManager
{
    private static final String RES_FOLDER = "resources/";
    private static final boolean DEBUG_FILES = false;

    public static String getExecPath()
    {
        String applicationDir = ResourceManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        String result = applicationDir;
        if (result.endsWith("classes/")) {
            //result = result + "../../../";
        } else if (result.endsWith("/Ragnarok.jar")) {
            result = result.substring(0, result.indexOf("/Ragnarok.jar"));
        }

        File file = new File(result);
        try {
            result = file.getCanonicalPath() + "\\";
        } catch (IOException ex) {
        }

        return result;
    }

    public static String getAppPath()
    {
        String applicationDir = ResourceManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        String result = applicationDir;
        if (result.endsWith("classes/")) {
            result = result + "../../../";
        } else if (result.endsWith("/Ragnarok.jar")) {
            result = result.substring(0, result.indexOf("/Ragnarok.jar"));
        }

        File file = new File(result);
        try {
            result = file.getCanonicalPath() + "\\";
        } catch (IOException ex) {
        }

        return result;
    }

    private static Archive fArchive = null;

    public static void load()
    {
        String appPath = ResourceManager.getAppPath();
        String arc = appPath + "Ragnarok.rfa";

        if ((new java.io.File(arc)).isFile()) {
            fArchive = new Archive(arc);
            fArchive.unpack();
        } else {
            fArchive = null;
        }
    }

    public static void close()
    {
        
    }

    public static boolean hasStream(String fileName)
    {
        try {
            if (fArchive == null) {
                String resPath = getAppPath() + RES_FOLDER + fileName;
                return ((new java.io.File(resPath)).isFile());
            } else {
                return fArchive.isExist(RES_FOLDER + fileName);
            }
        } catch (Exception ex) {
            if (DEBUG_FILES) {
                Logger.write("ResourceManager.hasStream(" + fileName + "): " + ex.getMessage());
            }
            return false;
        }
    }

    public static InputStream loadStream(String fileName)
    {
        try {
            if (fArchive == null) {
                String resPath = getAppPath() + RES_FOLDER + fileName;
                return new FileInputStream(/*AuxUtils.ConvertPath*/(resPath));
            } else {
                return fArchive.extractFile(RES_FOLDER + fileName);
            }
        } catch (IOException ex) {
            if (DEBUG_FILES) {
                Logger.write("ResourceManager.loadStream(" + fileName + "): " + ex.getMessage());
            }
            return null;
        }
    }

    public static BaseImage loadImage(BaseScreen screen, String fileName, int transColor)
    {
        BaseImage result = null;

        try {
            BaseImage image = screen.createImage();
            InputStream stm = ResourceManager.loadStream(fileName);

            if (stm != null) {
                try (BinaryInputStream bis = new BinaryInputStream(stm, AuxUtils.binEndian)) {
                    image.loadFromStream(bis, transColor);
                    result = image;
                } finally {
                    stm.close();
                }
            }
        } catch (IOException ex) {
            Logger.write("ResourceManager.loadImage(" + fileName + "): " + ex.getMessage());
        }

        return result;
    }
}
