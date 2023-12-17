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
using System.IO;
using BSLib;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using ZRLib.Core;
using ZRLib.Map;

namespace NWR.Universe
{
    public sealed class NWLayer : AbstractMap
    {
        private readonly int fH;
        private readonly int fW;

        private EffectsList fEffects;
        private readonly LayerEntry fEntry;
        private NWField[,] fFields;

        private readonly NWGameSpace fSpace;
        public readonly int EntryID;

        public NWLayer(NWGameSpace space, int layerID)
            : base(0, 0)
        {
            fSpace = space;

            fEntry = (LayerEntry)GlobalVars.nwrDB.GetEntry(layerID);
            fW = fEntry.W;
            fH = fEntry.H;

            Resize(fW * StaticData.FieldWidth, fH * StaticData.FieldHeight);

            EntryID = layerID;

            fFields = new NWField[fH, fW];

            for (int y = 0; y < fH; y++) {
                for (int x = 0; x < fW; x++) {
                    fFields[y, x] = new NWField(space, this, new ExtPoint(x, y));
                }
            }

            fEffects = new EffectsList(this, true);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing) {
                fEffects.Dispose();
                fEffects = null;

                for (int y = 0; y < fH; y++) {
                    for (int x = 0; x < fW; x++) {
                        fFields[y, x].Dispose();
                    }
                }
                fFields = null;
            }
            base.Dispose(disposing);
        }

        public NWGameSpace Space
        {
            get {
                return (NWGameSpace)fSpace;
            }
        }

        public EffectsList Effects
        {
            get {
                return fEffects;
            }
        }

        public LayerEntry Entry
        {
            get {
                return fEntry;
            }
        }

        public int H
        {
            get {
                return fH;
            }
        }

        public int W
        {
            get {
                return fW;
            }
        }

        public NWField GetField(int X, int Y)
        {
            if (X >= 0 && X < fW && Y >= 0 && Y < fH) {
                return fFields[Y, X];
            } else {
                return null;
            }
        }

        public string Name
        {
            get {
                return GlobalVars.nwrDB.GetEntry(EntryID).Name;
            }
        }

        public override BaseTile GetTile(int x, int y)
        {
            BaseTile result = null;

            if (IsValid(x, y)) {
                int fx = x / StaticData.FieldWidth;
                int fy = y / StaticData.FieldHeight;

                int tx = x % StaticData.FieldWidth;
                int ty = y % StaticData.FieldHeight;

                NWField fld = GetField(fx, fy);
                result = fld.GetTile(tx, ty);
            }

            return result;
        }

        public override bool IsBlockLOS(int x, int y)
        {
            bool result = true;
            if (IsValid(x, y)) {
                int fx = x / StaticData.FieldWidth;
                int fy = y / StaticData.FieldHeight;
                int tx = x % StaticData.FieldWidth;
                int ty = y % StaticData.FieldHeight;
                result = GetField(fx, fy).IsBlockLOS(tx, ty);
            }
            return result;
        }

        public override CreatureEntity FindCreature(int aX, int aY)
        {
            int fx = aX / StaticData.FieldWidth;
            int fy = aY / StaticData.FieldHeight;
            int x = aX % StaticData.FieldWidth;
            int y = aY % StaticData.FieldHeight;
            NWField f = GetField(fx, fy);
            return f.FindCreature(x, y);
        }

        /// <summary>
        /// Performs the passage of time for the entire layer. 
        /// Necessary in cases, such as for example, the battle on the field Vigrid if the player is in Valhalla.
        /// </summary>
        public void DoTurn()
        {
            try {
                for (int y = 0; y < fH; y++) {
                    for (int x = 0; x < fW; x++) {
                        NWField fld = fFields[y, x];
                        fld.DoTurn();
                    }
                }

                fEffects.Execute();
            } catch (Exception ex) {
                Logger.Write("NWLayer.doTurn(): " + ex.Message);
            }
        }

        public DungeonRoom FindDungeonRoom(int aX, int aY)
        {
            int num = Features.Count;
            for (int i = 0; i < num; i++) {
                object feat = Features[i];
                if (feat is DungeonRoom) {
                    DungeonRoom dr = (DungeonRoom)feat;
                    if (dr.InArea(aX, aY)) {
                        return dr;
                    }
                }
            }

            return null;
        }

        public bool Gen_Creature(int aID, int aX, int aY, bool aFlock)
        {
            int fx = aX / StaticData.FieldWidth;
            int fy = aY / StaticData.FieldHeight;
            int x = aX % StaticData.FieldWidth;
            int y = aY % StaticData.FieldHeight;
            NWField f = GetField(fx, fy);
            return UniverseBuilder.Gen_Creature(f, aID, x, y, aFlock);
        }

        public override Movements GetTileMovements(ushort tileID)
        {
            int pd = AuxUtils.GetShortLo(tileID);
            return StaticData.dbPlaces[pd].Moves;
        }

        public void InitLayer()
        {
            LayerEntry layer_entry = (LayerEntry)GlobalVars.nwrDB.GetEntry(EntryID);
            string entry_sign = layer_entry.Sign;

            try {
                for (int y = 0; y < fH; y++) {
                    for (int x = 0; x < fW; x++) {
                        fFields[y, x].Clear();
                        GlobalVars.nwrWin.ProgressStep();
                    }
                }

                if (EntryID == GlobalVars.Layer_Svartalfheim1 || EntryID == GlobalVars.Layer_Svartalfheim2) {
                    UniverseBuilder.Build_Dungeon(this, AreaRect);
                } else {
                    if (EntryID == GlobalVars.Layer_Svartalfheim3) {
                        ExtRect area = ExtRect.Create(0, 0, StaticData.FieldWidth - 1, StaticData.FieldHeight * 3 - 1);
                        UniverseBuilder.Build_Dungeon(this, area);

                        area = ExtRect.Create(StaticData.FieldWidth, StaticData.FieldHeight * 2, StaticData.FieldWidth * 2 - 1, StaticData.FieldHeight * 3 - 1);
                        UniverseBuilder.Build_Dungeon(this, area);

                        area = ExtRect.Create(StaticData.FieldWidth, 0, StaticData.FieldWidth * 3 - 1, StaticData.FieldHeight * 2 - 1);
                        UniverseBuilder.Build_Caves(this, area);
                    }
                }

                for (int y = 0; y < fH; y++) {
                    for (int x = 0; x < fW; x++) {
                        fFields[y, x].InitField();
                        GlobalVars.nwrWin.ProgressStep();
                    }
                }

                for (int y = 0; y < fH; y++) {
                    for (int x = 0; x < fW; x++) {
                        fFields[y, x].Normalize();
                        GlobalVars.nwrWin.ProgressStep();
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWLayer.InitLayer(" + entry_sign + "): " + ex.Message);
            }
        }

        public override bool IsBarrier(int x, int y)
        {
            bool result = true;
            if (IsValid(x, y)) {
                int fx = x / StaticData.FieldWidth;
                int fy = y / StaticData.FieldHeight;
                int tx = x % StaticData.FieldWidth;
                int ty = y % StaticData.FieldHeight;
                result = GetField(fx, fy).IsBarrier(tx, ty);
            }
            return result;
        }

        public override LocatedEntity FindItem(int aX, int aY)
        {
            int fx = aX / StaticData.FieldWidth;
            int fy = aY / StaticData.FieldHeight;
            int x = aX % StaticData.FieldWidth;
            int y = aY % StaticData.FieldHeight;
            NWField f = GetField(fx, fy);
            return f.FindItem(x, y);
        }

        public override void Normalize()
        {
            for (int y = 0; y < fH; y++) {
                for (int x = 0; x < fW; x++) {
                    fFields[y, x].Normalize();
                }
            }
        }

        public void LoadFromStream(BinaryReader  stream, FileVersion version)
        {
            try {
                for (int y = 0; y < fH; y++) {
                    for (int x = 0; x < fW; x++) {
                        NWField fld = fFields[y, x];

                        fld.Clear();
                        fld.LoadFromStream(stream, version);

                        GlobalVars.nwrWin.ProgressStep();
                    }
                }

                for (int y = 0; y < fH; y++) {
                    for (int x = 0; x < fW; x++) {
                        NWField fld = fFields[y, x];

                        fld.Normalize();
                        fld.NormalizeFog();
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWLayer.loadFromStream(): " + ex.Message);
                throw ex;
            }
        }

        public void SaveToStream(BinaryWriter stream, FileVersion version)
        {
            try {
                for (int y = 0; y < fH; y++) {
                    for (int x = 0; x < fW; x++) {
                        fFields[y, x].SaveToStream(stream, version);
                        GlobalVars.nwrWin.ProgressStep();
                    }
                }
            } catch (Exception ex) {
                Logger.Write("NWLayer.saveToStream(): " + ex.Message);
                throw ex;
            }
        }

        public override ushort TranslateTile(TileType defTile)
        {
            return UniverseBuilder.TranslateTile(defTile);
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