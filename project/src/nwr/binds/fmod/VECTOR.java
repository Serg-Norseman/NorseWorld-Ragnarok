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
public class VECTOR extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public float x()
    {
        return this.io.getFloatField(this, 0);
    }

    @Field(0)
    public VECTOR x(float x)
    {
        this.io.setFloatField(this, 0, x);
        return this;
    }
    
    @Field(1)
    public float y()
    {
        return this.io.getFloatField(this, 0);
    }

    @Field(1)
    public VECTOR y(float y)
    {
        this.io.setFloatField(this, 0, y);
        return this;
    }
    
    @Field(2)
    public float z()
    {
        return this.io.getFloatField(this, 0);
    }

    @Field(2)
    public VECTOR z(float z)
    {
        this.io.setFloatField(this, 0, z);
        return this;
    }

    public VECTOR()
    {
        super();
    }

    public VECTOR(Pointer pointer)
    {
        super(pointer);
    }

    public void setValues(float x, float y, float z)
    {
        this.x(x);
        this.y(y);
        this.z(z);
    }
}
