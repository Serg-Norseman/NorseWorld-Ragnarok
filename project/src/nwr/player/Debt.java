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
package nwr.player;

import java.io.IOException;
import jzrlib.common.CreatureSex;
import jzrlib.core.FileVersion;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.StreamUtils;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import jzrlib.grammar.Case;
import jzrlib.grammar.Number;
import nwr.main.GlobalVars;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Debt extends MemoryEntry
{
    public String Lender;
    public int Value;

    public Debt(Object owner)
    {
        super(owner);
    }

    private String getTitle()
    {
        String s;
        if (GlobalVars.nwrWin.getLangExt().compareTo("en") == 0) {
            s = " to ";
        } else {
            s = " ";
        }
        return Locale.getStr(RS.rs_Debt) + s + StaticData.morphCompNoun(this.Lender, Case.cDative, Number.nSingle, CreatureSex.csMale, true, false);
    }

    @Override
    public String getDesc()
    {
        return this.getTitle() + ": " + String.valueOf(this.Value) + "$.";
    }

    @Override
    public String getName()
    {
        return this.getTitle();
    }

    @Override
    public byte getSerializeKind()
    {
        return StaticData.SID_DEBT;
    }

    @Override
    public void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.Lender = StreamUtils.readString(stream);
        this.Value = StreamUtils.readInt(stream);

        super.Sign = "Debt_" + this.Lender;
    }

    @Override
    public void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        StreamUtils.writeString(stream, this.Lender);
        StreamUtils.writeInt(stream, this.Value);
    }
}
