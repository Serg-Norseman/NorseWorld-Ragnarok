/*
 *  FMOD Ex - C# Wrapper.
 *  Copyright (c) 2004-2014 by Firelight Technologies Pty, Ltd.
 *
 *  Java translation.
 *  Copyright (C) 2014 by Serg V. Zhdanovskih (aka Alchemist).
 */
package nwr.binds.fmod;

public enum CHANNELINDEX
{
    FREE(-1), // For a channel index, FMOD chooses a free voice using the priority system.
    REUSE(-2); // For a channel index, re-use the channel handle that was passed in.

    public final int value;

    private CHANNELINDEX(int value)
    {
        this.value = value;
    }
}
