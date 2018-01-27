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
using ZRLib.Engine.sdl2;
using ZRLib.Core;
using ZRLib.Engine;

namespace NWR.GUI
{
    public abstract class BaseMainWindow : BaseWindow, IMainWindow
    {
        private sealed class KeyCapInfo
        {
            public string Sign;
            public Keys Key;
            public ShiftStates Shift;

            public KeyCapInfo(string sign, Keys key, ShiftStates shift)
            {
                Sign = sign;
                Key = key;
                Shift = shift;
            }
        }

        private static readonly KeyCapInfo[] KeyCaps;

        private const int DefHintPause = 500;
        private const int DefHintHidePause = 3000;

        private const int mkcShift = 3;
        private const int mkcCtrl = 4;
        private const int mkcAlt = 5;

        private readonly BaseHintWindow fHintWindow;
        private readonly BaseSystem fSystem;

        private int fFrameCount;
        private long fFrameStartTime;
        private ExtPoint fPrevPos;
        private long fPrevTime;

        public float FPS;

        static BaseMainWindow()
        {
            KeyCaps = new KeyCapInfo[80];
            KeyCaps[0] = new KeyCapInfo("", Keys.GK_UNK, new ShiftStates());
            KeyCaps[1] = new KeyCapInfo("Tab", Keys.GK_TAB, new ShiftStates());
            KeyCaps[2] = new KeyCapInfo("Enter", Keys.GK_RETURN, new ShiftStates());
            KeyCaps[3] = new KeyCapInfo("Shift+", Keys.GK_UNK, new ShiftStates(ShiftStates.SsShift));
            KeyCaps[4] = new KeyCapInfo("Ctrl+", Keys.GK_UNK, new ShiftStates(ShiftStates.SsCtrl));
            KeyCaps[5] = new KeyCapInfo("Alt+", Keys.GK_UNK, new ShiftStates(ShiftStates.SsAlt));
            KeyCaps[6] = new KeyCapInfo("Esc", Keys.GK_ESCAPE, new ShiftStates());
            KeyCaps[7] = new KeyCapInfo("Space", Keys.GK_SPACE, new ShiftStates());
            KeyCaps[8] = new KeyCapInfo("PgUp", Keys.GK_PRIOR, new ShiftStates());
            KeyCaps[9] = new KeyCapInfo("PgDn", Keys.GK_NEXT, new ShiftStates());
            KeyCaps[10] = new KeyCapInfo("End", Keys.GK_END, new ShiftStates());
            KeyCaps[11] = new KeyCapInfo("Home", Keys.GK_HOME, new ShiftStates());
            KeyCaps[12] = new KeyCapInfo("Left", Keys.GK_LEFT, new ShiftStates());
            KeyCaps[13] = new KeyCapInfo("Up", Keys.GK_UP, new ShiftStates());
            KeyCaps[14] = new KeyCapInfo("Right", Keys.GK_RIGHT, new ShiftStates());
            KeyCaps[15] = new KeyCapInfo("Down", Keys.GK_DOWN, new ShiftStates());
            KeyCaps[16] = new KeyCapInfo("Ins", Keys.GK_INSERT, new ShiftStates());
            KeyCaps[17] = new KeyCapInfo("Del", Keys.GK_DELETE, new ShiftStates());
            KeyCaps[18] = new KeyCapInfo(",", Keys.GK_COMMA, new ShiftStates());
            KeyCaps[19] = new KeyCapInfo(".", Keys.GK_PERIOD, new ShiftStates());
            KeyCaps[20] = new KeyCapInfo("0", Keys.GK_0, new ShiftStates());
            KeyCaps[21] = new KeyCapInfo("1", Keys.GK_1, new ShiftStates());
            KeyCaps[22] = new KeyCapInfo("2", Keys.GK_2, new ShiftStates());
            KeyCaps[23] = new KeyCapInfo("3", Keys.GK_3, new ShiftStates());
            KeyCaps[24] = new KeyCapInfo("4", Keys.GK_4, new ShiftStates());
            KeyCaps[25] = new KeyCapInfo("5", Keys.GK_5, new ShiftStates());
            KeyCaps[26] = new KeyCapInfo("6", Keys.GK_6, new ShiftStates());
            KeyCaps[27] = new KeyCapInfo("7", Keys.GK_7, new ShiftStates());
            KeyCaps[28] = new KeyCapInfo("8", Keys.GK_8, new ShiftStates());
            KeyCaps[29] = new KeyCapInfo("9", Keys.GK_9, new ShiftStates());
            KeyCaps[30] = new KeyCapInfo("<", Keys.GK_LESS, new ShiftStates());
            KeyCaps[31] = new KeyCapInfo(">", Keys.GK_GREATER, new ShiftStates());
            KeyCaps[32] = new KeyCapInfo("A", Keys.GK_A, new ShiftStates());
            KeyCaps[33] = new KeyCapInfo("B", Keys.GK_B, new ShiftStates());
            KeyCaps[34] = new KeyCapInfo("C", Keys.GK_C, new ShiftStates());
            KeyCaps[35] = new KeyCapInfo("D", Keys.GK_D, new ShiftStates());
            KeyCaps[36] = new KeyCapInfo("E", Keys.GK_E, new ShiftStates());
            KeyCaps[37] = new KeyCapInfo("F", Keys.GK_F, new ShiftStates());
            KeyCaps[38] = new KeyCapInfo("G", Keys.GK_G, new ShiftStates());
            KeyCaps[39] = new KeyCapInfo("H", Keys.GK_H, new ShiftStates());
            KeyCaps[40] = new KeyCapInfo("I", Keys.GK_I, new ShiftStates());
            KeyCaps[41] = new KeyCapInfo("J", Keys.GK_J, new ShiftStates());
            KeyCaps[42] = new KeyCapInfo("K", Keys.GK_K, new ShiftStates());
            KeyCaps[43] = new KeyCapInfo("L", Keys.GK_L, new ShiftStates());
            KeyCaps[44] = new KeyCapInfo("M", Keys.GK_M, new ShiftStates());
            KeyCaps[45] = new KeyCapInfo("N", Keys.GK_N, new ShiftStates());
            KeyCaps[46] = new KeyCapInfo("O", Keys.GK_O, new ShiftStates());
            KeyCaps[47] = new KeyCapInfo("P", Keys.GK_P, new ShiftStates());
            KeyCaps[48] = new KeyCapInfo("Q", Keys.GK_Q, new ShiftStates());
            KeyCaps[49] = new KeyCapInfo("R", Keys.GK_R, new ShiftStates());
            KeyCaps[50] = new KeyCapInfo("S", Keys.GK_S, new ShiftStates());
            KeyCaps[51] = new KeyCapInfo("T", Keys.GK_T, new ShiftStates());
            KeyCaps[52] = new KeyCapInfo("U", Keys.GK_U, new ShiftStates());
            KeyCaps[53] = new KeyCapInfo("V", Keys.GK_V, new ShiftStates());
            KeyCaps[54] = new KeyCapInfo("W", Keys.GK_W, new ShiftStates());
            KeyCaps[55] = new KeyCapInfo("X", Keys.GK_X, new ShiftStates());
            KeyCaps[56] = new KeyCapInfo("Y", Keys.GK_Y, new ShiftStates());
            KeyCaps[57] = new KeyCapInfo("Z", Keys.GK_Z, new ShiftStates());
            KeyCaps[58] = new KeyCapInfo("F1", Keys.GK_F1, new ShiftStates());
            KeyCaps[59] = new KeyCapInfo("F2", Keys.GK_F2, new ShiftStates());
            KeyCaps[60] = new KeyCapInfo("F3", Keys.GK_F3, new ShiftStates());
            KeyCaps[61] = new KeyCapInfo("F4", Keys.GK_F4, new ShiftStates());
            KeyCaps[62] = new KeyCapInfo("F5", Keys.GK_F5, new ShiftStates());
            KeyCaps[63] = new KeyCapInfo("F6", Keys.GK_F6, new ShiftStates());
            KeyCaps[64] = new KeyCapInfo("F7", Keys.GK_F7, new ShiftStates());
            KeyCaps[65] = new KeyCapInfo("F8", Keys.GK_F8, new ShiftStates());
            KeyCaps[66] = new KeyCapInfo("F9", Keys.GK_F9, new ShiftStates());
            KeyCaps[67] = new KeyCapInfo("F10", Keys.GK_F10, new ShiftStates());
            KeyCaps[68] = new KeyCapInfo("F11", Keys.GK_F11, new ShiftStates());
            KeyCaps[69] = new KeyCapInfo("F12", Keys.GK_F12, new ShiftStates());
            KeyCaps[70] = new KeyCapInfo("BkSp", Keys.GK_BACK, new ShiftStates());
            KeyCaps[71] = new KeyCapInfo("NumPad1", Keys.GK_NUMPAD1, new ShiftStates());
            KeyCaps[72] = new KeyCapInfo("NumPad2", Keys.GK_NUMPAD2, new ShiftStates());
            KeyCaps[73] = new KeyCapInfo("NumPad3", Keys.GK_NUMPAD3, new ShiftStates());
            KeyCaps[74] = new KeyCapInfo("NumPad4", Keys.GK_NUMPAD4, new ShiftStates());
            KeyCaps[75] = new KeyCapInfo("NumPad5", Keys.GK_NUMPAD5, new ShiftStates());
            KeyCaps[76] = new KeyCapInfo("NumPad6", Keys.GK_NUMPAD6, new ShiftStates());
            KeyCaps[77] = new KeyCapInfo("NumPad7", Keys.GK_NUMPAD7, new ShiftStates());
            KeyCaps[78] = new KeyCapInfo("NumPad8", Keys.GK_NUMPAD8, new ShiftStates());
            KeyCaps[79] = new KeyCapInfo("NumPad9", Keys.GK_NUMPAD9, new ShiftStates());
        }

        private static string GetKeySign(Keys key)
        {
            for (int cap = 0; cap < KeyCaps.Length; cap++) {
                if (KeyCaps[cap].Key == key) {
                    return KeyCaps[cap].Sign;
                }
            }

            return "";
        }

        public static string HotKeyToText(HotKey hotKey)
        {
            Keys key = hotKey.Key;
            string keySign = GetKeySign(key);

            string result;
            if (keySign != "") {
                result = "";
                if ((hotKey.Shift.Contains(ShiftStates.SsShift))) {
                    result += KeyCaps[mkcShift].Sign;
                }
                if ((hotKey.Shift.Contains(ShiftStates.SsCtrl))) {
                    result += KeyCaps[mkcCtrl].Sign;
                }
                if ((hotKey.Shift.Contains(ShiftStates.SsAlt))) {
                    result += KeyCaps[mkcAlt].Sign;
                }
                result += keySign;
            } else {
                result = "";
            }

            return result;
        }

        private static bool CompareFront(ref string refText, string front)
        {
            bool result = false;
            if (string.IsNullOrEmpty(refText) || string.IsNullOrEmpty(front)) {
                return result;
            }

            string text = refText;
            if (text.IndexOf(front) == 0) {
                result = true;
                refText = text.Substring(front.Length);
            }

            return result;
        }

        public static HotKey TextToHotKey(string text)
        {
            ShiftStates shift = new ShiftStates();

            while (true) {
                bool res = CompareFront(ref text, KeyCaps[mkcShift].Sign);

                if (res) {
                    shift.Include(ShiftStates.SsShift);
                } else {
                    res = CompareFront(ref text, KeyCaps[mkcCtrl].Sign);

                    if (res) {
                        shift.Include(ShiftStates.SsCtrl);
                    } else {
                        res = !CompareFront(ref text, KeyCaps[mkcAlt].Sign);

                        if (res) {
                            break;
                        }
                        shift.Include(ShiftStates.SsAlt);
                    }
                }
            }

            HotKey result = new HotKey();

            if (text != "") {
                for (int key = 0; key < KeyCaps.Length; key++) {
                    if ((text == KeyCaps[key].Sign)) {
                        result.Key = KeyCaps[key].Key;
                        result.Shift = shift;
                    }
                }
            }

            return result;
        }

        protected BaseMainWindow(int width, int height)
            : base(null)
        {
            fPrevPos = new ExtPoint();
            fFrameCount = 0;
            fFrameStartTime = 0;
            FPS = 0f;
            fSystem = new SDL2System(this, width, height, false);

            Bounds = ExtRect.Create(0, 0, width - 1, height - 1);

            fHintWindow = CreateHintWindow(this);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fHintWindow.Dispose();
                fSystem.Dispose();
            }
            base.Dispose(disposing);
        }

        protected virtual BaseHintWindow CreateHintWindow(BaseMainWindow owner)
        {
            return new BaseHintWindow(owner);
        }

        public BaseScreen Screen
        {
            get { return fSystem.Screen; }
        }

        public bool SysCursorVisible
        {
            get { return fSystem.SysCursorVisible; }
            set { fSystem.SysCursorVisible = value; }
        }


        protected abstract void AppCreate();

        protected abstract void AppDestroy();

        protected override void DoResizeEvent()
        {
            fSystem.Resize(Width, Height);
            base.DoResizeEvent();
        }

        public override string Caption
        {
            set {
                base.Caption = value;
                fSystem.Caption = value;
            }
        }

        public abstract void DoActive(bool active);

        public void Help(string fileName)
        {
            fSystem.Help(fileName);
        }

        private void ProcessHintTick(long time)
        {
            ExtPoint curPos = MousePoint;

            if (fPrevPos.X != curPos.X || fPrevPos.Y != curPos.Y) {
                HideHint();
                fPrevTime = time;
            } else {
                long d = time - fPrevTime;
                if (d >= DefHintPause && !fHintWindow.Visible) {
                    ShowHint();
                    fPrevTime = time;
                }
                if (d >= DefHintHidePause && fHintWindow.Visible) {
                    HideHint();
                    fPrevTime = time;
                }
            }

            fPrevPos = curPos;
        }

        public override void Update(long time)
        {
            base.Update(time);

            ProcessHintTick(time);
            Repaint();
        }

        public void Repaint(int delayInterval)
        {
            BaseSystem.Sleep(delayInterval);
            Repaint();
        }

        public void Repaint()
        {
            BaseScreen scr = Screen;
            if (scr != null) {
                try {
                    scr.BeginPaint();
                    base.Repaint(scr);
                    scr.EndPaint();

                    fFrameCount += 1;
                    long now = BaseSystem.TickCount;
                    if (now > fFrameStartTime + 1000) {
                        FPS = ((1000 * fFrameCount / (now - fFrameStartTime)));
                        fFrameStartTime = now;
                        fFrameCount = 0;
                    }
                } catch (Exception ex) {
                    Logger.Write("BaseMainWindow.repaint(): " + ex.Message);
                }
            }
        }

        public abstract void ProcessGameStep();

        public void Quit()
        {
            fSystem.Quit();
        }

        public void Run()
        {
            try {
                AppCreate();
                try {
                    fSystem.Run();
                } finally {
                    AppDestroy();
                }
            } catch (Exception ex) {
                Logger.Write("BaseMainWindow.run(): " + ex.Message);
            }
        }

        public void HideHint()
        {
            if (fHintWindow.Visible) {
                fHintWindow.Hide();
            }
        }

        public void ShowHint()
        {
            string hint;

            BaseControl ctl = GetSubControl(MousePoint.X, MousePoint.Y);
            if (ctl != null && ctl.ShowHints) {
                hint = ctl.Hint;
            } else {
                hint = Hint;
            }

            if (hint != "" && (!fHintWindow.Visible || fHintWindow.Caption != hint)) {
                int dx;
                int dy;
                if (Cursor == null) {
                    dx = 16;
                    dy = 16;
                } else {
                    dx = Cursor.Width;
                    dy = Cursor.Height;
                }

                fHintWindow.Caption = hint;

                if (MousePoint.X + dx + fHintWindow.Width > Width) {
                    dx = Width - (MousePoint.X + dx + fHintWindow.Width);
                }
                if (MousePoint.Y + dy + fHintWindow.Height > Height) {
                    dy = Height - (MousePoint.Y + dy + fHintWindow.Height);
                }

                fHintWindow.Left = MousePoint.X + dx;
                fHintWindow.Top = MousePoint.Y + dy;
                fHintWindow.Show();
            }
        }

        public static void InitEnvironment(string appName)
        {
            //SDLSystem.EnvironmentInit(AppName);
        }

        public static bool InstanceExists
        {
            get {
                return false; //SDLSystem.InstanceExists();
            }
        }
    }
}
