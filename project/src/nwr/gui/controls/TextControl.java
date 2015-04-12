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
package nwr.gui.controls;

import jzrlib.utils.Logger;
import jzrlib.core.Rect;
import jzrlib.utils.RefObject;
import jzrlib.core.StringList;
import nwr.engine.BaseControl;
import nwr.engine.BaseScreen;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class TextControl extends BaseControl
{
    protected int fLineHeight;
    protected StringList fLines;
    protected int fVisibleLines;

    public int Margin;
    public int TextColor;

    public IGetVariableEvent OnGetVariable;

    public TextControl(BaseControl owner)
    {
        super(owner);
        this.fLines = new StringList();
        this.fLines.OnChange = this::OnChange;
        this.Margin = 3;
        this.TextColor = BaseScreen.clGold;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fLines.dispose();
        }
        super.dispose(disposing);
    }

    public final void clear()
    {
        this.fLines.clear();
    }

    public final int getLineHeight()
    {
        return this.fLineHeight;
    }

    public final void setLineHeight(int value)
    {
        if (this.fLineHeight != value) {
            this.fLineHeight = value;
            this.recalculate();
        }
    }

    public final StringList getLines()
    {
        return this.fLines;
    }

    protected void doPaintLine(BaseScreen screen, int index, Rect rect)
    {
        screen.drawText(rect.Left, rect.Top, this.fLines.get(index), 0);
    }

    @Override
    protected void doResizeEvent()
    {
        super.doResizeEvent();
        this.recalculate();
    }

    protected final int getLineWidth(String line)
    {
        int result = 0;
        if (super.getFont() != null) {
            result = super.getFont().getTextWidth(line) + (this.Margin << 1);
        }
        return result;
    }

    protected final String getVariable(String varName)
    {
        String result = varName;
        if (this.OnGetVariable != null) {
            RefObject<String> tempRef_Result = new RefObject<>(result);
            this.OnGetVariable.invoke(this, tempRef_Result);
            result = tempRef_Result.argValue;
        }
        return result;
    }

    private String processVars(String line)
    {
        StringBuilder temp = new StringBuilder(line);

        int i = 0;
        while (i < temp.length()) {
            if (temp.charAt(i) == '#') {
                int sp = i;
                i++;
                while (i < temp.length() && temp.charAt(i) != '#') {
                    i++;
                }

                if (temp.charAt(i) == '#') {
                    String val = temp.substring(sp + 1, i);
                    temp.delete(sp, i + 1);

                    val = this.getVariable(val);
                    temp.insert(sp, val);

                    i = sp + val.length();
                }
            } else {
                i++;
            }
        }

        return temp.toString();
    }
    
    protected void prepareText()
    {
    }

    private void recalculate()
    {
        Rect r = super.getIntRect();
        if (this.fLineHeight == 0) {
            this.fVisibleLines = 1;
        } else {
            this.fVisibleLines = r.getHeight() / this.fLineHeight;
        }
    }

    protected void OnChange(Object sender)
    {
        try {
            this.fLines.OnChange = null;
            try {
                Rect intRect = this.getIntRect();
                int maxWidth = intRect.getWidth();

                int idx = 0;
                while (idx < this.fLines.getCount()) {
                    String line = this.fLines.get(idx);

                    // process variables
                    line = this.processVars(line);
                    this.fLines.set(idx, line);

                    // wrap text by control width
                    if (this.getLineWidth(line) > maxWidth) {
                        int i = line.length() - 1;
                        while (i > 0) {
                            if (line.charAt(i) == ' ') {
                                String fp = line.substring(0, i + 1);
                                if (this.getLineWidth(fp) <= maxWidth) {
                                    String sp = line.substring(i + 1);
                                    this.fLines.set(idx, fp);
                                    this.fLines.insert(idx + 1, sp);
                                    break;
                                }
                            }
                            i--;
                        }
                    }
                    idx++;
                }

                this.prepareText();
            } finally {
                this.fLines.OnChange = this::OnChange;
            }
        } catch (Exception ex) {
            Logger.write("TextControl.OnChange(): " + ex.getMessage());
        }
    }
}
