package com.redhat.thermostat.treemap.impl;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import com.redhat.thermostat.treemap.model.TreeMapNode;
import com.redhat.thermostat.treemap.util.ColorManager;


public class TreeProcessor {

    private TreeMapNode tree;

    private SquarifiedTreeMap algorithm;

    /**
     * Padding between the main component and its sub component.
     */
    public static final int X_PADDING = 15;
    public static final int Y_PADDING = 20;

    
    /**
     * Constructor. It use the area given in input as the reference size to 
     * build the TreeMap, assigning it to the root element.
     * @param root the tree's root.
     * @param area the available area to draw the TreeMap.
     */
    public TreeProcessor(TreeMapNode root, Rectangle2D.Double area) {
        if (root == null || area == null) {
            throw new TreeMapException(this.getClass() + " constructor does not allow null parameters");
        }
        this.tree = root;
        tree.setRectangle(new Rectangle2D.Double(0, 0, area.width, area.height));
        
    }

    /**
     * This method process recursively the tree nested in the node element
     * passed as argument in the constructor calculating the children TreeMap 
     * for each node also applying coloring.
     * @return the updated tree, where nodes have additional information like
     * {@link Rectangle2D>Float} instance and a color.
     */
    public TreeMapNode processTreeMap() {
        ColorManager colorManager = ColorManager.getInstance();
        colorManager.reset();
        if (tree.getColor() == null) {
            tree.setColor(colorManager.getNextColor(colorManager.getNextColor()));
        }
        
        process(tree);
        return tree;
    }

    /**
     * This method is used to effectively process the whole tree structure. It
     * uses a {@link SquarifiedTreeMap} object to calculate a TreeMap for each
     * node who has children.
     * @param node the subtree's root to process
     */
    private void process(TreeMapNode node) {                                                                                                                            
        
        this.algorithm = new SquarifiedTreeMap(getSubArea(node.getRectangle()), node.getChildren());
        node.setChildren(algorithm.squarefy());
        Color c = ColorManager.getInstance().getNextColor(node.getColor());

        for (TreeMapNode child : node.getChildren()) {
            if (child.getColor() == null) {
                child.setColor(c);
            }
            // if squarified rectangles have drawable sides then continue to 
            // process, else don't process the subtree having as root a 
            // non drawable rectangle.
            if (child.isDrawable()) {
                process(child);
            } 
        }
    }

    /**
     * Calculate space and coordinates in which children's rectangle will be 
     * drawn, from the main component.
     * @return the rectangle representing the new available area.
     */
    private Rectangle2D.Double getSubArea(Rectangle2D.Double area) {
        Rectangle2D.Double subArea = new Rectangle2D.Double();
        subArea.setRect(area);
        
        subArea.width = Math.max(0, (subArea.width - 2 * X_PADDING));
        subArea.height = Math.max(0, (subArea.height - 1.5 * Y_PADDING));
        return subArea;
    }  
}
