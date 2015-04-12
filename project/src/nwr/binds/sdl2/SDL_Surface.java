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
public class SDL_Surface extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public int flags()
    {
        return this.io.getIntField(this, 0);
    }

    @Field(1)
    public Pointer<SDL_PixelFormat> format()
    {
        return this.io.getPointerField(this, 1);
    }

    @Field(2)
    public int w()
    {
        return this.io.getIntField(this, 2);
    }

    @Field(3)
    public int h()
    {
        return this.io.getIntField(this, 3);
    }

    @Field(4)
    public int pitch()
    {
        return this.io.getIntField(this, 4);
    }

    @Field(5)
    public Pointer<?> pixels()
    {
        return this.io.getPointerField(this, 5);
    }

    @Field(6)
    public Pointer<?> userdata()
    {
        return this.io.getPointerField(this, 6);
    }

    @Field(7)
    public int locked()
    {
        return this.io.getIntField(this, 7);
    }

    @Field(8)
    public Pointer<?> lock_data()
    {
        return this.io.getPointerField(this, 8);
    }

    @Field(9)
    public SDL_Rect clip_rect()
    {
        return this.io.getNativeObjectField(this, 9);
    }

    @Field(10)
    public Pointer<?> map()
    {
        return this.io.getPointerField(this, 10);
    }

    @Field(11)
    public int refcount()
    {
        return this.io.getIntField(this, 11);
    }

    public SDL_Surface()
    {
        super();
    }

    public SDL_Surface(Pointer pointer)
    {
        super(pointer);
    }
}
