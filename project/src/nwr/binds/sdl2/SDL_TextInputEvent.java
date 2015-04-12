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
import org.bridj.ann.Array;
import org.bridj.ann.Field;
import org.bridj.ann.Runtime;

@Runtime(CRuntime.class)
public class SDL_TextInputEvent extends StructObject
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

    @Array({32})
    @Field(3)
    public Pointer<Byte> text()
    {
        return this.io.getPointerField(this, 3);
    }

    public SDL_TextInputEvent()
    {
        super();
    }

    public SDL_TextInputEvent(Pointer pointer)
    {
        super(pointer);
    }
}
