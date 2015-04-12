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
import jzrlib.core.Rect;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.gui.controls.CustomButton;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public enum MainControl
{
    mbNone(0, RS.rs_Reserved, "", EventID.event_Nothing, new Rect(0, 0, 0, 0)),
    mbMenu(1, RS.rs_Menu, "itf/MainStBtn.tga", EventID.event_Menu, new Rect(StaticData.MC_X, StaticData.MC_Y + StaticData.BtnTH * 0, StaticData.MC_X + StaticData.St_BtnW, StaticData.MC_Y + StaticData.btnHeight + StaticData.BtnTH * 0)),
    mbMap(2, RS.rs_Map, "itf/MainStBtn.tga", EventID.event_Map, new Rect(StaticData.MC_X + StaticData.St_BtnW + 4, StaticData.MC_Y + StaticData.BtnTH * 0, StaticData.MC_X + (StaticData.St_BtnW << 1) + 4, StaticData.MC_Y + StaticData.btnHeight + StaticData.BtnTH * 0)),
    mbKnowledges(3, RS.rs_Knowledges, "itf/MainStBtn.tga", EventID.event_Knowledges, new Rect(StaticData.MC_X, StaticData.MC_Y + (StaticData.BtnTH * 1), StaticData.MC_X + StaticData.St_BtnW, StaticData.MC_Y + StaticData.btnHeight + (StaticData.BtnTH * 1))),
    mbSkills(4, RS.rs_Skills, "itf/MainStBtn.tga", EventID.event_Skills, new Rect(StaticData.MC_X + StaticData.St_BtnW + 4, StaticData.MC_Y + (StaticData.BtnTH * 1), StaticData.MC_X + (StaticData.St_BtnW << 1) + 4, StaticData.MC_Y + StaticData.btnHeight + (StaticData.BtnTH * 1))),
    mbSelf(5, RS.rs_Self, "itf/MainLgBtn.tga", EventID.event_Self, new Rect(StaticData.MC_X, StaticData.MC_Y + (StaticData.BtnTH << 1), StaticData.MC_X + StaticData.Lg_BtnW, StaticData.MC_Y + StaticData.btnHeight + (StaticData.BtnTH << 1))),
    mbPack(6, RS.rs_Pack, "itf/MainLgBtn.tga", EventID.event_Pack, new Rect(StaticData.MC_X, StaticData.MC_Y + StaticData.BtnTH * 3, StaticData.MC_X + StaticData.Lg_BtnW, StaticData.MC_Y + StaticData.btnHeight + StaticData.BtnTH * 3)),

    mgHP(7, RS.rs_Reserved, "", EventID.event_Nothing, new Rect(640, 73, 792, 80)),
    mgMag(8, RS.rs_Reserved, "", EventID.event_Nothing, new Rect(640, 99, 792, 106)),
    mgFood(9, RS.rs_Reserved, "", EventID.event_Nothing, new Rect(640, 125, 792, 132));

    public static final int mbFirst = 1;
    public static final int mbLast = 6;

    private final int intValue;
    public final int NameRS;
    public final String ImageFile;
    public final EventID Event;
    public final Rect R;
    public CustomButton Button;

    private static HashMap<Integer, MainControl> mappings;

    private static HashMap<Integer, MainControl> getMappings()
    {
        synchronized (MainControl.class) {
            if (mappings == null) {
                mappings = new HashMap<>();
            }
        }
        return mappings;
    }

    private MainControl(int value, int NameRes, String ImageFile, EventID Event, Rect R)
    {
        this.intValue = value;
        this.NameRS = NameRes;
        this.ImageFile = ImageFile;
        this.Event = Event;
        this.R = R;
        this.Button = null;

        getMappings().put(value, this);
    }

    public int getValue()
    {
        return intValue;
    }

    public static MainControl forValue(int value)
    {
        return getMappings().get(value);
    }
}
