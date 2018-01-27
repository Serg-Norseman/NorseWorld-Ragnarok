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

using NWR.Core.Types;
using NWR.Database;
using NWR.Effects;
using NWR.Game;
using NWR.GUI;
using NWR.Items;
using NWR.Universe;
using ZRLib.Core;

namespace NWR.Creatures
{
    public sealed class Projectile
    {
        public const int HIT_NONE = 0;
        public const int HIT_BODY = 1;
        public const int HIT_BARRIER = 2;

        public Item Weapon;
        public Item ProjectileItem;
        public NWCreature Creature;
        public NWField Map;
        public int Steps;
        public int Range;
        public AttackKind Kind;
        public int Hit;

        public Projectile(NWCreature creature, NWField field, AttackKind attackKind, int range, Item weapon, Item projectile)
        {
            Kind = attackKind;
            Range = range;
            Steps = 0;
            Creature = creature;
            Map = field;
            Weapon = weapon;
            ProjectileItem = projectile;
        }

        public void Run(int tX, int tY)
        {
            int sx = Creature.PosX;
            int sy = Creature.PosY;

            int dir = Directions.GetDirByCoords(sx, sy, tX, tY);
            if (GlobalVars.nwrWin.Style == NWMainWindow.RGS_MODERN) {
                dir = NWCreature.ShootIsoTrans[dir];
            }

            ProjectileItem.SetPos(sx, sy);
            ProjectileItem.Frame = (byte)dir;

            AuxUtils.DoLine(sx, sy, tX, tY, LineProc, true);

            if (ProjectileItem.Entry.Flags.Contains(ItemFlags.if_ReturnWeapon)) {
                Creature.PickupItem(ProjectileItem);
                ProjectileItem.InUse = true;
            } else {
                bool loss;
                if (ProjectileItem.Unique) {
                    loss = false;
                } else {
                    bool hit = (Hit != HIT_NONE);
                    loss = ProjectileItem.IsBreakage(hit) || Map.CanSink(ProjectileItem.PosX, ProjectileItem.PosY);
                }

                if (loss) {
                    Map.Items.Remove(ProjectileItem);
                } else {
                    Map.RepackItems();
                }
            }
        }

        private void LineProc(int x, int y, ref bool refContinue)
        {
            Steps++;
            NWGameSpace space = Creature.Space;

            if (!Map.IsValid(x, y)) {
                bool gpChanged = false;

                int fx = Map.Coords.X;
                int fy = Map.Coords.Y;
                int px = x;
                int py = y;

                GlobalPosition gpi = new GlobalPosition(fx, fy, px, py, gpChanged);
                gpi.CheckPos();
                fx = gpi.Fx;
                fy = gpi.Fy;
                px = gpi.Px;
                py = gpi.Py;
                gpChanged = gpi.GlobalChanged;

                NWLayer layer = Map.Layer;
                if (fx >= 0 && fx != layer.W && fy >= 0 && fy != layer.H && gpChanged) {
                    Map.Items.Extract(ProjectileItem);
                    Map = space.GetField(Creature.LayerID, fx, fy);
                    Map.Items.Add(ProjectileItem, false);
                    ProjectileItem.SetPos(px, py);
                }

                refContinue = false;
            } else {
                if (Map.IsBarrier(x, y)) {
                    Hit = HIT_BARRIER;
                    refContinue = false;
                } else if (Steps > Range) {
                    Hit = HIT_NONE;
                    refContinue = false;
                } else {
                    NWCreature target = (NWCreature)Map.FindCreature(x, y);

                    if (target != null && !target.Equals(Creature)) {
                        Creature.AttackTo(Kind, target, Weapon, ProjectileItem);
                        ProjectileItem.SetPos(x, y);
                        Hit = HIT_BODY;

                        ProjectileItem.ApplyEffects(target, InvokeMode.im_Use, null);

                        refContinue = false;
                    }

                    if (refContinue) {
                        ProjectileItem.SetPos(x, y);
                    }
                }
            }

            if (space.Player.IsSeen(x, y, false)) {
                space.RepaintView(25);
            }
        }
    }
}
