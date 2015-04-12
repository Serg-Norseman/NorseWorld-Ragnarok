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

import jzrlib.utils.AuxUtils;
import jzrlib.core.BaseObject;
import jzrlib.external.BinaryInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class BaseImage extends BaseObject
{
    protected BaseScreen fScreen;

    public short Height;
    public int TransColor;
    public short Width;
    public boolean PaletteMode;

    protected void Done()
    {
    }

    public BaseImage(BaseScreen screen)
    {
        this.fScreen = screen;
    }

    public BaseImage(BaseScreen screen, String fileName, int transColor)
    {
        this.fScreen = screen;
        this.loadFromFile(fileName, transColor);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.Done();
        }
        super.dispose(disposing);
    }

    public final void loadFromFile(String fileName, int transColor)
    {
        //fileName = AuxUtils.ConvertPath(fileName);
        if ((new java.io.File(fileName)).isFile()) {
            try {
                FileInputStream stream = new FileInputStream(fileName);
                BinaryInputStream dis = new BinaryInputStream(stream, AuxUtils.binEndian);
                try {
                    this.loadFromStream(dis, transColor);
                } finally {
                    dis.close();
                    stream.close();
                }
            } catch (IOException ex) {
                throw new RuntimeException("BaseImage.loadFromFile(" + fileName + "): " + ex.getMessage());
            }
        }
    }

    public void loadFromStream(BinaryInputStream stream, int transColor)
    {
    }

    public void setTransDefault()
    {
    }

    public void replaceColor(int index, int replacement)
    {
    }
}
