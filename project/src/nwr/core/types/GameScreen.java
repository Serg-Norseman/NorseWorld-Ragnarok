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
public enum GameScreen
{
    // WARNING: don't change enum's names, they used in db!
    gsStartup(0, "Startup", ScreenStatus.ssOnce),
    gsVillage(1, "Village", ScreenStatus.ssOnce),
    gsForest(2, "Forest", ScreenStatus.ssOnce),
    gsJotenheim(3, "Jotenheim", ScreenStatus.ssOnce),
    gsNidavell(4, "Nidavell", ScreenStatus.ssOnce),
    gsNiflheim(5, "Niflheim", ScreenStatus.ssOnce),
    gsCrossroad(6, "Crossroad", ScreenStatus.ssOnce),
    gsDungeon(7, "Dungeon", ScreenStatus.ssOnce),
    gsSea(8, "Sea", ScreenStatus.ssOnce),
    gsAlfheim(9, "Alfheim", ScreenStatus.ssOnce),
    gsMuspelheim(10, "Muspelheim", ScreenStatus.ssOnce),
    gsWell(11, "Well", ScreenStatus.ssOnce),
    gsTemple(12, "Temple", ScreenStatus.ssOnce),
    gsWasteland(13, "Wasteland", ScreenStatus.ssOnce),
    gsDead(14, "Dead", ScreenStatus.ssAlways),
    gsMain(15, "", ScreenStatus.ssAlways),
    gsDefeat(16, "Defeat", ScreenStatus.ssAlways),
    gsVictory(17, "Victory", ScreenStatus.ssAlways),
    gsSwirl(18, "Swirl", ScreenStatus.ssAlways),
    gsNone(19, "", ScreenStatus.ssNever);

    public static final int gsFirst = 0;
    public static final int gsLast = 19;

    private final int intValue;
    public final String gfx;
    public ScreenStatus status;

    private static HashMap<Integer, GameScreen> mappings;

    private static HashMap<Integer, GameScreen> getMappings()
    {
        synchronized (GameScreen.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private GameScreen(int value, String gfx, ScreenStatus status)
    {
        this.intValue = value;
        this.gfx = gfx;
        this.status = status;

        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static GameScreen forValue(int value)
    {
        return getMappings().get(value);
    }
}
