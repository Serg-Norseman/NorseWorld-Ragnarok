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
package nwr.main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import jzrlib.core.Directions;
import jzrlib.core.EntityList;
import jzrlib.core.GameEntity;
import jzrlib.core.Point;
import jzrlib.core.Rect;
import jzrlib.map.BaseTile;
import jzrlib.map.TileStates;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.Logger;
import jzrlib.utils.TextUtils;
import nwr.core.GameEvent;
import nwr.core.Locale;
import nwr.core.RS;
import nwr.core.StaticData;
import nwr.core.types.CreatureAction;
import nwr.core.types.EventFlags;
import nwr.core.types.EventID;
import nwr.core.types.GameScreen;
import nwr.core.types.GameState;
import nwr.core.types.ItemKind;
import nwr.core.types.ItfElement;
import nwr.core.types.LogFeatures;
import nwr.core.types.MainControl;
import nwr.core.types.MaterialKind;
import nwr.core.types.PlaceFlags;
import nwr.core.types.PlaceID;
import nwr.core.types.PlaceRec;
import nwr.core.types.RaceID;
import nwr.core.types.RuneRec;
import nwr.core.types.ScreenStatus;
import nwr.core.types.SkillID;
import nwr.core.types.SkillKind;
import nwr.core.types.SymbolID;
import nwr.core.types.TurnState;
import nwr.core.types.UserAction;
import nwr.creatures.NWCreature;
import nwr.creatures.brain.SentientBrain;
import nwr.creatures.specials.ArticulateCreature;
import nwr.creatures.specials.ArticulateSegment;
import nwr.database.CreatureEntry;
import nwr.database.ItemEntry;
import nwr.database.ItemFlags;
import nwr.database.LandEntry;
import nwr.database.LayerEntry;
import nwr.effects.EffectExt;
import nwr.effects.EffectID;
import nwr.effects.EffectParams;
import nwr.effects.EffectTarget;
import nwr.effects.EffectsData;
import nwr.effects.InvokeMode;
import nwr.engine.BaseControl;
import nwr.engine.BaseHintWindow;
import nwr.engine.BaseImage;
import nwr.engine.BaseMainWindow;
import nwr.engine.BaseScreen;
import nwr.engine.HotKey;
import nwr.engine.ImageList;
import nwr.engine.KeyEventArgs;
import nwr.engine.Keys;
import nwr.engine.MouseButton;
import nwr.engine.MouseEventArgs;
import nwr.engine.MouseMoveEventArgs;
import nwr.engine.ResourceManager;
import nwr.engine.ShiftStates;
import nwr.engine.SoundEngine;
import nwr.engine.viewer.IsoViewer;
import nwr.game.NWGameSpace;
import nwr.game.ghosts.Ghost;
import nwr.game.ghosts.GhostsList;
import nwr.game.scores.Score;
import nwr.game.scores.ScoresList;
import nwr.game.story.JournalItem;
import jzrlib.grammar.Case;
import nwr.gui.AboutWindow;
import nwr.gui.AlchemyWindow;
import nwr.gui.ConsoleWindow;
import nwr.gui.DivinationWindow;
import nwr.gui.ExchangeWindow;
import nwr.gui.FilesWindow;
import nwr.gui.HeroWindow;
import nwr.gui.IInputAcceptProc;
import nwr.gui.InputWindow;
import nwr.gui.IntroWindow;
import nwr.gui.InventoryWindow;
import nwr.gui.JournalWindow;
import nwr.gui.KnowledgesWindow;
import nwr.gui.MapWindow;
import nwr.gui.MenuWindow;
import nwr.gui.MessageWindow;
import nwr.gui.NPCWindow;
import nwr.gui.NWButton;
import nwr.gui.OptionsWindow;
import nwr.gui.PartyWindow;
import nwr.gui.ProgressWindow;
import nwr.gui.RecruitWindow;
import nwr.gui.SelfWindow;
import nwr.gui.SkillsWindow;
import nwr.gui.SmithyWindow;
import nwr.gui.StartupWindow;
import nwr.gui.TeachWindow;
import nwr.gui.controls.CtlCommon;
import nwr.gui.controls.CustomButton;
import nwr.gui.controls.HintWindow;
import nwr.gui.controls.ProgressBar;
import nwr.gui.controls.TextBox;
import nwr.item.Item;
import nwr.player.Player;
import nwr.universe.Building;
import nwr.universe.Door;
import nwr.universe.MapObject;
import nwr.universe.NWField;
import nwr.universe.NWTile;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class NWMainWindow extends BaseMainWindow
{
    // graphics styles
    public static final int RGS_CLASSIC = 0;
    public static final int RGS_MODERN = 1;

    // Objects paint mode
    public static final int OPM_FIELD = 0;
    public static final int OPM_LOCAL = 1;
    public static final int OPM_ISO = 2;

    // fields
    private boolean fAutoPickup;
    private boolean fCircularFOV;
    private boolean fCursorValid;
    private boolean fExtremeMode;
    private NWGameSpace fGameSpace;
    private GameState fGameState;
    private Locale fLocale;
    private String fLanguage;
    private GameScreen fMainScreen;
    private int fMouseMapX;
    private int fMouseMapY;
    private Rect FMV_Rect;
    private int fOldStyle;
    private ScoresList fScoresList;
    private GhostsList fGhostsList;
    private int fSongsVolume;
    private int fSoundsVolume;
    private TargetObj fTargetData;
    private TextBox fTextBox;
    private boolean fHideCtlPanel;
    private boolean fHideInfoPanel;
    private boolean fHideLocMap;
    private boolean fInventoryOnlyIcons;
    private Point FLocMapSize;
    private Rect FLV_Rect;
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

    private final SoundEngine fSoundEngine;
    private final ScriptEngine fScriptEngine;

    private int IP_Top = 150;
    private int IP_Left = 640;
    private int IP_Right = 792;

    public ImageList Resources;
    public ImageList Symbols;

    public int DialogVisibleRefs;
    
    public NWMainWindow()
    {
        super(800, 600);
        super.setCaption(StaticData.rs_GameName);
        this.fSoundEngine = new SoundEngine();
        this.fScriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    }

    public final boolean getAutoPickup()
    {
        return this.fAutoPickup;
    }

    public final boolean getCircularFOV()
    {
        return this.fCircularFOV;
    }

    public final boolean getExtremeMode()
    {
        return this.fExtremeMode;
    }

    public final NWGameSpace getGameSpace()
    {
        return this.fGameSpace;
    }

    public final GameState getGameState()
    {
        return this.fGameState;
    }

    public final Locale getLocale()
    {
        return this.fLocale;
    }

    public final String getLanguage()
    {
        return this.fLanguage;
    }

    public final GameScreen getMainScreen()
    {
        return this.fMainScreen;
    }

    public final int getSongsVolume()
    {
        return this.fSongsVolume;
    }

    public final int getSoundsVolume()
    {
        return this.fSoundsVolume;
    }

    public final TargetObj getTargetData()
    {
        return this.fTargetData;
    }

    public final boolean getHideCtlPanel()
    {
        return this.fHideCtlPanel;
    }

    public final boolean getHideInfoPanel()
    {
        return this.fHideInfoPanel;
    }

    public final boolean getHideLocMap()
    {
        return this.fHideLocMap;
    }

    public final boolean getInventoryOnlyIcons()
    {
        return this.fInventoryOnlyIcons;
    }

    public GhostsList getGhostsList()
    {
        return this.fGhostsList;
    }

    private Rect getLocMapRect()
    {
        int pX = this.fGameSpace.getPlayer().getPosX();
        int pY = this.fGameSpace.getPlayer().getPosY();
        return new Rect(pX - this.FLocMapSize.X, pY - this.FLocMapSize.Y, pX + this.FLocMapSize.X, pY + this.FLocMapSize.Y);
    }

    private void getIsoOffset(int iIndex, int x0, int y0, Point scrPoint)
    {
        BaseImage img = this.Resources.getImage(iIndex);
        if (img == null) {
            scrPoint.X = x0;
            scrPoint.Y = y0 - 96;
        } else {
            scrPoint.X = x0 + (64 - (int) img.Width) / 2;
            scrPoint.Y = y0 + (32 - (int) img.Height);
        }
    }

    private void MainButtonClick(Object sender)
    {
        MainControl mc = MainControl.forValue(((NWButton) sender).Tag);
        if (mc != MainControl.mbNone) {
            this.doEvent(mc.Event, null, null, null);
        }
    }

    public final void setHideCtlPanel(boolean value)
    {
        if (this.fHideCtlPanel != value) {
            this.fHideCtlPanel = value;
            this.updateView();
        }
    }

    public final void setHideInfoPanel(boolean value)
    {
        if (this.fHideInfoPanel != value) {
            this.fHideInfoPanel = value;
            this.updateView();
        }
    }

    public final void setHideLocMap(boolean value)
    {
        if (this.fHideLocMap != value) {
            this.fHideLocMap = value;
            this.updateView();
        }
    }

    public final void setInventoryOnlyIcons(boolean value)
    {
        this.fInventoryOnlyIcons = value;
    }

    private void ViewerTilePaint(Object sender, int X, int Y, BaseTile tile, Rect R, BaseScreen screen)
    {
        try {
            Player player = this.fGameSpace.getPlayer();
            NWField fld;
            if (player.isHallucinations()) {
                fld = player.HalMap;
            } else {
                fld = (NWField) player.getCurrentMap();
            }

            NWTile nwTile = fld.getTile(X, Y);
            Point scrPt = new Point();

            if ((tile.hasState(TileStates.TS_VISITED))) {
                int bg = (int) tile.Background;
                int fg = Painter.vtpChange(fld, (int) tile.Foreground);
                int bgExt = (int) tile.BackgroundExt;
                int fgExt = (int) tile.ForegroundExt;

                short op = 255;
                int xx = R.Left;
                int yy = R.Top - 96;

                this.Resources.drawImage(screen, xx, yy, Painter.getTileImageIndex((short) bg), op);
                if (bgExt != PlaceID.pid_Undefined) {
                    this.Resources.drawImage(screen, xx, yy, Painter.getTileImageIndex((short) bgExt), op);
                }
                if (fg != PlaceID.pid_Undefined) {
                    if (Painter.vtpForwardPlayer(player, X, Y)) {
                        op = 100;//191;
                    }

                    boolean trap = fld.isTrap(X, Y);
                    if (!trap || (trap && (nwTile.Trap_Discovered || GlobalVars.Debug_Divinity))) {
                        int idx = Painter.getTileImageIndex((short) fg);
                        if (fg == PlaceID.pid_CaveWall) {
                            if (player.LayerID == GlobalVars.Layer_Svartalfheim2) {
                                idx++;
                            }
                            if (player.LayerID == GlobalVars.Layer_Svartalfheim3 || player.LayerID == GlobalVars.Layer_MimerWell) {
                                idx += 2;
                            }
                        }

                        this.getIsoOffset(idx, R.Left, R.Top, scrPt);
                        this.Resources.drawImage(screen, scrPt.X, scrPt.Y, idx, op);

                        if (fgExt != PlaceID.pid_Undefined) {
                            idx = Painter.getTileImageIndex((short) fgExt);

                            this.getIsoOffset(idx, R.Left, R.Top, scrPt);
                            this.Resources.drawImage(screen, scrPt.X, scrPt.Y, idx, op);
                        }
                    }
                }

                if ((tile.hasState(TileStates.TS_SEEN)) || player.getEffects().findEffectByID(EffectID.eid_DetectItems) != null || GlobalVars.Debug_Divinity) {
                    op = 255;
                    xx = R.Left + 16;
                    yy = R.Top - 15;

                    int num = fld.getItems().getCount();
                    for (int i = 0; i < num; i++) {
                        Item it = fld.getItems().getItem(i);
                        if (it.getPosX() == X && it.getPosY() == Y) {
                            int idx = it.getImageIndex();
                            BaseImage img = this.Resources.getImage(idx);
                            if (img.Height == 30) {
                                yy = R.Top + 1;
                            } else {
                                if (img.Height == 32) {
                                    yy = R.Top;
                                }
                            }
                            this.Resources.drawImage(screen, xx, yy, idx, op);
                        }
                    }
                }

                NWCreature creature = (NWCreature) fld.findCreature(X, Y);
                if (creature != null && this.fGameSpace.isCreatureVisible(creature, tile)) {
                    this.getIsoOffset(creature.getEntry().ImageIndex, R.Left, R.Top, scrPt);
                    if ((!tile.hasState(TileStates.TS_SEEN)) || creature.getEffects().findEffectByID(EffectID.eid_Invisibility) != null) {
                        op = 127;
                    } else {
                        op = 255;
                    }
                    this.Resources.drawImage(screen, scrPt.X, scrPt.Y, creature.getEntry().ImageIndex, op);
                }
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.ViewerTilePaint(): " + ex.getMessage());
        }
    }

    private void AppDone()
    {
        for (int pid = PlaceID.pid_First; pid <= PlaceID.pid_Last; pid++) {
            StaticData.dbPlaces[pid].ImageIndex = -1;
        }

        for (int sk = SkillID.Sk_First; sk <= SkillID.Sk_Last; sk++) {
            SkillID skid = SkillID.forValue(sk);
            skid.ImageIndex = -1;
        }

        for (int sym = SymbolID.sid_First; sym <= SymbolID.sid_Last; sym++) {
            SymbolID sid = SymbolID.forValue(sym);
            sid.ImageIndex = -1;
        }

        for (int ie = ItfElement.id_First; ie <= ItfElement.id_Last; ie++) {
            ItfElement itf = ItfElement.forValue(ie);
            itf.ImageIndex = -1;
        }

        for (int i = 0; i < GlobalVars.dbItems.getCount(); i++) {
            ItemEntry eItem = (ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.dbItems.get(i));
            eItem.ImageIndex = -1;
        }

        for (int i = 0; i < GlobalVars.dbCreatures.getCount(); i++) {
            CreatureEntry eCreature = (CreatureEntry) GlobalVars.nwrBase.getEntry(GlobalVars.dbCreatures.get(i));
            eCreature.ImageIndex = -1;
        }

        for (int i = 0; i < GlobalVars.dbLayers.getCount(); i++) {
            LayerEntry eLayer = (LayerEntry) GlobalVars.nwrBase.getEntry(GlobalVars.dbLayers.get(i));
            eLayer.IconsIndex = -1;
        }

        this.Resources.dispose();
        this.Symbols.dispose();

        this.fScoresList.save(ResourceManager.getAppPath() + StaticData.SCORES_FILE);
        this.fScoresList.dispose();

        this.fGhostsList.save(ResourceManager.getAppPath() + StaticData.GHOSTS_FILE);
        this.fGhostsList.dispose();
    }

    private void AppInit()
    {
        try {
            this.fScoresList = new ScoresList();
            this.fScoresList.load(ResourceManager.getAppPath() + StaticData.SCORES_FILE);

            this.fGhostsList = new GhostsList();
            this.fGhostsList.load(ResourceManager.getAppPath() + StaticData.GHOSTS_FILE);

            this.Resources = new ImageList(super.getScreen());
            this.Symbols = new ImageList(super.getScreen());
            
            int cnt = PlaceID.pid_Last + (ItemKind.ik_Last + 1) +
                    GlobalVars.dbItems.getCount() + GlobalVars.dbCreatures.getCount() +
                    EffectID.eid_Last + SkillID.Sk_Last + (ItfElement.id_Last + 1) +
                    (SymbolID.sid_Last + 1) + 
                    GlobalVars.dbLayers.getCount() + StaticData.dbRunes.length;

            this.ProgressInit(cnt);
            this.ProgressLabel(Locale.getStr(RS.rs_ResLoading));

            int col;
            String dir;

            if (this.getStyle() == NWMainWindow.RGS_MODERN) {
                col = BaseScreen.clBlack;
                dir = "tileset_ms/";
            } else {
                col = BaseScreen.clWhite;
                dir = "tileset/";
            }

            for (int pd = PlaceID.pid_First; pd <= PlaceID.pid_Last; pd++) {
                PlaceRec pRec = StaticData.dbPlaces[pd];
                String s = dir + pRec.ImageName;

                if ((pRec.Signs.contains(PlaceFlags.psMask))) {
                    pRec.ImageIndex = this.Resources.getImagesCount();
                } else {
                    pRec.ImageIndex = this.Resources.addImage(s + ".tga", col, false);
                }

                pRec.SubsLoaded = 0;

                for (int i = 1; i <= pRec.subTiles; i++) {
                    int idx = this.Resources.addImage(s + "_" + String.valueOf(i) + ".tga", col, false);

                    if (idx > -1) {
                        pRec.SubsLoaded = pRec.SubsLoaded + 1;
                    }
                }

                this.ProgressStep();
            }

            dir = "items/";
            col = BaseScreen.clWhite;
            for (int ik = ItemKind.ik_First; ik <= ItemKind.ik_Last; ik++) {
                ItemKind itk = ItemKind.forValue(ik);
                
                if (itk.RndImage) {
                    String s = dir + itk.ImageSign + "_";
                    itk.ImagesLoaded = 0;
                    itk.ImageIndex = this.Resources.getImagesCount();

                    int num2 = (int) itk.Images;
                    for (int i = 1; i <= num2; i++) {
                        String fn = s + String.valueOf(i) + ".tga";
                        int idx = this.Resources.addImage(fn, col, true);

                        if (idx > -1) {
                            itk.ImagesLoaded = itk.ImagesLoaded + 1;
                        }
                    }
                }

                this.ProgressStep();
            }

            for (int i = 0; i < GlobalVars.dbItems.getCount(); i++) {
                ItemEntry eItem = (ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.dbItems.get(i));

                if (!eItem.isMeta()) {
                    String s = eItem.ImageName;
                    String fn = dir + s + ".tga";

                    int idx = this.Resources.addImage(fn, col, true);
                    if (idx < 0) {
                        fn = dir + "Unknown.tga";
                        idx = this.Resources.addImage(fn, col, true);
                    }

                    eItem.ImageIndex = idx;
                    eItem.FramesLoaded = 0;

                    for (int k = 1; k <= (int) eItem.FramesCount; k++) {
                        fn = dir + s + "_" + String.valueOf(k) + ".tga";

                        idx = this.Resources.addImage(fn, col, true);
                        if (idx < 0) {
                            fn = dir + "Unknown.tga";
                            idx = this.Resources.addImage(fn, col, true);
                        }

                        if (idx > -1) {
                            eItem.FramesLoaded += 1;
                        }
                    }

                    if (this.getStyle() == NWMainWindow.RGS_MODERN && eItem.ItmKind.RndImage && eItem.ItmKind.ImagesLoaded > 0) {
                        int k = AuxUtils.getRandom(eItem.ItmKind.ImagesLoaded + 1);
                        if (k > 0) {
                            eItem.ImageIndex = eItem.ItmKind.ImageIndex + (k - 1);
                        }
                    }
                }

                this.ProgressStep();
            }

            dir = "creatures/";
            col = BaseScreen.clWhite;
            for (int j = 0; j < GlobalVars.dbCreatures.getCount(); j++) {
                CreatureEntry eCreature = (CreatureEntry) GlobalVars.nwrBase.getEntry(GlobalVars.dbCreatures.get(j));

                String fn = dir + eCreature.gfx + ".tga";
                String fn_gray = dir + "gray/" + eCreature.gfx + "_gray.tga";
                
                int idx = this.Resources.addImage(fn, col, true);
                eCreature.ImageIndex = idx;
                if (idx >= 0) {
                    eCreature.GrayImageIndex = this.Resources.addImage(fn_gray, col, true);
                }
                eCreature.FramesLoaded = 0;

                for (int i = 1; i <= (int) eCreature.FramesCount; i++) {
                    fn = dir + eCreature.gfx + "_" + AuxUtils.adjustNumber(i, 2) + ".tga";

                    idx = this.Resources.addImage(fn, col, true);
                    if (idx < 0) {
                        fn = dir + "Unknown.tga";
                        idx = this.Resources.addImage(fn, col, true);
                    }

                    if (idx > -1) {
                        eCreature.FramesLoaded += 1;
                    }
                }

                this.ProgressStep();
            }

            dir = "effects/";
            col = BaseScreen.clWhite;
            for (int eff = EffectID.eid_First; eff <= EffectID.eid_Last; eff++) {
                int idx;

                if (EffectsData.dbEffects[eff].GFX.compareTo("") == 0 || EffectsData.dbEffects[eff].FrameCount == 0) {
                    idx = -1;
                } else {
                    idx = this.Resources.getImagesCount();
                    String s = dir + EffectsData.dbEffects[eff].GFX + "_";

                    int num7 = (int) EffectsData.dbEffects[eff].FrameCount;
                    for (int i = 1; i <= num7; i++) {
                        this.Resources.addImage(s + String.valueOf(i) + ".tga", col, true);
                    }
                }
                EffectsData.dbEffects[eff].ImageIndex = idx;

                this.ProgressStep();
            }

            dir = "itf/";
            for (int sk = SkillID.Sk_First; sk <= SkillID.Sk_Last; sk++) {
                SkillID skid = SkillID.forValue(sk);

                if (skid.Kinds != SkillKind.ssInnatePower && skid.gfx.compareTo("") != 0) {
                    String s = dir + skid.gfx + ".tga";
                    skid.ImageIndex = this.Resources.addImage(s, BaseScreen.clWhite, false);
                }

                this.ProgressStep();
            }

            for (int ie = ItfElement.id_Up.getValue(); ie <= ItfElement.id_FileNum.getValue(); ie++) {
                ItfElement itf = ItfElement.forValue(ie);
                String s = dir + itf.fName;
                itf.ImageIndex = this.Resources.addImage(s, itf.transColor, false);
                this.ProgressStep();
            }

            dir = "symbols/";
            for (int sym = SymbolID.sid_First; sym <= SymbolID.sid_Last; sym++) {
                SymbolID sid = SymbolID.forValue(sym);
                
                String s = sid.gfx;
                sid.ImageIndex = this.Symbols.addImage(dir + s + ".tga", BaseScreen.clWhite, false);

                int num8 = sid.subCount;
                for (int j = 1; j <= num8; j++) {
                    this.Symbols.addImage((dir + s + "_" + AuxUtils.adjustNumber(j, 3) + ".tga"), BaseScreen.clWhite, false);
                }

                this.ProgressStep();
            }

            dir = "map/";
            for (int j = 0; j < GlobalVars.dbLayers.getCount(); j++) {
                LayerEntry eLayer = (LayerEntry) GlobalVars.nwrBase.getEntry(GlobalVars.dbLayers.get(j));
                eLayer.IconsIndex = this.Resources.getImagesCount();

                for (int y = 0; y < eLayer.H; y++) {
                    for (int x = 0; x < eLayer.W; x++) {
                        String s = dir + eLayer.IconsName + String.valueOf(x) + String.valueOf(y) + ".tga";
                        this.Resources.addImage(s, BaseScreen.clFuchsia, false);
                    }
                }

                this.ProgressStep();
            }

            dir = "runes/";
            for (RuneRec rune : StaticData.dbRunes) {
                String s = dir + rune.Sign + ".tga";
                rune.ImageIndex = this.Resources.addImage(s, BaseScreen.clWhite, false);
                this.ProgressStep();
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.AppInit(): " + ex.getMessage());
            super.quit();
        }

        this.ProgressDone();
    }

    private void drawCreatureInt(BaseScreen screen, NWField field, NWCreature creature, int mode, int tX, int tY, int offset)
    {
        NWTile place = field.getTile(tX, tY);
        if (place != null && this.fGameSpace.isCreatureVisible(creature, place)) {
            boolean transparent = (creature.getEffects().findEffectByID(EffectID.eid_Invisibility) != null) 
                    || (creature.Ghost) || (creature.Illusion);
            
            switch (mode) {
                case NWMainWindow.OPM_FIELD: {
                    int xx = this.FMV_Rect.Left + 8 * (tX + 1);
                    int yy = this.FMV_Rect.Top + 10 * (tY + 1);
                    short op = 255;

                    if (creature.getEntry().Race == RaceID.crHuman) {
                        SymbolID sid;

                        if (creature.isPlayer() || creature.isMercenary()) {
                            sid = SymbolID.sid_Player;
                        } else {
                            if (creature.isStoning()) {
                                sid = SymbolID.sid_StoningHuman;
                            } else {
                                if (this.fGameSpace.getPlayer().isEnemy(creature)) {
                                    sid = SymbolID.sid_EnemyHuman;
                                } else {
                                    sid = SymbolID.sid_AllyHuman;
                                }
                            }
                        }
                        this.Symbols.drawImage(screen, xx, yy, sid.ImageIndex, op);
                    } else {
                        SymbolID sid = creature.getSymbol();
                        if (sid == SymbolID.sid_None) {
                            if (creature.isStoning()) {
                                screen.setTextColor(BaseScreen.clGray, true);
                            } else {
                                if (this.fGameSpace.getPlayer().isEnemy(creature)) {
                                    screen.setTextColor(BaseScreen.clRed, true);
                                } else {
                                    screen.setTextColor(BaseScreen.clLime, true);
                                }
                            }

                            String sym = String.valueOf(creature.getEntry().Symbol);
                            screen.drawText(xx + 1, yy, sym, 0);
                        } else {
                            this.Symbols.drawImage(screen, xx, yy, sid.ImageIndex + offset, op);
                        }
                    }
                }
                break;

                case NWMainWindow.OPM_LOCAL: {
                    Rect rt = this.getLocMapRect();
                    int xx = this.FLV_Rect.Left + 32 * (tX - rt.Left);
                    int yy = this.FLV_Rect.Top + 30 * (tY - rt.Top);
                    if (rt.contains(tX, tY)) {
                        short op;
                        if (transparent) {
                            op = 127;
                        } else {
                            op = this.fGameSpace.getTileBrightness(field, place.States, false);
                        }

                        if (offset < 0) {
                            offset = 0;
                        } else {
                            if (offset > 0) {
                                offset++;
                            }
                        }

                        int idx;
                        if (creature.isStoning()) {
                            idx = creature.getEntry().GrayImageIndex;
                        } else {
                            idx = creature.getEntry().ImageIndex + offset;
                        }
                        
                        if (creature.getEffects().findEffectByID(EffectID.eid_Sail) != null) {
                            idx = ((ItemEntry) GlobalVars.nwrBase.getEntry(GlobalVars.iid_Skidbladnir)).ImageIndex;
                        }

                        this.Resources.drawImage(screen, xx, yy, idx, op);
                        
                        if (creature.getEffects().findEffectByID(EffectID.eid_CaughtInNet) != null) {
                            idx = ((ItemEntry) GlobalVars.nwrBase.findEntryBySign("Net")).ImageIndex;
                            this.Resources.drawImage(screen, xx, yy, idx, 127);
                        }
                    }
                }
                break;
            }
        }
    }

    private void drawCreatures(BaseScreen screen, NWField field, int mode)
    {
        int num1 = field.getCreatures().getCount();
        for (int i = 0; i < num1; i++) {
            NWCreature creature = field.getCreatures().getItem(i);
            int px = creature.getPosX();
            int py = creature.getPosY();

            if (creature instanceof ArticulateCreature) {
                ArticulateCreature art = (ArticulateCreature) creature;

                int num2 = art.getSize();
                for (int k = 0; k < num2; k++) {
                    ArticulateSegment seg = art.getSegment(k);
                    this.drawCreatureInt(screen, field, creature, mode, seg.X, seg.Y, seg.ImageIndex);
                }
            } else {
                this.drawCreatureInt(screen, field, creature, mode, px, py, 0);
            }
        }
    }

    private void drawItems(BaseScreen screen, NWField field, int mode)
    {
        Player player = this.fGameSpace.getPlayer();
        boolean detectItems = (player.getEffects().findEffectByID(EffectID.eid_DetectItems) != null);

        int num = field.getItems().getCount();
        for (int i = 0; i < num; i++) {
            Item item = field.getItems().getItem(i);
            int px = item.getPosX();
            int py = item.getPosY();

            NWTile place = field.getTile(px, py);
            boolean seen = ((place.hasState(TileStates.TS_SEEN)) || GlobalVars.Debug_Divinity || detectItems);

            if (seen) {
                switch (mode) {
                    case NWMainWindow.OPM_FIELD: {
                        SymbolID sid = item.getSymbol();
                        int xx = this.FMV_Rect.Left + 8 * (px + 1);
                        int yy = this.FMV_Rect.Top + 10 * (py + 1);
                        short op = this.fGameSpace.getTileBrightness(field, place.States, true);
                        this.Symbols.drawImage(screen, xx, yy, sid.ImageIndex, op);
                    }
                    break;

                    case NWMainWindow.OPM_LOCAL:
                        Rect rt = this.getLocMapRect();
                        if (item.inRect(rt)) {
                            int xx = this.FLV_Rect.Left + 32 * (px - rt.Left);
                            int yy = this.FLV_Rect.Top + 30 * (py - rt.Top);
                            short op = this.fGameSpace.getTileBrightness(field, place.States, false);
                            int idx = item.getImageIndex();
                            this.Resources.drawImage(screen, xx, yy, idx, op);
                        }
                        break;
                }
            }
        }
    }

    private void drawFeatures(BaseScreen screen, NWField field, Rect rt)
    {
        Player player = this.fGameSpace.getPlayer();

        EntityList features = field.getFeatures();
        int num = features.getCount();
        for (int i = 0; i < num; i++) {
            GameEntity he = features.getItem(i);

            if (he instanceof MapObject) {
                MapObject mf = (MapObject) he;
                int px = mf.PosX;
                int py = mf.PosY;
                NWTile place = field.getTile(px, py);

                short op = this.fGameSpace.getTileBrightness(field, place.States, true);

                if (player.isSeen(px, py, false)) {
                    int xx = this.FLV_Rect.Left + 32 * (px - rt.Left);
                    int yy = this.FLV_Rect.Top + 30 * (py - rt.Top);
                    this.Resources.drawImage(screen, xx, yy, mf.ImageIndex, op);
                }
            }
        }
    }

    private void updateFeatures(long time)
    {
        Player player = this.fGameSpace.getPlayer();
        NWField field = player.getCurrentField();

        EntityList features = field.getFeatures();
        for (int i = features.getCount() - 1; i >= 0; i--) {
            GameEntity he = features.getItem(i);

            if (he instanceof MapObject) {
                MapObject mf = (MapObject) he;

                if (mf.IsNeedUpdate) {
                    mf.update(time);
                    
                    if (mf.IsFinished) {
                        features.delete(i);
                    }
                }
            }
        }
    }

    @Override
    public void update(long time)
    {
        if (this.fGameSpace != null) {
            switch (this.fMainScreen) {
                case gsMain:
                    if (this.fGameState == GameState.gsDefault) {
                        this.updateFeatures(time);
                    }
                    break;

                case gsSwirl:
                    break;
            }
        }

        super.update(time);
    }

    private void doUserAction(Keys key, ShiftStates shift)
    {
        try {
            for (int ua = UserAction.uaFirst; ua <= UserAction.uaLast; ua++) {
                UserAction uAct = UserAction.forValue(ua);
                
                ShiftStates hkShift;
                if (uAct.WithoutShift) {
                    hkShift = new ShiftStates();
                } else {
                    hkShift = shift;
                }

                HotKey hkAct = uAct.HotKey;

                if (hkAct.Key == key && hkAct.Shift.equals(hkShift)) {
                    this.doEvent(uAct.Event, null, null, shift);
                    return;
                }
            }

            if (GlobalVars.Debug_DevMode) {
                this.showTextAux("Key: " + String.valueOf(key.getValue()));
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.doUserAction(): " + ex.getMessage());
            throw ex;
        }
    }

    private void doPlayerMove(int moveDir, ShiftStates shift, boolean trans)
    {
        if (this.getStyle() == NWMainWindow.RGS_MODERN & trans) {
            moveDir = Directions.IsoTrans[moveDir];
        }

        if ((shift.contains(ShiftStates.ssCtrl))) {
            this.fGameSpace.doPlayerAction(CreatureAction.caAttackMelee, moveDir);
        } else {
            if ((shift.contains(ShiftStates.ssAlt))) {
                this.fGameSpace.doPlayerAction(CreatureAction.caAttackShoot, moveDir);
            } else {
                this.fGameSpace.doPlayerAction(CreatureAction.caMove, moveDir);
            }
        }
    }

    private void OnLinkClick(Object sender, String linkValue)
    {
        this.showKnowledge(linkValue);
    }

    private void OptionsDone()
    {
        try {
            Properties saveProps = new Properties();

            saveProps.setProperty("Common/SongsVolume", String.valueOf(this.fSongsVolume));
            saveProps.setProperty("Common/SoundsVolume", String.valueOf(this.fSoundsVolume));
            saveProps.setProperty("Common/HideLocMap", String.valueOf(this.fHideLocMap));
            saveProps.setProperty("Common/HideCtlPanel", String.valueOf(this.fHideCtlPanel));
            saveProps.setProperty("Common/HideInfoPanel", String.valueOf(this.fHideInfoPanel));
            saveProps.setProperty("Common/Style", String.valueOf(super.getStyle()));
            saveProps.setProperty("Common/InventoryOnlyIcons", String.valueOf(this.fInventoryOnlyIcons));
            saveProps.setProperty("Common/CircularFOV", String.valueOf(this.fCircularFOV));
            saveProps.setProperty("Common/AutoPickup", String.valueOf(this.fAutoPickup));
            saveProps.setProperty("Common/ExtremeMode", String.valueOf(this.fExtremeMode));
            saveProps.setProperty("Common/Language", this.fLanguage);

            for (int ua = UserAction.uaFirst; ua <= UserAction.uaLast; ua++) {
                UserAction uAct = UserAction.forValue(ua);
                saveProps.setProperty("Keys/"+uAct.Sign, BaseMainWindow.HotKeyToText(uAct.HotKey));
            }

            saveProps.storeToXML(new FileOutputStream(ResourceManager.getAppPath() + StaticData.OPTIONS_FILE), "");
        } catch (IOException ex) {
            Logger.write("NWMainWindow.OptionsDone.io(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("NWMainWindow.OptionsDone(): " + ex.getMessage());
        }
    }

    private void OptionsInit()
    {
        try {
            Properties loadProps = new Properties();

            String filename = ResourceManager.getAppPath() + StaticData.OPTIONS_FILE;
            if ((new java.io.File(filename)).isFile()) {
                loadProps.loadFromXML(new FileInputStream(filename));
            }

            this.setSongsVolume(Integer.valueOf(loadProps.getProperty("Common/SongsVolume", "255")));
            this.setSoundsVolume(Integer.valueOf(loadProps.getProperty("Common/SoundsVolume", "255")));
            this.setHideLocMap(Boolean.valueOf(loadProps.getProperty("Common/HideLocMap", "false")));
            this.setHideCtlPanel(Boolean.valueOf(loadProps.getProperty("Common/HideCtlPanel", "false")));
            this.setHideInfoPanel(Boolean.valueOf(loadProps.getProperty("Common/HideInfoPanel", "false")));
            this.setStyle(Integer.valueOf(loadProps.getProperty("Common/Style", "0")));
            this.setInventoryOnlyIcons(Boolean.valueOf(loadProps.getProperty("Common/InventoryOnlyIcons", "false")));
            this.fCircularFOV = Boolean.valueOf(loadProps.getProperty("Common/CircularFOV", "true"));
            this.fAutoPickup = Boolean.valueOf(loadProps.getProperty("Common/AutoPickup", "false"));
            this.fExtremeMode = Boolean.valueOf(loadProps.getProperty("Common/ExtremeMode", "false"));
            this.setLanguage(loadProps.getProperty("Common/Language", "English"));

            for (int ua = UserAction.uaFirst; ua <= UserAction.uaLast; ua++) {
                UserAction uAct = UserAction.forValue(ua);

                String hk = loadProps.getProperty("Keys/" + uAct.Sign, "?");
                if (!TextUtils.equals(hk, "?")) {
                    uAct.HotKey = BaseMainWindow.TextToHotKey(hk);
                }
            }
        } catch (IOException ex) {
            Logger.write("NWMainWindow.OptionsInit.io(): " + ex.getMessage());
        } catch (Exception ex) {
            Logger.write("NWMainWindow.OptionsInit(): " + ex.getMessage());
        }        
    }

    public final void setAutoPickup(boolean Value)
    {
        if (this.fAutoPickup != Value) {
            this.fAutoPickup = Value;
        }
    }

    public final void setCircularFOV(boolean Value)
    {
        if (this.fCircularFOV != Value) {
            this.fCircularFOV = Value;
        }
    }

    public final void setExtremeMode(boolean Value)
    {
        if (this.fExtremeMode != Value) {
            this.fExtremeMode = Value;
        }
    }

    public final void setLanguage(String Value)
    {
        if (!TextUtils.equals(this.fLanguage, Value) && this.fLocale.setLang(Value)) {
            this.fLanguage = Value;
            this.changeLang();
        }
    }

    public final void setSongsVolume(int value)
    {
        this.fSongsVolume = value;
        this.fSoundEngine.sfxSetVolume(this.fSongsVolume, SoundEngine.sk_Song);
    }

    public final void setSoundsVolume(int value)
    {
        this.fSoundsVolume = value;
        this.fSoundEngine.sfxSetVolume(this.fSoundsVolume, SoundEngine.sk_Ambient);
        this.fSoundEngine.sfxSetVolume(this.fSoundsVolume, SoundEngine.sk_Sound);
    }

    private void updateView()
    {
        this.fTextBox.setBounds(StaticData.TV_Rect);
        if (this.fHideCtlPanel) {
            if (this.fHideLocMap) {
                this.IP_Top = 14;
            } else {
                this.IP_Top = -2;
            }
        } else {
            this.IP_Top = 150;
        }
        this.IP_Left = 640;
        this.IP_Right = 792;

        int LV_Y;
        int LV_YR;
        int MV_X;
        int MV_Y;
        int LV_X;
        int LV_XR;
        if (this.fHideLocMap) {
            LV_Y = 22;
            LV_YR = 7;
            MV_X = 8;
            MV_Y = 8;
            if (this.fHideCtlPanel && this.fHideInfoPanel) {
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
            if (!this.fHideCtlPanel && !this.fHideInfoPanel) {
                LV_X = 16;
                LV_XR = 9;
            } else {
                LV_X = 32;
                LV_XR = 11;
            }
            if (this.fHideCtlPanel && this.fHideInfoPanel) {
                MV_X = 88;
            } else {
                MV_X = 8;
            }
        }
        int LV_W = (LV_XR << 1) + 1 << 5;
        int LV_H = ((LV_YR << 1) + 1) * 30;
        this.FLV_Rect = new Rect(LV_X, LV_Y, LV_X + LV_W, LV_Y + LV_H);
        this.FMV_Rect = new Rect(MV_X, MV_Y, MV_X + 624, MV_Y + 200);
        this.FLocMapSize = new Point(LV_XR, LV_YR);
        if (this.getStyle() == NWMainWindow.RGS_MODERN) {
            this.fViewer.BufferResize(this.FLV_Rect);
        }

        for (int mc = MainControl.mbMenu.getValue(); mc <= MainControl.mbPack.getValue(); mc++) {
            MainControl mCtl = MainControl.forValue(mc);
            mCtl.Button.setVisible((this.fMainScreen == GameScreen.gsMain && !this.fHideCtlPanel));
        }
    }

    private void drawGameView(BaseScreen screen, NWField field, Player player)
    {
        if (this.DialogVisibleRefs > 0) {
            return;
        }

        try {
            NWField currentField;
            if (player.isHallucinations()) {
                currentField = player.HalMap;
            } else {
                currentField = field;
            }

            Rect tempRect;
            if (!this.fHideLocMap) {
                final Rect mapRect = new Rect(-1, -1, StaticData.FieldWidth, StaticData.FieldHeight);
                screen.fillRect(this.FMV_Rect, BaseScreen.clBlack);

                tempRect = this.FMV_Rect.clone();
                tempRect.inflate(2, 2);
                CtlCommon.drawCtlBorder(screen, tempRect);

                for (int py = mapRect.Top; py <= mapRect.Bottom; py++) {
                    for (int px = mapRect.Left; px <= mapRect.Right; px++) {
                        Painter.drawSymTile(this.fGameSpace, currentField, px, py, screen, mapRect, this.FMV_Rect, this.Symbols);
                    }
                }

                this.drawItems(screen, field, NWMainWindow.OPM_FIELD);

                screen.Font = CtlCommon.symFont;
                this.drawCreatures(screen, field, NWMainWindow.OPM_FIELD);

                screen.Font = CtlCommon.smFont;
                if (GlobalVars.Debug_CheckCrPtr) {
                    for (int py = mapRect.Top; py <= mapRect.Bottom; py++) {
                        for (int px = mapRect.Left; px <= mapRect.Right; px++) {
                            if (field.findCreature(px, py) != null) {
                                int xx = this.FMV_Rect.Left + 8 * (px + 1);
                                int yy = this.FMV_Rect.Top + 10 * (py + 1);
                                screen.drawRectangle(new Rect(xx, yy, xx + 4, yy + 4), BaseScreen.clYellow, BaseScreen.clRed);
                            }
                        }
                    }
                }

                if (GlobalVars.Debug_CheckScent) {
                    for (int py = mapRect.Top; py <= mapRect.Bottom; py++) {
                        for (int px = mapRect.Left; px <= mapRect.Right; px++) {
                            NWTile place = field.getTile(px, py);
                            if (place != null && place.ScentTrail != null) {
                                int xx = this.FMV_Rect.Left + 8 * (px + 1);
                                int yy = this.FMV_Rect.Top + 10 * (py + 1);
                                screen.drawRectangle(new Rect(xx, yy, xx + 4, yy + 4), BaseScreen.clYellow, BaseScreen.RGB((140 + (int) place.ScentAge), 0, 0));
                            }
                        }
                    }
                }

                if (this.fTargetData != null && this.fCursorValid && mapRect.contains(this.fMouseMapX, this.fMouseMapY)) {
                    int xx = this.FMV_Rect.Left + 8 * (this.fMouseMapX + 1);
                    int yy = this.FMV_Rect.Top + 10 * (this.fMouseMapY + 1);
                    this.Symbols.drawImage(screen, xx, yy, SymbolID.sid_Cursor.ImageIndex, 255);
                }
            }

            tempRect = this.FLV_Rect.clone();
            tempRect.inflate(2, 2);
            CtlCommon.drawCtlBorder(screen, tempRect);

            if (this.getStyle() == NWMainWindow.RGS_MODERN) {
                this.fViewer.Map = currentField;
                this.fViewer.CenterByTile(player.getPosX(), player.getPosY());
                this.fViewer.BufferPaint(screen, this.FLV_Rect.Left, this.FLV_Rect.Top);
            } else {
                final Rect mapRect = this.getLocMapRect();
                screen.fillRect(this.FLV_Rect, BaseScreen.clBlack);

                for (int py = mapRect.Top; py <= mapRect.Bottom; py++) {
                    for (int px = mapRect.Left; px <= mapRect.Right; px++) {
                        Painter.drawLocTile(this.fGameSpace, currentField, px, py, screen, player, mapRect, this.FLV_Rect, this.Resources);
                    }
                }

                this.drawItems(screen, field, NWMainWindow.OPM_LOCAL);

                this.drawCreatures(screen, field, NWMainWindow.OPM_LOCAL);

                this.drawFeatures(screen, field, mapRect);

                if (this.fTargetData != null && this.fCursorValid && mapRect.contains(this.fMouseMapX, this.fMouseMapY)) {
                    ItfElement itf = Painter.ptGetCursor(this.fTargetData.Target, player, this.fMouseMapX, this.fMouseMapY);
                    int xx = this.FLV_Rect.Left + 32 * (this.fMouseMapX - mapRect.Left);
                    int yy = this.FLV_Rect.Top + 30 * (this.fMouseMapY - mapRect.Top);
                    this.Resources.drawImage(screen, xx, yy, itf.ImageIndex, 255);
                }
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.drawGameView(): " + ex.getMessage());
        }
    }

    private void setScreenInternal(boolean active)
    {
        switch (this.fMainScreen) {
            case gsVillage: {
                if (active) {
                    this.showText(this, Locale.getStr(RS.rs_VillageMsg), new LogFeatures(LogFeatures.lfDialog));
                }
                break;
            }
            case gsForest: {
                if (active) {
                    this.showText(this, Locale.getStr(RS.rs_ForestMsg), new LogFeatures(LogFeatures.lfDialog));
                }
                break;
            }
            case gsMain: {
                this.fTextBox.setVisible(active);
                break;
            }
            case gsDefeat: {
                if (active) {
                    this.showText(this, Locale.getStr(RS.rs_DefeatMsg), new LogFeatures(LogFeatures.lfDialog));
                }
                break;
            }
            case gsVictory: {
                if (active) {
                    this.showText(this, Locale.getStr(RS.rs_VictoryMsg), new LogFeatures(LogFeatures.lfDialog));
                }
                break;
            }
        }
    }

    @Override
    protected void AppCreate()
    {
        try {
            this.fGameState = GameState.gsAppBusy;

            Logger.init(ResourceManager.getAppPath() + "Ragnarok.log");
            
            this.fLocale = new Locale();

            ResourceManager.load();

            this.fGameSpace = new NWGameSpace(this);

            super.setCursor(ResourceManager.loadImage(super.getScreen(), "itf/CursorMain.tga", BaseScreen.clBlack));
            super.setSysCursorVisible(false);

            CtlCommon.initCtlImages(super.getScreen());
            CtlCommon.smFont = super.getScreen().createFont("itf/rsf.gef");
            CtlCommon.bgFont = super.getScreen().createFont("itf/rbf.gef");
            CtlCommon.symFont = super.getScreen().createFont("itf/rsymfont.gef");

            this.fSoundEngine.sfxInit(0);

            this.fScrImage = null;
            super.setFont(CtlCommon.smFont);

            this.fViewer = new IsoViewer(null);
            this.fViewer.setTileWidth(64);
            this.fViewer.setTileHeight(32);
            this.fViewer.OnTilePaint = this::ViewerTilePaint;

            this.fMainScreen = GameScreen.gsNone;

            this.fTextBox = new TextBox(this);
            this.fTextBox.setVisible(false);
            this.fTextBox.LastVisible = true;
            this.fTextBox.setLinks(true);
            this.fTextBox.OnLinkClick = this::OnLinkClick;
            this.fTextBox.setLineHeight(18);

            this.fStartupDialog = new StartupWindow(this);
            this.fMenuDialog = new MenuWindow(this);
            this.fNPCWindow = new NPCWindow(this);
            this.fMessageWindow = new MessageWindow(this);
            this.fConsoleWindow = new ConsoleWindow(this);
            this.fOptions = new OptionsWindow(this);
            this.fMapDialog = new MapWindow(this);
            this.fFilesWindow = new FilesWindow(this);
            this.fKnowledgesWindow = new KnowledgesWindow(this);
            this.fSkillsWindow = new SkillsWindow(this);
            this.fSelfWindow = new SelfWindow(this);
            this.fInventoryWindow = new InventoryWindow(this);
            this.fHeroWindow = new HeroWindow(this);
            this.fIntroDialog = new IntroWindow(this);
            this.fAboutDialog = new AboutWindow(this);
            this.fInputWindow = new InputWindow(this);
            this.fProgressWindow = new ProgressWindow(this);
            this.fAlchemyWindow = new AlchemyWindow(this);
            this.fExchangeWindow = new ExchangeWindow(this);
            this.fTeachWindow = new TeachWindow(this);
            this.fRecruitWindow = new RecruitWindow(this);
            this.fSmithyWindow = new SmithyWindow(this);
            this.fPartyWindow = new PartyWindow(this);
            this.fJournalWindow = new JournalWindow(this);
            this.fDivinationWindow = new DivinationWindow(this);

            for (int mb = MainControl.mbFirst; mb <= MainControl.mbLast; mb++) {
                MainControl mCtl = MainControl.forValue(mb);

                CustomButton button = new NWButton(this);
                button.setImageFile(mCtl.ImageFile);
                button.OnLangChange = this::LangChange;
                button.setLangResID(mCtl.NameRS);
                button.setLeft(mCtl.R.Left);
                button.setTop(mCtl.R.Top);
                button.setWidth(mCtl.R.Right - mCtl.R.Left);
                button.setHeight(mCtl.R.Bottom - mCtl.R.Top);
                button.OnClick = this::MainButtonClick;
                button.setVisible(false);
                button.Tag = (int) mb;

                mCtl.Button = button;
            }

            this.OptionsInit();
            this.doEvent(EventID.event_Startup, null, null, null);
            this.AppInit();
            this.updateView();
            this.showStartupWin();
            this.fGameState = GameState.gsDefault;
        } catch (Exception ex) {
            Logger.write("NWMainWindow.AppCreate(): " + ex.getMessage());
            throw ex;
        }
    }

    @Override
    protected void AppDestroy()
    {
        try {
            this.fGameState = GameState.gsAppBusy;
            this.AppDone();
            this.fPartyWindow.dispose();
            this.fSmithyWindow.dispose();
            this.fExchangeWindow.dispose();
            this.fTeachWindow.dispose();
            this.fRecruitWindow.dispose();
            this.fAlchemyWindow.dispose();
            this.fProgressWindow.dispose();
            this.fInputWindow.dispose();
            this.fAboutDialog.dispose();
            this.fIntroDialog.dispose();
            this.fHeroWindow.dispose();
            this.fInventoryWindow.dispose();
            this.fSelfWindow.dispose();
            this.fSkillsWindow.dispose();
            this.fKnowledgesWindow.dispose();
            this.fFilesWindow.dispose();
            this.fMapDialog.dispose();
            this.fOptions.dispose();
            this.fConsoleWindow.dispose();
            this.fNPCWindow.dispose();
            this.fMessageWindow.dispose();
            this.fMenuDialog.dispose();
            this.fStartupDialog.dispose();
            this.fTextBox.dispose();

            this.fViewer = null;

            this.fScrImage.dispose();
            this.fScrImage = null;

            this.fSoundEngine.sfxDone();
            CtlCommon.symFont.dispose();
            CtlCommon.smFont.dispose();
            CtlCommon.bgFont.dispose();
            CtlCommon.doneCtlImages();
            super.getCursor().dispose();
            this.OptionsDone();

            this.fGameSpace.dispose();
            this.fGameSpace = null;

            ResourceManager.close();

            this.fLocale.dispose();
        } catch (Exception ex) {
            Logger.write("NWMainWindow.AppDestroy(): " + ex.getMessage());
        }
        Logger.done();
    }

    public final void testSound(Keys Key)
    {
        switch (Key) {
            case GK_F5:
                this.fSoundEngine.sfxSetReverb(SoundEngine.Reverb.Cave);
                break;

            case GK_F6:
                this.fSoundEngine.sfxSetReverb(SoundEngine.Reverb.Room);
                break;

            case GK_F7:
                this.fSoundEngine.sfxSetReverb(SoundEngine.Reverb.Arena);
                break;

            case GK_F8:
                this.fSoundEngine.sfxSetReverb(SoundEngine.Reverb.CHall);
                break;

            case GK_F9:
                this.fSoundEngine.sfxSetReverb(SoundEngine.Reverb.Quarry);
                break;

            case GK_F10:
                this.fSoundEngine.sfxSetReverb(SoundEngine.Reverb.Plain);
                break;
        }
    }

    @Override
    protected void doKeyDownEvent(KeyEventArgs eventArgs)
    {
        try {
            super.doKeyDownEvent(eventArgs);

            switch (this.fMainScreen) {
                case gsStartup:
                    break;

                case gsMain:
                    switch (eventArgs.Key) {
                        case GK_ESCAPE:
                            if (this.fTargetData != null) {
                                this.fTargetData.dispose();
                                this.fTargetData = null;
                            }
                            break;

                        case GK_TWIDDLE:
                            this.fConsoleWindow.show();
                            break;

                        case GK_F11:
                            /*NWField fld = this.fGameSpace.getPlayer().getCurrentField();
                            UniverseBuilder.build_Vanaheim(fld);
                            fld.research(true, new TileStates(TileStates.tsSeen, TileStates.tsVisited));*/
                            break;

                        default:
                            this.doUserAction(eventArgs.Key, eventArgs.Shift);
                            break;
                    }
                    break;

                case gsDead:
                    this.doEvent(EventID.event_Startup, null, null, null);
                    this.showStartupWin();
                    break;

                default:
                    this.setScreen(GameScreen.gsMain);
                    break;
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.doKeyDownEvent(): " + ex.getMessage());
        }
    }

    @Override
    protected void doMouseDownEvent(MouseEventArgs eventArgs)
    {
        try {
            super.doMouseDownEvent(eventArgs);

            switch (this.fMainScreen) {
                case gsVillage:
                case gsForest:
                case gsJotenheim:
                case gsNidavell:
                case gsNiflheim:
                case gsCrossroad:
                case gsDungeon:
                case gsSea:
                case gsAlfheim:
                case gsMuspelheim:
                case gsWell:
                case gsTemple:
                case gsWasteland:
                case gsSwirl: {
                    this.setScreen(GameScreen.gsMain);
                    break;
                }

                case gsDead:
                case gsDefeat:
                case gsVictory: {
                    this.doEvent(EventID.event_Startup, null, null, null);
                    this.showStartupWin();
                    break;
                }

                case gsMain: {
                    if ((this.FMV_Rect.contains(eventArgs.X, eventArgs.Y) && !this.fHideLocMap) || this.FLV_Rect.contains(eventArgs.X, eventArgs.Y)) {
                        if (eventArgs.Button != MouseButton.mbLeft) {
                            if (eventArgs.Button == MouseButton.mbRight) {
                                this.fGameSpace.showPlaceInfo(this.fMouseMapX, this.fMouseMapY, true);
                            }
                        } else {
                            this.clickTarget(eventArgs.Shift);
                        }
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.doMouseDownEvent(): " + ex.getMessage());
        }
    }

    @Override
    protected void doMouseMoveEvent(MouseMoveEventArgs eventArgs)
    {
        super.doMouseMoveEvent(eventArgs);

        this.fCursorValid = false;

        if (this.fMainScreen == GameScreen.gsMain) {
            if (this.FLV_Rect.contains(eventArgs.X, eventArgs.Y)) {
                if (this.getStyle() == NWMainWindow.RGS_MODERN) {
                    Point p = this.fViewer.TileByMouse(eventArgs.X - this.FLV_Rect.Left, eventArgs.Y - this.FLV_Rect.Top);
                    this.fMouseMapX = p.X;
                    this.fMouseMapY = p.Y;
                    this.fCursorValid = true;
                } else {
                    Rect R = this.getLocMapRect();
                    this.fMouseMapX = R.Left + (eventArgs.X - this.FLV_Rect.Left) / 32;
                    this.fMouseMapY = R.Top + (eventArgs.Y - this.FLV_Rect.Top) / 30;
                    this.fCursorValid = true;
                }
            } else {
                if (this.FMV_Rect.contains(eventArgs.X, eventArgs.Y) && !this.fHideLocMap) {
                    this.fMouseMapX = -1 + (eventArgs.X - this.FMV_Rect.Left) / 8;
                    this.fMouseMapY = -1 + (eventArgs.Y - this.FMV_Rect.Top) / 10;
                    this.fCursorValid = true;
                }
            }
        }
    }

    private void drawMainScreen(BaseScreen screen, NWField fld, Player player)
    {
        try {
            Rect crt = super.getClientRect();
            screen.drawFilled(crt, BaseScreen.FILL_TILE, 0, 0, CtlCommon.fWinBack.Width, CtlCommon.fWinBack.Height, 0, 0, CtlCommon.fWinBack);

            this.drawGameView(screen, fld, player);

            if (!this.fHideInfoPanel) {
                crt = MainControl.mgHP.R.clone();
                crt.Top += this.IP_Top;
                crt.Bottom += this.IP_Top;
                crt.Left = this.IP_Left;
                crt.Right = this.IP_Right;
                ProgressBar.drawGauge(screen, crt, player.HPCur, player.HPMax, BaseScreen.clBlack, BaseScreen.clGray, 1476417);

                crt = MainControl.mgMag.R.clone();
                crt.Top += this.IP_Top;
                crt.Bottom += this.IP_Top;
                crt.Left = this.IP_Left;
                crt.Right = this.IP_Right;
                ProgressBar.drawGauge(screen, crt, player.MPCur, player.MPMax, BaseScreen.clBlack, BaseScreen.clGray, 1476417);

                crt = MainControl.mgFood.R.clone();
                crt.Top += this.IP_Top;
                crt.Bottom += this.IP_Top;
                crt.Left = this.IP_Left;
                crt.Right = this.IP_Right;
                ProgressBar.drawGauge(screen, crt, (int) player.getSatiety(), Player.SatietyMax, BaseScreen.clBlack, BaseScreen.clGray, 1476417);

                screen.setTextColor(BaseScreen.clGold, true);
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 8, this.IP_Right, player.getLocationName(), "");
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 24, this.IP_Right, this.fGameSpace.getDayTimeInfo(), "");
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 40, this.IP_Right, Locale.getStr(RS.rs_Turn), String.valueOf(player.Turn));
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 56, this.IP_Right, Locale.getStr(RS.rs_HP), String.valueOf(player.HPCur) + " (" + String.valueOf(player.HPMax) + ")");
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 82, this.IP_Right, Locale.getStr(RS.rs_MP), String.valueOf(player.MPCur) + " (" + String.valueOf(player.MPMax) + ")");
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 108, this.IP_Right, Locale.getStr(RS.rs_Satiety), "");
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 134, this.IP_Right, Locale.getStr(RS.rs_Morality), player.getMoralityName());
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 150, this.IP_Right, Locale.getStr(RS.rs_Armor), String.valueOf(player.ArmorClass));
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 166, this.IP_Right, Locale.getStr(RS.rs_Damage), String.valueOf(player.DBMin) + "-" + String.valueOf(player.DBMax));
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 182, this.IP_Right, Locale.getStr(RS.rs_ToHit), String.valueOf(player.ToHit) + "%");
                Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 198, this.IP_Right, Locale.getStr(RS.rs_Speed), String.valueOf(player.getSpeed()));

                if (!this.fHideCtlPanel || (this.fHideCtlPanel && this.fHideLocMap)) {
                    Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 214, this.IP_Right, Locale.getStr(RS.rs_Luck), String.valueOf(player.Luck));
                    Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 230, this.IP_Right, Locale.getStr(RS.rs_Strength), String.valueOf(player.Strength));
                    Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 246, this.IP_Right, Locale.getStr(RS.rs_Constitution), String.valueOf(player.Constitution));
                    Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 262, this.IP_Right, Locale.getStr(RS.rs_Experience), String.valueOf(player.getExperience()));
                    Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 278, this.IP_Right, Locale.getStr(RS.rs_Level), String.valueOf(player.Level));
                    Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 294, this.IP_Right, Locale.getStr(RS.rs_Dexterity), String.valueOf(player.Dexterity));
                }

                if (this.fHideCtlPanel && this.fHideLocMap) {
                    Painter.drawProperty(screen, this.IP_Left, this.IP_Top + 326, this.IP_Right, Locale.getStr(RS.rs_Perception), String.valueOf(player.Perception));
                }
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.drawMainScreen(): " + ex.getMessage());
        }
    }
    
    @Override
    protected void doPaintEvent(BaseScreen screen)
    {
        try {
            if (this.fGameSpace == null) return;

            Player player = this.fGameSpace.getPlayer();
            if (player == null) return;

            NWField fld = (NWField) player.getCurrentMap();
            if (this.fScrImage != null && this.fMainScreen.gfx.compareTo("") != 0) {
                screen.drawImage(0, 0, 0, 0, (int) this.fScrImage.Width, (int) this.fScrImage.Height, this.fScrImage, 255);
            }

            if (CtlCommon.smFont == null) {
                return;
            }

            switch (this.fMainScreen) {
                case gsStartup:
                    int ats = 550;
                    screen.setTextColor(BaseScreen.clGold, true);
                    screen.drawText(8, ats, "Valhalla, Version 1.0, Copyright (c) 1992 by Norsehelm Productions", 0);
                    screen.drawText(8, ats + 15, "Ragnarok, Version 2.5V, Copyright (c) 1992-1995 by Norsehelm Productions", 0);
                    screen.drawText(8, ats + 30, (StaticData.rs_GameName + ", " + StaticData.rs_GameVersion + ", " + StaticData.rs_GameCopyright), 0);
                    break;

                case gsDead:
                    screen.setTextColor(BaseScreen.clRed, true);
                    int num = this.fScoresList.getScoreCount();
                    for (int i = 0; i < num; i++) {
                        int atd = 20 + i * 18;
                        Score sr = this.fScoresList.getScore(i);
                        screen.drawText(8, atd, String.valueOf(sr.Exp), 0);
                        screen.drawText(100, atd, String.valueOf(sr.Level), 0);
                        screen.drawText(140, atd, sr.Name, 0);
                        screen.drawText(300, atd, sr.Desc, 0);
                    }
                    break;

                case gsMain:
                    if (this.fGameState == GameState.gsDefault) {
                        this.drawMainScreen(screen, fld, player);
                    }
                    break;
            }

            if (GlobalVars.Debug_ShowFPS) {
                screen.drawText(5, 5, "FPS: " + String.valueOf(super.FPS), 0);
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.doPaintEvent(): " + ex.getMessage());
        }
    }

    @Override
    protected void doStyleChanged()
    {
        this.setScreen(this.fMainScreen);
        if (this.fGameState == GameState.gsDefault) {
            this.fGameState = GameState.gsResLoad;
            if (this.fOldStyle != super.getStyle()) {
                this.AppDone();
                this.AppInit();
            }
            this.fGameState = GameState.gsDefault;
        }
    }

    @Override
    protected BaseHintWindow createHintWindow(BaseMainWindow owner)
    {
        return new HintWindow(owner);
    }

    public final void clear()
    {
        if (this.fTargetData != null) {
            this.fTargetData.dispose();
            this.fTargetData = null;
        }

        this.fTextBox.clear();

        for (int gs = GameScreen.gsFirst; gs <= GameScreen.gsLast; gs++) {
            GameScreen scr = GameScreen.forValue(gs);
            
            if (scr.status == ScreenStatus.ssAlready) {
                scr.status = ScreenStatus.ssOnce;
            }
        }
    }

    public final boolean showNPCDialog(NWCreature collocutor)
    {
        boolean result = false;
        if (collocutor == null) {
            this.showText(this, Locale.getStr(RS.rs_HereNobody));
        } else {
            if (!(collocutor.getBrain() instanceof SentientBrain)) {
                this.showText(this, Locale.getStr(RS.rs_ThisNotCollocutor));
            } else {
                this.fNPCWindow.setCollocutor(collocutor);
                this.fNPCWindow.show();
                result = true;
            }
        }
        return result;
    }

    private void doCreatureEvent(EventID eventID, Object sender, Object extData)
    {
        Player player = this.fGameSpace.getPlayer();

        String sAct = "";
        switch (eventID) {
            case event_Attack: {
                sAct = "Attack";
                break;
            }
            case event_Killed: {
                sAct = "Killed";
                break;
            }
            case event_Move: {
                sAct = "Move";
                break;
            }
            case event_Shot: {
                sAct = "Shot";
                break;
            }
            case event_Slay: {
                sAct = "Slay";
                break;
            }
            case event_Wounded: {
                sAct = "Wounded";
                break;
            }
        }

        NWCreature creat = (NWCreature) sender;
        String sMonster = creat.getEntry().sfx;

        if (sMonster.compareTo("") != 0) {
            this.playSound("creatures\\" + sMonster + "_" + sAct + ".ogg", SoundEngine.sk_Sound, creat.getPosX(), creat.getPosY());
        }

        NWField crField = creat.getCurrentField();
        if (!creat.isPlayer() && crField != null && player.getCurrentMap().equals(crField)) {
            int dist = AuxUtils.distance(creat.getLocation(), player.getLocation());
            if (dist <= (int) player.Hear && AuxUtils.chance(5) && crField.LandID != GlobalVars.Land_Village) {
                this.showText(player, Locale.getStr(RS.rs_YouHearNoise));
            }
        }
    }

    private void doItemEvent(EventID eventID, Object sender, Object extData)
    {
        Item item = (Item) extData;

        int id = item.CLSID;
        String itKind = "";
        MaterialKind matKind = item.getMaterial();
        switch (item.getKind()) {
            case ik_Armor:
            case ik_HeavyArmor:
            case ik_MediumArmor:
            case ik_LightArmor:
                if (matKind == MaterialKind.mk_Leather) {
                    itKind = "Leather";
                } else {
                    itKind = "Armor";
                }
                break;

            case ik_DeadBody:
            case ik_Food:
                itKind = "Food";
                break;

            case ik_Potion:
                itKind = "Potion";
                break;

            case ik_Ring:
                itKind = "Ring";
                break;

            case ik_Tool:
                if (id == GlobalVars.iid_Vial || id == GlobalVars.iid_Flask) {
                    itKind = "Bottle";
                } else if (id == GlobalVars.iid_Stylus) {
                    itKind = "Stylus";
                }
                break;

            case ik_Wand:
            case ik_Spear:
            case ik_Axe:
            case ik_Bow:
            case ik_CrossBow:
            case ik_Projectile:
                if (item.getFlags().contains(ItemFlags.if_Projectile)) {
                    itKind = "Quiver";
                } else {
                    if (item.CLSID == GlobalVars.iid_LongBow) {
                        itKind = "Bow";
                    } else {
                        if (item.CLSID == GlobalVars.iid_CrossBow) {
                            itKind = "Crossbow";
                        }
                    }
                }
                break;

            case ik_BluntWeapon:
                itKind = "Mace";
                break;

            case ik_Scroll:
                itKind = "Scroll";
                break;

            case ik_Coin:
                itKind = "Coin";
                break;

            case ik_ShortBlade:
                itKind = "Knife";
                break;

            case ik_LongBlade:
                itKind = "Sword";
                break;

            case ik_Shield:
                if (eventID.getValue() < EventID.event_ItemRemove.getValue() || eventID.getValue() >= EventID.event_ItemBreak.getValue()) {
                    itKind = "Armor";
                } else {
                    itKind = "Shield";
                }
                break;

            case ik_Helmet:
                if (eventID.getValue() < EventID.event_ItemRemove.getValue() || eventID.getValue() >= EventID.event_ItemBreak.getValue()) {
                    itKind = "Armor";
                } else {
                    itKind = "Helmet";
                }
                break;

            case ik_Clothing:
                if (matKind == MaterialKind.mk_Leather) {
                    itKind = "Leather";
                } else {
                    itKind = "Clothing";
                }
                break;

            case ik_MusicalTool:
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

            case ik_Misc:
                // dummy
                break;
        }

        String sAct = "";
        switch (eventID) {
            case event_ItemDrop: {
                sAct = "Drop";
                break;
            }
            case event_ItemPickup: {
                sAct = "Pickup";
                break;
            }
            case event_ItemRemove: {
                sAct = "Remove";
                break;
            }
            case event_ItemWear: {
                sAct = "Wear";
                break;
            }
            case event_ItemBreak: {
                sAct = "Break";
                break;
            }
            case event_ItemUse: {
                sAct = "Use";
                break;
            }
            case event_ItemMix: {
                sAct = "Mix";
                break;
            }
        }

        if (itKind.compareTo("") != 0) {
            NWCreature creat = ((NWCreature) sender);
            this.playSound("items\\" + itKind + "_" + sAct + ".ogg", SoundEngine.sk_Sound, creat.getPosX(), creat.getPosY());
        }
    }

    public final void doEvent(EventID eventID, Object sender, Object receiver, Object extData)
    {
        try {
            if (eventID.getValue() < EventID.event_First || eventID.getValue() > EventID.event_Last) {
                return;
            }

            if (eventID.Flags.contains(EventFlags.efInQueue)) {
                this.fGameSpace.sendEvent(eventID, eventID.Priority, sender, receiver);
            }

            if (eventID.Flags.contains(EventFlags.efInJournal)) {
                String msg = this.fGameSpace.getEventMessage(eventID, sender, receiver, extData);
                this.showText(sender, msg);
            }

            Player player = this.fGameSpace.getPlayer();

            switch (eventID) {
                case event_Nothing: {
                    // dummy
                    break;
                }

                case event_Startup: {
                    this.setScreen(GameScreen.gsStartup);
                    this.playSound("startup.ogg", SoundEngine.sk_Song, -1, -1);
                    break;
                }

                case event_Map: {
                    this.playSound("map.ogg", SoundEngine.sk_Sound, -1, -1);
                    this.fMapDialog.show();
                    break;
                }

                case event_About: {
                    this.playSound("intro.ogg", SoundEngine.sk_Song, -1, -1);
                    this.fAboutDialog.show();
                    break;
                }

                case event_Self: {
                    this.playSound("self.ogg", SoundEngine.sk_Sound, -1, -1);
                    this.fSelfWindow.show();
                    break;
                }

                case event_Wait: {
                    this.fGameSpace.doPlayerAction(CreatureAction.caWait, 0);
                    break;
                }

                case event_PickupAll: {
                    NWField f = player.getCurrentField();
                    Building b = f.findBuilding(player.getPosX(), player.getPosY());
                    if (b != null && b.Holder != null) {
                        this.doEvent(EventID.event_Pack, null, null, null);
                    } else {
                        this.fGameSpace.doPlayerAction(CreatureAction.caPickupAll, 0);
                    }
                    break;
                }

                case event_DoorClose: {
                    Door door = (Door) sender;
                    this.playSound("doorClose.ogg", SoundEngine.sk_Sound, door.X, door.Y);
                    break;
                }

                case event_DoorOpen: {
                    Door door = (Door) sender;
                    this.playSound("doorOpen.ogg", SoundEngine.sk_Sound, door.X, door.Y);
                    break;
                }

                case event_Help: {
                    String helpFile = ResourceManager.getAppPath() + Locale.LANGS_FOLDER + this.getLangExt() + "_help.htm";
                    super.help(helpFile);
                    break;
                }

                case event_LandEnter: {
                    LandEntry eLand = (LandEntry) extData;
                    if (!TextUtils.equals(eLand.Song, "")) {
                        this.playSound(eLand.Song, SoundEngine.sk_Song, -1, -1);
                    }
                    if (eLand.BackSFX.compareTo("") != 0) {
                        this.playSound(eLand.BackSFX, SoundEngine.sk_Ambient, -1, -1);
                    }
                    GameScreen scr = eLand.Splash;
                    if (scr != GameScreen.gsNone && scr.status != ScreenStatus.ssAlready) {
                        if (scr.status == ScreenStatus.ssOnce) {
                            scr.status = ScreenStatus.ssAlready;
                        }
                    } else {
                        scr = GameScreen.gsMain;
                    }
                    this.setScreen(scr);
                    break;
                }

                case event_Intro:
                    this.playSound("intro.ogg", SoundEngine.sk_Song, -1, -1);
                    this.fIntroDialog.show();
                    break;

                case event_Knowledges:
                    if (this.fGameSpace.getPlayer().getMemory().getData().isEmpty()) {
                        this.showText(this.fGameSpace.getPlayer(), Locale.getStr(RS.rs_YouKnowNothing));
                    } else {
                        this.playSound("knowledges.ogg", SoundEngine.sk_Sound, -1, -1);
                        this.fKnowledgesWindow.show();
                    }
                    break;

                case event_Skills:
                    if (this.fGameSpace.getPlayer().getSkillsCount() == 0) {
                        this.showText(this.fGameSpace.getPlayer(), Locale.getStr(RS.rs_NoSkills));
                    } else {
                        this.playSound("skills.ogg", SoundEngine.sk_Sound, -1, -1);
                        this.fSkillsWindow.setMode(SkillsWindow.SWM_SKILLS);
                        this.fSkillsWindow.show();
                    }
                    break;

                case event_Menu:
                    this.fMenuDialog.show();
                    break;

                case event_Attack:
                case event_Killed:
                case event_Move:
                case event_Shot:
                case event_Slay:
                case event_Wounded: {
                    this.doCreatureEvent(eventID, sender, extData);
                    break;
                }

                case event_Trade:
                    this.playSound("pay.ogg", SoundEngine.sk_Sound, -1, -1);
                    break;

                case event_LevelUp:
                    this.playSound("levelUp.ogg", SoundEngine.sk_Sound, -1, -1);
                    this.showText(this, Locale.getStr(RS.rs_LevelUp) + String.valueOf(((NWCreature) sender).Level) + ".");
                    break;

                case event_Pack:
                    this.playSound("pack.ogg", SoundEngine.sk_Sound, -1, -1);
                    this.showInventory(null);
                    break;

                case event_Options:
                    this.fOptions.show();
                    break;

                case event_Defeat:
                    this.fScoresList.add(Score.KIND_DEFEAT, player.getName(), Locale.getStr(RS.rs_RagnarokDefeat), player.getExperience(), player.Level);
                    this.playSound("defeat.ogg", SoundEngine.sk_Sound, -1, -1);
                    break;

                case event_Dialog:
                    player.useEffect(EffectID.eid_Dialog, null, InvokeMode.im_ItSelf, null);
                    break;

                case event_Quit:
                    this.playSound("quit.ogg", SoundEngine.sk_Sound, -1, -1);
                    super.quit();
                    break;

                case event_Save:
                    this.fFilesWindow.FilesMode = FilesWindow.FWMODE_SAVE;
                    this.fFilesWindow.show();
                    break;

                case event_Load: {
                    this.fFilesWindow.FilesMode = FilesWindow.FWMODE_LOAD;
                    this.fFilesWindow.show();
                    break;
                }

                case event_New: {
                    this.playSound("game_new.ogg", SoundEngine.sk_Sound, -1, -1);
                    this.newGame_Start();
                    break;
                }

                case event_Dead: {
                    this.doPlayerDeathEvent();
                    this.setScreen(GameScreen.gsDead);
                    break;
                }

                case event_Party: {
                    this.fPartyWindow.show();
                    break;
                }

                case event_Victory: {
                    this.fScoresList.add(Score.KIND_VICTORY, player.getName(), Locale.getStr(RS.rs_RagnarokVictory), player.getExperience(), player.Level);
                    player.setName(Locale.getStr(RS.rs_Unknown));
                    this.playSound("victory.ogg", SoundEngine.sk_Sound, -1, -1);
                    this.setScreen(GameScreen.gsVictory);
                    break;
                }

                case event_EffectSound: {
                    GameEvent effectEvent = (GameEvent) extData;
                    int eid = effectEvent.CLSID;
                    this.playSound("effects\\" + EffectsData.dbEffects[eid].SFX, SoundEngine.sk_Sound, effectEvent.getPosX(), effectEvent.getPosY());
                    break;
                }

                case event_ItemDrop:
                case event_ItemPickup:
                case event_ItemRemove:
                case event_ItemWear:
                case event_ItemBreak:
                case event_ItemUse:
                case event_ItemMix: {
                    this.doItemEvent(eventID, sender, extData);
                    break;
                }

                case event_PlayerMoveN:
                case event_PlayerMoveS:
                case event_PlayerMoveW:
                case event_PlayerMoveE:
                case event_PlayerMoveNW:
                case event_PlayerMoveNE:
                case event_PlayerMoveSW:
                case event_PlayerMoveSE: {
                    int dir;
                    switch (eventID) {
                        case event_PlayerMoveN: {
                            dir = Directions.dtNorth;
                            break;
                        }
                        case event_PlayerMoveS: {
                            dir = Directions.dtSouth;
                            break;
                        }
                        case event_PlayerMoveW: {
                            dir = Directions.dtWest;
                            break;
                        }
                        case event_PlayerMoveE: {
                            dir = Directions.dtEast;
                            break;
                        }
                        case event_PlayerMoveNW: {
                            dir = Directions.dtNorthWest;
                            break;
                        }
                        case event_PlayerMoveNE: {
                            dir = Directions.dtNorthEast;
                            break;
                        }
                        case event_PlayerMoveSW: {
                            dir = Directions.dtSouthWest;
                            break;
                        }
                        case event_PlayerMoveSE: {
                            dir = Directions.dtSouthEast;
                            break;
                        }
                        default: {
                            dir = Directions.dtNone;
                            break;
                        }
                    }

                    ShiftStates sh = (ShiftStates) extData;
                    this.doPlayerMove(dir, sh, true);
                    break;
                }

                case event_PlayerMoveUp:
                case event_PlayerMoveDown: {
                    int dir = Directions.dtNone;
                    switch (eventID) {
                        case event_PlayerMoveUp:
                            dir = Directions.dtZenith;
                            break;
                        case event_PlayerMoveDown:
                            dir = Directions.dtNadir;
                            break;
                    }

                    ShiftStates sh = (ShiftStates) extData;
                    this.doPlayerMove(dir, sh, false);
                    break;
                }
                
                case event_Journal:
                    this.fJournalWindow.show();
                    break;
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.doEvent(): " + ex.getMessage());
            throw ex;
        }
    }

    private void doPlayerDeathEvent()
    {
        try {
            Player player = this.fGameSpace.getPlayer();
            
            Ghost ghost = player.createGhost();
            if (ghost != null) {
                this.fGhostsList.add(ghost);
            }

            String in_land = Locale.getStr(RS.rs_DeathIn);
            in_land += player.getCurrentField().getLandEntry().getNounDeclension(jzrlib.grammar.Number.nSingle, Case.cPrepositional);
            String deathReason = player.DeathReason;
            int p = deathReason.indexOf(".");
            if (p >= 0) {
                StringBuilder sb = new StringBuilder(deathReason);
                sb.insert(p, in_land);
                deathReason = sb.toString();
            } else {
                deathReason += in_land;
            }

            this.fScoresList.add(Score.KIND_DEATH, player.getName(), deathReason, player.getExperience(), player.Level);

            player.setName(Locale.getStr(RS.rs_Unknown));

            if (this.getExtremeMode() && this.fGameSpace.getFileIndex() >= 0) {
                this.fGameSpace.eraseGame(this.fGameSpace.getFileIndex());
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.doPlayerDeathEvent(): " + ex.getMessage());
            throw ex;
        }
    }
    
    public final void newGame_Start()
    {
        this.clear();

        this.fGameState = GameState.gsWorldGen;
        this.fGameSpace.InitBegin();

        this.fHeroWindow.show();
    }

    public final void newGame_Finish()
    {
        this.fGameSpace.InitEnd();
        this.fGameState = GameState.gsDefault;

        this.initScripts();
    }

    public final Object executeScript(String script)
    {
        try {
            this.initScripts();

            return fScriptEngine.eval(script);
        } catch (Exception ex) {
            Logger.write("NWMainWindow.executeScript(): " + ex.getMessage());
            return null;
        }
    }

    public final void setScriptVar(String var, Object value)
    {
        this.fScriptEngine.put(var, value);
    }

    private void initScripts()
    {
        Player player = this.fGameSpace.getPlayer();

        this.fScriptEngine.put("win", this);
        this.fScriptEngine.put("player", player);
        this.fScriptEngine.put("PC", player);
    }

    public final String getLangExt()
    {
        int i = this.fLocale.findLang(this.fLanguage);
        String result;
        if (i >= 0) {
            result = this.fLocale.getLang(i).Prefix;
        } else {
            result = "en";
        }
        return result;
    }

    public final void showDivination()
    {
        this.fDivinationWindow.show();
    }

    public final void showInventory(NWCreature collocutor)
    {
        if (collocutor == null) {
            Player player = this.fGameSpace.getPlayer();
            Building b = ((NWField) player.getCurrentMap()).findBuilding(player.getPosX(), player.getPosY());
            if (b != null) {
                collocutor = ((NWCreature) b.Holder);
            }
        }
        this.fInventoryWindow.Collocutor = collocutor;
        this.fInventoryWindow.show();
    }

    public final void hideInventory()
    {
        this.fInventoryWindow.hide();
    }

    public final void LangChange(Object sender)
    {
        BaseControl ctl = (BaseControl) sender;
        if (ctl.getLangResID() > -1) {
            ctl.setCaption(Locale.getStr(ctl.getLangResID()));
        }
    }

    @Override
    public void processGameStep()
    {
        if (this.fGameSpace != null) {
            this.fGameSpace.processGameStep();
        }
    }

    public final void ProgressInit(int stageCount)
    {
        this.fProgressWindow.setStageCount(stageCount);
        this.fProgressWindow.setStage(0);
        this.fProgressWindow.show();
    }

    public final void ProgressDone()
    {
        this.fProgressWindow.hide();
    }

    public final void ProgressStep()
    {
        this.fProgressWindow.step();
    }

    public final void ProgressLabel(String stageLabel)
    {
        this.fProgressWindow.StageLabel = stageLabel;
    }

    public final void setScreen(GameScreen value)
    {
        if (value != GameScreen.gsNone) {
            this.setScreenInternal(false);
            this.fMainScreen = value;
            this.setScreenInternal(true);

            this.updateView();
            String sf = value.gfx;
            if (sf.compareTo("") != 0) {
                sf = "screens/" + sf + ".tga";
                this.fScrImage = ResourceManager.loadImage(super.getScreen(), sf, BaseScreen.clNone);
            }
        }
    }

    public final void showAlchemyWin()
    {
        this.fAlchemyWindow.show();
    }

    public final void showExchangeWin(NWCreature aCollocutor)
    {
        this.fExchangeWindow.setCollocutor(aCollocutor);
        this.fExchangeWindow.show();
    }

    public final void showInput(String aCaption, IInputAcceptProc anAcceptProc)
    {
        this.fInputWindow.setCaption(aCaption);
        this.fInputWindow.setValue("");
        this.fInputWindow.AcceptProc = anAcceptProc;
        this.fInputWindow.show();
    }

    public final void showKnowledge(String aName)
    {
        this.fKnowledgesWindow.select(aName);
    }

    public final void showMessage(String aText)
    {
        this.fMessageWindow.setText(aText);
        this.fMessageWindow.show();
    }

    public final void showRecruit(NWCreature aCollocutor)
    {
        this.fRecruitWindow.setCollocutor(aCollocutor);
        this.fRecruitWindow.show();
    }

    public final void showSmithyWin()
    {
        this.fSmithyWindow.show();
    }

    public final void showStartupWin()
    {
        for (int i = GameScreen.gsFirst; i <= GameScreen.gsLast; i++) {
            GameScreen scr = GameScreen.forValue(i);
            if (scr.status == ScreenStatus.ssAlready) {
                scr.status = ScreenStatus.ssOnce;
            }
        }

        this.fStartupDialog.show();
    }

    public final void showTeachWin(NWCreature aCollocutor)
    {
        this.fTeachWindow.setCollocutor(aCollocutor);
        this.fTeachWindow.show();
    }

    public void showText(String text)
    {
        this.showText(this, text, new LogFeatures());
    }

    public final void showText(Object sender, String text)
    {
        this.showText(sender, text, new LogFeatures());
    }

    public final void showText(Object sender, String text, LogFeatures features)
    {
        try {
            if (sender != null && !TextUtils.isNullOrEmpty(text)) {
                boolean res = sender.equals(this) || sender.equals(this.fGameSpace.getPlayer()) || (sender instanceof NWCreature && this.fGameSpace.getPlayer().isAvailable((NWCreature) sender, true));
                if (res) {
                    String temp = text;
                    temp = TextUtils.upperFirst(temp);

                    String resText = "   " + temp;
                    this.fTextBox.getLines().add(resText);

                    if (!features.contains(LogFeatures.lfAux)) {
                        this.fGameSpace.getJournal().storeMessage(JournalItem.SIT_DEFAULT, resText);
                    }

                    if (features.contains(LogFeatures.lfDialog)) {
                        this.showMessage(text);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.write("NWMainWindow.showText(): " + ex.getMessage());
        }
    }

    public final void showTextAux(String text)
    {
        this.showText(this, text, new LogFeatures(LogFeatures.lfAux));
    }

    public final void showTextRes(Object sender, int textID, Object[] args)
    {
        String msg = this.fGameSpace.parseMessage(Locale.getStr(textID), args);
        this.showText(sender, msg, new LogFeatures());
    }

    public final void setSoundsReverb(SoundEngine.Reverb reverb)
    {
        this.fSoundEngine.sfxSetReverb(reverb);
    }

    public final void playSound(String fileName, int kind, int sX, int sY)
    {
        boolean res = true;

        if (this.fGameState != GameState.gsWorldGen) {
            if (kind == SoundEngine.sk_Sound && sX != -1 && sY != -1) {
                int dist = AuxUtils.distance(sX, sY, this.fGameSpace.getPlayer().getPosX(), this.fGameSpace.getPlayer().getPosY());
                res = (dist <= (int) this.fGameSpace.getPlayer().getSurvey());
            }

            if (res) {
                if (kind == SoundEngine.sk_Song) {
                    fileName = "songs/" + fileName;
                }
                fileName = "sfx/" + fileName;

                Point ppt = new Point();
                ppt.X = this.fGameSpace.getPlayer().getPosX();
                ppt.Y = this.fGameSpace.getPlayer().getPosY();

                Point spt = new Point();
                spt.X = sX;
                spt.Y = sY;
                this.fSoundEngine.sfxPlay(fileName, kind, ppt, spt);
            }
        }
    }

    private void clickTarget(ShiftStates shift)
    {
        Player player = this.fGameSpace.getPlayer();
        NWField fld = (NWField) player.getCurrentMap();

        if (this.fTargetData == null) {
            int dir = Directions.getDirByCoords(player.getPosX(), player.getPosY(), this.fMouseMapX, this.fMouseMapY);
            int dist = AuxUtils.distance(player.getPosX(), player.getPosY(), this.fMouseMapX, this.fMouseMapY);
            if (dir != Directions.dtNone && dist == 1) {
                this.doPlayerMove(dir, shift, false);
            } else {
                if (GlobalVars.Debug_Divinity) {
                    player.moveTo(this.fMouseMapX, this.fMouseMapY);
                    this.fGameSpace.setTurnState(TurnState.gtsDone);
                }
            }
        } else {
            EffectTarget eRes = EffectTarget.et_None;
            switch (this.fTargetData.Target) {
                case et_PlaceNear: {
                    if (player.isNear(new Point(this.fMouseMapX, this.fMouseMapY))) {
                        this.fTargetData.Ext.setParam(EffectParams.ep_Place, new Point(this.fMouseMapX, this.fMouseMapY));
                    } else {
                        eRes = EffectTarget.et_PlaceNear;
                    }
                    break;
                }

                case et_PlaceFar:
                    this.fTargetData.Ext.setParam(EffectParams.ep_Place, new Point(this.fMouseMapX, this.fMouseMapY));
                    break;

                case et_Direction: {
                    int dir = Directions.getDirByCoords(player.getPosX(), player.getPosY(), this.fMouseMapX, this.fMouseMapY);
                    if (dir == Directions.dtNone) {
                        eRes = EffectTarget.et_Direction;
                    } else {
                        this.fTargetData.Ext.setParam(EffectParams.ep_Direction, dir);
                    }
                    break;
                }

                case et_Item:
                    eRes = EffectTarget.et_Item;
                    break;

                case et_Creature: {
                    NWCreature cr = (NWCreature) fld.findCreature(this.fMouseMapX, this.fMouseMapY);
                    if (cr == null) {
                        eRes = EffectTarget.et_Creature;
                    } else {
                        this.fTargetData.Ext.setParam(EffectParams.ep_Creature, cr);
                    }
                    break;
                }
            }

            if (eRes == EffectTarget.et_None) {
                this.useTarget();
            } else {
                this.showTextAux(Locale.getStr(eRes.InvalidRS));
            }
        }
    }

    public final void initTarget(EffectID effectID, Object source, InvokeMode invokeMode, EffectExt ext)
    {
        if (this.fTargetData != null) {
            this.fTargetData.dispose();
            this.fTargetData = null;
        }

        this.fTargetData = new TargetObj();
        this.fTargetData.EffID = effectID;
        this.fTargetData.Source = source;
        this.fTargetData.InvMode = invokeMode;
        this.fTargetData.Target = EffectTarget.et_None;
        this.fTargetData.Ext = ext;
        this.useTarget();
    }

    public final void useTarget()
    {
        EffectExt ext = this.fTargetData.Ext;

        for (int ep = EffectParams.ep_First; ep <= EffectParams.ep_Last; ep++) {
            if (ext.isRequire(ep)) {
                switch (ep) {
                    case EffectParams.ep_Place:
                        this.fTargetData.Target = EffectTarget.et_PlaceFar;
                        this.showTextAux(Locale.getStr(EffectTarget.et_PlaceNear.QuestionRS));
                        return;

                    case EffectParams.ep_Direction:
                        this.fTargetData.Target = EffectTarget.et_Direction;
                        this.showTextAux(Locale.getStr(EffectTarget.et_Direction.QuestionRS));
                        return;

                    case EffectParams.ep_Item:
                        this.fTargetData.Target = EffectTarget.et_Item;
                        this.showTextAux(Locale.getStr(EffectTarget.et_Item.QuestionRS));
                        this.showInventory(null);
                        return;

                    case EffectParams.ep_Creature:
                        this.fTargetData.Target = EffectTarget.et_Creature;
                        this.showTextAux(Locale.getStr(EffectTarget.et_Creature.QuestionRS));
                        return;

                    case EffectParams.ep_Area:
                        // dummy
                        break;

                    case EffectParams.ep_Land:
                        this.fSkillsWindow.setMode(SkillsWindow.SWM_LANDS);
                        this.fSkillsWindow.show();
                        return;

                    case EffectParams.ep_DunRoom:
                        // dummy
                        break;

                    case EffectParams.ep_MonsterID:
                        // dummy
                        break;

                    case EffectParams.ep_TileID:
                        this.fSkillsWindow.setMode(SkillsWindow.SWM_TILES);
                        this.fSkillsWindow.show();
                        return;

                    case EffectParams.ep_ScrollID:
                        if (this.fGameSpace.getPlayer().getScrollsCount() == 0) {
                            this.showTextAux(Locale.getStr(RS.rs_YouMayWriteNoScrolls));
                        } else {
                            this.fSkillsWindow.setMode(SkillsWindow.SWM_SCROLLS);
                            this.fSkillsWindow.show();
                        }
                        return;

                    case EffectParams.ep_GodID:
                        this.fSkillsWindow.setMode(SkillsWindow.SWM_GODS);
                        this.fSkillsWindow.show();
                        return;

                    case EffectParams.ep_ItemExt:
                        // dummy
                        break;
                }
            }
        }

        this.fGameSpace.getPlayer().useEffect(this.fTargetData.EffID, this.fTargetData.Source, this.fTargetData.InvMode, this.fTargetData.Ext);

        if (this.fTargetData != null) {
            this.fTargetData.dispose();
            this.fTargetData = null;
        }
    }

    @Override
    public void DoActive(boolean active)
    {
        if (active) {
            this.fSoundEngine.sfxResume();
        } else {
            this.fSoundEngine.sfxSuspend();
        }
    }

    @Override
    public final void setStyle(int value)
    {
        this.fOldStyle = super.getStyle();
        super.setStyle(value);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        NWMainWindow.initEnvironment(StaticData.rs_GameName);
        if (!NWMainWindow.isInstanceExists()) {
            GlobalVars.nwrWin = new NWMainWindow();
            GlobalVars.nwrWin.run();
            GlobalVars.nwrWin.dispose();
        }
    }
}
