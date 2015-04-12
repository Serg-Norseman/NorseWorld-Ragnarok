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

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum Keys
{
    GK_UNK(0),
    GK_BACK(8),
    GK_TAB(9),
    GK_CLEAR(12),
    GK_RETURN(13),
    GK_ESCAPE(27),
    GK_SPACE(32),
    GK_ADD(43),
    GK_COMMA(44),
    GK_SUBTRACT(45),
    GK_PERIOD(46),
    GK_0(48),
    GK_1(49),
    GK_2(50),
    GK_3(51),
    GK_4(52),
    GK_5(53),
    GK_6(54),
    GK_7(55),
    GK_8(56),
    GK_9(57),
    GK_LESS(60),
    GK_GREATER(62),
    GK_TWIDDLE(96),
    GK_A(97),
    GK_B(98),
    GK_C(99),
    GK_D(100),
    GK_E(101),
    GK_F(102),
    GK_G(103),
    GK_H(104),
    GK_I(105),
    GK_J(106),
    GK_K(107),
    GK_L(108),
    GK_M(109),
    GK_N(110),
    GK_O(111),
    GK_P(112),
    GK_Q(113),
    GK_R(114),
    GK_S(115),
    GK_T(116),
    GK_U(117),
    GK_V(118),
    GK_W(119),
    GK_X(120),
    GK_Y(121),
    GK_Z(122),
    GK_DELETE(127),
    GK_NUMPAD1(1073741913),
    GK_NUMPAD2(1073741914),
    GK_NUMPAD3(1073741915),
    GK_NUMPAD4(1073741916),
    GK_NUMPAD5(1073741917),
    GK_NUMPAD6(1073741918),
    GK_NUMPAD7(1073741919),
    GK_NUMPAD8(1073741920),
    GK_NUMPAD9(1073741921),
    GK_UP(1073741906),
    GK_DOWN(1073741905),
    GK_RIGHT(1073741903),
    GK_LEFT(1073741904),
    GK_INSERT(277),
    GK_HOME(1073741898),
    GK_END(1073741901),
    GK_PRIOR(1073741899),
    GK_NEXT(1073741902),
    GK_F1(1073741882),
    GK_F2(1073741883),
    GK_F3(1073741884),
    GK_F4(1073741885),
    GK_F5(1073741886),
    GK_F6(1073741887),
    GK_F7(1073741888),
    GK_F8(1073741889),
    GK_F9(1073741890),
    GK_F10(1073741891),
    GK_F11(1073741892),
    GK_F12(1073741893);

    private final int intValue;
    private static java.util.HashMap<Integer, Keys> mappings;

    private static java.util.HashMap<Integer, Keys> getMappings()
    {
        synchronized (Keys.class) {
            if (mappings == null) {
                mappings = new java.util.HashMap<>();
            }
        }
        return mappings;
    }

    private Keys(int value)
    {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static Keys forValue(int value)
    {
        return getMappings().get(value);
    }
}
