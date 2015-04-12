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
public class SDL_WindowEvent extends StructObject
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
    public byte event()
    {
        return this.io.getByteField(this, 3);
    }

    @Field(4)
    public byte padding1()
    {
        return this.io.getByteField(this, 4);
    }

    @Field(5)
    public byte padding2()
    {
        return this.io.getByteField(this, 5);
    }

    @Field(6)
    public byte padding3()
    {
        return this.io.getByteField(this, 6);
    }

    public SDL_WindowEvent()
    {
        super();
    }

    public SDL_WindowEvent(Pointer pointer)
    {
        super(pointer);
    }
}
