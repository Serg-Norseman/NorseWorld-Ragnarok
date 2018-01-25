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

using System;
using BSLib;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public delegate void IDragDropEvent(object sender, object source, int X, int Y);
    public delegate void IDragEndEvent(object sender, object target, int X, int Y);
    public delegate void IDragOverEvent(object sender, object source, int X, int Y, ref bool  refAccept);
    public delegate void IDragStartEvent(object sender, ref BaseDragObject  refDragObject);
    public delegate void IKeyPressEvent(object Sender, KeyPressEventArgs eventArgs);
    public delegate void IMouseMoveEvent(object Sender, MouseMoveEventArgs eventArgs);
    public delegate void IMouseWheelEvent(object sender, MouseWheelEventArgs eventArgs);
    public delegate void IMouseEvent(object Sender, MouseEventArgs eventArgs);
    public delegate void IPaintEvent(object sender, BaseScreen screen);
    public delegate void IKeyEvent(object Sender, KeyEventArgs eventArgs);

    public sealed class ControlStyles : FlagSet
    {
        public const int сsAcceptsControls = 0;
        public const int сsCaptureMouse = 1;
        public const int сsOpaque = 2;

        public ControlStyles(params int[] args)
            : base(args)
        {
        }
    }

    public abstract class BaseControl : BaseObject
    {
        public const int DRAGSTATE_ENTER = 0;
        public const int DRAGSTATE_LEAVE = 1;
        public const int DRAGSTATE_MOVE = 2;

        public const int DRAGMODE_MANUAL = 0;
        public const int DRAGMODE_AUTOMATIC = 1;

        private const int MouseDragThreshold = 5;

        private static BaseControl DragControl;

        public static BaseDragObject DragObject;

        protected int fMouseX;
        protected int fMouseY;

        private int fAbsLeft;
        private int fAbsTop;
        private BaseControl fActiveControl;
        private string fCaption;
        private ExtList<BaseControl> fControls;
        private BaseImage fCursor;
        private int fDragMode;
        private Font fFont;
        private int fHeight;
        private int fLangResID;
        private int fLeft;
        private readonly BaseControl fOwner;
        private int fStyle;
        private bool fStyleChanging;
        private int fTop;
        private bool fVisible;
        private int fWidth;

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
        public bool Enabled;
        public string Hint;
        public bool ShowHints;
        public int Tag;

        protected BaseControl(BaseControl owner)
        {
            ControlStyle = new ControlStyles(ControlStyles.сsOpaque);

            fControls = new ExtList<BaseControl>(true);
            fActiveControl = null;
            fOwner = owner;
            fCursor = null;
            Enabled = true;
            fVisible = true;
            Hint = "";
            ShowHints = false;
            fLangResID = -1;

            if (fOwner != null) {
                fOwner.fControls.Add(this);
            }
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                if (fOwner != null) {
                    fOwner.fControls.Extract(this);
                    fOwner.fControls.Pack();
                }
                fControls.Dispose();
            }
            base.Dispose(disposing);
        }

        public int Left
        {
            get {
                return fLeft;
            }
            set {
                if (fLeft != value) {
                    fLeft = value;
                    DoResizeEvent();
                }
            }
        }


        public int Top
        {
            get {
                return fTop;
            }
            set {
                if (fTop != value) {
                    fTop = value;
                    DoResizeEvent();
                }
            }
        }


        public int Height
        {
            get {
                return fHeight;
            }
            set {
                if (fHeight != value) {
                    fHeight = value;
                    DoResizeEvent();
                }
            }
        }


        public int Width
        {
            get {
                return fWidth;
            }
            set {
                if (fWidth != value) {
                    fWidth = value;
                    DoResizeEvent();
                }
            }
        }


        public ExtRect ClientRect
        {
            get {
                return ExtRect.Create(0, 0, fWidth - 1, fHeight - 1);
            }
        }

        private void AdjustAbsCoords()
        {
            fAbsLeft = fLeft;
            fAbsTop = fTop;

            if (fOwner != null) {
                fAbsLeft += fOwner.fAbsLeft;
                fAbsTop += fOwner.fAbsTop;
            }
        }

        private void ResetAbsClip(BaseScreen screen)
        {
            int al = fAbsLeft;
            int at = fAbsTop;
            screen.SetOffset(al, at);
            ExtRect absRect = ExtRect.Create(al, at, al + fWidth - 1, at + fHeight - 1);
            screen.InitClip(absRect);
        }

        public ExtRect Bounds
        {
            get {
                return ExtRect.Create(fLeft, fTop, fLeft + fWidth - 1, fTop + fHeight - 1);
            }
            set {
                int L = value.Left;
                int T = value.Top;
                int H = value.Height;
                int W = value.Width;
                if (L != fLeft || T != fTop || H != fHeight || W != fWidth) {
                    fLeft = L;
                    fTop = T;
                    fHeight = H;
                    fWidth = W;
                    DoResizeEvent();
                }
            }
        }


        public virtual ExtRect IntRect
        {
            get {
                return ExtRect.Create(1, 1, fWidth - 2, fHeight - 2);
            }
        }

        public virtual string Caption
        {
            get {
                return fCaption;
            }
            set {
                if ((fCaption != value)) {
                    fCaption = value;
                }
            }
        }


        public int DragMode
        {
            get {
                return fDragMode;
            }
            set {
                fDragMode = value;
            }
        }


        public int LangResID
        {
            get {
                return fLangResID;
            }
            set {
                fLangResID = value;
            }
        }


        public BaseControl ActiveControl
        {
            get {
                return fActiveControl;
            }
            set {
                if (fOwner != null) {
                    fOwner.ActiveControl = this;
                }
                if (fActiveControl != null) {
                    fActiveControl.DoExitEvent();
                }
                fActiveControl = value;
                if (fActiveControl != null) {
                    fActiveControl.DoEnterEvent();
                }
            }
        }


        public ExtList<BaseControl> Controls
        {
            get {
                return fControls;
            }
        }

        public ExtPoint MousePoint
        {
            get {
                return new ExtPoint(fMouseX, fMouseY);
            }
        }

        public BaseControl Owner
        {
            get {
                return fOwner;
            }
        }

        public virtual int Style
        {
            get {
                return fStyle;
            }
            set {
                fStyleChanging = true;
                fStyle = value;
                DoStyleChanged();
                
                int num = fControls.Count;
                for (int i = 0; i < num; i++) {
                    fControls[i].Style = value;
                }
                fStyleChanging = false;
            }
        }


        public bool Visible
        {
            get {
                return fVisible;
            }
            set {
                if (fVisible != value) {
                    fVisible = value;
                    
                    if (fVisible) {
                        DoShowEvent();
                    } else {
                        if (fOwner != null && fOwner.ActiveControl != null && fOwner.ActiveControl.Equals(this)) {
                            Owner.ActiveControl = null;
                        }
                        DoHideEvent();
                    }
                }
            }
        }


        public BaseImage Cursor
        {
            get {
                BaseImage result = null;
                if (fCursor != null) {
                    result = fCursor;
                } else {
                    if (fOwner != null) {
                        result = fOwner.Cursor;
                    }
                }
                return result;
            }
            set {
                fCursor = value;
            }
        }


        public Font Font
        {
            get {
                Font result = null;
                if (fFont != null) {
                    result = fFont;
                } else {
                    if (fOwner != null) {
                        result = fOwner.Font;
                    }
                }
                return result;
            }
            set {
                fFont = value;
                BaseMainWindow mainWnd = MainWindow;
                mainWnd.Screen.Font = value;
            }
        }


        private bool KeyPreview
        {
            get {
                BaseWindow wnd = (this is BaseWindow) ? (BaseWindow)this : null;
                return (wnd != null) ? wnd.HasStyle(WindowStyles.wsKeyPreview) : false;
            }
        }

        private bool InClientRect(int aX, int aY)
        {
            int right = fLeft + fWidth - 1;
            int bottom = fTop + fHeight - 1;
            return aX >= fLeft && aX <= right && aY >= fTop && aY <= bottom;
        }

        private BaseControl GetClientControl(int aX, int aY)
        {
            for (int i = fControls.Count - 1; i >= 0; i--) {
                BaseControl ctl = ((BaseControl)fControls[i]);
                if (ctl.fVisible && ctl.InClientRect(aX, aY)) {
                    return ctl;
                }
            }

            return null;
        }

        public BaseControl GetSubControl(int aX, int aY)
        {
            aX -= fLeft;
            aY -= fTop;

            BaseControl ctl = GetClientControl(aX, aY);
            BaseControl result;
            if (ctl == null) {
                result = this;
            } else {
                result = ctl.GetSubControl(aX, aY);
            }
            return result;
        }

        private void ExDragInit(bool immediate)
        {
            BaseMainWindow mainWnd = MainWindow;
            DragControl = this;
            try {
                BaseDragObject dObject = null;
                DragControl.DoDragStartEvent(ref dObject);

                if (DragControl == null) {
                    return;
                }

                if (dObject == null) {
                    dObject = new BaseDragObject();
                }
                DragObject = dObject;
                DragObject.DragTarget = null;
                DragObject.DragStartPos = mainWnd.MousePoint;
                DragObject.DragPos = DragObject.DragStartPos;

                if (immediate) {
                    DragObject.ActiveDrag = true;
                } else {
                    DragObject.ActiveDrag = false;
                }

                if (DragObject.ActiveDrag) {
                    ExDragTo(DragObject.DragStartPos);
                }
            } catch (Exception e) {
                DragControl = null;
                throw e;
            }
        }

        private static bool ExDoDragOver(int dragState)
        {
            bool result = false;
            if (DragObject.DragTarget != null) {
                bool accepted = false;
                DragObject.DragTarget.DoDragOverEvent(DragControl,
                    DragObject.DragPos.X, DragObject.DragPos.Y, dragState, ref accepted);

                result = accepted;
            }
            return result;
        }

        public static void ExDragDone(bool Drop)
        {
            if (DragObject != null && !DragObject.Cancelling) {
                BaseDragObject DragSave = DragObject;
                try {
                    try {
                        DragObject.Cancelling = true;
                        ExtPoint TargetPos = ExtPoint.Empty;

                        if (DragObject.DragTarget != null && DragObject.DragTarget != null) {
                            TargetPos = DragObject.DragPos;
                        }

                        bool Accepted = (DragObject.DragTarget != null & Drop) && DragObject.ActiveDrag && ExDoDragOver(DRAGSTATE_LEAVE);

                        if (Accepted) {
                            DragControl.DoDragEndEvent(DragObject.DragTarget, TargetPos.X, TargetPos.Y);
                            DragObject.DragTarget.DoDragDropEvent(DragControl, TargetPos.X, TargetPos.Y);
                        }
                    } finally {
                        if (DragSave != null) {
                            DragSave.Cancelling = false;
                        }
                        DragObject = null;
                    }
                } finally {
                    DragControl = null;
                    if (DragSave != null) {
                        DragSave = null;
                    }
                }
            }
        }

        public static void ExDragTo(ExtPoint Pos)
        {
            if (DragObject.ActiveDrag || Math.Abs(DragObject.DragStartPos.X - Pos.X) >= MouseDragThreshold || Math.Abs(DragObject.DragStartPos.Y - Pos.Y) >= MouseDragThreshold) {
                BaseMainWindow mainWnd = DragControl.MainWindow;
                BaseControl Target = mainWnd.GetSubControl(Pos.X, Pos.Y);
                DragObject.ActiveDrag = true;
                if (!Target.Equals(DragObject.DragTarget)) {
                    ExDoDragOver(DRAGSTATE_LEAVE);
                    if (DragObject == null) {
                        return;
                    }
                    DragObject.DragTarget = Target;
                    DragObject.DragPos = Pos;
                    ExDoDragOver(DRAGSTATE_ENTER);
                    if (DragObject == null) {
                        return;
                    }
                }
                DragObject.DragPos = Pos;
                DragObject.Accepted = ExDoDragOver(DRAGSTATE_MOVE);
            }
        }

        protected virtual void DoClickEvent()
        {
            if (OnClick != null) {
                OnClick.Invoke(this);
            }
        }

        protected virtual void DoDragDropEvent(object Source, int X, int Y)
        {
            if (OnDragDrop != null) {
                OnDragDrop.Invoke(this, Source, X, Y);
            }
        }

        protected virtual void DoDragEndEvent(object Target, int X, int Y)
        {
            if (OnDragEnd != null) {
                OnDragEnd.Invoke(this, Target, X, Y);
            }
        }

        protected virtual void DoDragOverEvent(object source, int X, int Y, int dragState, ref bool  refAccept)
        {
            refAccept = false;
            if (OnDragOver != null) {
                refAccept = true;
                OnDragOver.Invoke(this, source, X, Y, ref refAccept);
            }
        }

        protected virtual void DoDragStartEvent(ref BaseDragObject  dragObject)
        {
            if (OnDragStart != null) {
                OnDragStart.Invoke(this, ref dragObject);
            }
        }

        protected virtual void DoEnterEvent()
        {
            if (OnEnter != null) {
                OnEnter.Invoke(this);
            }
        }

        protected virtual void DoExitEvent()
        {
            if (OnExit != null) {
                OnExit.Invoke(this);
            }
        }

        protected virtual void DoHideEvent()
        {
            int num = fControls.Count;
            for (int i = 0; i < num; i++) {
                BaseControl ctl = fControls[i];
                if (ctl.fVisible) {
                    ctl.DoHideEvent();
                }
            }
            if (OnHide != null) {
                OnHide.Invoke(this);
            }
        }

        protected virtual void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            if (OnKeyDown != null) {
                OnKeyDown.Invoke(this, eventArgs);
            }
        }

        protected virtual void DoKeyPressEvent(KeyPressEventArgs eventArgs)
        {
            if (OnKeyPress != null) {
                OnKeyPress.Invoke(this, eventArgs);
            }
        }

        protected virtual void DoKeyUpEvent(KeyEventArgs eventArgs)
        {
            if (OnKeyUp != null) {
                OnKeyUp.Invoke(this, eventArgs);
            }
        }

        protected virtual void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            if (OnMouseDown != null) {
                OnMouseDown.Invoke(this, eventArgs);
            }
        }

        protected virtual void DoMouseMoveEvent(MouseMoveEventArgs eventArgs)
        {
            if (OnMouseMove != null) {
                OnMouseMove.Invoke(this, eventArgs);
            }
        }

        protected virtual void DoMouseUpEvent(MouseEventArgs eventArgs)
        {
            if (OnMouseUp != null) {
                OnMouseUp.Invoke(this, eventArgs);
            }
        }

        protected virtual void DoMouseWheelEvent(MouseWheelEventArgs eventArgs)
        {
            if (OnMouseWheel != null) {
                OnMouseWheel.Invoke(this, eventArgs);
            }
        }

        protected virtual void DoPaintEvent(BaseScreen screen)
        {
            if (OnPaint != null) {
                OnPaint.Invoke(this, screen);
            }
        }

        protected virtual void DoResizeEvent()
        {
            if (OnResize != null) {
                OnResize.Invoke(this);
            }
        }

        protected virtual void DoShowEvent()
        {
            int num = fControls.Count;
            for (int i = 0; i < num; i++) {
                BaseControl ctl = fControls[i];
                if (ctl.fVisible) {
                    ctl.DoShowEvent();
                }
            }

            if (OnShow != null) {
                OnShow.Invoke(this);
            }
        }

        protected virtual void DoStyleChanged()
        {
        }

        protected BaseMainWindow MainWindow
        {
            get {
                BaseMainWindow result;
                if (fOwner == null) {
                    if (this is BaseMainWindow) {
                        result = (BaseMainWindow)this;
                    } else {
                        result = null;
                    }
                } else {
                    if (fOwner is BaseMainWindow) {
                        result = (BaseMainWindow)fOwner;
                    } else {
                        result = fOwner.MainWindow;
                    }
                }
                return result;
            }
        }

        public void BeginDrag(bool immediate)
        {
            if (!(this is BaseWindow) && DragControl == null) {
                ExDragInit(immediate);
            }
        }

        public void EndDrag(bool drop)
        {
            if (DragControl.Equals(this)) {
                ExDragDone(drop);
            }
        }

        public bool Active
        {
            get {
                return Owner != null && Owner.ActiveControl.Equals(this);
            }
        }

        public virtual void ChangeLang()
        {
            if (OnLangChange != null) {
                OnLangChange.Invoke(this);
            }

            int num = fControls.Count;
            for (int i = 0; i < num; i++) {
                fControls[i].ChangeLang();
            }
        }

        public void ProcessKeyDown(KeyEventArgs eventArgs)
        {
            if (fActiveControl != null) {
                if (KeyPreview) {
                    DoKeyDownEvent(eventArgs);
                }
                fActiveControl.ProcessKeyDown(eventArgs);
            } else {
                if (Enabled) {
                    DoKeyDownEvent(eventArgs);
                }
            }
        }

        public void ProcessKeyPress(KeyPressEventArgs eventArgs)
        {
            if (fActiveControl != null) {
                if (KeyPreview) {
                    DoKeyPressEvent(eventArgs);
                }
                fActiveControl.ProcessKeyPress(eventArgs);
            } else {
                if (Enabled) {
                    DoKeyPressEvent(eventArgs);
                }
            }
        }

        public void ProcessKeyUp(KeyEventArgs eventArgs)
        {
            if (fActiveControl != null) {
                if (KeyPreview) {
                    DoKeyUpEvent(eventArgs);
                }
                fActiveControl.ProcessKeyUp(eventArgs);
            } else {
                if (Enabled) {
                    DoKeyUpEvent(eventArgs);
                }
            }
        }

        public void ProcessMouseDown(MouseEventArgs eventArgs)
        {
            eventArgs.X -= fLeft;
            eventArgs.Y -= fTop;

            if (DragObject == null) {
                BaseControl ctl = GetClientControl(eventArgs.X, eventArgs.Y);
                BaseControl activeCtl = fActiveControl;

                if (this is BaseWindow && activeCtl != null && activeCtl is BaseWindow && (((BaseWindow)activeCtl).HasStyle(WindowStyles.wsModal))) {
                    if (activeCtl.Equals(ctl)) {
                        activeCtl.ProcessMouseDown(eventArgs);
                    }
                } else {
                    if (Owner != null && (Owner.ControlStyle.Contains(ControlStyles.сsAcceptsControls))) {
                        Owner.ActiveControl = this;
                    }
                    fActiveControl = null;
                    if (ctl != null) {
                        ctl.ProcessMouseDown(eventArgs);
                    } else {
                        if (Enabled) {
                            DoMouseDownEvent(eventArgs);
                        }
                        if (fDragMode == DRAGMODE_AUTOMATIC) {
                            BeginDrag(false);
                        }
                    }
                }
            }
        }

        public void ProcessMouseMove(MouseMoveEventArgs eventArgs)
        {
            eventArgs = (MouseMoveEventArgs)eventArgs.Clone();

            eventArgs.X -= fLeft;
            eventArgs.Y -= fTop;

            fMouseX = eventArgs.X;
            fMouseY = eventArgs.Y;

            if (DragObject != null) {
                ExDragTo(new ExtPoint(eventArgs.X, eventArgs.Y));
            } else {
                BaseControl ctl = GetClientControl(eventArgs.X, eventArgs.Y);
                BaseControl activeCtl = fActiveControl;

                if (this is BaseWindow && activeCtl != null && activeCtl is BaseWindow && (((BaseWindow)activeCtl).HasStyle(WindowStyles.wsModal))) {
                    if (activeCtl.Equals(ctl)) {
                        activeCtl.ProcessMouseMove(eventArgs);
                    }
                } else {
                    if (fActiveControl != null) {
                        fActiveControl.ProcessMouseMove(eventArgs);
                    }
                    if (ctl != null) {
                        ctl.ProcessMouseMove(eventArgs);
                    }
                    if (Enabled) {
                        DoMouseMoveEvent(eventArgs);
                    }
                }
            }
        }

        public void ProcessMouseUp(MouseEventArgs eventArgs)
        {
            eventArgs.X -= fLeft;
            eventArgs.Y -= fTop;

            if (DragObject != null) {
                ExDragDone(true);
            } else {
                BaseControl ctl = GetClientControl(eventArgs.X, eventArgs.Y);
                BaseControl activeCtl = fActiveControl;

                if (this is BaseWindow && activeCtl != null && activeCtl is BaseWindow && (((BaseWindow)activeCtl).HasStyle(WindowStyles.wsModal))) {
                    if (activeCtl.Equals(ctl)) {
                        activeCtl.ProcessMouseUp(eventArgs);
                    }
                } else {
                    if (fActiveControl != null && (fActiveControl.ControlStyle.Contains(ControlStyles.сsCaptureMouse))) {
                        fActiveControl.ProcessMouseUp(eventArgs);
                    } else {
                        if (ctl != null) {
                            ctl.ProcessMouseUp(eventArgs);
                        } else {
                            if (Enabled) {
                                DoMouseUpEvent(eventArgs);
                            }
                        }
                    }
                }
            }
        }

        public void ProcessMouseWheel(MouseWheelEventArgs eventArgs)
        {
            /*eventArgs.X -= this.fLeft;
            eventArgs.Y -= this.fTop;*/

            BaseControl ctl = GetClientControl(fMouseX, fMouseY);
            if (fActiveControl != null && (fActiveControl.ControlStyle.Contains(ControlStyles.сsCaptureMouse))) {
                fActiveControl.ProcessMouseWheel(eventArgs);
            } else {
                if (ctl != null) {
                    ctl.ProcessMouseWheel(eventArgs);
                } else {
                    if (Enabled) {
                        DoMouseWheelEvent(eventArgs);
                    }
                }
            }
        }

        protected void Repaint(BaseScreen screen)
        {
            try {
                if (fStyleChanging) {
                    return;
                }

                AdjustAbsCoords();
                ResetAbsClip(screen);

                DoPaintEvent(screen);

                int num = fControls.Count;
                for (int i = 0; i < num; i++) {
                    BaseControl ctl = fControls[i];
                    if (ctl.fVisible) {
                        ctl.Repaint(screen);
                    }
                }

                ResetAbsClip(screen);

                if (fOwner == null) {
                    BaseImage cur = GetSubControl(fMouseX, fMouseY).Cursor;
                    if (cur != null) {
                        screen.DrawImage(fMouseX, fMouseY, 0, 0, cur.Width, cur.Height, cur, 255);
                    }

                    BaseDragObject dragObject = DragObject;
                    if (dragObject != null) {
                        dragObject.Draw(screen);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("BaseControl.repaint(" + GetType().Name + "): " + ex.Message);
            }
        }

        public virtual void Update(long time)
        {
            try {
                int num = fControls.Count;
                for (int i = 0; i < num; i++) {
                    BaseControl ctl = fControls[i];
                    if (ctl.fVisible) {
                        ctl.Update(time);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("BaseControl.update(" + GetType().Name + "): " + ex.Message);
            }
        }
    }
}
