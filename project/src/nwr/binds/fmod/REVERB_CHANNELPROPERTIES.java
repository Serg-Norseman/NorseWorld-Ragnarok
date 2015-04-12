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
import org.bridj.ann.Field;
import org.bridj.ann.Runtime;

@Runtime(CRuntime.class)
public class REVERB_CHANNELPROPERTIES extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public int Direct;

    @Field(1)
    public int Room;

    @Field(2)
    public long Flags;

    @Field(3)
    public Pointer ConnectionPoint;


    public REVERB_CHANNELPROPERTIES()
    {
        super();
    }

    public REVERB_CHANNELPROPERTIES(Pointer pointer)
    {
        super(pointer);
    }
}
