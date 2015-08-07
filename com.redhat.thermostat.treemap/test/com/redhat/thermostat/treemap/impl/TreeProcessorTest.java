package com.redhat.thermostat.treemap.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.treemap.model.TreeMapNode;

/**
 * Using eclEmma tool has been proved that this test covers 100% 
 * of {@link TreeProcessor} code.
 */
public class TreeProcessorTest {

    TreeMapNode node;
    Rectangle2D.Double area;
    TreeProcessor processor;

    @Before
    public void setUp() throws Exception {
        node = new TreeMapNode(1);
        area = new Rectangle2D.Double(0, 0, 500, 500);
        processor = null;
    }

    @Test
    public final void testTreeProcessor() {
        boolean catched = false;
        // this test check all wrong combinations for constructor parameters
        try {
            processor = new TreeProcessor(null, area);
        } catch(TreeMapException e) {
            // exception catched
            catched = true;
        }
        assertTrue(catched);
        catched = false;

        try {
            processor = new TreeProcessor(node, null);
        } catch(TreeMapException e) {
            // exception catched
            catched = true;
        }
        assertTrue(catched);
        catched = false;

        try {
            processor = new TreeProcessor(null, null);
        } catch(TreeMapException e) {
            // exception catched
            catched = true;
        }
        assertTrue(catched);
    }

    @Test
    public final void testCheckValue() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Method checkValue = TreeProcessor.class.getDeclaredMethod("checkValue", double.class);
        checkValue.setAccessible(true);
        processor = new TreeProcessor(node, area);
        
        double positiveResult = (double) checkValue.invoke(processor, new Double(1));
        assertTrue(positiveResult == 1);
        
        double negativeResult = (double) checkValue.invoke(processor, new Double(-1));
        assertTrue(negativeResult == 0);
    }

    @Test
    public final void testProcessTreeMap() {
        generateTree(node, 5, 5);
        processor = new TreeProcessor(node, area);
        processor.processTreeMap();

        // the test will check if any drawable node in the tree has a rectangle and a 
        // color, which means the processor function has processed the whole tree
        traverse(node);        
    }

    private void traverse(TreeMapNode tree) {
        if (tree.isDrawable() && (tree.getRectangle() == null || tree.getColor() == null)) {
            fail("node " + tree.getId() + " not processed");
        }
        for (TreeMapNode child : tree.getChildren()) {
            traverse(child);
        }
    }

    private void generateTree(TreeMapNode root, int levels, int childrenNumber) {        
        if (levels == 0) {
        } else {
            for (int i = 0; i < childrenNumber; i++) {
                root.addChild(new TreeMapNode(100));
            }
            for (TreeMapNode child : root.getChildren()) {
                generateTree(child, levels-1, childrenNumber);
            }
        }
    }
}
