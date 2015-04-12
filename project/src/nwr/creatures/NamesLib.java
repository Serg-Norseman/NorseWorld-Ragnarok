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
package nwr.creatures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import jzrlib.core.BaseObject;
import jzrlib.external.BinaryInputStream;
import jzrlib.grammar.Gender;
import jzrlib.grammar.Grammar;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.TextUtils;
import nwr.core.Parser;
import nwr.engine.ResourceManager;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class NamesLib extends BaseObject
{
    private enum TLineKind
    {
        lkInf,
        lkFirst,
        lkMid,
        lkFinal,
        lkEnd;
    }

    private static class DicRecord
    {
        public String name;
        public String rus_name;
    }

    private static final String lat_end_f = "sdottir";
    private static final String rus_end_f = "сдоттир";
    private static final String lat_end_m = "sson";
    private static final String rus_end_m = "ссон";

    private final ArrayList<String> FFirstSlabs;
    private final ArrayList<String> FMidSlabs;
    private final ArrayList<String> FFinalSlabs;

    private ArrayList<DicRecord> viking_f;
    private ArrayList<DicRecord> viking_m;

    public static final byte NameGen_NorseDic = 0;
    public static final byte NameGen_RndSlabs = 1;

    public static final byte Langbase_Latin = 0;
    public static final byte Langbase_Rus = 1;
    
    static {
    }

    public NamesLib()
    {
        this.viking_f = new ArrayList<>();
        this.viking_m = new ArrayList<>();

        this.FFirstSlabs = new ArrayList<>();
        this.FMidSlabs = new ArrayList<>();
        this.FFinalSlabs = new ArrayList<>();
    }

    @Override
    protected void dispose(boolean disposing)
    {
        if (disposing) {
            this.viking_f.clear();
            this.viking_f = null;
            this.viking_m.clear();
            this.viking_m = null;
        }
        super.dispose(disposing);
    }

    private String genNorseName(byte langBase, Gender gender)
    {
        String Result = "";

        if (this.viking_m.size() > 0 && this.viking_f.size() > 0) {
            DicRecord fRec = null;
            String nd = "";
            if (gender != Gender.gMale) {
                if (gender == Gender.gFemale) {
                    int fn = AuxUtils.getRandom(this.viking_f.size() - 1);
                    fRec = this.viking_f.get(fn);
                    if (langBase != Langbase_Rus) {
                        if (langBase == Langbase_Latin) {
                            nd = NamesLib.lat_end_f;
                        }
                    } else {
                        nd = NamesLib.rus_end_f;
                    }
                }
            } else {
                int fn = AuxUtils.getRandom(this.viking_m.size() - 1);
                fRec = this.viking_m.get(fn);
                if (langBase != Langbase_Rus) {
                    if (langBase == Langbase_Latin) {
                        nd = NamesLib.lat_end_m;
                    }
                } else {
                    nd = NamesLib.rus_end_m;
                }
            }
            int sn = AuxUtils.getRandom(this.viking_m.size() - 1);
            DicRecord sRec = this.viking_m.get(sn);
            if (langBase != Langbase_Rus) {
                if (langBase == Langbase_Latin) {
                    Result = fRec.name + " " + sRec.name + nd;
                }
            } else {
                Result = fRec.rus_name + " " + sRec.rus_name + nd;
            }
        }

        return Result;
    }

    private String genRndSlabs(byte langBase, Gender gender)
    {
        String result = "";

        int i = this.FFirstSlabs.size();
        if (i > 0) {
            result += this.FFirstSlabs.get(AuxUtils.getRandom(i));
        }

        i = this.FMidSlabs.size();
        if (i > 0) {
            result += this.FMidSlabs.get(AuxUtils.getRandom(i));
        }

        i = this.FFinalSlabs.size();
        if (i > 0) {
            result += this.FFinalSlabs.get(AuxUtils.getRandom(i));
        }

        if (langBase == Langbase_Rus) {
            result = Grammar.getTransliterateName(result);
        } else {
            // dummy
        }
        return result;
    }

    public final String generateName(String aLangExt, Gender gender, byte method)
    {
        byte langBase;
        if (aLangExt.compareTo("ru") == 0) {
            langBase = Langbase_Rus;
        } else {
            langBase = Langbase_Latin;
        }

        String result = "";

        switch (method) {
            case NameGen_NorseDic:
                result = this.genNorseName(langBase, gender);
                break;
            case NameGen_RndSlabs:
                this.loadNamesData("nameslib/elves_m.nmf");
                result = this.genRndSlabs(langBase, gender);
                break;
        }

        return result;
    }

    public final void initNorseDic()
    {
        try {
            InputStream is = ResourceManager.loadStream("Names.txt");

            BinaryInputStream bis = new BinaryInputStream(is, AuxUtils.binEndian);
            Parser aParser = new Parser(bis);
            while (aParser.Token != Parser.cptEOF) {
                char token = aParser.Token;
                if (token == Parser.cptSymbol) {
                    String symName = aParser.TokenString();
                    aParser.NextToken();
                    aParser.CheckToken('=');
                    aParser.NextToken();

                    if (!TextUtils.equals(symName, "femaleCount") && !TextUtils.equals(symName, "maleCount")) {
                        String engName = aParser.TokenString();
                        aParser.NextToken();
                        aParser.CheckToken(',');
                        aParser.NextToken();
                        String rusName = aParser.TokenString();
                        aParser.NextToken();

                        char g = symName.charAt(0);
                        //symName = symName.substring(1);
                        //TAuxUtils.StrToInt(symName);

                        DicRecord drec = new DicRecord();
                        drec.name = engName;
                        drec.rus_name = rusName;

                        if (g != 'f') {
                            if (g == 'm') {
                                this.viking_m.add(drec);
                            }
                        } else {
                            this.viking_f.add(drec);
                        }
                    }
                } else {
                    aParser.NextToken();
                }
            }

            aParser.dispose();
            bis.close();
        } catch (IOException ex) {
            Logger.write("TNWNameLib.InitNorseDic.io(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("TNWNameLib.InitNorseDic(): " + ex.getMessage());
        }
    }

    public final void loadNamesData(String fileName)
    {
        try {
            TLineKind line_kind = TLineKind.lkInf;

            InputStreamReader isr = new InputStreamReader(ResourceManager.loadStream(fileName));
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) != '/') {
                    if (line.charAt(0) == '[') {
                        if (line.contains("[inf]")) {
                            line_kind = TLineKind.lkInf;
                        } else {
                            if (line.contains("[first]")) {
                                line_kind = TLineKind.lkFirst;
                            } else {
                                if (line.contains("[mid]")) {
                                    line_kind = TLineKind.lkMid;
                                } else {
                                    if (line.contains("[final]")) {
                                        line_kind = TLineKind.lkFinal;
                                    } else {
                                        if (line.contains("[end]") || line.contains("[stop]")) {
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
                                    this.FFinalSlabs.add(line);
                                }
                            } else {
                                this.FMidSlabs.add(line);
                            }
                        } else {
                            this.FFirstSlabs.add(line);
                        }
                    }
                }
            }
            br.close();
        } catch (IOException ex) {
            Logger.write("TNWNameLib.LoadData.io(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("TNWNameLib.LoadData(): " + ex.getMessage());
        }
    }
}
