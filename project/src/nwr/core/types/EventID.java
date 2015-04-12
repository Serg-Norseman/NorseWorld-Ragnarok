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
public enum EventID
{
    event_Nothing(0),
    event_Startup(1),
    event_Map(2),
    event_About(3),
    event_Self(4),
    event_Wait(5),
    event_PickupAll(6),
    event_DoorClose(7),
    event_DoorOpen(8),
    event_Help(9),
    event_AutoStart(10),
    event_AutoStop(11),
    event_LandEnter(12, new EventFlags(EventFlags.efInJournal), 0),
    event_Hit(13),
    event_Miss(14),
    event_Intro(15, new EventFlags(EventFlags.efInJournal), 0),
    event_Knowledges(16),
    event_Skills(17),
    event_Menu(18),
    event_Attack(19, new EventFlags(EventFlags.efInQueue, EventFlags.efInJournal), 100),
    event_Killed(20, new EventFlags(EventFlags.efInJournal), 0),
    event_Move(21),
    event_Shot(22),
    event_Slay(23),
    event_Wounded(24, new EventFlags(EventFlags.efInJournal), 0),
    event_Trap(25),
    event_Trade(26),
    event_LevelUp(27),
    event_Pack(28),
    event_DialogBegin(29),
    event_DialogEnd(30),
    event_DialogRemark(31),
    event_Options(32),
    event_Throw(33),
    event_Defeat(34),
    event_Dialog(35),
    event_LookAt(36),
    event_Quit(38),
    event_Save(39),
    event_Load(40),
    event_New(41),
    event_Dead(42),
    event_Party(43),
    event_Victory(44),
    event_EffectSound(45),
    event_ItemDrop(46),
    event_ItemPickup(47),
    event_ItemRemove(48),
    event_ItemWear(49),
    event_ItemBreak(50),
    event_ItemUse(51),
    event_ItemMix(52),
    event_PlayerMoveN(61),
    event_PlayerMoveS(62),
    event_PlayerMoveW(63),
    event_PlayerMoveE(64),
    event_PlayerMoveNW(65),
    event_PlayerMoveNE(66),
    event_PlayerMoveSW(67),
    event_PlayerMoveSE(68),
    event_PlayerMoveUp(69),
    event_PlayerMoveDown(70),
    event_Journal(71);

    
    public static final int event_First = 1;
    public static final int event_Last = 71;

    private final int intValue;
    public final EventFlags Flags;
    public final int Priority;

    private static HashMap<Integer, EventID> mappings;

    private static HashMap<Integer, EventID> getMappings()
    {
        synchronized (EventID.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private EventID(int value)
    {
        this.intValue = value;
        this.Flags = new EventFlags();
        this.Priority = 0;
        getMappings().put(value, this);
    }

    private EventID(int value, EventFlags flags, int priority)
    {
        this.intValue = value;
        this.Flags = flags;
        this.Priority = priority;
        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static EventID forValue(int value)
    {
        return getMappings().get(value);
    }
}
