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
public class SDL_KeyboardEvent extends StructObject
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
    public byte state()
    {
        return this.io.getByteField(this, 3);
    }

    @Field(4)
    public byte repeat()
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

    @Field(7)
    public SDL_keysym keysym()
    {
        return this.io.getNativeObjectField(this, 7);
    }

    public SDL_KeyboardEvent()
    {
        super();
    }

    public SDL_KeyboardEvent(Pointer pointer)
    {
        super(pointer);
    }
}
