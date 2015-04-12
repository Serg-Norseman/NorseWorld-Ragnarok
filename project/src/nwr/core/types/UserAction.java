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
import nwr.core.RS;
import nwr.engine.HotKey;
import nwr.engine.Keys;
import nwr.engine.ShiftStates;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum UserAction
{
    uaDialog(0, "Dialog", RS.rs_Dialog, EventID.event_Dialog, false, new HotKey(Keys.GK_SPACE, new ShiftStates())),
    uaMenu(1, "Menu", RS.rs_Menu, EventID.event_Menu, false, new HotKey(Keys.GK_Q, new ShiftStates())),
    uaSkills(2, "Skills", RS.rs_Skills, EventID.event_Skills, false, new HotKey(Keys.GK_S, new ShiftStates())),
    uaKnowledges(3, "Knowledges", RS.rs_Knowledges, EventID.event_Knowledges, false, new HotKey(Keys.GK_K, new ShiftStates())),
    uaPack(4, "Pack", RS.rs_Pack, EventID.event_Pack, false, new HotKey(Keys.GK_I, new ShiftStates())),
    uaSelf(5, "Self", RS.rs_Self, EventID.event_Self, false, new HotKey(Keys.GK_C, new ShiftStates())),
    uaMap(6, "Map", RS.rs_Map, EventID.event_Map, false, new HotKey(Keys.GK_M, new ShiftStates())),
    uaOptions(7, "Options", RS.rs_Options, EventID.event_Options, false, new HotKey(Keys.GK_O, new ShiftStates())),
    uaHelp(8, "Help", RS.rs_Help, EventID.event_Help, false, new HotKey(Keys.GK_F1, new ShiftStates())),
    uaMoveN(9, "MoveN", RS.rs_MoveN, EventID.event_PlayerMoveN, true, new HotKey(Keys.GK_NUMPAD8, new ShiftStates())),
    uaMoveS(10, "MoveS", RS.rs_MoveS, EventID.event_PlayerMoveS, true, new HotKey(Keys.GK_NUMPAD2, new ShiftStates())),
    uaMoveW(11, "MoveW", RS.rs_MoveW, EventID.event_PlayerMoveW, true, new HotKey(Keys.GK_NUMPAD4, new ShiftStates())),
    uaMoveE(12, "MoveE", RS.rs_MoveE, EventID.event_PlayerMoveE, true, new HotKey(Keys.GK_NUMPAD6, new ShiftStates())),
    uaMoveNW(13, "MoveNW", RS.rs_MoveNW, EventID.event_PlayerMoveNW, true, new HotKey(Keys.GK_NUMPAD7, new ShiftStates())),
    uaMoveNE(14, "MoveNE", RS.rs_MoveNE, EventID.event_PlayerMoveNE, true, new HotKey(Keys.GK_NUMPAD9, new ShiftStates())),
    uaMoveSW(15, "MoveSW", RS.rs_MoveSW, EventID.event_PlayerMoveSW, true, new HotKey(Keys.GK_NUMPAD1, new ShiftStates())),
    uaMoveSE(16, "MoveSE", RS.rs_MoveSE, EventID.event_PlayerMoveSE, true, new HotKey(Keys.GK_NUMPAD3, new ShiftStates())),
    uaWait(17, "Wait", RS.rs_Wait, EventID.event_Wait, false, new HotKey(Keys.GK_PERIOD, new ShiftStates())),
    uaPickupAll(18, "PickupAll", RS.rs_PickupAll, EventID.event_PickupAll, false, new HotKey(Keys.GK_COMMA, new ShiftStates())),
    uaLoad(19, "Load", RS.rs_Load, EventID.event_Load, false, new HotKey(Keys.GK_F3, new ShiftStates())),
    uaSave(20, "Save", RS.rs_Save, EventID.event_Save, false, new HotKey(Keys.GK_F2, new ShiftStates())),
    uaParty(21, "Party", RS.rs_Party, EventID.event_Party, false, new HotKey(Keys.GK_P, new ShiftStates())),
    uaMoveUp(22, "MoveUp", RS.rs_MoveUp, EventID.event_PlayerMoveUp, false, new HotKey(Keys.GK_PERIOD, new ShiftStates())),
    uaMoveDown(23, "MoveDown", RS.rs_MoveDown, EventID.event_PlayerMoveDown, false, new HotKey(Keys.GK_COMMA, new ShiftStates())),
    uaJournal(24, "Journal", RS.rs_Journal, EventID.event_Journal, false, new HotKey(Keys.GK_J, new ShiftStates()));

    public static final int uaFirst = 0;
    public static final int uaLast = 24;

    private final int intValue;
    public final String Sign;
    public final int NameRS;
    public final EventID Event;
    public final boolean WithoutShift;
    public HotKey HotKey;

    private static HashMap<Integer, UserAction> mappings;

    private static HashMap<Integer, UserAction> getMappings()
    {
        synchronized (UserAction.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private UserAction(int value, String sign, int nameRS, EventID event, boolean withoutShift, HotKey hotKey)
    {
        this.intValue = value;
        this.Sign = sign;
        this.NameRS = nameRS;
        this.Event = event;
        this.WithoutShift = withoutShift;
        this.HotKey = hotKey;

        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static UserAction forValue(int value)
    {
        return getMappings().get(value);
    }
}
