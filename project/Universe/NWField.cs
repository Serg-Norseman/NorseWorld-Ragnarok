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
using NWR.Creatures;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.Game.Types;
using NWR.Items;
using ZRLib.Core;
using ZRLib.Core.Brain;
using ZRLib.Map;

namespace NWR.Universe
{
    public sealed class NWField : CustomMap
    {
        // Wasteland States
        private const int WS_ERUPTION = 0;
        private const int WS_FINISH = 1;
        private const int WS_VULCAN_NOT_EXISTS = 2;

        // other
        public static readonly ushort[] Muspel_bgTiles;
        public static readonly ushort[] Muspel_fgTiles;

        private readonly ExtPoint fCoords;
        private readonly CreaturesList fCreatures;
        private readonly EffectsList fEffects;
        private readonly EmitterList fEmitters;
        private readonly FieldEntry fEntry;
        private readonly ItemsList fItems;
        private readonly LandEntry fLandEntry;
        private readonly NWGameSpace fSpace;
        private readonly NWLayer fLayer;

        public readonly int EntryID;
        public readonly int LandID;
        public readonly List<int> ValidCreatures;
        public bool Visited;

        public NWField(NWGameSpace space, NWLayer layer, ExtPoint coords)
            : base(StaticData.FieldWidth, StaticData.FieldHeight)
        {
            fSpace = space;
            fLayer = layer;
            fCoords = coords;

            fCreatures = new CreaturesList(this, true);
            fItems = new ItemsList(this, true);
            fEffects = new EffectsList(this, true);
            fEmitters = new EmitterList();
            ValidCreatures = new List<int>();

            if (Layer != null) {
                LayerEntry layerEntry = (LayerEntry)GlobalVars.nwrDB.GetEntry(Layer.EntryID);
                fEntry = layerEntry.GetFieldEntry(fCoords.X, fCoords.Y);

                EntryID = fEntry.GUID;

                fLandEntry = (LandEntry)GlobalVars.nwrDB.FindEntryBySign(fEntry.LandSign);
                LandID = fLandEntry.GUID;

                PrepareCreatures();
            } else {
                fEntry = null;
                EntryID = -1;
                fLandEntry = null;
                LandID = -1;
            }
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fEmitters.Dispose();
                fEffects.Dispose();
                fItems.Dispose();
                fCreatures.Dispose();
                //this.ValidCreatures = null;
            }
            base.Dispose(disposing);
        }

        protected override BaseTile CreateTile()
        {
            return new NWTile();
        }

        public override BaseTile GetTile(int x, int y)
        {
            BaseTile result = base.GetTile(x, y);

            if (result == null) {
                int fx = fCoords.X;
                int fy = fCoords.Y;
                int px = x;
                int py = y;

                if (x < 0 || x >= StaticData.FieldWidth) {
                    px = Math.Abs(Math.Abs(x) - StaticData.FieldWidth);
                }
                if (y < 0 || y >= StaticData.FieldHeight) {
                    py = Math.Abs(Math.Abs(y) - StaticData.FieldHeight);
                }
                if (x < 0) {
                    fx--;
                }
                if (x >= StaticData.FieldWidth) {
                    fx++;
                }
                if (y < 0) {
                    fy--;
                }
                if (y >= StaticData.FieldHeight) {
                    fy++;
                }

                NWLayer layer = Layer;

                if (layer != null) {
                    NWField fld = layer.GetField(fx, fy);
                    if (fld != null) {
                        result = fld.GetTile(px, py);
                    }
                }
            }

            return result;
        }

        public NWGameSpace Space
        {
            get {
                return (NWGameSpace)fSpace;
            }
        }

        public NWLayer Layer
        {
            get {
                return fLayer;
            }
        }

        public ExtPoint Coords
        {
            get {
                return fCoords;
            }
        }

        public ExtPoint GetLayerTileCoords(int tx, int ty)
        {
            return new ExtPoint(fCoords.X * StaticData.FieldWidth + tx, fCoords.Y * StaticData.FieldHeight + ty);
        }

        public ExtPoint GetLayerTileCoords(ExtPoint pt)
        {
            return new ExtPoint(fCoords.X * StaticData.FieldWidth + pt.X, fCoords.Y * StaticData.FieldHeight + pt.Y);
        }

        public CreaturesList Creatures
        {
            get {
                return fCreatures;
            }
        }

        public EffectsList Effects
        {
            get {
                return fEffects;
            }
        }

        public EmitterList Emitters
        {
            get {
                return fEmitters;
            }
        }

        public ItemsList Items
        {
            get {
                return fItems;
            }
        }

        public LandEntry LandEntry
        {
            get {
                return fLandEntry;
            }
        }

        private void PrepareCreatures()
        {
            try {
                ValidCreatures.Clear();

                int num2 = GlobalVars.dbCreatures.Count;
                for (int i = 0; i < num2; i++) {
                    CreatureEntry crEntry = (CreatureEntry)GlobalVars.nwrDB.GetEntry(GlobalVars.dbCreatures[i]);
                    if (crEntry.IsInhabitant(fLandEntry.Sign)) {
                        ValidCreatures.Add(GlobalVars.dbCreatures[i]);
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWField.prepareCreatures(): " + ex.Message);
                throw ex;
            }
        }

        public string LandName
        {
            get {
                return fLandEntry.Name;
            }
        }

        public override void Normalize()
        {
            UniverseBuilder.NormalizeFieldMasks(this, false);
        }

        public void NormalizeFog()
        {
            UniverseBuilder.NormalizeFieldMasks(this, true);
        }

        private void DoTurn_prepareFogs()
        {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = (NWTile)GetTile(x, y);
                    if (tile.FogID == PlaceID.pid_Fog) {
                        if (tile.FogAge > 0) {
                            tile.FogAge = (sbyte)(tile.FogAge - 1);
                        }
                        if (tile.FogAge == 0) {
                            tile.FogID = (ushort)PlaceID.pid_Undefined;
                        }
                    }
                }
            }

            NormalizeFog();
        }

        private void DoTurn_prepareScentTrails()
        {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = (NWTile)GetTile(x, y);
                    if (tile.ScentAge > 0) {
                        tile.ScentAge = (byte)(tile.ScentAge - 1);
                    }
                    if (tile.ScentAge == 0) {
                        tile.ScentTrail = null;
                    }
                }
            }
        }

        public void SpreadTiles(ushort tileID, float density)
        {
            try {
                int tsz = 0;
                List<ExtPoint> bounds = new List<ExtPoint>();
                for (int y = 0; y < Height; y++) {
                    for (int x = 0; x < Width; x++) {
                        NWTile tile = (NWTile)GetTile(x, y);
                        if (tile.BackBase != tileID) {
                            int cnt = GetBackTilesCount(x, y, (short)tileID);
                            if (cnt > 0) {
                                bounds.Add(new ExtPoint(x, y));
                            }
                        } else {
                            tsz++;
                        }
                    }
                }

                int xcnt = RandomHelper.GetBoundedRnd(1, Math.Max(1, (int)Math.Round(bounds.Count * density)));
                for (int i = 1; i <= xcnt; i++) {
                    int idx = RandomHelper.GetRandom(bounds.Count);
                    ExtPoint pt = bounds[idx];
                    bounds.RemoveAt(idx);

                    ChangeTile(pt.X, pt.Y, tileID, false);
                }
                tsz += xcnt;

                // ALERT: change genBackTiles for compatibility with changeTile()
                float min = AreaRect.Square * 0.1f;
                if ((float)tsz < min) {
                    UniverseBuilder.GenBackTiles(this, (int)(min - tsz), tileID);
                }

                Normalize();
            } catch (Exception ex) {
                Logger.Write("NWField.spreadTiles(): " + ex.Message);
            }
        }

        // FIXME: change total other functions for compatibiliy with this
        private void ChangeTile(int x, int y, ushort tileID, bool fg)
        {
            BaseTile tile = GetTile(x, y);
            if (tile != null) {
                if (!fg) {
                    tile.Background = tileID;
                } else {
                    tile.Foreground = tileID;
                }

                switch (tileID) {
                    case PlaceID.pid_Lava:
                        {
                            if (tile.ForeBase == PlaceID.pid_Tree) {
                                tile.Foreground = PlaceID.pid_DeadTree;
                                GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_TreeBursts));
                            }

                            // ALERT: fixme, where resistances?!!
                            NWCreature cr = (NWCreature)FindCreature(x, y);
                            if (cr != null) {
                                if (cr.IsPlayer) {
                                    GlobalVars.nwrWin.ShowText(cr, BaseLocale.GetStr(RS.rs_LavaEncasesYou));
                                    cr.Death(BaseLocale.GetStr(RS.rs_EncasedInVolcanicRock), null);
                                } else {
                                    cr.Death(BaseLocale.GetStr(RS.rs_XIsConsumedByLava), null);
                                }
                            }
                            break;
                        }
                }
            }
        }

        private int DoTurn_eruptionWastelandVulcan()
        {
            int state = WS_ERUPTION;

            try {
                bool vulcan_ex = false;

                List<ExtPoint> lava_bounds = new List<ExtPoint>();

                for (int y = 0; y < Height; y++) {
                    for (int x = 0; x < Width; x++) {
                        NWTile tile = (NWTile)GetTile(x, y);
                        int cnt = GetBackTilesCount(x, y, (short)PlaceID.pid_Lava);

                        if (tile.ForeBase == PlaceID.pid_Vulcan) {
                            vulcan_ex = true;
                        }

                        if (tile.BackBase == PlaceID.pid_Grass && cnt > 0) {
                            lava_bounds.Add(new ExtPoint(x, y));
                        }
                    }
                }

                if (lava_bounds.Count == 0) {
                    state = WS_FINISH;
                }
                if (!vulcan_ex) {
                    state = WS_VULCAN_NOT_EXISTS;
                }

                if (state != WS_ERUPTION) {
                    return state;
                }

                if (lava_bounds.Count > 5) {
                    GlobalVars.nwrWin.ShowText(GlobalVars.nwrWin, BaseLocale.GetStr(RS.rs_GroundBeginsToShake));
                }

                int xcnt = RandomHelper.GetBoundedRnd(1, Math.Max(1, (int)Math.Round(lava_bounds.Count * 0.25f)));
                for (int i = 1; i <= xcnt; i++) {
                    int idx = RandomHelper.GetRandom(lava_bounds.Count);
                    ExtPoint pt = lava_bounds[idx];
                    lava_bounds.RemoveAt(idx);

                    ChangeTile(pt.X, pt.Y, PlaceID.pid_Lava, false);
                }

                Normalize();
            } catch (Exception ex) {
                Logger.Write("NWField.doTurn_eruptionWastelandVulcan(): " + ex.Message);
            }

            return state;
        }

        private void ProcessVariants()
        {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = (NWTile)GetTile(x, y);
                    if (tile.ForeBase == PlaceID.pid_Mountain) {
                        tile.Foreground = GetVarTile(PlaceID.pid_Mountain);
                    }
                }
            }
        }

        public void AddSeveralTraps(ushort[] kinds, int f)
        {
            int cnt;
            if (f > 0) {
                cnt = f;
            } else {
                cnt = RandomHelper.GetBoundedRnd(3, -4 * f);
            }

            int fx = fCoords.X;
            int fy = fCoords.Y;

            for (int i = 1; i <= cnt; i++) {
                while (true) {
                    int y = RandomHelper.GetBoundedRnd(0, Height - 1);
                    int x = RandomHelper.GetBoundedRnd(0, Width - 1);
                    NWTile tile = (NWTile)GetTile(x, y);

                    bool ready = (FindBuilding(x, y) == null && tile.ForeBase == PlaceID.pid_Undefined);
                    if (ready) {
                        ushort trapKind;
                        do {
                            trapKind = RandomHelper.GetRandomItem(kinds);
                            bool crt = (trapKind == PlaceID.pid_CrushRoofTrap);

                            int lx = (fx * StaticData.FieldWidth) + x;
                            int ly = (fy * StaticData.FieldHeight) + y;

                            bool hasDunRoom = crt && (FindDungeonRoom(x, y) != null || Layer.FindDungeonRoom(lx, ly) != null);
                            ready = (!crt || (crt & hasDunRoom));
                        } while (!ready);

                        AddTrap(x, y, trapKind, false);
                        break;
                    }
                }
            }
        }

        public void AddTrap(int aX, int aY, ushort trapTileID, bool discovered)
        {
            NWTile tile = (NWTile)GetTile(aX, aY);
            tile.Foreground = trapTileID;
            tile.Trap_Discovered = discovered;
        }

        public override bool IsBlockLOS(int x, int y)
        {
            bool result;

            NWTile tile = (NWTile)GetTile(x, y);
            if (tile == null) {
                result = true;
            } else {
                int bg = tile.BackBase;
                int fg = tile.ForeBase;

                result = ((StaticData.dbPlaces[bg].Signs.Contains(PlaceFlags.psBlockLOS)) || (fg != PlaceID.pid_Undefined && (StaticData.dbPlaces[fg].Signs.Contains(PlaceFlags.psBlockLOS))));
            }

            return result;
        }

        public override bool IsBarrier(int x, int y)
        {
            bool result = true;
            NWTile p = (NWTile)GetTile(x, y);
            if (p != null) {
                int bg = (p.BackBase);
                int fg = (p.ForeBase);

                result = ((StaticData.dbPlaces[bg].Signs.Contains(PlaceFlags.psBarrier)) || (fg != PlaceID.pid_Undefined && (StaticData.dbPlaces[fg].Signs.Contains(PlaceFlags.psBarrier))));
            }
            return result;
        }

        public void Close(bool needFG)
        {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = (NWTile)GetTile(x, y);
                    if (needFG && tile.ForeBase != PlaceID.pid_Undefined) {
                        // dummy
                    } else {
                        tile.ExcludeState(BaseTile.TS_VISITED);
                    }
                }
            }
        }

        public override CreatureEntity FindCreature(int aX, int aY)
        {
            NWTile tile = (NWTile)GetTile(aX, aY);

            CreatureEntity result;

            if (tile != null) {
                result = tile.CreaturePtr;
            } else {
                result = null;
            }

            return result;
        }

        public void SetAmbient()
        {
            SoundEngine.Reverb reverb = SoundEngine.Reverb.Forest;

            if (LandID == GlobalVars.Land_Vanaheim || LandID == GlobalVars.Land_Wasteland || LandID == GlobalVars.Land_Alfheim || LandID == GlobalVars.Land_Crossroads || LandID == GlobalVars.Land_Niflheim || LandID == GlobalVars.Land_Ocean) {
                reverb = SoundEngine.Reverb.Plain;
            }
            if (LandID == GlobalVars.Land_Village || LandID == GlobalVars.Land_Forest) {
                reverb = SoundEngine.Reverb.Forest;
            }
            if (LandID == GlobalVars.Land_MimerRealm || LandID == GlobalVars.Land_Muspelheim || LandID == GlobalVars.Land_Jotenheim) {
                reverb = SoundEngine.Reverb.Mountains;
            }
            if (LandID == GlobalVars.Land_MimerWell) {
                reverb = SoundEngine.Reverb.Underwater;
            }
            if (LandID == GlobalVars.Land_GreatCaves || LandID == GlobalVars.Land_Nidavellir) {
                reverb = SoundEngine.Reverb.Cave;
            }
            if (LandID == GlobalVars.Land_Caves || LandID == GlobalVars.Land_DeepCaves || LandID == GlobalVars.Land_GrynrHalls || LandID == GlobalVars.Land_Temple || LandID == GlobalVars.Land_Armory || LandID == GlobalVars.Land_Bazaar || LandID == GlobalVars.Land_Crypt) {
                reverb = SoundEngine.Reverb.Dungeon;
            }
            if (LandID == GlobalVars.Land_GodsFortress) {
                reverb = SoundEngine.Reverb.Room;
            }
            /*if (this.LandID == GlobalVars.Land_Valhalla || this.LandID == GlobalVars.Land_Vigrid) {
                reverb = SoundEngine.Reverb.Arena;
            }*/
            if (LandID == GlobalVars.Land_Valhalla) {
                reverb = SoundEngine.Reverb.CHall;
            }
            if (LandID == GlobalVars.Land_Vigrid) {
                reverb = SoundEngine.Reverb.Quarry;
            }

            GlobalVars.nwrWin.SoundsReverb = reverb;
        }

        public void DoTurn()
        {
            try {
                fEmitters.UpdateEmitters(1);
                fEffects.Execute();

                DoTurn_prepareFogs();
                DoTurn_prepareScentTrails();

                if (LandID == GlobalVars.Land_Wasteland) {
                    DoTurn_eruptionWastelandVulcan();
                }

                if (LandID == GlobalVars.Land_Muspelheim) {
                    UniverseBuilder.Regen_Muspelheim(this);
                }

                Player player = Space.Player;
                if (LandID == GlobalVars.Land_Village && AuxUtils.Chance(10) && FindBuilding(player.PosX, player.PosY) == null) {
                    int num = Features.Count;
                    for (int i = 0; i < num; i++) {
                        GameEntity entry = Features[i];
                        if (entry is Building) {
                            ((Building)entry).Refresh();
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWField.doTurn(): " + ex.Message);
            }
        }

        public Building FindBuilding(int aX, int aY)
        {
            int num = Features.Count;
            for (int i = 0; i < num; i++) {
                GameEntity feat = Features[i];
                if (feat is Building && ((Building)feat).Area.Contains(aX, aY)) {
                    return ((Building)feat);
                }
            }
            return null;
        }

        public DungeonRoom FindDungeonRoom(int aX, int aY)
        {
            int num = Features.Count;
            for (int i = 0; i < num; i++) {
                object feat = Features[i];
                if (feat is DungeonRoom) {
                    DungeonRoom dr = ((DungeonRoom)feat);
                    if (dr.InArea(aX, aY)) {
                        return dr;
                    }
                }
            }

            return null;
        }

        public Village FindVillage()
        {
            int num = Features.Count;
            for (int i = 0; i < num; i++) {
                object feat = Features[i];
                if (feat is Village) {
                    return ((Village)feat);
                }
            }

            return null;
        }

        public string GetPlaceName(int x, int y)
        {
            string result = "";

            NWTile tile = (NWTile)base.GetTile(x, y);
            if (tile != null && !tile.EmptyStates) {
                int bg = tile.BackBase;
                int fg = tile.ForeBase;

                if (bg != PlaceID.pid_Undefined) {
                    result = BaseLocale.GetStr(StaticData.dbPlaces[bg].NameRS);
                    if (bg == PlaceID.pid_Liquid) {
                        result = result + " (" + GlobalVars.nwrDB.GetEntry(tile.Lake_LiquidID).Name + ")";
                    }
                }

                if (fg != PlaceID.pid_Undefined) {
                    if (IsTrap(x, y) && tile.Trap_Discovered) {
                        result = result + ", " + BaseLocale.GetStr(StaticData.dbPlaces[fg].NameRS);
                    } else {
                        Gate gate = FindGate(x, y);
                        if (gate != null) {
                            result = result + ", " + gate.Name;
                        } else {
                            result = result + ", " + BaseLocale.GetStr(StaticData.dbPlaces[fg].NameRS);
                        }
                    }
                }
            }
            return result;
        }

        public override Movements GetTileMovements(ushort tileID)
        {
            int pd = AuxUtils.GetShortLo(tileID);
            return StaticData.dbPlaces[pd].Moves;
        }

        private void Prepare(int defPlace)
        {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    BaseTile tile = base.GetTile(x, y);
                    tile.Background = (ushort)defPlace;
                    tile.Foreground = (ushort)PlaceID.pid_Undefined;
                    tile.BackgroundExt = (ushort)PlaceID.pid_Undefined;
                    tile.ForegroundExt = (ushort)PlaceID.pid_Undefined;
                }
            }
        }

        public void InitField()
        {
            try {
                NWLayer layer = Layer;
                if (fEntry != null && fEntry.Source == FieldSource.fsTemplate) {
                    LoadFromEntry(fEntry);
                    ProcessVariants();
                } else {
                    if (LandID == GlobalVars.Land_Armory || LandID == GlobalVars.Land_GrynrHalls) {
                        UniverseBuilder.Build_Dungeon(this, AreaRect);
                    } else {
                        if (layer.EntryID != GlobalVars.Layer_Svartalfheim1 && layer.EntryID != GlobalVars.Layer_Svartalfheim2 && layer.EntryID != GlobalVars.Layer_Svartalfheim3) {
                            Prepare(PlaceID.pid_Grass);
                        }
                    }
                }

                int defTS = 0;
                if (LandID == GlobalVars.Land_Bifrost || LandID == GlobalVars.Land_Crossroads) {
                    defTS = BaseTile.TS_VISITED;
                }

                for (int y = 0; y < StaticData.FieldHeight; y++) {
                    for (int x = 0; x < StaticData.FieldWidth; x++) {
                        NWTile tile = (NWTile)GetTile(x, y);
                        tile.BackgroundExt = 0;
                        tile.ForegroundExt = 0;
                        tile.ScentAge = 0;
                        tile.ScentTrail = null;
                        tile.AddStates(defTS);
                    }
                }

                if (fLandEntry.Flags.Contains(LandFlags.lsHasForest)) {
                    UniverseBuilder.Build_Forest(this);
                }
                if (fLandEntry.Flags.Contains(LandFlags.lsHasMountain)) {
                    UniverseBuilder.Gen_RarelyMountains(this);
                }
                if (fLandEntry.Flags.Contains(LandFlags.lsHasItems)) {
                    UniverseBuilder.Gen_Items(this, -1);
                }
                if (fLandEntry.Flags.Contains(LandFlags.lsIsCave)) {
                    UniverseBuilder.Gen_CaveObjects(this);
                }

                if (LandID == GlobalVars.Land_Ocean) {
                    Prepare(PlaceID.pid_Water);
                }

                if (LandID == GlobalVars.Land_Vanaheim) {
                    UniverseBuilder.Build_Vanaheim(this);
                }
                if (LandID == GlobalVars.Land_Alfheim) {
                    UniverseBuilder.Build_Alfheim(this);
                }
                if (LandID == GlobalVars.Land_Wasteland) {
                    UniverseBuilder.Build_Wasteland(this);
                }
                if (LandID == GlobalVars.Land_Crossroads) {
                    UniverseBuilder.Build_Crossroads(this);
                }
                if (LandID == GlobalVars.Land_Village) {
                    UniverseBuilder.Build_Village(this);
                }
                if (LandID == GlobalVars.Land_MimerRealm) {
                    UniverseBuilder.Build_MimerRealm(this);
                }
                if (LandID == GlobalVars.Land_MimerWell) {
                    UniverseBuilder.Build_MimerWell(this);
                }
                if (LandID == GlobalVars.Land_GrynrHalls) {
                    UniverseBuilder.Build_GrynrHalls(this);
                }
                if (LandID == GlobalVars.Land_Bazaar) {
                    UniverseBuilder.Build_Bazaar(this);
                }
                if (LandID == GlobalVars.Land_Temple) {
                    UniverseBuilder.Build_VidurTemple(this);
                }
                if (LandID == GlobalVars.Land_GodsFortress) {
                    UniverseBuilder.Build_GodsFortress(this);
                }
                if (LandID == GlobalVars.Land_Valhalla) {
                    UniverseBuilder.Build_Valhalla(this);
                }
                if (LandID == GlobalVars.Land_Muspelheim) {
                    UniverseBuilder.Build_Muspelheim(this);
                }

                if (LandID == GlobalVars.Land_Ocean) {
                    UniverseBuilder.Build_Ocean(this);
                } else {
                    UniverseBuilder.Gen_Creatures(this, -1);
                    UniverseBuilder.Gen_Traps(this, -2);
                }
            } catch (Exception ex) {
                Logger.Write("NWField.InitField(" + DebugSign + "): " + ex.Message);
            }
        }

        public bool Dark
        {
            get {
                return fLandEntry.Flags.HasIntersect(LandFlags.lsIsDungeon, LandFlags.lsIsCave);
            }
        }

        public void AddGate(ushort tileID, int posX, int posY, int targetLayer, ExtPoint targetField, ExtPoint targetPos)
        {
            NWTile tile = (NWTile)GetTile(posX, posY);
            tile.Foreground = tileID;

            Gate gate = new Gate(fSpace, this);
            gate.CLSID = tileID;
            gate.SetPos(posX, posY);
            gate.TargetLayer = targetLayer;
            gate.TargetField = targetField.Clone();
            gate.TargetPos = targetPos.Clone();
            Features.Add(gate);
        }

        public Gate FindGate(int tx, int ty)
        {
            var features = Features;
            int num = features.Count;
            for (int i = 0; i < num; i++) {
                GameEntity feat = features[i];
                if (feat is Gate) {
                    Gate gate = (Gate)feat;
                    if (gate.Location.Equals(tx, ty)) {
                        return gate;
                    }
                }
            }
            return null;
        }

        public bool IsTrap(int tx, int ty)
        {
            bool result = false;
            NWTile tile = (NWTile)GetTile(tx, ty);
            int fg = tile.ForeBase;
            if (fg != PlaceID.pid_Undefined) {
                result = ((StaticData.dbPlaces[fg].Signs.Contains(PlaceFlags.psIsTrap)));
            }
            return result;
        }

        public override LocatedEntity FindItem(int aX, int aY)
        {
            return fItems.SearchItemByPos(aX, aY);
        }

        public void DropItem(NWCreature creature, Item item, int tx, int ty)
        {
            item.Owner = this;
            item.SetPos(tx, ty);
            fItems.Add(item, false);
        }

        public void PickupItem(NWCreature creature, Item item)
        {
            fItems.Extract(item);
        }

        public void RepackItems()
        {
            try {
                int num = fItems.Count;
                if (num < 2) {
                    return;
                }

                for (int i = 0; i < fItems.Count; i++) {
                    Item it1 = fItems[i];

                    for (int k = fItems.Count - 1; k >= i + 1; k--) {
                        Item it2 = fItems[k];
                        if (it1.PosX == it2.PosX && it1.PosY == it2.PosY && it1.Assign(it2)) {
                            fItems.Delete(k);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWField.repackItems(): " + ex.Message);
            }
        }

        public void LoadFromEntry(FieldEntry entry)
        {
            for (int yy = 0; yy < StaticData.FieldHeight; yy++) {
                for (int xx = 0; xx < StaticData.FieldWidth; xx++) {
                    NWTile tile = (NWTile)GetTile(xx, yy);

                    FieldEntry.FDTile fdTile = entry.Data.Tiles[yy, xx];

                    tile.Background = fdTile.BackGround;
                    tile.BackgroundExt = (ushort)PlaceID.pid_Undefined;

                    tile.Foreground = fdTile.ForeGround;
                    tile.ForegroundExt = (ushort)PlaceID.pid_Undefined;

                    tile.CreaturePtr = null;

                    tile.FogID = (ushort)PlaceID.pid_Undefined;
                    tile.FogExtID = (ushort)PlaceID.pid_Undefined;
                    tile.FogAge = 0;
                }
            }
        }

        public void Clear()
        {
            try {
                Features.Clear();
                fCreatures.Clear();
                fItems.Clear();

                Visited = false;
            } catch (Exception ex) {
                Logger.Write("NWField.clear(" + DebugSign + "): " + ex.Message);
                throw ex;
            }
        }

        public void LoadFromStream(BinaryReader stream, FileVersion version)
        {
            try {
                for (int y = 0; y < StaticData.FieldHeight; y++) {
                    for (int x = 0; x < StaticData.FieldWidth; x++) {
                        NWTile tile = (NWTile)GetTile(x, y);
                        tile.LoadFromStream(stream, version);
                    }
                }

                fCreatures.LoadFromStream(stream, version);
                fItems.LoadFromStream(stream, version);

                int num3 = fItems.Count;
                for (int i = 0; i < num3; i++) {
                    fItems[i].Owner = this;
                }

                Features.LoadFromStream(stream, version);

                Visited = StreamUtils.ReadBoolean(stream);
            } catch (Exception ex) {
                Logger.Write("NWField.loadFromStream(): " + ex.Message, ex);
                throw ex;
            }
        }

        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            try {
                for (int y = 0; y < StaticData.FieldHeight; y++) {
                    for (int x = 0; x < StaticData.FieldWidth; x++) {
                        NWTile tile = (NWTile)GetTile(x, y);
                        tile.SaveToStream(stream, version);
                    }
                }

                fCreatures.SaveToStream(stream, version);
                fItems.SaveToStream(stream, version);
                Features.SaveToStream(stream, version);

                StreamUtils.WriteBoolean(stream, Visited);
            } catch (Exception ex) {
                Logger.Write("NWField.saveToStream(): " + ex.Message, ex);
                throw ex;
            }
        }

        public void Research(bool onlyForeground, int tileStates)
        {
            for (int y = 0; y < StaticData.FieldHeight; y++) {
                for (int x = 0; x < StaticData.FieldWidth; x++) {
                    NWTile tile = (NWTile)GetTile(x, y);

                    if (onlyForeground) {
                        if (tile.ForeBase != PlaceID.pid_Undefined) {
                            tile.AddStates(tileStates);
                        }
                    } else {
                        tile.AddStates(tileStates);
                    }
                }
            }
        }

        public bool CanSink(int x, int y)
        {
            NWTile tile = (NWTile)GetTile(x, y);
            int bg = tile.BackBase;
            return bg == PlaceID.pid_Water || bg == PlaceID.pid_Quicksand;
        }

        public override ushort TranslateTile(TileType defTile)
        {
            return UniverseBuilder.TranslateTile(defTile);
        }

        public static ushort GetBuildPlaceKind(int x, int y, ExtRect r, bool isRuins)
        {
            ushort res = PlaceID.pid_Undefined;

            if (x == r.Left && y == r.Top) {
                res = PlaceID.pid_WallNW;
            } else {
                if (x == r.Right && y == r.Top) {
                    res = PlaceID.pid_WallNE;
                } else {
                    if (x == r.Left && y == r.Bottom) {
                        res = PlaceID.pid_WallSW;
                    } else {
                        if (x == r.Right && y == r.Bottom) {
                            res = PlaceID.pid_WallSE;
                        } else {
                            if (x > r.Left && x < r.Right && y == r.Top) {
                                res = PlaceID.pid_WallN;
                            } else {
                                if (x > r.Left && x < r.Right && y == r.Bottom) {
                                    res = PlaceID.pid_WallS;
                                } else {
                                    if (y > r.Top && y < r.Bottom && x == r.Left) {
                                        res = PlaceID.pid_WallW;
                                    } else {
                                        if (y > r.Top && y < r.Bottom && x == r.Right) {
                                            res = PlaceID.pid_WallE;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isRuins) {
                switch (res) {
                    case PlaceID.pid_WallNW:
                        res = PlaceID.pid_RnWallNW;
                        break;
                    case PlaceID.pid_WallNE:
                        res = PlaceID.pid_RnWallNE;
                        break;
                    case PlaceID.pid_WallSW:
                        res = PlaceID.pid_RnWallSW;
                        break;
                    case PlaceID.pid_WallSE:
                        res = PlaceID.pid_RnWallSE;
                        break;
                    case PlaceID.pid_WallN:
                        res = PlaceID.pid_RnWallN;
                        break;
                    case PlaceID.pid_WallS:
                        res = PlaceID.pid_RnWallS;
                        break;
                    case PlaceID.pid_WallW:
                        res = PlaceID.pid_RnWallW;
                        break;
                    case PlaceID.pid_WallE:
                        res = PlaceID.pid_RnWallE;
                        break;
                }
            }

            return res;
        }

        public static ushort GetVarTile(int tid)
        {
            int @base = tid;
            int @var;

            if ((StaticData.dbPlaces[@base].Signs.Contains(PlaceFlags.psVarDirect)) && StaticData.dbPlaces[@base].SubsLoaded > 0) {
                @var = RandomHelper.GetBoundedRnd(0, StaticData.dbPlaces[@base].SubsLoaded);
            } else {
                @var = 0;
            }

            return BaseTile.GetVarID((byte)@base, (byte)@var);
        }

        public string DebugSign
        {
            get {
                string res = string.Format("{0:D}, {1:D}, {2:D}, {3}", EntryID, fCoords.X, fCoords.Y, fLandEntry.Sign);
                return res;
            }
        }

        public NWCreature AddCreature(int px, int py, int creatureID)
        {
            return Space.AddCreatureEx(Layer.EntryID, fCoords.X, fCoords.Y, px, py, creatureID);
        }

        public Item AddItem(int px, int py, int itemID)
        {
            return Space.AddItemEx(Layer.EntryID, fCoords.X, fCoords.Y, px, py, itemID);
        }

        public Region GetRegion(int ax, int ay)
        {
            int num = Features.Count;
            for (int i = 0; i < num; i++) {
                GameEntity entry = Features[i];
                if (entry is Region) {
                    Region region = ((Region)entry);
                    if (region.InArea(ax, ay)) {
                        return region;
                    }
                }
            }

            return null;
        }

        static NWField()
        {
            Muspel_bgTiles = new ushort[] {
                PlaceID.pid_Grass,
                PlaceID.pid_Water,
                PlaceID.pid_Quicksand,
                PlaceID.pid_Lava,
                PlaceID.pid_Mud,
                PlaceID.pid_Rubble,
                PlaceID.pid_Ground
            };

            Muspel_fgTiles = new ushort[] {
                PlaceID.pid_Undefined,
                PlaceID.pid_Tree,
                PlaceID.pid_Undefined,
                PlaceID.pid_Mountain,
                PlaceID.pid_Undefined,
                PlaceID.pid_DeadTree
            };
        }

        /// ALERT!

        public override void SetMetaTile(int x, int y, TileType tile)
        {
            BaseTile baseTile = GetTile(x, y);
            if (baseTile != null) {
                baseTile.Background = TranslateTile(tile);
            }
        }

        public override void FillMetaBorder(int x1, int y1, int x2, int y2, TileType tile)
        {
            ushort defTile = TranslateTile(tile);
            FillBorder(x1, y1, x2, y2, defTile, false);
        }

        public char GetMetaTile(int x, int y)
        {
            BaseTile baseTile = GetTile(x, y);
            if (baseTile != null) {
                return (char)baseTile.Background;
            } else {
                return ' ';
            }
        }

        public override float GetPathTileCost(CreatureEntity creature, int tx, int ty, BaseTile tile)
        {
            bool barrier;
            if (creature == null) {
                barrier = IsBarrier(tx, ty);
            } else {
                barrier = !creature.CanMove(this, tx, ty);
                if (!barrier && creature.IsSeen(tx, ty, false)) {
                    barrier = (FindCreature(tx, ty) != null);
                }
            }

            return (barrier ? PathSearch.BARRIER_COST : 1.0f);
        }
    }

}