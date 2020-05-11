/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih.
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

namespace NWR.Game.Types
{
    public enum EventID
    {
        event_Nothing,
        event_Startup,
        event_Map,
        event_About,
        event_Self,
        event_Wait,
        event_PickupAll,
        event_DoorClose,
        event_DoorOpen,
        event_Help,
        event_AutoStart,
        event_AutoStop,
        event_LandEnter,
        event_Hit,
        event_Miss,
        event_Intro,
        event_Knowledges,
        event_Skills,
        event_Menu,
        event_Attack,
        event_Killed,
        event_Move,
        event_Shot,
        event_Slay,
        event_Wounded,
        event_Trap,
        event_Trade,
        event_LevelUp,
        event_Pack,
        event_DialogBegin,
        event_DialogEnd,
        event_DialogRemark,
        event_Options,
        event_Throw,
        event_Defeat,
        event_Dialog,
        event_LookAt,
        event_Quit,
        event_Save,
        event_Load,
        event_New,
        event_Dead,
        event_Party,
        event_Victory,
        event_EffectSound,
        event_ItemDrop,
        event_ItemPickup,
        event_ItemRemove,
        event_ItemWear,
        event_ItemBreak,
        event_ItemUse,
        event_ItemMix,
        event_PlayerMoveN,
        event_PlayerMoveS,
        event_PlayerMoveW,
        event_PlayerMoveE,
        event_PlayerMoveNW,
        event_PlayerMoveNE,
        event_PlayerMoveSW,
        event_PlayerMoveSE,
        event_PlayerMoveUp,
        event_PlayerMoveDown,
        event_Journal,

        event_First = event_Startup,
        event_Last = event_Journal
    }
}
