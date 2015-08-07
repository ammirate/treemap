package com.redhat.thermostat.treemap.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.treemap.impl.TreeMapComponent.Comp;
import com.redhat.thermostat.treemap.model.TreeMapNode;

/**
 * Using eclEmma tool has been proved that this test covers 75% 
 * of {@link TreeProcessor} code. What has not been tested is the graphic 
 * event handling methods.
 */
public class TreeMapComponentTest {

    private TreeMapComponent treeMap;
    private TreeMapNode tree;
    TreeMapNode node1, node2;
    private Dimension dim;

    @Before
    public void setUp() throws Exception {
        dim = new Dimension(500, 500);
        tree = new TreeMapNode(1);
        
        node1 = new TreeMapNode(1);
        node2 = new TreeMapNode(1);
        tree.addChild(node1);
        node1.addChild(node2);
        
        treeMap = new TreeMapComponent(tree, dim);
    }


    @Test
    public final void testTreeMapComponent() {
        boolean catched = false;

        try {
            treeMap = new TreeMapComponent(null, null);
        } catch(TreeMapException e) {
            catched = true;
        }
        assertTrue(catched);
        catched = false;

        try {
            treeMap = new TreeMapComponent(tree, null);
        } catch(TreeMapException e) {
            catched = true;
        }
        assertTrue(catched);
        catched = false;

        try {
            treeMap = new TreeMapComponent(null, dim);
        } catch(TreeMapException e) {
            catched = true;
        }
        assertTrue(catched);
    }

    @Test
    public final void testGetRoot() {
        assertEquals(tree, treeMap.getTreeMapRoot());
    }

    @Test
    public final void testZoomIn() {
        treeMap.zoomIn(node1);
        assertEquals(node1, treeMap.getTreeMapRoot());

        treeMap.zoomIn(node2);
        assertEquals(node2, treeMap.getTreeMapRoot());
    }

    @Test
    public final void testZoomOut() {
        treeMap.zoomOut();
        assertEquals(tree, treeMap.getTreeMapRoot());

        treeMap.zoomIn(node1); //if zoom out root is tree
        treeMap.zoomIn(node2); //if zoom out root is node1

        treeMap.zoomOut();
        assertEquals(node1, treeMap.getTreeMapRoot());

        treeMap.zoomOut();
        assertEquals(tree, treeMap.getTreeMapRoot());
    }

    @Test
    public final void testZoomFull() {
        treeMap.zoomIn(node2);
        treeMap.zoomFull();
        assertEquals(tree, treeMap.getTreeMapRoot());
    }

    @Test
    public final void testGetZoomCallsStack() {
        // the root is always in the stack
        assertEquals(1, treeMap.getZoomCallsStack().size());

        treeMap.zoomIn(tree);
        // zooming on the same element nothing happen
        assertEquals(1, treeMap.getZoomCallsStack().size());

        treeMap.zoomIn(node1);
        treeMap.zoomIn(node2);
        treeMap.zoomFull();
        assertEquals(tree, treeMap.getTreeMapRoot());
    }


    @Test
    public final void testClearZoomCallsStack() {
        treeMap.clearZoomCallsStack();
        assertEquals(1, treeMap.getZoomCallsStack().size());

        treeMap.zoomIn(node1);
        treeMap.zoomIn(node2);
        treeMap.clearZoomCallsStack();
        assertEquals(1, treeMap.getZoomCallsStack().size());
    }

    @Test
    public final void testObservers() {
        TreeMapObserver observer = new TreeMapObserver() {

            @Override
            public void notifySelection(TreeMapNode node) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void notifyZoomIn(TreeMapNode node) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void notifyZoomOut() {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void notifyZoomFull() {
                // TODO Auto-generated method stub
                
            }

        };

        assertEquals(0, treeMap.getObservers().size());

        treeMap.register(observer);
        assertEquals(1, treeMap.getObservers().size());

        treeMap.unregister(observer);
        assertEquals(0, treeMap.getObservers().size());
    }
}
