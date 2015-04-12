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
package nwr.data.types;

import nwr.core.types.ItemState;
import nwr.core.types.ItemStates;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Serg V. Zhdanovskih
 */
public class ItemStatesTest
{
    
    public ItemStatesTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }


    /**
     * Test of getValue method, of class ItemStates.
     */
    @Test
    public void testGetValue()
    {
        System.out.println("getValue");
        ItemStates instance = new ItemStates(ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(6, instance.getValue());

        instance = new ItemStates(ItemState.is_Normal, ItemState.is_Cursed);
        assertEquals(5, instance.getValue());
    }

    /**
     * Test of setValue method, of class ItemStates.
     */
    @Test
    public void testSetValue()
    {
        System.out.println("setValue");
        ItemStates instance = new ItemStates();
        instance.setValue(5);
        assertEquals(5, instance.getValue());
        assertEquals(true, instance.containsAll(ItemState.is_Normal, ItemState.is_Cursed));
    }

    /**
     * Test of clear method, of class ItemStates.
     */
    @Test
    public void testClear()
    {
        System.out.println("clear");
        ItemStates instance = new ItemStates(ItemState.is_Normal, ItemState.is_Cursed);
        assertEquals(5, instance.getValue());
        instance.clear();
        assertEquals(0, instance.getValue());
    }

    /**
     * Test of isEmpty method, of class ItemStates.
     */
    @Test
    public void testIsEmpty()
    {
        System.out.println("isEmpty");
        ItemStates instance = new ItemStates(ItemState.is_Normal, ItemState.is_Cursed);
        assertEquals(5, instance.getValue());
        assertEquals(false, instance.isEmpty());
        instance.clear();
        assertEquals(true, instance.isEmpty());
    }

    /**
     * Test of getSignature method, of class ItemStates.
     */
    @Test
    public void testGetSignature()
    {
        System.out.println("getSignature");
        ItemStates instance = new ItemStates(ItemState.is_Normal, ItemState.is_Cursed);
        assertEquals("", instance.getSignature());
    }

    /**
     * Test of setSignature method, of class ItemStates.
     */
    @Test
    public void testSetSignature()
    {
        System.out.println("setSignature");
        ItemStates instance = new ItemStates();
        instance.setSignature("");
        assertEquals("", instance.getSignature());
    }

    /**
     * Test of include method, of class ItemStates.
     */
    @Test
    public void testInclude_1args_1()
    {
        System.out.println("include");
        ItemStates instance = new ItemStates();
        assertEquals(true, instance.isEmpty());
        instance.include(ItemState.is_Cursed);
        assertEquals(true, instance.contains(ItemState.is_Cursed));
        assertEquals(4, instance.getValue());
    }

    /**
     * Test of include method, of class ItemStates.
     */
    @Test
    public void testInclude_1args_2()
    {
        System.out.println("include");
        ItemStates instance = new ItemStates();
        assertEquals(true, instance.isEmpty());
        instance.include(ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(false, instance.contains(ItemState.is_Normal));
        assertEquals(true, instance.contains(ItemState.is_Blessed));
        assertEquals(true, instance.contains(ItemState.is_Cursed));
        assertEquals(6, instance.getValue());
    }

    /**
     * Test of exclude method, of class ItemStates.
     */
    @Test
    public void testExclude()
    {
        System.out.println("exclude");
        ItemStates instance = new ItemStates(ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(6, instance.getValue());
        instance.exclude(ItemState.is_Blessed);
        assertEquals(4, instance.getValue());
    }

    /**
     * Test of contains method, of class ItemStates.
     */
    @Test
    public void testContains()
    {
        System.out.println("contains");
        ItemStates instance = new ItemStates(ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(false, instance.contains(ItemState.is_Normal));
        assertEquals(true, instance.contains(ItemState.is_Blessed));
        assertEquals(true, instance.contains(ItemState.is_Cursed));
    }

    /**
     * Test of containsAll method, of class ItemStates.
     */
    @Test
    public void testContainsAll()
    {
        System.out.println("containsAll");
        ItemStates instance = new ItemStates(ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(true, instance.containsAll(ItemState.is_Blessed, ItemState.is_Cursed));
    }

    /**
     * Test of hasIntersect method, of class ItemStates.
     */
    @Test
    public void testHasIntersect_ItemStates()
    {
        System.out.println("hasIntersect");
        ItemStates instance = new ItemStates(ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(true, instance.hasIntersect(ItemState.is_Normal, ItemState.is_Cursed));
    }

    /**
     * Test of hasIntersect method, of class ItemStates.
     */
    @Test
    public void testHasIntersect_GenericType()
    {
        System.out.println("hasIntersect");

        ItemStates instance1 = new ItemStates(ItemState.is_Blessed, ItemState.is_Cursed);
        ItemStates instance2 = new ItemStates(ItemState.is_Normal, ItemState.is_Cursed);
        assertEquals(true, instance1.hasIntersect(instance2));
    }

    /**
     * Test of equals method, of class ItemStates.
     */
    @Test
    public void testEquals()
    {
        System.out.println("equals");

        ItemStates instance1 = new ItemStates(ItemState.is_Blessed, ItemState.is_Cursed);

        ItemStates instance2 = new ItemStates(ItemState.is_Normal, ItemState.is_Cursed);
        assertEquals(false, instance1.equals(instance2));
        
        instance2 = new ItemStates(ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(true, instance1.equals(instance2));
    }

    /**
     * Test of hashCode method, of class ItemStates.
     */
    @Test
    public void testHashCode()
    {
        System.out.println("hashCode");
        ItemStates instance = new ItemStates();
        assertEquals(295, instance.hashCode());
    }

    /**
     * Test of add method, of class ItemStates.
     */
    @Test
    public void testAdd()
    {
        System.out.println("add");
        ItemStates instance = new ItemStates(ItemState.is_Normal);
        assertEquals(1, instance.getValue());

        ItemStates instance2 = new ItemStates(ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(6, instance2.getValue());

        instance.add(instance2);
        assertEquals(7, instance.getValue());
    }

    /**
     * Test of sub method, of class ItemStates.
     */
    @Test
    public void testSub()
    {
        System.out.println("sub");
        ItemStates instance = new ItemStates(ItemState.is_Normal, ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(7, instance.getValue());

        ItemStates instance2 = new ItemStates(ItemState.is_Blessed);
        assertEquals(2, instance2.getValue());

        instance.sub(instance2);
        assertEquals(5, instance.getValue());
    }

    /**
     * Test of mul method, of class ItemStates.
     */
    @Test
    public void testMul()
    {
        System.out.println("mul");
        ItemStates instance = new ItemStates(ItemState.is_Normal, ItemState.is_Blessed, ItemState.is_Cursed);
        assertEquals(7, instance.getValue());

        ItemStates instance2 = new ItemStates(ItemState.is_Blessed);
        assertEquals(2, instance2.getValue());

        instance.mul(instance2);
        assertEquals(2, instance.getValue());
    }
}
