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
package nwr.game.ghosts;

import java.io.IOException;
import jzrlib.core.FileVersion;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.Logger;
import nwr.creatures.NWCreature;
import nwr.game.NWGameSpace;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public final class Ghost extends NWCreature
{
    public Ghost(NWGameSpace space, Object owner)
    {
        super(space, owner);
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        try {
            super.loadFromStream(stream, version);
        } catch (Exception ex) {
            Logger.write("Ghost.loadFromStream(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        try {
            super.saveToStream(stream, version);
        } catch (Exception ex) {
            Logger.write("Ghost.saveToStream(): " + ex.getMessage());
            throw ex;
        }
    }
}
