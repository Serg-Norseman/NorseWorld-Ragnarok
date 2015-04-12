/*
 *  FMOD Ex - C# Wrapper.
 *  Copyright (c) 2004-2014 by Firelight Technologies Pty, Ltd.
 *
 *  Java translation.
 *  Copyright (C) 2014 by Serg V. Zhdanovskih (aka Alchemist).
 */
package nwr.binds.fmod;

public enum OUTPUTTYPE
{
    AUTODETECT, // Picks the best output mode for the platform.  This is the default.

    UNKNOWN, // All             - 3rd party plugin, unknown.  This is for use with System::getOutput only.
    NOSOUND, // All             - All calls in this mode succeed but make no sound.
    WAVWRITER, // All             - Writes output to fmodoutput.wav by default.  Use the 'extradriverdata' parameter in System::init, by simply passing the filename as a string, to set the wav filename.
    NOSOUND_NRT, // All             - Non-realtime version of _NOSOUND.  User can drive mixer with System::update at whatever rate they want.
    WAVWRITER_NRT, // All             - Non-realtime version of _WAVWRITER.  User can drive mixer with System::update at whatever rate they want.

    DSOUND, // Win32/Win64     - DirectSound output.                       (Default on Windows XP and below)
    WINMM, // Win32/Win64     - Windows Multimedia output.
    WASAPI, // Win32           - Windows Audio Session API.                (Default on Windows Vista and above)
    ASIO, // Win32           - Low latency ASIO 2.0 driver.
    OSS, // Linux/Linux64   - Open Sound System output.                 (Default on Linux, third preference)
    ALSA, // Linux/Linux64   - Advanced Linux Sound Architecture output. (Default on Linux, second preference if available)
    ESD, // Linux/Linux64   - Enlightment Sound Daemon output.
    PULSEAUDIO, // Linux/Linux64   - PulseAudio output.                        (Default on Linux, first preference if available)
    COREAUDIO, // Mac             - Macintosh CoreAudio output.               (Default on Mac)
    XBOX360, // Xbox 360        - Native Xbox360 output.                    (Default on Xbox 360)
    PSP, // PSP             - Native PSP output.                        (Default on PSP)
    PS3, // PS3             - Native PS3 output.                        (Default on PS3)
    NGP, // NGP             - Native NGP output.                        (Default on NGP)
    WII, // Wii                - Native Wii output.                        (Default on Wii)
    _3DS, // 3DS             - Native 3DS output                         (Default on 3DS)
    AUDIOTRACK, // Android         - Java Audio Track output.                  (Default on Android 2.2 and below)
    OPENSL, // Android         - OpenSL ES output.                         (Default on Android 2.3 and above)
    NACL, // Native Client   - Native Client output.                     (Default on Native Client)
    WIIU, // Wii U           - Native Wii U output.                      (Default on Wii U)

    MAX; // Maximum number of output types supported.
}
