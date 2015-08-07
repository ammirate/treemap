package com.redhat.thermostat.treemap.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.thermostat.treemap.model.TreeMapNode;

/**
 * This class is able to test just public methods for {@link TreeMapBuilder} 
 * class, which cover about 40% of the code. But is possible to verify its 
 * goodness testing its wrapping class, i.e. {@link SquarifiedTreeMapTest}.
 */
public class TreeMapBuilderTest {
    
    private TreeMapBuilder builder;
    private static Rectangle2D.Double rectangle;
    
    @BeforeClass
    public static void oneTimeSetUp() {
        rectangle = new Rectangle2D.Double(0, 0, 6, 4);
    }
 
    @Before
    public void setUp() {
        builder = new TreeMapBuilder(rectangle);
    }
 
    @After
    public void tearDown() {
        builder = null;
    }
    
    @Test
    public void testTreeMapBuilder() {
        boolean catched = false;
        try {
            builder = new TreeMapBuilder(null);
        } catch(TreeMapException e) {
            catched = true;
        }
        assertTrue(catched);
    }

    @Test
    public final void testGetPrincipalSide() {
        assertTrue(builder.getPrincipalSide() == 4);
        builder = new TreeMapBuilder(new Rectangle2D.Double(0, 0, 2, 4));
        assertTrue(builder.getPrincipalSide() == 2);
    }

    @Test
    public final void testBestAspectRatio() {
        TreeMapNode node1 = new TreeMapNode(1);
        node1.setRectangle(new Rectangle2D.Double(0, 0, 1, 1)); // aspect ratio 1
       
        TreeMapNode node2 = new TreeMapNode(1);
        node2.setRectangle(new Rectangle2D.Double(0, 0, 2, 0.5)); // aspect ratio 4
        
        List<TreeMapNode> nodes = new ArrayList<>();
        nodes.add(node1);
        nodes.add(node2);
        assertTrue(builder.bestAspectRatio(nodes, 1) == 4);
    }

    @Test
    public final void testPrepareData() {
        TreeMapNode root = new TreeMapNode(1);
        TreeMapNode n0 = new TreeMapNode(6);
        TreeMapNode n1 = new TreeMapNode(6);
        TreeMapNode n2 = new TreeMapNode(4);
        TreeMapNode n3 = new TreeMapNode(3);
        TreeMapNode n4 = new TreeMapNode(2);
        TreeMapNode n5 = new TreeMapNode(2);
        TreeMapNode n6 = new TreeMapNode(1);
        TreeMapNode n7 = new TreeMapNode(1);
        root.addChild(n0);
        root.addChild(n1);
        root.addChild(n2);
        root.addChild(n3);
        root.addChild(n4);
        root.addChild(n5);
        root.addChild(n6);
        
        List<TreeMapNode> elements = new ArrayList<>();
        elements.add(n0);
        elements.add(n1);
        elements.add(n2);
        elements.add(n3);
        elements.add(n4);
        elements.add(n5);
        elements.add(n6);
        elements.add(n7);
        
        List<TreeMapNode> oracle = new ArrayList<>(elements);

        
        Rectangle2D.Double biggerR = new Rectangle2D.Double();
        biggerR.width = rectangle.getWidth() * 10;
        biggerR.height = rectangle.getHeight() * 10;
        
        builder = new TreeMapBuilder(biggerR);
        builder.prepareData(elements);
        
        int sum = 6 + 6 + 4 + 3 + 2 + 2 + 1;
        int area = 6 * 4;
        
        // adapt weight in proportion to the rectangle area
        for (TreeMapNode node : oracle) {
            node.setWeight((int) Math.round(node.getWeight() / sum * area));
        }
        
        for (int i=0; i < elements.size(); i++) {
            assertTrue(elements.get(i).getWeight() == oracle.get(i).getWeight());
        }
    }

    @Test
    public final void testWillImprove() {
        assertFalse(builder.willImprove(1, 4));
        assertTrue(builder.willImprove(4, 1));
    }

}
