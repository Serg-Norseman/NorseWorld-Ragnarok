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
package nwr.engine.sdl2;

import nwr.binds.sdl2.SDL_CommonEvent;
import nwr.binds.sdl2.SDL_Event;
import nwr.binds.sdl2.SDL_KeyboardEvent;
import nwr.binds.sdl2.SDL_Lib;
import nwr.binds.sdl2.SDL_MouseButtonEvent;
import nwr.binds.sdl2.SDL_MouseMotionEvent;
import nwr.binds.sdl2.SDL_MouseWheelEvent;
import nwr.binds.sdl2.SDL_TextInputEvent;
import nwr.binds.sdl2.SDL_Window;
import nwr.binds.sdl2.SDL_WindowEvent;
import jzrlib.utils.Logger;
import nwr.engine.BaseMainWindow;
import nwr.engine.BaseSystem;
import nwr.engine.KeyEventArgs;
import nwr.engine.KeyPressEventArgs;
import nwr.engine.Keys;
import nwr.engine.MouseButton;
import nwr.engine.MouseEventArgs;
import nwr.engine.MouseMoveEventArgs;
import nwr.engine.MouseWheelEventArgs;
import nwr.engine.ShiftStates;
import org.bridj.Pointer;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class SDL2System extends BaseSystem
{
    private final Pointer<SDL_Window> fWinPtr;

    // <editor-fold defaultstate="collapsed" desc="Runtime fields for optimization">

    private final Pointer<SDL_Event> fEventPtr = Pointer.allocate(SDL_Event.class);

    // </editor-fold>

    public SDL2System(BaseMainWindow mainWindow, int width, int height, boolean fullScreen)
    {
        super(mainWindow, width, height, fullScreen);
        super.fFrameDuration = 50;

        SDL_Lib.SDL_Init(SDL_Lib.SDL_INIT_VIDEO | SDL_Lib.SDL_INIT_EVENTS);
        this.fWinPtr = SDL_Lib.SDL_CreateWindow(Pointer.pointerToCString("Win"), 
                SDL_Lib.SDL_WINDOWPOS_CENTERED, SDL_Lib.SDL_WINDOWPOS_CENTERED, 
                width, height, 0);

        SDL_Lib.SDL_StartTextInput();

        this.fScreen = new SDL2Screen(this.fWinPtr, fullScreen);
        this.fScreen.setSize(width, height);
    }

    @Override
    public void setSysCursorVisible(boolean value)
    {
        super.setSysCursorVisible(value);
        SDL_Lib.SDL_ShowCursor(!value ? 0 : 1);
    }

    @Override
    public void setCaption(String value)
    {
        SDL_Lib.SDL_SetWindowTitle(this.fWinPtr, Pointer.pointerToCString(value));
    }

    private static ShiftStates KeyboardStateToShiftState(SDL_KeyboardEvent keyEvent)
    {
        ShiftStates result = new ShiftStates();

        int modifier = keyEvent.keysym().mod();
        if ((modifier & SDL_Lib.KMOD_CTRL) != 0) {
            result.include(ShiftStates.ssCtrl);
        }
        if ((modifier & SDL_Lib.KMOD_SHIFT) != 0) {
            result.include(ShiftStates.ssShift);
        }
        if ((modifier & SDL_Lib.KMOD_ALT) != 0) {
            result.include(ShiftStates.ssAlt);
        }

        return result;
    }

    private static ShiftStates MouseStateToShiftState(SDL_MouseMotionEvent mouseEvent)
    {
        ShiftStates result = new ShiftStates();

        if (mouseEvent.state() == 1) {
            result.include(ShiftStates.ssLeft);
        }
        if (mouseEvent.state() == 2) {
            result.include(ShiftStates.ssMiddle);
        }
        if (mouseEvent.state() == 3) {
            result.include(ShiftStates.ssRight);
        }

        return result;
    }

    @Override
    protected void processEvents()
    {
        try {
            BaseMainWindow mainWindow = this.fMainWindow;

            while (SDL_Lib.SDL_PollEvent(fEventPtr) == 1 && !super.fTerminate) {
                SDL_Event event = fEventPtr.get();
                SDL_CommonEvent temp = event.user();

                switch (temp.type()) {
                    case SDL_Lib.SDL_WINDOWEVENT:
                        SDL_WindowEvent winEvent = event.window();
                        switch (winEvent.event()) {
                            case SDL_Lib.SDL_WINDOWEVENT_SHOWN:
                                mainWindow.DoActive(true);
                                break;
                            case SDL_Lib.SDL_WINDOWEVENT_HIDDEN:
                                mainWindow.DoActive(false);
                                break;
                        }
                        break;

                    case SDL_Lib.SDL_KEYDOWN: {
                        SDL_KeyboardEvent keyEvent = event.key();
                        int sym = keyEvent.keysym().sym();
                        Keys key = Keys.forValue(sym);

                        if (key != null) {
                            KeyEventArgs eventArgs = new KeyEventArgs();
                            eventArgs.Key = key;
                            eventArgs.Shift = KeyboardStateToShiftState(keyEvent);
                            mainWindow.processKeyDown(eventArgs);
                        }
                        break;
                    }

                    case SDL_Lib.SDL_KEYUP: {
                        SDL_KeyboardEvent keyEvent = event.key();
                        Keys key = Keys.forValue(keyEvent.keysym().sym());

                        if (key != null) {
                            KeyEventArgs eventArgs = new KeyEventArgs();
                            eventArgs.Key = key;
                            eventArgs.Shift = KeyboardStateToShiftState(keyEvent);
                            mainWindow.processKeyUp(eventArgs);
                        }
                        break;
                    }

                    case SDL_Lib.SDL_TEXTINPUT:
                        SDL_TextInputEvent textInputEvent = event.text();

                        Pointer<Byte> buf = textInputEvent.text();
                        String txt = buf.getCString();
                        if (txt != null) {
                            KeyPressEventArgs eventArg = new KeyPressEventArgs();
                            eventArg.Key = txt.charAt(0);
                            mainWindow.processKeyPress(eventArg);
                        }
                        break;

                    case SDL_Lib.SDL_MOUSEMOTION: {
                        SDL_MouseMotionEvent motionEvent = event.motion();
                        MouseMoveEventArgs eventArgs = new MouseMoveEventArgs(motionEvent.x(), motionEvent.y());
                        eventArgs.Shift = MouseStateToShiftState(motionEvent);
                        mainWindow.processMouseMove(eventArgs);
                        break;
                    }

                    case SDL_Lib.SDL_MOUSEBUTTONDOWN: {
                        SDL_MouseButtonEvent buttonEvent = event.button();
                        int x = buttonEvent.x();
                        int y = buttonEvent.y();
                        ShiftStates shift = new ShiftStates();

                        if (buttonEvent.button() == 4 || buttonEvent.button() == 5) {
                        } else {
                            MouseEventArgs eventArgs = new MouseEventArgs(x, y);
                            eventArgs.Shift = shift;
                            switch (buttonEvent.button()) {
                                case 1:
                                    eventArgs.Button = MouseButton.mbLeft;
                                    break;
                                case 2:
                                    eventArgs.Button = MouseButton.mbMiddle;
                                    break;
                                case 3:
                                    eventArgs.Button = MouseButton.mbRight;
                                    break;
                                default:
                                    eventArgs.Button = MouseButton.mbLeft;
                                    break;
                            }
                            mainWindow.processMouseDown(eventArgs);
                        }
                        break;
                    }

                    case SDL_Lib.SDL_MOUSEBUTTONUP: {
                        SDL_MouseButtonEvent buttonEvent = event.button();
                        int x = buttonEvent.x();
                        int y = buttonEvent.y();
                        ShiftStates shift = new ShiftStates();

                        if (buttonEvent.button() == 4 || buttonEvent.button() == 5) {
                        } else {
                            MouseEventArgs eventArgs = new MouseEventArgs(x, y);
                            eventArgs.Shift = shift;
                            switch (buttonEvent.button()) {
                                case 1:
                                    eventArgs.Button = MouseButton.mbLeft;
                                    break;
                                case 2:
                                    eventArgs.Button = MouseButton.mbMiddle;
                                    break;
                                case 3:
                                    eventArgs.Button = MouseButton.mbRight;
                                    break;
                                default:
                                    eventArgs.Button = MouseButton.mbLeft;
                                    break;
                            }
                            mainWindow.processMouseUp(eventArgs);
                        }
                        break;
                    }

                    case SDL_Lib.SDL_MOUSEWHEEL:
                        SDL_MouseWheelEvent wheelEvent = event.wheel();
                        int x = wheelEvent.x();
                        int y = wheelEvent.y();

                        MouseWheelEventArgs eventArgs = new MouseWheelEventArgs(x, y);
                        eventArgs.Shift = new ShiftStates();
                        eventArgs.WheelDelta = y * -1;

                        mainWindow.processMouseWheel(eventArgs);
                        break;

                    case SDL_Lib.SDL_QUIT:
                        super.fTerminate = true;
                        break;
                }
            }
        } catch (Exception ex) {
            Logger.write("SDLSystem.processEvents(): " + ex.getMessage());
        }
    }
}
