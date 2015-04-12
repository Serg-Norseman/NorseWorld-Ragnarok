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

import jzrlib.core.BaseObject;
import jzrlib.core.ExtList;
import jzrlib.core.INotifyEvent;
import jzrlib.utils.Logger;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.utils.RefObject;
import jzrlib.utils.TextUtils;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class BaseControl extends BaseObject
{
    public static final int DRAGSTATE_ENTER = 0;
    public static final int DRAGSTATE_LEAVE = 1;
    public static final int DRAGSTATE_MOVE = 2;

    public static final int DRAGMODE_MANUAL = 0;
    public static final int DRAGMODE_AUTOMATIC = 1;
    
    private static final int MouseDragThreshold = 5;

    private static BaseControl DragControl;

    public static BaseDragObject DragObject;

    protected int fMouseX;
    protected int fMouseY;

    private BaseControl fActiveControl;
    private String fCaption;
    private ExtList<BaseControl> fControls;
    private BaseImage fCursor;
    private int fDragMode;
    private Font fFont;
    private int fHeight;
    private int fLangResID;
    private int fLeft;
    private final BaseControl fOwner;
    private int fStyle;
    private boolean fStyleChanging;
    private int fTop;
    private boolean fVisible;
    private int fWidth;

    private int fAbsLeft;
    private int fAbsTop;

    public INotifyEvent OnClick;
    public IDragEndEvent OnDragEnd;
    public IDragDropEvent OnDragDrop;
    public IDragOverEvent OnDragOver;
    public IDragStartEvent OnDragStart;
    public INotifyEvent OnEnter;
    public INotifyEvent OnExit;
    public INotifyEvent OnHide;
    public IKeyEvent OnKeyDown;
    public IKeyEvent OnKeyUp;
    public IKeyPressEvent OnKeyPress;
    public INotifyEvent OnLangChange;
    public IMouseEvent OnMouseDown;
    public IMouseMoveEvent OnMouseMove;
    public IMouseEvent OnMouseUp;
    public IMouseWheelEvent OnMouseWheel;
    public IPaintEvent OnPaint;
    public INotifyEvent OnResize;
    public INotifyEvent OnShow;

    public ControlStyles ControlStyle;
    public boolean Enabled;
    public String Hint;
    public boolean ShowHints;
    public int Tag;

    public BaseControl(BaseControl owner)
    {
        this.ControlStyle = new ControlStyles(ControlStyles.csOpaque);

        this.fControls = new ExtList<>(true);
        this.fActiveControl = null;
        this.fOwner = owner;
        this.fCursor = null;
        this.Enabled = true;
        this.fVisible = true;
        this.Hint = "";
        this.ShowHints = false;
        this.fLangResID = -1;

        if (this.fOwner != null) {
            this.fOwner.fControls.add(this);
        }
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.fOwner != null) {
                this.fOwner.fControls.extract(this);
                this.fOwner.fControls.pack();
            }
            this.fControls.dispose();
        }
        super.dispose(disposing);
    }

    public final int getLeft()
    {
        return this.fLeft;
    }

    public final void setLeft(int value)
    {
        if (this.fLeft != value) {
            this.fLeft = value;
            this.doResizeEvent();
        }
    }

    public final int getTop()
    {
        return this.fTop;
    }

    public final void setTop(int value)
    {
        if (this.fTop != value) {
            this.fTop = value;
            this.doResizeEvent();
        }
    }

    public final int getHeight()
    {
        return this.fHeight;
    }

    public final void setHeight(int value)
    {
        if (this.fHeight != value) {
            this.fHeight = value;
            this.doResizeEvent();
        }
    }

    public final int getWidth()
    {
        return this.fWidth;
    }

    public final void setWidth(int value)
    {
        if (this.fWidth != value) {
            this.fWidth = value;
            this.doResizeEvent();
        }
    }

    public final Rect getClientRect()
    {
        return new Rect(0, 0, this.fWidth - 1, this.fHeight - 1);
    }

    private void adjustAbsCoords()
    {
        this.fAbsLeft = this.fLeft;
        this.fAbsTop = this.fTop;

        if (this.fOwner != null) {
            this.fAbsLeft += this.fOwner.fAbsLeft;
            this.fAbsTop += this.fOwner.fAbsTop;
        }
    }

    private void resetAbsClip(BaseScreen screen)
    {
        int al = this.fAbsLeft;
        int at = this.fAbsTop;
        screen.setOffset(al, at);
        Rect absRect = new Rect(al, at, al + this.fWidth - 1, at + this.fHeight - 1);
        screen.initClip(absRect);
    }

    public final Rect getBounds()
    {
        return new Rect(this.fLeft, this.fTop, this.fLeft + this.fWidth - 1, this.fTop + this.fHeight - 1);
    }

    public final void setBounds(Rect value)
    {
        int L = value.Left;
        int T = value.Top;
        int H = value.getHeight();
        int W = value.getWidth();
        if (L != this.fLeft || T != this.fTop || H != this.fHeight || W != this.fWidth) {
            this.fLeft = L;
            this.fTop = T;
            this.fHeight = H;
            this.fWidth = W;
            this.doResizeEvent();
        }
    }

    public Rect getIntRect()
    {
        return new Rect(1, 1, this.fWidth - 2, this.fHeight - 2);
    }

    public String getCaption()
    {
        return this.fCaption;
    }

    public void setCaption(String value)
    {
        if (!TextUtils.equals(this.fCaption, value)) {
            this.fCaption = value;
        }
    }

    public final int getDragMode()
    {
        return this.fDragMode;
    }

    public final void setDragMode(int value)
    {
        this.fDragMode = value;
    }

    public final int getLangResID()
    {
        return this.fLangResID;
    }

    public final void setLangResID(int value)
    {
        this.fLangResID = value;
    }

    public final BaseControl getActiveControl()
    {
        return this.fActiveControl;
    }

    public void setActiveControl(BaseControl value)
    {
        if (this.fOwner != null) {
            this.fOwner.setActiveControl(this);
        }
        if (this.fActiveControl != null) {
            this.fActiveControl.doExitEvent();
        }
        this.fActiveControl = value;
        if (this.fActiveControl != null) {
            this.fActiveControl.doEnterEvent();
        }
    }

    public final ExtList<BaseControl> getControls()
    {
        return this.fControls;
    }

    public final Point getMousePoint()
    {
        return new Point(this.fMouseX, this.fMouseY);
    }

    public final BaseControl getOwner()
    {
        return this.fOwner;
    }

    public final int getStyle()
    {
        return this.fStyle;
    }

    public void setStyle(int value)
    {
        this.fStyleChanging = true;
        this.fStyle = value;
        this.doStyleChanged();

        int num = this.fControls.getCount();
        for (int i = 0; i < num; i++) {
            this.fControls.get(i).setStyle(value);
        }
        this.fStyleChanging = false;
    }

    public final boolean getVisible()
    {
        return this.fVisible;
    }

    public void setVisible(boolean value)
    {
        if (this.fVisible != value) {
            this.fVisible = value;

            if (this.fVisible) {
                this.doShowEvent();
            } else {
                if (this.fOwner != null && this.fOwner.getActiveControl() != null && this.fOwner.getActiveControl().equals(this)) {
                    this.getOwner().setActiveControl(null);
                }
                this.doHideEvent();
            }
        }
    }

    public final BaseImage getCursor()
    {
        BaseImage result = null;
        if (this.fCursor != null) {
            result = this.fCursor;
        } else {
            if (this.fOwner != null) {
                result = this.fOwner.getCursor();
            }
        }
        return result;
    }

    public final void setCursor(BaseImage value)
    {
        this.fCursor = value;
    }

    public final Font getFont()
    {
        Font result = null;
        if (this.fFont != null) {
            result = this.fFont;
        } else {
            if (this.fOwner != null) {
                result = this.fOwner.getFont();
            }
        }
        return result;
    }

    public final void setFont(Font value)
    {
        this.fFont = value;
        BaseMainWindow mainWnd = this.getMainWindow();
        mainWnd.getScreen().Font = value;
    }

    private boolean isKeyPreview()
    {
        BaseWindow wnd = (this instanceof BaseWindow) ? (BaseWindow) this : null;
        return (wnd != null) ? wnd.hasStyle(WindowStyles.wsKeyPreview) : false;
    }

    private boolean inClientRect(int aX, int aY)
    {
        int right = this.fLeft + this.fWidth - 1;
        int bottom = this.fTop + this.fHeight - 1;
        return aX >= this.fLeft && aX <= right && aY >= this.fTop && aY <= bottom;
    }

    private BaseControl getClientControl(int aX, int aY)
    {
        for (int i = this.fControls.getCount() - 1; i >= 0; i--) {
            BaseControl ctl = ((BaseControl) this.fControls.get(i));
            if (ctl.fVisible && ctl.inClientRect(aX, aY)) {
                return ctl;
            }
        }

        return null;
    }

    public final BaseControl getSubControl(int aX, int aY)
    {
        aX -= this.fLeft;
        aY -= this.fTop;

        BaseControl ctl = this.getClientControl(aX, aY);
        BaseControl result;
        if (ctl == null) {
            result = this;
        } else {
            result = ctl.getSubControl(aX, aY);
        }
        return result;
    }

    private void exDragInit(boolean immediate)
    {
        BaseMainWindow mainWnd = this.getMainWindow();
        BaseControl.DragControl = this;
        try {
            BaseDragObject dObject = null;
            RefObject<BaseDragObject> tempRef_dObject = new RefObject<>(dObject);
            BaseControl.DragControl.doDragStartEvent(tempRef_dObject);
            dObject = tempRef_dObject.argValue;
            if (BaseControl.DragControl == null) {
                return;
            }

            if (dObject == null) {
                dObject = new BaseDragObject();
            }
            BaseControl.DragObject = dObject;
            BaseControl.DragObject.DragTarget = null;
            BaseControl.DragObject.DragStartPos = mainWnd.getMousePoint();
            BaseControl.DragObject.DragPos = BaseControl.DragObject.DragStartPos;

            if (immediate) {
                BaseControl.DragObject.ActiveDrag = true;
            } else {
                BaseControl.DragObject.ActiveDrag = false;
            }

            if (BaseControl.DragObject.ActiveDrag) {
                BaseControl.exDragTo(BaseControl.DragObject.DragStartPos);
            }
        } catch (Exception e) {
            BaseControl.DragControl = null;
            throw e;
        }
    }

    private static boolean exDoDragOver(int dragState)
    {
        boolean result = false;
        if (BaseControl.DragObject.DragTarget != null) {
            boolean accepted = false;
            RefObject<Boolean> tempRef_accepted = new RefObject<>(accepted);
            BaseControl.DragObject.DragTarget.doDragOverEvent(BaseControl.DragControl, BaseControl.DragObject.DragPos.X, BaseControl.DragObject.DragPos.Y, dragState, tempRef_accepted);
            accepted = tempRef_accepted.argValue;
            result = accepted;
        }
        return result;
    }

    public static void exDragDone(boolean Drop)
    {
        if (BaseControl.DragObject != null && !BaseControl.DragObject.Cancelling) {
            BaseDragObject DragSave = BaseControl.DragObject;
            try {
                try {
                    BaseControl.DragObject.Cancelling = true;
                    Point TargetPos = Point.Zero();

                    if (BaseControl.DragObject.DragTarget != null && BaseControl.DragObject.DragTarget != null) {
                        TargetPos = BaseControl.DragObject.DragPos;
                    }

                    boolean Accepted = (BaseControl.DragObject.DragTarget != null & Drop) && BaseControl.DragObject.ActiveDrag && BaseControl.exDoDragOver(BaseControl.DRAGSTATE_LEAVE);

                    if (Accepted) {
                        BaseControl.DragControl.doDragEndEvent(BaseControl.DragObject.DragTarget, TargetPos.X, TargetPos.Y);
                        BaseControl.DragObject.DragTarget.doDragDropEvent(BaseControl.DragControl, TargetPos.X, TargetPos.Y);
                    }
                } finally {
                    if (DragSave != null) {
                        DragSave.Cancelling = false;
                    }
                    BaseControl.DragObject = null;
                }
            } finally {
                BaseControl.DragControl = null;
                if (DragSave != null) {
                    DragSave = null;
                }
            }
        }
    }

    public static void exDragTo(Point Pos)
    {
        if (BaseControl.DragObject.ActiveDrag || Math.abs(BaseControl.DragObject.DragStartPos.X - Pos.X) >= BaseControl.MouseDragThreshold || Math.abs(BaseControl.DragObject.DragStartPos.Y - Pos.Y) >= BaseControl.MouseDragThreshold) {
            BaseMainWindow mainWnd = BaseControl.DragControl.getMainWindow();
            BaseControl Target = mainWnd.getSubControl(Pos.X, Pos.Y);
            BaseControl.DragObject.ActiveDrag = true;
            if (!Target.equals(BaseControl.DragObject.DragTarget)) {
                BaseControl.exDoDragOver(BaseControl.DRAGSTATE_LEAVE);
                if (BaseControl.DragObject == null) {
                    return;
                }
                BaseControl.DragObject.DragTarget = Target;
                BaseControl.DragObject.DragPos = Pos;
                BaseControl.exDoDragOver(BaseControl.DRAGSTATE_ENTER);
                if (BaseControl.DragObject == null) {
                    return;
                }
            }
            BaseControl.DragObject.DragPos = Pos;
            BaseControl.DragObject.Accepted = BaseControl.exDoDragOver(BaseControl.DRAGSTATE_MOVE);
        }
    }

    protected void doClickEvent()
    {
        if (this.OnClick != null) {
            this.OnClick.invoke(this);
        }
    }

    protected void doDragDropEvent(Object Source, int X, int Y)
    {
        if (this.OnDragDrop != null) {
            this.OnDragDrop.invoke(this, Source, X, Y);
        }
    }

    protected void doDragEndEvent(Object Target, int X, int Y)
    {
        if (this.OnDragEnd != null) {
            this.OnDragEnd.invoke(this, Target, X, Y);
        }
    }

    protected void doDragOverEvent(Object source, int X, int Y, int dragState, RefObject<Boolean> refAccept)
    {
        refAccept.argValue = false;
        if (this.OnDragOver != null) {
            refAccept.argValue = true;
            this.OnDragOver.invoke(this, source, X, Y, refAccept);
        }
    }

    protected void doDragStartEvent(RefObject<BaseDragObject> DragObject)
    {
        if (this.OnDragStart != null) {
            this.OnDragStart.invoke(this, DragObject);
        }
    }

    protected void doEnterEvent()
    {
        if (this.OnEnter != null) {
            this.OnEnter.invoke(this);
        }
    }

    protected void doExitEvent()
    {
        if (this.OnExit != null) {
            this.OnExit.invoke(this);
        }
    }

    protected void doHideEvent()
    {
        int num = this.fControls.getCount();
        for (int i = 0; i < num; i++) {
            BaseControl ctl = this.fControls.get(i);
            if (ctl.fVisible) {
                ctl.doHideEvent();
            }
        }
        if (this.OnHide != null) {
            this.OnHide.invoke(this);
        }
    }

    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        if (this.OnKeyDown != null) {
            this.OnKeyDown.invoke(this, eventArgs);
        }
    }

    protected void doKeyPressEvent(KeyPressEventArgs eventArgs)
    {
        if (this.OnKeyPress != null) {
            this.OnKeyPress.invoke(this, eventArgs);
        }
    }

    protected void doKeyUpEvent(KeyEventArgs eventArgs)
    {
        if (this.OnKeyUp != null) {
            this.OnKeyUp.invoke(this, eventArgs);
        }
    }

    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        if (this.OnMouseDown != null) {
            this.OnMouseDown.invoke(this, eventArgs);
        }
    }

    protected void doMouseMoveEvent(MouseMoveEventArgs eventArgs)
    {
        if (this.OnMouseMove != null) {
            this.OnMouseMove.invoke(this, eventArgs);
        }
    }

    protected void doMouseUpEvent(MouseEventArgs eventArgs)
    {
        if (this.OnMouseUp != null) {
            this.OnMouseUp.invoke(this, eventArgs);
        }
    }

    protected void doMouseWheelEvent(MouseWheelEventArgs eventArgs)
    {
        if (this.OnMouseWheel != null) {
            this.OnMouseWheel.invoke(this, eventArgs);
        }
    }

    protected void doPaintEvent(BaseScreen screen)
    {
        if (this.OnPaint != null) {
            this.OnPaint.invoke(this, screen);
        }
    }

    protected void doResizeEvent()
    {
        if (this.OnResize != null) {
            this.OnResize.invoke(this);
        }
    }

    protected void doShowEvent()
    {
        int num = this.fControls.getCount();
        for (int i = 0; i < num; i++) {
            BaseControl ctl = this.fControls.get(i);
            if (ctl.fVisible) {
                ctl.doShowEvent();
            }
        }

        if (this.OnShow != null) {
            this.OnShow.invoke(this);
        }
    }

    protected void doStyleChanged()
    {
    }

    protected final BaseMainWindow getMainWindow()
    {
        BaseMainWindow result;
        if (this.fOwner == null) {
            if (this instanceof BaseMainWindow) {
                result = (BaseMainWindow) this;
            } else {
                result = null;
            }
        } else {
            if (this.fOwner instanceof BaseMainWindow) {
                result = (BaseMainWindow) this.fOwner;
            } else {
                result = this.fOwner.getMainWindow();
            }
        }
        return result;
    }

    public final void BeginDrag(boolean immediate)
    {
        if (!(this instanceof BaseWindow) && BaseControl.DragControl == null) {
            this.exDragInit(immediate);
        }
    }

    public final void EndDrag(boolean drop)
    {
        if (BaseControl.DragControl.equals(this)) {
            BaseControl.exDragDone(drop);
        }
    }

    public final boolean isActive()
    {
        return this.getOwner() != null && this.getOwner().getActiveControl().equals(this);
    }

    public void changeLang()
    {
        if (this.OnLangChange != null) {
            this.OnLangChange.invoke(this);
        }

        int num = this.fControls.getCount();
        for (int i = 0; i < num; i++) {
            this.fControls.get(i).changeLang();
        }
    }

    public final void processKeyDown(KeyEventArgs eventArgs)
    {
        if (this.fActiveControl != null) {
            if (this.isKeyPreview()) {
                this.doKeyDownEvent(eventArgs);
            }
            this.fActiveControl.processKeyDown(eventArgs);
        } else {
            if (this.Enabled) {
                this.doKeyDownEvent(eventArgs);
            }
        }
    }

    public final void processKeyPress(KeyPressEventArgs eventArgs)
    {
        if (this.fActiveControl != null) {
            if (this.isKeyPreview()) {
                this.doKeyPressEvent(eventArgs);
            }
            this.fActiveControl.processKeyPress(eventArgs);
        } else {
            if (this.Enabled) {
                this.doKeyPressEvent(eventArgs);
            }
        }
    }

    public final void processKeyUp(KeyEventArgs eventArgs)
    {
        if (this.fActiveControl != null) {
            if (this.isKeyPreview()) {
                this.doKeyUpEvent(eventArgs);
            }
            this.fActiveControl.processKeyUp(eventArgs);
        } else {
            if (this.Enabled) {
                this.doKeyUpEvent(eventArgs);
            }
        }
    }

    public final void processMouseDown(MouseEventArgs eventArgs)
    {
        eventArgs.X -= this.fLeft;
        eventArgs.Y -= this.fTop;

        if (BaseControl.DragObject == null) {
            BaseControl ctl = this.getClientControl(eventArgs.X, eventArgs.Y);
            BaseControl activeCtl = this.fActiveControl;

            if (this instanceof BaseWindow && activeCtl != null && activeCtl instanceof BaseWindow && (((BaseWindow) activeCtl).hasStyle(WindowStyles.wsModal))) {
                if (activeCtl.equals(ctl)) {
                    activeCtl.processMouseDown(eventArgs);
                }
            } else {
                if (this.getOwner() != null && (this.getOwner().ControlStyle.contains(ControlStyles.csAcceptsControls))) {
                    this.getOwner().setActiveControl(this);
                }
                this.fActiveControl = null;
                if (ctl != null) {
                    ctl.processMouseDown(eventArgs);
                } else {
                    if (this.Enabled) {
                        this.doMouseDownEvent(eventArgs);
                    }
                    if (this.fDragMode == BaseControl.DRAGMODE_AUTOMATIC) {
                        this.BeginDrag(false);
                    }
                }
            }
        }
    }

    public final void processMouseMove(MouseMoveEventArgs eventArgs)
    {
        eventArgs = (MouseMoveEventArgs) eventArgs.clone();

        eventArgs.X -= this.fLeft;
        eventArgs.Y -= this.fTop;

        this.fMouseX = eventArgs.X;
        this.fMouseY = eventArgs.Y;

        if (BaseControl.DragObject != null) {
            BaseControl.exDragTo(new Point(eventArgs.X, eventArgs.Y));
        } else {
            BaseControl ctl = this.getClientControl(eventArgs.X, eventArgs.Y);
            BaseControl activeCtl = this.fActiveControl;

            if (this instanceof BaseWindow && activeCtl != null && activeCtl instanceof BaseWindow && (((BaseWindow) activeCtl).hasStyle(WindowStyles.wsModal))) {
                if (activeCtl.equals(ctl)) {
                    activeCtl.processMouseMove(eventArgs);
                }
            } else {
                if (this.fActiveControl != null) {
                    this.fActiveControl.processMouseMove(eventArgs);
                }
                if (ctl != null) {
                    ctl.processMouseMove(eventArgs);
                }
                if (this.Enabled) {
                    this.doMouseMoveEvent(eventArgs);
                }
            }
        }
    }

    public final void processMouseUp(MouseEventArgs eventArgs)
    {
        eventArgs.X -= this.fLeft;
        eventArgs.Y -= this.fTop;

        if (BaseControl.DragObject != null) {
            BaseControl.exDragDone(true);
        } else {
            BaseControl ctl = this.getClientControl(eventArgs.X, eventArgs.Y);
            BaseControl activeCtl = this.fActiveControl;

            if (this instanceof BaseWindow && activeCtl != null && activeCtl instanceof BaseWindow && (((BaseWindow) activeCtl).hasStyle(WindowStyles.wsModal))) {
                if (activeCtl.equals(ctl)) {
                    activeCtl.processMouseUp(eventArgs);
                }
            } else {
                if (this.fActiveControl != null && (this.fActiveControl.ControlStyle.contains(ControlStyles.csCaptureMouse))) {
                    this.fActiveControl.processMouseUp(eventArgs);
                } else {
                    if (ctl != null) {
                        ctl.processMouseUp(eventArgs);
                    } else {
                        if (this.Enabled) {
                            this.doMouseUpEvent(eventArgs);
                        }
                    }
                }
            }
        }
    }

    public final void processMouseWheel(MouseWheelEventArgs eventArgs)
    {
        /*eventArgs.X -= this.fLeft;
        eventArgs.Y -= this.fTop;*/

        BaseControl ctl = this.getClientControl(this.fMouseX, this.fMouseY);
        if (this.fActiveControl != null && (this.fActiveControl.ControlStyle.contains(ControlStyles.csCaptureMouse))) {
            this.fActiveControl.processMouseWheel(eventArgs);
        } else {
            if (ctl != null) {
                ctl.processMouseWheel(eventArgs);
            } else {
                if (this.Enabled) {
                    this.doMouseWheelEvent(eventArgs);
                }
            }
        }
    }

    protected final void repaint(BaseScreen screen)
    {
        try {
            if (this.fStyleChanging) {
                return;
            }

            this.adjustAbsCoords();
            this.resetAbsClip(screen);

            this.doPaintEvent(screen);

            int num = this.fControls.getCount();
            for (int i = 0; i < num; i++) {
                BaseControl ctl = this.fControls.get(i);
                if (ctl.fVisible) {
                    ctl.repaint(screen);
                }
            }

            this.resetAbsClip(screen);

            if (this.fOwner == null) {
                BaseImage cur = this.getSubControl(this.fMouseX, this.fMouseY).getCursor();
                if (cur != null) {
                    screen.drawImage(this.fMouseX, this.fMouseY, 0, 0, cur.Width, cur.Height, cur, 255);
                }

                BaseDragObject dragObject = BaseControl.DragObject;
                if (dragObject != null) {
                    dragObject.draw(screen);
                }
            }
        } catch (Exception ex) {
            Logger.write("BaseControl.repaint(" + this.getClass().getTypeName() + "): " + ex.getMessage());
        }
    }

    public void update(long time)
    {
        try {
            int num = this.fControls.getCount();
            for (int i = 0; i < num; i++) {
                BaseControl ctl = this.fControls.get(i);
                if (ctl.fVisible) {
                    ctl.update(time);
                }
            }
        } catch (Exception ex) {
            Logger.write("BaseControl.update(" + this.getClass().getTypeName() + "): " + ex.getMessage());
        }
    }
}
