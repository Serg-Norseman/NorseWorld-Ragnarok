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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

using System;
using System.Collections.Generic;
using System.IO;
using BSLib;
using NWR.Game;
using ZRLib.Core;
using ZRLib.Grammar;

namespace NWR.Creatures
{
    public sealed class NamesLib : BaseObject
    {
        private enum TLineKind
        {
            lkInf,
            lkFirst,
            lkMid,
            lkFinal,
            lkEnd
        }

        private class DicRecord
        {
            public string Name;
            public string Rus_name;
        }

        private const string Lat_end_f = "sdottir";
        private const string Rus_end_f = "сдоттир";
        private const string Lat_end_m = "sson";
        private const string Rus_end_m = "ссон";

        private readonly List<string> fFirstSlabs;
        private readonly List<string> fMidSlabs;
        private readonly List<string> fFinalSlabs;

        private List<DicRecord> Viking_f;
        private List<DicRecord> Viking_m;

        public const sbyte NameGen_NorseDic = 0;
        public const sbyte NameGen_RndSlabs = 1;

        public const sbyte Langbase_Latin = 0;
        public const sbyte Langbase_Rus = 1;

        static NamesLib()
        {
        }

        public NamesLib()
        {
            Viking_f = new List<DicRecord>();
            Viking_m = new List<DicRecord>();

            fFirstSlabs = new List<string>();
            fMidSlabs = new List<string>();
            fFinalSlabs = new List<string>();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                Viking_f.Clear();
                Viking_f = null;
                Viking_m.Clear();
                Viking_m = null;
            }
            base.Dispose(disposing);
        }

        private string GenNorseName(sbyte langBase, Gender gender)
        {
            string result = "";

            if (Viking_m.Count > 0 && Viking_f.Count > 0) {
                DicRecord fRec = null;
                string nd = "";
                if (gender != Gender.gMale) {
                    if (gender == Gender.gFemale) {
                        int fn = RandomHelper.GetRandom(Viking_f.Count - 1);
                        fRec = Viking_f[fn];
                        if (langBase != Langbase_Rus) {
                            if (langBase == Langbase_Latin) {
                                nd = Lat_end_f;
                            }
                        } else {
                            nd = Rus_end_f;
                        }
                    }
                } else {
                    int fn = RandomHelper.GetRandom(Viking_m.Count - 1);
                    fRec = Viking_m[fn];
                    if (langBase != Langbase_Rus) {
                        if (langBase == Langbase_Latin) {
                            nd = Lat_end_m;
                        }
                    } else {
                        nd = Rus_end_m;
                    }
                }
                int sn = RandomHelper.GetRandom(Viking_m.Count - 1);
                DicRecord sRec = Viking_m[sn];
                if (langBase != Langbase_Rus) {
                    if (langBase == Langbase_Latin) {
                        result = fRec.Name + " " + sRec.Name + nd;
                    }
                } else {
                    result = fRec.Rus_name + " " + sRec.Rus_name + nd;
                }
            }

            return result;
        }

        private string GenRndSlabs(sbyte langBase, Gender gender)
        {
            string result = "";

            int i = fFirstSlabs.Count;
            if (i > 0) {
                result += fFirstSlabs[RandomHelper.GetRandom(i)];
            }

            i = fMidSlabs.Count;
            if (i > 0) {
                result += fMidSlabs[RandomHelper.GetRandom(i)];
            }

            i = fFinalSlabs.Count;
            if (i > 0) {
                result += fFinalSlabs[RandomHelper.GetRandom(i)];
            }

            if (langBase == Langbase_Rus) {
                result = Grammar.getTransliterateName(result);
            } else {
                // dummy
            }
            return result;
        }

        public string GenerateName(string aLangExt, Gender gender, sbyte method)
        {
            sbyte langBase;
            if (aLangExt.CompareTo("ru") == 0) {
                langBase = Langbase_Rus;
            } else {
                langBase = Langbase_Latin;
            }

            string result = "";

            switch (method) {
                case NameGen_NorseDic:
                    result = GenNorseName(langBase, gender);
                    break;
                case NameGen_RndSlabs:
                    LoadNamesData("nameslib/elves_m.nmf");
                    result = GenRndSlabs(langBase, gender);
                    break;
            }

            return result;
        }

        public void InitNorseDic()
        {
            try {
                Stream stm = NWResourceManager.LoadStream("Names.txt");
                using (BinaryReader bis = new BinaryReader(stm)) {
                    Parser aParser = new Parser(bis, StaticData.DefEncoding);
                    while (aParser.Token != Parser.сptEOF) {
                        char token = aParser.Token;
                        if (token == Parser.сptSymbol) {
                            string symName = aParser.TokenString();
                            aParser.NextToken();
                            aParser.CheckToken('=');
                            aParser.NextToken();

                            if ((symName != "femaleCount") && (symName != "maleCount")) {
                                string engName = aParser.TokenString();
                                aParser.NextToken();
                                aParser.CheckToken(',');
                                aParser.NextToken();
                                string rusName = aParser.TokenString();
                                aParser.NextToken();

                                char g = symName[0];
                                //symName = symName.substring(1);
                                //TAuxUtils.StrToInt(symName);

                                DicRecord drec = new DicRecord();
                                drec.Name = engName;
                                drec.Rus_name = rusName;

                                if (g != 'f') {
                                    if (g == 'm') {
                                        Viking_m.Add(drec);
                                    }
                                } else {
                                    Viking_f.Add(drec);
                                }
                            }
                        } else {
                            aParser.NextToken();
                        }
                    }

                    aParser.Dispose();
                }
            } catch (Exception ex) {
                Logger.Write("TNWNameLib.InitNorseDic(): " + ex.Message);
            }
        }

        public void LoadNamesData(string fileName)
        {
            try {
                TLineKind line_kind = TLineKind.lkInf;

                using (StreamReader isr = new StreamReader(NWResourceManager.LoadStream(fileName))) {
                    string line;
                    while ((line = isr.ReadLine()) != null) {
                        if (line.Length > 0 && line[0] != '/') {
                            if (line[0] == '[') {
                                if (line.Contains("[inf]")) {
                                    line_kind = TLineKind.lkInf;
                                } else {
                                    if (line.Contains("[first]")) {
                                        line_kind = TLineKind.lkFirst;
                                    } else {
                                        if (line.Contains("[mid]")) {
                                            line_kind = TLineKind.lkMid;
                                        } else {
                                            if (line.Contains("[final]")) {
                                                line_kind = TLineKind.lkFinal;
                                            } else {
                                                if (line.Contains("[end]") || line.Contains("[stop]")) {
                                                    line_kind = TLineKind.lkEnd;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (line_kind != TLineKind.lkFirst) {
                                    if (line_kind != TLineKind.lkMid) {
                                        if (line_kind == TLineKind.lkFinal) {
                                            fFinalSlabs.Add(line);
                                        }
                                    } else {
                                        fMidSlabs.Add(line);
                                    }
                                } else {
                                    fFirstSlabs.Add(line);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("TNWNameLib.LoadData(): " + ex.Message);
            }
        }
    }
}
