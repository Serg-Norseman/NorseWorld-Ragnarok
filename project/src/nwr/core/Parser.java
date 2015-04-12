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
package nwr.core;

import java.io.IOException;
import jzrlib.core.BaseObject;
import jzrlib.external.BinaryInputStream;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Parser extends BaseObject
{
    public static final char cptEOF = (char) (0);
    public static final char cptSymbol = (char) (1);
    public static final char cptString = (char) (2);
    public static final char cptInteger = (char) (3);
    public static final char cptFloat = (char) (4);

    private final char[] fBuffer = new char[4096];
    private final BinaryInputStream fStream;

    private int fBufPos;
    private int fBufSize;
    private String fTemp;

    public int SourceLine;
    public char Token;

    private void ErrorStr(String message)
    {
        throw new ParserException(String.format("%s on line %d", new Object[]{message, this.SourceLine}));
    }

    private void NextChar() throws IOException
    {
        if (this.fBufPos == this.fBufSize - 1) {
            byte[] buf = new byte[4096];
            this.fBufSize = this.fStream.read(buf, 0, 4096);
            
            String str = new String(buf, "Cp1251");
            for (int i = 0; i < fBufSize; i++) {
                this.fBuffer[i] = (char) str.charAt(i);
            }
            this.fBufPos = 0;
        } else {
            this.fBufPos++;
        }
    }

    public Parser(BinaryInputStream Stream)
    {
        this.fStream = Stream;
        this.fBufSize = 0;
        this.fBufPos = 0;
        this.NextToken();
    }

    public final void CheckToken(char T)
    {
        if (this.Token != T) {
            switch (T) {
                case cptSymbol:
                    this.ErrorStr("Identifier expected");
                    break;
                case cptString:
                    this.ErrorStr("String expected");
                    break;
                case cptInteger:
                case cptFloat:
                    this.ErrorStr("Number expected");
                    break;
                default:
                    this.ErrorStr(String.format("\"%s\" expected", new Object[]{T}));
                    break;
            }
        }
    }

    public final char NextToken()
    {
        try {
            byte[] buf = new byte[4096];
            this.fTemp = "";

            char result;

            // skip blanks
            while (true) {
                if (this.fBufPos >= this.fBufSize) {
                    this.fBufSize = this.fStream.read(buf, 0, 4096);

                    String str = new String(buf, "Cp1251");
                    for (int i = 0; i < fBufSize; i++) {
                        this.fBuffer[i] = (char) str.charAt(i);
                    }

                    if (this.fBufSize <= 0) {
                        result = cptEOF;
                        this.Token = result;
                        return result;
                    }

                    this.fBufPos = 0;
                } else {
                    byte b = (byte) this.fBuffer[this.fBufPos];
                    if (b == 10) {
                        this.SourceLine++;
                    } else {
                        if (b >= 33 && b <= 255) {
                            break;
                        }
                    }

                    this.NextChar();
                }
            }

            //
            char c = (char) this.fBuffer[this.fBufPos];
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c == '_')) {
                this.fTemp += (this.fBuffer[this.fBufPos]);
                this.NextChar();
                while (true) {
                    char c5 = (char) this.fBuffer[this.fBufPos];
                    if (c5 < '0' || (c5 >= ':' && (c5 < 'A' || (c5 >= '[' && c5 != '_' && (c5 < 'a' || c5 >= '{'))))) {
                        break;
                    }
                    this.fTemp += (this.fBuffer[this.fBufPos]);
                    this.NextChar();
                }
                result = cptSymbol;
            } else if (c == '{') {
                while (true) {
                    char c6 = (char) this.fBuffer[this.fBufPos];
                    if (c6 != '{') {
                        break;
                    }

                    this.NextChar();

                    while (true) {
                        char c7 = (char) this.fBuffer[this.fBufPos];
                        if (c7 != '{') {
                            if (c7 == '}') {
                                this.NextChar();
                                if ((short) this.fBuffer[this.fBufPos] != 125) {
                                    break;
                                }
                            }
                        } else {
                            this.ErrorStr("Invalid string constant");
                        }
                        this.fTemp += (this.fBuffer[this.fBufPos]);
                        this.NextChar();
                    }
                }
                result = cptString;
            } else if (c == '$') {
                this.fTemp += (this.fBuffer[this.fBufPos]);
                this.NextChar();
                while (true) {
                    char c2 = (char) this.fBuffer[this.fBufPos];
                    if (c2 < '0' || (c2 >= ':' && (c2 < 'A' || (c2 >= 'G' && (c2 < 'a' || c2 >= 'g'))))) {
                        break;
                    }
                    this.fTemp += (this.fBuffer[this.fBufPos]);
                    this.NextChar();
                }
                result = cptInteger;
            } else if ((c == '-') || (c >= '0' && c <= '9')) {
                this.fTemp += (this.fBuffer[this.fBufPos]);
                this.NextChar();

                while (true) {
                    char c3 = (char) this.fBuffer[this.fBufPos];
                    if (c3 < '0' || c3 >= ':') {
                        break;
                    }
                    this.fTemp += (this.fBuffer[this.fBufPos]);
                    this.NextChar();
                }

                result = cptInteger;

                while (true) {
                    char c4 = (char) this.fBuffer[this.fBufPos];
                    if (c4 != '+' && (c4 < '-' || (c4 >= '/' && (c4 < '0' || (c4 >= ':' && c4 != 'E' && c4 != 'e'))))) {
                        break;
                    }
                    this.fTemp += (this.fBuffer[this.fBufPos]);
                    this.NextChar();
                    result = cptFloat;
                }
            } else {
                result = (char) this.fBuffer[this.fBufPos];
                this.NextChar();
            }

            this.Token = result;
            return result;
        } catch (IOException e) {
            return cptEOF;
        }
    }

    public final double TokenFloat()
    {
        return Double.parseDouble(this.TokenString());
    }

    public final int TokenInt()
    {
        return Integer.parseInt(this.TokenString());
    }

    public final String TokenString()
    {
        return this.fTemp;
    }
}
