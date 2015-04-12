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
package nwr.game.story;

import java.io.IOException;
import jzrlib.core.FileVersion;
import jzrlib.external.BinaryInputStream;
import jzrlib.external.BinaryOutputStream;
import jzrlib.utils.StreamUtils;
import nwr.core.NWDateTime;
import nwr.engine.BaseScreen;

/**
 *
 * @author Serg V. Zhdanovskih
 * @since 0.9.0
 */
public class JournalItem
{
    public final static int DEFAULT_COLOR = BaseScreen.clGold;
    public final static int DEFAULT_TURN = 0;

    public final static int SIT_DEFAULT = 0;
    public final static int SIT_DIALOG = 1;
    public final static int SIT_DAY = 2;
    public final static int SIT_KILLED = 3;
    public final static int SIT_QUESTS = 4;
    public final static int SIT_QUEST_Y = 5;
    public final static int SIT_QUEST_N = 6;
    
    public int Type;
    public String Text;
    public int Color;
    public int Turn;
    public NWDateTime DateTime;

    public JournalItem()
    {
    }

    public JournalItem(int type)
    {
        this.Type = type;
        this.Text = "";
        this.Turn = DEFAULT_TURN;
        this.DateTime = null;
        
        this.initColor();
    }

    public JournalItem(int type, String text, int color, int turn)
    {
        this.Type = type;
        this.Text = text;
        this.Color = color;
        this.Turn = turn;
        this.DateTime = null;
        
        this.initColor();
    }

    public JournalItem(int type, String text, int color, NWDateTime dateTime)
    {
        this.Type = type;
        this.Text = text;
        this.Color = color;
        this.Turn = DEFAULT_TURN;
        this.DateTime = dateTime;
        
        this.initColor();
    }
    
    private void initColor()
    {
        int res;
        
        switch (this.Type) {
            case SIT_DIALOG:
                res = BaseScreen.clGoldenrod;
                break;
            case SIT_DAY:
                res = BaseScreen.clSkyBlue;
                break;
            case SIT_KILLED:
                res = BaseScreen.clRed;
                break;
            case SIT_QUESTS:
                res = BaseScreen.clBlue;
                break;
            case SIT_QUEST_Y:
                res = BaseScreen.clGreen;
                break;
            case SIT_QUEST_N:
                res = BaseScreen.clMaroon;
                break;

            default:
                res = BaseScreen.clGold;
                break;
        }

        this.Color = res;
    }

    public final void loadFromStream(BinaryInputStream stream, FileVersion version) throws IOException
    {
        this.Type = StreamUtils.readInt(stream);
        this.Text = StreamUtils.readString(stream);
        this.Color = StreamUtils.readInt(stream);
        this.Turn = StreamUtils.readInt(stream);

        if (this.Type == JournalItem.SIT_DAY) {
            this.DateTime = new NWDateTime();
            this.DateTime.loadFromStream(stream, version);
        }
    }

    public final void saveToStream(BinaryOutputStream stream, FileVersion version) throws IOException
    {
        StreamUtils.writeInt(stream, this.Type);
        StreamUtils.writeString(stream, this.Text);
        StreamUtils.writeInt(stream, this.Color);
        StreamUtils.writeInt(stream, this.Turn);

        if (this.Type == JournalItem.SIT_DAY) {
            this.DateTime.saveToStream(stream, version);
        }
    }
}
