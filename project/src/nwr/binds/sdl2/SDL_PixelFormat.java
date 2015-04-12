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
public final class SDL_PixelFormat extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public int format()
    {
        return this.io.getIntField(this, 0);
    }

    @Field(1)
    public Pointer<SDL_Palette> palette()
    {
        return this.io.getPointerField(this, 1);
    }

    @Field(2)
    public byte BitsPerPixel()
    {
        return this.io.getByteField(this, 2);
    }

    @Field(3)
    public byte BytesPerPixel()
    {
        return this.io.getByteField(this, 3);
    }

    /*@Field(3)
    public byte Rloss()
    {
        return this.io.getByteField(this, 3);
    }

    @Field(4)
    public byte Gloss()
    {
        return this.io.getByteField(this, 4);
    }

    @Field(5)
    public byte Bloss()
    {
        return this.io.getByteField(this, 5);
    }

    @Field(6)
    public byte Aloss()
    {
        return this.io.getByteField(this, 6);
    }

    @Field(7)
    public byte Rshift()
    {
        return this.io.getByteField(this, 7);
    }

    @Field(8)
    public byte Gshift()
    {
        return this.io.getByteField(this, 8);
    }

    @Field(9)
    public byte Bshift()
    {
        return this.io.getByteField(this, 9);
    }

    @Field(10)
    public byte Ashift()
    {
        return this.io.getByteField(this, 10);
    }

    @Field(11)
    public int Rmask()
    {
        return this.io.getIntField(this, 11);
    }

    @Field(12)
    public int Gmask()
    {
        return this.io.getIntField(this, 12);
    }

    @Field(13)
    public int Bmask()
    {
        return this.io.getIntField(this, 13);
    }

    @Field(14)
    public int Amask()
    {
        return this.io.getIntField(this, 14);
    }

    @Field(15)
    public int colorkey()
    {
        return this.io.getIntField(this, 15);
    }

    @Field(16)
    public byte alpha()
    {
        return this.io.getByteField(this, 16);
    }*/

    public SDL_PixelFormat()
    {
        super();
    }

    public SDL_PixelFormat(Pointer pointer)
    {
        super(pointer);
    }
}
