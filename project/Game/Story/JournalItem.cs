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

using System.IO;
using NWR.Core;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.Game.Story
{
    public class JournalItem
    {
        public const int DEFAULT_COLOR = BaseScreen.clGold;
        public const int DEFAULT_TURN = 0;

        public const int SIT_DEFAULT = 0;
        public const int SIT_DIALOG = 1;
        public const int SIT_DAY = 2;
        public const int SIT_KILLED = 3;
        public const int SIT_QUESTS = 4;
        public const int SIT_QUEST_Y = 5;
        public const int SIT_QUEST_N = 6;

        public int Type;
        public string Text;
        public int Color;
        public int Turn;
        public NWDateTime DateTime;

        public JournalItem()
        {
        }

        public JournalItem(int type)
        {
            Type = type;
            Text = "";
            Turn = DEFAULT_TURN;
            DateTime = null;

            InitColor();
        }

        public JournalItem(int type, string text, int color, int turn)
        {
            Type = type;
            Text = text;
            Color = color;
            Turn = turn;
            DateTime = null;

            InitColor();
        }

        public JournalItem(int type, string text, int color, NWDateTime dateTime)
        {
            Type = type;
            Text = text;
            Color = color;
            Turn = DEFAULT_TURN;
            DateTime = dateTime;

            InitColor();
        }

        private void InitColor()
        {
            int res;

            switch (Type) {
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

            Color = res;
        }

        public void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            Type = StreamUtils.ReadInt(stream);
            Text = StreamUtils.ReadString(stream, StaticData.DefEncoding);
            Color = StreamUtils.ReadInt(stream);
            Turn = StreamUtils.ReadInt(stream);

            if (Type == SIT_DAY) {
                DateTime = new NWDateTime();
                DateTime.LoadFromStream(stream, version);
            }
        }

        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            StreamUtils.WriteInt(stream, Type);
            StreamUtils.WriteString(stream, Text, StaticData.DefEncoding);
            StreamUtils.WriteInt(stream, Color);
            StreamUtils.WriteInt(stream, Turn);

            if (Type == SIT_DAY) {
                DateTime.SaveToStream(stream, version);
            }
        }
    }
}
