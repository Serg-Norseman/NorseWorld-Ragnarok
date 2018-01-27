/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih.
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
 *  along with program.  If not, see <http://www.gnu.org/licenses/>.
 */

using System;
using System.IO;
using BSLib;
using FMOD;
using ZRLib.Core;

namespace NWR.Game
{
    public sealed class SoundEngine
    {
        public const int sk_Sound = 0;
        public const int sk_Song = 1;
        public const int sk_Ambient = 2;

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

        private sealed class SoundData
        {
            public int Kind;
            public Sound Stream;
            public Channel Channel;
        }

        private const int MaxSounds = 64;

        private readonly SoundData[] fSndList;
        private readonly int[] fSndVolume;

        private FMOD.System fSystem;
        private int fSndCount;
        private bool fSndReady;

        public SoundEngine()
        {
            fSndVolume = new int[3];
            fSndList = new SoundData[MaxSounds];
            for (int i = 0; i < MaxSounds; i++) {
                fSndList[i] = new SoundData();
            }
        }

        public void SfxSetReverb(Reverb reverb)
        {
            PRESET preset = new PRESET();
            REVERB_PROPERTIES rp = preset.OFF();
            switch (reverb) {
                case Reverb.Room:
                    rp = preset.STONEROOM();
                    break;

                case Reverb.Cave:
                    rp = preset.CAVE();
                    break;

                case Reverb.Arena:
                    rp = preset.ARENA();
                    break;

                case Reverb.Forest:
                    rp = preset.FOREST();
                    break;

                case Reverb.Mountains:
                    rp = preset.MOUNTAINS();
                    break;

                case Reverb.Underwater:
                    rp = preset.UNDERWATER();
                    break;

                case Reverb.Dungeon:
                    rp = preset.STONECORRIDOR();
                    break;

                case Reverb.CHall:
                    rp = preset.CONCERTHALL();
                    break;

                case Reverb.Quarry:
                    rp = preset.QUARRY();
                    break;

                case Reverb.Plain:
                    rp = preset.PLAIN();
                    break;
            }

            fSystem.setReverbAmbientProperties(ref rp);
            fSystem.setReverbProperties(ref rp);
        }

        public void SfxDone()
        {
            try {
                if (GlobalVars.Debug_Silent) {
                    return;
                }

                for (int i = 0; i < fSndCount; i++) {
                    if (fSndList[i].Stream != null) {
                        fSndList[i].Channel.stop();
                        fSndList[i].Channel = null;
                        fSndList[i].Stream.release();
                        fSndList[i].Stream = null;
                    }
                }

                fSystem.close();
            } catch (Exception ex) {
                Logger.Write("SoundEngine.sfxDone(): " + ex.Message);
            }
        }

        public void SfxInit(int wnd)
        {
            try {
                if (!GlobalVars.Debug_Silent) {
                    RESULT result = Factory.System_Create(ref fSystem);
                    if (result != RESULT.OK) {
                        throw new Exception("Create(): " + Error.String(result));
                    }

                    uint ver = 0;
                    result = fSystem.getVersion(ref ver);
                    if (result != RESULT.OK) {
                        throw new Exception("GetVersion(): " + Error.String(result));
                    }
                    if (ver < 279604) {
                        throw new Exception("GetVersion(): You are using an old version of FMOD");
                    }

                    result = fSystem.setOutput(OUTPUTTYPE.AUTODETECT);
                    if (result != RESULT.OK) {
                        throw new Exception("SetOutput(): " + Error.String(result));
                    }
                    result = fSystem.setDriver(0);
                    if (result != RESULT.OK) {
                        throw new Exception("SetDriver(): " + Error.String(result));
                    }
                    result = fSystem.init(MaxSounds, INITFLAGS.NORMAL, IntPtr.Zero);
                    if (result != RESULT.OK) {
                        throw new Exception("Init(): " + Error.String(result));
                    }

                    fSndReady = true;
                    fSndCount = 0;

                    for (int i = 0; i < MaxSounds; i++) {
                        fSndList[i].Stream = null;
                        fSndList[i].Channel = null;
                    }

                    fSndVolume[0] = 255;
                    fSndVolume[1] = 255;
                    fSndVolume[2] = 255;
                }
            } catch (Exception ex) {
                Logger.Write("SoundEngine.sfxInit(): " + ex.Message);
            }
        }

        public void SfxPlay(string fileName, int kind)
        {
            SfxPlay(fileName, kind, ExtPoint.Empty, ExtPoint.Empty);
        }

        public void SfxPlay(string fileName, int kind, ExtPoint player, ExtPoint sound)
        {
            if (GlobalVars.Debug_Silent) {
                return;
            }

            if (!fSndReady) {
                Logger.Write("SFX kernel not initialized");
                return;
            }

            if (fSndVolume[kind] != 0) {
                fileName = NWResourceManager.GetAppPath() + fileName;
                //fileName = AuxUtils.ConvertPath(fileName);

                if (!File.Exists(fileName)) {
                    Logger.Write(string.Format("Media file \"{0}\" not exists", new object[]{ fileName }));
                } else {
                    if (fSndCount == MaxSounds) {
                        Logger.Write(string.Format("Limit of {0:D} songs reached", new object[]{ MaxSounds }));
                    } else {
                        int aPos = -1;
                        int num = fSndCount;
                        for (int idx = 0; idx < num; idx++) {
                            if (fSndList[idx].Kind == kind) {
                                bool isplaying = false;
                                fSndList[idx].Channel.isPlaying(ref isplaying);

                                if (kind == sk_Song || kind == sk_Ambient || (kind == sk_Sound && !isplaying)) {
                                    fSndList[idx].Channel.stop();
                                    fSndList[idx].Channel = null;
                                    fSndList[idx].Stream.release();
                                    fSndList[idx].Stream = null;
                                    aPos = idx;
                                    break;
                                }
                            }
                        }

                        MODE mode = MODE.SOFTWARE | MODE._3D; // | MODE.UNICODE
                        if (kind == sk_Sound) {
                            mode |= MODE.LOOP_OFF;
                        } else if (kind == sk_Song || kind == sk_Ambient) {
                            mode |= MODE.LOOP_NORMAL;
                        }

                        Sound stream = null;
                        RESULT res = fSystem.createStream(fileName, mode, ref stream);

                        if (res != RESULT.OK || stream == null) {
                            Logger.Write(Error.String(res));
                        } else {
                            if (aPos == -1) {
                                aPos = fSndCount;
                                fSndCount++;
                            }

                            Channel channel = null;
                            res = fSystem.playSound(CHANNELINDEX.FREE, stream, false, ref channel);

                            if (res != RESULT.OK || channel == null) {
                                Logger.Write(Error.String(res));
                            } else {
                                SoundData soundData = fSndList[aPos];
                                soundData.Kind = kind;
                                soundData.Stream = stream;
                                soundData.Channel = channel;

                                soundData.Channel.setVolume((float)(fSndVolume[kind] / 255.0f));
                                soundData.Channel.set3DMinMaxDistance(0, 80);

                                if (kind == sk_Sound) {
                                    Set3DProps(soundData.Channel, player, sound);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void Set3DProps(Channel channel, ExtPoint player, ExtPoint sound)
        {
            try {
                channel.set3DMinMaxDistance(0f, 80f);

                VECTOR zero, listenerpos, sourcePos;
                zero.x = 0.0f;
                zero.y = 0.0f;
                zero.z = 0.0f;

                listenerpos.x = player.X;
                listenerpos.y = 0.0f;
                listenerpos.z = player.Y;

                fSystem.set3DListenerAttributes(0, ref listenerpos, ref zero, ref zero, ref zero);

                sourcePos.x = sound.X;
                sourcePos.y = 0.0f;
                sourcePos.z = sound.Y;

                channel.set3DAttributes(ref sourcePos, ref zero);
            } catch (Exception ex) {
                Logger.Write("SoundEngine.set3DProps(): " + ex.Message);
            }
        }

        public void SfxSetVolume(int volume, int aKind)
        {
            if (GlobalVars.Debug_Silent) {
                return;
            }

            if (!fSndReady) {
                Logger.Write("SFX kernel not initialized");
                return;
            }

            fSndVolume[aKind] = volume;

            for (int i = 0; i < fSndCount; i++) {
                if (fSndList[i].Kind == aKind && fSndList[i].Channel != null) {
                    fSndList[i].Channel.setVolume((float)(volume / 255.0f));
                }
            }
        }

        public void SfxResume()
        {
            if (GlobalVars.Debug_Silent) {
                return;
            }

            if (!fSndReady) {
                Logger.Write("SFX kernel not initialized");
                return;
            }

            for (int i = 0; i < fSndCount; i++) {
                fSndList[i].Channel.setPaused(false);
            }
        }

        public void SfxSuspend()
        {
            if (GlobalVars.Debug_Silent) {
                return;
            }

            if (!fSndReady) {
                Logger.Write("SFX kernel not initialized");
                return;
            }

            for (int i = 0; i < fSndCount; i++) {
                fSndList[i].Channel.setPaused(true);
            }
        }
    }
}
