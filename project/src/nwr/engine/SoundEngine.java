/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  This file is part of "NorseWorld: Ragnarok".
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nwr.engine;

import jzrlib.core.Point;
import jzrlib.utils.Logger;
import nwr.binds.fmod.CHANNEL;
import nwr.binds.fmod.CHANNELINDEX;
import nwr.binds.fmod.FMOD;
import nwr.binds.fmod.FMODError;
import nwr.binds.fmod.SYSTEM;
import nwr.binds.fmod.INITFLAGS;
import nwr.binds.fmod.MODE;
import nwr.binds.fmod.OUTPUTTYPE;
import nwr.binds.fmod.PRESET;
import nwr.binds.fmod.RESULT;
import nwr.binds.fmod.REVERB_PROPERTIES;
import nwr.binds.fmod.STREAM;
import nwr.binds.fmod.VECTOR;
import nwr.main.GlobalVars;
import org.bridj.Pointer;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class SoundEngine
{
    public static final int sk_Sound = 0;
    public static final int sk_Song = 1;
    public static final int sk_Ambient = 2;

    public enum Reverb
    {
        Room,
        Cave,
        Arena,
        Forest,
        Mountains,
        Underwater,
        Dungeon, 
        CHall,
        Quarry,
        Plain
    }

    private final static class SoundData
    {
        public int Kind;
        public Pointer<STREAM> Stream;
        public Pointer<CHANNEL> Channel;
    }

    private static final int MaxSounds = 64;

    private final SoundEngine.SoundData[] fSndList;
    private final int[] fSndVolume;

    private Pointer<Pointer<SYSTEM>> FSOUND;
    private int fSndCount;
    private boolean fSndReady;

    public SoundEngine()
    {
        this.fSndVolume = new int[3];

        this.fSndList = new SoundEngine.SoundData[MaxSounds];
        for (int i = 0; i < MaxSounds; i++) {
            this.fSndList[i] = new SoundData();
        }
    }

    public final void sfxSetReverb(Reverb reverb)
    {
        PRESET preset = new PRESET();
        REVERB_PROPERTIES rp = preset.OFF();
        switch (reverb) {
            case Room:
                rp = preset.STONEROOM();
                break;

            case Cave:
                rp = preset.CAVE();
                break;

            case Arena:
                rp = preset.ARENA();
                break;

            case Forest:
                rp = preset.FOREST();
                break;

            case Mountains:
                rp = preset.MOUNTAINS();
                break;

            case Underwater:
                rp = preset.UNDERWATER();
                break;

            case Dungeon:
                rp = preset.STONECORRIDOR();
                break;

            case CHall:
                rp = preset.CONCERTHALL();
                break;

            case Quarry:
                rp = preset.QUARRY();
                break;

            case Plain:
                rp = preset.PLAIN();
                break;
        }

        //FMOD.FMOD_System_SetReverbAmbientProperties(this.FSOUND.getValue(), rp);
        /*FMOD.FMOD_System_SetReverbProperties(this.FSOUND, rp);*/
    }

    public final void sfxDone()
    {
        try {
            if (GlobalVars.Debug_Silent) {
                return;
            }

            for (int i = 0; i < this.fSndCount; i++) {
                if (this.fSndList[i].Stream != null) {
                    FMOD.FMOD_Channel_Stop(this.fSndList[i].Channel);
                    this.fSndList[i].Channel = null;
                    FMOD.FMOD_Sound_Release(this.fSndList[i].Stream);
                    this.fSndList[i].Stream = null;
                }
            }

            FMOD.FMOD_System_Close(FSOUND.get());
        } catch (Exception ex) {
            Logger.write("SoundEngine.sfxDone(): " + ex.getMessage());
        }
    }

    public final void sfxInit(int aWnd)
    {
        try {
            if (!GlobalVars.Debug_Silent) {
                this.FSOUND = Pointer.allocatePointer(SYSTEM.class);

                RESULT result = RESULT.forValue(FMOD.FMOD_System_Create(FSOUND));
                if (result != RESULT.OK) {
                    throw new RuntimeException("Create(): " + FMODError.getString(result));
                }

                Pointer<Integer> ver = Pointer.allocateInt();
                result = RESULT.forValue(FMOD.FMOD_System_GetVersion(FSOUND.get(), ver));
                if (result != RESULT.OK) {
                    throw new RuntimeException("GetVersion(): " + FMODError.getString(result));
                }
                int verVal = ver.get();
                if (verVal < 279604) {
                    throw new RuntimeException("GetVersion(): You are using an old version of FMOD");
                }

                result = RESULT.forValue(FMOD.FMOD_System_SetOutput(FSOUND.get(), OUTPUTTYPE.AUTODETECT.ordinal()));
                if (result != RESULT.OK) {
                    throw new RuntimeException("SetOutput(): " + FMODError.getString(result));
                }
                result = RESULT.forValue(FMOD.FMOD_System_SetDriver(FSOUND.get(), 0));
                if (result != RESULT.OK) {
                    throw new RuntimeException("SetDriver(): " + FMODError.getString(result));
                }
                result = RESULT.forValue(FMOD.FMOD_System_Init(FSOUND.get(), SoundEngine.MaxSounds, INITFLAGS.NORMAL.value, Pointer.NULL));
                if (result != RESULT.OK) {
                    throw new RuntimeException("Init(): " + FMODError.getString(result));
                }

                this.fSndReady = true;
                this.fSndCount = 0;

                for (int i = 0; i < MaxSounds; i++) {
                    this.fSndList[i].Stream = null;
                    this.fSndList[i].Channel = null;
                }

                this.fSndVolume[0] = 255;
                this.fSndVolume[1] = 255;
                this.fSndVolume[2] = 255;
            }
        } catch (Exception ex) {
            Logger.write("SoundEngine.sfxInit(): " + ex.getMessage());
        }
    }

    public final void sfxPlay(String fileName, int kind)
    {
        this.sfxPlay(fileName, kind, null, null);
    }

    public final void sfxPlay(String fileName, int kind, Point player, Point sound)
    {
        if (GlobalVars.Debug_Silent) {
            return;
        }

        if (!this.fSndReady) {
            Logger.write("SFX kernel not initialized");
            return;
        }

        if (this.fSndVolume[kind] != 0) {
            fileName = ResourceManager.getAppPath() + fileName;
            //fileName = AuxUtils.ConvertPath(fileName);

            if (!(new java.io.File(fileName)).isFile()) {
                Logger.write(String.format("Media file \"%s\" not exists", new Object[]{fileName}));
            } else {
                if (this.fSndCount == SoundEngine.MaxSounds) {
                    Logger.write(String.format("Limit of %d songs reached", new Object[]{SoundEngine.MaxSounds}));
                } else {
                    int aPos = -1;
                    int num = this.fSndCount;
                    for (int idx = 0; idx < num; idx++) {
                        if (this.fSndList[idx].Kind == kind) {
                            boolean isplaying;
                            Pointer<Integer> p = Pointer.allocateInt();
                            //this.FSndList[Index].Channel.isPlaying(tempRef_isplaying);
                            FMOD.FMOD_Channel_IsPlaying(this.fSndList[idx].Channel, p);
                            isplaying = p.get() != 0;

                            if (kind == SoundEngine.sk_Song || kind == SoundEngine.sk_Ambient || (kind == SoundEngine.sk_Sound && !isplaying)) {
                                FMOD.FMOD_Channel_Stop(this.fSndList[idx].Channel);
                                this.fSndList[idx].Channel = null;
                                FMOD.FMOD_Sound_Release(this.fSndList[idx].Stream);
                                this.fSndList[idx].Stream = null;
                                aPos = idx;
                                break;
                            }
                        }
                    }

                    long mode = MODE.SOFTWARE.value | MODE._3D.value/* | MODE.UNICODE.getValue()*/;
                    if (kind == SoundEngine.sk_Sound) {
                        mode |= MODE.LOOP_OFF.value;
                    } else if (kind == SoundEngine.sk_Song || kind == SoundEngine.sk_Ambient) {
                        mode |= MODE.LOOP_NORMAL.value;
                    }

                    Pointer<Pointer<STREAM>> stream = Pointer.allocatePointer(STREAM.class);
                    Pointer<Byte> strFilename = Pointer.pointerToCString(fileName);
                    RESULT res = RESULT.forValue(FMOD.FMOD_System_CreateStream(FSOUND.get(), strFilename, (int) mode, 0, stream));

                    if (res != RESULT.OK || stream == Pointer.NULL) {
                        Logger.write(FMODError.getString(res));
                    } else {
                        if (aPos == -1) {
                            aPos = this.fSndCount;
                            this.fSndCount++;
                        }

                        Pointer<Pointer<CHANNEL>> channel = Pointer.allocatePointer(CHANNEL.class);
                        res = RESULT.forValue(FMOD.FMOD_System_PlaySound(FSOUND.get(), (byte) CHANNELINDEX.FREE.value, stream.get(), 0, channel));

                        if (res != RESULT.OK || channel == Pointer.NULL) {
                            Logger.write(FMODError.getString(res));
                        } else {
                            SoundData soundData = this.fSndList[aPos];
                            soundData.Kind = kind;
                            soundData.Stream = stream.get();
                            soundData.Channel = channel.get();

                            FMOD.FMOD_Channel_SetVolume(soundData.Channel, (float) (this.fSndVolume[kind] / 255.0f));
                            FMOD.FMOD_Channel_Set3DMinMaxDistance(soundData.Channel, 0, 80);

                            if (kind == SoundEngine.sk_Sound) {
                                set3DProps(soundData.Channel, player, sound);
                            }
                        }
                    }
                }
            }
        }
    }

    private void set3DProps(Pointer<CHANNEL> channel, Point player, Point sound)
    {
        try {
            FMOD.FMOD_Channel_Set3DMinMaxDistance(channel, 0f, 80f);

            Pointer<VECTOR> zero, listenerpos, sourcePos;
            
            zero = Pointer.allocate(VECTOR.class);
            zero.get().setValues(0.0f, 0.0f, 0.0f);

            listenerpos = Pointer.allocate(VECTOR.class);
            listenerpos.get().setValues(player.X, 0.0f, player.Y);
            FMOD.FMOD_System_Set3DListenerAttributes(FSOUND.get(), 0, listenerpos, zero, zero, zero);

            sourcePos = Pointer.allocate(VECTOR.class);
            sourcePos.get().setValues(sound.X, 0.0f, sound.Y);

            FMOD.FMOD_Channel_Set3DAttributes(channel, sourcePos, zero);
        } catch (Exception ex) {
            Logger.write("SoundEngine.set3DProps(): " + ex.getMessage());
        }
    }

    public final void sfxSetVolume(int volume, int aKind)
    {
        if (GlobalVars.Debug_Silent) {
            return;
        }

        if (!this.fSndReady) {
            Logger.write("SFX kernel not initialized");
            return;
        }

        this.fSndVolume[aKind] = volume;

        for (int i = 0; i < this.fSndCount; i++) {
            if (this.fSndList[i].Kind == aKind && this.fSndList[i].Channel != null) {
                FMOD.FMOD_Channel_SetVolume(this.fSndList[i].Channel, (float)(volume / 255.0f));
            }
        }
    }

    public final void sfxResume()
    {
        if (GlobalVars.Debug_Silent) {
            return;
        }

        if (!this.fSndReady) {
            Logger.write("SFX kernel not initialized");
            return;
        }

        for (int i = 0; i < this.fSndCount; i++) {
            FMOD.FMOD_Channel_SetPaused(this.fSndList[i].Channel, 0);
        }
    }

    public final void sfxSuspend()
    {
        if (GlobalVars.Debug_Silent) {
            return;
        }

        if (!this.fSndReady) {
            Logger.write("SFX kernel not initialized");
            return;
        }

        for (int i = 0; i < this.fSndCount; i++) {
            FMOD.FMOD_Channel_SetPaused(this.fSndList[i].Channel, 1);
        }
    }
}
