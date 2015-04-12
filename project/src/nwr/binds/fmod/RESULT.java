/*
 *  FMOD Ex - C# Wrapper.
 *  Copyright (c) 2004-2014 by Firelight Technologies Pty, Ltd.
 *
 *  Java translation.
 *  Copyright (C) 2014 by Serg V. Zhdanovskih (aka Alchemist).
 */
package nwr.binds.fmod;

public enum RESULT
{
    OK, // No errors.
    ERR_ALREADYLOCKED, // Tried to call lock a second time before unlock was called.
    ERR_BADCOMMAND, // Tried to call a function on a data type that does not allow this type of functionality (ie calling Sound::lock on a streaming sound).
    ERR_CDDA_DRIVERS, // Neither NTSCSI nor ASPI could be initialised.
    ERR_CDDA_INIT, // An error occurred while initialising the CDDA subsystem.
    ERR_CDDA_INVALID_DEVICE, // Couldn't find the specified device.
    ERR_CDDA_NOAUDIO, // No audio tracks on the specified disc.
    ERR_CDDA_NODEVICES, // No CD/DVD devices were found.
    ERR_CDDA_NODISC, // No disc present in the specified drive.
    ERR_CDDA_READ, // A CDDA read error occurred.
    ERR_CHANNEL_ALLOC, // Error trying to allocate a channel.
    ERR_CHANNEL_STOLEN, // The specified channel has been reused to play another sound.
    ERR_COM, // A Win32 COM related error occured. COM failed to initialize or a QueryInterface failed meaning a Windows codec or driver was not installed properly.
    ERR_DMA, // DMA Failure.  See debug output for more information.
    ERR_DSP_CONNECTION, // DSP connection error.  Connection possibly caused a cyclic dependancy.
    ERR_DSP_FORMAT, // DSP Format error.  A DSP unit may have attempted to connect to this network with the wrong format.
    ERR_DSP_NOTFOUND, // DSP connection error.  Couldn't find the DSP unit specified.
    ERR_DSP_RUNNING, // DSP error.  Cannot perform this operation while the network is in the middle of running.  This will most likely happen if a connection or disconnection is attempted in a DSP callback.
    ERR_DSP_TOOMANYCONNECTIONS, // DSP connection error.  The unit being connected to or disconnected should only have 1 input or output.
    ERR_FILE_BAD, // Error loading file.
    ERR_FILE_COULDNOTSEEK, // Couldn't perform seek operation.  This is a limitation of the medium (ie netstreams) or the file format.
    ERR_FILE_DISKEJECTED, // Media was ejected while reading.
    ERR_FILE_EOF, // End of file unexpectedly reached while trying to read essential data (truncated data?).
    ERR_FILE_NOTFOUND, // File not found.
    ERR_FILE_UNWANTED, // Unwanted file access occured.
    ERR_FORMAT, // Unsupported file or audio format.
    ERR_HTTP, // A HTTP error occurred. This is a catch-all for HTTP errors not listed elsewhere.
    ERR_HTTP_ACCESS, // The specified resource requires authentication or is forbidden.
    ERR_HTTP_PROXY_AUTH, // Proxy authentication is required to access the specified resource.
    ERR_HTTP_SERVER_ERROR, // A HTTP server error occurred.
    ERR_HTTP_TIMEOUT, // The HTTP request timed out.
    ERR_INITIALIZATION, // FMOD was not initialized correctly to support this function.
    ERR_INITIALIZED, // Cannot call this command after System::init.
    ERR_INTERNAL, // An error occured that wasn't supposed to.  Contact support.
    ERR_INVALID_ADDRESS, // On Xbox 360, this memory address passed to FMOD must be physical, (ie allocated with XPhysicalAlloc.)
    ERR_INVALID_FLOAT, // Value passed in was a NaN, Inf or denormalized float.
    ERR_INVALID_HANDLE, // An invalid object handle was used.
    ERR_INVALID_PARAM, // An invalid parameter was passed to this function.
    ERR_INVALID_POSITION, // An invalid seek position was passed to this function.
    ERR_INVALID_SPEAKER, // An invalid speaker was passed to this function based on the current speaker mode.
    ERR_INVALID_SYNCPOINT, // The syncpoint did not come from this sound handle.
    ERR_INVALID_VECTOR, // The vectors passed in are not unit length, or perpendicular.
    ERR_MAXAUDIBLE, // Reached maximum audible playback count for this sound's soundgroup.
    ERR_MEMORY, // Not enough memory or resources.
    ERR_MEMORY_CANTPOINT, // Can't use FMOD_OPENMEMORY_POINT on non PCM source data, or non mp3/xma/adpcm data if CREATECOMPRESSEDSAMPLE was used.
    ERR_MEMORY_SRAM, // Not enough memory or resources on console sound ram.
    ERR_NEEDS2D, // Tried to call a command on a 3d sound when the command was meant for 2d sound.
    ERR_NEEDS3D, // Tried to call a command on a 2d sound when the command was meant for 3d sound.
    ERR_NEEDSHARDWARE, // Tried to use a feature that requires hardware support.  (ie trying to play a GCADPCM compressed sound in software on Wii).
    ERR_NEEDSSOFTWARE, // Tried to use a feature that requires the software engine.  Software engine has either been turned off, or command was executed on a hardware channel which does not support this feature.
    ERR_NET_CONNECT, // Couldn't connect to the specified host.
    ERR_NET_SOCKET_ERROR, // A socket error occurred.  This is a catch-all for socket-related errors not listed elsewhere.
    ERR_NET_URL, // The specified URL couldn't be resolved.
    ERR_NET_WOULD_BLOCK, // Operation on a non-blocking socket could not complete immediately.
    ERR_NOTREADY, // Operation could not be performed because specified sound is not ready.
    ERR_OUTPUT_ALLOCATED, // Error initializing output device, but more specifically, the output device is already in use and cannot be reused.
    ERR_OUTPUT_CREATEBUFFER, // Error creating hardware sound buffer.
    ERR_OUTPUT_DRIVERCALL, // A call to a standard soundcard driver failed, which could possibly mean a bug in the driver or resources were missing or exhausted.
    ERR_OUTPUT_ENUMERATION, // Error enumerating the available driver list. List may be inconsistent due to a recent device addition or removal.
    ERR_OUTPUT_FORMAT, // Soundcard does not support the minimum features needed for this soundsystem (16bit stereo output).
    ERR_OUTPUT_INIT, // Error initializing output device.
    ERR_OUTPUT_NOHARDWARE, // FMOD_HARDWARE was specified but the sound card does not have the resources nescessary to play it.
    ERR_OUTPUT_NOSOFTWARE, // Attempted to create a software sound but no software channels were specified in System::init.
    ERR_PAN, // Panning only works with mono or stereo sound sources.
    ERR_PLUGIN, // An unspecified error has been returned from a 3rd party plugin.
    ERR_PLUGIN_INSTANCES, // The number of allowed instances of a plugin has been exceeded
    ERR_PLUGIN_MISSING, // A requested output, dsp unit type or codec was not available.
    ERR_PLUGIN_RESOURCE, // A resource that the plugin requires cannot be found. (ie the DLS file for MIDI playback)
    ERR_PRELOADED, // The specified sound is still in use by the event system, call EventSystem::unloadFSB before trying to release it.
    ERR_PROGRAMMERSOUND, // The specified sound is still in use by the event system, wait for the event which is using it finish with it.
    ERR_RECORD, // An error occured trying to initialize the recording device.
    ERR_REVERB_INSTANCE, // Specified Instance in REVERB_PROPERTIES couldn't be set. Most likely because another application has locked the EAX4 FX slot.
    ERR_SUBSOUND_ALLOCATED, // This subsound is already being used by another sound, you cannot have more than one parent to a sound.  Null out the other parent's entry first.
    ERR_SUBSOUND_CANTMOVE, // Shared subsounds cannot be replaced or moved from their parent stream, such as when the parent stream is an FSB file.
    ERR_SUBSOUND_MODE, // The subsound's mode bits do not match with the parent sound's mode bits.  See documentation for function that it was called with.
    ERR_SUBSOUNDS, // The error occured because the sound referenced contains subsounds.  (ie you cannot play the parent sound as a static sample, only its subsounds.)
    ERR_TAGNOTFOUND, // The specified tag could not be found or there are no tags.
    ERR_TOOMANYCHANNELS, // The sound created exceeds the allowable input channel count.  This can be increased using the maxinputchannels parameter in System::setSoftwareFormat.
    ERR_UNIMPLEMENTED, // Something in FMOD hasn't been implemented when it should be! contact support!
    ERR_UNINITIALIZED, // This command failed because System::init or System::setDriver was not called.
    ERR_UNSUPPORTED, // A command issued was not supported by this object.  Possibly a plugin without certain callbacks specified.
    ERR_UPDATE, // An error caused by System::update occured.
    ERR_VERSION, // The version number of this file format is not supported.

    ERR_EVENT_FAILED, // An Event failed to be retrieved, most likely due to 'just fail' being specified as the max playbacks behavior.
    ERR_EVENT_INFOONLY, // Can't execute this command on an EVENT_INFOONLY event.
    ERR_EVENT_INTERNAL, // An error occured that wasn't supposed to.  See debug log for reason.
    ERR_EVENT_MAXSTREAMS, // Event failed because 'Max streams' was hit when FMOD_INIT_FAIL_ON_MAXSTREAMS was specified.
    ERR_EVENT_MISMATCH, // FSB mis-matches the FEV it was compiled with.
    ERR_EVENT_NAMECONFLICT, // A category with the same name already exists.
    ERR_EVENT_NOTFOUND, // The requested event, event group, event category or event property could not be found.
    ERR_EVENT_NEEDSSIMPLE, // Tried to call a function on a complex event that's only supported by simple events.
    ERR_EVENT_GUIDCONFLICT, // An event with the same GUID already exists.
    ERR_EVENT_ALREADY_LOADED, // The specified project has already been loaded. Having multiple copies of the same project loaded simultaneously is forbidden.

    ERR_MUSIC_UNINITIALIZED, // Music system is not initialized probably because no music data is loaded.
    ERR_MUSIC_NOTFOUND, // The requested music entity could not be found.
    ERR_MUSIC_NOCALLBACK; // The music callback is required, but it has not been set.

    public static RESULT forValue(int value)
    {
        return values()[value];
    }
}
