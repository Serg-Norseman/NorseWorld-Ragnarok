/*
 *  FMOD Ex - C# Wrapper.
 *  Copyright (c) 2004-2014 by Firelight Technologies Pty, Ltd.
 *
 *  Java translation.
 *  Copyright (C) 2014 by Serg V. Zhdanovskih (aka Alchemist).
 */
package nwr.binds.fmod;

import org.bridj.BridJ;
import org.bridj.CRuntime;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Runtime;

@Runtime(CRuntime.class)
public final class CHANNEL extends StructObject
{
    static {
        BridJ.register();
    }

    public CHANNEL()
    {
        super();
    }

    public CHANNEL(Pointer pointer)
    {
        super(pointer);
    }    
}
