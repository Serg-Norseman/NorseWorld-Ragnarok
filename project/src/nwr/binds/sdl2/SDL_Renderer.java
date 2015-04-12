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
import org.bridj.ann.Runtime;

@Runtime(CRuntime.class)
public class SDL_Renderer extends StructObject
{
    static {
        BridJ.register();
    }

    public SDL_Renderer()
    {
        super();
    }

    public SDL_Renderer(Pointer pointer)
    {
        super(pointer);
    }
}
