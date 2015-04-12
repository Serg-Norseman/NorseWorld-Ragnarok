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
public final class SDL_Color extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public byte r()
    {
        return this.io.getByteField(this, 0);
    }

    @Field(0)
    public SDL_Color r(byte r)
    {
        this.io.setByteField(this, 0, r);
        return this;
    }

    @Field(1)
    public byte g()
    {
        return this.io.getByteField(this, 1);
    }

    @Field(1)
    public SDL_Color g(byte g)
    {
        this.io.setByteField(this, 1, g);
        return this;
    }

    @Field(2)
    public byte b()
    {
        return this.io.getByteField(this, 2);
    }

    @Field(2)
    public SDL_Color b(byte b)
    {
        this.io.setByteField(this, 2, b);
        return this;
    }

    @Field(3)
    public byte a()
    {
        return this.io.getByteField(this, 3);
    }

    @Field(3)
    public SDL_Color a(byte a)
    {
        this.io.setByteField(this, 3, a);
        return this;
    }

    public SDL_Color()
    {
        super();
    }

    public SDL_Color(Pointer pointer)
    {
        super(pointer);
    }

    public void setRGB(byte r, byte g, byte b)
    {
        this.r(r);
        this.g(g);
        this.b(b);
        this.a((byte) 0);
    }
}
