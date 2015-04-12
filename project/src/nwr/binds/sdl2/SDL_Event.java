/*
 *  The Simple DirectMedia Layer library (SDL).
 *  This library is distributed under the terms of the GNU LGPL license:
 *  http://www.gnu.org/copyleft/lesser.html
 *
 *  Java translation.
 *  Copyright (C) 2014 by Serg V. Zhdanovskih (aka Alchemist).
 */
package nwr.binds.sdl2;

import org.bridj.BridJ;
import org.bridj.CRuntime;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Runtime;
import org.bridj.ann.Union;

@Union
@Runtime(CRuntime.class)
public class SDL_Event extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public int type()
    {
        return this.io.getIntField(this, 0);
    }

    @Field(1)
    public SDL_WindowEvent window()
    {
        return this.io.getNativeObjectField(this, 1);
    }

    @Field(2)
    public SDL_KeyboardEvent key()
    {
        return this.io.getNativeObjectField(this, 2);
    }

    @Field(3)
    public SDL_MouseMotionEvent motion()
    {
        return this.io.getNativeObjectField(this, 3);
    }

    @Field(4)
    public SDL_MouseButtonEvent button()
    {
        return this.io.getNativeObjectField(this, 4);
    }

    @Field(5)
    public SDL_CommonEvent user()
    {
        return this.io.getNativeObjectField(this, 5);
    }

    @Field(6)
    public SDL_MouseWheelEvent wheel()
    {
        return this.io.getNativeObjectField(this, 6);
    }

    @Field(7)
    public SDL_TextInputEvent text()
    {
        return this.io.getNativeObjectField(this, 7);
    }

    public SDL_Event()
    {
        super();
    }

    public SDL_Event(Pointer pointer)
    {
        super(pointer);
    }
}
