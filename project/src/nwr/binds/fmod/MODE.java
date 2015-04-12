/*
 *  FMOD Ex - C# Wrapper.
 *  Copyright (c) 2004-2014 by Firelight Technologies Pty, Ltd.
 *
 *  Java translation.
 *  Copyright (C) 2014 by Serg V. Zhdanovskih (aka Alchemist).
 */
package nwr.binds.fmod;

public enum MODE
{
    DEFAULT(0x00000000), // FMOD_DEFAULT is a default sound type.  Equivalent to all the defaults listed below.  FMOD_LOOP_OFF, FMOD_2D, FMOD_HARDWARE.
    LOOP_OFF(0x00000001), // For non looping sounds. (default).  Overrides FMOD_LOOP_NORMAL / FMOD_LOOP_BIDI.
    LOOP_NORMAL(0x00000002), // For forward looping sounds.
    LOOP_BIDI(0x00000004), // For bidirectional looping sounds. (only works on software mixed static sounds).
    _2D(0x00000008), // Ignores any 3d processing. (default).
    _3D(0x00000010), // Makes the sound positionable in 3D.  Overrides FMOD_2D.
    HARDWARE(0x00000020), // Attempts to make sounds use hardware acceleration. (default).
    SOFTWARE(0x00000040), // Makes sound reside in software.  Overrides FMOD_HARDWARE.  Use this for FFT, DSP, 2D multi speaker support and other software related features.
    CREATESTREAM(0x00000080), // Decompress at runtime, streaming from the source provided (standard stream).  Overrides FMOD_CREATESAMPLE.
    CREATESAMPLE(0x00000100), // Decompress at loadtime, decompressing or decoding whole file into memory as the target sample format. (standard sample).
    CREATECOMPRESSEDSAMPLE(0x00000200), // Load MP2, MP3, IMAADPCM or XMA into memory and leave it compressed.  During playback the FMOD software mixer will decode it in realtime as a 'compressed sample'.  Can only be used in combination with FMOD_SOFTWARE.
    OPENUSER(0x00000400), // Opens a user created static sample or stream. Use FMOD_CREATESOUNDEXINFO to specify format and/or read callbacks.  If a user created 'sample' is created with no read callback, the sample will be empty.  Use FMOD_Sound_Lock and FMOD_Sound_Unlock to place sound data into the sound if this is the case.
    OPENMEMORY(0x00000800), // "name_or_data" will be interpreted as a pointer to memory instead of filename for creating sounds.
    OPENMEMORY_POINT(0x10000000), // "name_or_data" will be interpreted as a pointer to memory instead of filename for creating sounds.  Use FMOD_CREATESOUNDEXINFO to specify length.  This differs to FMOD_OPENMEMORY in that it uses the memory as is, without duplicating the memory into its own buffers.  FMOD_SOFTWARE only.  Doesn't work with FMOD_HARDWARE, as sound hardware cannot access main ram on a lot of platforms.  Cannot be freed after open, only after Sound::release.   Will not work if the data is compressed and FMOD_CREATECOMPRESSEDSAMPLE is not used.
    OPENRAW(0x00001000), // Will ignore file format and treat as raw pcm.  User may need to declare if data is FMOD_SIGNED or FMOD_UNSIGNED
    OPENONLY(0x00002000), // Just open the file, dont prebuffer or read.  Good for fast opens for info, or when sound::readData is to be used.
    ACCURATETIME(0x00004000), // For FMOD_CreateSound - for accurate FMOD_Sound_GetLength / FMOD_Channel_SetPosition on VBR MP3, AAC and MOD/S3M/XM/IT/MIDI files.  Scans file first, so takes longer to open. FMOD_OPENONLY does not affect this.
    MPEGSEARCH(0x00008000), // For corrupted / bad MP3 files.  This will search all the way through the file until it hits a valid MPEG header.  Normally only searches for 4k.
    NONBLOCKING(0x00010000), // For opening sounds and getting streamed subsounds (seeking) asyncronously.  Use Sound::getOpenState to poll the state of the sound as it opens or retrieves the subsound in the background.
    UNIQUE(0x00020000), // Unique sound, can only be played one at a time
    _3D_HEADRELATIVE(0x00040000), // Make the sound's position, velocity and orientation relative to the listener.
    _3D_WORLDRELATIVE(0x00080000), // Make the sound's position, velocity and orientation absolute (relative to the world). (DEFAULT)
    _3D_INVERSEROLLOFF(0x00100000), // This sound will follow the inverse rolloff model where mindistance = full volume, maxdistance = where sound stops attenuating, and rolloff is fixed according to the global rolloff factor.  (DEFAULT)
    _3D_LINEARSQUAREROLLOFF(0x00400000), // This sound will follow a linear-square rolloff model where mindistance = full volume, maxdistance = silence.  Rolloffscale is ignored.
    _3D_LOGROLLOFF(0x00100000), // This sound will follow the standard logarithmic rolloff model where mindistance = full volume, maxdistance = where sound stops attenuating, and rolloff is fixed according to the global rolloff factor.  (default)
    _3D_LINEARROLLOFF(0x00200000), // This sound will follow a linear rolloff model where mindistance = full volume, maxdistance = silence.
    _3D_CUSTOMROLLOFF(0x04000000), // This sound will follow a rolloff model defined by Sound::set3DCustomRolloff / Channel::set3DCustomRolloff.
    _3D_IGNOREGEOMETRY(0x40000000), // Is not affect by geometry occlusion.  If not specified in Sound::setMode, or Channel::setMode, the flag is cleared and it is affected by geometry again.
    CDDA_FORCEASPI(0x00400000), // For CDDA sounds only - use ASPI instead of NTSCSI to access the specified CD/DVD device.
    CDDA_JITTERCORRECT(0x00800000), // For CDDA sounds only - perform jitter correction. Jitter correction helps produce a more accurate CDDA stream at the cost of more CPU time.
    UNICODE(0x01000000), // Filename is double-byte unicode.
    IGNORETAGS(0x02000000), // Skips id3v2/asf/etc tag checks when opening a sound, to reduce seek/read overhead when opening files (helps with CD performance).
    LOWMEM(0x08000000), // Removes some features from samples to give a lower memory overhead, like Sound::getName.
    LOADSECONDARYRAM(0x20000000), // Load sound into the secondary RAM of supported platform.  On PS3, sounds will be loaded into RSX/VRAM.
    VIRTUAL_PLAYFROMSTART(0x80000000); // For sounds that start virtual (due to being quiet or low importance), instead of swapping back to audible, and playing at the correct offset according to time, this flag makes the sound play from the start.

    public final int value;

    private MODE(int value)
    {
        this.value = value;
    }
}
