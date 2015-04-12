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

@Runtime(CRuntime.class)
public class SDL_MouseWheelEvent extends StructObject
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
    public int timestamp()
    {
        return this.io.getIntField(this, 1);
    }

    @Field(2)
    public int windowID()
    {
        return this.io.getIntField(this, 2);
    }

    @Field(3)
    public int which()
    {
        return this.io.getIntField(this, 3);
    }

    @Field(4)
    public int x()
    {
        return this.io.getIntField(this, 4);
    }

    @Field(5)
    public int y()
    {
        return this.io.getIntField(this, 5);
    }

    public SDL_MouseWheelEvent()
    {
        super();
    }

    public SDL_MouseWheelEvent(Pointer pointer)
    {
        super(pointer);
    }
}
