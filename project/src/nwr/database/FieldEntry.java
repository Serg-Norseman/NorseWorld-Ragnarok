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
package nwr.database;

import jzrlib.core.FileVersion;
import jzrlib.utils.Logger;
import jzrlib.utils.TypeUtils;
import nwr.core.StaticData;
import nwr.engine.ResourceManager;
import nwr.core.types.PlaceID;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class FieldEntry extends DataEntry
{
    public static final class FDTile
    {
        public short backGround;
        public short foreGround;
    }

    public static final class FieldData
    {
        public FDTile[][] Tiles = new FDTile[18][76];
    }

    public String LandSign;
    public String LayerSign;
    public FieldSource Source;
    public FieldData Data;

    private static final String TemplateExt = ".tpl";

    public FieldEntry(Object owner)
    {
        super(owner);
        this.Data = null;
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            if (this.Data != null) {
                this.Data = null;
            }
        }
        super.dispose(disposing);
    }

    @Override
    public void loadXML(Element element, FileVersion version) throws XMLStreamException
    {
        try {
            super.loadXML(element, version);

            this.LandSign = DataEntry.readElement(element, "LandID");
            this.LayerSign = DataEntry.readElement(element, "LayerID");
            this.Source = FieldSource.valueOf(DataEntry.readElement(element, "Source"));

            if (this.Source == FieldSource.fsTemplate) {
                this.loadTemplate();
            }
        } catch (Exception ex) {
            Logger.write("FieldEntry.loadXML(): " + ex.getMessage());
            throw ex;
        }
    }

    private void loadTemplate()
    {
        this.Data = new FieldData();

        int[][] varTiles = new int[StaticData.FieldHeight][StaticData.FieldWidth];
        String varFile = "/fields/" + this.Sign + ".var";
        if (ResourceManager.hasStream(varFile)) {
            try {
                InputStreamReader isr = new InputStreamReader(ResourceManager.loadStream(varFile));
                BufferedReader in = new BufferedReader(isr);

                for (int y = 0; y < StaticData.FieldHeight; y++) {
                    String line = in.readLine().trim();
                    String[] parts = line.split(" ");

                    for (int x = 0; x < StaticData.FieldWidth; x++) {
                        int var = Integer.parseInt(parts[x]);
                        varTiles[y][x] = var;
                    }
                }

                in.close();
            } catch (IOException ex) {
                Logger.write("FieldEntry.loadTemplate.2(): " + ex.getMessage());
            }
        }

        try {
            String tplFile = "fields/" + this.Sign + TemplateExt;
            InputStreamReader isr = new InputStreamReader(ResourceManager.loadStream(tplFile));
            BufferedReader in = new BufferedReader(isr);

            for (int y = 0; y < StaticData.FieldHeight; y++) {
                String line = in.readLine();

                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    char sym = line.charAt(x);

                    FDTile tile = new FDTile();
                    this.Data.Tiles[y][x] = tile;

                    int bg = 0;
                    int fg = 0;
                    int bgv = 0;
                    int fgv = 0;

                    switch (sym) {
                        case '.':
                            bg = PlaceID.pid_Grass;
                            break;
                        case ',':
                            bg = PlaceID.pid_Floor;
                            break;
                        case '~':
                            bg = PlaceID.pid_Water;
                            break;
                        case '*':
                            bg = PlaceID.pid_Space;
                            break;
                        case '^':
                            bg = PlaceID.pid_Ground;
                            fg = PlaceID.pid_Mountain;
                            break;
                        case ':':
                            bg = PlaceID.pid_Floor;
                            break;
                        case 'x':
                            bg = PlaceID.pid_Ground;
                            fg = PlaceID.pid_Rock;
                            break;

                        case '+':
                            bg = PlaceID.pid_Bifrost;
                            bgv = varTiles[y][x];
                            break;
                        case 'o':
                            bg = PlaceID.pid_Grass;
                            fg = PlaceID.pid_Ting;
                            fgv = varTiles[y][x];
                            break;

                        case '1':
                            bg = PlaceID.pid_cr_y;
                            break;
                        case '2':
                            bg = PlaceID.pid_cr_r;
                            break;
                        case '3':
                            bg = PlaceID.pid_cr_b;
                            break;
                        case '4':
                            bg = PlaceID.pid_cr_a;
                            break;
                        case '5':
                            bg = PlaceID.pid_cr_l;
                            break;
                        case '6':
                            bg = PlaceID.pid_cr_w;
                            break;
                        case '7':
                            bg = PlaceID.pid_cr_k;
                            break;
                        case '8':
                            bg = PlaceID.pid_cr_g;
                            break;

                        case '\\':
                        case '/':
                            bg = PlaceID.pid_Ground;
                            break;
                    }

                    tile.backGround = (short) TypeUtils.fitShort(bg, bgv);
                    tile.foreGround = (short) TypeUtils.fitShort(fg, fgv);
                }
            }

            in.close();
        } catch (IOException ex) {
            Logger.write("FieldEntry.loadTemplate.1(): " + ex.getMessage());
        }
    }
}
