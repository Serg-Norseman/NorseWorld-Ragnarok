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

import jzrlib.utils.Logger;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.utils.RefObject;
import jzrlib.utils.TextUtils;
import nwr.engine.sdl2.SDL2System;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class BaseMainWindow extends BaseWindow
{
    private static final class KeyCapInfo
    {
        public String Sign;
        public Keys Key;
        public ShiftStates Shift;

        public KeyCapInfo(String sign, Keys key, ShiftStates shift)
        {
            this.Sign = sign;
            this.Key = key;
            this.Shift = shift;
        }
    }

    private static final KeyCapInfo[] KeyCaps;

    private static final int DefHintPause = 500;
    private static final int DefHintHidePause = 3000;
    private static final int mkcShift = 3;
    private static final int mkcCtrl = 4;
    private static final int mkcAlt = 5;

    private final BaseHintWindow fHintWindow;
    private final BaseSystem fSystem;

    private int fFrameCount;
    private long fFrameStartTime;
    private Point fPrevPos;
    private long fPrevTime;

    public float FPS;

    static {
        KeyCaps = new KeyCapInfo[80];
        KeyCaps[0] = new KeyCapInfo("", Keys.GK_UNK, new ShiftStates());
        KeyCaps[1] = new KeyCapInfo("Tab", Keys.GK_TAB, new ShiftStates());
        KeyCaps[2] = new KeyCapInfo("Enter", Keys.GK_RETURN, new ShiftStates());
        KeyCaps[3] = new KeyCapInfo("Shift+", Keys.GK_UNK, new ShiftStates(ShiftStates.ssShift));
        KeyCaps[4] = new KeyCapInfo("Ctrl+", Keys.GK_UNK, new ShiftStates(ShiftStates.ssCtrl));
        KeyCaps[5] = new KeyCapInfo("Alt+", Keys.GK_UNK, new ShiftStates(ShiftStates.ssAlt));
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

    private static String getKeySign(Keys key)
    {
        for (int cap = 0; cap < KeyCaps.length; cap++) {
            if (KeyCaps[cap].Key == key) {
                return KeyCaps[cap].Sign;
            }
        }

        return "";
    }

    public static String HotKeyToText(HotKey hotKey)
    {
        Keys key = hotKey.Key;
        String keySign = BaseMainWindow.getKeySign(key);

        String result;
        if (keySign.compareTo("") != 0) {
            result = "";
            if ((hotKey.Shift.contains(ShiftStates.ssShift))) {
                result += KeyCaps[BaseMainWindow.mkcShift].Sign;
            }
            if ((hotKey.Shift.contains(ShiftStates.ssCtrl))) {
                result += KeyCaps[BaseMainWindow.mkcCtrl].Sign;
            }
            if ((hotKey.Shift.contains(ShiftStates.ssAlt))) {
                result += KeyCaps[BaseMainWindow.mkcAlt].Sign;
            }
            result += keySign;
        } else {
            result = "";
        }

        return result;
    }

    private static boolean compareFront(RefObject<String> refText, String front)
    {
        boolean result = false;
        if (TextUtils.isNullOrEmpty(refText.argValue) || TextUtils.isNullOrEmpty(front)) {
            return result;
        }

        String text = refText.argValue;
        if (text.indexOf(front) == 0) {
            result = true;
            refText.argValue = text.substring(front.length());
        }

        return result;
    }

    public static HotKey TextToHotKey(String text)
    {
        ShiftStates shift = new ShiftStates();

        while (true) {
            RefObject<String> refText = new RefObject<>(text);
            boolean res = BaseMainWindow.compareFront(refText, KeyCaps[BaseMainWindow.mkcShift].Sign);
            text = refText.argValue;

            if (res) {
                shift.include(ShiftStates.ssShift);
            } else {
                RefObject<String> refText2 = new RefObject<>(text);
                res = BaseMainWindow.compareFront(refText2, KeyCaps[BaseMainWindow.mkcCtrl].Sign);
                text = refText2.argValue;

                if (res) {
                    shift.include(ShiftStates.ssCtrl);
                } else {
                    RefObject<String> refText3 = new RefObject<>(text);
                    res = !BaseMainWindow.compareFront(refText3, KeyCaps[BaseMainWindow.mkcAlt].Sign);
                    text = refText3.argValue;

                    if (res) {
                        break;
                    }
                    shift.include(ShiftStates.ssAlt);
                }
            }
        }

        HotKey result = new HotKey();

        if (text.compareTo("") != 0) {
            for (int key = 0; key < KeyCaps.length; key++) {
                if (TextUtils.equals(text, KeyCaps[key].Sign)) {
                    result.Key = KeyCaps[key].Key;
                    result.Shift = shift;
                }
            }
        }

        return result;
    }

    public BaseMainWindow(int width, int height)
    {
        super(null);

        this.fPrevPos = new Point();
        this.fFrameCount = 0;
        this.fFrameStartTime = 0;
        this.FPS = 0f;
        this.fSystem = new SDL2System(this, width, height, false);

        super.setBounds(new Rect(0, 0, width - 1, height - 1));

        this.fHintWindow = this.createHintWindow(this);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.fHintWindow.dispose();
            this.fSystem.dispose();
        }
        super.dispose(disposing);
    }

    protected BaseHintWindow createHintWindow(BaseMainWindow owner)
    {
        return new BaseHintWindow(owner);
    }

    public final BaseScreen getScreen()
    {
        return this.fSystem.getScreen();
    }

    public final boolean getSysCursorVisible()
    {
        return this.fSystem.getSysCursorVisible();
    }

    public final void setSysCursorVisible(boolean value)
    {
        this.fSystem.setSysCursorVisible(value);
    }

    protected abstract void AppCreate();

    protected abstract void AppDestroy();

    @Override
    protected void doResizeEvent()
    {
        this.fSystem.resize(super.getWidth(), super.getHeight());
        super.doResizeEvent();
    }

    @Override
    public void setCaption(String value)
    {
        super.setCaption(value);
        this.fSystem.setCaption(value);
    }

    public abstract void DoActive(boolean active);

    public final void help(String fileName)
    {
        this.fSystem.help(fileName);
    }

    private void processHintTick(long time)
    {
        Point curPos = super.getMousePoint();

        if (this.fPrevPos.X != curPos.X || this.fPrevPos.Y != curPos.Y) {
            this.hideHint();
            this.fPrevTime = time;
        } else {
            long d = time - this.fPrevTime;
            if (d >= (long) BaseMainWindow.DefHintPause && !this.fHintWindow.getVisible()) {
                this.showHint();
                this.fPrevTime = time;
            }
            if (d >= (long) BaseMainWindow.DefHintHidePause && this.fHintWindow.getVisible()) {
                this.hideHint();
                this.fPrevTime = time;
            }
        }

        this.fPrevPos = curPos;
    }

    @Override
    public void update(long time)
    {
        super.update(time);

        this.processHintTick(time);
        this.repaint();
    }

    public final void repaint(int delayInterval)
    {
        BaseSystem.sleep(delayInterval);
        this.repaint();
    }

    public final void repaint()
    {
        BaseScreen scr = this.getScreen();
        if (scr != null) {
            try {
                scr.beginPaint();
                super.repaint(scr);
                scr.endPaint();

                this.fFrameCount += 1;
                long now = BaseSystem.getTickCount();
                if (now > this.fFrameStartTime + 1000) {
                    this.FPS = ((1000 * this.fFrameCount / (now - this.fFrameStartTime)));
                    this.fFrameStartTime = now;
                    this.fFrameCount = 0;
                }
            } catch (Exception ex) {
                Logger.write("BaseMainWindow.repaint(): " + ex.getMessage());
            }
        }
    }

    public abstract void processGameStep();

    public final void quit()
    {
        this.fSystem.quit();
    }

    public final void run()
    {
        try {
            this.AppCreate();
            try {
                this.fSystem.run();
            } finally {
                this.AppDestroy();
            }
        } catch (Exception ex) {
            Logger.write("BaseMainWindow.run(): " + ex.getMessage());
        }
    }

    public final void hideHint()
    {
        if (this.fHintWindow.getVisible()) {
            this.fHintWindow.hide();
        }
    }

    public final void showHint()
    {
        String hint;

        BaseControl ctl = super.getSubControl(super.getMousePoint().X, super.getMousePoint().Y);
        if (ctl != null && ctl.ShowHints) {
            hint = ctl.Hint;
        } else {
            hint = super.Hint;
        }

        if (hint.compareTo("") != 0 && (!this.fHintWindow.getVisible() || this.fHintWindow.getCaption().compareTo(hint) != 0)) {
            int dx;
            int dy;
            if (super.getCursor() == null) {
                dx = 16;
                dy = 16;
            } else {
                dx = (int) super.getCursor().Width;
                dy = (int) super.getCursor().Height;
            }

            this.fHintWindow.setCaption(hint);

            if (super.getMousePoint().X + dx + this.fHintWindow.getWidth() > super.getWidth()) {
                dx = super.getWidth() - (super.getMousePoint().X + dx + this.fHintWindow.getWidth());
            }
            if (super.getMousePoint().Y + dy + this.fHintWindow.getHeight() > super.getHeight()) {
                dy = super.getHeight() - (super.getMousePoint().Y + dy + this.fHintWindow.getHeight());
            }

            this.fHintWindow.setLeft(super.getMousePoint().X + dx);
            this.fHintWindow.setTop(super.getMousePoint().Y + dy);
            this.fHintWindow.show();
        }
    }

    public static void initEnvironment(String appName)
    {
        //SDLSystem.EnvironmentInit(AppName);
    }

    public static boolean isInstanceExists()
    {
        return false; //SDLSystem.InstanceExists();
    }
}
