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

import jzrlib.utils.AuxUtils;
import jzrlib.core.BaseObject;
import jzrlib.external.BinaryInputStream;
import jzrlib.utils.Logger;
import nwr.core.Parser;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Font extends BaseObject
{
    private final class TGEFontChar
    {
        public short Offset;
        public byte Width;
        public byte Adjust;
    }

    protected int fColor;
    protected BaseImage fImage;
    protected BaseScreen fScreen;
    protected TGEFontChar[] fChars;
    protected int fCharColor;
    protected String fImageFile;

    public int Height;
    public String Name;

    public Font()
    {
        this.fChars = new TGEFontChar[256];
        for (int i = 0; i < 256; i++) {
            this.fChars[i] = new TGEFontChar();
        }
    }

    public Font(BaseScreen screen, String fileName)
    {
        this();
        this.fScreen = screen;
        this.loadFromFile(fileName);
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.fImage != null) {
                this.fImage.dispose();
            }
        }
        super.dispose(disposing);
    }

    public final int getColor()
    {
        int num = this.fColor;
        int r = (num >> 16) & 0xff;
        int g = (num >> 8) & 0xff;
        int b = num & 0xff;
        return BaseScreen.RGB(r, g, b);
    }

    public final void setColor(int value)
    {
        if (this.fColor != value) {
            this.fImage.replaceColor(this.fCharColor, value);
            this.fColor = value;
        }
    }

    public final void loadFromFile(String fileName)
    {
        try {
            InputStream is = ResourceManager.loadStream(fileName);

            BinaryInputStream bis = new BinaryInputStream(is, AuxUtils.binEndian);
            Parser parser = new Parser(bis);
            try {
                this.fImageFile = "";
                while (parser.Token != Parser.cptEOF) {
                    char token = parser.Token;
                    if (token != Parser.cptSymbol) {
                        if (token != '@') {
                            if (token == '{') {
                                do {
                                    parser.NextToken();
                                } while (parser.Token != '}');
                            }
                        } else {
                            parser.NextToken();
                            int c = parser.TokenInt();
                            parser.NextToken();
                            this.fChars[c].Width = (byte) parser.TokenInt();
                            parser.NextToken();
                            this.fChars[c].Adjust = (byte) parser.TokenInt();
                        }
                    } else {
                        String id = parser.TokenString();
                        parser.NextToken();
                        parser.CheckToken('=');
                        parser.NextToken();
                        String value = parser.TokenString();

                        if (id.compareTo("Name") == 0) {
                            this.Name = value;
                        } else {
                            if (id.compareTo("ImageFile") == 0) {
                                this.fImageFile = value;
                            } else {
                                if (id.compareTo("Height") == 0) {
                                    this.Height = Integer.parseInt(value);
                                } else {
                                    if (id.compareTo("CharDivider") == 0) {
                                        //this.fDivider = TAuxUtils.StrToInt(value);
                                    } else {
                                        if (id.compareTo("CharColor") == 0) {
                                            this.fCharColor = Integer.parseInt(value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    parser.NextToken();
                }

                int offset = 1;
                for (int b = 0; b <= 255; b++) {
                    if (this.fChars[b].Width > 0) {
                        this.fChars[b].Offset = (short) offset;
                        offset = offset + (int) this.fChars[b].Width + 1;
                    }
                }

                this.fImageFile = AuxUtils.changeExtension(fileName, ".tga");

                this.fImage = this.fScreen.createImage();
                this.fImage.PaletteMode = true;
                
                InputStream is1 = ResourceManager.loadStream(this.fImageFile);
                BinaryInputStream bis1 = new BinaryInputStream(is1, AuxUtils.binEndian);
                this.fImage.loadFromStream(bis1, BaseScreen.clBlack);

                this.fColor = BaseScreen.clWhite;
            } finally {
                if (parser != null) {
                    parser.dispose();
                }
                if (bis != null) {
                    bis.close();
                }
            }
        } catch (IOException ex) {
            Logger.write("Font.loadFromFile.io(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("Font.loadFromFile(): " + ex.getMessage());
        }
    }

    public final boolean isValidChar(char aChar)
    {
        try {
            String str = String.valueOf(aChar);
            byte[] buf = str.getBytes("Cp1251");
            int idx = (int) (buf[0] & 0xff);
            return (this.fChars[idx].Width > 0);
        } catch (UnsupportedEncodingException ex) {
            return false;
        }
    }

    public final int getTextWidth(String text)
    {
        int result = 0;
        if (text == null || text.length() == 0) {
            return result;
        }

        try {
            byte[] data = text.getBytes("Cp1251");
            for (int i = 0; i < data.length; i++) {
                int idx = (data[i] & 0xff);
                TGEFontChar fc = this.fChars[idx];

                int symW = fc.Width;
                int adj = fc.Adjust;
                if (symW > 0) {
                    if (adj > 0 && i > 0) {
                        result -= adj;
                    }
                    result = result + symW + 1;
                    if (adj < 0) {
                        result += adj;
                    }
                }
            }
            result--;
        } catch (UnsupportedEncodingException ex) {

        }

        return result;
    }

    public final void drawText(BaseScreen screen, int aX, int aY, String text)
    {
        if (text == null || text.length() == 0) {
            return;
        }

        try {
            int symH = this.Height;
            int xx = aX;

            byte[] data = text.getBytes("Cp1251");
            for (int i = 0; i < data.length; i++) {
                int idx = (data[i] & 0xff);
                TGEFontChar chr = this.fChars[idx];

                int symW = chr.Width;
                if (symW > 0) {
                    int adj = chr.Adjust;
                    int offX = chr.Offset;
                    if (adj > 0 && i > 0) {
                        xx -= adj;
                    }
                    screen.drawImage(xx, aY, offX, 0, symW, symH, this.fImage, 255);
                    xx = xx + symW + 1;
                    if (adj < 0) {
                        xx += adj;
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {

        }
    }
}
