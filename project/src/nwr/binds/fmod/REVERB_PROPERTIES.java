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
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Runtime;

@Runtime(CRuntime.class)
public class REVERB_PROPERTIES extends StructObject
{
    static {
        BridJ.register();
    }

    @Field(0)
    public int Instance;

    @Field(1)
    public int Environment;

    @Field(2)
    public float EnvDiffusion;

    @Field(3)
    public int Room;

    @Field(4)
    public int RoomHF;

    @Field(5)
    public int RoomLF;

    @Field(6)
    public float DecayTime;

    @Field(7)
    public float DecayHFRatio;

    @Field(8)
    public float DecayLFRatio;

    @Field(9)
    public int Reflections;

    @Field(10)
    public float ReflectionsDelay;

    @Field(11)
    public int Reverb;

    @Field(12)
    public float ReverbDelay;

    @Field(13)
    public float ModulationTime;

    @Field(14)
    public float ModulationDepth;

    @Field(15)
    public float HFReference;

    @Field(16)
    public float LFReference;

    @Field(17)
    public float Diffusion;

    @Field(18)
    public float Density;

    @Field(19)
    public long Flags;


    public REVERB_PROPERTIES(int instance, int environment, float envDiffusion, int room, int roomHF, int roomLF,
            float decayTime, float decayHFRatio, float decayLFRatio, int reflections, float reflectionsDelay,
            int reverb, float reverbDelay, float modulationTime, float modulationDepth, float hfReference,
            float lfReference, float diffusion, float density, long flags)
    {
        Instance = instance;
        Environment = environment;
        EnvDiffusion = envDiffusion;
        Room = room;
        RoomHF = roomHF;
        RoomLF = roomLF;
        DecayTime = decayTime;
        DecayHFRatio = decayHFRatio;
        DecayLFRatio = decayLFRatio;
        Reflections = reflections;
        ReflectionsDelay = reflectionsDelay;
        Reverb = reverb;
        ReverbDelay = reverbDelay;
        ModulationTime = modulationTime;
        ModulationDepth = modulationDepth;
        HFReference = hfReference;
        LFReference = lfReference;
        Diffusion = diffusion;
        Density = density;
        Flags = flags;
    }
}
