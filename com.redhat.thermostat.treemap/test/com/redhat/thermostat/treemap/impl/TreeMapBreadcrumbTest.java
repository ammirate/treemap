package com.redhat.thermostat.treemap.impl;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.redhat.thermostat.treemap.model.TreeMapNode;

public class TreeMapBreadcrumbTest {
    
    private TreeMapBreadcrumb bc;
    private TreeMapComponent treemap;


    @Before
    public void setUp() throws Exception {
        treemap = Mockito.mock(TreeMapComponent.class);
    }

    @Test
    public final void testTreeMapBreadcrumb() {
        
        boolean catched = false;
        // must throw exception
        try {
            bc = new TreeMapBreadcrumb(null, null);
        } catch(NullPointerException e) {
            catched = true;
        }
        assertTrue(catched);
        catched = false;
        
        // must throw exception
        try {
            bc = new TreeMapBreadcrumb(null, new TreeMapNode(0));
        } catch(NullPointerException e) {
            catched = true;
        }
        assertTrue(catched);
        catched = false;
        
        // must throw exception
        try {
            bc = new TreeMapBreadcrumb(treemap, null);
        } catch(NullPointerException e) {
            catched = true;
        }
        assertTrue(catched);
        catched = false;
        
        // no exception
        try {
            bc = new TreeMapBreadcrumb(treemap, new TreeMapNode(0));
        } catch(NullPointerException e) {
            catched = true;
        }
        assertFalse(catched);
    }

}
