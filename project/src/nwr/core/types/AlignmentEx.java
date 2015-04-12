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

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum AlignmentEx
{
    am_None(0),

    am_Lawful_Good(17),
    am_Lawful_Neutral(18),
    am_Lawful_Evil(19),

    am_Neutral_Good(33),
    am_Neutral(34),
    am_Neutral_Evil(35),
    
    am_Chaotic_Good(49),
    am_Chaotic_Neutral(50),
    am_Chaotic_Evil(51);

    public static final int am_Mask_Good = 1;
    public static final int am_Mask_GENeutral = 2;
    public static final int am_Mask_Evil = 3;

    public static final int am_Mask_Lawful = 1;
    public static final int am_Mask_LCNeutral = 2;
    public static final int am_Mask_Chaotic = 3;

    private final int intValue;
    private static HashMap<Integer, AlignmentEx> mappings;

    private static HashMap<Integer, AlignmentEx> getMappings()
    {
        synchronized (AlignmentEx.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private AlignmentEx(int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public final int getValue()
    {
        return intValue;
    }

    public static AlignmentEx forValue(int value)
    {
        return getMappings().get(value);
    }

    public static int getGE(AlignmentEx alignment)
    {
        return (alignment.getValue() & 0xF);
    }

    public static int getLC(AlignmentEx alignment)
    {
        return (alignment.getValue() >> 4 & 0xF);
    }

    public static AlignmentEx genAlignment(int lc, int ge)
    {
        return AlignmentEx.forValue((ge) | (lc) << 4);
    }

    public static AlignmentEx getOppositeAlignment(AlignmentEx anAlignment, boolean total)
    {
        int ge = AlignmentEx.getGE(anAlignment);
        int lc = AlignmentEx.getLC(anAlignment);

        int oge = AlignmentEx.am_Mask_GENeutral;
        if (ge != AlignmentEx.am_Mask_Good) {
            if (ge != AlignmentEx.am_Mask_GENeutral) {
                if (ge == AlignmentEx.am_Mask_Evil) {
                    oge = AlignmentEx.am_Mask_Good;
                }
            } else {
                oge = ge;
            }
        } else {
            oge = AlignmentEx.am_Mask_Evil;
        }

        int olc = AlignmentEx.am_Mask_GENeutral;
        if (!total) {
            olc = lc;
        } else {
            if (lc != AlignmentEx.am_Mask_Good) {
                if (lc != AlignmentEx.am_Mask_GENeutral) {
                    if (lc == AlignmentEx.am_Mask_Evil) {
                        olc = AlignmentEx.am_Mask_Good;
                    }
                } else {
                    olc = lc;
                }
            } else {
                olc = AlignmentEx.am_Mask_Evil;
            }
        }

        return AlignmentEx.genAlignment(olc, oge);
    }

    public final AlignmentEx invert()
    {
        int ge = AlignmentEx.getGE(this);
        int ce = AlignmentEx.getLC(this);

        if (ge != AlignmentEx.am_Mask_Good) {
            if (ge != AlignmentEx.am_Mask_GENeutral) {
                if (ge == AlignmentEx.am_Mask_Evil) {
                    ge = AlignmentEx.am_Mask_Good;
                }
            } else {
            }
        } else {
            ge = AlignmentEx.am_Mask_Evil;
        }

        return AlignmentEx.genAlignment(ce, ge);
    }
}
