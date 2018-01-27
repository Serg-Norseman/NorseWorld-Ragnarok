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

using System;
using BSLib;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI.Controls
{
    public delegate void IGetVariableEvent(object sender, ref string  refVar);

    public abstract class TextControl : BaseControl
    {
        protected int fLineHeight;
        protected StringList fLines;
        protected int fVisibleLines;

        public int Margin;
        public int TextColor;

        public IGetVariableEvent OnGetVariable;

        protected TextControl(BaseControl owner)
            : base(owner)
        {
            fLines = new StringList();
            fLines.OnChange += OnChange;
            Margin = 3;
            TextColor = Colors.Gold;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fLines.Dispose();
            }
            base.Dispose(disposing);
        }

        public void Clear()
        {
            fLines.Clear();
        }

        public int LineHeight
        {
            get {
                return fLineHeight;
            }
            set {
                if (fLineHeight != value) {
                    fLineHeight = value;
                    Recalculate();
                }
            }
        }

        public StringList Lines
        {
            get {
                return fLines;
            }
        }

        protected virtual void DoPaintLine(BaseScreen screen, int index, ExtRect rect)
        {
            screen.DrawText(rect.Left, rect.Top, fLines[index], 0);
        }

        protected override void DoResizeEvent()
        {
            base.DoResizeEvent();
            Recalculate();
        }

        protected int GetLineWidth(string line)
        {
            int result = 0;
            if (Font != null) {
                result = Font.GetTextWidth(line) + (Margin << 1);
            }
            return result;
        }

        protected string GetVariable(string varName)
        {
            string result = varName;
            if (OnGetVariable != null) {
                OnGetVariable.Invoke(this, ref result);
            }
            return result;
        }

        private string ProcessVars(string line)
        {
            string temp = line;

            int i = 0;
            while (i < temp.Length) {
                if (temp[i] == '#') {
                    int sp = i;
                    i++;
                    while (i < temp.Length && temp[i] != '#') {
                        i++;
                    }

                    if (temp[i] == '#') {
                        string val = temp.Substring(sp + 1, i - (sp + 1));
                        temp = temp.Remove(sp, val.Length + 2);

                        val = GetVariable(val);
                        temp = temp.Insert(sp, val);

                        i = sp + val.Length;
                    }
                } else {
                    i++;
                }
            }

            return temp;
        }

        protected virtual void PrepareText()
        {
        }

        private void Recalculate()
        {
            ExtRect r = IntRect;
            if (fLineHeight == 0) {
                fVisibleLines = 1;
            } else {
                fVisibleLines = r.Height / fLineHeight;
            }
        }

        protected virtual void OnChange(object sender)
        {
            try {
                fLines.OnChange -= OnChange;
                try {
                    ExtRect intRect = IntRect;
                    int maxWidth = intRect.Width;

                    int idx = 0;
                    while (idx < fLines.Count) {
                        string line = fLines[idx];

                        // process variables
                        line = ProcessVars(line);
                        fLines[idx] = line;

                        // wrap text by control width
                        if (GetLineWidth(line) > maxWidth) {
                            int i = line.Length - 1;
                            while (i > 0) {
                                if (line[i] == ' ') {
                                    string fp = line.Substring(0, i + 1);
                                    if (GetLineWidth(fp) <= maxWidth) {
                                        string sp = line.Substring(i + 1);
                                        fLines[idx] = fp;
                                        fLines.Insert(idx + 1, sp);
                                        break;
                                    }
                                }
                                i--;
                            }
                        }
                        idx++;
                    }

                    PrepareText();
                } finally {
                    fLines.OnChange += OnChange;
                }
            } catch (Exception ex) {
                Logger.Write("TextControl.OnChange(): " + ex.Message);
            }
        }
    }
}
