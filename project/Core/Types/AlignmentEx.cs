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
namespace NWR.Core.Types
{
    public enum Alignment
    {
        am_None = 0,

        am_Lawful_Good = 17,
        am_Lawful_Neutral = 18,
        am_Lawful_Evil = 19,

        am_Neutral_Good = 33,
        am_Neutral = 34,
        am_Neutral_Evil = 35,

        am_Chaotic_Good = 49,
        am_Chaotic_Neutral = 50,
        am_Chaotic_Evil = 51
    }

    public static class AlignmentEx
    {
        public const byte am_Mask_Good = 1;
        public const byte am_Mask_GENeutral = 2;
        public const byte am_Mask_Evil = 3;

        public const byte am_Mask_Lawful = 1;
        public const byte am_Mask_LCNeutral = 2;
        public const byte am_Mask_Chaotic = 3;

        public static byte GetGE(this Alignment alignment)
        {
            return (byte)((byte)alignment & 0xF);
        }

        public static byte GetLC(this Alignment alignment)
        {
            return (byte)((byte)alignment >> 4 & 0xF);
        }

        public static Alignment GenAlignment(byte lc, byte ge)
        {
            return (Alignment)((ge) | (lc) << 4);
        }

        public static Alignment GetOppositeAlignment(Alignment alignment, bool total)
        {
            byte ge = alignment.GetGE();
            byte lc = alignment.GetLC();

            byte oge = am_Mask_GENeutral;
            if (ge != am_Mask_Good)
            {
                if (ge != am_Mask_GENeutral)
                {
                    if (ge == am_Mask_Evil)
                    {
                        oge = am_Mask_Good;
                    }
                }
                else
                {
                    oge = ge;
                }
            }
            else
            {
                oge = am_Mask_Evil;
            }

            byte olc = am_Mask_GENeutral;
            if (!total)
            {
                olc = lc;
            }
            else
            {
                if (lc != am_Mask_Good)
                {
                    if (lc != am_Mask_GENeutral)
                    {
                        if (lc == am_Mask_Evil)
                        {
                            olc = am_Mask_Good;
                        }
                    }
                    else
                    {
                        olc = lc;
                    }
                }
                else
                {
                    olc = am_Mask_Evil;
                }
            }

            return GenAlignment(olc, oge);
        }

        public static Alignment Invert(this Alignment instance)
        {
            byte ge = instance.GetGE();
            byte ce = instance.GetLC();

            if (ge != am_Mask_Good) {
                if (ge != am_Mask_GENeutral) {
                    if (ge == am_Mask_Evil) {
                        ge = am_Mask_Good;
                    }
                } else {
                }
            } else {
                ge = am_Mask_Evil;
            }

            return GenAlignment(ce, ge);
        }
    }
}