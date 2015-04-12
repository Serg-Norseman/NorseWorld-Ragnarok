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
public class SDL_keysym extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public short scancode() // ???
    {
        return this.io.getShortField(this, 0);
    }

    @Field(1)
    public int sym()
    {
        return this.io.getIntField(this, 1);
    }

    @Field(2)
    public short mod()
    {
        return this.io.getShortField(this, 2);
    }

    @Field(3)
    public int unused()
    {
        return this.io.getIntField(this, 3);
    }

    public SDL_keysym()
    {
        super();
    }

    public SDL_keysym(Pointer pointer)
    {
        super(pointer);
    }
}
