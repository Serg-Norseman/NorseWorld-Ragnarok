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
import jzrlib.utils.Logger;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public abstract class BaseSystem extends BaseObject
{
    private static final boolean DEBUG_FRAMES = false;
    
    protected int fFrameDuration;
    protected boolean fFullScreen;
    protected BaseMainWindow fMainWindow;
    protected BaseScreen fScreen;
    protected boolean fSysCursorVisible;
    protected boolean fTerminate;

    private int fFLCount;
    private long fFLSum;

    public BaseSystem(BaseMainWindow mainWindow, int width, int height, boolean fullScreen)
    {
        this.fMainWindow = mainWindow;
        this.fFullScreen = fullScreen;
        this.fTerminate = false;
        this.fScreen = null;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.fScreen != null) {
                this.fScreen.dispose();
            }
        }
        super.dispose(disposing);
    }

    public final BaseScreen getScreen()
    {
        return this.fScreen;
    }

    public final boolean getSysCursorVisible()
    {
        return this.fSysCursorVisible;
    }

    public void setSysCursorVisible(boolean value)
    {
        if (this.fSysCursorVisible != value) {
            this.fSysCursorVisible = value;
        }
    }

    public boolean initFullScreen()
    {
        return false;
    }

    public void doneFullScreen()
    {
    }

    public final void help(String fileName)
    {
        BaseSystem.exec("explorer.exe \"" + fileName + "\"");
    }

    public final void resize(int width, int height)
    {
        if (this.fScreen != null) {
            this.fScreen.setSize(width, height);
        }
    }

    public final void quit()
    {
        this.fTerminate = true;
    }

    protected abstract void processEvents();

    public abstract void setCaption(String value);

    public final void run()
    {
        try {
            if (this.fFullScreen) {
                this.fFullScreen = this.initFullScreen();
            }

            try {
                BaseMainWindow mainWindow = this.fMainWindow;

                long drawTimer = BaseSystem.getTickCount();
                while (!this.fTerminate) {
                    this.processEvents();

                    mainWindow.processGameStep();

                    long now = BaseSystem.getTickCount();
                    long late = now - drawTimer;
                    if (late >= this.fFrameDuration * 5) {
                        drawTimer = now;
                        late = 0;
                    }

                    if (late < this.fFrameDuration) {
                        mainWindow.update(now);

                        late = BaseSystem.getTickCount() - drawTimer;
                        if (late < this.fFrameDuration) {
                            long intval = this.fFrameDuration - late;

                            if (DEBUG_FRAMES) {
                                if (this.fFLCount == 200) {
                                    this.fFLCount = 0;
                                    this.fFLSum = 0;
                                }

                                this.fFLCount++;
                                this.fFLSum += intval;

                                this.setCaption(String.valueOf(this.fFLSum / this.fFLCount));
                            }

                            BaseSystem.sleep(intval);
                        }
                    }

                    drawTimer += this.fFrameDuration;
                }
            } finally {
                if (this.fFullScreen) {
                    this.doneFullScreen();
                }
            }
        } catch (Exception ex) {
            Logger.write("BaseSystem.run(): " + ex.getMessage());
        }
    }

    public static final void exec(String command)
    {
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            //System.out.println(p.exitValue());
        } catch (Exception ex) {
            Logger.write("BaseSystem.exec(): " + ex.getMessage());
        }
    }

    public static long getTickCount()
    {
        return System.currentTimeMillis();
    }

    public static void sleep(long milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
        }
    }
}
