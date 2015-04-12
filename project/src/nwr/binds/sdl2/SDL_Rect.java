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
public final class SDL_Rect extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public int x()
    {
        return this.io.getIntField(this, 0);
    }

    @Field(0)
    public SDL_Rect x(int x)
    {
        this.io.setIntField(this, 0, x);
        return this;
    }

    @Field(1)
    public int y()
    {
        return this.io.getIntField(this, 1);
    }

    @Field(1)
    public SDL_Rect y(int y)
    {
        this.io.setIntField(this, 1, y);
        return this;
    }

    @Field(2)
    public int w()
    {
        return this.io.getIntField(this, 2);
    }

    @Field(2)
    public SDL_Rect w(int w)
    {
        this.io.setIntField(this, 2, w);
        return this;
    }

    @Field(3)
    public int h()
    {
        return this.io.getIntField(this, 3);
    }

    @Field(3)
    public SDL_Rect h(int h)
    {
        this.io.setIntField(this, 3, h);
        return this;
    }

    public SDL_Rect()
    {
        super();
    }

    public SDL_Rect(Pointer pointer)
    {
        super(pointer);
    }

    public final void setBounds(int x, int y, int w, int h)
    {
        this.x(x);
        this.y(y);
        this.w(w);
        this.h(h);
    }
}
