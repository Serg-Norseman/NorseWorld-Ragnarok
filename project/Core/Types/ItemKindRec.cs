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

using NWR.Database;

namespace NWR.Core.Types
{
    public enum EquipMode
    {
        emNone,
        emHold,
        emWear
    }

    public sealed class ItemKindRec
    {
        public int Name;
        public byte Order;
        public int Color;
        public bool RndImage;
        public byte Images;
        public string ImageSign;
        public AbilityID Ability;
        public ItemFlags Flags;
        public EquipMode EquipMode;
        public int ImageIndex;
        public int ImagesLoaded;

        public static ItemKindRec Create(int name, byte order, int color,
            bool rndImage, byte images, string imageSign,
            AbilityID ability, ItemFlags flags, EquipMode equipMode)
        {
            ItemKindRec result = new ItemKindRec();
            result.Name = name;
            result.Order = order;
            result.Color = color;
            result.RndImage = rndImage;
            result.Images = images;
            result.ImageSign = imageSign;
            result.Ability = ability;
            result.Flags = flags;
            result.EquipMode = equipMode;
            result.ImageIndex = -1;
            result.ImagesLoaded = 0;
            return result;
        }
    }
}
