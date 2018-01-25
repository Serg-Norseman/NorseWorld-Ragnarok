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
using System.Text;
using BSLib;
using NWR.Core;
using NWR.Core.Types;
using NWR.Creatures;
using NWR.Creatures.Brain;
using NWR.Creatures.Specials;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.Game.Ghosts;
using NWR.Game.Scores;
using NWR.Game.Story;
using NWR.GUI;
using NWR.GUI.Controls;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;
using ZRLib.Engine;
using ZRLib.Grammar;
using ZRLib.Map;

namespace NWR.GUI
{
    public sealed class NWMainWindow : BaseMainWindow
    {
        // graphics styles
        public const int RGS_CLASSIC = 0;
        public const int RGS_MODERN = 1;

        // Objects paint mode
        public const int OPM_FIELD = 0;
        public const int OPM_LOCAL = 1;
        public const int OPM_ISO = 2;

        // fields
        private bool fAutoPickup;
        private bool fCircularFOV;
        private bool fCursorValid;
        private bool fExtremeMode;
        private NWGameSpace fGameSpace;
        private GameState fGameState;
        private Locale fLocale;
        private string fLanguage;
        private GameScreen fMainScreen;
        private int fMouseMapX;
        private int fMouseMapY;
        private ExtRect fMV_Rect;
        private int fOldStyle;
        private ScoresList fScoresList;
        private GhostsList fGhostsList;
        private int fSongsVolume;
        private int fSoundsVolume;
        private TargetObj fTargetData;
        private TextBox fTextBox;
        private bool fHideCtlPanel;
        private bool fHideInfoPanel;
        private bool fHideLocMap;
        private bool fInventoryOnlyIcons;
        private ExtPoint fLocMapSize;
        private ExtRect fLV_Rect;
        private BaseImage fScrImage;
        private IsoViewer fViewer;

        private AboutWindow fAboutDialog;
        private AlchemyWindow fAlchemyWindow;
        private ConsoleWindow fConsoleWindow;
        private ExchangeWindow fExchangeWindow;
        private FilesWindow fFilesWindow;
        private HeroWindow fHeroWindow;
        private InputWindow fInputWindow;
        private IntroWindow fIntroDialog;
        private InventoryWindow fInventoryWindow;
        private KnowledgesWindow fKnowledgesWindow;
        private MapWindow fMapDialog;
        private MenuWindow fMenuDialog;
        private MessageWindow fMessageWindow;
        private NPCWindow fNPCWindow;
        private OptionsWindow fOptions;
        private PartyWindow fPartyWindow;
        private ProgressWindow fProgressWindow;
        private RecruitWindow fRecruitWindow;
        private SelfWindow fSelfWindow;
        private SkillsWindow fSkillsWindow;
        private SmithyWindow fSmithyWindow;
        private StartupWindow fStartupDialog;
        private TeachWindow fTeachWindow;
        private JournalWindow fJournalWindow;
        private DivinationWindow fDivinationWindow;

        private readonly SoundEngine fSoundEngine;
        //private readonly ScriptEngine FScriptEngine;

        private int IP_Top = 150;
        private int IP_Left = 640;
        private int IP_Right = 792;

        public ImageList Resources;
        public ImageList Symbols;

        public int DialogVisibleRefs;

        public NWMainWindow()
            : base(800, 600)
        {
            Caption = StaticData.Rs_GameName;
            fSoundEngine = new SoundEngine();
            //FScriptEngine = (new ScriptEngineManager()).getEngineByName("JavaScript");
        }

        public bool AutoPickup
        {
            get {
                return fAutoPickup;
            }
            set {
                if (fAutoPickup != value) {
                    fAutoPickup = value;
                }
            }
        }

        public bool CircularFOV
        {
            get {
                return fCircularFOV;
            }
            set {
                if (fCircularFOV != value) {
                    fCircularFOV = value;
                }
            }
        }

        public bool ExtremeMode
        {
            get {
                return fExtremeMode;
            }
            set {
                if (fExtremeMode != value) {
                    fExtremeMode = value;
                }
            }
        }

        public NWGameSpace GameSpace
        {
            get {
                return fGameSpace;
            }
        }

        public GameState GameState
        {
            get {
                return fGameState;
            }
        }

        public Locale Locale
        {
            get {
                return fLocale;
            }
        }

        public string Language
        {
            get {
                return fLanguage;
            }
            set {
                if ((fLanguage != value) && fLocale.SetLang(value)) {
                    fLanguage = value;
                    ChangeLang();
                }
            }
        }

        public GameScreen MainScreen
        {
            get {
                return fMainScreen;
            }
        }

        public int SongsVolume
        {
            get {
                return fSongsVolume;
            }
            set {
                fSongsVolume = value;
                fSoundEngine.SfxSetVolume(fSongsVolume, SoundEngine.sk_Song);
            }
        }

        public int SoundsVolume
        {
            get {
                return fSoundsVolume;
            }
            set {
                fSoundsVolume = value;
                fSoundEngine.SfxSetVolume(fSoundsVolume, SoundEngine.sk_Ambient);
                fSoundEngine.SfxSetVolume(fSoundsVolume, SoundEngine.sk_Sound);
            }
        }

        public TargetObj TargetData
        {
            get {
                return fTargetData;
            }
        }

        public bool HideCtlPanel
        {
            get {
                return fHideCtlPanel;
            }
            set {
                if (fHideCtlPanel != value) {
                    fHideCtlPanel = value;
                    UpdateView();
                }
            }
        }

        public bool HideInfoPanel
        {
            get {
                return fHideInfoPanel;
            }
            set {
                if (fHideInfoPanel != value) {
                    fHideInfoPanel = value;
                    UpdateView();
                }
            }
        }

        public bool HideLocMap
        {
            get {
                return fHideLocMap;
            }
            set {
                if (fHideLocMap != value) {
                    fHideLocMap = value;
                    UpdateView();
                }
            }
        }

        public bool InventoryOnlyIcons
        {
            get {
                return fInventoryOnlyIcons;
            }
            set {
                fInventoryOnlyIcons = value;
            }
        }

        public GhostsList GhostsList
        {
            get {
                return fGhostsList;
            }
        }

        private ExtRect LocMapRect
        {
            get {
                int pX = fGameSpace.Player.PosX;
                int pY = fGameSpace.Player.PosY;
                return ExtRect.Create(pX - fLocMapSize.X, pY - fLocMapSize.Y, pX + fLocMapSize.X, pY + fLocMapSize.Y);
            }
        }

        private ExtPoint GetIsoOffset(int iIndex, int x0, int y0)
        {
            BaseImage img = Resources.GetImage(iIndex);
            if (img == null) {
                return new ExtPoint(x0, y0 - 96);
            } else {
                return new ExtPoint(x0 + (64 - (int)img.Width) / 2, y0 + (32 - (int)img.Height));
            }
        }

        private void MainButtonClick(object sender)
        {
            MainControl mc = (MainControl)(((NWButton)sender).Tag);
            var mcRec = StaticData.dbMainControls[(int)mc];
            if (mc != MainControl.mbNone) {
                DoEvent(mcRec.Event, null, null, null);
            }
        }

        private void ViewerTilePaint(object sender, int X, int Y, BaseTile tile, ExtRect R, BaseScreen screen)
        {
            try {
                Player player = fGameSpace.Player;
                NWField fld;
                if (player.Hallucinations) {
                    fld = player.HalMap;
                } else {
                    fld = (NWField)player.CurrentMap;
                }

                NWTile nwTile = (NWTile)fld.GetTile(X, Y);
                ExtPoint scrPt = new ExtPoint();

                if ((tile.HasState(BaseTile.TS_VISITED))) {
                    int bg = (int)tile.Background;
                    int fg = Painter.VtpChange(fld, (int)tile.Foreground);
                    int bgExt = (int)tile.BackgroundExt;
                    int fgExt = (int)tile.ForegroundExt;

                    short op = 255;
                    int xx = R.Left;
                    int yy = R.Top - 96;

                    Resources.DrawImage(screen, xx, yy, Painter.GetTileImageIndex((ushort)bg), op);
                    if (bgExt != PlaceID.pid_Undefined) {
                        Resources.DrawImage(screen, xx, yy, Painter.GetTileImageIndex((ushort)bgExt), op);
                    }
                    if (fg != PlaceID.pid_Undefined) {
                        if (Painter.VtpForwardPlayer(player, X, Y)) {
                            op = 100; //191;
                        }

                        bool trap = fld.IsTrap(X, Y);
                        if (!trap || (trap && (nwTile.Trap_Discovered || GlobalVars.Debug_Divinity))) {
                            int idx = Painter.GetTileImageIndex((ushort)fg);
                            if (fg == PlaceID.pid_CaveWall) {
                                if (player.LayerID == GlobalVars.Layer_Svartalfheim2) {
                                    idx++;
                                }
                                if (player.LayerID == GlobalVars.Layer_Svartalfheim3 || player.LayerID == GlobalVars.Layer_MimerWell) {
                                    idx += 2;
                                }
                            }

                            scrPt = GetIsoOffset(idx, R.Left, R.Top);
                            Resources.DrawImage(screen, scrPt.X, scrPt.Y, idx, op);

                            if (fgExt != PlaceID.pid_Undefined) {
                                idx = Painter.GetTileImageIndex((ushort)fgExt);

                                scrPt = GetIsoOffset(idx, R.Left, R.Top);
                                Resources.DrawImage(screen, scrPt.X, scrPt.Y, idx, op);
                            }
                        }
                    }

                    if ((tile.HasState(BaseTile.TS_SEEN)) || player.Effects.FindEffectByID(EffectID.eid_DetectItems) != null || GlobalVars.Debug_Divinity) {
                        op = 255;
                        xx = R.Left + 16;
                        yy = R.Top - 15;

                        int num = fld.Items.Count;
                        for (int i = 0; i < num; i++) {
                            Item it = fld.Items.GetItem(i);
                            if (it.PosX == X && it.PosY == Y) {
                                int idx = it.ImageIndex;
                                BaseImage img = Resources.GetImage(idx);
                                if (img.Height == 30) {
                                    yy = R.Top + 1;
                                } else {
                                    if (img.Height == 32) {
                                        yy = R.Top;
                                    }
                                }
                                Resources.DrawImage(screen, xx, yy, idx, op);
                            }
                        }
                    }

                    NWCreature creature = (NWCreature)fld.FindCreature(X, Y);
                    if (creature != null && fGameSpace.IsCreatureVisible(creature, tile)) {
                        scrPt = GetIsoOffset(creature.Entry.ImageIndex, R.Left, R.Top);
                        if ((!tile.HasState(BaseTile.TS_SEEN)) || creature.Effects.FindEffectByID(EffectID.eid_Invisibility) != null) {
                            op = 127;
                        } else {
                            op = 255;
                        }
                        Resources.DrawImage(screen, scrPt.X, scrPt.Y, creature.Entry.ImageIndex, op);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.ViewerTilePaint(): " + ex.Message);
            }
        }

        private void AppDone()
        {
            for (int pid = PlaceID.pid_First; pid <= PlaceID.pid_Last; pid++) {
                StaticData.dbPlaces[pid].ImageIndex = -1;
            }

            for (var sk = SkillID.Sk_First; sk <= SkillID.Sk_Last; sk++) {
                SkillRec skid = StaticData.dbSkills[(int)sk];
                skid.ImageIndex = -1;
            }

            for (var sym = SymbolID.sid_First; sym <= SymbolID.sid_Last; sym++) {
                SymbolRec sid = StaticData.dbSymbols[(int)sym];
                sid.ImageIndex = -1;
            }

            for (var ie = ItfElement.id_First; ie <= ItfElement.id_Last; ie++) {
                var itf = StaticData.dbItfElements[(int)ie];
                itf.ImageIndex = -1;
            }

            for (int i = 0; i < GlobalVars.dbItems.Count; i++) {
                ItemEntry eItem = (ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.dbItems[i]);
                eItem.ImageIndex = -1;
            }

            for (int i = 0; i < GlobalVars.dbCreatures.Count; i++) {
                CreatureEntry eCreature = (CreatureEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.dbCreatures[i]);
                eCreature.ImageIndex = -1;
            }

            for (int i = 0; i < GlobalVars.dbLayers.Count; i++) {
                LayerEntry eLayer = (LayerEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.dbLayers[i]);
                eLayer.IconsIndex = -1;
            }

            Resources.Dispose();
            Symbols.Dispose();

            fScoresList.Save(NWResourceManager.GetAppPath() + StaticData.SCORES_FILE);
            fScoresList.Dispose();

            fGhostsList.Save(NWResourceManager.GetAppPath() + StaticData.GHOSTS_FILE);
            fGhostsList.Dispose();
        }

        private void AppInit()
        {
            try {
                fScoresList = new ScoresList();
                fScoresList.Load(NWResourceManager.GetAppPath() + StaticData.SCORES_FILE);

                fGhostsList = new GhostsList();
                fGhostsList.Load(NWResourceManager.GetAppPath() + StaticData.GHOSTS_FILE);

                Resources = new ImageList(base.Screen);
                Symbols = new ImageList(base.Screen);

                int cnt = (int)PlaceID.pid_Last + ((int)ItemKind.ik_Last + 1) + GlobalVars.dbItems.Count +
                          GlobalVars.dbCreatures.Count + (int)EffectID.eid_Last + (int)SkillID.Sk_Last + ((int)ItfElement.id_Last + 1) +
                          ((int)SymbolID.sid_Last + 1) + GlobalVars.dbLayers.Count + StaticData.dbRunes.Length;

                ProgressInit(cnt);
                ProgressLabel(BaseLocale.GetStr(RS.rs_ResLoading));

                int col;
                string dir;

                if (Style == RGS_MODERN) {
                    col = BaseScreen.clBlack;
                    dir = "tileset_ms/";
                } else {
                    col = BaseScreen.clWhite;
                    dir = "tileset/";
                }

                for (int pd = PlaceID.pid_First; pd <= PlaceID.pid_Last; pd++) {
                    PlaceRec pRec = StaticData.dbPlaces[pd];
                    string s = dir + pRec.ImageName;

                    if ((pRec.Signs.Contains(PlaceFlags.psMask))) {
                        pRec.ImageIndex = Resources.ImagesCount;
                    } else {
                        pRec.ImageIndex = Resources.AddImage(s + ".tga", col, false);
                    }

                    pRec.SubsLoaded = 0;

                    for (int i = 1; i <= pRec.SubTiles; i++) {
                        int idx = Resources.AddImage(s + "_" + Convert.ToString(i) + ".tga", col, false);

                        if (idx > -1) {
                            pRec.SubsLoaded = pRec.SubsLoaded + 1;
                        }
                    }

                    ProgressStep();
                }

                dir = "items/";
                col = BaseScreen.clWhite;
                for (var ik = ItemKind.ik_First; ik <= ItemKind.ik_Last; ik++) {
                    var ikRec = StaticData.dbItemKinds[(int)ik];

                    if (ikRec.RndImage) {
                        string s = dir + ikRec.ImageSign + "_";
                        ikRec.ImagesLoaded = 0;
                        ikRec.ImageIndex = Resources.ImagesCount;

                        int num2 = (int)ikRec.Images;
                        for (int i = 1; i <= num2; i++) {
                            string fn = s + Convert.ToString(i) + ".tga";
                            int idx = Resources.AddImage(fn, col, true);

                            if (idx > -1) {
                                ikRec.ImagesLoaded = ikRec.ImagesLoaded + 1;
                            }
                        }
                    }

                    ProgressStep();
                }

                for (int i = 0; i < GlobalVars.dbItems.Count; i++) {
                    ItemEntry eItem = (ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.dbItems[i]);
                    var ikRec = StaticData.dbItemKinds[(int)eItem.ItmKind];

                    if (!eItem.Meta) {
                        string s = eItem.ImageName;
                        string fn = dir + s + ".tga";

                        int idx = Resources.AddImage(fn, col, true);
                        if (idx < 0) {
                            fn = dir + "Unknown.tga";
                            idx = Resources.AddImage(fn, col, true);
                        }

                        eItem.ImageIndex = idx;
                        eItem.FramesLoaded = 0;

                        for (int k = 1; k <= (int)eItem.FramesCount; k++) {
                            fn = dir + s + "_" + Convert.ToString(k) + ".tga";

                            idx = Resources.AddImage(fn, col, true);
                            if (idx < 0) {
                                fn = dir + "Unknown.tga";
                                idx = Resources.AddImage(fn, col, true);
                            }

                            if (idx > -1) {
                                eItem.FramesLoaded += 1;
                            }
                        }

                        if (Style == RGS_MODERN && ikRec.RndImage && ikRec.ImagesLoaded > 0) {
                            int k = RandomHelper.GetRandom(ikRec.ImagesLoaded + 1);
                            if (k > 0) {
                                eItem.ImageIndex = ikRec.ImageIndex + (k - 1);
                            }
                        }
                    }

                    ProgressStep();
                }

                dir = "creatures/";
                col = BaseScreen.clWhite;
                for (int j = 0; j < GlobalVars.dbCreatures.Count; j++) {
                    CreatureEntry eCreature = (CreatureEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.dbCreatures[j]);

                    string fn = dir + eCreature.Gfx + ".tga";
                    string fn_gray = dir + "gray/" + eCreature.Gfx + "_gray.tga";

                    int idx = Resources.AddImage(fn, col, true);
                    eCreature.ImageIndex = idx;
                    if (idx >= 0) {
                        eCreature.GrayImageIndex = Resources.AddImage(fn_gray, col, true);
                    }
                    eCreature.FramesLoaded = 0;

                    for (int i = 1; i <= (int)eCreature.FramesCount; i++) {
                        fn = dir + eCreature.Gfx + "_" + ConvertHelper.AdjustNumber(i, 2) + ".tga";

                        idx = Resources.AddImage(fn, col, true);
                        if (idx < 0) {
                            fn = dir + "Unknown.tga";
                            idx = Resources.AddImage(fn, col, true);
                        }

                        if (idx > -1) {
                            eCreature.FramesLoaded += 1;
                        }
                    }

                    ProgressStep();
                }

                dir = "effects/";
                col = BaseScreen.clWhite;
                for (var eff = EffectID.eid_First; eff <= EffectID.eid_Last; eff++) {
                    int idx;

                    if (EffectsData.dbEffects[(int)eff].GFX.CompareTo("") == 0 || EffectsData.dbEffects[(int)eff].FrameCount == 0) {
                        idx = -1;
                    } else {
                        idx = Resources.ImagesCount;
                        string s = dir + EffectsData.dbEffects[(int)eff].GFX + "_";

                        int num7 = (int)EffectsData.dbEffects[(int)eff].FrameCount;
                        for (int i = 1; i <= num7; i++) {
                            Resources.AddImage(s + Convert.ToString(i) + ".tga", col, true);
                        }
                    }
                    EffectsData.dbEffects[(int)eff].ImageIndex = idx;

                    ProgressStep();
                }

                dir = "itf/";
                for (var sk = SkillID.Sk_First; sk <= SkillID.Sk_Last; sk++) {
                    SkillRec skRec = StaticData.dbSkills[(int)sk];

                    if (skRec.Kinds != SkillKind.ssInnatePower && skRec.gfx != "") {
                        string s = dir + skRec.gfx + ".tga";
                        skRec.ImageIndex = Resources.AddImage(s, BaseScreen.clWhite, false);
                    }

                    ProgressStep();
                }

                for (var ie = ItfElement.id_First; ie <= ItfElement.id_Last; ie++) {
                    var ieRec = StaticData.dbItfElements[(int)ie];
                    string s = dir + ieRec.FileName;
                    ieRec.ImageIndex = Resources.AddImage(s, ieRec.TransColor, false);
                    ProgressStep();
                }

                dir = "symbols/";
                for (var sym = SymbolID.sid_First; sym <= SymbolID.sid_Last; sym++) {
                    SymbolRec sid = StaticData.dbSymbols[(int)sym];

                    string s = sid.gfx;
                    sid.ImageIndex = Symbols.AddImage(dir + s + ".tga", BaseScreen.clWhite, false);

                    int num8 = sid.SubCount;
                    for (int j = 1; j <= num8; j++) {
                        Symbols.AddImage((dir + s + "_" + ConvertHelper.AdjustNumber(j, 3) + ".tga"), BaseScreen.clWhite, false);
                    }

                    ProgressStep();
                }

                dir = "map/";
                for (int j = 0; j < GlobalVars.dbLayers.Count; j++) {
                    LayerEntry eLayer = (LayerEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.dbLayers[j]);
                    eLayer.IconsIndex = Resources.ImagesCount;

                    for (int y = 0; y < eLayer.H; y++) {
                        for (int x = 0; x < eLayer.W; x++) {
                            string s = dir + eLayer.IconsName + Convert.ToString(x) + Convert.ToString(y) + ".tga";
                            Resources.AddImage(s, BaseScreen.clFuchsia, false);
                        }
                    }

                    ProgressStep();
                }

                dir = "runes/";
                foreach (RuneRec rune in StaticData.dbRunes) {
                    string s = dir + rune.Sign + ".tga";
                    rune.ImageIndex = Resources.AddImage(s, BaseScreen.clWhite, false);
                    ProgressStep();
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.AppInit(): " + ex.Message);
                Quit();
            }

            ProgressDone();
        }

        private void DrawCreatureInt(BaseScreen screen, NWField field, NWCreature creature, int mode, int tX, int tY, int offset)
        {
            NWTile place = (NWTile)field.GetTile(tX, tY);
            if (place != null && fGameSpace.IsCreatureVisible(creature, place)) {
                bool transparent = (creature.Effects.FindEffectByID(EffectID.eid_Invisibility) != null) || (creature.Ghost) || (creature.Illusion);

                switch (mode) {
                    case OPM_FIELD:
                        {
                            int xx = fMV_Rect.Left + 8 * (tX + 1);
                            int yy = fMV_Rect.Top + 10 * (tY + 1);
                            short op = 255;

                            if (creature.Entry.Race == RaceID.crHuman) {
                                SymbolID sid;

                                if (creature.IsPlayer || creature.Mercenary) {
                                    sid = SymbolID.sid_Player;
                                } else {
                                    if (creature.Stoning) {
                                        sid = SymbolID.sid_StoningHuman;
                                    } else {
                                        if (fGameSpace.Player.IsEnemy(creature)) {
                                            sid = SymbolID.sid_EnemyHuman;
                                        } else {
                                            sid = SymbolID.sid_AllyHuman;
                                        }
                                    }
                                }
                                var symRec = StaticData.dbSymbols[(int)sid];
                                Symbols.DrawImage(screen, xx, yy, symRec.ImageIndex, op);
                            } else {
                                SymbolID sid = creature.Symbol;
                                if (sid == SymbolID.sid_None) {
                                    if (creature.Stoning) {
                                        screen.SetTextColor(BaseScreen.clGray, true);
                                    } else {
                                        if (fGameSpace.Player.IsEnemy(creature)) {
                                            screen.SetTextColor(BaseScreen.clRed, true);
                                        } else {
                                            screen.SetTextColor(BaseScreen.clLime, true);
                                        }
                                    }

                                    string sym = Convert.ToString(creature.Entry.Symbol);
                                    screen.DrawText(xx + 1, yy, sym, 0);
                                } else {
                                    var symRec = StaticData.dbSymbols[(int)sid];
                                    Symbols.DrawImage(screen, xx, yy, symRec.ImageIndex + offset, op);
                                }
                            }
                        }
                        break;

                    case OPM_LOCAL:
                        {
                            ExtRect rt = LocMapRect;
                            int xx = fLV_Rect.Left + 32 * (tX - rt.Left);
                            int yy = fLV_Rect.Top + 30 * (tY - rt.Top);
                            if (rt.Contains(tX, tY)) {
                                short op;
                                if (transparent) {
                                    op = 127;
                                } else {
                                    op = fGameSpace.GetTileBrightness(field, place, false);
                                }

                                if (offset < 0) {
                                    offset = 0;
                                } else {
                                    if (offset > 0) {
                                        offset++;
                                    }
                                }

                                int idx;
                                if (creature.Stoning) {
                                    idx = creature.Entry.GrayImageIndex;
                                } else {
                                    idx = creature.Entry.ImageIndex + offset;
                                }

                                if (creature.Effects.FindEffectByID(EffectID.eid_Sail) != null) {
                                    idx = ((ItemEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.iid_Skidbladnir)).ImageIndex;
                                }

                                Resources.DrawImage(screen, xx, yy, idx, op);

                                if (creature.Effects.FindEffectByID(EffectID.eid_CaughtInNet) != null) {
                                    idx = ((ItemEntry)GlobalVars.nwrDB.FindEntryBySign("Net")).ImageIndex;
                                    Resources.DrawImage(screen, xx, yy, idx, 127);
                                }
                            }
                        }
                        break;
                }
            }
        }

        private void DrawCreatures(BaseScreen screen, NWField field, int mode)
        {
            int num1 = field.Creatures.Count;
            for (int i = 0; i < num1; i++) {
                NWCreature creature = field.Creatures.GetItem(i);
                int px = creature.PosX;
                int py = creature.PosY;

                if (creature is ArticulateCreature) {
                    ArticulateCreature art = (ArticulateCreature)creature;

                    int num2 = art.Size;
                    for (int k = 0; k < num2; k++) {
                        ArticulateSegment seg = art.GetSegment(k);
                        DrawCreatureInt(screen, field, creature, mode, seg.X, seg.Y, seg.ImageIndex);
                    }
                } else {
                    DrawCreatureInt(screen, field, creature, mode, px, py, 0);
                }
            }
        }

        private void DrawItems(BaseScreen screen, NWField field, int mode)
        {
            Player player = fGameSpace.Player;
            bool detectItems = (player.Effects.FindEffectByID(EffectID.eid_DetectItems) != null);

            int num = field.Items.Count;
            for (int i = 0; i < num; i++) {
                Item item = field.Items.GetItem(i);
                int px = item.PosX;
                int py = item.PosY;

                NWTile place = (NWTile)field.GetTile(px, py);
                bool seen = ((place.HasState(BaseTile.TS_SEEN)) || GlobalVars.Debug_Divinity || detectItems);

                if (seen) {
                    switch (mode) {
                        case OPM_FIELD:
                            {
                                SymbolID sid = item.Symbol;
                                int xx = fMV_Rect.Left + 8 * (px + 1);
                                int yy = fMV_Rect.Top + 10 * (py + 1);
                                short op = fGameSpace.GetTileBrightness(field, place, true);
                                var symRec = StaticData.dbSymbols[(int)sid];
                                Symbols.DrawImage(screen, xx, yy, symRec.ImageIndex, op);
                            }
                            break;

                        case OPM_LOCAL:
                            ExtRect rt = LocMapRect;
                            if (item.InRect(rt)) {
                                int xx = fLV_Rect.Left + 32 * (px - rt.Left);
                                int yy = fLV_Rect.Top + 30 * (py - rt.Top);
                                short op = fGameSpace.GetTileBrightness(field, place, false);
                                int idx = item.ImageIndex;
                                Resources.DrawImage(screen, xx, yy, idx, op);
                            }
                            break;
                    }
                }
            }
        }

        private void DrawFeatures(BaseScreen screen, NWField field, ExtRect rt)
        {
            Player player = fGameSpace.Player;

            EntityList features = field.Features;
            int num = features.Count;
            for (int i = 0; i < num; i++) {
                GameEntity he = features.GetItem(i);

                if (he is MapObject) {
                    MapObject mf = (MapObject)he;
                    int px = mf.PosX;
                    int py = mf.PosY;
                    NWTile place = (NWTile)field.GetTile(px, py);

                    short op = fGameSpace.GetTileBrightness(field, place, true);

                    if (player.IsSeen(px, py, false)) {
                        int xx = fLV_Rect.Left + 32 * (px - rt.Left);
                        int yy = fLV_Rect.Top + 30 * (py - rt.Top);
                        Resources.DrawImage(screen, xx, yy, mf.ImageIndex, op);
                    }
                }
            }
        }

        private void UpdateFeatures(long time)
        {
            Player player = fGameSpace.Player;
            NWField field = player.CurrentField;

            EntityList features = field.Features;
            for (int i = features.Count - 1; i >= 0; i--) {
                GameEntity he = features.GetItem(i);

                if (he is MapObject) {
                    MapObject mf = (MapObject)he;

                    if (mf.IsNeedUpdate) {
                        mf.Update(time);

                        if (mf.IsFinished) {
                            features.Delete(i);
                        }
                    }
                }
            }
        }

        public override void Update(long time)
        {
            if (fGameSpace != null) {
                switch (fMainScreen) {
                    case GameScreen.gsMain:
                        if (fGameState == GameState.gsDefault) {
                            UpdateFeatures(time);
                        }
                        break;

                    case GameScreen.gsSwirl:
                        break;
                }
            }

            base.Update(time);
        }

        private void DoUserAction(Keys key, ShiftStates shift)
        {
            try {
                for (var ua = UserAction.uaFirst; ua <= UserAction.uaLast; ua++) {
                    UserActionRec uAct = StaticData.dbUserActions[(int)ua];

                    ShiftStates hkShift;
                    if (uAct.WithoutShift) {
                        hkShift = new ShiftStates();
                    } else {
                        hkShift = shift;
                    }

                    HotKey hkAct = uAct.HotKey;

                    if (hkAct.Key == key && hkAct.Shift.Equals(hkShift)) {
                        DoEvent(uAct.Event, null, null, shift);
                        return;
                    }
                }

                if (GlobalVars.Debug_DevMode) {
                    ShowTextAux("Key: " + Convert.ToString(key));
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.doUserAction(): " + ex.Message);
                throw ex;
            }
        }

        private void DoPlayerMove(int moveDir, ShiftStates shift, bool trans)
        {
            if (Style == RGS_MODERN & trans) {
                moveDir = Directions.IsoTrans[moveDir];
            }

            if ((shift.Contains(ShiftStates.SsCtrl))) {
                fGameSpace.DoPlayerAction(CreatureAction.caAttackMelee, moveDir);
            } else {
                if ((shift.Contains(ShiftStates.SsAlt))) {
                    fGameSpace.DoPlayerAction(CreatureAction.caAttackShoot, moveDir);
                } else {
                    fGameSpace.DoPlayerAction(CreatureAction.caMove, moveDir);
                }
            }
        }

        private void OnLinkClick(object sender, string linkValue)
        {
            ShowKnowledge(linkValue);
        }

        private void OptionsDone()
        {
            try {
                using (var ini = new IniFile(NWResourceManager.GetAppPath() + StaticData.OPTIONS_FILE)) {
                    ini.WriteInteger("Common", "SongsVolume", (fSongsVolume));
                    ini.WriteInteger("Common", "SoundsVolume", (fSoundsVolume));
                    ini.WriteBool("Common", "HideLocMap", (HideLocMap));
                    ini.WriteBool("Common", "HideCtlPanel", (HideCtlPanel));
                    ini.WriteBool("Common", "HideInfoPanel", (HideInfoPanel));
                    ini.WriteInteger("Common", "Style", (base.Style));
                    ini.WriteBool("Common", "InventoryOnlyIcons", (fInventoryOnlyIcons));
                    ini.WriteBool("Common", "CircularFOV", (fCircularFOV));
                    ini.WriteBool("Common", "AutoPickup", (fAutoPickup));
                    ini.WriteBool("Common", "ExtremeMode", (fExtremeMode));
                    ini.WriteString("Common", "Language", fLanguage);

                    for (var ua = UserAction.uaFirst; ua <= UserAction.uaLast; ua++) {
                        UserActionRec uAct = StaticData.dbUserActions[(int)ua];
                        ini.WriteString("Keys", "" + uAct.Sign, HotKeyToText(uAct.HotKey));
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.OptionsDone(): " + ex.Message);
            }
        }

        private void OptionsInit()
        {
            try {
                using (var ini = new IniFile(NWResourceManager.GetAppPath() + StaticData.OPTIONS_FILE)) {
                    SongsVolume = (ini.ReadInteger("Common", "SongsVolume", 255));
                    SoundsVolume = (ini.ReadInteger("Common", "SoundsVolume", 255));
                    HideLocMap = (ini.ReadBool("Common", "HideLocMap", false));
                    HideCtlPanel = (ini.ReadBool("Common", "HideCtlPanel", false));
                    HideInfoPanel = (ini.ReadBool("Common", "HideInfoPanel", false));
                    Style = (ini.ReadInteger("Common", "Style", 0));
                    InventoryOnlyIcons = (ini.ReadBool("Common", "InventoryOnlyIcons", false));
                    fCircularFOV = (ini.ReadBool("Common", "CircularFOV", true));
                    fAutoPickup = (ini.ReadBool("Common", "AutoPickup", false));
                    fExtremeMode = (ini.ReadBool("Common", "ExtremeMode", false));
                    Language = ini.ReadString("Common", "Language", "English");

                    for (var ua = UserAction.uaFirst; ua <= UserAction.uaLast; ua++) {
                        UserActionRec uAct = StaticData.dbUserActions[(int)ua];

                        string hk = ini.ReadString("Keys", "" + uAct.Sign, "?");
                        if (hk != "?") {
                            uAct.HotKey = TextToHotKey(hk);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.OptionsInit(): " + ex.Message);
            }
        }

        private void UpdateView()
        {
            fTextBox.Bounds = StaticData.TV_Rect;
            if (fHideCtlPanel) {
                if (fHideLocMap) {
                    IP_Top = 14;
                } else {
                    IP_Top = -2;
                }
            } else {
                IP_Top = 150;
            }
            IP_Left = 640;
            IP_Right = 792;

            int LV_Y;
            int LV_YR;
            int MV_X;
            int MV_Y;
            int LV_X;
            int LV_XR;
            if (fHideLocMap) {
                LV_Y = 22;
                LV_YR = 7;
                MV_X = 8;
                MV_Y = 8;
                if (fHideCtlPanel && fHideInfoPanel) {
                    LV_X = 32;
                    LV_XR = 11;
                } else {
                    LV_X = 16;
                    LV_XR = 9;
                }
            } else {
                LV_Y = 216;
                LV_YR = 4;
                MV_Y = 8;
                if (!fHideCtlPanel && !fHideInfoPanel) {
                    LV_X = 16;
                    LV_XR = 9;
                } else {
                    LV_X = 32;
                    LV_XR = 11;
                }
                if (fHideCtlPanel && fHideInfoPanel) {
                    MV_X = 88;
                } else {
                    MV_X = 8;
                }
            }
            int LV_W = (LV_XR << 1) + 1 << 5;
            int LV_H = ((LV_YR << 1) + 1) * 30;
            fLV_Rect = ExtRect.Create(LV_X, LV_Y, LV_X + LV_W, LV_Y + LV_H);
            fMV_Rect = ExtRect.Create(MV_X, MV_Y, MV_X + 624, MV_Y + 200);
            fLocMapSize = new ExtPoint(LV_XR, LV_YR);
            if (Style == RGS_MODERN) {
                fViewer.BufferResize(fLV_Rect);
            }

            for (var mc = (int)MainControl.mbMenu; mc <= (int)MainControl.mbPack; mc++) {
                MainControlRec mCtl = StaticData.dbMainControls[mc];
                mCtl.Button.Visible = (fMainScreen == GameScreen.gsMain && !fHideCtlPanel);
            }
        }

        private void DrawGameView(BaseScreen screen, NWField field, Player player)
        {
            if (DialogVisibleRefs > 0) {
                return;
            }

            try {
                NWField currentField;
                if (player.Hallucinations) {
                    currentField = player.HalMap;
                } else {
                    currentField = field;
                }

                ExtRect tempRect;
                if (!fHideLocMap) {
                    ExtRect mapRect = ExtRect.Create(-1, -1, StaticData.FieldWidth, StaticData.FieldHeight);
                    screen.FillRect(fMV_Rect, BaseScreen.clBlack);

                    tempRect = fMV_Rect;
                    tempRect.Inflate(2, 2);
                    CtlCommon.DrawCtlBorder(screen, tempRect);

                    for (int py = mapRect.Top; py <= mapRect.Bottom; py++) {
                        for (int px = mapRect.Left; px <= mapRect.Right; px++) {
                            Painter.DrawSymTile(fGameSpace, currentField, px, py, screen, mapRect, fMV_Rect, Symbols);
                        }
                    }

                    DrawItems(screen, field, OPM_FIELD);

                    screen.Font = CtlCommon.SymFont;
                    DrawCreatures(screen, field, OPM_FIELD);

                    screen.Font = CtlCommon.SmFont;
                    if (GlobalVars.Debug_CheckCrPtr) {
                        for (int py = mapRect.Top; py <= mapRect.Bottom; py++) {
                            for (int px = mapRect.Left; px <= mapRect.Right; px++) {
                                if (field.FindCreature(px, py) != null) {
                                    int xx = fMV_Rect.Left + 8 * (px + 1);
                                    int yy = fMV_Rect.Top + 10 * (py + 1);
                                    screen.DrawRectangle(ExtRect.Create(xx, yy, xx + 4, yy + 4), BaseScreen.clYellow, BaseScreen.clRed);
                                }
                            }
                        }
                    }

                    if (GlobalVars.Debug_CheckScent) {
                        for (int py = mapRect.Top; py <= mapRect.Bottom; py++) {
                            for (int px = mapRect.Left; px <= mapRect.Right; px++) {
                                NWTile place = (NWTile)field.GetTile(px, py);
                                if (place != null && place.ScentTrail != null) {
                                    int xx = fMV_Rect.Left + 8 * (px + 1);
                                    int yy = fMV_Rect.Top + 10 * (py + 1);
                                    screen.DrawRectangle(ExtRect.Create(xx, yy, xx + 4, yy + 4), BaseScreen.clYellow, BaseScreen.RGB((140 + (int)place.ScentAge), 0, 0));
                                }
                            }
                        }
                    }

                    if (fTargetData != null && fCursorValid && mapRect.Contains(fMouseMapX, fMouseMapY)) {
                        int xx = fMV_Rect.Left + 8 * (fMouseMapX + 1);
                        int yy = fMV_Rect.Top + 10 * (fMouseMapY + 1);
                        Symbols.DrawImage(screen, xx, yy, StaticData.dbSymbols[(int)SymbolID.sid_Cursor].ImageIndex, 255);
                    }
                }

                tempRect = fLV_Rect.Clone();
                tempRect.Inflate(2, 2);
                CtlCommon.DrawCtlBorder(screen, tempRect);

                if (Style == RGS_MODERN) {
                    fViewer.Map = currentField;
                    fViewer.CenterByTile(player.PosX, player.PosY);
                    fViewer.BufferPaint(screen, fLV_Rect.Left, fLV_Rect.Top);
                } else {
                    ExtRect mapRect = LocMapRect;
                    screen.FillRect(fLV_Rect, BaseScreen.clBlack);

                    for (int py = mapRect.Top; py <= mapRect.Bottom; py++) {
                        for (int px = mapRect.Left; px <= mapRect.Right; px++) {
                            Painter.DrawLocTile(fGameSpace, currentField, px, py, screen, player, mapRect, fLV_Rect, Resources);
                        }
                    }

                    DrawItems(screen, field, OPM_LOCAL);

                    DrawCreatures(screen, field, OPM_LOCAL);

                    DrawFeatures(screen, field, mapRect);

                    if (fTargetData != null && fCursorValid && mapRect.Contains(fMouseMapX, fMouseMapY)) {
                        ItfElement itf = Painter.PtGetCursor(fTargetData.Target, player, fMouseMapX, fMouseMapY);
                        int xx = fLV_Rect.Left + 32 * (fMouseMapX - mapRect.Left);
                        int yy = fLV_Rect.Top + 30 * (fMouseMapY - mapRect.Top);
                        Resources.DrawImage(screen, xx, yy, StaticData.dbItfElements[(int)itf].ImageIndex, 255);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.drawGameView(): " + ex.Message);
            }
        }

        private bool ScreenInternal
        {
            set {
                switch (fMainScreen) {
                    case GameScreen.gsVillage:
                        {
                            if (value) {
                                ShowText(this, BaseLocale.GetStr(RS.rs_VillageMsg), new LogFeatures(LogFeatures.lfDialog));
                            }
                            break;
                        }
                    case GameScreen.gsForest:
                        {
                            if (value) {
                                ShowText(this, BaseLocale.GetStr(RS.rs_ForestMsg), new LogFeatures(LogFeatures.lfDialog));
                            }
                            break;
                        }
                    case GameScreen.gsMain:
                        {
                            fTextBox.Visible = value;
                            break;
                        }
                    case GameScreen.gsDefeat:
                        {
                            if (value) {
                                ShowText(this, BaseLocale.GetStr(RS.rs_DefeatMsg), new LogFeatures(LogFeatures.lfDialog));
                            }
                            break;
                        }
                    case GameScreen.gsVictory:
                        {
                            if (value) {
                                ShowText(this, BaseLocale.GetStr(RS.rs_VictoryMsg), new LogFeatures(LogFeatures.lfDialog));
                            }
                            break;
                        }
                }
            }
        }

        protected override void AppCreate()
        {
            try {
                fGameState = GameState.gsAppBusy;

                Logger.LogInit(NWResourceManager.GetAppPath() + "Ragnarok.log");

                fLocale = new Locale();

                NWResourceManager.Load();

                fGameSpace = new NWGameSpace(this);

                Cursor = NWResourceManager.LoadImage(base.Screen, "itf/CursorMain.tga", BaseScreen.clBlack);
                SysCursorVisible = false;

                CtlCommon.InitCtlImages(base.Screen);
                CtlCommon.SmFont = base.Screen.CreateFont("itf/rsf.gef", StaticData.DefEncoding);
                CtlCommon.BgFont = base.Screen.CreateFont("itf/rbf.gef", StaticData.DefEncoding);
                CtlCommon.SymFont = base.Screen.CreateFont("itf/rsymfont.gef", StaticData.DefEncoding);

                fSoundEngine.SfxInit(0);

                fScrImage = null;
                Font = CtlCommon.SmFont;

                fViewer = new IsoViewer(null);
                fViewer.TileWidth = 64;
                fViewer.TileHeight = 32;
                fViewer.OnTilePaint = ViewerTilePaint;

                fMainScreen = GameScreen.gsNone;

                fTextBox = new TextBox(this);
                fTextBox.Visible = false;
                fTextBox.LastVisible = true;
                fTextBox.Links = true;
                fTextBox.OnLinkClick = OnLinkClick;
                fTextBox.LineHeight = 18;

                fStartupDialog = new StartupWindow(this);
                fMenuDialog = new MenuWindow(this);
                fNPCWindow = new NPCWindow(this);
                fMessageWindow = new MessageWindow(this);
                fConsoleWindow = new ConsoleWindow(this);
                fOptions = new OptionsWindow(this);
                fMapDialog = new MapWindow(this);
                fFilesWindow = new FilesWindow(this);
                fKnowledgesWindow = new KnowledgesWindow(this);
                fSkillsWindow = new SkillsWindow(this);
                fSelfWindow = new SelfWindow(this);
                fInventoryWindow = new InventoryWindow(this);
                fHeroWindow = new HeroWindow(this);
                fIntroDialog = new IntroWindow(this);
                fAboutDialog = new AboutWindow(this);
                fInputWindow = new InputWindow(this);
                fProgressWindow = new ProgressWindow(this);
                fAlchemyWindow = new AlchemyWindow(this);
                fExchangeWindow = new ExchangeWindow(this);
                fTeachWindow = new TeachWindow(this);
                fRecruitWindow = new RecruitWindow(this);
                fSmithyWindow = new SmithyWindow(this);
                fPartyWindow = new PartyWindow(this);
                fJournalWindow = new JournalWindow(this);
                fDivinationWindow = new DivinationWindow(this);

                for (var mb = MainControl.mbFirst; mb <= MainControl.mbLast; mb++) {
                    var mcRec = StaticData.dbMainControls[(int)mb];

                    CustomButton button = new NWButton(this);
                    button.ImageFile = mcRec.ImageFile;
                    button.OnLangChange = LangChange;
                    button.LangResID = mcRec.NameRes;
                    button.Left = mcRec.R.Left;
                    button.Top = mcRec.R.Top;
                    button.Width = mcRec.R.Right - mcRec.R.Left;
                    button.Height = mcRec.R.Bottom - mcRec.R.Top;
                    button.OnClick = MainButtonClick;
                    button.Visible = false;
                    button.Tag = (int)mb;

                    mcRec.Button = button;
                }

                OptionsInit();
                DoEvent(EventID.event_Startup, null, null, null);
                AppInit();
                UpdateView();
                ShowStartupWin();
                fGameState = GameState.gsDefault;
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.AppCreate(): " + ex.Message);
                throw ex;
            }
        }

        protected override void AppDestroy()
        {
            try {
                fGameState = GameState.gsAppBusy;
                AppDone();
                fPartyWindow.Dispose();
                fSmithyWindow.Dispose();
                fExchangeWindow.Dispose();
                fTeachWindow.Dispose();
                fRecruitWindow.Dispose();
                fAlchemyWindow.Dispose();
                fProgressWindow.Dispose();
                fInputWindow.Dispose();
                fAboutDialog.Dispose();
                fIntroDialog.Dispose();
                fHeroWindow.Dispose();
                fInventoryWindow.Dispose();
                fSelfWindow.Dispose();
                fSkillsWindow.Dispose();
                fKnowledgesWindow.Dispose();
                fFilesWindow.Dispose();
                fMapDialog.Dispose();
                fOptions.Dispose();
                fConsoleWindow.Dispose();
                fNPCWindow.Dispose();
                fMessageWindow.Dispose();
                fMenuDialog.Dispose();
                fStartupDialog.Dispose();
                fTextBox.Dispose();

                fViewer = null;

                fScrImage.Dispose();
                fScrImage = null;

                fSoundEngine.SfxDone();
                CtlCommon.SymFont.Dispose();
                CtlCommon.SmFont.Dispose();
                CtlCommon.BgFont.Dispose();
                CtlCommon.DoneCtlImages();
                Cursor.Dispose();
                OptionsDone();

                fGameSpace.Dispose();
                fGameSpace = null;

                NWResourceManager.Close();

                fLocale.Dispose();
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.AppDestroy(): " + ex.Message);
            }
            //Logger.Done();
        }

        public void TestSound(Keys Key)
        {
            switch (Key) {
                case Keys.GK_F5:
                    fSoundEngine.SfxSetReverb(SoundEngine.Reverb.Cave);
                    break;

                case Keys.GK_F6:
                    fSoundEngine.SfxSetReverb(SoundEngine.Reverb.Room);
                    break;

                case Keys.GK_F7:
                    fSoundEngine.SfxSetReverb(SoundEngine.Reverb.Arena);
                    break;

                case Keys.GK_F8:
                    fSoundEngine.SfxSetReverb(SoundEngine.Reverb.CHall);
                    break;

                case Keys.GK_F9:
                    fSoundEngine.SfxSetReverb(SoundEngine.Reverb.Quarry);
                    break;

                case Keys.GK_F10:
                    fSoundEngine.SfxSetReverb(SoundEngine.Reverb.Plain);
                    break;
            }
        }

        protected override void DoKeyDownEvent(KeyEventArgs eventArgs)
        {
            try {
                base.DoKeyDownEvent(eventArgs);

                switch (fMainScreen) {
                    case GameScreen.gsStartup:
                        break;

                    case GameScreen.gsMain:
                        switch (eventArgs.Key) {
                            case Keys.GK_ESCAPE:
                                if (fTargetData != null) {
                                    fTargetData.Dispose();
                                    fTargetData = null;
                                }
                                break;

                            case Keys.GK_TWIDDLE:
                                fConsoleWindow.Show();
                                break;

                            case Keys.GK_F11:
                                /*NWField fld = fGameSpace.getPlayer().getCurrentField();
                                UniverseBuilder.build_Vanaheim(fld);
                                fld.research(true, new TileStates(TileStates.tsSeen, TileStates.tsVisited));*/
                                break;

                            default:
                                DoUserAction(eventArgs.Key, eventArgs.Shift);
                                break;
                        }
                        break;

                    case GameScreen.gsDead:
                        DoEvent(EventID.event_Startup, null, null, null);
                        ShowStartupWin();
                        break;

                    default:
                        SetScreen(GameScreen.gsMain);
                        break;
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.doKeyDownEvent(): " + ex.Message);
            }
        }

        protected override void DoMouseDownEvent(MouseEventArgs eventArgs)
        {
            try {
                base.DoMouseDownEvent(eventArgs);

                switch (fMainScreen) {
                    case GameScreen.gsVillage:
                    case GameScreen.gsForest:
                    case GameScreen.gsJotenheim:
                    case GameScreen.gsNidavell:
                    case GameScreen.gsNiflheim:
                    case GameScreen.gsCrossroad:
                    case GameScreen.gsDungeon:
                    case GameScreen.gsSea:
                    case GameScreen.gsAlfheim:
                    case GameScreen.gsMuspelheim:
                    case GameScreen.gsWell:
                    case GameScreen.gsTemple:
                    case GameScreen.gsWasteland:
                    case GameScreen.gsSwirl:
                        {
                            SetScreen(GameScreen.gsMain);
                            break;
                        }

                    case GameScreen.gsDead:
                    case GameScreen.gsDefeat:
                    case GameScreen.gsVictory:
                        {
                            DoEvent(EventID.event_Startup, null, null, null);
                            ShowStartupWin();
                            break;
                        }

                    case GameScreen.gsMain:
                        {
                            if ((fMV_Rect.Contains(eventArgs.X, eventArgs.Y) && !fHideLocMap) || fLV_Rect.Contains(eventArgs.X, eventArgs.Y)) {
                                if (eventArgs.Button != MouseButton.mbLeft) {
                                    if (eventArgs.Button == MouseButton.mbRight) {
                                        fGameSpace.ShowPlaceInfo(fMouseMapX, fMouseMapY, true);
                                    }
                                } else {
                                    ClickTarget(eventArgs.Shift);
                                }
                            }
                            break;
                        }
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.doMouseDownEvent(): " + ex.Message);
            }
        }

        protected override void DoMouseMoveEvent(MouseMoveEventArgs eventArgs)
        {
            base.DoMouseMoveEvent(eventArgs);

            fCursorValid = false;

            if (fMainScreen == GameScreen.gsMain) {
                if (fLV_Rect.Contains(eventArgs.X, eventArgs.Y)) {
                    if (Style == RGS_MODERN) {
                        ExtPoint p = fViewer.TileByMouse(eventArgs.X - fLV_Rect.Left, eventArgs.Y - fLV_Rect.Top);
                        fMouseMapX = p.X;
                        fMouseMapY = p.Y;
                        fCursorValid = true;
                    } else {
                        ExtRect R = LocMapRect;
                        fMouseMapX = R.Left + (eventArgs.X - fLV_Rect.Left) / 32;
                        fMouseMapY = R.Top + (eventArgs.Y - fLV_Rect.Top) / 30;
                        fCursorValid = true;
                    }
                } else {
                    if (fMV_Rect.Contains(eventArgs.X, eventArgs.Y) && !fHideLocMap) {
                        fMouseMapX = -1 + (eventArgs.X - fMV_Rect.Left) / 8;
                        fMouseMapY = -1 + (eventArgs.Y - fMV_Rect.Top) / 10;
                        fCursorValid = true;
                    }
                }
            }
        }

        private void DrawMainScreen(BaseScreen screen, NWField fld, Player player)
        {
            try {
                ExtRect crt = ClientRect;
                screen.DrawFilled(crt, BaseScreen.FILL_TILE, 0, 0, CtlCommon.WinBack.Width, CtlCommon.WinBack.Height, 0, 0, CtlCommon.WinBack);

                DrawGameView(screen, fld, player);

                if (!fHideInfoPanel) {
                    crt = StaticData.dbMainControls[(int)MainControl.mgHP].R;
                    crt.Top += IP_Top;
                    crt.Bottom += IP_Top;
                    crt.Left = IP_Left;
                    crt.Right = IP_Right;
                    ProgressBar.DrawGauge(screen, crt, player.HPCur, player.HPMax_Renamed, BaseScreen.clBlack, BaseScreen.clGray, 1476417);

                    crt = StaticData.dbMainControls[(int)MainControl.mgMag].R;
                    crt.Top += IP_Top;
                    crt.Bottom += IP_Top;
                    crt.Left = IP_Left;
                    crt.Right = IP_Right;
                    ProgressBar.DrawGauge(screen, crt, player.MPCur, player.MPMax, BaseScreen.clBlack, BaseScreen.clGray, 1476417);

                    crt = StaticData.dbMainControls[(int)MainControl.mgFood].R;
                    crt.Top += IP_Top;
                    crt.Bottom += IP_Top;
                    crt.Left = IP_Left;
                    crt.Right = IP_Right;
                    ProgressBar.DrawGauge(screen, crt, (int)player.Satiety, Player.SatietyMax, BaseScreen.clBlack, BaseScreen.clGray, 1476417);

                    screen.SetTextColor(BaseScreen.clGold, true);
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 8, IP_Right, player.LocationName, "");
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 24, IP_Right, fGameSpace.DayTimeInfo, "");
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 40, IP_Right, BaseLocale.GetStr(RS.rs_Turn), Convert.ToString(player.Turn));
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 56, IP_Right, BaseLocale.GetStr(RS.rs_HP), Convert.ToString(player.HPCur) + " (" + Convert.ToString(player.HPMax_Renamed) + ")");
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 82, IP_Right, BaseLocale.GetStr(RS.rs_MP), Convert.ToString(player.MPCur) + " (" + Convert.ToString(player.MPMax) + ")");
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 108, IP_Right, BaseLocale.GetStr(RS.rs_Satiety), "");
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 134, IP_Right, BaseLocale.GetStr(RS.rs_Morality), player.MoralityName);
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 150, IP_Right, BaseLocale.GetStr(RS.rs_Armor), Convert.ToString(player.ArmorClass));
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 166, IP_Right, BaseLocale.GetStr(RS.rs_Damage), Convert.ToString(player.DBMin) + "-" + Convert.ToString(player.DBMax));
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 182, IP_Right, BaseLocale.GetStr(RS.rs_ToHit), Convert.ToString(player.ToHit) + "%");
                    Painter.DrawProperty(screen, IP_Left, IP_Top + 198, IP_Right, BaseLocale.GetStr(RS.rs_Speed), Convert.ToString(player.Speed));

                    if (!fHideCtlPanel || (fHideCtlPanel && fHideLocMap)) {
                        Painter.DrawProperty(screen, IP_Left, IP_Top + 214, IP_Right, BaseLocale.GetStr(RS.rs_Luck), Convert.ToString(player.Luck));
                        Painter.DrawProperty(screen, IP_Left, IP_Top + 230, IP_Right, BaseLocale.GetStr(RS.rs_Strength), Convert.ToString(player.Strength));
                        Painter.DrawProperty(screen, IP_Left, IP_Top + 246, IP_Right, BaseLocale.GetStr(RS.rs_Constitution), Convert.ToString(player.Constitution));
                        Painter.DrawProperty(screen, IP_Left, IP_Top + 262, IP_Right, BaseLocale.GetStr(RS.rs_Experience), Convert.ToString(player.Experience));
                        Painter.DrawProperty(screen, IP_Left, IP_Top + 278, IP_Right, BaseLocale.GetStr(RS.rs_Level), Convert.ToString(player.Level));
                        Painter.DrawProperty(screen, IP_Left, IP_Top + 294, IP_Right, BaseLocale.GetStr(RS.rs_Dexterity), Convert.ToString(player.Dexterity));
                    }

                    if (fHideCtlPanel && fHideLocMap) {
                        Painter.DrawProperty(screen, IP_Left, IP_Top + 326, IP_Right, BaseLocale.GetStr(RS.rs_Perception), Convert.ToString(player.Perception));
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.drawMainScreen(): " + ex.Message);
            }
        }

        protected override void DoPaintEvent(BaseScreen screen)
        {
            try {
                if (fGameSpace == null) {
                    return;
                }

                Player player = fGameSpace.Player;
                if (player == null) {
                    return;
                }

                NWField fld = (NWField)player.CurrentMap;
                ScreenRec scr = StaticData.dbScreens[(int)fMainScreen];
                if (fScrImage != null && scr.gfx != "") {
                    screen.DrawImage(0, 0, 0, 0, (int)fScrImage.Width, (int)fScrImage.Height, fScrImage, 255);
                }

                if (CtlCommon.SmFont == null) {
                    return;
                }

                switch (fMainScreen) {
                    case GameScreen.gsStartup:
                        int ats = 550;
                        screen.SetTextColor(BaseScreen.clGold, true);
                        screen.DrawText(8, ats, "Valhalla, Version 1.0, Copyright (c) 1992 by Norsehelm Productions", 0);
                        screen.DrawText(8, ats + 15, "Ragnarok, Version 2.5V, Copyright (c) 1992-1995 by Norsehelm Productions", 0);
                        screen.DrawText(8, ats + 30, (StaticData.Rs_GameName + ", " + StaticData.Rs_GameVersion + ", " + StaticData.Rs_GameCopyright), 0);
                        break;

                    case GameScreen.gsDead:
                        screen.SetTextColor(BaseScreen.clRed, true);
                        int num = fScoresList.ScoreCount;
                        for (int i = 0; i < num; i++) {
                            int atd = 20 + i * 18;
                            Score sr = fScoresList.GetScore(i);
                            screen.DrawText(8, atd, Convert.ToString(sr.Exp), 0);
                            screen.DrawText(100, atd, Convert.ToString(sr.Level), 0);
                            screen.DrawText(140, atd, sr.Name, 0);
                            screen.DrawText(300, atd, sr.Desc, 0);
                        }
                        break;

                    case GameScreen.gsMain:
                        if (fGameState == GameState.gsDefault) {
                            DrawMainScreen(screen, fld, player);
                        }
                        break;
                }

                if (GlobalVars.Debug_ShowFPS) {
                    screen.DrawText(5, 5, "FPS: " + Convert.ToString(FPS), 0);
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.doPaintEvent(): " + ex.Message);
            }
        }

        protected override void DoStyleChanged()
        {
            SetScreen(fMainScreen);
            if (fGameState == GameState.gsDefault) {
                fGameState = GameState.gsResLoad;
                if (fOldStyle != base.Style) {
                    AppDone();
                    AppInit();
                }
                fGameState = GameState.gsDefault;
            }
        }

        protected override BaseHintWindow CreateHintWindow(BaseMainWindow owner)
        {
            return new HintWindow(owner);
        }

        public void Clear()
        {
            if (fTargetData != null) {
                fTargetData.Dispose();
                fTargetData = null;
            }

            fTextBox.Clear();

            for (var gs = GameScreen.gsFirst; gs <= GameScreen.gsLast; gs++) {
                ScreenRec scr = StaticData.dbScreens[(int)gs];

                if (scr.status == ScreenStatus.ssAlready) {
                    scr.status = ScreenStatus.ssOnce;
                }
            }
        }

        public bool ShowNPCDialog(NWCreature collocutor)
        {
            bool result = false;
            if (collocutor == null) {
                ShowText(this, BaseLocale.GetStr(RS.rs_HereNobody));
            } else {
                if (!(collocutor.Brain is SentientBrain)) {
                    ShowText(this, BaseLocale.GetStr(RS.rs_ThisNotCollocutor));
                } else {
                    fNPCWindow.Collocutor = collocutor;
                    fNPCWindow.Show();
                    result = true;
                }
            }
            return result;
        }

        private void DoCreatureEvent(EventID eventID, object sender, object extData)
        {
            Player player = fGameSpace.Player;

            string sAct = "";
            switch (eventID) {
                case EventID.event_Attack:
                    {
                        sAct = "Attack";
                        break;
                    }
                case EventID.event_Killed:
                    {
                        sAct = "Killed";
                        break;
                    }
                case EventID.event_Move:
                    {
                        sAct = "Move";
                        break;
                    }
                case EventID.event_Shot:
                    {
                        sAct = "Shot";
                        break;
                    }
                case EventID.event_Slay:
                    {
                        sAct = "Slay";
                        break;
                    }
                case EventID.event_Wounded:
                    {
                        sAct = "Wounded";
                        break;
                    }
            }

            NWCreature creat = (NWCreature)sender;
            string sMonster = creat.Entry.Sfx;

            if (sMonster.CompareTo("") != 0) {
                PlaySound("creatures\\" + sMonster + "_" + sAct + ".ogg", SoundEngine.sk_Sound, creat.PosX, creat.PosY);
            }

            NWField crField = creat.CurrentField;
            if (!creat.IsPlayer && crField != null && player.CurrentMap.Equals(crField)) {
                int dist = MathHelper.Distance(creat.Location, player.Location);
                if (dist <= (int)player.Hear && AuxUtils.Chance(5) && crField.LandID != GlobalVars.Land_Village) {
                    ShowText(player, BaseLocale.GetStr(RS.rs_YouHearNoise));
                }
            }
        }

        private void DoItemEvent(EventID eventID, object sender, object extData)
        {
            Item item = (Item)extData;

            int id = item.CLSID_Renamed;
            string itKind = "";
            MaterialKind matKind = item.Material;
            switch (item.Kind) {
                case ItemKind.ik_Armor:
                case ItemKind.ik_HeavyArmor:
                case ItemKind.ik_MediumArmor:
                case ItemKind.ik_LightArmor:
                    if (matKind == MaterialKind.mk_Leather) {
                        itKind = "Leather";
                    } else {
                        itKind = "Armor";
                    }
                    break;

                case ItemKind.ik_DeadBody:
                case ItemKind.ik_Food:
                    itKind = "Food";
                    break;

                case ItemKind.ik_Potion:
                    itKind = "Potion";
                    break;

                case ItemKind.ik_Ring:
                    itKind = "Ring";
                    break;

                case ItemKind.ik_Tool:
                    if (id == GlobalVars.iid_Vial || id == GlobalVars.iid_Flask) {
                        itKind = "Bottle";
                    } else if (id == GlobalVars.iid_Stylus) {
                        itKind = "Stylus";
                    }
                    break;

                case ItemKind.ik_Wand:
                case ItemKind.ik_Spear:
                case ItemKind.ik_Axe:
                case ItemKind.ik_Bow:
                case ItemKind.ik_CrossBow:
                case ItemKind.ik_Projectile:
                    if (item.Flags.Contains(ItemFlags.if_Projectile)) {
                        itKind = "Quiver";
                    } else {
                        if (item.CLSID_Renamed == GlobalVars.iid_LongBow) {
                            itKind = "Bow";
                        } else {
                            if (item.CLSID_Renamed == GlobalVars.iid_CrossBow) {
                                itKind = "Crossbow";
                            }
                        }
                    }
                    break;

                case ItemKind.ik_BluntWeapon:
                    itKind = "Mace";
                    break;

                case ItemKind.ik_Scroll:
                    itKind = "Scroll";
                    break;

                case ItemKind.ik_Coin:
                    itKind = "Coin";
                    break;

                case ItemKind.ik_ShortBlade:
                    itKind = "Knife";
                    break;

                case ItemKind.ik_LongBlade:
                    itKind = "Sword";
                    break;

                case ItemKind.ik_Shield:
                    if (eventID < EventID.event_ItemRemove || eventID >= EventID.event_ItemBreak) {
                        itKind = "Armor";
                    } else {
                        itKind = "Shield";
                    }
                    break;

                case ItemKind.ik_Helmet:
                    if (eventID < EventID.event_ItemRemove || eventID >= EventID.event_ItemBreak) {
                        itKind = "Armor";
                    } else {
                        itKind = "Helmet";
                    }
                    break;

                case ItemKind.ik_Clothing:
                    if (matKind == MaterialKind.mk_Leather) {
                        itKind = "Leather";
                    } else {
                        itKind = "Clothing";
                    }
                    break;

                case ItemKind.ik_MusicalTool:
                    if (id == GlobalVars.iid_Flute) {
                        itKind = "Flute";
                    } else {
                        if (id == GlobalVars.iid_Ocarina) {
                            itKind = "Ocarina";
                        } else {
                            if (id == GlobalVars.iid_GlassOcarina) {
                                itKind = "GlassOcarina";
                            }
                        }
                    }
                    break;

                case ItemKind.ik_Misc:
                    // dummy
                    break;
            }

            string sAct = "";
            switch (eventID) {
                case EventID.event_ItemDrop:
                    {
                        sAct = "Drop";
                        break;
                    }
                case EventID.event_ItemPickup:
                    {
                        sAct = "Pickup";
                        break;
                    }
                case EventID.event_ItemRemove:
                    {
                        sAct = "Remove";
                        break;
                    }
                case EventID.event_ItemWear:
                    {
                        sAct = "Wear";
                        break;
                    }
                case EventID.event_ItemBreak:
                    {
                        sAct = "Break";
                        break;
                    }
                case EventID.event_ItemUse:
                    {
                        sAct = "Use";
                        break;
                    }
                case EventID.event_ItemMix:
                    {
                        sAct = "Mix";
                        break;
                    }
            }

            if (itKind.CompareTo("") != 0) {
                NWCreature creat = ((NWCreature)sender);
                PlaySound("items\\" + itKind + "_" + sAct + ".ogg", SoundEngine.sk_Sound, creat.PosX, creat.PosY);
            }
        }

        public void DoEvent(EventID eventID, object sender, object receiver, object extData)
        {
            try {
                var evtRec = StaticData.dbEvent[(int)eventID];

                if (eventID < EventID.event_First || eventID > EventID.event_Last) {
                    return;
                }

                if (evtRec.Flags.Contains(EventFlags.efInQueue)) {
                    fGameSpace.SendEvent(eventID, evtRec.Priority, sender, receiver);
                }

                if (evtRec.Flags.Contains(EventFlags.efInJournal)) {
                    string msg = fGameSpace.GetEventMessage(eventID, sender, receiver, extData);
                    ShowText(sender, msg);
                }

                Player player = fGameSpace.Player;

                switch (eventID) {
                    case EventID.event_Nothing:
                        // dummy
                        break;

                    case EventID.event_Startup:
                        SetScreen(GameScreen.gsStartup);
                        PlaySound("startup.ogg", SoundEngine.sk_Song, -1, -1);
                        break;

                    case EventID.event_Map:
                        PlaySound("map.ogg", SoundEngine.sk_Sound, -1, -1);
                        fMapDialog.Show();
                        break;

                    case EventID.event_About:
                        PlaySound("intro.ogg", SoundEngine.sk_Song, -1, -1);
                        fAboutDialog.Show();
                        break;

                    case EventID.event_Self:
                        PlaySound("self.ogg", SoundEngine.sk_Sound, -1, -1);
                        fSelfWindow.Show();
                        break;

                    case EventID.event_Wait:
                        fGameSpace.DoPlayerAction(CreatureAction.caWait, 0);
                        break;

                    case EventID.event_PickupAll:
                        {
                            NWField f = player.CurrentField;
                            Building b = f.FindBuilding(player.PosX, player.PosY);
                            if (b != null && b.Holder != null) {
                                DoEvent(EventID.event_Pack, null, null, null);
                            } else {
                                fGameSpace.DoPlayerAction(CreatureAction.caPickupAll, 0);
                            }
                            break;
                        }

                    case EventID.event_DoorClose:
                        {
                            Door door = (Door)sender;
                            PlaySound("doorClose.ogg", SoundEngine.sk_Sound, door.X, door.Y);
                            break;
                        }

                    case EventID.event_DoorOpen:
                        {
                            Door door = (Door)sender;
                            PlaySound("doorOpen.ogg", SoundEngine.sk_Sound, door.X, door.Y);
                            break;
                        }

                    case EventID.event_Help:
                        {
                            string helpFile = NWResourceManager.GetAppPath() + Locale.LANGS_FOLDER + LangExt + "_help.htm";
                            Help(helpFile);
                            break;
                        }

                    case EventID.event_LandEnter:
                        {
                            LandEntry eLand = (LandEntry)extData;
                            if (eLand.Song != "") {
                                PlaySound(eLand.Song, SoundEngine.sk_Song, -1, -1);
                            }
                            if (eLand.BackSFX.CompareTo("") != 0) {
                                PlaySound(eLand.BackSFX, SoundEngine.sk_Ambient, -1, -1);
                            }
                            GameScreen scr = eLand.Splash;
                            var srcRec = StaticData.dbScreens[(int)scr];
                            if (scr != GameScreen.gsNone && srcRec.status != ScreenStatus.ssAlready) {
                                if (srcRec.status == ScreenStatus.ssOnce) {
                                    srcRec.status = ScreenStatus.ssAlready;
                                }
                            } else {
                                scr = GameScreen.gsMain;
                            }
                            SetScreen(scr);
                            break;
                        }

                    case EventID.event_Intro:
                        PlaySound("intro.ogg", SoundEngine.sk_Song, -1, -1);
                        fIntroDialog.Show();
                        break;

                    case EventID.event_Knowledges:
                        if (fGameSpace.Player.Memory.Data.Count == 0) {
                            ShowText(fGameSpace.Player, BaseLocale.GetStr(RS.rs_YouKnowNothing));
                        } else {
                            PlaySound("knowledges.ogg", SoundEngine.sk_Sound, -1, -1);
                            fKnowledgesWindow.Show();
                        }
                        break;

                    case EventID.event_Skills:
                        if (fGameSpace.Player.SkillsCount == 0) {
                            ShowText(fGameSpace.Player, BaseLocale.GetStr(RS.rs_NoSkills));
                        } else {
                            PlaySound("skills.ogg", SoundEngine.sk_Sound, -1, -1);
                            fSkillsWindow.Mode = SkillsWindow.SWM_SKILLS;
                            fSkillsWindow.Show();
                        }
                        break;

                    case EventID.event_Menu:
                        fMenuDialog.Show();
                        break;

                    case EventID.event_Attack:
                    case EventID.event_Killed:
                    case EventID.event_Move:
                    case EventID.event_Shot:
                    case EventID.event_Slay:
                    case EventID.event_Wounded:
                        DoCreatureEvent(eventID, sender, extData);
                        break;

                    case EventID.event_Trade:
                        PlaySound("pay.ogg", SoundEngine.sk_Sound, -1, -1);
                        break;

                    case EventID.event_LevelUp:
                        PlaySound("levelUp.ogg", SoundEngine.sk_Sound, -1, -1);
                        ShowText(this, BaseLocale.GetStr(RS.rs_LevelUp) + Convert.ToString(((NWCreature)sender).Level) + ".");
                        break;

                    case EventID.event_Pack:
                        PlaySound("pack.ogg", SoundEngine.sk_Sound, -1, -1);
                        ShowInventory(null);
                        break;

                    case EventID.event_Options:
                        fOptions.Show();
                        break;

                    case EventID.event_Defeat:
                        fScoresList.Add(Score.KIND_DEFEAT, player.Name, BaseLocale.GetStr(RS.rs_RagnarokDefeat), player.Experience, player.Level);
                        PlaySound("defeat.ogg", SoundEngine.sk_Sound, -1, -1);
                        break;

                    case EventID.event_Dialog:
                        player.UseEffect(EffectID.eid_Dialog, null, InvokeMode.im_ItSelf, null);
                        break;

                    case EventID.event_Quit:
                        PlaySound("quit.ogg", SoundEngine.sk_Sound, -1, -1);
                        Quit();
                        break;

                    case EventID.event_Save:
                        fFilesWindow.FilesMode = FilesWindow.FWMODE_SAVE;
                        fFilesWindow.Show();
                        break;

                    case EventID.event_Load:
                        fFilesWindow.FilesMode = FilesWindow.FWMODE_LOAD;
                        fFilesWindow.Show();
                        break;

                    case EventID.event_New:
                        PlaySound("game_new.ogg", SoundEngine.sk_Sound, -1, -1);
                        NewGame_Start();
                        break;

                    case EventID.event_Dead:
                        DoPlayerDeathEvent();
                        SetScreen(GameScreen.gsDead);
                        break;

                    case EventID.event_Party:
                        fPartyWindow.Show();
                        break;

                    case EventID.event_Victory:
                        {
                            fScoresList.Add(Score.KIND_VICTORY, player.Name, BaseLocale.GetStr(RS.rs_RagnarokVictory), player.Experience, player.Level);
                            player.Name = BaseLocale.GetStr(RS.rs_Unknown);
                            PlaySound("victory.ogg", SoundEngine.sk_Sound, -1, -1);
                            SetScreen(GameScreen.gsVictory);
                            break;
                        }

                    case EventID.event_EffectSound:
                        {
                            GameEvent effectEvent = (GameEvent)extData;
                            int eid = effectEvent.CLSID_Renamed;
                            PlaySound("effects\\" + EffectsData.dbEffects[eid].SFX, SoundEngine.sk_Sound, effectEvent.PosX, effectEvent.PosY);
                            break;
                        }

                    case EventID.event_ItemDrop:
                    case EventID.event_ItemPickup:
                    case EventID.event_ItemRemove:
                    case EventID.event_ItemWear:
                    case EventID.event_ItemBreak:
                    case EventID.event_ItemUse:
                    case EventID.event_ItemMix:
                        DoItemEvent(eventID, sender, extData);
                        break;

                    case EventID.event_PlayerMoveN:
                    case EventID.event_PlayerMoveS:
                    case EventID.event_PlayerMoveW:
                    case EventID.event_PlayerMoveE:
                    case EventID.event_PlayerMoveNW:
                    case EventID.event_PlayerMoveNE:
                    case EventID.event_PlayerMoveSW:
                    case EventID.event_PlayerMoveSE:
                        {
                            int dir;
                            switch (eventID) {
                                case EventID.event_PlayerMoveN:
                                    {
                                        dir = Directions.DtNorth;
                                        break;
                                    }
                                case EventID.event_PlayerMoveS:
                                    {
                                        dir = Directions.DtSouth;
                                        break;
                                    }
                                case EventID.event_PlayerMoveW:
                                    {
                                        dir = Directions.DtWest;
                                        break;
                                    }
                                case EventID.event_PlayerMoveE:
                                    {
                                        dir = Directions.DtEast;
                                        break;
                                    }
                                case EventID.event_PlayerMoveNW:
                                    {
                                        dir = Directions.DtNorthWest;
                                        break;
                                    }
                                case EventID.event_PlayerMoveNE:
                                    {
                                        dir = Directions.DtNorthEast;
                                        break;
                                    }
                                case EventID.event_PlayerMoveSW:
                                    {
                                        dir = Directions.DtSouthWest;
                                        break;
                                    }
                                case EventID.event_PlayerMoveSE:
                                    {
                                        dir = Directions.DtSouthEast;
                                        break;
                                    }
                                default:
                                    {
                                        dir = Directions.DtNone;
                                        break;
                                    }
                            }

                            ShiftStates sh = (ShiftStates)extData;
                            DoPlayerMove(dir, sh, true);
                            break;
                        }

                    case EventID.event_PlayerMoveUp:
                    case EventID.event_PlayerMoveDown:
                        {
                            int dir = Directions.DtNone;
                            switch (eventID) {
                                case EventID.event_PlayerMoveUp:
                                    dir = Directions.DtZenith;
                                    break;
                                case EventID.event_PlayerMoveDown:
                                    dir = Directions.DtNadir;
                                    break;
                            }

                            ShiftStates sh = (ShiftStates)extData;
                            DoPlayerMove(dir, sh, false);
                            break;
                        }

                    case EventID.event_Journal:
                        fJournalWindow.Show();
                        break;
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.DoEvent(): " + ex.Message);
                throw ex;
            }
        }

        private void DoPlayerDeathEvent()
        {
            try {
                Player player = fGameSpace.Player;

                Ghost ghost = player.CreateGhost();
                if (ghost != null) {
                    fGhostsList.Add(ghost);
                }

                string in_land = BaseLocale.GetStr(RS.rs_DeathIn);
                in_land += player.CurrentField.LandEntry.GetNounDeclension(Number.nSingle, Case.cPrepositional);
                string deathReason = player.DeathReason;
                int p = deathReason.IndexOf(".");
                if (p >= 0) {
                    StringBuilder sb = new StringBuilder(deathReason);
                    sb.Insert(p, in_land);
                    deathReason = sb.ToString();
                } else {
                    deathReason += in_land;
                }

                fScoresList.Add(Score.KIND_DEATH, player.Name, deathReason, player.Experience, player.Level);

                player.Name = BaseLocale.GetStr(RS.rs_Unknown);

                if (ExtremeMode && fGameSpace.FileIndex >= 0) {
                    fGameSpace.EraseGame(fGameSpace.FileIndex);
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.doPlayerDeathEvent(): " + ex.Message);
                throw ex;
            }
        }

        public void NewGame_Start()
        {
            Clear();

            fGameState = GameState.gsWorldGen;
            fGameSpace.InitBegin();

            fHeroWindow.Show();
        }

        public void NewGame_Finish()
        {
            fGameSpace.InitEnd();
            fGameState = GameState.gsDefault;

            InitScripts();
        }

        public object ExecuteScript(string script)
        {
            try {
                InitScripts();

                return null;//FScriptEngine.eval(script);
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.executeScript(): " + ex.Message);
                return null;
            }
        }

        public void SetScriptVar(string @var, object value)
        {
            //FScriptEngine.put(@var, value);
        }

        private void InitScripts()
        {
            /*Player player = FGameSpace.Player;

            FScriptEngine.put("win", this);
            FScriptEngine.put("player", player);
            FScriptEngine.put("PC", player);*/
        }

        public string LangExt
        {
            get {
                int i = fLocale.FindLang(fLanguage);
                string result;
                if (i >= 0) {
                    result = fLocale.GetLang(i).Prefix;
                } else {
                    result = "en";
                }
                return result;
            }
        }

        public void ShowDivination()
        {
            fDivinationWindow.Show();
        }

        public void ShowInventory(NWCreature collocutor)
        {
            if (collocutor == null) {
                Player player = fGameSpace.Player;
                Building b = ((NWField)player.CurrentMap).FindBuilding(player.PosX, player.PosY);
                if (b != null) {
                    collocutor = ((NWCreature)b.Holder);
                }
            }
            fInventoryWindow.Collocutor = collocutor;
            fInventoryWindow.Show();
        }

        public void HideInventory()
        {
            fInventoryWindow.Hide();
        }

        public void LangChange(object sender)
        {
            BaseControl ctl = (BaseControl)sender;
            if (ctl.LangResID > -1) {
                ctl.Caption = BaseLocale.GetStr(ctl.LangResID);
            }
        }

        public override void ProcessGameStep()
        {
            if (fGameSpace != null) {
                fGameSpace.ProcessGameStep();
            }
        }

        public void ProgressInit(int stageCount)
        {
            fProgressWindow.StageCount = stageCount;
            fProgressWindow.Stage = 0;
            fProgressWindow.Show();
        }

        public void ProgressDone()
        {
            fProgressWindow.Hide();
        }

        public void ProgressStep()
        {
            fProgressWindow.Step();
        }

        public void ProgressLabel(string stageLabel)
        {
            fProgressWindow.StageLabel = stageLabel;
        }

        public void SetScreen(GameScreen value)
        {
            if (value != GameScreen.gsNone) {
                ScreenInternal = false;
                fMainScreen = value;
                ScreenInternal = true;
                    
                UpdateView();
                string sf = StaticData.dbScreens[(int)value].gfx;
                if (sf.CompareTo("") != 0) {
                    sf = "screens/" + sf + ".tga";
                    fScrImage = NWResourceManager.LoadImage(base.Screen, sf, BaseScreen.clNone);
                }
            }
        }

        public void ShowAlchemyWin()
        {
            fAlchemyWindow.Show();
        }

        public void ShowExchangeWin(NWCreature aCollocutor)
        {
            fExchangeWindow.Collocutor = aCollocutor;
            fExchangeWindow.Show();
        }

        public void ShowInput(string aCaption, IInputAcceptProc anAcceptProc)
        {
            fInputWindow.Caption = aCaption;
            fInputWindow.Value = "";
            fInputWindow.AcceptProc = anAcceptProc;
            fInputWindow.Show();
        }

        public void ShowKnowledge(string aName)
        {
            fKnowledgesWindow.Select(aName);
        }

        public void ShowMessage(string aText)
        {
            fMessageWindow.Text = aText;
            fMessageWindow.Show();
        }

        public void ShowRecruit(NWCreature aCollocutor)
        {
            fRecruitWindow.Collocutor = aCollocutor;
            fRecruitWindow.Show();
        }

        public void ShowSmithyWin()
        {
            fSmithyWindow.Show();
        }

        public void ShowStartupWin()
        {
            for (var i = GameScreen.gsFirst; i <= GameScreen.gsLast; i++) {
                ScreenRec scr = StaticData.dbScreens[(int)i];
                if (scr.status == ScreenStatus.ssAlready) {
                    scr.status = ScreenStatus.ssOnce;
                }
            }

            fStartupDialog.Show();
        }

        public void ShowTeachWin(NWCreature aCollocutor)
        {
            fTeachWindow.Collocutor = aCollocutor;
            fTeachWindow.Show();
        }

        public void ShowText(string text)
        {
            ShowText(this, text, new LogFeatures());
        }

        public void ShowText(object sender, string text)
        {
            ShowText(sender, text, new LogFeatures());
        }

        public void ShowText(object sender, string text, LogFeatures features)
        {
            try {
                if (sender != null && !string.IsNullOrEmpty(text)) {
                    bool res = sender.Equals(this) || sender.Equals(fGameSpace.Player) || (sender is NWCreature && fGameSpace.Player.IsAvailable((NWCreature)sender, true));
                    if (res) {
                        string temp = text;
                        temp = ConvertHelper.UniformName(temp);

                        string resText = "   " + temp;
                        fTextBox.Lines.Add(resText);

                        if (!features.Contains(LogFeatures.lfAux)) {
                            fGameSpace.Journal.StoreMessage(JournalItem.SIT_DEFAULT, resText);
                        }

                        if (features.Contains(LogFeatures.lfDialog)) {
                            ShowMessage(text);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWMainWindow.ShowText(): " + ex.Message);
            }
        }

        public void ShowTextAux(string text)
        {
            ShowText(this, text, new LogFeatures(LogFeatures.lfAux));
        }

        public void ShowTextRes(object sender, int textID, object[] args)
        {
            string msg = fGameSpace.ParseMessage(BaseLocale.GetStr(textID), args);
            ShowText(sender, msg, new LogFeatures());
        }

        public SoundEngine.Reverb SoundsReverb
        {
            set {
                fSoundEngine.SfxSetReverb(value);
            }
        }

        public void PlaySound(string fileName, int kind, int sX, int sY)
        {
            bool res = true;

            if (fGameState != GameState.gsWorldGen) {
                if (kind == SoundEngine.sk_Sound && sX != -1 && sY != -1) {
                    int dist = MathHelper.Distance(sX, sY, fGameSpace.Player.PosX, fGameSpace.Player.PosY);
                    res = (dist <= (int)fGameSpace.Player.Survey);
                }

                if (res) {
                    if (kind == SoundEngine.sk_Song) {
                        fileName = "songs/" + fileName;
                    }
                    fileName = "sfx/" + fileName;

                    ExtPoint ppt = new ExtPoint();
                    ppt.X = fGameSpace.Player.PosX;
                    ppt.Y = fGameSpace.Player.PosY;

                    ExtPoint spt = new ExtPoint();
                    spt.X = sX;
                    spt.Y = sY;
                    fSoundEngine.SfxPlay(fileName, kind, ppt, spt);
                }
            }
        }

        private void ClickTarget(ShiftStates shift)
        {
            Player player = fGameSpace.Player;
            NWField fld = (NWField)player.CurrentMap;

            if (fTargetData == null) {
                int dir = Directions.GetDirByCoords(player.PosX, player.PosY, fMouseMapX, fMouseMapY);
                int dist = MathHelper.Distance(player.PosX, player.PosY, fMouseMapX, fMouseMapY);
                if (dir != Directions.DtNone && dist == 1) {
                    DoPlayerMove(dir, shift, false);
                } else {
                    if (GlobalVars.Debug_Divinity) {
                        player.MoveTo(fMouseMapX, fMouseMapY);
                        fGameSpace.TurnState = TurnState.gtsDone;
                    }
                }
            } else {
                EffectTarget eRes = EffectTarget.et_None;
                switch (fTargetData.Target) {
                    case EffectTarget.et_PlaceNear:
                        {
                            if (player.IsNear(new ExtPoint(fMouseMapX, fMouseMapY))) {
                                fTargetData.Ext.SetParam(EffectParams.ep_Place, new ExtPoint(fMouseMapX, fMouseMapY));
                            } else {
                                eRes = EffectTarget.et_PlaceNear;
                            }
                            break;
                        }

                    case EffectTarget.et_PlaceFar:
                        fTargetData.Ext.SetParam(EffectParams.ep_Place, new ExtPoint(fMouseMapX, fMouseMapY));
                        break;

                    case EffectTarget.et_Direction:
                        {
                            int dir = Directions.GetDirByCoords(player.PosX, player.PosY, fMouseMapX, fMouseMapY);
                            if (dir == Directions.DtNone) {
                                eRes = EffectTarget.et_Direction;
                            } else {
                                fTargetData.Ext.SetParam(EffectParams.ep_Direction, dir);
                            }
                            break;
                        }

                    case EffectTarget.et_Item:
                        eRes = EffectTarget.et_Item;
                        break;

                    case EffectTarget.et_Creature:
                        {
                            NWCreature cr = (NWCreature)fld.FindCreature(fMouseMapX, fMouseMapY);
                            if (cr == null) {
                                eRes = EffectTarget.et_Creature;
                            } else {
                                fTargetData.Ext.SetParam(EffectParams.ep_Creature, cr);
                            }
                            break;
                        }
                }

                if (eRes == EffectTarget.et_None) {
                    UseTarget();
                } else {
                    ShowTextAux(BaseLocale.GetStr(StaticData.dbEffectTarget[(int)eRes].Invalid));
                }
            }
        }

        public void InitTarget(EffectID effectID, object source, InvokeMode invokeMode, EffectExt ext)
        {
            if (fTargetData != null) {
                fTargetData.Dispose();
                fTargetData = null;
            }

            fTargetData = new TargetObj();
            fTargetData.EffID = effectID;
            fTargetData.Source = source;
            fTargetData.InvMode = invokeMode;
            fTargetData.Target = EffectTarget.et_None;
            fTargetData.Ext = ext;
            UseTarget();
        }

        public void UseTarget()
        {
            EffectExt ext = fTargetData.Ext;

            for (int ep = EffectParams.ep_First; ep <= EffectParams.ep_Last; ep++) {
                if (ext.IsRequire(ep)) {
                    switch (ep) {
                        case EffectParams.ep_Place:
                            fTargetData.Target = EffectTarget.et_PlaceFar;
                            ShowTextAux(BaseLocale.GetStr(StaticData.dbEffectTarget[(int)EffectTarget.et_PlaceNear].Question));
                            return;

                        case EffectParams.ep_Direction:
                            fTargetData.Target = EffectTarget.et_Direction;
                            ShowTextAux(BaseLocale.GetStr(StaticData.dbEffectTarget[(int)EffectTarget.et_Direction].Question));
                            return;

                        case EffectParams.ep_Item:
                            fTargetData.Target = EffectTarget.et_Item;
                            ShowTextAux(BaseLocale.GetStr(StaticData.dbEffectTarget[(int)EffectTarget.et_Item].Question));
                            ShowInventory(null);
                            return;

                        case EffectParams.ep_Creature:
                            fTargetData.Target = EffectTarget.et_Creature;
                            ShowTextAux(BaseLocale.GetStr(StaticData.dbEffectTarget[(int)EffectTarget.et_Creature].Question));
                            return;

                        case EffectParams.ep_Area:
                            // dummy
                            break;

                        case EffectParams.ep_Land:
                            fSkillsWindow.Mode = SkillsWindow.SWM_LANDS;
                            fSkillsWindow.Show();
                            return;

                        case EffectParams.ep_DunRoom:
                            // dummy
                            break;

                        case EffectParams.ep_MonsterID:
                            // dummy
                            break;

                        case EffectParams.ep_TileID:
                            fSkillsWindow.Mode = SkillsWindow.SWM_TILES;
                            fSkillsWindow.Show();
                            return;

                        case EffectParams.ep_ScrollID:
                            if (fGameSpace.Player.ScrollsCount == 0) {
                                ShowTextAux(BaseLocale.GetStr(RS.rs_YouMayWriteNoScrolls));
                            } else {
                                fSkillsWindow.Mode = SkillsWindow.SWM_SCROLLS;
                                fSkillsWindow.Show();
                            }
                            return;

                        case EffectParams.ep_GodID:
                            fSkillsWindow.Mode = SkillsWindow.SWM_GODS;
                            fSkillsWindow.Show();
                            return;

                        case EffectParams.ep_ItemExt:
                            // dummy
                            break;
                    }
                }
            }

            fGameSpace.Player.UseEffect(fTargetData.EffID, fTargetData.Source, fTargetData.InvMode, fTargetData.Ext);

            if (fTargetData != null) {
                fTargetData.Dispose();
                fTargetData = null;
            }
        }

        public override void DoActive(bool active)
        {
            if (active) {
                fSoundEngine.SfxResume();
            } else {
                fSoundEngine.SfxSuspend();
            }
        }

        public override int Style
        {
            set {
                fOldStyle = base.Style;
                base.Style = value;
            }
        }

        /// <param name="args"> the command line arguments </param>
        public static void Main(string[] args)
        {
            InitEnvironment(StaticData.Rs_GameName);
            if (!InstanceExists) {
                GlobalVars.nwrWin = new NWMainWindow();
                GlobalVars.nwrWin.Run();
                GlobalVars.nwrWin.Dispose();
            }
        }
    }

}