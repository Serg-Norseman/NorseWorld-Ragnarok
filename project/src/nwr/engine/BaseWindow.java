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

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class BaseWindow extends BaseControl
{
    private int fMouseOldX;
    private int fMouseOldY;

    public WindowStyles WindowStyle;

    public BaseWindow(BaseControl owner)
    {
        super(owner);

        this.setVisible(false);
        this.ControlStyle.include(ControlStyles.csAcceptsControls);
        this.WindowStyle = new WindowStyles(WindowStyles.wsMoveable);
    }

    public final boolean hasStyle(int style)
    {
        return this.WindowStyle.contains(style);
    }

    private void setTopMost()
    {
        BaseControl owner = super.getOwner();
        if (owner != null && owner instanceof BaseWindow) {
            int num = owner.getControls().getCount() - 2;
            for (int i = owner.getControls().indexOf(this); i <= num; i++) {
                owner.getControls().exchange(i, i + 1);
            }
        }
    }

    protected void DoClose()
    {
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        if (eventArgs.Button == MouseButton.mbLeft) {
            this.fMouseOldX = eventArgs.X;
            this.fMouseOldY = eventArgs.Y;
        }
        super.doMouseDownEvent(eventArgs);
    }

    @Override
    protected void doMouseMoveEvent(MouseMoveEventArgs eventArgs)
    {
        if ((this.hasStyle(WindowStyles.wsMoveable)) && this.getOwner() != null && (eventArgs.Shift.contains(ShiftStates.ssLeft))) {
            super.setLeft(super.getLeft() + (this.fMouseX - this.fMouseOldX));
            super.setTop(super.getTop() + (this.fMouseY - this.fMouseOldY));
        }
        super.doMouseMoveEvent(eventArgs);
    }

    @Override
    protected void doShowEvent()
    {
        super.doShowEvent();

        BaseControl owner = super.getOwner();
        if (owner != null) {
            if (this.hasStyle(WindowStyles.wsScreenCenter) && owner instanceof BaseMainWindow) {
                super.setLeft((owner.getWidth() - super.getWidth()) / 2);
                super.setTop((owner.getHeight() - super.getHeight()) / 2);
            }

            if (this.hasStyle(WindowStyles.wsModal)) {
                owner.setActiveControl(this);
            }
        }
    }

    public final void hide()
    {
        if (super.getOwner() != null) {
            super.setVisible(false);
        }
    }

    public final void show()
    {
        if (super.getOwner() != null) {
            this.setTopMost();
            super.setVisible(true);
        }
    }
}
