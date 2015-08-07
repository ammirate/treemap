package com.redhat.thermostat.treemap.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.treemap.model.TreeMapNode;

/**
 * Using eclEmma tool has been proved that this test covers 100% 
 * of {@link SquarifiedTreeMap} code and also 90% of {@link TreeMapBuilder} code.
 */
public class SquarifiedTreeMapTest {
    
    private SquarifiedTreeMap algorithm;
    Rectangle2D.Double bounds;
    List<TreeMapNode> list;

    @Before
    public void setUp() throws Exception {
        bounds = new Rectangle2D.Double(0, 0, 10, 5);
        list = new ArrayList<>();
    }

    @After
    public void tearDown() throws Exception {
        bounds = null;
        list = null;
    }

    @Test
    public final void testSquarifiedTreeMap() {
        //check every parameters combinations
        boolean catched = false;
        try {
            algorithm = new SquarifiedTreeMap(null, null);
        } catch(TreeMapException e) {
            catched = true;
        }
        assertTrue(catched);
        catched = false;
        
        try {
            algorithm = new SquarifiedTreeMap(bounds, null);
        } catch(TreeMapException e) {
            catched = true;
        }
        assertTrue(catched);
        catched = false;
        
        try {
            algorithm = new SquarifiedTreeMap(null, list);
        } catch(TreeMapException e) {
            catched = true;
        }
        assertTrue(catched);
    }
    
    @Test
    public final void testSquarefy() {
        // test using an empty node list
        algorithm = new SquarifiedTreeMap(bounds, new ArrayList<TreeMapNode>());
        assertEquals(0, algorithm.squarefy().size());
        
        // test using a correct list
        int n = 10;
        for (int i = 0; i < n; i++) {
            list.add(new TreeMapNode(i+1));
        }
        // process the list
        algorithm = new SquarifiedTreeMap(bounds, list);
        list = algorithm.squarefy();
        
        assertEquals(n, list.size());
        
        for (int i = 0; i < n; i++) {
            // node has been processed
            assertNotNull(list.get(i).getRectangle());
        }
        
        assertEquals(list, algorithm.getSquarifiedNodes());
    }
}
