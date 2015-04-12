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
public final class SDL_Palette extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public int ncolors()
    {
        return this.io.getIntField(this, 0);
    }

    @Field(1)
    public Pointer<SDL_Color> colors()
    {
        return this.io.getPointerField(this, 1);
    }

    public SDL_Palette()
    {
        super();
    }

    public SDL_Palette(Pointer pointer)
    {
        super(pointer);
    }
}
