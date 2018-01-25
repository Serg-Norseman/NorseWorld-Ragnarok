/*
 *  "NorseWorld: Ragnarok", a roguelike game for PCs.
 *  Copyright (C) 2002-2008, 2014 by Serg V. Zhdanovskih (aka Alchemist).
 *
 *  this file is part of "NorseWorld: Ragnarok".
 *
 *  this program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  this program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using System;
using System.IO;
using System.Xml;
using NWR.Core;
using NWR.Core.Types;
using NWR.Game;
using ZRLib.Core;

namespace NWR.Database
{
    public sealed class FieldEntry : DataEntry
    {
        public sealed class FDTile
        {
            public ushort BackGround;
            public ushort ForeGround;
        }

        public sealed class FieldData
        {
            public FDTile[,] Tiles = new FDTile[18, 76];
        }

        public string LandSign;
        public string LayerSign;
        public FieldSource Source;
        public FieldData Data;

        private const string TemplateExt = ".tpl";

        public FieldEntry(object owner)
            : base(owner)
        {
            Data = null;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                if (Data != null) {
                    Data = null;
                }
            }
            base.Dispose(disposing);
        }

        public override void LoadXML(XmlNode element, FileVersion version)
        {
            try {
                base.LoadXML(element, version);

                LandSign = ReadElement(element, "LandID");
                LayerSign = ReadElement(element, "LayerID");
                Source = (FieldSource)Enum.Parse(typeof(FieldSource), ReadElement(element, "Source"));

                if (Source == FieldSource.fsTemplate) {
                    LoadTemplate();
                }
            } catch (Exception ex) {
                Logger.Write("FieldEntry.loadXML(): " + ex.Message);
                throw ex;
            }
        }

        private void LoadTemplate()
        {
            Data = new FieldData();

            int[, ] varTiles = new int[StaticData.FieldHeight, StaticData.FieldWidth];
            string varFile = "/fields/" + Sign + ".var";
            if (NWResourceManager.HasStream(varFile)) {
                try {
                    using (StreamReader isr = new StreamReader(NWResourceManager.LoadStream(varFile))) {

                        for (int y = 0; y < StaticData.FieldHeight; y++) {
                            string line = isr.ReadLine().Trim();
                            string[] parts = line.Split(' ');

                            for (int x = 0; x < StaticData.FieldWidth; x++) {
                                int @var = Convert.ToInt32(parts[x]);
                                varTiles[y, x] = @var;
                            }
                        }

                    }
                } catch (Exception ex) {
                    Logger.Write("FieldEntry.loadTemplate.2(): " + ex.Message);
                }
            }

            try {
                string tplFile = "fields/" + Sign + TemplateExt;
                using (StreamReader isr = new StreamReader(NWResourceManager.LoadStream(tplFile))) {

                    for (int y = 0; y < StaticData.FieldHeight; y++) {
                        string line = isr.ReadLine();

                        for (int x = 0; x < StaticData.FieldWidth; x++) {
                            char sym = line[x];

                            FDTile tile = new FDTile();
                            Data.Tiles[y, x] = tile;

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
                                    bgv = varTiles[y, x];
                                    break;
                                case 'o':
                                    bg = PlaceID.pid_Grass;
                                    fg = PlaceID.pid_Ting;
                                    fgv = varTiles[y, x];
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

                            tile.BackGround = AuxUtils.FitShort(bg, bgv);
                            tile.ForeGround = AuxUtils.FitShort(fg, fgv);
                        }
                    }

                }
            } catch (Exception ex) {
                Logger.Write("FieldEntry.loadTemplate.1(): " + ex.Message);
            }
        }
    }

}