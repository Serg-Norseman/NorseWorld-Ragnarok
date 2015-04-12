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
package nwr.core.types;

import java.util.HashMap;
import jzrlib.core.IExtEnum;
import nwr.core.RS;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum Service implements IExtEnum
{
    ds_Teach(1, RS.rs_Teach, RS.rs_Teach_Question),
    ds_Trade(2, RS.rs_Trade, RS.rs_Trade_Question),
    ds_Exchange(3, RS.rs_Exchange, RS.rs_Exchange_Question),
    ds_Recruit(4, RS.rs_Recruit, RS.rs_Recruit_Question);

    public static final int ds_First = 1;
    public static final int ds_Last = 4;

    private final int fValue;
    public final int NameRS;
    public final int QuestionRS;

    private Service(int value, int nameRS, int questionRS)
    {
        this.fValue = value;
        this.NameRS = nameRS;
        this.QuestionRS = questionRS;
        getMappings().put(value, this);
    }

    @Override
    public int getValue()
    {
        return this.fValue;
    }

    private static HashMap<Integer, Service> mappings;

    private static HashMap<Integer, Service> getMappings()
    {
        synchronized (Service.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    public static Service forValue(int value)
    {
        return getMappings().get(value);
    }
}
