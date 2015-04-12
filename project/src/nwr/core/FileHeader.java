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
package nwr.core;

import java.io.IOException;
import jzrlib.core.FileVersion;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.StreamUtils;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class FileHeader implements Cloneable
{
    public char[] Sign;
    public FileVersion Version;

    public FileHeader()
    {
        this.Sign = new char[3];
    }

    public FileHeader(char[] sign, FileVersion version)
    {
        this.Sign = sign.clone();
        this.Version = version;
    }

    @Override
    public FileHeader clone()
    {
        FileHeader varCopy = new FileHeader(this.Sign, this.Version);
        return varCopy;
    }

    public final void read(BinaryInputStream S) throws IOException
    {
        byte[] signBuffer = new byte[3];
        for (int i = 0; i <= 2; i++) {
            signBuffer[i] = (byte) StreamUtils.readByte(S);
        }
        String str = new String(signBuffer, "Cp1251");
        for (int i = 0; i <= 2; i++) {
            this.Sign[i] = str.charAt(i);
        }

        this.Version = StreamUtils.readFileVersion(S);
    }

    public final void write(BinaryOutputStream S) throws IOException
    {
        String str = new String(this.Sign);
        byte[] signBuffer = str.getBytes("cp1251");
        for (int i = 0; i <= 2; i++) {
            StreamUtils.writeByte(S, signBuffer[i]);
        }

        StreamUtils.writeFileVersion(S, this.Version);
    }
}
