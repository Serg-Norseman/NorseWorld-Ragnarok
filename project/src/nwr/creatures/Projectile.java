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

import jzrlib.core.Directions;
import jzrlib.utils.AuxUtils;
import jzrlib.utils.RefObject;
import nwr.core.types.AttackKind;
import nwr.database.ItemFlags;
import nwr.effects.InvokeMode;
import nwr.game.NWGameSpace;
import nwr.item.Item;
import nwr.main.GlobalVars;
import nwr.main.NWMainWindow;
import nwr.universe.NWField;
import nwr.universe.NWLayer;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public final class Projectile
{
    public static final int HIT_NONE = 0;
    public static final int HIT_BODY = 1;
    public static final int HIT_BARRIER = 2;
    
    public Item Weapon;
    public Item Projectile;
    public NWCreature Creature;
    public NWField Map;
    public int Steps;
    public int Range;
    public AttackKind Kind;
    public int Hit;

    public Projectile(NWCreature creature, NWField field, AttackKind attackKind, int range, Item weapon, Item projectile)
    {
        this.Kind = attackKind;
        this.Range = range;
        this.Steps = 0;
        this.Creature = creature;
        this.Map = field;
        this.Weapon = weapon;
        this.Projectile = projectile;
    }

    public final void run(int tX, int tY)
    {
        int sx = this.Creature.getPosX();
        int sy = this.Creature.getPosY();
        
        int dir = Directions.getDirByCoords(sx, sy, tX, tY);
        if (GlobalVars.nwrWin.getStyle() == NWMainWindow.RGS_MODERN) {
            dir = NWCreature.ShootIsoTrans[dir];
        }

        this.Projectile.setPos(sx, sy);
        this.Projectile.Frame = (byte) dir;

        AuxUtils.doLine(sx, sy, tX, tY, this::LineProc, true);

        if (this.Projectile.getEntry().Flags.contains(ItemFlags.if_ReturnWeapon)) {
            this.Creature.pickupItem(this.Projectile);
            this.Projectile.setInUse(true);
        } else {
            boolean loss;
            if (this.Projectile.isUnique()) {
                loss = false;
            } else {
                boolean hit = (this.Hit != HIT_NONE);
                loss = this.Projectile.isBreakage(hit)
                        || this.Map.canSink(this.Projectile.getPosX(), this.Projectile.getPosY());
            }

            if (loss) {
                this.Map.getItems().remove(this.Projectile);
            } else {
                this.Map.repackItems();
            }
        }
    }

    private void LineProc(int x, int y, RefObject<Boolean> refContinue)
    {
        this.Steps++;
        NWGameSpace space = this.Creature.getSpace();

        if (!this.Map.isValid(x, y)) {
            boolean gpChanged = false;

            int fx = this.Map.getCoords().X;
            int fy = this.Map.getCoords().Y;
            int px = x;
            int py = y;

            GlobalPosition gpi = new GlobalPosition(fx, fy, px, py, gpChanged);
            gpi.checkPos();
            fx = gpi.fx;
            fy = gpi.fy;
            px = gpi.px;
            py = gpi.py;
            gpChanged = gpi.globalChanged;

            NWLayer layer = this.Map.getLayer();
            if (fx >= 0 && fx != layer.getW() && fy >= 0 && fy != layer.getH() && gpChanged) {
                this.Map.getItems().extract(this.Projectile);
                this.Map = space.getField(this.Creature.LayerID, fx, fy);
                this.Map.getItems().add(this.Projectile, false);
                this.Projectile.setPos(px, py);
            }

            refContinue.argValue = false;
        } else {
            if (this.Map.isBarrier(x, y)) {
                this.Hit = HIT_BARRIER;
                refContinue.argValue = false;
            } else if (this.Steps > this.Range) {
                this.Hit = HIT_NONE;
                refContinue.argValue = false;
            } else {
                NWCreature target = (NWCreature) this.Map.findCreature(x, y);

                if (target != null && !target.equals(this.Creature)) {
                    this.Creature.attackTo(this.Kind, target, this.Weapon, this.Projectile);
                    this.Projectile.setPos(x, y);
                    this.Hit = HIT_BODY;

                    this.Projectile.applyEffects(target, InvokeMode.im_Use, null);
                    
                    refContinue.argValue = false;
                }

                if (refContinue.argValue) {
                    this.Projectile.setPos(x, y);
                }
            }
        }

        if (space.getPlayer().isSeen(x, y, false)) {
            space.repaintView(25);
        }
    }
}
