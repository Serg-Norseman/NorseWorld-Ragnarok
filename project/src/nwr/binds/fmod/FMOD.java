/*
 *  FMOD Ex - C# Wrapper.
 *  Copyright (c) 2004-2014 by Firelight Technologies Pty, Ltd.
 *
 *  Java translation.
 *  Copyright (C) 2014 by Serg V. Zhdanovskih (aka Alchemist).
 */
package nwr.binds.fmod;

import jzrlib.utils.AuxUtils;
import nwr.engine.ResourceManager;
import org.bridj.BridJ;
import org.bridj.CRuntime;
import org.bridj.Pointer;
import org.bridj.ann.Library;
import org.bridj.ann.Runtime;

@Library("fmodex")
@Runtime(CRuntime.class)
public final class FMOD
{
    static {
        String path = ResourceManager.getExecPath();
        int sys = AuxUtils.getSystemModel();
        if (sys == 32) {
            //path += "/win-x86";
        } else if (sys == 64) {
            //path += "/win-x64";
            BridJ.addNativeLibraryAlias("fmodex", "fmodex64");
        }

        BridJ.addLibraryPath(path);
        BridJ.register();
    }

    public static native int FMOD_System_Create(Pointer system);

    public static native int FMOD_System_GetVersion(Pointer<SYSTEM> system, Pointer<Integer> version);

    public static native int FMOD_System_Release(Pointer<SYSTEM> system);

    public static native int FMOD_System_SetOutput(Pointer<SYSTEM> system, int output);

    public static native int FMOD_System_SetDriver(Pointer<SYSTEM> system, int driver);

    public static native int FMOD_System_Init(Pointer<SYSTEM> system, int maxchannels, int flags, Pointer<?> extradriverdata);

    public static native int FMOD_System_Close(Pointer<SYSTEM> system);

    public static native int FMOD_System_Update(Pointer<SYSTEM> system);

    public static native int FMOD_System_Set3DSettings(Pointer<SYSTEM> system, float dopplerscale, float distancefactor, float rolloffscale);

    public static native int FMOD_System_CreateStream(Pointer<SYSTEM> system, Pointer<Byte> name_or_data, int mode, int exinfo, Pointer sound);

    public static native int FMOD_System_PlaySound(Pointer<SYSTEM> system, byte channelid, Pointer sound, int paused, Pointer channel);

    public static native int FMOD_System_Set3DListenerAttributes(Pointer<SYSTEM> system, int listener, Pointer<VECTOR> pos, Pointer<VECTOR> vel, Pointer<VECTOR> forward, Pointer<VECTOR> up);

    public static native int FMOD_System_SetReverbProperties(Pointer<SYSTEM> system, Pointer<REVERB_PROPERTIES> prop);

    public static native int FMOD_System_SetReverbAmbientProperties(Pointer<SYSTEM> system, Pointer<REVERB_PROPERTIES> prop);

    public static native int FMOD_Sound_Release(Pointer<?> sound);

    public static native int FMOD_Channel_Stop(Pointer<CHANNEL> channel);

    public static native int FMOD_Channel_SetPaused(Pointer<CHANNEL> channel, int paused);

    public static native int FMOD_Channel_SetVolume(Pointer<CHANNEL> channel, float volume);

    public static native int FMOD_Channel_IsPlaying(Pointer<CHANNEL> channel, Pointer<Integer> isplaying);

    public static native int FMOD_Channel_Set3DMinMaxDistance(Pointer<CHANNEL> channel, float mindistance, float maxdistance);

    public static native int FMOD_Channel_SetReverbProperties(Pointer<CHANNEL> channel, Pointer<REVERB_CHANNELPROPERTIES> prop);

    public static native int FMOD_Channel_Set3DAttributes(Pointer<CHANNEL> channel, Pointer<VECTOR> pos, Pointer<VECTOR> vel);
}
