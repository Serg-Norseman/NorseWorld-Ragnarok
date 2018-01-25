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

using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public sealed class WindowStyles : FlagSet
    {
        public const int wsMoveable = 0;
        public const int wsScreenCenter = 1;
        public const int wsModal = 2;
        public const int wsKeyPreview = 3;

        public WindowStyles(params int[] args)
            : base(args)
        {
        }
    }

    public abstract class BaseWindow : BaseControl
    {
        private int fMouseOldX;
        private int fMouseOldY;

        public WindowStyles WindowStyle;

        protected BaseWindow(BaseControl owner)
            : base(owner)
        {
            Visible = false;
            ControlStyle.Include(ControlStyles.сsAcceptsControls);
            WindowStyle = new WindowStyles(WindowStyles.wsMoveable);
        }

        public bool HasStyle(int style)
        {
            return WindowStyle.Contains(style);
        }

        private void SetTopMost()
        {
            BaseControl owner = Owner;
            if (owner != null && owner is BaseWindow) {
                int num = owner.Controls.Count - 2;
                for (int i = owner.Controls.IndexOf(this); i <= num; i++) {
                    owner.Controls.Exchange(i, i + 1);
                }
            }
        }

        protected virtual void DoClose()
        {
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            if (eventArgs.Button == MouseButton.mbLeft) {
                fMouseOldX = eventArgs.X;
                fMouseOldY = eventArgs.Y;
            }
            base.DoMouseDownEvent(eventArgs);
        }

        protected override void DoMouseMoveEvent(MouseMoveEventArgs eventArgs)
        {
            if ((HasStyle(WindowStyles.wsMoveable)) && Owner != null && (eventArgs.Shift.Contains(ShiftStates.SsLeft))) {
                Left = Left + (fMouseX - fMouseOldX);
                Top = Top + (fMouseY - fMouseOldY);
            }
            base.DoMouseMoveEvent(eventArgs);
        }

        protected override void DoShowEvent()
        {
            base.DoShowEvent();

            BaseControl owner = Owner;
            if (owner != null) {
                if (HasStyle(WindowStyles.wsScreenCenter) && owner is BaseMainWindow) {
                    Left = (owner.Width - Width) / 2;
                    Top = (owner.Height - Height) / 2;
                }

                if (HasStyle(WindowStyles.wsModal)) {
                    owner.ActiveControl = this;
                }
            }
        }

        public void Hide()
        {
            if (Owner != null) {
                Visible = false;
            }
        }

        public void Show()
        {
            if (Owner != null) {
                SetTopMost();
                Visible = true;
            }
        }
    }
}
