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

import jzrlib.core.StringList;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class BaseHintWindow extends BaseWindow
{
    protected StringList fText;

    public int Margin;

    @Override
    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        super.doKeyDownEvent(eventArgs);
        if (eventArgs.Key == Keys.GK_ESCAPE) {
            this.hide();
        }
    }

    @Override
    public void setCaption(String value)
    {
        super.setCaption(value);

        this.fText.setTextStr(value);
        int max = 0;

        int num = this.fText.getCount();
        for (int i = 0; i < num; i++) {
            int w = super.getFont().getTextWidth(this.fText.get(i));
            if (max < w) {
                max = w;
            }
        }

        int h = super.getFont().Height * this.fText.getCount();

        super.setHeight(h + (this.Margin << 1));
        super.setWidth(max + (this.Margin << 1));
    }

    public BaseHintWindow(BaseControl owner)
    {
        super(owner);
        super.WindowStyle = new WindowStyles();
        this.fText = new StringList();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fText.dispose();
        }
        super.dispose(disposing);
    }
}
